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
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewOscillator
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewSpline
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.core.motion.utils.Oscillator
import dev.topping.ios.constraint.core.motion.utils.SplineSet

/**
 * Provide the passive data structure to get KeyPosition information form XML
 *
 *
 */
class KeyCycle : Key() {
    private var mTransitionEasing: String? = null
    private var mCurveFit = 0
    private var mWaveShape = -1
    private var mCustomWaveShape: String? = null
    private var mWavePeriod = Float.NaN
    private var mWaveOffset = 0f
    private var mWavePhase = 0f
    private var mProgress = Float.NaN
    private var mWaveVariesBy = -1
    private var mAlpha = Float.NaN
    private var mElevation = Float.NaN
    private var mRotation = Float.NaN
    private var mTransitionPathRotate = Float.NaN
    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN
    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN

    init {
        mType = KEY_TYPE
        mCustomConstraints = mutableMapOf()
    }

    /**
     * Load the KeyCycle from xml attributes
     * @param context
     * @param attrs
     */
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
        if (!Float.isNaN(mScaleX)) {
            attributes.add(Key.SCALE_X)
        }
        if (!Float.isNaN(mScaleY)) {
            attributes.add(Key.SCALE_Y)
        }
        if (!Float.isNaN(mTransitionPathRotate)) {
            attributes.add(Key.TRANSITION_PATH_ROTATE)
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
        if (mCustomConstraints?.isNotEmpty() == true) {
            for (s in mCustomConstraints!!.keys) {
                attributes.add(Key.CUSTOM + "," + s)
            }
        }
    }

    /**
     * Add this key cycle to the keyCycle engines
     * @param oscSet
     */
    fun addCycleValues(oscSet: MutableMap<String, ViewOscillator>) {
        for (key in oscSet.keys) {
            if (key.startsWith(Key.CUSTOM)) {
                val customKey: String = key.substring(Key.CUSTOM.length + 1)
                val cValue: ConstraintAttribute? = mCustomConstraints?.get(customKey)
                if (cValue == null
                    || cValue.type !== ConstraintAttribute.AttributeType.FLOAT_TYPE
                ) {
                    continue
                }
                val osc: ViewOscillator = oscSet[key] ?: continue
                osc.setPoint(
                    mFramePosition, mWaveShape, mCustomWaveShape, mWaveVariesBy,
                    mWavePeriod, mWaveOffset, mWavePhase,
                    cValue.valueToInterpolate, cValue
                )
                continue
            }
            val value = getValue(key)
            if (Float.isNaN(value)) {
                continue
            }
            val osc: ViewOscillator = oscSet[key] ?: continue
            osc.setPoint(
                mFramePosition, mWaveShape, mCustomWaveShape, mWaveVariesBy,
                mWavePeriod, mWaveOffset, mWavePhase, value
            )
        }
    }

    /**
     * get the value for a given attribute of the keyCycel
     * @param key
     * @return
     */
    fun getValue(key: String): Float {
        return when (key) {
            Key.ALPHA -> mAlpha
            Key.ELEVATION -> mElevation
            Key.ROTATION -> mRotation
            Key.ROTATION_X -> mRotationX
            Key.ROTATION_Y -> mRotationY
            Key.TRANSITION_PATH_ROTATE -> mTransitionPathRotate
            Key.SCALE_X -> mScaleX
            Key.SCALE_Y -> mScaleY
            Key.TRANSLATION_X -> mTranslationX
            Key.TRANSLATION_Y -> mTranslationY
            Key.TRANSLATION_Z -> mTranslationZ
            Key.WAVE_OFFSET -> mWaveOffset
            Key.WAVE_PHASE -> mWavePhase
            Key.PROGRESS -> mProgress
            else -> {
                if (!key.startsWith("CUSTOM")) {
                    Log.v("WARNING! KeyCycle", "  UNKNOWN  $key")
                }
                Float.NaN
            }
        }
    }

