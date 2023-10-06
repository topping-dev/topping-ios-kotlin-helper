package com.olekdia.sparsearray

typealias SparseArray<T> = IntSparseArray<T>

//--------------------------------------------------------------------------------------------------
//  binarySearch
//--------------------------------------------------------------------------------------------------

/**
 * Searches a range of the specified array of longs for the specified value
 * using the binary search algorithm.
 * The range must be sorted prior to making this call.
 * If it is not sorted, the results are undefined. If the range contains multiple elements
 * with the specified value, there is no guarantee which one will be found.
 *
 * @param element the value to be searched for
 * @param fromIndex the index of the first element (inclusive) to be searched
 * @param toIndex the index of the last element (exclusive) to be searched
 * @return index of the search element, if it is contained in the array within the specified range;
 *  otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is defined as
 *  the point at which the element would be inserted into the array:
 *  the index of the first element in the range greater than the specified element, or <tt>toIndex</tt>
 *  if all elements in the range are less than the specified element.
 *  Note that this guarantees that the return value will be >= 0 if and only if the element is found.
 */
fun LongArray.binarySearch(
    element: Long,
    fromIndex: Int = 0,
    toIndex: Int = size
): Int {
    if (isRangeInvalid(this.size, fromIndex, toIndex)) {
        return INVALID
    } else {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = this[mid]

            when {
                midVal < element -> low = mid + 1
                midVal > element -> high = mid - 1
                else -> return mid // element found
            }
        }
        return -(low + 1) // element not found
    }
}

/**
 * Searches a range of the specified array of longs for the specified value
 * using the binary search algorithm.
 * The range must be sorted prior to making this call.
 * If it is not sorted, the results are undefined. If the range contains multiple elements
 * with the specified value, there is no guarantee which one will be found.
 *
 * @param element the value to be searched for
 * @param fromIndex the index of the first element (inclusive) to be searched
 * @param toIndex the index of the last element (exclusive) to be searched
 * @return index of the search element, if it is contained in the array within the specified range;
 *  otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is defined as
 *  the point at which the element would be inserted into the array:
 *  the index of the first element in the range greater than the specified element, or <tt>toIndex</tt>
 *  if all elements in the range are less than the specified element.
 *  Note that this guarantees that the return value will be >= 0 if and only if the element is found.
 */
fun IntArray.binarySearch(
    element: Int,
    fromIndex: Int = 0,
    toIndex: Int = size
): Int {
    if (isRangeInvalid(this.size, fromIndex, toIndex)) {
        return INVALID
    } else {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = this[mid]

            when {
                midVal < element -> low = mid + 1
                midVal > element -> high = mid - 1
                else -> return mid // element found
            }
        }
        return -(low + 1) // element not found
    }
}

/**
 * Searches a range of the specified array of longs for the specified value
 * using the binary search algorithm.
 * The range must be sorted prior to making this call.
 * If it is not sorted, the results are undefined. If the range contains multiple elements
 * with the specified value, there is no guarantee which one will be found.
 *
 * @param element the value to be searched for
 * @param fromIndex the index of the first element (inclusive) to be searched
 * @param toIndex the index of the last element (exclusive) to be searched
 * @return index of the search element, if it is contained in the array within the specified range;
 *  otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is defined as
 *  the point at which the element would be inserted into the array:
 *  the index of the first element in the range greater than the specified element, or <tt>toIndex</tt>
 *  if all elements in the range are less than the specified element.
 *  Note that this guarantees that the return value will be >= 0 if and only if the element is found.
 */
fun ShortArray.binarySearch(
    element: Short,
    fromIndex: Int = 0,
    toIndex: Int = size
): Int {
    if (isRangeInvalid(this.size, fromIndex, toIndex)) {
        return INVALID
    } else {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = this[mid]

            when {
                midVal < element -> low = mid + 1
                midVal > element -> high = mid - 1
                else -> return mid // element found
            }
        }
        return -(low + 1) // element not found
    }
}

