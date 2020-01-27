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
        val idToken = this.prefs.getString(getKey("idToken"), null)
        val refreshToken = this.prefs.getString(getKey("refreshToken"), null)

        if (idToken != null && refreshToken != null) {
            return FirebaseRestAuthUser(idToken, refreshToken)
        }

        return null
    }

    private fun clear() {
        this.prefs.edit().clear().apply()
    }

    private fun getKey(field: String): String {
        return "UserStorage___${app.options.projectId}__$field"
    }

}
