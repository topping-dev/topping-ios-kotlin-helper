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

import dev.topping.ios.constraint.core.widgets.*

/**
 * Implements a simple grouping mechanism, to group interdependent widgets together.
 *
 * TODO: we should move towards a more leaner implementation
 * -- this is more expensive as it could be.
 */
object Grouping {
    private const val I_DEBUG = false
    private const val DEBUG_GROUPING = false
    private const val FORCE_USE = true

    // @TODO: add description
    fun validInGroup(
        layoutHorizontal: ConstraintWidget.DimensionBehaviour,
        layoutVertical: ConstraintWidget.DimensionBehaviour,
        widgetHorizontal: ConstraintWidget.DimensionBehaviour,
        widgetVertical: ConstraintWidget.DimensionBehaviour
    ): Boolean {
        val fixedHorizontal =
            widgetHorizontal == ConstraintWidget.DimensionBehaviour.FIXED || widgetHorizontal == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT || widgetHorizontal == ConstraintWidget.DimensionBehaviour.MATCH_PARENT && layoutHorizontal != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
        val fixedVertical =
            widgetVertical == ConstraintWidget.DimensionBehaviour.FIXED || widgetVertical == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT || widgetVertical == ConstraintWidget.DimensionBehaviour.MATCH_PARENT && layoutVertical != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
        return if (fixedHorizontal || fixedVertical) {
            true
        } else false
    }

