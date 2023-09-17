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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.height
import dev.topping.ios.constraint.core.widgets.width

/**
 * Control the visibility and elevation of the referenced views
 *
 * **Added in 1.1**
 *
 *
 * This class controls the visibility of a set of referenced widgets.
 * Widgets are referenced by being added to a comma separated list of ids, e.g.:
 * <pre>
 * `<androidx.constraintlayout.widget.Group
 * android:id="@+id/group"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * android:visibility="visible"
 * app:constraint_referenced_ids="button4,button9" />
` *
</pre> *
 *
 *
 * The visibility of the group will be applied to the referenced widgets.
 * It's a convenient way to easily hide/show a set of widgets
 * without having to maintain this set
 * programmatically.
 *
 *
 * <h2>Multiple groups</h2>
 *
 *
 * Multiple groups can reference the same widgets
 * -- in that case, the XML declaration order will
 * define the final visibility state (the group declared last will have the last word).
 *
 */
class Group(myContext: TContext?, attrs: AttributeSet, self: TView) : ConstraintHelper(myContext, attrs, self) {

    init {
        mUseViewMeasure = false
        self.swizzleFunction("setVisibility") { sup, params ->
            val args = params as Array<Any>
            setVisibility(sup, args[0] as Int)
            0
        }
        self.swizzleFunction("setElevation")  { sup, params ->
            val args = params as Array<Any>
            setElevation(sup, args[0] as Float)
            0
        }
    }

    override fun onAttachedToWindow(sup: TView?) {
        super.onAttachedToWindow(sup)
        applyLayoutFeatures()
    }

    fun setVisibility(sup: TView?, visibility: Int) {
        sup?.setVisibility(visibility)
        applyLayoutFeatures()
    }

    fun setElevation(sup: TView?, elevation: Float) {
        sup?.setElevation(elevation)
        applyLayoutFeatures()
    }

    /**
     *
     * @param container
     */
    override fun applyLayoutFeaturesInConstraintSet(container: ConstraintLayout?) {
        applyLayoutFeatures(container!!)
    }

    /**
     *
     * @param container
     */
    override fun updatePostLayout(container: ConstraintLayout?) {
        val params: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        params.mWidget?.width = 0
        params.mWidget?.height = 0
    }
}