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

class Barrier : Helper {
    private var mDirection: Constraint.Side? = null
    private var mMargin: Int = Int.MIN_VALUE
    private val references: ArrayList<Ref?> = ArrayList()

    constructor(name: String) : super(name, HelperType(typeMap.get(Type.BARRIER)!!)) {}
    constructor(name: String, config: String?) : super(
        name,
        HelperType(typeMap.get(Type.BARRIER)!!),
        config
    ) {
        configMap = convertConfigToMap()
        if (configMap?.containsKey("contains") == true) {
            Ref.addStringToReferences(
                configMap?.get("contains"),
                references
            )
        }
    }
    /**
     * Get the direction of the Barrier
     *
     * @return direction
     */
    /**
     * Set the direction of the Barrier
     *
     * @param direction
     */
    var direction: Constraint.Side?
        get() = mDirection
        set(direction) {
            mDirection = direction
            configMap?.put("direction", sideMap[direction]!!)
        }
    /**
     * Get the margin of the Barrier
     *
     * @return margin
     */
    /**
     * Set the margin of the Barrier
     *
     * @param margin
     */
    var margin: Int
        get() = mMargin
        set(margin) {
            mMargin = margin
            configMap?.put("margin", margin.toString())
        }

    /**
     * Convert references into a String representation
     *
     * @return a String representation of references
     */
    fun referencesToString(): String {
        if (references.isEmpty()) {
            return ""
        }
        val builder = StringBuilder("[")
        for (ref in references) {
            builder.append(ref.toString())
        }
        builder.append("]")
        return builder.toString()
    }

    /**
     * Add a new reference
     *
     * @param ref reference
     * @return Barrier
     */
    fun addReference(ref: dev.topping.ios.constraint.core.dsl.Ref?): Barrier {
        references.add(ref)
        configMap?.put("contains", referencesToString())
        return this
    }

    /**
     * Add a new reference
     *
     * @param ref reference in a String representation
     * @return Chain
     */
    fun addReference(ref: String): Barrier {
        return addReference(dev.topping.ios.constraint.core.dsl.Ref.Companion.parseStringToRef(ref))
    }
}