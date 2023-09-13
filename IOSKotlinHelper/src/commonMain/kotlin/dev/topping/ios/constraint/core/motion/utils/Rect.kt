/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.topping.ios.constraint.core.motion.utils
import kotlin.math.max
import kotlin.math.min

class Point {
    var x = 0
    var y = 0
}

class Rect {
    constructor()

    constructor(w: Int, h: Int) {
        right = w
        bottom = h
    }

    constructor(left: Int, top: Int, right: Int, bottom: Int) {
        set(left, top, right, bottom)
    }

    companion object {
        val INVALID_RECT = Rect(0, 0, -1, -1)
        val EMPTY_RECT = Rect(0, 0, 0, 0)
    }

    var bottom = 0
    var left = 0
    var right = 0
    var top = 0

    fun width(): Int {
        return right - left
    }

    fun height(): Int {
        return bottom - top
    }

    fun centerX(): Int {
        return left + (width().toFloat() / 2f).toInt()
    }

    fun centerY(): Int {
        return top + (height().toFloat() / 2f).toInt()
    }

    fun offset(dx: Int, dy: Int) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    fun offsetTo(x: Int, y: Int)
    {
        right -= left - x
        bottom -= top - y
        left = x
        top = y
    }

    fun offsetBy(x: Int, y: Int)
    {
        left += x
        top  += y
        right += x
        bottom +=y
    }

    fun set(left: Number, top: Number, right: Number, bottom: Number) {
        this.left = left.toInt()
        this.top = top.toInt()
        this.right = right.toInt()
        this.bottom = bottom.toInt()
    }

    fun set(rect: Rect) {
        this.left = rect.left
        this.top = rect.top
        this.right = rect.right
        this.bottom = rect.bottom
    }

    /**
     * Returns true if (x,y) is inside the rectangle. The left and top are
     * considered to be inside, while the right and bottom are not. This means
     * that for a x,y to be contained: left <= x < right and top <= y < bottom.
     * An empty rectangle never contains any point.
     *
     * @param x The X coordinate of the point being tested for containment
     * @param y The Y coordinate of the point being tested for containment
     * @return true iff (x,y) are contained by the rectangle, where containment
     * means left <= x < right and top <= y < bottom
     */
    fun contains(x: Float, y: Float): Boolean {
        return left < right && top < bottom // check for empty first
                && x >= left && x < right && y >= top && y < bottom
    }

    fun contains(x: Int, y: Int): Boolean {
        return left < right && top < bottom // check for empty first
                && x >= left && x < right && y >= top && y < bottom
    }

    fun contains(x: Double, y: Double): Boolean {
        return left < right && top < bottom // check for empty first
                && x >= left && x < right && y >= top && y < bottom
    }

    inline fun isValid(): Boolean {
        return (width() >= 0) && (height() >= 0);
    }
    // an empty rect has a zero width or height, or is invalid
    inline fun isEmpty(): Boolean {
        return (width() <= 0) || (height() <= 0);
    }

    fun setEmpty() {
        bottom = 0
        left = 0
        right = 0
        top = 0
    }

    fun toRectF(): RectF {
        val rect = RectF()
        rect.bottom = this.bottom.toFloat()
        rect.left = this.left.toFloat()
        rect.right = this.right.toFloat()
        rect.top = this.top.toFloat()
        return rect
    }
}

class RectF {
    var bottom = 0f
    var left = 0f
    var right = 0f
    var top = 0f

    fun toRect(): Rect {
        val rect = Rect()
        rect.bottom = this.bottom.toInt()
        rect.left = this.left.toInt()
        rect.right = this.right.toInt()
        rect.top = this.top.toInt()
        return rect
    }

    fun width(): Float {
        return right - left
    }

    fun height(): Float {
        return bottom - top
    }

    fun centerX(): Float {
        return left + (width() / 2f)
    }

    fun centerY(): Float {
        return top + (height() / 2f)
    }

    /**
     * Offset the rectangle by adding dx to its left and right coordinates, and
     * adding dy to its top and bottom coordinates.
     *
     * @param dx The amount to add to the rectangle's left and right coordinates
     * @param dy The amount to add to the rectangle's top and bottom coordinates
     */
    fun offset(dx: Int, dy: Int) {
        left += dx.toFloat()
        top += dy.toFloat()
        right += dx.toFloat()
        bottom += dy.toFloat()
    }

    /**
     * Offset the rectangle to a specific (left, top) position,
     * keeping its width and height the same.
     *
     * @param newLeft   The new "left" coordinate for the rectangle
     * @param newTop    The new "top" coordinate for the rectangle
     */
    fun offsetTo(newLeft: Int, newTop: Int) {
        right += newLeft - left
        bottom += newTop - top
        left = newLeft.toFloat()
        top = newTop.toFloat()
    }

