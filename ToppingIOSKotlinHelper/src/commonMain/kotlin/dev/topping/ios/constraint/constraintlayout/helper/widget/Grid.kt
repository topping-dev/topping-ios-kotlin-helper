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
package dev.topping.ios.constraint.constraintlayout.helper.widget

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.constraintlayout.widget.VirtualLayout
import kotlin.math.max
import kotlin.math.sqrt

/**
 * A helper class that helps arrange widgets in a grid form
 *
 * <h2>Grid</h2>
 * <table summary="Grid attributes">
 * <tr>
 * <th>Attributes</th><th>Description</th>
</tr> *
 * <tr>
 * <td>grid_rows</td>
 * <td>Indicates the number of rows will be created for the grid form.</td>
</tr> *
 * <tr>
 * <td>grid_columns</td>
 * <td>Indicates the number of columns will be created for the grid form.</td>
</tr> *
 * <tr>
 * <td>grid_rowWeights</td>
 * <td>Specifies the weight of each row in the grid form (default value is 1).</td>
</tr> *
 * <tr>
 * <td>grid_columnWeights</td>
 * <td>Specifies the weight of each column in the grid form (default value is 1).</td>
</tr> *
 * <tr>
 * <td>grid_spans</td>
 * <td>Offers the capability to span a widget across multiple rows and columns</td>
</tr> *
 * <tr>
 * <td>grid_skips</td>
 * <td>Enables skip certain positions in the grid and leave them empty</td>
</tr> *
 * <tr>
 * <td>grid_orientation</td>
 * <td>Defines how the associated widgets will be arranged - vertically or horizontally</td>
</tr> *
 * <tr>
 * <td>grid_horizontalGaps</td>
 * <td>Adds margin horizontally between widgets</td>
</tr> *
 * <tr>
 * <td>grid_verticalGaps</td>
 * <td>Adds margin vertically between widgets</td>
</tr> *
</table> *
 */
class Grid(context: TContext, attrs: AttributeSet, self: TView) : VirtualLayout(context, attrs, self) {
    private val mMaxRows = 50 // maximum number of rows can be specified.
    private val mMaxColumns = 50 // maximum number of columns can be specified.

    // private final ConstraintSet mConstraintSet = new ConstraintSet();
    private var mBoxViews: Array<TView?>? = null
    var mContainer: ConstraintLayout? = null

    /**
     * number of rows of the grid
     */
    private var mRows = 0

    /**
     * number of rows set by the XML or API
     */
    private var mRowsSet = 0

    /**
     * number of columns of the grid
     */
    private var mColumns = 0

    /**
     * number of columns set by the XML or API
     */
    private var mColumnsSet = 0
    /**
     * get the string value of spans
     *
     * @return the string value of spans
     */
    /**
     * string format of the input Spans
     */
    var spans: String? = null
        private set
    /**
     * get the string value of skips
     *
     * @return the string value of skips
     */
    /**
     * string format of the input Skips
     */
    var skips: String? = null
        private set
    /**
     * get the string value of rowWeights
     *
     * @return the string value of rowWeights
     */
    /**
     * string format of the row weight
     */
    var rowWeights: String? = null
        private set
    /**
     * get the string value of columnWeights
     *
     * @return the string value of columnWeights
     */
    /**
     * string format of the column weight
     */
    var columnWeights: String? = null
        private set

    /**
     * Horizontal gaps in Dp
     */
    private var mHorizontalGaps = 0f

    /**
     * Vertical gaps in Dp
     */
    private var mVerticalGaps = 0f

    /**
     * orientation of the view arrangement - vertical or horizontal
     */
    private var mOrientation = 0

    /**
     * Indicates what is the next available position to place an widget
     */
    private var mNextAvailableIndex = 0

    /**
     * Indicates whether the input attributes need to be validated
     */
    private var mValidateInputs = false

    /**
     * Indicates whether to use RTL layout direction
     */
    private var mUseRtl = false

