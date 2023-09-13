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
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewTimeCycle
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.core.motion.utils.Oscillator

/**
 * Defines container for a key frame of for storing KeyTimeCycles.
 * KeyTimeCycles change post layout values of a view.
 *
 *
 */
class KeyTimeCycle : Key() {
    private var mTransitionEasing: String? = null
    private var mCurveFit = -1
    private var mAlpha = Float.NaN
    private var mElevation = Float.NaN
    private var mRotation = Float.NaN
    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mTransitionPathRotate = Float.NaN
    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN
    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN
    private var mProgress = Float.NaN
    private var mWaveShape = 0
    private var mCustomWaveShape: String? = null // TODO add support of custom wave shapes
    private var mWavePeriod = Float.NaN
    private var mWaveOffset = 0f

    init {
        mType = KEY_TYPE
        mCustomConstraints = mutableMapOf()
    }

    override fun load(context: TContext, attrs: AttributeSet) {
        Loader.read(context, this, attrs)
    }

    /**
     * Gets the curve fit type this drives the interpolation
     */
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
     * put key and position into the interpolation map
     *
     * @param interpolation
     */
    override fun setInterpolation(interpolation: MutableMap<String, Int>) {
        if (mCurveFit == -1) {
            return
        }
        if (!Float.isNaN(mAlpha)) {
            interpolation[Key.ALPHA] = mCurveFit
        }
        if (!Float.isNaN(mElevation)) {
            interpolation[Key.ELEVATION] = mCurveFit
        }
        if (!Float.isNaN(mRotation)) {
            interpolation[Key.ROTATION] = mCurveFit
        }
        if (!Float.isNaN(mRotationX)) {
            interpolation[Key.ROTATION_X] = mCurveFit
        }
        if (!Float.isNaN(mRotationY)) {
            interpolation[Key.ROTATION_Y] = mCurveFit
        }
        if (!Float.isNaN(mTranslationX)) {
            interpolation[Key.TRANSLATION_X] = mCurveFit
        }
        if (!Float.isNaN(mTranslationY)) {
            interpolation[Key.TRANSLATION_Y] = mCurveFit
        }
        if (!Float.isNaN(mTranslationZ)) {
            interpolation[Key.TRANSLATION_Z] = mCurveFit
        }
        if (!Float.isNaN(mTransitionPathRotate)) {
            interpolation[Key.TRANSITION_PATH_ROTATE] = mCurveFit
        }
        if (!Float.isNaN(mScaleX)) {
            interpolation[Key.SCALE_X] = mCurveFit
        }
        if (!Float.isNaN(mScaleX)) {
            interpolation[Key.SCALE_Y] = mCurveFit
        }
        if (!Float.isNaN(mProgress)) {
            interpolation[Key.PROGRESS] = mCurveFit
        }
        if (mCustomConstraints?.isNotEmpty() == true) {
            for (s in mCustomConstraints!!.keys) {
                interpolation[Key.CUSTOM + "," + s] = mCurveFit
            }
        }
    }

    override fun addValues(splines: MutableMap<String, ViewSpline>) {
        // This should not get called
        throw IllegalArgumentException(" KeyTimeCycles do not support SplineSet")
    }

