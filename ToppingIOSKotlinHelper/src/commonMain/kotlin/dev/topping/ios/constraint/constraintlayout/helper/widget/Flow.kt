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
package dev.topping.ios.constraint.constraintlayout.helper.widget

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.MeasureSpec
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.constraintlayout.widget.VirtualLayout
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.HelperWidget

/**
 *
 * Flow VirtualLayout. **Added in 2.0**
 *
 * Allows positioning of referenced widgets horizontally or vertically, similar to a Chain.
 *
 * The elements referenced are indicated via constraint_referenced_ids, as with other
 * ConstraintHelper implementations.
 *
 * Those referenced widgets are then laid out by the Flow virtual layout in three possible ways:
 *
 *  * [wrap none](#wrap_none) : simply create a chain out of the
 * referenced elements
 *  * [wrap chain](#wrap_chain) : create multiple chains (one after the other)
 * if the referenced elements do not fit
 *  * [wrap aligned](#wrap_aligned) : similar to wrap chain, but will align the
 * elements by creating rows and columns
 *
 *
 * As VirtualLayouts are ConstraintHelpers, they are normal views; you can thus treat them as such,
 * and setting up constraints on them (position, dimension) or some view attributes
 * (background, padding) will work. The main difference between VirtualLayouts and ViewGroups is
 * that:
 *
 *  * VirtualLayout keep the hierarchy flat
 *  * Other views can thus reference / constrain to not only the VirtualLayout, but also
 * the views laid out by the VirtualLayout
 *  * VirtualLayout allow on the fly behavior modifications
 * (e.g. for Flow, changing the orientation)
 *
 *
 * <h4 id="wrap_none">flow_wrapMode = "none"</h4>
 *
 * This will simply create an horizontal or vertical chain out of the referenced widgets.
 * This is the default behavior of Flow.
 *
 * XML attributes that are allowed in this mode:
 *
 *
 *  * flow_horizontalStyle = "spread|spread_inside|packed"
 *  * flow_verticalStyle = "spread|spread_inside|packed"
 *  * flow_horizontalBias = "*float*"
 *  * flow_verticalBias = "*float*"
 *  * flow_horizontalGap = "*dimension*"
 *  * flow_verticalGap = "*dimension*"
 *  * flow_horizontalAlign = "start|end"
 *  * flow_verticalAlign = "top|bottom|center|baseline
 *
 *
 * While the elements are laid out as a chain in the orientation defined, the way they are laid
 * out in the other dimension is controlled
 * by *flow_horizontalAlign* and *flow_verticalAlign* attributes.
 *
 * <h4 id="wrap_chain">flow_wrapMode = "chain"</h4>
 *
 * Similar to wrap none in terms of creating chains, but if the referenced widgets do not fit the
 * horizontal or vertical dimension (depending
 * on the orientation picked), they will wrap around to the next line / column.
 *
 * XML attributes are the same same as in wrap_none, with the addition of attributes specifying
 * chain style and chain bias applied to the first chain. This way, it is possible to specify
 * different chain behavior between the first chain and the rest of the chains eventually created.
 *
 *
 *  * flow_firstHorizontalStyle = "spread|spread_inside|packed"
 *  * flow_firstVerticalStyle = "spread|spread_inside|packed"
 *  * flow_firstHorizontalBias = "*float*"
 *  * flow_firstVerticalBias = "*float*"
 *
 *
 * One last important attribute is *flow_maxElementsWrap*, which specify the number
 * of elements before wrapping, regardless if they
 * fit or not in the available space.
 *
 * <h4 id="wrap_aligned">flow_wrapMode = "aligned"</h4>
 *
 * Same XML attributes as for WRAP_CHAIN, with the difference that the elements are going to be
 * laid out in a set of rows and columns instead of chains.
 * The attribute specifying chains style and bias are thus not going to be applied.
 */
class Flow(context: TContext, attrs: AttributeSet, self: TView) : VirtualLayout(context, attrs, self) {
    private var mFlow: dev.topping.ios.constraint.core.widgets.Flow = dev.topping.ios.constraint.core.widgets.Flow()

