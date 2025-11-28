package com.bup.ys.daitso.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for CartItem data class.
 *
 * Tests cover:
 * - CartItem serialization and deserialization
 * - Quantity and total price calculations
 * - CartItem equality
 */
class CartItemTest {

    @Test
    fun testCartItemSerialization() {
        // Arrange
        val cartItem = CartItem(
            id = "1",
            productId = "prod-1",
            productName = "Test Product",
            quantity = 2,
            price = 10.99
        )

        // Act
        val json = Json.encodeToString(cartItem)

        // Assert
        assertNotNull(json)
        assert(json.contains("\"id\":\"1\""))
        assert(json.contains("\"productId\":\"prod-1\""))
        assert(json.contains("\"quantity\":2"))
    }

    @Test
    fun testCartItemDeserialization() {
        // Arrange
        val jsonString = """
            {
                "id": "1",
                "productId": "prod-1",
                "productName": "Test Product",
                "quantity": 3,
                "price": 15.50
            }
        """.trimIndent()

        // Act
        val cartItem = Json.decodeFromString<CartItem>(jsonString)

        // Assert
        assertEquals("1", cartItem.id)
        assertEquals("prod-1", cartItem.productId)
        assertEquals("Test Product", cartItem.productName)
        assertEquals(3, cartItem.quantity)
        assertEquals(15.50, cartItem.price)
    }

    @Test
    fun testCartItemEquality() {
        // Arrange
        val item1 = CartItem(
            id = "1",
            productId = "prod-1",
            productName = "Test",
            quantity = 2,
            price = 10.0
        )
        val item2 = CartItem(
            id = "1",
            productId = "prod-1",
            productName = "Test",
            quantity = 2,
            price = 10.0
        )

        // Assert
        assertEquals(item1, item2)
    }

    @Test
    fun testCartItemTotalPrice() {
        // Arrange
        val cartItem = CartItem(
            id = "1",
            productId = "prod-1",
            productName = "Test Product",
            quantity = 5,
            price = 10.0
        )

        // Act
        val totalPrice = cartItem.quantity * cartItem.price

        // Assert
        assertEquals(50.0, totalPrice)
    }
}
