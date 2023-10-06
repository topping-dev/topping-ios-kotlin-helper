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

abstract class Chain(name: String) :
    Helper(name, HelperType("")) {
    enum class Style {
        PACKED, SPREAD, SPREAD_INSIDE
    }

    private var mStyle: Style? = null
    protected var references: MutableList<Ref?> = mutableListOf()
    var style: Style?
        get() = mStyle
        set(style) {
            mStyle = style
            configMap?.put("style", styleMap[style]!!)
        }

    /**
     * convert references to a String representation
     *
     * @return a String representation of references
     */
    fun referencesToString(): String {
        if (references.isEmpty()) {
            return ""
        }
        val builder = StringBuilder("[")
        for (ref in references) {
            builder.append(ref.toString())
        }
        builder.append("]")
        return builder.toString()
    }

    /**
     * Add a new reference
     *
     * @param ref reference
     * @return Chain
     */
    fun addReference(ref: Ref?): Chain {
        references.add(ref)
        configMap?.put("contains", referencesToString())
        return this
    }

    /**
     * Add a new reference
     *
     * @param ref reference in a String representation
     * @return Chain
     */
    fun addReference(ref: String): Chain {
        return addReference(Ref.Companion.parseStringToRef(ref))
    }

    open inner class Anchor internal constructor(side: Constraint.Side) {
        val mSide: Constraint.Side
        var mConnection: Constraint.Anchor? = null
        var mMargin = 0
        var mGoneMargin: Int = Int.MIN_VALUE

        init {
            mSide = side
        }

        fun getId(): String {
            return this@Chain.name
        }

        fun build(builder: StringBuilder) {
            if (mConnection != null) {
                builder.append(mSide.toString().lowercase())
                    .append(":").append(this).append(",\n")
            }
        }

        override fun toString(): String {
            val ret = StringBuilder("[")
            if (mConnection != null) {
                ret.append("'").append(mConnection!!.getId()).append("',")
                    .append("'").append(mConnection!!.mSide.toString().lowercase()).append("'")
            }
            if (mMargin != 0) {
                ret.append(",").append(mMargin)
            }
            if (mGoneMargin != Int.MIN_VALUE) {
                if (mMargin == 0) {
                    ret.append(",0,").append(mGoneMargin)
                } else {
                    ret.append(",").append(mGoneMargin)
                }
            }
            ret.append("]")
            return ret.toString()
        }
    }

    companion object {
        protected val styleMap: MutableMap<Style?, String> = mutableMapOf()

        init {
            styleMap[Style.SPREAD] = "'spread'"
            styleMap[Style.SPREAD_INSIDE] = "'spread_inside'"
            styleMap[Style.PACKED] = "'packed'"
        }
    }
}