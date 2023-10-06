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

import dev.topping.ios.constraint.Arrays
import dev.topping.ios.constraint.Cloneable
import dev.topping.ios.constraint.objectsEquals

/**
 * Base element to represent a piece of parsed Json.
 */
open class CLElement(private val mContent: CharArray?) : Cloneable() {
    /**
     * The character index this element was started on
     */
    var start: Long = -1
    protected var mEnd = Long.MAX_VALUE
    protected var mContainer: CLContainer? = null

    /**
     * get the line Number
     *
     * @return return the line number this element was on
     */
    var line = 0

    // @TODO: add description
    fun notStarted(): Boolean {
        return start == -1L
    }

    /**
     * The character index this element was ended on
     */// @TODO: add description
    var end: Long
        get() = mEnd
        set(end) {
            if (mEnd != Long.MAX_VALUE) {
                return
            }
            mEnd = end
            if (CLParser.sDebug) {
                println("closing " + this.hashCode() + " -> " + this)
            }
            mContainer?.add(this)
        }

    protected fun addIndent(builder: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            builder.append(' ')
        }
    }

    
    override fun toString(): String {
        if (start > mEnd || mEnd == Long.MAX_VALUE) {
            return this::class.toString() + " (INVALID, " + start + "-" + mEnd + ")"
        }
        var content = mContent.toString()
        content = content.substring(start.toInt(), mEnd.toInt() + 1)
        return strClass + " (" + start + " : " + mEnd + ") <<" + content + ">>"
    }

    val strClass: String
        get() {
            val myClass: String = this::class.toString()
            return myClass.substring(myClass.lastIndexOf('.') + 1)
        }
    val debugName: String
        get() = if (CLParser.sDebug) {
            "$strClass -> "
        } else ""

    // @TODO: add description
    fun content(): String {
        val content = mContent.toString()
        // Handle empty string
        if (content.isEmpty()) {
            return ""
        }
        return if (mEnd == Long.MAX_VALUE || mEnd < start) {
            content.substring(start.toInt(), start.toInt() + 1)
        } else content.substring(start.toInt(), mEnd.toInt() + 1)
    }

    /**
     * Whether this element has any valid content defined.
     *
     *
     * The content is valid when [.content] can be called without causing exceptions.
     */
    fun hasContent(): Boolean {
        return mContent != null && mContent.size >= 1
    }

    val isDone: Boolean
        get() = mEnd != Long.MAX_VALUE
    var container: CLElement?
        get() = mContainer
        set(element) {
            mContainer = element as CLContainer?
        }
    val isStarted: Boolean
        get() = start > -1

    open fun toJSON(): String {
        return ""
    }

    open fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        return ""
    }

    // @TODO: add description
    val int: Int
        get() = if (this is CLNumber) {
            (this as CLNumber).getInt()
        } else 0

    // @TODO: add description
    open val float: Float
        get() = if (this is CLNumber) {
            this.float
        } else Float.NaN

    
    override fun equals(other: Any?): Boolean {
        if (this == other) return true
        if (other !is CLElement) return false
        val clElement = other as CLElement
        if (start != clElement.start) return false
        if (mEnd != clElement.mEnd) return false
        if (line != clElement.line) return false
        return if (!mContent.contentEquals(clElement.mContent)) false else objectsEquals(
            mContainer,
            clElement.mContainer
        )
    }

    
    override fun hashCode(): Int {
        // Auto-generated with Intellij Action "equals() and hashcode()"
        var result: Int = mContent.contentHashCode()
        result = 31 * result + (start xor (start ushr 32)).toInt()
        result = 31 * result + (mEnd xor (mEnd ushr 32)).toInt()
        result = 31 * result + if (mContainer != null) mContainer.hashCode() else 0
        result = 31 * result + line
        return result
    }

    
    override fun clone(): CLElement {
        return try {
            val clone = super.clone() as CLElement
            if (mContainer != null) {
                clone.mContainer = mContainer!!.clone()
            }
            clone
        } catch (e: Exception) {
            throw AssertionError()
        }
    }

    companion object {
        protected var sMaxLine = 80 // Max number of characters before the formatter indents
        protected var sBaseIndent = 2 // default indentation value
    }
}