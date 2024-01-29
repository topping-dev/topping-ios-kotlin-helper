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
package dev.topping.ios.constraint.constraintlayout.motion.widget

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.constraintlayout.motion.utils.StopLogic
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewState
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionScene.Transition.Companion.TRANSITION_FLAG_FIRST_DRAW
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionScene.Transition.Companion.TRANSITION_FLAG_INTERCEPT_TOUCH
import dev.topping.ios.constraint.constraintlayout.widget.Barrier
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintHelper
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout.LayoutParams.Companion.PARENT_ID
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.UNSET
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.UNSET_ID
import dev.topping.ios.constraint.constraintlayout.widget.Constraints
import dev.topping.ios.constraint.core.motion.utils.KeyCache
import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.core.motion.utils.RectF
import dev.topping.ios.constraint.core.state.Interpolator
import dev.topping.ios.constraint.core.widgets.*
import dev.topping.ios.constraint.shared.graphics.inverse
import dev.topping.ios.constraint.shared.graphics.isIdentity
import org.jetbrains.skia.Matrix33
import kotlin.math.*

/**
 * A subclass of ConstraintLayout that supports animating between
 * various states **Added in 2.0**
 *
 *
 * A `MotionLayout` is a subclass of [ConstraintLayout]
 * which supports transitions between between various states ([ConstraintSet])
 * defined in [MotionScene]s.
 *
 *
 * **Note:** `MotionLayout` is available as a support library that you can use
 * on Android systems starting with API level 14 (ICS).
 *
 *
 *
 * `MotionLayout` links to and requires a [MotionScene] file.
 * The file contains one top level tag "MotionScene"
 * <h2>LayoutDescription</h2>
 * <table summary="LayoutDescription">
 * <tr>
 * <th>Tags</th><th>Description</th>
</tr> *
 * <tr>
 * <td>`<StateSet> `</td>
 * <td>Describes states supported by the system (optional)</td>
</tr> *
 * <tr>
 * <td>`<ConstraintSet> `</td>
 * <td>Describes a constraint set</td>
</tr> *
 * <tr>
 * <td>`<Transition> `</td>
 * <td>Describes a transition between two states or ConstraintSets</td>
</tr> *
 * <tr>
 * <td>`<ViewTransition> `</td>
 * <td>Describes a transition of a TView within a states or ConstraintSets</td>
</tr> *
</table> *
 *
 * <h2>Transition</h2>
 * <table summary="Transition attributes & tags">
 * <tr>
 * <th>Attributes</th><th>Description</th>
</tr> *
 * <tr>
 * <td>android:id</td>
 * <td>The id of the Transition</td>
</tr> *
 * <tr>
 * <td>constraintSetStart</td>
 * <td>ConstraintSet to be used as the start constraints or a
 * layout file to get the constraint from</td>
</tr> *
 * <tr>
 * <td>constraintSetEnd</td>
 * <td>ConstraintSet to be used as the end constraints or a
 * layout file to get the constraint from</td>
</tr> *
 * <tr>
 * <td>motionInterpolator</td>
 * <td>The ability to set an overall interpolation (easeInOut, linear, etc.)</td>
</tr> *
 * <tr>
 * <td>duration</td>
 * <td>Length of time to take to perform the transition</td>
</tr> *
 * <tr>
 * <td>staggered</td>
 * <td>Overrides the Manhattan distance from the top most view in the list of views.
 *
 *  * For any view of stagger value `S(Vi)`
 *  * With the transition stagger value of `TS` (from 0.0 - 1.0)
 *  * The duration of the animation is `duration`
 *  * The views animation duration `DS = duration * (1 -TS)`
 *  * Call the stagger fraction `SFi = (S(Vi) - S(V0)) / (S(Vn) - S(V0))`
 *  * The view starts animating at: `(duration-DS) * SFi`
 *
</td> *
</tr> *
 * <tr>
 * <td>pathMotionArc</td>
 * <td>The path will move in arc (quarter ellipses)
 * key words {startVertical | startHorizontal | flip | none }</td>
</tr> *
 * <tr>
 * <td>autoTransition</td>
 * <td>automatically transition from one state to another.
 * key words {none, jumpToStart, jumpToEnd, animateToStart, animateToEnd}</td>
</tr> *
 *
 * <tr>
 * <td>transitionFlags</td>
 * <td>flags that adjust the behaviour of Transitions. supports {none, beginOnFirstDraw}
 * begin on first draw forces the transition's clock to start when it is first
 * displayed not when the begin is called</td>
</tr> *
 *
 * <tr>
 * <td>layoutDuringTransition</td>
 * <td>Configures MotionLayout on how to react to requestLayout calls during transitions.
 * Allowed values are {ignoreRequest, honorRequest}</td>
</tr> *
 * <tr>
 * <td>`<OnSwipe> `</td>
 * <td>Adds support for touch handling (optional)</td>
</tr> *
 * <tr>
 * <td>`<OnClick> `</td>
 * <td>Adds support for triggering transition (optional)</td>
</tr> *
 * <tr>
 * <td>`<KeyFrameSet> `</td>
 * <td>Describes a set of Key object which modify the animation between constraint sets.</td>
</tr> *
</table> *
 *
 *
 *  * A transition is typically defined by specifying its start and end ConstraintSets.
 * You also have the possibility to not specify them, in which case such transition
 * will become a Default transition.
 * That Default transition will be applied between any state change that isn't
 * explicitly covered by a transition.
 *  * The starting state of the MotionLayout is defined  to be the constraintSetStart of the first
 * transition.
 *  * If no transition is specified (or only a default Transition)
 * the MotionLayout tag must contain
 * a app:currentState to define the starting state of the MotionLayout
 *
 *
 * <h2>ViewTransition</h2>
 * <table summary="Transition attributes & tags">
 * <tr>
 * <th>Attributes</th><th>Description</th>
</tr> *
 * <tr>
 * <td>android:id</td>
 * <td>The id of the ViewTransition</td>
</tr> *
 * <tr>
 * <td>viewTransitionMode</td>
 * <td>currentState, allStates, noState transition affect the state of the view
 * in the current constraintSet or all ConstraintSets or non
 * if noState the ViewTransitions are run asynchronous</td>
</tr> *
 * <tr>
 * <td>onStateTransition</td>
 * <td>actionDown or actionUp run transition if on touch down or
 * up if view matches motionTarget</td>
</tr> *
 * <tr>
 * <td>motionInterpolator</td>
 * <td>The ability to set an overall interpolation
 * key words {easeInOut, linear, etc.}</td>
</tr> *
 * <tr>
 * <td>duration</td>
 * <td>Length of time to take to perform the `ViewTransition`</td>
</tr> *
 * <tr>
 * <td>pathMotionArc</td>
 * <td>The path will move in arc (quarter ellipses)
 * key words {startVertical | startHorizontal | flip | none }</td>
</tr> *
 * <tr>
 * <td>motionTarget</td>
 * <td>Apply ViewTransition matching this string or id.</td>
</tr> *
 *
 * <tr>
 * <td>setsTag</td>
 * <td>set this tag at end of transition</td>
</tr> *
 *
 * <tr>
 * <td>clearsTag</td>
 * <td>clears this tag at end of transition</td>
</tr> *
 * <tr>
 * <td>ifTagSet</td>
 * <td>run transition if this tag is set on view</td>
</tr> *
 * <tr>
 * <td>ifTagNotSet</td>
 * <td>run transition if this tag is not set on view/td>
</td></tr> *
 * <tr>
 * <td>`<OnSwipe> `</td>
 * <td>Adds support for touch handling (optional)</td>
</tr> *
 * <tr>
 * <td>`<OnClick> `</td>
 * <td>Adds support for triggering transition (optional)</td>
</tr> *
 * <tr>
 * <td>`<KeyFrameSet> `</td>
 * <td>Describes a set of Key object which modify the animation between constraint sets.</td>
</tr> *
</table> *
 *
 *
 *  * A Transition is typically defined by specifying its start and end ConstraintSets.
 * You also have the possibility to not specify them, in which case such transition
 * will become a Default transition.
 * That Default transition will be applied between any state change that isn't
 * explicitly covered by a transition.
 *  * The starting state of the MotionLayout is defined to be the constraintSetStart of the first
 * transition.
 *  * If no transition is specified (or only a default Transition) the
 * MotionLayout tag must contain
 * a app:currentState to define the starting state of the MotionLayout
 *
 *
 *
 *
 *
 * <h2>OnSwipe (optional)</h2>
 * <table summary="OnSwipe attributes">
 * <tr>
 * <th>Attributes</th><th>Description</th>
</tr> *
 * <tr>
 * <td>touchAnchorId</td>
 * <td>Have the drag act as if it is moving the "touchAnchorSide" of this object</td>
</tr> *
 * <tr>
 * <td>touchRegionId</td>
 * <td>Limits the region that the touch can be start in to the bounds of this view
 * (even if the view is invisible)</td>
</tr> *
 * <tr>
 * <td>touchAnchorSide</td>
 * <td>The side of the object to move with {top|left|right|bottom}</td>
</tr> *
 * <tr>
 * <td>maxVelocity</td>
 * <td>limit the maximum velocity (in progress/sec) of the animation will on touch up.
 * Default 4</td>
</tr> *
 * <tr>
 * <td>dragDirection</td>
 * <td>which side to swipe from {dragUp|dragDown|dragLeft|dragRight}</td>
</tr> *
 * <tr>
 * <td>maxAcceleration</td>
 * <td>how quickly the animation will accelerate
 * (progress/sec/sec) and decelerate on touch up. Default 1.2</td>
</tr> *
 * <tr>
 * <td>dragScale</td>
 * <td>scale factor to adjust the swipe by. (e.g. 0.5 would require you to move 2x as much)</td>
</tr> *
 * <td>dragThreshold</td>
 * <td>How much to drag before swipe gesture runs.
 * Important for mult-direction swipe. Default is 10. 1 is very sensitive.</td>
 *
 * <tr>
 * <td>moveWhenScrollAtTop</td>
 * <td>If the swipe is scrolling and TView (such as RecyclerView or NestedScrollView)
 * do scroll and transition happen at the same time</td>
</tr> *
 * <tr>
 * <td>onTouchUp</td>
 * <td>Support for various swipe modes
 * autoComplete,autoCompleteToStart,autoCompleteToEnd,stop,decelerate,decelerateAndComplete</td>
</tr> *
</table> *
 *
 *
 *
 * <h2>OnClick (optional)</h2>
 * <table summary="OnClick attributes">
 * <tr>
 * <th>Attributes</th><th>Description</th>
</tr> *
 * <tr>
 * <td>motionTarget</td>
 * <td>What view triggers Transition.</td>
</tr> *
 * <tr>
 * <td>clickAction</td>
 * <td>Direction for buttons to move the animation.
 * Or (|) combination of:  toggle, transitionToEnd, transitionToStart, jumpToEnd, jumpToStart</td>
</tr> *
</table> *
 *
 *
 *
 * <h2>StateSet</h2>
 * <table summary="StateSet tags & attributes">
 * <tr>
 * <td>defaultState</td>
 * <td>The constraint set or layout to use</td>
</tr> *
 * <tr>
 * <td>`<State> `</td>
 * <td>The side of the object to move</td>
</tr> *
</table> *
 *
 *
 *
 * <h2>State</h2>
 * <table summary="State attributes">
 * <tr>
 * <td>android:id</td>
 * <td>Id of the State</td>
</tr> *
 * <tr>
 * <td>constraints</td>
 * <td>Id of the ConstraintSet or the Layout file</td>
</tr> *
 * <tr>
 * <td>`<Variant> `</td>
 * <td>a different constraintSet/layout to choose if the with or height matches</td>
</tr> *
</table> *
 *
 * <h2>Variant</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>region_widthLessThan</td>
 * <td>Match if width less than</td>
</tr> *
 * <tr>
 * <td>region_widthMoreThan</td>
 * <td>Match if width more than</td>
</tr> *
 * <tr>
 * <td>region_heightLessThan</td>
 * <td>Match if height less than</td>
</tr> *
 * <tr>
 * <td>region_heightMoreThan</td>
 * <td>Match if height more than</td>
</tr> *
 * <tr>
 * <td>constraints</td>
 * <td>Id of the ConstraintSet or layout</td>
</tr> *
</table> *
 *
 *
 *
 * <h2>ConstraintSet</h2>
 * <table summary="StateSet tags & attributes">
 * <tr>
 * <td>android:id</td>
 * <td>The id of the ConstraintSet</td>
</tr> *
 * <tr>
 * <td>deriveConstraintsFrom</td>
 * <td>The id of another constraintSet which defines the constraints not define in this set.
 * If not specified the layout defines the undefined constraints.</td>
</tr> *
 * <tr>
 * <td>`<Constraint> `</td>
 * <td>A ConstraintLayout Constraints + other attributes associated with a view</td>
</tr> *
</table> *
 *
 *
 *
 * <h2>Constraint</h2>
 *
 *  Constraint supports two forms:
 *
 *1: All of ConstraintLayout + the ones listed below +
 * `<CustomAttribute> `.
 *
 * Or
 *
 *
 * 2: Combination of tags: `<Layout> <PropertySet> <Transform> <Motion> <CustomAttribute> `.
 * The advantage of using these is that if not present the attributes are taken from the base
 * layout file. This saves from replicating all the layout tags if only a Motion tag is needed.
 * If <Layout> is used then all layout attributes in the base are ignored. </Layout>
 *
 * <table summary="Constraint attributes">
 * <tr>
 * <td>android:id</td>
 * <td>Id of the TView</td>
</tr> *
 * <tr>
 * <td>[ConstraintLayout attributes]</td>
 * <td>Any attribute that is part of ConstraintLayout layout is allowed</td>
</tr> *
 * <tr>
 * <td>[Standard TView attributes]</td>
 * <td>A collection of view attributes supported by the system (see below)</td>
</tr> *
 * <tr>
 * <td>transitionEasing</td>
 * <td>define an easing curve to be used when animating from this point
 * (e.g. `curve(1.0,0,0,1.0)`)
 * or key words {standard | accelerate | decelerate | linear}</td>
</tr> *
 * <tr>
 * <td>pathMotionArc</td>
 * <td>the path will move in arc (quarter ellipses)
 * or key words {startVertical | startHorizontal | none }</td>
</tr> *
 * <tr>
 * <td>transitionPathRotate</td>
 * <td>(float) rotate object relative to path taken</td>
</tr> *
 * <tr>
 * <td>drawPath</td>
 * <td>draw the path the layout will animate animate</td>
</tr> *
 * <tr>
 * <td>progress</td>
 * <td>call method setProgress(float) on this  view
 * (used to talk to nested ConstraintLayouts etc.)</td>
</tr> *
 * <tr>
 * <td>`<CustomAttribute> `</td>
 * <td>call a set"name" method via reflection</td>
</tr> *
 * <tr>
 * <td>`<Layout> `</td>
 * <td>Attributes for the ConstraintLayout e.g. layout_constraintTop_toTopOf</td>
</tr> *
 * <tr>
 * <td>`<PropertySet> `</td>
 * <td>currently only visibility, alpha, motionProgress,layout_constraintTag.</td>
</tr> *
 * <tr>
 * <td>`<Transform> `</td>
 * <td>All the view transform API such as android:rotation.</td>
</tr> *
 * <tr>
 * <td>`<Motion> `</td>
 * <td>Motion Layout control commands such as transitionEasing and pathMotionArc</td>
</tr> *
</table> *
 *
 *
 *
 *
 *
 * <h2>Layout</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>[ConstraintLayout attributes]</td>
 * <td>see {@see androidx.constraintlayout.widget.
 * * ConstraintLayout ConstraintLayout} for attributes</td>
</tr> *
</table> *
 *
 * <h2>PropertySet</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>visibility</td>
 * <td>set the Visibility of the view. One of Visible, invisible or gone</td>
</tr> *
 * <tr>
 * <td>alpha</td>
 * <td>setAlpha value</td>
</tr> *
 * <tr>
 * <td>motionProgress</td>
 * <td>using reflection call setProgress</td>
</tr> *
 * <tr>
 * <td>layout_constraintTag</td>
 * <td>a tagging string to identify the type of object</td>
</tr> *
</table> *
 *
 *
 * <h2>Transform</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>android:elevation</td>
 * <td>base z depth of the view.</td>
</tr> *
 * <tr>
 * <td>android:rotation</td>
 * <td>rotation of the view, in degrees.</td>
</tr> *
 * <tr>
 * <td>android:rotationX</td>
 * <td>rotation of the view around the x axis, in degrees.</td>
</tr> *
 * <tr>
 * <td>android:rotationY</td>
 * <td>rotation of the view around the y axis, in degrees.</td>
</tr> *
 * <tr>
 * <td>android:scaleX</td>
 * <td>scale of the view in the x direction</td>
</tr> *
 * <tr>
 * <td>android:scaleY</td>
 * <td>scale of the view in the y direction.</td>
</tr> *
 * <tr>
 * <td>android:translationX</td>
 * <td>translation in x of the view. This value is added post-layout to the  left
 * property of the view, which is set by its layout.</td>
</tr> *
 * <tr>
 * <td>android:translationY</td>
 * <td>translation in y of the view. This value is added post-layout to th e top
 * property of the view, which is set by its layout</td>
</tr> *
 * <tr>
 * <td>android:translationZ</td>
 * <td>translation in z of the view. This value is added to its elevation.</td>
</tr> *
</table> *
 *
 *
 * <h2>Motion</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>transitionEasing</td>
 * <td>Defines an acceleration curve.</td>
</tr> *
 * <tr>
 * <td>pathMotionArc</td>
 * <td>Says the object should move in a quarter ellipse
 * unless the motion is vertical or horizontal</td>
</tr> *
 * <tr>
 * <td>motionPathRotate</td>
 * <td>set the rotation to the path of the object + this angle.</td>
</tr> *
 * <tr>
 * <td>drawPath</td>
 * <td>Debugging utility to draw the motion of the path</td>
</tr> *
</table> *
 *
 *
 * <h2>CustomAttribute</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>attributeName</td>
 * <td>The name of the attribute. Case sensitive. ( MyAttr will look for method setMyAttr(...)</td>
</tr> *
 * <tr>
 * <td>customColorValue</td>
 * <td>The value is a color looking setMyAttr(int )</td>
</tr> *
 * <tr>
 * <td>customIntegerValue</td>
 * <td>The value is an integer looking setMyAttr(int )</td>
</tr> *
 * <tr>
 * <td>customFloatValue</td>
 * <td>The value is a float looking setMyAttr(float )</td>
</tr> *
 * <tr>
 * <td>customStringValue</td>
 * <td>The value is a String looking setMyAttr(String )</td>
</tr> *
 * <tr>
 * <td>customDimension</td>
 * <td>The value is a dimension looking setMyAttr(float )</td>
</tr> *
 * <tr>
 * <td>customBoolean</td>
 * <td>The value is true or false looking setMyAttr(boolean )</td>
</tr> *
</table> *
 *
 *
 *
 *
 *
 * <h2>KeyFrameSet</h2>
 *
 *  This is the container for a collection of Key objects (such as KeyPosition) which provide
 * information about how the views should move
 * <table summary="StateSet tags & attributes">
 * <tr>
 * <td>`<KeyPosition>`</td>
 * <td>Controls the layout position during animation</td>
</tr> *
 * <tr>
 * <td>`<KeyAttribute>`</td>
 * <td>Controls the post layout properties during animation</td>
</tr> *
 * <tr>
 * <td>`<KeyCycle>`</td>
 * <td>Controls oscillations with respect to position
 * of post layout properties during animation</td>
</tr> *
 * <tr>
 * <td>`<KeyTimeCycle>`</td>
 * <td>Controls oscillations with respect to time of post layout properties during animation</td>
</tr> *
 * <tr>
 * <td>`<KeyTrigger>`</td>
 * <td>trigger callbacks into code at fixed point during the animation</td>
</tr> *
</table> *
 *
 *
 *
 * <h2>KeyPosition</h2>
 * <table summary="KeyPosition attributes">
 * <tr>
 * <td>motionTarget</td>
 * <td>Id of the TView or a regular expression to match layout_ConstraintTag</td>
</tr> *
 * <tr>
 * <td>framePosition</td>
 * <td>The point along the interpolation 0 = start 100 = end</td>
</tr> * <tr>
 * <td>transitionEasing</td>
 * <td>define an easing curve to be used when animating from this point (e.g. curve(1.0,0,0, 1.0))
 * or key words {standard | accelerate | decelerate | linear }
</td> *
</tr> *
 * <tr>
 * <td>pathMotionArc</td>
 * <td>The path will move in arc (quarter ellipses)
 * key words {startVertical | startHorizontal | flip | none }</td>
</tr> *
 * <tr>
 * <td>keyPositionType</td>
 * <td>how this keyframe's deviation for linear path is calculated
 * {deltaRelative | pathRelative|parentRelative}</td>
</tr> *
 * <tr>
 * <td>percentX</td>
 * <td>(float) percent distance from start to end along
 * X axis (deltaRelative) or along the path in pathRelative</td>
</tr> *
 * <tr>
 * <td>percentY</td>
 * <td>(float) Percent distance from start to end along Y axis
 * (deltaRelative) or perpendicular to path in pathRelative</td>
</tr> *
 * <tr>
 * <td>percentWidth</td>
 * <td>(float) Percent of change in the width.
 * Note if the width does not change this has no effect.This overrides sizePercent.</td>
</tr> *
 * <tr>
 * <td>percentHeight</td>
 * <td>(float) Percent of change in the width.
 * Note if the width does not change this has no effect.This overrides sizePercent.</td>
</tr> *
 * <tr>
 * <td>curveFit</td>
 * <td>path is traced</td>
</tr> *
 * <tr>
 * <td>drawPath</td>
 * <td>Draw the path of the objects layout takes useful for debugging</td>
</tr> *
 * <tr>
 * <td>sizePercent</td>
 * <td>If the view changes size this controls how growth of the  size.
 * (for fixed size objects use KeyAttributes scaleX/X)</td>
</tr> *
 * <tr>
 * <td>curveFit</td>
 * <td>selects a path based on straight lines or a path based on a
 * monotonic spline {linear|spline}</td>
</tr> *
</table> *
 *
 *
 *
 *
 *
 * <h2>KeyAttribute</h2>
 * <table summary="KeyAttribute attributes">
 * <tr>
 * <td>motionTarget</td>
 * <td>Id of the TView or a regular expression to match layout_ConstraintTag</td>
</tr> *
 * <tr>
 * <td>framePosition</td>
 * <td>The point along the interpolation 0 = start 100 = end</td>
</tr> *
 * <tr>
 * <td>curveFit</td>
 * <td>selects a path based on straight lines or a path
 * based on a monotonic spline {linear|spline}</td>
</tr> *
 * <tr>
 * <td>transitionEasing</td>
 * <td>Define an easing curve to be used when animating from this point (e.g. curve(1.0,0,0, 1.0))
 * or key words {standard | accelerate | decelerate | linear }
</td> *
</tr> *
 * <tr>
 * <td>transitionPathRotate</td>
 * <td>(float) rotate object relative to path taken</td>
</tr> *
 * <tr>
 * <td>drawPath</td>
 * <td>draw the path the layout will animate animate</td>
</tr> *
 * <tr>
 * <td>motionProgress</td>
 * <td>call method setProgress(float) on this  view
 * (used to talk to nested ConstraintLayouts etc.)</td>
</tr> *
 * <tr>
 * <td>[standard view attributes](except visibility)</td>
 * <td>A collection of post layout view attributes see below </td>
</tr> *
 * <tr>
 *
 *
</tr> * <tr>
 * <td>`<CustomAttribute> `</td>
 * <td>call a set"name" method via reflection</td>
</tr> *
</table> *
 *
 * <h2>CustomAttribute</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>attributeName</td>
 * <td>The name of the attribute. Case sensitive. ( MyAttr will look for method setMyAttr(...)</td>
</tr> *
 * <tr>
 * <td>customColorValue</td>
 * <td>The value is a color looking setMyAttr(int )</td>
</tr> *
 * <tr>
 * <td>customIntegerValue</td>
 * <td>The value is an integer looking setMyAttr(int )</td>
</tr> *
 * <tr>
 * <td>customFloatValue</td>
 * <td>The value is a float looking setMyAttr(float )</td>
</tr> *
 * <tr>
 * <td>customStringValue</td>
 * <td>The value is a String looking setMyAttr(String )</td>
</tr> *
 * <tr>
 * <td>customDimension</td>
 * <td>The value is a dimension looking setMyAttr(float )</td>
</tr> *
 * <tr>
 * <td>customBoolean</td>
 * <td>The value is true or false looking setMyAttr(boolean )</td>
</tr> *
</table> *
 *
 *
 *
 *
 * <h2>KeyCycle</h2>
 * <table summary="Constraint attributes">
 * <tr>
 * <td>motionTarget</td>
 * <td>Id of the TView or a regular expression to match layout_ConstraintTag</td>
</tr> *
 * <tr>
 * <td>framePosition</td>
 * <td>The point along the interpolation 0 = start 100 = end</td>
</tr> *
 * <tr>
 * <td>[Standard TView attributes]</td>
 * <td>A collection of view attributes supported by the system (see below)</td>
</tr> *
 * <tr>
 * <td>waveShape</td>
 * <td>The shape of the wave to generate
 * {sin|square|triangle|sawtooth|reverseSawtooth|cos|bounce}</td>
</tr> *
 * <tr>
 * <td>wavePeriod</td>
 * <td>The number of cycles to loop near this region</td>
</tr> *
 * <tr>
 * <td>waveOffset</td>
 * <td>offset value added to the attribute</td>
</tr> *
 * <tr>
 * <td>transitionPathRotate</td>
 * <td>Cycles applied to rotation relative to the path the view is travelling</td>
</tr> *
 * <tr>
 * <td>progress</td>
 * <td>call method setProgress(float) on this  view
 * (used to talk to nested ConstraintLayouts etc.)</td>
</tr> *
 * <tr>
 * <td>`<CustomAttribute> `</td>
 * <td>call a set"name" method via reflection (limited to floats)</td>
</tr> *
</table> *
 *
 * <h2>CustomAttribute</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>attributeName</td>
 * <td>The name of the attribute. Case sensitive. ( MyAttr will look for method setMyAttr(...)</td>
</tr> *
 * <tr>
</tr> * <tr>
 * <td>customFloatValue</td>
 * <td>The value is a float looking setMyAttr(float )</td>
</tr> *
 * <tr>
</tr></table> *
 *
 * <h2>KeyTimeCycle</h2>
 * <table summary="Constraint attributes">
 * <tr>
 * <td>motionTarget</td>
 * <td>Id of the TView or a regular expression to match layout_ConstraintTag</td>
</tr> *
 * <tr>
 * <td>framePosition</td>
 * <td>The point along the interpolation 0 = start 100 = end</td>
</tr> *
 * <tr>
 * <td>[Standard TView attributes]</td>
 * <td>A collection of view attributes supported by the system (see below)</td>
</tr> *
 * <tr>
 * <td>waveShape</td>
 * <td>The shape of the wave to generate
 * {sin|square|triangle|sawtooth|reverseSawtooth|cos|bounce}</td>
</tr> *
 * <tr>
 * <td>wavePeriod</td>
 * <td>The number of cycles per second</td>
</tr> *
 * <tr>
 * <td>waveOffset</td>
 * <td>offset value added to the attribute</td>
</tr> *
 * <tr>
 * <td>transitionPathRotate</td>
 * <td>Cycles applied to rotation relative to the path the view is travelling</td>
</tr> *
 * <tr>
 * <td>progress</td>
 * <td>call method setProgress(float) on this  view
 * (used to talk to nested ConstraintLayouts etc.)</td>
</tr> *
 * <tr>
 * <td>`<CustomAttribute> `</td>
 * <td>call a set"name" method via reflection (limited to floats)</td>
</tr> *
</table> *
 *
 * <h2>CustomAttribute</h2>
 * <table summary="Variant attributes">
 * <tr>
 * <td>attributeName</td>
 * <td>The name of the attribute. Case sensitive. ( MyAttr will look for method setMyAttr(...)</td>
</tr> *
 * <tr>
</tr> * <tr>
 * <td>customFloatValue</td>
 * <td>The value is a float looking setMyAttr(float )</td>
</tr> *
 * <tr>
</tr></table> *
 *
 * <h2>KeyTrigger</h2>
 * <table summary="KeyTrigger attributes">
 * <tr>
 * <td>motionTarget</td>
 * <td>Id of the TView or a regular expression to match layout_ConstraintTag</td>
</tr> *
 * <tr>
 * <td>framePosition</td>
 * <td>The point along the interpolation 0 = start 100 = end</td>
</tr> * <tr>
 * <td>onCross</td>
 * <td>(method name) on crossing this position call this methods on the t arget
</td> *
</tr> *
 * <tr>
 * <td>onPositiveCross</td>
 * <td>(method name) on forward crossing of the framePosition call this methods on the target</td>
</tr> *
 * <tr>
 * <td>onNegativeCross/td>
</td> * <td>(method name) backward crossing of the framePosition call this methods on the target</td>
</tr> *
 * <tr>
 * <td>viewTransitionOnCross</td>
 * <td>(ViewTransition Id) start a NoState view transition on crossing or hitting target
</td> *
</tr> *
 * <tr>
 * <td>viewTransitionOnPositiveCross</td>
 * <td>(ViewTransition Id) start a NoState view transition forward crossing of the
 * framePosition or entering target</td>
</tr> *
 * <tr>
 * <td>viewTransitionOnNegativeCross/td>
</td> * <td>(ViewTransition Id) start a NoState view transition backward crossing of the
 * framePosition or leaving target</td>
</tr> *
 * <tr>
 * <td>triggerSlack</td>
 * <td>(float) do not call trigger again if the framePosition has not moved this
 * fraction away from the trigger point</td>
</tr> *
 * <tr>
 * <td>triggerId</td>
 * <td>(id) call the TransitionListener with this trigger id</td>
</tr> *
 * <tr>
 * <td>motion_postLayoutCollision</td>
 * <td>Define motion pre or post layout. Post layout is more expensive but captures
 * KeyAttributes or KeyCycle motions.</td>
</tr> *
 * <tr>
 * <td>motion_triggerOnCollision</td>
 * <td>(id) Trigger if the motionTarget collides with the other motionTarget</td>
</tr> *
</table> *
 *
 *
 *
 *
 * <h2>Standard attributes</h2>
 * <table summary="Constraint attributes">
 * <tr>
 * <td>android:visibility</td>
 * <td>Android view attribute that</td>
</tr> *
 * <tr>
 * <td>android:alpha</td>
 * <td>Android view attribute that</td>
</tr> *
 * <tr>
 * <td>android:elevation</td>
 * <td>base z depth of the view.</td>
</tr> *
 * <tr>
 * <td>android:rotation</td>
 * <td>rotation of the view, in degrees.</td>
</tr> *
 * <tr>
 * <td>android:rotationX</td>
 * <td>rotation of the view around the x axis, in degrees.</td>
</tr> *
 * <tr>
 * <td>android:rotationY</td>
 * <td>rotation of the view around the y axis, in degrees.</td>
</tr> *
 * <tr>
 * <td>android:scaleX</td>
 * <td>scale of the view in the x direction.</td>
</tr> *
 * <tr>
 * <td>android:scaleY</td>
 * <td>scale of the view in the y direction.</td>
</tr> *
 * <tr>
 * <td>android:translationX</td>
 * <td>translation in x of the view.</td>
</tr> *
 * <tr>
 * <td>android:translationY</td>
 * <td>translation in y of the view.</td>
</tr> *
 * <tr>
 * <td>android:translationZ</td>
 * <td>translation in z of the view.</td>
</tr> *
 *
 *
</table> *
 *
 *
 */
