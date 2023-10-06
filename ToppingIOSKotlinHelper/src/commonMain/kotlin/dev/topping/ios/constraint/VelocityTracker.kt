package dev.topping.ios.constraint

import kotlin.math.*

const val MAX_POINTERS = 10
const val MAX_POINTER_ID = 10

fun FloatArray.vectorDot(b: FloatArray, m: Int): Float {
    var r = 0F
    var mI = m
    var index = 0
    while (mI > 0) {
        mI--
        r += this[index] * b[index]
        index++
    }
    return r
}

fun FloatArray.vectorNorm(m: Int): Float {
    var r = 0F
    var mI = m
    var index = 0
    while (mI > 0) {
        mI--
        val t = this[index]
        r += t * t
        index++
    }
    return r
}

interface VelocityTrackerStrategy {
    fun clear()
    fun clearPointers(idBits: BitSet32)
    fun addMovement(eventTime: Long, idBits: BitSet32, positions: Array<VelocityTrackerN.Position>)
    fun getEstimator(id: Int): Estimator?
}

/**
 * An estimator for the movements of a pointer based on a polynomial model.
 *
 * The last recorded position of the pointer is at time zero seconds.
 * Past estimated positions are at negative times and future estimated positions
 * are at positive times.
 *
 * First coefficient is position (in pixels), second is velocity (in pixels per second),
 * third is acceleration (in pixels per second squared).
 *
 * @hide For internal use only.  Not a final API.
 */
class Estimator {
    var time = 0L
    /**
     * Polynomial coefficients describing motion in X.
     */
    val xCoeff = FloatArray(MAX_DEGREE + 1)

    /**
     * Polynomial coefficients describing motion in Y.
     */
    val yCoeff = FloatArray(MAX_DEGREE + 1)

    /**
     * Polynomial degree, or zero if only position information is available.
     */
    var degree = 0

    /**
     * Confidence (coefficient of determination), between 0 (no fit) and 1 (perfect fit).
     */
    var confidence = 0f

    /**
     * Gets an estimate of the X position of the pointer at the specified time point.
     * @param time The time point in seconds, 0 is the last recorded time.
     * @return The estimated X coordinate.
     */
    fun estimateX(time: Float): Float {
        return estimate(time, xCoeff)
    }

    /**
     * Gets an estimate of the Y position of the pointer at the specified time point.
     * @param time The time point in seconds, 0 is the last recorded time.
     * @return The estimated Y coordinate.
     */
    fun estimateY(time: Float): Float {
        return estimate(time, yCoeff)
    }

    /**
     * Gets the X coefficient with the specified index.
     * @param index The index of the coefficient to return.
     * @return The X coefficient, or 0 if the index is greater than the degree.
     */
    fun getXCoeff(index: Int): Float {
        return if (index <= degree) xCoeff[index] else 0f
    }

    /**
     * Gets the Y coefficient with the specified index.
     * @param index The index of the coefficient to return.
     * @return The Y coefficient, or 0 if the index is greater than the degree.
     */
    fun getYCoeff(index: Int): Float {
        return if (index <= degree) yCoeff[index] else 0f
    }

    fun clear() {
        time = 0L
        degree = 0
        confidence = 0f
        for (i in 0..MAX_DEGREE) {
            xCoeff[i] = 0f
            yCoeff[i] = 0f
        }
    }

    private fun estimate(time: Float, c: FloatArray): Float {
        var a = 0f
        var scale = 1f
        for (i in 0..degree) {
            a += c[i] * scale
            scale *= time
        }
        return a
    }

    companion object {
        // Must match VelocityTracker::Estimator::MAX_DEGREE
        private const val MAX_DEGREE = 4
    }
}

class VelocityTrackerN(strategy: Int) {

