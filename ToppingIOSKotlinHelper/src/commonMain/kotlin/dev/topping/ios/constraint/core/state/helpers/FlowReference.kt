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
package dev.topping.ios.constraint.core.state.helpers

import dev.topping.ios.constraint.core.state.HelperReference
import dev.topping.ios.constraint.core.state.State
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.Flow
import dev.topping.ios.constraint.core.widgets.Helper
import dev.topping.ios.constraint.core.widgets.HelperWidget
import dev.topping.ios.constraint.isNaN

/**
 * The FlowReference class can be used to store the relevant properties of a Flow Helper
 * when parsing the Flow Helper information in a JSON representation.
 *
 */
class FlowReference(state: State, type: State.Helper) : HelperReference(state, type) {
    protected var mFlow: Flow? = null
    protected var mMapWeights: MutableMap<String, Float>? = null
    protected var mMapPreMargin: MutableMap<String, Float>? = null
    protected var mMapPostMargin: MutableMap<String, Float>? = null
    /**
     * Get wrap mode
     *
     * @return wrap mode
     */
    /**
     * Set wrap Mode
     *
     * @param wrap wrap Mode
     */
    var wrapMode = Flow.WRAP_NONE
    /**
     * Get vertical style
     *
     * @return vertical style
     */
    /**
     * set vertical style
     *
     * @param verticalStyle Flow vertical style
     */
    var verticalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    /**
     * Get first vertical style
     *
     * @return first vertical style
     */
    /**
     * Set first vertical style
     *
     * @param firstVerticalStyle Flow first vertical style
     */
    var firstVerticalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    /**
     * Get last vertical style
     *
     * @return last vertical style
     */
    /**
     * Set last vertical style
     *
     * @param lastVerticalStyle Flow last vertical style
     */
    var lastVerticalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    /**
     * Get horizontal style
     *
     * @return horizontal style
     */
    /**
     * Set horizontal style
     *
     * @param horizontalStyle Flow horizontal style
     */
    var horizontalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    /**
     * Get first horizontal style
     *
     * @return first horizontal style
     */
    /**
     * Set first horizontal style
     *
     * @param firstHorizontalStyle Flow first horizontal style
     */
    var firstHorizontalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    /**
     * Get last horizontal style
     *
     * @return last horizontal style
     */
    /**
     * Set last horizontal style
     *
     * @param lastHorizontalStyle Flow last horizontal style
     */
    var lastHorizontalStyle: Int = ConstraintWidget.Companion.UNKNOWN
    /**
     * Get vertical align
     * @return vertical align value
     */
    /**
     * Set vertical align
     *
     * @param verticalAlign vertical align value
     */
    var verticalAlign = Flow.HORIZONTAL_ALIGN_CENTER
    /**
     * Get horizontal align
     *
     * @return horizontal align value
     */
    /**
     * Set horizontal align
     *
     * @param horizontalAlign horizontal align value
     */
    var horizontalAlign = Flow.VERTICAL_ALIGN_CENTER
    /**
     * Get vertical gap
     *
     * @return vertical gap value
     */
    /**
     * Set vertical gap
     *
     * @param verticalGap vertical gap value
     */
    var verticalGap = 0
    /**
     * Get horizontal gap
     *
     * @return horizontal gap value
     */
    /**
     * Set horizontal gap
     *
     * @param horizontalGap horizontal gap value
     */
    var horizontalGap = 0
    /**
     * Get paddingLeft
     *
     * @return paddingLeft value
     */
    /**
     * Set paddingLeft
     *
     * @param padding paddingLeft value
     */
    var paddingLeft = 0
    /**
     * Get paddingRight
     *
     * @return paddingRight value
     */
    /**
     * Set paddingRight
     *
     * @param padding paddingRight value
     */
    var paddingRight = 0
    /**
     * Get paddingTop
     *
     * @return paddingTop value
     */
    /**
     * Set paddingTop
     *
     * @param padding paddingTop value
     */
    var paddingTop = 0
    /**
     * Get paddingBottom
     *
     * @return paddingBottom value
     */
    /**
     * Set padding
     *
     * @param padding paddingBottom value
     */
    var paddingBottom = 0
    /**
     * Get max element wrap
     *
     * @return max element wrap value
     */
    /**
     * Set max element wrap
     *
     * @param maxElementsWrap max element wrap value
     */
    var maxElementsWrap: Int = ConstraintWidget.Companion.UNKNOWN
    /**
     * Get the orientation of a Flow
     *
     * @return orientation value
     */
    /**
     * Set the orientation of a Flow
     *
     * @param mOrientation orientation value
     */
    var orientation: Int = ConstraintWidget.Companion.HORIZONTAL
    /**
     * Get first vertical bias
     *
     * @return first vertical bias value
     */
    /**
     * Set first vertical bias
     *
     * @param firstVerticalBias first vertical bias value
     */
    var firstVerticalBias = 0.5f
    /**
     * Get last vertical bias
     *
     * @return last vertical bias
     */
    /**
     * Set last vertical bias
     *
     * @param lastVerticalBias last vertical bias value
     */
    var lastVerticalBias = 0.5f
    /**
     * Get first horizontal bias
     *
     * @return first horizontal bias
     */
    /**
     * Set first horizontal bias
     *
     * @param firstHorizontalBias first horizontal bias value
     */
    var firstHorizontalBias = 0.5f
    /**
     * Get last horizontal bias
     *
     * @return last horizontal bias value
     */
    /**
     * Set last horizontal bias
     *
     * @param lastHorizontalBias last horizontal bias value
     */
    var lastHorizontalBias = 0.5f

    init {
        if (type == State.Helper.VERTICAL_FLOW) {
            orientation = ConstraintWidget.Companion.VERTICAL
        }
    }

