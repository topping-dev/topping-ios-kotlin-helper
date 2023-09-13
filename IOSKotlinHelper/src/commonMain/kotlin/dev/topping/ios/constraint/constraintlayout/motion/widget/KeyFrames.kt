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
package dev.topping.ios.constraint.constraintlayout.motion.widget

import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.Log
import dev.topping.ios.constraint.Xml
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintAttribute
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlBufferedReader
import kotlin.reflect.KClass

/**
 * The parses the KeyFrame structure in a MotionScene xml
 *
 */
class KeyFrames {
    private val mFramesMap: MutableMap<String, MutableList<Key>> = mutableMapOf()

    /**
     * Add a key to this set of keyframes
     * @param key
     */
    fun addKey(key: Key) {
        if (!mFramesMap.containsKey(key.mTargetId)) {
            mFramesMap[key.mTargetId] = mutableListOf()
        }
        val frames: MutableList<Key> = mFramesMap[key.mTargetId]!!
        frames.add(key)
    }

    constructor() {}
    constructor(context: TContext, parser: XmlBufferedReader) {
        var tagName: String? = null
        try {
            var key: Key? = null
            var eventType: EventType = parser.eventType
            while (eventType != EventType.END_DOCUMENT) {
                when (eventType) {
                    EventType.START_DOCUMENT -> {}
                    EventType.START_ELEMENT -> {
                        tagName = parser.localName
                        if (sKeyMakers.containsKey(tagName)) {
                            try {
                                val keyMaker: KClass<*>? = sKeyMakers[tagName]
                                if (keyMaker != null) {
                                    key = KeyFrames.construct(keyMaker) as Key?
                                    key?.load(context, Xml.asAttributeSet(parser))
                                    addKey(key!!)
                                } else {
                                    throw NullPointerException(
                                        "Keymaker for $tagName not found"
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "unable to create $e")
                            }
                        } else if (tagName == CUSTOM_ATTRIBUTE) {
                            if (key?.mCustomConstraints != null) {
                                ConstraintAttribute.parse(context, parser, key.mCustomConstraints!!)
                            }
                        } else if (tagName == CUSTOM_METHOD) {
                            if (key?.mCustomConstraints != null) {
                                ConstraintAttribute.parse(context, parser, key.mCustomConstraints!!)
                            }
                        }
                    }
                    EventType.END_ELEMENT -> if ("KeyFrameSet" == parser.localName) {
                        return
                    }
                    EventType.TEXT -> {}
                    else -> {}
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML resource $e")
        }
    }

    /**
     * Do not filter the set by matches
     * @param motionController
     */
    fun addAllFrames(motionController: MotionController) {
        val list: MutableList<Key>? = mFramesMap[UNSET_ID]
        if (list != null) {
            motionController.addKeys(list)
        }
    }

    /**
     * add the key frames to the motion controller
     * @param motionController
     */
    fun addFrames(motionController: MotionController) {
        var list: MutableList<Key>? = mFramesMap[motionController.mId]
        if (list != null) {
            motionController.addKeys(list)
        }
        list = mFramesMap[UNSET_ID]
        if (list != null) {
            for (key in list) {
                val tag: String? =
                    (motionController.mView?.getLayoutParams() as ConstraintLayout.LayoutParams).constraintTag
                if (key.matches(tag)) {
                    motionController.addKey(key)
                }
            }
        }
    }

    val keys: MutableSet<String>
        get() = mFramesMap.keys

    /**
     * Get the list of keyframes given and ID
     * @param id
     * @return
     */
    fun getKeyFramesForView(id: String): MutableList<Key> {
        return mFramesMap[id]!!
    }

    companion object {
        val UNSET = ConstraintLayout.LayoutParams.UNSET
        val UNSET_ID = ConstraintLayout.LayoutParams.UNSET_ID
        private const val CUSTOM_METHOD = "CustomMethod"
        private const val CUSTOM_ATTRIBUTE = "CustomAttribute"
        var sKeyMakers: MutableMap<String, KClass<*>> = mutableMapOf()
        private const val TAG = "KeyFrames"

        init {
            try {
                sKeyMakers[KeyAttributes.NAME] = KeyAttributes::class
                sKeyMakers[KeyPosition.NAME] = KeyPosition::class
                sKeyMakers[KeyCycle.NAME] = KeyCycle::class
                sKeyMakers[KeyTimeCycle.NAME] = KeyTimeCycle::class
                sKeyMakers[KeyTrigger.NAME] = KeyTrigger::class
            } catch (e: Exception) {
                Log.e(TAG, "unable to load $e")
            }
        }

        fun construct(cls: KClass<*>) : Any? {
            return when(cls) {
                KeyAttributes::class -> KeyAttributes()
                KeyPosition::class -> KeyPosition()
                KeyCycle::class -> KeyCycle()
                KeyTimeCycle::class -> KeyTimeCycle()
                KeyTrigger::class -> KeyTrigger()
                else -> null
            }
        }

        fun name(viewId: String, context: TContext): String {
            return context.getResources().getResourceEntryName(viewId)
        }
    }
}