package org.covid19support.modules.users

import com.google.gson.JsonSyntaxException
import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.routing.*
import io.ktor.response.*
import io.ktor.sessions.*
import org.covid19support.DbSettings
import org.covid19support.SQLState
import org.covid19support.SessionAuth
import org.covid19support.authentication.Authenticator
import org.covid19support.authentication.Role
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.exceptions.*
import org.mindrot.jbcrypt.BCrypt
import org.covid19support.authentication.Token
import org.covid19support.constants.*
import java.lang.IllegalStateException

//TODO Lookup how to cancel a transaction (or perhaps just check before the transaction if role is being changed)
//TODO Logout user on email or role change (or at least, delete the old token and create a new one
//TODO Edit User overwrites isInstructor to false if nothing is provided
//TODO invalid data check doesn't work the way I thought I did... the invalid state exception was being thrown on the insert rather than the receive command

fun Application.users_module() {
    routing {
        route("/users") {
            get {
                val users: ArrayList<User> = arrayListOf()
                val page_size: Int = run { if (call.parameters["page_size"]?.toIntOrNull() != null) call.parameters["page_size"]!!.toInt() else 10}
                val current_page: Int = run { if (call.parameters["page"]?.toIntOrNull() != null) call.parameters["page"]!!.toInt() else 1}
                if (current_page < 1 || page_size < 1) {
                    call.respond(HttpStatusCode.BadRequest, Message("Invalid pagination values (Can't be less than 1)"))
                }
                else {
                    transaction(DbSettings.db) {
                        val results:List<ResultRow> = Users.selectAll().limit(page_size, offset = (page_size * (current_page-1)).toLong()).toList()
                        results.forEach {
                            users.add(Users.toUser(it))
                        }
                    }
                    if (users.isEmpty()) {
                        call.respond(HttpStatusCode.NoContent, Message("No users found!"))
                    }
                    else {
                        call.respond(HttpStatusCode.OK, users)
                    }
                }

            }

            post {
                try {
                    val newUser: User = call.receive()
                    var id:Int = -1
                    log.info(newUser.toString())
                    try {
                        var canAdd = true
                        var addingPrivilegedUser = false
                        if (newUser.role != Role.NORMAL.value && newUser.role != null) {
                            val authenticator = Authenticator(call)
                            if (authenticator.authenticate()) {
                                if (authenticator.getRole()!! > Role.MODERATOR) {
                                    addingPrivilegedUser = true
                                }
                                else {
                                    canAdd = false
                                    call.respond(HttpStatusCode.Forbidden, Message(FORBIDDEN))
                                }
                            }
                            else {
                                canAdd = false
                            }
                        }
                        if (canAdd) {
                            val passhash = BCrypt.hashpw(newUser.password, BCrypt.gensalt())
                            transaction (DbSettings.db) {
                                id = Users.insertAndGetId {
                                    it[email] = newUser.email
                                    it[password] = passhash
                                    it[first_name] = newUser.firstName
                                    it[last_name] = newUser.lastName
                                    it[description] = newUser.description
                                }.value
                            }
                            newUser.id = id
                            if (!addingPrivilegedUser) {
                                call.sessions.set(SessionAuth(Token.create(id, newUser.email, newUser.role)))
                            }
                            call.respond(HttpStatusCode.Created, newUser)
                        }
                    }
                    catch (ex:ExposedSQLException) {
                        log.error(ex.message)
                        when (ex.sqlState) {
                            SQLState.UNIQUE_CONSTRAINT_VIOLATION.code -> call.respond(HttpStatusCode.BadRequest, "Email already taken!")
                            SQLState.FOREIGN_KEY_VIOLATION.code -> call.respond(HttpStatusCode.BadRequest, ex.localizedMessage)
                            else -> call.respond(HttpStatusCode.InternalServerError, Message(INTERNAL_ERROR))
                        }
                    }
                }
                catch(ex:IllegalStateException) {
                    log.error(ex.message)
                    call.respond(HttpStatusCode.BadRequest, Message(INVALID_BODY))
                }
                catch(ex:JsonSyntaxException) {
                    log.error(ex.message)
                    call.respond(HttpStatusCode.BadRequest, Message(INVALID_BODY))
                }
            }

            route("/{id}") {
                get {
                    var user: User? = null
                    val id:Int? = call.parameters["id"]!!.toIntOrNull()
                    if (id != null) {
                        transaction(DbSettings.db) {
                            val result:ResultRow? = Users.select{Users.id eq id}.firstOrNull()

                            if (result != null) {
                                user = Users.toUser(result)
                            }

                        }
                        if (user == null) {
                            call.respond(HttpStatusCode.NoContent, Message("User not found!"))
                        }
                        else {
                            call.respond(user!!)
                        }
                    }
                    else {
                        call.respond(HttpStatusCode.BadRequest, Message("Must pass an integer value!"))
                    }
                }

                patch {
                    val authenticator = Authenticator(call)
                    if (authenticator.authenticate()) {
                        val id:Int? = call.parameters["id"]!!.toIntOrNull()
                        if (id != null) {
                            try {
                                val userInfo: User = call.receive()
                                var canEdit = false
                                if (authenticator.getRole()!! > Role.MODERATOR) {
                                    canEdit = true
                                }
                                else if (id == authenticator.getID()) {
                                    if (userInfo.role == null || userInfo.role == Role.NORMAL.value) {
                                        canEdit = true
                                    }
                                }
                                if (canEdit) {
                                    var result = 0
                                    transaction(DbSettings.db) {
                                        result = Users.update({Users.id eq id}) {
                                            if (userInfo.email != null) {
                                                it[email] = userInfo.email
                                            }
                                            if (userInfo.password != null) {
                                                it[password] = BCrypt.hashpw(userInfo.password, BCrypt.gensalt())
                                            }
                                            if (userInfo.firstName != null) {
                                                it[first_name] = userInfo.firstName
                                            }
                                            if (userInfo.lastName != null) {
                                                it[last_name] = userInfo.lastName
                                            }
                                            if (userInfo.description != null) {
                                                it[description] = userInfo.description
                                            }
                                            if (userInfo.isInstructor != null) {
                                                it[is_instructor] = userInfo.isInstructor
                                            }

                                            if (userInfo.role != null) {
                                                it[role] = userInfo.role
                                            }
                                        }
                                    }
                                    if (result == 1) {
                                        call.respond(HttpStatusCode.OK, Message("Successfully updated user!"))
                                    }
                                    else if (result == 0) {
                                        call.respond(HttpStatusCode.BadRequest, Message("User does not exist or no data was provided to update!"))
                                    }
                                    else {
                                        call.respond(HttpStatusCode.InternalServerError, Message(INTERNAL_ERROR))
                                    }

                                }
                                else {
                                    call.respond(HttpStatusCode.Forbidden, Message(FORBIDDEN))
                                }
                            }
                            catch (ex:ExposedSQLException) {
                                log.error(ex.message)
                                call.respond(HttpStatusCode.BadRequest, Message("Database Error"))
                            }
                            catch (ex:JsonSyntaxException) {
                                log.error(ex.message)
                                call.respond(HttpStatusCode.BadRequest, Message(INVALID_BODY))
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest, Message("Must pass an integer value!"))
                        }
                    }
                }

                delete {
                    val authenticator = Authenticator(call)
                    if (authenticator.authenticate()) {
                        val id:Int? = call.parameters["id"]!!.toIntOrNull()
                        if (id != null) {
                            try {
                                var canDelete = false
                                if (authenticator.getRole()!! > Role.MODERATOR)
                                {
                                    canDelete = true
                                }
                                else if (id == authenticator.getID()) {
                                    canDelete = true
                                }
                                if (canDelete) {
                                    var deleteResult: Int = 0
                                    transaction(DbSettings.db) {
                                        deleteResult = Users.deleteWhere { Users.id eq id }
                                    }
                                    if (deleteResult == 1) {
                                        if (id == authenticator.getID()) {
                                            call.sessions.clear<SessionAuth>()
                                        }
                                        call.respond(HttpStatusCode.OK, Message(DELETED))
                                    }
                                    else if (deleteResult == 0) {
                                        call.respond(HttpStatusCode.BadRequest, Message("User was not deleted, likely cause was it did not exist in the first place."))
                                    }

                                }
                                else {
                                    call.respond(HttpStatusCode.Forbidden, Message(FORBIDDEN))
                                }
                            }
                            catch (ex:ExposedSQLException) {
                                log.error(ex.message)
                                call.respond(HttpStatusCode.BadRequest, Message("Something went wrong, will implement more detailed response later."))
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest, Message("Must pass an integer value!"))
                        }
                    }
                }
            }
        }
    }
}