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
package dev.topping.ios.constraint.core.motion.utils

import dev.topping.ios.constraint.pow

class Utils {
    // @TODO: add description
    fun getInterpolatedColor(value: FloatArray): Int {
        val r = clamp(
            (pow(
                value[0], 1.0 / 2.2
            ).toFloat() * 255.0f).toInt()
        )
        val g = clamp(
            (pow(
                value[1], 1.0 / 2.2
            ).toFloat() * 255.0f).toInt()
        )
        val b = clamp(
            (pow(
                value[2], 1.0 / 2.2
            ).toFloat() * 255.0f).toInt()
        )
        val a =
            clamp((value[3] * 255.0f).toInt())
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    interface DebugHandle {
        // @TODO: add description
        fun message(str: String?)
    }

    companion object {
        // @TODO: add description
        fun log(tag: String, value: String) {
            println("$tag : $value")
        }

        // @TODO: add description
        fun loge(tag: String, value: String) {
            println("$tag : $value")
        }

        // @TODO: add description
        fun socketSend(str: String) {
            /*try {
                val socket = Socket("127.0.0.1", 5327)
                val out: OutputStream = socket.getOutputStream()
                out.write(str.getBytes())
                out.close()
            } catch (e: IOException) {
                //TODO replace with something not equal to printStackTrace();
                System.err.println(
                    e.toString() + "\n" + Arrays.toString(e.getStackTrace())
                        .replace("[", "   at ")
                        .replace(",", "\n   at")
                        .replace("]", "")
                )
            }*/
        }

        private fun clamp(c: Int): Int {
            var c = c
            val n = 255
            c = c and (c shr 31).inv()
            c -= n
            c = c and (c shr 31)
            c += n
            return c
        }

        // @TODO: add description
        fun rgbaTocColor(
            r: Float,
            g: Float,
            b: Float,
            a: Float
        ): Int {
            val ir =
                clamp((r * 255f).toInt())
            val ig =
                clamp((g * 255f).toInt())
            val ib =
                clamp((b * 255f).toInt())
            val ia =
                clamp((a * 255f).toInt())
            return ia shl 24 or (ir shl 16) or (ig shl 8) or ib
        }

        var sOurHandle: DebugHandle? = null
        fun setDebugHandle(handle: DebugHandle?) {
            sOurHandle = handle
        }

        // @TODO: add description
        fun logStack(msg: String, n: Int) {
            /*var n = n
            val st: Array<StackTraceElement> = Throwable().getStackTrace()
            var s = " "
            n = min(n, st.size - 1)
            for (i in 1..n) {
                val ste: StackTraceElement = st[i]
                val stack = (".(" + ste.getFileName().toString() + ":"
                        + ste.getLineNumber().toString() + ") " + ste.getMethodName())
                s += " "
                System.out.println(msg + s + stack + s)
            }*/
        }

        // @TODO: add description
        fun log(str: String) {
            /*val s: StackTraceElement = Throwable().getStackTrace()[1]
            var methodName: String = s.getMethodName()
            methodName = "$methodName                  ".substring(0, 17)
            val npad: String = "    ".substring(Integer.toString(s.getLineNumber()).length())
            val ss = ".(" + s.getFileName().toString() + ":" + s.getLineNumber()
                .toString() + ")" + npad + methodName
            System.out.println("$ss $str")
            if (sOurHandle != null) {
                sOurHandle!!.message("$ss $str")
            }*/
        }
    }
}