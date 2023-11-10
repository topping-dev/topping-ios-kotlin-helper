package dev.topping.ios.constraint

class KeyEvent {
    private var mNext: KeyEvent? = null

    /** @hide
     */
    var id = 0
        private set
    /** {@inheritDoc}  */
    /**
     * Renamed to [.getDeviceId].
     *
     * @hide
     */
    var keyboardDevice = 0
        private set
        @Deprecated("use {@link #getDeviceId()} instead.") get

    /** {@inheritDoc}  */
    /** {@inheritDoc}  */
    var source = 0
    /** @hide
     */
    /** @hide
     */
    var displayId = 0

    /**
     *
     * Returns the state of the meta keys.
     *
     * @return an integer in which each bit set to 1 represents a pressed
     * meta key
     *
     * @see .isAltPressed
     * @see .isShiftPressed
     * @see .isSymPressed
     * @see .isCtrlPressed
     * @see .isMetaPressed
     * @see .isFunctionPressed
     * @see .isCapsLockOn
     * @see .isNumLockOn
     * @see .isScrollLockOn
     * @see .META_ALT_ON
     *
     * @see .META_ALT_LEFT_ON
     *
     * @see .META_ALT_RIGHT_ON
     *
     * @see .META_SHIFT_ON
     *
     * @see .META_SHIFT_LEFT_ON
     *
     * @see .META_SHIFT_RIGHT_ON
     *
     * @see .META_SYM_ON
     *
     * @see .META_FUNCTION_ON
     *
     * @see .META_CTRL_ON
     *
     * @see .META_CTRL_LEFT_ON
     *
     * @see .META_CTRL_RIGHT_ON
     *
     * @see .META_META_ON
     *
     * @see .META_META_LEFT_ON
     *
     * @see .META_META_RIGHT_ON
     *
     * @see .META_CAPS_LOCK_ON
     *
     * @see .META_NUM_LOCK_ON
     *
     * @see .META_SCROLL_LOCK_ON
     *
     * @see .getModifiers
     */
    var metaState = 0
        private set

    /**
     * Retrieve the action of this key event.  May be either
     * [.ACTION_DOWN], [.ACTION_UP], or [.ACTION_MULTIPLE].
     *
     * @return The event action: ACTION_DOWN, ACTION_UP, or ACTION_MULTIPLE.
     */
    var action = 0
        private set

    /**
     * Retrieve the key code of the key event.  This is the physical key that
     * was pressed, *not* the Unicode character.
     *
     * @return The key code of the event.
     */
    var keyCode = 0
        private set

    /**
     * Retrieve the hardware key id of this key event.  These values are not
     * reliable and vary from device to device.
     *
     * {@more}
     * Mostly this is here for debugging purposes.
     */
    var scanCode = 0
        private set

    /**
     * Retrieve the repeat count of the event.  For key down events,
     * this is the number of times the key has repeated with the first
     * down starting at 0 and counting up from there.  For key up events,
     * this is always equal to zero. For multiple key events,
     * this is the number of down/up pairs that have occurred.
     *
     * @return The number of times the key has repeated.
     */
    var repeatCount = 0
        private set
    /**
     * Returns the flags for this key event.
     *
     * @see .FLAG_WOKE_HERE
     */
    /**
     * Modifies the flags of the event.
     *
     * @param newFlags New flags for the event, replacing the entire value.
     * @hide
     */
    var flags = 0

    /**
     * The time when the key initially was pressed, in nanoseconds. Only millisecond precision is
     * exposed as public api, so this must always be converted to / from milliseconds when used.
     */
    private var mDownTime: Long = 0
    /**
     * Retrieve the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base but with
     * nanosecond (instead of millisecond) precision.
     *
     *
     * The value is in nanosecond precision but it may not have nanosecond accuracy.
     *
     *
     * @return Returns the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base but with
     * nanosecond (instead of millisecond) precision.
     *
     * @hide
     */
    /**
     * The time when the current key event occurred. If mAction is ACTION_DOWN, then this is equal
     * to mDownTime. Only millisecond precision is exposed as public api, so this must always be
     * converted to / from milliseconds when used.
     */
    var eventTimeNano: Long = 0
        private set

    /**
     * For the special case of a [.ACTION_MULTIPLE] event with key
     * code of [.KEYCODE_UNKNOWN], this is a raw string of characters
     * associated with the event.  In all other cases it is null.
     *
     * @return Returns a String of 1 or more characters associated with
     * the event.
     *
     */
    var characters: String? = null
        private set

    interface Callback {
        /**
         * Called when a key down event has occurred.  If you return true,
         * you can first call [ KeyEvent.startTracking()][KeyEvent.startTracking] to have the framework track the event
         * through its [.onKeyUp] and also call your
         * [.onKeyLongPress] if it occurs.
         *
         * @param keyCode The value in event.getKeyCode().
         * @param event Description of the key event.
         *
         * @return If you handled the event, return true.  If you want to allow
         * the event to be handled by the next receiver, return false.
         */
        fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean

        /**
         * Called when a long press has occurred.  If you return true,
         * the final key up will have [KeyEvent.FLAG_CANCELED] and
         * [KeyEvent.FLAG_CANCELED_LONG_PRESS] set.  Note that in
         * order to receive this callback, someone in the event change
         * *must* return true from [.onKeyDown] *and*
         * call [KeyEvent.startTracking] on the event.
         *
         * @param keyCode The value in event.getKeyCode().
         * @param event Description of the key event.
         *
         * @return If you handled the event, return true.  If you want to allow
         * the event to be handled by the next receiver, return false.
         */
        fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean

        /**
         * Called when a key up event has occurred.
         *
         * @param keyCode The value in event.getKeyCode().
         * @param event Description of the key event.
         *
         * @return If you handled the event, return true.  If you want to allow
         * the event to be handled by the next receiver, return false.
         */
        fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean

        /**
         * Called when a user's interaction with an analog control, such as
         * flinging a trackball, generates simulated down/up events for the same
         * key multiple times in quick succession.
         *
         * @param keyCode The value in event.getKeyCode().
         * @param count Number of pairs as returned by event.getRepeatCount().
         * @param event Description of the key event.
         *
         * @return If you handled the event, return true.  If you want to allow
         * the event to be handled by the next receiver, return false.
         */
        fun onKeyMultiple(keyCode: Int, count: Int, event: KeyEvent?): Boolean
    }

    private constructor() {}

    /**
     * Create a new key event.
     *
     * @param action Action code: either [.ACTION_DOWN],
     * [.ACTION_UP], or [.ACTION_MULTIPLE].
     * @param code The key code.
     */
    constructor(action: Int, code: Int) {
        id = nativeNextId()
        this.action = action
        keyCode = code
        repeatCount = 0
        keyboardDevice = 0
    }

    /**
     * Create a new key event.
     *
     * @param downTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this key code originally went down.
     * @param eventTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this event happened.
     * @param action Action code: either [.ACTION_DOWN],
     * [.ACTION_UP], or [.ACTION_MULTIPLE].
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     */
    constructor(
        downTime: Long, eventTime: Long, action: Int,
        code: Int, repeat: Int
    ) {
        id = nativeNextId()
        mDownTime = nanoTimeToMilliseconds()
        eventTimeNano = nanoTimeToMilliseconds()
        this.action = action
        keyCode = code
        repeatCount = repeat
        keyboardDevice = 0
    }

    /**
     * Create a new key event.
     *
     * @param downTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this key code originally went down.
     * @param eventTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this event happened.
     * @param action Action code: either [.ACTION_DOWN],
     * [.ACTION_UP], or [.ACTION_MULTIPLE].
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     */
    constructor(
        downTime: Long, eventTime: Long, action: Int,
        code: Int, repeat: Int, metaState: Int
    ) {
        id = nativeNextId()
        mDownTime = nanoTimeToMilliseconds()
        eventTimeNano = nanoTimeToMilliseconds()
        this.action = action
        keyCode = code
        repeatCount = repeat
        this.metaState = metaState
        keyboardDevice = 0
    }

    /**
     * Create a new key event.
     *
     * @param downTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this key code originally went down.
     * @param eventTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this event happened.
     * @param action Action code: either [.ACTION_DOWN],
     * [.ACTION_UP], or [.ACTION_MULTIPLE].
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     * @param deviceId The device ID that generated the key event.
     * @param scancode Raw device scan code of the event.
     */
    constructor(
        downTime: Long, eventTime: Long, action: Int,
        code: Int, repeat: Int, metaState: Int,
        deviceId: Int, scancode: Int
    ) {
        id = nativeNextId()
        mDownTime = nanoTimeToMilliseconds()
        eventTimeNano = nanoTimeToMilliseconds()
        this.action = action
        keyCode = code
        repeatCount = repeat
        this.metaState = metaState
        keyboardDevice = deviceId
        scanCode = scancode
    }

    /**
     * Create a new key event.
     *
     * @param downTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this key code originally went down.
     * @param eventTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this event happened.
     * @param action Action code: either [.ACTION_DOWN],
     * [.ACTION_UP], or [.ACTION_MULTIPLE].
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     * @param deviceId The device ID that generated the key event.
     * @param scancode Raw device scan code of the event.
     * @param flags The flags for this key event
     */
    constructor(
        downTime: Long, eventTime: Long, action: Int,
        code: Int, repeat: Int, metaState: Int,
        deviceId: Int, scancode: Int, flags: Int
    ) {
        id = nativeNextId()
        mDownTime = nanoTimeToMilliseconds()
        eventTimeNano = nanoTimeToMilliseconds()
        this.action = action
        keyCode = code
        repeatCount = repeat
        this.metaState = metaState
        keyboardDevice = deviceId
        scanCode = scancode
        this.flags = flags
    }

    /**
     * Create a new key event.
     *
     * @param downTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this key code originally went down.
     * @param eventTime The time (in [android.os.SystemClock.uptimeMillis])
     * at which this event happened.
     * @param action Action code: either [.ACTION_DOWN],
     * [.ACTION_UP], or [.ACTION_MULTIPLE].
     * @param code The key code.
     * @param repeat A repeat count for down events (> 0 if this is after the
     * initial down) or event count for multiple events.
     * @param metaState Flags indicating which meta keys are currently pressed.
     * @param deviceId The device ID that generated the key event.
     * @param scancode Raw device scan code of the event.
     * @param flags The flags for this key event
     * @param source The input source such as [InputDevice.SOURCE_KEYBOARD].
     */
    constructor(
        downTime: Long, eventTime: Long, action: Int,
        code: Int, repeat: Int, metaState: Int,
        deviceId: Int, scancode: Int, flags: Int, source: Int
    ) {
        id = nativeNextId()
        mDownTime = nanoTimeToMilliseconds()
        eventTimeNano = nanoTimeToMilliseconds()
        this.action = action
        keyCode = code
        repeatCount = repeat
        this.metaState = metaState
        keyboardDevice = deviceId
        scanCode = scancode
        this.flags = flags
        this.source = source
        displayId = -1
    }

    /**
     * Create a new key event for a string of characters.  The key code,
     * action, repeat count and source will automatically be set to
     * [.KEYCODE_UNKNOWN], [.ACTION_MULTIPLE], 0, and
     * [InputDevice.SOURCE_KEYBOARD] for you.
     *
     * @param time The time (in [android.os.SystemClock.uptimeMillis])
     * at which this event occured.
     * @param characters The string of characters.
     * @param deviceId The device ID that generated the key event.
     * @param flags The flags for this key event
     */
    constructor(time: Long, characters: String?, deviceId: Int, flags: Int) {
        id = nativeNextId()
        mDownTime = nanoTimeToMilliseconds()
        eventTimeNano = nanoTimeToMilliseconds()
        this.characters = characters
        action = ACTION_MULTIPLE
        keyCode = KEYCODE_UNKNOWN
        repeatCount = 0
        keyboardDevice = deviceId
        this.flags = flags
        source = 0
        displayId = -1
    }

    /**
     * Make an exact copy of an existing key event.
     */
    constructor(origEvent: KeyEvent) {
        id = origEvent.id
        mDownTime = origEvent.mDownTime
        eventTimeNano = origEvent.eventTimeNano
        action = origEvent.action
        keyCode = origEvent.keyCode
        repeatCount = origEvent.repeatCount
        metaState = origEvent.metaState
        keyboardDevice = origEvent.keyboardDevice
        source = origEvent.source
        displayId = origEvent.displayId
        scanCode = origEvent.scanCode
        flags = origEvent.flags
        characters = origEvent.characters
    }

    /**
     * Copy an existing key event, modifying its time and repeat count.
     *
     * @param origEvent The existing event to be copied.
     * @param eventTime The new event time
     * (in [android.os.SystemClock.uptimeMillis]) of the event.
     * @param newRepeat The new repeat count of the event.
     */
    @Deprecated("Use {@link #changeTimeRepeat(KeyEvent, long, int)}\n" + "      instead.\n" + "     \n" + "      ")
    constructor(origEvent: KeyEvent, eventTime: Long, newRepeat: Int) {
        id = nativeNextId() // Not an exact copy so assign a new ID.
        mDownTime = origEvent.mDownTime
        eventTimeNano = nanoTimeToMilliseconds()
        action = origEvent.action
        keyCode = origEvent.keyCode
        repeatCount = newRepeat
        metaState = origEvent.metaState
        keyboardDevice = origEvent.keyboardDevice
        source = origEvent.source
        displayId = origEvent.displayId
        scanCode = origEvent.scanCode
        flags = origEvent.flags
        characters = origEvent.characters
    }

