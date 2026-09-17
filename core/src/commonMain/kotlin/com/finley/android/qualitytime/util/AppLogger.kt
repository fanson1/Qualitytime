package com.finley.android.qualitytime.util

/**
 * Minimal cross-platform logger.
 *
 * Use this everywhere instead of `println` / platform `Log` so that debug
 * output is consistent and can be globally gated or routed later.
 */
interface AppLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

expect fun createAppLogger(): AppLogger

/**
 * Process-wide logger singleton. Each platform provides its own backend
 * (android.util.Log, NSLog, JVM stdout, ...). Debug messages are only emitted
 * when [isDebugEnabled] is true, so hot paths can avoid string allocation.
 */
object AppLog {
    private val backend: AppLogger = createAppLogger()

    fun d(tag: String, message: () -> String) {
        if (isDebugEnabled) backend.debug(tag, message())
    }

    fun i(tag: String, message: () -> String) = backend.info(tag, message())
    fun w(tag: String, message: () -> String) = backend.warn(tag, message())
    fun e(tag: String, message: () -> String, throwable: Throwable? = null) =
        backend.error(tag, message(), throwable)

    /** True for debug builds; false in release, where debug logs are stripped. */
    val isDebugEnabled: Boolean = true
}