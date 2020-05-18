package org.covid19support

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.covid19support.modules.courses.CourseComponent
import org.covid19support.modules.courses.CourseComponentTypeAdapter
import org.covid19support.modules.users.User
import org.covid19support.modules.users.UserSerializer

abstract class BaseTest {
    protected val gson: Gson
    private val gsonBuilder: GsonBuilder = GsonBuilder()

    init {
        gsonBuilder.registerTypeAdapter(User::class.java, UserSerializer())
        gsonBuilder.registerTypeAdapter(CourseComponent::class.java, CourseComponentTypeAdapter())
        gson = gsonBuilder.create()
    }
}