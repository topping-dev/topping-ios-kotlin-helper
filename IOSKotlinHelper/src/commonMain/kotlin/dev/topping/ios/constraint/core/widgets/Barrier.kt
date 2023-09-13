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
package dev.topping.ios.constraint.core.widgets

import dev.topping.ios.constraint.core.LinearSystem
import dev.topping.ios.constraint.core.SolverVariable

import kotlin.math.*

/**
 * A Barrier takes multiple widgets
 */
class Barrier : HelperWidget {
    var barrierType = LEFT

    /**
     * Find if this barrier supports gone widgets.
     *
     * @return true if this barrier supports gone widgets, otherwise false
     */
    var allowsGoneWidget = true
    var margin = 0
    
    override var isResolvedVertically = false
         get() = field

    constructor() {}
    constructor(debugName: String?) {
        this.debugName = debugName
    }

    
    override fun allowedInBarrier(): Boolean {
        return true
    }

    /**
     * Find if this barrier supports gone widgets.
     *
     * @return true if this barrier supports gone widgets, otherwise false
     */
    @Deprecated(
        """This method should be called {@code getAllowsGoneWidget}
      such that {@code allowsGoneWidget}
      can be accessed as a property from Kotlin; {@see https://android.github
     * .io/kotlin-guides/interop.html#property-prefixes}.
      Use {@link #getAllowsGoneWidget()} instead."""
    )
    fun allowsGoneWidget(): Boolean {
        return allowsGoneWidget
    }

    
    override fun copy(src: ConstraintWidget, map: MutableMap<ConstraintWidget, ConstraintWidget>) {
        super.copy(src, map)
        val srcBarrier = src as Barrier
        barrierType = srcBarrier.barrierType
        allowsGoneWidget = srcBarrier.allowsGoneWidget
        margin = srcBarrier.margin
    }

    
    override fun toString(): String {
        var debug = "[Barrier] " + debugName.toString() + " {"
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets[i]
            if (i > 0) {
                debug += ", "
            }
            debug += widget?.debugName.toString()
        }
        debug += "}"
        return debug
    }

    fun markWidgets() {
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets.get(i)
            if (!allowsGoneWidget && widget?.allowedInBarrier() == false) {
                continue
            }
            if (barrierType == LEFT || barrierType == RIGHT) {
                widget?.setInBarrier(HORIZONTAL, true)
            } else if (barrierType == TOP || barrierType == BOTTOM) {
                widget?.setInBarrier(VERTICAL, true)
            }
        }
    }

    /**
     * Add this widget to the solver
     *
     * @param system   the solver we want to add the widget to
     * @param optimize true if [Optimizer.OPTIMIZATION_GRAPH] is on
     */
    
    override fun addToSolver(system: LinearSystem?, optimize: Boolean) {
        if (LinearSystem.FULL_DEBUG) {
            println("\n----------------------------------------------")
            println("-- adding " + debugName.toString() + " to the solver")
            println("----------------------------------------------\n")
        }
        val position: ConstraintAnchor
        mListAnchors[LEFT] = mLeft
        mListAnchors[TOP] = mTop
        mListAnchors[RIGHT] = mRight
        mListAnchors[BOTTOM] = mBottom
        for (i in mListAnchors.indices) {
            mListAnchors[i].mSolverVariable = system?.createObjectVariable(mListAnchors[i])
        }
        position = if (barrierType in 0..3) {
            mListAnchors[barrierType]
        } else {
            return
        }
        if (USE_RESOLUTION) {
            if (!isResolvedVertically) {
                allSolved()
            }
            if (isResolvedVertically) {
                isResolvedVertically = false
                if (barrierType == LEFT || barrierType == RIGHT) {
                    system?.addEquality(mLeft.mSolverVariable, mX)
                    system?.addEquality(mRight.mSolverVariable, mX)
                } else if (barrierType == TOP || barrierType == BOTTOM) {
                    system?.addEquality(mTop.mSolverVariable, mY)
                    system?.addEquality(mBottom.mSolverVariable, mY)
                }
                return
            }
        }

        // We have to handle the case where some of the elements
        //  referenced in the barrier are set as
        // match_constraint; we have to take it in account to set the strength of the barrier.
        var hasMatchConstraintWidgets = false
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets.get(i)
            if (!allowsGoneWidget && widget?.allowedInBarrier() == false) {
                continue
            }
            if ((barrierType == LEFT || barrierType == RIGHT)
                && (widget?.horizontalDimensionBehaviour
                        == DimensionBehaviour.MATCH_CONSTRAINT)
                && widget.mLeft.target != null && widget.mRight.target != null
            ) {
                hasMatchConstraintWidgets = true
                break
            } else if ((barrierType == TOP || barrierType == BOTTOM)
                && (widget?.verticalDimensionBehaviour
                        == DimensionBehaviour.MATCH_CONSTRAINT)
                && widget.mTop.target != null && widget.mBottom.target != null
            ) {
                hasMatchConstraintWidgets = true
                break
            }
        }
        val mHasHorizontalCenteredDependents =
            mLeft.hasCenteredDependents() || mRight.hasCenteredDependents()
        val mHasVerticalCenteredDependents =
            mTop.hasCenteredDependents() || mBottom.hasCenteredDependents()
        val applyEqualityOnReferences = (!hasMatchConstraintWidgets
                && (barrierType == LEFT && mHasHorizontalCenteredDependents
                || barrierType == TOP && mHasVerticalCenteredDependents
                || barrierType == RIGHT && mHasHorizontalCenteredDependents
                || barrierType == BOTTOM && mHasVerticalCenteredDependents))
        var equalityOnReferencesStrength: Int = SolverVariable.STRENGTH_EQUALITY
        if (!applyEqualityOnReferences) {
            equalityOnReferencesStrength = SolverVariable.STRENGTH_HIGHEST
        }
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets[i]
            if (!allowsGoneWidget && widget?.allowedInBarrier() == false) {
                continue
            }
            val target: SolverVariable? = system?.createObjectVariable(
                widget?.mListAnchors?.get(barrierType)
            )
            widget?.mListAnchors?.get(barrierType)?.mSolverVariable = target
            var margin = 0
            if (widget?.mListAnchors?.get(barrierType)?.target != null
                && widget.mListAnchors[barrierType].target!!.mOwner == this
            ) {
                margin += widget.mListAnchors[barrierType].mMargin
            }
            if (position.mSolverVariable != null && target != null) {
                if (barrierType == LEFT || barrierType == TOP) {
                    system.addLowerBarrier(
                        position.mSolverVariable!!, target,
                        this.margin - margin, hasMatchConstraintWidgets
                        )
                    }
                else {
                    system.addGreaterBarrier(
                        position.mSolverVariable!!, target,
                        this.margin + margin, hasMatchConstraintWidgets
                    )
                }
            }
            if (USE_RELAX_GONE) {
                if (widget?.visibility != GONE || widget is Guideline || widget is Barrier) {
                    system?.addEquality(
                        position.mSolverVariable, target,
                        this.margin + margin, equalityOnReferencesStrength
                    )
                }
            } else {
                system?.addEquality(
                    position.mSolverVariable, target,
                    this.margin + margin, equalityOnReferencesStrength
                )
            }
        }
        val barrierParentStrength: Int = SolverVariable.STRENGTH_HIGHEST
        val barrierParentStrengthOpposite: Int = SolverVariable.STRENGTH_NONE
        if (barrierType == LEFT) {
            system?.addEquality(
                mRight.mSolverVariable,
                mLeft.mSolverVariable, 0, SolverVariable.STRENGTH_FIXED
            )
            system?.addEquality(
                mLeft.mSolverVariable,
                parent?.mRight?.mSolverVariable, 0, barrierParentStrength
            )
            system?.addEquality(
                mLeft.mSolverVariable,
                parent?.mLeft?.mSolverVariable, 0, barrierParentStrengthOpposite
            )
        } else if (barrierType == RIGHT) {
            system?.addEquality(
                mLeft.mSolverVariable,
                mRight.mSolverVariable, 0, SolverVariable.STRENGTH_FIXED
            )
            system?.addEquality(
                mLeft.mSolverVariable,
                parent?.mLeft?.mSolverVariable, 0, barrierParentStrength
            )
            system?.addEquality(
                mLeft.mSolverVariable,
                parent?.mRight?.mSolverVariable, 0, barrierParentStrengthOpposite
            )
        } else if (barrierType == TOP) {
            system?.addEquality(
                mBottom.mSolverVariable,
                mTop.mSolverVariable, 0, SolverVariable.STRENGTH_FIXED
            )
            system?.addEquality(
                mTop.mSolverVariable,
                parent?.mBottom?.mSolverVariable, 0, barrierParentStrength
            )
            system?.addEquality(
                mTop.mSolverVariable,
                parent?.mTop?.mSolverVariable, 0, barrierParentStrengthOpposite
            )
        } else if (barrierType == BOTTOM) {
            system?.addEquality(
                mTop.mSolverVariable,
                mBottom.mSolverVariable, 0, SolverVariable.STRENGTH_FIXED
            )
            system?.addEquality(
                mTop.mSolverVariable,
                parent?.mTop?.mSolverVariable, 0, barrierParentStrength
            )
            system?.addEquality(
                mTop.mSolverVariable,
                parent?.mBottom?.mSolverVariable, 0, barrierParentStrengthOpposite
            )
        }
    }

    // @TODO: add description
    val orientation: Int
        get() {
            when (barrierType) {
                LEFT, RIGHT -> return HORIZONTAL
                TOP, BOTTOM -> return VERTICAL
            }
            return UNKNOWN
        }

    // @TODO: add description
    fun allSolved(): Boolean {
        if (!USE_RESOLUTION) {
            return false
        }
        var hasAllWidgetsResolved = true
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets.get(i)
            if (!allowsGoneWidget && widget?.allowedInBarrier() == false) {
                continue
            }
            if ((barrierType == LEFT || barrierType == RIGHT)
                && widget?.isResolvedHorizontally == false
            ) {
                hasAllWidgetsResolved = false
            } else if ((barrierType == TOP || barrierType == BOTTOM)
                && widget?.isResolvedVertically == false
            ) {
                hasAllWidgetsResolved = false
            }
        }
        if (hasAllWidgetsResolved && mWidgetsCount > 0) {
            // we're done!
            var barrierPosition = 0
            var initialized = false
            for (i in 0 until mWidgetsCount) {
                val widget: ConstraintWidget? = mWidgets.get(i)
                if (!allowsGoneWidget && widget?.allowedInBarrier() == false) {
                    continue
                }
                if (!initialized) {
                    if (barrierType == LEFT) {
                        barrierPosition =
                            widget?.getAnchor(ConstraintAnchor.Type.LEFT)?.finalValue ?: 0
                    } else if (barrierType == RIGHT) {
                        barrierPosition =
                            widget?.getAnchor(ConstraintAnchor.Type.RIGHT)?.finalValue ?: 0
                    } else if (barrierType == TOP) {
                        barrierPosition =
                            widget?.getAnchor(ConstraintAnchor.Type.TOP)?.finalValue ?: 0
                    } else if (barrierType == BOTTOM) {
                        barrierPosition =
                            widget?.getAnchor(ConstraintAnchor.Type.BOTTOM)?.finalValue ?: 0
                    }
                    initialized = true
                }
                if (barrierType == LEFT) {
                    barrierPosition = min(
                        barrierPosition,
                        widget?.getAnchor(ConstraintAnchor.Type.LEFT)?.finalValue ?: 0
                    )
                } else if (barrierType == RIGHT) {
                    barrierPosition = max(
                        barrierPosition,
                        widget?.getAnchor(ConstraintAnchor.Type.RIGHT)?.finalValue ?: 0
                    )
                } else if (barrierType == TOP) {
                    barrierPosition = min(
                        barrierPosition,
                        widget?.getAnchor(ConstraintAnchor.Type.TOP)?.finalValue ?: 0
                    )
                } else if (barrierType == BOTTOM) {
                    barrierPosition = max(
                        barrierPosition,
                        widget?.getAnchor(ConstraintAnchor.Type.BOTTOM)?.finalValue ?: 0
                    )
                }
            }
            barrierPosition += margin
            if (barrierType == LEFT || barrierType == RIGHT) {
                setFinalHorizontal(barrierPosition, barrierPosition)
            } else {
                setFinalVertical(barrierPosition, barrierPosition)
            }
            if (LinearSystem.FULL_DEBUG) {
                println(
                    "*** BARRIER " + debugName
                        .toString() + " SOLVED TO " + barrierPosition.toString() + " ***"
                )
            }
            isResolvedVertically = true
            return true
        }
        return false
    }

    companion object {
        const val LEFT = 0
        const val RIGHT = 1
        const val TOP = 2
        const val BOTTOM = 3
        private const val USE_RESOLUTION = true
        private const val USE_RELAX_GONE = false
    }
}