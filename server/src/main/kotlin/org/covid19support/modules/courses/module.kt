package org.covid19support.modules.courses

import com.auth0.jwt.interfaces.DecodedJWT
import com.google.gson.JsonSyntaxException
import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.response.*
import org.covid19support.DbSettings
import org.covid19support.SQLState
import org.covid19support.authentication.Authenticator
import org.covid19support.authentication.Role
import org.covid19support.constants.FORBIDDEN
import org.covid19support.constants.INTERNAL_ERROR
import org.covid19support.constants.INVALID_BODY
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
                if (page_size < 1 || current_page < 1) {
                    call.respond(HttpStatusCode.BadRequest, Message("Invalid pagination values (Can't be less than 1)"))
                }
                else {
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
            }
            post {
                val authenticator = Authenticator(call)
                if (authenticator.authenticate()) {
                    try {
                        val course: Course = call.receive<Course>()
                        try {
                            transaction(DbSettings.db) {
                                Courses.insert {
                                    it[name] = course.name
                                    it[description] = course.description
                                    it[instructor_id] = authenticator.getID()!!
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
                        log.error(ex.message)
                        call.respond(HttpStatusCode.BadRequest, Message(INVALID_BODY))
                    }
                    catch(ex: JsonSyntaxException) {
                        log.error(ex.message)
                        call.respond(HttpStatusCode.BadRequest, Message(INVALID_BODY))
                    }
                }
            }

            route("/{id}") {
                get {
                    var course: Course? = null
                    val id: Int = call.parameters["id"]!!.toInt()
                    val ratings: ArrayList<Rating> = arrayListOf()
                    lateinit var instructor: User
                    transaction(DbSettings.db) {
                        val result: ResultRow? = Courses.select { Courses.id eq id }.firstOrNull()

                        if (result != null) {
                            course = Courses.toCourse(result)
                            Ratings.select {Ratings.course_id eq id  }.forEach {
                                ratings.add(Ratings.toRating(it))
                            }
                            instructor = Users.toUser(Users.select { Users.id eq course!!.instructorId }.first())
                        }

                    }
                    if (course == null) {
                        call.respond(HttpStatusCode.NoContent, Message("Course not found!"))
                    }
                    else {
                        var averageRating: Short? = null
                        if (ratings.isNotEmpty()) {
                            averageRating = 0
                            for (rating in ratings) {
                                averageRating = (averageRating!! + rating.ratingValue).toShort()
                            }
                            averageRating = (averageRating!! / ratings.size).toShort()
                        }
                        val courseComponent = CourseComponent(course!!.instructorId, "${instructor.firstName} ${instructor.lastName}", course!!.id!!, course!!.name, course!!.description, averageRating, course!!.category, course!!.rate)
                        call.respond(HttpStatusCode.OK, courseComponent)
                    }
                }

                patch {
                    val authenticator = Authenticator(call)
                    if (authenticator.authenticate()) {
                        val id:Int? = call.parameters["id"]!!.toIntOrNull()
                        if (id != null) {
                            try {
                                val newCourseInfo: Course = call.receive()
                                lateinit var currentCourseInfo: Course
                                transaction(DbSettings.db) {
                                    val result = Courses.select { Courses.id eq id }.first()
                                    currentCourseInfo = Courses.toCourse(result)
                                }
                                var canEdit = false
                                if (authenticator.getRole()!! > Role.MODERATOR) {
                                    canEdit = true
                                }
                                else if (currentCourseInfo.instructorId == authenticator.getID()) {
                                    canEdit = true
                                }

                                if (canEdit) {
                                    var result = 0
                                    transaction(DbSettings.db) {
                                        result = Courses.update({ Courses.id eq id }) {
                                            if (newCourseInfo.name != null) {
                                                it[name] = newCourseInfo.name
                                            }
                                            if (newCourseInfo.category != null) {
                                                it[category] = newCourseInfo.category
                                            }
                                            if (newCourseInfo.description != null) {
                                                it[description] = newCourseInfo.description
                                            }
                                            if (newCourseInfo.rate > 0) {
                                                it[rate] = newCourseInfo.rate
                                            }
                                        }
                                    }

                                    when (result) {
                                        1 -> {
                                            call.respond(HttpStatusCode.OK, Message("Successfully updated user!"))
                                        }
                                        0 -> {
                                            call.respond(HttpStatusCode.BadRequest, Message("Course does not exist or no data was provided to update!"))
                                        }
                                        else -> {
                                            call.respond(HttpStatusCode.InternalServerError, Message(INTERNAL_ERROR))
                                        }
                                    }
                                }
                                else {
                                    call.respond(HttpStatusCode.Forbidden, Message(FORBIDDEN))
                                }
                            }
                            catch (ex:NoSuchElementException) {
                                log.error(ex.message)
                                call.respond(HttpStatusCode.BadRequest, Message("Course doesn't exist!"))
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
            }

            route("/instructor") {
                get {
                    val authenticator = Authenticator(call)
                    if (authenticator.authenticate()) {
                        val courses: ArrayList<Course> = arrayListOf()
                        transaction(DbSettings.db) {
                            val results: List<ResultRow> = Courses.select { Courses.instructor_id eq authenticator.getID()!! }.toList()
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