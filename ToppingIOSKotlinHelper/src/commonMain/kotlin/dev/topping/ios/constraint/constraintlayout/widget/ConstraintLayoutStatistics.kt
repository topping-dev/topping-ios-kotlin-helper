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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.DecimalFormat
import dev.topping.ios.constraint.Log
import dev.topping.ios.constraint.core.Metrics
import kotlin.experimental.ExperimentalNativeApi

/**
 * This provide metrics of the complexity of the layout that is being solved.
 * The intent is for developers using the too to track the evolution of their UI
 * Typically the developer will monitor the computations on every callback of
 * mConstraintLayout.addOnLayoutChangeListener(this::callback);
 *
 */
class ConstraintLayoutStatistics {
    private val mMetrics: Metrics = Metrics()
    var mConstraintLayout: ConstraintLayout? = null

    /**
     * Measure performance information about ConstraintLayout
     *
     * @param constraintLayout
     */
    constructor(constraintLayout: ConstraintLayout) {
        attach(constraintLayout)
    }

    /**
     * Copy a layout Stats useful for comparing two stats
     * @param copy
     */
    constructor(copy: ConstraintLayoutStatistics) {
        mMetrics.copy(copy.mMetrics)
    }

    /**
     * Attach to a ConstraintLayout to gather statistics on its layout performance
     * @param constraintLayout
     */
    fun attach(constraintLayout: ConstraintLayout) {
        constraintLayout.fillMetrics(mMetrics)
        mConstraintLayout = constraintLayout
    }

    /**
     * Detach from a ConstraintLayout
     */
    fun detach() {
        if (mConstraintLayout != null) {
            mConstraintLayout!!.fillMetrics(null)
        }
    }

    /**
     * Clear the current metrics
     */
    fun reset() {
        mMetrics.reset()
    }

    /**
     * Create a copy of the statistics
     * @return a copy
     */
    fun clone(): ConstraintLayoutStatistics {
        return ConstraintLayoutStatistics(this)
    }

    /**
     * Format a float value outputting a string of fixed length
     * @param df format to use
     * @param val
     * @param length
     * @return
     */
    private fun fmt(df: DecimalFormat, `val`: Float, length: Int): String {
        var s = CharArray(length).concatToString().replace('\u0000', ' ')
        s = s + df.format(`val`)
        return s.substring(s.length - length)
    }

    /**
     * Log a summary of the statistics
     * @param tag
     */
    fun logSummary(tag: String) {
        log(tag)
    }

    /**
     * Generate a formatted String for the parameter formatting as a float
     * @param df
     * @param param
     * @return
     */
    private fun log(df: DecimalFormat, param: Int): String {
        val value = fmt(df, getValue(param) * 1E-6f, 7)
        var title = geName(param)
        title = WORD_PAD + title
        title = title.substring(title.length - MAX_WORD)
        title += " = "
        return "CL Perf: $title$value"
    }

    @OptIn(ExperimentalNativeApi::class)
    private fun log(tag: String) {
        val s = Throwable().getStackTrace().contentToString()
        Log.v(
            tag, "CL Perf: --------  Performance .(" +
                    s + ")  ------ "
        )
        val df = DecimalFormat("###.000")
        Log.v(tag, log(df, DURATION_OF_CHILD_MEASURES))
        Log.v(tag, log(df, DURATION_OF_LAYOUT))
        Log.v(tag, log(df, DURATION_OF_MEASURES))
        Log.v(tag, log(NUMBER_OF_LAYOUTS))
        Log.v(tag, log(NUMBER_OF_ON_MEASURES))
        Log.v(tag, log(NUMBER_OF_CHILD_VIEWS))
        Log.v(tag, log(NUMBER_OF_CHILD_MEASURES))
        Log.v(tag, log(NUMBER_OF_VARIABLES))
        Log.v(tag, log(NUMBER_OF_EQUATIONS))
        Log.v(tag, log(NUMBER_OF_SIMPLE_EQUATIONS))
    }

    /**
     * Generate a formatted String for the parameter
     * @param param
     * @return
     */
    private fun log(param: Int): String {
        val value = this.getValue(param).toString()
        var title = geName(param)
        title = WORD_PAD + title
        title = title.substring(title.length - MAX_WORD)
        title += " = "
        return "CL Perf: $title$value"
    }

    /**
     * Generate a float formatted String for the parameter comparing
     * current value with value in relative
     * @param df Format the float using this
     * @param relative compare against
     * @param param the parameter to compare
     * @return
     */
    private fun compare(
        df: DecimalFormat,
        relative: ConstraintLayoutStatistics,
        param: Int
    ): String {
        var value = fmt(df, getValue(param) * 1E-6f, 7)
        value += " -> " + fmt(df, relative.getValue(param) * 1E-6f, 7) + "ms"
        var title = geName(param)
        title = WORD_PAD + title
        title = title.substring(title.length - MAX_WORD)
        title += " = "
        return "CL Perf: $title$value"
    }

