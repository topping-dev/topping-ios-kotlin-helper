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

import com.olekdia.sparsearray.SparseArray
import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.constraintlayout.motion.utils.*
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewState
import dev.topping.ios.constraint.constraintlayout.motion.widget.Key.Companion.UNSET_ID
import dev.topping.ios.constraint.constraintlayout.motion.widget.KeyFrames.Companion.UNSET
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.core.motion.utils.*
import dev.topping.ios.constraint.core.state.Interpolator
import dev.topping.ios.constraint.core.state.interpolators.*
import kotlin.math.*

/**
 * Contains the picture of a view through a transition and is used to interpolate it.
 * During an transition every view has a MotionController which drives its position.
 *
 *
 * All parameter which affect a views motion are added to MotionController and then setup()
 * builds out the splines that control the view.
 *
 *
 */
class MotionController {
    var mTempRect: Rect = Rect() // for efficiency
    var mView: TView? = null
    var mId = ""
    var mForceMeasure = false
    var mConstraintTag: String? = null
    private var mCurveFitType: Int = KeyFrames.UNSET
    private val mStartMotionPath: MotionPaths = MotionPaths()
    private val mEndMotionPath: MotionPaths = MotionPaths()
    private val mStartPoint: MotionConstrainedPoint = MotionConstrainedPoint()
    private val mEndPoint: MotionConstrainedPoint = MotionConstrainedPoint()
    private var mSpline : Array<CurveFit>? = null // spline 0 is the one that process all the standard attributes
    private var mArcSpline: CurveFit? = null
    var mMotionStagger = Float.NaN
    var mStaggerOffset = 0f
    var mStaggerScale = 1.0f

    /**
     * Get the center X of the motion at the current progress
     * @return
     */
    var centerX = 0f

    /**
     * Get the center Y of the motion at the current progress
     * @return
     */
    var centerY = 0f
    private lateinit var mInterpolateVariables: IntArray
    private lateinit var mInterpolateData // scratch data created during setup
            : DoubleArray
    private lateinit var mInterpolateVelocity // scratch data created during setup
            : DoubleArray
    private lateinit var mAttributeNames // the names of the custom attributes
            : Array<String>
    private lateinit var mAttributeInterpolatorCount // how many interpolators for each custom attribute
            : IntArray
    private val mMaxDimension = 4
    private val mValuesBuff = FloatArray(mMaxDimension)
    private val mMotionPaths: MutableList<MotionPaths> = mutableListOf()
    private val mVelocity = FloatArray(1) // used as a temp buffer to return values
    private val mKeyList: MutableList<Key> = mutableListOf() // List of key frame items
    private var mTimeCycleAttributesMap : MutableMap<String, ViewTimeCycle>? = null // splines for use TimeCycles
    private var mAttributesMap : MutableMap<String, ViewSpline>? = null // splines to calculate values of attributes
    private var mCycleMap : MutableMap<String, ViewOscillator>? = null // splines to calculate values of attributes
    private var mKeyTriggers : Array<KeyTrigger>? = null // splines to calculate values of attributes
    private var mPathMotionArc: Int = UNSET
    private var mTransformPivotTarget = UNSET_ID // if set, pivot point is set to the other object
    private var mTransformPivotView: TView? = null // if set, pivot point is set to the other object
    private var mQuantizeMotionSteps: Int = UNSET
    private var mQuantizeMotionPhase = Float.NaN
    private var mQuantizeMotionInterpolator: Interpolator? = null
    private var mNoMovement = false
    /**
     * Get the view to pivot around
     *
     * @return id of view or UNSET if not set
     */
    /**
     * Set a view to pivot around
     *
     * @param transformPivotTarget id of view
     */
    var transformPivotTarget: String
        get() = mTransformPivotTarget
        set(transformPivotTarget) {
            mTransformPivotTarget = transformPivotTarget
            mTransformPivotView = null
        }

    fun getKeyFrame(i: Int): MotionPaths {
        return mMotionPaths[i]
    }

    /**
     * get the left most position of the widget at the start of the movement.
     *
     * @return the left most position
     */
    val startX: Float
        get() = mStartMotionPath.mX

    /**
     * get the top most position of the widget at the start of the movement.
     * Positive is down.
     *
     * @return the top most position
     */
    val startY: Float
        get() = mStartMotionPath.mY

    /**
     * get the left most position of the widget at the end of the movement.
     *
     * @return the left most position
     */
    val finalX: Float
        get() = mEndMotionPath.mX

    /**
     * get the top most position of the widget at the end of the movement.
     * Positive is down.
     *
     * @return the top most position
     */
    val finalY: Float
        get() = mEndMotionPath.mY

    /**
     * get the width of the widget at the start of the movement.
     *
     * @return the width at the start
     */
    val startWidth: Float
        get() = mStartMotionPath.mWidth

    /**
     * get the width of the widget at the start of the movement.
     *
     * @return the height at the start
     */
    val startHeight: Float
        get() = mStartMotionPath.mHeight

    /**
     * get the width of the widget at the end of the movement.
     *
     * @return the width at the end
     */
    val finalWidth: Float
        get() = mEndMotionPath.mWidth

    /**
     * get the width of the widget at the end of the movement.
     *
     * @return the height at the end
     */
    val finalHeight: Float
        get() = mEndMotionPath.mHeight

    /**
     * Will return the id of the view to move relative to.
     * The position at the start and then end will be viewed relative to this view
     * -1 is the return value if NOT in polar mode
     *
     * @return the view id of the view this is in polar mode to or -1 if not in polar
     */
    val animateRelativeTo: String
        get() = mStartMotionPath.mAnimateRelativeTo

    constructor(viewP: TView?) {
        view = viewP
    }

    /**
     * This ties one motionController to another to allow relative pathes
     * @param motionController
     */
    fun setupRelative(motionController: MotionController) {
        mStartMotionPath.setupRelative(motionController, motionController.mStartMotionPath)
        mEndMotionPath.setupRelative(motionController, motionController.mEndMotionPath)
    }

    /**
     * Get a center and velocities at the position Pointer
     * @param p
     * @param pos
     * @param vel
     */
    fun getCenter(p: Double, pos: FloatArray, vel: FloatArray) {
        val position = DoubleArray(4)
        val velocity = DoubleArray(4)
        val temp = IntArray(4)
        mSpline!![0].getPos(p, position)
        mSpline!![0].getSlope(p, velocity)
        Arrays.fill(vel, 0f)
        mStartMotionPath.getCenter(p, mInterpolateVariables, position, pos, velocity, vel)
    }

    /**
     * During the next layout call measure then layout
     */
    fun remeasure() {
        mForceMeasure = true
    }

