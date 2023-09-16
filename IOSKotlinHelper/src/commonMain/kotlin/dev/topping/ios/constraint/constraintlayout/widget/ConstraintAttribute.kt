/*
 * Copyright (C) 2017 The Android Open Source Project
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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.*
import nl.adaptivity.xmlutil.XmlBufferedReader
import kotlin.collections.set

/**
 * Defines non standard Attributes
 *
 *
 */
class ConstraintAttribute {
    var isMethod = false
        private set
    var name: String
    var type: AttributeType
        private set
    var integerValue = 0
        private set
    var floatValue = 0f
    var stringValue: String? = null
    var isBooleanValue = false
    var colorValue = TColor.argb(255f, 255f, 255f, 255f)

    enum class AttributeType {
        INT_TYPE, FLOAT_TYPE, COLOR_TYPE, COLOR_DRAWABLE_TYPE, STRING_TYPE, BOOLEAN_TYPE, DIMENSION_TYPE, REFERENCE_TYPE
    }

    /**
     * Continuous types are interpolated they are fired only at
     * @return
     */
    val isContinuous: Boolean
        get() = when (type) {
            AttributeType.REFERENCE_TYPE, AttributeType.BOOLEAN_TYPE, AttributeType.STRING_TYPE -> false
            else -> true
        }

    fun setIntValue(value: Int) {
        integerValue = value
    }

    /**
     * The number of interpolation values that need to be interpolated
     * Typically 1 but 3 for colors.
     *
     * @return Typically 1 but 3 for colors.
     */
    fun numberOfInterpolatedValues(): Int {
        return when (type) {
            AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> 4
            else -> 1
        }
    }

    /**
     * Transforms value to a float for the purpose of interpolation
     *
     * @return interpolation value
     */
    val valueToInterpolate: Float
        get() {
            when (type) {
                AttributeType.INT_TYPE -> return integerValue.toFloat()
                AttributeType.FLOAT_TYPE, AttributeType.DIMENSION_TYPE -> return floatValue
                AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> throw RuntimeException(
                    "TColor does not have a single color to interpolate"
                )
                AttributeType.STRING_TYPE -> throw RuntimeException("Cannot interpolate String")
                AttributeType.BOOLEAN_TYPE -> return if (isBooleanValue) 1f else 0f
                AttributeType.REFERENCE_TYPE -> return Float.NaN
            }
            return Float.NaN
        }

    /**
     * populate the float array with colors it will fill 4 values
     * @param ret
     */
    fun getValuesToInterpolate(ret: FloatArray) {
        when (type) {
            AttributeType.INT_TYPE -> ret[0] = integerValue.toFloat()
            AttributeType.FLOAT_TYPE -> ret[0] = floatValue
            AttributeType.COLOR_DRAWABLE_TYPE, AttributeType.COLOR_TYPE -> {
                val a = Pointer(0)
                val r = Pointer(0)
                val g = Pointer(0)
                val b = Pointer(0)
                colorValue.getValues(r, g, b, a)
                val f_r = pow(r.v / 255f, 2.2).toFloat()
                val f_g = pow(g.v / 255f, 2.2).toFloat()
                val f_b = pow(b.v / 255f, 2.2).toFloat()
                ret[0] = f_r
                ret[1] = f_g
                ret[2] = f_b
                ret[3] = a.v.toFloat()
            }
            AttributeType.STRING_TYPE -> throw RuntimeException("TColor does not have a single color to interpolate")
            AttributeType.BOOLEAN_TYPE -> ret[0] = if (isBooleanValue) 1f else 0f
            AttributeType.DIMENSION_TYPE -> ret[0] = floatValue
            else -> if (I_DEBUG) {
                Log.v(TAG, type.toString())
            }
        }
    }

    /**
     * setValue based on the values in the array
     * @param value
     */
    fun setValue(value: FloatArray) {
        when (type) {
            AttributeType.REFERENCE_TYPE, AttributeType.INT_TYPE -> integerValue = value[0].toInt()
            AttributeType.FLOAT_TYPE -> floatValue = value[0]
            AttributeType.COLOR_DRAWABLE_TYPE, AttributeType.COLOR_TYPE -> {
                colorValue = TColor.HSVToColor(value)
                colorValue = TColor.fromInt((TColor.toInt(colorValue) and 0xFFFFFF) or (clamp((0xFF * value[3]).toInt()) shl 24))
            }
            AttributeType.STRING_TYPE -> throw RuntimeException("TColor does not have a single color to interpolate")
            AttributeType.BOOLEAN_TYPE -> isBooleanValue = value[0] > 0.5
            AttributeType.DIMENSION_TYPE -> floatValue = value[0]
            else -> if (I_DEBUG) {
                Log.v(TAG, type.toString())
            }
        }
    }

