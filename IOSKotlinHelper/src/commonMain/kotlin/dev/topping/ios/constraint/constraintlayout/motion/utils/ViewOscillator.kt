/*
 * Copyright (C) 2020 The Android Open Source Project
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
package dev.topping.ios.constraint.constraintlayout.motion.utils

import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.motion.widget.Key
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.core.motion.utils.KeyCycleOscillator
import dev.topping.ios.constraint.toDegrees
import kotlin.math.*

/**
 * Provide the engine for executing cycles.
 * KeyCycleOscillator
 *
 *
 */
abstract class ViewOscillator : KeyCycleOscillator() {
    /**
     * Set the property of that view
     * @param view
     * @param t
     */
    abstract fun setProperty(view: TView, t: Float)
    internal class ElevationSet : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
                view.setElevation(get(t))
        }
    }

    internal class AlphaSet : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setAlpha(get(t))
        }
    }

    internal class RotationSet : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setRotation(get(t))
        }
    }

    internal class RotationXset : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setRotationX(get(t))
        }
    }

    internal class RotationYset : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setRotationY(get(t))
        }
    }

    class PathRotateSet : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
        }

        /**
         * use to modify the rotation relative to the current path
         * @param view the view to modify
         * @param t the point in time to manipulate
         * @param dx of the path
         * @param dy of the path
         */
        fun setPathRotate(view: TView, t: Float, dx: Double, dy: Double) {
            view.setRotation(get(t) + toDegrees(atan2(dy, dx)).toFloat())
        }
    }

    internal class ScaleXset : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setScaleX(get(t))
        }
    }

    internal class ScaleYset : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setScaleY(get(t))
        }
    }

    internal class TranslationXset : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setTranslationX(get(t))
        }
    }

    internal class TranslationYset : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setTranslationY(get(t))
        }
    }

    internal class TranslationZset : ViewOscillator() {
        override fun setProperty(view: TView, t: Float) {
            view.setTranslationZ(get(t))
        }
    }

    internal class CustomSet : ViewOscillator() {
        var mValue = FloatArray(1)
        protected lateinit var mCustom: ConstraintAttribute
        protected fun setCustom(custom: Any) {
            mCustom = custom as ConstraintAttribute
        }
        
        override fun setProperty(view: TView, t: Float) {
            mValue[0] = get(t)
            CustomSupport.setInterpolatedValue(mCustom, view, mValue)
        }
    }

    internal class ProgressSet : ViewOscillator() {
        var mNoMethod = false
        override fun setProperty(view: TView, t: Float) {
            if (view.getParentType() is MotionLayout) {
                (view.getParentType() as MotionLayout).progress = get(t)
            } else {
                view.invokeMethod("setProgress", get(t))
            }
        }
    }

    companion object {
        private const val TAG = "ViewOscillator"

        /**
         * Create a spline that manipulates a specific property of a view
         * @param str the property to manipulate
         * @return
         */
        fun makeSpline(str: String): ViewOscillator? {
            return if (str.startsWith(Key.CUSTOM)) {
                CustomSet()
            } else when (str) {
                Key.ALPHA -> AlphaSet()
                Key.ELEVATION -> ElevationSet()
                Key.ROTATION -> RotationSet()
                Key.ROTATION_X -> RotationXset()
                Key.ROTATION_Y -> RotationYset()
                Key.TRANSITION_PATH_ROTATE -> PathRotateSet()
                Key.SCALE_X -> ScaleXset()
                Key.SCALE_Y -> ScaleYset()
                Key.WAVE_OFFSET -> AlphaSet()
                Key.WAVE_VARIES_BY -> AlphaSet()
                Key.TRANSLATION_X -> TranslationXset()
                Key.TRANSLATION_Y -> TranslationYset()
                Key.TRANSLATION_Z -> TranslationZset()
                Key.PROGRESS -> ProgressSet()
                else -> null
            }
        }
    }
}