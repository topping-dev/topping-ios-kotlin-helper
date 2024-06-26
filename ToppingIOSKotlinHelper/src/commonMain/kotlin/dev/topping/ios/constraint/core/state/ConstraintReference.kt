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

import dev.topping.ios.constraint.core.motion.utils.TypedBundle
import dev.topping.ios.constraint.core.motion.utils.TypedValues
import dev.topping.ios.constraint.core.state.helpers.Facade
import dev.topping.ios.constraint.core.widgets.*

open class ConstraintReference(state: State) : Reference {
    private var mKey: Any? = null

    override var key: Any?
        get() = mKey
        set(key) {
            mKey = key
        }

    interface ConstraintReferenceFactory {
        // @TODO: add description
        fun create(state: State?): ConstraintReference?
    }

    val mState: State
    var tag: String? = null
    var mFacade: Facade? = null
    var horizontalChainStyle: Int = ConstraintWidget.CHAIN_SPREAD
    var mVerticalChainStyle: Int = ConstraintWidget.CHAIN_SPREAD
    var horizontalChainWeight: Float = ConstraintWidget.Companion.UNKNOWN.toFloat()
    var verticalChainWeight: Float = ConstraintWidget.Companion.UNKNOWN.toFloat()
    protected var mHorizontalBias = 0.5f
    protected var mVerticalBias = 0.5f
    protected var mMarginLeft = 0
    protected var mMarginRight = 0
    protected var mMarginStart = 0
    protected var mMarginEnd = 0
    protected var mMarginTop = 0
    protected var mMarginBottom = 0
    protected var mMarginLeftGone = 0
    protected var mMarginRightGone = 0
    protected var mMarginStartGone = 0
    protected var mMarginEndGone = 0
    protected var mMarginTopGone = 0
    protected var mMarginBottomGone = 0
    var mMarginBaseline = 0
    var mMarginBaselineGone = 0
    var pivotX = Float.NaN
    var pivotY = Float.NaN
    var rotationX = Float.NaN
    var rotationY = Float.NaN
    var rotationZ = Float.NaN
    var translationX = Float.NaN
    var translationY = Float.NaN
    var translationZ = Float.NaN
    var alpha = Float.NaN
    var scaleX = Float.NaN
    var scaleY = Float.NaN
    var mVisibility: Int = ConstraintWidget.VISIBLE
    protected var mLeftToLeft: Any? = null
    protected var mLeftToRight: Any? = null
    protected var mRightToLeft: Any? = null
    protected var mRightToRight: Any? = null
    protected var mStartToStart: Any? = null
    protected var mStartToEnd: Any? = null
    protected var mEndToStart: Any? = null
    protected var mEndToEnd: Any? = null
    protected var mTopToTop: Any? = null
    protected var mTopToBottom: Any? = null

    var mTopToBaseline: Any? = null
    protected var mBottomToTop: Any? = null
    protected var mBottomToBottom: Any? = null

    var mBottomToBaseline: Any? = null
    var mBaselineToBaseline: Any? = null
    var mBaselineToTop: Any? = null
    var mBaselineToBottom: Any? = null
    var mCircularConstraint: Any? = null
    private var mCircularAngle = 0f
    private var mCircularDistance = 0f
    var mLast: State.Constraint? = null
    var mHorizontalDimension: Dimension = Dimension.createFixed(Dimension.WRAP_DIMENSION)
    var mVerticalDimension: Dimension = Dimension.createFixed(Dimension.WRAP_DIMENSION)
    private var mView: Any? = null
    private var mConstraintWidget: ConstraintWidget? = null
    private val mCustomColors: MutableMap<String, Int>? = mutableMapOf()
    private var mCustomFloats: MutableMap<String, Float>? = mutableMapOf()
    var mMotionProperties: TypedBundle? = null

    // @TODO: add description
    var view: Any?
        get() = mView
        set(view) {
            mView = view
            if (mConstraintWidget != null) {
                mConstraintWidget!!.companionWidget = mView
            }
        }

