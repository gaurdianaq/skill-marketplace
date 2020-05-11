package org.covid19support.modules.courses

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.response.*
import org.covid19support.DbSettings
import org.covid19support.SQLState
import org.covid19support.constants.INTERNAL_ERROR
import org.covid19support.constants.INVALID_BODY
import org.covid19support.authentication.authenticate
import org.covid19support.constants.Message
import org.covid19support.modules.ratings.Rating
import org.covid19support.modules.ratings.Ratings
import org.covid19support.modules.users.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalStateException

fun Application.courses_module() {
    routing {
        route("/courses") {
            get {
                val instructor_id: Int? = call.parameters["instructor_id"]?.toIntOrNull()
                val categories: List<String>? = call.parameters["categories"]?.split(',')
                val page_size: Int = run { if (call.parameters["page_size"]?.toIntOrNull() != null) call.parameters["page_size"]!!.toInt() else 10}
                val current_page: Int = run { if (call.parameters["page"]?.toIntOrNull() != null) call.parameters["page"]!!.toInt() else 1}
                val courses: HashMap<Int, ArrayList<Course>> = HashMap()
                val instructors: HashMap<Int, User> = HashMap()
                val ratings: HashMap<Int, ArrayList<Rating>> = HashMap()
                val courseComponents: ArrayList<CourseComponent> = arrayListOf()
                lateinit var coursesQuery: Query
                if (categories == null && instructor_id == null) {
                    coursesQuery = Courses.selectAll().limit(page_size, offset = (page_size * (current_page-1)).toLong())
                } else if (categories != null && instructor_id == null) {
                    coursesQuery = Courses.select { Courses.category inList categories }.limit(page_size, offset = (page_size * (current_page-1)).toLong())
                } else if (categories == null && instructor_id != null) {
                    coursesQuery = Courses.select { Courses.instructor_id eq instructor_id }.limit(page_size, offset = (page_size * (current_page-1)).toLong())
                } else if (categories != null && instructor_id != null) {
                    coursesQuery = Courses.select { (Courses.instructor_id eq instructor_id) and (Courses.category inList categories) }.limit(page_size, offset = (page_size * (current_page-1)).toLong())
                }

                transaction(DbSettings.db) {
                    coursesQuery.forEach {
                        val courseId: Int = it[Courses.id].value
                        val instructorId: Int = it[Courses.instructor_id]
                        if (courses[instructorId] == null) {
                            courses[instructorId] = arrayListOf()
                            courses[instructorId]?.add(Courses.toCourse(it))
                        } else {
                            courses[instructorId]?.add(Courses.toCourse(it))
                        }
                        Ratings.select { Ratings.course_id eq courseId }.forEach {
                            if (ratings[courseId] == null) {
                                ratings[courseId] = arrayListOf()
                                ratings[courseId]?.add(Ratings.toRating(it))
                            } else {
                                ratings[courseId]?.add(Ratings.toRating(it))
                            }
                        }
                        if (instructors[instructorId] == null) {
                            instructors[instructorId] = Users.toUser(Users.select { Users.id eq instructorId }.first())
                        }
                    }
                    Users.selectAll().forEach {
                        instructors[it[Users.id].value] = Users.toUser(it)
                    }
                }
                if (courses.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent, Message("No courses found!"))
                } else {
                    courses.forEach { (instructor_id, instructor_courses) ->
                        instructor_courses.forEach {
                            var rating: Short? = null
                            if (ratings[it.id] != null) {
                                rating = 0
                                ratings[it.id]?.forEach {
                                    rating = (rating!! + it.ratingValue).toShort()
                                }
                                rating = (rating!! / ratings[it.id]?.size!!).toShort()
                            }
                            courseComponents.add(CourseComponent(instructor_id,
                                    instructors[instructor_id]?.firstName + ' ' + instructors[instructor_id]?.lastName,
                                    it.id!!,
                                    it.name,
                                    it.description,
                                    rating,
                                    it.category,
                                    it.rate
                            ))
                        }
                    }
                    call.respond(HttpStatusCode.OK, courseComponents)
                }
            }
            post {
                val decodedToken: DecodedJWT? = authenticate(call)
                if (decodedToken != null) {
                    try {
                        val course: Course = call.receive<Course>()
                        try {
                            transaction(DbSettings.db) {
                                Courses.insert {
                                    it[name] = course.name
                                    it[description] = course.description
                                    it[instructor_id] = decodedToken.claims["id"]!!.asInt()
                                    it[category] = course.category
                                    it[rate] = course.rate
                                }
                            }
                            call.respond(HttpStatusCode.Created, Message("Successfully created course!"))
                        }
                        catch (ex: ExposedSQLException) {
                            log.error(ex.message)
                            when (ex.sqlState) {
                                SQLState.FOREIGN_KEY_VIOLATION.code -> call.respond(HttpStatusCode.BadRequest, Message(ex.localizedMessage))
                                SQLState.CHECK_VIOLATION.code -> call.respond(HttpStatusCode.BadRequest, Message(ex.localizedMessage))
                                else -> call.respond(HttpStatusCode.InternalServerError, Message(INTERNAL_ERROR))
                            }
                        }
                    }
                    catch (ex:IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, Message(INVALID_BODY))
                    }
                }
            }

            route("/{id}") {
                get {
                    var course: Course? = null
                    val id: Int = call.parameters["id"]!!.toInt()
                    transaction(DbSettings.db) {
                        val result: ResultRow? = Courses.select { Courses.id eq id }.firstOrNull()

                        if (result != null) {
                            course = Courses.toCourse(result)
                        }

                    }
                    if (course == null) {
                        call.respond(HttpStatusCode.NoContent, Message("Course not found!"))
                    }
                    else {
                        call.respond(course!!)
                    }
                }
            }

            route("/instructor") {
                get {
                    val decodedToken: DecodedJWT? = authenticate(call)
                    if (decodedToken != null) {
                        val courses: ArrayList<Course> = arrayListOf()
                        transaction(DbSettings.db) {
                            val results: List<ResultRow> = Courses.select { Courses.instructor_id eq decodedToken.claims["id"]!!.asInt() }.toList()
                            results.forEach {
                                courses.add(Courses.toCourse(it))
                            }
                        }
                        if (courses.isEmpty()) {
                            call.respond(HttpStatusCode.NoContent, Message("This user has not created any courses!"))
                        }
                        else {
                            call.respond(courses)
                        }
                    }
                }
            }
        }
    }
}