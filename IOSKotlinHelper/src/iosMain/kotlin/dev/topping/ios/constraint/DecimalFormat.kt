package dev.topping.ios.constraint

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter

actual class DecimalFormat actual constructor() {
    actual var format: String = ""

    actual constructor(format: String) : this() {
        this.format = format
    }

    actual fun format(double: Double): String {
        NSNumberFormatter()
        val formatter = NSNumberFormatter()
        if(format != "")
            formatter.setPositiveFormat(format)
        return formatter.stringFromNumber(NSNumber(double))!!
    }

    actual fun format(float: Float): String {
        NSNumberFormatter()
        val formatter = NSNumberFormatter()
        if(format != "")
            formatter.setPositiveFormat(format)
        return formatter.stringFromNumber(NSNumber(float))!!
    }
}