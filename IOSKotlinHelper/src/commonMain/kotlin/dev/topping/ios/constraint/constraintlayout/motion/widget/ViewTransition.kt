/*
 * Copyright (C) 2020 The Android Open Source Project
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
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.core.motion.utils.Easing
import dev.topping.ios.constraint.core.motion.utils.KeyCache
import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.core.state.Interpolator
import dev.topping.ios.constraint.core.state.interpolators.*
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.localPart

/**
 * Provides a support for <ViewTransition> tag
 * it Parses tag
 * it implement the transition
 * it will update ConstraintSet or sets
 * For asynchronous it will create and drive a MotionController.
</ViewTransition> */
class ViewTransition internal constructor(context: TContext, parser: XmlBufferedReader) {
    var mSet: ConstraintSet? = null
    var id = UNSET_ID
    /**
     * Gets the type of transition to listen to.
     *
     * @return ONSTATE_TRANSITION_*
     */
    /**
     * Sets the type of transition to listen to.
     *
     * @param stateTransition
     */
    var stateTransition = UNSET
    private var mDisabled = false
    private var mPathMotionArc = 0
    var mViewTransitionMode = 0
    var mKeyFrames: KeyFrames? = null
    var mConstraintDelta: ConstraintSet.Constraint? = null
    private var mDuration = UNSET
    private var mUpDuration = UNSET
    private var mTargetId = UNSET_ID
    private var mTargetString: String? = null
    private var mDefaultInterpolator = 0
    private var mDefaultInterpolatorString: String? = null
    private var mDefaultInterpolatorID = UNSET_ID
    var mContext: TContext
    private var mSetsTag = UNSET_ID
    private var mClearsTag = UNSET_ID
    private var mIfTagSet = UNSET_ID
    private var mIfTagNotSet = UNSET_ID
    /**
     * Gets the SharedValue it will be listening for.
     *
     * @return
     */
    /**
     * sets the SharedValue it will be listening for.
     */
    // shared value management. mSharedValueId is the key we are watching,
    // mSharedValueCurrent the current value for that key, and mSharedValueTarget
    // is the target we are waiting for to trigger.
    var sharedValue = UNSET
    /**
     * Gets the ID of the SharedValue it will be listening for.
     *
     * @return the id of the shared value
     */
    /**
     * sets the ID of the SharedValue it will be listening for.
     */
    var sharedValueID = UNSET_ID
    var sharedValueCurrent = UNSET

    /**
     * debug string for a ViewTransition
     * @return
     */
    override fun toString(): String {
        return "ViewTransition(" + Debug.getName(mContext, id).toString() + ")"
    }

    fun getInterpolator(context: TContext): Interpolator? {
        when (mDefaultInterpolator) {
            SPLINE_STRING -> {
                val easing: Easing? = Easing.getInterpolator(mDefaultInterpolatorString)
                return object : Interpolator {
                    override fun getInterpolation(v: Float): Float {
                        return easing?.get(v.toDouble())?.toFloat() ?: 0f
                    }
                }
            }
            INTERPOLATOR_REFERENCE_ID -> return AnimationUtils.loadInterpolator(
                context,
                mDefaultInterpolatorID
            )
            EASE_IN_OUT -> return AccelerateDecelerateInterpolator()
            EASE_IN -> return AccelerateInterpolator()
            EASE_OUT -> return DecelerateInterpolator()
            LINEAR -> return null
            ANTICIPATE -> return AnticipateInterpolator()
            OVERSHOOT -> return OvershootInterpolator()
            BOUNCE -> return BounceInterpolator()
        }
        return null
    }

