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

import dev.topping.ios.constraint.Log
import dev.topping.ios.constraint.TView
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.BASELINE
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.BOTTOM
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.END
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.HORIZONTAL
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.LEFT
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.RIGHT
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.START
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.TOP
import dev.topping.ios.constraint.constraintlayout.widget.ConstraintSet.Companion.VERTICAL

/**
 * Utility class to manipulate MotionLayout from the layout editor
 *
 *
 */
class DesignTool(motionLayout: MotionLayout) {
    private val mMotionLayout: MotionLayout
    private var mSceneCache: MotionScene? = null
    private var mLastStartState: String? = null
    private var mLastEndState: String? = null
    private var mLastStartStateId = ""
    private var mLastEndStateId = ""

    init {
        mMotionLayout = motionLayout
    }

    /**
     * Get the center point of the animation path of a view
     *
     * @param view view to getMap the animation of
     * @param path array to be filled (x1,y1,x2,y2...)
     * @return -1 if not under and animation 0 if not animated or number of point along animation
     */
    fun getAnimationPath(view: Any?, path: FloatArray, len: Int): Int {
        if (mMotionLayout.mScene == null) {
            return -1
        }
        val motionController: MotionController = mMotionLayout.mFrameArrayList.get(view) ?: return 0
        motionController.buildPath(path, len)
        return len
    }

    /**
     * Get the center point of the animation path of a view
     *
     * @param view view to getMap the animation of
     * @param path array to be filled (in groups of 8) (x1,y1,x2,y2...)
     */
    fun getAnimationRectangles(view: Any?, path: FloatArray) {
        if (mMotionLayout.mScene == null) {
            return
        }
        val duration: Int = mMotionLayout.mScene!!.duration
        val frames = duration / 16
        val motionController: MotionController = mMotionLayout.mFrameArrayList.get(view) ?: return
        motionController.buildRectangles(path, frames)
    }

    /**
     * Get the location of the start end and key frames
     *
     * @param view the view to track
     * @param key  array to be filled
     * @return number of key frames + 2
     */
    fun getAnimationKeyFrames(view: Any?, key: FloatArray?): Int {
        if (mMotionLayout.mScene == null) {
            return -1
        }
        val duration: Int = mMotionLayout.mScene!!.duration
        val frames = duration / 16
        val motionController: MotionController = mMotionLayout.mFrameArrayList.get(view) ?: return 0
        motionController.buildKeyFrames(key, null)
        return frames
    }

    /**
     * @param position
     */
    fun setToolPosition(position: Float) {
        if (mMotionLayout.mScene == null) {
            mMotionLayout.mScene = mSceneCache
        }
        mMotionLayout.progress = position
        mMotionLayout.evaluate(true)
        mMotionLayout.requestLayout(mMotionLayout.self)
        mMotionLayout.self.invalidate()
    }
    // @TODO: add description
    /**
     *
     * @return
     */
    val startState: String?
        get() {
            val startId: String = mMotionLayout.startState
            if (mLastStartStateId == startId) {
                return mLastStartState
            }
            val last: String? = mMotionLayout.getConstraintSetNames(startId)
            if (last != null) {
                mLastStartState = last
                mLastStartStateId = startId
            }
            return mMotionLayout.getConstraintSetNames(startId)
        }
    // @TODO: add description
    /**
     *
     * @return
     */
    val endState: String?
        get() {
            val endId: String = mMotionLayout.endState
            if (mLastEndStateId == endId) {
                return mLastEndState
            }
            val last: String? = mMotionLayout.getConstraintSetNames(endId)
            if (last != null) {
                mLastEndState = last
                mLastEndStateId = endId
            }
            return last
        }