    // @TODO: add description
    override var facade: Facade?
        get() = mFacade
        set(facade) {
            mFacade = facade
            if (facade != null) {
                constraintWidget = facade.constraintWidget
            }
        }

    // @TODO: add description
    override var constraintWidget: ConstraintWidget?
        get() {
            if (mConstraintWidget == null) {
                mConstraintWidget = createConstraintWidget()
                mConstraintWidget!!.companionWidget = mView
            }
            return mConstraintWidget
        }
        set(widget) {
            if (widget == null) {
                return
            }
            mConstraintWidget = widget
            mConstraintWidget!!.companionWidget = mView
        }

    // @TODO: add description
    fun createConstraintWidget(): ConstraintWidget {
        return ConstraintWidget(
            width.value,
            height.value
        )
    }

    internal class IncorrectConstraintException(val errors: ArrayList<String>) : Exception() {

        override val message: String
            get() = toString()

        
        override fun toString(): String {
            return "IncorrectConstraintException: $errors"
        }
    }

    /**
     * Validate the constraints
     */
    @Throws(IncorrectConstraintException::class)
    fun validate() {
        val errors: ArrayList<String> = ArrayList()
        if (mLeftToLeft != null && mLeftToRight != null) {
            errors.add("LeftToLeft and LeftToRight both defined")
        }
        if (mRightToLeft != null && mRightToRight != null) {
            errors.add("RightToLeft and RightToRight both defined")
        }
        if (mStartToStart != null && mStartToEnd != null) {
            errors.add("StartToStart and StartToEnd both defined")
        }
        if (mEndToStart != null && mEndToEnd != null) {
            errors.add("EndToStart and EndToEnd both defined")
        }
        if ((mLeftToLeft != null || mLeftToRight != null || mRightToLeft != null || mRightToRight != null)
            && (mStartToStart != null || mStartToEnd != null || mEndToStart != null || mEndToEnd != null)
        ) {
            errors.add("Both left/right and start/end constraints defined")
        }
        if (errors.size > 0) {
            throw IncorrectConstraintException(errors)
        }
    }

    private operator fun get(reference: Any?): Any? {
        if (reference == null) {
            return null
        }
        return if (reference !is ConstraintReference) {
            mState.reference(reference)
        } else reference
    }

    init {
        mState = state
    }

    fun setVerticalChainStyle(chainStyle: Int) {
        mVerticalChainStyle = chainStyle
    }

    // @TODO: add description
    fun getVerticalChainStyle(chainStyle: Int): Int {
        return mVerticalChainStyle
    }

    // @TODO: add description
    fun clearVertical(): ConstraintReference {
        top().clear()
        baseline().clear()
        bottom().clear()
        return this
    }

    // @TODO: add description
    fun clearHorizontal(): ConstraintReference {
        start().clear()
        end().clear()
        left().clear()
        right().clear()
        return this
    }

    // @TODO: add description
    fun pivotX(x: Float): ConstraintReference {
        pivotX = x
        return this
    }

    // @TODO: add description
    fun pivotY(y: Float): ConstraintReference {
        pivotY = y
        return this
    }

    // @TODO: add description
    fun rotationX(x: Float): ConstraintReference {
        rotationX = x
        return this
    }

    // @TODO: add description
    fun rotationY(y: Float): ConstraintReference {
        rotationY = y
        return this
    }

    // @TODO: add description
    fun rotationZ(z: Float): ConstraintReference {
        rotationZ = z
        return this
    }

    // @TODO: add description
    fun translationX(x: Float): ConstraintReference {
        translationX = x
        return this
    }

    // @TODO: add description
    fun translationY(y: Float): ConstraintReference {
        translationY = y
        return this
    }

    // @TODO: add description
    fun translationZ(z: Float): ConstraintReference {
        translationZ = z
        return this
    }

    // @TODO: add description
    fun scaleX(x: Float): ConstraintReference {
        scaleX = x
        return this
    }

    // @TODO: add description
    fun scaleY(y: Float): ConstraintReference {
        scaleY = y
        return this
    }

    // @TODO: add description
    fun alpha(alpha: Float): ConstraintReference {
        this.alpha = alpha
        return this
    }

