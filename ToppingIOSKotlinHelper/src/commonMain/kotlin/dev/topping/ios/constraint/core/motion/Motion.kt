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

import dev.topping.ios.constraint.Arrays
import dev.topping.ios.constraint.core.motion.MotionWidget.Companion.UNSET
import dev.topping.ios.constraint.core.motion.key.*
import dev.topping.ios.constraint.core.motion.utils.*
import dev.topping.ios.constraint.isNaN

import kotlin.math.*

/**
 * Contains the picture of a view through a transition and is used to interpolate it.
 * During a transition every view has a MotionController which drives its position.
 *
 *
 * All parameter which affect a views motion are added to MotionController and then setup()
 * builds out the splines that control the view.
 *
 *
 */
class Motion(view: MotionWidget?) : TypedValues {
    var mTempRect: Rect = Rect() // for efficiency
    var mView: MotionWidget? = null
    var mId: String? = null
    var mConstraintTag: String? = null
    private var mCurveFitType: Int = CurveFit.SPLINE
    private val mStartMotionPath: MotionPaths = MotionPaths()
    private val mEndMotionPath: MotionPaths = MotionPaths()
    private val mStartPoint: MotionConstrainedPoint = MotionConstrainedPoint()
    private val mEndPoint: MotionConstrainedPoint = MotionConstrainedPoint()

    // spline 0 is the generic one that process all the standard attributes
    private var mSpline: Array<CurveFit?> = arrayOf()
    private var mArcSpline: CurveFit? = null

    /**
     * The values set in
     * motion: {
     * stagger: '2'
     * }
     *
     * @return value from motion: { stagger: ? } or NaN if not set
     */
    var motionStagger = Float.NaN
    var mStaggerOffset = 0f
    var mStaggerScale = 1.0f
    var centerX = 0f
    var centerY = 0f
    private var mInterpolateVariables: IntArray = intArrayOf()
    private var mInterpolateData // scratch data created during setup
            : DoubleArray = doubleArrayOf()
    private var mInterpolateVelocity // scratch data created during setup
            : DoubleArray = doubleArrayOf()
    private var mAttributeNames // the names of the custom attributes
            : Array<String?> = arrayOf()
    private var mAttributeInterpolatorCount // how many interpolators for each custom attribute
            : IntArray = intArrayOf()
    private val mMaxDimension = 4
    private val mValuesBuff = FloatArray(mMaxDimension)
    private val mMotionPaths: MutableList<MotionPaths> = mutableListOf()
    private val mVelocity = FloatArray(1) // used as a temp buffer to return values
    private val mKeyList: MutableList<MotionKey> = mutableListOf() // List of key frame items

    // splines to calculate for use TimeCycles
    private var mTimeCycleAttributesMap: MutableMap<String, TimeCycleSplineSet>? = null
    private var mAttributesMap // splines to calculate values of attributes
            : MutableMap<String, SplineSet>? = null

    // splines to calculate values of attributes
    private var mCycleMap: MutableMap<String, KeyCycleOscillator?>? = null
    private var mKeyTriggers // splines to calculate values of attributes
            : Array<MotionKeyTrigger?> = arrayOf()
    private var mPathMotionArc: Int = UNSET

    // if set, pivot point is maintained as the other object
    private var mTransformPivotTarget: Int = UNSET

    // if set, pivot point is maintained as the other object
    private var mTransformPivotView: MotionWidget? = null
    private var mQuantizeMotionSteps: Int = UNSET
    private var mQuantizeMotionPhase = Float.NaN
    private var mQuantizeMotionInterpolator: DifferentialInterpolator? = null
    private var mNoMovement = false
    var mRelativeMotion: Motion? = null
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
    var transformPivotTarget: Int
        get() = mTransformPivotTarget
        set(transformPivotTarget) {
            mTransformPivotTarget = transformPivotTarget
            mTransformPivotView = null
        }

    /**
     * provides access to MotionPath objects
     */
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
     * Returns the id of the view to move relative to.
     * The position at the start and then end will be viewed relative to this view
     * -1 is the return value if NOT in polar mode
     *
     * @return the view id of the view this is in polar mode to or -1 if not in polar
     */
    val animateRelativeTo: String?
        get() = mStartMotionPath.mAnimateRelativeTo

    /**
     * set up the motion to be relative to this other motionController
     */
    fun setupRelative(motionController: Motion?) {
        mRelativeMotion = motionController
    }

    private fun setupRelative() {
        if (mRelativeMotion == null) {
            return
        }
        mStartMotionPath.setupRelative(mRelativeMotion, mRelativeMotion!!.mStartMotionPath)
        mEndMotionPath.setupRelative(mRelativeMotion, mRelativeMotion!!.mEndMotionPath)
    }

    // @TODO: add description
    fun getCenter(p: Double, pos: FloatArray, vel: FloatArray) {
        val position = DoubleArray(4)
        val velocity = DoubleArray(4)
        mSpline[0]?.getPos(p, position)
        mSpline[0]?.getSlope(p, velocity)
        Arrays.fill(vel, 0f)
        mStartMotionPath.getCenter(p, mInterpolateVariables, position, pos, velocity, vel)
    }