    /**
     * Return the current progress of the current transition
     *
     * @return current transition's progress
     */
    val progress: Float
        get() = mMotionLayout.progress
    /**
     * Return the current state (ConstraintSet id) as a string
     *
     * @return the last state set via the design tool bridge
     */// going to base layout
    /**
     * This sets the constraint set based on a string. (without the "@+id/")
     *
     * @param id
     */
    var state: String?
        get() {
            if (mLastStartState != null && mLastEndState != null) {
                val progress = progress
                val epsilon = 0.01f
                if (progress <= epsilon) {
                    return mLastStartState
                } else if (progress >= 1 - epsilon) {
                    return mLastEndState
                }
            }
            return mLastStartState
        }
        set(id) {
            var id = id
            if (id == null) {
                id = "motion_base"
            }
            if (mLastStartState == id) {
                return
            }
            if (I_DEBUG) {
                println("================================")
                dumpConstraintSet(id)
            }
            mLastStartState = id
            mLastEndState = null
            if (id == null && DO_NOT_USE) { // going to base layout
                if (mMotionLayout.mScene != null) {
                    mSceneCache = mMotionLayout.mScene
                    mMotionLayout.mScene = null
                }
                mMotionLayout.progress = 0f
                mMotionLayout.requestLayout(mMotionLayout.self)
            }
            if (mMotionLayout.mScene == null) {
                mMotionLayout.mScene = mSceneCache
            }
            val rscId: String = mMotionLayout.lookUpConstraintId(id)
            mLastStartStateId = rscId
            if (rscId != "") {
                if (rscId == mMotionLayout.startState) {
                    mMotionLayout.progress = 0f
                } else if (rscId == mMotionLayout.endState) {
                    mMotionLayout.progress =1f
                } else {
                    mMotionLayout.transitionToState(rscId)
                    mMotionLayout.progress = 1f
                }
            }
            mMotionLayout.requestLayout(mMotionLayout.self)
        }

    /**
     * Utility method, returns true if we are currently in a transition
     *
     * @return true if in a transition, false otherwise
     */
    val isInTransition: Boolean
        get() = mLastStartState != null && mLastEndState != null

    /**
     * This sets the constraint set based on a string. (without the "@+id/")
     *
     * @param start
     * @param end
     */
    fun setTransition(start: String, end: String) {
        if (mMotionLayout.mScene == null) {
            mMotionLayout.mScene = mSceneCache
        }
        val startId = mMotionLayout.lookUpConstraintId(start)
        val endId = mMotionLayout.lookUpConstraintId(end)
        mMotionLayout.setTransition(startId, endId)
        mLastStartStateId = startId
        mLastEndStateId = endId
        mLastStartState = start
        mLastEndState = end
    }

    /**
     * this allow disabling autoTransitions to prevent design surface
     * from being in undefined states
     *
     * @param disable
     */
    fun disableAutoTransition(disable: Boolean) {
        mMotionLayout.disableAutoTransition(disable)
    }

    /**
     * Gets the time of the currently set animation.
     *
     * @return time in Milliseconds
     */
    val transitionTimeMs: Long
        get() = mMotionLayout.transitionTimeMs

    /**
     * Get the keyFrames for the view controlled by this MotionController.
     * The call is designed to be efficient because it will be called 30x Number of views a second
     *
     * @param view the view to return keyframe positions
     * @param type is pos(0-100) + 1000*mType(1=Attrib, 2=Position, 3=TimeCycle 4=Cycle 5=Trigger
     * @param pos  the x&y position of the keyFrame along the path
     * @return Number of keyFrames found
     */
    fun getKeyFramePositions(view: Any?, type: IntArray, pos: FloatArray): Int {
        val controller: MotionController =
            mMotionLayout.mFrameArrayList.get(view as TView?) ?: return 0
        return controller.getKeyFramePositions(type, pos)
    }

    /**
     * Get the keyFrames for the view controlled by this MotionController.
     * The call is designed to be efficient because it will be called 30x Number of views a second
     *
     * @param view the view to return keyframe positions
     * @param info
     * @return Number of keyFrames found
     */
    fun getKeyFrameInfo(view: Any?, type: Int, info: IntArray): Int {
        val controller: MotionController =
            mMotionLayout.mFrameArrayList.get(view as TView?) ?: return 0
        return controller.getKeyFrameInfo(type, info)
    }

