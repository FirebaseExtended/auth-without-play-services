package com.google.firebase.nongmsauth.utils

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitUtils {

    companion object {

        fun <T> callToTask(call: Call<T>): Task<T> {
            val source = TaskCompletionSource<T>();

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
