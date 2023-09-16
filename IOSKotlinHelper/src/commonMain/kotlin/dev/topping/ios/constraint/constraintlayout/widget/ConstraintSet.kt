/*
 * Copyright (C) 2016 The Android Open Source Project
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
import dev.topping.ios.constraint.constraintlayout.motion.widget.Debug
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionLayout
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionScene
import dev.topping.ios.constraint.core.motion.utils.Easing
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.HelperWidget
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.core.impl.multiplatform.Writer
import nl.adaptivity.xmlutil.localPart
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * This class allows you to define programmatically a set of constraints to be used with
 * [ConstraintLayout].
 *
 *
 * For details about Constraint behaviour see [ConstraintLayout].
 * It lets you create and save constraints, and apply them to an existing ConstraintLayout.
 * ConstraintsSet can be created in various ways:
 *
 *  *
 * Manually <br></br> `c = new ConstraintSet(); c.connect(....);`
 *
 *  *
 * from a R.layout.* object <br></br> `c.clone(context, R.layout.layout1);`
 *
 *  *
 * from a ConstraintLayout <br></br> `c.clone(constraintLayout);`
 *
 *
 *
 *
 * Example code:
 * <pre>
 * import android.content.TContext;
 * import android.os.Bundle;
 * import android.support.constraint.ConstraintLayout;
 * import android.support.constraint.ConstraintSet;
 * import android.support.transition.TransitionManager;
 * import android.support.v7.app.AppCompatActivity;
 * import android.view.TView;
 *
 * public class MainActivity extends AppCompatActivity {
 * ConstraintSet mConstraintSet1 = new ConstraintSet(); // create a Constraint Set
 * ConstraintSet mConstraintSet2 = new ConstraintSet(); // create a Constraint Set
 * ConstraintLayout mConstraintLayout; // cache the ConstraintLayout
 * boolean mOld = true;
 *
 *
 * protected void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * TContext context = this;
 * mConstraintSet2.clone(context, R.layout.state2); // get constraints from layout
 * setContentView(R.layout.state1);
 * mConstraintLayout = (ConstraintLayout) findViewById(R.id.activity_main);
 * mConstraintSet1.clone(mConstraintLayout); // get constraints from ConstraintSet
 * }
 *
 * public void foo(TView view) {
 * TransitionManager.beginDelayedTransition(mConstraintLayout);
 * if (mOld = !mOld) {
 * mConstraintSet1.applyTo(mConstraintLayout); // set new constraints
 * }  else {
 * mConstraintSet2.applyTo(mConstraintLayout); // set new constraints
 * }
 * }
 * }
 * <pre></pre>
 *
 *
</pre> */
class ConstraintSet {
    /**
     * If true perform validation checks when parsing ConstraintSets
     * This will slow down parsing and should only be used for debugging
     *
     * @return validate
     */
    /**
     * If true perform validation checks when parsing ConstraintSets
     * This will slow down parsing and should only be used for debugging
     *
     * @param validate
     */
    var isValidateOnParse = false
    var mIdString: String = UNSET_ID
    var derivedState = ""
    private var mMatchLabels = arrayOfNulls<String>(0)
    var mRotate = 0
    private val mSavedAttributes: MutableMap<String, ConstraintAttribute> = mutableMapOf()
    /**
     * Enforce id are required for all ConstraintLayout children to use ConstraintSet.
     * default = true;
     */
    /**
     * Enforce id are required for all ConstraintLayout children to use ConstraintSet.
     * default = true;
     *
     * @param forceId
     */
    /**
     * require that all views have IDs to function
     */
    var isForceId = true
    private val mConstraints: MutableMap<String, Constraint> = mutableMapOf()
    val customAttributeSet: MutableMap<String, ConstraintAttribute>
        get() = mSavedAttributes
    // @TODO: add description
    /**
     *
     * @param mId
     * @return
     */
    fun getParameters(mId: String): Constraint {
        return get(mId)
    }

    /**
     * This will copy Constraints from the ConstraintSet
     *
     * @param set
     */
    fun readFallback(set: ConstraintSet) {
        for (key in set.mConstraints.keys) {
            val id = key
            val parent = set.mConstraints[key]
            if (!mConstraints.containsKey(id)) {
                mConstraints[id] = Constraint()
            }
            val constraint = mConstraints[id] ?: continue
            if (!constraint.layout.mApply) {
                constraint.layout.copyFrom(parent!!.layout)
            }
            if (!constraint.propertySet.mApply) {
                constraint.propertySet.copyFrom(parent!!.propertySet)
            }
            if (!constraint.transform.mApply) {
                constraint.transform.copyFrom(parent!!.transform)
            }
            if (!constraint.motion.mApply) {
                constraint.motion.copyFrom(parent!!.motion)
            }
            for (s in parent!!.mCustomConstraints.keys) {
                if (!constraint.mCustomConstraints.containsKey(s)) {
                    constraint.mCustomConstraints[s] = parent.mCustomConstraints[s]!!
                }
            }
        }
    }

    /**
     * This will copy Constraints from the ConstraintLayout if it does not have parameters
     *
     * @param constraintLayout
     */
    fun readFallback(constraintLayout: ConstraintLayout) {
        val count: Int = constraintLayout.self.getChildCount()
        for (i in 0 until count) {
            val view: TView = constraintLayout.self.getChildAt(i)
            val id = view.getId()
            if (isForceId && id == UNSET_ID) {
                throw RuntimeException(
                    "All children of ConstraintLayout "
                            + "must have ids to use ConstraintSet"
                )
            }
            if (!mConstraints.containsKey(id)) {
                mConstraints[id] = Constraint()
            }
            val constraint = mConstraints[id] ?: continue
            if (!constraint.layout.mApply) {
                constraint.fillFrom(id, view.getLayoutParams()!! as ConstraintLayout.LayoutParams)
                if (view.getParentType() is ConstraintHelper) {
                    constraint.layout.mReferenceIds = (view.getParentType() as ConstraintHelper).referencedIds
                    if (view.getParentType() is Barrier) {
                        val barrier: Barrier = view.getParentType() as Barrier
                        constraint.layout.mBarrierAllowsGoneWidgets = barrier.allowsGoneWidget
                        constraint.layout.mBarrierDirection = barrier.type
                        constraint.layout.mBarrierMargin = barrier.margin
                    }
                }
                constraint.layout.mApply = true
            }
            if (!constraint.propertySet.mApply) {
                constraint.propertySet.visibility = view.getVisibility()
                constraint.propertySet.alpha = view.getAlpha()
                constraint.propertySet.mApply = true
            }

            if (!constraint.transform.mApply) {
                constraint.transform.mApply = true
                constraint.transform.rotation = view.getRotation()
                constraint.transform.rotationX = view.getRotationX()
                constraint.transform.rotationY = view.getRotationY()
                constraint.transform.scaleX = view.getScaleX()
                constraint.transform.scaleY = view.getScaleY()
                val pivotX: Float = view.getPivotX() // we assume it is not set if set to 0.0
                val pivotY: Float = view.getPivotY() // we assume it is not set if set to 0.0
                if (pivotX.toDouble() != 0.0 || pivotY.toDouble() != 0.0) {
                    constraint.transform.transformPivotX = pivotX
                    constraint.transform.transformPivotY = pivotY
                }
                constraint.transform.translationX = view.getTranslationX()
                constraint.transform.translationY = view.getTranslationY()
                constraint.transform.translationZ = view.getTranslationZ()
                if (constraint.transform.applyElevation) {
                    constraint.transform.elevation = view.getElevation()
                }
            }
        }
    }

    /**
     * Get the delta form the ConstraintSet and aplly to this
     * @param cs
     */
    fun applyDeltaFrom(cs: ConstraintSet) {
        for (from in cs.mConstraints.values) {
            if (from.mDelta == null) {
                continue
            }
            if (from.mTargetString == null) {
                val constraint = getConstraint(from.mViewId)
                from.mDelta!!.applyDelta(constraint)
                continue
            }
            for (key in mConstraints.keys) {
                val potential = getConstraint(key)
                if (potential!!.layout.mConstraintTag == null) {
                    continue
                }
                if (from.mTargetString!!.matches(potential.layout.mConstraintTag!!.toRegex())) {
                    from.mDelta!!.applyDelta(potential)
                    potential.mCustomConstraints.putAll(from.mCustomConstraints.toMap())
                }
            }
        }
    }

    /**
     * Get the types associated with this ConstraintSet
     * The types mechanism allows you to tag the constraint set
     * with a series of string to define properties of a ConstraintSet
     *
     * @return an array of type strings
     */
    val stateLabels: Array<String?>
        get() = Arrays.copyOfNonNull(mMatchLabels, mMatchLabels.size)

    /**
     * Set the types associated with this ConstraintSet
     * The types mechanism allows you to tag the constraint set
     * with a series of string to define properties of a ConstraintSet
     * @param types a comer separated array of strings.
     */
    fun setStateLabels(types: String) {
        mMatchLabels = types.split(",").toTypedArray()
        for (i in mMatchLabels.indices) {
            mMatchLabels[i] = mMatchLabels[i]!!.trim()
        }
    }

    /**
     * Set the types associated with this ConstraintSet
     * The types mechanism allows you to tag the constraint set
     * with a series of string to define properties of a ConstraintSet
     * @param types a comer separated array of strings.
     */
    fun setStateLabelsList(vararg types: String) {
        val a = mutableListOf<String>()
        types.forEach {
            a.add(it)
        }
        mMatchLabels = a.toTypedArray()
        for (i in mMatchLabels.indices) {
            mMatchLabels[i] = mMatchLabels[i]!!.trim()
        }
    }

    /**
     * Test if the list of strings matches labels defined on this constraintSet
     * @param types list of types
     * @return true if all types are in the labels
     */
    fun matchesLabels(vararg types: String): Boolean {
        for (type in types) {
            var match = false
            for (matchType in mMatchLabels) {
                if (matchType.equals(type)) {
                    match = true
                    break
                }
            }
            if (!match) {
                return false
            }
        }
        return true
    }

    /**
     *
     */
    class Layout {
        var mIsGuideline = false
        var mApply = false
        var mOverride = false
        var mWidth = 0
        var mHeight = 0
        var guideBegin = UNSET
        var guideEnd = UNSET
        var guidePercent = UNSET.toFloat()
        var guidelineUseRtl = true
        var leftToLeft = UNSET_ID
        var leftToRight = UNSET_ID
        var rightToLeft = UNSET_ID
        var rightToRight = UNSET_ID
        var topToTop = UNSET_ID
        var topToBottom = UNSET_ID
        var bottomToTop = UNSET_ID
        var bottomToBottom = UNSET_ID
        var baselineToBaseline = UNSET_ID
        var baselineToTop = UNSET_ID
        var baselineToBottom = UNSET_ID
        var startToEnd = UNSET_ID
        var startToStart = UNSET_ID
        var endToStart = UNSET_ID
        var endToEnd = UNSET_ID
        var horizontalBias = 0.5f
        var verticalBias = 0.5f
        var dimensionRatio: String? = null
        var circleConstraint = UNSET_ID
        var circleRadius = 0
        var circleAngle = 0f
        var editorAbsoluteX = UNSET
        var editorAbsoluteY = UNSET
        var orientation = UNSET
        var leftMargin = 0
        var rightMargin = 0
        var topMargin = 0
        var bottomMargin = 0
        var endMargin = 0
        var startMargin = 0
        var baselineMargin = 0
        var goneLeftMargin = UNSET_GONE_MARGIN
        var goneTopMargin = UNSET_GONE_MARGIN
        var goneRightMargin = UNSET_GONE_MARGIN
        var goneBottomMargin = UNSET_GONE_MARGIN
        var goneEndMargin = UNSET_GONE_MARGIN
        var goneStartMargin = UNSET_GONE_MARGIN
        var goneBaselineMargin = UNSET_GONE_MARGIN
        var verticalWeight = UNSET.toFloat()
        var horizontalWeight = UNSET.toFloat()
        var horizontalChainStyle = CHAIN_SPREAD
        var verticalChainStyle = CHAIN_SPREAD
        var widthDefault: Int = ConstraintWidget.MATCH_CONSTRAINT_SPREAD
        var heightDefault: Int = ConstraintWidget.MATCH_CONSTRAINT_SPREAD
        var widthMax = 0
        var heightMax = 0
        var widthMin = 0
        var heightMin = 0
        var widthPercent = 1f
        var heightPercent = 1f
        var mBarrierDirection = UNSET
        var mBarrierMargin = 0
        var mHelperType = UNSET
        var mReferenceIds: Array<String>? = null
        var mReferenceIdString: String? = null
        var mConstraintTag: String? = null
        var constrainedWidth = false
        var constrainedHeight = false

        // TODO public boolean mChainUseRtl = false;
        var mBarrierAllowsGoneWidgets = true
        var mWrapBehavior: Int = ConstraintWidget.WRAP_BEHAVIOR_INCLUDED

        /**
         * Copy from Layout
         * @param src
         */
        fun copyFrom(src: Layout) {
            mIsGuideline = src.mIsGuideline
            mWidth = src.mWidth
            mApply = src.mApply
            mHeight = src.mHeight
            guideBegin = src.guideBegin
            guideEnd = src.guideEnd
            guidePercent = src.guidePercent
            guidelineUseRtl = src.guidelineUseRtl
            leftToLeft = src.leftToLeft
            leftToRight = src.leftToRight
            rightToLeft = src.rightToLeft
            rightToRight = src.rightToRight
            topToTop = src.topToTop
            topToBottom = src.topToBottom
            bottomToTop = src.bottomToTop
            bottomToBottom = src.bottomToBottom
            baselineToBaseline = src.baselineToBaseline
            baselineToTop = src.baselineToTop
            baselineToBottom = src.baselineToBottom
            startToEnd = src.startToEnd
            startToStart = src.startToStart
            endToStart = src.endToStart
            endToEnd = src.endToEnd
            horizontalBias = src.horizontalBias
            verticalBias = src.verticalBias
            dimensionRatio = src.dimensionRatio
            circleConstraint = src.circleConstraint
            circleRadius = src.circleRadius
            circleAngle = src.circleAngle
            editorAbsoluteX = src.editorAbsoluteX
            editorAbsoluteY = src.editorAbsoluteY
            orientation = src.orientation
            leftMargin = src.leftMargin
            rightMargin = src.rightMargin
            topMargin = src.topMargin
            bottomMargin = src.bottomMargin
            endMargin = src.endMargin
            startMargin = src.startMargin
            baselineMargin = src.baselineMargin
            goneLeftMargin = src.goneLeftMargin
            goneTopMargin = src.goneTopMargin
            goneRightMargin = src.goneRightMargin
            goneBottomMargin = src.goneBottomMargin
            goneEndMargin = src.goneEndMargin
            goneStartMargin = src.goneStartMargin
            goneBaselineMargin = src.goneBaselineMargin
            verticalWeight = src.verticalWeight
            horizontalWeight = src.horizontalWeight
            horizontalChainStyle = src.horizontalChainStyle
            verticalChainStyle = src.verticalChainStyle
            widthDefault = src.widthDefault
            heightDefault = src.heightDefault
            widthMax = src.widthMax
            heightMax = src.heightMax
            widthMin = src.widthMin
            heightMin = src.heightMin
            widthPercent = src.widthPercent
            heightPercent = src.heightPercent
            mBarrierDirection = src.mBarrierDirection
            mBarrierMargin = src.mBarrierMargin
            mHelperType = src.mHelperType
            mConstraintTag = src.mConstraintTag
            mReferenceIds = if (src.mReferenceIds != null && src.mReferenceIdString == null) {
                Arrays.copyOfNonNull(src.mReferenceIds!!, src.mReferenceIds!!.size)
            } else {
                null
            }
            mReferenceIdString = src.mReferenceIdString
            constrainedWidth = src.constrainedWidth
            constrainedHeight = src.constrainedHeight
            // TODO mChainUseRtl = t.mChainUseRtl;
            mBarrierAllowsGoneWidgets = src.mBarrierAllowsGoneWidgets
            mWrapBehavior = src.mWrapBehavior
        }

        fun fillFromAttributeList(context: TContext, attrs: AttributeSet) {
            mApply = true
            attrs.forEach { kvp ->
                val a = context.getResources()
                val attr = kvp.value
                when (sMapToConstant.get(attr)) {
                    LEFT_TO_LEFT -> leftToLeft = lookupID(a, kvp.key, attr, leftToLeft)
                    LEFT_TO_RIGHT -> leftToRight = lookupID(a, kvp.key, attr, leftToRight)
                    RIGHT_TO_LEFT -> rightToLeft = lookupID(a, kvp.key, attr, rightToLeft)
                    RIGHT_TO_RIGHT -> rightToRight = lookupID(a, kvp.key, attr, rightToRight)
                    TOP_TO_TOP -> topToTop = lookupID(a, kvp.key, attr, topToTop)
                    TOP_TO_BOTTOM -> topToBottom = lookupID(a, kvp.key, attr, topToBottom)
                    BOTTOM_TO_TOP -> bottomToTop = lookupID(a, kvp.key, attr, bottomToTop)
                    BOTTOM_TO_BOTTOM -> bottomToBottom = lookupID(a, kvp.key, attr, bottomToBottom)
                    BASELINE_TO_BASELINE -> baselineToBaseline =
                        lookupID(a, kvp.key, attr, baselineToBaseline)
                    BASELINE_TO_TOP -> baselineToTop = lookupID(a, kvp.key, attr, baselineToTop)
                    BASELINE_TO_BOTTOM -> baselineToBottom = lookupID(
                        a,
                        kvp.key,
                        attr,
                        baselineToBottom
                    )
                    EDITOR_ABSOLUTE_X -> editorAbsoluteX =
                        a.getDimensionPixelOffset(attr, editorAbsoluteX)
                    EDITOR_ABSOLUTE_Y -> editorAbsoluteY =
                        a.getDimensionPixelOffset(attr, editorAbsoluteY)
                    GUIDE_BEGIN -> guideBegin = a.getDimensionPixelOffset(attr, guideBegin)
                    GUIDE_END -> guideEnd = a.getDimensionPixelOffset(attr, guideEnd)
                    GUIDE_PERCENT -> guidePercent = a.getFloat(attr, guidePercent)
                    GUIDE_USE_RTL -> guidelineUseRtl = a.getBoolean(attr, guidelineUseRtl)
                    ORIENTATION -> orientation = a.getInt(attr, orientation)
                    START_TO_END -> startToEnd = lookupID(a, kvp.key, attr, startToEnd)
                    START_TO_START -> startToStart = lookupID(a, kvp.key, attr, startToStart)
                    END_TO_START -> endToStart = lookupID(a, kvp.key, attr, endToStart)
                    END_TO_END -> endToEnd = lookupID(a, kvp.key, attr, endToEnd)
                    CIRCLE -> circleConstraint = lookupID(a, kvp.key, attr, circleConstraint)
                    CIRCLE_RADIUS -> circleRadius = a.getDimensionPixelSize(attr, circleRadius)
                    CIRCLE_ANGLE -> circleAngle = a.getFloat(attr, circleAngle)
                    GONE_LEFT_MARGIN -> goneLeftMargin =
                        a.getDimensionPixelSize(attr, goneLeftMargin)
                    GONE_TOP_MARGIN -> goneTopMargin = a.getDimensionPixelSize(attr, goneTopMargin)
                    GONE_RIGHT_MARGIN -> goneRightMargin =
                        a.getDimensionPixelSize(attr, goneRightMargin)
                    GONE_BOTTOM_MARGIN -> goneBottomMargin =
                        a.getDimensionPixelSize(attr, goneBottomMargin)
                    GONE_START_MARGIN -> goneStartMargin =
                        a.getDimensionPixelSize(attr, goneStartMargin)
                    GONE_END_MARGIN -> goneEndMargin = a.getDimensionPixelSize(attr, goneEndMargin)
                    GONE_BASELINE_MARGIN -> goneBaselineMargin =
                        a.getDimensionPixelSize(attr, goneBaselineMargin)
                    HORIZONTAL_BIAS -> horizontalBias = a.getFloat(attr, horizontalBias)
                    VERTICAL_BIAS -> verticalBias = a.getFloat(attr, verticalBias)
                    LEFT_MARGIN -> leftMargin = a.getDimensionPixelSize(attr, leftMargin)
                    RIGHT_MARGIN -> rightMargin = a.getDimensionPixelSize(attr, rightMargin)
                    START_MARGIN -> startMargin = a.getDimensionPixelSize(attr, startMargin)
                    END_MARGIN -> endMargin = a.getDimensionPixelSize(attr, endMargin)
                    TOP_MARGIN -> topMargin = a.getDimensionPixelSize(attr, topMargin)
                    BOTTOM_MARGIN -> bottomMargin = a.getDimensionPixelSize(attr, bottomMargin)
                    BASELINE_MARGIN -> baselineMargin =
                        a.getDimensionPixelSize(attr, baselineMargin)
                    LAYOUT_WIDTH -> mWidth = a.getLayoutDimension(attr, mWidth)
                    LAYOUT_HEIGHT -> mHeight = a.getLayoutDimension(attr, mHeight)
                    LAYOUT_CONSTRAINT_WIDTH -> parseDimensionConstraints(
                        this,
                        a,
                        kvp.key,
                        attr,
                        HORIZONTAL
                    )
                    LAYOUT_CONSTRAINT_HEIGHT -> parseDimensionConstraints(
                        this,
                        a,
                        kvp.key,
                        attr,
                        VERTICAL
                    )
                    WIDTH_DEFAULT -> widthDefault = a.getInt(attr, widthDefault)
                    HEIGHT_DEFAULT -> heightDefault = a.getInt(attr, heightDefault)
                    VERTICAL_WEIGHT -> verticalWeight = a.getFloat(attr, verticalWeight)
                    HORIZONTAL_WEIGHT -> horizontalWeight = a.getFloat(attr, horizontalWeight)
                    VERTICAL_STYLE -> verticalChainStyle = a.getInt(attr, verticalChainStyle)
                    HORIZONTAL_STYLE -> horizontalChainStyle = a.getInt(attr, horizontalChainStyle)
                    DIMENSION_RATIO -> dimensionRatio = a.getString(kvp.key, attr)
                    HEIGHT_MAX -> heightMax = a.getDimensionPixelSize(attr, heightMax)
                    WIDTH_MAX -> widthMax = a.getDimensionPixelSize(attr, widthMax)
                    HEIGHT_MIN -> heightMin = a.getDimensionPixelSize(attr, heightMin)
                    WIDTH_MIN -> widthMin = a.getDimensionPixelSize(attr, widthMin)
                    WIDTH_PERCENT -> widthPercent = a.getFloat(attr, 1f)
                    HEIGHT_PERCENT -> heightPercent = a.getFloat(attr, 1f)
                    CONSTRAINED_WIDTH -> constrainedWidth = a.getBoolean(attr, constrainedWidth)
                    CONSTRAINED_HEIGHT -> constrainedHeight = a.getBoolean(attr, constrainedHeight)
                    CHAIN_USE_RTL -> Log.e(
                        TAG,
                        "CURRENTLY UNSUPPORTED"
                    ) // TODO add support or remove
                    BARRIER_DIRECTION -> mBarrierDirection = a.getInt(attr, mBarrierDirection)
                    LAYOUT_WRAP_BEHAVIOR -> mWrapBehavior = a.getInt(attr, mWrapBehavior)
                    BARRIER_MARGIN -> mBarrierMargin = a.getDimensionPixelSize(attr, mBarrierMargin)
                    CONSTRAINT_REFERENCED_IDS -> mReferenceIdString = a.getString(kvp.key, attr)
                    BARRIER_ALLOWS_GONE_WIDGETS -> mBarrierAllowsGoneWidgets =
                        a.getBoolean(attr, mBarrierAllowsGoneWidgets)
                    CONSTRAINT_TAG -> mConstraintTag = a.getString(kvp.key, attr)
                    UNUSED -> Log.w(
                        TAG,
                        "unused attribute 0x" + attr
                            .toString() + "   " + sMapToConstant.get(attr)
                    )
                    else -> Log.w(
                        TAG,
                        "Unknown attribute 0x" + attr
                            .toString() + "   " + sMapToConstant.get(attr)
                    )
                }
            }
        }

        /**
         * Print the content to a string
         * @param scene
         * @param stringBuilder
         */
        fun dump(scene: MotionScene, stringBuilder: StringBuilder) {
            /*val fields: Array<Field> = this.getClass().getDeclaredFields()
            stringBuilder.put("\n")
            for (i in fields.indices) {
                val field: Field = fields[i]
                val name: String = field.getName()
                if (Modifier.isStatic(field.getModifiers())) {
                    continue
                }
                try {
                    val value: Object = field.get(this)
                    val type: TClass<*> = field.getType()
                    if (type === Int.TYPE) {
                        val iValue: Int = value as Int
                        if (iValue != UNSET) {
                            val stringId = scene.lookUpConstraintName(iValue)
                            stringBuilder.put("    ")
                            stringBuilder.put(name)
                            stringBuilder.put(" = \"")
                            stringBuilder.put(stringId ?: iValue)
                            stringBuilder.put("\"\n")
                        }
                    } else if (type === Float.TYPE) {
                        val fValue = value.toFloat()
                        if (fValue != UNSET) {
                            stringBuilder.put("    ")
                            stringBuilder.put(name)
                            stringBuilder.put(" = \"")
                            stringBuilder.put(fValue)
                            stringBuilder.put("\"\n")
                        }
                    }
                } catch (e: IllegalAccessException) {
                    Log.e(TAG, "Error accessing ConstraintSet field", e)
                }
            }*/
        }