    /**
     * @param view
     * @param type
     * @param x
     * @param y
     * @return
     */
    fun getKeyFramePosition(view: Any?, type: Int, x: Float, y: Float): Float {
        if (view !is TView) {
            return 0f
        }
        val mc: MotionController = mMotionLayout.mFrameArrayList.get(view as TView?) ?: return 0f
        return mc.getKeyFrameParameter(type, x, y)
    }

    /**
     * @param view
     * @param position
     * @param name
     * @param value
     */
    fun setKeyFrame(view: Any, position: Int, name: String, value: Any) {
        if (I_DEBUG) {
            Log.v(TAG, "setKeyFrame $position <$name> $value")
        }
        if (mMotionLayout.mScene != null) {
            mMotionLayout.mScene!!.setKeyframe(view as TView, position, name, value)
            mMotionLayout.targetPosition = position / 100f
            mMotionLayout.mTransitionLastPosition = 0f
            mMotionLayout.rebuildScene()
            mMotionLayout.evaluate(true)
        }
    }

    /**
     * Move the widget directly
     *
     * @param view
     * @param position
     * @param type
     * @param x
     * @param y
     * @return
     */
    fun setKeyFramePosition(view: Any, position: Int, type: Int, x: Float, y: Float): Boolean {
        var position = position
        if (view !is TView) {
            return false
        }
        if (mMotionLayout.mScene != null) {
            val controller: MotionController? = mMotionLayout.mFrameArrayList.get(view)
            position = (mMotionLayout.mTransitionPosition * 100) as Int
            if (controller != null
                && mMotionLayout.mScene!!.hasKeyFramePosition(view as TView, position)
            ) {
                val fx: Float = controller.getKeyFrameParameter(
                    MotionController.HORIZONTAL_PATH_X,
                    x, y
                )
                val fy: Float = controller.getKeyFrameParameter(
                    MotionController.VERTICAL_PATH_Y,
                    x, y
                )
                // TODO: supports path relative
                mMotionLayout.mScene!!.setKeyframe(view as TView, position, "motion:percentX", fx)
                mMotionLayout.mScene!!.setKeyframe(view as TView, position, "motion:percentY", fy)
                mMotionLayout.rebuildScene()
                mMotionLayout.evaluate(true)
                mMotionLayout.self.invalidate()
                return true
            }
        }
        return false
    }

    /**
     * @param view
     * @param debugMode
     */
    fun setViewDebug(view: Any?, debugMode: Int) {
        if (view !is TView) {
            return
        }
        val motionController: MotionController? = mMotionLayout.mFrameArrayList.get(view)
        if (motionController != null) {
            motionController.drawPath = debugMode
            mMotionLayout.self.invalidate()
        }
    }

