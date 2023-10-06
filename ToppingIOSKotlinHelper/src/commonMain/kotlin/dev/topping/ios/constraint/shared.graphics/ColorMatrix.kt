package dev.topping.ios.constraint.shared.graphics

import dev.topping.ios.constraint.Arrays
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ColorMatrix {
    /**
     * Return the array of floats representing this colormatrix.
     */
    val array = FloatArray(20)

    /**
     * Create a new colormatrix initialized to identity (as if reset() had
     * been called).
     */
    constructor() {
        reset()
    }

    /**
     * Create a new colormatrix initialized with the specified array of values.
     */
    constructor(src: FloatArray) {
        Arrays.arraycopy(src, 0, array, 0, 20)
    }

    /**
     * Create a new colormatrix initialized with the specified colormatrix.
     */
    constructor(src: ColorMatrix) {
        Arrays.arraycopy(src.array, 0, array, 0, 20)
    }

    /**
     * Set this colormatrix to identity:
     * [ 1 0 0 0 0   - red vector
     * 0 1 0 0 0   - green vector
     * 0 0 1 0 0   - blue vector
     * 0 0 0 1 0 ] - alpha vector
     */
    fun reset() {
        val a = array
        for (i in 19 downTo 1) {
            a[i] = 0f
        }
        a[18] = 1f
        a[12] = a[18]
        a[6] = a[12]
        a[0] = a[6]
    }

    /**
     * Assign the src colormatrix into this matrix, copying all of its values.
     */
    fun set(src: ColorMatrix) {
        Arrays.arraycopy(src.array, 0, array, 0, 20)
    }

    /**
     * Assign the array of floats into this matrix, copying all of its values.
     */
    fun set(src: FloatArray) {
        Arrays.arraycopy(src, 0, array, 0, 20)
    }

    /**
     * Set this colormatrix to scale by the specified values.
     */
    fun setScale(
        rScale: Float, gScale: Float, bScale: Float,
        aScale: Float
    ) {
        val a = array
        for (i in 19 downTo 1) {
            a[i] = 0f
        }
        a[0] = rScale
        a[6] = gScale
        a[12] = bScale
        a[18] = aScale
    }

    /**
     * Set the rotation on a color axis by the specified values.
     * axis=0 correspond to a rotation around the RED color
     * axis=1 correspond to a rotation around the GREEN color
     * axis=2 correspond to a rotation around the BLUE color
     */
    fun setRotate(axis: Int, degrees: Float) {
        reset()
        val radians: Float = degrees * PI.toFloat() / 180
        val cosine: Float = cos(radians)
        val sine: Float = sin(radians)
        when (axis) {
            0 -> {
                run {
                    array[12] = cosine
                    array[6] = array[12]
                }
                array[7] = sine
                array[11] = -sine
            }
            1 -> {
                run {
                    array[12] = cosine
                    array[0] = array[12]
                }
                array[2] = -sine
                array[10] = sine
            }
            2 -> {
                run {
                    array[6] = cosine
                    array[0] = array[6]
                }
                array[1] = sine
                array[5] = -sine
            }
            else -> throw Exception()
        }
    }

    /**
     * Set this colormatrix to the concatenation of the two specified
     * colormatrices, such that the resulting colormatrix has the same effect
     * as applying matB and then applying matA. It is legal for either matA or
     * matB to be the same colormatrix as this.
     */
    fun setConcat(matA: ColorMatrix, matB: ColorMatrix) {
        var tmp: FloatArray? = null
        tmp = if (matA === this || matB === this) {
            FloatArray(20)
        } else {
            array
        }
        val a = matA.array
        val b = matB.array
        var index = 0
        var j = 0
        while (j < 20) {
            for (i in 0..3) {
                tmp[index++] =
                    a[j + 0] * b[i + 0] + a[j + 1] * b[i + 5] + a[j + 2] * b[i + 10] + a[j + 3] * b[i + 15]
            }
            tmp[index++] = a[j + 0] * b[4] + a[j + 1] * b[9] + a[j + 2] * b[14] + a[j + 3] * b[19] +
                    a[j + 4]
            j += 5
        }
        if (!(tmp contentEquals array)) {
            Arrays.arraycopy(tmp, 0, array, 0, 20)
        }
    }

    /**
     * Concat this colormatrix with the specified prematrix. This is logically
     * the same as calling setConcat(this, prematrix);
     */
    fun preConcat(prematrix: ColorMatrix) {
        setConcat(this, prematrix)
    }

    /**
     * Concat this colormatrix with the specified postmatrix. This is logically
     * the same as calling setConcat(postmatrix, this);
     */
    fun postConcat(postmatrix: ColorMatrix) {
        setConcat(postmatrix, this)
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Set the matrix to affect the saturation of colors. A value of 0 maps the
     * color to gray-scale. 1 is identity.
     */
    fun setSaturation(sat: Float) {
        reset()
        val m = array
        val invSat = 1 - sat
        val R = 0.213f * invSat
        val G = 0.715f * invSat
        val B = 0.072f * invSat
        m[0] = R + sat
        m[1] = G
        m[2] = B
        m[5] = R
        m[6] = G + sat
        m[7] = B
        m[10] = R
        m[11] = G
        m[12] = B + sat
    }

    /**
     * Set the matrix to convert RGB to YUV
     */
    fun setRGB2YUV() {
        reset()
        val m = array
        // these coefficients match those in libjpeg
        m[0] = 0.299f
        m[1] = 0.587f
        m[2] = 0.114f
        m[5] = -0.16874f
        m[6] = -0.33126f
        m[7] = 0.5f
        m[10] = 0.5f
        m[11] = -0.41869f
        m[12] = -0.08131f
    }

    /**
     * Set the matrix to convert from YUV to RGB
     */
    fun setYUV2RGB() {
        reset()
        val m = array
        // these coefficients match those in libjpeg
        m[2] = 1.402f
        m[5] = 1f
        m[6] = -0.34414f
        m[7] = -0.71414f
        m[10] = 1f
        m[11] = 1.772f
        m[12] = 0f
    }
}