package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.TResources
import dev.topping.ios.constraint.core.state.Interpolator

class AnticipateInterpolator : Interpolator {
    private var mTension = 2.0f

    /**
     * Creates a new instance of [AnticipateInterpolator] with y=x^2 parabola.
     */
    constructor() {
        mTension = 1.0f
    }

    /**
     * @param tension Amount of anticipation. When tension equals 0.0f, there is
     *                no anticipation and the interpolator becomes a simple
     *                acceleration interpolator.
     */
    constructor(tension: Float) {
        mTension = tension
    }

    constructor(res: TResources, attrs: MutableMap<String, String>) {
        mTension = res.getFloat(attrs["tension"] ?: "", 2.0f)
    }

    override fun getInterpolation(input: Float): Float {
        return input * input * ((mTension + 1) * input - mTension);
    }
}