package dev.topping.ios.constraint.shared.graphics

import dev.topping.ios.constraint.Pointer
import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.core.motion.utils.RectF
import org.jetbrains.skia.*
import platform.posix.rint
import kotlin.math.sqrt

/**
 * The Path class encapsulates compound (multiple contour) geometric paths
 * consisting of straight line segments, quadratic curves, and cubic curves.
 * It can be drawn with canvas.drawPath(path, paint), either filled or stroked
 * (based on the paint's Style), or it can be used for clipping or to draw
 * text on a path.
 */
class Path {
    /**
     * @hide
     */
    var mNativePath: PathN

    /**
     * @hide
     */

    var isSimplePath = true

    /**
     * @hide
     */

    var rects: Region? = null
    private var mLastDirection: Direction? = null

    /**
     * Create an empty path
     */
    constructor() {
        mNativePath = nInit()
    }

    /**
     * Create a new path, copying the contents from the src path.
     *
     * @param src The path to copy from when initializing the new path
     */
    constructor(src: Path?) {
        var valNative = PathN()
        if (src != null) {
            valNative = src.mNativePath
            isSimplePath = src.isSimplePath
            if (src.rects != null) {
                rects = Region(src.rects!!)
            }
        }
        mNativePath = nInit(valNative)
    }

    /**
     * Clear any lines and curves from the path, making it empty.
     * This does NOT change the fill-type setting.
     */
    fun reset() {
        isSimplePath = true
        mLastDirection = null
        if (rects != null) rects!!.setEmpty()
        // We promised not to change this, so preserve it around the native
        // call, which does now reset fill type.
        var fillType = fillType
        nReset(mNativePath)
        fillType = fillType
    }

    /**
     * Rewinds the path: clears any lines and curves from the path but
     * keeps the internal data structure for faster reuse.
     */
    fun rewind() {
        isSimplePath = true
        mLastDirection = null
        if (rects != null) rects!!.setEmpty()
        nRewind(mNativePath)
    }

    /** Replace the contents of this with the contents of src.
     */
    fun set(src: Path) {
        if (this == src) {
            return
        }
        isSimplePath = src.isSimplePath
        val pNativePath = Pointer(mNativePath)
        nSet(pNativePath, src.mNativePath)
        mNativePath = pNativePath.v
        if (!isSimplePath) {
            return
        }
        if (rects != null && src.rects != null) {
            rects!!.set(src.rects!!)
        } else if (rects != null && src.rects == null) {
            rects!!.setEmpty()
        } else if (src.rects != null) {
            rects = Region(src.rects!!)
        }
    }

    /**
     * The logical operations that can be performed when combining two paths.
     *
     * @see .op
     * @see .op
     */
    enum class Op {
        /**
         * Subtract the second path from the first path.
         */
        DIFFERENCE,

        /**
         * Intersect the two paths.
         */
        INTERSECT,

        /**
         * Union (inclusive-or) the two paths.
         */
        UNION,

        /**
         * Exclusive-or the two paths.
         */
        XOR,

        /**
         * Subtract the first path from the second path.
         */
        REVERSE_DIFFERENCE
    }

    /**
     * Set this path to the result of applying the Op to this path and the specified path.
     * The resulting path will be constructed from non-overlapping contours.
     * The curve order is reduced where possible so that cubics may be turned
     * into quadratics, and quadratics maybe turned into lines.
     *
     * @param path The second operand (for difference, the subtrahend)
     *
     * @return True if operation succeeded, false otherwise and this path remains unmodified.
     *
     * @see Op
     *
     * @see .op
     */
    fun op(path: Path, op: Op): Boolean {
        return op(this, path, op)
    }

    /**
     * Set this path to the result of applying the Op to the two specified paths.
     * The resulting path will be constructed from non-overlapping contours.
     * The curve order is reduced where possible so that cubics may be turned
     * into quadratics, and quadratics maybe turned into lines.
     *
     * @param path1 The first operand (for difference, the minuend)
     * @param path2 The second operand (for difference, the subtrahend)
     *
     * @return True if operation succeeded, false otherwise and this path remains unmodified.
     *
     * @see Op
     *
     * @see .op
     */
    fun op(path1: Path, path2: Path, op: Op): Boolean {
        val pNativePath = Pointer(mNativePath)
        if (nOp(path1.mNativePath, path2.mNativePath, op.ordinal, pNativePath)) {
            mNativePath = pNativePath.v
            isSimplePath = false
            rects = null
            return true
        }
        return false
    }

    /**
     * Returns the path's convexity, as defined by the content of the path.
     *
     *
     * A path is convex if it has a single contour, and only ever curves in a
     * single direction.
     *
     *
     * This function will calculate the convexity of the path from its control
     * points, and cache the result.
     *
     * @return True if the path is convex.
     *
     */
    @get:Deprecated(
        """This method is not reliable. The way convexity is computed may change from
      release to release, and convexity could change based on a matrix as well. This method was
      useful when non-convex Paths were unable to be used in certain contexts, but that is no
      longer the case."""
    )
    val isConvex: Boolean
        get() = nIsConvex(mNativePath)

