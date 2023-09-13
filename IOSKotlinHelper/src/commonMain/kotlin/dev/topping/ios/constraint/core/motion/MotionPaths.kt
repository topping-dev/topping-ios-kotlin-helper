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
package dev.topping.ios.constraint.core.motion

import dev.topping.ios.constraint.Arrays
import dev.topping.ios.constraint.core.motion.MotionWidget.Companion.UNSET
import dev.topping.ios.constraint.core.motion.key.MotionKeyPosition
import dev.topping.ios.constraint.core.motion.utils.Easing
import dev.topping.ios.constraint.core.motion.utils.Utils
import dev.topping.ios.constraint.isNaN
import dev.topping.ios.constraint.toDegrees
import dev.topping.ios.constraint.toRadians

import kotlin.math.*

/**
 * This is used to capture and play back path of the layout.
 * It is used to set the bounds of the view (view.layout(l, t, r, b))
 */
class MotionPaths : Comparable<MotionPaths?> {
    var mId: String? = null
    var mKeyFrameEasing: Easing? = null
    var mDrawPath = 0
    var mTime = 0f
    var mPosition = 0f
    var mX = 0f
    var mY = 0f
    var mWidth = 0f
    var mHeight = 0f
    var mPathRotate = Float.NaN
    var mProgress = Float.NaN
    var mPathMotionArc: Int = UNSET
    var mAnimateRelativeTo: String? = null
    var mRelativeAngle = Float.NaN
    var mRelativeToController: Motion? = null
    var mCustomAttributes: MutableMap<String, CustomVariable?> = mutableMapOf()
    var mMode = 0 // how was this point computed 1=perpendicular 2=deltaRelative
    var mAnimateCircleAngleTo // since angles loop there are 4 ways we can pic direction
            = 0

    constructor() {}

    /**
     * set up with Cartesian
     */
    fun initCartesian(
        c: MotionKeyPosition,
        startTimePoint: MotionPaths,
        endTimePoint: MotionPaths
    ) {
        val position: Float = c.mFramePosition / 100f
        val point = this
        point.mTime = position
        mDrawPath = c.mDrawPath
        val scaleWidth = if (Float.isNaN(c.mPercentWidth)) position else c.mPercentWidth
        val scaleHeight = if (Float.isNaN(c.mPercentHeight)) position else c.mPercentHeight
        val scaleX = endTimePoint.mWidth - startTimePoint.mWidth
        val scaleY = endTimePoint.mHeight - startTimePoint.mHeight
        point.mPosition = point.mTime
        val startCenterX = startTimePoint.mX + startTimePoint.mWidth / 2
        val startCenterY = startTimePoint.mY + startTimePoint.mHeight / 2
        val endCenterX = endTimePoint.mX + endTimePoint.mWidth / 2
        val endCenterY = endTimePoint.mY + endTimePoint.mHeight / 2
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        point.mX =
            (startTimePoint.mX + pathVectorX * position - scaleX * scaleWidth / 2).toInt().toFloat()
        point.mY = (startTimePoint.mY + pathVectorY * position - scaleY * scaleHeight / 2).toInt()
            .toFloat()
        point.mWidth = (startTimePoint.mWidth + scaleX * scaleWidth).toInt().toFloat()
        point.mHeight = (startTimePoint.mHeight + scaleY * scaleHeight).toInt().toFloat()
        val dxdx = if (Float.isNaN(c.mPercentX)) position else c.mPercentX
        val dydx: Float = if (Float.isNaN(c.mAltPercentY)) 0f else c.mAltPercentY
        val dydy = if (Float.isNaN(c.mPercentY)) position else c.mPercentY
        val dxdy: Float = if (Float.isNaN(c.mAltPercentX)) 0f else c.mAltPercentX
        point.mMode = CARTESIAN
        point.mX =
            (startTimePoint.mX + pathVectorX * dxdx + pathVectorY * dxdy - scaleX * scaleWidth / 2).toInt()
                .toFloat()
        point.mY =
            (startTimePoint.mY + pathVectorX * dydx + pathVectorY * dydy - scaleY * scaleHeight / 2).toInt()
                .toFloat()
        point.mKeyFrameEasing = Easing.getInterpolator(c.mTransitionEasing)
        point.mPathMotionArc = c.mPathMotionArc
    }

