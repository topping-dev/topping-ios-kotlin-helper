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

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.core.motion.utils.RectF
import nl.adaptivity.xmlutil.XmlBufferedReader
import kotlin.math.*

/**
 * This class is used to manage Touch behaviour
 *
 *
 */
class TouchResponse {
    private var mTouchAnchorSide = 0
    private var mTouchSide = 0
    private var mOnTouchUp = 0
    /**
     * Get the view being used as anchor
     *
     * @return
     */
    /**
     * set the id of the anchor
     *
     * @param id
     */
    var anchorId: String = MotionScene.UNSET_ID
    var touchRegionId: String = MotionScene.UNSET_ID
        private set
    var limitBoundsToId: String = MotionScene.UNSET_ID
        private set
    private var mTouchAnchorY = 0.5f
    private var mTouchAnchorX = 0.5f
    var mRotateCenterX = 0.5f
    var mRotateCenterY = 0.5f
    private var mRotationCenterId: String = MotionScene.UNSET_ID
    var mIsRotateMode = false
    private var mTouchDirectionX = 0f
    private var mTouchDirectionY = 1f
    var isDragStarted = false
        private set
    private val mAnchorDpDt = FloatArray(2)
    private val mTempLoc = IntArray(2)
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private val mMotionLayout: MotionLayout
    /**
     * Gets the maximum velocity allowed on touch up.
     * Velocity is the rate of change in "progress" per second.
     *
     * @return
     */
    /**
     * Sets the maximum velocity allowed on touch up.
     * Velocity is the rate of change in "progress" per second.
     *
     * @param velocity in progress per second 1 = one second to do the entire animation
     */
    var maxVelocity = 4f

    /**
     * set the maximum Acceleration allowed for a motion.
     * Acceleration is the rate of change velocity per second.
     *
     * @param acceleration
     */
    var maxAcceleration = 1.2f
    var moveWhenScrollAtTop = true
        private set
    private var mDragScale = 1f

    /**
     * flags to control
     *
     * @return
     */
    var flags = 0
        private set
    private var mDragThreshold = 10f

    /**
     * the damping of the spring if using spring
     * c in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     * @return NaN if not set
     */
    var springDamping = 10f
        private set

    /**
     * the Mass of the spring if using spring
     * m in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     * @return default is 1
     */
    var springMass = 1f
        private set

    /**
     * the stiffness of the spring if using spring
     * K in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     * @return NaN if not set
     */
    var springStiffness = Float.NaN
        private set

    /**
     * The threshold below
     * @return NaN if not set
     */
    var springStopThreshold = Float.NaN
        private set

    /**
     * The spring's behaviour when it hits 0 or 1. It can be made ot overshoot or bounce
     * overshoot = 0
     * bounceStart = 1
     * bounceEnd = 2
     * bounceBoth = 3
     * @return Bounce mode
     */
    var springBoundary = 0
        private set
    /**
     * Get how the drag progress will return to the start or end state on touch up.
     * Can be ether COMPLETE_MODE_CONTINUOUS_VELOCITY (default) or COMPLETE_MODE_SPRING
     * @return
     */
    /**
     * set how the drag progress will return to the start or end state on touch up.
     *
     *
     * @return
     */
    var autoCompleteMode = COMPLETE_MODE_CONTINUOUS_VELOCITY

    constructor(context: TContext, layout: MotionLayout, parser: XmlBufferedReader) {
        mMotionLayout = layout
        fillFromAttributeList(context, Xml.asAttributeSet(parser))
    }

