/*
 * Copyright (C) 2021 The Android Open Source Project
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
import dev.topping.ios.constraint.core.widgets.ConstraintWidget
import dev.topping.ios.constraint.core.widgets.ConstraintWidgetContainer
import dev.topping.ios.constraint.core.widgets.Helper
import dev.topping.ios.constraint.core.widgets.HelperWidget

/**
 *
 * **Added in 1.1**
 *
 *
 * This class manages a set of referenced widgets. HelperWidget objects can be
 * created to act upon the set
 * of referenced widgets. The difference between `ConstraintHelper` and
 * `ViewGroup` is that
 * multiple `ConstraintHelper` can reference the same widgets.
 *
 *
 * Widgets are referenced by being added to a comma separated list of ids, e.g.:
 * <pre>
 * `<dev.topping.ios.constraint.constraintlayout.widget.Barrier
 * android:id="@+id/barrier"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * app:barrierDirection="start"
 * app:constraint_referenced_ids="button1,button2" />
` *
</pre> *
 *
 */
abstract class ConstraintHelper(val myContext: TContext?, val self: TView) {
    /**
     *
     */
    protected var mIds = Array(32){""}

    /**
     *
     */
    protected var mCount = 0

    /**
     *
     */
    protected var mHelperWidget: Helper? = null

    /**
     *
     */
    protected var mUseViewMeasure = false

    /**
     *
     */
    protected var mReferenceIds: String? = null

    /**
     *
     */
    protected var mReferenceTags: String? = null

    /**
     *
     */
    private var mViews: Array<TView?>? = null
    protected var mMap: MutableMap<String, String?> = mutableMapOf()

    init {
        self.setParentType(this)
        mReferenceIds = self.getObjCProperty("constraint_referenced_ids") as String?
        setIds(mReferenceIds)
        mReferenceTags = self.getObjCProperty("constraint_referenced_tags") as String?
        setReferenceTags(mReferenceTags)
        self.swizzleFunction("onAttachedToWindow") { sup, params ->
            onAttachedToWindow(sup)
            0
        }
        self.swizzleFunction("onMeasure") { sup, params ->
            var args = params as Array<Any>
            onMeasure(sup, args[0] as Int, args[1] as Int)
            0
        }
        self.swizzleFunction("setTag") { sup, params ->
            var args = params as Array<Any?>
            setTag(sup, args[0] as String, args[1])
            0
        }
    }

    protected open fun onAttachedToWindow(sup: TView?) {
        sup?.onAttachedToWindow()
        if (mReferenceIds != null) {
            setIds(mReferenceIds)
        }
        if (mReferenceTags != null) {
            setReferenceTags(mReferenceTags)
        }
    }

    /**
     * Add a view to the helper. The referenced view need to be a child of the helper's parent.
     * The view also need to have its id set in order to be added.
     *
     * @param view
     */
    fun addView(view: TView) {
        if (view === this) {
            return
        }
        if (view.getId() == "") {
            Log.e("ConstraintHelper", "Views added to a ConstraintHelper need to have an id")
            return
        }
        if (view.getParent() == null) {
            Log.e("ConstraintHelper", "Views added to a ConstraintHelper need to have a parent")
            return
        }
        mReferenceIds = null
        addRscID(view.getId())
        self.requestLayout()
    }

    /**
     * Remove a given view from the helper.
     *
     * @param view
     * @return index of view removed
     */
    fun removeView(view: TView): Int {
        var index = -1
        val id: String = view.getId()
        if (id == "") {
            return index
        }
        mReferenceIds = null
        for (i in 0 until mCount) {
            if (mIds[i] == id) {
                index = i
                for (j in i until mCount - 1) {
                    mIds[j] = mIds[j + 1]
                }
                mIds[mCount - 1] = ""
                mCount--
                break
            }
        }
        self.requestLayout()
        return index
    }
    /**
     * Helpers typically reference a collection of ids
     * @return ids referenced
     */
    /**
     * Helpers typically reference a collection of ids
     */
    var referencedIds: Array<String>?
        get() = Arrays.copyOfNonNull(mIds, mCount)
        set(ids) {
            mReferenceIds = null
            mCount = 0
            for (i in ids!!.indices) {
                addRscID(ids[i])
            }
        }

