package com.google.firebase.nongmsauth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: RestAuthProvider;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = FirebaseApp.getInstance()
        auth = RestAuthProvider.getInstance(app)

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
                it.exception?.let{ e->
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
