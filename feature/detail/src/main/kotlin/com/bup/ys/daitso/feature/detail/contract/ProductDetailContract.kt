package com.bup.ys.daitso.feature.detail.contract

import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.core.ui.contract.UiEvent
import com.bup.ys.daitso.core.ui.contract.UiSideEffect
import com.bup.ys.daitso.core.ui.contract.UiState

/**
 * Represents the complete UI state for the product detail screen.
 *
 * @param product The currently displayed product, null if not yet loaded
 * @param selectedQuantity The quantity selected by the user (1-999 range)
 * @param isLoading True while loading product data
 * @param error Error message if loading failed
 * @param isAddingToCart True while adding to cart
 * @param addToCartSuccess True when item was successfully added to cart
 */
data class ProductDetailUiState(
    val product: Product? = null,
    val selectedQuantity: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingToCart: Boolean = false,
    val addToCartSuccess: Boolean = false
) : UiState

/**
 * User intents (events) in the product detail feature.
 * These represent user actions and system notifications.
 */
sealed interface ProductDetailIntent : UiEvent {
    /**
     * Request to load product details by ID
     */
    data class LoadProduct(val productId: String) : ProductDetailIntent

    /**
     * Update the selected quantity
     */
    data class SetQuantity(val quantity: Int) : ProductDetailIntent

    /**
     * Request to add selected product to shopping cart
     */
    object AddToCart : ProductDetailIntent

    /**
     * Dismiss the current error message
     */
    object DismissError : ProductDetailIntent

    /**
     * Dismiss the success message
     */
    object DismissSuccess : ProductDetailIntent
}

/**
 * Side effects for one-time events like navigation and snackbars.
 * These are not part of the persistent state.
 */
sealed interface ProductDetailSideEffect : UiSideEffect {
    /**
     * Navigate back to the previous screen
     */
    object NavigateBack : ProductDetailSideEffect

    /**
     * Show a snackbar message with optional action button
     */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String?
    ) : ProductDetailSideEffect

    /**
     * Navigate to the shopping cart screen
     */
    object NavigateToCart : ProductDetailSideEffect
}