open class MotionLayout(context: TContext, attrs: AttributeSet, self: TView) : ConstraintLayout(context, attrs, self) {
    var mScene: MotionScene? = null
    var mInterpolator: Interpolator? = null
    var mProgressInterpolator: Interpolator? = null

    private var mBeginState: String = UNSET_ID

    /**
     * Return the current state id
     *
     * @return current state id
     */
    var currentState: String = UNSET_ID

    /**
     * Gets the state you are currently transition to.
     *
     * @return The State you are transitioning to.
     */
    var endState: String = UNSET_ID
        private set
    private var mLastWidthMeasureSpec = 0
    private var mLastHeightMeasureSpec = 0
    /**
     * Determines whether MotionLayout's touch & click handling are enabled.
     * An interaction enabled MotionLayout can respond to user input and initiate and control.
     * MotionLayout interactions are enabled initially by default.
     * MotionLayout touch & click handling may be enabled or disabled by calling its
     * setInteractionEnabled method.
     *
     * @return true if MotionLayout's  touch & click  is enabled, false otherwise
     */
    /**
     * Enables (or disables) MotionLayout's onClick and onSwipe handling.
     *
     * @param enabled If true,  touch & click  is enabled; otherwise it is disabled
     */
    var isInteractionEnabled = true
    var mFrameArrayList: HashMap<TView, MotionController> = HashMap()
    private var mAnimationStartTime: Long = 0
    private var mTransitionDuration = 1f
    var mTransitionPosition = 0.0f
    var mTransitionLastPosition = 0.0f
    private var mTransitionLastTime: Long = 0

    /**
     * Gets the position you are animating to typically 0 or 1.
     * This is useful during animation after touch up
     *
     * @return The target position you are moving to
     */
    var targetPosition = 0.0f
    private var mTransitionInstantly = false
    var mInTransition = false
    var mIndirectTransition = false
    private var mTransitionListener: TransitionListener? = null
    private var mLastPos = 0f
    private var mLastY = 0f
    var mDebugPath = 0
    //var mDevModeDraw: DevModeDraw? = null
    private var mTemporalInterpolator = false
    private val mStopLogic: StopLogic = StopLogic()
    private val mDecelerateLogic = DecelerateInterpolator()
    private var mDesignTool: DesignTool? = null
    var mFirstDown = true
    var mOldWidth = 0
    var mOldHeight = 0
    var mLastLayoutWidth = 0
    var mLastLayoutHeight = 0
    var mUndergoingMotion = false
    var mScrollTargetDX = 0f
    var mScrollTargetDY = 0f
    var mScrollTargetTime: Long = 0
    var mScrollTargetDT = 0f
    private var mKeepAnimating = false
    private var mOnShowHelpers: ArrayList<MotionHelper>? = null
    private var mOnHideHelpers: ArrayList<MotionHelper>? = null
    private var mDecoratorsHelpers: ArrayList<MotionHelper>? = null
    private var mTransitionListeners: ArrayList<TransitionListener>? = null
    private var mFrames = 0
    private var mLastDrawTime: Long = -1
    private var mLastFps = 0f
    private var mListenerState = ""
    private var mListenerPosition = 0.0f
    var mIsAnimating = false
    protected var mMeasureDuringTransition = false
    var mStartWrapWidth = 0
    var mStartWrapHeight = 0
    var mEndWrapWidth = 0
    var mEndWrapHeight = 0
    var mWidthMeasureMode = 0
    var mHeightMeasureMode = 0
    var mPostInterpolationPosition = 0f
    private val mKeyCache: KeyCache = KeyCache()
    private var mInLayout = false
    private var mStateCache: StateCache? = null
    private var mOnComplete: TRunnable? = null
    private var mScheduledTransitionTo: Array<String>? = null
    var mScheduledTransitions = 0
    var isInRotation = false
        private set
    var mRotatMode = 0
    var mPreRotate: MutableMap<TView, ViewState> = mutableMapOf()
    private var mPreRotateWidth = 0
    private var mPreRotateHeight = 0
    private var mPreviouseRotation = 0f
    var mTempRect: Rect = Rect()
    /**
     * Is initial state changes are applied during onAttachedToWindow or after.
     * @return
     */
    /**
     * Initial state changes are applied during onAttachedToWindow unless this is set to true.
     * @param delayedApply
     */
    var isDelayedApplicationOfInitialState = false
    fun getMotionController(mTouchAnchorId: String): MotionController? {
        return mFrameArrayList[self.findViewById(mTouchAnchorId)]
    }

    enum class TransitionState {
        UNDEFINED, SETUP, MOVING, FINISHED
    }

    var mTransitionState = TransitionState.UNDEFINED

    init {
        self.setParentType(this)
        self.swizzleFunction("onMeasure") { sup, params ->
            var args = params as Array<Any>
            onMeasure(sup, params[0] as Int, params[1] as Int)
            0
        }
        self.swizzleFunction("onAttachedToWindow") { sup, params ->
            onAttachedToWindow(sup)
            0
        }
        self.swizzleFunction("onInterceptTouchEvent") { sup, params ->
            var args = params as Array<Any>
            onInterceptTouchEvent(sup, params[0] as MotionEvent)
        }
        self.swizzleFunction("onTouchEvent") { sup, params ->
            var args = params as Array<Any>
            onTouchEvent(sup, params[0] as MotionEvent)
        }
        self.swizzleFunction("onRtlPropertiesChanged") { sup, params ->
            var args = params as Array<Any>
            onRtlPropertiesChanged(sup, params[0] as Int)
        }
        IS_IN_EDIT_MODE = self.isInEditMode()
        var apply = true
        attrs.forEach { kvp ->
            if(kvp.key == "layoutDescription") {
                val id = context.getResources().getResourceId(kvp.value, UNSET_ID)
                mScene = MotionScene(context, this, id)
            } else if(kvp.key == "currentState") {
                currentState = context.getResources().getResourceId(kvp.value, currentState)
            } else if(kvp.key == "motionProgress") {
                targetPosition = context.getResources().getFloat(
                    kvp.key,
                    kvp.value,
                    targetPosition
                )
                mInTransition = true
            } else if(kvp.key == "applyMotionScene") {
                apply = context.getResources().getBoolean(kvp.value, apply)
            } else if(kvp.key == "showPaths") {
                if (mDebugPath == 0) { // favor motionDebug
                    mDebugPath = if (context.getResources().getBoolean(kvp.value, false)) DEBUG_SHOW_PATH else 0
                }
            } else if(kvp.key == "motionDebug") {
                mDebugPath = context.getResources().getInt(kvp.key, kvp.value, mDebugPath)
            }
        }
        if (mScene == null) {
            Log.e(TAG, "WARNING NO app:layoutDescription tag")
        }
        if (!apply) {
            mScene = null
        }
        if (mDebugPath != 0) {
            checkStructure()
        }
        if (currentState == UNSET_ID && mScene != null) {
            currentState = mScene!!.startId
            mBeginState = mScene!!.startId
            if (I_DEBUG) {
                Log.v(
                    TAG, " ============= init   end is "
                            + Debug.getName(self.getContext(), endState)
                )
            }
            endState = mScene!!.endId
            if (I_DEBUG) {
                Log.v(
                    TAG, " ============= init setting end to "
                            + Debug.getName(self.getContext(), endState)
                )
            }
        }
    }

    /**
     * Subclasses can override to define testClasses
     *
     * @return
     */
    protected val nanoTime: Long
        protected get() = nanoTime()

    /**
     * Subclasses can override to build test frameworks
     *
     * @return
     */
    fun obtainVelocityTracker(): MotionTracker {
        return MyTracker.obtain()
    }

    /**
     * Disable the transition based on transitionID
     * @param transitionID
     * @param enable
     */
    fun enableTransition(transitionID: String, enable: Boolean) {
        val t: MotionScene.Transition? = getTransition(transitionID)
        if (enable) {
            t?.isEnabled = true
            return
        } else {
            if (t == mScene!!.mCurrentTransition) { // disabling current transition
                val transitions: List<MotionScene.Transition?> = mScene!!.getTransitionsWithState(
                    currentState
                )
                for (transition in transitions) {
                    if (transition!!.isEnabled) {
                        mScene!!.mCurrentTransition = transition
                        break
                    }
                }
            }
            t?.isEnabled = false
        }
    }

    /**
     * Subclasses can override to build test frameworks
     */
    interface MotionTracker {
        fun recycle()
        fun clear()
        fun addMovement(event: MotionEvent)
        fun computeCurrentVelocity(units: Int)
        fun computeCurrentVelocity(units: Int, maxVelocity: Float)
        val xVelocity: Float
        val yVelocity: Float
        fun getXVelocity(id: Int): Float
        fun getYVelocity(id: Int): Float
    }

