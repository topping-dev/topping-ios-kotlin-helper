/*
 * Copyright (C) 2022 The Android Open Source Project
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
package dev.topping.ios.constraint.core.dsl

class HChain : Chain {
    inner class HAnchor internal constructor(side: Constraint.HSide) :
        Anchor(Constraint.Side.valueOf(side.name))

    /**
     * Get the left anchor
     *
     * @return the left anchor
     */
    val left: HAnchor = HAnchor(Constraint.HSide.LEFT)

    /**
     * Get the right anchor
     *
     * @return the right anchor
     */
    val right: HAnchor = HAnchor(Constraint.HSide.RIGHT)

    /**
     * Get the start anchor
     *
     * @return the start anchor
     */
    val start: HAnchor = HAnchor(Constraint.HSide.START)

    /**
     * Get the end anchor
     *
     * @return the end anchor
     */
    val end: HAnchor = HAnchor(Constraint.HSide.END)

    constructor(name: String) : super(name) {
        type = HelperType(typeMap[Type.HORIZONTAL_CHAIN]!!)
    }

    constructor(name: String, config: String) : super(name) {
        this.config = config
        type = HelperType(typeMap[Type.HORIZONTAL_CHAIN]!!)
        configMap = convertConfigToMap()
        if (configMap?.containsKey("contains") == true) {
            Ref.addStringToReferences(
                configMap!!["contains"],
                references
            )
        }
    }
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     */
    fun linkToLeft(
        anchor: Constraint.HAnchor,
        margin: Int = 0,
        goneMargin: Int = Int.MIN_VALUE
    ) {
        left.mConnection = anchor
        left.mMargin = margin
        left.mGoneMargin = goneMargin
        configMap?.put("left", left.toString())
    }
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     */
    fun linkToRight(
        anchor: Constraint.HAnchor,
        margin: Int = 0,
        goneMargin: Int = Int.MIN_VALUE
    ) {
        right.mConnection = anchor
        right.mMargin = margin
        right.mGoneMargin = goneMargin
        configMap?.put("right", right.toString())
    }
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     */
    fun linkToStart(
        anchor: Constraint.HAnchor,
        margin: Int = 0,
        goneMargin: Int = Int.MIN_VALUE
    ) {
        start.mConnection = anchor
        start.mMargin = margin
        start.mGoneMargin = goneMargin
        configMap?.put("start", start.toString())
    }
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     */
    fun linkToEnd(
        anchor: Constraint.HAnchor,
        margin: Int = 0,
        goneMargin: Int = Int.MIN_VALUE
    ) {
        end.mConnection = anchor
        end.mMargin = margin
        end.mGoneMargin = goneMargin
        configMap?.put("end", end.toString())
    }
}