package com.google.firebase.nongmsauth.api.types.firebase

data class SignInAnonymouslyResponse(
    var kind: String = "",
    var idToken: String = "",
    var email: String = "",
    var refreshToken: String = "",
    var expiresIn: String = "",
    var localId: String = ""
)
