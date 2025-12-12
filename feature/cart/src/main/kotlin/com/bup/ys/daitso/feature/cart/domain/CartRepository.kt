package com.bup.ys.daitso.feature.cart.domain

import com.bup.ys.daitso.feature.cart.contract.CartItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cart operations.
 * Provides abstraction for cart data access and manipulation.
 */
interface CartRepository {
    /**
     * Get all items currently in the shopping cart.
     * Emits updates whenever the cart changes.
     *
     * @return Flow emitting the list of cart items
     */
    fun getCartItems(): Flow<List<CartItem>>

    /**
     * Update the quantity of a specific item in the cart.
     * The quantity will be clamped between 1 and 999.
     *
     * @param productId The ID of the product to update
     * @param quantity The new quantity (will be clamped to valid range)
     */
    suspend fun updateQuantity(productId: String, quantity: Int)

    /**
     * Remove a specific item from the cart.
     *
     * @param productId The ID of the product to remove
     */
    suspend fun removeItem(productId: String)

    /**
     * Clear all items from the shopping cart.
     */
    suspend fun clearCart()
}
