package com.google.firebase.nongmsauth.utils

import android.text.TextUtils
import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.FirebaseException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * TODO: This whole class is cribbed
 */
class IdTokenParser {

    companion object {

        fun parseIdToken(idToken: String): Map<String, Any> {
            val parts = idToken.split(".").toList()
            if (parts.size < 2) {
                return mapOf()
            }

            val encodedToken = parts[1]
            try {
                val decodedToken = String(Base64Utils.decodeUrlSafeNoPadding(encodedToken), Charset.defaultCharset())
                val map = parseRawUserInfo(decodedToken)
                if (map == null) {
                    return mapOf()
                } else {
                    return map
                }
            } catch (e: UnsupportedEncodingException) {
                return mapOf()
            }

        }

        fun parseRawUserInfo(rawUserInfo: String): Map<String, Any>? {
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

                map.put(key, value)
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

    }

}
