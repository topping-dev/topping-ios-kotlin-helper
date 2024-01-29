package dev.topping.ios.constraint.core.motion.parse

import dev.topping.ios.constraint.Arrays
import dev.topping.ios.constraint.core.motion.utils.TypedBundle
import dev.topping.ios.constraint.core.motion.utils.TypedValues
import dev.topping.ios.constraint.core.parser.*
import kotlin.experimental.ExperimentalNativeApi

class KeyParser {
    companion object {
        @OptIn(ExperimentalNativeApi::class)
        private fun parse(str: String, table: Ids, dtype: DataType): TypedBundle? {
            val bundle = TypedBundle()
            try {
                val parsedContent = CLParser.parse(str)
                val n = parsedContent.size()
                for (i in 0 until n) {
                    val clkey = parsedContent[i] as CLKey?
                    val type = clkey!!.content()
                    val value = clkey.value
                    val id = table[type]
                    if (id == -1) {
                        println("unknown type $type")
                        continue
                    }
                    when (dtype[id]) {
                        TypedValues.FLOAT_MASK -> {
                            bundle.add(id, value!!.float)
                            println("parse " + type + " FLOAT_MASK > " + value.float)
                        }
                        TypedValues.STRING_MASK -> {
                            bundle.add(id, value!!.content())
                            println("parse " + type + " STRING_MASK > " + value.content())
                        }
                        TypedValues.INT_MASK -> {
                            bundle.add(id, value!!.int)
                            println("parse " + type + " INT_MASK > " + value.int)
                        }
                        TypedValues.BOOLEAN_MASK -> bundle.add(id, parsedContent.getBoolean(i))
                    }
                }
            } catch (e: CLParsingException) {
                //TODO replace with something not equal to printStackTrace();
                println(
                    """
            $e
            
            """.trimIndent() + Arrays.toString(e.getStackTrace())
                        .replace("[", "   at ")
                        .replace(",", "\n   at")
                        .replace("]", "")
                )
            }
            return bundle
        }

       /* fun parseAttributes(str: String) : TypedValues {
            //return parse(str, TypedValues.AttributesType.getId())
            return TypedValues.
        }*/
    }

    private interface Ids {
        operator fun get(str: String?): Int
    }

    private interface DataType {
        operator fun get(str: Int): Int
    }

    // @TODO: add description
    fun main(args: Array<String>) {
        val str = """
             {frame:22,
             target:'widget1',
             easing:'easeIn',
             curveFit:'spline',
             progress:0.3,
             alpha:0.2,
             elevation:0.7,
             rotationZ:23,
             rotationX:25.0,
             rotationY:27.0,
             pivotX:15,
             pivotY:17,
             pivotTarget:'32',
             pathRotate:23,
             scaleX:0.5,
             scaleY:0.7,
             translationX:5,
             translationY:7,
             translationZ:11,
             }
             """.trimIndent()
        //KeyParser.parseAttributes(str)
    }
}