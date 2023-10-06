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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.ViewGroup

/**
 * **Added in 2.0**
 *
 *
 * ConstraintProperties provides an easy to use api to update the layout params
 * of [ConstraintLayout] children
 *
 */
class ConstraintProperties(val context: TContext, val self: TView) {
    var mParams: ConstraintLayout.LayoutParams? = null
    var mView: TView? = null

    /**
     * Center view between the other two widgets.
     *
     * @param firstID      ID of the first widget to connect the left or top of the widget to
     * @param firstSide    the side of the widget to connect to
     * @param firstMargin  the connection margin
     * @param secondId     the ID of the second widget to connect to right or top of the widget to
     * @param secondSide   the side of the widget to connect to
     * @param secondMargin the connection margin
     * @param bias         the ratio between two connections
     * @return this
     */
    fun center(
        firstID: String,
        firstSide: Int,
        firstMargin: Int,
        secondId: String,
        secondSide: Int,
        secondMargin: Int,
        bias: Float
    ): ConstraintProperties {
        // Error checking
        require(firstMargin >= 0) { "margin must be > 0" }
        require(secondMargin >= 0) { "margin must be > 0" }
        require(!(bias <= 0 || bias > 1)) { "bias must be between 0 and 1 inclusive" }
        if (firstSide == LEFT || firstSide == RIGHT) {
            connect(LEFT, firstID, firstSide, firstMargin)
            connect(RIGHT, secondId, secondSide, secondMargin)
            mParams!!.horizontalBias = bias
        } else if (firstSide == START || firstSide == END) {
            connect(START, firstID, firstSide, firstMargin)
            connect(END, secondId, secondSide, secondMargin)
            mParams!!.horizontalBias = bias
        } else {
            connect(TOP, firstID, firstSide, firstMargin)
            connect(BOTTOM, secondId, secondSide, secondMargin)
            mParams!!.verticalBias = bias
        }
        return this
    }

    /**
     * Centers the widget horizontally to the left and right side on another widgets sides.
     *
     * @param leftId      The Id of the widget on the left side
     * @param leftSide    The side of the leftId widget to connect to
     * @param leftMargin  The margin on the left side
     * @param rightId     The Id of the widget on the right side
     * @param rightSide   The side  of the rightId widget to connect to
     * @param rightMargin The margin on the right side
     * @param bias        The ratio of the space on the left vs.
     * right sides 0.5 is centered (default)
     * @return this
     */
    fun centerHorizontally(
        leftId: String,
        leftSide: Int,
        leftMargin: Int,
        rightId: String,
        rightSide: Int,
        rightMargin: Int,
        bias: Float
    ): ConstraintProperties {
        connect(LEFT, leftId, leftSide, leftMargin)
        connect(RIGHT, rightId, rightSide, rightMargin)
        mParams!!.horizontalBias = bias
        return this
    }

    /**
     * Centers the widgets horizontally to the left and right side on another widgets sides.
     *
     * @param startId     The Id of the widget on the start side (left in non rtl languages)
     * @param startSide   The side of the startId widget to connect to
     * @param startMargin The margin on the start side
     * @param endId       The Id of the widget on the start side (left in non rtl languages)
     * @param endSide     The side of the endId widget to connect to
     * @param endMargin   The margin on the end side
     * @param bias        The ratio of the space on the start vs end side 0.5 is centered (default)
     * @return this
     */
    fun centerHorizontallyRtl(
        startId: String,
        startSide: Int,
        startMargin: Int,
        endId: String,
        endSide: Int,
        endMargin: Int,
        bias: Float
    ): ConstraintProperties {
        connect(START, startId, startSide, startMargin)
        connect(END, endId, endSide, endMargin)
        mParams!!.horizontalBias = bias
        return this
    }

    /**
     * Centers the widgets Vertically to the top and bottom side on another widgets sides.
     *
     * @param topId        The Id of the widget on the top side
     * @param topSide      The side of the leftId widget to connect to
     * @param topMargin    The margin on the top side
     * @param bottomId     The Id of the widget on the bottom side
     * @param bottomSide   The side of the bottomId widget to connect to
     * @param bottomMargin The margin on the bottom side
     * @param bias         The ratio of the space on the top vs.
     * bottom sides 0.5 is centered (default)
     * @return this
     */
    fun centerVertically(
        topId: String,
        topSide: Int,
        topMargin: Int,
        bottomId: String,
        bottomSide: Int,
        bottomMargin: Int,
        bias: Float
    ): ConstraintProperties {
        connect(TOP, topId, topSide, topMargin)
        connect(BOTTOM, bottomId, bottomSide, bottomMargin)
        mParams!!.verticalBias = bias
        return this
    }

