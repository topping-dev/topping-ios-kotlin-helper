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

import dev.topping.ios.constraint.core.Cache
import dev.topping.ios.constraint.core.SolverVariable
import dev.topping.ios.constraint.core.widgets.analyzer.Grouping
import dev.topping.ios.constraint.core.widgets.analyzer.WidgetGroup

/**
 * Model a constraint relation. Widgets contains anchors, and a constraint relation between
 * two widgets is made by connecting one anchor to another. The anchor will contains a pointer
 * to the target anchor if it is connected.
 */
class ConstraintAnchor(owner: ConstraintWidget, type: Type) {
    var dependents: MutableSet<ConstraintAnchor>? = null
        private set
    private var mFinalValue = 0
    private var mHasFinalValue = false

    // @TODO: add description
    fun findDependents(orientation: Int, list: MutableList<WidgetGroup>, group: WidgetGroup?) {
        if (dependents != null) {
            for (anchor in dependents!!) {
                Grouping.findDependents(anchor.mOwner, orientation, list, group)
            }
        }
    }

    // @TODO: add description
    fun hasDependents(): Boolean {
        return if (dependents == null) {
            false
        } else dependents!!.size > 0
    }

    // @TODO: add description
    fun hasCenteredDependents(): Boolean {
        if (dependents == null) {
            return false
        }
        for (anchor in dependents!!) {
            val opposite = anchor.opposite
            if (opposite!!.isConnected) {
                return true
            }
        }
        return false
    }

    // @TODO: add description
    // @TODO: add description
    var finalValue: Int
        get() = if (!mHasFinalValue) {
            0
        } else mFinalValue
        set(finalValue) {
            mFinalValue = finalValue
            mHasFinalValue = true
        }

    // @TODO: add description
    fun resetFinalResolution() {
        mHasFinalValue = false
        mFinalValue = 0
    }

    // @TODO: add description
    fun hasFinalValue(): Boolean {
        return mHasFinalValue
    }

    /**
     * Define the type of anchor
     */
    enum class Type {
        NONE, LEFT, TOP, RIGHT, BOTTOM, BASELINE, CENTER, CENTER_X, CENTER_Y
    }

    val mOwner: ConstraintWidget

    /**
     * Return the type of the anchor
     *
     * @return type of the anchor.
     */
    val type: Type

    /**
     * Return the connection's target (null if not connected)
     *
     * @return the ConstraintAnchor target
     */
    var target: ConstraintAnchor? = null
    var mMargin = 0
    var mGoneMargin = UNSET_GONE_MARGIN
    var mSolverVariable: SolverVariable? = null

    // @TODO: add description
    fun copyFrom(source: ConstraintAnchor, map: HashMap<ConstraintWidget?, ConstraintWidget?>) {
        if (target != null) {
            if (target!!.dependents != null) {
                target!!.dependents!!.remove(this)
            }
        }
        if (source.target != null) {
            val type = source.target!!.type
            val owner: ConstraintWidget? = map[source.target!!.mOwner]
            target = owner!!.getAnchor(type)
        } else {
            target = null
        }
        if (target != null) {
            if (target!!.dependents == null) {
                target!!.dependents = HashSet()
            }
            target!!.dependents!!.add(this)
        }
        mMargin = source.mMargin
        mGoneMargin = source.mGoneMargin
    }

    /**
     * Constructor
     *
     * @param owner the widget owner of this anchor.
     * @param type  the anchor type.
     */
    init {
        mOwner = owner
        this.type = type
    }

    /**
     * Return the solver variable for this anchor
     */
    val solverVariable: SolverVariable?
        get() = mSolverVariable

    /**
     * Reset the solver variable
     */
    fun resetSolverVariable(cache: Cache?) {
        if (mSolverVariable == null) {
            mSolverVariable = SolverVariable(SolverVariable.Type.UNRESTRICTED, null)
        } else {
            mSolverVariable?.reset()
        }
    }

