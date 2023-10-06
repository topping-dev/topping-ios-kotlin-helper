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

class VChain : Chain {
    inner class VAnchor internal constructor(side: Constraint.VSide) :
        Anchor(Constraint.Side.valueOf(side.name))

    /**
     * Get the top anchor
     *
     * @return the top anchor
     */
    val top: VAnchor = VAnchor(Constraint.VSide.TOP)

    /**
     * Get the bottom anchor
     *
     * @return the bottom anchor
     */
    val bottom: VAnchor = VAnchor(Constraint.VSide.BOTTOM)

    /**
     * Get the baseline anchor
     *
     * @return the baseline anchor
     */
    val baseline: VAnchor = VAnchor(Constraint.VSide.BASELINE)

    constructor(name: String) : super(name) {
        type = HelperType(typeMap.get(Type.VERTICAL_CHAIN)!!)
    }

    constructor(name: String, config: String) : super(name) {
        this.config = config
        type = HelperType(typeMap.get(Type.VERTICAL_CHAIN)!!)
        configMap = convertConfigToMap()
        if (configMap?.containsKey("contains") == true) {
            dev.topping.ios.constraint.core.dsl.Ref.Companion.addStringToReferences(
                configMap!!["contains"],
                references
            )
        }
    }
    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     */
    fun linkToTop(
        anchor: Constraint.VAnchor,
        margin: Int = 0,
        goneMargin: Int = Int.MIN_VALUE
    ) {
        top.mConnection = anchor
        top.mMargin = margin
        top.mGoneMargin = goneMargin
        configMap?.put("top", top.toString())
    }
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     */
    fun linkToBottom(
        anchor: Constraint.VAnchor,
        margin: Int = 0,
        goneMargin: Int = Int.MIN_VALUE
    ) {
        bottom.mConnection = anchor
        bottom.mMargin = margin
        bottom.mGoneMargin = goneMargin
        configMap?.put("bottom", bottom.toString())
    }
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     */
    fun linkToBaseline(
        anchor: Constraint.VAnchor,
        margin: Int = 0,
        goneMargin: Int = Int.MIN_VALUE
    ) {
        baseline.mConnection = anchor
        baseline.mMargin = margin
        baseline.mGoneMargin = goneMargin
        configMap?.put("baseline", baseline.toString())
    }
}