package com.bup.ys.daitso.core.data.datasource

import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Local data source interface for accessing cached data from Room database.
 *
 * Provides offline-first data access through local Room database.
 */
interface LocalDataSource {

    /**
     * Gets all products from local cache.
     *
     * @return Flow of product list (may be empty if no cached data)
     */
    suspend fun getProducts(): List<Product>

    /**
     * Gets a single product by ID from local cache.
     *
     * @param productId The ID of the product to fetch
     * @return The product if found, null otherwise
     */
    suspend fun getProduct(productId: String): Product?

    /**
     * Saves products to local cache.
     *
     * @param products The products to save
     */
    suspend fun saveProducts(products: List<Product>)

    /**
     * Saves a single product to local cache.
     *
     * @param product The product to save
     */
    suspend fun saveProduct(product: Product)

    /**
     * Gets all cart items.
     *
     * @return Flow of cart item list
     */
    fun getCartItems(): Flow<List<CartItem>>

    /**
     * Inserts or updates a cart item.
     *
     * @param cartItem The cart item to insert or update
     */
    suspend fun insertCartItem(cartItem: CartItem)

    /**
     * Deletes a cart item.
     *
     * @param cartItem The cart item to delete
     */
    suspend fun deleteCartItem(cartItem: CartItem)

    /**
     * Clears all cart items.
     */
    suspend fun clearCart()
}
