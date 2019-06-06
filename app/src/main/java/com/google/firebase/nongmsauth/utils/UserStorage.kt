package com.google.firebase.nongmsauth.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.nongmsauth.FirebaseRestAuthUser

class UserStorage(context: Context, private val app: FirebaseApp) {

    private val prefs: SharedPreferences = context.getSharedPreferences("UserStorage", Context.MODE_PRIVATE)

    fun set(user: FirebaseRestAuthUser?) {
        if (user == null) {
            clear()
            return
        }

        this.prefs.edit()
            .putString(getKey("idToken"), user.idToken)
            .putString(getKey("refreshToken"), user.refreshToken)
            .apply()
    }

    fun get(): FirebaseRestAuthUser? {
        val idToken = this.prefs.getString(getKey("idToken"), null);
        val refreshToken = this.prefs.getString(getKey("refreshToken"), null);

        if (idToken != null && refreshToken != null) {
            return FirebaseRestAuthUser(idToken, refreshToken)
        }

        return null
    }

    private fun clear() {
        this.prefs.edit().clear().apply()
    }

    private fun getKey(field: String): String {
        return "UserStorage___${app.name}__$field"
    }

}
