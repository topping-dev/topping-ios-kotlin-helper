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

package dev.topping.ios.constraint.constraintlayout.helper.widget

import dev.topping.ios.constraint.*
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintLayout
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.constraintlayout.widget.VirtualLayout

/**
 *
 * CircularFlow virtual layout.
 *
 * Allows positioning of referenced widgets circular.
 *
 * The elements referenced are indicated via constraint_referenced_ids, as with other
 * ConstraintHelper implementations.
 *
 * XML attributes that are needed:
 *
 *  * constraint_referenced_ids = "view2, view3, view4,view5,view6".
 * It receives id's of the views that will add the references.
 *  * circularflow_viewCenter = "view1". It receives the id of the view of the center where
 * the views received in constraint_referenced_ids will be referenced.
 *  * circularflow_angles = "45,90,135,180,225". Receive the angles that you
 * will assign to each view.
 *  * circularflow_radiusInDP = "90,100,110,120,130". Receive the radios in DP that you
 * will assign to each view.
 *
 *
 * Example in XML:
 * <androidx.constraintlayout.helper.widget.CircularFlow android:id="@+id/circularFlow" android:layout_width="match_parent" android:layout_height="match_parent" app:circularflow_angles="0,40,80,120" app:circularflow_radiusInDP="90,100,110,120" app:circularflow_viewCenter="@+id/view1" app:constraint_referenced_ids="view2,view3,view4,view5"></androidx.constraintlayout.helper.widget.CircularFlow>
 *
 * DEFAULT radius - If you add a view and don't set its radius, the default value will be 0.
 * DEFAULT angles - If you add a view and don't set its angle, the default value will be 0.
 *
 * Recommendation - always set radius and angle for all views in *constraint_referenced_ids*
 *
 */
class CircularFlow(context: TContext, attrs: AttributeSet, self: TView) : VirtualLayout(context, attrs, self) {
    var mContainer: ConstraintLayout? = null
    var mViewCenter = ""

    /**
     *
     */
    private var mAngles: FloatArray = floatArrayOf()

    /**
     *
     */
    private var mRadius: IntArray = intArrayOf()

    /**
     *
     */
    private var mCountRadius = 0

    /**
     *
     */
    private var mCountAngle = 0

    /**
     *
     */
    private var mReferenceAngles: String? = null

    /**
     *
     */
    private var mReferenceRadius: String? = null

    /**
     *
     */
    private var mReferenceDefaultAngle: Float? = null

    /**
     *
     */
    private var mReferenceDefaultRadius: Int? = null

    val radius: IntArray
        get() = Arrays.copyOf(mRadius, mCountRadius)
    val angles: FloatArray
        get() = Arrays.copyOf(mAngles, mCountAngle)

    init {
        val a = context.getResources()
        attrs.forEach { kvp ->
            val attr = kvp.value
            if(kvp.key == "circularflow_viewCenter") {
                mViewCenter = a.getResourceId(attr, "")
            } else if(kvp.key == "circularflow_angles") {
                mReferenceAngles = a.getString(kvp.key, attr)
                setAngles(mReferenceAngles)
            } else if(kvp.key == "circularflow_radiusInDP") {
                mReferenceRadius = a.getString(kvp.key, attr)
                setRadius(mReferenceRadius)
            } else if(kvp.key == "circularflow_defaultAngle") {
                mReferenceDefaultAngle = a.getFloat(kvp.key, attr, sDefaultAngle)
                setDefaultAngle(mReferenceDefaultAngle!!)
            } else if(kvp.key == "circularflow_defaultRadius") {
                mReferenceDefaultRadius = a.getDimensionPixelSize(attr, sDefaultRadius)
                setDefaultRadius(mReferenceDefaultRadius!!)
            }
        }
    }

    override fun onAttachedToWindow(sup: TView?) {
        sup?.onAttachedToWindow()
        if (mReferenceAngles != null) {
            mAngles = FloatArray(1)
            setAngles(mReferenceAngles)
        }
        if (mReferenceRadius != null) {
            mRadius = IntArray(1)
            setRadius(mReferenceRadius)
        }
        if (mReferenceDefaultAngle != null) {
            setDefaultAngle(mReferenceDefaultAngle!!)
        }
        if (mReferenceDefaultRadius != null) {
            setDefaultRadius(mReferenceDefaultRadius!!)
        }
        anchorReferences()
    }

