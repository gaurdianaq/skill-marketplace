package org.covid19support

object Routes {
    const val LOGIN: String = "/session/login"
    const val LOGOUT: String = "/session/logout"
    const val AUTHENTICATE: String = "/session/authenticate"
    const val COURSES: String = "/courses"
    const val USERS: String = "/users"
    const val RATINGS: String = "/ratings"
    const val COURSE_RATINGS: String = "$RATINGS/course"
}
