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
package dev.topping.ios.constraint.core.state

import dev.topping.ios.constraint.core.motion.CustomVariable
import dev.topping.ios.constraint.core.motion.Motion
import dev.topping.ios.constraint.core.motion.MotionWidget
import dev.topping.ios.constraint.core.motion.key.MotionKeyAttributes
import dev.topping.ios.constraint.core.motion.key.MotionKeyCycle
import dev.topping.ios.constraint.core.motion.key.MotionKeyPosition
import dev.topping.ios.constraint.core.motion.utils.*
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.ConstraintWidgetContainer
import dev.topping.ios.constraint.core.widgets.height
import dev.topping.ios.constraint.core.widgets.width
import dev.topping.ios.constraint.isNaN
import dev.topping.ios.constraint.nanoTime

import kotlin.math.*

class Transition(dpToPixel: CorePixelDp) : TypedValues {
    private val mKeyPositions: MutableMap<Int, MutableMap<String, KeyPosition>> = mutableMapOf()
    private val mState: MutableMap<String, WidgetState> = mutableMapOf()
    private val mBundle: TypedBundle = TypedBundle()

    // Interpolation
    private val mDefaultInterpolator = 0
    private var mDefaultInterpolatorString: String? = null
    private var mEasing: Easing? = null

    /**
     * This gets the auto transition mode being used
     */
    val autoTransition = 0
    private val mDuration = 400
    private var mStagger = 0.0f
    private var mOnSwipe: OnSwipe? = null
    val mToPixel // Todo placed here as a temp till the refactor is done
            : CorePixelDp
    var mParentStartWidth = 0
    var mParentStartHeight = 0
    var mParentEndWidth = 0
    var mParentEndHeight = 0
    var interpolatedWidth = 0
    var interpolatedHeight = 0
    var mWrap = false

    init {
        mToPixel = dpToPixel
    }

    // @TODO: add description
    fun createOnSwipe(): OnSwipe {
        return OnSwipe().also { mOnSwipe = it }
    }

    // @TODO: add description
    fun hasOnSwipe(): Boolean {
        return mOnSwipe != null
    }

    class OnSwipe {
        var mAnchorId: String? = null
        private var mAnchorSide = 0
        private var mEngine: StopEngine? = null

        
        private var mRotationCenterId: String? = null

        
        private var mLimitBoundsTo: String? = null

        
        private var mDragVertical = true
        private var mDragDirection = 0
        var scale = 1f
            private set

        
        private var mDragThreshold = 10f
        private var mAutoCompleteMode = 0
        private var mMaxVelocity = 4f
        private var mMaxAcceleration = 1.2f

        // On touch up what happens
        private var mOnTouchUp = 0
        private var mSpringMass = 1f
        private var mSpringStiffness = 400f
        private var mSpringDamping = 10f
        private var mSpringStopThreshold = 0.01f
        private var mDestination = 0.0f

        // In spring mode what happens at the boundary
        private var mSpringBoundary = 0
        private var mStart: Long = 0
        val direction: FloatArray
            get() = TOUCH_DIRECTION[mDragDirection]
        val side: FloatArray
            get() = TOUCH_SIDES[mAnchorSide]

        fun setAnchorId(anchorId: String?) {
            mAnchorId = anchorId
        }

        fun setAnchorSide(anchorSide: Int) {
            mAnchorSide = anchorSide
        }

        fun setRotationCenterId(rotationCenterId: String?) {
            mRotationCenterId = rotationCenterId
        }

        fun setLimitBoundsTo(limitBoundsTo: String?) {
            mLimitBoundsTo = limitBoundsTo
        }

        fun setDragDirection(dragDirection: Int) {
            mDragDirection = dragDirection
            mDragVertical = mDragDirection < 2
        }

        fun setDragScale(dragScale: Float) {
            if (Float.isNaN(dragScale)) {
                return
            }
            scale = dragScale
        }

        fun setDragThreshold(dragThreshold: Float) {
            if (Float.isNaN(dragThreshold)) {
                return
            }
            mDragThreshold = dragThreshold
        }

        fun setAutoCompleteMode(mAutoCompleteMode: Int) {
            this.mAutoCompleteMode = mAutoCompleteMode
        }

        fun setMaxVelocity(maxVelocity: Float) {
            if (Float.isNaN(maxVelocity)) {
                return
            }
            mMaxVelocity = maxVelocity
        }

