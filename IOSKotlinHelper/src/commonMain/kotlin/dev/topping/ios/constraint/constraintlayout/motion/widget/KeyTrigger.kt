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
import dev.topping.ios.constraint.constraintlayout.motion.utils.ViewSpline
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.core.motion.utils.RectF
import kotlin.math.*

/**
 * Defines container for a key frame of for storing KeyAttributes.
 * KeyAttributes change post layout values of a view.
 *
 *
 */
class KeyTrigger : Key() {
    var mTriggerSlack = .1f
    var mViewTransitionOnNegativeCross = UNSET_ID
    var mViewTransitionOnPositiveCross = UNSET_ID
    var mViewTransitionOnCross = UNSET_ID
    var mCollisionRect: RectF = RectF()
    var mTargetRect: RectF = RectF()

    /**
     * Gets the curve fit type this drives the interpolation
     *
     * @return
     */
    var curveFit = -1
        private set
    private var mCross: String? = null
    private var mTriggerReceiver = UNSET_ID
    private var mNegativeCross: String? = null
    private var mPositiveCross: String? = null
    private var mTriggerID = UNSET_ID
    private var mTriggerCollisionId = UNSET_ID
    private var mTriggerCollisionView: TView? = null
    private var mFireCrossReset = true
    private var mFireNegativeReset = true
    private var mFirePositiveReset = true
    private var mFireThreshold = Float.NaN
    private var mFireLastPos = 0f
    private var mPostLayout = false

    init {
        mType = KEY_TYPE
        mCustomConstraints = HashMap()
    }

    override fun load(context: TContext, attrs: AttributeSet) {
        Loader.read(context, this, attrs)
    }

    override fun getAttributeNames(attributes: MutableSet<String>) {

    }

    override fun addValues(splines: MutableMap<String, ViewSpline>) {

    }

    override fun setValue(tag: String, value: Any) {
        when (tag) {
            CROSS -> mCross = value.toString()
            TRIGGER_RECEIVER -> mTriggerReceiver = value.toString()
            NEGATIVE_CROSS -> mNegativeCross = value.toString()
            POSITIVE_CROSS -> mPositiveCross = value.toString()
            TRIGGER_ID -> mTriggerID = value.toString()
            TRIGGER_COLLISION_ID -> mTriggerCollisionId = value.toString()
            TRIGGER_COLLISION_VIEW -> mTriggerCollisionView = value as TView
            TRIGGER_SLACK -> mTriggerSlack = toFloat(value)
            POST_LAYOUT -> mPostLayout = toBoolean(value)
            VIEW_TRANSITION_ON_NEGATIVE_CROSS -> mViewTransitionOnNegativeCross = value.toString()
            VIEW_TRANSITION_ON_POSITIVE_CROSS -> mViewTransitionOnPositiveCross = value.toString()
            VIEW_TRANSITION_ON_CROSS -> mViewTransitionOnCross = value.toString()
        }
    }

    private fun setUpRect(rect: RectF, child: TView, postLayout: Boolean) {
        rect.top = child.getTop().toFloat()
        rect.bottom = child.getBottom().toFloat()
        rect.left = child.getLeft().toFloat()
        rect.right = child.getRight().toFloat()
        /*if (postLayout) {
            child.getMatrix().mapRect(rect)
        }*/
    }

