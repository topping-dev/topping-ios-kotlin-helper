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
import dev.topping.ios.constraint.core.motion.utils.KeyCache
import dev.topping.ios.constraint.core.motion.utils.TimeCycleSplineSet
import dev.topping.ios.constraint.isNaN
import dev.topping.ios.constraint.toDegrees

import kotlin.math.*

/**
 * This engine allows manipulation of attributes by wave shapes oscillating in time
 *
 *
 */
abstract class ViewTimeCycle : TimeCycleSplineSet() {
    /**
     * Set the time cycle parameters
     * @param view
     * @param t
     * @param time
     * @param cache
     * @return
     */
    abstract fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean

    /**
     * get a value from the time cycle
     * @param pos
     * @param time
     * @param view
     * @param cache
     * @return
     */
    operator fun get(pos: Float, time: Long, view: TView, cache: KeyCache): Float {
        mCurveFit.getPos(pos, mCache)
        val period: Float = mCache[CURVE_PERIOD]
        if (period == 0f) {
            mContinue = false
            return mCache[CURVE_OFFSET]
        }
        if (Float.isNaN(mLastCycle)) { // it has not been set
            mLastCycle = cache.getFloatValue(view, mType, 0) // check the cache
            if (Float.isNaN(mLastCycle)) {  // not in cache so set to 0 (start)
                mLastCycle = 0f
            }
        }
        val delta_time: Long = time - mLastTime
        mLastCycle = ((mLastCycle + delta_time * 1E-9 * period) % 1.0).toFloat()
        cache.setFloatValue(view, mType, 0, mLastCycle)
        mLastTime = time
        val v: Float = mCache[CURVE_VALUE]
        val wave: Float = calcWave(mLastCycle)
        val offset: Float = mCache[CURVE_OFFSET]
        val value = v * wave + offset
        mContinue = v != 0.0f || period != 0.0f
        return value
    }

    internal class ElevationSet : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setElevation(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class AlphaSet : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setAlpha(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class RotationSet : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setRotation(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class RotationXset : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setRotationX(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class RotationYset : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setRotationY(get(t, time, view, cache))
            return mContinue
        }
    }

    class PathRotate : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            return mContinue
        }

        /**
         * DO NOT CALL
         * @param view
         * @param cache
         * @param t
         * @param time
         * @param dx
         * @param dy
         * @return
         */
        fun setPathRotate(
            view: TView,
            cache: KeyCache,
            t: Float,
            time: Long,
            dx: Double,
            dy: Double
        ): Boolean {
            view.setRotation(
                get(t, time, view, cache)
                        + toDegrees(atan2(dy, dx)).toFloat()
            )
            return mContinue
        }
    }

    internal class ScaleXset : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setScaleX(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class ScaleYset : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setScaleY(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class TranslationXset : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setTranslationX(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class TranslationYset : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setTranslationY(get(t, time, view, cache))
            return mContinue
        }
    }

    internal class TranslationZset : ViewTimeCycle() {
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            view.setTranslationZ(get(t, time, view, cache))
            return mContinue
        }
    }

