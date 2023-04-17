package com.example.afinal
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// this api will check if heart rythem is normal or not
interface ApiService {
    @POST("/check")
    fun post(@Body requestBody:  RequestBody): Call<Response>
}

// basic object to call an api
val retrofit = Retrofit.Builder()
//    .baseUrl("https://d17f-174-114-232-152.ngrok-free.app")
    .baseUrl("http://ec2-54-227-201-82.compute-1.amazonaws.com/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)