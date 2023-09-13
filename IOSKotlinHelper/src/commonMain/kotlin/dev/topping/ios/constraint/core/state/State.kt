/*
 * Copyright (C) 2019 The Android Open Source Project
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
package dev.topping.ios.constraint.core.state

import dev.topping.ios.constraint.core.state.helpers.*
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.ConstraintWidgetContainer
import dev.topping.ios.constraint.core.widgets.HelperWidget

import kotlin.math.*

/**
 * Represents a full state of a ConstraintLayout
 */
class State {
    private var mDpToPixel: CorePixelDp? = null
    /**
     * Returns true if layout direction is left to right. False for right to left.
     */
    /**
     * Set whether the layout direction is left to right (Ltr).
     */
    var isLtr = true
    protected var mReferences: HashMap<Any?, Reference?> = HashMap()
    protected var mHelperReferences: HashMap<Any?, HelperReference?> = HashMap()
    var mTags: HashMap<String, ArrayList<String?>> = HashMap()
    val mParent: ConstraintReference = ConstraintReference(this)

    enum class Constraint {
        LEFT_TO_LEFT, LEFT_TO_RIGHT, RIGHT_TO_LEFT, RIGHT_TO_RIGHT, START_TO_START, START_TO_END, END_TO_START, END_TO_END, TOP_TO_TOP, TOP_TO_BOTTOM, TOP_TO_BASELINE, BOTTOM_TO_TOP, BOTTOM_TO_BOTTOM, BOTTOM_TO_BASELINE, BASELINE_TO_BASELINE, BASELINE_TO_TOP, BASELINE_TO_BOTTOM, CENTER_HORIZONTALLY, CENTER_VERTICALLY, CIRCULAR_CONSTRAINT
    }

    enum class Direction {
        LEFT, RIGHT, START, END, TOP, BOTTOM
    }

    enum class Helper {
        HORIZONTAL_CHAIN, VERTICAL_CHAIN, ALIGN_HORIZONTALLY, ALIGN_VERTICALLY, BARRIER, LAYER, HORIZONTAL_FLOW, VERTICAL_FLOW, GRID, ROW, COLUMN, FLOW
    }

    enum class Chain {
        SPREAD, SPREAD_INSIDE, PACKED;

        companion object {
            var chainMap: MutableMap<String, Chain> = mutableMapOf()
            var valueMap: MutableMap<String, Int> = mutableMapOf()

            init {
                chainMap.put("packed", PACKED)
                chainMap.put("spread_inside", SPREAD_INSIDE)
                chainMap.put("spread", SPREAD)
                valueMap.put("packed", ConstraintWidget.CHAIN_PACKED)
                valueMap.put("spread_inside", ConstraintWidget.CHAIN_SPREAD_INSIDE)
                valueMap.put("spread", ConstraintWidget.CHAIN_SPREAD)
            }

            /**
             * Get the Enum value with a String
             * @param str a String representation of a Enum value
             * @return a Enum value
             */
            fun getValueByString(str: String): Int {
                return if (valueMap.containsKey(str)) {
                    valueMap[str]!!
                } else UNKNOWN
            }

            /**
             * Get the actual int value with a String
             * @param str a String representation of a Enum value
             * @return an actual int value
             */
            fun getChainByString(str: String): Chain? {
                return if (chainMap.containsKey(str)) {
                    chainMap[str]
                } else null
            }
        }
    }

    enum class Wrap {
        NONE, CHAIN, ALIGNED;

        companion object {
            var wrapMap: MutableMap<String, Wrap> = mutableMapOf()
            var valueMap: MutableMap<String, Int> = mutableMapOf()

            init {
                wrapMap.put("none", NONE)
                wrapMap.put("chain", CHAIN)
                wrapMap.put("aligned", ALIGNED)
                valueMap.put("none", 0)
                valueMap.put("chain", 3) // Corresponds to CHAIN_NEW
                valueMap.put("aligned", 2)
            }

            /**
             * Get the actual int value with a String
             * @param str a String representation of a Enum value
             * @return a actual int value
             */
            fun getValueByString(str: String): Int {
                return if (valueMap.containsKey(str)) {
                    valueMap[str]!!
                } else UNKNOWN
            }

            /**
             * Get the Enum value with a String
             * @param str a String representation of a Enum value
             * @return a Enum value
             */
            fun getChainByString(str: String): Wrap? {
                return if (wrapMap.containsKey(str)) {
                    wrapMap[str]
                } else null
            }
        }
    }

    /**
     * Set the function that converts dp to Pixels
     */
    var dpToPixel: CorePixelDp?
        get() = mDpToPixel
        set(dpToPixel) {
            mDpToPixel = dpToPixel
        }

