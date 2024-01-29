package dev.topping.ios.constraint

import kotlin.math.*

enum class DISPLAY_ORIENTATION(val value: Int) {
    DISPLAY_ORIENTATION_0(0),
    DISPLAY_ORIENTATION_90(1),
    DISPLAY_ORIENTATION_180(2),
    DISPLAY_ORIENTATION_270(3)
}

enum class AMOTION(val value: Int) {
    AMOTION_EVENT_ACTION_MASK(0xff),
    AMOTION_EVENT_ACTION_POINTER_INDEX_MASK(0xff00),
    AMOTION_EVENT_ACTION_DOWN(0),
    AMOTION_EVENT_ACTION_UP(1),
    AMOTION_EVENT_ACTION_MOVE(2),
    AMOTION_EVENT_ACTION_CANCEL(3),
    AMOTION_EVENT_ACTION_OUTSIDE(4),
    AMOTION_EVENT_ACTION_POINTER_DOWN(5),
    AMOTION_EVENT_ACTION_POINTER_UP(6),
    AMOTION_EVENT_ACTION_HOVER_MOVE(7),
    AMOTION_EVENT_ACTION_SCROLL(8),
    AMOTION_EVENT_ACTION_HOVER_ENTER(9),
    AMOTION_EVENT_ACTION_HOVER_EXIT(10),
    AMOTION_EVENT_ACTION_BUTTON_PRESS(11),
    AMOTION_EVENT_ACTION_BUTTON_RELEASE(12),
}

enum class AINPUT_SOURCE(val value: UInt) {
    AINPUT_SOURCE_CLASS_MASK(0x000000ffU),
    AINPUT_SOURCE_CLASS_NONE(0x00000000U),
    AINPUT_SOURCE_CLASS_BUTTON(0x00000001U),
    AINPUT_SOURCE_CLASS_POINTER(0x00000002U),
    AINPUT_SOURCE_CLASS_NAVIGATION(0x00000004U),
    AINPUT_SOURCE_CLASS_POSITION(0x00000008U),
    AINPUT_SOURCE_CLASS_JOYSTICK(0x00000010U),

    AINPUT_SOURCE_UNKNOWN(0x00000000U),
    AINPUT_SOURCE_KEYBOARD(0x00000100U or AINPUT_SOURCE_CLASS_BUTTON.value),
    AINPUT_SOURCE_DPAD(0x00000200U or AINPUT_SOURCE_CLASS_BUTTON.value),
    AINPUT_SOURCE_GAMEPAD(0x00000400U or AINPUT_SOURCE_CLASS_BUTTON.value),
    AINPUT_SOURCE_TOUCHSCREEN(0x00001000U or AINPUT_SOURCE_CLASS_POINTER.value),
    AINPUT_SOURCE_MOUSE(0x00002000U or AINPUT_SOURCE_CLASS_POINTER.value),
    AINPUT_SOURCE_STYLUS(0x00004000U or AINPUT_SOURCE_CLASS_POINTER.value),
    AINPUT_SOURCE_BLUETOOTH_STYLUS(0x00008000U or AINPUT_SOURCE_STYLUS.value),
    AINPUT_SOURCE_TRACKBALL(0x00010000U or AINPUT_SOURCE_CLASS_NAVIGATION.value),
    AINPUT_SOURCE_MOUSE_RELATIVE(0x00020000U or AINPUT_SOURCE_CLASS_NAVIGATION.value),
    AINPUT_SOURCE_TOUCHPAD(0x00100000U or AINPUT_SOURCE_CLASS_POSITION.value),
    AINPUT_SOURCE_TOUCH_NAVIGATION(0x00200000U or AINPUT_SOURCE_CLASS_NONE.value),
    AINPUT_SOURCE_JOYSTICK(0x01000000U or AINPUT_SOURCE_CLASS_JOYSTICK.value),
    AINPUT_SOURCE_HDMI(0x02000000U or AINPUT_SOURCE_CLASS_BUTTON.value),
    AINPUT_SOURCE_SENSOR(0x04000000U or AINPUT_SOURCE_CLASS_NONE.value),
    AINPUT_SOURCE_ROTARY_ENCODER(0x00400000U or AINPUT_SOURCE_CLASS_NONE.value),
    AINPUT_SOURCE_ANY(0xffffff00U)
}