/**
 * Searches a range of the specified array of longs for the specified value
 * using the binary search algorithm.
 * The range must be sorted prior to making this call.
 * If it is not sorted, the results are undefined. If the range contains multiple elements
 * with the specified value, there is no guarantee which one will be found.
 *
 * @param element the value to be searched for
 * @param fromIndex the index of the first element (inclusive) to be searched
 * @param toIndex the index of the last element (exclusive) to be searched
 * @return index of the search element, if it is contained in the array within the specified range;
 *  otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is defined as
 *  the point at which the element would be inserted into the array:
 *  the index of the first element in the range greater than the specified element, or <tt>toIndex</tt>
 *  if all elements in the range are less than the specified element.
 *  Note that this guarantees that the return value will be >= 0 if and only if the element is found.
 */
fun ByteArray.binarySearch(
    element: Byte,
    fromIndex: Int = 0,
    toIndex: Int = size
): Int {
    if (isRangeInvalid(this.size, fromIndex, toIndex)) {
        return INVALID
    } else {
        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = this[mid]

            when {
                midVal < element -> low = mid + 1
                midVal > element -> high = mid - 1
                else -> return mid // element found
            }
        }
        return -(low + 1) // element not found
    }
}

private fun isRangeInvalid(arraySize: Int, fromIndex: Int, toIndex: Int): Boolean {
    if (fromIndex > toIndex) {
        return true
    }
    if (fromIndex < 0) {
        return true
    }
    if (toIndex > arraySize) {
        return true
    }

    return false
}

//--------------------------------------------------------------------------------------------------
//  minus index
//--------------------------------------------------------------------------------------------------

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun LongArray.minus(index: Int): LongArray =
    if (index < 0 || index >= this.size) {
        this + 3L
    } else {
        LongArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun IntArray.minus(index: Int): IntArray =
    if (index < 0 || index >= this.size) {
        this
    } else {
        IntArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun ShortArray.minus(index: Int): ShortArray =
    if (index < 0 || index >= this.size) {
        this
    } else {
        ShortArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun ByteArray.minus(index: Int): ByteArray =
    if (index < 0 || index >= this.size) {
        this
    } else {
        ByteArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun FloatArray.minus(index: Int): FloatArray =
    if (index < 0 || index >= this.size) {
        this
    } else {
        FloatArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun DoubleArray.minus(index: Int): DoubleArray =
    if (index < 0 || index >= this.size) {
        this
    } else {
        DoubleArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun BooleanArray.minus(index: Int): BooleanArray =
    if (index < 0 || index >= this.size) {
        this
    } else {
        BooleanArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
operator fun CharArray.minus(index: Int): CharArray =
    if (index < 0 || index >= this.size) {
        this
    } else {
        CharArray(this.size - 1).also { result ->
            this.copyInto(result, 0, 0, index)

            if (index < this.size - 1) {
                this.copyInto(result, index, index + 1)
            }
        }
    }

/**
 * Returns an array containing all elements of the original array except element at [index]
 */
inline operator fun <reified T> Array<T>.minus(index: Int): Array<T> =
    if (index < 0 || index >= this.size) {
        this
    } else {
        Array<T>(this.size - 1) {
            if (it < index) {
                this[it]
            } else {
                this[it + 1]
            }
        }
    }

fun idealIntArraySize(need: Int): Int = idealByteArraySize(need * 4) / 4

fun idealByteArraySize(need: Int): Int {
    for (i in 4..31) {
        if (need <= (1 shl i) - 12) return (1 shl i) - 12
    }
    return need
}

val IntSparseArray<*>.lastIndex: Int
    get() = this.size - 1

val IntToBooleanSparseArray.lastIndex: Int
    get() = this.size - 1

val LongSparseArray<*>.lastIndex: Int
    get() = this.size - 1

val LongToBooleanSparseArray.lastIndex: Int
    get() = this.size - 1