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

import dev.topping.ios.constraint.core.motion.utils.*
import dev.topping.ios.constraint.isNaN

import kotlin.math.*

/**
 * All the parameter it extracts from a ConstraintSet/TView
 *
 *
 */
internal class MotionConstrainedPoint : Comparable<MotionConstrainedPoint> {
    private var mAlpha = 1f
    var mVisibilityMode: Int = MotionWidget.VISIBILITY_MODE_NORMAL
    var mVisibility = 0

    
    private var mApplyElevation = false
    private val mElevation = 0f
    private var mRotation = 0f
    private var mRotationX = 0f
    var rotationY = 0f
    private var mScaleX = 1f
    private var mScaleY = 1f
    private var mPivotX = Float.NaN
    private var mPivotY = Float.NaN
    private var mTranslationX = 0f
    private var mTranslationY = 0f
    private var mTranslationZ = 0f

    
    private val mKeyFrameEasing: Easing? = null

    
    private val mDrawPath = 0
    private val mPosition = 0f
    private var mX = 0f
    private var mY = 0f
    private var mWidth = 0f
    private var mHeight = 0f
    private val mPathRotate = Float.NaN
    private val mProgress = Float.NaN

    
    private val mAnimateRelativeTo = -1
    var mCustomVariable: LinkedHashMap<String, CustomVariable> = LinkedHashMap()
    var mMode = 0 // how was this point computed 1=perpendicular 2=deltaRelative
    private fun diff(a: Float, b: Float): Boolean {
        return if (Float.isNaN(a) || Float.isNaN(b)) {
            Float.isNaN(a) != Float.isNaN(b)
        } else abs(a - b) > 0.000001f
    }

