package com.google.firebase.nongmsauth

import android.os.Handler
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.auth.internal.IdTokenListener
import com.google.firebase.internal.InternalTokenResult

class FirebaseTokenRefresher(val auth: FirebaseRestAuth, val owner: LifecycleOwner) : IdTokenListener,
    LifecycleObserver {

    val handler: Handler = Handler();
    val refreshRunnable: Runnable;

    var failureRetryTimeSecs: Long = MIN_RETRY_BACKOFF_SECS;
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
                Log.d(TAG, "Token expires in $secsRemaining, refreshing token now!");
                auth.getAccessToken(true)
                    .addOnSuccessListener {
                        // On success just re-post, the logic above will handle the timing.
                        Log.d(TAG, "Token refreshed successfully.")
                        handler.post(this)

                        // Clear the failure backoff
                        failureRetryTimeSecs = MIN_RETRY_BACKOFF_SECS
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to refresh token", e)
                        Log.d(TAG, "Retrying in $failureRetryTimeSecs...")

                        // Retry and double the backoff time (up to the max)
                        handler.postDelayed(this, failureRetryTimeSecs * 1000);
                        failureRetryTimeSecs = Math.min(failureRetryTimeSecs * 2, MAX_RETRY_BACKOFF_SECS)
                    }
            }
        }

        this.owner.lifecycle.addObserver(this)
    }

    @Keep
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifecycleStarted() {
        this.start()
    }

    @Keep
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onLifecycleStopped() {
        this.stop()
    }

    private fun start() {
        Log.d(TAG, "start()")
        this.auth.addIdTokenListener(this)
        this.handler.post(this.refreshRunnable)
    }

    private fun stop() {
        Log.d(TAG, "stop()")
        this.auth.removeIdTokenListener(this)
        this.handler.removeCallbacksAndMessages(null)
        this.lastToken = null
    }

    override fun onIdTokenChanged(res: InternalTokenResult) {
        val token = res.token
        if (token != null && lastToken == null) {
            // We are now signed in, time to start refreshing
            Log.d(TAG, "Token changed from null --> non-null, starting refreshing");
            this.handler.post(this.refreshRunnable)
        }

        if (lastToken != null && token == null) {
            // The user signed out, stop all refreshing
            Log.d(TAG, "Signed out, canceling refreshes")
            this.handler.removeCallbacksAndMessages(null)
        }

        this.lastToken = token
    }

    companion object {
        const val TAG = "TokenRefresher"

        const val TEN_MINUTES_SECS = 10 * 60;

        const val MIN_RETRY_BACKOFF_SECS: Long = 30;
        const val MAX_RETRY_BACKOFF_SECS: Long = 5 * 60;
    }

}
