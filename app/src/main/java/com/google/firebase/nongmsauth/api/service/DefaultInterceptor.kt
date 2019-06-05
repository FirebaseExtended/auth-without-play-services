package com.google.firebase.nongmsauth.api.service

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds API Key param and Content-Type header to every request.
 */
class DefaultInterceptor(val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalUrl = request.url()

        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("key", this.apiKey)
            .build()

        val requestBuilder = request.newBuilder()
            .header("Content-Type", "application/json")
            .url(newUrl)

        return chain.proceed(requestBuilder.build())
    }

}
