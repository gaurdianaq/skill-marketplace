package org.covid19support


import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.covid19support.modules.session.session_module
import org.covid19support.modules.users.User
import org.covid19support.modules.users.users_module
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class TestUsers : BaseTest() {
    //TODO Valid Format Returned (Description included when it exists and not included when it doesn't
    //TODO Register logs user in
    //TODO Add Users Text Too Long
    //TODO Invalid Email & Password (once validation is added)
    //TODO Edit User
    //TODO Edit User Unauthenticated
    //TODO Edit User Unauthorized
    //TODO Delete User
    //TODO Delete User Unauthenticated
    //TODO Delete User Unauthorized

    @Test
    fun addUsers() = withTestApplication({
        main(true)
        users_module()
    }) {
        val testUsers: Array<User> = arrayOf(
            User(null, "test@test.org", "test123", "Test1", "McTesterson", "The head of the McTesterson House, Mr. McTesterson rules with an iron fist!"),
            User(null, "test2@test.org", "test123", "Test2", "McTesterson", null),
            User(null, "test3@test.org", "test321", "Test3", "Not McTesterson", "The head of the Not McTesterson House, Mr. McTesterson rules with an iron fist!"),
            User(null, "test4@test.org", "test321", "Test4", "Not McTesterson", "The foot of the Not McTesterson House, Mr. McTesterson rules with an iron fist!"),
            User(null, "test5@test.org", "test1234", "Test5", "McTesterson", null)
        )
        for (user in testUsers) {
            with (handleRequest(HttpMethod.Post, Routes.USERS) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(user))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                user.id = gson.fromJson(response.content, User::class.java).id
            }
        }

        with(handleRequest(HttpMethod.Get, Routes.USERS)) {
            assertEquals(HttpStatusCode.OK, response.status())
            lateinit var users: Array<User>
            assertDoesNotThrow { users = gson.fromJson(response.content, Array<User>::class.java) }
            assertTrue { users.size == 5 }
        }

        for (user in testUsers) {
            with (handleRequest(HttpMethod.Get, "/users/${user.id}")) {
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                val responseUser: User = gson.fromJson(response.content, User::class.java)
                assertEquals(user.description, responseUser.description)
                assertEquals(user.email, responseUser.email)
                assertEquals(user.firstName, responseUser.firstName)
                assertEquals(user.lastName, responseUser.lastName)
                assertEquals(user.isInstructor, responseUser.isInstructor)
                assertEquals(user.role, responseUser.role)
                assertNull(responseUser.password)
            }
        }

    }

    @Test
    fun addUserLogsIn() = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val testUser = User(null, "test@test.org", "test123", "Test1", "McTesterson", "The head of the McTesterson House, Mr. McTesterson rules with an iron fist!")
        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(testUser))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertNotNull(sessions.get<SessionAuth>())
            }

            with(handleRequest(HttpMethod.Get, Routes.AUTHENTICATE)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow{gson.fromJson(response.content, User::class.java)}
            }
        }

    }

    @Test
    fun addUsersUniqueViolation() = withTestApplication ({
        main(true)
        users_module()
    }) {
        val testUser: User = User(null,"test@test.org", "test123", "Test1", "McTesterson", "The head of the McTesterson House, Mr. McTesterson rules with an iron fist!")
        val testUser2: User = User(null,"test@test.org", "test123", "Test1", "McTesterson", "The head of the McTesterson House, Mr. McTesterson rules with an iron fist!")
        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
        }

        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser2))
        }) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }

        with(handleRequest(HttpMethod.Get, Routes.USERS)) {
            assertDoesNotThrow { gson.fromJson(response.content, Array<User>::class.java) }
            val users: Array<User> = gson.fromJson(response.content, Array<User>::class.java)
            assertEquals(1, users.size)
        }
    }

    @Test
    fun addUserInvalidData() = withTestApplication ({
        main(true)
        users_module()
    }) {
        val badData: JsonObject = JsonObject()
        badData.addProperty("lame", "data")
        with(handleRequest (HttpMethod.Post, Routes.USERS){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(badData))
        }) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun getUserNotFound() = withTestApplication({
        main(true)
        users_module()
    }) {
        with(handleRequest(HttpMethod.Get, Routes.USERS)) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
        }
        with(handleRequest(HttpMethod.Get, "${Routes.USERS}/3")) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
        }
    }
}