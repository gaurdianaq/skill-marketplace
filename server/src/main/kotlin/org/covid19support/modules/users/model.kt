package org.covid19support.modules.users

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.covid19support.modules.roles.Roles
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.mindrot.jbcrypt.BCrypt
import java.lang.reflect.Type

data class User(
        var id: Int?,
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val description: String?,
        val isInstructor: Boolean = false,
        val role: String = "Normal"
)

class UserSerializer : JsonSerializer<User> {
    override fun serialize(src: User?, srcType: Type?, context: JsonSerializationContext?): JsonElement {
        val output: JsonObject = JsonObject()
        output.addProperty("id", src?.id)
        output.addProperty("email", src?.email)
        output.addProperty("firstName", src?.firstName)
        output.addProperty("lastName", src?.lastName)
        output.addProperty("description", src?.description)
        output.addProperty("isInstructor", src?.isInstructor)
        output.addProperty("role", src?.role)
        return output
    }

}

object Users : IntIdTable("users") {
    val email: Column<String> = varchar("email", 256).uniqueIndex()
    val password: Column<String> = varchar("password", 64)
    val first_name: Column<String> = varchar("first_name", 32)
    val last_name: Column<String> = varchar("last_name", 32)
    val description: Column<String?> = varchar("description", 1024).nullable()
    val is_instructor: Column<Boolean> = bool("is_instructor").default(false)
    val role: Column<String> = varchar("role", 128).references(Roles.name).default("Normal")

    //Must only ever be called from within a transaction
    fun insertUserAndGetId(user: User) : Int {
        val passhash = BCrypt.hashpw(user.password, BCrypt.gensalt())
        return Users.insertAndGetId {
            it[email] = user.email
            it[password] = passhash
            it[first_name] = user.firstName
            it[last_name] = user.lastName
            it[description] = user.description
            it[is_instructor] = user.isInstructor
            it[role] = user.role
        }.value
    }

    //Must only ever be called from within a transaction
    fun insertUser(user: User) {
        val passhash = BCrypt.hashpw(user.password, BCrypt.gensalt())
        Users.insert {
            it[email] = user.email
            it[password] = passhash
            it[first_name] = user.firstName
            it[last_name] = user.lastName
            it[description] = user.description
            it[is_instructor] = user.isInstructor
            it[role] = user.role
        }
    }

    fun toUser(resultRow: ResultRow): User {
        return User(resultRow[id].value, resultRow[email], resultRow[password], resultRow[first_name], resultRow[last_name], resultRow[description], resultRow[is_instructor], resultRow[role])
    }
}

