package org.covid19support.modules.roles

import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.*
import org.covid19support.DbSettings
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.covid19support.constants.Message

fun Application.roles_module() {
    routing {
        get("/roles") {
            val roles: ArrayList<Role> = arrayListOf<Role>()
            transaction(DbSettings.db) {
                val results: List<ResultRow> = Roles.selectAll().toList()
                results.forEach {
                    roles.add(Roles.toRole(it))
                }
            }
            if (roles.isEmpty()) {
                call.respond(HttpStatusCode.NoContent, Message("No roles were found!"))
            }
            else {
                call.respond(roles)
            }
        }
    }
}