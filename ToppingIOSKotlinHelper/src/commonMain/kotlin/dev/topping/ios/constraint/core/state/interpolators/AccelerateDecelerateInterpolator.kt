package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.core.state.Interpolator
import kotlin.math.*

class AccelerateDecelerateInterpolator : Interpolator {
    override fun getInterpolation(input: Float): Float {
        return ((cos((input + 1) * PI) / 2.0f) + 0.5f).toFloat()
    }
}