fun isFromSource(source: UInt, test: AINPUT_SOURCE): Boolean {
    return source and test.value == test.value
}

fun transformAngle(transform: Transform, angleRadians: Float): Float {
    // Construct and transform a vector oriented at the specified clockwise angle from vertical.
    // Coordinate system: down is increasing Y, right is increasing X.
    val x = sin(angleRadians)
    val y = -cos(angleRadians)
    val transformedPoint = transform.transform(x, y)
    // Determine how the origin is transformed by the matrix so that we
    // can transform orientation vectors.
    val origin = transform.transform(0f, 0f)
    transformedPoint.x -= origin.x
    transformedPoint.y -= origin.y
    // Derive the transformed vector's clockwise angle from vertical.
    // The return value of atan2f is in range [-pi, pi] which conforms to the orientation API.
    return atan2(transformedPoint.x, -transformedPoint.y)
}

fun shouldDisregardTransformation(source: UInt): Boolean {
    return isFromSource(source, AINPUT_SOURCE.AINPUT_SOURCE_CLASS_JOYSTICK) ||
            isFromSource(source, AINPUT_SOURCE.AINPUT_SOURCE_CLASS_POSITION) ||
            isFromSource(source, AINPUT_SOURCE.AINPUT_SOURCE_MOUSE_RELATIVE)
}

fun shouldDisregardOffset(source: UInt): Boolean {
    // Pointer events are the only type of events that refer to absolute coordinates on the display,
    // so we should apply the entire window transform. For other types of events, we should make
    // sure to not apply the window translation/offset.
    return !isFromSource(source, AINPUT_SOURCE.AINPUT_SOURCE_CLASS_POINTER)
}

class MotionEventN {

    class PointerProperties {
        var id: Int
        var toolType: Int

        constructor() {
            id = -1
            toolType = 0
        }

        constructor(id: Int, toolType: Int) {
            this.id = id
            this.toolType = toolType
        }

        fun clear() {
            id = -1
            toolType = 0
        }
    }

    class PointerCoords {

        var values = FloatArray(MAX_AXES)
        var bits = Pointer(0U)

        companion object {
            const val MAX_AXES = 30

            inline fun scaleAxisValue(c: PointerCoords, axis: UInt, scaleFactor: Float) {
                val value = c.getAxisValue(axis)
                if (value != 0f) {
                    c.setAxisValue(axis, value * scaleFactor)
                }
            }
        }

        fun scale(globalScaleFactor: Float, windowXScale: Float, windowYScale: Float) {
            // No need to scale pressure or size since they are normalized.
            // No need to scale orientation since it is meaningless to do so.
            // If there is a global scale factor, it is included in the windowX/YScale
            // so we don't need to apply it twice to the X/Y axes.
            // However we don't want to apply any windowXYScale not included in the global scale
            // to the TOUCH_MAJOR/MINOR coordinates.
            scaleAxisValue(this, AXIS_X, windowXScale)
            scaleAxisValue(this, AXIS_Y, windowYScale)
            /*scaleAxisValue(this, AXIS_TOUCH_MAJOR, globalScaleFactor);
            scaleAxisValue(this, AXIS_TOUCH_MINOR, globalScaleFactor);
            scaleAxisValue(this, TOOL_MAJOR, globalScaleFactor);
            scaleAxisValue(this, TOOL_MINOR, globalScaleFactor);*/
            scaleAxisValue(this, AXIS_RELATIVE_X, windowXScale)
            scaleAxisValue(this, AXIS_RELATIVE_Y, windowYScale)
        }

