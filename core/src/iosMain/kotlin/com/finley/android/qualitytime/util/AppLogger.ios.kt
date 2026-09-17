package com.finley.android.qualitytime.util

import platform.Foundation.NSLog

private object IosLogger : AppLogger {
    override fun debug(tag: String, message: String) = NSLog("%@", "[$tag] $message")
    override fun info(tag: String, message: String) = NSLog("%@", "[$tag] $message")
    override fun warn(tag: String, message: String) = NSLog("%@", "[$tag] $message")
    override fun error(tag: String, message: String, throwable: Throwable?) {
        NSLog("%@", "[$tag] $message")
        throwable?.let { NSLog("%@", "[$tag] ${it.message ?: it.toString()}") }
    }
}

actual fun createAppLogger(): AppLogger = IosLogger