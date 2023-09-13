/*
 * Copyright (C) 2018 The Android Open Source Project
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
package dev.topping.ios.constraint.constraintlayout.utils.widget

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.core.motion.utils.RectF
import dev.topping.ios.constraint.shared.graphics.*
import org.jetbrains.skia.Matrix33
import kotlin.math.min

/**
 * An AppCompatImageButton that can display, combine and filter images. **Added in 2.0**
 *
 *
 * Subclass of AppCompatImageButton to handle various common filtering operations.
 *
 * <h2>ImageFilterButton attributes</h2>
 * <table summary="KeyTrigger attributes">
 * <tr>
 * <td>altSrc</td>
 * <td>Provide and alternative image to the src image to allow cross fading</td>
</tr> *
 * <tr>
 * <td>saturation</td>
 * <td>Sets the saturation of the image.<br></br>  0 = grayscale, 1 = original, 2 = hyper saturated</td>
</tr> * <tr>
 * <td>brightness</td>
 * <td>Sets the brightness of the image.<br></br>  0 = black, 1 = original, 2 = twice as bright
</td> *
</tr> *
 * <tr>
 * <td>warmth</td>
 * <td>This adjust the apparent color temperature of the image.<br></br> 1=neutral, 2=warm, .5=cold</td>
</tr> *
 * <tr>
 * <td>contrast</td>
 * <td>This sets the contrast. 1 = unchanged, 0 = gray, 2 = high contrast</td>
</tr> *
 * <tr>
 * <td>crossfade</td>
 * <td>Set the current mix between the two images. <br></br>  0=src 1= altSrc image</td>
</tr> *
 * <tr>
 * <td>round</td>
 * <td>(id) call the TransitionListener with this trigger id</td>
</tr> *
 * <tr>
 * <td>roundPercent</td>
 * <td>Set the corner radius of curvature  as a fraction of the smaller side.
 * For squares 1 will result in a circle</td>
</tr> *
 * <tr>
 * <td>overlay</td>
 * <td>Defines whether the alt image will be faded in on top of the original image or if it will be
 * crossfaded with it. Default is true. Set to false for semitransparent objects</td>
</tr> *
</table> *
 */
class ImageFilterButton(val context: TContext, val attrs: AttributeSet, val self: TView, val selfImageButton: TImageButton) {
    private val mImageMatrix: ImageFilterView.ImageMatrix = ImageFilterView.ImageMatrix()
    private var mCrossfade = 0f
    private var mRoundPercent = 0f // rounds the corners as a percent
    private var mRound = Float.NaN // rounds the corners in dp if NaN RoundPercent is in effect
    private var mPath: Path? = null
    var mViewOutlineProvider: ViewOutlineProvider? = null
    var mRect: RectF? = null
    var mLayers: Array<TDrawable?>? = arrayOfNulls<TDrawable>(2)
    var mLayer: TDrawable? = null
    private var mOverlay = true
    private var mAltDrawable: TDrawable? = null
    private var mDrawable: TDrawable? = null

