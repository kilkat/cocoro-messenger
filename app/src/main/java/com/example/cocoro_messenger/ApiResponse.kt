package com.example.cocoro_messenger

data class ApiResponse(
    val message: String,
    val token: String? = null,
    val name: String? = null
)

data class UserSearchResponse(
    val message: String,
    val email: String,
    val name: String
)

data class AddFriendResponse(
    val message: String,
)