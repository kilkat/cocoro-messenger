package com.example.cocoro_messenger.network

import com.example.cocoro_messenger.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/userCreate")
    suspend fun createUser(@Body user: UserCreate): Response<ApiResponse>

    @POST("/login")
    suspend fun loginUser(@Body user: UserLogin): Response<ApiResponse>
}
