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
package dev.topping.ios.constraint.constraintlayout.utils.widget

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.core.motion.utils.RectF
import dev.topping.ios.constraint.shared.graphics.Outline
import dev.topping.ios.constraint.shared.graphics.Path
import kotlin.math.min

/**
 * A MotionButton is an AppCompatButton that can round its edges. **Added in 2.0**
 *
 *
 * Subclass of AppCompatButton to handle rounding edges dynamically.
 *
 * <h2>MotionButton attributes</h2>
 * <td>round</td>
 * <td>(id) call the TransitionListener with this trigger id</td>
 *
 * <tr>
 * <td>roundPercent</td>
 * <td>Set the corner radius of curvature  as a fraction of the smaller side.
 * For squares 1 will result in a circle</td>
</tr> *
 * <tr>
 * <td>round</td>
 * <td>Set the corner radius of curvature  as a fraction of the smaller side.
 * For squares 1 will result in a circle</td>
</tr> *
 *
 *
 */
class MotionButton(val context: TContext, val attrs: AttributeSet, val self: TView)
{
    private var mRoundPercent = 0f // rounds the corners as a percent
    private var mRound = Float.NaN // rounds the corners in dp if NaN RoundPercent is in effect
    private var mPath: Path? = null
    var mViewOutlineProvider: ViewOutlineProvider? = null
    var mRect: RectF? = null

    init {
        self.setParentType(this)
        self.setPadding(0, 0, 0, 0)
        attrs.forEach { kvp ->
            if (kvp.key == "round") {
                round = context.getResources().getDimension(kvp.value, 0f)
            } else if (kvp.key == "roundPercent") {
                roundPercent = context.getResources().getFloat(kvp.value, 0f)
            }
        }
    }

    /**
     * Get the fractional corner radius of curvature.
     *
     * @return Fractional radius of curvature with respect to smallest size
     */
    /**
     * Set the corner radius of curvature  as a fraction of the smaller side.
     * For squares 1 will result in a circle
     *
     * @param round the radius of curvature as a fraction of the smaller width
     */
    var roundPercent: Float
        get() = mRoundPercent
        set(round) {
            val change = mRoundPercent != round
            mRoundPercent = round
            if (mRoundPercent != 0.0f) {
                if (mPath == null) {
                    mPath = Path()
                }
                if (mRect == null) {
                    mRect = RectF()
                }
                if (mViewOutlineProvider == null) {
                    mViewOutlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: TView, outline: Outline) {
                            val w: Int = self.getWidth()
                            val h: Int = self.getHeight()
                            val r: Float = min(w, h) * mRoundPercent / 2
                            outline.setRoundRect(0, 0, w, h, r)
                        }
                    }
                    self.setOutlineProvider(mViewOutlineProvider)
                }
                self.setClipToOutline(true)
                val w: Int = self.getWidth()
                val h: Int = self.getHeight()
                val r: Float = min(w, h) * mRoundPercent / 2
                mRect!!.set(0, 0, w, h)
                mPath!!.reset()
                mPath!!.addRoundRect(mRect!!, r, r, Path.Direction.CW)
            } else {
                self.setClipToOutline(false)
            }
            if (change) {
                self.invalidateOutline()
            }
        }
    /**
     * Get the corner radius of curvature NaN = RoundPercent in effect.
     *
     * @return Radius of curvature
     */// force eval of roundPercent
    /**
     * Set the corner radius of curvature
     *
     * @param round the radius of curvature  NaN = default meaning roundPercent in effect
     */
    var round: Float
        get() = mRound
        set(round) {
            if (Float.isNaN(round)) {
                mRound = round
                val tmp = mRoundPercent
                mRoundPercent = -1f
                roundPercent = tmp // force eval of roundPercent
                return
            }
            val change = mRound != round
            mRound = round
            if (mRound != 0.0f) {
                if (mPath == null) {
                    mPath = Path()
                }
                if (mRect == null) {
                    mRect = RectF()
                }
                if (mViewOutlineProvider == null) {
                    mViewOutlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: TView, outline: Outline) {
                            val w: Int = self.getWidth()
                            val h: Int = self.getHeight()
                            outline.setRoundRect(0, 0, w, h, mRound)
                        }
                    }
                    self.setOutlineProvider(mViewOutlineProvider)
                }
                self.setClipToOutline(true)
                val w: Int = self.getWidth()
                val h: Int = self.getHeight()
                mRect!!.set(0, 0, w, h)
                mPath!!.reset()
                mPath!!.addRoundRect(mRect!!, mRound, mRound, Path.Direction.CW)
            } else {
                self.setClipToOutline(false)
            }
            if (change) {
                self.invalidateOutline()
            }
        }
}