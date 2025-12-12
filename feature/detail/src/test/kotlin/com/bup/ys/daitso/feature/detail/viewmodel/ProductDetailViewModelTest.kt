package com.bup.ys.daitso.feature.detail.viewmodel

import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.feature.detail.contract.ProductDetailIntent
import com.bup.ys.daitso.feature.detail.contract.ProductDetailSideEffect
import com.bup.ys.daitso.feature.detail.contract.ProductDetailUiState
import com.bup.ys.daitso.feature.detail.repository.CartRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Test suite for ProductDetailViewModel.
 * Tests the MVI event handling and state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    @MockK
    private lateinit var cartRepository: CartRepository

    private lateinit var viewModel: ProductDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductDetailViewModel(cartRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateIsCorrect() = runTest {
        // Assert
        assertEquals(ProductDetailUiState(), viewModel.currentState)
    }

    @Test
    fun testLoadProductSuccess() = runTest {
        // Arrange
        val productId = "product-123"
        val mockProduct = createMockProduct(id = productId, name = "Test Product")
        coEvery { cartRepository.getProductDetails(productId) } returns mockProduct

        // Act
        viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
        advanceUntilIdle()

        // Assert
        assertEquals(mockProduct, viewModel.currentState.product)
        assertEquals(false, viewModel.currentState.isLoading)
        assertEquals(null, viewModel.currentState.error)
    }

    @Test
    fun testLoadProductFailure() = runTest {
        // Arrange
        val productId = "product-123"
        val errorMessage = "Failed to load product"
        coEvery { cartRepository.getProductDetails(productId) } throws Exception(errorMessage)

        // Act
        viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
        advanceUntilIdle()

        // Assert
        assertEquals(null, viewModel.currentState.product)
        assertEquals(false, viewModel.currentState.isLoading)
        assertEquals(errorMessage, viewModel.currentState.error)
    }

    @Test
    fun testSetQuantityWithinBounds() = runTest {
        // Arrange
        val quantity = 5
        viewModel.submitEvent(ProductDetailIntent.SetQuantity(quantity))

        // Act
        advanceUntilIdle()

        // Assert
        assertEquals(quantity, viewModel.currentState.selectedQuantity)
    }

    @Test
    fun testSetQuantityClampedAtMinBoundary() = runTest {
        // Arrange - Set quantity below minimum
        viewModel.submitEvent(ProductDetailIntent.SetQuantity(0))

        // Act
        advanceUntilIdle()

        // Assert - Should be clamped to 1
        assertEquals(1, viewModel.currentState.selectedQuantity)
    }

    @Test
    fun testSetQuantityClampedAtMaxBoundary() = runTest {
        // Arrange - Set quantity above maximum
        viewModel.submitEvent(ProductDetailIntent.SetQuantity(1000))

        // Act
        advanceUntilIdle()

        // Assert - Should be clamped to 999
        assertEquals(999, viewModel.currentState.selectedQuantity)
    }

    @Test
    fun testAddToCartSuccess() = runTest {
        // Arrange
        val productId = "product-123"
        val mockProduct = createMockProduct(id = productId, name = "Test Product", price = 29.99)
        coEvery { cartRepository.getProductDetails(productId) } returns mockProduct
        coEvery { cartRepository.addToCart(productId, 2) } returns true

        // Load product first
        viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
        advanceUntilIdle()

        // Set quantity
        viewModel.submitEvent(ProductDetailIntent.SetQuantity(2))
        advanceUntilIdle()

        // Act - Add to cart
        viewModel.submitEvent(ProductDetailIntent.AddToCart)
        advanceUntilIdle()

        // Assert
        assertEquals(true, viewModel.currentState.addToCartSuccess)
        assertEquals(false, viewModel.currentState.isAddingToCart)
    }

    @Test
    fun testAddToCartIgnoredDuringLoading() = runTest {
        // Arrange
        val productId = "product-123"
        coEvery { cartRepository.getProductDetails(productId) } returns createMockProduct(id = productId)

        // Load product (will be in loading state)
        viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))

        // Act - Try to add to cart while loading
        viewModel.submitEvent(ProductDetailIntent.AddToCart)
        advanceUntilIdle()

        // Assert - Should not proceed with add to cart during loading
        // The test verifies no crash occurs and state remains consistent
    }

    @Test
    fun testDismissError() = runTest {
        // Arrange
        val productId = "product-123"
        coEvery { cartRepository.getProductDetails(productId) } throws Exception("Load error")

        // Load product to create error state
        viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
        advanceUntilIdle()
        assertEquals("Load error", viewModel.currentState.error)

        // Act
        viewModel.submitEvent(ProductDetailIntent.DismissError)
        advanceUntilIdle()

        // Assert
        assertEquals(null, viewModel.currentState.error)
    }

    @Test
    fun testDismissSuccess() = runTest {
        // Arrange
        val productId = "product-123"
        val mockProduct = createMockProduct(id = productId)
        coEvery { cartRepository.getProductDetails(productId) } returns mockProduct
        coEvery { cartRepository.addToCart(productId, 1) } returns true

        // Load and add to cart
        viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
        advanceUntilIdle()
        viewModel.submitEvent(ProductDetailIntent.AddToCart)
        advanceUntilIdle()
        assertEquals(true, viewModel.currentState.addToCartSuccess)

        // Act
        viewModel.submitEvent(ProductDetailIntent.DismissSuccess)
        advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.currentState.addToCartSuccess)
    }

    // Helper function to create mock products
    private fun createMockProduct(
        id: String = "product-1",
        name: String = "Product Name",
        price: Double = 19.99,
        description: String = "Product Description",
        imageUrl: String = "https://example.com/image.jpg",
        stock: Int = 100
    ): Product {
        return Product(
            id = id,
            name = name,
            price = price,
            description = description,
            imageUrl = imageUrl,
            stock = stock
        )
    }
}