        fun setMaxAcceleration(maxAcceleration: Float) {
            if (Float.isNaN(maxAcceleration)) {
                return
            }
            mMaxAcceleration = maxAcceleration
        }

        fun setOnTouchUp(onTouchUp: Int) {
            mOnTouchUp = onTouchUp
        }

        fun setSpringMass(mSpringMass: Float) {
            if (Float.isNaN(mSpringMass)) {
                return
            }
            this.mSpringMass = mSpringMass
        }

        fun setSpringStiffness(mSpringStiffness: Float) {
            if (Float.isNaN(mSpringStiffness)) {
                return
            }
            this.mSpringStiffness = mSpringStiffness
        }

        fun setSpringDamping(mSpringDamping: Float) {
            if (Float.isNaN(mSpringDamping)) {
                return
            }
            this.mSpringDamping = mSpringDamping
        }

        fun setSpringStopThreshold(mSpringStopThreshold: Float) {
            if (Float.isNaN(mSpringStopThreshold)) {
                return
            }
            this.mSpringStopThreshold = mSpringStopThreshold
        }

        fun setSpringBoundary(mSpringBoundary: Int) {
            this.mSpringBoundary = mSpringBoundary
        }

        fun getDestinationPosition(
            currentPosition: Float,
            velocity: Float,
            duration: Float
        ): Float {
            val rest: Float =
                currentPosition + 0.5f * abs(velocity) * velocity / mMaxAcceleration
            when (mOnTouchUp) {
                ON_UP_AUTOCOMPLETE_TO_START, ON_UP_NEVER_COMPLETE_TO_END -> return 0f
                ON_UP_AUTOCOMPLETE_TO_END, ON_UP_NEVER_COMPLETE_TO_START -> return 1f
                ON_UP_STOP -> return Float.NaN
                ON_UP_DECELERATE -> return max(0f, min(1f, rest))
                ON_UP_DECELERATE_AND_COMPLETE -> return if (rest > 0.2f && rest < 0.8f) {
                    rest
                } else {
                    if (rest > .5f) 1f else 0f
                }
                ON_UP_AUTOCOMPLETE -> {}
            }
            if (I_DEBUG) {
                Utils.log(" currentPosition = $currentPosition")
                Utils.log("        velocity = $velocity")
                Utils.log("            peek = $rest")
                Utils.log("mMaxAcceleration = $mMaxAcceleration")
            }
            return if (rest > .5) 1f else 0f
        }

        fun config(position: Float, velocity: Float, start: Long, duration: Float) {
            mStart = start
            mDestination = getDestinationPosition(position, velocity, duration)
            if (mOnTouchUp == ON_UP_DECELERATE
                && mAutoCompleteMode == MODE_CONTINUOUS_VELOCITY
            ) {
                val sld: StopLogicEngine.Decelerate
                if (mEngine is StopLogicEngine.Decelerate) {
                    sld = mEngine as StopLogicEngine.Decelerate
                } else {
                    sld = StopLogicEngine.Decelerate()
                    mEngine = sld
                }
                sld.config(position, mDestination, velocity)
                return
            }
            if (mAutoCompleteMode == MODE_CONTINUOUS_VELOCITY) {
                val sl: StopLogicEngine
                if (mEngine is StopLogicEngine) {
                    sl = mEngine as StopLogicEngine
                } else {
                    sl = StopLogicEngine()
                    mEngine = sl
                }
                sl.config(
                    position, mDestination, velocity,
                    duration, mMaxAcceleration,
                    mMaxVelocity
                )
                return
            }
            val sl: SpringStopEngine
            if (mEngine is SpringStopEngine) {
                sl = mEngine as SpringStopEngine
            } else {
                sl = SpringStopEngine()
                mEngine = sl
            }
            sl.springConfig(
                position, mDestination, velocity,
                mSpringMass,
                mSpringStiffness,
                mSpringDamping,
                mSpringStopThreshold, mSpringBoundary
            )
        }

        /**
         * @param currentTime time in nanoseconds
         * @return new values of progress
         */
        fun getTouchUpProgress(currentTime: Long): Float {
            val time = (currentTime - mStart) * 1E-9f
            var pos: Float = mEngine?.getInterpolation(time) ?: 0f
            if (mEngine?.isStopped == true) {
                pos = mDestination
            }
            return pos
        }

