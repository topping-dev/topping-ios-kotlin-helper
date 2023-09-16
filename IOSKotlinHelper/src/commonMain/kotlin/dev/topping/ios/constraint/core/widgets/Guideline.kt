/*
 * Copyright (C) 2015 The Android Open Source Project
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
package dev.topping.ios.constraint.core.widgets

import dev.topping.ios.constraint.core.LinearSystem
import dev.topping.ios.constraint.core.SolverVariable
import dev.topping.ios.constraint.core.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT

/**
 * Guideline
 */
class Guideline : ConstraintWidget() {
    var relativePercent = -1f
        protected set
    var relativeBegin = -1
        protected set
    var relativeEnd = -1
        protected set
    protected var mGuidelineUseRtl = true
    private var mAnchor: ConstraintAnchor = mTop
    private var mOrientation = HORIZONTAL
    /**
     * Get the Minimum Position
     * @return the Minimum Position
     */
    /**
     * set the minimum position
     * @param minimum
     */
    var minimumPosition = 0

    var mResolved = false
    override var isResolvedVertically: Boolean
        get() = mResolved
        set(value) { mResolved = value }

    override fun isResolvedHorizontally(): Boolean {
        return mResolved
    }

    override fun isResolvedVertically(): Boolean {
        return mResolved

    }

    init {
        mAnchors.clear()
        mAnchors.add(mAnchor)
        val count: Int = mListAnchors.size
        for (i in 0 until count) {
            mListAnchors[i] = mAnchor
        }
    }

    
    override fun copy(src: ConstraintWidget, map: MutableMap<ConstraintWidget, ConstraintWidget>) {
        super.copy(src, map)
        val srcGuideline = src as Guideline
        relativePercent = srcGuideline.relativePercent
        relativeBegin = srcGuideline.relativeBegin
        relativeEnd = srcGuideline.relativeEnd
        mGuidelineUseRtl = srcGuideline.mGuidelineUseRtl
        orientation = srcGuideline.mOrientation
    }

    
    override fun allowedInBarrier(): Boolean {
        return true
    }

    // @TODO: add description
    val relativeBehaviour: Int
        get() {
            if (relativePercent != -1f) {
                return RELATIVE_PERCENT
            }
            if (relativeBegin != -1) {
                return RELATIVE_BEGIN
            }
            return if (relativeEnd != -1) {
                RELATIVE_END
            } else RELATIVE_UNKNOWN
        }
    val anchor: dev.topping.ios.constraint.core.widgets.ConstraintAnchor
        get() = mAnchor

    /**
     * Specify the xml type for the container
     */
    override fun getType(): String {
        return type
    }

    override fun setType(type: String) {
        super.setType(type)
    }
    var type: String
        get() = "Guideline"
        set(type) {
            setType(type)
        }

    /**
     * get the orientation VERTICAL or HORIZONTAL
     * @return orientation
     */// @TODO: add description
    var orientation: Int
        get() = mOrientation
        set(orientation) {
            if (mOrientation == orientation) {
                return
            }
            mOrientation = orientation
            mAnchors.clear()
            if (mOrientation == VERTICAL) {
                mAnchor = mLeft
            } else {
                mAnchor = mTop
            }
            mAnchors.add(mAnchor)
            val count: Int = mListAnchors.size
            for (i in 0 until count) {
                mListAnchors[i] = mAnchor
            }
        }

    
    fun getAnchor(anchorType: ConstraintAnchor.Type?): ConstraintAnchor? {
        when (anchorType) {
            ConstraintAnchor.Type.LEFT, ConstraintAnchor.Type.RIGHT -> {
                if (mOrientation == VERTICAL) {
                    return mAnchor
                }
            }
            ConstraintAnchor.Type.TOP, ConstraintAnchor.Type.BOTTOM -> {
                if (mOrientation == HORIZONTAL) {
                    return mAnchor
                }
            }
            ConstraintAnchor.Type.BASELINE, ConstraintAnchor.Type.CENTER, ConstraintAnchor.Type.CENTER_X, ConstraintAnchor.Type.CENTER_Y, ConstraintAnchor.Type.NONE -> return null
            else -> return null
        }

        return null
    }

    // @TODO: add description
    fun setGuidePercent(value: Int) {
        setGuidePercent(value / 100f)
    }

    // @TODO: add description
    fun setGuidePercent(value: Float) {
        if (value > -1) {
            relativePercent = value
            relativeBegin = -1
            relativeEnd = -1
        }
    }

    // @TODO: add description
    fun setGuideBegin(value: Int) {
        if (value > -1) {
            relativePercent = -1f
            relativeBegin = value
            relativeEnd = -1
        }
    }

    // @TODO: add description
    fun setGuideEnd(value: Int) {
        if (value > -1) {
            relativePercent = -1f
            relativeBegin = -1
            relativeEnd = value
        }
    }

