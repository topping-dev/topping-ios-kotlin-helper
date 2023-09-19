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

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewSpline
import dev.topping.ios.constraint.core.motion.utils.Easing
import dev.topping.ios.constraint.core.motion.utils.RectF
import kotlin.math.*


fun KeyPosition.setFramePosition(value: Int) {
    mFramePosition = value
}

/**
 * Provide the passive data structure to get KeyPosition information form XML
 *
 *
 */
class KeyPosition : KeyPositionBase() {
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

    override var positionX = Float.NaN
        private set

    override var positionY = Float.NaN
        private set

    init {
        mType = KEY_TYPE
    }

    override fun load(context: TContext, attrs: AttributeSet) {
        Loader.read(context, this, attrs)
    }

    override fun addValues(splines: MutableMap<String, ViewSpline>) {

    }

    fun setType(type: Int) {
        mPositionType = type
    }

    override fun calcPosition(
        layoutWidth: Int, layoutHeight: Int,
        startX: Float, startY: Float,
        endX: Float, endY: Float
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
        val dxdx: Float = if (Float.isNaN(mPercentX)) 0f else mPercentX
        val dydx: Float = if (Float.isNaN(mAltPercentY)) 0f else mAltPercentY
        val dydy: Float = if (Float.isNaN(mPercentY)) 0f else mPercentY
        val dxdy: Float = if (Float.isNaN(mAltPercentX)) 0f else mAltPercentX
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
        val dxdx: Float = if (Float.isNaN(mPercentX)) 0f else mPercentX
        val dydx: Float = if (Float.isNaN(mAltPercentY)) 0f else mAltPercentY
        val dydy: Float = if (Float.isNaN(mPercentY)) 0f else mPercentY
        val dxdy: Float = if (Float.isNaN(mAltPercentX)) 0f else mAltPercentX
        positionX = (minX + pathVectorX * dxdx + pathVectorY * dxdy).toInt().toFloat()
        positionY = (minY + pathVectorX * dydx + pathVectorY * dydy).toInt().toFloat()
    }

