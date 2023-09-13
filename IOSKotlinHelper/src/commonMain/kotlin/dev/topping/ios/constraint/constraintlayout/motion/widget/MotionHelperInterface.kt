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
package dev.topping.ios.constraint.constraintlayout.motion.widget

import dev.topping.ios.constraint.TCanvas
import dev.topping.ios.constraint.TView

/**
 * This defined the interface for MotionLayout helpers
 * Helpers can be used to draw motion effects or modify motions
 */
interface MotionHelperInterface : Animatable, MotionLayout.TransitionListener {
    /**
     * Notify when view is visible
     * @return
     */
    val isUsedOnShow: Boolean

    /**
     * Notify when views are hidden
     * @return
     */
    val isUseOnHide: Boolean

    /**
     * is involved in painting
     * @return
     */
    val isDecorator: Boolean

    /**
     * Call before views are painted
     * @param canvas
     */
    fun onPreDraw(canvas: TCanvas)

    /**
     * Called after views are painted
     * @param canvas
     */
    fun onPostDraw(canvas: TCanvas)

    /**
     * Called after motionController is populated with start and end and keyframes.
     *
     * @param motionLayout
     * @param controllerMap
     */
    fun onPreSetup(motionLayout: MotionLayout, controllerMap: MutableMap<TView, MotionController>)

    /**
     * This is called after motionLayout read motionScene and assembles all constraintSets
     * @param motionLayout
     */
    fun onFinishedMotionScene(motionLayout: MotionLayout)
}