    companion object {
        const val NANOS_PER_MS: Long = 1000000

        const val ASSUME_POINTER_STOPPED_TIME: Long = 40 * NANOS_PER_MS

        fun vectorDot(a: FloatArray, b: FloatArray, m: UInt): Float {
            var r = 0f
            var i = m.toInt()
            while (i > 0) {
                i--
                r += a[i] * b[i]
            }
            return r
        }

        fun vectorNorm(a: FloatArray, m: UInt): Float {
            var r = 0f
            var i = m.toInt()
            while (i > 0) {
                i--
                val t = a[i]
                r += t * t
            }
            return sqrt(r)
        }
    }

    data class Position(
        var x: Float,
        var y: Float
    )

    var mLastEventTime = 0L
    var mCurrentPointerIdBits = BitSet32()
    var mActivePointerId = -1
    lateinit var mStrategy: VelocityTrackerStrategy

    fun createStrategy(strategy: String): LeastSquaresVelocityTrackerStrategy {
        return when (strategy) {
            "lsq1" -> LeastSquaresVelocityTrackerStrategy(1, LeastSquaresVelocityTrackerStrategy.Weighting.WEIGHTING_NONE)
            "lsq2" -> LeastSquaresVelocityTrackerStrategy(2, LeastSquaresVelocityTrackerStrategy.Weighting.WEIGHTING_NONE)
            "lsq3" -> LeastSquaresVelocityTrackerStrategy(3, LeastSquaresVelocityTrackerStrategy.Weighting.WEIGHTING_NONE)
            "wlsq2-delta" -> LeastSquaresVelocityTrackerStrategy(2, LeastSquaresVelocityTrackerStrategy.Weighting.WEIGHTING_DELTA)
            "wlsq2-central" -> LeastSquaresVelocityTrackerStrategy(2, LeastSquaresVelocityTrackerStrategy.Weighting.WEIGHTING_CENTRAL)
            else -> throw IllegalArgumentException("Invalid strategy")
        }
    }

    fun clear() {
        mCurrentPointerIdBits.clear()
        mActivePointerId = -1
        mStrategy.clear()
    }

    fun clearPointers(idBits: BitSet32) {
        val remainingIdBits = BitSet32(mCurrentPointerIdBits.value.v and idBits.value.v.inv())
        mCurrentPointerIdBits = remainingIdBits
        if (mActivePointerId >= 0 && idBits.hasBit(mActivePointerId.toUInt())) {
            mActivePointerId = if (!remainingIdBits.isEmpty()) remainingIdBits.firstMarkedBit().toInt() else -1
        }
        mStrategy.clearPointers(idBits)
    }

    fun addMovement(event: MotionEvent) {
        val actionMasked = event.actionMasked

        when(actionMasked) {
            AMOTION.AMOTION_EVENT_ACTION_MASK.value,
            AMOTION.AMOTION_EVENT_ACTION_HOVER_ENTER.value -> {
                clear()
            }
            AMOTION.AMOTION_EVENT_ACTION_POINTER_DOWN.value -> {
                var downIdBits = BitSet32()
                downIdBits.markBit(event.getPointerId(event.actionIndex).toUInt())
                clearPointers(downIdBits)
            }
            AMOTION.AMOTION_EVENT_ACTION_MOVE.value,
            AMOTION.AMOTION_EVENT_ACTION_HOVER_MOVE.value -> {

            }
            else -> {
                return@addMovement
            }
        }

        var pointerCount = event.pointerCount
        if(pointerCount > MAX_POINTERS) {
            pointerCount = MAX_POINTERS
        }

        var idBits = BitSet32()
        for(i in 0 until pointerCount) {
            idBits.markBit(event.getPointerId(i).toUInt())
        }

        var pointerIndex = IntArray(MAX_POINTERS)
        for(i in 0 until pointerCount) {
            pointerIndex[i] = idBits.getIndexOfBit(event.getPointerId(i).toUInt()).toInt()
        }

        var positions = Array<Position>(pointerCount) { Position(0f, 0f) }
        var historySize = event.historySize
        for(h in 0 .. historySize) {
            var eventTime = event.getHistoricalEventTime(h)
            for(i in 0 until pointerCount) {
                var index = pointerIndex[i]
                positions[index].x = event.getHistoricalX(i, h)
                positions[index].y = event.getHistoricalY(i, h)
            }
            addMovement(eventTime, idBits, positions)
        }
    }