    /**
     * Centers the view horizontally relative to toView's position.
     *
     * @param toView ID of view to center on (or in)
     * @return this
     */
    fun centerHorizontally(toView: String): ConstraintProperties {
        if (toView == PARENT_ID) {
            center(
                PARENT_ID, ConstraintSet.LEFT, 0, PARENT_ID,
                ConstraintSet.RIGHT, 0, 0.5f
            )
        } else {
            center(
                toView, ConstraintSet.RIGHT, 0, toView,
                ConstraintSet.LEFT, 0, 0.5f
            )
        }
        return this
    }

    /**
     * Centers the view horizontally relative to toView's position.
     *
     * @param toView ID of view to center on (or in)
     * @return this
     */
    fun centerHorizontallyRtl(toView: String): ConstraintProperties {
        if (toView == PARENT_ID) {
            center(
                PARENT_ID, ConstraintSet.START, 0, PARENT_ID,
                ConstraintSet.END, 0, 0.5f
            )
        } else {
            center(
                toView, ConstraintSet.END, 0, toView,
                ConstraintSet.START, 0, 0.5f
            )
        }
        return this
    }

    /**
     * Centers the view vertically relative to toView's position.
     *
     * @param toView ID of view to center on (or in)
     * @return this
     */
    fun centerVertically(toView: String): ConstraintProperties {
        if (toView == PARENT_ID) {
            center(
                PARENT_ID, ConstraintSet.TOP, 0, PARENT_ID,
                ConstraintSet.BOTTOM, 0, 0.5f
            )
        } else {
            center(
                toView, ConstraintSet.BOTTOM, 0, toView,
                ConstraintSet.TOP, 0, 0.5f
            )
        }
        return this
    }

    /**
     * Remove a constraint from this view.
     *
     * @param anchor the Anchor to remove constraint from
     * @return this
     */
    fun removeConstraints(anchor: Int): ConstraintProperties {
        when (anchor) {
            LEFT -> {
                mParams!!.leftToRight = UNSET_ID
                mParams!!.leftToLeft = UNSET_ID
                mParams!!.leftMargin = UNSET
                mParams!!.goneLeftMargin = GONE_UNSET
            }
            RIGHT -> {
                mParams!!.rightToRight = UNSET_ID
                mParams!!.rightToLeft = UNSET_ID
                mParams!!.rightMargin = UNSET
                mParams!!.goneRightMargin = GONE_UNSET
            }
            TOP -> {
                mParams!!.topToBottom = UNSET_ID
                mParams!!.topToTop = UNSET_ID
                mParams!!.topMargin = UNSET
                mParams!!.goneTopMargin = GONE_UNSET
            }
            BOTTOM -> {
                mParams!!.bottomToTop = UNSET_ID
                mParams!!.bottomToBottom = UNSET_ID
                mParams!!.bottomMargin = UNSET
                mParams!!.goneBottomMargin = GONE_UNSET
            }
            BASELINE -> mParams!!.baselineToBaseline = UNSET_ID
            START -> {
                mParams!!.startToEnd = UNSET_ID
                mParams!!.startToStart = UNSET_ID
                mParams!!.setMarginStart(UNSET)
                mParams!!.goneStartMargin = GONE_UNSET
            }
            END -> {
                mParams!!.endToStart = UNSET_ID
                mParams!!.endToEnd = UNSET_ID
                mParams!!.setMarginEnd(UNSET)
                mParams!!.goneEndMargin = GONE_UNSET
            }
            else -> throw IllegalArgumentException("unknown constraint")
        }
        return this
    }

