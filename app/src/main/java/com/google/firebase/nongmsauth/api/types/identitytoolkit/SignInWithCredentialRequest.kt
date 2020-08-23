package com.google.firebase.nongmsauth.api.types.identitytoolkit

data class SignInWithCredentialRequest(
        var requestUri: String = "", // The URI to which the IDP redirects the user back.
        var postBody: String = "", // Contains the OAuth credential (an ID token or access token) and provider ID which issues the credential.
        var returnSecureToken: Boolean = true, // Whether or not to return an ID and refresh token. Should always be true.
        var returnIdpCredential: Boolean = true // Whether to force the return of the OAuth credential on the following errors: FEDERATED_USER_ID_ALREADY_LINKED and EMAIL_EXISTS.
)