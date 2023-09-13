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

import dev.topping.ios.constraint.Log
import dev.topping.ios.constraint.MotionEvent
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.constraintlayout.widget.SharedValues
import dev.topping.ios.constraint.core.motion.utils.Rect

/**
 * Container for ViewTransitions. It dispatches the run of a ViewTransition.
 * It receives animate calls
 */
class ViewTransitionController(layout: MotionLayout) {
    private val mMotionLayout: MotionLayout
    private val mViewTransitions: ArrayList<ViewTransition> = ArrayList()
    private var mRelatedViews: HashSet<TView>? = null
    private val mTAG = "ViewTransitionController"

    /**
     * Add a ViewTransition
     * @param viewTransition
     */
    fun add(viewTransition: ViewTransition) {
        mViewTransitions.add(viewTransition)
        mRelatedViews = null
        if (viewTransition.stateTransition == ViewTransition.ONSTATE_SHARED_VALUE_SET) {
            listenForSharedVariable(viewTransition, true)
        } else if (viewTransition.stateTransition
            == ViewTransition.ONSTATE_SHARED_VALUE_UNSET
        ) {
            listenForSharedVariable(viewTransition, false)
        }
    }

    fun remove(id: String) {
        var del: ViewTransition? = null
        for (viewTransition in mViewTransitions) {
            if (viewTransition.id == id) {
                del = viewTransition
                break
            }
        }
        if (del != null) {
            mRelatedViews = null
            mViewTransitions.remove(del)
        }
    }

    private fun viewTransition(vt: ViewTransition, vararg view: TView) {
        val currentId = mMotionLayout.currentState
        if (vt.mViewTransitionMode != ViewTransition.VIEWTRANSITIONMODE_NOSTATE) {
            if (currentId == "") {
                Log.w(
                    mTAG, "No support for ViewTransition within transition yet. Currently: "
                            + mMotionLayout.toString()
                )
                return
            }
            val current: ConstraintSet = mMotionLayout.getConstraintSet(currentId) ?: return
            vt.applyTransition(this, mMotionLayout, currentId, current, *view)
        } else {
            vt.applyTransition(this, mMotionLayout, currentId, null, *view)
        }
    }

    fun enableViewTransition(id: String, enable: Boolean) {
        for (viewTransition in mViewTransitions) {
            if (viewTransition.id == id) {
                viewTransition.isEnabled = enable
                break
            }
        }
    }

    fun isViewTransitionEnabled(id: String): Boolean {
        for (viewTransition in mViewTransitions) {
            if (viewTransition.id == id) {
                return viewTransition.isEnabled
            }
        }
        return false
    }

    /**
     * Support call from MotionLayout.viewTransition
     *
     * @param id    the id of a ViewTransition
     * @param views the list of views to transition simultaneously
     */
    fun viewTransition(id: String, vararg views: TView?) {
        var vt: ViewTransition? = null
        val list: ArrayList<TView> = ArrayList()
        for (viewTransition in mViewTransitions) {
            if (viewTransition.id == id) {
                vt = viewTransition
                for (view in views) {
                    if(view == null)
                        continue
                    if (viewTransition.checkTags(view)) {
                        list.add(view)
                    }
                }
                if (!list.isEmpty()) {
                    viewTransition(vt, *list.toTypedArray())
                    list.clear()
                }
            }
        }
        if (vt == null) {
            Log.e(mTAG, " Could not find ViewTransition")
            return
        }
    }

