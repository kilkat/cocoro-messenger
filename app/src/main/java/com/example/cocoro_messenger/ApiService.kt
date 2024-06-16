package com.example.cocoro_messenger

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/user/userCreate")
    suspend fun createUser(@Body userCreate: UserCreate): Response<UserCreateResponse>

    @POST("/user/userLogin")
    suspend fun loginUser(@Body userLogin: UserLogin): Response<UserLoginResponse>

    @POST("/user/userSearch")
    suspend fun searchUser(@Body userSearch: UserSearch): Response<UserSearchResponse>

    @POST("/user/addFriend")
    suspend fun addFriend(@Body addFriend: AddFriend): Response<AddFriendResponse>
}