    private fun anchorReferences() {
        mContainer = self.getParent()?.getParentType() as ConstraintLayout?
        if(mContainer != null) {
            for (i in 0 until mCount) {
                val view= mContainer!!.getViewById(mIds.get(i)) ?: continue
                var radius = sDefaultRadius
                var angle = sDefaultAngle
                if (mRadius != null && i < mRadius!!.size) {
                    radius = mRadius!![i]
                } else if (mReferenceDefaultRadius != null && mReferenceDefaultRadius != -1) {
                    mCountRadius++
                    if (mRadius == null) {
                        mRadius = IntArray(1)
                    }
                    mRadius = this.radius
                    mRadius.set(mCountRadius - 1, radius)
                } else {
                    Log.e("CircularFlow", "Added radius to view with id: " + mMap.get(view.getId()))
                }
                if (mAngles != null && i < mAngles!!.size) {
                    angle = mAngles!![i]
                } else if (mReferenceDefaultAngle != null && mReferenceDefaultAngle != -1f) {
                    mCountAngle++
                    if (mAngles == null) {
                        mAngles = FloatArray(1)
                    }
                    mAngles = angles
                    mAngles!![mCountAngle - 1] = angle
                } else {
                    Log.e(
                        "CircularFlow",
                        "Added angle to view with id: " + mMap.get(view.getId())
                    )
                }
                val params: ConstraintLayout.LayoutParams =
                    view.getLayoutParams() as ConstraintLayout.LayoutParams
                params.circleAngle = angle
                params.circleConstraint = mViewCenter
                params.circleRadius = radius
                view.setLayoutParams(params)
            }
        }
        applyLayoutFeatures()
    }

    /**
     * Add a view to the CircularFlow.
     * The referenced view need to be a child of the container parent.
     * The view also need to have its id set in order to be added.
     * The views previous need to have its radius and angle set in order
     * to be added correctly a new view.
     * @param view
     * @param radius
     * @param angle
     * @return
     */
    fun addViewToCircularFlow(view: TView, radius: Int, angle: Float) {
        if (containsId(view.getId())) {
            return
        }
        addView(view)
        mCountAngle++
        mAngles = angles
        mAngles!![mCountAngle - 1] = angle
        mCountRadius++
        mRadius = this.radius
        mRadius.set(mCountRadius - 1,
            (radius * context.getResources().getDisplayMetrics().density) as Int)
        anchorReferences()
    }

    /**
     * Update radius from a view in CircularFlow.
     * The referenced view need to be a child of the container parent.
     * The view also need to have its id set in order to be added.
     * @param view
     * @param radius
     * @return
     */
    fun updateRadius(view: TView, radius: Int) {
        if (!isUpdatable(view)) {
            Log.e(
                "CircularFlow",
                "It was not possible to update radius to view with id: " + view.getId()
            )
            return
        }
        val indexView: Int = indexFromId(view.getId())
        if (indexView > mRadius!!.size) {
            return
        }
        mRadius = this.radius
        mRadius.set(indexView, (radius * context.getResources().getDisplayMetrics().density) as Int)
        anchorReferences()
    }

    /**
     * Update angle from a view in CircularFlow.
     * The referenced view need to be a child of the container parent.
     * The view also need to have its id set in order to be added.
     * @param view
     * @param angle
     * @return
     */
    fun updateAngle(view: TView, angle: Float) {
        if (!isUpdatable(view)) {
            Log.e(
                "CircularFlow",
                "It was not possible to update angle to view with id: " + view.getId()
            )
            return
        }
        val indexView: Int = indexFromId(view.getId())
        if (indexView > mAngles!!.size) {
            return
        }
        mAngles = angles
        mAngles!![indexView] = angle
        anchorReferences()
    }

    /**
     * Update angle and radius from a view in CircularFlow.
     * The referenced view need to be a child of the container parent.
     * The view also need to have its id set in order to be added.
     * @param view
     * @param radius
     * @param angle
     * @return
     */
    fun updateReference(view: TView, radius: Int, angle: Float) {
        if (!isUpdatable(view)) {
            Log.e(
                "CircularFlow", "It was not possible to update radius and angle to view with id: "
                        + view.getId()
            )
            return
        }
        val indexView: Int = indexFromId(view.getId())
        if (angles.size > indexView) {
            mAngles = angles
            mAngles!![indexView] = angle
        }
        if (this.radius.size > indexView) {
            mRadius = this.radius
            mRadius.set(indexView, (radius * context.getResources().getDisplayMetrics().density) as Int)
        }
        anchorReferences()
    }

