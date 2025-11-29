package com.bup.ys.daitso.core.ui.contract

/**
 * Marker interface for UI events in MVI architecture.
 *
 * All UI events in the application should implement this interface to ensure
 * consistent event handling across MVI patterns.
 *
 * Events should be:
 * - Immutable (preferably sealed interface with object/data classes)
 * - Represent user actions or system notifications
 * - Be processed by the ViewModel to update state
 *
 * Example:
 * ```kotlin
 * sealed interface HomeEvent : UiEvent {
 *     object LoadProducts : HomeEvent()
 *     data class SearchProducts(val query: String) : HomeEvent()
 *     data class SelectProduct(val productId: String) : HomeEvent()
 *     object RetryLoad : HomeEvent()
 * }
 * ```
 */
interface UiEvent
