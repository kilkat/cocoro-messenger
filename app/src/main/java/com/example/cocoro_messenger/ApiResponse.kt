package com.example.cocoro_messenger

data class UserCreateResponse(
    val message: String,
    val token: String? = null,
    val name: String? = null
)

data class UserLoginResponse(
    val message: String,
    val token: String,
    val email: String,
    val name: String,
    val friends: List<Friend>
)

data class UserSearchResponse(
    val message: String,
    val email: String,
    val name: String
)

data class AddFriendResponse(
    val message: String,
)