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
package dev.topping.ios.constraint.core.motion

import dev.topping.ios.constraint.core.motion.utils.TypedValues
import dev.topping.ios.constraint.pow
import dev.topping.ios.constraint.toHexString

import kotlin.math.*

/**
 * Defines non standard Attributes
 */
class CustomVariable {
    var name: String
    var type: Int
        private set
    var integerValue: Int = Int.MIN_VALUE
    var floatValue = Float.NaN
    var stringValue: String? = null
    var booleanValue = false
    var colorValue = integerValue
        get() = integerValue

    // @TODO: add description
    fun copy(): CustomVariable {
        return CustomVariable(this)
    }

    constructor(c: CustomVariable) {
        name = c.name
        type = c.type
        integerValue = c.integerValue
        floatValue = c.floatValue
        stringValue = c.stringValue
        booleanValue = c.booleanValue
    }

    constructor(name: String, type: Int, value: String?) {
        this.name = name
        this.type = type
        stringValue = value
    }

    constructor(name: String, type: Int, value: Int) {
        this.name = name
        this.type = type
        if (type == TypedValues.Custom.TYPE_FLOAT) { // catch int ment for float
            floatValue = value.toFloat()
        } else {
            integerValue = value
        }
    }

    constructor(name: String, type: Int, value: Float) {
        this.name = name
        this.type = type
        floatValue = value
    }

    constructor(name: String, type: Int, value: Boolean) {
        this.name = name
        this.type = type
        booleanValue = value
    }

    
    override fun toString(): String {
        val str = name + ':'
        when (type) {
            TypedValues.Custom.TYPE_INT -> return str + integerValue
            TypedValues.Custom.TYPE_FLOAT -> return str + floatValue
            TypedValues.Custom.TYPE_COLOR -> return str + colorString(
                integerValue
            )
            TypedValues.Custom.TYPE_STRING -> return str + stringValue
            TypedValues.Custom.TYPE_BOOLEAN -> return str + booleanValue
            TypedValues.Custom.TYPE_DIMENSION -> return str + floatValue
        }
        return "$str????"
    }

    /**
     * Continuous types are interpolated they are fired only at
     */
    val isContinuous: Boolean
        get() = when (type) {
            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_BOOLEAN, TypedValues.Custom.TYPE_STRING -> false
            else -> true
        }

    fun setIntValue(value: Int) {
        integerValue = value
    }

    /**
     * The number of interpolation values that need to be interpolated
     * Typically 1 but 3 for colors.
     *
     * @return Typically 1 but 3 for colors.
     */
    fun numberOfInterpolatedValues(): Int {
        return when (type) {
            TypedValues.Custom.TYPE_COLOR -> 4
            else -> 1
        }
    }

    /**
     * Transforms value to a float for the purpose of interpolation
     *
     * @return interpolation value
     */
    val valueToInterpolate: Float
        get() {
            when (type) {
                TypedValues.Custom.TYPE_INT -> return integerValue.toFloat()
                TypedValues.Custom.TYPE_FLOAT -> return floatValue
                TypedValues.Custom.TYPE_COLOR -> throw RuntimeException("TColor does not have a single color to interpolate")
                TypedValues.Custom.TYPE_STRING -> throw RuntimeException("Cannot interpolate String")
                TypedValues.Custom.TYPE_BOOLEAN -> return if (booleanValue) 1f else 0f
                TypedValues.Custom.TYPE_DIMENSION -> return floatValue
            }
            return Float.NaN
        }