    /**
     * set up with Axis TODO check
     */
    fun initAxis(c: MotionKeyPosition, startTimePoint: MotionPaths, endTimePoint: MotionPaths) {
        val position: Float = c.mFramePosition / 100f
        val point = this
        point.mTime = position
        mDrawPath = c.mDrawPath
        val scaleWidth = if (Float.isNaN(c.mPercentWidth)) position else c.mPercentWidth
        val scaleHeight = if (Float.isNaN(c.mPercentHeight)) position else c.mPercentHeight
        val scaleX = endTimePoint.mWidth - startTimePoint.mWidth
        val scaleY = endTimePoint.mHeight - startTimePoint.mHeight
        point.mPosition = point.mTime
        val startCenterX = startTimePoint.mX + startTimePoint.mWidth / 2
        val startCenterY = startTimePoint.mY + startTimePoint.mHeight / 2
        val endCenterX = endTimePoint.mX + endTimePoint.mWidth / 2
        val endCenterY = endTimePoint.mY + endTimePoint.mHeight / 2
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        point.mX =
            (startTimePoint.mX + pathVectorX * position - scaleX * scaleWidth / 2).toInt().toFloat()
        point.mY = (startTimePoint.mY + pathVectorY * position - scaleY * scaleHeight / 2).toInt()
            .toFloat()
        point.mWidth = (startTimePoint.mWidth + scaleX * scaleWidth).toInt().toFloat()
        point.mHeight = (startTimePoint.mHeight + scaleY * scaleHeight).toInt().toFloat()
        val dxdx = if (Float.isNaN(c.mPercentX)) position else c.mPercentX
        val dydy = if (Float.isNaN(c.mPercentY)) position else c.mPercentY
        point.mMode = AXIS
        point.mX = (startTimePoint.mX + pathVectorX * dxdx
                - scaleX * scaleWidth / 2).toInt().toFloat()
        point.mY = (startTimePoint.mY
                + pathVectorY * dydy - scaleY * scaleHeight / 2).toInt().toFloat()
        point.mKeyFrameEasing = Easing.getInterpolator(c.mTransitionEasing)
        point.mPathMotionArc = c.mPathMotionArc
    }

    /**
     * takes the new keyPosition
     */
    constructor(
        parentWidth: Int,
        parentHeight: Int,
        c: MotionKeyPosition,
        startTimePoint: MotionPaths,
        endTimePoint: MotionPaths
    ) {
        if (startTimePoint.mAnimateRelativeTo != null) {
            initPolar(parentWidth, parentHeight, c, startTimePoint, endTimePoint)
            return
        }
        when (c.mPositionType) {
            MotionKeyPosition.TYPE_SCREEN -> {
                initScreen(parentWidth, parentHeight, c, startTimePoint, endTimePoint)
                return
            }
            MotionKeyPosition.TYPE_PATH -> {
                initPath(c, startTimePoint, endTimePoint)
                return
            }
            MotionKeyPosition.TYPE_CARTESIAN -> {
                initCartesian(c, startTimePoint, endTimePoint)
                return
            }
            MotionKeyPosition.TYPE_AXIS -> {
                initAxis(c, startTimePoint, endTimePoint)
                return
            }
            else -> {
                initCartesian(c, startTimePoint, endTimePoint)
                return
            }
        }
    }

    fun initPolar(
        parentWidth: Int,
        parentHeight: Int,
        c: MotionKeyPosition,
        s: MotionPaths,
        e: MotionPaths
    ) {
        val position: Float = c.mFramePosition / 100f
        mTime = position
        mDrawPath = c.mDrawPath
        mMode = c.mPositionType // mode and type have same numbering scheme
        val scaleWidth = if (Float.isNaN(c.mPercentWidth)) position else c.mPercentWidth
        val scaleHeight = if (Float.isNaN(c.mPercentHeight)) position else c.mPercentHeight
        val scaleX = e.mWidth - s.mWidth
        val scaleY = e.mHeight - s.mHeight
        mPosition = mTime
        mWidth = (s.mWidth + scaleX * scaleWidth).toInt().toFloat()
        mHeight = (s.mHeight + scaleY * scaleHeight).toInt().toFloat()
         val startfactor = 1 - position
         val endfactor = position
        when (c.mPositionType) {
            MotionKeyPosition.TYPE_SCREEN -> {
                mX =
                    if (Float.isNaN(c.mPercentX)) position * (e.mX - s.mX) + s.mX else c.mPercentX * min(
                        scaleHeight,
                        scaleWidth
                    )
                mY = if (Float.isNaN(c.mPercentY)) position * (e.mY - s.mY) + s.mY else c.mPercentY
            }
            MotionKeyPosition.TYPE_PATH -> {
                mX =
                    (if (Float.isNaN(c.mPercentX)) position else c.mPercentX) * (e.mX - s.mX) + s.mX
                mY =
                    (if (Float.isNaN(c.mPercentY)) position else c.mPercentY) * (e.mY - s.mY) + s.mY
            }
            MotionKeyPosition.TYPE_AXIS -> {
                mX =
                    (if (Float.isNaN(c.mPercentX)) position else c.mPercentX) * abs(e.mX - s.mX) + min(
                        s.mX,
                        e.mX
                    )
                mY =
                    (if (Float.isNaN(c.mPercentY)) position else c.mPercentY) * abs(e.mY - s.mY) + min(
                        s.mY,
                        e.mY
                    )
            }
            MotionKeyPosition.TYPE_CARTESIAN -> {
                mX =
                    (if (Float.isNaN(c.mPercentX)) position else c.mPercentX) * (e.mX - s.mX) + s.mX
                mY =
                    (if (Float.isNaN(c.mPercentY)) position else c.mPercentY) * (e.mY - s.mY) + s.mY
            }
            else -> {
                mX =
                    (if (Float.isNaN(c.mPercentX)) position else c.mPercentX) * (e.mX - s.mX) + s.mX
                mY =
                    (if (Float.isNaN(c.mPercentY)) position else c.mPercentY) * (e.mY - s.mY) + s.mY
            }
        }
        mAnimateRelativeTo = s.mAnimateRelativeTo
        mKeyFrameEasing = Easing.getInterpolator(c.mTransitionEasing)
        mPathMotionArc = c.mPathMotionArc
    }