    // @TODO: add description
    fun visibility(visibility: Int): ConstraintReference {
        mVisibility = visibility
        return this
    }

    // @TODO: add description
    fun left(): ConstraintReference {
        mLast = if (mLeftToLeft != null) {
            State.Constraint.LEFT_TO_LEFT
        } else {
            State.Constraint.LEFT_TO_RIGHT
        }
        return this
    }

    // @TODO: add description
    fun right(): ConstraintReference {
        mLast = if (mRightToLeft != null) {
            State.Constraint.RIGHT_TO_LEFT
        } else {
            State.Constraint.RIGHT_TO_RIGHT
        }
        return this
    }

    // @TODO: add description
    fun start(): ConstraintReference {
        mLast = if (mStartToStart != null) {
            State.Constraint.START_TO_START
        } else {
            State.Constraint.START_TO_END
        }
        return this
    }

    // @TODO: add description
    fun end(): ConstraintReference {
        mLast = if (mEndToStart != null) {
            State.Constraint.END_TO_START
        } else {
            State.Constraint.END_TO_END
        }
        return this
    }

    // @TODO: add description
    fun top(): ConstraintReference {
        mLast = if (mTopToTop != null) {
            State.Constraint.TOP_TO_TOP
        } else {
            State.Constraint.TOP_TO_BOTTOM
        }
        return this
    }

    // @TODO: add description
    fun bottom(): ConstraintReference {
        mLast = if (mBottomToTop != null) {
            State.Constraint.BOTTOM_TO_TOP
        } else {
            State.Constraint.BOTTOM_TO_BOTTOM
        }
        return this
    }

    // @TODO: add description
    fun baseline(): ConstraintReference {
        mLast = State.Constraint.BASELINE_TO_BASELINE
        return this
    }

    // @TODO: add description
    fun addCustomColor(name: String, color: Int) {
        mCustomColors!![name] = color
    }

    // @TODO: add description
    fun addCustomFloat(name: String, value: Float) {
        if (mCustomFloats == null) {
            mCustomFloats = HashMap()
        }
        mCustomFloats!![name] = value
    }

    private fun dereference() {
        mLeftToLeft = get(mLeftToLeft)
        mLeftToRight = get(mLeftToRight)
        mRightToLeft = get(mRightToLeft)
        mRightToRight = get(mRightToRight)
        mStartToStart = get(mStartToStart)
        mStartToEnd = get(mStartToEnd)
        mEndToStart = get(mEndToStart)
        mEndToEnd = get(mEndToEnd)
        mTopToTop = get(mTopToTop)
        mTopToBottom = get(mTopToBottom)
        mBottomToTop = get(mBottomToTop)
        mBottomToBottom = get(mBottomToBottom)
        mBaselineToBaseline = get(mBaselineToBaseline)
        mBaselineToTop = get(mBaselineToTop)
        mBaselineToBottom = get(mBaselineToBottom)
    }

    // @TODO: add description
    fun leftToLeft(reference: Any?): ConstraintReference {
        mLast = State.Constraint.LEFT_TO_LEFT
        mLeftToLeft = reference
        return this
    }

    // @TODO: add description
    fun leftToRight(reference: Any?): ConstraintReference {
        mLast = State.Constraint.LEFT_TO_RIGHT
        mLeftToRight = reference
        return this
    }

    // @TODO: add description
    fun rightToLeft(reference: Any?): ConstraintReference {
        mLast = State.Constraint.RIGHT_TO_LEFT
        mRightToLeft = reference
        return this
    }

    // @TODO: add description
    fun rightToRight(reference: Any?): ConstraintReference {
        mLast = State.Constraint.RIGHT_TO_RIGHT
        mRightToRight = reference
        return this
    }

    // @TODO: add description
    fun startToStart(reference: Any?): ConstraintReference {
        mLast = State.Constraint.START_TO_START
        mStartToStart = reference
        return this
    }

    // @TODO: add description
    fun startToEnd(reference: Any?): ConstraintReference {
        mLast = State.Constraint.START_TO_END
        mStartToEnd = reference
        return this
    }