    // @TODO: add description
    fun getValuesToInterpolate(ret: FloatArray) {
        when (type) {
            TypedValues.Custom.TYPE_INT -> ret[0] = integerValue.toFloat()
            TypedValues.Custom.TYPE_FLOAT -> ret[0] = floatValue
            TypedValues.Custom.TYPE_COLOR -> {
                val a = 0xFF and (integerValue shr 24)
                val r = 0xFF and (integerValue shr 16)
                val g = 0xFF and (integerValue shr 8)
                val b = 0xFF and integerValue
                val f_r = pow(r / 255.0f, 2.2)
                val f_g = pow(g / 255.0f, 2.2)
                val f_b = pow(b / 255.0f, 2.2)
                ret[0] = f_r
                ret[1] = f_g
                ret[2] = f_b
                ret[3] = a / 255f
            }
            TypedValues.Custom.TYPE_STRING -> throw RuntimeException("Cannot interpolate String")
            TypedValues.Custom.TYPE_BOOLEAN -> ret[0] = if (booleanValue) 1f else 0f
            TypedValues.Custom.TYPE_DIMENSION -> ret[0] = floatValue
        }
    }

    // @TODO: add description
    fun setValue(value: FloatArray) {
        when (type) {
            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_INT -> integerValue =
                value[0].toInt()
            TypedValues.Custom.TYPE_FLOAT, TypedValues.Custom.TYPE_DIMENSION -> floatValue =
                value[0]
            TypedValues.Custom.TYPE_COLOR -> {
                val f_r = value[0]
                val f_g = value[1]
                val f_b = value[2]
                val r = 0xFF and round(pow(f_r, 1.0 / 2.0) * 255.0f).toInt()
                val g = 0xFF and round(pow(f_g, 1.0 / 2.0) * 255.0f).toInt()
                val b = 0xFF and round(pow(f_b, 1.0 / 2.0) * 255.0f).toInt()
                val a = 0xFF and round(value[3] * 255.0f).toInt()
                integerValue = a shl 24 or (r shl 16) or (g shl 8) or b
            }
            TypedValues.Custom.TYPE_STRING -> throw RuntimeException("Cannot interpolate String")
            TypedValues.Custom.TYPE_BOOLEAN -> booleanValue = value[0] > 0.5
        }
    }

    /**
     * test if the two attributes are different
     */
    fun diff(customAttribute: CustomVariable?): Boolean {
        if (customAttribute == null || type != customAttribute.type) {
            return false
        }
        when (type) {
            TypedValues.Custom.TYPE_INT, TypedValues.Custom.TYPE_REFERENCE -> return integerValue == customAttribute.integerValue
            TypedValues.Custom.TYPE_FLOAT -> return floatValue == customAttribute.floatValue
            TypedValues.Custom.TYPE_COLOR -> return integerValue == customAttribute.integerValue
            TypedValues.Custom.TYPE_STRING -> return integerValue == customAttribute.integerValue
            TypedValues.Custom.TYPE_BOOLEAN -> return booleanValue == customAttribute.booleanValue
            TypedValues.Custom.TYPE_DIMENSION -> return floatValue == customAttribute.floatValue
        }
        return false
    }

    constructor(name: String, attributeType: Int) {
        this.name = name
        type = attributeType
    }

    constructor(name: String, attributeType: Int, value: Any?) {
        this.name = name
        type = attributeType
        setValue(value)
    }

    constructor(source: CustomVariable, value: Any?) {
        name = source.name
        type = source.type
        setValue(value)
    }

    // @TODO: add description
    fun setValue(value: Any?) {
        when (type) {
            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_INT -> integerValue =
                value.toString().toInt()
            TypedValues.Custom.TYPE_FLOAT -> floatValue = value.toString().toFloat()
            TypedValues.Custom.TYPE_COLOR -> integerValue = value.toString().toInt()
            TypedValues.Custom.TYPE_STRING -> stringValue = value.toString()
            TypedValues.Custom.TYPE_BOOLEAN -> booleanValue = value.toString().toBoolean()
            TypedValues.Custom.TYPE_DIMENSION -> floatValue = value.toString().toFloat()
        }
    }