    /**
     * This fires the keyTriggers associated with this view at that position
     *
     * @param pos   the progress
     * @param child the view
     */
    fun conditionallyFire(pos: Float, child: TView) {
        var fireCross = false
        var fireNegative = false
        var firePositive = false
        if (mTriggerCollisionId != UNSET_ID) {
            if (mTriggerCollisionView == null) {
                mTriggerCollisionView =
                    (child.getParent() as TView).findViewById(mTriggerCollisionId)
            }
            setUpRect(mCollisionRect, mTriggerCollisionView!!, mPostLayout)
            setUpRect(mTargetRect, child, mPostLayout)
            val `in`: Boolean = mCollisionRect.intersect(mTargetRect)
            // TODO scale by mTriggerSlack
            if (`in`) {
                if (mFireCrossReset) {
                    fireCross = true
                    mFireCrossReset = false
                }
                if (mFirePositiveReset) {
                    firePositive = true
                    mFirePositiveReset = false
                }
                mFireNegativeReset = true
            } else {
                if (!mFireCrossReset) {
                    fireCross = true
                    mFireCrossReset = true
                }
                if (mFireNegativeReset) {
                    fireNegative = true
                    mFireNegativeReset = false
                }
                mFirePositiveReset = true
            }
        } else {

            // Check for crossing
            if (mFireCrossReset) {
                val offset = pos - mFireThreshold
                val lastOffset = mFireLastPos - mFireThreshold
                if (offset * lastOffset < 0) { // just crossed the threshold
                    fireCross = true
                    mFireCrossReset = false
                }
            } else {
                if (abs(pos - mFireThreshold) > mTriggerSlack) {
                    mFireCrossReset = true
                }
            }

            // Check for negative crossing
            if (mFireNegativeReset) {
                val offset = pos - mFireThreshold
                val lastOffset = mFireLastPos - mFireThreshold
                if (offset * lastOffset < 0 && offset < 0) { // just crossed the threshold
                    fireNegative = true
                    mFireNegativeReset = false
                }
            } else {
                if (abs(pos - mFireThreshold) > mTriggerSlack) {
                    mFireNegativeReset = true
                }
            }
            // Check for positive crossing
            if (mFirePositiveReset) {
                val offset = pos - mFireThreshold
                val lastOffset = mFireLastPos - mFireThreshold
                if (offset * lastOffset < 0 && offset > 0) { // just crossed the threshold
                    firePositive = true
                    mFirePositiveReset = false
                }
            } else {
                if (abs(pos - mFireThreshold) > mTriggerSlack) {
                    mFirePositiveReset = true
                }
            }
        }
        mFireLastPos = pos
        if (fireNegative || fireCross || firePositive) {
            (child.getParent() as MotionLayout).fireTrigger(mTriggerID, firePositive, pos)
        }
        val call: TView =
            if (mTriggerReceiver == UNSET_ID) child else (child.getParent()?.getParentType() as MotionLayout).self.findViewById(
                mTriggerReceiver
            ) as TView
        if (fireNegative) {
            if (mNegativeCross != null) {
                fire(mNegativeCross, call)
            }
            if (mViewTransitionOnNegativeCross != UNSET_ID) {
                (child.getParent() as MotionLayout).viewTransition(
                    mViewTransitionOnNegativeCross,
                    call
                )
            }
        }
        if (firePositive) {
            if (mPositiveCross != null) {
                fire(mPositiveCross, call)
            }
            if (mViewTransitionOnPositiveCross != UNSET_ID) {
                (child.getParent() as MotionLayout).viewTransition(
                    mViewTransitionOnPositiveCross,
                    call
                )
            }
        }
        if (fireCross) {
            if (mCross != null) {
                fire(mCross, call)
            }
            if (mViewTransitionOnCross != UNSET_ID) {
                (child.getParent() as MotionLayout).viewTransition(mViewTransitionOnCross, call)
            }
        }
    }

    private fun fire(str: String?, call: TView) {
        if (str == null) {
            return
        }
        if (str.startsWith(".")) {
            fireCustom(str, call)
            return
        }
        call.invokeMethod(str, "")
    }

    private fun fireCustom(str: String, view: TView) {
        var str = str
        val callAll = str.length == 1
        if (!callAll) {
            str = str.substring(1).lowercase()
        }
        mCustomConstraints?.let {
            for (name in it.keys) {
                val lowerCase = name.lowercase()
                if (callAll || lowerCase.matches(str.toRegex())) {
                    val custom: ConstraintAttribute? = it[name]
                    if (custom != null) {
                        custom.applyCustom(view)
                    }
                }
            }
        }

    }

    /**
     * Copy the key
     *
     * @param src to be copied
     * @return self
     */
    override fun copy(src: Key): Key {
        super.copy(src)
        val k = src as KeyTrigger
        curveFit = k.curveFit
        mCross = k.mCross
        mTriggerReceiver = k.mTriggerReceiver
        mNegativeCross = k.mNegativeCross
        mPositiveCross = k.mPositiveCross
        mTriggerID = k.mTriggerID
        mTriggerCollisionId = k.mTriggerCollisionId
        mTriggerCollisionView = k.mTriggerCollisionView
        mTriggerSlack = k.mTriggerSlack
        mFireCrossReset = k.mFireCrossReset
        mFireNegativeReset = k.mFireNegativeReset
        mFirePositiveReset = k.mFirePositiveReset
        mFireThreshold = k.mFireThreshold
        mFireLastPos = k.mFireLastPos
        mPostLayout = k.mPostLayout
        mCollisionRect = k.mCollisionRect
        mTargetRect = k.mTargetRect
        return this
    }

    /**
     * Clone this KeyAttributes
     *
     * @return
     */
    override fun clone(): Key {
        return KeyTrigger().copy(this)
    }

    private object Loader {
        private const val NEGATIVE_CROSS = 1
        private const val POSITIVE_CROSS = 2
        private const val CROSS = 4
        private const val TRIGGER_SLACK = 5
        private const val TRIGGER_ID = 6
        private const val TARGET_ID = 7
        private const val FRAME_POS = 8
        private const val COLLISION = 9
        private const val POST_LAYOUT = 10
        private const val TRIGGER_RECEIVER = 11
        private const val VT_CROSS = 12
        private const val VT_NEGATIVE_CROSS = 13
        private const val VT_POSITIVE_CROSS = 14
        private val sAttrMap: MutableMap<String, Int> = mutableMapOf()

