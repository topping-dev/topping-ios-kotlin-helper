package dev.topping.ios.constraint.shared.graphics

import dev.topping.ios.constraint.core.motion.utils.Rect
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Matrix33
import kotlin.math.E
import kotlin.math.log

fun IRect.toTRect(): Rect {
    return Rect(left, top, right, bottom)
}

fun Rect.toIRect(): IRect {
    return IRect.makeLTRB(left, top, right, bottom)
}

fun org.jetbrains.skia.Rect.toTRect(): Rect {
    return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}

operator fun Matrix33.get(rowIndex: Int, colIndex: Int) : Float {
    if (rowIndex < 0 || colIndex < 0 || rowIndex >= 3 || colIndex >= 3) {
        throw IllegalArgumentException("Matrix.get: Index out of bound")
    } else {
        return mat[rowIndex * 3 + colIndex]
    }
}

operator fun Matrix33.set(rowIndex: Int, colIndex: Int, value: Number) {
    if (rowIndex < 0 || colIndex < 0 || rowIndex >= 3 || colIndex >= 3) {
        throw IllegalArgumentException("Matrix.set: Index out of bound")
    } else {
        mat[rowIndex * 3 + colIndex] = value.toFloat()
    }
}

fun Matrix33.copy(): Matrix33 {
    return Matrix33(*mat.copyOf())
}

class AndroidMatrix33(val data: FloatArray = FloatArray(9) { 0f })

fun AndroidMatrix33.toMatrix() : Matrix33 {
    return Matrix33(*data)
}

fun Matrix33.isIdentity() : Boolean {
    val idM = Matrix33.IDENTITY
    return this.mat contentEquals idM.mat
}

fun Matrix33.inverse() : Matrix33 {
    val newMatrix = Matrix33.IDENTITY

    var determinant = determinant()

    var matrix = this

    newMatrix[0,0] = ((matrix[1,1]*matrix[2,2]) - (matrix[1,2]*matrix[2,1]));
    newMatrix[0,1] = -((matrix[0,1]*matrix[2,2]) - (matrix[0,2]*matrix[2,1]));
    newMatrix[0,2] = ((matrix[0,1]*matrix[1,2]) - (matrix[0,2]*matrix[1,1]));

    newMatrix[1,0] = -((matrix[1,0]*matrix[2,2]) - (matrix[1,2]*matrix[2,0]));
    newMatrix[1,1] = ((matrix[0,0]*matrix[2,2]) - (matrix[0,2]*matrix[2,0]));
    newMatrix[1,2] = -((matrix[0,0]*matrix[1,2]) - (matrix[0,2]*matrix[1,0]));

    newMatrix[2,0] = ((matrix[1,0]*matrix[2,1]) - (matrix[1,1]*matrix[2,0]));
    newMatrix[2,1] = -((matrix[0,0]*matrix[2,1]) - (matrix[0,1]*matrix[2,0]));
    newMatrix[2,2] = ((matrix[0,0]*matrix[1,1]) - (matrix[0,1]*matrix[1,0]));

    for (i in 0..2) {
        for (j in 0..2) {
            newMatrix[i,j] *= 1 / determinant
            if (newMatrix[i,j] == 0f) newMatrix[i,j] = 0f //to fix -0.0 showing in output
        }
    }

    return newMatrix
}

fun Matrix33.determinant(): Float {
    var determinant = 0f
    determinant += this[0,0] * (this[1,1] * this[2,2] - this[1,2] * this[2,1])
    determinant -= this[0,1] * (this[1,0] * this[2,2] - this[1,2] * this[2,0])
    determinant += this[0,2] * (this[1,0] * this[2,1] - this[1,1] * this[2,0])
    return determinant
}

fun Matrix33.reset() {
    var count = 0
    Matrix33.IDENTITY.mat.forEach {
        this.mat[count++] = it
    }
}

/** SkMatrix organizes its values in row-major order. These members correspond to
each value in SkMatrix.
 */
