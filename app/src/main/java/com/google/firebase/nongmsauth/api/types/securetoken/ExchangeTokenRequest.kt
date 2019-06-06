package com.google.firebase.nongmsauth.api.types.securetoken

data class ExchangeTokenRequest(
    var grant_type: String = "",
    var refresh_token: String = ""
)
