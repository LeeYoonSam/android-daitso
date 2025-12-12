package com.bup.ys.daitso.feature.detail.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.feature.detail.contract.ProductDetailUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI test suite for ProductDetailScreen using Compose test framework.
 * Tests composable rendering and user interactions.
 */
@RunWith(AndroidJUnit4::class)
class ProductDetailScreenTest {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun testLoadingStateShowsProgressIndicator() {
        // Arrange
        val loadingState = ProductDetailUiState(isLoading = true)

        // Act
        composeTestRule.setContent {
            ProductDetailScreenContent(
                state = loadingState,
                onIntentSubmitted = {},
                onNavigateBack = {},
                onNavigateToCart = {}
            )
        }

        // Assert
        composeTestRule
            .onNodeWithTag("loading_progress")
            .assertIsDisplayed()
    }

    @Test
    fun testSuccessStateShowsProductInfo() {
        // Arrange
        val product = Product(
            id = "product-1",
            name = "Test Product",
            price = 29.99,
            description = "A test product",
            imageUrl = "https://example.com/image.jpg",
            stock = 100
        )
        val successState = ProductDetailUiState(product = product)

        // Act
        composeTestRule.setContent {
            ProductDetailScreenContent(
                state = successState,
                onIntentSubmitted = {},
                onNavigateBack = {},
                onNavigateToCart = {}
            )
        }

        // Assert
        composeTestRule
            .onNodeWithText("Test Product")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("$29.99")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("A test product")
            .assertIsDisplayed()
    }

    @Test
    fun testErrorStateShowsErrorMessageWithRetryButton() {
        // Arrange
        val errorState = ProductDetailUiState(error = "Failed to load product")

        // Act
        composeTestRule.setContent {
            ProductDetailScreenContent(
                state = errorState,
                onIntentSubmitted = {},
                onNavigateBack = {},
                onNavigateToCart = {}
            )
        }

        // Assert
        composeTestRule
            .onNodeWithText("Failed to load product")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
    }

    @Test
    fun testQuantitySelectorChangesQuantity() {
        // Arrange
        val product = Product(
            id = "product-1",
            name = "Test Product",
            price = 29.99,
            description = "A test product",
            imageUrl = "https://example.com/image.jpg",
            stock = 100
        )
        val state = ProductDetailUiState(product = product, selectedQuantity = 1)
        var submittedIntent: String? = null

        // Act
        composeTestRule.setContent {
            ProductDetailScreenContent(
                state = state,
                onIntentSubmitted = { submittedIntent = "SetQuantity" },
                onNavigateBack = {},
                onNavigateToCart = {}
            )
        }

        composeTestRule
            .onNodeWithTag("quantity_increment_button")
            .performClick()

        // Assert
        assert(submittedIntent == "SetQuantity")
    }

    @Test
    fun testAddToCartButtonTriggersIntent() {
        // Arrange
        val product = Product(
            id = "product-1",
            name = "Test Product",
            price = 29.99,
            description = "A test product",
            imageUrl = "https://example.com/image.jpg",
            stock = 100
        )
        val state = ProductDetailUiState(product = product)
        var intentSubmitted = false

        // Act
        composeTestRule.setContent {
            ProductDetailScreenContent(
                state = state,
                onIntentSubmitted = { intentSubmitted = true },
                onNavigateBack = {},
                onNavigateToCart = {}
            )
        }

        composeTestRule
            .onNodeWithTag("add_to_cart_button")
            .performClick()

        // Assert
        assert(intentSubmitted)
    }

    @Test
    fun testSnackbarShowsOnSuccess() {
        // Arrange
        val product = Product(
            id = "product-1",
            name = "Test Product",
            price = 29.99,
            description = "A test product",
            imageUrl = "https://example.com/image.jpg",
            stock = 100
        )
        val successState = ProductDetailUiState(product = product, addToCartSuccess = true)

        // Act
        composeTestRule.setContent {
            ProductDetailScreenContent(
                state = successState,
                onIntentSubmitted = {},
                onNavigateBack = {},
                onNavigateToCart = {}
            )
        }

        // Assert - Snackbar should be displayed with success message
        composeTestRule
            .onNodeWithTag("success_snackbar")
            .assertIsDisplayed()
    }
}
