package org.covid19support.modules.roles

import org.jetbrains.exposed.sql.*

data class Role(val name: String)

object Roles : Table("roles") {
    val name: Column<String> = varchar("name", 128)
    override val primaryKey = PrimaryKey(name, name = "PK_Roles_Name")

    fun toRole(resultRow:ResultRow): Role {
        return Role(resultRow[name])
    }
}