        fun clear() {
            BitSet32.clear(bits)
        }

        fun isEmpty(): Boolean {
            return BitSet32.isEmpty(bits.v)
        }

        fun getAxisValue(axis: UInt): Float {
            return values[BitSet32.getIndexOfBit(bits.v, axis).toInt()]
        }

        fun setAxisValue(axis: UInt, value: Float): Int {
            val index = BitSet32.getIndexOfBit(bits.v, axis)
            if (!BitSet32.hasBit(bits.v, axis)) {
                if (value == 0f) {
                    return 0 // axes with value 0 do not need to be stored
                }
                val count = BitSet32.count(bits.v)
                if (count >= MAX_AXES) {
                    return -1
                }
                BitSet32.markBit(bits, axis)
                for(i in count downTo (index.toInt() + 1)) {
                    values[i] = values[i - 1]
                }
            }
            values[index.toInt()] = value
            return 0
        }

        fun getXYValue(): vec2 {
            return vec2(getX(), getY())
        }

        inline fun getX() : Float {
            return getAxisValue(AXIS_X)
        }
        inline fun getY() : Float {
            return getAxisValue(AXIS_Y)
        }

        fun transform(transform: Transform) {
            var xy = transform.transform(getXYValue())
            setAxisValue(AXIS_X, xy.x)
            setAxisValue(AXIS_Y, xy.y)

            if(BitSet32.hasBit(bits.v, AXIS_RELATIVE_X)
                || BitSet32.hasBit(bits.v, AXIS_RELATIVE_Y)) {
                var rotation = Transform(transform.getOrientation())
                var relativeXy = rotation.transform(getAxisValue(AXIS_RELATIVE_X),
                    getAxisValue(AXIS_RELATIVE_Y))
                setAxisValue(AXIS_RELATIVE_X, relativeXy.x)
                setAxisValue(AXIS_RELATIVE_Y, relativeXy.y)
            }

            if(BitSet32.hasBit(bits.v, AXIS_ORIENTATION)) {
                val v = getAxisValue(AXIS_ORIENTATION)
                setAxisValue(AXIS_ORIENTATION, transformAngle(transform, v))
            }
        }
    }

    var id = 0
    var source = 0U
    var action = 0
    var actionButton = 0
    var flags = 0
    var edgeFlags = 0
    var metaState = 0
    var buttonState = 0
    var classification = 0
    var transform = Transform()
    var mXPrecision = 0f
    var mYPrecision = 0f
    var mRawXCursorPosition = 0f
    var mRawYCursorPosition = 0f
    var rawTransform = Transform()
    var downTime = 0L
    var pointerProperties = mutableListOf<PointerProperties>()
    var sampleEventTimes = mutableListOf<Long>()
    var samplePointerCoords = mutableListOf<PointerCoords>()

    companion object {
        const val ACTION_MASK = 0xff
        const val ACTION_DOWN = 0
        const val ACTION_UP = 1
        const val ACTION_MOVE = 2
        const val ACTION_CANCEL = 3
        const val ACTION_OUTSIDE = 4
        const val ACTION_POINTER_DOWN = 5
        const val ACTION_POINTER_UP = 6
        const val ACTION_HOVER_MOVE = 7
        const val ACTION_SCROLL = 8
        const val ACTION_HOVER_ENTER = 9
        const val ACTION_HOVER_EXIT = 10
        const val ACTION_BUTTON_PRESS = 11
        const val ACTION_BUTTON_RELEASE = 12
        const val ACTION_POINTER_INDEX_MASK = 0xff00
        const val ACTION_POINTER_INDEX_SHIFT = 8

        const val AXIS_X = 0U
        const val AXIS_Y = 1U
        const val AXIS_RELATIVE_X = 27U
        const val AXIS_RELATIVE_Y = 28U
        const val AXIS_ORIENTATION = 8U

        const val HISTORY_CURRENT = -0x80000000;

        fun obtain(): MotionEventN = MotionEventN()

        fun validatePointerIndex(pointerIndex: Int, event: MotionEventN) : Boolean {
            if (pointerIndex < 0 || pointerIndex >= event.getPointerCount())
                return false
            return true
        }

        fun validatehistoricalIndex(historicalIndex: Int, event: MotionEventN) : Boolean {
            if (historicalIndex < 0 || historicalIndex >= event.getHistorySize())
                return false
            return true
        }

        fun getActionMasked(action: Int): Int { return action and ACTION_MASK }
    }

