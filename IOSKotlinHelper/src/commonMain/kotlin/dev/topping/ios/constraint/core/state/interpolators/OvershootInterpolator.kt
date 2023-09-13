package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.TResources
import dev.topping.ios.constraint.core.state.Interpolator

class OvershootInterpolator : Interpolator {
    private var mTension = 0f

    constructor() {
        mTension = 2.0f
    }

    /**
     * @param tension Amount of overshoot. When tension equals 0.0f, there is
     * no overshoot and the interpolator becomes a simple
     * deceleration interpolator.
     */
    constructor(tension: Float) {
        mTension = tension
    }

    constructor(res: TResources, attrs: MutableMap<String, String>) {
        mTension = res.getFloat(attrs["app_tension"] ?: "", 2.0f)
    }

    override fun getInterpolation(input: Float): Float {
        // _o(t) = t * t * ((tension + 1) * t + tension)
        // o(t) = _o(t - 1) + 1
        var input = input
        input -= 1.0f
        return input * input * ((mTension + 1) * input + mTension) + 1.0f
    }
}