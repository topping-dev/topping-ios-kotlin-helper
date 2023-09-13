/*
 * Copyright (C) 2017 The Android Open Source Project
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

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.ViewGroup

/**
 *
 * This defines the internally defined Constraint set
 * It allows you to have a group of References which point to other views and provide them with
 * constraint attributes
 *
 */
class Constraints(val self: TView) {
    var mConstraintSet: ConstraintSet? = null

    init {
        self.setParentType(this)
        self.setVisibility(TView.GONE)
        self.swizzleFunction("onLayout") { sup, params ->
            var args = params as Array<Any>
            onLayout(sup, params[0] as Boolean, params[1] as Int, params[2] as Int, params[3] as Int, params[4] as Int)
            0
        }
    }

    class LayoutParams : ConstraintLayout.LayoutParams {
        var alpha = 1f
        var applyElevation = false
        var elevation = 0f
        var rotation = 0f
        var rotationX = 0f
        var rotationY = 0f
        var scaleX = 1f
        var scaleY = 1f
        var transformPivotX = 0f
        var transformPivotY = 0f
        var translationX = 0f
        var translationY = 0f
        var translationZ = 0f

        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(source: LayoutParams) : super(source) {}
        constructor(c: TContext, attrs: AttributeSet) : super(c, attrs) {
            val a = c.getResources()
            attrs.forEach { kvp ->
                val attr = kvp.value
                if (kvp.key == "android_alpha") {
                    alpha = a.getFloat(attr, alpha)
                } else if (kvp.key == "android_elevation") {
                    elevation = a.getFloat(attr, elevation)
                    applyElevation = true
                } else if (kvp.key == "android_rotationX") {
                    rotationX = a.getFloat(attr, rotationX)
                } else if (kvp.key == "android_rotationY") {
                    rotationY = a.getFloat(attr, rotationY)
                } else if (kvp.key == "android_rotation") {
                    rotation = a.getFloat(attr, rotation)
                } else if (kvp.key == "android_scaleX") {
                    scaleX = a.getFloat(attr, scaleX)
                } else if (kvp.key == "android_scaleY") {
                    scaleY = a.getFloat(attr, scaleY)
                } else if (kvp.key == "android_transformPivotX") {
                    transformPivotX = a.getFloat(attr, transformPivotX)
                } else if (kvp.key == "android_transformPivotY") {
                    transformPivotY = a.getFloat(attr, transformPivotY)
                } else if (kvp.key == "android_translationX") {
                    translationX = a.getFloat(attr, translationX)
                } else if (kvp.key == "android_translationY") {
                    translationY = a.getFloat(attr, translationY)
                } else if (kvp.key == "android_translationZ") {
                    translationZ = a.getFloat(attr, translationZ)
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(TView.WRAP_CONTENT, TView.WRAP_CONTENT)
    }

    /**
     * {@inheritDoc}
     */
    fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p as LayoutParams)
    }// TODO -- could be more efficient...

    /**
     * get the Constraints associated with this constraint
     *
     * @return
     */
    val constraintSet: ConstraintSet
        get() {
            if (mConstraintSet == null) {
                mConstraintSet = ConstraintSet()
            }
            // TODO -- could be more efficient...
            mConstraintSet!!.clone(this)
            return mConstraintSet!!
        }

    fun onLayout(sup: TView?, changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    companion object {
        const val TAG = "Constraints"
    }
}