package com.google.firebase.nongmsauth.api.service

import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenRequest
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface SecureTokenApi {

    @POST("v1/token")
    fun exchangeToken(@Body request: ExchangeTokenRequest): Call<ExchangeTokenResponse>

    companion object {
        const val BASE_URL = "https://securetoken.googleapis.com/"

        fun getInstance(client: OkHttpClient): SecureTokenApi {
            // Retrofit client pointed at the Firebase Auth API
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(SecureTokenApi::class.java)
        }
    }

}