    // @TODO: add description
    fun setupRelative(mc: Motion?, relative: MotionPaths) {
        val dx = (mX + mWidth / 2 - relative.mX - relative.mWidth / 2).toDouble()
        val dy = (mY + mHeight / 2 - relative.mY - relative.mHeight / 2).toDouble()
        mRelativeToController = mc
        mX = hypot(dy, dx).toFloat()
        mY = if (Float.isNaN(mRelativeAngle)) {
            (atan2(dy, dx) + PI / 2).toFloat()
        } else {
            toRadians(mRelativeAngle)
        }
    }

    fun initScreen(
        parentWidth: Int,
        parentHeight: Int,
        c: MotionKeyPosition,
        startTimePoint: MotionPaths,
        endTimePoint: MotionPaths
    ) {
        var parentWidth = parentWidth
        var parentHeight = parentHeight
        val position: Float = c.mFramePosition / 100f
        val point = this
        point.mTime = position
        mDrawPath = c.mDrawPath
        val scaleWidth = if (Float.isNaN(c.mPercentWidth)) position else c.mPercentWidth
        val scaleHeight = if (Float.isNaN(c.mPercentHeight)) position else c.mPercentHeight
        val scaleX = endTimePoint.mWidth - startTimePoint.mWidth
        val scaleY = endTimePoint.mHeight - startTimePoint.mHeight
        point.mPosition = point.mTime
        val startCenterX = startTimePoint.mX + startTimePoint.mWidth / 2
        val startCenterY = startTimePoint.mY + startTimePoint.mHeight / 2
        val endCenterX = endTimePoint.mX + endTimePoint.mWidth / 2
        val endCenterY = endTimePoint.mY + endTimePoint.mHeight / 2
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        point.mX =
            (startTimePoint.mX + pathVectorX * position - scaleX * scaleWidth / 2).toInt().toFloat()
        point.mY = (startTimePoint.mY + pathVectorY * position - scaleY * scaleHeight / 2).toInt()
            .toFloat()
        point.mWidth = (startTimePoint.mWidth + scaleX * scaleWidth).toInt().toFloat()
        point.mHeight = (startTimePoint.mHeight + scaleY * scaleHeight).toInt().toFloat()
        point.mMode = SCREEN
        if (!Float.isNaN(c.mPercentX)) {
            parentWidth -= point.mWidth.toInt()
            point.mX = (c.mPercentX * parentWidth)
        }
        if (!Float.isNaN(c.mPercentY)) {
            parentHeight -= point.mHeight.toInt()
            point.mY = (c.mPercentY * parentHeight)
        }
        point.mAnimateRelativeTo = mAnimateRelativeTo
        point.mKeyFrameEasing = Easing.getInterpolator(c.mTransitionEasing)
        point.mPathMotionArc = c.mPathMotionArc
    }

