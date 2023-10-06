package dev.topping.ios.constraint

expect fun nanoTime(): Long

fun nanoTimeToMilliseconds() : Long {
    return nanoTime() / 1000000
}

fun toNanoSeconds(value: Long) : Long {
    return value * 1000000
}