    /**
     * Sets the margin.
     *
     * @param anchor The side to adjust the margin on
     * @param value  The new value for the margin
     * @return this
     */
    fun margin(anchor: Int, value: Int): ConstraintProperties {
        when (anchor) {
            LEFT -> mParams!!.leftMargin = value
            RIGHT -> mParams!!.rightMargin = value
            TOP -> mParams!!.topMargin = value
            BOTTOM -> mParams!!.bottomMargin = value
            BASELINE -> throw IllegalArgumentException("baseline does not support margins")
            START -> mParams!!.setMarginStart(value)
            END -> mParams!!.setMarginEnd(value)
            else -> throw IllegalArgumentException("unknown constraint")
        }
        return this
    }

    /**
     * Sets the gone margin.
     *
     * @param anchor The side to adjust the margin on
     * @param value  The new value for the margin
     * @return this
     */
    fun goneMargin(anchor: Int, value: Int): ConstraintProperties {
        when (anchor) {
            LEFT -> mParams!!.goneLeftMargin = value
            RIGHT -> mParams!!.goneRightMargin = value
            TOP -> mParams!!.goneTopMargin = value
            BOTTOM -> mParams!!.goneBottomMargin = value
            BASELINE -> throw IllegalArgumentException("baseline does not support margins")
            START -> mParams!!.goneStartMargin = value
            END -> mParams!!.goneEndMargin = value
            else -> throw IllegalArgumentException("unknown constraint")
        }
        return this
    }

    /**
     * Adjust the horizontal bias of the view (used with views constrained on left and right).
     *
     * @param bias the new bias 0.5 is in the middle
     * @return this
     */
    fun horizontalBias(bias: Float): ConstraintProperties {
        mParams!!.horizontalBias = bias
        return this
    }

    /**
     * Adjust the vertical bias of the view (used with views constrained on left and right).
     *
     * @param bias the new bias 0.5 is in the middle
     * @return this
     */
    fun verticalBias(bias: Float): ConstraintProperties {
        mParams!!.verticalBias = bias
        return this
    }

    /**
     * Constrains the views aspect ratio.
     * For Example a HD screen is 16 by 9 = 16/(float)9 = 1.777f.
     *
     * @param ratio The ratio of the width to height (width / height)
     * @return this
     */
    fun dimensionRatio(ratio: String?): ConstraintProperties {
        mParams!!.dimensionRatio = ratio
        return this
    }

    /**
     * Adjust the visibility of a view.
     *
     * @param visibility the visibility (TView.VISIBLE, TView.INVISIBLE, TView.GONE)
     * @return this
     */
    fun visibility(visibility: Int): ConstraintProperties {
        mView!!.setVisibility(visibility)
        return this
    }

    /**
     * Adjust the alpha of a view.
     *
     * @param alpha the alpha
     * @return this
     */
    fun alpha(alpha: Float): ConstraintProperties {
        mView!!.setAlpha(alpha)
        return this
    }

    /**
     * Set the elevation of a view.
     *
     * @param elevation the elevation
     * @return this
     */
    fun elevation(elevation: Float): ConstraintProperties {
        mView!!.setElevation(elevation)
        return this
    }

    /**
     * Adjust the post-layout rotation about the Z axis of a view.
     *
     * @param rotation the rotation about the Z axis
     * @return this
     */
    fun rotation(rotation: Float): ConstraintProperties {
        mView!!.setRotation(rotation)
        return this
    }

    /**
     * Adjust the post-layout rotation about the X axis of a view.
     *
     * @param rotationX the rotation about the X axis
     * @return this
     */
    fun rotationX(rotationX: Float): ConstraintProperties {
        mView!!.setRotationX(rotationX)
        return this
    }

    /**
     * Adjust the post-layout rotation about the Y axis of a view.
     *
     * @param rotationY the rotation about the Y axis
     * @return this
     */
    fun rotationY(rotationY: Float): ConstraintProperties {
        mView!!.setRotationY(rotationY)
        return this
    }

    /**
     * Adjust the post-layout scale in X of a view.
     *
     * @param scaleX the scale in X
     * @return this
     */
    fun scaleX(scaleX: Float): ConstraintProperties {
        mView!!.setScaleY(scaleX)
        return this
    }

    /**
     * Adjust the post-layout scale in Y of a view.
     *
     * @param scaleY the scale in Y
     * @return this
     */
    fun scaleY(scaleY: Float): ConstraintProperties {
        return this
    }

