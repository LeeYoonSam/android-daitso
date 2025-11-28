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
            imageUrl = "https://example.com/image.jpg"
        )

        // Act
        val json = Json.encodeToString(product)

        // Assert
        assertNotNull(json)
        assert(json.contains("\"id\":\"1\""))
        assert(json.contains("\"name\":\"Test Product\""))
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
                "imageUrl": "https://example.com/image.jpg"
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
    }

    @Test
    fun testProductEquality() {
        // Arrange
        val product1 = Product(
            id = "1",
            name = "Test Product",
            description = "A test product",
            price = 10.99,
            imageUrl = "https://example.com/image.jpg"
        )
        val product2 = Product(
            id = "1",
            name = "Test Product",
            description = "A test product",
            price = 10.99,
            imageUrl = "https://example.com/image.jpg"
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
            imageUrl = "https://example.com/premium.jpg"
        )

        // Assert
        assertEquals("123", product.id)
        assertEquals("Premium Product", product.name)
        assertEquals("High quality item", product.description)
        assertEquals(99.99, product.price)
        assertEquals("https://example.com/premium.jpg", product.imageUrl)
    }
}