    fun setState(newState: TransitionState) {
        if (I_DEBUG) {
            Debug.logStack(
                TAG, mTransitionState.toString() + " -> " + newState + " "
                        + Debug.getName(self.getContext(), currentState), 2
            )
        }
        if (newState == TransitionState.FINISHED && currentState == UNSET_ID) {
            return
        }
        val oldState = mTransitionState
        mTransitionState = newState
        if (oldState == TransitionState.MOVING && newState == TransitionState.MOVING) {
            fireTransitionChange()
        }
        when (oldState) {
            TransitionState.UNDEFINED, TransitionState.SETUP -> {
                if (newState == TransitionState.MOVING) {
                    fireTransitionChange()
                }
                if (newState == TransitionState.FINISHED) {
                    fireTransitionCompleted()
                }
            }
            TransitionState.MOVING -> if (newState == TransitionState.FINISHED) {
                fireTransitionCompleted()
            }
            TransitionState.FINISHED -> {}
        }
    }

    private class MyTracker : MotionTracker {
        var mTracker: VelocityTracker? = null

        override fun recycle() {
            if (mTracker != null) {
                mTracker!!.recycle()
                mTracker = null // not allowed to call after recycle
            }
        }

        override fun clear() {
            if (mTracker != null) {
                mTracker!!.clear()
            }
        }

        override fun addMovement(event: MotionEvent) {
            if (mTracker != null) {
                mTracker!!.addMovement(event)
            }
        }

        override fun computeCurrentVelocity(units: Int) {
            if (mTracker != null) {
                mTracker!!.computeCurrentVelocity(units)
            }
        }

        override fun computeCurrentVelocity(units: Int, maxVelocity: Float) {
            if (mTracker != null) {
                mTracker!!.computeCurrentVelocity(units, maxVelocity)
            }
        }

        override val xVelocity: Float
            get() = if (mTracker != null) {
                mTracker!!.xVelocity
            } else 0f

        override val yVelocity: Float
            get() = if (mTracker != null) {
                mTracker!!.yVelocity
            } else 0f

        override fun getXVelocity(id: Int): Float {
            return if (mTracker != null) {
                mTracker!!.getXVelocity(id)
            } else 0f
        }

        override fun getYVelocity(id: Int): Float {
            return if (mTracker != null) {
                mTracker!!.getYVelocity(id)
            } else 0f
        }

        companion object {
            private val sMe = MyTracker()
            fun obtain(): MyTracker {
                sMe.mTracker = VelocityTracker.obtain()
                return sMe
            }
        }
    }

    /**
     * Set a transition explicitly between two constraint sets
     *
     * @param beginId the id of the start constraint set
     * @param endId   the id of the end constraint set
     */
    fun setTransition(beginId: String, endId: String) {
        if (!isAttachedToWindow) {
            if (mStateCache == null) {
                mStateCache = StateCache()
            }
            mStateCache!!.setStartState(beginId)
            mStateCache!!.setEndState(endId)
            return
        }
        if (mScene != null) {
            mBeginState = beginId
            endState = endId
            if (I_DEBUG) {
                Log.v(
                    TAG, Debug.location + " setTransition "
                            + Debug.getName(self.getContext(), beginId) + " -> "
                            + Debug.getName(self.getContext(), endId)
                )
            }
            mScene!!.setTransition(beginId, endId)
            mModel.initFrom(
                mLayoutWidget, mScene!!.getConstraintSet(beginId),
                mScene!!.getConstraintSet(endId)
            )
            rebuildScene()
            mTransitionLastPosition = 0f
            transitionToStart()
        }
    }

    /**
     * Set a transition explicitly to a Transition that has an ID
     * The transition must have been named with android:id=...
     *
     * @param transitionId the id to set
     */
    fun setTransition(transitionId: String) {
        if (mScene != null) {
            val transition: MotionScene.Transition? = getTransition(transitionId)
            mBeginState = transition?.startConstraintSetId ?: ""
            endState = transition?.endConstraintSetId ?: ""
            if (!isAttachedToWindow) {
                if (mStateCache == null) {
                    mStateCache = StateCache()
                }
                mStateCache!!.setStartState(mBeginState)
                mStateCache!!.setEndState(endState)
                return
            }
            if (I_DEBUG) {
                Log.v(
                    TAG, Debug.location + " setTransition "
                            + Debug.getName(self.getContext(), mBeginState) + " -> "
                            + Debug.getName(self.getContext(), endState)
                            + "   current=" + Debug.getName(self.getContext(), currentState)
                )
            }
            var pos = Float.NaN
            if (currentState == mBeginState) {
                pos = 0f
            } else if (currentState == endState) {
                pos = 1f
            }
            mScene!!.setTransition(transition)
            mModel.initFrom(
                mLayoutWidget,
                mScene!!.getConstraintSet(mBeginState),
                mScene!!.getConstraintSet(endState)
            )
            rebuildScene()
            if (mTransitionLastPosition != pos) {
                // If the last drawn position isn't the same,
                // we might have to make sure we apply the corresponding constraintset.
                if (pos == 0f) {
                    endTrigger(true)
                    mScene!!.getConstraintSet(mBeginState)?.applyTo(this)
                } else if (pos == 1f) {
                    endTrigger(false)
                    mScene!!.getConstraintSet(endState)?.applyTo(this)
                }
            }
            mTransitionLastPosition = if (Float.isNaN(pos)) 0f else pos
            if (Float.isNaN(pos)) {
                Log.v(TAG, Debug.location + " transitionToStart ")
                transitionToStart()
            } else {
                progress = pos
            }
        }
    }

    fun setTransition(transition: MotionScene.Transition?) {
        mScene!!.setTransition(transition)
        setState(TransitionState.SETUP)
        if (currentState == mScene!!.endId) {
            mTransitionLastPosition = 1.0f
            mTransitionPosition = 1.0f
            targetPosition = 1f
        } else {
            mTransitionLastPosition = 0f
            mTransitionPosition = 0f
            targetPosition = 0f
        }
        mTransitionLastTime =
            if (transition!!.isTransitionFlag(TRANSITION_FLAG_FIRST_DRAW)) -1 else nanoTime
        if (I_DEBUG) {
            Log.v(
                TAG, Debug.location + "  new mTransitionLastPosition = "
                        + mTransitionLastPosition + ""
            )
            Log.v(
                TAG, Debug.location + " setTransition was "
                        + Debug.getName(self.getContext(), mBeginState)
                        + " -> " + Debug.getName(self.getContext(), endState)
            )
        }
        val newBeginState = mScene!!.startId
        val newEndState = mScene!!.endId
        if (newBeginState == mBeginState && newEndState == endState) {
            return
        }
        mBeginState = newBeginState
        endState = newEndState
        mScene!!.setTransition(mBeginState, endState)
        if (I_DEBUG) {
            Log.v(
                TAG, Debug.location + " setTransition now "
                        + Debug.getName(self.getContext(), mBeginState) + " -> "
                        + Debug.getName(self.getContext(), endState)
            )
        }
        mModel.initFrom(
            mLayoutWidget,
            mScene!!.getConstraintSet(mBeginState),
            mScene!!.getConstraintSet(endState)
        )
        mModel.setMeasuredId(mBeginState, endState)
        mModel.reEvaluateState()
        rebuildScene()
    }