    // @TODO: add description
    fun endToStart(reference: Any?): ConstraintReference {
        mLast = State.Constraint.END_TO_START
        mEndToStart = reference
        return this
    }

    // @TODO: add description
    fun endToEnd(reference: Any?): ConstraintReference {
        mLast = State.Constraint.END_TO_END
        mEndToEnd = reference
        return this
    }

    // @TODO: add description
    fun topToTop(reference: Any?): ConstraintReference {
        mLast = State.Constraint.TOP_TO_TOP
        mTopToTop = reference
        return this
    }

    // @TODO: add description
    fun topToBottom(reference: Any?): ConstraintReference {
        mLast = State.Constraint.TOP_TO_BOTTOM
        mTopToBottom = reference
        return this
    }

    fun topToBaseline(reference: Any?): ConstraintReference {
        mLast = State.Constraint.TOP_TO_BASELINE
        mTopToBaseline = reference
        return this
    }

    // @TODO: add description
    fun bottomToTop(reference: Any?): ConstraintReference {
        mLast = State.Constraint.BOTTOM_TO_TOP
        mBottomToTop = reference
        return this
    }

    // @TODO: add description
    fun bottomToBottom(reference: Any?): ConstraintReference {
        mLast = State.Constraint.BOTTOM_TO_BOTTOM
        mBottomToBottom = reference
        return this
    }

    fun bottomToBaseline(reference: Any?): ConstraintReference {
        mLast = State.Constraint.BOTTOM_TO_BASELINE
        mBottomToBaseline = reference
        return this
    }

    // @TODO: add description
    fun baselineToBaseline(reference: Any?): ConstraintReference {
        mLast = State.Constraint.BASELINE_TO_BASELINE
        mBaselineToBaseline = reference
        return this
    }

    // @TODO: add description
    fun baselineToTop(reference: Any?): ConstraintReference {
        mLast = State.Constraint.BASELINE_TO_TOP
        mBaselineToTop = reference
        return this
    }

    // @TODO: add description
    fun baselineToBottom(reference: Any?): ConstraintReference {
        mLast = State.Constraint.BASELINE_TO_BOTTOM
        mBaselineToBottom = reference
        return this
    }

    // @TODO: add description
    fun centerHorizontally(reference: Any?): ConstraintReference {
        val ref: Any? = get(reference)
        mStartToStart = ref
        mEndToEnd = ref
        mLast = State.Constraint.CENTER_HORIZONTALLY
        mHorizontalBias = 0.5f
        return this
    }

    // @TODO: add description
    fun centerVertically(reference: Any?): ConstraintReference {
        val ref: Any? = get(reference)
        mTopToTop = ref
        mBottomToBottom = ref
        mLast = State.Constraint.CENTER_VERTICALLY
        mVerticalBias = 0.5f
        return this
    }

    // @TODO: add description
    fun circularConstraint(reference: Any?, angle: Float, distance: Float): ConstraintReference {
        val ref: Any? = get(reference)
        mCircularConstraint = ref
        mCircularAngle = angle
        mCircularDistance = distance
        mLast = State.Constraint.CIRCULAR_CONSTRAINT
        return this
    }

    // @TODO: add description
    fun width(dimension: Dimension): ConstraintReference {
        return setWidth(dimension)
    }

    // @TODO: add description
    fun height(dimension: Dimension): ConstraintReference {
        return setHeight(dimension)
    }

    val width: Dimension
        get() = mHorizontalDimension

    // @TODO: add description
    fun setWidth(dimension: Dimension): ConstraintReference {
        mHorizontalDimension = dimension
        return this
    }

    val height: Dimension
        get() = mVerticalDimension

    // @TODO: add description
    fun setHeight(dimension: Dimension): ConstraintReference {
        mVerticalDimension = dimension
        return this
    }

    // @TODO: add description
    open fun margin(marginValue: Any): ConstraintReference {
        return marginInt(mState.convertDimension(marginValue))
    }

