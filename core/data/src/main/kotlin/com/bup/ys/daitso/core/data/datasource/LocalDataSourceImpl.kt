package com.bup.ys.daitso.core.data.datasource

import com.bup.ys.daitso.core.database.DaitsoDatabase
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow
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

    override suspend fun getProducts(): List<Product> {
        // Note: In a real app, we would have ProductEntity and ProductDao
        // For now, returning empty list as this requires database schema
        return emptyList()
    }

    override suspend fun getProduct(productId: String): Product? {
        // Note: Would use ProductDao in real implementation
        return null
    }

    override suspend fun saveProducts(products: List<Product>) {
        // Note: Would save to ProductDao in real implementation
    }

    override suspend fun saveProduct(product: Product) {
        // Note: Would save to ProductDao in real implementation
    }

    override fun getCartItems(): Flow<List<CartItem>> {
        return database.cartDao().getAllCartItems().map { entities ->
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
    }

    override suspend fun insertCartItem(cartItem: CartItem) {
        database.cartDao().insertCartItem(
            CartItemEntity(
                productId = cartItem.productId,
                productName = cartItem.productName,
                quantity = cartItem.quantity,
                price = cartItem.price,
                imageUrl = cartItem.imageUrl
            )
        )
    }

    override suspend fun deleteCartItem(cartItem: CartItem) {
        database.cartDao().deleteCartItem(
            CartItemEntity(
                productId = cartItem.productId,
                productName = cartItem.productName,
                quantity = cartItem.quantity,
                price = cartItem.price,
                imageUrl = cartItem.imageUrl
            )
        )
    }

    override suspend fun clearCart() {
        database.cartDao().clearCart()
    }
}
