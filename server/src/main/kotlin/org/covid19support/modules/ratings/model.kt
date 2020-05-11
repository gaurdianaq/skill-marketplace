package org.covid19support.modules.ratings

import org.covid19support.modules.courses.Courses
import org.covid19support.modules.users.Users
import org.jetbrains.exposed.sql.*

data class Rating(val userId: Int,
                  val courseId: Int,
                  val ratingValue: Short,
                  val comment: String)

data class RatingComponent(val userId: Int,
                           val courseId: Int,
                           val ratingValue: Short,
                           val comment: String,
                           val firstName: String,
                           val lastName: String)

object Ratings : Table("ratings") {
    val user_id: Column<Int> = integer("user_id").references(Users.id)
    val course_id: Column<Int> = integer("course_id").references(Courses.id)
    val rating_value: Column<Short> = short("rating")
    val comment: Column<String> = varchar("comment", 512)
    override val primaryKey = PrimaryKey(user_id, course_id, name = "PK_Ratings_Id")

    //Must only ever be called from within a transaction
    fun insertRating(rating: Rating) {
        insert {
            it[user_id] = rating.userId
            it[course_id] = rating.courseId
            it[rating_value] = rating.ratingValue
            it[comment] = rating.comment
        }
    }

    fun toRating(resultRow: ResultRow): Rating {
        return Rating(resultRow[user_id], resultRow[course_id], resultRow[rating_value], resultRow[comment])
    }
}