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

import dev.topping.ios.constraint.core.motion.CustomVariable
import dev.topping.ios.constraint.core.motion.utils.*
import dev.topping.ios.constraint.isNaN

class MotionKeyCycle : MotionKey() {
    
    private var mTransitionEasing: String? = null

    
    private var mCurveFit = 0
    private var mWaveShape = -1
    private var mCustomWaveShape: String? = null
    private var mWavePeriod = Float.NaN
    private var mWaveOffset = 0f
    private var mWavePhase = 0f
    private var mProgress = Float.NaN
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
        mCustom = mutableMapOf()
    }

    
    override fun getAttributeNames(attributes: MutableSet<String>?) {
        attributes?.let {
            if (!Float.isNaN(mAlpha)) {
                attributes.add(TypedValues.CycleType.S_ALPHA)
            }
            if (!Float.isNaN(mElevation)) {
                attributes.add(TypedValues.CycleType.S_ELEVATION)
            }
            if (!Float.isNaN(mRotation)) {
                attributes.add(TypedValues.CycleType.S_ROTATION_Z)
            }
            if (!Float.isNaN(mRotationX)) {
                attributes.add(TypedValues.CycleType.S_ROTATION_X)
            }
            if (!Float.isNaN(mRotationY)) {
                attributes.add(TypedValues.CycleType.S_ROTATION_Y)
            }
            if (!Float.isNaN(mScaleX)) {
                attributes.add(TypedValues.CycleType.S_SCALE_X)
            }
            if (!Float.isNaN(mScaleY)) {
                attributes.add(TypedValues.CycleType.S_SCALE_Y)
            }
            if (!Float.isNaN(mTransitionPathRotate)) {
                attributes.add(TypedValues.CycleType.S_PATH_ROTATE)
            }
            if (!Float.isNaN(mTranslationX)) {
                attributes.add(TypedValues.CycleType.S_TRANSLATION_X)
            }
            if (!Float.isNaN(mTranslationY)) {
                attributes.add(TypedValues.CycleType.S_TRANSLATION_Y)
            }
            if (!Float.isNaN(mTranslationZ)) {
                attributes.add(TypedValues.CycleType.S_TRANSLATION_Z)
            }
            if (mCustom?.isNotEmpty() == true) {
                for (s in mCustom!!.keys) {
                    attributes.add(TypedValues.S_CUSTOM + "," + s)
                }
            }
        }
    }

    
    override fun addValues(splines: MutableMap<String, SplineSet>?) {
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: Int): Boolean {
        return when (type) {
            TypedValues.CycleType.TYPE_CURVE_FIT -> {
                mCurveFit = value
                true
            }
            TypedValues.CycleType.TYPE_WAVE_SHAPE -> {
                mWaveShape = value
                true
            }
            else -> {
                val ret = setValue(type, value.toFloat())
                if (ret) {
                    true
                } else super.setValue(type, value)
            }
        }
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: String): Boolean {
        return when (type) {
            TypedValues.CycleType.TYPE_EASING -> {
                mTransitionEasing = value
                true
            }
            TypedValues.CycleType.TYPE_CUSTOM_WAVE_SHAPE -> {
                mCustomWaveShape = value
                true
            }
            else -> super.setValue(type, value)
        }
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            TypedValues.CycleType.TYPE_ALPHA -> mAlpha = value
            TypedValues.CycleType.TYPE_TRANSLATION_X -> mTranslationX = value
            TypedValues.CycleType.TYPE_TRANSLATION_Y -> mTranslationY = value
            TypedValues.CycleType.TYPE_TRANSLATION_Z -> mTranslationZ = value
            TypedValues.CycleType.TYPE_ELEVATION -> mElevation = value
            TypedValues.CycleType.TYPE_ROTATION_X -> mRotationX = value
            TypedValues.CycleType.TYPE_ROTATION_Y -> mRotationY = value
            TypedValues.CycleType.TYPE_ROTATION_Z -> mRotation = value
            TypedValues.CycleType.TYPE_SCALE_X -> mScaleX = value
            TypedValues.CycleType.TYPE_SCALE_Y -> mScaleY = value
            TypedValues.CycleType.TYPE_PROGRESS -> mProgress = value
            TypedValues.CycleType.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            TypedValues.CycleType.TYPE_WAVE_PERIOD -> mWavePeriod = value
            TypedValues.CycleType.TYPE_WAVE_OFFSET -> mWaveOffset = value
            TypedValues.CycleType.TYPE_WAVE_PHASE -> mWavePhase = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    // @TODO: add description
    fun getValue(key: String?): Float {
        return when (key) {
            TypedValues.CycleType.S_ALPHA -> mAlpha
            TypedValues.CycleType.S_ELEVATION -> mElevation
            TypedValues.CycleType.S_ROTATION_Z -> mRotation
            TypedValues.CycleType.S_ROTATION_X -> mRotationX
            TypedValues.CycleType.S_ROTATION_Y -> mRotationY
            TypedValues.CycleType.S_PATH_ROTATE -> mTransitionPathRotate
            TypedValues.CycleType.S_SCALE_X -> mScaleX
            TypedValues.CycleType.S_SCALE_Y -> mScaleY
            TypedValues.CycleType.S_TRANSLATION_X -> mTranslationX
            TypedValues.CycleType.S_TRANSLATION_Y -> mTranslationY
            TypedValues.CycleType.S_TRANSLATION_Z -> mTranslationZ
            TypedValues.CycleType.S_WAVE_OFFSET -> mWaveOffset
            TypedValues.CycleType.S_WAVE_PHASE -> mWavePhase
            TypedValues.CycleType.S_PROGRESS -> mProgress
            else -> Float.NaN
        }
    }

    
    override fun clone(): MotionKey? {
        return null
    }

    
    override fun getId(name: String): Int {
        when (name) {
            TypedValues.CycleType.S_CURVE_FIT -> return TypedValues.CycleType.TYPE_CURVE_FIT
            TypedValues.CycleType.S_VISIBILITY -> return TypedValues.CycleType.TYPE_VISIBILITY
            TypedValues.CycleType.S_ALPHA -> return TypedValues.CycleType.TYPE_ALPHA
            TypedValues.CycleType.S_TRANSLATION_X -> return TypedValues.CycleType.TYPE_TRANSLATION_X
            TypedValues.CycleType.S_TRANSLATION_Y -> return TypedValues.CycleType.TYPE_TRANSLATION_Y
            TypedValues.CycleType.S_TRANSLATION_Z -> return TypedValues.CycleType.TYPE_TRANSLATION_Z
            TypedValues.CycleType.S_ROTATION_X -> return TypedValues.CycleType.TYPE_ROTATION_X
            TypedValues.CycleType.S_ROTATION_Y -> return TypedValues.CycleType.TYPE_ROTATION_Y
            TypedValues.CycleType.S_ROTATION_Z -> return TypedValues.CycleType.TYPE_ROTATION_Z
            TypedValues.CycleType.S_SCALE_X -> return TypedValues.CycleType.TYPE_SCALE_X
            TypedValues.CycleType.S_SCALE_Y -> return TypedValues.CycleType.TYPE_SCALE_Y
            TypedValues.CycleType.S_PIVOT_X -> return TypedValues.CycleType.TYPE_PIVOT_X
            TypedValues.CycleType.S_PIVOT_Y -> return TypedValues.CycleType.TYPE_PIVOT_Y
            TypedValues.CycleType.S_PROGRESS -> return TypedValues.CycleType.TYPE_PROGRESS
            TypedValues.CycleType.S_PATH_ROTATE -> return TypedValues.CycleType.TYPE_PATH_ROTATE
            TypedValues.CycleType.S_EASING -> return TypedValues.CycleType.TYPE_EASING
            TypedValues.CycleType.S_WAVE_PERIOD -> return TypedValues.CycleType.TYPE_WAVE_PERIOD
            TypedValues.CycleType.S_WAVE_SHAPE -> return TypedValues.CycleType.TYPE_WAVE_SHAPE
            TypedValues.CycleType.S_WAVE_PHASE -> return TypedValues.CycleType.TYPE_WAVE_PHASE
            TypedValues.CycleType.S_WAVE_OFFSET -> return TypedValues.CycleType.TYPE_WAVE_OFFSET
            TypedValues.CycleType.S_CUSTOM_WAVE_SHAPE -> return TypedValues.CycleType.TYPE_CUSTOM_WAVE_SHAPE
        }
        return -1
    }

    // @TODO: add description
    fun addCycleValues(oscSet: MutableMap<String, KeyCycleOscillator?>?) {
        oscSet?.let {
            for (key in oscSet.keys) {
                if (key.startsWith(TypedValues.S_CUSTOM)) {
                    val customKey: String = key.substring(TypedValues.S_CUSTOM.length + 1)
                    val cValue: CustomVariable? = mCustom?.get(customKey)
                    if (cValue == null || cValue.type != TypedValues.Custom.TYPE_FLOAT) {
                        continue
                    }
                    val osc: KeyCycleOscillator = oscSet[key] ?: continue
                    osc.setPoint(
                        mFramePosition, mWaveShape, mCustomWaveShape, -1, mWavePeriod,
                        mWaveOffset, mWavePhase / 360, cValue.valueToInterpolate, cValue
                    )
                    continue
                }
                val value = getValue(key)
                if (Float.isNaN(value)) {
                    continue
                }
                val osc: KeyCycleOscillator = oscSet[key] ?: continue
                osc.setPoint(
                    mFramePosition, mWaveShape, mCustomWaveShape,
                    -1, mWavePeriod, mWaveOffset, mWavePhase / 360, value
                )
            }
        }
    }

    // @TODO: add description
    fun dump() {
        println(
            "MotionKeyCycle{"
                    + "mWaveShape=" + mWaveShape
                    + ", mWavePeriod=" + mWavePeriod
                    + ", mWaveOffset=" + mWaveOffset
                    + ", mWavePhase=" + mWavePhase
                    + ", mRotation=" + mRotation
                    + '}'
        )
    }

    // @TODO: add description
    fun printAttributes() {
        val nameSet: MutableSet<String> = mutableSetOf()
        getAttributeNames(nameSet)
        Utils.log(" ------------- " + mFramePosition.toString() + " -------------")
        Utils.log(
            "MotionKeyCycle{"
                    + "Shape=" + mWaveShape
                    + ", Period=" + mWavePeriod
                    + ", Offset=" + mWaveOffset
                    + ", Phase=" + mWavePhase
                    + '}'
        )
        val names: Array<String?> = arrayOfNulls(nameSet.size)
        for (i in names.indices) {
            Utils.log(names[i] + ":" + getValue(names[i]))
        }
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