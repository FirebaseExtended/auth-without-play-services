package com.google.firebase.nongmsauth

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.internal.InternalAuthProvider
import com.google.firebase.nongmsauth.api.types.firebase.SignInAnonymouslyResponse
import com.google.firebase.nongmsauth.internal.RestAuthProvider

interface FirebaseRestAuth : InternalAuthProvider {

    var currentUser: FirebaseRestAuthUser?

    fun signInAnonymously(): Task<SignInAnonymouslyResponse>
    fun signOut()

    companion object {
        val INSTANCES = mutableMapOf<String, RestAuthProvider>()

        fun getInstance(app: FirebaseApp): FirebaseRestAuth {
            if (!INSTANCES.containsKey(app.name)) {
                val instance = RestAuthProvider(app)
                INSTANCES.set(app.name, instance);
            }

            return INSTANCES.get(app.name)!!;
        }
    }
}
