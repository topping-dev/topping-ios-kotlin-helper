/*
 * Copyright (C) 2017 The Android Open Source Project
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
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.ConstraintWidgetContainer
import dev.topping.ios.constraint.core.widgets.HelperWidget

/**
 * **Added in 1.1**
 *
 *
 * A Barrier references multiple widgets as input,
 * and creates a virtual guideline based on the most
 * extreme widget on the specified side. For example,
 * a left barrier will align to the left of all the referenced views.
 *
 *
 *
 * <h2>Example</h2>
 *
 * <div align="center">
 * <img width="325px" src="resources/images/barrier-buttons.png"></img>
</div> *
 * Let's have two buttons, @id/button1 and @id/button2.
 * The constraint_referenced_ids field will reference
 * them by simply having them as comma-separated list:
 * <pre>
 * `<dev.topping.ios.constraint.constraintlayout.widget.Barrier
 * android:id="@+id/barrier"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * app:barrierDirection="start"
 * app:constraint_referenced_ids="button1,button2" />
` *
</pre> *
 *
 *
 * With the barrier direction set to start, we will have the following result:
 *
 * <div align="center">
 * <img width="325px" src="resources/images/barrier-start.png"></img>
</div> *
 *
 *
 * Reversely, with the direction set to end, we will have:
 *
 * <div align="center">
 * <img width="325px" src="resources/images/barrier-end.png"></img>
</div> *
 *
 *
 * If the widgets dimensions change,
 * the barrier will automatically move according to its direction to get
 * the most extreme widget:
 *
 * <div align="center">
 * <img width="325px" src="resources/images/barrier-adapt.png"></img>
</div> *
 *
 *
 *
 * Other widgets can then be constrained to the barrier itself,
 * instead of the individual widget. This allows a layout
 * to automatically adapt on widget dimension changes
 * (e.g. different languages will end up with different length for similar worlds).
 *
 * <h2>GONE widgets handling</h2>
 *
 * If the barrier references GONE widgets,
 * the default behavior is to create a barrier on the resolved position of the GONE widget.
 * If you do not want to have the barrier take GONE widgets into account,
 * you can change this by setting the attribute *barrierAllowsGoneWidgets*
 * to false (default being true).
 *
 *
 *
 */
class Barrier(myContext: TContext?, self: TView) : ConstraintHelper(myContext, self) {
    /**
     * Get the barrier type (`Barrier.LEFT`, `Barrier.TOP`,
     * `Barrier.RIGHT`, `Barrier.BOTTOM`, `Barrier.END`,
     * `Barrier.START`)
     */
    /**
     * Set the barrier type (`Barrier.LEFT`, `Barrier.TOP`,
     * `Barrier.RIGHT`, `Barrier.BOTTOM`, `Barrier.END`,
     * `Barrier.START`)
     */
    var type = 0
    private var mResolvedType = 0
    private var mBarrier: dev.topping.ios.constraint.core.widgets.Barrier

    init {
        self.setParentType(this)
        self.setVisibility(TView.GONE)
        mBarrier = dev.topping.ios.constraint.core.widgets.Barrier()
        type = self.getObjCProperty("layout_barrierDirection") as Int
        mBarrier.allowsGoneWidget = self.getObjCProperty("layout_barrierAllowsGoneWidgets") as Boolean? ?: false
        val margin: Float = self.dpToPixel(self.getObjCProperty("layout_barrierMargin") as Float? ?: 0f)
        mHelperWidget = mBarrier
        validateParams()
    }

    private fun updateType(widget: ConstraintWidget, type: Int, isRtl: Boolean) {
        mResolvedType = type
        // Post JB MR1, if start/end are defined, they take precedence over left/right
        if (self.isRtl()) {
            if (this.type == START) {
                mResolvedType = RIGHT
            } else if (this.type == END) {
                mResolvedType = LEFT
            }
        } else {
            if (this.type == START) {
                mResolvedType = LEFT
            } else if (this.type == END) {
                mResolvedType = RIGHT
            }
        }
        if(widget is dev.topping.ios.constraint.core.widgets.Barrier) {
            widget.barrierType = mResolvedType
        }
    }

    /**
     * Find if this barrier supports gone widgets.
     *
     * @return true if this barrier supports gone widgets, otherwise false
     *
     */

    /**
     * Find if this barrier supports gone widgets.
     *
     * @return true if this barrier supports gone widgets, otherwise false
     */
    /**
     * allows gone widgets to be included in the barrier
     * @param supportGone
     */
    var allowsGoneWidget: Boolean
        get() = mBarrier.allowsGoneWidget
        set(supportGone) {
            mBarrier.allowsGoneWidget = supportGone
        }

    /**
     * Set a margin on the barrier
     *
     * @param margin in dp
     */
    fun setDpMargin(margin: Int) {
        val density: Float = self.getResources().getDisplayMetrics().density
        val px = (0.5f + margin * density).toInt()
        mBarrier.margin = px
    }
    /**
     * Returns the barrier margin
     *
     * @return the barrier margin (in pixels)
     */
    /**
     * Set the barrier margin
     *
     * @param margin in pixels
     */
    var margin: Int
        get() = mBarrier.margin
        set(margin) {
            mBarrier.margin = margin
        }

    override fun loadParameters(
        constraint: ConstraintSet.Constraint,
        child: HelperWidget?,
        layoutParams: ConstraintLayout.LayoutParams?,
        mapIdToWidget: MutableMap<String, ConstraintWidget>
    ) {
        super.loadParameters(constraint, child, layoutParams, mapIdToWidget)
        if (child is dev.topping.ios.constraint.core.widgets.Barrier) {
            val barrier: dev.topping.ios.constraint.core.widgets.Barrier =
                child
            val container: ConstraintWidgetContainer =
                child.getParent() as ConstraintWidgetContainer
            val isRtl: Boolean = container.isRtl
            updateType(barrier, constraint.layout.mBarrierDirection, isRtl)
            barrier.allowsGoneWidget = constraint.layout.mBarrierAllowsGoneWidgets
            barrier.margin = constraint.layout.mBarrierMargin
        }
    }

    companion object {
        /**
         * Left direction constant
         */
        val LEFT: Int = dev.topping.ios.constraint.core.widgets.Barrier.LEFT

        /**
         * Top direction constant
         */
        val TOP: Int = dev.topping.ios.constraint.core.widgets.Barrier.TOP

        /**
         * Right direction constant
         */
        val RIGHT: Int = dev.topping.ios.constraint.core.widgets.Barrier.RIGHT

        /**
         * Bottom direction constant
         */
        val BOTTOM: Int = dev.topping.ios.constraint.core.widgets.Barrier.BOTTOM

        /**
         * Start direction constant
         */
        val START = BOTTOM + 2

        /**
         * End Barrier constant
         */
        val END = START + 1
    }
}