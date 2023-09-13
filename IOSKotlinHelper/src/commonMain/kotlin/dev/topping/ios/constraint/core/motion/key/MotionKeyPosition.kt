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
package dev.topping.ios.constraint.core.motion.key

import dev.topping.ios.constraint.core.motion.MotionWidget
import dev.topping.ios.constraint.core.motion.utils.FloatRect
import dev.topping.ios.constraint.core.motion.utils.SplineSet
import dev.topping.ios.constraint.core.motion.utils.TypedValues
import kotlin.math.*

class MotionKeyPosition : MotionKey() {
    var mCurveFit: Int = UNSET
    var mTransitionEasing: String? = null
    var mPathMotionArc: Int = UNSET // -1 means not set
    var mDrawPath = 0
    var mPercentWidth = Float.NaN
    var mPercentHeight = Float.NaN
    var mPercentX = Float.NaN
    var mPercentY = Float.NaN
    var mAltPercentX = Float.NaN
    var mAltPercentY = Float.NaN
    var mPositionType = TYPE_CARTESIAN
    var positionX = Float.NaN
        private set
    var positionY = Float.NaN
        private set

    init {
        mType = KEY_TYPE
    }

    // TODO this needs the views dimensions to be accurate
    private fun calcScreenPosition(layoutWidth: Int, layoutHeight: Int) {
        val viewWidth = 0
        val viewHeight = 0
        positionX = (layoutWidth - viewWidth) * mPercentX + viewWidth / 2
        positionY = (layoutHeight - viewHeight) * mPercentX + viewHeight / 2
    }

    private fun calcPathPosition(
        startX: Float, startY: Float,
        endX: Float, endY: Float
    ) {
        val pathVectorX = endX - startX
        val pathVectorY = endY - startY
        val perpendicularX = -pathVectorY
        positionX = startX + pathVectorX * mPercentX + perpendicularX * mPercentY
        positionY = startY + pathVectorY * mPercentX + pathVectorX * mPercentY
    }

    private fun calcCartesianPosition(
        startX: Float, startY: Float,
        endX: Float, endY: Float
    ) {
        val pathVectorX = endX - startX
        val pathVectorY = endY - startY
        val dxdx: Float = if (mPercentX.isNaN()) 0f else mPercentX
        val dydx: Float = if (mAltPercentY.isNaN()) 0f else mAltPercentY
        val dydy: Float = if (mPercentY.isNaN()) 0f else mPercentY
        val dxdy: Float = if (mAltPercentX.isNaN()) 0f else mAltPercentX
        positionX = (startX + pathVectorX * dxdx + pathVectorY * dxdy).toInt().toFloat()
        positionY = (startY + pathVectorX * dydx + pathVectorY * dydy).toInt().toFloat()
    }

    private fun calcAxisPosition(
        startX: Float, startY: Float,
        endX: Float, endY: Float
    ) {
        val pathVectorX: Float = abs(endX - startX)
        val pathVectorY: Float = abs(endY - startY)
        val minX: Float = min(startX, endX)
        val minY: Float = min(startY, endY)
        val dxdx: Float = if (mPercentX.isNaN()) 0f else mPercentX
        val dydx: Float = if (mAltPercentY.isNaN()) 0f else mAltPercentY
        val dydy: Float = if (mPercentY.isNaN()) 0f else mPercentY
        val dxdy: Float = if (mAltPercentX.isNaN()) 0f else mAltPercentX
        positionX = (minX + pathVectorX * dxdx + pathVectorY * dxdy).toInt().toFloat()
        positionY = (minY + pathVectorX * dydx + pathVectorY * dydy).toInt().toFloat()
    }

    // @TODO: add description
    fun positionAttributes(
        view: MotionWidget,
        start: FloatRect,
        end: FloatRect,
        x: Float,
        y: Float,
        attribute: Array<String?>,
        value: FloatArray
    ) {
        when (mPositionType) {
            TYPE_PATH -> {
                positionPathAttributes(start, end, x, y, attribute, value)
                return
            }
            TYPE_SCREEN -> {
                positionScreenAttributes(view, start, end, x, y, attribute, value)
                return
            }
            TYPE_AXIS -> {
                positionAxisAttributes(start, end, x, y, attribute, value)
                return
            }
            TYPE_CARTESIAN -> {
                positionCartAttributes(start, end, x, y, attribute, value)
                return
            }
            else -> {
                positionCartAttributes(start, end, x, y, attribute, value)
                return
            }
        }
    }

