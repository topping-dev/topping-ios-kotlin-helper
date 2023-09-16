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
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.core.motion.utils.SplineSet

/**
 * Defines container for a key frame of for storing KeyAttributes.
 * KeyAttributes change post layout values of a view.
 *
 *
 */
class KeyAttributes : Key() {
    private var mTransitionEasing: String? = null

    /**
     * Gets the curve fit type this drives the interpolation
     *
     * @return
     */
    var curveFit = -1
        private set
    private var mVisibility = false
    private var mAlpha = Float.NaN
    private var mElevation = Float.NaN
    private var mRotation = Float.NaN
    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mPivotX = Float.NaN
    private var mPivotY = Float.NaN
    private var mTransitionPathRotate = Float.NaN
    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN
    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN
    private var mProgress = Float.NaN

    init {
        mType = KEY_TYPE
        mCustomConstraints = mutableMapOf()
    }

    override fun load(context: TContext, attrs: AttributeSet) {
        Loader.read(context, this, attrs)
    }

    override fun getAttributeNames(attributes: MutableSet<String>) {
        if (!Float.isNaN(mAlpha)) {
            attributes.add(Key.ALPHA)
        }
        if (!Float.isNaN(mElevation)) {
            attributes.add(Key.ELEVATION)
        }
        if (!Float.isNaN(mRotation)) {
            attributes.add(Key.ROTATION)
        }
        if (!Float.isNaN(mRotationX)) {
            attributes.add(Key.ROTATION_X)
        }
        if (!Float.isNaN(mRotationY)) {
            attributes.add(Key.ROTATION_Y)
        }
        if (!Float.isNaN(mPivotX)) {
            attributes.add(Key.PIVOT_X)
        }
        if (!Float.isNaN(mPivotY)) {
            attributes.add(Key.PIVOT_Y)
        }
        if (!Float.isNaN(mTranslationX)) {
            attributes.add(Key.TRANSLATION_X)
        }
        if (!Float.isNaN(mTranslationY)) {
            attributes.add(Key.TRANSLATION_Y)
        }
        if (!Float.isNaN(mTranslationZ)) {
            attributes.add(Key.TRANSLATION_Z)
        }
        if (!Float.isNaN(mTransitionPathRotate)) {
            attributes.add(Key.TRANSITION_PATH_ROTATE)
        }
        if (!Float.isNaN(mScaleX)) {
            attributes.add(Key.SCALE_X)
        }
        if (!Float.isNaN(mScaleY)) {
            attributes.add(Key.SCALE_Y)
        }
        if (!Float.isNaN(mProgress)) {
            attributes.add(Key.PROGRESS)
        }
        if (mCustomConstraints?.isNotEmpty() == true) {
            for (s in mCustomConstraints!!.keys) {
                attributes.add(Key.CUSTOM + "," + s)
            }
        }
    }

    /**
     * create the interpolations associated with this KeyAttribute
     * @param interpolation will be added to with keyAttributes
     */
    override fun setInterpolation(interpolation: MutableMap<String, Int>) {
        if (curveFit == -1) {
            return
        }
        if (!Float.isNaN(mAlpha)) {
            interpolation[Key.ALPHA] = curveFit
        }
        if (!Float.isNaN(mElevation)) {
            interpolation[Key.ELEVATION] = curveFit
        }
        if (!Float.isNaN(mRotation)) {
            interpolation[Key.ROTATION] = curveFit
        }
        if (!Float.isNaN(mRotationX)) {
            interpolation[Key.ROTATION_X] = curveFit
        }
        if (!Float.isNaN(mRotationY)) {
            interpolation[Key.ROTATION_Y] = curveFit
        }
        if (!Float.isNaN(mPivotX)) {
            interpolation[Key.PIVOT_X] = curveFit
        }
        if (!Float.isNaN(mPivotY)) {
            interpolation[Key.PIVOT_Y] = curveFit
        }
        if (!Float.isNaN(mTranslationX)) {
            interpolation[Key.TRANSLATION_X] = curveFit
        }
        if (!Float.isNaN(mTranslationY)) {
            interpolation[Key.TRANSLATION_Y] = curveFit
        }
        if (!Float.isNaN(mTranslationZ)) {
            interpolation[Key.TRANSLATION_Z] = curveFit
        }
        if (!Float.isNaN(mTransitionPathRotate)) {
            interpolation[Key.TRANSITION_PATH_ROTATE] = curveFit
        }
        if (!Float.isNaN(mScaleX)) {
            interpolation[Key.SCALE_X] = curveFit
        }
        if (!Float.isNaN(mScaleY)) {
            interpolation[Key.SCALE_Y] = curveFit
        }
        if (!Float.isNaN(mProgress)) {
            interpolation[Key.PROGRESS] = curveFit
        }
        if (mCustomConstraints?.isNotEmpty() == true) {
            for (s in mCustomConstraints!!.keys) {
                interpolation[Key.CUSTOM + "," + s] = curveFit
            }
        }
    }

