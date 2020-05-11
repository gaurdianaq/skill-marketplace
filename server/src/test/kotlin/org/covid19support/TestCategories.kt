package org.covid19support

import com.google.gson.Gson
import com.google.gson.JsonArray
import io.ktor.http.*
import io.ktor.server.testing.*
import org.covid19support.modules.categories.Category
import org.covid19support.modules.categories.categories_module
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestCategories : BaseTest() {
    @Test
    fun testCategories() = withTestApplication({
        main(true)
        categories_module()
    }) {
        with(handleRequest(HttpMethod.Get, "/categories")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertDoesNotThrow { gson.fromJson(response.content, Array<Category>::class.java) }
            val categories: Array<Category> = gson.fromJson(response.content, Array<Category>::class.java)
            assertTrue { categories.isNotEmpty() }
        }
    }
}

