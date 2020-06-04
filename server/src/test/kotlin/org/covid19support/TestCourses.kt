package org.covid19support

import com.google.gson.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.sessions.*
import org.covid19support.authentication.Role
import org.covid19support.constants.Message
import org.covid19support.modules.courses.Course
import org.covid19support.modules.courses.CourseComponent
import org.covid19support.modules.courses.Courses
import org.covid19support.modules.courses.courses_module
import org.covid19support.modules.ratings.Rating
import org.covid19support.modules.ratings.Ratings
import org.covid19support.modules.session.Login
import org.covid19support.modules.session.session_module
import org.covid19support.modules.users.User
import org.covid19support.modules.users.Users
import org.covid19support.modules.users.users_module
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.*
import kotlin.test.*

class TestCourses : BaseTest() {
    //TODO Add Course Text Fields too Long
    //TODO Delete Courses
    //TODO Delete Courses
    //TODO Delete Courses Unauthenticated
    //TODO Delete Courses Unauthorized
    //TODO Delete Course when Users have Booked

    @Test
    fun addCourses() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
        courses_module()
    }) {
        val testUsers: Array<User> = arrayOf(
                User(null, "cheeseguy@cheesey.com", "gouldalover#123", "Brie", "Camenbert", "The cheesiest of cheesey people.", true),
                User(null, "test@test.org", "test", "Test", "McTesterson", "The head of the McTesterson Household", true),
                User(null, "anonymous@cantfindme.ca", "secretphrase" , "Mr.", "E", null, true)
        )
        for (user in testUsers) {
            with(handleRequest(HttpMethod.Post, Routes.USERS) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(user))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                user.id = gson.fromJson(response.content, User::class.java).id
            }
        }
        cookiesSession {
            val login: Login = Login(testUsers[0].email, testUsers[0].password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val testCourses: Array<Course> = arrayOf(
                    Course(null, "Cheese Appreciation", "The art of cheese appreciation is a fine one, but often misunderstood!", -1, "Cooking", 14.3f),
                    Course(null, "Cheese Making", "You'll learn how to make delicious cheese!", -1, "Cooking", 15.0f),
                    Course(null, "Cheese Photography", "You will learn how to truly capture the essence of cheese in your photography!", -1,"Photography/Film", 11f)
            )

            for (course in testCourses) {
                with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(course))
                }) {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
        }
        cookiesSession {
            val login: Login = Login(testUsers[1].email, testUsers[1].password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val testCourses: Array<Course> = arrayOf(
                    Course(null, "Unit Testing 101", "The art of proper unit testing! Let no bugs escape!", -1, "Coding", 17.7f),
                    Course(null, "1337 Coding", "You think you can code, but is your code 1337? After taking my course you'll be the envy of all the other coders!!!", -1, "Coding", 13.37f)
            )

            for (course in testCourses) {
                with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(course))
                }) {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
        }

        cookiesSession {
            val login: Login = Login(testUsers[2].email, testUsers[2].password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val testCourse = Course(null, "Unit Testing 101", "The art of proper unit testing! Let no bugs escape!", -1, "Coding", 17.7f)

            with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(testCourse))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
            }
        }

        with(handleRequest(HttpMethod.Get, Routes.COURSES)) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courses = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(6, courses.size)
        }
    }

    @Test
    fun addCoursesInvalidData() : Unit = withTestApplication ({
        main(true)
        users_module()
        session_module()
        courses_module()
    }) {
        val testUser = User(null, "cheeseguy@cheesey.com", "gouldalover#123", "Brie", "Camenbert", "The cheesiest of cheesey people.", true)
        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
            testUser.id = gson.fromJson(response.content, User::class.java).id
        }

        cookiesSession {
            val login: Login = Login(testUser.email, testUser.password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            val badData: JsonObject = JsonObject()
            badData.addProperty("lame", "data")
            with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(badData))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
            with(handleRequest(HttpMethod.Get, Routes.COURSES)) {
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun addCoursesUnauthenticated() : Unit = withTestApplication({
        main(true)
        users_module()
        courses_module()
    }) {
        val testCourses: Array<Course> = arrayOf(
                Course(null, "Cheese Appreciation", "The art of cheese appreciation is a fine one, but often misunderstood!", -1, "Cooking", 14.3f),
                Course(null, "Cheese Making", "You'll learn how to make delicious cheese!", -1, "Cooking", 15.0f),
                Course(null, "Cheese Photography", "You will learn how to truly capture the essence of cheese in your photography!", -1,"Photography/Film", 11f),
                Course(null, "Unit Testing 101", "The art of proper unit testing! Let no bugs escape!", -1, "Coding", 17.7f),
                Course(null, "1337 Coding", "You think you can code, but is your code 1337? After taking my course you'll be the envy of all the other coders!!!", -1, "Coding", 13.37f),
                Course(null, "Unit Testing 101", "The art of proper unit testing! Let no bugs escape!", -1, "Coding", 17.7f)
        )

        for (course in testCourses) {
            with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(course))
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        with(handleRequest(HttpMethod.Get, Routes.COURSES)) {
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            assertEquals(HttpStatusCode.NoContent, response.status())
        }
    }

    @Test
    fun addCoursesForeignKeyViolation() : Unit = withTestApplication ({
        main(true)
        users_module()
        session_module()
        courses_module()
    }) {
        val testUser = User(null, "cheeseguy@cheesey.com", "gouldalover#123", "Brie", "Camenbert", "The cheesiest of cheesey people.", true)
        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
            testUser.id = gson.fromJson(response.content, User::class.java).id
        }

        cookiesSession {
            val login: Login = Login(testUser.email, testUser.password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            val testCourse = Course(null, "Cheese Appreciation", "We teach you how to REALLY appreciate your cheese...", -1, "CheeseLover", 2.35f)
            with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(testCourse))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
            with(handleRequest(HttpMethod.Get, Routes.COURSES)) {
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun addCoursesNegativeRate() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
        courses_module()
    }) {
        val testUser = User(null, "cheeseguy@cheesey.com", "gouldalover#123", "Brie", "Camenbert", "The cheesiest of cheesey people.", true)
        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
            testUser.id = gson.fromJson(response.content, User::class.java).id
        }

        cookiesSession {
            val login: Login = Login(testUser.email, testUser.password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            val testCourse = Course(null, "Cheese Appreciation", "We teach you how to REALLY appreciate your cheese...", -1, "Cooking", -3.0f)
            val testCourse2 = Course(null, "Chocolate Appreciation", "We teach you how to REALLY appreciate your Chocolate...", -1, "Cooking", 0.0f)

            with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(testCourse))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }

            with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(testCourse2))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }

            with(handleRequest(HttpMethod.Get, Routes.COURSES)) {

                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun getCourseWithRatings() : Unit = withTestApplication({
        main(true)
        courses_module()
    }) {
        val instructor = User(null, "instructor@course.org", "password", "The", "Instructor", null, true)
        val users = arrayOf(
                User(null, "user1@user.org", "password", "User", "McUserson", null),
                User(null, "user2@user.org", "password", "User", "McUserson", null),
                User(null, "user3@user.org", "password", "User", "McUserson", null),
                User(null, "user4@user.org", "password", "User", "McUserson", null),
                User(null, "user5@user.org", "password", "User", "McUserson", null)
        )

        transaction(DbSettings.db) {
            instructor.id = Users.insertUserAndGetId(instructor)
            for (user in users) {
                user.id = Users.insertUserAndGetId(user)
            }
        }
        val course = Course(null, "Course", "A course", instructor.id!!, "Coding", 3f)
        transaction(DbSettings.db) {
            course.id = Courses.insertCourseAndGetId(course)
        }

        val ratings = arrayOf(
                Rating(users[0].id!!, course.id!!, 3, "Meh"),
                Rating(users[1].id!!, course.id!!, 1, "Shit"),
                Rating(users[2].id!!, course.id!!, 5, "Divine"),
                Rating(users[3].id!!, course.id!!, 4, "Good"),
                Rating(users[4].id!!, course.id!!, 4, "Great!!")
        )
        transaction(DbSettings.db) {
            for (rating in ratings) {
                Ratings.insertRating(rating)
            }
        }
        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
            val courseComponent = gson.fromJson(response.content, CourseComponent::class.java)
            assertEquals(instructor.id, courseComponent.instructor_id)
            assertEquals("${instructor.firstName} ${instructor.lastName}", courseComponent.instructor_name)
            assertEquals(course.id, courseComponent.course_id)
            assertEquals(course.name, courseComponent.course_name)
            assertEquals(course.category, courseComponent.course_category)
            assertEquals(course.description, courseComponent.course_description)
            assertEquals(course.rate, courseComponent.course_rate)
            assertEquals(3, courseComponent.course_rating)

        }
    }

    @Test
    fun getCourseNoRatings() : Unit = withTestApplication({
        main(true)
        courses_module()
    }) {
        val instructor = User(null, "instructor@course.org", "password", "The", "Instructor", null, true)

        transaction(DbSettings.db) {
            instructor.id = Users.insertUserAndGetId(instructor)
        }
        val course = Course(null, "Course", "A course", instructor.id!!, "Coding", 3f)
        transaction(DbSettings.db) {
            course.id = Courses.insertCourseAndGetId(course)
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
            val courseComponent = gson.fromJson(response.content, CourseComponent::class.java)
            assertEquals(instructor.id, courseComponent.instructor_id)
            assertEquals("${instructor.firstName} ${instructor.lastName}", courseComponent.instructor_name)
            assertEquals(course.id, courseComponent.course_id)
            assertEquals(course.name, courseComponent.course_name)
            assertEquals(course.category, courseComponent.course_category)
            assertEquals(course.description, courseComponent.course_description)
            assertEquals(course.rate, courseComponent.course_rate)
            assertEquals(null, courseComponent.course_rating)

        }
    }

    @Test
    fun getCoursesByParameters() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
        courses_module()
    }) {
        val testUsers: Array<User> = arrayOf(
                User(null, "cheeseguy@cheesey.com", "gouldalover#123", "Brie", "Camenbert", "The cheesiest of cheesey people.", true),
                User(null, "test@test.org", "test", "Test", "McTesterson", "The head of the McTesterson Household", true),
                User(null, "anonymous@cantfindme.ca", "secretphrase" , "Mr.", "E", null, true)
        )
        for (user in testUsers) {
            with(handleRequest(HttpMethod.Post, Routes.USERS) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(user))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                user.id = gson.fromJson(response.content, User::class.java).id
            }
        }

        cookiesSession {
            val login: Login = Login(testUsers[0].email, testUsers[0].password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val testCourses: Array<Course> = arrayOf(
                    Course(null, "Cheese Appreciation", "The art of cheese appreciation is a fine one, but often misunderstood!", -1, "Cooking", 14.3f),
                    Course(null, "Cheese Making", "You'll learn how to make delicious cheese!", -1, "Cooking", 15.0f),
                    Course(null, "Cheese Photography", "You will learn how to truly capture the essence of cheese in your photography!", -1,"Photography/Film", 11f),
                    Course(null, "Cheese Filming", "Filming cheese", -1, "Photography/Film", 11f),
                    Course(null, "Chocolate Appreciation", "Appreciating Chocolate", -1, "Cooking", 11f),
                    Course(null, "Chocolate Making", "Filming cheese", -1, "Cooking", 11f),
                    Course(null, "Chocolate Photography", "Photographic chocolate", -1, "Photography/Film", 11f),
                    Course(null, "Chocolate Filming", "Filming chocolate", -1, "Photography/Film", 11f),
                    Course(null, "Cheese Coding", "Writing cheesey code", -1, "Coding", 11f),
                    Course(null, "Cheese Art", "Arts & Crafts made from cheese! Cheese scupltures, cheese paintings...", -1, "Arts & Crafts", 5f)
            )

            for (course in testCourses) {
                with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(course))
                }) {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
        }

        cookiesSession {
            val login: Login = Login(testUsers[1].email, testUsers[1].password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val testCourses: Array<Course> = arrayOf(
                    Course(null, "Unit Testing 101", "The art of proper unit testing! Let no bugs escape!", -1, "Coding", 17.7f),
                    Course(null, "1337 Coding", "You think you can code, but is your code 1337? After taking my course you'll be the envy of all the other coders!!!", -1, "Coding", 13.37f),
                    Course(null, "Starting as a freelance coder!", "How to jumpstart your career as a freelance coder.", -1, "Business", 0.02f)
            )

            for (course in testCourses) {
                with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(course))
                }) {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
        }

        cookiesSession {
            val login: Login = Login(testUsers[2].email, testUsers[2].password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val testCourses = arrayOf(
                    Course(null, "Unit Testing 101", "The art of proper unit testing! Let no bugs escape!", -1, "Coding", 17.7f),
                    Course(null, "Unit Testing Art", "We design crafts dedicated to embodying the art of unit testing!", -1, "Arts & Crafts", 33.3f),
                    Course(null, "UI Testing", "We will test UI's!", -1, "UI/UX Design", 41.12f)
            )

            for (course in testCourses) {
                with(handleRequest(HttpMethod.Post, Routes.COURSES) {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(course))
                }) {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }

        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page_size=16")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courses = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(16, courses.size)
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courseComponents = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(5, courseComponents.size)
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courseComponents = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(7, courseComponents.size)
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business,Cooking,Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm&page_size=16")) {
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courseComponents = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(16, courseComponents.size)
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business,Cooking,Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courseComponents = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(10, courseComponents.size)
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business,Cooking,Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm&page=2")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courseComponents = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(6, courseComponents.size)
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?instructor_id=${testUsers[0].id}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courseComponents = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(10, courseComponents.size)
            for (courseComponent in courseComponents) {
                assertEquals(testUsers[0].id, courseComponent.instructor_id)
                assertEquals("${testUsers[0].firstName} ${testUsers[0].lastName}", courseComponent.instructor_name)
            }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?instructor_id=${testUsers[0].id}&categories=Cooking,Coding")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<CourseComponent>::class.java) }
            val courseComponents = gson.fromJson(response.content, Array<CourseComponent>::class.java)
            assertEquals(5, courseComponents.size)
            for (courseComponent in courseComponents) {
                assertEquals(testUsers[0].id, courseComponent.instructor_id)
                assertEquals("${testUsers[0].firstName} ${testUsers[0].lastName}", courseComponent.instructor_name)
                assert(courseComponent.course_category == "Cooking" || courseComponent.course_category == "Coding")
            }
        }
    }

    @Test
    fun getCourseNotFound() : Unit = withTestApplication ({
        main(true)
        courses_module()
    }) {
        with(handleRequest(HttpMethod.Get, Routes.COURSES)) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }
        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/3")) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }
    }

    @Test
    fun invalidPaginationValuesLessThan1() : Unit = withTestApplication({
        main(true)
        courses_module()
    }) {
        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page_size=0")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page_size=-1")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page=0")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page=-1")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page=-1&page_size=0")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page=-1&page_size=5")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?page=2&page_size=-1")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }
    }

    @Test
    fun editCourse() : Unit = withTestApplication({
        main(true)
        session_module()
        courses_module()
    }) {
        val user = User(null, "user@user.org", "password", "User", "McUserson", null, true)
        transaction(DbSettings.db) {
            user.id = Users.insertUserAndGetId(user)
        }
        val course = Course(null, "Course", "Description", user.id!!, "Coding", 5f)
        transaction(DbSettings.db) {
            course.id = Courses.insertCourseAndGetId(course)
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(user.email, user.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }
            var editCourseData = JsonObject()
            editCourseData.addProperty("name", "Cheese Appreciation")
            editCourseData.addProperty("description", "APPRECIATE THE CHEESE!")
            with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/${course.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editCourseData))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
                val editedCourseComponent = gson.fromJson(response.content, CourseComponent::class.java)
                assertEquals(course.rate, editedCourseComponent.course_rate)
                assertEquals(course.category, editedCourseComponent.course_category)
                assertEquals("Cheese Appreciation", editedCourseComponent.course_name)
                assertEquals("APPRECIATE THE CHEESE!", editedCourseComponent.course_description)
                assertEquals(course.instructorId, editedCourseComponent.instructor_id)
            }

            editCourseData = JsonObject()
            editCourseData.addProperty("description", "Seriously... cheese is amazing... It deserves the appreciation!")
            editCourseData.addProperty("category", "Cooking")
            editCourseData.addProperty("rate", 2.1f)
            with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/${course.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editCourseData))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
                val editedCourseComponent = gson.fromJson(response.content, CourseComponent::class.java)
                assertEquals(2.1f, editedCourseComponent.course_rate)
                assertEquals("Cooking", editedCourseComponent.course_category)
                assertEquals("Cheese Appreciation", editedCourseComponent.course_name)
                assertEquals("Seriously... cheese is amazing... It deserves the appreciation!", editedCourseComponent.course_description)
                assertEquals(course.instructorId, editedCourseComponent.instructor_id)
            }
        }
    }

    @Test
    fun editCourseBadDataValidField() : Unit = withTestApplication({
        main(true)
        session_module()
        courses_module()
    }) {
        val instructor = User(null, "instructor@instructor.cra", "password", "Doctor", "No!", null, true)
        transaction(DbSettings.db) {
            instructor.id = Users.insertUserAndGetId(instructor)
        }

        val course = Course(null, "Course", "Description", instructor.id!!, "Coding", 5f)
        transaction(DbSettings.db) {
            course.id = Courses.insertCourseAndGetId(course)
        }

        val editData = JsonObject()
        val subObject = JsonObject()
        subObject.addProperty("blah", "no")
        editData.addProperty("name", 5)
        editData.add("description", subObject)
        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(instructor.email, instructor.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }

            with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/${course.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editData))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
                val courseComponent = gson.fromJson(response.content, CourseComponent::class.java)
                assertEquals(course.name, courseComponent.course_name)
                assertEquals(course.description, courseComponent.course_description)
                assertEquals(course.category, courseComponent.course_category)
                assertEquals(course.rate, courseComponent.course_rate)
                assertEquals(course.instructorId, courseComponent.instructor_id)
            }
        }
    }

    @Test
    fun editCourseNoValidFields() : Unit = withTestApplication({
        main(true)
        session_module()
        courses_module()
    }) {
        val instructor = User(null, "instructor@instructor.cra", "password", "Doctor", "No!", null, true)
        transaction(DbSettings.db) {
            instructor.id = Users.insertUserAndGetId(instructor)
        }

        val course = Course(null, "Course", "Description", instructor.id!!, "Coding", 5f)
        transaction(DbSettings.db) {
            course.id = Courses.insertCourseAndGetId(course)
        }

        val editData = JsonObject()
        val subObject = JsonObject()
        subObject.addProperty("blah", "no")
        editData.addProperty("cheese", 5)
        editData.add("crazy", subObject)
        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(instructor.email, instructor.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }

            with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/${course.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editData))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
                val courseComponent = gson.fromJson(response.content, CourseComponent::class.java)
                assertEquals(course.name, courseComponent.course_name)
                assertEquals(course.description, courseComponent.course_description)
                assertEquals(course.category, courseComponent.course_category)
                assertEquals(course.rate, courseComponent.course_rate)
                assertEquals(course.instructorId, courseComponent.instructor_id)
            }
        }
    }

    @Test
    fun editCourseUnauthenticated() : Unit = withTestApplication({
        main(true)
        courses_module()
    }) {
        val user = User(null, "user@user.org", "password", "User", "McUserson", null, true)
        transaction(DbSettings.db) {
            user.id = Users.insertUserAndGetId(user)
        }
        val course = Course(null, "Course", "Description", user.id!!, "Cooking", 5f)
        transaction(DbSettings.db) {
            course.id = Courses.insertCourseAndGetId(course)
        }

        val courseEditData = JsonObject()
        courseEditData.addProperty("category", "Coding")
        courseEditData.addProperty("name", "NotCourse")

        with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/${course.id}"){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(courseEditData))
        }) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
            val courseComponent = gson.fromJson(response.content, CourseComponent::class.java)
            assertEquals(course.id, courseComponent.course_id)
            assertEquals(course.instructorId, courseComponent.instructor_id)
            assertEquals(course.category, courseComponent.course_category)
            assertEquals(course.rate, courseComponent.course_rate)
            assertEquals(course.description, courseComponent.course_description)
            assertEquals(course.name, courseComponent.course_name)
        }
    }

    @Test
    fun editCourseUnauthorized() : Unit = withTestApplication({
        main(true)
        session_module()
        courses_module()
    }) {
        val instructor = User(null, "instructor@instructor.net", "password", "Person", "No", null, true)
        val user = User(null, "user@user.net", "password", "NotPerson", "Yes", null)

        transaction(DbSettings.db) {
            instructor.id = Users.insertUserAndGetId(instructor)
            user.id = Users.insertUserAndGetId(user)
        }
        val course = Course(null, "Course", "Description", instructor.id!!, "Cooking", 5f)
        transaction {
            course.id = Courses.insertCourseAndGetId(course)
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(user.email, user.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }

            val editCourseData = JsonObject()
            editCourseData.addProperty("name", "notaname")

            with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/${course.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editCourseData))
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${course.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
                val courseComponent = gson.fromJson(response.content, CourseComponent::class.java)
                assertEquals(course.id, courseComponent.course_id)
                assertEquals(course.instructorId, courseComponent.instructor_id)
                assertEquals(course.category, courseComponent.course_category)
                assertEquals(course.rate, courseComponent.course_rate)
                assertEquals(course.description, courseComponent.course_description)
                assertEquals(course.name, courseComponent.course_name)
            }
        }
    }

    @Test
    fun editCoursesAsAdmin() : Unit = withTestApplication({
        main(true)
        courses_module()
        session_module()
    }) {
        val admin = User(null, "admin@boss.com", "adminpassword", "Admin", "Bossman", "The boss!", false, Role.ADMIN.value)
        val users = arrayOf(
                User(null, "user1@user.com", "password", "User", "McUserson", null),
                User(null, "user2@user.com", "password", "User", "McUserson", null),
                User(null, "user3@user.com", "password", "User", "McUserson", null)
        )

        transaction(DbSettings.db) {
            admin.id = Users.insertUserAndGetId(admin)
            for (user in users) {
                user.id = Users.insertUserAndGetId(user)
            }
        }

        val courses = arrayOf(
                Course(null, "Course", "Description", users[0].id!!, "Coding", 5f),
                Course(null, "Course", "Description", users[1].id!!, "Coding", 5f),
                Course(null, "Course", "Description", users[2].id!!, "Coding", 5f)
        )

        transaction(DbSettings.db) {
            for (course in courses) {
                course.id = Courses.insertCourseAndGetId(course)
            }
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(admin.email, admin.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }

            val editData = arrayOf(
                    JsonObject(), JsonObject(), JsonObject()
            )

            editData[0].addProperty("name", "Goulda")
            editData[1].addProperty("name", "Cheddar")
            editData[2].addProperty("name", "Swiss")

            for (i in 0..2) {
                with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/${courses[i].id}") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(editData[i]))
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, Message::class.java)}
                }

                with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/${courses[i].id}")) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, CourseComponent::class.java) }
                    val courseComponent = gson.fromJson(response.content, CourseComponent::class.java)
                    assertEquals(courses[i].id, courseComponent.course_id)
                    assertEquals(courses[i].instructorId, courseComponent.instructor_id)
                    assertEquals(courses[i].category, courseComponent.course_category)
                    assertEquals(courses[i].rate, courseComponent.course_rate)
                    assertEquals(courses[i].description, courseComponent.course_description)
                    assertEquals(editData[i].getAsJsonPrimitive("name").asString, courseComponent.course_name)
                }
            }
        }
    }

    @Test
    fun editCourseDoesNotExist() : Unit = withTestApplication({
        main(true)
        courses_module()
        session_module()
    }) {
        val user = User(null, "user@user.ca", "password", "User", "User", null)
        transaction(DbSettings.db) {
            user.id = Users.insertUserAndGetId(user)
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(user.email, user.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }
            val editData = JsonObject()
            editData.addProperty("name", "cheese")
            with(handleRequest(HttpMethod.Patch, "${Routes.COURSES}/5"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editData))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }
    }
}