    init {
        self.setParentType(this)
        self.setPadding(0, 0, 0, 0)
        selfImageButton.swizzleFunction("setImageDrawable") { sup, params ->
            val args = params as Array<Any?>
            setImageDrawable(sup, args[0] as TDrawable?)
            0
        }
        selfImageButton.swizzleFunction("setImageResource") { sup, params ->
            val args = params as Array<Any?>
            setImageResource(sup, args[0] as String)
            0
        }
        self.swizzleFunction("layout") { sup, params ->
            val args = params as Array<Any>
            layout(sup, args[0] as Int, args[1] as Int, args[2] as Int, args[3] as Int)
            0
        }
        attrs.forEach { kvp ->
            if(kvp.key == "app_altSrc") {
                mAltDrawable = context.getResources().getDrawable(kvp.value)
            } else if(kvp.key == "app_crossfade") {
                mCrossfade = context.getResources().getFloat(kvp.value, 0f)
            } else if(kvp.key == "app_warmth") {
                warmth = context.getResources().getFloat(kvp.value, 0f)
            } else if(kvp.key == "app_saturation") {
                saturation = context.getResources().getFloat(kvp.value, 0f)
            } else if(kvp.key == "app_contrast") {
                contrast = context.getResources().getFloat(kvp.value, 0f)
            } else if(kvp.key == "app_round") {
                round = context.getResources().getDimension(kvp.value, 0f)
            } else if(kvp.key == "app_roundPercent") {
                roundPercent = context.getResources().getFloat(kvp.value, 0f)
            } else if(kvp.key == "app_overlay") {
                setOverlay(context.getResources().getBoolean(kvp.value, mOverlay))
            } else if(kvp.key == "app_imagePanX") {
                imagePanX = context.getResources().getFloat(kvp.value, mPanX)
            } else if(kvp.key == "app_imagePanY") {
                imagePanY = context.getResources().getFloat(kvp.value, mPanY)
            } else if(kvp.key == "app_imageRotate") {
                imageRotate = context.getResources().getFloat(kvp.value, mRotate)
            } else if(kvp.key == "app_imageZoom") {
                imageZoom = context.getResources().getFloat(kvp.value, mZoom)
            }
        }
        mDrawable = selfImageButton.getDrawable()
        if (mAltDrawable != null && mDrawable != null) {
            mDrawable = selfImageButton.getDrawable()!!.mutate()
            mLayers!![0] = mDrawable
            mLayers!![1] = mAltDrawable!!.mutate()
            mLayer = context.createLayerDrawable(mLayers!!)
            mLayer!!.getDrawable(1).setAlpha((255 * mCrossfade).toInt())
            if (!mOverlay) {
                mLayer!!.getDrawable(0).setAlpha((255 * (1 - mCrossfade)).toInt())
            }
            selfImageButton.setImageDrawable(mLayer)
        } else {
            mDrawable = selfImageButton.getDrawable()
            if (mDrawable != null) {
                mDrawable = mDrawable!!.mutate()
                mLayers!![0] = mDrawable
            }
        }
    }

    // ======================== support for pan/zoom/rotate =================
    // defined as 0 = center of screen
    // if with < scree with,  1 is the right edge lines up with screen
    // if width > screen width, 1 is thee left edge lines up
    // -1 works similarly
    // zoom 1 = the image fits such that the view is filed
    private var mPanX = Float.NaN
    private var mPanY = Float.NaN
    private var mZoom = Float.NaN
    private var mRotate = Float.NaN
    /**
     * gts the pan from the center
     * pan of 1 the image is "all the way to the right"
     * if the images width is greater than the screen width,
     * pan = 1 results in the left edge lining up
     * if the images width is less than the screen width,
     * pan = 1 results in the right edges lining up
     * if image width == screen width it does nothing
     *
     * @return the pan in X. Where 0 is centered = Float. NaN if not set
     */
    /**
     * sets the pan from the center
     * pan of 1 the image is "all the way to the right"
     * if the images width is greater than the screen width,
     * pan = 1 results in the left edge lining up
     * if the images width is less than the screen width,
     * pan = 1 results in the right edges lining up
     * if image width == screen width it does nothing
     *
     * @param pan sets the pan in X. Where 0 is centered
     */
    var imagePanX: Float
        get() = mPanX
        set(pan) {
            mPanX = pan
            updateViewMatrix()
        }
    /**
     * gets the pan from the center
     * pan of 1 the image is "all the way to the bottom"
     * if the images width is greater than the screen height,
     * pan = 1 results in the bottom edge lining up
     * if the images width is less than the screen height,
     * pan = 1 results in the top edges lining up
     * if image height == screen height it does nothing
     *
     * @return pan in Y. Where 0 is centered
     */
    /**
     * sets the pan from the center
     * pan of 1 the image is "all the way to the bottom"
     * if the images width is greater than the screen height,
     * pan = 1 results in the bottom edge lining up
     * if the images width is less than the screen height,
     * pan = 1 results in the top edges lining up
     * if image height == screen height it does nothing
     *
     * @param pan sets the pan in Y. Where 0 is centered
     */
    var imagePanY: Float
        get() = mPanY
        set(pan) {
            mPanY = pan
            updateViewMatrix()
        }
    /**
     * sets the zoom where 1 scales the image just enough to fill the view
     *
     * @return the zoom
     */
    /**
     * sets the zoom where 1 scales the image just enough to fill the view
     *
     * @param zoom the zoom factor
     */
    var imageZoom: Float
        get() = mZoom
        set(zoom) {
            mZoom = zoom
            updateViewMatrix()
        }
    /**
     * gets the rotation
     *
     * @return the rotation in degrees
     */
    /**
     * sets the rotation angle of the image in degrees
     *
     * @param rotation the rotation in degrees
     */
    var imageRotate: Float
        get() = mRotate
        set(rotation) {
            mRotate = rotation
            updateViewMatrix()
        }