    // @TODO: add description
    fun simpleSolvingPass(
        layout: ConstraintWidgetContainer,
        measurer: BasicMeasure.Measurer?
    ): Boolean {
        if (I_DEBUG) {
            println("*** GROUP SOLVING ***")
        }
        val children: MutableList<ConstraintWidget> = layout.children
        val count: Int = children?.size ?: 0
        var verticalGuidelines: ArrayList<Guideline>? = null
        var horizontalGuidelines: ArrayList<Guideline>? = null
        var horizontalBarriers: ArrayList<HelperWidget>? = null
        var verticalBarriers: ArrayList<HelperWidget>? = null
        var isolatedHorizontalChildren: ArrayList<ConstraintWidget>? = null
        var isolatedVerticalChildren: ArrayList<ConstraintWidget>? = null
        for (i in 0 until count) {
            val child: ConstraintWidget = children!![i]
            if (!validInGroup(
                    layout.horizontalDimensionBehaviour,
                    layout.verticalDimensionBehaviour,
                    child.horizontalDimensionBehaviour,
                    child.verticalDimensionBehaviour
                )
            ) {
                if (I_DEBUG) {
                    println("*** NO GROUP SOLVING ***")
                }
                return false
            }
            if (child is Flow) {
                return false
            }
        }
        if (layout.mMetrics != null) {
            layout.mMetrics!!.grouping++
        }
        for (i in 0 until count) {
            val child: ConstraintWidget = children!![i]
            if (!validInGroup(
                    layout.horizontalDimensionBehaviour,
                    layout.verticalDimensionBehaviour,
                    child.horizontalDimensionBehaviour,
                    child.verticalDimensionBehaviour
                )
            ) {
                ConstraintWidgetContainer.measure(
                    0, child, measurer,
                    layout.mMeasure, BasicMeasure.Measure.SELF_DIMENSIONS
                )
            }
            if (child is Guideline) {
                val guideline: Guideline = child as Guideline
                if (guideline.orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    if (horizontalGuidelines == null) {
                        horizontalGuidelines = ArrayList()
                    }
                    horizontalGuidelines!!.add(guideline)
                }
                if (guideline.orientation == ConstraintWidget.Companion.VERTICAL) {
                    if (verticalGuidelines == null) {
                        verticalGuidelines = ArrayList()
                    }
                    verticalGuidelines!!.add(guideline)
                }
            }
            if (child is HelperWidget) {
                if (child is Barrier) {
                    val barrier: Barrier = child as Barrier
                    if (barrier.orientation == ConstraintWidget.Companion.HORIZONTAL) {
                        if (horizontalBarriers == null) {
                            horizontalBarriers = ArrayList()
                        }
                        horizontalBarriers!!.add(barrier)
                    }
                    if (barrier.orientation == ConstraintWidget.Companion.VERTICAL) {
                        if (verticalBarriers == null) {
                            verticalBarriers = ArrayList()
                        }
                        verticalBarriers!!.add(barrier)
                    }
                } else {
                    val helper: HelperWidget = child as HelperWidget
                    if (horizontalBarriers == null) {
                        horizontalBarriers = ArrayList()
                    }
                    horizontalBarriers!!.add(helper)
                    if (verticalBarriers == null) {
                        verticalBarriers = ArrayList()
                    }
                    verticalBarriers!!.add(helper)
                }
            }
            if (child.mLeft.target == null && child.mRight.target == null && child !is Guideline && child !is Barrier) {
                if (isolatedHorizontalChildren == null) {
                    isolatedHorizontalChildren = ArrayList()
                }
                isolatedHorizontalChildren!!.add(child)
            }
            if (child.mTop.target == null && child.mBottom.target == null && child.mBaseline.target == null && child !is Guideline && child !is Barrier) {
                if (isolatedVerticalChildren == null) {
                    isolatedVerticalChildren = ArrayList()
                }
                isolatedVerticalChildren!!.add(child)
            }
        }
        val allDependencyLists: ArrayList<WidgetGroup> = ArrayList()
        if (FORCE_USE || layout.horizontalDimensionBehaviour
            == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
        ) {
            //horizontalDependencyLists; //new ArrayList<>();
            val dependencyLists: ArrayList<WidgetGroup> = allDependencyLists
            if (verticalGuidelines != null) {
                for (guideline in verticalGuidelines) {
                    findDependents(
                        guideline,
                        ConstraintWidget.Companion.HORIZONTAL,
                        dependencyLists,
                        null
                    )
                }
            }
            if (horizontalBarriers != null) {
                for (barrier in horizontalBarriers) {
                    val group: WidgetGroup = findDependents(
                        barrier,
                        ConstraintWidget.Companion.HORIZONTAL,
                        dependencyLists,
                        null
                    )
                    barrier.addDependents(
                        dependencyLists,
                        ConstraintWidget.Companion.HORIZONTAL,
                        group
                    )
                    group.cleanup(dependencyLists)
                }
            }
            val left: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.LEFT)
            if (left.dependents != null) {
                for (first in left.dependents!!) {
                    findDependents(
                        first.mOwner, ConstraintWidget.HORIZONTAL,
                        dependencyLists, null
                    )
                }
            }
            val right: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.RIGHT)
            if (right.dependents != null) {
                for (first in right.dependents!!) {
                    findDependents(
                        first.mOwner, ConstraintWidget.HORIZONTAL,
                        dependencyLists, null
                    )
                }
            }
            val center: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.CENTER)
            if (center.dependents != null) {
                for (first in center.dependents!!) {
                    findDependents(
                        first.mOwner, ConstraintWidget.HORIZONTAL,
                        dependencyLists, null
                    )
                }
            }
            if (isolatedHorizontalChildren != null) {
                for (widget in isolatedHorizontalChildren) {
                    findDependents(
                        widget,
                        ConstraintWidget.Companion.HORIZONTAL,
                        dependencyLists,
                        null
                    )
                }
            }
        }
        if (FORCE_USE || layout.verticalDimensionBehaviour
            == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
        ) {
            //verticalDependencyLists; //new ArrayList<>();
            val dependencyLists: ArrayList<WidgetGroup> = allDependencyLists
            if (horizontalGuidelines != null) {
                for (guideline in horizontalGuidelines) {
                    findDependents(
                        guideline,
                        ConstraintWidget.Companion.VERTICAL,
                        dependencyLists,
                        null
                    )
                }
            }
            if (verticalBarriers != null) {
                for (barrier in verticalBarriers) {
                    val group: WidgetGroup = findDependents(
                        barrier,
                        ConstraintWidget.Companion.VERTICAL,
                        dependencyLists,
                        null
                    )
                    barrier.addDependents(
                        dependencyLists,
                        ConstraintWidget.Companion.VERTICAL,
                        group
                    )
                    group.cleanup(dependencyLists)
                }
            }
            val top: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.TOP)
            if (top.dependents != null) {
                for (first in top.dependents!!) {
                    findDependents(
                        first.mOwner,
                        ConstraintWidget.Companion.VERTICAL,
                        dependencyLists,
                        null
                    )
                }
            }
            val baseline: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.BASELINE)
            if (baseline.dependents != null) {
                for (first in baseline.dependents!!) {
                    findDependents(
                        first.mOwner,
                        ConstraintWidget.Companion.VERTICAL,
                        dependencyLists,
                        null
                    )
                }
            }
            val bottom: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.BOTTOM)
            if (bottom.dependents != null) {
                for (first in bottom.dependents!!) {
                    findDependents(
                        first.mOwner,
                        ConstraintWidget.Companion.VERTICAL,
                        dependencyLists,
                        null
                    )
                }
            }
            val center: ConstraintAnchor = layout.getAnchor(ConstraintAnchor.Type.CENTER)
            if (center.dependents != null) {
                for (first in center.dependents!!) {
                    findDependents(
                        first.mOwner,
                        ConstraintWidget.Companion.VERTICAL,
                        dependencyLists,
                        null
                    )
                }
            }
            if (isolatedVerticalChildren != null) {
                for (widget in isolatedVerticalChildren) {
                    findDependents(
                        widget,
                        ConstraintWidget.Companion.VERTICAL,
                        dependencyLists,
                        null
                    )
                }
            }
        }
        // Now we may have to merge horizontal/vertical dependencies
        for (i in 0 until count) {
            val child: ConstraintWidget = children[i]
            if (child.oppositeDimensionsTied()) {
                val horizontalGroup: WidgetGroup? =
                    findGroup(allDependencyLists, child.horizontalGroup)
                val verticalGroup: WidgetGroup? = findGroup(allDependencyLists, child.verticalGroup)
                if (horizontalGroup != null && verticalGroup != null) {
                    if (DEBUG_GROUPING) {
                        println(
                            "Merging " + horizontalGroup
                                    + " to " + verticalGroup + " for " + child
                        )
                    }
                    horizontalGroup.moveTo(ConstraintWidget.Companion.HORIZONTAL, verticalGroup)
                    verticalGroup.orientation = ConstraintWidget.Companion.BOTH
                    allDependencyLists.remove(horizontalGroup)
                }
            }
            if (DEBUG_GROUPING) {
                println(
                    "Widget " + child + " => "
                            + child.horizontalGroup + " : " + child.verticalGroup
                )
            }
        }
        if (allDependencyLists.size <= 1) {
            return false
        }
        if (I_DEBUG) {
            println("----------------------------------")
            println("-- Horizontal dependency lists:")
            println("----------------------------------")
            for (list in allDependencyLists) {
                if (list.orientation != ConstraintWidget.Companion.VERTICAL) {
                    println("list: $list")
                }
            }
            println("----------------------------------")
            println("-- Vertical dependency lists:")
            println("----------------------------------")
            for (list in allDependencyLists) {
                if (list.orientation != ConstraintWidget.Companion.HORIZONTAL) {
                    println("list: $list")
                }
            }
            println("----------------------------------")
        }
        var horizontalPick: WidgetGroup? = null
        var verticalPick: WidgetGroup? = null
        if (layout.horizontalDimensionBehaviour
            == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
        ) {
            var maxWrap = 0
            var picked: WidgetGroup? = null
            for (list in allDependencyLists) {
                if (list.orientation == ConstraintWidget.Companion.VERTICAL) {
                    continue
                }
                list.isAuthoritative = false
                val wrap: Int =
                    list.measureWrap(layout.system, ConstraintWidget.Companion.HORIZONTAL)
                if (wrap > maxWrap) {
                    picked = list
                    maxWrap = wrap
                }
                if (I_DEBUG) {
                    println("list: $list => $wrap")
                }
            }
            if (picked != null) {
                if (I_DEBUG) {
                    println("Horizontal MaxWrap : $maxWrap with group $picked")
                }
                layout.horizontalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED
                layout.width = maxWrap
                picked.isAuthoritative = true
                horizontalPick = picked
            }
        }
        if (layout.verticalDimensionBehaviour
            == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
        ) {
            var maxWrap = 0
            var picked: WidgetGroup? = null
            for (list in allDependencyLists) {
                if (list.orientation == ConstraintWidget.Companion.HORIZONTAL) {
                    continue
                }
                list.isAuthoritative = false
                val wrap: Int = list.measureWrap(layout.system, ConstraintWidget.Companion.VERTICAL)
                if (wrap > maxWrap) {
                    picked = list
                    maxWrap = wrap
                }
                if (I_DEBUG) {
                    println("      $list => $wrap")
                }
            }
            if (picked != null) {
                if (I_DEBUG) {
                    println("Vertical MaxWrap : $maxWrap with group $picked")
                }
                layout.verticalDimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED
                layout.height = maxWrap
                picked.isAuthoritative = true
                verticalPick = picked
            }
        }
        return horizontalPick != null || verticalPick != null
    }

    private fun findGroup(
        horizontalDependencyLists: ArrayList<WidgetGroup>,
        groupId: Int
    ): WidgetGroup? {
        val count: Int = horizontalDependencyLists.size
        for (i in 0 until count) {
            val group: WidgetGroup = horizontalDependencyLists[i]
            if (groupId == group.id) {
                return group
            }
        }
        return null
    }

    // @TODO: add description
    fun findDependents(
        constraintWidget: ConstraintWidget,
        orientation: Int,
        list: MutableList<WidgetGroup>,
        group: WidgetGroup?
    ): WidgetGroup {
        var group: WidgetGroup? = group
        var groupId = -1
        groupId = if (orientation == ConstraintWidget.HORIZONTAL) {
            constraintWidget.horizontalGroup
        } else {
            constraintWidget.verticalGroup
        }
        if (DEBUG_GROUPING) {
            println(
                "--- find " + (if (orientation == ConstraintWidget.Companion.HORIZONTAL) "Horiz" else "Vert")
                        + " dependents of " + constraintWidget.debugName
                        + " group " + group + " widget group id " + groupId
            )
        }
        if (groupId != -1 && (group == null || groupId != group.id)) {
            // already in a group!
            if (DEBUG_GROUPING) {
                println(
                    "widget " + constraintWidget.debugName
                            + " already in group " + groupId + " group: " + group
                )
            }
            for (i in 0 until list.size) {
                val widgetGroup: WidgetGroup = list[i]
                if (widgetGroup.id == groupId) {
                    if (group != null) {
                        if (DEBUG_GROUPING) {
                            println("Move group $group to $widgetGroup")
                        }
                        group.moveTo(orientation, widgetGroup)
                        list.remove(group)
                    }
                    group = widgetGroup
                    break
                }
            }
        } else if (groupId != -1) {
            return group!!
        }
        if (group == null) {
            if (constraintWidget is HelperWidget) {
                val helper: HelperWidget = constraintWidget as HelperWidget
                groupId = helper.findGroupInDependents(orientation)
                if (groupId != -1) {
                    for (i in 0 until list.size) {
                        val widgetGroup: WidgetGroup = list[i]
                        if (widgetGroup.id == groupId) {
                            group = widgetGroup
                            break
                        }
                    }
                }
            }
            if (group == null) {
                group = WidgetGroup(orientation)
            }
            if (DEBUG_GROUPING) {
                println(
                    "Create group " + group
                            + " for widget " + constraintWidget.debugName
                )
            }
            list.add(group)
        }
        if (group.add(constraintWidget)) {
            if (constraintWidget is Guideline) {
                val guideline: Guideline = constraintWidget
                guideline.anchor.findDependents(
                    if (guideline.orientation
                        == Guideline.HORIZONTAL
                    ) ConstraintWidget.VERTICAL else ConstraintWidget.HORIZONTAL,
                    list,
                    group
                )
            }
            if (orientation == ConstraintWidget.HORIZONTAL) {
                constraintWidget.horizontalGroup = group.id
                if (DEBUG_GROUPING) {
                    println(
                        "Widget " + constraintWidget.debugName
                                + " H group is " + constraintWidget.horizontalGroup
                    )
                }
                constraintWidget.mLeft.findDependents(orientation, list, group)
                constraintWidget.mRight.findDependents(orientation, list, group)
            } else {
                constraintWidget.verticalGroup = group.id
                if (DEBUG_GROUPING) {
                    println(
                        "Widget " + constraintWidget.debugName
                                + " V group is " + constraintWidget.verticalGroup
                    )
                }
                constraintWidget.mTop.findDependents(orientation, list, group)
                constraintWidget.mBaseline.findDependents(orientation, list, group)
                constraintWidget.mBottom.findDependents(orientation, list, group)
            }
            constraintWidget.mCenter.findDependents(orientation, list, group)
        }
        return group
    }
}