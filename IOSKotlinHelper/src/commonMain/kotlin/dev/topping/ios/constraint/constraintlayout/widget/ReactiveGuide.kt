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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionLayout

/**
 * Utility class representing a reactive Guideline helper object for [ConstraintLayout].
 */
class ReactiveGuide(val context: TContext?, val attrs: AttributeSet, val self: TView) : SharedValues.SharedValuesListener {
    private var mAttributeId = ""
    var isAnimatingChange = false
        private set
    var applyToConstraintSetId = ""
    private var mApplyToAllConstraintSets = true

    init {
        self.setParentType(this)
        self.setVisibility(TView.GONE)
        mAttributeId = self.getObjCProperty("reactiveGuide_valueId") as String
        isAnimatingChange = self.getObjCProperty("reactiveGuide_animateChange") as Boolean
        applyToConstraintSetId = self.getObjCProperty("reactiveGuide_applyToConstraintSet") as String
        mApplyToAllConstraintSets = self.getObjCProperty("reactiveGuide_applyToAllConstraintSets") as Boolean
        if (mAttributeId != "") {
            val sharedValues: SharedValues = ConstraintLayout.sharedValues
            sharedValues.addListener(mAttributeId, this)
        }
        self.swizzleFunction("setVisibility")  { sup, params ->
            val args = params as Array<Any>
            setVisibility(sup, args[0] as Int)
            0
        }
        self.swizzleFunction("onMeasure")  { sup, params ->
            val args = params as Array<Any>
            onMeasure(sup, args[0] as Int, args[1] as Int)
            0
        }
    }

    /**
     *
     * @param id
     */
    var attributeId: String
        get() = mAttributeId
        set(id) {
            val sharedValues: SharedValues = ConstraintLayout.sharedValues
            if (mAttributeId != "") {
                sharedValues.removeListener(mAttributeId, this)
            }
            mAttributeId = id
            if (mAttributeId != "") {
                sharedValues.addListener(mAttributeId, this)
            }
        }

    // @TODO: add description
    fun setAnimateChange(animate: Boolean) {
        isAnimatingChange = animate
    }

    /**
     *
     */
    fun setVisibility(sup: TView, visibility: Int) {
    }

    /**
     *
     */
    protected fun onMeasure(sup: TView, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        sup.setMeasuredDimension(0, 0)
    }

    /**
     * Set the guideline's distance from the top or left edge.
     *
     * @param margin the distance to the top or left edge
     */
    fun setGuidelineBegin(margin: Int) {
        val params: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        params.guideBegin = margin
        self.setLayoutParams(params)
    }

    /**
     * Set a guideline's distance to end.
     *
     * @param margin the margin to the right or bottom side of container
     */
    fun setGuidelineEnd(margin: Int) {
        val params: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        params.guideEnd = margin
        self.setLayoutParams(params)
    }

    /**
     * Set a Guideline's percent.
     * @param ratio the ratio between the gap on the left and right 0.0 is top/left 0.5 is middle
     */
    fun setGuidelinePercent(ratio: Float) {
        val params: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        params.guidePercent = ratio
        self.setLayoutParams(params)
    }

    override fun onNewValue(key: String, newValue: Int, oldValue: Int) {
        setGuidelineBegin(newValue)
        val id: String = self.getId()
        if (id == "") {
            return
        }
        if (self.getParent()?.getParentType() is MotionLayout) {
            val motionLayout: MotionLayout = self.getParent()?.getParentType() as MotionLayout
            var currentState: String = motionLayout.currentState
            if (applyToConstraintSetId != "") {
                currentState = applyToConstraintSetId
            }
            if (isAnimatingChange) {
                if (mApplyToAllConstraintSets) {
                    val ids: Array<String>? = motionLayout.constraintSetIds
                    ids?.let {
                        for (i in it.indices) {
                            val cs = ids[i]
                            if (cs != currentState) {
                                changeValue(newValue, id, motionLayout, cs)
                            }
                        }
                    }
                }
                val constraintSet: ConstraintSet? = motionLayout.cloneConstraintSet(currentState)
                constraintSet?.setGuidelineEnd(id, newValue)
                motionLayout.updateStateAnimate(currentState, constraintSet, 1000)
            } else {
                if (mApplyToAllConstraintSets) {
                    val ids: Array<String>? = motionLayout.constraintSetIds
                    ids?.let {
                        for (i in ids.indices) {
                            val cs = ids[i]
                            changeValue(newValue, id, motionLayout, cs)
                        }
                    }
                } else {
                    changeValue(newValue, id, motionLayout, currentState)
                }
            }
        }
    }

    private fun changeValue(newValue: Int, id: String, motionLayout: MotionLayout, currentState: String) {
        val constraintSet: ConstraintSet? = motionLayout.getConstraintSet(currentState)
        constraintSet?.setGuidelineEnd(id, newValue)
        motionLayout.updateState(currentState, constraintSet)
    }
}