package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Unified repository interface for all cart operations.
 * Provides abstraction for cart data access and manipulation across the application.
 *
 * This interface combines operations from both:
 * - feature:detail cart operations (adding products to cart)
 * - feature:cart cart management operations (viewing, updating, removing items)
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
     * @throws Exception if product not found in cart
     */
    suspend fun updateQuantity(productId: String, quantity: Int)

    /**
     * Remove a specific item from the cart.
     *
     * @param productId The ID of the product to remove
     * @throws Exception if product not found in cart
     */
    suspend fun removeItem(productId: String)

    /**
     * Clear all items from the shopping cart.
     */
    suspend fun clearCart()

    /**
     * Add a product to the shopping cart with specified quantity.
     * If product already exists in cart, quantity is replaced (not added to existing).
     *
     * @param product The Product to add to cart
     * @param quantity The quantity to add (1-999)
     * @return true if successfully added, false if invalid quantity
     * @throws Exception if operation fails
     */
    suspend fun addToCart(product: Product, quantity: Int): Boolean

    /**
     * Retrieve product details by ID.
     * This is needed for loading product information before adding to cart.
     *
     * @param productId The ID of the product to retrieve
     * @return The Product object with full details
     * @throws Exception if product not found
     */
    suspend fun getProductDetails(productId: String): Product
}
