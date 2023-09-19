package dev.topping.ios.constraint

import dev.topping.ios.constraint.TView.Companion.LAYOUT_DIRECTION_LTR
import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.core.state.Interpolator
import dev.topping.ios.constraint.shared.graphics.AndroidMatrix33
import kotlinx.cinterop.*
import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.attributes

class NotFoundException : Exception()

typealias AttributeSet = MutableMap<String, String>

class Xml {
    companion object {
        fun asAttributeSet(parser: XmlBufferedReader) : AttributeSet {
            val set: AttributeSet = mutableMapOf()
            parser.attributes.forEach {
                set[it.localName] = it.value
            }
            return set
        }

        fun getBufferedReader(value: String) : XmlBufferedReader {
            return XmlBufferedReader(XmlStreaming.newReader(value))
        }
    }
}

interface TCanvas {
    fun drawLine(startX: Int, startY: Int, stopX: Int, stopY: Int, paint: TPaint)
    fun drawRect(r: Rect, paint: TPaint)
    fun drawText(text: String, x: Float, y: Float, paint: TPaint)
    fun save()
    fun restore()
}
interface TPaint {
    fun getTextBounds(text: String, start: Int, end: Int, bounds: Rect)
    fun setColor(color: TColor)
    fun setAntiAlias(value: Boolean)
    fun setStrokeWidth(value: Int)
}

expect class TColor {
    companion object {
        fun argb(a: Number, r: Number, g: Number, b: Number) : TColor

        fun HSVToColor(hsv: FloatArray): TColor

        fun HSVToColor(alpha: Double, hsv: FloatArray): TColor

        fun rgbInt(red: Int, green: Int, blue: Int): Int

        /**
         * Return a color-int from alpha, red, green, blue components.
         * These component values should be [0..255], but there is no
         * range check performed, so if they are out of range, the
         * returned color is undefined.
         * @param alpha Alpha component [0..255] of the color
         * @param red   Red component [0..255] of the color
         * @param green Green component [0..255] of the color
         * @param blue  Blue component [0..255] of the color
         */
        fun argbInt(alpha: Int, red: Int, green: Int, blue: Int): Int

        /**
         * Return the alpha component of a color int. This is the same as saying
         * color >>> 24
         */
        fun alpha(color: Int): Int

        /**
         * Return the red component of a color int. This is the same as saying
         * (color >> 16) & 0xFF
         */
        fun red(color: Int): Int

        /**
         * Return the green component of a color int. This is the same as saying
         * (color >> 8) & 0xFF
         */
        fun green(color: Int): Int

        /**
         * Return the blue component of a color int. This is the same as saying
         * color & 0xFF
         */
        fun blue(color: Int): Int

        fun toInt(color: TColor) : Int

        fun fromInt(color: Int) : TColor
        fun parseColor(colorString: String): Int
    }

    constructor(a: Number, r: Number, g: Number, b: Number)
    constructor(alpha: Double, hsv: FloatArray)

    fun getValues(r: Pointer<Int>, g: Pointer<Int>, b: Pointer<Int>, a: Pointer<Int>)
}

interface TLayoutInflater {
    fun inflate(resId: String, parent: TView?): TView
}

open class ViewGroup
{
    open class LayoutParams {
        val FILL_PARENT = -1
        val MATCH_PARENT = -1
        val WRAP_CONTENT = -2

        var width = 0
        var height = 0

        constructor() {
            this.width = WRAP_CONTENT
            this.height = WRAP_CONTENT
        }

        constructor(width: Int, height: Int) {
            this.width = width
            this.height = height
        }

        constructor(source: LayoutParams) {
            width = source.width
            height = source.height
        }

        open fun resolveLayoutDirection(layoutDirection: Int) {}
    }
}

public inline infix fun Byte.or(other: Int): Byte = (this.toInt() or other).toByte()
public inline infix fun Byte.and(other: Int): Byte = (this.toInt() and other).toByte()

