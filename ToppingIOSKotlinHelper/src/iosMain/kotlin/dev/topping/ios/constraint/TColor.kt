package dev.topping.ios.constraint

import kotlinx.cinterop.*
import platform.UIKit.UIColor

actual class TColor {
    var colorNS: UIColor = UIColor.whiteColor
    var color: Int = 0

    actual companion object {
        actual fun argb(a: Number, r: Number, g: Number, b: Number) : TColor {
            return TColor(a, r, g, b)
        }

        actual fun HSVToColor(hsv: FloatArray): TColor {
            return HSVToColor(1.0, hsv)
        }

        actual fun HSVToColor(alpha: Double, hsv: FloatArray): TColor {
            return TColor(alpha, hsv)
        }

        actual fun rgbInt(red: Int, green: Int, blue: Int): Int {
            return 0xFF shl 24 or (red shl 16) or (green shl 8) or blue
        }

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
        actual fun argbInt(alpha: Int, red: Int, green: Int, blue: Int): Int {
            return alpha shl 24 or (red shl 16) or (green shl 8) or blue
        }

        /**
         * Return the alpha component of a color int. This is the same as saying
         * color >>> 24
         */
        actual fun alpha(color: Int): Int {
            return color ushr 24
        }

        /**
         * Return the red component of a color int. This is the same as saying
         * (color >> 16) & 0xFF
         */
        actual fun red(color: Int): Int {
            return color shr 16 and 0xFF
        }

        /**
         * Return the green component of a color int. This is the same as saying
         * (color >> 8) & 0xFF
         */
        actual fun green(color: Int): Int {
            return color shr 8 and 0xFF
        }

        /**
         * Return the blue component of a color int. This is the same as saying
         * color & 0xFF
         */
        actual fun blue(color: Int): Int {
            return color and 0xFF
        }

        actual fun toInt(color: TColor) : Int {
            return color.color
        }

        actual fun fromInt(color: Int) : TColor {
            return TColor(color)
        }

        actual fun parseColor(colorString: String): Int {
            if (colorString[0] == '#') {
                // Use a long to avoid rollovers on #ffXXXXXX
                var color = colorString.substring(1).toLong(16)
                if (colorString.length == 7) {
                    // Set the alpha value
                    color = color or -0x1000000
                } else if (colorString.length != 9) {
                    throw IllegalArgumentException("Unknown color")
                }
                return color.toInt()
            }
            throw IllegalArgumentException("Unknown color")
        }

        fun toIntInternal(color: UIColor) : Int {
            memScoped {
                val a = alloc<DoubleVar>()
                val r = alloc<DoubleVar>()
                val g = alloc<DoubleVar>()
                val b = alloc<DoubleVar>()
                color.getRed(r.ptr, g.ptr, b.ptr, a.ptr)
                return argbInt(
                    (a.value * 255).toInt(), (r.value * 255).toInt(),
                    (g.value * 255).toInt(), (b.value * 255).toInt()
                )
            }
        }
    }

    constructor(color: Int) {
        colorNS = UIColor.colorWithRed(red(color).toDouble() / 255.0, green(color).toDouble() / 255.0, blue(color).toDouble() / 255.0, alpha(color).toDouble() / 255.0)
        this.color = color
    }

    actual constructor(a: Number, r: Number, g: Number, b: Number) {
        colorNS = argb(a, r, g, b)
        color = toIntInternal(colorNS)
    }

    actual constructor(alpha: Double, hsv: FloatArray) {
        colorNS = HSVToColor(alpha, hsv)
        color = toIntInternal(colorNS)
    }

    fun argb(a: Number, r: Number, g: Number, b: Number) =
        UIColor.colorWithRed(r.toDouble() / 255.0, g.toDouble() / 255.0, b.toDouble() / 255.0, a.toDouble() / 255.0)

    fun HSVToColor(alpha: Double, hsv: FloatArray): UIColor {
        return UIColor.colorWithHue(hsv[0].toDouble() / 255.0, hsv[1].toDouble() / 255.0, hsv[2].toDouble() / 255.0, alpha / 255.0)
    }

    actual fun getValues(r: Pointer<Int>, g: Pointer<Int>, b: Pointer<Int>, a: Pointer<Int>) {
        memScoped {
            val aP = alloc<DoubleVar>()
            val rP = alloc<DoubleVar>()
            val gP = alloc<DoubleVar>()
            val bP = alloc<DoubleVar>()
            colorNS.getRed(rP.ptr, gP.ptr, bP.ptr, aP.ptr)
            r.v = (rP.value * 255f).toInt()
            g.v = (gP.value * 255f).toInt()
            b.v = (bP.value * 255f).toInt()
            a.v = (aP.value * 255f).toInt()
        }
    }
}