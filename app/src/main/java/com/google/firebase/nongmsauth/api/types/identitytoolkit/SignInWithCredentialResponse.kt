package com.google.firebase.nongmsauth.api.types.identitytoolkit

data class SignInWithCredentialResponse(
        var kind: String = "",
        var federatedId: String = "", // The unique ID identifies the IdP account.
        var providerId: String = "", // The linked provider ID (e.g. "google.com" for the Google provider).
        var localId: String = "", // The uid of the authenticated user.
        var emailVerified: Boolean = true, // Whether the sign-in email is verified.
        var email: String = "", // The email of the account.
        var oauthIdToken: String = "", // The OIDC id token if available.
        //var oauthAccessToken: String = "", // The OAuth access token if available.
        //var oauthTokenSecret: String = "", // The OAuth 1.0 token secret if available.
        var idToken: String = "", // A Firebase Auth ID token for the authenticated user.
        var refreshToken: String = "", // A Firebase Auth refresh token for the authenticated user.
        var expiresIn: String = "", // The number of seconds in which the ID token expires.
        var rawUserInfo: String = "", // The stringified JSON response containing all the IdP data corresponding to the provided OAuth credential.
        var firstName: String = "", // The first name for the account.
        var lastName: String = "", // The last name for the account.
        var fullName: String = "", // The full name for the account.
        var displayName: String = "", // The display name for the account.
        var photoUrl: String = "" // The photo Url for the account.
        //var needConfirmation: Boolean = false // Whether another account with the same credential already exists. The user will need to sign in to the original account and then link the current credential to it.
)