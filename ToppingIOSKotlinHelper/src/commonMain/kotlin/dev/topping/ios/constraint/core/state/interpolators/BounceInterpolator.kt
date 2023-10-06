package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.core.state.Interpolator

class BounceInterpolator : Interpolator {
    companion object {
        fun bounce(t: Float): Float {
            return t * t * 8.0f
        }
    }

    override fun getInterpolation(input: Float): Float {
        // _b(t) = t * t * 8
        // bs(t) = _b(t) for t < 0.3535
        // bs(t) = _b(t - 0.54719) + 0.7 for t < 0.7408
        // bs(t) = _b(t - 0.8526) + 0.9 for t < 0.9644
        // bs(t) = _b(t - 1.0435) + 0.95 for t <= 1.0
        // b(t) = bs(t * 1.1226)
        val t = 1.1226f * input
        return if (t < 0.3535f) {
            bounce(t)
        } else if (t < 0.7408f) {
            bounce(t - 0.54719f) + 0.7f
        } else if (t < 0.9644f) {
            bounce(t - 0.8526f) + 0.9f
        } else {
            bounce(t - 1.0435f) + 0.95f
        }
    }
}