    /**
     * Relate widgets to the FlowReference
     *
     * @param id id of a widget
     * @param weight weight of a widget
     * @param preMargin preMargin of a widget
     * @param postMargin postMargin of a widget
     */
    fun addFlowElement(id: String, weight: Float, preMargin: Float, postMargin: Float) {
        super.add(id)
        if (!Float.isNaN(weight)) {
            if (mMapWeights == null) {
                mMapWeights = HashMap()
            }
            mMapWeights!![id] = weight
        }
        if (!Float.isNaN(preMargin)) {
            if (mMapPreMargin == null) {
                mMapPreMargin = HashMap()
            }
            mMapPreMargin!![id] = preMargin
        }
        if (!Float.isNaN(postMargin)) {
            if (mMapPostMargin == null) {
                mMapPostMargin = HashMap()
            }
            mMapPostMargin!![id] = postMargin
        }
    }

    /**
     * Get the weight of a widget
     *
     * @param id id of a widget
     * @return the weight of a widget
     */
    protected fun getWeight(id: String): Float {
        if (mMapWeights == null) {
            return ConstraintWidget.Companion.UNKNOWN.toFloat()
        }
        return if (mMapWeights!!.containsKey(id)) {
            mMapWeights!![id]!!
        } else ConstraintWidget.Companion.UNKNOWN.toFloat()
    }

    /**
     * Get the post margin of a widget
     *
     * @param id id id of a widget
     * @return the post margin of a widget
     */
    protected fun getPostMargin(id: String): Float {
        return if (mMapPreMargin != null && mMapPreMargin!!.containsKey(id)) {
            mMapPreMargin!![id]!!
        } else 0f
    }

    /**
     * Get the pre margin of a widget
     *
     * @param id id id of a widget
     * @return the pre margin of a widget
     */
    protected fun getPreMargin(id: String): Float {
        return if (mMapPostMargin != null && mMapPostMargin!!.containsKey(id)) {
            mMapPostMargin!![id]!!
        } else 0f
    }

    /**
     * Get vertical bias
     *
     * @return vertical bias value
     */
    val verticalBias: Float
        get() = mVerticalBias

    /**
     * Get horizontal bias
     * @return horizontal bias value
     */
    val horizontalBias: Float
        get() = mHorizontalBias

    override var helperWidget: HelperWidget?
        get() {
            if (mFlow == null) {
                mFlow = Flow()
            }
            return mFlow as HelperWidget?
        }
        set(widget) {
            mFlow = if (widget is Flow) {
                widget
            } else {
                null
            }
        }

    
    override fun apply() {
        helperWidget
        this.constraintWidget = mFlow
        mFlow!!.setOrientation(orientation)
        mFlow!!.setWrapMode(wrapMode)
        if (maxElementsWrap != ConstraintWidget.Companion.UNKNOWN) {
            mFlow!!.setMaxElementsWrap(maxElementsWrap)
        }

        // Padding
        if (paddingLeft != 0) {
            mFlow?.mPaddingLeft = paddingLeft
        }
        if (paddingTop != 0) {
            mFlow?.paddingTop = paddingTop
        }
        if (paddingRight != 0) {
            mFlow?.paddingRight = paddingRight
        }
        if (paddingBottom != 0) {
            mFlow?.paddingBottom = paddingBottom
        }

        // Gap
        if (horizontalGap != 0) {
            mFlow!!.setHorizontalGap(horizontalGap)
        }
        if (verticalGap != 0) {
            mFlow!!.setVerticalGap(verticalGap)
        }

        // Bias
        if (mHorizontalBias != 0.5f) {
            mFlow!!.setHorizontalBias(mHorizontalBias)
        }
        if (firstHorizontalBias != 0.5f) {
            mFlow!!.setFirstHorizontalBias(firstHorizontalBias)
        }
        if (lastHorizontalBias != 0.5f) {
            mFlow!!.setLastHorizontalBias(lastHorizontalBias)
        }
        if (mVerticalBias != 0.5f) {
            mFlow!!.setVerticalBias(mVerticalBias)
        }
        if (firstVerticalBias != 0.5f) {
            mFlow!!.setFirstVerticalBias(firstVerticalBias)
        }
        if (lastVerticalBias != 0.5f) {
            mFlow!!.setLastVerticalBias(lastVerticalBias)
        }

        // Align
        if (horizontalAlign != Flow.HORIZONTAL_ALIGN_CENTER) {
            mFlow!!.setHorizontalAlign(horizontalAlign)
        }
        if (verticalAlign != Flow.VERTICAL_ALIGN_CENTER) {
            mFlow!!.setVerticalAlign(verticalAlign)
        }

        // Style
        if (verticalStyle != ConstraintWidget.Companion.UNKNOWN) {
            mFlow!!.setVerticalStyle(verticalStyle)
        }
        if (firstVerticalStyle != ConstraintWidget.Companion.UNKNOWN) {
            mFlow!!.setFirstVerticalStyle(firstVerticalStyle)
        }
        if (lastVerticalStyle != ConstraintWidget.Companion.UNKNOWN) {
            mFlow!!.setLastVerticalStyle(lastVerticalStyle)
        }
        if (horizontalStyle != ConstraintWidget.Companion.UNKNOWN) {
            mFlow!!.setHorizontalStyle(horizontalStyle)
        }
        if (firstHorizontalStyle != ConstraintWidget.Companion.UNKNOWN) {
            mFlow!!.setFirstHorizontalStyle(firstHorizontalStyle)
        }
        if (lastHorizontalStyle != ConstraintWidget.Companion.UNKNOWN) {
            mFlow!!.setLastHorizontalStyle(lastHorizontalStyle)
        }

        // General attributes of a widget
        applyBase()
    }
}