    /**
     * fill the array point with the center coordinates.
     * point[0] is filled with the
     * x coordinate of "time" 0.0 mPoints[point.length-1] is filled with the y coordinate of "time"
     * 1.0
     *
     * @param points     array to fill (should be 2x the number of mPoints
     * @param pointCount
     * @return number of key frames
     */
    fun buildPath(points: FloatArray, pointCount: Int) {
        val mils = 1.0f / (pointCount - 1)
        val trans_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.TRANSLATION_X]
        val trans_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.TRANSLATION_Y]
        val osc_x: ViewOscillator? = if (mCycleMap == null) null else mCycleMap!![Key.TRANSLATION_X]
        val osc_y: ViewOscillator? = if (mCycleMap == null) null else mCycleMap!![Key.TRANSLATION_Y]
        for (i in 0 until pointCount) {
            var position = i * mils
            if (mStaggerScale != 1.0f) {
                if (position < mStaggerOffset) {
                    position = 0f
                }
                if (position > mStaggerOffset && position < 1.0) {
                    position -= mStaggerOffset
                    position *= mStaggerScale
                    position = min(position, 1.0f)
                }
            }
            var p = position.toDouble()
            var easing: Easing? = mStartMotionPath.mKeyFrameEasing
            var start = 0f
            var end = Float.NaN
            for (frame in mMotionPaths) {
                if (frame.mKeyFrameEasing != null) { // this frame has an easing
                    if (frame.mTime < position) {  // frame with easing is before the current pos
                        easing = frame.mKeyFrameEasing // this is the candidate
                        start = frame.mTime // this is also the starting time
                    } else { // frame with easing is past the pos
                        if (Float.isNaN(end)) { // we never ended the time line
                            end = frame.mTime
                        }
                    }
                }
            }
            if (easing != null) {
                if (Float.isNaN(end)) {
                    end = 1.0f
                }
                var offset = (position - start) / (end - start)
                offset = easing.get(offset.toDouble()).toFloat()
                p = (offset * (end - start) + start).toDouble()
            }
            mSpline!![0].getPos(p, mInterpolateData)
            if (mArcSpline != null) {
                if (mInterpolateData.isNotEmpty()) {
                    mArcSpline!!.getPos(p, mInterpolateData)
                }
            }
            mStartMotionPath.getCenter(
                p, mInterpolateVariables, mInterpolateData,
                points, i * 2
            )
            if (osc_x != null) {
                points[i * 2] += osc_x[position]
            } else if (trans_x != null) {
                points[i * 2] += trans_x[position]
            }
            if (osc_y != null) {
                points[i * 2 + 1] += osc_y[position]
            } else if (trans_y != null) {
                points[i * 2 + 1] += trans_y[position]
            }
        }
    }

    fun getPos(position: Double): DoubleArray {
        mSpline!![0].getPos(position, mInterpolateData)
        if (mArcSpline != null) {
            if (mInterpolateData.isNotEmpty()) {
                mArcSpline!!.getPos(position, mInterpolateData)
            }
        }
        return mInterpolateData
    }

    /**
     * fill the array point with the center coordinates.
     * point[0] is filled with the
     * x coordinate of "time" 0.0 mPoints[point.length-1] is filled with the y coordinate of "time"
     * 1.0
     *
     * @param bounds     array to fill (should be 2x the number of mPoints
     * @param pointCount
     * @return number of key frames
     */
    fun buildBounds(bounds: FloatArray, pointCount: Int) {
        val mils = 1.0f / (pointCount - 1)
        val trans_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.TRANSLATION_X]
        val trans_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.TRANSLATION_Y]
        val osc_x: ViewOscillator? =
            if (mCycleMap == null) null else mCycleMap!![Key.TRANSLATION_X]
        val osc_y: ViewOscillator? =
            if (mCycleMap == null) null else mCycleMap!![Key.TRANSLATION_Y]
        for (i in 0 until pointCount) {
            var position = i * mils
            if (mStaggerScale != 1.0f) {
                if (position < mStaggerOffset) {
                    position = 0f
                }
                if (position > mStaggerOffset && position < 1.0) {
                    position -= mStaggerOffset
                    position *= mStaggerScale
                    position = min(position, 1.0f)
                }
            }
            var p = position.toDouble()
            var easing: Easing? = mStartMotionPath.mKeyFrameEasing
            var start = 0f
            var end = Float.NaN
            for (frame in mMotionPaths) {
                if (frame.mKeyFrameEasing != null) { // this frame has an easing
                    if (frame.mTime < position) {  // frame with easing is before the current pos
                        easing = frame.mKeyFrameEasing // this is the candidate
                        start = frame.mTime // this is also the starting time
                    } else { // frame with easing is past the pos
                        if (Float.isNaN(end)) { // we never ended the time line
                            end = frame.mTime
                        }
                    }
                }
            }
            if (easing != null) {
                if (Float.isNaN(end)) {
                    end = 1.0f
                }
                var offset = (position - start) / (end - start)
                offset = easing.get(offset.toDouble()).toFloat()
                p = (offset * (end - start) + start).toDouble()
            }
            mSpline!![0].getPos(p, mInterpolateData)
            if (mArcSpline != null) {
                if (mInterpolateData.isNotEmpty()) {
                    mArcSpline!!.getPos(p, mInterpolateData)
                }
            }
            mStartMotionPath.getBounds(mInterpolateVariables, mInterpolateData, bounds, i * 2)
        }
    }// we never ended the time line// frame with easing is past the pos// frame with easing is before the current pos

    // this is the candidate
    // this is also the starting time
    // this frame has an easing
    private val preCycleDistance: Float
        private get() {
            val pointCount = 100
            val points = FloatArray(2)
            var sum = 0f
            val mils = 1.0f / (pointCount - 1)
            var x = 0.0
            var y = 0.0
            for (i in 0 until pointCount) {
                val position = i * mils
                var p = position.toDouble()
                var easing: Easing? = mStartMotionPath.mKeyFrameEasing
                var start = 0f
                var end = Float.NaN
                for (frame in mMotionPaths) {
                    if (frame.mKeyFrameEasing != null) { // this frame has an easing
                        if (frame.mTime < position) {  // frame with easing is before the current pos
                            easing = frame.mKeyFrameEasing // this is the candidate
                            start = frame.mTime // this is also the starting time
                        } else { // frame with easing is past the pos
                            if (Float.isNaN(end)) { // we never ended the time line
                                end = frame.mTime
                            }
                        }
                    }
                }
                if (easing != null) {
                    if (Float.isNaN(end)) {
                        end = 1.0f
                    }
                    var offset = (position - start) / (end - start)
                    offset = easing.get(offset.toDouble()).toFloat()
                    p = (offset * (end - start) + start).toDouble()
                }
                mSpline!![0].getPos(p, mInterpolateData)
                mStartMotionPath.getCenter(p, mInterpolateVariables, mInterpolateData, points, 0)
                if (i > 0) {
                    sum += hypot(y - points[1], x - points[0]).toFloat()
                }
                x = points[0].toDouble()
                y = points[1].toDouble()
            }
            return sum
        }

    fun getPositionKeyframe(
        layoutWidth: Int,
        layoutHeight: Int,
        x: Float,
        y: Float
    ): KeyPositionBase? {
        val start = RectF()
        start.left = mStartMotionPath.mX
        start.top = mStartMotionPath.mY
        start.right = start.left + mStartMotionPath.mWidth
        start.bottom = start.top + mStartMotionPath.mHeight
        val end = RectF()
        end.left = mEndMotionPath.mX
        end.top = mEndMotionPath.mY
        end.right = end.left + mEndMotionPath.mWidth
        end.bottom = end.top + mEndMotionPath.mHeight
        for (key in mKeyList) {
            if (key is KeyPositionBase) {
                if ((key as KeyPositionBase).intersects(
                        layoutWidth, layoutHeight,
                        start, end, x, y
                    )
                ) {
                    return key
                }
            }
        }
        return null
    }

    fun buildKeyFrames(keyFrames: FloatArray?, mode: IntArray?): Int {
        if (keyFrames != null) {
            var count = 0
            val time: DoubleArray = mSpline!![0].getTimePoints()
            if (mode != null) {
                for (keyFrame in mMotionPaths) {
                    mode[count++] = keyFrame.mMode
                }
                count = 0
            }
            for (i in time.indices) {
                mSpline!![0].getPos(time[i], mInterpolateData)
                mStartMotionPath.getCenter(
                    time[i], mInterpolateVariables, mInterpolateData,
                    keyFrames, count
                )
                count += 2
            }
            return count / 2
        }
        return 0
    }

    fun buildKeyBounds(keyBounds: FloatArray?, mode: IntArray?): Int {
        if (keyBounds != null) {
            var count = 0
            val time: DoubleArray = mSpline!![0].getTimePoints()
            if (mode != null) {
                for (keyFrame in mMotionPaths) {
                    mode[count++] = keyFrame.mMode
                }
                count = 0
            }
            for (i in time.indices) {
                mSpline!![0].getPos(time[i], mInterpolateData)
                mStartMotionPath.getBounds(
                    mInterpolateVariables, mInterpolateData,
                    keyBounds, count
                )
                count += 2
            }
            return count / 2
        }
        return 0
    }

    var mAttributeTable: Array<String> = arrayOf()
    fun getAttributeValues(attributeType: String?, points: FloatArray, pointCount: Int): Int {
        val mils = 1.0f / (pointCount - 1)
        val spline: SplineSet = mAttributesMap!![attributeType] ?: return -1
        for (j in points.indices) {
            points[j] = spline.get(j.toFloat() / (points.size - 1f))
        }
        return points.size
    }

    fun buildRect(p: Float, path: FloatArray, offset: Int) {
        var p = p
        p = getAdjustedPosition(p, null)
        mSpline!![0].getPos(p, mInterpolateData)
        mStartMotionPath.getRect(mInterpolateVariables, mInterpolateData, path, offset)
    }

    fun buildRectangles(path: FloatArray, pointCount: Int) {
        val mils = 1.0f / (pointCount - 1)
        for (i in 0 until pointCount) {
            var position = i * mils
            position = getAdjustedPosition(position, null)
            mSpline!![0].getPos(position, mInterpolateData)
            mStartMotionPath.getRect(mInterpolateVariables, mInterpolateData, path, i * 8)
        }
    }

    fun getKeyFrameParameter(type: Int, x: Float, y: Float): Float {
        val dx: Float = mEndMotionPath.mX - mStartMotionPath.mX
        val dy: Float = mEndMotionPath.mY - mStartMotionPath.mY
        val startCenterX: Float = mStartMotionPath.mX + mStartMotionPath.mWidth / 2
        val startCenterY: Float = mStartMotionPath.mY + mStartMotionPath.mHeight / 2
        val hypotenuse = hypot(dx, dy).toFloat()
        if (hypotenuse < 0.0000001) {
            return Float.NaN
        }
        val vx = x - startCenterX
        val vy = y - startCenterY
        val distFromStart = hypot(vx, vy).toFloat()
        if (distFromStart == 0f) {
            return 0f
        }
        val pathDistance = vx * dx + vy * dy
        when (type) {
            PATH_PERCENT -> return pathDistance / hypotenuse
            PATH_PERPENDICULAR -> return sqrt(hypotenuse * hypotenuse - pathDistance * pathDistance)
            HORIZONTAL_PATH_X -> return vx / dx
            HORIZONTAL_PATH_Y -> return vy / dx
            VERTICAL_PATH_X -> return vx / dy
            VERTICAL_PATH_Y -> return vy / dy
        }
        return 0f
    }

    private fun insertKey(point: MotionPaths) {
        val pos: Int = mMotionPaths.binarySearch {
            it.compareTo(point)
        }
        if (pos == 0) {
            Log.e(TAG, " KeyPath position \"" + point.mPosition.toString() + "\" outside of range")
        }
        mMotionPaths.add(-pos - 1, point)
    }

    fun addKeys(list: MutableList<Key>?) {
        mKeyList.addAll(list!!)
        if (I_DEBUG) {
            for (key in mKeyList) {
                Log.v(TAG, " ################ set = " + key::class.simpleName)
            }
        }
    }

    /**
     * Add a key to the MotionController
     * @param key
     */
    fun addKey(key: Key) {
        mKeyList.add(key)
        if (I_DEBUG) {
            Log.v(TAG, " ################ addKey = " + key::class.simpleName)
        }
    }

    fun setPathMotionArc(arc: Int) {
        mPathMotionArc = arc
    }

    /**
     * Called after all TimePoints & Cycles have been added;
     * Spines are evaluated
     */
    fun setup(
        parentWidth: Int,
        parentHeight: Int,
        transitionDuration: Float,
        currentTime: Long
    ) {
        val springAttributes: MutableSet<String> = mutableSetOf() // attributes we need to interpolate
        val timeCycleAttributes: MutableSet<String> = mutableSetOf() // attributes we need to interpolate
        val splineAttributes: MutableSet<String> = mutableSetOf() // attributes we need to interpolate
        val cycleAttributes: MutableSet<String> = mutableSetOf() // attributes we need to oscillate
        val interpolation: MutableMap<String, Int> = mutableMapOf()
        var triggerList: MutableList<KeyTrigger>? = null
        if (I_DEBUG) {
            if (mKeyList == null) {
                Log.v(TAG, ">>>>>>>>>>>>>>> mKeyList==null")
            } else {
                Log.v(TAG, ">>>>>>>>>>>>>>> mKeyList for " + Debug.getName(mView))
            }
        }
        if (mPathMotionArc != UNSET) {
            mStartMotionPath.mPathMotionArc = mPathMotionArc
        }
        mStartPoint.different(mEndPoint, splineAttributes)
        if (I_DEBUG) {
            val attr: MutableSet<String> = mutableSetOf()
            mStartPoint.different(mEndPoint, attr)
            Log.v(
                TAG, ">>>>>>>>>>>>>>> MotionConstrainedPoint found "
                        + Arrays.toString(attr.toTypedArray())
            )
        }
        if (mKeyList != null) {
            for (key in mKeyList) {
                if (key is KeyPosition) {
                    val keyPath: KeyPosition = key
                    insertKey(
                        MotionPaths(
                            parentWidth, parentHeight, keyPath,
                            mStartMotionPath, mEndMotionPath
                        )
                    )
                    if (keyPath.mCurveFit !== UNSET) {
                        mCurveFitType = keyPath.mCurveFit
                    }
                } else if (key is KeyCycle) {
                    key.getAttributeNames(cycleAttributes)
                } else if (key is KeyTimeCycle) {
                    key.getAttributeNames(timeCycleAttributes)
                } else if (key is KeyTrigger) {
                    if (triggerList == null) {
                        triggerList = ArrayList()
                    }
                    triggerList.add(key as KeyTrigger)
                } else {
                    key.setInterpolation(interpolation)
                    key.getAttributeNames(splineAttributes)
                }
            }
        }

        //--------------------------- trigger support --------------------
        if (triggerList != null) {
            mKeyTriggers = triggerList.toTypedArray()
        }
        if (I_DEBUG) {
            if (!cycleAttributes.isEmpty()) {
                Log.v(
                    TAG, (">>>>>>>>>>>>>>>>  found cycleA"
                            + Debug.getName(mView)) + " cycles     "
                            + Arrays.toString(cycleAttributes.toTypedArray())
                )
            }
            if (!splineAttributes.isEmpty()) {
                Log.v(
                    TAG, (">>>>>>>>>>>>>>>>  found spline "
                            + Debug.getName(mView)) + " attrs      "
                            + Arrays.toString(splineAttributes.toTypedArray())
                )
            }
            if (!timeCycleAttributes.isEmpty()) {
                Log.v(
                    TAG, (">>>>>>>>>>>>>>>>  found timeCycle "
                            + Debug.getName(mView)) + " attrs      "
                            + Arrays.toString(timeCycleAttributes.toTypedArray())
                )
            }
            if (!springAttributes.isEmpty()) {
                Log.v(
                    TAG, (">>>>>>>>>>>>>>>>  found springs "
                            + Debug.getName(mView)) + " attrs      "
                            + Arrays.toString(springAttributes.toTypedArray())
                )
            }
        }

        //--------------------------- splines support --------------------
        if (splineAttributes.isNotEmpty()) {
            mAttributesMap = mutableMapOf()
            for (attribute in splineAttributes) {
                var splineSets: ViewSpline?
                splineSets = if (attribute.startsWith("CUSTOM,")) {
                    val attrList: SparseArray<ConstraintAttribute> =
                        SparseArray<ConstraintAttribute>()
                    val customAttributeName = attribute.split(",")[1]
                    for (key in mKeyList) {
                        if (key.mCustomConstraints == null) {
                            continue
                        }
                        val customAttribute: ConstraintAttribute? =
                            key.mCustomConstraints?.get(customAttributeName)
                        if (customAttribute != null) {
                            attrList.append(key.mFramePosition, customAttribute!!)
                        }
                    }
                    ViewSpline.makeCustomSpline(attribute, attrList)
                } else {
                    ViewSpline.makeSpline(attribute)
                }
                if (splineSets == null) {
                    continue
                }
                splineSets.setType(attribute)
                mAttributesMap!![attribute] = splineSets
            }
            if (mKeyList != null) {
                for (key in mKeyList) {
                    if (key is KeyAttributes) {
                        key.addValues(mAttributesMap!!)
                    }
                }
            }
            mStartPoint.addValues(mAttributesMap!!, 0)
            mEndPoint.addValues(mAttributesMap!!, 100)
            for (spline in mAttributesMap!!.keys) {
                var curve: Int = CurveFit.SPLINE // default is SPLINE
                if (interpolation.containsKey(spline)) {
                    val boxedCurve: Int? = interpolation[spline]
                    if (boxedCurve != null) {
                        curve = boxedCurve
                    }
                }
                val splineSet: SplineSet? = mAttributesMap!![spline]
                if (splineSet != null) {
                    splineSet.setup(curve)
                }
            }
        }

        //--------------------------- timeCycle support --------------------
        if (timeCycleAttributes.isNotEmpty()) {
            if (mTimeCycleAttributesMap == null) {
                mTimeCycleAttributesMap = HashMap()
            }
            for (attribute in timeCycleAttributes) {
                if (mTimeCycleAttributesMap!!.containsKey(attribute)) {
                    continue
                }
                var splineSets: ViewTimeCycle? = null
                splineSets = if (attribute.startsWith("CUSTOM,")) {
                    val attrList =
                        SparseArray<ConstraintAttribute>()
                    val customAttributeName = attribute.split(",")[1]
                    for (key in mKeyList) {
                        if (key.mCustomConstraints == null) {
                            continue
                        }
                        val customAttribute: ConstraintAttribute? =
                            key.mCustomConstraints?.get(customAttributeName)
                        if (customAttribute != null) {
                            attrList.append(key.mFramePosition, customAttribute)
                        }
                    }
                    ViewTimeCycle.makeCustomSpline(attribute, attrList)
                } else {
                    ViewTimeCycle.makeSpline(attribute, currentTime)
                }
                if (splineSets == null) {
                    continue
                }
                splineSets.setType(attribute)
                mTimeCycleAttributesMap!![attribute] = splineSets
            }
            if (mKeyList != null) {
                for (key in mKeyList) {
                    if (key is KeyTimeCycle) {
                        key.addTimeValues(mTimeCycleAttributesMap!!)
                    }
                }
            }
            for (spline in mTimeCycleAttributesMap!!.keys) {
                var curve: Int = CurveFit.SPLINE // default is SPLINE
                if (interpolation.containsKey(spline)) {
                    curve = interpolation[spline]!!
                }
                mTimeCycleAttributesMap!![spline]!!.setup(curve)
            }
        }

        //--------------------------------- end new key frame 2
        val points: Array<MotionPaths> = Array(2 + mMotionPaths.size) { MotionPaths() }
        var count = 1
        points[0] = mStartMotionPath
        points[points.size - 1] = mEndMotionPath
        if (mMotionPaths.size > 0 && mCurveFitType == UNSET) {
            mCurveFitType = CurveFit.SPLINE
        }
        for (point in mMotionPaths) {
            points[count++] = point
        }

        // -----  setup custom attributes which must be in the start and end constraint sets
        val variables = 18
        val attributeNameSet: MutableSet<String> = mutableSetOf()
        for (s in mEndMotionPath.mAttributes.keys) {
            if (mStartMotionPath.mAttributes.containsKey(s)) {
                if (!splineAttributes.contains("CUSTOM,$s")) {
                    attributeNameSet.add(s)
                }
            }
        }
        mAttributeNames = attributeNameSet.toTypedArray()
        mAttributeInterpolatorCount = IntArray(mAttributeNames.size)
        for (i in mAttributeNames.indices) {
            val attributeName = mAttributeNames[i]
            mAttributeInterpolatorCount[i] = 0
            for (j in points.indices) {
                if (points[j].mAttributes.containsKey(attributeName)) {
                    val attribute: ConstraintAttribute? = points[j].mAttributes.get(attributeName)
                    if (attribute != null) {
                        mAttributeInterpolatorCount[i] += attribute.numberOfInterpolatedValues()
                        break
                    }
                }
            }
        }
        val arcMode = points[0].mPathMotionArc != UNSET
        val mask = BooleanArray(variables + mAttributeNames.size) // defaults to false
        for (i in 1 until points.size) {
            points[i].different(points[i - 1], mask, mAttributeNames, arcMode)
        }
        count = 0
        for (i in 1 until mask.size) {
            if (mask[i]) {
                count++
            }
        }
        mInterpolateVariables = IntArray(count)
        val varLen: Int = max(2, count)
        mInterpolateData = DoubleArray(varLen)
        mInterpolateVelocity = DoubleArray(varLen)
        count = 0
        for (i in 1 until mask.size) {
            if (mask[i]) {
                mInterpolateVariables[count++] = i
            }
        }
        val splineData = Array(points.size) { DoubleArray(mInterpolateVariables.size) }
        val timePoint = DoubleArray(points.size)
        for (i in points.indices) {
            points[i].fillStandard(splineData[i], mInterpolateVariables)
            timePoint[i] = points[i].mTime.toDouble()
        }
        for (j in mInterpolateVariables.indices) {
            val interpolateVariable = mInterpolateVariables[j]
            if (interpolateVariable < MotionPaths.sNames.size) {
                var s: String = MotionPaths.sNames.get(
                    mInterpolateVariables[j]
                ) + " ["
                for (i in points.indices) {
                    s += splineData[i][j]
                }
            }
        }
        mSpline = Array(1 + mAttributeNames.size) {
            CurveFit.get(0, DoubleArray(0), Array(0) { DoubleArray(0) })
        }
        for (i in mAttributeNames.indices) {
            var pointCount = 0
            var splinePoints: Array<DoubleArray>? = null
            lateinit var timePoints: DoubleArray
            val name = mAttributeNames[i]
            for (j in points.indices) {
                if (points[j].hasCustomData(name)) {
                    if (splinePoints == null) {
                        timePoints = DoubleArray(points.size)
                        splinePoints = Array(points.size) {
                            DoubleArray(
                                points[j].getCustomDataCount(name)
                            )
                        }
                    }
                    timePoints[pointCount] = points[j].mTime.toDouble()
                    points[j].getCustomData(name, splinePoints[pointCount], 0)
                    pointCount++
                }
            }
            timePoints = Arrays.copyOf(timePoints, pointCount)
            splinePoints = Arrays.copyOfNonNull(splinePoints!!, pointCount)
            mSpline!![i + 1] = CurveFit.get(mCurveFitType, timePoints, splinePoints!!)
        }
        mSpline!![0] = CurveFit.get(mCurveFitType, timePoint, splineData)
        // --------------------------- SUPPORT ARC MODE --------------
        if (points[0].mPathMotionArc != UNSET) {
            val size = points.size
            val mode = IntArray(size)
            val time = DoubleArray(size)
            val values = Array(size) { DoubleArray(2) }
            for (i in 0 until size) {
                mode[i] = points[i].mPathMotionArc
                time[i] = points[i].mTime.toDouble()
                values[i][0] = points[i].mX.toDouble()
                values[i][1] = points[i].mY.toDouble()
            }
            mArcSpline = CurveFit.getArc(mode, time, values)
        }

        //--------------------------- Cycle support --------------------
        var distance = Float.NaN
        mCycleMap = mutableMapOf()
        if (mKeyList != null) {
            for (attribute in cycleAttributes) {
                val cycle: ViewOscillator = ViewOscillator.makeSpline(attribute) ?: continue
                if (cycle.variesByPath()) {
                    if (Float.isNaN(distance)) {
                        distance = preCycleDistance
                    }
                }
                cycle.setType(attribute)
                mCycleMap!![attribute] = cycle
            }
            for (key in mKeyList) {
                if (key is KeyCycle) {
                    key.addCycleValues(mCycleMap!!)
                }
            }
            for (cycle in mCycleMap!!.values) {
                cycle.setup(distance)
            }
        }
        if (I_DEBUG) {
            Log.v(
                TAG, "Animation of splineAttributes "
                        + Arrays.toString(splineAttributes.toTypedArray())
            )
            Log.v(TAG, "Animation of cycle " + Arrays.toString(mCycleMap!!.keys.toTypedArray()))
            if (mAttributesMap != null) {
                Log.v(
                    TAG, " splines = "
                            + Arrays.toString(mAttributesMap!!.keys.toTypedArray())
                )
                for (s in mAttributesMap!!.keys) {
                    Log.v(TAG, s + " = " + mAttributesMap!![s])
                }
            }
            Log.v(TAG, " ---------------------------------------- ")
        }

        //--------------------------- end cycle support ----------------
    }

    /**
     * Debug string
     *
     * @return
     */
    override fun toString(): String {
        return " start: x: " + mStartMotionPath.mX.toString() + " y: " + mStartMotionPath.mY
            .toString() + " end: x: " + mEndMotionPath.mX.toString() + " y: " + mEndMotionPath.mY
    }

    private fun readView(motionPaths: MotionPaths) {
        mView?.let { mView ->
            motionPaths.setBounds(
                mView.getX().toFloat(), mView.getY().toFloat(),
                mView.getWidth().toFloat(), mView.getHeight().toFloat()
            )
        }
    }
    // @TODO: add description
    /**
     * Get the view that is being controlled
     * @return
     */
    /**
     *
     * @param view
     */
    var view: TView?
        get() = mView
        set(view) {
            mView = view
            mId = view?.getId() ?: UNSET_ID
            val lp = view?.getLayoutParams()
            if (lp is ConstraintLayout.LayoutParams) {
                mConstraintTag = lp.constraintTag
            }
        }

    fun setStartCurrentState(v: TView) {
        mStartMotionPath.mTime = 0f
        mStartMotionPath.mPosition = 0f
        mStartMotionPath.setBounds(v.getX().toFloat(), v.getY().toFloat(), v.getWidth().toFloat(), v.getHeight().toFloat())
        mStartPoint.setState(v)
    }

    /**
     * configure the position of the view
     * @param rect
     * @param v
     * @param rotation
     * @param preWidth
     * @param preHeight
     */
    fun setStartState(rect: ViewState, v: TView, rotation: Int, preWidth: Int, preHeight: Int) {
        mStartMotionPath.mTime = 0f
        mStartMotionPath.mPosition = 0f
        val cx: Int
        val cy: Int
        val r = RectF()
        when (rotation) {
            2 -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                r.left = (preHeight - (cy + rect.width()) / 2).toFloat()
                r.top = ((cx - rect.height()) / 2).toFloat()
                r.right = r.left + rect.width()
                r.bottom = r.top + rect.height()
            }
            1 -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                r.left = ((cy - rect.width()) / 2).toFloat()
                r.top = (preWidth - (cx + rect.height()) / 2).toFloat()
                r.right = r.left + rect.width()
                r.bottom = r.top + rect.height()
            }
        }
        mStartMotionPath.setBounds(r.left, r.top, r.width(), r.height())
        mStartPoint.setState(r.toRect(), v, rotation, rect.rotation)
    }

    fun rotate(rect: Rect, out: Rect, rotation: Int, preHeight: Int, preWidth: Int) {
        val cx: Int
        val cy: Int
        when (rotation) {
            ConstraintSet.ROTATE_PORTRATE_OF_LEFT -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                out.left = preHeight - (cy + rect.width()) / 2
                out.top = (cx - rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
            ConstraintSet.ROTATE_PORTRATE_OF_RIGHT -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                out.left = (cy - rect.width()) / 2
                out.top = preWidth - (cx + rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
            ConstraintSet.ROTATE_LEFT_OF_PORTRATE -> {
                cx = rect.left + rect.right
                cy = rect.bottom + rect.top
                out.left = preHeight - (cy + rect.width()) / 2
                out.top = (cx - rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
            ConstraintSet.ROTATE_RIGHT_OF_PORTRATE -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                out.left = rect.height() / 2 + rect.top - cx / 2
                out.top = preWidth - (cx + rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
        }
    }

    fun setStartState(
        cw: Rect,
        constraintSet: ConstraintSet,
        parentWidth: Int,
        parentHeight: Int
    ) {
        val rotate: Int = constraintSet.mRotate // for rotated frames
        if (rotate != 0) {
            rotate(cw, mTempRect, rotate, parentWidth, parentHeight)
        }
        mStartMotionPath.mTime = 0f
        mStartMotionPath.mPosition = 0f
        readView(mStartMotionPath)
        mStartMotionPath.setBounds(cw.left.toFloat(), cw.top.toFloat(), cw.width().toFloat(), cw.height().toFloat())
        val constraint: ConstraintSet.Constraint = constraintSet.getParameters(mId)
        mStartMotionPath.applyParameters(constraint)
        mMotionStagger = constraint.motion.mMotionStagger
        mStartPoint.setState(cw, constraintSet, rotate, mId)
        mTransformPivotTarget = constraint.transform.transformPivotTarget
        mQuantizeMotionSteps = constraint.motion.mQuantizeMotionSteps
        mQuantizeMotionPhase = constraint.motion.mQuantizeMotionPhase
        mQuantizeMotionInterpolator = getInterpolator(
            mView!!.getContext(),
            constraint.motion.mQuantizeInterpolatorType,
            constraint.motion.mQuantizeInterpolatorString,
            constraint.motion.mQuantizeInterpolatorID
        )
    }

    init {
        view = view
    }

    fun setEndState(cw: Rect, constraintSet: ConstraintSet, parentWidth: Int, parentHeight: Int) {
        var cw: Rect = cw
        val rotate: Int = constraintSet.mRotate // for rotated frames
        if (rotate != 0) {
            rotate(cw, mTempRect, rotate, parentWidth, parentHeight)
            cw = mTempRect
        }
        mEndMotionPath.mTime = 1f
        mEndMotionPath.mPosition = 1f
        readView(mEndMotionPath)
        mEndMotionPath.setBounds(cw.left.toFloat(), cw.top.toFloat(), cw.width().toFloat(), cw.height().toFloat())
        mEndMotionPath.applyParameters(constraintSet.getParameters(mId))
        mEndPoint.setState(cw, constraintSet, rotate, mId)
    }

    fun setBothStates(v: TView) {
        mStartMotionPath.mTime = 0f
        mStartMotionPath.mPosition = 0f
        mNoMovement = true
        mStartMotionPath.setBounds(v.getX().toFloat(), v.getY().toFloat(), v.getWidth().toFloat(), v.getHeight().toFloat())
        mEndMotionPath.setBounds(v.getX().toFloat(), v.getY().toFloat(), v.getWidth().toFloat(), v.getHeight().toFloat())
        mStartPoint.setState(v)
        mEndPoint.setState(v)
    }

    /**
     * Calculates the adjusted position (and optional velocity).
     * Note if requesting velocity staggering is not considered
     *
     * @param position position pre stagger
     * @param velocity return velocity
     * @return actual position accounting for easing and staggering
     */
    private fun getAdjustedPosition(position: Float, velocity: FloatArray?): Float {
        var position = position
        if (velocity != null) {
            velocity[0] = 1f
        } else if (mStaggerScale.toDouble() != 1.0) {
            if (position < mStaggerOffset) {
                position = 0f
            }
            if (position > mStaggerOffset && position < 1.0) {
                position -= mStaggerOffset
                position *= mStaggerScale
                position = min(position, 1.0f)
            }
        }

        // adjust the position based on the easing curve
        var adjusted = position
        var easing: Easing? = mStartMotionPath.mKeyFrameEasing
        var start = 0f
        var end = Float.NaN
        for (frame in mMotionPaths) {
            if (frame.mKeyFrameEasing != null) { // this frame has an easing
                if (frame.mTime < position) {  // frame with easing is before the current pos
                    easing = frame.mKeyFrameEasing // this is the candidate
                    start = frame.mTime // this is also the starting time
                } else { // frame with easing is past the pos
                    if (Float.isNaN(end)) { // we never ended the time line
                        end = frame.mTime
                    }
                }
            }
        }
        if (easing != null) {
            if (Float.isNaN(end)) {
                end = 1.0f
            }
            val offset = (position - start) / (end - start)
            val new_offset = easing.get(offset.toDouble()).toFloat()
            adjusted = new_offset * (end - start) + start
            if (velocity != null) {
                velocity[0] = easing.getDiff(offset.toDouble()).toFloat()
            }
        }
        return adjusted
    }

    fun endTrigger(start: Boolean) {
        if ("button" == Debug.getName(mView)) {
            if (mKeyTriggers != null) {
                for (i in mKeyTriggers!!.indices) {
                    mKeyTriggers!![i].conditionallyFire(if (start) -100f else 100f, mView!!)
                }
            }
        }
    }

    /**
     * The main driver of interpolation
     *
     * @param child
     * @param globalPosition
     * @param time
     * @param keyCache
     * @return do you need to keep animating
     */
    fun interpolate(child: TView, globalPosition: Float, time: Long, keyCache: KeyCache): Boolean {
        var timeAnimation = false
        var position = getAdjustedPosition(globalPosition, null)
        // This quantize the position into steps e.g. 4 steps = 0-0.25,0.25-0.50 etc
        if (mQuantizeMotionSteps != UNSET) {
            val steps = 1.0f / mQuantizeMotionSteps // the length of a step
            val jump = floor(position / steps).toFloat() * steps // step jumps
            var section = position % steps / steps // float from 0 to 1 in a step
            if (!Float.isNaN(mQuantizeMotionPhase)) {
                section = (section + mQuantizeMotionPhase) % 1
            }
            section = if (mQuantizeMotionInterpolator != null) {
                mQuantizeMotionInterpolator!!.getInterpolation(section)
            } else {
                if (section > 0.5) 1f else 0f
            }
            position = section * steps + jump
        }
        var timePathRotate: ViewTimeCycle.PathRotate? = null
        if (mAttributesMap != null) {
            for (aSpline in mAttributesMap!!.values) {
                aSpline.setProperty(child, position)
            }
        }
        if (mTimeCycleAttributesMap != null) {
            for (aSpline in mTimeCycleAttributesMap!!.values) {
                if (aSpline is ViewTimeCycle.PathRotate) {
                    timePathRotate = aSpline
                    continue
                }
                timeAnimation =
                    timeAnimation or aSpline.setProperty(child, position, time, keyCache)
            }
        }
        if (mSpline != null) {
            mSpline!![0].getPos(position, mInterpolateData)
            mSpline!![0].getSlope(position.toDouble(), mInterpolateVelocity)
            if (mArcSpline != null) {
                if (mInterpolateData.isNotEmpty()) {
                    mArcSpline!!.getPos(position, mInterpolateData)
                    mArcSpline!!.getSlope(position.toDouble(), mInterpolateVelocity)
                }
            }
            if (!mNoMovement) {
                mStartMotionPath.setView(
                    position, child,
                    mInterpolateVariables, mInterpolateData, mInterpolateVelocity,
                    null, mForceMeasure
                )
                mForceMeasure = false
            }
            if (mTransformPivotTarget != UNSET_ID) {
                if (mTransformPivotView == null) {
                    val layout: TView = child.getParent() as TView
                    mTransformPivotView = layout.findViewById(mTransformPivotTarget)
                }
                if (mTransformPivotView != null) {
                    val cy: Float = (mTransformPivotView!!.getTop()
                            + mTransformPivotView!!.getBottom()) / 2.0f
                    val cx: Float = (mTransformPivotView!!.getLeft()
                            + mTransformPivotView!!.getRight()) / 2.0f
                    if (child.getRight() - child.getLeft() > 0
                        && child.getBottom() - child.getTop() > 0
                    ) {
                        val px: Float = cx - child.getLeft()
                        val py: Float = cy - child.getTop()
                        child.setPivotX(px)
                        child.setPivotY(py)
                    }
                }
            }
            if (mAttributesMap != null) {
                for (aSpline in mAttributesMap!!.values) {
                    if (aSpline is ViewSpline.PathRotate
                        && mInterpolateVelocity.size > 1
                    ) {
                        (aSpline as ViewSpline.PathRotate).setPathRotate(
                            child, position,
                            mInterpolateVelocity[0], mInterpolateVelocity[1]
                        )
                    }
                }
            }
            if (timePathRotate != null) {
                timeAnimation = timeAnimation or timePathRotate.setPathRotate(
                    child, keyCache, position, time,
                    mInterpolateVelocity[0], mInterpolateVelocity[1]
                )
            }
            for (i in 1 until mSpline!!.size) {
                val spline: CurveFit = mSpline!![i]
                spline.getPos(position, mValuesBuff)
                CustomSupport.setInterpolatedValue(
                    mStartMotionPath.mAttributes.get(mAttributeNames[i - 1])!!,
                    child,
                    mValuesBuff
                )
            }
            if (mStartPoint.mVisibilityMode == ConstraintSet.VISIBILITY_MODE_NORMAL) {
                if (position <= 0.0f) {
                    child.setVisibility(mStartPoint.mVisibility)
                } else if (position >= 1.0f) {
                    child.setVisibility(mEndPoint.mVisibility)
                } else if (mEndPoint.mVisibility != mStartPoint.mVisibility) {
                    child.setVisibility(TView.VISIBLE)
                }
            }
            if (mKeyTriggers != null) {
                for (i in mKeyTriggers!!.indices) {
                    mKeyTriggers!![i].conditionallyFire(position, child)
                }
            }
        } else {
            // do the interpolation
            val float_l: Float = (mStartMotionPath.mX
                    + (mEndMotionPath.mX - mStartMotionPath.mX) * position)
            val float_t: Float = (mStartMotionPath.mY
                    + (mEndMotionPath.mY - mStartMotionPath.mY) * position)
            val float_width: Float = (mStartMotionPath.mWidth
                    + (mEndMotionPath.mWidth - mStartMotionPath.mWidth) * position)
            val float_height: Float = (mStartMotionPath.mHeight
                    + (mEndMotionPath.mHeight - mStartMotionPath.mHeight) * position)
            var l = (0.5f + float_l).toInt()
            var t = (0.5f + float_t).toInt()
            var r = (0.5f + float_l + float_width).toInt()
            var b = (0.5f + float_t + float_height).toInt()
            var width = r - l
            var height = b - t
            if (FAVOR_FIXED_SIZE_VIEWS) {
                l = (mStartMotionPath.mX
                        + (mEndMotionPath.mX - mStartMotionPath.mX) * position) as Int
                t = (mStartMotionPath.mY
                        + (mEndMotionPath.mY - mStartMotionPath.mY) * position) as Int
                width = (mStartMotionPath.mWidth
                        + (mEndMotionPath.mWidth - mStartMotionPath.mWidth) * position) as Int
                height = (mStartMotionPath.mHeight
                        + (mEndMotionPath.mHeight - mStartMotionPath.mHeight) * position) as Int
                r = l + width
                b = t + height
            }
            if (mEndMotionPath.mWidth != mStartMotionPath.mWidth || mEndMotionPath.mHeight != mStartMotionPath.mHeight || mForceMeasure) {
                val widthMeasureSpec: Int =
                    child.makeMeasureSpec(width, TView.MeasureSpec.EXACTLY)
                val heightMeasureSpec: Int =
                    child.makeMeasureSpec(height, TView.MeasureSpec.EXACTLY)
                child.measure(widthMeasureSpec, heightMeasureSpec)
                mForceMeasure = false
            }
            child.layout(l, t, r, b)
        }
        if (mCycleMap != null) {
            for (osc in mCycleMap!!.values) {
                if (osc is ViewOscillator.PathRotateSet) {
                    osc.setPathRotate(
                        child, position,
                        mInterpolateVelocity[0], mInterpolateVelocity[1]
                    )
                } else {
                    osc.setProperty(child, position)
                }
            }
        }
        return timeAnimation
    }

    /**
     * This returns the differential with respect to the animation layout position (Progress)
     * of a point on the view (post layout effects are not computed)
     *
     * @param position    position in time
     * @param locationX   the x location on the view (0 = left edge, 1 = right edge)
     * @param locationY   the y location on the view (0 = top, 1 = bottom)
     * @param mAnchorDpDt returns the differential of the motion with respect to the position
     */
    fun getDpDt(position: Float, locationX: Float, locationY: Float, mAnchorDpDt: FloatArray) {
        var position = position
        position = getAdjustedPosition(position, mVelocity)
        if (mSpline != null) {
            mSpline!![0].getSlope(position.toDouble(), mInterpolateVelocity)
            mSpline!![0].getPos(position, mInterpolateData)
            val v = mVelocity[0]
            for (i in mInterpolateVelocity.indices) {
                mInterpolateVelocity[i] *= v.toDouble()
            }
            if (mArcSpline != null) {
                if (mInterpolateData.size > 0) {
                    mArcSpline!!.getPos(position, mInterpolateData)
                    mArcSpline!!.getSlope(position.toDouble(), mInterpolateVelocity)
                    mStartMotionPath.setDpDt(
                        locationX, locationY, mAnchorDpDt,
                        mInterpolateVariables, mInterpolateVelocity, mInterpolateData
                    )
                }
                return
            }
            mStartMotionPath.setDpDt(
                locationX, locationY, mAnchorDpDt,
                mInterpolateVariables, mInterpolateVelocity, mInterpolateData
            )
            return
        }
        // do the interpolation
        val dleft: Float = mEndMotionPath.mX - mStartMotionPath.mX
        val dTop: Float = mEndMotionPath.mY - mStartMotionPath.mY
        val dWidth: Float = mEndMotionPath.mWidth - mStartMotionPath.mWidth
        val dHeight: Float = mEndMotionPath.mHeight - mStartMotionPath.mHeight
        val dRight = dleft + dWidth
        val dBottom = dTop + dHeight
        mAnchorDpDt[0] = dleft * (1 - locationX) + dRight * locationX
        mAnchorDpDt[1] = dTop * (1 - locationY) + dBottom * locationY
    }

    /**
     * This returns the differential with respect to the animation post layout transform
     * of a point on the view
     *
     * @param position    position in time
     * @param width       width of the view
     * @param height      height of the view
     * @param locationX   the x location on the view (0 = left edge, 1 = right edge)
     * @param locationY   the y location on the view (0 = top, 1 = bottom)
     * @param mAnchorDpDt returns the differential of the motion with respect to the position
     */
    fun getPostLayoutDvDp(
        position: Float,
        width: Int,
        height: Int,
        locationX: Float,
        locationY: Float,
        mAnchorDpDt: FloatArray
    ) {
        var position = position
        if (I_DEBUG) {
            Log.v(
                TAG, " position= " + position + " location= "
                        + locationX + " , " + locationY
            )
        }
        position = getAdjustedPosition(position, mVelocity)
        val trans_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.TRANSLATION_X]
        val trans_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.TRANSLATION_Y]
        val rotation: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.ROTATION]
        val scale_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.SCALE_X]
        val scale_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![Key.SCALE_Y]
        val osc_x: ViewOscillator? = if (mCycleMap == null) null else mCycleMap!![Key.TRANSLATION_X]
        val osc_y: ViewOscillator? = if (mCycleMap == null) null else mCycleMap!![Key.TRANSLATION_Y]
        val osc_r: ViewOscillator? = if (mCycleMap == null) null else mCycleMap!![Key.ROTATION]
        val osc_sx: ViewOscillator? = if (mCycleMap == null) null else mCycleMap!![Key.SCALE_X]
        val osc_sy: ViewOscillator? = if (mCycleMap == null) null else mCycleMap!![Key.SCALE_Y]
        val vmat = VelocityMatrix()
        vmat.clear()
        vmat.setRotationVelocity(rotation, position)
        vmat.setTranslationVelocity(trans_x, trans_y, position)
        vmat.setScaleVelocity(scale_x, scale_y, position)
        vmat.setRotationVelocity(osc_r, position)
        vmat.setTranslationVelocity(osc_x, osc_y, position)
        vmat.setScaleVelocity(osc_sx, osc_sy, position)
        if (mArcSpline != null) {
            if (mInterpolateData.isNotEmpty()) {
                mArcSpline!!.getPos(position, mInterpolateData)
                mArcSpline!!.getSlope(position.toDouble(), mInterpolateVelocity)
                mStartMotionPath.setDpDt(
                    locationX, locationY, mAnchorDpDt,
                    mInterpolateVariables, mInterpolateVelocity, mInterpolateData
                )
            }
            vmat.applyTransform(locationX, locationY, width, height, mAnchorDpDt)
            return
        }
        if (mSpline != null) {
            position = getAdjustedPosition(position, mVelocity)
            mSpline!![0].getSlope(position.toDouble(), mInterpolateVelocity)
            mSpline!![0].getPos(position, mInterpolateData)
            val v = mVelocity[0]
            for (i in mInterpolateVelocity.indices) {
                mInterpolateVelocity[i] *= v.toDouble()
            }
            mStartMotionPath.setDpDt(
                locationX, locationY, mAnchorDpDt,
                mInterpolateVariables, mInterpolateVelocity, mInterpolateData
            )
            vmat.applyTransform(locationX, locationY, width, height, mAnchorDpDt)
            return
        }

        // do the interpolation
        val dleft: Float = mEndMotionPath.mX - mStartMotionPath.mX
        val dTop: Float = mEndMotionPath.mY - mStartMotionPath.mY
        val dWidth: Float = mEndMotionPath.mWidth - mStartMotionPath.mWidth
        val dHeight: Float = mEndMotionPath.mHeight - mStartMotionPath.mHeight
        val dRight = dleft + dWidth
        val dBottom = dTop + dHeight
        mAnchorDpDt[0] = dleft * (1 - locationX) + dRight * locationX
        mAnchorDpDt[1] = dTop * (1 - locationY) + dBottom * locationY
        vmat.clear()
        vmat.setRotationVelocity(rotation, position)
        vmat.setTranslationVelocity(trans_x, trans_y, position)
        vmat.setScaleVelocity(scale_x, scale_y, position)
        vmat.setRotationVelocity(osc_r, position)
        vmat.setTranslationVelocity(osc_x, osc_y, position)
        vmat.setScaleVelocity(osc_sx, osc_sy, position)
        vmat.applyTransform(locationX, locationY, width, height, mAnchorDpDt)
        return
    }

    /**
     * returns the draw path mode
     * @return
     */
    var drawPath: Int
        get() {
            var mode: Int = mStartMotionPath.mDrawPath
            for (keyFrame in mMotionPaths) {
                mode = max(mode, keyFrame.mDrawPath)
            }
            mode = max(mode, mEndMotionPath.mDrawPath)
            return mode
        }
        set(debugMode) {
            mStartMotionPath.mDrawPath = debugMode
        }

    fun name(): String {
        val context: TContext = mView!!.getContext()
        return context.getResources().getResourceEntryName(mView!!.getId())
    }

    fun positionKeyframe(
        view: TView?,
        key: KeyPositionBase,
        x: Float,
        y: Float,
        attribute: Array<String>,
        value: FloatArray
    ) {
        val start = RectF()
        start.left = mStartMotionPath.mX
        start.top = mStartMotionPath.mY
        start.right = start.left + mStartMotionPath.mWidth
        start.bottom = start.top + mStartMotionPath.mHeight
        val end = RectF()
        end.left = mEndMotionPath.mX
        end.top = mEndMotionPath.mY
        end.right = end.left + mEndMotionPath.mWidth
        end.bottom = end.top + mEndMotionPath.mHeight
        key.positionAttributes(view!!, start, end, x, y, attribute, value)
    }

    /**
     * Get the keyFrames for the view controlled by this MotionController
     *
     * @param type is position(0-100) + 1000*mType(1=Attr, 2=Pos, 3=TimeCycle 4=Cycle 5=Trigger
     * @param pos  the x&y position of the keyFrame along the path
     * @return Number of keyFrames found
     */
    fun getKeyFramePositions(type: IntArray, pos: FloatArray): Int {
        var i = 0
        var count = 0
        for (key in mKeyList) {
            type[i++] = key.mFramePosition + 1000 * key.mType
            val time: Float = key.mFramePosition / 100.0f
            mSpline!![0].getPos(time, mInterpolateData)
            mStartMotionPath.getCenter(
                time.toDouble(),
                mInterpolateVariables,
                mInterpolateData,
                pos,
                count
            )
            count += 2
        }
        return i
    }

    /**
     * Get the keyFrames for the view controlled by this MotionController.
     * the info data structure is of the form
     * 0 length if your are at index i the [i+len+1] is the next entry
     * 1 type  1=Attributes, 2=Position, 3=TimeCycle 4=Cycle 5=Trigger
     * 2 position
     * 3 x location
     * 4 y location
     * 5
     * ...
     * length
     *
     * @param info is a data structure array of int that holds info on each keyframe
     * @return Number of keyFrames found
     */
    fun getKeyFrameInfo(type: Int, info: IntArray): Int {
        var count = 0
        var cursor = 0
        val pos = FloatArray(2)
        var len: Int
        for (key in mKeyList) {
            if (key.mType != type && type == -1) {
                continue
            }
            len = cursor
            info[cursor] = 0
            info[++cursor] = key.mType
            info[++cursor] = key.mFramePosition
            val time: Float = key.mFramePosition / 100.0f
            mSpline!![0].getPos(time, mInterpolateData)
            mStartMotionPath.getCenter(
                time.toDouble(),
                mInterpolateVariables,
                mInterpolateData,
                pos,
                0
            )
            info[++cursor] = pos[0].toBits()
            info[++cursor] = pos[1].toBits()
            if (key is KeyPosition) {
                val kp: KeyPosition = key
                info[++cursor] = kp.mPositionType
                info[++cursor] = kp.mPercentX.toBits()
                info[++cursor] = kp.mPercentY.toBits()
            }
            cursor++
            info[len] = cursor - len
            count++
        }
        return count
    }

    companion object {
        const val PATH_PERCENT = 0
        const val PATH_PERPENDICULAR = 1
        const val HORIZONTAL_PATH_X = 2
        const val HORIZONTAL_PATH_Y = 3
        const val VERTICAL_PATH_X = 4
        const val VERTICAL_PATH_Y = 5
        const val DRAW_PATH_NONE = 0
        const val DRAW_PATH_BASIC = 1
        const val DRAW_PATH_RELATIVE = 2
        const val DRAW_PATH_CARTESIAN = 3
        const val DRAW_PATH_AS_CONFIGURED = 4
        const val DRAW_PATH_RECTANGLE = 5
        const val DRAW_PATH_SCREEN = 6
        const val ROTATION_RIGHT = 1
        const val ROTATION_LEFT = 2
        private const val TAG = "MotionController"
        private const val I_DEBUG = false
        private const val FAVOR_FIXED_SIZE_VIEWS = false
        const val EASE_IN_OUT = 0
        const val EASE_IN = 1
        const val EASE_OUT = 2
        const val LINEAR = 3
        const val BOUNCE = 4
        const val OVERSHOOT = 5
        private const val SPLINE_STRING = -1
        private const val INTERPOLATOR_REFERENCE_ID = -2
        private const val INTERPOLATOR_UNDEFINED = -3
        private fun getInterpolator(
            context: TContext,
            type: Int,
            interpolatorString: String?,
            id: String?
        ): Interpolator? {
            when (type) {
                SPLINE_STRING -> {
                    val easing: Easing? = Easing.getInterpolator(interpolatorString)
                    return object : Interpolator {
                        override fun getInterpolation(v: Float): Float {
                            return easing?.get(v.toDouble())?.toFloat() ?: 0f
                        }
                    }
                }
                INTERPOLATOR_REFERENCE_ID -> return context.loadInterpolator(id!!)
                EASE_IN_OUT -> return AccelerateDecelerateInterpolator()
                EASE_IN -> return AccelerateInterpolator()
                EASE_OUT -> return DecelerateInterpolator()
                LINEAR -> return null
                BOUNCE -> return BounceInterpolator()
                OVERSHOOT -> return OvershootInterpolator()
                INTERPOLATOR_UNDEFINED -> return null
            }
            return null
        }
    }
}