    /**
     * This is a general access to systems in the  MotionLayout System
     * This provides a series of commands used by the designer to access needed logic
     * It is written this way to minimize the interface between the library and designer.
     * It allows the logic to be kept only in the library not replicated in the gui builder.
     * It also allows us to understand understand the version  of MotionLayout in use
     * commands
     * 0 return the version number
     * 1 Get the center point of the animation path of a view
     * 2 Get the location of the start end and key frames
     *
     * @param cmd        this provide the command needed
     * @param type       support argument for command
     * @param viewObject if this command references a view this provides access
     * @param in         this allows for an array of float to be the input to the system
     * @param inLength   this provides the length of the input
     * @param out        this provide the output array
     * @param outLength  the length of the output array
     * @return command dependent -1 is typically an error (do not understand)
     */
    fun designAccess(
        cmd: Int, type: String?, viewObject: Any?,
        `in`: FloatArray?, inLength: Int, out: FloatArray, outLength: Int
    ): Int {
        val view: TView? = viewObject as TView?
        var motionController: MotionController? = null
        if (cmd != 0) {
            if (mMotionLayout.mScene == null) {
                return -1
            }
            if (view != null) { // Can't find the view
                motionController = mMotionLayout.mFrameArrayList.get(view)
                if (motionController == null) {
                    return -1
                }
            } else { // currently only cmd  == 0 does not require a motion view
                return -1
            }
        }
        return when (cmd) {
            0 -> 1
            1 -> {
                // get TView path
                val duration: Int = mMotionLayout.mScene!!.duration
                val frames = duration / 16
                motionController!!.buildPath(out, frames)
                frames
            }
            2 -> {
                // get key frames
                val duration: Int = mMotionLayout.mScene!!.duration
                val frames = duration / 16
                motionController!!.buildKeyFrames(out, null)
                frames
            }
            3 -> {
                // get Attribute
                val duration: Int = mMotionLayout.mScene!!.duration
                val frames = duration / 16
                motionController!!.getAttributeValues(type, out, outLength)
            }
            else -> -1
        }
    }
    // @TODO: add description
    /**
     *
     * @param type
     * @param target
     * @param position
     * @return
     */
    fun getKeyframe(type: Int, target: String, position: Int): Any? {
        return if (mMotionLayout.mScene == null) {
            null
        } else mMotionLayout.mScene!!.getKeyFrame(
            mMotionLayout.context,
            type,
            target,
            position
        )
    }
    // @TODO: add description
    /**
     *
     * @param viewObject
     * @param x
     * @param y
     * @return
     */
    fun getKeyframeAtLocation(viewObject: Any?, x: Float, y: Float): Any? {
        val view: TView? = viewObject as TView?
        var motionController: MotionController? = null
        if (mMotionLayout.mScene == null) {
            return -1
        }
        if (view != null) { // Can't find the view
            motionController = mMotionLayout.mFrameArrayList.get(view)
            if (motionController == null) {
                return null
            }
        } else {
            return null
        }
        val viewGroup: TView = view.getParent() as TView
        val layoutWidth: Int = viewGroup.getWidth()
        val layoutHeight: Int = viewGroup.getHeight()
        return motionController.getPositionKeyframe(layoutWidth, layoutHeight, x, y)
    }
    // @TODO: add description
    /**
     *
     * @param keyFrame
     * @param view
     * @param x
     * @param y
     * @param attribute
     * @param value
     * @return
     */
    fun getPositionKeyframe(
        keyFrame: Any,
        view: Any?,
        x: Float,
        y: Float,
        attribute: Array<String>,
        value: FloatArray
    ): Boolean {
        if (keyFrame is KeyPositionBase) {
            val motionController: MotionController =
                mMotionLayout.mFrameArrayList.get(view as TView?)!!
            motionController.positionKeyframe(view as TView?, keyFrame, x, y, attribute, value)
            mMotionLayout.rebuildScene()
            mMotionLayout.mInTransition = true
            return true
        }
        return false
    }
    // @TODO: add description
    /**
     *
     * @param view
     * @param type
     * @param position
     * @return
     */
    fun getKeyframe(view: Any, type: Int, position: Int): Any? {
        if (mMotionLayout.mScene == null) {
            return null
        }
        val target: String = (view as TView).getId()
        return mMotionLayout.mScene!!.getKeyFrame(mMotionLayout.context, type, target, position)
    }
    // @TODO: add description
    /**
     *
     * @param keyFrame
     * @param tag
     * @param value
     */
    fun setKeyframe(keyFrame: Any, tag: String, value: Any) {
        if (keyFrame is Key) {
            keyFrame.setValue(tag, value)
            mMotionLayout.rebuildScene()
            mMotionLayout.mInTransition = true
        }
    }

