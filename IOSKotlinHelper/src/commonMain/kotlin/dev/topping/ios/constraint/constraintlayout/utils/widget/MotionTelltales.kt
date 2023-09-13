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
package dev.topping.ios.constraint.constraintlayout.utils.widget

/**
 * A view that is useful for prototyping Views that will move in MotionLayout. **Added in 2.0**
 *
 *
 * This view works with MotionLayout to demonstrate the motion of 25 points on the view.
 * It is based on MockView which draws a label (by default the view id),
 * along with diagonals.
 *
 * Useful as a deeper understanding of the motion of a view in a MotionLayout
 *
 *
 */
//class MotionTelltales : MockView {
//    private val mPaintTelltales: TPaint = TPaint()
//    var mMotionLayout: MotionLayout? = null
//    var mVelocity = FloatArray(2)
//    var mInvertMatrix: Matrix = Matrix()
//    var mVelocityMode: Int = MotionLayout.VELOCITY_POST_LAYOUT
//    var mTailColor: Int = TColor.MAGENTA
//    var mTailScale = 0.25f
//
//    constructor(context: TContext) : super(context) {
//        init(context, null)
//    }
//
//    constructor(context: TContext, attrs: AttributeSet?) : super(context, attrs) {
//        init(context, attrs)
//    }
//
//    constructor(context: TContext, attrs: AttributeSet?, defStyleAttr: Int) : super(
//        context,
//        attrs,
//        defStyleAttr
//    ) {
//        init(context, attrs)
//    }
//
//    private fun init(context: TContext, attrs: AttributeSet?) {
//        if (attrs != null) {
//            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MotionTelltales)
//            val count: Int = a.getIndexCount()
//            for (i in 0 until count) {
//                val attr: Int = a.getIndex(i)
//                if (attr == R.styleable.MotionTelltales_telltales_tailColor) {
//                    mTailColor = a.getColor(attr, mTailColor)
//                } else if (attr == R.styleable.MotionTelltales_telltales_velocityMode) {
//                    mVelocityMode = a.getInt(attr, mVelocityMode)
//                } else if (attr == R.styleable.MotionTelltales_telltales_tailScale) {
//                    mTailScale = a.getFloat(attr, mTailScale)
//                }
//            }
//            a.recycle()
//        }
//        mPaintTelltales.setColor(mTailColor)
//        mPaintTelltales.setStrokeWidth(5)
//    }
//
//    @Override
//    protected fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//    }
//
//    /**
//     * set the text
//     * @param text
//     */
//    fun setText(text: CharSequence) {
//        mText = text.toString()
//        requestLayout()
//    }
//
//    @Override
//    protected fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//        postInvalidate()
//    }
//
//    @Override
//    override fun onDraw(@NonNull canvas: TCanvas) {
//        super.onDraw(canvas)
//        val matrix: Matrix = getMatrix()
//        matrix.invert(mInvertMatrix)
//        if (mMotionLayout == null) {
//            val vp: ViewParent = getParent()
//            if (vp is MotionLayout) {
//                mMotionLayout = vp as MotionLayout
//            }
//            return
//        }
//        val width: Int = getWidth()
//        val height: Int = getHeight()
//        val f = floatArrayOf(0.1f, 0.25f, 0.5f, 0.75f, 0.9f)
//        for (y in f.indices) {
//            val py = f[y]
//            for (x in f.indices) {
//                val px = f[x]
//                mMotionLayout.getViewVelocity(this, px, py, mVelocity, mVelocityMode)
//                mInvertMatrix.mapVectors(mVelocity)
//                val sx = width * px
//                val sy = height * py
//                val ex = sx - mVelocity[0] * mTailScale
//                val ey = sy - mVelocity[1] * mTailScale
//                mInvertMatrix.mapVectors(mVelocity)
//                canvas.drawLine(sx, sy, ex, ey, mPaintTelltales)
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "MotionTelltales"
//    }
//}