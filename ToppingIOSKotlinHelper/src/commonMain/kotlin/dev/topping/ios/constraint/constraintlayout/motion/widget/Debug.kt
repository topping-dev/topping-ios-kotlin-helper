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
import dev.topping.ios.constraint.TView

// @TODO: add description
/**
 * Utilities useful for debugging
 *
 */
object Debug {
    /**
     * This logs n elements in the stack
     *
     * @param tag
     * @param msg
     * @param n
     */
    fun logStack(tag: String?, msg: String, n: Int) {
        /*var n = n
        val st: Array<StackTraceElement> = Throwable().getStackTrace()
        var s = " "
        n = Math.min(n, st.size - 1)
        for (i in 1..n) {
            @SuppressWarnings("unused") val ste: StackTraceElement = st[i]
            val stack = ".(" + st[i].getFileName().toString() + ":" + st[i].getLineNumber()
                .toString() + ") " + st[i].getMethodName()
            s += " "
            Log.v(tag, msg + s + stack + s)
        }*/
    }

    /**
     * This logs n elements in the stack
     *
     * @param msg
     * @param n
     */
    fun printStack(msg: String, n: Int) {
        /*var n = n
        val st: Array<StackTraceElement> = Throwable().getStackTrace()
        var s = " "
        n = Math.min(n, st.size - 1)
        for (i in 1..n) {
            @SuppressWarnings("unused") val ste: StackTraceElement = st[i]
            val stack = ".(" + st[i].getFileName().toString() + ":" + st[i].getLineNumber()
                .toString() + ") "
            s += " "
            System.out.println(msg + s + stack + s)
        }*/
    }

    /**
     * This provides return the name of a view
     *
     * @param view
     * @return name of view
     */
    fun getName(view: TView?): String {
        return try {
            val context: TContext = view?.getContext()!!
            context.getResources().getResourceEntryName(view.getId())
        } catch (ex: Exception) {
            "UNKNOWN"
        }
    }
    // @TODO: add description
    /**
     * @param obj
     */
    fun dumpPoc(obj: Any) {
        /*val s: StackTraceElement = Throwable().getStackTrace()[1]
        val loc = ".(" + s.getFileName().toString() + ":" + s.getLineNumber().toString() + ")"
        val c: TClass = obj.getClass()
        System.out.println(loc + "------------- " + c.getName() + " --------------------")
        val declaredFields: Array<Field> = c.getFields()
        for (i in declaredFields.indices) {
            val declaredField: Field = declaredFields[i]
            try {
                val value: Object = declaredField.get(obj)
                if (!declaredField.getName().startsWith("layout_constraint")) {
                    continue
                }
                if (value is Integer && value.toString().equals("-1")) {
                    continue
                }
                if (value is Integer && value.toString().equals("0")) {
                    continue
                }
                if (value is Float && value.toString().equals("1.0")) {
                    continue
                }
                if (value is Float && value.toString().equals("0.5")) {
                    continue
                }
                System.out.println(loc + "    " + declaredField.getName() + " " + value)
            } catch (e: IllegalAccessException) {
            }
        }
        System.out.println(loc + "------------- " + c.getSimpleName() + " --------------------")*/
    }

    /**
     * This returns the name of a view given its id
     *
     * @param context
     * @param id
     * @return name of view
     */
    fun getName(context: TContext, id: String): String {
        return try {
            if (id != "") {
                context.getResources().getResourceEntryName(id)
            } else {
                "UNKNOWN"
            }
        } catch (ex: Exception) {
            "?$id"
        }
    }

    /**
     * This returns the name of a view given its id
     *
     * @param context
     * @param id
     * @return name of view
     */
    fun getName(context: TContext, id: Array<String>?): String {
        /*return try {
            var str: String? = id.size.toString() + "["
            for (i in id.indices) {
                str += if (i == 0) "" else " "
                var tmp: String? = null
                tmp = try {
                    context.getResources().getResourceEntryName(id[i])
                } catch (e: NotFoundException) {
                    "? " + id[i] + " "
                }
                str += tmp
            }
            str.toString() + "]"
        } catch (ex: Exception) {
            Log.v("DEBUG", ex.toString())
            "UNKNOWN"
        }*/
        return "UNKNOWN"
    }

    /**
     * convert an id number to an id String useful in debugging
     *
     * @param layout
     * @param stateId
     * @return
     */
    fun getState(layout: MotionLayout, stateId: String): String {
        return getState(layout, stateId, -1)
    }

