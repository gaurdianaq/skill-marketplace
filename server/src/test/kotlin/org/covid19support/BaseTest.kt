package org.covid19support

import com.google.gson.Gson

abstract class BaseTest {
    protected val gson: Gson = Gson()
}