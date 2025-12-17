package com.bup.ys.daitso.feature.cart.contract

import com.bup.ys.daitso.core.model.CartItem as CoreCartItem
import com.bup.ys.daitso.core.ui.contract.UiEvent
import com.bup.ys.daitso.core.ui.contract.UiSideEffect
import com.bup.ys.daitso.core.ui.contract.UiState

/**
 * Represents the complete UI state for the shopping cart screen.
 *
 * @param items List of items currently in the cart
 * @param totalPrice Total price of all items in the cart
 * @param isLoading True while loading cart data
 * @param error Error message if operation failed
 */
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

/**
 * UI representation of a cart item.
 * Adapter for core::CartItem with UI-specific field names.
 *
 * @param productId Unique identifier for the product
 * @param name Name of the product
 * @param price Unit price of the product
 * @param quantity Quantity of this item in the cart
 * @param imageUrl URL of the product image
 */
data class CartItem(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

/**
 * Convert core CartItem to UI CartItem
 */
fun CoreCartItem.toUICartItem(): CartItem {
    return CartItem(
        productId = this.productId,
        name = this.productName,
        price = this.price,
        quantity = this.quantity,
        imageUrl = this.imageUrl
    )
}

/**
 * Convert UI CartItem to core CartItem
 */
fun CartItem.toCoreCartItem(): CoreCartItem {
    return CoreCartItem(
        productId = this.productId,
        productName = this.name,
        quantity = this.quantity,
        price = this.price,
        imageUrl = this.imageUrl
    )
}

/**
 * User intents (events) in the shopping cart feature.
 * These represent user actions and system notifications.
 */
sealed interface CartIntent : UiEvent {
    /**
     * Request to load cart items from the repository
     */
    object LoadCartItems : CartIntent

    /**
     * Update the quantity of a specific cart item
     */
    data class UpdateQuantity(
        val productId: String,
        val quantity: Int
    ) : CartIntent

    /**
     * Remove a specific item from the cart
     */
    data class RemoveItem(val productId: String) : CartIntent

    /**
     * Clear all items from the cart
     */
    object ClearCart : CartIntent

    /**
     * Dismiss the current error message
     */
    object DismissError : CartIntent
}

/**
 * Alias for CartIntent.
 * Renamed from Intent to Event for better MVI naming convention.
 */
typealias CartEvent = CartIntent

/**
 * Side effects for one-time events like navigation and snackbars.
 * These are not part of the persistent state.
 */
sealed interface CartSideEffect : UiSideEffect {
    /**
     * Navigate to the checkout screen
     */
    object NavigateToCheckout : CartSideEffect

    /**
     * Show a snackbar with a message
     */
    data class ShowToast(val message: String) : CartSideEffect

    /**
     * Navigate back to the home screen
     */
    object NavigateToHome : CartSideEffect
}