    fun addMovement(eventTime: Long, idBits: BitSet32, positions: Array<Position>) {
        while (idBits.count() > MAX_POINTERS) {
            idBits.clearLastMarkedBit()
        }
        if (((mCurrentPointerIdBits.value.v and idBits.value.v) != 0U)
            && eventTime >= mLastEventTime + ASSUME_POINTER_STOPPED_TIME) {
            mStrategy.clear()
        }
        mLastEventTime = eventTime
        mCurrentPointerIdBits = idBits
        if (mActivePointerId < 0 || !idBits.hasBit(mActivePointerId.toUInt())) {
            mActivePointerId = if (idBits.isEmpty()) -1 else idBits.firstMarkedBit().toInt()
        }
        mStrategy.addMovement(eventTime, idBits, positions)
    }

    fun getVelocity(id: Int, outVx: Pointer<Float>, outVy: Pointer<Float>): Boolean {
        var estimator = getEstimator(id)
        if (estimator != null && estimator.degree >= 1) {
            outVx.v = estimator.xCoeff[1]
            outVy.v = estimator.yCoeff[1]
            return true
        }
        outVx.v = 0f
        outVy.v = 0f
        return false
    }

    fun getEstimator(id: Int) : Estimator? {
        return mStrategy.getEstimator(id)
    }

    inline fun getActivePointerId(): Int { return mActivePointerId }
    inline fun getCurrentPointerIdBits(): BitSet32 { return mCurrentPointerIdBits }
}

class LeastSquaresVelocityTrackerStrategy(val degree: Int, val weighting: Weighting) : VelocityTrackerStrategy {

    enum class Weighting {
        // No weights applied.  All data points are equally reliable.
        WEIGHTING_NONE,  // Weight by time delta.  Data points clustered together are weighted less.
        WEIGHTING_DELTA,  // Weight such that points within a certain horizon are weighed more than those

        // outside of that horizon.
        WEIGHTING_CENTRAL,  // Weight such that points older than a certain amount are weighed less.
        WEIGHTING_RECENT
    }

    class Movement {
        var eventTime = 0L
        var idBits = BitSet32()
        var positions = Array<VelocityTrackerN.Position>(MAX_POINTERS) { VelocityTrackerN.Position(0f, 0f) }

        inline fun getPosition(id: Int): VelocityTrackerN.Position {
            return positions[idBits.getIndexOfBit(id.toUInt()).toInt()]
        }
    }

    init {
        clear()
    }

    private var index = 0
    private val movements = Array(HISTORY_SIZE) { Movement() }

