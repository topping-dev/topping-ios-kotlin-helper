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
package dev.topping.ios.constraint.constraintlayout.helper.widget

import dev.topping.ios.constraint.AttributeSet
import dev.topping.ios.constraint.Log
import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.motion.widget.*
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import kotlin.math.max
import kotlin.math.min

/**
 * MotionHelper that automatically inserts keyframes for views moving in a given
 * direction, out of:
 *
 *  * NORTH
 *  * SOUTH
 *  * EAST
 *  * WEST
 *
 *
 * By default, will pick the opposite of the dominant direction (e.g. elements /not/ moving
 * in the dominant direction will have the keyframes inserted).
 */
class MotionEffect(context: TContext, attrs: AttributeSet, self: TView) : MotionHelper(context, attrs, self) {
    private var mMotionEffectAlpha = 0.1f
    private var mMotionEffectStart = 49
    private var mMotionEffectEnd = 50
    private var mMotionEffectTranslationX = 0
    private var mMotionEffectTranslationY = 0
    private var mMotionEffectStrictMove = true
    private var mViewTransitionId = UNSET_ID
    private var mFadeMove = AUTO
    
    init {
        self.setParentType(this)
        val a = context.getResources()
        attrs.forEach { kvp ->
            val attr = kvp.value
            if (kvp.key == "motionEffect_start") {
                mMotionEffectStart = a.getInt(kvp.key, attr, mMotionEffectStart)
                mMotionEffectStart =
                    max(min(mMotionEffectStart, 99), 0)
            } else if (kvp.key == "motionEffect_end") {
                mMotionEffectEnd = a.getInt(kvp.key, attr, mMotionEffectEnd)
                mMotionEffectEnd =
                    max(min(mMotionEffectEnd, 99), 0)
            } else if (kvp.key == "motionEffect_translationX") {
                mMotionEffectTranslationX =
                    a.getDimensionPixelOffset(attr, mMotionEffectTranslationX)
            } else if (kvp.key == "motionEffect_translationY") {
                mMotionEffectTranslationY =
                    a.getDimensionPixelOffset(attr, mMotionEffectTranslationY)
            } else if (kvp.key == "motionEffect_alpha") {
                mMotionEffectAlpha = a.getFloat(kvp.key, attr, mMotionEffectAlpha)
            } else if (kvp.key == "motionEffect_move") {
                mFadeMove = a.getInt(kvp.key, attr, mFadeMove)
            } else if (kvp.key == "motionEffect_strict") {
                mMotionEffectStrictMove = a.getBoolean(attr, mMotionEffectStrictMove)
            } else if (kvp.key == "motionEffect_viewTransition") {
                mViewTransitionId = a.getResourceId(attr, mViewTransitionId)
            }
        }
        if (mMotionEffectStart == mMotionEffectEnd) {
            if (mMotionEffectStart > 0) {
                mMotionEffectStart--
            } else {
                mMotionEffectEnd++
            }
        }
    }

    override val isDecorator: Boolean
        get() = true