open class MarginLayoutParams : ViewGroup.LayoutParams {
    var leftMargin = 0
    var topMargin = 0
    var rightMargin = 0
    var bottomMargin = 0

    val DEFAULT_MARGIN_RELATIVE = Int.MIN_VALUE
    private var startMargin = DEFAULT_MARGIN_RELATIVE
    private var endMargin = DEFAULT_MARGIN_RELATIVE
    var mMarginFlags: Byte = 0
    private val LAYOUT_DIRECTION_MASK = 0x00000003
    private val LEFT_MARGIN_UNDEFINED_MASK = 0x00000004
    private val RIGHT_MARGIN_UNDEFINED_MASK = 0x00000008
    private val RTL_COMPATIBILITY_MODE_MASK = 0x00000010
    private val NEED_RESOLUTION_MASK = 0x00000020
    private val DEFAULT_MARGIN_RESOLVED = 0
    private val UNDEFINED_MARGIN = DEFAULT_MARGIN_RELATIVE

    constructor(width: Int, height: Int) : super(width, height) {
        mMarginFlags = mMarginFlags or LEFT_MARGIN_UNDEFINED_MASK
        mMarginFlags = mMarginFlags or RIGHT_MARGIN_UNDEFINED_MASK
        mMarginFlags = mMarginFlags and NEED_RESOLUTION_MASK.inv()
        mMarginFlags = mMarginFlags and RTL_COMPATIBILITY_MODE_MASK.inv()
    }

    constructor(c: TContext, attrs: AttributeSet) : super() {

        width = c.getResources().getLayoutDimension(attrs["layout_width"] ?: "", WRAP_CONTENT)
        width = c.getResources().getLayoutDimension(attrs["layout_height"] ?: "", WRAP_CONTENT)

        val margin = c.getResources().getDimensionPixelSize(attrs["layout_margin"] ?: "", -1)
        if(margin != -1) {
            leftMargin = margin
            topMargin = margin
            rightMargin= margin
            bottomMargin = margin
        } else {
            val horizontalMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginHorizontal"] ?: "", -1)
            val verticalMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginVertical"] ?: "", -1)

            if(horizontalMargin >= 0) {
                leftMargin = horizontalMargin
                startMargin = horizontalMargin
            }
            else {
                leftMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginLeft"] ?: "", UNDEFINED_MARGIN)
                if (leftMargin == UNDEFINED_MARGIN) {
                    mMarginFlags = mMarginFlags or LEFT_MARGIN_UNDEFINED_MASK
                    leftMargin = DEFAULT_MARGIN_RESOLVED
                }

                rightMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginRight"] ?: "", -1)
                if (rightMargin == UNDEFINED_MARGIN) {
                    mMarginFlags = mMarginFlags or RIGHT_MARGIN_UNDEFINED_MASK
                    rightMargin = DEFAULT_MARGIN_RESOLVED
                }
            }

            startMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginStart"] ?: "", DEFAULT_MARGIN_RELATIVE)
            endMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginEnd"] ?: "", DEFAULT_MARGIN_RELATIVE)

            if (verticalMargin >= 0) {
                topMargin = verticalMargin
                bottomMargin = verticalMargin
            } else {
                topMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginTop"] ?: "", DEFAULT_MARGIN_RESOLVED)
                bottomMargin = c.getResources().getDimensionPixelSize(attrs["layout_marginBottom"] ?: "", DEFAULT_MARGIN_RESOLVED);
            }

            if (isMarginRelative()) {
                mMarginFlags = mMarginFlags or NEED_RESOLUTION_MASK
            }
        }

        // Layout direction is LTR by default
        mMarginFlags = mMarginFlags or LAYOUT_DIRECTION_LTR
    }

    constructor(source: MarginLayoutParams) {
        leftMargin = source.leftMargin
        topMargin = source.topMargin
        rightMargin = source.rightMargin
        bottomMargin = source.bottomMargin
        startMargin = source.startMargin
        endMargin = source.endMargin
        mMarginFlags = source.mMarginFlags
    }