    /** @hide
     */
    fun copy(): KeyEvent? {
        return obtain(this)
    }

    /**
     * Recycles a key event.
     * Key events should only be recycled if they are owned by the system since user
     * code expects them to be essentially immutable, "tracking" notwithstanding.
     *
     * @hide
     */
    fun recycle() {
        characters = null
        //synchronized(gRecyclerLock) {
            if (gRecyclerUsed < MAX_RECYCLED) {
                gRecyclerUsed++
                mNext = gRecyclerTop
                gRecyclerTop = this
            }
        //}
    }

    /** @hide
     */
    fun recycleIfNeededAfterDispatch() {
        // Do nothing.
    }

    /**
     * Copy an existing key event, modifying its action.
     *
     * @param origEvent The existing event to be copied.
     * @param action The new action code of the event.
     */
    private constructor(origEvent: KeyEvent, action: Int) {
        id = nativeNextId() // Not an exact copy so assign a new ID.
        mDownTime = origEvent.mDownTime
        eventTimeNano = origEvent.eventTimeNano
        this.action = action
        keyCode = origEvent.keyCode
        repeatCount = origEvent.repeatCount
        metaState = origEvent.metaState
        keyboardDevice = origEvent.keyboardDevice
        source = origEvent.source
        displayId = origEvent.displayId
        scanCode = origEvent.scanCode
        flags = origEvent.flags
        // Don't copy mCharacters, since one way or the other we'll lose it
        // when changing the action.
    }
    /** @hide
     */
    /** @hide
     */
    var isTainted: Boolean
        get() = (flags and FLAG_TAINTED) != 0
        set(tainted) {
            flags = if (tainted) flags or FLAG_TAINTED else flags and FLAG_TAINTED.inv()
        }

    /**
     * Don't use in new code, instead explicitly check
     * [.getAction].
     *
     * @return If the action is ACTION_DOWN, returns true; else false.
     *
     * @hide
     */
    @get:Deprecated(" ")
    val isDown: Boolean
        get() = action == ACTION_DOWN

    /** Is this a system key?  System keys can not be used for menu shortcuts.
     */
    val isSystem: Boolean
        get() = isSystemKey(keyCode)

    /** @hide
     */
    val isWakeKey: Boolean
        get() = isWakeKey(keyCode)

    /**
     * Returns the state of the modifier keys.
     *
     *
     * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
     * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
     * not considered modifier keys.  Consequently, this function specifically masks out
     * [.META_CAPS_LOCK_ON], [.META_SCROLL_LOCK_ON] and [.META_NUM_LOCK_ON].
     *
     *
     * The value returned consists of the meta state (from [.getMetaState])
     * normalized using [.normalizeMetaState] and then masked with
     * [.getModifierMetaStateMask] so that only valid modifier bits are retained.
     *
     *
     * @return An integer in which each bit set to 1 represents a pressed modifier key.
     * @see .getMetaState
     */
    val modifiers: Int
        get() = normalizeMetaState(metaState) and modifierMetaStateMask

    /**
     * Returns true if no modifier keys are pressed.
     *
     *
     * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
     * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
     * not considered modifier keys.  Consequently, this function ignores
     * [.META_CAPS_LOCK_ON], [.META_SCROLL_LOCK_ON] and [.META_NUM_LOCK_ON].
     *
     *
     * The meta state is normalized prior to comparison using [.normalizeMetaState].
     *
     *
     * @return True if no modifier keys are pressed.
     * @see .metaStateHasNoModifiers
     */
    fun hasNoModifiers(): Boolean {
        return metaStateHasNoModifiers(metaState)
    }

    /**
     * Returns true if only the specified modifiers keys are pressed.
     * Returns false if a different combination of modifier keys are pressed.
     *
     *
     * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
     * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
     * not considered modifier keys.  Consequently, this function ignores
     * [.META_CAPS_LOCK_ON], [.META_SCROLL_LOCK_ON] and [.META_NUM_LOCK_ON].
     *
     *
     * If the specified modifier mask includes directional modifiers, such as
     * [.META_SHIFT_LEFT_ON], then this method ensures that the
     * modifier is pressed on that side.
     * If the specified modifier mask includes non-directional modifiers, such as
     * [.META_SHIFT_ON], then this method ensures that the modifier
     * is pressed on either side.
     * If the specified modifier mask includes both directional and non-directional modifiers
     * for the same type of key, such as [.META_SHIFT_ON] and [.META_SHIFT_LEFT_ON],
     * then this method throws an illegal argument exception.
     *
     *
     * @param modifiers The meta state of the modifier keys to check.  May be a combination
     * of modifier meta states as defined by [.getModifierMetaStateMask].  May be 0 to
     * ensure that no modifier keys are pressed.
     * @return True if only the specified modifier keys are pressed.
     * @throws IllegalArgumentException if the modifiers parameter contains invalid modifiers
     * @see .metaStateHasModifiers
     */
    fun hasModifiers(modifiers: Int): Boolean {
        return metaStateHasModifiers(metaState, modifiers)
    }

    /**
     *
     * Returns the pressed state of the ALT meta key.
     *
     * @return true if the ALT key is pressed, false otherwise
     *
     * @see .KEYCODE_ALT_LEFT
     *
     * @see .KEYCODE_ALT_RIGHT
     *
     * @see .META_ALT_ON
     */
    val isAltPressed: Boolean
        get() = (metaState and META_ALT_ON) != 0

    /**
     *
     * Returns the pressed state of the SHIFT meta key.
     *
     * @return true if the SHIFT key is pressed, false otherwise
     *
     * @see .KEYCODE_SHIFT_LEFT
     *
     * @see .KEYCODE_SHIFT_RIGHT
     *
     * @see .META_SHIFT_ON
     */
    val isShiftPressed: Boolean
        get() = (metaState and META_SHIFT_ON) != 0

    /**
     *
     * Returns the pressed state of the SYM meta key.
     *
     * @return true if the SYM key is pressed, false otherwise
     *
     * @see .KEYCODE_SYM
     *
     * @see .META_SYM_ON
     */
    val isSymPressed: Boolean
        get() = (metaState and META_SYM_ON) != 0

    /**
     *
     * Returns the pressed state of the CTRL meta key.
     *
     * @return true if the CTRL key is pressed, false otherwise
     *
     * @see .KEYCODE_CTRL_LEFT
     *
     * @see .KEYCODE_CTRL_RIGHT
     *
     * @see .META_CTRL_ON
     */
    val isCtrlPressed: Boolean
        get() = (metaState and META_CTRL_ON) != 0

    /**
     *
     * Returns the pressed state of the META meta key.
     *
     * @return true if the META key is pressed, false otherwise
     *
     * @see .KEYCODE_META_LEFT
     *
     * @see .KEYCODE_META_RIGHT
     *
     * @see .META_META_ON
     */
    val isMetaPressed: Boolean
        get() = (metaState and META_META_ON) != 0

    /**
     *
     * Returns the pressed state of the FUNCTION meta key.
     *
     * @return true if the FUNCTION key is pressed, false otherwise
     *
     * @see .KEYCODE_FUNCTION
     *
     * @see .META_FUNCTION_ON
     */
    val isFunctionPressed: Boolean
        get() = (metaState and META_FUNCTION_ON) != 0

    /**
     *
     * Returns the locked state of the CAPS LOCK meta key.
     *
     * @return true if the CAPS LOCK key is on, false otherwise
     *
     * @see .KEYCODE_CAPS_LOCK
     *
     * @see .META_CAPS_LOCK_ON
     */
    val isCapsLockOn: Boolean
        get() = (metaState and META_CAPS_LOCK_ON) != 0

    /**
     *
     * Returns the locked state of the NUM LOCK meta key.
     *
     * @return true if the NUM LOCK key is on, false otherwise
     *
     * @see .KEYCODE_NUM_LOCK
     *
     * @see .META_NUM_LOCK_ON
     */
    val isNumLockOn: Boolean
        get() = (metaState and META_NUM_LOCK_ON) != 0

    /**
     *
     * Returns the locked state of the SCROLL LOCK meta key.
     *
     * @return true if the SCROLL LOCK key is on, false otherwise
     *
     * @see .KEYCODE_SCROLL_LOCK
     *
     * @see .META_SCROLL_LOCK_ON
     */
    val isScrollLockOn: Boolean
        get() = (metaState and META_SCROLL_LOCK_ON) != 0

    /**
     * For [.ACTION_UP] events, indicates that the event has been
     * canceled as per [.FLAG_CANCELED].
     */
    val isCanceled: Boolean
        get() = (flags and FLAG_CANCELED) != 0

    /**
     * Set [.FLAG_CANCELED] flag for the key event.
     *
     * @hide
     */
    fun cancel() {
        flags = flags or FLAG_CANCELED
    }

    /**
     * Call this during [Callback.onKeyDown] to have the system track
     * the key through its final up (possibly including a long press).  Note
     * that only one key can be tracked at a time -- if another key down
     * event is received while a previous one is being tracked, tracking is
     * stopped on the previous event.
     */
    fun startTracking() {
        flags = flags or FLAG_START_TRACKING
    }

    /**
     * For [.ACTION_UP] events, indicates that the event is still being
     * tracked from its initial down event as per
     * [.FLAG_TRACKING].
     */
    val isTracking: Boolean
        get() = (flags and FLAG_TRACKING) != 0

    /**
     * For [.ACTION_DOWN] events, indicates that the event has been
     * canceled as per [.FLAG_LONG_PRESS].
     */
    val isLongPress: Boolean
        get() = (flags and FLAG_LONG_PRESS) != 0

    /**
     * Modifies the down time and the event time of the event.
     *
     * @param downTime The new down time (in [android.os.SystemClock.uptimeMillis]) of the
     * event.
     * @param eventTime The new event time (in [android.os.SystemClock.uptimeMillis]) of the
     * event.
     * @hide
     */
    fun setTime(downTime: Long, eventTime: Long) {
        mDownTime = nanoTimeToMilliseconds()
        eventTimeNano = nanoTimeToMilliseconds()
    }

    /**
     * Retrieve the time of the most recent key down event,
     * in the [android.os.SystemClock.uptimeMillis] time base.  If this
     * is a down event, this will be the same as [.getEventTime].
     * Note that when chording keys, this value is the down time of the
     * most recently pressed key, which may *not* be the same physical
     * key of this event.
     *
     * @return Returns the most recent key down time, in the
     * [android.os.SystemClock.uptimeMillis] time base
     */
    val downTime: Long
        get() = toNanoSeconds(mDownTime)

    /**
     * Retrieve the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base.
     *
     * @return Returns the time this event occurred,
     * in the [android.os.SystemClock.uptimeMillis] time base.
     */
    val eventTime: Long
        get() = toNanoSeconds(eventTimeNano)

    /**
     * Gets the Unicode character generated by the specified key and meta
     * key state combination.
     *
     *
     * Returns the Unicode character that the specified key would produce
     * when the specified meta bits (see [MetaKeyKeyListener])
     * were active.
     *
     *
     * Returns 0 if the key is not one that is used to type Unicode
     * characters.
     *
     *
     * If the return value has bit [KeyCharacterMap.COMBINING_ACCENT] set, the
     * key is a "dead key" that should be combined with another to
     * actually produce a character -- see [KeyCharacterMap.getDeadChar] --
     * after masking with [KeyCharacterMap.COMBINING_ACCENT_MASK].
     *
     *
     * @return The associated character or combining accent, or 0 if none.
     */
    val unicodeChar: Int
        get() = getUnicodeChar(metaState)

    /**
     * Gets the Unicode character generated by the specified key and meta
     * key state combination.
     *
     *
     * Returns the Unicode character that the specified key would produce
     * when the specified meta bits (see [MetaKeyKeyListener])
     * were active.
     *
     *
     * Returns 0 if the key is not one that is used to type Unicode
     * characters.
     *
     *
     * If the return value has bit [KeyCharacterMap.COMBINING_ACCENT] set, the
     * key is a "dead key" that should be combined with another to
     * actually produce a character -- see [KeyCharacterMap.getDeadChar] --
     * after masking with [KeyCharacterMap.COMBINING_ACCENT_MASK].
     *
     *
     * @param metaState The meta key modifier state.
     * @return The associated character or combining accent, or 0 if none.
     */
    fun getUnicodeChar(metaState: Int): Int {
        TODO()
        //return keyCharacterMap.get(keyCode, metaState)
    }

    /**
     * Gets the first character in the character array that can be generated
     * by the specified key code.
     *
     *
     * This is a convenience function that returns the same value as
     * [getMatch(chars, 0)][.getMatch].
     *
     *
     * @param chars The array of matching characters to consider.
     * @return The matching associated character, or 0 if none.
     */
    fun getMatch(chars: CharArray?): Char {
        return getMatch(chars, 0)
    }

    /**
     * Gets the first character in the character array that can be generated
     * by the specified key code.  If there are multiple choices, prefers
     * the one that would be generated with the specified meta key modifier state.
     *
     * @param chars The array of matching characters to consider.
     * @param metaState The preferred meta key modifier state.
     * @return The matching associated character, or 0 if none.
     */
    fun getMatch(chars: CharArray?, metaState: Int): Char {
       TODO()
    }

