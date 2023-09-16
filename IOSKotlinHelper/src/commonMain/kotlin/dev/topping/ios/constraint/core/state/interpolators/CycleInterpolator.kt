package dev.topping.ios.constraint.core.state.interpolators

import dev.topping.ios.constraint.TResources
import dev.topping.ios.constraint.core.state.Interpolator
import kotlin.math.PI
import kotlin.math.sin

class CycleInterpolator : Interpolator {
    private var mCycles = 1.0f

    constructor(cycles: Float) {
        mCycles = cycles
    }

    constructor(res: TResources, attrs: MutableMap<String, String>) {
        mCycles = res.getFloat(attrs["cycles"] ?: "", 1.0f)
    }

    override fun getInterpolation(input: Float): Float {
        return (sin(2 * mCycles * PI * input)).toFloat()
    }
}