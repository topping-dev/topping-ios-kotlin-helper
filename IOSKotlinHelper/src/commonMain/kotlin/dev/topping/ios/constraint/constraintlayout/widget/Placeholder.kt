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

import dev.topping.ios.constraint.TContext
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.core.widgets.ConstraintWidget

/**
 * **Added in 1.1**
 *
 *
 * A `Placeholder` provides a virtual object which can position an existing object.
 *
 *
 * When the id of another view is set on a placeholder (using `setContent()`),
 * the placeholder effectively becomes the content view. If the content view exist on the
 * screen it is treated as gone from its original location.
 *
 *
 * The content view is positioned using the layout of the parameters of the
 * `Placeholder`  (the `Placeholder`
 * is simply constrained in the layout like any other view).
 *
 */
class Placeholder(val context: TContext?, val self: TView) {
    private var mContentId = ""
    private var mContent: TView? = null
    /**
     * Returns the behaviour of a placeholder when it contains no view.
     *
     * @return Either TView.VISIBLE, TView.INVISIBLE, TView.GONE. Default is INVISIBLE
     */
    /**
     * Sets the visibility of placeholder when not containing objects typically gone or invisible.
     * This can be important as it affects behaviour of surrounding components.
     *
     * @param visibility Either TView.VISIBLE, TView.INVISIBLE, TView.GONE
     */
    var emptyVisibility: Int = TView.INVISIBLE

    init {
        self.setParentType(this)
        mContentId = self.getObjCProperty("app_placeholder_content") as String? ?: ""
        emptyVisibility = self.getObjCProperty("app_placeholder_content") as Int? ?: TView.INVISIBLE
        self.setVisibility(emptyVisibility)
    }

    /**
     * Returns the content view
     *
     * @return `null` if no content is set, otherwise the content view
     */
    val content: TView?
        get() = mContent

    /**
     * @param container
     */
    fun updatePreLayout(container: ConstraintLayout) {
        if (mContentId == "") {
            if (!self.isInEditMode()) {
                self.setVisibility(emptyVisibility)
            }
        }
        mContent = container.self.findViewById(mContentId)
        if (mContent != null) {
            (mContent!!.getLayoutParams() as ConstraintLayout.LayoutParams?)?.mIsInPlaceholder = true
            mContent!!.setVisibility(TView.VISIBLE)
            self.setVisibility(TView.VISIBLE)
        }
    }

    /**
     * Sets the content view id
     *
     * @param id the id of the content view we want to place in the Placeholder
     */
    fun setContentId(id: String) {
        if (mContentId == id) {
            return
        }
        if (mContent != null) {
            mContent!!.setVisibility(TView.VISIBLE) // ???
            (mContent!!.getLayoutParams() as ConstraintLayout.LayoutParams?)?.mIsInPlaceholder = false
            mContent = null
        }
        mContentId = id
        if (id != ConstraintLayout.LayoutParams.UNSET_ID) {
            val v: TView? = (self.getParent() as TView?)?.findViewById(id)
            if (v != null) {
                v.setVisibility(TView.GONE)
            }
        }
    }

    /**
     * @param container
     */
    fun updatePostMeasure(container: ConstraintLayout?) {
        if (mContent == null) {
            return
        }
        val layoutParams: ConstraintLayout.LayoutParams =
            self.getLayoutParams() as ConstraintLayout.LayoutParams
        val layoutParamsContent: ConstraintLayout.LayoutParams = mContent!!
            .getLayoutParams() as ConstraintLayout.LayoutParams
        layoutParamsContent.mWidget?.visibility = TView.VISIBLE
        if (layoutParams.mWidget?.horizontalDimensionBehaviour
            != ConstraintWidget.DimensionBehaviour.FIXED
        ) {
            layoutParams.mWidget?.width = layoutParamsContent.mWidget!!.width
        }
        if (layoutParams.mWidget?.verticalDimensionBehaviour
            !== ConstraintWidget.DimensionBehaviour.FIXED
        ) {
            layoutParams.mWidget?.height = layoutParamsContent.mWidget!!.height
        }
        layoutParamsContent.mWidget?.visibility = TView.GONE
    }
}