    /**
     * A integer matrix that tracks the positions that are occupied by skips and spans
     * true: available position
     * false: non-available position
     */
    private var mPositionMatrix: Array<BooleanArray> = Array<BooleanArray>(0) { booleanArrayOf() }

    /**
     * Store the view ids of handled spans
     */
    var mSpanIds: HashSet<String> = hashSetOf()

    /**
     * Ids of the boxViews
     */
    private var mBoxViewIds: Array<String> = Array(0) { "" }
    
    init {
        mUseViewMeasure = true
        val a = context.getResources()
        attrs.forEach { kvp ->
            val attr = kvp.value
            if(kvp.key == "grid_rows") {
                mRowsSet = a.getInt(kvp.key, attr, 0)
            } else if(kvp.key == "grid_columns") {
                mColumnsSet = a.getInt(kvp.key, attr, 0)
            } else if(kvp.key == "grid_spans") {
                spans = a.getString(kvp.key, attr)
            } else if(kvp.key == "grid_skips") {
                skips = a.getString(kvp.key, attr)
            } else if(kvp.key == "grid_rowWeights") {
                rowWeights = a.getString(kvp.key, attr)
            } else if(kvp.key == "grid_columnWeights") {
                columnWeights = a.getString(kvp.key, attr)
            } else if(kvp.key == "grid_orientation") {
                mOrientation = a.getInt(kvp.key, attr, 0)
            } else if(kvp.key == "grid_horizontalGaps") {
                mHorizontalGaps = a.getDimension(attr, 0f)
            } else if(kvp.key == "grid_verticalGaps") {
                mVerticalGaps = a.getDimension(attr, 0f)
            } else if(kvp.key == "grid_validateInputs") {
                // @TODO handle validation
                mValidateInputs = a.getBoolean(attr, false)
            } else if(kvp.key == "grid_useRtl") {
                // @TODO handle RTL
                mUseRtl = a.getBoolean(attr, false)
            }
        }
        updateActualRowsAndColumns()
        initVariables()
    }

    /**
     * Compute the actual rows and columns given what was set
     * if 0,0 find the most square rows and columns that fits
     * if 0,n or n,0 scale to fit
     */
    private fun updateActualRowsAndColumns() {
        if (mRowsSet == 0 || mColumnsSet == 0) {
            if (mColumnsSet > 0) {
                mColumns = mColumnsSet
                mRows = (mCount + mColumns - 1) / mColumnsSet // round up
            } else if (mRowsSet > 0) {
                mRows = mRowsSet
                mColumns = (mCount + mRowsSet - 1) / mRowsSet // round up
            } else { // as close to square as possible favoring more rows
                mRows = (1.5 + sqrt(mCount.toDouble())).toInt()
                mColumns = (mCount + mRows - 1) / mRows
            }
        } else {
            mRows = mRowsSet
            mColumns = mColumnsSet
        }
    }

    override fun onAttachedToWindow(sup: TView?) {
        super.onAttachedToWindow(sup)
        mContainer = self.getParent()?.getParentType() as ConstraintLayout?
        generateGrid(false)
    }

    /**
     * generate the Grid form based on the input attributes
     *
     * @param isUpdate whether to update the existing grid (true) or create a new one (false)
     * @return true if all the inputs are valid else false
     */
    private fun generateGrid(isUpdate: Boolean): Boolean {
        if (mContainer == null || mRows < 1 || mColumns < 1) {
            return false
        }
        if (isUpdate) {
            for (i in mPositionMatrix.indices) {
                for (j in mPositionMatrix[0].indices) {
                    mPositionMatrix[i][j] = true
                }
            }
            mSpanIds.clear()
        }
        mNextAvailableIndex = 0
        var isSuccess = true
        buildBoxes()
        if (skips != null && !skips!!.trim { it <= ' ' }.isEmpty()) {
            val mSkips = parseSpans(skips!!)
            if (mSkips != null) {
                isSuccess = isSuccess and handleSkips(mSkips)
            }
        }
        if (spans != null && !spans!!.trim { it <= ' ' }.isEmpty()) {
            val mSpans = parseSpans(spans!!)
            if (mSpans != null) {
                isSuccess = isSuccess and handleSpans(mIds, mSpans)
            }
        }
        isSuccess = isSuccess and arrangeWidgets()
        return isSuccess || !mValidateInputs
    }