    // @TODO: add description
    fun getInterpolatedColor(value: FloatArray): Int {
        val r = clamp(
            (pow(
                value[0], 1.0 / 2.2
            ) * 255.0f).toInt()
        )
        val g = clamp(
            (pow(
                value[1], 1.0 / 2.2
            ) * 255.0f).toInt()
        )
        val b = clamp(
            (pow(
                value[2], 1.0 / 2.2
            ) * 255.0f).toInt()
        )
        val a =
            clamp((value[3] * 255.0f).toInt())
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    // @TODO: add description
    fun setInterpolatedValue(view: MotionWidget, value: FloatArray) {
        when (type) {
            TypedValues.Custom.TYPE_INT -> view.setCustomAttribute(name, type, value[0].toInt())
            TypedValues.Custom.TYPE_COLOR -> {
                val r = clamp(
                    (pow(
                        value[0], 1.0 / 2.2
                    ) * 255.0f).toInt()
                )
                val g = clamp(
                    (pow(
                        value[1], 1.0 / 2.2
                    ) * 255.0f).toInt()
                )
                val b = clamp(
                    (pow(
                        value[2], 1.0 / 2.2
                    ) * 255.0f).toInt()
                )
                val a = clamp((value[3] * 255.0f).toInt())
                val color = a shl 24 or (r shl 16) or (g shl 8) or b
                view.setCustomAttribute(name, type, color)
            }
            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_STRING -> throw RuntimeException(
                "unable to interpolate " + name
            )
            TypedValues.Custom.TYPE_BOOLEAN -> view.setCustomAttribute(name, type, value[0] > 0.5f)
            TypedValues.Custom.TYPE_DIMENSION, TypedValues.Custom.TYPE_FLOAT -> view.setCustomAttribute(
                name, type, value[0]
            )
        }
    }

    // @TODO: add description
    fun applyToWidget(view: MotionWidget) {
        when (type) {
            TypedValues.Custom.TYPE_INT, TypedValues.Custom.TYPE_COLOR, TypedValues.Custom.TYPE_REFERENCE -> view.setCustomAttribute(
                name, type, integerValue
            )
            TypedValues.Custom.TYPE_STRING -> view.setCustomAttribute(name, type, stringValue)
            TypedValues.Custom.TYPE_BOOLEAN -> view.setCustomAttribute(name, type, booleanValue)
            TypedValues.Custom.TYPE_DIMENSION, TypedValues.Custom.TYPE_FLOAT -> view.setCustomAttribute(
                name, type, floatValue
            )
        }
    }

    companion object {
        private const val TAG = "TransitionLayout"

        // @TODO: add description
        fun colorString(v: Int): String {
            val str = "00000000" + Int.toHexString(v)
            return "#" + str.substring(str.length - 8)
        }

        // @TODO: add description
        fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
            val h = (hue * 6).toInt()
            val f = hue * 6 - h
            val p = (0.5f + 255 * value * (1 - saturation)).toInt()
            val q = (0.5f + 255 * value * (1 - f * saturation)).toInt()
            val t = (0.5f + 255 * value * (1 - (1 - f) * saturation)).toInt()
            val v = (0.5f + 255 * value).toInt()
            when (h) {
                0 -> return -0x1000000 or (v shl 16) + (t shl 8) + p
                1 -> return -0x1000000 or (q shl 16) + (v shl 8) + p
                2 -> return -0x1000000 or (p shl 16) + (v shl 8) + t
                3 -> return -0x1000000 or (p shl 16) + (q shl 8) + v
                4 -> return -0x1000000 or (t shl 16) + (p shl 8) + v
                5 -> return -0x1000000 or (v shl 16) + (p shl 8) + q
            }
            return 0
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

        // @TODO: add description
        fun rgbaTocColor(
            r: Float,
            g: Float,
            b: Float,
            a: Float
        ): Int {
            val ir =
                clamp((r * 255f).toInt())
            val ig =
                clamp((g * 255f).toInt())
            val ib =
                clamp((b * 255f).toInt())
            val ia =
                clamp((a * 255f).toInt())
            return ia shl 24 or (ir shl 16) or (ig shl 8) or ib
        }
    }
}