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

import dev.topping.ios.constraint.Log
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewSpline
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.core.motion.utils.Easing
import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.isNaN
import kotlin.math.*


/**
 * All the parameter it extracts from a ConstraintSet/TView
 *
 *
 */
internal class MotionConstrainedPoint : Comparable<MotionConstrainedPoint> {
    var rotationY = 0f
    var mVisibilityMode: Int = ConstraintSet.VISIBILITY_MODE_NORMAL
    var mVisibility = 0
    var mAttributes: MutableMap<String, ConstraintAttribute> = mutableMapOf()
    var mMode = 0 // how was this point computed 1=perpendicular 2=deltaRelative
    var mTempValue = DoubleArray(18)
    var mTempDelta = DoubleArray(18)
    private var mAlpha = 1f

    private var mApplyElevation = false
    private var mElevation = 0f
    private var mRotation = 0f
    private var mRotationX = 0f
    private var mScaleX = 1f
    private var mScaleY = 1f
    private var mPivotX = Float.NaN
    private var mPivotY = Float.NaN
    private var mTranslationX = 0f
    private var mTranslationY = 0f
    private var mTranslationZ = 0f

    private var mKeyFrameEasing: Easing? = null

    private var mDrawPath = 0

    private val mPosition = 0f
    private var mX = 0f
    private var mY = 0f
    private var mWidth = 0f
    private var mHeight = 0f
    private var mPathRotate = Float.NaN
    private var mProgress = Float.NaN

    private var mAnimateRelativeTo = ""
    private fun diff(a: Float, b: Float): Boolean {
        return if (Float.isNaN(a) || Float.isNaN(b)) {
            Float.isNaN(a) != Float.isNaN(b)
        } else abs(a - b) > 0.000001f
    }

    /**
     * Given the start and end points define Keys that need to be built
     *
     * @param points
     * @param keySet
     */
    fun different(points: MotionConstrainedPoint, keySet: MutableSet<String>) {
        if (diff(mAlpha, points.mAlpha)) {
            keySet.add(Key.ALPHA)
        }
        if (diff(mElevation, points.mElevation)) {
            keySet.add(Key.ELEVATION)
        }
        if (mVisibility != points.mVisibility && mVisibilityMode == ConstraintSet.VISIBILITY_MODE_NORMAL && (mVisibility == ConstraintSet.VISIBLE
                    || points.mVisibility == ConstraintSet.VISIBLE)
        ) {
            keySet.add(Key.ALPHA)
        }
        if (diff(mRotation, points.mRotation)) {
            keySet.add(Key.ROTATION)
        }
        if (!(Float.isNaN(mPathRotate) && Float.isNaN(points.mPathRotate))) {
            keySet.add(Key.TRANSITION_PATH_ROTATE)
        }
        if (!(Float.isNaN(mProgress) && Float.isNaN(points.mProgress))) {
            keySet.add(Key.PROGRESS)
        }
        if (diff(mRotationX, points.mRotationX)) {
            keySet.add(Key.ROTATION_X)
        }
        if (diff(rotationY, points.rotationY)) {
            keySet.add(Key.ROTATION_Y)
        }
        if (diff(mPivotX, points.mPivotX)) {
            keySet.add(Key.PIVOT_X)
        }
        if (diff(mPivotY, points.mPivotY)) {
            keySet.add(Key.PIVOT_Y)
        }
        if (diff(mScaleX, points.mScaleX)) {
            keySet.add(Key.SCALE_X)
        }
        if (diff(mScaleY, points.mScaleY)) {
            keySet.add(Key.SCALE_Y)
        }
        if (diff(mTranslationX, points.mTranslationX)) {
            keySet.add(Key.TRANSLATION_X)
        }
        if (diff(mTranslationY, points.mTranslationY)) {
            keySet.add(Key.TRANSLATION_Y)
        }
        if (diff(mTranslationZ, points.mTranslationZ)) {
            keySet.add(Key.TRANSLATION_Z)
        }
    }