    /**
     * Set X location of the pivot point around which the view will rotate and scale.
     *
     * @param transformPivotX X location of the pivot point.
     * @return this
     */
    fun transformPivotX(transformPivotX: Float): ConstraintProperties {
        mView!!.setPivotX(transformPivotX)
        return this
    }

    /**
     * Set Y location of the pivot point around which the view will rotate and scale.
     *
     * @param transformPivotY Y location of the pivot point.
     * @return this
     */
    fun transformPivotY(transformPivotY: Float): ConstraintProperties {
        mView!!.setPivotY(transformPivotY)
        return this
    }

    /**
     * Set X and Y location of the pivot point around which the view will rotate and scale.
     *
     * @param transformPivotX X location of the pivot point.
     * @param transformPivotY Y location of the pivot point.
     * @return this
     */
    fun transformPivot(transformPivotX: Float, transformPivotY: Float): ConstraintProperties {
        mView!!.setPivotX(transformPivotX)
        mView!!.setPivotY(transformPivotY)
        return this
    }

    /**
     * Adjust the post-layout X translation of a view.
     *
     * @param translationX the translation in X
     * @return this
     */
    fun translationX(translationX: Float): ConstraintProperties {
        mView!!.setTranslationX(translationX)
        return this
    }

    /**
     * Adjust the  post-layout Y translation of a view.
     *
     * @param translationY the translation in Y
     * @return this
     */
    fun translationY(translationY: Float): ConstraintProperties {
        mView!!.setTranslationY(translationY)
        return this
    }

    /**
     * Adjust the  post-layout X and Y translation of a view.
     *
     * @param translationX the translation in X
     * @param translationY the translation in Y
     * @return this
     */
    fun translation(translationX: Float, translationY: Float): ConstraintProperties {
        mView!!.setTranslationX(translationX)
        mView!!.setTranslationY(translationY)
        return this
    }

    /**
     * Adjust the post-layout translation in Z of a view.
     * This is the preferred way to adjust the shadow.
     *
     * @param translationZ the translationZ
     * @return this
     */
    fun translationZ(translationZ: Float): ConstraintProperties {
        mView!!.setTranslationZ(translationZ)
        return this
    }

    /**
     * Sets the height of the view.
     *
     * @param height the height of the view
     * @return this
     */
    fun constrainHeight(height: Int): ConstraintProperties {
        mParams!!.height = height
        return this
    }

    /**
     * Sets the width of the view.
     *
     * @param width the width of the view
     * @return this
     */
    fun constrainWidth(width: Int): ConstraintProperties {
        mParams!!.width = width
        return this
    }

    /**
     * Sets the maximum height of the view. It is a dimension, It is only applicable if height is
     * #MATCH_CONSTRAINT}.
     *
     * @param height the maximum height of the view
     * @return this
     */
    fun constrainMaxHeight(height: Int): ConstraintProperties {
        mParams!!.matchConstraintMaxHeight = height
        return this
    }

    /**
     * Sets the maximum width of the view. It is a dimension, It is only applicable if height is
     * #MATCH_CONSTRAINT}.
     *
     * @param width the maximum width of the view
     * @return this
     */
    fun constrainMaxWidth(width: Int): ConstraintProperties {
        mParams!!.matchConstraintMaxWidth = width
        return this
    }

    /**
     * Sets the minimum height of the view. It is a dimension, It is only applicable if height is
     * #MATCH_CONSTRAINT}.
     *
     * @param height the minimum height of the view
     * @return this
     */
    fun constrainMinHeight(height: Int): ConstraintProperties {
        mParams!!.matchConstraintMinHeight = height
        return this
    }

    /**
     * Sets the minimum width of the view. It is a dimension, It is only applicable if height is
     * #MATCH_CONSTRAINT}.
     *
     * @param width the minimum width of the view
     * @return this
     */
    fun constrainMinWidth(width: Int): ConstraintProperties {
        mParams!!.matchConstraintMinWidth = width
        return this
    }

    /**
     * Sets how the height is calculated ether MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD.
     * Default is spread.
     *
     * @param height MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD
     * @return this
     */
    fun constrainDefaultHeight(height: Int): ConstraintProperties {
        mParams!!.matchConstraintDefaultHeight = height
        return this
    }