    fun setImageDrawable(sup: TImageButton?, drawable: TDrawable?) {
        if (mAltDrawable != null && drawable != null) {
            mDrawable = drawable.mutate()
            mLayers!![0] = mDrawable
            mLayers!![1] = mAltDrawable
            mLayer = context.createLayerDrawable(mLayers!!)
            sup?.setImageDrawable(mLayer)
            crossfade = mCrossfade
        } else {
            sup?.setImageDrawable(drawable)
        }
    }

    fun setImageResource(sup: TImageButton?, resId: String) {
        if (mAltDrawable != null) {
            mDrawable = context.getResources().getDrawable(resId)!!.mutate()
            mLayers!![0] = mDrawable
            mLayers!![1] = mAltDrawable
            mLayer = context.createLayerDrawable(mLayers!!)
            sup?.setImageDrawable(mLayer)
            crossfade = mCrossfade
        } else {
            sup?.setImageResource(resId)
        }
    }

    /**
     * Set the alternative image used to cross fade to.
     *
     * @param resId
     */
    fun setAltImageResource(resId: String) {
        mAltDrawable = context.getResources().getDrawable(resId)!!.mutate()
        mLayers!![0] = mDrawable
        mLayers!![1] = mAltDrawable
        mLayer = context.createLayerDrawable(mLayers!!)
        selfImageButton.setImageDrawable(mLayer)
        crossfade = mCrossfade
    }

    private fun updateViewMatrix() {
        if (Float.isNaN(mPanX)
            && Float.isNaN(mPanY)
            && Float.isNaN(mZoom)
            && Float.isNaN(mRotate)
        ) {
            selfImageButton.setScaleType(ScaleType.FIT_CENTER)
            return
        }
        setMatrix()
    }

    private fun setMatrix() {
        if (Float.isNaN(mPanX)
            && Float.isNaN(mPanY)
            && Float.isNaN(mZoom)
            && Float.isNaN(mRotate)
        ) {
            return
        }
        val panX: Float = if (Float.isNaN(mPanX)) 0f else mPanX
        val panY: Float = if (Float.isNaN(mPanY)) 0f else mPanY
        val zoom: Float = if (Float.isNaN(mZoom)) 1f else mZoom
        val rota: Float = if (Float.isNaN(mRotate)) 0f else mRotate
        val imageMatrix = Matrix33.IDENTITY
        imageMatrix.reset()
        val iw: Float = selfImageButton.getDrawable()!!.getIntrinsicWidth()
        val ih: Float = selfImageButton.getDrawable()!!.getIntrinsicHeight()
        val sw: Float = self.getWidth().toFloat()
        val sh: Float = self.getHeight().toFloat()
        val scale = zoom * if (iw * sh < ih * sw) sw / iw else sh / ih
        imageMatrix.postScale(scale, scale)
        val tx = 0.5f * (panX * (sw - scale * iw) + sw - scale * iw)
        val ty = 0.5f * (panY * (sh - scale * ih) + sh - scale * ih)
        imageMatrix.postTranslate(tx, ty)
        imageMatrix.postRotate(rota, sw / 2, sh / 2)
        selfImageButton.setImageMatrix(imageMatrix)
        selfImageButton.setScaleType(ScaleType.MATRIX)
    }
    // ================================================================
    /**
     * Defines whether the alt image will be faded in on top of the original image or if it will be
     * crossfaded with it. Default is true.
     *
     * @param overlay
     */
    private fun setOverlay(overlay: Boolean) {
        mOverlay = overlay
    }
    /**
     * Returns the currently applied saturation
     *
     * @return 0 = grayscale, 1 = original, 2 = hyper saturated
     */
    /**
     * sets the saturation of the image;
     * 0 = grayscale, 1 = original, 2 = hyper saturated
     *
     * @param saturation
     */
    var saturation: Float
        get() = mImageMatrix.mSaturation
        set(saturation) {
            mImageMatrix.mSaturation = saturation
            mImageMatrix.updateMatrix(selfImageButton)
        }
    /**
     * Returns the currently applied contrast
     *
     * @return 1 = unchanged, 0 = gray, 2 = high contrast
     */
    /**
     * This sets the contrast. 1 = unchanged, 0 = gray, 2 = high contrast
     *
     * @param contrast
     */
    var contrast: Float
        get() = mImageMatrix.mContrast
        set(contrast) {
            mImageMatrix.mContrast = contrast
            mImageMatrix.updateMatrix(selfImageButton)
        }
    /**
     * Returns the currently applied warmth
     *
     * @return warmth 1 is neutral, 2 is warm, .5 is cold
     */
    /**
     * This makes the apparent color temperature of the image warmer or colder.
     *
     * @param warmth 1 is neutral, 2 is warm, .5 is cold
     */
    var warmth: Float
        get() = mImageMatrix.mWarmth
        set(warmth) {
            mImageMatrix.mWarmth = warmth
            mImageMatrix.updateMatrix(selfImageButton)
        }
    /**
     * Returns the currently applied crossfade.
     *
     * @return a number from 0 to 1
     */
    /**
     * Set the current mix between the two images that can be set on this view.
     *
     * @param crossfade a number from 0 to 1
     */
    var crossfade: Float
        get() = mCrossfade
        set(crossfade) {
            mCrossfade = crossfade
            if (mLayers != null) {
                if (!mOverlay) {
                    mLayer!!.getDrawable(0).setAlpha((255 * (1 - mCrossfade)).toInt())
                }
                mLayer!!.getDrawable(1).setAlpha((255 * mCrossfade).toInt())
                selfImageButton.setImageDrawable(mLayer)
            }
        }