    /**
     * Initialize the relevant variables
     */
    private fun initVariables() {
        mPositionMatrix = Array(mRows) { BooleanArray(mColumns) }
        for (row in mPositionMatrix) {
            Arrays.fill(row, true)
        }
    }

    /**
     * parse the weights/pads in the string format into a float array
     *
     * @param size size of the return array
     * @param str  weights/pads in a string format
     * @return a float array with weights/pads values
     */
    private fun parseWeights(size: Int, str: String?): FloatArray? {
        if (str == null || str.trim { it <= ' ' }.isEmpty()) {
            return null
        }
        val values = str.split(",").toTypedArray()
        if (values.size != size) {
            return null
        }
        val arr = FloatArray(size)
        for (i in arr.indices) {
            arr[i] = values[i].trim { it <= ' ' }.toFloat()
        }
        return arr
    }

    private fun params(v: TView?): ConstraintLayout.LayoutParams {
        return v!!.getLayoutParams() as ConstraintLayout.LayoutParams
    }

    /**
     * Connect the view to the corresponding viewBoxes based on the input params
     *
     * @param view   the Id of the view
     * @param row    row position to place the view
     * @param column column position to place the view
     */
    private fun connectView(
        view: TView,
        row: Int,
        column: Int,
        rowSpan: Int,
        columnSpan: Int
    ) {
        val params: ConstraintLayout.LayoutParams = params(view)
        // @TODO handle RTL
        // Connect the 4 sides
        params.leftToLeft = mBoxViewIds[column]
        params.topToTop = mBoxViewIds[row]
        params.rightToRight = mBoxViewIds[column + columnSpan - 1]
        params.bottomToBottom = mBoxViewIds[row + rowSpan - 1]
        view.setLayoutParams(params)
    }

    /**
     * Arrange the views in the constraint_referenced_ids
     *
     * @return true if all the widgets can be arranged properly else false
     */
    private fun arrangeWidgets(): Boolean {
        var position: Int
        val views: Array<TView?>? = getViews(mContainer)
        // @TODO handle RTL
        for (i in 0 until mCount) {
            if(views?.get(i) == null)
                continue
            if (mSpanIds.contains(mIds.get(i))) {
                // skip the viewId that's already handled by handleSpans
                continue
            }
            position = nextPosition
            val row = getRowByIndex(position)
            val col = getColByIndex(position)
            if (position == -1) {
                // no more available position.
                return false
            }
            connectView(views[i]!!, row, col, 1, 1)
        }
        return true
    }

    /**
     * Convert a 1D index to a 2D index that has index for row and index for column
     *
     * @param index index in 1D
     * @return row as its values.
     */
    private fun getRowByIndex(index: Int): Int {
        return if (mOrientation == 1) {
            index % mRows
        } else {
            index / mColumns
        }
    }

    /**
     * Convert a 1D index to a 2D index that has index for row and index for column
     *
     * @param index index in 1D
     * @return column as its values.
     */
    private fun getColByIndex(index: Int): Int {
        return if (mOrientation == 1) {
            index / mRows
        } else {
            index % mColumns
        }
    }// position = getPositionByIndex(mNextAvailableIndex);//  int[] position = new int[] {0, 0};

    /**
     * Get the next available position for widget arrangement.
     *
     * @return int[] -> [row, column]
     */
    private val nextPosition: Int
        private get() {
            //  int[] position = new int[] {0, 0};
            var position = 0
            var positionFound = false
            while (!positionFound) {
                if (mNextAvailableIndex >= mRows * mColumns) {
                    return -1
                }

                // position = getPositionByIndex(mNextAvailableIndex);
                position = mNextAvailableIndex
                val row = getRowByIndex(mNextAvailableIndex)
                val col = getColByIndex(mNextAvailableIndex)
                if (mPositionMatrix[row][col]) {
                    mPositionMatrix[row][col] = false
                    positionFound = true
                }
                mNextAvailableIndex++
            }
            return position
        }

