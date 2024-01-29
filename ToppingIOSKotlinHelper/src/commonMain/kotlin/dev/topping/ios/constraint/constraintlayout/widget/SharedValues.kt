package dev.topping.ios.constraint.constraintlayout.widget

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

/**
 * Shared values
 */
class SharedValues {
    private val mValues: MutableMap<String, Int> = mutableMapOf()
    @OptIn(ExperimentalNativeApi::class)
    private val mValuesListeners: MutableMap<String, MutableSet<WeakReference<SharedValuesListener>>> =
        mutableMapOf()

    /**
     * interface for listeners
     */
    interface SharedValuesListener {
        // @TODO: add description
        /**
         *
         * @param key
         * @param newValue
         * @param oldValue
         */
        fun onNewValue(key: String, newValue: Int, oldValue: Int)
    }

    /**
     * Add a listener for a key
     * @param key
     * @param listener
     */
    @OptIn(ExperimentalNativeApi::class)
    fun addListener(key: String, listener: SharedValuesListener?) {
        var listeners: MutableSet<WeakReference<SharedValuesListener>>? =
            mValuesListeners[key]
        if (listeners == null) {
            listeners = mutableSetOf()
            mValuesListeners.put(key, listeners)
        }
        listeners.add(WeakReference(listener!!))
    }

    /**
     * Remove listener for a key (will not be removed for other keys)
     * @param key
     * @param listener
     */
    @OptIn(ExperimentalNativeApi::class)
    fun removeListener(key: String, listener: SharedValuesListener) {
        val listeners: MutableSet<WeakReference<SharedValuesListener>> =
            mValuesListeners[key]
                ?: return
        val toRemove: MutableList<WeakReference<SharedValuesListener>> =
            mutableListOf()
        for (listenerWeakReference in listeners) {
            val l: SharedValuesListener? = listenerWeakReference.get()
            if (l == null || l == listener) {
                toRemove.add(listenerWeakReference)
            }
        }
        listeners.removeAll(toRemove)
    }

    /**
     * Remove a listener
     * @param listener
     */
    @OptIn(ExperimentalNativeApi::class)
    fun removeListener(listener: SharedValuesListener) {
        for (key in mValuesListeners.keys) {
            removeListener(key, listener)
        }
    }

    /**
     * remove all listeners
     */
    @OptIn(ExperimentalNativeApi::class)
    fun clearListeners() {
        mValuesListeners.clear()
    }

    /**
     * get the value from the map
     * @param key
     * @return
     */
    fun getValue(key: String): Int {
        return mValues[key] ?: UNSET
    }

    /**
     * notify that value has changed
     * @param key
     * @param value
     */
    @OptIn(ExperimentalNativeApi::class)
    fun fireNewValue(key: String, value: Int) {
        var needsCleanup = false
        val previousValue = mValues[key] ?: UNSET
        if (previousValue == value) {
            // don't send the value to listeners if it's the same one.
            return
        }
        mValues.put(key, value)
        val listeners: MutableSet<WeakReference<SharedValuesListener>> =
            mValuesListeners[key]
                ?: return
        for (listenerWeakReference in listeners) {
            val l: SharedValuesListener? = listenerWeakReference.get()
            if (l != null) {
                l.onNewValue(key, value, previousValue)
            } else {
                needsCleanup = true
            }
        }
        if (needsCleanup) {
            val toRemove: MutableList<WeakReference<SharedValuesListener>> =
                mutableListOf()
            for (listenerWeakReference in listeners) {
                val listener: SharedValuesListener? = listenerWeakReference.get()
                if (listener == null) {
                    toRemove.add(listenerWeakReference)
                }
            }
            listeners.removeAll(toRemove)
        }
    }

    companion object {
        const val UNSET = -1
        const val UNSET_ID = ""
    }
}