    /**
     * Given the start and end points define Keys that need to be built
     */
    fun different(points: MotionConstrainedPoint, keySet: MutableSet<String>) {
        if (diff(mAlpha, points.mAlpha)) {
            keySet.add(TypedValues.AttributesType.S_ALPHA)
        }
        if (diff(mElevation, points.mElevation)) {
            keySet.add(TypedValues.AttributesType.S_TRANSLATION_Z)
        }
        if (mVisibility != points.mVisibility && mVisibilityMode == MotionWidget.VISIBILITY_MODE_NORMAL && (mVisibility == MotionWidget.VISIBLE
                    || points.mVisibility == MotionWidget.VISIBLE)
        ) {
            keySet.add(TypedValues.AttributesType.S_ALPHA)
        }
        if (diff(mRotation, points.mRotation)) {
            keySet.add(TypedValues.AttributesType.S_ROTATION_Z)
        }
        if (!(Float.isNaN(mPathRotate) && Float.isNaN(points.mPathRotate))) {
            keySet.add(TypedValues.AttributesType.S_PATH_ROTATE)
        }
        if (!(Float.isNaN(mProgress) && Float.isNaN(points.mProgress))) {
            keySet.add(TypedValues.AttributesType.S_PROGRESS)
        }
        if (diff(mRotationX, points.mRotationX)) {
            keySet.add(TypedValues.AttributesType.S_ROTATION_X)
        }
        if (diff(rotationY, points.rotationY)) {
            keySet.add(TypedValues.AttributesType.S_ROTATION_Y)
        }
        if (diff(mPivotX, points.mPivotX)) {
            keySet.add(TypedValues.AttributesType.S_PIVOT_X)
        }
        if (diff(mPivotY, points.mPivotY)) {
            keySet.add(TypedValues.AttributesType.S_PIVOT_Y)
        }
        if (diff(mScaleX, points.mScaleX)) {
            keySet.add(TypedValues.AttributesType.S_SCALE_X)
        }
        if (diff(mScaleY, points.mScaleY)) {
            keySet.add(TypedValues.AttributesType.S_SCALE_Y)
        }
        if (diff(mTranslationX, points.mTranslationX)) {
            keySet.add(TypedValues.AttributesType.S_TRANSLATION_X)
        }
        if (diff(mTranslationY, points.mTranslationY)) {
            keySet.add(TypedValues.AttributesType.S_TRANSLATION_Y)
        }
        if (diff(mTranslationZ, points.mTranslationZ)) {
            keySet.add(TypedValues.AttributesType.S_TRANSLATION_Z)
        }
        if (diff(mElevation, points.mElevation)) {
            keySet.add(TypedValues.AttributesType.S_ELEVATION)
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

    var mTempValue = DoubleArray(18)
    var mTempDelta = DoubleArray(18)
    fun fillStandard(data: DoubleArray, toUse: IntArray) {
        val set = floatArrayOf(
            mPosition, mX, mY, mWidth, mHeight, mAlpha, mElevation,
            mRotation, mRotationX, rotationY, mScaleX, mScaleY, mPivotX,
            mPivotY, mTranslationX, mTranslationY, mTranslationZ, mPathRotate
        )
        var c = 0
        for (i in toUse.indices) {
            if (toUse[i] < set.size) {
                data[c++] = set[toUse[i]].toDouble()
            }
        }
    }

    fun hasCustomData(name: String): Boolean {
        return mCustomVariable.containsKey(name)
    }

    fun getCustomDataCount(name: String): Int {
        return mCustomVariable[name]!!.numberOfInterpolatedValues()
    }

    fun getCustomData(name: String, value: DoubleArray, offset: Int): Int {
        var offset = offset
        val a: CustomVariable? = mCustomVariable[name]
        return if (a!!.numberOfInterpolatedValues() == 1) {
            value[offset] = a?.valueToInterpolate?.toDouble() ?: 0.0
            1
        } else {
            val n: Int = a!!.numberOfInterpolatedValues()
            val f = FloatArray(n)
            a!!.getValuesToInterpolate(f)
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

    override fun compareTo(other: MotionConstrainedPoint): Int {
        return mPosition.compareTo(other.mPosition)
    }

    fun applyParameters(view: MotionWidget) {
        mVisibility = view.visibility
        mAlpha = if (view.visibility != MotionWidget.VISIBLE) 0.0f else view.alpha
        mApplyElevation = false // TODO figure a way to cache parameters
        mRotation = view.rotationZ
        mRotationX = view.rotationX
        rotationY = view.rotationY
        mScaleX = view.scaleX
        mScaleY = view.scaleY
        mPivotX = view.pivotX
        mPivotY = view.pivotY
        mTranslationX = view.translationX
        mTranslationY = view.translationY
        mTranslationZ = view.translationZ
        val at: Set<String> = view.customAttributeNames
        for (s in at) {
            val attr: CustomVariable? = view.getCustomAttribute(s)
            if (attr != null && attr.isContinuous) {
                mCustomVariable[s] = attr
            }
        }
    }

    fun addValues(splines: MutableMap<String, SplineSet>?, mFramePosition: Int) {
        if(splines == null)
            return
        for (s in splines) {
            val ViewSpline: SplineSet = s.value
            if (I_DEBUG) {
                Utils.log(TAG, "setPoint$mFramePosition  spline set = $s")
            }
            when (s.key) {
                TypedValues.AttributesType.S_ALPHA -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mAlpha)) 1f else mAlpha
                )
                TypedValues.AttributesType.S_ROTATION_Z -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mRotation)) 0f else mRotation
                )
                TypedValues.AttributesType.S_ROTATION_X -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mRotationX)) 0f else mRotationX
                )
                TypedValues.AttributesType.S_ROTATION_Y -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(rotationY)) 0f else rotationY
                )
                TypedValues.AttributesType.S_PIVOT_X -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mPivotX)) 0f else mPivotX
                )
                TypedValues.AttributesType.S_PIVOT_Y -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mPivotY)) 0f else mPivotY
                )
                TypedValues.AttributesType.S_PATH_ROTATE -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mPathRotate)) 0f else mPathRotate
                )
                TypedValues.AttributesType.S_PROGRESS -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mProgress)) 0f else mProgress
                )
                TypedValues.AttributesType.S_SCALE_X -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mScaleX)) 1f else mScaleX
                )
                TypedValues.AttributesType.S_SCALE_Y -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mScaleY)) 1f else mScaleY
                )
                TypedValues.AttributesType.S_TRANSLATION_X -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mTranslationX)) 0f else mTranslationX
                )
                TypedValues.AttributesType.S_TRANSLATION_Y -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mTranslationY)) 0f else mTranslationY
                )
                TypedValues.AttributesType.S_TRANSLATION_Z -> ViewSpline.setPoint(
                    mFramePosition,
                    if (Float.isNaN(mTranslationZ)) 0f else mTranslationZ
                )
                else -> if (s.key.startsWith("CUSTOM")) {
                    val customName = s.key.split(",")[1]
                    if (mCustomVariable.containsKey(customName)) {
                        val custom: CustomVariable? = mCustomVariable[customName]
                        if (ViewSpline is SplineSet.CustomSpline) {
                            ViewSpline
                                .setPoint(mFramePosition, custom)
                        } else {
                            Utils.loge(
                                TAG, s.key + " ViewSpline not a CustomSet frame = "
                                        + mFramePosition + ", value"
                                        + custom?.valueToInterpolate + ViewSpline
                            )
                        }
                    }
                } else {
                    Utils.loge(TAG, "UNKNOWN spline $s")
                }
            }
        }
    }

    fun setState(view: MotionWidget) {
        setBounds(view.x.toFloat(), view.y.toFloat(), view.width.toFloat(), view.height.toFloat())
        applyParameters(view)
    }

    /**
     * @param rect     assumes pre rotated
     * @param rotation mode Surface.ROTATION_0,Surface.ROTATION_90...
     */
    fun setState(rect: Rect, view: MotionWidget, rotation: Int, prevous: Float) {
        setBounds(rect.left.toFloat(), rect.top.toFloat(), rect.width().toFloat(), rect.height().toFloat())
        applyParameters(view)
        mPivotX = Float.NaN
        mPivotY = Float.NaN
        when (rotation) {
            MotionWidget.ROTATE_PORTRATE_OF_LEFT -> mRotation = prevous + 90
            MotionWidget.ROTATE_PORTRATE_OF_RIGHT -> mRotation = prevous - 90
        }
    } //   TODO support Screen Rotation

    //    /**
    //     * Sets the state of the position given a rect, constraintset, rotation and viewid
    //     *
    //     * @param cw
    //     * @param constraintSet
    //     * @param rotation
    //     * @param viewId
    //     */
    //    public void setState(Rect cw, ConstraintSet constraintSet, int rotation, int viewId) {
    //        setBounds(cw.left, cw.top, cw.width(), cw.height());
    //        applyParameters(constraintSet.getParameters(viewId));
    //        switch (rotation) {
    //            case ConstraintSet.ROTATE_PORTRATE_OF_RIGHT:
    //            case ConstraintSet.ROTATE_RIGHT_OF_PORTRATE:
    //                this.rotation -= 90;
    //                break;
    //            case ConstraintSet.ROTATE_PORTRATE_OF_LEFT:
    //            case ConstraintSet.ROTATE_LEFT_OF_PORTRATE:
    //                this.rotation += 90;
    //                if (this.rotation > 180) this.rotation -= 360;
    //                break;
    //        }
    //    }
    companion object {
        const val TAG = "MotionPaths"
        const val I_DEBUG = false
        const val PERPENDICULAR = 1
        const val CARTESIAN = 2
        var sNames = arrayOf("position", "x", "y", "width", "height", "pathRotate")
    }
}