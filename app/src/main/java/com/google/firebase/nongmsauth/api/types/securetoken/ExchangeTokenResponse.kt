package com.google.firebase.nongmsauth.api.types.securetoken

data class ExchangeTokenResponse(
    var expires_in: String = "",
    var token_type: String = "",
    var refresh_token: String = "",
    var id_token: String = "",
    var user_id: String = "",
    var project_id: String = ""
)
