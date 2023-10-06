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
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.constraintlayout.widget.StateSet
import dev.topping.ios.constraint.core.motion.utils.Easing
import dev.topping.ios.constraint.core.motion.utils.RectF
import dev.topping.ios.constraint.core.state.Interpolator
import dev.topping.ios.constraint.core.state.interpolators.*
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.localPart
import kotlin.math.*

/**
 * The information to transition between multiple ConstraintSets
 * This TClass is meant to be used from XML
 *
 */
class MotionScene {
    private val mMotionLayout: MotionLayout
    var mStateSet: StateSet? = null
    var mCurrentTransition: Transition? = null
    private var mDisableAutoTransition = false

    /**
     * Get list of Transitions know to the system
     * @return
     */
    val definedTransitions: MutableList<Transition> = mutableListOf()
    private var mDefaultTransition: Transition? = null
    private val mAbstractTransitionList: MutableList<Transition> = mutableListOf()
    private val mConstraintSetMap: LinkedHashMap<String, ConstraintSet?> = linkedMapOf()
    private val mConstraintSetIdMap: LinkedHashMap<String, String> = linkedMapOf()
    private val mDeriveMap: MutableMap<String, String> = mutableMapOf()
    private var mDefaultDuration = 400
    private var mLayoutDuringTransition = 0
    private var mLastTouchDown: MotionEvent? = null
    private var mIgnoreTouch = false
    private var mMotionOutsideRegion = false
    private var mVelocityTracker // used to support fling
            : MotionLayout.MotionTracker? = null
    private var mRtl = false
    val mViewTransitionController: ViewTransitionController?