    init {
        mContext = context
        try {
            var eventType = parser.eventType
            while (eventType != EventType.END_DOCUMENT) {
                when (eventType) {
                    EventType.START_DOCUMENT, EventType.TEXT -> {}
                    EventType.START_ELEMENT -> {
                        val tagName: String = parser.name.localPart
                        when (tagName) {
                            VIEW_TRANSITION_TAG -> parseViewTransitionTags(context, parser)
                            KEY_FRAME_SET_TAG -> mKeyFrames = KeyFrames(context, parser)
                            CONSTRAINT_OVERRIDE -> mConstraintDelta =
                                ConstraintSet.buildDelta(context, parser)
                            CUSTOM_ATTRIBUTE, CUSTOM_METHOD -> ConstraintAttribute.parse(
                                context, parser,
                                mConstraintDelta?.mCustomConstraints
                            )
                            else -> {
                                Log.e(TAG, Debug.loc + " unknown tag " + tagName)
                                Log.e(TAG, ".xml:" + parser.locationInfo)
                            }
                        }
                    }
                    EventType.END_ELEMENT -> if (VIEW_TRANSITION_TAG == parser.name.localPart) {
                        throw WhenException()
                    }
                    else -> {}
                }
                eventType = parser.next()
            }
        }
        catch (e: WhenException) {

        }
        catch (e: Exception) {
            Log.e(TAG, "Error parsing XML resource", e)
        }
    }

    private fun parseViewTransitionTags(context: TContext, parser: XmlBufferedReader) {
        val attrs: AttributeSet = Xml.asAttributeSet(parser)
        attrs.forEach { kvp ->
            if(kvp.key == "android_id")
            {
                id = context.getResources().getResourceId(kvp.value, UNSET_ID)
            } else if(kvp.key == "motionTarget") {
                if (MotionLayout.IS_IN_EDIT_MODE) {
                    mTargetId = context.getResources().getResourceId(kvp.value, mTargetId)
                    if (mTargetId == UNSET_ID) {
                        mTargetString = kvp.value
                    }
                } else {
                    if(context.getResources().getResourceType(kvp.value) == TypedValue.TYPE_STRING)
                    {
                        mTargetString = kvp.value
                    } else {
                        mTargetId = context.getResources().getResourceId(kvp.value, mTargetId)
                    }
                }
            } else if (kvp.key == "onStateTransition") {
                stateTransition = context.getResources().getInt(kvp.value, stateTransition)
            } else if (kvp.key == "transitionDisable") {
                mDisabled = context.getResources().getBoolean(kvp.value, mDisabled)
            } else if (kvp.key == "pathMotionArc") {
                mPathMotionArc = context.getResources().getInt(kvp.value, mPathMotionArc)
            } else if (kvp.key == "duration") {
                mDuration = context.getResources().getInt(kvp.value, mDuration)
            } else if (kvp.key == "upDuration") {
                mUpDuration = context.getResources().getInt(kvp.value, mUpDuration)
            } else if (kvp.key == "viewTransitionMode") {
                mViewTransitionMode = context.getResources().getInt(kvp.value, mViewTransitionMode)
            } else if (kvp.key == "motionInterpolator") {
                val type = context.getResources().getResourceType(kvp.value)
                if (type == TypedValue.TYPE_REFERENCE) {
                    mDefaultInterpolatorID = context.getResources().getResourceId(kvp.value, UNSET_ID)
                    if (mDefaultInterpolatorID != UNSET_ID) {
                        mDefaultInterpolator = INTERPOLATOR_REFERENCE_ID
                    }
                } else if (type == TypedValue.TYPE_STRING) {
                    mDefaultInterpolatorString = context.getResources().getString(
                        kvp.key,
                        kvp.value
                    )
                    if (mDefaultInterpolatorString != null
                        && mDefaultInterpolatorString!!.indexOf("/") > 0
                    ) {
                        mDefaultInterpolatorID = context.getResources().getResourceId(kvp.value, UNSET_ID)
                        mDefaultInterpolator = INTERPOLATOR_REFERENCE_ID
                    } else {
                        mDefaultInterpolator = SPLINE_STRING
                    }
                } else {
                    mDefaultInterpolator = context.getResources().getInt(kvp.value, mDefaultInterpolator)
                }
            } else if (kvp.key == "setsTag") {
                mSetsTag = context.getResources().getResourceId(kvp.value, mSetsTag)
            } else if (kvp.key == "clearsTag") {
                mClearsTag = context.getResources().getResourceId(kvp.value, mClearsTag)
            } else if (kvp.key == "ifTagSet") {
                mIfTagSet = context.getResources().getResourceId(kvp.value, mIfTagSet)
            } else if (kvp.key == "ifTagNotSet") {
                mIfTagNotSet = context.getResources().getResourceId(kvp.value, mIfTagNotSet)
            } else if (kvp.key == "SharedValueId") {
                sharedValueID = context.getResources().getResourceId(kvp.value, mIfTagNotSet)
            } else if (kvp.key == "SharedValue") {
                sharedValue = context.getResources().getInt(kvp.value, sharedValue)
            }
        }
    }

