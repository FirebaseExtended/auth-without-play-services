package com.google.firebase.nongmsauth

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.internal.IdTokenListener
import com.google.firebase.auth.internal.InternalAuthProvider
import com.google.firebase.internal.InternalTokenResult
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.google.firebase.nongmsauth.api.service.DefaultInterceptor
import com.google.firebase.nongmsauth.api.service.FirebaseAuthApi
import com.google.firebase.nongmsauth.api.service.SecureTokenApi
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyRequest
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyResponse
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenRequest
import com.google.firebase.nongmsauth.api.types.securetoken.ExchangeTokenResponse
import com.google.firebase.nongmsauth.utils.IdTokenParser
import com.google.firebase.nongmsauth.utils.RetrofitUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestAuthProvider(app: FirebaseApp) : InternalAuthProvider {

    val listeners = mutableListOf<IdTokenListener>()
    val apiKey: String

    val firebaseApi: FirebaseAuthApi
    val secureTokenApi: SecureTokenApi

    var currentUser: RestAuthUser? = null
        set(value) {
            Log.d(TAG, "currentUser = $value")
            field = value
            listeners.forEach { listener ->
                listener.onIdTokenChanged(InternalTokenResult(value?.idToken))
            }
        }

    init {
        this.apiKey = app.options.apiKey

        // OkHttpClient with the custom interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(DefaultInterceptor(this.apiKey))
            .build()

        // Retrofit client pointed at the Firebase Auth API
        val retrofit = Retrofit.Builder()
            .baseUrl(FirebaseAuthApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        this.firebaseApi = retrofit.create(FirebaseAuthApi::class.java)
        this.secureTokenApi = retrofit.create(SecureTokenApi::class.java)

        this.currentUser = null
    }

    fun signInAnonymously(): Task<SignInAnonymouslyResponse> {
        val task = RetrofitUtils.callToTask(
            this.firebaseApi.signInAnonymously(
                SignInAnonymouslyRequest(
                    true
                )
            )
        )

        task.addOnSuccessListener { res ->
            this.currentUser = RestAuthUser(res.idToken, res.refreshToken)
        }

        task.addOnFailureListener { e ->
            Log.e(TAG, "signInAnonymously: failed", e)
            this.currentUser = null
        }

        return task
    }

    fun signOut() {
        this.currentUser = null
    }

    private fun refreshUserToken(): Task<ExchangeTokenResponse> {
        val refreshToken = this.currentUser?.refreshToken

        if (refreshToken == null) {
            throw Exception("Can't refresh token, current user has no refresh token");
        }

        val request = ExchangeTokenRequest("refresh_token", refreshToken)
        val call = this.secureTokenApi.exchangeToken(request)
        val task = RetrofitUtils.callToTask(call)
            .addOnSuccessListener { res ->
                this.currentUser = RestAuthUser(res.id_token, res.refresh_token)
            }

        return task
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
        Log.d(TAG, "getAccessToken(${forceRefresh})")

        val source = TaskCompletionSource<GetTokenResult>()

        if (this.currentUser != null) {
            // Already signed in
            if (!forceRefresh) {
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
            source.trySetException(FirebaseNoSignedInUserException("Please sign in before trying to get a token"));
        }

        return source.task
    }

    override fun addIdTokenListener(listener: IdTokenListener) {
        Log.d(TAG, "addIdTokenListener: $listener")
        // TODO: Implement and start proactive refresh
        listeners.add(listener)
    }

    override fun removeIdTokenListener(listener: IdTokenListener) {
        Log.d(TAG, "removeIdTokenListener $listener")
        listeners.remove(listener)
    }

    companion object {
        const val TAG = "RestAuthProvider"

        val INSTANCES = mutableMapOf<String,RestAuthProvider>()

        fun getInstance(app: FirebaseApp): RestAuthProvider {
            if (!INSTANCES.containsKey(app.name)) {
                val instance = RestAuthProvider(app)
                INSTANCES.set(app.name, instance);
            }

            return INSTANCES.get(app.name)!!;
        }
    }
}