    constructor(layout: MotionLayout, onSwipe: OnSwipe) {
        mMotionLayout = layout
        anchorId = onSwipe.touchAnchorId
        mTouchAnchorSide = onSwipe.touchAnchorSide
        if (mTouchAnchorSide != -1) {
            mTouchAnchorX = TOUCH_SIDES[mTouchAnchorSide][0]
            mTouchAnchorY = TOUCH_SIDES[mTouchAnchorSide][1]
        }
        mTouchSide = onSwipe.dragDirection
        if (mTouchSide < TOUCH_DIRECTION.size) {
            mTouchDirectionX = TOUCH_DIRECTION[mTouchSide][0]
            mTouchDirectionY = TOUCH_DIRECTION[mTouchSide][1]
        } else {
            mTouchDirectionY = Float.NaN
            mTouchDirectionX = mTouchDirectionY
            mIsRotateMode = true
        }
        maxVelocity = onSwipe.maxVelocity
        maxAcceleration = onSwipe.maxAcceleration
        moveWhenScrollAtTop = onSwipe.moveWhenScrollAtTop
        mDragScale = onSwipe.dragScale
        mDragThreshold = onSwipe.dragThreshold
        touchRegionId = onSwipe.touchRegionId
        mOnTouchUp = onSwipe.onTouchUp
        flags = onSwipe.nestedScrollFlags
        limitBoundsToId = onSwipe.limitBoundsTo
        mRotationCenterId = onSwipe.rotationCenterId
        springBoundary = onSwipe.springBoundary
        springDamping = onSwipe.springDamping
        springMass = onSwipe.springMass
        springStiffness = onSwipe.springStiffness
        springStopThreshold = onSwipe.springStopThreshold
        autoCompleteMode = onSwipe.autoCompleteMode
    }

    fun setRTL(rtl: Boolean) {
        if (rtl) {
            TOUCH_DIRECTION[TOUCH_START] = TOUCH_DIRECTION[TOUCH_RIGHT]
            TOUCH_DIRECTION[TOUCH_END] = TOUCH_DIRECTION[TOUCH_LEFT]
            TOUCH_SIDES[SIDE_START] = TOUCH_SIDES[SIDE_RIGHT]
            TOUCH_SIDES[SIDE_END] = TOUCH_SIDES[SIDE_LEFT]
        } else {
            TOUCH_DIRECTION[TOUCH_START] = TOUCH_DIRECTION[TOUCH_LEFT]
            TOUCH_DIRECTION[TOUCH_END] = TOUCH_DIRECTION[TOUCH_RIGHT]
            TOUCH_SIDES[SIDE_START] = TOUCH_SIDES[SIDE_LEFT]
            TOUCH_SIDES[SIDE_END] = TOUCH_SIDES[SIDE_RIGHT]
        }
        mTouchAnchorX = TOUCH_SIDES[mTouchAnchorSide][0]
        mTouchAnchorY = TOUCH_SIDES[mTouchAnchorSide][1]
        if (mTouchSide >= TOUCH_DIRECTION.size) {
            return
        }
        mTouchDirectionX = TOUCH_DIRECTION[mTouchSide][0]
        mTouchDirectionY = TOUCH_DIRECTION[mTouchSide][1]
    }

    private fun fillFromAttributeList(context: TContext, attrs: AttributeSet) {
        fill(context, attrs)
    }