    /**
     * Sets how the width is calculated ether MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD.
     * Default is spread.
     *
     * @param width MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD
     * @return this
     */
    fun constrainDefaultWidth(width: Int): ConstraintProperties {
        mParams!!.matchConstraintDefaultWidth = width
        return this
    }

    /**
     * The child's weight that we can use to distribute the available horizontal space
     * in a chain, if the dimension behaviour is set to MATCH_CONSTRAINT
     *
     * @param weight the weight that we can use to distribute the horizontal space
     * @return this
     */
    fun horizontalWeight(weight: Float): ConstraintProperties {
        mParams!!.horizontalWeight = weight
        return this
    }

    /**
     * The child's weight that we can use to distribute the available vertical space
     * in a chain, if the dimension behaviour is set to MATCH_CONSTRAINT
     *
     * @param weight the weight that we can use to distribute the vertical space
     * @return this
     */
    fun verticalWeight(weight: Float): ConstraintProperties {
        mParams!!.verticalWeight = weight
        return this
    }

    /**
     * How the elements of the horizontal chain will be positioned. If the dimension
     * behaviour is set to MATCH_CONSTRAINT. The possible values are:
     *
     *
     *
     *  * CHAIN_SPREAD -- the elements will be spread out
     *  * CHAIN_SPREAD_INSIDE -- similar, but the endpoints of the
     * chain will not be spread out
     *  * CHAIN_PACKED -- the elements of the chain will be packed together. The horizontal
     * bias attribute of the child will then affect the positioning of the packed elements
     *
     *
     * @param chainStyle the weight that we can use to distribute the horizontal space
     * @return this
     */
    fun horizontalChainStyle(chainStyle: Int): ConstraintProperties {
        mParams!!.horizontalChainStyle = chainStyle
        return this
    }

    /**
     * How the elements of the vertical chain will be positioned. in a chain, if the dimension
     * behaviour is set to MATCH_CONSTRAINT
     *
     *
     *
     *  * CHAIN_SPREAD -- the elements will be spread out
     *  * CHAIN_SPREAD_INSIDE -- similar, but the endpoints of the
     * chain will not be spread out
     *  * CHAIN_PACKED -- the elements of the chain will be packed together. The horizontal
     * bias attribute of the child will then affect the positioning of the packed elements
     *
     *
     * @param chainStyle the weight that we can use to distribute the horizontal space
     * @return this
     */
    fun verticalChainStyle(chainStyle: Int): ConstraintProperties {
        mParams!!.verticalChainStyle = chainStyle
        return this
    }

    /**
     * Adds the view to a horizontal chain.
     *
     * @param leftId  id of the view in chain to the left
     * @param rightId id of the view in chain to the right
     * @return this
     */
    fun addToHorizontalChain(leftId: String, rightId: String): ConstraintProperties {
        connect(LEFT, leftId, if (leftId == PARENT_ID) LEFT else RIGHT, 0)
        connect(RIGHT, rightId, if (rightId == PARENT_ID) RIGHT else LEFT, 0)
        if (leftId != PARENT_ID) {
            val leftView: TView = (mView!!.getParent() as TView).findViewById(leftId)!!
            val leftProp = ConstraintProperties(context, leftView)
            leftProp.connect(RIGHT, mView!!.getId(), LEFT, 0)
        }
        if (rightId != PARENT_ID) {
            val rightView: TView = (mView!!.getParent() as TView).findViewById(rightId)!!
            val rightProp = ConstraintProperties(context, rightView)
            rightProp.connect(LEFT, mView!!.getId(), RIGHT, 0)
        }
        return this
    }

    /**
     * Adds the view to a horizontal chain using RTL attributes.
     *
     * @param leftId  id of the view in chain to the left
     * @param rightId id of the view in chain to the right
     * @return this
     */
    fun addToHorizontalChainRTL(leftId: String, rightId: String): ConstraintProperties {
        connect(START, leftId, if (leftId == PARENT_ID) START else END, 0)
        connect(END, rightId, if (rightId == PARENT_ID) END else START, 0)
        if (leftId != PARENT_ID) {
            val leftView: TView = (mView!!.getParent() as TView).findViewById(leftId)!!
            val leftProp = ConstraintProperties(context, leftView)
            leftProp.connect(END, mView!!.getId(), START, 0)
        }
        if (rightId != PARENT_ID) {
            val rightView: TView = (mView!!.getParent() as TView).findViewById(rightId)!!
            val rightProp = ConstraintProperties(context, rightView)
            rightProp.connect(START, mView!!.getId(), END, 0)
        }
        return this
    }