    constructor(source: ViewGroup.LayoutParams) : super(source) {
        mMarginFlags = mMarginFlags or LEFT_MARGIN_UNDEFINED_MASK
        mMarginFlags = mMarginFlags or RIGHT_MARGIN_UNDEFINED_MASK
        mMarginFlags = mMarginFlags and NEED_RESOLUTION_MASK.inv()
        mMarginFlags = mMarginFlags and RTL_COMPATIBILITY_MODE_MASK.inv()
    }

    fun copyMarginsFrom(source: MarginLayoutParams) {
        leftMargin = source.leftMargin
        topMargin = source.topMargin
        rightMargin = source.rightMargin
        bottomMargin = source.bottomMargin
        startMargin = source.startMargin
        endMargin = source.endMargin
        mMarginFlags = source.mMarginFlags
    }

    fun setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        leftMargin = left
        topMargin = top
        rightMargin = right
        bottomMargin = bottom
        mMarginFlags = mMarginFlags and LEFT_MARGIN_UNDEFINED_MASK.inv()
        mMarginFlags = mMarginFlags and RIGHT_MARGIN_UNDEFINED_MASK.inv()
        if (isMarginRelative()) {
            mMarginFlags = mMarginFlags or NEED_RESOLUTION_MASK
        } else {
            mMarginFlags = mMarginFlags and NEED_RESOLUTION_MASK.inv()
        }
    }

    fun setMarginsRelative(start: Int, top: Int, end: Int, bottom: Int) {
        startMargin = start
        topMargin = top
        endMargin = end
        bottomMargin = bottom
        mMarginFlags = mMarginFlags or NEED_RESOLUTION_MASK
    }

    /**
     * Sets the relative start margin. Margin values should be positive.
     *
     * @param start the start margin size
     *
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginStart
     */
    fun setMarginStart(start: Int) {
        startMargin = start
        mMarginFlags = mMarginFlags or NEED_RESOLUTION_MASK
    }

    /**
     * Returns the start margin in pixels.
     *
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginStart
     *
     * @return the start margin in pixels.
     */
    fun getMarginStart(): Int {
        if (startMargin != DEFAULT_MARGIN_RELATIVE) return startMargin
        if ((mMarginFlags and NEED_RESOLUTION_MASK).toInt() == NEED_RESOLUTION_MASK) {
            doResolveMargins()
        }
        return when ((mMarginFlags and LAYOUT_DIRECTION_MASK).toInt()) {
            TView.LAYOUT_DIRECTION_RTL -> rightMargin
            TView.LAYOUT_DIRECTION_LTR -> leftMargin
            else -> leftMargin
        }
    }

    /**
     * Sets the relative end margin. Margin values should be positive.
     *
     * @param end the end margin size
     *
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginEnd
     */
    fun setMarginEnd(end: Int) {
        endMargin = end
        mMarginFlags = mMarginFlags or NEED_RESOLUTION_MASK
    }

    /**
     * Returns the end margin in pixels.
     *
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginEnd
     *
     * @return the end margin in pixels.
     */
    fun getMarginEnd(): Int {
        if (endMargin != DEFAULT_MARGIN_RELATIVE) return endMargin
        if ((mMarginFlags and NEED_RESOLUTION_MASK).toInt() == NEED_RESOLUTION_MASK) {
            doResolveMargins()
        }
        return when ((mMarginFlags and LAYOUT_DIRECTION_MASK).toInt()) {
            TView.LAYOUT_DIRECTION_RTL -> leftMargin
            TView.LAYOUT_DIRECTION_LTR -> rightMargin
            else -> rightMargin
        }
    }

    /**
     * Check if margins are relative.
     *
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginStart
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginEnd
     *
     * @return true if either marginStart or marginEnd has been set.
     */
    fun isMarginRelative(): Boolean {
        return startMargin != DEFAULT_MARGIN_RELATIVE || endMargin != DEFAULT_MARGIN_RELATIVE
    }

    /**
     * Set the layout direction
     * @param layoutDirection the layout direction.
     * Should be either [TView.LAYOUT_DIRECTION_LTR]
     * or [TView.LAYOUT_DIRECTION_RTL].
     */
    fun setLayoutDirection(layoutDirection: Int) {
        if (layoutDirection != TView.LAYOUT_DIRECTION_LTR &&
            layoutDirection != TView.LAYOUT_DIRECTION_RTL
        ) return
        if (layoutDirection != (mMarginFlags and LAYOUT_DIRECTION_MASK).toInt()) {
            mMarginFlags = mMarginFlags and LAYOUT_DIRECTION_MASK.inv()
            mMarginFlags = mMarginFlags or (layoutDirection and LAYOUT_DIRECTION_MASK)
            if (isMarginRelative()) {
                mMarginFlags = mMarginFlags or NEED_RESOLUTION_MASK
            } else {
                mMarginFlags = mMarginFlags and NEED_RESOLUTION_MASK.inv()
            }
        }
    }

    /**
     * Retuns the layout direction. Can be either [TView.LAYOUT_DIRECTION_LTR] or
     * [TView.LAYOUT_DIRECTION_RTL].
     *
     * @return the layout direction.
     */
    fun getLayoutDirection(): Int {
        return (mMarginFlags and LAYOUT_DIRECTION_MASK).toInt()
    }

    /**
     * This will be called by [android.view.View.requestLayout]. Left and Right margins
     * may be overridden depending on layout direction.
     */
    override fun resolveLayoutDirection(layoutDirection: Int) {
        setLayoutDirection(layoutDirection)
        // No relative margin or pre JB-MR1 case or no need to resolve, just dont do anything
        // Will use the left and right margins if no relative margin is defined.
        if (!isMarginRelative() ||
            (mMarginFlags and NEED_RESOLUTION_MASK).toInt() != NEED_RESOLUTION_MASK
        ) return
        // Proceed with resolution
        doResolveMargins()
    }

    private fun doResolveMargins() {
        if ((mMarginFlags and RTL_COMPATIBILITY_MODE_MASK).toInt() == RTL_COMPATIBILITY_MODE_MASK) {
            // if left or right margins are not defined and if we have some start or end margin
            // defined then use those start and end margins.
            if ((mMarginFlags and LEFT_MARGIN_UNDEFINED_MASK).toInt() == LEFT_MARGIN_UNDEFINED_MASK
                && startMargin > DEFAULT_MARGIN_RELATIVE
            ) {
                leftMargin = startMargin
            }
            if ((mMarginFlags and RIGHT_MARGIN_UNDEFINED_MASK).toInt() == RIGHT_MARGIN_UNDEFINED_MASK
                && endMargin > DEFAULT_MARGIN_RELATIVE
            ) {
                rightMargin = endMargin
            }
        } else {
            // We have some relative margins (either the start one or the end one or both). So use
            // them and override what has been defined for left and right margins. If either start
            // or end margin is not defined, just set it to default "0".
            when ((mMarginFlags and LAYOUT_DIRECTION_MASK).toInt()) {
                TView.LAYOUT_DIRECTION_RTL -> {
                    leftMargin =
                        if (endMargin > DEFAULT_MARGIN_RELATIVE) endMargin else DEFAULT_MARGIN_RESOLVED
                    rightMargin =
                        if (startMargin > DEFAULT_MARGIN_RELATIVE) startMargin else DEFAULT_MARGIN_RESOLVED
                }
                TView.LAYOUT_DIRECTION_LTR -> {
                    leftMargin =
                        if (startMargin > DEFAULT_MARGIN_RELATIVE) startMargin else DEFAULT_MARGIN_RESOLVED
                    rightMargin =
                        if (endMargin > DEFAULT_MARGIN_RELATIVE) endMargin else DEFAULT_MARGIN_RESOLVED
                }
                else -> {
                    leftMargin =
                        if (startMargin > DEFAULT_MARGIN_RELATIVE) startMargin else DEFAULT_MARGIN_RESOLVED
                    rightMargin =
                        if (endMargin > DEFAULT_MARGIN_RELATIVE) endMargin else DEFAULT_MARGIN_RESOLVED
                }
            }
        }
        mMarginFlags = mMarginFlags and NEED_RESOLUTION_MASK.inv()
    }

    /**
     * @hide
     */
    fun isLayoutRtl(): Boolean {
        return (mMarginFlags and LAYOUT_DIRECTION_MASK).toInt() == TView.LAYOUT_DIRECTION_RTL
    }
}