    fun initPath(c: MotionKeyPosition, startTimePoint: MotionPaths, endTimePoint: MotionPaths) {
        val position: Float = c.mFramePosition / 100f
        val point = this
        point.mTime = position
        mDrawPath = c.mDrawPath
        val scaleWidth = if (Float.isNaN(c.mPercentWidth)) position else c.mPercentWidth
        val scaleHeight = if (Float.isNaN(c.mPercentHeight)) position else c.mPercentHeight
        val scaleX = endTimePoint.mWidth - startTimePoint.mWidth
        val scaleY = endTimePoint.mHeight - startTimePoint.mHeight
        point.mPosition = point.mTime
        val path =
            if (Float.isNaN(c.mPercentX)) position else c.mPercentX // the position on the path
        val startCenterX = startTimePoint.mX + startTimePoint.mWidth / 2
        val startCenterY = startTimePoint.mY + startTimePoint.mHeight / 2
        val endCenterX = endTimePoint.mX + endTimePoint.mWidth / 2
        val endCenterY = endTimePoint.mY + endTimePoint.mHeight / 2
        val pathVectorX = endCenterX - startCenterX
        val pathVectorY = endCenterY - startCenterY
        point.mX =
            (startTimePoint.mX + pathVectorX * path - scaleX * scaleWidth / 2).toInt().toFloat()
        point.mY =
            (startTimePoint.mY + pathVectorY * path - scaleY * scaleHeight / 2).toInt().toFloat()
        point.mWidth = (startTimePoint.mWidth + scaleX * scaleWidth).toInt().toFloat()
        point.mHeight = (startTimePoint.mHeight + scaleY * scaleHeight).toInt().toFloat()
        val perpendicular: Float =
            if (Float.isNaN(c.mPercentY)) 0f else c.mPercentY // the position on the path
        val perpendicularX = -pathVectorY
        val normalX = perpendicularX * perpendicular
        val normalY = pathVectorX * perpendicular
        point.mMode = PERPENDICULAR
        point.mX =
            (startTimePoint.mX + pathVectorX * path - scaleX * scaleWidth / 2).toInt().toFloat()
        point.mY =
            (startTimePoint.mY + pathVectorY * path - scaleY * scaleHeight / 2).toInt().toFloat()
        point.mX += normalX
        point.mY += normalY
        point.mAnimateRelativeTo = mAnimateRelativeTo
        point.mKeyFrameEasing = Easing.getInterpolator(c.mTransitionEasing)
        point.mPathMotionArc = c.mPathMotionArc
    }

    private fun diff(a: Float, b: Float): Boolean {
        return if (Float.isNaN(a) || Float.isNaN(b)) {
            Float.isNaN(a) != Float.isNaN(b)
        } else abs(a - b) > 0.000001f
    }

    fun different(
        points: MotionPaths?,
        mask: BooleanArray,
        custom: Array<String?>?,
        arcMode: Boolean
    ) {
        var c = 0
        val diffx = diff(mX, points!!.mX)
        val diffy = diff(mY, points.mY)
        mask[c++] = mask[c++] or diff(mPosition, points.mPosition)
        mask[c++] = mask[c++] or (diffx || diffy || arcMode)
        mask[c++] = mask[c++] or (diffx || diffy || arcMode)
        mask[c++] = mask[c++] or diff(mWidth, points.mWidth)
        mask[c++] = mask[c++] or diff(mHeight, points.mHeight)
    }

    fun getCenter(p: Double, toUse: IntArray, data: DoubleArray, point: FloatArray, offset: Int) {
        var v_x = mX
        var v_y = mY
        var v_width = mWidth
        var v_height = mHeight
        val translationX = 0f
        val translationY = 0f
        for (i in toUse.indices) {
            val value = data[i].toFloat()
            when (toUse[i]) {
                OFF_X -> v_x = value
                OFF_Y -> v_y = value
                OFF_WIDTH -> v_width = value
                OFF_HEIGHT -> v_height = value
            }
        }
        if (mRelativeToController != null) {
            val pos = FloatArray(2)
            val vel = FloatArray(2)
            mRelativeToController!!.getCenter(p, pos, vel)
            val rx = pos[0]
            val ry = pos[1]
            val radius = v_x
            val angle = v_y
            // TODO Debug angle
            v_x = (rx + radius * sin(angle) - v_width / 2)
            v_y = (ry - radius * cos(angle) - v_height / 2)
        }
        point[offset] = v_x + v_width / 2 + translationX
        point[offset + 1] = v_y + v_height / 2 + translationY
    }