    fun applyIndependentTransition(
        controller: ViewTransitionController,
        motionLayout: MotionLayout,
        view: TView
    ) {
        val motionController = MotionController(view)
        motionController.setBothStates(view)
        mKeyFrames!!.addAllFrames(motionController)
        motionController.setup(
            motionLayout.self.getWidth(), motionLayout.self.getHeight(),
            mDuration.toFloat(), nanoTime()
        )
        Animate(
            controller, motionController,
            mDuration, mUpDuration, stateTransition,
            getInterpolator(motionLayout.self.getContext()), mSetsTag, mClearsTag
        )
    }

    class Animate(
        controller: ViewTransitionController,
        motionController: MotionController,
        duration: Int, upDuration: Int, mode: Int,
        interpolator: Interpolator?, setTag: String, clearTag: String
    ) {
        private val mSetsTag: String
        private val mClearsTag: String
        var mStart: Long
        var mMC: MotionController
        var mDuration: Int
        var mUpDuration: Int
        var mCache: KeyCache = KeyCache()
        var mVtController: ViewTransitionController
        var mInterpolator: Interpolator?
        var mReverse = false
        var mPosition = 0f
        var mDpositionDt: Float
        var mLastRender: Long
        var mTempRec: Rect = Rect()
        var mHoldAt100 = false

        init {
            mVtController = controller
            mMC = motionController
            mDuration = duration
            mUpDuration = upDuration
            mStart = nanoTime()
            mLastRender = mStart
            mVtController.addAnimation(this)
            mInterpolator = interpolator
            mSetsTag = setTag
            mClearsTag = clearTag
            if (mode == ONSTATE_ACTION_DOWN_UP) {
                mHoldAt100 = true
            }
            mDpositionDt = if (duration == 0) Float.MAX_VALUE else 1f / duration
            mutate()
        }

        fun reverse(dir: Boolean) {
            mReverse = dir
            if (mReverse && mUpDuration != UNSET) {
                mDpositionDt = if (mUpDuration == 0) Float.MAX_VALUE else 1f / mUpDuration
            }
            mVtController.invalidate()
            mLastRender = nanoTime()
        }

        fun mutate() {
            if (mReverse) {
                mutateReverse()
            } else {
                mutateForward()
            }
        }

        fun mutateReverse() {
            val current: Long = nanoTime()
            val elapse = current - mLastRender
            mLastRender = current
            mPosition -= (elapse * 1E-6).toFloat() * mDpositionDt
            if (mPosition < 0.0f) {
                mPosition = 0.0f
            }
            val ipos =
                if (mInterpolator == null) mPosition else mInterpolator!!.getInterpolation(mPosition)
            val repaint: Boolean = mMC.interpolate(mMC.mView!!, ipos, current, mCache)
            if (mPosition <= 0) {
                if (mSetsTag != UNSET_ID) {
                    mMC.view!!.setTag(mSetsTag, nanoTime())
                }
                if (mClearsTag != UNSET_ID) {
                    mMC.view!! .setTag(mClearsTag, null)
                }
                mVtController.removeAnimation(this)
            }
            if (mPosition > 0f || repaint) {
                mVtController.invalidate()
            }
        }

        fun mutateForward() {
            val current: Long = nanoTime()
            val elapse = current - mLastRender
            mLastRender = current
            mPosition += (elapse * 1E-6).toFloat() * mDpositionDt
            if (mPosition >= 1.0f) {
                mPosition = 1.0f
            }
            val ipos =
                if (mInterpolator == null) mPosition else mInterpolator!!.getInterpolation(mPosition)
            val repaint: Boolean = mMC.interpolate(mMC.view!!, ipos, current, mCache)
            if (mPosition >= 1) {
                if (mSetsTag != UNSET_ID) {
                    mMC.view?.setTag(mSetsTag, nanoTime())
                }
                if (mClearsTag != UNSET_ID) {
                    mMC.view?.setTag(mClearsTag, null)
                }
                if (!mHoldAt100) {
                    mVtController.removeAnimation(this)
                }
            }
            if (mPosition < 1f || repaint) {
                mVtController.invalidate()
            }
        }

        fun reactTo(action: Int, x: Float, y: Float) {
            when (action) {
                MotionEvent.ACTION_UP -> {
                    if (!mReverse) {
                        reverse(true)
                    }
                    return
                }
                MotionEvent.ACTION_MOVE -> {
                    val view: TView = mMC.view!!
                    view.getHitRect(mTempRec)
                    if (!mTempRec.contains(x.toInt(), y.toInt())) {
                        if (!mReverse) {
                            reverse(true)
                        }
                    }
                }
            }
        }
    }