    private fun fill(context: TContext, attrs: AttributeSet) {
        attrs.forEach { kvp ->
            if (kvp.key == "app_touchAnchorId") {
                anchorId = context.getResources().getResourceId(kvp.value, anchorId)
            } else if (kvp.key == "app_touchAnchorSide") {
                mTouchAnchorSide = context.getResources().getInt(kvp.value, mTouchAnchorSide)
                mTouchAnchorX = TOUCH_SIDES[mTouchAnchorSide][0]
                mTouchAnchorY = TOUCH_SIDES[mTouchAnchorSide][1]
            } else if (kvp.key == "app_dragDirection") {
                mTouchSide = context.getResources().getInt(kvp.value, mTouchSide)
                if (mTouchSide < TOUCH_DIRECTION.size) {
                    mTouchDirectionX = TOUCH_DIRECTION[mTouchSide][0]
                    mTouchDirectionY = TOUCH_DIRECTION[mTouchSide][1]
                } else {
                    mTouchDirectionY = Float.NaN
                    mTouchDirectionX = mTouchDirectionY
                    mIsRotateMode = true
                }
            } else if (kvp.key == "app_maxVelocity") {
                maxVelocity = context.getResources().getFloat(kvp.value, maxVelocity)
            } else if (kvp.key == "app_maxAcceleration") {
                maxAcceleration = context.getResources().getFloat(kvp.value, maxAcceleration)
            } else if (kvp.key == "app_moveWhenScrollAtTop") {
                moveWhenScrollAtTop = context.getResources().getBoolean(kvp.value, moveWhenScrollAtTop)
            } else if (kvp.key == "app_dragScale") {
                mDragScale = context.getResources().getFloat(kvp.value, mDragScale)
            } else if (kvp.key == "app_dragThreshold") {
                mDragThreshold = context.getResources().getFloat(kvp.value, mDragThreshold)
            } else if (kvp.key == "app_touchRegionId") {
                touchRegionId = context.getResources().getResourceId(kvp.value, touchRegionId)
            } else if (kvp.key == "app_onTouchUp") {
                mOnTouchUp = context.getResources().getInt(kvp.value, mOnTouchUp)
            } else if (kvp.key == "app_nestedScrollFlags") {
                flags = context.getResources().getInt(kvp.value, 0)
            } else if (kvp.key == "app_limitBoundsTo") {
                limitBoundsToId = context.getResources().getResourceId(kvp.value, "")
            } else if (kvp.key == "app_rotationCenterId") {
                mRotationCenterId = context.getResources().getResourceId(kvp.value, mRotationCenterId)
            } else if (kvp.key == "app_springDamping") {
                springDamping = context.getResources().getFloat(kvp.value, springDamping)
            } else if (kvp.key == "app_springMass") {
                springMass = context.getResources().getFloat(kvp.value, springMass)
            } else if (kvp.key == "app_springStiffness") {
                springStiffness = context.getResources().getFloat(kvp.value, springStiffness)
            } else if (kvp.key == "app_springStopThreshold") {
                springStopThreshold = context.getResources().getFloat(kvp.value, springStopThreshold)
            } else if (kvp.key == "app_springBoundary") {
                springBoundary = context.getResources().getInt(kvp.value, springBoundary)
            } else if (kvp.key == "app_autoCompleteMode") {
                autoCompleteMode = context.getResources().getInt(kvp.value, autoCompleteMode)
            }
        }
    }

    fun setUpTouchEvent(lastTouchX: Float, lastTouchY: Float) {
        mLastTouchX = lastTouchX
        mLastTouchY = lastTouchY
        isDragStarted = false
    }

