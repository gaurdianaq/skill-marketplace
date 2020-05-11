package org.covid19support.modules.ratings

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
import org.covid19support.modules.users.User
import org.covid19support.modules.users.Users
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalStateException

fun Application.ratings_module() {
    routing {
        route("/ratings") {
            post {
                val decodedToken: DecodedJWT? = authenticate(call)
                if (decodedToken != null) {
                    try {
                        val rating: Rating = call.receive<Rating>()
                        try {
                            transaction(DbSettings.db) {
                                Ratings.insert {
                                    it[user_id] = decodedToken.claims["id"]!!.asInt()
                                    it[course_id] = rating.courseId
                                    it[rating_value] = rating.ratingValue
                                    it[comment] = rating.comment
                                }
                            }
                            call.respond(HttpStatusCode.Created, Message("Rating successfully submitted!"))
                        } catch (ex: ExposedSQLException) {
                            log.error(ex.message)
                            when (ex.sqlState) {
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
            route("/course/{course_id}") {
                get {
                    val id: Int = call.parameters["course_id"]!!.toInt()
                    val page_size: Int = run { if (call.parameters["page_size"]?.toIntOrNull() != null) call.parameters["page_size"]!!.toInt() else 10}
                    val current_page: Int = run { if (call.parameters["page"]?.toIntOrNull() != null) call.parameters["page"]!!.toInt() else 1}
                    val ratings: ArrayList<Rating> = arrayListOf()
                    val users: ArrayList<User> = arrayListOf()
                    val ratingsComponents: ArrayList<RatingComponent> = arrayListOf()
                    transaction {
                        val results: List<ResultRow> = Ratings.select { Ratings.course_id eq id }.limit(page_size, offset = (page_size * (current_page-1)).toLong()).toList()
                        results.forEach {
                            ratings.add(Ratings.toRating(it))
                            users.add(Users.toUser(Users.select { Users.id eq ratings.last().userId }.first()))
                        }
                    }
                    for (i in 0 until ratings.size) {
                        ratingsComponents.add(RatingComponent(ratings[i].userId, ratings[i].courseId, ratings[i].ratingValue,
                                                              ratings[i].comment, users[i].firstName, users[i].lastName))
                    }
                    if (ratingsComponents.isEmpty()) {
                        call.respond(HttpStatusCode.NoContent, Message("No ratings found!"))
                    } else {
                        call.respond(ratingsComponents)
                    }
                }
            }
            route("/{course_id}/{user_id}") {
                get {
                    var rating: Rating? = null
                    var ratingUser: User? = null
                    val course_id: Int = call.parameters["course_id"]!!.toInt()
                    val user_id: Int = call.parameters["user_id"]!!.toInt()
                    transaction(DbSettings.db) {
                        val result: ResultRow? = Ratings.select { (Ratings.user_id eq user_id) and (Ratings.course_id eq course_id) }.firstOrNull()
                        if (result != null) {
                            rating = Ratings.toRating(result)
                        }
                        val userResult: ResultRow? = Users.select {Users.id eq rating?.userId}.firstOrNull()
                        if (userResult != null) {
                            ratingUser = Users.toUser(userResult)
                        }
                    }
                    if (rating == null) {
                        call.respond(HttpStatusCode.NoContent, Message("Rating not found!"))
                    } else {
                        val ratingComponent = RatingComponent(rating!!.userId, rating!!.courseId, rating!!.ratingValue, rating!!.comment, ratingUser!!.firstName, ratingUser!!.lastName)
                        call.respond(ratingComponent)
                    }
                }
            }
        }
    }
}