    /**
     * Check if the value of the spans/skips is valid
     *
     * @param str spans/skips in string format
     * @return true if it is valid else false
     */
    private fun isSpansValid(str: CharSequence): Boolean {
        // TODO: check string has a valid format.
        return true
    }

    /**
     * Check if the value of the rowWeights or columnsWeights is valid
     *
     * @param str rowWeights/columnsWeights in string format
     * @return true if it is valid else false
     */
    private fun isWeightsValid(str: String): Boolean {
        // TODO: check string has a valid format.
        return true
    }

    /**
     * parse the skips/spans in the string format into a int matrix
     * that each row has the information - [index, row_span, col_span]
     * the format of the input string is index:row_spanxcol_span.
     * index - the index of the starting position
     * row_span - the number of rows to span
     * col_span- the number of columns to span
     *
     * @param str string format of skips or spans
     * @return a int matrix that contains skip information.
     */
    private fun parseSpans(str: String): Array<IntArray>? {
        if (!isSpansValid(str)) {
            return null
        }
        val spans = str.split(",").toTypedArray()
        val spanMatrix = Array(spans.size) {
            IntArray(
                3
            )
        }
        var indexAndSpan: Array<String>
        var rowAndCol: Array<String>
        for (i in spans.indices) {
            indexAndSpan = spans[i].trim { it <= ' ' }.split(":").toTypedArray()
            rowAndCol = indexAndSpan[1].split("x").toTypedArray()
            spanMatrix[i][0] = indexAndSpan[0].toInt()
            spanMatrix[i][1] = rowAndCol[0].toInt()
            spanMatrix[i][2] = rowAndCol[1].toInt()
        }
        return spanMatrix
    }

    /**
     * Handle the span use cases
     *
     * @param spansMatrix a int matrix that contains span information
     * @return true if the input spans is valid else false
     */
    private fun handleSpans(mId: Array<String>, spansMatrix: Array<IntArray>): Boolean {
        val views: Array<TView?>? = getViews(mContainer)
        for (i in spansMatrix.indices) {
            val row = getRowByIndex(spansMatrix[i][0])
            val col = getColByIndex(spansMatrix[i][0])
            if (!invalidatePositions(
                    row, col,
                    spansMatrix[i][1], spansMatrix[i][2]
                )
            ) {
                return false
            }
            if(views?.get(i) == null)
                return false
            connectView(
                views[i]!!, row, col,
                spansMatrix[i][1], spansMatrix[i][2]
            )
            mSpanIds.add(mId[i])
        }
        return true
    }

    /**
     * Make positions in the grid unavailable based on the skips attr
     *
     * @param skipsMatrix a int matrix that contains skip information
     * @return true if all the skips are valid else false
     */
    private fun handleSkips(skipsMatrix: Array<IntArray>): Boolean {
        for (i in skipsMatrix.indices) {
            val row = getRowByIndex(skipsMatrix[i][0])
            val col = getColByIndex(skipsMatrix[i][0])
            if (!invalidatePositions(
                    row, col,
                    skipsMatrix[i][1], skipsMatrix[i][2]
                )
            ) {
                return false
            }
        }
        return true
    }

    /**
     * Make the specified positions in the grid unavailable.
     *
     * @param startRow    the row of the staring position
     * @param startColumn the column of the staring position
     * @param rowSpan     how many rows to span
     * @param columnSpan  how many columns to span
     * @return true if we could properly invalidate the positions else false
     */
    private fun invalidatePositions(
        startRow: Int, startColumn: Int,
        rowSpan: Int, columnSpan: Int
    ): Boolean {
        for (i in startRow until startRow + rowSpan) {
            for (j in startColumn until startColumn + columnSpan) {
                if (i >= mPositionMatrix.size || j >= mPositionMatrix[0].size || !mPositionMatrix[i][j]
                ) {
                    // the position is already occupied.
                    return false
                }
                mPositionMatrix[i][j] = false
            }
        }
        return true
    }

