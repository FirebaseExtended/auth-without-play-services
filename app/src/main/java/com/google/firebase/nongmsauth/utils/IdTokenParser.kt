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
package com.google.firebase.nongmsauth.utils

import android.text.TextUtils
import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.FirebaseException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class IdTokenParser {

    companion object {

        fun parseIdToken(idToken: String): Map<String, Any> {
            val parts = idToken.split(".").toList()
            if (parts.size < 2) {
                return mapOf()
            }

            val encodedToken = parts[1]
            return try {
                val decodedToken = String(Base64Utils.decodeUrlSafeNoPadding(encodedToken), Charset.defaultCharset())
                parseRawUserInfo(decodedToken) ?: mapOf()
            } catch (e: UnsupportedEncodingException) {
                mapOf()
            }

        }

        @Throws(JSONException::class)
        fun toMap(json: JSONObject): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            val keyItr = json.keys()
            while (keyItr.hasNext()) {
                val key = keyItr.next()

                // Value can be a primitive, a map, or a list
                var value = json.get(key)
                if (value is JSONArray) {
                    value = toList(value)
                } else if (value is JSONObject) {
                    value = toMap(value)
                }

                map[key] = value
            }
            return map
        }

        @Throws(JSONException::class)
        fun toList(array: JSONArray): List<Any> {
            val list = mutableListOf<Any>()
            for (i in 0 until array.length()) {
                var value = array.get(i)
                if (value is JSONArray) {
                    value = toList(value)
                } else if (value is JSONObject) {
                    value = toMap(value)
                }
                list.add(value)
            }
            return list
        }

        private fun parseRawUserInfo(rawUserInfo: String): Map<String, Any>? {
            if (TextUtils.isEmpty(rawUserInfo)) {
                return null
            }

            try {
                val jsonObject = JSONObject(rawUserInfo)
                return if (jsonObject !== JSONObject.NULL) {
                    toMap(jsonObject)
                } else {
                    null
                }
            } catch (e: Exception) {
                throw FirebaseException(e.message!!)
            }

        }

    }
}
