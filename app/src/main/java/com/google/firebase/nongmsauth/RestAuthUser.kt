package com.google.firebase.nongmsauth

import com.google.firebase.nongmsauth.utils.IdTokenParser

class RestAuthUser(
    val idToken: String,
    val refreshToken: String
) {

    val userId: String;

    init {
        val claims = IdTokenParser.parseIdToken(this.idToken)

        val userId = claims["user_id"]
        this.userId = "$userId"
    }

    override fun toString(): String {
        return "RestAuthUser(userId='$userId')"
    }

}
