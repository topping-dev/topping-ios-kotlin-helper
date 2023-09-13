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
package dev.topping.ios.constraint.constraintlayout.widget

/**
 * **Added in 2.0**
 *
 *
 * Callbacks on state change
 *
 */
abstract class ConstraintsChangedListener {
    /**
     * called before layout happens
     * @param stateId -1 if state unknown, otherwise the state we will transition to
     * @param constraintId the constraintSet id that we will transition to
     */
    fun preLayoutChange(stateId: String, constraintId: String) {
        // nothing
    }

    /**
     * called after layout happens
     * @param stateId -1 if state unknown, otherwise the current state
     * @param constraintId the current constraintSet id we transitioned to
     */
    fun postLayoutChange(stateId: String, constraintId: String) {
        // nothing
    }
}