    // @TODO: add description
    fun marginGone(marginGoneValue: Any): ConstraintReference {
        return marginGoneInt(mState.convertDimension(marginGoneValue))
    }

    // @TODO: add description
    open fun marginInt(value: Int): ConstraintReference {
        if (mLast != null) {
            when (mLast) {
                State.Constraint.LEFT_TO_LEFT, State.Constraint.LEFT_TO_RIGHT -> {
                    mMarginLeft = value
                }
                State.Constraint.RIGHT_TO_LEFT, State.Constraint.RIGHT_TO_RIGHT -> {
                    mMarginRight = value
                }
                State.Constraint.START_TO_START, State.Constraint.START_TO_END -> {
                    mMarginStart = value
                }
                State.Constraint.END_TO_START, State.Constraint.END_TO_END -> {
                    mMarginEnd = value
                }
                State.Constraint.TOP_TO_TOP, State.Constraint.TOP_TO_BOTTOM, State.Constraint.TOP_TO_BASELINE -> {
                    mMarginTop = value
                }
                State.Constraint.BOTTOM_TO_TOP, State.Constraint.BOTTOM_TO_BOTTOM, State.Constraint.BOTTOM_TO_BASELINE -> {
                    mMarginBottom = value
                }
                State.Constraint.BASELINE_TO_BOTTOM, State.Constraint.BASELINE_TO_TOP, State.Constraint.BASELINE_TO_BASELINE -> {
                    mMarginBaseline = value
                }
                State.Constraint.CIRCULAR_CONSTRAINT -> {
                    mCircularDistance = value.toFloat()
                }
                else -> {}
            }
        } else {
            mMarginLeft = value
            mMarginRight = value
            mMarginStart = value
            mMarginEnd = value
            mMarginTop = value
            mMarginBottom = value
        }
        return this
    }

    // @TODO: add description
    fun marginGoneInt(value: Int): ConstraintReference {
        if (mLast != null) {
            when (mLast) {
                State.Constraint.LEFT_TO_LEFT, State.Constraint.LEFT_TO_RIGHT -> {
                    mMarginLeftGone = value
                }
                State.Constraint.RIGHT_TO_LEFT, State.Constraint.RIGHT_TO_RIGHT -> {
                    mMarginRightGone = value
                }
                State.Constraint.START_TO_START, State.Constraint.START_TO_END -> {
                    mMarginStartGone = value
                }
                State.Constraint.END_TO_START, State.Constraint.END_TO_END -> {
                    mMarginEndGone = value
                }
                State.Constraint.TOP_TO_TOP, State.Constraint.TOP_TO_BOTTOM, State.Constraint.TOP_TO_BASELINE -> {
                    mMarginTopGone = value
                }
                State.Constraint.BOTTOM_TO_TOP, State.Constraint.BOTTOM_TO_BOTTOM, State.Constraint.BOTTOM_TO_BASELINE -> {
                    mMarginBottomGone = value
                }
                State.Constraint.BASELINE_TO_TOP, State.Constraint.BASELINE_TO_BOTTOM, State.Constraint.BASELINE_TO_BASELINE -> {
                    mMarginBaselineGone = value
                }
                else -> {}
            }
        } else {
            mMarginLeftGone = value
            mMarginRightGone = value
            mMarginStartGone = value
            mMarginEndGone = value
            mMarginTopGone = value
            mMarginBottomGone = value
        }
        return this
    }

    // @TODO: add description
    fun horizontalBias(value: Float): ConstraintReference {
        mHorizontalBias = value
        return this
    }

    // @TODO: add description
    fun verticalBias(value: Float): ConstraintReference {
        mVerticalBias = value
        return this
    }

