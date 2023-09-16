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
class ConstraintLayoutStates internal constructor(
    context: TContext,
    layout: ConstraintLayout,
    resourceID: String
) {
    private val mConstraintLayout: ConstraintLayout
    var mDefaultConstraintSet: ConstraintSet? = null
    var mCurrentStateId = "" // default
    var mCurrentConstraintNumber = -1 // default
    private val mStateList = mutableMapOf<String, State>()
    private val mConstraintSetMap: MutableMap<String, ConstraintSet> = mutableMapOf()
    private var mConstraintsChangedListener: ConstraintsChangedListener? = null

    init {
        mConstraintLayout = layout
        load(context, resourceID)
    }

    /**
     * Return true if it needs to change
     * @param id
     * @param width
     * @param height
     * @return
     */
    fun needsToChange(id: String, width: Float, height: Float): Boolean {
        if (mCurrentStateId != id) {
            return true
        }
        val state = if (id == "") mStateList.valueAt(0) else mStateList[mCurrentStateId]
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
     * updateConstraints for the view with the id and width and height
     * @param id
     * @param width
     * @param height
     */
    fun updateConstraints(id: String, width: Float, height: Float) {
        if (mCurrentStateId == id) {
            val state: State?
            state = if (id == "") {
                mStateList[mStateList.keyAt(0)] // id not being used take the first
            } else {
                mStateList[mCurrentStateId]
            }
            if (mCurrentConstraintNumber != -1) {
                if (state!!.mVariants[mCurrentConstraintNumber].match(width, height)) {
                    return
                }
            }
            val match = state!!.findMatch(width, height)
            if (mCurrentConstraintNumber == match) {
                return
            }
            val constraintSet: ConstraintSet? =
                if (match == -1) mDefaultConstraintSet else state.mVariants[match].mConstraintSet
            val cid = if (match == -1) state.mConstraintID else state.mVariants[match].mConstraintID
            if (constraintSet == null) {
                return
            }
            mCurrentConstraintNumber = match
            if (mConstraintsChangedListener != null) {
                mConstraintsChangedListener!!.preLayoutChange("", cid)
            }
            constraintSet.applyTo(mConstraintLayout)
            if (mConstraintsChangedListener != null) {
                mConstraintsChangedListener!!.postLayoutChange("", cid)
            }
        } else {
            mCurrentStateId = id
            val state = mStateList[mCurrentStateId]
            val match = state!!.findMatch(width, height)
            val constraintSet: ConstraintSet? =
                if (match == -1) state.mConstraintSet else state.mVariants[match].mConstraintSet
            val cid = if (match == -1) state.mConstraintID else state.mVariants[match].mConstraintID
            if (constraintSet == null) {
                Log.v(
                    TAG, "NO Constraint set found ! id=" + id
                            + ", dim =" + width + ", " + height
                )
                return
            }
            mCurrentConstraintNumber = match
            if (mConstraintsChangedListener != null) {
                mConstraintsChangedListener!!.preLayoutChange(id, cid)
            }
            constraintSet.applyTo(mConstraintLayout)
            if (mConstraintsChangedListener != null) {
                mConstraintsChangedListener!!.postLayoutChange(id, cid)
            }
        }
    }

    fun setOnConstraintsChanged(constraintsChangedListener: ConstraintsChangedListener?) {
        mConstraintsChangedListener = constraintsChangedListener
    }

    /////////////////////////////////////////////////////////////////////////
    //      This represents one state
    /////////////////////////////////////////////////////////////////////////
    internal class State(context: TContext, parser: XmlBufferedReader) {
        var mId = ""
        var mVariants: ArrayList<Variant> = ArrayList()
        var mConstraintID = ""
        var mConstraintSet: ConstraintSet? = null

        init {
            val attrs: AttributeSet = Xml.asAttributeSet(parser)
            attrs.forEach { kvp ->
                if (kvp.key == "android_id") {
                    mId = context.getResources().getResourceId(kvp.value, mId)
                } else if (kvp.key == "constraints") {
                    mConstraintID = context.getResources().getResourceId(kvp.value, mConstraintID)
                    val type = context.getResources().getResourceType(mConstraintID)
                    val name: String = context.getResources().getResourceName(mConstraintID)
                    if ("layout".equals(type)) {
                        mConstraintSet = ConstraintSet()
                        mConstraintSet?.clone(context, mConstraintID)
                        if (I_DEBUG) {
                            Log.v(TAG, "############### mConstraintSet.load($name)")
                        }
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
        var mId = 0
        var mMinWidth = Float.NaN
        var mMinHeight = Float.NaN
        var mMaxWidth = Float.NaN
        var mMaxHeight = Float.NaN
        var mConstraintID = ""
        var mConstraintSet: ConstraintSet? = null

        init {
            val attrs: AttributeSet = Xml.asAttributeSet(parser)
            if (I_DEBUG) {
                Log.v(TAG, "############### Variant")
            }
            attrs.forEach { kvp ->
                if (kvp.key == "constraints") {
                    mConstraintID = context.getResources().getResourceId(kvp.value, mConstraintID)
                    val type = context.getResources().getResourceType(mConstraintID)
                    val name: String = context.getResources().getResourceName(mConstraintID)
                    if ("layout".equals(type)) {
                        mConstraintSet = ConstraintSet()
                        if (I_DEBUG) {
                            Log.v(TAG, "############### mConstraintSet.load($name)")
                        }
                        mConstraintSet?.clone(context, mConstraintID)
                        if (I_DEBUG) {
                            Log.v(TAG, "############### mConstraintSet.load($name)")
                        }
                    } else {
                        if (I_DEBUG) {
                            Log.v(
                                TAG, "############### id -> "
                                        + "ConstraintSet should be in this file"
                            )
                        }
                    }
                } else if (kvp.key == "heightLessThan") {
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

    /**
     * Load a constraint set from a constraintSet.xml file
     *
     * @param context    the context for the inflation
     * @param resourceId mId of xml file in res/xml/
     */
    private fun load(context: TContext, resourceId: String) {
        if (I_DEBUG) {
            Log.v(TAG, "############### ")
        }
        val res: TResources = context.getResources()
        val parser: XmlBufferedReader = res.getXml(resourceId)
        try {
            var match: Variant
            var state: State? = null
            var eventType = parser.eventType
            while (eventType != EventType.END_DOCUMENT) {
                when (eventType) {
                    EventType.START_DOCUMENT, EventType.END_ELEMENT, EventType.TEXT -> {}
                    EventType.START_ELEMENT -> {
                        val tagName: String = parser.name.localPart
                        when (tagName) {
                            "layoutDescription", "StateSet" -> {}
                            "State" -> {
                                state = State(context, parser)
                                mStateList[state.mId] = state
                            }
                            "Variant" -> {
                                match = Variant(context, parser)
                                state?.add(match)
                            }
                            "ConstraintSet" -> parseConstraintSet(context, parser)
                            else -> if (I_DEBUG) {
                                Log.v(TAG, "unknown tag $tagName")
                            }
                        }
                    }
                    else -> {}
                }
                eventType = parser.next()
            }
            //            for (Variant sizeMatch : mSizeMatchList) {
//                if (sizeMatch.mConstraintSet == null) {
//                    continue;
//                }
//                if (sizeMatch.mConstraintID != -1) {
//                    sizeMatch.mConstraintSet = mConstraintSetMap.get(sizeMatch.mConstraintID);
//                }
//            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing resource: $resourceId", e)
        }
    }

    private fun parseConstraintSet(context: TContext, parser: XmlBufferedReader) {
        val set = ConstraintSet()
        val count: Int = parser.attributeCount
        for (i in 0 until count) {
            val name: String = parser.getAttributeName(i).localPart
            val s: String = parser.getAttributeValue(i)
            if (name == null || s == null) continue
            if ("id".equals(name)) {
                var id = ""
                if (s.contains("/")) {
                    val tmp = s.substring(s.indexOf('/') + 1)
                    id = context.getResources().getIdentifier(tmp, "id", context.getPackageName())
                }
                if (id == "") {
                    if (s.length > 1) {
                        id = s.substring(1)
                    } else {
                        Log.e(TAG, "error in parsing id")
                    }
                }
                set.load(context, parser)
                if (I_DEBUG) {
                    Log.v(TAG, " id name " + context.getResources().getResourceName(id))
                }
                mConstraintSetMap[id] = set
                break
            }
        }
    }

    companion object {
        const val TAG = "ConstraintLayoutStates"
        private const val I_DEBUG = false
    }
}