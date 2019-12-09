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
package com.google.firebase.nongmsauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var auth: FirebaseRestAuth
    private lateinit var refresher: FirebaseTokenRefresher
    private lateinit var googleApiClient: GoogleApiClient
    private val RCSIGNIN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = FirebaseApp.getInstance()
        auth = FirebaseRestAuth.getInstance(app)
        auth.tokenRefresher.bindTo(this)

        googleApiClient = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
            .let { signInConfigBuilder ->
                // Build a GoogleApiClient with access to the Google Sign-In API and the
                // options specified in the sign-in configuration.
                GoogleApiClient.Builder(this)
                    .enableAutoManage(
                        this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */
                    )
                    .addApi(Auth.GOOGLE_SIGN_IN_API, signInConfigBuilder)
                    .build()
            }

        getDocButton.setOnClickListener {
            getDocument()
        }

        signInButton.setOnClickListener {
            // signInAnonymously()
            signInWithGoogle()
        }

        signOutButton.setOnClickListener {
            signOut()
        }

        updateUI()
    }

    private fun signInWithGoogle() {
        Auth.GoogleSignInApi.getSignInIntent(googleApiClient).let {
            startActivityForResult(it, RCSIGNIN)
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener { res ->
                Log.d(TAG, "signInAnonymously: $res")
                updateUI()
            }
            .addOnFailureListener { err ->
                Log.w(TAG, "signInAnonymously: failure", err)
                updateUI()
            }
    }

    private fun signOut() {
        auth.signOut()
        updateUI()
    }

    private fun getDocument() {
        FirebaseFirestore.getInstance().collection("test").document("test").get()
            .addOnCompleteListener {
                it.exception?.let { e ->
                    Log.e(TAG, "get failed", e)
                    snackbar("Error retrieving document...")
                    return@addOnCompleteListener
                }

                it.result?.let { res ->
                    Log.d(TAG, "get success: $res")
                    snackbar("Successfully retrieved document!")
                }
            }
    }

    fun updateUI() {
        val user = auth.currentUser
        val signedIn = user != null

        signInButton.isEnabled = !signedIn
        signOutButton.isEnabled = signedIn

        if (user == null) {
            authStatus.text = "Signed out."
        } else {
            authStatus.text = "Signed in as ${user.userId}"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RCSIGNIN) {
            Auth.GoogleSignInApi.getSignInResultFromIntent(data)?.apply {
                if (isSuccess) {
                    auth.signInWithGoogle(signInAccount!!.idToken!!)
                        .addOnFailureListener {
                            Log.w(TAG, "signInWithGoogle: failure", it)
                            updateUI()
                        }
                        .addOnSuccessListener {
                            Log.d(TAG, "signInWithGoogle: $it")
                            updateUI()
                        }
                }
            }
        }
    }

    override fun onConnectionFailed(res: ConnectionResult) {
        snackbar("Error trying to log in with Google")
        Log.e(TAG, res.errorMessage.toString())
    }

    fun snackbar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
