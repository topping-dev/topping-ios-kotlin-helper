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
import dev.topping.ios.constraint.core.motion.utils.SplineSet
import dev.topping.ios.constraint.core.motion.utils.TypedValues
import dev.topping.ios.constraint.isNaN

class MotionKeyAttributes : MotionKey() {
    
    private var mTransitionEasing: String? = null
    var curveFit = -1
        private set

    
    private var mVisibility = 0
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
        mCustom = HashMap()
    }

    
    override fun getAttributeNames(attributes: MutableSet<String>?) {
        attributes?.let {
            if (!Float.isNaN(mAlpha)) {
                attributes.add(TypedValues.AttributesType.S_ALPHA)
            }
            if (!Float.isNaN(mElevation)) {
                attributes.add(TypedValues.AttributesType.S_ELEVATION)
            }
            if (!Float.isNaN(mRotation)) {
                attributes.add(TypedValues.AttributesType.S_ROTATION_Z)
            }
            if (!Float.isNaN(mRotationX)) {
                attributes.add(TypedValues.AttributesType.S_ROTATION_X)
            }
            if (!Float.isNaN(mRotationY)) {
                attributes.add(TypedValues.AttributesType.S_ROTATION_Y)
            }
            if (!Float.isNaN(mPivotX)) {
                attributes.add(TypedValues.AttributesType.S_PIVOT_X)
            }
            if (!Float.isNaN(mPivotY)) {
                attributes.add(TypedValues.AttributesType.S_PIVOT_Y)
            }
            if (!Float.isNaN(mTranslationX)) {
                attributes.add(TypedValues.AttributesType.S_TRANSLATION_X)
            }
            if (!Float.isNaN(mTranslationY)) {
                attributes.add(TypedValues.AttributesType.S_TRANSLATION_Y)
            }
            if (!Float.isNaN(mTranslationZ)) {
                attributes.add(TypedValues.AttributesType.S_TRANSLATION_Z)
            }
            if (!Float.isNaN(mTransitionPathRotate)) {
                attributes.add(TypedValues.AttributesType.S_PATH_ROTATE)
            }
            if (!Float.isNaN(mScaleX)) {
                attributes.add(TypedValues.AttributesType.S_SCALE_X)
            }
            if (!Float.isNaN(mScaleY)) {
                attributes.add(TypedValues.AttributesType.S_SCALE_Y)
            }
            if (!Float.isNaN(mProgress)) {
                attributes.add(TypedValues.AttributesType.S_PROGRESS)
            }
            if (mCustom?.isNotEmpty() == true) {
                for (s in mCustom!!.keys) {
                    attributes.add(TypedValues.S_CUSTOM + "," + s)
                }
            }
        }
    }

    
    override fun addValues(splines: MutableMap<String, SplineSet>?) {
        if(splines == null)
            return
        for (s in splines.keys) {
            val splineSet: SplineSet = splines[s] ?: continue
            // TODO support custom
            if (s.startsWith(TypedValues.AttributesType.S_CUSTOM)) {
                val cKey: String = s.substring(TypedValues.AttributesType.S_CUSTOM.length + 1)
                val cValue: CustomVariable? = mCustom?.get(cKey)
                if (cValue != null) {
                    (splineSet as SplineSet.CustomSpline).setPoint(mFramePosition, cValue)
                }
                continue
            }
            when (s) {
                TypedValues.AttributesType.S_ALPHA -> if (!Float.isNaN(mAlpha)) {
                    splineSet.setPoint(mFramePosition, mAlpha)
                }
                TypedValues.AttributesType.S_ELEVATION -> if (!Float.isNaN(mElevation)) {
                    splineSet.setPoint(mFramePosition, mElevation)
                }
                TypedValues.AttributesType.S_ROTATION_Z -> if (!Float.isNaN(mRotation)) {
                    splineSet.setPoint(mFramePosition, mRotation)
                }
                TypedValues.AttributesType.S_ROTATION_X -> if (!Float.isNaN(mRotationX)) {
                    splineSet.setPoint(mFramePosition, mRotationX)
                }
                TypedValues.AttributesType.S_ROTATION_Y -> if (!Float.isNaN(mRotationY)) {
                    splineSet.setPoint(mFramePosition, mRotationY)
                }
                TypedValues.AttributesType.S_PIVOT_X -> if (!Float.isNaN(mRotationX)) {
                    splineSet.setPoint(mFramePosition, mPivotX)
                }
                TypedValues.AttributesType.S_PIVOT_Y -> if (!Float.isNaN(mRotationY)) {
                    splineSet.setPoint(mFramePosition, mPivotY)
                }
                TypedValues.AttributesType.S_PATH_ROTATE -> if (!Float.isNaN(mTransitionPathRotate)) {
                    splineSet.setPoint(mFramePosition, mTransitionPathRotate)
                }
                TypedValues.AttributesType.S_SCALE_X -> if (!Float.isNaN(mScaleX)) {
                    splineSet.setPoint(mFramePosition, mScaleX)
                }
                TypedValues.AttributesType.S_SCALE_Y -> if (!Float.isNaN(mScaleY)) {
                    splineSet.setPoint(mFramePosition, mScaleY)
                }
                TypedValues.AttributesType.S_TRANSLATION_X -> if (!Float.isNaN(mTranslationX)) {
                    splineSet.setPoint(mFramePosition, mTranslationX)
                }
                TypedValues.AttributesType.S_TRANSLATION_Y -> if (!Float.isNaN(mTranslationY)) {
                    splineSet.setPoint(mFramePosition, mTranslationY)
                }
                TypedValues.AttributesType.S_TRANSLATION_Z -> if (!Float.isNaN(mTranslationZ)) {
                    splineSet.setPoint(mFramePosition, mTranslationZ)
                }
                TypedValues.AttributesType.S_PROGRESS -> if (!Float.isNaN(mProgress)) {
                    splineSet.setPoint(mFramePosition, mProgress)
                }
                else -> println("not supported by KeyAttributes $s")
            }
        }
    }

    
    override fun clone(): MotionKey? {
        return null
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            TypedValues.AttributesType.TYPE_VISIBILITY -> mVisibility = value
            TypedValues.AttributesType.TYPE_CURVE_FIT -> curveFit = value
            TypedValues.TYPE_FRAME_POSITION -> mFramePosition = value
            else -> if (!setValue(type, value)) {
                return super.setValue(type, value)
            }
        }
        return true
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            TypedValues.AttributesType.TYPE_ALPHA -> mAlpha = value
            TypedValues.AttributesType.TYPE_TRANSLATION_X -> mTranslationX = value
            TypedValues.AttributesType.TYPE_TRANSLATION_Y -> mTranslationY = value
            TypedValues.AttributesType.TYPE_TRANSLATION_Z -> mTranslationZ = value
            TypedValues.AttributesType.TYPE_ELEVATION -> mElevation = value
            TypedValues.AttributesType.TYPE_ROTATION_X -> mRotationX = value
            TypedValues.AttributesType.TYPE_ROTATION_Y -> mRotationY = value
            TypedValues.AttributesType.TYPE_ROTATION_Z -> mRotation = value
            TypedValues.AttributesType.TYPE_SCALE_X -> mScaleX = value
            TypedValues.AttributesType.TYPE_SCALE_Y -> mScaleY = value
            TypedValues.AttributesType.TYPE_PIVOT_X -> mPivotX = value
            TypedValues.AttributesType.TYPE_PIVOT_Y -> mPivotY = value
            TypedValues.AttributesType.TYPE_PROGRESS -> mProgress = value
            TypedValues.AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            TypedValues.TYPE_FRAME_POSITION -> mTransitionPathRotate = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    // @TODO: add description
    
    override fun setInterpolation(interpolation: MutableMap<String, Int>) {
        if (!Float.isNaN(mAlpha)) {
            interpolation[TypedValues.AttributesType.S_ALPHA] = curveFit
        }
        if (!Float.isNaN(mElevation)) {
            interpolation[TypedValues.AttributesType.S_ELEVATION] = curveFit
        }
        if (!Float.isNaN(mRotation)) {
            interpolation[TypedValues.AttributesType.S_ROTATION_Z] = curveFit
        }
        if (!Float.isNaN(mRotationX)) {
            interpolation[TypedValues.AttributesType.S_ROTATION_X] = curveFit
        }
        if (!Float.isNaN(mRotationY)) {
            interpolation[TypedValues.AttributesType.S_ROTATION_Y] = curveFit
        }
        if (!Float.isNaN(mPivotX)) {
            interpolation[TypedValues.AttributesType.S_PIVOT_X] = curveFit
        }
        if (!Float.isNaN(mPivotY)) {
            interpolation[TypedValues.AttributesType.S_PIVOT_Y] = curveFit
        }
        if (!Float.isNaN(mTranslationX)) {
            interpolation[TypedValues.AttributesType.S_TRANSLATION_X] = curveFit
        }
        if (!Float.isNaN(mTranslationY)) {
            interpolation[TypedValues.AttributesType.S_TRANSLATION_Y] = curveFit
        }
        if (!Float.isNaN(mTranslationZ)) {
            interpolation[TypedValues.AttributesType.S_TRANSLATION_Z] = curveFit
        }
        if (!Float.isNaN(mTransitionPathRotate)) {
            interpolation[TypedValues.AttributesType.S_PATH_ROTATE] = curveFit
        }
        if (!Float.isNaN(mScaleX)) {
            interpolation[TypedValues.AttributesType.S_SCALE_X] = curveFit
        }
        if (!Float.isNaN(mScaleY)) {
            interpolation[TypedValues.AttributesType.S_SCALE_Y] = curveFit
        }
        if (!Float.isNaN(mProgress)) {
            interpolation[TypedValues.AttributesType.S_PROGRESS] = curveFit
        }
        if (mCustom?.isNotEmpty() == true) {
            for (s in mCustom!!.keys) {
                interpolation[TypedValues.AttributesType.S_CUSTOM + "," + s] = curveFit
            }
        }
    }

    // @TODO: add description
    
    override fun setValue(type: Int, value: String): Boolean {
        when (type) {
            TypedValues.AttributesType.TYPE_EASING -> mTransitionEasing = value
            TypedValues.TYPE_TARGET -> mTargetString = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    
    override fun getId(name: String): Int {
        return TypedValues.AttributesType.getId(name)
    }

    // @TODO: add description
    fun printAttributes() {
        val nameSet: MutableSet<String> = mutableSetOf()
        getAttributeNames(nameSet)
        println(" ------------- $mFramePosition -------------")
        val names: Array<String> = nameSet.toTypedArray()
        for (i in names.indices) {
            val id: Int = TypedValues.AttributesType.getId(names[i])
            println(names[i] + ":" + getFloatValue(id))
        }
    }

    private fun getFloatValue(id: Int): Float {
        return when (id) {
            TypedValues.AttributesType.TYPE_ALPHA -> mAlpha
            TypedValues.AttributesType.TYPE_TRANSLATION_X -> mTranslationX
            TypedValues.AttributesType.TYPE_TRANSLATION_Y -> mTranslationY
            TypedValues.AttributesType.TYPE_TRANSLATION_Z -> mTranslationZ
            TypedValues.AttributesType.TYPE_ELEVATION -> mElevation
            TypedValues.AttributesType.TYPE_ROTATION_X -> mRotationX
            TypedValues.AttributesType.TYPE_ROTATION_Y -> mRotationY
            TypedValues.AttributesType.TYPE_ROTATION_Z -> mRotation
            TypedValues.AttributesType.TYPE_SCALE_X -> mScaleX
            TypedValues.AttributesType.TYPE_SCALE_Y -> mScaleY
            TypedValues.AttributesType.TYPE_PIVOT_X -> mPivotX
            TypedValues.AttributesType.TYPE_PIVOT_Y -> mPivotY
            TypedValues.AttributesType.TYPE_PROGRESS -> mProgress
            TypedValues.AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate
            TypedValues.TYPE_FRAME_POSITION -> mFramePosition.toFloat()
            else -> Float.NaN
        }
    }

    companion object {
        const val NAME = "KeyAttribute"
        private const val TAG = "KeyAttributes"

        
        private val I_DEBUG = false
        const val KEY_TYPE = 1
    }
}