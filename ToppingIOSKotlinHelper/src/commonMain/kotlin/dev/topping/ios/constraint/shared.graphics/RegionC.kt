package dev.topping.ios.constraint.shared.graphics

import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.Pointer

class region_operator {
    companion object {
        const val LHS = 0x5U
        const val RHS = 0x6U
        const val max_value = 0x7FFFFFF

        val op_nand = LHS and RHS.inv()
        val op_and = LHS and RHS
        val op_or = LHS or RHS
        val op_xor = LHS xor RHS
    }

    var spanner: Spanner
    var op_mask: UInt

    constructor(op: UInt, lhs: region, rhs: region) {
        op_mask = op
        spanner = Spanner(lhs, rhs)
    }

    fun call(rasterizer: region_rasterizer) {
        var current = Rect()
        do {
            val spannerInner = SpannerInner(spanner.lhs, spanner.rhs)
            val top = Pointer(current.top)
            val bottom = Pointer(current.bottom)
            val inside = spanner.next(top, bottom)
            current.top = top.v
            current.bottom = bottom.v
            spannerInner.prepare(inside)
            do {
                val left = Pointer(current.left)
                val right = Pointer(current.right)
                val inside = spannerInner.next(left, right)
                current.left = left.v
                current.right = right.v
                if (((op_mask shr inside) and 1U) > 0U) {
                    if (current.left < current.right && current.top < current.bottom) {
                        rasterizer.call(current)
                    }
                }
            } while (!spannerInner.isDone())
        } while (!spanner.isDone())
    }

    class region {
        var rects: kVectorPointer<Rect> = kVectorPointer<Rect>(Rect())
        var count: Int = 0
        var dx: Int = 0
        var dy: Int = 0

        constructor(rhs: region) {
            rects = rhs.rects
            count = rhs.count
            dx = rhs.dx
            dy = rhs.dy
        }

        constructor(r: kVectorPointer<Rect>, c: Int) {
            rects = r
            count = c
            dx = 0
            dy = 0
        }

        constructor(r: kVectorPointer<Rect>, c: Int, dx: Int, dy: Int) {
            rects = r
            count = c
            this.dx = dx
            this.dy = dy
        }
    }

    abstract class region_rasterizer {
        abstract fun call(rect: Rect)
    }

    open class SpannerBase {
        protected var lhs_head: Int = max_value
        protected var lhs_tail: Int = max_value
        protected var rhs_head: Int = max_value
        protected var rhs_tail: Int = max_value

        companion object {
            const val lhs_before_rhs = 0
            const val lhs_after_rhs = 1
            const val lhs_coincide_rhs = 2
        }

        protected fun next(head: Pointer<Int>, tail: Pointer<Int>, more_lhs: Pointer<Boolean>, more_rhs: Pointer<Boolean>): Int {
            var inside: Int

            more_lhs.v = false
            more_rhs.v = false
            if (lhs_head < rhs_head) {
                inside = lhs_before_rhs
                head.v = lhs_head
                if (lhs_tail <= rhs_head) {
                    tail.v = lhs_tail
                    more_lhs.v = true
                } else {
                    lhs_head = rhs_head
                    tail.v = rhs_head
                }
            } else if (rhs_head < lhs_head) {
                inside = lhs_after_rhs
                head.v = rhs_head
                if (rhs_tail <= lhs_head) {
                    tail.v = rhs_tail
                    more_rhs.v = true
                } else {
                    rhs_head = lhs_head
                    tail.v = lhs_head
                }
            } else {
                inside = lhs_coincide_rhs
                head.v = lhs_head
                if (lhs_tail <= rhs_tail) {
                    rhs_head = lhs_tail
                    tail.v = rhs_head
                    more_lhs.v = true
                }
                if (rhs_tail <= lhs_tail) {
                    lhs_head = rhs_tail
                    tail.v = lhs_head
                    more_rhs.v = true
                }
            }
            return inside
        }
    }

    class Spanner(lhs: region, rhs: region) : SpannerBase() {

        val lhs: region = lhs
        val rhs: region = rhs

        init {
            if (lhs.count != 0) {
                lhs_head = lhs.rects.v.top + lhs.dy
                lhs_tail = lhs.rects.v.bottom + lhs.dy
            }
            if (rhs.count != 0) {
                rhs_head = rhs.rects.v.top + rhs.dy
                rhs_tail = rhs.rects.v.bottom + rhs.dy
            }
        }