    /**
     *
     */
    private fun addRscID(id: String) {
        if (id == self.getId()) {
            return
        }
        if (mCount + 1 > mIds.size) {
            mIds = Arrays.copyOfNonNull(mIds, mIds.size * 2)
        }
        mIds[mCount] = id
        mCount++
    }

    /**
     *
     */
    fun onMeasure(sup: TView?, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mUseViewMeasure) {
            sup?.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            sup?.setMeasuredDimension(0, 0)
        }
    }

    /**
     *
     * Allows a helper to replace the default ConstraintWidget in LayoutParams by its own subclass
     */
    fun validateParams() {
        if (mHelperWidget == null) {
            return
        }
        val params = self.getLayoutParams() as ConstraintLayout.LayoutParams?
        if (params != null) {
            params.mWidget = mHelperWidget as ConstraintWidget?
        }
    }

    /**
     *
     */
    private fun addID(idString: String) {
        var idString: String? = idString
        if (idString == null || idString.isEmpty()) {
            return
        }
        if (myContext == null) {
            return
        }
        idString = idString.trim()
        val rscId = findId(idString)
        if (rscId != "") {
            mMap[rscId] = idString // let's remember the idString used,
            // as we may need it for dynamic modules
            addRscID(rscId)
        } else {
            Log.w("ConstraintHelper", "Could not find id of \"$idString\"")
        }
    }

    /**
     *
     */
    private fun addTag(tagString: String) {
        var tagString: String? = tagString
        if (tagString == null || tagString.isEmpty()) {
            return
        }
        if (myContext == null) {
            return
        }
        tagString = tagString.trim()
        var parent: ConstraintLayout? = null
        if (self.getParent()?.getParentType() is ConstraintLayout) {
            parent = self.getParent()?.getParentType() as ConstraintLayout?
        }
        if (parent == null) {
            Log.w("ConstraintHelper", "Parent not a ConstraintLayout")
            return
        }
        val count: Int = parent.self.getChildCount()
        for (i in 0 until count) {
            val v: TView = parent.self.getChildAt(i)
            val params = v.getLayoutParams() as ConstraintLayout.LayoutParams?
            if (params != null) {
                if (tagString == params.constraintTag) {
                    if (v.getId() == TView.NO_ID) {
                        Log.w(
                            "ConstraintHelper", ("to use ConstraintTag view "
                                    + v.getClass().getSimpleName()) + " must have an ID"
                        )
                    } else {
                        addRscID(v.getId())
                    }
                }
            }
        }
    }

    /**
     * Attempt to find the id given a reference string
     * @param referenceId
     * @return
     */
    private fun findId(referenceId: String): String {
        var parent: ConstraintLayout? = null
        if (self.getParent()?.getParentType() is ConstraintLayout) {
            parent = self.getParent()?.getParentType() as ConstraintLayout?
        }
        var rscId = ""

        // First, if we are in design mode let's get the cached information
        if (self.isInEditMode() && parent != null) {
            val value: Any? = parent.getDesignInformation(0, referenceId)
            if (value is String) {
                rscId = value as String
            }
        }

        // ... if not, let's check our siblings
        if (rscId == "" && parent != null) {
            // TODO: cache this in ConstraintLayout
            rscId = findId(parent, referenceId)
        }
        /*if (rscId == 0) {
            try {
                val res: TClass = id::class.java
                val field: Field = res.getField(referenceId)
                rscId = field.getInt(null)
            } catch (e: Exception) {
                // Do nothing
            }
        }*/
        if (rscId == "" && myContext != null) {
            // this will first try to parse the string id as a number (!) in ResourcesImpl, so
            // let's try that last...
            rscId = myContext.getResources().getIdentifier(
                referenceId, "id",
                myContext.getPackageName()
            )
        }
        return rscId
    }

    /**
     * Iterate through the container's children to find a matching id.
     * Slow path, seems necessary to handle dynamic modules resolution...
     *
     * @param container
     * @param idString
     * @return
     */
    private fun findId(container: ConstraintLayout?, idString: String?): String {
        if (idString == null || container == null || myContext == null) {
            return ""
        }
        val resources: TResources = myContext.getResources() ?: return ""
        val count: Int = container.self.getChildCount()
        for (j in 0 until count) {
            val child: TView = container.self.getChildAt(j)
            if (child.getId() != "") {
                var res: String? = null
                try {
                    res = resources.getResourceEntryName(child.getId())
                } catch (e: NotFoundException) {
                    // nothing
                }
                if (idString == res) {
                    return child.getId()
                }
            }
        }
        return ""
    }

    /**
     *
     */
    protected fun setIds(idList: String?) {
        mReferenceIds = idList
        if (idList == null) {
            return
        }
        var begin = 0
        mCount = 0
        while (true) {
            val end = idList.indexOf(',', begin)
            if (end == -1) {
                addID(idList.substring(begin))
                break
            }
            addID(idList.substring(begin, end))
            begin = end + 1
        }
    }

    /**
     *
     */
    protected fun setReferenceTags(tagList: String?) {
        mReferenceTags = tagList
        if (tagList == null) {
            return
        }
        var begin = 0
        mCount = 0
        while (true) {
            val end = tagList.indexOf(',', begin)
            if (end == -1) {
                addTag(tagList.substring(begin))
                break
            }
            addTag(tagList.substring(begin, end))
            begin = end + 1
        }
    }

    /**
     *
     * @param container
     */
    protected fun applyLayoutFeatures(container: ConstraintLayout) {
        val visibility: Int = self.getVisibility()
        var elevation = self.getElevation()
        for (i in 0 until mCount) {
            val id = mIds[i]
            val view: TView? = container.getViewById(id)
            if (view != null) {
                view.setVisibility(visibility)
                if (elevation > 0) {
                    view.setTranslationZ(view.getTranslationZ() + elevation)
                }
            }
        }
    }

    /**
     *
     */
    protected fun applyLayoutFeatures() {
        val parent: TView? = self.getParent()
        if (parent != null && parent.getParentType() is ConstraintLayout) {
            applyLayoutFeatures(parent.getParentType() as ConstraintLayout)
        }
    }

    /**
     *
     */
    open fun applyLayoutFeaturesInConstraintSet(container: ConstraintLayout?) {}

    /**
     *
     * Allows a helper a chance to update its internal object pre layout or
     * set up connections for the pointed elements
     *
     * @param container
     */
    fun updatePreLayout(container: ConstraintLayout) {
        if (self.isInEditMode()) {
            setIds(mReferenceIds)
        }
        if (mHelperWidget == null) {
            return
        }
        mHelperWidget!!.removeAllIds()
        for (i in 0 until mCount) {
            val id = mIds[i]
            var view: TView? = container.getViewById(id)
            if (view == null) {
                // hm -- we couldn't find the view.
                // It might still be there though, but with the wrong id (with dynamic modules)
                val candidate = mMap[id]
                val foundId = findId(container, candidate)
                if (foundId != "") {
                    mIds[i] = foundId
                    mMap[foundId] = candidate
                    view = container.getViewById(foundId)
                }
            }
            if (view != null) {
                mHelperWidget?.add(container.getViewWidget(view))
            }
        }
        mHelperWidget?.updateConstraints(container.mLayoutWidget)
    }

    /**
     * called before solver resolution
     * @param container
     * @param helper
     * @param map
     */
    fun updatePreLayout(
        container: ConstraintWidgetContainer?,
        helper: Helper,
        map: MutableMap<String, ConstraintWidget>
    ) {
        helper.removeAllIds()
        for (i in 0 until mCount) {
            val id = mIds[i]
            helper.add(map[id])
        }
    }

    protected fun getViews(layout: ConstraintLayout?): Array<TView?>? {
        if (mViews == null || mViews!!.size != mCount) {
            mViews = arrayOfNulls(mCount)
        }
        for (i in 0 until mCount) {
            val id = mIds[i]
            mViews!![i] = layout?.getViewById(id)
        }
        return mViews
    }

    /**
     *
     * Allows a helper a chance to update its internal object post layout or
     * set up connections for the pointed elements
     *
     * @param container
     */
    open fun updatePostLayout(container: ConstraintLayout?) {
        // Do nothing
    }

    /**
     *
     * @param container
     */
    fun updatePostMeasure(container: ConstraintLayout?) {
        // Do nothing
    }

    /**
     * update after constraints are resolved
     * @param container
     */
    fun updatePostConstraints(container: ConstraintLayout?) {
        // Do nothing
    }

    /**
     * called before the draw
     * @param container
     */
    fun updatePreDraw(container: ConstraintLayout?) {
        // Do nothing
    }

    /**
     * Load the parameters
     * @param constraint
     * @param child
     * @param layoutParams
     * @param mapIdToWidget
     */
    open fun loadParameters(
        constraint: ConstraintSet.Constraint,
        child: HelperWidget?,
        layoutParams: ConstraintLayout.LayoutParams?,
        mapIdToWidget: MutableMap<String, ConstraintWidget>
    ) {
        // TODO: rethink this. The list of views shouldn't be resolved at updatePreLayout stage,
        // as this makes changing referenced views tricky at runtime
        if (constraint.layout.mReferenceIds != null) {
            referencedIds = constraint.layout.mReferenceIds!!
        } else if (constraint.layout.mReferenceIdString != null) {
            if (constraint.layout.mReferenceIdString!!.isNotEmpty()) {
                constraint.layout.mReferenceIds = convertReferenceString(
                    constraint.layout.mReferenceIdString!!
                )
            } else {
                constraint.layout.mReferenceIds = null
            }
        }
        if (child != null) {
            child.removeAllIds()
            if (constraint.layout.mReferenceIds != null) {
                for (i in 0 until constraint.layout.mReferenceIds!!.size) {
                    val id: String = constraint.layout.mReferenceIds!![i]
                    val widget: ConstraintWidget? = mapIdToWidget[id]
                    if (widget != null) {
                        child.add(widget)
                    }
                }
            }
        }
    }

    private fun convertReferenceString(referenceIdString: String): Array<String> {
        val split: List<String> = referenceIdString.split(",")
        var rscIds = Array(split.size){""}
        var count = 0
        for (i in split.indices) {
            var idString = split[i]
            idString = idString.trim()
            val id = findId(idString)
            if (id != "") {
                rscIds[count++] = id
            }
        }
        if (count != split.size) {
            rscIds = Arrays.copyOfNonNull(rscIds, count)
        }
        return rscIds
    }

    /**
     * resolve the RTL
     * @param widget
     * @param isRtl
     */
    fun resolveRtl(widget: ConstraintWidget?, isRtl: Boolean) {
        // nothing here
    }

    fun setTag(sup: TView?, key: String, tag: Any?) {
        sup?.setTag(key, tag)
        if (tag == null && mReferenceIds == null) {
            addRscID(key)
        }
    }

    /**
     * does id table contain the id
     *
     * @param id
     * @return
     */
    fun containsId(id: String): Boolean {
        var result = false
        for (i in mIds) {
            if (i == id) {
                result = true
                break
            }
        }
        return result
    }

    /**
     * find the position of an id
     *
     * @param id
     * @return
     */
    fun indexFromId(id: String): Int {
        var index = -1
        for (i in mIds) {
            index++
            if (i == id) {
                return index
            }
        }
        return index
    }

    /**
     * hook for helpers to apply parameters in MotionLayout
     */
    fun applyHelperParams() {}

    companion object {
        /**
         *
         */
        protected const val CHILD_TAG = "CONSTRAINT_LAYOUT_HELPER_CHILD"
        fun isChildOfHelper(v: TView): Boolean {
            return CHILD_TAG === v.getTag()
        }
    }
}