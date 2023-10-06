package dev.topping.ios.constraint

class Log {
    companion object {
        fun v(tag: String, v: String) {
            print("$tag : $v")
        }

        fun w(tag: String, v: String) {
            print("$tag : $v")
        }

        fun d(tag: String, v: String) {
            print("$tag : $v")
        }

        fun e(tag: String, v: String) {
            print("$tag : $v")
        }

        fun e(tag: String, v: String, e: Exception) {
            print("$tag : $e $v")
        }

        fun i(tag: String, v: String) {
            print("$tag : $v")
        }
    }
}