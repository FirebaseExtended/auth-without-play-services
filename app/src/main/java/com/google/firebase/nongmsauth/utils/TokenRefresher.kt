package com.google.firebase.nongmsauth.utils

import android.os.Handler
import android.util.Log
import com.google.firebase.auth.internal.IdTokenListener
import com.google.firebase.internal.InternalTokenResult
import com.google.firebase.nongmsauth.RestAuthProvider

class TokenRefresher(val auth: RestAuthProvider) : IdTokenListener {

    val handler: Handler = Handler();
    val refreshRunnable: Runnable;
    var lastToken: String? = null;

    init {
        this.refreshRunnable = object : Runnable {
            override fun run() {
                val user = auth.currentUser
                if (user == null) {
                    Log.d(TAG, "User signed out, nothing to refresh.")
                    return;
                }

                val minSecsRemaining = TEN_MINUTES_SECS;
                val secsRemaining = user.expiresInSeconds();
                val diffSecs = secsRemaining - minSecsRemaining;

                // If the token has enough time left, run a refresh later.
                if (diffSecs > 0) {
                    Log.d(TAG, "Token expires in ${user.expiresInSeconds()}, scheduling refresh in $diffSecs seconds");
                    handler.postDelayed(this, diffSecs * 1000);
                    return;
                }

                // Time to refresh the token now
                Log.d(TAG, "Refreshing token now!");
                auth.getAccessToken(true)
                    .addOnSuccessListener {
                        // On success just re-post, the logic above will handle the timing.
                        Log.d(TAG, "Token refreshed successfully.")
                        handler.post(this)
                    }
                    .addOnFailureListener { e ->
                        // TODO: Handle failure, need to reschedule
                        Log.e(TAG, "Failed to refresh token", e)
                    }
            }
        }
    }

    fun start() {
        Log.d(TAG, "start()")
        this.auth.addIdTokenListener(this)
        this.handler.post(this.refreshRunnable)
    }

    fun stop() {
        Log.d(TAG, "stop()")
        this.auth.removeIdTokenListener(this)
        this.handler.removeCallbacksAndMessages(null)
        this.lastToken = null
    }

    override fun onIdTokenChanged(res: InternalTokenResult) {
        val token = res.token
        if (token != null && lastToken == null) {
            // We are now signed in, time to start refreshing
            Log.d(TAG, "Token changed from $lastToken to $token, starting refreshing");
            this.handler.post(this.refreshRunnable)
        }

        this.lastToken = token
    }

    companion object {
        const val TAG = "TokenRefresher"
        const val TEN_MINUTES_SECS = 10 * 60;
    }

}
