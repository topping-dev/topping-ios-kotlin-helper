/*
 * Copyright (C) 2018 The Android Open Source Project
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

import dev.topping.ios.constraint.core.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
import dev.topping.ios.constraint.core.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
import dev.topping.ios.constraint.core.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD

/**
 * TClass to represent a chain by its main elements.
 */
class ChainHead(first: ConstraintWidget, orientation: Int, isRtl: Boolean) {
    var mFirst: ConstraintWidget
    var mFirstVisibleWidget: ConstraintWidget? = null
    var mLast: ConstraintWidget? = null
    var mLastVisibleWidget: ConstraintWidget? = null
    var mHead: ConstraintWidget? = null
    var mFirstMatchConstraintWidget: ConstraintWidget? = null
    var mLastMatchConstraintWidget: ConstraintWidget? = null
    var mWeightedMatchConstraintsWidgets: ArrayList<ConstraintWidget>? = null
    var mWidgetsCount = 0
    var mWidgetsMatchCount = 0
    var totalWeight = 0f
        protected set
    var mVisibleWidgets = 0
    var mTotalSize = 0
    var mTotalMargins = 0
    var mOptimizable = false
    private val mOrientation: Int
    private var mIsRtl = false
    var mHasUndefinedWeights = false
    protected var mHasDefinedWeights = false
    var mHasComplexMatchWeights = false
    protected var mHasRatio = false
    private var mDefined = false

    /**
     * Initialize variables, then determine visible widgets, the head of chain and
     * matched constraint widgets.
     *
     * @param first       first widget in a chain
     * @param orientation orientation of the chain (either Horizontal or Vertical)
     * @param isRtl       Right-to-left layout flag to determine the actual head of the chain
     */
    init {
        mFirst = first
        mOrientation = orientation
        mIsRtl = isRtl
    }

