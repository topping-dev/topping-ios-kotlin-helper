package dev.topping.ios.constraint

data class Pointer<T>(   // It's a generic wrap class for scalar type T
    var v:T
)

class BitSet32 {
    var value: Pointer<UInt>
    constructor() {
        value = Pointer(0U)
    }
    constructor(value: UInt) {
        this.value = Pointer(value)
    }
    constructor(value: Pointer<UInt>) {
        this.value = Pointer(value.v)
    }

    companion object {
        fun valueForBit(n: UInt) : UInt {
            return (0x80000000 shr n.toInt()).toUInt()
        }
        inline fun clear(value: Pointer<UInt>) { value.v = 0U }
        inline fun count(value: UInt): Int { return value.countOneBits() }
        inline fun isEmpty(value: UInt): Boolean { return value == 0U }
        inline fun isFull(value: UInt): Boolean { return value == 0xffffffffU }
        inline fun hasBit(value:UInt, n: UInt): Boolean { return (value and valueForBit(n)) > 0UL }
        inline fun markBit(value:Pointer<UInt>, n: UInt)
        {
            value.v = value.v or valueForBit(n)
        }
        inline fun clearBit(value:Pointer<UInt>, n: UInt)
        {
            value.v = value.v and (valueForBit(n).inv())
        }
        inline fun firstMarkedBit(value: UInt): UInt { return value.countLeadingZeroBits().toUInt() }
        inline fun firstUnmarkedBit(value: UInt): UInt { return value.inv().countLeadingZeroBits().toUInt() }
        inline fun lastMarkedBit(value: UInt): UInt { return (31 - value.countTrailingZeroBits()).toUInt() }
        inline fun clearFirstMarkedBit(value: UInt): UInt
        {
            val n = firstMarkedBit(value)
            clearBit(Pointer(value), n)
            return n
        }
        inline fun markFirstUnmarkedBit(value: UInt): UInt
        {
            val n = firstUnmarkedBit(value)
            markBit(Pointer(value), n)
            return n
        }
        inline fun clearLastMarkedBit(value: UInt): UInt
        {
            val n = lastMarkedBit(value)
            clearBit(Pointer(value), n)
            return n
        }
        inline fun getIndexOfBit(value: UInt, n: UInt): UInt { return (value and (0xffffffffU shr n.toInt()).inv()).countOneBits().toUInt() }
    }

    inline fun clear() { clear(value) }
    inline fun count(): Int { return count(value.v) }
    inline fun isEmpty(): Boolean { return isEmpty(value.v) }
    inline fun isFull(): Boolean { return isFull(value.v) }
    inline fun hasBit(n: UInt): Boolean { return hasBit(value.v, n) }
    inline fun markBit(n: UInt) { markBit(value, n) }
    inline fun clearBit(n: UInt) { clearBit(value, n) }
    inline fun firstMarkedBit(): UInt { return firstMarkedBit(value.v) }
    inline fun firstUnmarkedBit(): UInt { return firstUnmarkedBit(value.v) }
    inline fun lastMarkedBit(): UInt { return lastMarkedBit(value.v) }
    inline fun clearFirstMarkedBit(): UInt { return clearFirstMarkedBit(value.v) }
    inline fun markFirstUnmarkedBit(): UInt { return markFirstUnmarkedBit(value.v) }
    inline fun clearLastMarkedBit(): UInt { return clearLastMarkedBit(value.v) }
    inline fun getIndexOfBit(n: UInt): UInt { return getIndexOfBit(value.v, n) }


    override fun equals(other: Any?): Boolean {
        if(other is BitSet32) {
            return value == other.value
        }
        return false
    }
}