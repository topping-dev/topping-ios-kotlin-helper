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
package dev.topping.ios.constraint.constraintlayout.helper.widget

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.MeasureSpec
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.widget.VirtualLayout
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.ConstraintWidgetContainer
import dev.topping.ios.constraint.core.widgets.Helper
import dev.topping.ios.constraint.core.widgets.Placeholder

class MotionPlaceholder(context: TContext, attrs: AttributeSet, self: TView) : VirtualLayout(context, attrs, self) {
    var mPlaceholder: Placeholder? = null

    init {
        self.setParentType(this)
        mHelperWidget = Placeholder()
        validateParams()
    }


    override fun onMeasure(sup: TView?, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onMeasure(mPlaceholder, widthMeasureSpec, heightMeasureSpec)
    }

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

    override fun updatePreLayout(
        container: ConstraintWidgetContainer?,
        helper: Helper,
        map: MutableMap<String, ConstraintWidget>
    ) {
        // override to block the ids being replaced
    }

    companion object {
        private const val TAG = "MotionPlaceholder"
    }
}