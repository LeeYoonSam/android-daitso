package com.bup.ys.daitso.feature.cart.contract

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test suite for CartContract models.
 * Validates the data structures and sealed interfaces used in the MVI pattern.
 */
class CartContractTest {

    @Test
    fun testCartUiStateInitialState() {
        // Arrange & Act
        val state = CartUiState()

        // Assert
        assertEquals(emptyList(), state.items)
        assertEquals(0.0, state.totalPrice)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun testCartUiStateWithItems() {
        // Arrange
        val items = listOf(
            CartItem("1", "Product 1", 10.0, 2, "url1"),
            CartItem("2", "Product 2", 20.0, 1, "url2")
        )
        val totalPrice = 40.0

        // Act
        val state = CartUiState(
            items = items,
            totalPrice = totalPrice,
            isLoading = false,
            error = null
        )

        // Assert
        assertEquals(2, state.items.size)
        assertEquals(40.0, state.totalPrice)
        assertEquals("Product 1", state.items[0].name)
    }

    @Test
    fun testCartUiStateErrorState() {
        // Arrange & Act
        val state = CartUiState(error = "Failed to load cart")

        // Assert
        assertEquals("Failed to load cart", state.error)
    }

    @Test
    fun testCartItemDataClass() {
        // Arrange & Act
        val item = CartItem(
            productId = "prod-123",
            name = "Test Product",
            price = 29.99,
            quantity = 3,
            imageUrl = "https://example.com/image.jpg"
        )

        // Assert
        assertEquals("prod-123", item.productId)
        assertEquals("Test Product", item.name)
        assertEquals(29.99, item.price)
        assertEquals(3, item.quantity)
        assertEquals("https://example.com/image.jpg", item.imageUrl)
    }

    @Test
    fun testLoadCartItemsIntent() {
        // Arrange & Act
        val intent: CartIntent = CartIntent.LoadCartItems

        // Assert
        assertIs<CartIntent.LoadCartItems>(intent)
    }

    @Test
    fun testUpdateQuantityIntent() {
        // Arrange & Act
        val intent: CartIntent = CartIntent.UpdateQuantity("prod-1", 5)

        // Assert
        assertIs<CartIntent.UpdateQuantity>(intent)
        assertEquals("prod-1", (intent as CartIntent.UpdateQuantity).productId)
        assertEquals(5, intent.quantity)
    }

    @Test
    fun testRemoveItemIntent() {
        // Arrange & Act
        val intent: CartIntent = CartIntent.RemoveItem("prod-1")

        // Assert
        assertIs<CartIntent.RemoveItem>(intent)
        assertEquals("prod-1", (intent as CartIntent.RemoveItem).productId)
    }

    @Test
    fun testClearCartIntent() {
        // Arrange & Act
        val intent: CartIntent = CartIntent.ClearCart

        // Assert
        assertIs<CartIntent.ClearCart>(intent)
    }

    @Test
    fun testDismissErrorIntent() {
        // Arrange & Act
        val intent: CartIntent = CartIntent.DismissError

        // Assert
        assertIs<CartIntent.DismissError>(intent)
    }

    @Test
    fun testNavigateToCheckoutSideEffect() {
        // Arrange & Act
        val effect: CartSideEffect = CartSideEffect.NavigateToCheckout

        // Assert
        assertIs<CartSideEffect.NavigateToCheckout>(effect)
    }

    @Test
    fun testShowToastSideEffect() {
        // Arrange & Act
        val effect: CartSideEffect = CartSideEffect.ShowToast("Item added successfully")

        // Assert
        assertIs<CartSideEffect.ShowToast>(effect)
        assertEquals("Item added successfully", (effect as CartSideEffect.ShowToast).message)
    }

    @Test
    fun testNavigateToHomeSideEffect() {
        // Arrange & Act
        val effect: CartSideEffect = CartSideEffect.NavigateToHome

        // Assert
        assertIs<CartSideEffect.NavigateToHome>(effect)
    }

    @Test
    fun testCartUiStateCopy() {
        // Arrange
        val state = CartUiState(
            items = listOf(CartItem("1", "Product 1", 10.0, 1, "url")),
            totalPrice = 10.0,
            isLoading = false,
            error = null
        )

        // Act
        val updatedState = state.copy(isLoading = true)

        // Assert
        assertEquals(1, updatedState.items.size)
        assertEquals(true, updatedState.isLoading)
        assertEquals(null, updatedState.error)
    }
}