    /**
     * Return the anchor's owner
     *
     * @return the Widget owning the anchor
     */
    val owner: dev.topping.ios.constraint.core.widgets.ConstraintWidget
        get() = mOwner
    /**
     * Return the connection's margin from this anchor to its target.
     *
     * @return the margin value. 0 if not connected.
     */
    /**
     * Set the margin of the connection (if there's one)
     *
     * @param margin the new margin of the connection
     */
    var margin: Int
        get() {
            if (mOwner.visibility == ConstraintWidget.GONE) {
                return 0
            }
            return if (mGoneMargin != UNSET_GONE_MARGIN && target != null && target!!.mOwner.visibility == ConstraintWidget.GONE) {
                mGoneMargin
            } else mMargin
        }
        set(margin) {
            if (isConnected) {
                mMargin = margin
            }
        }

    /**
     * Resets the anchor's connection.
     */
    fun reset() {
        if (target != null && target!!.dependents != null) {
            target!!.dependents!!.remove(this)
            if (target!!.dependents!!.size == 0) {
                target!!.dependents = null
            }
        }
        dependents = null
        target = null
        mMargin = 0
        mGoneMargin = UNSET_GONE_MARGIN
        mHasFinalValue = false
        mFinalValue = 0
    }
    /**
     * Connects this anchor to another one.
     *
     * @return true if the connection succeeds.
     */
    /**
     * Connects this anchor to another one.
     *
     * @return true if the connection succeeds.
     */
    fun connect(
        toAnchor: ConstraintAnchor?, margin: Int, goneMargin: Int = UNSET_GONE_MARGIN,
        forceConnection: Boolean = false
    ): Boolean {
        if (toAnchor == null) {
            reset()
            return true
        }
        if (!forceConnection && !isValidConnection(toAnchor)) {
            return false
        }
        target = toAnchor
        if (target!!.dependents == null) {
            target!!.dependents = HashSet()
        }
        if (target!!.dependents != null) {
            target!!.dependents!!.add(this)
        }
        mMargin = margin
        mGoneMargin = goneMargin
        return true
    }

    /**
     * Returns the connection status of this anchor
     *
     * @return true if the anchor is connected to another one.
     */
    val isConnected: Boolean
        get() = target != null

    /**
     * Checks if the connection to a given anchor is valid.
     *
     * @param anchor the anchor we want to connect to
     * @return true if it's a compatible anchor
     */
    fun isValidConnection(anchor: ConstraintAnchor?): Boolean {
        if (anchor == null) {
            return false
        }
        val target = anchor.type
        return if (target == type) {
            if (type == Type.BASELINE
                && (!anchor.owner.hasBaseline() || !owner.hasBaseline())
            ) {
                false
            } else true
        } else when (type) {
            Type.CENTER -> {

                // allow everything but baseline and center_x/center_y
                target != Type.BASELINE && target != Type.CENTER_X && target != Type.CENTER_Y
            }
            Type.LEFT, Type.RIGHT -> {
                var isCompatible =
                    target == Type.LEFT || target == Type.RIGHT
                if (anchor.owner is Guideline) {
                    isCompatible =
                        isCompatible || target == Type.CENTER_X
                }
                isCompatible
            }
            Type.TOP, Type.BOTTOM -> {
                var isCompatible =
                    target == Type.TOP || target == Type.BOTTOM
                if (anchor.owner is Guideline) {
                    isCompatible =
                        isCompatible || target == Type.CENTER_Y
                }
                isCompatible
            }
            Type.BASELINE -> {
                if (target == Type.LEFT || target == Type.RIGHT) {
                    false
                } else true
            }
            Type.CENTER_X, Type.CENTER_Y, Type.NONE -> false
        }
    }

    /**
     * Return true if this anchor is a side anchor
     *
     * @return true if side anchor
     */
    val isSideAnchor: Boolean
        get() {
            return when (type) {
                Type.LEFT, Type.RIGHT, Type.TOP, Type.BOTTOM -> true
                Type.BASELINE, Type.CENTER, Type.CENTER_X, Type.CENTER_Y, Type.NONE -> false
            }
        }

    /**
     * Return true if the connection to the given anchor is in the
     * same dimension (horizontal or vertical)
     *
     * @param anchor the anchor we want to connect to
     * @return true if it's an anchor on the same dimension
     */
    fun isSimilarDimensionConnection(anchor: ConstraintAnchor): Boolean {
        val target = anchor.type
        return if (target == type) {
            true
        } else when (type) {
            Type.CENTER -> {
                target != Type.BASELINE
            }
            Type.LEFT, Type.RIGHT, Type.CENTER_X -> {
                target == Type.LEFT || target == Type.RIGHT || target == Type.CENTER_X
            }
            Type.TOP, Type.BOTTOM, Type.CENTER_Y, Type.BASELINE -> {
                target == Type.TOP || target == Type.BOTTOM || target == Type.CENTER_Y || target == Type.BASELINE
            }
            Type.NONE -> false
        }
    }

