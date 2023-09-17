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
package dev.topping.ios.constraint.core.widgets.analyzer

import dev.topping.ios.constraint.core.LinearSystem
import dev.topping.ios.constraint.core.widgets.*
import dev.topping.ios.constraint.nanoTime

import kotlin.math.*

/**
 * Direct resolution engine
 *
 * This walks through the graph of dependencies and infer final position. This allows
 * us to skip the linear solver in many situations, as well as skipping intermediate measure passes.
 *
 * Widgets are solved independently in horizontal and vertical. Any widgets not fully resolved
 * will be computed later on by the linear solver.
 */
object Direct {
    private val I_DEBUG: Boolean = LinearSystem.FULL_DEBUG
    private const val APPLY_MATCH_PARENT = false
    private val sMeasure: BasicMeasure.Measure = BasicMeasure.Measure()
    private const val EARLY_TERMINATION = true // feature flag -- remove after release.
    private var sHcount = 0
    private var sVcount = 0

    /**
     * Walk the dependency graph and solves it.
     *
     * @param layout   the container we want to optimize
     * @param measurer the measurer used to measure the widget
     */
    fun solvingPass(
        layout: ConstraintWidgetContainer,
        measurer: BasicMeasure.Measurer
    ) {
        val horizontal: ConstraintWidget.DimensionBehaviour = layout.horizontalDimensionBehaviour
        val vertical: ConstraintWidget.DimensionBehaviour = layout.verticalDimensionBehaviour
        sHcount = 0
        sVcount = 0
        var time: Long = 0
        if (I_DEBUG) {
            time = nanoTime()
            println(
                "#### SOLVING PASS (horiz " + horizontal
                        + ", vert " + vertical + ") ####"
            )
        }
        layout.resetFinalResolution()
        val children: MutableList<ConstraintWidget> = layout.children
        val count: Int = children.size
        if (I_DEBUG) {
            println("#### SOLVING PASS on $count widgeets ####")
        }
        for (i in 0 until count) {
            val child: ConstraintWidget = children[i]
            child.resetFinalResolution()
        }
        val isRtl: Boolean = layout.isRtl

        // First, let's solve the horizontal dependencies, as it's a lot more common to have
        // a container with a fixed horizontal dimension (e.g. match_parent) than the opposite.

        // If we know our size, we can fully set the entire dimension, but if not we can
        // still solve what we can starting from the left.
        if (horizontal == ConstraintWidget.DimensionBehaviour.FIXED) {
            layout.setFinalHorizontal(0, layout.width)
        } else {
            layout.setFinalLeft(0)
        }
        if (I_DEBUG) {
            println("\n### Let's solve horizontal dependencies ###\n")
        }

        // Then let's first try to solve horizontal guidelines,
        // as they only depends on the container
        var hasGuideline = false
        var hasBarrier = false
        for (i in 0 until count) {
            val child: ConstraintWidget = children[i]
            if (child is Guideline) {
                val guideline: Guideline = child as Guideline
                if (guideline.orientation == Guideline.VERTICAL) {
                    if (guideline.relativeBegin != -1) {
                        guideline.setFinalValue(guideline.relativeBegin)
                    } else if (guideline.relativeEnd != -1
                        && layout.isResolvedHorizontally
                    ) {
                        guideline.setFinalValue(layout.width - guideline.relativeEnd)
                    } else if (layout.isResolvedHorizontally) {
                        val position: Int =
                            (0.5f + guideline.relativePercent * layout.width).toInt()
                        guideline.setFinalValue(position)
                    }
                    hasGuideline = true
                }
            } else if (child is Barrier) {
                val barrier: Barrier = child as Barrier
                if (barrier.orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    hasBarrier = true
                }
            }
        }
        if (hasGuideline) {
            if (I_DEBUG) {
                println("\n#### VERTICAL GUIDELINES CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children[i]
                if (child is Guideline) {
                    val guideline: Guideline = child as Guideline
                    if (guideline.orientation == Guideline.VERTICAL) {
                        horizontalSolvingPass(0, guideline, measurer, isRtl)
                    }
                }
            }
            if (I_DEBUG) {
                println("### Done solving guidelines.")
            }
        }
        if (I_DEBUG) {
            println("\n#### HORIZONTAL SOLVING PASS ####")
        }

        // Now let's resolve what we can in the dependencies of the container
        horizontalSolvingPass(0, layout, measurer, isRtl)

        // Finally, let's go through barriers, as they depends on widgets that may have been solved.
        if (hasBarrier) {
            if (I_DEBUG) {
                println("\n#### HORIZONTAL BARRIER CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children[i]
                if (child is Barrier) {
                    val barrier: Barrier = child as Barrier
                    if (barrier.orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        solveBarrier(
                            0,
                            barrier,
                            measurer,
                            ConstraintWidget.Companion.HORIZONTAL,
                            isRtl
                        )
                    }
                }
            }
            if (I_DEBUG) {
                println("#### DONE HORIZONTAL BARRIER CHECKS ####")
            }
        }
        if (I_DEBUG) {
            println("\n### Let's solve vertical dependencies now ###\n")
        }

        // Now we are done with the horizontal axis, let's see what we can do vertically
        if (vertical == ConstraintWidget.DimensionBehaviour.FIXED) {
            layout.setFinalVertical(0, layout.height)
        } else {
            layout.setFinalTop(0)
        }

        // Same thing as above -- let's start with guidelines...
        hasGuideline = false
        hasBarrier = false
        for (i in 0 until count) {
            val child: ConstraintWidget = children[i]
            if (child is Guideline) {
                val guideline: Guideline = child as Guideline
                if (guideline.orientation == Guideline.HORIZONTAL) {
                    if (guideline.relativeBegin != -1) {
                        guideline.setFinalValue(guideline.relativeBegin)
                    } else if (guideline.relativeEnd != -1 && layout.isResolvedVertically) {
                        guideline.setFinalValue(layout.height - guideline.relativeEnd)
                    } else if (layout.isResolvedVertically) {
                        val position: Int =
                            (0.5f + guideline.relativePercent * layout.height).toInt()
                        guideline.setFinalValue(position)
                    }
                    hasGuideline = true
                }
            } else if (child is Barrier) {
                val barrier: Barrier = child as Barrier
                if (barrier.orientation == ConstraintWidget.VERTICAL) {
                    hasBarrier = true
                }
            }
        }
        if (hasGuideline) {
            if (I_DEBUG) {
                println("\n#### HORIZONTAL GUIDELINES CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children[i]
                if (child is Guideline) {
                    val guideline: Guideline = child as Guideline
                    if (guideline.orientation == Guideline.HORIZONTAL) {
                        verticalSolvingPass(1, guideline, measurer)
                    }
                }
            }
            if (I_DEBUG) {
                println("\n### Done solving guidelines.")
            }
        }
        if (I_DEBUG) {
            println("\n#### VERTICAL SOLVING PASS ####")
        }

        // ...then solve the vertical dependencies...
        verticalSolvingPass(0, layout, measurer)

        // ...then deal with any barriers left.
        if (hasBarrier) {
            if (I_DEBUG) {
                println("#### VERTICAL BARRIER CHECKS ####")
            }
            for (i in 0 until count) {
                val child: ConstraintWidget = children[i]
                if (child is Barrier) {
                    val barrier: Barrier = child as Barrier
                    if (barrier.orientation == ConstraintWidget.VERTICAL) {
                        solveBarrier(
                            0,
                            barrier,
                            measurer,
                            ConstraintWidget.Companion.VERTICAL,
                            isRtl
                        )
                    }
                }
            }


        }
        if (I_DEBUG) {
            println("\n#### LAST PASS ####")
        }
        // We can do a last pass to see any widget that could still be measured
        for (i in 0 until count) {
            val child: ConstraintWidget = children[i]
            if (child.isMeasureRequested && canMeasure(0, child)) {
                ConstraintWidgetContainer.measure(
                    0, child,
                    measurer, sMeasure, BasicMeasure.Measure.SELF_DIMENSIONS
                )
                if (child is Guideline) {
                    if ((child as Guideline).orientation == Guideline.HORIZONTAL) {
                        verticalSolvingPass(0, child, measurer)
                    } else {
                        horizontalSolvingPass(0, child, measurer, isRtl)
                    }
                } else {
                    horizontalSolvingPass(0, child, measurer, isRtl)
                    verticalSolvingPass(0, child, measurer)
                }
            }
        }
        if (I_DEBUG) {
            time = nanoTime() - time
            println("\n*** THROUGH WITH DIRECT PASS in $time ns ***\n")
            println("hcount: " + sHcount + " vcount: " + sVcount)
        }
    }

    /**
     * Ask the barrier if it's resolved, and if so do a solving pass
     */
    private fun solveBarrier(
        level: Int,
        barrier: Barrier,
        measurer: BasicMeasure.Measurer,
        orientation: Int,
        isRtl: Boolean
    ) {
        if (barrier.allSolved()) {
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                horizontalSolvingPass(level + 1, barrier, measurer, isRtl)
            } else {
                verticalSolvingPass(level + 1, barrier, measurer)
            }
        }
    }

    /**
     * Small utility function to indent logs depending on the level
     *
     * @return a formatted string for the indentation
     */
    fun ls(level: Int): String {
        val builder = StringBuilder()
        for (i in 0 until level) {
            builder.append("  ")
        }
        builder.append("+-($level) ")
        return builder.toString()
    }

    /**
     * Does an horizontal solving pass for the given widget. This will walk through the widget's
     * horizontal dependencies and if they can be resolved directly, do so.
     *
     * @param layout   the widget we want to solve the dependencies
     * @param measurer the measurer object to measure the widgets.
     */
    private fun horizontalSolvingPass(
        level: Int,
        layout: ConstraintWidget,
        measurer: BasicMeasure.Measurer,
        isRtl: Boolean
    ) {
        if (EARLY_TERMINATION && layout.isHorizontalSolvingPassDone) {
            if (I_DEBUG) {
                println(
                    ls(level) + "HORIZONTAL SOLVING PASS ON "
                            + layout.debugName + " ALREADY CALLED"
                )
            }
            return
        }
        sHcount++
        if (I_DEBUG) {
            println(ls(level) + "HORIZONTAL SOLVING PASS ON " + layout.debugName)
        }
        if (layout !is ConstraintWidgetContainer && layout.isMeasureRequested
            && canMeasure(level + 1, layout)
        ) {
            val measure = BasicMeasure.Measure()
            ConstraintWidgetContainer.measure(
                level + 1, layout,
                measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
            )
        }
        val left: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.LEFT)
        val right: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.RIGHT)
        val l: Int = left.finalValue
        val r: Int = right.finalValue
        if (left.dependents != null && left.hasFinalValue()) {
            for (first in left.dependents!!) {
                val widget: ConstraintWidget = first.mOwner
                var x1 = 0
                var x2 = 0
                val canMeasure = canMeasure(level + 1, widget)
                if (widget.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
                    )
                }
                val bothConnected =
                    (first == widget.mLeft && widget.mRight.target != null && widget.mRight.target!!.hasFinalValue()
                            || first == widget.mRight && widget.mLeft.target != null && widget.mLeft.target!!.hasFinalValue())
                if (widget.horizontalDimensionBehaviour
                    != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget.isMeasureRequested) {
                        // Widget needs to be measured
                        if (I_DEBUG) {
                            println(
                                ls(level + 1) + "(L) We didn't measure "
                                        + widget.debugName + ", let's bail"
                            )
                        }
                        continue
                    }
                    if (first == widget.mLeft && widget.mRight.target == null) {
                        x1 = l + widget.mLeft.margin
                        x2 = x1 + widget.width
                        widget.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (first == widget.mRight && widget.mLeft.target == null) {
                        x2 = l - widget.mRight.margin
                        x1 = x2 - widget.width
                        widget.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (bothConnected && !widget.isInHorizontalChain) {
                        solveHorizontalCenterConstraints(level + 1, measurer, widget, isRtl)
                    } else if (APPLY_MATCH_PARENT && widget.horizontalDimensionBehaviour
                        == ConstraintWidget.DimensionBehaviour.MATCH_PARENT
                    ) {
                        widget.setFinalHorizontal(0, widget.width)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    }
                } else if ((widget.horizontalDimensionBehaviour
                            == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) && widget.mMatchConstraintMaxWidth >= 0 && widget.mMatchConstraintMinWidth >= 0 && (widget.visibility == ConstraintWidget.GONE
                            || ((widget.mMatchConstraintDefaultWidth
                            == ConstraintWidget.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget.isInHorizontalChain && !widget.isInVirtualLayout
                ) {
                    if (bothConnected && !widget.isInHorizontalChain) {
                        solveHorizontalMatchConstraint(level + 1, layout, measurer, widget, isRtl)
                    }
                }
            }
        }
        if (layout is Guideline) {
            return
        }
        if (right.dependents != null && right.hasFinalValue()) {
            for (first in right.dependents!!) {
                val widget: ConstraintWidget = first.mOwner
                val canMeasure = canMeasure(level + 1, widget)
                if (widget.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
                    )
                }
                var x1 = 0
                var x2 = 0
                val bothConnected =
                    (first == widget.mLeft && widget.mRight.target != null && widget.mRight.target!!.hasFinalValue()
                            || first == widget.mRight && widget.mLeft.target != null && widget.mLeft.target!!.hasFinalValue())
                if (widget.horizontalDimensionBehaviour
                    != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget.isMeasureRequested) {
                        // Widget needs to be measured
                        if (I_DEBUG) {
                            println(
                                ls(level + 1) + "(R) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first == widget.mLeft && widget.mRight.target == null) {
                        x1 = r + widget.mLeft.margin
                        x2 = x1 + widget.width
                        widget.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (first == widget.mRight && widget.mLeft.target == null) {
                        x2 = r - widget.mRight.margin
                        x1 = x2 - widget.width
                        widget.setFinalHorizontal(x1, x2)
                        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                    } else if (bothConnected && !widget.isInHorizontalChain) {
                        solveHorizontalCenterConstraints(level + 1, measurer, widget, isRtl)
                    }
                } else if ((widget.horizontalDimensionBehaviour
                            == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) && widget.mMatchConstraintMaxWidth >= 0 && widget.mMatchConstraintMinWidth >= 0 && (widget.visibility == ConstraintWidget.GONE
                            || ((widget.mMatchConstraintDefaultWidth
                            == ConstraintWidget.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget.isInHorizontalChain && !widget.isInVirtualLayout
                ) {
                    if (bothConnected && !widget.isInHorizontalChain) {
                        solveHorizontalMatchConstraint(level + 1, layout, measurer, widget, isRtl)
                    }
                }
            }
        }
        layout.markHorizontalSolvingPassDone()
    }

    /**
     * Does an vertical solving pass for the given widget. This will walk through the widget's
     * vertical dependencies and if they can be resolved directly, do so.
     *
     * @param layout   the widget we want to solve the dependencies
     * @param measurer the measurer object to measure the widgets.
     */
    private fun verticalSolvingPass(
        level: Int,
        layout: ConstraintWidget,
        measurer: BasicMeasure.Measurer
    ) {
        if (EARLY_TERMINATION && layout.isVerticalSolvingPassDone) {
            if (I_DEBUG) {
                println(
                    ls(level) + "VERTICAL SOLVING PASS ON "
                            + layout.debugName + " ALREADY CALLED"
                )
            }
            return
        }
        sVcount++
        if (I_DEBUG) {
            println(ls(level) + "VERTICAL SOLVING PASS ON " + layout.debugName)
        }
        if (layout !is ConstraintWidgetContainer
            && layout.isMeasureRequested && canMeasure(level + 1, layout)
        ) {
            val measure = BasicMeasure.Measure()
            ConstraintWidgetContainer.measure(
                level + 1, layout,
                measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
            )
        }
        val top: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.TOP)
        val bottom: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.BOTTOM)
        val t: Int = top.finalValue
        val b: Int = bottom.finalValue
        if (top.dependents != null && top.hasFinalValue()) {
            for (first in top.dependents!!) {
                val widget: ConstraintWidget = first.mOwner
                var y1 = 0
                var y2 = 0
                val canMeasure = canMeasure(level + 1, widget)
                if (widget.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
                    )
                }
                val bothConnected =
                    (first == widget.mTop && widget.mBottom.target != null && widget.mBottom.target!!.hasFinalValue()
                            || first == widget.mBottom && widget.mTop.target != null && widget.mTop.target!!.hasFinalValue())
                if (widget.verticalDimensionBehaviour
                    != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
                    || canMeasure
                ) {
                    if (widget.isMeasureRequested) {
                        // Widget needs to be measured
                        if (I_DEBUG) {
                            println(
                                ls(level + 1) + "(T) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first == widget.mTop && widget.mBottom.target == null) {
                        y1 = t + widget.mTop.margin
                        y2 = y1 + widget.height
                        widget.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (first == widget.mBottom && widget.mTop.target == null) {
                        y2 = t - widget.mBottom.margin
                        y1 = y2 - widget.height
                        widget.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (bothConnected && !widget.isInVerticalChain) {
                        solveVerticalCenterConstraints(level + 1, measurer, widget)
                    } else if (APPLY_MATCH_PARENT && widget.verticalDimensionBehaviour
                        == ConstraintWidget.DimensionBehaviour.MATCH_PARENT
                    ) {
                        widget.setFinalVertical(0, widget.height)
                        verticalSolvingPass(level + 1, widget, measurer)
                    }
                } else if ((widget.verticalDimensionBehaviour
                            == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) && widget.mMatchConstraintMaxHeight >= 0 && widget.mMatchConstraintMinHeight >= 0 && (widget.visibility == ConstraintWidget.GONE
                            || ((widget.mMatchConstraintDefaultHeight
                            == ConstraintWidget.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget.isInVerticalChain && !widget.isInVirtualLayout
                ) {
                    if (bothConnected && !widget.isInVerticalChain) {
                        solveVerticalMatchConstraint(level + 1, layout, measurer, widget)
                    }
                }
            }
        }
        if (layout is Guideline) {
            return
        }
        if (bottom.dependents != null && bottom.hasFinalValue()) {
            for (first in bottom.dependents!!) {
                val widget: ConstraintWidget = first.mOwner
                val canMeasure = canMeasure(level + 1, widget)
                if (widget.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
                    )
                }
                var y1 = 0
                var y2 = 0
                val bothConnected =
                    (first == widget.mTop && widget.mBottom.target != null && widget.mBottom.target!!.hasFinalValue()
                            || first == widget.mBottom && widget.mTop.target != null && widget.mTop.target!!.hasFinalValue())
                if (widget.verticalDimensionBehaviour
                    != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget.isMeasureRequested) {
                        // Widget needs to be measured
                        if (I_DEBUG) {
                            println(
                                ls(level + 1) + "(B) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first == widget.mTop && widget.mBottom.target == null) {
                        y1 = b + widget.mTop.margin
                        y2 = y1 + widget.height
                        widget.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (first == widget.mBottom && widget.mTop.target == null) {
                        y2 = b - widget.mBottom.margin
                        y1 = y2 - widget.height
                        widget.setFinalVertical(y1, y2)
                        verticalSolvingPass(level + 1, widget, measurer)
                    } else if (bothConnected && !widget.isInVerticalChain) {
                        solveVerticalCenterConstraints(level + 1, measurer, widget)
                    }
                } else if ((widget.verticalDimensionBehaviour
                            == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) && widget.mMatchConstraintMaxHeight >= 0 && widget.mMatchConstraintMinHeight >= 0 && (widget.visibility == ConstraintWidget.GONE
                            || ((widget.mMatchConstraintDefaultHeight
                            == ConstraintWidget.MATCH_CONSTRAINT_SPREAD)
                            && widget.dimensionRatio == 0f))
                    && !widget.isInVerticalChain && !widget.isInVirtualLayout
                ) {
                    if (bothConnected && !widget.isInVerticalChain) {
                        solveVerticalMatchConstraint(level + 1, layout, measurer, widget)
                    }
                }
            }
        }
        val baseline: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.BASELINE)
        if (baseline.dependents != null && baseline.hasFinalValue()) {
            val baselineValue: Int = baseline.finalValue
            for (first in baseline.dependents!!) {
                val widget: ConstraintWidget = first.mOwner
                val canMeasure = canMeasure(level + 1, widget)
                if (widget.isMeasureRequested && canMeasure) {
                    val measure = BasicMeasure.Measure()
                    ConstraintWidgetContainer.measure(
                        level + 1, widget,
                        measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
                    )
                }
                if (widget.verticalDimensionBehaviour
                    != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                ) {
                    if (widget.isMeasureRequested) {
                        // Widget needs to be measured
                        if (I_DEBUG) {
                            println(
                                ls(level + 1) + "(B) We didn't measure "
                                        + widget.debugName + ", le'ts bail"
                            )
                        }
                        continue
                    }
                    if (first == widget.mBaseline) {
                        widget.setFinalBaseline(baselineValue + first.margin)
                        verticalSolvingPass(level + 1, widget, measurer)
                    }
                }
            }
        }
        layout.markVerticalSolvingPassDone()
    }

    /**
     * Solve horizontal centering constraints
     */
    private fun solveHorizontalCenterConstraints(
        level: Int,
        measurer: BasicMeasure.Measurer,
        widget: ConstraintWidget,
        isRtl: Boolean
    ) {
        // TODO: Handle match constraints here or before calling this
        var x1: Int
        var x2: Int
        var bias: Float = widget.horizontalBiasPercent
        val start: Int = widget.mLeft.target?.finalValue ?: 0
        val end: Int = widget.mRight.target?.finalValue ?: 0
        var s1: Int = start + widget.mLeft.margin
        var s2: Int = end - widget.mRight.margin
        if (start == end) {
            bias = 0.5f
            s1 = start
            s2 = end
        }
        val width: Int = widget.width
        var distance = s2 - s1 - width
        if (s1 > s2) {
            distance = s1 - s2 - width
        }
        val d1: Int
        d1 = if (distance > 0) {
            (0.5f + bias * distance).toInt()
        } else {
            (bias * distance).toInt()
        }
        x1 = s1 + d1
        x2 = x1 + width
        if (s1 > s2) {
            x1 = s1 + d1
            x2 = x1 - width
        }
        widget.setFinalHorizontal(x1, x2)
        horizontalSolvingPass(level + 1, widget, measurer, isRtl)
    }

    /**
     * Solve vertical centering constraints
     */
    private fun solveVerticalCenterConstraints(
        level: Int,
        measurer: BasicMeasure.Measurer,
        widget: ConstraintWidget
    ) {
        // TODO: Handle match constraints here or before calling this
        var y1: Int
        var y2: Int
        var bias: Float = widget.verticalBiasPercent
        val start: Int = widget.mTop.target?.finalValue ?: 0
        val end: Int = widget.mBottom.target?.finalValue ?: 0
        var s1: Int = start + widget.mTop.margin
        var s2: Int = end - widget.mBottom.margin
        if (start == end) {
            bias = 0.5f
            s1 = start
            s2 = end
        }
        val height: Int = widget.height
        var distance = s2 - s1 - height
        if (s1 > s2) {
            distance = s1 - s2 - height
        }
        val d1: Int
        d1 = if (distance > 0) {
            (0.5f + bias * distance).toInt()
        } else {
            (bias * distance).toInt()
        }
        y1 = s1 + d1
        y2 = y1 + height
        if (s1 > s2) {
            y1 = s1 - d1
            y2 = y1 - height
        }
        widget.setFinalVertical(y1, y2)
        verticalSolvingPass(level + 1, widget, measurer)
    }

    /**
     * Solve horizontal match constraints
     */
    private fun solveHorizontalMatchConstraint(
        level: Int,
        layout: ConstraintWidget?,
        measurer: BasicMeasure.Measurer,
        widget: ConstraintWidget,
        isRtl: Boolean
    ) {
        val x1: Int
        val x2: Int
        val bias: Float = widget.horizontalBiasPercent
        val s1: Int = (widget.mLeft.target?.finalValue ?: 0) + widget.mLeft.margin
        val s2: Int = (widget.mRight.target?.finalValue ?: 0) - widget.mRight.margin
        if (s2 >= s1) {
            var width: Int = widget.width
            if (widget.visibility != ConstraintWidget.GONE) {
                if (widget.mMatchConstraintDefaultWidth
                    == ConstraintWidget.MATCH_CONSTRAINT_PERCENT
                ) {
                    var parentWidth = 0
                    parentWidth = if (layout is ConstraintWidgetContainer) {
                        layout.width
                    } else {
                        layout?.parent?.width ?: 0
                    }
                    width = (0.5f * widget.horizontalBiasPercent * parentWidth).toInt()
                } else if (widget.mMatchConstraintDefaultWidth
                    == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                ) {
                    width = s2 - s1
                }
                width = max(widget.mMatchConstraintMinWidth, width)
                if (widget.mMatchConstraintMaxWidth > 0) {
                    width = min(widget.mMatchConstraintMaxWidth, width)
                }
            }
            val distance = s2 - s1 - width
            val d1 = (0.5f + bias * distance).toInt()
            x1 = s1 + d1
            x2 = x1 + width
            widget.setFinalHorizontal(x1, x2)
            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
        }
    }

    /**
     * Solve vertical match constraints
     */
    private fun solveVerticalMatchConstraint(
        level: Int,
        layout: ConstraintWidget?,
        measurer: BasicMeasure.Measurer,
        widget: ConstraintWidget
    ) {
        val y1: Int
        val y2: Int
        val bias: Float = widget.verticalBiasPercent
        val s1: Int = (widget.mTop.target?.finalValue ?: 0) + widget.mTop.margin
        val s2: Int = (widget.mBottom.target?.finalValue ?: 0) - widget.mBottom.margin
        if (s2 >= s1) {
            var height: Int = widget.height
            if (widget.visibility != ConstraintWidget.GONE) {
                if (widget.mMatchConstraintDefaultHeight
                    == ConstraintWidget.MATCH_CONSTRAINT_PERCENT
                ) {
                    var parentHeight = 0
                    parentHeight = if (layout is ConstraintWidgetContainer) {
                        layout.height
                    } else {
                        layout?.parent?.height ?: 0
                    }
                    height = (0.5f * bias * parentHeight).toInt()
                } else if (widget.mMatchConstraintDefaultHeight
                    == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                ) {
                    height = s2 - s1
                }
                height = max(widget.mMatchConstraintMinHeight, height)
                if (widget.mMatchConstraintMaxHeight > 0) {
                    height = min(widget.mMatchConstraintMaxHeight, height)
                }
            }
            val distance = s2 - s1 - height
            val d1 = (0.5f + bias * distance).toInt()
            y1 = s1 + d1
            y2 = y1 + height
            widget.setFinalVertical(y1, y2)
            verticalSolvingPass(level + 1, widget, measurer)
        }
    }

    /**
     * Returns true if the dimensions of the given widget are computable directly
     *
     * @param layout the widget to check
     * @return true if both dimensions are knowable by a single measure pass
     */
    private fun canMeasure(level: Int, layout: ConstraintWidget): Boolean {
        val horizontalBehaviour: ConstraintWidget.DimensionBehaviour = layout.horizontalDimensionBehaviour
        val verticalBehaviour: ConstraintWidget.DimensionBehaviour = layout.verticalDimensionBehaviour
        val parent: ConstraintWidgetContainer? =
            if (layout.parent != null) layout.parent as ConstraintWidgetContainer else null
        val isParentHorizontalFixed = parent != null && parent.horizontalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.FIXED
        val isParentVerticalFixed = parent != null && parent.verticalDimensionBehaviour == ConstraintWidget.DimensionBehaviour.FIXED
        val isHorizontalFixed =
            (horizontalBehaviour == ConstraintWidget.DimensionBehaviour.FIXED || layout.isResolvedHorizontally
                    || APPLY_MATCH_PARENT && (horizontalBehaviour
                    == ConstraintWidget.DimensionBehaviour.MATCH_PARENT) && isParentHorizontalFixed
                    || horizontalBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT || horizontalBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && (layout.mMatchConstraintDefaultWidth
                    == ConstraintWidget.MATCH_CONSTRAINT_SPREAD) && layout.dimensionRatio == 0f && layout.hasDanglingDimension(
                ConstraintWidget.Companion.HORIZONTAL
            )
                    || horizontalBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && layout.mMatchConstraintDefaultWidth == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP && layout.hasResolvedTargets(
                ConstraintWidget.Companion.HORIZONTAL,
                layout.width
            ))
        val isVerticalFixed =
            (verticalBehaviour == ConstraintWidget.DimensionBehaviour.FIXED || layout.isResolvedVertically
                    || APPLY_MATCH_PARENT && (verticalBehaviour
                    == ConstraintWidget.DimensionBehaviour.MATCH_PARENT) && isParentVerticalFixed
                    || verticalBehaviour == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT || verticalBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && (layout.mMatchConstraintDefaultHeight
                    == ConstraintWidget.MATCH_CONSTRAINT_SPREAD) && layout.dimensionRatio == 0f && layout.hasDanglingDimension(
                ConstraintWidget.VERTICAL
            )
                    || verticalBehaviour == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && layout.mMatchConstraintDefaultHeight == ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP && layout.hasResolvedTargets(
                ConstraintWidget.Companion.VERTICAL,
                layout.height
            ))
        if (layout.dimensionRatio > 0 && (isHorizontalFixed || isVerticalFixed)) {
            return true
        }
        if (I_DEBUG) {
            println(
                ls(level) + "can measure " + layout.debugName + " ? "
                        + (isHorizontalFixed && isVerticalFixed) + "  [ "
                        + isHorizontalFixed + " (horiz " + horizontalBehaviour + ") & "
                        + isVerticalFixed + " (vert " + verticalBehaviour + ") ]"
            )
        }
        return isHorizontalFixed && isVerticalFixed
    }

    /**
     * Try to directly resolve the chain
     *
     * @return true if fully resolved
     */
    fun solveChain(
        container: ConstraintWidgetContainer, system: LinearSystem,
        orientation: Int, offset: Int, chainHead: ChainHead,
        isChainSpread: Boolean, isChainSpreadInside: Boolean,
        isChainPacked: Boolean
    ): Boolean {
        if (LinearSystem.FULL_DEBUG) {
            println("\n### SOLVE CHAIN ###")
        }
        if (isChainPacked) {
            return false
        }
        if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
            if (!container.isResolvedHorizontally) {
                return false
            }
        } else {
            if (!container.isResolvedVertically) {
                return false
            }
        }
        val level = 0 // nested level (used for debugging)
        val isRtl: Boolean = container.isRtl
        val first: ConstraintWidget = chainHead.first
        val last: ConstraintWidget? = chainHead.last
        val firstVisibleWidget: ConstraintWidget? = chainHead.firstVisibleWidget
        val lastVisibleWidget: ConstraintWidget? = chainHead.lastVisibleWidget
        val head: ConstraintWidget? = chainHead.head
        var widget: ConstraintWidget = first
        var next: ConstraintWidget?
        var done = false
        val begin: ConstraintAnchor = first.mListAnchors.get(offset)
        val end: ConstraintAnchor? = last?.mListAnchors?.get(offset + 1)
        if (begin.target == null || end?.target == null) {
            return false
        }
        if (!begin.target!!.hasFinalValue() || !end.target!!.hasFinalValue()) {
            return false
        }
        if (firstVisibleWidget == null || lastVisibleWidget == null) {
            return false
        }
        val startPoint: Int = (begin.target!!.finalValue
                + firstVisibleWidget.mListAnchors.get(offset).margin)
        val endPoint: Int = (end.target!!.finalValue
                - lastVisibleWidget.mListAnchors.get(offset + 1).margin)
        val distance = endPoint - startPoint
        if (distance <= 0) {
            return false
        }
        var totalSize = 0
        val measure = BasicMeasure.Measure()
        var numWidgets = 0
        var numVisibleWidgets = 0
        while (!done) {
            val canMeasure = canMeasure(level + 1, widget)
            if (!canMeasure) {
                return false
            }
            if (widget.mListDimensionBehaviors.get(orientation)
                == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
            ) {
                return false
            }
            if (widget.isMeasureRequested) {
                ConstraintWidgetContainer.measure(
                    level + 1, widget,
                    container.measurer, measure, BasicMeasure.Measure.SELF_DIMENSIONS
                )
            }
            totalSize += widget.mListAnchors.get(offset).margin
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                totalSize += +widget.width
            } else {
                totalSize += widget.height
            }
            totalSize += widget.mListAnchors.get(offset + 1).margin
            numWidgets++
            if (widget.visibility != ConstraintWidget.GONE) {
                numVisibleWidgets++
            }


            // go to the next widget
            val nextAnchor: ConstraintAnchor? = widget.mListAnchors.get(offset + 1).target
            if (nextAnchor != null) {
                next = nextAnchor.mOwner
                if (next.mListAnchors.get(offset).target == null
                    || next.mListAnchors.get(offset).target!!.mOwner != widget
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
        if (numVisibleWidgets == 0) {
            return false
        }
        if (numVisibleWidgets != numWidgets) {
            return false
        }
        if (distance < totalSize) {
            return false
        }
        var gap = distance - totalSize
        if (isChainSpread) {
            gap = gap / (numVisibleWidgets + 1)
        } else if (isChainSpreadInside) {
            if (numVisibleWidgets > 2) {
                gap = gap / numVisibleWidgets - 1
            }
        }
        if (numVisibleWidgets == 1) {
            val bias: Float
            bias = if (orientation == ConstraintWidget.HORIZONTAL) {
                head?.horizontalBiasPercent ?: 0f
            } else {
                head?.verticalBiasPercent ?: 0f
            }
            val p1 = (0.5f + startPoint + gap * bias).toInt()
            if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                firstVisibleWidget.setFinalHorizontal(p1, p1 + firstVisibleWidget.width)
            } else {
                firstVisibleWidget.setFinalVertical(p1, p1 + firstVisibleWidget.height)
            }
            horizontalSolvingPass(
                level + 1,
                firstVisibleWidget, container.measurer, isRtl
            )
            return true
        }
        if (isChainSpread) {
            done = false
            var current = startPoint + gap
            widget = first
            while (!done) {
                if (widget.visibility == ConstraintWidget.Companion.GONE) {
                    if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        widget.setFinalHorizontal(current, current)
                        horizontalSolvingPass(
                            level + 1,
                            widget, container.measurer, isRtl
                        )
                    } else {
                        widget.setFinalVertical(current, current)
                        verticalSolvingPass(level + 1, widget, container.measurer)
                    }
                } else {
                    current += widget.mListAnchors.get(offset).margin
                    current += if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        widget.setFinalHorizontal(current, current + widget.width)
                        horizontalSolvingPass(
                            level + 1,
                            widget, container.measurer, isRtl
                        )
                        widget.width
                    } else {
                        widget.setFinalVertical(current, current + widget.height)
                        verticalSolvingPass(
                            level + 1,
                            widget,
                            container.measurer
                        )
                        widget.height
                    }
                    current += widget.mListAnchors.get(offset + 1).margin
                    current += gap
                }
                widget.addToSolver(system, false)

                // go to the next widget
                val nextAnchor: ConstraintAnchor? = widget.mListAnchors.get(offset + 1).target
                if (nextAnchor != null) {
                    next = nextAnchor.mOwner
                    if (next.mListAnchors.get(offset).target == null
                        || next.mListAnchors.get(offset).target!!.mOwner != widget
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
        } else if (isChainSpreadInside) {
            if (numVisibleWidgets == 2) {
                if (orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    firstVisibleWidget.setFinalHorizontal(
                        startPoint,
                        startPoint + firstVisibleWidget.width
                    )
                    lastVisibleWidget.setFinalHorizontal(
                        endPoint - lastVisibleWidget.width,
                        endPoint
                    )
                    horizontalSolvingPass(
                        level + 1,
                        firstVisibleWidget, container.measurer, isRtl
                    )
                    horizontalSolvingPass(
                        level + 1,
                        lastVisibleWidget, container.measurer, isRtl
                    )
                } else {
                    firstVisibleWidget.setFinalVertical(
                        startPoint,
                        startPoint + firstVisibleWidget.height
                    )
                    lastVisibleWidget.setFinalVertical(
                        endPoint - lastVisibleWidget.height,
                        endPoint
                    )
                    verticalSolvingPass(
                        level + 1,
                        firstVisibleWidget, container.measurer
                    )
                    verticalSolvingPass(
                        level + 1,
                        lastVisibleWidget, container.measurer
                    )
                }
                return true
            }
            return false
        }
        return true
    }
}