        fun printInfo() {
            if (mAutoCompleteMode == MODE_CONTINUOUS_VELOCITY) {
                println("velocity = " + mEngine?.velocity)
                println("mMaxAcceleration = $mMaxAcceleration")
                println("mMaxVelocity = $mMaxVelocity")
            } else {
                println("mSpringMass          = $mSpringMass")
                println("mSpringStiffness     = $mSpringStiffness")
                println("mSpringDamping       = $mSpringDamping")
                println("mSpringStopThreshold = $mSpringStopThreshold")
                println("mSpringBoundary      = $mSpringBoundary")
            }
        }

        fun isNotDone(progress: Float): Boolean {
            return if (mOnTouchUp == ON_UP_STOP) {
                false
            } else mEngine?.isStopped == false
        }

        companion object {
            const val ANCHOR_SIDE_TOP = 0
            const val ANCHOR_SIDE_LEFT = 1
            const val ANCHOR_SIDE_RIGHT = 2
            const val ANCHOR_SIDE_BOTTOM = 3
            const val ANCHOR_SIDE_MIDDLE = 4
            const val ANCHOR_SIDE_START = 5
            const val ANCHOR_SIDE_END = 6
            val SIDES = arrayOf(
                "top", "left", "right",
                "bottom", "middle", "start", "end"
            )
            private val TOUCH_SIDES = arrayOf(
                floatArrayOf(0.5f, 0.0f),
                floatArrayOf(0.0f, 0.5f),
                floatArrayOf(1.0f, 0.5f),
                floatArrayOf(0.5f, 1.0f),
                floatArrayOf(0.5f, 0.5f),
                floatArrayOf(0.0f, 0.5f),
                floatArrayOf(1.0f, 0.5f)
            )
            const val DRAG_UP = 0
            const val DRAG_DOWN = 1
            const val DRAG_LEFT = 2
            const val DRAG_RIGHT = 3
            const val DRAG_START = 4
            const val DRAG_END = 5
            const val DRAG_CLOCKWISE = 6
            const val DRAG_ANTICLOCKWISE = 7
            val DIRECTIONS = arrayOf(
                "up", "down", "left", "right", "start",
                "end", "clockwise", "anticlockwise"
            )
            const val MODE_CONTINUOUS_VELOCITY = 0
            const val MODE_SPRING = 1
            val MODE = arrayOf("velocity", "spring")
            const val ON_UP_AUTOCOMPLETE = 0
            const val ON_UP_AUTOCOMPLETE_TO_START = 1
            const val ON_UP_AUTOCOMPLETE_TO_END = 2
            const val ON_UP_STOP = 3
            const val ON_UP_DECELERATE = 4
            const val ON_UP_DECELERATE_AND_COMPLETE = 5
            const val ON_UP_NEVER_COMPLETE_TO_START = 6
            const val ON_UP_NEVER_COMPLETE_TO_END = 7
            val TOUCH_UP = arrayOf(
                "autocomplete", "toStart",
                "toEnd", "stop", "decelerate", "decelerateComplete",
                "neverCompleteStart", "neverCompleteEnd"
            )
            const val BOUNDARY_OVERSHOOT = 0
            const val BOUNDARY_BOUNCE_START = 1
            const val BOUNDARY_BOUNCE_END = 2
            const val BOUNDARY_BOUNCE_BOTH = 3
            val BOUNDARY = arrayOf(
                "overshoot", "bounceStart",
                "bounceEnd", "bounceBoth"
            )
            private val TOUCH_DIRECTION = arrayOf(
                floatArrayOf(0.0f, -1.0f),
                floatArrayOf(0.0f, 1.0f),
                floatArrayOf(-1.0f, 0.0f),
                floatArrayOf(1.0f, 0.0f),
                floatArrayOf(-1.0f, 0.0f),
                floatArrayOf(1.0f, 0.0f)
            )
        }
    }