    override fun positionAttributes(
        view: TView,
        start: RectF,
        end: RectF,
        x: Float,
        y: Float,
        attribute: Array<String>,
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
        start: RectF,
        end: RectF,
        x: Float,
        y: Float,
        attribute: Array<String>,
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
            if (PERCENT_X.equals(
                    attribute[0]
                )
            ) {
                value[0] = dist
                value[1] = perpendicular
            }
        } else {
            attribute[0] = PERCENT_X
            attribute[1] = PERCENT_Y
            value[0] = dist
            value[1] = perpendicular
        }
    }

    fun positionScreenAttributes(
        view: TView,
        start: RectF,
        end: RectF,
        x: Float,
        y: Float,
        attribute: Array<String>,
        value: FloatArray
    ) {
        val startCenterX: Float = start.centerX()
        val startCenterY: Float = start.centerY()
        val endCenterX: Float = end.centerX()
        val endCenterY: Float = end.centerY()
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        val viewGroup: TView = view.getParent() as TView
        val width: Int = viewGroup.getWidth()
        val height: Int = viewGroup.getHeight()
        if (attribute[0] != null) { // they are saying what to use
            if (PERCENT_X.equals(attribute[0])) {
                value[0] = x / width
                value[1] = y / height
            } else {
                value[1] = x / width
                value[0] = y / height
            }
        } else { // we will use what we want to
            attribute[0] = PERCENT_X
            value[0] = x / width
            attribute[1] = PERCENT_Y
            value[1] = y / height
        }
    }

    fun positionCartAttributes(
        start: RectF,
        end: RectF,
        x: Float,
        y: Float,
        attribute: Array<String>,
        value: FloatArray
    ) {
        val startCenterX: Float = start.centerX()
        val startCenterY: Float = start.centerY()
        val endCenterX: Float = end.centerX()
        val endCenterY: Float = end.centerY()
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        if (attribute[0] != null) { // they are saying what to use
            if (PERCENT_X.equals(attribute[0])) {
                value[0] = (x - startCenterX) / pathVectorX
                value[1] = (y - startCenterY) / pathVectorY
            } else {
                value[1] = (x - startCenterX) / pathVectorX
                value[0] = (y - startCenterY) / pathVectorY
            }
        } else { // we will use what we want to
            attribute[0] = PERCENT_X
            value[0] = (x - startCenterX) / pathVectorX
            attribute[1] = PERCENT_Y
            value[1] = (y - startCenterY) / pathVectorY
        }
    }

    fun positionAxisAttributes(
        start: RectF,
        end: RectF,
        x: Float,
        y: Float,
        attribute: Array<String>,
        value: FloatArray
    ) {
        var startCenterX: Float = start.centerX()
        var startCenterY: Float = start.centerY()
        var endCenterX: Float = end.centerX()
        var endCenterY: Float = end.centerY()
        if (startCenterX > endCenterX) {
            val tmp = startCenterX
            startCenterX = endCenterX
            endCenterX = tmp
        }
        if (startCenterY > endCenterY) {
            val tmp = startCenterY
            startCenterY = endCenterY
            endCenterY = tmp
        }
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        if (attribute[0] != null) { // they are saying what to use
            if (PERCENT_X.equals(attribute[0])) {
                value[0] = (x - startCenterX) / pathVectorX
                value[1] = (y - startCenterY) / pathVectorY
            } else {
                value[1] = (x - startCenterX) / pathVectorX
                value[0] = (y - startCenterY) / pathVectorY
            }
        } else { // we will use what we want to
            attribute[0] = PERCENT_X
            value[0] = (x - startCenterX) / pathVectorX
            attribute[1] = PERCENT_Y
            value[1] = (y - startCenterY) / pathVectorY
        }
    }

    override fun intersects(
        layoutWidth: Int,
        layoutHeight: Int,
        start: RectF,
        end: RectF,
        x: Float,
        y: Float
    ): Boolean {
        calcPosition(
            layoutWidth, layoutHeight,
            start.centerX(), start.centerY(),
            end.centerX(), end.centerY()
        )
        return if (abs(x - positionX) < SELECTION_SLOPE
            && abs(y - positionY) < SELECTION_SLOPE
        ) {
            true
        } else false
    }

    private object Loader {
        private const val TARGET_ID = 1
        private const val FRAME_POSITION = 2
        private const val TRANSITION_EASING = 3
        private const val CURVE_FIT = 4
        private const val DRAW_PATH = 5
        private const val PERCENT_X = 6
        private const val PERCENT_Y = 7
        private const val SIZE_PERCENT = 8
        private const val TYPE = 9
        private const val PATH_MOTION_ARC = 10
        private const val PERCENT_WIDTH = 11
        private const val PERCENT_HEIGHT = 12
        private val sAttrMap: MutableMap<String, Int> = mutableMapOf()

        init {
            sAttrMap["motionTarget"] = TARGET_ID
            sAttrMap["framePosition"] = FRAME_POSITION
            sAttrMap["transitionEasing"] = TRANSITION_EASING
            sAttrMap["curveFit"] = CURVE_FIT
            sAttrMap["drawPath"] = DRAW_PATH
            sAttrMap["percentX"] = PERCENT_X
            sAttrMap["percentY"] = PERCENT_Y
            sAttrMap["keyPositionType"] = TYPE
            sAttrMap["sizePercent"] = SIZE_PERCENT
            sAttrMap["percentWidth"] = PERCENT_WIDTH
            sAttrMap["percentHeight"] = PERCENT_HEIGHT
            sAttrMap["pathMotionArc"] = PATH_MOTION_ARC
        }

        fun read(context: TContext, c: KeyPosition, a: AttributeSet) {
            a.forEach { kvp ->
                val intValue = KeyPosition.Loader.sAttrMap[kvp.key]
                when(intValue) {
                    TARGET_ID -> {
                        if (MotionLayout.IS_IN_EDIT_MODE) {
                            c.mTargetId = context.getResources().getResourceId(kvp.value, UNSET_ID)
                            if (c.mTargetId == "") {
                                c.mTargetString = kvp.value
                            }
                        } else {
                            if(context.getResources().getResourceType(kvp.value) == TypedValue.TYPE_STRING)
                            {
                                c.mTargetString = kvp.value
                            } else {
                                c.mTargetId = context.getResources().getResourceId(kvp.value, UNSET_ID)
                            }
                        }
                    }
                    FRAME_POSITION -> c.mFramePosition = context.getResources().getInt(
                        kvp.key,
                        kvp.value,
                        c.mFramePosition
                    )
                    TRANSITION_EASING -> if (context.getResources().getType(kvp.value) == "string") {
                        c.mTransitionEasing = context.getResources().getString(kvp.key, kvp.value)
                    } else {
                        c.mTransitionEasing = Easing.NAMED_EASING.get(context.getResources().getInt(
                            kvp.key,
                            kvp.value,
                            0
                        ))
                    }
                    PATH_MOTION_ARC -> c.mPathMotionArc = context.getResources().getInt(
                        kvp.key,
                        kvp.value,
                        c.mPathMotionArc
                    )
                    CURVE_FIT -> c.mCurveFit = context.getResources().getInt(
                        kvp.key,
                        kvp.value,
                        c.mCurveFit
                    )
                    DRAW_PATH -> c.mDrawPath = context.getResources().getInt(
                        kvp.key,
                        kvp.value,
                        c.mDrawPath
                    )
                    PERCENT_X -> c.mPercentX = context.getResources().getFloat(
                        kvp.key,
                        kvp.value,
                        c.mPercentX
                    )
                    PERCENT_Y -> c.mPercentY = context.getResources().getFloat(
                        kvp.key,
                        kvp.value,
                        c.mPercentY
                    )
                    SIZE_PERCENT -> {
                        c.mPercentWidth = context.getResources().getFloat(
                            kvp.key,
                            kvp.value,
                            c.mPercentWidth
                        )
                        c.mPercentHeight = c.mPercentWidth
                    }
                    PERCENT_WIDTH -> c.mPercentWidth = context.getResources().getFloat(
                        kvp.key,
                        kvp.value,
                        c.mPercentWidth
                    )
                    PERCENT_HEIGHT -> c.mPercentHeight = context.getResources().getFloat(
                        kvp.key,
                        kvp.value,
                        c.mPercentHeight
                    )
                    TYPE -> c.mPositionType = context.getResources().getInt(
                        kvp.key,
                        kvp.value,
                        c.mPositionType
                    )
                    else -> Log.e(TAG, "unused attribute " + sAttrMap.get(kvp.value))
                }
            }
            if (c.mFramePosition == -1) {
                Log.e(TAG, "no frame position")
            }
        }
    }

    override fun setValue(tag: String, value: Any) {
        when (tag) {
            TRANSITION_EASING -> mTransitionEasing = value.toString()
            DRAWPATH -> mDrawPath = toInt(value)
            PERCENT_WIDTH -> mPercentWidth = toFloat(value)
            PERCENT_HEIGHT -> mPercentHeight = toFloat(value)
            SIZE_PERCENT -> {
                mPercentWidth = toFloat(value)
                mPercentHeight = mPercentWidth
            }
            PERCENT_X -> mPercentX = toFloat(value)
            PERCENT_Y -> mPercentY = toFloat(value)
        }
    }

    /**
     * Copy the key
     * @param src to be copied
     * @return self
     */
    override fun copy(src: Key): Key {
        super.copy(src)
        val k = src as KeyPosition
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

    /**
     * Clone this KeyAttributes
     * @return
     */
    override fun clone(): Key {
        return KeyPosition().copy(this)
    }

    companion object {
        private const val TAG = "KeyPosition"
        const val NAME = "KeyPosition"
        const val TYPE_AXIS = 3
        const val TYPE_SCREEN = 2
        const val TYPE_PATH = 1
        const val TYPE_CARTESIAN = 0
        const val TRANSITION_EASING = "transitionEasing"
        const val DRAWPATH = "drawPath"
        const val PERCENT_WIDTH = "percentWidth"
        const val PERCENT_HEIGHT = "percentHeight"
        const val SIZE_PERCENT = "sizePercent"
        const val PERCENT_X = "percentX"
        const val PERCENT_Y = "percentY"
        const val KEY_TYPE = 2
    }
}