    companion object {
        const val HORIZON = 100L * 1000000L
        const val HISTORY_SIZE = 20

        fun solveLeastSquares(
            x: FloatArray,
            y: FloatArray,
            w: FloatArray,
            m: Int,
            n: Int,
            outB: FloatArray,
            outDet: Pointer<Float>
        ) : Boolean {
            // Expand the X vector to a matrix A, pre-multiplied by the weights.
            var a = Array(n) { FloatArray(m) } // column-major order
            for (h in 0 until m) {
                a[0][h] = w[h]
                for(i in 1 until n) {
                    a[i][h] = a[i - 1][h] * x[h]
                }
            }

            // Apply the Gram-Schmidt process to A to obtain its QR decomposition.
            var q = Array(n) { FloatArray(m) } // orthonormal basis, column-major order
            var r = Array(n) { FloatArray(n) } // upper triangular matrix, row-major order
            for (j in 0 until n) {
                for (h in 0 until m) {
                    q[j][h] = a[j][h]
                }
                for(i in 0 until j) {
                    val dot = q[j].vectorDot(q[i], m)
                    for(h in 0 until m) {
                        q[j][h] -= dot * q[i][h]
                    }
                }
                var norm = q[j].vectorNorm(m)
                if (norm < 0.000001f) {
                    // vectors are linearly dependent or zero so no solution
                    return false
                }
                var invNorm = 1.0f / norm
                for(h in 0 until m) {
                    q[j][h] *= invNorm
                }
                for(i in 0 until n) {
                    r[j][i] = if(i < j) 0f else q[j].vectorDot(a[i], m)
                }
            }

            // Solve R B = Qt W Y to find B.  This is easy because R is upper triangular.
            // We just work from bottom-right to top-left calculating B's coefficients.
            var wy = FloatArray(m)
            for(h in 0 until m) {
                wy[h] = y[h] * w[h]
            }
            for(i in (n-1) until 0) {
                outB[i] = q[i].vectorDot(wy, m)
                for(j in (n-1) until i) {
                    outB[i] -= r[i][j] * outB[j]
                }
                outB[i] /= r[i][i]
            }

            // Calculate the coefficient of determination as 1 - (SSerr / SStot) where
            // SSerr is the residual sum of squares (variance of the error),
            // and SStot is the total sum of squares (variance of the data) where each
            // has been weighted.
            var ymean = 0f;
            for (h in 0 until m) {
                ymean += y[h]
            }
            ymean /= m
            var sserr = 0f
            var sstot = 0f
            for(h in 0 until m) {
                var err = y[h] - outB[0]
                var term = 1f
                for(i in 1 until n) {
                    term *= x[h]
                    err -= term * outB[i]
                }
                sserr += w[h] * w[h] * err * err
                var vari = y[h] - ymean
                sstot += w[h] * w[h] * vari * vari
            }
            outDet.v = if(sstot > 0.000001f) 1.0f - (sserr / sstot) else 1f

            return true
        }
    }

    override fun clear() {
        index = 0
        movements[0].idBits.clear()
    }

    override fun clearPointers(idBits: BitSet32) {
        val remainingIdBits = BitSet32(movements[index].idBits.value.v and idBits.value.v.inv())
        movements[index].idBits = remainingIdBits
    }

    override fun addMovement(eventTime: Long, idBits: BitSet32, positions: Array<VelocityTrackerN.Position>) {
        if (++index == HISTORY_SIZE) {
            index = 0
        }
        val movement = movements[index]
        movement.eventTime = eventTime
        movement.idBits = idBits
        val count = idBits.count()
        for (i in 0 until count) {
            movement.positions[i] = positions[i]
        }
    }

    override fun getEstimator(id: Int): Estimator? {
        var outEstimator = Estimator()
        outEstimator.clear()
        var x = FloatArray(HISTORY_SIZE)
        var y = FloatArray(HISTORY_SIZE)
        var w = FloatArray(HISTORY_SIZE)
        var time = FloatArray(HISTORY_SIZE)

        var m = 0
        var index = this.index
        var newestMovement = this.movements[this.index]

        do {
            var movement = movements[index]
            if(!movement.idBits.hasBit(id.toUInt())) {
                break
            }

            var age = newestMovement.eventTime - movement.eventTime
            if(age > HORIZON) {
                break
            }

            var position = movement.getPosition(id)
            x[m] = position.x.toFloat()
            y[m] = position.y.toFloat()
            w[m] = chooseWeight(index)
            time[m] = -age * 0.000000001f
            index = (if(index == 0) HISTORY_SIZE else index) - 1
        } while (++m < HISTORY_SIZE)

        if (m == 0) {
            return null // no data
        }

        var degree = this.degree
        if (degree > m - 1) {
            degree = m - 1
        }
        if (degree >= 1) {
            var xdet = Pointer(0f)
            var ydet = Pointer(0f)
            var n = degree + 1
            if (solveLeastSquares(time, x, w, m, n, outEstimator.xCoeff, xdet)
            && solveLeastSquares(time, y, w, m, n, outEstimator.yCoeff, ydet)) {
                    outEstimator.time = newestMovement.eventTime
                outEstimator.degree = degree
                outEstimator.confidence = xdet.v * ydet.v
                return outEstimator
            }
        }
        // No velocity data available for this pointer, but we do have its current position.
        outEstimator.xCoeff[0] = x[0]
        outEstimator.yCoeff[0] = y[0]
        outEstimator.time = newestMovement.eventTime
        outEstimator.degree = 0
        outEstimator.confidence = 1f
        return outEstimator
    }

