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

import dev.topping.ios.constraint.core.ArrayRow
import dev.topping.ios.constraint.core.LinearSystem
import dev.topping.ios.constraint.core.SolverVariable
import dev.topping.ios.constraint.core.widgets.ConstraintWidget.Companion.GONE
import dev.topping.ios.constraint.core.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
import dev.topping.ios.constraint.core.widgets.ConstraintWidget.DimensionBehaviour
import dev.topping.ios.constraint.core.widgets.analyzer.Direct

/**
 * Chain management and constraints creation
 */
object Chain {
    private const val I_DEBUG = false
    const val USE_CHAIN_OPTIMIZATION = false

    /**
     * Apply specific rules for dealing with chains of widgets.
     * Chains are defined as a list of widget linked together with bi-directional connections
     *
     * @param constraintWidgetContainer root container
     * @param system                    the linear system we add the equations to
     * @param orientation               HORIZONTAL or VERTICAL
     */
    fun applyChainConstraints(
        constraintWidgetContainer: ConstraintWidgetContainer,
        system: LinearSystem,
        widgets: ArrayList<ConstraintWidget?>?,
        orientation: Int
    ) {
        // what to do:
        // Don't skip things. Either the element is GONE or not.
        var offset = 0
        var chainsSize = 0
        var chainsArray: Array<ChainHead?>
        if (orientation == ConstraintWidget.HORIZONTAL) {
            offset = 0
            chainsSize = constraintWidgetContainer.mHorizontalChainsSize
            chainsArray = constraintWidgetContainer.mHorizontalChainsArray
        } else {
            offset = 2
            chainsSize = constraintWidgetContainer.mVerticalChainsSize
            chainsArray = constraintWidgetContainer.mVerticalChainsArray
        }
        for (i in 0 until chainsSize) {
            val first: ChainHead? = chainsArray[i]
            // we have to make sure we define the ChainHead here,
            // otherwise the values we use may not be correctly initialized
            // (as we initialize them in the ConstraintWidget.addToSolver())
            first?.let {
                first.define()
                if (widgets == null || widgets.contains(first.mFirst)) {
                    applyChainConstraints(
                        constraintWidgetContainer,
                        system, orientation, offset, first
                    )
                }
            }
        }
    }

