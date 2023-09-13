/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.topping.ios.constraint.core.motion.utils

interface StopEngine {
    // @TODO: add description
    fun debug(desc: String, time: Float): String

    // @TODO: add description
    fun getVelocity(time: Float): Float

    // @TODO: add description
    fun getInterpolation(time: Float): Float

    // @TODO: add description
    val velocity: Float

    // @TODO: add description
    val isStopped: Boolean
}