    class CustomSet constructor(
        attribute: String,
        attrList: SparseArray<ConstraintAttribute>
    ) : ViewTimeCycle() {
        var mAttributeName: String
        var mConstraintAttributeList: SparseArray<ConstraintAttribute>
        var mWaveProperties = SparseArray<FloatArray>()
        lateinit var mTempValues: FloatArray

        init {
            mAttributeName = attribute.split(",")[1]
            mConstraintAttributeList = attrList
        }

        /**
         * Setup the curve
         * @param curveType
         */
        override fun setup(curveType: Int) {
            val size: Int = mConstraintAttributeList.size
            val dimensionality: Int =
                mConstraintAttributeList.valueAt(0)!!.numberOfInterpolatedValues()
            val time = DoubleArray(size)
            mTempValues = FloatArray(dimensionality + 2)
            mCache = FloatArray(dimensionality)
            val values = Array(size) { DoubleArray(dimensionality + 2) }
            for (i in 0 until size) {
                val key = mConstraintAttributeList.keyAt(i)!!
                val ca: ConstraintAttribute = mConstraintAttributeList.valueAt(i)!!
                val waveProp = mWaveProperties.valueAt(i)
                time[i] = key * 1E-2
                ca.getValuesToInterpolate(mTempValues)
                for (k in mTempValues.indices) {
                    values[i][k] = mTempValues[k].toDouble()
                }
                values[i][dimensionality] = waveProp!![0].toDouble()
                values[i][dimensionality + 1] = waveProp[1].toDouble()
            }
            mCurveFit = CurveFit[curveType, time, values]
        }
        // @TODO: add description
        /**
         * @param position
         * @param value
         * @param period
         * @param shape
         * @param offset
         */
        override fun setPoint(position: Int, value: Float, period: Float, shape: Int, offset: Float) {
            throw RuntimeException("Wrong call for custom attribute")
        }

        /**
         * set the keyTimePoint
         * @param position
         * @param value
         * @param period
         * @param shape
         * @param offset
         */
        fun setPoint(
            position: Int, value: ConstraintAttribute,
            period: Float,
            shape: Int,
            offset: Float
        ) {
            mConstraintAttributeList.append(position, value)
            mWaveProperties.append(position, floatArrayOf(period, offset))
            mWaveShape = max(mWaveShape, shape) // the highest value shape is chosen
        }

        /**
         * Set the property of the view given the position the current time
         * @param view the view to set the property of
         * @param t the position on the curve
         * @param time the current time
         * @param cache the cache used to keep the previous position
         * @return true if it will need repaint
         */
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            mCurveFit.getPos(t, mTempValues)
            val period = mTempValues[mTempValues.size - 2]
            val offset = mTempValues[mTempValues.size - 1]
            val delta_time: Long = time - mLastTime
            if (Float.isNaN(mLastCycle)) { // it has not been set
                mLastCycle = cache.getFloatValue(view, mAttributeName, 0) // check the cache
                if (Float.isNaN(mLastCycle)) {  // not in cache so set to 0 (start)
                    mLastCycle = 0f
                }
            }
            mLastCycle = ((mLastCycle + delta_time * 1E-9 * period) % 1.0).toFloat()
            mLastTime = time
            val wave: Float = calcWave(mLastCycle)
            mContinue = false
            for (i in 0 until mCache.size) {
                mContinue = mContinue or (mTempValues[i].toDouble() != 0.0)
                mCache[i] = mTempValues[i] * wave + offset
            }
            CustomSupport.setInterpolatedValue(
                mConstraintAttributeList.valueAt(0)!!,
                view, mCache
            )
            if (period != 0.0f) {
                mContinue = true
            }
            return mContinue
        }
    }

    internal class ProgressSet : ViewTimeCycle() {
        var mNoMethod = false
        override fun setProperty(view: TView, t: Float, time: Long, cache: KeyCache): Boolean {
            if (view.getParentType() is MotionLayout) {
                (view.getParentType() as MotionLayout).progress = get(t, time, view, cache)
            } else {
                view.invokeMethod("setProgress", get(t, time, view, cache))
            }
            return mContinue
        }
    }

    companion object {
        private const val TAG = "ViewTimeCycle"

        /**
         * make a custom time cycle
         * @param str
         * @param attrList
         * @return
         */
        fun makeCustomSpline(
            str: String,
            attrList: SparseArray<ConstraintAttribute>
        ): ViewTimeCycle {
            return CustomSet(str, attrList)
        }

        /**
         * Make a time cycle spline
         * @param str
         * @param currentTime
         * @return
         */
        fun makeSpline(str: String?, currentTime: Long): ViewTimeCycle? {
            val timeCycle: ViewTimeCycle
            timeCycle = when (str) {
                Key.ALPHA -> AlphaSet()
                Key.ELEVATION -> ElevationSet()
                Key.ROTATION -> RotationSet()
                Key.ROTATION_X -> RotationXset()
                Key.ROTATION_Y -> RotationYset()
                Key.TRANSITION_PATH_ROTATE -> PathRotate()
                Key.SCALE_X -> ScaleXset()
                Key.SCALE_Y -> ScaleYset()
                Key.TRANSLATION_X -> TranslationXset()
                Key.TRANSLATION_Y -> TranslationYset()
                Key.TRANSLATION_Z -> TranslationZset()
                Key.PROGRESS -> ProgressSet()
                else -> return null
            }
            timeCycle.setStartTime(currentTime)
            return timeCycle
        }
    }
}