    fun chooseWeight(index: Int): Float {
        when(weighting) {
            Weighting.WEIGHTING_DELTA -> {
                // Weight points based on how much time elapsed between them and the next
                // point so that points that "cover" a shorter time span are weighed less.
                //   delta  0ms: 0.5
                //   delta 10ms: 1.0
                if (index == this.index) {
                    return 1.0f
                }
                var nextIndex = (index + 1) % HISTORY_SIZE
                var deltaMillis = (movements[nextIndex].eventTime- movements[index].eventTime) * 0.000001f
                if (deltaMillis < 0) {
                    return 0.5f
                }
                if (deltaMillis < 10) {
                    return 0.5f + deltaMillis * 0.05f
                }
                return 1.0f
            }
            Weighting.WEIGHTING_CENTRAL -> {
                // Weight points based on their age, weighing very recent and very old points less.
                //   age  0ms: 0.5
                //   age 10ms: 1.0
                //   age 50ms: 1.0
                //   age 60ms: 0.5
                var ageMillis = (movements[index].eventTime - movements[index].eventTime) * 0.000001f
                if (ageMillis < 0) {
                    return 0.5f
                }
                if (ageMillis < 10) {
                    return 0.5f + ageMillis * 0.05f
                }
                if (ageMillis < 50) {
                    return 1.0f
                }
                if (ageMillis < 60) {
                    return 0.5f + (60 - ageMillis) * 0.05f
                }
                return 0.5f
            }
            Weighting.WEIGHTING_RECENT -> {
                // Weight points based on their age, weighing older points less.
                //   age   0ms: 1.0
                //   age  50ms: 1.0
                //   age 100ms: 0.5
                var ageMillis = (movements[index].eventTime - movements[index].eventTime) * 0.000001f
                if (ageMillis < 50) {
                    return 1.0f
                }
                if (ageMillis < 100) {
                    return 0.5f + (100 - ageMillis) * 0.01f
                }
                return 0.5f
            }
            Weighting.WEIGHTING_NONE -> return 1.0f
            else -> return 1.0f
        }
    }
}

class VelocityTrackerState {
    data class Velocity(
        var vx: Float,
        var vy: Float
    )

    companion object {
        const val ACTIVE_POINTER_ID = -1
    }

    var mVelocityTracker: VelocityTrackerN

    constructor() {
        mVelocityTracker = VelocityTrackerN(VelocityTracker.VELOCITY_TRACKER_STRATEGY_DEFAULT)
    }

    constructor(strategy: Int) {
        mVelocityTracker = VelocityTrackerN(strategy)
    }


    var mActivePointerId: Int = -1
    var mCalculatedIdBits = BitSet32()
    var mCalculatedVelocity = mutableListOf<Velocity>()

    fun clear() {
        mVelocityTracker.clear()
        mActivePointerId = -1
        mCalculatedIdBits.clear()
    }

    fun addMovement(event: MotionEvent) {
        mVelocityTracker.addMovement(event)
    }