    /**
     * This overrides ConstraintLayout and only accepts a MotionScene.
     *
     * @param motionScene The resource id, or 0 to reset the MotionScene.
     */
    override fun loadLayoutDescription(motionScene: String) {
        if (motionScene != UNSET_ID) {
            try {
                mScene = MotionScene(self.getContext(), this, motionScene)
                if (currentState == UNSET_ID && mScene != null) {
                    currentState = mScene!!.startId
                    mBeginState = mScene!!.startId
                    endState = mScene!!.endId
                }
                if (isAttachedToWindow) {
                    try {
                        mPreviouseRotation = if (self.getDisplay() == null) 0f else self.getRotation()
                        if (mScene != null) {
                            val cSet: ConstraintSet? = mScene!!.getConstraintSet(currentState)
                            mScene!!.readFallback(this)
                            if (mDecoratorsHelpers != null) {
                                for (mh in mDecoratorsHelpers!!) {
                                    mh.onFinishedMotionScene(this)
                                }
                            }
                            if (cSet != null) {
                                cSet.applyTo(this)
                            }
                            mBeginState = currentState
                        }
                        onNewStateAttachHandlers()
                        if (mStateCache != null) {
                            if (isDelayedApplicationOfInitialState) {
                                self.post(object : TRunnable {
                                    override fun run() {
                                        mStateCache!!.apply()
                                    }
                                })
                            } else {
                                mStateCache!!.apply()
                            }
                        } else {
                            if (mScene != null && mScene!!.mCurrentTransition != null) {
                                if (mScene!!.mCurrentTransition!!.autoTransition
                                    == MotionScene.Transition.AUTO_ANIMATE_TO_END
                                ) {
                                    transitionToEnd()
                                    setState(TransitionState.SETUP)
                                    setState(TransitionState.MOVING)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        throw IllegalArgumentException("unable to parse MotionScene file", ex)
                    }
                } else {
                    mScene = null
                }
            } catch (ex: Exception) {
                throw IllegalArgumentException("unable to parse MotionScene file", ex)
            }
        } else {
            mScene = null
        }
    }

    /**
     * Returns true if the provided view is currently attached to a window.
     */
    val isAttachedToWindow: Boolean
        get() = self.isAttachedToWindow()

    /**
     * Set the State of the Constraint layout. Causing it to load a particular ConstraintSet.
     * for states with variants the variant with matching
     * width and height constraintSet will be chosen
     *
     * @param id           set the state width and height
     * @param screenWidth
     * @param screenHeight
     */
    override fun setState(id: String, screenWidth: Int, screenHeight: Int) {
        setState(TransitionState.SETUP)
        currentState = id
        mBeginState = UNSET_ID
        endState = UNSET_ID
        if (mConstraintLayoutSpec != null) {
            mConstraintLayoutSpec!!.updateConstraints(id, screenWidth.toFloat(), screenHeight.toFloat())
        } else if (mScene != null) {
            mScene!!.getConstraintSet(id)?.applyTo(this)
        }
    }

    /**
     * Set the transition position between 0 an 1
     *
     * @param pos
     */
    fun setInterpolatedProgress(pos: Float) {
        if (mScene != null) {
            setState(TransitionState.MOVING)
            val interpolator: Interpolator? = mScene!!.interpolator
            if (interpolator != null) {
                progress = interpolator!!.getInterpolation(pos)
                return
            }
        }
        progress = pos
    }

    /**
     * Set the transition position between 0 an 1
     *
     * @param pos
     * @param velocity
     */
    fun setProgress(pos: Float, velocity: Float) {
        if (!isAttachedToWindow) {
            if (mStateCache == null) {
                mStateCache = StateCache()
            }
            mStateCache!!.setProgress(pos)
            mStateCache!!.setVelocity(velocity)
            return
        }
        progress = pos
        setState(TransitionState.MOVING)
        this.velocity = velocity
        if (velocity != 0.0f) {
            animateTo(if (velocity > 0f) 1f else 0f)
        } else if (pos != 0f && pos != 1f) {
            animateTo(if (pos > 0.5f) 1f else 0f)
        }
    }

    /////////////////////// use to cache the state
    internal inner class StateCache {
        var mProgress = Float.NaN
        var mVelocity = Float.NaN
        var mStartState = UNSET_ID
        var mEndState = UNSET_ID
        val mKeyProgress = "motion.progress"
        val mKeyVelocity = "motion.velocity"
        val mKeyStartState = "motion.StartState"
        val mKeyEndState = "motion.EndState"
        fun apply() {
            if (mStartState != UNSET_ID || mEndState != UNSET_ID) {
                if (mStartState == UNSET_ID) {
                    transitionToState(mEndState)
                } else if (mEndState == UNSET_ID) {
                    setState(mStartState, -1, -1)
                } else {
                    setTransition(mStartState, mEndState)
                }
                setState(TransitionState.SETUP)
            }
            if (Float.isNaN(mVelocity)) {
                if (Float.isNaN(mProgress)) {
                    return
                }
                progress = mProgress
                return
            }
            this@MotionLayout.setProgress(mProgress, mVelocity)
            mProgress = Float.NaN
            mVelocity = Float.NaN
            mStartState = UNSET_ID
            mEndState = UNSET_ID
        }

        var transitionState: Bundle
            get() {
                val bundle = Bundle()
                bundle.putFloat(mKeyProgress, mProgress)
                bundle.putFloat(mKeyVelocity, mVelocity)
                bundle.putString(mKeyStartState, mStartState)
                bundle.putString(mKeyEndState, mEndState)
                return bundle
            }
            set(bundle) {
                mProgress = bundle.getFloat(mKeyProgress)
                mVelocity = bundle.getFloat(mKeyVelocity)
                mStartState = bundle.getString(mKeyStartState)
                mEndState = bundle.getString(mKeyEndState)
            }

        fun setProgress(progress: Float) {
            mProgress = progress
        }

        fun setEndState(endState: String) {
            mEndState = endState
        }

        fun setVelocity(mVelocity: Float) {
            this.mVelocity = mVelocity
        }

        fun setStartState(startState: String) {
            mStartState = startState
        }

        fun recordState() {
            mEndState = endState
            mStartState = mBeginState
            mVelocity = this@MotionLayout.velocity
            mProgress = progress
        }
    }
    /**
     * @return bundle containing start and end state
     */
    /**
     * Set the transition state as a bundle
     */
    var transitionState: Bundle
        get() {
            if (mStateCache == null) {
                mStateCache = StateCache()
            }
            mStateCache!!.recordState()
            return mStateCache!!.transitionState
        }
        set(bundle) {
            if (mStateCache == null) {
                mStateCache = StateCache()
            }
            mStateCache!!.transitionState = bundle
            if (isAttachedToWindow) {
                mStateCache!!.apply()
            }
        }

    /**
     * Create a transition view for every view
     */
    private fun setupMotionViews() {
        val n: Int = self.getChildCount()
        mModel.build()
        mInTransition = true
        val controllers: MutableMap<String, MotionController?> = mutableMapOf()
        for (i in 0 until n) {
            val child = self.getChildAt(i)
            controllers[child.getId()] = mFrameArrayList[child]
        }
        val layoutWidth: Int = self.getWidth()
        val layoutHeight: Int = self.getHeight()
        val arc: Int = mScene!!.gatPathMotionArc()
        if (arc != UNSET) {
            for (i in 0 until n) {
                val motionController: MotionController? = mFrameArrayList[self.getChildAt(i)]
                if (motionController != null) {
                    motionController.setPathMotionArc(arc)
                }
            }
        }
        val sparseBooleanArray = mutableMapOf<String, Boolean>()
        val depends = Array<String>(mFrameArrayList.size) { UNSET_ID }
        var count = 0
        for (i in 0 until n) {
            val view: TView = self.getChildAt(i)
            val motionController: MotionController? = mFrameArrayList[view]
            if (motionController?.animateRelativeTo != UNSET_ID) {
                sparseBooleanArray.put(motionController!!.animateRelativeTo, true)
                depends[count++] = motionController!!.animateRelativeTo
            }
        }
        if (mDecoratorsHelpers != null) {
            for (i in 0 until count) {
                val motionController: MotionController = mFrameArrayList[self.findViewById(depends[i])]
                    ?: continue
                mScene!!.getKeyFrames(motionController)
            }
            // Allow helpers to access all the motionControllers after
            for (mDecoratorsHelper in mDecoratorsHelpers!!) {
                mDecoratorsHelper.onPreSetup(this, mFrameArrayList)
            }
            for (i in 0 until count) {
                val motionController: MotionController = mFrameArrayList[self.findViewById(depends[i])]
                    ?: continue
                motionController.setup(
                    layoutWidth, layoutHeight,
                    mTransitionDuration, nanoTime
                )
            }
        } else {
            for (i in 0 until count) {
                val motionController: MotionController = mFrameArrayList[self.findViewById(depends[i])]
                    ?: continue
                mScene!!.getKeyFrames(motionController)
                motionController.setup(
                    layoutWidth, layoutHeight,
                    mTransitionDuration, nanoTime
                )
            }
        }
        // getMap the KeyFrames for each view
        for (i in 0 until n) {
            val v: TView = self.getChildAt(i)
            val motionController: MotionController? = mFrameArrayList[v]
            if (sparseBooleanArray.get(v.getId()) == true) {
                continue
            }
            if (motionController != null) {
                mScene!!.getKeyFrames(motionController)
                motionController.setup(
                    layoutWidth, layoutHeight,
                    mTransitionDuration, nanoTime
                )
            }
        }
        var stagger: Float = mScene?.staggered ?: 0f
        if (stagger != 0.0f) {
            val flip = stagger < 0.0
            var useMotionStagger = false
            stagger = abs(stagger)
            var min = Float.MAX_VALUE
            var max = -Float.MAX_VALUE
            for (i in 0 until n) {
                val f: MotionController? = mFrameArrayList[self.getChildAt(i)]
                if (!Float.isNaN(f!!.mMotionStagger)) {
                    useMotionStagger = true
                    break
                }
                val x: Float = f.finalX
                val y: Float = f.finalY
                val mdist = if (flip) y - x else y + x
                min = min(min, mdist)
                max = max(max, mdist)
            }
            if (useMotionStagger) {
                min = Float.MAX_VALUE
                max = -Float.MAX_VALUE
                for (i in 0 until n) {
                    val f: MotionController? = mFrameArrayList[self.getChildAt(i)]
                    if (!Float.isNaN(f!!.mMotionStagger)) {
                        min = min(min, f!!.mMotionStagger)
                        max = max(max, f!!.mMotionStagger)
                    }
                }
                for (i in 0 until n) {
                    val f: MotionController? = mFrameArrayList[self.getChildAt(i)]
                    if (!Float.isNaN(f!!.mMotionStagger)) {
                        f!!.mStaggerScale = 1 / (1 - stagger)
                        if (flip) {
                            f!!.mStaggerOffset = stagger - stagger * (max - f!!.mMotionStagger) / (max - min)
                        } else {
                            f!!.mStaggerOffset = stagger - stagger * (f!!.mMotionStagger - min) / (max - min)
                        }
                    }
                }
            } else {
                for (i in 0 until n) {
                    val f: MotionController? = mFrameArrayList[self.getChildAt(i)]
                    val x: Float = f!!.finalX
                    val y: Float = f.finalY
                    val mdist = if (flip) y - x else y + x
                    f.mStaggerScale = 1 / (1 - stagger)
                    f.mStaggerOffset = stagger - stagger * (mdist - min) / (max - min)
                }
            }
        }
    }

    /**
     * @param touchUpMode     behavior on touch up, can be either:
     *
     *  * TOUCH_UP_COMPLETE (default) : will complete the transition,
     * picking up
     * automatically a correct velocity to do so
     *  * TOUCH_UP_STOP : will allow stopping mid-transition
     *  * TOUCH_UP_DECELERATE : will slowly decay,
     * possibly past the transition (i.e.
     * it will do a hard stop if unmanaged)
     *  * TOUCH_UP_DECELERATE_AND_COMPLETE :
     * will automatically pick between
     * TOUCH_UP_COMPLETE and TOUCH_UP_DECELERATE
     * ,
     * TOUCH_UP_STOP (will allow stopping
     * @param position        animate to given position
     * @param currentVelocity
     */
    fun touchAnimateTo(touchUpMode: Int, position: Float, currentVelocity: Float) {
        var position = position
        if (I_DEBUG) {
            Log.v(
                TAG, " " + Debug.location.toString() + " touchAnimateTo "
                        + position.toString() + "   " + currentVelocity
            )
        }
        if (mScene == null) {
            return
        }
        if (mTransitionLastPosition == position) {
            return
        }
        mTemporalInterpolator = true
        mAnimationStartTime = nanoTime
        mTransitionDuration = mScene!!.duration / 1000f
        targetPosition = position
        mInTransition = true
        when (touchUpMode) {
            TOUCH_UP_COMPLETE, TOUCH_UP_NEVER_TO_START, TOUCH_UP_NEVER_TO_END, TOUCH_UP_COMPLETE_TO_START, TOUCH_UP_COMPLETE_TO_END -> {
                if (touchUpMode == TOUCH_UP_COMPLETE_TO_START
                    || touchUpMode == TOUCH_UP_NEVER_TO_END
                ) {
                    position = 0f
                } else if (touchUpMode == TOUCH_UP_COMPLETE_TO_END
                    || touchUpMode == TOUCH_UP_NEVER_TO_START
                ) {
                    position = 1f
                }
                if (mScene!!.autoCompleteMode
                    == TouchResponse.COMPLETE_MODE_CONTINUOUS_VELOCITY
                ) {
                    mStopLogic.config(
                        mTransitionLastPosition, position, currentVelocity,
                        mTransitionDuration, mScene!!.maxAcceleration,
                        mScene!!.maxVelocity
                    )
                } else {
                    mStopLogic.springConfig(
                        mTransitionLastPosition, position, currentVelocity,
                        mScene!!.springMass,
                        mScene!!.springStiffiness,
                        mScene!!.springDamping,
                        mScene!!.springStopThreshold, mScene!!.springBoundary
                    )
                }
                val currentState = currentState // TODO: remove setProgress(), temporary fix
                targetPosition = position
                this.currentState = currentState
                mInterpolator = mStopLogic
            }
            TOUCH_UP_STOP -> {}
            TOUCH_UP_DECELERATE -> {
                mDecelerateLogic.config(
                    currentVelocity, mTransitionLastPosition,
                    mScene!!.maxAcceleration
                )
                mInterpolator = mDecelerateLogic
            }
            TOUCH_UP_DECELERATE_AND_COMPLETE -> {
                if (willJump(
                        currentVelocity, mTransitionLastPosition,
                        mScene!!.maxAcceleration
                    )
                ) {
                    mDecelerateLogic.config(
                        currentVelocity,
                        mTransitionLastPosition, mScene!!.maxAcceleration
                    )
                    mInterpolator = mDecelerateLogic
                } else {
                    mStopLogic.config(
                        mTransitionLastPosition, position, currentVelocity,
                        mTransitionDuration,
                        mScene!!.maxAcceleration, mScene!!.maxVelocity
                    )
                    this.velocity = 0f
                    val currentState = currentState // TODO: remove setProgress(), (temporary fix)
                    targetPosition = position
                    this.currentState = currentState
                    mInterpolator = mStopLogic
                }
            }
        }
        mTransitionInstantly = false
        mAnimationStartTime = nanoTime
        self.invalidate()
    }

    /**
     * Allows you to use trigger spring motion touch behaviour.
     * You must have configured all the spring parameters in the Transition's OnSwipe
     *
     * @param position the position 0 - 1
     * @param currentVelocity the current velocity rate of change in position per second
     */
    fun touchSpringTo(position: Float, currentVelocity: Float) {
        if (I_DEBUG) {
            Log.v(
                TAG, " " + Debug.location
                    .toString() + " touchAnimateTo " + position.toString() + "   " + currentVelocity
            )
        }
        if (mScene == null) {
            return
        }
        if (mTransitionLastPosition == position) {
            return
        }
        mTemporalInterpolator = true
        mAnimationStartTime = nanoTime
        mTransitionDuration = mScene!!.duration / 1000f
        targetPosition = position
        mInTransition = true
        mStopLogic.springConfig(
            mTransitionLastPosition, position, currentVelocity,
            mScene!!.springMass, mScene!!.springStiffiness, mScene!!.springDamping,
            mScene!!.springStopThreshold, mScene!!.springBoundary
        )
        val currentState = currentState // TODO: remove setProgress(), this is a temporary fix
        targetPosition = position
        this.currentState = currentState
        mInterpolator = mStopLogic
        mTransitionInstantly = false
        mAnimationStartTime = nanoTime
        self.invalidate()
    }

    /**
     * Basic deceleration interpolator
     */
    internal inner class DecelerateInterpolator : MotionInterpolator() {
        var mInitialV = 0f
        var mCurrentP = 0f
        var mMaxA = 0f
        fun config(velocity: Float, position: Float, maxAcceleration: Float) {
            mInitialV = velocity
            mCurrentP = position
            mMaxA = maxAcceleration
        }

        override fun getInterpolation(time: Float): Float {
            var time = time
            return if (mInitialV > 0) {
                if (mInitialV / mMaxA < time) {
                    time = mInitialV / mMaxA
                }
                this.velocity = mInitialV - mMaxA * time
                val pos = mInitialV * time - mMaxA * time * time / 2
                pos + mCurrentP
            } else {
                if (-mInitialV / mMaxA < time) {
                    time = -mInitialV / mMaxA
                }
                this.velocity = mInitialV + mMaxA * time
                val pos = mInitialV * time + mMaxA * time * time / 2
                pos + mCurrentP
            }
        }

        override var velocity: Float = 0f
            get() = mInitialV
    }

    /**
     * @param position animate to given position
     */
    fun animateTo(position: Float) {
        if (I_DEBUG) {
            Log.v(
                TAG, " " + Debug.location.toString() + " ... animateTo(" + position
                    .toString() + ") last:" + mTransitionLastPosition
            )
        }
        if (mScene == null) {
            return
        }
        if (mTransitionLastPosition != mTransitionPosition && mTransitionInstantly) {
            // if we had a call from setProgress() but evaluate() didn't run,
            // the mTransitionLastPosition might not have been updated
            mTransitionLastPosition = mTransitionPosition
        }
        if (mTransitionLastPosition == position) {
            return
        }
        mTemporalInterpolator = false
        val currentPosition = mTransitionLastPosition
        targetPosition = position
        mTransitionDuration = mScene!!.duration / 1000f
        progress = targetPosition
        mInterpolator = null
        mProgressInterpolator = mScene!!.interpolator
        mTransitionInstantly = false
        mAnimationStartTime = nanoTime
        mInTransition = true
        mTransitionPosition = currentPosition
        if (I_DEBUG) {
            Log.v(
                TAG, Debug.location + " mTransitionLastPosition = "
                        + mTransitionLastPosition + " currentPosition =" + currentPosition
            )
        }
        mTransitionLastPosition = currentPosition
        self.invalidate()
    }

    private fun computeCurrentPositions() {
        val n: Int = self.getChildCount()
        for (i in 0 until n) {
            val v: TView = self.getChildAt(i)
            val frame: MotionController = mFrameArrayList[v] ?: continue
            frame.setStartCurrentState(v)
        }
    }

    /**
     * Animate to the starting position of the current transition.
     * This will not work during on create as there is no transition
     * Transitions are only set up during onAttach
     */
    fun transitionToStart() {
        animateTo(0.0f)
    }

    /**
     * Animate to the starting position of the current transition.
     * This will not work during on create as there is no transition
     * Transitions are only set up during onAttach
     *
     * @param onComplete callback when task is done
     */
    fun transitionToStart(onComplete: TRunnable?) {
        animateTo(0.0f)
        mOnComplete = onComplete
    }

    /**
     * Animate to the ending position of the current transition.
     * This will not work during on create as there is no transition
     * Transitions are only set up during onAttach
     */
    fun transitionToEnd() {
        animateTo(1.0f)
        mOnComplete = null
    }

    /**
     * Animate to the ending position of the current transition.
     * This will not work during on create as there is no transition
     * Transitions are only set up during onAttach
     *
     * @param onComplete callback when task is done
     */
    fun transitionToEnd(onComplete: TRunnable?) {
        animateTo(1.0f)
        mOnComplete = onComplete
    }

    /**
     * Animate to the state defined by the id.
     * The id is the id of the ConstraintSet or the id of the State.
     *
     * @param id the state to transition to
     */
    fun transitionToState(id: String) {
        if (!isAttachedToWindow) {
            if (mStateCache == null) {
                mStateCache = StateCache()
            }
            mStateCache!!.setEndState(id)
            return
        }
        transitionToState(id, -1, -1)
    }

    /**
     * Animate to the state defined by the id.
     * The id is the id of the ConstraintSet or the id of the State.
     *
     * @param id       the state to transition to
     * @param duration time in ms. if 0 set by default or transition -1 by current
     */
    fun transitionToState(id: String, duration: Int) {
        if (!isAttachedToWindow) {
            if (mStateCache == null) {
                mStateCache = StateCache()
            }
            mStateCache!!.setEndState(id)
            return
        }
        transitionToState(id, -1, -1, duration)
    }

    /**
     * Rotate the layout based on the angle to a ConstraintSet
     * @param id constraintSet
     * @param duration time to take to rotate
     */
    fun rotateTo(id: String, duration: Int) {
        isInRotation = true
        mPreRotateWidth = self.getWidth()
        mPreRotateHeight = self.getHeight()
        val currentRotation: Int = self.getDisplay().getRotation()
        mRotatMode = if ((currentRotation + 1) % 4 > (mPreviouseRotation + 1) % 4) 1 else 2
        mPreviouseRotation = currentRotation.toFloat()
        val n: Int = self.getChildCount()
        for (i in 0 until n) {
            val v: TView = self.getChildAt(i)
            var bounds = mPreRotate[v]
            if (bounds == null) {
                bounds = ViewState()
                mPreRotate[v] = bounds
            }
            bounds.getState(v)
        }
        mBeginState = UNSET_ID
        endState = id
        mScene!!.setTransition(UNSET_ID, endState)
        mModel.initFrom(mLayoutWidget, null, mScene!!.getConstraintSet(endState))
        mTransitionPosition = 0f
        mTransitionLastPosition = 0f
        self.invalidate()
        transitionToEnd(object : TRunnable {
            override fun run() {
                isInRotation = false
            }
        })
        if (duration > 0) {
            mTransitionDuration = duration / 1000f
        }
    }

    /**
     * This jumps to a state
     * It will be at that state after one repaint cycle
     * If the current transition contains that state.
     * It setsProgress 0 or 1 to that state.
     * If not in the current transition itsl
     *
     * @param id state to set
     */
    fun jumpToState(id: String) {
        if (!isAttachedToWindow) {
            currentState = id
        }
        if (mBeginState == id) {
            progress = 0f
        } else if (endState == id) {
            progress = 1f
        } else {
            setTransition(id, id)
        }
    }
    /**
     * Animate to the state defined by the id.
     * Width and height may be used in the picking of the id using this StateSet.
     *
     * @param id           the state to transition
     * @param screenWidth  the with of the motionLayout used to select the variant
     * @param screenHeight the height of the motionLayout used to select the variant
     * @param duration     time in ms. if 0 set by default or transition -1 by current
     */
    /**
     * Animate to the state defined by the id.
     * Width and height may be used in the picking of the id using this StateSet.
     *
     * @param id           the state to transition
     * @param screenWidth  the with of the motionLayout used to select the variant
     * @param screenHeight the height of the motionLayout used to select the variant
     */
    fun transitionToState(id: String, screenWidth: Int, screenHeight: Int, duration: Int = -1) {
        // if id is either end or start state, transition using current setup.
        // if id is not part of end/start, need to setup

        // if id == end state, just animate
        // ... but check if currentState is unknown. if unknown, call computeCurrentPosition
        // if id != end state
        var id = id
        if (I_DEBUG && mScene!!.mStateSet == null) {
            Log.v(TAG, Debug.location + " mStateSet = null")
        }
        if (mScene != null && mScene!!.mStateSet != null) {
            val tmp_id: String = mScene!!.mStateSet!!.convertToConstraintSet(
                currentState,
                id, screenWidth.toFloat(), screenHeight.toFloat()
            )
            if (tmp_id != UNSET_ID) {
                if (I_DEBUG) {
                    Log.v(
                        TAG, " got state  " + Debug.location.toString() + " lookup("
                                + Debug.getName(self.getContext(), id)
                                + screenWidth.toString() + " , " + screenHeight.toString() + " ) =  "
                                + Debug.getName(self.getContext(), tmp_id)
                    )
                }
                id = tmp_id
            }
        }
        if (currentState == id) {
            return
        }
        if (mBeginState == id) {
            animateTo(0.0f)
            if (duration > 0) {
                mTransitionDuration = duration / 1000f
            }
            return
        }
        if (endState == id) {
            animateTo(1.0f)
            if (duration > 0) {
                mTransitionDuration = duration / 1000f
            }
            return
        }
        endState = id
        if (currentState != UNSET_ID) {
            if (I_DEBUG) {
                Log.v(
                    TAG, " transitionToState " + Debug.location.toString() + " current  = "
                            + Debug.getName(self.getContext(), currentState)
                        .toString() + " to " + Debug.getName(self.getContext(), endState)
                )
                Debug.logStack(TAG, " transitionToState  ", 4)
                Log.v(TAG, "-------------------------------------------")
            }
            setTransition(currentState, id)
            animateTo(1.0f)
            mTransitionLastPosition = 0f
            transitionToEnd()
            if (duration > 0) {
                mTransitionDuration = duration / 1000f
            }
            return
        }
        if (I_DEBUG) {
            Log.v(
                TAG, "setTransition  unknown -> "
                        + Debug.getName(self.getContext(), id)
            )
        }

        // TODO correctly use width & height
        mTemporalInterpolator = false
        targetPosition = 1f
        mTransitionPosition = 0f
        mTransitionLastPosition = 0f
        mTransitionLastTime = nanoTime
        mAnimationStartTime = nanoTime
        mTransitionInstantly = false
        mInterpolator = null
        if (duration == -1) {
            mTransitionDuration = mScene!!.duration / 1000f
        }
        mBeginState = UNSET_ID
        mScene!!.setTransition(mBeginState, endState)
        val controllers: MutableMap<String, MotionController?> = mutableMapOf()
        if (duration == 0) {
            mTransitionDuration = mScene!!.duration / 1000f
        } else if (duration > 0) {
            mTransitionDuration = duration / 1000f
        }
        val n: Int = self.getChildCount()
        mFrameArrayList.clear()
        for (i in 0 until n) {
            val v: TView = self.getChildAt(i)
            val f = MotionController(v)
            mFrameArrayList[v] = f
            controllers[v.getId()] = mFrameArrayList[v]
        }
        mInTransition = true
        mModel.initFrom(mLayoutWidget, null, mScene!!.getConstraintSet(id))
        rebuildScene()
        mModel.build()
        computeCurrentPositions()
        val layoutWidth: Int = self.getWidth()
        val layoutHeight: Int = self.getHeight()
        // getMap the KeyFrames for each view
        if (mDecoratorsHelpers != null) {
            for (i in 0 until n) {
                val motionController: MotionController = mFrameArrayList[self.getChildAt(i)] ?: continue
                mScene!!.getKeyFrames(motionController)
            }
            // Allow helpers to access all the motionControllers after
            for (mDecoratorsHelper in mDecoratorsHelpers!!) {
                mDecoratorsHelper.onPreSetup(this, mFrameArrayList)
            }
            for (i in 0 until n) {
                val motionController: MotionController = mFrameArrayList[self.getChildAt(i)] ?: continue
                motionController.setup(
                    layoutWidth, layoutHeight,
                    mTransitionDuration, nanoTime
                )
            }
        } else {
            for (i in 0 until n) {
                val motionController: MotionController = mFrameArrayList[self.getChildAt(i)] ?: continue
                mScene!!.getKeyFrames(motionController)
                motionController.setup(
                    layoutWidth, layoutHeight,
                    mTransitionDuration, nanoTime
                )
            }
        }
        val stagger: Float = mScene?.staggered ?: 0f
        if (stagger != 0f) {
            var min = Float.MAX_VALUE
            var max = -Float.MAX_VALUE
            for (i in 0 until n) {
                val f: MotionController? = mFrameArrayList[self.getChildAt(i)]
                val x: Float = f!!.finalX
                val y: Float = f.finalY
                min = min(min, y + x)
                max = max(max, y + x)
            }
            for (i in 0 until n) {
                val f: MotionController? = mFrameArrayList[self.getChildAt(i)]
                val x: Float = f!!.finalX
                val y: Float = f.finalY
                f.mStaggerScale = 1 / (1 - stagger)
                f.mStaggerOffset = stagger - stagger * (x + y - min) / (max - min)
            }
        }
        mTransitionPosition = 0f
        mTransitionLastPosition = 0f
        mInTransition = true
        self.invalidate()
    }

    /**
     * Returns the last velocity used in the transition
     *
     * @return
     */
    override var velocity: Float = 0f

    /**
     * Returns the last layout velocity used in the transition
     *
     * @param view           The view
     * @param posOnViewX     The x position on the view
     * @param posOnViewY     The y position on the view
     * @param returnVelocity The velocity
     * @param type           Velocity returned 0 = post layout, 1 = layout, 2 = static postlayout
     */
    fun getViewVelocity(
        view: TView,
        posOnViewX: Float,
        posOnViewY: Float,
        returnVelocity: FloatArray,
        type: Int
    ) {
        var v: Float = this.velocity
        var position = mTransitionLastPosition
        if (mInterpolator != null) {
            val deltaT = EPSILON
            val dir: Float = signum(targetPosition - mTransitionLastPosition)
            var interpolatedPosition: Float =
                mInterpolator!!.getInterpolation(mTransitionLastPosition + deltaT)
            position = mInterpolator!!.getInterpolation(mTransitionLastPosition)
            interpolatedPosition -= position
            interpolatedPosition /= deltaT
            v = dir * interpolatedPosition / mTransitionDuration
        }
        if (mInterpolator is MotionInterpolator) {
            v = (mInterpolator as MotionInterpolator?)?.velocity ?: 0f
        }
        val f: MotionController? = mFrameArrayList[view]
        if (type and 1 == 0) {
            f!!.getPostLayoutDvDp(
                position,
                view.getWidth(), view.getHeight(),
                posOnViewX, posOnViewY, returnVelocity
            )
        } else {
            f!!.getDpDt(position, posOnViewX, posOnViewY, returnVelocity)
        }
        if (type < VELOCITY_STATIC_POST_LAYOUT) {
            returnVelocity[0] *= v
            returnVelocity[1] *= v
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // This contains the logic for interacting with the ConstraintLayout Solver
    inner class Model {
        var mLayoutStart: ConstraintWidgetContainer = ConstraintWidgetContainer()
        var mLayoutEnd: ConstraintWidgetContainer = ConstraintWidgetContainer()
        var mStart: ConstraintSet? = null
        var mEnd: ConstraintSet? = null
        var mStartId = UNSET_ID
        var mEndId = UNSET_ID
        fun copy(src: ConstraintWidgetContainer, dest: ConstraintWidgetContainer) {
            val children = src.children
            val map: MutableMap<ConstraintWidget, ConstraintWidget> = mutableMapOf()
            map[src] = dest
            dest.children.clear()
            dest.copy(src, map)
            for (child_s in children) {
                var child_d: ConstraintWidget
                if (child_s is dev.topping.ios.constraint.core.widgets.Barrier) {
                    child_d = dev.topping.ios.constraint.core.widgets.Barrier()
                } else if (child_s is dev.topping.ios.constraint.core.widgets.Guideline) {
                    child_d = dev.topping.ios.constraint.core.widgets.Guideline()
                } else if (child_s is Flow) {
                    child_d = Flow()
                } else if (child_s is Placeholder2) {
                    child_d = Placeholder2()
                } else if (child_s is dev.topping.ios.constraint.core.widgets.Helper) {
                    child_d = HelperWidget()
                } else {
                    child_d = ConstraintWidget()
                }
                dest.add(child_d)
                map[child_s] = child_d
            }
            for (child_s in children) {
                map[child_s]?.copy(child_s, map)
            }
        }

        fun initFrom(
            baseLayout: ConstraintWidgetContainer?,
            start: ConstraintSet?,
            end: ConstraintSet?
        ) {
            mStart = start
            mEnd = end
            mLayoutStart = ConstraintWidgetContainer()
            mLayoutEnd = ConstraintWidgetContainer()
            mLayoutStart.measurer = mLayoutWidget.measurer
            mLayoutEnd.measurer = mLayoutWidget.measurer
            mLayoutStart.removeAllChildren()
            mLayoutEnd.removeAllChildren()
            copy(mLayoutWidget, mLayoutStart)
            copy(mLayoutWidget, mLayoutEnd)
            if (mTransitionLastPosition > 0.5) {
                if (start != null) {
                    setupConstraintWidget(mLayoutStart, start)
                }
                setupConstraintWidget(mLayoutEnd, end)
            } else {
                setupConstraintWidget(mLayoutEnd, end)
                if (start != null) {
                    setupConstraintWidget(mLayoutStart, start)
                }
            }
            // then init the engine...
            if (I_DEBUG) {
                Log.v(TAG, "> mLayoutStart.updateHierarchy " + Debug.location)
            }
            mLayoutStart.isRtl = self.isRtl()
            mLayoutStart.updateHierarchy()
            if (I_DEBUG) {
                for (child in mLayoutStart.children) {
                    val view: TView = child.getCompanionWidget() as TView
                    debugWidget(">>>>>>>  " + Debug.getName(view), child)
                }
                Log.v(TAG, "> mLayoutEnd.updateHierarchy " + Debug.location)
                Log.v(
                    TAG, ("> mLayoutEnd.updateHierarchy  "
                            + Debug.location) + "  == isRtl()=" + self.isRtl()
                )
            }
            mLayoutEnd.isRtl = self.isRtl()
            mLayoutEnd.updateHierarchy()
            if (I_DEBUG) {
                for (child in mLayoutEnd.children) {
                    val view: TView = child.getCompanionWidget() as TView
                    debugWidget(">>>>>>>  " + Debug.getName(view), child)
                }
            }
            val layoutParams: ViewGroup.LayoutParams? = self.getLayoutParams() as ViewGroup.LayoutParams?
            if (layoutParams != null) {
                if (layoutParams.width == WRAP_CONTENT) {
                    mLayoutStart.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
                    mLayoutEnd.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
                }
                if (layoutParams.height == WRAP_CONTENT) {
                    mLayoutStart.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
                    mLayoutEnd.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
                }
            }
        }

        private fun setupConstraintWidget(base: ConstraintWidgetContainer, cSet: ConstraintSet?) {
            val mapIdToWidget: MutableMap<String, ConstraintWidget> = mutableMapOf()
            val layoutParams: Constraints.LayoutParams = Constraints.LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT
            )
            mapIdToWidget.clear()
            mapIdToWidget[PARENT_ID] = base
            mapIdToWidget[self.getId()] = base
            if (cSet != null && cSet.mRotate != 0) {
                resolveSystem(
                    mLayoutEnd, optimizationLevel,
                    MeasureSpec.makeMeasureSpec(self.getHeight(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(self.getWidth(), MeasureSpec.EXACTLY)
                )
            }
            //  build id widget map
            for (child in base.children) {
                child.setAnimated(true)
                val view: TView = child.getCompanionWidget() as TView
                mapIdToWidget[view.getId()] = child
            }
            for (child in base.children) {
                val view: TView = child.getCompanionWidget() as TView
                cSet?.applyToLayoutParams(view.getId(), layoutParams as ConstraintLayout.LayoutParams)
                child.setWidth(cSet?.getWidth(view.getId()) ?: 0)
                child.setHeight(cSet?.getHeight(view.getId()) ?: 0)
                if (view.getParentType() is ConstraintHelper) {
                    cSet?.applyToHelper(view.getParentType() as ConstraintHelper, child, layoutParams as ConstraintLayout.LayoutParams, mapIdToWidget)
                    if (view.getParentType() is Barrier) {
                        (view.getParentType() as Barrier).validateParams()
                        if (I_DEBUG) {
                            Log.v(
                                TAG, ">>>>>>>>>> Barrier " + Debug.getName(
                                    self.getContext(),
                                    (view.getParentType() as Barrier).referencedIds
                                )
                            )
                        }
                    }
                }
                if (I_DEBUG) {
                    debugLayoutParam(">>>>>>>  " + Debug.getName(view), layoutParams as ConstraintLayout.LayoutParams)
                }

                layoutParams.resolveLayoutDirection(self.getLayoutDirection())

                applyConstraintsFromLayoutParams(false, view, child, layoutParams, mapIdToWidget)
                if (cSet!!.getVisibilityMode(view.getId()) == ConstraintSet.VISIBILITY_MODE_IGNORE) {
                    child.setVisibility(view.getVisibility())
                } else {
                    child.setVisibility(cSet!!.getVisibility(view.getId()))
                }
            }
            for (child in base.children) {
                if (child is VirtualLayout) {
                    val view: ConstraintHelper = child.getCompanionWidget() as ConstraintHelper
                    val helper: Helper = child as Helper
                    view.updatePreLayout(base, helper, mapIdToWidget)
                    val virtualLayout: VirtualLayout = helper as VirtualLayout
                    virtualLayout.captureWidgets()
                }
            }
        }

        private fun debugLayoutParam(str: String, params: LayoutParams) {
            var a = " "
            a += if (params.startToStart != UNSET_ID) "SS" else "__"
            a += if (params.startToEnd != UNSET_ID) "|SE" else "|__"
            a += if (params.endToStart != UNSET_ID) "|ES" else "|__"
            a += if (params.endToEnd != UNSET_ID) "|EE" else "|__"
            a += if (params.leftToLeft != UNSET_ID) "|LL" else "|__"
            a += if (params.leftToRight != UNSET_ID) "|LR" else "|__"
            a += if (params.rightToLeft != UNSET_ID) "|RL" else "|__"
            a += if (params.rightToRight != UNSET_ID) "|RR" else "|__"
            a += if (params.topToTop != UNSET_ID) "|TT" else "|__"
            a += if (params.topToBottom != UNSET_ID) "|TB" else "|__"
            a += if (params.bottomToTop != UNSET_ID) "|BT" else "|__"
            a += if (params.bottomToBottom != UNSET_ID) "|BB" else "|__"
            Log.v(TAG, str + a)
        }

        fun getWidget(container: ConstraintWidgetContainer, view: TView): ConstraintWidget? {
            if (container.getCompanionWidget() == view.getParentType()) {
                return container
            }
            val children: MutableList<ConstraintWidget> = container.children
            val count: Int = children.size
            for (i in 0 until count) {
                val widget: ConstraintWidget = children[i]
                if (widget.getCompanionWidget() == view) {
                    return widget
                }
            }
            return null
        }

        private fun debugWidget(str: String, child: ConstraintWidget) {
            var a = " "
            a += if (child.mTop.target != null) "T" + if (child.mTop.target!!.type == ConstraintAnchor.Type.TOP) "T" else "B" else "__"
            a += if (child.mBottom.target != null) "B" + if (child.mBottom.target!!.type == ConstraintAnchor.Type.TOP) "T" else "B" else "__"
            a += if (child.mLeft.target != null) "L" + if (child.mLeft.target!!.type == ConstraintAnchor.Type.LEFT) "L" else "R" else "__"
            a += if (child.mRight.target != null) "R" + if (child.mRight.target!!.type == ConstraintAnchor.Type.LEFT) "L" else "R" else "__"
            Log.v(TAG, "$str$a ---  $child")
        }

        private fun debugLayout(title: String, c: ConstraintWidgetContainer) {
            var v: TView = c.getCompanionWidget() as TView
            val cName = title + " " + Debug.getName(v)
            Log.v(TAG, "$cName  ========= $c")
            val count: Int = c.children.size
            for (i in 0 until count) {
                val str = "$cName[$i] "
                val child: ConstraintWidget = c.children.get(i)
                var a = ""
                a += if (child.mTop.target != null) "T" else "_"
                a += if (child.mBottom.target != null) "B" else "_"
                a += if (child.mLeft.target != null) "L" else "_"
                a += if (child.mRight.target != null) "R" else "_"
                v = child.getCompanionWidget() as TView
                var name: String = Debug.getName(v)
                Log.v(TAG, "$str  $name $child $a")
            }
            Log.v(TAG, "$cName done. ")
        }

        fun reEvaluateState() {
            measure(mLastWidthMeasureSpec, mLastHeightMeasureSpec)
            setupMotionViews()
        }

        fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val widthMode: Int = MeasureSpec.getMode(widthMeasureSpec)
            val heightMode: Int = MeasureSpec.getMode(heightMeasureSpec)
            mWidthMeasureMode = widthMode
            mHeightMeasureMode = heightMode
            computeStartEndSize(widthMeasureSpec, heightMeasureSpec)

            // This works around the problem that MotionLayout calls its children
            // Wrap content children
            // with measure(AT_MOST,AT_MOST) then measure(EXACTLY, EXACTLY)
            // if a child of MotionLayout is a motionLayout
            // it would not know it could resize during animation
            // other Layouts may have this behaviour but for now this is the only one we support
            var recompute_start_end_size = true
            if (self.getParent()?.getParentType() is MotionLayout
                && widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY
            ) {
                recompute_start_end_size = false
            }
            if (recompute_start_end_size) {
                computeStartEndSize(widthMeasureSpec, heightMeasureSpec)
                mStartWrapWidth = mLayoutStart.getWidth()
                mStartWrapHeight = mLayoutStart.getHeight()
                mEndWrapWidth = mLayoutEnd.getWidth()
                mEndWrapHeight = mLayoutEnd.getHeight()
                mMeasureDuringTransition = (mStartWrapWidth != mEndWrapWidth
                        || mStartWrapHeight != mEndWrapHeight)
            }
            var width = mStartWrapWidth
            var height = mStartWrapHeight
            if (mWidthMeasureMode == MeasureSpec.AT_MOST
                || mWidthMeasureMode == MeasureSpec.UNSPECIFIED
            ) {
                width = (mStartWrapWidth + mPostInterpolationPosition
                        * (mEndWrapWidth - mStartWrapWidth)).toInt()
            }
            if (mHeightMeasureMode == MeasureSpec.AT_MOST
                || mHeightMeasureMode == MeasureSpec.UNSPECIFIED
            ) {
                height = (mStartWrapHeight + mPostInterpolationPosition
                        * (mEndWrapHeight - mStartWrapHeight)).toInt()
            }
            val isWidthMeasuredTooSmall = (mLayoutStart.isWidthMeasuredTooSmall
                    || mLayoutEnd.isWidthMeasuredTooSmall)
            val isHeightMeasuredTooSmall = (mLayoutStart.isHeightMeasuredTooSmall
                    || mLayoutEnd.isHeightMeasuredTooSmall)
            resolveMeasuredDimension(
                widthMeasureSpec, heightMeasureSpec,
                width, height, isWidthMeasuredTooSmall, isHeightMeasuredTooSmall
            )
            if (I_DEBUG) {
                Debug.logStack(TAG, ">>>>>>>>", 3)
                debugLayout(">>>>>>> measure str ", mLayoutStart)
                debugLayout(">>>>>>> measure end ", mLayoutEnd)
            }
        }

        private fun computeStartEndSize(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val optimisationLevel: Int = optimizationLevel
            if (currentState == startState) {
                resolveSystem(
                    mLayoutEnd, optimisationLevel,
                    if (mEnd == null || mEnd!!.mRotate == 0) widthMeasureSpec else heightMeasureSpec,
                    if (mEnd == null || mEnd!!.mRotate == 0) heightMeasureSpec else widthMeasureSpec
                )
                if (mStart != null) {
                    resolveSystem(
                        mLayoutStart, optimisationLevel,
                        if (mStart!!.mRotate == 0) widthMeasureSpec else heightMeasureSpec,
                        if (mStart!!.mRotate == 0) heightMeasureSpec else widthMeasureSpec
                    )
                }
            } else {
                if (mStart != null) {
                    resolveSystem(
                        mLayoutStart, optimisationLevel,
                        if (mStart!!.mRotate == 0) widthMeasureSpec else heightMeasureSpec,
                        if (mStart!!.mRotate == 0) heightMeasureSpec else widthMeasureSpec
                    )
                }
                resolveSystem(
                    mLayoutEnd, optimisationLevel,
                    if (mEnd == null || mEnd!!.mRotate == 0) widthMeasureSpec else heightMeasureSpec,
                    if (mEnd == null || mEnd!!.mRotate == 0) heightMeasureSpec else widthMeasureSpec
                )
            }
        }

        fun build() {
            val n: Int = self.getChildCount()
            mFrameArrayList.clear()
            val controllers: MutableMap<String, MotionController> = mutableMapOf()
            val ids = Array<String>(n) { UNSET_ID }
            for (i in 0 until n) {
                val v: TView = self.getChildAt(i)
                val motionController = MotionController(v)
                controllers[v.getId().also { ids[i] = it }] = motionController
                mFrameArrayList[v] = motionController
            }
            for (i in 0 until n) {
                val v: TView = self.getChildAt(i)
                val motionController: MotionController = mFrameArrayList[v] ?: continue
                if (mStart != null) {
                    val startWidget: ConstraintWidget? = getWidget(mLayoutStart, v)
                    if (startWidget != null) {
                        motionController.setStartState(
                            toRect(startWidget), mStart!!,
                            self.getWidth(), self.getHeight()
                        )
                    } else {
                        if (mDebugPath != 0) {
                            Log.e(
                                TAG, Debug.location + "no widget for  "
                                        + Debug.getName(v) + " (" + v.getClass().getName() + ")"
                            )
                        }
                    }
                } else {
                    if (isInRotation) {
                        motionController.setStartState(
                            mPreRotate[v]!!, v, mRotatMode,
                            mPreRotateWidth, mPreRotateHeight
                        )
                    }
                }
                if (mEnd != null) {
                    val endWidget: ConstraintWidget? = getWidget(mLayoutEnd, v)
                    if (endWidget != null) {
                        motionController.setEndState(
                            toRect(endWidget), mEnd!!,
                            self.getWidth(), self.getHeight()
                        )
                    } else {
                        if (mDebugPath != 0) {
                            Log.e(
                                TAG, Debug.location + "no widget for  "
                                        + Debug.getName(v)
                                        + " (" + v.getClass().getName() + ")"
                            )
                        }
                    }
                }
            }
            for (i in 0 until n) {
                val controller: MotionController = controllers[ids[i]]!!
                val relativeToId = controller.animateRelativeTo
                if (relativeToId != UNSET_ID) {
                    controller.setupRelative(controllers[relativeToId]!!)
                }
            }
        }

        fun setMeasuredId(startId: String, endId: String) {
            mStartId = startId
            mEndId = endId
        }

        fun isNotConfiguredWith(startId: String, endId: String): Boolean {
            return startId != mStartId || endId != mEndId
        }
    }

    var mModel: Model = Model()
    private fun toRect(cw: ConstraintWidget): Rect {
        mTempRect.top = cw.getY()
        mTempRect.left = cw.getX()
        mTempRect.right = cw.getWidth() + mTempRect.left
        mTempRect.bottom = cw.getHeight() + mTempRect.top
        return mTempRect
    }

    override fun requestLayout(sup: TView?) {
        if (!mMeasureDuringTransition) {
            if (currentState == UNSET_ID && mScene != null && mScene!!.mCurrentTransition != null) {
                val mode: Int = mScene!!.mCurrentTransition!!.layoutDuringTransition
                if (mode == MotionScene.LAYOUT_IGNORE_REQUEST) {
                    return
                } else if (mode == MotionScene.LAYOUT_CALL_MEASURE) {
                    val n: Int = self.getChildCount()
                    for (i in 0 until n) {
                        val v: TView = self.getChildAt(i)
                        val motionController: MotionController? = mFrameArrayList[v]
                        motionController!!.remeasure()
                    }
                    return
                }
            }
        }
        sup?.requestLayout()
    }

    override fun toString(): String {
        val context: TContext = self.getContext()
        return (Debug.getName(context, mBeginState) + "->"
                + Debug.getName(context, endState)
                + " (pos:" + mTransitionLastPosition + " Dpos/Dt:" + this.velocity)
    }

    override fun onMeasure(sup: TView?, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (I_DEBUG) {
            Log.v(TAG, "onMeasure " + Debug.location)
        }
        if (mScene == null) {
            super.onMeasure(sup, widthMeasureSpec, heightMeasureSpec)
            return
        }
        var recalc = (mLastWidthMeasureSpec != widthMeasureSpec
                || mLastHeightMeasureSpec != heightMeasureSpec)
        if (mNeedsFireTransitionCompleted) {
            mNeedsFireTransitionCompleted = false
            onNewStateAttachHandlers()
            processTransitionCompleted()
            recalc = true
        }
        if (mDirtyHierarchy) {
            recalc = true
        }
        mLastWidthMeasureSpec = widthMeasureSpec
        mLastHeightMeasureSpec = heightMeasureSpec
        val startId = mScene!!.startId
        val endId = mScene!!.endId
        var setMeasure = true
        if ((recalc || mModel.isNotConfiguredWith(startId, endId)) && mBeginState != UNSET_ID) {
            super.onMeasure(sup, widthMeasureSpec, heightMeasureSpec)
            mModel.initFrom(
                mLayoutWidget, mScene!!.getConstraintSet(startId),
                mScene!!.getConstraintSet(endId)
            )
            mModel.reEvaluateState()
            mModel.setMeasuredId(startId, endId)
            setMeasure = false
        } else if (recalc) {
            super.onMeasure(sup, widthMeasureSpec, heightMeasureSpec)
        }
        if (mMeasureDuringTransition || setMeasure) {
            val heightPadding: Int = self.getPaddingTop() + self.getPaddingBottom()
            val widthPadding: Int = self.getPaddingLeft() + self.getPaddingRight()
            var androidLayoutWidth: Int = mLayoutWidget.getWidth() + widthPadding
            var androidLayoutHeight: Int = mLayoutWidget.getHeight() + heightPadding
            if (mWidthMeasureMode == MeasureSpec.AT_MOST
                || mWidthMeasureMode == MeasureSpec.UNSPECIFIED
            ) {
                androidLayoutWidth = (mStartWrapWidth + mPostInterpolationPosition
                        * (mEndWrapWidth - mStartWrapWidth)).toInt()
                requestLayout(self)
            }
            if (mHeightMeasureMode == MeasureSpec.AT_MOST
                || mHeightMeasureMode == MeasureSpec.UNSPECIFIED
            ) {
                androidLayoutHeight = (mStartWrapHeight + mPostInterpolationPosition
                        * (mEndWrapHeight - mStartWrapHeight)).toInt()
                requestLayout(self)
            }
            self.setMeasuredDimension(androidLayoutWidth, androidLayoutHeight)
        }
        evaluateLayout()
    }

    fun onStartNestedScroll(
        child: TView,
        target: TView,
        axes: Int, type: Int
    ): Boolean {
        if (I_DEBUG) {
            Log.v(
                TAG, "********** onStartNestedScroll( child:" + Debug.getName(child)
                    .toString() + ", target:" + Debug.getName(target)
                    .toString() + ", axis:" + axes.toString() + ", type:" + type
            )
        }
        return if (mScene == null || mScene!!.mCurrentTransition == null || mScene!!.mCurrentTransition!!.touchResponse == null
            || (mScene!!.mCurrentTransition!!.touchResponse!!.flags and TouchResponse.FLAG_DISABLE_SCROLL) != 0
        ) {
            false
        } else true
    }

    fun onNestedScrollAccepted(
        child: TView,
        target: TView,
        axes: Int,
        type: Int
    ) {
        if (I_DEBUG) {
            Log.v(
                TAG, "********** onNestedScrollAccepted( child:" + Debug.getName(child)
                    .toString() + ", target:" + Debug.getName(target)
                    .toString() + ", axis:" + axes.toString() + ", type:" + type
            )
        }
        mScrollTargetTime = nanoTime
        mScrollTargetDT = 0f
        mScrollTargetDX = 0f
        mScrollTargetDY = 0f
    }

    fun onStopNestedScroll(target: TView, type: Int) {
        if (I_DEBUG) {
            Log.v(
                TAG, ("********** onStopNestedScroll(   target:"
                        + Debug.getName(target)) + " , type:" + type.toString() + " "
                        + mScrollTargetDX.toString() + ", " + mScrollTargetDY
            )
            Debug.logStack(TAG, "onStopNestedScroll ", 8)
        }
        if (mScene == null || mScrollTargetDT == 0f) {
            return
        }
        mScene!!.processScrollUp(
            mScrollTargetDX / mScrollTargetDT,
            mScrollTargetDY / mScrollTargetDT
        )
    }

    fun onNestedScroll(
        target: TView,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        if (I_DEBUG) {
            Log.v(
                TAG, "********** onNestedScroll( target:" + Debug.getName(target)
                    .toString() + ", dxConsumed:" + dxConsumed
                    .toString() + ", dyConsumed:" + dyConsumed
                    .toString() + ", dyConsumed:" + dxUnconsumed
                    .toString() + ", dyConsumed:" + dyUnconsumed.toString() + ", type:" + type
            )
        }
    }

    fun onNestedScroll(
        target: TView,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int, consumed: IntArray
    ) {
        if (mUndergoingMotion || dxConsumed != 0 || dyConsumed != 0) {
            consumed[0] += dxUnconsumed
            consumed[1] += dyUnconsumed
        }
        mUndergoingMotion = false
    }

    fun onNestedPreScroll(
        target: TView,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        val scene: MotionScene = mScene ?: return
        val currentTransition: MotionScene.Transition? = scene.mCurrentTransition
        if (currentTransition == null || !currentTransition.isEnabled) {
            return
        }
        if (currentTransition.isEnabled) {
            val touchResponse: TouchResponse? = currentTransition.touchResponse
            if (touchResponse != null) {
                val regionId: String = touchResponse.touchRegionId
                if (regionId != MotionScene.UNSET_ID && target.getId() != regionId) {
                    return
                }
            }
        }
        if (scene.moveWhenScrollAtTop) {
            // This blocks transition during scrolling
            val touchResponse: TouchResponse? = currentTransition.touchResponse
            var vert = -1
            if (touchResponse != null) {
                if (touchResponse!!.flags and TouchResponse.FLAG_SUPPORT_SCROLL_UP !== 0) {
                    vert = dy
                }
            }
            if ((mTransitionPosition == 1f || mTransitionPosition == 0f)
                && target.canScrollVertically(vert)
            ) {
                return
            }
        }

        // This should be disabled in androidx
        if (currentTransition.touchResponse != null
            && (currentTransition.touchResponse!!.flags
                    and TouchResponse.FLAG_DISABLE_POST_SCROLL) != 0
        ) {
            val dir: Float = scene.getProgressDirection(dx.toFloat(), dy.toFloat())
            if (mTransitionLastPosition <= 0.0f && dir < 0
                || mTransitionLastPosition >= 1.0f && dir > 0
            ) {
                return
            }
        }
        if (I_DEBUG) {
            Log.v(
                TAG, ("********** onNestedPreScroll(target:"
                        + Debug.getName(target)) + ", dx:" + dx.toString() + ", dy:" + dy.toString() + ", type:" + type
            )
        }
        val progress = mTransitionPosition
        val time = nanoTime
        mScrollTargetDX = dx.toFloat()
        mScrollTargetDY = dy.toFloat()
        mScrollTargetDT = ((time - mScrollTargetTime) * 1E-9).toFloat()
        mScrollTargetTime = time
        if (I_DEBUG) {
            Log.v(TAG, "********** dy = $dx dy = $dy dt = $mScrollTargetDT")
        }
        scene.processScrollMove(dx.toFloat(), dy.toFloat())
        if (progress != mTransitionPosition) {
            consumed[0] = dx
            consumed[1] = dy
        }
        evaluate(false)
        if (consumed[0] != 0 || consumed[1] != 0) {
            mUndergoingMotion = true
        }
    }

    fun onNestedPreFling(target: TView, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    fun onNestedFling(
        target: TView,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return false
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // Used to draw debugging lines
    ////////////////////////////////////////////////////////////////////////////////////////
    /*inner class DevModeDraw internal constructor() {
        var mPoints: FloatArray?
        var mPathMode: IntArray
        var mKeyFramePoints: FloatArray
        var mPath: Path? = null
        var mPaint: TPaint
        var mPaintKeyframes: TPaint
        var mPaintGraph: TPaint
        var mTextPaint: TPaint
        var mFillPaint: TPaint
        private val mRectangle: FloatArray
        val mRedColor = -0x55cd
        val mKeyframeColor = -0x1f8a66
        val mGraphColor = -0xcc5600
        val mShadowColor = 0x77000000
        val mDiamondSize = 10
        var mDashPathEffect: DashPathEffect
        var mKeyFrameCount = 0
        var mBounds: Rect = Rect()
        var mPresentationMode = false
        var mShadowTranslate = 1

        init {
            mPaint = TPaint()
            mPaint.setAntiAlias(true)
            mPaint.setColor(mRedColor)
            mPaint.setStrokeWidth(2)
            mPaint.setStyle(TPaint.Style.STROKE)
            mPaintKeyframes = TPaint()
            mPaintKeyframes.setAntiAlias(true)
            mPaintKeyframes.setColor(mKeyframeColor)
            mPaintKeyframes.setStrokeWidth(2)
            mPaintKeyframes.setStyle(TPaint.Style.STROKE)
            mPaintGraph = TPaint()
            mPaintGraph.setAntiAlias(true)
            mPaintGraph.setColor(mGraphColor)
            mPaintGraph.setStrokeWidth(2)
            mPaintGraph.setStyle(TPaint.Style.STROKE)
            mTextPaint = TPaint()
            mTextPaint.setAntiAlias(true)
            mTextPaint.setColor(mGraphColor)
            mTextPaint.setTextSize(12 * getContext().getResources().getDisplayMetrics().density)
            mRectangle = FloatArray(8)
            mFillPaint = TPaint()
            mFillPaint.setAntiAlias(true)
            mDashPathEffect = DashPathEffect(floatArrayOf(4f, 8f), 0)
            mPaintGraph.setPathEffect(mDashPathEffect)
            mKeyFramePoints = FloatArray(MAX_KEY_FRAMES * 2)
            mPathMode = IntArray(MAX_KEY_FRAMES)
            if (mPresentationMode) {
                mPaint.setStrokeWidth(8)
                mFillPaint.setStrokeWidth(8)
                mPaintKeyframes.setStrokeWidth(8)
                mShadowTranslate = 4
            }
        }

        fun draw(
            canvas: TCanvas,
            frameArrayList: HashMap<TView?, MotionController>?,
            duration: Int, debugPath: Int
        ) {
            if (frameArrayList == null || frameArrayList.size() === 0) {
                return
            }
            canvas.save()
            if (!isInEditMode() && DEBUG_SHOW_PROGRESS and debugPath == DEBUG_SHOW_PATH) {
                val str: String = (getContext().getResources().getResourceName(endState)
                        + ":" + progress)
                canvas.drawText(str, 10, getHeight() - 30, mTextPaint)
                canvas.drawText(str, 11, getHeight() - 29, mPaint)
            }
            for (motionController in frameArrayList.values()) {
                var mode: Int = motionController.getDrawPath()
                if (debugPath > 0 && mode == MotionController.DRAW_PATH_NONE) {
                    mode = MotionController.DRAW_PATH_BASIC
                }
                if (mode == MotionController.DRAW_PATH_NONE) { // do not draw path
                    continue
                }
                mKeyFrameCount = motionController.buildKeyFrames(mKeyFramePoints, mPathMode)
                if (mode >= MotionController.DRAW_PATH_BASIC) {
                    val frames = duration / Companion.DEBUG_PATH_TICKS_PER_MS
                    if (mPoints == null || mPoints!!.size != frames * 2) {
                        mPoints = FloatArray(frames * 2)
                        mPath = Path()
                    }
                    canvas.translate(mShadowTranslate, mShadowTranslate)
                    mPaint.setColor(mShadowColor)
                    mFillPaint.setColor(mShadowColor)
                    mPaintKeyframes.setColor(mShadowColor)
                    mPaintGraph.setColor(mShadowColor)
                    motionController.buildPath(mPoints!!, frames)
                    drawAll(canvas, mode, mKeyFrameCount, motionController)
                    mPaint.setColor(mRedColor)
                    mPaintKeyframes.setColor(mKeyframeColor)
                    mFillPaint.setColor(mKeyframeColor)
                    mPaintGraph.setColor(mGraphColor)
                    canvas.translate(-mShadowTranslate, -mShadowTranslate)
                    drawAll(canvas, mode, mKeyFrameCount, motionController)
                    if (mode == MotionController.DRAW_PATH_RECTANGLE) {
                        drawRectangle(canvas, motionController)
                    }
                }
            }
            canvas.restore()
        }

        fun drawAll(
            canvas: TCanvas,
            mode: Int,
            keyFrames: Int,
            motionController: MotionController
        ) {
            if (mode == MotionController.DRAW_PATH_AS_CONFIGURED) {
                drawPathAsConfigured(canvas)
            }
            if (mode == MotionController.DRAW_PATH_RELATIVE) {
                drawPathRelative(canvas)
            }
            if (mode == MotionController.DRAW_PATH_CARTESIAN) {
                drawPathCartesian(canvas)
            }
            drawBasicPath(canvas)
            drawTicks(canvas, mode, keyFrames, motionController)
        }

        private fun drawBasicPath(canvas: TCanvas) {
            canvas.drawLines(mPoints, mPaint)
        }

        private fun drawTicks(
            canvas: TCanvas,
            mode: Int,
            keyFrames: Int,
            motionController: MotionController
        ) {
            var viewWidth = 0
            var viewHeight = 0
            if (motionController.mView != null) {
                viewWidth = motionController.mView.getWidth()
                viewHeight = motionController.mView.getHeight()
            }
            for (i in 1 until keyFrames - 1) {
                if (mode == MotionController.DRAW_PATH_AS_CONFIGURED
                    && mPathMode[i - 1] == MotionController.DRAW_PATH_NONE
                ) {
                    continue
                }
                val x = mKeyFramePoints[i * 2]
                val y = mKeyFramePoints[i * 2 + 1]
                mPath.reset()
                mPath.moveTo(x, y + mDiamondSize)
                mPath.lineTo(x + mDiamondSize, y)
                mPath.lineTo(x, y - mDiamondSize)
                mPath.lineTo(x - mDiamondSize, y)
                mPath.close()
                @SuppressWarnings("unused") val framePoint: MotionPaths =
                    motionController.getKeyFrame(i - 1)
                val dx = 0f //framePoint.translationX;
                val dy = 0f //framePoint.translationY;
                if (mode == MotionController.DRAW_PATH_AS_CONFIGURED) {
                    if (mPathMode[i - 1] == MotionPaths.PERPENDICULAR) {
                        drawPathRelativeTicks(canvas, x - dx, y - dy)
                    } else if (mPathMode[i - 1] == MotionPaths.CARTESIAN) {
                        drawPathCartesianTicks(canvas, x - dx, y - dy)
                    } else if (mPathMode[i - 1] == MotionPaths.SCREEN) {
                        drawPathScreenTicks(canvas, x - dx, y - dy, viewWidth, viewHeight)
                    }
                    canvas.drawPath(mPath, mFillPaint)
                }
                if (mode == MotionController.DRAW_PATH_RELATIVE) {
                    drawPathRelativeTicks(canvas, x - dx, y - dy)
                }
                if (mode == MotionController.DRAW_PATH_CARTESIAN) {
                    drawPathCartesianTicks(canvas, x - dx, y - dy)
                }
                if (mode == MotionController.DRAW_PATH_SCREEN) {
                    drawPathScreenTicks(canvas, x - dx, y - dy, viewWidth, viewHeight)
                }
                if (dx != 0f || dy != 0f) {
                    drawTranslation(canvas, x - dx, y - dy, x, y)
                } else {
                    canvas.drawPath(mPath, mFillPaint)
                }
            }
            if (mPoints!!.size > 1) {
                // Draw the starting and ending circle
                canvas.drawCircle(mPoints!![0], mPoints!![1], 8, mPaintKeyframes)
                canvas.drawCircle(
                    mPoints!![mPoints!!.size - 2],
                    mPoints!![mPoints!!.size - 1], 8, mPaintKeyframes
                )
            }
        }

        private fun drawTranslation(canvas: TCanvas, x1: Float, y1: Float, x2: Float, y2: Float) {
            canvas.drawRect(x1, y1, x2, y2, mPaintGraph)
            canvas.drawLine(x1, y1, x2, y2, mPaintGraph)
        }

        private fun drawPathRelative(canvas: TCanvas) {
            canvas.drawLine(
                mPoints!![0], mPoints!![1],
                mPoints!![mPoints!!.size - 2], mPoints!![mPoints!!.size - 1], mPaintGraph
            )
        }

        private fun drawPathAsConfigured(canvas: TCanvas) {
            var path = false
            var cart = false
            for (i in 0 until mKeyFrameCount) {
                if (mPathMode[i] == MotionPaths.PERPENDICULAR) {
                    path = true
                }
                if (mPathMode[i] == MotionPaths.CARTESIAN) {
                    cart = true
                }
            }
            if (path) {
                drawPathRelative(canvas)
            }
            if (cart) {
                drawPathCartesian(canvas)
            }
        }

        private fun drawPathRelativeTicks(canvas: TCanvas, x: Float, y: Float) {
            val x1 = mPoints!![0]
            val y1 = mPoints!![1]
            val x2 = mPoints!![mPoints!!.size - 2]
            val y2 = mPoints!![mPoints!!.size - 1]
            val dist = hypot(x1 - x2, y1 - y2).toFloat()
            val t = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / (dist * dist)
            val xp = x1 + t * (x2 - x1)
            val yp = y1 + t * (y2 - y1)
            val path = Path()
            path.moveTo(x, y)
            path.lineTo(xp, yp)
            val len = hypot(xp - x, yp - y).toFloat()
            val text = "" + (100 * len / dist).toInt() / 100.0f
            getTextBounds(text, mTextPaint)
            val off: Float = len / 2 - mBounds.width() / 2
            canvas.drawTextOnPath(text, path, off, -20, mTextPaint)
            canvas.drawLine(x, y, xp, yp, mPaintGraph)
        }

        fun getTextBounds(text: String, paint: TPaint) {
            paint.getTextBounds(text, 0, text.length(), mBounds)
        }

        private fun drawPathCartesian(canvas: TCanvas) {
            val x1 = mPoints!![0]
            val y1 = mPoints!![1]
            val x2 = mPoints!![mPoints!!.size - 2]
            val y2 = mPoints!![mPoints!!.size - 1]
            canvas.drawLine(
                min(x1, x2), max(y1, y2),
                max(x1, x2), max(y1, y2), mPaintGraph
            )
            canvas.drawLine(
                min(x1, x2), min(y1, y2),
                min(x1, x2), max(y1, y2), mPaintGraph
            )
        }

        private fun drawPathCartesianTicks(canvas: TCanvas, x: Float, y: Float) {
            val x1 = mPoints!![0]
            val y1 = mPoints!![1]
            val x2 = mPoints!![mPoints!!.size - 2]
            val y2 = mPoints!![mPoints!!.size - 1]
            val minx: Float = min(x1, x2)
            val maxy: Float = max(y1, y2)
            val xgap: Float = x - min(x1, x2)
            val ygap: Float = max(y1, y2) - y
            // Horizontal line
            var text = "" + (0.5 + 100 * xgap / abs(x2 - x1)) as Int / 100.0f
            getTextBounds(text, mTextPaint)
            var off: Float = xgap / 2 - mBounds.width() / 2
            canvas.drawText(text, off + minx, y - 20, mTextPaint)
            canvas.drawLine(
                x, y,
                min(x1, x2), y, mPaintGraph
            )

            // Vertical line
            text = "" + (0.5 + 100 * ygap / abs(y2 - y1)) as Int / 100.0f
            getTextBounds(text, mTextPaint)
            off = ygap / 2 - mBounds.height() / 2
            canvas.drawText(text, x + 5, maxy - off, mTextPaint)
            canvas.drawLine(
                x, y,
                x, max(y1, y2), mPaintGraph
            )
        }

        private fun drawPathScreenTicks(
            canvas: TCanvas,
            x: Float,
            y: Float,
            viewWidth: Int,
            viewHeight: Int
        ) {
            val x1 = 0f
            val y1 = 0f
            val x2 = 1f
            val y2 = 1f
            val minx = 0f
            val maxy = 0f
            // Horizontal line
            var text = "" + (0.5 + 100 * (x - viewWidth / 2)
                    / (getWidth() - viewWidth)) as Int / 100.0f
            getTextBounds(text, mTextPaint)
            var off: Float = x / 2 - mBounds.width() / 2
            canvas.drawText(text, off + minx, y - 20, mTextPaint)
            canvas.drawLine(
                x, y,
                min(x1, x2), y, mPaintGraph
            )

            // Vertical line
            text = "" + (0.5 + 100 * (y - viewHeight / 2)
                    / (getHeight() - viewHeight)) as Int / 100.0f
            getTextBounds(text, mTextPaint)
            off = y / 2 - mBounds.height() / 2
            canvas.drawText(text, x + 5, maxy - off, mTextPaint)
            canvas.drawLine(
                x, y,
                x, max(y1, y2), mPaintGraph
            )
        }

        private fun drawRectangle(canvas: TCanvas, motionController: MotionController) {
            mPath.reset()
            val rectFrames = 50
            for (i in 0..rectFrames) {
                val Pointer = i / rectFrames.toFloat()
                motionController.buildRect(Pointer, mRectangle, 0)
                mPath.moveTo(mRectangle[0], mRectangle[1])
                mPath.lineTo(mRectangle[2], mRectangle[3])
                mPath.lineTo(mRectangle[4], mRectangle[5])
                mPath.lineTo(mRectangle[6], mRectangle[7])
                mPath.close()
            }
            mPaint.setColor(0x44000000)
            canvas.translate(2, 2)
            canvas.drawPath(mPath, mPaint)
            canvas.translate(-2, -2)
            mPaint.setColor(-0x10000)
            canvas.drawPath(mPath, mPaint)
        }

        companion object {
            private const val DEBUG_PATH_TICKS_PER_MS = 16
        }
    }
*/
    private fun debugPos() {
        for (i in 0 until self.getChildCount()) {
            val child: TView = self.getChildAt(i)
            Log.v(
                TAG, " " + Debug.location.toString() + " " + Debug.getName(this.self)
                    .toString() + " " + Debug.getName(self.getContext(), currentState)
                    .toString() + " " + Debug.getName(child)
                        + child.getLeft().toString() + " "
                        + child.getTop()
            )
        }
    }

    /**
     * Used to draw debugging graphics and to do post layout changes
     *
     * @param canvas
     */
    override fun dispatchDraw(sup: TView?, canvas: TCanvas) {
        if (I_DEBUG) {
            Log.v(TAG, " dispatchDraw " + progress + Debug.location)
        }
        if (mDecoratorsHelpers != null) {
            for (decor in mDecoratorsHelpers!!) {
                decor.onPreDraw(canvas)
            }
        }
        evaluate(false)
        if (mScene != null && mScene!!.mViewTransitionController != null) {
            mScene!!.mViewTransitionController!!.animate()
        }
        if (I_DEBUG) {
            Log.v(
                TAG, " dispatchDraw" + Debug.location.toString() + " " + Debug.getName(this.self)
                    .toString() + " " + Debug.getName(self.getContext(), currentState)
            )
            debugPos()
        }
        super.dispatchDraw(sup, canvas)
        if (mScene == null) {
            return
        }
        /*if (DEBUG) {
            mDebugPath = 0xFF
        }
        if (mDebugPath and 1 == 1) {
            if (!isInEditMode()) {
                mFrames++
                val currentDrawTime = nanoTime
                if (mLastDrawTime != -1L) {
                    val delay = currentDrawTime - mLastDrawTime
                    if (delay > 200000000) {
                        val fps = mFrames / (delay * 1E-9f)
                        mLastFps = (fps * 100).toInt() / 100.0f
                        mFrames = 0
                        mLastDrawTime = currentDrawTime
                    }
                } else {
                    mLastDrawTime = currentDrawTime
                }
                val paint = TPaint()
                paint.setTextSize(42)
                val Pointer = (progress * 1000).toInt() / 10f
                var str = mLastFps.toString() + " fps " + Debug.getState(this, mBeginState) + " -> "
                str += (Debug.getState(this, endState) + " (progress: " + Pointer + " ) state="
                        + if (currentState == UNSET) "undefined" else Debug.getState(
                    this,
                    currentState
                ))
                paint.setColor(-0x1000000)
                canvas.drawText(str, 11, getHeight() - 29, paint)
                paint.setColor(-0x77ff78)
                canvas.drawText(str, 10, getHeight() - 30, paint)
            }
        }
        if (mDebugPath > 1) {
            if (mDevModeDraw == null) {
                mDevModeDraw = DevModeDraw()
            }
            mDevModeDraw!!.draw(canvas, mFrameArrayList, mScene.getDuration(), mDebugPath)
        }*/
        if (mDecoratorsHelpers != null) {
            for (decor in mDecoratorsHelpers!!) {
                decor.onPostDraw(canvas)
            }
        }
    }

    /**
     * Direct layout evaluation
     */
    private fun evaluateLayout() {
        val dir: Float = signum(targetPosition - mTransitionLastPosition)
        val currentTime = nanoTime
        var deltaPos = 0f
        if (mInterpolator !is StopLogic) { // if we are not in a drag
            deltaPos = dir * (currentTime - mTransitionLastTime) * 1E-9f / mTransitionDuration
        }
        var position = mTransitionLastPosition + deltaPos
        var done = false
        if (mTransitionInstantly) {
            position = targetPosition
        }
        if (dir > 0 && position >= targetPosition
            || dir <= 0 && position <= targetPosition
        ) {
            position = targetPosition
            done = true
        }
        if (mInterpolator != null && !done) {
            position = if (mTemporalInterpolator) {
                val time = (currentTime - mAnimationStartTime) * 1E-9f
                mInterpolator!!.getInterpolation(time)
            } else {
                mInterpolator!!.getInterpolation(position)
            }
        }
        if (dir > 0 && position >= targetPosition
            || dir <= 0 && position <= targetPosition
        ) {
            position = targetPosition
        }
        mPostInterpolationPosition = position
        val n: Int = self.getChildCount()
        val time = nanoTime
        val interPos =
            if (mProgressInterpolator == null) position else mProgressInterpolator!!.getInterpolation(
                position
            )
        for (i in 0 until n) {
            val child: TView = self.getChildAt(i)
            val frame: MotionController? = mFrameArrayList[child]
            if (frame != null) {
                frame!!.interpolate(child, interPos, time, mKeyCache)
            }
        }
        if (mMeasureDuringTransition) {
            requestLayout(self)
        }
    }

    fun endTrigger(start: Boolean) {
        val n: Int = self.getChildCount()
        for (i in 0 until n) {
            val child: TView = self.getChildAt(i)
            val frame: MotionController? = mFrameArrayList[child]
            if (frame != null) {
                frame!!.endTrigger(start)
            }
        }
    }

    fun evaluate(force: Boolean) {
        if (mTransitionLastTime == -1L) {
            mTransitionLastTime = nanoTime
        }
        if (mTransitionLastPosition > 0.0f && mTransitionLastPosition < 1.0f) {
            currentState = UNSET_ID
        }
        var newState = false
        if (mKeepAnimating || (mInTransition
                    && (force || targetPosition != mTransitionLastPosition))
        ) {
            val dir: Float = signum(targetPosition - mTransitionLastPosition)
            val currentTime = nanoTime
            var deltaPos = 0f
            if (mInterpolator !is MotionInterpolator) { // if we are not in a drag
                deltaPos = dir * (currentTime - mTransitionLastTime) * 1E-9f / mTransitionDuration
            }
            var position = mTransitionLastPosition + deltaPos
            var done = false
            if (mTransitionInstantly) {
                position = targetPosition
            }
            if (dir > 0 && position >= targetPosition
                || dir <= 0 && position <= targetPosition
            ) {
                position = targetPosition
                mInTransition = false
                done = true
            }
            if (I_DEBUG) {
                Log.v(
                    TAG, Debug.location + " mTransitionLastPosition = "
                            + mTransitionLastPosition + " position = " + position
                )
            }
            mTransitionLastPosition = position
            mTransitionPosition = position
            mTransitionLastTime = currentTime
            val notStopLogic = 0
            val stopLogicContinue = 1
            val stopLogicStop = 2
            var stopLogicDone = notStopLogic
            if (mInterpolator != null && !done) {
                if (mTemporalInterpolator) {
                    val time = (currentTime - mAnimationStartTime) * 1E-9f
                    position = mInterpolator!!.getInterpolation(time)
                    if (mInterpolator === mStopLogic) {
                        val dp: Boolean = mStopLogic.isStopped
                        stopLogicDone = if (dp) stopLogicStop else stopLogicContinue
                    }
                    if (I_DEBUG) {
                        Log.v(
                            TAG, Debug.location + " mTransitionLastPosition = "
                                    + mTransitionLastPosition + " position = " + position
                        )
                    }
                    mTransitionLastPosition = position
                    mTransitionLastTime = currentTime
                    if (mInterpolator is MotionInterpolator) {
                        val lastVelocity: Float =
                            (mInterpolator as MotionInterpolator).velocity
                        this.velocity = lastVelocity
                        if (abs(lastVelocity) * mTransitionDuration <= EPSILON
                            && stopLogicDone == stopLogicStop
                        ) {
                            mInTransition = false
                        }
                        if (lastVelocity > 0 && position >= 1.0f) {
                            position = 1.0f
                            mTransitionLastPosition = position
                            mInTransition = false
                        }
                        if (lastVelocity < 0 && position <= 0) {
                            position = 0.0f
                            mTransitionLastPosition = position
                            mInTransition = false
                        }
                    }
                } else {
                    var p2 = position
                    position = mInterpolator!!.getInterpolation(position)
                    if (mInterpolator is MotionInterpolator) {
                        this.velocity = (mInterpolator as MotionInterpolator).velocity
                    } else {
                        p2 = mInterpolator!!.getInterpolation(p2 + deltaPos)
                        this.velocity = dir * (p2 - position) / deltaPos
                    }
                }
            } else {
                this.velocity = deltaPos
            }
            if (abs(this.velocity) > EPSILON) {
                setState(TransitionState.MOVING)
            }
            if (stopLogicDone != stopLogicContinue) {
                if (dir > 0 && position >= targetPosition
                    || dir <= 0 && position <= targetPosition
                ) {
                    position = targetPosition
                    mInTransition = false
                }
                if (position >= 1.0f || position <= 0.0f) {
                    mInTransition = false
                    setState(TransitionState.FINISHED)
                }
            }
            val n: Int = self.getChildCount()
            mKeepAnimating = false
            val time = nanoTime
            if (I_DEBUG) {
                Log.v(TAG, "LAYOUT frame.interpolate at $position")
            }
            mPostInterpolationPosition = position
            val interPos =
                if (mProgressInterpolator == null) position else mProgressInterpolator!!.getInterpolation(
                    position
                )
            if (mProgressInterpolator != null) {
                this.velocity = mProgressInterpolator!!
                    .getInterpolation(position + dir / mTransitionDuration)
                this.velocity -= mProgressInterpolator!!.getInterpolation(position)
            }
            for (i in 0 until n) {
                val child: TView = self.getChildAt(i)
                val frame: MotionController? = mFrameArrayList[child]
                if (frame != null) {
                    mKeepAnimating =
                        mKeepAnimating or frame.interpolate(child, interPos, time, mKeyCache)
                }
            }
            if (I_DEBUG) {
                Log.v(
                    TAG,
                    " interpolate " + Debug.location.toString() + " " + Debug.getName(this.self)
                        .toString() + " " + Debug.getName(self.getContext(), mBeginState)
                        .toString() + " " + position
                )
            }
            val end = (dir > 0 && position >= targetPosition
                    || dir <= 0 && position <= targetPosition)
            if (!mKeepAnimating && !mInTransition && end) {
                setState(TransitionState.FINISHED)
            }
            if (mMeasureDuringTransition) {
                requestLayout(self)
            }
            mKeepAnimating = mKeepAnimating or !end

            // If we have hit the begin state begin state could be unset
            if (position <= 0 && mBeginState != UNSET_ID) {
                if (currentState != mBeginState) {
                    newState = true
                    currentState = mBeginState
                    val set: ConstraintSet? = mScene!!.getConstraintSet(mBeginState)
                    set?.applyCustomAttributes(this)
                    setState(TransitionState.FINISHED)
                }
            }
            if (position >= 1.0) {
                if (I_DEBUG) {
                    Log.v(
                        TAG, Debug.loc + " ============= setting  to end "
                                + Debug.getName(self.getContext(), endState) + "  " + position
                    )
                }
                if (currentState != endState) {
                    newState = true
                    currentState = endState
                    val set: ConstraintSet? = mScene!!.getConstraintSet(endState)
                    set?.applyCustomAttributes(this)
                    setState(TransitionState.FINISHED)
                }
            }
            if (mKeepAnimating || mInTransition) {
                self.invalidate()
            } else {
                if (dir > 0 && position == 1f || dir < 0 && position == 0f) {
                    setState(TransitionState.FINISHED)
                }
            }
            if (!mKeepAnimating && !mInTransition && (dir > 0 && position == 1f
                        || dir < 0 && position == 0f)
            ) {
                onNewStateAttachHandlers()
            }
        }
        if (mTransitionLastPosition >= 1.0f) {
            if (currentState != endState) {
                newState = true
            }
            currentState = endState
        } else if (mTransitionLastPosition <= 0.0f) {
            if (currentState != mBeginState) {
                newState = true
            }
            currentState = mBeginState
        }
        mNeedsFireTransitionCompleted = mNeedsFireTransitionCompleted or newState
        if (newState && !mInLayout) {
            requestLayout(self)
        }
        mTransitionPosition = mTransitionLastPosition
    }

    private var mNeedsFireTransitionCompleted = false

    override fun onLayout(
        sup: TView?,
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        mInLayout = true
        try {
            if (I_DEBUG) {
                Log.v(TAG, " onLayout " + progress + "  " + Debug.location)
            }
            if (mScene == null) {
                super.onLayout(sup, changed, left, top, right, bottom)
                return
            }
            val w = right - left
            val h = bottom - top
            if (mLastLayoutWidth != w || mLastLayoutHeight != h) {
                rebuildScene()
                evaluate(true)
                if (I_DEBUG) {
                    Log.v(TAG, " onLayout  rebuildScene  " + Debug.location)
                }
            }
            mLastLayoutWidth = w
            mLastLayoutHeight = h
            mOldWidth = w
            mOldHeight = h
        } finally {
            mInLayout = false
        }
    }

    /**
     * block ConstraintLayout from handling layout description
     *
     * @param id
     */
    override fun parseLayoutDescription(id: String) {
        mConstraintLayoutSpec = null
    }

    /**
     * Get the motion scene of the layout.
     * Warning! This gives you direct access to the internal
     * state of the MotionLayout making it easy
     * corrupt the state.
     * @return the motion scene
     */
    /**
     * Sets a motion scene to the layout. Subsequent calls to it will override the previous scene.
     */
    var scene: MotionScene?
        get() = mScene
        set(scene) {
            mScene = scene
            mScene!!.setRtl(self.isRtl())
            rebuildScene()
        }

    private fun checkStructure() {
        if (mScene == null) {
            Log.e(TAG, "CHECK: motion scene not set! set \"app:layoutDescription=\"@xml/file\"")
            return
        }
        checkStructure(mScene!!.startId, mScene!!.getConstraintSet(mScene!!.startId))
        val startToEnd = mutableMapOf<String, String>()
        val endToStart = mutableMapOf<String, String>()
        for (definedTransition in mScene!!.definedTransitions) {
            if (definedTransition == mScene!!.mCurrentTransition) {
                Log.v(TAG, "CHECK: CURRENT")
            }
            checkStructure(definedTransition)
            val startId = definedTransition.startConstraintSetId
            val endId = definedTransition.endConstraintSetId
            val startString: String = Debug.getName(self.getContext(), startId)
            val endString: String = Debug.getName(self.getContext(), endId)
            if (startToEnd.get(startId) == endId) {
                Log.e(
                    TAG, "CHECK: two transitions with the same start and end "
                            + startString + "->" + endString
                )
            }
            if (endToStart.get(endId) == startId) {
                Log.e(
                    TAG, "CHECK: you can't have reverse transitions"
                            + startString + "->" + endString
                )
            }
            startToEnd.put(startId, endId)
            endToStart.put(endId, startId)
            if (mScene!!.getConstraintSet(startId) == null) {
                Log.e(TAG, " no such constraintSetStart $startString")
            }
            if (mScene!!.getConstraintSet(endId) == null) {
                Log.e(TAG, " no such constraintSetEnd $startString")
            }
        }
    }

    private fun checkStructure(csetId: String, set: ConstraintSet?) {
        val setName: String = Debug.getName(self.getContext(), csetId)
        val size: Int = self.getChildCount()
        for (i in 0 until size) {
            val v: TView = self.getChildAt(i)
            val id = v.getId()
            if (id == "") {
                Log.w(
                    TAG, "CHECK: " + setName + " ALL VIEWS SHOULD HAVE ID's "
                            + v.getClass().getName() + " does not!"
                )
            }
            val c: ConstraintSet.Constraint? = set?.getConstraint(id)
            if (c == null) {
                Log.w(TAG, "CHECK: " + setName + " NO CONSTRAINTS for " + Debug.getName(v))
            }
        }
        val ids = set?.knownIds ?: arrayOf()
        for (i in ids.indices) {
            val id = ids[i]
            val idString: String = Debug.getName(self.getContext(), id)
            if (null == self.findViewById(ids[i])) {
                Log.w(TAG, "CHECK: $setName NO TView matches id $idString")
            }
            if (set?.getHeight(id) == UNSET) {
                Log.w(TAG, "CHECK: $setName($idString) no LAYOUT_HEIGHT")
            }
            if (set?.getWidth(id) == UNSET) {
                Log.w(TAG, "CHECK: $setName($idString) no LAYOUT_HEIGHT")
            }
        }
    }

    private fun checkStructure(transition: MotionScene.Transition?) {
        if (I_DEBUG) {
            Log.v(TAG, "CHECK: transition = " + transition!!.debugString(self.getContext()))
            Log.v(TAG, "CHECK: transition.setDuration = " + transition.duration)
        }
        if (transition?.startConstraintSetId == transition?.endConstraintSetId) {
            Log.e(TAG, "CHECK: start and end constraint set should not be the same!")
        }
    }

    /**
     * TDisplay the debugging information such as paths information
     *
     * @param debugMode integer representing various debug modes
     */
    fun setDebugMode(debugMode: Int) {
        mDebugPath = debugMode
        self.invalidate()
    }

    private val mBoundsCheck: RectF = RectF()
    private var mRegionView: TView? = null
    private var mInverseMatrix: Matrix33? = null
    private fun callTransformedTouchEvent(
        view: TView,
        event: MotionEvent,
        offsetX: Float,
        offsetY: Float
    ): Boolean {
        val viewMatrix: Matrix33 = view.getMatrix()
        if (viewMatrix.isIdentity()) {
            event.offsetLocation(offsetX, offsetY)
            val handled: Boolean = view.onTouchEvent(event)
            event.offsetLocation(-offsetX, -offsetY)
            return handled
        }
        val transformedEvent: MotionEvent = MotionEvent.obtain(event)
        transformedEvent.offsetLocation(offsetX, offsetY)
        mInverseMatrix = viewMatrix.inverse()
        transformedEvent.transform(mInverseMatrix)
        val handled: Boolean = view.onTouchEvent(transformedEvent)
        return handled
    }

    /**
     * Walk the view tree to see if a child view handles a touch event.
     *
     * @param x
     * @param y
     * @param view
     * @param event
     * @return
     */
    private fun handlesTouchEvent(x: Int, y: Int, view: TView, event: MotionEvent): Boolean {
        return handlesTouchEvent(x.toFloat(), y.toFloat(), view, event)
    }
    private fun handlesTouchEvent(x: Float, y: Float, view: TView, event: MotionEvent): Boolean {
        var handled = false
        if (view is TView) {
            val group: TView = view
            val childCount: Int = group.getChildCount()
            for (i in childCount - 1 downTo 0) {
                val child: TView = group.getChildAt(i)
                if (handlesTouchEvent(
                        x + child.getLeft() - view.getScrollX(),
                        y + child.getTop() - view.getScrollY(),
                        child, event
                    )
                ) {
                    handled = true
                    break
                }
            }
        }
        if (!handled) {
            mBoundsCheck.set(
                x, y,
                x + view.getRight() - view.getLeft(),
                y + view.getBottom() - view.getTop()
            )
            if (event.action != MotionEvent.ACTION_DOWN
                || mBoundsCheck.contains(event.x.toInt(), event.y.toInt())
            ) {
                if (callTransformedTouchEvent(view, event, -x, -y)) {
                    handled = true
                }
            }
        }
        return handled
    }

    /**
     * Intercepts the touch event to correctly handle touch region id handover
     *
     * @param event
     * @return
     */
    fun onInterceptTouchEvent(sup:TView?, event: MotionEvent): Boolean {
        if (mScene == null || !isInteractionEnabled) {
            return false
        }
        if (mScene!!.mViewTransitionController != null) {
            mScene!!.mViewTransitionController!!.touchEvent(event)
        }
        val currentTransition: MotionScene.Transition? = mScene!!.mCurrentTransition
        if (currentTransition != null && currentTransition.isEnabled) {
            val touchResponse: TouchResponse? = currentTransition.touchResponse
            if (touchResponse != null) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val region: RectF? = touchResponse!!.getTouchRegion(self, RectF())
                    if (region != null
                        && !region!!.contains(event.x, event.y)
                    ) {
                        return false
                    }
                }
                val regionId: String = touchResponse.touchRegionId
                if (regionId != MotionScene.UNSET_ID) {
                    if (mRegionView == null || mRegionView!!.getId() != regionId) {
                        mRegionView = self.findViewById(regionId)
                    }
                    if (mRegionView != null) {
                        mBoundsCheck.set(
                            mRegionView!!.getLeft(),
                            mRegionView!!.getTop(),
                            mRegionView!!.getRight(),
                            mRegionView!!.getBottom()
                        )
                        if (mBoundsCheck.contains(event.x, event.y)) {
                            // In case of region id, if the view or a child of the view
                            // handles an event we don't need to do anything;
                            if (!handlesTouchEvent(
                                    mRegionView!!.getLeft(), mRegionView!!.getTop(),
                                    mRegionView!!, event
                                )
                            ) {
                                // but if not, then *we* need to handle the event.
                                return onTouchEvent(sup, event)
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    fun onTouchEvent(sup: TView?, event: MotionEvent): Boolean {
        if (I_DEBUG) {
            Log.v(TAG, Debug.location + " onTouchEvent = " + mTransitionLastPosition)
        }
        if (mScene != null && isInteractionEnabled && mScene!!.supportTouch()) {
            val currentTransition: MotionScene.Transition? = mScene!!.mCurrentTransition
            if (currentTransition != null && !currentTransition!!.isEnabled) {
                return sup?.onTouchEvent(event) ?: false
            }
            mScene!!.processTouchEvent(event, currentState, this)
            return if (mScene!!.mCurrentTransition!!.isTransitionFlag(TRANSITION_FLAG_INTERCEPT_TOUCH)) {
                mScene!!.mCurrentTransition!!.touchResponse?.isDragStarted == true
            } else true
        }
        if (I_DEBUG) {
            Log.v(
                TAG, Debug.location + " mTransitionLastPosition = "
                        + mTransitionLastPosition
            )
        }
        return sup?.onTouchEvent(event) ?: false
    }

    open fun onAttachedToWindow(sup: TView?) {
        sup?.onAttachedToWindow()
        val display: TDisplay = self.getDisplay()
        if (display != null) {
            mPreviouseRotation = display.getRotation().toFloat()
        }
        if (mScene != null && currentState != UNSET_ID) {
            val cSet: ConstraintSet? = mScene!!.getConstraintSet(currentState)
            mScene!!.readFallback(this)
            if (mDecoratorsHelpers != null) {
                for (mh in mDecoratorsHelpers!!) {
                    mh.onFinishedMotionScene(this)
                }
            }
            if (cSet != null) {
                cSet!!.applyTo(this)
            }
            mBeginState = currentState
        }
        onNewStateAttachHandlers()
        if (mStateCache != null) {
            if (isDelayedApplicationOfInitialState) {
                self.post(object : TRunnable {
                    override fun run() {
                        mStateCache!!.apply()
                    }
                })
            } else {
                mStateCache!!.apply()
            }
        } else {
            if (mScene != null && mScene!!.mCurrentTransition != null) {
                if (mScene!!.mCurrentTransition!!.autoTransition
                    == MotionScene.Transition.AUTO_ANIMATE_TO_END
                ) {
                    transitionToEnd()
                    setState(TransitionState.SETUP)
                    setState(TransitionState.MOVING)
                }
            }
        }
    }

    //TODO: TView override
    fun onRtlPropertiesChanged(sup:TView?, layoutDirection: Int) {
        if (mScene != null) {
            mScene!!.setRtl(self.isRtl())
        }
    }

    /**
     * This function will set up various handlers (swipe, click...) whenever
     * a new state is reached.
     */
    fun onNewStateAttachHandlers() {
        if (mScene == null) {
            return
        }
        if (mScene!!.autoTransition(this, currentState)) {
            requestLayout(self)
            return
        }
        if (currentState != UNSET_ID) {
            mScene!!.addOnClickListeners(this, currentState)
        }
        if (mScene!!.supportTouch()) {
            mScene!!.setupTouch()
        }
    }
    /**
     * Get current position during an animation.
     *
     * @return current position from 0.0 to 1.0 inclusive
     */// fire a transient moving as jumping end to start// fire a transient moving as jumping start to end
    /**
     * Set the transition position between 0 an 1
     *
     * @param pos the position in the transition from 0...1
     */
    var progress: Float
        get() = mTransitionLastPosition
        set(pos) {
            if (pos < 0.0f || pos > 1.0f) {
                Log.w(TAG, "Warning! Progress is defined for values between 0.0 and 1.0 inclusive")
            }
            if (!isAttachedToWindow) {
                if (mStateCache == null) {
                    mStateCache = StateCache()
                }
                mStateCache!!.setProgress(pos)
                return
            }
            if (I_DEBUG) {
                var str: String = self.getContext().getResources().getResourceName(mBeginState) + " -> "
                str += self.getContext().getResources().getResourceName(endState) + ":" + progress
                Log.v(TAG, Debug.location + " > " + str)
                Debug.logStack(TAG, " Progress = $pos", 3)
            }
            if (pos <= 0f) {
                if (mTransitionLastPosition == 1.0f && currentState == endState) {
                    setState(TransitionState.MOVING) // fire a transient moving as jumping start to end
                }
                currentState = mBeginState
                if (mTransitionLastPosition == 0.0f) {
                    setState(TransitionState.FINISHED)
                }
            } else if (pos >= 1.0f) {
                if (mTransitionLastPosition == 0.0f && currentState == mBeginState) {
                    setState(TransitionState.MOVING) // fire a transient moving as jumping end to start
                }
                currentState = endState
                if (mTransitionLastPosition == 1.0f) {
                    setState(TransitionState.FINISHED)
                }
            } else {
                currentState = UNSET_ID
                setState(TransitionState.MOVING)
            }
            if (mScene == null) {
                return
            }
            mTransitionInstantly = true
            targetPosition = pos
            mTransitionPosition = pos
            mTransitionLastTime = -1
            mAnimationStartTime = -1
            mInterpolator = null
            mInTransition = true
            self.invalidate()
        }

    /**
     * Provide an estimate of the motion with respect to change in transitionPosition
     * (assume you are currently in a transition)
     *
     * @param mTouchAnchorId id of the anchor view that will be "moved" by touch
     * @param pos            the transition position at which to estimate the position
     * @param locationX      the x location within the view (0.0 = left , 1.0 = right)
     * @param locationY      the y location within the view (0.0 = left , 1.0 = right)
     * @param mAnchorDpDt    returns the dx/dp and dy/dp
     */
    fun getAnchorDpDt(
        mTouchAnchorId: String,
        pos: Float,
        locationX: Float, locationY: Float,
        mAnchorDpDt: FloatArray
    ) {
        var v: TView?
        val f: MotionController? = mFrameArrayList[getViewById(mTouchAnchorId).also { v = it }]
        if (I_DEBUG) {
            Log.v(TAG, " getAnchorDpDt " + Debug.getName(v).toString() + " " + Debug.location)
        }
        if (f != null) {
            f.getDpDt(pos, locationX, locationY, mAnchorDpDt)
            val y = v?.getY()?.toFloat() ?: 0f
            val deltaPos = pos - mLastPos
            val deltaY = y - mLastY
            val dydp =
                if (deltaPos != 0.0f) deltaY / deltaPos else Float.NaN
            if (I_DEBUG) {
                Log.v(
                    TAG, " getAnchorDpDt " + Debug.getName(v).toString() + " "
                            + Debug.location.toString() + " " + Arrays.toString(mAnchorDpDt)
                )
            }
            mLastPos = pos
            mLastY = y
        } else {
            val idName = if (v == null) "" + mTouchAnchorId else v!!.getContext().getResources()
                .getResourceName(mTouchAnchorId)
            Log.w(TAG, "WARNING could not find view id $idName")
        }
    }

    /**
     * Gets the time of the currently set animation.
     *
     * @return time in Milliseconds
     */
    val transitionTimeMs: Long
        get() {
            if (mScene != null) {
                mTransitionDuration = mScene!!.duration / 1000f
            }
            return (mTransitionDuration * 1000).toLong()
        }

    /**
     * Set a listener to be notified of drawer events.
     *
     * @param listener Listener to notify when drawer events occur
     * @see TransitionListener
     */
    fun setTransitionListener(listener: TransitionListener?) {
        mTransitionListener = listener
    }

    /**
     * adds a listener to be notified of drawer events.
     *
     * @param listener Listener to notify when drawer events occur
     * @see TransitionListener
     */
    fun addTransitionListener(listener: TransitionListener) {
        if (mTransitionListeners == null) {
            mTransitionListeners = ArrayList()
        }
        mTransitionListeners!!.add(listener)
    }

    /**
     * adds a listener to be notified of drawer events.
     *
     * @param listener Listener to notify when drawer events occur
     * @return <tt>true</tt> if it contained the specified listener
     * @see TransitionListener
     */
    fun removeTransitionListener(listener: TransitionListener): Boolean {
        return if (mTransitionListeners == null) {
            false
        } else mTransitionListeners!!.remove(listener)
    }

    /**
     * Listener for monitoring events about TransitionLayout. **Added in 2.0**
     */
    interface TransitionListener {
        /**
         * Called when a drawer is about to start a transition.
         * Note. startId may be -1 if starting from an "undefined state"
         *
         * @param motionLayout The TransitionLayout view that was moved
         * @param startId      the id of the start state (or ConstraintSet). Will be -1 if unknown.
         * @param endId        the id of the end state (or ConstraintSet).
         */
        fun onTransitionStarted(
            motionLayout: MotionLayout,
            startId: String, endId: String
        )

        /**
         * Called when a drawer's position changes.
         *
         * @param motionLayout The TransitionLayout view that was moved
         * @param startId      the id of the start state (or ConstraintSet). Will be -1 if unknown.
         * @param endId        the id of the end state (or ConstraintSet).
         * @param progress     The progress on this transition, from 0 to 1.
         */
        fun onTransitionChange(
            motionLayout: MotionLayout,
            startId: String, endId: String,
            progress: Float
        )

        /**
         * Called when a drawer has settled completely a state.
         * The TransitionLayout is interactive at this point.
         *
         * @param motionLayout Drawer view that is now open
         * @param currentId    the id it has reached
         */
        fun onTransitionCompleted(motionLayout: MotionLayout, currentId: String)

        /**
         * Call when a trigger is fired
         *
         * @param motionLayout
         * @param triggerId    The id set set with triggerID
         * @param positive     for positive transition edge
         * @param progress
         */
        fun onTransitionTrigger(
            motionLayout: MotionLayout, triggerId: String, positive: Boolean,
            progress: Float
        )
    }

    /**
     * This causes the callback onTransitionTrigger to be called
     *
     * @param triggerId The id set set with triggerID
     * @param positive  for positive transition edge
     * @param progress  the current progress
     */
    fun fireTrigger(triggerId: String, positive: Boolean, progress: Float) {
        if (mTransitionListener != null) {
            mTransitionListener!!.onTransitionTrigger(this, triggerId, positive, progress)
        }
        if (mTransitionListeners != null) {
            for (listeners in mTransitionListeners!!) {
                listeners.onTransitionTrigger(this, triggerId, positive, progress)
            }
        }
    }

    private fun fireTransitionChange() {
        if (mTransitionListener != null
            || mTransitionListeners != null && !mTransitionListeners!!.isEmpty()
        ) {
            if (mListenerPosition != mTransitionPosition) {
                if (mListenerState != UNSET_ID) {
                    fireTransitionStarted()
                    mIsAnimating = true
                }
                mListenerState = UNSET_ID
                mListenerPosition = mTransitionPosition
                if (mTransitionListener != null) {
                    mTransitionListener!!.onTransitionChange(
                        this,
                        mBeginState, endState, mTransitionPosition
                    )
                }
                if (mTransitionListeners != null) {
                    for (listeners in mTransitionListeners!!) {
                        listeners.onTransitionChange(
                            this,
                            mBeginState, endState, mTransitionPosition
                        )
                    }
                }
                mIsAnimating = true
            }
        }
    }

    var mTransitionCompleted: ArrayList<String> = ArrayList()

    /**
     * This causes the callback TransitionCompleted to be called
     */
    protected fun fireTransitionCompleted() {
        if (mTransitionListener != null
            || mTransitionListeners != null && !mTransitionListeners!!.isEmpty()
        ) {
            if (mListenerState == UNSET_ID) {
                mListenerState = currentState
                var lastState = UNSET_ID
                if (!mTransitionCompleted.isEmpty()) {
                    lastState = mTransitionCompleted[mTransitionCompleted.size - 1]
                }
                if (lastState != currentState && currentState != UNSET_ID) {
                    mTransitionCompleted.add(currentState)
                }
            }
        }
        processTransitionCompleted()
        if (mOnComplete != null) {
            mOnComplete!!.run()
            mOnComplete = null
        }
        if (mScheduledTransitionTo != null && mScheduledTransitions > 0) {
            transitionToState(mScheduledTransitionTo!![0])
            mScheduledTransitionTo = Arrays.arraycopy(
                mScheduledTransitionTo!!,
                1, mScheduledTransitionTo!!,
                0, mScheduledTransitionTo!!.size - 1
            )
            mScheduledTransitions--
        }
    }

    private fun processTransitionCompleted() {
        if (mTransitionListener == null
            && (mTransitionListeners == null || mTransitionListeners!!.isEmpty())
        ) {
            return
        }
        mIsAnimating = false
        for (state in mTransitionCompleted) {
            if (mTransitionListener != null) {
                mTransitionListener!!.onTransitionCompleted(this, state)
            }
            if (mTransitionListeners != null) {
                for (listeners in mTransitionListeners!!) {
                    listeners.onTransitionCompleted(this, state)
                }
            }
        }
        mTransitionCompleted.clear()
    }

    /**
     *
     */
    val designTool: DesignTool
        get() {
            if (mDesignTool == null) {
                mDesignTool = DesignTool(this)
            }
            return mDesignTool!!
        }

    /**
     *
     */
    override fun onViewAdded(sup: TView?, view: TView) {
        super.onViewAdded(sup, view)
        if (view.getParentType() is MotionHelper) {
            val helper: MotionHelper = view.getParentType() as MotionHelper
            if (mTransitionListeners == null) {
                mTransitionListeners = ArrayList()
            }
            mTransitionListeners!!.add(helper)
            if (helper.isUsedOnShow) {
                if (mOnShowHelpers == null) {
                    mOnShowHelpers = ArrayList()
                }
                mOnShowHelpers!!.add(helper)
            }
            if (helper.isUseOnHide) {
                if (mOnHideHelpers == null) {
                    mOnHideHelpers = ArrayList()
                }
                mOnHideHelpers!!.add(helper)
            }
            if (helper.isDecorator) {
                if (mDecoratorsHelpers == null) {
                    mDecoratorsHelpers = ArrayList()
                }
                mDecoratorsHelpers!!.add(helper)
            }
        }
    }

    /**
     *
     */
    override fun onViewRemoved(sup: TView?, view: TView) {
        super.onViewRemoved(sup, view)
        if (mOnShowHelpers != null) {
            mOnShowHelpers!!.remove(view.getParentType() as MotionHelper)
        }
        if (mOnHideHelpers != null) {
            mOnHideHelpers!!.remove(view.getParentType() as MotionHelper)
        }
    }

    /**
     * Notify OnShow motion helpers
     * @param progress
     */
    fun setOnShow(progress: Float) {
        if (mOnShowHelpers != null) {
            val count: Int = mOnShowHelpers!!.size
            for (i in 0 until count) {
                val helper: MotionHelper = mOnShowHelpers!![i]
                helper.progress = progress
            }
        }
    }

    /**
     * Notify OnHide motion helpers
     * @param progress
     */
    fun setOnHide(progress: Float) {
        if (mOnHideHelpers != null) {
            val count: Int = mOnHideHelpers!!.size
            for (i in 0 until count) {
                val helper: MotionHelper = mOnHideHelpers!![i]
                helper.progress = progress
            }
        }
    }

    /**
     * Get the id's of all constraintSets used by MotionLayout
     *
     * @return
     */
    val constraintSetIds: Array<String>?
        get() = if (mScene == null) {
            null
        } else mScene!!.constraintSetIds

    /**
     * Get the id's of all constraintSets with the matching types
     *
     * @return
     */
    fun getMatchingConstraintSetIds(vararg types: String): Array<String>? {
        return if (mScene == null) {
            null
        } else mScene!!.getMatchingStateLabels(*types)
    }

    /**
     * Get the ConstraintSet associated with an id
     * This returns a link to the constraintSet
     * But in most cases can be used.
     * createConstraintSet makes a copy which is more expensive.
     *
     * @param id of the constraintSet
     * @return ConstraintSet of MotionLayout
     * @see .cloneConstraintSet
     */
    fun getConstraintSet(id: String): ConstraintSet? {
        return if (mScene == null) {
            null
        } else mScene!!.getConstraintSet(id)
    }

    /**
     * Creates a ConstraintSet based on an existing
     * constraintSet.
     * This makes a copy of the ConstraintSet.
     *
     * @param id The ide of the ConstraintSet
     * @return the ConstraintSet
     */
    fun cloneConstraintSet(id: String): ConstraintSet? {
        if (mScene == null) {
            return null
        }
        val orig: ConstraintSet? = mScene!!.getConstraintSet(id)
        val ret = ConstraintSet()
        ret.clone(orig)
        return ret
    }

    /**
     * rebuild the motion Layouts
     *
     */
    fun rebuildMotion() {
        Log.e(TAG, "This method is deprecated. Please call rebuildScene() instead.")
        rebuildScene()
    }

    /**
     * rebuild the motion Layouts
     */
    fun rebuildScene() {
        mModel.reEvaluateState()
        self.invalidate()
    }

    /**
     * update a ConstraintSet under the id.
     *
     * @param stateId id of the ConstraintSet
     * @param set     The constraintSet
     */
    fun updateState(stateId: String, set: ConstraintSet?) {
        if (mScene != null) {
            mScene!!.setConstraintSet(stateId, set)
        }
        updateState()
        if (currentState == stateId) {
            set?.applyTo(this)
        }
    }

    /**
     * Update a ConstraintSet but animate the change.
     *
     * @param stateId  id of the ConstraintSet
     * @param set      The constraintSet
     * @param duration The length of time to perform the animation
     */
    fun updateStateAnimate(stateId: String, set: ConstraintSet?, duration: Int) {
        if (mScene == null) {
            return
        }
        if (currentState == stateId) {
            //TODO:?
            updateState("R.id.view_transition", getConstraintSet(stateId))
            setState("R.id.view_transition", -1, -1)
            updateState(stateId, set)
            val tmpTransition: MotionScene.Transition =
                MotionScene.Transition(UNSET_ID, mScene!!, "R.id.view_transition", stateId)
            tmpTransition.duration = duration
            setTransition(tmpTransition)
            transitionToEnd()
        }
    }

    /**
     * on completing the current transition, transition to this state.
     *
     * @param id
     */
    fun scheduleTransitionTo(id: String) {
        if (currentState == UNSET_ID) {
            transitionToState(id)
        } else {
            if (mScheduledTransitionTo == null) {
                mScheduledTransitionTo = Array(4) { UNSET_ID }
            } else if (mScheduledTransitionTo!!.size <= mScheduledTransitions) {
                val a = Array(mScheduledTransitionTo!!.size * 2) { UNSET_ID }
                for(i in mScheduledTransitionTo!!.indices) {
                    a[i] = mScheduledTransitionTo!![i]
                }
                mScheduledTransitionTo = a
            }
            mScheduledTransitionTo!![mScheduledTransitions++] = id
        }
    }

    /**
     * Not sure we want this
     *
     *
     */
    fun updateState() {
        mModel.initFrom(
            mLayoutWidget,
            mScene!!.getConstraintSet(mBeginState),
            mScene!!.getConstraintSet(endState)
        )
        rebuildScene()
    }

    /**
     * Get all Transitions known to the system.
     *
     * @return
     */
    val definedTransitions: MutableList<MotionScene.Transition>?
        get() = if (mScene == null) {
            null
        } else mScene!!.definedTransitions
    /**
     * Gets the state you are currently transitioning from.
     * If you are transitioning from an unknown state returns -1
     *
     * @return State you are transitioning from.
     */
    /**
     * sets the state to start in. To be used during OnCreate
     *
     * @param beginId the id of the start constraint set
     */
    var startState: String
        get() = mBeginState
        set(beginId) {
            if (!isAttachedToWindow) {
                if (mStateCache == null) {
                    mStateCache = StateCache()
                }
                mStateCache!!.setStartState(beginId)
                mStateCache!!.setEndState(beginId)
                return
            }
            currentState = beginId
        }

    /**
     * Change the current Transition duration.
     *
     * @param milliseconds duration for transition to complete
     */
    fun setTransitionDuration(milliseconds: Int) {
        if (mScene == null) {
            Log.e(TAG, "MotionScene not defined")
            return
        }
        mScene!!.duration = milliseconds
    }

    /**
     * This returns the internal Transition Structure
     *
     * @param id
     * @return
     */
    fun getTransition(id: String): MotionScene.Transition? {
        return mScene!!.getTransitionById(id)
    }

    /**
     * This looks up the constraintset ID given an id string (
     *
     * @param id String id (without the "@+id/")
     * @return the integer id of the string
     */
    fun lookUpConstraintId(id: String): String {
        return if (mScene == null) {
            UNSET_ID
        } else mScene!!.lookUpConstraintId(id)
    }

    /**
     * does a revers look up to find the ConstraintSets Name
     *
     * @param id the integer id of the constraintSet
     * @return
     */
    fun getConstraintSetNames(id: String): String? {
        return if (mScene == null) {
            null
        } else mScene!!.lookUpConstraintName(id)
    }

    /**
     * this allow disabling autoTransitions to prevent design surface from being in undefined states
     *
     * @param disable
     */
    fun disableAutoTransition(disable: Boolean) {
        if (mScene == null) {
            return
        }
        mScene!!.disableAutoTransition(disable)
    }

    private fun fireTransitionStarted() {
        if (mTransitionListener != null) {
            mTransitionListener!!.onTransitionStarted(this, mBeginState, endState)
        }
        if (mTransitionListeners != null) {
            for (listeners in mTransitionListeners!!) {
                listeners.onTransitionStarted(this, mBeginState, endState)
            }
        }
    }

    /**
     * Execute a ViewTransition.
     * Transition will execute if its conditions are met and it is enabled
     *
     * @param viewTransitionId
     * @param view             The views to apply to
     */
    fun viewTransition(viewTransitionId: String, vararg view: TView?) {
        if (mScene != null) {
            mScene!!.viewTransition(viewTransitionId, *view)
        } else {
            Log.e(TAG, " no motionScene")
        }
    }

    /**
     * Enable a ViewTransition ID.
     *
     * @param viewTransitionId id of ViewTransition
     * @param enable           If false view transition cannot be executed.
     */
    fun enableViewTransition(viewTransitionId: String, enable: Boolean) {
        if (mScene != null) {
            mScene!!.enableViewTransition(viewTransitionId, enable)
        }
    }

    /**
     * Is transition id enabled or disabled
     *
     * @param viewTransitionId the ide of the transition
     * @return true if enabled
     */
    fun isViewTransitionEnabled(viewTransitionId: String): Boolean {
        return if (mScene != null) {
            mScene!!.isViewTransitionEnabled(viewTransitionId)
        } else false
    }

    /**
     * Apply the view transitions keyFrames to the MotionController.
     * Note ConstraintOverride is not used
     *
     * @param viewTransitionId the id of the view transition
     * @param motionController the MotionController to apply the keyframes to
     * @return true if it found and applied the viewTransition false otherwise
     */
    fun applyViewTransition(viewTransitionId: String, motionController: MotionController): Boolean {
        return if (mScene != null) {
            mScene!!.applyViewTransition(viewTransitionId, motionController)
        } else false
    }

    companion object {
        const val TOUCH_UP_COMPLETE = 0
        const val TOUCH_UP_COMPLETE_TO_START = 1
        const val TOUCH_UP_COMPLETE_TO_END = 2
        const val TOUCH_UP_STOP = 3
        const val TOUCH_UP_DECELERATE = 4
        const val TOUCH_UP_DECELERATE_AND_COMPLETE = 5
        const val TOUCH_UP_NEVER_TO_START = 6
        const val TOUCH_UP_NEVER_TO_END = 7
        const val TAG = "MotionLayout"
        private const val I_DEBUG = false
        var IS_IN_EDIT_MODE = false
        const val DEBUG_SHOW_NONE = 0
        const val DEBUG_SHOW_PROGRESS = 1
        const val DEBUG_SHOW_PATH = 2

        // variable used in painting the debug
        const val MAX_KEY_FRAMES = 50
        const val VELOCITY_POST_LAYOUT = 0
        const val VELOCITY_LAYOUT = 1
        const val VELOCITY_STATIC_POST_LAYOUT = 2
        const val VELOCITY_STATIC_LAYOUT = 3
        private const val EPSILON = 0.00001f
        private fun willJump(
            velocity: Float,
            position: Float,
            maxAcceleration: Float
        ): Boolean {
            return if (velocity > 0) {
                val time = velocity / maxAcceleration
                val pos = velocity * time - maxAcceleration * time * time / 2
                position + pos > 1
            } else {
                val time = -velocity / maxAcceleration
                val pos = velocity * time + maxAcceleration * time * time / 2
                position + pos < 0
            }
        }
    }
}