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

import com.google.firebase.nongmsauth.utils.IdTokenParser
import java.util.*

class FirebaseRestAuthUser(
    val idToken: String,
    val refreshToken: String
) {

    val userId: String;
    private val expTime: Long;

    init {
        val claims = IdTokenParser.parseIdToken(this.idToken)

        this.userId = claims["user_id"].toString()
        this.expTime = claims["exp"].toString().toLong()
    }

    fun isExpired(): Boolean {
        return expiresInSeconds() <= 0
    }

    fun expiresInSeconds(): Long {
        val now = Date().time / 1000;
        return this.expTime - now;
    }

    override fun toString(): String {
        return "RestAuthUser(userId='$userId', expiresIn=${expiresInSeconds()})"
    }

}