    // @TODO: add description
    open fun bias(value: Float): ConstraintReference {
        if (mLast == null) {
            return this
        }
        when (mLast) {
            State.Constraint.CENTER_HORIZONTALLY, State.Constraint.LEFT_TO_LEFT, State.Constraint.LEFT_TO_RIGHT, State.Constraint.RIGHT_TO_LEFT, State.Constraint.RIGHT_TO_RIGHT, State.Constraint.START_TO_START, State.Constraint.START_TO_END, State.Constraint.END_TO_START, State.Constraint.END_TO_END -> {
                mHorizontalBias = value
            }
            State.Constraint.CENTER_VERTICALLY, State.Constraint.TOP_TO_TOP, State.Constraint.TOP_TO_BOTTOM, State.Constraint.TOP_TO_BASELINE, State.Constraint.BOTTOM_TO_TOP, State.Constraint.BOTTOM_TO_BOTTOM, State.Constraint.BOTTOM_TO_BASELINE -> {
                mVerticalBias = value
            }
            else -> {}
        }
        return this
    }

    /**
     * Clears all constraints.
     */
    fun clearAll(): ConstraintReference {
        mLeftToLeft = null
        mLeftToRight = null
        mMarginLeft = 0
        mRightToLeft = null
        mRightToRight = null
        mMarginRight = 0
        mStartToStart = null
        mStartToEnd = null
        mMarginStart = 0
        mEndToStart = null
        mEndToEnd = null
        mMarginEnd = 0
        mTopToTop = null
        mTopToBottom = null
        mMarginTop = 0
        mBottomToTop = null
        mBottomToBottom = null
        mMarginBottom = 0
        mBaselineToBaseline = null
        mCircularConstraint = null
        mHorizontalBias = 0.5f
        mVerticalBias = 0.5f
        mMarginLeftGone = 0
        mMarginRightGone = 0
        mMarginStartGone = 0
        mMarginEndGone = 0
        mMarginTopGone = 0
        mMarginBottomGone = 0
        return this
    }

    // @TODO: add description
    fun clear(): ConstraintReference {
        if (mLast != null) {
            when (mLast) {
                State.Constraint.LEFT_TO_LEFT, State.Constraint.LEFT_TO_RIGHT -> {
                    mLeftToLeft = null
                    mLeftToRight = null
                    mMarginLeft = 0
                    mMarginLeftGone = 0
                }
                State.Constraint.RIGHT_TO_LEFT, State.Constraint.RIGHT_TO_RIGHT -> {
                    mRightToLeft = null
                    mRightToRight = null
                    mMarginRight = 0
                    mMarginRightGone = 0
                }
                State.Constraint.START_TO_START, State.Constraint.START_TO_END -> {
                    mStartToStart = null
                    mStartToEnd = null
                    mMarginStart = 0
                    mMarginStartGone = 0
                }
                State.Constraint.END_TO_START, State.Constraint.END_TO_END -> {
                    mEndToStart = null
                    mEndToEnd = null
                    mMarginEnd = 0
                    mMarginEndGone = 0
                }
                State.Constraint.TOP_TO_TOP, State.Constraint.TOP_TO_BOTTOM, State.Constraint.TOP_TO_BASELINE -> {
                    mTopToTop = null
                    mTopToBottom = null
                    mTopToBaseline = null
                    mMarginTop = 0
                    mMarginTopGone = 0
                }
                State.Constraint.BOTTOM_TO_TOP, State.Constraint.BOTTOM_TO_BOTTOM, State.Constraint.BOTTOM_TO_BASELINE -> {
                    mBottomToTop = null
                    mBottomToBottom = null
                    mBottomToBaseline = null
                    mMarginBottom = 0
                    mMarginBottomGone = 0
                }
                State.Constraint.BASELINE_TO_BASELINE -> {
                    mBaselineToBaseline = null
                }
                State.Constraint.CIRCULAR_CONSTRAINT -> {
                    mCircularConstraint = null
                }
                else -> {}
            }
        } else {
            clearAll()
        }
        return this
    }

    private fun getTarget(target: Any?): ConstraintWidget? {
        if (target is Reference) {
            return target.constraintWidget
        }
        return null
    }

