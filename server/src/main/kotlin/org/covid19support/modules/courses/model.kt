package org.covid19support.modules.courses

import com.google.gson.*
import org.covid19support.modules.categories.Categories
import org.covid19support.modules.users.User
import org.covid19support.modules.users.Users
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import java.lang.reflect.Type

data class Course(var id: Int?,
                  val name: String,
                  val description: String,
                  val instructorId: Int,
                  val category: String,
                  val rate: Float)

data class CourseComponent(
        val instructor_id: Int,
        val instructor_name: String,
        val course_id: Int,
        val course_name: String,
        val course_description: String,
        val course_rating: Short?,
        val course_category: String,
        val course_rate: Float
)

class CourseComponentTypeAdapter : JsonSerializer<CourseComponent>, JsonDeserializer<CourseComponent> {
    override fun serialize(src: CourseComponent?, srcType: Type?, context: JsonSerializationContext?): JsonElement {
        val output = JsonObject()
        val instructor = JsonObject()
        val course = JsonObject()
        instructor.addProperty("id", src?.instructor_id)
        instructor.addProperty("name", src?.instructor_name)
        course.addProperty("id", src?.course_id)
        course.addProperty("name", src?.course_name)
        course.addProperty("description", src?.course_description)
        if (src?.course_rating != null) {
            course.addProperty("rating", src.course_rating)
        }
        course.addProperty("category", src?.course_category)
        course.addProperty("rate", src?.course_rate)
        output.add("instructor", instructor)
        output.add("course", course)
        return output
    }

    override fun deserialize(json: JsonElement?, courseComponentType: Type?, context: JsonDeserializationContext?): CourseComponent {
        val component = json!!.asJsonObject
        var instructor_id = -1
        var instructor_name = ""
        var course_id = -1
        var course_name = ""
        var course_description = ""
        var course_rating: Short? = null
        var course_category = ""
        var course_rate = -1f

        if (component.has("instructor") && component.has("course")) {
            if(component.get("instructor").isJsonObject && component.get("course").isJsonObject) {
                val instructor:JsonObject = component.get("instructor").asJsonObject
                val course:JsonObject = component.get("course").asJsonObject
                if (instructor.has("id") && instructor.has("name")) {
                    if (instructor.get("id").isJsonPrimitive && instructor.get("name").isJsonPrimitive) {
                        val instructor_id_element:JsonPrimitive = instructor.getAsJsonPrimitive("id")
                        val instructor_name_element:JsonPrimitive = instructor.getAsJsonPrimitive("name")
                        if (!instructor_id_element.isNumber || !instructor_name_element.isString) {
                            throw JsonParseException("Invalid format provided!")
                        }
                        instructor_id = instructor_id_element.asInt
                        instructor_name = instructor_name_element.asString
                    }
                    else {
                        throw JsonParseException("Invalid format provided!")
                    }
                }
                else {
                    throw JsonParseException("Invalid format provided!")
                }
                if (course.has("id") && course.has("name") && course.has("description")
                        && course.has("category") && course.has("rate")) {
                    if (course.get("id").isJsonPrimitive && course.get("name").isJsonPrimitive && course.get("description").isJsonPrimitive
                            && course.get("category").isJsonPrimitive && course.get("rate").isJsonPrimitive) {
                        val course_id_element: JsonPrimitive = course.getAsJsonPrimitive("id")
                        val course_name_element: JsonPrimitive = course.getAsJsonPrimitive("name")
                        val course_description_element: JsonPrimitive = course.getAsJsonPrimitive("description")
                        val course_category_element: JsonPrimitive = course.getAsJsonPrimitive("category")
                        val course_rate_element: JsonPrimitive = course.getAsJsonPrimitive("rate")
                        if (!course_id_element.isNumber || !course_name_element.isString || !course_description_element.isString || !course_category_element.isString || !course_rate_element.isNumber) {
                            throw JsonParseException("Invalid format provided!")
                        }
                        course_id = course_id_element.asInt
                        course_name = course_name_element.asString
                        course_description = course_description_element.asString
                        course_category = course_category_element.asString
                        course_rate = course_rate_element.asFloat
                        if (course.has("rating")) {
                            if (course.get("rating").isJsonPrimitive) {
                                if (course.get("rating").asJsonPrimitive.isNumber) {
                                    course_rating = course.get("rating").asJsonPrimitive.asShort
                                }
                                else {
                                    throw JsonParseException("Invalid format provided!")
                                }
                            }
                            else {
                                throw JsonParseException("Invalid format provided!")
                            }
                        }
                    }
                }
                else {
                    throw JsonParseException("Invalid format provided!")
                }
            }
            else {
                throw JsonParseException("Invalid format provided!")
            }
        }
        else {
            throw JsonParseException("Invalid format provided!")
        }
        return CourseComponent(instructor_id, instructor_name, course_id, course_name, course_description, course_rating, course_category, course_rate)
    }
}

object Courses : IntIdTable("courses") {
    val name: Column<String> = varchar("name", 64)
    val description: Column<String> = varchar("description", 1024)
    val instructor_id: Column<Int> = integer("instructor_id").references(Users.id)
    val category: Column<String> = varchar("category", 128).references(Categories.name)
    val rate: Column<Float> = float("rate")

    //Must only ever be called from within a transaction
    fun insertCourseAndGetId(course: Course) : Int {
        return insertAndGetId {
            it[name] = course.name
            it[description] = course.description
            it[instructor_id] = course.instructorId
            it[category] = course.category
            it[rate] = course.rate
        }.value
    }

    //Must only ever be called from within a transaction
    fun insertCourse(course: Course) {
        insert {
            it[name] = course.name
            it[description] = course.description
            it[instructor_id] = course.instructorId
            it[category] = course.category
            it[rate] = course.rate
        }
    }

    fun toCourse(resultRow: ResultRow): Course {
        return Course(resultRow[id].value, resultRow[name], resultRow[description], resultRow[instructor_id], resultRow[category], resultRow[rate])
    }
}