/*
 * Copyright (C) 2018 The Android Open Source Project
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
package dev.topping.ios.constraint.constraintlayout.motion.widget

import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.core.motion.utils.RectF

/**
 * Defines a KeyPositionBase abstract base class KeyPositionBase elements provide
 *
 */
abstract class KeyPositionBase : Key() {
    var mCurveFit: Int = UNSET

    /**
     * Get the position of the view
     *
     *
     * @param layoutWidth
     * @param layoutHeight
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     */
    abstract fun calcPosition(
        layoutWidth: Int,
        layoutHeight: Int,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    )

    /**
     * @return
     */
    abstract val positionX: Float

    /**
     * @return
     */
    abstract val positionY: Float

    override fun getAttributeNames(attributes: MutableSet<String>) {

    }

    /**
     *
     * @param view
     * @param start
     * @param end
     * @param x
     * @param y
     * @param attribute
     * @param value
     */
    abstract fun positionAttributes(
        view: TView,
        start: RectF,
        end: RectF,
        x: Float,
        y: Float,
        attribute: Array<String>,
        value: FloatArray
    )

    /**
     *
     * @param layoutWidth
     * @param layoutHeight
     * @param start
     * @param end
     * @param x
     * @param y
     * @return
     */
    abstract fun intersects(
        layoutWidth: Int,
        layoutHeight: Int,
        start: RectF,
        end: RectF,
        x: Float,
        y: Float
    ): Boolean

    companion object {
        protected const val SELECTION_SLOPE = 20f
    }
}