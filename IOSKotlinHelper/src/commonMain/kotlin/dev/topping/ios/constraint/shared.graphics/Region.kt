package dev.topping.ios.constraint.shared.graphics

import dev.topping.ios.constraint.Pool
import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.Pointer
import org.jetbrains.skia.IRect

class Region {
    /**
     * @hide
     */
    var mNativeRegion: RegionN

    // the native values for these must match up with the enum in SkRegion.h
    enum class Op(
        /**
         * @hide
         */
        val nativeInt: Int,
        val skValue: org.jetbrains.skia.Region.Op
    ) {
        DIFFERENCE(0, org.jetbrains.skia.Region.Op.DIFFERENCE),
        INTERSECT(1, org.jetbrains.skia.Region.Op.INTERSECT),
        UNION(2, org.jetbrains.skia.Region.Op.UNION),
        XOR(3, org.jetbrains.skia.Region.Op.XOR),
        REVERSE_DIFFERENCE(4, org.jetbrains.skia.Region.Op.REVERSE_DIFFERENCE),
        REPLACE(5, org.jetbrains.skia.Region.Op.REPLACE);

        companion object {
            fun getOp(nativeInt: Int): org.jetbrains.skia.Region.Op {
                return Op.values().first {
                    it.nativeInt == nativeInt
                }.skValue
            }
        }
    }

    /** Create an empty region
     */
    constructor() : this(nativeConstructor()) {}

    /** Return a copy of the specified region
     */
    constructor(region: Region) : this(nativeConstructor()) {
        var pNativeRegion = Pointer(mNativeRegion)
        nativeSetRegion(pNativeRegion, region.mNativeRegion)
        mNativeRegion = pNativeRegion.v
    }

    /** Return a region set to the specified rectangle
     */
    constructor(r: Rect) {
        mNativeRegion = nativeConstructor()
        nativeSetRect(mNativeRegion, r.left, r.top, r.right, r.bottom)
    }

    /** Return a region set to the specified rectangle
     */
    constructor(left: Int, top: Int, right: Int, bottom: Int) {
        mNativeRegion = nativeConstructor()
        nativeSetRect(mNativeRegion, left, top, right, bottom)
    }

    /** Set the region to the empty region
     */
    fun setEmpty() {
        nativeSetRect(mNativeRegion, 0, 0, 0, 0)
    }

    /** Set the region to the specified region.
     */
    fun set(region: Region): Boolean {
        nativeSetRegion(Pointer(mNativeRegion), region.mNativeRegion)
        return true
    }

    /** Set the region to the specified rectangle
     */
    fun set(r: Rect): Boolean {
        return nativeSetRect(mNativeRegion, r.left, r.top, r.right, r.bottom)
    }

    /** Set the region to the specified rectangle
     */
    operator fun set(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return nativeSetRect(mNativeRegion, left, top, right, bottom)
    }

    /**
     * Set the region to the area described by the path and clip.
     * Return true if the resulting region is non-empty. This produces a region
     * that is identical to the pixels that would be drawn by the path
     * (with no antialiasing).
     */
    fun setPath(path: Path, clip: Region): Boolean {
        return nativeSetPath(mNativeRegion, path.readOnlyNI(), clip.mNativeRegion)
    }

    /**
     * Return true if this region is empty
     */
    val isEmpty: Boolean
         get() = mNativeRegion.isEmpty

    /**
     * Return true if the region contains a single rectangle
     */
    val isRect: Boolean
        get() = mNativeRegion.isRect

    /**
     * Return true if the region contains more than one rectangle
     */
    val isComplex: Boolean
        get() = mNativeRegion.isComplex

    /**
     * Return a new Rect set to the bounds of the region. If the region is
     * empty, the Rect will be set to [0, 0, 0, 0]
     */
    val bounds: Rect
        get() {
            val r = Pointer(Rect())
            nativeGetBounds(mNativeRegion, r)
            return r.v
        }

    /**
     * Set the Rect to the bounds of the region. If the region is empty, the
     * Rect will be set to [0, 0, 0, 0]
     */
    fun getBounds(r: Pointer<Rect>?): Boolean {
        if (r == null) {
            throw NullPointerException()
        }

        return nativeGetBounds(mNativeRegion, r)
    }

