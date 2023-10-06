package dev.topping.ios.constraint

import dev.topping.ios.constraint.shared.graphics.Outline

interface TDrawable {
    fun getOutline(outline: Outline)
    fun mutate(): TDrawable
    fun getDrawable(position: Int): TDrawable
    fun getIntrinsicWidth(): Float
    fun getIntrinsicHeight(): Float
    fun setAlpha(toInt: Int)
}