interface TRunnable {
    fun run()
}

interface TResources {
    fun getIdentifier(id: String, type: String, packageName: String): String
    fun getResourceEntryName(id: String): String
    fun getResourceType(id: String): Int
    fun getResourceId(id: String, def: String): String
    fun getDisplayMetrics(): TDisplayMetrics
    fun getInt(key: String?, value: String, def: Int): Int
    fun getFloat(key: String?, value: String, def: Float): Float
    fun getDimension(value: String, def: Float): Float
    fun getString(key: String?, value: String): String
    fun getType(value: String): String
    fun getBoolean(value: String, def: Boolean): Boolean
    fun getXml(resourceId: String): XmlBufferedReader
    fun getResourceName(key: String): String
    fun getAnimation(id: String): XmlBufferedReader?
    fun getColor(value: String, def: TColor): TColor
    fun getDrawable(resId: String): TDrawable?
    fun getDimensionPixelOffset(value: String, def: Int): Int
    fun getDimensionPixelSize(value: String, def: Int): Int
    fun getLayoutDimension(attr: String, def: Int): Int
}

interface TDisplay {
    fun getRotation(): Int
}

interface TContext {
    fun getResources(): TResources
    fun getPackageName(): String
    fun getLayoutInflater(): TLayoutInflater
    fun loadInterpolator(id: String): Interpolator?
    fun createView(): TView
    fun createPaint(): TPaint
    fun createLayerDrawable(layers: Array<TDrawable?>): TDrawable
}

