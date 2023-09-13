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
package dev.topping.ios.constraint.core.dsl

import dev.topping.ios.constraint.isNaN

/**
 * Provides the API for creating a Constraint Any for use in the Core
 * ConstraintLayout & MotionLayout system
 */
class Constraint(val id: String) {

    inner class VAnchor internal constructor(side: VSide) : Anchor(Side.valueOf(side.name))
    inner class HAnchor internal constructor(side: HSide) : Anchor(Side.valueOf(side.name))
    open inner class Anchor internal constructor(val mSide: Side) {
        var mConnection: Anchor? = null
        var mMargin = 0
        var mGoneMargin: Int = Int.MIN_VALUE
        val parent: Constraint
            get() = this@Constraint

        fun getId(): String {
            return this@Constraint.id
        }

        fun build(builder: StringBuilder) {
            if (mConnection != null) {
                builder.append(mSide.toString().lowercase())
                    .append(":").append(this).append(",\n")
            }
        }

        override fun toString(): String {
            val ret = StringBuilder("[")
            if (mConnection != null) {
                ret.append("'").append(mConnection!!.getId()).append("',")
                    .append("'").append(mConnection!!.mSide.toString().lowercase()).append("'")
            }
            if (mMargin != 0) {
                ret.append(",").append(mMargin)
            }
            if (mGoneMargin != Int.MIN_VALUE) {
                if (mMargin == 0) {
                    ret.append(",0,").append(mGoneMargin)
                } else {
                    ret.append(",").append(mGoneMargin)
                }
            }
            ret.append("]")
            return ret.toString()
        }
    }

    enum class Behaviour {
        SPREAD, WRAP, PERCENT, RATIO, RESOLVED
    }

    enum class ChainMode {
        SPREAD, SPREAD_INSIDE, PACKED
    }

    enum class VSide {
        TOP, BOTTOM, BASELINE
    }

    enum class HSide {
        LEFT, RIGHT, START, END
    }

    enum class Side {
        LEFT, RIGHT, TOP, BOTTOM, START, END, BASELINE
    }

    var helperType: String? = null
    var helperJason: String? = null

    /**
     * get left anchor
     *
     * @return left anchor
     */
    val left: HAnchor = HAnchor(HSide.LEFT)

    /**
     * get right anchor
     *
     * @return right anchor
     */
    val right: HAnchor = HAnchor(HSide.RIGHT)

    /**
     * get top anchor
     *
     * @return top anchor
     */
    val top: VAnchor = VAnchor(VSide.TOP)

    /**
     * get bottom anchor
     *
     * @return bottom anchor
     */
    val bottom: VAnchor = VAnchor(VSide.BOTTOM)

    /**
     * get start anchor
     *
     * @return start anchor
     */
    val start: HAnchor = HAnchor(HSide.START)

    /**
     * get end anchor
     *
     * @return end anchor
     */
    val end: HAnchor = HAnchor(HSide.END)

