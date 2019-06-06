package com.google.firebase.nongmsauth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseRestAuth;
    private lateinit var refresher: FirebaseTokenRefresher;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = FirebaseApp.getInstance()
        auth = FirebaseRestAuth.getInstance(app)
        refresher = FirebaseTokenRefresher(auth, this)

        getDocButton.setOnClickListener {
            getDocument()
        }

        signInButton.setOnClickListener {
            signInAnonymously()
        }

        signOutButton.setOnClickListener {
            signOut()
        }

        updateUI()
    }

    fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener { res ->
                Log.d(TAG, "signInAnonymously: ${res}")
                updateUI()
            }
            .addOnFailureListener { err ->
                Log.w(TAG, "signInAnonymously: failure", err)
                updateUI()
            }
    }

    fun signOut() {
        auth.signOut()
        updateUI()
    }

    fun getDocument() {
        FirebaseFirestore.getInstance().collection("test").document("test").get()
            .addOnCompleteListener {
                it.exception?.let { e ->
                    Log.e(TAG, "get failed", e)
                    toast("Error retrieving document...")
                    return@addOnCompleteListener
                }

                it.result?.let { res ->
                    Log.d(TAG, "get success: $res")
                    toast("Successfully retrieved document!")
                }
            }
    }

    fun updateUI() {
        val user = auth.currentUser
        val signedIn = user != null

        signInButton.isEnabled = !signedIn
        signOutButton.isEnabled = signedIn

        if (user == null) {
            authStatus.setText("Signed out.")
        } else {
            authStatus.setText("Signed in as ${user.userId}")
        }
    }

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