    /**
     * Apply specific rules for dealing with chains of widgets.
     * Chains are defined as a list of widget linked together with bi-directional connections
     *
     * @param container   the root container
     * @param system      the linear system we add the equations to
     * @param orientation HORIZONTAL or VERTICAL
     * @param offset      0 or 2 to accommodate for HORIZONTAL / VERTICAL
     * @param chainHead   a chain represented by its main elements
     */
    fun applyChainConstraints(
        container: ConstraintWidgetContainer, system: LinearSystem,
        orientation: Int, offset: Int, chainHead: ChainHead
    ) {
        val first: ConstraintWidget = chainHead.mFirst
        val last: ConstraintWidget? = chainHead.mLast
        val firstVisibleWidget: ConstraintWidget? = chainHead.mFirstVisibleWidget
        var lastVisibleWidget: ConstraintWidget? = chainHead.mLastVisibleWidget
        val head: ConstraintWidget? = chainHead.mHead
        var widget: ConstraintWidget? = first
        var next: ConstraintWidget? = null
        var done = false
        var totalWeights: Float = chainHead.totalWeight
         val firstMatchConstraintsWidget: ConstraintWidget? =
             chainHead.mFirstMatchConstraintWidget
         val previousMatchConstraintsWidget: ConstraintWidget? =
             chainHead.mLastMatchConstraintWidget
        val isWrapContent = (container.mListDimensionBehaviors.get(orientation)
                == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        var isChainSpread = false
        var isChainSpreadInside = false
        var isChainPacked = false
        if (orientation == ConstraintWidget.HORIZONTAL) {
            isChainSpread = head?.horizontalChainStyle == ConstraintWidget.CHAIN_SPREAD
            isChainSpreadInside =
                head?.horizontalChainStyle == ConstraintWidget.CHAIN_SPREAD_INSIDE
            isChainPacked = head?.horizontalChainStyle == ConstraintWidget.CHAIN_PACKED
        } else {
            isChainSpread = head?.mVerticalChainStyle == ConstraintWidget.CHAIN_SPREAD
            isChainSpreadInside =
                head?.mVerticalChainStyle == ConstraintWidget.CHAIN_SPREAD_INSIDE
            isChainPacked = head?.mVerticalChainStyle == ConstraintWidget.CHAIN_PACKED
        }
        if (USE_CHAIN_OPTIMIZATION && !isWrapContent
            && Direct.solveChain(
                container, system, orientation, offset, chainHead,
                isChainSpread, isChainSpreadInside, isChainPacked
            )
        ) {
            if (LinearSystem.FULL_DEBUG) {
                println("### CHAIN FULLY SOLVED! ###")
            }
            return  // done with the chain!
        } else if (LinearSystem.FULL_DEBUG) {
            println("### CHAIN WASN'T SOLVED DIRECTLY... ###")
        }

        // This traversal will:
        // - set up some basic ordering constraints
        // - build a linked list of matched constraints widgets
        while (!done) {
            val begin: ConstraintAnchor = widget!!.mListAnchors.get(offset)
            var strength: Int = SolverVariable.STRENGTH_HIGHEST
            if (isChainPacked) {
                strength = SolverVariable.STRENGTH_LOW
            }
            var margin: Int = begin.margin
            val isSpreadOnly = (widget.mListDimensionBehaviors.get(orientation)
                    == DimensionBehaviour.MATCH_CONSTRAINT
                    && widget.mResolvedMatchConstraintDefault.get(orientation)
                    == MATCH_CONSTRAINT_SPREAD)
            if (begin.target != null && widget != first) {
                margin += begin.target!!.margin
            }
            if (isChainPacked && widget != first && widget != firstVisibleWidget) {
                strength = SolverVariable.STRENGTH_FIXED
            }
            if (begin.target != null) {
                if (widget == firstVisibleWidget) {
                    system.addGreaterThan(
                        begin.mSolverVariable, begin.target!!.mSolverVariable,
                        margin, SolverVariable.STRENGTH_BARRIER
                    )
                } else {
                    system.addGreaterThan(
                        begin.mSolverVariable, begin.target!!.mSolverVariable,
                        margin, SolverVariable.STRENGTH_FIXED
                    )
                }
                if (isSpreadOnly && !isChainPacked) {
                    strength = SolverVariable.STRENGTH_EQUALITY
                }
                if (widget == firstVisibleWidget && isChainPacked
                    && widget.isInBarrier(orientation)
                ) {
                    strength = SolverVariable.STRENGTH_EQUALITY
                }
                system.addEquality(
                    begin.mSolverVariable, begin.target!!.mSolverVariable, margin,
                    strength
                )
            }
            if (isWrapContent) {
                if (widget.visibility != ConstraintWidget.GONE
                    && widget.mListDimensionBehaviors[orientation]
                    == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    system.addGreaterThan(
                        widget.mListAnchors[offset + 1].mSolverVariable,
                        widget.mListAnchors[offset].mSolverVariable, 0,
                        SolverVariable.STRENGTH_EQUALITY
                    )
                }
                system.addGreaterThan(
                    widget.mListAnchors[offset].mSolverVariable,
                    container.mListAnchors[offset].mSolverVariable,
                    0, SolverVariable.STRENGTH_FIXED
                )
            }

            // go to the next widget
            val nextAnchor: ConstraintAnchor? = widget.mListAnchors.get(offset + 1).target
            if (nextAnchor != null) {
                next = nextAnchor.mOwner
                if (next.mListAnchors[offset].target == null
                    || next.mListAnchors[offset].target?.mOwner != widget
                ) {
                    next = null
                }
            } else {
                next = null
            }
            if (next != null) {
                widget = next
            } else {
                done = true
            }
        }

        // Make sure we have constraints for the last anchors / targets
        if (lastVisibleWidget != null && last!!.mListAnchors.get(offset + 1).target != null) {
            val end: ConstraintAnchor = lastVisibleWidget.mListAnchors.get(offset + 1)
            val isSpreadOnly = (lastVisibleWidget.mListDimensionBehaviors.get(orientation)
                    == DimensionBehaviour.MATCH_CONSTRAINT
                    && lastVisibleWidget.mResolvedMatchConstraintDefault.get(orientation)
                    == MATCH_CONSTRAINT_SPREAD)
            if (isSpreadOnly && !isChainPacked && end.target?.mOwner == container) {
                system.addEquality(
                    end.mSolverVariable, end.target?.mSolverVariable,
                    -end.margin, SolverVariable.STRENGTH_EQUALITY
                )
            } else if (isChainPacked && end.target?.mOwner == container) {
                system.addEquality(
                    end.mSolverVariable, end.target?.mSolverVariable,
                    -end.margin, SolverVariable.STRENGTH_HIGHEST
                )
            }
            system.addLowerThan(
                end.mSolverVariable,
                last.mListAnchors.get(offset + 1).target?.mSolverVariable, -end.margin,
                SolverVariable.STRENGTH_BARRIER
            )
        }

        // ... and make sure the root end is constrained in wrap content.
        if (isWrapContent) {
            system.addGreaterThan(
                container.mListAnchors.get(offset + 1).mSolverVariable,
                last!!.mListAnchors.get(offset + 1)!!.mSolverVariable,
                last!!.mListAnchors.get(offset + 1).margin, SolverVariable.STRENGTH_FIXED
            )
        }

        // Now, let's apply the centering / spreading for matched constraints widgets
        val listMatchConstraints: ArrayList<ConstraintWidget>? =
            chainHead.mWeightedMatchConstraintsWidgets
        if (listMatchConstraints != null) {
            val count: Int = listMatchConstraints.size
            if (count > 1) {
                var lastMatch: ConstraintWidget? = null
                var lastWeight = 0f
                if (chainHead.mHasUndefinedWeights && !chainHead.mHasComplexMatchWeights) {
                    totalWeights = chainHead.mWidgetsMatchCount.toFloat()
                }
                for (i in 0 until count) {
                    val match: ConstraintWidget = listMatchConstraints[i]
                    var currentWeight: Float = match.mWeight.get(orientation)
                    if (currentWeight < 0) {
                        if (chainHead.mHasComplexMatchWeights) {
                            system.addEquality(
                                match.mListAnchors.get(offset + 1).mSolverVariable,
                                match.mListAnchors.get(offset).mSolverVariable,
                                0, SolverVariable.STRENGTH_HIGHEST
                            )
                            continue
                        }
                        currentWeight = 1f
                    }
                    if (currentWeight == 0f) {
                        system.addEquality(
                            match.mListAnchors.get(offset + 1).mSolverVariable,
                            match.mListAnchors.get(offset).mSolverVariable,
                            0, SolverVariable.STRENGTH_FIXED
                        )
                        continue
                    }
                    if (lastMatch != null) {
                        val begin: SolverVariable? =
                            lastMatch.mListAnchors.get(offset).mSolverVariable
                        val end: SolverVariable? =
                            lastMatch.mListAnchors.get(offset + 1).mSolverVariable
                        val nextBegin: SolverVariable? =
                            match.mListAnchors.get(offset).mSolverVariable
                        val nextEnd: SolverVariable? =
                            match.mListAnchors.get(offset + 1).mSolverVariable
                        val row: ArrayRow = system.createRow()
                        row.createRowEqualMatchDimensions(
                            lastWeight, totalWeights, currentWeight,
                            begin, end, nextBegin, nextEnd
                        )
                        system.addConstraint(row)
                    }
                    lastMatch = match
                    lastWeight = currentWeight
                }
            }
        }
        if (I_DEBUG) {
            widget = firstVisibleWidget
            while (widget != null) {
                next = widget.mNextChainWidget.get(orientation)
                widget.mListAnchors.get(offset).mSolverVariable?.name = "" + widget.debugName.toString() + ".left"
                widget.mListAnchors.get(offset + 1).mSolverVariable?.name = "" + widget.debugName.toString() + ".right"
                widget = next
            }
        }

        // Finally, let's apply the specific rules dealing with the different chain types
        if (firstVisibleWidget != null
            && (firstVisibleWidget == lastVisibleWidget || isChainPacked)
        ) {
            var begin: ConstraintAnchor = first.mListAnchors.get(offset)
            var end: ConstraintAnchor = last!!.mListAnchors.get(offset + 1)
            val beginTarget: SolverVariable? =
                if (begin.target != null) begin.target!!.mSolverVariable else null
            val endTarget: SolverVariable? =
                if (end.target != null) end.target!!.mSolverVariable else null
            begin = firstVisibleWidget.mListAnchors.get(offset)
            if (lastVisibleWidget != null) {
                end = lastVisibleWidget.mListAnchors.get(offset + 1)
            }
            if (beginTarget != null && endTarget != null) {
                var bias = 0.5f
                bias = if (orientation == ConstraintWidget.HORIZONTAL) {
                    head?.horizontalBiasPercent
                } else {
                    head?.mVerticalBiasPercent
                } ?: 0f
                val beginMargin: Int = begin.margin
                val endMargin: Int = end.margin
                system.addCentering(
                    begin.mSolverVariable, beginTarget,
                    beginMargin, bias, endTarget, end.mSolverVariable,
                    endMargin, SolverVariable.STRENGTH_CENTERING
                )
            }
        } else if (isChainSpread && firstVisibleWidget != null) {
            // for chain spread, we need to add equal dimensions in between *visible* widgets
            widget = firstVisibleWidget
            var previousVisibleWidget: ConstraintWidget = firstVisibleWidget
            val applyFixedEquality = (chainHead.mWidgetsMatchCount > 0
                    && chainHead.mWidgetsCount == chainHead.mWidgetsMatchCount)
            while (widget != null) {
                next = widget.mNextChainWidget.get(orientation)
                while (next != null && next.visibility == GONE) {
                    next = next.mNextChainWidget.get(orientation)
                }
                if (next != null || widget == lastVisibleWidget) {
                    val beginAnchor: ConstraintAnchor = widget.mListAnchors.get(offset)
                    val begin: SolverVariable? = beginAnchor.mSolverVariable
                    var beginTarget: SolverVariable? =
                        if (beginAnchor.target != null) beginAnchor.target?.mSolverVariable else null
                    if (previousVisibleWidget != widget) {
                        beginTarget =
                            previousVisibleWidget.mListAnchors.get(offset + 1).mSolverVariable
                    } else if (widget == firstVisibleWidget) {
                        beginTarget =
                            if (first.mListAnchors.get(offset).target != null) first.mListAnchors.get(
                                offset
                            ).target?.mSolverVariable else null
                    }
                    var beginNextAnchor: ConstraintAnchor? = null
                    var beginNext: SolverVariable? = null
                     var beginNextTarget: SolverVariable? = null
                    var beginMargin: Int = beginAnchor.margin
                    var nextMargin: Int = widget.mListAnchors.get(offset + 1).margin
                    if (next != null) {
                        beginNextAnchor = next.mListAnchors.get(offset)
                        beginNext = beginNextAnchor.mSolverVariable
                    } else {
                        beginNextAnchor = last!!.mListAnchors.get(offset + 1).target
                        if (beginNextAnchor != null) {
                            beginNext = beginNextAnchor.mSolverVariable
                        }
                    }
                    beginNextTarget = widget.mListAnchors.get(offset + 1).mSolverVariable
                    if (beginNextAnchor != null) {
                        nextMargin += beginNextAnchor.margin
                    }
                    beginMargin += previousVisibleWidget.mListAnchors.get(offset + 1).margin
                    if (begin != null && beginTarget != null && beginNext != null && beginNextTarget != null) {
                        var margin1 = beginMargin
                        if (widget == firstVisibleWidget) {
                            margin1 = firstVisibleWidget.mListAnchors.get(offset).margin
                        }
                        var margin2 = nextMargin
                        if (widget == lastVisibleWidget) {
                            margin2 = lastVisibleWidget.mListAnchors.get(offset + 1).margin
                        }
                        var strength: Int = SolverVariable.STRENGTH_EQUALITY
                        if (applyFixedEquality) {
                            strength = SolverVariable.STRENGTH_FIXED
                        }
                        system.addCentering(
                            begin, beginTarget, margin1, 0.5f,
                            beginNext, beginNextTarget, margin2,
                            strength
                        )
                    }
                }
                if (widget.visibility != GONE) {
                    previousVisibleWidget = widget
                }
                widget = next
            }
        } else if (isChainSpreadInside && firstVisibleWidget != null) {
            // for chain spread inside, we need to add equal dimensions in between *visible* widgets
            widget = firstVisibleWidget
            var previousVisibleWidget: ConstraintWidget = firstVisibleWidget
            val applyFixedEquality = (chainHead.mWidgetsMatchCount > 0
                    && chainHead.mWidgetsCount == chainHead.mWidgetsMatchCount)
            while (widget != null) {
                next = widget.mNextChainWidget.get(orientation)
                while (next != null && next.visibility == GONE) {
                    next = next.mNextChainWidget.get(orientation)
                }
                if (widget != firstVisibleWidget && widget != lastVisibleWidget && next != null) {
                    if (next == lastVisibleWidget) {
                        next = null
                    }
                    val beginAnchor: ConstraintAnchor = widget.mListAnchors.get(offset)
                    val begin: SolverVariable? = beginAnchor.mSolverVariable
                     var beginTarget: SolverVariable? =
                        if (beginAnchor.target != null) beginAnchor.target?.mSolverVariable else null
                    beginTarget =
                        previousVisibleWidget.mListAnchors.get(offset + 1)!!.mSolverVariable
                    var beginNextAnchor: ConstraintAnchor? = null
                    var beginNext: SolverVariable? = null
                    var beginNextTarget: SolverVariable? = null
                    var beginMargin: Int = beginAnchor.margin
                    var nextMargin: Int = widget.mListAnchors.get(offset + 1).margin
                    if (next != null) {
                        beginNextAnchor = next.mListAnchors.get(offset)
                        beginNext = beginNextAnchor.mSolverVariable
                        beginNextTarget =
                            if (beginNextAnchor.target != null) beginNextAnchor.target!!.mSolverVariable else null
                    } else {
                        beginNextAnchor = lastVisibleWidget!!.mListAnchors.get(offset)
                        if (beginNextAnchor != null) {
                            beginNext = beginNextAnchor.mSolverVariable
                        }
                        beginNextTarget = widget.mListAnchors.get(offset + 1)!!.mSolverVariable
                    }
                    if (beginNextAnchor != null) {
                        nextMargin += beginNextAnchor.margin
                    }
                    beginMargin += previousVisibleWidget.mListAnchors.get(offset + 1).margin
                    var strength: Int = SolverVariable.STRENGTH_HIGHEST
                    if (applyFixedEquality) {
                        strength = SolverVariable.STRENGTH_FIXED
                    }
                    if (begin != null && beginTarget != null && beginNext != null && beginNextTarget != null) {
                        system.addCentering(
                            begin, beginTarget, beginMargin, 0.5f,
                            beginNext, beginNextTarget, nextMargin,
                            strength
                        )
                    }
                }
                if (widget.visibility != GONE) {
                    previousVisibleWidget = widget
                }
                widget = next
            }
            val begin: ConstraintAnchor = firstVisibleWidget.mListAnchors.get(offset)
            val beginTarget: ConstraintAnchor? = first.mListAnchors.get(offset).target
            val end: ConstraintAnchor? = lastVisibleWidget?.mListAnchors?.get(offset + 1)
            val endTarget: ConstraintAnchor? = last?.mListAnchors?.get(offset + 1)?.target
            val endPointsStrength: Int = SolverVariable.STRENGTH_EQUALITY
            if (beginTarget != null) {
                if (firstVisibleWidget != lastVisibleWidget) {
                    system.addEquality(
                        begin!!.mSolverVariable, beginTarget.mSolverVariable,
                        begin.margin, endPointsStrength
                    )
                } else if (endTarget != null) {
                    system.addCentering(
                        begin!!.mSolverVariable, beginTarget.mSolverVariable,
                        begin.margin, 0.5f, end!!.mSolverVariable, endTarget.mSolverVariable,
                        end.margin, endPointsStrength
                    )
                }
            }
            if (endTarget != null && firstVisibleWidget != lastVisibleWidget) {
                system.addEquality(
                    end!!.mSolverVariable,
                    endTarget.mSolverVariable, -end.margin, endPointsStrength
                )
            }
        }

        // final centering, necessary if the chain is larger than the available space...
        if ((isChainSpread || isChainSpreadInside) && (firstVisibleWidget
                    != null) && firstVisibleWidget != lastVisibleWidget
        ) {
            var begin: ConstraintAnchor = firstVisibleWidget.mListAnchors.get(offset)
            if (lastVisibleWidget == null) {
                lastVisibleWidget = firstVisibleWidget
            }
            var end: ConstraintAnchor = lastVisibleWidget.mListAnchors.get(offset + 1)
            val beginTarget: SolverVariable? =
                if (begin.target != null) begin.target!!.mSolverVariable else null
            var endTarget: SolverVariable? =
                if (end.target != null) end.target!!.mSolverVariable else null
            if (last != lastVisibleWidget) {
                val realEnd: ConstraintAnchor = last!!.mListAnchors.get(offset + 1)
                endTarget = if (realEnd.target != null) realEnd.target!!.mSolverVariable else null
            }
            if (firstVisibleWidget == lastVisibleWidget) {
                begin = firstVisibleWidget.mListAnchors.get(offset)
                end = firstVisibleWidget.mListAnchors.get(offset + 1)
            }
            if (beginTarget != null && endTarget != null) {
                val bias = 0.5f
                val beginMargin: Int = begin.margin
                val endMargin: Int = lastVisibleWidget.mListAnchors.get(offset + 1).margin
                system.addCentering(
                    begin!!.mSolverVariable, beginTarget, beginMargin,
                    bias, endTarget, end!!.mSolverVariable, endMargin,
                    SolverVariable.STRENGTH_EQUALITY
                )
            }
        }
    }
}