    /**
     * Inset the rectangle by (dx,dy). If dx is positive, then the sides are
     * moved inwards, making the rectangle narrower. If dx is negative, then the
     * sides are moved outwards, making the rectangle wider. The same holds true
     * for dy and the top and bottom.
     *
     * @param dx The amount to add(subtract) from the rectangle's left(right)
     * @param dy The amount to add(subtract) from the rectangle's top(bottom)
     */
    fun inset(dx: Int, dy: Int) {
        left += dx.toFloat()
        top += dy.toFloat()
        right -= dx.toFloat()
        bottom -= dy.toFloat()
    }

    /**
     * Insets the rectangle on all sides specified by the dimensions of the `insets`
     * rectangle.
     * @hide
     * @param insets The rectangle specifying the insets on all side.
     */
    fun inset(insets: Rect) {
        left += insets.left.toFloat()
        top += insets.top.toFloat()
        right -= insets.right.toFloat()
        bottom -= insets.bottom.toFloat()
    }

    /**
     * Insets the rectangle on all sides specified by the insets.
     *
     * @param left The amount to add from the rectangle's left
     * @param top The amount to add from the rectangle's top
     * @param right The amount to subtract from the rectangle's right
     * @param bottom The amount to subtract from the rectangle's bottom
     */
    fun inset(left: Int, top: Int, right: Int, bottom: Int) {
        this.left += left.toFloat()
        this.top += top.toFloat()
        this.right -= right.toFloat()
        this.bottom -= bottom.toFloat()
    }

    /**
     * Returns true if (x,y) is inside the rectangle. The left and top are
     * considered to be inside, while the right and bottom are not. This means
     * that for a x,y to be contained: left <= x < right and top <= y < bottom.
     * An empty rectangle never contains any point.
     *
     * @param x The X coordinate of the point being tested for containment
     * @param y The Y coordinate of the point being tested for containment
     * @return true iff (x,y) are contained by the rectangle, where containment
     * means left <= x < right and top <= y < bottom
     */
    fun contains(x: Float, y: Float): Boolean {
        return left < right && top < bottom // check for empty first
                && x >= left && x < right && y >= top && y < bottom
    }

    fun contains(x: Int, y: Int): Boolean {
        return left < right && top < bottom // check for empty first
                && x >= left && x < right && y >= top && y < bottom
    }

    fun contains(x: Double, y: Double): Boolean {
        return left < right && top < bottom // check for empty first
                && x >= left && x < right && y >= top && y < bottom
    }

    /**
     * Returns true iff the 4 specified sides of a rectangle are inside or equal
     * to this rectangle. i.e. is this rectangle a superset of the specified
     * rectangle. An empty rectangle never contains another rectangle.
     *
     * @param left The left side of the rectangle being tested for containment
     * @param top The top of the rectangle being tested for containment
     * @param right The right side of the rectangle being tested for containment
     * @param bottom The bottom of the rectangle being tested for containment
     * @return true iff the the 4 specified sides of a rectangle are inside or
     * equal to this rectangle
     */
    fun contains(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        // check for empty first
        return this.left < this.right && this.top < this.bottom // now check for containment
                && this.left <= left && this.top <= top && this.right >= right && this.bottom >= bottom
    }

    /**
     * Returns true iff the specified rectangle r is inside or equal to this
     * rectangle. An empty rectangle never contains another rectangle.
     *
     * @param r The rectangle being tested for containment.
     * @return true iff the specified rectangle r is inside or equal to this
     * rectangle
     */
    operator fun contains(r: Rect): Boolean {
        // check for empty first
        return left < right && top < bottom // now check for containment
                && left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom
    }

    /**
     * If the rectangle specified by left,top,right,bottom intersects this
     * rectangle, return true and set this rectangle to that intersection,
     * otherwise return false and do not change this rectangle. No check is
     * performed to see if either rectangle is empty. Note: To just test for
     * intersection, use [.intersects].
     *
     * @param left The left side of the rectangle being intersected with this
     * rectangle
     * @param top The top of the rectangle being intersected with this rectangle
     * @param right The right side of the rectangle being intersected with this
     * rectangle.
     * @param bottom The bottom of the rectangle being intersected with this
     * rectangle.
     * @return true if the specified rectangle and this rectangle intersect
     * (and this rectangle is then set to that intersection) else
     * return false and do not change this rectangle.
     */
    fun intersect(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        if (this.left < right && left < this.right && this.top < bottom && top < this.bottom) {
            if (this.left < left) this.left = left.toFloat()
            if (this.top < top) this.top = top.toFloat()
            if (this.right > right) this.right = right.toFloat()
            if (this.bottom > bottom) this.bottom = bottom.toFloat()
            return true
        }
        return false
    }

    /**
     * If the specified rectangle intersects this rectangle, return true and set
     * this rectangle to that intersection, otherwise return false and do not
     * change this rectangle. No check is performed to see if either rectangle
     * is empty. To just test for intersection, use intersects()
     *
     * @param r The rectangle being intersected with this rectangle.
     * @return true if the specified rectangle and this rectangle intersect
     * (and this rectangle is then set to that intersection) else
     * return false and do not change this rectangle.
     */
    fun intersect(r: Rect): Boolean {
        return intersect(r.left, r.top, r.right, r.bottom)
    }

