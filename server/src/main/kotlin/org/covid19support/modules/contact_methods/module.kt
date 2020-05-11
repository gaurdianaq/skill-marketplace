package org.covid19support.modules.contact_methods

import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.*
import org.covid19support.DbSettings
import org.covid19support.constants.Message
import org.covid19support.modules.contact_info.ContactInfo
import org.covid19support.modules.contact_info.ContactInfoTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.contactMethods_module() {
    routing {
        route("/contact_methods") {
            get {
                val contactMethods: ArrayList<ContactMethod> = arrayListOf()
                transaction(DbSettings.db) {
                    val results:List<ResultRow> = ContactMethods.selectAll().toList()
                    results.forEach {
                        contactMethods.add(ContactMethods.toContactMethod(it))
                    }
                }
                if (contactMethods.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent, Message("No contact methods found!"))
                }
                else {
                    call.respond(contactMethods)
                }
            }
        }
    }
}