/*
 * Copyright (C) 2015 The Android Open Source Project
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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView

/**
 * Utility class representing a Guideline helper object for
 * [ConstraintLayout].
 * Helper objects are not displayed on device
 * (they are marked as `TView.GONE`) and are only used
 * for layout purposes. They only work within a
 * [ConstraintLayout].
 *
 *
 * A Guideline can be either horizontal or vertical:
 *
 *  * Vertical Guidelines have a width of zero and the height of their
 * [ConstraintLayout] parent
 *  * Horizontal Guidelines have a height of zero and the width of their
 * [ConstraintLayout] parent
 *
 *
 *
 * Positioning a Guideline is possible in three different ways:
 *
 *  * specifying a fixed distance from the left or the top of a layout
 * (`layout_constraintGuide_begin`)
 *  * specifying a fixed distance from the right or the bottom of a layout
 * (`layout_constraintGuide_end`)
 *  * specifying a percentage of the width or the height of a layout
 * (`layout_constraintGuide_percent`)
 *
 *
 *
 * Widgets can then be constrained to a Guideline,
 * allowing multiple widgets to be positioned easily from
 * one Guideline, or allowing reactive layout behavior by using percent positioning.
 *
 *
 * See the list of attributes in
 * [androidx.constraintlayout.widget.ConstraintLayout.LayoutParams] to set a Guideline
 * in XML, as well as the corresponding [ConstraintSet.setGuidelineBegin],
 * [ConstraintSet.setGuidelineEnd]
 * and [ConstraintSet.setGuidelinePercent] functions in [ConstraintSet].
 *
 *
 * Example of a `Button` constrained to a vertical `Guideline`:
 * <pre>
 * <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android" xmlns:app="http://schemas.android.com/apk/res-auto" xmlns:tools="http://schemas.android.com/tools" android:layout_width="match_parent" android:layout_height="match_parent">
 *
 * <androidx.constraintlayout.widget.Guideline android:layout_width="wrap_content" android:layout_height="wrap_content" android:id="@+id/guideline" app:layout_constraintGuide_begin="100dp" android:orientation="vertical"></androidx.constraintlayout.widget.Guideline>
 * <Button android:text="Button" android:layout_width="wrap_content" android:layout_height="wrap_content" android:id="@+id/button" app:layout_constraintLeft_toLeftOf="@+id/guideline" android:layout_marginTop="16dp" app:layout_constraintTop_toTopOf="parent"></Button>
</androidx.constraintlayout.widget.ConstraintLayout> *
</pre> *
 *
 *
 */
class Guideline(val context: TContext, val self: TView) {
    private var mFilterRedundantCalls = true

    init {
        self.setParentType(this)
        self.setVisibility(TView.GONE)
        self.swizzleFunction("setVisibility") { sup, params ->
            val args = params as Array<Any>
            setVisibility(sup, args[0] as Int)
            0
        }
        self.swizzleFunction("onMeasure") { sup, params ->
            val args = params as Array<Any>
            setVisibility(sup, args[0] as Int)
            0
        }
    }

    /**
     *
     */
    fun setVisibility(sup: TView?, visibility: Int) {
    }

    /**
     *
     */
    protected fun onMeasure(sup: TView?, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        sup?.setMeasuredDimension(0, 0)
    }

    /**
     * Set the guideline's distance from the top or left edge.
     *
     * @param margin the distance to the top or left edge
     */
    fun setGuidelineBegin(margin: Int) {
        val params: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        if (mFilterRedundantCalls && params.guideBegin == margin) {
            return
        }
        params.guideBegin = margin
        self.setLayoutParams(params)
    }

    /**
     * Set a guideline's distance to end.
     *
     * @param margin the margin to the right or bottom side of container
     */
    fun setGuidelineEnd(margin: Int) {
        val params: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        if (mFilterRedundantCalls && params.guideEnd == margin) {
            return
        }
        params.guideEnd = margin
        self.setLayoutParams(params)
    }

    /**
     * Set a Guideline's percent.
     * @param ratio the ratio between the gap on the left and right 0.0 is top/left 0.5 is middle
     */
    fun setGuidelinePercent(ratio: Float) {
        val params: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        if (mFilterRedundantCalls && params.guidePercent == ratio) {
            return
        }
        params.guidePercent = ratio
        self.setLayoutParams(params)
    }

    /**
     * filter redundant calls to setGuidelineBegin, setGuidelineEnd & setGuidelinePercent.
     *
     * By default calling setGuidelineStart,setGuideLineEnd and setGuidelinePercent will do nothing
     * if the value is the same as the current value. This can disable that behaviour and call
     * setLayoutParams(..) while will call requestLayout
     *
     * @param filter default true set false to always generate a setLayoutParams
     */
    fun setFilterRedundantCalls(filter: Boolean) {
        mFilterRedundantCalls = filter
    }
}