    /**
     * test if the two attributes are different
     *
     * @param constraintAttribute
     * @return
     */
    fun diff(constraintAttribute: ConstraintAttribute?): Boolean {
        if (constraintAttribute == null || type != constraintAttribute.type) {
            return false
        }
        when (type) {
            AttributeType.INT_TYPE, AttributeType.REFERENCE_TYPE -> return integerValue == constraintAttribute.integerValue
            AttributeType.FLOAT_TYPE -> return floatValue == constraintAttribute.floatValue
            AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> return colorValue == constraintAttribute.colorValue
            AttributeType.STRING_TYPE -> return integerValue == constraintAttribute.integerValue
            AttributeType.BOOLEAN_TYPE -> return isBooleanValue == constraintAttribute.isBooleanValue
            AttributeType.DIMENSION_TYPE -> return floatValue == constraintAttribute.floatValue
        }
        return false
    }

    constructor(name: String, attributeType: AttributeType) {
        this.name = name
        type = attributeType
    }

    constructor(
        name: String,
        attributeType: AttributeType,
        value: Any,
        method: Boolean
    ) {
        this.name = name
        type = attributeType
        isMethod = method
        setValue(value)
    }

    constructor(source: ConstraintAttribute?, value: Any) {
        name = source!!.name
        type = source.type
        setValue(value)
    }

    /**
     * set the value based on casting the object
     * @param value
     */
    fun setValue(value: Any) {
        when (type) {
            AttributeType.REFERENCE_TYPE, AttributeType.INT_TYPE -> integerValue = value as Int
            AttributeType.FLOAT_TYPE -> floatValue = value as Float
            AttributeType.COLOR_TYPE, AttributeType.COLOR_DRAWABLE_TYPE -> colorValue =
                value as TColor
            AttributeType.STRING_TYPE -> stringValue = value as String
            AttributeType.BOOLEAN_TYPE -> isBooleanValue = value as Boolean
            AttributeType.DIMENSION_TYPE -> floatValue = value as Float
        }
    }

    /**
     * Apply custom attributes to the view
     * @param view
     */
    fun applyCustom(view: TView) {
        val name = name
        var methodName = name
        if (!isMethod) {
            methodName = "set$methodName"
        }
        try {
            when (type) {
                AttributeType.INT_TYPE, AttributeType.REFERENCE_TYPE -> {
                    view.setObjCProperty(methodName, integerValue)
                }
                AttributeType.FLOAT_TYPE -> {
                    view.setObjCProperty(methodName, floatValue)
                }
                AttributeType.COLOR_DRAWABLE_TYPE -> {
                    //TODO
                    /*method = viewClass.getMethod(methodName, Drawable::class.java)
                    val drawable = ColorDrawable() // TODO cache
                    drawable.setColor(colorValue)
                    method.invoke(view, drawable)*/
                }
                AttributeType.COLOR_TYPE -> {
                    view.setObjCProperty(methodName, colorValue)
                }
                AttributeType.STRING_TYPE -> {
                    view.setObjCProperty(methodName, stringValue ?: "")
                }
                AttributeType.BOOLEAN_TYPE -> {
                    view.setObjCProperty(methodName, isBooleanValue)
                }
                AttributeType.DIMENSION_TYPE -> {
                    view.setObjCProperty(methodName, floatValue)
                }
            }
        } catch (e: Exception) {
            Log.e(
                TAG, " Custom Attribute \"" + name
                        + "\" not found on " + view.getParentType().toString(), e
            )
        }
    }

