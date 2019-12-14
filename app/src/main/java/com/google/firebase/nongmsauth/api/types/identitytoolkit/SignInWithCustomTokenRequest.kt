package com.google.firebase.nongmsauth.api.types.identitytoolkit

data class SignInWithCustomTokenRequest(
    val token: String,
    val returnSecureToken: Boolean = true
)