        companion object {
            val UNSET = ConstraintSet.UNSET
            val UNSET_ID = ConstraintSet.UNSET_ID
            val UNSET_GONE_MARGIN: Int = Int.MIN_VALUE
            private val sMapToConstant: MutableMap<String, Int> = mutableMapOf()
            private const val BASELINE_TO_BASELINE = 1
            private const val BOTTOM_MARGIN = 2
            private const val BOTTOM_TO_BOTTOM = 3
            private const val BOTTOM_TO_TOP = 4
            private const val DIMENSION_RATIO = 5
            private const val EDITOR_ABSOLUTE_X = 6
            private const val EDITOR_ABSOLUTE_Y = 7
            private const val END_MARGIN = 8
            private const val END_TO_END = 9
            private const val END_TO_START = 10
            private const val GONE_BOTTOM_MARGIN = 11
            private const val GONE_END_MARGIN = 12
            private const val GONE_LEFT_MARGIN = 13
            private const val GONE_RIGHT_MARGIN = 14
            private const val GONE_START_MARGIN = 15
            private const val GONE_TOP_MARGIN = 16
            private const val GUIDE_BEGIN = 17
            private const val GUIDE_END = 18
            private const val GUIDE_PERCENT = 19
            private const val HORIZONTAL_BIAS = 20
            private const val LAYOUT_HEIGHT = 21
            private const val LAYOUT_WIDTH = 22
            private const val LEFT_MARGIN = 23
            private const val LEFT_TO_LEFT = 24
            private const val LEFT_TO_RIGHT = 25
            private const val ORIENTATION = 26
            private const val RIGHT_MARGIN = 27
            private const val RIGHT_TO_LEFT = 28
            private const val RIGHT_TO_RIGHT = 29
            private const val START_MARGIN = 30
            private const val START_TO_END = 31
            private const val START_TO_START = 32
            private const val TOP_MARGIN = 33
            private const val TOP_TO_BOTTOM = 34
            private const val TOP_TO_TOP = 35
            private const val VERTICAL_BIAS = 36
            private const val HORIZONTAL_WEIGHT = 37
            private const val VERTICAL_WEIGHT = 38
            private const val HORIZONTAL_STYLE = 39
            private const val VERTICAL_STYLE = 40
            private const val LAYOUT_CONSTRAINT_WIDTH = 41
            private const val LAYOUT_CONSTRAINT_HEIGHT = 42
            private const val CIRCLE = 61
            private const val CIRCLE_RADIUS = 62
            private const val CIRCLE_ANGLE = 63
            private const val WIDTH_PERCENT = 69
            private const val HEIGHT_PERCENT = 70
            private const val CHAIN_USE_RTL = 71
            private const val BARRIER_DIRECTION = 72
            private const val BARRIER_MARGIN = 73
            private const val CONSTRAINT_REFERENCED_IDS = 74
            private const val BARRIER_ALLOWS_GONE_WIDGETS = 75
            private const val LAYOUT_WRAP_BEHAVIOR = 76
            private const val BASELINE_TO_TOP = 77
            private const val BASELINE_TO_BOTTOM = 78
            private const val GONE_BASELINE_MARGIN = 79
            private const val BASELINE_MARGIN = 80
            private const val WIDTH_DEFAULT = 81
            private const val HEIGHT_DEFAULT = 82
            private const val HEIGHT_MAX = 83
            private const val WIDTH_MAX = 84
            private const val HEIGHT_MIN = 85
            private const val WIDTH_MIN = 86
            private const val CONSTRAINED_WIDTH = 87
            private const val CONSTRAINED_HEIGHT = 88
            private const val CONSTRAINT_TAG = 89
            private const val GUIDE_USE_RTL = 90
            private const val UNUSED = 91

            init {
                sMapToConstant.put(
                    "layout_constraintLeft_toLeftOf",
                    LEFT_TO_LEFT
                )
                sMapToConstant.put(
                    "layout_constraintLeft_toRightOf",
                    LEFT_TO_RIGHT
                )
                sMapToConstant.put(
                    "layout_constraintRight_toLeftOf",
                    RIGHT_TO_LEFT
                )
                sMapToConstant.put(
                    "layout_constraintRight_toRightOf",
                    RIGHT_TO_RIGHT
                )
                sMapToConstant.put("layout_constraintTop_toTopOf", TOP_TO_TOP)
                sMapToConstant.put(
                    "layout_constraintTop_toBottomOf",
                    TOP_TO_BOTTOM
                )
                sMapToConstant.put(
                    "layout_constraintBottom_toTopOf",
                    BOTTOM_TO_TOP
                )
                sMapToConstant.put(
                    "layout_constraintBottom_toBottomOf",
                    BOTTOM_TO_BOTTOM
                )
                sMapToConstant.put(
                    "layout_constraintBaseline_toBaselineOf",
                    BASELINE_TO_BASELINE
                )
                sMapToConstant.put("layout_editor_absoluteX", EDITOR_ABSOLUTE_X)
                sMapToConstant.put("layout_editor_absoluteY", EDITOR_ABSOLUTE_Y)
                sMapToConstant.put("layout_constraintGuide_begin", GUIDE_BEGIN)
                sMapToConstant.put("layout_constraintGuide_end", GUIDE_END)
                sMapToConstant.put(
                    "layout_constraintGuide_percent",
                    GUIDE_PERCENT
                )
                sMapToConstant.put("guidelineUseRtl", GUIDE_USE_RTL)
                sMapToConstant.put("android_orientation", ORIENTATION)
                sMapToConstant.put(
                    "layout_constraintStart_toEndOf",
                    START_TO_END
                )
                sMapToConstant.put(
                    "layout_constraintStart_toStartOf",
                    START_TO_START
                )
                sMapToConstant.put(
                    "layout_constraintEnd_toStartOf",
                    END_TO_START
                )
                sMapToConstant.put("layout_constraintEnd_toEndOf", END_TO_END)
                sMapToConstant.put("layout_goneMarginLeft", GONE_LEFT_MARGIN)
                sMapToConstant.put("layout_goneMarginTop", GONE_TOP_MARGIN)
                sMapToConstant.put("layout_goneMarginRight", GONE_RIGHT_MARGIN)
                sMapToConstant.put(
                    "layout_goneMarginBottom",
                    GONE_BOTTOM_MARGIN
                )
                sMapToConstant.put("layout_goneMarginStart", GONE_START_MARGIN)
                sMapToConstant.put("layout_goneMarginEnd", GONE_END_MARGIN)
                sMapToConstant.put(
                    "layout_constraintVertical_weight",
                    VERTICAL_WEIGHT
                )
                sMapToConstant.put(
                    "layout_constraintHorizontal_weight",
                    HORIZONTAL_WEIGHT
                )
                sMapToConstant.put(
                    "layout_constraintHorizontal_chainStyle",
                    HORIZONTAL_STYLE
                )
                sMapToConstant.put(
                    "layout_constraintVertical_chainStyle",
                    VERTICAL_STYLE
                )
                sMapToConstant.put(
                    "layout_constraintHorizontal_bias",
                    HORIZONTAL_BIAS
                )
                sMapToConstant.put(
                    "layout_constraintVertical_bias",
                    VERTICAL_BIAS
                )
                sMapToConstant.put(
                    "layout_constraintDimensionRatio",
                    DIMENSION_RATIO
                )
                sMapToConstant.put("layout_constraintLeft_creator", UNUSED)
                sMapToConstant.put("layout_constraintTop_creator", UNUSED)
                sMapToConstant.put("layout_constraintRight_creator", UNUSED)
                sMapToConstant.put("layout_constraintBottom_creator", UNUSED)
                sMapToConstant.put("layout_constraintBaseline_creator", UNUSED)
                sMapToConstant.put("android_layout_marginLeft", LEFT_MARGIN)
                sMapToConstant.put("android_layout_marginRight", RIGHT_MARGIN)
                sMapToConstant.put("android_layout_marginStart", START_MARGIN)
                sMapToConstant.put("android_layout_marginEnd", END_MARGIN)
                sMapToConstant.put("android_layout_marginTop", TOP_MARGIN)
                sMapToConstant.put("android_layout_marginBottom", BOTTOM_MARGIN)
                sMapToConstant.put("android_layout_width", LAYOUT_WIDTH)
                sMapToConstant.put("android_layout_height", LAYOUT_HEIGHT)
                sMapToConstant.put(
                    "layout_constraintWidth",
                    LAYOUT_CONSTRAINT_WIDTH
                )
                sMapToConstant.put(
                    "layout_constraintHeight",
                    LAYOUT_CONSTRAINT_HEIGHT
                )
                sMapToConstant.put(
                    "layout_constrainedWidth",
                    LAYOUT_CONSTRAINT_WIDTH
                )
                sMapToConstant.put(
                    "layout_constrainedHeight",
                    LAYOUT_CONSTRAINT_HEIGHT
                )
                sMapToConstant.put(
                    "layout_wrapBehaviorInParent",
                    LAYOUT_WRAP_BEHAVIOR
                )
                sMapToConstant.put("layout_constraintCircle", CIRCLE)
                sMapToConstant.put(
                    "layout_constraintCircleRadius",
                    CIRCLE_RADIUS
                )
                sMapToConstant.put("layout_constraintCircleAngle", CIRCLE_ANGLE)
                sMapToConstant.put(
                    "layout_constraintWidth_percent",
                    WIDTH_PERCENT
                )
                sMapToConstant.put(
                    "layout_constraintHeight_percent",
                    HEIGHT_PERCENT
                )
                sMapToConstant.put("chainUseRtl", CHAIN_USE_RTL)
                sMapToConstant.put("barrierDirection", BARRIER_DIRECTION)
                sMapToConstant.put("barrierMargin", BARRIER_MARGIN)
                sMapToConstant.put(
                    "constraint_referenced_ids",
                    CONSTRAINT_REFERENCED_IDS
                )
                sMapToConstant.put(
                    "barrierAllowsGoneWidgets",
                    BARRIER_ALLOWS_GONE_WIDGETS
                )
                sMapToConstant.put(
                    "layout_constraintWidth_max",
                    WIDTH_MAX
                )
                sMapToConstant.put(
                    "layout_constraintWidth_min",
                    WIDTH_MIN
                )
                sMapToConstant.put(
                    "layout_constraintWidth_max",
                    HEIGHT_MAX
                )
                sMapToConstant.put(
                    "layout_constraintHeight_min",
                    HEIGHT_MIN
                )
                sMapToConstant.put("layout_constraintWidth", CONSTRAINED_WIDTH)
                sMapToConstant.put(
                    "layout_constraintHeight",
                    CONSTRAINED_HEIGHT
                )
                sMapToConstant.put(
                    "layout_constraintTag",
                    CONSTRAINT_TAG
                )
                sMapToConstant.put("guidelineUseRtl", GUIDE_USE_RTL)
            }
        }
    }

    /**
     *
     */
    class Transform {
        var mApply = false
        var rotation = 0f
        var rotationX = 0f
        var rotationY = 0f
        var scaleX = 1f
        var scaleY = 1f
        var transformPivotX = Float.NaN
        var transformPivotY = Float.NaN
        var transformPivotTarget = UNSET_ID
        var translationX = 0f
        var translationY = 0f
        var translationZ = 0f
        var applyElevation = false
        var elevation = 0f

        /**
         * Copy Transform from src
         * @param src
         */
        fun copyFrom(src: Transform) {
            mApply = src.mApply
            rotation = src.rotation
            rotationX = src.rotationX
            rotationY = src.rotationY
            scaleX = src.scaleX
            scaleY = src.scaleY
            transformPivotX = src.transformPivotX
            transformPivotY = src.transformPivotY
            transformPivotTarget = src.transformPivotTarget
            translationX = src.translationX
            translationY = src.translationY
            translationZ = src.translationZ
            applyElevation = src.applyElevation
            elevation = src.elevation
        }

        fun fillFromAttributeList(context: TContext, attrs: AttributeSet) {
            mApply = true
            attrs.forEach { kvp ->
                val a = context.getResources()
                val attr = kvp.value
                when (sMapToConstant.get(attr)) {
                    ROTATION -> rotation = a.getFloat(attr, rotation)
                    ROTATION_X -> rotationX = a.getFloat(attr, rotationX)
                    ROTATION_Y -> rotationY = a.getFloat(attr, rotationY)
                    SCALE_X -> scaleX = a.getFloat(attr, scaleX)
                    SCALE_Y -> scaleY = a.getFloat(attr, scaleY)
                    TRANSFORM_PIVOT_X -> transformPivotX = a.getDimension(attr, transformPivotX)
                    TRANSFORM_PIVOT_Y -> transformPivotY = a.getDimension(attr, transformPivotY)
                    TRANSFORM_PIVOT_TARGET -> transformPivotTarget = lookupID(
                        a,
                        kvp.key,
                        attr,
                        transformPivotTarget
                    )
                    TRANSLATION_X -> translationX = a.getDimension(attr, translationX)
                    TRANSLATION_Y -> translationY = a.getDimension(attr, translationY)
                    TRANSLATION_Z -> translationZ = a.getDimension(attr, translationZ)
                    ELEVATION -> {
                        applyElevation = true
                        elevation = a.getDimension(attr, elevation)
                    }
                }
            }
        }

        companion object {
            private val sMapToConstant: MutableMap<String, Int> = mutableMapOf()
            private const val ROTATION = 1
            private const val ROTATION_X = 2
            private const val ROTATION_Y = 3
            private const val SCALE_X = 4
            private const val SCALE_Y = 5
            private const val TRANSFORM_PIVOT_X = 6
            private const val TRANSFORM_PIVOT_Y = 7
            private const val TRANSLATION_X = 8
            private const val TRANSLATION_Y = 9
            private const val TRANSLATION_Z = 10
            private const val ELEVATION = 11
            private const val TRANSFORM_PIVOT_TARGET = 12

            init {
                sMapToConstant.put("android_rotation", ROTATION)
                sMapToConstant.put("android_rotationX", ROTATION_X)
                sMapToConstant.put("android_rotationY", ROTATION_Y)
                sMapToConstant.put("android_scaleX", SCALE_X)
                sMapToConstant.put("android_scaleY", SCALE_Y)
                sMapToConstant.put(
                    "android_transformPivotX",
                    TRANSFORM_PIVOT_X
                )
                sMapToConstant.put(
                    "android_transformPivotY",
                    TRANSFORM_PIVOT_Y
                )
                sMapToConstant.put("android_translationX", TRANSLATION_X)
                sMapToConstant.put("android_translationY", TRANSLATION_Y)
                sMapToConstant.put("android_translationZ", TRANSLATION_Z)
                sMapToConstant.put("android_elevation", ELEVATION)
                sMapToConstant.put(
                    "transformPivotTarget",
                    TRANSFORM_PIVOT_TARGET
                )
            }
        }
    }

    /**
     *
     */
    class PropertySet {
        var mApply = false
        var visibility: Int = TView.VISIBLE
        var mVisibilityMode = VISIBILITY_MODE_NORMAL
        var alpha = 1f
        var mProgress = Float.NaN
        // @TODO: add description
        /**
         *
         * @param src
         */
        fun copyFrom(src: PropertySet) {
            mApply = src.mApply
            visibility = src.visibility
            alpha = src.alpha
            mProgress = src.mProgress
            mVisibilityMode = src.mVisibilityMode
        }

        fun fillFromAttributeList(context: TContext, attrs: AttributeSet) {
            mApply = true
            attrs.forEach { kvp ->
                val a = context.getResources()
                val attr = kvp.value
                if (attr == "android_alpha") {
                    alpha = a.getFloat(attr, alpha)
                } else if (attr == "android_visibility") {
                    visibility = a.getInt(attr, visibility)
                    visibility = VISIBILITY_FLAGS[visibility]
                } else if (attr == "visibilityMode") {
                    mVisibilityMode = a.getInt(attr, mVisibilityMode)
                } else if (attr == "motionProgress") {
                    mProgress = a.getFloat(attr, mProgress)
                }
            }
        }
    }

    /**
     *
     */
    class Motion {
        var mApply = false
        var mAnimateRelativeTo = Layout.UNSET_ID
        var mAnimateCircleAngleTo = 0
        var mTransitionEasing: String? = null
        var mPathMotionArc = Layout.UNSET
        var mDrawPath = 0
        var mMotionStagger = Float.NaN
        var mPolarRelativeTo = Layout.UNSET_ID
        var mPathRotate = Float.NaN
        var mQuantizeMotionPhase = Float.NaN
        var mQuantizeMotionSteps = Layout.UNSET
        var mQuantizeInterpolatorString: String? = null
        var mQuantizeInterpolatorType = INTERPOLATOR_UNDEFINED // undefined
        var mQuantizeInterpolatorID = UNSET_ID
        // @TODO: add description
        /**
         *
         * @param src
         */
        fun copyFrom(src: Motion) {
            mApply = src.mApply
            mAnimateRelativeTo = src.mAnimateRelativeTo
            mTransitionEasing = src.mTransitionEasing
            mPathMotionArc = src.mPathMotionArc
            mDrawPath = src.mDrawPath
            mPathRotate = src.mPathRotate
            mMotionStagger = src.mMotionStagger
            mPolarRelativeTo = src.mPolarRelativeTo
        }

        fun fillFromAttributeList(context: TContext, attrs: AttributeSet) {
            mApply = true
            attrs.forEach { kvp ->
                val a = context.getResources()
                val attr = kvp.value
                when (sMapToConstant.get(attr)) {
                    TRANSITION_PATH_ROTATE -> mPathRotate = a.getFloat(attr, mPathRotate)
                    PATH_MOTION_ARC -> mPathMotionArc = a.getInt(attr, mPathMotionArc)
                    TRANSITION_EASING -> {
                        val type = a.getResourceType(attr)
                        mTransitionEasing = if (type == TypedValue.TYPE_STRING) {
                            a.getString(kvp.key, attr)
                        } else {
                            Easing.NAMED_EASING.get(a.getInt(attr, 0))
                        }
                    }
                    MOTION_DRAW_PATH -> mDrawPath = a.getInt(attr, 0)
                    ANIMATE_RELATIVE_TO -> mAnimateRelativeTo =
                        lookupID(a, kvp.key, attr, mAnimateRelativeTo)
                    ANIMATE_CIRCLE_ANGLE_TO -> mAnimateCircleAngleTo =
                        a.getInt(attr, mAnimateCircleAngleTo)
                    MOTION_STAGGER -> mMotionStagger = a.getFloat(attr, mMotionStagger)
                    QUANTIZE_MOTION_STEPS -> mQuantizeMotionSteps =
                        a.getInt(attr, mQuantizeMotionSteps)
                    QUANTIZE_MOTION_PHASE -> mQuantizeMotionPhase =
                        a.getFloat(attr, mQuantizeMotionPhase)
                    QUANTIZE_MOTION_INTERPOLATOR -> {
                        val type = a.getResourceType(attr)
                        if (type == TypedValue.TYPE_REFERENCE) {
                            mQuantizeInterpolatorID = a.getResourceId(attr, "")
                            if (mQuantizeInterpolatorID != "") {
                                mQuantizeInterpolatorType = INTERPOLATOR_REFERENCE_ID
                            }
                        } else if (type == TypedValue.TYPE_STRING) {
                            mQuantizeInterpolatorString = a.getString(kvp.key, attr)
                            if (mQuantizeInterpolatorString!!.indexOf("/") > 0) {
                                mQuantizeInterpolatorID = a.getResourceId(attr, "")
                                mQuantizeInterpolatorType = INTERPOLATOR_REFERENCE_ID
                            } else {
                                mQuantizeInterpolatorType = SPLINE_STRING
                            }
                        } else {
                            mQuantizeInterpolatorType = a.getInt(attr, -1)
                        }
                    }
                }
            }
        }

        companion object {
            const val INTERPOLATOR_REFERENCE_ID = -2
            const val SPLINE_STRING = -1
            private const val INTERPOLATOR_UNDEFINED = -3
            private val sMapToConstant: MutableMap<String, Int> = mutableMapOf()
            private const val TRANSITION_PATH_ROTATE = 1
            private const val PATH_MOTION_ARC = 2
            private const val TRANSITION_EASING = 3
            private const val MOTION_DRAW_PATH = 4
            private const val ANIMATE_RELATIVE_TO = 5
            private const val ANIMATE_CIRCLE_ANGLE_TO = 6
            private const val MOTION_STAGGER = 7
            private const val QUANTIZE_MOTION_STEPS = 8
            private const val QUANTIZE_MOTION_PHASE = 9
            private const val QUANTIZE_MOTION_INTERPOLATOR = 10

            init {
                sMapToConstant.put("motionPathRotate", TRANSITION_PATH_ROTATE)
                sMapToConstant.put("pathMotionArc", PATH_MOTION_ARC)
                sMapToConstant.put("transitionEasing", TRANSITION_EASING)
                sMapToConstant.put("drawPath", MOTION_DRAW_PATH)
                sMapToConstant.put("animateRelativeTo", ANIMATE_RELATIVE_TO)
                sMapToConstant.put(
                    "animateCircleAngleTo",
                    ANIMATE_CIRCLE_ANGLE_TO
                )
                sMapToConstant.put("motionStagger", MOTION_STAGGER)
                sMapToConstant.put("quantizeMotionSteps", QUANTIZE_MOTION_STEPS)
                sMapToConstant.put("quantizeMotionPhase", QUANTIZE_MOTION_PHASE)
                sMapToConstant.put(
                    "quantizeMotionInterpolator",
                    QUANTIZE_MOTION_INTERPOLATOR
                )
            }
        }
    }

    /**
     *
     */
    class Constraint {
        var mViewId = ""
        var mTargetString: String? = null
        val propertySet = PropertySet()
        val motion = Motion()
        val layout = Layout()
        val transform = Transform()
        var mCustomConstraints: MutableMap<String, ConstraintAttribute> = mutableMapOf()
        var mDelta: Delta? = null

        class Delta {
            var mTypeInt = IntArray(INITIAL_INT)
            var mValueInt = IntArray(INITIAL_INT)
            var mCountInt = 0
            fun add(type: Int, value: Int) {
                if (mCountInt >= mTypeInt.size) {
                    mTypeInt = Arrays.copyOf(mTypeInt, mTypeInt.size * 2)
                    mValueInt = Arrays.copyOf(mValueInt, mValueInt.size * 2)
                }
                mTypeInt[mCountInt] = type
                mValueInt[mCountInt++] = value
            }

            var mTypeFloat = IntArray(INITIAL_FLOAT)
            var mValueFloat = FloatArray(INITIAL_FLOAT)
            var mCountFloat = 0
            fun add(type: Int, value: Float) {
                if (mCountFloat >= mTypeFloat.size) {
                    mTypeFloat = Arrays.copyOf(mTypeFloat, mTypeFloat.size * 2)
                    mValueFloat = Arrays.copyOf(mValueFloat, mValueFloat.size * 2)
                }
                mTypeFloat[mCountFloat] = type
                mValueFloat[mCountFloat++] = value
            }

            var mTypeString = IntArray(INITIAL_STRING)
            var mValueString = arrayOfNulls<String>(INITIAL_STRING)
            var mCountString = 0
            fun add(type: Int, value: String?) {
                if (mCountString >= mTypeString.size) {
                    mTypeString = Arrays.copyOf(mTypeString, mTypeString.size * 2)
                    mValueString = Arrays.copyOf(mValueString, mValueString.size * 2)
                }
                mTypeString[mCountString] = type
                mValueString[mCountString++] = value
            }

            var mTypeBoolean = IntArray(INITIAL_BOOLEAN)
            var mValueBoolean = BooleanArray(INITIAL_BOOLEAN)
            var mCountBoolean = 0
            fun add(type: Int, value: Boolean) {
                if (mCountBoolean >= mTypeBoolean.size) {
                    mTypeBoolean = Arrays.copyOf(mTypeBoolean, mTypeBoolean.size * 2)
                    mValueBoolean = Arrays.copyOf(mValueBoolean, mValueBoolean.size * 2)
                }
                mTypeBoolean[mCountBoolean] = type
                mValueBoolean[mCountBoolean++] = value
            }

            fun applyDelta(c: Constraint?) {
                for (i in 0 until mCountInt) {
                    setDeltaValue(c, mTypeInt[i], mValueInt[i])
                }
                for (i in 0 until mCountFloat) {
                    setDeltaValue(c, mTypeFloat[i], mValueFloat[i])
                }
                for (i in 0 until mCountString) {
                    setDeltaValue(c, mTypeString[i], mValueString[i])
                }
                for (i in 0 until mCountBoolean) {
                    setDeltaValue(c, mTypeBoolean[i], mValueBoolean[i])
                }
            }

            fun printDelta(tag: String) {
                Log.v(tag, "int")
                for (i in 0 until mCountInt) {
                    Log.v(tag, mTypeInt[i].toString() + " = " + mValueInt[i])
                }
                Log.v(tag, "float")
                for (i in 0 until mCountFloat) {
                    Log.v(tag, mTypeFloat[i].toString() + " = " + mValueFloat[i])
                }
                Log.v(tag, "strings")
                for (i in 0 until mCountString) {
                    Log.v(tag, mTypeString[i].toString() + " = " + mValueString[i])
                }
                Log.v(tag, "boolean")
                for (i in 0 until mCountBoolean) {
                    Log.v(tag, mTypeBoolean[i].toString() + " = " + mValueBoolean[i])
                }
            }

            companion object {
                private const val INITIAL_BOOLEAN = 4
                private const val INITIAL_INT = 10
                private const val INITIAL_FLOAT = 10
                private const val INITIAL_STRING = 5
            }
        }

        /**
         * Apply a delta to a constraint
         * @param c
         */
        fun applyDelta(c: Constraint?) {
            if (mDelta != null) {
                mDelta!!.applyDelta(c)
            }
        }

        /**
         * Apply a delta file
         * @param tag
         */
        fun printDelta(tag: String) {
            if (mDelta != null) {
                mDelta!!.printDelta(tag)
            } else {
                Log.v(tag, "DELTA IS NULL")
            }
        }

        private operator fun get(
            attributeName: String,
            attributeType: ConstraintAttribute.AttributeType
        ): ConstraintAttribute {
            val ret: ConstraintAttribute
            if (mCustomConstraints.containsKey(attributeName)) {
                ret = mCustomConstraints[attributeName]!!
                require(!(ret.type !== attributeType)) {
                    "ConstraintAttribute is already a " + ret.type.name
                }
            } else {
                ret = ConstraintAttribute(attributeName, attributeType)
                mCustomConstraints[attributeName] = ret
            }
            return ret
        }

        fun setStringValue(attributeName: String, value: String) {
            get(attributeName, ConstraintAttribute.AttributeType.STRING_TYPE).stringValue = (value)
        }

        fun setFloatValue(attributeName: String, value: Float) {
            get(attributeName, ConstraintAttribute.AttributeType.FLOAT_TYPE).floatValue = (value)
        }

        fun setIntValue(attributeName: String, value: Int) {
            get(attributeName, ConstraintAttribute.AttributeType.INT_TYPE).setIntValue(value)
        }

        fun setColorValue(attributeName: String, value: Int) {
            get(attributeName, ConstraintAttribute.AttributeType.COLOR_TYPE).colorValue = TColor.fromInt(value)
        }

        /**
         * Return a copy of the Constraint
         * @return
         */
        fun clone(): Constraint {
            val clone = Constraint()
            clone.layout.copyFrom(layout)
            clone.motion.copyFrom(motion)
            clone.propertySet.copyFrom(propertySet)
            clone.transform.copyFrom(transform)
            clone.mViewId = mViewId
            clone.mDelta = mDelta
            return clone
        }

        fun fillFromConstraints(
            helper: ConstraintHelper,
            viewId: String,
            param: Constraints.LayoutParams
        ) {
            fillFromConstraints(viewId, param)
            if (helper is Barrier) {
                layout.mHelperType = BARRIER_TYPE
                val barrier: Barrier = helper
                layout.mBarrierDirection = barrier.type
                layout.mReferenceIds = barrier.referencedIds
                layout.mBarrierMargin = barrier.margin
            }
        }

        fun fillFromConstraints(viewId: String, param: Constraints.LayoutParams) {
            fillFrom(viewId, param)
            propertySet.alpha = param.alpha
            transform.rotation = param.rotation
            transform.rotationX = param.rotationX
            transform.rotationY = param.rotationY
            transform.scaleX = param.scaleX
            transform.scaleY = param.scaleY
            transform.transformPivotX = param.transformPivotX
            transform.transformPivotY = param.transformPivotY
            transform.translationX = param.translationX
            transform.translationY = param.translationY
            transform.translationZ = param.translationZ
            transform.elevation = param.elevation
            transform.applyElevation = param.applyElevation
        }

        fun fillFrom(viewId: String, param: ConstraintLayout.LayoutParams) {
            mViewId = viewId
            layout.leftToLeft = param.leftToLeft
            layout.leftToRight = param.leftToRight
            layout.rightToLeft = param.rightToLeft
            layout.rightToRight = param.rightToRight
            layout.topToTop = param.topToTop
            layout.topToBottom = param.topToBottom
            layout.bottomToTop = param.bottomToTop
            layout.bottomToBottom = param.bottomToBottom
            layout.baselineToBaseline = param.baselineToBaseline
            layout.baselineToTop = param.baselineToTop
            layout.baselineToBottom = param.baselineToBottom
            layout.startToEnd = param.startToEnd
            layout.startToStart = param.startToStart
            layout.endToStart = param.endToStart
            layout.endToEnd = param.endToEnd
            layout.horizontalBias = param.horizontalBias
            layout.verticalBias = param.verticalBias
            layout.dimensionRatio = param.dimensionRatio
            layout.circleConstraint = param.circleConstraint
            layout.circleRadius = param.circleRadius
            layout.circleAngle = param.circleAngle
            layout.editorAbsoluteX = param.editorAbsoluteX
            layout.editorAbsoluteY = param.editorAbsoluteY
            layout.orientation = param.orientation
            layout.guidePercent = param.guidePercent
            layout.guideBegin = param.guideBegin
            layout.guideEnd = param.guideEnd
            layout.mWidth = param.width
            layout.mHeight = param.height
            layout.leftMargin = param.leftMargin
            layout.rightMargin = param.rightMargin
            layout.topMargin = param.topMargin
            layout.bottomMargin = param.bottomMargin
            layout.baselineMargin = param.baselineMargin
            layout.verticalWeight = param.verticalWeight
            layout.horizontalWeight = param.horizontalWeight
            layout.verticalChainStyle = param.verticalChainStyle
            layout.horizontalChainStyle = param.horizontalChainStyle
            layout.constrainedWidth = param.constrainedWidth
            layout.constrainedHeight = param.constrainedHeight
            layout.widthDefault = param.matchConstraintDefaultWidth
            layout.heightDefault = param.matchConstraintDefaultHeight
            layout.widthMax = param.matchConstraintMaxWidth
            layout.heightMax = param.matchConstraintMaxHeight
            layout.widthMin = param.matchConstraintMinWidth
            layout.heightMin = param.matchConstraintMinHeight
            layout.widthPercent = param.matchConstraintPercentWidth
            layout.heightPercent = param.matchConstraintPercentHeight
            layout.mConstraintTag = param.constraintTag
            layout.goneTopMargin = param.goneTopMargin
            layout.goneBottomMargin = param.goneBottomMargin
            layout.goneLeftMargin = param.goneLeftMargin
            layout.goneRightMargin = param.goneRightMargin
            layout.goneStartMargin = param.goneStartMargin
            layout.goneEndMargin = param.goneEndMargin
            layout.goneBaselineMargin = param.goneBaselineMargin
            layout.mWrapBehavior = param.wrapBehaviorInParent
            layout.endMargin = param.getMarginEnd()
            layout.startMargin = param.getMarginStart()
        }