    /**
     * Generate a formatted String for the parameter comparing current value with value in relative
     * @param relative compare against
     * @param param the parameter to compare
     * @return
     */
    private fun compare(relative: ConstraintLayoutStatistics, param: Int): String {
        val value = this.getValue(param).toString() + " -> " + relative.getValue(param)
        var title = geName(param)
        title = WORD_PAD + title
        title = title.substring(title.length - MAX_WORD)
        title += " = "
        return "CL Perf: $title$value"
    }

    /**
     * log a summary of the stats compared to another statics
     * @param tag used in Log.v(tag, ...)
     * @param prev the previous stats to compare to
     */
    fun logSummary(tag: String, prev: ConstraintLayoutStatistics?) {
        if (prev == null) {
            log(tag)
            return
        }
        val df = DecimalFormat("###.000")
        val s = Throwable().stackTraceToString()
        Log.v(
            tag, "CL Perf: -=  Performance .(" +
                    s + ")  =- "
        )
        Log.v(tag, compare(df, prev, DURATION_OF_CHILD_MEASURES))
        Log.v(tag, compare(df, prev, DURATION_OF_LAYOUT))
        Log.v(tag, compare(df, prev, DURATION_OF_MEASURES))
        Log.v(tag, compare(prev, NUMBER_OF_LAYOUTS))
        Log.v(tag, compare(prev, NUMBER_OF_ON_MEASURES))
        Log.v(tag, compare(prev, NUMBER_OF_CHILD_VIEWS))
        Log.v(tag, compare(prev, NUMBER_OF_CHILD_MEASURES))
        Log.v(tag, compare(prev, NUMBER_OF_VARIABLES))
        Log.v(tag, compare(prev, NUMBER_OF_EQUATIONS))
        Log.v(tag, compare(prev, NUMBER_OF_SIMPLE_EQUATIONS))
    }

    /**
     * get the value of a statistic
     * @param type
     * @return
     */
    fun getValue(type: Int): Long {
        when (type) {
            NUMBER_OF_LAYOUTS -> return mMetrics.mNumberOfLayouts.toLong()
            NUMBER_OF_ON_MEASURES -> return mMetrics.mMeasureCalls
            NUMBER_OF_CHILD_VIEWS -> return mMetrics.mChildCount
            NUMBER_OF_CHILD_MEASURES -> return mMetrics.mNumberOfMeasures.toLong()
            DURATION_OF_CHILD_MEASURES -> return mMetrics.measuresWidgetsDuration
            DURATION_OF_MEASURES -> return mMetrics.mMeasureDuration
            DURATION_OF_LAYOUT -> return mMetrics.measuresLayoutDuration
            NUMBER_OF_VARIABLES -> return mMetrics.mVariables
            NUMBER_OF_EQUATIONS -> return mMetrics.mEquations
            NUMBER_OF_SIMPLE_EQUATIONS -> return mMetrics.mSimpleEquations
        }
        return 0
    }

    /** get a simple name for a statistic
     *
     * @param type type of statistic
     * @return a camel case
     */
    fun geName(type: Int): String {
        when (type) {
            NUMBER_OF_LAYOUTS -> return "NumberOfLayouts"
            NUMBER_OF_ON_MEASURES -> return "MeasureCalls"
            NUMBER_OF_CHILD_VIEWS -> return "ChildCount"
            NUMBER_OF_CHILD_MEASURES -> return "ChildrenMeasures"
            DURATION_OF_CHILD_MEASURES -> return "MeasuresWidgetsDuration "
            DURATION_OF_MEASURES -> return "MeasureDuration"
            DURATION_OF_LAYOUT -> return "MeasuresLayoutDuration"
            NUMBER_OF_VARIABLES -> return "SolverVariables"
            NUMBER_OF_EQUATIONS -> return "SolverEquations"
            NUMBER_OF_SIMPLE_EQUATIONS -> return "SimpleEquations"
        }
        return ""
    }

    companion object {
        const val NUMBER_OF_LAYOUTS = 1
        const val NUMBER_OF_ON_MEASURES = 2
        const val NUMBER_OF_CHILD_VIEWS = 3
        const val NUMBER_OF_CHILD_MEASURES = 4
        const val DURATION_OF_CHILD_MEASURES = 5
        const val DURATION_OF_MEASURES = 6
        const val DURATION_OF_LAYOUT = 7
        const val NUMBER_OF_VARIABLES = 8
        const val NUMBER_OF_EQUATIONS = 9
        const val NUMBER_OF_SIMPLE_EQUATIONS = 10
        private const val MAX_WORD = 25
        private val WORD_PAD = CharArray(MAX_WORD).concatToString().replace('\u0000', ' ')
    }
}