    /**
     * Converts from xy drag to progress
     * This should be used till touch up
     *
     * @param baseW parent width
     * @param baseH parent height
     * @param dx    change in x
     * @param dy    change in y
     * @return the change in progress
     */
    fun dragToProgress(
        currentProgress: Float,
        baseW: Int,
        baseH: Int,
        dx: Float,
        dy: Float
    ): Float {
        val widgets: Collection<WidgetState> = mState.values
        var childWidget: WidgetState? = null
        for (widget in widgets) {
            childWidget = widget
            break
        }
        if (mOnSwipe == null || childWidget == null) {
            return if (childWidget != null) {
                -dy / childWidget.mParentHeight
            } else 1.0f
        }
        if (mOnSwipe!!.mAnchorId == null) {
            val dir = mOnSwipe!!.direction
            val motionDpDtX = childWidget.mParentHeight.toFloat()
            val motionDpDtY = childWidget.mParentHeight.toFloat()
            val drag: Float =
                if (dir[0] != 0f) dx * abs(dir[0]) / motionDpDtX else dy * abs(
                    dir[1]
                ) / motionDpDtY
            return drag * mOnSwipe!!.scale
        }
        val base = mState[mOnSwipe!!.mAnchorId]
        val dir = mOnSwipe!!.direction
        val side = mOnSwipe!!.side
        val motionDpDt = FloatArray(2)
        base!!.interpolate(baseW, baseH, currentProgress, this)
        base.mMotionControl.getDpDt(currentProgress, side[0], side[1], motionDpDt)
        val drag: Float =
            if (dir[0] != 0f) dx * abs(dir[0]) / motionDpDt[0] else dy * abs(
                dir[1]
            ) / motionDpDt[1]
        if (I_DEBUG) {
            Utils.log(" drag $drag")
        }
        return drag * mOnSwipe!!.scale
    }

    /**
     * Set the start of the touch up
     *
     * @param currentProgress 0...1 progress in
     * @param currentTime     time in nanoseconds
     * @param velocityX       pixels per millisecond
     * @param velocityY       pixels per millisecond
     */
    fun setTouchUp(
        currentProgress: Float,
        currentTime: Long,
        velocityX: Float,
        velocityY: Float
    ) {
        if (mOnSwipe != null) {
            if (I_DEBUG) {
                Utils.log(" >>> velocity x,y = $velocityX , $velocityY")
            }
            val base = mState[mOnSwipe!!.mAnchorId]
            val motionDpDt = FloatArray(2)
            val dir = mOnSwipe!!.direction
            val side = mOnSwipe!!.side
            base!!.mMotionControl.getDpDt(currentProgress, side[0], side[1], motionDpDt)
            val movementInDir = dir[0] * motionDpDt[0] + dir[1] * motionDpDt[1]
            if (abs(movementInDir) < 0.01) {
                if (I_DEBUG) {
                    Utils.log(" >>> cap minimum v!! ")
                }
                motionDpDt[0] = .01f
                motionDpDt[1] = .01f
            }
            var drag = if (dir[0] != 0f) velocityX / motionDpDt[0] else velocityY / motionDpDt[1]
            drag *= mOnSwipe!!.scale
            if (I_DEBUG) {
                Utils.log(" >>> velocity        $drag")
                Utils.log(" >>> mDuration       $mDuration")
                Utils.log(" >>> currentProgress $currentProgress")
            }
            mOnSwipe!!.config(currentProgress, drag, currentTime, mDuration * 1E-3f)
            if (I_DEBUG) {
                mOnSwipe!!.printInfo()
            }
        }
    }

    /**
     * get the current touch up progress current time in nanoseconds
     * (ideally coming from an animation clock)
     *
     * @param currentTime in nanoseconds
     * @return progress
     */
    fun getTouchUpProgress(currentTime: Long): Float {
        return if (mOnSwipe != null) {
            mOnSwipe!!.getTouchUpProgress(currentTime)
        } else 0f
    }

    /**
     * Are we still animating
     *
     * @param currentProgress motion progress
     * @return true to continue moving
     */
    fun isTouchNotDone(currentProgress: Float): Boolean {
        return mOnSwipe!!.isNotDone(currentProgress)
    }

    // @TODO: add description
    fun findPreviousPosition(target: String, frameNumber: Int): KeyPosition? {
        var frameNumber = frameNumber
        while (frameNumber >= 0) {
            val map = mKeyPositions[frameNumber]
            if (map != null) {
                val keyPosition = map[target]
                if (keyPosition != null) {
                    return keyPosition
                }
            }
            frameNumber--
        }
        return null
    }

    // @TODO: add description
    fun findNextPosition(target: String, frameNumber: Int): KeyPosition? {
        var frameNumber = frameNumber
        while (frameNumber <= 100) {
            val map = mKeyPositions[frameNumber]
            if (map != null) {
                val keyPosition = map[target]
                if (keyPosition != null) {
                    return keyPosition
                }
            }
            frameNumber++
        }
        return null
    }

