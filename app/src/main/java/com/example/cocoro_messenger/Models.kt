package com.example.cocoro_messenger

data class UserCreate(
    val email: String,
    val name: String,
    val phone: String,
    val password: String,
    val confirmPassword: String
)

data class UserLogin(
    val email: String,
    val password: String
)

data class UserSearch(
    val email: String
)

data class AddFriend(
    val userEmail: String,
    val friendEmail: String
)

data class Friend(
    val name: String,
    val email: String
)