    /**
     * Fills the array point with the center coordinates.
     * point[0] is filled with the
     * x coordinate of "time" 0.0 mPoints[point.length-1] is filled with the y coordinate of "time"
     * 1.0
     *
     * @param points array to fill (should be 2x the number of mPoints
     */
    fun buildPath(points: FloatArray, pointCount: Int) {
        val mils = 1.0f / (pointCount - 1)
        val trans_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.TRANSLATION_X]
        val trans_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.TRANSLATION_Y]
        val osc_x: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.TRANSLATION_X]
        val osc_y: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.TRANSLATION_Y]
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
            mSpline[0]?.getPos(p, mInterpolateData)
            if (mArcSpline != null) {
                if (mInterpolateData.size > 0) {
                    mArcSpline?.getPos(p, mInterpolateData)
                }
            }
            mStartMotionPath.getCenter(p, mInterpolateVariables, mInterpolateData, points, i * 2)
            if (osc_x != null) {
                points[i * 2] += osc_x.get(position)
            } else if (trans_x != null) {
                points[i * 2] += trans_x.get(position)
            }
            if (osc_y != null) {
                points[i * 2 + 1] += osc_y.get(position)
            } else if (trans_y != null) {
                points[i * 2 + 1] += trans_y.get(position)
            }
        }
    }

    fun getPos(position: Double): DoubleArray {
        mSpline[0]?.getPos(position, mInterpolateData)
        if (mArcSpline != null) {
            if (mInterpolateData.size > 0) {
                mArcSpline?.getPos(position, mInterpolateData)
            }
        }
        return mInterpolateData
    }

    /**
     * Fills the array point with the center coordinates.
     * point[0] is filled with the
     * x coordinate of "time" 0.0 mPoints[point.length-1] is filled with the y coordinate of "time"
     * 1.0
     *
     * @param bounds array to fill (should be 2x the number of mPoints
     * @return number of key frames
     */
    fun buildBounds(bounds: FloatArray, pointCount: Int) {
        val mils = 1.0f / (pointCount - 1)
         val trans_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.TRANSLATION_X]
         val trans_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.TRANSLATION_Y]
         val osc_x: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.TRANSLATION_X]
         val osc_y: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.TRANSLATION_Y]
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
            mSpline[0]?.getPos(p, mInterpolateData)
            if (mArcSpline != null) {
                if (mInterpolateData.size > 0) {
                    mArcSpline?.getPos(p, mInterpolateData)
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
                mSpline[0]?.getPos(p, mInterpolateData)
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
    ): MotionKeyPosition? {
        val start = FloatRect()
        start.left = mStartMotionPath.mX
        start.top = mStartMotionPath.mY
        start.right = start.left + mStartMotionPath.mWidth
        start.bottom = start.top + mStartMotionPath.mHeight
        val end = FloatRect()
        end.left = mEndMotionPath.mX
        end.top = mEndMotionPath.mY
        end.right = end.left + mEndMotionPath.mWidth
        end.bottom = end.top + mEndMotionPath.mHeight
        for (key in mKeyList) {
            if (key is MotionKeyPosition) {
                if (key.intersects(
                        layoutWidth,
                        layoutHeight, start, end, x, y
                    )
                ) {
                    return key
                }
            }
        }
        return null
    }

    // @TODO: add description
    fun buildKeyFrames(keyFrames: FloatArray?, mode: IntArray?, pos: IntArray?): Int {
        if (keyFrames != null) {
            var count = 0
            val time: DoubleArray = mSpline[0]?.getTimePoints() ?: doubleArrayOf()
            if (mode != null) {
                for (keyFrame in mMotionPaths) {
                    mode[count++] = keyFrame.mMode
                }
                count = 0
            }
            if (pos != null) {
                for (keyFrame in mMotionPaths) {
                    pos[count++] = (100 * keyFrame.mPosition).toInt()
                }
                count = 0
            }
            for (i in time.indices) {
                mSpline[0]?.getPos(time[i], mInterpolateData)
                mStartMotionPath.getCenter(
                    time[i],
                    mInterpolateVariables, mInterpolateData, keyFrames, count
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
            val time: DoubleArray = mSpline[0]?.getTimePoints() ?: doubleArrayOf()
            if (mode != null) {
                for (keyFrame in mMotionPaths) {
                    mode[count++] = keyFrame.mMode
                }
                count = 0
            }
            for (i in time.indices) {
                mSpline[0]?.getPos(time[i], mInterpolateData)
                mStartMotionPath.getBounds(
                    mInterpolateVariables,
                    mInterpolateData, keyBounds, count
                )
                count += 2
            }
            return count / 2
        }
        return 0
    }

    var mAttributeTable: Array<String> = arrayOf()

    fun getAttributeValues(attributeType: String, points: FloatArray, pointCount: Int): Int {
         val mils = 1.0f / (pointCount - 1)
        val spline: SplineSet = mAttributesMap?.get(attributeType) ?: return -1
        for (j in points.indices) {
            points[j] = spline.get((j / (points.size - 1)).toFloat())
        }
        return points.size
    }

    // @TODO: add description
    fun buildRect(p: Float, path: FloatArray, offset: Int) {
        var p = p
        p = getAdjustedPosition(p, null)
        mSpline[0]?.getPos(p.toDouble(), mInterpolateData)
        mStartMotionPath.getRect(mInterpolateVariables, mInterpolateData, path, offset)
    }

    fun buildRectangles(path: FloatArray, pointCount: Int) {
        val mils = 1.0f / (pointCount - 1)
        for (i in 0 until pointCount) {
            var position = i * mils
            position = getAdjustedPosition(position, null)
            mSpline[0]?.getPos(position.toDouble(), mInterpolateData)
            mStartMotionPath.getRect(mInterpolateVariables, mInterpolateData, path, i * 8)
        }
    }

    fun getKeyFrameParameter(type: Int, x: Float, y: Float): Float {
        val dx: Float = mEndMotionPath.mX - mStartMotionPath.mX
        val dy: Float = mEndMotionPath.mY - mStartMotionPath.mY
        val startCenterX: Float = mStartMotionPath.mX + mStartMotionPath.mWidth / 2
        val startCenterY: Float = mStartMotionPath.mY + mStartMotionPath.mHeight / 2
        val hypotenuse = hypot(dx, dy)
        if (hypotenuse < 0.0000001) {
            return Float.NaN
        }
        val vx = x - startCenterX
        val vy = y - startCenterY
        val distFromStart = hypot(vx, vy)
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
        var redundant: MotionPaths? = null
        for (p in mMotionPaths) {
            if (point.mPosition == p.mPosition) {
                redundant = p
            }
        }
        if (redundant != null) {
            mMotionPaths.remove(redundant)
        }
        val pos: Int = Arrays.binarySearch(mMotionPaths, point)
        if (pos == 0) {
            Utils.loge(
                TAG,
                " KeyPath position \"" + point.mPosition.toString() + "\" outside of range"
            )
        }
        mMotionPaths.add(-pos - 1, point)
    }

    fun addKeys(list: ArrayList<MotionKey>?) {
        mKeyList.addAll(list!!)
        if (I_DEBUG) {
            for (key in mKeyList) {
                Utils.log(TAG, " ################ set = " + key::class.toString())
            }
        }
    }

    // @TODO: add description
    fun addKey(key: MotionKey) {
        mKeyList.add(key)
        if (I_DEBUG) {
            Utils.log(TAG, " ################ addKey = " + key::class.toString())
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
        val springAttributes: MutableSet<String> = mutableSetOf()
        // attributes we need to interpolate
        val timeCycleAttributes: MutableSet<String> = mutableSetOf() // attributes we need to interpolate
        val splineAttributes: MutableSet<String> = mutableSetOf() // attributes we need to interpolate
        val cycleAttributes: MutableSet<String> = mutableSetOf() // attributes we need to oscillate
        val interpolation: MutableMap<String, Int> = mutableMapOf()
        var triggerList: MutableList<MotionKeyTrigger?>? = null
        setupRelative()
        if (I_DEBUG) {
            if (mKeyList == null) {
                Utils.log(TAG, ">>>>>>>>>>>>>>> mKeyList==null")
            } else {
                Utils.log(TAG, ">>>>>>>>>>>>>>> mKeyList for " + mView?.name.toString())
            }
        }
        if (mPathMotionArc != UNSET && mStartMotionPath.mPathMotionArc == UNSET) {
            mStartMotionPath.mPathMotionArc = mPathMotionArc
        }
        mStartPoint.different(mEndPoint, splineAttributes)
        if (I_DEBUG) {
            val attr: MutableSet<String> = mutableSetOf()
            mStartPoint.different(mEndPoint, attr)
            Utils.log(
                TAG, ">>>>>>>>>>>>>>> MotionConstrainedPoint found "
                        + Arrays.toString(attr)
            )
        }
        if (mKeyList != null) {
            for (key in mKeyList) {
                if (key is MotionKeyPosition) {
                    val keyPath: MotionKeyPosition = key
                    insertKey(
                        MotionPaths(
                            parentWidth, parentHeight,
                            keyPath, mStartMotionPath, mEndMotionPath
                        )
                    )
                    if (keyPath.mCurveFit != UNSET) {
                        mCurveFitType = keyPath.mCurveFit
                    }
                } else if (key is MotionKeyCycle) {
                    key.getAttributeNames(cycleAttributes)
                } else if (key is MotionKeyTimeCycle) {
                    key.getAttributeNames(timeCycleAttributes)
                } else if (key is MotionKeyTrigger) {
                    if (triggerList == null) {
                        triggerList = ArrayList()
                    }
                    triggerList.add(key as MotionKeyTrigger)
                } else {
                    key.setInterpolation(interpolation)
                    key.getAttributeNames(splineAttributes)
                }
            }
        }

        //--------------------------- trigger support --------------------
        if (triggerList != null) {
            mKeyTriggers = arrayOfNulls(triggerList.size)
        }

        //--------------------------- splines support --------------------
        if (splineAttributes.isNotEmpty()) {
            mAttributesMap = mutableMapOf()
            for (attribute in splineAttributes) {
                var splineSets: SplineSet?
                splineSets = if (attribute.startsWith("CUSTOM,")) {
                    val attrList: KeyFrameArray.CustomVar = KeyFrameArray.CustomVar()
                    val customAttributeName = attribute.split(",")[1]
                    for (key in mKeyList) {
                        if (key.mCustom == null) {
                            continue
                        }
                        val customAttribute: CustomVariable? = key.mCustom!!.get(customAttributeName)
                        if (customAttribute != null) {
                            attrList.append(key.mFramePosition, customAttribute)
                        }
                    }
                    SplineSet.makeCustomSplineSet(attribute, attrList)
                } else {
                    SplineSet.makeSpline(attribute, currentTime)
                }
                if (splineSets == null) {
                    continue
                }
                splineSets!!.setType(attribute)
                mAttributesMap!![attribute] = splineSets!!
            }
            if (mKeyList != null) {
                for (key in mKeyList) {
                    if (key is MotionKeyAttributes) {
                        key.addValues(mAttributesMap)
                    }
                }
            }
            mStartPoint.addValues(mAttributesMap, 0)
            mEndPoint.addValues(mAttributesMap, 100)
            for (spline in mAttributesMap?.keys ?: mutableSetOf()) {
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
        if (!timeCycleAttributes.isEmpty()) {
            if (mTimeCycleAttributesMap == null) {
                mTimeCycleAttributesMap = HashMap()
            }
            for (attribute in timeCycleAttributes) {
                if (mTimeCycleAttributesMap!!.containsKey(attribute)) {
                    continue
                }
                var splineSets: SplineSet? = null
                splineSets = if (attribute.startsWith("CUSTOM,")) {
                    val attrList: KeyFrameArray.CustomVar = KeyFrameArray.CustomVar()
                    val customAttributeName = attribute.split(",")[1]
                    for (key in mKeyList) {
                        if (key.mCustom == null) {
                            continue
                        }
                        val customAttribute: CustomVariable? = key.mCustom!!.get(customAttributeName)
                        if (customAttribute != null) {
                            attrList.append(key.mFramePosition, customAttribute)
                        }
                    }
                    SplineSet.makeCustomSplineSet(attribute, attrList)
                } else {
                    SplineSet.makeSpline(attribute, currentTime)
                }
                if (splineSets == null) {
                    continue
                }
                splineSets.setType(attribute)
                //                mTimeCycleAttributesMap.put(attribute, splineSets);
            }
            if (mKeyList != null) {
                for (key in mKeyList) {
                    if (key is MotionKeyTimeCycle) {
                        (key as MotionKeyTimeCycle).addTimeValues(mTimeCycleAttributesMap)
                    }
                }
            }
            for (spline in mTimeCycleAttributesMap?.keys ?: mutableSetOf()) {
                var curve: Int = CurveFit.SPLINE // default is SPLINE
                if (interpolation.containsKey(spline)) {
                    curve = interpolation[spline] ?: CurveFit.SPLINE
                }
                mTimeCycleAttributesMap?.get(spline)?.setup(curve)
            }
        }

        //--------------------------------- end new key frame 2
        val points: Array<MotionPaths?> = arrayOfNulls<MotionPaths>(2 + mMotionPaths.size)
        var count = 1
        points[0] = mStartMotionPath
        points[points.size - 1] = mEndMotionPath
        if (mMotionPaths.size > 0 && mCurveFitType == MotionKey.UNSET) {
            mCurveFitType = CurveFit.SPLINE
        }
        for (point in mMotionPaths) {
            points[count++] = point
        }

        // -----  setup custom attributes which must be in the start and end constraint sets
        val variables = 18
        val attributeNameSet: HashSet<String?> = HashSet()
        for (s in mEndMotionPath.mCustomAttributes.keys) {
            if (mStartMotionPath.mCustomAttributes.containsKey(s)) {
                if (!splineAttributes.contains("CUSTOM,$s")) {
                    attributeNameSet.add(s)
                }
            }
        }
        mAttributeNames = arrayOfNulls<String>(attributeNameSet.size)
        mAttributeInterpolatorCount = IntArray(mAttributeNames.size)
        for (i in mAttributeNames.indices) {
            val attributeName = mAttributeNames[i]
            mAttributeInterpolatorCount[i] = 0
            for (j in points.indices) {
                if (points[j]?.mCustomAttributes?.containsKey(attributeName) == true) {
                    val attribute: CustomVariable? = points[j]?.mCustomAttributes?.get(attributeName)
                    if (attribute != null) {
                        mAttributeInterpolatorCount[i] += attribute.numberOfInterpolatedValues()
                        break
                    }
                }
            }
        }
        val arcMode = points[0]?.mPathMotionArc != UNSET
        val mask = BooleanArray(variables + mAttributeNames.size) // defaults to false
        for (i in 1 until points.size) {
            points[i]?.different(points[i - 1], mask, mAttributeNames, arcMode)
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
            points[i]?.fillStandard(splineData[i], mInterpolateVariables)
            timePoint[i] = points[i]?.mTime?.toDouble() ?: 0.0
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
        mSpline = arrayOfNulls<CurveFit>(1 + mAttributeNames.size)
        for (i in mAttributeNames.indices) {
            var pointCount = 0
            var splinePoints: Array<DoubleArray>? = null
            var timePoints: DoubleArray? = null
            val name = mAttributeNames[i] ?: continue
            for (j in points.indices) {
                if(points[j] == null)
                    continue
                if (points[j]!!.hasCustomData(name)) {
                    if (splinePoints == null) {
                        timePoints = DoubleArray(points.size)
                        splinePoints = Array(points.size) {
                            DoubleArray(
                                points[j]!!.getCustomDataCount(name)
                            )
                        }
                    }
                    timePoints!![pointCount] = points[j]!!.mTime.toDouble()
                    points[j]!!.getCustomData(name, splinePoints[pointCount], 0)
                    pointCount++
                }
            }
            timePoints = Arrays.copyOf(timePoints!!, pointCount)
            splinePoints = Arrays.copyOfNonNull(splinePoints!!, pointCount)
            mSpline[i + 1] = CurveFit.get(mCurveFitType, timePoints, splinePoints!!)
        }

        // Spline for positions
        mSpline[0] = CurveFit.get(mCurveFitType, timePoint, splineData)
        // --------------------------- SUPPORT ARC MODE --------------
        if (points[0]?.mPathMotionArc != UNSET) {
            val size = points.size
            val mode = IntArray(size)
            val time = DoubleArray(size)
            val values = Array(size) { DoubleArray(2) }
            for (i in 0 until size) {
                if(points[i] == null)
                    continue
                mode[i] = points[i]!!.mPathMotionArc
                time[i] = points[i]!!.mTime.toDouble()
                values[i][0] = points[i]!!.mX.toDouble()
                values[i][1] = points[i]!!.mY.toDouble()
            }
            mArcSpline = CurveFit.getArc(mode, time, values)
        }

        //--------------------------- Cycle support --------------------
        var distance = Float.NaN
        mCycleMap = mutableMapOf()
        if (mKeyList != null) {
            for (attribute in cycleAttributes) {
                val cycle: KeyCycleOscillator =
                    KeyCycleOscillator.makeWidgetCycle(attribute) ?: continue
                if (cycle.variesByPath()) {
                    if (Float.isNaN(distance)) {
                        distance = preCycleDistance
                    }
                }
                cycle.setType(attribute)
                mCycleMap!![attribute] = cycle
            }
            for (key in mKeyList) {
                if (key is MotionKeyCycle) {
                    (key as MotionKeyCycle).addCycleValues(mCycleMap)
                }
            }
            for (cycle in mCycleMap!!.values) {
                cycle?.setup(distance)
            }
        }
        if (I_DEBUG) {
            Utils.log(
                TAG, "Animation of splineAttributes "
                        + Arrays.toString(splineAttributes)
            )
            Utils.log(TAG, "Animation of cycle " + Arrays.toString(mCycleMap?.keys ?: mutableSetOf<String>()))
            if (mAttributesMap != null) {
                Utils.log(TAG, " splines = " + Arrays.toString(mAttributesMap!!.keys))
                for (s in mAttributesMap!!.keys) {
                    Utils.log(TAG, s + " = " + mAttributesMap!![s])
                }
            }
            Utils.log(TAG, " ---------------------------------------- ")
        }

        //--------------------------- end cycle support ----------------
    }

    /**
     * Debug string
     */
    
    override fun toString(): String {
        return " start: x: " + mStartMotionPath.mX.toString() + " y: " + mStartMotionPath.mY
            .toString() + " end: x: " + mEndMotionPath.mX.toString() + " y: " + mEndMotionPath.mY
    }

    private fun readView(motionPaths: MotionPaths) {
        if(mView == null)
            return
        motionPaths.setBounds(
            mView!!.x.toFloat(), mView!!.y.toFloat(),
            mView!!.width.toFloat(), mView!!.height.toFloat()
        )
    }

    var view: MotionWidget?
        get() = mView
        set(view) {
            mView = view
        }

    // @TODO: add description
    fun setStart(mw: MotionWidget) {
        mStartMotionPath.mTime = 0f
        mStartMotionPath.mPosition = 0f
        mStartMotionPath.setBounds(mw.x.toFloat(), mw.y.toFloat(), mw.width.toFloat(), mw.height.toFloat())
        mStartMotionPath.applyParameters(mw)
        mStartPoint.setState(mw)
        val p: TypedBundle? = mw.widgetFrame?.motionProperties
        if (p != null) {
            p.applyDelta(this)
        }
    }

    // @TODO: add description
    fun setEnd(mw: MotionWidget) {
        mEndMotionPath.mTime = 1f
        mEndMotionPath.mPosition = 1f
        readView(mEndMotionPath)
        mEndMotionPath.setBounds(mw.left.toFloat(), mw.top.toFloat(), mw.width.toFloat(), mw.height.toFloat())
        mEndMotionPath.applyParameters(mw)
        mEndPoint.setState(mw)
    }

    // @TODO: add description
    fun setStartState(
        rect: ViewState,
        v: MotionWidget,
        rotation: Int,
        preWidth: Int,
        preHeight: Int
    ) {
        mStartMotionPath.mTime = 0f
        mStartMotionPath.mPosition = 0f
        val cx: Int
        val cy: Int
        val r = Rect()
        when (rotation) {
            2 -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                r.left = preHeight - (cy + rect.width()) / 2
                r.top = (cx - rect.height()) / 2
                r.right = r.left + rect.width()
                r.bottom = r.top + rect.height()
            }
            1 -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                r.left = (cy - rect.width()) / 2
                r.top = preWidth - (cx + rect.height()) / 2
                r.right = r.left + rect.width()
                r.bottom = r.top + rect.height()
            }
        }
        mStartMotionPath.setBounds(r.left.toFloat(), r.top.toFloat(), r.width().toFloat(), r.height().toFloat())
        mStartPoint.setState(r, v, rotation, rect.rotation)
    }

    fun rotate(rect: Rect, out: Rect, rotation: Int, preHeight: Int, preWidth: Int) {
        val cx: Int
        val cy: Int
        when (rotation) {
            MotionConstraintSet.ROTATE_PORTRATE_OF_LEFT -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                out.left = preHeight - (cy + rect.width()) / 2
                out.top = (cx - rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
            MotionConstraintSet.ROTATE_PORTRATE_OF_RIGHT -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                out.left = (cy - rect.width()) / 2
                out.top = preWidth - (cx + rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
            MotionConstraintSet.ROTATE_LEFT_OF_PORTRATE -> {
                cx = rect.left + rect.right
                cy = rect.bottom + rect.top
                out.left = preHeight - (cy + rect.width()) / 2
                out.top = (cx - rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
            MotionConstraintSet.ROTATE_RIGHT_OF_PORTRATE -> {
                cx = rect.left + rect.right
                cy = rect.top + rect.bottom
                out.left = rect.height() / 2 + rect.top - cx / 2
                out.top = preWidth - (cx + rect.height()) / 2
                out.right = out.left + rect.width()
                out.bottom = out.top + rect.height()
            }
        }
    }

    init {
        this.view = view
    }

    //    void setEndState(Rect cw, ConstraintSet constraintSet, int parentWidth, int parentHeight) {
    //        int rotate = constraintSet.mRotate; // for rotated frames
    //        if (rotate != 0) {
    //            rotate(cw, mTempRect, rotate, parentWidth, parentHeight);
    //            cw = mTempRect;
    //        }
    //        mEndMotionPath.time = 1;
    //        mEndMotionPath.position = 1;
    //        readView(mEndMotionPath);
    //        mEndMotionPath.setBounds(cw.left, cw.top, cw.width(), cw.height());
    //        mEndMotionPath.applyParameters(constraintSet.getParameters(mId));
    //        mEndPoint.setState(cw, constraintSet, rotate, mId);
    //    }
    fun setBothStates(v: MotionWidget) {
        mStartMotionPath.mTime = 0f
        mStartMotionPath.mPosition = 0f
        mNoMovement = true
        mStartMotionPath.setBounds(v.x.toFloat(), v.y.toFloat(), v.width.toFloat(), v.height.toFloat())
        mEndMotionPath.setBounds(v.x.toFloat(), v.y.toFloat(), v.width.toFloat(), v.height.toFloat())
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
//        if ("button".equals(Debug.getName(mView)))
//            if (mKeyTriggers != null) {
//                for (int i = 0; i < mKeyTriggers.length; i++) {
//                    mKeyTriggers[i].conditionallyFire(start ? -100 : 100, mView);
//                }
//            }
    }
    //##############################################################################################
    //$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%
    //$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%
    //$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%
    //$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%$%
    //##############################################################################################
    /**
     * The main driver of interpolation
     *
     * @return do you need to keep animating
     */
    fun interpolate(
        child: MotionWidget,
        globalPosition: Float,
        time: Long,
        keyCache: KeyCache?
    ): Boolean {
         val timeAnimation = false
        var position = getAdjustedPosition(globalPosition, null)
        // This quantize the position into steps e.g. 4 steps = 0-0.25,0.25-0.50 etc
        if (mQuantizeMotionSteps != UNSET) {
             val pin = position
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
        // MotionKeyTimeCycle.PathRotate timePathRotate = null;
        if (mAttributesMap != null) {
            for (aSpline in mAttributesMap!!.values) {
                aSpline.setProperty(child, position)
            }
        }

        //       TODO add KeyTimeCycle
        //        if (mTimeCycleAttributesMap != null) {
        //            for (ViewTimeCycle aSpline : mTimeCycleAttributesMap.values()) {
        //                if (aSpline instanceof ViewTimeCycle.PathRotate) {
        //                    timePathRotate = (ViewTimeCycle.PathRotate) aSpline;
        //                    continue;
        //                }
        //                timeAnimation |= aSpline.setProperty(child, position, time, keyCache);
        //            }
        //        }
        if (mSpline != null) {
            mSpline[0]?.getPos(position.toDouble(), mInterpolateData)
            mSpline[0]?.getSlope(position.toDouble(), mInterpolateVelocity)
            if (mArcSpline != null) {
                if (mInterpolateData.isNotEmpty()) {
                    mArcSpline!!.getPos(position.toDouble(), mInterpolateData)
                    mArcSpline!!.getSlope(position.toDouble(), mInterpolateVelocity)
                }
            }
            if (!mNoMovement) {
                mStartMotionPath.setView(
                    position, child,
                    mInterpolateVariables, mInterpolateData, mInterpolateVelocity, null
                )
            }
            if (mTransformPivotTarget != UNSET) {
                if (mTransformPivotView == null) {
                    val layout: MotionWidget? = child.parent
                    mTransformPivotView = layout?.findViewById(mTransformPivotTarget)
                }
                if (mTransformPivotView != null) {
                    val cy: Float =
                        (mTransformPivotView!!.top + mTransformPivotView!!.bottom) / 2.0f
                    val cx: Float =
                        (mTransformPivotView!!.left + mTransformPivotView!!.right) / 2.0f
                    if (child.right - child.left > 0
                        && child.bottom - child.top > 0
                    ) {
                        val px: Float = cx - child.left
                        val py: Float = cy - child.top
                        child.pivotX = px
                        child.pivotY = py
                    }
                }
            }

            //       TODO add support for path rotate
            //            if (mAttributesMap != null) {
            //                for (SplineSet aSpline : mAttributesMap.values()) {
            //                    if (aSpline instanceof ViewSpline.PathRotate
            //                          && mInterpolateVelocity.length > 1)
            //                        ((ViewSpline.PathRotate) aSpline).setPathRotate(child,
            //                        position, mInterpolateVelocity[0], mInterpolateVelocity[1]);
            //                }
            //
            //            }
            //            if (timePathRotate != null) {
            //                timeAnimation |= timePathRotate.setPathRotate(child, keyCache,
            //                  position, time, mInterpolateVelocity[0], mInterpolateVelocity[1]);
            //            }
            for (i in 1 until mSpline.size) {
                val spline: CurveFit? = mSpline[i]
                spline?.getPos(position.toDouble(), mValuesBuff)
                //interpolated here
                mStartMotionPath.mCustomAttributes
                    .get(mAttributeNames[i - 1])
                    ?.setInterpolatedValue(child, mValuesBuff)
            }
            if (mStartPoint.mVisibilityMode == MotionWidget.VISIBILITY_MODE_NORMAL) {
                if (position <= 0.0f) {
                    child.visibility = mStartPoint.mVisibility
                } else if (position >= 1.0f) {
                    child.visibility = mEndPoint.mVisibility
                } else if (mEndPoint.mVisibility != mStartPoint.mVisibility) {
                    child.visibility = MotionWidget.VISIBLE
                }
            }
            if (mKeyTriggers != null) {
                for (i in mKeyTriggers.indices) {
                    mKeyTriggers[i]?.conditionallyFire(position, child)
                }
            }
        } else {
            // do the interpolation
            val float_l: Float =
                mStartMotionPath.mX + (mEndMotionPath.mX - mStartMotionPath.mX) * position
            val float_t: Float =
                mStartMotionPath.mY + (mEndMotionPath.mY - mStartMotionPath.mY) * position
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
                        + (mEndMotionPath.mX - mStartMotionPath.mX) * position).toInt()
                t = (mStartMotionPath.mY
                        + (mEndMotionPath.mY - mStartMotionPath.mY) * position).toInt()
                width = (mStartMotionPath.mWidth
                        + (mEndMotionPath.mWidth - mStartMotionPath.mWidth) * position).toInt()
                height = (mStartMotionPath.mHeight
                        + (mEndMotionPath.mHeight - mStartMotionPath.mHeight) * position).toInt()
                r = l + width
                b = t + height
            }
            // widget is responsible to call measure
            child.layout(l, t, r, b)
        }

        // TODO add pathRotate KeyCycles
        if (mCycleMap != null) {
            for (osc in mCycleMap!!.values) {
                if (osc is KeyCycleOscillator.PathRotateSet) {
                    (osc as KeyCycleOscillator.PathRotateSet).setPathRotate(
                        child, position,
                        mInterpolateVelocity[0], mInterpolateVelocity[1]
                    )
                } else {
                    osc?.setProperty(child, position)
                }
            }
        }
        //   When we support TimeCycle return true if repaint is needed
        //        return timeAnimation;
        return false
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
            mSpline[0]?.getSlope(position.toDouble(), mInterpolateVelocity)
            mSpline[0]?.getPos(position.toDouble(), mInterpolateData)
            val v = mVelocity[0]
            for (i in mInterpolateVelocity.indices) {
                mInterpolateVelocity[i] *= v.toDouble()
            }
            if (mArcSpline != null) {
                if (mInterpolateData.isNotEmpty()) {
                    mArcSpline!!.getPos(position.toDouble(), mInterpolateData)
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
            Utils.log(
                TAG, " position= " + position
                        + " location= " + locationX + " , " + locationY
            )
        }
        position = getAdjustedPosition(position, mVelocity)
        val trans_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.TRANSLATION_X]
        val trans_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.TRANSLATION_Y]
        val rotation: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.ROTATION]
        val scale_x: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.SCALE_X]
        val scale_y: SplineSet? =
            if (mAttributesMap == null) null else mAttributesMap!![MotionKey.SCALE_Y]
        val osc_x: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.TRANSLATION_X]
        val osc_y: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.TRANSLATION_Y]
        val osc_r: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.ROTATION]
        val osc_sx: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.SCALE_X]
        val osc_sy: KeyCycleOscillator? =
            if (mCycleMap == null) null else mCycleMap!![MotionKey.SCALE_Y]
        val vmat = VelocityMatrix()
        vmat.clear()
        vmat.setRotationVelocity(rotation, position)
        vmat.setTranslationVelocity(trans_x, trans_y, position)
        vmat.setScaleVelocity(scale_x, scale_y, position)
        vmat.setRotationVelocity(osc_r, position)
        vmat.setTranslationVelocity(osc_x, osc_y, position)
        vmat.setScaleVelocity(osc_sx, osc_sy, position)
        if (mArcSpline != null) {
            if (mInterpolateData.size > 0) {
                mArcSpline!!.getPos(position.toDouble(), mInterpolateData)
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
            mSpline[0]?.getSlope(position.toDouble(), mInterpolateVelocity)
            mSpline[0]?.getPos(position.toDouble(), mInterpolateData)
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

    // @TODO: add description
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
        return mView?.name.toString()
    }

    fun positionKeyframe(
        view: MotionWidget,
        key: MotionKeyPosition,
        x: Float,
        y: Float,
        attribute: Array<String?>,
        value: FloatArray
    ) {
        val start = FloatRect()
        start.left = mStartMotionPath.mX
        start.top = mStartMotionPath.mY
        start.right = start.left + mStartMotionPath.mWidth
        start.bottom = start.top + mStartMotionPath.mHeight
        val end = FloatRect()
        end.left = mEndMotionPath.mX
        end.top = mEndMotionPath.mY
        end.right = end.left + mEndMotionPath.mWidth
        end.bottom = end.top + mEndMotionPath.mHeight
        key.positionAttributes(view, start, end, x, y, attribute, value)
    }

    /**
     * Get the keyFrames for the view controlled by this MotionController
     *
     * @param type is position(0-100) + 1000
     * * mType(1=Attributes, 2=Position, 3=TimeCycle 4=Cycle 5=Trigger
     * @param pos  the x&y position of the keyFrame along the path
     * @return Number of keyFrames found
     */
    fun getKeyFramePositions(type: IntArray, pos: FloatArray): Int {
        var i = 0
        var count = 0
        for (key in mKeyList) {
            type[i++] = key.mFramePosition + 1000 * key.mType
            val time: Float = key.mFramePosition / 100.0f
            mSpline!![0]?.getPos(time.toDouble(), mInterpolateData)
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
     * Gets the keyFrames for the view controlled by this MotionController.
     * The info data structure is of the form
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
            mSpline!![0]?.getPos(time.toDouble(), mInterpolateData)
            mStartMotionPath.getCenter(
                time.toDouble(),
                mInterpolateVariables,
                mInterpolateData,
                pos,
                0
            )
            info[++cursor] = pos[0].toBits()
            info[++cursor] = pos[1].toBits()
            if (key is MotionKeyPosition) {
                val kp: MotionKeyPosition = key as MotionKeyPosition
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

    
    override fun setValue(id: Int, value: Int): Boolean {
        when (id) {
            TypedValues.PositionType.TYPE_PATH_MOTION_ARC -> {
                setPathMotionArc(value)
                return true
            }
            TypedValues.MotionType.TYPE_QUANTIZE_MOTIONSTEPS -> {
                mQuantizeMotionSteps = value
                return true
            }
            TypedValues.TransitionType.TYPE_AUTO_TRANSITION ->                 // TODO add support for auto transitions mAutoTransition = value;
                return true
        }
        return false
    }

    
    override fun setValue(id: Int, value: Float): Boolean {
        if (TypedValues.MotionType.TYPE_QUANTIZE_MOTION_PHASE == id) {
            mQuantizeMotionPhase = value
            return true
        }
        if (TypedValues.MotionType.TYPE_STAGGER == id) {
            motionStagger = value
            return true
        }
        return false
    }


    override fun setValue(id: Int, value: String): Boolean {
        if (TypedValues.TransitionType.TYPE_INTERPOLATOR == id
            || TypedValues.MotionType.TYPE_QUANTIZE_INTERPOLATOR_TYPE == id
        ) {
            mQuantizeMotionInterpolator = getInterpolator(SPLINE_STRING, value, 0)
            return true
        }
        if (TypedValues.MotionType.TYPE_ANIMATE_RELATIVE_TO == id) {
            mStartMotionPath.mAnimateRelativeTo = value
            return true
        }
        return false
    }


    override fun setValue(id: Int, value: Boolean): Boolean {
        return false
    }

    
    override fun getId(name: String): Int {
        return 0
    }

    /**
     * Set stagger scale
     */
    fun setStaggerScale(staggerScale: Float) {
        mStaggerScale = staggerScale
    }

    /**
     * set the offset used in calculating stagger launches
     *
     * @param staggerOffset fraction of progress before this controller runs
     */
    fun setStaggerOffset(staggerOffset: Float) {
        mStaggerOffset = staggerOffset
    }

    fun setIdString(stringId: String?) {
        mId = stringId
        mStartMotionPath.mId = mId
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

        // Todo : Implement  QuantizeMotion scene rotate
        //    void setStartState(Rect cw, ConstraintSet constraintSet,
        //                      int parentWidth, int parentHeight) {
        //        int rotate = constraintSet.mRotate; // for rotated frames
        //        if (rotate != 0) {
        //            rotate(cw, mTempRect, rotate, parentWidth, parentHeight);
        //        }
        //        mStartMotionPath.time = 0;
        //        mStartMotionPath.position = 0;
        //        readView(mStartMotionPath);
        //        mStartMotionPath.setBounds(cw.left, cw.top, cw.width(), cw.height());
        //        ConstraintSet.Constraint constraint = constraintSet.getParameters(mId);
        //        mStartMotionPath.applyParameters(constraint);
        //        mMotionStagger = constraint.motion.mMotionStagger;
        //        mStartPoint.setState(cw, constraintSet, rotate, mId);
        //        mTransformPivotTarget = constraint.transform.transformPivotTarget;
        //        mQuantizeMotionSteps = constraint.motion.mQuantizeMotionSteps;
        //        mQuantizeMotionPhase = constraint.motion.mQuantizeMotionPhase;
        //        mQuantizeMotionInterpolator = getInterpolator(mView.getContext(),
        //                constraint.motion.mQuantizeInterpolatorType,
        //                constraint.motion.mQuantizeInterpolatorString,
        //                constraint.motion.mQuantizeInterpolatorID
        //        );
        //    }
        const val EASE_IN_OUT = 0
        const val EASE_IN = 1
        const val EASE_OUT = 2
        const val LINEAR = 3
        const val BOUNCE = 4
        const val OVERSHOOT = 5
        private const val SPLINE_STRING = -1

        
        private val INTERPOLATOR_REFERENCE_ID = -2

        
        private val INTERPOLATOR_UNDEFINED = -3
        private fun getInterpolator(
            type: Int,
            interpolatorString: String,
             id: Int
        ): DifferentialInterpolator? {
            when (type) {
                SPLINE_STRING -> {
                    val easing: Easing? = Easing.getInterpolator(interpolatorString)
                    return object : DifferentialInterpolator {
                        var mX = 0.0
                        
                        override fun getInterpolation(x: Float): Float {
                            mX = x.toDouble()
                            return easing?.get(mX)?.toFloat() ?: 0f
                        }

                        override val velocity: Float
                            get() = easing?.getDiff(mX)?.toFloat() ?: 0f
                    }
                }
            }
            return null
        }
    }
}