var kMScaleX = 0 //!< horizontal scale factor
var kMSkewX  = 1 //!< horizontal skew factor
var kMTransX = 2 //!< horizontal translation
var kMSkewY  = 3 //!< vertical skew factor
var kMScaleY = 4 //!< vertical scale factor
var kMTransY = 5 //!< vertical translation
var kMPersp0 = 6 //!< input x perspective factor
var kMPersp1 = 7 //!< input y perspective factor
var kMPersp2 = 8 //!< perspective bias

/** Affine arrays are in column-major order to match the matrix used by
PDF and XPS.
 */
var kAScaleX = 0 //!< horizontal scale factor
var kASkewY  = 1 //!< vertical skew factor
var kASkewX  = 2 //!< horizontal skew factor
var kAScaleY = 3 //!< vertical scale factor
var kATransX = 4 //!< horizontal translation
var kATransY = 5 //!< vertical translation

fun Matrix33.get(index: Int): Float {
    return mat[index]
}

fun Matrix33.set(index: Int, value: Float): Matrix33 {
    mat[index] = value
    return this
}

fun Matrix33.rc(r: Int, c: Int): Float {
    return mat[r*3 + c]
}

/** Returns scale factor multiplied by x-axis input, contributing to x-axis output.
With mapPoints(), scales SkPoint along the x-axis.

@return  horizontal scale factor
 */
fun Matrix33.getScaleX(): Float { return mat[kMScaleX] }

/** Returns scale factor multiplied by y-axis input, contributing to y-axis output.
With mapPoints(), scales SkPoint along the y-axis.

@return  vertical scale factor
 */
fun Matrix33.getScaleY(): Float { return mat[kMScaleY] }

/** Returns scale factor multiplied by x-axis input, contributing to y-axis output.
With mapPoints(), skews SkPoint along the y-axis.
Skewing both axes can rotate SkPoint.

@return  vertical skew factor
 */
fun Matrix33.getSkewY(): Float { return mat[kMSkewY] }

/** Returns scale factor multiplied by y-axis input, contributing to x-axis output.
With mapPoints(), skews SkPoint along the x-axis.
Skewing both axes can rotate SkPoint.

@return  horizontal scale factor
 */
fun Matrix33.getSkewX(): Float { return mat[kMSkewX] }

/** Returns translation contributing to x-axis output.
With mapPoints(), moves SkPoint along the x-axis.

@return  horizontal translation factor
 */
fun Matrix33.getTranslateX(): Float { return mat[kMTransX] }

/** Returns translation contributing to y-axis output.
With mapPoints(), moves SkPoint along the y-axis.

@return  vertical translation factor
 */
fun Matrix33.getTranslateY(): Float { return mat[kMTransY] }

/** Returns factor scaling input x-axis relative to input y-axis.

@return  input x-axis perspective factor
 */
fun Matrix33.getPerspX(): Float { return mat[kMPersp0] }

/** Returns factor scaling input y-axis relative to input x-axis.

@return  input y-axis perspective factor
 */
fun Matrix33.getPerspY(): Float { return mat[kMPersp1] }

/** Sets horizontal scale factor.

@param v  horizontal scale factor to store
 */
fun Matrix33.setScaleX(v: Float): Matrix33 { return this.set(kMScaleX, v) }

/** Sets vertical scale factor.

@param v  vertical scale factor to store
 */
fun Matrix33.setScaleY(v: Float): Matrix33 { return this.set(kMScaleY, v) }

/** Sets vertical skew factor.

@param v  vertical skew factor to store
 */
fun Matrix33.setSkewY(v: Float): Matrix33 { return this.set(kMSkewY, v) }

/** Sets horizontal skew factor.

@param v  horizontal skew factor to store
 */
fun Matrix33.setSkewX(v: Float): Matrix33 { return this.set(kMSkewX, v) }

/** Sets horizontal translation.

@param v  horizontal translation to store
 */
fun Matrix33.setTranslateX(v: Float): Matrix33 { return this.set(kMTransX, v) }