    fun applyTransition(
        controller: ViewTransitionController,
        layout: MotionLayout,
        fromId: String,
        current: ConstraintSet?,
        vararg views: TView
    ) {
        if (mDisabled) {
            return
        }
        if (mViewTransitionMode == VIEWTRANSITIONMODE_NOSTATE) {
            applyIndependentTransition(controller, layout, views[0])
            return
        }
        if (mViewTransitionMode == VIEWTRANSITIONMODE_ALLSTATES) {
            val ids = layout.constraintSetIds
            ids?.let {
                for (i in ids.indices) {
                    val id = ids[i]
                    if (id == fromId) {
                        continue
                    }
                    val cSet: ConstraintSet? = layout.getConstraintSet(id)
                    for (view in views) {
                        val constraint: ConstraintSet.Constraint? = cSet?.getConstraint(view.getId())
                        if (mConstraintDelta != null) {
                            mConstraintDelta!!.applyDelta(constraint)
                            constraint?.mCustomConstraints?.putAll(mConstraintDelta!!.mCustomConstraints)
                        }
                    }
                }
            }
        }
        val transformedState = ConstraintSet()
        transformedState.clone(current)
        for (view in views) {
            val constraint: ConstraintSet.Constraint? = transformedState.getConstraint(view.getId())
            if (mConstraintDelta != null) {
                mConstraintDelta!!.applyDelta(constraint)
                constraint?.mCustomConstraints?.putAll(mConstraintDelta!!.mCustomConstraints)
            }
        }
        layout.updateState(fromId, transformedState)
        layout.updateState("view_transition", current)
        layout.setState("view_transition", -1, -1)
        val tmpTransition: MotionScene.Transition =
            MotionScene.Transition(UNSET_ID, layout.mScene!!, "view_transition", fromId)
        for (view in views) {
            updateTransition(tmpTransition, view)
        }
        layout.setTransition(tmpTransition)
        layout.transitionToEnd(object : TRunnable {
            override fun run() {
                if (mSetsTag != UNSET_ID) {
                    for (view in views) {
                        view.setTag(mSetsTag, nanoTime())
                    }
                }
                if (mClearsTag != UNSET_ID) {
                    for (view in views) {
                        view.setTag(mClearsTag, null)
                    }
                }
            }
        })
    }