    // @TODO: add description
    fun getNumberKeyPositions(frame: WidgetFrame): Int {
        var numKeyPositions = 0
        var frameNumber = 0
        while (frameNumber <= 100) {
            val map = mKeyPositions[frameNumber]
            if (map != null && frame.widget != null) {
                val keyPosition = map[frame.widget!!.stringId]
                if (keyPosition != null) {
                    numKeyPositions++
                }
            }
            frameNumber++
        }
        return numKeyPositions
    }

    // @TODO: add description
    fun getMotion(id: String): Motion {
        return getWidgetState(id, null, 0).mMotionControl
    }

    // @TODO: add description
    fun fillKeyPositions(frame: WidgetFrame, x: FloatArray, y: FloatArray, pos: FloatArray) {
        var numKeyPositions = 0
        var frameNumber = 0
        while (frameNumber <= 100) {
            val map = mKeyPositions[frameNumber]
            if (map != null && frame.widget != null) {
                val keyPosition = map[frame.widget!!.stringId]
                if (keyPosition != null) {
                    x[numKeyPositions] = keyPosition.mX
                    y[numKeyPositions] = keyPosition.mY
                    pos[numKeyPositions] = keyPosition.mFrame.toFloat()
                    numKeyPositions++
                }
            }
            frameNumber++
        }
    }

    // @TODO: add description
    fun hasPositionKeyframes(): Boolean {
        return mKeyPositions.isNotEmpty()
    }

    // @TODO: add description
    fun setTransitionProperties(bundle: TypedBundle) {
        bundle.applyDelta(mBundle)
        bundle.applyDelta(this)
    }

    
    override fun setValue(id: Int, value: Int): Boolean {
        return false
    }

    
    override fun setValue(id: Int, value: Float): Boolean {
        if (id == TypedValues.TransitionType.TYPE_STAGGERED) {
            mStagger = value
        }
        return false
    }

    
    override fun setValue(id: Int, value: String): Boolean {
        if (id == TypedValues.TransitionType.TYPE_INTERPOLATOR) {
            mEasing = Easing.getInterpolator(value.also { mDefaultInterpolatorString = it })
        }
        return false
    }

    
    override fun setValue(id: Int, value: Boolean): Boolean {
        return false
    }

    
    override fun getId(name: String): Int {
        return 0
    }

    val isEmpty: Boolean
        get() = mState.isEmpty()

    // @TODO: add description
    fun clear() {
        mState.clear()
    }

    // @TODO: add description
    operator fun contains(key: String?): Boolean {
        return mState.containsKey(key)
    }

    // @TODO: add description
    fun addKeyPosition(target: String, bundle: TypedBundle) {
        getWidgetState(target, null, 0).setKeyPosition(bundle)
    }

    // @TODO: add description
    fun addKeyAttribute(target: String, bundle: TypedBundle) {
        getWidgetState(target, null, 0).setKeyAttribute(bundle)
    }

    /**
     * Add a key attribute and the custom variables into the
     * @param target the id of the target
     * @param bundle the key attributes bundle containing position etc.
     * @param custom the customVariables to add at that position
     */
    fun addKeyAttribute(target: String, bundle: TypedBundle, custom: Array<CustomVariable?>?) {
        getWidgetState(target, null, 0).setKeyAttribute(bundle, custom)
    }

    // @TODO: add description
    fun addKeyCycle(target: String, bundle: TypedBundle?) {
        getWidgetState(target, null, 0).setKeyCycle(bundle)
    }

    // @TODO: add description
    fun addKeyPosition(target: String, frame: Int, type: Int, x: Float, y: Float) {
        val bundle = TypedBundle()
        bundle.add(TypedValues.PositionType.TYPE_POSITION_TYPE, 2)
        bundle.add(TypedValues.TYPE_FRAME_POSITION, frame)
        bundle.add(TypedValues.PositionType.TYPE_PERCENT_X, x)
        bundle.add(TypedValues.PositionType.TYPE_PERCENT_Y, y)
        getWidgetState(target, null, 0).setKeyPosition(bundle)
        val keyPosition = KeyPosition(target, frame, type, x, y)
        var map = mKeyPositions[frame]
        if (map == null) {
            map = HashMap()
            mKeyPositions[frame] = map
        }
        map[target] = keyPosition
    }

