package dev.topping.ios.constraint

import dev.topping.ios.constraint.shared.graphics.ColorMatrixColorFilter
import org.jetbrains.skia.Matrix33

interface TImageButton {
    fun getDrawable(): TDrawable?
    fun setImageDrawable(drawable: TDrawable?)
    fun swizzleFunction(funcName: String, block: (TImageButton, Any?) -> (Any?))
    fun setImageResource(resId: String)
    fun setColorFilter(colorMatrixColorFilter: ColorMatrixColorFilter)
    fun clearColorFilter()
    fun setImageMatrix(imageMatrix: Matrix33)
    fun setScaleType(matrix: ScaleType)
}