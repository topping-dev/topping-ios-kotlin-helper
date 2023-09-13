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
import kotlin.math.*

/**
 * An ImageView that can display, combine and filter images. **Added in 2.0**
 *
 *
 * Subclass of ImageView to handle various common filtering operations
 *
 *
 * <h2>ImageFilterView attributes</h2>
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
class ImageFilterView(val context: TContext, val attrs: AttributeSet, val self: TView, val selfImage: TImageView) {
    internal class ImageMatrix {
        var mMatrix = FloatArray(4 * 5)
        var mColorMatrix: ColorMatrix = ColorMatrix()
        var mTmpColorMatrix: ColorMatrix = ColorMatrix()
        var mBrightness = 1f
        var mSaturation = 1f
        var mContrast = 1f
        var mWarmth = 1f
        private fun saturation(saturationStrength: Float) {
            val Rf = 0.2999f
            val Gf = 0.587f
            val Bf = 0.114f
            val ms = 1.0f - saturationStrength
            val Rt = Rf * ms
            val Gt = Gf * ms
            val Bt = Bf * ms
            mMatrix[0] = Rt + saturationStrength
            mMatrix[1] = Gt
            mMatrix[2] = Bt
            mMatrix[3] = 0f
            mMatrix[4] = 0f
            mMatrix[5] = Rt
            mMatrix[6] = Gt + saturationStrength
            mMatrix[7] = Bt
            mMatrix[8] = 0f
            mMatrix[9] = 0f
            mMatrix[10] = Rt
            mMatrix[11] = Gt
            mMatrix[12] = Bt + saturationStrength
            mMatrix[13] = 0f
            mMatrix[14] = 0f
            mMatrix[15] = 0f
            mMatrix[16] = 0f
            mMatrix[17] = 0f
            mMatrix[18] = 1f
            mMatrix[19] = 0f
        }

        private fun warmth(warmth: Float) {
            var warmth = warmth
            val baseTemperature = 5000f
            if (warmth <= 0) warmth = .01f
            var tmpColor_r: Float
            var tmpColor_g: Float
            var tmpColor_b: Float
            var kelvin = baseTemperature / warmth
            run {
                // simulate a black body radiation
                val centiKelvin = kelvin / 100
                val colorR: Float
                val colorG: Float
                val colorB: Float
                if (centiKelvin > 66) {
                    val tmp = centiKelvin - 60f
                    // Original statements (all decimal values)
                    // colorR = (329.698727446f * (float) pow(tmp, -0.1332047592f))
                    // colorG = (288.1221695283f * (float) pow(tmp, 0.0755148492f))
                    colorR = 329.69873f * pow(tmp, -0.13320476f).toFloat()
                    colorG = 288.12216f * pow(tmp, 0.07551485f).toFloat()
                } else {
                    // Original statements (all decimal values)
                    // colorG = (99.4708025861f * (float) log(centiKelvin) - 161.1195681661f);
                    colorG = 99.4708f * log(centiKelvin.toDouble()).toFloat() - 161.11957f
                    colorR = 255f
                }
                colorB = if (centiKelvin < 66) {
                    if (centiKelvin > 19) {
                        // Original statements (all decimal values)
                        // 138.5177312231f * (float) log(centiKelvin - 10) - 305.0447927307f);
                        (138.51773f
                                * log((centiKelvin - 10).toDouble()).toFloat() - 305.0448f)
                    } else {
                        0f
                    }
                } else {
                    255f
                }
                tmpColor_r = min(255f, max(colorR, 0f))
                tmpColor_g = min(255f, max(colorG, 0f))
                tmpColor_b = min(255f, max(colorB, 0f))
            }
            var color_r = tmpColor_r
            var color_g = tmpColor_g
            var color_b = tmpColor_b
            kelvin = baseTemperature
            run {
                // simulate a black body radiation
                val centiKelvin = kelvin / 100
                val colorR: Float
                val colorG: Float
                val colorB: Float
                if (centiKelvin > 66) {
                    val tmp = centiKelvin - 60f
                    // Original statements (all decimal values)
                    //  colorR = (329.698727446f * (float) pow(tmp, -0.1332047592f));
                    //  colorG = (288.1221695283f * (float) pow(tmp, 0.0755148492f));
                    colorR = 329.69873f * pow(tmp, -0.13320476f).toFloat()
                    colorG = 288.12216f * pow(tmp, 0.07551485f).toFloat()
                } else {
                    // Original statements (all decimal values)
                    //float of (99.4708025861f * (float) log(centiKelvin) - 161.1195681661f);
                    colorG = 99.4708f * log(centiKelvin.toDouble()).toFloat() - 161.11957f
                    colorR = 255f
                }
                colorB = if (centiKelvin < 66) {
                    if (centiKelvin > 19) {
                        // Original statements (all decimal values)
                        //float of (138.5177312231 * log(centiKelvin - 10) - 305.0447927307);
                        138.51773f * log((centiKelvin - 10).toDouble()).toFloat() - 305.0448f
                    } else {
                        0f
                    }
                } else {
                    255f
                }
                tmpColor_r = min(255f, max(colorR, 0f)).toFloat()
                tmpColor_g = min(255f, max(colorG, 0f)).toFloat()
                tmpColor_b = min(255f, max(colorB, 0f)).toFloat()
            }
            color_r /= tmpColor_r
            color_g /= tmpColor_g
            color_b /= tmpColor_b
            mMatrix[0] = color_r
            mMatrix[1] = 0f
            mMatrix[2] = 0f
            mMatrix[3] = 0f
            mMatrix[4] = 0f
            mMatrix[5] = 0f
            mMatrix[6] = color_g
            mMatrix[7] = 0f
            mMatrix[8] = 0f
            mMatrix[9] = 0f
            mMatrix[10] = 0f
            mMatrix[11] = 0f
            mMatrix[12] = color_b
            mMatrix[13] = 0f
            mMatrix[14] = 0f
            mMatrix[15] = 0f
            mMatrix[16] = 0f
            mMatrix[17] = 0f
            mMatrix[18] = 1f
            mMatrix[19] = 0f
        }

        private fun brightness(brightness: Float) {
            mMatrix[0] = brightness
            mMatrix[1] = 0f
            mMatrix[2] = 0f
            mMatrix[3] = 0f
            mMatrix[4] = 0f
            mMatrix[5] = 0f
            mMatrix[6] = brightness
            mMatrix[7] = 0f
            mMatrix[8] = 0f
            mMatrix[9] = 0f
            mMatrix[10] = 0f
            mMatrix[11] = 0f
            mMatrix[12] = brightness
            mMatrix[13] = 0f
            mMatrix[14] = 0f
            mMatrix[15] = 0f
            mMatrix[16] = 0f
            mMatrix[17] = 0f
            mMatrix[18] = 1f
            mMatrix[19] = 0f
        }

        fun updateMatrix(view: TImageView) {
            mColorMatrix.reset()
            var filter = false
            if (mSaturation != 1.0f) {
                saturation(mSaturation)
                mColorMatrix.set(mMatrix)
                filter = true
            }
            if (mContrast != 1.0f) {
                mTmpColorMatrix.setScale(mContrast, mContrast, mContrast, 1f)
                mColorMatrix.postConcat(mTmpColorMatrix)
                filter = true
            }
            if (mWarmth != 1.0f) {
                warmth(mWarmth)
                mTmpColorMatrix.set(mMatrix)
                mColorMatrix.postConcat(mTmpColorMatrix)
                filter = true
            }
            if (mBrightness != 1.0f) {
                brightness(mBrightness)
                mTmpColorMatrix.set(mMatrix)
                mColorMatrix.postConcat(mTmpColorMatrix)
                filter = true
            }
            if (filter) {
                view.setColorFilter(ColorMatrixColorFilter(mColorMatrix))
            } else {
                view.clearColorFilter()
            }
        }

        fun updateMatrix(view: TImageButton) {
            mColorMatrix.reset()
            var filter = false
            if (mSaturation != 1.0f) {
                saturation(mSaturation)
                mColorMatrix.set(mMatrix)
                filter = true
            }
            if (mContrast != 1.0f) {
                mTmpColorMatrix.setScale(mContrast, mContrast, mContrast, 1f)
                mColorMatrix.postConcat(mTmpColorMatrix)
                filter = true
            }
            if (mWarmth != 1.0f) {
                warmth(mWarmth)
                mTmpColorMatrix.set(mMatrix)
                mColorMatrix.postConcat(mTmpColorMatrix)
                filter = true
            }
            if (mBrightness != 1.0f) {
                brightness(mBrightness)
                mTmpColorMatrix.set(mMatrix)
                mColorMatrix.postConcat(mTmpColorMatrix)
                filter = true
            }
            if (filter) {
                view.setColorFilter(ColorMatrixColorFilter(mColorMatrix))
            } else {
                view.clearColorFilter()
            }
        }
    }

    private val mImageMatrix = ImageMatrix()
    private var mOverlay = true
    private var mAltDrawable: TDrawable? = null
    private var mDrawable: TDrawable? = null
    private var mCrossfade = 0f
    private var mRoundPercent = 0f // rounds the corners as a percent
    private var mRound = Float.NaN // rounds the corners in dp if NaN RoundPercent is in effect
    private var mPath: Path? = null
    var mViewOutlineProvider: ViewOutlineProvider? = null
    var mRect: RectF? = null
    var mLayers: Array<TDrawable?> = arrayOfNulls(2)
    var mLayer: TDrawable? = null

    // ======================== support for pan/zoom/rotate =================
    // defined as 0 = center of screen
    // if with < scree with,  1 is the right edge lines up with screen
    // if width > screen width, 1 is thee left edge lines up
    // -1 works similarly
    // zoom 1 = the image fits such that the view is filed
    var mPanX = Float.NaN
    var mPanY = Float.NaN
    var mZoom = Float.NaN
    var mRotate = Float.NaN
    /**
     * Gets the pan from the center
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
     * @return pan in y. Where 0 is centered NaN if not set
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
     * @param pan sets the pan in X. Where 0 is centered
     */
    var imagePanY: Float
        get() = mPanY
        set(pan) {
            mPanY = pan
            updateViewMatrix()
        }
    /**
     * gets the zoom where 1 scales the image just enough to fill the view
     *
     * @return the zoom factor
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

    init {
        self.setParentType(this)
        self.swizzleFunction("setImageDrawable") { sup, params ->
            var args = params as Array<Any?>
            setImageDrawable(sup, params[0] as TDrawable?)
            0
        }
        self.swizzleFunction("setImageResource") { sup, params ->
            var args = params as Array<Any?>
            setImageResource(sup, params[0] as String)
            0
        }
        self.swizzleFunction("layout") { sup, params ->
            var args = params as Array<Any>
            layout(sup, params[0] as Int, params[1] as Int, params[2] as Int, params[3] as Int)
            0
        }
        attrs.forEach { kvp ->
            if(kvp.key == "app_altSrc") {
                mAltDrawable = context.getResources().getDrawable(kvp.value)
            } else if (kvp.key == "app_crossfade") {
                mCrossfade = context.getResources().getFloat(kvp.value, 0f)
            } else if (kvp.key == "app_warmth") {
                warmth = context.getResources().getFloat(kvp.value, 0f)
            } else if (kvp.key == "app_saturation") {
                saturation = context.getResources().getFloat(kvp.value, 0f)
            } else if (kvp.key == "app_contrast") {
                contrast = context.getResources().getFloat(kvp.value, 0f)
            } else if (kvp.key == "app_brightness") {
                brightness = context.getResources().getFloat(kvp.value, 0f)
            } else if (kvp.key == "app_round") {
                round = context.getResources().getDimension(kvp.value, 0f)
            } else if (kvp.key == "app_roundPercent") {
                roundPercent = context.getResources().getFloat(kvp.value, 0f)
            } else if (kvp.key == "app_overlay") {
                setOverlay(context.getResources().getBoolean(kvp.value, mOverlay))
            } else if (kvp.key == "app_imagePanX") {
                imagePanX = context.getResources().getFloat(kvp.value, mPanX)
            } else if (kvp.key == "app_imagePanY") {
                imagePanY = context.getResources().getFloat(kvp.value, mPanY)
            } else if (kvp.key == "app_imageRotate") {
                imageRotate = context.getResources().getFloat(kvp.value, mRotate)
            } else if (kvp.key == "app_imageZoom") {
                imageZoom = context.getResources().getFloat(kvp.value, mZoom)
            }
        }

        mDrawable = selfImage.getDrawable()
        if (mAltDrawable != null && mDrawable != null) {
            mDrawable = selfImage.getDrawable()?.mutate()
            mLayers[0] = mDrawable
            mLayers[1] = mAltDrawable!!.mutate()
            mLayer = context.createLayerDrawable(mLayers)
            mLayer!!.getDrawable(1).setAlpha((255 * mCrossfade).toInt())
            if (!mOverlay) {
                mLayer!!.getDrawable(0).setAlpha((255 * (1 - mCrossfade)).toInt())
            }
            selfImage.setImageDrawable(mLayer)
        } else {
            mDrawable = selfImage.getDrawable()
            if (mDrawable != null) {
                mDrawable = mDrawable!!.mutate()
                mLayers!![0] = mDrawable
            }
        }
    }

    fun setImageDrawable(sup: TView?, drawable: TDrawable?) {
        if (mAltDrawable != null && drawable != null) {
            mDrawable = drawable.mutate()
            mLayers[0] = mDrawable
            mLayers[1] = mAltDrawable
            mLayer = context.createLayerDrawable(mLayers)
            sup?.setImageDrawable(mLayer)
            crossfade = mCrossfade
        } else {
            sup?.setImageDrawable(drawable)
        }
    }

    fun setImageResource(sup:TView?, resId: String) {
        if (mAltDrawable != null) {
            mDrawable = context.getResources().getDrawable(resId)
            mLayers[0] = mDrawable
            mLayers[1] = mAltDrawable
            mLayer = context.createLayerDrawable(mLayers)
            sup?.setImageDrawable(mLayer)
            crossfade = mCrossfade
        } else {
            sup?.setImageResource(resId)
        }
    }

    /**
     * Set the alternative Image resource used in cross fading
     * @param resId id of drawable
     */
    fun setAltImageResource(resId: String) {
        mAltDrawable = context.getResources().getDrawable(resId)
        setAltImageDrawable(mAltDrawable)
    }

    /**
     * Set the alternative Image Drawable used in cross fading.
     * @param altDrawable of drawable
     */
    fun setAltImageDrawable(altDrawable: TDrawable?) {
        mAltDrawable = altDrawable?.mutate()
        mLayers[0] = mDrawable
        mLayers[1] = mAltDrawable
        mLayer = context.createLayerDrawable(mLayers)
        self.setImageDrawable(mLayer)
        crossfade = mCrossfade
    }

    private fun updateViewMatrix() {
        if (Float.isNaN(mPanX)
            && Float.isNaN(mPanY)
            && Float.isNaN(mZoom)
            && Float.isNaN(mRotate)
        ) {
            selfImage.setScaleType(ScaleType.FIT_CENTER)
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
        val imageMatrix = Matrix33()
        imageMatrix.reset()
        val iw: Float = selfImage.getDrawable()?.getIntrinsicWidth() ?: 0f
        val ih: Float = selfImage.getDrawable()?.getIntrinsicHeight() ?: 0f
        val sw: Float = self.getWidth().toFloat()
        val sh: Float = self.getHeight().toFloat()
        val scale = zoom * if (iw * sh < ih * sw) sw / iw else sh / ih
        imageMatrix.postScale(scale, scale)
        val tx = 0.5f * (panX * (sw - scale * iw) + sw - scale * iw)
        val ty = 0.5f * (panY * (sh - scale * ih) + sh - scale * ih)
        imageMatrix.postTranslate(tx, ty)
        imageMatrix.postRotate(rota, sw / 2, sh / 2)
        selfImage.setImageMatrix(imageMatrix)
        selfImage.setScaleType(ScaleType.MATRIX)
    }

    /**
     * Defines whether the alt image will be faded in on top
     * of the original image or if it will be crossfaded with it.
     * Default is true;
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
            mImageMatrix.updateMatrix(selfImage)
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
            mImageMatrix.updateMatrix(selfImage)
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
            mImageMatrix.updateMatrix(selfImage)
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
                selfImage.setImageDrawable(mLayer)
            }
        }
    /**
     * Returns the currently applied brightness
     *
     * @return brightness 0 = black, 1 = original, 2 = twice as bright
     */
    /**
     * sets the brightness of the image;
     * 0 = black, 1 = original, 2 = twice as bright
     *
     * @param brightness
     */
    var brightness: Float
        get() = mImageMatrix.mBrightness
        set(brightness) {
            mImageMatrix.mBrightness = brightness
            mImageMatrix.updateMatrix(selfImage)
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

    fun layout(sup:TView, l: Int, t: Int, r: Int, b: Int) {
        sup.layout(l, t, r, b)
        setMatrix()
    }
}