    fun initialize(id: Int, source: Int, action: Int, actionButton: Int,
        flags: Int, edgeFlags: Int, metaState: Int,
        buttonState: Int, classification: Int,
        transform: Transform, xPrecision: Float, yPrecision: Float,
        rawXCursorPosition: Float, rawYCursorPosition: Float,
        rawTransform: Transform, downTime: Long, eventTime: Long,
        pointerCount: Int, pointerProperties: MutableList<PointerProperties>,
        pointerCoords: MutableList<PointerCoords>) {
        this.id = id
        this.source = source.toUInt()
        this.action = action
        this.actionButton = actionButton
        this.flags = flags
        this.edgeFlags = edgeFlags
        this.metaState = metaState
        this.buttonState = buttonState
        this.classification = classification
        this.transform = transform
        this.mXPrecision = xPrecision
        this.mYPrecision = yPrecision
        this.mRawXCursorPosition = rawXCursorPosition
        this.mRawYCursorPosition = rawYCursorPosition
        this.rawTransform = rawTransform
        this.downTime = downTime
        this.pointerProperties.clear()
        this.pointerProperties.addAll(pointerProperties)
        sampleEventTimes.clear()
        samplePointerCoords.clear()
        addSample(eventTime, pointerCoords)
    }

    fun addSample(
        eventTime: Long,
        pointerCoords: MutableList<PointerCoords>) {
        sampleEventTimes.add(eventTime)
        samplePointerCoords.addAll(pointerCoords)
    }

    fun getType(): Int { return 0 }
    fun getActonMasked(): Int { return Companion.getActionMasked(action) }

    inline fun getPointerProperties(pointerIndex: Int): PointerProperties {
        return pointerProperties[pointerIndex];
    }

    inline fun getXOffset(): Float { return transform.tx() }
    inline fun getYOffset(): Float { return transform.ty() }

    fun getXCursorPosition() : Float {
        val vals = transform.transform(mRawXCursorPosition, mRawYCursorPosition)
        return roundTransformedCoords(vals.x)
    }
    fun getYCursorPosition() : Float {
        val vals = transform.transform(mRawXCursorPosition, mRawYCursorPosition)
        return roundTransformedCoords(vals.y)
    }

    fun setCursorPosition(x: Float, y: Float) {
        val inverse = transform.inverse()
        val vals = inverse.transform(x, y)
        mRawXCursorPosition = vals.x
        mRawYCursorPosition = vals.y
    }

    fun getRawPointerCoords(pointerIndex: Int): PointerCoords {
        return samplePointerCoords[getHistorySize() * getPointerCount() + pointerIndex]
    }

    fun getHistoricalRawPointerCoords(pointerIndex: Int, historicalIndex: Int): PointerCoords {
        return samplePointerCoords[historicalIndex * getPointerCount() + pointerIndex]
    }

    fun getRawAxisValue(axis: UInt, pointerIndex: Int): Float {
        return getHistoricalRawAxisValue(axis, pointerIndex, getHistorySize())
    }

    fun getAxisValue(axis: UInt, pointerIndex: Int): Float {
        return getHistoricalAxisValue(axis, pointerIndex, getHistorySize())
    }

    fun getHistoricalRawAxisValue(axis: UInt, pointerIndex: Int, historicalIndex: Int): Float {
        val coords = getHistoricalRawPointerCoords(pointerIndex, historicalIndex)
        return calculateTransformedAxisValue(axis, source, rawTransform, coords)
    }