    /**
     * convert an id number to an id String useful in debugging
     *
     * @param layout
     * @param stateId
     * @param len     trim if string > len
     * @return
     */
    fun getState(layout: MotionLayout, stateId: String, len: Int): String {
        if (stateId == "") {
            return "UNDEFINED"
        }
        val context: TContext = layout.self.getContext()
        var str: String = context.getResources().getResourceEntryName(stateId)
        if (len != -1) {
            if (str.length > len) {
                str = str.replace("([^_])[aeiou]+", "$1") // del vowels ! at start
            }
            if (str.length > len) {
                val n: Int = str.replace("[^_]", "").length // count number of "_"
                if (n > 0) {
                    val extra: Int = (str.length - len) / n
                    /*val reg: String =
                        CharBuffer.allocate(extra).toString().replace('\u0000', '.') + "_"
                    str = str.replace(reg, "_")*/
                }
            }
        }
        return str
    }

    /**
     * Convert a motion event action to a string
     *
     * @param event
     * @return
     */
    fun getActionType(event: Any): String {
        /*val type: Int = event.getAction()
        val fields: Array<Field> = MotionEvent::class.java.getFields()
        for (i in fields.indices) {
            val field: Field = fields[i]
            try {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && field.getType().equals(Integer.TYPE)
                    && field.getInt(null) === type
                ) {
                    return field.getName()
                }
            } catch (e: IllegalAccessException) {
            }
        }*/
        return "---"
    }

    /**
     * Get file name and location where this method is called.
     * Formatting it such that it is clickable by Intellij
     *
     * @return (filename : line_no)
     */
    val location: String = ""
        /*get() {
            val s: StackTraceElement = Throwable().getStackTrace()[1]
            return ".(" + s.getFileName().toString() + ":" + s.getLineNumber().toString() + ")"
        }*/

    /**
     * Get file name and location where this method is called.
     * Formatting it such that it is clickable by Intellij
     *
     * @return (filename : line_no)
     */
    val loc: String = ""
        /*get() {
            val s: StackTraceElement = Throwable().getStackTrace()[1]
            return ".(" + s.getFileName().toString() + ":" + s.getLineNumber()
                .toString() + ") " + s.getMethodName().toString() + "()"
        }*/

    /**
     * Get file name and location where this method is called.
     * Formatting it such that it is clickable by Intellij
     *
     * @return (filename : line_no)
     */
    val location2: String = ""
        /*get() {
            val s: StackTraceElement = Throwable().getStackTrace()[2]
            return ".(" + s.getFileName().toString() + ":" + s.getLineNumber().toString() + ")"
        }*/

    /**
     * Get file name and location where this method is called.
     * Formatting it such that it is clickable by Intellij
     *
     * @return (filename : line_no)
     */
    fun getCallFrom(n: Int): String {
        /*val s: StackTraceElement = Throwable().getStackTrace()[2 + n]
        return ".(" + s.getFileName().toString() + ":" + s.getLineNumber().toString() + ")"*/
        return ""
    }
    // @TODO: add description
    /**
     *
     * @param layout
     * @param str
     */
    fun dumpLayoutParams(layout: TView, str: String) {
        /*val s: StackTraceElement = Throwable().getStackTrace()[1]
        val loc = ".(" + s.getFileName().toString() + ":" + s.getLineNumber()
            .toString() + ") " + str + "  "
        val n: Int = layout.getChildCount()
        System.out.println("$str children $n")
        for (i in 0 until n) {
            val v: TView = layout.getChildAt(i)
            System.out.println(loc + "     " + getName(v))
            val param: ViewGroup.LayoutParams = v.getLayoutParams()
            val declaredFields: Array<Field> = param.getClass().getFields()
            for (k in declaredFields.indices) {
                val declaredField: Field = declaredFields[k]
                try {
                    val value: Object = declaredField.get(param)
                    val name: String = declaredField.getName()
                    if (!name.contains("To")) {
                        continue
                    }
                    if (value.toString().equals("-1")) {
                        continue
                    }
                    System.out.println(loc + "       " + declaredField.getName() + " " + value)
                } catch (e: IllegalAccessException) {
                }
            }
        }*/
    }
    // @TODO: add description
    /**
     *
     * @param param
     * @param str
     */
    fun dumpLayoutParams(param: Any, str: String) {
        /*val s: StackTraceElement = Throwable().getStackTrace()[1]
        val loc = ".(" + s.getFileName().toString() + ":" + s.getLineNumber()
            .toString() + ") " + str + "  "
        System.out.println(" >>>>>>>>>>>>>>>>>>. dump " + loc + "  " + param.getClass().getName())
        val declaredFields: Array<Field> = param.getClass().getFields()
        for (k in declaredFields.indices) {
            val declaredField: Field = declaredFields[k]
            try {
                val value: Object = declaredField.get(param)
                val name: String = declaredField.getName()
                if (!name.contains("To")) {
                    continue
                }
                if (value.toString().equals("-1")) {
                    continue
                }
                //                    if (value instanceof  Integer && value.toString().equals("-1")) {
//                        continue;
//                    }
                System.out.println("$loc       $name $value")
            } catch (e: IllegalAccessException) {
            }
        }
        System.out.println(" <<<<<<<<<<<<<<<<< dump $loc")*/
    }
}