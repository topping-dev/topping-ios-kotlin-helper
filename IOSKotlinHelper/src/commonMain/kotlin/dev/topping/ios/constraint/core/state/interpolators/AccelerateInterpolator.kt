package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.TResources
import dev.topping.ios.constraint.core.state.Interpolator
import kotlin.math.*

class AccelerateInterpolator : Interpolator {
    private var mFactor = 0f
    private var mDoubleFactor = 0.0

    /**
     * Creates a new instance of [AccelerateInterpolator] with y=x^2 parabola.
     */
    constructor() {
        mFactor = 1.0f
        mDoubleFactor = 2.0
    }

    /**
     * Creates a new instance of [AccelerateInterpolator].
     *
     * @param factor Degree to which the animation should be eased. Setting
     * factor to 1.0f produces a y=x^2 parabola. Increasing factor above
     * 1.0f  exaggerates the ease-in effect (i.e., it starts even
     * slower and ends evens faster)
     */
    constructor(factor: Float) {
        mFactor = factor
        mDoubleFactor = (2 * mFactor).toDouble()
    }

    constructor(res: TResources, attrs: MutableMap<String, String>) {
        mFactor = res.getFloat(attrs["factor"] ?: "", 1.0f)
        mDoubleFactor = mFactor * 2.0
    }

    override fun getInterpolation(input: Float): Float {
        return if (mFactor == 1.0f) {
            input * input
        } else {
            input.toDouble().pow(mDoubleFactor).toFloat()
        }
    }
}