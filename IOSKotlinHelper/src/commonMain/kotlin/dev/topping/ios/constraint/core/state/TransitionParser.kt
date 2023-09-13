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

import dev.topping.ios.constraint.core.motion.CustomVariable
import dev.topping.ios.constraint.core.motion.utils.TypedBundle
import dev.topping.ios.constraint.core.motion.utils.TypedValues
import dev.topping.ios.constraint.core.parser.*
import dev.topping.ios.constraint.core.state.ConstraintSetParser.parseColorString
import dev.topping.ios.constraint.isNaN

/**
 * Contains code for Parsing Transitions
 */
object TransitionParser {
    /**
     * Parse a JSON string of a Transition and insert it into the Transition object
     *
     * @param json       Transition Any to parse.
     * @param transition Transition Any to write transition to
     */
    @Deprecated(
        """dpToPixel is unused now
      """
    )
    @Throws(CLParsingException::class)
    fun parse(json: CLObject, transition: Transition, dpToPixel: CorePixelDp?) {
        parse(json, transition)
    }

    /**
     * Parse a JSON string of a Transition and insert it into the Transition object
     *
     * @param json       Transition Any to parse.
     * @param transition Transition Any to write transition to
     * @hide
     */
    @Throws(CLParsingException::class)
    fun parse(json: CLObject, transition: Transition) {
        val pathMotionArc: String? = json.getStringOrNull("pathMotionArc")
        val bundle = TypedBundle()
        var setBundle = false
        if (pathMotionArc != null) {
            setBundle = true
            when (pathMotionArc) {
                "none" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 0)
                "startVertical" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 1)
                "startHorizontal" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 2)
                "flip" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 3)
                "below" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 4)
                "above" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 5)
            }
        }
        // TODO: Add duration
        val interpolator: String? = json.getStringOrNull("interpolator")
        if (interpolator != null) {
            setBundle = true
            bundle.add(TypedValues.TransitionType.TYPE_INTERPOLATOR, interpolator)
        }
        val staggered: Float = json.getFloatOrNaN("staggered")
        if (!Float.isNaN(staggered)) {
            setBundle = true
            bundle.add(TypedValues.TransitionType.TYPE_STAGGERED, staggered)
        }
        if (setBundle) {
            transition.setTransitionProperties(bundle)
        }
        val onSwipe: CLContainer? = json.getObjectOrNull("onSwipe")
        if (onSwipe != null) {
            parseOnSwipe(onSwipe, transition)
        }
        parseKeyFrames(json, transition)
    }

    private fun parseOnSwipe(onSwipe: CLContainer, transition: Transition) {
        val anchor: String? = onSwipe.getStringOrNull("anchor")
        val side = map(onSwipe.getStringOrNull("side"), *Transition.OnSwipe.SIDES)
        val direction = map(
            onSwipe.getStringOrNull("direction"),
            *Transition.OnSwipe.DIRECTIONS
        )
        val scale: Float = onSwipe.getFloatOrNaN("scale")
        val threshold: Float = onSwipe.getFloatOrNaN("threshold")
        val maxVelocity: Float = onSwipe.getFloatOrNaN("maxVelocity")
        val maxAccel: Float = onSwipe.getFloatOrNaN("maxAccel")
        val limitBounds: String? = onSwipe.getStringOrNull("limitBounds")
        val autoCompleteMode = map(onSwipe.getStringOrNull("mode"), *Transition.OnSwipe.MODE)
        val touchUp = map(onSwipe.getStringOrNull("touchUp"), *Transition.OnSwipe.TOUCH_UP)
        val springMass: Float = onSwipe.getFloatOrNaN("springMass")
        val springStiffness: Float = onSwipe.getFloatOrNaN("springStiffness")
        val springDamping: Float = onSwipe.getFloatOrNaN("springDamping")
        val stopThreshold: Float = onSwipe.getFloatOrNaN("stopThreshold")
        val springBoundary = map(
            onSwipe.getStringOrNull("springBoundary"),
            *Transition.OnSwipe.BOUNDARY
        )
        val around: String? = onSwipe.getStringOrNull("around")
        val swipe: Transition.OnSwipe = transition.createOnSwipe()
        swipe.setAnchorId(anchor)
        swipe.setAnchorSide(side)
        swipe.setDragDirection(direction)
        swipe.setDragScale(scale)
        swipe.setDragThreshold(threshold)
        swipe.setMaxVelocity(maxVelocity)
        swipe.setMaxAcceleration(maxAccel)
        swipe.setLimitBoundsTo(limitBounds)
        swipe.setAutoCompleteMode(autoCompleteMode)
        swipe.setOnTouchUp(touchUp)
        swipe.setSpringMass(springMass)
        swipe.setSpringStiffness(springStiffness)
        swipe.setSpringDamping(springDamping)
        swipe.setSpringStopThreshold(stopThreshold)
        swipe.setSpringBoundary(springBoundary)
        swipe.setRotationCenterId(around)
    }

    private fun map(`val`: String?, vararg types: String): Int {
        for (i in types.indices) {
            if (types[i] == `val`) {
                return i
            }
        }
        return 0
    }

    private fun map(bundle: TypedBundle, type: Int, `val`: String, vararg types: String) {
        for (i in types.indices) {
            if (types[i] == `val`) {
                bundle.add(type, i)
            }
        }
    }

    /**
     * Parses `KeyFrames` attributes from the [CLObject] into [Transition].
     *
     * @param transitionCLObject the CLObject for the root transition json
     * @param transition         core object that holds the state of the Transition
     */
    @Throws(CLParsingException::class)
    fun parseKeyFrames(transitionCLObject: CLObject, transition: Transition) {
        val keyframes: CLContainer = transitionCLObject.getObjectOrNull("KeyFrames") ?: return
        val keyPositions: CLArray? = keyframes.getArrayOrNull("KeyPositions")
        if (keyPositions != null) {
            for (i in 0 until keyPositions.size()) {
                val keyPosition: CLElement? = keyPositions.get(i)
                if (keyPosition is CLObject) {
                    parseKeyPosition(keyPosition as CLObject, transition)
                }
            }
        }
        val keyAttributes: CLArray? = keyframes.getArrayOrNull("KeyAttributes")
        if (keyAttributes != null) {
            for (i in 0 until keyAttributes.size()) {
                val keyAttribute: CLElement? = keyAttributes.get(i)
                if (keyAttribute is CLObject) {
                    parseKeyAttribute(keyAttribute as CLObject, transition)
                }
            }
        }
        val keyCycles: CLArray? = keyframes.getArrayOrNull("KeyCycles")
        if (keyCycles != null) {
            for (i in 0 until keyCycles.size()) {
                val keyCycle: CLElement? = keyCycles.get(i)
                if (keyCycle is CLObject) {
                    parseKeyCycle(keyCycle as CLObject, transition)
                }
            }
        }
    }

    @Throws(CLParsingException::class)
    private fun parseKeyPosition(
        keyPosition: CLObject,
        transition: Transition
    ) {
        val bundle = TypedBundle()
        val targets: CLArray = keyPosition.getArray("target")
        val frames: CLArray = keyPosition.getArray("frames")
        val percentX: CLArray? = keyPosition.getArrayOrNull("percentX")
        val percentY: CLArray? = keyPosition.getArrayOrNull("percentY")
        val percentWidth: CLArray? = keyPosition.getArrayOrNull("percentWidth")
        val percentHeight: CLArray? = keyPosition.getArrayOrNull("percentHeight")
        val pathMotionArc: String? = keyPosition.getStringOrNull("pathMotionArc")
        val transitionEasing: String? = keyPosition.getStringOrNull("transitionEasing")
        val curveFit: String? = keyPosition.getStringOrNull("curveFit")
        var type: String? = keyPosition.getStringOrNull("type")
        if (type == null) {
            type = "parentRelative"
        }
        if (percentX != null && frames.size() != percentX.size()) {
            return
        }
        if (percentY != null && frames.size() != percentY.size()) {
            return
        }
        for (i in 0 until targets.size()) {
            val target: String = targets.getString(i)
            val pos_type = map(type, "deltaRelative", "pathRelative", "parentRelative")
            bundle.clear()
            bundle.add(TypedValues.PositionType.TYPE_POSITION_TYPE, pos_type)
            if (curveFit != null) {
                map(
                    bundle, TypedValues.PositionType.TYPE_CURVE_FIT, curveFit,
                    "spline", "linear"
                )
            }
            bundle.addIfNotNull(TypedValues.PositionType.TYPE_TRANSITION_EASING, transitionEasing)
            if (pathMotionArc != null) {
                map(
                    bundle, TypedValues.PositionType.TYPE_PATH_MOTION_ARC, pathMotionArc,
                    "none", "startVertical", "startHorizontal", "flip", "below", "above"
                )
            }
            for (j in 0 until frames.size()) {
                val frame: Int = frames.getInt(j)
                bundle.add(TypedValues.TYPE_FRAME_POSITION, frame)
                TransitionParser[bundle, TypedValues.PositionType.TYPE_PERCENT_X, percentX] = j
                TransitionParser[bundle, TypedValues.PositionType.TYPE_PERCENT_Y, percentY] = j
                TransitionParser[bundle, TypedValues.PositionType.TYPE_PERCENT_WIDTH, percentWidth] =
                    j
                TransitionParser[bundle, TypedValues.PositionType.TYPE_PERCENT_HEIGHT, percentHeight] =
                    j
                transition.addKeyPosition(target, bundle)
            }
        }
    }

    @Throws(CLParsingException::class)
    private operator fun set(
        bundle: TypedBundle, type: Int,
        array: CLArray?, index: Int
    ) {
        if (array != null) {
            bundle.add(type, array.getFloat(index))
        }
    }

    @Throws(CLParsingException::class)
    private fun parseKeyAttribute(
        keyAttribute: CLObject,
        transition: Transition
    ) {
        val targets: CLArray = keyAttribute.getArrayOrNull("target") ?: return
        val frames: CLArray = keyAttribute.getArrayOrNull("frames") ?: return
        val transitionEasing: String? = keyAttribute.getStringOrNull("transitionEasing")
        // These present an ordered list of attributes that might be used in a keyCycle
        val attrNames = arrayOf(
            TypedValues.AttributesType.S_SCALE_X,
            TypedValues.AttributesType.S_SCALE_Y,
            TypedValues.AttributesType.S_TRANSLATION_X,
            TypedValues.AttributesType.S_TRANSLATION_Y,
            TypedValues.AttributesType.S_TRANSLATION_Z,
            TypedValues.AttributesType.S_ROTATION_X,
            TypedValues.AttributesType.S_ROTATION_Y,
            TypedValues.AttributesType.S_ROTATION_Z,
            TypedValues.AttributesType.S_ALPHA
        )
        val attrIds = intArrayOf(
            TypedValues.AttributesType.TYPE_SCALE_X,
            TypedValues.AttributesType.TYPE_SCALE_Y,
            TypedValues.AttributesType.TYPE_TRANSLATION_X,
            TypedValues.AttributesType.TYPE_TRANSLATION_Y,
            TypedValues.AttributesType.TYPE_TRANSLATION_Z,
            TypedValues.AttributesType.TYPE_ROTATION_X,
            TypedValues.AttributesType.TYPE_ROTATION_Y,
            TypedValues.AttributesType.TYPE_ROTATION_Z,
            TypedValues.AttributesType.TYPE_ALPHA
        )
        // if true scale the values from pixels to dp
        val scaleTypes = booleanArrayOf(
            false,
            false,
            true,
            true,
            true,
            false,
            false,
            false,
            false
        )
        val bundles: Array<TypedBundle?> = arrayOfNulls(frames.size())
        var customVars: Array<Array<CustomVariable?>>? = null
        for (i in 0 until frames.size()) {
            bundles[i] = TypedBundle()
        }
        for (k in attrNames.indices) {
            val attrName = attrNames[k]
            val attrId = attrIds[k]
            val scale = scaleTypes[k]
            val arrayValues: CLArray? = keyAttribute.getArrayOrNull(attrName)
            // array must contain one per frame
            if (arrayValues != null && arrayValues.size() != bundles.size) {
                throw CLParsingException(
                    "incorrect size for " + attrName + " array, "
                            + "not matching targets array!", keyAttribute
                )
            }
            if (arrayValues != null) {
                for (i in bundles.indices) {
                    var value: Float = arrayValues.getFloat(i)
                    if (scale) {
                        value = transition.mToPixel.toPixels(value)
                    }
                    bundles[i]!!.add(attrId, value)
                }
            } else {
                var value: Float = keyAttribute.getFloatOrNaN(attrName)
                if (!Float.isNaN(value)) {
                    if (scale) {
                        value = transition.mToPixel.toPixels(value)
                    }
                    for (i in bundles.indices) {
                        bundles[i]!!.add(attrId, value)
                    }
                }
            }
        }
        // Support for custom attributes in KeyAttributes
        val customElement: CLElement? = keyAttribute.getOrNull("custom")
        if (customElement != null && customElement is CLObject) {
            val customObj: CLObject = customElement as CLObject
            val n: Int = customObj.size()
            customVars =
                Array<Array<CustomVariable?>>(frames.size()) { arrayOfNulls<CustomVariable>(n) }
            for (i in 0 until n) {
                val key: CLKey = customObj.get(i) as CLKey
                val customName: String = key.content()
                if (key.value is CLArray) {
                    val arrayValues: CLArray = key.value as CLArray
                    val vSize: Int = arrayValues.size()
                    if (vSize == bundles.size && vSize > 0) {
                        if (arrayValues.get(0) is CLNumber) {
                            for (j in bundles.indices) {
                                customVars[j][i] = CustomVariable(
                                    customName,
                                    TypedValues.Custom.TYPE_FLOAT,
                                    arrayValues.get(j)?.float ?: 0f
                                )
                            }
                        } else {  // since it is not a number switching to custom color parsing
                            for (j in bundles.indices) {
                                val color: Long = parseColorString(arrayValues.get(j)?.content().toString())
                                if (color != -1L) {
                                    customVars[j][i] = CustomVariable(
                                        customName,
                                        TypedValues.Custom.TYPE_COLOR, color.toInt()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val value: CLElement? = key.value
                    if (value is CLNumber) {
                        val fValue: Float = value.float
                        for (j in bundles.indices) {
                            customVars[j][i] = CustomVariable(
                                customName,
                                TypedValues.Custom.TYPE_FLOAT,
                                fValue
                            )
                        }
                    } else {
                        val cValue: Long = parseColorString(value?.content().toString())
                        if (cValue != -1L) {
                            for (j in bundles.indices) {
                                customVars[j][i] = CustomVariable(
                                    customName,
                                    TypedValues.Custom.TYPE_COLOR, cValue.toInt()
                                )
                            }
                        }
                    }
                }
            }
        }
        val curveFit: String? = keyAttribute.getStringOrNull("curveFit")
        for (i in 0 until targets.size()) {
            for (j in bundles.indices) {
                val target: String = targets.getString(i)
                val bundle: TypedBundle = bundles[j]!!
                if (curveFit != null) {
                    bundle.add(
                        TypedValues.PositionType.TYPE_CURVE_FIT,
                        map(curveFit, "spline", "linear")
                    )
                }
                bundle.addIfNotNull(
                    TypedValues.PositionType.TYPE_TRANSITION_EASING,
                    transitionEasing
                )
                val frame: Int = frames.getInt(j)
                bundle.add(TypedValues.TYPE_FRAME_POSITION, frame)
                transition.addKeyAttribute(target, bundle, customVars?.get(j))
            }
        }
    }

    @Throws(CLParsingException::class)
    private fun parseKeyCycle(
        keyCycleData: CLObject,
        transition: Transition
    ) {
        val targets: CLArray = keyCycleData.getArray("target")
        val frames: CLArray = keyCycleData.getArray("frames")
        val transitionEasing: String? = keyCycleData.getStringOrNull("transitionEasing")
        // These present an ordered list of attributes that might be used in a keyCycle
        val attrNames = arrayOf<String>(
            TypedValues.CycleType.S_SCALE_X,
            TypedValues.CycleType.S_SCALE_Y,
            TypedValues.CycleType.S_TRANSLATION_X,
            TypedValues.CycleType.S_TRANSLATION_Y,
            TypedValues.CycleType.S_TRANSLATION_Z,
            TypedValues.CycleType.S_ROTATION_X,
            TypedValues.CycleType.S_ROTATION_Y,
            TypedValues.CycleType.S_ROTATION_Z,
            TypedValues.CycleType.S_ALPHA,
            TypedValues.CycleType.S_WAVE_PERIOD,
            TypedValues.CycleType.S_WAVE_OFFSET,
            TypedValues.CycleType.S_WAVE_PHASE
        )
        val attrIds = intArrayOf(
            TypedValues.CycleType.TYPE_SCALE_X,
            TypedValues.CycleType.TYPE_SCALE_Y,
            TypedValues.CycleType.TYPE_TRANSLATION_X,
            TypedValues.CycleType.TYPE_TRANSLATION_Y,
            TypedValues.CycleType.TYPE_TRANSLATION_Z,
            TypedValues.CycleType.TYPE_ROTATION_X,
            TypedValues.CycleType.TYPE_ROTATION_Y,
            TypedValues.CycleType.TYPE_ROTATION_Z,
            TypedValues.CycleType.TYPE_ALPHA,
            TypedValues.CycleType.TYPE_WAVE_PERIOD,
            TypedValues.CycleType.TYPE_WAVE_OFFSET,
            TypedValues.CycleType.TYPE_WAVE_PHASE
        )
        // type 0 the values are used as.
        // type 1 the value is scaled from dp to pixels.
        // type 2 are scaled if the system has another type 1.
        val scaleTypes = intArrayOf(
            0,
            0,
            1,
            1,
            1,
            0,
            0,
            0,
            0,
            0,
            2,
            0
        )

//  TODO S_WAVE_SHAPE S_CUSTOM_WAVE_SHAPE
        val bundles: Array<TypedBundle?> = arrayOfNulls<TypedBundle>(frames.size())
        for (i in bundles.indices) {
            bundles[i] = TypedBundle()
        }
        var scaleOffset = false
        for (k in attrNames.indices) {
            if (keyCycleData.has(attrNames[k]) && scaleTypes[k] == 1) {
                scaleOffset = true
            }
        }
        for (k in attrNames.indices) {
            val attrName = attrNames[k]
            val attrId = attrIds[k]
            val scale = scaleTypes[k]
            val arrayValues: CLArray? = keyCycleData.getArrayOrNull(attrName)
            // array must contain one per frame
            if (arrayValues != null && arrayValues.size() != bundles.size) {
                throw CLParsingException(
                    "incorrect size for \$attrName array, "
                            + "not matching targets array!", keyCycleData
                )
            }
            if (arrayValues != null) {
                for (i in bundles.indices) {
                    var value: Float = arrayValues.getFloat(i)
                    if (scale == 1) {
                        value = transition.mToPixel.toPixels(value)
                    } else if (scale == 2 && scaleOffset) {
                        value = transition.mToPixel.toPixels(value)
                    }
                    bundles[i]?.add(attrId, value)
                }
            } else {
                var value: Float = keyCycleData.getFloatOrNaN(attrName)
                if (!Float.isNaN(value)) {
                    if (scale == 1) {
                        value = transition.mToPixel.toPixels(value)
                    } else if (scale == 2 && scaleOffset) {
                        value = transition.mToPixel.toPixels(value)
                    }
                    for (i in bundles.indices) {
                        bundles[i]?.add(attrId, value)
                    }
                }
            }
        }
        val curveFit: String? = keyCycleData.getStringOrNull(TypedValues.CycleType.S_CURVE_FIT)
        val easing: String? = keyCycleData.getStringOrNull(TypedValues.CycleType.S_EASING)
        val waveShape: String? = keyCycleData.getStringOrNull(TypedValues.CycleType.S_WAVE_SHAPE)
        val customWave: String? =
            keyCycleData.getStringOrNull(TypedValues.CycleType.S_CUSTOM_WAVE_SHAPE)
        for (i in 0 until targets.size()) {
            for (j in bundles.indices) {
                val target: String = targets.getString(i)
                val bundle: TypedBundle? = bundles[j]
                if (curveFit != null) {
                    when (curveFit) {
                        "spline" -> bundle?.add(TypedValues.CycleType.TYPE_CURVE_FIT, 0)
                        "linear" -> bundle?.add(TypedValues.CycleType.TYPE_CURVE_FIT, 1)
                    }
                }
                bundle?.addIfNotNull(
                    TypedValues.PositionType.TYPE_TRANSITION_EASING,
                    transitionEasing
                )
                if (easing != null) {
                    bundle?.add(TypedValues.CycleType.TYPE_EASING, easing)
                }
                if (waveShape != null) {
                    bundle?.add(TypedValues.CycleType.TYPE_WAVE_SHAPE, waveShape)
                }
                if (customWave != null) {
                    bundle?.add(TypedValues.CycleType.TYPE_CUSTOM_WAVE_SHAPE, customWave)
                }
                val frame: Int = frames.getInt(j)
                bundle?.add(TypedValues.TYPE_FRAME_POSITION, frame)
                transition.addKeyCycle(target, bundle)
            }
        }
    }
}