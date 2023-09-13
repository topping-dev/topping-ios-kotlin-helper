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

/**
 * This class is designed to create complex animated single line text in MotionLayout.
 * Its API are designed with animation in mine.
 * for example it uses setTextPanX(float x) where 0 is centered -1 is left +1 is right
 *
 * It supports the following features:
 *
 *  * color outlines
 *  * Textured text
 *  * Blured Textured Text
 *  * Scrolling of Texture in text
 *  * PanX, PanY instead of Gravity
 *
 */
//class MotionLabel(val context: TContext, val attrs: AttributeSet, val self: TView, val selfButton: TButton) : FloatLayout {
//    var mPaint: TextPaint = TextPaint()
//    var mPath: Path? = Path()
//    private var mTextFillColor = 0xFFFF
//    private var mTextOutlineColor = 0xFFFF
//    private var mUseOutline = false
//    private var mRoundPercent = 0f // rounds the corners as a percent
//    private var mRound = Float.NaN // rounds the corners in dp if NaN RoundPercent is in effect
//    var mViewOutlineProvider: ViewOutlineProvider? = null
//    var mRect: RectF? = null
//    private var mTextSize = 48f
//    private var mBaseTextSize = Float.NaN
//    private var mStyleIndex = 0
//    private var mTypefaceIndex = 0
//    private var mTextOutlineThickness = 0f
//    private var mText = "Hello World"
//    var mNotBuilt = true
//    private val mTextBounds: Rect = Rect()
//    private var mPaddingLeft = 1
//    private var mPaddingRight = 1
//    private var mPaddingTop = 1
//    private var mPaddingBottom = 1
//    private var mFontFamily: String? = null
//
//    //    private StaticLayout mStaticLayout;
//    private var mLayout: Layout? = null
//    private var mGravity: Int = Gravity.TOP or Gravity.START
//    private var mAutoSizeTextType: Int = TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
//    private var mAutoSize = false // decided during measure
//    private var mDeltaLeft = 0f
//    private var mFloatWidth = 0f
//    private var mFloatHeight = 0f
//    private var mTextBackground: Drawable? = null
//    var mOutlinePositionMatrix: Matrix33? = null
//    private var mTextBackgroundBitmap: Bitmap? = null
//    private var mTextShader: BitmapShader? = null
//    private var mTextShaderMatrix: Matrix33? = null
//    private var mTextureHeight = Float.NaN
//    private var mTextureWidth = Float.NaN
//    private var mTextPanX = 0f
//    private var mTextPanY = 0f
//    var mPaintCache: TPaint = TPaint()
//    private var mTextureEffect = 0
//
//    constructor(context: TContext) : super(context) {
//        init(context, null)
//    }
//
//    constructor(context: TContext, @Nullable attrs: AttributeSet?) : super(context, attrs) {
//        init(context, attrs)
//    }
//
//    constructor(
//        context: TContext,
//        @Nullable attrs: AttributeSet?,
//        defStyleAttr: Int
//    ) : super(context, attrs, defStyleAttr) {
//        init(context, attrs)
//    }
//
//    private fun init(context: TContext, attrs: AttributeSet?) {
//        setUpTheme(context)
//        if (attrs != null) {
//            val a: TypedArray = getContext()
//                .obtainStyledAttributes(attrs, R.styleable.MotionLabel)
//            val count: Int = a.getIndexCount()
//            for (i in 0 until count) {
//                val attr: Int = a.getIndex(i)
//                if (attr == R.styleable.MotionLabel_android_text) {
//                    setText(a.getText(attr))
//                } else if (attr == R.styleable.MotionLabel_android_fontFamily) {
//                    mFontFamily = a.getString(attr)
//                } else if (attr == R.styleable.MotionLabel_scaleFromTextSize) {
//                    mBaseTextSize = a.getDimensionPixelSize(attr, mBaseTextSize.toInt())
//                } else if (attr == R.styleable.MotionLabel_android_textSize) {
//                    mTextSize = a.getDimensionPixelSize(attr, mTextSize.toInt())
//                } else if (attr == R.styleable.MotionLabel_android_textStyle) {
//                    mStyleIndex = a.getInt(attr, mStyleIndex)
//                } else if (attr == R.styleable.MotionLabel_android_typeface) {
//                    mTypefaceIndex = a.getInt(attr, mTypefaceIndex)
//                } else if (attr == R.styleable.MotionLabel_android_textColor) {
//                    mTextFillColor = a.getColor(attr, mTextFillColor)
//                } else if (attr == R.styleable.MotionLabel_borderRound) {
//                    mRound = a.getDimension(attr, mRound)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        round = mRound
//                    }
//                } else if (attr == R.styleable.MotionLabel_borderRoundPercent) {
//                    mRoundPercent = a.getFloat(attr, mRoundPercent)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        roundPercent = mRoundPercent
//                    }
//                } else if (attr == R.styleable.MotionLabel_android_gravity) {
//                    setGravity(a.getInt(attr, -1))
//                } else if (attr == R.styleable.MotionLabel_android_autoSizeTextType) {
//                    mAutoSizeTextType = a.getInt(attr, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
//                } else if (attr == R.styleable.MotionLabel_textOutlineColor) {
//                    mTextOutlineColor = a.getInt(attr, mTextOutlineColor)
//                    mUseOutline = true
//                } else if (attr == R.styleable.MotionLabel_textOutlineThickness) {
//                    mTextOutlineThickness = a.getDimension(attr, mTextOutlineThickness)
//                    mUseOutline = true
//                } else if (attr == R.styleable.MotionLabel_textBackground) {
//                    mTextBackground = a.getDrawable(attr)
//                    mUseOutline = true
//                } else if (attr == R.styleable.MotionLabel_textBackgroundPanX) {
//                    mBackgroundPanX = a.getFloat(attr, mBackgroundPanX)
//                } else if (attr == R.styleable.MotionLabel_textBackgroundPanY) {
//                    mBackgroundPanY = a.getFloat(attr, mBackgroundPanY)
//                } else if (attr == R.styleable.MotionLabel_textPanX) {
//                    mTextPanX = a.getFloat(attr, mTextPanX)
//                } else if (attr == R.styleable.MotionLabel_textPanY) {
//                    mTextPanY = a.getFloat(attr, mTextPanY)
//                } else if (attr == R.styleable.MotionLabel_textBackgroundRotate) {
//                    mRotate = a.getFloat(attr, mRotate)
//                } else if (attr == R.styleable.MotionLabel_textBackgroundZoom) {
//                    mZoom = a.getFloat(attr, mZoom)
//                } else if (attr == R.styleable.MotionLabel_textureHeight) {
//                    mTextureHeight = a.getDimension(attr, mTextureHeight)
//                } else if (attr == R.styleable.MotionLabel_textureWidth) {
//                    mTextureWidth = a.getDimension(attr, mTextureWidth)
//                } else if (attr == R.styleable.MotionLabel_textureEffect) {
//                    mTextureEffect = a.getInt(attr, mTextureEffect)
//                }
//            }
//            a.recycle()
//        }
//        setupTexture()
//        setupPath()
//    }
//
//    fun blur(bitmapOriginal: Bitmap?, factor: Int): Bitmap {
//        var w: Int = bitmapOriginal.getWidth()
//        var h: Int = bitmapOriginal.getHeight()
//        w /= 2
//        h /= 2
//        var ret: Bitmap = Bitmap.createScaledBitmap(
//            bitmapOriginal,
//            w, h, true
//        )
//        for (i in 0 until factor) {
//            if (w < 32 || h < 32) {
//                break
//            }
//            w /= 2
//            h /= 2
//            ret = Bitmap.createScaledBitmap(ret, w, h, true)
//        }
//        return ret
//    }
//
//    private fun setupTexture() {
//        if (mTextBackground != null) {
//            mTextShaderMatrix = Matrix33.IDENTITY
//            var iw: Int = mTextBackground.getIntrinsicWidth()
//            var ih: Int = mTextBackground.getIntrinsicHeight()
//            if (iw <= 0) {
//                var w: Int = getWidth()
//                if (w == 0) {
//                    w = if (Float.isNaN(mTextureWidth)) 128 else mTextureWidth.toInt()
//                }
//                iw = w
//            }
//            if (ih <= 0) {
//                var h: Int = getHeight()
//                if (h == 0) {
//                    h = if (Float.isNaN(mTextureHeight)) 128 else mTextureHeight.toInt()
//                }
//                ih = h
//            }
//            if (mTextureEffect != 0) {
//                iw /= 2
//                ih /= 2
//            }
//            mTextBackgroundBitmap = Bitmap.createBitmap(iw, ih, Bitmap.Config.ARGB_8888)
//            val canvas = TCanvas(mTextBackgroundBitmap)
//            mTextBackground.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
//            mTextBackground.setFilterBitmap(true)
//            mTextBackground.draw(canvas)
//            if (mTextureEffect != 0) {
//                mTextBackgroundBitmap = blur(mTextBackgroundBitmap, 4)
//            }
//            mTextShader = BitmapShader(
//                mTextBackgroundBitmap,
//                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT
//            )
//        }
//    }
//
//    private fun adjustTexture(l: Float, t: Float, r: Float, b: Float) {
//        if (mTextShaderMatrix == null) {
//            return
//        }
//        mFloatWidth = r - l
//        mFloatHeight = b - t
//        updateShaderMatrix()
//    }
//
//    /**
//     * Sets the horizontal alignment of the text and the
//     * vertical gravity that will be used when there is extra space
//     * in the TextView beyond what is required for the text itself.
//     *
//     * @attr ref android.R.styleable#TextView_gravity
//     * @see android.view.Gravity
//     */
//    @SuppressLint("RtlHardcoded")
//    fun setGravity(gravity: Int) {
//        var gravity = gravity
//        if (gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK === 0) {
//            gravity = gravity or Gravity.START
//        }
//        if (gravity and Gravity.VERTICAL_GRAVITY_MASK === 0) {
//            gravity = gravity or Gravity.TOP
//        }
//        @SuppressWarnings("unused") var newLayout = false
//        if (gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK
//            !== mGravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK
//        ) {
//            newLayout = true
//        }
//        if (gravity != mGravity) {
//            invalidate()
//        }
//        mGravity = gravity
//        mTextPanY = when (mGravity and Gravity.VERTICAL_GRAVITY_MASK) {
//            Gravity.TOP -> -1f
//            Gravity.BOTTOM -> 1f
//            else -> 0f
//        }
//        mTextPanX = when (mGravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
//            Gravity.START, Gravity.LEFT -> -1f
//            Gravity.END, Gravity.RIGHT -> 1f
//            else -> 0f
//        }
//    }
//
//    private val horizontalOffset: Float
//        private get() {
//            val scale = if (Float.isNaN(mBaseTextSize)) 1.0f else mTextSize / mBaseTextSize
//            val textWidth: Float = scale * mPaint.measureText(mText, 0, mText.length())
//            val boxWidth: Float =
//                ((if (Float.isNaN(mFloatWidth)) getMeasuredWidth() else mFloatWidth)
//                        - getPaddingLeft()
//                        - getPaddingRight())
//            return (boxWidth - textWidth) * (1 + mTextPanX) / 2f
//        }
//    private val verticalOffset: Float
//        private get() {
//            val scale = if (Float.isNaN(mBaseTextSize)) 1.0f else mTextSize / mBaseTextSize
//            val fm: TPaint.FontMetrics = mPaint.getFontMetrics()
//            val boxHeight: Float =
//                ((if (Float.isNaN(mFloatHeight)) getMeasuredHeight() else mFloatHeight)
//                        - getPaddingTop()
//                        - getPaddingBottom())
//            val textHeight: Float = scale * (fm.descent - fm.ascent)
//            return (boxHeight - textHeight) * (1 - mTextPanY) / 2 - scale * fm.ascent
//        }
//
//    private fun setUpTheme(context: TContext) {
//        val typedValue = TypedValue()
//        val theme: Theme = context.getTheme()
//        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
//        mPaint.setColor(typedValue.data.also { mTextFillColor = it })
//    }
//
//    /**
//     * set text
//     * @param text
//     */
//    fun setText(text: CharSequence) {
//        mText = text.toString()
//        invalidate()
//    }
//
//    fun setupPath() {
//        mPaddingLeft = getPaddingLeft()
//        mPaddingRight = getPaddingRight()
//        mPaddingTop = getPaddingTop()
//        mPaddingBottom = getPaddingBottom()
//        setTypefaceFromAttrs(mFontFamily, mTypefaceIndex, mStyleIndex)
//        mPaint.setColor(mTextFillColor)
//        mPaint.setStrokeWidth(mTextOutlineThickness)
//        mPaint.setStyle(TPaint.Style.FILL_AND_STROKE)
//        mPaint.setFlags(TPaint.SUBPIXEL_TEXT_FLAG)
//        setTextSize(mTextSize)
//        mPaint.setAntiAlias(true)
//    }
//
//    fun buildShape(scale: Float) {
//        if (!mUseOutline && scale == 1.0f) {
//            return
//        }
//        mPath.reset()
//        val str = mText
//        val len: Int = str.length()
//        mPaint.getTextBounds(str, 0, len, mTextBounds)
//        mPaint.getTextPath(str, 0, len, 0, 0, mPath)
//        if (scale != 1.0f) {
//            Log.v(TAG, Debug.loc + " scale " + scale)
//            val matrix = Matrix33.IDENTITY
//            matrix.postScale(scale, scale)
//            mPath.transform(matrix)
//        }
//        mTextBounds.right--
//        mTextBounds.left++
//        mTextBounds.bottom++
//        mTextBounds.top--
//        val rect = RectF()
//        rect.bottom = getHeight()
//        rect.right = getWidth()
//        mNotBuilt = false
//    }
//
//    var mTempRect: Rect? = null
//    var mTempPaint: TPaint? = null
//    var mPaintTextSize = 0f
//    @Override
//    fun layout(l: Int, t: Int, r: Int, b: Int) {
//        super.layout(l, t, r, b)
//        val normalScale: Boolean = Float.isNaN(mBaseTextSize)
//        var scaleText: Float = if (normalScale) 1 else mTextSize / mBaseTextSize
//        mFloatWidth = (r - l).toFloat()
//        mFloatHeight = (b - t).toFloat()
//        if (mAutoSize) {
//            if (mTempRect == null) {
//                mTempPaint = TPaint()
//                mTempRect = Rect()
//                mTempPaint.set(mPaint)
//                mPaintTextSize = mTempPaint.getTextSize()
//            }
//            mTempPaint.getTextBounds(mText, 0, mText.length(), mTempRect)
//            val tw: Int = mTempRect.width()
//            val th = (1.3f * mTempRect.height()) as Int
//            val vw = mFloatWidth - mPaddingRight - mPaddingLeft
//            val vh = mFloatHeight - mPaddingBottom - mPaddingTop
//            if (normalScale) {
//                if (tw * vh > th * vw) { // width limited tw/vw > th/vh
//                    mPaint.setTextSize(mPaintTextSize * vw / tw)
//                } else { // height limited
//                    mPaint.setTextSize(mPaintTextSize * vh / th)
//                }
//            } else {
//                scaleText = if (tw * vh > th * vw) vw / tw.toFloat() else vh / th.toFloat()
//            }
//        }
//        if (mUseOutline || !normalScale) {
//            adjustTexture(l.toFloat(), t.toFloat(), r.toFloat(), b.toFloat())
//            buildShape(scaleText)
//        }
//    }
//
//    @Override
//    fun layout(l: Float, t: Float, r: Float, b: Float) {
//        mDeltaLeft = l - (0.5f + l).toInt()
//        val w = (0.5f + r).toInt() - (0.5f + l).toInt()
//        val h = (0.5f + b).toInt() - (0.5f + t).toInt()
//        mFloatWidth = r - l
//        mFloatHeight = b - t
//        adjustTexture(l, t, r, b)
//        if (getMeasuredHeight() !== h || getMeasuredWidth() !== w) {
//            val widthMeasureSpec: Int =
//                TView.MeasureSpec.makeMeasureSpec(w, TView.MeasureSpec.EXACTLY)
//            val heightMeasureSpec: Int =
//                TView.MeasureSpec.makeMeasureSpec(h, TView.MeasureSpec.EXACTLY)
//            measure(widthMeasureSpec, heightMeasureSpec)
//            super.layout(
//                (0.5f + l).toInt(),
//                (0.5f + t).toInt(),
//                (0.5f + r).toInt(),
//                (0.5f + b).toInt()
//            )
//        } else {
//            super.layout(
//                (0.5f + l).toInt(),
//                (0.5f + t).toInt(),
//                (0.5f + r).toInt(),
//                (0.5f + b).toInt()
//            )
//        }
//        if (mAutoSize) {
//            if (mTempRect == null) {
//                mTempPaint = TPaint()
//                mTempRect = Rect()
//                mTempPaint.set(mPaint)
//                mPaintTextSize = mTempPaint.getTextSize()
//            }
//            mFloatWidth = r - l
//            mFloatHeight = b - t
//            mTempPaint.getTextBounds(mText, 0, mText.length(), mTempRect)
//            val tw: Int = mTempRect.width()
//            val th: Float = 1.3f * mTempRect.height()
//            val vw = r - l - mPaddingRight - mPaddingLeft
//            val vh = b - t - mPaddingBottom - mPaddingTop
//            if (tw * vh > th * vw) { // width limited tw/vw > th/vh
//                mPaint.setTextSize(mPaintTextSize * vw / tw)
//            } else { // height limited
//                mPaint.setTextSize(mPaintTextSize * vh / th)
//            }
//            if (mUseOutline || !Float.isNaN(mBaseTextSize)) {
//                buildShape(if (Float.isNaN(mBaseTextSize)) 1.0f else mTextSize / mBaseTextSize)
//            }
//        }
//    }
//
//    @Override
//    protected fun onDraw(@NonNull canvas: TCanvas) {
//        val scale = if (Float.isNaN(mBaseTextSize)) 1.0f else mTextSize / mBaseTextSize
//        super.onDraw(canvas)
//        if (!mUseOutline && scale == 1.0f) {
//            val x = mPaddingLeft + horizontalOffset
//            val y = mPaddingTop + verticalOffset
//            canvas.drawText(mText, mDeltaLeft + x, y, mPaint)
//            return
//        }
//        if (mNotBuilt) {
//            buildShape(scale)
//        }
//        if (mOutlinePositionMatrix == null) {
//            mOutlinePositionMatrix = Matrix33.IDENTITY
//        }
//        if (mUseOutline) {
//            mPaintCache.set(mPaint)
//            mOutlinePositionMatrix.reset()
//            val x = mPaddingLeft + horizontalOffset
//            val y = mPaddingTop + verticalOffset
//            mOutlinePositionMatrix.postTranslate(x, y)
//            mOutlinePositionMatrix.preScale(scale, scale)
//            mPath.transform(mOutlinePositionMatrix)
//            if (mTextShader != null) {
//                mPaint.setFilterBitmap(true)
//                mPaint.setShader(mTextShader)
//            } else {
//                mPaint.setColor(mTextFillColor)
//            }
//            mPaint.setStyle(TPaint.Style.FILL)
//            mPaint.setStrokeWidth(mTextOutlineThickness)
//            canvas.drawPath(mPath, mPaint)
//            if (mTextShader != null) {
//                mPaint.setShader(null)
//            }
//            mPaint.setColor(mTextOutlineColor)
//            mPaint.setStyle(TPaint.Style.STROKE)
//            mPaint.setStrokeWidth(mTextOutlineThickness)
//            canvas.drawPath(mPath, mPaint)
//            mOutlinePositionMatrix.reset()
//            mOutlinePositionMatrix.postTranslate(-x, -y)
//            mPath.transform(mOutlinePositionMatrix)
//            mPaint.set(mPaintCache)
//        } else {
//            val x = mPaddingLeft + horizontalOffset
//            val y = mPaddingTop + verticalOffset
//            mOutlinePositionMatrix.reset()
//            mOutlinePositionMatrix.preTranslate(x, y)
//            mPath.transform(mOutlinePositionMatrix)
//            mPaint.setColor(mTextFillColor)
//            mPaint.setStyle(TPaint.Style.FILL_AND_STROKE)
//            mPaint.setStrokeWidth(mTextOutlineThickness)
//            canvas.drawPath(mPath, mPaint)
//            mOutlinePositionMatrix.reset()
//            mOutlinePositionMatrix.preTranslate(-x, -y)
//            mPath.transform(mOutlinePositionMatrix)
//        }
//    }
//
//    /**
//     * Set outline thickness
//     * @param width
//     */
//    fun setTextOutlineThickness(width: Float) {
//        mTextOutlineThickness = width
//        mUseOutline = true
//        if (Float.isNaN(mTextOutlineThickness)) {
//            mTextOutlineThickness = 1f
//            mUseOutline = false
//        }
//        invalidate()
//    }
//
//    /**
//     * Set the color of the text.
//     *
//     * @param color the color of the text
//     */
//    fun setTextFillColor(color: Int) {
//        mTextFillColor = color
//        invalidate()
//    }
//
//    private fun setTypefaceFromAttrs(familyName: String?, typefaceIndex: Int, styleIndex: Int) {
//        var tf: Typeface? = null
//        if (familyName != null) {
//            tf = Typeface.create(familyName, styleIndex)
//            if (tf != null) {
//                typeface = tf
//                return
//            }
//        }
//        when (typefaceIndex) {
//            SANS -> tf = Typeface.SANS_SERIF
//            SERIF -> tf = Typeface.SERIF
//            MONOSPACE -> tf = Typeface.MONOSPACE
//        }
//        if (styleIndex > 0) {
//            tf = if (tf == null) {
//                Typeface.defaultFromStyle(styleIndex)
//            } else {
//                Typeface.create(tf, styleIndex)
//            }
//            typeface = tf
//            // now compute what (if any) algorithmic styling is needed
//            val typefaceStyle = if (tf != null) tf.getStyle() else 0
//            val need = styleIndex and typefaceStyle.inv()
//            mPaint.setFakeBoldText(need and Typeface.BOLD !== 0)
//            mPaint.setTextSkewX(if (need and Typeface.ITALIC !== 0) -0.25f else 0)
//        } else {
//            mPaint.setFakeBoldText(false)
//            mPaint.setTextSkewX(0)
//            typeface = tf
//        }
//    }
//    /**
//     * @return the current typeface and style in which the text is being
//     * displayed.
//     * @see .setTypeface
//     */
//    /**
//     * set the typeface
//     * @param tf
//     */
//    var typeface: Typeface
//        get() = mPaint.getTypeface()
//        set(tf) {
//            if (!Objects.equals(mPaint.getTypeface(), tf)) {
//                mPaint.setTypeface(tf)
//                if (mLayout != null) {
//                    mLayout = null
//                    requestLayout()
//                    invalidate()
//                }
//            }
//        }
//
//    @Override
//    protected fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val widthMode: Int = TView.MeasureSpec.getMode(widthMeasureSpec)
//        val heightMode: Int = TView.MeasureSpec.getMode(heightMeasureSpec)
//        val widthSize: Int = TView.MeasureSpec.getSize(widthMeasureSpec)
//        val heightSize: Int = TView.MeasureSpec.getSize(heightMeasureSpec)
//        var width = widthSize
//        var height = heightSize
//        mAutoSize = false
//        mPaddingLeft = getPaddingLeft()
//        mPaddingRight = getPaddingRight()
//        mPaddingTop = getPaddingTop()
//        mPaddingBottom = getPaddingBottom()
//        if (widthMode != TView.MeasureSpec.EXACTLY || heightMode != TView.MeasureSpec.EXACTLY) {
//            mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds)
//            // WIDTH
//            if (widthMode != TView.MeasureSpec.EXACTLY) {
//                width = (mTextBounds.width() + 0.99999f)
//            }
//            width += mPaddingLeft + mPaddingRight
//            if (heightMode != TView.MeasureSpec.EXACTLY) {
//                val desired = (mPaint.getFontMetricsInt(null) + 0.99999f) as Int
//                height = if (heightMode == TView.MeasureSpec.AT_MOST) {
//                    Math.min(height, desired)
//                } else {
//                    desired
//                }
//                height += mPaddingTop + mPaddingBottom
//            }
//        } else {
//            if (mAutoSizeTextType != TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE) {
//                mAutoSize = true
//            }
//        }
//        setMeasuredDimension(width, height)
//    }
//    //============================= rounding ==============================================
//    /**
//     * Get the fractional corner radius of curvature.
//     *
//     * @return Fractional radius of curvature with respect to smallest size
//     */
//    /**
//     * Set the corner radius of curvature  as a fraction of the smaller side.
//     * For squares 1 will result in a circle
//     *
//     * @param round the radius of curvature as a fraction of the smaller width
//     */
//    @set:RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    var roundPercent: Float
//        get() = mRoundPercent
//        set(round) {
//            val change = mRoundPercent != round
//            mRoundPercent = round
//            if (mRoundPercent != 0.0f) {
//                if (mPath == null) {
//                    mPath = Path()
//                }
//                if (mRect == null) {
//                    mRect = RectF()
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    if (mViewOutlineProvider == null) {
//                        mViewOutlineProvider = object : ViewOutlineProvider() {
//                            @Override
//                            fun getOutline(view: TView?, outline: Outline) {
//                                val w: Int = getWidth()
//                                val h: Int = getHeight()
//                                val r: Float = Math.min(w, h) * mRoundPercent / 2
//                                outline.setRoundRect(0, 0, w, h, r)
//                            }
//                        }
//                        setOutlineProvider(mViewOutlineProvider)
//                    }
//                    setClipToOutline(true)
//                }
//                val w: Int = getWidth()
//                val h: Int = getHeight()
//                val r: Float = Math.min(w, h) * mRoundPercent / 2
//                mRect.set(0, 0, w, h)
//                mPath.reset()
//                mPath.addRoundRect(mRect, r, r, Path.Direction.CW)
//            } else {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    setClipToOutline(false)
//                }
//            }
//            if (change) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    invalidateOutline()
//                }
//            }
//        }
//    /**
//     * Get the corner radius of curvature NaN = RoundPercent in effect.
//     *
//     * @return Radius of curvature
//     */// force eval of roundPercent
//    /**
//     * Set the corner radius of curvature
//     *
//     * @param round the radius of curvature  NaN = default meaning roundPercent in effect
//     */
//    var round: Float
//        get() = mRound
//    //===========================================================================================
//    /**
//     * set text size
//     *
//     * @param size the size of the text
//     * @see TPaint.setTextSize
//     */
//    fun setTextSize(size: Float) {
//        mTextSize = size
//        mPaint.setTextSize(if (Float.isNaN(mBaseTextSize)) size else mBaseTextSize)
//        buildShape(if (Float.isNaN(mBaseTextSize)) 1.0f else mTextSize / mBaseTextSize)
//        requestLayout()
//        invalidate()
//    }
//
//    /**
//     * Sets the color of the text outline.
//     *
//     * @param color the color of the outline of the text
//     */
//    var textOutlineColor: Int
//        get() = mTextOutlineColor
//        set(color) {
//            mTextOutlineColor = color
//            mUseOutline = true
//            invalidate()
//        }
//
//    // ============================ TextureTransformLogic ===============================//
//    var mBackgroundPanX = Float.NaN
//    var mBackgroundPanY = Float.NaN
//    var mZoom = Float.NaN
//    var mRotate = Float.NaN
//
//    /**
//     * Gets the pan from the center
//     * pan of 1 the image is "all the way to the right"
//     * if the images width is greater than the screen width,
//     * pan = 1 results in the left edge lining up
//     * if the images width is less than the screen width,
//     * pan = 1 results in the right edges lining up
//     * if image width == screen width it does nothing
//     *
//     * @return the pan in X. Where 0 is centered = Float. NaN if not set
//     */
//    fun getTextBackgroundPanX(): Float {
//        return mBackgroundPanX
//    }
//
//    /**
//     * gets the pan from the center
//     * pan of 1 the image is "all the way to the bottom"
//     * if the images width is greater than the screen height,
//     * pan = 1 results in the bottom edge lining up
//     * if the images width is less than the screen height,
//     * pan = 1 results in the top edges lining up
//     * if image height == screen height it does nothing
//     *
//     * @return pan in y. Where 0 is centered NaN if not set
//     */
//    fun getTextBackgroundPanY(): Float {
//        return mBackgroundPanY
//    }
//
//    /**
//     * gets the zoom where 1 scales the image just enough to fill the view
//     *
//     * @return the zoom factor
//     */
//    fun getTextBackgroundZoom(): Float {
//        return mZoom
//    }
//
//    /**
//     * gets the rotation
//     *
//     * @return the rotation in degrees
//     */
//    fun getTextBackgroundRotate(): Float {
//        return mRotate
//    }
//
//    /**
//     * sets the pan from the center
//     * pan of 1 the image is "all the way to the right"
//     * if the images width is greater than the screen width,
//     * pan = 1 results in the left edge lining up
//     * if the images width is less than the screen width,
//     * pan = 1 results in the right edges lining up
//     * if image width == screen width it does nothing
//     *
//     * @param pan sets the pan in X. Where 0 is centered
//     */
//    fun setTextBackgroundPanX(pan: Float) {
//        mBackgroundPanX = pan
//        updateShaderMatrix()
//        invalidate()
//    }
//
//    /**
//     * sets the pan from the center
//     * pan of 1 the image is "all the way to the bottom"
//     * if the images width is greater than the screen height,
//     * pan = 1 results in the bottom edge lining up
//     * if the images width is less than the screen height,
//     * pan = 1 results in the top edges lining up
//     * if image height == screen height it does nothing
//     *
//     * @param pan sets the pan in X. Where 0 is centered
//     */
//    fun setTextBackgroundPanY(pan: Float) {
//        mBackgroundPanY = pan
//        updateShaderMatrix()
//        invalidate()
//    }
//
//    /**
//     * sets the zoom where 1 scales the image just enough to fill the view
//     *
//     * @param zoom the zoom factor
//     */
//    fun setTextBackgroundZoom(zoom: Float) {
//        mZoom = zoom
//        updateShaderMatrix()
//        invalidate()
//    }
//
//    /**
//     * sets the rotation angle of the image in degrees
//     *
//     * @param rotation angle in degrees
//     */
//    fun setTextBackgroundRotate(rotation: Float) {
//        mRotate = rotation
//        updateShaderMatrix()
//        invalidate()
//    }
//
//    private fun updateShaderMatrix() {
//        val panX: Float = if (Float.isNaN(mBackgroundPanX)) 0 else mBackgroundPanX
//        val panY: Float = if (Float.isNaN(mBackgroundPanY)) 0 else mBackgroundPanY
//        val zoom: Float = if (Float.isNaN(mZoom)) 1 else mZoom
//        val rota: Float = if (Float.isNaN(mRotate)) 0 else mRotate
//        mTextShaderMatrix.reset()
//        val iw: Float = mTextBackgroundBitmap.getWidth()
//        val ih: Float = mTextBackgroundBitmap.getHeight()
//        val sw = if (Float.isNaN(mTextureWidth)) mFloatWidth else mTextureWidth
//        val sh = if (Float.isNaN(mTextureHeight)) mFloatHeight else mTextureHeight
//        val scale = zoom * if (iw * sh < ih * sw) sw / iw else sh / ih
//        mTextShaderMatrix.postScale(scale, scale)
//        var gapx = sw - scale * iw
//        var gapy = sh - scale * ih
//        if (!Float.isNaN(mTextureHeight)) {
//            gapy = mTextureHeight / 2
//        }
//        if (!Float.isNaN(mTextureWidth)) {
//            gapx = mTextureWidth / 2
//        }
//        val tx = 0.5f * (panX * gapx + sw - scale * iw)
//        val ty = 0.5f * (panY * gapy + sh - scale * ih)
//        mTextShaderMatrix.postTranslate(tx, ty)
//        mTextShaderMatrix.postRotate(rota, sw / 2, sh / 2)
//        mTextShader.setLocalMatrix(mTextShaderMatrix)
//    }
//
//    /**
//     * Pan the Texture in the text in the x axis.
//     *
//     * @return pan of the Text -1 = left 0 = center +1 = right
//     */
//    fun getTextPanX(): Float {
//        return mTextPanX
//    }
//
//    /**
//     * Pan the Texture in the text in the x axis.
//     *
//     * @param textPanX pan of the Text -1 = left 0 = center +1 = right
//     */
//    fun setTextPanX(textPanX: Float) {
//        mTextPanX = textPanX
//        invalidate()
//    }
//
//    /**
//     * Pan the Texture in the text in the y axis.
//     *
//     * @return the pan value 0 being centered in the center of screen.
//     */
//    fun getTextPanY(): Float {
//        return mTextPanY
//    }
//
//    /**
//     * Pan the Texture in the text in the y axis.
//     *
//     * @param textPanY pan of the Text -1 = top 0 = center +1 = bottom
//     */
//    fun setTextPanY(textPanY: Float) {
//        mTextPanY = textPanY
//        invalidate()
//    }
//
//    /**
//     * Pan the Texture in the text in the y axis.
//     *
//     * @return pan of the Text -1 = top 0 = center +1 = bottom
//     */
//    fun getTextureHeight(): Float {
//        return mTextureHeight
//    }
//
//    /**
//     * set the height of the texture. Setting Float.NaN is the default Use the view size.
//     *
//     * @param mTextureHeight the height of the texture
//     */
//    fun setTextureHeight(mTextureHeight: Float) {
//        this.mTextureHeight = mTextureHeight
//        updateShaderMatrix()
//        invalidate()
//    }
//
//    /**
//     * get the width of the texture. Setting Float.NaN is the default Use the view size.
//     *
//     * @return the width of the texture
//     */
//    fun getTextureWidth(): Float {
//        return mTextureWidth
//    }
//
//    /**
//     * set the width of the texture. Setting Float.NaN is the default Use the view size
//     *
//     * @param mTextureWidth set the width of the texture Float.NaN clears setting
//     */
//    fun setTextureWidth(mTextureWidth: Float) {
//        this.mTextureWidth = mTextureWidth
//        updateShaderMatrix()
//        invalidate()
//    }
//
//    /**
//     * if set the font is rendered to polygons at this size and then scaled to the size set by
//     * textSize.
//     *
//     * @return size to pre render font or NaN if not used.
//     */
//    fun getScaleFromTextSize(): Float {
//        return mBaseTextSize
//    }
//
//    /**
//     * if set the font is rendered to polygons at this size and then scaled to the size set by
//     * textSize.
//     * This allows smooth efficient animation of fonts size.
//     *
//     * @param size the size to pre render the font or NaN if not used.
//     */
//    fun setScaleFromTextSize(size: Float) {
//        mBaseTextSize = size
//    }
//
//    companion object {
//        const val TAG = "MotionLabel"
//        private const val SANS = 1
//        private const val SERIF = 2
//        private const val MONOSPACE = 3
//    }
//}