    /**
     * Visualize the boxViews that are used to constraint widgets.
     *
     * @param canvas canvas to visualize the boxViews
     */
    fun onDraw(sup: TView?, canvas: TCanvas) {
        sup?.onDraw(canvas)
        // Visualize the viewBoxes if isInEditMode() is true
        if (!self.isInEditMode()) {
            return
        }
        //TODO:
        /*@SuppressLint("DrawAllocation") val paint: java.awt.Paint =
            java.awt.Paint() // used only during design time
        paint.setColor(java.awt.Color.RED)
        paint.setStyle(java.awt.Paint.Style.STROKE)
        val myTop: Int = getTop()
        val myLeft: Int = getLeft()
        val myBottom: Int = getBottom()
        val myRight: Int = getRight()
        for (box in mBoxViews) {
            val l: Int = box.getLeft() - myLeft
            val t: Int = box.getTop() - myTop
            val r: Int = box.getRight() - myLeft
            val b: Int = box.getBottom() - myTop
            canvas.drawRect(l, 0, r, myBottom - myTop, paint)
            canvas.drawRect(0, t, myRight - myLeft, b, paint)
        }*/
    }

    /**
     * Set chain between boxView horizontally
     */
    private fun setBoxViewHorizontalChains() {
        val gridId = self.getId()
        val maxVal: Int = max(mRows, mColumns)
        val columnWeights = parseWeights(mColumns, columnWeights)
        var params: ConstraintLayout.LayoutParams = params(mBoxViews!![0])
        // chain all the views on the longer side (either horizontal or vertical)
        if (mColumns == 1) {
            clearHParams(mBoxViews!![0])
            params.leftToLeft = gridId
            params.rightToRight = gridId
            mBoxViews!![0]!!.setLayoutParams(params)
            return
        }


        //  chains are grid <- box <-> box <-> box -> grid
        for (i in 0 until mColumns) {
            params = params(mBoxViews!![i])
            clearHParams(mBoxViews!![i])
            if (columnWeights != null) {
                params.horizontalWeight = columnWeights[i]
            }
            if (i > 0) {
                params.leftToRight = mBoxViewIds[i - 1]
            } else {
                params.leftToLeft = gridId
            }
            if (i < mColumns - 1) {
                params.rightToLeft = mBoxViewIds[i + 1]
            } else {
                params.rightToRight = gridId
            }
            if (i > 0) {
                params.leftMargin = mHorizontalGaps.toInt()
            }
            mBoxViews!![i]!!.setLayoutParams(params)
        }
        // excess boxes are connected to grid those sides are not use
        // for efficiency they should be connected to parent
        for (i in mColumns until maxVal) {
            params = params(mBoxViews!![i])
            clearHParams(mBoxViews!![i])
            params.leftToLeft = gridId
            params.rightToRight = gridId
            mBoxViews!![i]!!.setLayoutParams(params)
        }
    }

    /**
     * Set chain between boxView vertically
     */
    private fun setBoxViewVerticalChains() {
        val gridId = self.getId()
        val maxVal: Int = max(mRows, mColumns)
        val rowWeights = parseWeights(mRows, rowWeights)
        var params: ConstraintLayout.LayoutParams
        // chain all the views on the longer side (either horizontal or vertical)
        if (mRows == 1) {
            params = params(mBoxViews!![0])
            clearVParams(mBoxViews!![0])
            params.topToTop = gridId
            params.bottomToBottom = gridId
            mBoxViews!![0]?.setLayoutParams(params)
            return
        }
        // chains are constrained like this: grid <- box <-> box <-> box -> grid
        for (i in 0 until mRows) {
            params = params(mBoxViews!![i])
            clearVParams(mBoxViews!![i])
            if (rowWeights != null) {
                params.verticalWeight = rowWeights[i]
            }
            if (i > 0) {
                params.topToBottom = mBoxViewIds[i - 1]
            } else {
                params.topToTop = gridId
            }
            if (i < mRows - 1) {
                params.bottomToTop = mBoxViewIds[i + 1]
            } else {
                params.bottomToBottom = gridId
            }
            if (i > 0) {
                params.topMargin = mHorizontalGaps.toInt()
            }
            mBoxViews!![i]!!.setLayoutParams(params)
        }

        // excess boxes are connected to grid those sides are not use
        // for efficiency they should be connected to parent
        for (i in mRows until maxVal) {
            params = params(mBoxViews!![i])
            clearVParams(mBoxViews!![i])
            params.topToTop = gridId
            params.bottomToBottom = gridId
            mBoxViews!![i]!!.setLayoutParams(params)
        }
    }

