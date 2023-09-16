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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.TView.MeasureSpec
import dev.topping.ios.constraint.core.LinearSystem
import dev.topping.ios.constraint.core.Metrics
import dev.topping.ios.constraint.core.widgets.*
import dev.topping.ios.constraint.core.widgets.Guideline
import dev.topping.ios.constraint.core.widgets.analyzer.BasicMeasure
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

        /**
 * A `ConstraintLayout` is a [android.view.ViewGroup] which allows you
 * to position and size widgets in a flexible way.
 *
 *
 * **Note:** `ConstraintLayout` is available as a support library that you can use
 * on Android systems starting with API level 9 (Gingerbread).
 * As such, we are planning on enriching its API and capabilities over time.
 * This documentation will reflect those changes.
 *
 *
 *
 * There are currently various types of constraints that you can use:
 *
 *  * Relative positioning
 *  * Margins
 *  * Centering positioning
 *  * Circular positioning
 *  * Visibility behavior
 *  * Dimension constraints
 *  * Chains
 *  * Virtual Helpers objects
 *  * Optimizer
 *
 *
 *
 *
 *
 * Note that you cannot have a circular dependency in constraints.
 *
 *
 *
 * Also see [ ConstraintLayout.LayoutParams][ConstraintLayout.LayoutParams] for layout attributes
 *
 *
 * <h2>Developer Guide</h2>
 *
 * <h3 id="RelativePositioning"> Relative positioning </h3>
 *
 *
 * Relative positioning is one of the basic building blocks of creating layouts in ConstraintLayout.
 * Those constraints allow you to position a given widget relative to another one. You can constrain
 * a widget on the horizontal and vertical axis:
 *
 *  * Horizontal Axis: left, right, start and end sides
 *  * Vertical Axis: top, bottom sides and text baseline
 *
 *
 *
 * The general concept is to constrain a given side of a widget to another side of any other widget.
 *
 *
 * For example, in order to position button B to the right of button A (Fig. 1):
 * <br></br><div align="center">
 * <img width="300px" src="resources/images/relative-positioning.png"></img>
 * <br></br>***Fig. 1 - Relative Positioning Example***
</div> *
 *
 *
 *
 * you would need to do:
 *
 * <pre>`<Button android:id="@+id/buttonA" ... />
 * <Button android:id="@+id/buttonB" ...
 * app:layout_constraintLeft_toRightOf="@+id/buttonA" />
` *
</pre> *
 * This tells the system that we want the left side of button
 * B to be constrained to the right side of button A.
 * Such a position constraint means that the system will try to have
 * both sides share the same location.
 * <br></br><div align="center">
 * <img width="350px" src="resources/images/relative-positioning-constraints.png"></img>
 * <br></br>***Fig. 2 - Relative Positioning Constraints***
</div> *
 *
 *
 * Here is the list of available constraints (Fig. 2):
 *
 *  * `layout_constraintLeft_toLeftOf`
 *  * `layout_constraintLeft_toRightOf`
 *  * `layout_constraintRight_toLeftOf`
 *  * `layout_constraintRight_toRightOf`
 *  * `layout_constraintTop_toTopOf`
 *  * `layout_constraintTop_toBottomOf`
 *  * `layout_constraintBottom_toTopOf`
 *  * `layout_constraintBottom_toBottomOf`
 *  * `layout_constraintBaseline_toBaselineOf`
 *  * `layout_constraintStart_toEndOf`
 *  * `layout_constraintStart_toStartOf`
 *  * `layout_constraintEnd_toStartOf`
 *  * `layout_constraintEnd_toEndOf`
 *
 *
 *
 * They all take a reference `id` to another widget, or the
 * `parent` (which will reference the parent container, i.e. the ConstraintLayout):
 * <pre>`<Button android:id="@+id/buttonB" ...
 * app:layout_constraintLeft_toLeftOf="parent" />
` *
</pre> *
 *
 *
 *
 * <h3 id="Margins"> Margins </h3>
 *
 *
 * <div align="center">
 * <img width="325px" src="resources/images/relative-positioning-margin.png"></img>
 * <br></br>***Fig. 3 - Relative Positioning Margins***
</div> *
 *
 * If side margins are set, they will be applied to the corresponding constraints
 * (if they exist) (Fig. 3), enforcing the margin as a space between
 * the target and the source side. The usual layout margin attributes can be used to this effect:
 *
 *  * `android:layout_marginStart`
 *  * `android:layout_marginEnd`
 *  * `android:layout_marginLeft`
 *  * `android:layout_marginTop`
 *  * `android:layout_marginRight`
 *  * `android:layout_marginBottom`
 *  * `layout_marginBaseline`
 *
 *
 * Note that a margin can only be positive or equal to zero,
 * and takes a `Dimension`.
 * <h3 id="GoneMargin"> Margins when connected to a GONE widget</h3>
 *
 * When a position constraint target's visibility is `TView.GONE`,
 * you can also indicate a different
 * margin value to be used using the following attributes:
 *
 *  * `layout_goneMarginStart`
 *  * `layout_goneMarginEnd`
 *  * `layout_goneMarginLeft`
 *  * `layout_goneMarginTop`
 *  * `layout_goneMarginRight`
 *  * `layout_goneMarginBottom`
 *  * `layout_goneMarginBaseline`
 *
 *
 *
 *
 * <h3 id="CenteringPositioning"> Centering positioning and bias</h3>
 *
 *
 * A useful aspect of `ConstraintLayout` is in how it deals with "impossible" constraints.
 * For example, if
 * we have something like:
 * <pre>`<androidx.constraintlayout.widget.ConstraintLayout ...>
 * <Button android:id="@+id/button" ...
 * app:layout_constraintLeft_toLeftOf="parent"
 * app:layout_constraintRight_toRightOf="parent"/>
 * </>
` *
</pre> *
 *
 *
 *
 * Unless the `ConstraintLayout` happens to have the exact same size as the
 * `Button`, both constraints
 * cannot be satisfied at the same time (both sides cannot be where we want them to be).
 *
 * <div align="center">
 * <img width="325px" src="resources/images/centering-positioning.png"></img>
 * <br></br>***Fig. 4 - Centering Positioning***
</div> *
 *
 *
 * What happens in this case is that the constraints act like opposite forces
 * pulling the widget apart equally (Fig. 4); such that the widget will end up being centered
 * in the parent container.
 * This will apply similarly for vertical constraints.
 *
 * **Bias**
 *
 *
 * The default when encountering such opposite constraints is to center the widget;
 * but you can tweak
 * the positioning to favor one side over another using the bias attributes:
 *
 *  * `layout_constraintHorizontal_bias`
 *  * `layout_constraintVertical_bias`
 *
 *
 * <div align="center">
 * <img width="325px" src="resources/images/centering-positioning-bias.png"></img>
 * <br></br>***Fig. 5 - Centering Positioning with Bias***
</div> *
 *
 *
 * For example the following will make the left side with a 30% bias instead of the default 50%,
 * such that the left side will be
 * shorter, with the widget leaning more toward the left side (Fig. 5):
 *
 * <pre>`<androidx.constraintlayout.widget.ConstraintLayout ...>
 * <Button android:id="@+id/button" ...
 * app:layout_constraintHorizontal_bias="0.3"
 * app:layout_constraintLeft_toLeftOf="parent"
 * app:layout_constraintRight_toRightOf="parent"/>
 * </>
` *
</pre> *
 * Using bias, you can craft User Interfaces that will better adapt to screen sizes changes.
 *
 *
 *
 * <h3 id="CircularPositioning"> Circular positioning (**Added in 1.1**)</h3>
 *
 *
 * You can constrain a widget center relative to another widget center,
 * at an angle and a distance. This allows
 * you to position a widget on a circle (see Fig. 6). The following attributes can be used:
 *
 *  * `layout_constraintCircle` : references another widget id
 *  * `layout_constraintCircleRadius` : the distance to the other widget center
 *  * `layout_constraintCircleAngle` : which angle the widget should be at
 * (in degrees, from 0 to 360)
 *
 *
 * <div align="center">
 * <img width="325px" src="resources/images/circle1.png"></img>
 * <img width="325px" src="resources/images/circle2.png"></img>
 * <br></br>***Fig. 6 - Circular Positioning***
</div> *
 * <br></br><br></br>
 * <pre>`<Button android:id="@+id/buttonA" ... />
 * <Button android:id="@+id/buttonB" ...
 * app:layout_constraintCircle="@+id/buttonA"
 * app:layout_constraintCircleRadius="100dp"
 * app:layout_constraintCircleAngle="45" />
` *
</pre> *
 *
 * <h3 id="VisibilityBehavior"> Visibility behavior </h3>
 *
 *
 * `ConstraintLayout` has a specific handling of widgets being marked as `TView.GONE`.
 *
 * `GONE` widgets, as usual, are not going to be displayed and
 * are not part of the layout itself (i.e. their actual dimensions
 * will not be changed if marked as `GONE`).
 *
 *
 * But in terms of the layout computations, `GONE` widgets are still part of it,
 * with an important distinction:
 *
 *  *  For the layout pass, their dimension will be considered as zero
 * (basically, they will be resolved to a point)
 *  *  If they have constraints to other widgets they will still be respected,
 * but any margins will be as if equals to zero
 *
 *
 *
 * <div align="center">
 * <img width="350px" src="resources/images/visibility-behavior.png"></img>
 * <br></br>***Fig. 7 - Visibility Behavior***
</div> *
 *
 * This specific behavior allows to build layouts where you can
 * temporarily mark widgets as being `GONE`,
 * without breaking the layout (Fig. 7), which can be particularly useful
 * when doing simple layout animations.
 *
 * **Note: **The margin used will be the margin that B had
 * defined when connecting to A (see Fig. 7 for an example).
 * In some cases, this might not be the margin you want
 * (e.g. A had a 100dp margin to the side of its container,
 * B only a 16dp to A, marking
 * A as gone, B will have a margin of 16dp to the container).
 * For this reason, you can specify an alternate
 * margin value to be used when the connection is to a widget being marked as gone
 * (see [the section above about the gone margin attributes](#GoneMargin)).
 *
 *
 * <h3 id="DimensionConstraints"> Dimensions constraints </h3>
 * **Minimum dimensions on ConstraintLayout**
 *
 *
 * You can define minimum and maximum sizes for the `ConstraintLayout` itself:
 *
 *  * `android:minWidth` set the minimum width for the layout
 *  * `android:minHeight` set the minimum height for the layout
 *  * `android:maxWidth` set the maximum width for the layout
 *  * `android:maxHeight` set the maximum height for the layout
 *
 * Those minimum and maximum dimensions will be used by
 * `ConstraintLayout` when its dimensions are set to `WRAP_CONTENT`.
 *
 * **Widgets dimension constraints**
 *
 *
 * The dimension of the widgets can be specified by setting the
 * `android:layout_width` and
 * `android:layout_height` attributes in 3 different ways:
 *
 *  * Using a specific dimension (either a literal value such as
 * `123dp` or a `Dimension` reference)
 *  * Using `WRAP_CONTENT`, which will ask the widget to compute its own size
 *  * Using `0dp`, which is the equivalent of "`MATCH_CONSTRAINT`"
 *
 *
 * <div align="center">
 * <img width="325px" src="resources/images/dimension-match-constraints.png"></img>
 * <br></br>***Fig. 8 - Dimension Constraints***
</div> *
 * The first two works in a similar fashion as other layouts.
 * The last one will resize the widget in such a way as
 * matching the constraints that are set (see Fig. 8, (a) is wrap_content,
 * (b) is 0dp). If margins are set, they will be taken in account
 * in the computation (Fig. 8, (c) with 0dp).
 *
 *
 * **Important: ** `MATCH_PARENT` is not recommended for widgets
 * contained in a `ConstraintLayout`. Similar behavior can
 * be defined by using `MATCH_CONSTRAINT` with the corresponding
 * left/right or top/bottom constraints being set to `"parent"`.
 *
 *
 * **WRAP_CONTENT : enforcing constraints (*Added in 1.1*)**
 *
 *
 * If a dimension is set to `WRAP_CONTENT`, in versions before 1.1
 * they will be treated as a literal dimension -- meaning, constraints will
 * not limit the resulting dimension. While in general this is enough (and faster),
 * in some situations, you might want to use `WRAP_CONTENT`,
 * yet keep enforcing constraints to limit the resulting dimension. In that case,
 * you can add one of the corresponding attribute:
 *
 *  * `app:layout_constrainedWidth="true|false"`
 *  * `app:layout_constrainedHeight="true|false"`
 *
 *
 * **MATCH_CONSTRAINT dimensions (*Added in 1.1*)**
 *
 *
 * When a dimension is set to `MATCH_CONSTRAINT`,
 * the default behavior is to have the resulting size take all the available space.
 * Several additional modifiers are available:
 *
 *
 *  * `layout_constraintWidth_min` and `layout_constraintHeight_min` :
 * will set the minimum size for this dimension
 *  * `layout_constraintWidth_max` and `layout_constraintHeight_max` :
 * will set the maximum size for this dimension
 *  * `layout_constraintWidth_percent` and `layout_constraintHeight_percent` :
 * will set the size of this dimension as a percentage of the parent
 *
 * **Min and Max**
 *
 * The value indicated for min and max can be either a dimension in Dp,
 * or "wrap", which will use the same value as what `WRAP_CONTENT` would do.
 * **Percent dimension**
 *
 * To use percent, you need to set the following
 *
 *  * The dimension should be set to `MATCH_CONSTRAINT` (0dp)
 *  * The default should be set to percent `app:layout_constraintWidth_default="percent"`
 * or `app:layout_constraintHeight_default="percent"`
 *  * Then set the `layout_constraintWidth_percent`
 * or `layout_constraintHeight_percent` attributes to a value between 0 and 1
 *
 * **Ratio**
 *
 *
 * You can also define one dimension of a widget as a ratio of the other one.
 * In order to do that, you
 * need to have at least one constrained dimension be set to
 * `0dp` (i.e., `MATCH_CONSTRAINT`), and set the
 * attribute `layout_constraintDimensionRatio` to a given ratio.
 * For example:
 * <pre>
 * `<Button android:layout_width="wrap_content"
 * android:layout_height="0dp"
 * app:layout_constraintDimensionRatio="1:1" />
` *
</pre> *
 * will set the height of the button to be the same as its width.
 *
 *
 *  The ratio can be expressed either as:
 *
 *  * a float value, representing a ratio between width and height
 *  * a ratio in the form "width:height"
 *
 *
 *
 *
 * You can also use ratio if both dimensions are set to
 * `MATCH_CONSTRAINT` (0dp). In this case the system sets the
 * largest dimensions that satisfies all constraints and maintains
 * the aspect ratio specified. To constrain one specific side
 * based on the dimensions of another, you can pre append
 * `W,`" or `H,` to constrain the width or height
 * respectively.
 * For example,
 * If one dimension is constrained by two targets
 * (e.g. width is 0dp and centered on parent) you can indicate which
 * side should be constrained, by adding the letter
 * `W` (for constraining the width) or `H`
 * (for constraining the height) in front of the ratio, separated
 * by a comma:
 * <pre>
 * `<Button android:layout_width="0dp"
 * android:layout_height="0dp"
 * app:layout_constraintDimensionRatio="H,16:9"
 * app:layout_constraintBottom_toBottomOf="parent"
 * app:layout_constraintTop_toTopOf="parent"/>
` *
</pre> *
 * will set the height of the button following a 16:9 ratio,
 * while the width of the button will match the constraints
 * to its parent.
 *
 *
 *
 * <h3 id="Chains">Chains</h3>
 *
 * Chains provide group-like behavior in a single axis (horizontally or vertically).
 * The other axis can be constrained independently.
 * **Creating a chain**
 *
 *
 * A set of widgets are considered a chain if they are linked together via a
 * bi-directional connection (see Fig. 9, showing a minimal chain, with two widgets).
 *
 *
 * <div align="center">
 * <img width="325px" src="resources/images/chains.png"></img>
 * <br></br>***Fig. 9 - Chain***
</div> *
 *
 *
 * **Chain heads**
 *
 *
 * Chains are controlled by attributes set on the first element of the chain
 * (the "head" of the chain):
 *
 *
 * <div align="center">
 * <img width="400px" src="resources/images/chains-head.png"></img>
 * <br></br>***Fig. 10 - Chain Head***
</div> *
 *
 * The head is the left-most widget for horizontal chains,
 * and the top-most widget for vertical chains.
 * **Margins in chains**
 *
 * If margins are specified on connections, they will be taken into account.
 * In the case of spread chains, margins will be deducted from the allocated space.
 * **Chain Style**
 *
 * When setting the attribute `layout_constraintHorizontal_chainStyle` or
 * `layout_constraintVertical_chainStyle` on the first element of a chain,
 * the behavior of the chain will change according to the specified style
 * (default is `CHAIN_SPREAD`).
 *
 *  * `CHAIN_SPREAD` -- the elements will be spread out (default style)
 *  * Weighted chain -- in `CHAIN_SPREAD` mode,
 * if some widgets are set to `MATCH_CONSTRAINT`, they will split the available space
 *  * `CHAIN_SPREAD_INSIDE` -- similar,
 * but the endpoints of the chain will not be spread out
 *  * `CHAIN_PACKED` -- the elements of the chain will be packed together.
 * The horizontal or vertical
 * bias attribute of the child will then affect the positioning of the packed elements
 *
 *
 * <div align="center">
 * <img width="600px" src="resources/images/chains-styles.png"></img>
 * <br></br>***Fig. 11 - Chains Styles***
</div> *
 *
 * **Weighted chains**
 *
 * The default behavior of a chain is to spread the elements equally in the available space.
 * If one or more elements are using `MATCH_CONSTRAINT`, they
 * will use the available empty space (equally divided among themselves).
 * The attribute `layout_constraintHorizontal_weight` and
 * `layout_constraintVertical_weight`
 * will control how the space will be distributed among the elements using
 * `MATCH_CONSTRAINT`. For example,
 * on a chain containing two elements using `MATCH_CONSTRAINT`,
 * with the first element using a weight of 2 and the second a weight of 1,
 * the space occupied by the first element will be twice that of the second element.
 *
 * **Margins and chains (*in 1.1*)**
 *
 * When using margins on elements in a chain, the margins are additive.
 *
 * For example, on a horizontal chain, if one element defines
 * a right margin of 10dp and the next element
 * defines a left margin of 5dp, the resulting margin between those
 * two elements is 15dp.
 *
 * An item plus its margins are considered together when calculating
 * leftover space used by chains
 * to position items. The leftover space does not contain the margins.
 *
 * <h3 id="VirtualHelpers"> Virtual Helper objects </h3>
 *
 * In addition to the intrinsic capabilities detailed previously,
 * you can also use special helper objects
 * in `ConstraintLayout` to help you with your layout. Currently, the
 * `Guideline`{@see Guideline} object allows you to create
 * Horizontal and Vertical guidelines which are positioned relative to the
 * `ConstraintLayout` container. Widgets can
 * then be positioned by constraining them to such guidelines. In **1.1**,
 * `Barrier` and `Group` were added too.
 *
 * <h3 id="Optimizer">Optimizer (***in 1.1***)</h3>
 *
 *
 * In 1.1 we exposed the constraints optimizer. You can decide which optimizations
 * are applied by adding the tag *app:layout_optimizationLevel*
 * to the ConstraintLayout element.
 *
 *  * **none** : no optimizations are applied
 *  * **standard** : Default. Optimize direct and barrier constraints only
 *  * **direct** : optimize direct constraints
 *  * **barrier** : optimize barrier constraints
 *  * **chain** : optimize chain constraints (experimental)
 *  * **dimensions** : optimize dimensions measures (experimental),
 * reducing the number of measures of match constraints elements
 *
 *
 *
 * This attribute is a mask, so you can decide to turn on or off
 * specific optimizations by listing the ones you want.
 * For example: *app:layout_optimizationLevel="direct|barrier|chain"*
 */