    private fun defineChainProperties() {
        val offset = mOrientation * 2
        var lastVisited: ConstraintWidget = mFirst
        mOptimizable = true

        // TraverseChain
        var widget: ConstraintWidget = mFirst
        var next: ConstraintWidget? = mFirst
        var done = false
        while (!done) {
            mWidgetsCount++
            widget.mNextChainWidget[mOrientation] = null
            widget.mListNextMatchConstraintsWidget[mOrientation] = null
            if (widget.visibility != ConstraintWidget.GONE) {
                mVisibleWidgets++
                if (widget.getDimensionBehaviour(mOrientation)
                    != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    mTotalSize += widget.getLength(mOrientation)
                }
                mTotalSize += widget.mListAnchors.get(offset).margin
                mTotalSize += widget.mListAnchors.get(offset + 1).margin
                mTotalMargins += widget.mListAnchors.get(offset).margin
                mTotalMargins += widget.mListAnchors.get(offset + 1).margin
                // Visible widgets linked list.
                if (mFirstVisibleWidget == null) {
                    mFirstVisibleWidget = widget
                }
                mLastVisibleWidget = widget

                // Match constraint linked list.
                if (widget.mListDimensionBehaviors.get(mOrientation)
                    == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    if ((widget.mResolvedMatchConstraintDefault.get(mOrientation)
                                == MATCH_CONSTRAINT_SPREAD) || (widget.mResolvedMatchConstraintDefault.get(
                            mOrientation
                        )
                                == MATCH_CONSTRAINT_RATIO) || (widget.mResolvedMatchConstraintDefault.get(
                            mOrientation
                        )
                                == MATCH_CONSTRAINT_PERCENT)
                    ) {
                        mWidgetsMatchCount++
                        // Note: Might cause an issue if we support MATCH_CONSTRAINT_RATIO_RESOLVED
                        // in chain optimization. (we currently don't)
                        val weight: Float = widget.mWeight.get(mOrientation)
                        if (weight > 0) {
                            totalWeight += widget.mWeight.get(mOrientation)
                        }
                        if (isMatchConstraintEqualityCandidate(widget, mOrientation)) {
                            if (weight < 0) {
                                mHasUndefinedWeights = true
                            } else {
                                mHasDefinedWeights = true
                            }
                            if (mWeightedMatchConstraintsWidgets == null) {
                                mWeightedMatchConstraintsWidgets = ArrayList()
                            }
                            mWeightedMatchConstraintsWidgets!!.add(widget)
                        }
                        if (mFirstMatchConstraintWidget == null) {
                            mFirstMatchConstraintWidget = widget
                        }
                        if (mLastMatchConstraintWidget != null) {
                            mLastMatchConstraintWidget!!.mListNextMatchConstraintsWidget[mOrientation] = widget
                        }
                        mLastMatchConstraintWidget = widget
                    }
                    if (mOrientation == ConstraintWidget.HORIZONTAL) {
                        if (widget.mMatchConstraintDefaultWidth
                            != ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                        ) {
                            mOptimizable = false
                        } else if (widget.mMatchConstraintMinWidth != 0
                            || widget.mMatchConstraintMaxWidth != 0
                        ) {
                            mOptimizable = false
                        }
                    } else {
                        if (widget.mMatchConstraintDefaultHeight
                            != ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                        ) {
                            mOptimizable = false
                        } else if (widget.mMatchConstraintMinHeight != 0
                            || widget.mMatchConstraintMaxHeight != 0
                        ) {
                            mOptimizable = false
                        }
                    }
                    if (widget.dimensionRatio != 0.0f) {
                        //TODO: Improve (Could use ratio optimization).
                        mOptimizable = false
                        mHasRatio = true
                    }
                }
            }
            if (lastVisited != widget) {
                lastVisited.mNextChainWidget[mOrientation] = widget
            }
            lastVisited = widget

            // go to the next widget
            val nextAnchor: ConstraintAnchor? = widget.mListAnchors.get(offset + 1).target
            if (nextAnchor != null) {
                next = nextAnchor.mOwner
                if (next.mListAnchors[offset].target == null
                    || next.mListAnchors.get(offset).target?.mOwner != widget
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
        if (mFirstVisibleWidget != null) {
            mTotalSize -= mFirstVisibleWidget!!.mListAnchors.get(offset).margin
        }
        if (mLastVisibleWidget != null) {
            mTotalSize -= mLastVisibleWidget!!.mListAnchors.get(offset + 1).margin
        }
        mLast = widget
        mHead = if (mOrientation == ConstraintWidget.HORIZONTAL && mIsRtl) {
            mLast
        } else {
            mFirst
        }
        mHasComplexMatchWeights = mHasDefinedWeights && mHasUndefinedWeights
    }

    val first: dev.topping.ios.constraint.core.widgets.ConstraintWidget
        get() = mFirst
    val firstVisibleWidget: dev.topping.ios.constraint.core.widgets.ConstraintWidget?
        get() = mFirstVisibleWidget
    val last: dev.topping.ios.constraint.core.widgets.ConstraintWidget?
        get() = mLast
    val lastVisibleWidget: dev.topping.ios.constraint.core.widgets.ConstraintWidget?
        get() = mLastVisibleWidget
    val head: dev.topping.ios.constraint.core.widgets.ConstraintWidget?
        get() = mHead
    val firstMatchConstraintWidget: dev.topping.ios.constraint.core.widgets.ConstraintWidget?
        get() = mFirstMatchConstraintWidget
    val lastMatchConstraintWidget: dev.topping.ios.constraint.core.widgets.ConstraintWidget?
        get() = mLastMatchConstraintWidget

    // @TODO: add description
    fun define() {
        if (!mDefined) {
            defineChainProperties()
        }
        mDefined = true
    }

    companion object {
        /**
         * Returns true if the widget should be part of the match equality rules in the chain
         *
         * @param widget      the widget to test
         * @param orientation current orientation, HORIZONTAL or VERTICAL
         */
        private fun isMatchConstraintEqualityCandidate(
            widget: ConstraintWidget,
            orientation: Int
        ): Boolean {
            return widget.visibility != ConstraintWidget.GONE && (widget.mListDimensionBehaviors.get(
                orientation
            )
                    == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) && (widget.mResolvedMatchConstraintDefault.get(
                orientation
            ) == MATCH_CONSTRAINT_SPREAD
                    || widget.mResolvedMatchConstraintDefault.get(orientation) == MATCH_CONSTRAINT_RATIO)
        }
    }
}