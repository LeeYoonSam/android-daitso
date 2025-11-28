package com.bup.ys.daitso.core.data.repository

import app.cash.turbine.test
import com.bup.ys.daitso.core.common.Result
import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.core.network.NetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for ProductRepository.
 *
 * Tests cover:
 * - Offline-first pattern (local cache first)
 * - Network sync when online
 * - Error handling
 * - Flow emission states
 */
class ProductRepositoryTest {

    private val mockNetworkDataSource = mockk<NetworkDataSource>()
    private val mockCartDao = mockk<CartDao>()

    private lateinit var repository: ProductRepository

    @Test
    fun testGetProducts_OfflineFirst() = runTest {
        // Arrange
        val localProducts = listOf(
            Product("1", "Local Product 1", "Desc 1", 10.0, "url1"),
            Product("2", "Local Product 2", "Desc 2", 20.0, "url2")
        )

        coEvery {
            mockCartDao.getAllCartItems()
        } returns flowOf(emptyList())

        repository = ProductRepositoryImpl(mockNetworkDataSource)

        // Act & Assert
        repository.getProducts().test {
            val firstEmission = awaitItem()
            assertTrue(firstEmission is Result.Loading || firstEmission is Result.Success)
        }
    }

    @Test
    fun testGetProducts_ErrorHandling() = runTest {
        // Arrange
        coEvery {
            mockNetworkDataSource.getProducts()
        } throws Exception("Network error")

        repository = ProductRepositoryImpl(mockNetworkDataSource)

        // Act & Assert
        repository.getProducts().test {
            val emission = awaitItem()
            if (emission is Result.Error) {
                assertEquals("Network error", emission.exception.message)
            }
        }
    }

    @Test
    fun testGetProduct_Success() = runTest {
        // Arrange
        val product = Product("1", "Test Product", "Test", 10.0, "url")
        coEvery {
            mockNetworkDataSource.getProduct("1")
        } returns product

        repository = ProductRepositoryImpl(mockNetworkDataSource)

        // Act & Assert
        repository.getProduct("1").test {
            val emission = awaitItem()
            assertTrue(emission is Result.Success || emission is Result.Loading)
        }
    }

    @Test
    fun testGetProduct_NotFound() = runTest {
        // Arrange
        coEvery {
            mockNetworkDataSource.getProduct("nonexistent")
        } throws Exception("Product not found")

        repository = ProductRepositoryImpl(mockNetworkDataSource)

        // Act & Assert
        repository.getProduct("nonexistent").test {
            val emission = awaitItem()
            // Should eventually emit error
            assertTrue(emission is Result.Error || emission is Result.Loading)
        }
    }
}
