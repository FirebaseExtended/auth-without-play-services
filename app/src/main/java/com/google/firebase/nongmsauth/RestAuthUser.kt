package com.google.firebase.nongmsauth

import com.google.firebase.nongmsauth.utils.IdTokenParser
import java.util.*

class RestAuthUser(
    val idToken: String,
    val refreshToken: String
) {

    val userId: String;
    val expTime: Long;

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