    /**
     * Enum for the ways a path may be filled.
     */
    enum class FillType(val nativeInt: Int) {
        // these must match the values in SkPath.h
        /**
         * Specifies that "inside" is computed by a non-zero sum of signed
         * edge crossings.
         */
        WINDING(0),

        /**
         * Specifies that "inside" is computed by an odd number of edge
         * crossings.
         */
        EVEN_ODD(1),

        /**
         * Same as [.WINDING], but draws outside of the path, rather than inside.
         */
        INVERSE_WINDING(2),

        /**
         * Same as [.EVEN_ODD], but draws outside of the path, rather than inside.
         */
        INVERSE_EVEN_ODD(3);
    }
    /**
     * Return the path's fill type. This defines how "inside" is
     * computed. The default value is WINDING.
     *
     * @return the path's fill type
     */
    /**
     * Set the path's fill type. This defines how "inside" is computed.
     *
     * @param ft The new fill type for this path
     */
    var fillType: FillType
        get() = sFillTypeArray[nGetFillType(mNativePath)]
        set(ft) {
            nSetFillType(mNativePath, ft.nativeInt)
        }

    /**
     * Returns true if the filltype is one of the INVERSE variants
     *
     * @return true if the filltype is one of the INVERSE variants
     */
    val isInverseFillType: Boolean
        get() {
            val ft = nGetFillType(mNativePath)
            return ft and FillType.INVERSE_WINDING.nativeInt != 0
        }

    /**
     * Toggles the INVERSE state of the filltype
     */
    fun toggleInverseFillType() {
        var ft = nGetFillType(mNativePath)
        ft = ft xor FillType.INVERSE_WINDING.nativeInt
        nSetFillType(mNativePath, ft)
    }

    /**
     * Returns true if the path is empty (contains no lines or curves)
     *
     * @return true if the path is empty (contains no lines or curves)
     */
    val isEmpty: Boolean
        get() = nIsEmpty(mNativePath)

    /**
     * Returns true if the path specifies a rectangle. If so, and if rect is
     * not null, set rect to the bounds of the path. If the path does not
     * specify a rectangle, return false and ignore rect.
     *
     * @param rect If not null, returns the bounds of the path if it specifies
     * a rectangle
     * @return     true if the path specifies a rectangle
     */
    fun isRect(rect: Pointer<RectF>): Boolean {
        return nIsRect(mNativePath, rect)
    }

    /**
     * Compute the bounds of the control points of the path, and write the
     * answer into bounds. If the path contains 0 or 1 points, the bounds is
     * set to (0,0,0,0)
     *
     * @param bounds Returns the computed bounds of the path's control points.
     * @param exact This parameter is no longer used.
     */
    fun computeBounds(bounds: Pointer<Rect>, exact: Boolean) {
        nComputeBounds(mNativePath, bounds)
    }

    /**
     * Hint to the path to prepare for adding more points. This can allow the
     * path to more efficiently allocate its storage.
     *
     * @param extraPtCount The number of extra points that may be added to this
     * path
     */
    fun incReserve(extraPtCount: Int) {
        nIncReserve(mNativePath, extraPtCount)
    }

    /**
     * Set the beginning of the next contour to the point (x,y).
     *
     * @param x The x-coordinate of the start of a new contour
     * @param y The y-coordinate of the start of a new contour
     */
    fun moveTo(x: Float, y: Float) {
        nMoveTo(mNativePath, x, y)
    }

    /**
     * Set the beginning of the next contour relative to the last point on the
     * previous contour. If there is no previous contour, this is treated the
     * same as moveTo().
     *
     * @param dx The amount to add to the x-coordinate of the end of the
     * previous contour, to specify the start of a new contour
     * @param dy The amount to add to the y-coordinate of the end of the
     * previous contour, to specify the start of a new contour
     */
    fun rMoveTo(dx: Float, dy: Float) {
        nRMoveTo(mNativePath, dx, dy)
    }

    /**
     * Add a line from the last point to the specified point (x,y).
     * If no moveTo() call has been made for this contour, the first point is
     * automatically set to (0,0).
     *
     * @param x The x-coordinate of the end of a line
     * @param y The y-coordinate of the end of a line
     */
    fun lineTo(x: Float, y: Float) {
        isSimplePath = false
        nLineTo(mNativePath, x, y)
    }