    fun positionPathAttributes(
        start: FloatRect,
        end: FloatRect,
        x: Float,
        y: Float,
        attribute: Array<String?>,
        value: FloatArray
    ) {
        val startCenterX: Float = start.centerX()
        val startCenterY: Float = start.centerY()
        val endCenterX: Float = end.centerX()
        val endCenterY: Float = end.centerY()
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        val distance = hypot(pathVectorX, pathVectorY).toFloat()
        if (distance < 0.0001) {
            println("distance ~ 0")
            value[0] = 0f
            value[1] = 0f
            return
        }
        val dx = pathVectorX / distance
        val dy = pathVectorY / distance
        val perpendicular = (dx * (y - startCenterY) - (x - startCenterX) * dy) / distance
        val dist = (dx * (x - startCenterX) + dy * (y - startCenterY)) / distance
        if (attribute[0] != null) {
            if (TypedValues.PositionType.S_PERCENT_X.equals(attribute[0])) {
                value[0] = dist
                value[1] = perpendicular
            }
        } else {
            attribute[0] = TypedValues.PositionType.S_PERCENT_X
            attribute[1] = TypedValues.PositionType.S_PERCENT_Y
            value[0] = dist
            value[1] = perpendicular
        }
    }

    fun positionScreenAttributes(
        view: MotionWidget,
        start: FloatRect,
        end: FloatRect,
        x: Float,
        y: Float,
        attribute: Array<String?>,
        value: FloatArray
    ) {
        val startCenterX: Float = start.centerX()
        val startCenterY: Float = start.centerY()
        val endCenterX: Float = end.centerX()
        val endCenterY: Float = end.centerY()
         val pathVectorX = endCenterX - startCenterX
         val pathVectorY = endCenterY - startCenterY
        val viewGroup: MotionWidget = view.parent as MotionWidget
        val width: Int = viewGroup.width
        val height: Int = viewGroup.height
        if (attribute[0] != null) { // they are saying what to use
            if (TypedValues.PositionType.S_PERCENT_X.equals(attribute[0])) {
                value[0] = x / width
                value[1] = y / height
            } else {
                value[1] = x / width
                value[0] = y / height
            }
        } else { // we will use what we want to
            attribute[0] = TypedValues.PositionType.S_PERCENT_X
            value[0] = x / width
            attribute[1] = TypedValues.PositionType.S_PERCENT_Y
            value[1] = y / height
        }
    }

    fun positionCartAttributes(
        start: FloatRect,
        end: FloatRect,
        x: Float,
        y: Float,
        attribute: Array<String?>,
        value: FloatArray
    ) {
        val startCenterX: Float = start.centerX()
        val startCenterY: Float = start.centerY()
        val endCenterX: Float = end.centerX()
        val endCenterY: Float = end.centerY()
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        if (attribute[0] != null) { // they are saying what to use
            if (TypedValues.PositionType.S_PERCENT_X.equals(attribute[0])) {
                value[0] = (x - startCenterX) / pathVectorX
                value[1] = (y - startCenterY) / pathVectorY
            } else {
                value[1] = (x - startCenterX) / pathVectorX
                value[0] = (y - startCenterY) / pathVectorY
            }
        } else { // we will use what we want to
            attribute[0] = TypedValues.PositionType.S_PERCENT_X
            value[0] = (x - startCenterX) / pathVectorX
            attribute[1] = TypedValues.PositionType.S_PERCENT_Y
            value[1] = (y - startCenterY) / pathVectorY
        }
    }

