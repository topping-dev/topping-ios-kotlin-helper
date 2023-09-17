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

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView

/**
 * **Added in 2.0**
 *
 *
 *
 *
 */
abstract class VirtualLayout(myContext: TContext?, attrs: AttributeSet, self: TView) : ConstraintHelper(myContext, attrs, self) {
    private var mApplyVisibilityOnAttach = false
    private var mApplyElevationOnAttach = false

    init {
        mApplyElevationOnAttach = self.getObjCProperty("layout_android_visibility") as Boolean? ?: false
        mApplyElevationOnAttach = self.getObjCProperty("layout_android_elevation") as Boolean? ?: false
        self.swizzleFunction("setVisibility")  { sup, params ->
            val args = params as Array<Any>
            setVisibility(sup, args[0] as Int)
            0
        }
        self.swizzleFunction("setElevation") { sup, params ->
            val args = params as Array<Any>
            setElevation(sup, args[0] as Float)
            0
        }
    }

    fun onMeasure(
        layout: dev.topping.ios.constraint.core.widgets.VirtualLayout?,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        // nothing
    }

    /**
     *
     */
    override fun onAttachedToWindow(sup: TView?) {
        super.onAttachedToWindow(sup)
        if (mApplyVisibilityOnAttach || mApplyElevationOnAttach) {
            val parent: TView? = self.getParent()
            if (parent?.getParentType() is ConstraintLayout) {
                val visibility: Int = self.getVisibility()
                var elevation = self.getElevation()

                for (i in 0 until mCount) {
                    val id: String = mIds.get(i)
                    val view: TView? = parent.getViewById(id)
                    if (view != null) {
                        if (mApplyVisibilityOnAttach) {
                            view.setVisibility(visibility)
                        }
                        if (mApplyElevationOnAttach) {
                            if (elevation > 0
                            ) {
                                view.setTranslationZ(view.getTranslationZ() + elevation)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    fun setVisibility(sup: TView?, visibility: Int) {
        sup?.setVisibility(visibility)
        applyLayoutFeatures()
    }

    /**
     *
     */
    fun setElevation(sup:TView?, elevation: Float) {
        sup?.setElevation(elevation)
        applyLayoutFeatures()
    }

    /**
     *
     * @param container
     */
    override fun applyLayoutFeaturesInConstraintSet(container: ConstraintLayout?) {
        applyLayoutFeatures(container!!)
    }
}