    /**
     * Deliver this key event to a [Callback] interface.  If this is
     * an ACTION_MULTIPLE event and it is not handled, then an attempt will
     * be made to deliver a single normal event.
     *
     * @param receiver The Callback that will be given the event.
     * @param state State information retained across events.
     * @param target The target of the dispatch, for use in tracking.
     *
     * @return The return value from the Callback method that was called.
     */
    fun dispatch(
        receiver: Callback, state: DispatcherState?,
        target: Any?
    ): Boolean {
        when (action) {
            ACTION_DOWN -> {
                flags = flags and FLAG_START_TRACKING.inv()
                if (DBG) Log.v(
                    TAG,
                    "Key down to " + target + " in " + state
                            + ": " + this
                )
                var res = receiver.onKeyDown(keyCode, this)
                if (state != null) {
                    if (res && (repeatCount == 0) && ((flags and FLAG_START_TRACKING) != 0)) {
                        if (DBG) Log.v(TAG, "  Start tracking!")
                        state.startTracking(this, target)
                    } else if (isLongPress && state.isTracking(this)) {
                        try {
                            if (receiver.onKeyLongPress(keyCode, this)) {
                                if (DBG) Log.v(TAG, "  Clear from long press!")
                                state.performedLongPress(this)
                                res = true
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                return res
            }
            ACTION_UP -> {
                if (DBG) Log.v(
                    TAG, ("Key up to " + target + " in " + state
                            + ": " + this)
                )
                state?.handleUpEvent(this)
                return receiver.onKeyUp(keyCode, this)
            }
            ACTION_MULTIPLE -> {
                val count = repeatCount
                val code = keyCode
                if (receiver.onKeyMultiple(code, count, this)) {
                    return true
                }
                if (code != KEYCODE_UNKNOWN) {
                    action = ACTION_DOWN
                    repeatCount = 0
                    val handled = receiver.onKeyDown(code, this)
                    if (handled) {
                        action = ACTION_UP
                        receiver.onKeyUp(code, this)
                    }
                    action = ACTION_MULTIPLE
                    repeatCount = count
                    return handled
                }
                return false
            }
        }
        return false
    }

    /**
     * Use with [KeyEvent.dispatch]
     * for more advanced key dispatching, such as long presses.
     */
    class DispatcherState() {
        var mDownKeyCode = 0
        var mDownTarget: Any? = null
        var mActiveLongPresses: HashMap<Int, Int> = HashMap()

        /**
         * Reset back to initial state.
         */
        fun reset() {
            if (DBG) Log.v(
                TAG,
                "Reset: $this"
            )
            mDownKeyCode = 0
            mDownTarget = null
            mActiveLongPresses.clear()
        }

        /**
         * Stop any tracking associated with this target.
         */
        fun reset(target: Any) {
            if (mDownTarget === target) {
                if (DBG) Log.v(
                    TAG,
                    "Reset in $target: $this"
                )
                mDownKeyCode = 0
                mDownTarget = null
            }
        }

        /**
         * Start tracking the key code associated with the given event.  This
         * can only be called on a key down.  It will allow you to see any
         * long press associated with the key, and will result in
         * [KeyEvent.isTracking] return true on the long press and up
         * events.
         *
         *
         * This is only needed if you are directly dispatching events, rather
         * than handling them in [Callback.onKeyDown].
         */
        fun startTracking(event: KeyEvent, target: Any?) {
            if (event.action != ACTION_DOWN) {
                throw IllegalArgumentException(
                    "Can only start tracking on a down event"
                )
            }
            if (DBG) Log.v(
                TAG,
                "Start trackingt in $target: $this"
            )
            mDownKeyCode = event.keyCode
            mDownTarget = target
        }

        /**
         * Return true if the key event is for a key code that is currently
         * being tracked by the dispatcher.
         */
        fun isTracking(event: KeyEvent): Boolean {
            return mDownKeyCode == event.keyCode
        }

        /**
         * Keep track of the given event's key code as having performed an
         * action with a long press, so no action should occur on the up.
         *
         * This is only needed if you are directly dispatching events, rather
         * than handling them in [Callback.onKeyLongPress].
         */
        fun performedLongPress(event: KeyEvent) {
            mActiveLongPresses.put(event.keyCode, 1)
        }

        /**
         * Handle key up event to stop tracking.  This resets the dispatcher state,
         * and updates the key event state based on it.
         *
         * This is only needed if you are directly dispatching events, rather
         * than handling them in [Callback.onKeyUp].
         */
        fun handleUpEvent(event: KeyEvent) {
            val keyCode = event.keyCode
            if (DBG) Log.v(
                TAG,
                "Handle key up $event: $this"
            )
            val index: Int = mActiveLongPresses.indexOfKey(keyCode)
            if (index >= 0) {
                if (DBG) Log.v(
                    TAG,
                    "  Index: $index"
                )
                event.flags = event.flags or (FLAG_CANCELED or FLAG_CANCELED_LONG_PRESS)
                mActiveLongPresses.removeAt(index)
            }
            if (mDownKeyCode == keyCode) {
                if (DBG) Log.v(TAG, "  Tracking!")
                event.flags = event.flags or FLAG_TRACKING
                mDownKeyCode = 0
                mDownTarget = null
            }
        }
    }

    companion object {
        /** Key code constant: Unknown key code.  */
        val KEYCODE_UNKNOWN = 0

        /** Key code constant: Soft Left key.
         * Usually situated below the display on phones and used as a multi-function
         * feature key for selecting a software defined function shown on the bottom left
         * of the display.  */
        val KEYCODE_SOFT_LEFT = 1

        /** Key code constant: Soft Right key.
         * Usually situated below the display on phones and used as a multi-function
         * feature key for selecting a software defined function shown on the bottom right
         * of the display.  */
        val KEYCODE_SOFT_RIGHT = 2

        /** Key code constant: Home key.
         * This key is handled by the framework and is never delivered to applications.  */
        val KEYCODE_HOME = 3

        /** Key code constant: Back key.  */
        val KEYCODE_BACK = 4

        /** Key code constant: Call key.  */
        val KEYCODE_CALL = 5

        /** Key code constant: End Call key.  */
        val KEYCODE_ENDCALL = 6

        /** Key code constant: '0' key.  */
        val KEYCODE_0 = 7

        /** Key code constant: '1' key.  */
        val KEYCODE_1 = 8

        /** Key code constant: '2' key.  */
        val KEYCODE_2 = 9

        /** Key code constant: '3' key.  */
        val KEYCODE_3 = 10

        /** Key code constant: '4' key.  */
        val KEYCODE_4 = 11

        /** Key code constant: '5' key.  */
        val KEYCODE_5 = 12

        /** Key code constant: '6' key.  */
        val KEYCODE_6 = 13

        /** Key code constant: '7' key.  */
        val KEYCODE_7 = 14

        /** Key code constant: '8' key.  */
        val KEYCODE_8 = 15

        /** Key code constant: '9' key.  */
        val KEYCODE_9 = 16

        /** Key code constant: '*' key.  */
        val KEYCODE_STAR = 17

        /** Key code constant: '#' key.  */
        val KEYCODE_POUND = 18

        /** Key code constant: Directional Pad Up key.
         * May also be synthesized from trackball motions.  */
        val KEYCODE_DPAD_UP = 19

        /** Key code constant: Directional Pad Down key.
         * May also be synthesized from trackball motions.  */
        val KEYCODE_DPAD_DOWN = 20

        /** Key code constant: Directional Pad Left key.
         * May also be synthesized from trackball motions.  */
        val KEYCODE_DPAD_LEFT = 21

        /** Key code constant: Directional Pad Right key.
         * May also be synthesized from trackball motions.  */
        val KEYCODE_DPAD_RIGHT = 22

        /** Key code constant: Directional Pad Center key.
         * May also be synthesized from trackball motions.  */
        val KEYCODE_DPAD_CENTER = 23

        /** Key code constant: Volume Up key.
         * Adjusts the speaker volume up.  */
        val KEYCODE_VOLUME_UP = 24

        /** Key code constant: Volume Down key.
         * Adjusts the speaker volume down.  */
        val KEYCODE_VOLUME_DOWN = 25

        /** Key code constant: Power key.  */
        val KEYCODE_POWER = 26

        /** Key code constant: Camera key.
         * Used to launch a camera application or take pictures.  */
        val KEYCODE_CAMERA = 27

        /** Key code constant: Clear key.  */
        val KEYCODE_CLEAR = 28

        /** Key code constant: 'A' key.  */
        val KEYCODE_A = 29

        /** Key code constant: 'B' key.  */
        val KEYCODE_B = 30

        /** Key code constant: 'C' key.  */
        val KEYCODE_C = 31

        /** Key code constant: 'D' key.  */
        val KEYCODE_D = 32

        /** Key code constant: 'E' key.  */
        val KEYCODE_E = 33

        /** Key code constant: 'F' key.  */
        val KEYCODE_F = 34

        /** Key code constant: 'G' key.  */
        val KEYCODE_G = 35

        /** Key code constant: 'H' key.  */
        val KEYCODE_H = 36

        /** Key code constant: 'I' key.  */
        val KEYCODE_I = 37

        /** Key code constant: 'J' key.  */
        val KEYCODE_J = 38

        /** Key code constant: 'K' key.  */
        val KEYCODE_K = 39

        /** Key code constant: 'L' key.  */
        val KEYCODE_L = 40

        /** Key code constant: 'M' key.  */
        val KEYCODE_M = 41

        /** Key code constant: 'N' key.  */
        val KEYCODE_N = 42

        /** Key code constant: 'O' key.  */
        val KEYCODE_O = 43

        /** Key code constant: 'P' key.  */
        val KEYCODE_P = 44

        /** Key code constant: 'Q' key.  */
        val KEYCODE_Q = 45

        /** Key code constant: 'R' key.  */
        val KEYCODE_R = 46

        /** Key code constant: 'S' key.  */
        val KEYCODE_S = 47

        /** Key code constant: 'T' key.  */
        val KEYCODE_T = 48

        /** Key code constant: 'U' key.  */
        val KEYCODE_U = 49

        /** Key code constant: 'V' key.  */
        val KEYCODE_V = 50

        /** Key code constant: 'W' key.  */
        val KEYCODE_W = 51

        /** Key code constant: 'X' key.  */
        val KEYCODE_X = 52

        /** Key code constant: 'Y' key.  */
        val KEYCODE_Y = 53

        /** Key code constant: 'Z' key.  */
        val KEYCODE_Z = 54

        /** Key code constant: ',' key.  */
        val KEYCODE_COMMA = 55

        /** Key code constant: '.' key.  */
        val KEYCODE_PERIOD = 56

        /** Key code constant: Left Alt modifier key.  */
        val KEYCODE_ALT_LEFT = 57

        /** Key code constant: Right Alt modifier key.  */
        val KEYCODE_ALT_RIGHT = 58

        /** Key code constant: Left Shift modifier key.  */
        val KEYCODE_SHIFT_LEFT = 59

        /** Key code constant: Right Shift modifier key.  */
        val KEYCODE_SHIFT_RIGHT = 60

        /** Key code constant: Tab key.  */
        val KEYCODE_TAB = 61

        /** Key code constant: Space key.  */
        val KEYCODE_SPACE = 62

        /** Key code constant: Symbol modifier key.
         * Used to enter alternate symbols.  */
        val KEYCODE_SYM = 63

        /** Key code constant: Explorer special function key.
         * Used to launch a browser application.  */
        val KEYCODE_EXPLORER = 64

        /** Key code constant: Envelope special function key.
         * Used to launch a mail application.  */
        val KEYCODE_ENVELOPE = 65

        /** Key code constant: Enter key.  */
        val KEYCODE_ENTER = 66

        /** Key code constant: Backspace key.
         * Deletes characters before the insertion point, unlike [.KEYCODE_FORWARD_DEL].  */
        val KEYCODE_DEL = 67

        /** Key code constant: '`' (backtick) key.  */
        val KEYCODE_GRAVE = 68

        /** Key code constant: '-'.  */
        val KEYCODE_MINUS = 69

        /** Key code constant: '=' key.  */
        val KEYCODE_EQUALS = 70

        /** Key code constant: '[' key.  */
        val KEYCODE_LEFT_BRACKET = 71

        /** Key code constant: ']' key.  */
        val KEYCODE_RIGHT_BRACKET = 72

        /** Key code constant: '\' key.  */
        val KEYCODE_BACKSLASH = 73

        /** Key code constant: ';' key.  */
        val KEYCODE_SEMICOLON = 74

        /** Key code constant: ''' (apostrophe) key.  */
        val KEYCODE_APOSTROPHE = 75

        /** Key code constant: '/' key.  */
        val KEYCODE_SLASH = 76

        /** Key code constant: '@' key.  */
        val KEYCODE_AT = 77

        /** Key code constant: Number modifier key.
         * Used to enter numeric symbols.
         * This key is not Num Lock; it is more like [.KEYCODE_ALT_LEFT] and is
         * interpreted as an ALT key by [android.text.method.MetaKeyKeyListener].  */
        val KEYCODE_NUM = 78

        /** Key code constant: Headset Hook key.
         * Used to hang up calls and stop media.  */
        val KEYCODE_HEADSETHOOK = 79

        /** Key code constant: Camera Focus key.
         * Used to focus the camera.  */
        val KEYCODE_FOCUS = 80 // *Camera* focus

        /** Key code constant: '+' key.  */
        val KEYCODE_PLUS = 81

        /** Key code constant: Menu key.  */
        val KEYCODE_MENU = 82

        /** Key code constant: Notification key.  */
        val KEYCODE_NOTIFICATION = 83

        /** Key code constant: Search key.  */
        val KEYCODE_SEARCH = 84

        /** Key code constant: Play/Pause media key.  */
        val KEYCODE_MEDIA_PLAY_PAUSE = 85

        /** Key code constant: Stop media key.  */
        val KEYCODE_MEDIA_STOP = 86

        /** Key code constant: Play Next media key.  */
        val KEYCODE_MEDIA_NEXT = 87

        /** Key code constant: Play Previous media key.  */
        val KEYCODE_MEDIA_PREVIOUS = 88

        /** Key code constant: Rewind media key.  */
        val KEYCODE_MEDIA_REWIND = 89

        /** Key code constant: Fast Forward media key.  */
        val KEYCODE_MEDIA_FAST_FORWARD = 90

        /** Key code constant: Mute key.
         * Mute key for the microphone (unlike [.KEYCODE_VOLUME_MUTE], which is the speaker mute
         * key).  */
        val KEYCODE_MUTE = 91

        /** Key code constant: Page Up key.  */
        val KEYCODE_PAGE_UP = 92

        /** Key code constant: Page Down key.  */
        val KEYCODE_PAGE_DOWN = 93

        /** Key code constant: Picture Symbols modifier key.
         * Used to switch symbol sets (Emoji, Kao-moji).  */
        val KEYCODE_PICTSYMBOLS = 94 // switch symbol-sets (Emoji,Kao-moji)

        /** Key code constant: Switch Charset modifier key.
         * Used to switch character sets (Kanji, Katakana).  */
        val KEYCODE_SWITCH_CHARSET = 95 // switch char-sets (Kanji,Katakana)

        /** Key code constant: A Button key.
         * On a game controller, the A button should be either the button labeled A
         * or the first button on the bottom row of controller buttons.  */
        val KEYCODE_BUTTON_A = 96

        /** Key code constant: B Button key.
         * On a game controller, the B button should be either the button labeled B
         * or the second button on the bottom row of controller buttons.  */
        val KEYCODE_BUTTON_B = 97

        /** Key code constant: C Button key.
         * On a game controller, the C button should be either the button labeled C
         * or the third button on the bottom row of controller buttons.  */
        val KEYCODE_BUTTON_C = 98

        /** Key code constant: X Button key.
         * On a game controller, the X button should be either the button labeled X
         * or the first button on the upper row of controller buttons.  */
        val KEYCODE_BUTTON_X = 99

        /** Key code constant: Y Button key.
         * On a game controller, the Y button should be either the button labeled Y
         * or the second button on the upper row of controller buttons.  */
        val KEYCODE_BUTTON_Y = 100

        /** Key code constant: Z Button key.
         * On a game controller, the Z button should be either the button labeled Z
         * or the third button on the upper row of controller buttons.  */
        val KEYCODE_BUTTON_Z = 101

        /** Key code constant: L1 Button key.
         * On a game controller, the L1 button should be either the button labeled L1 (or L)
         * or the top left trigger button.  */
        val KEYCODE_BUTTON_L1 = 102

        /** Key code constant: R1 Button key.
         * On a game controller, the R1 button should be either the button labeled R1 (or R)
         * or the top right trigger button.  */
        val KEYCODE_BUTTON_R1 = 103

        /** Key code constant: L2 Button key.
         * On a game controller, the L2 button should be either the button labeled L2
         * or the bottom left trigger button.  */
        val KEYCODE_BUTTON_L2 = 104

        /** Key code constant: R2 Button key.
         * On a game controller, the R2 button should be either the button labeled R2
         * or the bottom right trigger button.  */
        val KEYCODE_BUTTON_R2 = 105

        /** Key code constant: Left Thumb Button key.
         * On a game controller, the left thumb button indicates that the left (or only)
         * joystick is pressed.  */
        val KEYCODE_BUTTON_THUMBL = 106

        /** Key code constant: Right Thumb Button key.
         * On a game controller, the right thumb button indicates that the right
         * joystick is pressed.  */
        val KEYCODE_BUTTON_THUMBR = 107

        /** Key code constant: Start Button key.
         * On a game controller, the button labeled Start.  */
        val KEYCODE_BUTTON_START = 108

        /** Key code constant: Select Button key.
         * On a game controller, the button labeled Select.  */
        val KEYCODE_BUTTON_SELECT = 109

        /** Key code constant: Mode Button key.
         * On a game controller, the button labeled Mode.  */
        val KEYCODE_BUTTON_MODE = 110

        /** Key code constant: Escape key.  */
        val KEYCODE_ESCAPE = 111

        /** Key code constant: Forward Delete key.
         * Deletes characters ahead of the insertion point, unlike [.KEYCODE_DEL].  */
        val KEYCODE_FORWARD_DEL = 112

        /** Key code constant: Left Control modifier key.  */
        val KEYCODE_CTRL_LEFT = 113

        /** Key code constant: Right Control modifier key.  */
        val KEYCODE_CTRL_RIGHT = 114

        /** Key code constant: Caps Lock key.  */
        val KEYCODE_CAPS_LOCK = 115

        /** Key code constant: Scroll Lock key.  */
        val KEYCODE_SCROLL_LOCK = 116

        /** Key code constant: Left Meta modifier key.  */
        val KEYCODE_META_LEFT = 117

        /** Key code constant: Right Meta modifier key.  */
        val KEYCODE_META_RIGHT = 118

        /** Key code constant: Function modifier key.  */
        val KEYCODE_FUNCTION = 119

        /** Key code constant: System Request / Print Screen key.  */
        val KEYCODE_SYSRQ = 120

        /** Key code constant: Break / Pause key.  */
        val KEYCODE_BREAK = 121

        /** Key code constant: Home Movement key.
         * Used for scrolling or moving the cursor around to the start of a line
         * or to the top of a list.  */
        val KEYCODE_MOVE_HOME = 122

        /** Key code constant: End Movement key.
         * Used for scrolling or moving the cursor around to the end of a line
         * or to the bottom of a list.  */
        val KEYCODE_MOVE_END = 123

        /** Key code constant: Insert key.
         * Toggles insert / overwrite edit mode.  */
        val KEYCODE_INSERT = 124

        /** Key code constant: Forward key.
         * Navigates forward in the history stack.  Complement of [.KEYCODE_BACK].  */
        val KEYCODE_FORWARD = 125

        /** Key code constant: Play media key.  */
        val KEYCODE_MEDIA_PLAY = 126

        /** Key code constant: Pause media key.  */
        val KEYCODE_MEDIA_PAUSE = 127

        /** Key code constant: Close media key.
         * May be used to close a CD tray, for example.  */
        val KEYCODE_MEDIA_CLOSE = 128

        /** Key code constant: Eject media key.
         * May be used to eject a CD tray, for example.  */
        val KEYCODE_MEDIA_EJECT = 129

        /** Key code constant: Record media key.  */
        val KEYCODE_MEDIA_RECORD = 130

        /** Key code constant: F1 key.  */
        val KEYCODE_F1 = 131

        /** Key code constant: F2 key.  */
        val KEYCODE_F2 = 132

        /** Key code constant: F3 key.  */
        val KEYCODE_F3 = 133

        /** Key code constant: F4 key.  */
        val KEYCODE_F4 = 134

        /** Key code constant: F5 key.  */
        val KEYCODE_F5 = 135

        /** Key code constant: F6 key.  */
        val KEYCODE_F6 = 136

        /** Key code constant: F7 key.  */
        val KEYCODE_F7 = 137

        /** Key code constant: F8 key.  */
        val KEYCODE_F8 = 138

        /** Key code constant: F9 key.  */
        val KEYCODE_F9 = 139

        /** Key code constant: F10 key.  */
        val KEYCODE_F10 = 140

        /** Key code constant: F11 key.  */
        val KEYCODE_F11 = 141

        /** Key code constant: F12 key.  */
        val KEYCODE_F12 = 142

        /** Key code constant: Num Lock key.
         * This is the Num Lock key; it is different from [.KEYCODE_NUM].
         * This key alters the behavior of other keys on the numeric keypad.  */
        val KEYCODE_NUM_LOCK = 143

        /** Key code constant: Numeric keypad '0' key.  */
        val KEYCODE_NUMPAD_0 = 144

        /** Key code constant: Numeric keypad '1' key.  */
        val KEYCODE_NUMPAD_1 = 145

        /** Key code constant: Numeric keypad '2' key.  */
        val KEYCODE_NUMPAD_2 = 146

        /** Key code constant: Numeric keypad '3' key.  */
        val KEYCODE_NUMPAD_3 = 147

        /** Key code constant: Numeric keypad '4' key.  */
        val KEYCODE_NUMPAD_4 = 148

        /** Key code constant: Numeric keypad '5' key.  */
        val KEYCODE_NUMPAD_5 = 149

        /** Key code constant: Numeric keypad '6' key.  */
        val KEYCODE_NUMPAD_6 = 150

        /** Key code constant: Numeric keypad '7' key.  */
        val KEYCODE_NUMPAD_7 = 151

        /** Key code constant: Numeric keypad '8' key.  */
        val KEYCODE_NUMPAD_8 = 152

        /** Key code constant: Numeric keypad '9' key.  */
        val KEYCODE_NUMPAD_9 = 153

        /** Key code constant: Numeric keypad '/' key (for division).  */
        val KEYCODE_NUMPAD_DIVIDE = 154

        /** Key code constant: Numeric keypad '*' key (for multiplication).  */
        val KEYCODE_NUMPAD_MULTIPLY = 155

        /** Key code constant: Numeric keypad '-' key (for subtraction).  */
        val KEYCODE_NUMPAD_SUBTRACT = 156

        /** Key code constant: Numeric keypad '+' key (for addition).  */
        val KEYCODE_NUMPAD_ADD = 157

        /** Key code constant: Numeric keypad '.' key (for decimals or digit grouping).  */
        val KEYCODE_NUMPAD_DOT = 158

        /** Key code constant: Numeric keypad ',' key (for decimals or digit grouping).  */
        val KEYCODE_NUMPAD_COMMA = 159

        /** Key code constant: Numeric keypad Enter key.  */
        val KEYCODE_NUMPAD_ENTER = 160

        /** Key code constant: Numeric keypad '=' key.  */
        val KEYCODE_NUMPAD_EQUALS = 161

        /** Key code constant: Numeric keypad '(' key.  */
        val KEYCODE_NUMPAD_LEFT_PAREN = 162

        /** Key code constant: Numeric keypad ')' key.  */
        val KEYCODE_NUMPAD_RIGHT_PAREN = 163

        /** Key code constant: Volume Mute key.
         * Mute key for speaker (unlike [.KEYCODE_MUTE], which is the mute key for the
         * microphone). This key should normally be implemented as a toggle such that the first press
         * mutes the speaker and the second press restores the original volume.
         */
        val KEYCODE_VOLUME_MUTE = 164

        /** Key code constant: Info key.
         * Common on TV remotes to show additional information related to what is
         * currently being viewed.  */
        val KEYCODE_INFO = 165

        /** Key code constant: Channel up key.
         * On TV remotes, increments the television channel.  */
        val KEYCODE_CHANNEL_UP = 166

        /** Key code constant: Channel down key.
         * On TV remotes, decrements the television channel.  */
        val KEYCODE_CHANNEL_DOWN = 167

        /** Key code constant: Zoom in key.  */
        val KEYCODE_ZOOM_IN = 168

        /** Key code constant: Zoom out key.  */
        val KEYCODE_ZOOM_OUT = 169

        /** Key code constant: TV key.
         * On TV remotes, switches to viewing live TV.  */
        val KEYCODE_TV = 170

        /** Key code constant: Window key.
         * On TV remotes, toggles picture-in-picture mode or other windowing functions.
         * On Android Wear devices, triggers a display offset.  */
        val KEYCODE_WINDOW = 171

        /** Key code constant: Guide key.
         * On TV remotes, shows a programming guide.  */
        val KEYCODE_GUIDE = 172

        /** Key code constant: DVR key.
         * On some TV remotes, switches to a DVR mode for recorded shows.  */
        val KEYCODE_DVR = 173

        /** Key code constant: Bookmark key.
         * On some TV remotes, bookmarks content or web pages.  */
        val KEYCODE_BOOKMARK = 174

        /** Key code constant: Toggle captions key.
         * Switches the mode for closed-captioning text, for example during television shows.  */
        val KEYCODE_CAPTIONS = 175

        /** Key code constant: Settings key.
         * Starts the system settings activity.  */
        val KEYCODE_SETTINGS = 176

        /**
         * Key code constant: TV power key.
         * On HDMI TV panel devices and Android TV devices that don't support HDMI, toggles the power
         * state of the device.
         * On HDMI source devices, toggles the power state of the HDMI-connected TV via HDMI-CEC and
         * makes the source device follow this power state.
         */
        val KEYCODE_TV_POWER = 177

        /** Key code constant: TV input key.
         * On TV remotes, switches the input on a television screen.  */
        val KEYCODE_TV_INPUT = 178

        /** Key code constant: Set-top-box power key.
         * On TV remotes, toggles the power on an external Set-top-box.  */
        val KEYCODE_STB_POWER = 179

        /** Key code constant: Set-top-box input key.
         * On TV remotes, switches the input mode on an external Set-top-box.  */
        val KEYCODE_STB_INPUT = 180

        /** Key code constant: A/V Receiver power key.
         * On TV remotes, toggles the power on an external A/V Receiver.  */
        val KEYCODE_AVR_POWER = 181

        /** Key code constant: A/V Receiver input key.
         * On TV remotes, switches the input mode on an external A/V Receiver.  */
        val KEYCODE_AVR_INPUT = 182

        /** Key code constant: Red "programmable" key.
         * On TV remotes, acts as a contextual/programmable key.  */
        val KEYCODE_PROG_RED = 183

        /** Key code constant: Green "programmable" key.
         * On TV remotes, actsas a contextual/programmable key.  */
        val KEYCODE_PROG_GREEN = 184

        /** Key code constant: Yellow "programmable" key.
         * On TV remotes, acts as a contextual/programmable key.  */
        val KEYCODE_PROG_YELLOW = 185

        /** Key code constant: Blue "programmable" key.
         * On TV remotes, acts as a contextual/programmable key.  */
        val KEYCODE_PROG_BLUE = 186

        /** Key code constant: App switch key.
         * Should bring up the application switcher dialog.  */
        val KEYCODE_APP_SWITCH = 187

        /** Key code constant: Generic Game Pad Button #1. */
        val KEYCODE_BUTTON_1 = 188

        /** Key code constant: Generic Game Pad Button #2. */
        val KEYCODE_BUTTON_2 = 189

        /** Key code constant: Generic Game Pad Button #3. */
        val KEYCODE_BUTTON_3 = 190

        /** Key code constant: Generic Game Pad Button #4. */
        val KEYCODE_BUTTON_4 = 191

        /** Key code constant: Generic Game Pad Button #5. */
        val KEYCODE_BUTTON_5 = 192

        /** Key code constant: Generic Game Pad Button #6. */
        val KEYCODE_BUTTON_6 = 193

        /** Key code constant: Generic Game Pad Button #7. */
        val KEYCODE_BUTTON_7 = 194

        /** Key code constant: Generic Game Pad Button #8. */
        val KEYCODE_BUTTON_8 = 195

        /** Key code constant: Generic Game Pad Button #9. */
        val KEYCODE_BUTTON_9 = 196

        /** Key code constant: Generic Game Pad Button #10. */
        val KEYCODE_BUTTON_10 = 197

        /** Key code constant: Generic Game Pad Button #11. */
        val KEYCODE_BUTTON_11 = 198

        /** Key code constant: Generic Game Pad Button #12. */
        val KEYCODE_BUTTON_12 = 199

        /** Key code constant: Generic Game Pad Button #13. */
        val KEYCODE_BUTTON_13 = 200

        /** Key code constant: Generic Game Pad Button #14. */
        val KEYCODE_BUTTON_14 = 201

        /** Key code constant: Generic Game Pad Button #15. */
        val KEYCODE_BUTTON_15 = 202

        /** Key code constant: Generic Game Pad Button #16. */
        val KEYCODE_BUTTON_16 = 203

        /** Key code constant: Language Switch key.
         * Toggles the current input language such as switching between English and Japanese on
         * a QWERTY keyboard.  On some devices, the same function may be performed by
         * pressing Shift+Spacebar.  */
        val KEYCODE_LANGUAGE_SWITCH = 204

        /** Key code constant: Manner Mode key.
         * Toggles silent or vibrate mode on and off to make the device behave more politely
         * in certain settings such as on a crowded train.  On some devices, the key may only
         * operate when long-pressed.  */
        val KEYCODE_MANNER_MODE = 205

        /** Key code constant: 3D Mode key.
         * Toggles the display between 2D and 3D mode.  */
        val KEYCODE_3D_MODE = 206

        /** Key code constant: Contacts special function key.
         * Used to launch an address book application.  */
        val KEYCODE_CONTACTS = 207

        /** Key code constant: Calendar special function key.
         * Used to launch a calendar application.  */
        val KEYCODE_CALENDAR = 208

        /** Key code constant: Music special function key.
         * Used to launch a music player application.  */
        val KEYCODE_MUSIC = 209

        /** Key code constant: Calculator special function key.
         * Used to launch a calculator application.  */
        val KEYCODE_CALCULATOR = 210

        /** Key code constant: Japanese full-width / half-width key.  */
        val KEYCODE_ZENKAKU_HANKAKU = 211

        /** Key code constant: Japanese alphanumeric key.  */
        val KEYCODE_EISU = 212

        /** Key code constant: Japanese non-conversion key.  */
        val KEYCODE_MUHENKAN = 213

        /** Key code constant: Japanese conversion key.  */
        val KEYCODE_HENKAN = 214

        /** Key code constant: Japanese katakana / hiragana key.  */
        val KEYCODE_KATAKANA_HIRAGANA = 215

        /** Key code constant: Japanese Yen key.  */
        val KEYCODE_YEN = 216

        /** Key code constant: Japanese Ro key.  */
        val KEYCODE_RO = 217

        /** Key code constant: Japanese kana key.  */
        val KEYCODE_KANA = 218

        /** Key code constant: Assist key.
         * Launches the global assist activity.  Not delivered to applications.  */
        val KEYCODE_ASSIST = 219

        /** Key code constant: Brightness Down key.
         * Adjusts the screen brightness down.  */
        val KEYCODE_BRIGHTNESS_DOWN = 220

        /** Key code constant: Brightness Up key.
         * Adjusts the screen brightness up.  */
        val KEYCODE_BRIGHTNESS_UP = 221

        /** Key code constant: Audio Track key.
         * Switches the audio tracks.  */
        val KEYCODE_MEDIA_AUDIO_TRACK = 222

        /** Key code constant: Sleep key.
         * Puts the device to sleep.  Behaves somewhat like [.KEYCODE_POWER] but it
         * has no effect if the device is already asleep.  */
        val KEYCODE_SLEEP = 223

        /** Key code constant: Wakeup key.
         * Wakes up the device.  Behaves somewhat like [.KEYCODE_POWER] but it
         * has no effect if the device is already awake.  */
        val KEYCODE_WAKEUP = 224

        /** Key code constant: Pairing key.
         * Initiates peripheral pairing mode. Useful for pairing remote control
         * devices or game controllers, especially if no other input mode is
         * available.  */
        val KEYCODE_PAIRING = 225

        /** Key code constant: Media Top Menu key.
         * Goes to the top of media menu.  */
        val KEYCODE_MEDIA_TOP_MENU = 226

        /** Key code constant: '11' key.  */
        val KEYCODE_11 = 227

        /** Key code constant: '12' key.  */
        val KEYCODE_12 = 228

        /** Key code constant: Last Channel key.
         * Goes to the last viewed channel.  */
        val KEYCODE_LAST_CHANNEL = 229

        /** Key code constant: TV data service key.
         * Displays data services like weather, sports.  */
        val KEYCODE_TV_DATA_SERVICE = 230

        /** Key code constant: Voice Assist key.
         * Launches the global voice assist activity. Not delivered to applications.  */
        val KEYCODE_VOICE_ASSIST = 231

        /** Key code constant: Radio key.
         * Toggles TV service / Radio service.  */
        val KEYCODE_TV_RADIO_SERVICE = 232

        /** Key code constant: Teletext key.
         * Displays Teletext service.  */
        val KEYCODE_TV_TELETEXT = 233

        /** Key code constant: Number entry key.
         * Initiates to enter multi-digit channel nubmber when each digit key is assigned
         * for selecting separate channel. Corresponds to Number Entry Mode (0x1D) of CEC
         * User Control Code.  */
        val KEYCODE_TV_NUMBER_ENTRY = 234

        /** Key code constant: Analog Terrestrial key.
         * Switches to analog terrestrial broadcast service.  */
        val KEYCODE_TV_TERRESTRIAL_ANALOG = 235

        /** Key code constant: Digital Terrestrial key.
         * Switches to digital terrestrial broadcast service.  */
        val KEYCODE_TV_TERRESTRIAL_DIGITAL = 236

        /** Key code constant: Satellite key.
         * Switches to digital satellite broadcast service.  */
        val KEYCODE_TV_SATELLITE = 237

        /** Key code constant: BS key.
         * Switches to BS digital satellite broadcasting service available in Japan.  */
        val KEYCODE_TV_SATELLITE_BS = 238

        /** Key code constant: CS key.
         * Switches to CS digital satellite broadcasting service available in Japan.  */
        val KEYCODE_TV_SATELLITE_CS = 239

        /** Key code constant: BS/CS key.
         * Toggles between BS and CS digital satellite services.  */
        val KEYCODE_TV_SATELLITE_SERVICE = 240

        /** Key code constant: Toggle Network key.
         * Toggles selecting broacast services.  */
        val KEYCODE_TV_NETWORK = 241

        /** Key code constant: Antenna/Cable key.
         * Toggles broadcast input source between antenna and cable.  */
        val KEYCODE_TV_ANTENNA_CABLE = 242

        /** Key code constant: HDMI #1 key.
         * Switches to HDMI input #1.  */
        val KEYCODE_TV_INPUT_HDMI_1 = 243

        /** Key code constant: HDMI #2 key.
         * Switches to HDMI input #2.  */
        val KEYCODE_TV_INPUT_HDMI_2 = 244

        /** Key code constant: HDMI #3 key.
         * Switches to HDMI input #3.  */
        val KEYCODE_TV_INPUT_HDMI_3 = 245

        /** Key code constant: HDMI #4 key.
         * Switches to HDMI input #4.  */
        val KEYCODE_TV_INPUT_HDMI_4 = 246

        /** Key code constant: Composite #1 key.
         * Switches to composite video input #1.  */
        val KEYCODE_TV_INPUT_COMPOSITE_1 = 247

        /** Key code constant: Composite #2 key.
         * Switches to composite video input #2.  */
        val KEYCODE_TV_INPUT_COMPOSITE_2 = 248

        /** Key code constant: Component #1 key.
         * Switches to component video input #1.  */
        val KEYCODE_TV_INPUT_COMPONENT_1 = 249

        /** Key code constant: Component #2 key.
         * Switches to component video input #2.  */
        val KEYCODE_TV_INPUT_COMPONENT_2 = 250

        /** Key code constant: VGA #1 key.
         * Switches to VGA (analog RGB) input #1.  */
        val KEYCODE_TV_INPUT_VGA_1 = 251

        /** Key code constant: Audio description key.
         * Toggles audio description off / on.  */
        val KEYCODE_TV_AUDIO_DESCRIPTION = 252

        /** Key code constant: Audio description mixing volume up key.
         * Louden audio description volume as compared with normal audio volume.  */
        val KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP = 253

        /** Key code constant: Audio description mixing volume down key.
         * Lessen audio description volume as compared with normal audio volume.  */
        val KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN = 254

        /** Key code constant: Zoom mode key.
         * Changes Zoom mode (Normal, Full, Zoom, Wide-zoom, etc.)  */
        val KEYCODE_TV_ZOOM_MODE = 255

        /** Key code constant: Contents menu key.
         * Goes to the title list. Corresponds to Contents Menu (0x0B) of CEC User Control
         * Code  */
        val KEYCODE_TV_CONTENTS_MENU = 256

        /** Key code constant: Media context menu key.
         * Goes to the context menu of media contents. Corresponds to Media Context-sensitive
         * Menu (0x11) of CEC User Control Code.  */
        val KEYCODE_TV_MEDIA_CONTEXT_MENU = 257

        /** Key code constant: Timer programming key.
         * Goes to the timer recording menu. Corresponds to Timer Programming (0x54) of
         * CEC User Control Code.  */
        val KEYCODE_TV_TIMER_PROGRAMMING = 258

        /** Key code constant: Help key.  */
        val KEYCODE_HELP = 259

        /** Key code constant: Navigate to previous key.
         * Goes backward by one item in an ordered collection of items.  */
        val KEYCODE_NAVIGATE_PREVIOUS = 260

        /** Key code constant: Navigate to next key.
         * Advances to the next item in an ordered collection of items.  */
        val KEYCODE_NAVIGATE_NEXT = 261

        /** Key code constant: Navigate in key.
         * Activates the item that currently has focus or expands to the next level of a navigation
         * hierarchy.  */
        val KEYCODE_NAVIGATE_IN = 262

        /** Key code constant: Navigate out key.
         * Backs out one level of a navigation hierarchy or collapses the item that currently has
         * focus.  */
        val KEYCODE_NAVIGATE_OUT = 263

        /** Key code constant: Primary stem key for Wear
         * Main power/reset button on watch.  */
        val KEYCODE_STEM_PRIMARY = 264

        /** Key code constant: Generic stem key 1 for Wear  */
        val KEYCODE_STEM_1 = 265

        /** Key code constant: Generic stem key 2 for Wear  */
        val KEYCODE_STEM_2 = 266

        /** Key code constant: Generic stem key 3 for Wear  */
        val KEYCODE_STEM_3 = 267

        /** Key code constant: Directional Pad Up-Left  */
        val KEYCODE_DPAD_UP_LEFT = 268

        /** Key code constant: Directional Pad Down-Left  */
        val KEYCODE_DPAD_DOWN_LEFT = 269

        /** Key code constant: Directional Pad Up-Right  */
        val KEYCODE_DPAD_UP_RIGHT = 270

        /** Key code constant: Directional Pad Down-Right  */
        val KEYCODE_DPAD_DOWN_RIGHT = 271

        /** Key code constant: Skip forward media key.  */
        val KEYCODE_MEDIA_SKIP_FORWARD = 272

        /** Key code constant: Skip backward media key.  */
        val KEYCODE_MEDIA_SKIP_BACKWARD = 273

        /** Key code constant: Step forward media key.
         * Steps media forward, one frame at a time.  */
        val KEYCODE_MEDIA_STEP_FORWARD = 274

        /** Key code constant: Step backward media key.
         * Steps media backward, one frame at a time.  */
        val KEYCODE_MEDIA_STEP_BACKWARD = 275

        /** Key code constant: put device to sleep unless a wakelock is held.  */
        val KEYCODE_SOFT_SLEEP = 276

        /** Key code constant: Cut key.  */
        val KEYCODE_CUT = 277

        /** Key code constant: Copy key.  */
        val KEYCODE_COPY = 278

        /** Key code constant: Paste key.  */
        val KEYCODE_PASTE = 279

        /** Key code constant: Consumed by the system for navigation up  */
        val KEYCODE_SYSTEM_NAVIGATION_UP = 280

        /** Key code constant: Consumed by the system for navigation down  */
        val KEYCODE_SYSTEM_NAVIGATION_DOWN = 281

        /** Key code constant: Consumed by the system for navigation left */
        val KEYCODE_SYSTEM_NAVIGATION_LEFT = 282

        /** Key code constant: Consumed by the system for navigation right  */
        val KEYCODE_SYSTEM_NAVIGATION_RIGHT = 283

        /** Key code constant: Show all apps  */
        val KEYCODE_ALL_APPS = 284

        /** Key code constant: Refresh key.  */
        val KEYCODE_REFRESH = 285

        /** Key code constant: Thumbs up key. Apps can use this to let user upvote content.  */
        val KEYCODE_THUMBS_UP = 286

        /** Key code constant: Thumbs down key. Apps can use this to let user downvote content.  */
        val KEYCODE_THUMBS_DOWN = 287

        /**
         * Key code constant: Used to switch current [android.accounts.Account] that is
         * consuming content. May be consumed by system to set account globally.
         */
        val KEYCODE_PROFILE_SWITCH = 288

        /** Key code constant: Video Application key #1.  */
        val KEYCODE_VIDEO_APP_1 = 289

        /** Key code constant: Video Application key #2.  */
        val KEYCODE_VIDEO_APP_2 = 290

        /** Key code constant: Video Application key #3.  */
        val KEYCODE_VIDEO_APP_3 = 291

        /** Key code constant: Video Application key #4.  */
        val KEYCODE_VIDEO_APP_4 = 292

        /** Key code constant: Video Application key #5.  */
        val KEYCODE_VIDEO_APP_5 = 293

        /** Key code constant: Video Application key #6.  */
        val KEYCODE_VIDEO_APP_6 = 294

        /** Key code constant: Video Application key #7.  */
        val KEYCODE_VIDEO_APP_7 = 295

        /** Key code constant: Video Application key #8.  */
        val KEYCODE_VIDEO_APP_8 = 296

        /** Key code constant: Featured Application key #1.  */
        val KEYCODE_FEATURED_APP_1 = 297

        /** Key code constant: Featured Application key #2.  */
        val KEYCODE_FEATURED_APP_2 = 298

        /** Key code constant: Featured Application key #3.  */
        val KEYCODE_FEATURED_APP_3 = 299

        /** Key code constant: Featured Application key #4.  */
        val KEYCODE_FEATURED_APP_4 = 300

        /** Key code constant: Demo Application key #1.  */
        val KEYCODE_DEMO_APP_1 = 301

        /** Key code constant: Demo Application key #2.  */
        val KEYCODE_DEMO_APP_2 = 302

        /** Key code constant: Demo Application key #3.  */
        val KEYCODE_DEMO_APP_3 = 303

        /** Key code constant: Demo Application key #4.  */
        val KEYCODE_DEMO_APP_4 = 304
        /**
         * Returns the maximum keycode.
         */
        /**
         * Integer value of the last KEYCODE. Increases as new keycodes are added to KeyEvent.
         * @hide
         */
        val maxKeyCode = KEYCODE_DEMO_APP_4

        // NOTE: If you add a new keycode here you must also add it to:
        //  isSystem()
        //  isWakeKey()
        //  frameworks/native/include/android/keycodes.h
        //  frameworks/native/include/input/InputEventLabels.h
        //  frameworks/base/core/res/res/values/attrs.xml
        //  emulator?
        //  LAST_KEYCODE
        //
        //  Also Android currently does not reserve code ranges for vendor-
        //  specific key codes.  If you have new key codes to have, you
        //  MUST contribute a patch to the open source project to define
        //  those new codes.  This is intended to maintain a consistent
        //  set of key code definitions across all Android devices.
        // Symbolic names of all metakeys in bit order from least significant to most significant.
        // Accordingly there are exactly 32 values in this table.
        private val META_SYMBOLIC_NAMES = arrayOf(
            "META_SHIFT_ON",
            "META_ALT_ON",
            "META_SYM_ON",
            "META_FUNCTION_ON",
            "META_ALT_LEFT_ON",
            "META_ALT_RIGHT_ON",
            "META_SHIFT_LEFT_ON",
            "META_SHIFT_RIGHT_ON",
            "META_CAP_LOCKED",
            "META_ALT_LOCKED",
            "META_SYM_LOCKED",
            "0x00000800",
            "META_CTRL_ON",
            "META_CTRL_LEFT_ON",
            "META_CTRL_RIGHT_ON",
            "0x00008000",
            "META_META_ON",
            "META_META_LEFT_ON",
            "META_META_RIGHT_ON",
            "0x00080000",
            "META_CAPS_LOCK_ON",
            "META_NUM_LOCK_ON",
            "META_SCROLL_LOCK_ON",
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
        private val LABEL_PREFIX = "KEYCODE_"

        @Deprecated("There are now more than MAX_KEYCODE keycodes.\n" + "      Use {@link #getMaxKeyCode()} instead.")
        val MAX_KEYCODE = 84

        /**
         * [.getAction] value: the key has been pressed down.
         */
        val ACTION_DOWN = 0

        /**
         * [.getAction] value: the key has been released.
         */
        val ACTION_UP = 1

        @Deprecated("No longer used by the input system.\n" + "      {@link #getAction} value: multiple duplicate key events have\n" + "      occurred in a row, or a complex string is being delivered.  If the\n" + "      key code is not {@link #KEYCODE_UNKNOWN} then the\n" + "      {@link #getRepeatCount()} method returns the number of times\n" + "      the given key code should be executed.\n" + "      Otherwise, if the key code is {@link #KEYCODE_UNKNOWN}, then\n" + "      this is a sequence of characters as returned by {@link #getCharacters}.")
        val ACTION_MULTIPLE = 2

        /**
         * SHIFT key locked in CAPS mode.
         * Reserved for use by [MetaKeyKeyListener] for a published constant in its API.
         * @hide
         */
        val META_CAP_LOCKED = 0x100

        /**
         * ALT key locked.
         * Reserved for use by [MetaKeyKeyListener] for a published constant in its API.
         * @hide
         */
        val META_ALT_LOCKED = 0x200

        /**
         * SYM key locked.
         * Reserved for use by [MetaKeyKeyListener] for a published constant in its API.
         * @hide
         */
        val META_SYM_LOCKED = 0x400

        /**
         * Text is in selection mode.
         * Reserved for use by [MetaKeyKeyListener] for a private unpublished constant
         * in its API that is currently being retained for legacy reasons.
         * @hide
         */
        val META_SELECTING = 0x800

        /**
         *
         * This mask is used to check whether one of the ALT meta keys is pressed.
         *
         * @see .isAltPressed
         * @see .getMetaState
         * @see .KEYCODE_ALT_LEFT
         *
         * @see .KEYCODE_ALT_RIGHT
         */
        val META_ALT_ON = 0x02

        /**
         *
         * This mask is used to check whether the left ALT meta key is pressed.
         *
         * @see .isAltPressed
         * @see .getMetaState
         * @see .KEYCODE_ALT_LEFT
         */
        val META_ALT_LEFT_ON = 0x10

        /**
         *
         * This mask is used to check whether the right the ALT meta key is pressed.
         *
         * @see .isAltPressed
         * @see .getMetaState
         * @see .KEYCODE_ALT_RIGHT
         */
        val META_ALT_RIGHT_ON = 0x20

        /**
         *
         * This mask is used to check whether one of the SHIFT meta keys is pressed.
         *
         * @see .isShiftPressed
         * @see .getMetaState
         * @see .KEYCODE_SHIFT_LEFT
         *
         * @see .KEYCODE_SHIFT_RIGHT
         */
        val META_SHIFT_ON = 0x1

        /**
         *
         * This mask is used to check whether the left SHIFT meta key is pressed.
         *
         * @see .isShiftPressed
         * @see .getMetaState
         * @see .KEYCODE_SHIFT_LEFT
         */
        val META_SHIFT_LEFT_ON = 0x40

        /**
         *
         * This mask is used to check whether the right SHIFT meta key is pressed.
         *
         * @see .isShiftPressed
         * @see .getMetaState
         * @see .KEYCODE_SHIFT_RIGHT
         */
        val META_SHIFT_RIGHT_ON = 0x80

        /**
         *
         * This mask is used to check whether the SYM meta key is pressed.
         *
         * @see .isSymPressed
         * @see .getMetaState
         */
        val META_SYM_ON = 0x4

        /**
         *
         * This mask is used to check whether the FUNCTION meta key is pressed.
         *
         * @see .isFunctionPressed
         * @see .getMetaState
         */
        val META_FUNCTION_ON = 0x8

        /**
         *
         * This mask is used to check whether one of the CTRL meta keys is pressed.
         *
         * @see .isCtrlPressed
         * @see .getMetaState
         * @see .KEYCODE_CTRL_LEFT
         *
         * @see .KEYCODE_CTRL_RIGHT
         */
        val META_CTRL_ON = 0x1000

        /**
         *
         * This mask is used to check whether the left CTRL meta key is pressed.
         *
         * @see .isCtrlPressed
         * @see .getMetaState
         * @see .KEYCODE_CTRL_LEFT
         */
        val META_CTRL_LEFT_ON = 0x2000

        /**
         *
         * This mask is used to check whether the right CTRL meta key is pressed.
         *
         * @see .isCtrlPressed
         * @see .getMetaState
         * @see .KEYCODE_CTRL_RIGHT
         */
        val META_CTRL_RIGHT_ON = 0x4000

        /**
         *
         * This mask is used to check whether one of the META meta keys is pressed.
         *
         * @see .isMetaPressed
         * @see .getMetaState
         * @see .KEYCODE_META_LEFT
         *
         * @see .KEYCODE_META_RIGHT
         */
        val META_META_ON = 0x10000

        /**
         *
         * This mask is used to check whether the left META meta key is pressed.
         *
         * @see .isMetaPressed
         * @see .getMetaState
         * @see .KEYCODE_META_LEFT
         */
        val META_META_LEFT_ON = 0x20000

        /**
         *
         * This mask is used to check whether the right META meta key is pressed.
         *
         * @see .isMetaPressed
         * @see .getMetaState
         * @see .KEYCODE_META_RIGHT
         */
        val META_META_RIGHT_ON = 0x40000

        /**
         *
         * This mask is used to check whether the CAPS LOCK meta key is on.
         *
         * @see .isCapsLockOn
         * @see .getMetaState
         * @see .KEYCODE_CAPS_LOCK
         */
        val META_CAPS_LOCK_ON = 0x100000

        /**
         *
         * This mask is used to check whether the NUM LOCK meta key is on.
         *
         * @see .isNumLockOn
         * @see .getMetaState
         * @see .KEYCODE_NUM_LOCK
         */
        val META_NUM_LOCK_ON = 0x200000

        /**
         *
         * This mask is used to check whether the SCROLL LOCK meta key is on.
         *
         * @see .isScrollLockOn
         * @see .getMetaState
         * @see .KEYCODE_SCROLL_LOCK
         */
        val META_SCROLL_LOCK_ON = 0x400000

        /**
         * This mask is a combination of [.META_SHIFT_ON], [.META_SHIFT_LEFT_ON]
         * and [.META_SHIFT_RIGHT_ON].
         */
        val META_SHIFT_MASK = (META_SHIFT_ON
                or META_SHIFT_LEFT_ON or META_SHIFT_RIGHT_ON)

        /**
         * This mask is a combination of [.META_ALT_ON], [.META_ALT_LEFT_ON]
         * and [.META_ALT_RIGHT_ON].
         */
        val META_ALT_MASK = (META_ALT_ON
                or META_ALT_LEFT_ON or META_ALT_RIGHT_ON)

        /**
         * This mask is a combination of [.META_CTRL_ON], [.META_CTRL_LEFT_ON]
         * and [.META_CTRL_RIGHT_ON].
         */
        val META_CTRL_MASK = (META_CTRL_ON
                or META_CTRL_LEFT_ON or META_CTRL_RIGHT_ON)

        /**
         * This mask is a combination of [.META_META_ON], [.META_META_LEFT_ON]
         * and [.META_META_RIGHT_ON].
         */
        val META_META_MASK = (META_META_ON
                or META_META_LEFT_ON or META_META_RIGHT_ON)

        /**
         * This mask is set if the device woke because of this key event.
         *
         */
        @Deprecated("This flag will never be set by the system since the system\n" + "      consumes all wake keys itself.")
        val FLAG_WOKE_HERE = 0x1

        /**
         * This mask is set if the key event was generated by a software keyboard.
         */
        val FLAG_SOFT_KEYBOARD = 0x2

        /**
         * This mask is set if we don't want the key event to cause us to leave
         * touch mode.
         */
        val FLAG_KEEP_TOUCH_MODE = 0x4

        /**
         * This mask is set if an event was known to come from a trusted part
         * of the system.  That is, the event is known to come from the user,
         * and could not have been spoofed by a third party component.
         */
        val FLAG_FROM_SYSTEM = 0x8

        /**
         * This mask is used for compatibility, to identify enter keys that are
         * coming from an IME whose enter key has been auto-labelled "next" or
         * "done".  This allows TextView to dispatch these as normal enter keys
         * for old applications, but still do the appropriate action when
         * receiving them.
         */
        val FLAG_EDITOR_ACTION = 0x10

        /**
         * When associated with up key events, this indicates that the key press
         * has been canceled.  Typically this is used with virtual touch screen
         * keys, where the user can slide from the virtual key area on to the
         * display: in that case, the application will receive a canceled up
         * event and should not perform the action normally associated with the
         * key.  Note that for this to work, the application can not perform an
         * action for a key until it receives an up or the long press timeout has
         * expired.
         */
        val FLAG_CANCELED = 0x20

        /**
         * This key event was generated by a virtual (on-screen) hard key area.
         * Typically this is an area of the touchscreen, outside of the regular
         * display, dedicated to "hardware" buttons.
         */
        val FLAG_VIRTUAL_HARD_KEY = 0x40

        /**
         * This flag is set for the first key repeat that occurs after the
         * long press timeout.
         */
        val FLAG_LONG_PRESS = 0x80

        /**
         * Set when a key event has [.FLAG_CANCELED] set because a long
         * press action was executed while it was down.
         */
        val FLAG_CANCELED_LONG_PRESS = 0x100

        /**
         * Set for [.ACTION_UP] when this event's key code is still being
         * tracked from its initial down.  That is, somebody requested that tracking
         * started on the key down and a long press has not caused
         * the tracking to be canceled.
         */
        val FLAG_TRACKING = 0x200

        /**
         * Set when a key event has been synthesized to implement default behavior
         * for an event that the application did not handle.
         * Fallback key events are generated by unhandled trackball motions
         * (to emulate a directional keypad) and by certain unhandled key presses
         * that are declared in the key map (such as special function numeric keypad
         * keys when numlock is off).
         */
        val FLAG_FALLBACK = 0x400

        /**
         * This flag indicates that this event was modified by or generated from an accessibility
         * service. Value = 0x800
         * @hide
         */
        val FLAG_IS_ACCESSIBILITY_EVENT: Int = 0

        /**
         * Signifies that the key is being predispatched.
         * @hide
         */
        val FLAG_PREDISPATCH = 0x20000000

        /**
         * Private control to determine when an app is tracking a key sequence.
         * @hide
         */
        val FLAG_START_TRACKING = 0x40000000

        /**
         * Private flag that indicates when the system has detected that this key event
         * may be inconsistent with respect to the sequence of previously delivered key events,
         * such as when a key up event is sent but the key was not down.
         *
         * @hide
         * @see .isTainted
         *
         * @see .setTainted
         */
        val FLAG_TAINTED = -0x80000000

        val DBG = false
        val TAG = "KeyEvent"
        private val MAX_RECYCLED = 10
        private val gRecyclerLock = Any()
        private var gRecyclerUsed = 0
        private var gRecyclerTop: KeyEvent? = null
        //TODO: Check this
        private fun nativeKeyCodeToString(keyCode: Int): String {
            return keyCode.toString()
        }
        private fun nativeKeyCodeFromString(keyCode: String): Int {
            return keyCode.toInt()
        }
        private fun nativeNextId(): Int {
            return 0
        }
        private fun obtain(): KeyEvent? {
            var ev: KeyEvent?
            //synchronized(gRecyclerLock) {
                ev = gRecyclerTop
                if (ev == null) {
                    return KeyEvent()
                }
                gRecyclerTop = ev!!.mNext
                gRecyclerUsed -= 1
            //}
            ev!!.mNext = null
            return ev
        }

        /**
         * Obtains a (potentially recycled) key event. Used by native code to create a Java object.
         *
         * @hide
         */
        private fun obtain(
            id: Int, downTimeNanos: Long, eventTimeNanos: Long, action: Int,
            code: Int, repeat: Int, metaState: Int,
            deviceId: Int, scancode: Int, flags: Int, source: Int, displayId: Int, hmac: ByteArray?,
            characters: String
        ): KeyEvent? {
            val ev = obtain()
            ev!!.id = id
            ev.mDownTime = downTimeNanos
            ev.eventTimeNano = eventTimeNanos
            ev.action = action
            ev.keyCode = code
            ev.repeatCount = repeat
            ev.metaState = metaState
            ev.keyboardDevice = deviceId
            ev.scanCode = scancode
            ev.flags = flags
            ev.source = source
            ev.displayId = displayId
            ev.characters = characters
            return ev
        }

        /**
         * Obtains a (potentially recycled) key event.
         *
         * @hide
         */
        fun obtain(
            downTime: Long,
            eventTime: Long,
            action: Int,
            code: Int,
            repeat: Int,
            metaState: Int,
            deviceId: Int,
            scanCode: Int,
            flags: Int,
            source: Int,
            displayId: Int,
            characters: String
        ): KeyEvent? {
            var downTime = downTime
            var eventTime = eventTime
            downTime = nanoTimeToMilliseconds()
            eventTime = nanoTimeToMilliseconds()
            return obtain(
                nativeNextId(), downTime, eventTime, action, code, repeat, metaState,
                deviceId, scanCode, flags, source, displayId, null /* hmac */, characters
            )
        }

        /**
         * Obtains a (potentially recycled) key event.
         *
         * @hide
         */
        fun obtain(
            downTime: Long,
            eventTime: Long,
            action: Int,
            code: Int,
            repeat: Int,
            metaState: Int,
            deviceId: Int,
            scancode: Int,
            flags: Int,
            source: Int,
            characters: String
        ): KeyEvent? {
            // Do not convert downTime and eventTime here. We are calling the obtain method above,
            // which will do the conversion. Just specify INVALID_DISPLAY and forward the request.
            return obtain(
                downTime, eventTime, action, code, repeat, metaState, deviceId, scancode,
                flags, source, -1, characters
            )
        }

        /**
         * / **
         * Obtains a (potentially recycled) copy of another key event.
         *
         * @hide
         */
        fun obtain(other: KeyEvent): KeyEvent? {
            val ev = obtain()
            ev!!.id = other.id
            ev.mDownTime = other.mDownTime
            ev.eventTimeNano = other.eventTimeNano
            ev.action = other.action
            ev.keyCode = other.keyCode
            ev.repeatCount = other.repeatCount
            ev.metaState = other.metaState
            ev.keyboardDevice = other.keyboardDevice
            ev.scanCode = other.scanCode
            ev.flags = other.flags
            ev.source = other.source
            ev.displayId = other.displayId
            ev.characters = other.characters
            return ev
        }

        /**
         * Create a new key event that is the same as the given one, but whose
         * event time and repeat count are replaced with the given value.
         *
         * @param event The existing event to be copied.  This is not modified.
         * @param eventTime The new event time
         * (in [android.os.SystemClock.uptimeMillis]) of the event.
         * @param newRepeat The new repeat count of the event.
         */
        fun changeTimeRepeat(
            event: KeyEvent, eventTime: Long,
            newRepeat: Int
        ): KeyEvent {
            return KeyEvent(event, eventTime, newRepeat)
        }

        /**
         * Create a new key event that is the same as the given one, but whose
         * event time and repeat count are replaced with the given value.
         *
         * @param event The existing event to be copied.  This is not modified.
         * @param eventTime The new event time
         * (in [android.os.SystemClock.uptimeMillis]) of the event.
         * @param newRepeat The new repeat count of the event.
         * @param newFlags New flags for the event, replacing the entire value
         * in the original event.
         */
        fun changeTimeRepeat(
            event: KeyEvent, eventTime: Long,
            newRepeat: Int, newFlags: Int
        ): KeyEvent {
            val ret = KeyEvent(event)
            ret.id = nativeNextId() // Not an exact copy so assign a new ID.
            ret.eventTimeNano = nanoTimeToMilliseconds()
            ret.repeatCount = newRepeat
            ret.flags = newFlags
            return ret
        }

        /**
         * Create a new key event that is the same as the given one, but whose
         * action is replaced with the given value.
         *
         * @param event The existing event to be copied.  This is not modified.
         * @param action The new action code of the event.
         */
        fun changeAction(event: KeyEvent, action: Int): KeyEvent {
            return KeyEvent(event, action)
        }

        /**
         * Create a new key event that is the same as the given one, but whose
         * flags are replaced with the given value.
         *
         * @param event The existing event to be copied.  This is not modified.
         * @param flags The new flags constant.
         */
        fun changeFlags(event: KeyEvent, flags: Int): KeyEvent {
            var event = event
            event = KeyEvent(event)
            event.id = nativeNextId() // Not an exact copy so assign a new ID.
            event.flags = flags
            return event
        }

        /**
         * Returns true if the specified keycode is a gamepad button.
         * @return True if the keycode is a gamepad button, such as [.KEYCODE_BUTTON_A].
         */
        fun isGamepadButton(keyCode: Int): Boolean {
            when (keyCode) {
                KEYCODE_BUTTON_A, KEYCODE_BUTTON_B, KEYCODE_BUTTON_C, KEYCODE_BUTTON_X, KEYCODE_BUTTON_Y, KEYCODE_BUTTON_Z, KEYCODE_BUTTON_L1, KEYCODE_BUTTON_R1, KEYCODE_BUTTON_L2, KEYCODE_BUTTON_R2, KEYCODE_BUTTON_THUMBL, KEYCODE_BUTTON_THUMBR, KEYCODE_BUTTON_START, KEYCODE_BUTTON_SELECT, KEYCODE_BUTTON_MODE, KEYCODE_BUTTON_1, KEYCODE_BUTTON_2, KEYCODE_BUTTON_3, KEYCODE_BUTTON_4, KEYCODE_BUTTON_5, KEYCODE_BUTTON_6, KEYCODE_BUTTON_7, KEYCODE_BUTTON_8, KEYCODE_BUTTON_9, KEYCODE_BUTTON_10, KEYCODE_BUTTON_11, KEYCODE_BUTTON_12, KEYCODE_BUTTON_13, KEYCODE_BUTTON_14, KEYCODE_BUTTON_15, KEYCODE_BUTTON_16 -> return true
                else -> return false
            }
        }

        /** Whether key will, by default, trigger a click on the focused view.
         * @hide
         */
        fun isConfirmKey(keyCode: Int): Boolean {
            when (keyCode) {
                KEYCODE_DPAD_CENTER, KEYCODE_ENTER, KEYCODE_SPACE, KEYCODE_NUMPAD_ENTER -> return true
                else -> return false
            }
        }

        /**
         * Returns whether this key will be sent to the
         * [android.media.session.MediaSession.Callback] if not handled.
         */
        fun isMediaSessionKey(keyCode: Int): Boolean {
            when (keyCode) {
                KEYCODE_MEDIA_PLAY, KEYCODE_MEDIA_PAUSE, KEYCODE_MEDIA_PLAY_PAUSE, KEYCODE_HEADSETHOOK, KEYCODE_MEDIA_STOP, KEYCODE_MEDIA_NEXT, KEYCODE_MEDIA_PREVIOUS, KEYCODE_MEDIA_REWIND, KEYCODE_MEDIA_RECORD, KEYCODE_MEDIA_FAST_FORWARD -> return true
            }
            return false
        }

        /** Is this a system key? System keys can not be used for menu shortcuts.
         * @hide
         */
        fun isSystemKey(keyCode: Int): Boolean {
            when (keyCode) {
                KEYCODE_MENU, KEYCODE_SOFT_RIGHT, KEYCODE_HOME, KEYCODE_BACK, KEYCODE_CALL, KEYCODE_ENDCALL, KEYCODE_VOLUME_UP, KEYCODE_VOLUME_DOWN, KEYCODE_VOLUME_MUTE, KEYCODE_MUTE, KEYCODE_POWER, KEYCODE_HEADSETHOOK, KEYCODE_MEDIA_PLAY, KEYCODE_MEDIA_PAUSE, KEYCODE_MEDIA_PLAY_PAUSE, KEYCODE_MEDIA_STOP, KEYCODE_MEDIA_NEXT, KEYCODE_MEDIA_PREVIOUS, KEYCODE_MEDIA_REWIND, KEYCODE_MEDIA_RECORD, KEYCODE_MEDIA_FAST_FORWARD, KEYCODE_CAMERA, KEYCODE_FOCUS, KEYCODE_SEARCH, KEYCODE_BRIGHTNESS_DOWN, KEYCODE_BRIGHTNESS_UP, KEYCODE_MEDIA_AUDIO_TRACK, KEYCODE_SYSTEM_NAVIGATION_UP, KEYCODE_SYSTEM_NAVIGATION_DOWN, KEYCODE_SYSTEM_NAVIGATION_LEFT, KEYCODE_SYSTEM_NAVIGATION_RIGHT -> return true
            }
            return false
        }

        /** @hide
         */
        fun isWakeKey(keyCode: Int): Boolean {
            when (keyCode) {
                KEYCODE_CAMERA, KEYCODE_MENU, KEYCODE_PAIRING, KEYCODE_STEM_1, KEYCODE_STEM_2, KEYCODE_STEM_3, KEYCODE_WAKEUP -> return true
            }
            return false
        }

        /** @hide
         */
        fun isMetaKey(keyCode: Int): Boolean {
            return keyCode == KEYCODE_META_LEFT || keyCode == KEYCODE_META_RIGHT
        }

        /** @hide
         */
        fun isAltKey(keyCode: Int): Boolean {
            return keyCode == KEYCODE_ALT_LEFT || keyCode == KEYCODE_ALT_RIGHT
        }

        /**
         * Gets a mask that includes all valid modifier key meta state bits.
         *
         *
         * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
         * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
         * not considered modifier keys.  Consequently, the mask specifically excludes
         * [.META_CAPS_LOCK_ON], [.META_SCROLL_LOCK_ON] and [.META_NUM_LOCK_ON].
         *
         *
         * @return The modifier meta state mask which is a combination of
         * [.META_SHIFT_ON], [.META_SHIFT_LEFT_ON], [.META_SHIFT_RIGHT_ON],
         * [.META_ALT_ON], [.META_ALT_LEFT_ON], [.META_ALT_RIGHT_ON],
         * [.META_CTRL_ON], [.META_CTRL_LEFT_ON], [.META_CTRL_RIGHT_ON],
         * [.META_META_ON], [.META_META_LEFT_ON], [.META_META_RIGHT_ON],
         * [.META_SYM_ON], [.META_FUNCTION_ON].
         */
        // Mask of all modifier key meta states.  Specifically excludes locked keys like caps lock.
        val modifierMetaStateMask = (META_SHIFT_ON or META_SHIFT_LEFT_ON or META_SHIFT_RIGHT_ON
                or META_ALT_ON or META_ALT_LEFT_ON or META_ALT_RIGHT_ON
                or META_CTRL_ON or META_CTRL_LEFT_ON or META_CTRL_RIGHT_ON
                or META_META_ON or META_META_LEFT_ON or META_META_RIGHT_ON
                or META_SYM_ON or META_FUNCTION_ON)

        // Mask of all lock key meta states.
        private val META_LOCK_MASK = META_CAPS_LOCK_ON or META_NUM_LOCK_ON or META_SCROLL_LOCK_ON

        // Mask of all valid meta states.
        private val META_ALL_MASK = modifierMetaStateMask or META_LOCK_MASK

        // Mask of all synthetic meta states that are reserved for API compatibility with
        // historical uses in MetaKeyKeyListener.
        private val META_SYNTHETIC_MASK =
            META_CAP_LOCKED or META_ALT_LOCKED or META_SYM_LOCKED or META_SELECTING

        // Mask of all meta states that are not valid use in specifying a modifier key.
        // These bits are known to be used for purposes other than specifying modifiers.
        private val META_INVALID_MODIFIER_MASK = META_LOCK_MASK or META_SYNTHETIC_MASK

        /**
         * Returns true if this key code is a modifier key.
         *
         *
         * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
         * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
         * not considered modifier keys.  Consequently, this function return false
         * for those keys.
         *
         *
         * @return True if the key code is one of
         * [.KEYCODE_SHIFT_LEFT] [.KEYCODE_SHIFT_RIGHT],
         * [.KEYCODE_ALT_LEFT], [.KEYCODE_ALT_RIGHT],
         * [.KEYCODE_CTRL_LEFT], [.KEYCODE_CTRL_RIGHT],
         * [.KEYCODE_META_LEFT], or [.KEYCODE_META_RIGHT],
         * [.KEYCODE_SYM], [.KEYCODE_NUM], [.KEYCODE_FUNCTION].
         */
        fun isModifierKey(keyCode: Int): Boolean {
            when (keyCode) {
                KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT, KEYCODE_ALT_LEFT, KEYCODE_ALT_RIGHT, KEYCODE_CTRL_LEFT, KEYCODE_CTRL_RIGHT, KEYCODE_META_LEFT, KEYCODE_META_RIGHT, KEYCODE_SYM, KEYCODE_NUM, KEYCODE_FUNCTION -> return true
                else -> return false
            }
        }

        /**
         * Normalizes the specified meta state.
         *
         *
         * The meta state is normalized such that if either the left or right modifier meta state
         * bits are set then the result will also include the universal bit for that modifier.
         *
         *
         * If the specified meta state contains [.META_ALT_LEFT_ON] then
         * the result will also contain [.META_ALT_ON] in addition to [.META_ALT_LEFT_ON]
         * and the other bits that were specified in the input.  The same is process is
         * performed for shift, control and meta.
         *
         *
         * If the specified meta state contains synthetic meta states defined by
         * [MetaKeyKeyListener], then those states are translated here and the original
         * synthetic meta states are removed from the result.
         * [MetaKeyKeyListener.META_CAP_LOCKED] is translated to [.META_CAPS_LOCK_ON].
         * [MetaKeyKeyListener.META_ALT_LOCKED] is translated to [.META_ALT_ON].
         * [MetaKeyKeyListener.META_SYM_LOCKED] is translated to [.META_SYM_ON].
         *
         *
         * Undefined meta state bits are removed.
         *
         *
         * @param metaState The meta state.
         * @return The normalized meta state.
         */
        fun normalizeMetaState(metaState: Int): Int {
            var metaState = metaState
            if ((metaState and (META_SHIFT_LEFT_ON or META_SHIFT_RIGHT_ON)) != 0) {
                metaState = metaState or META_SHIFT_ON
            }
            if ((metaState and (META_ALT_LEFT_ON or META_ALT_RIGHT_ON)) != 0) {
                metaState = metaState or META_ALT_ON
            }
            if ((metaState and (META_CTRL_LEFT_ON or META_CTRL_RIGHT_ON)) != 0) {
                metaState = metaState or META_CTRL_ON
            }
            if ((metaState and (META_META_LEFT_ON or META_META_RIGHT_ON)) != 0) {
                metaState = metaState or META_META_ON
            }
            /*if ((metaState and MetaKeyKeyListener.META_CAP_LOCKED) !== 0) {
                metaState = metaState or META_CAPS_LOCK_ON
            }
            if ((metaState and MetaKeyKeyListener.META_ALT_LOCKED) !== 0) {
                metaState = metaState or META_ALT_ON
            }
            if ((metaState and MetaKeyKeyListener.META_SYM_LOCKED) !== 0) {
                metaState = metaState or META_SYM_ON
            }*/
            return metaState and META_ALL_MASK
        }

        /**
         * Returns true if no modifiers keys are pressed according to the specified meta state.
         *
         *
         * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
         * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
         * not considered modifier keys.  Consequently, this function ignores
         * [.META_CAPS_LOCK_ON], [.META_SCROLL_LOCK_ON] and [.META_NUM_LOCK_ON].
         *
         *
         * The meta state is normalized prior to comparison using [.normalizeMetaState].
         *
         *
         * @param metaState The meta state to consider.
         * @return True if no modifier keys are pressed.
         * @see .hasNoModifiers
         */
        fun metaStateHasNoModifiers(metaState: Int): Boolean {
            return (normalizeMetaState(metaState) and modifierMetaStateMask) == 0
        }

        /**
         * Returns true if only the specified modifier keys are pressed according to
         * the specified meta state.  Returns false if a different combination of modifier
         * keys are pressed.
         *
         *
         * For the purposes of this function, [.KEYCODE_CAPS_LOCK],
         * [.KEYCODE_SCROLL_LOCK], and [.KEYCODE_NUM_LOCK] are
         * not considered modifier keys.  Consequently, this function ignores
         * [.META_CAPS_LOCK_ON], [.META_SCROLL_LOCK_ON] and [.META_NUM_LOCK_ON].
         *
         *
         * If the specified modifier mask includes directional modifiers, such as
         * [.META_SHIFT_LEFT_ON], then this method ensures that the
         * modifier is pressed on that side.
         * If the specified modifier mask includes non-directional modifiers, such as
         * [.META_SHIFT_ON], then this method ensures that the modifier
         * is pressed on either side.
         * If the specified modifier mask includes both directional and non-directional modifiers
         * for the same type of key, such as [.META_SHIFT_ON] and [.META_SHIFT_LEFT_ON],
         * then this method throws an illegal argument exception.
         *
         *
         * @param metaState The meta state to consider.
         * @param modifiers The meta state of the modifier keys to check.  May be a combination
         * of modifier meta states as defined by [.getModifierMetaStateMask].  May be 0 to
         * ensure that no modifier keys are pressed.
         * @return True if only the specified modifier keys are pressed.
         * @throws IllegalArgumentException if the modifiers parameter contains invalid modifiers
         * @see .hasModifiers
         */
        fun metaStateHasModifiers(metaState: Int, modifiers: Int): Boolean {
            // Note: For forward compatibility, we allow the parameter to contain meta states
            //       that we do not recognize but we explicitly disallow meta states that
            //       are not valid modifiers.
            var metaState = metaState
            if ((modifiers and META_INVALID_MODIFIER_MASK) != 0) {
                throw Exception(
                    ("modifiers must not contain "
                            + "META_CAPS_LOCK_ON, META_NUM_LOCK_ON, META_SCROLL_LOCK_ON, "
                            + "META_CAP_LOCKED, META_ALT_LOCKED, META_SYM_LOCKED, "
                            + "or META_SELECTING")
                )
            }
            metaState = normalizeMetaState(metaState) and modifierMetaStateMask
            metaState = metaStateFilterDirectionalModifiers(
                metaState, modifiers,
                META_SHIFT_ON, META_SHIFT_LEFT_ON, META_SHIFT_RIGHT_ON
            )
            metaState = metaStateFilterDirectionalModifiers(
                metaState, modifiers,
                META_ALT_ON, META_ALT_LEFT_ON, META_ALT_RIGHT_ON
            )
            metaState = metaStateFilterDirectionalModifiers(
                metaState, modifiers,
                META_CTRL_ON, META_CTRL_LEFT_ON, META_CTRL_RIGHT_ON
            )
            metaState = metaStateFilterDirectionalModifiers(
                metaState, modifiers,
                META_META_ON, META_META_LEFT_ON, META_META_RIGHT_ON
            )
            return metaState == modifiers
        }

        private fun metaStateFilterDirectionalModifiers(
            metaState: Int,
            modifiers: Int, basic: Int, left: Int, right: Int
        ): Int {
            val wantBasic = (modifiers and basic) != 0
            val directional = left or right
            val wantLeftOrRight = (modifiers and directional) != 0
            if (wantBasic) {
                if (wantLeftOrRight) {
                    throw IllegalArgumentException(
                        ("modifiers must not contain "
                                + metaStateToString(basic) + " combined with "
                                + metaStateToString(left) + " or " + metaStateToString(right))
                    )
                }
                return metaState and directional.inv()
            } else return if (wantLeftOrRight) {
                metaState and basic.inv()
            } else {
                metaState
            }
        }

        /**
         * Returns a string that represents the symbolic name of the specified action
         * such as "ACTION_DOWN", or an equivalent numeric constant such as "35" if unknown.
         *
         * @param action The action.
         * @return The symbolic name of the specified action.
         * @hide
         */
        fun actionToString(action: Int): String {
            when (action) {
                ACTION_DOWN -> return "ACTION_DOWN"
                ACTION_UP -> return "ACTION_UP"
                ACTION_MULTIPLE -> return "ACTION_MULTIPLE"
                else -> return action.toString()
            }
        }

        /**
         * Returns a string that represents the symbolic name of the specified keycode
         * such as "KEYCODE_A", "KEYCODE_DPAD_UP", or an equivalent numeric constant
         * such as "1001" if unknown.
         *
         * This function is intended to be used mostly for debugging, logging, and testing. It is not
         * locale-specific and is not intended to be used in a user-facing manner.
         *
         * @param keyCode The key code.
         * @return The symbolic name of the specified keycode.
         *
         * @see KeyCharacterMap.getDisplayLabel
         */
        fun keyCodeToString(keyCode: Int): String {
            return keyCode.toString()
            val symbolicName = nativeKeyCodeToString(keyCode)
            return if (symbolicName != null) LABEL_PREFIX + symbolicName else keyCode.toString()
        }

        /**
         * Gets a keycode by its symbolic name such as "KEYCODE_A" or an equivalent
         * numeric constant such as "29". For symbolic names,
         * starting in [android.os.Build.VERSION_CODES.Q] the prefix "KEYCODE_" is optional.
         *
         * @param symbolicName The symbolic name of the keycode.
         * @return The keycode or [.KEYCODE_UNKNOWN] if not found.
         * @see .keyCodeToString
         */
        fun keyCodeFromString(symbolicName: String): Int {
            var symbolicName = symbolicName
            try {
                val keyCode = symbolicName.toInt()
                if (keyCodeIsValid(keyCode)) {
                    return keyCode
                }
            } catch (ex: NumberFormatException) {
            }
            if (symbolicName.startsWith(LABEL_PREFIX)) {
                symbolicName = symbolicName.substring(LABEL_PREFIX.length)
            }
            val keyCode = nativeKeyCodeFromString(symbolicName)
            return if (keyCodeIsValid(keyCode)) {
                keyCode
            } else KEYCODE_UNKNOWN
        }

        private fun keyCodeIsValid(keyCode: Int): Boolean {
            return keyCode >= KEYCODE_UNKNOWN && keyCode <= maxKeyCode
        }

        /**
         * Returns a string that represents the symbolic name of the specified combined meta
         * key modifier state flags such as "0", "META_SHIFT_ON",
         * "META_ALT_ON|META_SHIFT_ON" or an equivalent numeric constant such as "0x10000000"
         * if unknown.
         *
         * @param metaState The meta state.
         * @return The symbolic name of the specified combined meta state flags.
         * @hide
         */
        fun metaStateToString(metaState: Int): String {
            var metaState = metaState
            if (metaState == 0) {
                return "0"
            }
            var result: StringBuilder? = null
            var i = 0
            while (metaState != 0) {
                val isSet = (metaState and 1) != 0
                metaState = metaState ushr 1 // unsigned shift!
                if (isSet) {
                    val name = META_SYMBOLIC_NAMES[i]
                    if (result == null) {
                        if (metaState == 0) {
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
    }
}