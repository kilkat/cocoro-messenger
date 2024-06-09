package com.example.cocoro_messenger

import com.example.cocoro_messenger.ApiResponse
import com.example.cocoro_messenger.UserCreate
import com.example.cocoro_messenger.UserLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/user/userCreate")
    suspend fun createUser(@Body userCreate: UserCreate): Response<ApiResponse>

    @POST("/user/userLogin")
    suspend fun loginUser(@Body userLogin: UserLogin): Response<ApiResponse>

    @POST("/user/userSearch")
    suspend fun searchUser(@Body userSearch: UserSearch): Response<UserSearchResponse>

    @POST("/user/addFriend")
    suspend fun addFriend(@Body addFriend: AddFriend): Response<AddFriendResponse>
}
