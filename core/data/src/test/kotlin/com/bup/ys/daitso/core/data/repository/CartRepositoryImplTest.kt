package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for CartRepositoryImpl implementation.
 * Tests all cart operations with mocked database access.
 */
class CartRepositoryImplTest {

    @MockK
    private lateinit var cartDao: CartDao

    private lateinit var cartRepository: CartRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        cartRepository = CartRepositoryImpl(cartDao)
    }

    @Test
    fun testGetCartItemsReturnsFlowOfCartItems() = runTest {
        // Arrange
        val productId = "prod-1"
        val cartItemEntity = CartItemEntity(
            id = "cart-1",
            productId = productId,
            productName = "Test Product",
            quantity = 2,
            price = 29.99,
            imageUrl = "https://example.com/image.jpg"
        )
        coEvery { cartDao.getAllCartItems() } returns flowOf(listOf(cartItemEntity))

        // Act
        var result: List<CartItem>? = null
        cartRepository.getCartItems().collect { items ->
            result = items
        }

        // Assert
        assertEquals(1, result?.size)
        assertEquals("Test Product", result?.first()?.productName)
        assertEquals(2, result?.first()?.quantity)
    }

    @Test
    fun testUpdateQuantityClampsBetweenBounds() = runTest {
        // Arrange
        val productId = "prod-1"
        val cartItemEntity = CartItemEntity(
            id = "cart-1",
            productId = productId,
            productName = "Test Product",
            quantity = 2,
            price = 29.99,
            imageUrl = "https://example.com/image.jpg"
        )
        coEvery { cartDao.getCartItemsByProductId(productId) } returns flowOf(listOf(cartItemEntity))
        val updateSlot = slot<CartItemEntity>()
        coEvery { cartDao.updateCartItem(capture(updateSlot)) } returns Unit

        // Act - Update with quantity that needs clamping
        cartRepository.updateQuantity(productId, 2000)

        // Assert
        coVerify { cartDao.updateCartItem(any()) }
        assertEquals(999, updateSlot.captured.quantity)
    }

    @Test
    fun testRemoveItemCallsDeleteByProductId() = runTest {
        // Arrange
        val productId = "prod-1"
        coEvery { cartDao.deleteByProductId(productId) } returns Unit

        // Act
        cartRepository.removeItem(productId)

        // Assert
        coVerify { cartDao.deleteByProductId(productId) }
    }

    @Test
    fun testClearCartCallsCartDaoClearCart() = runTest {
        // Arrange
        coEvery { cartDao.clearCart() } returns Unit

        // Act
        cartRepository.clearCart()

        // Assert
        coVerify { cartDao.clearCart() }
    }

    @Test
    fun testAddToCartWithValidProduct() = runTest {
        // Arrange
        val product = Product(
            id = "prod-1",
            name = "Test Product",
            description = "A test product",
            price = 29.99,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics",
            stock = 100
        )
        val quantity = 2
        coEvery { cartDao.insertCartItem(any()) } returns Unit

        // Act
        val result = cartRepository.addToCart(product, quantity)

        // Assert
        assertTrue(result)
        coVerify { cartDao.insertCartItem(any()) }
    }

    @Test
    fun testAddToCartWithInvalidQuantityBelowMin() = runTest {
        // Arrange
        val product = Product(
            id = "prod-1",
            name = "Test Product",
            description = "A test product",
            price = 29.99,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics",
            stock = 100
        )
        val invalidQuantity = 0

        // Act
        val result = cartRepository.addToCart(product, invalidQuantity)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testAddToCartWithInvalidQuantityAboveMax() = runTest {
        // Arrange
        val product = Product(
            id = "prod-1",
            name = "Test Product",
            description = "A test product",
            price = 29.99,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics",
            stock = 100
        )
        val invalidQuantity = 1000

        // Act
        val result = cartRepository.addToCart(product, invalidQuantity)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testGetProductDetailsReturnsProduct() = runTest {
        // Arrange
        val productId = "product-001"

        // Act
        val result = cartRepository.getProductDetails(productId)

        // Assert
        assertEquals("product-001", result.id)
        assertEquals("Wireless Headphones", result.name)
    }

    @Test
    fun testGetProductDetailsThrowsForUnknownProduct() = runTest {
        // Arrange
        val productId = "unknown-product"

        // Act & Assert
        try {
            cartRepository.getProductDetails(productId)
            throw AssertionError("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Product not found: $productId", e.message)
        }
    }
}
