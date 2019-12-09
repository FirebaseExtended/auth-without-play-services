/**
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.nongmsauth.api.service

import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyRequest
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyResponse
import com.google.firebase.nongmsauth.api.types.firebase.SignInWithCredentialRequest
import com.google.firebase.nongmsauth.api.types.firebase.SignInWithCredentialResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FirebaseAuthApi {

    @POST("v1/accounts:signUp")
    fun signInAnonymously(@Body request: SignInAnonymouslyRequest): Call<SignInAnonymouslyResponse>

    @POST("v1/accounts:signInWithIdp")
    fun signInWithCredential(@Body request: SignInWithCredentialRequest): Call<SignInWithCredentialResponse>

    companion object {
        private const val BASE_URL = "https://identitytoolkit.googleapis.com/"

        // Retrofit client pointed at the Firebase Auth API
        fun getInstance(client: OkHttpClient) = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseAuthApi::class.java)
    }
}
