package com.example.cocoro_messenger

data class ApiResponse(
    val message: String,
    val token: String? = null,
    val name: String? = null
)
