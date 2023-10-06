package com.olekdia.sparsearray

import kotlin.math.min

val INVALID = -1

@Suppress("UNCHECKED_CAST")
open class IntSparseArray<E>(
    private var initialCapacity: Int = 10
) : MutableMap<Int, E>,
    Iterable<Map.Entry<Int, E>> {

    private var garbage = false
    private var _keys: IntArray
    private var _values: Array<Any?>
    private var _size: Int

    /**
     * Creates a new SparseArray containing no mappings that will not
     * require any additional memory allocation to store the specified
     * number of mappings.  If you supply an initial capacity of 0, the
     * sparse array will be initialized with a light-weight representation
     * not requiring any additional array allocations.
     */
    init {
        if (initialCapacity == 0) {
            _keys = intArrayOf()
            _values = arrayOf()
        } else {
            initialCapacity = idealIntArraySize(initialCapacity)
            _keys = IntArray(initialCapacity)
            _values = arrayOfNulls(initialCapacity)
        }
        _size = 0
    }

//--------------------------------------------------------------------------------------------------
//  Map methods
//--------------------------------------------------------------------------------------------------

    override val size: Int
        get() {
            if (garbage) gc()
            return _size
        }

    override fun isEmpty(): Boolean {
        if (garbage) gc()
        return size == 0
    }

    override fun containsKey(key: Int): Boolean {
        if (garbage) gc()
        return indexOfKey(key) >= 0
    }

    override fun containsValue(value: E): Boolean {
        if (garbage) gc()
        return indexOfEqualValue(value) >= 0
    }

    override operator fun get(key: Int): E? =
        _keys.binarySearch(key, toIndex = _size).let { i ->
            if (i < 0) {
                null
            } else {
                _values[i].let { value ->
                    if (value === DELETED) {
                        null
                    } else {
                        value as? E
                    }
                }
            }
        }

    operator fun get(key: Int, defaultValue: E): E = getOrDefault(key, defaultValue)

    fun getOrDefault(key: Int, defaultValue: E): E =
        _keys.binarySearch(key, toIndex = _size).let { i ->
            if (i < 0) {
                defaultValue
            } else {
                _values[i].let { value ->
                    if (value === DELETED) {
                        defaultValue
                    } else {
                        value as? E ?: defaultValue
                    }
                }
            }
        }

    override val keys: MutableSet<Int>
        get() {
            if (garbage) gc()

            return LinkedHashSet<Int>(_size).also {
                for (i in 0 until _size) {
                    it.add(_keys[i])
                }
            }
        }

    override val values: MutableList<E>
        get() {
            if (garbage) gc()

            return MutableList<E>(_size) {
                _values[it] as E
            }
        }

    override val entries: MutableSet<MutableMap.MutableEntry<Int, E>>
        get() {
            if (garbage) gc()

            return LinkedHashSet<MutableMap.MutableEntry<Int, E>>(_size).also {
                for (i in 0 until _size) {
                    it.add(MutableEntry(_keys[i], _values[i] as E))
                }
            }
        }

    /**
     * Adds a mapping from the specified key to the specified value,
     * replacing the previous mapping from the specified key if there
     * was one.
     */
    override fun put(key: Int, value: E): E? {
        var i: Int = _keys.binarySearch(key, toIndex = _size)

        if (i >= 0) {
            val oldValue: E? = _values[i] as? E
            _values[i] = value
            return oldValue
        } else {
            i = i.inv()

            if (i < _size && _values[i] === DELETED) {
                _keys[i] = key
                _values[i] = value
                return null
            }

            if (garbage && _size >= _keys.size) {
                gc()
                // Search again because indices may have changed.
                i = _keys.binarySearch(key, toIndex = _size)
                if (i < 0) {
                    i = i.inv()
                }
            }

            if (_size >= _keys.size) {
                expandToCapacity(idealIntArraySize(_size + 1))
            }

            if (_size - i != 0) {
                _keys.copyInto(_keys, i + 1, i, _size)
                _values.copyInto(_values, i + 1, i, _size)
            }

            _keys[i] = key
            _values[i] = value
            _size++
        }

        return null
    }

    /**
     * Removes the mapping from the specified key, if there was any.
     * @return true if item was found and deleted, false otherwise
     */
    override fun remove(key: Int): E? =
        _keys.binarySearch(key, toIndex = _size).let { i ->
            if (i >= 0 && _values[i] !== DELETED) {
                val removedValue: E? = _values[i] as? E
                _values[i] = DELETED
                garbage = true
                removedValue
            } else {
                null
            }
        }

    /**
     * Removes the entry for the specified key only if it is mapped to the specified value.
     *
     * @return true if entry was removed
     */
    fun remove(key: Int, valueToRemove: E): Boolean {
        val i: Int = _keys.binarySearch(key, toIndex = _size)

        if (i >= 0) {
            val value: Any? = _values[i]

            if (value !== DELETED
                && (value as? E) == valueToRemove
            ) {
                _values[i] = DELETED
                garbage = true
                return true
            }
        }

        return false
    }

    override fun putAll(from: Map<out Int, E>) {
        for (item in from) {
            put(item.key, item.value)
        }
    }

    /**
     * Removes all key-value mappings from this SparseArray.
     */
    override fun clear() {
        for (i in 0 until _size) {
            _values[i] = null
        }
        _size = 0
        garbage = false
    }

//--------------------------------------------------------------------------------------------------
//  Other methods
//--------------------------------------------------------------------------------------------------

    /**
     * Removes the mapping at the specified index.
     */
    fun removeAt(index: Int) {
        if (index in 0 until _size
            && _values[index] !== DELETED
        ) {
            _values[index] = DELETED
            garbage = true
        }
    }

    /**
     * Remove a range of mappings as a batch.
     *
     * @param index Index to begin at
     * @param size Number of mappings to remove
     */
    fun removeAtRange(index: Int, size: Int) {
        if (index in 0 until _size
            && size > 0
        ) {
            val end: Int = min(_size, index + size)
            for (i in index until end) {
                removeAt(i)
            }
        }
    }

    /**
     * Given an index in the range `0...size()-1`, returns
     * the key from the `index`th key-value mapping that this
     * SparseArray stores.
     */
    fun keyAt(index: Int): Int? =
        if (index in 0 until size) {
            _keys[index]
        } else {
            null
        }

    /**
     * Given an index in the range `0...size()-1`, returns
     * the value from the `index`th key-value mapping that this
     * SparseArray stores.
     */
    fun valueAt(index: Int): E? =
        if (index in 0 until size) {
            _values[index] as? E
        } else {
            null
        }

    fun setKeyAt(index: Int, key: Int): Boolean =
        if (index in 0 until size) {
            _keys[index] = key
            true
        } else {
            false
        }

    /**
     * Returns the index for which [.keyAt] would return the
     * specified key, or a negative number if the specified
     * key is not mapped.
     */
    fun indexOfKey(key: Int): Int {
        if (garbage) gc()
        return _keys.binarySearch(key, toIndex = _size)
    }

    /**
     * Returns an index for which [.valueAt] would return the
     * specified key, or a negative number if no keys map to the
     * specified value.
     *
     * Beware that this is a linear search, unlike lookups by key,
     * and that multiple keys can map to the same value and this will
     * find only one of them.
     *
     * Note also that unlike most collections' `indexOf` methods,
     * this method compares values using `===` rather than `==`.
     */
    fun indexOfSameValue(value: E): Int {
        if (garbage) gc()
        for (i in 0 until _size) {
            if (_values[i] === value) return i
        }
        return INVALID
    }

    fun indexOfEqualValue(value: E): Int {
        if (garbage) gc()
        if (value === null) {
            for (i in 0 until _size) {
                if (_values[i] === null) return i
            }
        } else {
            for (i in 0 until _size) {
                if (value == _values[i]) return i
            }
        }
        return INVALID
    }

    /**
     * Increases the size of the underlying storage if needed, to ensure that it can
     * hold the specified number of items without having to allocate additional memory
     * @param capacity the number of items
     */
    fun ensureCapacity(capacity: Int): Int {
        if (garbage && _size >= _keys.size) gc()
        if (_keys.size < capacity) {
            return expandToCapacity(capacity)
        }

        return INVALID
    }

    private fun expandToCapacity(capacity: Int): Int {
        val newKeys = IntArray(capacity)
        val newValues = arrayOfNulls<Any>(capacity)

        _keys.copyInto(newKeys)
        _values.copyInto(newValues)

        _keys = newKeys
        _values = newValues

        return _keys.size
    }

    /**
     * Puts a key/value pair into the array, optimizing for the case where
     * the key is greater than all existing keys in the array.
     */
    fun append(key: Int, value: E) {
        if (_size != 0 && key <= _keys[_size - 1]) {
            put(key, value)
            return
        }

        if (garbage && _size >= _keys.size) gc()

        if (_size >= _keys.size) {
            expandToCapacity(idealIntArraySize(_size + 1))
        }

        _keys[_size] = key
        _values[_size] = value
        _size++
    }

    private fun gc() {
        var o = 0

        for (i in 0 until _size) {
            val value: Any? = _values[i]
            if (value !== DELETED) {
                if (i != o) {
                    _keys[o] = _keys[i]
                    _values[o] = value
                    _values[i] = null
                }
                o++
            }
        }

        garbage = false
        _size = o
    }

//--------------------------------------------------------------------------------------------------
//  Iterator
//--------------------------------------------------------------------------------------------------

    override fun iterator(): MutableIterator<Map.Entry<Int, E>> {
        if (garbage) gc()
        return object : MutableIterator<Map.Entry<Int, E>> {
            var i: Int = 0

            override fun hasNext(): Boolean = i < _size

            override fun next(): Map.Entry<Int, E> {
                val entry = Entry(_keys[i], _values[i] as E)
                i++
                return entry
            }

            override fun remove() {
                val pos: Int = i - 1
                removeAt(pos)
            }
        }
    }

    val keysIterator: Iterator<Int>
        get() {
            if (garbage) gc()
            return object : Iterator<Int> {
                var i: Int = 0

                override fun hasNext(): Boolean = i < _size
                override fun next(): Int {
                    val key: Int = _keys[i]
                    i++
                    return key
                }
            }
        }

    val valuesIterator: Iterator<E>
        get() {
            if (garbage) gc()
            return object : Iterator<E> {
                var i: Int = 0

                override fun hasNext(): Boolean = i < _size
                override fun next(): E {
                    val value: E = _values[i] as E
                    i++
                    return value
                }
            }
        }

    class Entry<E>(
        override val key: Int,
        override val value: E
    ) : Map.Entry<Int, E>

    class MutableEntry<E>(
        override val key: Int,
        override var value: E
    ) : MutableMap.MutableEntry<Int, E> {
        override fun setValue(newValue: E): E {
            val oldValue: E = value
            value = newValue
            return oldValue
        }
    }

    companion object {
        private val DELETED = Any()
    }
}