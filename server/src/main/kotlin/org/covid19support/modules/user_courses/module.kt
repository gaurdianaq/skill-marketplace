package org.covid19support.modules.user_courses

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.routing.*
import io.ktor.response.*
import org.covid19support.DbSettings
import org.covid19support.SQLState
import org.covid19support.constants.INTERNAL_ERROR
import org.covid19support.constants.INVALID_BODY
import org.covid19support.authentication.authenticate
import org.covid19support.constants.Message
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalStateException

fun Application.userCourses_module() {
    routing {
        route("/user_courses") {
            get {
                val userCourses: ArrayList<UserCourse> = arrayListOf()
                transaction(DbSettings.db) {
                    val results:List<ResultRow> = UserCourses.selectAll().toList()
                    results.forEach {
                        userCourses.add(UserCourses.toUserCourse(it))
                    }
                }
                if (userCourses.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent, Message("No user courses found!"))
                }
                else {
                    call.respond(userCourses)
                }
            }
            post {
                val decodedToken: DecodedJWT? = authenticate(call)
                if (decodedToken != null) {
                    try {
                        val userCourse: UserCourse = call.receive<UserCourse>()
                        try {
                            transaction(DbSettings.db) {
                                UserCourses.insert {
                                    it[user_id] = userCourse.userId
                                    it[course_id] = userCourse.courseId
                                    it[course_date] = userCourse.courseDate
                                    it[course_time] = userCourse.courseTime
                                    it[course_length] = userCourse.courseLength
                                }
                            }
                            call.respond(HttpStatusCode.Created, Message("UserCourse successfully submitted!"))
                        }
                        catch(ex:ExposedSQLException) {
                            log.error(ex.message)
                            when(ex.sqlState) {
                                SQLState.FOREIGN_KEY_VIOLATION.code -> call.respond(HttpStatusCode.BadRequest, Message(ex.localizedMessage))
                                else -> call.respond(HttpStatusCode.InternalServerError, Message(INTERNAL_ERROR))
                            }
                        }
                    }
                    catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, Message(INVALID_BODY))
                    }
                }
            }

            route("/{id}") {
                get {
                    var userCourse: UserCourse? = null
                    val id:Int = call.parameters["id"]!!.toInt()
                    transaction(DbSettings.db) {
                        val result:ResultRow? = UserCourses.select{ UserCourses.id eq id }.firstOrNull()
                        if (result != null) {
                            userCourse = UserCourses.toUserCourse(result)
                        }
                    }

                    if (userCourse == null) {
                        call.respond(HttpStatusCode.NoContent, Message("User course not found!"))
                    }
                    else {
                        call.respond(userCourse as UserCourse)
                    }
                }
            }
        }
    }
}