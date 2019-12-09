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

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.internal.InternalAuthProvider
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyResponse
import com.google.firebase.nongmsauth.api.types.firebase.SignInWithCredentialResponse
import com.google.firebase.nongmsauth.internal.RestAuthProvider
interface FirebaseRestAuth : InternalAuthProvider {

    val tokenRefresher: FirebaseTokenRefresher
    var currentUser: FirebaseRestAuthUser?

    fun signInAnonymously(): Task<SignInAnonymouslyResponse>
    fun signOut()
    fun signInWithGoogle(idToken: String, provider: String = "google.com"): Task<SignInWithCredentialResponse>

    companion object {
        private val INSTANCE = mutableMapOf<String, RestAuthProvider>()

        fun getInstance(app: FirebaseApp): FirebaseRestAuth {
            if (!INSTANCE.containsKey(app.name)) {
                val instance = RestAuthProvider(app)
                INSTANCE[app.name] = instance
            }

            return INSTANCE[app.name]!!
        }
    }
}

