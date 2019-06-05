package com.google.firebase.nongmsauth.api.service

import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenRequest
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SecureTokenApi {

    @POST("v1/token")
    fun exchangeToken(@Body request: ExchangeTokenRequest): Call<ExchangeTokenResponse>

    companion object {
        const val BASE_URL = "https://securetoken.googleapis.com/"
    }

}
