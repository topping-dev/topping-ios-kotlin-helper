/*
 * Copyright (C) 2021 The Android Open Source Project
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

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.TCanvas
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintHelper
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout

/**
 *
 */
open class MotionHelper(context: TContext, attrs: AttributeSet, self: TView) : ConstraintHelper(context, attrs, self), MotionHelperInterface {
    /**
     *
     * @return
     */
    override var isUsedOnShow = false
        set

    /**
     *
     * @return
     */
    override var isUseOnHide = false
        set
    private var mProgress = 0f
    protected var views: Array<TView?>? = null

    init {
        val a = context.getResources()
        attrs.forEach { kvp ->
            val attr = kvp.value
            if (kvp.key == "onShow") {
                isUsedOnShow = a.getBoolean(attr, isUsedOnShow)
            } else if (kvp.key == "onHide") {
                isUseOnHide = a.getBoolean(attr, isUseOnHide)
            }
        }
        isUsedOnShow = self.getObjCProperty("onShow") as Boolean
        isUseOnHide = self.getObjCProperty("onHide") as Boolean
    }

    override var progress: Float
        get() = mProgress
        set(progress) {
            mProgress = progress
            if (this.mCount > 0) {
                views = this.getViews(self.getParent()?.getParentType() as ConstraintLayout?)
                views?.let {
                    for (i in 0 until this.mCount) {
                        val view: TView? = views!![i]
                        setProgress(view, progress)
                    }
                }
            } else {
                val group: TView? = self.getParent()
                val count: Int = group?.getChildCount() ?: 0
                for (i in 0 until count) {
                    val view: TView? = group?.getChildAt(i)
                    if (view?.getParentType() is MotionHelper) {
                        continue
                    }
                    setProgress(view, progress)
                }
            }
        }

    /**
     *
     * @param view
     * @param progress
     */
    fun setProgress(view: TView?, progress: Float) {}

    override fun onTransitionStarted(motionLayout: MotionLayout, startId: String, endId: String) {
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout,
        startId: String,
        endId: String,
        progress: Float
    ) {
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: String) {
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout,
        triggerId: String,
        positive: Boolean,
        progress: Float
    ) {
    }

    override val isDecorator: Boolean
        get() = false

    override fun onPreDraw(canvas: TCanvas) {
    }

    override fun onFinishedMotionScene(motionLayout: MotionLayout) {
    }

    override fun onPostDraw(canvas: TCanvas) {
    }

    override fun onPreSetup(
        motionLayout: MotionLayout,
        controllerMap: MutableMap<TView, MotionController>
    ) {
    }
}