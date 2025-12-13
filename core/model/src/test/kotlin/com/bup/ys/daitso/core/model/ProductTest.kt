package com.bup.ys.daitso.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for Product data class.
 *
 * Tests cover:
 * - Product serialization to JSON
 * - Product deserialization from JSON
 * - Product equality
 */
class ProductTest {

    @Test
    fun testProductSerialization() {
        // Arrange
        val product = Product(
            id = "1",
            name = "Test Product",
            description = "A test product",
            price = 10.99,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics"
        )

        // Act
        val json = Json.encodeToString(product)

        // Assert
        assertNotNull(json)
        assert(json.contains("\"id\":\"1\""))
        assert(json.contains("\"name\":\"Test Product\""))
        assert(json.contains("\"category\":\"Electronics\""))
    }

    @Test
    fun testProductDeserialization() {
        // Arrange
        val jsonString = """
            {
                "id": "1",
                "name": "Test Product",
                "description": "A test product",
                "price": 10.99,
                "imageUrl": "https://example.com/image.jpg",
                "category": "Electronics"
            }
        """.trimIndent()

        // Act
        val product = Json.decodeFromString<Product>(jsonString)

        // Assert
        assertEquals("1", product.id)
        assertEquals("Test Product", product.name)
        assertEquals("A test product", product.description)
        assertEquals(10.99, product.price)
        assertEquals("https://example.com/image.jpg", product.imageUrl)
        assertEquals("Electronics", product.category)
    }

    @Test
    fun testProductEquality() {
        // Arrange
        val product1 = Product(
            id = "1",
            name = "Test Product",
            description = "A test product",
            price = 10.99,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics"
        )
        val product2 = Product(
            id = "1",
            name = "Test Product",
            description = "A test product",
            price = 10.99,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics"
        )

        // Assert
        assertEquals(product1, product2)
    }

    @Test
    fun testProductDataClass() {
        // Arrange & Act
        val product = Product(
            id = "123",
            name = "Premium Product",
            description = "High quality item",
            price = 99.99,
            imageUrl = "https://example.com/premium.jpg",
            category = "Luxury"
        )

        // Assert
        assertEquals("123", product.id)
        assertEquals("Premium Product", product.name)
        assertEquals("High quality item", product.description)
        assertEquals(99.99, product.price)
        assertEquals("https://example.com/premium.jpg", product.imageUrl)
        assertEquals("Luxury", product.category)
    }

    @Test
    fun testProductWithStock() {
        // Arrange & Act
        val product = Product(
            id = "product-456",
            name = "In Stock Product",
            description = "Product with stock",
            price = 49.99,
            imageUrl = "https://example.com/stock.jpg",
            category = "General",
            stock = 100
        )

        // Assert
        assertEquals("product-456", product.id)
        assertEquals("In Stock Product", product.name)
        assertEquals(49.99, product.price)
        assertEquals(100, product.stock)
    }

    @Test
    fun testProductStockZero() {
        // Arrange & Act
        val product = Product(
            id = "product-789",
            name = "Out of Stock Product",
            description = "Product out of stock",
            price = 29.99,
            imageUrl = "https://example.com/oos.jpg",
            category = "General",
            stock = 0
        )

        // Assert
        assertEquals(0, product.stock)
    }

    @Test
    fun testProductWithStockDeserialization() {
        // Arrange
        val jsonString = """
            {
                "id": "product-999",
                "name": "Stocked Item",
                "description": "Item with stock field",
                "price": 59.99,
                "imageUrl": "https://example.com/item.jpg",
                "category": "Electronics",
                "stock": 250
            }
        """.trimIndent()

        // Act
        val product = Json.decodeFromString<Product>(jsonString)

        // Assert
        assertEquals("product-999", product.id)
        assertEquals(250, product.stock)
    }
}