        init {
            sAttrMap["framePosition"] = FRAME_POS
            sAttrMap["onCross"] = CROSS
            sAttrMap["onNegativeCross"] = NEGATIVE_CROSS
            sAttrMap["onPositiveCross"] = POSITIVE_CROSS
            sAttrMap["motionTarget"] = TARGET_ID
            sAttrMap["triggerId"] = TRIGGER_ID
            sAttrMap["triggerSlack"] = TRIGGER_SLACK
            sAttrMap["motion_triggerOnCollision"] = COLLISION
            sAttrMap["motion_postLayoutCollision"] = POST_LAYOUT
            sAttrMap["triggerReceiver"] = TRIGGER_RECEIVER
            sAttrMap["viewTransitionOnCross"] = VT_CROSS
            sAttrMap["viewTransitionOnNegativeCross"] = VT_NEGATIVE_CROSS
            sAttrMap["viewTransitionOnPositiveCross"] = VT_POSITIVE_CROSS
        }

        fun read(context: TContext, c: KeyTrigger, a: AttributeSet) {
            a.forEach { kvp ->
                val intValue = sAttrMap[kvp.key]
                when(intValue) {
                    FRAME_POS -> {
                        c.mFramePosition = context.getResources().getInt(
                            kvp.key,
                            kvp.value,
                            c.mFramePosition
                        )
                        c.mFireThreshold = (c.mFramePosition + .5f) / 100f
                    }
                    TARGET_ID -> {
                        if (MotionLayout.IS_IN_EDIT_MODE) {
                            c.mTargetId = context.getResources().getResourceId(kvp.value, UNSET_ID)
                            if (c.mTargetId == "") {
                                c.mTargetString = kvp.value
                            }
                        } else {
                            if(context.getResources().getResourceType(kvp.value) == TypedValue.TYPE_STRING)
                            {
                                c.mTargetString = kvp.value
                            } else {
                                c.mTargetId = context.getResources().getResourceId(kvp.value, UNSET_ID)
                            }
                        }
                    }
                    NEGATIVE_CROSS -> c.mNegativeCross = context.getResources().getString(
                        kvp.key,
                        kvp.value
                    )
                    POSITIVE_CROSS -> c.mPositiveCross = context.getResources().getString(
                        kvp.key,
                        kvp.value
                    )
                    CROSS -> c.mCross = context.getResources().getString(kvp.key, kvp.value)
                    TRIGGER_SLACK -> c.mTriggerSlack = context.getResources().getFloat(
                        kvp.key,
                        kvp.value,
                        c.mTriggerSlack
                    )
                    TRIGGER_ID -> c.mTriggerID = context.getResources().getResourceId(kvp.value, c.mTriggerID)
                    COLLISION -> c.mTriggerCollisionId = context.getResources().getResourceId(kvp.value, c.mTriggerCollisionId)
                    POST_LAYOUT -> c.mPostLayout = context.getResources().getBoolean(kvp.value, c.mPostLayout)
                    TRIGGER_RECEIVER -> c.mTriggerReceiver = context.getResources().getResourceId(kvp.value, c.mTriggerReceiver)
                    VT_NEGATIVE_CROSS -> c.mViewTransitionOnNegativeCross = context.getResources().getResourceId(kvp.value, c.mViewTransitionOnNegativeCross)
                    VT_POSITIVE_CROSS -> c.mViewTransitionOnPositiveCross = context.getResources().getResourceId(kvp.value, c.mViewTransitionOnPositiveCross)
                    VT_CROSS -> c.mViewTransitionOnCross = context.getResources().getResourceId(kvp.value, c.mViewTransitionOnCross)
                    else -> Log.e(
                        NAME, "unused attribute " + sAttrMap[kvp.value]
                    )
                }
            }
        }
    }

    companion object {
        const val VIEW_TRANSITION_ON_CROSS = "viewTransitionOnCross"
        const val VIEW_TRANSITION_ON_POSITIVE_CROSS = "viewTransitionOnPositiveCross"
        const val VIEW_TRANSITION_ON_NEGATIVE_CROSS = "viewTransitionOnNegativeCross"
        const val POST_LAYOUT = "postLayout"
        const val TRIGGER_SLACK = "triggerSlack"
        const val TRIGGER_COLLISION_VIEW = "triggerCollisionView"
        const val TRIGGER_COLLISION_ID = "triggerCollisionId"
        const val TRIGGER_ID = "triggerID"
        const val POSITIVE_CROSS = "positiveCross"
        const val NEGATIVE_CROSS = "negativeCross"
        const val TRIGGER_RECEIVER = "triggerReceiver"
        const val CROSS = "CROSS"
        const val KEY_TYPE = 5
        const val NAME = "KeyTrigger"
        private const val TAG = "KeyTrigger"
    }
}