    companion object {
        private const val TAG = "TransitionLayout"
        private const val I_DEBUG = false

        /**
         * extract attributes from the view
         * @param base
         * @param view
         * @return
         */
        fun extractAttributes(
            base: MutableMap<String, ConstraintAttribute>, view: TView
        ): HashMap<String, ConstraintAttribute> {
            val ret: HashMap<String, ConstraintAttribute> = HashMap()
            for (name in base.keys) {
                val constraintAttribute = base[name]
                try {
                    //TODO:
                    /*if (name == "BackgroundColor") { // hack for getMap set background color
                        val viewColor: ColorDrawable = view.getBackground() as ColorDrawable
                        val `val`: Object = viewColor.getColor()
                        ret[name] = ConstraintAttribute(constraintAttribute, `val`)
                    } else {
                        val method: Method = viewClass.getMethod("getMap$name")
                        val `val`: Object = method.invoke(view)
                        ret[name] = ConstraintAttribute(constraintAttribute, `val`)
                    } */
                } catch (e: Exception) {
                    Log.e(
                        TAG, " Custom Attribute \"" + name
                                + "\" not found on " + view.getParentType().toString(), e
                    )
                }
            }
            return ret
        }

        /**
         * set attributes from map on to the view
         * @param view
         * @param map
         */
        fun setAttributes(view: TView, map: MutableMap<String, ConstraintAttribute>) {
            for (name in map.keys) {
                val constraintAttribute = map[name]
                var methodName = name
                if (!constraintAttribute!!.isMethod) {
                    methodName = "set$methodName"
                }
                try {
                    when (constraintAttribute.type) {
                        AttributeType.INT_TYPE -> {
                            view.setObjCProperty(methodName, constraintAttribute.integerValue)
                        }
                        AttributeType.FLOAT_TYPE -> {
                            view.setObjCProperty(methodName, constraintAttribute.floatValue)
                        }
                        AttributeType.COLOR_DRAWABLE_TYPE -> {
                            /*method = viewClass.getMethod(methodName, Drawable::class.java)
                            val drawable = ColorDrawable() // TODO cache
                            drawable.setColor(constraintAttribute.colorValue)
                            method.invoke(view, drawable)*/
                        }
                        AttributeType.COLOR_TYPE -> {
                            view.setObjCProperty(methodName, constraintAttribute.colorValue)
                        }
                        AttributeType.STRING_TYPE -> {
                            view.setObjCProperty(methodName, constraintAttribute.stringValue ?: "")
                        }
                        AttributeType.BOOLEAN_TYPE -> {
                            view.setObjCProperty(methodName, constraintAttribute.isBooleanValue)
                        }
                        AttributeType.DIMENSION_TYPE -> {
                            view.setObjCProperty(methodName, constraintAttribute.floatValue)
                        }
                        AttributeType.REFERENCE_TYPE -> {
                            view.setObjCProperty(methodName, constraintAttribute.integerValue)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG, " Custom Attribute \"" + name
                                + "\" not found on " + view.getParentType().toString(), e
                    )
                }
            }
        }

        private fun clamp(c: Int): Int {
            var c = c
            val n = 255
            c = c and (c shr 31).inv()
            c -= n
            c = c and (c shr 31)
            c += n
            return c
        }

        /**
         * parse Custom attributes and fill Custom
         * @param context
         * @param parser
         * @param custom
         */
        fun parse(
            context: TContext,
            parser: XmlBufferedReader,
            custom: MutableMap<String, ConstraintAttribute>?
        ) {
            val attributeSet = Xml.asAttributeSet(parser)
            var name: String? = null
            var method = false
            var value: Any? = null
            var type: AttributeType = AttributeType.STRING_TYPE
            attributeSet.forEach { kvp ->
                if(kvp.key == "attributeName") {
                    name = context.getResources().getString(kvp.key, kvp.value)
                    if (name != null && name!!.length > 0) {
                        name = name!![0].uppercase() + name!!.substring(1)
                    }
                } else if(kvp.key == "customBoolean") {

                }
                else if (kvp.key == "methodName") {
                    method = true
                    name = context.getResources().getString(kvp.key, kvp.value)
                } else if (kvp.key == "customBoolean") {
                    value = context.getResources().getBoolean(kvp.value, false)
                    type = AttributeType.BOOLEAN_TYPE
                } else if (kvp.key == "customColorValue") {
                    type = AttributeType.COLOR_TYPE
                    value = context.getResources().getColor(kvp.value, TColor.argb(255f, 255f, 255f, 255f))
                } else if (kvp.key == "customColorDrawableValue") {
                    type = AttributeType.COLOR_DRAWABLE_TYPE
                    value = context.getResources().getColor(kvp.value, TColor.argb(255f, 255f, 255f, 255f))
                } else if (kvp.key == "customPixelDimension") {
                    type = AttributeType.DIMENSION_TYPE
                    value = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        context.getResources().getDimension(kvp.value, 0f),
                        context.getResources().getDisplayMetrics()
                    )
                } else if (kvp.key == "customDimension") {
                    type = AttributeType.DIMENSION_TYPE
                    value = context.getResources().getDimension(kvp.value, 0f)
                } else if (kvp.key == "customFloatValue") {
                    type = AttributeType.FLOAT_TYPE
                    value = context.getResources().getFloat(kvp.value, Float.NaN)
                } else if (kvp.key == "customIntegerValue") {
                    type = AttributeType.INT_TYPE
                    value = context.getResources().getInt(kvp.value, -1)
                } else if (kvp.key == "customStringValue") {
                    type = AttributeType.STRING_TYPE
                    value = context.getResources().getString(kvp.key, kvp.value)
                } else if (kvp.key == "customReference") {
                    type = AttributeType.REFERENCE_TYPE
                    var tmp = context.getResources().getResourceId(kvp.value, "")
                    if (tmp == "") {
                        tmp = context.getResources().getString(kvp.key, kvp.value)
                    }
                    value = tmp
                }
                if (custom != null && name != null && value != null) {
                    custom[name!!] = ConstraintAttribute(name!!, type, value!!, method)
                }
            }
        }
    }
}