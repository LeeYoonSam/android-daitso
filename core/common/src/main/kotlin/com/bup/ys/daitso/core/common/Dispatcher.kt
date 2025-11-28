package com.bup.ys.daitso.core.common

/**
 * Enum of available Kotlin Coroutine dispatchers.
 *
 * - Main: Main/UI dispatcher for UI updates
 * - IO: IO dispatcher for network/database operations
 * - Default: Default dispatcher for CPU-intensive work
 */
enum class Dispatcher {
    Main,
    IO,
    Default
}

/**
 * Annotation to mark the dispatcher context for a function.
 *
 * Used to document which dispatcher a suspend function runs on.
 *
 * Example:
 * ```
 * @DispatcherType(Dispatcher.IO)
 * suspend fun fetchData(): Result<Data> { }
 * ```
 *
 * @param dispatcher The dispatcher context
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DispatcherType(val dispatcher: Dispatcher)