    fun getCenter(
        p: Double,
        toUse: IntArray,
        data: DoubleArray,
        point: FloatArray,
        vdata: DoubleArray,
        velocity: FloatArray
    ) {
        var v_x = mX
        var v_y = mY
        var v_width = mWidth
        var v_height = mHeight
        var dv_x = 0f
        var dv_y = 0f
        var dv_width = 0f
        var dv_height = 0f
        val translationX = 0f
        val translationY = 0f
        for (i in toUse.indices) {
            val value = data[i].toFloat()
            val dvalue = vdata[i].toFloat()
            when (toUse[i]) {
                OFF_X -> {
                    v_x = value
                    dv_x = dvalue
                }
                OFF_Y -> {
                    v_y = value
                    dv_y = dvalue
                }
                OFF_WIDTH -> {
                    v_width = value
                    dv_width = dvalue
                }
                OFF_HEIGHT -> {
                    v_height = value
                    dv_height = dvalue
                }
            }
        }
        var dpos_x = dv_x + dv_width / 2
        var dpos_y = dv_y + dv_height / 2
        if (mRelativeToController != null) {
            val pos = FloatArray(2)
            val vel = FloatArray(2)
            mRelativeToController!!.getCenter(p, pos, vel)
            val rx = pos[0]
            val ry = pos[1]
            val radius = v_x
            val angle = v_y
            val dradius = dv_x
            val dangle = dv_y
            val drx = vel[0]
            val dry = vel[1]
            // TODO Debug angle
            v_x = (rx + radius * sin(angle) - v_width / 2)
            v_y = (ry - radius * cos(angle) - v_height / 2)
            dpos_x = (drx + dradius * sin(angle) + cos(angle) * dangle)
            dpos_y = (dry - dradius * cos(angle) + sin(angle) * dangle)
        }
        point[0] = v_x + v_width / 2 + translationX
        point[1] = v_y + v_height / 2 + translationY
        velocity[0] = dpos_x
        velocity[1] = dpos_y
    }

    fun getCenterVelocity(
        p: Double,
        toUse: IntArray,
        data: DoubleArray,
        point: FloatArray,
        offset: Int
    ) {
        var v_x = mX
        var v_y = mY
        var v_width = mWidth
        var v_height = mHeight
        val translationX = 0f
        val translationY = 0f
        for (i in toUse.indices) {
            val value = data[i].toFloat()
            when (toUse[i]) {
                OFF_X -> v_x = value
                OFF_Y -> v_y = value
                OFF_WIDTH -> v_width = value
                OFF_HEIGHT -> v_height = value
            }
        }
        if (mRelativeToController != null) {
            val pos = FloatArray(2)
            val vel = FloatArray(2)
            mRelativeToController!!.getCenter(p, pos, vel)
            val rx = pos[0]
            val ry = pos[1]
            val radius = v_x
            val angle = v_y
            // TODO Debug angle
            v_x = (rx + radius * sin(angle) - v_width / 2)
            v_y = (ry - radius * cos(angle) - v_height / 2)
        }
        point[offset] = v_x + v_width / 2 + translationX
        point[offset + 1] = v_y + v_height / 2 + translationY
    }

    fun getBounds(toUse: IntArray, data: DoubleArray, point: FloatArray, offset: Int) {
         var v_x = mX
         var v_y = mY
        var v_width = mWidth
        var v_height = mHeight
        for (i in toUse.indices) {
            val value = data[i].toFloat()
            when (toUse[i]) {
                OFF_X -> v_x = value
                OFF_Y -> v_y = value
                OFF_WIDTH -> v_width = value
                OFF_HEIGHT -> v_height = value
            }
        }
        point[offset] = v_width
        point[offset + 1] = v_height
    }

    var mTempValue = DoubleArray(18)
    var mTempDelta = DoubleArray(18)