    fun intersect(r: RectF): Boolean {
        return intersect(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
    }

    /**
     * If the specified rectangle intersects this rectangle, set this rectangle to that
     * intersection, otherwise set this rectangle to the empty rectangle.
     * @see .inset
     * @hide
     */
    fun intersectUnchecked(other: Rect) {
        left = max(left, other.left.toFloat())
        top = max(top, other.top.toFloat())
        right = min(right, other.right.toFloat())
        bottom = min(bottom, other.bottom.toFloat())
    }

    /**
     * If rectangles a and b intersect, return true and set this rectangle to
     * that intersection, otherwise return false and do not change this
     * rectangle. No check is performed to see if either rectangle is empty.
     * To just test for intersection, use intersects()
     *
     * @param a The first rectangle being intersected with
     * @param b The second rectangle being intersected with
     * @return true iff the two specified rectangles intersect. If they do, set
     * this rectangle to that intersection. If they do not, return
     * false and do not change this rectangle.
     */
    fun setIntersect(a: Rect, b: Rect): Boolean {
        if (a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom) {
            left = max(a.left, b.left).toFloat()
            top = max(a.top, b.top).toFloat()
            right = min(a.right, b.right).toFloat()
            bottom = min(a.bottom, b.bottom).toFloat()
            return true
        }
        return false
    }

    /**
     * Returns true if this rectangle intersects the specified rectangle.
     * In no event is this rectangle modified. No check is performed to see
     * if either rectangle is empty. To record the intersection, use intersect()
     * or setIntersect().
     *
     * @param left The left side of the rectangle being tested for intersection
     * @param top The top of the rectangle being tested for intersection
     * @param right The right side of the rectangle being tested for
     * intersection
     * @param bottom The bottom of the rectangle being tested for intersection
     * @return true iff the specified rectangle intersects this rectangle. In
     * no event is this rectangle modified.
     */
    fun intersects(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return this.left < right && left < this.right && this.top < bottom && top < this.bottom
    }

    /**
     * Returns true iff the two specified rectangles intersect. In no event are
     * either of the rectangles modified. To record the intersection,
     * use [.intersect] or [.setIntersect].
     *
     * @param a The first rectangle being tested for intersection
     * @param b The second rectangle being tested for intersection
     * @return true iff the two specified rectangles intersect. In no event are
     * either of the rectangles modified.
     */
    fun intersects(a: Rect, b: Rect): Boolean {
        return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom
    }

    /**
     * Update this Rect to enclose itself and the specified rectangle. If the
     * specified rectangle is empty, nothing is done. If this rectangle is empty
     * it is set to the specified rectangle.
     *
     * @param left The left edge being unioned with this rectangle
     * @param top The top edge being unioned with this rectangle
     * @param right The right edge being unioned with this rectangle
     * @param bottom The bottom edge being unioned with this rectangle
     */
    fun union(left: Int, top: Int, right: Int, bottom: Int) {
        if (left < right && top < bottom) {
            if (this.left < this.right && this.top < this.bottom) {
                if (this.left > left) this.left = left.toFloat()
                if (this.top > top) this.top = top.toFloat()
                if (this.right < right) this.right = right.toFloat()
                if (this.bottom < bottom) this.bottom = bottom.toFloat()
            } else {
                this.left = left.toFloat()
                this.top = top.toFloat()
                this.right = right.toFloat()
                this.bottom = bottom.toFloat()
            }
        }
    }

    /**
     * Update this Rect to enclose itself and the specified rectangle. If the
     * specified rectangle is empty, nothing is done. If this rectangle is empty
     * it is set to the specified rectangle.
     *
     * @param r The rectangle being unioned with this rectangle
     */
    fun union(r: Rect) {
        union(r.left, r.top, r.right, r.bottom)
    }

    /**
     * Update this Rect to enclose itself and the [x,y] coordinate. There is no
     * check to see that this rectangle is non-empty.
     *
     * @param x The x coordinate of the point to add to the rectangle
     * @param y The y coordinate of the point to add to the rectangle
     */
    fun union(x: Int, y: Int) {
        if (x < left) {
            left = x.toFloat()
        } else if (x > right) {
            right = x.toFloat()
        }
        if (y < top) {
            top = y.toFloat()
        } else if (y > bottom) {
            bottom = y.toFloat()
        }
    }

    /**
     * Swap top/bottom or left/right if there are flipped (i.e. left > right
     * and/or top > bottom). This can be called if
     * the edges are computed separately, and may have crossed over each other.
     * If the edges are already correct (i.e. left <= right and top <= bottom)
     * then nothing is done.
     */
    fun sort() {
        if (left > right) {
            val temp = left.toInt()
            left = right
            right = temp.toFloat()
        }
        if (top > bottom) {
            val temp = top.toInt()
            top = bottom
            bottom = temp.toFloat()
        }
    }

    fun set(left: Number, top: Number, right: Number, bottom: Number) {
        this.left = left.toFloat()
        this.top = top.toFloat()
        this.right = right.toFloat()
        this.bottom = bottom.toFloat()
    }
}