    fun different(points: MotionConstrainedPoint, mask: BooleanArray, custom: Array<String?>?) {
        var c = 0
        mask[c++] = mask[c++] or diff(mPosition, points.mPosition)
        mask[c++] = mask[c++] or diff(mX, points.mX)
        mask[c++] = mask[c++] or diff(mY, points.mY)
        mask[c++] = mask[c++] or diff(mWidth, points.mWidth)
        mask[c++] = mask[c++] or diff(mHeight, points.mHeight)
    }

    fun fillStandard(data: DoubleArray, toUse: IntArray) {
        val set = floatArrayOf(
            mPosition, mX, mY, mWidth, mHeight, mAlpha, mElevation,
            mRotation, mRotationX, rotationY,
            mScaleX, mScaleY, mPivotX, mPivotY,
            mTranslationX, mTranslationY, mTranslationZ, mPathRotate
        )
        var c = 0
        for (i in toUse.indices) {
            if (toUse[i] < set.size) {
                data[c++] = set[toUse[i]].toDouble()
            }
        }
    }

    fun hasCustomData(name: String): Boolean {
        return mAttributes.containsKey(name)
    }

    fun getCustomDataCount(name: String): Int {
        return mAttributes[name]?.numberOfInterpolatedValues() ?: 0
    }