    override fun addValues(splines: MutableMap<String, ViewSpline>)
    {
        Debug.logStack(TAG, "add " + splines.size.toString() + " values", 2)
        for (s in splines.keys) {
            val splineSet: SplineSet = splines[s] ?: continue
            when (s) {
                Key.ALPHA -> splineSet.setPoint(mFramePosition, mAlpha)
                Key.ELEVATION -> splineSet.setPoint(mFramePosition, mElevation)
                Key.ROTATION -> splineSet.setPoint(mFramePosition, mRotation)
                Key.ROTATION_X -> splineSet.setPoint(mFramePosition, mRotationX)
                Key.ROTATION_Y -> splineSet.setPoint(mFramePosition, mRotationY)
                Key.TRANSITION_PATH_ROTATE -> splineSet.setPoint(
                    mFramePosition,
                    mTransitionPathRotate
                )
                Key.SCALE_X -> splineSet.setPoint(mFramePosition, mScaleX)
                Key.SCALE_Y -> splineSet.setPoint(mFramePosition, mScaleY)
                Key.TRANSLATION_X -> splineSet.setPoint(mFramePosition, mTranslationX)
                Key.TRANSLATION_Y -> splineSet.setPoint(mFramePosition, mTranslationY)
                Key.TRANSLATION_Z -> splineSet.setPoint(mFramePosition, mTranslationZ)
                Key.WAVE_OFFSET -> splineSet.setPoint(mFramePosition, mWaveOffset)
                Key.WAVE_PHASE -> splineSet.setPoint(mFramePosition, mWavePhase)
                Key.PROGRESS -> splineSet.setPoint(mFramePosition, mProgress)
                else -> if (!s.startsWith("CUSTOM")) {
                    Log.v("WARNING KeyCycle", "  UNKNOWN  $s")
                }
            }
        }
    }

    private object Loader {
        private const val TARGET_ID = 1
        private const val FRAME_POSITION = 2
        private const val TRANSITION_EASING = 3
        private const val CURVE_FIT = 4
        private const val WAVE_SHAPE = 5
        private const val WAVE_PERIOD = 6
        private const val WAVE_OFFSET = 7
        private const val WAVE_VARIES_BY = 8
        private const val ANDROID_ALPHA = 9
        private const val ANDROID_ELEVATION = 10
        private const val ANDROID_ROTATION = 11
        private const val ANDROID_ROTATION_X = 12
        private const val ANDROID_ROTATION_Y = 13
        private const val TRANSITION_PATH_ROTATE = 14
        private const val ANDROID_SCALE_X = 15
        private const val ANDROID_SCALE_Y = 16
        private const val ANDROID_TRANSLATION_X = 17
        private const val ANDROID_TRANSLATION_Y = 18
        private const val ANDROID_TRANSLATION_Z = 19
        private const val PROGRESS = 20
        private const val WAVE_PHASE = 21
        private val sAttrMap: MutableMap<String, Int> = mutableMapOf()

        init {
            sAttrMap["app_motionTarget"] = TARGET_ID
            sAttrMap["app_framePosition"] = FRAME_POSITION
            sAttrMap["app_transitionEasing"] = TRANSITION_EASING
            sAttrMap["app_curveFit"] = CURVE_FIT
            sAttrMap["app_waveShape"] = WAVE_SHAPE
            sAttrMap["app_wavePeriod"] = WAVE_PERIOD
            sAttrMap["app_waveOffset"] = WAVE_OFFSET
            sAttrMap["app_waveVariesBy"] = WAVE_VARIES_BY
            sAttrMap["android_alpha"] = ANDROID_ALPHA
            sAttrMap["android_elevation"] = ANDROID_ELEVATION
            sAttrMap["android_rotation"] = ANDROID_ROTATION
            sAttrMap["android_rotationX"] = ANDROID_ROTATION_X
            sAttrMap["android_rotationY"] = ANDROID_ROTATION_Y
            sAttrMap["app_transitionPathRotate"] = TRANSITION_PATH_ROTATE
            sAttrMap["android_scaleX"] = ANDROID_SCALE_X
            sAttrMap["android_scaleY"] = ANDROID_SCALE_Y
            sAttrMap["android_translationX"] = ANDROID_TRANSLATION_X
            sAttrMap["android_translationY"] = ANDROID_TRANSLATION_Y
            sAttrMap["android_translationZ"] = ANDROID_TRANSLATION_Z
            sAttrMap["app_motionProgress"] = PROGRESS
            sAttrMap["app_wavePhase"] = WAVE_PHASE
        }

