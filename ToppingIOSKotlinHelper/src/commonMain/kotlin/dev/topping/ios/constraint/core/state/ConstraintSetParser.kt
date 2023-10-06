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
package dev.topping.ios.constraint.core.state

import dev.topping.ios.constraint.core.motion.utils.TypedBundle
import dev.topping.ios.constraint.core.motion.utils.TypedValues
import dev.topping.ios.constraint.core.motion.utils.TypedValues.MotionType.Companion.TYPE_QUANTIZE_INTERPOLATOR_TYPE
import dev.topping.ios.constraint.core.motion.utils.TypedValues.MotionType.Companion.TYPE_QUANTIZE_MOTIONSTEPS
import dev.topping.ios.constraint.core.motion.utils.TypedValues.MotionType.Companion.TYPE_QUANTIZE_MOTION_PHASE
import dev.topping.ios.constraint.core.parser.*
import dev.topping.ios.constraint.core.state.helpers.*
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.Flow
import dev.topping.ios.constraint.isNaN

object ConstraintSetParser {
    private const val PARSER_DEBUG = false
    //==================== end Motion Scene =========================
    /**
     * Parse and populate a transition
     *
     * @param content    JSON string to parse
     * @param transition The Transition to be populated
     * @param state
     */
    fun parseJSON(content: String, transition: Transition, state: Int) {
        try {
            val json: CLObject = CLParser.parse(content)
            val elements: ArrayList<String> = json.names() ?: return
            for (elementName in elements) {
                val base_element: CLElement? = json.get(elementName)
                if (base_element is CLObject) {
                    val element: CLObject = base_element as CLObject
                    val customProperties: CLObject? = element.getObjectOrNull("custom")
                    if (customProperties != null) {
                        val properties: ArrayList<String> = customProperties.names()
                        for (property in properties) {
                            val value: CLElement? = customProperties.get(property)
                            if (value is CLNumber) {
                                transition.addCustomFloat(
                                    state,
                                    elementName,
                                    property,
                                    value.float
                                )
                            } else if (value is CLString) {
                                val color = parseColorString(value.content())
                                if (color != -1L) {
                                    transition.addCustomColor(
                                        state,
                                        elementName, property, color.toInt()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: CLParsingException) {
            println("Error parsing JSON $e")
        }
    }

    /**
     * Parse and build a motionScene
     *
     * this should be in a MotionScene / MotionSceneParser
     */
    fun parseMotionSceneJSON(scene: CoreMotionScene, content: String) {
        try {
            val json: CLObject = CLParser.parse(content)
            val elements: ArrayList<String> = json.names() ?: return
            for (elementName in elements) {
                val element: CLElement? = json.get(elementName)
                if (element is CLObject) {
                    val clObject: CLObject = element as CLObject
                    when (elementName) {
                        "ConstraintSets" -> parseConstraintSets(scene, clObject)
                        "Transitions" -> parseTransitions(scene, clObject)
                        "Header" -> parseHeader(scene, clObject)
                    }
                }
            }
        } catch (e: CLParsingException) {
            println("Error parsing JSON $e")
        }
    }

    /**
     * Parse ConstraintSets and populate MotionScene
     */
    @Throws(CLParsingException::class)
    fun parseConstraintSets(
        scene: CoreMotionScene,
        json: CLObject
    ) {
        val constraintSetNames: ArrayList<String> = json.names() ?: return
        for (csName in constraintSetNames) {
            val constraintSet: CLObject = json.getObject(csName)
            var added = false
            val ext: String? = constraintSet.getStringOrNull("Extends")
            if (ext != null && !ext.isEmpty()) {
                val base: String = scene.getConstraintSet(ext) ?: continue
                val baseJson: CLObject = CLParser.parse(base)
                val widgetsOverride: ArrayList<String> = constraintSet.names() ?: continue
                for (widgetOverrideName in widgetsOverride) {
                    val value: CLElement? = constraintSet.get(widgetOverrideName)
                    if (value is CLObject) {
                        override(baseJson, widgetOverrideName, value as CLObject)
                    }
                }
                scene.setConstraintSetContent(csName, baseJson.toJSON())
                added = true
            }
            if (!added) {
                scene.setConstraintSetContent(csName, constraintSet.toJSON())
            }
        }
    }

    @Throws(CLParsingException::class)
    fun override(
        baseJson: CLObject,
        name: String, overrideValue: CLObject
    ) {
        if (!baseJson.has(name)) {
            baseJson.put(name, overrideValue)
        } else {
            val base: CLObject = baseJson.getObject(name)
            val keys: ArrayList<String> = overrideValue.names()
            for (key in keys) {
                if (!key.equals("clear")) {
                    base.put(key, overrideValue.get(key))
                    continue
                }
                val toClear: CLArray = overrideValue.getArray("clear")
                for (i in 0 until toClear.size()) {
                    val clearedKey: String = toClear.getStringOrNull(i) ?: continue
                    when (clearedKey) {
                        "dimensions" -> {
                            base.remove("width")
                            base.remove("height")
                        }
                        "constraints" -> {
                            base.remove("start")
                            base.remove("end")
                            base.remove("top")
                            base.remove("bottom")
                            base.remove("baseline")
                            base.remove("center")
                            base.remove("centerHorizontally")
                            base.remove("centerVertically")
                        }
                        "transforms" -> {
                            base.remove("visibility")
                            base.remove("alpha")
                            base.remove("pivotX")
                            base.remove("pivotY")
                            base.remove("rotationX")
                            base.remove("rotationY")
                            base.remove("rotationZ")
                            base.remove("scaleX")
                            base.remove("scaleY")
                            base.remove("translationX")
                            base.remove("translationY")
                        }
                        else -> base.remove(clearedKey)
                    }
                }
            }
        }
    }

    /**
     * Parse the Transition
     */
    @Throws(CLParsingException::class)
    fun parseTransitions(scene: CoreMotionScene, json: CLObject) {
        val elements: ArrayList<String> = json.names() ?: return
        for (elementName in elements) {
            scene.setTransitionContent(elementName, json.getObject(elementName).toJSON())
        }
    }

    /**
     * Used to parse for "export"
     */
    fun parseHeader(scene: CoreMotionScene, json: CLObject) {
        val name: String? = json.getStringOrNull("export")
        if (name != null) {
            scene.setDebugName(name)
        }
    }

    /**
     * Top leve parsing of the json ConstraintSet supporting
     * "Variables", "Helpers", "Generate", guidelines, and barriers
     *
     * @param content         the JSON string
     * @param state           the state to populate
     * @param layoutVariables the variables to override
     */
    @Throws(CLParsingException::class)
    fun parseJSON(
        content: String, state: State,
        layoutVariables: LayoutVariables
    ) {
        try {
            val json: CLObject = CLParser.parse(content)
            populateState(json, state, layoutVariables)
        } catch (e: CLParsingException) {
            println("Error parsing JSON $e")
        }
    }

    /**
     * Populates the given [State] with the parameters from [CLObject]. Where the
     * object represents a parsed JSONObject of a ConstraintSet.
     *
     * @param parsedJson CLObject of the parsed ConstraintSet
     * @param state the state to populate
     * @param layoutVariables the variables to override
     * @throws CLParsingException when parsing fails
     * @hide
     */
    @Throws(CLParsingException::class)
    fun populateState(
        parsedJson: CLObject,
        state: State,
        layoutVariables: LayoutVariables
    ) {
        val elements: ArrayList<String> = parsedJson.names() ?: return
        for (elementName in elements) {
            val element: CLElement? = parsedJson.get(elementName)
            if (PARSER_DEBUG) {
                println(
                    "[" + elementName + "] = " + element
                            + " > " + element?.container
                )
            }
            when (elementName) {
                "Variables" -> if (element is CLObject) {
                    parseVariables(state, layoutVariables, element as CLObject)
                }
                "Helpers" -> if (element is CLArray) {
                    parseHelpers(state, layoutVariables, element as CLArray)
                }
                "Generate" -> if (element is CLObject) {
                    parseGenerate(state, layoutVariables, element as CLObject)
                }
                else -> if (element is CLObject) {
                    val type = lookForType(element as CLObject)
                    if (type != null) {
                        when (type) {
                            "hGuideline" -> parseGuidelineParams(
                                ConstraintWidget.HORIZONTAL,
                                state,
                                elementName,
                                element as CLObject
                            )
                            "vGuideline" -> parseGuidelineParams(
                                ConstraintWidget.VERTICAL,
                                state,
                                elementName,
                                element as CLObject
                            )
                            "barrier" -> parseBarrier(state, elementName, element as CLObject)
                            "vChain", "hChain" -> parseChainType(
                                type,
                                state,
                                elementName,
                                layoutVariables,
                                element as CLObject
                            )
                            "vFlow", "hFlow" -> parseFlowType(
                                type,
                                state,
                                elementName,
                                layoutVariables,
                                element as CLObject
                            )
                            "grid", "row", "column" -> parseGridType(
                                type,
                                state,
                                elementName,
                                layoutVariables,
                                element as CLObject
                            )
                        }
                    } else {
                        parseWidget(
                            state,
                            layoutVariables,
                            elementName,
                            element as CLObject
                        )
                    }
                } else if (element is CLNumber) {
                    layoutVariables.put(elementName, element.getInt())
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    private fun parseVariables(
        state: State,
        layoutVariables: LayoutVariables,
        json: CLObject
    ) {
        val elements: ArrayList<String> = json.names() ?: return
        for (elementName in elements) {
            val element: CLElement? = json.get(elementName)
            if (element is CLNumber) {
                layoutVariables.put(elementName, element.getInt())
            } else if (element is CLObject) {
                val obj: CLObject = element as CLObject
                var arrayIds: ArrayList<String?>?
                if (obj.has("from") && obj.has("to")) {
                    val from = layoutVariables[obj["from"]]
                    val to = layoutVariables[obj["to"]]
                    val prefix: String? = obj.getStringOrNull("prefix")
                    val postfix: String? = obj.getStringOrNull("postfix")
                    layoutVariables.put(elementName, from, to, 1f, prefix, postfix)
                } else if (obj.has("from") && obj.has("step")) {
                    val start = layoutVariables[obj["from"]]
                    val increment = layoutVariables[obj["step"]]
                    layoutVariables.put(elementName, start, increment)
                } else if (obj.has("ids")) {
                    val ids: CLArray = obj.getArray("ids")
                    arrayIds = ArrayList()
                    for (i in 0 until ids.size()) {
                        arrayIds.add(ids.getString(i))
                    }
                    layoutVariables.put(elementName, arrayIds)
                } else if (obj.has("tag")) {
                    arrayIds = state.getIdsForTag(obj.getString("tag"))
                    layoutVariables.put(elementName, arrayIds)
                }
            }
        }
    }

    /**
     * parse the Design time elements.
     *
     * @param content the json
     * @param list    output the list of design elements
     */
    @Throws(CLParsingException::class)
    fun parseDesignElementsJSON(
        content: String, list: ArrayList<DesignElement?>
    ) {
        val json: CLObject = CLParser.parse(content)
        var elements: ArrayList<String> = json.names() ?: return
        for (i in 0 until elements.size) {
            val elementName = elements[i]
            val element: CLElement? = json.get(elementName)
            if (PARSER_DEBUG && element != null) {
                println("[" + element + "] " + element::class.toString())
            }
            when (elementName) {
                "Design" -> {
                    if (element !is CLObject) {
                        return
                    }
                    val obj: CLObject = element as CLObject
                    elements = obj.names()
                    var j = 0
                    while (j < elements.size) {
                        val designElementName = elements[j]
                        val designElement: CLObject =
                            (element as CLObject).get(designElementName) as CLObject
                        println("element found $designElementName")
                        val type: String? = designElement.getStringOrNull("type")
                        if (type != null) {
                            val parameters = HashMap<String, String>()
                            val size: Int = designElement.size()
                            var k = 0
                            while (k < size) {
                                val key: CLKey = designElement.get(j) as CLKey
                                val paramName: String = key.content()
                                val paramValue: String? = key.value?.content()
                                if (paramValue != null) {
                                    parameters[paramName] = paramValue
                                }
                                k++
                            }
                            list.add(DesignElement(elementName, type, parameters))
                        }
                        j++
                    }
                }
            }
            break
        }
    }

    @Throws(CLParsingException::class)
    fun parseHelpers(
        state: State,
        layoutVariables: LayoutVariables,
        element: CLArray
    ) {
        for (i in 0 until element.size()) {
            val helper: CLElement? = element.get(i)
            if (helper is CLArray) {
                val array: CLArray = helper as CLArray
                if (array.size() > 1) {
                    when (array.getString(0)) {
                        "hChain" -> parseChain(
                            ConstraintWidget.HORIZONTAL,
                            state,
                            layoutVariables,
                            array
                        )
                        "vChain" -> parseChain(
                            ConstraintWidget.VERTICAL,
                            state,
                            layoutVariables,
                            array
                        )
                        "hGuideline" -> parseGuideline(ConstraintWidget.HORIZONTAL, state, array)
                        "vGuideline" -> parseGuideline(ConstraintWidget.VERTICAL, state, array)
                    }
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseGenerate(
        state: State,
        layoutVariables: LayoutVariables,
        json: CLObject
    ) {
        val elements: ArrayList<String> = json.names() ?: return
        for (elementName in elements) {
            val element: CLElement? = json.get(elementName)
            val arrayIds = layoutVariables.getList(elementName)
            if (arrayIds != null && element is CLObject) {
                for (id in arrayIds) {
                    parseWidget(state, layoutVariables, id, element as CLObject)
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseChain(
        orientation: Int, state: State,
        margins: LayoutVariables, helper: CLArray
    ) {
        val chain: ChainReference? =
            (if (orientation == ConstraintWidget.HORIZONTAL) state.horizontalChain() else state.verticalChain()) as ChainReference?
        val refs: CLElement? = helper[1]
        if (refs !is CLArray || (refs as CLArray).size() < 1) {
            return
        }
        for (i in 0 until (refs as CLArray).size()) {
            chain?.add((refs as CLArray).getString(i))
        }
        if (helper.size() > 2) { // we have additional parameters
            val params: CLElement = helper[2] as? CLObject ?: return
            val obj: CLObject = params as CLObject
            val constraints: ArrayList<String> = obj.names()
            for (constraintName in constraints) {
                when (constraintName) {
                    "style" -> {
                        val styleObject: CLElement? = (params as CLObject).get(constraintName)
                        var styleValue: String
                        if (styleObject is CLArray && (styleObject as CLArray).size() > 1) {
                            styleValue = (styleObject as CLArray).getString(0)
                            val biasValue: Float = (styleObject as CLArray).getFloat(1)
                            chain?.bias(biasValue)
                        } else {
                            styleValue = styleObject?.content().toString()
                        }
                        when (styleValue) {
                            "packed" -> chain?.style(State.Chain.PACKED)
                            "spread_inside" -> chain?.style(State.Chain.SPREAD_INSIDE)
                            else -> chain?.style(State.Chain.SPREAD)
                        }
                    }
                    else -> parseConstraint(
                        state,
                        margins,
                        params as CLObject,
                        chain as ConstraintReference,
                        constraintName
                    )
                }
            }
        }
    }

    private fun toPix(state: State, dp: Float): Float {
        return state.dpToPixel?.toPixels(dp) ?: 0f
    }

    /**
     * Support parsing Chain in the following manner
     * chainId : {
     * type:'hChain'  // or vChain
     * contains: ['id1', 'id2', 'id3' ]
     * contains: [['id', weight, marginL ,marginR], 'id2', 'id3' ]
     * start: ['parent', 'start',0],
     * end: ['parent', 'end',0],
     * top: ['parent', 'top',0],
     * bottom: ['parent', 'bottom',0],
     * style: 'spread'
     * }
     *
     * @throws CLParsingException
     */
    @Throws(CLParsingException::class)
    private fun parseChainType(
        orientation: String,
        state: State,
        chainName: String,
        margins: LayoutVariables,
        `object`: CLObject
    ) {
        val chain: ChainReference? =
            (if (orientation[0] == 'h') state.horizontalChain() else state.verticalChain()) as ChainReference?
        chain?.key = chainName
        for (params in `object`.names()) {
            when (params) {
                "contains" -> {
                    val refs: CLElement? = `object`.get(params)
                    if (refs !is CLArray || (refs as CLArray).size() < 1) {
                        println(
                            chainName + " contains should be an array \"" + refs?.content().toString()
                                    + "\""
                        )
                        return
                    }
                    var i = 0
                    while (i < (refs as CLArray).size()) {
                        val chainElement: CLElement? = (refs as CLArray).get(i)
                        if (chainElement is CLArray) {
                            val array: CLArray = chainElement as CLArray
                            if (array.size() > 0) {
                                val id: String = array[0]?.content().toString()
                                var weight = Float.NaN
                                var preMargin = Float.NaN
                                var postMargin = Float.NaN
                                var preGoneMargin = Float.NaN
                                var postGoneMargin = Float.NaN
                                when (array.size()) {
                                    2 -> weight = array.getFloat(1)
                                    3 -> {
                                        weight = array.getFloat(1)
                                        run {
                                            preMargin = toPix(state, array.getFloat(2))
                                            postMargin = preMargin
                                        }
                                    }
                                    4 -> {
                                        weight = array.getFloat(1)
                                        preMargin = toPix(state, array.getFloat(2))
                                        postMargin = toPix(state, array.getFloat(3))
                                    }
                                    6 -> {
                                        // postGoneMargin
                                        weight = array.getFloat(1)
                                        preMargin = toPix(state, array.getFloat(2))
                                        postMargin = toPix(state, array.getFloat(3))
                                        preGoneMargin = toPix(state, array.getFloat(4))
                                        postGoneMargin = toPix(state, array.getFloat(5))
                                    }
                                }
                                chain?.addChainElement(
                                    id,
                                    weight,
                                    preMargin,
                                    postMargin,
                                    preGoneMargin,
                                    postGoneMargin
                                )
                            }
                        } else {
                            chain?.add(chainElement?.content().toString())
                        }
                        i++
                    }
                }
                "start", "end", "top", "bottom", "left", "right" -> parseConstraint(
                    state,
                    margins,
                    `object`,
                    chain,
                    params
                )
                "style" -> {
                    val styleObject: CLElement? = `object`.get(params)
                    var styleValue: String?
                    if (styleObject is CLArray && (styleObject as CLArray).size() > 1) {
                        styleValue = (styleObject as CLArray).getString(0)
                        val biasValue: Float = (styleObject as CLArray).getFloat(1)
                        chain?.bias(biasValue)
                    } else {
                        styleValue = styleObject?.content()
                    }
                    when (styleValue) {
                        "packed" -> chain?.style(State.Chain.PACKED)
                        "spread_inside" -> chain?.style(State.Chain.SPREAD_INSIDE)
                        else -> chain?.style(State.Chain.SPREAD)
                    }
                }
            }
        }
    }

    /**
     * Support parsing Grid in the following manner
     * chainId : {
     * height: "parent",
     * width: "parent",
     * type: "Grid",
     * vGap: 10,
     * hGap: 10,
     * orientation: 0,
     * rows: 0,
     * columns: 1,
     * columnWeights: "",
     * rowWeights: "",
     * contains: ["btn1", "btn2", "btn3", "btn4"],
     * top: ["parent", "top", 10],
     * bottom: ["parent", "bottom", 20],
     * right: ["parent", "right", 30],
     * left: ["parent", "left", 40],
     * }
     *
     * @param gridType type of the Grid helper could be "Grid"|"Row"|"Column"
     * @param state ConstraintLayout State
     * @param name the name of the Grid Helper
     * @param layoutVariables layout margins
     * @param element the element to be parsed
     * @throws CLParsingException
     */
    @Throws(CLParsingException::class)
    private fun parseGridType(
        gridType: String,
        state: State,
        name: String,
        layoutVariables: LayoutVariables,
        element: CLObject
    ) {
        val grid: GridReference = state.getGrid(name, gridType)
        for (param in element.names()) {
            when (param) {
                "contains" -> {
                    val list: CLArray? = element.getArrayOrNull(param)
                    if (list != null) {
                        var j = 0
                        while (j < list.size()) {
                            val elementNameReference: String? = list[j]?.content()
                            val elementReference: ConstraintReference? =
                                state.constraints(elementNameReference)
                            grid.add(elementReference)
                            j++
                        }
                    }
                }
                "orientation" -> {
                    val orientation: Int = element.get(param)?.int ?: 0
                    grid.orientation = orientation
                }
                "rows" -> {
                    val rows: Int = element.get(param)?.int ?: 0
                    if (rows > 0) {
                        grid.rowsSet = rows
                    }
                }
                "columns" -> {
                    val columns: Int = element.get(param)?.int ?: 0
                    if (columns > 0) {
                        grid.columnsSet = columns
                    }
                }
                "hGap" -> {
                    val hGap: Float = element[param]?.float ?: 0f
                    grid.horizontalGaps = toPix(state, hGap)
                }
                "vGap" -> {
                    val vGap: Float = element.get(param)?.float ?: 0f
                    grid.verticalGaps = toPix(state, vGap)
                }
                "spans" -> {
                    val spans: String? = element.get(param)?.content()
                    if (spans != null && spans.contains(":")) {
                        grid.spans = spans
                    }
                }
                "skips" -> {
                    val skips: String? = element.get(param)?.content()
                    if (skips != null && skips.contains(":")) {
                        grid.skips = skips
                    }
                }
                "rowWeights" -> {
                    val rowWeights: String? = element.get(param)?.content()
                    if (rowWeights != null && rowWeights.contains(",")) {
                        grid.rowWeights = rowWeights
                    }
                }
                "columnWeights" -> {
                    val columnWeights: String? = element.get(param)?.content()
                    if (columnWeights != null && columnWeights.contains(",")) {
                        grid.columnWeights = columnWeights
                    }
                }
                "padding" -> {
                    val paddingObject: CLElement? = element.get(param)
                    var paddingStart = 0
                    var paddingTop = 0
                    var paddingEnd = 0
                    var paddingBottom = 0
                    if (paddingObject is CLArray && (paddingObject as CLArray).size() > 1) {
                        paddingStart = (paddingObject as CLArray).getInt(0)
                        paddingEnd = paddingStart
                        paddingTop = (paddingObject as CLArray).getInt(1)
                        paddingBottom = paddingTop
                        if ((paddingObject as CLArray).size() > 2) {
                            paddingEnd = (paddingObject as CLArray).getInt(2)
                            paddingBottom = try {
                                (paddingObject as CLArray).getInt(3)
                            } catch (e: ArrayIndexOutOfBoundsException) {
                                0
                            }
                        }
                    } else {
                        paddingStart = paddingObject?.int ?: 0
                        paddingTop = paddingStart
                        paddingEnd = paddingStart
                        paddingBottom = paddingStart
                    }
                    grid.paddingStart = paddingStart
                    grid.paddingTop = paddingTop
                    grid.paddingEnd = paddingEnd
                    grid.paddingBottom = paddingBottom
                }
                "flags" -> {
                    var flags: String? = element.get(param)?.content()
                    if (flags != null && flags.isNotEmpty()) {
                        grid.setFlags(flags)
                    } else {
                        val flagArray: CLArray? = element.getArrayOrNull(param)
                        flags = ""
                        if (flagArray != null) {
                            var i = 0
                            while (i < flagArray.size()) {
                                val flag: String = flagArray.get(i)?.content().toString()
                                flags += flag
                                if (i != flagArray.size() - 1) {
                                    flags += "|"
                                }
                                i++
                            }
                            grid.setFlags(flags)
                        }
                    }
                }
                else -> {
                    val reference: ConstraintReference? = state.constraints(name)
                    applyAttribute(state, layoutVariables, reference, element, param)
                }
            }
        }
    }

    /**
     * It's used to parse the Flow type of Helper with the following format:
     * flowID: {
     * type: 'hFlow'|'vFlow’
     * wrap: 'chain'|'none'|'aligned',
     * contains: ['id1', 'id2', 'id3' ] |
     * [['id1', weight, preMargin , postMargin], 'id2', 'id3'],
     * vStyle: 'spread'|'spread_inside'|'packed' | ['first', 'middle', 'last'],
     * hStyle: 'spread'|'spread_inside'|'packed' | ['first', 'middle', 'last'],
     * vAlign: 'top'|'bottom'|'baseline'|'center',
     * hAlign: 'start'|'end'|'center',
     * vGap: 32,
     * hGap: 23,
     * padding: 32,
     * maxElement: 5,
     * vBias: 0.3 | [0.0, 0.5, 0.5],
     * hBias: 0.4 | [0.0, 0.5, 0.5],
     * start: ['parent', 'start', 0],
     * end: ['parent', 'end', 0],
     * top: ['parent', 'top', 0],
     * bottom: ['parent', 'bottom', 0],
     * }
     *
     * @param flowType orientation of the Flow Helper
     * @param state ConstraintLayout State
     * @param flowName the name of the Flow Helper
     * @param layoutVariables layout margins
     * @param element the element to be parsed
     * @throws CLParsingException
     */
    @Throws(CLParsingException::class)
    private fun parseFlowType(
        flowType: String,
        state: State,
        flowName: String,
        layoutVariables: LayoutVariables,
        element: CLObject
    ) {
        val isVertical = flowType[0] == 'v'
        val flow: FlowReference? = state.getFlow(flowName, isVertical)
        for (param in element.names()) {
            when (param) {
                "contains" -> {
                    val refs: CLElement? = element.get(param)
                    if (refs !is CLArray || (refs as CLArray).size() < 1) {
                        println(
                            flowName + " contains should be an array \"" + refs?.content().toString()
                                    + "\""
                        )
                        return
                    }
                    var i = 0
                    while (i < (refs as CLArray).size()) {
                        val chainElement: CLElement? = (refs as CLArray).get(i)
                        if (chainElement is CLArray) {
                            val array: CLArray = chainElement as CLArray
                            if (array.size() > 0) {
                                val id: String = array.get(0)?.content().toString()
                                var weight = Float.NaN
                                var preMargin = Float.NaN
                                var postMargin = Float.NaN
                                when (array.size()) {
                                    2 -> weight = array.getFloat(1)
                                    3 -> {
                                        weight = array.getFloat(1)
                                        run {
                                            preMargin = toPix(state, array.getFloat(2))
                                            postMargin = preMargin
                                        }
                                    }
                                    4 -> {
                                        weight = array.getFloat(1)
                                        preMargin = toPix(state, array.getFloat(2))
                                        postMargin = toPix(state, array.getFloat(3))
                                    }
                                }
                                flow?.addFlowElement(id, weight, preMargin, postMargin)
                            }
                        } else {
                            flow?.add(chainElement?.content())
                        }
                        i++
                    }
                }
                "type" -> if (element.get(param)?.content().equals("hFlow")) {
                    flow?.orientation = ConstraintWidget.HORIZONTAL
                } else {
                    flow?.orientation = ConstraintWidget.VERTICAL
                }
                "wrap" -> {
                    val wrapValue: String = element.get(param)?.content().toString()
                    flow?.wrapMode = State.Wrap.getValueByString(wrapValue)
                }
                "vGap" -> {
                    val vGapValue: Int = element.get(param)?.int ?: 0
                    flow?.verticalGap = vGapValue
                }
                "hGap" -> {
                    val hGapValue: Int = element.get(param)?.int ?: 0
                    flow?.horizontalGap = hGapValue
                }
                "maxElement" -> {
                    val maxElementValue: Int = element.get(param)?.int ?: 0
                    flow?.maxElementsWrap = maxElementValue
                }
                "padding" -> {
                    val paddingObject: CLElement? = element.get(param)
                    var paddingLeft = 0
                    var paddingTop = 0
                    var paddingRight = 0
                    var paddingBottom = 0
                    if (paddingObject is CLArray && (paddingObject as CLArray).size() > 1) {
                        paddingLeft = (paddingObject as CLArray).getInt(0)
                        paddingRight = paddingLeft
                        paddingTop = (paddingObject as CLArray).getInt(1)
                        paddingBottom = paddingTop
                        if ((paddingObject as CLArray).size() > 2) {
                            paddingRight = (paddingObject as CLArray).getInt(2)
                            paddingBottom = try {
                                (paddingObject as CLArray).getInt(3)
                            } catch (e: ArrayIndexOutOfBoundsException) {
                                0
                            }
                        }
                    } else {
                        paddingLeft = paddingObject?.int ?: 0
                        paddingTop = paddingLeft
                        paddingRight = paddingLeft
                        paddingBottom = paddingLeft
                    }
                    flow?.paddingLeft = paddingLeft
                    flow?.paddingTop = paddingTop
                    flow?.paddingRight = paddingRight
                    flow?.paddingBottom = paddingBottom
                }
                "vAlign" -> {
                    val vAlignValue: String = element.get(param)?.content().toString()
                    when (vAlignValue) {
                        "top" -> flow?.verticalAlign = Flow.VERTICAL_ALIGN_TOP
                        "bottom" -> flow?.verticalAlign = Flow.VERTICAL_ALIGN_BOTTOM
                        "baseline" -> flow?.verticalAlign = Flow.VERTICAL_ALIGN_BASELINE
                        else -> flow?.verticalAlign = Flow.VERTICAL_ALIGN_CENTER
                    }
                }
                "hAlign" -> {
                    val hAlignValue: String = element.get(param)?.content().toString()
                    when (hAlignValue) {
                        "start" -> flow?.horizontalAlign = Flow.HORIZONTAL_ALIGN_START
                        "end" -> flow?.horizontalAlign = Flow.HORIZONTAL_ALIGN_END
                        else -> flow?.horizontalAlign = Flow.HORIZONTAL_ALIGN_CENTER
                    }
                }
                "vFlowBias" -> {
                    val vBiasObject: CLElement? = element.get(param)
                    var vBiasValue = 0.5f
                    var vFirstBiasValue = 0.5f
                    var vLastBiasValue = 0.5f
                    if (vBiasObject is CLArray && (vBiasObject as CLArray).size() > 1) {
                        vFirstBiasValue = (vBiasObject as CLArray).getFloat(0)
                        vBiasValue = (vBiasObject as CLArray).getFloat(1)
                        if ((vBiasObject as CLArray).size() > 2) {
                            vLastBiasValue = (vBiasObject as CLArray).getFloat(2)
                        }
                    } else {
                        vBiasValue = vBiasObject?.float ?: 0f
                    }
                    try {
                        flow?.verticalBias(vBiasValue)
                        if (vFirstBiasValue != 0.5f) {
                            flow?.firstVerticalBias = vFirstBiasValue
                        }
                        if (vLastBiasValue != 0.5f) {
                            flow?.lastVerticalBias = vLastBiasValue
                        }
                    } catch (e: NumberFormatException) {
                    }
                }
                "hFlowBias" -> {
                    val hBiasObject: CLElement? = element.get(param)
                    var hBiasValue = 0.5f
                    var hFirstBiasValue = 0.5f
                    var hLastBiasValue = 0.5f
                    if (hBiasObject is CLArray && (hBiasObject as CLArray).size() > 1) {
                        hFirstBiasValue = (hBiasObject as CLArray).getFloat(0)
                        hBiasValue = (hBiasObject as CLArray).getFloat(1)
                        if ((hBiasObject as CLArray).size() > 2) {
                            hLastBiasValue = (hBiasObject as CLArray).getFloat(2)
                        }
                    } else {
                        hBiasValue = hBiasObject?.float ?: 0f
                    }
                    try {
                        flow?.horizontalBias(hBiasValue)
                        if (hFirstBiasValue != 0.5f) {
                            flow?.firstHorizontalBias = hFirstBiasValue
                        }
                        if (hLastBiasValue != 0.5f) {
                            flow?.lastHorizontalBias = hLastBiasValue
                        }
                    } catch (e: NumberFormatException) {
                    }
                }
                "vStyle" -> {
                    val vStyleObject: CLElement? = element.get(param)
                    var vStyleValueStr = ""
                    var vFirstStyleValueStr = ""
                    var vLastStyleValueStr = ""
                    if (vStyleObject is CLArray && (vStyleObject as CLArray).size() > 1) {
                        vFirstStyleValueStr = (vStyleObject as CLArray).getString(0)
                        vStyleValueStr = (vStyleObject as CLArray).getString(1)
                        if ((vStyleObject as CLArray).size() > 2) {
                            vLastStyleValueStr = (vStyleObject as CLArray).getString(2)
                        }
                    } else {
                        vStyleValueStr = vStyleObject?.content().toString()
                    }
                    if (!vStyleValueStr.equals("")) {
                        flow?.verticalStyle = State.Chain.getValueByString(vStyleValueStr)
                    }
                    if (!vFirstStyleValueStr.equals("")) {
                        flow?.firstVerticalStyle = State.Chain.getValueByString(vFirstStyleValueStr)
                    }
                    if (!vLastStyleValueStr.equals("")) {
                        flow?.lastVerticalStyle = State.Chain.getValueByString(vLastStyleValueStr)
                    }
                }
                "hStyle" -> {
                    val hStyleObject: CLElement? = element.get(param)
                    var hStyleValueStr = ""
                    var hFirstStyleValueStr = ""
                    var hLastStyleValueStr = ""
                    if (hStyleObject is CLArray && (hStyleObject as CLArray).size() > 1) {
                        hFirstStyleValueStr = (hStyleObject as CLArray).getString(0)
                        hStyleValueStr = (hStyleObject as CLArray).getString(1)
                        if ((hStyleObject as CLArray).size() > 2) {
                            hLastStyleValueStr = (hStyleObject as CLArray).getString(2)
                        }
                    } else {
                        hStyleValueStr = hStyleObject?.content().toString()
                    }
                    if (hStyleValueStr != "") {
                        flow?.horizontalStyle = State.Chain.getValueByString(hStyleValueStr)
                    }
                    if (hFirstStyleValueStr != "") {
                        flow?.firstHorizontalStyle = State.Chain.getValueByString(hFirstStyleValueStr)
                    }
                    if (hLastStyleValueStr != "") {
                        flow?.lastHorizontalStyle = State.Chain.getValueByString(hLastStyleValueStr)
                    }
                }
                else -> {
                    // Get the underlying reference for the flow, apply the constraints
                    // attributes to it
                    val reference: ConstraintReference? = state.constraints(flowName)
                    applyAttribute(state, layoutVariables, reference, element, param)
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseGuideline(
        orientation: Int,
        state: State, helper: CLArray
    ) {
        val params: CLElement = helper.get(1) as? CLObject ?: return
        val guidelineId: String = (params as CLObject).getStringOrNull("id") ?: return
        parseGuidelineParams(orientation, state, guidelineId, params as CLObject)
    }

    @Throws(CLParsingException::class)
    fun parseGuidelineParams(
        orientation: Int,
        state: State,
        guidelineId: String?,
        params: CLObject
    ) {
        val constraints: ArrayList<String> = params.names() ?: return
        val reference: ConstraintReference? = state.constraints(guidelineId)
        if (orientation == ConstraintWidget.HORIZONTAL) {
            state.horizontalGuideline(guidelineId)
        } else {
            state.verticalGuideline(guidelineId)
        }

        // Ignore LTR for Horizontal guidelines, since `start` & `end` represent the distance
        // from `top` and `bottom` respectively
        val isLtr = state.isLtr || orientation == ConstraintWidget.HORIZONTAL
        val guidelineReference: GuidelineReference = reference?.facade as GuidelineReference

        // Whether the guideline is based on percentage or distance
        var isPercent = false

        // Percent or distance value of the guideline
        var value = 0f

        // Indicates if the value is considered from the "start" position,
        // meaning "left" anchor for vertical guidelines and "top" anchor for
        // horizontal guidelines
        var fromStart = true
        for (constraintName in constraints) {
            when (constraintName) {
                "left" -> {
                    value = toPix(state, params.getFloat(constraintName))
                    fromStart = true
                }
                "right" -> {
                    value = toPix(state, params.getFloat(constraintName))
                    fromStart = false
                }
                "start" -> {
                    value = toPix(state, params.getFloat(constraintName))
                    fromStart = isLtr
                }
                "end" -> {
                    value = toPix(state, params.getFloat(constraintName))
                    fromStart = !isLtr
                }
                "percent" -> {
                    isPercent = true
                    val percentParams: CLArray? = params.getArrayOrNull(constraintName)
                    if (percentParams == null) {
                        fromStart = true
                        value = params.getFloat(constraintName)
                    } else if (percentParams.size() > 1) {
                        val origin: String = percentParams.getString(0)
                        value = percentParams.getFloat(1)
                        when (origin) {
                            "left" -> fromStart = true
                            "right" -> fromStart = false
                            "start" -> fromStart = isLtr
                            "end" -> fromStart = !isLtr
                        }
                    }
                }
            }
        }

        // Populate the guideline based on the resolved properties
        if (isPercent) {
            if (fromStart) {
                guidelineReference.percent(value)
            } else {
                guidelineReference.percent(1f - value)
            }
        } else {
            if (fromStart) {
                guidelineReference.start(value)
            } else {
                guidelineReference.end(value)
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseBarrier(
        state: State,
        elementName: String?, element: CLObject
    ) {
        val isLtr: Boolean = state.isLtr
        val reference: BarrierReference = state.barrier(elementName, State.Direction.END)
        val constraints: ArrayList<String> = element.names() ?: return
        for (constraintName in constraints) {
            when (constraintName) {
                "direction" -> {
                    when (element.getString(constraintName)) {
                        "start" -> if (isLtr) {
                            reference.setBarrierDirection(State.Direction.LEFT)
                        } else {
                            reference.setBarrierDirection(State.Direction.RIGHT)
                        }
                        "end" -> if (isLtr) {
                            reference.setBarrierDirection(State.Direction.RIGHT)
                        } else {
                            reference.setBarrierDirection(State.Direction.LEFT)
                        }
                        "left" -> reference.setBarrierDirection(State.Direction.LEFT)
                        "right" -> reference.setBarrierDirection(State.Direction.RIGHT)
                        "top" -> reference.setBarrierDirection(State.Direction.TOP)
                        "bottom" -> reference.setBarrierDirection(State.Direction.BOTTOM)
                    }
                }
                "margin" -> {
                    val margin: Float = element.getFloatOrNaN(constraintName)
                    if (!Float.isNaN(margin)) {
                        reference.margin(toPix(state, margin))
                    }
                }
                "contains" -> {
                    val list: CLArray? = element.getArrayOrNull(constraintName)
                    if (list != null) {
                        var j = 0
                        while (j < list.size()) {
                            val elementNameReference: String = list.get(j)?.content().toString()
                            val elementReference: ConstraintReference? =
                                state.constraints(elementNameReference)
                            if (PARSER_DEBUG) {
                                println(
                                    "Add REFERENCE "
                                            + "(\$elementNameReference = \$elementReference) "
                                            + "TO BARRIER "
                                )
                            }
                            reference.add(elementReference)
                            j++
                        }
                    }
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    fun parseWidget(
        state: State,
        layoutVariables: LayoutVariables,
        elementName: String?,
        element: CLObject
    ) {
        val reference: ConstraintReference? = state.constraints(elementName)
        parseWidget(state, layoutVariables, reference, element)
    }

    /**
     * Set/apply attribute to a widget/helper reference
     *
     * @param state Constraint State
     * @param layoutVariables layout variables
     * @param reference widget/helper reference
     * @param element the parsed CLObject
     * @param attributeName Name of the attribute to be set/applied
     * @throws CLParsingException
     */
    @Throws(CLParsingException::class)
    fun applyAttribute(
        state: State,
        layoutVariables: LayoutVariables,
        reference: ConstraintReference?,
        element: CLObject,
        attributeName: String?
    ) {
        var value: Float
        when (attributeName) {
            "width" -> reference!!.setWidth(
                parseDimension(
                    element,
                    attributeName, state, state.dpToPixel
                )
            )
            "height" -> reference!!.setHeight(
                parseDimension(
                    element,
                    attributeName, state, state.dpToPixel
                )
            )
            "center" -> {
                val target: String = element.getString(attributeName)
                val targetReference: ConstraintReference?
                targetReference = if (target == "parent") {
                    state.constraints(State.PARENT)
                } else {
                    state.constraints(target)
                }
                reference?.startToStart(targetReference)
                reference?.endToEnd(targetReference)
                reference?.topToTop(targetReference)
                reference?.bottomToBottom(targetReference)
            }
            "centerHorizontally" -> {
                val target: String = element.getString(attributeName)
                val targetReference: ConstraintReference?
                targetReference =
                    if (target == "parent") state.constraints(State.PARENT) else state.constraints(
                        target
                    )
                reference?.startToStart(targetReference)
                reference?.endToEnd(targetReference)
            }
            "centerVertically" -> {
                val target: String = element.getString(attributeName)
                val targetReference: ConstraintReference?
                targetReference =
                    if (target == "parent") state.constraints(State.PARENT) else state.constraints(
                        target
                    )
                reference?.topToTop(targetReference)
                reference?.bottomToBottom(targetReference)
            }
            "alpha" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.alpha(value)
            }
            "scaleX" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.scaleX(value)
            }
            "scaleY" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.scaleY(value)
            }
            "translationX" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.translationX(toPix(state, value))
            }
            "translationY" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.translationY(toPix(state, value))
            }
            "translationZ" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.translationZ(toPix(state, value))
            }
            "pivotX" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.pivotX(value)
            }
            "pivotY" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.pivotY(value)
            }
            "rotationX" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.rotationX(value)
            }
            "rotationY" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.rotationY(value)
            }
            "rotationZ" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.rotationZ(value)
            }
            "visibility" -> when (element.getString(attributeName)) {
                "visible" -> reference?.visibility(ConstraintWidget.VISIBLE)
                "invisible" -> reference?.visibility(ConstraintWidget.INVISIBLE)
                "gone" -> reference?.visibility(ConstraintWidget.GONE)
            }
            "vBias" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.verticalBias(value)
            }
            "hRtlBias" -> {
                // TODO: This is a temporary solution to support bias with start/end constraints,
                //  where the bias needs to be reversed in RTL, we probably want a better or more
                //  intuitive way to do this
                value = layoutVariables[element.get(attributeName)]
                if (!state.isLtr) {
                    value = 1f - value
                }
                reference?.horizontalBias(value)
            }
            "hBias" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.horizontalBias(value)
            }
            "vWeight" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.verticalChainWeight = value
            }
            "hWeight" -> {
                value = layoutVariables[element.get(attributeName)]
                reference?.horizontalChainWeight = value
            }
            "custom" -> parseCustomProperties(element, reference, attributeName)
            "motion" -> parseMotionProperties(element.get(attributeName), reference)
            else -> parseConstraint(state, layoutVariables, element, reference, attributeName)
        }
    }

    @Throws(CLParsingException::class)
    fun parseWidget(
        state: State,
        layoutVariables: LayoutVariables,
        reference: ConstraintReference?,
        element: CLObject
    ) {
        if (reference?.width == null) {
            // Default to Wrap when the Dimension has not been assigned
            reference?.setWidth(Dimension.createWrap())
        }
        if (reference?.height == null) {
            // Default to Wrap when the Dimension has not been assigned
            reference?.setHeight(Dimension.createWrap())
        }
        val constraints: ArrayList<String> = element.names() ?: return
        for (constraintName in constraints) {
            applyAttribute(state, layoutVariables, reference, element, constraintName)
        }
    }

    @Throws(CLParsingException::class)
    fun parseCustomProperties(
        element: CLObject,
        reference: ConstraintReference?,
        constraintName: String?
    ) {
        val json: CLObject = element.getObjectOrNull(constraintName) ?: return
        val properties: ArrayList<String> = json.names() ?: return
        for (property in properties) {
            val value: CLElement? = json.get(property)
            if (value is CLNumber) {
                reference!!.addCustomFloat(property, value.float)
            } else if (value is CLString) {
                val it = parseColorString(value.content())
                if (it != -1L) {
                    reference!!.addCustomColor(property, it.toInt())
                }
            }
        }
    }

    private fun indexOf(`val`: String, vararg types: String): Int {
        for (i in types.indices) {
            if (types[i].equals(`val`)) {
                return i
            }
        }
        return -1
    }

    /**
     * parse the motion section of a constraint
     * <pre>
     * csetName: {
     * idToConstrain : {
     * motion: {
     * pathArc : 'startVertical'
     * relativeTo: 'id'
     * easing: 'curve'
     * stagger: '2'
     * quantize: steps or [steps, 'interpolator' phase ]
     * }
     * }
     * }
    </pre> *
     */
    @Throws(CLParsingException::class)
    private fun parseMotionProperties(
        element: CLElement?,
        reference: ConstraintReference?
    ) {
        if (element !is CLObject) {
            return
        }
        val obj: CLObject = element as CLObject
        val bundle = TypedBundle()
        val constraints: ArrayList<String> = obj.names() ?: return
        for (constraintName in constraints) {
            when (constraintName) {
                "pathArc" -> {
                    val `val`: String = obj.getString(constraintName)
                    val ord = indexOf(
                        `val`, "none", "startVertical", "startHorizontal", "flip",
                        "below", "above"
                    )
                    if (ord == -1) {
                        println(obj.line.toString() + " pathArc = '" + `val` + "'")
                        break
                    }
                    bundle.add(TypedValues.MotionType.TYPE_PATHMOTION_ARC, ord)
                }
                "relativeTo" -> bundle.add(
                    TypedValues.MotionType.TYPE_ANIMATE_RELATIVE_TO,
                    obj.getString(constraintName)
                )
                "easing" -> bundle.add(
                    TypedValues.MotionType.TYPE_EASING,
                    obj.getString(constraintName)
                )
                "stagger" -> bundle.add(
                    TypedValues.MotionType.TYPE_STAGGER,
                    obj.getFloat(constraintName)
                )
                "quantize" -> {
                    val quant: CLElement? = obj.get(constraintName)
                    if (quant is CLArray) {
                        val array: CLArray = quant as CLArray
                        val len: Int = array.size()
                        if (len > 0) {
                            bundle.add(TYPE_QUANTIZE_MOTIONSTEPS, array.getInt(0))
                            if (len > 1) {
                                bundle.add(TYPE_QUANTIZE_INTERPOLATOR_TYPE, array.getString(1))
                                if (len > 2) {
                                    bundle.add(TYPE_QUANTIZE_MOTION_PHASE, array.getFloat(2))
                                }
                            }
                        }
                    } else {
                        bundle.add(TYPE_QUANTIZE_MOTIONSTEPS, obj.getInt(constraintName))
                    }
                }
            }
        }
        reference!!.mMotionProperties = bundle
    }

    @Throws(CLParsingException::class)
    fun parseConstraint(
        state: State,
        layoutVariables: LayoutVariables,
        element: CLObject,
        reference: ConstraintReference?,
        constraintName: String?
    ) {
        val isLtr: Boolean = state.isLtr
        val constraint: CLArray? = element.getArrayOrNull(constraintName)
        if (constraint != null && constraint.size() > 1) {
            // params: target, anchor
            val target: String = constraint.getString(0)
            val anchor: String? = constraint.getStringOrNull(1)
            var margin = 0f
            var marginGone = 0f
            if (constraint.size() > 2) {
                // params: target, anchor, margin
                val arg2: CLElement? = constraint.getOrNull(2)
                margin = layoutVariables[arg2]
                margin = toPix(state, margin)
            }
            if (constraint.size() > 3) {
                // params: target, anchor, margin, marginGone
                val arg2: CLElement? = constraint.getOrNull(3)
                marginGone = layoutVariables[arg2]
                marginGone = toPix(state, marginGone)
            }
            val targetReference: ConstraintReference? =
                if (target.equals("parent")) state.constraints(State.PARENT) else state.constraints(
                    target
                )

            // For simplicity, we'll apply horizontal constraints separately
            var isHorizontalConstraint = false
            var isHorOriginLeft = true
            var isHorTargetLeft = true
            when (constraintName) {
                "circular" -> {
                    val angle = layoutVariables[constraint.get(1)]
                    var distance = 0f
                    if (constraint.size() > 2) {
                        val distanceArg: CLElement? = constraint.getOrNull(2)
                        distance = layoutVariables[distanceArg]
                        distance = toPix(state, distance)
                    }
                    reference!!.circularConstraint(targetReference, angle, distance)
                }
                "top" -> when (anchor) {
                    "top" -> reference!!.topToTop(targetReference)
                    "bottom" -> reference!!.topToBottom(targetReference)
                    "baseline" -> {
                        state.baselineNeededFor(targetReference?.key)
                        reference!!.topToBaseline(targetReference)
                    }
                }
                "bottom" -> when (anchor) {
                    "top" -> reference!!.bottomToTop(targetReference)
                    "bottom" -> reference!!.bottomToBottom(targetReference)
                    "baseline" -> {
                        state.baselineNeededFor(targetReference?.key)
                        reference!!.bottomToBaseline(targetReference)
                    }
                }
                "baseline" -> when (anchor) {
                    "baseline" -> {
                        state.baselineNeededFor(reference?.key)
                        state.baselineNeededFor(targetReference?.key)
                        reference!!.baselineToBaseline(targetReference)
                    }
                    "top" -> {
                        state.baselineNeededFor(reference?.key)
                        reference!!.baselineToTop(targetReference)
                    }
                    "bottom" -> {
                        state.baselineNeededFor(reference?.key)
                        reference!!.baselineToBottom(targetReference)
                    }
                }
                "left" -> {
                    isHorizontalConstraint = true
                    isHorOriginLeft = true
                }
                "right" -> {
                    isHorizontalConstraint = true
                    isHorOriginLeft = false
                }
                "start" -> {
                    isHorizontalConstraint = true
                    isHorOriginLeft = isLtr
                }
                "end" -> {
                    isHorizontalConstraint = true
                    isHorOriginLeft = !isLtr
                }
            }
            if (isHorizontalConstraint) {
                // Resolve horizontal target anchor
                when (anchor) {
                    "left" -> isHorTargetLeft = true
                    "right" -> isHorTargetLeft = false
                    "start" -> isHorTargetLeft = isLtr
                    "end" -> isHorTargetLeft = !isLtr
                }

                // Resolved anchors, apply corresponding constraint
                if (isHorOriginLeft) {
                    if (isHorTargetLeft) {
                        reference!!.leftToLeft(targetReference)
                    } else {
                        reference!!.leftToRight(targetReference)
                    }
                } else {
                    if (isHorTargetLeft) {
                        reference!!.rightToLeft(targetReference)
                    } else {
                        reference!!.rightToRight(targetReference)
                    }
                }
            }
            reference!!.margin(margin).marginGone(marginGone)
        } else {
            val target: String? = element.getStringOrNull(constraintName)
            if (target != null) {
                val targetReference: ConstraintReference? =
                    if (target.equals("parent")) state.constraints(State.PARENT) else state.constraints(
                        target
                    )
                when (constraintName) {
                    "start" -> if (isLtr) {
                        reference?.leftToLeft(targetReference)
                    } else {
                        reference?.rightToRight(targetReference)
                    }
                    "end" -> if (isLtr) {
                        reference?.rightToRight(targetReference)
                    } else {
                        reference?.leftToLeft(targetReference)
                    }
                    "top" -> reference?.topToTop(targetReference)
                    "bottom" -> reference?.bottomToBottom(targetReference)
                    "baseline" -> {
                        state.baselineNeededFor(reference?.key)
                        state.baselineNeededFor(targetReference?.key)
                        reference!!.baselineToBaseline(targetReference)
                    }
                }
            }
        }
    }

    fun parseDimensionMode(dimensionString: String): Dimension {
        var dimension: Dimension = Dimension.createFixed(0)
        when (dimensionString) {
            "wrap" -> dimension = Dimension.createWrap()
            "preferWrap" -> dimension = Dimension.createSuggested(Dimension.WRAP_DIMENSION)
            "spread" -> dimension = Dimension.createSuggested(Dimension.SPREAD_DIMENSION)
            "parent" -> dimension = Dimension.createParent()
            else -> {
                if (dimensionString.endsWith("%")) {
                    // parent percent
                    val percentString = dimensionString.substring(0, dimensionString.indexOf('%'))
                    val percentValue: Float = percentString.toFloat() / 100f
                    dimension = Dimension.createPercent(0, percentValue).suggested(0)
                } else if (dimensionString.contains(":")) {
                    dimension = Dimension.createRatio(dimensionString)
                        .suggested(Dimension.SPREAD_DIMENSION)
                }
            }
        }
        return dimension
    }

    @Throws(CLParsingException::class)
    fun parseDimension(
        element: CLObject,
        constraintName: String?,
        state: State,
        dpToPixels: CorePixelDp?
    ): Dimension {
        val dimensionElement: CLElement? = element.get(constraintName.toString())
        var dimension: Dimension = Dimension.createFixed(0)
        if (dimensionElement is CLString) {
            dimension = parseDimensionMode(dimensionElement.content())
        } else if (dimensionElement is CLNumber) {
            dimension = Dimension.createFixed(
                state.convertDimension(dpToPixels!!.toPixels(element.getFloat(constraintName.toString())))
            )
        } else if (dimensionElement is CLObject) {
            val obj: CLObject = dimensionElement as CLObject
            val mode: String? = obj.getStringOrNull("value")
            if (mode != null) {
                dimension = parseDimensionMode(mode)
            }
            val minEl: CLElement? = obj.getOrNull("min")
            if (minEl != null) {
                if (minEl is CLNumber) {
                    val min: Float = (minEl as CLNumber).float
                    dimension.min(state.convertDimension(dpToPixels!!.toPixels(min)))
                } else if (minEl is CLString) {
                    dimension.min(Dimension.WRAP_DIMENSION)
                }
            }
            val maxEl: CLElement? = obj.getOrNull("max")
            if (maxEl != null) {
                if (maxEl is CLNumber) {
                    val max: Float = (maxEl as CLNumber).float
                    dimension.max(state.convertDimension(dpToPixels!!.toPixels(max)))
                } else if (maxEl is CLString) {
                    dimension.max(Dimension.WRAP_DIMENSION)
                }
            }
        }
        return dimension
    }

    /**
     * parse a color string
     *
     * @return -1 if it cannot parse unsigned long
     */
    fun parseColorString(value: String): Long {
        var str = value
        return if (str.startsWith("#")) {
            str = str.substring(1)
            if (str.length == 6) {
                str = "FF$str"
            }
            str.toLong(16)
        } else {
            -1L
        }
    }

    @Throws(CLParsingException::class)
    fun lookForType(element: CLObject): String? {
        val constraints: ArrayList<String> = element.names()
        for (constraintName in constraints) {
            if (constraintName.equals("type")) {
                return element.getString("type")
            }
        }
        return null
    }

    class DesignElement internal constructor(
        var id: String,
        var type: String,
        var params: HashMap<String, String>
    )

    /**
     * Provide the storage for managing Variables in the system.
     * When the json has a variable:{   } section this is used.
     */
    class LayoutVariables {
        var mMargins: MutableMap<String, Int> = mutableMapOf()
        var mGenerators: MutableMap<String, GeneratedValue> = mutableMapOf()
        var mArrayIds: MutableMap<String?, ArrayList<String?>?> = mutableMapOf()
        fun put(elementName: String, element: Int) {
            mMargins[elementName] = element
        }

        fun put(elementName: String, start: Float, incrementBy: Float) {
            if (mGenerators.containsKey(elementName)) {
                if (mGenerators[elementName] is OverrideValue) {
                    return
                }
            }
            mGenerators[elementName] =
                Generator(start, incrementBy)
        }

        fun put(
            elementName: String,
            from: Float,
            to: Float,
            step: Float,
            prefix: String?,
            postfix: String?
        ) {
            if (mGenerators.containsKey(elementName)) {
                if (mGenerators[elementName] is OverrideValue) {
                    return
                }
            }
            val generator = FiniteGenerator(from, to, step, prefix, postfix)
            mGenerators[elementName] = generator
            mArrayIds[elementName] = generator.array()
        }

        /**
         * insert an override variable
         *
         * @param elementName the name
         * @param value       the value a float
         */
        fun putOverride(elementName: String, value: Float) {
            val generator: GeneratedValue = OverrideValue(value)
            mGenerators[elementName] = generator
        }

        operator fun get(elementName: CLElement?): Float {
            if (elementName is CLString) {
                val stringValue: String = (elementName as CLString).content()
                if (mGenerators.containsKey(stringValue)) {
                    return mGenerators[stringValue]!!.value()
                }
                if (mMargins.containsKey(stringValue)) {
                    return mMargins[stringValue]?.toFloat() ?: 0f
                }
            } else if (elementName is CLNumber) {
                return (elementName as CLNumber).float
            }
            return 0f
        }

        fun getList(elementName: String?): ArrayList<String?>? {
            return if (mArrayIds.containsKey(elementName)) {
                mArrayIds[elementName]
            } else null
        }

        fun put(elementName: String?, elements: ArrayList<String?>?) {
            mArrayIds[elementName] = elements
        }
    }

    interface GeneratedValue {
        fun value(): Float
    }

    /**
     * Generate a floating point value
     */
    internal class Generator(start: Float, incrementBy: Float) : GeneratedValue {
        var mStart = 0f
        var mIncrementBy = 0f
        var mCurrent = 0f
        var mStop = false

        init {
            mStart = start
            mIncrementBy = incrementBy
            mCurrent = start
        }

        
        override fun value(): Float {
            if (!mStop) {
                mCurrent += mIncrementBy
            }
            return mCurrent
        }
    }

    /**
     * Generate values like button1, button2 etc.
     */
    internal class FiniteGenerator(
        from: Float,
        to: Float,
        step: Float,
        prefix: String?,
        postfix: String?
    ) : GeneratedValue {
        var mFrom = 0f
        var mTo = 0f
        var mStep = 0f
        var mStop = false
        var mPrefix: String
        var mPostfix: String
        var mCurrent = 0f
        var mInitial: Float
        var mMax: Float

        init {
            mFrom = from
            mTo = to
            mStep = step
            mPrefix = prefix ?: ""
            mPostfix = postfix ?: ""
            mMax = to
            mInitial = from
        }

        
        override fun value(): Float {
            if (mCurrent >= mMax) {
                mStop = true
            }
            if (!mStop) {
                mCurrent += mStep
            }
            return mCurrent
        }

        fun array(): ArrayList<String?> {
            val array: ArrayList<String?> = ArrayList()
            var value = mInitial.toInt()
            val maxInt = mMax.toInt()
            for (i in value..maxInt) {
                array.add(mPrefix + value + mPostfix)
                value += mStep.toInt()
            }
            return array
        }
    }

    internal class OverrideValue(var mValue: Float) : GeneratedValue {
        
        override fun value(): Float {
            return mValue
        }
    }

    //==================== end store variables =========================
    //==================== MotionScene =========================
    enum class MotionLayoutDebugFlags {
        NONE, SHOW_ALL, UNKNOWN
    }
}