interface TClass {
    fun getSimpleName(): String
    fun getName(): String
}

interface TOnTouchListener {
    fun onTouch(view: TView, motionEvent: MotionEvent): Boolean
}

interface TOnScrollChangeListener {
    fun onScrollChange(
        v: TNestedScrollView,
        scrollX: Int,
        scrollY: Int,
        oldScrollX: Int,
        oldScrollY: Int
    ) {
    }
}

interface TView {

    class MeasureSpec
    {
        companion object {
            const val MODE_SHIFT = 30
            const val MODE_MASK = 0x3 shl MODE_SHIFT
            const val UNSPECIFIED = 0 shl MODE_SHIFT
            const val EXACTLY = 1 shl MODE_SHIFT
            const val AT_MOST = 2 shl MODE_SHIFT
            const val MEASURED_STATE_TOO_SMALL = 0x01000000
            const val MEASURED_SIZE_MASK = 0x00ffffff
            const val MEASURED_STATE_MASK = 0xff000000
            const val MEASURED_HEIGHT_STATE_SHIFT = 16

            fun makeMeasureSpec(size: Int, mode: Int): Int {
                return size + mode
            }

            fun getMode(measureSpec: Int) : Int {
                return (measureSpec and MODE_MASK);
            }

            fun getSize(measureSpec: Int) : Int {
                return (measureSpec and MODE_MASK.inv())
            }

            fun toString(measureSpec: Int) : String {
                return measureSpec.toString()
            }
        }
    }

    interface OnClickListener {
        fun onClick(view: TView?)
    }