    fun computeCurrentVelocity(units: Int, maxVelocity: Float) {
        var idBits = BitSet32(mVelocityTracker.getCurrentPointerIdBits().value)
        mCalculatedIdBits = idBits
        var index = 0
        while(!idBits.isEmpty()) {
            var id = idBits.clearFirstMarkedBit();
            var vx = Pointer(0f)
            var vy = Pointer(0f)
            mVelocityTracker.getVelocity(id.toInt(), vx, vy)
            vx.v = vx.v * units / 1000
            vy.v = vy.v * units / 1000
            if (vx.v > maxVelocity) {
                vx.v = maxVelocity
            } else if (vx.v < -maxVelocity) {
                vx.v = -maxVelocity
            }
            if (vy.v > maxVelocity) {
                vy.v = maxVelocity
            } else if (vy.v < -maxVelocity) {
                vy.v = -maxVelocity
            }
            var velocity = mCalculatedVelocity[index]
            velocity.vx = vx.v
            velocity.vy = vy.v
        }
    }

    fun getVelocity(id: Int, outVx: Pointer<Float>, outVy: Pointer<Float>) {
        var id = id
        if (id == ACTIVE_POINTER_ID) {
            id = mVelocityTracker.getActivePointerId()
        }

        var vx = 0f
        var vy = 0f
        if (id >= 0 && id <= MAX_POINTER_ID && mCalculatedIdBits.hasBit(id.toUInt())) {
            var index = mCalculatedIdBits.getIndexOfBit(id.toUInt())
            var velocity = mCalculatedVelocity[index.toInt()]
            vx = velocity.vx
            vy = velocity.vy
        } else {
            vx = 0f
            vy = 0f
        }

        outVx.v = vx
        outVy.v = vy
    }

    fun getEstimator(id: Int): Estimator? {
        return mVelocityTracker.getEstimator(id)
    }
}

class VelocityTracker constructor(strategy: Int) {
    private var mPtr: VelocityTrackerState?

    /**
     * Return strategy Id of VelocityTracker object.
     * @return The velocity tracker strategy Id.
     *
     * @hide
     */
    var strategyId = 0

    /**
     * Return a VelocityTracker object back to be re-used by others.  You must
     * not touch the object after calling this function.
     */
    fun recycle() {
        if (strategyId == VELOCITY_TRACKER_STRATEGY_DEFAULT) {
            clear()
            sPool.clear()
        }
    }

    init {
        // If user has not selected a specific strategy
        strategyId = strategy
        mPtr = VelocityTrackerState(strategyId)
    }

    /**
     * Reset the velocity tracker back to its initial state.
     */
    fun clear() {
        mPtr!!.clear()
    }

    /**
     * Add a user's movement to the tracker.  You should call this for the
     * initial [MotionEvent.ACTION_DOWN], the following
     * [MotionEvent.ACTION_MOVE] events that you receive, and the
     * final [MotionEvent.ACTION_UP].  You can, however, call this
     * for whichever events you desire.
     *
     * @param event The MotionEvent you received and would like to track.
     */
    fun addMovement(event: MotionEvent) {
        mPtr!!.addMovement(event)
    }

    /**
     * Equivalent to invoking [.computeCurrentVelocity] with a maximum
     * velocity of Float.MAX_VALUE.
     *
     * @see .computeCurrentVelocity
     */
    fun computeCurrentVelocity(units: Int) {
        mPtr!!.computeCurrentVelocity(units, Float.MAX_VALUE)
    }

    /**
     * Compute the current velocity based on the points that have been
     * collected.  Only call this when you actually want to retrieve velocity
     * information, as it is relatively expensive.  You can then retrieve
     * the velocity with [.getXVelocity] and
     * [.getYVelocity].
     *
     * @param units The units you would like the velocity in.  A value of 1
     * provides pixels per millisecond, 1000 provides pixels per second, etc.
     * @param maxVelocity The maximum velocity that can be computed by this method.
     * This value must be declared in the same unit as the units parameter. This value
     * must be positive.
     */
    fun computeCurrentVelocity(units: Int, maxVelocity: Float) {
        mPtr!!.computeCurrentVelocity(units, maxVelocity)
    }

    /**
     * Retrieve the last computed X velocity.  You must first call
     * [.computeCurrentVelocity] before calling this function.
     *
     * @return The previously computed X velocity.
     */
    val xVelocity: Float
        get() {
            var x = Pointer(0f)
            var y = Pointer(0f)
            mPtr!!.getVelocity(ACTIVE_POINTER_ID, x, y)
            return x.v
        }