    /**
     * Set the transition between two constraint set / states.
     * The transition will get created between the two sets
     * if it doesn't exist already.
     *
     * @param beginId id of the start constraint set or state
     * @param endId   id of the end constraint set or state
     */
    fun setTransition(beginId: String, endId: String) {
        var start = beginId
        var end = endId
        if (mStateSet != null) {
            var tmp = mStateSet!!.stateGetConstraintID(beginId, -1, -1)
            if (tmp != UNSET_ID) {
                start = tmp
            }
            tmp = mStateSet!!.stateGetConstraintID(endId, -1, -1)
            if (tmp != UNSET_ID) {
                end = tmp
            }
        }
        if (I_DEBUG) {
            Log.v(
                TAG, Debug.location + " setTransition "
                        + Debug.getName(mMotionLayout.context, beginId) + " -> "
                        + Debug.getName(mMotionLayout.context, endId)
            )
        }
        if (mCurrentTransition != null) {
            if (mCurrentTransition!!.endConstraintSetId == endId
                && mCurrentTransition!!.startConstraintSetId == beginId
            ) {
                return
            }
        }
        for (transition in definedTransitions) {
            if ((transition!!.endConstraintSetId == end
                        && transition.startConstraintSetId == start)
                || (transition.endConstraintSetId == endId
                        && transition.startConstraintSetId == beginId)
            ) {
                if (I_DEBUG) {
                    Log.v(
                        TAG, Debug.location + " found transition  "
                                + Debug.getName(mMotionLayout.context, beginId) + " -> "
                                + Debug.getName(mMotionLayout.context, endId)
                    )
                }
                mCurrentTransition = transition
                if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
                    mCurrentTransition!!.mTouchResponse!!.setRTL(mRtl)
                }
                return
            }
        }
        // No transition defined for this so we will create one?
        var matchTransition = mDefaultTransition
        for (transition in mAbstractTransitionList) {
            if (transition!!.endConstraintSetId == endId) {
                matchTransition = transition
            }
        }
        val t = Transition(this, matchTransition)
        t.startConstraintSetId = start
        t.endConstraintSetId = end
        if (start != UNSET_ID) {
            definedTransitions.add(t)
        }
        mCurrentTransition = t
    }

    /**
     * Add a transition to the motion scene. If a transition with the same id already exists
     * in the scene, the new transition will replace the existing one.
     *
     * @throws IllegalArgumentException if the transition does not have an id.
     */
    fun addTransition(transition: Transition) {
        val index = getIndex(transition)
        if (index == -1) {
            definedTransitions.add(transition)
        } else {
            definedTransitions[index] = transition
        }
    }

    /**
     * Remove the transition with the matching id from the motion scene. If no matching transition
     * is found, it does nothing.
     *
     * @throws IllegalArgumentException if the transition does not have an id.
     */
    fun removeTransition(transition: Transition) {
        val index = getIndex(transition)
        if (index != -1) {
            definedTransitions.removeAt(index)
        }
    }

    /**
     * @return the index in the transition list. -1 if transition wasn't found.
     */
    private fun getIndex(transition: Transition): Int {
        val id = transition.id
        require(id != UNSET_ID) { "The transition must have an id" }
        var index = 0
        while (index < definedTransitions.size) {
            if (definedTransitions[index].id === id) {
                return index
            }
            index++
        }
        return -1
    }

    /**
     * @return true if the layout is valid for the scene. False otherwise. Use it for the debugging
     * purposes.
     */
    fun validateLayout(layout: MotionLayout): Boolean {
        return layout === mMotionLayout && layout.mScene === this
    }

    /**
     * Set the transition to be the current transition of the motion scene.
     *
     * @param transition a transition to be set. The transition must exist within the motion scene.
     * (e.g. [.addTransition])
     */
    fun setTransition(transition: Transition?) {
        mCurrentTransition = transition
        if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.setRTL(mRtl)
        }
    }

    private fun getRealID(stateId: String): String {
        if (mStateSet != null) {
            val tmp = mStateSet!!.stateGetConstraintID(stateId, -1, -1)
            if (tmp != UNSET_ID) {
                return tmp
            }
        }
        return stateId
    }

    /**
     * Get all transitions that include this state
     * @param stateId
     * @return
     */
    fun getTransitionsWithState(stateId: String): List<Transition> {
        var stateId = stateId
        stateId = getRealID(stateId)
        val ret: MutableList<Transition> = mutableListOf()
        for (transition in definedTransitions) {
            if (transition.startConstraintSetId == stateId
                || transition.endConstraintSetId == stateId
            ) {
                ret.add(transition)
            }
        }
        return ret
    }

    /**
     * Add all on click listeners for the current state
     * @param motionLayout
     * @param currentState
     */
    fun addOnClickListeners(motionLayout: MotionLayout, currentState: String) {
        // remove all on clicks listeners
        for (transition in definedTransitions) {
            if (transition.mOnClicks.size > 0) {
                for (onClick in transition.mOnClicks) {
                    onClick.removeOnClickListeners(motionLayout)
                }
            }
        }
        for (transition in mAbstractTransitionList) {
            if (transition.mOnClicks.size > 0) {
                for (onClick in transition.mOnClicks) {
                    onClick.removeOnClickListeners(motionLayout)
                }
            }
        }
        // add back all the listeners that are needed
        for (transition in definedTransitions) {
            if (transition.mOnClicks.size > 0) {
                for (onClick in transition.mOnClicks) {
                    onClick.addOnClickListeners(motionLayout, currentState, transition)
                }
            }
        }
        for (transition in mAbstractTransitionList) {
            if (transition.mOnClicks.size > 0) {
                for (onClick in transition.mOnClicks) {
                    onClick.addOnClickListeners(motionLayout, currentState, transition)
                }
            }
        }
    }

    /**
     * Find the best transition for the motion
     * @param currentState
     * @param dx drag delta x
     * @param dy drag delta y
     * @param lastTouchDown
     * @return
     */
    fun bestTransitionFor(
        currentState: String,
        dx: Float,
        dy: Float,
        lastTouchDown: MotionEvent?
    ): Transition? {
        var candidates: List<Transition>? = null
        if (currentState != UNSET_ID) {
            candidates = getTransitionsWithState(currentState)
            var max = 0f
            var best: Transition? = null
            val cache = RectF()
            for (transition in candidates) {
                if (transition.mDisable) {
                    continue
                }
                if (transition.mTouchResponse != null) {
                    transition.mTouchResponse!!.setRTL(mRtl)
                    var region: RectF? =
                        transition.mTouchResponse!!.getTouchRegion(mMotionLayout.self, cache)
                    if (region != null && lastTouchDown != null && !region!!.contains(
                            lastTouchDown!!.getX(),
                            lastTouchDown!!.getY()
                        )
                    ) {
                        continue
                    }
                    region = transition.mTouchResponse!!.getLimitBoundsTo(mMotionLayout.self, cache)
                    if (region != null && lastTouchDown != null && !region!!.contains(
                            lastTouchDown!!.getX(),
                            lastTouchDown!!.getY()
                        )
                    ) {
                        continue
                    }
                    var `val`: Float = transition.mTouchResponse!!.dot(dx, dy)
                    if (transition.mTouchResponse!!.mIsRotateMode && lastTouchDown != null) {
                        val startX: Float = (lastTouchDown.x
                                - transition.mTouchResponse!!.mRotateCenterX)
                        val startY: Float = (lastTouchDown.y
                                - transition.mTouchResponse!!.mRotateCenterY)
                        val endX = dx + startX
                        val endY = dy + startY
                        val endAngle: Double = atan2(endY, endX).toDouble()
                        val startAngle: Double = atan2(startX, startY).toDouble()
                        `val` = (endAngle - startAngle).toFloat() * 10
                    }
                    `val` *= if (transition.endConstraintSetId == currentState) { // flip because backwards
                        -1f
                    } else {
                        1.1f // slightly bias towards the transition which is start over end
                    }
                    if (`val` > max) {
                        max = `val`
                        best = transition
                    }
                }
            }
            if (I_DEBUG) {
                if (best != null) {
                    Log.v(
                        TAG, Debug.location + "  ### BEST ----- "
                                + best.debugString(mMotionLayout.context) + " ----"
                    )
                } else {
                    Log.v(TAG, Debug.location + "  ### BEST ----- " + null + " ----")
                }
            }
            return best
        }
        return mCurrentTransition
    }

    /**
     * Find the transition based on the id
     * @param id
     * @return
     */
    fun getTransitionById(id: String): Transition? {
        for (transition in definedTransitions) {
            if (transition.id == id) {
                return transition
            }
        }
        return null
    }

    /**
     * Get the list of all Constraint Sets Know to the system
     * @return
     */
    val constraintSetIds: Array<String>
        get() {
            return mConstraintSetMap.keys.toTypedArray()
        }

    /**
     * Get the id's of all constraintSets with the matching types
     * @return
     */
    fun getMatchingStateLabels(vararg types: String): Array<String> {
        return mConstraintSetMap.filter { kvp ->
            kvp.value?.matchesLabels(*types) == true
        }.keys.toTypedArray()
        /*val ids = Array(mConstraintSetMap.size){ "" }
        var count = 0
        for (i in ids.indices) {
            val set: ConstraintSet? = mConstraintSetMap.values[i]
            val id = mConstraintSetMap.keys[i]
            if (set.matchesLabels(types)) {
                @SuppressWarnings("unused") val s: Array<String> = set.getStateLabels()
                ids[count++] = id
            }
        }
        return Arrays.copyOf(ids, count)*/
    }

    /**
     * This will launch a transition to another state if an autoTransition is enabled on
     * a Transition that matches the current state.
     *
     * @param motionLayout
     * @param currentState
     * @return
     */
    fun autoTransition(motionLayout: MotionLayout, currentState: String): Boolean {
        if (isProcessingTouch) {
            return false
        }
        if (mDisableAutoTransition) {
            return false
        }
        for (transition in definedTransitions) {
            if (transition.autoTransition == Transition.AUTO_NONE) {
                continue
            }
            if (mCurrentTransition === transition
                && mCurrentTransition!!.isTransitionFlag(Transition.TRANSITION_FLAG_INTRA_AUTO)
            ) {
                continue
            }
            if ((currentState == transition.startConstraintSetId) && (transition.autoTransition == Transition.AUTO_ANIMATE_TO_END
                        || transition.autoTransition == Transition.AUTO_JUMP_TO_END)
            ) {
                motionLayout.setState(MotionLayout.TransitionState.FINISHED)
                motionLayout.setTransition(transition)
                if (transition.autoTransition == Transition.AUTO_ANIMATE_TO_END) {
                    motionLayout.transitionToEnd()
                    motionLayout.setState(MotionLayout.TransitionState.SETUP)
                    motionLayout.setState(MotionLayout.TransitionState.MOVING)
                } else {
                    motionLayout.progress = 1f
                    motionLayout.evaluate(true)
                    motionLayout.setState(MotionLayout.TransitionState.SETUP)
                    motionLayout.setState(MotionLayout.TransitionState.MOVING)
                    motionLayout.setState(MotionLayout.TransitionState.FINISHED)
                    motionLayout.onNewStateAttachHandlers()
                }
                return true
            }
            if ((currentState == transition.endConstraintSetId) && (transition.autoTransition == Transition.AUTO_ANIMATE_TO_START
                        || transition.autoTransition == Transition.AUTO_JUMP_TO_START)
            ) {
                motionLayout.setState(MotionLayout.TransitionState.FINISHED)
                motionLayout.setTransition(transition)
                if (transition.autoTransition == Transition.AUTO_ANIMATE_TO_START) {
                    motionLayout.transitionToStart()
                    motionLayout.setState(MotionLayout.TransitionState.SETUP)
                    motionLayout.setState(MotionLayout.TransitionState.MOVING)
                } else {
                    motionLayout.progress = 0f
                    motionLayout.evaluate(true)
                    motionLayout.setState(MotionLayout.TransitionState.SETUP)
                    motionLayout.setState(MotionLayout.TransitionState.MOVING)
                    motionLayout.setState(MotionLayout.TransitionState.FINISHED)
                    motionLayout.onNewStateAttachHandlers()
                }
                return true
            }
        }
        return false
    }

    private val isProcessingTouch: Boolean
        private get() = mVelocityTracker != null

    /**
     * Set Right to left
     * @param rtl
     */
    fun setRtl(rtl: Boolean) {
        mRtl = rtl
        if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.setRTL(mRtl)
        }
    }

    /**
     * Apply the viewTransition on the list of views
     * @param id
     * @param view
     */
    fun viewTransition(id: String, vararg view: TView?) {
        mViewTransitionController!!.viewTransition(id, *view)
    }

    /**
     * Enable this viewTransition
     * @param id of viewTransition
     * @param enable
     */
    fun enableViewTransition(id: String, enable: Boolean) {
        mViewTransitionController!!.enableViewTransition(id, enable)
    }

    /**
     * Is this view transition enabled
     * @param id of viewTransition
     * @return
     */
    fun isViewTransitionEnabled(id: String): Boolean {
        return mViewTransitionController!!.isViewTransitionEnabled(id)
    }

    /**
     * Apply a view transition to the MotionController
     * @param viewTransitionId of viewTransition
     * @param motionController
     * @return
     */
    fun applyViewTransition(viewTransitionId: String, motionController: MotionController): Boolean {
        return mViewTransitionController!!.applyViewTransition(viewTransitionId, motionController)
    }
    ///////////////////////////////////////////////////////////////////////////////
    // ====================== Transition ==========================================
    /**
     * Transition defines the interaction from one state to another.
     * With out a Transition object Transition between two stats involves strictly linear
     * interpolation
     */
    class Transition {
        /**
         * Transitions can be given and ID. If unset it returns UNSET (-1)
         *
         * @return The Id of the Transition set in the MotionScene File or UNSET (-1)
         */
        var id = UNSET_ID
            private set
        var mIsAbstract = false

        /**
         * Get the id of the constraint set to go to
         *
         * @return
         */
        var endConstraintSetId = UNSET_ID

        /**
         * Gets the id of the starting constraint set
         *
         * @return
         */
        var startConstraintSetId = UNSET_ID
        var mDefaultInterpolator = 0
        var mDefaultInterpolatorString: String? = null
        var mDefaultInterpolatorID = UNSET_ID
        var mDuration = 400
        /**
         * Gets the stagger value.
         *
         * @return
         */
        /**
         * Sets the stagger value.
         * A Stagger value of zero means no stagger.
         * A Stagger value of 1 means the last view starts moving at .5 progress
         *
         * @param stagger
         */
        var stagger = 0.0f
        private val mMotionScene: MotionScene
        var mKeyFramesList: ArrayList<KeyFrames> = ArrayList()
        var mTouchResponse: TouchResponse? = null
        val mOnClicks: ArrayList<TransitionOnClick> = ArrayList()
        /**
         * return the autoTransitionType.
         * one of AUTO_NONE, AUTO_JUMP_TO_START, AUTO_JUMP_TO_END, AUTO_ANIMATE_TO_START,
         * AUTO_ANIMATE_TO_END
         *
         * @return 0=NONE, 1=JUMP_TO_START, 2=JUMP_TO_END, 3=ANIMATE_TO_START, 4=ANIMATE_TO_END
         */
        /**
         * sets the autoTransitionType
         * On reaching a state auto transitions may be run based on
         * one of AUTO_NONE, AUTO_JUMP_TO_START, AUTO_JUMP_TO_END, AUTO_ANIMATE_TO_START,
         * AUTO_ANIMATE_TO_END
         *
         * @param type
         */
        var autoTransition = 0
        var mDisable = false
        /**
         * gets the pathMotionArc for the all motions in this transition.
         * if set to UNSET (default) it reverts to the setting of the constraintSet
         *
         * @return arcMode
         */
        /**
         * Sets the pathMotionArc for the all motions in this transition.
         * if set to UNSET (default) it reverts to the setting of the constraintSet
         *
         * @param arcMode
         */
        var pathMotionArc = UNSET
        /**
         * get the mode of layout during transition
         * @return
         */
        /**
         * set the mode of layout during transition
         * @param mode
         */
        var layoutDuringTransition = 0
        private var mTransitionFlags = 0

        /**
         * Set the onSwipe for this Transition
         * @param onSwipe
         */
        fun setOnSwipe(onSwipe: OnSwipe?) {
            mTouchResponse =
                if (onSwipe == null) null else TouchResponse(mMotionScene.mMotionLayout, onSwipe)
        }

        /**
         * Add the onclick to this view
         * @param id
         * @param action
         */
        fun addOnClick(id: String, action: Int) {
            for (onClick in mOnClicks) {
                if (onClick.mTargetId == id) {
                    onClick.mMode = action
                    return
                }
            }
            val click = TransitionOnClick(this, id, action)
            mOnClicks.add(click)
        }

        /**
         * Remove the onclick added to this view
         * @param id
         */
        fun removeOnClick(id: String) {
            var toRemove: TransitionOnClick? = null
            for (onClick in mOnClicks) {
                if (onClick.mTargetId == id) {
                    toRemove = onClick
                    break
                }
            }
            if (toRemove != null) {
                mOnClicks.remove(toRemove)
            }
        }

        /**
         * Add on Click support using the xml parser
         * @param context
         * @param parser
         */
        fun addOnClick(context: TContext, parser: XmlBufferedReader) {
            mOnClicks.add(TransitionOnClick(context, this, parser))
        }
        /**
         * gets the default transition duration
         *
         * @return duration int milliseconds
         */
        /**
         * sets the duration of the transition
         * if set to < 8 it will be set to 8
         *
         * @param duration in milliseconds (min is 8)
         */
        var duration: Int
            get() = mDuration
            set(duration) {
                mDuration = max(duration, MIN_DURATION)
            }
        val keyFrameList: List<KeyFrames>
            get() = mKeyFramesList

        /**
         * add a keyframe to this motion scene
         * @param keyFrames
         */
        fun addKeyFrame(keyFrames: KeyFrames) {
            mKeyFramesList.add(keyFrames)
        }

        /**
         * Get the onClick handlers.
         *
         * @return list of on click handler
         */
        val onClickList: List<TransitionOnClick>
            get() = mOnClicks

        /**
         * Get the Touch response manager
         *
         * @return
         */
        val touchResponse: TouchResponse?
            get() = mTouchResponse
        /**
         * Returns true if this Transition can be auto considered for transition
         * Default is enabled
         */
        /**
         * enable or disable the Transition. If a Transition is disabled it is not eligible
         * for automatically switching to.
         *
         * @param enable
         */
        var isEnabled: Boolean
            get() = !mDisable
            set(enable) {
                mDisable = !enable
            }

        /**
         * Print a debug string indicating the starting and ending state of the transition
         *
         * @param context
         * @return
         */
        fun debugString(context: TContext): String {
            var ret: String
            ret = if (startConstraintSetId == UNSET_ID) {
                "null"
            } else {
                context.getResources().getResourceEntryName(startConstraintSetId)
            }
            ret += if (endConstraintSetId == UNSET_ID) {
                " -> " + "null"
            } else {
                " -> " + context.getResources().getResourceEntryName(endConstraintSetId)
            }
            return ret
        }

        /**
         * is the transition flag set
         * @param flag
         * @return
         */
        fun isTransitionFlag(flag: Int): Boolean {
            return 0 != mTransitionFlags and flag
        }

        fun setTransitionFlag(flag: Int) {
            mTransitionFlags = flag
        }

        /**
         * Set the on touch up mode
         * @param touchUpMode
         */
        fun setOnTouchUp(touchUpMode: Int) {
            val touchResponse: TouchResponse? = touchResponse
            if (touchResponse != null) {
                touchResponse.setTouchUpMode(touchUpMode)
            }
        }

        class TransitionOnClick : TView.OnClickListener {
            private val mTransition: Transition
            var mTargetId = UNSET_ID
            var mMode = 0x11

            constructor(
                context: TContext,
                transition: Transition,
                parser: XmlBufferedReader
            ) {
                mTransition = transition
                val a = Xml.asAttributeSet(parser)
                a.forEach { kvp ->
                    if (kvp.key == "targetId") {
                        mTargetId = context.getResources().getString(kvp.key, kvp.value)
                    } else if (kvp.key == "clickAction") {
                        mMode = context.getResources().getInt(kvp.key, kvp.value, mMode)
                    }
                }
            }

            constructor(transition: Transition, id: String, action: Int) {
                mTransition = transition
                mTargetId = id
                mMode = action
            }

            /**
             * Add the on click listeners for the current state
             *
             * @param motionLayout
             * @param currentState
             * @param transition
             */
            fun addOnClickListeners(
                motionLayout: MotionLayout,
                currentState: String,
                transition: Transition?
            ) {
                val v: TView? =
                    if (mTargetId == UNSET_ID) motionLayout.self else motionLayout.self.findViewById(mTargetId)
                if (v == null) {
                    Log.e(TAG, "OnClick could not find id $mTargetId")
                    return
                }
                val start = transition!!.startConstraintSetId
                val end = transition.endConstraintSetId
                if (start == UNSET_ID) { // does not require a known end state
                    v.setOnClickListener(this)
                    return
                }
                var listen = mMode and ANIM_TO_END != 0 && currentState == start
                listen = listen or (mMode and JUMP_TO_END != 0 && currentState == start)
                listen = listen or (mMode and ANIM_TO_END != 0 && currentState == start)
                listen = listen or (mMode and ANIM_TO_START != 0 && currentState == end)
                listen = listen or (mMode and JUMP_TO_START != 0 && currentState == end)
                if (listen) {
                    v.setOnClickListener(this)
                }
            }

            /**
             * Remove the OnClickListeners
             * (typically called because you are removing the transition)
             *
             * @param motionLayout
             */
            fun removeOnClickListeners(motionLayout: MotionLayout) {
                if (mTargetId == UNSET_ID) {
                    return
                }
                val v: TView? = motionLayout.self.findViewById(mTargetId)
                if (v == null) {
                    Log.e(TAG, " (*)  could not find id $mTargetId")
                    return
                }
                v.setOnClickListener(null)
            }

            fun isTransitionViable(current: Transition?, tl: MotionLayout): Boolean {
                if (mTransition === current) {
                    return true
                }
                val dest = mTransition.endConstraintSetId
                val from = mTransition.startConstraintSetId
                return if (from == UNSET_ID) {
                    tl.currentState != dest
                } else tl.currentState == from || tl.currentState == dest
            }

            override fun onClick(view: TView?) {
                val tl: MotionLayout = mTransition.mMotionScene.mMotionLayout
                if (!tl.isInteractionEnabled) {
                    return
                }
                if (mTransition.startConstraintSetId == UNSET_ID) {
                    val currentState = tl.currentState
                    if (currentState == UNSET_ID) {
                        tl.transitionToState(mTransition.endConstraintSetId)
                        return
                    }
                    val t = Transition(mTransition.mMotionScene, mTransition)
                    t.startConstraintSetId = currentState
                    t.endConstraintSetId = mTransition.endConstraintSetId
                    tl.setTransition(t)
                    tl.transitionToEnd()
                    return
                }
                val current = mTransition.mMotionScene.mCurrentTransition
                var forward = mMode and ANIM_TO_END != 0 || mMode and JUMP_TO_END != 0
                var backward = mMode and ANIM_TO_START != 0 || mMode and JUMP_TO_START != 0
                val bidirectional = forward && backward
                if (bidirectional) {
                    if (mTransition.mMotionScene.mCurrentTransition !== mTransition) {
                        tl.setTransition(mTransition)
                    }
                    if (tl.currentState == tl.endState || tl.progress > 0.5f) {
                        forward = false
                    } else {
                        backward = false
                    }
                }
                if (isTransitionViable(current, tl)) {
                    if (forward && mMode and ANIM_TO_END != 0) {
                        tl.setTransition(mTransition)
                        tl.transitionToEnd()
                    } else if (backward && mMode and ANIM_TO_START != 0) {
                        tl.setTransition(mTransition)
                        tl.transitionToStart()
                    } else if (forward && mMode and JUMP_TO_END != 0) {
                        tl.setTransition(mTransition)
                        tl.progress = 1f
                    } else if (backward && mMode and JUMP_TO_START != 0) {
                        tl.setTransition(mTransition)
                        tl.progress = 0f
                    }
                }
            }

            companion object {
                const val ANIM_TO_END = 0x0001
                const val ANIM_TOGGLE = 0x0011
                const val ANIM_TO_START = 0x0010
                const val JUMP_TO_END = 0x100
                const val JUMP_TO_START = 0x1000
            }
        }

        internal constructor(motionScene: MotionScene, global: Transition?) {
            mMotionScene = motionScene
            mDuration = motionScene.mDefaultDuration
            if (global != null) {
                pathMotionArc = global.pathMotionArc
                mDefaultInterpolator = global.mDefaultInterpolator
                mDefaultInterpolatorString = global.mDefaultInterpolatorString
                mDefaultInterpolatorID = global.mDefaultInterpolatorID
                mDuration = global.mDuration
                mKeyFramesList = global.mKeyFramesList
                stagger = global.stagger
                layoutDuringTransition = global.layoutDuringTransition
            }
        }

        /**
         * Create a transition
         *
         * @param id                   a unique id to represent the transition.
         * @param motionScene          the motion scene that the transition will be added to.
         * @param constraintSetStartId id of the ConstraintSet to be used for the start of
         * transition
         * @param constraintSetEndId   id of the ConstraintSet to be used for the end of transition
         */
        constructor(
            id: String,
            motionScene: MotionScene,
            constraintSetStartId: String,
            constraintSetEndId: String
        ) {
            this.id = id
            mMotionScene = motionScene
            startConstraintSetId = constraintSetStartId
            endConstraintSetId = constraintSetEndId
            mDuration = motionScene.mDefaultDuration
            layoutDuringTransition = motionScene.mLayoutDuringTransition
        }

        internal constructor(motionScene: MotionScene, context: TContext, parser: XmlBufferedReader) {
            mDuration = motionScene.mDefaultDuration
            layoutDuringTransition = motionScene.mLayoutDuringTransition
            mMotionScene = motionScene
            fillFromAttributeList(motionScene, context, Xml.asAttributeSet(parser))
        }

        /**
         * Sets the interpolation used for this transition.
         * <br></br>
         * The call support standard types EASE_IN_OUT etc.:<br></br>
         * setInterpolatorInfo(MotionScene.Transition.INTERPOLATE_EASE_IN_OUT, null, 0);
         * setInterpolatorInfo(MotionScene.Transition.INTERPOLATE_OVERSHOOT, null, 0);
         * <br></br>
         * Strings such as "cubic(...)" , "spline(...)"<br></br>
         * setInterpolatorInfo(
         * MotionScene.Transition.INTERPOLATE_SPLINE_STRING, "cubic(1,0,0,1)", 0);
         * <br></br>
         * Android interpolators in res/anim : <br></br>
         * setInterpolatorInfo(
         * MotionScene.Transition.INTERPOLATE_REFERENCE_ID, null, R.anim....);
         * <br></br>
         * @param interpolator sets the type of interpolation (MotionScene.Transition.INTERPOLATE_*)
         * @param interpolatorString sets a string based custom interpolation
         * @param interpolatorID sets the id of a Android Transition
         */
        fun setInterpolatorInfo(
            interpolator: Int,
            interpolatorString: String?,
            interpolatorID: String
        ) {
            mDefaultInterpolator = interpolator
            mDefaultInterpolatorString = interpolatorString
            mDefaultInterpolatorID = interpolatorID
        }

        private fun fillFromAttributeList(
            motionScene: MotionScene,
            context: TContext,
            attrs: AttributeSet
        ) {
            fill(motionScene, context, attrs)
        }

        private fun fill(motionScene: MotionScene, context: TContext, a: AttributeSet) {
            a.forEach { kvp ->
                if (kvp.key == "constraintSetEnd") {
                    endConstraintSetId = context.getResources().getResourceId(kvp.value, endConstraintSetId)
                    val type = context.getResources().getResourceType(
                        endConstraintSetId
                    )
                    if (type == TypedValue.TYPE_LAYOUT) {
                        val cSet = ConstraintSet()
                        cSet.load(context, endConstraintSetId)
                        motionScene.mConstraintSetMap.put(endConstraintSetId, cSet)
                        if (I_DEBUG) {
                            Log.v(
                                TAG, " constraint Set end loaded from layout "
                                        + Debug.getName(context, endConstraintSetId)
                            )
                        }
                    } else if (type == TypedValue.TYPE_XML) {
                        val id = motionScene.parseInclude(context, endConstraintSetId)
                        endConstraintSetId = id
                    }
                } else if (kvp.key == "constraintSetStart") {
                    startConstraintSetId = context.getResources().getResourceId(kvp.value, startConstraintSetId)
                    val type = context.getResources().getResourceType(
                        startConstraintSetId
                    )
                    if (type == TypedValue.TYPE_LAYOUT) {
                        val cSet = ConstraintSet()
                        cSet.load(context, startConstraintSetId)
                        motionScene.mConstraintSetMap.put(startConstraintSetId, cSet)
                    } else if (type == TypedValue.TYPE_XML) {
                        val id = motionScene.parseInclude(context, startConstraintSetId)
                        startConstraintSetId = id
                    }
                } else if (kvp.key == "motionInterpolator") {
                    val type = context.getResources().getResourceType(kvp.key)
                    if (type == TypedValue.TYPE_REFERENCE) {
                        mDefaultInterpolatorID = context.getResources().getResourceId(kvp.value, mDefaultInterpolatorID)
                        if (mDefaultInterpolatorID != UNSET_ID) {
                            mDefaultInterpolator = INTERPOLATOR_REFERENCE_ID
                        }
                    } else if (type == TypedValue.TYPE_STRING) {
                        mDefaultInterpolatorString = context.getResources().getString(
                            kvp.key,
                            kvp.value
                        )
                        if (mDefaultInterpolatorString != null) {
                            if (mDefaultInterpolatorString!!.indexOf("/") > 0) {
                                mDefaultInterpolatorID = context.getResources().getString(
                                    kvp.key,
                                    kvp.value
                                )
                                mDefaultInterpolator = INTERPOLATOR_REFERENCE_ID
                            } else {
                                mDefaultInterpolator = SPLINE_STRING
                            }
                        }
                    } else {
                        mDefaultInterpolator = context.getResources().getInt(
                            kvp.key,
                            kvp.value,
                            mDefaultInterpolator
                        )
                    }
                } else if (kvp.key == "duration") {
                    mDuration = context.getResources().getInt(kvp.key, kvp.key, mDuration)
                    if (mDuration < MIN_DURATION) {
                        mDuration = MIN_DURATION
                    }
                } else if (kvp.key == "staggered") {
                    stagger = context.getResources().getFloat(kvp.key, kvp.key, stagger)
                } else if (kvp.key == "autoTransition") {
                    autoTransition = context.getResources().getInt(kvp.key, kvp.key, autoTransition)
                } else if (kvp.key == "android_id") {
                    id = context.getResources().getResourceId(kvp.key, id)
                } else if (kvp.key == "transitionDisable") {
                    mDisable = context.getResources().getBoolean(kvp.key, mDisable)
                } else if (kvp.key == "pathMotionArc") {
                    pathMotionArc = context.getResources().getInt(kvp.key, kvp.key, pathMotionArc)
                } else if (kvp.key == "layoutDuringTransition") {
                    layoutDuringTransition = context.getResources().getInt(
                        kvp.key,
                        kvp.key,
                        layoutDuringTransition
                    )
                } else if (kvp.key == "transitionFlags") {
                    mTransitionFlags = context.getResources().getInt(
                        kvp.key,
                        kvp.key,
                        mTransitionFlags
                    )
                }
            }
            if (startConstraintSetId == UNSET_ID) {
                mIsAbstract = true
            }
        }

        companion object {
            const val AUTO_NONE = 0
            const val AUTO_JUMP_TO_START = 1
            const val AUTO_JUMP_TO_END = 2
            const val AUTO_ANIMATE_TO_START = 3
            const val AUTO_ANIMATE_TO_END = 4
            const val TRANSITION_FLAG_FIRST_DRAW = 1
            const val TRANSITION_FLAG_INTRA_AUTO = 2
            const val TRANSITION_FLAG_INTERCEPT_TOUCH = 4
            const val INTERPOLATE_REFERENCE_ID = -2
            const val INTERPOLATE_SPLINE_STRING = -1
            const val INTERPOLATE_EASE_IN_OUT = 0
            const val INTERPOLATE_EASE_IN = 1
            const val INTERPOLATE_EASE_OUT = 2
            const val INTERPOLATE_LINEAR = 3
            const val INTERPOLATE_BOUNCE = 4
            const val INTERPOLATE_OVERSHOOT = 5
            const val INTERPOLATE_ANTICIPATE = 6
        }
    }

    /**
     * Create a motion scene.
     *
     * @param layout Motion layout to which the scene will be set.
     */
    constructor(layout: MotionLayout) {
        mMotionLayout = layout
        mViewTransitionController = ViewTransitionController(layout)
    }

    internal constructor(context: TContext, layout: MotionLayout, resourceID: String) {
        mMotionLayout = layout
        mViewTransitionController = ViewTransitionController(layout)
        load(context, resourceID)
        mConstraintSetMap["motion_base"] = ConstraintSet()
        mConstraintSetIdMap["motion_base"] = "motion_base"
    }

    /**
     * Load a MotionScene   from a MotionScene.xml file
     *
     * @param context    the context for the inflation
     * @param resourceId id of xml file in res/xml/
     */
    private fun load(context: TContext, resourceId: String) {
        val res: TResources = context.getResources()
        val parser: XmlBufferedReader = context.getResources().getXml(resourceId)
        try {
            var transition: Transition? = null
            var eventType = parser.eventType
            while (eventType != EventType.END_DOCUMENT) {
                when (eventType) {
                    EventType.START_DOCUMENT, EventType.END_ELEMENT, EventType.TEXT -> {}
                    EventType.START_ELEMENT -> {
                        val tagName: String = parser.name.localPart
                        if (DEBUG_DESKTOP) {
                            println("parsing = $tagName")
                        }
                        if (I_DEBUG) {
                            Log.v(TAG, "MotionScene ----------- START_TAG $tagName")
                        }
                        when (tagName) {
                            MOTIONSCENE_TAG -> parseMotionSceneTags(context, parser)
                            TRANSITION_TAG -> {
                                definedTransitions.add(
                                    Transition(
                                        this,
                                        context,
                                        parser
                                    ).also { transition = it })
                                if (mCurrentTransition == null && !transition!!.mIsAbstract) {
                                    mCurrentTransition = transition
                                    if (mCurrentTransition != null
                                        && mCurrentTransition!!.mTouchResponse != null
                                    ) {
                                        mCurrentTransition!!.mTouchResponse!!.setRTL(mRtl)
                                    }
                                }
                                if (transition!!.mIsAbstract) { // global transition only one for now
                                    if (transition!!.endConstraintSetId == UNSET_ID) {
                                        mDefaultTransition = transition
                                    } else {
                                        mAbstractTransitionList.add(transition!!)
                                    }
                                    definedTransitions.remove(transition)
                                }
                            }
                            ONSWIPE_TAG -> {
                                if (I_DEBUG || transition == null) {
                                    val name: String = context.getResources()
                                        .getResourceEntryName(resourceId)
                                    val line = parser.locationInfo
                                    Log.v(TAG, " OnSwipe ($name.xml:$line)")
                                }
                                if (transition != null) {
                                    transition!!.mTouchResponse =
                                        TouchResponse(context, mMotionLayout, parser)
                                }
                            }
                            ONCLICK_TAG -> if (transition != null) {
                                if (!mMotionLayout.self.isInEditMode()) {
                                    transition!!.addOnClick(context, parser)
                                }
                            }
                            STATESET_TAG -> mStateSet = StateSet(context, mutableMapOf(),  parser)
                            CONSTRAINTSET_TAG -> parseConstraintSet(context, parser)
                            INCLUDE_TAG, INCLUDE_TAG_UC -> parseInclude(context, parser)
                            KEYFRAMESET_TAG -> {
                                val keyFrames = KeyFrames(context, parser)
                                if (transition != null) {
                                    transition!!.mKeyFramesList.add(keyFrames)
                                }
                            }
                            VIEW_TRANSITION -> {
                                val viewTransition = ViewTransition(context, parser)
                                mViewTransitionController!!.add(viewTransition)
                            }
                            else -> if (I_DEBUG) {
                                Log.v(
                                    TAG,
                                    getLine(
                                        context,
                                        resourceId,
                                        parser
                                    ) + "WARNING UNKNOWN ATTRIBUTE " + tagName
                                )
                            }
                        }
                    }
                    else -> {}
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            if (I_DEBUG) {
                Log.v(TAG, getLine(context, resourceId, parser) + " " + e.toString())
            }
            Log.e(TAG, "Error parsing resource: $resourceId", e)
        }
    }

    private fun parseMotionSceneTags(context: TContext, parser: XmlBufferedReader) {
        val attrs: AttributeSet = Xml.asAttributeSet(parser)
        attrs.forEach { kvp ->
            if(kvp.key == "defaultDuration") {
                mDefaultDuration = context.getResources().getInt(
                    kvp.key,
                    kvp.value,
                    mDefaultDuration
                )
                if (mDefaultDuration < MIN_DURATION) {
                    mDefaultDuration = MIN_DURATION
                }
            } else if(kvp.key == "layoutDuringTransition") {
                mLayoutDuringTransition = context.getResources().getInt(
                    kvp.key,
                    kvp.value,
                    mLayoutDuringTransition
                )
            }
        }
    }

    private fun getId(context: TContext, idString: String?): String {
        var id = UNSET_ID
        if (idString!!.contains("/")) {
            val tmp = idString.substring(idString.indexOf('/') + 1)
            id = context.getResources().getIdentifier(tmp, "id", context.getPackageName())
            if (DEBUG_DESKTOP) {
                println("id getMap res = $id")
            }
        }
        if (id == UNSET_ID) {
            if (idString != null && idString.length > 1) {
                id = idString.substring(1)
            } else {
                Log.e(TAG, "error in parsing id")
            }
        }
        return id
    }

    private fun parseInclude(context: TContext, mainParser: XmlBufferedReader) {
        val attrs: AttributeSet = Xml.asAttributeSet(mainParser)
        attrs.forEach { kvp ->
            if (kvp.key == "include_constraintSet") {
                val resourceId = context.getResources().getResourceId(kvp.value, UNSET_ID)
                parseInclude(context, resourceId)
            }
        }
    }

    private fun parseInclude(context: TContext, resourceId: String): String {
        val res: TResources = context.getResources()
        val includeParser: XmlBufferedReader = res.getXml(resourceId)
        try {
            var eventType = includeParser.eventType
            while (eventType != EventType.END_DOCUMENT) {
                val tagName: String = includeParser.name.localPart
                if (EventType.START_ELEMENT == eventType
                    && CONSTRAINTSET_TAG == tagName
                ) {
                    return parseConstraintSet(context, includeParser)
                }
                eventType = includeParser.next()
            }
        } catch (e: Exception) {
            if (I_DEBUG) {
                Log.v(TAG, getLine(context, resourceId, includeParser) + " " + e.toString())
            }
            Log.e(TAG, "Error parsing resource: $resourceId", e)
        }
        return UNSET_ID
    }

    private fun parseConstraintSet(context: TContext, parser: XmlBufferedReader): String {
        val set = ConstraintSet()
        set.isForceId = false
        val count: Int = parser.attributeCount
        var id = UNSET_ID
        var derivedId = UNSET_ID
        for (i in 0 until count) {
            val name: String = parser.getAttributeName(i).localPart
            val value: String = parser.getAttributeValue(i)
            if (DEBUG_DESKTOP) {
                println("id string = $value")
            }
            when (name) {
                "id" -> {
                    id = getId(context, value)
                    mConstraintSetIdMap[stripID(value)] = id
                    set.mIdString = Debug.getName(context, id)
                }
                "deriveConstraintsFrom" -> derivedId = getId(context, value)
                "stateLabels" -> set.setStateLabels(value)
                "constraintRotate" -> try {
                    set.mRotate = value.toInt()
                } catch (exception: NumberFormatException) {
                    when (value) {
                        "none" -> set.mRotate = 0
                        "right" -> set.mRotate = 1
                        "left" -> set.mRotate = 2
                        "x_right" -> set.mRotate = 3
                        "x_left" -> set.mRotate = 4
                    }
                }
            }
        }
        if (id != UNSET_ID) {
            if (mMotionLayout.mDebugPath != 0) {
                set.isValidateOnParse = true
            }
            set.load(context, parser)
            if (derivedId != UNSET_ID) {
                mDeriveMap.put(id, derivedId)
            }
            mConstraintSetMap[id] = set
        }
        return id
    }

    protected fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {}

    /**
     * Get the constraintSet given the id
     * @param context
     * @param id
     * @return
     */
    fun getConstraintSet(context: TContext, id: String): ConstraintSet? {
        if (DEBUG_DESKTOP) {
            println("id $id")
            println("size " + mConstraintSetMap.size)
        }
        for (i in 0 until mConstraintSetMap.size) {
            val key = mConstraintSetMap.keyAt(i)
            val idAsString: String = context.getResources().getResourceName(key)
            if (DEBUG_DESKTOP) {
                println(
                    "Id for <" + i + "> is <"
                            + idAsString + "> looking for <" + id + ">"
                )
            }
            if (id == idAsString) {
                return mConstraintSetMap[key]
            }
        }
        return null
    }

    fun getConstraintSet(id: String): ConstraintSet? {
        return getConstraintSet(id, -1, -1)
    }

    fun getConstraintSet(id: String, width: Int, height: Int): ConstraintSet? {
        var id = id
        if (DEBUG_DESKTOP) {
            println("id $id")
            println("size " + mConstraintSetMap.size)
        }
        if (mStateSet != null) {
            val cid = mStateSet!!.stateGetConstraintID(id, width, height)
            if (cid != "") {
                id = cid
            }
        }
        if (mConstraintSetMap[id] == null) {
            Log.e(
                TAG, ("Warning could not find ConstraintSet id/"
                        + Debug.getName(mMotionLayout.context, id)) + " In MotionScene"
            )
            return mConstraintSetMap[mConstraintSetMap.keyAt(0)!!]
        }
        return mConstraintSetMap[id]
    }

    /**
     * Maps the Constraint set to the id.
     *
     * @param id  - unique id to represent the ConstraintSet
     * @param set - ConstraintSet to be represented with the id.
     */
    fun setConstraintSet(id: String, set: ConstraintSet?) {
        mConstraintSetMap[id] = set
    }

    /**
     * provides the key frames & CycleFrames to the motion view to
     *
     * @param motionController
     */
    fun getKeyFrames(motionController: MotionController) {
        if (mCurrentTransition == null) {
            if (mDefaultTransition != null) {
                for (keyFrames in mDefaultTransition!!.mKeyFramesList) {
                    keyFrames.addFrames(motionController)
                }
            }
            return
        }
        for (keyFrames in mCurrentTransition!!.mKeyFramesList) {
            keyFrames.addFrames(motionController)
        }
    }

    /**
     * get key frame
     *
     * @param context
     * @param type
     * @param target
     * @param position
     * @return Key Object
     */
    fun getKeyFrame(context: TContext?, type: Int, target: String, position: Int): Key? {
        if (mCurrentTransition == null) {
            return null
        }
        for (keyFrames in mCurrentTransition!!.mKeyFramesList) {
            for (integer in keyFrames.keys) {
                if (target == integer) {
                    val keys: MutableList<Key> = keyFrames.getKeyFramesForView(integer)
                    for (key in keys) {
                        if (key.mFramePosition == position) {
                            if (key.mType == type) {
                                return key
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    fun getTransitionDirection(stateId: String): Int {
        for (transition in definedTransitions) {
            if (transition!!.startConstraintSetId == stateId) {
                return TRANSITION_BACKWARD
            }
        }
        return TRANSITION_FORWARD
    }

    /**
     * Returns true if the view has a keyframe defined at the given position
     *
     * @param view
     * @param position
     * @return true if a keyframe exists, false otherwise
     */
    fun hasKeyFramePosition(view: TView, position: Int): Boolean {
        if (mCurrentTransition == null) {
            return false
        }
        for (keyFrames in mCurrentTransition!!.mKeyFramesList) {
            val framePoints = keyFrames.getKeyFramesForView(view.getId())
            for (framePoint in framePoints) {
                if (framePoint.mFramePosition == position) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Set a keyFrame on the current Transition
     * @param view
     * @param position
     * @param name
     * @param value
     */
    fun setKeyframe(view: TView, position: Int, name: String, value: Any?) {
        if (I_DEBUG) {
            println(
                "setKeyframe for pos " + position
                        + " name <" + name + "> value: " + value
            )
        }
        if (mCurrentTransition == null) {
            return
        }
        for (keyFrames in mCurrentTransition!!.mKeyFramesList) {
            if (I_DEBUG) {
                println("key frame $keyFrames")
            }
            val framePoints = keyFrames.getKeyFramesForView(view.getId())
            if (I_DEBUG) {
                println(
                    "key frame has " + framePoints.size.toString() + " frame points"
                )
            }
            for (framePoint in framePoints) {
                if (I_DEBUG) {
                    println("framePoint pos: " + framePoint.mFramePosition)
                }
                if (framePoint.mFramePosition == position) {
                    var v = 0f
                    if (value != null) {
                        v = value!!.toString().toFloat()
                        if (I_DEBUG) {
                            println("value: $v")
                        }
                    } else {
                        if (I_DEBUG) {
                            println("value was null!!!")
                        }
                    }
                    if (v == 0f) {
                        v = 0.01f
                    }
                }
            }
        }
    }

    /**
     * Get the path percent  (Non functional currently)
     * @param view
     * @param position
     * @return
     */
    fun getPathPercent(view: TView?, position: Int): Float {
        return 0f
    }

    //////////////////////////////////////////////////////////
    // touch handling
    ///////////////////////////////////////////////////////////
    fun supportTouch(): Boolean {
        for (transition in definedTransitions) {
            if (transition.mTouchResponse != null) {
                return true
            }
        }
        return mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null
    }

    var mLastTouchX = 0f
    var mLastTouchY = 0f
    fun processTouchEvent(event: MotionEvent, currentState: String, motionLayout: MotionLayout) {
        if (I_DEBUG) {
            Log.v(TAG, Debug.location + " processTouchEvent")
        }
        val cache = RectF()
        if (mVelocityTracker == null) {
            mVelocityTracker = mMotionLayout!!.obtainVelocityTracker()
        }
        mVelocityTracker!!.addMovement(event)
        if (I_DEBUG) {
            val time: Float = event.eventTime % 100000 / 1000f
            val x: Float = event.getRawX()
            val y: Float = event.getRawY()
            Log.v(
                TAG, " " + time + "  processTouchEvent "
                        + "state=" + Debug.getState(motionLayout, currentState)
                        + "  " + Debug.getActionType(event) + " " + x
                        + ", " + y + " \t " + motionLayout.progress
            )
        }
        if (currentState != "") {
            var region: RectF?
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    mLastTouchX = event.getRawX()
                    mLastTouchY = event.getRawY()
                    mLastTouchDown = event
                    mIgnoreTouch = false
                    if (mCurrentTransition!!.mTouchResponse != null) {
                        region = mCurrentTransition!!.mTouchResponse!!.getLimitBoundsTo(mMotionLayout.self, cache)
                        if (region != null
                            && !region!!.contains(mLastTouchDown!!.getX(), mLastTouchDown!!.getY())
                        ) {
                            mLastTouchDown = null
                            mIgnoreTouch = true
                            return
                        }
                        region = mCurrentTransition!!.mTouchResponse!!.getTouchRegion(mMotionLayout.self, cache)
                        mMotionOutsideRegion = if (region != null
                            && !region!!.contains(
                                mLastTouchDown!!.getX(),
                                mLastTouchDown!!.getY()
                            )
                        ) {
                            true
                        } else {
                            false
                        }
                        mCurrentTransition!!.mTouchResponse!!.setDown(mLastTouchX, mLastTouchY)
                    }
                    if (I_DEBUG) {
                        Log.v(TAG, "----- ACTION_DOWN $mLastTouchX,$mLastTouchY")
                    }
                    return
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!mIgnoreTouch) {
                        val dy: Float = event.getRawY() - mLastTouchY
                        val dx: Float = event.getRawX() - mLastTouchX
                        if (I_DEBUG) {
                            Log.v(TAG, "----- ACTION_MOVE $dx,$dy")
                        }
                        if (dx.toDouble() == 0.0 && dy.toDouble() == 0.0 || mLastTouchDown == null) {
                            return
                        }
                        val transition = bestTransitionFor(currentState, dx, dy, mLastTouchDown)
                        if (I_DEBUG) {
                            Log.v(
                                TAG, Debug.location + " best Transition For "
                                        + dx + "," + dy + " "
                                        + transition?.debugString(mMotionLayout.context)
                            )
                        }
                        if (transition != null) {
                            motionLayout.setTransition(transition)
                            region = mCurrentTransition!!.mTouchResponse!!.getTouchRegion(
                                mMotionLayout.self,
                                cache
                            )
                            mMotionOutsideRegion = (region != null
                                    && !region!!.contains(
                                mLastTouchDown!!.getX(),
                                mLastTouchDown!!.getY()
                            ))
                            mCurrentTransition!!.mTouchResponse!!.setUpTouchEvent(
                                mLastTouchX,
                                mLastTouchY
                            )
                        }
                    }
                }
            }
        }
        if (mIgnoreTouch) {
            return
        }
        if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null && !mMotionOutsideRegion) {
            mCurrentTransition!!.mTouchResponse!!.processTouchEvent(
                event,
                mVelocityTracker, currentState, this
            )
        }
        mLastTouchX = event.rawX
        mLastTouchY = event.rawY
        if (event.action == MotionEvent.ACTION_UP) {
            if (mVelocityTracker != null) {
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
                if (motionLayout.currentState != UNSET_ID) {
                    autoTransition(motionLayout, motionLayout.currentState)
                }
            }
        }
    }

    fun processScrollMove(dx: Float, dy: Float) {
        if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.scrollMove(dx, dy)
        }
    }

    fun processScrollUp(dx: Float, dy: Float) {
        if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.scrollUp(dx, dy)
        }
    }

    /**
     * Calculate if a drag in this direction results in an increase or decrease in progress.
     *
     * @param dx drag direction in x
     * @param dy drag direction in y
     * @return change in progress given that dx and dy
     */
    fun getProgressDirection(dx: Float, dy: Float): Float {
        return if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.getProgressDirection(dx, dy)
        } else 0f
    }

    /////////////////////////////////////////////////////////////
    val startId: String
        get() = if (mCurrentTransition == null) {
            UNSET_ID
        } else mCurrentTransition!!.startConstraintSetId
    val endId: String
        get() = if (mCurrentTransition == null) {
            UNSET_ID
        } else mCurrentTransition!!.endConstraintSetId

    /**
     * Get the interpolator define for the current Transition
     * @return
     */
    val interpolator: Interpolator?
        get() {
            when (mCurrentTransition!!.mDefaultInterpolator) {
                SPLINE_STRING -> {
                    val easing = Easing.getInterpolator(mCurrentTransition!!.mDefaultInterpolatorString)
                    return object : Interpolator {
                        override fun getInterpolation(v: Float): Float {
                            return easing?.get(v.toDouble())?.toFloat() ?: 0f
                        }
                    }
                }
                INTERPOLATOR_REFERENCE_ID -> return mMotionLayout.context.loadInterpolator(
                    mCurrentTransition!!.mDefaultInterpolatorID
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
    /**
     * Get Duration of the current transition.
     *
     * @return duration in milliseconds
     */
    /**
     * Sets the duration of the current transition or the default if there is no current transition
     *
     * @param duration in milliseconds
     */
    var duration: Int
        get() = if (mCurrentTransition != null) {
            mCurrentTransition!!.mDuration
        } else mDefaultDuration
        set(duration) {
            if (mCurrentTransition != null) {
                mCurrentTransition!!.duration = duration
            } else {
                mDefaultDuration = duration
            }
        }

    /**
     * The transition arc path mode
     * @return
     */
    fun gatPathMotionArc(): Int {
        return if (mCurrentTransition != null) mCurrentTransition!!.pathMotionArc else UNSET
    }

    /**
     * Get the staggered value of the current transition.
     * Will default to 0 staggered if there is no current transition.
     *
     * @return
     */
    val staggered: Float
        get() = if (mCurrentTransition != null) {
            mCurrentTransition!!.stagger
        } else 0f
    val maxAcceleration: Float
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.maxAcceleration
        } else 0f
    val maxVelocity: Float
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.maxVelocity
        } else 0f
    val springStiffiness: Float
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.springStiffness
        } else 0f
    val springMass: Float
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.springMass
        } else 0f
    val springDamping: Float
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.springDamping
        } else 0f
    val springStopThreshold: Float
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.springStopThreshold
        } else 0f
    val springBoundary: Int
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.springBoundary
        } else 0
    val autoCompleteMode: Int
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.autoCompleteMode
        } else 0

    fun setupTouch() {
        if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.setupTouch()
        }
    }

    val moveWhenScrollAtTop: Boolean
        get() = if (mCurrentTransition != null && mCurrentTransition!!.mTouchResponse != null) {
            mCurrentTransition!!.mTouchResponse!!.moveWhenScrollAtTop
        } else false

    /**
     * read the constraints from the inflation of the ConstraintLayout
     * If the constraintset does not contain information about a view this information is used
     * as a "fallback" position.
     *
     * @param motionLayout
     */
    fun readFallback(motionLayout: MotionLayout) {
        for (i in 0 until mConstraintSetMap.size) {
            val key = mConstraintSetMap.keyAt(i)
            if (hasCycleDependency(key)) {
                Log.e(TAG, "Cannot be derived from yourself")
                return
            }
            readConstraintChain(key, motionLayout)
        }
    }

    /**
     * This is brute force but the number of ConstraintSets is typically very small (< 5)
     *
     * @param key
     * @return
     */
    private fun hasCycleDependency(key: String): Boolean {
        var derived: String? = mDeriveMap.get(key)
        var len: Int = mDeriveMap.size
        while (derived != UNSET_ID) {
            if (derived == key) {
                return true
            }
            if (len-- < 0) {
                return true
            }
            derived = mDeriveMap.get(derived)
        }
        return false
    }

    /**
     * Recursive descent of the deriveConstraintsFrom tree reading the motionLayout if
     * needed.
     *
     * @param key
     */
    private fun readConstraintChain(key: String, motionLayout: MotionLayout) {
        val cs: ConstraintSet? = mConstraintSetMap[key]
        cs?.let {
            cs.derivedState = cs.mIdString
            val derivedFromId: String = mDeriveMap.get(key) ?: UNSET_ID
            if (derivedFromId != UNSET_ID) {
                readConstraintChain(derivedFromId, motionLayout)
                val derivedFrom: ConstraintSet? = mConstraintSetMap[derivedFromId]
                if (derivedFrom == null) {
                    Log.e(
                        TAG, "ERROR! invalid deriveConstraintsFrom: @id/"
                                + Debug.getName(mMotionLayout.context, derivedFromId)
                    )
                    return
                }
                cs.derivedState += "/" + derivedFrom.derivedState
                cs.readFallback(derivedFrom)
            } else {
                cs.derivedState += "  layout"
                cs.readFallback(motionLayout)
            }
            cs.applyDeltaFrom(cs)
        }
    }

    /**
     * Used at design time
     *
     * @param id
     * @return
     */
    fun lookUpConstraintId(id: String): String {
        val boxed: String? = mConstraintSetIdMap[id]
        return if (boxed == null) {
            UNSET_ID
        } else {
            boxed
        }
    }

    /**
     * used at design time
     *
     * @return
     */
    fun lookUpConstraintName(id: String): String? {
        for (entry in mConstraintSetIdMap.entries) {
            val boxed = entry.value ?: continue
            if (boxed == id) {
                return entry.key
            }
        }
        return null
    }

    /**
     * this allow disabling autoTransitions to prevent design surface from being in undefined states
     *
     * @param disable
     */
    fun disableAutoTransition(disable: Boolean) {
        mDisableAutoTransition = disable
    }

    companion object {
        private const val TAG = "MotionScene"
        private const val I_DEBUG = false
        private const val MIN_DURATION = 8
        const val TRANSITION_BACKWARD = 0
        const val TRANSITION_FORWARD = 1
        private const val SPLINE_STRING = -1
        private const val INTERPOLATOR_REFERENCE_ID = -2
        const val UNSET = -1
        const val UNSET_ID = ""
        private const val DEBUG_DESKTOP = false
        const val LAYOUT_IGNORE_REQUEST = 0
        const val LAYOUT_HONOR_REQUEST = 1
        const val LAYOUT_CALL_MEASURE = 2
        private const val MOTIONSCENE_TAG = "MotionScene"
        private const val TRANSITION_TAG = "Transition"
        private const val ONSWIPE_TAG = "OnSwipe"
        private const val ONCLICK_TAG = "OnClick"
        private const val STATESET_TAG = "StateSet"
        private const val INCLUDE_TAG_UC = "Include"
        private const val INCLUDE_TAG = "include"
        private const val KEYFRAMESET_TAG = "KeyFrameSet"
        private const val CONSTRAINTSET_TAG = "ConstraintSet"
        private const val VIEW_TRANSITION = "ViewTransition"
        const val EASE_IN_OUT = 0
        const val EASE_IN = 1
        const val EASE_OUT = 2
        const val LINEAR = 3
        const val BOUNCE = 4
        const val OVERSHOOT = 5
        const val ANTICIPATE = 6

        /**
         * Utility to strip the @id/ from an id
         * @param id
         * @return
         */
        fun stripID(id: String?): String {
            if (id == null) {
                return ""
            }
            val index = id.indexOf('/')
            return if (index < 0) {
                id
            } else id.substring(index + 1)
        }

        /**
         * Construct a user friendly error string
         *
         * @param context    the context
         * @param resourceId the xml being parsed
         * @param pullParser the XML parser
         * @return
         */
        fun getLine(context: TContext, resourceId: String, pullParser: XmlBufferedReader): String {
            return ".(" + Debug.getName(context, resourceId)
                .toString() + ".xml:" + pullParser.locationInfo
                .toString() + ") \"" + pullParser.name.toString() + "\""
        }
    }
}