    /**
     * Adds a view to a vertical chain.
     *
     * @param topId    view above.
     * @param bottomId view below
     * @return this
     */
    fun addToVerticalChain(topId: String, bottomId: String): ConstraintProperties {
        connect(TOP, topId, if (topId == PARENT_ID) TOP else BOTTOM, 0)
        connect(BOTTOM, bottomId, if (bottomId == PARENT_ID) BOTTOM else TOP, 0)
        if (topId != PARENT_ID) {
            val topView: TView = (mView!!.getParent() as TView).findViewById(topId)!!
            val topProp = ConstraintProperties(context, topView)
            topProp.connect(BOTTOM, mView!!.getId(), TOP, 0)
        }
        if (bottomId != PARENT_ID) {
            val bottomView: TView = (mView!!.getParent() as TView).findViewById(bottomId)!!
            val bottomProp = ConstraintProperties(context, bottomView)
            bottomProp.connect(TOP, mView!!.getId(), BOTTOM, 0)
        }
        return this
    }

    /**
     * Removes a view from a vertical chain.
     * This assumes the view is connected to a vertical chain.
     * Its behaviour is undefined if not part of a vertical chain.
     *
     * @return this
     */
    fun removeFromVerticalChain(): ConstraintProperties {
        val topId = mParams!!.topToBottom
        val bottomId = mParams!!.bottomToTop
        if (topId != UNSET_ID || bottomId != UNSET_ID) {
            val topView: TView = (mView!!.getParent() as TView).findViewById(topId)!!
            val topProp = ConstraintProperties(context, topView)
            val bottomView: TView = (mView!!.getParent() as TView).findViewById(bottomId)!!
            val bottomProp = ConstraintProperties(context, bottomView)
            if (topId != UNSET_ID && bottomId != UNSET_ID) {
                // top and bottom connected to views
                topProp.connect(BOTTOM, bottomId, TOP, 0)
                bottomProp.connect(TOP, topId, BOTTOM, 0)
            } else if (topId != UNSET_ID || bottomId != UNSET_ID) {
                if (mParams!!.bottomToBottom !== UNSET_ID) {
                    // top connected to view. Bottom connected to parent
                    topProp.connect(BOTTOM, mParams!!.bottomToBottom, BOTTOM, 0)
                } else if (mParams!!.topToTop !== UNSET_ID) {
                    // bottom connected to view. Top connected to parent
                    bottomProp.connect(TOP, mParams!!.topToTop, TOP, 0)
                }
            }
        }
        removeConstraints(TOP)
        removeConstraints(BOTTOM)
        return this
    }