    init {
        self.setParentType(this)
        val a = context.getResources()
        attrs.forEach { kvp ->
        val attr = kvp.value
            if(kvp.key == "android_orientation") {
                mFlow.setOrientation(a.getInt(kvp.key, attr, 0))
            } else if(kvp.key == "android_padding") {
                mFlow.setPadding(a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "android_paddingStart") {
                mFlow.setPaddingStart(a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "android_paddingEnd") {
                mFlow.setPaddingEnd(a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "android_paddingLeft") {
                mFlow.paddingLeft = (a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "android_paddingTop") {
                mFlow.paddingTop = (a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "android_paddingRight") {
                mFlow.paddingRight = (a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "android_paddingBottom") {
                mFlow.paddingBottom = (a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "flow_wrapMode") {
                mFlow.setWrapMode(
                    a.getInt(
                        kvp.key,
                        attr,
                        dev.topping.ios.constraint.core.widgets.Flow.WRAP_NONE
                    )
                )
            } else if(kvp.key == "flow_horizontalStyle") {
                mFlow.setHorizontalStyle(a.getInt(kvp.key, attr, 0))
            } else if(kvp.key == "flow_verticalStyle") {
                mFlow.setVerticalStyle(a.getInt(kvp.key, attr, 0))
            } else if(kvp.key == "flow_firstHorizontalStyle") {
                mFlow.setFirstHorizontalStyle(a.getInt(kvp.key, attr, 0))
            } else if(kvp.key == "flow_lastHorizontalStyle") {
                mFlow.setLastHorizontalStyle(a.getInt(kvp.key, attr, 0))
            } else if(kvp.key == "flow_firstVerticalStyle") {
                mFlow.setFirstVerticalStyle(a.getInt(kvp.key, attr, 0))
            } else if(kvp.key == "flow_lastVerticalStyle") {
                mFlow.setLastVerticalStyle(a.getInt(kvp.key, attr, 0))
            } else if(kvp.key == "flow_horizontalBias") {
                mFlow.setHorizontalBias(a.getFloat(kvp.key, attr, 0.5f))
            } else if(kvp.key == "flow_firstHorizontalBias") {
                mFlow.setFirstHorizontalBias(a.getFloat(kvp.key, attr, 0.5f))
            } else if(kvp.key == "flow_lastHorizontalBias") {
                mFlow.setLastHorizontalBias(a.getFloat(kvp.key, attr, 0.5f))
            } else if(kvp.key == "flow_firstVerticalBias") {
                mFlow.setFirstVerticalBias(a.getFloat(kvp.key, attr, 0.5f))
            } else if(kvp.key == "flow_lastVerticalBias") {
                mFlow.setLastVerticalBias(a.getFloat(kvp.key, attr, 0.5f))
            } else if(kvp.key == "flow_verticalBias") {
                mFlow.setVerticalBias(a.getFloat(kvp.key, attr, 0.5f))
            } else if(kvp.key == "flow_horizontalAlign") {
                mFlow.setHorizontalAlign(
                    a.getInt(
                        kvp.key,
                        attr,
                        dev.topping.ios.constraint.core.widgets.Flow.HORIZONTAL_ALIGN_CENTER
                    )
                )
            } else if(kvp.key == "flow_verticalAlign") {
                mFlow.setVerticalAlign(
                    a.getInt(
                        kvp.key,
                        attr,
                        dev.topping.ios.constraint.core.widgets.Flow.VERTICAL_ALIGN_CENTER
                    )
                )
            } else if(kvp.key == "flow_horizontalGap") {
                mFlow.setHorizontalGap(a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "flow_verticalGap") {
                mFlow.setVerticalGap(a.getDimensionPixelSize(attr, 0))
            } else if(kvp.key == "flow_maxElementsWrap") {
                mFlow.setMaxElementsWrap(a.getInt(kvp.key, attr, -1))
            }
        }
        mHelperWidget = mFlow
        validateParams()
    }

    /**
     *
     *
     * @param widget
     * @param isRtl
     */
    override fun resolveRtl(widget: ConstraintWidget, isRtl: Boolean) {
        mFlow.applyRtl(isRtl)
    }

    protected fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onMeasure(mFlow, widthMeasureSpec, heightMeasureSpec)
    }

    /**
     *
     *
     * @param layout
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    override fun onMeasure(
        layout: dev.topping.ios.constraint.core.widgets.VirtualLayout?,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val widthMode: Int = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize: Int = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode: Int = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize: Int = MeasureSpec.getSize(heightMeasureSpec)
        if (layout != null) {
            layout.measure(widthMode, widthSize, heightMode, heightSize)
            self.setMeasuredDimension(layout.measuredWidth, layout.measuredHeight)
        } else {
            self.setMeasuredDimension(0, 0)
        }
    }

    /**
     *
     *
     * @param constraint
     * @param child
     * @param layoutParams
     * @param mapIdToWidget
     */
    override fun loadParameters(
        constraint: ConstraintSet.Constraint,
        child: HelperWidget?,
        layoutParams: ConstraintLayout.LayoutParams?,
        mapIdToWidget: MutableMap<String, ConstraintWidget>
    ) {
        super.loadParameters(constraint, child, layoutParams, mapIdToWidget)
        if(layoutParams == null)
            return
        if (child is dev.topping.ios.constraint.core.widgets.Flow) {
            val flow =
                child as dev.topping.ios.constraint.core.widgets.Flow
            if (layoutParams!!.orientation != -1) {
                flow.setOrientation(layoutParams!!.orientation)
            }
        }
    }

    /**
     * Set the orientation of the layout
     *
     * @param orientation either Flow.HORIZONTAL or FLow.VERTICAL
     */
    fun setOrientation(orientation: Int) {
        mFlow.setOrientation(orientation)
        self.requestLayout()
    }

    /**
     * Set padding around the content
     *
     * @param padding
     */
    fun setPadding(padding: Int) {
        mFlow.setPadding(padding)
        self.requestLayout()
    }

    /**
     * Set padding left around the content
     *
     * @param paddingLeft
     */
    fun setPaddingLeft(paddingLeft: Int) {
        mFlow.paddingLeft = (paddingLeft)
        self.requestLayout()
    }

    /**
     * Set padding top around the content
     *
     * @param paddingTop
     */
    fun setPaddingTop(paddingTop: Int) {
        mFlow.paddingTop = (paddingTop)
        self.requestLayout()
    }

    /**
     * Set padding right around the content
     *
     * @param paddingRight
     */
    fun setPaddingRight(paddingRight: Int) {
        mFlow.paddingRight = (paddingRight)
        self.requestLayout()
    }

    /**
     * Set padding bottom around the content
     *
     * @param paddingBottom
     */
    fun setPaddingBottom(paddingBottom: Int) {
        mFlow.paddingBottom = (paddingBottom)
        self.requestLayout()
    }

    /**
     * Set the style of the last Horizontal column.
     * @param style Flow.CHAIN_SPREAD, Flow.CHAIN_SPREAD_INSIDE, or Flow.CHAIN_PACKED
     */
    fun setLastHorizontalStyle(style: Int) {
        mFlow.setLastHorizontalStyle(style)
        self.requestLayout()
    }

    /**
     * Set the style of the last vertical row.
     * @param style  Flow.CHAIN_SPREAD, Flow.CHAIN_SPREAD_INSIDE, or Flow.CHAIN_PACKED
     */
    fun setLastVerticalStyle(style: Int) {
        mFlow.setLastVerticalStyle(style)
        self.requestLayout()
    }

    /**
     * Set the bias of the last Horizontal column.
     * @param bias
     */
    fun setLastHorizontalBias(bias: Float) {
        mFlow.setLastHorizontalBias(bias)
        self.requestLayout()
    }

    /**
     * Set the bias of the last vertical row.
     * @param bias
     */
    fun setLastVerticalBias(bias: Float) {
        mFlow.setLastVerticalBias(bias)
        self.requestLayout()
    }

    /**
     * Set wrap mode for the layout. Can be:
     *
     * Flow.WRAP_NONE (default) -- no wrap behavior, create a single chain
     * Flow.WRAP_CHAIN -- if not enough space to fit the referenced elements,
     * will create additional chains after the first one
     * Flow.WRAP_ALIGNED -- if not enough space to fit the referenced elements,
     * will wrap the elements, keeping them aligned (like a table)
     *
     * @param mode
     */
    fun setWrapMode(mode: Int) {
        mFlow.setWrapMode(mode)
        self.requestLayout()
    }

    /**
     * Set horizontal chain style. Can be:
     *
     * Flow.CHAIN_SPREAD
     * Flow.CHAIN_SPREAD_INSIDE
     * Flow.CHAIN_PACKED
     *
     * @param style
     */
    fun setHorizontalStyle(style: Int) {
        mFlow.setHorizontalStyle(style)
        self.requestLayout()
    }

    /**
     * Set vertical chain style. Can be:
     *
     * Flow.CHAIN_SPREAD
     * Flow.CHAIN_SPREAD_INSIDE
     * Flow.CHAIN_PACKED
     *
     * @param style
     */
    fun setVerticalStyle(style: Int) {
        mFlow.setVerticalStyle(style)
        self.requestLayout()
    }

    /**
     * Set the horizontal bias applied to the chain
     *
     * @param bias from 0 to 1
     */
    fun setHorizontalBias(bias: Float) {
        mFlow.setHorizontalBias(bias)
        self.requestLayout()
    }

    /**
     * Set the vertical bias applied to the chain
     *
     * @param bias from 0 to 1
     */
    fun setVerticalBias(bias: Float) {
        mFlow.setVerticalBias(bias)
        self.requestLayout()
    }

    /**
     * Similar to setHorizontalStyle(), but only applies to the first chain.
     *
     * @param style
     */
    fun setFirstHorizontalStyle(style: Int) {
        mFlow.setFirstHorizontalStyle(style)
        self.requestLayout()
    }

    /**
     * Similar to setVerticalStyle(), but only applies to the first chain.
     *
     * @param style
     */
    fun setFirstVerticalStyle(style: Int) {
        mFlow.setFirstVerticalStyle(style)
        self.requestLayout()
    }

    /**
     * Similar to setHorizontalBias(), but only applied to the first chain.
     *
     * @param bias
     */
    fun setFirstHorizontalBias(bias: Float) {
        mFlow.setFirstHorizontalBias(bias)
        self.requestLayout()
    }

    /**
     * Similar to setVerticalBias(), but only applied to the first chain.
     *
     * @param bias
     */
    fun setFirstVerticalBias(bias: Float) {
        mFlow.setFirstVerticalBias(bias)
        self.requestLayout()
    }

    /**
     * Set up the horizontal alignment of the elements in the layout,
     * if the layout orientation is set to Flow.VERTICAL
     *
     * Can be either:
     * Flow.HORIZONTAL_ALIGN_START
     * Flow.HORIZONTAL_ALIGN_END
     * Flow.HORIZONTAL_ALIGN_CENTER
     *
     * @param align
     */
    fun setHorizontalAlign(align: Int) {
        mFlow.setHorizontalAlign(align)
        self.requestLayout()
    }

    /**
     * Set up the vertical alignment of the elements in the layout,
     * if the layout orientation is set to Flow.HORIZONTAL
     *
     * Can be either:
     * Flow.VERTICAL_ALIGN_TOP
     * Flow.VERTICAL_ALIGN_BOTTOM
     * Flow.VERTICAL_ALIGN_CENTER
     * Flow.VERTICAL_ALIGN_BASELINE
     *
     * @param align
     */
    fun setVerticalAlign(align: Int) {
        mFlow.setVerticalAlign(align)
        self.requestLayout()
    }

    /**
     * Set up the horizontal gap between elements
     *
     * @param gap
     */
    fun setHorizontalGap(gap: Int) {
        mFlow.setHorizontalGap(gap)
        self.requestLayout()
    }

    /**
     * Set up the vertical gap between elements
     *
     * @param gap
     */
    fun setVerticalGap(gap: Int) {
        mFlow.setVerticalGap(gap)
        self.requestLayout()
    }

    /**
     * Set up the maximum number of elements before wrapping.
     *
     * @param max
     */
    fun setMaxElementsWrap(max: Int) {
        mFlow.setMaxElementsWrap(max)
        self.requestLayout()
    }

    companion object {
        private const val TAG = "Flow"
        val HORIZONTAL: Int = ConstraintWidget.HORIZONTAL
        val VERTICAL: Int = ConstraintWidget.VERTICAL
        val WRAP_NONE: Int = dev.topping.ios.constraint.core.widgets.Flow.WRAP_NONE
        val WRAP_CHAIN: Int = dev.topping.ios.constraint.core.widgets.Flow.WRAP_CHAIN
        val WRAP_ALIGNED: Int = dev.topping.ios.constraint.core.widgets.Flow.WRAP_ALIGNED
        val CHAIN_SPREAD: Int = ConstraintWidget.CHAIN_SPREAD
        val CHAIN_SPREAD_INSIDE: Int = ConstraintWidget.CHAIN_SPREAD_INSIDE
        val CHAIN_PACKED: Int = ConstraintWidget.CHAIN_PACKED
        val HORIZONTAL_ALIGN_START: Int =
            dev.topping.ios.constraint.core.widgets.Flow.HORIZONTAL_ALIGN_START
        val HORIZONTAL_ALIGN_END: Int =
            dev.topping.ios.constraint.core.widgets.Flow.HORIZONTAL_ALIGN_END
        val HORIZONTAL_ALIGN_CENTER: Int =
            dev.topping.ios.constraint.core.widgets.Flow.HORIZONTAL_ALIGN_CENTER
        val VERTICAL_ALIGN_TOP: Int = dev.topping.ios.constraint.core.widgets.Flow.VERTICAL_ALIGN_TOP
        val VERTICAL_ALIGN_BOTTOM: Int =
            dev.topping.ios.constraint.core.widgets.Flow.VERTICAL_ALIGN_BOTTOM
        val VERTICAL_ALIGN_CENTER: Int =
            dev.topping.ios.constraint.core.widgets.Flow.VERTICAL_ALIGN_CENTER
        val VERTICAL_ALIGN_BASELINE: Int =
            dev.topping.ios.constraint.core.widgets.Flow.VERTICAL_ALIGN_BASELINE
    }
}