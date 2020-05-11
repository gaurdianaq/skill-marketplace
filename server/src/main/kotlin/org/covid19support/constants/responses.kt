package org.covid19support.constants

const val SHAME = "Sending a fake token... Shame on you!"
const val FORBIDDEN = "You're not authorized to access this resource."
const val AUTHORIZED = "You're authorized!"
const val UNAUTHORIZED = "You need to login!"
const val INVALID_BODY = "Invalid body provided!"
const val INTERNAL_ERROR = "Oh no... Something went wrong!"

data class Message(val message: String)