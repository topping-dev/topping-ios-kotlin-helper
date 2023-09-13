package dev.topping.ios.constraint.shared.graphics

class kVectorPointer<T>(var v: T) {
    var previous: kVectorPointer<T>? = null
    var next: kVectorPointer<T>? = null
    /*lateinit var listStorage : MutableList<kVectorPointer<T>>
    val virtualAddress: Int
        get() = listStorage.indexOf(this)*/
    var virtualAddress = 0

    operator fun minus(other: Int) : kVectorPointer<T> {
        var count = 0
        var result: kVectorPointer<T>? = this
        while(count <= other && result != null) {
            result = result.previous
            count++
        }
        return result!!
    }

    operator fun plus(other: Int) : kVectorPointer<T> {
        var count = 0
        var result: kVectorPointer<T>? = this
        while(count <= other && result != null) {
            result = result.next
            count++
        }
        return result!!
    }

    operator fun dec(): kVectorPointer<T> {
        return previous!!
    }

    operator fun inc(): kVectorPointer<T> {
        return next!!
    }

    operator fun get(index: Int): kVectorPointer<T> {
        return this + index
    }

    operator fun set(index: Int, v: T) {
        (this + index).v = v
    }

    operator fun compareTo(other: kVectorPointer<T>) : Int {
        return this.compareTo(other)
    }

    override fun equals(other: Any?): Boolean {
        if(other is kVectorPointer<*>) {
            return this.v?.equals(other.v) ?: false
        }
        return super.equals(other)
    }
}

class FatVector<T> : MutableList<kVectorPointer<T>> by mutableListOf() {
    fun push_back(v: T) {
        val last = last()
        val ptr = kVectorPointer(v)
        ptr.previous = last
        last.next = ptr
        //ptr.listStorage = this
        ptr.virtualAddress = size
        add(ptr)
    }

    fun push_back(ptr: kVectorPointer<T>) {
        val last = last()
        ptr.previous = last
        last.next = ptr
        //ptr.listStorage = this
        ptr.virtualAddress = size
        add(ptr)
    }

    fun begin(): kVectorPointer<T> = first()
    fun end(): kVectorPointer<T> = last()
    fun data(): kVectorPointer<T> = first()
    fun front(): T = first().v
    fun back(): T = last().v
    fun insert(position: kVectorPointer<T>, first: kVectorPointer<T>, last: kVectorPointer<T>) {
        val oldNext = position.next

        //Remove right side from array
        val toRemove = mutableListOf<kVectorPointer<T>>()
        if(oldNext != null) {
            for(i in oldNext.virtualAddress .. size) {
                toRemove.add(this[i])
            }
            removeAll(toRemove)
        }

        var current: kVectorPointer<T>? = first
        while (current != (last + 1)) {
            if(current == null)
                break
            push_back(current)
            current = current.next
        }
    }
}