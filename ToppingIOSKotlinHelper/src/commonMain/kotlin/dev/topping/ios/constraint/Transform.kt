package dev.topping.ios.constraint

import dev.topping.ios.constraint.core.motion.utils.Rect
import dev.topping.ios.constraint.core.motion.utils.RectF
import dev.topping.ios.constraint.shared.graphics.copy
import dev.topping.ios.constraint.shared.graphics.get
import dev.topping.ios.constraint.shared.graphics.set
import org.jetbrains.skia.Matrix33
import kotlin.math.*

class vec2 {
    private var value = FloatArray(2)

    constructor()

    constructor(x: Float, y: Float) {
        value[0] = x
        value[1] = y
    }

    var x: Float = 0f
        get() = value[0]
    var y: Float = 0f
        get() = value[1]

    operator fun get(index: Int) : Float {
        if(index < 0 && index > 2)
            throw Exception()
        return value.get(index)
    }

    operator fun get(index: UInt) : Float {
        if(index < 0U && index > 2U)
            throw Exception()
        return value.get(index.toInt())
    }

    operator fun set(index: Int, value: Float) {
        if(index < 0 && index > 2)
            throw Exception()
        this.value.set(index, value)
    }

    operator fun set(index: UInt, value: Float) {
        if(index < 0U && index > 2U)
            throw Exception()
        this.value.set(index.toInt(), value)
    }

    operator fun times(scalar: Number) = vec2(x * scalar.toFloat(), y * scalar.toFloat())
    operator fun div(scalar: Number) = vec2(x / scalar.toFloat(), y / scalar.toFloat())
    operator fun plus(vec: vec2) = vec2(x + vec.x, y + vec.y)
    operator fun minus(vec: vec2) = vec2(x - vec.x, y - vec.y)
}

class vec3 {
    private var value = FloatArray(3)

    constructor()

    constructor(x: Float, y: Float, z: Float) {
        value[0] = x
        value[1] = y
        value[2] = z
    }

    operator fun get(index: Int) : Float {
        if(index < 0 && index > 3)
            throw Exception()
        return value.get(index)
    }

    operator fun set(index: Int, value: Float) {
        if(index < 0 && index > 3)
            throw Exception()
        this.value.set(index, value)
    }
}

class Transform {

    enum class RotationFlags(val value: Int) {
        ROT_0(0),
        FLIP_H(1), // HAL_TRANSFORM_FLIP_H
        FLIP_V(2), // HAL_TRANSFORM_FLIP_V
        ROT_90(4), // HAL_TRANSFORM_ROT_90
        ROT_180(FLIP_H.value or FLIP_V.value),
        ROT_270(ROT_180.value or ROT_90.value),
        ROT_INVALID(0x80)
    }

    enum class type_mask(val value: Int) {
        IDENTITY            (0),
        TRANSLATE           (0x1),
        ROTATE              (0x2),
        SCALE               (0x4),
        UNKNOWN             (0x8)
    }

    var mMatrix = Matrix33.IDENTITY
    var mType = 0

    companion object {
        const val EPSILON = Float.MIN_VALUE
        const val UNKNOWN_TYPE = 0x80000000
    }

    constructor() {
        reset()
    }

    constructor(transform: Transform) {
        this.mMatrix = transform.mMatrix.copy()
        this.mType = transform.mType
    }

    constructor(orientation: Int, w: Float = 0f, h: Float = 0f) {
        set(orientation, w, h)
    }

    fun reset() {
        mType = type_mask.IDENTITY.value
        mMatrix = Matrix33.IDENTITY
    }

    fun set(tx: Float, ty: Float) {
        mMatrix[2,0] = tx
        mMatrix[2,1] = ty
        mMatrix[2,2] = 1.0f
        if (isZero(tx) && isZero(ty)) {
            mType = mType and type_mask.TRANSLATE.value.inv()
        } else {
            mType = mType or type_mask.TRANSLATE.value.inv()
        }
    }

