/*
 * Copyright (C) 2014 The Android Open Source Project
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
package dev.topping.ios.constraint.shared.graphics

import dev.topping.ios.constraint.core.motion.utils.Rect

/**
 * Defines a simple shape, used for bounding graphical regions.
 *
 *
 * Can be computed for a TView, or computed by a Drawable, to drive the shape of
 * shadows cast by a TView, or to clip the contents of the TView.
 *
 * @see android.view.ViewOutlineProvider
 *
 * @see android.view.TView.setOutlineProvider
 * @see Drawable.getOutline
 */
class Outline {

    /** @hide
     */
    var mMode = MODE_EMPTY

    /**
     * Only guaranteed to be non-null when mode == MODE_PATH
     *
     * @hide
     */
    var mPath: Path? = null

    /** @hide
     */
    val mRect: Rect = Rect()
    /**
     * Returns the rounded rect radius, if set, or a value less than 0 if a path has
     * been set via [.setPath]. A return value of `0`
     * indicates a non-rounded rect.
     *
     * @return the rounded rect radius, or value < 0
     */
    /** @hide
     */
    var radius = RADIUS_UNDEFINED
    /**
     * Returns the alpha represented by the Outline.
     */
    /**
     * Sets the alpha represented by the Outline - the degree to which the
     * producer is guaranteed to be opaque over the Outline's shape.
     *
     *
     * An alpha value of `0.0f` either represents completely
     * transparent content, or content that isn't guaranteed to fill the shape
     * it publishes.
     *
     *
     * Content producing a fully opaque (alpha = `1.0f`) outline is
     * assumed by the drawing system to fully cover content beneath it,
     * meaning content beneath may be optimized away.
     */
    /** @hide
     */
    var alpha = 0f

    /**
     * Constructs an empty Outline. Call one of the setter methods to make
     * the outline valid for use with a TView.
     */
    constructor() {}

    /**
     * Constructs an Outline with a copy of the data in src.
     */
    constructor(src: Outline) {
        set(src)
    }

    /**
     * Sets the outline to be empty.
     *
     * @see .isEmpty
     */
    fun setEmpty() {
        if (mPath != null) {
            // rewind here to avoid thrashing the allocations, but could alternately clear ref
            mPath!!.rewind()
        }
        mMode = MODE_EMPTY
        mRect.setEmpty()
        radius = RADIUS_UNDEFINED
    }

    /**
     * Returns whether the Outline is empty.
     *
     *
     * Outlines are empty when constructed, or if [.setEmpty] is called,
     * until a setter method is called
     *
     * @see .setEmpty
     */
    val isEmpty: Boolean
        get() = mMode == MODE_EMPTY

    /**
     * Returns whether the outline can be used to clip a TView.
     *
     *
     * As of API 33, all Outline shapes support clipping. Prior to API 33, only Outlines that
     * could be represented as a rectangle, circle, or round rect supported clipping.
     *
     * @see android.view.TView.setClipToOutline
     */
    fun canClip(): Boolean {
        return true
    }

    /**
     * Replace the contents of this Outline with the contents of src.
     *
     * @param src Source outline to copy from.
     */
    fun set(src: Outline) {
        mMode = src.mMode
        if (src.mMode == MODE_PATH) {
            if (mPath == null) {
                mPath = Path()
            }
            mPath!!.set(src.mPath!!)
        }
        mRect.set(src.mRect)
        radius = src.radius
        alpha = src.alpha
    }

    /**
     * Sets the Outline to the rect defined by the input coordinates.
     */
    fun setRect(left: Int, top: Int, right: Int, bottom: Int) {
        setRoundRect(left, top, right, bottom, 0.0f)
    }

    /**
     * Convenience for [.setRect]
     */
    fun setRect(rect: Rect) {
        setRect(rect.left, rect.top, rect.right, rect.bottom)
    }