    /**
     * Retrieve the last computed Y velocity.  You must first call
     * [.computeCurrentVelocity] before calling this function.
     *
     * @return The previously computed Y velocity.
     */
    val yVelocity: Float
        get() {
            var x = Pointer(0f)
            var y = Pointer(0f)
            mPtr!!.getVelocity(ACTIVE_POINTER_ID, x, y)
            return y.v
        }

    /**
     * Retrieve the last computed X velocity.  You must first call
     * [.computeCurrentVelocity] before calling this function.
     *
     * @param id Which pointer's velocity to return.
     * @return The previously computed X velocity.
     */
    fun getXVelocity(id: Int): Float {
        var x = Pointer(0f)
        var y = Pointer(0f)
        mPtr!!.getVelocity(id, x, y)
        return x.v
    }

    /**
     * Retrieve the last computed Y velocity.  You must first call
     * [.computeCurrentVelocity] before calling this function.
     *
     * @param id Which pointer's velocity to return.
     * @return The previously computed Y velocity.
     */
    fun getYVelocity(id: Int): Float {
        var x = Pointer(0f)
        var y = Pointer(0f)
        mPtr!!.getVelocity(id, x, y)
        return y.v
    }

    /**
     * Get an estimator for the movements of a pointer using past movements of the
     * pointer to predict future movements.
     *
     * It is not necessary to call [.computeCurrentVelocity] before calling
     * this method.
     *
     * @param id Which pointer's velocity to return.
     * @param outEstimator The estimator to populate.
     * @return True if an estimator was obtained, false if there is no information
     * available about the pointer.
     *
     * @hide For internal use only.  Not a final API.
     */
    fun getEstimator(id: Int): Estimator? {
        return mPtr!!.getEstimator(id)
    }

