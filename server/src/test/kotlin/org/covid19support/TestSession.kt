package org.covid19support

import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import kotlin.test.*import io.ktor.server.testing.withTestApplication
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.covid19support.modules.session.Login
import org.covid19support.modules.session.session_module
import org.covid19support.modules.test.test_module
import org.covid19support.modules.users.User
import org.covid19support.modules.users.users_module

class TestSession : BaseTest() {
    //TODO Login Fail (Text too Long) - Not Implemented
    //TODO Login Fail (Already logged in) - Not Implemented
    //TODO Authenticate Fail (Valid Token, User no longer exists in database) - Delete Not Implemented

    @Test
    fun loginSuccess() = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        val testUser = User(null, "test@test.org", "testingmypassword", "Test", "McTesterson", "The head of the McTesterson household!")
        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
        }

        cookiesSession {
            val login: Login = Login(testUser.email, testUser.password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(sessions.get<SessionAuth>())
                assertDoesNotThrow{gson.fromJson(response.content, User::class.java)}
                val returnedUser = gson.fromJson(response.content, User::class.java)
                assertEquals(testUser.email, returnedUser.email)
                assertEquals(testUser.role, returnedUser.role)
                assertEquals(testUser.firstName, returnedUser.firstName)
                assertEquals(testUser.lastName, returnedUser.lastName)
                assertEquals(testUser.isInstructor, returnedUser.isInstructor)
                assertEquals(testUser.description, returnedUser.description)
            }
        }
    }

    @Test
    fun loginFailWrongPassword() = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        val testUser = User(null, "test@test.org", "testingmypassword", "Test", "McTesterson", "The head of the McTesterson household!")
        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
        }

        cookiesSession {
            val login: Login = Login("test@test.org", "notthecorrectpassword")
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertNull(sessions.get<SessionAuth>())
                assertDoesNotThrow{gson.fromJson(response.content, JsonObject::class.java)}
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
            }
        }
    }

    @Test
    fun loginFailUserDoesNotExist() = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        cookiesSession {
            val login: Login = Login("notreal@test.org", "imnotreal")
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertNull(sessions.get<SessionAuth>())
                assertDoesNotThrow{gson.fromJson(response.content, JsonObject::class.java)}
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
            }
        }
    }

    @Test
    fun logoutSuccess() = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        val testUsers = arrayOf(
                User(null, "test@test.org", "testingmypassword", "Test", "McTesterson", "The head of the McTesterson household!"),
                User(null,"testbrother@test.org", "notthesamepassword", "Brother", "McTesterson", "The jealous brother of Test, plans to take hold of the McTesterson household by force!")
                )
        for (testUser in testUsers) {
            with(handleRequest(HttpMethod.Post, Routes.USERS) {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(testUser))
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
            }
        }


        cookiesSession {
            val logins: Array<Login> = arrayOf(Login(testUsers[0].email, testUsers[0].password), Login(testUsers[1].email, testUsers[1].password))

            for (login in logins) {
                with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(gson.toJson(login))
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertNotNull(sessions.get<SessionAuth>())
                    assertDoesNotThrow{gson.fromJson(response.content, User::class.java)}
                }
                with(handleRequest(HttpMethod.Post, Routes.LOGOUT)) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertNull(sessions.get<SessionAuth>())
                    assertDoesNotThrow{gson.fromJson(response.content, JsonObject::class.java)}
                    assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
                }
            }

        }
    }

    @Test
    fun logoutFailedAlreadyLoggedOut() = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        val testUser = User(null, "test@test.org", "testingmypassword", "Test", "McTesterson", "The head of the McTesterson household!")

        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
        }

        cookiesSession {
            val login = Login(testUser.email, testUser.password)

            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(sessions.get<SessionAuth>())
                assertDoesNotThrow{gson.fromJson(response.content, User::class.java)}
            }
            with(handleRequest(HttpMethod.Post, Routes.LOGOUT)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNull(sessions.get<SessionAuth>())
                assertDoesNotThrow{gson.fromJson(response.content, JsonObject::class.java)}
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
            }

            with(handleRequest(HttpMethod.Post, Routes.LOGOUT)) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertNull(sessions.get<SessionAuth>())
                assertDoesNotThrow{gson.fromJson(response.content, JsonObject::class.java)}
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
            }

        }
    }

    @Test
    fun authenticateSuccess() = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        val testUser = User(null, "test@test.org", "testingmypassword", "Test", "McTesterson", "The head of the McTesterson household!")
        with(handleRequest(HttpMethod.Post, Routes.USERS) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(testUser))
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
        }

        cookiesSession {
            val login: Login = Login(testUser.email, testUser.password)
            with(handleRequest(HttpMethod.Post, Routes.LOGIN){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(gson.toJson(login))
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(sessions.get<SessionAuth>())
            }

            with(handleRequest(HttpMethod.Get, Routes.AUTHENTICATE)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertDoesNotThrow{gson.fromJson(response.content, User::class.java)}
                val returnedUser = gson.fromJson(response.content, User::class.java)
                assertEquals(testUser.email, returnedUser.email)
                assertEquals(testUser.role, returnedUser.role)
                assertEquals(testUser.firstName, returnedUser.firstName)
                assertEquals(testUser.lastName, returnedUser.lastName)
                assertEquals(testUser.isInstructor, returnedUser.isInstructor)
                assertEquals(testUser.description, returnedUser.description)
            }
        }
    }

    @Test
    fun authenticateFailureNotLoggedIn() = withTestApplication ({
        main(true)
        users_module()
        session_module()
    }) {
        cookiesSession {
            with(handleRequest(HttpMethod.Get, Routes.AUTHENTICATE)) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertNull(sessions.get<SessionAuth>())
                assertDoesNotThrow{gson.fromJson(response.content, JsonObject::class.java)}
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
            }
        }
    }

    @Test
    fun authenticateFailureInvalidToken() = withTestApplication ({
        main(true)
        users_module()
        session_module()
        test_module()
    }) {
        cookiesSession {
            with(handleRequest(HttpMethod.Get, "/badtoken")) {
                //a few quick assertions to make sure my dummy route is also working
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(sessions.get<SessionAuth>())
            }
            with(handleRequest(HttpMethod.Get, Routes.AUTHENTICATE)) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertNull(sessions.get<SessionAuth>())//session should be null after it finds the bad token since it will clear the session
                assertDoesNotThrow{gson.fromJson(response.content, JsonObject::class.java)}
                assertTrue(validateMessageFormat(gson.fromJson(response.content, JsonObject::class.java)))
            }
        }
    }
}