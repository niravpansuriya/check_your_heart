package com.example.afinal
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @POST("/check")
    fun post(@Body requestBody:  RequestBody): Call<Response>
}

val retrofit = Retrofit.Builder()
//    .baseUrl("https://5e5e-174-114-232-152.ngrok-free.app/")
    .baseUrl("http://ec2-54-227-201-82.compute-1.amazonaws.com/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)