    fun positionAxisAttributes(
        start: FloatRect,
        end: FloatRect,
        x: Float,
        y: Float,
        attribute: Array<String?>,
        value: FloatArray
    ) {
        val startCenterX: Float = start.centerX()
        val startCenterY: Float = start.centerY()
        val endCenterX: Float = end.centerX()
        val endCenterY: Float = end.centerY()
        val minX: Float = min(startCenterX, endCenterX)
        val minY: Float = min(startCenterY, endCenterY)
        val pathVectorX: Float = abs(endCenterX - startCenterX)
        val pathVectorY: Float = abs(endCenterY - startCenterY)
        if (attribute[0] != null) { // they are saying what to use
            if (TypedValues.PositionType.S_PERCENT_X.equals(attribute[0])) {
                value[0] = (x - minX) / pathVectorX
                value[1] = (y - minY) / pathVectorY
            } else {
                value[1] = (x - minX) / pathVectorX
                value[0] = (y - minY) / pathVectorY
            }
        } else { // we will use what we want to
            attribute[0] = TypedValues.PositionType.S_PERCENT_X
            value[0] = (x - minX) / pathVectorX
            attribute[1] = TypedValues.PositionType.S_PERCENT_Y
            value[1] = (y - minY) / pathVectorY
        }
    }

    // @TODO: add description
    fun intersects(
        layoutWidth: Int,
        layoutHeight: Int,
        start: FloatRect,
        end: FloatRect,
        x: Float,
        y: Float
    ): Boolean {
        calcPosition(
            layoutWidth, layoutHeight, start.centerX(),
            start.centerY(), end.centerX(), end.centerY()
        )
        return if (abs(x - positionX) < SELECTION_SLOPE
            && abs(y - positionY) < SELECTION_SLOPE
        ) {
            true
        } else false
    }

    // @TODO: add description
    
    override fun copy(src: MotionKey): MotionKey {
        super.copy(src)
        val k = src as MotionKeyPosition
        mTransitionEasing = k.mTransitionEasing
        mPathMotionArc = k.mPathMotionArc
        mDrawPath = k.mDrawPath
        mPercentWidth = k.mPercentWidth
        mPercentHeight = Float.NaN
        mPercentX = k.mPercentX
        mPercentY = k.mPercentY
        mAltPercentX = k.mAltPercentX
        mAltPercentY = k.mAltPercentY
        positionX = k.positionX
        positionY = k.positionY
        return this
    }

    // @TODO: add description
    
    override fun clone(): MotionKey {
        return MotionKeyPosition().copy(this)
    }

    fun calcPosition(
        layoutWidth: Int,
        layoutHeight: Int,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ) {
        when (mPositionType) {
            TYPE_SCREEN -> {
                calcScreenPosition(layoutWidth, layoutHeight)
                return
            }
            TYPE_PATH -> {
                calcPathPosition(startX, startY, endX, endY)
                return
            }
            TYPE_AXIS -> {
                calcAxisPosition(startX, startY, endX, endY)
                return
            }
            TYPE_CARTESIAN -> {
                calcCartesianPosition(startX, startY, endX, endY)
                return
            }
            else -> {
                calcCartesianPosition(startX, startY, endX, endY)
                return
            }
        }
    }

    
    override fun getAttributeNames(attributes: MutableSet<String>?) {
    }
    // @TODO: add description
    /**
     * @param splines splines to write values to
     */
    
    override fun addValues(splines: MutableMap<String, SplineSet>?) {
    }

    
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            TypedValues.PositionType.TYPE_POSITION_TYPE -> mPositionType = value
            TypedValues.TYPE_FRAME_POSITION -> mFramePosition = value
            TypedValues.PositionType.TYPE_CURVE_FIT -> mCurveFit = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            TypedValues.PositionType.TYPE_PERCENT_WIDTH -> mPercentWidth = value
            TypedValues.PositionType.TYPE_PERCENT_HEIGHT -> mPercentHeight = value
            TypedValues.PositionType.TYPE_SIZE_PERCENT -> {
                mPercentWidth = value
                mPercentHeight = mPercentWidth
            }
            TypedValues.PositionType.TYPE_PERCENT_X -> mPercentX = value
            TypedValues.PositionType.TYPE_PERCENT_Y -> mPercentY = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    
    override fun setValue(type: Int, value: String): Boolean {
        mTransitionEasing = when (type) {
            TypedValues.PositionType.TYPE_TRANSITION_EASING -> value
            else -> return super.setValue(type, value)
        }
        return true
    }

    
    override fun getId(name: String): Int {
        return TypedValues.PositionType.getId(name)
    }

    companion object {
        const val NAME = "KeyPosition"
        protected const val SELECTION_SLOPE = 20f
        const val TYPE_AXIS = 3
        const val TYPE_SCREEN = 2
        const val TYPE_PATH = 1
        const val TYPE_CARTESIAN = 0
        const val KEY_TYPE = 2
    }
}