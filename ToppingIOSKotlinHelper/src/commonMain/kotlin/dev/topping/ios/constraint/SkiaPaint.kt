package dev.topping.ios.constraint

import org.jetbrains.skia.*

abstract class SkiaPaint(
    val skia: Paint = Paint()
) : TPaint {
    private var mAlphaMultiplier = 1.0f
    private var mColor: Int = Color.BLACK

    override fun setColor(color: TColor) {
        mColor = TColor.toInt(color)
    }

    override fun setAntiAlias(value: Boolean) {
        isAntiAlias = value
    }

    override fun setStrokeWidth(value: Int) {
        strokeWidth = value.toFloat()
    }

    var alphaMultiplier: Float
        get() = mAlphaMultiplier
        set(value) {
            val multiplier = value.coerceIn(0f, 1f)
            updateAlpha(multiplier = multiplier)
            mAlphaMultiplier = multiplier
        }

    private fun updateAlpha(alpha: Float = this.alpha, multiplier: Float = this.mAlphaMultiplier) {
        mColor = Color.withA(mColor, (Color.getA(mColor) * multiplier).toInt())
        skia.color = mColor
    }

    override var alpha: Float
        get() = Color.getA(mColor).toFloat()
        set(value) {
            mColor = Color.withA(mColor, value.toInt())
            updateAlpha(alpha = value)
        }

    override var isAntiAlias: Boolean
        get() = skia.isAntiAlias
        set(value) {
            skia.isAntiAlias = value
        }

    override var color: Int
        get() = mColor
        set(color) {
            mColor = color
            skia.color = color
        }

    override var blendMode: BlendMode = BlendMode.SRC_OVER
        set(value) {
            skia.blendMode = value
            field = value
        }

    override var style: PaintMode = PaintMode.FILL
        set(value) {
            skia.mode = value
            field = value
        }

    override var strokeWidth: Float
        get() = skia.strokeWidth
        set(value) {
            skia.strokeWidth = value
        }

    override var strokeCap: PaintStrokeCap = PaintStrokeCap.BUTT
        set(value) {
            skia.strokeCap = value
            field = value
        }

    override var strokeJoin: PaintStrokeJoin = PaintStrokeJoin.ROUND
        set(value) {
            skia.strokeJoin = value
            field = value
        }

    override var strokeMiterLimit: Float = 0f
        set(value) {
            skia.strokeMiter = value
            field = value
        }

    override var shader: Shader? = null
        set(value) {
            skia.shader = value
            field = value
        }

    override var colorFilter: ColorFilter? = null
        set(value) {
            skia.colorFilter = value
            field = value
        }

    override var pathEffect: PathEffect? = null
        set(value) {
            skia.pathEffect = value
            field = value
        }
}