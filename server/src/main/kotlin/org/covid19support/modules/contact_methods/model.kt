package org.covid19support.modules.contact_methods

import org.jetbrains.exposed.sql.*

data class ContactMethod(val name: String)

object ContactMethods : Table("contact_methods") {
    val name: Column<String> = varchar("name", 128)

    override val primaryKey = PrimaryKey(name, name = "PK_ContactMethods_Name")

    fun toContactMethod(resultRow: ResultRow): ContactMethod {
        return ContactMethod(resultRow[name])
    }
}