    fun set(flags: Int, w: Float, h: Float): Int {
        var flags = flags
        var w = w
        var h = h
        if ((flags and RotationFlags.ROT_INVALID.value) > 0) {
            // that's not allowed!
            reset()
            return -1;
        }
        var H = Transform()
        var V = Transform()
        var R = Transform()

        if ((flags and RotationFlags.ROT_90.value) > 0) {
            // w & h are inverted when rotating by 90 degrees
            w = h.also { h = w }
        }
        if ((flags and RotationFlags.FLIP_H.value) > 0) {
            H.mType = (RotationFlags.FLIP_H.value shl 8) or type_mask.SCALE.value
            H.mType = H.mType or if(isZero(w)) type_mask.IDENTITY.value else type_mask.TRANSLATE.value
            H.mMatrix[0, 0] = -1
            H.mMatrix[2, 0] = w
        }
        if ((flags and RotationFlags.FLIP_V.value) > 0) {
            V.mType = (RotationFlags.FLIP_V.value shl 8) or type_mask.SCALE.value
            V.mType = V.mType or if(isZero(h)) type_mask.IDENTITY.value else type_mask.TRANSLATE.value
            V.mMatrix[1,1] = -1
            V.mMatrix[2,1] = h
        }
        if ((flags and RotationFlags.ROT_90.value) > 0) {
            val original_w = h
            R.mType = (RotationFlags.ROT_90.value shl 8) or type_mask.ROTATE.value
            R.mType = R.mType or if(isZero(original_w)) type_mask.IDENTITY.value else type_mask.TRANSLATE.value
            R.mMatrix[0,0] = 0
            R.mMatrix[1,0] =-1
            R.mMatrix[2,0] = original_w
            R.mMatrix[0,1] = 1
            R.mMatrix[1,1] = 0
        }
        var t = (R*(H*V))
        mMatrix = t.mMatrix
        mType = t.mType
        return 0
    }

    fun set(matrix: FloatArray) {
        val M = mMatrix
        M[0,0] = matrix[0]
        M[1,0] = matrix[1]
        M[2,0] = matrix[2]
        M[0,1] = matrix[3]
        M[1,1] = matrix[4]
        M[2,1] = matrix[5]
        M[0,2] = matrix[6]
        M[1,2] = matrix[7]
        M[2,2] = matrix[8]
        mType = UNKNOWN_TYPE.toInt()
        type()
    }

    fun tx(): Float {
        return mMatrix[2,0].toFloat()
    }
    fun ty(): Float {
        return mMatrix[2,1].toFloat()
    }
    fun dsdx(): Float {
        return mMatrix[0,0].toFloat()
    }
    fun dtdx(): Float {
        return mMatrix[1,0].toFloat()
    }
    fun dtdy(): Float {
        return mMatrix[0,1].toFloat()
    }
    fun dsdy(): Float {
        return mMatrix[1,1].toFloat()
    }
    fun det(): Float {
        return (mMatrix[0,0] * mMatrix[1,1] - mMatrix[0,1] * mMatrix[1,0]).toFloat()
    }
    fun getScaleX(): Float {
        return sqrt((dsdx() * dsdx()) + (dtdx() * dtdx())).toFloat()
    }
    fun getScaleY(): Float {
        return sqrt((dtdy() * dtdy()) + (dsdy() * dsdy())).toFloat()
    }

    fun transform(v: vec2) : vec2 {
        var r = vec2()
        var M = mMatrix
        r[0] = (M[0,0]*v[0] + M[1,0]*v[1] + M[2,0]).toFloat()
        r[1] = (M[0,1]*v[0] + M[1,1]*v[1] + M[2,1]).toFloat()
        return r
    }

    fun transform(v: vec3) : vec3 {
        var r = vec3()
        var M = mMatrix
        r[0] = (M[0,0]*v[0] + M[1,0]*v[1] + M[2,0]*v[2]).toFloat()
        r[1] = (M[0,1]*v[0] + M[1,1]*v[1] + M[2,1]*v[2]).toFloat()
        r[2] = (M[0,2]*v[0] + M[1,2]*v[1] + M[2,2]*v[2]).toFloat()
        return r
    }

