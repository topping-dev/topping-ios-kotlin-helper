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
package dev.topping.ios.constraint.core

import dev.topping.ios.constraint.Arrays

/**
 * Store a set of variables and their values in an array-based linked list coupled
 * with a custom hashmap.
 */
class SolverVariableValues internal constructor(row: ArrayRow, cache: Cache) :
    ArrayRow.ArrayRowVariables {
    private val mNone = -1
    private var mSize = 16
    private val mHashSize = 16
    var mKeys = IntArray(mSize)
    var mNextKeys = IntArray(mSize)
    var mVariables = IntArray(mSize)
    var mValues = FloatArray(mSize)
    var mPrevious = IntArray(mSize)
    var mNext = IntArray(mSize)
    
    override var currentSize = 0
    var mHead = -1
    private val mRow // our owner
            : ArrayRow

    // pointer to the system-wide cache, allowing access to SolverVariables
    protected val mCache: Cache

    init {
        mRow = row
        mCache = cache
        clear()
    }

    
    override fun getVariable(index: Int): SolverVariable? {
        val count = currentSize
        if (count == 0) {
            return null
        }
        var j = mHead
        for (i in 0 until count) {
            if (i == index && j != mNone) {
                return mCache.mIndexedVariables.get(mVariables[j])
            }
            j = mNext[j]
            if (j == mNone) {
                break
            }
        }
        return null
    }

    
    override fun getVariableValue(index: Int): Float {
        val count = currentSize
        var j = mHead
        for (i in 0 until count) {
            if (i == index) {
                return mValues[j]
            }
            j = mNext[j]
            if (j == mNone) {
                break
            }
        }
        return 0f
    }

    
    override operator fun contains(variable: SolverVariable?): Boolean {
        return indexOf(variable) != mNone
    }

    
    override fun indexOf(variable: SolverVariable?): Int {
        if (currentSize == 0 || variable == null) {
            return mNone
        }
        val id: Int = variable.id
        var key = id % mHashSize
        key = mKeys[key]
        if (key == mNone) {
            return mNone
        }
        if (mVariables[key] == id) {
            return key
        }
        while (mNextKeys[key] != mNone && mVariables[mNextKeys[key]] != id) {
            key = mNextKeys[key]
        }
        if (mNextKeys[key] == mNone) {
            return mNone
        }
        return if (mVariables[mNextKeys[key]] == id) {
            mNextKeys[key]
        } else mNone
    }

    
    override operator fun get(variable: SolverVariable?): Float {
        val index = indexOf(variable)
        return if (index != mNone) {
            mValues[index]
        } else 0f
    }

    
    override fun display() {
        val count = currentSize
        print("{ ")
        for (i in 0 until count) {
            val v: SolverVariable = getVariable(i) ?: continue
            print(v.toString() + " = " + getVariableValue(i) + " ")
        }
        println(" }")
    }

    
    override fun toString(): String {
        var str: String = hashCode().toString() + " { "
        val count = currentSize
        for (i in 0 until count) {
            val v: SolverVariable = getVariable(i) ?: continue
            str += v.toString() + " = " + getVariableValue(i) + " "
            val index = indexOf(v)
            str += "[Pointer: "
            if (mPrevious[index] != mNone) {
                str += mCache.mIndexedVariables.get(mVariables[mPrevious[index]])
            } else {
                str += "none"
            }
            str += ", n: "
            if (mNext[index] != mNone) {
                str += mCache.mIndexedVariables.get(mVariables[mNext[index]])
            } else {
                str += "none"
            }
            str += "]"
        }
        str += " }"
        return str
    }

    
    override fun clear() {
        if (I_DEBUG) {
            println("$this <clear>")
        }
        val count = currentSize
        for (i in 0 until count) {
            val v: SolverVariable? = getVariable(i)
            if (v != null) {
                v.removeFromRow(mRow)
            }
        }
        for (i in 0 until mSize) {
            mVariables[i] = mNone
            mNextKeys[i] = mNone
        }
        for (i in 0 until mHashSize) {
            mKeys[i] = mNone
        }
        currentSize = 0
        mHead = -1
    }

    private fun increaseSize() {
        val size = mSize * 2
        mVariables = Arrays.copyOf(mVariables, size)
        mValues = Arrays.copyOf(mValues, size)
        mPrevious = Arrays.copyOf(mPrevious, size)
        mNext = Arrays.copyOf(mNext, size)
        mNextKeys = Arrays.copyOf(mNextKeys, size)
        for (i in mSize until size) {
            mVariables[i] = mNone
            mNextKeys[i] = mNone
        }
        mSize = size
    }

    private fun addToHashMap(variable: SolverVariable?, index: Int) {
        if (I_DEBUG) {
            println(this.hashCode().toString() + " hash add " + variable!!.id + " @ " + index)
        }
        val hash: Int = variable!!.id % mHashSize
        var key = mKeys[hash]
        if (key == mNone) {
            mKeys[hash] = index
            if (I_DEBUG) {
                println(
                    this.hashCode().toString() + " hash add "
                            + variable.id + " @ " + index + " directly on keys " + hash
                )
            }
        } else {
            while (mNextKeys[key] != mNone) {
                key = mNextKeys[key]
            }
            mNextKeys[key] = index
            if (I_DEBUG) {
                println(
                    this.hashCode().toString() + " hash add "
                            + variable.id + " @ " + index + " as nextkey of " + key
                )
            }
        }
        mNextKeys[index] = mNone
        if (I_DEBUG) {
            displayHash()
        }
    }

    private fun displayHash() {
        for (i in 0 until mHashSize) {
            if (mKeys[i] != mNone) {
                var str: String = this.hashCode().toString() + " hash [" + i + "] => "
                var key = mKeys[i]
                var done = false
                while (!done) {
                    str += " " + mVariables[key]
                    if (mNextKeys[key] != mNone) {
                        key = mNextKeys[key]
                    } else {
                        done = true
                    }
                }
                println(str)
            }
        }
    }

    private fun removeFromHashMap(variable: SolverVariable?) {
        if (I_DEBUG) {
            println(this.hashCode().toString() + " hash remove " + variable!!.id)
        }
        val hash: Int = variable!!.id % mHashSize
        var key = mKeys[hash]
        if (key == mNone) {
            if (I_DEBUG) {
                displayHash()
            }
            return
        }
        val id: Int = variable.id
        // let's first find it
        if (mVariables[key] == id) {
            mKeys[hash] = mNextKeys[key]
            mNextKeys[key] = mNone
        } else {
            while (mNextKeys[key] != mNone && mVariables[mNextKeys[key]] != id) {
                key = mNextKeys[key]
            }
            val currentKey = mNextKeys[key]
            if (currentKey != mNone && mVariables[currentKey] == id) {
                mNextKeys[key] = mNextKeys[currentKey]
                mNextKeys[currentKey] = mNone
            }
        }
        if (I_DEBUG) {
            displayHash()
        }
    }

    private fun addVariable(index: Int, variable: SolverVariable?, value: Float) {
        mVariables[index] = variable!!.id
        mValues[index] = value
        mPrevious[index] = mNone
        mNext[index] = mNone
        variable.addToRow(mRow)
        variable.usageInRowCount++
        currentSize++
    }

    private fun findEmptySlot(): Int {
        for (i in 0 until mSize) {
            if (mVariables[i] == mNone) {
                return i
            }
        }
        return -1
    }

    private fun insertVariable(index: Int, variable: SolverVariable?, value: Float) {
        val availableSlot = findEmptySlot()
        addVariable(availableSlot, variable, value)
        if (index != mNone) {
            mPrevious[availableSlot] = index
            mNext[availableSlot] = mNext[index]
            mNext[index] = availableSlot
        } else {
            mPrevious[availableSlot] = mNone
            if (currentSize > 0) {
                mNext[availableSlot] = mHead
                mHead = availableSlot
            } else {
                mNext[availableSlot] = mNone
            }
        }
        if (mNext[availableSlot] != mNone) {
            mPrevious[mNext[availableSlot]] = availableSlot
        }
        addToHashMap(variable, availableSlot)
    }

    
    override fun put(variable: SolverVariable?, value: Float) {
        if (I_DEBUG) {
            println(this.toString() + " <put> " + variable!!.id + " = " + value)
        }
        if (value > -sEpsilon && value < sEpsilon) {
            remove(variable, true)
            return
        }
        if (currentSize == 0) {
            addVariable(0, variable, value)
            addToHashMap(variable, 0)
            mHead = 0
        } else {
            val index = indexOf(variable)
            if (index != mNone) {
                mValues[index] = value
            } else {
                if (currentSize + 1 >= mSize) {
                    increaseSize()
                }
                val count = currentSize
                var previousItem = -1
                var j = mHead
                for (i in 0 until count) {
                    if (mVariables[j] == variable!!.id) {
                        mValues[j] = value
                        return
                    }
                    if (mVariables[j] < variable.id) {
                        previousItem = j
                    }
                    j = mNext[j]
                    if (j == mNone) {
                        break
                    }
                }
                insertVariable(previousItem, variable, value)
            }
        }
    }

    
    override fun sizeInBytes(): Int {
        return 0
    }

    
    override fun remove(v: SolverVariable?, removeFromDefinition: Boolean): Float {
        if (I_DEBUG) {
            println(this.toString() + " <remove> " + v!!.id)
        }
        val index = indexOf(v)
        if (index == mNone) {
            return 0f
        }
        removeFromHashMap(v)
        val value = mValues[index]
        if (mHead == index) {
            mHead = mNext[index]
        }
        mVariables[index] = mNone
        if (mPrevious[index] != mNone) {
            mNext[mPrevious[index]] = mNext[index]
        }
        if (mNext[index] != mNone) {
            mPrevious[mNext[index]] = mPrevious[index]
        }
        currentSize--
        v!!.usageInRowCount--
        if (removeFromDefinition) {
            v.removeFromRow(mRow)
        }
        return value
    }

    
    override fun add(v: SolverVariable?, value: Float, removeFromDefinition: Boolean) {
        if (I_DEBUG) {
            println(this.toString() + " <add> " + v!!.id + " = " + value)
        }
        if (value > -sEpsilon && value < sEpsilon) {
            return
        }
        val index = indexOf(v)
        if (index == mNone) {
            put(v, value)
        } else {
            mValues[index] += value
            if (mValues[index] > -sEpsilon && mValues[index] < sEpsilon) {
                mValues[index] = 0f
                remove(v, removeFromDefinition)
            }
        }
    }

    
    override fun use(definition: ArrayRow?, removeFromDefinition: Boolean): Float {
        if(definition == null)
            return 0f
        val value = get(definition.mVariable)
        remove(definition.mVariable, removeFromDefinition)
        if (false) {
            val definitionVariables: ArrayRow.ArrayRowVariables? = definition.variables
            val definitionSize: Int = definitionVariables?.currentSize ?: 0
            for (i in 0 until definitionSize) {
                val definitionVariable: SolverVariable? = definitionVariables!!.getVariable(i)
                val definitionValue: Float = definitionVariables.get(definitionVariable)
                add(definitionVariable, definitionValue * value, removeFromDefinition)
            }
            return value
        }
        val localDef = definition.variables as SolverVariableValues
        val definitionSize = localDef.currentSize
        var j = localDef.mHead
        if (false) {
            for (i in 0 until definitionSize) {
                val definitionValue = localDef.mValues[j]
                val definitionVariable: SolverVariable? =
                    mCache.mIndexedVariables.get(localDef.mVariables[j])
                add(definitionVariable, definitionValue * value, removeFromDefinition)
                j = localDef.mNext[j]
                if (j == mNone) {
                    break
                }
            }
        } else {
            j = 0
            var i = 0
            while (j < definitionSize) {
                if (localDef.mVariables[i] != mNone) {
                    val definitionValue = localDef.mValues[i]
                    val definitionVariable: SolverVariable? = mCache.mIndexedVariables.get(
                        localDef.mVariables[i]
                    )
                    add(definitionVariable, definitionValue * value, removeFromDefinition)
                    j++
                }
                i++
            }
        }
        return value
    }

    
    override fun invert() {
        val count = currentSize
        var j = mHead
        for (i in 0 until count) {
            mValues[j] *= -1f
            j = mNext[j]
            if (j == mNone) {
                break
            }
        }
    }

    
    override fun divideByAmount(amount: Float) {
        val count = currentSize
        var j = mHead
        for (i in 0 until count) {
            mValues[j] /= amount
            j = mNext[j]
            if (j == mNone) {
                break
            }
        }
    }

    companion object {
        private const val I_DEBUG = false

        
        private val HASH = true
        private const val sEpsilon = 0.001f
    }
}