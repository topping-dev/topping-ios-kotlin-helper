package dev.topping.ios.constraint

import kotlin.math.abs
import kotlin.math.round


class TypedValue {
    /** @hide
     */
    annotation class ComplexDimensionUnit
    /* ------------------------------------------------------------ */
    /** The type held by this value, as defined by the constants here.
     * This tells you how to interpret the other fields in the object.  */
    var type = 0

    /** If the value holds a string, this is it.  */
    var string: CharSequence? = null

    /** Basic data in the value, interpreted according to [.type]  */
    var data = 0

    /** Additional information about where the value came from; only
     * set for strings.  */
    var assetCookie = 0

    /** If Value came from a resource, this holds the corresponding resource id.  */
    var resourceId = 0

    /**
     * If the value came from a resource, these are the configurations for
     * which its contents can change.
     *
     *
     * For example, if a resource has a value defined for the -land resource qualifier,
     * this field will have the [android.content.pm.ActivityInfo.CONFIG_ORIENTATION] bit set.
     *
     *
     * @see android.content.pm.ActivityInfo.CONFIG_MCC
     *
     * @see android.content.pm.ActivityInfo.CONFIG_MNC
     *
     * @see android.content.pm.ActivityInfo.CONFIG_LOCALE
     *
     * @see android.content.pm.ActivityInfo.CONFIG_TOUCHSCREEN
     *
     * @see android.content.pm.ActivityInfo.CONFIG_KEYBOARD
     *
     * @see android.content.pm.ActivityInfo.CONFIG_KEYBOARD_HIDDEN
     *
     * @see android.content.pm.ActivityInfo.CONFIG_NAVIGATION
     *
     * @see android.content.pm.ActivityInfo.CONFIG_ORIENTATION
     *
     * @see android.content.pm.ActivityInfo.CONFIG_SCREEN_LAYOUT
     *
     * @see android.content.pm.ActivityInfo.CONFIG_UI_MODE
     *
     * @see android.content.pm.ActivityInfo.CONFIG_SCREEN_SIZE
     *
     * @see android.content.pm.ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE
     *
     * @see android.content.pm.ActivityInfo.CONFIG_DENSITY
     *
     * @see android.content.pm.ActivityInfo.CONFIG_LAYOUT_DIRECTION
     *
     * @see android.content.pm.ActivityInfo.CONFIG_COLOR_MODE
     */
    var changingConfigurations = -1

    /**
     * If the Value came from a resource, this holds the corresponding pixel density.
     */
    var density = 0

    /**
     * If the Value came from a style resource or a layout resource (set in an XML layout), this
     * holds the corresponding style or layout resource id against which the attribute was resolved.
     */
    var sourceResourceId = 0
    /* ------------------------------------------------------------ */
    /** Return the data for this value as a float.  Only use for values
     * whose type is [.TYPE_FLOAT].  */
    val float: Float
        get() = Float.fromBits(data)

    /**
     * Determine if a value is a color.
     *
     * This works by comparing [.type] to [.TYPE_FIRST_COLOR_INT]
     * and [.TYPE_LAST_COLOR_INT].
     *
     * @return true if this value is a color
     */
    val isColorType: Boolean
        get() = type >= Companion.TYPE_FIRST_COLOR_INT && type <= Companion.TYPE_LAST_COLOR_INT

    /**
     * Return the complex unit type for this value. For example, a dimen type
     * with value 12sp will return [.COMPLEX_UNIT_SP]. Only use for values
     * whose type is [.TYPE_DIMENSION].
     *
     * @return The complex unit type.
     */
    val complexUnit: Int
        get() = Companion.COMPLEX_UNIT_MASK and (data shr Companion.COMPLEX_UNIT_SHIFT)

    /**
     * Return the data for this value as a dimension.  Only use for values
     * whose type is [.TYPE_DIMENSION].
     *
     * @param metrics Current display metrics to use in the conversion --
     * supplies display density and scaling information.
     *
     * @return The complex floating point value multiplied by the appropriate
     * metrics depending on its unit.
     */
    fun getDimension(metrics: TDisplayMetrics): Float {
        return Companion.complexToDimension(data, metrics)
    }