    companion object {
        private val sPool: Pool<VelocityTracker?> = Pool(5)
        private const val ACTIVE_POINTER_ID = -1

        /**
         * Velocity Tracker Strategy: Invalid.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_DEFAULT = -1

        /**
         * Velocity Tracker Strategy: Impulse.
         * Physical model of pushing an object.  Quality: VERY GOOD.
         * Works with duplicate coordinates, unclean finger liftoff.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_IMPULSE = 0

        /**
         * Velocity Tracker Strategy: LSQ1.
         * 1st order least squares.  Quality: POOR.
         * Frequently underfits the touch data especially when the finger accelerates
         * or changes direction.  Often underestimates velocity.  The direction
         * is overly influenced by historical touch points.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_LSQ1 = 1

        /**
         * Velocity Tracker Strategy: LSQ2.
         * 2nd order least squares.  Quality: VERY GOOD.
         * Pretty much ideal, but can be confused by certain kinds of touch data,
         * particularly if the panel has a tendency to generate delayed,
         * duplicate or jittery touch coordinates when the finger is released.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_LSQ2 = 2

        /**
         * Velocity Tracker Strategy: LSQ3.
         * 3rd order least squares.  Quality: UNUSABLE.
         * Frequently overfits the touch data yielding wildly divergent estimates
         * of the velocity when the finger is released.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_LSQ3 = 3

        /**
         * Velocity Tracker Strategy: WLSQ2_DELTA.
         * 2nd order weighted least squares, delta weighting.  Quality: EXPERIMENTAL
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_WLSQ2_DELTA = 4

        /**
         * Velocity Tracker Strategy: WLSQ2_CENTRAL.
         * 2nd order weighted least squares, central weighting.  Quality: EXPERIMENTAL
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_WLSQ2_CENTRAL = 5

        /**
         * Velocity Tracker Strategy: WLSQ2_RECENT.
         * 2nd order weighted least squares, recent weighting.  Quality: EXPERIMENTAL
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_WLSQ2_RECENT = 6

        /**
         * Velocity Tracker Strategy: INT1.
         * 1st order integrating filter.  Quality: GOOD.
         * Not as good as 'lsq2' because it cannot estimate acceleration but it is
         * more tolerant of errors.  Like 'lsq1', this strategy tends to underestimate
         * the velocity of a fling but this strategy tends to respond to changes in
         * direction more quickly and accurately.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_INT1 = 7

        /**
         * Velocity Tracker Strategy: INT2.
         * 2nd order integrating filter.  Quality: EXPERIMENTAL.
         * For comparison purposes only.  Unlike 'int1' this strategy can compensate
         * for acceleration but it typically overestimates the effect.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_INT2 = 8

        /**
         * Velocity Tracker Strategy: Legacy.
         * Legacy velocity tracker algorithm.  Quality: POOR.
         * For comparison purposes only.  This algorithm is strongly influenced by
         * old data points, consistently underestimates velocity and takes a very long
         * time to adjust to changes in direction.
         *
         * @hide
         */
        const val VELOCITY_TRACKER_STRATEGY_LEGACY = 9

        /**
         * Velocity Tracker Strategy look up table.
         */
        private val STRATEGIES: MutableMap<String, Int> = mutableMapOf()

        init {
            // Strategy string and IDs mapping lookup.
            STRATEGIES["impulse"] = VELOCITY_TRACKER_STRATEGY_IMPULSE
            STRATEGIES["lsq1"] = VELOCITY_TRACKER_STRATEGY_LSQ1
            STRATEGIES["lsq2"] = VELOCITY_TRACKER_STRATEGY_LSQ2
            STRATEGIES["lsq3"] = VELOCITY_TRACKER_STRATEGY_LSQ3
            STRATEGIES["wlsq2-delta"] = VELOCITY_TRACKER_STRATEGY_WLSQ2_DELTA
            STRATEGIES["wlsq2-central"] = VELOCITY_TRACKER_STRATEGY_WLSQ2_CENTRAL
            STRATEGIES["wlsq2-recent"] = VELOCITY_TRACKER_STRATEGY_WLSQ2_RECENT
            STRATEGIES["int1"] = VELOCITY_TRACKER_STRATEGY_INT1
            STRATEGIES["int2"] = VELOCITY_TRACKER_STRATEGY_INT2
            STRATEGIES["legacy"] = VELOCITY_TRACKER_STRATEGY_LEGACY
        }

        /**
         * Return a strategy ID from string.
         */
        private fun toStrategyId(strStrategy: String): Int {
            return if (STRATEGIES.containsKey(strStrategy)) {
                STRATEGIES[strStrategy]!!
            } else VELOCITY_TRACKER_STRATEGY_DEFAULT
        }

        /**
         * Retrieve a new VelocityTracker object to watch the velocity of a
         * motion.  Be sure to call [.recycle] when done.  You should
         * generally only maintain an active object while tracking a movement,
         * so that the VelocityTracker can be re-used elsewhere.
         *
         * @return Returns a new VelocityTracker.
         */
        fun obtain(): VelocityTracker {
            val instance: VelocityTracker? = sPool.acquire()
            return instance ?: VelocityTracker(VELOCITY_TRACKER_STRATEGY_DEFAULT)
        }

        /**
         * Obtains a velocity tracker with the specified strategy as string.
         * For testing and comparison purposes only.
         * @param strategy The strategy, or null to use the default.
         * @return The velocity tracker.
         *
         * @hide
         */
        fun obtain(strategy: String?): VelocityTracker {
            return if (strategy == null) {
                obtain()
            } else VelocityTracker(toStrategyId(strategy))
        }

        /**
         * Obtains a velocity tracker with the specified strategy.
         * For testing and comparison purposes only.
         *
         * @param strategy The strategy Id, VELOCITY_TRACKER_STRATEGY_DEFAULT to use the default.
         * @return The velocity tracker.
         *
         * @hide
         */
        fun obtain(strategy: Int): VelocityTracker {
            return VelocityTracker(strategy)
        }
    }
}