    fun transform(x: Float, y: Float) : vec2 {
        return transform(vec2(x, y))
    }

    fun transform(bounds: Rect, roundOutwards: Boolean): Rect {
        var r = Rect()
        var lt = vec2(bounds.left.toFloat(),  bounds.top.toFloat()    )
        var rt = vec2( bounds.right.toFloat(), bounds.top.toFloat()    )
        var lb = vec2( bounds.left.toFloat(),  bounds.bottom.toFloat() )
        var rb = vec2( bounds.right.toFloat(), bounds.bottom.toFloat() )
        lt = transform(lt);
        rt = transform(rt);
        lb = transform(lb);
        rb = transform(rb);
        if (roundOutwards) {
            r.left   = floor(minOf(lt[0], rt[0], lb[0], rb[0])).toInt()
            r.top    = floor(minOf(lt[1], rt[1], lb[1], rb[1])).toInt()
            r.right  = ceil(maxOf(lt[0], rt[0], lb[0], rb[0])).toInt()
            r.bottom = ceil(maxOf(lt[1], rt[1], lb[1], rb[1])).toInt()
        } else {
            r.left   = floor(minOf(lt[0], rt[0], lb[0], rb[0]) + 0.5f).toInt()
            r.top    = floor(minOf(lt[1], rt[1], lb[1], rb[1]) + 0.5f).toInt()
            r.right  = floor(maxOf(lt[0], rt[0], lb[0], rb[0]) + 0.5f).toInt()
            r.bottom = floor(maxOf(lt[1], rt[1], lb[1], rb[1]) + 0.5f).toInt()
        }
        return r
    }

    fun transform(bounds: RectF, roundOutwards: Boolean): RectF {
        var r = RectF()
        var lt = vec2(bounds.left,  bounds.top    )
        var rt = vec2( bounds.right, bounds.top    )
        var lb = vec2( bounds.left,  bounds.bottom )
        var rb = vec2( bounds.right, bounds.bottom )
        lt = transform(lt);
        rt = transform(rt);
        lb = transform(lb);
        rb = transform(rb);
        if (roundOutwards) {
            r.left   = floor(minOf(lt[0], rt[0], lb[0], rb[0]))
            r.top    = floor(minOf(lt[1], rt[1], lb[1], rb[1]))
            r.right  = ceil(maxOf(lt[0], rt[0], lb[0], rb[0]))
            r.bottom = ceil(maxOf(lt[1], rt[1], lb[1], rb[1]))
        } else {
            r.left   = floor(minOf(lt[0], rt[0], lb[0], rb[0]) + 0.5f)
            r.top    = floor(minOf(lt[1], rt[1], lb[1], rb[1]) + 0.5f)
            r.right  = floor(maxOf(lt[0], rt[0], lb[0], rb[0]) + 0.5f)
            r.bottom = floor(maxOf(lt[1], rt[1], lb[1], rb[1]) + 0.5f)
        }
        return r
    }

    fun type(): Int {
        if ((mType and UNKNOWN_TYPE.toInt()) > 0) {
            // recompute what this transform is
            var M = mMatrix
            var a = M[0,0]
            var b = M[1,0]
            var c = M[0,1]
            var d = M[1,1]
            var x = M[2,0]
            var y = M[2,1]
            var scale = false
            var flags = RotationFlags.ROT_0.value
            if (isZero(b) && isZero(c)) {
                if (a<0)    flags = flags or RotationFlags.FLIP_H.value
                if (d<0)    flags = flags or RotationFlags.FLIP_V.value
                if (!absIsOne(a.toFloat()) || !absIsOne(d)) {
                    scale = true;
                }
            } else if (isZero(a) && isZero(d)) {
                flags = flags or RotationFlags.ROT_90.value
                if (b>0)    flags = flags or RotationFlags.FLIP_V.value
                if (c<0)    flags = flags or RotationFlags.FLIP_H.value
                if (!absIsOne(b) || !absIsOne(c)) {
                    scale = true;
                }
            } else {
                // there is a skew component and/or a non 90 degrees rotation
                flags = RotationFlags.ROT_INVALID.value
            }
            mType = flags shl 8;
            if ((flags and RotationFlags.ROT_INVALID.value) > 0) {
            mType = mType or type_mask.UNKNOWN.value
        } else {
            if (((flags and RotationFlags.ROT_90.value) > 0) || ((flags and RotationFlags.ROT_180.value) == RotationFlags.ROT_180.value))
            mType = mType or type_mask.ROTATE.value
            if ((flags and RotationFlags.FLIP_H.value) > 0)
            mType = mType xor type_mask.SCALE.value
            if ((flags and RotationFlags.FLIP_V.value) > 0)
            mType = mType xor type_mask.SCALE.value
            if (scale)
                mType = mType or type_mask.SCALE.value
        }
            if (!isZero(x) || !isZero(y))
                mType = mType or type_mask.TRANSLATE.value
        }
        return mType
    }