        fun read(context: TContext, c: KeyCycle, a: AttributeSet) {
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
                    TRANSITION_EASING -> c.mTransitionEasing = context.getResources().getString(kvp.value)
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
                            c.mWaveOffset = context.getResources().getDimension(kvp.value, c.mWaveOffset)
                        } else {
                            c.mWaveOffset = context.getResources().getFloat(kvp.value, c.mWaveOffset)
                        }
                    }
                    WAVE_VARIES_BY -> c.mWaveVariesBy = context.getResources().getInt(kvp.value, c.mWaveVariesBy)
                    ANDROID_ALPHA -> c.mAlpha = context.getResources().getFloat(kvp.value, c.mAlpha)
                    ANDROID_ELEVATION -> c.mElevation = context.getResources().getDimension(kvp.value, c.mElevation)
                    ANDROID_ROTATION -> c.mRotation = context.getResources().getFloat(kvp.value, c.mRotation)
                    ANDROID_ROTATION_X -> c.mRotationX = context.getResources().getFloat(kvp.value, c.mRotationX)
                    ANDROID_ROTATION_Y -> c.mRotationY = context.getResources().getFloat(kvp.value, c.mRotationY)
                    TRANSITION_PATH_ROTATE -> c.mTransitionPathRotate =
                        context.getResources().getFloat(kvp.value, c.mTransitionPathRotate)
                    ANDROID_SCALE_X -> c.mScaleX = context.getResources().getFloat(kvp.value, c.mScaleX)
                    ANDROID_SCALE_Y -> c.mScaleY = context.getResources().getFloat(kvp.value, c.mScaleY)
                    ANDROID_TRANSLATION_X -> c.mTranslationX = context.getResources().getDimension(kvp.value, c.mTranslationX)
                    ANDROID_TRANSLATION_Y -> c.mTranslationY = context.getResources().getDimension(kvp.value, c.mTranslationY)
                    ANDROID_TRANSLATION_Z -> c.mTranslationZ = context.getResources().getDimension(kvp.value, c.mTranslationZ)
                    PROGRESS -> c.mProgress = context.getResources().getFloat(kvp.value, c.mProgress)
                    WAVE_PHASE -> c.mWavePhase = context.getResources().getFloat(kvp.value, c.mWavePhase) / 360
                    else -> Log.e(
                        TAG, "unused attribute ${kvp.value}")
                }
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
            WAVE_PHASE -> mWavePhase = toFloat(value)
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
     * @param src to be copied
     * @return self
     */
    override fun copy(src: Key): Key {
        super.copy(src)
        val k = src as KeyCycle
        mTransitionEasing = k.mTransitionEasing
        mCurveFit = k.mCurveFit
        mWaveShape = k.mWaveShape
        mCustomWaveShape = k.mCustomWaveShape
        mWavePeriod = k.mWavePeriod
        mWaveOffset = k.mWaveOffset
        mWavePhase = k.mWavePhase
        mProgress = k.mProgress
        mWaveVariesBy = k.mWaveVariesBy
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
        return this
    }

    /**
     * Clone this KeyAttributes
     * @return
     */
    override fun clone(): Key {
        return KeyCycle().copy(this)
    }

    companion object {
        private const val TAG = "KeyCycle"
        const val NAME = "KeyCycle"
        const val WAVE_PERIOD = "wavePeriod"
        const val WAVE_OFFSET = "waveOffset"
        const val WAVE_PHASE = "wavePhase"
        const val WAVE_SHAPE = "waveShape"
        val SHAPE_SIN_WAVE: Int = Oscillator.SIN_WAVE
        val SHAPE_SQUARE_WAVE: Int = Oscillator.SQUARE_WAVE
        val SHAPE_TRIANGLE_WAVE: Int = Oscillator.TRIANGLE_WAVE
        val SHAPE_SAW_WAVE: Int = Oscillator.SAW_WAVE
        val SHAPE_REVERSE_SAW_WAVE: Int = Oscillator.REVERSE_SAW_WAVE
        val SHAPE_COS_WAVE: Int = Oscillator.COS_WAVE
        val SHAPE_BOUNCE: Int = Oscillator.BOUNCE
        const val KEY_TYPE = 4
    }
}