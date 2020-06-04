package org.covid19support.authentication

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.sessions.*
import org.covid19support.SessionAuth
import org.covid19support.constants.FORBIDDEN
import org.covid19support.constants.Message
import org.covid19support.constants.SHAME
import org.covid19support.constants.UNAUTHORIZED

open class Authenticator(val call: ApplicationCall) {
    //Checks for session token, checks if it's valid, and responds appropriately if it's not
    //Returns token if valid, and null if not.
    protected var decodedToken: DecodedJWT? = null

    fun getID() : Int? {
        if (decodedToken == null) {
            return null
        }
        return decodedToken?.claims!![Token.ID_CLAIM]?.asInt()
    }

    fun getEmail() : String? {
        if (decodedToken == null) {
            return null
        }
        return decodedToken?.claims!![Token.EMAIL_CLAIM].toString()
    }

    fun getRole() : Role? {
        if (decodedToken == null) {
            return null
        }
        return when(decodedToken?.claims!![Token.ROLE_CLAIM]?.asString()) {
            Role.NORMAL.value -> Role.NORMAL
            Role.MODERATOR.value -> Role.MODERATOR
            Role.ADMIN.value -> Role.ADMIN
            else -> null
        }
    }

    suspend fun authenticate(): Boolean {
        val token: String? = call.sessions.get<SessionAuth>()?.token
        if (token != null) {
            decodedToken = Token.verify(token)
            if (decodedToken == null) {
                call.respond(HttpStatusCode.Unauthorized, Message(SHAME))
                call.sessions.clear<SessionAuth>()
                return false
            }
        }
        else {
            call.respond(HttpStatusCode.Unauthorized, Message(UNAUTHORIZED))
            return false
        }
        return true
    }
}