    override fun addValues(splines: MutableMap<String, ViewSpline>) {
        for (s in splines.keys) {
            val splineSet: SplineSet = splines[s] ?: continue
            if (s.startsWith(Key.CUSTOM)) {
                val cKey: String = s.substring(Key.CUSTOM.length + 1)
                val cValue: ConstraintAttribute? = mCustomConstraints!!.get(cKey)
                if (cValue != null) {
                    (splineSet as ViewSpline.CustomSet).setPoint(mFramePosition, cValue)
                }
                continue
            }
            when (s) {
                Key.ALPHA -> if (!Float.isNaN(mAlpha)) {
                    splineSet.setPoint(mFramePosition, mAlpha)
                }
                Key.ELEVATION -> if (!Float.isNaN(mElevation)) {
                    splineSet.setPoint(mFramePosition, mElevation)
                }
                Key.ROTATION -> if (!Float.isNaN(mRotation)) {
                    splineSet.setPoint(mFramePosition, mRotation)
                }
                Key.ROTATION_X -> if (!Float.isNaN(mRotationX)) {
                    splineSet.setPoint(mFramePosition, mRotationX)
                }
                Key.ROTATION_Y -> if (!Float.isNaN(mRotationY)) {
                    splineSet.setPoint(mFramePosition, mRotationY)
                }
                Key.PIVOT_X -> if (!Float.isNaN(mRotationX)) {
                    splineSet.setPoint(mFramePosition, mPivotX)
                }
                Key.PIVOT_Y -> if (!Float.isNaN(mRotationY)) {
                    splineSet.setPoint(mFramePosition, mPivotY)
                }
                Key.TRANSITION_PATH_ROTATE -> if (!Float.isNaN(mTransitionPathRotate)) {
                    splineSet.setPoint(mFramePosition, mTransitionPathRotate)
                }
                Key.SCALE_X -> if (!Float.isNaN(mScaleX)) {
                    splineSet.setPoint(mFramePosition, mScaleX)
                }
                Key.SCALE_Y -> if (!Float.isNaN(mScaleY)) {
                    splineSet.setPoint(mFramePosition, mScaleY)
                }
                Key.TRANSLATION_X -> if (!Float.isNaN(mTranslationX)) {
                    splineSet.setPoint(mFramePosition, mTranslationX)
                }
                Key.TRANSLATION_Y -> if (!Float.isNaN(mTranslationY)) {
                    splineSet.setPoint(mFramePosition, mTranslationY)
                }
                Key.TRANSLATION_Z -> if (!Float.isNaN(mTranslationZ)) {
                    splineSet.setPoint(mFramePosition, mTranslationZ)
                }
                Key.PROGRESS -> if (!Float.isNaN(mProgress)) {
                    splineSet.setPoint(mFramePosition, mProgress)
                }
                else -> if (I_DEBUG) {
                    Log.v(TAG, "UNKNOWN addValues \"$s\"")
                }
            }
        }
    }

    override fun setValue(tag: String, value: Any) {
        when (tag) {
            ALPHA -> mAlpha = toFloat(value)
            CURVEFIT -> curveFit = toInt(value)
            ELEVATION -> mElevation = toFloat(value)
            MOTIONPROGRESS -> mProgress = toFloat(value)
            ROTATION -> mRotation = toFloat(value)
            ROTATION_X -> mRotationX = toFloat(value)
            ROTATION_Y -> mRotationY = toFloat(value)
            PIVOT_X -> mPivotX = toFloat(value)
            PIVOT_Y -> mPivotY = toFloat(value)
            SCALE_X -> mScaleX = toFloat(value)
            SCALE_Y -> mScaleY = toFloat(value)
            TRANSITIONEASING -> mTransitionEasing = value.toString()
            VISIBILITY -> mVisibility = toBoolean(value)
            TRANSITION_PATH_ROTATE -> mTransitionPathRotate = toFloat(value)
            TRANSLATION_X -> mTranslationX = toFloat(value)
            TRANSLATION_Y -> mTranslationY = toFloat(value)
            TRANSLATION_Z -> mTranslationZ = toFloat(value)
        }
    }

    private object Loader {
        private const val ANDROID_ALPHA = 1
        private const val ANDROID_ELEVATION = 2
        private const val ANDROID_ROTATION = 4
        private const val ANDROID_ROTATION_X = 5
        private const val ANDROID_ROTATION_Y = 6
        private const val TRANSITION_PATH_ROTATE = 8
        private const val ANDROID_SCALE_X = 7
        private const val TRANSITION_EASING = 9
        private const val TARGET_ID = 10
        private const val FRAME_POSITION = 12
        private const val CURVE_FIT = 13
        private const val ANDROID_SCALE_Y = 14
        private const val ANDROID_TRANSLATION_X = 15
        private const val ANDROID_TRANSLATION_Y = 16
        private const val ANDROID_TRANSLATION_Z = 17
        private const val PROGRESS = 18
        private const val ANDROID_PIVOT_X = 19
        private const val ANDROID_PIVOT_Y = 20
        private val sAttrMap: MutableMap<String, Int> = mutableMapOf()

