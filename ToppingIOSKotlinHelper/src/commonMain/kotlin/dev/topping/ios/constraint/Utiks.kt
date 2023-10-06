package dev.topping.ios.constraint

import dev.topping.ios.constraint.core.state.Interpolator
import dev.topping.ios.constraint.core.state.interpolators.*
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.localPart
import kotlin.math.pow


class Utiks {

}

class WhenException : Exception()

class AnimationUtils {
    companion object {
        fun loadInterpolator(context: TContext, id: String): Interpolator? {
            var parser: XmlBufferedReader? = null
            return try {
                parser = context.getResources().getAnimation(id)
                createInterpolatorFromXml(context.getResources(), parser!!)
            } catch (ex: Exception) {
                null
            } finally {
                parser?.close()
            }
        }

        private fun createInterpolatorFromXml(
            res: TResources,
            parser: XmlBufferedReader
        ): Interpolator? {
            var interpolator: Interpolator? = null
            // Make sure we are on a start tag.
            var type: EventType
            val depth: Int = parser.depth
            while ((parser.next()
                    .also { type = it } != EventType.END_ELEMENT || parser.depth > depth)
                && type != EventType.END_DOCUMENT
            ) {
                if (type != EventType.START_ELEMENT) {
                    continue
                }
                val attrs = Xml.asAttributeSet(parser)
                val name: String = parser.name.localPart
                if (name == "linearInterpolator") {
                    interpolator = LinearInterpolator()
                } else if (name == "accelerateInterpolator") {
                    interpolator = AccelerateInterpolator(res, attrs)
                } else if (name == "decelerateInterpolator") {
                    interpolator = DecelerateInterpolator(res, attrs)
                } else if (name == "accelerateDecelerateInterpolator") {
                    interpolator = AccelerateDecelerateInterpolator()
                } else if (name == "cycleInterpolator") {
                    interpolator = CycleInterpolator(res, attrs)
                } else if (name == "anticipateInterpolator") {
                    interpolator = AnticipateInterpolator(res, attrs)
                } else if (name == "overshootInterpolator") {
                    interpolator = OvershootInterpolator(res, attrs)
                } else if (name == "anticipateOvershootInterpolator") {
                    interpolator = AnticipateOvershootInterpolator(res, attrs)
                } else if (name == "bounceInterpolator") {
                    interpolator = BounceInterpolator()
                } /*else if (name == "pathInterpolator") {
                    interpolator = PathInterpolator(res, attrs)
                }*/ else {
                    throw Exception("Unknown interpolator name: " + parser.name.localPart)
                }
            }
            return interpolator
        }
    }
}

class Pool<T> {
    private var list: MutableList<T?>
    private var current = 0
    private val poolMax: Int
    constructor() {
        poolMax = 5
        list = MutableList(poolMax) { null }
    }
    constructor(max: Int) {
        poolMax = max
        list = MutableList(poolMax) { null }
    }

    fun acquire(): T? {
        val v = list[current]
        current++
        if(current >= poolMax)
            current = 0
        return v
    }

    fun clear() {
        current = 0
        for(i in 0 until list.size)
        {
            list[i] = null
        }
    }

    fun release(t: T) {
        list[list.indexOf(t)] = null
    }
}

class Bundle {
    val storage = mutableMapOf<String, Any>()

    fun getFloat(key: String) : Float {
        return storage[key]!! as Float
    }

    fun putFloat(key: String, obj: Float) {
        storage[key] = obj
    }

    fun getInt(key: String) : Int {
        return storage[key]!! as Int
    }

    fun putInt(key: String, obj: Int) {
        storage[key] = obj
    }

    fun getString(key: String) : String {
        return storage[key]!! as String
    }

    fun putString(key: String, obj: String) {
        storage[key] = obj
    }
}

inline fun <reified K, V> Map<K, V>.keyAt(i: Int): K {
    return this.keys.toTypedArray()[i]
}

inline fun <reified K, V> Map<K, V>.indexOfKey(i: K): Int {
    return this.keys.indexOf(i)
}

inline fun <reified K, V> Map<K, V>.valueAt(i: Int): V? {
    return this[keyAt(i)]
}

inline fun <reified K, V> MutableMap<K, V>.removeAt(i: Int): V? {
    return this.remove(this[keyAt(i)] as K)
}