    private fun updateTransition(transition: MotionScene.Transition, view: TView) {
        if (mDuration != -1) {
            transition.duration = mDuration
        }
        transition.pathMotionArc = mPathMotionArc
        transition.setInterpolatorInfo(
            mDefaultInterpolator,
            mDefaultInterpolatorString, mDefaultInterpolatorID
        )
        val id = view.getId()
        if (mKeyFrames != null) {
            val keys = mKeyFrames!!.getKeyFramesForView(KeyFrames.UNSET_ID)
            val keyFrames = KeyFrames()
            for (key in keys) {
                keyFrames.addKey(key!!.clone().setViewId(id))
            }
            transition.addKeyFrame(keyFrames)
        }
    }

    fun matchesView(view: TView?): Boolean {
        if (view == null) {
            return false
        }
        if (mTargetId == UNSET_ID && mTargetString == null) {
            return false
        }
        if (!checkTags(view)) {
            return false
        }
        if (view.getId() == mTargetId) {
            return true
        }
        if (mTargetString == null) {
            return false
        }
        val lp: ViewGroup.LayoutParams = view.getLayoutParams() as ViewGroup.LayoutParams
        if (lp is ConstraintLayout.LayoutParams) {
            val tag: String? =
                (view.getLayoutParams() as ConstraintLayout.LayoutParams).constraintTag
            if (tag != null && tag.matches(mTargetString!!.toRegex())) {
                return true
            }
        }
        return false
    }

    fun supports(action: Int): Boolean {
        if (stateTransition == ONSTATE_ACTION_DOWN) {
            return action == MotionEvent.ACTION_DOWN
        }
        if (stateTransition == ONSTATE_ACTION_UP) {
            return action == MotionEvent.ACTION_UP
        }
        return if (stateTransition == ONSTATE_ACTION_DOWN_UP) {
            action == MotionEvent.ACTION_DOWN
        } else false
    }

    var isEnabled: Boolean
        get() = !mDisabled
        set(enable) {
            mDisabled = !enable
        }

    fun checkTags(view: TView): Boolean {
        val set = if (mIfTagSet == UNSET_ID) true else null != view.getTag(mIfTagSet)
        val notSet = if (mIfTagNotSet == UNSET_ID) true else null == view.getTag(mIfTagNotSet)
        return set && notSet
    }

    companion object {
        private const val TAG = "ViewTransition"
        const val VIEW_TRANSITION_TAG = "ViewTransition"
        const val KEY_FRAME_SET_TAG = "KeyFrameSet"
        const val CONSTRAINT_OVERRIDE = "ConstraintOverride"
        const val CUSTOM_ATTRIBUTE = "CustomAttribute"
        const val CUSTOM_METHOD = "CustomMethod"
        private const val UNSET = -1
        private const val UNSET_ID = ""

        // Transition can be up or down of manually fired
        const val ONSTATE_ACTION_DOWN = 1
        const val ONSTATE_ACTION_UP = 2
        const val ONSTATE_ACTION_DOWN_UP = 3
        const val ONSTATE_SHARED_VALUE_SET = 4
        const val ONSTATE_SHARED_VALUE_UNSET = 5
        const val VIEWTRANSITIONMODE_CURRENTSTATE = 0
        const val VIEWTRANSITIONMODE_ALLSTATES = 1
        const val VIEWTRANSITIONMODE_NOSTATE = 2

        // interpolator code
        private const val SPLINE_STRING = -1
        private const val INTERPOLATOR_REFERENCE_ID = -2
        const val EASE_IN_OUT = 0
        const val EASE_IN = 1
        const val EASE_OUT = 2
        const val LINEAR = 3
        const val BOUNCE = 4
        const val OVERSHOOT = 5
        const val ANTICIPATE = 6
    }
}