    /**
     * Removes a view from a vertical chain.
     * This assumes the view is connected to a vertical chain.
     * Its behaviour is undefined if not part of a vertical chain.
     *
     * @return this
     */
    fun removeFromHorizontalChain(): ConstraintProperties {
        val leftId = mParams!!.leftToRight
        val rightId = mParams!!.rightToLeft
        if (leftId != UNSET_ID || rightId != UNSET_ID) {
            val leftView: TView = (mView!!.getParent() as TView).findViewById(leftId)!!
            val leftProp = ConstraintProperties(context, leftView)
            val rightView: TView = (mView!!.getParent() as TView).findViewById(rightId)!!
            val rightProp = ConstraintProperties(context, rightView)
            if (leftId != UNSET_ID && rightId != UNSET_ID) {
                // left and right connected to views
                leftProp.connect(RIGHT, rightId, LEFT, 0)
                rightProp.connect(LEFT, leftId, RIGHT, 0)
            } else if (leftId != UNSET_ID || rightId != UNSET_ID) {
                if (mParams!!.rightToRight !== UNSET_ID) {
                    // left connected to view. right connected to parent
                    leftProp.connect(RIGHT, mParams!!.rightToRight, RIGHT, 0)
                } else if (mParams!!.leftToLeft !== UNSET_ID) {
                    // right connected to view. left connected to parent
                    rightProp.connect(LEFT, mParams!!.leftToLeft, LEFT, 0)
                }
            }
            removeConstraints(LEFT)
            removeConstraints(RIGHT)
        } else {
            val startId = mParams!!.startToEnd
            val endId = mParams!!.endToStart
            if (startId != UNSET_ID || endId != UNSET_ID) {
                val startView: TView = (mView!!.getParent() as TView).findViewById(startId)!!
                val startProp = ConstraintProperties(context, startView)
                val endView: TView = (mView!!.getParent() as TView).findViewById(endId)!!
                val endProp = ConstraintProperties(context, endView)
                if (startId != UNSET_ID && endId != UNSET_ID) {
                    // start and end connected to views
                    startProp.connect(END, endId, START, 0)
                    endProp.connect(START, leftId, END, 0)
                } else if (leftId != UNSET_ID || endId != UNSET_ID) {
                    if (mParams!!.rightToRight !== UNSET_ID) {
                        // left connected to view. right connected to parent
                        startProp.connect(END, mParams!!.rightToRight, END, 0)
                    } else if (mParams!!.leftToLeft !== UNSET_ID) {
                        // right connected to view. left connected to parent
                        endProp.connect(START, mParams!!.leftToLeft, START, 0)
                    }
                }
            }
            removeConstraints(START)
            removeConstraints(END)
        }
        return this
    }

