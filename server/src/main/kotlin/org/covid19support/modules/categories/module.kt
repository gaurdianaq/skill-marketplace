package org.covid19support.modules.categories

import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.*
import org.covid19support.DbSettings
import org.covid19support.constants.Message
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.categories_module() {
    routing {
        route("/categories") {
            get {
                val categories: ArrayList<Category> = arrayListOf()
                transaction(DbSettings.db) {
                    val results:List<ResultRow> = Categories.selectAll().toList()
                    results.forEach {
                        categories.add(Categories.toCategory(it))
                    }
                }
                if (categories.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent, Message("No categories found!"))
                }
                else {
                    call.respond(HttpStatusCode.OK, categories)
                }
            }
        }
    }
}