package com.olekdia.sparsearray

import kotlin.math.min

class LongToLongSparseArray(
    private var initialCapacity: Int = 10
) : MutableMap<Long, Long>,
    Iterable<Map.Entry<Long, Long>> {

    private var _keys: LongArray
    private var _values: LongArray
    private var _size: Int

    /**
     * Creates a new SparseLongArray containing no mappings that will not
     * require any additional memory allocation to store the specified
     * number of mappings.  If you supply an initial capacity of 0, the
     * sparse array will be initialized with a light-weight representation
     * not requiring any additional array allocations.
     */
    /**
     * Creates a new SparseLongArray containing no mappings.
     */
    init {
        if (initialCapacity == 0) {
            _keys = longArrayOf()
            _values = longArrayOf()
        } else {
            initialCapacity = idealIntArraySize(initialCapacity)
            _keys = LongArray(initialCapacity)
            _values = LongArray(initialCapacity)
        }
        _size = 0
    }

//--------------------------------------------------------------------------------------------------
//  Map methods
//--------------------------------------------------------------------------------------------------

    override val size: Int
        get() {
            return _size
        }

    override fun isEmpty(): Boolean = size == 0

    override fun containsKey(key: Long): Boolean = indexOfKey(key) >= 0

    override fun containsValue(value: Long): Boolean = indexOfValue(value) >= 0

    override operator fun get(key: Long): Long? =
        _keys.binarySearch(key, toIndex = _size).let { i ->
            if (i < 0) null else _values[i]
        }

    operator fun get(key: Long, defaultValue: Long): Long = getOrDefault(key, defaultValue)

    fun getOrDefault(key: Long, defaultValue: Long): Long =
        _keys.binarySearch(key, toIndex = _size).let { i ->
            if (i < 0) defaultValue else _values[i]
        }

    override val keys: MutableSet<Long>
        get() = LinkedHashSet<Long>(_size).also {
            for (i in 0 until _size) {
                it.add(_keys[i])
            }
        }

    override val values: MutableList<Long>
        get() = MutableList(_size) {
            _values[it]
        }

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Long>>
        get() = LinkedHashSet<MutableMap.MutableEntry<Long, Long>>(_size).also {
            for (i in 0 until _size) {
                it.add(MutableEntry(_keys[i], _values[i]))
            }
        }

    /**
     * Adds a mapping from the specified key to the specified value,
     * replacing the previous mapping from the specified key if there
     * was one.
     */
    override fun put(key: Long, value: Long): Long? {
        var i: Int = _keys.binarySearch(key, toIndex = _size)

        if (i >= 0) {
            val oldValue: Long = _values[i]
            _values[i] = value
            return oldValue
        } else {
            i = i.inv()

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
     */
    override fun remove(key: Long): Long? =
        _keys.binarySearch(key, toIndex = _size).let { i ->
            if (i >= 0) {
                val removedValue: Long = _values[i]

                _keys.copyInto(_keys, i, i + 1, _size)
                _values.copyInto(_values, i, i + 1, _size)
                _size--

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
    fun remove(key: Long, valueToRemove: Long): Boolean =
        _keys.binarySearch(key, toIndex = _size).let { i ->
            if (i >= 0 && _values[i] == valueToRemove) {
                _keys.copyInto(_keys, i, i + 1, _size)
                _values.copyInto(_values, i, i + 1, _size)
                _size--

                true
            } else {
                false
            }
        }

    override fun putAll(from: Map<out Long, Long>) {
        for (item in from) {
            put(item.key, item.value)
        }
    }

    /**
     * Removes all key-value mappings from this SparseLongArray.
     */
    override fun clear() {
        _size = 0
    }

//--------------------------------------------------------------------------------------------------
//  Other methods
//--------------------------------------------------------------------------------------------------

    /**
     * Removes the mapping at the specified index.
     *
     *
     * For indices outside of the range `0...size()-1`, the behavior is undefined.
     */
    fun removeAt(index: Int) {
        if (index in 0 until _size) {
            _keys.copyInto(_keys, index, index + 1, _size)
            _values.copyInto(_values, index, index + 1, _size)
            _size--
        }
    }

    fun removeAtRange(index: Int, size: Int) {
        if (index in 0 until _size
            && size > 0
        ) {
            val end: Int = min(_size, index + size)
            _keys.copyInto(_keys, index, end, _size)
            _values.copyInto(_values, index, end, _size)
            _size -= end - index
        }
    }

    /**
     * Given an index in the range `0...size()-1`, returns
     * the key from the `index`th key-value mapping that this
     * SparseLongArray stores.
     *
     *
     * The keys corresponding to indices in ascending order are guaranteed to
     * be in ascending order, e.g., `keyAt(0)` will return the
     * smallest key and `keyAt(size()-1)` will return the largest
     * key.
     */
    fun keyAt(index: Int): Long? = if (index in 0 until _size) _keys[index] else null

    /**
     * Given an index in the range `0...size()-1`, returns
     * the value from the `index`th key-value mapping that this
     * SparseLongArray stores.
     *
     *
     * The values corresponding to indices in ascending order are guaranteed
     * to be associated with keys in ascending order, e.g.,
     * `valueAt(0)` will return the value associated with the
     * smallest key and `valueAt(size()-1)` will return the value
     * associated with the largest key.
     */
    fun valueAt(index: Int): Long? =
        if (index in 0 until _size) _values[index] else null

    fun setKeyAt(index: Int, key: Long): Boolean =
        if (index in 0 until _size) {
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
    fun indexOfKey(key: Long): Int = _keys.binarySearch(key, toIndex = _size)

    /**
     * Returns an index for which [.valueAt] would return the
     * specified key, or a negative number if no keys map to the
     * specified value.
     * Beware that this is a linear search, unlike lookups by key,
     * and that multiple keys can map to the same value and this will
     * find only one of them.
     */
    fun indexOfValue(value: Long): Int {
        for (i in 0 until _size) {
            if (_values[i] == value) return i
        }
        return INVALID
    }

    /**
     * Increases the size of the underlying storage if needed, to ensure that it can
     * hold the specified number of items without having to allocate additional memory
     * @param capacity the number of items
     */
    fun ensureCapacity(capacity: Int): Int {
        if (_keys.size < capacity) {
            return expandToCapacity(capacity)
        }

        return INVALID
    }

    private fun expandToCapacity(capacity: Int): Int {
        val newKeys = LongArray(capacity)
        val newValues = LongArray(capacity)

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
    fun append(key: Long, value: Long) {
        if (_size != 0 && key <= _keys[_size - 1]) {
            put(key, value)
            return
        }

        if (_size >= _keys.size) {
            expandToCapacity(idealIntArraySize(_size + 1))
        }

        _keys[_size] = key
        _values[_size] = value
        _size++
    }

//--------------------------------------------------------------------------------------------------
//  Iterator
//--------------------------------------------------------------------------------------------------

    override fun iterator(): MutableIterator<Map.Entry<Long, Long>> {
        return object : MutableIterator<Map.Entry<Long, Long>> {
            var i: Int = 0

            override fun hasNext(): Boolean = i < _size

            override fun next(): Map.Entry<Long, Long> {
                val entry = Entry(_keys[i], _values[i])
                i++
                return entry
            }

            override fun remove() {
                i--
                removeAt(i)
            }
        }
    }

    val keysIterator: Iterator<Long>
        get() = object : Iterator<Long> {
            var i: Int = 0

            override fun hasNext(): Boolean = i < _size
            override fun next(): Long {
                val key: Long = _keys[i]
                i++
                return key
            }
        }
    val valuesIterator: Iterator<Long>
        get() = object : Iterator<Long> {
            var i: Int = 0

            override fun hasNext(): Boolean = i < _size
            override fun next(): Long {
                val value: Long = _values[i]
                i++
                return value
            }
        }

    class Entry(
        override val key: Long,
        override val value: Long
    ) : Map.Entry<Long, Long>

    class MutableEntry(
        override val key: Long,
        override var value: Long
    ) : MutableMap.MutableEntry<Long, Long> {
        override fun setValue(newValue: Long): Long {
            val oldValue: Long = value
            value = newValue
            return oldValue
        }
    }
}