    /**
     * Return the boundary of the region as a new Path. If the region is empty,
     * the path will also be empty.
     */
    val boundaryPath: Path
        get() {
            val path = Path()
            nativeGetBoundaryPath(mNativeRegion, path.mutateNI())
            return path
        }

    /**
     * Set the path to the boundary of the region. If the region is empty, the
     * path will also be empty.
     */
    fun getBoundaryPath(path: Path): Boolean {
        return nativeGetBoundaryPath(mNativeRegion, path.mutateNI())
    }

    /**
     * Return true if the region contains the specified point
     */
    fun contains(x: Int, y: Int): Boolean {
        return mNativeRegion.contains(x, y)
    }

    /**
     * Return true if the region is a single rectangle (not complex) and it
     * contains the specified rectangle. Returning false is not a guarantee
     * that the rectangle is not contained by this region, but return true is a
     * guarantee that the rectangle is contained by this region.
     */
    fun quickContains(r: Rect): Boolean {
        return quickContains(r.left, r.top, r.right, r.bottom)
    }

    /**
     * Return true if the region is a single rectangle (not complex) and it
     * contains the specified rectangle. Returning false is not a guarantee
     * that the rectangle is not contained by this region, but return true is a
     * guarantee that the rectangle is contained by this region.
     */
    fun quickContains(
        left: Int, top: Int, right: Int,
        bottom: Int
    ): Boolean {
        return mNativeRegion.quickContains(IRect.makeLTRB(left, top, right, bottom))
    }

    /**
     * Return true if the region is empty, or if the specified rectangle does
     * not intersect the region. Returning false is not a guarantee that they
     * intersect, but returning true is a guarantee that they do not.
     */
    fun quickReject(r: Rect): Boolean {
        return quickReject(r.left, r.top, r.right, r.bottom)
    }

    /**
     * Return true if the region is empty, or if the specified rectangle does
     * not intersect the region. Returning false is not a guarantee that they
     * intersect, but returning true is a guarantee that they do not.
     */
    fun quickReject(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return mNativeRegion.quickReject(IRect.makeLTRB(left, top, right, bottom))
    }

    /**
     * Return true if the region is empty, or if the specified region does not
     * intersect the region. Returning false is not a guarantee that they
     * intersect, but returning true is a guarantee that they do not.
     */
    fun quickReject(rgn: Region?): Boolean {
        return mNativeRegion.quickReject(rgn?.mNativeRegion ?: RegionN())
    }
    /**
     * Set the dst region to the result of translating this region by [dx, dy].
     * If this region is empty, then dst will be set to empty.
     */
    /**
     * Translate the region by [dx, dy]. If the region is empty, do nothing.
     */
    fun translate(dx: Int, dy: Int, dst: Region? = null) {
        mNativeRegion.translate(dx, dy)
    }

    /**
     * Scale the region by the given scale amount. This re-constructs new region by
     * scaling the rects that this region consists of. New rectis are computed by scaling
     * coordinates by float, then rounded by roundf() function to integers. This may results
     * in less internal rects if 0 < scale < 1. Zero and Negative scale result in
     * an empty region. If this region is empty, do nothing.
     *
     * @hide
     */
    fun scale(scale: Float) {
        scale(scale, null)
    }

    /**
     * Set the dst region to the result of scaling this region by the given scale amount.
     * If this region is empty, then dst will be set to empty.
     * @hide
     */
    fun scale(scale: Float, dst: Region?) {
        //TODO:
        /*val rect = mNativeRegion.bounds
            mStorage.forEach { rects ->
                rects.v.left = (rects.v.left * sx + 0.5f).toInt()
                rects.v.right = (rects.v.right * sx + 0.5f).toInt()
                rects.v.top = (rects.v.top * sy + 0.5f).toInt()
                rects.v.bottom = (rects.v.bottom * sy + 0.5f).toInt()
            }
            return this*/
    }
    fun union(r: Rect): Boolean {
        return op(r, Op.UNION)
    }

    /**
     * Perform the specified Op on this region and the specified rect. Return
     * true if the result of the op is not empty.
     */
    fun op(r: Rect, op: Op): Boolean {
        return nativeOp(
            mNativeRegion, r.left, r.top, r.right, r.bottom,
            op.nativeInt
        )
    }

