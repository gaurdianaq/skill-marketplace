package org.covid19support

import com.google.gson.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.covid19support.modules.courses.Course
import org.covid19support.modules.courses.courses_module
import org.covid19support.modules.session.Login
import org.covid19support.modules.session.session_module
import org.covid19support.modules.users.User
import org.covid19support.modules.users.users_module
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.*
import kotlin.test.*

class TestCourses : BaseTest() {
    //TODO Add Courses with Ratings
    //TODO Add Course Text Fields too Long
    //TODO Edit Course
    //TODO Edit Course Unauthenticated
    //TODO Edit Course Unauthorized
    //TODO Delete Courses
    //TODO Delete Courses
    //TODO Delete Courses Unauthenticated
    //TODO Delete Courses Unauthorized
    //TODO Delete Course when Users have Booked

    private fun validateCourseComponentFormat(component:JsonObject) : Boolean {
        if (component.has("instructor") && component.has("course")) {
            if(component.get("instructor").isJsonObject && component.get("course").isJsonObject) {
                val instructor:JsonObject = component.get("instructor").asJsonObject
                val course:JsonObject = component.get("course").asJsonObject
                if (instructor.has("id") && instructor.has("name")) {
                    if (instructor.get("id").isJsonPrimitive && instructor.get("name").isJsonPrimitive) {
                        val instructor_id:JsonPrimitive = instructor.getAsJsonPrimitive("id")
                        val instructor_name:JsonPrimitive = instructor.getAsJsonPrimitive("name")
                        if (!instructor_id.isNumber || !instructor_name.isString) {
                            return false
                        }
                    }
                    else {
                        return false
                    }
                }
                else {
                    return false
                }
                if (course.has("id") && course.has("name") && course.has("description")
                        && course.has("category") && course.has("rate")) {
                    if (course.get("id").isJsonPrimitive && course.get("name").isJsonPrimitive && course.get("description").isJsonPrimitive
                            && course.get("category").isJsonPrimitive && course.get("rate").isJsonPrimitive) {
                        val course_id: JsonPrimitive = course.getAsJsonPrimitive("id")
                        val course_name: JsonPrimitive = course.getAsJsonPrimitive("name")
                        val course_description: JsonPrimitive = course.getAsJsonPrimitive("description")
                        val course_category: JsonPrimitive = course.getAsJsonPrimitive("category")
                        val course_rate: JsonPrimitive = course.getAsJsonPrimitive("rate")
                        if (!course_id.isNumber || !course_name.isString || !course_description.isString || !course_category.isString || !course_rate.isNumber) {
                            return false
                        }
                    }
                }
                else {
                    return false
                }
            }
            else {
                return false
            }
        }
        else {
            return false
        }
        return true
    }

    @Test
    fun addCoursesNoRatings() = withTestApplication({
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
            val content = response.content
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courses: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            for (course in courses) {
                assertTrue(course.isJsonObject)
                assertTrue(validateCourseComponentFormat(course.asJsonObject))
            }
            assertEquals(6, courses.size())
        }
    }

    @Test
    fun addCoursesInvalidData() = withTestApplication ({
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
                assertDoesNotThrow { gson.fromJson(response.content, JsonObject::class.java) }
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun addCoursesUnauthenticated() = withTestApplication({
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
            assertDoesNotThrow { gson.fromJson(response.content, JsonObject::class.java) }
            assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
            assertEquals(HttpStatusCode.NoContent, response.status())
        }
    }

    @Test
    fun addCoursesForeignKeyViolation() = withTestApplication ({
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
                assertDoesNotThrow { gson.fromJson(response.content, JsonObject::class.java) }
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun addCoursesNegativeRate() = withTestApplication({
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
                assertDoesNotThrow { gson.fromJson(response.content, JsonObject::class.java) }
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun getCoursesByParameters() = withTestApplication({
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
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courses: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(16, courses.size())
            for (course in courses) {
                assertDoesNotThrow { gson.fromJson(course, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(course.asJsonObject))
            }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business")) {
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courseComponents: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(5, courseComponents.size())
            for (courseComponent in courseComponents) {
                assertDoesNotThrow { gson.fromJson(courseComponent, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(courseComponent.asJsonObject))
                val course: Course = gson.fromJson(courseComponent.asJsonObject.get("course"), Course::class.java)
                assert(course.category == "Coding" || course.category == "Business")
            }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm")) {
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courseComponents: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(7, courseComponents.size())
            for (courseComponent in courseComponents) {
                assertDoesNotThrow { gson.fromJson(courseComponent, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(courseComponent.asJsonObject))
                val course: Course = gson.fromJson(courseComponent.asJsonObject.get("course"), Course::class.java)
                assert(course.category == "Arts & Crafts" || course.category == "Photography/Film" || course.category == "UI/UX Design")
            }
        }
        println("debug!")
        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business,Cooking,Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm&page_size=16")) {
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courseComponents: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(16, courseComponents.size())
            for (courseComponent in courseComponents) {
                assertDoesNotThrow { gson.fromJson(courseComponent, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(courseComponent.asJsonObject))
            }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business,Cooking,Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm")) {
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courseComponents: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(10, courseComponents.size())
            for (courseComponent in courseComponents) {
                assertDoesNotThrow { gson.fromJson(courseComponent, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(courseComponent.asJsonObject))
            }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?categories=Coding,Business,Cooking,Arts%20%26%20Crafts,UI%2FUX%20Design,Photography%2FFilm&page=2")) {
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courseComponents: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(6, courseComponents.size())
            for (courseComponent in courseComponents) {
                assertDoesNotThrow { gson.fromJson(courseComponent, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(courseComponent.asJsonObject))
            }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?instructor_id=${testUsers[0].id}")) {
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courseComponents: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(10, courseComponents.size())
            for (courseComponent in courseComponents) {
                assertDoesNotThrow { gson.fromJson(courseComponent, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(courseComponent.asJsonObject))
                val instructor:JsonObject = gson.fromJson(courseComponent.asJsonObject.getAsJsonObject("instructor"), JsonObject::class.java)
                assertEquals(testUsers[0].id, instructor.get("id").asInt)
            }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}?instructor_id=${testUsers[0].id}&categories=Cooking,Coding")) {
            assertDoesNotThrow { gson.fromJson(response.content, JsonArray::class.java) }
            val courseComponents: JsonArray = gson.fromJson(response.content, JsonArray::class.java)
            assertEquals(5, courseComponents.size())
            for (courseComponent in courseComponents) {
                assertDoesNotThrow { gson.fromJson(courseComponent, JsonObject::class.java)}
                assertTrue(validateCourseComponentFormat(courseComponent.asJsonObject))
                val instructor:JsonObject = gson.fromJson(courseComponent.asJsonObject.getAsJsonObject("instructor"), JsonObject::class.java)
                assertEquals(testUsers[0].id, instructor.get("id").asInt)
                val course: Course = gson.fromJson(courseComponent.asJsonObject.get("course"), Course::class.java)
                assert(course.category == "Cooking" || course.category == "Coding")
            }
        }
    }

    @Test
    fun getCourseNotFound() = withTestApplication ({
        main(true)
        courses_module()
    }) {
        with(handleRequest(HttpMethod.Get, Routes.COURSES)) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
        }
        with(handleRequest(HttpMethod.Get, "${Routes.COURSES}/3")) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
        }
    }
}