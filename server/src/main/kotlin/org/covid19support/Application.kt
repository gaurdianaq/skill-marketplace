package org.covid19support

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.application.*
import io.ktor.config.MapApplicationConfig
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.sessions.*
import org.covid19support.modules.courses.CourseComponent
import org.covid19support.modules.courses.CourseComponentSerializer
import org.covid19support.modules.users.User
import org.covid19support.modules.users.UserSerializer



fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.main(isTesting: Boolean = false) {
    dotenv = Dotenv.load()
    DbSettings.init(isTesting)
    if(isTesting) {
        clearDataBase()
    }
    install(DefaultHeaders)
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(User::class.java, UserSerializer())
            registerTypeAdapter(CourseComponent::class.java, CourseComponentSerializer())
        }
    }
    install(Sessions) {
        cookie<SessionAuth>("TOKEN") {
            cookie.httpOnly = true
        }
    }
}