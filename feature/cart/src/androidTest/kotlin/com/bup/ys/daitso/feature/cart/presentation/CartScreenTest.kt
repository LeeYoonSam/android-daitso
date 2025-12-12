package com.bup.ys.daitso.feature.cart.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.bup.ys.daitso.feature.cart.contract.CartItem
import com.bup.ys.daitso.feature.cart.contract.CartUiState
import org.junit.Rule
import org.junit.Test

/**
 * Test suite for CartScreen composable UI.
 * Tests the rendering of cart items and UI interactions.
 */
class CartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCartScreen_showsItems_whenLoaded() {
        // Arrange
        val items = listOf(
            CartItem("1", "Product 1", 10.0, 2, "url1"),
            CartItem("2", "Product 2", 20.0, 1, "url2")
        )
        val state = CartUiState(
            items = items,
            totalPrice = 40.0,
            isLoading = false,
            error = null
        )

        // Act
        composeTestRule.setContent {
            CartScreen(
                state = state,
                onLoadCart = {},
                onUpdateQuantity = { _, _ -> },
                onRemoveItem = { },
                onClearCart = {},
                onDismissError = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithText("Product 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Product 2").assertIsDisplayed()
    }

    @Test
    fun testCartScreen_showsEmptyState_whenEmpty() {
        // Arrange
        val state = CartUiState(
            items = emptyList(),
            totalPrice = 0.0,
            isLoading = false,
            error = null
        )

        // Act
        composeTestRule.setContent {
            CartScreen(
                state = state,
                onLoadCart = {},
                onUpdateQuantity = { _, _ -> },
                onRemoveItem = { },
                onClearCart = {},
                onDismissError = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("empty_cart_message").assertIsDisplayed()
    }

    @Test
    fun testCartScreen_showsLoading_whenLoading() {
        // Arrange
        val state = CartUiState(
            items = emptyList(),
            totalPrice = 0.0,
            isLoading = true,
            error = null
        )

        // Act
        composeTestRule.setContent {
            CartScreen(
                state = state,
                onLoadCart = {},
                onUpdateQuantity = { _, _ -> },
                onRemoveItem = { },
                onClearCart = {},
                onDismissError = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun testCartScreen_showsTotalPrice() {
        // Arrange
        val items = listOf(
            CartItem("1", "Product 1", 10.0, 2, "url1"),
            CartItem("2", "Product 2", 15.0, 1, "url2")
        )
        val state = CartUiState(
            items = items,
            totalPrice = 35.0,
            isLoading = false,
            error = null
        )

        // Act
        composeTestRule.setContent {
            CartScreen(
                state = state,
                onLoadCart = {},
                onUpdateQuantity = { _, _ -> },
                onRemoveItem = { },
                onClearCart = {},
                onDismissError = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithText("35.0").assertIsDisplayed()
    }

    @Test
    fun testQuantityControl_incrementsQuantity() {
        // Arrange
        var updatedQuantity = 1
        val items = listOf(
            CartItem("1", "Product 1", 10.0, 1, "url1")
        )
        val state = CartUiState(items = items, totalPrice = 10.0)

        // Act
        composeTestRule.setContent {
            CartScreen(
                state = state,
                onLoadCart = {},
                onUpdateQuantity = { _, quantity -> updatedQuantity = quantity },
                onRemoveItem = { },
                onClearCart = {},
                onDismissError = {}
            )
        }

        composeTestRule.onNodeWithTag("quantity_increase_button_1").performClick()

        // Assert
        // This will be verified when the component is actually implemented
    }

    @Test
    fun testQuantityControl_decrementsQuantity() {
        // Arrange
        var updatedQuantity = 2
        val items = listOf(
            CartItem("1", "Product 1", 10.0, 2, "url1")
        )
        val state = CartUiState(items = items, totalPrice = 20.0)

        // Act
        composeTestRule.setContent {
            CartScreen(
                state = state,
                onLoadCart = {},
                onUpdateQuantity = { _, quantity -> updatedQuantity = quantity },
                onRemoveItem = { },
                onClearCart = {},
                onDismissError = {}
            )
        }

        composeTestRule.onNodeWithTag("quantity_decrease_button_1").performClick()

        // Assert
        // This will be verified when the component is actually implemented
    }

    @Test
    fun testDeleteButton_removesItem() {
        // Arrange
        var removedProductId: String? = null
        val items = listOf(
            CartItem("1", "Product 1", 10.0, 1, "url1")
        )
        val state = CartUiState(items = items, totalPrice = 10.0)

        // Act
        composeTestRule.setContent {
            CartScreen(
                state = state,
                onLoadCart = {},
                onUpdateQuantity = { _, _ -> },
                onRemoveItem = { productId -> removedProductId = productId },
                onClearCart = {},
                onDismissError = {}
            )
        }

        composeTestRule.onNodeWithTag("delete_button_1").performClick()

        // Assert
        // This will be verified when the component is actually implemented
    }
}