    // Called on the start Time Point
    fun setView(
        position: Float,
        view: MotionWidget,
        toUse: IntArray,
        data: DoubleArray,
        slope: DoubleArray,
        cycle: DoubleArray?
    ) {
        var v_x = mX
        var v_y = mY
        var v_width = mWidth
        var v_height = mHeight
        var dv_x = 0f
        var dv_y = 0f
        var dv_width = 0f
        var dv_height = 0f
         var delta_path = 0f
        var path_rotate = Float.NaN
         var mod: String = ""
        if (toUse.size != 0 && mTempValue.size <= toUse[toUse.size - 1]) {
            val scratch_data_length = toUse[toUse.size - 1] + 1
            mTempValue = DoubleArray(scratch_data_length)
            mTempDelta = DoubleArray(scratch_data_length)
        }
        Arrays.fill(mTempValue, Double.NaN)
        for (i in toUse.indices) {
            mTempValue[toUse[i]] = data[i]
            mTempDelta[toUse[i]] = slope[i]
        }
        for (i in mTempValue.indices) {
            if (Double.isNaN(mTempValue[i]) && (cycle == null || cycle[i] == 0.0)) {
                continue
            }
            val deltaCycle = cycle?.get(i) ?: 0.0
            val value =
                (if (Double.isNaN(mTempValue[i])) deltaCycle else mTempValue[i] + deltaCycle).toFloat()
            val dvalue = mTempDelta[i].toFloat()
            when (i) {
                OFF_POSITION -> delta_path = value
                OFF_X -> {
                    v_x = value
                    dv_x = dvalue
                }
                OFF_Y -> {
                    v_y = value
                    dv_y = dvalue
                }
                OFF_WIDTH -> {
                    v_width = value
                    dv_width = dvalue
                }
                OFF_HEIGHT -> {
                    v_height = value
                    dv_height = dvalue
                }
                OFF_PATH_ROTATE -> path_rotate = value
            }
        }
        if (mRelativeToController != null) {
            val pos = FloatArray(2)
            val vel = FloatArray(2)
            mRelativeToController!!.getCenter(position.toDouble(), pos, vel)
            val rx = pos[0]
            val ry = pos[1]
            val radius = v_x
            val angle = v_y
            val dradius = dv_x
            val dangle = dv_y
            val drx = vel[0]
            val dry = vel[1]

            // TODO Debug angle
            val pos_x = (rx + radius * sin(angle) - v_width / 2).toFloat()
            val pos_y = (ry - radius * cos(angle) - v_height / 2).toFloat()
            val dpos_x =
                (drx + dradius * sin(angle) + radius * cos(angle) * dangle).toFloat()
            val dpos_y = (dry - dradius * cos(angle)
                    + radius * sin(angle) * dangle).toFloat()
            dv_x = dpos_x
            dv_y = dpos_y
            v_x = pos_x
            v_y = pos_y
            if (slope.size >= 2) {
                slope[0] = dpos_x.toDouble()
                slope[1] = dpos_y.toDouble()
            }
            if (!Float.isNaN(path_rotate)) {
                val rot = (path_rotate + toDegrees(atan2(dv_y, dv_x).toDouble()).toFloat())
                view.rotationZ = rot
            }
        } else {
            if (!Float.isNaN(path_rotate)) {
                var rot = 0f
                val dx = dv_x + dv_width / 2
                val dy = dv_y + dv_height / 2
                if (I_DEBUG) {
                    Utils.log(TAG, "dv_x       =$dv_x")
                    Utils.log(TAG, "dv_y       =$dv_y")
                    Utils.log(TAG, "dv_width   =$dv_width")
                    Utils.log(TAG, "dv_height  =$dv_height")
                }
                rot += (path_rotate + toDegrees(atan2(dy, dx).toDouble()).toFloat())
                view.rotationZ = rot
                if (I_DEBUG) {
                    Utils.log(TAG, "Rotated $rot  = $dx,$dy")
                }
            }
        }

        // Todo: develop a concept of Float layout in MotionWidget widget.layout(float ...)
        var l = (0.5f + v_x).toInt()
        var t = (0.5f + v_y).toInt()
        var r = (0.5f + v_x + v_width).toInt()
        var b = (0.5f + v_y + v_height).toInt()
        var i_width = r - l
        var i_height = b - t
        if (OLD_WAY) { // This way may produce more stable with and height but risk gaps
            l = v_x.toInt()
            t = v_y.toInt()
            i_width = v_width.toInt()
            i_height = v_height.toInt()
            r = l + i_width
            b = t + i_height
        }

        // MotionWidget must do Android TView measure if layout changes
        view.layout(l, t, r, b)
        if (I_DEBUG) {
            if (toUse.size > 0) {
                Utils.log(TAG, "setView $mod")
            }
        }
    }

