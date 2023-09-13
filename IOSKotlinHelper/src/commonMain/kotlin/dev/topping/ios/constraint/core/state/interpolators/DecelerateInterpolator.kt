package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.TResources
import dev.topping.ios.constraint.core.state.Interpolator
import dev.topping.ios.constraint.pow

class DecelerateInterpolator : Interpolator {
    private var mFactor = 1f

    /**
     * Creates a new instance of [DecelerateInterpolator] with y=x^2 parabola.
     */
    constructor() {
        mFactor = 1f
    }

    /**
     * Creates a new instance of [DecelerateInterpolator].
     *
     * @param factor Degree to which the animation should be eased. Setting
     * factor to 1.0f produces a y=x^2 parabola. Increasing factor above
     * 1.0f  exaggerates the ease-in effect (i.e., it starts even
     * slower and ends evens faster)
     */
    constructor(factor: Float) {
        mFactor = factor
    }

    constructor(res: TResources, attrs: MutableMap<String, String>) {
        mFactor = res.getFloat(attrs["app_factor"] ?: "", 1.0f)
    }

    override fun getInterpolation(input: Float): Float {
        val result: Float
        result = if (mFactor == 1.0f) {
            1.0f - (1.0f - input) * (1.0f - input)
        } else {
            (1.0f - pow(1.0f - input, (2 * mFactor).toDouble()))
        }
        return result
    }
}