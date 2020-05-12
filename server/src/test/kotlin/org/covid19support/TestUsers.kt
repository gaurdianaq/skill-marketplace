package org.covid19support


import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.covid19support.constants.Message
import org.covid19support.modules.session.Login
import org.covid19support.modules.session.session_module
import org.covid19support.modules.users.User
import org.covid19support.modules.users.Users
import org.covid19support.modules.users.users_module
import org.jetbrains.exposed.sql.transactions.transaction
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
    //TODO Delete User Does Not Exist


    @Test
    fun addUsers() : Unit = withTestApplication({
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
    fun addUserLogsIn() : Unit = withTestApplication({
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
    fun addUsersUniqueViolation() : Unit = withTestApplication ({
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
    fun addUserInvalidData() : Unit = withTestApplication ({
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
    fun getUserNotFound() : Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        with(handleRequest(HttpMethod.Get, Routes.USERS)) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }
        with(handleRequest(HttpMethod.Get, "${Routes.USERS}/3")) {
            assertEquals(HttpStatusCode.NoContent, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }
    }

    @Test
    fun getUserInvalidId() : Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        with(handleRequest(HttpMethod.Get, "${Routes.USERS}/asgas")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}/123.12")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }
    }

    @Test
    fun invalidPaginationValuesLessThan1() : Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        with(handleRequest(HttpMethod.Get, "${Routes.USERS}?page_size=0")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}?page_size=-1")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}?page=0")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}?page=-1")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}?page=-1&page_size=0")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}?page=-1&page_size=5")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}?page=2&page_size=-1")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }
    }

    @Test
    fun deleteSelf() : Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        val user = User(null, "user@user.org", "password", "User", "McUser", null)
        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(user))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                user.id = gson.fromJson(response.content, User::class.java).id
                assertNotNull(sessions.get<SessionAuth>())
            }
            with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${user.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                assertNull(sessions.get<SessionAuth>())
            }

            with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${user.id}")) {
                assertEquals(HttpStatusCode.NoContent, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

        }
    }

    @Test
    fun deleteUserUnauthenticated() : Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        val user = User(null, "user@user.org", "password", "User", "McUser", null)
        with(handleRequest(HttpMethod.Post, Routes.USERS){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(user))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            user.id = gson.fromJson(response.content, User::class.java).id
        }
        with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${user.id}")) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${user.id}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
        }
    }

    @Test
    fun deleteUserUnauthorized() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val user = User(null, "user@user.org", "password", "User", "McUser", null)
        val mod = User(null, "mod@mod.org", "password", "Mod", "McMod", null, role = "Moderator")

        transaction(DbSettings.db) {
            user.id = Users.insertUserAndGetId(user)
            mod.id = Users.insertUserAndGetId(mod)
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(user.email, user.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            }

            with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${mod.id}")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${mod.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            }
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(mod.email, mod.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            }

            with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${user.id}")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${user.id}")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            }
        }
    }

    @Test
    fun deleteUsersAsAdmin() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val admin = User(null, "admin@admin.org", "password", "Admin", "McBoss", null, role = "Admin")
        val users = arrayOf(
                User(null, "test1@test.org", "password", "Test", "Test", null),
                User(null, "test2@test.org", "password", "Test", "Test", null),
                User(null, "test3@test.org", "password", "Test", "Test", null)
        )
        transaction(DbSettings.db) {
            Users.insertUser(admin)
            for (user in users) {
                user.id = Users.insertUserAndGetId(user)
            }
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(admin.email, admin.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            }

            for (user in users) {
                with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${user.id}")) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                    assertNotNull(sessions.get<SessionAuth>())
                }
                with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${user.id}")) {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                }
            }
        }
    }

    /*
    @Test
    fun deleteUserDoesNotExist() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val admin = User(null, "admin@admin.org", "password", "Admin", "McBoss", null, role = "Admin")
        transaction(DbSettings.db) {
            admin.id = Users.insertUserAndGetId(admin)
        }
        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(admin.email, admin.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            }
            val badid = admin.id!! + 1
            with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${(badid)}")) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }
    }*/
}