open class ConstraintLayout(val context: TContext, val attrs: AttributeSet, val self: TView) {
    var mChildrenByIds: MutableMap<String, TView> = mutableMapOf()

    // This array keep a list of helper objects if they are present
    val mConstraintHelpers: ArrayList<ConstraintHelper> = ArrayList(4)
    var mLayoutWidget: ConstraintWidgetContainer = ConstraintWidgetContainer()
    private var mMinWidth = 0
    private var mMinHeight = 0
    private var mMaxWidth: Int = Int.MAX_VALUE
    private var mMaxHeight: Int = Int.MAX_VALUE
    protected var mDirtyHierarchy = true
    private var mOptimizationLevel: Int = Optimizer.OPTIMIZATION_STANDARD;
    private var mConstraintSet: ConstraintSet? = null
    protected var mConstraintLayoutSpec: ConstraintLayoutStates? = null
    private var mConstraintSetId = ""
    private var mDesignIds: HashMap<String, String> = HashMap()

    // Cache last measure
    private var mLastMeasureWidth = -1
    private var mLastMeasureHeight = -1
    var mLastMeasureWidthSize = -1
    var mLastMeasureHeightSize = -1
    var mLastMeasureWidthMode: Int = MeasureSpec.UNSPECIFIED
    var mLastMeasureHeightMode: Int = MeasureSpec.UNSPECIFIED
    private val mTempMapIdToWidget: MutableMap<String, ConstraintWidget> = mutableMapOf()

    // private ConstraintsChangedListener mConstraintsChangedListener;
    private var mMetrics: Metrics? = null
    private var mMeasurer: Measurer

    /**
     *
     */
    fun setDesignInformation(type: Int, value1: Any, value2: Any) {
        if (type == DESIGN_INFO_ID && value1 is String
            && value2 is Int
        ) {
            if (mDesignIds == null) {
                mDesignIds = HashMap()
            }
            var name = value1 as String
            val index = name.indexOf("/")
            if (index != -1) {
                name = name.substring(index + 1)
            }
            val id = value2 as String
            mDesignIds!![name] = id
        }
    }

    /**
     *
     */
    fun getDesignInformation(type: Int, value: Any): Any? {
        if (type == DESIGN_INFO_ID && value is String) {
            val name = value as String
            if (mDesignIds != null && mDesignIds!!.containsKey(name)) {
                return mDesignIds!![name]
            }
        }
        return null
    }

    init {
        self.setParentType(this)
        self.swizzleFunction("setId") { sup, params ->
            var args = params as Array<Any>
            setId(sup, params[0] as String)
        }
        self.swizzleFunction("onViewAdded") { sup, params ->
            var args = params as Array<Any>
            onViewAdded(sup, params[0] as TView)
        }
        self.swizzleFunction("onViewRemoved") { sup, params ->
            var args = params as Array<Any>
            onViewRemoved(sup, params[0] as TView)
        }
        self.swizzleFunction("requestLayout") { sup, params ->
            var args = params as Array<Any>
            requestLayout(sup)
        }
        self.swizzleFunction("forceLayout") { sup, params ->
            var args = params as Array<Any>
            forceLayout(sup)
        }
        self.swizzleFunction("dispatchDraw") { sup, params ->
            var args = params as Array<Any>
            dispatchDraw(sup, params[0] as TCanvas)
        }
        self.swizzleFunction("onMeasure") { sup, params ->
            var args = params as Array<Any>
            onMeasure(sup, params[0] as Int, params[1] as Int)
        }

        self.swizzleFunction("onLayout") { sup, params ->
            var args = params as Array<Any>
            onLayout(sup, params[0] as Boolean, params[1] as Int, params[2] as Int, params[3] as Int, params[4] as Int)
        }

        mMeasurer = Measurer(this)
        mLayoutWidget.setCompanionWidget(this)
        mLayoutWidget.measurer = mMeasurer
        mChildrenByIds[self.getId()] = self
        mConstraintSet = null
        val a = context.getResources()
        attrs.forEach { kvp ->
            val attr = kvp.value
            if (kvp.key == "android_minWidth") {
                mMinWidth = a.getDimensionPixelOffset(attr, mMinWidth)
            } else if (kvp.key == "android_minHeight") {
                mMinHeight = a.getDimensionPixelOffset(attr, mMinHeight)
            } else if (kvp.key == "android_maxWidth") {
                mMaxWidth = a.getDimensionPixelOffset(attr, mMaxWidth)
            } else if (kvp.key == "android_maxHeight") {
                mMaxHeight = a.getDimensionPixelOffset(attr, mMaxHeight)
            } else if (kvp.key == "layout_optimizationLevel") {
                mOptimizationLevel = a.getInt(attr, mOptimizationLevel)
            } else if (kvp.key == "layoutDescription") {
                val id = a.getResourceId(attr, "-1")
                if (id != "-1") {
                    try {
                        parseLayoutDescription(id)
                    } catch (e: NotFoundException) {
                        mConstraintLayoutSpec = null
                    }
                }
            } else if (kvp.key == "constraintSet") {
                val id = a.getResourceId(attr, "")
                try {
                    mConstraintSet = ConstraintSet()
                    mConstraintSet!!.load(context, id)
                } catch (e: NotFoundException) {
                    mConstraintSet = null
                }
                mConstraintSetId = id
            }
        }
        mLayoutWidget.optimizationLevel = (mOptimizationLevel)
    }

    /**
     *
     */
    fun setId(sup:TView?, id: String) {
        mChildrenByIds.remove(self.getId())
        sup?.setId(id)
        mChildrenByIds[self.getId()] = self
    }