    /**
     * Set the gone margin of the connection (if there's one)
     *
     * @param margin the new margin of the connection
     */
    fun setGoneMargin(margin: Int) {
        if (isConnected) {
            mGoneMargin = margin
        }
    }

    /**
     * Utility function returning true if this anchor is a vertical one.
     *
     * @return true if vertical anchor, false otherwise
     */
    val isVerticalAnchor: Boolean
        get() {
            return when (type) {
                Type.LEFT, Type.RIGHT, Type.CENTER, Type.CENTER_X -> false
                Type.CENTER_Y, Type.TOP, Type.BOTTOM, Type.BASELINE, Type.NONE -> true
            }
        }

    /**
     * Return a string representation of this anchor
     *
     * @return string representation of the anchor
     */
    
    override fun toString(): String {
        return mOwner.debugName + ":" + type.toString()
    }

    /**
     * Return true if we can connect this anchor to this target.
     * We recursively follow connections in order to detect eventual cycles; if we
     * do we disallow the connection.
     * We also only allow connections to direct parent, siblings, and descendants.
     *
     * @param target the ConstraintWidget we are trying to connect to
     * @param anchor Allow anchor if it loops back to me directly
     * @return if the connection is allowed, false otherwise
     */
    fun isConnectionAllowed(target: ConstraintWidget, anchor: ConstraintAnchor?): Boolean {
        if (ALLOW_BINARY) {
            if (anchor != null && anchor.target == this) {
                return true
            }
        }
        return isConnectionAllowed(target)
    }

    /**
     * Return true if we can connect this anchor to this target.
     * We recursively follow connections in order to detect eventual cycles; if we
     * do we disallow the connection.
     * We also only allow connections to direct parent, siblings, and descendants.
     *
     * @param target the ConstraintWidget we are trying to connect to
     * @return true if the connection is allowed, false otherwise
     */
    fun isConnectionAllowed(target: ConstraintWidget): Boolean {
        val checked: HashSet<ConstraintWidget> = HashSet()
        if (isConnectionToMe(target, checked)) {
            return false
        }
        val parent: ConstraintWidget? = owner.parent
        if (parent == target) { // allow connections to parent
            return true
        }
        return if (target.parent == parent) { // allow if we share the same parent
            true
        } else false
    }

    /**
     * Recursive with check for loop
     *
     * @param checked set of things already checked
     * @return true if it is connected to me
     */
    private fun isConnectionToMe(
        target: ConstraintWidget,
        checked: HashSet<ConstraintWidget>
    ): Boolean {
        if (checked.contains(target)) {
            return false
        }
        checked.add(target)
        if (target == owner) {
            return true
        }
        val targetAnchors: ArrayList<ConstraintAnchor?> = target.anchors
        var i = 0
        val targetAnchorsSize: Int = targetAnchors.size
        while (i < targetAnchorsSize) {
            val anchor = targetAnchors[i]
            if (anchor!!.isSimilarDimensionConnection(this) && anchor.isConnected) {
                if (isConnectionToMe(anchor.target!!.owner, checked)) {
                    return true
                }
            }
            i++
        }
        return false
    }

    /**
     * Returns the opposite anchor to this one
     *
     * @return opposite anchor
     */
    val opposite: ConstraintAnchor?
        get() {
            return when (type) {
                Type.LEFT -> {
                    mOwner.mRight
                }
                Type.RIGHT -> {
                    mOwner.mLeft
                }
                Type.TOP -> {
                    mOwner.mBottom
                }
                Type.BOTTOM -> {
                    mOwner.mTop
                }
                Type.BASELINE, Type.CENTER, Type.CENTER_X, Type.CENTER_Y, Type.NONE -> null
            }
        }

    companion object {
        private const val ALLOW_BINARY = false
        private val UNSET_GONE_MARGIN: Int = Int.MIN_VALUE
    }
}