    private fun applyConnection(
        widget: ConstraintWidget?,
        opaqueTarget: Any?,
        type: State.Constraint
    ) {
        val target: ConstraintWidget = getTarget(opaqueTarget) ?: return
        if(widget == null) return
        when (type) {
            State.Constraint.START_TO_START -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT
                    ), mMarginStart, mMarginStartGone, false
                )
            }
            State.Constraint.START_TO_END -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT
                    ), mMarginStart, mMarginStartGone, false
                )
            }
            State.Constraint.END_TO_START -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT
                    ), mMarginEnd, mMarginEndGone, false
                )
            }
            State.Constraint.END_TO_END -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT
                    ), mMarginEnd, mMarginEndGone, false
                )
            }
            State.Constraint.LEFT_TO_LEFT -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT
                    ), mMarginLeft, mMarginLeftGone, false
                )
            }
            State.Constraint.LEFT_TO_RIGHT -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT
                    ), mMarginLeft, mMarginLeftGone, false
                )
            }
            State.Constraint.RIGHT_TO_LEFT -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT
                    ), mMarginRight, mMarginRightGone, false
                )
            }
            State.Constraint.RIGHT_TO_RIGHT -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT
                    ), mMarginRight, mMarginRightGone, false
                )
            }
            State.Constraint.TOP_TO_TOP -> {
                widget.getAnchor(ConstraintAnchor.Type.TOP)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.TOP
                    ), mMarginTop, mMarginTopGone, false
                )
            }
            State.Constraint.TOP_TO_BOTTOM -> {
                widget.getAnchor(ConstraintAnchor.Type.TOP)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.BOTTOM
                    ), mMarginTop, mMarginTopGone, false
                )
            }
            State.Constraint.TOP_TO_BASELINE -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.TOP, target,
                    ConstraintAnchor.Type.BASELINE, mMarginTop, mMarginTopGone
                )
            }
            State.Constraint.BOTTOM_TO_TOP -> {
                widget.getAnchor(ConstraintAnchor.Type.BOTTOM)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.TOP
                    ), mMarginBottom, mMarginBottomGone, false
                )
            }
            State.Constraint.BOTTOM_TO_BOTTOM -> {
                widget.getAnchor(ConstraintAnchor.Type.BOTTOM)?.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.BOTTOM
                    ), mMarginBottom, mMarginBottomGone, false
                )
            }
            State.Constraint.BOTTOM_TO_BASELINE -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BOTTOM, target,
                    ConstraintAnchor.Type.BASELINE, mMarginBottom, mMarginBottomGone
                )
            }
            State.Constraint.BASELINE_TO_BASELINE -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BASELINE, target,
                    ConstraintAnchor.Type.BASELINE, mMarginBaseline, mMarginBaselineGone
                )
            }
            State.Constraint.BASELINE_TO_TOP -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BASELINE,
                    target, ConstraintAnchor.Type.TOP, mMarginBaseline, mMarginBaselineGone
                )
            }
            State.Constraint.BASELINE_TO_BOTTOM -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BASELINE, target,
                    ConstraintAnchor.Type.BOTTOM, mMarginBaseline, mMarginBaselineGone
                )
            }
            State.Constraint.CIRCULAR_CONSTRAINT -> {
                widget.connectCircularConstraint(target, mCircularAngle, mCircularDistance.toInt())
            }
            else -> {}
        }
    }

    /**
     * apply all the constraints attributes of the mConstraintWidget
     */
    fun applyWidgetConstraints() {
        applyConnection(mConstraintWidget, mLeftToLeft, State.Constraint.LEFT_TO_LEFT)
        applyConnection(mConstraintWidget, mLeftToRight, State.Constraint.LEFT_TO_RIGHT)
        applyConnection(mConstraintWidget, mRightToLeft, State.Constraint.RIGHT_TO_LEFT)
        applyConnection(mConstraintWidget, mRightToRight, State.Constraint.RIGHT_TO_RIGHT)
        applyConnection(mConstraintWidget, mStartToStart, State.Constraint.START_TO_START)
        applyConnection(mConstraintWidget, mStartToEnd, State.Constraint.START_TO_END)
        applyConnection(mConstraintWidget, mEndToStart, State.Constraint.END_TO_START)
        applyConnection(mConstraintWidget, mEndToEnd, State.Constraint.END_TO_END)
        applyConnection(mConstraintWidget, mTopToTop, State.Constraint.TOP_TO_TOP)
        applyConnection(mConstraintWidget, mTopToBottom, State.Constraint.TOP_TO_BOTTOM)
        applyConnection(mConstraintWidget, mTopToBaseline, State.Constraint.TOP_TO_BASELINE)
        applyConnection(mConstraintWidget, mBottomToTop, State.Constraint.BOTTOM_TO_TOP)
        applyConnection(mConstraintWidget, mBottomToBottom, State.Constraint.BOTTOM_TO_BOTTOM)
        applyConnection(mConstraintWidget, mBottomToBaseline, State.Constraint.BOTTOM_TO_BASELINE)
        applyConnection(
            mConstraintWidget, mBaselineToBaseline,
            State.Constraint.BASELINE_TO_BASELINE
        )
        applyConnection(mConstraintWidget, mBaselineToTop, State.Constraint.BASELINE_TO_TOP)
        applyConnection(mConstraintWidget, mBaselineToBottom, State.Constraint.BASELINE_TO_BOTTOM)
        applyConnection(
            mConstraintWidget, mCircularConstraint,
            State.Constraint.CIRCULAR_CONSTRAINT
        )
    }

    // @TODO: add description
    
    override fun apply() {
        if (mConstraintWidget == null) {
            return
        }
        if (mFacade != null) {
            mFacade!!.apply()
        }
        mHorizontalDimension.apply(mState, mConstraintWidget, ConstraintWidget.Companion.HORIZONTAL)
        mVerticalDimension.apply(mState, mConstraintWidget, ConstraintWidget.Companion.VERTICAL)
        dereference()
        applyWidgetConstraints()
        if (horizontalChainStyle != ConstraintWidget.CHAIN_SPREAD) {
            mConstraintWidget!!.horizontalChainStyle = horizontalChainStyle
        }
        if (mVerticalChainStyle != ConstraintWidget.CHAIN_SPREAD) {
            mConstraintWidget!!.verticalChainStyle = mVerticalChainStyle
        }
        if (horizontalChainWeight != ConstraintWidget.Companion.UNKNOWN.toFloat()) {
            mConstraintWidget!!.setHorizontalWeight(horizontalChainWeight)
        }
        if (verticalChainWeight != ConstraintWidget.Companion.UNKNOWN.toFloat()) {
            mConstraintWidget!!.setVerticalWeight(verticalChainWeight)
        }
        mConstraintWidget!!.horizontalBiasPercent = mHorizontalBias
        mConstraintWidget!!.verticalBiasPercent = mVerticalBias
        mConstraintWidget!!.frame.pivotX = pivotX
        mConstraintWidget!!.frame.pivotY = pivotY
        mConstraintWidget!!.frame.rotationX = rotationX
        mConstraintWidget!!.frame.rotationY = rotationY
        mConstraintWidget!!.frame.rotationZ = rotationZ
        mConstraintWidget!!.frame.translationX = translationX
        mConstraintWidget!!.frame.translationY = translationY
        mConstraintWidget!!.frame.translationZ = translationZ
        mConstraintWidget!!.frame.scaleX = scaleX
        mConstraintWidget!!.frame.scaleY = scaleY
        mConstraintWidget!!.frame.alpha = alpha
        mConstraintWidget!!.frame.visibility = mVisibility
        mConstraintWidget!!.visibility = mVisibility
        mConstraintWidget!!.frame.setMotionAttributes(mMotionProperties)
        if (mCustomColors != null) {
            for (key in mCustomColors.keys) {
                val color: Int = mCustomColors[key] ?: continue
                mConstraintWidget!!.frame.setCustomAttribute(
                    key,
                    TypedValues.Custom.TYPE_COLOR, color
                )
            }
        }
        if (mCustomFloats != null) {
            for (key in mCustomFloats!!.keys) {
                val value = mCustomFloats!![key] ?: continue
                mConstraintWidget!!.frame.setCustomAttribute(
                    key,
                    TypedValues.Custom.TYPE_FLOAT, value
                )
            }
        }
    }
}