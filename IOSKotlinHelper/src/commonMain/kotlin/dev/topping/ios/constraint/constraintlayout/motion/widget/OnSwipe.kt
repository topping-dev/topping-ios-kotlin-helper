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
package dev.topping.ios.constraint.constraintlayout.motion.widget

/**
 * Container for holding swipe information
 */
class OnSwipe {
    var dragDirection = 0
        private set
    var touchAnchorSide = 0
        private set
    var touchAnchorId = MotionScene.UNSET_ID
        private set
    var touchRegionId= MotionScene.UNSET_ID
        private set
    var limitBoundsTo = MotionScene.UNSET_ID
        private set
    var onTouchUp = 0
        private set
    var rotationCenterId = MotionScene.UNSET_ID
        private set
    var maxVelocity = 4f
        private set
    var maxAcceleration = 1.2f
        private set
    var moveWhenScrollAtTop = true
        private set
    var dragScale = 1f
        private set
    var nestedScrollFlags = 0
        private set
    var dragThreshold = 10f
        private set
    var springDamping = Float.NaN
        private set

    /**
     * Get the mass of the spring.
     * the m in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @return
     */
    var springMass = 1f
        private set

    /**
     * get the stiffness of the spring
     *
     * @return NaN if not set
     */
    var springStiffness = Float.NaN
        private set

    /**
     * The threshold for spring motion to stop.
     *
     * @return
     */
    var springStopThreshold = Float.NaN
        private set

    /**
     * The behaviour at the boundaries 0 and 1
     *
     * @return
     */
    var springBoundary = 0
        private set

    /**
     * sets the behaviour at the boundaries 0 and 1
     * COMPLETE_MODE_CONTINUOUS_VELOCITY = 0;
     * COMPLETE_MODE_SPRING = 1;
     *
     */
    var autoCompleteMode = 0

    /**
     * The id of the view who's movement is matched to your drag
     * If not specified it will map to a linear movement across the width of the motionLayout
     *
     * @param side
     * @return
     */
    fun setTouchAnchorId(side: String): OnSwipe {
        touchAnchorId = side
        return this
    }

    /**
     * This side of the view that matches the drag movement.
     * Only meaning full if the object changes size during the movement.
     * (rotation is not considered)
     *
     * @param side
     * @return
     */
    fun setTouchAnchorSide(side: Int): OnSwipe {
        touchAnchorSide = side
        return this
    }

    /**
     * The direction of the drag.
     *
     * @param dragDirection
     * @return
     */
    fun setDragDirection(dragDirection: Int): OnSwipe {
        this.dragDirection = dragDirection
        return this
    }

    /**
     * The maximum velocity (Change in progress per second) animation can achieve
     *
     * @param maxVelocity
     * @return
     */
    fun setMaxVelocity(maxVelocity: Int): OnSwipe {
        this.maxVelocity = maxVelocity.toFloat()
        return this
    }

    /**
     * The maximum acceleration and deceleration of the animation
     * (Change in Change in progress per second)
     * Faster makes the object seem lighter and quicker
     *
     * @param maxAcceleration
     * @return
     */
    fun setMaxAcceleration(maxAcceleration: Int): OnSwipe {
        this.maxAcceleration = maxAcceleration.toFloat()
        return this
    }

    /**
     * When collaborating with a NestedScrollView do you progress form 0-1 only
     * when the scroll view is at the top.
     *
     * @param moveWhenScrollAtTop
     * @return
     */
    fun setMoveWhenScrollAtTop(moveWhenScrollAtTop: Boolean): OnSwipe {
        this.moveWhenScrollAtTop = moveWhenScrollAtTop
        return this
    }

    /**
     * Normally 1 this can be tweaked to make the acceleration faster
     *
     * @param dragScale
     * @return
     */
    fun setDragScale(dragScale: Int): OnSwipe {
        this.dragScale = dragScale.toFloat()
        return this
    }

    /**
     * This sets the threshold before the animation is kicked off.
     * It is important when have multi state animations the have some play before the
     * System decides which animation to jump on.
     *
     * @param dragThreshold
     * @return
     */
    fun setDragThreshold(dragThreshold: Int): OnSwipe {
        this.dragThreshold = dragThreshold.toFloat()
        return this
    }

    /**
     * @param side
     * @return
     */
    fun setTouchRegionId(side: String): OnSwipe {
        touchRegionId = side
        return this
    }

