package org.covid19support

import org.covid19support.modules.contact_info.ContactInfoTable
import org.covid19support.modules.courses.Courses
import org.covid19support.modules.ratings.Ratings
import org.covid19support.modules.user_courses.UserCourses
import org.covid19support.modules.users.Users
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager

//will add more to this as I run across more
enum class SQLState(val code: String) {
    UNIQUE_CONSTRAINT_VIOLATION("23505"),
    FOREIGN_KEY_VIOLATION("23503"),
    CHECK_VIOLATION("23514")

}

object DbSettings {
    private lateinit var db_url: String
    private lateinit var db_user: String
    private lateinit var db_password: String

    fun init(isTesting: Boolean) {
        if (isTesting) {
            db_url = "jdbc:postgresql://"+ dotenv["TEST_DB_HOST"]+"/"+ dotenv["TEST_DB_NAME"]
            db_user = dotenv["TEST_DB_USER"]!!
            db_password = dotenv["TEST_DB_PASSWORD"]!!
        }
        else {
            db_url = "jdbc:postgresql://"+ dotenv["DB_HOST"]+"/"+ dotenv["DB_NAME"]
            db_user = dotenv["DB_USER"]!!
            db_password = dotenv["DB_PASSWORD"]!!
        }
    }

    val db by lazy {
       Database.connect(db_url, driver = "org.postgresql.Driver",
               user = db_user, password = db_password)
    }
}

fun clearDataBase() {
    transaction(DbSettings.db) {
        Users.deleteAll()
        ContactInfoTable.deleteAll()
        Courses.deleteAll()
        Ratings.deleteAll()
        UserCourses.deleteAll()
    }
}