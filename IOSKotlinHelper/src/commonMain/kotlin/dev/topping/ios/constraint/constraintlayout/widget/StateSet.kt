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
package dev.topping.ios.constraint.constraintlayout.widget

import dev.topping.ios.constraint.*
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.localPart

/**
 *
 */
class StateSet(val context: TContext, val attrs: AttributeSet, val parser: XmlBufferedReader) {
    var mDefaultState = ""
    var mCurrentStateId = "" // default
    var mCurrentConstraintNumber = -1 // default
    private val mStateList = mutableMapOf<String, State>()

    private var mConstraintsChangedListener: ConstraintsChangedListener? = null

    /**
     * Parse a StateSet
     * @param context
     * @param parser
     */
    init {
        load(context, parser)
    }

    /**
     * Load a constraint set from a constraintSet.xml file
     *
     * @param context    the context for the inflation
     * @param parser  mId of xml file in res/xml/
     */
    private fun load(context: TContext, parser: XmlBufferedReader) {
        if (I_DEBUG) {
            Log.v(TAG, "#########load stateSet###### ")
        }
        // Parse the stateSet attributes
        val attrs: AttributeSet = Xml.asAttributeSet(parser)
        mDefaultState = context.getResources().getResourceId(attrs["defaultState"] ?: "", mDefaultState)
        try {
            var match: Variant
            var state: State? = null
            var eventType = parser.eventType
            while (eventType != EventType.END_DOCUMENT) {
                when (eventType) {
                    EventType.START_DOCUMENT, EventType.TEXT -> {}
                    EventType.START_ELEMENT -> {
                        val tagName: String = parser.name.localPart
                        when (tagName) {
                            "LayoutDescription" -> {}
                            "StateSet" -> {}
                            "State" -> {
                                state = State(context, parser)
                                mStateList[state.mId] = state
                            }
                            "Variant" -> {
                                match = Variant(context, parser)
                                state?.add(match)
                            }
                            else -> if (I_DEBUG) {
                                Log.v(TAG, "unknown tag $tagName")
                            }
                        }
                    }
                    EventType.END_ELEMENT -> if ("StateSet" == parser.name.localPart) {
                        if (I_DEBUG) {
                            Log.v(TAG, "############ finished parsing state set")
                        }
                        return
                    }
                    else -> {}
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML resource", e)
        }
    }

    /**
     * will the layout need to change
     * @param id
     * @param width
     * @param height
     * @return
     */
    fun needsToChange(id: String, width: Float, height: Float): Boolean {
        if (mCurrentStateId != id) {
            return true
        }
        val state = if (id == "") mStateList.values.toTypedArray().get(0) else mStateList[mCurrentStateId]
        if (mCurrentConstraintNumber != -1) {
            if (state!!.mVariants[mCurrentConstraintNumber].match(width, height)) {
                return false
            }
        }
        return if (mCurrentConstraintNumber == state!!.findMatch(width, height)) {
            false
        } else true
    }

    /**
     * listen for changes in constraintSet
     * @param constraintsChangedListener
     */
    fun setOnConstraintsChanged(constraintsChangedListener: ConstraintsChangedListener?) {
        mConstraintsChangedListener = constraintsChangedListener
    }

    /**
     * Get the constraint id for a state
     * @param id
     * @param width
     * @param height
     * @return
     */
    fun stateGetConstraintID(id: String, width: Int, height: Int): String {
        return updateConstraints("", id, width.toFloat(), height.toFloat())
    }

    /**
     * converts a state to a constraintSet
     *
     * @param currentConstrainSettId
     * @param stateId
     * @param width
     * @param height
     * @return
     */
    fun convertToConstraintSet(
        currentConstrainSettId: String,
        stateId: String,
        width: Float,
        height: Float
    ): String {
        val state = mStateList[stateId] ?: return stateId
        return if (width == -1f || height == -1f) {            // for the case without width/height matching
            if (state.mConstraintID == currentConstrainSettId) {
                return currentConstrainSettId
            }
            for (mVariant in state.mVariants) {
                if (currentConstrainSettId == mVariant.mConstraintID) {
                    return currentConstrainSettId
                }
            }
            state.mConstraintID
        } else {
            var match: Variant? = null
            for (mVariant in state.mVariants) {
                if (mVariant.match(width, height)) {
                    if (currentConstrainSettId == mVariant.mConstraintID) {
                        return currentConstrainSettId
                    }
                    match = mVariant
                }
            }
            match?.mConstraintID ?: state.mConstraintID
        }
    }

    /**
     * Update the Constraints
     * @param currentId
     * @param id
     * @param width
     * @param height
     * @return
     */
    fun updateConstraints(currentId: String, id: String, width: Float, height: Float): String {
        return if (currentId == id) {
            val state: State?
            state = if (id == "") {
                mStateList.values.toTypedArray().get(0) // id not being used take the first
            } else {
                mStateList[mCurrentStateId]
            }
            if (state == null) {
                return ""
            }
            if (mCurrentConstraintNumber != -1) {
                if (state.mVariants[currentId.toInt()].match(width, height)) {
                    return currentId
                }
            }
            val match = state.findMatch(width, height)
            if (currentId.toInt() == match) {
                return currentId
            }
            if (match == -1) state.mConstraintID else state.mVariants[match].mConstraintID
        } else {
            val state = mStateList[id] ?: return ""
            val match = state.findMatch(width, height)
            if (match == -1) state.mConstraintID else state.mVariants[match].mConstraintID
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //      This represents one state
    /////////////////////////////////////////////////////////////////////////
    internal class State(context: TContext, parser: XmlBufferedReader) {
        var mId = ""
        var mVariants: ArrayList<Variant> = ArrayList()
        var mConstraintID = ""
        var mIsLayout = false

        init {
            val attrs: AttributeSet = Xml.asAttributeSet(parser)
            attrs.forEach { kvp ->
                if (kvp.key == "android_id") {
                    mId = context.getResources().getResourceId(kvp.value, mId)
                } else if (kvp.key == "constraints") {
                    mConstraintID = context.getResources().getResourceId(kvp.value, mConstraintID)
                    val type: Int = context.getResources().getResourceType(mConstraintID)
                    val name: String =
                        context.getResources().getResourceName(mConstraintID)
                    if (TypedValue.TYPE_LAYOUT == type) {
                        mIsLayout = true
                    }
                }
            }
        }

        fun add(size: Variant) {
            mVariants.add(size)
        }

        fun findMatch(width: Float, height: Float): Int {
            for (i in 0 until mVariants.size) {
                if (mVariants[i].match(width, height)) {
                    return i
                }
            }
            return -1
        }
    }

    internal class Variant(context: TContext, parser: XmlBufferedReader) {
        var mId = ""
        var mMinWidth = Float.NaN
        var mMinHeight = Float.NaN
        var mMaxWidth = Float.NaN
        var mMaxHeight = Float.NaN
        var mConstraintID = ""
        var mIsLayout = false

        init {
            val attrs: AttributeSet = Xml.asAttributeSet(parser)
            attrs.forEach { kvp ->
                if (kvp.key == "constraints") {
                    mConstraintID = context.getResources().getResourceId(kvp.value, mConstraintID)
                    val type = context.getResources().getResourceType(mConstraintID)
                    val name: String =
                        context.getResources().getResourceName(mConstraintID)
                    if (TypedValue.TYPE_LAYOUT == (type)) {
                        mIsLayout = true
                    }
                } else if (kvp.key == "region_heightLessThan") {
                    mMaxHeight = context.getResources().getDimension(kvp.value, mMaxHeight)
                } else if (kvp.key == "region_heightMoreThan") {
                    mMinHeight = context.getResources().getDimension(kvp.value, mMinHeight)
                } else if (kvp.key == "region_widthLessThan") {
                    mMaxWidth = context.getResources().getDimension(kvp.value, mMaxWidth)
                } else if (kvp.key == "region_widthMoreThan") {
                    mMinWidth = context.getResources().getDimension(kvp.value, mMinWidth)
                } else {
                    Log.v(TAG, "Unknown tag")
                }
            }

            if (I_DEBUG) {
                Log.v(TAG, "############### Variant")
                if (!Float.isNaN(mMinWidth)) {
                    Log.v(TAG, "############### Variant mMinWidth $mMinWidth")
                }
                if (!Float.isNaN(mMinHeight)) {
                    Log.v(TAG, "############### Variant mMinHeight $mMinHeight")
                }
                if (!Float.isNaN(mMaxWidth)) {
                    Log.v(TAG, "############### Variant mMaxWidth $mMaxWidth")
                }
                if (!Float.isNaN(mMaxHeight)) {
                    Log.v(TAG, "############### Variant mMinWidth $mMaxHeight")
                }
            }
        }

        fun match(widthDp: Float, heightDp: Float): Boolean {
            if (I_DEBUG) {
                Log.v(
                    TAG,
                    "width = " + widthDp.toInt() + " < " + mMinWidth + " && " + widthDp.toInt() + " > " + mMaxWidth
                            + " height = " + heightDp.toInt() + " < " + mMinHeight + " && " + heightDp.toInt() + " > " + mMaxHeight
                )
            }
            if (!Float.isNaN(mMinWidth)) {
                if (widthDp < mMinWidth) return false
            }
            if (!Float.isNaN(mMinHeight)) {
                if (heightDp < mMinHeight) return false
            }
            if (!Float.isNaN(mMaxWidth)) {
                if (widthDp > mMaxWidth) return false
            }
            if (!Float.isNaN(mMaxHeight)) {
                if (heightDp > mMaxHeight) return false
            }
            return true
        }
    }

    companion object {
        const val TAG = "ConstraintLayoutStates"
        private const val I_DEBUG = false
    }
}