    // -------------------------------------------------------------------------------------------
    // Measure widgets callbacks
    // -------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------
    inner class Measurer(var mLayout: ConstraintLayout) : BasicMeasure.Measurer {
        var mPaddingTop = 0
        var mPaddingBottom = 0
        var mPaddingWidth = 0
        var mPaddingHeight = 0
        var mLayoutWidthSpec = 0
        var mLayoutHeightSpec = 0
        fun captureLayoutInfo(
            widthSpec: Int,
            heightSpec: Int,
            top: Int,
            bottom: Int,
            width: Int,
            height: Int
        ) {
            mPaddingTop = top
            mPaddingBottom = bottom
            mPaddingWidth = width
            mPaddingHeight = height
            mLayoutWidthSpec = widthSpec
            mLayoutHeightSpec = heightSpec
        }

        override fun measure(
            widget: ConstraintWidget,
            measure: BasicMeasure.Measure
        ) {
            if (widget == null) {
                return
            }
            if (widget.getVisibility() == TView.GONE && !widget.isInPlaceholder()) {
                measure.measuredWidth = 0
                measure.measuredHeight = 0
                measure.measuredBaseline = 0
                return
            }
            if (widget.getParent() == null) {
                return
            }
            var startMeasure: Long = 0
            val endMeasure: Long
            if (mMetrics != null) {
                mMetrics!!.mNumberOfMeasures++
                startMeasure = nanoTime()
            }
            val horizontalBehavior: ConstraintWidget.DimensionBehaviour? = measure.horizontalBehavior
            val verticalBehavior: ConstraintWidget.DimensionBehaviour? = measure.verticalBehavior
            val horizontalDimension: Int = measure.horizontalDimension
            val verticalDimension: Int = measure.verticalDimension
            var horizontalSpec = 0
            var verticalSpec = 0
            val heightPadding = mPaddingTop + mPaddingBottom
            val widthPadding = mPaddingWidth
            val child: TView = widget.getCompanionWidget() as TView
            when (horizontalBehavior) {
                ConstraintWidget.DimensionBehaviour.FIXED -> {
                    horizontalSpec = MeasureSpec.makeMeasureSpec(
                        horizontalDimension,
                        MeasureSpec.EXACTLY
                    )
                }
                ConstraintWidget.DimensionBehaviour.WRAP_CONTENT -> {
                    horizontalSpec = self.getChildMeasureSpec(
                        mLayoutWidthSpec,
                        widthPadding, WRAP_CONTENT
                    )
                }
                ConstraintWidget.DimensionBehaviour.MATCH_PARENT -> {

                    // Horizontal spec must account for margin as well as padding here.
                    horizontalSpec = self.getChildMeasureSpec(
                        mLayoutWidthSpec,
                        widthPadding + widget.getHorizontalMargin(),
                        MATCH_PARENT
                    )
                }
                ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT -> {
                    horizontalSpec = self.getChildMeasureSpec(
                        mLayoutWidthSpec,
                        widthPadding, WRAP_CONTENT
                    )
                    val shouldDoWrap = (widget.mMatchConstraintDefaultWidth
                            == ConstraintWidget.MATCH_CONSTRAINT_WRAP)
                    if (measure.measureStrategy == BasicMeasure.Measure.TRY_GIVEN_DIMENSIONS
                        || measure.measureStrategy
                        == BasicMeasure.Measure.USE_GIVEN_DIMENSIONS
                    ) {
                        // the solver gives us our new dimension,
                        // but if we previously had it measured with
                        // a wrap, it can be incorrect if the other side was also variable.
                        // So in that case, we have to double-check the
                        // other side is stable (else we can't
                        // just assume the wrap value will be correct).
                        val otherDimensionStable = (child.getMeasuredHeight()
                                == widget.getHeight())
                        val useCurrent = ((measure.measureStrategy
                                == BasicMeasure.Measure.USE_GIVEN_DIMENSIONS) || !shouldDoWrap
                                || shouldDoWrap && otherDimensionStable
                                || child.getParentType() is Placeholder
                                || widget.isResolvedHorizontally())
                        if (useCurrent) {
                            horizontalSpec = MeasureSpec.makeMeasureSpec(
                                widget.getWidth(),
                                MeasureSpec.EXACTLY
                            )
                        }
                    }
                }
                else -> {}
            }
            when (verticalBehavior) {
                ConstraintWidget.DimensionBehaviour.FIXED -> {
                    verticalSpec = MeasureSpec.makeMeasureSpec(
                        verticalDimension,
                        MeasureSpec.EXACTLY
                    )
                }
                ConstraintWidget.DimensionBehaviour.WRAP_CONTENT -> {
                    verticalSpec = self.getChildMeasureSpec(
                        mLayoutHeightSpec,
                        heightPadding, WRAP_CONTENT
                    )
                }
                ConstraintWidget.DimensionBehaviour.MATCH_PARENT -> {

                    // Vertical spec must account for margin as well as padding here.
                    verticalSpec = self.getChildMeasureSpec(
                        mLayoutHeightSpec,
                        heightPadding + widget.getVerticalMargin(),
                        MATCH_PARENT
                    )
                }
                ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT -> {
                    verticalSpec = self.getChildMeasureSpec(
                        mLayoutHeightSpec,
                        heightPadding, WRAP_CONTENT
                    )
                    val shouldDoWrap = (widget.mMatchConstraintDefaultHeight
                            == ConstraintWidget.MATCH_CONSTRAINT_WRAP)
                    if (measure.measureStrategy == BasicMeasure.Measure.TRY_GIVEN_DIMENSIONS
                        || measure.measureStrategy
                        == BasicMeasure.Measure.USE_GIVEN_DIMENSIONS
                    ) {
                        // the solver gives us our new dimension,
                        // but if we previously had it measured with
                        // a wrap, it can be incorrect if the other side was also variable.
                        // So in that case, we have to double-check
                        // the other side is stable (else we can't
                        // just assume the wrap value will be correct).
                        val otherDimensionStable = (child.getMeasuredWidth()
                                == widget.getWidth())
                        val useCurrent = ((measure.measureStrategy
                                == BasicMeasure.Measure.USE_GIVEN_DIMENSIONS) || !shouldDoWrap
                                || shouldDoWrap && otherDimensionStable
                                || child.getParentType() is Placeholder
                                || widget.isResolvedVertically())
                        if (useCurrent) {
                            verticalSpec = MeasureSpec.makeMeasureSpec(
                                widget.getHeight(),
                                MeasureSpec.EXACTLY
                            )
                        }
                    }
                }
                else -> {}
            }
            val container: ConstraintWidgetContainer? =
                widget.getParent() as ConstraintWidgetContainer?
            if (container != null && Optimizer.enabled(
                    mOptimizationLevel,
                    Optimizer.OPTIMIZATION_CACHE_MEASURES
                )
            ) {
                if (child.getMeasuredWidth() == widget.getWidth() // note: the container check replicates legacy behavior, but we might want
                    // to not enforce that in 3.0
                    && child.getMeasuredWidth() < container.getWidth() && child.getMeasuredHeight() == widget.getHeight() && child.getMeasuredHeight() < container.getHeight() && child.getBaseline() == widget.getBaselineDistance() && !widget.isMeasureRequested()
                ) {
                    val similar = (isSimilarSpec(
                        widget.getLastHorizontalMeasureSpec(),
                        horizontalSpec, widget.getWidth()
                    )
                            && isSimilarSpec(
                        widget.getLastVerticalMeasureSpec(),
                        verticalSpec, widget.getHeight()
                    ))
                    if (similar) {
                        measure.measuredWidth = widget.getWidth()
                        measure.measuredHeight = widget.getHeight()
                        measure.measuredBaseline = widget.getBaselineDistance()
                        // if the dimensions of the solver widget are already the
                        // same as the real view, no need to remeasure.
                        if (I_DEBUG) {
                            println("SKIPPED $widget")
                        }
                        return
                    }
                }
            }
            val horizontalMatchConstraints = (horizontalBehavior
                    == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
            val verticalMatchConstraints = (verticalBehavior
                    == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
            val verticalDimensionKnown = (verticalBehavior
                    == ConstraintWidget.DimensionBehaviour.MATCH_PARENT
                    || verticalBehavior == ConstraintWidget.DimensionBehaviour.FIXED)
            val horizontalDimensionKnown = (horizontalBehavior
                    == ConstraintWidget.DimensionBehaviour.MATCH_PARENT
                    || horizontalBehavior == ConstraintWidget.DimensionBehaviour.FIXED)
            val horizontalUseRatio = horizontalMatchConstraints && widget.dimensionRatio > 0
            val verticalUseRatio = verticalMatchConstraints && widget.dimensionRatio > 0
            if (child == null) {
                return
            }
            val params = child.getLayoutParams() as LayoutParams
            var width = 0
            var height = 0
            var baseline = 0
            if ((measure.measureStrategy == BasicMeasure.Measure.TRY_GIVEN_DIMENSIONS
                        || measure.measureStrategy == BasicMeasure.Measure.USE_GIVEN_DIMENSIONS)
                || !(horizontalMatchConstraints
                        && widget.mMatchConstraintDefaultWidth == ConstraintWidget.MATCH_CONSTRAINT_SPREAD && verticalMatchConstraints
                        && widget.mMatchConstraintDefaultHeight == ConstraintWidget.MATCH_CONSTRAINT_SPREAD)
            ) {
                if (child.getParentType() is VirtualLayout
                    && widget is dev.topping.ios.constraint.core.widgets.VirtualLayout
                ) {
                    (child as VirtualLayout).onMeasure(widget, horizontalSpec, verticalSpec)
                } else {
                    child.measure(horizontalSpec, verticalSpec)
                }
                widget.setLastMeasureSpec(horizontalSpec, verticalSpec)
                val w: Int = child.getMeasuredWidth()
                val h: Int = child.getMeasuredHeight()
                baseline = child.getBaseline()
                width = w
                height = h
                if (I_DEBUG) {
                    val measurement: String = (MeasureSpec.toString(horizontalSpec)
                            + " x " + MeasureSpec.toString(verticalSpec)
                            + " => " + width + " x " + height)
                    println(
                        "    (M) measure "
                                + " (" + widget.getDebugName() + ") : " + measurement
                    )
                }
                if (widget.mMatchConstraintMinWidth > 0) {
                    width = max(widget.mMatchConstraintMinWidth, width)
                }
                if (widget.mMatchConstraintMaxWidth > 0) {
                    width = min(widget.mMatchConstraintMaxWidth, width)
                }
                if (widget.mMatchConstraintMinHeight > 0) {
                    height = max(widget.mMatchConstraintMinHeight, height)
                }
                if (widget.mMatchConstraintMaxHeight > 0) {
                    height = min(widget.mMatchConstraintMaxHeight, height)
                }
                val optimizeDirect: Boolean = Optimizer.enabled(
                    mOptimizationLevel,
                    Optimizer.OPTIMIZATION_DIRECT
                )
                if (!optimizeDirect) {
                    if (horizontalUseRatio && verticalDimensionKnown) {
                        val ratio: Float = widget.getDimensionRatio()
                        width = (0.5f + height * ratio).toInt()
                    } else if (verticalUseRatio && horizontalDimensionKnown) {
                        val ratio: Float = widget.getDimensionRatio()
                        height = (0.5f + width / ratio).toInt()
                    }
                }
                if (w != width || h != height) {
                    if (w != width) {
                        horizontalSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                    }
                    if (h != height) {
                        verticalSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                    }
                    child.measure(horizontalSpec, verticalSpec)
                    widget.setLastMeasureSpec(horizontalSpec, verticalSpec)
                    width = child.getMeasuredWidth()
                    height = child.getMeasuredHeight()
                    baseline = child.getBaseline()
                    if (I_DEBUG) {
                        val measurement2: String = (MeasureSpec.toString(horizontalSpec)
                                + " x " + MeasureSpec.toString(verticalSpec)
                                + " => " + width + " x " + height)
                        println(
                            "measure (b) " + widget.getDebugName()
                                .toString() + " : " + measurement2
                        )
                    }
                }
            }
            var hasBaseline = baseline != -1
            measure.measuredNeedsSolverPass = (width != measure.horizontalDimension
                    || height != measure.verticalDimension)
            if (params.mNeedsBaseline) {
                hasBaseline = true
            }
            if (hasBaseline && baseline != -1 && widget.getBaselineDistance() != baseline) {
                measure.measuredNeedsSolverPass = true
            }
            measure.measuredWidth = width
            measure.measuredHeight = height
            measure.measuredHasBaseline = hasBaseline
            measure.measuredBaseline = baseline
            if (mMetrics != null) {
                endMeasure = nanoTime()
                mMetrics!!.measuresWidgetsDuration += endMeasure - startMeasure
            }
        }

        /**
         * Returns true if the previous measure spec is equivalent to the new one.
         * - if it's the same...
         * - if it's not, but the previous was AT_MOST or UNSPECIFIED and the new one
         * is EXACTLY with the same size.
         *
         * @param lastMeasureSpec
         * @param spec
         * @param widgetSize
         * @return
         */
        private fun isSimilarSpec(lastMeasureSpec: Int, spec: Int, widgetSize: Int): Boolean {
            if (lastMeasureSpec == spec) {
                return true
            }
            val lastMode: Int = MeasureSpec.getMode(lastMeasureSpec)
            val mode: Int = MeasureSpec.getMode(spec)
            val size: Int = MeasureSpec.getSize(spec)
            return (mode == MeasureSpec.EXACTLY && (lastMode == MeasureSpec.AT_MOST || lastMode == MeasureSpec.UNSPECIFIED)
                    && widgetSize == size)
        }

        override fun didMeasures() {
            val widgetsCount: Int = mLayout.self.getChildCount()
            for (i in 0 until widgetsCount) {
                val child: TView = mLayout.self.getChildAt(i)
                if (child.getParentType() is Placeholder) {
                    (child.getParentType() as Placeholder).updatePostMeasure(mLayout)
                }
            }
            // TODO refactor into an updatePostMeasure interface
            val helperCount: Int = mLayout.mConstraintHelpers!!.size
            if (helperCount > 0) {
                for (i in 0 until helperCount) {
                    val helper: ConstraintHelper? = mLayout.mConstraintHelpers!![i]
                    helper!!.updatePostMeasure(mLayout)
                }
            }
        }
    }

    /**
     * Subclasses can override the handling of layoutDescription
     *
     * @param id
     */
    open fun parseLayoutDescription(id: String) {
        mConstraintLayoutSpec = ConstraintLayoutStates(self.getContext(), this, id)
    }

    /**
     *
     */
    open fun onViewAdded(sup:TView?, view: TView) {
        sup?.onViewAdded(view)
        val widget: ConstraintWidget? = getViewWidget(view)
        if (view.getParentType() is dev.topping.ios.constraint.constraintlayout.widget.Guideline) {
            if (widget !is Guideline) {
                val layoutParams = view.getLayoutParams() as LayoutParams
                layoutParams.mWidget = Guideline()
                layoutParams.mIsGuideline = true
                (layoutParams.mWidget as Guideline?)?.orientation = (layoutParams.orientation)
            }
        }
        if (view.getParentType() is ConstraintHelper) {
            val helper: ConstraintHelper? = view.getParentType() as ConstraintHelper
            helper!!.validateParams()
            val layoutParams = view.getLayoutParams() as LayoutParams
            layoutParams.mIsHelper = true
            if (!mConstraintHelpers!!.contains(helper)) {
                mConstraintHelpers.add(helper)
            }
        }
        mChildrenByIds[view.getId()] = view
        mDirtyHierarchy = true
    }

    /**
     *
     */
    open fun onViewRemoved(sup: TView?, view: TView) {
        sup?.onViewRemoved(view)
        mChildrenByIds.remove(view.getId())
        val widget: ConstraintWidget? = getViewWidget(view)
        widget?.let {
            mLayoutWidget.remove(it)
        }
        mConstraintHelpers!!.remove(view.getParentType())
        mDirtyHierarchy = true
    }
    /**
     * The minimum width of this view.
     *
     * @return The minimum width of this view
     * @see .setMinWidth
     */
    /**
     * Set the min width for this view
     *
     * @param value
     */
    var minWidth: Int
        get() = mMinWidth
        set(value) {
            if (value == mMinWidth) {
                return
            }
            mMinWidth = value
            requestLayout(self)
        }
    /**
     * The minimum height of this view.
     *
     * @return The minimum height of this view
     * @see .setMinHeight
     */
    /**
     * Set the min height for this view
     *
     * @param value
     */
    var minHeight: Int
        get() = mMinHeight
        set(value) {
            if (value == mMinHeight) {
                return
            }
            mMinHeight = value
            requestLayout(self)
        }
    /*
     * The maximum width of this view.
     *
     * @return The maximum width of this view
     *
     * @see #setMaxWidth(int)
     */
    /**
     * Set the max width for this view
     *
     * @param value
     */
    var maxWidth: Int
        get() = mMaxWidth
        set(value) {
            if (value == mMaxWidth) {
                return
            }
            mMaxWidth = value
            requestLayout(self)
        }
    /**
     * The maximum height of this view.
     *
     * @return The maximum height of this view
     * @see .setMaxHeight
     */
    /**
     * Set the max height for this view
     *
     * @param value
     */
    var maxHeight: Int
        get() = mMaxHeight
        set(value) {
            if (value == mMaxHeight) {
                return
            }
            mMaxHeight = value
            requestLayout(self)
        }

    private fun updateHierarchy(): Boolean {
        val count: Int = self.getChildCount()
        var recompute = false
        for (i in 0 until count) {
            val child: TView = self.getChildAt(i)
            if (child.isLayoutRequested()) {
                recompute = true
                break
            }
        }
        if (recompute) {
            setChildrenConstraints()
        }
        return recompute
    }

    private fun setChildrenConstraints() {
        val isInEditMode = I_DEBUG || self.isInEditMode()
        val count: Int = self.getChildCount()

        // Make sure everything is fully reset before anything else
        for (i in 0 until count) {
            val child: TView = self.getChildAt(i)
            val widget: ConstraintWidget = getViewWidget(child) ?: continue
            widget.reset()
        }
        if (isInEditMode) {
            // In design mode, let's make sure we keep track of the ids; in Studio, a build step
            // might not have been done yet, so asking the system for ids can break. So to be safe,
            // we save the current ids, which helpers can ask for.
            for (i in 0 until count) {
                val view: TView = self.getChildAt(i)
                try {
                    var IdAsString: String = context.getResources().getResourceName(view.getId())
                    setDesignInformation(DESIGN_INFO_ID, IdAsString, view.getId())
                    val slashIndex = IdAsString.indexOf('/')
                    if (slashIndex != -1) {
                        IdAsString = IdAsString.substring(slashIndex + 1)
                    }
                    getTargetWidget(view.getId())?.debugName = IdAsString
                } catch (e: NotFoundException) {
                    // nothing
                }
            }
        } else if (I_DEBUG) {
            mLayoutWidget.setDebugName("root")
            for (i in 0 until count) {
                val view: TView = self.getChildAt(i)
                try {
                    var IdAsString: String = context.getResources().getResourceName(view.getId())
                    setDesignInformation(DESIGN_INFO_ID, IdAsString, view.getId())
                    val slashIndex = IdAsString.indexOf('/')
                    if (slashIndex != -1) {
                        IdAsString = IdAsString.substring(slashIndex + 1)
                    }
                    getTargetWidget(view.getId())?.setDebugName(IdAsString)
                } catch (e: NotFoundException) {
                    // nothing
                }
            }
        }
        if (USE_CONSTRAINTS_HELPER && mConstraintSetId != "") {
            for (i in 0 until count) {
                val child: TView = self.getChildAt(i)
                if (child.getId() == mConstraintSetId && child.getParentType() is Constraints) {
                    mConstraintSet = (child.getParentType() as Constraints).constraintSet
                }
            }
        }
        if (mConstraintSet != null) {
            mConstraintSet!!.applyToInternal(this, true)
        }
        mLayoutWidget.removeAllChildren()
        val helperCount: Int = mConstraintHelpers!!.size
        if (helperCount > 0) {
            for (i in 0 until helperCount) {
                val helper: ConstraintHelper? = mConstraintHelpers[i]
                helper!!.updatePreLayout(this)
            }
        }

        // TODO refactor into an updatePreLayout interface
        for (i in 0 until count) {
            val child: TView = self.getChildAt(i)
            if (child.getParentType() is Placeholder) {
                (child as Placeholder).updatePreLayout(this)
            }
        }
        mTempMapIdToWidget.clear()
        mTempMapIdToWidget[ConstraintLayout.LayoutParams.PARENT_ID] = mLayoutWidget
        mTempMapIdToWidget[self.getId()] = mLayoutWidget
        for (i in 0 until count) {
            val child: TView = self.getChildAt(i)
            val widget: ConstraintWidget? = getViewWidget(child)
            mTempMapIdToWidget[child.getId()] = widget!!
        }
        for (i in 0 until count) {
            val child: TView = self.getChildAt(i)
            val widget: ConstraintWidget = getViewWidget(child) ?: continue
            val layoutParams = child.getLayoutParams() as LayoutParams
            mLayoutWidget.add(widget)
            applyConstraintsFromLayoutParams(
                isInEditMode, child, widget,
                layoutParams, mTempMapIdToWidget
            )
        }
    }

    protected fun applyConstraintsFromLayoutParams(
        isInEditMode: Boolean,
        child: TView,
        widget: ConstraintWidget,
        layoutParams: LayoutParams,
        idToWidget: MutableMap<String, ConstraintWidget>
    ) {
        layoutParams.validate()
        layoutParams.helped = false
        widget.setVisibility(child.getVisibility())
        if (layoutParams.mIsInPlaceholder) {
            widget.setInPlaceholder(true)
            widget.setVisibility(TView.GONE)
        }
        widget.setCompanionWidget(child)
        if (child.getParentType() is ConstraintHelper) {
            (child.getParentType() as ConstraintHelper).resolveRtl(widget, mLayoutWidget.isRtl)
        }
        if (layoutParams.mIsGuideline) {
            val guideline: Guideline = widget as Guideline
            var resolvedGuideBegin = layoutParams.mResolvedGuideBegin
            var resolvedGuideEnd = layoutParams.mResolvedGuideEnd
            var resolvedGuidePercent = layoutParams.mResolvedGuidePercent
            if (resolvedGuidePercent != LayoutParams.UNSET.toFloat()) {
                guideline.setGuidePercent(resolvedGuidePercent)
            } else if (resolvedGuideBegin != LayoutParams.UNSET) {
                guideline.setGuideBegin(resolvedGuideBegin)
            } else if (resolvedGuideEnd != LayoutParams.UNSET) {
                guideline.setGuideEnd(resolvedGuideEnd)
            }
        } else {
            // Get the left/right constraints resolved for RTL
            var resolvedLeftToLeft = layoutParams.mResolvedLeftToLeft
            var resolvedLeftToRight = layoutParams.mResolvedLeftToRight
            var resolvedRightToLeft = layoutParams.mResolvedRightToLeft
            var resolvedRightToRight = layoutParams.mResolvedRightToRight
            var resolveGoneLeftMargin = layoutParams.mResolveGoneLeftMargin
            var resolveGoneRightMargin = layoutParams.mResolveGoneRightMargin
            var resolvedHorizontalBias = layoutParams.mResolvedHorizontalBias

            // Circular constraint
            if (layoutParams.circleConstraint != LayoutParams.UNSET_ID) {
                val target: ConstraintWidget? = idToWidget[layoutParams.circleConstraint]
                if (target != null) {
                    widget.connectCircularConstraint(
                        target,
                        layoutParams.circleAngle, layoutParams.circleRadius
                    )
                }
            } else {
                // Left constraint
                if (resolvedLeftToLeft != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[resolvedLeftToLeft]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.LEFT, target,
                            ConstraintAnchor.Type.LEFT, layoutParams.leftMargin,
                            resolveGoneLeftMargin
                        )
                    }
                } else if (resolvedLeftToRight != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[resolvedLeftToRight]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.LEFT, target,
                            ConstraintAnchor.Type.RIGHT, layoutParams.leftMargin,
                            resolveGoneLeftMargin
                        )
                    }
                }

                // Right constraint
                if (resolvedRightToLeft != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[resolvedRightToLeft]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.RIGHT, target,
                            ConstraintAnchor.Type.LEFT, layoutParams.rightMargin,
                            resolveGoneRightMargin
                        )
                    }
                } else if (resolvedRightToRight != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[resolvedRightToRight]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.RIGHT, target,
                            ConstraintAnchor.Type.RIGHT, layoutParams.rightMargin,
                            resolveGoneRightMargin
                        )
                    }
                }

                // Top constraint
                if (layoutParams.topToTop != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[layoutParams.topToTop]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.TOP, target,
                            ConstraintAnchor.Type.TOP, layoutParams.topMargin,
                            layoutParams.goneTopMargin
                        )
                    }
                } else if (layoutParams.topToBottom != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[layoutParams.topToBottom]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.TOP, target,
                            ConstraintAnchor.Type.BOTTOM, layoutParams.topMargin,
                            layoutParams.goneTopMargin
                        )
                    }
                }

                // Bottom constraint
                if (layoutParams.bottomToTop != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[layoutParams.bottomToTop]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.BOTTOM, target,
                            ConstraintAnchor.Type.TOP, layoutParams.bottomMargin,
                            layoutParams.goneBottomMargin
                        )
                    }
                } else if (layoutParams.bottomToBottom != LayoutParams.UNSET_ID) {
                    val target: ConstraintWidget? = idToWidget[layoutParams.bottomToBottom]
                    if (target != null) {
                        widget.immediateConnect(
                            ConstraintAnchor.Type.BOTTOM, target,
                            ConstraintAnchor.Type.BOTTOM, layoutParams.bottomMargin,
                            layoutParams.goneBottomMargin
                        )
                    }
                }

                // Baseline constraint
                if (layoutParams.baselineToBaseline != LayoutParams.UNSET_ID) {
                    setWidgetBaseline(
                        widget, layoutParams, idToWidget,
                        layoutParams.baselineToBaseline, ConstraintAnchor.Type.BASELINE
                    )
                } else if (layoutParams.baselineToTop != LayoutParams.UNSET_ID) {
                    setWidgetBaseline(
                        widget, layoutParams, idToWidget,
                        layoutParams.baselineToTop, ConstraintAnchor.Type.TOP
                    )
                } else if (layoutParams.baselineToBottom != LayoutParams.UNSET_ID) {
                    setWidgetBaseline(
                        widget, layoutParams, idToWidget,
                        layoutParams.baselineToBottom, ConstraintAnchor.Type.BOTTOM
                    )
                }
                if (resolvedHorizontalBias >= 0) {
                    widget.setHorizontalBiasPercent(resolvedHorizontalBias)
                }
                if (layoutParams.verticalBias >= 0) {
                    widget.setVerticalBiasPercent(layoutParams.verticalBias)
                }
            }
            if (isInEditMode && (layoutParams.editorAbsoluteX != LayoutParams.UNSET
                        || layoutParams.editorAbsoluteY != LayoutParams.UNSET)
            ) {
                widget.setOrigin(layoutParams.editorAbsoluteX, layoutParams.editorAbsoluteY)
            }

            // FIXME: need to agree on the correct magic value for this
            //  rather than simply using zero.
            if (!layoutParams.mHorizontalDimensionFixed) {
                if (layoutParams.width == MATCH_PARENT) {
                    if (layoutParams.constrainedWidth) {
                        widget.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
                    } else {
                        widget.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_PARENT)
                    }
                    widget.getAnchor(ConstraintAnchor.Type.LEFT).mMargin = layoutParams.leftMargin
                    widget.getAnchor(ConstraintAnchor.Type.RIGHT).mMargin = layoutParams.rightMargin
                } else {
                    widget.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
                    widget.setWidth(0)
                }
            } else {
                widget.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
                widget.setWidth(layoutParams.width)
                if (layoutParams.width == WRAP_CONTENT) {
                    widget.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
                }
            }
            if (!layoutParams.mVerticalDimensionFixed) {
                if (layoutParams.height == MATCH_PARENT) {
                    if (layoutParams.constrainedHeight) {
                        widget.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
                    } else {
                        widget.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_PARENT)
                    }
                    widget.getAnchor(ConstraintAnchor.Type.TOP).mMargin = layoutParams.topMargin
                    widget.getAnchor(ConstraintAnchor.Type.BOTTOM).mMargin =
                        layoutParams.bottomMargin
                } else {
                    widget.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
                    widget.setHeight(0)
                }
            } else {
                widget.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
                widget.setHeight(layoutParams.height)
                if (layoutParams.height == WRAP_CONTENT) {
                    widget.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
                }
            }
            widget.setDimensionRatio(layoutParams.dimensionRatio)
            widget.setHorizontalWeight(layoutParams.horizontalWeight)
            widget.setVerticalWeight(layoutParams.verticalWeight)
            widget.setHorizontalChainStyle(layoutParams.horizontalChainStyle)
            widget.setVerticalChainStyle(layoutParams.verticalChainStyle)
            widget.setWrapBehaviorInParent(layoutParams.wrapBehaviorInParent)
            widget.setHorizontalMatchStyle(
                layoutParams.matchConstraintDefaultWidth,
                layoutParams.matchConstraintMinWidth, layoutParams.matchConstraintMaxWidth,
                layoutParams.matchConstraintPercentWidth
            )
            widget.setVerticalMatchStyle(
                layoutParams.matchConstraintDefaultHeight,
                layoutParams.matchConstraintMinHeight, layoutParams.matchConstraintMaxHeight,
                layoutParams.matchConstraintPercentHeight
            )
        }
    }

    private fun setWidgetBaseline(
        widget: ConstraintWidget,
        layoutParams: LayoutParams,
        idToWidget: MutableMap<String, ConstraintWidget>,
        baselineTarget: String,
        type: ConstraintAnchor.Type
    ) {
        val view: TView? = mChildrenByIds[baselineTarget]
        val target: ConstraintWidget? = idToWidget[baselineTarget]
        if (target != null && view != null && view.getLayoutParams() is LayoutParams) {
            layoutParams.mNeedsBaseline = true
            if (type == ConstraintAnchor.Type.BASELINE) { // baseline to baseline
                val targetParams = view.getLayoutParams() as LayoutParams
                targetParams.mNeedsBaseline = true
                targetParams.mWidget?.setHasBaseline(true)
            }
            val baseline: ConstraintAnchor = widget.getAnchor(ConstraintAnchor.Type.BASELINE)
            val targetAnchor: ConstraintAnchor = target.getAnchor(type)
            baseline.connect(
                targetAnchor, layoutParams.baselineMargin,
                layoutParams.goneBaselineMargin, true
            )
            widget.setHasBaseline(true)
            widget.getAnchor(ConstraintAnchor.Type.TOP).reset()
            widget.getAnchor(ConstraintAnchor.Type.BOTTOM).reset()
        }
    }

    private fun getTargetWidget(id: String): ConstraintWidget? {
        return if (id == LayoutParams.PARENT_ID) {
            mLayoutWidget
        } else {
            var view: TView? = mChildrenByIds[id]
            if (view == null) {
                view = self.findViewById(id)
                if (view != null && view != this && view.getParent() == this) {
                    onViewAdded(null, view)
                }
            }
            if (view == this) {
                return mLayoutWidget
            }
            if (view == null) null else (view.getLayoutParams() as LayoutParams).mWidget
        }
    }

    /**
     * @param view
     * @return
     */
    fun getViewWidget(view: TView?): ConstraintWidget? {
        if (view == this) {
            return mLayoutWidget
        }
        if (view != null) {
            if (view.getLayoutParams() is LayoutParams) {
                return (view.getLayoutParams() as LayoutParams).mWidget
            }
            view.setLayoutParams(generateLayoutParams(view.getLayoutParams()))
            if (view.getLayoutParams() is LayoutParams) {
                return (view.getLayoutParams() as LayoutParams).mWidget
            }
        }
        return null
    }

    /**
     * @param metrics
     * Fills metrics object
     */
    fun fillMetrics(metrics: Metrics?) {
        mMetrics = metrics
        mLayoutWidget.fillMetrics(metrics)
    }

    private var mOnMeasureWidthMeasureSpec = 0
    private var mOnMeasureHeightMeasureSpec = 0

    /**
     * Handles measuring a layout
     *
     * @param layout
     * @param optimizationLevel
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    protected fun resolveSystem(
        layout: ConstraintWidgetContainer,
        optimizationLevel: Int,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val widthMode: Int = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize: Int = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode: Int = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize: Int = MeasureSpec.getSize(heightMeasureSpec)
        val paddingY: Int = max(0, self.getPaddingTop())
        val paddingBottom: Int = max(0, self.getPaddingBottom())
        val paddingHeight = paddingY + paddingBottom
        val paddingWidth = paddingWidth
        val paddingX: Int
        mMeasurer.captureLayoutInfo(
            widthMeasureSpec, heightMeasureSpec, paddingY, paddingBottom,
            paddingWidth, paddingHeight
        )
        val paddingStart: Int = max(0, self.getPaddingStart())
        val paddingEnd: Int = max(0, self.getPaddingEnd())
        if (paddingStart > 0 || paddingEnd > 0) {
            if (isRtl) {
                paddingX = paddingEnd
            } else {
                paddingX = paddingStart
            }
        } else {
            paddingX = max(0, self.getPaddingLeft())
        }
        widthSize -= paddingWidth
        heightSize -= paddingHeight
        setSelfDimensionBehaviour(layout, widthMode, widthSize, heightMode, heightSize)
        layout.measure(
            optimizationLevel, widthMode, widthSize, heightMode, heightSize,
            mLastMeasureWidth, mLastMeasureHeight, paddingX, paddingY
        )
    }

    /**
     * Handles calling setMeasuredDimension()
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     * @param measuredWidth
     * @param measuredHeight
     * @param isWidthMeasuredTooSmall
     * @param isHeightMeasuredTooSmall
     */
    protected fun resolveMeasuredDimension(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        measuredWidth: Int,
        measuredHeight: Int,
        isWidthMeasuredTooSmall: Boolean,
        isHeightMeasuredTooSmall: Boolean
    ) {
        val childState = 0
        val heightPadding = mMeasurer.mPaddingHeight
        val widthPadding = mMeasurer.mPaddingWidth
        val androidLayoutWidth = measuredWidth + widthPadding
        val androidLayoutHeight = measuredHeight + heightPadding
        var resolvedWidthSize: Int = self.resolveSizeAndState(
            androidLayoutWidth,
            widthMeasureSpec, childState
        )
        var resolvedHeightSize: Int = self.resolveSizeAndState(
            androidLayoutHeight, heightMeasureSpec,
            childState shl TView.MEASURED_HEIGHT_STATE_SHIFT
        )
        resolvedWidthSize = resolvedWidthSize and TView.MEASURED_SIZE_MASK
        resolvedHeightSize = resolvedHeightSize and TView.MEASURED_SIZE_MASK
        resolvedWidthSize = min(mMaxWidth, resolvedWidthSize)
        resolvedHeightSize = min(mMaxHeight, resolvedHeightSize)
        if (isWidthMeasuredTooSmall) {
            resolvedWidthSize = resolvedWidthSize or TView.MEASURED_STATE_TOO_SMALL
        }
        if (isHeightMeasuredTooSmall) {
            resolvedHeightSize = resolvedHeightSize or TView.MEASURED_STATE_TOO_SMALL
        }
        self.setMeasuredDimension(resolvedWidthSize, resolvedHeightSize)
        mLastMeasureWidth = resolvedWidthSize
        mLastMeasureHeight = resolvedHeightSize
    }

    /**
     * {@inheritDoc}
     */
    protected open fun onMeasure(sup: TView?, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var time: Long = 0
        if (mMetrics != null) {
            time = nanoTime()
            mMetrics!!.mChildCount = self.getChildCount().toLong()
            mMetrics!!.mMeasureCalls++
        }
        mDirtyHierarchy =
            mDirtyHierarchy or dynamicUpdateConstraints(widthMeasureSpec, heightMeasureSpec)
        val sameSpecsAsPreviousMeasure// TODO re-enable
                = false && (mOnMeasureWidthMeasureSpec == widthMeasureSpec
                && mOnMeasureHeightMeasureSpec == heightMeasureSpec)
        if (!mDirtyHierarchy && !sameSpecsAsPreviousMeasure) {
            // it's possible that, if we are already marked for a relayout,
            // a view would not call to request a layout;
            // in that case we'd miss updating the hierarchy correctly
            // (window insets change may do that -- we receive
            // a second onMeasure before onLayout).
            // We have to iterate on our children to verify that none set a request layout flag...
            val count: Int = self.getChildCount()
            for (i in 0 until count) {
                val child: TView = self.getChildAt(i)
                if (child.isLayoutRequested()) {
                    if (I_DEBUG) {
                        println(
                            "### CHILD " + child
                                    + " REQUESTED LAYOUT, FORCE DIRTY HIERARCHY"
                        )
                    }
                    mDirtyHierarchy = true
                    break
                }
            }
        }
        if (!mDirtyHierarchy) {
            if (sameSpecsAsPreviousMeasure) {
                resolveMeasuredDimension(
                    widthMeasureSpec, heightMeasureSpec,
                    mLayoutWidget.getWidth(), mLayoutWidget.getHeight(),
                    mLayoutWidget.isWidthMeasuredTooSmall,
                    mLayoutWidget.isHeightMeasuredTooSmall
                )
                if (mMetrics != null) {
                    mMetrics!!.mMeasureDuration += nanoTime() - time
                }
                return
            }
            if (OPTIMIZE_HEIGHT_CHANGE
                && mOnMeasureWidthMeasureSpec == widthMeasureSpec && MeasureSpec.getMode(
                    widthMeasureSpec
                ) == MeasureSpec.EXACTLY && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST && MeasureSpec.getMode(
                    mOnMeasureHeightMeasureSpec
                ) == MeasureSpec.AT_MOST
            ) {
                val newSize: Int = MeasureSpec.getSize(heightMeasureSpec)
                if (I_DEBUG) {
                    println(
                        "### COMPATIBLE REQ " + newSize
                                + " >= ? " + mLayoutWidget.getHeight()
                    )
                }
                if (newSize >= mLayoutWidget.getHeight()
                    && !mLayoutWidget.isHeightMeasuredTooSmall
                ) {
                    mOnMeasureWidthMeasureSpec = widthMeasureSpec
                    mOnMeasureHeightMeasureSpec = heightMeasureSpec
                    resolveMeasuredDimension(
                        widthMeasureSpec, heightMeasureSpec,
                        mLayoutWidget.getWidth(), mLayoutWidget.getHeight(),
                        mLayoutWidget.isWidthMeasuredTooSmall,
                        mLayoutWidget.isHeightMeasuredTooSmall
                    )
                    if (mMetrics != null) {
                        mMetrics!!.mMeasureDuration += nanoTime() - time
                    }
                    return
                }
            }
        }
        mOnMeasureWidthMeasureSpec = widthMeasureSpec
        mOnMeasureHeightMeasureSpec = heightMeasureSpec
        if (I_DEBUG) {
            println(
                "### ON MEASURE " + mDirtyHierarchy
                        + " of " + mLayoutWidget.getDebugName()
                        + " onMeasure width: " + MeasureSpec.toString(widthMeasureSpec)
                        + " height: " + MeasureSpec.toString(heightMeasureSpec) + this
            )
        }
        mLayoutWidget.isRtl = (isRtl)
        if (mDirtyHierarchy) {
            mDirtyHierarchy = false
            if (updateHierarchy()) {
                mLayoutWidget.updateHierarchy()
            }
        }
        mLayoutWidget.fillMetrics(mMetrics)
        resolveSystem(mLayoutWidget, mOptimizationLevel, widthMeasureSpec, heightMeasureSpec)
        resolveMeasuredDimension(
            widthMeasureSpec, heightMeasureSpec,
            mLayoutWidget.getWidth(), mLayoutWidget.getHeight(),
            mLayoutWidget.isWidthMeasuredTooSmall, mLayoutWidget.isHeightMeasuredTooSmall
        )
        if (mMetrics != null) {
            mMetrics!!.mMeasureDuration += nanoTime() - time
        }
        if (I_DEBUG) {
            time = nanoTime() - time
            println(
                mLayoutWidget.getDebugName() + " (" + self.getChildCount()
                        + ") DONE onMeasure width: " + MeasureSpec.toString(widthMeasureSpec)
                        + " height: " + MeasureSpec.toString(heightMeasureSpec) + " => "
                        + mLastMeasureWidth + " x " + mLastMeasureHeight
                        + " lasted " + time
            )
        }
    }

    protected val isRtl: Boolean
        protected get() {
            return self.isRtl()
        }

    /**
     * Compute the padding width, taking in account RTL start/end padding if available and present.
     * @return padding width
     */
    private val paddingWidth: Int
        private get() {
            var widthPadding: Int = max(0, self.getPaddingLeft()) + max(0, self.getPaddingRight())
            var rtlPadding = max(0, self.getPaddingStart()) + max(0, self.getPaddingEnd())
            if (rtlPadding > 0) {
                widthPadding = rtlPadding
            }
            return widthPadding
        }

    protected fun setSelfDimensionBehaviour(
        layout: ConstraintWidgetContainer,
        widthMode: Int,
        widthSize: Int,
        heightMode: Int,
        heightSize: Int
    ) {
        val heightPadding = mMeasurer.mPaddingHeight
        val widthPadding = mMeasurer.mPaddingWidth
        var widthBehaviour: ConstraintWidget.DimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED
        var heightBehaviour: ConstraintWidget.DimensionBehaviour = ConstraintWidget.DimensionBehaviour.FIXED
        var desiredWidth = 0
        var desiredHeight = 0
        val childCount: Int = self.getChildCount()
        when (widthMode) {
            MeasureSpec.AT_MOST -> {
                widthBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
                desiredWidth = widthSize
                if (childCount == 0) {
                    desiredWidth = max(0, mMinWidth)
                }
            }
            MeasureSpec.UNSPECIFIED -> {
                widthBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
                if (childCount == 0) {
                    desiredWidth = max(0, mMinWidth)
                }
            }
            MeasureSpec.EXACTLY -> {
                desiredWidth = min(mMaxWidth - widthPadding, widthSize)
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST -> {
                heightBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
                desiredHeight = heightSize
                if (childCount == 0) {
                    desiredHeight = max(0, mMinHeight)
                }
            }
            MeasureSpec.UNSPECIFIED -> {
                heightBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
                if (childCount == 0) {
                    desiredHeight = max(0, mMinHeight)
                }
            }
            MeasureSpec.EXACTLY -> {
                desiredHeight = min(mMaxHeight - heightPadding, heightSize)
            }
        }
        if (desiredWidth != layout.getWidth() || desiredHeight != layout.getHeight()) {
            layout.invalidateMeasures()
        }
        layout.setX(0)
        layout.setY(0)
        layout.setMaxWidth(mMaxWidth - widthPadding)
        layout.setMaxHeight(mMaxHeight - heightPadding)
        layout.setMinWidth(0)
        layout.setMinHeight(0)
        layout.setHorizontalDimensionBehaviour(widthBehaviour)
        layout.setWidth(desiredWidth)
        layout.setVerticalDimensionBehaviour(heightBehaviour)
        layout.setHeight(desiredHeight)
        layout.setMinWidth(mMinWidth - widthPadding)
        layout.setMinHeight(mMinHeight - heightPadding)
    }

    /**
     * Set the State of the ConstraintLayout, causing it to load a particular ConstraintSet.
     * For states with variants the variant with matching width and height
     * constraintSet will be chosen
     *
     * @param id           the constraint set state
     * @param screenWidth  the width of the screen in pixels
     * @param screenHeight the height of the screen in pixels
     */
    open fun setState(id: String, screenWidth: Int, screenHeight: Int) {
        if (mConstraintLayoutSpec != null) {
            mConstraintLayoutSpec!!.updateConstraints(
                id,
                screenWidth.toFloat(),
                screenHeight.toFloat()
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    protected open fun onLayout(sup: TView?, changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (mMetrics != null) {
            mMetrics!!.mNumberOfLayouts++
        }
        if (I_DEBUG) {
            println(
                mLayoutWidget.getDebugName() + " onLayout changed: "
                        + changed + " left: " + left + " top: " + top
                        + " right: " + right + " bottom: " + bottom
                        + " (" + (right - left) + " x " + (bottom - top) + ")"
            )
        }
        val widgetsCount: Int = self.getChildCount()
        val isInEditMode: Boolean = self.isInEditMode()
        for (i in 0 until widgetsCount) {
            val child: TView = self.getChildAt(i)
            val params = child.getLayoutParams() as LayoutParams
            val widget: ConstraintWidget? = params.mWidget
            if (child.getVisibility() == TView.GONE && !params.mIsGuideline
                && !params.mIsHelper
                && !params.mIsVirtualGroup
                && !isInEditMode
            ) {
                // If we are in edit mode, let's layout the widget
                // so that they are at "the right place"
                // visually in the editor (as we get our positions from layoutlib)
                continue
            }
            if (params.mIsInPlaceholder) {
                continue
            }
            if(widget == null)
                continue
            val l: Int = widget.getX()
            val t: Int = widget.getY()
            val r: Int = l + widget.getWidth()
            val b: Int = t + widget.getHeight()
            if (I_DEBUG) {
                if (widget != null && child.getVisibility() != TView.GONE
                    && (child.getMeasuredWidth() != widget!!.getWidth()
                            || child.getMeasuredHeight() != widget!!.getHeight())
                ) {
                    val deltaX: Int = abs(child.getMeasuredWidth() - widget!!.getWidth())
                    val deltaY: Int = abs(child.getMeasuredHeight() - widget!!.getHeight())
                    if (deltaX > 1 || deltaY > 1) {
                        println(
                            "child " + child
                                    + " measuredWidth " + child.getMeasuredWidth()
                                    + " vs " + widget!!.getWidth()
                                    + " x measureHeight " + child.getMeasuredHeight()
                                    + " vs " + widget!!.getHeight()
                        )
                    }
                }
            }
            child.layout(l, t, r, b)
            if (child.getParentType() is Placeholder) {
                val content: TView? = (child.getParentType() as Placeholder).content
                if (content != null) {
                    content.setVisibility(TView.VISIBLE)
                    content.layout(l, t, r, b)
                }
            }
        }
        val helperCount: Int = mConstraintHelpers!!.size
        if (helperCount > 0) {
            for (i in 0 until helperCount) {
                val helper: ConstraintHelper? = mConstraintHelpers[i]
                helper!!.updatePostLayout(this)
            }
        }
    }
    /**
     * Return the current optimization level for the layout resolution
     *
     * @return the current level
     * @since 1.1
     */
    /**
     * Set the optimization for the layout resolution.
     *
     *
     * The optimization can be any of the following:
     *
     *  * Optimizer.OPTIMIZATION_NONE
     *  * Optimizer.OPTIMIZATION_STANDARD
     *  * a mask composed of specific optimizations
     *
     * The mask can be composed of any combination of the following:
     *
     *  * Optimizer.OPTIMIZATION_DIRECT
     *  * Optimizer.OPTIMIZATION_BARRIER
     *  * Optimizer.OPTIMIZATION_CHAIN  (experimental)
     *  * Optimizer.OPTIMIZATION_DIMENSIONS  (experimental)
     *
     * Note that the current implementation of
     * Optimizer.OPTIMIZATION_STANDARD is as a mask of DIRECT and BARRIER.
     *
     *
     * @param level optimization level
     * @since 1.1
     */
    var optimizationLevel: Int
        get() = mLayoutWidget.optimizationLevel
        set(level) {
            mOptimizationLevel = level
            mLayoutWidget.optimizationLevel = level
        }

    /**
     *
     */
    fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    /**
     * {@inheritDoc}
     */
    fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }

    /**
     * {@inheritDoc}
     */
    protected fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return if(p == null) generateDefaultLayoutParams() else LayoutParams(p)
    }

    /**
     * {@inheritDoc}
     */
    protected fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    /**
     * Sets a ConstraintSet object to manage constraints.
     * The ConstraintSet overrides LayoutParams of child views.
     *
     * @param set Layout children using ConstraintSet
     */
    fun setConstraintSet(set: ConstraintSet?) {
        mConstraintSet = set
    }

    /**
     * @param id the view id
     * @return the child view, can return null
     * Return a direct child view by its id if it exists
     */
    fun getViewById(id: String): TView? {
        return mChildrenByIds[id]
    }

    /**
     *
     */
    open fun dispatchDraw(sup: TView?, canvas: TCanvas) {
        if (mConstraintHelpers != null) {
            val helperCount: Int = mConstraintHelpers.size
            if (helperCount > 0) {
                for (i in 0 until helperCount) {
                    val helper: ConstraintHelper? = mConstraintHelpers[i]
                    helper!!.updatePreDraw(this)
                }
            }
        }
        sup?.dispatchDraw(canvas)
        if (I_DEBUG || self.isInEditMode()) {
            val cw: Int = self.getWidth()
            val ch: Int = self.getHeight()
            val ow = 1080f
            val oh = 1920f
            val count: Int = self.getChildCount()
            for (i in 0 until count) {
                val child: TView = self.getChildAt(i)
                if (child.getVisibility() == TView.GONE) {
                    continue
                }
                val tag: Any? = child.getTag()
                if (tag != null && tag is String) {
                    val coordinates = tag as String
                    val split: List<String> = coordinates.split(",")
                    if (split.size == 4) {
                        var x: Int = split[0].toInt()
                        var y: Int = split[1].toInt()
                        var w: Int = split[2].toInt()
                        var h: Int = split[3].toInt()
                        x = (x / ow * cw).toInt()
                        y = (y / oh * ch).toInt()
                        w = (w / ow * cw).toInt()
                        h = (h / oh * ch).toInt()
                        val paint = context.createPaint()
                        paint.setColor(TColor.argb(255f, 255f, 0f, 0f))
                        canvas.drawLine(x, y, x + w, y, paint)
                        canvas.drawLine(x + w, y, x + w, y + h, paint)
                        canvas.drawLine(x + w, y + h, x, y + h, paint)
                        canvas.drawLine(x, y + h, x, y, paint)
                        paint.setColor(TColor.argb(255f, 0f, 255f, 0f))
                        canvas.drawLine(x, y, x + w, y + h, paint)
                        canvas.drawLine(x, y + h, x + w, y, paint)
                    }
                }
            }
        }
        if (DEBUG_DRAW_CONSTRAINTS) {
            val count: Int = self.getChildCount()
            for (i in 0 until count) {
                val child: TView = self.getChildAt(i)
                if (child.getVisibility() == TView.GONE) {
                    continue
                }
                val widget: ConstraintWidget? = getViewWidget(child)
                if (widget?.mTop?.isConnected == true) {
                    val target: ConstraintWidget = widget!!.mTop.target!!.mOwner
                    val x1: Int = widget!!.getX() + widget!!.getWidth() / 2
                    val y1: Int = widget!!.getY()
                    val x2: Int = target!!.getX() + target!!.getWidth() / 2
                    var y2 = 0
                    y2 = if (widget!!.mTop.target!!.type == ConstraintAnchor.Type.TOP) {
                        target.getY()
                    } else {
                        target.getY() + target.getHeight()
                    }
                    val paint = context.createPaint()
                    paint.setColor(TColor.argb(255f, 255f, 0f, 0f))
                    paint.setStrokeWidth(4)
                    canvas.drawLine(x1, y1, x2, y2, paint)
                }
                if (widget?.mBottom?.isConnected == true) {
                    val target: ConstraintWidget = widget!!.mBottom.target!!.mOwner
                    val x1: Int = widget!!.getX() + widget!!.getWidth() / 2
                    val y1: Int = widget!!.getY() + widget!!.getHeight()
                    val x2: Int = target.getX() + target.getWidth() / 2
                    var y2 = 0
                    y2 = if (widget!!.mBottom.target!!.type == ConstraintAnchor.Type.TOP) {
                        target.getY()
                    } else {
                        target.getY() + target.getHeight()
                    }
                    val paint = context.createPaint()
                    paint.setStrokeWidth(4)
                    paint.setColor(TColor.argb(255f, 255f, 0f, 0f))
                    canvas.drawLine(x1, y1, x2, y2, paint)
                }
            }
        }
    }

    /**
     * Notify of constraints changed
     * @param constraintsChangedListener
     */
    fun setOnConstraintsChanged(constraintsChangedListener: ConstraintsChangedListener?) {
        // this.mConstraintsChangedListener = constraintsChangedListener;
        if (mConstraintLayoutSpec != null) {
            mConstraintLayoutSpec!!.setOnConstraintsChanged(constraintsChangedListener)
        }
    }

    /**
     * Load a layout description file from the resources.
     *
     * @param layoutDescription The resource id, or 0 to reset the layout description.
     */
    open fun loadLayoutDescription(layoutDescription: String) {
        if (layoutDescription != "") {
            try {
                mConstraintLayoutSpec = ConstraintLayoutStates(
                    context,
                    this, layoutDescription
                )
            } catch (e: NotFoundException) {
                mConstraintLayoutSpec = null
            }
        } else {
            mConstraintLayoutSpec = null
        }
    }

    /**
     * This class contains the different attributes specifying
     * how a view want to be laid out inside
     * a [ConstraintLayout]. For building up constraints at run time,
     * using [ConstraintSet] is recommended.
     */
    open class LayoutParams : MarginLayoutParams {
        /**
         * The distance of child (guideline) to the top or left edge of its parent.
         */
        var guideBegin = UNSET

        /**
         * The distance of child (guideline) to the bottom or right edge of its parent.
         */
        var guideEnd = UNSET

        /**
         * The ratio of the distance to the parent's sides
         */
        var guidePercent = UNSET.toFloat()

        /**
         * The ratio of the distance to the parent's sides
         */
        var guidelineUseRtl = true

        /**
         * Constrains the left side of a child to the left side of
         * a target child (contains the target child id).
         */
        var leftToLeft = UNSET_ID

        /**
         * Constrains the left side of a child to the right side of
         * a target child (contains the target child id).
         */
        var leftToRight = UNSET_ID

        /**
         * Constrains the right side of a child to the left side of
         * a target child (contains the target child id).
         */
        var rightToLeft = UNSET_ID

        /**
         * Constrains the right side of a child to the right side of
         * a target child (contains the target child id).
         */
        var rightToRight = UNSET_ID

        /**
         * Constrains the top side of a child to the top side of
         * a target child (contains the target child id).
         */
        var topToTop = UNSET_ID

        /**
         * Constrains the top side of a child to the bottom side of
         * a target child (contains the target child id).
         */
        var topToBottom = UNSET_ID

        /**
         * Constrains the bottom side of a child to the top side of
         * a target child (contains the target child id).
         */
        var bottomToTop = UNSET_ID

        /**
         * Constrains the bottom side of a child to the bottom side of
         * a target child (contains the target child id).
         */
        var bottomToBottom = UNSET_ID

        /**
         * Constrains the baseline of a child to the baseline of
         * a target child (contains the target child id).
         */
        var baselineToBaseline = UNSET_ID

        /**
         * Constrains the baseline of a child to the top of
         * a target child (contains the target child id).
         */
        var baselineToTop = UNSET_ID

        /**
         * Constrains the baseline of a child to the bottom of
         * a target child (contains the target child id).
         */
        var baselineToBottom = UNSET_ID

        /**
         * Constrains the center of a child to the center of
         * a target child (contains the target child id).
         */
        var circleConstraint = UNSET_ID

        /**
         * The radius used for a circular constraint
         */
        var circleRadius = 0

        /**
         * The angle used for a circular constraint]
         */
        var circleAngle = 0f

        /**
         * Constrains the start side of a child to the end side of
         * a target child (contains the target child id).
         */
        var startToEnd = UNSET_ID

        /**
         * Constrains the start side of a child to the start side of
         * a target child (contains the target child id).
         */
        var startToStart = UNSET_ID

        /**
         * Constrains the end side of a child to the start side of
         * a target child (contains the target child id).
         */
        var endToStart = UNSET_ID

        /**
         * Constrains the end side of a child to the end side of
         * a target child (contains the target child id).
         */
        var endToEnd = UNSET_ID

        /**
         * The left margin to use when the target is gone.
         */
        var goneLeftMargin = GONE_UNSET

        /**
         * The top margin to use when the target is gone.
         */
        var goneTopMargin = GONE_UNSET

        /**
         * The right margin to use when the target is gone
         */
        var goneRightMargin = GONE_UNSET

        /**
         * The bottom margin to use when the target is gone.
         */
        var goneBottomMargin = GONE_UNSET

        /**
         * The start margin to use when the target is gone.
         */
        var goneStartMargin = GONE_UNSET

        /**
         * The end margin to use when the target is gone.
         */
        var goneEndMargin = GONE_UNSET

        /**
         * The baseline margin to use when the target is gone.
         */
        var goneBaselineMargin = GONE_UNSET

        /**
         * The baseline margin.
         */
        var baselineMargin = 0
        ///////////////////////////////////////////////////////////////////////////////////////////
        // Layout margins handling TODO: re-activate in 3.0
        ///////////////////////////////////////////////////////////////////////////////////////////
        /**
         * The left margin.
         */
        // public int leftMargin = 0;
        /**
         * The right margin.
         */
        // public int rightMargin = 0;
        // int originalLeftMargin = 0;
        // int originalRightMargin = 0;
        /**
         * The top margin.
         */
        // public int topMargin = 0;
        /**
         * The bottom margin.
         */
        // public int bottomMargin = 0;
        /**
         * The start margin.
         */
        // public int startMargin = UNSET;
        /**
         * The end margin.
         */
        // public int endMargin = UNSET;
        // boolean isRtl = false;
        // int layoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR;
        var mWidthSet = true // need to be set to false when we reactivate this in 3.0
        var mHeightSet = true // need to be set to false when we reactivate this in 3.0
        ///////////////////////////////////////////////////////////////////////////////////////////
        /**
         * The ratio between two connections when
         * the left and right (or start and end) sides are constrained.
         */
        var horizontalBias = 0.5f

        /**
         * The ratio between two connections when the top and bottom sides are constrained.
         */
        var verticalBias = 0.5f

        /**
         * The ratio information.
         */
        var dimensionRatio: String? = null

        /**
         * The ratio between the width and height of the child.
         */
        var mDimensionRatioValue = 0f

        /**
         * The child's side to constrain using dimensRatio.
         */
        var mDimensionRatioSide = VERTICAL

        /**
         * The child's weight that we can use to distribute the available horizontal space
         * in a chain, if the dimension behaviour is set to MATCH_CONSTRAINT
         */
        var horizontalWeight = UNSET.toFloat()

        /**
         * The child's weight that we can use to distribute the available vertical space
         * in a chain, if the dimension behaviour is set to MATCH_CONSTRAINT
         */
        var verticalWeight = UNSET.toFloat()

        /**
         * If the child is the start of a horizontal chain, this attribute will drive how
         * the elements of the chain will be positioned. The possible values are:
         *
         *  * [.CHAIN_SPREAD] -- the elements will be spread out
         *  * [.CHAIN_SPREAD_INSIDE] -- similar, but the endpoints of the chain will not
         * be spread out
         *  * [.CHAIN_PACKED] -- the elements of the chain will be packed together. The
         * horizontal bias attribute of the child will then affect the positioning of the packed
         * elements
         *
         */
        var horizontalChainStyle = CHAIN_SPREAD

        /**
         * If the child is the start of a vertical chain, this attribute will drive how
         * the elements of the chain will be positioned. The possible values are:
         *
         *  * [.CHAIN_SPREAD] -- the elements will be spread out
         *  * [.CHAIN_SPREAD_INSIDE] -- similar, but the endpoints of the chain will not
         * be spread out
         *  * [.CHAIN_PACKED] -- the elements of the chain will be packed together. The
         * vertical bias attribute of the child will then affect the positioning of the packed
         * elements
         *
         */
        var verticalChainStyle = CHAIN_SPREAD

        /**
         * Define how the widget horizontal dimension is handled when set to MATCH_CONSTRAINT
         *
         *  * [.MATCH_CONSTRAINT_SPREAD] -- the default. The dimension will expand up to
         * the constraints, minus margins
         *  * [.MATCH_CONSTRAINT_WRAP] -- DEPRECATED -- use instead WRAP_CONTENT and
         * constrainedWidth=true<br></br>
         * The dimension will be the same as WRAP_CONTENT, unless the size ends
         * up too large for the constraints;
         * in that case the dimension will expand up to the constraints, minus margins
         * This attribute may not be applied if
         * the widget is part of a chain in that dimension.
         *  * [.MATCH_CONSTRAINT_PERCENT] -- The dimension will be a percent of another
         * widget (by default, the parent)
         *
         */
        var matchConstraintDefaultWidth = MATCH_CONSTRAINT_SPREAD

        /**
         * Define how the widget vertical dimension is handled when set to MATCH_CONSTRAINT
         *
         *  * [.MATCH_CONSTRAINT_SPREAD] -- the default. The dimension will expand up to
         * the constraints, minus margins
         *  * [.MATCH_CONSTRAINT_WRAP] -- DEPRECATED -- use instead WRAP_CONTENT and
         * constrainedWidth=true<br></br>
         * The dimension will be the same as WRAP_CONTENT, unless the size ends
         * up too large for the constraints;
         * in that case the dimension will expand up to the constraints, minus margins
         * This attribute may not be applied if the widget is
         * part of a chain in that dimension.
         *  * [.MATCH_CONSTRAINT_PERCENT] -- The dimension will be a percent of another
         * widget (by default, the parent)
         *
         */
        var matchConstraintDefaultHeight = MATCH_CONSTRAINT_SPREAD

        /**
         * Specify a minimum width size for the widget.
         * It will only apply if the size of the widget
         * is set to MATCH_CONSTRAINT. Don't apply if the widget is part of a horizontal chain.
         */
        var matchConstraintMinWidth = 0

        /**
         * Specify a minimum height size for the widget.
         * It will only apply if the size of the widget
         * is set to MATCH_CONSTRAINT. Don't apply if the widget is part of a vertical chain.
         */
        var matchConstraintMinHeight = 0

        /**
         * Specify a maximum width size for the widget.
         * It will only apply if the size of the widget
         * is set to MATCH_CONSTRAINT. Don't apply if the widget is part of a horizontal chain.
         */
        var matchConstraintMaxWidth = 0

        /**
         * Specify a maximum height size for the widget.
         * It will only apply if the size of the widget
         * is set to MATCH_CONSTRAINT. Don't apply if the widget is part of a vertical chain.
         */
        var matchConstraintMaxHeight = 0

        /**
         * Specify the percentage when using the match constraint percent mode. From 0 to 1.
         */
        var matchConstraintPercentWidth = 1f

        /**
         * Specify the percentage when using the match constraint percent mode. From 0 to 1.
         */
        var matchConstraintPercentHeight = 1f

        /**
         * The design time location of the left side of the child.
         * Used at design time for a horizontally unconstrained child.
         */
        var editorAbsoluteX = UNSET

        /**
         * The design time location of the right side of the child.
         * Used at design time for a vertically unconstrained child.
         */
        var editorAbsoluteY = UNSET
        var orientation = UNSET

        /**
         * Specify if the horizontal dimension is constrained in
         * case both left & right constraints are set
         * and the widget dimension is not a fixed dimension. By default,
         * if a widget is set to WRAP_CONTENT,
         * we will treat that dimension as a fixed dimension,
         * meaning the dimension will not change regardless
         * of constraints. Setting this attribute to true allows the dimension to change
         * in order to respect constraints.
         */
        var constrainedWidth = false

        /**
         * Specify if the vertical dimension is constrained in case both
         * top & bottom constraints are set and the widget dimension is not a fixed dimension.
         * By default, if a widget is set to WRAP_CONTENT,
         * we will treat that dimension as a fixed dimension,
         * meaning the dimension will not change regardless
         * of constraints. Setting this attribute to true allows the
         * dimension to change in order to respect constraints.
         */
        var constrainedHeight = false
        /**
         * Tag that can be used to identify a view as being a member of a group.
         * Which can be used for Helpers or in MotionLayout
         *
         * @return tag string or null if not defined
         */
        /**
         * Define a category of view to be used by helpers and motionLayout
         */
        var constraintTag: String? = null

        /**
         * Specify how this view is taken in account during the parent's wrap computation
         *
         * Can be either of:
         * WRAP_BEHAVIOR_INCLUDED the widget is taken in account for the wrap (default)
         * WRAP_BEHAVIOR_HORIZONTAL_ONLY the widget will be included in the wrap only horizontally
         * WRAP_BEHAVIOR_VERTICAL_ONLY the widget will be included in the wrap only vertically
         * WRAP_BEHAVIOR_SKIPPED the widget is not part of the wrap computation
         */
        var wrapBehaviorInParent = WRAP_BEHAVIOR_INCLUDED

        // Internal use only
        var mHorizontalDimensionFixed = true
        var mVerticalDimensionFixed = true
        var mNeedsBaseline = false
        var mIsGuideline = false
        var mIsHelper = false
        var mIsInPlaceholder = false
        var mIsVirtualGroup = false
        var mResolvedLeftToLeft = UNSET_ID
        var mResolvedLeftToRight = UNSET_ID
        var mResolvedRightToLeft = UNSET_ID
        var mResolvedRightToRight = UNSET_ID
        var mResolveGoneLeftMargin = GONE_UNSET
        var mResolveGoneRightMargin = GONE_UNSET
        var mResolvedHorizontalBias = 0.5f
        var mResolvedGuideBegin = 0
        var mResolvedGuideEnd = 0
        var mResolvedGuidePercent = 0f
        var mWidget: ConstraintWidget? = ConstraintWidget()

        /**
         *
         */
        val constraintWidget: ConstraintWidget?
            get() = mWidget

        /**
         * @param text
         */
        fun setWidgetDebugName(text: String) {
            mWidget?.setDebugName(text)
        }

        /**
         * Reset the ConstraintWidget
         */
        fun reset() {
            if (mWidget != null) {
                mWidget!!.reset()
            }
        }

        var helped = false

        /**
         * Create a LayoutParams base on an existing layout Params
         *
         * @param params the Layout Params to be copied
         */
        constructor(params: ViewGroup.LayoutParams) : super(params) {

            // if the params is an instance of ViewGroup.MarginLayoutParams,
            // we should also copy margin relevant properties.
            if (params is MarginLayoutParams) {
                val marginSource: MarginLayoutParams = params as MarginLayoutParams
                this.leftMargin = marginSource.leftMargin
                this.rightMargin = marginSource.rightMargin
                this.topMargin = marginSource.topMargin
                this.bottomMargin = marginSource.bottomMargin
                this.setMarginStart(marginSource.getMarginStart())
                this.setMarginEnd(marginSource.getMarginEnd())
            }
            if (params !is LayoutParams) {
                return
            }
            val source = params as LayoutParams
            ///////////////////////////////////////////////////////////////////////////////////////
            // Layout margins handling TODO: re-activate in 3.0
            ///////////////////////////////////////////////////////////////////////////////////////
            // this.layoutDirection = source.layoutDirection;
            // this.isRtl = source.isRtl;
            // this.originalLeftMargin = source.originalLeftMargin;
            // this.originalRightMargin = source.originalRightMargin;
            // this.startMargin = source.startMargin;
            // this.endMargin = source.endMargin;
            // this.leftMargin = source.leftMargin;
            // this.rightMargin = source.rightMargin;
            // this.topMargin = source.topMargin;
            // this.bottomMargin = source.bottomMargin;
            ///////////////////////////////////////////////////////////////////////////////////////
            guideBegin = source.guideBegin
            guideEnd = source.guideEnd
            guidePercent = source.guidePercent
            guidelineUseRtl = source.guidelineUseRtl
            leftToLeft = source.leftToLeft
            leftToRight = source.leftToRight
            rightToLeft = source.rightToLeft
            rightToRight = source.rightToRight
            topToTop = source.topToTop
            topToBottom = source.topToBottom
            bottomToTop = source.bottomToTop
            bottomToBottom = source.bottomToBottom
            baselineToBaseline = source.baselineToBaseline
            baselineToTop = source.baselineToTop
            baselineToBottom = source.baselineToBottom
            circleConstraint = source.circleConstraint
            circleRadius = source.circleRadius
            circleAngle = source.circleAngle
            startToEnd = source.startToEnd
            startToStart = source.startToStart
            endToStart = source.endToStart
            endToEnd = source.endToEnd
            goneLeftMargin = source.goneLeftMargin
            goneTopMargin = source.goneTopMargin
            goneRightMargin = source.goneRightMargin
            goneBottomMargin = source.goneBottomMargin
            goneStartMargin = source.goneStartMargin
            goneEndMargin = source.goneEndMargin
            goneBaselineMargin = source.goneBaselineMargin
            baselineMargin = source.baselineMargin
            horizontalBias = source.horizontalBias
            verticalBias = source.verticalBias
            dimensionRatio = source.dimensionRatio
            mDimensionRatioValue = source.mDimensionRatioValue
            mDimensionRatioSide = source.mDimensionRatioSide
            horizontalWeight = source.horizontalWeight
            verticalWeight = source.verticalWeight
            horizontalChainStyle = source.horizontalChainStyle
            verticalChainStyle = source.verticalChainStyle
            constrainedWidth = source.constrainedWidth
            constrainedHeight = source.constrainedHeight
            matchConstraintDefaultWidth = source.matchConstraintDefaultWidth
            matchConstraintDefaultHeight = source.matchConstraintDefaultHeight
            matchConstraintMinWidth = source.matchConstraintMinWidth
            matchConstraintMaxWidth = source.matchConstraintMaxWidth
            matchConstraintMinHeight = source.matchConstraintMinHeight
            matchConstraintMaxHeight = source.matchConstraintMaxHeight
            matchConstraintPercentWidth = source.matchConstraintPercentWidth
            matchConstraintPercentHeight = source.matchConstraintPercentHeight
            editorAbsoluteX = source.editorAbsoluteX
            editorAbsoluteY = source.editorAbsoluteY
            orientation = source.orientation
            mHorizontalDimensionFixed = source.mHorizontalDimensionFixed
            mVerticalDimensionFixed = source.mVerticalDimensionFixed
            mNeedsBaseline = source.mNeedsBaseline
            mIsGuideline = source.mIsGuideline
            mResolvedLeftToLeft = source.mResolvedLeftToLeft
            mResolvedLeftToRight = source.mResolvedLeftToRight
            mResolvedRightToLeft = source.mResolvedRightToLeft
            mResolvedRightToRight = source.mResolvedRightToRight
            mResolveGoneLeftMargin = source.mResolveGoneLeftMargin
            mResolveGoneRightMargin = source.mResolveGoneRightMargin
            mResolvedHorizontalBias = source.mResolvedHorizontalBias
            constraintTag = source.constraintTag
            wrapBehaviorInParent = source.wrapBehaviorInParent
            mWidget = source.mWidget
            mWidthSet = source.mWidthSet
            mHeightSet = source.mHeightSet
        }

        private object Table {
            const val UNUSED = 0
            const val ANDROID_ORIENTATION = 1
            const val LAYOUT_CONSTRAINT_CIRCLE = 2
            const val LAYOUT_CONSTRAINT_CIRCLE_RADIUS = 3
            const val LAYOUT_CONSTRAINT_CIRCLE_ANGLE = 4
            const val LAYOUT_CONSTRAINT_GUIDE_BEGIN = 5
            const val LAYOUT_CONSTRAINT_GUIDE_END = 6
            const val LAYOUT_CONSTRAINT_GUIDE_PERCENT = 7
            const val LAYOUT_CONSTRAINT_LEFT_TO_LEFT_OF = 8
            const val LAYOUT_CONSTRAINT_LEFT_TO_RIGHT_OF = 9
            const val LAYOUT_CONSTRAINT_RIGHT_TO_LEFT_OF = 10
            const val LAYOUT_CONSTRAINT_RIGHT_TO_RIGHT_OF = 11
            const val LAYOUT_CONSTRAINT_TOP_TO_TOP_OF = 12
            const val LAYOUT_CONSTRAINT_TOP_TO_BOTTOM_OF = 13
            const val LAYOUT_CONSTRAINT_BOTTOM_TO_TOP_OF = 14
            const val LAYOUT_CONSTRAINT_BOTTOM_TO_BOTTOM_OF = 15
            const val LAYOUT_CONSTRAINT_BASELINE_TO_BASELINE_OF = 16
            const val LAYOUT_CONSTRAINT_START_TO_END_OF = 17
            const val LAYOUT_CONSTRAINT_START_TO_START_OF = 18
            const val LAYOUT_CONSTRAINT_END_TO_START_OF = 19
            const val LAYOUT_CONSTRAINT_END_TO_END_OF = 20
            const val LAYOUT_GONE_MARGIN_LEFT = 21
            const val LAYOUT_GONE_MARGIN_TOP = 22
            const val LAYOUT_GONE_MARGIN_RIGHT = 23
            const val LAYOUT_GONE_MARGIN_BOTTOM = 24
            const val LAYOUT_GONE_MARGIN_START = 25
            const val LAYOUT_GONE_MARGIN_END = 26
            const val LAYOUT_CONSTRAINED_WIDTH = 27
            const val LAYOUT_CONSTRAINED_HEIGHT = 28
            const val LAYOUT_CONSTRAINT_HORIZONTAL_BIAS = 29
            const val LAYOUT_CONSTRAINT_VERTICAL_BIAS = 30
            const val LAYOUT_CONSTRAINT_WIDTH_DEFAULT = 31
            const val LAYOUT_CONSTRAINT_HEIGHT_DEFAULT = 32
            const val LAYOUT_CONSTRAINT_WIDTH_MIN = 33
            const val LAYOUT_CONSTRAINT_WIDTH_MAX = 34
            const val LAYOUT_CONSTRAINT_WIDTH_PERCENT = 35
            const val LAYOUT_CONSTRAINT_HEIGHT_MIN = 36
            const val LAYOUT_CONSTRAINT_HEIGHT_MAX = 37
            const val LAYOUT_CONSTRAINT_HEIGHT_PERCENT = 38
            const val LAYOUT_CONSTRAINT_LEFT_CREATOR = 39
            const val LAYOUT_CONSTRAINT_TOP_CREATOR = 40
            const val LAYOUT_CONSTRAINT_RIGHT_CREATOR = 41
            const val LAYOUT_CONSTRAINT_BOTTOM_CREATOR = 42
            const val LAYOUT_CONSTRAINT_BASELINE_CREATOR = 43
            const val LAYOUT_CONSTRAINT_DIMENSION_RATIO = 44
            const val LAYOUT_CONSTRAINT_HORIZONTAL_WEIGHT = 45
            const val LAYOUT_CONSTRAINT_VERTICAL_WEIGHT = 46
            const val LAYOUT_CONSTRAINT_HORIZONTAL_CHAINSTYLE = 47
            const val LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE = 48
            const val LAYOUT_EDITOR_ABSOLUTEX = 49
            const val LAYOUT_EDITOR_ABSOLUTEY = 50
            const val LAYOUT_CONSTRAINT_TAG = 51
            const val LAYOUT_CONSTRAINT_BASELINE_TO_TOP_OF = 52
            const val LAYOUT_CONSTRAINT_BASELINE_TO_BOTTOM_OF = 53
            const val LAYOUT_MARGIN_BASELINE = 54
            const val LAYOUT_GONE_MARGIN_BASELINE = 55

            ///////////////////////////////////////////////////////////////////////////////////////
            // Layout margins handling TODO: re-activate in 3.0
            ///////////////////////////////////////////////////////////////////////////////////////
            // public static final int LAYOUT_MARGIN_LEFT = 56;
            // public static final int LAYOUT_MARGIN_RIGHT = 57;
            // public static final int LAYOUT_MARGIN_TOP = 58;
            // public static final int LAYOUT_MARGIN_BOTTOM = 59;
            // public static final int LAYOUT_MARGIN_START = 60;
            // public static final int LAYOUT_MARGIN_END = 61;
            // public static final int LAYOUT_WIDTH = 62;
            // public static final int LAYOUT_HEIGHT = 63;
            ///////////////////////////////////////////////////////////////////////////////////////
            const val LAYOUT_CONSTRAINT_WIDTH = 64
            const val LAYOUT_CONSTRAINT_HEIGHT = 65
            const val LAYOUT_WRAP_BEHAVIOR_IN_PARENT = 66
            const val GUIDELINE_USE_RTL = 67
            val sMap = mutableMapOf<String, Int>()

            init {
                ///////////////////////////////////////////////////////////////////////////////////
                // Layout margins handling TODO: re-activate in 3.0
                ///////////////////////////////////////////////////////////////////////////////////
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_width,
                // LAYOUT_WIDTH);
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_height,
                // LAYOUT_HEIGHT);
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_marginLeft,
                // LAYOUT_MARGIN_LEFT);
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_marginRight,
                // LAYOUT_MARGIN_RIGHT);
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_marginTop,
                // LAYOUT_MARGIN_TOP);
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_marginBottom,
                // LAYOUT_MARGIN_BOTTOM);
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_marginStart,
                // LAYOUT_MARGIN_START);
                // map.append(R.styleable.ConstraintLayout_Layout_android_layout_marginEnd,
                // LAYOUT_MARGIN_END);
                //////////////////////////////////////////////////////////////////////////////////
                sMap.put(
                    "layout_constraintWidth",
                    LAYOUT_CONSTRAINT_WIDTH
                )
                sMap.put(
                    "layout_constraintHeight",
                    LAYOUT_CONSTRAINT_HEIGHT
                )
                sMap.put(
                    "layout_constraintLeft_toLeftOf",
                    LAYOUT_CONSTRAINT_LEFT_TO_LEFT_OF
                )
                sMap.put(
                    "layout_constraintLeft_toRightOf",
                    LAYOUT_CONSTRAINT_LEFT_TO_RIGHT_OF
                )
                sMap.put(
                    "layout_constraintRight_toLeftOf",
                    LAYOUT_CONSTRAINT_RIGHT_TO_LEFT_OF
                )
                sMap.put(
                    "layout_constraintRight_toRightOf",
                    LAYOUT_CONSTRAINT_RIGHT_TO_RIGHT_OF
                )
                sMap.put(
                    "layout_constraintTop_toTopOf",
                    LAYOUT_CONSTRAINT_TOP_TO_TOP_OF
                )
                sMap.put(
                    "layout_constraintTop_toBottomOf",
                    LAYOUT_CONSTRAINT_TOP_TO_BOTTOM_OF
                )
                sMap.put(
                    "layout_constraintBottom_toTopOf",
                    LAYOUT_CONSTRAINT_BOTTOM_TO_TOP_OF
                )
                sMap.put(
                    "layout_constraintBottom_toBottomOf",
                    LAYOUT_CONSTRAINT_BOTTOM_TO_BOTTOM_OF
                )
                sMap.put(
                    "layout_constraintBaseline_toBaselineOf",
                    LAYOUT_CONSTRAINT_BASELINE_TO_BASELINE_OF
                )
                sMap.put(
                    "layout_constraintBaseline_toTopOf",
                    LAYOUT_CONSTRAINT_BASELINE_TO_TOP_OF
                )
                sMap.put(
                    "layout_constraintBaseline_toBottomOf",
                    LAYOUT_CONSTRAINT_BASELINE_TO_BOTTOM_OF
                )
                sMap.put(
                    "layout_constraintCircle",
                    LAYOUT_CONSTRAINT_CIRCLE
                )
                sMap.put(
                    "layout_constraintCircleRadius",
                    LAYOUT_CONSTRAINT_CIRCLE_RADIUS
                )
                sMap.put(
                    "layout_constraintCircleAngle",
                    LAYOUT_CONSTRAINT_CIRCLE_ANGLE
                )
                sMap.put(
                    "layout_editor_absoluteX",
                    LAYOUT_EDITOR_ABSOLUTEX
                )
                sMap.put(
                    "layout_editor_absoluteY",
                    LAYOUT_EDITOR_ABSOLUTEY
                )
                sMap.put(
                    "layout_constraintGuide_begin",
                    LAYOUT_CONSTRAINT_GUIDE_BEGIN
                )
                sMap.put(
                    "layout_constraintGuide_end",
                    LAYOUT_CONSTRAINT_GUIDE_END
                )
                sMap.put(
                    "layout_constraintGuide_percent",
                    LAYOUT_CONSTRAINT_GUIDE_PERCENT
                )
                sMap.put(
                    "guidelineUseRtl",
                    GUIDELINE_USE_RTL
                )
                sMap.put(
                    "android_orientation",
                    ANDROID_ORIENTATION
                )
                sMap.put(
                    "layout_constraintStart_toEndOf",
                    LAYOUT_CONSTRAINT_START_TO_END_OF
                )
                sMap.put(
                    "layout_constraintStart_toStartOf",
                    LAYOUT_CONSTRAINT_START_TO_START_OF
                )
                sMap.put(
                    "layout_constraintEnd_toStartOf",
                    LAYOUT_CONSTRAINT_END_TO_START_OF
                )
                sMap.put(
                    "layout_constraintEnd_toEndOf",
                    LAYOUT_CONSTRAINT_END_TO_END_OF
                )
                sMap.put(
                    "layout_goneMarginLeft",
                    LAYOUT_GONE_MARGIN_LEFT
                )
                sMap.put(
                    "layout_goneMarginTop",
                    LAYOUT_GONE_MARGIN_TOP
                )
                sMap.put(
                    "layout_goneMarginRight",
                    LAYOUT_GONE_MARGIN_RIGHT
                )
                sMap.put(
                    "layout_goneMarginBottom",
                    LAYOUT_GONE_MARGIN_BOTTOM
                )
                sMap.put(
                    "layout_goneMarginStart",
                    LAYOUT_GONE_MARGIN_START
                )
                sMap.put(
                    "layout_goneMarginEnd",
                    LAYOUT_GONE_MARGIN_END
                )
                sMap.put(
                    "layout_goneMarginBaseline",
                    LAYOUT_GONE_MARGIN_BASELINE
                )
                sMap.put(
                    "layout_marginBaseline",
                    LAYOUT_MARGIN_BASELINE
                )
                sMap.put(
                    "layout_constraintHorizontal_bias",
                    LAYOUT_CONSTRAINT_HORIZONTAL_BIAS
                )
                sMap.put(
                    "layout_constraintVertical_bias",
                    LAYOUT_CONSTRAINT_VERTICAL_BIAS
                )
                sMap.put(
                    "layout_constraintDimensionRatio",
                    LAYOUT_CONSTRAINT_DIMENSION_RATIO
                )
                sMap.put(
                    "layout_constraintHorizontal_weight",
                    LAYOUT_CONSTRAINT_HORIZONTAL_WEIGHT
                )
                sMap.put(
                    "layout_constraintVertical_weight",
                    LAYOUT_CONSTRAINT_VERTICAL_WEIGHT
                )
                sMap.put(
                    "layout_constraintHorizontal_chainStyle",
                    LAYOUT_CONSTRAINT_HORIZONTAL_CHAINSTYLE
                )
                sMap.put(
                    "layout_constraintVertical_chainStyle",
                    LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE
                )
                sMap.put(
                    "layout_constrainedWidth",
                    LAYOUT_CONSTRAINED_WIDTH
                )
                sMap.put(
                    "layout_constrainedHeight",
                    LAYOUT_CONSTRAINED_HEIGHT
                )
                sMap.put(
                    "layout_constraintWidth_default",
                    LAYOUT_CONSTRAINT_WIDTH_DEFAULT
                )
                sMap.put(
                    "layout_constraintHeight_default",
                    LAYOUT_CONSTRAINT_HEIGHT_DEFAULT
                )
                sMap.put(
                    "layout_constraintWidth_min",
                    LAYOUT_CONSTRAINT_WIDTH_MIN
                )
                sMap.put(
                    "layout_constraintWidth_max",
                    LAYOUT_CONSTRAINT_WIDTH_MAX
                )
                sMap.put(
                    "layout_constraintWidth_percent",
                    LAYOUT_CONSTRAINT_WIDTH_PERCENT
                )
                sMap.put(
                    "layout_constraintHeight_min",
                    LAYOUT_CONSTRAINT_HEIGHT_MIN
                )
                sMap.put(
                    "layout_constraintHeight_max",
                    LAYOUT_CONSTRAINT_HEIGHT_MAX
                )
                sMap.put(
                    "layout_constraintHeight_percent",
                    LAYOUT_CONSTRAINT_HEIGHT_PERCENT
                )
                sMap.put(
                    "layout_constraintLeft_creator",
                    LAYOUT_CONSTRAINT_LEFT_CREATOR
                )
                sMap.put(
                    "layout_constraintTop_creator",
                    LAYOUT_CONSTRAINT_TOP_CREATOR
                )
                sMap.put(
                    "layout_constraintRight_creator",
                    LAYOUT_CONSTRAINT_RIGHT_CREATOR
                )
                sMap.put(
                    "layout_constraintBottom_creator",
                    LAYOUT_CONSTRAINT_BOTTOM_CREATOR
                )
                sMap.put(
                    "layout_constraintBaseline_creator",
                    LAYOUT_CONSTRAINT_BASELINE_CREATOR
                )
                sMap.put(
                    "layout_constraintTag",
                    LAYOUT_CONSTRAINT_TAG
                )
                sMap.put(
                    "layout_wrapBehaviorInParent",
                    LAYOUT_WRAP_BEHAVIOR_IN_PARENT
                )
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Layout margins handling TODO: re-activate in 3.0
        ///////////////////////////////////////////////////////////////////////////////////////////
        /*
        public void setMarginStart(int start) {
            startMargin = start;
        }

        public void setMarginEnd(int end) {
            endMargin = end;
        }

        public int getMarginStart() {
            return startMargin;
        }

        public int getMarginEnd() {
            return endMargin;
        }

        public int getLayoutDirection() {
            return layoutDirection;
        }
        */
        ///////////////////////////////////////////////////////////////////////////////////////
        constructor(c: TContext, attrs: AttributeSet) : super(c, attrs) {
            ///////////////////////////////////////////////////////////////////////////////////////
            // Layout margins handling TODO: re-activate in 3.0
            ///////////////////////////////////////////////////////////////////////////////////////
            // super(WRAP_CONTENT, WRAP_CONTENT);
            /*
            if (n == 0) {
               // check if it's an include
               throw new IllegalArgumentException("Invalid LayoutParams supplied to " + this);
            }

            // let's first apply full margins if they are present.
            int margin = a.getDimensionPixelSize(R.styleable
            .ConstraintLayout_Layout_android_layout_margin, -1);
            int horizontalMargin = -1;
            int verticalMargin = -1;
            if (margin >= 0) {
                originalLeftMargin = margin;
                originalRightMargin = margin;
                topMargin = margin;
                bottomMargin = margin;
            } else {
                horizontalMargin = a.getDimensionPixelSize(R.styleable
                .ConstraintLayout_Layout_android_layout_marginHorizontal, -1);
                verticalMargin = a.getDimensionPixelSize(R.styleable
                .ConstraintLayout_Layout_android_layout_marginVertical, -1);
                if (horizontalMargin >= 0) {
                    originalLeftMargin = horizontalMargin;
                    originalRightMargin = horizontalMargin;
                }
                if (verticalMargin >= 0) {
                    topMargin = verticalMargin;
                    bottomMargin = verticalMargin;
                }
            }
            */
            //////////////////////////////////////////////////////////////////////////////////////
            attrs.forEach { kvp ->
                val look: Int = Table.sMap.get(kvp.key) ?: Table.UNUSED
                val a = c.getResources()
                val attr = kvp.value
                when (look) {
                    Table.UNUSED -> {}
                    Table.LAYOUT_CONSTRAINT_WIDTH -> {
                        ConstraintSet.parseDimensionConstraints(this, a, kvp.key, attr, HORIZONTAL)
                        mWidthSet = true
                    }
                    Table.LAYOUT_CONSTRAINT_HEIGHT -> {
                        ConstraintSet.parseDimensionConstraints(this, a, kvp.key, attr, VERTICAL)
                        mHeightSet = true
                    }
                    Table.LAYOUT_WRAP_BEHAVIOR_IN_PARENT -> {
                        wrapBehaviorInParent = c.getResources().getInt(kvp.value, wrapBehaviorInParent)
                    }
                    Table.LAYOUT_CONSTRAINT_LEFT_TO_LEFT_OF -> {
                        leftToLeft = c.getResources().getResourceId(kvp.value, leftToLeft)
                        if (leftToLeft == UNSET_ID) {
                            leftToLeft = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_LEFT_TO_RIGHT_OF -> {
                        leftToRight = c.getResources().getResourceId(kvp.value, leftToRight)
                        if (leftToRight == UNSET_ID) {
                            leftToRight = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_RIGHT_TO_LEFT_OF -> {
                        rightToLeft = c.getResources().getResourceId(kvp.value, rightToLeft)
                        if (rightToLeft == UNSET_ID) {
                            rightToLeft = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_RIGHT_TO_RIGHT_OF -> {
                        rightToRight = c.getResources().getResourceId(kvp.value, rightToRight)
                        if (rightToRight == UNSET_ID) {
                            rightToRight = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_TOP_TO_TOP_OF -> {
                        topToTop = c.getResources().getResourceId(kvp.value, topToTop)
                        if (topToTop == UNSET_ID) {
                            topToTop = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_TOP_TO_BOTTOM_OF -> {
                        topToBottom = c.getResources().getResourceId(kvp.value, topToBottom)
                        if (topToBottom == UNSET_ID) {
                            topToBottom = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_BOTTOM_TO_TOP_OF -> {
                        bottomToTop = c.getResources().getResourceId(kvp.value, bottomToTop)
                        if (bottomToTop == UNSET_ID) {
                            bottomToTop = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_BOTTOM_TO_BOTTOM_OF -> {
                        bottomToBottom = c.getResources().getResourceId(kvp.value, bottomToBottom)
                        if (bottomToBottom == UNSET_ID) {
                            bottomToBottom = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_BASELINE_TO_BASELINE_OF -> {
                        baselineToBaseline = c.getResources().getResourceId(kvp.value, baselineToBaseline)
                        if (baselineToBaseline == UNSET_ID) {
                            baselineToBaseline = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_BASELINE_TO_TOP_OF -> {
                        baselineToTop = c.getResources().getResourceId(kvp.value, baselineToTop)
                        if (baselineToTop == UNSET_ID) {
                            baselineToTop = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_BASELINE_TO_BOTTOM_OF -> {
                        baselineToBottom = c.getResources().getResourceId(kvp.value, baselineToBottom)
                        if (baselineToBottom == UNSET_ID) {
                            baselineToBottom = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_CIRCLE -> {
                        circleConstraint = c.getResources().getResourceId(kvp.value, circleConstraint)
                        if (circleConstraint == UNSET_ID) {
                            circleConstraint = c.getResources().getString(kvp.key, kvp.value)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_CIRCLE_RADIUS -> {
                        circleRadius = c.getResources().getDimensionPixelSize(kvp.value, circleRadius)
                    }
                    Table.LAYOUT_CONSTRAINT_CIRCLE_ANGLE -> {
                        circleAngle = c.getResources().getFloat(kvp.value, circleAngle) % 360
                        if (circleAngle < 0) {
                            circleAngle = (360 - circleAngle) % 360
                        }
                    }
                    Table.LAYOUT_EDITOR_ABSOLUTEX -> {
                        editorAbsoluteX = c.getResources().getDimensionPixelOffset(kvp.value, editorAbsoluteX)
                    }
                    Table.LAYOUT_EDITOR_ABSOLUTEY -> {
                        editorAbsoluteY = c.getResources().getDimensionPixelOffset(kvp.value, editorAbsoluteY)
                    }
                    Table.LAYOUT_CONSTRAINT_GUIDE_BEGIN -> {
                        guideBegin = c.getResources().getDimensionPixelOffset(kvp.value, guideBegin)
                    }
                    Table.LAYOUT_CONSTRAINT_GUIDE_END -> {
                        guideEnd = c.getResources().getDimensionPixelOffset(kvp.value, guideEnd)
                    }
                    Table.LAYOUT_CONSTRAINT_GUIDE_PERCENT -> {
                        guidePercent = c.getResources().getFloat(kvp.value, guidePercent)
                    }
                    Table.GUIDELINE_USE_RTL -> {
                        guidelineUseRtl = c.getResources().getBoolean(kvp.value, guidelineUseRtl)
                    }
                    Table.ANDROID_ORIENTATION -> {
                        orientation = c.getResources().getInt(kvp.value, orientation)
                    }
                    Table.LAYOUT_CONSTRAINT_START_TO_END_OF -> {
                        startToEnd = a.getResourceId(attr, startToEnd)
                        if (startToEnd == UNSET_ID) {
                            startToEnd = a.getString(kvp.key, attr)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_START_TO_START_OF -> {
                        startToStart = a.getResourceId(attr, startToStart)
                        if (startToStart == UNSET_ID) {
                            startToStart = a.getString(kvp.key, attr)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_END_TO_START_OF -> {
                        endToStart = a.getResourceId(attr, endToStart)
                        if (endToStart == UNSET_ID) {
                            endToStart = a.getString(kvp.key, attr)
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_END_TO_END_OF -> {
                        endToEnd = a.getResourceId(attr, endToEnd)
                        if (endToEnd == UNSET_ID) {
                            endToEnd = a.getString(kvp.key, attr)
                        }
                    }
                    Table.LAYOUT_GONE_MARGIN_LEFT -> {
                        goneLeftMargin = a.getDimensionPixelSize(attr, goneLeftMargin)
                    }
                    Table.LAYOUT_GONE_MARGIN_TOP -> {
                        goneTopMargin = a.getDimensionPixelSize(attr, goneTopMargin)
                    }
                    Table.LAYOUT_GONE_MARGIN_RIGHT -> {
                        goneRightMargin = a.getDimensionPixelSize(attr, goneRightMargin)
                    }
                    Table.LAYOUT_GONE_MARGIN_BOTTOM -> {
                        goneBottomMargin = a.getDimensionPixelSize(attr, goneBottomMargin)
                    }
                    Table.LAYOUT_GONE_MARGIN_START -> {
                        goneStartMargin = a.getDimensionPixelSize(attr, goneStartMargin)
                    }
                    Table.LAYOUT_GONE_MARGIN_END -> {
                        goneEndMargin = a.getDimensionPixelSize(attr, goneEndMargin)
                    }
                    Table.LAYOUT_GONE_MARGIN_BASELINE -> {
                        goneBaselineMargin = a.getDimensionPixelSize(attr, goneBaselineMargin)
                    }
                    Table.LAYOUT_MARGIN_BASELINE -> {
                        baselineMargin = a.getDimensionPixelSize(attr, baselineMargin)
                    }
                    Table.LAYOUT_CONSTRAINT_HORIZONTAL_BIAS -> {
                        horizontalBias = a.getFloat(attr, horizontalBias)
                    }
                    Table.LAYOUT_CONSTRAINT_VERTICAL_BIAS -> {
                        verticalBias = a.getFloat(attr, verticalBias)
                    }
                    Table.LAYOUT_CONSTRAINT_DIMENSION_RATIO -> {
                        ConstraintSet.parseDimensionRatioString(this, a.getString(kvp.key, attr))
                    }
                    Table.LAYOUT_CONSTRAINT_HORIZONTAL_WEIGHT -> {
                        horizontalWeight = a.getFloat(attr, horizontalWeight)
                    }
                    Table.LAYOUT_CONSTRAINT_VERTICAL_WEIGHT -> {
                        verticalWeight = a.getFloat(attr, verticalWeight)
                    }
                    Table.LAYOUT_CONSTRAINT_HORIZONTAL_CHAINSTYLE -> {
                        horizontalChainStyle = a.getInt(attr, CHAIN_SPREAD)
                    }
                    Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE -> {
                        verticalChainStyle = a.getInt(attr, CHAIN_SPREAD)
                    }
                    Table.LAYOUT_CONSTRAINED_WIDTH -> {
                        constrainedWidth = a.getBoolean(attr, constrainedWidth)
                    }
                    Table.LAYOUT_CONSTRAINED_HEIGHT -> {
                        constrainedHeight = a.getBoolean(attr, constrainedHeight)
                    }
                    Table.LAYOUT_CONSTRAINT_WIDTH_DEFAULT -> {
                        matchConstraintDefaultWidth = a.getInt(attr, MATCH_CONSTRAINT_SPREAD)
                        if (matchConstraintDefaultWidth == MATCH_CONSTRAINT_WRAP) {
                            Log.e(
                                TAG, """layout_constraintWidth_default="wrap" is deprecated.
Use layout_width="WRAP_CONTENT" and layout_constrainedWidth="true" instead."""
                            )
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_HEIGHT_DEFAULT -> {
                        matchConstraintDefaultHeight = a.getInt(attr, MATCH_CONSTRAINT_SPREAD)
                        if (matchConstraintDefaultHeight == MATCH_CONSTRAINT_WRAP) {
                            Log.e(
                                TAG, """layout_constraintHeight_default="wrap" is deprecated.
Use layout_height="WRAP_CONTENT" and layout_constrainedHeight="true" instead."""
                            )
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_WIDTH_MIN -> {
                        try {
                            matchConstraintMinWidth = a.getDimensionPixelSize(
                                attr,
                                matchConstraintMinWidth
                            )
                        } catch (e: Exception) {
                            val value: Int = a.getInt(attr, matchConstraintMinWidth)
                            if (value == WRAP_CONTENT) {
                                matchConstraintMinWidth = WRAP_CONTENT
                            }
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_WIDTH_MAX -> {
                        try {
                            matchConstraintMaxWidth = a.getDimensionPixelSize(
                                attr,
                                matchConstraintMaxWidth
                            )
                        } catch (e: Exception) {
                            val value: Int = a.getInt(attr, matchConstraintMaxWidth)
                            if (value == WRAP_CONTENT) {
                                matchConstraintMaxWidth = WRAP_CONTENT
                            }
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_WIDTH_PERCENT -> {
                        matchConstraintPercentWidth = max(
                            0f, a.getFloat(
                                attr,
                                matchConstraintPercentWidth
                            )
                        ).toFloat()
                        matchConstraintDefaultWidth = MATCH_CONSTRAINT_PERCENT
                    }
                    Table.LAYOUT_CONSTRAINT_HEIGHT_MIN -> {
                        try {
                            matchConstraintMinHeight = a.getDimensionPixelSize(
                                attr,
                                matchConstraintMinHeight
                            )
                        } catch (e: Exception) {
                            val value: Int = a.getInt(attr, matchConstraintMinHeight)
                            if (value == WRAP_CONTENT) {
                                matchConstraintMinHeight = WRAP_CONTENT
                            }
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_HEIGHT_MAX -> {
                        try {
                            matchConstraintMaxHeight = a.getDimensionPixelSize(
                                attr,
                                matchConstraintMaxHeight
                            )
                        } catch (e: Exception) {
                            val value: Int = a.getInt(attr, matchConstraintMaxHeight)
                            if (value == WRAP_CONTENT) {
                                matchConstraintMaxHeight = WRAP_CONTENT
                            }
                        }
                    }
                    Table.LAYOUT_CONSTRAINT_HEIGHT_PERCENT -> {
                        matchConstraintPercentHeight = max(
                            0f, a.getFloat(
                                attr,
                                matchConstraintPercentHeight
                            )
                        ).toFloat()
                        matchConstraintDefaultHeight = MATCH_CONSTRAINT_PERCENT
                    }
                    Table.LAYOUT_CONSTRAINT_TAG -> constraintTag = a.getString(kvp.key, attr)
                    Table.LAYOUT_CONSTRAINT_LEFT_CREATOR -> {}
                    Table.LAYOUT_CONSTRAINT_TOP_CREATOR -> {}
                    Table.LAYOUT_CONSTRAINT_RIGHT_CREATOR -> {}
                    Table.LAYOUT_CONSTRAINT_BOTTOM_CREATOR -> {}
                    Table.LAYOUT_CONSTRAINT_BASELINE_CREATOR -> {}
                }
            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // Layout margins handling TODO: re-activate in 3.0
            ///////////////////////////////////////////////////////////////////////////////////////
            /*
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                leftMargin = originalLeftMargin;
                rightMargin = originalRightMargin;
            }
            */
            ///////////////////////////////////////////////////////////////////////////////////////
            validate()
        }

        /**
         * validate the layout
         */
        fun validate() {
            mIsGuideline = false
            mHorizontalDimensionFixed = true
            mVerticalDimensionFixed = true
            ///////////////////////////////////////////////////////////////////////////////////////
            // Layout margins handling TODO: re-activate in 3.0
            ///////////////////////////////////////////////////////////////////////////////////////
            /*
            if (dimensionRatio != null && !widthSet && !heightSet) {
                width = MATCH_CONSTRAINT;
                height = MATCH_CONSTRAINT;
            }
            */
            ///////////////////////////////////////////////////////////////////////////////////////
            if (width == WRAP_CONTENT && constrainedWidth) {
                mHorizontalDimensionFixed = false
                if (matchConstraintDefaultWidth == MATCH_CONSTRAINT_SPREAD) {
                    matchConstraintDefaultWidth = MATCH_CONSTRAINT_WRAP
                }
            }
            if (height == WRAP_CONTENT && constrainedHeight) {
                mVerticalDimensionFixed = false
                if (matchConstraintDefaultHeight == MATCH_CONSTRAINT_SPREAD) {
                    matchConstraintDefaultHeight = MATCH_CONSTRAINT_WRAP
                }
            }
            if (width == MATCH_CONSTRAINT || width == MATCH_PARENT) {
                mHorizontalDimensionFixed = false
                // We have to reset LayoutParams width/height to WRAP_CONTENT here,
                // as some widgets like TextView
                // will use the layout params directly as a hint to know
                // if they need to request a layout
                // when their content change (e.g. during setTextView)
                if (width == MATCH_CONSTRAINT
                    && matchConstraintDefaultWidth == MATCH_CONSTRAINT_WRAP
                ) {
                    width = WRAP_CONTENT
                    constrainedWidth = true
                }
            }
            if (height == MATCH_CONSTRAINT || height == MATCH_PARENT) {
                mVerticalDimensionFixed = false
                // We have to reset LayoutParams width/height to WRAP_CONTENT here,
                // as some widgets like TextView
                // will use the layout params directly as a hint to know
                // if they need to request a layout
                // when their content change (e.g. during setTextView)
                if (height == MATCH_CONSTRAINT
                    && matchConstraintDefaultHeight == MATCH_CONSTRAINT_WRAP
                ) {
                    height = WRAP_CONTENT
                    constrainedHeight = true
                }
            }
            if (guidePercent != UNSET.toFloat() || guideBegin != UNSET || guideEnd != UNSET) {
                mIsGuideline = true
                mHorizontalDimensionFixed = true
                mVerticalDimensionFixed = true
                if (mWidget !is Guideline) {
                    mWidget = Guideline()
                }
                (mWidget as Guideline?)?.orientation = (orientation)
            }
        }

        constructor(width: Int, height: Int) : super(width, height) {}

        /**
         * {@inheritDoc}
         */
        override fun resolveLayoutDirection(layoutDirection: Int) {
            ///////////////////////////////////////////////////////////////////////////////////////
            // Layout margins handling TODO: re-activate in 3.0
            ///////////////////////////////////////////////////////////////////////////////////////
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                this.layoutDirection = layoutDirection;
                isRtl = (TView.LAYOUT_DIRECTION_RTL == layoutDirection);
            }

            // First apply margins.
            leftMargin = originalLeftMargin;
            rightMargin = originalRightMargin;

            if (isRtl) {
                leftMargin = originalRightMargin;
                rightMargin = originalLeftMargin;
                if (startMargin != UNSET) {
                    rightMargin = startMargin;
                }
                if (endMargin != UNSET) {
                    leftMargin = endMargin;
                }
            } else {
                if (startMargin != UNSET) {
                    leftMargin = startMargin;
                }
                if (endMargin != UNSET) {
                    rightMargin = endMargin;
                }
            }
            */
            ///////////////////////////////////////////////////////////////////////////////////////
            val originalLeftMargin: Int = leftMargin
            val originalRightMargin: Int = rightMargin
            var isRtl = false
            super.resolveLayoutDirection(layoutDirection)
            isRtl = TView.LAYOUT_DIRECTION_RTL == getLayoutDirection()
            ///////////////////////////////////////////////////////////////////////////////////////
            mResolvedRightToLeft = UNSET_ID
            mResolvedRightToRight = UNSET_ID
            mResolvedLeftToLeft = UNSET_ID
            mResolvedLeftToRight = UNSET_ID
            mResolveGoneLeftMargin = UNSET
            mResolveGoneRightMargin = UNSET
            mResolveGoneLeftMargin = goneLeftMargin
            mResolveGoneRightMargin = goneRightMargin
            mResolvedHorizontalBias = horizontalBias
            mResolvedGuideBegin = guideBegin
            mResolvedGuideEnd = guideEnd
            mResolvedGuidePercent = guidePercent

            // Post JB MR1, if start/end are defined, they take precedence over left/right
            if (isRtl) {
                var startEndDefined = false
                if (startToEnd != UNSET_ID) {
                    mResolvedRightToLeft = startToEnd
                    startEndDefined = true
                } else if (startToStart != UNSET_ID) {
                    mResolvedRightToRight = startToStart
                    startEndDefined = true
                }
                if (endToStart != UNSET_ID) {
                    mResolvedLeftToRight = endToStart
                    startEndDefined = true
                }
                if (endToEnd != UNSET_ID) {
                    mResolvedLeftToLeft = endToEnd
                    startEndDefined = true
                }
                if (goneStartMargin != GONE_UNSET) {
                    mResolveGoneRightMargin = goneStartMargin
                }
                if (goneEndMargin != GONE_UNSET) {
                    mResolveGoneLeftMargin = goneEndMargin
                }
                if (startEndDefined) {
                    mResolvedHorizontalBias = 1 - horizontalBias
                }

                // Only apply to vertical guidelines
                if (mIsGuideline && orientation == Guideline.VERTICAL && guidelineUseRtl) {
                    if (guidePercent != UNSET.toFloat()) {
                        mResolvedGuidePercent = 1 - guidePercent
                        mResolvedGuideBegin = UNSET
                        mResolvedGuideEnd = UNSET
                    } else if (guideBegin != UNSET) {
                        mResolvedGuideEnd = guideBegin
                        mResolvedGuideBegin = UNSET
                        mResolvedGuidePercent = UNSET.toFloat()
                    } else if (guideEnd != UNSET) {
                        mResolvedGuideBegin = guideEnd
                        mResolvedGuideEnd = UNSET
                        mResolvedGuidePercent = UNSET.toFloat()
                    }
                }
            } else {
                if (startToEnd != UNSET_ID) {
                    mResolvedLeftToRight = startToEnd
                }
                if (startToStart != UNSET_ID) {
                    mResolvedLeftToLeft = startToStart
                }
                if (endToStart != UNSET_ID) {
                    mResolvedRightToLeft = endToStart
                }
                if (endToEnd != UNSET_ID) {
                    mResolvedRightToRight = endToEnd
                }
                if (goneStartMargin != GONE_UNSET) {
                    mResolveGoneLeftMargin = goneStartMargin
                }
                if (goneEndMargin != GONE_UNSET) {
                    mResolveGoneRightMargin = goneEndMargin
                }
            }
            // if no constraint is defined via RTL attributes, use left/right if present
            if (endToStart == UNSET_ID && endToEnd == UNSET_ID && startToStart == UNSET_ID && startToEnd == UNSET_ID) {
                if (rightToLeft != UNSET_ID) {
                    mResolvedRightToLeft = rightToLeft
                    if (rightMargin <= 0 && originalRightMargin > 0) {
                        rightMargin = originalRightMargin
                    }
                } else if (rightToRight != UNSET_ID) {
                    mResolvedRightToRight = rightToRight
                    if (rightMargin <= 0 && originalRightMargin > 0) {
                        rightMargin = originalRightMargin
                    }
                }
                if (leftToLeft != UNSET_ID) {
                    mResolvedLeftToLeft = leftToLeft
                    if (leftMargin <= 0 && originalLeftMargin > 0) {
                        leftMargin = originalLeftMargin
                    }
                } else if (leftToRight != UNSET_ID) {
                    mResolvedLeftToRight = leftToRight
                    if (leftMargin <= 0 && originalLeftMargin > 0) {
                        leftMargin = originalLeftMargin
                    }
                }
            }
        }

        companion object {
            /**
             * Dimension will be controlled by constraints.
             */
            const val MATCH_CONSTRAINT = 0

            /**
             * References the id of the parent.
             */
            const val PARENT_ID = "0"

            /**
             * Defines an id that is not set.
             */
            const val UNSET = -1
            const val UNSET_ID = ""

            /**
             * Defines an id that is not set.
             */
            val GONE_UNSET: Int = Int.MIN_VALUE

            /**
             * The horizontal orientation.
             */
            val HORIZONTAL: Int = ConstraintWidget.HORIZONTAL

            /**
             * The vertical orientation.
             */
            val VERTICAL: Int = ConstraintWidget.VERTICAL

            /**
             * The left side of a view.
             */
            const val LEFT = 1

            /**
             * The right side of a view.
             */
            const val RIGHT = 2

            /**
             * The top of a view.
             */
            const val TOP = 3

            /**
             * The bottom side of a view.
             */
            const val BOTTOM = 4

            /**
             * The baseline of the text in a view.
             */
            const val BASELINE = 5

            /**
             * The left side of a view in left to right languages.
             * In right to left languages it corresponds to the right side of the view
             */
            const val START = 6

            /**
             * The right side of a view in left to right languages.
             * In right to left languages it corresponds to the left side of the view
             */
            const val END = 7

            /**
             * Circle reference from a view.
             */
            const val CIRCLE = 8

            /**
             * Set matchConstraintDefault* default to the wrap content size.
             * Use to set the matchConstraintDefaultWidth and matchConstraintDefaultHeight
             */
            val MATCH_CONSTRAINT_WRAP: Int = ConstraintWidget.MATCH_CONSTRAINT_WRAP

            /**
             * Set matchConstraintDefault* spread as much as possible within its constraints.
             * Use to set the matchConstraintDefaultWidth and matchConstraintDefaultHeight
             */
            val MATCH_CONSTRAINT_SPREAD: Int = ConstraintWidget.MATCH_CONSTRAINT_SPREAD

            /**
             * Set matchConstraintDefault* percent to be based
             * on a percent of another dimension (by default, the parent)
             * Use to set the matchConstraintDefaultWidth and matchConstraintDefaultHeight
             */
            val MATCH_CONSTRAINT_PERCENT: Int = ConstraintWidget.MATCH_CONSTRAINT_PERCENT

            /**
             * Chain spread style
             */
            val CHAIN_SPREAD: Int = ConstraintWidget.CHAIN_SPREAD

            /**
             * Chain spread inside style
             */
            val CHAIN_SPREAD_INSIDE: Int = ConstraintWidget.CHAIN_SPREAD_INSIDE

            /**
             * Chain packed style
             */
            val CHAIN_PACKED: Int = ConstraintWidget.CHAIN_PACKED
            val WRAP_BEHAVIOR_INCLUDED: Int = ConstraintWidget.WRAP_BEHAVIOR_INCLUDED
            val WRAP_BEHAVIOR_HORIZONTAL_ONLY: Int = ConstraintWidget.WRAP_BEHAVIOR_HORIZONTAL_ONLY
            val WRAP_BEHAVIOR_VERTICAL_ONLY: Int = ConstraintWidget.WRAP_BEHAVIOR_VERTICAL_ONLY
            val WRAP_BEHAVIOR_SKIPPED: Int = ConstraintWidget.WRAP_BEHAVIOR_SKIPPED
        }
    }

    /**
     * {@inheritDoc}
     */
    open fun requestLayout(sup: TView?) {
        markHierarchyDirty()
        sup?.requestLayout()
    }

    open fun forceLayout(sup: TView?) {
        markHierarchyDirty()
        sup?.forceLayout()
    }

    private fun markHierarchyDirty() {
        mDirtyHierarchy = true
        // reset measured cache
        mLastMeasureWidth = -1
        mLastMeasureHeight = -1
        mLastMeasureWidthSize = -1
        mLastMeasureHeightSize = -1
        mLastMeasureWidthMode = MeasureSpec.UNSPECIFIED
        mLastMeasureHeightMode = MeasureSpec.UNSPECIFIED
    }

    /**
     *
     *
     * @return
     */
    fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    /**
     * Returns a JSON5 string useful for debugging the constraints actually applied.
     * In situations where a complex set of code dynamically constructs constraints
     * it is useful to be able to query the layout for what are the constraints.
     * @return json5 string representing the constraint in effect now.
     */
    val sceneString: String
        get() {
            val ret = StringBuilder()
            if (mLayoutWidget.stringId == null) {
                val id: String = self.getId()
                if (id != "") {
                    mLayoutWidget.stringId = id
                } else {
                    mLayoutWidget.stringId = "parent"
                }
            }
            if (mLayoutWidget.debugName == null) {
                mLayoutWidget.debugName = (mLayoutWidget.stringId)
                Log.v(TAG, " setDebugName " + mLayoutWidget.debugName)
            }
            val children = mLayoutWidget.children
            for (child in children) {
                val v: TView? = child.companionWidget as TView?
                if (v != null) {
                    if (child.stringId == null) {
                        val id: String = v.getId()
                        if (id != null) {
                            child.stringId = id
                        }
                    }
                    if (child.debugName == null) {
                        child.debugName = child.stringId
                        Log.v(TAG, " setDebugName " + child.debugName)
                    }
                }
            }
            mLayoutWidget.getSceneString(ret)
            return ret.toString()
        }

    /**
     * This is the interface to a valued modifier.
     * implement this and add it using addValueModifier
     */
    interface ValueModifier {
        /**
         * if needed in the implementation modify params and return true
         * @param width of the ConstraintLayout in pixels
         * @param height of the ConstraintLayout in pixels
         * @param id The id of the view which
         * @param view The TView
         * @param params The layout params of the view
         * @return true if you modified the layout params
         */
        fun update(width: Int, height: Int, id: String, view: TView, params: LayoutParams): Boolean
    }

    private var mModifiers: ArrayList<ValueModifier>? = null

    /**
     * a ValueModify to the ConstraintLayout.
     * This can be useful to add custom behavour to the ConstraintLayout or
     * address limitation of the capabilities of Constraint Layout
     * @param modifier
     */
    fun addValueModifier(modifier: ValueModifier) {
        if (mModifiers == null) {
            mModifiers = ArrayList()
        }
        mModifiers!!.add(modifier)
    }

    /**
     * Remove a value modifier this can be useful if the modifier is used during in one state of the
     * system.
     * @param modifier The modifier to remove
     */
    fun removeValueModifier(modifier: ValueModifier?) {
        if (modifier == null) {
            return
        }
        mModifiers!!.remove(modifier)
    }

    /**
     * This can be overridden to change the way Modifiers are used.
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     * @return
     */
    protected fun dynamicUpdateConstraints(widthMeasureSpec: Int, heightMeasureSpec: Int): Boolean {
        if (mModifiers == null) {
            return false
        }
        var dirty = false
        val width: Int = MeasureSpec.getSize(widthMeasureSpec)
        val height: Int = MeasureSpec.getSize(heightMeasureSpec)
        for (m in mModifiers!!) {
            for (widget in mLayoutWidget.children) {
                val view: TView = widget.companionWidget as TView
                val id: String = view.getId()
                val layoutParams = view.getLayoutParams() as LayoutParams
                dirty = dirty or m.update(width, height, id, view, layoutParams)
            }
        }
        return dirty
    }

    companion object {
        /**
         *
         */
        const val VERSION = "ConstraintLayout-2.2.0-alpha04"
        private const val TAG = "ConstraintLayout"
        private const val USE_CONSTRAINTS_HELPER = true
        private val I_DEBUG: Boolean = LinearSystem.FULL_DEBUG
        private const val DEBUG_DRAW_CONSTRAINTS = false
        private const val OPTIMIZE_HEIGHT_CHANGE = false

        /**
         *
         */
        const val DESIGN_INFO_ID = 0
        private var sSharedValues: SharedValues? = null

        /**
         * Returns the SharedValues instance, creating it if it doesn't exist.
         *
         * @return the SharedValues instance
         */
        val sharedValues: SharedValues
            get() {
                if (sSharedValues == null) {
                    sSharedValues = SharedValues()
                }
                return sSharedValues!!
            }
    }

    open var velocity = 0.0f
}