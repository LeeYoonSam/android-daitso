package com.bup.ys.daitso.core.ui.contract

/**
 * Marker interface for side effects in MVI architecture.
 *
 * Side effects are one-time events that should not be part of the state,
 * such as showing a toast, navigating to another screen, or triggering a dialog.
 *
 * All side effects in the application should implement this interface to ensure
 * consistent side effect handling across MVI patterns.
 *
 * Side effects should be:
 * - Immutable (preferably sealed interface with object/data classes)
 * - Represent one-time actions that don't belong in state
 * - Be consumed only once and not retained after collection
 *
 * Example:
 * ```kotlin
 * sealed interface HomeSideEffect : UiSideEffect {
 *     data class ShowMessage(val text: String) : HomeSideEffect()
 *     data class NavigateToProductDetail(val productId: String) : HomeSideEffect()
 *     data class OpenUrl(val url: String) : HomeSideEffect()
 * }
 * ```
 */
interface UiSideEffect
