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

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitUtils {

    companion object {

        fun <T> callToTask(call: Call<T>): Task<T> {
            val source = TaskCompletionSource<T>()

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    source.trySetException(Exception(t))
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body == null) {
                        source.trySetException(Exception("Body null, status code ${response.code()}"))
                    } else {
                        source.trySetResult(body)
                    }
                }

            })

            return source.task
        }

    }

}
