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
import com.google.firebase.nongmsauth.api.service.FirebaseKeyInterceptor
import com.google.firebase.nongmsauth.api.service.IdentityToolkitApi
import com.google.firebase.nongmsauth.api.service.SecureTokenApi
import com.google.firebase.nongmsauth.api.types.identitytoolkit.SignInAnonymouslyRequest
import com.google.firebase.nongmsauth.api.types.identitytoolkit.SignInWithCustomTokenRequest
import com.google.firebase.nongmsauth.api.types.identitytoolkit.SignInWithCustomTokenResponse
import com.google.firebase.nongmsauth.api.types.identitytoolkit.SignInWithEmailRequest
import com.google.firebase.nongmsauth.api.types.identitytoolkit.SignInWithEmailResponse
import com.google.firebase.nongmsauth.api.types.identitytoolkit.SignInAnonymouslyResponse
import com.google.firebase.nongmsauth.api.types.identitytoolkit.SignUpWithEmailResponse
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenRequest
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenResponse
import com.google.firebase.nongmsauth.utils.ExpirationUtils
import com.google.firebase.nongmsauth.utils.IdTokenParser
import com.google.firebase.nongmsauth.utils.RetrofitUtils
import com.google.firebase.nongmsauth.utils.UserStorage
import okhttp3.OkHttpClient

/**
 * Implementation of FirebaseRestAuth
 * @param app FirebaseApp
 * @param apiKey Web API Key from Firebase Console, usually provided by google-services.json, but there are some cases where this does not match your assigned Web API Key and you will need to override
 */
class RestAuthProvider(app: FirebaseApp, apiKey: String = app.options.apiKey) : FirebaseRestAuth {

    private val context = app.applicationContext
    private val userStorage = UserStorage(context, app)
    private val listeners = mutableListOf<IdTokenListener>()
    private val firebaseApi: IdentityToolkitApi
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
        // OkHttpClient with the custom interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(FirebaseKeyInterceptor(apiKey))
            .build()

        this.firebaseApi = IdentityToolkitApi.getInstance(client)
        this.secureTokenApi = SecureTokenApi.getInstance(client)

        // TODO: What if the persisted user is expired?
        this.currentUser = userStorage.get()
    }

    override fun signInAnonymously(): Task<SignInAnonymouslyResponse> {
        return RetrofitUtils.callToTask(
            this.firebaseApi.signInAnonymously(
                SignInAnonymouslyRequest()
            )
        ).addOnSuccessListener { res ->
            this.currentUser = FirebaseRestAuthUser(res.idToken, res.refreshToken)
        }.addOnFailureListener { e ->
            Log.e(TAG, "signInAnonymously: failed", e)
            this.currentUser = null
        }
    }

    override fun signInWithCustomToken(token: String): Task<SignInWithCustomTokenResponse> {
        return RetrofitUtils.callToTask(
            this.firebaseApi.signInWithCustomToken(
                SignInWithCustomTokenRequest(token)
            )
        ).addOnSuccessListener { res ->
            this.currentUser = FirebaseRestAuthUser(res.idToken, res.refreshToken)
        }.addOnFailureListener { e ->
            Log.e(TAG, "signInWithCustomToken: failed", e)
            this.currentUser = null
        }
    }

    override fun signInWithEmail(email: String, password: String): Task<SignInWithEmailResponse> {
        return RetrofitUtils.callToTask(
            this.firebaseApi.signInWithPassword(
                SignInWithEmailRequest(email, password)
            )
        ).addOnSuccessListener { res ->
            this.currentUser = FirebaseRestAuthUser(res.idToken, res.refreshToken)
        }.addOnFailureListener { e ->
            Log.e(TAG, "signInWithEmail: failed", e)
            this.currentUser = null
        }
    }

    override fun signUpWithEmail(email: String, password: String): Task<SignUpWithEmailResponse> {
        return RetrofitUtils.callToTask(
            this.firebaseApi.signUpWithEmail(
                SignInWithEmailRequest(email, password)
            )
        ).addOnSuccessListener { res ->
            this.currentUser = FirebaseRestAuthUser(res.idToken, res.refreshToken)
        }.addOnFailureListener { e ->
            Log.e(TAG, "signUpWithEmail: failed", e)
            this.currentUser = null
        }
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
        private val TAG = RestAuthProvider::class.java.simpleName
    }
}