    /**
     * Add values to TimeCycle Map
     *
     * @param splines
     */
    fun addTimeValues(splines: MutableMap<String, ViewTimeCycle>) {
        for (s in splines.keys) {
            val splineSet: ViewTimeCycle = splines!![s] ?: continue
            if (s.startsWith(Key.CUSTOM)) {
                val cKey: String = s.substring(Key.CUSTOM.length + 1)
                val cValue: ConstraintAttribute? = mCustomConstraints?.get(cKey)
                if (cValue != null) {
                    (splineSet as ViewTimeCycle.CustomSet).setPoint(
                        mFramePosition, cValue,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                continue
            }
            when (s) {
                Key.ALPHA -> if (!Float.isNaN(mAlpha)) {
                    splineSet.setPoint(
                        mFramePosition, mAlpha,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.ELEVATION -> if (!Float.isNaN(mElevation)) {
                    splineSet.setPoint(
                        mFramePosition, mElevation,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.ROTATION -> if (!Float.isNaN(mRotation)) {
                    splineSet.setPoint(
                        mFramePosition, mRotation,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.ROTATION_X -> if (!Float.isNaN(mRotationX)) {
                    splineSet.setPoint(
                        mFramePosition, mRotationX,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.ROTATION_Y -> if (!Float.isNaN(mRotationY)) {
                    splineSet.setPoint(
                        mFramePosition, mRotationY,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.TRANSITION_PATH_ROTATE -> if (!Float.isNaN(mTransitionPathRotate)) {
                    splineSet.setPoint(
                        mFramePosition, mTransitionPathRotate,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.SCALE_X -> if (!Float.isNaN(mScaleX)) {
                    splineSet.setPoint(
                        mFramePosition, mScaleX,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.SCALE_Y -> if (!Float.isNaN(mScaleY)) {
                    splineSet.setPoint(
                        mFramePosition, mScaleY,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.TRANSLATION_X -> if (!Float.isNaN(mTranslationX)) {
                    splineSet.setPoint(
                        mFramePosition, mTranslationX,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.TRANSLATION_Y -> if (!Float.isNaN(mTranslationY)) {
                    splineSet.setPoint(
                        mFramePosition, mTranslationY,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.TRANSLATION_Z -> if (!Float.isNaN(mTranslationZ)) {
                    splineSet.setPoint(
                        mFramePosition, mTranslationZ,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                Key.PROGRESS -> if (!Float.isNaN(mProgress)) {
                    splineSet.setPoint(
                        mFramePosition, mProgress,
                        mWavePeriod, mWaveShape, mWaveOffset
                    )
                }
                else -> Log.e("KeyTimeCycles", "UNKNOWN addValues \"$s\"")
            }
        }
    }

    override fun setValue(tag: String, value: Any) {
        when (tag) {
            Key.ALPHA -> mAlpha = toFloat(value)
            CURVEFIT -> mCurveFit = toInt(value)
            ELEVATION -> mElevation = toFloat(value)
            MOTIONPROGRESS -> mProgress = toFloat(value)
            ROTATION -> mRotation = toFloat(value)
            ROTATION_X -> mRotationX = toFloat(value)
            ROTATION_Y -> mRotationY = toFloat(value)
            SCALE_X -> mScaleX = toFloat(value)
            SCALE_Y -> mScaleY = toFloat(value)
            TRANSITIONEASING -> mTransitionEasing = value.toString()
            TRANSITION_PATH_ROTATE -> mTransitionPathRotate = toFloat(value)
            TRANSLATION_X -> mTranslationX = toFloat(value)
            TRANSLATION_Y -> mTranslationY = toFloat(value)
            TRANSLATION_Z -> mTranslationZ = toFloat(value)
            WAVE_PERIOD -> mWavePeriod = toFloat(value)
            WAVE_OFFSET -> mWaveOffset = toFloat(value)
            WAVE_SHAPE -> if (value is Int) {
                mWaveShape = toInt(value)
            } else {
                mWaveShape = Oscillator.CUSTOM
                mCustomWaveShape = value.toString()
            }
        }
    }

    /**
     * Copy the key
     *
     * @param src to be copied
     * @return self
     */
    override fun copy(src: Key): Key {
        super.copy(src)
        val k = src as KeyTimeCycle
        mTransitionEasing = k.mTransitionEasing
        mCurveFit = k.mCurveFit
        mWaveShape = k.mWaveShape
        mWavePeriod = k.mWavePeriod
        mWaveOffset = k.mWaveOffset
        mProgress = k.mProgress
        mAlpha = k.mAlpha
        mElevation = k.mElevation
        mRotation = k.mRotation
        mTransitionPathRotate = k.mTransitionPathRotate
        mRotationX = k.mRotationX
        mRotationY = k.mRotationY
        mScaleX = k.mScaleX
        mScaleY = k.mScaleY
        mTranslationX = k.mTranslationX
        mTranslationY = k.mTranslationY
        mTranslationZ = k.mTranslationZ
        mCustomWaveShape = k.mCustomWaveShape
        return this
    }

    /**
     * Clone this KeyAttributes
     *
     * @return
     */
    override fun clone(): Key {
        return KeyTimeCycle().copy(this)
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
        private const val WAVE_SHAPE = 19
        private const val WAVE_PERIOD = 20
        private const val WAVE_OFFSET = 21
        private val sAttrMap: MutableMap<String, Int> = mutableMapOf()

        init {
            sAttrMap["android_alpha"] = ANDROID_ALPHA
            sAttrMap["android_elevation"] = ANDROID_ELEVATION
            sAttrMap["android_rotation"] = ANDROID_ROTATION
            sAttrMap["android_rotationX"] = ANDROID_ROTATION_X
            sAttrMap["android_rotationY"] = ANDROID_ROTATION_Y
            sAttrMap["android_scaleX"] = ANDROID_SCALE_X
            sAttrMap["app_transitionPathRotate"] = TRANSITION_PATH_ROTATE
            sAttrMap["app_transitionEasing"] = TRANSITION_EASING
            sAttrMap["app_motionTarget"] = TARGET_ID
            sAttrMap["app_framePosition"] = FRAME_POSITION
            sAttrMap["app_curveFit"] = CURVE_FIT
            sAttrMap["android_scaleY"] = ANDROID_SCALE_Y
            sAttrMap["android_translationX"] = ANDROID_TRANSLATION_X
            sAttrMap["android_translationY"] = ANDROID_TRANSLATION_Y
            sAttrMap["android_translationZ"] = ANDROID_TRANSLATION_Z
            sAttrMap["app_motionProgress"] = PROGRESS
            sAttrMap["app_wavePeriod"] = WAVE_PERIOD
            sAttrMap["app_waveOffset"] = WAVE_OFFSET
            sAttrMap["app_waveShape"] = WAVE_SHAPE
        }

        fun read(context: TContext, c: KeyTimeCycle, a: AttributeSet) {
            a.forEach { kvp ->
                val intValue = Loader.sAttrMap[kvp.key]
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
                    CURVE_FIT -> c.mCurveFit = context.getResources().getInt(kvp.value, c.mCurveFit)
                    WAVE_SHAPE -> if (context.getResources().getType(kvp.value) == "string") {
                        c.mCustomWaveShape = context.getResources().getString(kvp.value)
                        c.mWaveShape = Oscillator.CUSTOM
                    } else {
                        c.mWaveShape = context.getResources().getInt(kvp.value, c.mWaveShape)
                    }
                    WAVE_PERIOD -> c.mWavePeriod = context.getResources().getFloat(kvp.value, c.mWavePeriod)
                    WAVE_OFFSET -> {
                        if (context.getResources().getType(kvp.value) == "dimension") {
                            c.mWaveOffset =context.getResources().getDimension(kvp.value, c.mWaveOffset)
                        } else {
                            c.mWaveOffset = context.getResources().getFloat(kvp.value, c.mWaveOffset)
                        }
                    }
                    ANDROID_SCALE_X -> c.mScaleX = context.getResources().getFloat(kvp.value, c.mScaleX)
                    ANDROID_ROTATION_X -> c.mRotationX = context.getResources().getFloat(kvp.value, c.mRotationX)
                    ANDROID_ROTATION_Y -> c.mRotationY = context.getResources().getFloat(kvp.value, c.mRotationY)
                    TRANSITION_EASING -> c.mTransitionEasing = context.getResources().getString(kvp.value)
                    ANDROID_SCALE_Y -> c.mScaleY = context.getResources().getFloat(kvp.value, c.mScaleY)
                    TRANSITION_PATH_ROTATE -> c.mTransitionPathRotate =
                        context.getResources().getFloat(kvp.value, c.mTransitionPathRotate)
                    ANDROID_TRANSLATION_X -> c.mTranslationX = context.getResources().getDimension(kvp.value, c.mTranslationX)
                    ANDROID_TRANSLATION_Y -> c.mTranslationY = context.getResources().getDimension(kvp.value, c.mTranslationY)
                    ANDROID_TRANSLATION_Z -> c.mTranslationZ = context.getResources().getDimension(kvp.value, c.mTranslationZ)
                    PROGRESS -> c.mProgress = context.getResources().getFloat(kvp.value, c.mProgress)
                    else -> Log.e(
                        NAME, "unused attribute " + sAttrMap[kvp.value]
                    )
                }
            }
        }
    }

    companion object {
        const val WAVE_PERIOD = "wavePeriod"
        const val WAVE_OFFSET = "waveOffset"
        const val WAVE_SHAPE = "waveShape"
        val SHAPE_SIN_WAVE: Int = Oscillator.SIN_WAVE
        val SHAPE_SQUARE_WAVE: Int = Oscillator.SQUARE_WAVE
        val SHAPE_TRIANGLE_WAVE: Int = Oscillator.TRIANGLE_WAVE
        val SHAPE_SAW_WAVE: Int = Oscillator.SAW_WAVE
        val SHAPE_REVERSE_SAW_WAVE: Int = Oscillator.REVERSE_SAW_WAVE
        val SHAPE_COS_WAVE: Int = Oscillator.COS_WAVE
        val SHAPE_BOUNCE: Int = Oscillator.BOUNCE
        const val KEY_TYPE = 3
        const val NAME = "KeyTimeCycle"
        private const val TAG = NAME
    }
}