        /**
         * apply ConstraintSet to the layout params
         * @param param
         */
        fun applyTo(param: ConstraintLayout.LayoutParams) {
            param.leftToLeft = layout.leftToLeft
            param.leftToRight = layout.leftToRight
            param.rightToLeft = layout.rightToLeft
            param.rightToRight = layout.rightToRight
            param.topToTop = layout.topToTop
            param.topToBottom = layout.topToBottom
            param.bottomToTop = layout.bottomToTop
            param.bottomToBottom = layout.bottomToBottom
            param.baselineToBaseline = layout.baselineToBaseline
            param.baselineToTop = layout.baselineToTop
            param.baselineToBottom = layout.baselineToBottom
            param.startToEnd = layout.startToEnd
            param.startToStart = layout.startToStart
            param.endToStart = layout.endToStart
            param.endToEnd = layout.endToEnd
            param.leftMargin = layout.leftMargin
            param.rightMargin = layout.rightMargin
            param.topMargin = layout.topMargin
            param.bottomMargin = layout.bottomMargin
            param.goneStartMargin = layout.goneStartMargin
            param.goneEndMargin = layout.goneEndMargin
            param.goneTopMargin = layout.goneTopMargin
            param.goneBottomMargin = layout.goneBottomMargin
            param.horizontalBias = layout.horizontalBias
            param.verticalBias = layout.verticalBias
            param.circleConstraint = layout.circleConstraint
            param.circleRadius = layout.circleRadius
            param.circleAngle = layout.circleAngle
            param.dimensionRatio = layout.dimensionRatio
            param.editorAbsoluteX = layout.editorAbsoluteX
            param.editorAbsoluteY = layout.editorAbsoluteY
            param.verticalWeight = layout.verticalWeight
            param.horizontalWeight = layout.horizontalWeight
            param.verticalChainStyle = layout.verticalChainStyle
            param.horizontalChainStyle = layout.horizontalChainStyle
            param.constrainedWidth = layout.constrainedWidth
            param.constrainedHeight = layout.constrainedHeight
            param.matchConstraintDefaultWidth = layout.widthDefault
            param.matchConstraintDefaultHeight = layout.heightDefault
            param.matchConstraintMaxWidth = layout.widthMax
            param.matchConstraintMaxHeight = layout.heightMax
            param.matchConstraintMinWidth = layout.widthMin
            param.matchConstraintMinHeight = layout.heightMin
            param.matchConstraintPercentWidth = layout.widthPercent
            param.matchConstraintPercentHeight = layout.heightPercent
            param.orientation = layout.orientation
            param.guidePercent = layout.guidePercent
            param.guideBegin = layout.guideBegin
            param.guideEnd = layout.guideEnd
            param.width = layout.mWidth
            param.height = layout.mHeight
            if (layout.mConstraintTag != null) {
                param.constraintTag = layout.mConstraintTag
            }
            param.wrapBehaviorInParent = layout.mWrapBehavior
            param.setMarginStart(layout.startMargin)
            param.setMarginEnd(layout.endMargin)
            param.validate()
        }
    }

    /**
     * Copy the constraints from a layout.
     *
     * @param context            the context for the layout inflation
     * @param constraintLayoutId the id of the layout file
     */
    fun clone(context: TContext, constraintLayoutId: String) {
        clone(ConstraintLayout(context, mutableMapOf(), context.getLayoutInflater().inflate(constraintLayoutId, null)))
    }

    /**
     * Copy the constraints from a layout.
     *
     * @param set constraint set to copy
     */
    fun clone(set: ConstraintSet?) {
        if(set == null)
            return
        mConstraints.clear()
        for (key in set.mConstraints.keys) {
            val constraint = set.mConstraints[key] ?: continue
            mConstraints[key] = constraint.clone()
        }
    }

    /**
     * Copy the layout parameters of a ConstraintLayout.
     *
     * @param constraintLayout The ConstraintLayout to be copied
     */
    fun clone(constraintLayout: ConstraintLayout) {
        val count: Int = constraintLayout.self.getChildCount()
        mConstraints.clear()
        for (i in 0 until count) {
            val view: TView = constraintLayout.self.getChildAt(i)
            val id = view.getId()
            if (isForceId && id == "") {
                throw RuntimeException(
                    "All children of ConstraintLayout must "
                            + "have ids to use ConstraintSet"
                )
            }
            if (!mConstraints.containsKey(id)) {
                mConstraints[id] = Constraint()
            }
            val constraint = mConstraints[id] ?: continue
            constraint.mCustomConstraints =
                ConstraintAttribute.extractAttributes(mSavedAttributes, view)
            constraint.fillFrom(id, view.getLayoutParams() as ConstraintLayout.LayoutParams)
            constraint.propertySet.visibility = view.getVisibility()
            constraint.propertySet.alpha = view.getAlpha()
            constraint.transform.rotation = view.getRotation()
            constraint.transform.rotationX = view.getRotationX()
            constraint.transform.rotationY = view.getRotationY()
            constraint.transform.scaleX = view.getScaleX()
            constraint.transform.scaleY = view.getScaleY()
            val pivotX: Float = view.getPivotX() // we assume it is not set if set to 0.0
            val pivotY: Float = view.getPivotY() // we assume it is not set if set to 0.0
            if (pivotX.toDouble() != 0.0 || pivotY.toDouble() != 0.0) {
                constraint.transform.transformPivotX = pivotX
                constraint.transform.transformPivotY = pivotY
            }
            constraint.transform.translationX = view.getTranslationX()
            constraint.transform.translationY = view.getTranslationY()
            constraint.transform.translationZ = view.getTranslationZ()
            if (constraint.transform.applyElevation) {
                constraint.transform.elevation = view.getElevation()
            }
            if (view.getParentType() is Barrier) {
                val barrier: Barrier = view.getParentType() as Barrier
                constraint.layout.mBarrierAllowsGoneWidgets = barrier.allowsGoneWidget
                constraint.layout.mReferenceIds = barrier.referencedIds
                constraint.layout.mBarrierDirection = barrier.type
                constraint.layout.mBarrierMargin = barrier.margin
            }
        }
    }

    /**
     * Copy the layout parameters of a ConstraintLayout.
     *
     * @param constraints The ConstraintLayout to be copied
     */
    fun clone(constraints: Constraints) {
        val count: Int = constraints.self.getChildCount()
        mConstraints.clear()
        for (i in 0 until count) {
            val view: TView = constraints.self.getChildAt(i)
            val param: Constraints.LayoutParams = view.getLayoutParams() as Constraints.LayoutParams
            val id = view.getId()
            if (isForceId && id == "") {
                throw RuntimeException(
                    "All children of ConstraintLayout "
                            + "must have ids to use ConstraintSet"
                )
            }
            if (!mConstraints.containsKey(id)) {
                mConstraints[id] = Constraint()
            }
            val constraint = mConstraints[id] ?: continue
            if (view.getParentType() is ConstraintHelper) {
                constraint.fillFromConstraints(view.getParentType() as ConstraintHelper, id, param)
            }
            constraint.fillFromConstraints(id, param)
        }
    }

    /**
     * Apply the constraints to a ConstraintLayout.
     *
     * @param constraintLayout to be modified
     */
    fun applyTo(constraintLayout: ConstraintLayout) {
        applyToInternal(constraintLayout, true)
        constraintLayout.setConstraintSet(null)
        constraintLayout.requestLayout(constraintLayout.self)
    }

    /**
     * Apply the constraints to a ConstraintLayout.
     *
     * @param constraintLayout to be modified
     */
    fun applyToWithoutCustom(constraintLayout: ConstraintLayout) {
        applyToInternal(constraintLayout, false)
        constraintLayout.setConstraintSet(null)
    }

    /**
     * Apply custom attributes alone
     *
     * @param constraintLayout
     */
    fun applyCustomAttributes(constraintLayout: ConstraintLayout) {
        val count: Int = constraintLayout.self.getChildCount()
        for (i in 0 until count) {
            val view: TView = constraintLayout.self.getChildAt(i)
            val id = view.getId()
            if (!mConstraints.containsKey(id)) {
                Log.w(TAG, "id unknown " + Debug.getName(view))
                continue
            }
            if (isForceId && id == "") {
                throw RuntimeException(
                    "All children of ConstraintLayout "
                            + "must have ids to use ConstraintSet"
                )
            }
            if (mConstraints.containsKey(id)) {
                val constraint = mConstraints[id] ?: continue
                ConstraintAttribute.setAttributes(view, constraint.mCustomConstraints)
            }
        }
    }

    /**
     * Apply Layout to Helper widget
     *
     * @param helper
     * @param child
     * @param layoutParams
     * @param mapIdToWidget
     */
    fun applyToHelper(
        helper: ConstraintHelper, child: ConstraintWidget,
        layoutParams: ConstraintLayout.LayoutParams,
        mapIdToWidget: MutableMap<String, ConstraintWidget>
    ) {
        val id = helper.self.getId()
        if (mConstraints.containsKey(id)) {
            val constraint = mConstraints[id]
            if (constraint != null && child is HelperWidget) {
                val helperWidget: HelperWidget = child as HelperWidget
                helper.loadParameters(constraint, helperWidget, layoutParams, mapIdToWidget)
            }
        }
    }

    /**
     * Fill in a ConstraintLayout LayoutParam based on the id.
     *
     * @param id           Id of the view
     * @param layoutParams LayoutParams to be filled
     */
    fun applyToLayoutParams(id: String, layoutParams: ConstraintLayout.LayoutParams) {
        if (mConstraints.containsKey(id)) {
            val constraint = mConstraints[id]
            constraint?.applyTo(layoutParams)
        }
    }

    /**
     * Used to set constraints when used by constraint layout
     */
    fun applyToInternal(constraintLayout: ConstraintLayout, applyPostLayout: Boolean) {
        val count: Int = constraintLayout.self.getChildCount()
        val used = mConstraints.keys.toMutableSet()
        for (i in 0 until count) {
            val view: TView = constraintLayout.self.getChildAt(i)
            val id = view.getId()
            if (!mConstraints.containsKey(id)) {
                Log.w(TAG, "id unknown " + Debug.getName(view))
                continue
            }
            if (isForceId && id == "") {
                throw RuntimeException(
                    "All children of ConstraintLayout "
                            + "must have ids to use ConstraintSet"
                )
            }
            if (id == "") {
                continue
            }
            if (mConstraints.containsKey(id)) {
                used.remove(id)
                val constraint = mConstraints[id] ?: continue
                if (view.getParentType() is Barrier) {
                    constraint.layout.mHelperType = BARRIER_TYPE
                    val barrier: Barrier = view.getParentType() as Barrier
                    barrier.self.setId(id)
                    barrier.type = (constraint.layout.mBarrierDirection)
                    barrier.margin = (constraint.layout.mBarrierMargin)
                    barrier.allowsGoneWidget = (constraint.layout.mBarrierAllowsGoneWidgets)
                    if (constraint.layout.mReferenceIds != null) {
                        barrier.referencedIds = (constraint.layout.mReferenceIds)
                    } else if (constraint.layout.mReferenceIdString != null) {
                        constraint.layout.mReferenceIds = convertReferenceString(
                            barrier.self,
                            constraint.layout.mReferenceIdString
                        )
                        barrier.referencedIds = (constraint.layout.mReferenceIds)
                    }
                }
                val param: ConstraintLayout.LayoutParams = view.getLayoutParams() as ConstraintLayout.LayoutParams
                param.validate()
                constraint.applyTo(param)
                if (applyPostLayout) {
                    ConstraintAttribute.setAttributes(view, constraint.mCustomConstraints)
                }
                view.setLayoutParams(param)
                if (constraint.propertySet.mVisibilityMode == VISIBILITY_MODE_NORMAL) {
                    view.setVisibility(constraint.propertySet.visibility)
                }
                view.setAlpha(constraint.propertySet.alpha)
                view.setRotation(constraint.transform.rotation)
                view.setRotationX(constraint.transform.rotationX)
                view.setRotationY(constraint.transform.rotationY)
                view.setScaleX(constraint.transform.scaleX)
                view.setScaleY(constraint.transform.scaleY)
                if (constraint.transform.transformPivotTarget != UNSET_ID) {
                    val layout: TView = view.getParent() as TView
                    val center: TView? = layout.findViewById(
                        constraint.transform.transformPivotTarget
                    )
                    if (center != null) {
                        val cy: Float = (center.getTop() + center.getBottom()) / 2.0f
                        val cx: Float = (center.getLeft() + center.getRight()) / 2.0f
                        if (view.getRight() - view.getLeft() > 0
                            && view.getBottom() - view.getTop() > 0
                        ) {
                            val px: Float = cx - view.getLeft()
                            val py: Float = cy - view.getTop()
                            view.setPivotX(px)
                            view.setPivotY(py)
                        }
                    }
                } else {
                    if (!Float.isNaN(constraint.transform.transformPivotX)) {
                        view.setPivotX(constraint.transform.transformPivotX)
                    }
                    if (!Float.isNaN(constraint.transform.transformPivotY)) {
                        view.setPivotY(constraint.transform.transformPivotY)
                    }
                }
                view.setTranslationX(constraint.transform.translationX)
                view.setTranslationY(constraint.transform.translationY)
                view.setTranslationZ(constraint.transform.translationZ)
                if (constraint.transform.applyElevation) {
                    view.setElevation(constraint.transform.elevation)
                }
            } else {
                Log.v(TAG, "WARNING NO CONSTRAINTS for view $id")
            }
        }
        for (id in used) {
            val constraint = mConstraints[id] ?: continue
            if (constraint.layout.mHelperType == BARRIER_TYPE) {
                val barrier = Barrier(constraintLayout.context, constraintLayout.context.createView())
                barrier.self.setId(id)
                if (constraint.layout.mReferenceIds != null) {
                    barrier.referencedIds = (constraint.layout.mReferenceIds)
                } else if (constraint.layout.mReferenceIdString != null) {
                    constraint.layout.mReferenceIds = convertReferenceString(
                        barrier.self,
                        constraint.layout.mReferenceIdString
                    )
                    barrier.referencedIds = (constraint.layout.mReferenceIds)
                }
                barrier.type = (constraint.layout.mBarrierDirection)
                barrier.margin = (constraint.layout.mBarrierMargin)
                val param: ConstraintLayout.LayoutParams = constraintLayout
                    .generateDefaultLayoutParams()
                barrier.validateParams()
                constraint.applyTo(param)
                constraintLayout.self.addView(barrier.self, param)
            }
            if (constraint.layout.mIsGuideline) {
                val g = Guideline(constraintLayout.context, constraintLayout.context.createView())
                g.self.setId(id)
                val param: ConstraintLayout.LayoutParams =
                    constraintLayout.generateDefaultLayoutParams()
                constraint.applyTo(param)
                constraintLayout.self.addView(g.self, param)
            }
        }
        for (i in 0 until count) {
            val view: TView = constraintLayout.self.getChildAt(i)
            if (view.getParentType() is ConstraintHelper) {
                (view.getParentType() as ConstraintHelper).applyLayoutFeaturesInConstraintSet(constraintLayout)
            }
        }
    }

    /**
     * Center widget between the other two widgets.
     * (for sides see: [{][.TOP]
     */
    fun center(
        centerID: String,
        firstID: String, firstSide: Int, firstMargin: Int,
        secondId: String, secondSide: Int, secondMargin: Int,
        bias: Float
    ) {
        // Error checking
        require(firstMargin >= 0) { "margin must be > 0" }
        require(secondMargin >= 0) { "margin must be > 0" }
        require(!(bias <= 0 || bias > 1)) { "bias must be between 0 and 1 inclusive" }
        if (firstSide == LEFT || firstSide == RIGHT) {
            connect(centerID, LEFT, firstID, firstSide, firstMargin)
            connect(centerID, RIGHT, secondId, secondSide, secondMargin)
            val constraint = mConstraints[centerID]
            if (constraint != null) {
                constraint.layout.horizontalBias = bias
            }
        } else if (firstSide == START || firstSide == END) {
            connect(centerID, START, firstID, firstSide, firstMargin)
            connect(centerID, END, secondId, secondSide, secondMargin)
            val constraint = mConstraints[centerID]
            if (constraint != null) {
                constraint.layout.horizontalBias = bias
            }
        } else {
            connect(centerID, TOP, firstID, firstSide, firstMargin)
            connect(centerID, BOTTOM, secondId, secondSide, secondMargin)
            val constraint = mConstraints[centerID]
            if (constraint != null) {
                constraint.layout.verticalBias = bias
            }
        }
    }

    /**
     * Centers the widget horizontally to the left and right side on another widgets sides.
     * (for sides see: [{][.START]
     */
    fun centerHorizontally(
        centerID: String,
        leftId: String,
        leftSide: Int,
        leftMargin: Int,
        rightId: String,
        rightSide: Int,
        rightMargin: Int,
        bias: Float
    ) {
        connect(centerID, LEFT, leftId, leftSide, leftMargin)
        connect(centerID, RIGHT, rightId, rightSide, rightMargin)
        val constraint = mConstraints[centerID]
        if (constraint != null) {
            constraint.layout.horizontalBias = bias
        }
    }

    /**
     * Centers the widgets horizontally to the left and right side on another widgets sides.
     * (for sides see: [.START], [.END],
     * [.LEFT], [.RIGHT])
     *
     * @param centerID    ID of widget to be centered
     * @param startId     The Id of the widget on the start side (left in non rtl languages)
     * @param startSide   The side of the startId widget to connect to
     * @param startMargin The margin on the start side
     * @param endId       The Id of the widget on the start side (left in non rtl languages)
     * @param endSide     The side of the endId widget to connect to
     * @param endMargin   The margin on the end side
     * @param bias        The ratio of the space on the start vs end side 0.5 is centered (default)
     */
    fun centerHorizontallyRtl(
        centerID: String, startId: String, startSide: Int, startMargin: Int,
        endId: String, endSide: Int, endMargin: Int, bias: Float
    ) {
        connect(centerID, START, startId, startSide, startMargin)
        connect(centerID, END, endId, endSide, endMargin)
        val constraint = mConstraints[centerID]
        if (constraint != null) {
            constraint.layout.horizontalBias = bias
        }
    }

    /**
     * Centers the widgets vertically to the top and bottom side on another widgets sides.
     * (for sides see: [{][.TOP]
     */
    fun centerVertically(
        centerID: String, topId: String, topSide: Int, topMargin: Int, bottomId: String,
        bottomSide: Int, bottomMargin: Int, bias: Float
    ) {
        connect(centerID, TOP, topId, topSide, topMargin)
        connect(centerID, BOTTOM, bottomId, bottomSide, bottomMargin)
        val constraint = mConstraints[centerID]
        if (constraint != null) {
            constraint.layout.verticalBias = bias
        }
    }

    /**
     * Spaces a set of widgets vertically between the view topId and bottomId.
     * Widgets can be spaced with weights.
     * This operation sets all the related margins to 0.
     *
     *
     * (for sides see: [{][.TOP]
     */
    fun createVerticalChain(
        topId: String,
        topSide: Int,
        bottomId: String,
        bottomSide: Int,
        chainIds: Array<String>,
        weights: FloatArray?,
        style: Int
    ) {
        require(chainIds.size >= 2) { "must have 2 or more widgets in a chain" }
        require(!(weights != null && weights.size != chainIds.size)) { "must have 2 or more widgets in a chain" }
        if (weights != null) {
            get(chainIds[0])!!.layout.verticalWeight = weights[0]
        }
        get(chainIds[0])!!.layout.verticalChainStyle = style
        connect(chainIds[0], TOP, topId, topSide, 0)
        for (i in 1 until chainIds.size) {
            connect(chainIds[i], TOP, chainIds[i - 1], BOTTOM, 0)
            connect(chainIds[i - 1], BOTTOM, chainIds[i], TOP, 0)
            if (weights != null) {
                get(chainIds[i])!!.layout.verticalWeight = weights[i]
            }
        }
        connect(chainIds[chainIds.size - 1], BOTTOM, bottomId, bottomSide, 0)
    }

    /**
     * Spaces a set of widgets horizontally between the view startID and endId.
     * Widgets can be spaced with weights.
     * This operation sets all the related margins to 0.
     *
     *
     * (for sides see: [{][.START]
     */
    fun createHorizontalChain(
        leftId: String,
        leftSide: Int,
        rightId: String,
        rightSide: Int,
        chainIds: Array<String>,
        weights: FloatArray?,
        style: Int
    ) {
        createHorizontalChain(
            leftId, leftSide, rightId, rightSide,
            chainIds, weights, style, LEFT, RIGHT
        )
    }

    /**
     * Spaces a set of widgets horizontal between the view startID and endId.
     * Widgets can be spaced with weights.
     * (for sides see: [{][.START]
     */
    fun createHorizontalChainRtl(
        startId: String,
        startSide: Int,
        endId: String,
        endSide: Int,
        chainIds: Array<String>,
        weights: FloatArray?,
        style: Int
    ) {
        createHorizontalChain(
            startId, startSide, endId, endSide,
            chainIds, weights, style, START, END
        )
    }

    private fun createHorizontalChain(
        leftId: String,
        leftSide: Int,
        rightId: String,
        rightSide: Int,
        chainIds: Array<String>,
        weights: FloatArray?,
        style: Int, left: Int, right: Int
    ) {
        require(chainIds.size >= 2) { "must have 2 or more widgets in a chain" }
        require(!(weights != null && weights.size != chainIds.size)) { "must have 2 or more widgets in a chain" }
        if (weights != null) {
            get(chainIds[0])!!.layout.horizontalWeight = weights[0]
        }
        get(chainIds[0])!!.layout.horizontalChainStyle = style
        connect(chainIds[0], left, leftId, leftSide, UNSET)
        for (i in 1 until chainIds.size) {
            connect(chainIds[i], left, chainIds[i - 1], right, UNSET)
            connect(chainIds[i - 1], right, chainIds[i], left, UNSET)
            if (weights != null) {
                get(chainIds[i])!!.layout.horizontalWeight = weights[i]
            }
        }
        connect(
            chainIds[chainIds.size - 1], right, rightId, rightSide,
            UNSET
        )
    }