    companion object {
        const val NO_ID = ""
        const val VISIBLE = 0x00000000
        const val INVISIBLE = 0x00000004
        const val GONE = 0x00000008
        const val LAYOUT_DIRECTION_LTR = 0
        const val LAYOUT_DIRECTION_RTL = 1
        const val FILL_PARENT = -1
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
        const val MEASURED_HEIGHT_STATE_SHIFT = 16
        const val MEASURED_SIZE_MASK = 0x00ffffff
        const val MEASURED_STATE_TOO_SMALL = 0x01000000
    }
    fun setParentType(obj: Any)
    fun getParentType(): Any
    fun getObjCProperty(name: String): Any?
    fun swizzleFunction(funcName: String, block: (TView, Array<Any?>) -> (Any?))
    fun setReflectionValue(methodName: String, value: Any)
    fun setReflectionColorDrawable(methodName: String, r: Int, g: Int, b: Int, a: Int)
    fun setReflectionColor(methodName: String, r: Int, g: Int, b: Int, a: Int)
    fun getLayoutParams(): ViewGroup.LayoutParams?
    fun setElevation(value: Float)
    fun setAlpha(value: Float)
    fun setRotation(value: Float)
    fun setRotationX(value: Float)
    fun setRotationY(value: Float)
    fun setScaleX(value: Float)
    fun setScaleY(value: Float)
    fun setTranslationX(value: Float)
    fun setTranslationY(value: Float)
    fun getTranslationZ(): Float
    fun setTranslationZ(value: Float)
    fun invokeMethod(method: String, value: Any)
    fun setPivotX(value: Float)
    fun setPivotY(value: Float)
    fun getLeft(): Int
    fun getTop(): Int
    fun getRight(): Int
    fun getBottom(): Int
    fun getRotation(): Float
    fun getParent(): TView?
    fun findViewById(id: String): TView?
    fun getChildCount(): Int
    fun getChildAt(index: Int): TView
    fun getId(): String
    fun setId(id: String)
    fun getClass(): TClass
    fun isInEditMode(): Boolean
    fun getContext(): TContext
    fun getResources(): TResources
    fun requestLayout()
    fun forceLayout()
    fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    fun setMeasuredDimension(width: Int, height: Int)
    fun getVisibility(): Int
    fun getElevation(): Float
    fun setVisibility(value: Int)
    fun setTag(value: Any?)
    fun setTag(key: Any, value: Any?)
    fun getTag(): Any?
    fun getTag(key: Any): Any?
    fun isRtl(): Boolean
    fun dpToPixel(dp: Float): Float
    fun invalidate()
    fun getX(): Int
    fun getY(): Int
    fun getWidth(): Int
    fun getHeight(): Int
    fun makeMeasureSpec(measureSpec: Int, type: Int): Int
    fun layout(l: Int, t: Int, r: Int, b: Int)
    fun onAttachedToWindow()
    fun onDetachedFromWindow()
    fun setLayoutParams(params: ViewGroup.LayoutParams)
    fun getViewById(id: String): TView?
    fun getAlpha(): Float
    fun getRotationX(): Float
    fun getRotationY(): Float
    fun getScaleX(): Float
    fun getScaleY(): Float
    fun getPivotX(): Float
    fun getPivotY(): Float
    fun getTranslationX(): Float
    fun getTranslationY(): Float
    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    fun setOnClickListener(listener: OnClickListener?)
    fun canScrollVertically(vert: Int): Boolean
    fun post(runnable: TRunnable)
    fun isAttachedToWindow(): Boolean
    fun getDisplay(): TDisplay
    fun getPaddingTop(): Int
    fun getPaddingRight(): Int
    fun getPaddingBottom(): Int
    fun getPaddingLeft(): Int
    fun getScrollX(): Float
    fun getScrollY(): Float
    fun getMatrix(): AndroidMatrix33
    fun onTouchEvent(event: MotionEvent): Boolean
    fun getLayoutDirection(): Int
    fun onViewAdded(view: TView)
    fun onViewRemoved(view: TView)
    fun getMeasuredWidth(): Int
    fun getMeasuredHeight(): Int
    fun getHitRect(tempRec: Rect)
    fun onDraw(canvas: TCanvas)
    fun setObjCProperty(methodName: String, value: Any)
    fun getBackground(): TDrawable?
    fun getLocationOnScreen(tempLoc: IntArray)
    fun setImageDrawable(drawable: TDrawable?)
    fun setImageResource(resourceId: String)
    fun setOutlineProvider(viewOutlineProvider: ViewOutlineProvider?)
    fun setClipToOutline(clip: Boolean)
    fun invalidateOutline()
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int)
    fun getChildMeasureSpec(spec: Int, padding: Int, dimension: Int): Int
    fun isLayoutRequested(): Boolean
    fun getBaseline(): Int
    fun dispatchDraw(canvas: TCanvas)
    fun getPaddingStart(): Int
    fun getPaddingEnd(): Int
    fun resolveSizeAndState(size: Int, measureSpec: Int, childState: Int): Int
    fun addView(view: TView, param: ViewGroup.LayoutParams)
    fun generateViewId(): String
    fun removeView(view: TView)
}

