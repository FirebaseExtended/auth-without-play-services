package com.google.firebase.nongmsauth.api.types.identitytoolkit

data class SignInWithEmailRequest(
    val email: String,                      // The email the user is signing in with
    val password: String,                   // The password for the account
    val returnSecureToken: Boolean = true   // Whether or not to return an ID and refresh token. Should always be true.
)