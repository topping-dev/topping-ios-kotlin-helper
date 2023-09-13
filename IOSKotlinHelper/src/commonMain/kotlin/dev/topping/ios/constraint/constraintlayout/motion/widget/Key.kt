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

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewSpline
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute

/**
 * Base class in an element in a KeyFrame
 *
 *
 */
abstract class Key {
    /**
     * Gets the current frame position
     *
     * @return
     */
    /**
     * sets the frame position
     *
     * @param pos
     */
    var mFramePosition = UNSET
    var mTargetId = UNSET_ID
    var mTargetString: String? = null
    var mType = 0
    abstract fun load(context: TContext, attrs: AttributeSet)
    var mCustomConstraints: MutableMap<String, ConstraintAttribute>? = null
    abstract fun getAttributeNames(attributes: MutableSet<String>)
    fun matches(constraintTag: String?): Boolean {
        return if (mTargetString == null || constraintTag == null) false else constraintTag.matches(
            mTargetString!!.toRegex()
        )
    }

    /**
     * Defines method to add a a view to splines derived form this key frame.
     * The values are written to the spline
     *
     * @param splines splines to write values to
     */
    abstract fun addValues(splines: MutableMap<String, ViewSpline>)

    /**
     * Set the value associated with this tag
     *
     * @param tag
     * @param value
     */
    abstract fun setValue(tag: String, value: Any)

    /**
     * Return the float given a value. If the value is a "Float" object it is casted
     *
     * @param value
     * @return
     */
    fun toFloat(value: Any): Float {
        return if (value is Float) value else value.toString().toFloat()
    }

    /**
     * Return the int version of an object if the value is an Integer object it is casted.
     *
     * @param value
     * @return
     */
    fun toInt(value: Any): Int {
        return if (value is Int) value else value.toString().toInt()
    }

    /**
     * Return the boolean version this object if the object is a Boolean it is casted.
     *
     * @param value
     * @return
     */
    fun toBoolean(value: Any): Boolean {
        return if (value is Boolean) value else value.toString().toBoolean()
    }

    /**
     * Key frame can specify the type of interpolation it wants on various attributes
     * For each string it set it to -1, CurveFit.LINEAR or  CurveFit.SPLINE
     *
     * @param interpolation
     */
    open fun setInterpolation(interpolation: MutableMap<String, Int>) {}

    /**
     * Return a copy of this key
     * @param src
     * @return
     */
    open fun copy(src: Key): Key {
        mFramePosition = src.mFramePosition
        mTargetId = src.mTargetId
        mTargetString = src.mTargetString
        mType = src.mType
        mCustomConstraints = src.mCustomConstraints
        return this
    }

    /**
     * Return a copy of this
     * @return
     */
    abstract fun clone(): Key

    /**
     * set the id of the view
     * @param id
     * @return
     */
    fun setViewId(id: String): Key {
        mTargetId = id
        return this
    }

    companion object {
        const val UNSET = -1
        const val UNSET_ID = ""
        const val ALPHA = "alpha"
        const val ELEVATION = "elevation"
        const val ROTATION = "rotation"
        const val ROTATION_X = "rotationX"
        const val ROTATION_Y = "rotationY"
        const val PIVOT_X = "transformPivotX"
        const val PIVOT_Y = "transformPivotY"
        const val TRANSITION_PATH_ROTATE = "transitionPathRotate"
        const val SCALE_X = "scaleX"
        const val SCALE_Y = "scaleY"
        const val WAVE_PERIOD = "wavePeriod"
        const val WAVE_OFFSET = "waveOffset"
        const val WAVE_PHASE = "wavePhase"
        const val WAVE_VARIES_BY = "waveVariesBy"
        const val TRANSLATION_X = "translationX"
        const val TRANSLATION_Y = "translationY"
        const val TRANSLATION_Z = "translationZ"
        const val PROGRESS = "progress"
        const val CUSTOM = "CUSTOM"
        const val CURVEFIT = "curveFit"
        const val MOTIONPROGRESS = "motionProgress"
        const val TRANSITIONEASING = "transitionEasing"
        const val VISIBILITY = "visibility"
    }
}