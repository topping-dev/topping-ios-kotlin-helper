/*
 * Copyright (C) 2020 The Android Open Source Project
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

import dev.topping.ios.constraint.Arrays

/**
 * Used by KeyTimeCycles (and any future time dependent behaviour) to cache its current parameters
 * to maintain consistency across requestLayout type rebuilds.
 */
class KeyCache {
    var mMap: MutableMap<Any, MutableMap<String, FloatArray>> = mutableMapOf()

    // @TODO: add description
    fun setFloatValue(view: Any, type: String, element: Int, value: Float) {
        if (!mMap.containsKey(view)) {
            val array: HashMap<String, FloatArray> = HashMap()
            val vArray = FloatArray(element + 1)
            vArray[element] = value
            array[type] = vArray
            mMap[view] = array
        } else {
            var array = mMap[view]
            if (array == null) {
                array = HashMap()
            }
            if (!array.containsKey(type)) {
                val vArray = FloatArray(element + 1)
                vArray[element] = value
                array[type] = vArray
                mMap[view] = array
            } else {
                var vArray = array[type]
                if (vArray == null) {
                    vArray = FloatArray(0)
                }
                if (vArray.size <= element) {
                    vArray = (Arrays.copyOfN(vArray.toTypedArray(), element + 1) as Array<Float>).toFloatArray()
                }
                vArray[element] = value
                array[type] = vArray
            }
        }
    }

    // @TODO: add description
    fun getFloatValue(view: Any, type: String, element: Int): Float {
        return if (!mMap.containsKey(view)) {
            Float.NaN
        } else {
            val array = mMap[view]
            if (array == null || !array.containsKey(type)) {
                return Float.NaN
            }
            val vArray = array[type] ?: return Float.NaN
            if (vArray.size > element) {
                vArray[element]
            } else Float.NaN
        }
    }
}