    fun getHistoricalAxisValue(axis: UInt, pointerIndex: Int, historicalIndex: Int): Float {
        val coords = getHistoricalRawPointerCoords(pointerIndex, historicalIndex)
        return calculateTransformedAxisValue(axis, source, transform, coords)
    }

    // Due to precision limitations when working with floating points, transforming - namely
    // scaling - floating points can lead to minute errors. We round transformed values to approximately
    // three decimal places so that values like 0.99997 show up as 1.0.
    inline fun roundTransformedCoords(value: Float): Float {
        // Use a power to two to approximate three decimal places to potentially reduce some cycles.
        // This should be at least as precise as MotionEvent::ROUNDING_PRECISION.
        return round(value * 1024.0f) / 1024.0f
    }

    inline fun roundTransformedCoords(p: vec2) : vec2 {
        return vec2(roundTransformedCoords(p.x), roundTransformedCoords(p.y))
    }

    private fun transformWithoutTranslation(transform: Transform, xy: vec2) : vec2 {
        val transformedXy = transform.transform(xy)
        val transformedOrigin = transform.transform(0f, 0f);
        return roundTransformedCoords(transformedXy - transformedOrigin);
    }

    private fun calculateTransformedXYUnchecked(source: UInt, transform: Transform, xy: vec2): vec2 {
        return if(shouldDisregardTransformation(source)) transformWithoutTranslation(transform, xy)
        else roundTransformedCoords(transform.transform(xy))
    }

    private fun calculateTransformedAxisValue(
        axis: UInt,
        source: UInt,
        transform: Transform,
        coords: PointerCoords
    ): Float {
        if (shouldDisregardTransformation(source)) {
            return coords.getAxisValue(axis)
        }

        if (axis == AXIS_X || axis == AXIS_Y) {
            val xy = calculateTransformedXYUnchecked(source, transform, coords.getXYValue())
            return xy[axis]
        }
        if (axis == AXIS_RELATIVE_X || axis == AXIS_RELATIVE_Y) {
            val relativeXy =
                transformWithoutTranslation(transform,
                    vec2(coords.getAxisValue(AXIS_RELATIVE_X),
                        coords.getAxisValue(AXIS_RELATIVE_Y)))
            return if(axis == AXIS_RELATIVE_X) relativeXy.x else relativeXy.y
        }
        if (axis == AXIS_ORIENTATION) {
            return transformAngle(transform, coords.getAxisValue(AXIS_ORIENTATION))
        }
        return coords.getAxisValue(axis)
    }

    fun getActionMasked(): Int {
        return action and ACTION_MASK
    }

    fun getActionIndex(): Int {
        return (action and ACTION_POINTER_INDEX_MASK
                shr ACTION_POINTER_INDEX_SHIFT)
    }

    inline fun getPointerId(pointerIndex: Int): Int {
        return pointerProperties[pointerIndex].id
    }

    inline fun getToolType(pointerIndex: Int): Int {
        return pointerProperties[pointerIndex].toolType
    }

    inline fun getEventTime() : Long { return sampleEventTimes[getHistorySize()] }

    fun findPointerIndex(pointerId: Int) : Int {
        val pointerCount = pointerProperties.size
        for(i in 0 until pointerCount) {
            if(pointerProperties[id].id == pointerId)
                return i
        }
        return -1
    }

    fun getSurfaceRotation() : Int {
        // The surface rotation is the rotation from the window's coordinate space to that of the
        // display. Since the event's transform takes display space coordinates to window space, the
        // returned surface rotation is the inverse of the rotation for the surface.
        return when (transform.getOrientation()) {
            Transform.RotationFlags.ROT_0.value -> DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_0.value
            Transform.RotationFlags.ROT_90.value -> DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_270.value
            Transform.RotationFlags.ROT_180.value -> DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_180.value
            Transform.RotationFlags.ROT_270.value -> DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_90.value
            else -> -1
        }
    }

