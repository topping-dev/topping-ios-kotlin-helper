/*
 * Copyright (C) 2022 The Android Open Source Project
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
package dev.topping.ios.constraint.core.state.helpers

import dev.topping.ios.constraint.core.state.HelperReference
import dev.topping.ios.constraint.core.state.State
import dev.topping.ios.constraint.core.utils.GridCore
import dev.topping.ios.constraint.core.widgets.HelperWidget

/**
 * A HelperReference of a Grid Helper that helps enable Grid in Compose
 */
class GridReference(state: State, type: State.Helper) :
    HelperReference(state, type) {
    /**
     * The Grid Object
     */
    private var mGrid: GridCore? = null
    /**
     * get padding left
     * @return padding left
     */
    /**
     * set padding left
     * @param paddingStart padding left to be set
     */
    /**
     * padding start
     */
    var paddingStart = 0
    /**
     * get padding right
     * @return padding right
     */
    /**
     * set padding right
     * @param paddingEnd padding right to be set
     */
    /**
     * padding end
     */
    var paddingEnd = 0
    /**
     * get padding top
     * @return padding top
     */
    /**
     * set padding top
     * @param paddingTop padding top to be set
     */
    /**
     * padding top
     */
    var paddingTop = 0
    /**
     * get padding bottom
     * @return padding bottom
     */
    /**
     * set padding bottom
     * @param paddingBottom padding bottom to be set
     */
    /**
     * padding bottom
     */
    var paddingBottom = 0
    /**
     * Get the Orientation
     * @return the Orientation
     */
    /**
     * Set the Orientation
     * @param orientation the Orientation
     */
    /**
     * The orientation of the widgets arrangement horizontally or vertically
     */
    var orientation = 0

    /**
     * Number of rows of the Grid
     */
    private var mRowsSet = 0

    /**
     * Number of columns of the Grid
     */
    private var mColumnsSet = 0
    /**
     * Get the horizontal gaps
     * @return the horizontal gaps
     */
    /**
     * Set the horizontal gaps
     * @param horizontalGaps the horizontal gaps
     */
    /**
     * The horizontal gaps between widgets
     */
    var horizontalGaps = 0f
    /**
     * Get the vertical gaps
     * @return the vertical gaps
     */
    /**
     * Set the vertical gaps
     * @param verticalGaps  the vertical gaps
     */
    /**
     * The vertical gaps between widgets
     */
    var verticalGaps = 0f
    /**
     * Get the row weights
     * @return the row weights
     */
    /**
     * Set the row weights
     * @param rowWeights the row weights
     */
    /**
     * The weight of each widget in a row
     */
    var rowWeights: String? = null
    /**
     * Get the column weights
     * @return the column weights
     */
    /**
     * Set the column weights
     * @param columnWeights the column weights
     */
    /**
     * The weight of each widget in a column
     */
    var columnWeights: String? = null
    /**
     * Get the spans
     * @return the spans
     */
    /**
     * Set the spans
     * @param spans the spans
     */
    /**
     * Specify the spanned areas of widgets
     */
    var spans: String? = null
    /**
     * Get the skips
     * @return the skips
     */
    /**
     * Set the skips
     * @param skips the skips
     */
    /**
     * Specify the positions to be skipped in the Grid
     */
    var skips: String? = null
    /**
     * Get all the flags of a Grid
     * @return a String array containing all the flags
     */
    /**
     * Set flags of a Grid
     * @param flags a String array containing all the flags
     */
    /**
     * All the flags of a Grid
     */
    var flags: IntArray = intArrayOf()

    init {
        if (type == State.Helper.ROW) {
            mRowsSet = 1
        } else if (type == State.Helper.COLUMN) {
            mColumnsSet = 1
        }
    }

    /**
     * Set flags of a Grid
     * @param flags a String containing all the flags
     */
    fun setFlags(flags: String) {
        if (flags.length == 0) {
            return
        }
        val strArr: Array<String> = flags.split("\\|").toTypedArray()
        val flagList: MutableList<Int> = mutableListOf()
        for (flag in strArr) {
            when (flag.lowercase()) {
                SUB_GRID_BY_COL_ROW -> flagList.add(0)
                SPANS_RESPECT_WIDGET_ORDER -> flagList.add(1)
            }
        }
        val flagArr = IntArray(flagList.size)
        var i = 0
        for (flag in flagList) {
            flagArr[i++] = flag
        }
        this.flags = flagArr
    }
    /**
     * Get the number of rows
     * @return the number of rows
     */
    /**
     * Set the number of rows
     * @param rowsSet the number of rows
     */
    var rowsSet: Int
        get() = mRowsSet
        set(rowsSet) {
            if (super.type == State.Helper.COLUMN) {
                return
            }
            mRowsSet = rowsSet
        }
    /**
     * Get the number of columns
     * @return the number of columns
     */
    /**
     * Set the number of columns
     * @param columnsSet the number of columns
     */
    var columnsSet: Int
        get() = mColumnsSet
        set(columnsSet) {
            if (super.type == State.Helper.ROW) {
                return
            }
            mColumnsSet = columnsSet
        }
    /**
     * Get the helper widget (Grid)
     * @return the helper widget (Grid)
     */
    /**
     * Set the helper widget (Grid)
     * @param widget the helper widget (Grid)
     */
    override var helperWidget: HelperWidget?
        get() {
            if (mGrid == null) {
                mGrid = GridCore()
            }
            return mGrid
        }
        set(widget) {
            mGrid = if (widget is GridCore) {
                widget
            } else {
                null
            }
        }

    /**
     * Apply all the attributes to the helper widget (Grid)
     */
    
    override fun apply() {
        helperWidget
        mGrid?.orientation = orientation
        if (mRowsSet != 0) {
            mGrid?.setRows(mRowsSet)
        }
        if (mColumnsSet != 0) {
            mGrid?.setColumns(mColumnsSet)
        }
        if (horizontalGaps != 0f) {
            mGrid?.horizontalGaps = horizontalGaps
        }
        if (verticalGaps != 0f) {
            mGrid?.verticalGaps = verticalGaps
        }
        if (rowWeights != null && !rowWeights.equals("")) {
            mGrid?.rowWeights = rowWeights
        }
        if (columnWeights != null && !columnWeights.equals("")) {
            mGrid?.columnWeights = columnWeights
        }
        if (spans != null && !spans.equals("")) {
            mGrid?.setSpans(spans!!)
        }
        if (skips != null && !skips.equals("")) {
            mGrid?.setSkips(skips!!)
        }
        if (flags != null && flags!!.size > 0) {
            mGrid?.flags = flags
        }

        // General attributes of a widget
        applyBase()
    }

    companion object {
        private const val SPANS_RESPECT_WIDGET_ORDER = "spansrespectwidgetorder"
        private const val SUB_GRID_BY_COL_ROW = "subgridbycolrow"
    }
}