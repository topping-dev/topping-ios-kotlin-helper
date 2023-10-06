/*
 * Copyright (C) 2019 The Android Open Source Project
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
package dev.topping.ios.constraint.core.state.helpers

import dev.topping.ios.constraint.core.state.HelperReference
import dev.topping.ios.constraint.core.state.State
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.isNaN

/**
 * [HelperReference] for Chains.
 *
 * Elements should be added with [ChainReference.addChainElement]
 */
open class ChainReference(state: State, type: State.Helper) :
    HelperReference(state, type) {
    var bias = 0.5f
        protected set

    @Deprecated("Unintended visibility, use {@link #getWeight(String)} instead")
    protected var mMapWeights: HashMap<String, Float> = HashMap()

    @Deprecated("Unintended visibility, use {@link #getPreMargin(String)} instead")
    protected var mMapPreMargin: HashMap<String, Float> = HashMap()

    @Deprecated("Unintended visibility, use {@link #getPostMargin(String)} instead")
    protected var mMapPostMargin: HashMap<String, Float> = HashMap()
    private var mMapPreGoneMargin: HashMap<String, Float>? = null
    private var mMapPostGoneMargin: HashMap<String, Float>? = null

    protected var mStyle = State.Chain.SPREAD

    val style: State.Chain
        get() = State.Chain.SPREAD

    /**
     * Sets the [style][State.Chain].
     *
     * @param style Defines the way the chain will lay out its elements
     * @return This same instance
     */
    fun style(style: State.Chain): ChainReference {
        mStyle = style
        return this
    }

    /**
     * Adds the element by the given id to the Chain.
     *
     * The order in which the elements are added is important. It will represent the element's
     * position in the Chain.
     *
     * @param id         Id of the element to add
     * @param weight     Weight used to distribute remaining space to each element
     * @param preMargin  Additional space in pixels between the added element and the previous one
     * (if any)
     * @param postMargin Additional space in pixels between the added element and the next one (if
     * any)
     */
    fun addChainElement(
        id: String,
        weight: Float,
        preMargin: Float,
        postMargin: Float
    ) {
        addChainElement(id, weight, preMargin, postMargin, 0f, 0f)
    }

    /**
     * Adds the element by the given id to the Chain.
     *
     * The object's [Any.toString] result will be used to map the given margins and
     * weight to it, so it must stable and comparable.
     *
     * The order in which the elements are added is important. It will represent the element's
     * position in the Chain.
     *
     * @param id             Id of the element to add
     * @param weight         Weight used to distribute remaining space to each element
     * @param preMargin      Additional space in pixels between the added element and the
     * previous one
     * (if any)
     * @param postMargin     Additional space in pixels between the added element and the next
     * one (if
     * any)
     * @param preGoneMargin  Additional space in pixels between the added element and the previous
     * one (if any) when the previous element has Gone visibility
     * @param postGoneMargin Additional space in pixels between the added element and the next
     * one (if any) when the next element has Gone visibility
     * @hide
     */
    fun addChainElement(
        id: Any,
        weight: Float,
        preMargin: Float,
        postMargin: Float,
        preGoneMargin: Float,
        postGoneMargin: Float
    ) {
        super.add(id) // Add element id as is, it's expected to return the same given instance
        val idString: String = id.toString()
        if (!Float.isNaN(weight)) {
            mMapWeights[idString] = weight
        }
        if (!Float.isNaN(preMargin)) {
            mMapPreMargin[idString] = preMargin
        }
        if (!Float.isNaN(postMargin)) {
            mMapPostMargin[idString] = postMargin
        }
        if (!Float.isNaN(preGoneMargin)) {
            if (mMapPreGoneMargin == null) {
                mMapPreGoneMargin = HashMap()
            }
            mMapPreGoneMargin!![idString] = preGoneMargin
        }
        if (!Float.isNaN(postGoneMargin)) {
            if (mMapPostGoneMargin == null) {
                mMapPostGoneMargin = HashMap()
            }
            mMapPostGoneMargin!![idString] = postGoneMargin
        }
    }

    protected fun getWeight(id: String): Float {
        return if (mMapWeights.containsKey(id)) {
            mMapWeights[id]!!
        } else ConstraintWidget.Companion.UNKNOWN.toFloat()
    }

    protected fun getPostMargin(id: String): Float {
        return if (mMapPostMargin.containsKey(id)) {
            mMapPostMargin[id]!!
        } else 0f
    }

    protected fun getPreMargin(id: String): Float {
        return if (mMapPreMargin.containsKey(id)) {
            mMapPreMargin[id]!!
        } else 0f
    }

    protected fun getPostGoneMargin(id: String): Float {
        return if (mMapPostGoneMargin != null && mMapPostGoneMargin!!.containsKey(id)) {
            mMapPostGoneMargin!![id]!!
        } else 0f
    }

    protected fun getPreGoneMargin(id: String): Float {
        return if (mMapPreGoneMargin != null && mMapPreGoneMargin!!.containsKey(id)) {
            mMapPreGoneMargin!![id]!!
        } else 0f
    }

    // @TODO: add description
    override fun bias(bias: Float): ChainReference {
        this.bias = bias
        return this
    }
}