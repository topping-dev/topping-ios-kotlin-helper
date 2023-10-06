package dev.topping.ios.constraint

expect class DecimalFormat() {
    var format: String

    constructor(format: String)

    fun format(double: Double): String

    fun format(float: Float): String
}