    /**
     * Sets the Outline to the rounded rect defined by the input coordinates and corner radius.
     *
     *
     * Passing a zero radius is equivalent to calling [.setRect]
     */
    fun setRoundRect(left: Int, top: Int, right: Int, bottom: Int, radius: Float) {
        if (left >= right || top >= bottom) {
            setEmpty()
            return
        }
        if (mMode == MODE_PATH) {
            // rewind here to avoid thrashing the allocations, but could alternately clear ref
            mPath!!.rewind()
        }
        mMode = MODE_ROUND_RECT
        mRect.set(left, top, right, bottom)
        this.radius = radius
    }

    /**
     * Convenience for [.setRoundRect]
     */
    fun setRoundRect(rect: Rect, radius: Float) {
        setRoundRect(rect.left, rect.top, rect.right, rect.bottom, radius)
    }

    /**
     * Populates `outBounds` with the outline bounds, if set, and returns
     * `true`. If no outline bounds are set, or if a path has been set
     * via [.setPath], returns `false`.
     *
     * @param outRect the rect to populate with the outline bounds, if set
     * @return `true` if `outBounds` was populated with outline
     * bounds, or `false` if no outline bounds are set
     */
    fun getRect(outRect: Rect): Boolean {
        if (mMode != MODE_ROUND_RECT) {
            return false
        }
        outRect.set(mRect)
        return true
    }

    /**
     * Sets the outline to the oval defined by input rect.
     */
    fun setOval(left: Int, top: Int, right: Int, bottom: Int) {
        if (left >= right || top >= bottom) {
            setEmpty()
            return
        }
        if (bottom - top == right - left) {
            // represent circle as round rect, for efficiency, and to enable clipping
            setRoundRect(left, top, right, bottom, (bottom - top) / 2.0f)
            return
        }
        if (mPath == null) {
            mPath = Path()
        } else {
            mPath!!.rewind()
        }
        mMode = MODE_PATH
        mPath!!.addOval(
            left.toFloat(),
            top.toFloat(),
            right.toFloat(),
            bottom.toFloat(),
            Path.Direction.CW
        )
        mRect.setEmpty()
        radius = RADIUS_UNDEFINED
    }

    /**
     * Convenience for [.setOval]
     */
    fun setOval(rect: Rect) {
        setOval(rect.left, rect.top, rect.right, rect.bottom)
    }

    /**
     * Sets the Outline to a
     * [convex path][android.graphics.Path.isConvex].
     *
     * @param convexPath used to construct the Outline. As of
     * [android.os.Build.VERSION_CODES.Q], it is no longer required to be
     * convex.
     *
     */
    @Deprecated(
        """As of {@link android.os.Build.VERSION_CODES#Q}, the restriction
      that the path must be convex is removed. However, the API is misnamed until
      {@link android.os.Build.VERSION_CODES#R}, when {@link #setPath} is
      introduced. Use {@link #setPath} instead."""
    )
    fun setConvexPath(convexPath: Path) {
        setPath(convexPath)
    }

    /**
     * Sets the Outline to a [path][android.graphics.Path].
     *
     * @param path used to construct the Outline.
     */
    fun setPath(path: Path) {
        if (path.isEmpty) {
            setEmpty()
            return
        }
        if (mPath == null) {
            mPath = Path()
        }
        mMode = MODE_PATH
        mPath!!.set(path)
        mRect.setEmpty()
        radius = RADIUS_UNDEFINED
    }

    /**
     * Offsets the Outline by (dx,dy). Offsetting is cumulative, so additional calls to
     * offset() will add to previous offset values. Offset only applies to the current
     * geometry (setRect(), setPath(), etc.); setting new geometry resets any existing
     * offset.
     */
    fun offset(dx: Int, dy: Int) {
        if (mMode == MODE_ROUND_RECT) {
            mRect.offset(dx, dy)
        } else if (mMode == MODE_PATH) {
            mPath!!.offset(dx.toFloat(), dy.toFloat())
        }
    }

    companion object {
        private const val RADIUS_UNDEFINED = Float.NEGATIVE_INFINITY

        /** @hide
         */
        const val MODE_EMPTY = 0

        /** @hide
         */
        const val MODE_ROUND_RECT = 1

        /** @hide
         */
        const val MODE_PATH = 2
    }
}