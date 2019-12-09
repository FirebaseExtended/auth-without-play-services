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
package com.google.firebase.nongmsauth.internal

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.internal.IdTokenListener
import com.google.firebase.internal.InternalTokenResult
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.google.firebase.nongmsauth.FirebaseRestAuth
import com.google.firebase.nongmsauth.FirebaseRestAuthUser
import com.google.firebase.nongmsauth.FirebaseTokenRefresher
import com.google.firebase.nongmsauth.api.service.DefaultInterceptor
import com.google.firebase.nongmsauth.api.service.FirebaseAuthApi
import com.google.firebase.nongmsauth.api.service.SecureTokenApi
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyRequest
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyResponse
import com.google.firebase.nongmsauth.api.types.firebase.SignInWithCredentialRequest
import com.google.firebase.nongmsauth.api.types.firebase.SignInWithCredentialResponse
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenRequest
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenResponse
import com.google.firebase.nongmsauth.utils.ExpirationUtils
import com.google.firebase.nongmsauth.utils.IdTokenParser
import com.google.firebase.nongmsauth.utils.RetrofitUtils
import com.google.firebase.nongmsauth.utils.UserStorage
import okhttp3.OkHttpClient

class RestAuthProvider(app: FirebaseApp) : FirebaseRestAuth {

    private val context = app.applicationContext
    private val userStorage = UserStorage(context, app)
    private val listeners = mutableListOf<IdTokenListener>()
    private val firebaseApi: FirebaseAuthApi
    private val secureTokenApi: SecureTokenApi

    override val tokenRefresher = FirebaseTokenRefresher(this)

    override var currentUser: FirebaseRestAuthUser? = null
        set(value) {
            Log.d(TAG, "currentUser = $value")

            // Set the local field
            field = value

            // Set the value in persistence
            userStorage.set(value)

            listeners.forEach { listener ->
                listener.onIdTokenChanged(InternalTokenResult(value?.idToken))
            }
        }

    init {
        val apiKey = app.options.apiKey

        // OkHttpClient with the custom interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(DefaultInterceptor(apiKey))
            .build()

        this.firebaseApi = FirebaseAuthApi.getInstance(client)
        this.secureTokenApi = SecureTokenApi.getInstance(client)

        // TODO: What if the persisted user is expired?
        this.currentUser = userStorage.get()
    }

    override fun signInAnonymously(): Task<SignInAnonymouslyResponse> {
        val task = RetrofitUtils.callToTask(
            this.firebaseApi.signInAnonymously(
                SignInAnonymouslyRequest(true)
            )
        )

        task.addOnSuccessListener { res ->
            this.currentUser = FirebaseRestAuthUser(res.idToken, res.refreshToken)
        }

        task.addOnFailureListener { e ->
            Log.e(TAG, "signInAnonymously: failed", e)
            this.currentUser = null
        }

        return task
    }

    override fun signInWithGoogle(idToken: String, provider: String): Task<SignInWithCredentialResponse> {
        val task = RetrofitUtils.callToTask(
            this.firebaseApi.signInWithCredential(
                SignInWithCredentialRequest(
                    postBody = "id_token=$idToken&providerId=$provider",
                    returnSecureToken = true,
                    returnIdpCredential = true,
                    requestUri = "http://localhost"
                )
            )
        )

        task.addOnSuccessListener { res ->
            this.currentUser = FirebaseRestAuthUser(
                idToken = res.idToken,
                refreshToken = res.refreshToken
            )
            Log.d(TAG, "signInWithCredential: successful!, uid: ${currentUser?.userId}")
        }

        task.addOnFailureListener { e ->
            Log.e(TAG, "signInWithCredential: failed", e)
            this.currentUser = null
        }

        return task
    }

    override fun signOut() {
        this.currentUser = null
    }

    private fun refreshUserToken(): Task<ExchangeTokenResponse> {
        val refreshToken = this.currentUser?.refreshToken
            ?: throw Exception("Can't refresh token, current user has no refresh token")

        val request = ExchangeTokenRequest("refresh_token", refreshToken)
        val call = this.secureTokenApi.exchangeToken(request)

        return RetrofitUtils.callToTask(call)
            .addOnSuccessListener { res ->
                currentUser = FirebaseRestAuthUser(res.id_token, res.refresh_token)
            }
    }

    private fun currentUserToTokenResult(): GetTokenResult {
        val token = this.currentUser!!.idToken
        return GetTokenResult(token, IdTokenParser.parseIdToken(token))
    }


    override fun getUid(): String? {
        Log.d(TAG, "getUid()")
        return this.currentUser?.userId
    }

    override fun getAccessToken(forceRefresh: Boolean): Task<GetTokenResult> {
        Log.d(TAG, "getAccessToken($forceRefresh)")

        val source = TaskCompletionSource<GetTokenResult>()
        val user = this.currentUser

        if (user != null) {
            val needsRefresh = forceRefresh || ExpirationUtils.isExpired(user)
            if (!needsRefresh) {
                // Return the current token, no need to check anything
                source.trySetResult(currentUserToTokenResult())
            } else {
                // Get a new token and then return
                this.refreshUserToken()
                    .addOnSuccessListener {
                        source.trySetResult(currentUserToTokenResult())
                    }
                    .addOnFailureListener { e ->
                        source.trySetException(e)
                    }
            }
        } else {
            // Not yet signed in
            source.trySetException(FirebaseNoSignedInUserException("Please sign in before trying to get a token"))
        }

        return source.task
    }

    /**
     * Note: the JavaDoc says that we should start proactive token refresh here. In order to have better lifecycle
     * management, we force the user to manually start a "FirebaseTokenRefresher" instead.
     */
    override fun addIdTokenListener(listener: IdTokenListener) {
        Log.d(TAG, "addIdTokenListener: $listener")
        listeners.add(listener)
    }

    override fun removeIdTokenListener(listener: IdTokenListener) {
        Log.d(TAG, "removeIdTokenListener $listener")
        listeners.remove(listener)
    }

    companion object {
        const val TAG = "RestAuthProvider"
    }
}