    override fun onPreSetup(
        motionLayout: MotionLayout,
        controllerMap: MutableMap<TView, MotionController>
    ) {
        val views = getViews(self.getParent()?.getParentType() as ConstraintLayout?)
        if (views == null) {
            Log.v(TAG, Debug.loc + " views = null")
            return
        }

        // Prepare a set of keyframes to be inserted
        val alpha1 = KeyAttributes()
        val alpha2 = KeyAttributes()
        alpha1.setValue(Key.ALPHA, mMotionEffectAlpha)
        alpha2.setValue(Key.ALPHA, mMotionEffectAlpha)
        alpha1.setFramePosition(mMotionEffectStart)
        alpha2.setFramePosition(mMotionEffectEnd)
        val stick1 = KeyPosition()
        stick1.setFramePosition(mMotionEffectStart)
        stick1.setType(KeyPosition.TYPE_CARTESIAN)
        stick1.setValue(KeyPosition.PERCENT_X, 0)
        stick1.setValue(KeyPosition.PERCENT_Y, 0)
        val stick2 = KeyPosition()
        stick2.setFramePosition(mMotionEffectEnd)
        stick2.setType(KeyPosition.TYPE_CARTESIAN)
        stick2.setValue(KeyPosition.PERCENT_X, 1)
        stick2.setValue(KeyPosition.PERCENT_Y, 1)
        var translationX1: KeyAttributes? = null
        var translationX2: KeyAttributes? = null
        if (mMotionEffectTranslationX > 0) {
            translationX1 = KeyAttributes()
            translationX2 = KeyAttributes()
            translationX1.setValue(Key.TRANSLATION_X, mMotionEffectTranslationX)
            translationX1.setFramePosition(mMotionEffectEnd)
            translationX2.setValue(Key.TRANSLATION_X, 0)
            translationX2.setFramePosition(mMotionEffectEnd - 1)
        }
        var translationY1: KeyAttributes? = null
        var translationY2: KeyAttributes? = null
        if (mMotionEffectTranslationY > 0) {
            translationY1 = KeyAttributes()
            translationY2 = KeyAttributes()
            translationY1.setValue(Key.TRANSLATION_Y, mMotionEffectTranslationY)
            translationY1.setFramePosition(mMotionEffectEnd)
            translationY2.setValue(Key.TRANSLATION_Y, 0)
            translationY2.setFramePosition(mMotionEffectEnd - 1)
        }
        var moveDirection = mFadeMove
        if (mFadeMove == AUTO) {
            val direction = IntArray(4)
            // let's find out the general movement direction for all the referenced views
            for (i in views.indices) {
                val mc: MotionController = controllerMap.get(views[i]) ?: continue
                val x: Float = mc.finalX - mc.startX
                val y: Float = mc.finalY - mc.startY
                // look at the direction for this view, and increment the opposite direction
                // (as that's the one we will use to apply the fade)
                if (y < 0) {
                    direction[SOUTH]++
                }
                if (y > 0) {
                    direction[NORTH]++
                }
                if (x > 0) {
                    direction[WEST]++
                }
                if (x < 0) {
                    direction[EAST]++
                }
            }
            var max = direction[0]
            moveDirection = 0
            for (i in 1..3) {
                if (max < direction[i]) {
                    max = direction[i]
                    moveDirection = i
                }
            }
        }
        for (i in views.indices) {
            val mc: MotionController = controllerMap.get(views[i]) ?: continue
            val x: Float = mc.finalX - mc.startX
            val y: Float = mc.finalY - mc.startY
            var apply = true

            // Any view that is moving in the given direction will have the fade applied
            // if move strict is true, also include views that are moving in diagonal, even
            // if they aren't moving in the opposite direction.
            if (moveDirection == NORTH) {
                if (y > 0 && (!mMotionEffectStrictMove || x == 0f)) {
                    apply = false
                }
            } else if (moveDirection == SOUTH) {
                if (y < 0 && (!mMotionEffectStrictMove || x == 0f)) {
                    apply = false
                }
            } else if (moveDirection == EAST) {
                if (x < 0 && (!mMotionEffectStrictMove || y == 0f)) {
                    apply = false
                }
            } else if (moveDirection == WEST) {
                if (x > 0 && (!mMotionEffectStrictMove || y == 0f)) {
                    apply = false
                }
            }
            if (apply) {
                if (mViewTransitionId == UNSET_ID) {
                    mc.addKey(alpha1)
                    mc.addKey(alpha2)
                    mc.addKey(stick1)
                    mc.addKey(stick2)
                    if (mMotionEffectTranslationX > 0) {
                        mc.addKey(translationX1!!)
                        mc.addKey(translationX2!!)
                    }
                    if (mMotionEffectTranslationY > 0) {
                        mc.addKey(translationY1!!)
                        mc.addKey(translationY2!!)
                    }
                } else {
                    motionLayout.applyViewTransition(mViewTransitionId, mc)
                }
            }
        }
    }

    companion object {
        const val TAG = "FadeMove"
        const val AUTO = -1
        const val NORTH = 0
        const val SOUTH = 1
        const val EAST = 2
        const val WEST = 3
        private const val UNSET = -1
        private const val UNSET_ID = ""
    }
}
