package org.covid19support.modules.contact_info

import org.covid19support.modules.contact_methods.ContactMethods
import org.covid19support.modules.users.Users
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

data class ContactInfo(val id: Int?,
                       val userId: Int,
                       val contactMethod: String,
                       val contactInfo: String)

object ContactInfoTable : IntIdTable("contact_info") {
    val user_id: Column<Int> = integer("user_id").references(Users.id)
    val contact_method: Column<String> = varchar("contact_method", 128).references(ContactMethods.name)
    val contact_info: Column<String> = varchar("contact_info", 256)

    //Must only ever be called from within a transaction
    fun insertContactInfoAndGetId(contactInfo: ContactInfo) : Int {
        return insertAndGetId {
            it[user_id] = contactInfo.userId
            it[contact_info] = contactInfo.contactInfo
            it[contact_method] = contactInfo.contactMethod
        }.value
    }

    //Must only ever be called from within a transaction
    fun insertContactInfo(contactInfo: ContactInfo) {
        insert {
            it[user_id] = contactInfo.userId
            it[contact_info] = contactInfo.contactInfo
            it[contact_method] = contactInfo.contactMethod
        }
    }

    fun toContactInfo(resultRow: ResultRow): ContactInfo {
        return ContactInfo(resultRow[id].value, resultRow[user_id], resultRow[contact_method], resultRow[contact_info])
    }
}