    /**
     * Live setting of attributes on a view
     *
     * @param dpi              dpi used by the application
     * @param constraintSetId  ConstraintSet id
     * @param opaqueView       the Android TView we operate on, passed as an Object
     * @param opaqueAttributes the list of attributes (hash<string></string>,string>) we pass to the view
     */
    fun setAttributes(
        dpi: Int,
        constraintSetId: String,
        opaqueView: Any,
        opaqueAttributes: Any
    ) {
        val view: TView = opaqueView as TView
        val attributes: MutableMap<String?, String> =
            if (opaqueAttributes is MutableMap<*, *>) opaqueAttributes as MutableMap<String?, String> else mutableMapOf<String?, String>()
        val rscId = mMotionLayout.lookUpConstraintId(constraintSetId)
        val set: ConstraintSet? = mMotionLayout.mScene!!.getConstraintSet(rscId)
        if (I_DEBUG) {
            Log.v(TAG, "constraintSetId  = $constraintSetId  $rscId")
        }
        if (set == null) {
            return
        }
        set.clear(view.getId())
        setDimensions(dpi, set, view, attributes, HORIZONTAL)
        setDimensions(dpi, set, view, attributes, VERTICAL)
        connect(dpi, set, view, attributes, START, START)
        connect(dpi, set, view, attributes, START, END)
        connect(dpi, set, view, attributes, END, END)
        connect(dpi, set, view, attributes, END, START)
        connect(dpi, set, view, attributes, LEFT, LEFT)
        connect(dpi, set, view, attributes, LEFT, RIGHT)
        connect(dpi, set, view, attributes, RIGHT, RIGHT)
        connect(dpi, set, view, attributes, RIGHT, LEFT)
        connect(dpi, set, view, attributes, TOP, TOP)
        connect(dpi, set, view, attributes, TOP, BOTTOM)
        connect(dpi, set, view, attributes, BOTTOM, TOP)
        connect(dpi, set, view, attributes, BOTTOM, BOTTOM)
        connect(dpi, set, view, attributes, BASELINE, BASELINE)
        setBias(set, view, attributes, HORIZONTAL)
        setBias(set, view, attributes, VERTICAL)
        setAbsolutePositions(dpi, set, view, attributes)
        mMotionLayout.updateState(rscId, set)
        mMotionLayout.requestLayout(mMotionLayout.self)
    }
    // @TODO: add description
    /**
     *
     * @param set
     */
    fun dumpConstraintSet(set: String) {
        if (mMotionLayout.mScene == null) {
            mMotionLayout.mScene = mSceneCache
        }
        val setId = mMotionLayout.lookUpConstraintId(set)
        println(" dumping  $set ($setId)")
        try {
            mMotionLayout.mScene!!.getConstraintSet(setId)!!.dump(mMotionLayout.mScene!!)
        } catch (ex: Exception) {
            Log.e(TAG, "Error while dumping: $set ($setId) $ex")
        }
    }