    fun getCustomData(name: String, value: DoubleArray, offset: Int): Int {
        var offset = offset
        val a: ConstraintAttribute = mAttributes[name]!!
        return if (a.numberOfInterpolatedValues() == 1) {
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

    override fun compareTo(o: MotionConstrainedPoint): Int {
        return mPosition.compareTo(o.mPosition)
    }

    fun applyParameters(view: TView) {
        mVisibility = view.getVisibility()
        mAlpha = if (view.getVisibility() !== TView.VISIBLE) 0.0f else view.getAlpha()
        mApplyElevation = false // TODO figure a way to cache parameters
        mElevation = view.getElevation()
        mRotation = view.getRotation()
        mRotationX = view.getRotationX()
        rotationY = view.getRotationY()
        mScaleX = view.getScaleX()
        mScaleY = view.getScaleY()
        mPivotX = view.getPivotX()
        mPivotY = view.getPivotY()
        mTranslationX = view.getTranslationX()
        mTranslationY = view.getTranslationY()
        mTranslationZ = view.getTranslationZ()
    }

    fun applyParameters(c: ConstraintSet.Constraint) {
        mVisibilityMode = c.propertySet.mVisibilityMode
        mVisibility = c.propertySet.visibility
        mAlpha = if (c.propertySet.visibility !== ConstraintSet.VISIBLE
            && mVisibilityMode == ConstraintSet.VISIBILITY_MODE_NORMAL
        ) 0.0f else c.propertySet.alpha
        mApplyElevation = c.transform.applyElevation
        mElevation = c.transform.elevation
        mRotation = c.transform.rotation
        mRotationX = c.transform.rotationX
        rotationY = c.transform.rotationY
        mScaleX = c.transform.scaleX
        mScaleY = c.transform.scaleY
        mPivotX = c.transform.transformPivotX
        mPivotY = c.transform.transformPivotY
        mTranslationX = c.transform.translationX
        mTranslationY = c.transform.translationY
        mTranslationZ = c.transform.translationZ
        mKeyFrameEasing = Easing.getInterpolator(c.motion.mTransitionEasing)
        mPathRotate = c.motion.mPathRotate
        mDrawPath = c.motion.mDrawPath
        mAnimateRelativeTo = c.motion.mAnimateRelativeTo
        mProgress = c.propertySet.mProgress
        val at: Set<String> = c.mCustomConstraints.keys
        for (s in at) {
            val attr: ConstraintAttribute? = c.mCustomConstraints.get(s)
            if (attr?.isContinuous == true) {
                mAttributes[s] = attr
            }
        }
    }

    fun addValues(splines: MutableMap<String, ViewSpline>, mFramePosition: Int) {
        for (s in splines.keys) {
            val viewSpline: ViewSpline = splines[s] ?: continue
            if (I_DEBUG) {
                Log.v(TAG, "setPoint$mFramePosition  spline set = $s")
            }
            when (s) {
                Key.ALPHA -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mAlpha)) 1f else mAlpha
                )
                Key.ELEVATION -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mElevation)) 0f else mElevation
                )
                Key.ROTATION -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mRotation)) 0f else mRotation
                )
                Key.ROTATION_X -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mRotationX)) 0f else mRotationX
                )
                Key.ROTATION_Y -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(rotationY)) 0f else rotationY
                )
                Key.PIVOT_X -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mPivotX)) 0f else mPivotX
                )
                Key.PIVOT_Y -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mPivotY)) 0f else mPivotY
                )
                Key.TRANSITION_PATH_ROTATE -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mPathRotate)) 0f else mPathRotate
                )
                Key.PROGRESS -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mProgress)) 0f else mProgress
                )
                Key.SCALE_X -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mScaleX)) 1f else mScaleX
                )
                Key.SCALE_Y -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mScaleY)) 1f else mScaleY
                )
                Key.TRANSLATION_X -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mTranslationX)) 0f else mTranslationX
                )
                Key.TRANSLATION_Y -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mTranslationY)) 0f else mTranslationY
                )
                Key.TRANSLATION_Z -> viewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mTranslationZ)) 0f else mTranslationZ
                )
                else -> if (s.startsWith("CUSTOM")) {
                    val customName = s.split(",")[1]
                    if (mAttributes.containsKey(customName)) {
                        val custom: ConstraintAttribute? = mAttributes[customName]
                        if (viewSpline is ViewSpline.CustomSet) {
                            (viewSpline as ViewSpline.CustomSet)
                                .setPoint(mFramePosition, custom!!)
                        } else {
                            Log.e(
                                TAG, s + " ViewSpline not a CustomSet frame = "
                                        + mFramePosition
                                        + ", value" + custom!!.valueToInterpolate
                                        + viewSpline
                            )
                        }
                    }
                } else {
                    Log.e(TAG, "UNKNOWN spline $s")
                }
            }
        }
    }

    fun setState(view: TView) {
        setBounds(view.getX().toFloat(), view.getY().toFloat(), view.getWidth().toFloat(), view.getHeight().toFloat())
        applyParameters(view)
    }

    /**
     * @param rect     assumes pre rotated
     * @param view
     * @param rotation mode Surface.ROTATION_0,Surface.ROTATION_90...
     */
    fun setState(rect: Rect, view: TView, rotation: Int, prevous: Float) {
        setBounds(rect.left.toFloat(), rect.top.toFloat(), rect.width().toFloat(), rect.height().toFloat())
        applyParameters(view)
        mPivotX = Float.NaN
        mPivotY = Float.NaN
        when (rotation) {
            ConstraintSet.ROTATE_PORTRATE_OF_LEFT -> mRotation = prevous + 90
            ConstraintSet.ROTATE_PORTRATE_OF_RIGHT -> mRotation = prevous - 90
        }
    }

    /**
     * Sets the state of the position given a rect, constraintset, rotation and viewid
     *
     * @param cw
     * @param constraintSet
     * @param rotation
     * @param viewId
     */
    fun setState(cw: Rect, constraintSet: ConstraintSet, rotation: Int, viewId: String) {
        setBounds(cw.left.toFloat(), cw.top.toFloat(), cw.width().toFloat(), cw.height().toFloat())
        applyParameters(constraintSet.getParameters(viewId))
        when (rotation) {
            ConstraintSet.ROTATE_PORTRATE_OF_RIGHT, ConstraintSet.ROTATE_RIGHT_OF_PORTRATE -> mRotation -= 90f
            ConstraintSet.ROTATE_PORTRATE_OF_LEFT, ConstraintSet.ROTATE_LEFT_OF_PORTRATE -> {
                mRotation += 90f
                if (mRotation > 180) mRotation -= 360f
            }
        }
    }

    companion object {
        const val TAG = "MotionPaths"
        const val I_DEBUG = false
        const val PERPENDICULAR = 1
        const val CARTESIAN = 2
        var sNames = arrayOf("position", "x", "y", "width", "height", "pathRotate")
    }
}