    /**
     * @param event
     * @param velocityTracker
     * @param currentState
     * @param motionScene
     */
    fun processTouchRotateEvent(
        event: MotionEvent,
        velocityTracker: MotionLayout.MotionTracker?,
        currentState: String,
        motionScene: MotionScene?
    ) {
        velocityTracker!!.addMovement(event)
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchX = event.rawX
                mLastTouchY = event.rawY
                isDragStarted = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dy: Float = event.getRawY() - mLastTouchY
                val dx: Float = event.getRawX() - mLastTouchX
                var drag: Float
                var rcx: Float = mMotionLayout.self.getWidth() / 2.0f
                var rcy: Float = mMotionLayout.self.getHeight() / 2.0f
                if (mRotationCenterId != MotionScene.UNSET_ID) {
                    val v: TView = mMotionLayout.self.findViewById(mRotationCenterId) as TView
                    mMotionLayout.self.getLocationOnScreen(mTempLoc)
                    rcx = mTempLoc[0] + (v.getLeft() + v.getRight()) / 2.0f
                    rcy = mTempLoc[1] + (v.getTop() + v.getBottom()) / 2.0f
                } else if (anchorId != MotionScene.UNSET_ID) {
                    val mc: MotionController = mMotionLayout.getMotionController(anchorId) as MotionController
                    val v: TView? = mMotionLayout.self.findViewById(mc.animateRelativeTo)
                    if (v == null) {
                        Log.e(TAG, "could not find view to animate to")
                    } else {
                        mMotionLayout.self.getLocationOnScreen(mTempLoc)
                        rcx = mTempLoc[0] + (v.getLeft() + v.getRight()) / 2.0f
                        rcy = mTempLoc[1] + (v.getTop() + v.getBottom()) / 2.0f
                    }
                }
                val relativePosX: Float = event.getRawX() - rcx
                val relativePosY: Float = event.getRawY() - rcy
                val angle1: Double = atan2(event.getRawY() - rcy, event.getRawX() - rcx).toDouble()
                val angle2: Double = atan2(mLastTouchY - rcy, mLastTouchX - rcx).toDouble()
                drag = ((angle1 - angle2) * 180.0f / PI).toFloat()
                if (drag > 330) {
                    drag -= 360f
                } else if (drag < -330) {
                    drag += 360f
                }
                if (abs(drag) > 0.01 || isDragStarted) {
                    var pos: Float = mMotionLayout.progress
                    if (!isDragStarted) {
                        isDragStarted = true
                        mMotionLayout.progress = pos
                    }
                    if (anchorId != MotionScene.UNSET_ID) {
                        mMotionLayout.getAnchorDpDt(
                            anchorId, pos,
                            mTouchAnchorX, mTouchAnchorY, mAnchorDpDt
                        )
                        mAnchorDpDt[1] = toDegrees(mAnchorDpDt[1])
                    } else {
                        mAnchorDpDt[1] = 360f
                    }
                    val change = drag * mDragScale / mAnchorDpDt[1]
                    pos = max(min(pos + change, 1f), 0f)
                    val current: Float = mMotionLayout.progress
                    if (pos != current) {
                        if (current == 0.0f || current == 1.0f) {
                            mMotionLayout.endTrigger(current == 0.0f)
                        }
                        mMotionLayout.progress = pos
                        velocityTracker!!.computeCurrentVelocity(SEC_TO_MILLISECONDS)
                        val tvx: Float = velocityTracker!!.xVelocity
                        val tvy: Float = velocityTracker!!.yVelocity
                        val angularVelocity =  // v*sin(angle)/r
                            (hypot(tvy, tvx)
                                    * sin(atan2(tvy, tvx) - angle1)
                                    / hypot(relativePosX, relativePosY)).toFloat()
                        mMotionLayout.velocity = toDegrees(angularVelocity).toFloat()
                    } else {
                        mMotionLayout.velocity = 0f
                    }
                    mLastTouchX = event.getRawX()
                    mLastTouchY = event.getRawY()
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragStarted = false
                velocityTracker!!.computeCurrentVelocity(16)
                val tvx: Float = velocityTracker!!.xVelocity
                val tvy: Float = velocityTracker!!.yVelocity
                val currentPos: Float = mMotionLayout.progress
                var pos = currentPos
                var rcx = mMotionLayout.self.getWidth() / 2.0f
                var rcy = mMotionLayout.self.getHeight() / 2.0f
                if (mRotationCenterId != MotionScene.UNSET_ID) {
                    val v: TView = mMotionLayout.self.findViewById(mRotationCenterId) as TView
                    mMotionLayout.self.getLocationOnScreen(mTempLoc)
                    rcx = mTempLoc[0] + (v.getLeft() + v.getRight()) / 2.0f
                    rcy = mTempLoc[1] + (v.getTop() + v.getBottom()) / 2.0f
                } else if (anchorId != MotionScene.UNSET_ID) {
                    val mc: MotionController = mMotionLayout.getMotionController(anchorId) as MotionController
                    val v: TView = mMotionLayout.self.findViewById(mc.animateRelativeTo) as TView
                    mMotionLayout.self.getLocationOnScreen(mTempLoc)
                    rcx = mTempLoc[0] + (v.getLeft() + v.getRight()) / 2.0f
                    rcy = mTempLoc[1] + (v.getTop() + v.getBottom()) / 2.0f
                }
                var relativePosX = event.getRawX() - rcx
                var relativePosY = event.getRawY() - rcy
                var angle1 = toDegrees(atan2(relativePosY, relativePosX))
                if (anchorId != MotionScene.UNSET_ID) {
                    mMotionLayout.getAnchorDpDt(
                        anchorId, pos,
                        mTouchAnchorX, mTouchAnchorY, mAnchorDpDt
                    )
                    mAnchorDpDt[1] = toDegrees(mAnchorDpDt[1]).toFloat()
                } else {
                    mAnchorDpDt[1] = 360f
                }
                var angle2 = toDegrees(atan2(tvy + relativePosY, tvx + relativePosX))
                var drag = (angle2 - angle1).toFloat()
                val velocity_tweek = SEC_TO_MILLISECONDS / 16f
                var angularVelocity: Float = drag * velocity_tweek
                if (!Float.isNaN(angularVelocity)) {
                    pos += 3 * angularVelocity * mDragScale / mAnchorDpDt[1] // TODO calibrate vel
                }
                if (pos != 0.0f && pos != 1.0f && mOnTouchUp != MotionLayout.TOUCH_UP_STOP) {
                    angularVelocity = angularVelocity * mDragScale / mAnchorDpDt[1]
                    var target = if (pos < 0.5) 0.0f else 1.0f
                    if (mOnTouchUp == MotionLayout.TOUCH_UP_NEVER_TO_START) {
                        if (currentPos + angularVelocity < 0) {
                            angularVelocity = abs(angularVelocity)
                        }
                        target = 1f
                    }
                    if (mOnTouchUp == MotionLayout.TOUCH_UP_NEVER_TO_END) {
                        if (currentPos + angularVelocity > 1) {
                            angularVelocity = -abs(angularVelocity)
                        }
                        target = 0f
                    }
                    mMotionLayout.touchAnimateTo(
                        mOnTouchUp, target,
                        3 * angularVelocity
                    )
                    if (0.0f >= currentPos || 1.0f <= currentPos) {
                        mMotionLayout.setState(MotionLayout.TransitionState.FINISHED)
                    }
                } else if (0.0f >= pos || 1.0f <= pos) {
                    mMotionLayout.setState(MotionLayout.TransitionState.FINISHED)
                }
            }
        }
    }

    /**
     * Process touch events
     *
     * @param event        The event coming from the touch
     * @param currentState
     * @param motionScene  The relevant MotionScene
     */
    fun processTouchEvent(
        event: MotionEvent,
        velocityTracker: MotionLayout.MotionTracker?,
        currentState: String,
        motionScene: MotionScene?
    ) {
        if (I_DEBUG) {
            Log.v(TAG, Debug.location + " best processTouchEvent For ")
        }
        if (mIsRotateMode) {
            processTouchRotateEvent(event, velocityTracker, currentState, motionScene)
            return
        }
        velocityTracker!!.addMovement(event)
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchX = event.getRawX()
                mLastTouchY = event.getRawY()
                isDragStarted = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dy: Float = event.getRawY() - mLastTouchY
                val dx: Float = event.getRawX() - mLastTouchX
                val drag = dx * mTouchDirectionX + dy * mTouchDirectionY
                if (I_DEBUG) {
                    Log.v(TAG, "# dx = " + dx + " = " + event.getRawX() + " - " + mLastTouchX)
                    Log.v(TAG, "# drag = $drag")
                }
                if (abs(drag) > mDragThreshold || isDragStarted) {
                    if (I_DEBUG) {
                        Log.v(TAG, "# ACTION_MOVE  mDragStarted  ")
                    }
                    var pos: Float = mMotionLayout.progress
                    if (!isDragStarted) {
                        isDragStarted = true
                        mMotionLayout.progress = pos
                        if (I_DEBUG) {
                            Log.v(TAG, "# ACTION_MOVE  progress <- $pos")
                        }
                    }
                    if (anchorId != MotionScene.UNSET_ID) {
                        mMotionLayout.getAnchorDpDt(
                            anchorId, pos, mTouchAnchorX,
                            mTouchAnchorY, mAnchorDpDt
                        )
                        if (I_DEBUG) {
                            Log.v(
                                TAG, Debug.location + " mAnchorDpDt "
                                        + Arrays.toString(mAnchorDpDt)
                            )
                        }
                    } else {
                        if (I_DEBUG) {
                            Log.v(TAG, Debug.location + " NO ANCHOR ")
                        }
                        val minSize: Float = min(
                            mMotionLayout.self.getWidth(),
                            mMotionLayout.self.getHeight()
                        ).toFloat()
                        mAnchorDpDt[1] = minSize * mTouchDirectionY
                        mAnchorDpDt[0] = minSize * mTouchDirectionX
                    }
                    var movmentInDir = (mTouchDirectionX * mAnchorDpDt[0]
                            + mTouchDirectionY * mAnchorDpDt[1])
                    if (I_DEBUG) {
                        Log.v(TAG, "# ACTION_MOVE  movmentInDir <- $movmentInDir ")
                        Log.v(
                            TAG, "# ACTION_MOVE  mAnchorDpDt  = " + mAnchorDpDt[0]
                                    + " ,  " + mAnchorDpDt[1]
                        )
                        Log.v(
                            TAG, "# ACTION_MOVE  mTouchDir  = " + mTouchDirectionX
                                    + " , " + mTouchDirectionY
                        )
                    }
                    movmentInDir *= mDragScale
                    if (abs(movmentInDir) < 0.01) {
                        mAnchorDpDt[0] = .01f
                        mAnchorDpDt[1] = .01f
                    }
                    val change: Float
                    change = if (mTouchDirectionX != 0f) {
                        dx / mAnchorDpDt[0]
                    } else {
                        dy / mAnchorDpDt[1]
                    }
                    if (I_DEBUG) {
                        Log.v(TAG, "# ACTION_MOVE      CHANGE  = $change")
                    }
                    pos = max(min(pos + change, 1f), 0f)
                    if (mOnTouchUp == MotionLayout.TOUCH_UP_NEVER_TO_START) {
                        pos = max(pos, 0.01f)
                    }
                    if (mOnTouchUp == MotionLayout.TOUCH_UP_NEVER_TO_END) {
                        pos = min(pos, 0.99f)
                    }
                    val current: Float = mMotionLayout.progress
                    if (pos != current) {
                        if (current == 0.0f || current == 1.0f) {
                            mMotionLayout.endTrigger(current == 0.0f)
                        }
                        mMotionLayout.progress = pos
                        if (I_DEBUG) {
                            Log.v(TAG, "# ACTION_MOVE progress <- $pos")
                        }
                        velocityTracker!!.computeCurrentVelocity(SEC_TO_MILLISECONDS)
                        val tvx: Float = velocityTracker!!.xVelocity
                        val tvy: Float = velocityTracker!!.yVelocity
                        val velocity =
                            if (mTouchDirectionX != 0f) tvx / mAnchorDpDt[0] else tvy / mAnchorDpDt[1]
                        mMotionLayout.velocity = velocity
                    } else {
                        mMotionLayout.velocity = 0f
                    }
                    mLastTouchX = event.getRawX()
                    mLastTouchY = event.getRawY()
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragStarted = false
                velocityTracker!!.computeCurrentVelocity(SEC_TO_MILLISECONDS)
                val tvx: Float = velocityTracker!!.xVelocity
                val tvy: Float = velocityTracker!!.yVelocity
                val currentPos: Float = mMotionLayout.progress
                var pos = currentPos
                if (I_DEBUG) {
                    Log.v(TAG, "# ACTION_UP progress  = $pos")
                }
                if (anchorId != MotionScene.UNSET_ID) {
                    mMotionLayout.getAnchorDpDt(
                        anchorId, pos,
                        mTouchAnchorX, mTouchAnchorY, mAnchorDpDt
                    )
                } else {
                    val minSize: Float =
                        min(mMotionLayout.self.getWidth(), mMotionLayout.self.getHeight()).toFloat()
                    mAnchorDpDt[1] = minSize * mTouchDirectionY
                    mAnchorDpDt[0] = minSize * mTouchDirectionX
                }
                val movmentInDir = (mTouchDirectionX * mAnchorDpDt[0]
                        + mTouchDirectionY * mAnchorDpDt[1])
                var velocity: Float
                velocity = if (mTouchDirectionX != 0f) {
                    tvx / mAnchorDpDt[0]
                } else {
                    tvy / mAnchorDpDt[1]
                }
                if (I_DEBUG) {
                    Log.v(TAG, "# ACTION_UP               tvy = $tvy")
                    Log.v(TAG, "# ACTION_UP mTouchDirectionX  = $mTouchDirectionX")
                    Log.v(TAG, "# ACTION_UP         velocity  = $velocity")
                }
                if (!Float.isNaN(velocity)) {
                    pos += velocity / 3 // TODO calibration & animation speed based on velocity
                }
                if (pos != 0.0f && pos != 1.0f && mOnTouchUp != MotionLayout.TOUCH_UP_STOP) {
                    var target = if (pos < 0.5) 0.0f else 1.0f
                    if (mOnTouchUp == MotionLayout.TOUCH_UP_NEVER_TO_START) {
                        if (currentPos + velocity < 0) {
                            velocity = abs(velocity)
                        }
                        target = 1f
                    }
                    if (mOnTouchUp == MotionLayout.TOUCH_UP_NEVER_TO_END) {
                        if (currentPos + velocity > 1) {
                            velocity = -abs(velocity)
                        }
                        target = 0f
                    }
                    mMotionLayout.touchAnimateTo(mOnTouchUp, target, velocity)
                    if (0.0f >= currentPos || 1.0f <= currentPos) {
                        mMotionLayout.setState(MotionLayout.TransitionState.FINISHED)
                    }
                } else if (0.0f >= pos || 1.0f <= pos) {
                    mMotionLayout.setState(MotionLayout.TransitionState.FINISHED)
                }
            }
        }
    }

    fun setDown(lastTouchX: Float, lastTouchY: Float) {
        mLastTouchX = lastTouchX
        mLastTouchY = lastTouchY
    }

    /**
     * Calculate if a drag in this direction results in an increase or decrease in progress.
     *
     * @param dx drag direction in x
     * @param dy drag direction in y
     * @return the change in progress given that dx and dy
     */
    fun getProgressDirection(dx: Float, dy: Float): Float {
        val pos: Float = mMotionLayout.progress
        mMotionLayout.getAnchorDpDt(anchorId, pos, mTouchAnchorX, mTouchAnchorY, mAnchorDpDt)
        val velocity: Float
        if (mTouchDirectionX != 0f) {
            if (mAnchorDpDt[0] == 0f) {
                mAnchorDpDt[0] = EPSILON
            }
            velocity = dx * mTouchDirectionX / mAnchorDpDt[0]
        } else {
            if (mAnchorDpDt[1] == 0f) {
                mAnchorDpDt[1] = EPSILON
            }
            velocity = dy * mTouchDirectionY / mAnchorDpDt[1]
        }
        return velocity
    }

    fun scrollUp(dx: Float, dy: Float) {
        isDragStarted = false
        var pos: Float = mMotionLayout.progress
        mMotionLayout.getAnchorDpDt(anchorId, pos, mTouchAnchorX, mTouchAnchorY, mAnchorDpDt)
        val movmentInDir =
            mTouchDirectionX * mAnchorDpDt[0] + mTouchDirectionY * mAnchorDpDt[1]
        val velocity: Float
        velocity = if (mTouchDirectionX != 0f) {
            dx * mTouchDirectionX / mAnchorDpDt[0]
        } else {
            dy * mTouchDirectionY / mAnchorDpDt[1]
        }
        if (!Float.isNaN(velocity)) {
            pos += velocity / 3 // TODO calibration & animation speed based on velocity
        }
        if (pos != 0.0f && pos != 1.0f && mOnTouchUp != MotionLayout.TOUCH_UP_STOP) {
            mMotionLayout.touchAnimateTo(mOnTouchUp, if (pos < 0.5) 0.0f else 1.0f, velocity)
        }
    }

    fun scrollMove(dx: Float, dy: Float) {
        val drag = dx * mTouchDirectionX + dy * mTouchDirectionY
        if (true) { // Todo evaluate || abs(drag) > 10 || mDragStarted) {
            var pos: Float = mMotionLayout.progress
            if (!isDragStarted) {
                isDragStarted = true
                mMotionLayout.progress = pos
            }
            mMotionLayout.getAnchorDpDt(
                anchorId, pos,
                mTouchAnchorX, mTouchAnchorY, mAnchorDpDt
            )
            val movmentInDir = (mTouchDirectionX * mAnchorDpDt[0]
                    + mTouchDirectionY * mAnchorDpDt[1])
            if (abs(movmentInDir) < 0.01) {
                mAnchorDpDt[0] = .01f
                mAnchorDpDt[1] = .01f
            }
            val change: Float
            change = if (mTouchDirectionX != 0f) {
                dx * mTouchDirectionX / mAnchorDpDt[0]
            } else {
                dy * mTouchDirectionY / mAnchorDpDt[1]
            }
            pos = max(min(pos + change, 1f), 0f)
            if (pos != mMotionLayout.progress) {
                mMotionLayout.progress = pos
                if (I_DEBUG) {
                    Log.v(TAG, "# ACTION_UP        progress <- $pos")
                }
            }
        }
    }

    fun setupTouch() {
        var view: TView? = null
        if (anchorId != "") {
            view = mMotionLayout.self.findViewById(anchorId)
            if (view == null) {
                Log.e(
                    TAG, "cannot find TouchAnchorId @id/"
                            + Debug.getName(mMotionLayout.self.getContext(), anchorId)
                )
            }
        }
        if (view is TNestedScrollView) {
            val sv: TNestedScrollView = view as TNestedScrollView
            sv.setOnTouchListener(object : TOnTouchListener {
                override fun onTouch(view: TView, motionEvent: MotionEvent): Boolean {
                    return false
                }
            })
            sv.setOnScrollChangeListener(object : TOnScrollChangeListener {
                override fun onScrollChange(
                    v: TNestedScrollView,
                    scrollX: Int,
                    scrollY: Int,
                    oldScrollX: Int,
                    oldScrollY: Int
                ) {
                }
            })
        }
    }

    /**
     * Set the location in the view to be the touch anchor
     *
     * @param x location in x 0 = left, 1 = right
     * @param y location in y 0 = top, 1 = bottom
     */
    fun setTouchAnchorLocation(x: Float, y: Float) {
        mTouchAnchorX = x
        mTouchAnchorY = y
    }

    /**
     * This calculates the bounds of the mTouchRegionId view.
     * This reuses rect for efficiency as this class will be called many times.
     *
     * @param layout The layout containing the view (findViewId)
     * @param rect   the rectangle to fill provided so this function does not have to create memory
     * @return the rect or null
     */
    fun getTouchRegion(layout: TView, rect: RectF): RectF? {
        if (touchRegionId == MotionScene.UNSET_ID) {
            return null
        }
        val view: TView = layout.findViewById(touchRegionId) ?: return null
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())
        return rect
    }

    /**
     * This calculates the bounds of the mTouchRegionId view.
     * This reuses rect for efficiency as this class will be called many times.
     *
     * @param layout The layout containing the view (findViewId)
     * @param rect   the rectangle to fill provided for memory efficiency
     * @return the rect or null
     */
    fun getLimitBoundsTo(layout: TView, rect: RectF): RectF? {
        if (limitBoundsToId == MotionScene.UNSET_ID) {
            return null
        }
        val view: TView = layout.findViewById(limitBoundsToId) ?: return null
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())
        return rect
    }

    fun dot(dx: Float, dy: Float): Float {
        return dx * mTouchDirectionX + dy * mTouchDirectionY
    }

    override fun toString(): String {
        return if (Float.isNaN(mTouchDirectionX)) "rotation" else "$mTouchDirectionX , $mTouchDirectionY"
    }

    fun setTouchUpMode(touchUpMode: Int) {
        mOnTouchUp = touchUpMode
    }

    companion object {
        private const val TAG = "TouchResponse"
        private const val I_DEBUG = false
        private const val SEC_TO_MILLISECONDS = 1000
        private const val EPSILON = 0.0000001f
        private val TOUCH_SIDES = arrayOf(
            floatArrayOf(0.5f, 0.0f),
            floatArrayOf(0.0f, 0.5f),
            floatArrayOf(1.0f, 0.5f),
            floatArrayOf(0.5f, 1.0f),
            floatArrayOf(0.5f, 0.5f),
            floatArrayOf(0.0f, 0.5f),
            floatArrayOf(1.0f, 0.5f)
        )
        private val TOUCH_DIRECTION = arrayOf(
            floatArrayOf(0.0f, -1.0f),
            floatArrayOf(0.0f, 1.0f),
            floatArrayOf(-1.0f, 0.0f),
            floatArrayOf(1.0f, 0.0f),
            floatArrayOf(-1.0f, 0.0f),
            floatArrayOf(1.0f, 0.0f)
        )

        
        private val TOUCH_UP = 0

        
        private val TOUCH_DOWN = 1
        private const val TOUCH_LEFT = 2
        private const val TOUCH_RIGHT = 3
        private const val TOUCH_START = 4
        private const val TOUCH_END = 5

        
        private val SIDE_TOP = 0
        private const val SIDE_LEFT = 1
        private const val SIDE_RIGHT = 2

        
        private val SIDE_BOTTOM = 3

        
        private val SIDE_MIDDLE = 4
        private const val SIDE_START = 5
        private const val SIDE_END = 6
        const val FLAG_DISABLE_POST_SCROLL = 1
        const val FLAG_DISABLE_SCROLL = 2
        const val FLAG_SUPPORT_SCROLL_UP = 4
        const val COMPLETE_MODE_CONTINUOUS_VELOCITY = 0
        const val COMPLETE_MODE_SPRING = 1
    }
}