    fun getPointerCount(): Int {
        return pointerProperties.size
    }

    fun getHistorySize(): Int {
        return sampleEventTimes.size - 1
    }

    fun getHistoricalEventTime(historicalIndex: Int): Long {
        return sampleEventTimes[historicalIndex]
    }

    fun getRawX() : Float {
        return getRawX(0)
    }

    fun getRawY() : Float {
        return getRawY(0)
    }

    fun getRawX(pointerIndex: Int) : Float {
        return getRawAxisValue(AXIS_X, pointerIndex)
    }

    fun getRawY(pointerIndex: Int) : Float {
        return getRawAxisValue(AXIS_Y, pointerIndex)
    }

    fun isTouchEvent(source: UInt, action: Int): Boolean {
        if (isFromSource(source, AINPUT_SOURCE.AINPUT_SOURCE_CLASS_POINTER)) {
            // Specifically excludes HOVER_MOVE and SCROLL.
            when (action and ACTION_MASK) {
                ACTION_DOWN, ACTION_MOVE, ACTION_UP, ACTION_POINTER_DOWN, ACTION_POINTER_UP, ACTION_CANCEL, ACTION_OUTSIDE -> return true
            }
        }
        return false
    }

    inline fun isTouchEvent(): Boolean {
        return isTouchEvent(source, action)
    }

    fun offsetLocation(xOffset: Float, yOffset: Float) {
        val currXOffset: Float = transform.tx()
        val currYOffset: Float = transform.ty()
        transform.set(currXOffset + xOffset, currYOffset + yOffset)
    }

    fun scale(globalScaleFactor: Float) {
        transform.set(transform.tx() * globalScaleFactor, transform.ty() * globalScaleFactor)
        rawTransform.set(
            rawTransform.tx() * globalScaleFactor,
            rawTransform.ty() * globalScaleFactor
        )
        mXPrecision *= globalScaleFactor
        mYPrecision *= globalScaleFactor
        val numSamples = samplePointerCoords.size
        for (i in 0 until numSamples) {
            samplePointerCoords[i].scale(globalScaleFactor, globalScaleFactor, globalScaleFactor)
        }
    }

    fun transform(arr: FloatArray) {
        val newTransform = Transform()
        newTransform.set(arr)
        transform = newTransform * transform
    }

    fun applyTransform(arr: FloatArray) {
        val newTransform = Transform()
        newTransform.set(arr)

        samplePointerCoords.forEach {
            it.transform(newTransform)
        }

        /*if (rawXCursorPosition != INVALID_CURSOR_POSITION &&
            rawYCursorPosition != INVALID_CURSOR_POSITION) {
            const vec2 cursor = transform.transform(mRawXCursorPosition, mRawYCursorPosition);
            mRawXCursorPosition = cursor.x;
            mRawYCursorPosition = cursor.y;
        }*/
    }

    fun copyFrom(other: MotionEventN, keepHistory: Boolean)
    {
        this.id = other.id
        this.source = other.source
        this.action = other.action
        this.actionButton = other.actionButton
        this.flags = other.flags
        this.edgeFlags = other.edgeFlags
        this.metaState = other.metaState
        this.buttonState = other.buttonState
        this.classification = other.classification
        this.transform = other.transform
        this.mXPrecision = other.mXPrecision
        this.mYPrecision = other.mYPrecision
        this.mRawXCursorPosition = other.mRawXCursorPosition
        this.mRawYCursorPosition = other.mRawYCursorPosition
        this.rawTransform = other.rawTransform
        this.downTime = other.downTime
        this.pointerProperties = other.pointerProperties
        if(keepHistory) {
            this.sampleEventTimes = other.sampleEventTimes
            this.samplePointerCoords = other.samplePointerCoords
        } else {
            sampleEventTimes.clear()
            sampleEventTimes.add(other.getEventTime())
            samplePointerCoords.clear()
            samplePointerCoords.addAll(other.samplePointerCoords)
        }
    }
}