    /**
     * Clear the state
     */
    fun reset() {
        for (ref in mReferences.keys) {
            mReferences[ref]?.constraintWidget?.reset()
        }
        mReferences.clear()
        mReferences[PARENT] = mParent
        mHelperReferences.clear()
        mTags.clear()
        mBaselineNeeded.clear()
        mDirtyBaselineNeededWidgets = true
    }

    /**
     * Implements a conversion function for values, returning int.
     * This can be used in case values (e.g. margins) are represented
     * via an object, not directly an int.
     *
     * @param value the object to convert from
     */
    fun convertDimension(value: Any): Int {
        if (value is Float) {
            return value.roundToInt()
        }
        return if (value is Int) {
            value.toInt()
        } else 0
    }

    /**
     * Create a new reference given a key.
     */
    fun createConstraintReference(key: Any?): ConstraintReference {
        return ConstraintReference(this)
    }

    // @TODO: add description
    fun sameFixedWidth(width: Int): Boolean {
        return mParent.width?.equalsFixedValue(width) == true
    }

    // @TODO: add description
    fun sameFixedHeight(height: Int): Boolean {
        return mParent.height?.equalsFixedValue(height) == true
    }

    // @TODO: add description
    fun width(dimension: Dimension): State {
        return setWidth(dimension)
    }

    // @TODO: add description
    fun height(dimension: Dimension): State {
        return setHeight(dimension)
    }

    // @TODO: add description
    fun setWidth(dimension: Dimension): State {
        mParent.setWidth(dimension)
        return this
    }

    // @TODO: add description
    fun setHeight(dimension: Dimension): State {
        mParent.setHeight(dimension)
        return this
    }

    fun reference(key: Any?): Reference? {
        return mReferences[key]
    }

    // @TODO: add description
    fun constraints(key: Any?): ConstraintReference? {
        var reference: Reference? = mReferences[key]
        if (reference == null) {
            reference = createConstraintReference(key)
            mReferences[key] = reference
            reference.key = key
        }
        return if (reference is ConstraintReference) {
            reference
        } else null
    }

    private var mNumHelpers = 0
    private fun createHelperKey(): String {
        return "__HELPER_KEY_" + mNumHelpers++ + "__"
    }

    // @TODO: add description
    fun helper(key: Any?, type: Helper): HelperReference? {
        var key: Any? = key
        if (key == null) {
            key = createHelperKey()
        }
        var reference: HelperReference? = mHelperReferences[key]
        if (reference == null) {
            when (type) {
                Helper.HORIZONTAL_CHAIN -> {
                    reference = HorizontalChainReference(this)
                }
                Helper.VERTICAL_CHAIN -> {
                    reference = VerticalChainReference(this)
                }
                Helper.ALIGN_HORIZONTALLY -> {
                    reference = AlignHorizontallyReference(this)
                }
                Helper.ALIGN_VERTICALLY -> {
                    reference = AlignVerticallyReference(this)
                }
                Helper.BARRIER -> {
                    reference = BarrierReference(this)
                }
                Helper.VERTICAL_FLOW, Helper.HORIZONTAL_FLOW -> {
                    reference = FlowReference(this, type)
                }
                Helper.GRID, Helper.ROW, Helper.COLUMN -> {
                    reference = GridReference(this, type)
                }
                else -> {
                    reference = HelperReference(this, type)
                }
            }
            reference.key = key
            mHelperReferences[key] = reference
        }
        return reference
    }

    // @TODO: add description
    fun horizontalGuideline(key: Any?): GuidelineReference? {
        return guideline(key, ConstraintWidget.HORIZONTAL)
    }

    // @TODO: add description
    fun verticalGuideline(key: Any?): GuidelineReference? {
        return guideline(key, ConstraintWidget.VERTICAL)
    }

