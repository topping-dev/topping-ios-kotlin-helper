/*
 * Copyright (C) 2019 The Android Open Source Project
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

import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet

/**
 * Builder class for creating [Transition] programmatically.
 */
object TransitionBuilder {
    private const val TAG = "TransitionBuilder"

    /**
     * It validates if the motion layout is setup correctly or not. Use this for debugging purposes.
     */
    fun validate(layout: MotionLayout) {
        if (layout.mScene == null) {
            throw RuntimeException("Invalid motion layout. Layout missing Motion Scene.")
        }
        val scene: MotionScene = layout.mScene!!
        if (!scene.validateLayout(layout)) {
            throw RuntimeException("MotionLayout doesn't have the right motion scene.")
        }
        if (scene.mCurrentTransition == null || scene.definedTransitions.isEmpty()) {
            throw RuntimeException(
                "Invalid motion layout. "
                        + "Motion Scene doesn't have any transition."
            )
        }
    }

    /**
     * Builder for a basic transition that transition from the startConstraintSet to
     * the endConstraintSet.
     * @param scene
     * @param transitionId a unique id to represent the created transition
     * @param startConstraintSetId
     * @param startConstraintSet
     * @param endConstraintSetId
     * @param endConstraintSet
     * @return
     */
    fun buildTransition(
        scene: MotionScene,
        transitionId: String,
        startConstraintSetId: String,
        startConstraintSet: ConstraintSet,
        endConstraintSetId: String,
        endConstraintSet: ConstraintSet
    ): MotionScene.Transition {
        val transition = MotionScene.Transition(
            transitionId,
            scene,
            startConstraintSetId,
            endConstraintSetId
        )
        updateConstraintSetInMotionScene(scene, transition, startConstraintSet, endConstraintSet)
        return transition
    }

    /**
     * Ensure that motion scene understands the constraint set and its respective ids.
     */
    private fun updateConstraintSetInMotionScene(
        scene: MotionScene,
        transition: MotionScene.Transition,
        startConstraintSet: ConstraintSet,
        endConstraintSet: ConstraintSet
    ) {
        val startId = transition.startConstraintSetId
        val endId = transition.endConstraintSetId
        scene.setConstraintSet(startId, startConstraintSet)
        scene.setConstraintSet(endId, endConstraintSet)
    }
}