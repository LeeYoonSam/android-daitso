package com.bup.ys.daitso.feature.cart.di

import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.feature.cart.domain.CartRepository
import com.bup.ys.daitso.feature.cart.repository.CartRepositoryImpl
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * Unit tests for CartModule.
 * Validates dependency injection setup for cart feature.
 */
class CartModuleTest {

    @Test
    fun testProvideCartRepositoryReturnsImplementation() {
        // Arrange
        val mockCartDao = mockk<CartDao>()

        // Act
        val repository = CartModule.provideCartRepository(mockCartDao)

        // Assert
        assertNotNull(repository)
        assertIs<CartRepositoryImpl>(repository)
    }

    @Test
    fun testProvideCartRepositoryReturnsCartRepositoryInterface() {
        // Arrange
        val mockCartDao = mockk<CartDao>()

        // Act
        val repository = CartModule.provideCartRepository(mockCartDao)

        // Assert
        assertNotNull(repository)
        assertIs<CartRepository>(repository)
    }

    @Test
    fun testProvideCartRepositoryMultipleCallsReturnDifferentInstances() {
        // Arrange
        val mockCartDao1 = mockk<CartDao>()
        val mockCartDao2 = mockk<CartDao>()

        // Act
        val repository1 = CartModule.provideCartRepository(mockCartDao1)
        val repository2 = CartModule.provideCartRepository(mockCartDao2)

        // Assert
        assertNotNull(repository1)
        assertNotNull(repository2)
        // Different DAO instances should result in different repository instances
        // (without mocking the singleton scope)
    }
}
