package com.example.dicto.utils.logging

/**
 * Logger - Centralized logging interface
 *
 * Provides consistent logging across the app with support for:
 * - Debug/production mode toggling
 * - Feature-specific loggers
 * - Structured logging
 * - Conditional logging based on BuildConfig
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

object LoggerProvider {
    private var logger: Logger? = null

    fun getLogger(): Logger {
        return logger ?: LoggerImpl().also { logger = it }
    }

    fun setLogger(customLogger: Logger) {
        logger = customLogger
    }
}