    // @TODO: add description
    fun guideline(key: Any?, orientation: Int): GuidelineReference? {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.facade == null
            || reference.facade !is GuidelineReference
        ) {
            val guidelineReference = GuidelineReference(this)
            guidelineReference.orientation = orientation
            guidelineReference.key = key
            reference?.facade = guidelineReference
        }
        return reference?.facade as GuidelineReference
    }

    // @TODO: add description
    fun barrier(key: Any?, direction: Direction?): BarrierReference {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.facade == null || reference.facade !is BarrierReference) {
            val barrierReference = BarrierReference(this)
            barrierReference.setBarrierDirection(direction)
            reference?.facade = barrierReference
        }
        return reference?.facade as BarrierReference
    }

    /**
     * Get a Grid reference
     *
     * @param key name of the reference object
     * @param gridType type of Grid pattern - Grid, Row, or Column
     * @return a GridReference object
     */
    fun getGrid(key: Any?, gridType: String): GridReference {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.facade == null || reference.facade !is GridReference) {
            var Type = Helper.GRID
            if (gridType[0] == 'r') {
                Type = Helper.ROW
            } else if (gridType[0] == 'c') {
                Type = Helper.COLUMN
            }
            val gridReference = GridReference(this, Type)
            reference?.facade = gridReference
        }
        return reference?.facade as GridReference
    }

    /**
     * Gets a reference to a Flow object. Creating it if needed.
     * @param key id of the reference
     * @param vertical is it a vertical or horizontal flow
     * @return a FlowReference
     */
    fun getFlow(key: Any?, vertical: Boolean): FlowReference? {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.facade == null || reference.facade !is FlowReference) {
            val flowReference: FlowReference =
                if (vertical) FlowReference(this, Helper.VERTICAL_FLOW) else FlowReference(
                    this,
                    Helper.HORIZONTAL_FLOW
                )
            reference?.facade = flowReference
        }
        return reference?.facade as FlowReference
    }

    // @TODO: add description
    fun verticalChain(): VerticalChainReference? {
        return helper(null, Helper.VERTICAL_CHAIN) as VerticalChainReference?
    }

    // @TODO: add description
    fun verticalChain(vararg references: Any?): VerticalChainReference? {
        val reference: VerticalChainReference? =
            helper(null, Helper.VERTICAL_CHAIN) as VerticalChainReference?
        reference?.add(references)
        return reference
    }

    // @TODO: add description
    fun horizontalChain(): HorizontalChainReference? {
        return helper(null, Helper.HORIZONTAL_CHAIN) as HorizontalChainReference?
    }

    // @TODO: add description
    fun horizontalChain(vararg references: Any?): HorizontalChainReference? {
        val reference: HorizontalChainReference? =
            helper(null, Helper.HORIZONTAL_CHAIN) as HorizontalChainReference?
        reference?.add(references)
        return reference
    }

    /**
     * Get a VerticalFlowReference
     *
     * @return a VerticalFlowReference
     */
    val verticalFlow: FlowReference?
        get() = helper(null, Helper.VERTICAL_FLOW) as FlowReference?

    /**
     * Get a VerticalFlowReference and add it to references
     *
     * @param references where we add the VerticalFlowReference
     * @return a VerticalFlowReference
     */
    fun getVerticalFlow(vararg references: Any?): FlowReference? {
        val reference: FlowReference? = helper(null, Helper.VERTICAL_FLOW) as FlowReference?
        reference?.add(references)
        return reference
    }

    /**
     * Get a HorizontalFlowReference
     *
     * @return a HorizontalFlowReference
     */
    val horizontalFlow: FlowReference?
        get() = helper(null, Helper.HORIZONTAL_FLOW) as FlowReference?

    /**
     * Get a HorizontalFlowReference and add it to references
     *
     * @param references references where we the HorizontalFlowReference
     * @return a HorizontalFlowReference
     */
    fun getHorizontalFlow(vararg references: Any?): FlowReference? {
        val reference: FlowReference? = helper(null, Helper.HORIZONTAL_FLOW) as FlowReference?
        reference?.add(references)
        return reference
    }

    // @TODO: add description
    fun centerHorizontally(vararg references: Any?): AlignHorizontallyReference? {
        val reference: AlignHorizontallyReference? =
            helper(null, Helper.ALIGN_HORIZONTALLY) as AlignHorizontallyReference?
        reference?.add(references)
        return reference
    }

    // @TODO: add description
    fun centerVertically(vararg references: Any?): AlignVerticallyReference? {
        val reference: AlignVerticallyReference? =
            helper(null, Helper.ALIGN_VERTICALLY) as AlignVerticallyReference?
        reference?.add(references)
        return reference
    }

    // @TODO: add description
    fun directMapping() {
        for (key in mReferences.keys) {
            val ref: Reference = constraints(key) ?: continue
            val reference: ConstraintReference = ref as ConstraintReference
            reference.view = key
        }
    }

    // @TODO: add description
    fun map(key: Any?, view: Any?) {
        val ref: ConstraintReference? = constraints(key)
        if (ref != null) {
            ref.view = view
        }
    }

    // @TODO: add description
    fun setTag(key: String?, tag: String) {
        val ref: Reference? = constraints(key)
        if (ref is ConstraintReference) {
            ref.tag = tag
            var list: ArrayList<String?>? = null
            if (!mTags.containsKey(tag)) {
                list = ArrayList()
                mTags[tag] = list
            } else {
                list = mTags[tag]
            }
            list!!.add(key)
        }
    }

    // @TODO: add description
    fun getIdsForTag(tag: String): ArrayList<String?>? {
        return if (mTags.containsKey(tag)) {
            mTags[tag]
        } else null
    }

    // @TODO: add description
    fun apply(container: ConstraintWidgetContainer) {
        container.removeAllChildren()
        mParent.width?.apply(this, container, ConstraintWidget.HORIZONTAL)
        mParent.height?.apply(this, container, ConstraintWidget.VERTICAL)
        // add helper references
        for (key in mHelperReferences.keys) {
            val reference: HelperReference? = mHelperReferences[key]
            val helperWidget: HelperWidget? = reference?.helperWidget
            if (helperWidget != null) {
                var constraintReference: Reference? = mReferences[key]
                if (constraintReference == null) {
                    constraintReference = constraints(key)
                }
                constraintReference?.constraintWidget = helperWidget
            }
        }
        for (key in mReferences.keys) {
            val reference: Reference? = mReferences[key]
            if (reference != mParent && reference?.facade is HelperReference) {
                val helperWidget: HelperWidget? =
                    (reference.facade as HelperReference).helperWidget
                if (helperWidget != null) {
                    var constraintReference: Reference? = mReferences[key]
                    if (constraintReference == null) {
                        constraintReference = constraints(key)
                    }
                    constraintReference?.constraintWidget = helperWidget
                }
            }
        }
        for (key in mReferences.keys) {
            val reference: Reference? = mReferences[key]
            if (reference != mParent) {
                val widget: ConstraintWidget? = reference?.constraintWidget
                widget?.debugName = reference?.key.toString()
                widget?.parent = null
                if (reference?.facade is GuidelineReference) {
                    // we apply Guidelines first to correctly setup their ConstraintWidget.
                    reference.apply()
                }
                if(widget != null)
                    container.add(widget)
            } else {
                reference.constraintWidget = container
            }
        }
        for (key in mHelperReferences.keys) {
            // We need this pass to apply chains properly
            val reference: HelperReference? = mHelperReferences[key]
            val helperWidget: HelperWidget? = reference?.helperWidget
            if (helperWidget != null) {
                for (keyRef in reference.mReferences) {
                    val constraintReference: Reference? = mReferences[keyRef]
                    reference.helperWidget?.add(constraintReference?.constraintWidget)
                }
                reference.apply()
            } else {
                reference!!.apply()
            }
        }
        for (key in mReferences.keys) {
            val reference: Reference? = mReferences[key]
            if (reference != mParent && reference?.facade is HelperReference) {
                val helperReference: HelperReference = reference.facade as HelperReference
                val helperWidget: HelperWidget? = helperReference.helperWidget
                if (helperWidget != null) {
                    for (keyRef in helperReference.mReferences) {
                        val constraintReference: Reference? = mReferences[keyRef]
                        if (constraintReference != null) {
                            helperWidget.add(constraintReference.constraintWidget)
                        } else if (keyRef is Reference) {
                            helperWidget.add((keyRef as Reference).constraintWidget)
                        } else {
                            println("couldn't find reference for $keyRef")
                        }
                    }
                    reference.apply()
                }
            }
        }
        for (key in mReferences.keys) {
            val reference: Reference? = mReferences[key]
            reference!!.apply()
            val widget: ConstraintWidget? = reference.constraintWidget
            if (widget != null && key != null) {
                widget.stringId = key.toString()
            }
        }
    }

    // ================= add baseline code================================
    var mBaselineNeeded: ArrayList<Any?> = ArrayList()
    var mBaselineNeededWidgets: ArrayList<ConstraintWidget> = ArrayList()
    var mDirtyBaselineNeededWidgets = true

    init {
        mParent.key = PARENT
        mReferences[PARENT] = mParent
    }

    /**
     * Baseline is needed for this object
     */
    fun baselineNeededFor(id: Any?) {
        mBaselineNeeded.add(id)
        mDirtyBaselineNeededWidgets = true
    }

    /**
     * Does this constraintWidget need a baseline
     *
     * @return true if the constraintWidget needs a baseline
     */
    fun isBaselineNeeded(constraintWidget: ConstraintWidget): Boolean {
        if (mDirtyBaselineNeededWidgets) {
            mBaselineNeededWidgets.clear()
            for (id in mBaselineNeeded) {
                val widget: ConstraintWidget? = mReferences[id]?.constraintWidget
                if (widget != null) mBaselineNeededWidgets.add(widget)
            }
            mDirtyBaselineNeededWidgets = false
        }
        return mBaselineNeededWidgets.contains(constraintWidget)
    }

    companion object {
        const val UNKNOWN = -1
        const val CONSTRAINT_SPREAD = 0
        const val CONSTRAINT_WRAP = 1
        const val CONSTRAINT_RATIO = 2
        val PARENT: Int = 0
    }
}