    /**
     * sets the brightness of the image;
     * 0 = black, 1 = original, 2 = twice as bright
     *
     * @param brightness
     */
    fun setBrightness(brightness: Float) {
        mImageMatrix.mBrightness = brightness
        mImageMatrix.updateMatrix(selfImageButton)
    }
    /**
     * Get the fractional corner radius of curvature.
     *
     * @return Fractional radius of curvature with respect to smallest size
     */
    /**
     * Set the corner radius of curvature  as a fraction of the smaller side.
     * For squares 1 will result in a circle
     *
     * @param round the radius of curvature as a fraction of the smaller width
     */
    var roundPercent: Float
        get() = mRoundPercent
        set(round) {
            val change = mRoundPercent != round
            mRoundPercent = round
            if (mRoundPercent != 0.0f) {
                if (mPath == null) {
                    mPath = Path()
                }
                if (mRect == null) {
                    mRect = RectF()
                }
                if (mViewOutlineProvider == null) {
                    mViewOutlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: TView, outline: Outline) {
                            val w: Int = self.getWidth()
                            val h: Int = self.getHeight()
                            val r: Float = min(w, h) * mRoundPercent / 2
                            outline.setRoundRect(0, 0, w, h, r)
                        }
                    }
                    self.setOutlineProvider(mViewOutlineProvider)
                }
                self.setClipToOutline(true)
                val w: Int = self.getWidth()
                val h: Int = self.getHeight()
                val r: Float = min(w, h) * mRoundPercent / 2
                mRect!!.set(0, 0, w, h)
                mPath!!.reset()
                mPath!!.addRoundRect(mRect!!, r, r, Path.Direction.CW)
            } else {
                self.setClipToOutline(false)
            }
            if (change) {
                self.invalidateOutline()
            }
        }
    /**
     * Get the corner radius of curvature NaN = RoundPercent in effect.
     *
     * @return Radius of curvature
     */// force eval of roundPercent
    /**
     * Set the corner radius of curvature
     *
     * @param round the radius of curvature  NaN = default meaning roundPercent in effect
     */
    var round: Float
        get() = mRound
        set(round) {
            if (Float.isNaN(round)) {
                mRound = round
                val tmp = mRoundPercent
                mRoundPercent = -1f
                roundPercent = tmp // force eval of roundPercent
                return
            }
            val change = mRound != round
            mRound = round
            if (mRound != 0.0f) {
                if (mPath == null) {
                    mPath = Path()
                }
                if (mRect == null) {
                    mRect = RectF()
                }
                if (mViewOutlineProvider == null) {
                    mViewOutlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: TView, outline: Outline) {
                            val w: Int = self.getWidth()
                            val h: Int = self.getHeight()
                            outline.setRoundRect(0, 0, w, h, mRound)
                        }
                    }
                    self.setOutlineProvider(mViewOutlineProvider)
                }
                self.setClipToOutline(true)
                val w: Int = self.getWidth()
                val h: Int = self.getHeight()
                mRect!!.set(0, 0, w, h)
                mPath!!.reset()
                mPath!!.addRoundRect(mRect!!, mRound, mRound, Path.Direction.CW)
            } else {
                self.setClipToOutline(false)
            }
            if (change) {
                self.invalidateOutline()
            }
        }

    fun layout(sup: TView?, l: Int, t: Int, r: Int, b: Int) {
        sup?.layout(l, t, r, b)
        setMatrix()
    }
}