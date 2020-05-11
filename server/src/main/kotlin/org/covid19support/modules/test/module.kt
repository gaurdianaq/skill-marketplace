package org.covid19support.modules.test

//This file contains routes that are to only be used in tests, they are never to be included in the actual application

import io.ktor.application.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.covid19support.SessionAuth
import org.covid19support.constants.Message

fun Application.test_module() {
    routing {
        route("/badtoken") {
            get {
                call.sessions.set(SessionAuth("thisisarealbadtoken"))
                call.respond(HttpStatusCode.OK, Message("Created a bad token!"))
            }
        }
    }
}