package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of unified CartRepository interface.
 * Manages all cart operations with Room database integration.
 *
 * Provides comprehensive cart functionality:
 * - Get cart items with reactive updates
 * - Update item quantities (with clamping 1-999)
 * - Remove items from cart
 * - Clear entire cart
 * - Add products to cart
 * - Retrieve product details
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

    // In-memory product cache for product details retrieval
    private val productCache = mutableMapOf<String, Product>()

    init {
        initializeSampleProducts()
    }

    /**
     * Initialize the repository with sample products for demonstration.
     */
    private fun initializeSampleProducts() {
        val sampleProducts = listOf(
            Product(
                id = "product-001",
                name = "Wireless Headphones",
                description = "High-quality wireless headphones with noise cancellation",
                price = 99.99,
                imageUrl = "https://example.com/headphones.jpg",
                category = "Electronics",
                stock = 50
            ),
            Product(
                id = "product-002",
                name = "USB-C Cable",
                description = "Durable USB-C charging cable",
                price = 12.99,
                imageUrl = "https://example.com/cable.jpg",
                category = "Accessories",
                stock = 200
            ),
            Product(
                id = "product-003",
                name = "Phone Stand",
                description = "Adjustable phone stand for desk",
                price = 19.99,
                imageUrl = "https://example.com/stand.jpg",
                category = "Accessories",
                stock = 75
            )
        )

        sampleProducts.forEach { product ->
            productCache[product.id] = product
        }
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
                    productName = entity.productName,
                    quantity = entity.quantity,
                    price = entity.price,
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

    /**
     * Add a product to the shopping cart with specified quantity.
     *
     * @param product The Product to add to cart
     * @param quantity The quantity to add (1-999)
     * @return true if successfully added, false if invalid quantity
     */
    override suspend fun addToCart(product: Product, quantity: Int): Boolean {
        // Validate quantity
        if (quantity < QUANTITY_MIN || quantity > QUANTITY_MAX) {
            return false
        }

        // Create and insert cart item
        val cartItemEntity = CartItemEntity(
            id = "${product.id}_${System.currentTimeMillis()}",
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            price = product.price,
            imageUrl = product.imageUrl
        )

        cartDao.insertCartItem(cartItemEntity)
        return true
    }

    /**
     * Retrieve product details by ID.
     *
     * @param productId The ID of the product to retrieve
     * @return The Product object with full details
     * @throws Exception if product not found
     */
    override suspend fun getProductDetails(productId: String): Product {
        return productCache[productId]
            ?: throw Exception("Product not found: $productId")
    }
}
