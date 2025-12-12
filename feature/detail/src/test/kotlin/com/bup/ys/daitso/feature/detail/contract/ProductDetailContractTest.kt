package com.bup.ys.daitso.feature.detail.contract

import com.bup.ys.daitso.core.ui.contract.UiEvent
import com.bup.ys.daitso.core.ui.contract.UiSideEffect
import com.bup.ys.daitso.core.ui.contract.UiState
import org.junit.Test

/**
 * Test suite for ProductDetailContract validation.
 * Ensures all contract classes are properly defined and follow MVI patterns.
 */
class ProductDetailContractTest {

    @Test
    fun testProductDetailUiStateDefaultsAreCorrect() {
        // Arrange & Act
        val state = ProductDetailUiState()

        // Assert
        assert(state.product == null)
        assert(state.selectedQuantity == 1)
        assert(state.isLoading == false)
        assert(state.error == null)
        assert(state.isAddingToCart == false)
        assert(state.addToCartSuccess == false)
    }

    @Test
    fun testProductDetailUiStateImplementsUiState() {
        // Arrange
        val state = ProductDetailUiState()

        // Assert
        assert(state is UiState)
    }

    @Test
    fun testProductDetailIntentLoadProductIsIntent() {
        // Arrange & Act
        val intent = ProductDetailIntent.LoadProduct(productId = "test-123")

        // Assert
        assert(intent is UiEvent)
    }

    @Test
    fun testProductDetailIntentSetQuantityIsIntent() {
        // Arrange & Act
        val intent = ProductDetailIntent.SetQuantity(quantity = 5)

        // Assert
        assert(intent is UiEvent)
    }

    @Test
    fun testProductDetailIntentAddToCartIsIntent() {
        // Arrange & Act
        val intent = ProductDetailIntent.AddToCart

        // Assert
        assert(intent is UiEvent)
    }

    @Test
    fun testProductDetailIntentDismissErrorIsIntent() {
        // Arrange & Act
        val intent = ProductDetailIntent.DismissError

        // Assert
        assert(intent is UiEvent)
    }

    @Test
    fun testProductDetailIntentDismissSuccessIsIntent() {
        // Arrange & Act
        val intent = ProductDetailIntent.DismissSuccess

        // Assert
        assert(intent is UiEvent)
    }

    @Test
    fun testProductDetailSideEffectNavigateBackIsSideEffect() {
        // Arrange & Act
        val effect = ProductDetailSideEffect.NavigateBack

        // Assert
        assert(effect is UiSideEffect)
    }

    @Test
    fun testProductDetailSideEffectShowSnackbarIsSideEffect() {
        // Arrange & Act
        val effect = ProductDetailSideEffect.ShowSnackbar(
            message = "Added to cart",
            actionLabel = "View Cart"
        )

        // Assert
        assert(effect is UiSideEffect)
    }

    @Test
    fun testProductDetailSideEffectNavigateToCartIsSideEffect() {
        // Arrange & Act
        val effect = ProductDetailSideEffect.NavigateToCart

        // Assert
        assert(effect is UiSideEffect)
    }
}
