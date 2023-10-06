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
package dev.topping.ios.constraint.core.parser

import dev.topping.ios.constraint.isNaN

class CLNumber : CLElement {
    var mValue = Float.NaN

    constructor(content: CharArray?) : super(content) {}
    constructor(value: Float) : super(null) {
        mValue = value
    }

    
    override fun toJSON(): String {
        val value = float
        val intValue = value.toInt()
        return if (intValue.toFloat() == value) {
            "" + intValue
        } else "" + value
    }

    
    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json = StringBuilder()
        addIndent(json, indent)
        val value = float
        val intValue = value.toInt()
        if (intValue.toFloat() == value) {
            json.append(intValue)
        } else {
            json.append(value)
        }
        return json.toString()
    }

    // @TODO: add description
    val isInt: Boolean
        get() {
            val value = float
            val intValue = value.toInt()
            return intValue.toFloat() == value
        }

    
    fun getInt(): Int {
        if (Float.isNaN(mValue) && hasContent()) {
            // If the value is undefined, attempt to define it from the content
            mValue = content().toFloat()
        }
        return mValue.toInt()
    }

    // If the value is undefined, attempt to define it from the content
    override val float: Float
        get() {
            if (Float.isNaN(mValue) && hasContent()) {
                // If the value is undefined, attempt to define it from the content
                mValue = content().toFloat()
            }
            return mValue
        }

    // @TODO: add description
    fun putValue(value: Float) {
        mValue = value
    }

    override fun equals(other: Any?): Boolean {
        if (this == other) {
            return true
        }
        if (other is CLNumber) {
            val thisFloat = float
            val otherFloat = (other as CLNumber).float
            return if (Float.isNaN(thisFloat) && Float.isNaN(otherFloat)) {
                // Consider equal if both elements have a NaN value
                true
            } else thisFloat == otherFloat
        }
        return false
    }

    
    override fun hashCode(): Int {
        // Auto-generated with Intellij Action "equals() and hashcode()"
        var result = super.hashCode()
        result = 31 * result + if (mValue != 0.0f) mValue.toBits() else 0
        return result
    }

    companion object {
        // @TODO: add description
        fun allocate(content: CharArray?): CLElement {
            return CLNumber(content)
        }
    }
}