    fun inverse(): Transform {
        // our 3x3 matrix is always of the form of a 2x2 transformation
        // followed by a translation: T*M, therefore:
        // (T*M)^-1 = M^-1 * T^-1
        var result = Transform()
        if (mType <= type_mask.TRANSLATE.value) {
            // 1 0 0
            // 0 1 0
            // x y 1
            result = Transform(this)
            result.mMatrix[2,0] = -result.mMatrix[2,0];
            result.mMatrix[2,1] = -result.mMatrix[2,1];
        } else {
            // a c 0
            // b d 0
            // x y 1
            var M = mMatrix
            var a = M[0,0];
            var b = M[1,0];
            var c = M[0,1];
            var d = M[1,1];
            var x = M[2,0];
            var y = M[2,1];
            var idet = 1.0f / det();
            result.mMatrix[0,0] =  d*idet;
            result.mMatrix[0,1] = -c*idet;
            result.mMatrix[1,0] = -b*idet;
            result.mMatrix[1,1] =  a*idet;
            result.mType = mType;
            if ((getOrientation() and RotationFlags.ROT_90.value) > 0) {
                // Recalculate the type if there is a 90-degree rotation component, since the inverse
                // of ROT_90 is ROT_270 and vice versa.
                result.mType = result.mType or UNKNOWN_TYPE.toInt()
            }
            var T = vec2(-x.toFloat(), -y.toFloat())
            T = result.transform(T)
            result.mMatrix[2,0] = T[0]
            result.mMatrix[2,1] = T[1]
        }
        return result
    }

    fun getType(): Int {
        return type() and 0xFF
    }
    fun getOrientation(): Int {
        return (type() shr 8) and 0xFF
    }

    fun isZero(f: Float): Boolean {
        return abs(f) < EPSILON
    }

    fun isZero(d: Double): Boolean {
        return abs(d) < EPSILON
    }

    operator fun times(rhs: Transform) : Transform {
        if(mType == type_mask.IDENTITY.value)
            return rhs

        var r = Transform(this)
        if(rhs.mType == type_mask.IDENTITY.value)
            return this

        var A = mMatrix
        var B = rhs.mMatrix
        for(i in 0 until 3) {
            val v0 = A[0,i]
            val v1 = A[1,i]
            val v2 = A[2,i]
            r.mMatrix[0,i] = v0*B[0,0].toFloat() + v1*B[0,1] + v2*B[0,2]
            r.mMatrix[1,i] = v0*B[1,0].toFloat() + v1*B[1,1] + v2*B[1,2]
            r.mMatrix[2,i] = v0*B[2,0].toFloat() + v1*B[2,1] + v2*B[2,2]
        }
        r.mType = r.mType or rhs.mType

        // TODO: we could recompute this value from r and rhs
        r.mType = r.mType and 0xFF
        r.mType = r.mType or UNKNOWN_TYPE.toInt()
        return r
    }

    fun absIsOne(f: Float): Boolean {
        return isZero(kotlin.math.abs(f) - 1.0f)
    }

    fun absIsOne(d: Double): Boolean {
        return isZero(abs(d) - 1.0)
    }
}
