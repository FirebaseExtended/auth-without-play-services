package com.google.firebase.nongmsauth.api.service

import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyRequest
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FirebaseAuthApi {

    @POST("identitytoolkit/v3/relyingparty/signupNewUser")
    fun signInAnonymously(@Body request: SignInAnonymouslyRequest): Call<SignInAnonymouslyResponse>

    companion object {
        const val BASE_URL = "https://www.googleapis.com/"

        fun getInstance(client: OkHttpClient): FirebaseAuthApi {
            // Retrofit client pointed at the Firebase Auth API
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(FirebaseAuthApi::class.java)
        }
    }

}