    /**
     * get baseline anchor
     *
     * @return baseline anchor
     */
    val baseline: VAnchor = VAnchor(VSide.BASELINE)
    /**
     * get width
     * @return width
     */
    /**
     * set width
     *
     * @param width
     */
    var width = UNSET
    /**
     * get height
     * @return height
     */
    /**
     * set height
     *
     * @param height
     */
    var height = UNSET
    /**
     * get horizontalBias
     *
     * @return horizontalBias
     */
    /**
     * set horizontalBias
     *
     * @param horizontalBias
     */
    var horizontalBias = Float.NaN
    /**
     * get verticalBias
     *
     * @return verticalBias
     */
    /**
     * set verticalBias
     *
     * @param verticalBias
     */
    var verticalBias = Float.NaN
    /**
     * get dimensionRatio
     *
     * @return dimensionRatio
     */
    /**
     * set dimensionRatio
     *
     * @param dimensionRatio
     */
    var dimensionRatio: String? = null
    /**
     * get circleConstraint
     *
     * @return circleConstraint
     */
    /**
     * set circleConstraint
     *
     * @param circleConstraint
     */
    var circleConstraint: String? = null
    /**
     * get circleRadius
     *
     * @return circleRadius
     */
    /**
     * set circleRadius
     *
     * @param circleRadius
     */
    var circleRadius: Int = Int.MIN_VALUE
    /**
     * get circleAngle
     *
     * @return circleAngle
     */
    /**
     * set circleAngle
     *
     * @param circleAngle
     */
    var circleAngle = Float.NaN
    /**
     * get editorAbsoluteX
     * @return editorAbsoluteX
     */
    /**
     * set editorAbsoluteX
     * @param editorAbsoluteX
     */
    var editorAbsoluteX: Int = Int.MIN_VALUE
    /**
     * get editorAbsoluteY
     * @return editorAbsoluteY
     */
    /**
     * set editorAbsoluteY
     * @param editorAbsoluteY
     */
    var editorAbsoluteY: Int = Int.MIN_VALUE
    /**
     * get verticalWeight
     *
     * @return verticalWeight
     */
    /**
     * set verticalWeight
     *
     * @param verticalWeight
     */
    var verticalWeight = Float.NaN
    /**
     * get horizontalWeight
     *
     * @return horizontalWeight
     */
    /**
     * set horizontalWeight
     *
     * @param horizontalWeight
     */
    var horizontalWeight = Float.NaN
    /**
     * get horizontalChainStyle
     *
     * @return horizontalChainStyle
     */
    /**
     * set horizontalChainStyle
     *
     * @param horizontalChainStyle
     */
    var horizontalChainStyle: ChainMode? = null
    /**
     * get verticalChainStyle
     *
     * @return verticalChainStyle
     */
    /**
     * set verticalChainStyle
     *
     * @param verticalChainStyle
     */
    var verticalChainStyle: ChainMode? = null
    /**
     * get widthDefault
     *
     * @return widthDefault
     */
    /**
     * set widthDefault
     *
     * @param widthDefault
     */
    var widthDefault: Behaviour? = null
    /**
     * get heightDefault
     *
     * @return heightDefault
     */
    /**
     * set heightDefault
     *
     * @param heightDefault
     */
    var heightDefault: Behaviour? = null
    /**
     * get widthMax
     *
     * @return widthMax
     */
    /**
     * set widthMax
     *
     * @param widthMax
     */
    var widthMax = UNSET
    /**
     * get heightMax
     *
     * @return heightMax
     */
    /**
     * set heightMax
     *
     * @param heightMax
     */
    var heightMax = UNSET
    /**
     * get widthMin
     *
     * @return widthMin
     */
    /**
     * set widthMin
     *
     * @param widthMin
     */
    var widthMin = UNSET
    /**
     * get heightMin
     *
     * @return heightMin
     */
    /**
     * set heightMin
     *
     * @param heightMin
     */
    var heightMin = UNSET
    /**
     * get widthPercent
     *
     * @return
     */
    /**
     * set widthPercent
     *
     * @param widthPercent
     */
    var widthPercent = Float.NaN
    /**
     * get heightPercent
     *
     * @return heightPercent
     */
    /**
     * set heightPercent
     *
     * @param heightPercent
     */
    var heightPercent = Float.NaN
    /**
     * get referenceIds
     *
     * @return referenceIds
     */
    /**
     * set referenceIds
     *
     * @param referenceIds
     */
    var referenceIds: Array<String>? = null
    /**
     * is constrainedWidth
     *
     * @return true if width constrained
     */
    /**
     * set constrainedWidth
     *
     * @param constrainedWidth
     */
    var isConstrainedWidth = false
    /**
     * is constrainedHeight
     *
     * @return true if height constrained
     */
    /**
     * set constrainedHeight
     *
     * @param constrainedHeight
     */
    var isConstrainedHeight = false

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     */
    fun linkToTop(anchor: VAnchor, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        top.mConnection = anchor
        top.mMargin = margin
        top.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     */
    fun linkToLeft(anchor: HAnchor, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        left.mConnection = anchor
        left.mMargin = margin
        left.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     */
    fun linkToRight(anchor: HAnchor, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        right.mConnection = anchor
        right.mMargin = margin
        right.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     */
    fun linkToStart(anchor: HAnchor, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        start.mConnection = anchor
        start.mMargin = margin
        start.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     */
    fun linkToEnd(anchor: HAnchor, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        end.mConnection = anchor
        end.mMargin = margin
        end.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     */
    fun linkToBottom(anchor: VAnchor, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        bottom.mConnection = anchor
        bottom.mMargin = margin
        bottom.mGoneMargin = goneMargin
    }
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     */
    fun linkToBaseline(anchor: VAnchor, margin: Int = 0, goneMargin: Int = Int.MIN_VALUE) {
        baseline.mConnection = anchor
        baseline.mMargin = margin
        baseline.mGoneMargin = goneMargin
    }

    /**
     * convert a String array into a String representation
     *
     * @param str String array to be converted
     * @return a String representation of the input array.
     */
    fun convertStringArrayToString(str: Array<String>): String {
        val ret = StringBuilder("[")
        for (i in str.indices) {
            ret.append(if (i == 0) "'" else ",'")
            ret.append(str[i])
            ret.append("'")
        }
        ret.append("]")
        return ret.toString()
    }

    protected fun append(builder: StringBuilder, name: String?, value: Float) {
        if (Float.isNaN(value)) {
            return
        }
        builder.append(name)
        builder.append(":").append(value).append(",\n")
    }

    override fun toString(): String {
        val ret = StringBuilder("$id:{\n")
        left.build(ret)
        right.build(ret)
        top.build(ret)
        bottom.build(ret)
        start.build(ret)
        end.build(ret)
        baseline.build(ret)
        if (width != UNSET) {
            ret.append("width:").append(width).append(",\n")
        }
        if (height != UNSET) {
            ret.append("height:").append(height).append(",\n")
        }
        append(ret, "horizontalBias", horizontalBias)
        append(ret, "verticalBias", verticalBias)
        if (dimensionRatio != null) {
            ret.append("dimensionRatio:'").append(dimensionRatio).append("',\n")
        }
        if (circleConstraint != null) {
            if (!Float.isNaN(circleAngle) || circleRadius != Int.MIN_VALUE) {
                ret.append("circular:['").append(circleConstraint).append("'")
                if (!Float.isNaN(circleAngle)) {
                    ret.append(",").append(circleAngle)
                }
                if (circleRadius != Int.MIN_VALUE) {
                    if (Float.isNaN(circleAngle)) {
                        ret.append(",0,").append(circleRadius)
                    } else {
                        ret.append(",").append(circleRadius)
                    }
                }
                ret.append("],\n")
            }
        }
        append(ret, "verticalWeight", verticalWeight)
        append(ret, "horizontalWeight", horizontalWeight)
        if (horizontalChainStyle != null) {
            ret.append("horizontalChainStyle:'").append(chainModeMap[horizontalChainStyle])
                .append("',\n")
        }
        if (verticalChainStyle != null) {
            ret.append("verticalChainStyle:'").append(chainModeMap[verticalChainStyle])
                .append("',\n")
        }
        if (widthDefault != null) {
            if (widthMax == UNSET && widthMin == UNSET) {
                ret.append("width:'").append(widthDefault.toString().lowercase())
                    .append("',\n")
            } else {
                ret.append("width:{value:'").append(widthDefault.toString().lowercase())
                    .append("'")
                if (widthMax != UNSET) {
                    ret.append(",max:").append(widthMax)
                }
                if (widthMin != UNSET) {
                    ret.append(",min:").append(widthMin)
                }
                ret.append("},\n")
            }
        }
        if (heightDefault != null) {
            if (heightMax == UNSET && heightMin == UNSET) {
                ret.append("height:'").append(heightDefault.toString().lowercase())
                    .append("',\n")
            } else {
                ret.append("height:{value:'").append(heightDefault.toString().lowercase())
                    .append("'")
                if (heightMax != UNSET) {
                    ret.append(",max:").append(heightMax)
                }
                if (heightMin != UNSET) {
                    ret.append(",min:").append(heightMin)
                }
                ret.append("},\n")
            }
        }
        if (!Double.isNaN(widthPercent)) {
            ret.append("width:'").append(widthPercent.toInt()).append("%',\n")
        }
        if (!Double.isNaN(heightPercent)) {
            ret.append("height:'").append(heightPercent.toInt()).append("%',\n")
        }
        if (referenceIds != null) {
            ret.append("referenceIds:")
                .append(convertStringArrayToString(referenceIds!!))
                .append(",\n")
        }
        if (isConstrainedWidth) {
            ret.append("constrainedWidth:").append(isConstrainedWidth).append(",\n")
        }
        if (isConstrainedHeight) {
            ret.append("constrainedHeight:").append(isConstrainedHeight).append(",\n")
        }
        ret.append("},\n")
        return ret.toString()
    }

    companion object {
        val PARENT = Constraint("parent")
        var UNSET: Int = Int.MIN_VALUE
        var chainModeMap: MutableMap<ChainMode, String> = mutableMapOf()

        init {
            chainModeMap.put(ChainMode.SPREAD, "spread")
            chainModeMap.put(ChainMode.SPREAD_INSIDE, "spread_inside")
            chainModeMap.put(ChainMode.PACKED, "packed")
        }
    }
}