    /**
     * Configures what happens when the user releases on mouse up.
     * One of: ON_UP_AUTOCOMPLETE, ON_UP_AUTOCOMPLETE_TO_START, ON_UP_AUTOCOMPLETE_TO_END,
     * ON_UP_STOP, ON_UP_DECELERATE, ON_UP_DECELERATE_AND_COMPLETE
     *
     * @param mode default = ON_UP_AUTOCOMPLETE
     * @return
     */
    fun setOnTouchUp(mode: Int): OnSwipe {
        onTouchUp = mode
        return this
    }

    /**
     * Various flag to control behaviours of nested scroll
     * FLAG_DISABLE_POST_SCROLL = 1;
     * FLAG_DISABLE_SCROLL = 2;
     *
     * @param flags
     * @return
     */
    fun setNestedScrollFlags(flags: Int): OnSwipe {
        nestedScrollFlags = flags
        return this
    }

    /**
     * Only allow touch actions to be initiated within this region
     *
     * @param id
     * @return
     */
    fun setLimitBoundsTo(id: String): OnSwipe {
        limitBoundsTo = id
        return this
    }

    /**
     * The view to center the rotation about
     *
     * @param rotationCenterId
     * @return this
     */
    fun setRotateCenter(rotationCenterId: String): OnSwipe {
        this.rotationCenterId = rotationCenterId
        return this
    }

    /**
     * Set the damping of the spring if using spring.
     * c in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @param springDamping
     * @return this
     */
    fun setSpringDamping(springDamping: Float): OnSwipe {
        this.springDamping = springDamping
        return this
    }

    /**
     * Set the Mass of the spring if using spring.
     * m in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @param springMass
     * @return this
     */
    fun setSpringMass(springMass: Float): OnSwipe {
        this.springMass = springMass
        return this
    }

    /**
     * set the stiffness of the spring if using spring.
     * If this is set the swipe will use a spring return system.
     * If set to NaN it will revert to the norm system.
     * K in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @param springStiffness
     * @return
     */
    fun setSpringStiffness(springStiffness: Float): OnSwipe {
        this.springStiffness = springStiffness
        return this
    }

    /**
     * set the threshold for spring motion to stop.
     * This is in change in progress / second
     * If the spring will never go above that threshold again it will stop.
     *
     * @param springStopThreshold
     * @return
     */
    fun setSpringStopThreshold(springStopThreshold: Float): OnSwipe {
        this.springStopThreshold = springStopThreshold
        return this
    }

    /**
     * The behaviour at the boundaries 0 and 1.
     * SPRING_BOUNDARY_OVERSHOOT = 0;
     * SPRING_BOUNDARY_BOUNCE_START = 1;
     * SPRING_BOUNDARY_BOUNCE_END = 2;
     * SPRING_BOUNDARY_BOUNCE_BOTH = 3;
     *
     * @param springBoundary
     * @return
     */
    fun setSpringBoundary(springBoundary: Int): OnSwipe {
        this.springBoundary = springBoundary
        return this
    }

    companion object {
        const val COMPLETE_MODE_CONTINUOUS_VELOCITY = 0
        const val COMPLETE_MODE_SPRING = 1
        const val SPRING_BOUNDARY_OVERSHOOT = 0
        const val SPRING_BOUNDARY_BOUNCESTART = 1
        const val SPRING_BOUNDARY_BOUNCEEND = 2
        const val SPRING_BOUNDARY_BOUNCEBOTH = 3
        const val DRAG_UP = 0
        const val DRAG_DOWN = 1
        const val DRAG_LEFT = 2
        const val DRAG_RIGHT = 3
        const val DRAG_START = 4
        const val DRAG_END = 5
        const val DRAG_CLOCKWISE = 6
        const val DRAG_ANTICLOCKWISE = 7
        const val FLAG_DISABLE_POST_SCROLL = 1
        const val FLAG_DISABLE_SCROLL = 2
        const val SIDE_TOP = 0
        const val SIDE_LEFT = 1
        const val SIDE_RIGHT = 2
        const val SIDE_BOTTOM = 3
        const val SIDE_MIDDLE = 4
        const val SIDE_START = 5
        const val SIDE_END = 6
        const val ON_UP_AUTOCOMPLETE = 0
        const val ON_UP_AUTOCOMPLETE_TO_START = 1
        const val ON_UP_AUTOCOMPLETE_TO_END = 2
        const val ON_UP_STOP = 3
        const val ON_UP_DECELERATE = 4
        const val ON_UP_DECELERATE_AND_COMPLETE = 5
        const val ON_UP_NEVER_TO_START = 6
        const val ON_UP_NEVER_TO_END = 7
    }
}