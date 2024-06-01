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
