package com.finley.android.qualitytime.util

private object JvmLogger : AppLogger {
    override fun debug(tag: String, message: String) = println("[$tag] $message")
    override fun info(tag: String, message: String) = println("[$tag] $message")
    override fun warn(tag: String, message: String) = println("[$tag] $message")
    override fun error(tag: String, message: String, throwable: Throwable?) {
        println("[$tag] $message" + (throwable?.let { "\n${it.stackTraceToString()}" } ?: ""))
    }
}

actual fun createAppLogger(): AppLogger = JvmLogger