class Arrays {
    companion object {
        fun <T> copyOfN(array: Array<T>, size: Int) : Array<T?> {
            return array.copyOf(size)
        }

        fun <T> copyOf(array: Array<T?>, size: Int) : Array<T?> {
            return array.copyOf(size)
        }

        fun <T> arraycopy(sourceArr: Array<T>, sourcePos: Int, destArr: Array<T>, destPos: Int, size: Int) : Array<T> {
            var list = mutableListOf<T>()

            var count = 0
            for(i in 0 until destPos) {
                list.add(destArr[i])
                count++
            }

            for(i in sourcePos .. size) {
                list.add(sourceArr[i])
                count++
            }

            for(i in count until destArr.size) {
                list.add(destArr[i])
            }

            for(i in 0 until destArr.size) {
                destArr[i] = list[i]
            }

            return destArr
        }

        fun arraycopy(sourceArr: FloatArray, sourcePos: Int, destArr: FloatArray, destPos: Int, size: Int) : FloatArray {
            var list = mutableListOf<Float>()

            var count = 0
            for(i in 0 until destPos) {
                list.add(destArr[i])
                count++
            }

            for(i in sourcePos .. size) {
                list.add(sourceArr[i])
                count++
            }

            for(i in count until destArr.size) {
                list.add(destArr[i])
            }

            for(i in 0 until destArr.size) {
                destArr[i] = list[i]
            }

            return destArr
        }

        inline fun <reified T> copyOfNonNull(array: Array<T>, size: Int) : Array<T> {
            val res = array.copyOf(size)
            val ret : MutableList<T> = mutableListOf()
            res.forEach {
                ret.add(it!!)
            }
            return ret.toTypedArray()
        }

        fun copyOf(array: IntArray, size: Int) : IntArray {
            return array.copyOf(size)
        }

        fun copyOf(array: FloatArray, size: Int) : FloatArray {
            return array.copyOf(size)
        }

        fun copyOf(array: DoubleArray, size: Int) : DoubleArray {
            return array.copyOf(size)
        }

        fun copyOf(array: BooleanArray, size: Int) : BooleanArray {
            return array.copyOf(size)
        }

        fun <T> fill(array: Array<T?>, value: T) : Array<T?> {
            array.fill(value)
            return array
        }

        fun <T> fillN(array: Array<T?>, value: T?) : Array<T?> {
            array.fill(value)
            return array
        }

        fun fill(array: IntArray, value: Int) : IntArray {
            array.fill(value)
            return array
        }

        fun fill(array: FloatArray, value: Float) : FloatArray {
            array.fill(value)
            return array
        }

        fun fill(array: DoubleArray, value: Double) : DoubleArray {
            array.fill(value)
            return array
        }

        fun fill(array: BooleanArray, value: Boolean) : BooleanArray {
            array.fill(value)
            return array
        }

        fun sort(array: IntArray) : IntArray {
            array.sort()
            return array
        }

        fun toString(array: Any?) : String {
            return array.toString()
        }

        fun binarySearch(array: DoubleArray, value: Double): Int {
            return array.indexOfFirst {
                it == value
            }
        }

        fun <T> binarySearch(array: MutableList<T>, value: T): Int {
            return array.indexOfFirst {
                it == value
            }
        }
    }
}

fun Float.Companion.isNaN(value: Float) : Boolean {
    return value.isNaN()
}

fun Float.Companion.isNaN(value: Float?) : Boolean {
    if(value == null)
        return true
    return value.isNaN()
}

fun Double.Companion.isNaN(value: Float) : Boolean {
    return value.isNaN()
}

fun Double.Companion.isNaN(value: Double) : Boolean {
    return value.isNaN()
}

fun Double.Companion.parseDouble(value: String) : Double {
    return value.toDouble()
}

fun toRadians(a: Double) : Double {
    return (a * (kotlin.math.PI / 180.0))
}

fun toRadians(a: Float) : Float {
    return ((a * (kotlin.math.PI / 180.0)).toFloat())
}

fun toDegrees(a: Double) : Double {
    return (a * (180.0 / kotlin.math.PI))
}

fun toDegrees(a: Float) : Float {
    return ((a * (180.0 / kotlin.math.PI)).toFloat())
}

fun signum(a: Double) : Double {
    return if(a > 0.0) 1.0
    else if(a < 0.0) -1.0
    else 0.0
}

fun signum(a: Float) : Float {
    return signum(a.toDouble()).toFloat()
}

fun pow(a: Float, b: Float): Float {
    return a.pow(b)
}

fun pow(a: Float, b: Double): Float {
    return a.pow(b.toFloat())
}

fun pow(a: Double, b: Double): Double {
    return a.pow(b)
}

fun Int.Companion.toHexString(value: Int): String {
    return value.toUInt().toString(16)
}

inline fun objectsHashAll(vararg vals: Any?): Int {
    var res = 0
    for (v in vals) {
        res += v.hashCode()
        res *= 31
    }
    return res
}

inline fun objectsEquals(a: Any?, b: Any?): Boolean {
    return (a == b) || (a != null && a.equals(b));
}