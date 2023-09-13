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

import dev.topping.ios.constraint.core.motion.utils.TypedValues
import dev.topping.ios.constraint.core.state.WidgetFrame
import dev.topping.ios.constraint.core.widgets.ConstraintWidget

class MotionWidget : TypedValues {
    var mWidgetFrame: WidgetFrame = WidgetFrame()
    var mMotion = Motion()
    var mPropertySet = PropertySet()
    private var mProgress = 0f
    var mTransitionPathRotate = 0f

    /**
     *
     */
    class Motion {
        var mAnimateRelativeTo: String? = null
        var mAnimateCircleAngleTo = 0
        var mTransitionEasing: String? = null
        var mPathMotionArc = UNSET
        var mDrawPath = 0
        var mMotionStagger = Float.NaN
        var mPolarRelativeTo = UNSET
        var mPathRotate = Float.NaN
        var mQuantizeMotionPhase = Float.NaN
        var mQuantizeMotionSteps = UNSET
        var mQuantizeInterpolatorString: String? = null
        var mQuantizeInterpolatorType = INTERPOLATOR_UNDEFINED // undefined
        var mQuantizeInterpolatorID = -1

        companion object {
            
            private val INTERPOLATOR_REFERENCE_ID = -2

            
            private val SPLINE_STRING = -1
            private const val INTERPOLATOR_UNDEFINED = -3
        }
    }

    class PropertySet {
        var visibility = VISIBLE
        var mVisibilityMode = VISIBILITY_MODE_NORMAL
        var alpha = 1f
        var mProgress = Float.NaN
    }

    constructor() {}

    val parent: MotionWidget?
        get() = null

    // @TODO: add description
    fun findViewById(mTransformPivotTarget: Int): MotionWidget? {
        return null
    }

    val name: String
        get() = mWidgetFrame.id

    // @TODO: add description
    fun layout(l: Int, t: Int, r: Int, b: Int) {
        setBounds(l, t, r, b)
    }

    // @TODO: add description
    
    override fun toString(): String {
        return (mWidgetFrame.left.toString() + ", " + mWidgetFrame.top + ", "
                + mWidgetFrame.right + ", " + mWidgetFrame.bottom)
    }