    /**
     * this gets Touch events on the MotionLayout and can fire transitions on down or up
     *
     * @param event
     */
    fun touchEvent(event: MotionEvent) {
        val currentId = mMotionLayout.currentState
        if (currentId == "") {
            return
        }
        if (mRelatedViews == null) {
            mRelatedViews = HashSet()
            for (viewTransition in mViewTransitions) {
                val count: Int = mMotionLayout.self.getChildCount()
                for (i in 0 until count) {
                    val view: TView = mMotionLayout.self.getChildAt(i)
                    if (viewTransition.matchesView(view)) {
                        val id = view.getId()
                        mRelatedViews!!.add(view)
                    }
                }
            }
        }
        val x: Float = event.getX()
        val y: Float = event.getY()
        val rec = Rect()
        val action: Int = event.getAction()
        if (mAnimations != null && !mAnimations!!.isEmpty()) {
            for (animation in mAnimations!!) {
                animation.reactTo(action, x, y)
            }
        }
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_DOWN -> {
                val current: ConstraintSet? = mMotionLayout.getConstraintSet(currentId)
                for (viewTransition in mViewTransitions) {
                    if (viewTransition.supports(action)) {
                        for (view in mRelatedViews!!) {
                            if (!viewTransition.matchesView(view)) {
                                continue
                            }
                            view.getHitRect(rec)
                            if (rec.contains(x.toInt(), y.toInt())) {
                                viewTransition.applyTransition(
                                    this, mMotionLayout,
                                    currentId, current, view
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    var mAnimations: ArrayList<ViewTransition.Animate>? = null
    var mRemoveList: ArrayList<ViewTransition.Animate> = ArrayList()

    init {
        mMotionLayout = layout
    }

    fun addAnimation(animation: ViewTransition.Animate) {
        if (mAnimations == null) {
            mAnimations = ArrayList()
        }
        mAnimations!!.add(animation)
    }

    fun removeAnimation(animation: ViewTransition.Animate) {
        mRemoveList.add(animation)
    }

    /**
     * Called by motionLayout during draw to allow ViewTransitions to asynchronously animate
     */
    fun animate() {
        if (mAnimations == null) {
            return
        }
        for (animation in mAnimations!!) {
            animation.mutate()
        }
        mAnimations!!.removeAll(mRemoveList)
        mRemoveList.clear()
        if (mAnimations!!.isEmpty()) {
            mAnimations = null
        }
    }

    fun invalidate() {
        mMotionLayout.self.invalidate()
    }

    fun applyViewTransition(viewTransitionId: String, motionController: MotionController): Boolean {
        for (viewTransition in mViewTransitions) {
            if (viewTransition.id == viewTransitionId) {
                viewTransition.mKeyFrames!!.addAllFrames(motionController)
                return true
            }
        }
        return false
    }

    private fun listenForSharedVariable(viewTransition: ViewTransition, isSet: Boolean) {
        val listen_for_id = viewTransition.sharedValueID
        val listen_for_value = viewTransition.sharedValue
        ConstraintLayout.sharedValues.addListener(viewTransition.sharedValueID,
            object : SharedValues.SharedValuesListener {
                override fun onNewValue(id: String, value: Int, oldValue: Int) {
                    val current_value = viewTransition.sharedValueCurrent
                    viewTransition.sharedValueCurrent = value
                    if (listen_for_id == id && current_value != value) {
                        if (isSet) {
                            if (listen_for_value == value) {
                                val count: Int = mMotionLayout.self.getChildCount()
                                for (i in 0 until count) {
                                    val view: TView = mMotionLayout.self.getChildAt(i)
                                    if (viewTransition.matchesView(view)) {
                                        val currentId = mMotionLayout.currentState
                                        val current: ConstraintSet? =
                                            mMotionLayout.getConstraintSet(currentId)
                                        viewTransition.applyTransition(
                                            this@ViewTransitionController, mMotionLayout,
                                            currentId, current, view
                                        )
                                    }
                                }
                            }
                        } else { // not set
                            if (listen_for_value != value) {
                                val count: Int = mMotionLayout.self.getChildCount()
                                for (i in 0 until count) {
                                    val view: TView = mMotionLayout.self.getChildAt(i)
                                    if (viewTransition.matchesView(view)) {
                                        val currentId = mMotionLayout.currentState
                                        val current: ConstraintSet? =
                                            mMotionLayout.getConstraintSet(currentId)
                                        viewTransition.applyTransition(
                                            this@ViewTransitionController, mMotionLayout,
                                            currentId, current, view
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }
}