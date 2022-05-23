package com.ioh_c22_h2_4.hy_ponics.data.remote

import com.google.gson.annotations.SerializedName
import com.ioh_c22_h2_4.hy_ponics.data.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun signup(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") pass: String
    ): Call<FileUploadResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") pass: String
    ): Call<LoginResponse>

}

data class FileUploadResponse(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String
)

