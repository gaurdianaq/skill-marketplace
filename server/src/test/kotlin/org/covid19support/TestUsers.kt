package org.covid19support


import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.covid19support.authentication.Role
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
    //TODO Add Users Text Too Long
    //TODO Invalid Email & Password (once validation is added)
    //TODO Test Edit User id should not change anything (Should this throw a bad request or simply be ignored? Probably throw a bad request)


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
    fun addPrivilegedUserFailure() : Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        val user = User(null, "user@user.net", "password", "User", "User", null)
        val moderator = User(null, "mod@mod.net", "password", "Moderator", "Moderator", null, role = Role.MODERATOR.value)
        val admin = User(null, "admin@admin.net", "password", "Administrator", "Administrator", null, role = Role.ADMIN.value)

        with(handleRequest(HttpMethod.Post, Routes.USERS){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(moderator))
        }) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Post, Routes.USERS){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(admin))
        }) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(user))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }

            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(moderator))
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(admin))
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }
    }

    @Test
    fun addPrivilegedUserSuccess() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val admin = User(null, "user@user.net", "password", "User", "User", null, role = Role.ADMIN.value)
        val moderator = User(null, "mod@mod.net", "password", "Moderator", "Moderator", null, role = Role.MODERATOR.value)
        val admin2 = User(null, "admin@admin.net", "password", "Administrator", "Administrator", null, role = Role.ADMIN.value)

        transaction(DbSettings.db) {
            Users.insertUser(admin)
        }

        cookiesSession {
            lateinit var token: String
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(admin.email, admin.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
                token = sessions.get<SessionAuth>()?.token!!
            }

            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(moderator))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertEquals(token, sessions.get<SessionAuth>()?.token!!) //ensure that it's not changing my session token
            }

            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(admin2))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertEquals(token, sessions.get<SessionAuth>()?.token!!) //ensure that it's not changing my session token
            }
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
        val mod = User(null, "mod@mod.org", "password", "Mod", "McMod", null, role = Role.MODERATOR.value)

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
        val admin = User(null, "admin@admin.org", "password", "Admin", "McBoss", null, role = Role.ADMIN.value)
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

    @Test
    fun deleteUserDoesNotExist() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val admin = User(null, "admin@admin.org", "password", "Admin", "McBoss", null, role = Role.ADMIN.value)
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
            with(handleRequest(HttpMethod.Delete, "${Routes.USERS}/${(admin.id!!+1)}")) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }
    }

    @Test
    fun editSelf() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val user = User(null, "struggling@life.com", "gottakeeppushing", "Frustrated", "JobSeeker", null)
        var editUserMessage = JsonObject()
        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.USERS) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(user))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                user.id = gson.fromJson(response.content, User::class.java).id
                assertNotNull(sessions.get<SessionAuth>())
            }
            editUserMessage.addProperty("firstName", "NotFrustrated")
            editUserMessage.addProperty("lastName", "Cheeseman")

            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user.id}") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editUserMessage))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${user.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                val editedUser = gson.fromJson(response.content, User::class.java)
                assertEquals("NotFrustrated", editedUser.firstName)
                assertEquals("Cheeseman", editedUser.lastName)
                assertEquals(user.email, editedUser.email)
                assertEquals(user.description, editedUser.description)
                assertEquals(user.isInstructor, editedUser.isInstructor)
                assertEquals(user.role, editedUser.role)
            }

            editUserMessage = JsonObject()
            editUserMessage.addProperty("email", "cheeseman@cheesey.org")
            editUserMessage.addProperty("password", "spiceisright")

            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editUserMessage))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(user.email, user.password)))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                assertNull(sessions.get<SessionAuth>())
            }

            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login("cheeseman@cheesey.org", "spiceisright")))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
                val editedUser = gson.fromJson(response.content, User::class.java)
                assertEquals("NotFrustrated", editedUser.firstName)
                assertEquals("Cheeseman", editedUser.lastName)
                assertEquals(user.description, editedUser.description)
                assertEquals(user.isInstructor, editedUser.isInstructor)
                assertEquals(user.role, editedUser.role)
            }

            editUserMessage = JsonObject()
            editUserMessage.addProperty("firstName", "Goulda")
            editUserMessage.addProperty("description", "The cheesiest man you'll ever meet, you've been warned...")
            editUserMessage.addProperty("isInstructor", true)

            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editUserMessage))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }

            with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${user.id}")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                val editedUser = gson.fromJson(response.content, User::class.java)
                assertEquals("cheeseman@cheesey.org", editedUser.email)
                assertEquals("Goulda", editedUser.firstName)
                assertEquals("Cheeseman", editedUser.lastName)
                assertTrue(editedUser.isInstructor!!)
                assertEquals("The cheesiest man you'll ever meet, you've been warned...", editedUser.description)
                assertEquals(user.role, editedUser.role)
            }
        }
    }

    @Test
    fun editUserUnauthenticated() : Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        val user = User(null, "lamb@korma.ca", "notspicy", "Lamb", "Korma", "Tasty food!")
        with(handleRequest(HttpMethod.Post, Routes.USERS){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(user))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            user.id = gson.fromJson(response.content, User::class.java).id
        }
        val editUserMessage = JsonObject()
        editUserMessage.addProperty("firstName", "Chicken")
        editUserMessage.addProperty("lastName", "Vindaloo")
        with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user.id}"){
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(editUserMessage))
        }) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
        }

        with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${user.id}")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            val retrievedUser = gson.fromJson(response.content, User::class.java)
            assertEquals(user.email, retrievedUser.email)
            assertEquals(user.firstName, retrievedUser.firstName)
            assertEquals(user.lastName, retrievedUser.lastName)
            assertEquals(user.isInstructor, retrievedUser.isInstructor)
            assertEquals(user.role, retrievedUser.role)
            assertEquals(user.description, retrievedUser.description)
        }
    }

    @Test
    fun editUserUnauthorized() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val users = arrayOf(
                User(null, "user1@users.org", "password", "User1", "McUser", null, role = Role.MODERATOR.value),
                User(null, "user2@users.org", "password", "User2", "McUser", null),
                User(null, "user3@users.org", "password", "User3", "McUser", null),
                User(null, "user4@users.org", "password", "User4", "McUser", null),
                User(null, "user5@users.org", "password", "User5", "McUser", null)
        )

        transaction(DbSettings.db) {
            for (user in users) {
                user.id = Users.insertUserAndGetId(user)
            }
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(users[0].email, users[1].password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
            }
            val editUserMessage = JsonObject()
            editUserMessage.addProperty("firstName", "Dork!")
            for (i in 1 .. 4) {
                with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${users[i].id}"){
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(editUserMessage))
                }) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                }
                with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${users[i].id}")) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                    val retrievedUser = gson.fromJson(response.content, User::class.java)
                    assertEquals(users[i].email, retrievedUser.email)
                    assertEquals(users[i].firstName, retrievedUser.firstName)
                    assertEquals(users[i].lastName, retrievedUser.lastName)
                    assertEquals(users[i].isInstructor, retrievedUser.isInstructor)
                    assertEquals(users[i].role, retrievedUser.role)
                    assertEquals(users[i].description, retrievedUser.description)
                }
            }
        }
    }

    @Test
    fun editUsersAsAdmin() : Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val admin = User(null, "admin@admin.org", "password", "Admin", "McBoss", null, role = Role.ADMIN.value)
        val users = arrayOf(
                User(null, "test1@test.org", "password", "Test", "Test", null),
                User(null, "test2@test.org", "password", "Test", "Test", null),
                User(null, "test3@test.org", "password", "Test", "Test", null)
        )
        val editMessages = arrayOf(
                JsonObject(), JsonObject(), JsonObject()
        )

        editMessages[0].addProperty("email", "cheddarman@test.org")
        editMessages[1].addProperty("email", "gouldaman@test.org")
        editMessages[2].addProperty("email", "havartiman@test.org")
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


            for (i in 0 .. 2) {
                with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${users[i].id}"){
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(editMessages[i]))
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
                }
                with(handleRequest(HttpMethod.Get, "${Routes.USERS}/${users[i].id}")) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                    val fetchedUser = gson.fromJson(response.content, User::class.java)
                    assertEquals(editMessages[i]["email"].asString, fetchedUser.email)
                }
            }
        }
    }

    @Test
    fun editUsersBadData(): Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        val user = User(null, "cheeseman@cheesey.ca", "cheddahgouldahavartibleu", "Goulda", "Cheddar", null)
        val editMessage = JsonObject()
        editMessage.addProperty("notavalidfield", "somedata")
        editMessage.addProperty("anotherinavlidfield", 5)
        val subObject = JsonObject()
        subObject.addProperty("firstName", "cheese")
        editMessage.add("subobject", subObject)
        val firstNameSubObject = JsonObject()
        firstNameSubObject.addProperty("cheese", "goulda")
        editMessage.add("firstName", firstNameSubObject)
        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.USERS){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(user))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
                user.id = gson.fromJson(response.content, User::class.java).id
            }
            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editMessage))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java)}
            }
        }
    }

    @Test
    fun editUserNoValidFields(): Unit = withTestApplication({
        main(true)
        users_module()
    }) {
        val user = User(null, "user@user.com", "password", "User", "McUserson", null)
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
            val badData = JsonObject()
            badData.addProperty("notvalid", "doesntmatter")
            badData.addProperty("notvalidnumber", 5)
            badData.addProperty("notvalidbool", false)
            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user.id}"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(badData))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }
    }

    @Test
    fun editOwnRole(): Unit = withTestApplication({
        main(true)
        users_module()
        session_module()
    }) {
        val user = User(null, "user@user.org", "password", "User", "User", null, role = "Moderator")
        val user2 = User(null, "user2@user2.org", "password", "User", "User", null, role = "Normal")
        transaction(DbSettings.db) {
            user.id = Users.insertUserAndGetId(user)
            user2.id = Users.insertUserAndGetId(user2)
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(user.email, user.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }
            val editUser1 = JsonObject()
            editUser1.addProperty("role", Role.ADMIN.value)
            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user.id}") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editUser1))
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }

        cookiesSession {
            with(handleRequest(HttpMethod.Post, Routes.LOGIN) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(Login(user2.email, user2.password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, User::class.java) }
                assertNotNull(sessions.get<SessionAuth>())
            }
            val editUser1 = JsonObject()
            editUser1.addProperty("role", Role.MODERATOR.value)
            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/${user2.id}") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(editUser1))
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }

    }

    @Test
    fun editUserDoesNotExist() : Unit = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        val admin = User(null, "admin@boss.com", "password", "Admin", "Bossface", null, false, Role.ADMIN.value)

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
                assertNotNull(sessions.get<SessionAuth>())
            }
            val editData = JsonObject()
            editData.addProperty("email", "cheeseman@dork.com")
            editData.addProperty("lastName", "Goulda")

            with(handleRequest(HttpMethod.Patch, "${Routes.USERS}/3"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(gson.toJson(editData)))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertDoesNotThrow { gson.fromJson(response.content, Message::class.java) }
            }
        }
    }
}