    /**
     * Same as lineTo, but the coordinates are considered relative to the last
     * point on this contour. If there is no previous point, then a moveTo(0,0)
     * is inserted automatically.
     *
     * @param dx The amount to add to the x-coordinate of the previous point on
     * this contour, to specify a line
     * @param dy The amount to add to the y-coordinate of the previous point on
     * this contour, to specify a line
     */
    fun rLineTo(dx: Float, dy: Float) {
        isSimplePath = false
        nRLineTo(mNativePath, dx, dy)
    }

    /**
     * Add a quadratic bezier from the last point, approaching control point
     * (x1,y1), and ending at (x2,y2). If no moveTo() call has been made for
     * this contour, the first point is automatically set to (0,0).
     *
     * @param x1 The x-coordinate of the control point on a quadratic curve
     * @param y1 The y-coordinate of the control point on a quadratic curve
     * @param x2 The x-coordinate of the end point on a quadratic curve
     * @param y2 The y-coordinate of the end point on a quadratic curve
     */
    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        isSimplePath = false
        nQuadTo(mNativePath, x1, y1, x2, y2)
    }

    /**
     * Same as quadTo, but the coordinates are considered relative to the last
     * point on this contour. If there is no previous point, then a moveTo(0,0)
     * is inserted automatically.
     *
     * @param dx1 The amount to add to the x-coordinate of the last point on
     * this contour, for the control point of a quadratic curve
     * @param dy1 The amount to add to the y-coordinate of the last point on
     * this contour, for the control point of a quadratic curve
     * @param dx2 The amount to add to the x-coordinate of the last point on
     * this contour, for the end point of a quadratic curve
     * @param dy2 The amount to add to the y-coordinate of the last point on
     * this contour, for the end point of a quadratic curve
     */
    fun rQuadTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float) {
        isSimplePath = false
        nRQuadTo(mNativePath, dx1, dy1, dx2, dy2)
    }

    /**
     * Add a cubic bezier from the last point, approaching control points
     * (x1,y1) and (x2,y2), and ending at (x3,y3). If no moveTo() call has been
     * made for this contour, the first point is automatically set to (0,0).
     *
     * @param x1 The x-coordinate of the 1st control point on a cubic curve
     * @param y1 The y-coordinate of the 1st control point on a cubic curve
     * @param x2 The x-coordinate of the 2nd control point on a cubic curve
     * @param y2 The y-coordinate of the 2nd control point on a cubic curve
     * @param x3 The x-coordinate of the end point on a cubic curve
     * @param y3 The y-coordinate of the end point on a cubic curve
     */
    fun cubicTo(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float
    ) {
        isSimplePath = false
        nCubicTo(mNativePath, x1, y1, x2, y2, x3, y3)
    }

    /**
     * Same as cubicTo, but the coordinates are considered relative to the
     * current point on this contour. If there is no previous point, then a
     * moveTo(0,0) is inserted automatically.
     */
    fun rCubicTo(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float
    ) {
        isSimplePath = false
        nRCubicTo(mNativePath, x1, y1, x2, y2, x3, y3)
    }

    /**
     * Append the specified arc to the path as a new contour. If the start of
     * the path is different from the path's current last point, then an
     * automatic lineTo() is added to connect the current contour to the
     * start of the arc. However, if the path is empty, then we call moveTo()
     * with the first point of the arc.
     *
     * @param oval        The bounds of oval defining shape and size of the arc
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngle  Sweep angle (in degrees) measured clockwise, treated
     * mod 360.
     * @param forceMoveTo If true, always begin a new contour with the arc
     */
    fun arcTo(
        oval: RectF, startAngle: Float, sweepAngle: Float,
        forceMoveTo: Boolean
    ) {
        arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo)
    }

    /**
     * Append the specified arc to the path as a new contour. If the start of
     * the path is different from the path's current last point, then an
     * automatic lineTo() is added to connect the current contour to the
     * start of the arc. However, if the path is empty, then we call moveTo()
     * with the first point of the arc.
     *
     * @param oval        The bounds of oval defining shape and size of the arc
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngle  Sweep angle (in degrees) measured clockwise
     */
    fun arcTo(oval: RectF, startAngle: Float, sweepAngle: Float) {
        arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, false)
    }

    /**
     * Append the specified arc to the path as a new contour. If the start of
     * the path is different from the path's current last point, then an
     * automatic lineTo() is added to connect the current contour to the
     * start of the arc. However, if the path is empty, then we call moveTo()
     * with the first point of the arc.
     *
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngle  Sweep angle (in degrees) measured clockwise, treated
     * mod 360.
     * @param forceMoveTo If true, always begin a new contour with the arc
     */
    fun arcTo(
        left: Float, top: Float, right: Float, bottom: Float, startAngle: Float,
        sweepAngle: Float, forceMoveTo: Boolean
    ) {
        isSimplePath = false
        nArcTo(mNativePath, left, top, right, bottom, startAngle, sweepAngle, forceMoveTo)
    }

    /**
     * Close the current contour. If the current point is not equal to the
     * first point of the contour, a line segment is automatically added.
     */
    fun close() {
        isSimplePath = false
        nClose(mNativePath)
    }

    /**
     * Specifies how closed shapes (e.g. rects, ovals) are oriented when they
     * are added to a path.
     */
    enum class Direction  // must match enum in SkPath.h
        (val nativeInt: Int) {
        /** clockwise  */
        CW(0),  // must match enum in SkPath.h

        /** counter-clockwise  */
        CCW(1);
    }

    private fun detectSimplePath(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        dir: Direction
    ) {
        if (mLastDirection == null) {
            mLastDirection = dir
        }
        if (mLastDirection != dir) {
            isSimplePath = false
        } else {
            if (rects == null) rects = Region()
            rects!!.op(left.toInt(), top.toInt(), right.toInt(), bottom.toInt(), Region.Op.UNION)
        }
    }

    /**
     * Add a closed rectangle contour to the path
     *
     * @param rect The rectangle to add as a closed contour to the path
     * @param dir  The direction to wind the rectangle's contour
     */
    fun addRect(rect: RectF, dir: Direction) {
        addRect(rect.left, rect.top, rect.right, rect.bottom, dir)
    }

    /**
     * Add a closed rectangle contour to the path
     *
     * @param left   The left side of a rectangle to add to the path
     * @param top    The top of a rectangle to add to the path
     * @param right  The right side of a rectangle to add to the path
     * @param bottom The bottom of a rectangle to add to the path
     * @param dir    The direction to wind the rectangle's contour
     */
    fun addRect(left: Float, top: Float, right: Float, bottom: Float, dir: Direction) {
        detectSimplePath(left, top, right, bottom, dir)
        nAddRect(mNativePath, left, top, right, bottom, dir.nativeInt)
    }

    /**
     * Add a closed oval contour to the path
     *
     * @param oval The bounds of the oval to add as a closed contour to the path
     * @param dir  The direction to wind the oval's contour
     */
    fun addOval(oval: RectF, dir: Direction) {
        addOval(oval.left, oval.top, oval.right, oval.bottom, dir)
    }

    /**
     * Add a closed oval contour to the path
     *
     * @param dir The direction to wind the oval's contour
     */
    fun addOval(left: Float, top: Float, right: Float, bottom: Float, dir: Direction) {
        isSimplePath = false
        nAddOval(mNativePath, left, top, right, bottom, dir.nativeInt)
    }

    /**
     * Add a closed circle contour to the path
     *
     * @param x   The x-coordinate of the center of a circle to add to the path
     * @param y   The y-coordinate of the center of a circle to add to the path
     * @param radius The radius of a circle to add to the path
     * @param dir    The direction to wind the circle's contour
     */
    fun addCircle(x: Float, y: Float, radius: Float, dir: Direction) {
        isSimplePath = false
        nAddCircle(mNativePath, x, y, radius, dir.nativeInt)
    }

    /**
     * Add the specified arc to the path as a new contour.
     *
     * @param oval The bounds of oval defining the shape and size of the arc
     * @param startAngle Starting angle (in degrees) where the arc begins
     * @param sweepAngle Sweep angle (in degrees) measured clockwise
     */
    fun addArc(oval: RectF, startAngle: Float, sweepAngle: Float) {
        addArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle)
    }

    /**
     * Add the specified arc to the path as a new contour.
     *
     * @param startAngle Starting angle (in degrees) where the arc begins
     * @param sweepAngle Sweep angle (in degrees) measured clockwise
     */
    fun addArc(
        left: Float, top: Float, right: Float, bottom: Float, startAngle: Float,
        sweepAngle: Float
    ) {
        isSimplePath = false
        nAddArc(mNativePath, left, top, right, bottom, startAngle, sweepAngle)
    }

    /**
     * Add a closed round-rectangle contour to the path
     *
     * @param rect The bounds of a round-rectangle to add to the path
     * @param rx   The x-radius of the rounded corners on the round-rectangle
     * @param ry   The y-radius of the rounded corners on the round-rectangle
     * @param dir  The direction to wind the round-rectangle's contour
     */
    fun addRoundRect(
        rect: RectF,
        rx: Float,
        ry: Float,
        dir: Direction
    ) {
        addRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, dir)
    }

    /**
     * Add a closed round-rectangle contour to the path
     *
     * @param rx   The x-radius of the rounded corners on the round-rectangle
     * @param ry   The y-radius of the rounded corners on the round-rectangle
     * @param dir  The direction to wind the round-rectangle's contour
     */
    fun addRoundRect(
        left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float,
        dir: Direction
    ) {
        isSimplePath = false
        nAddRoundRect(mNativePath, left, top, right, bottom, rx, ry, dir.nativeInt)
    }

    /**
     * Add a closed round-rectangle contour to the path. Each corner receives
     * two radius values [X, Y]. The corners are ordered top-left, top-right,
     * bottom-right, bottom-left
     *
     * @param rect The bounds of a round-rectangle to add to the path
     * @param radii Array of 8 values, 4 pairs of [X,Y] radii
     * @param dir  The direction to wind the round-rectangle's contour
     */
    fun addRoundRect(
        rect: RectF,
        radii: FloatArray,
        dir: Direction
    ) {
        addRoundRect(rect.left, rect.top, rect.right, rect.bottom, radii, dir)
    }

    /**
     * Add a closed round-rectangle contour to the path. Each corner receives
     * two radius values [X, Y]. The corners are ordered top-left, top-right,
     * bottom-right, bottom-left
     *
     * @param radii Array of 8 values, 4 pairs of [X,Y] radii
     * @param dir  The direction to wind the round-rectangle's contour
     */
    fun addRoundRect(
        left: Float, top: Float, right: Float, bottom: Float,
        radii: FloatArray, dir: Direction
    ) {
        if (radii.size < 8) {
            throw ArrayIndexOutOfBoundsException("radii[] needs 8 values")
        }
        isSimplePath = false
        nAddRoundRect(mNativePath, left, top, right, bottom, radii, dir.nativeInt)
    }

    /**
     * Add a copy of src to the path, offset by (dx,dy)
     *
     * @param src The path to add as a new contour
     * @param dx  The amount to translate the path in X as it is added
     */
    fun addPath(src: Path, dx: Float, dy: Float) {
        isSimplePath = false
        nAddPath(mNativePath, src.mNativePath, dx, dy)
    }

    /**
     * Add a copy of src to the path
     *
     * @param src The path that is appended to the current path
     */
    fun addPath(src: Path) {
        isSimplePath = false
        nAddPath(mNativePath, src.mNativePath)
    }

    /**
     * Add a copy of src to the path, transformed by matrix
     *
     * @param src The path to add as a new contour
     */
    fun addPath(src: Path, matrix: Matrix33) {
        if (!src.isSimplePath) isSimplePath = false
        nAddPath(mNativePath, src.mNativePath, matrix)
    }

    /**
     * Offset the path by (dx,dy)
     *
     * @param dx  The amount in the X direction to offset the entire path
     * @param dy  The amount in the Y direction to offset the entire path
     * @param dst The translated path is written here. If this is null, then
     * the original path is modified.
     */
    fun offset(dx: Float, dy: Float, dst: Path?) {
        var dst = dst
        if (dst != null) {
            dst.set(this)
        } else {
            dst = this
        }
        dst.offset(dx, dy)
    }

    /**
     * Offset the path by (dx,dy)
     *
     * @param dx The amount in the X direction to offset the entire path
     * @param dy The amount in the Y direction to offset the entire path
     */
    fun offset(dx: Float, dy: Float) {
        if (isSimplePath && rects == null) {
            // nothing to offset
            return
        }
        if (isSimplePath && dx.toDouble() == rint(dx.toDouble()) && dy.toDouble() == rint(
                dy.toDouble()
            )
        ) {
            rects!!.translate(dx.toInt(), dy.toInt())
        } else {
            isSimplePath = false
        }
        nOffset(mNativePath, dx, dy)
    }

    /**
     * Sets the last point of the path.
     *
     * @param dx The new X coordinate for the last point
     * @param dy The new Y coordinate for the last point
     */
    fun setLastPoint(dx: Float, dy: Float) {
        isSimplePath = false
        nSetLastPoint(mNativePath, dx, dy)
    }

    /**
     * Transform the points in this path by matrix, and write the answer
     * into dst. If dst is null, then the the original path is modified.
     *
     * @param matrix The matrix to apply to the path
     * @param dst    The transformed path is written here. If dst is null,
     * then the the original path is modified
     */
    fun transform(matrix: Matrix33, dst: Path?) {
        var dstNative = PathN()
        if (dst != null) {
            dst.isSimplePath = false
            dstNative = dst.mNativePath
        }
        nTransform(mNativePath, matrix, dstNative)
    }

    /**
     * Transform the points in this path by matrix.
     *
     * @param matrix The matrix to apply to the path
     */
    fun transform(matrix: Matrix33) {
        isSimplePath = false
        nTransform(mNativePath, matrix)
    }

    /** @hide
     */
    fun readOnlyNI(): PathN {
        return mNativePath
    }

    fun mutateNI(): PathN {
        isSimplePath = false
        return mNativePath
    }

    /**
     * Approximate the `Path` with a series of line segments.
     * This returns float[] with the array containing point components.
     * There are three components for each point, in order:
     *
     *  * Fraction along the length of the path that the point resides
     *  * The x coordinate of the point
     *  * The y coordinate of the point
     *
     *
     * Two points may share the same fraction along its length when there is
     * a move action within the Path.
     *
     * @param acceptableError The acceptable error for a line on the
     * Path. Typically this would be 0.5 so that
     * the error is less than half a pixel.
     * @return An array of components for points approximating the Path.
     */
    fun approximate(acceptableError: Float): FloatArray {
        if (acceptableError < 0) {
            throw IllegalArgumentException("AcceptableError must be greater than or equal to 0")
        }
        return nApproximate(mNativePath, acceptableError)
    }

    companion object {
        // these must be in the same order as their native values
        val sFillTypeArray = arrayOf(
            FillType.WINDING,
            FillType.EVEN_ODD,
            FillType.INVERSE_WINDING,
            FillType.INVERSE_EVEN_ODD
        )

        // ------------------ Regular JNI ------------------------
        private fun nInit(): PathN = PathN.makeFromBytes(ByteArray(0))
        private fun nInit(nPath: PathN): PathN = nPath
        private fun nSet(native_dst: Pointer<PathN>, nSrc: PathN) {
            native_dst.v = nSrc
        }
        private fun nComputeBounds(nPath: PathN, bounds: Pointer<Rect>) {
            bounds.v = nPath.bounds.toTRect()
        }
        private fun nIncReserve(nPath: PathN, extraPtCount: Int) {
            nPath.incReserve(extraPtCount)
        }
        private fun nMoveTo(nPath: PathN, x: Float, y: Float) {
            nPath.moveTo(x, y)
        }
        private fun nRMoveTo(nPath: PathN, dx: Float, dy: Float) {
            nPath.rMoveTo(dx, dy)
        }
        private fun nLineTo(nPath: PathN, x: Float, y: Float) {
            nPath.lineTo(x, y)
        }
        private fun nRLineTo(nPath: PathN, dx: Float, dy: Float) {
            nPath.rLineTo(dx, dy)
        }
        private fun nQuadTo(nPath: PathN, x1: Float, y1: Float, x2: Float, y2: Float) {
            nPath.quadTo(x1, y1, x2, y2)
        }
        private fun nRQuadTo(nPath: PathN, dx1: Float, dy1: Float, dx2: Float, dy2: Float) {
            nPath.rQuadTo(dx1, dy1, dx2, dy2)
        }
        private fun nCubicTo(
            nPath: PathN, x1: Float, y1: Float, x2: Float, y2: Float,
            x3: Float, y3: Float
        ) {
            nPath.cubicTo(x1, y1, x2, y2, x3, y3)
        }

        private fun nRCubicTo(
            nPath: PathN, x1: Float, y1: Float, x2: Float, y2: Float,
            x3: Float, y3: Float
        ) {
            nPath.rCubicTo(x1, y1, x2, y2, x3, y3)
        }

        private fun nArcTo(
            nPath: PathN, left: Float, top: Float, right: Float, bottom: Float,
            startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean
        ) {
            nPath.arcTo(org.jetbrains.skia.Rect(left, top, right, bottom), startAngle, sweepAngle, forceMoveTo)
        }

        private fun nClose(nPath: PathN) {
            nPath.close()
        }
        private fun nAddRect(
            nPath: PathN, left: Float, top: Float,
            right: Float, bottom: Float, dir: Int
        ) {
            nPath.addRect(org.jetbrains.skia.Rect(left, top, right, bottom), PathDirection.values()[dir])
        }

        private fun nAddOval(
            nPath: PathN, left: Float, top: Float,
            right: Float, bottom: Float, dir: Int
        ) {
            nPath.addOval(org.jetbrains.skia.Rect(left, top, right, bottom), PathDirection.values()[dir])
        }

        private fun nAddCircle(nPath: PathN, x: Float, y: Float, radius: Float, dir: Int) {
            nPath.addCircle(x, y, radius, PathDirection.values()[dir])
        }
        private fun nAddArc(
            nPath: PathN, left: Float, top: Float, right: Float, bottom: Float,
            startAngle: Float, sweepAngle: Float
        ) {
            nPath.addArc(org.jetbrains.skia.Rect(left, top, right, bottom), startAngle, sweepAngle)
        }

        private fun nAddRoundRect(
            nPath: PathN, left: Float, top: Float,
            right: Float, bottom: Float, rx: Float, ry: Float, dir: Int
        ) {
            nPath.addRRect(org.jetbrains.skia.RRect.makeLTRB(left, top, right, bottom, rx, ry), PathDirection.values()[dir])
        }

        private fun nAddRoundRect(
            nPath: PathN, left: Float, top: Float,
            right: Float, bottom: Float, radii: FloatArray, dir: Int
        ) {
            nPath.addRRect(RRect.Companion.makeComplexLTRB(left, top, right, bottom, radii), PathDirection.values()[dir])
        }

        private fun nAddPath(nPath: PathN, src: PathN, dx: Float, dy: Float) {
            nPath.addPath(src, dx, dy)
        }
        private fun nAddPath(nPath: PathN, src: PathN) {
            nPath.addPath(src)
        }
        private fun nAddPath(nPath: PathN, src: PathN, matrix: Matrix33) {
            nPath.addPath(src, matrix)
        }
        private fun nOffset(nPath: PathN, dx: Float, dy: Float) {
            nPath.offset(dx, dy)
        }

        private fun nOffset(nPath: PathN, dx: Float, dy: Float, dst: PathN) {
            nPath.offset(dx, dy, dst)
        }
        private fun nSetLastPoint(nPath: PathN, dx: Float, dy: Float) {
            nPath.setLastPt(dx, dy)
        }
        private fun nTransform(nPath: PathN, matrix: Matrix33, dst_path: PathN) {
            nPath.transform(matrix, dst_path)
        }
        private fun nTransform(nPath: PathN, matrix: Matrix33) {
            nPath.transform(matrix)
        }
        private fun nOp(path1: PathN, path2: PathN, op: Int, result: Pointer<PathN>): Boolean {
            result.v = PathN.makeCombining(path1, path2, PathOp.values()[op])!!
            return true
        }

        fun addMove(segmentPoints: FatVector<Point>, lengths: FatVector<Float>, point: Point) {
            var length = 0f
            if (lengths.isNotEmpty()) {
                length = lengths.back()
            }
            segmentPoints.push_back(point)
            lengths.push_back(length)
        }

        fun calculateDistanceBetweenPoints(p1: Point, p2: Point) : Double {
            return sqrt((p2.y - p1.y) * (p2.y - p1.y) + (p2.x - p1.x) * (p2.x - p1.x)).toDouble()
        }

        fun addLine(segmentPoints: FatVector<Point>, lengths: FatVector<Float>, toPoint: Point) {
            if (segmentPoints.isEmpty()) {
                segmentPoints.push_back(Point(0f, 0f))
                lengths.push_back(0f)
            } else if (segmentPoints.back() == toPoint) {
                return // Empty line
            }

            var length = lengths.back() + calculateDistanceBetweenPoints(segmentPoints.back(), toPoint)
            segmentPoints.push_back(toPoint);
            lengths.push_back(length.toFloat())
        }

        fun subdividePoints(points: Array<Point>,
                                t0: Float, p0: Point, t1: Float, p1: Point, midT: Pointer<Float>, midPoint: Pointer<Point>, errorSquared: Float, bezierFunction: (Float, Array<Point>) -> Point): Boolean {
            midT.v = (t1 + t0) / 2
            var midX = (p1.x + p0.x) / 2
            var midY = (p1.y + p0.y) / 2
            midPoint.v = bezierFunction(midT.v, points)
            var xError = midPoint.v.x - midX
            var yError = midPoint.v.y - midY
            var midErrorSquared = (xError * xError) + (yError * yError)
            return midErrorSquared > errorSquared
        }

        fun addBezier(points: Array<Point>, segmentPoints: FatVector<Point>,
                        lengths: FatVector<Float>, errorSquared: Float, doubleCheckDivision: Pointer<Boolean>, bezierFunction: ((Float, Array<Point>) -> Point)) {
            var tToPoint = mutableMapOf<Float, Point>()
            tToPoint[0f] = bezierFunction(0f, points)
            tToPoint[1f] = bezierFunction(1f, points)
            /*var iter = tToPoint.iterator()
            var next = iter
            next.next()
            while (next.hasNext()) {
                var needsSubdivision = true
                var midPoint = Pointer(Point.ZERO)
                do {
                    var midT = Pointer(0f)
                    iter as MutableMap.MutableEntry<Float, Point>
                    next as MutableMap.MutableEntry<Float, Point>
                    needsSubdivision = subdividePoints(points, iter.key,
                    iter.value, next.key, next.value, midT, midPoint, errorSquared, bezierFunction)
                    if (!needsSubdivision && doubleCheckDivision.v) {

                        var quarterPoint = Pointer(Point.ZERO)
                        var quarterT = Pointer(0f)
                        needsSubdivision = subdividePoints(points, iter.key,
                        iter.value, midT.v, midPoint.v, quarterT, quarterPoint, errorSquared, bezierFunction)
                        if (needsSubdivision) {
                            // Found an inflection point. No need to double-check.
                            doubleCheckDivision.v = false
                        }
                    }
                    if (needsSubdivision) {
                        next = iter.
                        next = tToPoint.insert(iter, PointMap::value_type(midT, midPoint));
                    }
                } while (needsSubdivision)
                iter = next
                next.next()
            }*/
            // Now that each division can use linear interpolation with less than the allowed error
            tToPoint.forEach {
                addLine(segmentPoints, lengths, it.value)
            }
        }

        fun cubicCoordinateCalculation(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
            var oneMinusT = 1 - t
            var oneMinusTSquared = oneMinusT * oneMinusT
            var oneMinusTCubed = oneMinusTSquared * oneMinusT
            var tSquared = t * t
            var tCubed = tSquared * t
            return (oneMinusTCubed * p0) + (3 * oneMinusTSquared * t * p1)
            + (3 * oneMinusT * tSquared * p2) + (tCubed * p3)
        }

        fun cubicBezierCalculation(t: Float, points: Array<Point>): Point {
            var x = cubicCoordinateCalculation(t, points[0].x, points[1].x, points[2].x, points[3].x)
            var y = cubicCoordinateCalculation(t, points[0].y, points[1].y, points[2].y, points[3].y)
            return Point(x, y)
        }

        fun quadraticCoordinateCalculation(t: Float, p0: Float, p1: Float, p2: Float): Float {
            var oneMinusT = 1 - t
            return oneMinusT * ((oneMinusT * p0) + (t * p1)) + t * ((oneMinusT * p1) + (t * p2))
        }

        fun quadraticBezierCalculation(t: Float, points: Array<Point>): Point {
            var x = quadraticCoordinateCalculation(t, points[0].x, points[1].x, points[2].x)
            var y = quadraticCoordinateCalculation(t, points[0].y, points[1].y, points[2].y)
            return Point(x, y)
        }

        fun createVerbSegments(verb: PathVerb, points: Array<Point>,
                                segmentPoints: FatVector<Point>, lengths: FatVector<Float>, errorSquared: Float) {
            when (verb) {
                PathVerb.MOVE -> {
                    addMove(segmentPoints, lengths, points[0])
                }
                PathVerb.CLOSE -> {
                    addLine(segmentPoints, lengths, points[0])
                }
                PathVerb.LINE -> {
                    addLine(segmentPoints, lengths, points[1])
                }
                PathVerb.QUAD -> {
                    addBezier(points, segmentPoints, lengths, errorSquared, Pointer(false)) { fl, points ->
                        val v = quadraticBezierCalculation(fl, points)
                        v
                    }
                }
                PathVerb.CUBIC -> {
                    addBezier(points, segmentPoints, lengths, errorSquared, Pointer(false)) { fl, points ->
                        val v = cubicBezierCalculation(fl, points)
                        v
                    }
                }
                else -> {}
            }
        }

        private fun nApproximate(nPath: PathN, acceptableError: Float): FloatArray {
            var pathIter = nPath.iterator(false)
            var verb: PathVerb
            var points = Array<Point>(4) { Point.ZERO }

            var segmentPoints = FatVector<Point>()
            var lengths = FatVector<Float>()

            var errorSquared = acceptableError * acceptableError

            while (pathIter.hasNext()) {
                var next = pathIter.next()
                createVerbSegments(next!!.verb, points, segmentPoints, lengths, errorSquared)
            }

            if (segmentPoints.isEmpty()) {
                var numVerbs = nPath.verbsCount
                if (numVerbs == 1) {
                    addMove(segmentPoints, lengths, nPath.getPoint(0))
                } else {
                    // Invalid or empty path. Fall back to point(0,0)
                    addMove(segmentPoints, lengths, Point.ZERO);
                }
            }
            var totalLength = lengths.back()
            if (totalLength == 0f) {
                // Lone Move instructions should still be able to animate at the same value.
                segmentPoints.push_back(segmentPoints.back())
                lengths.push_back(1f)
                totalLength = 1f
            }
            var numPoints = segmentPoints.size
            var approximationArraySize = numPoints * 3
            var approximation = FloatArray(approximationArraySize)
            var approximationIndex = 0
            for(i in 0 until numPoints) {
                var point = segmentPoints[i]
                approximation[approximationIndex++] = lengths[i].v / totalLength
                approximation[approximationIndex++] = point.v.x
                approximation[approximationIndex++] = point.v.y
            }

            return approximation
        }

        private fun nIsRect(nPath: PathN, rect: Pointer<RectF>): Boolean {
            val res = nPath.isRect
            val ret = res != null
            if(ret)
                rect.v = res!!.toTRect().toRectF()
            return ret
        }

        private fun nReset(nPath: PathN) {
            nPath.reset()
        }
        private fun nRewind(nPath: PathN) {
            nPath.rewind()
        }
        private fun nIsEmpty(nPath: PathN): Boolean {
            return nPath.isEmpty
        }
        private fun nIsConvex(nPath: PathN): Boolean {
            return nPath.isConvex
        }
        private fun nGetFillType(nPath: PathN): Int {
            return nPath.fillMode.ordinal
        }
        private fun nSetFillType(nPath: PathN, ft: Int) {
            nPath.fillMode = PathFillMode.values()[ft]
        }
    }
}