    fun getRect(toUse: IntArray, data: DoubleArray, path: FloatArray, offset: Int) {
        var offset = offset
        var v_x = mX
        var v_y = mY
        var v_width = mWidth
        var v_height = mHeight
         var delta_path = 0f
        val rotation = 0f
         val alpha = 0f
         val rotationX = 0f
         val rotationY = 0f
        val scaleX = 1f
        val scaleY = 1f
        val pivotX = Float.NaN
        val pivotY = Float.NaN
        val translationX = 0f
        val translationY = 0f
         var mod: String
        for (i in toUse.indices) {
            val value = data[i].toFloat()
            when (toUse[i]) {
                OFF_POSITION -> delta_path = value
                OFF_X -> v_x = value
                OFF_Y -> v_y = value
                OFF_WIDTH -> v_width = value
                OFF_HEIGHT -> v_height = value
            }
        }
        if (mRelativeToController != null) {
            val rx: Float = mRelativeToController?.centerX ?: 0f
            val ry: Float = mRelativeToController?.centerY ?: 0f
            val radius = v_x
            val angle = v_y
            // TODO Debug angle
            v_x = (rx + radius * sin(angle) - v_width / 2)
            v_y = (ry - radius * cos(angle) - v_height / 2)
        }
        var x1 = v_x
        var y1 = v_y
        var x2 = v_x + v_width
        var y2 = y1
        var x3 = x2
        var y3 = v_y + v_height
        var x4 = x1
        var y4 = y3
        var cx = x1 + v_width / 2
        var cy = y1 + v_height / 2
        if (!Float.isNaN(pivotX)) {
            cx = x1 + (x2 - x1) * pivotX
        }
        if (!Float.isNaN(pivotY)) {
            cy = y1 + (y3 - y1) * pivotY
        }
        if (scaleX != 1f) {
            val midx = (x1 + x2) / 2
            x1 = (x1 - midx) * scaleX + midx
            x2 = (x2 - midx) * scaleX + midx
            x3 = (x3 - midx) * scaleX + midx
            x4 = (x4 - midx) * scaleX + midx
        }
        if (scaleY != 1f) {
            val midy = (y1 + y3) / 2
            y1 = (y1 - midy) * scaleY + midy
            y2 = (y2 - midy) * scaleY + midy
            y3 = (y3 - midy) * scaleY + midy
            y4 = (y4 - midy) * scaleY + midy
        }
        if (rotation != 0f) {
            val sin = sin(toRadians(rotation)).toFloat()
            val cos = cos(toRadians(rotation)).toFloat()
            val tx1 = xRotate(sin, cos, cx, cy, x1, y1)
            val ty1 = yRotate(sin, cos, cx, cy, x1, y1)
            val tx2 = xRotate(sin, cos, cx, cy, x2, y2)
            val ty2 = yRotate(sin, cos, cx, cy, x2, y2)
            val tx3 = xRotate(sin, cos, cx, cy, x3, y3)
            val ty3 = yRotate(sin, cos, cx, cy, x3, y3)
            val tx4 = xRotate(sin, cos, cx, cy, x4, y4)
            val ty4 = yRotate(sin, cos, cx, cy, x4, y4)
            x1 = tx1
            y1 = ty1
            x2 = tx2
            y2 = ty2
            x3 = tx3
            y3 = ty3
            x4 = tx4
            y4 = ty4
        }
        x1 += translationX
        y1 += translationY
        x2 += translationX
        y2 += translationY
        x3 += translationX
        y3 += translationY
        x4 += translationX
        y4 += translationY
        path[offset++] = x1
        path[offset++] = y1
        path[offset++] = x2
        path[offset++] = y2
        path[offset++] = x3
        path[offset++] = y3
        path[offset++] = x4
        path[offset++] = y4
    }

    /**
     * mAnchorDpDt
     */
    fun setDpDt(
        locationX: Float,
        locationY: Float,
        mAnchorDpDt: FloatArray,
        toUse: IntArray,
        deltaData: DoubleArray,
        data: DoubleArray?
    ) {
        var d_x = 0f
        var d_y = 0f
        var d_width = 0f
        var d_height = 0f
        val deltaScaleX = 0f
        val deltaScaleY = 0f
         val mPathRotate = Float.NaN
        val deltaTranslationX = 0f
        val deltaTranslationY = 0f
        var mod = " dd = "
        for (i in toUse.indices) {
            val deltaV = deltaData[i].toFloat()
            if (I_DEBUG) {
                mod += " , D" + sNames[toUse[i]] + "/Dt= " + deltaV
            }
            when (toUse[i]) {
                OFF_POSITION -> {}
                OFF_X -> d_x = deltaV
                OFF_Y -> d_y = deltaV
                OFF_WIDTH -> d_width = deltaV
                OFF_HEIGHT -> d_height = deltaV
            }
        }
        if (I_DEBUG) {
            if (toUse.size > 0) {
                Utils.log(TAG, "setDpDt $mod")
            }
        }
        val deltaX = d_x - deltaScaleX * d_width / 2
        val deltaY = d_y - deltaScaleY * d_height / 2
        val deltaWidth = d_width * (1 + deltaScaleX)
        val deltaHeight = d_height * (1 + deltaScaleY)
        val deltaRight = deltaX + deltaWidth
        val deltaBottom = deltaY + deltaHeight
        if (I_DEBUG) {
            if (toUse.size > 0) {
                Utils.log(TAG, "D x /dt           =$d_x")
                Utils.log(TAG, "D y /dt           =$d_y")
                Utils.log(TAG, "D width /dt       =$d_width")
                Utils.log(TAG, "D height /dt      =$d_height")
                Utils.log(TAG, "D deltaScaleX /dt =$deltaScaleX")
                Utils.log(TAG, "D deltaScaleY /dt =$deltaScaleY")
                Utils.log(TAG, "D deltaX /dt      =$deltaX")
                Utils.log(TAG, "D deltaY /dt      =$deltaY")
                Utils.log(TAG, "D deltaWidth /dt  =$deltaWidth")
                Utils.log(TAG, "D deltaHeight /dt =$deltaHeight")
                Utils.log(TAG, "D deltaRight /dt  =$deltaRight")
                Utils.log(TAG, "D deltaBottom /dt =$deltaBottom")
                Utils.log(TAG, "locationX         =$locationX")
                Utils.log(TAG, "locationY         =$locationY")
                Utils.log(TAG, "deltaTranslationX =$deltaTranslationX")
                Utils.log(TAG, "deltaTranslationX =$deltaTranslationX")
            }
        }
        mAnchorDpDt[0] = deltaX * (1 - locationX) + deltaRight * locationX + deltaTranslationX
        mAnchorDpDt[1] = deltaY * (1 - locationY) + deltaBottom * locationY + deltaTranslationY
    }

