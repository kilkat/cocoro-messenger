package com.example.cocoro_messenger

import com.example.cocoro_messenger.ApiResponse
import com.example.cocoro_messenger.UserCreate
import com.example.cocoro_messenger.UserLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/userCreate")
    suspend fun createUser(@Body userCreate: UserCreate): Response<ApiResponse>

    @POST("/userLogin")
    suspend fun loginUser(@Body userLogin: UserLogin): Response<ApiResponse>
}
