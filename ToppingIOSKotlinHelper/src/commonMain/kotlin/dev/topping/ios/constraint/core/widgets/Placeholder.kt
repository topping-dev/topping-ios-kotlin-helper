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
package dev.topping.ios.constraint.core.widgets

import dev.topping.ios.constraint.core.LinearSystem
import dev.topping.ios.constraint.core.widgets.analyzer.BasicMeasure.Companion.AT_MOST
import dev.topping.ios.constraint.core.widgets.analyzer.BasicMeasure.Companion.EXACTLY
import dev.topping.ios.constraint.core.widgets.analyzer.BasicMeasure.Companion.UNSPECIFIED
import kotlin.math.*

/**
 * Simple VirtualLayout that center the first referenced widget onto itself.
 */
class Placeholder : VirtualLayout() {
    // @TODO: add description
    
    override fun measure(widthMode: Int, widthSize: Int, heightMode: Int, heightSize: Int) {
        var width = 0
        var height = 0
        val paddingLeft: Int = super.paddingLeft
        val paddingRight: Int = super.paddingRight
        val paddingTop: Int = super.paddingTop
        val paddingBottom: Int = super.paddingBottom
        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom
        if (mWidgetsCount > 0) {
            // grab the first referenced widget size in case we are ourselves in wrap_content
            width += mWidgets[0]?.width ?: 0
            height += mWidgets[0]?.height ?: 0
        }
        width = max(minWidth, width)
        height = max(minHeight, height)
        var measuredWidth = 0
        var measuredHeight = 0
        if (widthMode == EXACTLY) {
            measuredWidth = widthSize
        } else if (widthMode == AT_MOST) {
            measuredWidth = min(width, widthSize)
        } else if (widthMode == UNSPECIFIED) {
            measuredWidth = width
        }
        if (heightMode == EXACTLY) {
            measuredHeight = heightSize
        } else if (heightMode == AT_MOST) {
            measuredHeight = min(height, heightSize)
        } else if (heightMode == UNSPECIFIED) {
            measuredHeight = height
        }
        setMeasure(measuredWidth, measuredHeight)
        width = measuredWidth
        height = measuredHeight
        needsCallbackFromSolver(mWidgetsCount > 0)
    }

    
    override fun addToSolver(system: LinearSystem, optimize: Boolean) {
        super.addToSolver(system, optimize)
        if (mWidgetsCount > 0) {
            val widget: ConstraintWidget? = mWidgets[0]
            widget?.let {
                widget.resetAllConstraints()
                widget.connect(ConstraintAnchor.Type.LEFT, this, ConstraintAnchor.Type.LEFT)
                widget.connect(ConstraintAnchor.Type.RIGHT, this, ConstraintAnchor.Type.RIGHT)
                widget.connect(ConstraintAnchor.Type.TOP, this, ConstraintAnchor.Type.TOP)
                widget.connect(ConstraintAnchor.Type.BOTTOM, this, ConstraintAnchor.Type.BOTTOM)
            }
        }
    }
}