/** Sets vertical translation.

@param v  vertical translation to store
 */
fun Matrix33.setTranslateY(v: Float): Matrix33 { return this.set(kMTransY, v) }

/** Sets input x-axis perspective factor, which causes mapXY() to vary input x-axis values
inversely proportional to input y-axis values.

@param v  perspective factor
 */
fun Matrix33.setPerspX(v: Float): Matrix33 { return this.set(kMPersp0, v) }

/** Sets input y-axis perspective factor, which causes mapXY() to vary input y-axis values
inversely proportional to input x-axis values.

@param v  perspective factor
 */
fun Matrix33.setPerspY(v: Float): Matrix33 { return this.set(kMPersp1, v) }

/** Sets all values from parameters. Sets matrix to:

| scaleX  skewX transX |
|  skewY scaleY transY |
| persp0 persp1 persp2 |

@param scaleX  horizontal scale factor to store
@param skewX   horizontal skew factor to store
@param transX  horizontal translation to store
@param skewY   vertical skew factor to store
@param scaleY  vertical scale factor to store
@param transY  vertical translation to store
@param persp0  input x-axis values perspective factor to store
@param persp1  input y-axis values perspective factor to store
@param persp2  perspective scale factor to store
 */
fun Matrix33.setAll(scaleX: Float, skewX: Float,  transX: Float,
skewY: Float,  scaleY: Float, transY: Float,
persp0: Float, persp1: Float, persp2: Float): Matrix33 {
    mat[kMScaleX] = scaleX
    mat[kMSkewX]  = skewX
    mat[kMTransX] = transX
    mat[kMSkewY]  = skewY
    mat[kMScaleY] = scaleY
    mat[kMTransY] = transY
    mat[kMPersp0] = persp0
    mat[kMPersp1] = persp1
    mat[kMPersp2] = persp2
    return this;
}

fun Matrix33.setScaleTranslate(sx: Float, sy: Float, tx: Float, ty: Float) {
    mat[kMScaleX] = sx
    mat[kMSkewX]  = 0f
    mat[kMTransX] = tx
    mat[kMSkewY]  = 0f
    mat[kMScaleY] = sy
    mat[kMTransY] = ty

    mat[kMPersp0] = 0f
    mat[kMPersp1] = 0f
    mat[kMPersp2] = 1f
}

fun Matrix33.setScale(sx: Float, sy: Float, px: Float, py: Float): Matrix33 {
    if (1f == sx && 1f == sy) {
        this.reset()
    } else {
        this.setScaleTranslate(sx, sy, px - sx * px, py - sy * py)
    }
    return this
}

fun Matrix33.setScale(sx: Float, sy: Float): Matrix33 {
    return Matrix33.makeScale(sx, sy)
}

fun Matrix33.postScale(sx: Float, sy: Float) : Matrix33 {
    if(sx == 1f && sy == 1f)
        return this

    val m = Matrix33.makeScale(sx, sy)
    return m.makeConcat(this)
}

fun Matrix33.postScale(sx: Float, sy: Float, px: Float, py: Float) : Matrix33 {
    if(sx == 1f && sy == 1f)
        return this

    val m = Matrix33.IDENTITY
    m.setScale(sx, sy, px, py)
    return m.makeConcat(this)
}

fun Matrix33.postConcat(mat: Matrix33): Matrix33 {
    if (!mat.isIdentity())
        return mat.makeConcat(this)

    return this
}

fun Matrix33.postTranslate(dx: Float, dy: Float): Matrix33 {
    mat[kMTransX] += dx
    mat[kMTransY] += dy
    return this
}

fun Matrix33.postRotate(degrees: Float, px: Float, py: Float): Matrix33 {
    val m = Matrix33.makeRotate(degrees, px, py)
    return this.postConcat(m)
}

fun Matrix33.postRotate(degrees: Float): Matrix33 {
    val m = Matrix33.makeRotate(degrees)
    return this.postConcat(m)
}

fun log(value: Double) : Double {
    return log(value, E)
}