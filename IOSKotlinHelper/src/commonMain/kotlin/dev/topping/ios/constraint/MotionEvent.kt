package dev.topping.ios.constraint

import org.jetbrains.skia.Matrix33
import kotlin.math.*

fun Int.toHexString() : String {
    return this.toString()
}

class MotionEvent private constructor() {
    private lateinit var mNativePtr: MotionEventN
    private var mNext: MotionEvent? = null

    private fun initialize(
        source: Int,
        action: Int,
        flags: Int,
        edgeFlags: Int,
        metaState: Int,
        buttonState: Int,
        classification: Int,
        xOffset: Float,
        yOffset: Float,
        xPrecision: Float,
        yPrecision: Float,
        downTimeNanos: Long,
        eventTimeNanos: Long,
        pointerCount: Int,
        pointerIds: Array<PointerProperties>,
        pointerCoords: Array<PointerCoords>
    ) {
        mNativePtr = MotionEventN()
        nativeInitialize(
            mNativePtr, source, action, flags,
            edgeFlags, metaState, buttonState, classification, xOffset, yOffset,
            xPrecision, yPrecision, downTimeNanos, eventTimeNanos, pointerCount, pointerIds,
            pointerCoords
        )
        updateCursorPosition()
    }

    fun copy(): MotionEvent {
        return obtain(this)
    }

    /**
     * Applies a scale factor to all points within this event.
     *
     * This method is used to adjust touch events to simulate different density
     * displays for compatibility mode.  The values returned by [.getRawX],
     * [.getRawY], [.getXPrecision] and [.getYPrecision]
     * are also affected by the scale factor.
     *
     * @param scale The scale factor to apply.
     * @hide
     */
    fun scale(scale: Float) {
        if (scale != 1.0f) {
            mNativePtr.scale(scale)
        }
    }

    /** @hide
     */
    val id: Int
        get() = nativeGetId(mNativePtr)

    var source: Int
        get() = nativeGetSource(mNativePtr)
        set(source) {
            if (source == source) {
                return
            }
            nativeSetSource(mNativePtr, source)
            updateCursorPosition()
        }

    /**
     * Return the kind of action being performed.
     * Consider using [.getActionMasked] and [.getActionIndex] to retrieve
     * the separate masked action and pointer index.
     * @return The action, such as [.ACTION_DOWN] or
     * the combination of [.ACTION_POINTER_DOWN] with a shifted pointer index.
     */
    /**
     * Sets this event's action.
     */
    var action: Int
        get() = nativeGetAction(mNativePtr)
        set(action) {
            nativeSetAction(mNativePtr, action)
        }

    /**
     * Return the masked action being performed, without pointer index information.
     * Use [.getActionIndex] to return the index associated with pointer actions.
     * @return The action, such as [.ACTION_DOWN] or [.ACTION_POINTER_DOWN].
     */
    val actionMasked: Int
        get() = nativeGetAction(mNativePtr) and ACTION_MASK

    /**
     * For [.ACTION_POINTER_DOWN] or [.ACTION_POINTER_UP]
     * as returned by [.getActionMasked], this returns the associated
     * pointer index.
     * The index may be used with [.getPointerId],
     * [.getX], [.getY], [.getPressure],
     * and [.getSize] to get information about the pointer that has
     * gone down or up.
     * @return The index associated with the action.
     */
    val actionIndex: Int
        get() = ((nativeGetAction(mNativePtr) and ACTION_POINTER_INDEX_MASK)
                shr ACTION_POINTER_INDEX_SHIFT)

    /**
     * Returns true if this motion event is a touch event.
     *
     *
     * Specifically excludes pointer events with action [.ACTION_HOVER_MOVE],
     * [.ACTION_HOVER_ENTER], [.ACTION_HOVER_EXIT], or [.ACTION_SCROLL]
     * because they are not actually touch events (the pointer is not down).
     *
     * @return True if this motion event is a touch event.
     * @hide
     */
    val isTouchEvent: Boolean
        get() {
            return nativeIsTouchEvent(mNativePtr)
        }

    /**
     * Gets the motion event flags.
     *
     * @see .FLAG_WINDOW_IS_OBSCURED
     */
    val flags: Int
        get() {
            return nativeGetFlags(mNativePtr)
        }
    /** @hide
     */
    /** @hide
     */
    var isTainted: Boolean
        get() {
            val flags: Int = flags
            return (flags and FLAG_TAINTED) != 0
        }
        set(tainted) {
            val flags: Int = flags
            nativeSetFlags(
                mNativePtr,
                if (tainted) flags or FLAG_TAINTED else flags and FLAG_TAINTED.inv()
            )
        }
    /** @hide
     */
    /** @hide
     */
    var isTargetAccessibilityFocus: Boolean
        get() {
            val flags: Int = flags
            return (flags and FLAG_TARGET_ACCESSIBILITY_FOCUS) != 0
        }
        set(targetsFocus) {
            val flags: Int = flags
            nativeSetFlags(
                mNativePtr,
                if (targetsFocus) flags or FLAG_TARGET_ACCESSIBILITY_FOCUS else flags and FLAG_TARGET_ACCESSIBILITY_FOCUS.inv()
            )
        }
    /** @hide
     */
    /** @hide
     */
    var isHoverExitPending: Boolean
        get() {
            val flags: Int = flags
            return (flags and FLAG_HOVER_EXIT_PENDING) != 0
        }
        set(hoverExitPending) {
            val flags: Int = flags
            nativeSetFlags(
                mNativePtr,
                if (hoverExitPending) flags or FLAG_HOVER_EXIT_PENDING else flags and FLAG_HOVER_EXIT_PENDING.inv()
            )
        }
    /**
     * Returns the time (in ms) when the user originally pressed down to start
     * a stream of position events.
     */
    /**
     * Sets the time (in ms) when the user originally pressed down to start
     * a stream of position events.
     *
     * @hide
     */
    var downTime: Long
        get() {
            return nativeGetDownTimeNanos(mNativePtr) / NS_PER_MS
        }
        set(downTime) {
            nativeSetDownTimeNanos(mNativePtr, downTime * NS_PER_MS)
        }

    /**
     * Retrieve the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base.
     *
     * @return Returns the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base.
     */
    val eventTime: Long
        get() {
            return nativeGetEventTimeNanos(mNativePtr, HISTORY_CURRENT) / NS_PER_MS
        }

    /**
     * Retrieve the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base but with
     * nanosecond precision.
     *
     *
     * The value is in nanosecond precision but it may not have nanosecond accuracy.
     *
     *
     * @return Returns the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base but with
     * nanosecond precision.
     *
     * @hide
     */
    val eventTimeNano: Long
        get() {
            return nativeGetEventTimeNanos(mNativePtr, HISTORY_CURRENT)
        }

