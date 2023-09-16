/*
 * Copyright (C) 2021 The Android Open Source Project
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
import dev.topping.ios.constraint.core.motion.utils.Rect
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * A view that is useful for prototyping layouts. **Added in 2.0**
 *
 *
 * Basic view that can draw a label (by default the view id),
 * along with diagonals. Useful as a temporary mock view while building up a UI.
 *
 */
class MockView(public val context: TContext, public val self: TView) {
    private val mPaintDiagonals: TPaint = context.createPaint()
    private val mPaintText: TPaint = context.createPaint()
    private val mPaintTextBackground: TPaint = context.createPaint()
    private var mDrawDiagonals = true
    private var mDrawLabel = true
    protected var mText: String? = null
    private val mTextBounds: Rect = Rect()
    private var mDiagonalsColor = TColor.argb(255, 0, 0, 0)
    private var mTextColor = TColor.argb(255, 200, 200, 200)
    private var mTextBackgroundColor = TColor.argb(255, 50, 50, 50)
    private var mMargin = 4

    init {
        self.swizzleFunction("onDraw") { sup, params ->
            var args = params as Array<Any>
            onDraw(sup, args[0] as TCanvas)
            0
        }
    }

    private fun init(context: TContext, attrs: AttributeSet) {
        attrs.forEach { kvp ->
            if (kvp.key == "mock_label") {
                mText = context.getResources().getString(kvp.key, kvp.value)
            } else if (kvp.key == "mock_showDiagonals") {
                mDrawDiagonals = context.getResources().getBoolean(kvp.value, mDrawDiagonals)
            } else if (kvp.key == "mock_diagonalsColor") {
                mDiagonalsColor = context.getResources().getColor(kvp.value, mDiagonalsColor)
            } else if (kvp.key == "mock_labelBackgroundColor") {
                mTextBackgroundColor = context.getResources().getColor(kvp.value, mTextBackgroundColor)
            } else if (kvp.key == "mock_labelColor") {
                mTextColor = context.getResources().getColor(kvp.value, mTextColor)
            } else if (kvp.key == "mock_showLabel") {
                mDrawLabel = context.getResources().getBoolean(kvp.value, mDrawLabel)
            }
        }
        if (mText == null) {
            try {
                mText = context.getResources().getResourceEntryName(self.getId())
            } catch (ex: Exception) {
            }
        }
        mPaintDiagonals.setColor(mDiagonalsColor)
        mPaintDiagonals.setAntiAlias(true)
        mPaintText.setColor(mTextColor)
        mPaintText.setAntiAlias(true)
        mPaintTextBackground.setColor(mTextBackgroundColor)
        mMargin = round(
            mMargin * (context.getResources().getDisplayMetrics().xdpi / context.getResources().getDisplayMetrics().density)
        ).roundToInt()
    }

    fun onDraw(sup: TView?, canvas: TCanvas) {
        sup?.onDraw(canvas)
        var w: Int = self.getWidth()
        var h: Int =self.getHeight()
        if (mDrawDiagonals) {
            w--
            h--
            canvas.drawLine(0, 0, w, h, mPaintDiagonals)
            canvas.drawLine(0, h, w, 0, mPaintDiagonals)
            canvas.drawLine(0, 0, w, 0, mPaintDiagonals)
            canvas.drawLine(w, 0, w, h, mPaintDiagonals)
            canvas.drawLine(w, h, 0, h, mPaintDiagonals)
            canvas.drawLine(0, h, 0, 0, mPaintDiagonals)
        }
        if (mText != null && mDrawLabel) {
            mPaintText.getTextBounds(mText!!, 0, mText!!.length, mTextBounds)
            val tx: Float = (w - mTextBounds.width()) / 2f
            val ty: Float = (h - mTextBounds.height()) / 2f + mTextBounds.height()
            mTextBounds.offset(tx.toInt(), ty.toInt())
            mTextBounds.set(
                mTextBounds.left - mMargin, mTextBounds.top - mMargin,
                mTextBounds.right + mMargin, mTextBounds.bottom + mMargin
            )
            canvas.drawRect(mTextBounds, mPaintTextBackground)
            canvas.drawText(mText!!, tx, ty, mPaintText)
        }
    }
}