    // @TODO: add description
    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (mWidgetFrame == null) {
            mWidgetFrame = WidgetFrame(null as ConstraintWidget?)
        }
        mWidgetFrame.top = top
        mWidgetFrame.left = left
        mWidgetFrame.right = right
        mWidgetFrame.bottom = bottom
    }

    constructor(f: WidgetFrame) {
        mWidgetFrame = f
    }

    /**
     * This populates the motion attributes from widgetFrame to the MotionWidget
     */
    fun updateMotion(toUpdate: TypedValues) {
        if (mWidgetFrame.motionProperties != null) {
            mWidgetFrame.motionProperties!!.applyDelta(toUpdate)
        }
    }

    
    override fun setValue(id: Int, value: Int): Boolean {
        val set = setValueAttributes(id, value.toFloat())
        return if (set) {
            true
        } else setValueMotion(id, value)
    }

    
    override fun setValue(id: Int, value: Float): Boolean {
        val set = setValueAttributes(id, value)
        return if (set) {
            true
        } else setValueMotion(id, value)
    }

    
    override fun setValue(id: Int, value: String): Boolean {
        if (id == TypedValues.MotionType.TYPE_ANIMATE_RELATIVE_TO) {
            mMotion.mAnimateRelativeTo = value
            return true
        }
        return setValueMotion(id, value)
    }

    
    override fun setValue(id: Int, value: Boolean): Boolean {
        return false
    }

    // @TODO: add description
    fun setValueMotion(id: Int, value: Int): Boolean {
        when (id) {
            TypedValues.MotionType.TYPE_ANIMATE_CIRCLEANGLE_TO -> mMotion.mAnimateCircleAngleTo = value
            TypedValues.MotionType.TYPE_PATHMOTION_ARC -> mMotion.mPathMotionArc = value
            TypedValues.MotionType.TYPE_DRAW_PATH -> mMotion.mDrawPath = value
            TypedValues.MotionType.TYPE_POLAR_RELATIVETO -> mMotion.mPolarRelativeTo = value
            TypedValues.MotionType.TYPE_QUANTIZE_MOTIONSTEPS -> mMotion.mQuantizeMotionSteps = value
            TypedValues.MotionType.TYPE_QUANTIZE_INTERPOLATOR_TYPE -> mMotion.mQuantizeInterpolatorType = value
            TypedValues.MotionType.TYPE_QUANTIZE_INTERPOLATOR_ID -> mMotion.mQuantizeInterpolatorID = value
            else -> return false
        }
        return true
    }

    // @TODO: add description
    fun setValueMotion(id: Int, value: String?): Boolean {
        when (id) {
            TypedValues.MotionType.TYPE_EASING -> mMotion.mTransitionEasing = value
            TypedValues.MotionType.TYPE_QUANTIZE_INTERPOLATOR -> mMotion.mQuantizeInterpolatorString = value
            else -> return false
        }
        return true
    }

    // @TODO: add description
    fun setValueMotion(id: Int, value: Float): Boolean {
        when (id) {
            TypedValues.MotionType.TYPE_STAGGER -> mMotion.mMotionStagger = value
            TypedValues.MotionType.TYPE_PATH_ROTATE -> mMotion.mPathRotate = value
            TypedValues.MotionType.TYPE_QUANTIZE_MOTION_PHASE -> mMotion.mQuantizeMotionPhase = value
            else -> return false
        }
        return true
    }

    /**
     * Sets the attributes
     */
    fun setValueAttributes(id: Int, value: Float): Boolean {
        when (id) {
            TypedValues.AttributesType.TYPE_ALPHA -> mWidgetFrame.alpha = value
            TypedValues.AttributesType.TYPE_TRANSLATION_X -> mWidgetFrame.translationX = value
            TypedValues.AttributesType.TYPE_TRANSLATION_Y -> mWidgetFrame.translationY = value
            TypedValues.AttributesType.TYPE_TRANSLATION_Z -> mWidgetFrame.translationZ = value
            TypedValues.AttributesType.TYPE_ROTATION_X -> mWidgetFrame.rotationX = value
            TypedValues.AttributesType.TYPE_ROTATION_Y -> mWidgetFrame.rotationY = value
            TypedValues.AttributesType.TYPE_ROTATION_Z -> mWidgetFrame.rotationZ = value
            TypedValues.AttributesType.TYPE_SCALE_X -> mWidgetFrame.scaleX = value
            TypedValues.AttributesType.TYPE_SCALE_Y -> mWidgetFrame.scaleY = value
            TypedValues.AttributesType.TYPE_PIVOT_X -> mWidgetFrame.pivotX = value
            TypedValues.AttributesType.TYPE_PIVOT_Y -> mWidgetFrame.pivotY = value
            TypedValues.AttributesType.TYPE_PROGRESS -> mProgress = value
            TypedValues.AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            else -> return false
        }
        return true
    }

    /**
     * Sets the attributes
     */
    fun getValueAttributes(id: Int): Float {
        return when (id) {
            TypedValues.AttributesType.TYPE_ALPHA -> mWidgetFrame.alpha
            TypedValues.AttributesType.TYPE_TRANSLATION_X -> mWidgetFrame.translationX
            TypedValues.AttributesType.TYPE_TRANSLATION_Y -> mWidgetFrame.translationY
            TypedValues.AttributesType.TYPE_TRANSLATION_Z -> mWidgetFrame.translationZ
            TypedValues.AttributesType.TYPE_ROTATION_X -> mWidgetFrame.rotationX
            TypedValues.AttributesType.TYPE_ROTATION_Y -> mWidgetFrame.rotationY
            TypedValues.AttributesType.TYPE_ROTATION_Z -> mWidgetFrame.rotationZ
            TypedValues.AttributesType.TYPE_SCALE_X -> mWidgetFrame.scaleX
            TypedValues.AttributesType.TYPE_SCALE_Y -> mWidgetFrame.scaleY
            TypedValues.AttributesType.TYPE_PIVOT_X -> mWidgetFrame.pivotX
            TypedValues.AttributesType.TYPE_PIVOT_Y -> mWidgetFrame.pivotY
            TypedValues.AttributesType.TYPE_PROGRESS -> mProgress
            TypedValues.AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate
            else -> Float.NaN
        }
    }

    
    override fun getId(name: String): Int {
        val ret: Int = TypedValues.AttributesType.getId(name)
        return if (ret != -1) {
            ret
        } else TypedValues.MotionType.getId(name)
    }

    val top: Int
        get() = mWidgetFrame.top
    val left: Int
        get() = mWidgetFrame.left
    val bottom: Int
        get() = mWidgetFrame.bottom
    val right: Int
        get() = mWidgetFrame.right
    var rotationX: Float
        get() = mWidgetFrame.rotationX
        set(rotationX) {
            mWidgetFrame.rotationX = rotationX
        }
    var rotationY: Float
        get() = mWidgetFrame.rotationY
        set(rotationY) {
            mWidgetFrame.rotationY = rotationY
        }
    var rotationZ: Float
        get() = mWidgetFrame.rotationZ
        set(rotationZ) {
            mWidgetFrame.rotationZ = rotationZ
        }
    var translationX: Float
        get() = mWidgetFrame.translationX
        set(translationX) {
            mWidgetFrame.translationX = translationX
        }
    var translationY: Float
        get() = mWidgetFrame.translationY
        set(translationY) {
            mWidgetFrame.translationY = translationY
        }
    var translationZ: Float
        get() = mWidgetFrame.translationZ
        set(tz) {
            mWidgetFrame.translationZ = tz
        }
    var scaleX: Float
        get() = mWidgetFrame.scaleX
        set(scaleX) {
            mWidgetFrame.scaleX = scaleX
        }
    var scaleY: Float
        get() = mWidgetFrame.scaleY
        set(scaleY) {
            mWidgetFrame.scaleY = scaleY
        }
    var visibility: Int
        get() = mPropertySet.visibility
        set(visibility) {
            mPropertySet.visibility = visibility
        }
    var pivotX: Float
        get() = mWidgetFrame.pivotX
        set(px) {
            mWidgetFrame.pivotX = px
        }
    var pivotY: Float
        get() = mWidgetFrame.pivotY
        set(py) {
            mWidgetFrame.pivotY = py
        }
    val alpha: Float
        get() = mWidgetFrame.alpha
    val x: Int
        get() = mWidgetFrame.left
    val y: Int
        get() = mWidgetFrame.top
    val width: Int
        get() = mWidgetFrame.right - mWidgetFrame.left
    val height: Int
        get() = mWidgetFrame.bottom - mWidgetFrame.top
    val widgetFrame: WidgetFrame?
        get() = mWidgetFrame
    val customAttributeNames: Set<String>
        get() = mWidgetFrame.customAttributeNames

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Float) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Int) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Boolean) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: String?) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun getCustomAttribute(name: String): CustomVariable? {
        return mWidgetFrame.getCustomAttribute(name)
    }

    // @TODO: add description
    fun setInterpolatedValue(attribute: CustomAttribute, mCache: FloatArray) {
        mWidgetFrame.setCustomAttribute(attribute.mName, TypedValues.Custom.TYPE_FLOAT, mCache[0])
    }

    companion object {
        const val VISIBILITY_MODE_NORMAL = 0
        const val VISIBILITY_MODE_IGNORE = 1

        
        private val INTERNAL_MATCH_PARENT = -1

        
        private val INTERNAL_WRAP_CONTENT = -2
        const val INVISIBLE = 0
        const val VISIBLE = 4

        
        private val INTERNAL_MATCH_CONSTRAINT = -3

        
        private val INTERNAL_WRAP_CONTENT_CONSTRAINED = -4
        const val ROTATE_NONE = 0
        const val ROTATE_PORTRATE_OF_RIGHT = 1
        const val ROTATE_PORTRATE_OF_LEFT = 2
        const val ROTATE_RIGHT_OF_PORTRATE = 3
        const val ROTATE_LEFT_OF_PORTRATE = 4
        const val UNSET = -1
        const val UNSET_ID = ""
        const val MATCH_CONSTRAINT = 0
        const val PARENT_ID = 0
        const val FILL_PARENT = -1
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
        val GONE_UNSET: Int = Int.MIN_VALUE
        val MATCH_CONSTRAINT_WRAP: Int = ConstraintWidget.MATCH_CONSTRAINT_WRAP
    }
}