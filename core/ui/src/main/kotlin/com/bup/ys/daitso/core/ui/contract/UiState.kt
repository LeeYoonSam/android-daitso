package com.bup.ys.daitso.core.ui.contract

/**
 * Marker interface for UI state in MVI architecture.
 *
 * All UI states in the application should implement this interface to ensure
 * consistent state management across MVI patterns.
 *
 * States should be:
 * - Immutable (preferably sealed interface with data classes)
 * - Serializable when needed
 * - Represent the complete UI state at a given moment
 *
 * Example:
 * ```kotlin
 * sealed interface HomeState : UiState {
 *     object Initial : HomeState()
 *     object Loading : HomeState()
 *     data class Success(val products: List<Product>) : HomeState()
 *     data class Error(val message: String) : HomeState()
 * }
 * ```
 */
interface UiState
