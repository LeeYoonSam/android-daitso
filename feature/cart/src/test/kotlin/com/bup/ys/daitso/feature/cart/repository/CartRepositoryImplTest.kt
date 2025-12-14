package com.bup.ys.daitso.feature.cart.repository

import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.feature.cart.contract.CartItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for CartRepositoryImpl.
 * Tests cart operations with database integration using mocked CartDao.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CartRepositoryImplTest {

    private lateinit var mockCartDao: CartDao
    private lateinit var cartRepository: CartRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockCartDao = mockk()
        cartRepository = CartRepositoryImpl(mockCartDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // RED: getCartItems() tests
    @Test
    fun testGetCartItemsReturnsEmptyListInitially() = runTest {
        // Arrange
        every { mockCartDao.getAllCartItems() } returns flowOf(emptyList())

        // Act
        val items = mutableListOf<CartItem>()
        cartRepository.getCartItems().collect { cartItems ->
            items.addAll(cartItems)
        }

        // Assert
        assertTrue(items.isEmpty())
    }

    @Test
    fun testGetCartItemsReturnsSingleItem() = runTest {
        // Arrange
        val entity = CartItemEntity(
            id = "item-001",
            productId = "product-001",
            productName = "Wireless Headphones",
            quantity = 2,
            price = 99.99,
            imageUrl = "https://example.com/headphones.jpg"
        )
        every { mockCartDao.getAllCartItems() } returns flowOf(listOf(entity))

        // Act
        val items = mutableListOf<CartItem>()
        cartRepository.getCartItems().collect { cartItems ->
            items.addAll(cartItems)
        }

        // Assert
        assertEquals(1, items.size)
        assertEquals("product-001", items[0].productId)
        assertEquals("Wireless Headphones", items[0].name)
        assertEquals(2, items[0].quantity)
    }

    @Test
    fun testGetCartItemsReturnsMultipleItems() = runTest {
        // Arrange
        val entities = listOf(
            CartItemEntity(
                id = "item-001",
                productId = "product-001",
                productName = "Headphones",
                quantity = 1,
                price = 99.99,
                imageUrl = "url1"
            ),
            CartItemEntity(
                id = "item-002",
                productId = "product-002",
                productName = "Cable",
                quantity = 3,
                price = 12.99,
                imageUrl = "url2"
            )
        )
        every { mockCartDao.getAllCartItems() } returns flowOf(entities)

        // Act
        val items = mutableListOf<CartItem>()
        cartRepository.getCartItems().collect { cartItems ->
            items.addAll(cartItems)
        }

        // Assert
        assertEquals(2, items.size)
        assertEquals("product-001", items[0].productId)
        assertEquals("product-002", items[1].productId)
    }

    // RED: updateQuantity() tests
    @Test
    fun testUpdateQuantityCallsDao() = runTest {
        // Arrange
        val productId = "product-001"
        val newQuantity = 5
        coEvery { mockCartDao.updateCartItem(any()) } returns Unit
        every { mockCartDao.getCartItemsByProductId(productId) } returns flowOf(
            listOf(
                CartItemEntity(
                    id = "item-001",
                    productId = productId,
                    productName = "Headphones",
                    quantity = 2,
                    price = 99.99,
                    imageUrl = "url"
                )
            )
        )

        // Act
        cartRepository.updateQuantity(productId, newQuantity)

        // Assert
        coVerify { mockCartDao.updateCartItem(any()) }
    }

    @Test
    fun testUpdateQuantityWithValidQuantity() = runTest {
        // Arrange
        val productId = "product-001"
        val newQuantity = 10
        val originalEntity = CartItemEntity(
            id = "item-001",
            productId = productId,
            productName = "Headphones",
            quantity = 2,
            price = 99.99,
            imageUrl = "url"
        )
        every { mockCartDao.getCartItemsByProductId(productId) } returns flowOf(listOf(originalEntity))
        coEvery { mockCartDao.updateCartItem(any()) } returns Unit

        // Act
        cartRepository.updateQuantity(productId, newQuantity)

        // Assert
        coVerify { mockCartDao.updateCartItem(match { it.quantity == newQuantity }) }
    }

    @Test
    fun testUpdateQuantityClampsMinQuantity() = runTest {
        // Arrange
        val productId = "product-001"
        val originalEntity = CartItemEntity(
            id = "item-001",
            productId = productId,
            productName = "Headphones",
            quantity = 2,
            price = 99.99,
            imageUrl = "url"
        )
        every { mockCartDao.getCartItemsByProductId(productId) } returns flowOf(listOf(originalEntity))
        coEvery { mockCartDao.updateCartItem(any()) } returns Unit

        // Act
        cartRepository.updateQuantity(productId, 0)

        // Assert
        coVerify { mockCartDao.updateCartItem(match { it.quantity == 1 }) }
    }

    @Test
    fun testUpdateQuantityClampMaxQuantity() = runTest {
        // Arrange
        val productId = "product-001"
        val originalEntity = CartItemEntity(
            id = "item-001",
            productId = productId,
            productName = "Headphones",
            quantity = 2,
            price = 99.99,
            imageUrl = "url"
        )
        every { mockCartDao.getCartItemsByProductId(productId) } returns flowOf(listOf(originalEntity))
        coEvery { mockCartDao.updateCartItem(any()) } returns Unit

        // Act
        cartRepository.updateQuantity(productId, 1000)

        // Assert
        coVerify { mockCartDao.updateCartItem(match { it.quantity == 999 }) }
    }

    // RED: removeItem() tests
    @Test
    fun testRemoveItemCallsDao() = runTest {
        // Arrange
        val productId = "product-001"
        coEvery { mockCartDao.deleteByProductId(productId) } returns Unit

        // Act
        cartRepository.removeItem(productId)

        // Assert
        coVerify { mockCartDao.deleteByProductId(productId) }
    }

    // RED: clearCart() tests
    @Test
    fun testClearCartCallsDao() = runTest {
        // Arrange
        coEvery { mockCartDao.clearCart() } returns Unit

        // Act
        cartRepository.clearCart()

        // Assert
        coVerify { mockCartDao.clearCart() }
    }

    // RED: Integration tests
    @Test
    fun testCartOperationsSequence() = runTest {
        // Arrange
        val entity1 = CartItemEntity(
            id = "item-001",
            productId = "product-001",
            productName = "Headphones",
            quantity = 1,
            price = 99.99,
            imageUrl = "url"
        )
        val entity2 = CartItemEntity(
            id = "item-002",
            productId = "product-002",
            productName = "Cable",
            quantity = 2,
            price = 12.99,
            imageUrl = "url"
        )

        every { mockCartDao.getAllCartItems() } returns flowOf(listOf(entity1, entity2))
        every { mockCartDao.getCartItemsByProductId("product-001") } returns flowOf(listOf(entity1))
        coEvery { mockCartDao.updateCartItem(any()) } returns Unit
        coEvery { mockCartDao.deleteByProductId(any()) } returns Unit

        // Act & Assert - Load items
        val items = mutableListOf<CartItem>()
        cartRepository.getCartItems().collect { cartItems ->
            items.addAll(cartItems)
        }
        assertEquals(2, items.size)

        // Act - Update quantity
        cartRepository.updateQuantity("product-001", 5)

        // Act - Remove item
        cartRepository.removeItem("product-002")
        coVerify { mockCartDao.deleteByProductId("product-002") }
    }

    @Test
    fun testGetCartItemsWithZeroQuantity() = runTest {
        // Arrange
        val entity = CartItemEntity(
            id = "item-001",
            productId = "product-001",
            productName = "Headphones",
            quantity = 0,
            price = 99.99,
            imageUrl = "url"
        )
        every { mockCartDao.getAllCartItems() } returns flowOf(listOf(entity))

        // Act
        val items = mutableListOf<CartItem>()
        cartRepository.getCartItems().collect { cartItems ->
            items.addAll(cartItems)
        }

        // Assert
        assertEquals(1, items.size)
        assertEquals(0, items[0].quantity)
    }

    @Test
    fun testGetCartItemsWithLargeQuantity() = runTest {
        // Arrange
        val entity = CartItemEntity(
            id = "item-001",
            productId = "product-001",
            productName = "Headphones",
            quantity = 999,
            price = 99.99,
            imageUrl = "url"
        )
        every { mockCartDao.getAllCartItems() } returns flowOf(listOf(entity))

        // Act
        val items = mutableListOf<CartItem>()
        cartRepository.getCartItems().collect { cartItems ->
            items.addAll(cartItems)
        }

        // Assert
        assertEquals(1, items.size)
        assertEquals(999, items[0].quantity)
    }
}
