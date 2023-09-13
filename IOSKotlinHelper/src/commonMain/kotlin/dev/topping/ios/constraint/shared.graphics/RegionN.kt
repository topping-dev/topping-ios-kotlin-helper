package dev.topping.ios.constraint.shared.graphics

typealias RegionN = org.jetbrains.skia.Region

/*class RegionN {
    var mStorage = FatVector<Rect>()
    var op_nand = region_operator.op_nand
    var op_and = region_operator.op_and
    var op_or = region_operator.op_or
    var op_xor = region_operator.op_xor

    constructor() {
        mStorage.push_back(Rect(0, 0))
    }

    constructor(regionN: RegionN) {
        mStorage.clear()
        mStorage.addAll(regionN.mStorage)
    }

    constructor(rect: Rect) {
        mStorage.push_back(rect)
    }

    companion object {
        const val direction_LTR = 0
        const val direction_RTL = 1

        fun reverseRectsResolvingJunctions(
            begin: kVectorPointer<Rect>,
            end: kVectorPointer<Rect>,
            dst: FatVector<Rect>,
            spanDirection: Int
        ) {
            dst.clear()
            var current = end - 1
            if(current == null)
                return
            var lastTop = current.v.top

            // add first span immediately
            do {
                dst.add(current)
                current--
            } while (current.v.top >= 0 && current >= begin)

            var beginLastSpan = -1
            var endLastSpan = -1
            var top = -1
            var bottom = -1

// for all other spans, split if a t-junction exists in the span directly above
            while (current >= begin) {
                if (current.v.top != (current + 1)!!.v.top) {
                    // new span
                    if ((spanDirection == direction_RTL && current.v.bottom != (current + 1)!!.v.top) ||
                    (spanDirection == direction_LTR && current.v.top != (current + 1)!!.v.bottom)) {
                        // previous span not directly adjacent, don't check for T junctions
                        beginLastSpan = Int.MAX_VALUE
                    } else {
                        beginLastSpan = endLastSpan + 1
                    }
                    endLastSpan = dst.size - 1
                    top = current.v.top
                    bottom = current.v.bottom
                }
                var left = current.v.left
                var right = current.v.right

                for (prevIndex in beginLastSpan..endLastSpan) {
                    // prevIndex can't be -1 here because if endLastSpan is set to a
                    // value greater than -1 (allowing the loop to execute),
                    // beginLastSpan (and therefore prevIndex) will also be increased
                    val prev = dst[prevIndex]
                    if (spanDirection == direction_RTL) {
                        // iterating over previous span RTL, quit if it's too far left
                        if (prev.v.right <= left) break
                        if (prev.v.right > left && prev.v.right < right) {
                            dst.push_back(Rect(prev.v.right, top, right, bottom))
                            right = prev.v.right
                        }
                        if (prev.v.left > left && prev.v.left < right) {
                            dst.push_back(Rect(prev.v.left, top, right, bottom))
                            right = prev.v.left
                        }
                        // if an entry in the previous span is too far right, nothing further left in the
                        // current span will need it
                        if (prev.v.left >= right) {
                            beginLastSpan = prevIndex
                        }
                    } else {
                        // iterating over previous span LTR, quit if it's too far right
                        if (prev.v.left >= right) break
                        if (prev.v.left > left && prev.v.left < right) {
                            dst.push_back(Rect(left, top, prev.v.left, bottom))
                            left = prev.v.left
                        }
                        if (prev.v.right > left && prev.v.right < right) {
                            dst.push_back(Rect(left, top, prev.v.right, bottom))
                            left = prev.v.right
                        }
                        // if an entry in the previous span is too far left, nothing further right in the
                        // current span will need it
                        if (prev.v.right <= left) {
                            beginLastSpan = prevIndex
                        }
                    }
                }

                if (left < right) {
                    dst.push_back(Rect(left, top, right, bottom))
                }

                current--
            }
        }
    }

    fun createTJunctionFreeRegion(r: RegionN): RegionN
    {
        if (r.isEmpty())
            return r
        if (r.isRect())
            return r
        val reversed = FatVector<Rect>()
        reverseRectsResolvingJunctions(r.begin(), r.end(), reversed, direction_RTL)
        val outputRegion = RegionN()
        reverseRectsResolvingJunctions(reversed.data(), reversed.data() + reversed.size, outputRegion.mStorage, direction_LTR)
        outputRegion.mStorage.push_back(r.getBounds()) // to make region valid, mStorage must end with bounds
        return outputRegion
    }

    fun makeBoundsSelf(): RegionN {
        if (mStorage.size >= 2) {
            val bounds = getBounds()
            mStorage.clear()
            mStorage.push_back(bounds)
        }
        return this
    }

    fun contains(point: Point): Boolean {
        return contains(point.x, point.y)
    }

    fun contains(x: Int, y: Int): Boolean {
        var cur = begin()
        var tail = end()
        while (cur != tail) {
            if (y >= cur.v.top && y < cur.v.bottom && x >= cur.v.left && x < cur.v.right) {
                return true
            }
            cur++
        }
        return false
    }

    fun clear() {
        mStorage.clear()
        mStorage.push_back(Rect(0, 0))
    }

    fun set(r: Rect) {
        mStorage.clear()
        mStorage.push_back(r)
    }

    fun set(w: Number, h: Number) {
        mStorage.clear()
        mStorage.push_back(Rect(w.toInt(), h.toInt()))
    }

    fun set(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        set(Rect(left, top, right, bottom))
        return true
    }

    fun isTriviallyEqual(region: RegionN): Boolean {
        return begin() == region.begin()
    }

    fun hasSameRects(other: RegionN): Boolean {
        val thisRectCount = Pointer(0)
        val thisRects = getArray(thisRectCount)
        val otherRectCount = Pointer(0)
        val otherRects = other.getArray(otherRectCount)
        if (thisRectCount != otherRectCount) return false
        for (i in 0 until thisRectCount.v) {
            if (thisRects[i] != otherRects[i]) return false
        }
        return true
    }

    fun addRectUnchecked(l: Int, t: Int, r: Int, b: Int)
    {
        val rect = Rect(l,t,r,b)
        mStorage.push_back(rect)
    }

    fun orSelf(r: kVectorPointer<Rect>): RegionN {
        if (isEmpty()) {
            set(r.v)
            return this
        }
        return operationSelf(r, op_or)
    }

    fun xorSelf(r: kVectorPointer<Rect>): RegionN {
        return operationSelf(r, op_xor)
    }

    fun andSelf(r: kVectorPointer<Rect>): RegionN {
        return operationSelf(r, op_and)
    }

    fun subtractSelf(r: kVectorPointer<Rect>): RegionN {
        return operationSelf(r, op_nand)
    }

    fun operationSelf(r: kVectorPointer<Rect>, op: UInt): RegionN {
        val lhs = RegionN(this)
        boolean_operation(op, this, lhs, r)
        return this
    }

    fun orSelf(rhs: RegionN): RegionN {
        if (isEmpty()) {
            return rhs
        }
        return operationSelf(rhs, op_or)
    }

    fun xorSelf(rhs: RegionN): RegionN {
        return operationSelf(rhs, op_xor)
    }

    fun andSelf(rhs: RegionN): RegionN {
        return operationSelf(rhs, op_and)
    }

    fun subtractSelf(rhs: RegionN): RegionN {
        return operationSelf(rhs, op_nand)
    }

    fun operationSelf(rhs: RegionN, op: UInt): RegionN {
        val lhs = RegionN(this)
        boolean_operation(op, this, lhs, rhs)
        return this
    }

    fun translateSelf(x: Int, y: Int): RegionN {
        if ((x or y) > 0) translate(this, x, y)
        return this
    }

    fun scaleSelf(sx: Float, sy: Float): RegionN {
        mStorage.forEach { rects ->
            rects.v.left = (rects.v.left * sx + 0.5f).toInt()
            rects.v.right = (rects.v.right * sx + 0.5f).toInt()
            rects.v.top = (rects.v.top * sy + 0.5f).toInt()
            rects.v.bottom = (rects.v.bottom * sy + 0.5f).toInt()
        }
        return this
    }

    fun merge(rhs: kVectorPointer<Rect>): RegionN {
        return operation(rhs, op_or)
    }

    fun mergeExclusive(rhs: kVectorPointer<Rect>): RegionN {
        return operation(rhs, op_xor)
    }

    fun intersect(rhs: kVectorPointer<Rect>): RegionN {
        return operation(rhs, op_and)
    }

    fun subtract(rhs: kVectorPointer<Rect>): RegionN {
        return operation(rhs, op_nand)
    }

    fun operation(rhs: kVectorPointer<Rect>, op: UInt): RegionN {
        val result = RegionN()
        boolean_operation(op, result, this, rhs)
        return result
    }

    fun merge(rhs: RegionN): RegionN {
        return operation(rhs, op_or)
    }

    fun mergeExclusive(rhs: RegionN): RegionN {
        return operation(rhs, op_xor)
    }

    fun intersect(rhs: RegionN): RegionN {
        return operation(rhs, op_and)
    }

    fun subtract(rhs: RegionN): RegionN {
        return operation(rhs, op_nand)
    }

    fun operation(rhs: RegionN, op: UInt): RegionN {
        val result = RegionN()
        boolean_operation(op, result, this, rhs)
        return result
    }

    fun translate(x: Int, y: Int): RegionN {
        val result = RegionN()
        val resultP = Pointer(result)
        translate(resultP, this, x, y)
        return result
    }

    fun orSelf(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operationSelf(rhs, dx, dy, op_or)
    }

    fun xorSelf(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operationSelf(rhs, dx, dy, op_xor)
    }

    fun andSelf(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operationSelf(rhs, dx, dy, op_and)
    }

    fun subtractSelf(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operationSelf(rhs, dx, dy, op_nand)
    }

    fun operationSelf(rhs: RegionN, dx: Int, dy: Int, op: UInt): RegionN {
        val lhs = RegionN(this)
        boolean_operation(op, this, lhs, rhs, dx, dy)
        return this
    }

    fun merge(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operation(rhs, dx, dy, op_or)
    }

    fun mergeExclusive(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operation(rhs, dx, dy, op_xor)
    }

    fun intersect(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operation(rhs, dx, dy, op_and)
    }

    fun subtract(rhs: RegionN, dx: Int, dy: Int): RegionN {
        return operation(rhs, dx, dy, op_nand)
    }

    fun operation(rhs: RegionN, dx: Int, dy: Int, op: UInt): RegionN {
        val result = RegionN()
        boolean_operation(op, result, this, rhs, dx, dy)
        return result
    }

    inner class rasterizer(reg: RegionN) : region_operator.region_rasterizer() {
        private val bounds = Rect(Int.MAX_VALUE, 0, Int.MIN_VALUE, 0)
        private val storage: FatVector<Rect> = reg.mStorage
        private var head: kVectorPointer<Rect> = kVectorPointer(Rect())
        private var tail: kVectorPointer<Rect> = kVectorPointer(Rect())
        private val span: FatVector<Rect> = FatVector()
        private var cur: kVectorPointer<Rect> = kVectorPointer(Rect())

        init {
            storage.clear()
        }

        override fun call(rect: Rect) {
            if (span.size > 0) {
                if (cur.v.top != rect.top) {
                    flushSpan()
                } else if (cur.v.right == rect.left) {
                    cur.v.right = rect.right
                    return
                }
            }
            span.push_back(rect)
            cur = span.data() + (span.size - 1)
        }

        private fun <T: Comparable<T>> min(rhs: T, lhs: T): T {
            return if (rhs < lhs) rhs else lhs
        }

        private fun <T: Comparable<T>> max(rhs: T, lhs: T): T {
            return if (rhs > lhs) rhs else lhs
        }

        fun flushSpan() {
            var merge = false
            if ((tail.virtualAddress - head.virtualAddress) == span.size) {
                var Pointer = span.data()
                var q = head
                if (Pointer.v.top == q.v.bottom) {
                    merge = true
                    while (q != tail) {
                        if ((Pointer.v.left != q.v.left) || (Pointer.v.right != q.v.right)) {
                            merge = false
                            break
                        }
                        Pointer++
                        q++
                    }
                }
            }
            if (merge) {
                val bottom = span.front().bottom
                var r = head
                while (r != tail) {
                    r.v.bottom = bottom
                    r++
                }
            } else {
                bounds.left = min(span.front().left, bounds.left)
                bounds.right = max(span.back().right, bounds.right)
                storage.insert(storage.end(), span.begin(), span.end())
                tail = storage.data() + storage.size
                head = tail - span.size
            }
            span.clear()
        }

    }

    fun boolean_operation(
        op: UInt,
        dst: RegionN,
        lhs: RegionN,
        rhs: RegionN,
        dx: Int,
        dy: Int
    ) {
        var lhsCount = Pointer(0)
        val lhsRects = lhs.getArray(lhsCount)
        var rhsCount = Pointer(0)
        val rhsRects = rhs.getArray(rhsCount)
        val lhsRegion = region_operator.region(lhsRects, lhsCount.v)
        val rhsRegion = region_operator.region(rhsRects, rhsCount.v, dx, dy)
        val operation = region_operator(op, lhsRegion, rhsRegion)

        val r = rasterizer(dst)
        operation.call(r)
    }

    fun boolean_operation(op: UInt, dst: RegionN, lhs: RegionN, rhs: kVectorPointer<Rect>, dx: Int, dy: Int) {
        if (!rhs.v.isValid() && rhs.v != Rect.INVALID_RECT) {
            Log.e("RegionN",
                "RegionN::boolean_operation(op=$op) invalid Rect={${rhs.v.left},${rhs.v.top},${rhs.v.right},${rhs.v.bottom}}")
            return
        }
        val lhsCount = Pointer(0)
        val lhsRects = lhs.getArray(lhsCount)
        val lhsRegion = region_operator.region(lhsRects, lhsCount.v)
        val rhsRegion = region_operator.region(rhs, 1, dx, dy)
        val operation = region_operator(op, lhsRegion, rhsRegion)
        val r = rasterizer(dst)
        operation.call(r)
    }

    fun boolean_operation(op: UInt, dst: RegionN, lhs: RegionN, rhs: RegionN) { boolean_operation(op, dst, lhs, rhs, 0, 0) }

    fun boolean_operation(op: UInt, dst: RegionN, lhs: RegionN, rhs: kVectorPointer<Rect>) { boolean_operation(op, dst, lhs, rhs, 0, 0) }

    fun translate(reg: RegionN, dx: Int, dy: Int) {
        if ((dx != 0 || dy != 0) && !reg.isEmpty())
        {
            val count = reg.mStorage.size
            val rects = reg.mStorage.data()
            var i = 0
            while (i < count) {
                rects[i].v.offsetBy(dx, dy)
                i++
            }
        }
    }

    fun translate(dst: Pointer<RegionN>, reg: RegionN, dx: Int, dy: Int) {
        dst.v = reg
        translate(dst.v, dx, dy)
    }

    fun begin(): kVectorPointer<Rect> {
        return mStorage.first()
    }

    fun end(): kVectorPointer<Rect> {
        return mStorage.last()
    }

    fun getArray(count: Pointer<Int>): kVectorPointer<Rect> {
        count.v = mStorage.size
        return begin()
    }

    fun isEmpty(): Boolean {
        return getBounds().isEmpty()
    }

    fun isRect(): Boolean {
        return mStorage.size == 1
    }

    fun getBounds(): Rect {
        return mStorage.back()
    }

    fun bounds(): Rect {
        return getBounds()
    }
}*/