    /**
     * Perform the specified Op on this region and the specified rect. Return
     * true if the result of the op is not empty.
     */
    fun op(left: Int, top: Int, right: Int, bottom: Int, op: Op): Boolean {
        return nativeOp(
            mNativeRegion, left, top, right, bottom,
            op.nativeInt
        )
    }

    /**
     * Perform the specified Op on this region and the specified region. Return
     * true if the result of the op is not empty.
     */
    fun op(region: Region, op: Op): Boolean {
        return op(this, region, op)
    }

    /**
     * Set this region to the result of performing the Op on the specified rect
     * and region. Return true if the result is not empty.
     */
    fun op(rect: Rect, region: Region, op: Op): Boolean {
        return nativeOp(
            mNativeRegion, rect, region.mNativeRegion,
            op.nativeInt
        )
    }

    /**
     * Set this region to the result of performing the Op on the specified
     * regions. Return true if the result is not empty.
     */
    fun op(region1: Region, region2: Region, op: Op): Boolean {
        return nativeOp(
            mNativeRegion, region1.mNativeRegion,
            region2.mNativeRegion, op.nativeInt
        )
    }

    override fun toString(): String {
        return nativeToString(mNativeRegion)
    }

    /**
     * Recycles an instance.
     *
     * @hide
     */
    fun recycle() {
        setEmpty()
        sPool.release(this)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null || obj !is Region) {
            return false
        }
        return nativeEquals(mNativeRegion, obj.mNativeRegion)
    }

    internal constructor(ni: RegionN) {
        mNativeRegion = ni
    }

    fun ni(): RegionN {
        return mNativeRegion
    }

    companion object {
        private const val MAX_POOL_SIZE = 10
        private val sPool: Pool<Region> = Pool<Region>(MAX_POOL_SIZE)

        /**
         * @return An instance from a pool if such or a new one.
         *
         * @hide
         */
        fun obtain(): Region {
            val region: Region? = sPool.acquire()
            return region ?: Region()
        }

        /**
         * @return An instance from a pool if such or a new one.
         *
         * @param other Region to copy values from for initialization.
         *
         * @hide
         */
        fun obtain(other: Region): Region {
            val region = obtain()
            region.set(other)
            return region
        }

        private fun nativeEquals(native_r1: RegionN, native_r2: RegionN): Boolean {
            return native_r1 == native_r2
        }
        private fun nativeConstructor(): RegionN = RegionN()
        private fun nativeSetRegion(native_dst: Pointer<RegionN>, native_src: RegionN) {
            native_dst.v = native_src
        }
        private fun nativeSetRect(
            native_dst: RegionN, left: Int,
            top: Int, right: Int, bottom: Int
        ): Boolean {
            return native_dst.setRect(IRect.makeLTRB(left, top, right, bottom))
        }

        private fun nativeSetPath(
            native_dst: RegionN, native_path: PathN,
            native_clip: RegionN
        ): Boolean {
             return native_dst.setPath(native_path, native_clip)
        }

        private fun nativeGetBounds(native_region: RegionN, rect: Pointer<Rect>): Boolean {
            val iRect = native_region.bounds
            rect.v = iRect.toTRect()
            return true
        }
        private fun nativeGetBoundaryPath(
            native_region: RegionN,
            native_path: PathN
        ): Boolean {
            return native_region.getBoundaryPath(native_path)
        }

        private fun nativeOp(
            native_dst: RegionN, left: Int, top: Int,
            right: Int, bottom: Int, op: Int
        ): Boolean {
            val iRect = IRect.makeLTRB(left, top, right, bottom)
            return native_dst.op(iRect, Op.getOp(op))
        }

        private fun nativeOp(
            native_dst: RegionN, rect: Rect,
            native_region: RegionN, op: Int
        ): Boolean {
            return native_dst.op(rect.toIRect(), native_region, Op.getOp(op))
        }

        private fun nativeOp(
            native_dst: RegionN, native_region1: RegionN,
            native_region2: RegionN, op: Int
        ): Boolean {
            return native_dst.op(native_region1, native_region2, Op.getOp(op))
        }

        private fun nativeToString(native_region: RegionN): String = native_region.toString()
    }
}