    /**
     * Create a new boxView
     * @return boxView
     */
    private fun makeNewView(): TView {
        val v = context.createView()
        v.setId(self.generateViewId())
        v.setVisibility(TView.INVISIBLE)
        if (DEBUG_BOXES) {
            v.setVisibility(TView.VISIBLE)
        }
        val params: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(0, 0)
        mContainer?.self?.addView(v, params)
        return v
    }

    /**
     * Clear vertical related layout params
     * @param view view that has the layout params to be cleared
     */
    private fun clearVParams(view: TView?) {
        val params: ConstraintLayout.LayoutParams = params(view)
        params.verticalWeight = ConstraintSet.UNSET.toFloat()
        params.topToBottom = ConstraintSet.UNSET_ID
        params.topToTop = ConstraintSet.UNSET_ID
        params.bottomToTop = ConstraintSet.UNSET_ID
        params.bottomToBottom = ConstraintSet.UNSET_ID
        params.topMargin = ConstraintSet.UNSET
        view?.setLayoutParams(params)
    }

    /**
     * Clear horizontal related layout params
     * @param view view that has the layout params to be cleared
     */
    private fun clearHParams(view: TView?) {
        val params: ConstraintLayout.LayoutParams = params(view)
        params.horizontalWeight = ConstraintSet.UNSET.toFloat()
        params.leftToRight = ConstraintSet.UNSET_ID
        params.leftToLeft = ConstraintSet.UNSET_ID
        params.rightToLeft = ConstraintSet.UNSET_ID
        params.rightToRight = ConstraintSet.UNSET_ID
        params.leftMargin = ConstraintSet.UNSET
        view?.setLayoutParams(params)
    }

    /**
     * create boxViews for constraining widgets
     */
    private fun buildBoxes() {
        val boxCount: Int = max(mRows, mColumns)
        if (mBoxViews == null) { // no box views build all
            mBoxViews = arrayOfNulls(boxCount)
            for (i in mBoxViews!!.indices) {
                mBoxViews!![i] = makeNewView() // need to remove old Views
            }
        } else {
            if (boxCount != mBoxViews!!.size) {
                val temp: Array<TView?> =
                    arrayOfNulls<TView>(boxCount)
                for (i in 0 until boxCount) {
                    if (i < mBoxViews!!.size) { // use old one
                        temp[i] = mBoxViews!![i]
                    } else { // make new one
                        temp[i] = makeNewView()
                    }
                }
                // remove excess
                for (j in boxCount until mBoxViews!!.size) {
                    val view: TView? = mBoxViews!![j]
                    if(view != null)
                        mContainer?.self?.removeView(view)
                }
                mBoxViews = temp
            }
        }
        mBoxViewIds = Array(boxCount) { "" }
        for (i in mBoxViews!!.indices) {
            mBoxViewIds[i] = mBoxViews!![i]?.getId() ?: ""
        }
        setBoxViewVerticalChains()
        setBoxViewHorizontalChains()
    }
    /**
     * get the value of rows
     *
     * @return the value of rows
     */
    /**
     * set new rows value and also invoke initVariables and invalidate
     *
     * @param rows new rows value
     */
    var rows: Int
        get() = mRowsSet
        set(rows) {
            if (rows > mMaxRows) {
                return
            }
            if (mRowsSet == rows) {
                return
            }
            mRowsSet = rows
            updateActualRowsAndColumns()
            initVariables()
            generateGrid(false)
            self.invalidate()
        }
    /**
     * get the value of columns
     *
     * @return the value of columns
     */
    /**
     * set new columns value and also invoke initVariables and invalidate
     *
     * @param columns new rows value
     */
    var columns: Int
        get() = mColumnsSet
        set(columns) {
            if (columns > mMaxColumns) {
                return
            }
            if (mColumnsSet == columns) {
                return
            }
            mColumnsSet = columns
            updateActualRowsAndColumns()
            initVariables()
            generateGrid(false)
            self.invalidate()
        }
    /**
     * get the value of orientation
     *
     * @return the value of orientation
     */
    /**
     * set new orientation value and also invoke invalidate
     *
     * @param orientation new orientation value
     */
    var orientation: Int
        get() = mOrientation
        set(orientation) {
            if (!(orientation == HORIZONTAL || orientation == VERTICAL)) {
                return
            }
            if (mOrientation == orientation) {
                return
            }
            mOrientation = orientation
            generateGrid(true)
            self.invalidate()
        }

