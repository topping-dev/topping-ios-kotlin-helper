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
package dev.topping.ios.constraint.core.widgets

import dev.topping.ios.constraint.core.widgets.analyzer.BasicMeasure

/**
 * Base class for Virtual layouts
 */
open class VirtualLayout : HelperWidget() {
    var paddingTop = 0
    var paddingBottom = 0


    var mPaddingLeft = 0


    private var mPaddingRight = 0

    
    private var mPaddingStart = 0
    private var mPaddingEnd = 0
    private var mResolvedPaddingLeft = 0
    private var mResolvedPaddingRight = 0
    private var mNeedsCallFromSolver = false
    var measuredWidth = 0
        private set
    var measuredHeight = 0
        private set
    protected var mMeasure: BasicMeasure.Measure = BasicMeasure.Measure()

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Accessors
    /////////////////////////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    fun setPadding(value: Int) {
        mPaddingLeft = value
        paddingTop = value
        mPaddingRight = value
        paddingBottom = value
        mPaddingStart = value
        mPaddingEnd = value
    }

    // @TODO: add description
    fun setPaddingStart(value: Int) {
        mPaddingStart = value
        mResolvedPaddingLeft = value
        mResolvedPaddingRight = value
    }

    fun setPaddingEnd(value: Int) {
        mPaddingEnd = value
    }

    // @TODO: add description
    fun applyRtl(isRtl: Boolean) {
        if (mPaddingStart > 0 || mPaddingEnd > 0) {
            if (isRtl) {
                mResolvedPaddingLeft = mPaddingEnd
                mResolvedPaddingRight = mPaddingStart
            } else {
                mResolvedPaddingLeft = mPaddingStart
                mResolvedPaddingRight = mPaddingEnd
            }
        }
    }

    // @TODO: add description
    var paddingLeft: Int
        get() = mResolvedPaddingLeft
        set(value) {
            mPaddingLeft = value
            mResolvedPaddingLeft = value
        }

    // @TODO: add description
    var paddingRight: Int
        get() = mResolvedPaddingRight
        set(value) {
            mPaddingRight = value
            mResolvedPaddingRight = value
        }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Solver callback
    /////////////////////////////////////////////////////////////////////////////////////////////
    protected fun needsCallbackFromSolver(value: Boolean) {
        mNeedsCallFromSolver = value
    }

    // @TODO: add description
    fun needSolverPass(): Boolean {
        return mNeedsCallFromSolver
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Measure
    /////////////////////////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    open fun measure(widthMode: Int, widthSize: Int, heightMode: Int, heightSize: Int) {
        // nothing
    }

    
    override fun updateConstraints(container: ConstraintWidgetContainer?) {
        captureWidgets()
    }

    // @TODO: add description
    fun captureWidgets() {
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets.get(i)
            widget?.setInVirtualLayout(true)
        }
    }

    // @TODO: add description
    fun setMeasure(width: Int, height: Int) {
        measuredWidth = width
        measuredHeight = height
    }

    protected fun measureChildren(): Boolean {
        var measurer: BasicMeasure.Measurer? = null
        if (getParent() != null) {
            measurer = (getParent() as ConstraintWidgetContainer).measurer
        }
        if (measurer == null) {
            return false
        }
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget = mWidgets.get(i) ?: continue
            if (widget is Guideline) {
                continue
            }
            var widthBehavior: DimensionBehaviour? = widget.getDimensionBehaviour(HORIZONTAL)
            var heightBehavior: DimensionBehaviour? = widget.getDimensionBehaviour(VERTICAL)
            val skip =
                widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT && widget.mMatchConstraintDefaultWidth != MATCH_CONSTRAINT_WRAP && heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT && widget.mMatchConstraintDefaultHeight != MATCH_CONSTRAINT_WRAP
            if (skip) {
                // we don't need to measure here as the dimension of the widget
                // will be completely computed by the solver.
                continue
            }
            if (widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                widthBehavior = DimensionBehaviour.WRAP_CONTENT
            }
            if (heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                heightBehavior = DimensionBehaviour.WRAP_CONTENT
            }
            mMeasure.horizontalBehavior = widthBehavior
            mMeasure.verticalBehavior = heightBehavior
            mMeasure.horizontalDimension = widget.width
            mMeasure.verticalDimension = widget.height
            measurer.measure(widget, mMeasure)
            widget.width = mMeasure.measuredWidth
            widget.height = mMeasure.measuredHeight
            widget.setBaselineDistance(mMeasure.measuredBaseline)
        }
        return true
    }

    var mMeasurer: BasicMeasure.Measurer? = null
    protected fun measure(
        widget: ConstraintWidget,
        horizontalBehavior: ConstraintWidget.DimensionBehaviour,
        horizontalDimension: Int,
        verticalBehavior: ConstraintWidget.DimensionBehaviour,
        verticalDimension: Int
    ) {
        while (mMeasurer == null && getParent() != null) {
            val parent: ConstraintWidgetContainer = getParent() as ConstraintWidgetContainer
            mMeasurer = parent.measurer
        }
        mMeasure.horizontalBehavior = horizontalBehavior
        mMeasure.verticalBehavior = verticalBehavior
        mMeasure.horizontalDimension = horizontalDimension
        mMeasure.verticalDimension = verticalDimension
        mMeasurer?.measure(widget, mMeasure)
        widget.width = mMeasure.measuredWidth
        widget.height = mMeasure.measuredHeight
        widget.setHasBaseline(mMeasure.measuredHasBaseline)
        widget.setBaselineDistance(mMeasure.measuredBaseline)
    }

    // @TODO: add description
    operator fun contains(widgets: MutableSet<ConstraintWidget>): Boolean {
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets[i]
            if (widgets.contains(widget)) {
                return true
            }
        }
        return false
    }
}