    // @TODO: add description
    fun addCustomFloat(state: Int, widgetId: String, property: String, value: Float) {
        val widgetState = getWidgetState(widgetId, null, state)
        val frame: WidgetFrame = widgetState.getFrame(state)
        frame.addCustomFloat(property, value)
    }

    // @TODO: add description
    fun addCustomColor(state: Int, widgetId: String, property: String, color: Int) {
        val widgetState = getWidgetState(widgetId, null, state)
        val frame: WidgetFrame = widgetState.getFrame(state)
        frame.addCustomColor(property, color)
    }

    private fun calculateParentDimensions(progress: Float) {
        interpolatedWidth = (0.5f +
                mParentStartWidth + (mParentEndWidth - mParentStartWidth) * progress).toInt()
        interpolatedHeight = (0.5f +
                mParentStartHeight + (mParentEndHeight - mParentStartHeight) * progress).toInt()
    }

    /**
     * Update container of parameters for the state
     *
     * @param container contains all the widget parameters
     * @param state     starting or ending
     */
    fun updateFrom(container: ConstraintWidgetContainer, state: Int) {
        mWrap = (container.mListDimensionBehaviors.get(0)
                == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        mWrap = mWrap or (container.mListDimensionBehaviors.get(1)
                == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        if (state == START) {
            mParentStartWidth = container.width
            interpolatedWidth = mParentStartWidth
            mParentStartHeight = container.height
            interpolatedHeight = mParentStartHeight
        } else {
            mParentEndWidth = container.width
            mParentEndHeight = container.height
        }
        val children: MutableList<ConstraintWidget> = container.mChildren
        val count: Int = children.size
        val states = arrayOfNulls<WidgetState>(count)
        for (i in 0 until count) {
            val child: ConstraintWidget = children[i]
            val widgetState = getWidgetState(child.stringId, null, state)
            states[i] = widgetState
            widgetState.update(child, state)
            val id = widgetState.pathRelativeId
            if (id != null) {
                widgetState.setPathRelative(getWidgetState(id, null, state))
            }
        }
        calcStagger()
    }

    // @TODO: add description
    fun interpolate(parentWidth: Int, parentHeight: Int, progress: Float) {
        var progress = progress
        if (mWrap) {
            calculateParentDimensions(progress)
        }
        if (mEasing != null) {
            progress = mEasing!!.get(progress.toDouble()).toFloat()
        }
        for (key in mState.keys) {
            val widget = mState[key]
            widget!!.interpolate(parentWidth, parentHeight, progress, this)
        }
    }

    // @TODO: add description
    fun getStart(id: String?): WidgetFrame? {
        val widgetState = mState[id] ?: return null
        return widgetState.mStart
    }

    // @TODO: add description
    fun getEnd(id: String?): WidgetFrame? {
        val widgetState = mState[id] ?: return null
        return widgetState.mEnd
    }

    // @TODO: add description
    fun getInterpolated(id: String?): WidgetFrame? {
        val widgetState = mState[id] ?: return null
        return widgetState.mInterpolated
    }

    // @TODO: add description
    fun getPath(id: String?): FloatArray {
        val widgetState = mState[id]
        val duration = 1000
        val frames = duration / 16
        val mPoints = FloatArray(frames * 2)
        widgetState!!.mMotionControl.buildPath(mPoints, frames)
        return mPoints
    }

    // @TODO: add description
    fun getKeyFrames(
        id: String?,
        rectangles: FloatArray?,
        pathMode: IntArray?,
        position: IntArray?
    ): Int {
        val widgetState = mState[id]
        return widgetState!!.mMotionControl.buildKeyFrames(rectangles, pathMode, position)
    }

    
    private fun getWidgetState(widgetId: String): WidgetState? {
        return mState[widgetId]
    }

    fun getWidgetState(
        widgetId: String,
        child: ConstraintWidget?,
        transitionState: Int
    ): WidgetState {
        var widgetState = mState[widgetId]
        if (widgetState == null) {
            widgetState = WidgetState()
            mBundle.applyDelta(widgetState.mMotionControl)
            widgetState.mMotionWidgetStart.updateMotion(widgetState.mMotionControl)
            mState[widgetId!!] = widgetState
            if (child != null) {
                widgetState.update(child, transitionState)
            }
        }
        return widgetState
    }

    /**
     * Used in debug draw
     */
    fun getStart(child: ConstraintWidget): WidgetFrame {
        return getWidgetState(child.stringId, null, START).mStart
    }

    /**
     * Used in debug draw
     */
    fun getEnd(child: ConstraintWidget): WidgetFrame {
        return getWidgetState(child.stringId, null, END).mEnd
    }

    /**
     * Used after the interpolation
     */
    fun getInterpolated(child: ConstraintWidget): WidgetFrame {
        return getWidgetState(child.stringId, null, INTERPOLATED).mInterpolated
    }

    /**
     * This gets the interpolator being used
     */
    val interpolator: dev.topping.ios.constraint.core.state.Interpolator?
        get() = getInterpolator(mDefaultInterpolator, mDefaultInterpolatorString)

    class WidgetState {
        var mStart: WidgetFrame
        var mEnd: WidgetFrame
        var mInterpolated: WidgetFrame
        var mMotionControl: Motion
        var mNeedSetup = true
        var mMotionWidgetStart: MotionWidget
        var mMotionWidgetEnd: MotionWidget
        var mMotionWidgetInterpolated: MotionWidget
        var mKeyCache: KeyCache = KeyCache()
        var mParentHeight = -1
        var mParentWidth = -1

        init {
            mStart = WidgetFrame()
            mEnd = WidgetFrame()
            mInterpolated = WidgetFrame()
            mMotionWidgetStart = MotionWidget(mStart)
            mMotionWidgetEnd = MotionWidget(mEnd)
            mMotionWidgetInterpolated = MotionWidget(mInterpolated)
            mMotionControl = Motion(mMotionWidgetStart)
            mMotionControl.setStart(mMotionWidgetStart)
            mMotionControl.setEnd(mMotionWidgetEnd)
        }

        fun setKeyPosition(prop: TypedBundle) {
            val keyPosition = MotionKeyPosition()
            prop.applyDelta(keyPosition)
            mMotionControl.addKey(keyPosition)
        }

        fun setKeyAttribute(prop: TypedBundle) {
            val keyAttributes = MotionKeyAttributes()
            prop.applyDelta(keyAttributes)
            mMotionControl.addKey(keyAttributes)
        }

        /**
         * Set tge keyAttribute bundle and associated custom attributes
         * @param prop
         * @param custom
         */
        fun setKeyAttribute(prop: TypedBundle, custom: Array<CustomVariable?>?) {
            val keyAttributes = MotionKeyAttributes()
            prop.applyDelta(keyAttributes)
            if (custom != null) {
                for (i in custom.indices) {
                    keyAttributes.mCustom?.put(custom[i]?.name.toString(), custom[i]!!)
                }
            }
            mMotionControl.addKey(keyAttributes)
        }

        fun setKeyCycle(prop: TypedBundle?) {
            val keyAttributes = MotionKeyCycle()
            prop?.applyDelta(keyAttributes)
            mMotionControl.addKey(keyAttributes)
        }

        fun update(child: ConstraintWidget?, state: Int) {
            if (state == START) {
                mStart.update(child)
                mMotionWidgetStart.updateMotion(mMotionWidgetStart)
                mMotionControl.setStart(mMotionWidgetStart)
                mNeedSetup = true
            } else if (state == END) {
                mEnd.update(child)
                mMotionControl.setEnd(mMotionWidgetEnd)
                mNeedSetup = true
            }
            mParentWidth = -1
        }

        /**
         * Return the id of the widget to animate relative to
         *
         * @return id of widget or null
         */
        val pathRelativeId: String?
            get() = mMotionControl.animateRelativeTo

        fun getFrame(type: Int): WidgetFrame {
            if (type == START) {
                return mStart
            } else if (type == END) {
                return mEnd
            }
            return mInterpolated
        }

        fun interpolate(
            parentWidth: Int,
            parentHeight: Int,
            progress: Float,
            transition: Transition
        ) {
            // TODO  only update if parentHeight != mParentHeight || parentWidth != mParentWidth) {
            mParentHeight = parentHeight
            mParentWidth = parentWidth
            if (mNeedSetup) {
                mMotionControl.setup(parentWidth, parentHeight, 1f, nanoTime())
                mNeedSetup = false
            }
            WidgetFrame.interpolate(
                parentWidth, parentHeight,
                mInterpolated, mStart, mEnd, transition, progress
            )
            mInterpolated.interpolatedPos = progress
            mMotionControl.interpolate(
                mMotionWidgetInterpolated,
                progress, nanoTime(), mKeyCache
            )
        }

        fun setPathRelative(widgetState: WidgetState) {
            mMotionControl.setupRelative(widgetState.mMotionControl)
        }
    }

    class KeyPosition(
        var mTarget: String,
        var mFrame: Int,
        var mType: Int,
        var mX: Float,
        var mY: Float
    )

    fun calcStagger() {
        if (mStagger == 0.0f) {
            return
        }
        val flip = mStagger < 0.0
        val stagger: Float = abs(mStagger)
        var min = Float.MAX_VALUE
        var max = -Float.MAX_VALUE
        var useMotionStagger = false
        for (widgetId in mState.keys) {
            val widgetState = mState[widgetId]
            val f: Motion = widgetState!!.mMotionControl
            if (!Float.isNaN(f.motionStagger)) {
                useMotionStagger = true
                break
            }
        }
        if (useMotionStagger) {
            for (widgetId in mState.keys) {
                val widgetState = mState[widgetId]
                val f: Motion = widgetState!!.mMotionControl
                val widgetStagger: Float = f.motionStagger
                if (!Float.isNaN(widgetStagger)) {
                    min = min(min, widgetStagger)
                    max = max(max, widgetStagger)
                }
            }
            for (widgetId in mState.keys) {
                val widgetState = mState[widgetId]
                val f: Motion = widgetState!!.mMotionControl
                val widgetStagger: Float = f.motionStagger
                if (!Float.isNaN(widgetStagger)) {
                    val scale = 1 / (1 - stagger)
                    var offset = stagger - stagger * (widgetStagger - min) / (max - min)
                    if (flip) {
                        offset = stagger - stagger * (max - widgetStagger) / (max - min)
                    }
                    f.setStaggerScale(scale)
                    f.setStaggerOffset(offset)
                }
            }
        } else {
            for (widgetId in mState.keys) {
                val widgetState = mState[widgetId]
                val f: Motion = widgetState!!.mMotionControl
                val x: Float = f.finalX
                val y: Float = f.finalY
                val widgetStagger = x + y
                min = min(min, widgetStagger)
                max = max(max, widgetStagger)
            }
            for (widgetId in mState.keys) {
                val widgetState = mState[widgetId]
                val f: Motion = widgetState!!.mMotionControl
                val x: Float = f.finalX
                val y: Float = f.finalY
                val widgetStagger = x + y
                var offset = stagger - stagger * (widgetStagger - min) / (max - min)
                if (flip) {
                    offset = stagger - stagger * (max - widgetStagger) / (max - min)
                }
                val scale = 1 / (1 - stagger)
                f.setStaggerScale(scale)
                f.setStaggerOffset(offset)
            }
        }
    }

    companion object {
        private const val I_DEBUG = false
        const val START = 0
        const val END = 1
        const val INTERPOLATED = 2
        const val EASE_IN_OUT = 0
        const val EASE_IN = 1
        const val EASE_OUT = 2
        const val LINEAR = 3
        const val BOUNCE = 4
        const val OVERSHOOT = 5
        const val ANTICIPATE = 6
        private const val SPLINE_STRING = -1

        
        private val INTERPOLATOR_REFERENCE_ID = -2

        /**
         * get the interpolater based on a constant or a string
         */
        fun getInterpolator(interpolator: Int, interpolatorString: String?): Interpolator? {
            when (interpolator) {
                SPLINE_STRING -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator(
                            interpolatorString
                        )?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
                EASE_IN_OUT -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator("standard")?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
                EASE_IN -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator("accelerate")?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
                EASE_OUT -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator("decelerate")?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
                LINEAR -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator("linear")?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
                ANTICIPATE -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator("anticipate")?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
                OVERSHOOT -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator("overshoot")?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
                BOUNCE -> return object : Interpolator {
                    override fun getInterpolation(input: Float): Float {
                        return Easing.getInterpolator(
                            "spline(0.0, 0.2, 0.4, 0.6, "
                                    + "0.8 ,1.0, 0.8, 1.0, 0.9, 1.0)"
                        )?.get(input.toDouble())?.toFloat() ?: 0f
                    }
                }
            }
            return null
        }
    }
}