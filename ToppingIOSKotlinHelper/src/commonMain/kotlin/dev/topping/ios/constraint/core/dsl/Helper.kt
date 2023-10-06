/*
 * Copyright (C) 2022 The Android Open Source Project
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
package dev.topping.ios.constraint.core.dsl

open class Helper {
    enum class Type {
        VERTICAL_GUIDELINE, HORIZONTAL_GUIDELINE, VERTICAL_CHAIN, HORIZONTAL_CHAIN, BARRIER
    }

    val name: String
    var type: HelperType? = null
        protected set
    var config: String? = null
        protected set
    protected var configMap: MutableMap<String, String>? = mutableMapOf()

    constructor(name: String, type: HelperType?) {
        this.name = name
        this.type = type
    }

    constructor(name: String, type: HelperType?, config: String?) {
        this.name = name
        this.type = type
        this.config = config
        configMap = convertConfigToMap()
    }

    fun convertConfigToMap(): MutableMap<String, String>? {
        if (config.isNullOrEmpty()) {
            return null
        }
        val map: MutableMap<String, String> = mutableMapOf()
        val builder = StringBuilder()
        var key: String? = ""
        var value = ""
        var squareBrackets = 0
        var curlyBrackets = 0
        var ch: Char
        for (i in 0 until config!!.length) {
            ch = config!![i]
            if (ch == ':') {
                key = builder.toString()
                builder.setLength(0)
            } else if (ch == ',' && squareBrackets == 0 && curlyBrackets == 0) {
                value = builder.toString()
                map.put(key!!, value)
                value = ""
                key = value
                builder.setLength(0)
            } else if (ch != ' ') {
                when (ch) {
                    '[' -> squareBrackets++
                    '{' -> curlyBrackets++
                    ']' -> squareBrackets--
                    '}' -> curlyBrackets--
                }
                builder.append(ch)
            }
        }
        map.put(key!!, builder.toString())
        return map
    }

    fun append(map: Map<String, String?>, ret: StringBuilder) {
        if (map.isEmpty()) {
            return
        }
        for (key in map) {
            ret.append(key.key).append(":").append(key.value).append(",\n")
        }
    }

    override fun toString(): String {
        val ret = StringBuilder("$name:{\n")
        if (type != null) {
            ret.append("type:'").append(type.toString()).append("',\n")
        }
        if (configMap != null) {
            append(configMap!!, ret)
        }
        ret.append("},\n")
        return ret.toString()
    }

    class HelperType(val mName: String) {
        override fun toString(): String {
            return mName
        }
    }

    companion object {
        protected val sideMap: MutableMap<Constraint.Side, String> = mutableMapOf()

        init {
            sideMap[Constraint.Side.LEFT] = "'left'"
            sideMap[Constraint.Side.RIGHT] = "'right'"
            sideMap[Constraint.Side.TOP] = "'top'"
            sideMap[Constraint.Side.BOTTOM] = "'bottom'"
            sideMap[Constraint.Side.START] = "'start'"
            sideMap[Constraint.Side.END] = "'end'"
            sideMap[Constraint.Side.BASELINE] = "'baseline'"
        }

        protected val typeMap: MutableMap<Type, String> = mutableMapOf()

        init {
            typeMap[Type.VERTICAL_GUIDELINE] = "vGuideline"
            typeMap[Type.HORIZONTAL_GUIDELINE] = "hGuideline"
            typeMap[Type.VERTICAL_CHAIN] = "vChain"
            typeMap[Type.HORIZONTAL_CHAIN] = "hChain"
            typeMap[Type.BARRIER] = "barrier"
        }

        fun main(args: Array<String?>?) {
            val b = Barrier("abc", "['a1', 'b2']")
            println(b.toString())
        }
    }
}