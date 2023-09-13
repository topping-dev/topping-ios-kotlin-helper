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

import dev.topping.ios.constraint.objectsHashAll

open class CLContainer(content: CharArray?) : CLElement(content) {
    var mElements: MutableList<CLElement?> = mutableListOf()

    // @TODO: add description
    fun add(element: CLElement?) {
        mElements.add(element)
        if (CLParser.sDebug) {
            println("added element $element to $this")
        }
    }

    
    override fun toString(): String {
        val list = StringBuilder()
        for (element in mElements) {
            if (list.length > 0) {
                list.append("; ")
            }
            list.append(element)
        }
        return super.toString() + " = <" + list + " >"
    }

    // @TODO: add description
    fun size(): Int {
        return mElements.size
    }

    // @TODO: add description
    fun names(): ArrayList<String> {
        val names: ArrayList<String> = ArrayList()
        for (element in mElements) {
            if (element is CLKey) {
                val key: CLKey = element as CLKey
                names.add(key.content())
            }
        }
        return names
    }

    // @TODO: add description
    fun has(name: String?): Boolean {
        for (element in mElements) {
            if (element is CLKey) {
                val key: CLKey = element as CLKey
                if (key.content().equals(name)) {
                    return true
                }
            }
        }
        return false
    }

    // @TODO: add description
    fun put(name: String, value: CLElement?) {
        for (element in mElements) {
            val key: CLKey = element as CLKey
            if (key.content().equals(name)) {
                key.set(value)
                return
            }
        }
        val key: CLKey = CLKey.allocate(name, value) as CLKey
        mElements.add(key)
    }

    // @TODO: add description
    fun putNumber(name: String, value: Float) {
        put(name, CLNumber(value))
    }

    fun putString(name: String, value: String) {
        val stringElement: CLElement = CLString(value.toCharArray())
        stringElement.start = 0L
        stringElement.end = (value.length - 1).toLong()
        put(name, stringElement)
    }

    // @TODO: add description
    fun remove(name: String?) {
        val toRemove: ArrayList<CLElement> = ArrayList()
        for (element in mElements) {
            val key: CLKey = element as CLKey
            if (key.content().equals(name)) {
                toRemove.add(element)
            }
        }
        for (element in toRemove) {
            mElements.remove(element)
        }
    }

    fun clear() {
        mElements.clear()
    }