        init {
            sAttrMap["android_alpha"] = ANDROID_ALPHA
            sAttrMap["android_elevation"] = ANDROID_ELEVATION
            sAttrMap["android_rotation"] = ANDROID_ROTATION
            sAttrMap["android_rotationX"] = ANDROID_ROTATION_X
            sAttrMap["android_rotationY"] = ANDROID_ROTATION_Y
            sAttrMap["android_transformPivotX"] = ANDROID_PIVOT_X
            sAttrMap["android_transformPivotY"] = ANDROID_PIVOT_Y
            sAttrMap["android_scaleX"] = ANDROID_SCALE_X
            sAttrMap["transitionPathRotate"] = TRANSITION_PATH_ROTATE
            sAttrMap["transitionEasing"] = TRANSITION_EASING
            sAttrMap["motionTarget"] = TARGET_ID
            sAttrMap["framePosition"] = FRAME_POSITION
            sAttrMap["curveFit"] = CURVE_FIT
            sAttrMap["android_scaleY"] = ANDROID_SCALE_Y
            sAttrMap["android_translationX"] = ANDROID_TRANSLATION_X
            sAttrMap["android_translationY"] = ANDROID_TRANSLATION_Y
            sAttrMap["android_translationZ"] = ANDROID_TRANSLATION_Z
            sAttrMap["motionProgress"] = PROGRESS
        }

        fun read(context: TContext, c: KeyAttributes, a: AttributeSet) {
            a.forEach { kvp ->
                val intValue = sAttrMap[kvp.key]
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
                    FRAME_POSITION -> c.mFramePosition = context.getResources().getInt(kvp.value, c.mFramePosition)
                    ANDROID_ALPHA -> c.mAlpha = context.getResources().getFloat(kvp.value, c.mAlpha)
                    ANDROID_ELEVATION -> c.mElevation = context.getResources().getDimension(kvp.value, c.mElevation)
                    ANDROID_ROTATION -> c.mRotation = context.getResources().getFloat(kvp.value, c.mRotation)
                    CURVE_FIT -> c.curveFit = context.getResources().getInt(kvp.value, c.curveFit)
                    ANDROID_SCALE_X -> c.mScaleX = context.getResources().getFloat(kvp.value, c.mScaleX)
                    ANDROID_SCALE_Y -> c.mScaleY = context.getResources().getFloat(kvp.value, c.mScaleY)
                    ANDROID_ROTATION_X -> c.mRotationX = context.getResources().getFloat(kvp.value, c.mRotationX)
                    ANDROID_ROTATION_Y -> c.mRotationY = context.getResources().getFloat(kvp.value, c.mRotationY)
                    ANDROID_PIVOT_X -> c.mPivotX = context.getResources().getDimension(kvp.value, c.mPivotX)
                    ANDROID_PIVOT_Y -> c.mPivotY = context.getResources().getDimension(kvp.value, c.mPivotY)
                    TRANSITION_EASING -> c.mTransitionEasing = context.getResources().getString(
                        kvp.key,
                        kvp.value
                    )
                    TRANSITION_PATH_ROTATE -> c.mTransitionPathRotate =
                        context.getResources().getFloat(kvp.value, c.mTransitionPathRotate)
                    ANDROID_TRANSLATION_X -> c.mTranslationX = context.getResources().getDimension(kvp.value, c.mTranslationX)
                    ANDROID_TRANSLATION_Y -> c.mTranslationY = context.getResources().getDimension(kvp.value, c.mTranslationY)
                    ANDROID_TRANSLATION_Z -> c.mTranslationZ = context.getResources().getDimension(kvp.value, c.mTranslationZ)
                    PROGRESS -> c.mProgress = context.getResources().getFloat(kvp.value, c.mProgress)
                    else -> Log.e(
                        NAME, "unused attribute $kvp.value"
                    )
                }
            }
        }
    }

    /**
     * Copy the key
     * @param src to be copied
     * @return self
     */
    override fun copy(src: Key): Key {
        super.copy(src)
        val k = src as KeyAttributes
        curveFit = k.curveFit
        mVisibility = k.mVisibility
        mAlpha = k.mAlpha
        mElevation = k.mElevation
        mRotation = k.mRotation
        mRotationX = k.mRotationX
        mRotationY = k.mRotationY
        mPivotX = k.mPivotX
        mPivotY = k.mPivotY
        mTransitionPathRotate = k.mTransitionPathRotate
        mScaleX = k.mScaleX
        mScaleY = k.mScaleY
        mTranslationX = k.mTranslationX
        mTranslationY = k.mTranslationY
        mTranslationZ = k.mTranslationZ
        mProgress = k.mProgress
        mTransitionEasing = k.mTransitionEasing
        return this
    }

    /**
     * Clone this KeyAttributes
     * @return
     */
    override fun clone(): Key {
        return KeyAttributes().copy(this)
    }

    companion object {
        const val NAME = "KeyAttribute"
        private const val TAG = "KeyAttributes"
        private const val I_DEBUG = false
        const val KEY_TYPE = 1
    }
}