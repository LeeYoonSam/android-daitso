package com.bup.ys.daitso.feature.cart.repository

import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.feature.cart.contract.CartItem
import com.bup.ys.daitso.feature.cart.domain.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of CartRepository interface.
 * Manages cart operations with Room database integration.
 *
 * Provides cart functionality:
 * - Get cart items with reactive updates
 * - Update item quantities (with clamping 1-999)
 * - Remove items from cart
 * - Clear entire cart
 *
 * @param cartDao Data Access Object for cart item database operations
 */
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    companion object {
        private const val QUANTITY_MIN = 1
        private const val QUANTITY_MAX = 999
    }

    /**
     * Get all items currently in the shopping cart.
     * Returns a Flow that emits updates whenever cart changes.
     *
     * Converts database entities to domain CartItem objects.
     *
     * @return Flow emitting list of cart items
     */
    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems().map { entities ->
            entities.map { entity ->
                CartItem(
                    productId = entity.productId,
                    name = entity.productName,
                    price = entity.price,
                    quantity = entity.quantity,
                    imageUrl = entity.imageUrl
                )
            }
        }
    }

    /**
     * Update the quantity of a specific item in the cart.
     * Quantity is clamped between 1 and 999.
     *
     * @param productId The ID of the product to update
     * @param quantity The new quantity (will be clamped to 1-999 range)
     */
    override suspend fun updateQuantity(productId: String, quantity: Int) {
        val clampedQuantity = quantity.coerceIn(QUANTITY_MIN, QUANTITY_MAX)
        cartDao.getCartItemsByProductId(productId).collect { items ->
            items.firstOrNull()?.let { existingItem ->
                val updatedItem = existingItem.copy(quantity = clampedQuantity)
                cartDao.updateCartItem(updatedItem)
            }
        }
    }

    /**
     * Remove a specific item from the cart by product ID.
     *
     * @param productId The ID of the product to remove
     */
    override suspend fun removeItem(productId: String) {
        cartDao.deleteByProductId(productId)
    }

    /**
     * Clear all items from the shopping cart.
     */
    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
