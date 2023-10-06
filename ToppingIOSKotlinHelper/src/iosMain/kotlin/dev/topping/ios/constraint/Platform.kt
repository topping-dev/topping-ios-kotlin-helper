package dev.topping.ios.constraint

import platform.CoreFoundation.CFAbsoluteTimeGetCurrent

actual fun nanoTime(): Long = CFAbsoluteTimeGetCurrent().toLong()