    /////////////////////////////////////////////////////////////////////////
    // By name
    /////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    @Throws(CLParsingException::class)
    operator fun get(name: String): CLElement? {
        for (element in mElements) {
            val key: CLKey = element as CLKey
            if (key.content() == name) {
                return key.value
            }
        }
        throw CLParsingException("no element for key <$name>", this)
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getInt(name: String): Int {
        val element: CLElement? = get(name)
        if (element != null) {
            return element.int
        }
        throw CLParsingException(
            "no int found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getFloat(name: String): Float {
        val element: CLElement? = get(name)
        if (element != null) {
            return element.float
        }
        throw CLParsingException(
            "no float found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getArray(name: String): CLArray {
        val element: CLElement? = get(name)
        if (element is CLArray) {
            return element as CLArray
        }
        throw CLParsingException(
            "no array found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getObject(name: String): CLObject {
        val element: CLElement? = get(name)
        if (element is CLObject) {
            return element as CLObject
        }
        throw CLParsingException(
            "no object found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getString(name: String): String {
        val element: CLElement? = get(name)
        if (element is CLString) {
            return element.content()
        }
        var strClass: String? = null
        if (element != null) {
            strClass = element.strClass
        }
        throw CLParsingException(
            "no string found for key <" + name + ">,"
                    + " found [" + strClass + "] : " + element, this
        )
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getBoolean(name: String): Boolean {
        val element: CLElement? = get(name)
        if (element is CLToken) {
            return element.boolean
        }
        throw CLParsingException(
            "no boolean found for key <" + name + ">,"
                    + " found [" + element?.strClass + "] : " + element, this
        )
    }

    /////////////////////////////////////////////////////////////////////////
    // Optional
    /////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    fun getOrNull(name: String?): CLElement? {
        for (element in mElements) {
            val key: CLKey = element as CLKey
            if (key.content().equals(name)) {
                return key.value
            }
        }
        return null
    }

    // @TODO: add description
    fun getObjectOrNull(name: String?): CLObject? {
        val element: CLElement? = getOrNull(name)
        return if (element is CLObject) {
            element as CLObject?
        } else null
    }

    // @TODO: add description
    fun getArrayOrNull(name: String?): CLArray? {
        val element: CLElement? = getOrNull(name)
        return if (element is CLArray) {
            element as CLArray?
        } else null
    }

    fun getArrayOrCreate(name: String): CLArray {
        var array: CLArray? = getArrayOrNull(name)
        if (array != null) {
            return array
        }
        array = CLArray(charArrayOf())
        put(name, array)
        return array
    }

    // @TODO: add description
    fun getStringOrNull(name: String?): String? {
        val element: CLElement? = getOrNull(name)
        return if (element is CLString) {
            element.content()
        } else null
    }

    // @TODO: add description
    fun getFloatOrNaN(name: String?): Float {
        val element: CLElement? = getOrNull(name)
        return if (element is CLNumber) {
            element.float
        } else Float.NaN
    }

    /////////////////////////////////////////////////////////////////////////
    // By index
    /////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    @Throws(CLParsingException::class)
    operator fun get(index: Int): CLElement? {
        if (index >= 0 && index < mElements.size) {
            return mElements[index]
        }
        throw CLParsingException("no element at index $index", this)
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getInt(index: Int): Int {
        val element = get(index)
        if (element != null) {
            return element.int
        }
        throw CLParsingException("no int at index $index", this)
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getFloat(index: Int): Float {
        val element = get(index)
        if (element != null) {
            return element.float
        }
        throw CLParsingException("no float at index $index", this)
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getArray(index: Int): CLArray {
        val element = get(index)
        if (element is CLArray) {
            return element as CLArray
        }
        throw CLParsingException("no array at index $index", this)
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getObject(index: Int): CLObject {
        val element = get(index)
        if (element is CLObject) {
            return element
        }
        throw CLParsingException("no object at index $index", this)
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getString(index: Int): String {
        val element = get(index)
        if (element is CLString) {
            return element.content()
        }
        throw CLParsingException("no string at index $index", this)
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun getBoolean(index: Int): Boolean {
        val element = get(index)
        if (element is CLToken) {
            return element.boolean
        }
        throw CLParsingException("no boolean at index $index", this)
    }

    /////////////////////////////////////////////////////////////////////////
    // Optional
    /////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    fun getOrNull(index: Int): CLElement? {
        return if (index >= 0 && index < mElements.size) {
            mElements[index]
        } else null
    }

    // @TODO: add description
    fun getStringOrNull(index: Int): String? {
        val element: CLElement? = getOrNull(index)
        return if (element is CLString) {
            element.content()
        } else null
    }

    
    override fun clone(): CLContainer {
        val clone = super.clone() as CLContainer
        val clonedArray: ArrayList<CLElement?> = ArrayList(mElements.size)
        for (element in mElements) {
            clonedArray.add(element?.clone())
        }
        clone.mElements = clonedArray
        return clone
    }

    
    override fun equals(other: Any?): Boolean {
        if (this == other) {
            return true
        }
        return if (other !is CLContainer) {
            false
        } else mElements.equals(
            other.mElements
        )
    }

    
    override fun hashCode(): Int {
        return objectsHashAll(mElements, super.hashCode())
    }

    companion object {
        // @TODO: add description
        fun allocate(content: CharArray?): CLElement {
            return CLContainer(content)
        }
    }
}