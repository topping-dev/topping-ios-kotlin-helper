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
package dev.topping.ios.constraint.constraintlayout.motion.utils

import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionInterpolator
import dev.topping.ios.constraint.core.motion.utils.SpringStopEngine
import dev.topping.ios.constraint.core.motion.utils.StopEngine
import dev.topping.ios.constraint.core.motion.utils.StopLogicEngine

/**
 * This contains the class to provide the logic for an animation to come to a stop.
 * The setup defines a series of velocity gradients that gets to the desired position
 * ending at 0 velocity.
 * The path is computed such that the velocities are continuous
 *
 *
 */
class StopLogic : MotionInterpolator() {
    private val mStopLogicEngine: StopLogicEngine = StopLogicEngine()
    private lateinit var mSpringStopEngine: SpringStopEngine
    private var mEngine: StopEngine = mStopLogicEngine

    /**
     * Debugging logic to log the state.
     *
     * @param desc Description to pre append
     * @param time Time during animation
     * @return string useful for debugging the state of the StopLogic
     */
    fun debug(desc: String, time: Float): String {
        return mEngine.debug(desc, time)
    }

    /**
     * Get the velocity at a point in time
     * @param x
     * @return
     */
    fun getVelocity(x: Float): Float {
        return mEngine.getVelocity(x)
    }

    /**
     * Configure the stop logic base on the parameters
     * @param currentPos   start position
     * @param destination  the ending position
     * @param currentVelocity  the starting velocity
     * @param maxTime   The maximum time to take
     * @param maxAcceleration the maximum acceleration to use
     * @param maxVelocity the maximum velocity to use
     */
    fun config(
        currentPos: Float, destination: Float, currentVelocity: Float,
        maxTime: Float, maxAcceleration: Float, maxVelocity: Float
    ) {
        mEngine = mStopLogicEngine
        mStopLogicEngine.config(
            currentPos, destination, currentVelocity,
            maxTime, maxAcceleration, maxVelocity
        )
    }

    /**
     * This configure the stop logic to be a spring.
     * Moving from currentPosition(P0)
     * to destination with an initial velocity of currentVelocity (V0)
     * moving as if it has a mass (m) with spring constant stiffness(k), and friction(c)
     * It moves with the equation acceleration a = (-k.x-c.v)/m.
     * x = current position - destination
     * v is velocity
     *
     * @param currentPos The current position
     * @param destination The destination position
     * @param currentVelocity the initial velocity
     * @param mass the mass
     * @param stiffness the stiffness or spring constant (the force by which the spring pulls)
     * @param damping the stiffness or spring constant. (the resistance to the motion)
     * @param stopThreshold (When the max velocity of the movement is below this it stops)
     * @param boundaryMode This controls if it overshoots or bounces when it hits 0 and 1
     */
    fun springConfig(
        currentPos: Float, destination: Float, currentVelocity: Float,
        mass: Float, stiffness: Float, damping: Float, stopThreshold: Float,
        boundaryMode: Int
    ) {
        if (mSpringStopEngine == null) {
            mSpringStopEngine = SpringStopEngine()
        }
        mEngine = mSpringStopEngine
        mSpringStopEngine.springConfig(
            currentPos, destination, currentVelocity, mass, stiffness,
            damping, stopThreshold, boundaryMode
        )
    }

    override fun getInterpolation(v: Float): Float {
        return mEngine.getInterpolation(v)
    }

    override val velocity: Float
        get() = mEngine.velocity
    val isStopped: Boolean
        get() = mEngine.isStopped
}