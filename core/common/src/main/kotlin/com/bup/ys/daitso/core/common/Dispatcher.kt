package com.bup.ys.daitso.core.common

import javax.inject.Qualifier

/**
 * Enum of available Kotlin Coroutine dispatchers.
 *
 * - IO: IO dispatcher for network/database operations
 * - Default: Default dispatcher for CPU-intensive work
 * - Main: Main/UI dispatcher for UI updates
 */
enum class DaitsoDispatchers {
    IO,
    Default,
    Main
}

/**
 * Qualifier annotation for dependency injection of Kotlin Coroutine dispatchers.
 *
 * Used with Hilt to inject the appropriate dispatcher.
 *
 * Example:
 * ```
 * fun myFunction(
 *     @Dispatcher(DaitsoDispatchers.IO) ioDispatcher: CoroutineDispatcher
 * ) { }
 * ```
 *
 * @param dispatcher The dispatcher type to inject
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(val dispatcher: DaitsoDispatchers)