    companion object {
        val sAllAttributes: MutableMap<Pair<Int, Int>, String> = mutableMapOf()
        val sAllMargins: MutableMap<String?, String> = mutableMapOf()
        private const val I_DEBUG = false
        private const val DO_NOT_USE = false
        private const val TAG = "DesignTool"

        init {
            sAllAttributes[Pair(BOTTOM, BOTTOM)] = "layout_constraintBottom_toBottomOf"
            sAllAttributes[Pair(BOTTOM, TOP)] =
                "layout_constraintBottom_toTopOf"
            sAllAttributes[Pair(
                TOP,
                BOTTOM
            )] =
                "layout_constraintTop_toBottomOf"
            sAllAttributes[Pair(TOP, TOP)] = "layout_constraintTop_toTopOf"
            sAllAttributes[Pair(START, START)] = "layout_constraintStart_toStartOf"
            sAllAttributes[Pair(
                START,
                END
            )] = "layout_constraintStart_toEndOf"
            sAllAttributes[Pair(END, START)] = "layout_constraintEnd_toStartOf"
            sAllAttributes[Pair(
                END,
                END
            )] =
                "layout_constraintEnd_toEndOf"
            sAllAttributes[Pair(
                LEFT,
                LEFT
            )] = "layout_constraintLeft_toLeftOf"
            sAllAttributes[Pair(
                LEFT,
                RIGHT
            )] = "layout_constraintLeft_toRightOf"
            sAllAttributes[Pair(
                RIGHT,
                RIGHT
            )] =
                "layout_constraintRight_toRightOf"
            sAllAttributes[Pair(
                RIGHT,
                LEFT
            )] = "layout_constraintRight_toLeftOf"
            sAllAttributes[Pair(BASELINE, BASELINE)] =
                "layout_constraintBaseline_toBaselineOf"
            sAllMargins["layout_constraintBottom_toBottomOf"] = "layout_marginBottom"
            sAllMargins["layout_constraintBottom_toTopOf"] =
                "layout_marginBottom"
            sAllMargins["layout_constraintTop_toBottomOf"] =
                "layout_marginTop"
            sAllMargins["layout_constraintTop_toTopOf"] =
                "layout_marginTop"
            sAllMargins["layout_constraintStart_toStartOf"] = "layout_marginStart"
            sAllMargins["layout_constraintStart_toEndOf"] = "layout_marginStart"
            sAllMargins["layout_constraintEnd_toStartOf"] = "layout_marginEnd"
            sAllMargins["layout_constraintEnd_toEndOf"] = "layout_marginEnd"
            sAllMargins["layout_constraintLeft_toLeftOf"] = "layout_marginLeft"
            sAllMargins["layout_constraintLeft_toRightOf"] = "layout_marginLeft"
            sAllMargins["layout_constraintRight_toRightOf"] =
                "layout_marginRight"
            sAllMargins["layout_constraintRight_toLeftOf"] =
                "layout_marginRight"
        }

        private fun getPxFromDp(dpi: Int, value: String?): Int {
            if (value == null) {
                return 0
            }
            val index = value.indexOf('d')
            if (index == -1) {
                return 0
            }
            val filteredValue = value.substring(0, index)
            return (filteredValue.toFloat() * dpi / 160f).toInt()
        }

        private fun connect(
            dpi: Int,
            set: ConstraintSet,
            view: TView,
            attributes: MutableMap<String?, String>,
            from: Int,
            to: Int
        ) {
            val connection = sAllAttributes[Pair(
                from,
                to
            )]
            val connectionValue = attributes[connection]
            if (connectionValue != null) {
                var marginValue = 0
                val margin = sAllMargins[connection]
                if (margin != null) {
                    marginValue = getPxFromDp(dpi, attributes[margin])
                }
                val id: String = connectionValue!!
                set.connect(view.getId(), from, id, to, marginValue)
            }
        }

        private fun setBias(
            set: ConstraintSet,
            view: TView,
            attributes: MutableMap<String?, String>,
            type: Int
        ) {
            var bias = "layout_constraintHorizontal_bias"
            if (type == VERTICAL) {
                bias = "layout_constraintVertical_bias"
            }
            val biasValue = attributes[bias]
            if (biasValue != null) {
                if (type == HORIZONTAL) {
                    set.setHorizontalBias(view.getId(), biasValue.toFloat())
                } else if (type == VERTICAL) {
                    set.setVerticalBias(view.getId(), biasValue.toFloat())
                }
            }
        }

        private fun setDimensions(
            dpi: Int,
            set: ConstraintSet,
            view: TView,
            attributes: MutableMap<String?, String>,
            type: Int
        ) {
            var dimension = "layout_width"
            if (type == VERTICAL) {
                dimension = "layout_height"
            }
            val dimensionValue = attributes[dimension]
            if (dimensionValue != null) {
                var value: Int = TView.WRAP_CONTENT
                if (dimensionValue != "wrap_content") {
                    value = getPxFromDp(dpi, dimensionValue)
                }
                if (type == HORIZONTAL) {
                    set.constrainWidth(view.getId(), value)
                } else {
                    set.constrainHeight(view.getId(), value)
                }
            }
        }

        private fun setAbsolutePositions(
            dpi: Int,
            set: ConstraintSet,
            view: TView,
            attributes: MutableMap<String?, String>
        ) {
            val absoluteX = attributes["layout_editor_absoluteX"]
            if (absoluteX != null) {
                set.setEditorAbsoluteX(view.getId(), getPxFromDp(dpi, absoluteX))
            }
            val absoluteY = attributes["layout_editor_absoluteY"]
            if (absoluteY != null) {
                set.setEditorAbsoluteY(view.getId(), getPxFromDp(dpi, absoluteY))
            }
        }
    }
}