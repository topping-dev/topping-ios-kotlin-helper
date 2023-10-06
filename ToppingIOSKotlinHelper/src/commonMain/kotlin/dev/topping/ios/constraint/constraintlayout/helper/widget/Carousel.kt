package dev.topping.ios.constraint.constraintlayout.helper.widget

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionHelper
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionLayout
import dev.topping.ios.constraint.constraintlayout.motion.widget.MotionScene
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import kotlin.math.max
import kotlin.math.min

/**
 * Carousel works within a MotionLayout to provide a simple recycler like pattern.
 * Based on a series of Transitions and callback to give you the ability to swap views.
 */
class Carousel(context: TContext, attrs: AttributeSet, self: TView) : MotionHelper(context, attrs, self) {
    private var mAdapter: Adapter? = null
    private val mList: ArrayList<TView> = arrayListOf()
    private var mPreviousIndex = 0

    /**
     * Returns the current index
     *
     * @return current index
     */
    var currentIndex = 0
        private set
    private var mMotionLayout: MotionLayout? = null
    private var mFirstViewReference = ""
    private var mInfiniteCarousel = false
    private var mBackwardTransition = ""
    private var mForwardTransition = ""
    private var mPreviousState = ""
    private var mNextState = ""
    private var mDampening = 0.9f
    private var mStartIndex = 0
    private var mEmptyViewBehavior: Int = TView.INVISIBLE
    private var mTouchUpMode = TOUCH_UP_IMMEDIATE_STOP
    private var mVelocityThreshold = 2f
    private var mTargetIndex = -1
    private var mAnimateTargetDelay = 200

    /**
     * Adapter for a Carousel
     */
    interface Adapter {
        /**
         * Number of items you want to display in the Carousel
         * @return number of items
         */
        fun count(): Int

        /**
         * Callback to populate the view for the given index
         *
         * @param view
         * @param index
         */
        fun populate(view: TView, index: Int)

        /**
         * Callback when we reach a new index
         * @param index
         */
        fun onNewItem(index: Int)
    }

    init {
        self.setParentType(this)
        val a = context.getResources()
        attrs.forEach { kvp ->
            val attr = kvp.value
            if (kvp.key == "carousel_firstView") {
                mFirstViewReference = a.getResourceId(attr, mFirstViewReference)
            } else if (kvp.key == "carousel_backwardTransition") {
                mBackwardTransition = a.getResourceId(attr, mBackwardTransition)
            } else if (kvp.key == "carousel_forwardTransition") {
                mForwardTransition = a.getResourceId(attr, mForwardTransition)
            } else if (kvp.key == "carousel_emptyViewsBehavior") {
                mEmptyViewBehavior = a.getInt(kvp.key, attr, mEmptyViewBehavior)
            } else if (kvp.key == "carousel_previousState") {
                mPreviousState = a.getResourceId(attr, mPreviousState)
            } else if (kvp.key == "carousel_nextState") {
                mNextState = a.getResourceId(attr, mNextState)
            } else if (kvp.key == "carousel_touchUp_dampeningFactor") {
                mDampening = a.getFloat(kvp.key, attr, mDampening)
            } else if (kvp.key == "carousel_touchUpMode") {
                mTouchUpMode = a.getInt(kvp.key, attr, mTouchUpMode)
            } else if (kvp.key == "carousel_touchUp_velocityThreshold") {
                mVelocityThreshold = a.getFloat(kvp.key, attr, mVelocityThreshold)
            } else if (kvp.key == "carousel_infinite") {
                mInfiniteCarousel = a.getBoolean(attr, mInfiniteCarousel)
            }
        }
    }

    fun setAdapter(adapter: Adapter?) {
        mAdapter = adapter
    }

    /**
     * Returns the number of elements in the Carousel
     *
     * @return number of elements
     */
    val count: Int
        get() {
            return if (mAdapter != null) {
                mAdapter!!.count()
            } else 0
        }

    /**
     * Transition the carousel to the given index, animating until we reach it.
     *
     * @param index index of the element we want to reach
     * @param delay animation duration for each individual transition to the next item, in ms
     */
    fun transitionToIndex(index: Int, delay: Int) {
        mTargetIndex = max(0, min(count - 1, index))
        mAnimateTargetDelay = max(0, delay)
        mMotionLayout?.setTransitionDuration(mAnimateTargetDelay)
        if (index < currentIndex) {
            mMotionLayout?.transitionToState(mPreviousState, mAnimateTargetDelay)
        } else {
            mMotionLayout?.transitionToState(mNextState, mAnimateTargetDelay)
        }
    }

    /**
     * Jump to the given index without any animation
     *
     * @param index index of the element we want to reach
     */
    fun jumpToIndex(index: Int) {
        currentIndex = max(0, min(count - 1, index))
        refresh()
    }

