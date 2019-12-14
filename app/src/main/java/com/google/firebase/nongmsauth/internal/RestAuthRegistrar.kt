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
package com.google.firebase.nongmsauth.internal

import androidx.annotation.Keep
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.internal.InternalAuthProvider
import com.google.firebase.components.Component
import com.google.firebase.components.ComponentRegistrar
import com.google.firebase.components.Dependency
import com.google.firebase.nongmsauth.FirebaseRestAuth

/**
 * Required so other Firebase libraries can find this implementation of InternalAuthProvider.
 * Note, you cannot also include the FirebaseAuth client library in your build.
 */
@Keep
class RestAuthRegistrar : ComponentRegistrar {

    override fun getComponents(): MutableList<Component<*>> {
        val restAuthComponent =
            Component.builder(InternalAuthProvider::class.java)
                .add(Dependency.required(FirebaseApp::class.java))
                .factory { container ->
                    val firebaseApp = container.get(FirebaseApp::class.java)
                    return@factory FirebaseRestAuth.getInstance(firebaseApp)
                }
                .build()

        return mutableListOf(restAuthComponent)
    }

}
