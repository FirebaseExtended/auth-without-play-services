package com.google.firebase.nongmsauth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.nongmsauth.utils.TokenRefresher
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: RestAuthProvider;
    private lateinit var refresher: TokenRefresher;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = FirebaseApp.getInstance()
        auth = RestAuthProvider.getInstance(app)
        refresher = TokenRefresher(auth)

        getDocButton.setOnClickListener {
            getDocument()
        }

        signInButton.setOnClickListener {
            signInAnonymously()
        }

        signOutButton.setOnClickListener {
            signOut()
        }
    }

    override fun onStart() {
        super.onStart()
        this.refresher.start()
    }

    override fun onStop() {
        super.onStop()
        this.refresher.stop()
    }

    fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener { res ->
                Log.d(TAG, "signInAnonymously: ${res}")
            }
            .addOnFailureListener { err ->
                Log.w(TAG, "signInAnonymously: failure", err)
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getDocument() {
        FirebaseFirestore.getInstance().collection("test").document("test").get()
            .addOnCompleteListener {
                it.exception?.let { e ->
                    Log.e(TAG, "get failed", e)
                    return@addOnCompleteListener
                }

                it.result?.let { res ->
                    Log.d(TAG, "get success: $res")
                }
            }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
