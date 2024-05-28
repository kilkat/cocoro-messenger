package com.example.cocoro_messenger.models

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

data class ApiResponse(
    val message: String
)