    /**
     * Return the data for this value as a fraction.  Only use for values whose
     * type is [.TYPE_FRACTION].
     *
     * @param base The base value of this fraction.  In other words, a
     * standard fraction is multiplied by this value.
     * @param pbase The parent base value of this fraction.  In other
     * words, a parent fraction (nn%Pointer) is multiplied by this
     * value.
     *
     * @return The complex floating point value multiplied by the appropriate
     * base value depending on its unit.
     */
    fun getFraction(base: Float, pbase: Float): Float {
        return Companion.complexToFraction(data, base, pbase)
    }

    /**
     * Regardless of the actual type of the value, try to convert it to a
     * string value.  For example, a color type will be converted to a
     * string of the form #aarrggbb.
     *
     * @return CharSequence The coerced string value.  If the value is
     * null or the type is not known, null is returned.
     */
    fun coerceToString(): CharSequence? {
        val t = type
        return if (t == Companion.TYPE_STRING) {
            string
        } else Companion.coerceToString(t, data)
    }

    fun setTo(other: TypedValue) {
        type = other.type
        string = other.string
        data = other.data
        assetCookie = other.assetCookie
        resourceId = other.resourceId
        density = other.density
    }

    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("TypedValue{t=0x").append(Int.toHexString(type))
        sb.append("/d=0x").append(Int.toHexString(data))
        if (type == Companion.TYPE_STRING) {
            sb.append(" \"").append(if (string != null) string else "<null>").append("\"")
        }
        if (assetCookie != 0) {
            sb.append(" a=").append(assetCookie)
        }
        if (resourceId != 0) {
            sb.append(" r=0x").append(Int.toHexString(resourceId))
        }
        sb.append("}")
        return sb.toString()
    }

    companion object {
        /** The value contains no data.  */
        const val TYPE_NULL = 0x00

        /** The <var>data</var> field holds a resource identifier.  */
        const val TYPE_REFERENCE = 0x01

        /** The <var>data</var> field holds an attribute resource
         * identifier (referencing an attribute in the current theme
         * style, not a resource entry).  */
        const val TYPE_ATTRIBUTE = 0x02

        /** The <var>string</var> field holds string data.  In addition, if
         * <var>data</var> is non-zero then it is the string block
         * index of the string and <var>assetCookie</var> is the set of
         * assets the string came from.  */
        const val TYPE_STRING = 0x03

        /** The <var>data</var> field holds an IEEE 754 floating point number.  */
        const val TYPE_FLOAT = 0x04

        /** The <var>data</var> field holds a complex number encoding a
         * dimension value.  */
        const val TYPE_DIMENSION = 0x05

        /** The <var>data</var> field holds a complex number encoding a fraction
         * of a container.  */
        const val TYPE_FRACTION = 0x06

        /** Identifies the start of plain integer values.  Any type value
         * from this to [.TYPE_LAST_INT] means the
         * <var>data</var> field holds a generic integer value.  */
        const val TYPE_FIRST_INT = 0x10

        /** The <var>data</var> field holds a number that was
         * originally specified in decimal.  */
        const val TYPE_INT_DEC = 0x10

        /** The <var>data</var> field holds a number that was
         * originally specified in hexadecimal (0xn).  */
        const val TYPE_INT_HEX = 0x11

        /** The <var>data</var> field holds 0 or 1 that was originally
         * specified as "false" or "true".  */
        const val TYPE_INT_BOOLEAN = 0x12

        const val TYPE_LAYOUT = 0x13

        const val TYPE_XML = 0x14

        /** Identifies the start of integer values that were specified as
         * color constants (starting with '#').  */
        const val TYPE_FIRST_COLOR_INT = 0x1c

        /** The <var>data</var> field holds a color that was originally
         * specified as #aarrggbb.  */
        const val TYPE_INT_COLOR_ARGB8 = 0x1c

        /** The <var>data</var> field holds a color that was originally
         * specified as #rrggbb.  */
        const val TYPE_INT_COLOR_RGB8 = 0x1d

        /** The <var>data</var> field holds a color that was originally
         * specified as #argb.  */
        const val TYPE_INT_COLOR_ARGB4 = 0x1e

        /** The <var>data</var> field holds a color that was originally
         * specified as #rgb.  */
        const val TYPE_INT_COLOR_RGB4 = 0x1f

        /** Identifies the end of integer values that were specified as color
         * constants.  */
        const val TYPE_LAST_COLOR_INT = 0x1f

        /** Identifies the end of plain integer values.  */
        const val TYPE_LAST_INT = 0x1f
        /* ------------------------------------------------------------ */
        /** Complex data: bit location of unit information.  */
        const val COMPLEX_UNIT_SHIFT = 0

        /** Complex data: mask to extract unit information (after shifting by
         * [.COMPLEX_UNIT_SHIFT]). This gives us 16 possible types, as
         * defined below.  */
        const val COMPLEX_UNIT_MASK = 0xf

        /** [.TYPE_DIMENSION] complex unit: Value is raw pixels.  */
        const val COMPLEX_UNIT_PX = 0

        /** [.TYPE_DIMENSION] complex unit: Value is Device Independent
         * Pixels.  */
        const val COMPLEX_UNIT_DIP = 1

        /** [.TYPE_DIMENSION] complex unit: Value is a scaled pixel.  */
        const val COMPLEX_UNIT_SP = 2

        /** [.TYPE_DIMENSION] complex unit: Value is in points.  */
        const val COMPLEX_UNIT_PT = 3

        /** [.TYPE_DIMENSION] complex unit: Value is in inches.  */
        const val COMPLEX_UNIT_IN = 4

        /** [.TYPE_DIMENSION] complex unit: Value is in millimeters.  */
        const val COMPLEX_UNIT_MM = 5

        /** [.TYPE_FRACTION] complex unit: A basic fraction of the overall
         * size.  */
        const val COMPLEX_UNIT_FRACTION = 0

        /** [.TYPE_FRACTION] complex unit: A fraction of the parent size.  */
        const val COMPLEX_UNIT_FRACTION_PARENT = 1

        /** Complex data: where the radix information is, telling where the decimal
         * place appears in the mantissa.  */
        const val COMPLEX_RADIX_SHIFT = 4

        /** Complex data: mask to extract radix information (after shifting by
         * [.COMPLEX_RADIX_SHIFT]). This give us 4 possible fixed point
         * representations as defined below.  */
        const val COMPLEX_RADIX_MASK = 0x3

        /** Complex data: the mantissa is an integral number -- i.e., 0xnnnnnn.0  */
        const val COMPLEX_RADIX_23p0 = 0

        /** Complex data: the mantissa magnitude is 16 bits -- i.e, 0xnnnn.nn  */
        const val COMPLEX_RADIX_16p7 = 1

        /** Complex data: the mantissa magnitude is 8 bits -- i.e, 0xnn.nnnn  */
        const val COMPLEX_RADIX_8p15 = 2

        /** Complex data: the mantissa magnitude is 0 bits -- i.e, 0x0.nnnnnn  */
        const val COMPLEX_RADIX_0p23 = 3

        /** Complex data: bit location of mantissa information.  */
        const val COMPLEX_MANTISSA_SHIFT = 8

        /** Complex data: mask to extract mantissa information (after shifting by
         * [.COMPLEX_MANTISSA_SHIFT]). This gives us 23 bits of precision;
         * the top bit is the sign.  */
        const val COMPLEX_MANTISSA_MASK = 0xffffff
        /* ------------------------------------------------------------ */
        /**
         * [.TYPE_NULL] data indicating the value was not specified.
         */
        const val DATA_NULL_UNDEFINED = 0

        /**
         * [.TYPE_NULL] data indicating the value was explicitly set to null.
         */
        const val DATA_NULL_EMPTY = 1
        /* ------------------------------------------------------------ */
        /**
         * If [.density] is equal to this value, then the density should be
         * treated as the system's default density value: [TDisplayMetrics.DENSITY_DEFAULT].
         */
        const val DENSITY_DEFAULT = 0

        /**
         * If [.density] is equal to this value, then there is no density
         * associated with the resource and it should not be scaled.
         */
        const val DENSITY_NONE = 0xffff
        private val MANTISSA_MULT = 1.0f / (1 shl Companion.COMPLEX_MANTISSA_SHIFT)
        private val RADIX_MULTS = floatArrayOf(
            1.0f * Companion.MANTISSA_MULT, 1.0f / (1 shl 7) * Companion.MANTISSA_MULT,
            1.0f / (1 shl 15) * Companion.MANTISSA_MULT, 1.0f / (1 shl 23) * Companion.MANTISSA_MULT
        )

        /**
         * Retrieve the base value from a complex data integer.  This uses the
         * [.COMPLEX_MANTISSA_MASK] and [.COMPLEX_RADIX_MASK] fields of
         * the data to compute a floating point representation of the number they
         * describe.  The units are ignored.
         *
         * @param complex A complex data value.
         *
         * @return A floating point value corresponding to the complex data.
         */
        fun complexToFloat(complex: Int): Float {
            return ((complex and (Companion.COMPLEX_MANTISSA_MASK
                    shl Companion.COMPLEX_MANTISSA_SHIFT))
                    * Companion.RADIX_MULTS.get(
                complex shr Companion.COMPLEX_RADIX_SHIFT
                        and Companion.COMPLEX_RADIX_MASK
            ))
        }

        /**
         * Converts a complex data value holding a dimension to its final floating
         * point value. The given <var>data</var> must be structured as a
         * [.TYPE_DIMENSION].
         *
         * @param data A complex data value holding a unit, magnitude, and
         * mantissa.
         * @param metrics Current display metrics to use in the conversion --
         * supplies display density and scaling information.
         *
         * @return The complex floating point value multiplied by the appropriate
         * metrics depending on its unit.
         */
        fun complexToDimension(data: Int, metrics: TDisplayMetrics): Float {
            return Companion.applyDimension(
                data shr Companion.COMPLEX_UNIT_SHIFT and Companion.COMPLEX_UNIT_MASK,
                Companion.complexToFloat(data),
                metrics
            )
        }

        /**
         * Converts a complex data value holding a dimension to its final value
         * as an integer pixel offset.  This is the same as
         * [.complexToDimension], except the raw floating point value is
         * truncated to an integer (pixel) value.
         * The given <var>data</var> must be structured as a
         * [.TYPE_DIMENSION].
         *
         * @param data A complex data value holding a unit, magnitude, and
         * mantissa.
         * @param metrics Current display metrics to use in the conversion --
         * supplies display density and scaling information.
         *
         * @return The number of pixels specified by the data and its desired
         * multiplier and units.
         */
        fun complexToDimensionPixelOffset(
            data: Int,
            metrics: TDisplayMetrics
        ): Int {
            return Companion.applyDimension(
                data shr Companion.COMPLEX_UNIT_SHIFT and Companion.COMPLEX_UNIT_MASK,
                Companion.complexToFloat(data),
                metrics
            ).toInt()
        }

        /**
         * Converts a complex data value holding a dimension to its final value
         * as an integer pixel size.  This is the same as
         * [.complexToDimension], except the raw floating point value is
         * converted to an integer (pixel) value for use as a size.  A size
         * conversion involves rounding the base value, and ensuring that a
         * non-zero base value is at least one pixel in size.
         * The given <var>data</var> must be structured as a
         * [.TYPE_DIMENSION].
         *
         * @param data A complex data value holding a unit, magnitude, and
         * mantissa.
         * @param metrics Current display metrics to use in the conversion --
         * supplies display density and scaling information.
         *
         * @return The number of pixels specified by the data and its desired
         * multiplier and units.
         */
        fun complexToDimensionPixelSize(
            data: Int,
            metrics: TDisplayMetrics
        ): Int {
            val value: Float = Companion.complexToFloat(data)
            val f: Float = Companion.applyDimension(
                data shr Companion.COMPLEX_UNIT_SHIFT and Companion.COMPLEX_UNIT_MASK,
                value,
                metrics
            )
            val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
            if (res != 0) return res
            if (value == 0f) return 0
            return if (value > 0) 1 else -1
        }

        /**
         * @hide Was accidentally exposed in API level 1 for debugging purposes.
         * Kept for compatibility just in case although the debugging code has been removed.
         */
        @Deprecated("")
        fun complexToDimensionNoisy(data: Int, metrics: TDisplayMetrics): Float {
            return Companion.complexToDimension(data, metrics)
        }

        /**
         * Converts an unpacked complex data value holding a dimension to its final floating
         * point value. The two parameters <var>unit</var> and <var>value</var>
         * are as in [.TYPE_DIMENSION].
         *
         * @param unit The unit to convert from.
         * @param value The value to apply the unit to.
         * @param metrics Current display metrics to use in the conversion --
         * supplies display density and scaling information.
         *
         * @return The complex floating point value multiplied by the appropriate
         * metrics depending on its unit.
         */
        fun applyDimension(
            unit: Int, value: Float,
            metrics: TDisplayMetrics
        ): Float {
            when (unit) {
                Companion.COMPLEX_UNIT_PX -> return value
                Companion.COMPLEX_UNIT_DIP -> return value * metrics.density
                Companion.COMPLEX_UNIT_SP -> return value * metrics.scaledDensity
                Companion.COMPLEX_UNIT_PT -> return value * metrics.xdpi * (1.0f / 72)
                Companion.COMPLEX_UNIT_IN -> return value * metrics.xdpi
                Companion.COMPLEX_UNIT_MM -> return value * metrics.xdpi * (1.0f / 25.4f)
            }
            return 0f
        }

        /**
         * Construct a complex data integer.  This validates the radix and the magnitude of the
         * mantissa, and sets the [TypedValue.COMPLEX_MANTISSA_MASK] and
         * [TypedValue.COMPLEX_RADIX_MASK] components as provided. The units are not set.
         *
         * @param mantissa an integer representing the mantissa.
         * @param radix a radix option, e.g. [TypedValue.COMPLEX_RADIX_23p0].
         * @return A complex data integer representing the value.
         * @hide
         */
        private fun createComplex(
            mantissa: Int,
            radix: Int
        ): Int {
            if (mantissa < -0x800000 || mantissa >= 0x800000) {
                throw Exception("Magnitude of mantissa is too large: $mantissa")
            }
            if (radix < Companion.COMPLEX_RADIX_23p0 || radix > Companion.COMPLEX_RADIX_0p23) {
                throw Exception("Invalid radix: $radix")
            }
            return (mantissa and Companion.COMPLEX_MANTISSA_MASK shl Companion.COMPLEX_MANTISSA_SHIFT
                    or (radix shl Companion.COMPLEX_RADIX_SHIFT))
        }

        /**
         * Convert a base value to a complex data integer.  This sets the [ ][TypedValue.COMPLEX_MANTISSA_MASK] and [TypedValue.COMPLEX_RADIX_MASK] fields of the
         * data to create a floating point representation of the given value. The units are not set.
         *
         *
         * This is the inverse of [TypedValue.complexToFloat].
         *
         * @param value An integer value.
         * @return A complex data integer representing the value.
         * @hide
         */
        fun intToComplex(value: Int): Int {
            if (value < -0x800000 || value >= 0x800000) {
                throw Exception("Magnitude of the value is too large: $value")
            }
            return Companion.createComplex(value, Companion.COMPLEX_RADIX_23p0)
        }

        /**
         * Convert a base value to a complex data integer.  This sets the [ ][TypedValue.COMPLEX_MANTISSA_MASK] and [TypedValue.COMPLEX_RADIX_MASK] fields of the
         * data to create a floating point representation of the given value. The units are not set.
         *
         *
         * This is the inverse of [TypedValue.complexToFloat].
         *
         * @param value A floating point value.
         * @return A complex data integer representing the value.
         * @hide
         */
        fun floatToComplex(value: Float): Int {
            // validate that the magnitude fits in this representation
            if (value < -0x800000f - .5f || value >= 0x800000f - .5f) {
                throw Exception("Magnitude of the value is too large: $value")
            }
            return try {
                // If there's no fraction, use integer representation, as that's clearer
                if (value == value.toInt().toFloat()) {
                    return Companion.createComplex(value.toInt(), Companion.COMPLEX_RADIX_23p0)
                }
                val absValue: Float = abs(value)
                // If the magnitude is 0, we don't need any magnitude digits
                if (absValue < 1f) {
                    return Companion.createComplex(
                        round(value * (1 shl 23)).toInt(),
                        Companion.COMPLEX_RADIX_0p23
                    )
                }
                // If the magnitude is less than 2^8, use 8 magnitude digits
                if (absValue < (1 shl 8).toFloat()) {
                    return Companion.createComplex(
                        round(value * (1 shl 15)).toInt(),
                        Companion.COMPLEX_RADIX_8p15
                    )
                }
                // If the magnitude is less than 2^16, use 16 magnitude digits
                if (absValue < (1 shl 16).toFloat()) {
                    Companion.createComplex(
                        round(value * (1 shl 7)).toInt(),
                        Companion.COMPLEX_RADIX_16p7
                    )
                } else Companion.createComplex(
                    round(value).toInt(),
                    Companion.COMPLEX_RADIX_23p0
                )
                // The magnitude requires all 23 digits
            } catch (ex: Exception) {
                // Wrap exception so as to include the value argument in the message.
                throw Exception(
                    "Unable to convert value to complex: $value",
                    ex
                )
            }
        }

        /**
         *
         * Creates a complex data integer that stores a dimension value and units.
         *
         *
         * The resulting value can be passed to e.g.
         * [TypedValue.complexToDimensionPixelOffset] to calculate the pixel
         * value for the dimension.
         *
         * @param value the value of the dimension
         * @param units the units of the dimension, e.g. [TypedValue.COMPLEX_UNIT_DIP]
         * @return A complex data integer representing the value and units of the dimension.
         * @hide
         */
        fun createComplexDimension(
            value: Int,
            units: Int
        ): Int {
            if (units < Companion.COMPLEX_UNIT_PX || units > Companion.COMPLEX_UNIT_MM) {
                throw Exception("Must be a valid COMPLEX_UNIT_*: $units")
            }
            return Companion.intToComplex(value) or units
        }

        /**
         *
         * Creates a complex data integer that stores a dimension value and units.
         *
         *
         * The resulting value can be passed to e.g.
         * [TypedValue.complexToDimensionPixelOffset] to calculate the pixel
         * value for the dimension.
         *
         * @param value the value of the dimension
         * @param units the units of the dimension, e.g. [TypedValue.COMPLEX_UNIT_DIP]
         * @return A complex data integer representing the value and units of the dimension.
         * @hide
         */
        fun createComplexDimension(
            value: Float,
            units: Int
        ): Int {
            if (units < Companion.COMPLEX_UNIT_PX || units > Companion.COMPLEX_UNIT_MM) {
                throw Exception("Must be a valid COMPLEX_UNIT_*: $units")
            }
            return Companion.floatToComplex(value) or units
        }

        /**
         * Converts a complex data value holding a fraction to its final floating
         * point value. The given <var>data</var> must be structured as a
         * [.TYPE_FRACTION].
         *
         * @param data A complex data value holding a unit, magnitude, and
         * mantissa.
         * @param base The base value of this fraction.  In other words, a
         * standard fraction is multiplied by this value.
         * @param pbase The parent base value of this fraction.  In other
         * words, a parent fraction (nn%Pointer) is multiplied by this
         * value.
         *
         * @return The complex floating point value multiplied by the appropriate
         * base value depending on its unit.
         */
        fun complexToFraction(data: Int, base: Float, pbase: Float): Float {
            when (data shr Companion.COMPLEX_UNIT_SHIFT and Companion.COMPLEX_UNIT_MASK) {
                Companion.COMPLEX_UNIT_FRACTION -> return Companion.complexToFloat(data) * base
                Companion.COMPLEX_UNIT_FRACTION_PARENT -> return Companion.complexToFloat(data) * pbase
            }
            return 0f
        }

        private val DIMENSION_UNIT_STRS = arrayOf(
            "px", "dip", "sp", "pt", "in", "mm"
        )
        private val FRACTION_UNIT_STRS = arrayOf(
            "%", "%Pointer"
        )

        /**
         * Perform type conversion as per [.coerceToString] on an
         * explicitly supplied type and data.
         *
         * @param type The data type identifier.
         * @param data The data value.
         *
         * @return String The coerced string value.  If the value is
         * null or the type is not known, null is returned.
         */
        fun coerceToString(type: Int, data: Int): String? {
            when (type) {
                Companion.TYPE_NULL -> return null
                Companion.TYPE_REFERENCE -> return "@$data"
                Companion.TYPE_ATTRIBUTE -> return "?$data"
                Companion.TYPE_FLOAT -> Float.fromBits(data).toString()
                Companion.TYPE_DIMENSION -> return Companion.complexToFloat(data).toString() + Companion.DIMENSION_UNIT_STRS.get(data shr Companion.COMPLEX_UNIT_SHIFT and Companion.COMPLEX_UNIT_MASK)
                Companion.TYPE_FRACTION -> return (Companion.complexToFloat(data) * 100).toString() + Companion.FRACTION_UNIT_STRS.get(data shr Companion.COMPLEX_UNIT_SHIFT and Companion.COMPLEX_UNIT_MASK)
                Companion.TYPE_INT_HEX -> return "0x" + Int.toHexString(data)
                Companion.TYPE_INT_BOOLEAN -> return if (data != 0) "true" else "false"
            }
            if (type >= Companion.TYPE_FIRST_COLOR_INT && type <= Companion.TYPE_LAST_COLOR_INT) {
                return "#" + Int.toHexString(data)
            } else if (type >= Companion.TYPE_FIRST_INT && type <= Companion.TYPE_LAST_INT) {
                return data.toString()
            }
            return null
        }
    }
}