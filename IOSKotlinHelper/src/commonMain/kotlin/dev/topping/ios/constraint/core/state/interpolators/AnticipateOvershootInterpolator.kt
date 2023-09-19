package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.TResources
import dev.topping.ios.constraint.core.state.Interpolator
import kotlin.math.*

class AnticipateOvershootInterpolator : Interpolator {
    private var mTension = 0f

    companion object {
        private fun a(t: Float, s: Float): Float {
            return t * t * ((s + 1) * t - s)
        }

        private fun o(t: Float, s: Float): Float {
            return t * t * ((s + 1) * t + s)
        }
    }

    /**
     * Creates a new instance of [AnticipateOvershootInterpolator] with y=x^2 parabola.
     */
    constructor() {
        mTension = 2.0f * 1.5f
    }

    /**
     * Creates a new instance of [AnticipateOvershootInterpolator].
     *
     * @param factor Degree to which the animation should be eased. Setting
     * factor to 1.0f produces a y=x^2 parabola. Increasing factor above
     * 1.0f  exaggerates the ease-in effect (i.e., it starts even
     * slower and ends evens faster)
     */
    constructor(tension: Float) {
        mTension = tension * 1.5f
    }

    constructor(tension: Float, extraTension: Float) {
        mTension = tension * extraTension
    }

    constructor(res: TResources, attrs: MutableMap<String, String>) {
        mTension = res.getFloat(null, attrs["tension"] ?: "", 2.0f) * res.getFloat(
            null,
            attrs["extraTension"] ?: "",
            1.5f
        )
    }

    override fun getInterpolation(input: Float): Float {
        // a(t, s) = t * t * ((s + 1) * t - s)
        // o(t, s) = t * t * ((s + 1) * t + s)
        // f(t) = 0.5 * a(t * 2, tension * extraTension), when t < 0.5
        // f(t) = 0.5 * (o(t * 2 - 2, tension * extraTension) + 2), when t <= 1.0
        if (input < 0.5f) return 0.5f * a(input * 2.0f, mTension)
        else return 0.5f * (o(input * 2.0f - 2.0f, mTension) + 2.0f)
    }
}