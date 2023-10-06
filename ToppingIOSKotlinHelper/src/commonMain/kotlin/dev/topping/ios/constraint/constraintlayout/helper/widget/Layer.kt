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

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintHelper
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Layer adds the ability to move and rotate a group of views as if they were contained
 * in a viewGroup
 * **Added in 2.0**
 * Methods such as setRotation(float) rotate all views about a common center.
 * For simple visibility manipulation use Group
 *
 */
class Layer(context: TContext, attrs: AttributeSet, self: TView) : ConstraintHelper(context, attrs, self) {
    private var mRotationCenterX = Float.NaN
    private var mRotationCenterY = Float.NaN
    private var mGroupRotateAngle = Float.NaN
    var mContainer: ConstraintLayout? = null
    private var mScaleX = 1f
    private var mScaleY = 1f
    protected var mComputedCenterX = Float.NaN
    protected var mComputedCenterY = Float.NaN
    protected var mComputedMaxX = Float.NaN
    protected var mComputedMaxY = Float.NaN
    protected var mComputedMinX = Float.NaN
    protected var mComputedMinY = Float.NaN
    var mNeedBounds = true
    var mViews: Array<TView?>? = null // used to reduce the getViewById() cost
    private var mShiftX = 0f
    private var mShiftY = 0f
    private var mApplyVisibilityOnAttach = false
    private var mApplyElevationOnAttach = false

    init {
        mUseViewMeasure = false
        attrs.forEach { kvp ->
            if (kvp.key == "android_visibility") {
                mApplyVisibilityOnAttach = true
            } else if (kvp.key == "android_elevation") {
                mApplyElevationOnAttach = true
            }
        }
    }

    override fun onAttachedToWindow(sup: TView?) {
        super.onAttachedToWindow(sup)
        mContainer = self.getParent()?.getParentType() as ConstraintLayout?
        if (mApplyVisibilityOnAttach || mApplyElevationOnAttach) {
            val visibility = self.getVisibility()
            var elevation = self.getElevation()
            for (i in 0 until mCount) {
                val id = mIds.get(i)
                val view: TView? = mContainer?.getViewById(id)
                if (view != null) {
                    if (mApplyVisibilityOnAttach) {
                        view.setVisibility(visibility)
                    }
                    if (mApplyElevationOnAttach) {
                        if (elevation > 0) {
                            view.setTranslationZ(view.getTranslationZ() + elevation)
                        }
                    }
                }
            }
        }
    }

    /**
     * @param container
     */
    override fun updatePreDraw(container: ConstraintLayout?) {
        mContainer = container
        val rotate: Float = self.getRotation()
        if (rotate == 0f) {
            if (!Float.isNaN(mGroupRotateAngle)) {
                mGroupRotateAngle = rotate
            }
        } else {
            mGroupRotateAngle = rotate
        }
    }

    /**
     * Rotates all associated views around a single point post layout..
     * The point is the middle of the bounding box or set by setPivotX,setPivotX;
     * @param angle
     */
    fun setRotation(angle: Float) {
        mGroupRotateAngle = angle
        transform()
    }

    /**
     * Scales all associated views around a single point post layout..
     * The point is the middle of the bounding box or set by setPivotX,setPivotX;
     * @param scaleX The value to scale in X.
     */
    fun setScaleX(scaleX: Float) {
        mScaleX = scaleX
        transform()
    }

    /**
     * Scales all associated views around a single point post layout..
     * The point is the middle of the bounding box or set by setPivotX,setPivotX;
     * @param scaleY The value to scale in X.
     */
    fun setScaleY(scaleY: Float) {
        mScaleY = scaleY
        transform()
    }

    /**
     * Sets the pivot point for scale operations.
     * Setting it to Float.NaN (default) results in the center of the group being used.
     * @param pivotX The X location of the pivot point
     */
    fun setPivotX(pivotX: Float) {
        mRotationCenterX = pivotX
        transform()
    }

    /**
     * Sets the pivot point for scale operations.
     * Setting it to Float.NaN (default) results in the center of the group being used.
     * @param pivotY The Y location of the pivot point
     */
    fun setPivotY(pivotY: Float) {
        mRotationCenterY = pivotY
        transform()
    }

    /**
     * Shift all the views in the X direction post layout.
     * @param dx number of pixes to shift
     */
    fun setTranslationX(dx: Float) {
        mShiftX = dx
        transform()
    }

    /**
     * Shift all the views in the Y direction post layout.
     * @param dy number of pixes to shift
     */
    fun setTranslationY(dy: Float) {
        mShiftY = dy
        transform()
    }

    /**
     *
     */
    fun setVisibility(visibility: Int) {
        self.setVisibility(visibility)
        applyLayoutFeatures()
    }

