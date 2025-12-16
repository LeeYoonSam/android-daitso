package com.bup.ys.daitso.core.data.datasource

import android.util.Log
import com.bup.ys.daitso.core.database.DaitsoDatabase
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.core.database.entity.toEntity
import com.bup.ys.daitso.core.database.entity.toDomainModel
import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of LocalDataSource using Room database.
 *
 * Provides local caching of products and cart items.
 *
 * @param database The Room database instance
 */
class LocalDataSourceImpl @Inject constructor(
    private val database: DaitsoDatabase
) : LocalDataSource {

    /**
     * Retrieves all cached products from the database.
     *
     * @return List of products, or empty list if database error occurs
     */
    override suspend fun getProducts(): List<Product> {
        return try {
            database.productDao().getProducts().first().map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load products", e)
            emptyList()
        }
    }

    /**
     * Retrieves a specific product by ID from the database.
     *
     * @param productId The ID of the product to retrieve
     * @return The product if found, null otherwise or on database error
     */
    override suspend fun getProduct(productId: String): Product? {
        return try {
            database.productDao().getProductById(productId)?.toDomainModel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load product with id: $productId", e)
            null
        }
    }

    /**
     * Saves a list of products to the database.
     *
     * @param products The list of products to save
     */
    override suspend fun saveProducts(products: List<Product>) {
        try {
            database.productDao().insertProducts(products.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save products", e)
        }
    }

    /**
     * Saves a single product to the database.
     *
     * @param product The product to save
     */
    override suspend fun saveProduct(product: Product) {
        try {
            database.productDao().insertProduct(product.toEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save product with id: ${product.id}", e)
        }
    }

    /**
     * Retrieves all cart items as a Flow.
     *
     * Lazy access to database.cartDao() to avoid RoomDatabase class access issues during compilation.
     *
     * @return A Flow of cart item lists
     */
    override fun getCartItems(): Flow<List<CartItem>> {
        return try {
            database.cartDao().getAllCartItems().map { entities ->
                entities.map { entity ->
                    CartItem(
                        productId = entity.productId,
                        productName = entity.productName,
                        quantity = entity.quantity,
                        price = entity.price,
                        imageUrl = entity.imageUrl
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load cart items", e)
            kotlinx.coroutines.flow.emptyFlow()
        }
    }

    /**
     * Inserts a cart item into the database.
     *
     * @param cartItem The cart item to insert
     */
    override suspend fun insertCartItem(cartItem: CartItem) {
        try {
            database.cartDao().insertCartItem(cartItem.toEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert cart item for product: ${cartItem.productId}", e)
        }
    }

    /**
     * Deletes a cart item from the database.
     *
     * Note: This is a simplified implementation using product ID.
     * In a real app, we'd need to match by product ID or have a unique identifier for each cart item.
     *
     * @param cartItem The cart item to delete
     */
    override suspend fun deleteCartItem(cartItem: CartItem) {
        try {
            database.cartDao().deleteByProductId(cartItem.productId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete cart item for product: ${cartItem.productId}", e)
        }
    }

    /**
     * Clears all items from the cart.
     */
    override suspend fun clearCart() {
        try {
            database.cartDao().clearCart()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cart", e)
        }
    }

    companion object {
        private const val TAG = "LocalDataSourceImpl"
    }
}
