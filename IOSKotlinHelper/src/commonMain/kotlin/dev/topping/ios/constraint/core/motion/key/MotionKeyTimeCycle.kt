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

class MotionKeyTimeCycle : MotionKey() {
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

    // TODO add support of custom wave shapes in KeyTimeCycle
    
    private var mCustomWaveShape: String? = null
    private var mWavePeriod = Float.NaN
    private var mWaveOffset = 0f

    init {
        mType = KEY_TYPE
        mCustom = HashMap()
    }

    // @TODO: add description
    fun addTimeValues(splines: MutableMap<String, TimeCycleSplineSet>?) {
        splines?.let {
            for (s in splines.keys) {
                val splineSet: TimeCycleSplineSet = splines[s] ?: continue
                if (s.startsWith(CUSTOM)) {
                    val cKey: String = s.substring(CUSTOM.length + 1)
                    val cValue: CustomVariable? = mCustom?.get(cKey)
                    if (cValue != null) {
                        (splineSet as TimeCycleSplineSet.CustomVarSet)
                            .setPoint(mFramePosition, cValue, mWavePeriod, mWaveShape, mWaveOffset)
                    }
                    continue
                }
                when (s) {
                    TypedValues.AttributesType.S_ALPHA -> if (!Float.isNaN(mAlpha)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mAlpha, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_ROTATION_X -> if (!Float.isNaN(mRotationX)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mRotationX, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_ROTATION_Y -> if (!Float.isNaN(mRotationY)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mRotationY, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_ROTATION_Z -> if (!Float.isNaN(mRotation)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mRotation, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_PATH_ROTATE -> if (!Float.isNaN(mTransitionPathRotate)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mTransitionPathRotate, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_SCALE_X -> if (!Float.isNaN(mScaleX)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mScaleX, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_SCALE_Y -> if (!Float.isNaN(mScaleY)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mScaleY, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_TRANSLATION_X -> if (!Float.isNaN(mTranslationX)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mTranslationX, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_TRANSLATION_Y -> if (!Float.isNaN(mTranslationY)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mTranslationY, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_TRANSLATION_Z -> if (!Float.isNaN(mTranslationZ)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mTranslationZ, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_ELEVATION -> if (!Float.isNaN(mTranslationZ)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mTranslationZ, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    TypedValues.AttributesType.S_PROGRESS -> if (!Float.isNaN(mProgress)) {
                        splineSet.setPoint(
                            mFramePosition,
                            mProgress, mWavePeriod, mWaveShape, mWaveOffset
                        )
                    }
                    else -> Utils.loge("KeyTimeCycles", "UNKNOWN addValues \"$s\"")
                }
            }
        }
    }

    
    override fun addValues(splines: MutableMap<String, SplineSet>?) {
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            TypedValues.TYPE_FRAME_POSITION -> mFramePosition = value
            TypedValues.CycleType.TYPE_WAVE_SHAPE -> mWaveShape = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            TypedValues.CycleType.TYPE_ALPHA -> mAlpha = value
            TypedValues.CycleType.TYPE_CURVE_FIT -> mCurveFit = toInt(value)
            TypedValues.CycleType.TYPE_ELEVATION -> mElevation = toFloat(value)
            TypedValues.CycleType.TYPE_PROGRESS -> mProgress = toFloat(value)
            TypedValues.CycleType.TYPE_ROTATION_Z -> mRotation = toFloat(value)
            TypedValues.CycleType.TYPE_ROTATION_X -> mRotationX = toFloat(value)
            TypedValues.CycleType.TYPE_ROTATION_Y -> mRotationY = toFloat(value)
            TypedValues.CycleType.TYPE_SCALE_X -> mScaleX = toFloat(value)
            TypedValues.CycleType.TYPE_SCALE_Y -> mScaleY = toFloat(value)
            TypedValues.CycleType.TYPE_PATH_ROTATE -> mTransitionPathRotate = toFloat(value)
            TypedValues.CycleType.TYPE_TRANSLATION_X -> mTranslationX = toFloat(value)
            TypedValues.CycleType.TYPE_TRANSLATION_Y -> mTranslationY = toFloat(value)
            TypedValues.CycleType.TYPE_TRANSLATION_Z -> mTranslationZ = toFloat(value)
            TypedValues.CycleType.TYPE_WAVE_PERIOD -> mWavePeriod = toFloat(value)
            TypedValues.CycleType.TYPE_WAVE_OFFSET -> mWaveOffset = toFloat(value)
            else -> return super.setValue(type, value)
        }
        return true
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: String): Boolean {
        when (type) {
            TypedValues.CycleType.TYPE_WAVE_SHAPE -> {
                mWaveShape = Oscillator.CUSTOM
                mCustomWaveShape = value
            }
            TypedValues.CycleType.TYPE_EASING -> mTransitionEasing = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: Boolean): Boolean {
        return super.setValue(type, value)
    }

    // @TODO: add description
    
    override fun copy(src: MotionKey): MotionKeyTimeCycle {
        super.copy(src)
        val k = src as MotionKeyTimeCycle
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
        return this
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

    // @TODO: add description
    
    override fun clone(): MotionKey {
        return MotionKeyTimeCycle().copy(this)
    }

    
    override fun getId(name: String): Int {
        return TypedValues.CycleType.getId(name)
    }

    companion object {
        const val NAME = "KeyTimeCycle"
        private const val TAG = NAME
        const val KEY_TYPE = 3
    }
}