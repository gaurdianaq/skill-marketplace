package org.covid19support

import com.google.gson.Gson
import com.google.gson.JsonObject

abstract class BaseTest {
    protected val gson: Gson = Gson()

    protected fun validateMessageFormat(jsonObject: JsonObject): Boolean {
        if (jsonObject.has("message")) {
            if (jsonObject.get("message").isJsonPrimitive) {
                if (!jsonObject.getAsJsonPrimitive("message").isString) {
                    return false
                }
            }
            else {
                return false
            }
        }
        else {
            return false
        }
        return true
    }
}