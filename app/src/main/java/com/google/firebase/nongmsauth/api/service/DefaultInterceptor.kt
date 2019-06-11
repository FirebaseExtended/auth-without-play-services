/**
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.nongmsauth.api.service

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds API Key param and Content-Type header to every request.
 */
class DefaultInterceptor(private val apiKey: String) : Interceptor {

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
