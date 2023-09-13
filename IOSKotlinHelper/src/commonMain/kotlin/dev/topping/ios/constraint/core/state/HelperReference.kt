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
package dev.topping.ios.constraint.core.state

import dev.topping.ios.constraint.core.state.helpers.Facade
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.HelperWidget

open class HelperReference(state: State, type: State.Helper) : ConstraintReference(state), Facade {
    protected val mHelperState: State
    val mType: State.Helper
    var mReferences: ArrayList<Any> = ArrayList()
    private var mHelperWidget: HelperWidget? = null

    init {
        mHelperState = state
        mType = type
    }

    val type: dev.topping.ios.constraint.core.state.State.Helper
        get() = mType

    // @TODO: add description
    fun add(vararg objects: Any?): HelperReference {
        mReferences.addAll(listOf(objects))
        return this
    }

    open var helperWidget: HelperWidget?
        get() = mHelperWidget
        set(helperWidget) {
            mHelperWidget = helperWidget
        }

    override var constraintWidget: ConstraintWidget?
        get() = helperWidget
        set(constraintWidget) {
            super.constraintWidget = constraintWidget
        }

    // @TODO: add description
    
    override fun apply() {
        // nothing
    }

    /**
     * Allows the derived classes to invoke the apply method in the ConstraintReference
     */
    fun applyBase() {
        super.apply()
    }
}