    /**
     * Create a constraint between two widgets.
     * (for sides see: [{][.TOP]
     */
    fun connect(startID: String, startSide: Int, endID: String, endSide: Int, margin: Int) {
        if (!mConstraints.containsKey(startID)) {
            mConstraints[startID] = Constraint()
        }
        val constraint = mConstraints[startID] ?: return
        when (startSide) {
            LEFT -> {
                if (endSide == LEFT) {
                    constraint.layout.leftToLeft = endID
                    constraint.layout.leftToRight = Layout.UNSET_ID
                } else if (endSide == RIGHT) {
                    constraint.layout.leftToRight = endID
                    constraint.layout.leftToLeft = Layout.UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "Left to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                constraint.layout.leftMargin = margin
            }
            RIGHT -> {
                if (endSide == LEFT) {
                    constraint.layout.rightToLeft = endID
                    constraint.layout.rightToRight = Layout.UNSET_ID
                } else if (endSide == RIGHT) {
                    constraint.layout.rightToRight = endID
                    constraint.layout.rightToLeft = Layout.UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                constraint.layout.rightMargin = margin
            }
            TOP -> {
                if (endSide == TOP) {
                    constraint.layout.topToTop = endID
                    constraint.layout.topToBottom = Layout.UNSET_ID
                    constraint.layout.baselineToBaseline = Layout.UNSET_ID
                    constraint.layout.baselineToTop = Layout.UNSET_ID
                    constraint.layout.baselineToBottom = Layout.UNSET_ID
                } else if (endSide == BOTTOM) {
                    constraint.layout.topToBottom = endID
                    constraint.layout.topToTop = Layout.UNSET_ID
                    constraint.layout.baselineToBaseline = Layout.UNSET_ID
                    constraint.layout.baselineToTop = Layout.UNSET_ID
                    constraint.layout.baselineToBottom = Layout.UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                constraint.layout.topMargin = margin
            }
            BOTTOM -> {
                if (endSide == BOTTOM) {
                    constraint.layout.bottomToBottom = endID
                    constraint.layout.bottomToTop = Layout.UNSET_ID
                    constraint.layout.baselineToBaseline = Layout.UNSET_ID
                    constraint.layout.baselineToTop = Layout.UNSET_ID
                    constraint.layout.baselineToBottom = Layout.UNSET_ID
                } else if (endSide == TOP) {
                    constraint.layout.bottomToTop = endID
                    constraint.layout.bottomToBottom = Layout.UNSET_ID
                    constraint.layout.baselineToBaseline = Layout.UNSET_ID
                    constraint.layout.baselineToTop = Layout.UNSET_ID
                    constraint.layout.baselineToBottom = Layout.UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                constraint.layout.bottomMargin = margin
            }
            BASELINE -> if (endSide == BASELINE) {
                constraint.layout.baselineToBaseline = endID
                constraint.layout.bottomToBottom = Layout.UNSET_ID
                constraint.layout.bottomToTop = Layout.UNSET_ID
                constraint.layout.topToTop = Layout.UNSET_ID
                constraint.layout.topToBottom = Layout.UNSET_ID
            } else if (endSide == TOP) {
                constraint.layout.baselineToTop = endID
                constraint.layout.bottomToBottom = Layout.UNSET_ID
                constraint.layout.bottomToTop = Layout.UNSET_ID
                constraint.layout.topToTop = Layout.UNSET_ID
                constraint.layout.topToBottom = Layout.UNSET_ID
            } else if (endSide == BOTTOM) {
                constraint.layout.baselineToBottom = endID
                constraint.layout.bottomToBottom = Layout.UNSET_ID
                constraint.layout.bottomToTop = Layout.UNSET_ID
                constraint.layout.topToTop = Layout.UNSET_ID
                constraint.layout.topToBottom = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "right to "
                            + sideToString(endSide) + " undefined"
                )
            }
            START -> {
                if (endSide == START) {
                    constraint.layout.startToStart = endID
                    constraint.layout.startToEnd = Layout.UNSET_ID
                } else if (endSide == END) {
                    constraint.layout.startToEnd = endID
                    constraint.layout.startToStart = Layout.UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                constraint.layout.startMargin = margin
            }
            END -> {
                if (endSide == END) {
                    constraint.layout.endToEnd = endID
                    constraint.layout.endToStart = Layout.UNSET_ID
                } else if (endSide == START) {
                    constraint.layout.endToStart = endID
                    constraint.layout.endToEnd = Layout.UNSET_ID
                } else {
                    throw IllegalArgumentException(
                        "right to "
                                + sideToString(endSide) + " undefined"
                    )
                }
                constraint.layout.endMargin = margin
            }
            else -> throw IllegalArgumentException(
                sideToString(startSide) + " to " + sideToString(endSide) + " unknown"
            )
        }
    }

    /**
     * Create a constraint between two widgets.
     * (for sides see: [{][.TOP]
     */
    fun connect(startID: String, startSide: Int, endID: String, endSide: Int) {
        if (!mConstraints.containsKey(startID)) {
            mConstraints[startID] = Constraint()
        }
        val constraint = mConstraints[startID] ?: return
        when (startSide) {
            LEFT -> if (endSide == LEFT) {
                constraint.layout.leftToLeft = endID
                constraint.layout.leftToRight = Layout.UNSET_ID
            } else if (endSide == RIGHT) {
                constraint.layout.leftToRight = endID
                constraint.layout.leftToLeft = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "left to "
                            + sideToString(endSide) + " undefined"
                )
            }
            RIGHT -> if (endSide == LEFT) {
                constraint.layout.rightToLeft = endID
                constraint.layout.rightToRight = Layout.UNSET_ID
            } else if (endSide == RIGHT) {
                constraint.layout.rightToRight = endID
                constraint.layout.rightToLeft = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "right to "
                            + sideToString(endSide) + " undefined"
                )
            }
            TOP -> if (endSide == TOP) {
                constraint.layout.topToTop = endID
                constraint.layout.topToBottom = Layout.UNSET_ID
                constraint.layout.baselineToBaseline = Layout.UNSET_ID
                constraint.layout.baselineToTop = Layout.UNSET_ID
                constraint.layout.baselineToBottom = Layout.UNSET_ID
            } else if (endSide == BOTTOM) {
                constraint.layout.topToBottom = endID
                constraint.layout.topToTop = Layout.UNSET_ID
                constraint.layout.baselineToBaseline = Layout.UNSET_ID
                constraint.layout.baselineToTop = Layout.UNSET_ID
                constraint.layout.baselineToBottom = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "right to "
                            + sideToString(endSide) + " undefined"
                )
            }
            BOTTOM -> if (endSide == BOTTOM) {
                constraint.layout.bottomToBottom = endID
                constraint.layout.bottomToTop = Layout.UNSET_ID
                constraint.layout.baselineToBaseline = Layout.UNSET_ID
                constraint.layout.baselineToTop = Layout.UNSET_ID
                constraint.layout.baselineToBottom = Layout.UNSET_ID
            } else if (endSide == TOP) {
                constraint.layout.bottomToTop = endID
                constraint.layout.bottomToBottom = Layout.UNSET_ID
                constraint.layout.baselineToBaseline = Layout.UNSET_ID
                constraint.layout.baselineToTop = Layout.UNSET_ID
                constraint.layout.baselineToBottom = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "right to "
                            + sideToString(endSide) + " undefined"
                )
            }
            BASELINE -> if (endSide == BASELINE) {
                constraint.layout.baselineToBaseline = endID
                constraint.layout.bottomToBottom = Layout.UNSET_ID
                constraint.layout.bottomToTop = Layout.UNSET_ID
                constraint.layout.topToTop = Layout.UNSET_ID
                constraint.layout.topToBottom = Layout.UNSET_ID
            } else if (endSide == TOP) {
                constraint.layout.baselineToTop = endID
                constraint.layout.bottomToBottom = Layout.UNSET_ID
                constraint.layout.bottomToTop = Layout.UNSET_ID
                constraint.layout.topToTop = Layout.UNSET_ID
                constraint.layout.topToBottom = Layout.UNSET_ID
            } else if (endSide == BOTTOM) {
                constraint.layout.baselineToBottom = endID
                constraint.layout.bottomToBottom = Layout.UNSET_ID
                constraint.layout.bottomToTop = Layout.UNSET_ID
                constraint.layout.topToTop = Layout.UNSET_ID
                constraint.layout.topToBottom = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "right to "
                            + sideToString(endSide) + " undefined"
                )
            }
            START -> if (endSide == START) {
                constraint.layout.startToStart = endID
                constraint.layout.startToEnd = Layout.UNSET_ID
            } else if (endSide == END) {
                constraint.layout.startToEnd = endID
                constraint.layout.startToStart = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "right to "
                            + sideToString(endSide) + " undefined"
                )
            }
            END -> if (endSide == END) {
                constraint.layout.endToEnd = endID
                constraint.layout.endToStart = Layout.UNSET_ID
            } else if (endSide == START) {
                constraint.layout.endToStart = endID
                constraint.layout.endToEnd = Layout.UNSET_ID
            } else {
                throw IllegalArgumentException(
                    "right to "
                            + sideToString(endSide) + " undefined"
                )
            }
            else -> throw IllegalArgumentException(
                sideToString(startSide) + " to " + sideToString(endSide) + " unknown"
            )
        }
    }

    /**
     * Centers the view horizontally relative to toView's position.
     *
     * @param viewId ID of view to center Horizontally
     * @param toView ID of view to center on (or in)
     */
    fun centerHorizontally(viewId: String, toView: String) {
        if (toView == PARENT_ID) {
            center(
                viewId, PARENT_ID, LEFT, 0, PARENT_ID,
                RIGHT, 0, 0.5f
            )
        } else {
            center(
                viewId, toView, RIGHT, 0, toView,
                LEFT, 0, 0.5f
            )
        }
    }

    /**
     * Centers the view horizontally relative to toView's position.
     *
     * @param viewId ID of view to center Horizontally
     * @param toView ID of view to center on (or in)
     */
    fun centerHorizontallyRtl(viewId: String, toView: String) {
        if (toView == PARENT_ID) {
            center(
                viewId, PARENT_ID, START, 0, PARENT_ID,
                END, 0, 0.5f
            )
        } else {
            center(
                viewId, toView, END, 0, toView,
                START, 0, 0.5f
            )
        }
    }

    /**
     * Centers the view vertically relative to toView's position.
     *
     * @param viewId ID of view to center Horizontally
     * @param toView ID of view to center on (or in)
     */
    fun centerVertically(viewId: String, toView: String) {
        if (toView == PARENT_ID) {
            center(
                viewId, PARENT_ID, TOP, 0, PARENT_ID,
                BOTTOM, 0, 0.5f
            )
        } else {
            center(
                viewId, toView, BOTTOM, 0, toView, TOP,
                0, 0.5f
            )
        }
    }

    /**
     * Remove all constraints from this view.
     *
     * @param viewId ID of view to remove all connections to
     */
    fun clear(viewId: String) {
        mConstraints.remove(viewId)
    }

    /**
     * Remove a constraint from this view.
     *
     * @param viewId ID of view to center on (or in)
     * @param anchor the Anchor to remove constraint from
     */
    fun clear(viewId: String, anchor: Int) {
        if (mConstraints.containsKey(viewId)) {
            val constraint = mConstraints[viewId] ?: return
            when (anchor) {
                LEFT -> {
                    constraint.layout.leftToRight = Layout.UNSET_ID
                    constraint.layout.leftToLeft = Layout.UNSET_ID
                    constraint.layout.leftMargin = Layout.UNSET
                    constraint.layout.goneLeftMargin = Layout.UNSET_GONE_MARGIN
                }
                RIGHT -> {
                    constraint.layout.rightToRight = Layout.UNSET_ID
                    constraint.layout.rightToLeft = Layout.UNSET_ID
                    constraint.layout.rightMargin = Layout.UNSET
                    constraint.layout.goneRightMargin = Layout.UNSET_GONE_MARGIN
                }
                TOP -> {
                    constraint.layout.topToBottom = Layout.UNSET_ID
                    constraint.layout.topToTop = Layout.UNSET_ID
                    constraint.layout.topMargin = 0
                    constraint.layout.goneTopMargin = Layout.UNSET_GONE_MARGIN
                }
                BOTTOM -> {
                    constraint.layout.bottomToTop = Layout.UNSET_ID
                    constraint.layout.bottomToBottom = Layout.UNSET_ID
                    constraint.layout.bottomMargin = 0
                    constraint.layout.goneBottomMargin = Layout.UNSET_GONE_MARGIN
                }
                BASELINE -> {
                    constraint.layout.baselineToBaseline = Layout.UNSET_ID
                    constraint.layout.baselineToTop = Layout.UNSET_ID
                    constraint.layout.baselineToBottom = Layout.UNSET_ID
                    constraint.layout.baselineMargin = 0
                    constraint.layout.goneBaselineMargin = Layout.UNSET_GONE_MARGIN
                }
                START -> {
                    constraint.layout.startToEnd = Layout.UNSET_ID
                    constraint.layout.startToStart = Layout.UNSET_ID
                    constraint.layout.startMargin = 0
                    constraint.layout.goneStartMargin = Layout.UNSET_GONE_MARGIN
                }
                END -> {
                    constraint.layout.endToStart = Layout.UNSET_ID
                    constraint.layout.endToEnd = Layout.UNSET_ID
                    constraint.layout.endMargin = 0
                    constraint.layout.goneEndMargin = Layout.UNSET_GONE_MARGIN
                }
                CIRCLE_REFERENCE -> {
                    constraint.layout.circleAngle = Layout.UNSET.toFloat()
                    constraint.layout.circleRadius = Layout.UNSET
                    constraint.layout.circleConstraint = Layout.UNSET_ID
                }
                else -> throw IllegalArgumentException("unknown constraint")
            }
        }
    }

    /**
     * Sets the margin.
     *
     * @param viewId ID of view to adjust the margin on
     * @param anchor The side to adjust the margin on
     * @param value  The new value for the margin
     */
    fun setMargin(viewId: String, anchor: Int, value: Int) {
        val constraint = get(viewId)
        when (anchor) {
            LEFT -> constraint!!.layout.leftMargin = value
            RIGHT -> constraint!!.layout.rightMargin = value
            TOP -> constraint!!.layout.topMargin = value
            BOTTOM -> constraint!!.layout.bottomMargin = value
            BASELINE -> constraint!!.layout.baselineMargin = value
            START -> constraint!!.layout.startMargin = value
            END -> constraint!!.layout.endMargin = value
            else -> throw IllegalArgumentException("unknown constraint")
        }
    }

    /**
     * Sets the gone margin.
     *
     * @param viewId ID of view to adjust the margin on
     * @param anchor The side to adjust the margin on
     * @param value  The new value for the margin
     */
    fun setGoneMargin(viewId: String, anchor: Int, value: Int) {
        val constraint = get(viewId)
        when (anchor) {
            LEFT -> constraint!!.layout.goneLeftMargin = value
            RIGHT -> constraint!!.layout.goneRightMargin = value
            TOP -> constraint!!.layout.goneTopMargin = value
            BOTTOM -> constraint!!.layout.goneBottomMargin = value
            BASELINE -> constraint!!.layout.goneBaselineMargin = value
            START -> constraint!!.layout.goneStartMargin = value
            END -> constraint!!.layout.goneEndMargin = value
            else -> throw IllegalArgumentException("unknown constraint")
        }
    }

    /**
     * Adjust the horizontal bias of the view (used with views constrained on left and right).
     *
     * @param viewId ID of view to adjust the horizontal
     * @param bias   the new bias 0.5 is in the middle
     */
    fun setHorizontalBias(viewId: String, bias: Float) {
        get(viewId)!!.layout.horizontalBias = bias
    }

    /**
     * Adjust the vertical bias of the view (used with views constrained on left and right).
     *
     * @param viewId ID of view to adjust the vertical
     * @param bias   the new bias 0.5 is in the middle
     */
    fun setVerticalBias(viewId: String, bias: Float) {
        get(viewId)!!.layout.verticalBias = bias
    }

    /**
     * Constrains the views aspect ratio.
     * For Example a HD screen is 16 by 9 = 16/(float)9 = 1.777f.
     *
     * @param viewId ID of view to constrain
     * @param ratio  The ratio of the width to height (width / height)
     */
    fun setDimensionRatio(viewId: String, ratio: String?) {
        get(viewId)!!.layout.dimensionRatio = ratio
    }

    /**
     * Adjust the visibility of a view.
     *
     * @param viewId     ID of view to adjust the vertical
     * @param visibility the visibility
     */
    fun setVisibility(viewId: String, visibility: Int) {
        get(viewId)!!.propertySet.visibility = visibility
    }

    /**
     * ConstraintSet will not setVisibility. [.VISIBILITY_MODE_IGNORE] or [ ][.VISIBILITY_MODE_NORMAL].
     *
     * @param viewId         ID of view
     * @param visibilityMode
     */
    fun setVisibilityMode(viewId: String, visibilityMode: Int) {
        get(viewId)!!.propertySet.mVisibilityMode = visibilityMode
    }

    /**
     * ConstraintSet will not setVisibility. [.VISIBILITY_MODE_IGNORE] or [ ][.VISIBILITY_MODE_NORMAL].
     *
     * @param viewId ID of view
     */
    fun getVisibilityMode(viewId: String): Int {
        return get(viewId)!!.propertySet.mVisibilityMode
    }

    /**
     * Get the visibility flag set in this view
     *
     * @param viewId the id of the view
     * @return the visibility constraint for the view
     */
    fun getVisibility(viewId: String): Int {
        return get(viewId)!!.propertySet.visibility
    }

    /**
     * Get the height set in the view
     *
     * @param viewId the id of the view
     * @return return the height constraint of the view
     */
    fun getHeight(viewId: String): Int {
        return get(viewId)!!.layout.mHeight
    }

    /**
     * Get the width set in the view
     *
     * @param viewId the id of the view
     * @return return the width constraint of the view
     */
    fun getWidth(viewId: String): Int {
        return get(viewId)!!.layout.mWidth
    }

    /**
     * Adjust the alpha of a view.
     *
     * @param viewId ID of view to adjust the vertical
     * @param alpha  the alpha
     */
    fun setAlpha(viewId: String, alpha: Float) {
        get(viewId)!!.propertySet.alpha = alpha
    }

    /**
     * return with the constraint set will apply elevation for the specified view.
     *
     * @return true if the elevation will be set on this view (default is false)
     */
    fun getApplyElevation(viewId: String): Boolean {
        return get(viewId)!!.transform.applyElevation
    }

    /**
     * set if elevation will be applied to the view.
     * Elevation logic is based on style and animation. By default it is not used because it would
     * lead to unexpected results.
     *
     * @param apply true if this constraint set applies elevation to this view
     */
    fun setApplyElevation(viewId: String, apply: Boolean) {
        get(viewId)!!.transform.applyElevation = apply
    }

    /**
     * Adjust the elevation of a view.
     *
     * @param viewId    ID of view to adjust the elevation
     * @param elevation the elevation
     */
    fun setElevation(viewId: String, elevation: Float) {
        get(viewId)!!.transform.elevation = elevation
        get(viewId)!!.transform.applyElevation = true
    }

    /**
     * Adjust the post-layout rotation about the Z axis of a view.
     *
     * @param viewId   ID of view to adjust th Z rotation
     * @param rotation the rotation about the X axis
     */
    fun setRotation(viewId: String, rotation: Float) {
        get(viewId)!!.transform.rotation = rotation
    }

    /**
     * Adjust the post-layout rotation about the X axis of a view.
     *
     * @param viewId    ID of view to adjust th X rotation
     * @param rotationX the rotation about the X axis
     */
    fun setRotationX(viewId: String, rotationX: Float) {
        get(viewId)!!.transform.rotationX = rotationX
    }

    /**
     * Adjust the post-layout rotation about the Y axis of a view.
     *
     * @param viewId    ID of view to adjust the Y rotation
     * @param rotationY the rotationY
     */
    fun setRotationY(viewId: String, rotationY: Float) {
        get(viewId)!!.transform.rotationY = rotationY
    }

    /**
     * Adjust the post-layout scale in X of a view.
     *
     * @param viewId ID of view to adjust the scale in X
     * @param scaleX the scale in X
     */
    fun setScaleX(viewId: String, scaleX: Float) {
        get(viewId)!!.transform.scaleX = scaleX
    }

    /**
     * Adjust the post-layout scale in Y of a view.
     *
     * @param viewId ID of view to adjust the scale in Y
     * @param scaleY the scale in Y
     */
    fun setScaleY(viewId: String, scaleY: Float) {
        get(viewId)!!.transform.scaleY = scaleY
    }

    /**
     * Set X location of the pivot point around which the view will rotate and scale.
     * use Float.NaN to clear the pivot value.
     * Note: once an actual TView has had its pivot set it cannot be cleared.
     *
     * @param viewId          ID of view to adjust the transforms pivot point about X
     * @param transformPivotX X location of the pivot point.
     */
    fun setTransformPivotX(viewId: String, transformPivotX: Float) {
        get(viewId)!!.transform.transformPivotX = transformPivotX
    }

    /**
     * Set Y location of the pivot point around which the view will rotate and scale.
     * use Float.NaN to clear the pivot value.
     * Note: once an actual TView has had its pivot set it cannot be cleared.
     *
     * @param viewId          ID of view to adjust the transforms pivot point about Y
     * @param transformPivotY Y location of the pivot point.
     */
    fun setTransformPivotY(viewId: String, transformPivotY: Float) {
        get(viewId)!!.transform.transformPivotY = transformPivotY
    }

    /**
     * Set X,Y location of the pivot point around which the view will rotate and scale.
     * use Float.NaN to clear the pivot value.
     * Note: once an actual TView has had its pivot set it cannot be cleared.
     *
     * @param viewId          ID of view to adjust the transforms pivot point
     * @param transformPivotX X location of the pivot point.
     * @param transformPivotY Y location of the pivot point.
     */
    fun setTransformPivot(viewId: String, transformPivotX: Float, transformPivotY: Float) {
        val constraint = get(viewId)
        constraint!!.transform.transformPivotY = transformPivotY
        constraint.transform.transformPivotX = transformPivotX
    }

    /**
     * Adjust the post-layout X translation of a view.
     *
     * @param viewId       ID of view to translate in X
     * @param translationX the translation in X
     */
    fun setTranslationX(viewId: String, translationX: Float) {
        get(viewId)!!.transform.translationX = translationX
    }

    /**
     * Adjust the  post-layout Y translation of a view.
     *
     * @param viewId       ID of view to to translate in Y
     * @param translationY the translation in Y
     */
    fun setTranslationY(viewId: String, translationY: Float) {
        get(viewId)!!.transform.translationY = translationY
    }

    /**
     * Adjust the post-layout translation of a view.
     *
     * @param viewId       ID of view to adjust its translation in X & Y
     * @param translationX the translation in X
     * @param translationY the translation in Y
     */
    fun setTranslation(viewId: String, translationX: Float, translationY: Float) {
        val constraint = get(viewId)
        constraint!!.transform.translationX = translationX
        constraint.transform.translationY = translationY
    }

    /**
     * Adjust the translation in Z of a view.
     *
     * @param viewId       ID of view to adjust
     * @param translationZ the translationZ
     */
    fun setTranslationZ(viewId: String, translationZ: Float) {
        get(viewId)!!.transform.translationZ = translationZ
    }

    /**
     *
     */
    fun setEditorAbsoluteX(viewId: String, position: Int) {
        get(viewId)!!.layout.editorAbsoluteX = position
    }

    /**
     *
     */
    fun setEditorAbsoluteY(viewId: String, position: Int) {
        get(viewId)!!.layout.editorAbsoluteY = position
    }

    /**
     * Sets the wrap behavior of the widget in the parent's wrap computation
     */
    fun setLayoutWrapBehavior(viewId: String, behavior: Int) {
        if (behavior >= 0 && behavior <= ConstraintWidget.WRAP_BEHAVIOR_SKIPPED) {
            get(viewId)!!.layout.mWrapBehavior = behavior
        }
    }

    /**
     * Sets the height of the view. It can be a dimension, [.WRAP_CONTENT] or [ ][.MATCH_CONSTRAINT].
     *
     * @param viewId ID of view to adjust its height
     * @param height the height of the view
     * @since 1.1
     */
    fun constrainHeight(viewId: String, height: Int) {
        get(viewId)!!.layout.mHeight = height
    }

    /**
     * Sets the width of the view. It can be a dimension, [.WRAP_CONTENT] or [ ][.MATCH_CONSTRAINT].
     *
     * @param viewId ID of view to adjust its width
     * @param width  the width of the view
     * @since 1.1
     */
    fun constrainWidth(viewId: String, width: Int) {
        get(viewId)!!.layout.mWidth = width
    }

    /**
     * Constrain the view on a circle constraint
     *
     * @param viewId ID of the view we constrain
     * @param id     ID of the view we constrain relative to
     * @param radius the radius of the circle in degrees
     * @param angle  the angle
     * @since 1.1
     */
    fun constrainCircle(viewId: String, id: String, radius: Int, angle: Float) {
        val constraint = get(viewId)
        constraint!!.layout.circleConstraint = id
        constraint.layout.circleRadius = radius
        constraint.layout.circleAngle = angle
    }

    /**
     * Sets the maximum height of the view. It is a dimension, It is only applicable if height is
     * #MATCH_CONSTRAINT}.
     *
     * @param viewId ID of view to adjust it height
     * @param height the maximum height of the constraint
     * @since 1.1
     */
    fun constrainMaxHeight(viewId: String, height: Int) {
        get(viewId)!!.layout.heightMax = height
    }

    /**
     * Sets the maximum width of the view. It is a dimension, It is only applicable if width is
     * #MATCH_CONSTRAINT}.
     *
     * @param viewId ID of view to adjust its max height
     * @param width  the maximum width of the view
     * @since 1.1
     */
    fun constrainMaxWidth(viewId: String, width: Int) {
        get(viewId)!!.layout.widthMax = width
    }

    /**
     * Sets the height of the view. It is a dimension, It is only applicable if height is
     * #MATCH_CONSTRAINT}.
     *
     * @param viewId ID of view to adjust its min height
     * @param height the minimum height of the view
     * @since 1.1
     */
    fun constrainMinHeight(viewId: String, height: Int) {
        get(viewId)!!.layout.heightMin = height
    }

    /**
     * Sets the width of the view.  It is a dimension, It is only applicable if width is
     * #MATCH_CONSTRAINT}.
     *
     * @param viewId ID of view to adjust its min height
     * @param width  the minimum width of the view
     * @since 1.1
     */
    fun constrainMinWidth(viewId: String, width: Int) {
        get(viewId)!!.layout.widthMin = width
    }

    /**
     * Sets the width of the view as a percentage of the parent.
     *
     * @param viewId
     * @param percent
     * @since 1.1
     */
    fun constrainPercentWidth(viewId: String, percent: Float) {
        get(viewId)!!.layout.widthPercent = percent
    }

    /**
     * Sets the height of the view as a percentage of the parent.
     *
     * @param viewId
     * @param percent
     * @since 1.1
     */
    fun constrainPercentHeight(viewId: String, percent: Float) {
        get(viewId)!!.layout.heightPercent = percent
    }

    /**
     * Sets how the height is calculated ether MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD.
     * Default is spread.
     *
     * @param viewId ID of view to adjust its matchConstraintDefaultHeight
     * @param height MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD
     * @since 1.1
     */
    fun constrainDefaultHeight(viewId: String, height: Int) {
        get(viewId)!!.layout.heightDefault = height
    }

    /**
     * Sets how the width is calculated ether MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD.
     * Default is spread.
     *
     * @param viewId      ID of view to adjust its matchConstraintDefaultWidth
     * @param constrained if true with will be constrained
     * @since 1.1
     */
    fun constrainedWidth(viewId: String, constrained: Boolean) {
        get(viewId)!!.layout.constrainedWidth = constrained
    }

    /**
     * Sets how the height is calculated ether MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD.
     * Default is spread.
     *
     * @param viewId      ID of view to adjust its matchConstraintDefaultHeight
     * @param constrained if true height will be constrained
     * @since 1.1
     */
    fun constrainedHeight(viewId: String, constrained: Boolean) {
        get(viewId)!!.layout.constrainedHeight = constrained
    }

    /**
     * Sets how the width is calculated ether MATCH_CONSTRAINT_WRAP or MATCH_CONSTRAINT_SPREAD.
     * Default is spread.
     *
     * @param viewId ID of view to adjust its matchConstraintDefaultWidth
     * @param width  SPREAD or WRAP
     * @since 1.1
     */
    fun constrainDefaultWidth(viewId: String, width: Int) {
        get(viewId)!!.layout.widthDefault = width
    }

    /**
     * The child's weight that we can use to distribute the available horizontal space
     * in a chain, if the dimension behaviour is set to MATCH_CONSTRAINT
     *
     * @param viewId ID of view to adjust its HorizontalWeight
     * @param weight the weight that we can use to distribute the horizontal space
     */
    fun setHorizontalWeight(viewId: String, weight: Float) {
        get(viewId)!!.layout.horizontalWeight = weight
    }

    /**
     * The child's weight that we can use to distribute the available vertical space
     * in a chain, if the dimension behaviour is set to MATCH_CONSTRAINT
     *
     * @param viewId ID of view to adjust its VerticalWeight
     * @param weight the weight that we can use to distribute the vertical space
     */
    fun setVerticalWeight(viewId: String, weight: Float) {
        get(viewId)!!.layout.verticalWeight = weight
    }

    /**
     * How the elements of the horizontal chain will be positioned. if the dimension
     * behaviour is set to MATCH_CONSTRAINT. The possible values are:
     *
     *
     *  * [.CHAIN_SPREAD] -- the elements will be spread out
     *  * [.CHAIN_SPREAD_INSIDE] -- similar, but the endpoints of the chain will not
     * be spread out
     *  * [.CHAIN_PACKED] -- the elements of the chain will be packed together. The
     * horizontal bias attribute of the child will then affect the positioning of the packed
     * elements
     *
     *
     * @param viewId     ID of view to adjust its HorizontalChainStyle
     * @param chainStyle the weight that we can use to distribute the horizontal space
     */
    fun setHorizontalChainStyle(viewId: String, chainStyle: Int) {
        get(viewId)!!.layout.horizontalChainStyle = chainStyle
    }

    /**
     * How the elements of the vertical chain will be positioned. in a chain, if the dimension
     * behaviour is set to MATCH_CONSTRAINT
     *
     *
     *  * [.CHAIN_SPREAD] -- the elements will be spread out
     *  * [.CHAIN_SPREAD_INSIDE] -- similar, but the endpoints of the chain will not
     * be spread out
     *  * [.CHAIN_PACKED] -- the elements of the chain will be packed together. The
     * vertical bias attribute of the child will then affect the positioning of the packed
     * elements
     *
     *
     * @param viewId     ID of view to adjust its VerticalChainStyle
     * @param chainStyle the weight that we can use to distribute the horizontal space
     */
    fun setVerticalChainStyle(viewId: String, chainStyle: Int) {
        get(viewId)!!.layout.verticalChainStyle = chainStyle
    }

    /**
     * Adds a view to a horizontal chain.
     *
     * @param viewId  view to add
     * @param leftId  view in chain to the left
     * @param rightId view in chain to the right
     */
    fun addToHorizontalChain(viewId: String, leftId: String, rightId: String) {
        connect(viewId, LEFT, leftId, if (leftId == PARENT_ID) LEFT else RIGHT, 0)
        connect(viewId, RIGHT, rightId, if (rightId == PARENT_ID) RIGHT else LEFT, 0)
        if (leftId != PARENT_ID) {
            connect(leftId, RIGHT, viewId, LEFT, 0)
        }
        if (rightId != PARENT_ID) {
            connect(rightId, LEFT, viewId, RIGHT, 0)
        }
    }

    /**
     * Adds a view to a horizontal chain.
     *
     * @param viewId  view to add
     * @param leftId  view to the start side
     * @param rightId view to the end side
     */
    fun addToHorizontalChainRTL(viewId: String, leftId: String, rightId: String) {
        connect(viewId, START, leftId, if (leftId == PARENT_ID) START else END, 0)
        connect(viewId, END, rightId, if (rightId == PARENT_ID) END else START, 0)
        if (leftId != PARENT_ID) {
            connect(leftId, END, viewId, START, 0)
        }
        if (rightId != PARENT_ID) {
            connect(rightId, START, viewId, END, 0)
        }
    }

    /**
     * Adds a view to a vertical chain.
     *
     * @param viewId   view to add to a vertical chain
     * @param topId    view above.
     * @param bottomId view below
     */
    fun addToVerticalChain(viewId: String, topId: String, bottomId: String) {
        connect(viewId, TOP, topId, if (topId == PARENT_ID) TOP else BOTTOM, 0)
        connect(viewId, BOTTOM, bottomId, if (bottomId == PARENT_ID) BOTTOM else TOP, 0)
        if (topId != PARENT_ID) {
            connect(topId, BOTTOM, viewId, TOP, 0)
        }
        if (bottomId != PARENT_ID) {
            connect(bottomId, TOP, viewId, BOTTOM, 0)
        }
    }

    /**
     * Removes a view from a vertical chain.
     * This assumes the view is connected to a vertical chain.
     * Its behaviour is undefined if not part of a vertical chain.
     *
     * @param viewId the view to be removed
     */
    fun removeFromVerticalChain(viewId: String) {
        if (mConstraints.containsKey(viewId)) {
            val constraint = mConstraints[viewId] ?: return
            val topId = constraint.layout.topToBottom
            val bottomId = constraint.layout.bottomToTop
            if (topId != Layout.UNSET_ID || bottomId != Layout.UNSET_ID) {
                if (topId != Layout.UNSET_ID && bottomId != Layout.UNSET_ID) {
                    // top and bottom connected to views
                    connect(topId, BOTTOM, bottomId, TOP, 0)
                    connect(bottomId, TOP, topId, BOTTOM, 0)
                } else if (constraint.layout.bottomToBottom != Layout.UNSET_ID) {
                    // top connected to view. Bottom connected to parent
                    connect(topId, BOTTOM, constraint.layout.bottomToBottom, BOTTOM, 0)
                } else if (constraint.layout.topToTop != Layout.UNSET_ID) {
                    // bottom connected to view. Top connected to parent
                    connect(bottomId, TOP, constraint.layout.topToTop, TOP, 0)
                }
            }
        }
        clear(viewId, TOP)
        clear(viewId, BOTTOM)
    }

    /**
     * Removes a view from a horizontal chain.
     * This assumes the view is connected to a horizontal chain.
     * Its behaviour is undefined if not part of a horizontal chain.
     *
     * @param viewId the view to be removed
     */
    fun removeFromHorizontalChain(viewId: String) {
        if (mConstraints.containsKey(viewId)) {
            val constraint = mConstraints[viewId] ?: return
            val leftId = constraint.layout.leftToRight
            val rightId = constraint.layout.rightToLeft
            if (leftId != Layout.UNSET_ID || rightId != Layout.UNSET_ID) {
                if (leftId != Layout.UNSET_ID && rightId != Layout.UNSET_ID) {
                    // left and right connected to views
                    connect(leftId, RIGHT, rightId, LEFT, 0)
                    connect(rightId, LEFT, leftId, RIGHT, 0)
                } else if (constraint.layout.rightToRight != Layout.UNSET_ID) {
                    // left connected to view. right connected to parent
                    connect(leftId, RIGHT, constraint.layout.rightToRight, RIGHT, 0)
                } else if (constraint.layout.leftToLeft != Layout.UNSET_ID) {
                    // right connected to view. left connected to parent
                    connect(rightId, LEFT, constraint.layout.leftToLeft, LEFT, 0)
                }
                clear(viewId, LEFT)
                clear(viewId, RIGHT)
            } else {
                val startId = constraint.layout.startToEnd
                val endId = constraint.layout.endToStart
                if (startId != Layout.UNSET_ID || endId != Layout.UNSET_ID) {
                    if (startId != Layout.UNSET_ID && endId != Layout.UNSET_ID) {
                        // start and end connected to views
                        connect(startId, END, endId, START, 0)
                        connect(endId, START, leftId, END, 0)
                    } else if (endId != Layout.UNSET_ID) {
                        if (constraint.layout.rightToRight != Layout.UNSET_ID) {
                            // left connected to view. right connected to parent
                            connect(leftId, END, constraint.layout.rightToRight, END, 0)
                        } else if (constraint.layout.leftToLeft != Layout.UNSET_ID) {
                            // right connected to view. left connected to parent
                            connect(endId, START, constraint.layout.leftToLeft, START, 0)
                        }
                    }
                }
                clear(viewId, START)
                clear(viewId, END)
            }
        }
    }

    /**
     * Creates a ConstraintLayout virtual object. Currently only horizontal or vertical GuideLines.
     *
     * @param guidelineID ID of guideline to create
     * @param orientation the Orientation of the guideline
     */
    fun create(guidelineID: String, orientation: Int) {
        val constraint = get(guidelineID)
        constraint!!.layout.mIsGuideline = true
        constraint.layout.orientation = orientation
    }

    /**
     * Creates a ConstraintLayout Barrier object.
     *
     * @param id
     * @param direction  Barrier.{LEFT,RIGHT,TOP,BOTTOM,START,END}
     * @param referenced
     * @since 1.1
     */
    fun createBarrier(id: String, direction: Int, margin: Int, vararg referenced: String) {
        val constraint = get(id)
        constraint!!.layout.mHelperType = BARRIER_TYPE
        constraint.layout.mBarrierDirection = direction
        constraint.layout.mBarrierMargin = margin
        constraint.layout.mIsGuideline = false
        constraint.layout.mReferenceIds = arrayOf(*referenced)
    }

    /**
     * Set the guideline's distance form the top or left edge.
     *
     * @param guidelineID ID of the guideline
     * @param margin      the distance to the top or left edge
     */
    fun setGuidelineBegin(guidelineID: String, margin: Int) {
        get(guidelineID)!!.layout.guideBegin = margin
        get(guidelineID)!!.layout.guideEnd = Layout.UNSET
        get(guidelineID)!!.layout.guidePercent = Layout.UNSET.toFloat()
    }

    /**
     * Set a guideline's distance to end.
     *
     * @param guidelineID ID of the guideline
     * @param margin      the margin to the right or bottom side of container
     */
    fun setGuidelineEnd(guidelineID: String, margin: Int) {
        get(guidelineID)!!.layout.guideEnd = margin
        get(guidelineID)!!.layout.guideBegin = Layout.UNSET
        get(guidelineID)!!.layout.guidePercent = Layout.UNSET.toFloat()
    }

    /**
     * Set a Guideline's percent.
     *
     * @param guidelineID ID of the guideline
     * @param ratio       the ratio between the gap on the left and right
     * 0.0 is top/left 0.5 is middle
     */
    fun setGuidelinePercent(guidelineID: String, ratio: Float) {
        get(guidelineID)!!.layout.guidePercent = ratio
        get(guidelineID)!!.layout.guideEnd = Layout.UNSET
        get(guidelineID)!!.layout.guideBegin = Layout.UNSET
    }

    /**
     * get the reference id's of a helper.
     *
     * @param id
     * @return array of id's
     */
    fun getReferencedIds(id: String): Array<String> {
        val constraint = get(id)
        return if (constraint!!.layout.mReferenceIds == null) {
            Array<String>(0) { "" }
        } else Arrays.copyOfNonNull(
            constraint.layout.mReferenceIds!!,
            constraint.layout.mReferenceIds!!.size
        )
    }

    /**
     * sets the reference id's of a barrier.
     *
     * @param id
     * @param referenced
     * @since 2.0
     */
    fun setReferencedIds(id: String, vararg referenced: String) {
        val constraint = get(id)
        constraint!!.layout.mReferenceIds = arrayOf(*referenced)
    }

    /**
     * SEt tye type of barier
     * @param id
     * @param type
     */
    fun setBarrierType(id: String, type: Int) {
        val constraint = get(id)
        constraint!!.layout.mHelperType = type
    }

    /**
     * Remove the attribute
     * @param attributeName
     */
    fun removeAttribute(attributeName: String?) {
        mSavedAttributes.remove(attributeName)
    }

    /**
     * Set the value of an attribute of type int
     * @param viewId
     * @param attributeName
     * @param value
     */
    fun setIntValue(viewId: String, attributeName: String, value: Int) {
        get(viewId)!!.setIntValue(attributeName, value)
    }

    /**
     * Set the value of an attribute of type color
     * @param viewId
     * @param attributeName
     * @param value
     */
    fun setColorValue(viewId: String, attributeName: String, value: Int) {
        get(viewId)!!.setColorValue(attributeName, value)
    }

    /**
     * Set the value of an attribute of type float
     * @param viewId
     * @param attributeName
     * @param value
     */
    fun setFloatValue(viewId: String, attributeName: String, value: Float) {
        get(viewId)!!.setFloatValue(attributeName, value)
    }

    /**
     * Set the value of an attribute of type string
     * @param viewId
     * @param attributeName
     * @param value
     */
    fun setStringValue(viewId: String, attributeName: String, value: String) {
        get(viewId)!!.setStringValue(attributeName, value)
    }

    private fun addAttributes(attributeType: ConstraintAttribute.AttributeType, vararg attributeName: String) {
        var constraintAttribute: ConstraintAttribute? = null
        for (i in attributeName.indices) {
            if (mSavedAttributes.containsKey(attributeName[i])) {
                constraintAttribute = mSavedAttributes[attributeName[i]]
                if (constraintAttribute == null) {
                    continue
                }
                require(!(constraintAttribute.type != attributeType)) {
                    ("ConstraintAttribute is already a "
                            + constraintAttribute!!.type.name)
                }
            } else {
                constraintAttribute = ConstraintAttribute(attributeName[i], attributeType)
                mSavedAttributes[attributeName[i]] = constraintAttribute
            }
        }
    }

    /**
     * Parse int
     * @param set
     * @param attributes
     */
    fun parseIntAttributes(set: Constraint, attributes: String) {
        val sp: Array<String> = attributes.split(",").toTypedArray()
        for (i in sp.indices) {
            val attr: Array<String> = sp[i].split("=").toTypedArray()
            if (attr.size != 2) {
                Log.w(TAG, " Unable to parse " + sp[i])
            } else {
                set.setFloatValue(attr[0], attr[1].toFloat())
            }
        }
    }

    /**
     * Parse color
     * @param set
     * @param attributes
     */
    fun parseColorAttributes(set: Constraint, attributes: String) {
        val sp: Array<String> = attributes.split(",").toTypedArray()
        for (i in sp.indices) {
            val attr: Array<String> = sp[i].split("=").toTypedArray()
            if (attr.size != 2) {
                Log.w(TAG, " Unable to parse " + sp[i])
            } else {
                set.setColorValue(attr[0], TColor.parseColor(attr[1]))
            }
        }
    }

    /**
     * Parse floats
     * @param set
     * @param attributes
     */
    fun parseFloatAttributes(set: Constraint, attributes: String) {
        val sp: Array<String> = attributes.split(",").toTypedArray()
        for (i in sp.indices) {
            val attr: Array<String> = sp[i].split("=").toTypedArray()
            if (attr.size != 2) {
                Log.w(TAG, " Unable to parse " + sp[i])
            } else {
                set.setFloatValue(attr[0], attr[1].toFloat())
            }
        }
    }

    /**
     * Parse string
     * @param set
     * @param attributes
     */
    fun parseStringAttributes(set: Constraint, attributes: String) {
        val sp = splitString(attributes)
        for (i in sp.indices) {
            val attr: Array<String> = sp[i].split("=").toTypedArray()
            Log.w(TAG, " Unable to parse " + sp[i])
            set.setStringValue(attr[0], attr[1])
        }
    }

    /**
     * Add attribute of type Int
     * @param attributeName
     */
    fun addIntAttributes(vararg attributeName: String) {
        addAttributes(ConstraintAttribute.AttributeType.INT_TYPE, *attributeName)
    }

    /**
     * Add attribute of type TColor
     * @param attributeName
     */
    fun addColorAttributes(vararg attributeName: String) {
        addAttributes(ConstraintAttribute.AttributeType.COLOR_TYPE, *attributeName)
    }

    /**
     * Add attribute of type float
     * @param attributeName
     */
    fun addFloatAttributes(vararg attributeName: String) {
        addAttributes(ConstraintAttribute.AttributeType.FLOAT_TYPE, *attributeName)
    }

    /**
     * Add attribute of type string
     * @param attributeName
     */
    fun addStringAttributes(vararg attributeName: String) {
        addAttributes(ConstraintAttribute.AttributeType.STRING_TYPE, *attributeName)
    }

    private operator fun get(id: String): Constraint {
        if (!mConstraints.containsKey(id)) {
            mConstraints[id] = Constraint()
        }
        return mConstraints[id]!!
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

    /**
     * Load a constraint set from a constraintSet.xml file.
     * Note. Do NOT use this to load a layout file.
     * It will fail silently as there is no efficient way to differentiate.
     *
     * @param context    the context for the inflation
     * @param resourceId id of xml file in res/xml/
     */
    fun load(context: TContext, resourceId: String) {
        val res: TResources = context.getResources()
        val parser: XmlBufferedReader = res.getXml(resourceId)
        try {
            var eventType = parser.eventType
            while (eventType != EventType.END_DOCUMENT) {
                when (eventType) {
                    EventType.START_DOCUMENT, EventType.END_ELEMENT, EventType.TEXT -> {}
                    EventType.START_ELEMENT -> {
                        val tagName: String = parser.name.localPart
                        val constraint = fillFromAttributeList(
                            context,
                            Xml.asAttributeSet(parser), false
                        )
                        if (tagName.equals("Guideline", ignoreCase = true)) {
                            constraint.layout.mIsGuideline = true
                        }
                        if (I_DEBUG) {
                            Log.v(
                                TAG, Debug.loc
                                        + " cache " + Debug.getName(context, constraint.mViewId)
                                        + " " + constraint.mViewId
                            )
                        }
                        mConstraints[constraint.mViewId] = constraint
                    }
                    else -> {}
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing resource: $resourceId", e)
        }
    }

    /**
     * Load a constraint set from a constraintSet.xml file
     *
     * @param context the context for the inflation
     * @param parser  id of xml file in res/xml/
     */
    fun load(context: TContext, parser: XmlBufferedReader) {
        var tagName: String? = null
        try {
            var constraint: Constraint? = null
            var eventType = parser.eventType
            var document = ""
            while (eventType != EventType.END_DOCUMENT) {
                when (eventType) {
                    EventType.START_DOCUMENT -> {
                        document = parser.name.toString()
                    }
                    EventType.START_ELEMENT -> {
                        tagName = parser.name.localPart
                        if (I_DEBUG) {
                            Log.v(TAG, Debug.loc + " " + document + " tagName=" + tagName)
                        }
                        when (tagName) {
                            "Constraint" -> constraint = fillFromAttributeList(
                                context,
                                Xml.asAttributeSet(parser), false
                            )
                            "ConstraintOverride" -> constraint = fillFromAttributeList(
                                context,
                                Xml.asAttributeSet(parser), true
                            )
                            "Guideline" -> {
                                constraint = fillFromAttributeList(
                                    context,
                                    Xml.asAttributeSet(parser), false
                                )
                                constraint.layout.mIsGuideline = true
                                constraint.layout.mApply = true
                            }
                            "Barrier" -> {
                                constraint = fillFromAttributeList(
                                    context,
                                    Xml.asAttributeSet(parser), false
                                )
                                constraint.layout.mHelperType = BARRIER_TYPE
                            }
                            "PropertySet" -> {
                                if (constraint == null) {
                                    throw RuntimeException(
                                        ERROR_MESSAGE
                                                + parser.locationInfo ?: ""
                                    )
                                }
                                constraint.propertySet.fillFromAttributeList(
                                    context,
                                    Xml.asAttributeSet(parser)
                                )
                            }
                            "Transform" -> {
                                if (constraint == null) {
                                    throw RuntimeException(
                                        ERROR_MESSAGE
                                                + parser.locationInfo ?: ""
                                    )
                                }
                                constraint.transform.fillFromAttributeList(
                                    context,
                                    Xml.asAttributeSet(parser)
                                )
                            }
                            "Layout" -> {
                                if (constraint == null) {
                                    throw RuntimeException(
                                        ERROR_MESSAGE
                                                + parser.locationInfo ?: ""
                                    )
                                }
                                constraint.layout.fillFromAttributeList(
                                    context,
                                    Xml.asAttributeSet(parser)
                                )
                            }
                            "Motion" -> {
                                if (constraint == null) {
                                    throw RuntimeException(
                                        ERROR_MESSAGE
                                                + parser.locationInfo ?: ""
                                    )
                                }
                                constraint.motion.fillFromAttributeList(
                                    context,
                                    Xml.asAttributeSet(parser)
                                )
                            }
                            "CustomAttribute", "CustomMethod" -> {
                                if (constraint == null) {
                                    throw RuntimeException(
                                        ERROR_MESSAGE
                                                + parser.locationInfo ?: ""
                                    )
                                }
                                ConstraintAttribute.parse(
                                    context, parser,
                                    constraint.mCustomConstraints
                                )
                            }
                        }
                    }
                    EventType.END_ELEMENT -> {
                        tagName = parser.localName
                        when (tagName.lowercase()) {
                            "constraintset" -> return
                            "constraint", "constraintoverride", "guideline" -> {
                                mConstraints[constraint!!.mViewId] = constraint
                                constraint = null
                            }
                        }
                        tagName = null
                    }
                    EventType.TEXT -> {}
                    else -> {}
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML resource", e)
        }
    }

    private fun fillFromAttributeList(
        context: TContext,
        attrs: AttributeSet,
        override: Boolean
    ): Constraint {
        val c = Constraint()
        populateConstraint(c, context.getResources(), attrs, override)
        return c
    }

    private fun populateConstraint(c: Constraint, a: TResources, attrs: AttributeSet, override: Boolean) {
        if (override) {
            populateOverride(c, a, attrs)
            return
        }
        attrs.forEach { kvp ->
            val attrId = kvp.key
            val attr = kvp.value
            /*if (DEBUG) { // USEFUL when adding features to track tags being parsed
                try {
                    var campos: Array<Field?> = R.styleable::class.java.getFields()
                    var found = false
                    for (f in campos) {
                        try {
                            if (f.getType().isPrimitive()
                                && attr == f.getInt(null) && f.getName()
                                    .contains("Constraint_")
                            ) {
                                found = true
                                if (DEBUG) {
                                    Log.v(TAG, "L id " + f.getName().toString() + " #" + attr)
                                }
                                break
                            }
                        } catch (e: Exception) {
                        }
                    }
                    if (!found) {
                        campos = attr::class.java.getFields()
                        for (f in campos) {
                            try {
                                if (f.getType()
                                        .isPrimitive() && attr == f.getInt(null) && f.getName()
                                        .contains("Constraint_")
                                ) {
                                    found = false
                                    if (DEBUG) {
                                        Log.v(TAG, "x id " + f.getName())
                                    }
                                    break
                                }
                            } catch (e: Exception) {
                            }
                        }
                    }
                    if (!found) {
                        Log.v(TAG, " ? $attr")
                    }
                } catch (e: Exception) {
                    Log.v(TAG, " $e")
                }
            }*/
            if (attrId != "android_id" && "android_layout_marginStart" != attrId && "android_layout_marginEnd" != attrId) {
                c.motion.mApply = true
                c.layout.mApply = true
                c.propertySet.mApply = true
                c.transform.mApply = true
            }
            when (sMapToConstant.get(attrId)) {
                LEFT_TO_LEFT -> c.layout.leftToLeft = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.leftToLeft
                )
                LEFT_TO_RIGHT -> c.layout.leftToRight = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.leftToRight
                )
                RIGHT_TO_LEFT -> c.layout.rightToLeft = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.rightToLeft
                )
                RIGHT_TO_RIGHT -> c.layout.rightToRight = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.rightToRight
                )
                TOP_TO_TOP -> c.layout.topToTop = lookupID(a, kvp.key, attr, c.layout.topToTop)
                TOP_TO_BOTTOM -> c.layout.topToBottom = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.topToBottom
                )
                BOTTOM_TO_TOP -> c.layout.bottomToTop = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.bottomToTop
                )
                BOTTOM_TO_BOTTOM -> c.layout.bottomToBottom =
                    lookupID(a, kvp.key, attr, c.layout.bottomToBottom)
                BASELINE_TO_BASELINE -> c.layout.baselineToBaseline =
                    lookupID(a, kvp.key, attr, c.layout.baselineToBaseline)
                BASELINE_TO_TOP -> c.layout.baselineToTop =
                    lookupID(a, kvp.key, attr, c.layout.baselineToTop)
                BASELINE_TO_BOTTOM -> c.layout.baselineToBottom =
                    lookupID(a, kvp.key, attr, c.layout.baselineToBottom)
                EDITOR_ABSOLUTE_X -> c.layout.editorAbsoluteX = a.getDimensionPixelOffset(
                    attr,
                    c.layout.editorAbsoluteX
                )
                EDITOR_ABSOLUTE_Y -> c.layout.editorAbsoluteY = a.getDimensionPixelOffset(
                    attr,
                    c.layout.editorAbsoluteY
                )
                GUIDE_BEGIN -> c.layout.guideBegin =
                    a.getDimensionPixelOffset(attr, c.layout.guideBegin)
                GUIDE_END -> c.layout.guideEnd = a.getDimensionPixelOffset(attr, c.layout.guideEnd)
                GUIDE_PERCENT -> c.layout.guidePercent = a.getFloat(attr, c.layout.guidePercent)
                ORIENTATION -> c.layout.orientation = a.getInt(attr, c.layout.orientation)
                START_TO_END -> c.layout.startToEnd = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.startToEnd
                )
                START_TO_START -> c.layout.startToStart = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.startToStart
                )
                END_TO_START -> c.layout.endToStart = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.endToStart
                )
                END_TO_END -> c.layout.endToEnd = lookupID(a, kvp.key, attr, c.layout.endToEnd)
                CIRCLE -> c.layout.circleConstraint = lookupID(
                    a,
                    kvp.key,
                    attr,
                    c.layout.circleConstraint
                )
                CIRCLE_RADIUS -> c.layout.circleRadius =
                    a.getDimensionPixelSize(attr, c.layout.circleRadius)
                CIRCLE_ANGLE -> c.layout.circleAngle = a.getFloat(attr, c.layout.circleAngle)
                GONE_LEFT_MARGIN -> c.layout.goneLeftMargin = a.getDimensionPixelSize(
                    attr,
                    c.layout.goneLeftMargin
                )
                GONE_TOP_MARGIN -> c.layout.goneTopMargin =
                    a.getDimensionPixelSize(attr, c.layout.goneTopMargin)
                GONE_RIGHT_MARGIN -> c.layout.goneRightMargin = a.getDimensionPixelSize(
                    attr,
                    c.layout.goneRightMargin
                )
                GONE_BOTTOM_MARGIN -> c.layout.goneBottomMargin = a.getDimensionPixelSize(
                    attr,
                    c.layout.goneBottomMargin
                )
                GONE_START_MARGIN -> c.layout.goneStartMargin =
                    a.getDimensionPixelSize(attr, c.layout.goneStartMargin)
                GONE_END_MARGIN -> c.layout.goneEndMargin =
                    a.getDimensionPixelSize(attr, c.layout.goneEndMargin)
                GONE_BASELINE_MARGIN -> c.layout.goneBaselineMargin = a.getDimensionPixelSize(
                    attr,
                    c.layout.goneBaselineMargin
                )
                HORIZONTAL_BIAS -> c.layout.horizontalBias =
                    a.getFloat(attr, c.layout.horizontalBias)
                VERTICAL_BIAS -> c.layout.verticalBias = a.getFloat(attr, c.layout.verticalBias)
                LEFT_MARGIN -> c.layout.leftMargin =
                    a.getDimensionPixelSize(attr, c.layout.leftMargin)
                RIGHT_MARGIN -> c.layout.rightMargin =
                    a.getDimensionPixelSize(attr, c.layout.rightMargin)
                START_MARGIN -> c.layout.startMargin = a.getDimensionPixelSize(attr, c.layout.startMargin)
                END_MARGIN -> c.layout.endMargin = a.getDimensionPixelSize(attr, c.layout.endMargin)
                TOP_MARGIN -> c.layout.topMargin = a.getDimensionPixelSize(attr, c.layout.topMargin)
                BOTTOM_MARGIN -> c.layout.bottomMargin =
                    a.getDimensionPixelSize(attr, c.layout.bottomMargin)
                BASELINE_MARGIN -> c.layout.baselineMargin = a.getDimensionPixelSize(
                    attr,
                    c.layout.baselineMargin
                )
                LAYOUT_WIDTH -> c.layout.mWidth = a.getLayoutDimension(attr, c.layout.mWidth)
                LAYOUT_HEIGHT -> c.layout.mHeight = a.getLayoutDimension(attr, c.layout.mHeight)
                LAYOUT_CONSTRAINT_WIDTH -> parseDimensionConstraints(
                    c.layout,
                    a,
                    kvp.key,
                    attr,
                    HORIZONTAL
                )
                LAYOUT_CONSTRAINT_HEIGHT -> parseDimensionConstraints(
                    c.layout,
                    a,
                    kvp.key,
                    attr,
                    VERTICAL
                )
                LAYOUT_WRAP_BEHAVIOR -> c.layout.mWrapBehavior =
                    a.getInt(attr, c.layout.mWrapBehavior)
                WIDTH_DEFAULT -> c.layout.widthDefault = a.getInt(attr, c.layout.widthDefault)
                HEIGHT_DEFAULT -> c.layout.heightDefault = a.getInt(attr, c.layout.heightDefault)
                HEIGHT_MAX -> c.layout.heightMax = a.getDimensionPixelSize(attr, c.layout.heightMax)
                WIDTH_MAX -> c.layout.widthMax = a.getDimensionPixelSize(attr, c.layout.widthMax)
                HEIGHT_MIN -> c.layout.heightMin = a.getDimensionPixelSize(attr, c.layout.heightMin)
                WIDTH_MIN -> c.layout.widthMin = a.getDimensionPixelSize(attr, c.layout.widthMin)
                CONSTRAINED_WIDTH -> c.layout.constrainedWidth =
                    a.getBoolean(attr, c.layout.constrainedWidth)
                CONSTRAINED_HEIGHT -> c.layout.constrainedHeight =
                    a.getBoolean(attr, c.layout.constrainedHeight)
                LAYOUT_VISIBILITY -> {
                    c.propertySet.visibility = a.getInt(attr, c.propertySet.visibility)
                    c.propertySet.visibility = VISIBILITY_FLAGS[c.propertySet.visibility]
                }
                VISIBILITY_MODE -> c.propertySet.mVisibilityMode =
                    a.getInt(attr, c.propertySet.mVisibilityMode)
                ALPHA -> c.propertySet.alpha = a.getFloat(attr, c.propertySet.alpha)
                ELEVATION -> {
                    c.transform.applyElevation = true
                    c.transform.elevation = a.getDimension(attr, c.transform.elevation)
                }
                ROTATION -> c.transform.rotation = a.getFloat(attr, c.transform.rotation)
                ROTATION_X -> c.transform.rotationX = a.getFloat(attr, c.transform.rotationX)
                ROTATION_Y -> c.transform.rotationY = a.getFloat(attr, c.transform.rotationY)
                SCALE_X -> c.transform.scaleX = a.getFloat(attr, c.transform.scaleX)
                SCALE_Y -> c.transform.scaleY = a.getFloat(attr, c.transform.scaleY)
                TRANSFORM_PIVOT_X -> c.transform.transformPivotX =
                    a.getDimension(attr, c.transform.transformPivotX)
                TRANSFORM_PIVOT_Y -> c.transform.transformPivotY =
                    a.getDimension(attr, c.transform.transformPivotY)
                TRANSLATION_X -> c.transform.translationX =
                    a.getDimension(attr, c.transform.translationX)
                TRANSLATION_Y -> c.transform.translationY =
                    a.getDimension(attr, c.transform.translationY)
                TRANSLATION_Z ->  {
                    c.transform.translationZ = a.getDimension(attr, c.transform.translationZ)
                }
                TRANSFORM_PIVOT_TARGET -> c.transform.transformPivotTarget =
                    lookupID(a, kvp.key, attr, c.transform.transformPivotTarget)
                VERTICAL_WEIGHT -> c.layout.verticalWeight =
                    a.getFloat(attr, c.layout.verticalWeight)
                HORIZONTAL_WEIGHT -> c.layout.horizontalWeight =
                    a.getFloat(attr, c.layout.horizontalWeight)
                VERTICAL_STYLE -> c.layout.verticalChainStyle =
                    a.getInt(attr, c.layout.verticalChainStyle)
                HORIZONTAL_STYLE -> c.layout.horizontalChainStyle =
                    a.getInt(attr, c.layout.horizontalChainStyle)
                VIEW_ID -> c.mViewId = a.getResourceId(attr, c.mViewId)
                DIMENSION_RATIO -> c.layout.dimensionRatio = a.getString(kvp.key, attr)
                WIDTH_PERCENT -> c.layout.widthPercent = a.getFloat(attr, 1f)
                HEIGHT_PERCENT -> c.layout.heightPercent = a.getFloat(attr, 1f)
                PROGRESS -> c.propertySet.mProgress = a.getFloat(attr, c.propertySet.mProgress)
                ANIMATE_RELATIVE_TO -> c.motion.mAnimateRelativeTo =
                    lookupID(a, kvp.key, attr, c.motion.mAnimateRelativeTo)
                ANIMATE_CIRCLE_ANGLE_TO -> c.motion.mAnimateCircleAngleTo =
                    a.getInt(attr, c.motion.mAnimateCircleAngleTo)
                TRANSITION_EASING -> {
                    val type = a.getResourceType(attr)
                    if (type == TypedValue.TYPE_STRING) {
                        c.motion.mTransitionEasing = a.getString(kvp.key, attr)
                    } else {
                        c.motion.mTransitionEasing = Easing.NAMED_EASING.get(a.getInt(attr, 0))
                    }
                }
                PATH_MOTION_ARC -> c.motion.mPathMotionArc = a.getInt(attr, c.motion.mPathMotionArc)
                TRANSITION_PATH_ROTATE -> c.motion.mPathRotate =
                    a.getFloat(attr, c.motion.mPathRotate)
                MOTION_STAGGER -> c.motion.mMotionStagger =
                    a.getFloat(attr, c.motion.mMotionStagger)
                QUANTIZE_MOTION_STEPS -> c.motion.mQuantizeMotionSteps = a.getInt(
                    attr,
                    c.motion.mQuantizeMotionSteps
                )
                QUANTIZE_MOTION_PHASE -> c.motion.mQuantizeMotionPhase =
                    a.getFloat(attr, c.motion.mQuantizeMotionPhase)
                QUANTIZE_MOTION_INTERPOLATOR -> {
                    val type = a.getResourceType(attr)
                    if (type == TypedValue.TYPE_REFERENCE) {
                        c.motion.mQuantizeInterpolatorID = a.getResourceId(attr, "")
                        if (c.motion.mQuantizeInterpolatorID != "") {
                            c.motion.mQuantizeInterpolatorType = Motion.INTERPOLATOR_REFERENCE_ID
                        }
                    } else if (type === TypedValue.TYPE_STRING) {
                        c.motion.mQuantizeInterpolatorString = a.getString(kvp.key, attr)
                        if (c.motion.mQuantizeInterpolatorString!!.indexOf("/") > 0) {
                            c.motion.mQuantizeInterpolatorID = a.getResourceId(attr, "")
                            c.motion.mQuantizeInterpolatorType = Motion.INTERPOLATOR_REFERENCE_ID
                        } else {
                            c.motion.mQuantizeInterpolatorType = Motion.SPLINE_STRING
                        }
                    } else {
                        c.motion.mQuantizeInterpolatorType = a.getInt(
                            attr,
                            -1
                        )
                    }
                }
                DRAW_PATH -> c.motion.mDrawPath = a.getInt(attr, 0)
                CHAIN_USE_RTL -> Log.e(TAG, "CURRENTLY UNSUPPORTED") // TODO add support or remove
                BARRIER_DIRECTION -> c.layout.mBarrierDirection =
                    a.getInt(attr, c.layout.mBarrierDirection)
                BARRIER_MARGIN -> c.layout.mBarrierMargin = a.getDimensionPixelSize(
                    attr,
                    c.layout.mBarrierMargin
                )
                CONSTRAINT_REFERENCED_IDS -> c.layout.mReferenceIdString = a.getString(
                    kvp.key,
                    attr
                )
                CONSTRAINT_TAG -> c.layout.mConstraintTag = a.getString(kvp.key, attr)
                BARRIER_ALLOWS_GONE_WIDGETS -> c.layout.mBarrierAllowsGoneWidgets = a.getBoolean(
                    attr,
                    c.layout.mBarrierAllowsGoneWidgets
                )
                UNUSED -> Log.w(
                    TAG,
                    "unused attribute 0x" + attr
                        .toString() + "   " + sMapToConstant.get(attr)
                )
                else -> Log.w(
                    TAG,
                    "Unknown attribute 0x" + attr
                        .toString() + "   " + sMapToConstant.get(attr)
                )
            }
        }
        if (c.layout.mReferenceIdString != null) {
            // in case the strings are set, make sure to clear up the cached ids
            c.layout.mReferenceIds = null
        }
    }

    private fun convertReferenceString(view: TView, referenceIdString: String?): Array<String> {
        if(referenceIdString == null)
            return arrayOf()
        val split: Array<String> = referenceIdString!!.split(",").toTypedArray()
        val context: TContext = view.getContext()
        var tags = Array<String>(split.size) { "" }
        var count = 0
        for (i in split.indices) {
            var idString = split[i]
            idString = idString.trim()
            var tag = ""
            try {
                tag = view.getObjCProperty(idString) as String? ?: ""
            } catch (e: Exception) {
                // Do nothing
            }
            if (tag == "") {
                tag = context.getResources().getIdentifier(
                    idString, "id",
                    context.getPackageName()
                )
            }
            //TODO
            /*if (tag == "" && view.isInEditMode() && view.getParent().getParentType() is ConstraintLayout) {
                val value: Object = view.getParent().getDesignInformation(0, idString)
                if (value != null && value is String) {
                    tag = value
                }
            }*/
            tags[count++] = tag
        }
        if (count != split.size) {
            tags = Arrays.copyOfNonNull(tags, count)
        }
        return tags
    }

    /**
     *
     */
    fun getConstraint(id: String): Constraint? {
        return if (mConstraints.containsKey(id)) {
            mConstraints[id]
        } else null
    }

    /**
     *
     */
    val knownIds: Array<String>
        get() {
            return mConstraints.keys.toTypedArray()
        }

    /**
     * Dump the contents
     *
     * @param scene
     * @param ids
     */
    fun dump(scene: MotionScene, vararg ids: String) {
        val keys: Set<String> = mConstraints.keys.toSet()
        val set: HashSet<String>
        if (ids.size != 0) {
            set = HashSet()
            for (id in ids) {
                set.add(id)
            }
        } else {
            set = HashSet(keys)
        }
        println(set.size.toString() + " constraints")
        val stringBuilder = StringBuilder()
        for (id in set.toTypedArray()) {
            val constraint = mConstraints[id] ?: continue
            stringBuilder.append("<Constraint id=")
            stringBuilder.append(id)
            stringBuilder.append(" \n")
            constraint.layout.dump(scene, stringBuilder)
            stringBuilder.append("/>\n")
        }
        println(stringBuilder.toString())
    }

    /**
     * Write the state to a Writer
     * @param writer
     * @param layout
     * @param flags
     * @throws IOException
     */
    fun writeState(writer: Writer, layout: ConstraintLayout, flags: Int) {
        writer.write("\n---------------------------------------------\n")
        if (flags and 1 == 1) {
            WriteXmlEngine(writer, layout, flags).writeLayout()
        } else {
            WriteJsonEngine(writer, layout, flags).writeLayout()
        }
        writer.write("\n---------------------------------------------\n")
    }

    internal inner class WriteXmlEngine(writer: Writer, layout: ConstraintLayout, flags: Int) {
        var mWriter: Writer
        var mLayout: ConstraintLayout
        var mContext: TContext
        var mFlags: Int
        var mUnknownCount = 0
        val mLEFT = "'left'"
        val mRIGHT = "'right'"
        val mBASELINE = "'baseline'"
        val mBOTTOM = "'bottom'"
        val mTOP = "'top'"
        val mSTART = "'start'"
        val mEND = "'end'"
        fun writeLayout() {
            mWriter.write("\n<ConstraintSet>\n")
            for (id in mConstraints.keys) {
                val c = mConstraints[id]
                val idName = getName(id)
                mWriter.write("  <Constraint")
                mWriter.write(Companion.SPACE + "android:id" + "=\"" + idName + "\"")
                val l = c!!.layout
                writeBaseDimension("android:layout_width", l.mWidth, -5)
                writeBaseDimension("android:layout_height", l.mHeight, -5)
                writeVariable(
                    "app:layout_constraintGuide_begin",
                    l.guideBegin.toFloat(),
                    UNSET.toFloat()
                )
                writeVariable(
                    "app:layout_constraintGuide_end",
                    l.guideEnd.toFloat(),
                    UNSET.toFloat()
                )
                writeVariable("app:layout_constraintGuide_percent", l.guidePercent, UNSET.toFloat())
                writeVariable(
                    "app:layout_constraintHorizontal_bias",
                    l.horizontalBias, 0.5f
                )
                writeVariable(
                    "app:layout_constraintVertical_bias",
                    l.verticalBias, 0.5f
                )
                writeVariable(
                    "app:layout_constraintDimensionRatio",
                    l.dimensionRatio, null
                )
                writeXmlConstraint("app:layout_constraintCircle", l.circleConstraint)
                writeVariable("app:layout_constraintCircleRadius", l.circleRadius.toFloat(), 0f)
                writeVariable("app:layout_constraintCircleAngle", l.circleAngle, 0f)
                writeVariable("android:orientation", l.orientation.toFloat(), UNSET.toFloat())
                writeVariable(
                    "app:layout_constraintVertical_weight",
                    l.verticalWeight, UNSET.toFloat()
                )
                writeVariable(
                    "app:layout_constraintHorizontal_weight",
                    l.horizontalWeight, UNSET.toFloat()
                )
                writeVariable(
                    "app:layout_constraintHorizontal_chainStyle",
                    l.horizontalChainStyle.toFloat(), CHAIN_SPREAD.toFloat()
                )
                writeVariable(
                    "app:layout_constraintVertical_chainStyle",
                    l.verticalChainStyle.toFloat(), CHAIN_SPREAD.toFloat()
                )
                writeVariable(
                    "app:barrierDirection",
                    l.mBarrierDirection.toFloat(),
                    UNSET.toFloat()
                )
                writeVariable("app:barrierMargin", l.mBarrierMargin.toFloat(), 0f)
                writeDimension("app:layout_marginLeft", l.leftMargin, 0)
                writeDimension(
                    "app:layout_goneMarginLeft",
                    l.goneLeftMargin, Layout.UNSET_GONE_MARGIN
                )
                writeDimension("app:layout_marginRight", l.rightMargin, 0)
                writeDimension(
                    "app:layout_goneMarginRight",
                    l.goneRightMargin, Layout.UNSET_GONE_MARGIN
                )
                writeDimension("app:layout_marginStart", l.startMargin, 0)
                writeDimension(
                    "app:layout_goneMarginStart",
                    l.goneStartMargin, Layout.UNSET_GONE_MARGIN
                )
                writeDimension("app:layout_marginEnd", l.endMargin, 0)
                writeDimension(
                    "app:layout_goneMarginEnd",
                    l.goneEndMargin, Layout.UNSET_GONE_MARGIN
                )
                writeDimension("app:layout_marginTop", l.topMargin, 0)
                writeDimension(
                    "app:layout_goneMarginTop",
                    l.goneTopMargin, Layout.UNSET_GONE_MARGIN
                )
                writeDimension("app:layout_marginBottom", l.bottomMargin, 0)
                writeDimension(
                    "app:layout_goneMarginBottom",
                    l.goneBottomMargin, Layout.UNSET_GONE_MARGIN
                )
                writeDimension(
                    "app:goneBaselineMargin",
                    l.goneBaselineMargin, Layout.UNSET_GONE_MARGIN
                )
                writeDimension("app:baselineMargin", l.baselineMargin, 0)
                writeBoolen("app:layout_constrainedWidth", l.constrainedWidth, false)
                writeBoolen(
                    "app:layout_constrainedHeight",
                    l.constrainedHeight, false
                )
                writeBoolen(
                    "app:barrierAllowsGoneWidgets",
                    l.mBarrierAllowsGoneWidgets, true
                )
                writeVariable(
                    "app:layout_wrapBehaviorInParent", l.mWrapBehavior.toFloat(),
                    ConstraintWidget.WRAP_BEHAVIOR_INCLUDED.toFloat()
                )
                writeXmlConstraint("app:baselineToBaseline", l.baselineToBaseline)
                writeXmlConstraint("app:baselineToBottom", l.baselineToBottom)
                writeXmlConstraint("app:baselineToTop", l.baselineToTop)
                writeXmlConstraint("app:layout_constraintBottom_toBottomOf", l.bottomToBottom)
                writeXmlConstraint("app:layout_constraintBottom_toTopOf", l.bottomToTop)
                writeXmlConstraint("app:layout_constraintEnd_toEndOf", l.endToEnd)
                writeXmlConstraint("app:layout_constraintEnd_toStartOf", l.endToStart)
                writeXmlConstraint("app:layout_constraintLeft_toLeftOf", l.leftToLeft)
                writeXmlConstraint("app:layout_constraintLeft_toRightOf", l.leftToRight)
                writeXmlConstraint("app:layout_constraintRight_toLeftOf", l.rightToLeft)
                writeXmlConstraint("app:layout_constraintRight_toRightOf", l.rightToRight)
                writeXmlConstraint("app:layout_constraintStart_toEndOf", l.startToEnd)
                writeXmlConstraint("app:layout_constraintStart_toStartOf", l.startToStart)
                writeXmlConstraint("app:layout_constraintTop_toBottomOf", l.topToBottom)
                writeXmlConstraint("app:layout_constraintTop_toTopOf", l.topToTop)
                val typesConstraintDefault = arrayOf("spread", "wrap", "percent")
                writeEnum(
                    "app:layout_constraintHeight_default", l.heightDefault,
                    typesConstraintDefault, ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                )
                writeVariable("app:layout_constraintHeight_percent", l.heightPercent, 1f)
                writeDimension("app:layout_constraintHeight_min", l.heightMin, 0)
                writeDimension("app:layout_constraintHeight_max", l.heightMax, 0)
                writeBoolen(
                    "android:layout_constrainedHeight",
                    l.constrainedHeight, false
                )
                writeEnum(
                    "app:layout_constraintWidth_default",
                    l.widthDefault, typesConstraintDefault,
                    ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                )
                writeVariable("app:layout_constraintWidth_percent", l.widthPercent, 1f)
                writeDimension("app:layout_constraintWidth_min", l.widthMin, 0)
                writeDimension("app:layout_constraintWidth_max", l.widthMax, 0)
                writeBoolen(
                    "android:layout_constrainedWidth",
                    l.constrainedWidth, false
                )
                writeVariable(
                    "app:layout_constraintVertical_weight",
                    l.verticalWeight, UNSET.toFloat()
                )
                writeVariable(
                    "app:layout_constraintHorizontal_weight",
                    l.horizontalWeight, UNSET.toFloat()
                )
                writeVariable(
                    "app:layout_constraintHorizontal_chainStyle",
                    l.horizontalChainStyle
                )
                writeVariable(
                    "app:layout_constraintVertical_chainStyle",
                    l.verticalChainStyle
                )
                val barrierDir = arrayOf("left", "right", "top", "bottom", "start", "end")
                writeEnum("app:barrierDirection", l.mBarrierDirection, barrierDir, UNSET)
                writeVariable("app:layout_constraintTag", l.mConstraintTag, null)
                if (l.mReferenceIds != null) {
                    writeVariable("'ReferenceIds'", l.mReferenceIds)
                }
                mWriter.write(" />\n")
            }
            mWriter.write("</ConstraintSet>\n")
        }

        private fun writeBoolen(dimString: String, `val`: Boolean, def: Boolean) {
            if (`val` != def) {
                mWriter.write(Companion.SPACE + dimString + "=\"" + `val` + "dp\"")
            }
        }

        private fun writeEnum(
            dimString: String,
            `val`: Int,
            types: Array<String>,
            def: Int
        ) {
            if (`val` != def) {
                mWriter.write(Companion.SPACE + dimString + "=\"" + types[`val`] + "\"")
            }
        }

        private fun writeDimension(dimString: String, dim: Int, def: Int) {
            if (dim != def) {
                mWriter.write(Companion.SPACE + dimString + "=\"" + dim + "dp\"")
            }
        }

        private fun writeBaseDimension(dimString: String, dim: Int, def: Int) {
            if (dim != def) {
                if (dim == -2) {
                    mWriter.write(Companion.SPACE + dimString + "=\"wrap_content\"")
                } else if (dim == -1) {
                    mWriter.write(Companion.SPACE + dimString + "=\"match_parent\"")
                } else {
                    mWriter.write(Companion.SPACE + dimString + "=\"" + dim + "dp\"")
                }
            }
        }

        var mIdMap: HashMap<String, String> = HashMap()

        init {
            mWriter = writer
            mLayout = layout
            mContext = layout.context
            mFlags = flags
        }

        fun getName(id: String): String {
            if (mIdMap.containsKey(id)) {
                return "@+id/" + mIdMap[id].toString() + ""
            }
            if (id == "") {
                return "parent"
            }
            val name = lookup(id)
            mIdMap[id] = name
            return "@+id/$name"
        }

        fun lookup(id: String): String {
            return try {
                if (id != "") {
                    mContext.getResources().getResourceEntryName(id)
                } else {
                    "unknown" + ++mUnknownCount
                }
            } catch (ex: Exception) {
                "unknown" + ++mUnknownCount
            }
        }

        fun writeXmlConstraint(str: String, leftToLeft: String) {
            if (leftToLeft == UNSET_ID) {
                return
            }
            mWriter.write(Companion.SPACE + str)
            mWriter.write("=\"" + getName(leftToLeft) + "\"")
        }

        fun writeConstraint(
            my: String, leftToLeft: String,
            other: String?,
            margin: Int,
            goneMargin: Int
        ) {
            if (leftToLeft == UNSET_ID) {
                return
            }
            mWriter.write(Companion.SPACE + my)
            mWriter.write(":[")
            mWriter.write(getName(leftToLeft))
            mWriter.write(" , ")
            mWriter.write(other ?: "")
            if (margin != 0) {
                mWriter.write(" , $margin")
            }
            mWriter.write("],\n")
        }

        fun writeCircle(
            circleConstraint: String,
            circleAngle: Float,
            circleRadius: Int
        ) {
            if (circleConstraint == UNSET_ID) {
                return
            }
            mWriter.write("circle")
            mWriter.write(":[")
            mWriter.write(getName(circleConstraint))
            mWriter.write(", $circleAngle")
            mWriter.write("$circleRadius]")
        }

        fun writeVariable(name: String, value: Int) {
            if (value == 0 || value == -1) {
                return
            }
            mWriter.write(
                """
                    ${Companion.SPACE}$name="$value"
                    
                    """.trimIndent()
            )
        }

        fun writeVariable(name: String, value: Float, def: Float) {
            if (value == def) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write("=\"$value\"")
        }

        fun writeVariable(name: String, value: String?, def: String?) {
            if (value == null || value.equals(def)) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write("=\"$value\"")
        }

        fun writeVariable(name: String, value: Array<String>?) {
            if (value == null) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(":")
            for (i in value.indices) {
                mWriter.write((if (i == 0) "[" else ", ") + getName(value[i]))
            }
            mWriter.write("],\n")
        }

        fun writeVariable(name: String?, value: String?) {
            if (value == null) {
                return
            }
            mWriter.write(name!!)
            mWriter.write(":")
            mWriter.write(", $value")
            mWriter.write("\n")
        }
    }

    // ================================== JSON ===============================================
    internal inner class WriteJsonEngine(writer: Writer, layout: ConstraintLayout, flags: Int) {
        var mWriter: Writer
        var mLayout: ConstraintLayout
        var mContext: TContext
        var mFlags: Int
        var mUnknownCount = 0
        val mLEFT = "'left'"
        val mRIGHT = "'right'"
        val mBASELINE = "'baseline'"
        val mBOTTOM = "'bottom'"
        val mTOP = "'top'"
        val mSTART = "'start'"
        val mEND = "'end'"
        fun writeLayout() {
            mWriter.write("\n\'ConstraintSet\':{\n")
            for (id in mConstraints.keys) {
                val c = mConstraints[id]
                val idName = getName(id)
                mWriter.write("$idName:{\n")
                val l = c!!.layout
                writeDimension(
                    "height", l.mHeight, l.heightDefault, l.heightPercent,
                    l.heightMin, l.heightMax, l.constrainedHeight
                )
                writeDimension(
                    "width", l.mWidth, l.widthDefault, l.widthPercent,
                    l.widthMin, l.widthMax, l.constrainedWidth
                )
                writeConstraint(mLEFT, l.leftToLeft, mLEFT, l.leftMargin, l.goneLeftMargin)
                writeConstraint(mLEFT, l.leftToRight, mRIGHT, l.leftMargin, l.goneLeftMargin)
                writeConstraint(mRIGHT, l.rightToLeft, mLEFT, l.rightMargin, l.goneRightMargin)
                writeConstraint(mRIGHT, l.rightToRight, mRIGHT, l.rightMargin, l.goneRightMargin)
                writeConstraint(
                    mBASELINE, l.baselineToBaseline, mBASELINE, UNSET,
                    l.goneBaselineMargin
                )
                writeConstraint(mBASELINE, l.baselineToTop, mTOP, UNSET, l.goneBaselineMargin)
                writeConstraint(
                    mBASELINE, l.baselineToBottom,
                    mBOTTOM, UNSET, l.goneBaselineMargin
                )
                writeConstraint(mTOP, l.topToBottom, mBOTTOM, l.topMargin, l.goneTopMargin)
                writeConstraint(mTOP, l.topToTop, mTOP, l.topMargin, l.goneTopMargin)
                writeConstraint(
                    mBOTTOM, l.bottomToBottom, mBOTTOM, l.bottomMargin,
                    l.goneBottomMargin
                )
                writeConstraint(mBOTTOM, l.bottomToTop, mTOP, l.bottomMargin, l.goneBottomMargin)
                writeConstraint(mSTART, l.startToStart, mSTART, l.startMargin, l.goneStartMargin)
                writeConstraint(mSTART, l.startToEnd, mEND, l.startMargin, l.goneStartMargin)
                writeConstraint(mEND, l.endToStart, mSTART, l.endMargin, l.goneEndMargin)
                writeConstraint(mEND, l.endToEnd, mEND, l.endMargin, l.goneEndMargin)
                writeVariable("'horizontalBias'", l.horizontalBias, 0.5f)
                writeVariable("'verticalBias'", l.verticalBias, 0.5f)
                writeCircle(l.circleConstraint, l.circleAngle, l.circleRadius)
                writeGuideline(l.orientation, l.guideBegin, l.guideEnd, l.guidePercent)
                writeVariable("'dimensionRatio'", l.dimensionRatio)
                writeVariable("'barrierMargin'", l.mBarrierMargin)
                writeVariable("'type'", l.mHelperType)
                writeVariable("'ReferenceId'", l.mReferenceIdString)
                writeVariable(
                    "'mBarrierAllowsGoneWidgets'",
                    l.mBarrierAllowsGoneWidgets, true
                )
                writeVariable("'WrapBehavior'", l.mWrapBehavior)
                writeVariable("'verticalWeight'", l.verticalWeight)
                writeVariable("'horizontalWeight'", l.horizontalWeight)
                writeVariable("'horizontalChainStyle'", l.horizontalChainStyle)
                writeVariable("'verticalChainStyle'", l.verticalChainStyle)
                writeVariable("'barrierDirection'", l.mBarrierDirection)
                if (l.mReferenceIds != null) {
                    writeVariable("'ReferenceIds'", l.mReferenceIds)
                }
                mWriter.write("}\n")
            }
            mWriter.write("}\n")
        }

        private fun writeGuideline(
            orientation: Int,
            guideBegin: Int,
            guideEnd: Int,
            guidePercent: Float
        ) {
            writeVariable("'orientation'", orientation)
            writeVariable("'guideBegin'", guideBegin)
            writeVariable("'guideEnd'", guideEnd)
            writeVariable("'guidePercent'", guidePercent)
        }

        private fun writeDimension(
            dimString: String,
            dim: Int,
            dimDefault: Int,
            dimPercent: Float,
            dimMin: Int,
            dimMax: Int,
            unusedConstrainedDim: Boolean
        ) {
            if (dim == 0) {
                if (dimMax != UNSET || dimMin != UNSET) {
                    when (dimDefault) {
                        0 -> mWriter.write(
                            """
                            ${Companion.SPACE}$dimString: {'spread' ,$dimMin, $dimMax}
                            
                            """.trimIndent()
                        )
                        1 -> {
                            mWriter.write(
                                """
                                    ${Companion.SPACE}$dimString: {'wrap' ,$dimMin, $dimMax}
                                    
                                    """.trimIndent()
                            )
                            return
                        }
                        2 -> {
                            mWriter.write(
                                """
                                    ${Companion.SPACE}$dimString: {'$dimPercent'% ,$dimMin, $dimMax}
                                    
                                    """.trimIndent()
                            )
                            return
                        }
                    }
                    return
                }
                when (dimDefault) {
                    0 -> {}
                    1 -> {
                        mWriter.write(
                            """
                                ${Companion.SPACE}$dimString: '???????????',
                                
                                """.trimIndent()
                        )
                        return
                    }
                    2 -> {
                        mWriter.write(
                            """
                                ${Companion.SPACE}$dimString: '$dimPercent%',
                                
                                """.trimIndent()
                        )
                        return
                    }
                }
            } else if (dim == -2) {
                mWriter.write(
                    """
                        ${Companion.SPACE}$dimString: 'wrap'
                        
                        """.trimIndent()
                )
            } else if (dim == -1) {
                mWriter.write(
                    """
                        ${Companion.SPACE}$dimString: 'parent'
                        
                        """.trimIndent()
                )
            } else {
                mWriter.write(
                    """
                        ${Companion.SPACE}$dimString: $dim,
                        
                        """.trimIndent()
                )
            }
        }

        var mIdMap: HashMap<String, String> = HashMap()

        init {
            mWriter = writer
            mLayout = layout
            mContext = layout.self.getContext()
            mFlags = flags
        }

        fun getName(id: String): String {
            if (mIdMap.containsKey(id)) {
                return "\'" + mIdMap[id].toString() + "\'"
            }
            if (id == "") {
                return "'parent'"
            }
            val name = lookup(id)
            mIdMap[id] = name
            return "\'" + name + "\'"
        }

        fun lookup(id: String): String {
            return try {
                if (id != "") {
                    mContext.getResources().getResourceEntryName(id)
                } else {
                    "unknown" + ++mUnknownCount
                }
            } catch (ex: Exception) {
                "unknown" + ++mUnknownCount
            }
        }

        fun writeConstraint(
            my: String,
            leftToLeft: String,
            other: String?,
            margin: Int,
            goneMargin: Int
        ) {
            if (leftToLeft == UNSET_ID) {
                return
            }
            mWriter.write(Companion.SPACE + my)
            mWriter.write(":[")
            mWriter.write(getName(leftToLeft))
            mWriter.write(" , ")
            mWriter.write(other ?: "")
            if (margin != 0) {
                mWriter.write(" , $margin")
            }
            mWriter.write("],\n")
        }

        fun writeCircle(
            circleConstraint: String,
            circleAngle: Float,
            circleRadius: Int
        ) {
            if (circleConstraint == UNSET_ID) {
                return
            }
            mWriter.write(Companion.SPACE + "circle")
            mWriter.write(":[")
            mWriter.write(getName(circleConstraint))
            mWriter.write(", $circleAngle")
            mWriter.write("$circleRadius]")
        }

        fun writeVariable(name: String, value: Int) {
            if (value == 0 || value == -1) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(":")
            mWriter.write(", $value")
            mWriter.write("\n")
        }

        fun writeVariable(name: String, value: Float) {
            if (value == UNSET.toFloat()) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(": $value")
            mWriter.write(",\n")
        }

        fun writeVariable(name: String, value: Float, def: Float) {
            if (value == def) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(": $value")
            mWriter.write(",\n")
        }

        fun writeVariable(name: String, value: Boolean) {
            if (!value) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(": $value")
            mWriter.write(",\n")
        }

        fun writeVariable(name: String, value: Boolean, def: Boolean) {
            if (value == def) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(": $value")
            mWriter.write(",\n")
        }

        fun writeVariable(name: String, value: Array<String>?) {
            if (value == null) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(": ")
            for (i in value.indices) {
                mWriter.write((if (i == 0) "[" else ", ") + getName(value[i]))
            }
            mWriter.write("],\n")
        }

        fun writeVariable(name: String, value: String?) {
            if (value == null) {
                return
            }
            mWriter.write(Companion.SPACE + name)
            mWriter.write(":")
            mWriter.write(", $value")
            mWriter.write("\n")
        }
    }

    companion object {
        private const val TAG = "ConstraintSet"
        private const val ERROR_MESSAGE = "XML parser error must be within a Constraint "
        private const val INTERNAL_MATCH_PARENT = -1
        private const val INTERNAL_WRAP_CONTENT = -2
        private const val INTERNAL_MATCH_CONSTRAINT = -3
        private const val INTERNAL_WRAP_CONTENT_CONSTRAINED = -4
        const val ROTATE_NONE = 0
        const val ROTATE_PORTRATE_OF_RIGHT = 1
        const val ROTATE_PORTRATE_OF_LEFT = 2
        const val ROTATE_RIGHT_OF_PORTRATE = 3
        const val ROTATE_LEFT_OF_PORTRATE = 4
        private const val SPACE = "       "

        /**
         * Used to indicate a parameter is cleared or not set
         */
        val UNSET: Int = -1
        val UNSET_ID: String = ""

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
        val MATCH_CONSTRAINT_PERCENT: Int = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_PERCENT

        /**
         * References the id of the parent.
         * Used in:
         *
         *  * [.connect]
         *  * [.center]
         *
         */
        val PARENT_ID = ConstraintLayout.LayoutParams.PARENT_ID

        /**
         * The horizontal orientation.
         */
        val HORIZONTAL: Int = ConstraintLayout.LayoutParams.HORIZONTAL

        /**
         * The vertical orientation.
         */
        val VERTICAL: Int = ConstraintLayout.LayoutParams.VERTICAL

        /**
         * Used to create a horizontal create guidelines.
         */
        const val HORIZONTAL_GUIDELINE = 0

        /**
         * Used to create a vertical create guidelines.
         * see [.create]
         */
        const val VERTICAL_GUIDELINE = 1

        /**
         * This view is visible.
         * Use with [.setVisibility] and [`android:visibility`.
](#attr_android:visibility) */
        val VISIBLE: Int = TView.VISIBLE

        /**
         * This view is invisible, but it still takes up space for layout purposes.
         * Use with [.setVisibility] and [`android:visibility`.
](#attr_android:visibility) */
        val INVISIBLE: Int = TView.INVISIBLE

        /**
         * This view is gone, and will not take any space for layout
         * purposes. Use with [.setVisibility] and [`android:visibility`.
](#attr_android:visibility) */
        val GONE: Int = TView.GONE

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
         * Circle reference from a view.
         */
        val CIRCLE_REFERENCE: Int = ConstraintLayout.LayoutParams.CIRCLE

        /**
         * Chain spread style
         */
        val CHAIN_SPREAD: Int = ConstraintLayout.LayoutParams.CHAIN_SPREAD

        /**
         * Chain spread inside style
         */
        val CHAIN_SPREAD_INSIDE: Int = ConstraintLayout.LayoutParams.CHAIN_SPREAD_INSIDE
        const val VISIBILITY_MODE_NORMAL = 0
        const val VISIBILITY_MODE_IGNORE = 1

        /**
         * Chain packed style
         */
        val CHAIN_PACKED: Int = ConstraintLayout.LayoutParams.CHAIN_PACKED
        private const val I_DEBUG = false
        private val VISIBILITY_FLAGS = intArrayOf(VISIBLE, INVISIBLE, GONE)
        private const val BARRIER_TYPE = 1
        private val sMapToConstant = mutableMapOf<String, Int>()
        private val sOverrideMapToConstant = mutableMapOf<String, Int>()
        private const val BASELINE_TO_BASELINE = 1
        private const val BOTTOM_MARGIN = 2
        private const val BOTTOM_TO_BOTTOM = 3
        private const val BOTTOM_TO_TOP = 4
        private const val DIMENSION_RATIO = 5
        private const val EDITOR_ABSOLUTE_X = 6
        private const val EDITOR_ABSOLUTE_Y = 7
        private const val END_MARGIN = 8
        private const val END_TO_END = 9
        private const val END_TO_START = 10
        private const val GONE_BOTTOM_MARGIN = 11
        private const val GONE_END_MARGIN = 12
        private const val GONE_LEFT_MARGIN = 13
        private const val GONE_RIGHT_MARGIN = 14
        private const val GONE_START_MARGIN = 15
        private const val GONE_TOP_MARGIN = 16
        private const val GUIDE_BEGIN = 17
        private const val GUIDE_END = 18
        private const val GUIDE_PERCENT = 19
        private const val HORIZONTAL_BIAS = 20
        private const val LAYOUT_HEIGHT = 21
        private const val LAYOUT_VISIBILITY = 22
        private const val LAYOUT_WIDTH = 23
        private const val LEFT_MARGIN = 24
        private const val LEFT_TO_LEFT = 25
        private const val LEFT_TO_RIGHT = 26
        private const val ORIENTATION = 27
        private const val RIGHT_MARGIN = 28
        private const val RIGHT_TO_LEFT = 29
        private const val RIGHT_TO_RIGHT = 30
        private const val START_MARGIN = 31
        private const val START_TO_END = 32
        private const val START_TO_START = 33
        private const val TOP_MARGIN = 34
        private const val TOP_TO_BOTTOM = 35
        private const val TOP_TO_TOP = 36
        private const val VERTICAL_BIAS = 37
        private const val VIEW_ID = 38
        private const val HORIZONTAL_WEIGHT = 39
        private const val VERTICAL_WEIGHT = 40
        private const val HORIZONTAL_STYLE = 41
        private const val VERTICAL_STYLE = 42
        private const val ALPHA = 43
        private const val ELEVATION = 44
        private const val ROTATION_X = 45
        private const val ROTATION_Y = 46
        private const val SCALE_X = 47
        private const val SCALE_Y = 48
        private const val TRANSFORM_PIVOT_X = 49
        private const val TRANSFORM_PIVOT_Y = 50
        private const val TRANSLATION_X = 51
        private const val TRANSLATION_Y = 52
        private const val TRANSLATION_Z = 53
        private const val WIDTH_DEFAULT = 54
        private const val HEIGHT_DEFAULT = 55
        private const val WIDTH_MAX = 56
        private const val HEIGHT_MAX = 57
        private const val WIDTH_MIN = 58
        private const val HEIGHT_MIN = 59
        private const val ROTATION = 60
        private const val CIRCLE = 61
        private const val CIRCLE_RADIUS = 62
        private const val CIRCLE_ANGLE = 63
        private const val ANIMATE_RELATIVE_TO = 64
        private const val TRANSITION_EASING = 65
        private const val DRAW_PATH = 66
        private const val TRANSITION_PATH_ROTATE = 67
        private const val PROGRESS = 68
        private const val WIDTH_PERCENT = 69
        private const val HEIGHT_PERCENT = 70
        private const val CHAIN_USE_RTL = 71
        private const val BARRIER_DIRECTION = 72
        private const val BARRIER_MARGIN = 73
        private const val CONSTRAINT_REFERENCED_IDS = 74
        private const val BARRIER_ALLOWS_GONE_WIDGETS = 75
        private const val PATH_MOTION_ARC = 76
        private const val CONSTRAINT_TAG = 77
        private const val VISIBILITY_MODE = 78
        private const val MOTION_STAGGER = 79
        private const val CONSTRAINED_WIDTH = 80
        private const val CONSTRAINED_HEIGHT = 81
        private const val ANIMATE_CIRCLE_ANGLE_TO = 82
        private const val TRANSFORM_PIVOT_TARGET = 83
        private const val QUANTIZE_MOTION_STEPS = 84
        private const val QUANTIZE_MOTION_PHASE = 85
        private const val QUANTIZE_MOTION_INTERPOLATOR = 86
        private const val UNUSED = 87
        private const val QUANTIZE_MOTION_INTERPOLATOR_TYPE = 88
        private const val QUANTIZE_MOTION_INTERPOLATOR_ID = 89
        private const val QUANTIZE_MOTION_INTERPOLATOR_STR = 90
        private const val BASELINE_TO_TOP = 91
        private const val BASELINE_TO_BOTTOM = 92
        private const val BASELINE_MARGIN = 93
        private const val GONE_BASELINE_MARGIN = 94
        private const val LAYOUT_CONSTRAINT_WIDTH = 95
        private const val LAYOUT_CONSTRAINT_HEIGHT = 96
        private const val LAYOUT_WRAP_BEHAVIOR = 97
        private const val MOTION_TARGET = 98
        private const val GUIDELINE_USE_RTL = 99
        private const val KEY_WEIGHT = "weight"
        private const val KEY_RATIO = "ratio"
        private const val KEY_PERCENT_PARENT = "parent"

        init {
            sMapToConstant.put(
                "layout_constraintLeft_toLeftOf",
                LEFT_TO_LEFT
            )
            sMapToConstant.put(
                "layout_constraintLeft_toRightOf",
                LEFT_TO_RIGHT
            )
            sMapToConstant.put(
                "layout_constraintRight_toLeftOf",
                RIGHT_TO_LEFT
            )
            sMapToConstant.put(
                "layout_constraintRight_toRightOf", RIGHT_TO_RIGHT
            )
            sMapToConstant.put("layout_constraintTop_toTopOf", TOP_TO_TOP)
            sMapToConstant.put(
                "layout_constraintTop_toBottomOf",
                TOP_TO_BOTTOM
            )
            sMapToConstant.put(
                "layout_constraintBottom_toTopOf",
                BOTTOM_TO_TOP
            )
            sMapToConstant.put(
                "layout_constraintBottom_toBottomOf", BOTTOM_TO_BOTTOM
            )
            sMapToConstant.put(
                "layout_constraintBaseline_toBaselineOf",
                BASELINE_TO_BASELINE
            )
            sMapToConstant.put(
                "layout_constraintBaseline_toTopOf", BASELINE_TO_TOP
            )
            sMapToConstant.put(
                "layout_constraintBaseline_toBottomOf", BASELINE_TO_BOTTOM
            )
            sMapToConstant.put("layout_editor_absoluteX", EDITOR_ABSOLUTE_X)
            sMapToConstant.put("layout_editor_absoluteY", EDITOR_ABSOLUTE_Y)
            sMapToConstant.put("layout_constraintGuide_begin", GUIDE_BEGIN)
            sMapToConstant.put("layout_constraintGuide_end", GUIDE_END)
            sMapToConstant.put(
                "layout_constraintGuide_percent",
                GUIDE_PERCENT
            )
            sMapToConstant.put("guidelineUseRtl", GUIDELINE_USE_RTL)
            sMapToConstant.put("android_orientation", ORIENTATION)
            sMapToConstant.put(
                "layout_constraintStart_toEndOf",
                START_TO_END
            )
            sMapToConstant.put(
                "layout_constraintStart_toStartOf", START_TO_START
            )
            sMapToConstant.put(
                "layout_constraintEnd_toStartOf",
                END_TO_START
            )
            sMapToConstant.put("layout_constraintEnd_toEndOf", END_TO_END)
            sMapToConstant.put("layout_goneMarginLeft", GONE_LEFT_MARGIN)
            sMapToConstant.put("layout_goneMarginTop", GONE_TOP_MARGIN)
            sMapToConstant.put("layout_goneMarginRight", GONE_RIGHT_MARGIN)
            sMapToConstant.put(
                "layout_goneMarginBottom",
                GONE_BOTTOM_MARGIN
            )
            sMapToConstant.put("layout_goneMarginStart", GONE_START_MARGIN)
            sMapToConstant.put("layout_goneMarginEnd", GONE_END_MARGIN)
            sMapToConstant.put(
                "layout_constraintVertical_weight", VERTICAL_WEIGHT
            )
            sMapToConstant.put(
                "layout_constraintHorizontal_weight", HORIZONTAL_WEIGHT
            )
            sMapToConstant.put(
                "layout_constraintHorizontal_chainStyle", HORIZONTAL_STYLE
            )
            sMapToConstant.put(
                "layout_constraintVertical_chainStyle", VERTICAL_STYLE
            )
            sMapToConstant.put(
                "layout_constraintHorizontal_bias", HORIZONTAL_BIAS
            )
            sMapToConstant.put(
                "layout_constraintVertical_bias", VERTICAL_BIAS
            )
            sMapToConstant.put(
                "layout_constraintDimensionRatio", DIMENSION_RATIO
            )
            sMapToConstant.put("layout_constraintLeft_creator", UNUSED)
            sMapToConstant.put("layout_constraintTop_creator", UNUSED)
            sMapToConstant.put("layout_constraintRight_creator", UNUSED)
            sMapToConstant.put("layout_constraintBottom_creator", UNUSED)
            sMapToConstant.put("layout_constraintBaseline_creator", UNUSED)
            sMapToConstant.put("android_layout_marginLeft", LEFT_MARGIN)
            sMapToConstant.put("android_layout_marginRight", RIGHT_MARGIN)
            sMapToConstant.put("android_layout_marginStart", START_MARGIN)
            sMapToConstant.put("android_layout_marginEnd", END_MARGIN)
            sMapToConstant.put("android_layout_marginTop", TOP_MARGIN)
            sMapToConstant.put("android_layout_marginBottom", BOTTOM_MARGIN)
            sMapToConstant.put("android_layout_width", LAYOUT_WIDTH)
            sMapToConstant.put(
                "android_layout_height", LAYOUT_HEIGHT
            )
            sMapToConstant.put(
                "layout_constraintWidth", LAYOUT_CONSTRAINT_WIDTH
            )
            sMapToConstant.put(
                "layout_constraintHeight", LAYOUT_CONSTRAINT_HEIGHT
            )
            sMapToConstant.put("android_visibility", LAYOUT_VISIBILITY)
            sMapToConstant.put("android_alpha", ALPHA)
            sMapToConstant.put("android_elevation", ELEVATION)
            sMapToConstant.put("android_rotationX", ROTATION_X)
            sMapToConstant.put("android_rotationY", ROTATION_Y)
            sMapToConstant.put("android_rotation", ROTATION)
            sMapToConstant.put("android_scaleX", SCALE_X)
            sMapToConstant.put("android_scaleY", SCALE_Y)
            sMapToConstant.put("android_transformPivotX", TRANSFORM_PIVOT_X)
            sMapToConstant.put("android_transformPivotY", TRANSFORM_PIVOT_Y)
            sMapToConstant.put("android_translationX", TRANSLATION_X)
            sMapToConstant.put("android_translationY", TRANSLATION_Y)
            sMapToConstant.put("android_translationZ", TRANSLATION_Z)
            sMapToConstant.put(
                "layout_constraintWidth_default",
                WIDTH_DEFAULT
            )
            sMapToConstant.put(
                "layout_constraintHeight_default", HEIGHT_DEFAULT
            )
            sMapToConstant.put("layout_constraintWidth_max", WIDTH_MAX)
            sMapToConstant.put("layout_constraintHeight_max", HEIGHT_MAX)
            sMapToConstant.put("layout_constraintWidth_min", WIDTH_MIN)
            sMapToConstant.put("layout_constraintHeight_min", HEIGHT_MIN)
            sMapToConstant.put("layout_constraintCircle", CIRCLE)
            sMapToConstant.put(
                "layout_constraintCircleRadius",
                CIRCLE_RADIUS
            )
            sMapToConstant.put("layout_constraintCircleAngle", CIRCLE_ANGLE)
            sMapToConstant.put("animateRelativeTo", ANIMATE_RELATIVE_TO)
            sMapToConstant.put("transitionEasing", TRANSITION_EASING)
            sMapToConstant.put("drawPath", DRAW_PATH)
            sMapToConstant.put(
                "transitionPathRotate",
                TRANSITION_PATH_ROTATE
            )
            sMapToConstant.put("motionStagger", MOTION_STAGGER)
            sMapToConstant.put("android_id", VIEW_ID)
            sMapToConstant.put("motionProgress", PROGRESS)
            sMapToConstant.put(
                "layout_constraintWidth_percent", WIDTH_PERCENT
            )
            sMapToConstant.put(
                "layout_constraintHeight_percent", HEIGHT_PERCENT
            )
            sMapToConstant.put(
                "layout_wrapBehaviorInParent", LAYOUT_WRAP_BEHAVIOR
            )
            sMapToConstant.put("chainUseRtl", CHAIN_USE_RTL)
            sMapToConstant.put("barrierDirection", BARRIER_DIRECTION)
            sMapToConstant.put("barrierMargin", BARRIER_MARGIN)
            sMapToConstant.put(
                "constraint_referenced_ids", CONSTRAINT_REFERENCED_IDS
            )
            sMapToConstant.put(
                "barrierAllowsGoneWidgets", BARRIER_ALLOWS_GONE_WIDGETS
            )
            sMapToConstant.put("pathMotionArc", PATH_MOTION_ARC)
            sMapToConstant.put("layout_constraintTag", CONSTRAINT_TAG)
            sMapToConstant.put("visibilityMode", VISIBILITY_MODE)
            sMapToConstant.put(
                "layout_constrainedWidth", CONSTRAINED_WIDTH
            )
            sMapToConstant.put(
                "layout_constrainedHeight", CONSTRAINED_HEIGHT
            )
            sMapToConstant.put(
                "polarRelativeTo", ANIMATE_CIRCLE_ANGLE_TO
            )
            sMapToConstant.put(
                "transformPivotTarget", TRANSFORM_PIVOT_TARGET
            )
            sMapToConstant.put(
                "quantizeMotionSteps", QUANTIZE_MOTION_STEPS
            )
            sMapToConstant.put(
                "quantizeMotionPhase", QUANTIZE_MOTION_PHASE
            )
            sMapToConstant.put(
                "quantizeMotionInterpolator", QUANTIZE_MOTION_INTERPOLATOR
            )


            /*
        The tags not available in constraintOverride
        Left here to help with documentation and understanding
        overrideMapToConstant.put(
        "layout_constraintLeft_toLeftOf, LEFT_TO_LEFT);
        overrideMapToConstant.put(
        "layout_constraintLeft_toRightOf, LEFT_TO_RIGHT);
        overrideMapToConstant.put(
        "layout_constraintRight_toLeftOf, RIGHT_TO_LEFT);
        overrideMapToConstant.put(
        "layout_constraintRight_toRightOf, RIGHT_TO_RIGHT);
        overrideMapToConstant.put(
        "layout_constraintTop_toTopOf, TOP_TO_TOP);
        overrideMapToConstant.put(
        "layout_constraintTop_toBottomOf, TOP_TO_BOTTOM);
        overrideMapToConstant.put(
        "layout_constraintBottom_toTopOf, BOTTOM_TO_TOP);
        overrideMapToConstant.put(
        "layout_constraintBottom_toBottomOf, BOTTOM_TO_BOTTOM);
        overrideMapToConstant.put(
        "layout_constraintBaseline_toBaselineOf,
        BASELINE_TO_BASELINE);
        overrideMapToConstant.put(
        "layout_constraintGuide_begin, GUIDE_BEGIN);
        overrideMapToConstant.put(
        "layout_constraintGuide_end, GUIDE_END);
        overrideMapToConstant.put(
        "layout_constraintGuide_percent, GUIDE_PERCENT);
        overrideMapToConstant.put(
        "layout_constraintStart_toEndOf, START_TO_END);
        overrideMapToConstant.put(
        "layout_constraintStart_toStartOf, START_TO_START);
        overrideMapToConstant.put(
        "layout_constraintEnd_toStartOf, END_TO_START);
        overrideMapToConstant.put(
        "layout_constraintEnd_toEndOf, END_TO_END);
        */sOverrideMapToConstant.put(
                "layout_editor_absoluteY", EDITOR_ABSOLUTE_X
            )
            sOverrideMapToConstant.put(
                "layout_editor_absoluteY", EDITOR_ABSOLUTE_Y
            )
            sOverrideMapToConstant.put(
                "android_orientation", ORIENTATION
            )
            sOverrideMapToConstant.put(
                "layout_goneMarginLeft", GONE_LEFT_MARGIN
            )
            sOverrideMapToConstant.put(
                "layout_goneMarginTop", GONE_TOP_MARGIN
            )
            sOverrideMapToConstant.put(
                "layout_goneMarginRight", GONE_RIGHT_MARGIN
            )
            sOverrideMapToConstant.put(
                "layout_goneMarginBottom", GONE_BOTTOM_MARGIN
            )
            sOverrideMapToConstant.put(
                "layout_goneMarginStart", GONE_START_MARGIN
            )
            sOverrideMapToConstant.put(
                "layout_goneMarginEnd", GONE_END_MARGIN
            )
            sOverrideMapToConstant.put(
                "layout_constraintVertical_weight",
                VERTICAL_WEIGHT
            )
            sOverrideMapToConstant.put(
                "layout_constraintHorizontal_weight",
                HORIZONTAL_WEIGHT
            )
            sOverrideMapToConstant.put(
                "layout_constraintHorizontal_chainStyle",
                HORIZONTAL_STYLE
            )
            sOverrideMapToConstant.put(
                "layout_constraintVertical_chainStyle",
                VERTICAL_STYLE
            )
            sOverrideMapToConstant.put(
                "layout_constraintHorizontal_bias",
                HORIZONTAL_BIAS
            )
            sOverrideMapToConstant.put(
                "layout_constraintVertical_bias",
                VERTICAL_BIAS
            )
            sOverrideMapToConstant.put(
                "layout_constraintDimensionRatio", DIMENSION_RATIO
            )
            sOverrideMapToConstant.put(
                "layout_constraintLeft_creator",
                UNUSED
            )
            sOverrideMapToConstant.put(
                "layout_constraintTop_creator",
                UNUSED
            )
            sOverrideMapToConstant.put(
                "layout_constraintRight_creator",
                UNUSED
            )
            sOverrideMapToConstant.put(
                "layout_constraintBottom_creator", UNUSED
            )
            sOverrideMapToConstant.put(
                "layout_constraintBaseline_creator",
                UNUSED
            )
            sOverrideMapToConstant.put(
                "android_layout_marginLeft",
                LEFT_MARGIN
            )
            sOverrideMapToConstant.put(
                "android_layout_marginRight",
                RIGHT_MARGIN
            )
            sOverrideMapToConstant.put(
                "android_layout_marginStart",
                START_MARGIN
            )
            sOverrideMapToConstant.put(
                "android_layout_marginEnd",
                END_MARGIN
            )
            sOverrideMapToConstant.put(
                "android_layout_marginTop",
                TOP_MARGIN
            )
            sOverrideMapToConstant.put(
                "android_layout_marginBottom",
                BOTTOM_MARGIN
            )
            sOverrideMapToConstant.put(
                "android_layout_width",
                LAYOUT_WIDTH
            )
            sOverrideMapToConstant.put(
                "android_layout_height",
                LAYOUT_HEIGHT
            )
            sOverrideMapToConstant.put(
                "layout_constraintWidth",
                LAYOUT_CONSTRAINT_WIDTH
            )
            sOverrideMapToConstant.put(
                "layout_constraintHeight",
                LAYOUT_CONSTRAINT_HEIGHT
            )
            sOverrideMapToConstant.put(
                "android_visibility",
                LAYOUT_VISIBILITY
            )
            sOverrideMapToConstant.put("android_alpha", ALPHA)
            sOverrideMapToConstant.put(
                "android_elevation",
                ELEVATION
            )
            sOverrideMapToConstant.put(
                "android_rotationX",
                ROTATION_X
            )
            sOverrideMapToConstant.put(
                "android_rotationY",
                ROTATION_Y
            )
            sOverrideMapToConstant.put("android_rotation", ROTATION)
            sOverrideMapToConstant.put("android_scaleX", SCALE_X)
            sOverrideMapToConstant.put("android_scaleY", SCALE_Y)
            sOverrideMapToConstant.put(
                "android_transformPivotX",
                TRANSFORM_PIVOT_X
            )
            sOverrideMapToConstant.put(
                "android_transformPivotY",
                TRANSFORM_PIVOT_Y
            )
            sOverrideMapToConstant.put(
                "android_translationX",
                TRANSLATION_X
            )
            sOverrideMapToConstant.put(
                "android_translationY",
                TRANSLATION_Y
            )
            sOverrideMapToConstant.put(
                "android_translationZ",
                TRANSLATION_Z
            )
            sOverrideMapToConstant.put(
                "layout_constraintWidth_default",
                WIDTH_DEFAULT
            )
            sOverrideMapToConstant.put(
                "layout_constraintHeight_default", HEIGHT_DEFAULT
            )
            sOverrideMapToConstant.put(
                "layout_constraintWidth_max",
                WIDTH_MAX
            )
            sOverrideMapToConstant.put(
                "layout_constraintHeight_max",
                HEIGHT_MAX
            )
            sOverrideMapToConstant.put(
                "layout_constraintWidth_min",
                WIDTH_MIN
            )
            sOverrideMapToConstant.put(
                "layout_constraintHeight_min",
                HEIGHT_MIN
            )
            sOverrideMapToConstant.put(
                "layout_constraintCircleRadius",
                CIRCLE_RADIUS
            )
            sOverrideMapToConstant.put(
                "layout_constraintCircleAngle",
                CIRCLE_ANGLE
            )
            sOverrideMapToConstant.put(
                "animateRelativeTo",
                ANIMATE_RELATIVE_TO
            )
            sOverrideMapToConstant.put(
                "transitionEasing",
                TRANSITION_EASING
            )
            sOverrideMapToConstant.put("drawPath", DRAW_PATH)
            sOverrideMapToConstant.put(
                "transitionPathRotate",
                TRANSITION_PATH_ROTATE
            )
            sOverrideMapToConstant.put(
                "motionStagger",
                MOTION_STAGGER
            )
            sOverrideMapToConstant.put("android_id", VIEW_ID)
            sOverrideMapToConstant.put(
                "motionTarget",
                MOTION_TARGET
            )
            sOverrideMapToConstant.put("motionProgress", PROGRESS)
            sOverrideMapToConstant.put(
                "layout_constraintWidth_percent",
                WIDTH_PERCENT
            )
            sOverrideMapToConstant.put(
                "layout_constraintHeight_percent", HEIGHT_PERCENT
            )
            sOverrideMapToConstant.put("chainUseRtl", CHAIN_USE_RTL)
            sOverrideMapToConstant.put(
                "barrierDirection",
                BARRIER_DIRECTION
            )
            sOverrideMapToConstant.put(
                "barrierMargin",
                BARRIER_MARGIN
            )
            sOverrideMapToConstant.put(
                "constraint_referenced_ids",
                CONSTRAINT_REFERENCED_IDS
            )
            sOverrideMapToConstant.put(
                "barrierAllowsGoneWidgets",
                BARRIER_ALLOWS_GONE_WIDGETS
            )
            sOverrideMapToConstant.put(
                "pathMotionArc",
                PATH_MOTION_ARC
            )
            sOverrideMapToConstant.put(
                "layout_constraintTag",
                CONSTRAINT_TAG
            )
            sOverrideMapToConstant.put(
                "visibilityMode",
                VISIBILITY_MODE
            )
            sOverrideMapToConstant.put(
                "layout_constrainedWidth",
                CONSTRAINED_WIDTH
            )
            sOverrideMapToConstant.put(
                "layout_constrainedHeight",
                CONSTRAINED_HEIGHT
            )
            sOverrideMapToConstant.put(
                "polarRelativeTo",
                ANIMATE_CIRCLE_ANGLE_TO
            )
            sOverrideMapToConstant.put(
                "transformPivotTarget",
                TRANSFORM_PIVOT_TARGET
            )
            sOverrideMapToConstant.put(
                "quantizeMotionSteps",
                QUANTIZE_MOTION_STEPS
            )
            sOverrideMapToConstant.put(
                "quantizeMotionPhase",
                QUANTIZE_MOTION_PHASE
            )
            sOverrideMapToConstant.put(
                "quantizeMotionInterpolator",
                QUANTIZE_MOTION_INTERPOLATOR
            )
            sOverrideMapToConstant.put(
                "layout_wrapBehaviorInParent",
                LAYOUT_WRAP_BEHAVIOR
            )
        }

        /**
         * Parse the constraint dimension attribute
         *
         * @param a
         * @param attr
         * @param orientation
         */
        fun parseDimensionConstraints(data: Any?, a: TResources, key: String, attr: String, orientation: Int) {
            if (data == null) {
                return
            }
            // data can be of:
            //
            // ConstraintLayout.LayoutParams
            // ConstraintSet.Layout
            // Constraint.Delta
            val type = a.getResourceType(attr)
            var finalValue = 0
            var finalConstrained = false
            when (type) {
                TypedValue.TYPE_DIMENSION -> {
                    finalValue = a.getDimensionPixelSize(attr, 0)
                }
                TypedValue.TYPE_STRING -> {
                    val value: String = a.getString(key, attr)
                    parseDimensionConstraintsString(data, value, orientation)
                    return
                }
                else -> {
                    val value: Int = a.getInt(attr, 0)
                    when (value) {
                        INTERNAL_WRAP_CONTENT, INTERNAL_MATCH_PARENT -> {
                            finalValue = value
                        }
                        INTERNAL_MATCH_CONSTRAINT -> {
                            finalValue = MATCH_CONSTRAINT
                        }
                        INTERNAL_WRAP_CONTENT_CONSTRAINED -> {
                            finalValue = WRAP_CONTENT
                            finalConstrained = true
                        }
                    }
                }
            }
            if (data is ConstraintLayout.LayoutParams) {
                val params: ConstraintLayout.LayoutParams = data
                if (orientation == HORIZONTAL) {
                    params.width = finalValue
                    params.constrainedWidth = finalConstrained
                } else {
                    params.height = finalValue
                    params.constrainedHeight = finalConstrained
                }
            } else if (data is Layout) {
                val params = data as Layout
                if (orientation == HORIZONTAL) {
                    params.mWidth = finalValue
                    params.constrainedWidth = finalConstrained
                } else {
                    params.mHeight = finalValue
                    params.constrainedHeight = finalConstrained
                }
            } else if (data is Constraint.Delta) {
                val params = data as Constraint.Delta
                if (orientation == HORIZONTAL) {
                    params.add(LAYOUT_WIDTH, finalValue)
                    params.add(CONSTRAINED_WIDTH, finalConstrained)
                } else {
                    params.add(LAYOUT_HEIGHT, finalValue)
                    params.add(CONSTRAINED_HEIGHT, finalConstrained)
                }
            }
        }

        /**
         * Parse the dimension ratio string
         *
         * @param value
         */
        fun parseDimensionRatioString(params: ConstraintLayout.LayoutParams, value: String?) {
            var dimensionRatioValue = Float.NaN
            var dimensionRatioSide = UNSET
            if (value != null) {
                val len: Int = value.length
                var commaIndex = value.indexOf(',')
                if (commaIndex > 0 && commaIndex < len - 1) {
                    val dimension = value.substring(0, commaIndex)
                    if (dimension.equals("W", ignoreCase = true)) {
                        dimensionRatioSide = HORIZONTAL
                    } else if (dimension.equals("H", ignoreCase = true)) {
                        dimensionRatioSide = VERTICAL
                    }
                    commaIndex++
                } else {
                    commaIndex = 0
                }
                val colonIndex = value.indexOf(':')
                if (colonIndex >= 0 && colonIndex < len - 1) {
                    val nominator = value.substring(commaIndex, colonIndex)
                    val denominator = value.substring(colonIndex + 1)
                    if (nominator.length > 0 && denominator.length > 0) {
                        try {
                            val nominatorValue: Float = nominator.toFloat()
                            val denominatorValue: Float = denominator.toFloat()
                            if (nominatorValue > 0 && denominatorValue > 0) {
                                dimensionRatioValue =
                                    if (dimensionRatioSide == VERTICAL) {
                                        abs(denominatorValue / nominatorValue)
                                    } else {
                                        abs(nominatorValue / denominatorValue)
                                    }
                            }
                        } catch (e: NumberFormatException) {
                            // Ignore
                        }
                    }
                } else {
                    val r = value.substring(commaIndex)
                    if (r.length > 0) {
                        try {
                            dimensionRatioValue = r.toFloat()
                        } catch (e: NumberFormatException) {
                            // Ignore
                        }
                    }
                }
            }
            params.dimensionRatio = value
            params.mDimensionRatioValue = dimensionRatioValue
            params.mDimensionRatioSide = dimensionRatioSide
        }

        /**
         * Parse the constraints string dimension
         *
         * @param value
         * @param orientation
         */
        fun parseDimensionConstraintsString(data: Any, value: String?, orientation: Int) {
            // data can be of:
            //
            // ConstraintLayout.LayoutParams
            // ConstraintSet.Layout
            // Constraint.Delta

            // String should be of the form
            //
            // "<Key>=<Value>"
            // supported Keys are:
            // "weight=<value>"
            // "ratio=<value>"
            // "parent=<value>"
            if (value == null) {
                return
            }
            val equalIndex = value.indexOf('=')
            val len: Int = value.length
            if (equalIndex > 0 && equalIndex < len - 1) {
                var key = value.substring(0, equalIndex)
                var `val` = value.substring(equalIndex + 1)
                if (`val`.length > 0) {
                    key = key.trim()
                    `val` = `val`.trim()
                    if (KEY_RATIO.equals(key, ignoreCase = true)) {
                        if (data is ConstraintLayout.LayoutParams) {
                            val params: ConstraintLayout.LayoutParams = data
                            if (orientation == HORIZONTAL) {
                                params.width = MATCH_CONSTRAINT
                            } else {
                                params.height = MATCH_CONSTRAINT
                            }
                            parseDimensionRatioString(params, `val`)
                        } else if (data is Layout) {
                            val params = data as Layout
                            params.dimensionRatio = `val`
                        } else if (data is Constraint.Delta) {
                            val params = data as Constraint.Delta
                            params.add(DIMENSION_RATIO, `val`)
                        }
                    } else if (KEY_WEIGHT.equals(key, ignoreCase = true)) {
                        try {
                            val weight: Float = (`val`).toFloat()
                            if (data is ConstraintLayout.LayoutParams) {
                                val params: ConstraintLayout.LayoutParams = data
                                if (orientation == HORIZONTAL) {
                                    params.width = MATCH_CONSTRAINT
                                    params.horizontalWeight = weight
                                } else {
                                    params.height = MATCH_CONSTRAINT
                                    params.verticalWeight = weight
                                }
                            } else if (data is Layout) {
                                val params = data as Layout
                                if (orientation == HORIZONTAL) {
                                    params.mWidth = MATCH_CONSTRAINT
                                    params.horizontalWeight = weight
                                } else {
                                    params.mHeight = MATCH_CONSTRAINT
                                    params.verticalWeight = weight
                                }
                            } else if (data is Constraint.Delta) {
                                val params = data as Constraint.Delta
                                if (orientation == HORIZONTAL) {
                                    params.add(LAYOUT_WIDTH, MATCH_CONSTRAINT)
                                    params.add(HORIZONTAL_WEIGHT, weight)
                                } else {
                                    params.add(LAYOUT_HEIGHT, MATCH_CONSTRAINT)
                                    params.add(VERTICAL_WEIGHT, weight)
                                }
                            }
                        } catch (e: NumberFormatException) {
                            // nothing
                        }
                    } else if (KEY_PERCENT_PARENT.equals(key, ignoreCase = true)) {
                        try {
                            var percent: Float = min(1f, `val`.toFloat())
                            percent = max(0f, percent)
                            if (data is ConstraintLayout.LayoutParams) {
                                val params: ConstraintLayout.LayoutParams = data
                                if (orientation == HORIZONTAL) {
                                    params.width = MATCH_CONSTRAINT
                                    params.matchConstraintPercentWidth = percent
                                    params.matchConstraintDefaultWidth = MATCH_CONSTRAINT_PERCENT
                                } else {
                                    params.height = MATCH_CONSTRAINT
                                    params.matchConstraintPercentHeight = percent
                                    params.matchConstraintDefaultHeight = MATCH_CONSTRAINT_PERCENT
                                }
                            } else if (data is Layout) {
                                val params = data as Layout
                                if (orientation == HORIZONTAL) {
                                    params.mWidth = MATCH_CONSTRAINT
                                    params.widthPercent = percent
                                    params.widthDefault = MATCH_CONSTRAINT_PERCENT
                                } else {
                                    params.mHeight = MATCH_CONSTRAINT
                                    params.heightPercent = percent
                                    params.heightDefault = MATCH_CONSTRAINT_PERCENT
                                }
                            } else if (data is Constraint.Delta) {
                                val params = data as Constraint.Delta
                                if (orientation == HORIZONTAL) {
                                    params.add(LAYOUT_WIDTH, MATCH_CONSTRAINT)
                                    params.add(WIDTH_DEFAULT, MATCH_CONSTRAINT_PERCENT)
                                } else {
                                    params.add(LAYOUT_HEIGHT, MATCH_CONSTRAINT)
                                    params.add(HEIGHT_DEFAULT, MATCH_CONSTRAINT_PERCENT)
                                }
                            }
                        } catch (e: NumberFormatException) {
                            // nothing
                        }
                    }
                }
            }
        }

        private fun splitString(str: String): Array<String> {
            val chars = str.toCharArray()
            val list: ArrayList<String> = ArrayList()
            var inDouble = false
            var start = 0
            for (i in chars.indices) {
                if (chars[i] == ',' && !inDouble) {
                    list.add(chars.concatToString(start, start + (i - start)))
                    start = i + 1
                } else if (chars[i] == '"') {
                    inDouble = !inDouble
                }
            }
            list.add(chars.concatToString(start, start + (chars.size - start)))
            return list.toTypedArray()
        }

        private fun lookupID(a: TResources, key: String, index: String, def: String): String {
            var ret = a.getResourceId(index, def)
            if (ret == Layout.UNSET_ID) {
                ret = a.getString(key, index)
            }
            return ret
        }

        /**
         * Used to read a ConstraintDelta
         *
         * @param context
         * @param parser
         * @return
         */
        fun buildDelta(context: TContext, parser: XmlBufferedReader): Constraint {
            val attrs: AttributeSet = Xml.asAttributeSet(parser)
            val c = Constraint()
            populateOverride(c, context.getResources(), attrs)
            return c
        }

        private fun populateOverride(c: Constraint, a: TResources, attrs: AttributeSet) {
            var type: Int
            val delta = Constraint.Delta()
            c.mDelta = delta
            c.motion.mApply = false
            c.layout.mApply = false
            c.propertySet.mApply = false
            c.transform.mApply = false
            attrs.forEach { kvp ->
                val attr = kvp.value
                val attrType: Int = sOverrideMapToConstant.get(kvp.key)!!
                if (I_DEBUG) {
                    Log.v(TAG, Debug.loc + " > " + attrType + " " + getDebugName(attrType))
                }
                when (attrType) {
                    EDITOR_ABSOLUTE_X -> delta.add(
                        EDITOR_ABSOLUTE_X,
                        a.getDimensionPixelOffset(attr, c.layout.editorAbsoluteX)
                    )
                    EDITOR_ABSOLUTE_Y -> delta.add(
                        EDITOR_ABSOLUTE_Y,
                        a.getDimensionPixelOffset(attr, c.layout.editorAbsoluteY)
                    )
                    GUIDE_BEGIN -> delta.add(
                        GUIDE_BEGIN,
                        a.getDimensionPixelOffset(attr, c.layout.guideBegin)
                    )
                    GUIDE_END -> delta.add(
                        GUIDE_END,
                        a.getDimensionPixelOffset(attr, c.layout.guideEnd)
                    )
                    GUIDE_PERCENT -> delta.add(
                        GUIDE_PERCENT,
                        a.getFloat(attr, c.layout.guidePercent)
                    )
                    GUIDELINE_USE_RTL -> delta.add(
                        GUIDELINE_USE_RTL,
                        a.getBoolean(attr, c.layout.guidelineUseRtl)
                    )
                    ORIENTATION -> delta.add(ORIENTATION, a.getInt(attr, c.layout.orientation))
                    CIRCLE_RADIUS -> delta.add(
                        CIRCLE_RADIUS,
                        a.getDimensionPixelSize(attr, c.layout.circleRadius)
                    )
                    CIRCLE_ANGLE -> delta.add(CIRCLE_ANGLE, a.getFloat(attr, c.layout.circleAngle))
                    GONE_LEFT_MARGIN -> delta.add(
                        GONE_LEFT_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.goneLeftMargin)
                    )
                    GONE_TOP_MARGIN -> delta.add(
                        GONE_TOP_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.goneTopMargin)
                    )
                    GONE_RIGHT_MARGIN -> delta.add(
                        GONE_RIGHT_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.goneRightMargin)
                    )
                    GONE_BOTTOM_MARGIN -> delta.add(
                        GONE_BOTTOM_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.goneBottomMargin)
                    )
                    GONE_START_MARGIN -> delta.add(
                        GONE_START_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.goneStartMargin)
                    )
                    GONE_END_MARGIN -> delta.add(
                        GONE_END_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.goneEndMargin)
                    )
                    GONE_BASELINE_MARGIN -> delta.add(
                        GONE_BASELINE_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.goneBaselineMargin)
                    )
                    HORIZONTAL_BIAS -> delta.add(
                        HORIZONTAL_BIAS,
                        a.getFloat(attr, c.layout.horizontalBias)
                    )
                    VERTICAL_BIAS -> delta.add(
                        VERTICAL_BIAS,
                        a.getFloat(attr, c.layout.verticalBias)
                    )
                    LEFT_MARGIN -> delta.add(
                        LEFT_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.leftMargin)
                    )
                    RIGHT_MARGIN -> delta.add(
                        RIGHT_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.rightMargin)
                    )
                    START_MARGIN ->  {
                        delta.add(
                            START_MARGIN,
                            a.getDimensionPixelSize(attr, c.layout.startMargin)
                        )
                    }
                    END_MARGIN ->  {
                        delta.add(END_MARGIN, a.getDimensionPixelSize(attr, c.layout.endMargin))
                    }
                    TOP_MARGIN -> delta.add(
                        TOP_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.topMargin)
                    )
                    BOTTOM_MARGIN -> delta.add(
                        BOTTOM_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.bottomMargin)
                    )
                    BASELINE_MARGIN -> delta.add(
                        BASELINE_MARGIN,
                        a.getDimensionPixelSize(attr, c.layout.baselineMargin)
                    )
                    LAYOUT_WIDTH -> delta.add(
                        LAYOUT_WIDTH,
                        a.getLayoutDimension(attr, c.layout.mWidth)
                    )
                    LAYOUT_HEIGHT -> delta.add(
                        LAYOUT_HEIGHT,
                        a.getLayoutDimension(attr, c.layout.mHeight)
                    )
                    LAYOUT_CONSTRAINT_WIDTH -> parseDimensionConstraints(
                        delta,
                        a,
                        kvp.key,
                        attr,
                        HORIZONTAL
                    )
                    LAYOUT_CONSTRAINT_HEIGHT -> parseDimensionConstraints(
                        delta,
                        a,
                        kvp.key,
                        attr,
                        VERTICAL
                    )
                    LAYOUT_WRAP_BEHAVIOR -> delta.add(
                        LAYOUT_WRAP_BEHAVIOR,
                        a.getInt(attr, c.layout.mWrapBehavior)
                    )
                    WIDTH_DEFAULT -> delta.add(WIDTH_DEFAULT, a.getInt(attr, c.layout.widthDefault))
                    HEIGHT_DEFAULT -> delta.add(
                        HEIGHT_DEFAULT,
                        a.getInt(attr, c.layout.heightDefault)
                    )
                    HEIGHT_MAX -> delta.add(
                        HEIGHT_MAX,
                        a.getDimensionPixelSize(attr, c.layout.heightMax)
                    )
                    WIDTH_MAX -> delta.add(
                        WIDTH_MAX,
                        a.getDimensionPixelSize(attr, c.layout.widthMax)
                    )
                    HEIGHT_MIN -> delta.add(
                        HEIGHT_MIN,
                        a.getDimensionPixelSize(attr, c.layout.heightMin)
                    )
                    WIDTH_MIN -> delta.add(
                        WIDTH_MIN,
                        a.getDimensionPixelSize(attr, c.layout.widthMin)
                    )
                    CONSTRAINED_WIDTH -> delta.add(
                        CONSTRAINED_WIDTH,
                        a.getBoolean(attr, c.layout.constrainedWidth)
                    )
                    CONSTRAINED_HEIGHT -> delta.add(
                        CONSTRAINED_HEIGHT,
                        a.getBoolean(attr, c.layout.constrainedHeight)
                    )
                    LAYOUT_VISIBILITY -> delta.add(
                        LAYOUT_VISIBILITY,
                        VISIBILITY_FLAGS[a.getInt(attr, c.propertySet.visibility)]
                    )
                    VISIBILITY_MODE -> delta.add(
                        VISIBILITY_MODE,
                        a.getInt(attr, c.propertySet.mVisibilityMode)
                    )
                    ALPHA -> delta.add(ALPHA, a.getFloat(attr, c.propertySet.alpha))
                    ELEVATION ->  {
                        delta.add(ELEVATION, true)
                        delta.add(ELEVATION, a.getDimension(attr, c.transform.elevation))
                    }
                    ROTATION -> delta.add(ROTATION, a.getFloat(attr, c.transform.rotation))
                    ROTATION_X -> delta.add(ROTATION_X, a.getFloat(attr, c.transform.rotationX))
                    ROTATION_Y -> delta.add(ROTATION_Y, a.getFloat(attr, c.transform.rotationY))
                    SCALE_X -> delta.add(SCALE_X, a.getFloat(attr, c.transform.scaleX))
                    SCALE_Y -> delta.add(SCALE_Y, a.getFloat(attr, c.transform.scaleY))
                    TRANSFORM_PIVOT_X -> delta.add(
                        TRANSFORM_PIVOT_X,
                        a.getDimension(attr, c.transform.transformPivotX)
                    )
                    TRANSFORM_PIVOT_Y -> delta.add(
                        TRANSFORM_PIVOT_Y,
                        a.getDimension(attr, c.transform.transformPivotY)
                    )
                    TRANSLATION_X -> delta.add(
                        TRANSLATION_X,
                        a.getDimension(attr, c.transform.translationX)
                    )
                    TRANSLATION_Y -> delta.add(
                        TRANSLATION_Y,
                        a.getDimension(attr, c.transform.translationY)
                    )
                    TRANSLATION_Z ->  {
                        delta.add(TRANSLATION_Z, a.getDimension(attr, c.transform.translationZ))
                    }
                    TRANSFORM_PIVOT_TARGET -> delta.add(
                        TRANSFORM_PIVOT_TARGET,
                        lookupID(a, kvp.key, attr, c.transform.transformPivotTarget)
                    )
                    VERTICAL_WEIGHT -> delta.add(
                        VERTICAL_WEIGHT,
                        a.getFloat(attr, c.layout.verticalWeight)
                    )
                    HORIZONTAL_WEIGHT -> delta.add(
                        HORIZONTAL_WEIGHT,
                        a.getFloat(attr, c.layout.horizontalWeight)
                    )
                    VERTICAL_STYLE -> delta.add(
                        VERTICAL_STYLE,
                        a.getInt(attr, c.layout.verticalChainStyle)
                    )
                    HORIZONTAL_STYLE -> delta.add(
                        HORIZONTAL_STYLE,
                        a.getInt(attr, c.layout.horizontalChainStyle)
                    )
                    VIEW_ID -> {
                        c.mViewId = a.getResourceId(attr, c.mViewId)
                        delta.add(VIEW_ID, c.mViewId)
                    }
                    MOTION_TARGET -> if (MotionLayout.IS_IN_EDIT_MODE) {
                        c.mViewId = a.getResourceId(attr, c.mViewId)
                        if (c.mViewId == "") {
                            c.mTargetString = a.getString(kvp.key, attr)
                        }
                    } else {
                        if (a.getResourceType(attr) == TypedValue.TYPE_STRING) {
                            c.mTargetString = a.getString(kvp.key, attr)
                        } else {
                            c.mViewId = a.getResourceId(attr, c.mViewId)
                        }
                    }
                    DIMENSION_RATIO -> delta.add(DIMENSION_RATIO, a.getString(kvp.key, attr))
                    WIDTH_PERCENT -> delta.add(WIDTH_PERCENT, a.getFloat(attr, 1f))
                    HEIGHT_PERCENT -> delta.add(HEIGHT_PERCENT, a.getFloat(attr, 1f))
                    PROGRESS -> delta.add(PROGRESS, a.getFloat(attr, c.propertySet.mProgress))
                    ANIMATE_RELATIVE_TO -> delta.add(
                        ANIMATE_RELATIVE_TO,
                        lookupID(a, kvp.key, attr, c.motion.mAnimateRelativeTo)
                    )
                    ANIMATE_CIRCLE_ANGLE_TO -> delta.add(
                        ANIMATE_CIRCLE_ANGLE_TO,
                        a.getInt(attr, c.motion.mAnimateCircleAngleTo)
                    )
                    TRANSITION_EASING -> {
                        type = a.getResourceType(attr)
                        if (type == TypedValue.TYPE_STRING) {
                            delta.add(TRANSITION_EASING, a.getString(kvp.key, attr))
                        } else {
                            delta.add(
                                TRANSITION_EASING,
                                Easing.NAMED_EASING.get(a.getInt(attr, 0))
                            )
                        }
                    }
                    PATH_MOTION_ARC -> delta.add(
                        PATH_MOTION_ARC,
                        a.getInt(attr, c.motion.mPathMotionArc)
                    )
                    TRANSITION_PATH_ROTATE -> delta.add(
                        TRANSITION_PATH_ROTATE,
                        a.getFloat(attr, c.motion.mPathRotate)
                    )
                    MOTION_STAGGER -> delta.add(
                        MOTION_STAGGER,
                        a.getFloat(attr, c.motion.mMotionStagger)
                    )
                    QUANTIZE_MOTION_STEPS -> delta.add(
                        QUANTIZE_MOTION_STEPS, a.getInt(
                            attr,
                            c.motion.mQuantizeMotionSteps
                        )
                    )
                    QUANTIZE_MOTION_PHASE -> delta.add(
                        QUANTIZE_MOTION_PHASE, a.getFloat(
                            attr,
                            c.motion.mQuantizeMotionPhase
                        )
                    )
                    QUANTIZE_MOTION_INTERPOLATOR -> {
                        type = a.getResourceType(attr)
                        if (type == TypedValue.TYPE_REFERENCE) {
                            c.motion.mQuantizeInterpolatorID = a.getResourceId(attr, "")
                            delta.add(
                                QUANTIZE_MOTION_INTERPOLATOR_ID,
                                c.motion.mQuantizeInterpolatorID
                            )
                            if (c.motion.mQuantizeInterpolatorID != "") {
                                c.motion.mQuantizeInterpolatorType =
                                    Motion.INTERPOLATOR_REFERENCE_ID
                                delta.add(
                                    QUANTIZE_MOTION_INTERPOLATOR_TYPE,
                                    c.motion.mQuantizeInterpolatorType
                                )
                            }
                        } else if (type == TypedValue.TYPE_STRING) {
                            c.motion.mQuantizeInterpolatorString = a.getString(kvp.key, attr)
                            delta.add(
                                QUANTIZE_MOTION_INTERPOLATOR_STR,
                                c.motion.mQuantizeInterpolatorString
                            )
                            if (c.motion.mQuantizeInterpolatorString!!.indexOf("/") > 0) {
                                c.motion.mQuantizeInterpolatorID = a.getResourceId(attr, "")
                                delta.add(
                                    QUANTIZE_MOTION_INTERPOLATOR_ID,
                                    c.motion.mQuantizeInterpolatorID
                                )
                                c.motion.mQuantizeInterpolatorType =
                                    Motion.INTERPOLATOR_REFERENCE_ID
                                delta.add(
                                    QUANTIZE_MOTION_INTERPOLATOR_TYPE,
                                    c.motion.mQuantizeInterpolatorType
                                )
                            } else {
                                c.motion.mQuantizeInterpolatorType = Motion.SPLINE_STRING
                                delta.add(
                                    QUANTIZE_MOTION_INTERPOLATOR_TYPE,
                                    c.motion.mQuantizeInterpolatorType
                                )
                            }
                        } else {
                            c.motion.mQuantizeInterpolatorType =
                                a.getInt(attr, c.motion.mQuantizeInterpolatorType)
                            delta.add(
                                QUANTIZE_MOTION_INTERPOLATOR_TYPE,
                                c.motion.mQuantizeInterpolatorType
                            )
                        }
                    }
                    DRAW_PATH -> delta.add(DRAW_PATH, a.getInt(attr, 0))
                    CHAIN_USE_RTL -> Log.e(
                        TAG,
                        "CURRENTLY UNSUPPORTED"
                    ) // TODO add support or remove
                    BARRIER_DIRECTION -> delta.add(
                        BARRIER_DIRECTION,
                        a.getInt(attr, c.layout.mBarrierDirection)
                    )
                    BARRIER_MARGIN -> delta.add(
                        BARRIER_MARGIN, a.getDimensionPixelSize(
                            attr,
                            c.layout.mBarrierMargin
                        )
                    )
                    CONSTRAINT_REFERENCED_IDS -> delta.add(
                        CONSTRAINT_REFERENCED_IDS,
                        a.getString(kvp.key, attr)
                    )
                    CONSTRAINT_TAG -> delta.add(CONSTRAINT_TAG, a.getString(kvp.key, attr))
                    BARRIER_ALLOWS_GONE_WIDGETS -> delta.add(
                        BARRIER_ALLOWS_GONE_WIDGETS, a.getBoolean(
                            attr,
                            c.layout.mBarrierAllowsGoneWidgets
                        )
                    )
                    UNUSED -> Log.w(
                        TAG,
                        "unused attribute 0x" + attr
                            .toString() + "   " + sMapToConstant.get(attr)
                    )
                    else -> Log.w(
                        TAG,
                        "Unknown attribute 0x" + attr
                            .toString() + "   " + sMapToConstant.get(attr)
                    )
                }
            }
        }

        private fun setDeltaValue(c: Constraint?, type: Int, value: Float) {
            when (type) {
                GUIDE_PERCENT -> c!!.layout.guidePercent = value
                CIRCLE_ANGLE -> c!!.layout.circleAngle = value
                HORIZONTAL_BIAS -> c!!.layout.horizontalBias = value
                VERTICAL_BIAS -> c!!.layout.verticalBias = value
                ALPHA -> c!!.propertySet.alpha = value
                ELEVATION -> {
                    c!!.transform.elevation = value
                    c.transform.applyElevation = true
                }
                ROTATION -> c!!.transform.rotation = value
                ROTATION_X -> c!!.transform.rotationX = value
                ROTATION_Y -> c!!.transform.rotationY = value
                SCALE_X -> c!!.transform.scaleX = value
                SCALE_Y -> c!!.transform.scaleY = value
                TRANSFORM_PIVOT_X -> c!!.transform.transformPivotX = value
                TRANSFORM_PIVOT_Y -> c!!.transform.transformPivotY = value
                TRANSLATION_X -> c!!.transform.translationX = value
                TRANSLATION_Y -> c!!.transform.translationY = value
                TRANSLATION_Z -> c!!.transform.translationZ = value
                VERTICAL_WEIGHT -> c!!.layout.verticalWeight = value
                HORIZONTAL_WEIGHT -> c!!.layout.horizontalWeight = value
                WIDTH_PERCENT -> c!!.layout.widthPercent = value
                HEIGHT_PERCENT -> c!!.layout.heightPercent = value
                PROGRESS -> c!!.propertySet.mProgress = value
                TRANSITION_PATH_ROTATE -> c!!.motion.mPathRotate = value
                MOTION_STAGGER -> c!!.motion.mMotionStagger = value
                QUANTIZE_MOTION_PHASE -> c!!.motion.mQuantizeMotionPhase = value
                UNUSED -> {}
                else -> Log.w(
                    TAG,
                    "Unknown attribute 0x"
                )
            }
        }

        private fun setDeltaValue(c: Constraint?, type: Int, value: Int) {
            when (type) {
                EDITOR_ABSOLUTE_X -> c!!.layout.editorAbsoluteX = value
                EDITOR_ABSOLUTE_Y -> c!!.layout.editorAbsoluteY = value
                LAYOUT_WRAP_BEHAVIOR -> c!!.layout.mWrapBehavior = value
                GUIDE_BEGIN -> c!!.layout.guideBegin = value
                GUIDE_END -> c!!.layout.guideEnd = value
                ORIENTATION -> c!!.layout.orientation = value
                //CIRCLE -> c!!.layout.circleConstraint = value
                CIRCLE_RADIUS -> c!!.layout.circleRadius = value
                GONE_LEFT_MARGIN -> c!!.layout.goneLeftMargin = value
                GONE_TOP_MARGIN -> c!!.layout.goneTopMargin = value
                GONE_RIGHT_MARGIN -> c!!.layout.goneRightMargin = value
                GONE_BOTTOM_MARGIN -> c!!.layout.goneBottomMargin = value
                GONE_START_MARGIN -> c!!.layout.goneStartMargin = value
                GONE_END_MARGIN -> c!!.layout.goneEndMargin = value
                GONE_BASELINE_MARGIN -> c!!.layout.goneBaselineMargin = value
                LEFT_MARGIN -> c!!.layout.leftMargin = value
                RIGHT_MARGIN -> c!!.layout.rightMargin = value
                START_MARGIN -> c!!.layout.startMargin = value
                END_MARGIN -> c!!.layout.endMargin = value
                TOP_MARGIN -> c!!.layout.topMargin = value
                BOTTOM_MARGIN -> c!!.layout.bottomMargin = value
                BASELINE_MARGIN -> c!!.layout.baselineMargin = value
                LAYOUT_WIDTH -> c!!.layout.mWidth = value
                LAYOUT_HEIGHT -> c!!.layout.mHeight = value
                WIDTH_DEFAULT -> c!!.layout.widthDefault = value
                HEIGHT_DEFAULT -> c!!.layout.heightDefault = value
                HEIGHT_MAX -> c!!.layout.heightMax = value
                WIDTH_MAX -> c!!.layout.widthMax = value
                HEIGHT_MIN -> c!!.layout.heightMin = value
                WIDTH_MIN -> c!!.layout.widthMin = value
                LAYOUT_VISIBILITY -> c!!.propertySet.visibility = value
                VISIBILITY_MODE -> c!!.propertySet.mVisibilityMode = value
                //TRANSFORM_PIVOT_TARGET -> c!!.transform.transformPivotTarget = value
                VERTICAL_STYLE -> c!!.layout.verticalChainStyle = value
                HORIZONTAL_STYLE -> c!!.layout.horizontalChainStyle = value
                //VIEW_ID -> c!!.mViewId = value
                //ANIMATE_RELATIVE_TO -> c!!.motion.mAnimateRelativeTo = value
                ANIMATE_CIRCLE_ANGLE_TO -> c!!.motion.mAnimateCircleAngleTo = value
                PATH_MOTION_ARC -> c!!.motion.mPathMotionArc = value
                QUANTIZE_MOTION_STEPS -> c!!.motion.mQuantizeMotionSteps = value
                QUANTIZE_MOTION_INTERPOLATOR_TYPE -> c!!.motion.mQuantizeInterpolatorType = value
                //QUANTIZE_MOTION_INTERPOLATOR_ID -> c!!.motion.mQuantizeInterpolatorID = value
                DRAW_PATH -> c!!.motion.mDrawPath = value
                BARRIER_DIRECTION -> c!!.layout.mBarrierDirection = value
                BARRIER_MARGIN -> c!!.layout.mBarrierMargin = value
                UNUSED -> {}
                else -> Log.w(
                    TAG,
                    "Unknown attribute 0x"
                )
            }
        }

        private fun setDeltaValue(c: Constraint?, type: Int, value: String?) {
            when (type) {
                DIMENSION_RATIO -> c!!.layout.dimensionRatio = value
                TRANSITION_EASING -> c!!.motion.mTransitionEasing = value
                QUANTIZE_MOTION_INTERPOLATOR_STR -> c!!.motion.mQuantizeInterpolatorString = value
                CONSTRAINT_REFERENCED_IDS -> {
                    c!!.layout.mReferenceIdString = value
                    // If a string is defined, clear up the reference ids array
                    c.layout.mReferenceIds = null
                }
                CONSTRAINT_TAG -> c!!.layout.mConstraintTag = value
                UNUSED -> {}
                else -> Log.w(
                    TAG,
                    "Unknown attribute 0x"
                )
            }
        }

        private fun setDeltaValue(c: Constraint?, type: Int, value: Boolean) {
            when (type) {
                CONSTRAINED_WIDTH -> c!!.layout.constrainedWidth = value
                CONSTRAINED_HEIGHT -> c!!.layout.constrainedHeight = value
                ELEVATION -> c!!.transform.applyElevation = value
                BARRIER_ALLOWS_GONE_WIDGETS -> c!!.layout.mBarrierAllowsGoneWidgets = value
                UNUSED -> {}
                else -> Log.w(TAG, "Unknown attribute 0x")
            }
        }

        fun getDebugName(v: Int): String {
            /*for (field in ConstraintSet::class.java.getDeclaredFields()) {
                if (field.getName().contains("_")
                    && field.getType() === Int::class.javaPrimitiveType && java.lang.reflect.Modifier.isStatic(
                        field.getModifiers()
                    )
                    && java.lang.reflect.Modifier.isFinal(field.getModifiers())
                ) {
                    var `val` = 0
                    try {
                        `val` = field.getInt(null)
                        if (`val` == v) {
                            return field.getName()
                        }
                    } catch (e: IllegalAccessException) {
                        Log.e(TAG, "Error accessing ConstraintSet field", e)
                    }
                }
            }*/
            return "UNKNOWN"
        }
    }
}