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
package dev.topping.ios.constraint.constraintlayout.motion.utils

import dev.topping.ios.constraint.Log
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.pow

object CustomSupport {
    private const val TAG = "CustomSupport"
    private const val I_DEBUG = false

    /**
     * sets the interpolated value
     * @param att
     * @param view
     * @param value
     */
    fun setInterpolatedValue(att: ConstraintAttribute, view: TView, value: FloatArray) {
        val methodName = "set" + att.name
        when (att.type) {
            ConstraintAttribute.AttributeType.INT_TYPE -> {
                view.setReflectionValue(methodName, value[0].toInt())
            }
            ConstraintAttribute.AttributeType.FLOAT_TYPE -> {
                view.setReflectionValue(methodName, value[0].toFloat())
            }
            ConstraintAttribute.AttributeType.COLOR_DRAWABLE_TYPE -> {
                val r = clamp(
                    (pow(
                        value[0], 1.0 / 2.2
                    ).toFloat() * 255.0f).toInt()
                )
                val g = clamp(
                    (pow(
                        value[1], 1.0 / 2.2
                    ).toFloat() * 255.0f).toInt()
                )
                val b = clamp(
                    (pow(
                        value[2], 1.0 / 2.2
                    ).toFloat() * 255.0f).toInt()
                )
                val a = clamp((value[3] * 255.0f).toInt())
                view.setReflectionColorDrawable(methodName, r, g, b, a)
            }
            ConstraintAttribute.AttributeType.COLOR_TYPE -> {
                val r = clamp(
                    (pow(
                        value[0], 1.0 / 2.2
                    ).toFloat() * 255.0f).toInt()
                )
                val g = clamp(
                    (pow(
                        value[1], 1.0 / 2.2
                    ).toFloat() * 255.0f).toInt()
                )
                val b = clamp(
                    (pow(
                        value[2], 1.0 / 2.2
                    ).toFloat() * 255.0f).toInt()
                )
                val a = clamp((value[3] * 255.0f).toInt())
                view.setReflectionColor(methodName, r, g, b, a)
            }
            ConstraintAttribute.AttributeType.STRING_TYPE -> return
            ConstraintAttribute.AttributeType.BOOLEAN_TYPE -> {
                view.setReflectionValue(methodName, value[0] > 0.5f)
            }
            ConstraintAttribute.AttributeType.DIMENSION_TYPE -> {
                view.setReflectionValue(methodName, value[0])
            }
            else -> if (I_DEBUG) {
                Log.v(TAG, att.type.toString())
            }
        }
    }

    private fun clamp(c: Int): Int {
        var c = c
        val n = 255
        c = c and (c shr 31).inv()
        c -= n
        c = c and (c shr 31)
        c += n
        return c
    }
}