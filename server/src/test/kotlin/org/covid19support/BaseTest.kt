package org.covid19support

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.covid19support.modules.courses.CourseComponent
import org.covid19support.modules.courses.CourseComponentTypeAdapter

abstract class BaseTest {
    protected val gson: Gson
    private val gsonBuilder: GsonBuilder = GsonBuilder()

    init {
        gsonBuilder.registerTypeAdapter(CourseComponent::class.java, CourseComponentTypeAdapter())
        gson = gsonBuilder.create()
    }
}