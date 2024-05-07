package com.aurelioklv.dicodingstoryapp.data.remote.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): BasicResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): LoginResponse

    @GET("stories")
    suspend fun getAllStories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
    ): StoriesResponse

    @GET("stories/{id}")
    suspend fun getDetails(
        @Path("id") storyId: String,
    ): DetailsResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): BasicResponse
}