    // @TODO: add description
    fun setFinalValue(position: Int) {
        if (LinearSystem.FULL_DEBUG) {
            println(
                "*** SET FINAL GUIDELINE VALUE "
                        + position + " FOR " + debugName
            )
        }
        mAnchor.finalValue = position
        mResolved = true
    }

    
    override fun addToSolver(system: LinearSystem, optimize: Boolean) {
        if (LinearSystem.FULL_DEBUG) {
            println("\n----------------------------------------------")
            println("-- adding " + debugName.toString() + " to the solver")
            println("----------------------------------------------\n")
        }
        val parent: ConstraintWidgetContainer? = getParent() as ConstraintWidgetContainer?
        var begin: ConstraintAnchor? = parent?.getAnchor(ConstraintAnchor.Type.LEFT)
        var end: ConstraintAnchor? = parent?.getAnchor(ConstraintAnchor.Type.RIGHT)
        var parentWrapContent =
            if (parent != null) parent.mListDimensionBehaviors[DIMENSION_HORIZONTAL] == WRAP_CONTENT else false
        if (mOrientation == HORIZONTAL) {
            begin = parent?.getAnchor(ConstraintAnchor.Type.TOP)
            end = parent?.getAnchor(ConstraintAnchor.Type.BOTTOM)
            parentWrapContent =
                if (parent != null) parent.mListDimensionBehaviors[DIMENSION_VERTICAL] == WRAP_CONTENT else false
        }
        if (mResolved && mAnchor.hasFinalValue()) {
            val guide: SolverVariable? = system?.createObjectVariable(mAnchor)
            if (LinearSystem.FULL_DEBUG) {
                println(
                    ("*** SET FINAL POSITION FOR GUIDELINE "
                            + debugName) + " TO " + mAnchor.finalValue
                )
            }
            system?.addEquality(guide, mAnchor.finalValue)
            if (relativeBegin != -1) {
                if (parentWrapContent) {
                    system?.addGreaterThan(
                        system.createObjectVariable(end)!!, guide,
                        0, SolverVariable.STRENGTH_EQUALITY
                    )
                }
            } else if (relativeEnd != -1) {
                if (parentWrapContent) {
                    val parentRight: SolverVariable? = system?.createObjectVariable(end)
                    system?.addGreaterThan(
                        guide, system.createObjectVariable(begin)!!,
                        0, SolverVariable.STRENGTH_EQUALITY
                    )
                    system?.addGreaterThan(parentRight, guide, 0, SolverVariable.STRENGTH_EQUALITY)
                }
            }
            mResolved = false
            return
        }
        if (relativeBegin != -1) {
            val guide: SolverVariable? = system?.createObjectVariable(mAnchor)
            val parentLeft: SolverVariable? = system?.createObjectVariable(begin)
            system?.addEquality(guide, parentLeft, relativeBegin, SolverVariable.STRENGTH_FIXED)
            if (parentWrapContent) {
                system?.addGreaterThan(
                    system.createObjectVariable(end)!!,
                    guide, 0, SolverVariable.STRENGTH_EQUALITY
                )
            }
        } else if (relativeEnd != -1) {
            val guide: SolverVariable? = system?.createObjectVariable(mAnchor)
            val parentRight: SolverVariable? = system?.createObjectVariable(end)
            system?.addEquality(guide, parentRight, -relativeEnd, SolverVariable.STRENGTH_FIXED)
            if (parentWrapContent) {
                system?.addGreaterThan(
                    guide, system.createObjectVariable(begin)!!,
                    0, SolverVariable.STRENGTH_EQUALITY
                )
                system?.addGreaterThan(parentRight, guide, 0, SolverVariable.STRENGTH_EQUALITY)
            }
        } else if (relativePercent != -1f) {
            val guide: SolverVariable? = system?.createObjectVariable(mAnchor)
            val parentRight: SolverVariable? = system?.createObjectVariable(end)
            system?.addConstraint(
                LinearSystem
                    .createRowDimensionPercent(
                        system, guide, parentRight,
                        relativePercent
                    )
            )
        }
    }

    
    override fun updateFromSolver(system: LinearSystem, optimize: Boolean) {
        if (getParent() == null) {
            return
        }
        val value: Int = system.getObjectVariableValue(mAnchor)
        if (mOrientation == VERTICAL) {
            x = value
            y = 0
            height = getParent()!!.height
            width = 0
        } else {
            x = 0
            y = value
            width = getParent()!!.width
            height = 0
        }
    }

    fun inferRelativePercentPosition() {
        var percent: Float = x.toFloat() / (getParent()?.width?.toFloat() ?: 1f)
        if (mOrientation == HORIZONTAL) {
            percent = y.toFloat() / (getParent()?.height?.toFloat() ?: 1f)
        }
        setGuidePercent(percent)
    }

    fun inferRelativeBeginPosition() {
        var position: Int = x
        if (mOrientation == HORIZONTAL) {
            position = y
        }
        setGuideBegin(position)
    }

    fun inferRelativeEndPosition() {
        var position: Int = (getParent()?.width ?: 0) - x
        if (mOrientation == HORIZONTAL) {
            position = (getParent()?.height ?: 0) - y
        }
        setGuideEnd(position)
    }

    // @TODO: add description
    fun cyclePosition() {
        if (relativeBegin != -1) {
            // cycle to percent-based position
            inferRelativePercentPosition()
        } else if (relativePercent != -1f) {
            // cycle to end-based position
            inferRelativeEndPosition()
        } else if (relativeEnd != -1) {
            // cycle to begin-based position
            inferRelativeBeginPosition()
        }
    }

    val isPercent: Boolean
        get() = relativePercent != -1f && relativeBegin == -1 && relativeEnd == -1

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val RELATIVE_PERCENT = 0
        const val RELATIVE_BEGIN = 1
        const val RELATIVE_END = 2
        const val RELATIVE_UNKNOWN = -1
    }
}