    fun fillStandard(data: DoubleArray, toUse: IntArray) {
        val set = floatArrayOf(mPosition, mX, mY, mWidth, mHeight, mPathRotate)
        var c = 0
        for (i in toUse.indices) {
            if (toUse[i] < set.size) {
                data[c++] = set[toUse[i]].toDouble()
            }
        }
    }

    fun hasCustomData(name: String): Boolean {
        return mCustomAttributes.containsKey(name)
    }

    fun getCustomDataCount(name: String): Int {
        val a: CustomVariable = mCustomAttributes[name] ?: return 0
        return a.numberOfInterpolatedValues()
    }

    fun getCustomData(name: String, value: DoubleArray, offset: Int): Int {
        var offset = offset
        val a: CustomVariable? = mCustomAttributes[name]
        return if (a == null) {
            0
        } else if (a.numberOfInterpolatedValues() == 1) {
            value[offset] = a.valueToInterpolate.toDouble()
            1
        } else {
            val n: Int = a.numberOfInterpolatedValues()
            val f = FloatArray(n)
            a.getValuesToInterpolate(f)
            for (i in 0 until n) {
                value[offset++] = f[i].toDouble()
            }
            n
        }
    }

    fun setBounds(x: Float, y: Float, w: Float, h: Float) {
        mX = x
        mY = y
        mWidth = w
        mHeight = h
    }

    
    override operator fun compareTo(o: MotionPaths?): Int {
        return mPosition.compareTo(o?.mPosition ?: 0f)
    }

    // @TODO: add description
    fun applyParameters(c: MotionWidget) {
        val point = this
        point.mKeyFrameEasing = Easing.getInterpolator(c.mMotion.mTransitionEasing)
        point.mPathMotionArc = c.mMotion.mPathMotionArc
        point.mAnimateRelativeTo = c.mMotion.mAnimateRelativeTo
        point.mPathRotate = c.mMotion.mPathRotate
        point.mDrawPath = c.mMotion.mDrawPath
        point.mAnimateCircleAngleTo = c.mMotion.mAnimateCircleAngleTo
        point.mProgress = c.mPropertySet.mProgress
        if (c.mWidgetFrame.widget != null) {
            point.mRelativeAngle = c.mWidgetFrame.widget!!.mCircleConstraintAngle
        }
        val at: Set<String> = c.customAttributeNames
        for (s in at) {
            val attr: CustomVariable? = c.getCustomAttribute(s)
            if (attr != null && attr.isContinuous) {
                mCustomAttributes[s] = attr
            }
        }
    }

    // @TODO: add description
    fun configureRelativeTo(toOrbit: Motion) {
         val p: DoubleArray =
            toOrbit.getPos(mProgress.toDouble()) // get the position
        // in the orbit
    }

    companion object {
        const val TAG = "MotionPaths"
        const val I_DEBUG = false
        const val OLD_WAY = false // the computes the positions the old way
        const val OFF_POSITION = 0
        const val OFF_X = 1
        const val OFF_Y = 2
        const val OFF_WIDTH = 3
        const val OFF_HEIGHT = 4
        const val OFF_PATH_ROTATE = 5

        // mode and type have same numbering scheme
        val PERPENDICULAR: Int = MotionKeyPosition.TYPE_PATH
        val CARTESIAN: Int = MotionKeyPosition.TYPE_CARTESIAN
        val AXIS: Int = MotionKeyPosition.TYPE_AXIS
        val SCREEN: Int = MotionKeyPosition.TYPE_SCREEN
        var sNames = arrayOf("position", "x", "y", "width", "height", "pathRotate")
        private fun xRotate(
            sin: Float,
            cos: Float,
            cx: Float,
            cy: Float,
            x: Float,
            y: Float
        ): Float {
            var x = x
            var y = y
            x = x - cx
            y = y - cy
            return x * cos - y * sin + cx
        }

        private fun yRotate(
            sin: Float,
            cos: Float,
            cx: Float,
            cy: Float,
            x: Float,
            y: Float
        ): Float {
            var x = x
            var y = y
            x = x - cx
            y = y - cy
            return x * sin + y * cos + cy
        }
    }
}