    /**
     * Rebuilds the scene
     */
    fun refresh() {
        val count: Int = mList.size
        for (i in 0 until count) {
            val view: TView = mList.get(i)
            if (mAdapter!!.count() == 0) {
                updateViewVisibility(view, mEmptyViewBehavior)
            } else {
                updateViewVisibility(view, TView.VISIBLE)
            }
        }
        mMotionLayout?.rebuildScene()
        updateItems()
    }

    fun onTransitionChange(
        motionLayout: MotionLayout?,
        startId: String,
        endId: String,
        progress: Float
    ) {
        if (DEBUG) {
            println(
                "onTransitionChange from " + startId
                        + " to " + endId + " progress " + progress
            )
        }
        mLastStartId = startId
    }

    var mLastStartId = ""
    fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: String) {
        mPreviousIndex = currentIndex
        if (currentId == mNextState) {
            currentIndex++
        } else if (currentId == mPreviousState) {
            currentIndex--
        }
        if (mInfiniteCarousel) {
            if (currentIndex >= mAdapter!!.count()) {
                currentIndex = 0
            }
            if (currentIndex < 0) {
                currentIndex = mAdapter!!.count() - 1
            }
        } else {
            if (currentIndex >= mAdapter!!.count()) {
                currentIndex = mAdapter!!.count() - 1
            }
            if (currentIndex < 0) {
                currentIndex = 0
            }
        }
        if (mPreviousIndex != currentIndex) {
            mMotionLayout?.self?.post(mUpdateRunnable)
        }
    }

    private fun enableAllTransitions(enable: Boolean) {
        if(mMotionLayout == null)
            return
        val transitions =
            mMotionLayout!!.definedTransitions ?: mutableListOf()
        for (transition: MotionScene.Transition in transitions) {
            transition.isEnabled = (enable)
        }
    }

    private fun enableTransition(transitionID: String, enable: Boolean): Boolean {
        if (transitionID == "") {
            return false
        }
        if (mMotionLayout == null) {
            return false
        }
        val transition: MotionScene.Transition = mMotionLayout!!.getTransition(transitionID)
            ?: return false
        if (enable == transition.isEnabled) {
            return false
        }
        transition.isEnabled = (enable)
        return true
    }

    var mUpdateRunnable: TRunnable = object : TRunnable {
        override fun run() {
            if(mMotionLayout == null)
                return
            mMotionLayout!!.progress = (0f)
            updateItems()
            mAdapter!!.onNewItem(currentIndex)
            val velocity: Float = mMotionLayout!!.velocity
            if ((mTouchUpMode == TOUCH_UP_CARRY_ON) && (velocity > mVelocityThreshold
                        ) && (currentIndex < mAdapter!!.count() - 1)
            ) {
                val v = velocity * mDampening
                if (currentIndex == 0 && mPreviousIndex > currentIndex) {
                    // don't touch animate when reaching the first item
                    return
                }
                if (currentIndex == mAdapter!!.count() - 1 && mPreviousIndex < currentIndex) {
                    // don't touch animate when reaching the last item
                    return
                }
                mMotionLayout!!.self.post(object : TRunnable {
                    override fun run() {
                        mMotionLayout?.touchAnimateTo(
                            MotionLayout.TOUCH_UP_DECELERATE_AND_COMPLETE,
                            1f, v
                        )
                    }
                })
            }
        }
    }

    override fun onDetachedFromWindow(sup: TView?) {
        super.onDetachedFromWindow(sup)
        mList.clear()
    }

    override fun onAttachedToWindow(sup: TView?) {
        super.onAttachedToWindow(sup)
        var container: MotionLayout? = null
        if (self.getParent()?.getParentType() is MotionLayout) {
            container = self.getParent()?.getParentType() as MotionLayout?
        } else {
            return
        }
        if(mMotionLayout == null || container == null)
            return
        mList.clear()
        for (i in 0 until mCount) {
            val id = mIds.get(i)
            val view: TView? = container!!.getViewById(id)
            if(view == null)
                continue
            if (mFirstViewReference == id) {
                mStartIndex = i
            }
            mList.add(view)
        }
        mMotionLayout = container
        // set up transitions if needed
        if (mTouchUpMode == TOUCH_UP_CARRY_ON) {
            val forward: MotionScene.Transition? = mMotionLayout!!.getTransition(mForwardTransition)
            if (forward != null) {
                forward.setOnTouchUp(MotionLayout.TOUCH_UP_DECELERATE_AND_COMPLETE)
            }
            val backward: MotionScene.Transition? = mMotionLayout!!.getTransition(mBackwardTransition)
            if (backward != null) {
                backward.setOnTouchUp(MotionLayout.TOUCH_UP_DECELERATE_AND_COMPLETE)
            }
        }
        updateItems()
    }

    /**
     * Update the view visibility on the different ConstraintSets
     *
     * @param view
     * @param visibility
     * @return
     */
    private fun updateViewVisibility(view: TView, visibility: Int): Boolean {
        if (mMotionLayout == null) {
            return false
        }
        var needsMotionSceneRebuild = false
        val constraintSets = mMotionLayout!!.constraintSetIds
        if(constraintSets == null)
            return false
        for (i in constraintSets.indices) {
            needsMotionSceneRebuild =
                needsMotionSceneRebuild or updateViewVisibility(constraintSets[i], view, visibility)
        }
        return needsMotionSceneRebuild
    }

    private fun updateViewVisibility(constraintSetId: String, view: TView, visibility: Int): Boolean {
        val constraintSet = mMotionLayout?.getConstraintSet(constraintSetId)
            ?: return false
        val constraint: ConstraintSet.Constraint = constraintSet.getConstraint(view.getId())
            ?: return false
        constraint.propertySet.mVisibilityMode = ConstraintSet.VISIBILITY_MODE_IGNORE
        //        if (constraint.propertySet.visibility == visibility) {
//            return false;
//        }
//        constraint.propertySet.visibility = visibility;
        view.setVisibility(visibility)
        return true
    }

    private fun updateItems() {
        if (mAdapter == null) {
            return
        }
        if (mMotionLayout == null) {
            return
        }
        if (mAdapter!!.count() == 0) {
            return
        }
        if (DEBUG) {
            println("Update items, index: " + currentIndex)
        }
        val viewCount: Int = mList.size
        for (i in 0 until viewCount) {
            // mIndex should map to i == startIndex
            val view: TView = mList.get(i)
            var index = currentIndex + i - mStartIndex
            if (mInfiniteCarousel) {
                if (index < 0) {
                    if (mEmptyViewBehavior != TView.INVISIBLE) {
                        updateViewVisibility(view, mEmptyViewBehavior)
                    } else {
                        updateViewVisibility(view, TView.VISIBLE)
                    }
                    if (index % mAdapter!!.count() == 0) {
                        mAdapter!!.populate(view, 0)
                    } else {
                        mAdapter!!.populate(view, mAdapter!!.count() + (index % mAdapter!!.count()))
                    }
                } else if (index >= mAdapter!!.count()) {
                    if (index == mAdapter!!.count()) {
                        index = 0
                    } else if (index > mAdapter!!.count()) {
                        index = index % mAdapter!!.count()
                    }
                    if (mEmptyViewBehavior != TView.INVISIBLE) {
                        updateViewVisibility(view, mEmptyViewBehavior)
                    } else {
                        updateViewVisibility(view, ConstraintSet.VISIBLE)
                    }
                    mAdapter!!.populate(view, index)
                } else {
                    updateViewVisibility(view, ConstraintSet.VISIBLE)
                    mAdapter!!.populate(view, index)
                }
            } else {
                if (index < 0) {
                    updateViewVisibility(view, mEmptyViewBehavior)
                } else if (index >= mAdapter!!.count()) {
                    updateViewVisibility(view, mEmptyViewBehavior)
                } else {
                    updateViewVisibility(view, ConstraintSet.VISIBLE)
                    mAdapter!!.populate(view, index)
                }
            }
        }
        if (mTargetIndex != -1 && mTargetIndex != currentIndex) {
            mMotionLayout?.self?.post(object : TRunnable {
                override fun run() {
                    mMotionLayout?.setTransitionDuration(mAnimateTargetDelay)
                    if (mTargetIndex < currentIndex) {
                        mMotionLayout?.transitionToState(mPreviousState, mAnimateTargetDelay)
                    } else {
                        mMotionLayout?.transitionToState(mNextState, mAnimateTargetDelay)
                    }
                }
            })
        } else if (mTargetIndex == currentIndex) {
            mTargetIndex = -1
        }
        if (mBackwardTransition == "" || mForwardTransition == "") {
            Log.w(TAG, "No backward or forward transitions defined for Carousel!")
            return
        }
        if (mInfiniteCarousel) {
            return
        }
        val count = mAdapter!!.count()
        if (currentIndex == 0) {
            enableTransition(mBackwardTransition, false)
        } else {
            enableTransition(mBackwardTransition, true)
            mMotionLayout?.setTransition(mBackwardTransition)
        }
        if (currentIndex == count - 1) {
            enableTransition(mForwardTransition, false)
        } else {
            enableTransition(mForwardTransition, true)
            mMotionLayout?.setTransition(mForwardTransition)
        }
    }

    companion object {
        private val DEBUG = false
        private val TAG = "Carousel"
        val TOUCH_UP_IMMEDIATE_STOP = 1
        val TOUCH_UP_CARRY_ON = 2
    }
}