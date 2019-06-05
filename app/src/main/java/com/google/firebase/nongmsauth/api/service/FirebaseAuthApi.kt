package com.google.firebase.nongmsauth.api.service

import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyRequest
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FirebaseAuthApi {

    // signInAnonymously
    // POST https://www.googleapis.com/identitytoolkit/v3/relyingparty/signupNewUser?key=[API_KEY]
    // {
    //  "returnSecureToken": true
    // }

    @POST("identitytoolkit/v3/relyingparty/signupNewUser")
    fun signInAnonymously(@Body request: SignInAnonymouslyRequest): Call<SignInAnonymouslyResponse>

    companion object {
        const val BASE_URL = "https://www.googleapis.com/"
    }

}
