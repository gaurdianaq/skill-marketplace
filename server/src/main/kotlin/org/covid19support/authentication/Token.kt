package org.covid19support.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.covid19support.dotenv

object Token {
    private const val ISSUER:String = "covid19support"
    private val algorithm: Algorithm = Algorithm.HMAC512(dotenv["SECRET_KEY"])
    private val verifier: JWTVerifier = JWT.require(algorithm)
            .withIssuer(ISSUER)
            .build()
    fun create(id:Int, email:String): String {
        return JWT.create()
                .withIssuer(ISSUER)
                .withClaim("id", id)
                .withClaim("email", email)
                .sign(algorithm)
    }

    fun verify(token:String):DecodedJWT? {
        return try {
            verifier.verify(token)
        }
        catch (exception:JWTVerificationException)
        {
            null
        }
    }

}