/*
 * Copyright (C) 2022 The Android Open Source Project
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
package dev.topping.ios.constraint.core.dsl

abstract class Guideline internal constructor(name: String) :
    Helper(name, HelperType("")) {
    private var mStart: Int = Int.MIN_VALUE
    private var mEnd: Int = Int.MIN_VALUE
    private var mPercent = Float.NaN
    /**
     * Get the start position
     *
     * @return start position
     */
    /**
     * Set the start position
     *
     * @param start the start position
     */
    var start: Int
        get() = mStart
        set(start) {
            mStart = start
            configMap?.put("start", mStart.toString())
        }
    /**
     * Get the end position
     *
     * @return end position
     */
    /**
     * Set the end position
     *
     * @param end the end position
     */
    var end: Int
        get() = mEnd
        set(end) {
            mEnd = end
            configMap?.put("end", mEnd.toString())
        }
    /**
     * Get the position in percent
     *
     * @return position in percent
     */
    /**
     * Set the position in percent
     *
     * @param percent the position in percent
     */
    var percent: Float
        get() = mPercent
        set(percent) {
            mPercent = percent
            configMap?.put("percent", mPercent.toString())
        }
}