    /**
     * Create a constraint between two widgets.
     *
     * @param startSide the side of the widget to constrain
     * @param endID     the id of the widget to constrain to
     * @param endSide   the side of widget to constrain to
     * @param margin    the margin to constrain (margin must be positive)
     */
    fun connect(startSide: Int, endID: String, endSide: Int, margin: Int): ConstraintProperties {
        when (startSide) {
            LEFT -> {
                if (endSide == LEFT) {
                    mParams!!.leftToLeft = endID
                    mParams!!.leftToRight = UNSET_ID
                } else if (endSide == RIGHT) {
                    mParams!!.leftToRight = endID
                    mParams!!.leftToLeft = UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "Left to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                mParams!!.leftMargin = margin
            }
            RIGHT -> {
                if (endSide == LEFT) {
                    mParams!!.rightToLeft = endID
                    mParams!!.rightToRight = UNSET_ID
                } else if (endSide == RIGHT) {
                    mParams!!.rightToRight = endID
                    mParams!!.rightToLeft = UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                mParams!!.rightMargin = margin
            }
            TOP -> {
                if (endSide == TOP) {
                    mParams!!.topToTop = endID
                    mParams!!.topToBottom = UNSET_ID
                    mParams!!.baselineToBaseline = UNSET_ID
                    mParams!!.baselineToTop = UNSET_ID
                    mParams!!.baselineToBottom = UNSET_ID
                } else if (endSide == BOTTOM) {
                    mParams!!.topToBottom = endID
                    mParams!!.topToTop = UNSET_ID
                    mParams!!.baselineToBaseline = UNSET_ID
                    mParams!!.baselineToTop = UNSET_ID
                    mParams!!.baselineToBottom = UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                mParams!!.topMargin = margin
            }
            BOTTOM -> {
                if (endSide == BOTTOM) {
                    mParams!!.bottomToBottom = endID
                    mParams!!.bottomToTop = UNSET_ID
                    mParams!!.baselineToBaseline = UNSET_ID
                    mParams!!.baselineToTop = UNSET_ID
                    mParams!!.baselineToBottom = UNSET_ID
                } else if (endSide == TOP) {
                    mParams!!.bottomToTop = endID
                    mParams!!.bottomToBottom = UNSET_ID
                    mParams!!.baselineToBaseline = UNSET_ID
                    mParams!!.baselineToTop = UNSET_ID
                    mParams!!.baselineToBottom = UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                mParams!!.bottomMargin = margin
            }
            BASELINE -> {
                if (endSide == BASELINE) {
                    mParams!!.baselineToBaseline = endID
                    mParams!!.bottomToBottom = UNSET_ID
                    mParams!!.bottomToTop = UNSET_ID
                    mParams!!.topToTop = UNSET_ID
                    mParams!!.topToBottom = UNSET_ID
                } else if (endSide == TOP) {
                    mParams!!.baselineToTop = endID
                    mParams!!.bottomToBottom = UNSET_ID
                    mParams!!.bottomToTop = UNSET_ID
                    mParams!!.topToTop = UNSET_ID
                    mParams!!.topToBottom = UNSET_ID
                } else if (endSide == BOTTOM) {
                    mParams!!.baselineToBottom = endID
                    mParams!!.bottomToBottom = UNSET_ID
                    mParams!!.bottomToTop = UNSET_ID
                    mParams!!.topToTop = UNSET_ID
                    mParams!!.topToBottom = UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                mParams!!.baselineMargin = margin
            }
            START -> {
                if (endSide == START) {
                    mParams!!.startToStart = endID
                    mParams!!.startToEnd = UNSET_ID
                } else if (endSide == END) {
                    mParams!!.startToEnd = endID
                    mParams!!.startToStart = UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                mParams!!.setMarginStart(margin)
            }
            END -> {
                if (endSide == END) {
                    mParams!!.endToEnd = endID
                    mParams!!.endToStart = UNSET_ID
                } else if (endSide == START) {
                    mParams!!.endToStart = endID
                    mParams!!.endToEnd = UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                mParams!!.setMarginEnd(margin)
            }
            else -> throw IllegalArgumentException(
                sideToString(startSide) + " to " + sideToString(endSide) + " unknown"
            )
        }
        return this
    }

    private fun sideToString(side: Int): String {
        when (side) {
            LEFT -> return "left"
            RIGHT -> return "right"
            TOP -> return "top"
            BOTTOM -> return "bottom"
            BASELINE -> return "baseline"
            START -> return "start"
            END -> return "end"
        }
        return "undefined"
    }

    init {
        val params: ViewGroup.LayoutParams = self.getLayoutParams() as ViewGroup.LayoutParams
        if (params is ConstraintLayout.LayoutParams) {
            mParams = params
            mView = self
        } else {
            throw RuntimeException("Only children of ConstraintLayout.LayoutParams supported")
        }
    }

    /**
     * Should be called to apply the changes currently a no op
     * in place for subclasses and future use
     */
    fun apply() {}

    companion object {
        /**
         * The left side of a view.
         */
        val LEFT: Int = ConstraintLayout.LayoutParams.LEFT

        /**
         * The right side of a view.
         */
        val RIGHT: Int = ConstraintLayout.LayoutParams.RIGHT

        /**
         * The top of a view.
         */
        val TOP: Int = ConstraintLayout.LayoutParams.TOP

        /**
         * The bottom side of a view.
         */
        val BOTTOM: Int = ConstraintLayout.LayoutParams.BOTTOM

        /**
         * The baseline of the text in a view.
         */
        val BASELINE: Int = ConstraintLayout.LayoutParams.BASELINE

        /**
         * The left side of a view in left to right languages.
         * In right to left languages it corresponds to the right side of the view
         */
        val START: Int = ConstraintLayout.LayoutParams.START

        /**
         * The right side of a view in left to right languages.
         * In right to left languages it corresponds to the left side of the view
         */
        val END: Int = ConstraintLayout.LayoutParams.END

        /**
         * Used to indicate a parameter is cleared or not set
         */
        val UNSET: Int = ConstraintLayout.LayoutParams.UNSET
        val UNSET_ID: String = ConstraintLayout.LayoutParams.UNSET_ID
        val GONE_UNSET: Int = ConstraintLayout.LayoutParams.GONE_UNSET

        /**
         * References the id of the parent.
         */
        val PARENT_ID = ConstraintLayout.LayoutParams.PARENT_ID

        /**
         * Dimension will be controlled by constraints
         */
        val MATCH_CONSTRAINT: Int = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT

        /**
         * Dimension will set by the view's content
         */
        val WRAP_CONTENT: Int = TView.WRAP_CONTENT

        /**
         * How to calculate the size of a view in 0 dp by using its wrap_content size
         */
        val MATCH_CONSTRAINT_WRAP: Int = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_WRAP

        /**
         * Calculate the size of a view in 0 dp by reducing the constrains gaps as much as possible
         */
        val MATCH_CONSTRAINT_SPREAD: Int = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD
    }
}