    /**
     * Equivalent to [.getX] for pointer index 0 (regardless of the
     * pointer identifier).
     *
     * @return The X coordinate of the first pointer index in the coordinate
     * space of the view that received this motion event.
     *
     * @see .AXIS_X
     */
    val x: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_X, 0, HISTORY_CURRENT)
        }

    /**
     * Equivalent to [.getY] for pointer index 0 (regardless of the
     * pointer identifier).
     *
     * @return The Y coordinate of the first pointer index in the coordinate
     * space of the view that received this motion event.
     *
     * @see .AXIS_Y
     */
    val y: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_Y, 0, HISTORY_CURRENT)
        }

    /**
     * [.getPressure] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @see .AXIS_PRESSURE
     */
    val pressure: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_PRESSURE, 0, HISTORY_CURRENT)
        }

    /**
     * [.getSize] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @see .AXIS_SIZE
     */
    val size: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_SIZE, 0, HISTORY_CURRENT)
        }

    /**
     * [.getTouchMajor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @see .AXIS_TOUCH_MAJOR
     */
    val touchMajor: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MAJOR, 0, HISTORY_CURRENT)
        }

    /**
     * [.getTouchMinor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @see .AXIS_TOUCH_MINOR
     */
    val touchMinor: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MINOR, 0, HISTORY_CURRENT)
        }

    /**
     * [.getToolMajor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @see .AXIS_TOOL_MAJOR
     */
    val toolMajor: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MAJOR, 0, HISTORY_CURRENT)
        }

    /**
     * [.getToolMinor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @see .AXIS_TOOL_MINOR
     */
    val toolMinor: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MINOR, 0, HISTORY_CURRENT)
        }

    /**
     * [.getOrientation] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @see .AXIS_ORIENTATION
     */
    val orientation: Float
        get() {
            return nativeGetAxisValue(mNativePtr, AXIS_ORIENTATION, 0, HISTORY_CURRENT)
        }

    /**
     * [.getAxisValue] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param axis The axis identifier for the axis value to retrieve.
     *
     * @see .AXIS_X
     *
     * @see .AXIS_Y
     */
    fun getAxisValue(axis: Int): Float {
        return nativeGetAxisValue(mNativePtr, axis, 0, HISTORY_CURRENT)
    }

    /**
     * The number of pointers of data contained in this event.  Always
     * >= 1.
     */
    val pointerCount: Int
        get() {
            return nativeGetPointerCount(mNativePtr)
        }

    /**
     * Return the pointer identifier associated with a particular pointer
     * data index in this event.  The identifier tells you the actual pointer
     * number associated with the data, accounting for individual pointers
     * going up and down since the start of the current gesture.
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     */
    fun getPointerId(pointerIndex: Int): Int {
        return nativeGetPointerId(mNativePtr, pointerIndex)
    }

    /**
     * Gets the tool type of a pointer for the given pointer index.
     * The tool type indicates the type of tool used to make contact such
     * as a finger or stylus, if known.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @return The tool type of the pointer.
     *
     * @see .TOOL_TYPE_UNKNOWN
     *
     * @see .TOOL_TYPE_FINGER
     *
     * @see .TOOL_TYPE_STYLUS
     *
     * @see .TOOL_TYPE_MOUSE
     */
    fun getToolType(pointerIndex: Int): Int {
        return nativeGetToolType(mNativePtr, pointerIndex)
    }

    /**
     * Given a pointer identifier, find the index of its data in the event.
     *
     * @param pointerId The identifier of the pointer to be found.
     * @return Returns either the index of the pointer (for use with
     * [.getX] et al.), or -1 if there is no data available for
     * that pointer identifier.
     */
    fun findPointerIndex(pointerId: Int): Int {
        return nativeFindPointerIndex(mNativePtr, pointerId)
    }

    /**
     * Returns the X coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The coordinate is in the
     * coordinate space of the view that received this motion event.
     *
     *
     * Use [.getPointerId] to get the pointer identifier for the
     * pointer referenced by `pointerIndex`.
     *
     * @param pointerIndex Index of the pointer for which the X coordinate is
     * returned. May be a value in the range of 0 (the first pointer that
     * is down) to [.getPointerCount] - 1.
     * @return The X coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The unit is pixels. The
     * value may contain a fractional portion for devices that are subpixel
     * precise.
     *
     * @see .AXIS_X
     */
    fun getX(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_X, pointerIndex, HISTORY_CURRENT)
    }

    fun getX(): Float {
        return x
    }

    /**
     * Returns the Y coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The coordinate is in the
     * coordinate space of the view that received this motion event.
     *
     *
     * Use [.getPointerId] to get the pointer identifier for the
     * pointer referenced by `pointerIndex`.
     *
     * @param pointerIndex Index of the pointer for which the Y coordinate is
     * returned. May be a value in the range of 0 (the first pointer that
     * is down) to [.getPointerCount] - 1.
     * @return The Y coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The unit is pixels. The
     * value may contain a fractional portion for devices that are subpixel
     * precise.
     *
     * @see .AXIS_Y
     */
    fun getY(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_Y, pointerIndex, HISTORY_CURRENT)
    }

    fun getY(): Float {
        return y
    }

    /**
     * Returns the current pressure of this event for the given pointer
     * *index* (use [.getPointerId] to find the pointer
     * identifier for this index).
     * The pressure generally
     * ranges from 0 (no pressure at all) to 1 (normal pressure), however
     * values higher than 1 may be generated depending on the calibration of
     * the input device.
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     *
     * @see .AXIS_PRESSURE
     */
    fun getPressure(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_PRESSURE, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Returns a scaled value of the approximate size for the given pointer
     * *index* (use [.getPointerId] to find the pointer
     * identifier for this index).
     * This represents some approximation of the area of the screen being
     * pressed; the actual value in pixels corresponding to the
     * touch is normalized with the device specific range of values
     * and scaled to a value between 0 and 1. The value of size can be used to
     * determine fat touch events.
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     *
     * @see .AXIS_SIZE
     */
    fun getSize(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_SIZE, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Returns the length of the major axis of an ellipse that describes the touch
     * area at the point of contact for the given pointer
     * *index* (use [.getPointerId] to find the pointer
     * identifier for this index).
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     *
     * @see .AXIS_TOUCH_MAJOR
     */
    fun getTouchMajor(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MAJOR, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Returns the length of the minor axis of an ellipse that describes the touch
     * area at the point of contact for the given pointer
     * *index* (use [.getPointerId] to find the pointer
     * identifier for this index).
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     *
     * @see .AXIS_TOUCH_MINOR
     */
    fun getTouchMinor(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MINOR, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Returns the length of the major axis of an ellipse that describes the size of
     * the approaching tool for the given pointer
     * *index* (use [.getPointerId] to find the pointer
     * identifier for this index).
     * The tool area represents the estimated size of the finger or pen that is
     * touching the device independent of its actual touch area at the point of contact.
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     *
     * @see .AXIS_TOOL_MAJOR
     */
    fun getToolMajor(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MAJOR, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Returns the length of the minor axis of an ellipse that describes the size of
     * the approaching tool for the given pointer
     * *index* (use [.getPointerId] to find the pointer
     * identifier for this index).
     * The tool area represents the estimated size of the finger or pen that is
     * touching the device independent of its actual touch area at the point of contact.
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     *
     * @see .AXIS_TOOL_MINOR
     */
    fun getToolMinor(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MINOR, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Returns the orientation of the touch area and tool area in radians clockwise from vertical
     * for the given pointer *index* (use [.getPointerId] to find the pointer
     * identifier for this index).
     * An angle of 0 radians indicates that the major axis of contact is oriented
     * upwards, is perfectly circular or is of unknown orientation.  A positive angle
     * indicates that the major axis of contact is oriented to the right.  A negative angle
     * indicates that the major axis of contact is oriented to the left.
     * The full range is from -PI/2 radians (finger pointing fully left) to PI/2 radians
     * (finger pointing fully right).
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     *
     * @see .AXIS_ORIENTATION
     */
    fun getOrientation(pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_ORIENTATION, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Returns the value of the requested axis for the given pointer *index*
     * (use [.getPointerId] to find the pointer identifier for this index).
     *
     * @param axis The axis identifier for the axis value to retrieve.
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @return The value of the axis, or 0 if the axis is not available.
     *
     * @see .AXIS_X
     *
     * @see .AXIS_Y
     */
    fun getAxisValue(axis: Int, pointerIndex: Int): Float {
        return nativeGetAxisValue(mNativePtr, axis, pointerIndex, HISTORY_CURRENT)
    }

    /**
     * Populates a [PointerCoords] object with pointer coordinate data for
     * the specified pointer index.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param outPointerCoords The pointer coordinate object to populate.
     *
     * @see PointerCoords
     */
    fun getPointerCoords(pointerIndex: Int, outPointerCoords: PointerCoords?) {
        nativeGetPointerCoords(mNativePtr, pointerIndex, HISTORY_CURRENT, outPointerCoords)
    }

    /**
     * Populates a [PointerProperties] object with pointer properties for
     * the specified pointer index.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param outPointerProperties The pointer properties object to populate.
     *
     * @see PointerProperties
     */
    fun getPointerProperties(
        pointerIndex: Int,
        outPointerProperties: PointerProperties?
    ) {
        nativeGetPointerProperties(mNativePtr, pointerIndex, outPointerProperties)
    }

    /**
     * Returns the state of any meta / modifier keys that were in effect when
     * the event was generated.  This is the same values as those
     * returned by [KeyEvent.getMetaState].
     *
     * @return an integer in which each bit set to 1 represents a pressed
     * meta key
     *
     * @see KeyEvent.getMetaState
     */
    val metaState: Int
        get() {
            return nativeGetMetaState(mNativePtr)
        }
    /**
     * Gets the state of all buttons that are pressed such as a mouse or stylus button.
     *
     * @return The button state.
     *
     * @see .BUTTON_PRIMARY
     *
     * @see .BUTTON_SECONDARY
     *
     * @see .BUTTON_TERTIARY
     *
     * @see .BUTTON_FORWARD
     *
     * @see .BUTTON_BACK
     *
     * @see .BUTTON_STYLUS_PRIMARY
     *
     * @see .BUTTON_STYLUS_SECONDARY
     */
    /**
     * Sets the bitfield indicating which buttons are pressed.
     *
     * @see .getButtonState
     * @hide
     */
    var buttonState: Int
        get() {
            return nativeGetButtonState(mNativePtr)
        }
        set(buttonState) {
            nativeSetButtonState(mNativePtr, buttonState)
        }

    /**
     * Returns the classification for the current gesture.
     * The classification may change as more events become available for the same gesture.
     *
     * @see .CLASSIFICATION_NONE
     *
     * @see .CLASSIFICATION_AMBIGUOUS_GESTURE
     *
     * @see .CLASSIFICATION_DEEP_PRESS
     */
    val classification: Int
        get() {
            return nativeGetClassification(mNativePtr)
        }
    /**
     * Gets which button has been modified during a press or release action.
     *
     * For actions other than [.ACTION_BUTTON_PRESS] and [.ACTION_BUTTON_RELEASE]
     * the returned value is undefined.
     *
     * @see .getButtonState
     */
    /**
     * Sets the action button for the event.
     *
     * @see .getActionButton
     * @hide
     */
    var actionButton: Int
        get() {
            return nativeGetActionButton(mNativePtr)
        }
        set(button) {
            nativeSetActionButton(mNativePtr, button)
        }

    /**
     * Equivalent to [.getRawX] for pointer index 0 (regardless of
     * the pointer identifier).
     *
     * @return The X coordinate of the first pointer index in the coordinate
     * space of the device display.
     *
     * @see .getX
     * @see .AXIS_X
     */
    val rawX: Float
        get() {
            return nativeGetRawAxisValue(mNativePtr, AXIS_X, 0, HISTORY_CURRENT)
        }

    /**
     * Equivalent to [.getRawY] for pointer index 0 (regardless of
     * the pointer identifier).
     *
     * @return The Y coordinate of the first pointer index in the coordinate
     * space of the device display.
     *
     * @see .getY
     * @see .AXIS_Y
     */
    val rawY: Float
        get() {
            return nativeGetRawAxisValue(mNativePtr, AXIS_Y, 0, HISTORY_CURRENT)
        }

    /**
     * Returns the X coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The coordinate is in the
     * coordinate space of the device display, irrespective of system
     * decorations and whether or not the system is in multi-window mode. If the
     * app spans multiple screens in a multiple-screen environment, the
     * coordinate space includes all of the spanned screens.
     *
     *
     * In multi-window mode, the coordinate space extends beyond the bounds
     * of the app window to encompass the entire display area. For example, if
     * the motion event occurs in the right-hand window of split-screen mode in
     * landscape orientation, the left edge of the screennot the left
     * edge of the windowis the origin from which the X coordinate is
     * calculated.
     *
     *
     * In multiple-screen scenarios, the coordinate space can span screens.
     * For example, if the app is spanning both screens of a dual-screen device,
     * and the motion event occurs on the right-hand screen, the X coordinate is
     * calculated from the left edge of the left-hand screen to the point of the
     * motion event on the right-hand screen. When the app is restricted to a
     * single screen in a multiple-screen environment, the coordinate space
     * includes only the screen on which the app is running.
     *
     *
     * Use [.getPointerId] to get the pointer identifier for the
     * pointer referenced by `pointerIndex`.
     *
     * @param pointerIndex Index of the pointer for which the X coordinate is
     * returned. May be a value in the range of 0 (the first pointer that
     * is down) to [.getPointerCount] - 1.
     * @return The X coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The unit is pixels. The
     * value may contain a fractional portion for devices that are subpixel
     * precise.
     *
     * @see .getX
     * @see .AXIS_X
     */
    fun getRawX(pointerIndex: Int): Float {
        return nativeGetRawAxisValue(mNativePtr, AXIS_X, pointerIndex, HISTORY_CURRENT)
    }

    fun getRawX(): Float {
        return rawX
    }

    /**
     * Returns the Y coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The coordinate is in the
     * coordinate space of the device display, irrespective of system
     * decorations and whether or not the system is in multi-window mode. If the
     * app spans multiple screens in a multiple-screen environment, the
     * coordinate space includes all of the spanned screens.
     *
     *
     * In multi-window mode, the coordinate space extends beyond the bounds
     * of the app window to encompass the entire device screen. For example, if
     * the motion event occurs in the lower window of split-screen mode in
     * portrait orientation, the top edge of the screennot the top edge
     * of the windowis the origin from which the Y coordinate is
     * determined.
     *
     *
     * In multiple-screen scenarios, the coordinate space can span screens.
     * For example, if the app is spanning both screens of a dual-screen device
     * that's rotated 90 degrees, and the motion event occurs on the lower
     * screen, the Y coordinate is calculated from the top edge of the upper
     * screen to the point of the motion event on the lower screen. When the app
     * is restricted to a single screen in a multiple-screen environment, the
     * coordinate space includes only the screen on which the app is running.
     *
     *
     * Use [.getPointerId] to get the pointer identifier for the
     * pointer referenced by `pointerIndex`.
     *
     * @param pointerIndex Index of the pointer for which the Y coordinate is
     * returned. May be a value in the range of 0 (the first pointer that
     * is down) to [.getPointerCount] - 1.
     * @return The Y coordinate of the pointer referenced by
     * `pointerIndex` for this motion event. The unit is pixels. The
     * value may contain a fractional portion for devices that are subpixel
     * precise.
     *
     * @see .getY
     * @see .AXIS_Y
     */
    fun getRawY(pointerIndex: Int): Float {
        return nativeGetRawAxisValue(mNativePtr, AXIS_Y, pointerIndex, HISTORY_CURRENT)
    }

    fun getRawY(): Float {
        return rawY
    }

    /**
     * Return the precision of the X coordinates being reported.  You can
     * multiply this number with [.getX] to find the actual hardware
     * value of the X coordinate.
     * @return Returns the precision of X coordinates being reported.
     *
     * @see .AXIS_X
     */
    val xPrecision: Float
        get() {
            return nativeGetXPrecision(mNativePtr)
        }

    /**
     * Return the precision of the Y coordinates being reported.  You can
     * multiply this number with [.getY] to find the actual hardware
     * value of the Y coordinate.
     * @return Returns the precision of Y coordinates being reported.
     *
     * @see .AXIS_Y
     */
    val yPrecision: Float
        get() {
            return nativeGetYPrecision(mNativePtr)
        }

    /**
     * Returns the x coordinate of mouse cursor position when this event is
     * reported. This value is only valid if [.getSource] returns
     * [InputDevice.SOURCE_MOUSE].
     *
     * @hide
     */
    val xCursorPosition: Float
        get() {
            return nativeGetXCursorPosition(mNativePtr)
        }

    /**
     * Returns the y coordinate of mouse cursor position when this event is
     * reported. This value is only valid if [.getSource] returns
     * [InputDevice.SOURCE_MOUSE].
     *
     * @hide
     */
    val yCursorPosition: Float
        get() {
            return nativeGetYCursorPosition(mNativePtr)
        }

    /**
     * Sets cursor position to given coordinates. The coordinate in parameters should be after
     * offsetting. In other words, the effect of this function is [.getXCursorPosition] and
     * [.getYCursorPosition] will return the same value passed in the parameters.
     *
     * @hide
     */
    private fun setCursorPosition(x: Float, y: Float) {
        nativeSetCursorPosition(mNativePtr, x, y)
    }

    /**
     * Returns the number of historical points in this event.  These are
     * movements that have occurred between this event and the previous event.
     * This only applies to ACTION_MOVE events -- all other actions will have
     * a size of 0.
     *
     * @return Returns the number of historical points in the event.
     */
    val historySize: Int
        get() {
            return nativeGetHistorySize(mNativePtr)
        }

    /**
     * Returns the time that a historical movement occurred between this event
     * and the previous event, in the [android.os.SystemClock.uptimeMillis] time base.
     *
     *
     * This only applies to ACTION_MOVE events.
     *
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     * @return Returns the time that a historical movement occurred between this
     * event and the previous event,
     * in the [android.os.SystemClock.uptimeMillis] time base.
     *
     * @see .getHistorySize
     *
     * @see .getEventTime
     */
    fun getHistoricalEventTime(pos: Int): Long {
        return nativeGetEventTimeNanos(mNativePtr, pos) / NS_PER_MS
    }

    /**
     * Returns the time that a historical movement occurred between this event
     * and the previous event, in the [android.os.SystemClock.uptimeMillis] time base
     * but with nanosecond (instead of millisecond) precision.
     *
     *
     * This only applies to ACTION_MOVE events.
     *
     *
     * The value is in nanosecond precision but it may not have nanosecond accuracy.
     *
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     * @return Returns the time that a historical movement occurred between this
     * event and the previous event,
     * in the [android.os.SystemClock.uptimeMillis] time base but with
     * nanosecond (instead of millisecond) precision.
     *
     * @see .getHistorySize
     *
     * @see .getEventTime
     *
     *
     * @hide
     */
    fun getHistoricalEventTimeNano(pos: Int): Long {
        return nativeGetEventTimeNanos(mNativePtr, pos)
    }

    /**
     * [.getHistoricalX] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getX
     * @see .AXIS_X
     */
    fun getHistoricalX(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_X, 0, pos)
    }

    /**
     * [.getHistoricalY] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getY
     * @see .AXIS_Y
     */
    fun getHistoricalY(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_Y, 0, pos)
    }

    /**
     * [.getHistoricalPressure] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getPressure
     * @see .AXIS_PRESSURE
     */
    fun getHistoricalPressure(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_PRESSURE, 0, pos)
    }

    /**
     * [.getHistoricalSize] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getSize
     * @see .AXIS_SIZE
     */
    fun getHistoricalSize(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_SIZE, 0, pos)
    }

    /**
     * [.getHistoricalTouchMajor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getTouchMajor
     * @see .AXIS_TOUCH_MAJOR
     */
    fun getHistoricalTouchMajor(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MAJOR, 0, pos)
    }

    /**
     * [.getHistoricalTouchMinor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getTouchMinor
     * @see .AXIS_TOUCH_MINOR
     */
    fun getHistoricalTouchMinor(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MINOR, 0, pos)
    }

    /**
     * [.getHistoricalToolMajor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getToolMajor
     * @see .AXIS_TOOL_MAJOR
     */
    fun getHistoricalToolMajor(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MAJOR, 0, pos)
    }

    /**
     * [.getHistoricalToolMinor] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getToolMinor
     * @see .AXIS_TOOL_MINOR
     */
    fun getHistoricalToolMinor(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MINOR, 0, pos)
    }

    /**
     * [.getHistoricalOrientation] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getOrientation
     * @see .AXIS_ORIENTATION
     */
    fun getHistoricalOrientation(pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_ORIENTATION, 0, pos)
    }

    /**
     * [.getHistoricalAxisValue] for the first pointer index (may be an
     * arbitrary pointer identifier).
     *
     * @param axis The axis identifier for the axis value to retrieve.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getAxisValue
     * @see .AXIS_X
     *
     * @see .AXIS_Y
     */
    fun getHistoricalAxisValue(axis: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, axis, 0, pos)
    }

    /**
     * Returns a historical X coordinate, as per [.getX], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getX
     * @see .AXIS_X
     */
    fun getHistoricalX(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_X, pointerIndex, pos)
    }

    /**
     * Returns a historical Y coordinate, as per [.getY], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getY
     * @see .AXIS_Y
     */
    fun getHistoricalY(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_Y, pointerIndex, pos)
    }

    /**
     * Returns a historical pressure coordinate, as per [.getPressure],
     * that occurred between this event and the previous event for the given
     * pointer.  Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getPressure
     * @see .AXIS_PRESSURE
     */
    fun getHistoricalPressure(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_PRESSURE, pointerIndex, pos)
    }

    /**
     * Returns a historical size coordinate, as per [.getSize], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getSize
     * @see .AXIS_SIZE
     */
    fun getHistoricalSize(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_SIZE, pointerIndex, pos)
    }

    /**
     * Returns a historical touch major axis coordinate, as per [.getTouchMajor], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getTouchMajor
     * @see .AXIS_TOUCH_MAJOR
     */
    fun getHistoricalTouchMajor(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MAJOR, pointerIndex, pos)
    }

    /**
     * Returns a historical touch minor axis coordinate, as per [.getTouchMinor], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getTouchMinor
     * @see .AXIS_TOUCH_MINOR
     */
    fun getHistoricalTouchMinor(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MINOR, pointerIndex, pos)
    }

    /**
     * Returns a historical tool major axis coordinate, as per [.getToolMajor], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getToolMajor
     * @see .AXIS_TOOL_MAJOR
     */
    fun getHistoricalToolMajor(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MAJOR, pointerIndex, pos)
    }

    /**
     * Returns a historical tool minor axis coordinate, as per [.getToolMinor], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getToolMinor
     * @see .AXIS_TOOL_MINOR
     */
    fun getHistoricalToolMinor(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MINOR, pointerIndex, pos)
    }

    /**
     * Returns a historical orientation coordinate, as per [.getOrientation], that
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     *
     * @see .getHistorySize
     *
     * @see .getOrientation
     * @see .AXIS_ORIENTATION
     */
    fun getHistoricalOrientation(pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, AXIS_ORIENTATION, pointerIndex, pos)
    }

    /**
     * Returns the historical value of the requested axis, as per [.getAxisValue],
     * occurred between this event and the previous event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param axis The axis identifier for the axis value to retrieve.
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     * @return The value of the axis, or 0 if the axis is not available.
     *
     * @see .AXIS_X
     *
     * @see .AXIS_Y
     */
    fun getHistoricalAxisValue(axis: Int, pointerIndex: Int, pos: Int): Float {
        return nativeGetAxisValue(mNativePtr, axis, pointerIndex, pos)
    }

    /**
     * Populates a [PointerCoords] object with historical pointer coordinate data,
     * as per [.getPointerCoords], that occurred between this event and the previous
     * event for the given pointer.
     * Only applies to ACTION_MOVE events.
     *
     * @param pointerIndex Raw index of pointer to retrieve.  Value may be from 0
     * (the first pointer that is down) to [.getPointerCount]-1.
     * @param pos Which historical value to return; must be less than
     * [.getHistorySize]
     * @param outPointerCoords The pointer coordinate object to populate.
     *
     * @see .getHistorySize
     *
     * @see .getPointerCoords
     *
     * @see PointerCoords
     */
    fun getHistoricalPointerCoords(
        pointerIndex: Int, pos: Int,
        outPointerCoords: PointerCoords?
    ) {
        nativeGetPointerCoords(mNativePtr, pointerIndex, pos, outPointerCoords)
    }
    /**
     * Returns a bitfield indicating which edges, if any, were touched by this
     * MotionEvent. For touch events, clients can use this to determine if the
     * user's finger was touching the edge of the display.
     *
     * This property is only set for [.ACTION_DOWN] events.
     *
     * @see .EDGE_LEFT
     *
     * @see .EDGE_TOP
     *
     * @see .EDGE_RIGHT
     *
     * @see .EDGE_BOTTOM
     */
    /**
     * Sets the bitfield indicating which edges, if any, were touched by this
     * MotionEvent.
     *
     * @see .getEdgeFlags
     */
    var edgeFlags: Int
        get() {
            return nativeGetEdgeFlags(mNativePtr)
        }
        set(flags) {
            nativeSetEdgeFlags(mNativePtr, flags)
        }

    /**
     * Adjust this event's location.
     * @param deltaX Amount to add to the current X coordinate of the event.
     * @param deltaY Amount to add to the current Y coordinate of the event.
     */
    fun offsetLocation(deltaX: Float, deltaY: Float) {
        if (deltaX != 0.0f || deltaY != 0.0f) {
            nativeOffsetLocation(mNativePtr, deltaX, deltaY)
        }
    }

    /**
     * Set this event's location.  Applies [.offsetLocation] with a
     * delta from the current location to the given new location.
     *
     * @param x New absolute X location.
     * @param y New absolute Y location.
     */
    fun setLocation(x: Float, y: Float) {
        val oldX: Float = x
        val oldY: Float = y
        offsetLocation(x - oldX, y - oldY)
    }

    /**
     * Applies a transformation matrix to all of the points in the event.
     *
     * @param matrix The transformation matrix to apply.
     */
    fun transform(matrix: Matrix33?) {
        if (matrix == null) {
            throw Exception("matrix must not be null")
        }
        nativeTransform(mNativePtr, matrix)
    }

    /**
     * Transforms all of the points in the event directly instead of modifying the event's
     * internal transform.
     *
     * @param matrix The transformation matrix to apply.
     * @hide
     */
    fun applyTransform(matrix: Matrix33?) {
        if (matrix == null) {
            throw Exception("matrix must not be null")
        }
        nativeApplyTransform(mNativePtr, matrix)
    }

    /**
     * Add a new movement to the batch of movements in this event.  The event's
     * current location, position and size is updated to the new values.
     * The current values in the event are added to a list of historical values.
     *
     * Only applies to [.ACTION_MOVE] or [.ACTION_HOVER_MOVE] events.
     *
     * @param eventTime The time stamp (in ms) for this data.
     * @param x The new X position.
     * @param y The new Y position.
     * @param pressure The new pressure.
     * @param size The new size.
     * @param metaState Meta key state.
     */
    fun addBatch(
        eventTime: Long, x: Float, y: Float,
        pressure: Float, size: Float, metaState: Int
    ) {
        //synchronized(gSharedTempLock) {
            ensureSharedTempPointerCapacity(1)
            val pc: Array<PointerCoords> =
                gSharedTempPointerCoords
            pc.get(0).clear()
            pc.get(0).x = x
            pc.get(0).y = y
            pc.get(0).pressure = pressure
            pc.get(0).size = size
            nativeAddBatch(
                mNativePtr,
                eventTime * NS_PER_MS,
                pc,
                metaState
            )
        //}
    }

    /**
     * Add a new movement to the batch of movements in this event.  The event's
     * current location, position and size is updated to the new values.
     * The current values in the event are added to a list of historical values.
     *
     * Only applies to [.ACTION_MOVE] or [.ACTION_HOVER_MOVE] events.
     *
     * @param eventTime The time stamp (in ms) for this data.
     * @param pointerCoords The new pointer coordinates.
     * @param metaState Meta key state.
     */
    fun addBatch(eventTime: Long, pointerCoords: Array<PointerCoords>, metaState: Int) {
        nativeAddBatch(mNativePtr, eventTime * NS_PER_MS, pointerCoords, metaState)
    }

    /**
     * Adds all of the movement samples of the specified event to this one if
     * it is compatible.  To be compatible, the event must have the same device id,
     * source, display id, action, flags, classification, pointer count, pointer properties.
     *
     * Only applies to [.ACTION_MOVE] or [.ACTION_HOVER_MOVE] events.
     *
     * @param event The event whose movements samples should be added to this one
     * if possible.
     * @return True if batching was performed or false if batching was not possible.
     * @hide
     */
    fun addBatch(event: MotionEvent): Boolean {
        val action: Int = nativeGetAction(mNativePtr)
        if (action != ACTION_MOVE && action != ACTION_HOVER_MOVE) {
            return false
        }
        if (action != nativeGetAction(event.mNativePtr)) {
            return false
        }
        if ((nativeGetSource(mNativePtr) != nativeGetSource(event.mNativePtr)
                    ) || (nativeGetFlags(mNativePtr) != nativeGetFlags(event.mNativePtr)
                    ) || ((nativeGetClassification(mNativePtr)
                    != nativeGetClassification(event.mNativePtr)))
        ) {
            return false
        }
        val pointerCount: Int = nativeGetPointerCount(mNativePtr)
        if (pointerCount != nativeGetPointerCount(event.mNativePtr)) {
            return false
        }
        //synchronized(gSharedTempLock) {
            ensureSharedTempPointerCapacity(
                max(
                    pointerCount,
                    2
                )
            )
            val pp: Array<PointerProperties> =
                gSharedTempPointerProperties
            val pc: Array<PointerCoords> =
                gSharedTempPointerCoords
            for (i in 0 until pointerCount) {
                nativeGetPointerProperties(mNativePtr, i, pp.get(0))
                nativeGetPointerProperties(event.mNativePtr, i, pp.get(1))
                if (!pp.get(0).equals(pp.get(1))) {
                    return false
                }
            }
            val metaState: Int = nativeGetMetaState(event.mNativePtr)
            val historySize: Int =
                nativeGetHistorySize(event.mNativePtr)
            for (h in 0..historySize) {
                val historyPos: Int =
                    (if (h == historySize) HISTORY_CURRENT else h)
                for (i in 0 until pointerCount) {
                    nativeGetPointerCoords(
                        event.mNativePtr,
                        i,
                        historyPos,
                        pc.get(i)
                    )
                }
                val eventTimeNanos: Long =
                    nativeGetEventTimeNanos(event.mNativePtr, historyPos)
                nativeAddBatch(mNativePtr, eventTimeNanos, pc, metaState)
            }
        //}
        return true
    }

    /**
     * Returns true if all points in the motion event are completely within the specified bounds.
     * @hide
     */
    fun isWithinBoundsNoHistory(
        left: Float, top: Float,
        right: Float, bottom: Float
    ): Boolean {
        val pointerCount: Int = nativeGetPointerCount(mNativePtr)
        for (i in 0 until pointerCount) {
            val x: Float = nativeGetAxisValue(mNativePtr, AXIS_X, i, HISTORY_CURRENT)
            val y: Float = nativeGetAxisValue(mNativePtr, AXIS_Y, i, HISTORY_CURRENT)
            if ((x < left) || (x > right) || (y < top) || (y > bottom)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns a new motion events whose points have been clamped to the specified bounds.
     * @hide
     */
    fun clampNoHistory(left: Float, top: Float, right: Float, bottom: Float): MotionEvent {
        val ev: MotionEvent = obtain()
        //synchronized(gSharedTempLock) {
            val pointerCount: Int = nativeGetPointerCount(mNativePtr)
            ensureSharedTempPointerCapacity(pointerCount)
            val pp: Array<PointerProperties> =
                gSharedTempPointerProperties
            val pc: Array<PointerCoords> =
                gSharedTempPointerCoords
            for (i in 0 until pointerCount) {
                nativeGetPointerProperties(mNativePtr, i, pp.get(i))
                nativeGetPointerCoords(
                    mNativePtr,
                    i,
                    HISTORY_CURRENT,
                    pc.get(i)
                )
                pc.get(i).x = clamp(pc.get(i).x, left, right)
                pc.get(i).y = clamp(pc.get(i).y, top, bottom)
            }
            ev.initialize(
                nativeGetSource(mNativePtr),
                nativeGetAction(mNativePtr),
                nativeGetFlags(mNativePtr),
                nativeGetEdgeFlags(mNativePtr),
                nativeGetMetaState(mNativePtr),
                nativeGetButtonState(mNativePtr),
                nativeGetClassification(mNativePtr),
                nativeGetXOffset(mNativePtr),
                nativeGetYOffset(mNativePtr),
                nativeGetXPrecision(mNativePtr),
                nativeGetYPrecision(mNativePtr),
                nativeGetDownTimeNanos(mNativePtr),
                nativeGetEventTimeNanos(
                    mNativePtr,
                    HISTORY_CURRENT
                ),
                pointerCount,
                pp,
                pc
            )
            return ev
        //}
    }

    /**
     * Gets an integer where each pointer id present in the event is marked as a bit.
     * @hide
     */
    val pointerIdBits: Int
        get() {
            var idBits: Int = 0
            val pointerCount: Int = nativeGetPointerCount(mNativePtr)
            for (i in 0 until pointerCount) {
                idBits = idBits or (1 shl nativeGetPointerId(mNativePtr, i))
            }
            return idBits
        }

    /**
     * Splits a motion event such that it includes only a subset of pointer ids.
     * @hide
     */
    fun split(idBits: Int): MotionEvent {
        val ev: MotionEvent = obtain()
        //synchronized(gSharedTempLock) {
            val oldPointerCount: Int =
                nativeGetPointerCount(mNativePtr)
            ensureSharedTempPointerCapacity(oldPointerCount)
            val pp: Array<PointerProperties> =
                gSharedTempPointerProperties
            val pc: Array<PointerCoords> =
                gSharedTempPointerCoords
            val map: IntArray = gSharedTempPointerIndexMap
            val oldAction: Int = nativeGetAction(mNativePtr)
            val oldActionMasked: Int = oldAction and ACTION_MASK
            val oldActionPointerIndex: Int =
                ((oldAction and ACTION_POINTER_INDEX_MASK)
                        shr ACTION_POINTER_INDEX_SHIFT)
            var newActionPointerIndex: Int = -1
            var newPointerCount: Int = 0
            for (i in 0 until oldPointerCount) {
                nativeGetPointerProperties(
                    mNativePtr,
                    i,
                    pp.get(newPointerCount)
                )
                val idBit: Int = 1 shl pp.get(newPointerCount).id
                if ((idBit and idBits) != 0) {
                    if (i == oldActionPointerIndex) {
                        newActionPointerIndex = newPointerCount
                    }
                    map[newPointerCount] = i
                    newPointerCount += 1
                }
            }
            if (newPointerCount == 0) {
                throw Exception("idBits did not match any ids in the event")
            }
            val newAction: Int
            if (oldActionMasked == ACTION_POINTER_DOWN || oldActionMasked == ACTION_POINTER_UP) {
                if (newActionPointerIndex < 0) {
                    // An unrelated pointer changed.
                    newAction = ACTION_MOVE
                } else if (newPointerCount == 1) {
                    // The first/last pointer went down/up.
                    newAction =
                        if (oldActionMasked == ACTION_POINTER_DOWN) ACTION_DOWN else if ((flags and FLAG_CANCELED) == 0) ACTION_UP else ACTION_CANCEL
                } else {
                    // A secondary pointer went down/up.
                    newAction = (oldActionMasked
                            or (newActionPointerIndex shl ACTION_POINTER_INDEX_SHIFT))
                }
            } else {
                // Simple up/down/cancel/move or other motion action.
                newAction = oldAction
            }
            val historySize: Int = nativeGetHistorySize(mNativePtr)
            for (h in 0..historySize) {
                val historyPos: Int =
                    if (h == historySize) HISTORY_CURRENT else h
                for (i in 0 until newPointerCount) {
                    nativeGetPointerCoords(
                        mNativePtr,
                        map.get(i),
                        historyPos,
                        pc.get(i)
                    )
                }
                val eventTimeNanos: Long =
                    nativeGetEventTimeNanos(mNativePtr, historyPos)
                if (h == 0) {
                    ev.initialize(
                        nativeGetSource(mNativePtr),
                        newAction,
                        nativeGetFlags(mNativePtr),
                        nativeGetEdgeFlags(mNativePtr),
                        nativeGetMetaState(mNativePtr),
                        nativeGetButtonState(mNativePtr),
                        nativeGetClassification(mNativePtr),
                        nativeGetXOffset(mNativePtr),
                        nativeGetYOffset(mNativePtr),
                        nativeGetXPrecision(mNativePtr),
                        nativeGetYPrecision(mNativePtr),
                        nativeGetDownTimeNanos(mNativePtr),
                        eventTimeNanos,
                        newPointerCount,
                        pp,
                        pc
                    )
                } else {
                    nativeAddBatch(ev.mNativePtr, eventTimeNanos, pc, 0)
                }
            }
            return ev
        //}
    }

    /**
     * Calculate new cursor position for events from mouse. This is used to split, clamp and inject
     * events.
     *
     *
     * If the source is mouse, it sets cursor position to the centroid of all pointers because
     * InputReader maps multiple fingers on a touchpad to locations around cursor position in screen
     * coordinates so that the mouse cursor is at the centroid of all pointers.
     *
     *
     * If the source is not mouse it sets cursor position to NaN.
     */
    private fun updateCursorPosition() {
        if (source != AINPUT_SOURCE.AINPUT_SOURCE_MOUSE.value.toInt()) {
            setCursorPosition(INVALID_CURSOR_POSITION, INVALID_CURSOR_POSITION)
            return
        }
        var x: Float = 0f
        var y: Float = 0f
        val pointerCount: Int = pointerCount
        for (i in 0 until pointerCount) {
            x += getX(i)
            y += getY(i)
        }
        // If pointer count is 0, divisions below yield NaN, which is an acceptable result for this
        // corner case.
        x /= pointerCount.toFloat()
        y /= pointerCount.toFloat()
        setCursorPosition(x, y)
    }

    override fun toString(): String {
        val msg = StringBuilder()
        msg.append("MotionEvent { action=").append(actionToString(action))
        appendUnless(
            "0", msg, ", actionButton=", buttonStateToString(
                actionButton
            )
        )
        val pointerCount: Int = pointerCount
        for (i in 0 until pointerCount) {
            appendUnless(i, msg, ", id[$i]=", getPointerId(i))
            val x: Float = getX(i)
            val y: Float = getY(i)
            if (!DEBUG_CONCISE_TOSTRING || (x != 0f) || (y != 0f)) {
                msg.append(", x[").append(i).append("]=").append(x)
                msg.append(", y[").append(i).append("]=").append(y)
            }
        }
        appendUnless(
            "0", msg, ", buttonState=", buttonStateToString(
                buttonState
            )
        )
        appendUnless(
            classificationToString(CLASSIFICATION_NONE), msg, ", classification=",
            classificationToString(classification)
        )
        appendUnless(
            "0", msg, ", metaState=",
                metaState
        )
        appendUnless(
            "0", msg, ", flags=0x",
                flags.toHexString()
        )
        appendUnless(
            "0", msg, ", edgeFlags=0x",
                edgeFlags.toHexString()
        )
        appendUnless(1, msg, ", pointerCount=", pointerCount)
        appendUnless(0, msg, ", historySize=", historySize)
        msg.append(", eventTime=").append(eventTime)
        if (!DEBUG_CONCISE_TOSTRING) {
            msg.append(", downTime=").append(downTime)
            msg.append(", source=0x").append(source.toHexString())
            msg.append(", eventId=").append(id)
        }
        msg.append(" }")
        return msg.toString()
    }

    /**
     * Checks if a mouse or stylus button (or combination of buttons) is pressed.
     * @param button Button (or combination of buttons).
     * @return True if specified buttons are pressed.
     *
     * @see .BUTTON_PRIMARY
     *
     * @see .BUTTON_SECONDARY
     *
     * @see .BUTTON_TERTIARY
     *
     * @see .BUTTON_FORWARD
     *
     * @see .BUTTON_BACK
     *
     * @see .BUTTON_STYLUS_PRIMARY
     *
     * @see .BUTTON_STYLUS_SECONDARY
     */
    fun isButtonPressed(button: Int): Boolean {
        if (button == 0) {
            return false
        }
        return (buttonState and button) == button
    }

    /**
     * Gets the rotation value of the transform for this MotionEvent.
     *
     * This MotionEvent's rotation can be changed by passing a rotation matrix to
     * [.transform] to change the coordinate space of this event.
     *
     * @return the rotation value, or -1 if unknown or invalid.
     * @see Surface.Rotation
     *
     * @see .createRotateMatrix
     * @hide
     */
    val surfaceRotation: Int
        get() {
            return nativeGetSurfaceRotation(mNativePtr)
        }

    /** @hide
     */
    fun cancel() {
        action = ACTION_CANCEL
    }

    fun getAction(): Int {
        return action
    }

    /**
     * Transfer object for pointer coordinates.
     *
     * Objects of this type can be used to specify the pointer coordinates when
     * creating new [MotionEvent] objects and to query pointer coordinates
     * in bulk.
     *
     * Refer to [InputDevice] for information about how different kinds of
     * input devices and sources represent pointer coordinates.
     */
    class PointerCoords {
        var mPackedAxisBits: UInt = 0U

        var mPackedAxisValues: FloatArray? = null

        /**
         * Creates a pointer coords object with all axes initialized to zero.
         */
        constructor() {}

        /**
         * Creates a pointer coords object as a copy of the
         * contents of another pointer coords object.
         *
         * @param other The pointer coords object to copy.
         */
        constructor(other: PointerCoords) {
            copyFrom(other)
        }

        /**
         * The X component of the pointer movement.
         *
         * @see MotionEvent.AXIS_X
         */
        var x: Float = 0f

        /**
         * The Y component of the pointer movement.
         *
         * @see MotionEvent.AXIS_Y
         */
        var y: Float = 0f

        /**
         * A normalized value that describes the pressure applied to the device
         * by a finger or other tool.
         * The pressure generally ranges from 0 (no pressure at all) to 1 (normal pressure),
         * although values higher than 1 may be generated depending on the calibration of
         * the input device.
         *
         * @see MotionEvent.AXIS_PRESSURE
         */
        var pressure: Float = 0f

        /**
         * A normalized value that describes the approximate size of the pointer touch area
         * in relation to the maximum detectable size of the device.
         * It represents some approximation of the area of the screen being
         * pressed; the actual value in pixels corresponding to the
         * touch is normalized with the device specific range of values
         * and scaled to a value between 0 and 1. The value of size can be used to
         * determine fat touch events.
         *
         * @see MotionEvent.AXIS_SIZE
         */
        var size: Float = 0f

        /**
         * The length of the major axis of an ellipse that describes the touch area at
         * the point of contact.
         * If the device is a touch screen, the length is reported in pixels, otherwise it is
         * reported in device-specific units.
         *
         * @see MotionEvent.AXIS_TOUCH_MAJOR
         */
        var touchMajor: Float = 0f

        /**
         * The length of the minor axis of an ellipse that describes the touch area at
         * the point of contact.
         * If the device is a touch screen, the length is reported in pixels, otherwise it is
         * reported in device-specific units.
         *
         * @see MotionEvent.AXIS_TOUCH_MINOR
         */
        var touchMinor: Float = 0f

        /**
         * The length of the major axis of an ellipse that describes the size of
         * the approaching tool.
         * The tool area represents the estimated size of the finger or pen that is
         * touching the device independent of its actual touch area at the point of contact.
         * If the device is a touch screen, the length is reported in pixels, otherwise it is
         * reported in device-specific units.
         *
         * @see MotionEvent.AXIS_TOOL_MAJOR
         */
        var toolMajor: Float = 0f

        /**
         * The length of the minor axis of an ellipse that describes the size of
         * the approaching tool.
         * The tool area represents the estimated size of the finger or pen that is
         * touching the device independent of its actual touch area at the point of contact.
         * If the device is a touch screen, the length is reported in pixels, otherwise it is
         * reported in device-specific units.
         *
         * @see MotionEvent.AXIS_TOOL_MINOR
         */
        var toolMinor: Float = 0f

        /**
         * The orientation of the touch area and tool area in radians clockwise from vertical.
         * An angle of 0 radians indicates that the major axis of contact is oriented
         * upwards, is perfectly circular or is of unknown orientation.  A positive angle
         * indicates that the major axis of contact is oriented to the right.  A negative angle
         * indicates that the major axis of contact is oriented to the left.
         * The full range is from -PI/2 radians (finger pointing fully left) to PI/2 radians
         * (finger pointing fully right).
         *
         * @see MotionEvent.AXIS_ORIENTATION
         */
        var orientation: Float = 0f

        /**
         * The movement of x position of a motion event.
         *
         * @see MotionEvent.AXIS_RELATIVE_X
         *
         * @hide
         */
        var relativeX: Float = 0f

        /**
         * The movement of y position of a motion event.
         *
         * @see MotionEvent.AXIS_RELATIVE_Y
         *
         * @hide
         */
        var relativeY: Float = 0f

        /**
         * Clears the contents of this object.
         * Resets all axes to zero.
         */
        fun clear() {
            mPackedAxisBits = 0U
            x = 0f
            y = 0f
            pressure = 0f
            size = 0f
            touchMajor = 0f
            touchMinor = 0f
            toolMajor = 0f
            toolMinor = 0f
            orientation = 0f
            relativeX = 0f
            relativeY = 0f
        }

        /**
         * Copies the contents of another pointer coords object.
         *
         * @param other The pointer coords object to copy.
         */
        fun copyFrom(other: PointerCoords) {
            val bits = other.mPackedAxisBits
            mPackedAxisBits = bits
            if (bits != 0U) {
                val otherValues: FloatArray? = other.mPackedAxisValues
                val count: Int = bits.countOneBits()
                var values: FloatArray? = mPackedAxisValues
                if (values == null || count > values.size) {
                    values = FloatArray(otherValues!!.size)
                    mPackedAxisValues = values
                }
                Arrays.arraycopy(otherValues!!, 0, values, 0, count)
            }
            x = other.x
            y = other.y
            pressure = other.pressure
            size = other.size
            touchMajor = other.touchMajor
            touchMinor = other.touchMinor
            toolMajor = other.toolMajor
            toolMinor = other.toolMinor
            orientation = other.orientation
            relativeX = other.relativeX
            relativeY = other.relativeY
        }

        /**
         * Gets the value associated with the specified axis.
         *
         * @param axis The axis identifier for the axis value to retrieve.
         * @return The value associated with the axis, or 0 if none.
         *
         * @see MotionEvent.AXIS_X
         *
         * @see MotionEvent.AXIS_Y
         */
        fun getAxisValue(axis: Int): Float {
            when (axis) {
                AXIS_X -> return x
                AXIS_Y -> return y
                AXIS_PRESSURE -> return pressure
                AXIS_SIZE -> return size
                AXIS_TOUCH_MAJOR -> return touchMajor
                AXIS_TOUCH_MINOR -> return touchMinor
                AXIS_TOOL_MAJOR -> return toolMajor
                AXIS_TOOL_MINOR -> return toolMinor
                AXIS_ORIENTATION -> return orientation
                AXIS_RELATIVE_X -> return relativeX
                AXIS_RELATIVE_Y -> return relativeY
                else -> {
                    if (axis < 0 || axis > 63) {
                        throw Exception("Axis out of range.")
                    }
                    val bits = mPackedAxisBits.toInt()
                    val axisBit = (FLAG_TAINTED ushr axis)
                    if ((bits and axisBit) == 0) {
                        return 0f
                    }
                    val index: Int = (bits and (-0x1 ushr axis).inv()).countOneBits()
                    return mPackedAxisValues!!.get(index)
                }
            }
        }

        /**
         * Sets the value associated with the specified axis.
         *
         * @param axis The axis identifier for the axis value to assign.
         * @param value The value to set.
         *
         * @see MotionEvent.AXIS_X
         *
         * @see MotionEvent.AXIS_Y
         */
        fun setAxisValue(axis: Int, value: Float) {
            when (axis) {
                AXIS_X -> x = value
                AXIS_Y -> y = value
                AXIS_PRESSURE -> pressure = value
                AXIS_SIZE -> size = value
                AXIS_TOUCH_MAJOR -> touchMajor = value
                AXIS_TOUCH_MINOR -> touchMinor = value
                AXIS_TOOL_MAJOR -> toolMajor = value
                AXIS_TOOL_MINOR -> toolMinor = value
                AXIS_ORIENTATION -> orientation = value
                AXIS_RELATIVE_X -> relativeX = value
                AXIS_RELATIVE_Y -> relativeY = value
                else -> {
                    if (axis < 0 || axis > 63) {
                        throw Exception("Axis out of range.")
                    }
                    val bits = mPackedAxisBits.toInt()
                    val axisBit = (FLAG_TAINTED ushr axis)
                    val index = (bits and (-0x1 ushr axis).inv()).countOneBits()
                    var values: FloatArray? = mPackedAxisValues
                    if ((bits and axisBit) == 0) {
                        if (values == null) {
                            values = FloatArray(INITIAL_PACKED_AXIS_VALUES)
                            mPackedAxisValues = values
                        } else {
                            val count: Int = (bits).countOneBits()
                            if (count < values.size) {
                                if (index != count) {
                                    Arrays.arraycopy(
                                        values, index, values, index + 1,
                                        count - index
                                    )
                                }
                            } else {
                                val newValues: FloatArray = FloatArray(count * 2)
                                Arrays.arraycopy(values, 0, newValues, 0, index)
                                Arrays.arraycopy(
                                    values, index, newValues, index + 1,
                                    count - index
                                )
                                values = newValues
                                mPackedAxisValues = values
                            }
                        }
                        mPackedAxisBits = (bits or axisBit).toUInt()
                    }
                    values!![index] = value
                }
            }
        }

        companion object {
            private val INITIAL_PACKED_AXIS_VALUES: Int = 8
            const val AMOTION_EVENT_INVALID_CURSOR_POSITION = Float.NaN

            fun createArray(size: Int): Array<PointerCoords> {
                return Array<PointerCoords>(size) { PointerCoords() }
            }
        }
    }

    /**
     * Transfer object for pointer properties.
     *
     * Objects of this type can be used to specify the pointer id and tool type
     * when creating new [MotionEvent] objects and to query pointer properties in bulk.
     */
    class PointerProperties {
        /**
         * Creates a pointer properties object with an invalid pointer id.
         */
        constructor() {
            clear()
        }

        /**
         * Creates a pointer properties object as a copy of the contents of
         * another pointer properties object.
         * @param other
         */
        constructor(other: PointerProperties) {
            copyFrom(other)
        }

        /**
         * The pointer id.
         * Initially set to [.INVALID_POINTER_ID] (-1).
         *
         * @see MotionEvent.getPointerId
         */
        var id: Int = 0

        /**
         * The pointer tool type.
         * Initially set to 0.
         *
         * @see MotionEvent.getToolType
         */
        var toolType: Int = 0

        /**
         * Resets the pointer properties to their initial values.
         */
        fun clear() {
            id = INVALID_POINTER_ID
            toolType = TOOL_TYPE_UNKNOWN
        }

        /**
         * Copies the contents of another pointer properties object.
         *
         * @param other The pointer properties object to copy.
         */
        fun copyFrom(other: PointerProperties) {
            id = other.id
            toolType = other.toolType
        }

        override fun equals(other: Any?): Boolean {
            if (other is PointerProperties) {
                return equals(other as PointerProperties?)
            }
            return false
        }

        private fun equals(other: PointerProperties?): Boolean {
            return (other != null) && (id == other.id) && (toolType == other.toolType)
        }

        override fun hashCode(): Int {
            return id or (toolType shl 8)
        }

        companion object {
            /** @hide
             */
            fun createArray(size: Int): Array<PointerProperties> {
                return Array<PointerProperties>(size) { PointerProperties() }
            }
        }
    }

    companion object {
        private val TAG: String = "MotionEvent"
        private val NS_PER_MS: Long = 1000000
        private val LABEL_PREFIX: String = "AXIS_"
        private val DEBUG_CONCISE_TOSTRING: Boolean = false
        const val AMOTION_EVENT_INVALID_CURSOR_POSITION = Float.NaN

        fun pointerCoordsToNative(pointerCoordsObj: PointerCoords,
                                  xOffset: Float, yOffset: Float): MotionEventN.PointerCoords {
            var outRawPointerCoords = MotionEventN.PointerCoords()
            outRawPointerCoords.clear()
            outRawPointerCoords.setAxisValue(AXIS_X.toUInt(), pointerCoordsObj.x - xOffset)
            outRawPointerCoords.setAxisValue(AXIS_Y.toUInt(), pointerCoordsObj.y - yOffset)
            /*outRawPointerCoords->setAxisValue(AMOTION_EVENT_AXIS_PRESSURE,
            env->GetFloatField(pointerCoordsObj, gPointerCoordsClassInfo.pressure));
            outRawPointerCoords->setAxisValue(AMOTION_EVENT_AXIS_SIZE,
            env->GetFloatField(pointerCoordsObj, gPointerCoordsClassInfo.size));
            outRawPointerCoords->setAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR,
            env->GetFloatField(pointerCoordsObj, gPointerCoordsClassInfo.touchMajor));
            outRawPointerCoords->setAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR,
            env->GetFloatField(pointerCoordsObj, gPointerCoordsClassInfo.touchMinor));
            outRawPointerCoords->setAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR,
            env->GetFloatField(pointerCoordsObj, gPointerCoordsClassInfo.toolMajor));
            outRawPointerCoords->setAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR,
            env->GetFloatField(pointerCoordsObj, gPointerCoordsClassInfo.toolMinor));*/
            outRawPointerCoords.setAxisValue(AXIS_ORIENTATION.toUInt(), pointerCoordsObj.orientation)
            outRawPointerCoords.setAxisValue(AXIS_RELATIVE_X.toUInt(), pointerCoordsObj.relativeX)
            outRawPointerCoords.setAxisValue(AXIS_RELATIVE_Y.toUInt(), pointerCoordsObj.relativeY)
            var bits = BitSet32(pointerCoordsObj.mPackedAxisBits)
            if (!bits.isEmpty()) {
                if (pointerCoordsObj.mPackedAxisValues != null) {
                    var index = 0;
                    do {
                        var axis = bits.clearFirstMarkedBit()
                        outRawPointerCoords.setAxisValue(axis, pointerCoordsObj.mPackedAxisValues!![index++])
                    } while (!bits.isEmpty())
                }
            }

            return outRawPointerCoords
        }
        fun obtainPackedAxisValuesArray(pointerCoords: MotionEvent.PointerCoords): FloatArray? {
            return pointerCoords.mPackedAxisValues
        }
        fun pointerCoordsFromNative(rawPointerCoords: MotionEventN.PointerCoords,
                                    axesBitsToCopy: BitSet32, pointerCoords: PointerCoords) : PointerCoords {
            val bits = axesBitsToCopy
            var outBits = 0
            if (!bits.isEmpty()) {
                val packedAxesCount = bits.count()
                val outValues = obtainPackedAxisValuesArray(pointerCoords)
                if (outValues == null) {
                    return pointerCoords // OOM
                }
                var index = 0
                do {
                    var axis = bits.clearFirstMarkedBit()
                    outBits = outBits or BitSet32.valueForBit(axis).toInt()
                    outValues[index++] = rawPointerCoords.getAxisValue(axis)
                } while (!bits.isEmpty())
            }
            pointerCoords.mPackedAxisBits = outBits.toUInt()
            return pointerCoords
        }
        fun pointerPropertiesToNative(pointerPropertiesObj: PointerProperties, outPointerProperties: MotionEventN.PointerProperties) : MotionEventN.PointerProperties {
            outPointerProperties.clear();
            outPointerProperties.id = pointerPropertiesObj.id
            outPointerProperties.toolType = pointerPropertiesObj.toolType

            return outPointerProperties
        }
        fun pointerPropertiesFromNative(pointerPropertiesObj: MotionEventN.PointerProperties, outPointerProperties: PointerProperties) : PointerProperties {
            outPointerProperties.id = pointerPropertiesObj.id
            outPointerProperties.toolType = pointerPropertiesObj.toolType

            return outPointerProperties
        }

        /**
         * An invalid pointer id.
         *
         * This value (-1) can be used as a placeholder to indicate that a pointer id
         * has not been assigned or is not available.  It cannot appear as
         * a pointer id inside a [MotionEvent].
         */
        val INVALID_POINTER_ID: Int = -1

        /**
         * Bit mask of the parts of the action code that are the action itself.
         */
        val ACTION_MASK: Int = 0xff

        /**
         * Constant for [.getActionMasked]: A pressed gesture has started, the
         * motion contains the initial starting location.
         *
         *
         * This is also a good time to check the button state to distinguish
         * secondary and tertiary button clicks and handle them appropriately.
         * Use [.getButtonState] to retrieve the button state.
         *
         */
        val ACTION_DOWN: Int = 0

        /**
         * Constant for [.getActionMasked]: A pressed gesture has finished, the
         * motion contains the final release location as well as any intermediate
         * points since the last down or move event.
         */
        val ACTION_UP: Int = 1

        /**
         * Constant for [.getActionMasked]: A change has happened during a
         * press gesture (between [.ACTION_DOWN] and [.ACTION_UP]).
         * The motion contains the most recent point, as well as any intermediate
         * points since the last down or move event.
         */
        val ACTION_MOVE: Int = 2

        /**
         * Constant for [.getActionMasked]: The current gesture has been aborted.
         * You will not receive any more points in it.  You should treat this as
         * an up event, but not perform any action that you normally would.
         */
        val ACTION_CANCEL: Int = 3

        /**
         * Constant for [.getActionMasked]: A movement has happened outside of the
         * normal bounds of the UI element.  This does not provide a full gesture,
         * but only the initial location of the movement/touch.
         *
         *
         * Note: Because the location of any event will be outside the
         * bounds of the view hierarchy, it will not get dispatched to
         * any children of a ViewGroup by default. Therefore,
         * movements with ACTION_OUTSIDE should be handled in either the
         * root [TView] or in the appropriate [Window.Callback]
         * (e.g. [android.app.Activity] or [android.app.Dialog]).
         *
         */
        val ACTION_OUTSIDE: Int = 4

        /**
         * Constant for [.getActionMasked]: A non-primary pointer has gone down.
         *
         *
         * Use [.getActionIndex] to retrieve the index of the pointer that changed.
         *
         *
         * The index is encoded in the [.ACTION_POINTER_INDEX_MASK] bits of the
         * unmasked action returned by [.getAction].
         *
         */
        val ACTION_POINTER_DOWN: Int = 5

        /**
         * Constant for [.getActionMasked]: A non-primary pointer has gone up.
         *
         *
         * Use [.getActionIndex] to retrieve the index of the pointer that changed.
         *
         *
         * The index is encoded in the [.ACTION_POINTER_INDEX_MASK] bits of the
         * unmasked action returned by [.getAction].
         *
         */
        val ACTION_POINTER_UP: Int = 6

        /**
         * Constant for [.getActionMasked]: A change happened but the pointer
         * is not down (unlike [.ACTION_MOVE]).  The motion contains the most
         * recent point, as well as any intermediate points since the last
         * hover move event.
         *
         *
         * This action is always delivered to the window or view under the pointer.
         *
         *
         * This action is not a touch event so it is delivered to
         * [TView.onGenericMotionEvent] rather than
         * [TView.onTouchEvent].
         *
         */
        val ACTION_HOVER_MOVE: Int = 7

        /**
         * Constant for [.getActionMasked]: The motion event contains relative
         * vertical and/or horizontal scroll offsets.  Use [.getAxisValue]
         * to retrieve the information from [.AXIS_VSCROLL] and [.AXIS_HSCROLL].
         * The pointer may or may not be down when this event is dispatched.
         *
         *
         * This action is always delivered to the window or view under the pointer, which
         * may not be the window or view currently touched.
         *
         *
         * This action is not a touch event so it is delivered to
         * [TView.onGenericMotionEvent] rather than
         * [TView.onTouchEvent].
         *
         */
        val ACTION_SCROLL: Int = 8

        /**
         * Constant for [.getActionMasked]: The pointer is not down but has entered the
         * boundaries of a window or view.
         *
         *
         * This action is always delivered to the window or view under the pointer.
         *
         *
         * This action is not a touch event so it is delivered to
         * [TView.onGenericMotionEvent] rather than
         * [TView.onTouchEvent].
         *
         */
        val ACTION_HOVER_ENTER: Int = 9

        /**
         * Constant for [.getActionMasked]: The pointer is not down but has exited the
         * boundaries of a window or view.
         *
         *
         * This action is always delivered to the window or view that was previously under the pointer.
         *
         *
         * This action is not a touch event so it is delivered to
         * [TView.onGenericMotionEvent] rather than
         * [TView.onTouchEvent].
         *
         */
        val ACTION_HOVER_EXIT: Int = 10

        /**
         * Constant for [.getActionMasked]: A button has been pressed.
         *
         *
         *
         * Use [.getActionButton] to get which button was pressed.
         *
         *
         * This action is not a touch event so it is delivered to
         * [TView.onGenericMotionEvent] rather than
         * [TView.onTouchEvent].
         *
         */
        val ACTION_BUTTON_PRESS: Int = 11

        /**
         * Constant for [.getActionMasked]: A button has been released.
         *
         *
         *
         * Use [.getActionButton] to get which button was released.
         *
         *
         * This action is not a touch event so it is delivered to
         * [TView.onGenericMotionEvent] rather than
         * [TView.onTouchEvent].
         *
         */
        val ACTION_BUTTON_RELEASE: Int = 12

        /**
         * Bits in the action code that represent a pointer index, used with
         * [.ACTION_POINTER_DOWN] and [.ACTION_POINTER_UP].  Shifting
         * down by [.ACTION_POINTER_INDEX_SHIFT] provides the actual pointer
         * index where the data for the pointer going up or down can be found; you can
         * get its identifier with [.getPointerId] and the actual
         * data with [.getX] etc.
         *
         * @see .getActionIndex
         */
        val ACTION_POINTER_INDEX_MASK: Int = 0xff00

        /**
         * Bit shift for the action bits holding the pointer index as
         * defined by [.ACTION_POINTER_INDEX_MASK].
         *
         * @see .getActionIndex
         */
        val ACTION_POINTER_INDEX_SHIFT: Int = 8

        @Deprecated("Use {@link #ACTION_POINTER_INDEX_MASK} to retrieve the\n" + "      data index associated with {@link #ACTION_POINTER_DOWN}.")
        val ACTION_POINTER_1_DOWN: Int = ACTION_POINTER_DOWN or 0x0000

        @Deprecated("Use {@link #ACTION_POINTER_INDEX_MASK} to retrieve the\n" + "      data index associated with {@link #ACTION_POINTER_DOWN}.")
        val ACTION_POINTER_2_DOWN: Int = ACTION_POINTER_DOWN or 0x0100

        @Deprecated("Use {@link #ACTION_POINTER_INDEX_MASK} to retrieve the\n" + "      data index associated with {@link #ACTION_POINTER_DOWN}.")
        val ACTION_POINTER_3_DOWN: Int = ACTION_POINTER_DOWN or 0x0200

        @Deprecated("Use {@link #ACTION_POINTER_INDEX_MASK} to retrieve the\n" + "      data index associated with {@link #ACTION_POINTER_UP}.")
        val ACTION_POINTER_1_UP: Int = ACTION_POINTER_UP or 0x0000

        @Deprecated("Use {@link #ACTION_POINTER_INDEX_MASK} to retrieve the\n" + "      data index associated with {@link #ACTION_POINTER_UP}.")
        val ACTION_POINTER_2_UP: Int = ACTION_POINTER_UP or 0x0100

        @Deprecated("Use {@link #ACTION_POINTER_INDEX_MASK} to retrieve the\n" + "      data index associated with {@link #ACTION_POINTER_UP}.")
        val ACTION_POINTER_3_UP: Int = ACTION_POINTER_UP or 0x0200

        @Deprecated("Renamed to {@link #ACTION_POINTER_INDEX_MASK} to match\n" + "      the actual data contained in these bits.")
        val ACTION_POINTER_ID_MASK: Int = 0xff00

        @Deprecated("Renamed to {@link #ACTION_POINTER_INDEX_SHIFT} to match\n" + "      the actual data contained in these bits.")
        val ACTION_POINTER_ID_SHIFT: Int = 8

        /**
         * This flag indicates that the window that received this motion event is partly
         * or wholly obscured by another visible window above it and the event directly passed through
         * the obscured area.
         *
         * A security sensitive application can check this flag to identify situations in which
         * a malicious application may have covered up part of its content for the purpose
         * of misleading the user or hijacking touches.  An appropriate response might be
         * to drop the suspect touches or to take additional precautions to confirm the user's
         * actual intent.
         */
        val FLAG_WINDOW_IS_OBSCURED: Int = 0x1

        /**
         * This flag indicates that the window that received this motion event is partly
         * or wholly obscured by another visible window above it and the event did not directly pass
         * through the obscured area.
         *
         * A security sensitive application can check this flag to identify situations in which
         * a malicious application may have covered up part of its content for the purpose
         * of misleading the user or hijacking touches.  An appropriate response might be
         * to drop the suspect touches or to take additional precautions to confirm the user's
         * actual intent.
         *
         * Unlike FLAG_WINDOW_IS_OBSCURED, this is only true if the window that received this event is
         * obstructed in areas other than the touched location.
         */
        val FLAG_WINDOW_IS_PARTIALLY_OBSCURED: Int = 0x2

        /**
         * This private flag is only set on [.ACTION_HOVER_MOVE] events and indicates that
         * this event will be immediately followed by a [.ACTION_HOVER_EXIT]. It is used to
         * prevent generating redundant [.ACTION_HOVER_ENTER] events.
         * @hide
         */
        val FLAG_HOVER_EXIT_PENDING: Int = 0x4

        /**
         * This flag indicates that the event has been generated by a gesture generator. It
         * provides a hint to the GestureDetector to not apply any touch slop.
         *
         * @hide
         */
        val FLAG_IS_GENERATED_GESTURE: Int = 0x8

        /**
         * This flag is only set for events with [.ACTION_POINTER_UP] and [.ACTION_CANCEL].
         * It indicates that the pointer going up was an unintentional user touch. When FLAG_CANCELED
         * is set, the typical actions that occur in response for a pointer going up (such as click
         * handlers, end of drawing) should be aborted. This flag is typically set when the user was
         * accidentally touching the screen, such as by gripping the device, or placing the palm on the
         * screen.
         *
         * @see .ACTION_POINTER_UP
         *
         * @see .ACTION_CANCEL
         */
        val FLAG_CANCELED: Int = 0x20

        /**
         * This flag indicates that the event will not cause a focus change if it is directed to an
         * unfocused window, even if it an [.ACTION_DOWN]. This is typically used with pointer
         * gestures to allow the user to direct gestures to an unfocused window without bringing the
         * window into focus.
         * @hide
         */
        val FLAG_NO_FOCUS_CHANGE: Int = 0x40

        /**
         * Private flag that indicates when the system has detected that this motion event
         * may be inconsistent with respect to the sequence of previously delivered motion events,
         * such as when a pointer move event is sent but the pointer is not down.
         *
         * @hide
         * @see .isTainted
         *
         * @see .setTainted
         */
        val FLAG_TAINTED: Int = -0x80000000

        /**
         * Private flag indicating that this event was synthesized by the system and should be delivered
         * to the accessibility focused view first. When being dispatched such an event is not handled
         * by predecessors of the accessibility focused view and after the event reaches that view the
         * flag is cleared and normal event dispatch is performed. This ensures that the platform can
         * click on any view that has accessibility focus which is semantically equivalent to asking the
         * view to perform a click accessibility action but more generic as views not implementing click
         * action correctly can still be activated.
         *
         * @hide
         * @see .isTargetAccessibilityFocus
         * @see .setTargetAccessibilityFocus
         */
        val FLAG_TARGET_ACCESSIBILITY_FOCUS: Int = 0x40000000

        /**
         * Flag indicating the motion event intersected the top edge of the screen.
         */
        val EDGE_TOP: Int = 0x00000001

        /**
         * Flag indicating the motion event intersected the bottom edge of the screen.
         */
        val EDGE_BOTTOM: Int = 0x00000002

        /**
         * Flag indicating the motion event intersected the left edge of the screen.
         */
        val EDGE_LEFT: Int = 0x00000004

        /**
         * Flag indicating the motion event intersected the right edge of the screen.
         */
        val EDGE_RIGHT: Int = 0x00000008

        /**
         * Axis constant: X axis of a motion event.
         *
         *
         *
         *  * For a touch screen, reports the absolute X screen position of the center of
         * the touch contact area.  The units are display pixels.
         *  * For a touch pad, reports the absolute X surface position of the center of the touch
         * contact area.  The units are device-dependent; use [InputDevice.getMotionRange]
         * to query the effective range of values.
         *  * For a mouse, reports the absolute X screen position of the mouse pointer.
         * The units are display pixels.
         *  * For a trackball, reports the relative horizontal displacement of the trackball.
         * The value is normalized to a range from -1.0 (left) to 1.0 (right).
         *  * For a joystick, reports the absolute X position of the joystick.
         * The value is normalized to a range from -1.0 (left) to 1.0 (right).
         *
         *
         *
         * @see .getX
         * @see .getHistoricalX
         * @see MotionEvent.PointerCoords.x
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_X: Int = 0

        /**
         * Axis constant: Y axis of a motion event.
         *
         *
         *
         *  * For a touch screen, reports the absolute Y screen position of the center of
         * the touch contact area.  The units are display pixels.
         *  * For a touch pad, reports the absolute Y surface position of the center of the touch
         * contact area.  The units are device-dependent; use [InputDevice.getMotionRange]
         * to query the effective range of values.
         *  * For a mouse, reports the absolute Y screen position of the mouse pointer.
         * The units are display pixels.
         *  * For a trackball, reports the relative vertical displacement of the trackball.
         * The value is normalized to a range from -1.0 (up) to 1.0 (down).
         *  * For a joystick, reports the absolute Y position of the joystick.
         * The value is normalized to a range from -1.0 (up or far) to 1.0 (down or near).
         *
         *
         *
         * @see .getY
         * @see .getHistoricalY
         * @see MotionEvent.PointerCoords.y
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_Y: Int = 1

        /**
         * Axis constant: Pressure axis of a motion event.
         *
         *
         *
         *  * For a touch screen or touch pad, reports the approximate pressure applied to the surface
         * by a finger or other tool.  The value is normalized to a range from
         * 0 (no pressure at all) to 1 (normal pressure), although values higher than 1
         * may be generated depending on the calibration of the input device.
         *  * For a trackball, the value is set to 1 if the trackball button is pressed
         * or 0 otherwise.
         *  * For a mouse, the value is set to 1 if the primary mouse button is pressed
         * or 0 otherwise.
         *
         *
         *
         * @see .getPressure
         * @see .getHistoricalPressure
         * @see MotionEvent.PointerCoords.pressure
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_PRESSURE: Int = 2

        /**
         * Axis constant: Size axis of a motion event.
         *
         *
         *
         *  * For a touch screen or touch pad, reports the approximate size of the contact area in
         * relation to the maximum detectable size for the device.  The value is normalized
         * to a range from 0 (smallest detectable size) to 1 (largest detectable size),
         * although it is not a linear scale.  This value is of limited use.
         * To obtain calibrated size information, use
         * [.AXIS_TOUCH_MAJOR] or [.AXIS_TOOL_MAJOR].
         *
         *
         *
         * @see .getSize
         * @see .getHistoricalSize
         * @see MotionEvent.PointerCoords.size
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_SIZE: Int = 3

        /**
         * Axis constant: TouchMajor axis of a motion event.
         *
         *
         *
         *  * For a touch screen, reports the length of the major axis of an ellipse that
         * represents the touch area at the point of contact.
         * The units are display pixels.
         *  * For a touch pad, reports the length of the major axis of an ellipse that
         * represents the touch area at the point of contact.
         * The units are device-dependent; use [InputDevice.getMotionRange]
         * to query the effective range of values.
         *
         *
         *
         * @see .getTouchMajor
         * @see .getHistoricalTouchMajor
         * @see MotionEvent.PointerCoords.touchMajor
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_TOUCH_MAJOR: Int = 4

        /**
         * Axis constant: TouchMinor axis of a motion event.
         *
         *
         *
         *  * For a touch screen, reports the length of the minor axis of an ellipse that
         * represents the touch area at the point of contact.
         * The units are display pixels.
         *  * For a touch pad, reports the length of the minor axis of an ellipse that
         * represents the touch area at the point of contact.
         * The units are device-dependent; use [InputDevice.getMotionRange]
         * to query the effective range of values.
         *
         *
         *
         * When the touch is circular, the major and minor axis lengths will be equal to one another.
         *
         *
         * @see .getTouchMinor
         * @see .getHistoricalTouchMinor
         * @see MotionEvent.PointerCoords.touchMinor
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_TOUCH_MINOR: Int = 5

        /**
         * Axis constant: ToolMajor axis of a motion event.
         *
         *
         *
         *  * For a touch screen, reports the length of the major axis of an ellipse that
         * represents the size of the approaching finger or tool used to make contact.
         *  * For a touch pad, reports the length of the major axis of an ellipse that
         * represents the size of the approaching finger or tool used to make contact.
         * The units are device-dependent; use [InputDevice.getMotionRange]
         * to query the effective range of values.
         *
         *
         *
         * When the touch is circular, the major and minor axis lengths will be equal to one another.
         *
         *
         * The tool size may be larger than the touch size since the tool may not be fully
         * in contact with the touch sensor.
         *
         *
         * @see .getToolMajor
         * @see .getHistoricalToolMajor
         * @see MotionEvent.PointerCoords.toolMajor
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_TOOL_MAJOR: Int = 6

        /**
         * Axis constant: ToolMinor axis of a motion event.
         *
         *
         *
         *  * For a touch screen, reports the length of the minor axis of an ellipse that
         * represents the size of the approaching finger or tool used to make contact.
         *  * For a touch pad, reports the length of the minor axis of an ellipse that
         * represents the size of the approaching finger or tool used to make contact.
         * The units are device-dependent; use [InputDevice.getMotionRange]
         * to query the effective range of values.
         *
         *
         *
         * When the touch is circular, the major and minor axis lengths will be equal to one another.
         *
         *
         * The tool size may be larger than the touch size since the tool may not be fully
         * in contact with the touch sensor.
         *
         *
         * @see .getToolMinor
         * @see .getHistoricalToolMinor
         * @see MotionEvent.PointerCoords.toolMinor
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_TOOL_MINOR: Int = 7

        /**
         * Axis constant: Orientation axis of a motion event.
         *
         *
         *
         *  * For a touch screen or touch pad, reports the orientation of the finger
         * or tool in radians relative to the vertical plane of the device.
         * An angle of 0 radians indicates that the major axis of contact is oriented
         * upwards, is perfectly circular or is of unknown orientation.  A positive angle
         * indicates that the major axis of contact is oriented to the right.  A negative angle
         * indicates that the major axis of contact is oriented to the left.
         * The full range is from -PI/2 radians (finger pointing fully left) to PI/2 radians
         * (finger pointing fully right).
         *  * For a stylus, the orientation indicates the direction in which the stylus
         * is pointing in relation to the vertical axis of the current orientation of the screen.
         * The range is from -PI radians to PI radians, where 0 is pointing up,
         * -PI/2 radians is pointing left, -PI or PI radians is pointing down, and PI/2 radians
         * is pointing right.  See also [.AXIS_TILT].
         *
         *
         *
         * @see .getOrientation
         * @see .getHistoricalOrientation
         * @see MotionEvent.PointerCoords.orientation
         *
         * @see InputDevice.getMotionRange
         */
        val AXIS_ORIENTATION: Int = 8

        /**
         * Axis constant: Vertical Scroll axis of a motion event.
         *
         *
         *
         *  * For a mouse, reports the relative movement of the vertical scroll wheel.
         * The value is normalized to a range from -1.0 (down) to 1.0 (up).
         *
         *
         *
         * This axis should be used to scroll views vertically.
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_VSCROLL: Int = 9

        /**
         * Axis constant: Horizontal Scroll axis of a motion event.
         *
         *
         *
         *  * For a mouse, reports the relative movement of the horizontal scroll wheel.
         * The value is normalized to a range from -1.0 (left) to 1.0 (right).
         *
         *
         *
         * This axis should be used to scroll views horizontally.
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_HSCROLL: Int = 10

        /**
         * Axis constant: Z axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute Z position of the joystick.
         * The value is normalized to a range from -1.0 (high) to 1.0 (low).
         * *On game pads with two analog joysticks, this axis is often reinterpreted
         * to report the absolute X position of the second joystick instead.*
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_Z: Int = 11

        /**
         * Axis constant: X Rotation axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute rotation angle about the X axis.
         * The value is normalized to a range from -1.0 (counter-clockwise) to 1.0 (clockwise).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_RX: Int = 12

        /**
         * Axis constant: Y Rotation axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute rotation angle about the Y axis.
         * The value is normalized to a range from -1.0 (counter-clockwise) to 1.0 (clockwise).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_RY: Int = 13

        /**
         * Axis constant: Z Rotation axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute rotation angle about the Z axis.
         * The value is normalized to a range from -1.0 (counter-clockwise) to 1.0 (clockwise).
         * *On game pads with two analog joysticks, this axis is often reinterpreted
         * to report the absolute Y position of the second joystick instead.*
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_RZ: Int = 14

        /**
         * Axis constant: Hat X axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute X position of the directional hat control.
         * The value is normalized to a range from -1.0 (left) to 1.0 (right).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_HAT_X: Int = 15

        /**
         * Axis constant: Hat Y axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute Y position of the directional hat control.
         * The value is normalized to a range from -1.0 (up) to 1.0 (down).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_HAT_Y: Int = 16

        /**
         * Axis constant: Left Trigger axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute position of the left trigger control.
         * The value is normalized to a range from 0.0 (released) to 1.0 (fully pressed).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_LTRIGGER: Int = 17

        /**
         * Axis constant: Right Trigger axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute position of the right trigger control.
         * The value is normalized to a range from 0.0 (released) to 1.0 (fully pressed).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_RTRIGGER: Int = 18

        /**
         * Axis constant: Throttle axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute position of the throttle control.
         * The value is normalized to a range from 0.0 (fully open) to 1.0 (fully closed).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_THROTTLE: Int = 19

        /**
         * Axis constant: Rudder axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute position of the rudder control.
         * The value is normalized to a range from -1.0 (turn left) to 1.0 (turn right).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_RUDDER: Int = 20

        /**
         * Axis constant: Wheel axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute position of the steering wheel control.
         * The value is normalized to a range from -1.0 (turn left) to 1.0 (turn right).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_WHEEL: Int = 21

        /**
         * Axis constant: Gas axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute position of the gas (accelerator) control.
         * The value is normalized to a range from 0.0 (no acceleration)
         * to 1.0 (maximum acceleration).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GAS: Int = 22

        /**
         * Axis constant: Brake axis of a motion event.
         *
         *
         *
         *  * For a joystick, reports the absolute position of the brake control.
         * The value is normalized to a range from 0.0 (no braking) to 1.0 (maximum braking).
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_BRAKE: Int = 23

        /**
         * Axis constant: Distance axis of a motion event.
         *
         *
         *
         *  * For a stylus, reports the distance of the stylus from the screen.
         * A value of 0.0 indicates direct contact and larger values indicate increasing
         * distance from the surface.
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_DISTANCE: Int = 24

        /**
         * Axis constant: Tilt axis of a motion event.
         *
         *
         *
         *  * For a stylus, reports the tilt angle of the stylus in radians where
         * 0 radians indicates that the stylus is being held perpendicular to the
         * surface, and PI/2 radians indicates that the stylus is being held flat
         * against the surface.
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_TILT: Int = 25

        /**
         * Axis constant: Generic scroll axis of a motion event.
         *
         *
         *
         *  * Reports the relative movement of the generic scrolling device.
         *
         *
         *
         * This axis should be used for scroll events that are neither strictly vertical nor horizontal.
         * A good example would be the rotation of a rotary encoder input device.
         *
         *
         * @see .getAxisValue
         */
        val AXIS_SCROLL: Int = 26

        /**
         * Axis constant: The movement of x position of a motion event.
         *
         *
         *
         *  * For a mouse, reports a difference of x position between the previous position.
         * This is useful when pointer is captured, in that case the mouse pointer doesn't change
         * the location but this axis reports the difference which allows the app to see
         * how the mouse is moved.
         *
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_RELATIVE_X: Int = 27

        /**
         * Axis constant: The movement of y position of a motion event.
         *
         *
         * This is similar to [.AXIS_RELATIVE_X] but for y-axis.
         *
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_RELATIVE_Y: Int = 28

        /**
         * Axis constant: Generic 1 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_1: Int = 32

        /**
         * Axis constant: Generic 2 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_2: Int = 33

        /**
         * Axis constant: Generic 3 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_3: Int = 34

        /**
         * Axis constant: Generic 4 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_4: Int = 35

        /**
         * Axis constant: Generic 5 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_5: Int = 36

        /**
         * Axis constant: Generic 6 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_6: Int = 37

        /**
         * Axis constant: Generic 7 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_7: Int = 38

        /**
         * Axis constant: Generic 8 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_8: Int = 39

        /**
         * Axis constant: Generic 9 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_9: Int = 40

        /**
         * Axis constant: Generic 10 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_10: Int = 41

        /**
         * Axis constant: Generic 11 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_11: Int = 42

        /**
         * Axis constant: Generic 12 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_12: Int = 43

        /**
         * Axis constant: Generic 13 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_13: Int = 44

        /**
         * Axis constant: Generic 14 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_14: Int = 45

        /**
         * Axis constant: Generic 15 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_15: Int = 46

        /**
         * Axis constant: Generic 16 axis of a motion event.
         * The interpretation of a generic axis is device-specific.
         *
         * @see .getAxisValue
         * @see .getHistoricalAxisValue
         * @see MotionEvent.PointerCoords.getAxisValue
         * @see InputDevice.getMotionRange
         */
        val AXIS_GENERIC_16: Int = 47

        /**
         * Button constant: Primary button (left mouse button).
         *
         * This button constant is not set in response to simple touches with a finger
         * or stylus tip.  The user must actually push a button.
         *
         * @see .getButtonState
         */
        val BUTTON_PRIMARY: Int = 1 shl 0

        /**
         * Button constant: Secondary button (right mouse button).
         *
         * @see .getButtonState
         */
        val BUTTON_SECONDARY: Int = 1 shl 1

        /**
         * Button constant: Tertiary button (middle mouse button).
         *
         * @see .getButtonState
         */
        val BUTTON_TERTIARY: Int = 1 shl 2

        /**
         * Button constant: Back button pressed (mouse back button).
         *
         *
         * The system may send a [KeyEvent.KEYCODE_BACK] key press to the application
         * when this button is pressed.
         *
         *
         * @see .getButtonState
         */
        val BUTTON_BACK: Int = 1 shl 3

        /**
         * Button constant: Forward button pressed (mouse forward button).
         *
         *
         * The system may send a [KeyEvent.KEYCODE_FORWARD] key press to the application
         * when this button is pressed.
         *
         *
         * @see .getButtonState
         */
        val BUTTON_FORWARD: Int = 1 shl 4

        /**
         * Button constant: Primary stylus button pressed.
         *
         * @see .getButtonState
         */
        val BUTTON_STYLUS_PRIMARY: Int = 1 shl 5

        /**
         * Button constant: Secondary stylus button pressed.
         *
         * @see .getButtonState
         */
        val BUTTON_STYLUS_SECONDARY: Int = 1 shl 6

        // NOTE: If you add a new axis here you must also add it to:
        //  native/include/android/input.h
        // Symbolic names of all button states in bit order from least significant
        // to most significant.
        private val BUTTON_SYMBOLIC_NAMES: Array<String> = arrayOf(
            "BUTTON_PRIMARY",
            "BUTTON_SECONDARY",
            "BUTTON_TERTIARY",
            "BUTTON_BACK",
            "BUTTON_FORWARD",
            "BUTTON_STYLUS_PRIMARY",
            "BUTTON_STYLUS_SECONDARY",
            "0x00000080",
            "0x00000100",
            "0x00000200",
            "0x00000400",
            "0x00000800",
            "0x00001000",
            "0x00002000",
            "0x00004000",
            "0x00008000",
            "0x00010000",
            "0x00020000",
            "0x00040000",
            "0x00080000",
            "0x00100000",
            "0x00200000",
            "0x00400000",
            "0x00800000",
            "0x01000000",
            "0x02000000",
            "0x04000000",
            "0x08000000",
            "0x10000000",
            "0x20000000",
            "0x40000000",
            "0x80000000"
        )

        /**
         * Classification constant: None.
         *
         * No additional information is available about the current motion event stream.
         *
         * @see .getClassification
         */
        val CLASSIFICATION_NONE: Int = 0

        /**
         * Classification constant: Ambiguous gesture.
         *
         * The user's intent with respect to the current event stream is not yet determined.
         * Gestural actions, such as scrolling, should be inhibited until the classification resolves
         * to another value or the event stream ends.
         *
         * @see .getClassification
         */
        val CLASSIFICATION_AMBIGUOUS_GESTURE: Int = 1

        /**
         * Classification constant: Deep press.
         *
         * The current event stream represents the user intentionally pressing harder on the screen.
         * This classification type should be used to accelerate the long press behaviour.
         *
         * @see .getClassification
         */
        val CLASSIFICATION_DEEP_PRESS: Int = 2

        /**
         * Tool type constant: Unknown tool type.
         * This constant is used when the tool type is not known or is not relevant,
         * such as for a trackball or other non-pointing device.
         *
         * @see .getToolType
         */
        val TOOL_TYPE_UNKNOWN: Int = 0

        /**
         * Tool type constant: The tool is a finger.
         *
         * @see .getToolType
         */
        val TOOL_TYPE_FINGER: Int = 1

        /**
         * Tool type constant: The tool is a stylus.
         *
         * @see .getToolType
         */
        val TOOL_TYPE_STYLUS: Int = 2

        /**
         * Tool type constant: The tool is a mouse.
         *
         * @see .getToolType
         */
        val TOOL_TYPE_MOUSE: Int = 3

        /**
         * Tool type constant: The tool is an eraser or a stylus being used in an inverted posture.
         *
         * @see .getToolType
         */
        val TOOL_TYPE_ERASER: Int = 4

        /**
         * Tool type constant: The tool is a palm and should be rejected.
         *
         * @see .getToolType
         *
         *
         * @hide
         */
        val TOOL_TYPE_PALM: Int = 5

        // Private value for history pos that obtains the current sample.
        private val HISTORY_CURRENT: Int = (-0x80000000).toInt()

        // This is essentially the same as native AMOTION_EVENT_INVALID_CURSOR_POSITION as they're all
        // NaN and we use isnan() everywhere to check validity.
        private val INVALID_CURSOR_POSITION: Float = Float.NaN
        private val MAX_RECYCLED: Int = 10
        private val gRecyclerLock: Any = Any()
        private var gRecyclerUsed: Int = 0
        private var gRecyclerTop: MotionEvent? = null

        // Shared temporary objects used when translating coordinates supplied by
        // the caller into single element PointerCoords and pointer id arrays.
        private val gSharedTempLock: Any = Any()
        private var gSharedTempPointerCoords: Array<PointerCoords> = arrayOf()
        private var gSharedTempPointerProperties: Array<PointerProperties> = arrayOf()
        private var gSharedTempPointerIndexMap: IntArray = intArrayOf()
        private fun ensureSharedTempPointerCapacity(desiredCapacity: Int) {
            var capacity: Int = gSharedTempPointerCoords.size
            while (capacity < desiredCapacity) {
                capacity *= 2
            }
            gSharedTempPointerCoords = PointerCoords.createArray(capacity)
            gSharedTempPointerProperties = PointerProperties.createArray(capacity)
            gSharedTempPointerIndexMap = IntArray(capacity)
        }

        private fun nativeInitialize(
            nativePtr: MotionEventN,
            source: Int,
            action: Int,
            flags: Int,
            edgeFlags: Int,
            metaState: Int,
            buttonState: Int,
            classification: Int,
            xOffset: Float,
            yOffset: Float,
            xPrecision: Float,
            yPrecision: Float,
            downTimeNanos: Long,
            eventTimeNanos: Long,
            pointerCount: Int,
            pointerIds: Array<PointerProperties>,
            pointerCoords: Array<PointerCoords>
        ) {

            var nPointerIds = mutableListOf<MotionEventN.PointerProperties>()
            var nPointerCoords = mutableListOf<MotionEventN.PointerCoords>()

            var pointerProperties = MutableList<MotionEventN.PointerProperties>(pointerCount) { MotionEventN.PointerProperties() }
            var rawPointerCoords = MutableList<MotionEventN.PointerCoords>(pointerCount) { MotionEventN.PointerCoords() }

            for(i in 0 until pointerCount) {
                pointerProperties.add(pointerPropertiesToNative(pointerIds[i], MotionEventN.PointerProperties(0, 0)))
                rawPointerCoords.add(pointerCoordsToNative(pointerCoords[i], nativePtr.getXOffset(), nativePtr.getYOffset()))
            }

            var transform = Transform()
            transform.set(xOffset, yOffset)
            var identityTransform = Transform()
            nativePtr.initialize(0, source, action, 0, flags, edgeFlags, metaState, buttonState, classification, transform, xPrecision
            , yPrecision, AMOTION_EVENT_INVALID_CURSOR_POSITION, AMOTION_EVENT_INVALID_CURSOR_POSITION, identityTransform,
            downTimeNanos, eventTimeNanos, pointerCount, pointerProperties, rawPointerCoords)
        }

        private fun nativeAddBatch(
            nativePtr: MotionEventN, eventTimeNanos: Long,
            pointerCoords: Array<PointerCoords>, metaState: Int
        ) {
            val pointerCount = nativePtr.getPointerCount()
            var rawPointerCoords = MutableList<MotionEventN.PointerCoords>(pointerCount) { MotionEventN.PointerCoords() }
            for(i in 0 until pointerCount) {
                rawPointerCoords.add(pointerCoordsToNative(pointerCoords[i], nativePtr.getXOffset(), nativePtr.getYOffset()))
            }

            nativePtr.addSample(eventTimeNanos, rawPointerCoords)
        }

        private fun nativeGetPointerCoords(
            nativePtr: MotionEventN,
            pointerIndex: Int, historyPos: Int, outPointerCoords: PointerCoords?
        ) {
            outPointerCoords?.let {
                outPointerCoords.x = if (historyPos == HISTORY_CURRENT) nativePtr.getAxisValue(
                    AXIS_X.toUInt(),
                    pointerIndex
                ) else nativePtr.getHistoricalAxisValue(AXIS_X.toUInt(), pointerIndex, historyPos)
                outPointerCoords.y = if (historyPos == HISTORY_CURRENT) nativePtr.getAxisValue(
                    AXIS_Y.toUInt(),
                    pointerIndex
                ) else nativePtr.getHistoricalAxisValue(AXIS_Y.toUInt(), pointerIndex, historyPos)
                outPointerCoords.orientation =
                    if (historyPos == HISTORY_CURRENT) nativePtr.getAxisValue(
                        AXIS_ORIENTATION.toUInt(),
                        pointerIndex
                    ) else nativePtr.getHistoricalAxisValue(
                        AXIS_ORIENTATION.toUInt(),
                        pointerIndex,
                        historyPos
                    )
                outPointerCoords.y = if (historyPos == HISTORY_CURRENT) nativePtr.getAxisValue(
                    AXIS_RELATIVE_X.toUInt(),
                    pointerIndex
                ) else nativePtr.getHistoricalAxisValue(AXIS_RELATIVE_X.toUInt(), pointerIndex, historyPos)
                outPointerCoords.y = if (historyPos == HISTORY_CURRENT) nativePtr.getAxisValue(
                    AXIS_RELATIVE_Y.toUInt(),
                    pointerIndex
                ) else nativePtr.getHistoricalAxisValue(AXIS_RELATIVE_Y.toUInt(), pointerIndex, historyPos)

                val rawPointerCoords =
                    if (historyPos == HISTORY_CURRENT) nativePtr.getRawPointerCoords(pointerIndex)
                    else nativePtr.getHistoricalRawPointerCoords(pointerIndex, historyPos)

                val bits = BitSet32(rawPointerCoords.bits);
                bits.clearBit(AXIS_X.toUInt())
                bits.clearBit(AXIS_Y.toUInt())
                bits.clearBit(AXIS_ORIENTATION.toUInt())
                bits.clearBit(AXIS_RELATIVE_X.toUInt())
                bits.clearBit(AXIS_RELATIVE_Y.toUInt())

                pointerCoordsFromNative(rawPointerCoords, bits, outPointerCoords)
            }
        }

        private fun nativeGetPointerProperties(
            nativePtr: MotionEventN,
            pointerIndex: Int, outPointerProperties: PointerProperties?
        ) {
            val pointerProperties = nativePtr.getPointerProperties(pointerIndex)
            pointerPropertiesFromNative(pointerProperties, outPointerProperties!!)
        }

        // -------------- @FastNative -------------------------
        private fun nativeGetPointerId(nativePtr: MotionEventN, pointerIndex: Int): Int {
            return nativePtr.getPointerId(pointerIndex)
        }

        private fun nativeGetToolType(nativePtr: MotionEventN, pointerIndex: Int): Int {
            return nativePtr.getToolType(pointerIndex)
        }

        private fun nativeGetEventTimeNanos(nativePtr: MotionEventN, historyPos: Int): Long {
            return if (historyPos == HISTORY_CURRENT) {
                nativePtr.getEventTime()
            } else {
                /*if (!validateHistoryPos(env, historyPos, *event)) {
                    0
                }*/
                nativePtr.getHistoricalEventTime(historyPos)
            }
        }

        private fun nativeGetRawAxisValue(
            nativePtr: MotionEventN,
            axis: Int, pointerIndex: Int, historyPos: Int
        ): Float {
            if (historyPos == HISTORY_CURRENT) {
                return nativePtr.getRawAxisValue(axis.toUInt(), pointerIndex)
            } else {
                return nativePtr.getHistoricalRawAxisValue(axis.toUInt(), pointerIndex, historyPos)
            }
        }

        private fun nativeGetAxisValue(
            nativePtr: MotionEventN,
            axis: Int, pointerIndex: Int, historyPos: Int
        ): Float {
            if (historyPos == HISTORY_CURRENT) {
                return nativePtr.getAxisValue(axis.toUInt(), pointerIndex)
            } else {
                return nativePtr.getHistoricalAxisValue(axis.toUInt(), pointerIndex, historyPos)
            }
        }

        private fun nativeTransform(nativePtr: MotionEventN, matrix: Matrix33) {
            nativePtr.transform(matrix.mat)
        }

        private fun nativeApplyTransform(nativePtr: MotionEventN, matrix: Matrix33) {
            nativePtr.applyTransform(matrix.mat)
        }

        private fun nativeCopy(
            destnativePtr: MotionEventN, sourcenativePtr: MotionEventN,
            keepHistory: Boolean
        ): MotionEventN {
            destnativePtr.copyFrom(sourcenativePtr, keepHistory)
            return destnativePtr
        }

        private fun nativeGetId(nativePtr: MotionEventN): Int {
            return nativePtr.id
        }

        private fun nativeGetSource(nativePtr: MotionEventN): Int {
            return nativePtr.source.toInt()
        }

        private fun nativeSetSource(nativePtr: MotionEventN, source: Int) {
            nativePtr.source = source.toUInt()
        }

        private fun nativeGetAction(nativePtr: MotionEventN): Int {
            return nativePtr.action
        }

        private fun nativeSetAction(nativePtr: MotionEventN, action: Int) {
            nativePtr.action = action
        }

        private fun nativeIsTouchEvent(nativePtr: MotionEventN): Boolean {
            return nativePtr.isTouchEvent()
        }

        private fun nativeGetFlags(nativePtr: MotionEventN): Int {
            return nativePtr.flags
        }

        private fun nativeSetFlags(nativePtr: MotionEventN, flags: Int) {
            nativePtr.flags = flags
        }

        private fun nativeGetEdgeFlags(nativePtr: MotionEventN): Int {
            return nativePtr.edgeFlags
        }

        private fun nativeSetEdgeFlags(nativePtr: MotionEventN, edgeFlags: Int) {
            nativePtr.edgeFlags = edgeFlags
        }

        private fun nativeGetMetaState(nativePtr: MotionEventN): Int {
            return nativePtr.metaState
        }

        private fun nativeGetButtonState(nativePtr: MotionEventN): Int {
            return nativePtr.buttonState
        }

        private fun nativeSetButtonState(nativePtr: MotionEventN, buttonState: Int) {
            nativePtr.buttonState = buttonState
        }

        private fun nativeGetClassification(nativePtr: MotionEventN): Int {
            return nativePtr.classification
        }

        private fun nativeGetActionButton(nativePtr: MotionEventN): Int {
            return nativePtr.actionButton
        }

        private fun nativeSetActionButton(nativePtr: MotionEventN, actionButton: Int) {
            nativePtr.actionButton = actionButton
        }

        private fun nativeOffsetLocation(nativePtr: MotionEventN, deltaX: Float, deltaY: Float) {
            nativePtr.offsetLocation(deltaX, deltaY)
        }

        private fun nativeGetXOffset(nativePtr: MotionEventN): Float {
            return nativePtr.getXOffset()
        }

        private fun nativeGetYOffset(nativePtr: MotionEventN): Float {
            return nativePtr.getYOffset()
        }

        private fun nativeGetXPrecision(nativePtr: MotionEventN): Float {
            return nativePtr.mXPrecision
        }

        private fun nativeGetYPrecision(nativePtr: MotionEventN): Float {
            return nativePtr.mYPrecision
        }

        private fun nativeGetXCursorPosition(nativePtr: MotionEventN): Float {
            return nativePtr.getXCursorPosition()
        }

        private fun nativeGetYCursorPosition(nativePtr: MotionEventN): Float {
            return nativePtr.getYCursorPosition()
        }

        private fun nativeSetCursorPosition(nativePtr: MotionEventN, x: Float, y: Float) {
            nativePtr.setCursorPosition(x, y)
        }

        private fun nativeGetDownTimeNanos(nativePtr: MotionEventN): Long {
            return nativePtr.downTime
        }

        private fun nativeSetDownTimeNanos(nativePtr: MotionEventN, downTime: Long) {
            nativePtr.downTime = downTime
        }

        private fun nativeGetPointerCount(nativePtr: MotionEventN): Int {
            return nativePtr.getPointerCount()
        }

        private fun nativeFindPointerIndex(nativePtr: MotionEventN, pointerId: Int): Int {
            return nativePtr.findPointerIndex(pointerId)
        }

        private fun nativeGetHistorySize(nativePtr: MotionEventN): Int {
            return nativePtr.getHistorySize()
        }

        private fun nativeScale(nativePtr: MotionEventN, scale: Float) {
            return nativePtr.scale(scale)
        }

        private fun nativeGetSurfaceRotation(nativePtr: MotionEventN): Int {
            return nativePtr.getSurfaceRotation()
        }

        private fun obtain(): MotionEvent {
            var ev: MotionEvent?
            //synchronized(gRecyclerLock) {
                ev = (gRecyclerTop)
                if (ev == null) {
                    return MotionEvent()
                }
                gRecyclerTop = ev.mNext
                gRecyclerUsed -= 1
            //}
            ev.mNext = null
            return ev
        }

        /**
         * Create a new MotionEvent, filling in all of the basic values that
         * define the motion.
         *
         * @param downTime The time (in ms) when the user originally pressed down to start
         * a stream of position events.  This must be obtained from [SystemClock.uptimeMillis].
         * @param eventTime The the time (in ms) when this specific event was generated.  This
         * must be obtained from [SystemClock.uptimeMillis].
         * @param action The kind of action being performed, such as [.ACTION_DOWN].
         * @param pointerCount The number of pointers that will be in this event.
         * @param pointerProperties An array of *pointerCount* values providing
         * a [PointerProperties] property object for each pointer, which must
         * include the pointer identifier.
         * @param pointerCoords An array of *pointerCount* values providing
         * a [PointerCoords] coordinate object for each pointer.
         * @param metaState The state of any meta / modifier keys that were in effect when
         * the event was generated.
         * @param buttonState The state of buttons that are pressed.
         * @param xPrecision The precision of the X coordinate being reported.
         * @param yPrecision The precision of the Y coordinate being reported.
         * @param deviceId The id for the device that this event came from.  An id of
         * zero indicates that the event didn't come from a physical device; other
         * numbers are arbitrary and you shouldn't depend on the values.
         * @param edgeFlags A bitfield indicating which edges, if any, were touched by this
         * MotionEvent.
         * @param source The source of this event.
         * @param displayId The display ID associated with this event.
         * @param flags The motion event flags.
         * @hide
         */
        fun obtain(
            downTime: Long, eventTime: Long,
            action: Int, pointerCount: Int, pointerProperties: Array<PointerProperties>,
            pointerCoords: Array<PointerCoords>, metaState: Int, buttonState: Int,
            xPrecision: Float, yPrecision: Float,
            edgeFlags: Int, source: Int, flags: Int
        ): MotionEvent? {
            val ev: MotionEvent = obtain()
            ev.initialize(
                source, action, flags, edgeFlags,
                metaState, buttonState, CLASSIFICATION_NONE, 0f, 0f, xPrecision, yPrecision,
                downTime * NS_PER_MS, eventTime * NS_PER_MS,
                pointerCount, pointerProperties, pointerCoords
            )
            return ev
        }

        /**
         * Create a new MotionEvent, filling in all of the basic values that
         * define the motion.
         *
         * @param downTime The time (in ms) when the user originally pressed down to start
         * a stream of position events.  This must be obtained from [SystemClock.uptimeMillis].
         * @param eventTime  The the time (in ms) when this specific event was generated.  This
         * must be obtained from [SystemClock.uptimeMillis].
         * @param action The kind of action being performed, such as [.ACTION_DOWN].
         * @param x The X coordinate of this event.
         * @param y The Y coordinate of this event.
         * @param pressure The current pressure of this event.  The pressure generally
         * ranges from 0 (no pressure at all) to 1 (normal pressure), however
         * values higher than 1 may be generated depending on the calibration of
         * the input device.
         * @param size A scaled value of the approximate size of the area being pressed when
         * touched with the finger. The actual value in pixels corresponding to the finger
         * touch is normalized with a device specific range of values
         * and scaled to a value between 0 and 1.
         * @param metaState The state of any meta / modifier keys that were in effect when
         * the event was generated.
         * @param xPrecision The precision of the X coordinate being reported.
         * @param yPrecision The precision of the Y coordinate being reported.
         * @param deviceId The id for the device that this event came from.  An id of
         * zero indicates that the event didn't come from a physical device; other
         * numbers are arbitrary and you shouldn't depend on the values.
         * @param source The source of this event.
         * @param edgeFlags A bitfield indicating which edges, if any, were touched by this
         * MotionEvent.
         * @param displayId The display ID associated with this event.
         * @hide
         */
        /**
         * Create a new MotionEvent, filling in all of the basic values that
         * define the motion.
         *
         * @param downTime The time (in ms) when the user originally pressed down to start
         * a stream of position events.  This must be obtained from [SystemClock.uptimeMillis].
         * @param eventTime  The the time (in ms) when this specific event was generated.  This
         * must be obtained from [SystemClock.uptimeMillis].
         * @param action The kind of action being performed, such as [.ACTION_DOWN].
         * @param x The X coordinate of this event.
         * @param y The Y coordinate of this event.
         * @param pressure The current pressure of this event.  The pressure generally
         * ranges from 0 (no pressure at all) to 1 (normal pressure), however
         * values higher than 1 may be generated depending on the calibration of
         * the input device.
         * @param size A scaled value of the approximate size of the area being pressed when
         * touched with the finger. The actual value in pixels corresponding to the finger
         * touch is normalized with a device specific range of values
         * and scaled to a value between 0 and 1.
         * @param metaState The state of any meta / modifier keys that were in effect when
         * the event was generated.
         * @param xPrecision The precision of the X coordinate being reported.
         * @param yPrecision The precision of the Y coordinate being reported.
         * @param deviceId The id for the device that this event came from.  An id of
         * zero indicates that the event didn't come from a physical device; other
         * numbers are arbitrary and you shouldn't depend on the values.
         * @param edgeFlags A bitfield indicating which edges, if any, were touched by this
         * MotionEvent.
         */
        fun obtain(
            downTime: Long,
            eventTime: Long,
            action: Int,
            x: Float,
            y: Float,
            pressure: Float,
            size: Float,
            metaState: Int,
            xPrecision: Float,
            yPrecision: Float,
            edgeFlags: Int,
            source: Int = AINPUT_SOURCE.AINPUT_SOURCE_MOUSE.value.toInt()
        ): MotionEvent {
            val ev: MotionEvent = obtain()
            //synchronized(gSharedTempLock) {
                ensureSharedTempPointerCapacity(1)
                val pp: Array<PointerProperties> =
                    gSharedTempPointerProperties
                pp.get(0).clear()
                pp.get(0).id = 0
                val pc: Array<PointerCoords> =
                    gSharedTempPointerCoords
                pc.get(0).clear()
                pc.get(0).x = x
                pc.get(0).y = y
                pc.get(0).pressure = pressure
                pc.get(0).size = size
                ev.initialize(
                    source,
                    action,
                    0,
                    edgeFlags,
                    metaState,
                    0 /*buttonState*/,
                    CLASSIFICATION_NONE,
                    0f,
                    0f,
                    xPrecision,
                    yPrecision,
                    downTime * NS_PER_MS,
                    eventTime * NS_PER_MS,
                    1,
                    pp,
                    pc
                )
                return ev
            //}
        }

        /**
         * Create a new MotionEvent, filling in all of the basic values that
         * define the motion.
         *
         * @param downTime The time (in ms) when the user originally pressed down to start
         * a stream of position events.  This must be obtained from [SystemClock.uptimeMillis].
         * @param eventTime  The the time (in ms) when this specific event was generated.  This
         * must be obtained from [SystemClock.uptimeMillis].
         * @param action The kind of action being performed, such as [.ACTION_DOWN].
         * @param pointerCount The number of pointers that are active in this event.
         * @param x The X coordinate of this event.
         * @param y The Y coordinate of this event.
         * @param pressure The current pressure of this event.  The pressure generally
         * ranges from 0 (no pressure at all) to 1 (normal pressure), however
         * values higher than 1 may be generated depending on the calibration of
         * the input device.
         * @param size A scaled value of the approximate size of the area being pressed when
         * touched with the finger. The actual value in pixels corresponding to the finger
         * touch is normalized with a device specific range of values
         * and scaled to a value between 0 and 1.
         * @param metaState The state of any meta / modifier keys that were in effect when
         * the event was generated.
         * @param xPrecision The precision of the X coordinate being reported.
         * @param yPrecision The precision of the Y coordinate being reported.
         * @param deviceId The id for the device that this event came from.  An id of
         * zero indicates that the event didn't come from a physical device; other
         * numbers are arbitrary and you shouldn't depend on the values.
         * @param edgeFlags A bitfield indicating which edges, if any, were touched by this
         * MotionEvent.
         *
         */
        @Deprecated("Use {@link #obtain(long, long, int, float, float, float, float, int, float, float, int, int)}\n" + "      instead.")
        fun obtain(
            downTime: Long,
            eventTime: Long,
            action: Int,
            pointerCount: Int,
            x: Float,
            y: Float,
            pressure: Float,
            size: Float,
            metaState: Int,
            xPrecision: Float,
            yPrecision: Float,
            deviceId: Int,
            edgeFlags: Int
        ): MotionEvent {
            return obtain(
                downTime, eventTime, action, x, y, pressure, size,
                metaState, xPrecision, yPrecision, deviceId, edgeFlags
            )
        }

        /**
         * Create a new MotionEvent, filling in a subset of the basic motion
         * values.  Those not specified here are: device id (always 0), pressure
         * and size (always 1), x and y precision (always 1), and edgeFlags (always 0).
         *
         * @param downTime The time (in ms) when the user originally pressed down to start
         * a stream of position events.  This must be obtained from [SystemClock.uptimeMillis].
         * @param eventTime  The the time (in ms) when this specific event was generated.  This
         * must be obtained from [SystemClock.uptimeMillis].
         * @param action The kind of action being performed, such as [.ACTION_DOWN].
         * @param x The X coordinate of this event.
         * @param y The Y coordinate of this event.
         * @param metaState The state of any meta / modifier keys that were in effect when
         * the event was generated.
         */
        fun obtain(
            downTime: Long, eventTime: Long, action: Int,
            x: Float, y: Float, metaState: Int
        ): MotionEvent {
            return obtain(
                downTime, eventTime, action, x, y, 1.0f, 1.0f,
                metaState, 1.0f, 1.0f, 0, 0
            )
        }

        /**
         * Create a new MotionEvent, copying from an existing one.
         */
        fun obtain(other: MotionEvent?): MotionEvent {
            if (other == null) {
                throw Exception("other motion event must not be null")
            }
            val ev: MotionEvent = obtain()
            ev.mNativePtr = nativeCopy(ev.mNativePtr, other.mNativePtr, true /*keepHistory*/)
            return ev
        }

        /**
         * Create a new MotionEvent, copying from an existing one, but not including
         * any historical point information.
         */
        fun obtainNoHistory(other: MotionEvent?): MotionEvent {
            if (other == null) {
                throw Exception("other motion event must not be null")
            }
            val ev: MotionEvent = obtain()
            ev.mNativePtr = nativeCopy(ev.mNativePtr, other.mNativePtr, false /*keepHistory*/)
            return ev
        }

        private fun clamp(value: Float, low: Float, high: Float): Float {
            if (value < low) {
                return low
            } else if (value > high) {
                return high
            }
            return value
        }

        private fun <T> appendUnless(
            defValue: T,
            sb: StringBuilder,
            key: String,
            value: T
        ) {
            if (DEBUG_CONCISE_TOSTRING && defValue == value) return
            sb.append(key).append(value)
        }

        /**
         * Returns a string that represents the symbolic name of the specified unmasked action
         * such as "ACTION_DOWN", "ACTION_POINTER_DOWN(3)" or an equivalent numeric constant
         * such as "35" if unknown.
         *
         * @param action The unmasked action.
         * @return The symbolic name of the specified action.
         * @see .getAction
         */
        fun actionToString(action: Int): String {
            when (action) {
                ACTION_DOWN -> return "ACTION_DOWN"
                ACTION_UP -> return "ACTION_UP"
                ACTION_CANCEL -> return "ACTION_CANCEL"
                ACTION_OUTSIDE -> return "ACTION_OUTSIDE"
                ACTION_MOVE -> return "ACTION_MOVE"
                ACTION_HOVER_MOVE -> return "ACTION_HOVER_MOVE"
                ACTION_SCROLL -> return "ACTION_SCROLL"
                ACTION_HOVER_ENTER -> return "ACTION_HOVER_ENTER"
                ACTION_HOVER_EXIT -> return "ACTION_HOVER_EXIT"
                ACTION_BUTTON_PRESS -> return "ACTION_BUTTON_PRESS"
                ACTION_BUTTON_RELEASE -> return "ACTION_BUTTON_RELEASE"
            }
            val index: Int = (action and ACTION_POINTER_INDEX_MASK) shr ACTION_POINTER_INDEX_SHIFT
            when (action and ACTION_MASK) {
                ACTION_POINTER_DOWN -> return "ACTION_POINTER_DOWN($index)"
                ACTION_POINTER_UP -> return "ACTION_POINTER_UP($index)"
                else -> return action.toString()
            }
        }

        /**
         * Returns a string that represents the symbolic name of the specified combined
         * button state flags such as "0", "BUTTON_PRIMARY",
         * "BUTTON_PRIMARY|BUTTON_SECONDARY" or an equivalent numeric constant such as "0x10000000"
         * if unknown.
         *
         * @param buttonState The button state.
         * @return The symbolic name of the specified combined button state flags.
         * @hide
         */
        fun buttonStateToString(buttonState: Int): String {
            var buttonState: Int = buttonState
            if (buttonState == 0) {
                return "0"
            }
            var result: StringBuilder? = null
            var i: Int = 0
            while (buttonState != 0) {
                val isSet: Boolean = (buttonState and 1) != 0
                buttonState = buttonState ushr 1 // unsigned shift!
                if (isSet) {
                    val name: String = BUTTON_SYMBOLIC_NAMES.get(i)
                    if (result == null) {
                        if (buttonState == 0) {
                            return name
                        }
                        result = StringBuilder(name)
                    } else {
                        result.append('|')
                        result.append(name)
                    }
                }
                i += 1
            }
            return result.toString()
        }

        /**
         * Returns a string that represents the symbolic name of the specified classification.
         *
         * @param classification The classification type.
         * @return The symbolic name of this classification.
         * @hide
         */
        fun classificationToString(classification: Int): String {
            when (classification) {
                CLASSIFICATION_NONE -> return "NONE"
                CLASSIFICATION_AMBIGUOUS_GESTURE -> return "AMBIGUOUS_GESTURE"
                CLASSIFICATION_DEEP_PRESS -> return "DEEP_PRESS"
            }
            return "NONE"
        }

        /**
         * Gets a rotation matrix that (when applied to a MotionEvent) will rotate that motion event
         * such that the result coordinates end up in the same physical location on a frame whose
         * coordinates are rotated by `rotation`.
         *
         * For example, rotating (0,0) by 90 degrees will move a point from the physical top-left to
         * the bottom-left of the 90-degree-rotated frame.
         *
         * @param rotation the surface rotation of the output matrix
         * @param rotatedFrameWidth the width of the rotated frame
         * @param rotatedFrameHeight the height of the rotated frame
         *
         * @see .transform
         * @see .getSurfaceRotation
         * @hide
         */
        fun createRotateMatrix(
            rotation: Int, rotatedFrameWidth: Int, rotatedFrameHeight: Int
        ): Matrix33 {
            if (rotation == DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_0.value) {
                return Matrix33.IDENTITY
            }
            // values is row-major
            var values = FloatArray(9)
            if (rotation == DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_90.value) {
                values = floatArrayOf(0.0f, 1.0f, 0.0f, -1.0f, 0.0f,
                    rotatedFrameHeight.toFloat(), 0.0f, 0.0f, 1.0f)
            } else if (rotation == DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_180.value) {
                values = floatArrayOf(
                    -1.0f,
                    0.0f,
                    rotatedFrameWidth.toFloat(),
                    0.0f,
                    -1.0f,
                    rotatedFrameHeight.toFloat(),
                    0.0f,
                    0.0f,
                    1.0f
                )
            } else if (rotation == DISPLAY_ORIENTATION.DISPLAY_ORIENTATION_270.value) {
                values = floatArrayOf(0.0f, -1.0f,
                    rotatedFrameWidth.toFloat(), 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f)
            }
            val toOrient = Matrix33(*values)
            return toOrient
        }
    }
}