    /**
     *
     */
    fun setElevation(elevation: Float) {
        self.setElevation(elevation)
        applyLayoutFeatures()
    }

    /**
     * @param container
     */
    override fun updatePostLayout(container: ConstraintLayout?) {
        reCacheViews()
        mComputedCenterX = Float.NaN
        mComputedCenterY = Float.NaN
        val params: ConstraintLayout.LayoutParams? =
            self.getLayoutParams() as ConstraintLayout.LayoutParams?
        val widget: ConstraintWidget? = params?.constraintWidget
        widget?.setWidth(0)
        widget?.setHeight(0)
        calcCenters()
        val left: Int = mComputedMinX.toInt() - self.getPaddingLeft()
        val top: Int = mComputedMinY.toInt() - self.getPaddingTop()
        val right: Int = mComputedMaxX.toInt() + self.getPaddingRight()
        val bottom: Int = mComputedMaxY.toInt() + self.getPaddingBottom()
        self.layout(left, top, right, bottom)
        transform()
    }

    private fun reCacheViews() {
        if (mContainer == null) {
            return
        }
        if (mCount == 0) {
            return
        }
        if (mViews == null || mViews!!.size != mCount) {
            mViews = arrayOfNulls<TView>(mCount)
        }
        for (i in 0 until mCount) {
            val id = mIds[i]
            mViews!![i] = mContainer?.getViewById(id)
        }
    }

    protected fun calcCenters() {
        if (mContainer == null) {
            return
        }
        if (!mNeedBounds) {
            if (!(Float.isNaN(mComputedCenterX) || Float.isNaN(mComputedCenterY))) {
                return
            }
        }
        if (Float.isNaN(mRotationCenterX) || Float.isNaN(mRotationCenterY)) {
            val views: Array<TView?>? = getViews(mContainer)
            var minx: Int = views?.get(0)?.getLeft() ?: 0
            var miny: Int = views?.get(0)?.getTop() ?: 0
            var maxx: Int = views?.get(0)?.getRight() ?: 0
            var maxy: Int = views?.get(0)?.getBottom() ?: 0
            for (i in 0 until mCount) {
                val view: TView? = views?.get(i)
                if(view == null)
                    continue
                minx = min(minx, view.getLeft())
                miny = min(miny, view.getTop())
                maxx = max(maxx, view.getRight())
                maxy = max(maxy, view.getBottom())
            }
            mComputedMaxX = maxx.toFloat()
            mComputedMaxY = maxy.toFloat()
            mComputedMinX = minx.toFloat()
            mComputedMinY = miny.toFloat()
            mComputedCenterX = if (Float.isNaN(mRotationCenterX)) {
                ((minx + maxx) / 2).toFloat()
            } else {
                mRotationCenterX
            }
            mComputedCenterY = if (Float.isNaN(mRotationCenterY)) {
                ((miny + maxy) / 2).toFloat()
            } else {
                mRotationCenterY
            }
        } else {
            mComputedCenterY = mRotationCenterY
            mComputedCenterX = mRotationCenterX
        }
    }

    private fun transform() {
        if (mContainer == null) {
            return
        }
        if (mViews == null) {
            reCacheViews()
        }
        calcCenters()
        val rad = if (Float.isNaN(mGroupRotateAngle)) 0.0 else toRadians(
            mGroupRotateAngle.toDouble()
        )
        val sin: Float = sin(rad).toFloat()
        val cos: Float = cos(rad).toFloat()
        val m11 = mScaleX * cos
        val m12 = -mScaleY * sin
        val m21 = mScaleX * sin
        val m22 = mScaleY * cos
        for (i in 0 until mCount) {
            val view: TView? = mViews?.get(i)
            if(view == null)
                continue
            val x: Int = (view.getLeft() + view.getRight()) / 2
            val y: Int = (view.getTop() + view.getBottom()) / 2
            val dx = x - mComputedCenterX
            val dy = y - mComputedCenterY
            val shiftx = m11 * dx + m12 * dy - dx + mShiftX
            val shifty = m21 * dx + m22 * dy - dy + mShiftY
            view.setTranslationX(shiftx)
            view.setTranslationY(shifty)
            view.setScaleY(mScaleY)
            view.setScaleX(mScaleX)
            if (!Float.isNaN(mGroupRotateAngle)) {
                view.setRotation(mGroupRotateAngle)
            }
        }
    }

    /**
     *
     * @param container
     */
    override fun applyLayoutFeaturesInConstraintSet(container: ConstraintLayout) {
        applyLayoutFeatures(container)
    }

    companion object {
        private const val TAG = "Layer"
    }
}