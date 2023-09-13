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

import dev.topping.ios.constraint.Arrays
import dev.topping.ios.constraint.isNaN

class Ref {
    /**
     * Get the Id of the reference
     *
     * @return the Id of the reference
     */
    /**
     * Set the Id of the reference
     *
     * @param id
     */
    var id: String?
    /**
     * Get the weight of the reference
     *
     * @return the weight of the reference
     */
    /**
     * Set the weight of the reference
     *
     * @param weight
     */
    var weight = Float.NaN
    /**
     * Get the preMargin of the reference
     *
     * @return the preMargin of the reference
     */
    /**
     * Set the preMargin of the reference
     *
     * @param preMargin
     */
    var preMargin = Float.NaN
    /**
     * Get the postMargin of the reference
     *
     * @return the preMargin of the reference
     */
    /**
     * Set the postMargin of the reference
     *
     * @param postMargin
     */
    var postMargin = Float.NaN

    internal constructor(id: String?) {
        this.id = id
    }

    internal constructor(id: String?, weight: Float) {
        this.id = id
        this.weight = weight
    }

    internal constructor(id: String?, weight: Float, preMargin: Float) {
        this.id = id
        this.weight = weight
        this.preMargin = preMargin
    }

    internal constructor(id: String?, weight: Float, preMargin: Float, postMargin: Float) {
        this.id = id
        this.weight = weight
        this.preMargin = preMargin
        this.postMargin = postMargin
    }

    override fun toString(): String {
        if (id.isNullOrEmpty()) {
            return ""
        }
        val ret = StringBuilder()
        var isArray = false
        if (!Float.isNaN(weight) || !Float.isNaN(preMargin)
            || !Float.isNaN(postMargin)
        ) {
            isArray = true
        }
        if (isArray) {
            ret.append("[")
        }
        ret.append("'").append(id).append("'")
        if (!Float.isNaN(postMargin)) {
            ret.append(",").append(if (!Float.isNaN(weight)) weight else 0).append(",")
            ret.append(if (!Float.isNaN(preMargin)) preMargin else 0).append(",")
            ret.append(postMargin)
        } else if (!Float.isNaN(preMargin)) {
            ret.append(",").append(if (!Float.isNaN(weight)) weight else 0).append(",")
            ret.append(preMargin)
        } else if (!Float.isNaN(weight)) {
            ret.append(",").append(weight)
        }
        if (isArray) {
            ret.append("]")
        }
        ret.append(",")
        return ret.toString()
    }

    companion object {
        /**
         * Try to parse an object into a float number
         *
         * @param obj object to be parsed
         * @return a number
         */
        fun parseFloat(obj: Any?): Float {
            var `val` = Float.NaN
            try {
                `val` = obj.toString().toFloat()
            } catch (e: Exception) {
                // ignore
            }
            return `val`
        }

        fun parseStringToRef(str: String): Ref? {
            val values: List<String> = str.replace("[\\[\\]\\']", "").split(",")
            if (values.isEmpty()) {
                return null
            }
            val arr: Array<Any?> = arrayOfNulls<Any>(4)
            for (i in values.indices) {
                if (i >= 4) {
                    break
                }
                arr[i] = values[i]
            }
            return Ref(
                arr[0].toString().replace("'", ""), parseFloat(
                    arr[1]
                ),
                parseFloat(arr[2]), parseFloat(
                    arr[3]
                )
            )
        }

        /**
         * Add references in a String representation to a Ref ArrayList
         * Used to add the Ref(s) property in the Config to references
         *
         * @param str references in a String representation
         * @param refs  a Ref ArrayList
         */
        fun addStringToReferences(str: String?, refs: MutableList<Ref?>) {
            if (str.isNullOrEmpty()) {
                return
            }
            val arr: Array<Any?> = arrayOfNulls<Any>(4)
            val builder = StringBuilder()
            var squareBrackets = 0
            var varCount = 0
            var ch: Char
            for (i in 0 until str.length) {
                ch = str[i]
                when (ch) {
                    '[' -> squareBrackets++
                    ']' -> if (squareBrackets > 0) {
                        squareBrackets--
                        arr[varCount] = builder.toString()
                        builder.setLength(0)
                        if (arr[0] != null) {
                            refs.add(
                                Ref(
                                    arr[0].toString(), parseFloat(
                                        arr[1]
                                    ),
                                    parseFloat(arr[2]), parseFloat(
                                        arr[3]
                                    )
                                )
                            )
                            varCount = 0
                            Arrays.fill(arr, null)
                        }
                    }
                    ',' -> {
                        // deal with the first 3 values in the nested array,
                        // the fourth value (postMargin) would be handled at case ']'
                        if (varCount < 3) {
                            arr[varCount++] = builder.toString()
                            builder.setLength(0)
                        }
                        // squareBrackets == 1 indicate the value is not in a nested array.
                        if (squareBrackets == 1 && arr[0] != null) {
                            refs.add(Ref(arr[0].toString()))
                            varCount = 0
                            arr[0] = null
                        }
                    }
                    ' ', '\'' -> {}
                    else -> builder.append(ch)
                }
            }
        }
    }
}