    /**
     * Set default Angle for CircularFlow.
     *
     * @param angle
     * @return
     */
    fun setDefaultAngle(angle: Float) {
        sDefaultAngle = angle
    }

    /**
     * Set default Radius for CircularFlow.
     *
     * @param radius
     * @return
     */
    fun setDefaultRadius(radius: Int) {
        sDefaultRadius = radius
    }

    override fun removeView(view: TView): Int {
        val index: Int = super.removeView(view)
        if (index == -1) {
            return index
        }
        if(mContainer == null)
            return index
        val c = ConstraintSet()
        c.clone(mContainer!!)
        c.clear(view.getId(), ConstraintSet.CIRCLE_REFERENCE)
        c.applyTo(mContainer!!)
        if (index < mAngles!!.size) {
            mAngles = removeAngle(mAngles, index)
            mCountAngle--
        }
        if (index < mRadius!!.size) {
            mRadius = removeRadius(mRadius, index)
            mCountRadius--
        }
        anchorReferences()
        return index
    }

    /**
     *
     */
    private fun removeAngle(angles: FloatArray, index: Int): FloatArray {
        return if (angles == null || index < 0 || index >= mCountAngle
        ) {
            angles
        } else removeElementFromArray(angles, index)
    }

    /**
     *
     */
    private fun removeRadius(radius: IntArray, index: Int): IntArray {
        return if (radius == null || index < 0 || index >= mCountRadius
        ) {
            radius
        } else removeElementFromArray(radius, index)
    }

    /**
     *
     */
    private fun setAngles(idList: String?) {
        if (idList == null) {
            return
        }
        var begin = 0
        mCountAngle = 0
        while (true) {
            val end = idList.indexOf(',', begin)
            if (end == -1) {
                addAngle(idList.substring(begin).trim { it <= ' ' })
                break
            }
            addAngle(idList.substring(begin, end).trim { it <= ' ' })
            begin = end + 1
        }
    }

    /**
     *
     */
    private fun setRadius(idList: String?) {
        if (idList == null) {
            return
        }
        var begin = 0
        mCountRadius = 0
        while (true) {
            val end = idList.indexOf(',', begin)
            if (end == -1) {
                addRadius(idList.substring(begin).trim { it <= ' ' })
                break
            }
            addRadius(idList.substring(begin, end).trim { it <= ' ' })
            begin = end + 1
        }
    }

    /**
     *
     */
    private fun addAngle(angleString: String?) {
        if (angleString == null || angleString.length == 0) {
            return
        }
        if (mCountAngle + 1 > mAngles!!.size) {
            mAngles = Arrays.copyOf(mAngles, mAngles!!.size + 1)
        }
        mAngles!![mCountAngle] = angleString.toInt().toFloat()
        mCountAngle++
    }

    /**
     *
     */
    private fun addRadius(radiusString: String?) {
        if (radiusString == null || radiusString.length == 0) {
            return
        }
        if (mCountRadius + 1 > mRadius!!.size) {
            mRadius = Arrays.copyOf(mRadius, mRadius!!.size + 1)
        }
        mRadius!![mCountRadius] =
            (radiusString.toInt() * context.getResources().getDisplayMetrics().density) as Int
        mCountRadius++
    }

    /**
     * if view is part of circular flow
     * @param view
     * @return true if the flow contains the view
     */
    fun isUpdatable(view: TView): Boolean {
        if (!containsId(view.getId())) {
            return false
        }
        val indexView: Int = indexFromId(view.getId())
        return indexView != -1
    }

    companion object {
        private const val TAG = "CircularFlow"
        private var sDefaultRadius = 0
        private var sDefaultAngle = 0f
        private fun removeElementFromArray(array: IntArray, index: Int): IntArray {
            val newArray = IntArray(array.size - 1)
            var i = 0
            var k = 0
            while (i < array.size) {
                if (i == index) {
                    i++
                    continue
                }
                newArray[k++] = array[i]
                i++
            }
            return newArray
        }

        private fun removeElementFromArray(array: FloatArray, index: Int): FloatArray {
            val newArray = FloatArray(array.size - 1)
            var i = 0
            var k = 0
            while (i < array.size) {
                if (i == index) {
                    i++
                    continue
                }
                newArray[k++] = array[i]
                i++
            }
            return newArray
        }
    }
}