    /**
     * set new spans value and also invoke invalidate
     *
     * @param spans new spans value
     */
    fun setSpans(spans: CharSequence) {
        if (!isSpansValid(spans)) {
            return
        }
        if (this.spans != null && spans.contentEquals(spans)) {
            return
        }
        this.spans = spans.toString()
        generateGrid(true)
        self.invalidate()
    }

    /**
     * set new skips value and also invoke invalidate
     *
     * @param skips new spans value
     */
    fun setSkips(skips: String) {
        if (!isSpansValid(skips)) {
            return
        }
        if (this.skips != null && this.skips == skips) {
            return
        }
        this.skips = skips
        generateGrid(true)
        self.invalidate()
    }

    /**
     * set new rowWeights value and also invoke invalidate
     *
     * @param rowWeights new rowWeights value
     */
    fun setRowWeights(rowWeights: String) {
        if (!isWeightsValid(rowWeights)) {
            return
        }
        if (this.rowWeights != null && this.rowWeights == rowWeights) {
            return
        }
        this.rowWeights = rowWeights
        generateGrid(true)
        self.invalidate()
    }

    /**
     * set new columnWeights value and also invoke invalidate
     *
     * @param columnWeights new columnWeights value
     */
    fun setColumnWeights(columnWeights: String) {
        if (!isWeightsValid(columnWeights)) {
            return
        }
        if (this.columnWeights != null && this.columnWeights == columnWeights) {
            return
        }
        this.columnWeights = columnWeights
        generateGrid(true)
        self.invalidate()
    }
    /**
     * get the value of horizontalGaps
     *
     * @return the value of horizontalGaps
     */
    /**
     * set new horizontalGaps value and also invoke invalidate
     *
     * @param horizontalGaps new horizontalGaps value
     */
    var horizontalGaps: Float
        get() = mHorizontalGaps
        set(horizontalGaps) {
            if (horizontalGaps < 0) {
                return
            }
            if (mHorizontalGaps == horizontalGaps) {
                return
            }
            mHorizontalGaps = horizontalGaps
            generateGrid(true)
            self.invalidate()
        }
    /**
     * get the value of verticalGaps
     *
     * @return the value of verticalGaps
     */
    /**
     * set new verticalGaps value and also invoke invalidate
     *
     * @param verticalGaps new verticalGaps value
     */
    var verticalGaps: Float
        get() = mVerticalGaps
        set(verticalGaps) {
            if (verticalGaps < 0) {
                return
            }
            if (mVerticalGaps == verticalGaps) {
                return
            }
            mVerticalGaps = verticalGaps
            generateGrid(true)
            self.invalidate()
        }

    companion object {
        private const val TAG = "Grid"
        const val VERTICAL = 1
        const val HORIZONTAL = 0
        private const val DEBUG_BOXES = false
    }
}