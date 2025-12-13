package com.bup.ys.daitso.feature.detail.repository

import com.bup.ys.daitso.core.model.Product
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of CartRepository.
 *
 * This is a simplified implementation for quick development.
 * In production, this would integrate with a real backend and database.
 */
@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {

    // In-memory cache of products
    private val productCache = mutableMapOf<String, Product>()

    // In-memory cart
    private val cart = mutableMapOf<String, Int>()

    init {
        // Initialize with sample products
        initializeSampleProducts()
    }

    /**
     * Initialize the repository with sample products.
     * In production, these would come from a backend API or database.
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

    /**
     * Add a product to the shopping cart.
     *
     * @param productId The ID of the product to add
     * @param quantity The quantity to add (1-999)
     * @return true if successfully added, false if invalid quantity
     */
    override suspend fun addToCart(productId: String, quantity: Int): Boolean {
        // Validate quantity
        if (quantity < 1 || quantity > 999) {
            return false
        }

        // Check if product exists
        if (!productCache.containsKey(productId)) {
            return false
        }

        // Add to cart (in a real implementation, would check stock)
        cart[productId] = quantity

        return true
    }

    /**
     * Get the current cart contents (for testing/debugging purposes).
     *
     * @return Map of product IDs to quantities
     */
    fun getCartContents(): Map<String, Int> = cart.toMap()

    /**
     * Clear the cart (for testing/debugging purposes).
     */
    fun clearCart() {
        cart.clear()
    }
}
