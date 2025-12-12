package com.bup.ys.daitso.feature.detail.repository

import com.bup.ys.daitso.core.model.Product

/**
 * Repository interface for cart operations.
 * Provides abstraction for adding items to cart and retrieving product details.
 */
interface CartRepository {
    /**
     * Retrieve product details by ID.
     *
     * @param productId The ID of the product to retrieve
     * @return The Product object with full details
     * @throws Exception if product not found or network error
     */
    suspend fun getProductDetails(productId: String): Product

    /**
     * Add a product to the shopping cart.
     *
     * @param productId The ID of the product to add
     * @param quantity The quantity to add (1-999)
     * @return true if successfully added, false otherwise
     */
    suspend fun addToCart(productId: String, quantity: Int): Boolean
}