val MATCH_PARENT = -1
val WRAP_CONTENT = -2

object MeasureSpec {
    private const val MODE_SHIFT = 30
    private const val MODE_MASK = 0x3 shl MODE_SHIFT

    /**
     * Measure specification mode: The parent has not imposed any constraint
     * on the child. It can be whatever size it wants.
     */
    const val UNSPECIFIED = 0 shl MODE_SHIFT

    /**
     * Measure specification mode: The parent has determined an exact size
     * for the child. The child is going to be given those bounds regardless
     * of how big it wants to be.
     */
    const val EXACTLY = 1 shl MODE_SHIFT

    /**
     * Measure specification mode: The child can be as large as it wants up
     * to the specified size.
     */
    const val AT_MOST = 2 shl MODE_SHIFT

    /**
     * Creates a measure specification based on the supplied size and mode.
     *
     * The mode must always be one of the following:
     *
     *  * [android.view.View.MeasureSpec.UNSPECIFIED]
     *  * [android.view.View.MeasureSpec.EXACTLY]
     *  * [android.view.View.MeasureSpec.AT_MOST]
     *
     *
     * @param size the size of the measure specification
     * @param mode the mode of the measure specification
     * @return the measure specification based on size and mode
     */
    fun makeMeasureSpec(size: Int, mode: Int): Int {
        return size + mode
    }

    /**
     * Extracts the mode from the supplied measure specification.
     *
     * @param measureSpec the measure specification to extract the mode from
     * @return [android.view.View.MeasureSpec.UNSPECIFIED],
     * [android.view.View.MeasureSpec.AT_MOST] or
     * [android.view.View.MeasureSpec.EXACTLY]
     */
    fun getMode(measureSpec: Int): Int {
        return measureSpec and MODE_MASK
    }

    /**
     * Extracts the size from the supplied measure specification.
     *
     * @param measureSpec the measure specification to extract the size from
     * @return the size in pixels defined in the supplied measure specification
     */
    fun getSize(measureSpec: Int): Int {
        return measureSpec and MODE_MASK.inv()
    }

    /**
     * Returns a String representation of the specified measure
     * specification.
     *
     * @param measureSpec the measure specification to convert to a String
     * @return a String with the following format: "MeasureSpec: MODE SIZE"
     */
    fun toString(measureSpec: Int): String {
        val mode = getMode(measureSpec)
        val size = getSize(measureSpec)
        val sb = StringBuilder("MeasureSpec: ")
        if (mode == UNSPECIFIED) sb.append("UNSPECIFIED ") else if (mode == EXACTLY) sb.append("EXACTLY ") else if (mode == AT_MOST) sb.append(
            "AT_MOST "
        ) else sb.append(mode).append(" ")
        sb.append(size)
        return sb.toString()
    }
}
