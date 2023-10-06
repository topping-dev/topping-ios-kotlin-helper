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

import com.olekdia.sparsearray.SparseArray
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.motion.widget.Key
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.core.motion.utils.CurveFit
import dev.topping.ios.constraint.core.motion.utils.SplineSet
import dev.topping.ios.constraint.toDegrees
import kotlin.math.*

abstract class ViewSpline : SplineSet() {
    /**
     * the main interface to setting a view property
     * @param view the view
     * @param t the point of time
     */
    abstract fun setProperty(view: TView, t: Float)
    internal class ElevationSet : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setElevation(get(t))
        }
    }

    internal class AlphaSet : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setAlpha(get(t))
        }
    }

    internal class RotationSet : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setRotation(get(t))
        }
    }

    internal class RotationXset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setRotationX(get(t))
        }
    }

    internal class RotationYset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setRotationY(get(t))
        }
    }

    internal class PivotXset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setPivotX(get(t))
        }
    }

    internal class PivotYset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setPivotY(get(t))
        }
    }

    class PathRotate : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
        }

        /**
         * Use to set the rotation relative to the path
         * @param view the view to set
         * @param t the time point
         * @param dx the path velocity in x
         * @param dy the path velocity in y
         */
        fun setPathRotate(view: TView, t: Float, dx: Double, dy: Double) {
            view.setRotation(get(t) + toDegrees(atan2(dy, dx)).toFloat())
        }
    }

    internal class ScaleXset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setScaleX(get(t))
        }
    }

    internal class ScaleYset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setScaleY(get(t))
        }
    }

    internal class TranslationXset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setTranslationX(get(t))
        }
    }

    internal class TranslationYset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setTranslationY(get(t))
        }
    }

    internal class TranslationZset : ViewSpline() {
        override fun setProperty(view: TView, t: Float) {
            view.setTranslationZ(get(t))
        }
    }

    class CustomSet(attribute: String, attrList: SparseArray<ConstraintAttribute>) : ViewSpline() {
        var mAttributeName: String
        var mConstraintAttributeList: SparseArray<ConstraintAttribute>
        lateinit var mTempValues: FloatArray

        init {
            mAttributeName = attribute.split(",")[1]
            mConstraintAttributeList = attrList
        }
        // @TODO: add description
        /**
         * @param curveType
         */
        override fun setup(curveType: Int) {
            val size: Int = mConstraintAttributeList.size
            val dimensionality: Int =
                mConstraintAttributeList.valueAt(0)!!.numberOfInterpolatedValues()
            val time = DoubleArray(size)
            mTempValues = FloatArray(dimensionality)
            val values = Array(size) { DoubleArray(dimensionality) }
            for (i in 0 until size) {
                val key = mConstraintAttributeList.keyAt(i)!!
                val ca: ConstraintAttribute = mConstraintAttributeList.valueAt(i)!!
                time[i] = key * 1E-2
                ca.getValuesToInterpolate(mTempValues)
                for (k in mTempValues.indices) {
                    values[i][k] = mTempValues[k].toDouble()
                }
            }
            mCurveFit = CurveFit[curveType, time, values]
        }

        /**
         * this call will throw RuntimeException
         * @param position the position
         * @param value the value
         */
        override fun setPoint(position: Int, value: Float) {
            throw RuntimeException("call of custom attribute setPoint")
        }

        /**
         * set the CustomAttribute
         * @param position
         * @param value
         */
        fun setPoint(position: Int, value: ConstraintAttribute) {
            mConstraintAttributeList.append(position, value)
        }

        override fun setProperty(view: TView, t: Float) {
            mCurveFit.getPos(t, mTempValues)
            CustomSupport.setInterpolatedValue(
                mConstraintAttributeList.valueAt(0)!!,
                view, mTempValues
            )
        }
    }

    internal class ProgressSet : ViewSpline() {
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
        private const val TAG = "ViewSpline"
        // @TODO: add description
        /**
         * @param str
         * @param attrList
         * @return
         */
        fun makeCustomSpline(
            str: String,
            attrList: SparseArray<ConstraintAttribute>
        ): ViewSpline {
            return CustomSet(str, attrList)
        }
        // @TODO: add description
        /**
         * @param str
         * @return
         */
        fun makeSpline(str: String?): ViewSpline? {
            return when (str) {
                Key.ALPHA -> AlphaSet()
                Key.ELEVATION -> ElevationSet()
                Key.ROTATION -> RotationSet()
                Key.ROTATION_X -> RotationXset()
                Key.ROTATION_Y -> RotationYset()
                Key.PIVOT_X -> PivotXset()
                Key.PIVOT_Y -> PivotYset()
                Key.TRANSITION_PATH_ROTATE -> PathRotate()
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