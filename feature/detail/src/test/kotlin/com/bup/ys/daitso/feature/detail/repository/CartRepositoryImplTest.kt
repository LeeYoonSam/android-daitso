package com.bup.ys.daitso.feature.detail.repository

import com.bup.ys.daitso.core.model.Product
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for CartRepositoryImpl.
 * Tests in-memory cart operations and product retrieval.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CartRepositoryImplTest {

    private lateinit var repository: CartRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = CartRepositoryImpl()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGetProductDetailsSuccess() = runTest {
        // Arrange
        val productId = "product-001"

        // Act
        val product = repository.getProductDetails(productId)

        // Assert
        assertEquals(productId, product.id)
        assertEquals("Wireless Headphones", product.name)
        assertTrue(product.stock > 0)
    }

    @Test
    fun testGetProductDetailsForDifferentIds() = runTest {
        // Arrange
        val productId1 = "product-001"
        val productId2 = "product-002"

        // Act
        val product1 = repository.getProductDetails(productId1)
        val product2 = repository.getProductDetails(productId2)

        // Assert
        assertEquals(productId1, product1.id)
        assertEquals(productId2, product2.id)
        assertEquals("Wireless Headphones", product1.name)
        assertEquals("USB-C Cable", product2.name)
    }

    @Test
    fun testAddToCartSuccess() = runTest {
        // Arrange
        val productId = "product-001"
        val quantity = 5

        // Act
        val result = repository.addToCart(productId, quantity)

        // Assert
        assertTrue(result)
    }

    @Test
    fun testAddToCartWithDifferentQuantities() = runTest {
        // Arrange
        val productId = "product-002"
        val quantity1 = 1
        val quantity2 = 999

        // Act
        val result1 = repository.addToCart(productId, quantity1)
        val result2 = repository.addToCart(productId, quantity2)

        // Assert
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun testAddToCartWithMultipleProducts() = runTest {
        // Arrange
        val productId1 = "product-001"
        val productId2 = "product-002"
        val quantity = 3

        // Act
        val result1 = repository.addToCart(productId1, quantity)
        val result2 = repository.addToCart(productId2, quantity)

        // Assert
        assertTrue(result1)
        assertTrue(result2)
    }

    @Test
    fun testAddToCartFailsWithInvalidQuantity() = runTest {
        // Arrange
        val productId = "product-001"
        val invalidQuantity = 0

        // Act
        val result = repository.addToCart(productId, invalidQuantity)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testAddToCartFailsWithNegativeQuantity() = runTest {
        // Arrange
        val productId = "product-001"
        val invalidQuantity = -5

        // Act
        val result = repository.addToCart(productId, invalidQuantity)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testProductHasStock() = runTest {
        // Arrange
        val productId = "product-001"

        // Act
        val product = repository.getProductDetails(productId)

        // Assert
        assertTrue(product.stock > 0)
    }

    @Test
    fun testProductHasValidFields() = runTest {
        // Arrange
        val productId = "product-001"

        // Act
        val product = repository.getProductDetails(productId)

        // Assert
        assertEquals(productId, product.id)
        assertTrue(product.name.isNotEmpty())
        assertTrue(product.description.isNotEmpty())
        assertTrue(product.price > 0)
        assertTrue(product.imageUrl.isNotEmpty())
        assertTrue(product.category.isNotEmpty())
        assertTrue(product.stock >= 0)
    }
}
