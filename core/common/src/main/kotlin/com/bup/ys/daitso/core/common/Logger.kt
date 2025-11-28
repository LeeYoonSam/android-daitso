package com.bup.ys.daitso.core.common

/**
 * Simple logging utility for the application.
 *
 * Provides convenient methods for logging messages at different levels:
 * - Debug: Development-time diagnostics
 * - Info: General informational messages
 * - Warning: Warning messages for unexpected conditions
 * - Error: Error messages for failures
 */
object Logger {
    /**
     * Logs a debug message.
     *
     * @param tag The tag for log categorization
     * @param message The message to log
     */
    fun debug(tag: String, message: String) {
        println("DEBUG [$tag]: $message")
    }

    /**
     * Logs an info message.
     *
     * @param tag The tag for log categorization
     * @param message The message to log
     */
    fun info(tag: String, message: String) {
        println("INFO [$tag]: $message")
    }

    /**
     * Logs a warning message.
     *
     * @param tag The tag for log categorization
     * @param message The message to log
     */
    fun warn(tag: String, message: String) {
        println("WARN [$tag]: $message")
    }

    /**
     * Logs an error message.
     *
     * @param tag The tag for log categorization
     * @param message The message to log
     * @param throwable Optional throwable to log
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        println("ERROR [$tag]: $message")
        if (throwable != null) {
            throwable.printStackTrace()
        }
    }
}