        fun isDone(): Boolean {
            return rhs.count == 0 && lhs.count == 0
        }

        fun next(top: Pointer<Int>, bottom: Pointer<Int>): Int {
            val moreLhs = Pointer(false)
            val moreRhs = Pointer(false)
            val inside = super.next(top, bottom, moreLhs, moreRhs)
            if (moreLhs.v) {
                val pLhs_head = Pointer(lhs_head)
                val pLhs_tail = Pointer(lhs_tail)
                advance(lhs, pLhs_head, pLhs_tail)
                lhs_head = pLhs_head.v
                lhs_head = pLhs_head.v
            }
            if (moreRhs.v) {
                val pRhs_head = Pointer(rhs_head)
                val pRhs_tail = Pointer(rhs_tail)
                advance(rhs, pRhs_head, pRhs_tail)
                rhs_head = pRhs_head.v
                rhs_tail = pRhs_tail.v
            }
            return inside
        }

        companion object {
            private fun advance(reg: region, aTop: Pointer<Int>, aBottom: Pointer<Int>) {
                // got to next span
                var count = reg.count
                var rects = reg.rects
                val end = rects + count
                val top = rects.v.top
                while (rects != end && rects.v.top == top) {
                    rects++
                    count--
                }
                if (rects != end) {
                    aTop.v = rects.v.top + reg.dy
                    aBottom.v = rects.v.bottom + reg.dy
                } else {
                    aTop.v = Int.MAX_VALUE
                    aBottom.v = Int.MAX_VALUE
                }
                reg.rects = rects
                reg.count = count
            }
        }
    }

    class SpannerInner : SpannerBase {
        private lateinit var lhs: region
        private lateinit var rhs: region

        constructor(lhs: region, rhs: region) {
            this.lhs = lhs
            this.rhs = rhs
        }

        fun prepare(inside: Int) {
            if (inside == SpannerBase.lhs_before_rhs) {
                if (lhs.count != 0) {
                    lhs_head = lhs.rects.v.left + lhs.dx
                    lhs_tail = lhs.rects.v.right + lhs.dx
                }
                rhs_head = max_value
                rhs_tail = max_value
            } else if (inside == SpannerBase.lhs_after_rhs) {
                lhs_head = max_value
                lhs_tail = max_value
                if (rhs.count != 0) {
                    rhs_head = rhs.rects.v.left + rhs.dx
                    rhs_tail = rhs.rects.v.right + rhs.dx
                }
            } else {
                if (lhs.count != 0) {
                    lhs_head = lhs.rects.v.left + lhs.dx
                    lhs_tail = lhs.rects.v.right + lhs.dx
                }
                if (rhs.count != 0) {
                    rhs_head = rhs.rects.v.left + rhs.dx
                    rhs_tail = rhs.rects.v.right + rhs.dx
                }
            }
        }

        fun isDone(): Boolean {
            return lhs_head == max_value && rhs_head == max_value
        }

        fun next(left: Pointer<Int>, right: Pointer<Int>): Int {
            var more_lhs = Pointer(false)
            var more_rhs = Pointer(false)
            val inside = super.next(left, right, more_lhs, more_rhs)
            if (more_lhs.v) {
                val pLhs_head = Pointer(lhs_head)
                val pLhs_tail = Pointer(lhs_tail)
                advance(lhs, pLhs_head, pLhs_tail)
                lhs_head = pLhs_head.v
                lhs_tail = pLhs_tail.v
            }
            if (more_rhs.v) {
                val pRhs_head = Pointer(rhs_head)
                val pRhs_tail = Pointer(rhs_tail)
                advance(rhs, pRhs_head, pRhs_tail)
                rhs_head = pRhs_head.v
                rhs_tail = pRhs_tail.v
            }
            return inside
        }

        private fun advance(reg: region, left: Pointer<Int>, right: Pointer<Int>) {
            if (reg.rects != null && reg.count != 0) {
                val cur_span_top = reg.rects.v.top
                reg.rects++
                reg.count--
                if (reg.count == 0 || reg.rects.v.top != cur_span_top) {
                    left.v = max_value
                    right.v = max_value
                } else {
                    left.v = reg.rects.v.left + reg.dx
                    right.v = reg.rects.v.right + reg.dx
                }
            }
        }

    }
}