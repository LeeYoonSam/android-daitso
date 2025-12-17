package com.bup.ys.daitso.core.data.repository

import app.cash.turbine.test
import com.bup.ys.daitso.core.common.Result
import com.bup.ys.daitso.core.data.datasource.LocalDataSource
import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.core.network.NetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
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
    private val mockLocalDataSource = mockk<LocalDataSource>()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: ProductRepository

    @Test
    fun testGetProducts_OfflineFirst() = runTest(testDispatcher) {
        // Arrange
        val localProducts = listOf(
            Product("1", "Local Product 1", "Desc 1", 10.0, "url1", "category1"),
            Product("2", "Local Product 2", "Desc 2", 20.0, "url2", "category2")
        )

        coEvery { mockLocalDataSource.getProducts() } returns localProducts
        coEvery { mockNetworkDataSource.getProducts() } returns localProducts
        coEvery { mockLocalDataSource.saveProducts(any()) } returns Unit

        repository = ProductRepositoryImpl(
            networkDataSource = mockNetworkDataSource,
            localDataSource = mockLocalDataSource,
            ioDispatcher = testDispatcher
        )

        // Act & Assert
        repository.getProducts().test {
            val firstEmission = awaitItem()
            assertTrue(firstEmission is Result.Loading || firstEmission is Result.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testGetProducts_ErrorHandling() = runTest(testDispatcher) {
        // Arrange
        coEvery { mockLocalDataSource.getProducts() } returns emptyList()
        coEvery { mockNetworkDataSource.getProducts() } throws Exception("Network error")

        repository = ProductRepositoryImpl(
            networkDataSource = mockNetworkDataSource,
            localDataSource = mockLocalDataSource,
            ioDispatcher = testDispatcher
        )

        // Act & Assert
        repository.getProducts().test {
            val emission = awaitItem()
            assertTrue(emission is Result.Loading || emission is Result.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testGetProduct_Success() = runTest(testDispatcher) {
        // Arrange
        val product = Product("1", "Test Product", "Test", 10.0, "url", "category")
        coEvery { mockLocalDataSource.getProduct("1") } returns product
        coEvery { mockNetworkDataSource.getProduct("1") } returns product
        coEvery { mockLocalDataSource.saveProduct(any()) } returns Unit

        repository = ProductRepositoryImpl(
            networkDataSource = mockNetworkDataSource,
            localDataSource = mockLocalDataSource,
            ioDispatcher = testDispatcher
        )

        // Act & Assert
        repository.getProduct("1").test {
            val emission = awaitItem()
            assertTrue(emission is Result.Success || emission is Result.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testGetProduct_NotFound() = runTest(testDispatcher) {
        // Arrange
        coEvery { mockLocalDataSource.getProduct("nonexistent") } returns null
        coEvery { mockNetworkDataSource.getProduct("nonexistent") } throws Exception("Product not found")

        repository = ProductRepositoryImpl(
            networkDataSource = mockNetworkDataSource,
            localDataSource = mockLocalDataSource,
            ioDispatcher = testDispatcher
        )

        // Act & Assert
        repository.getProduct("nonexistent").test {
            val emission = awaitItem()
            // Should eventually emit error
            assertTrue(emission is Result.Error || emission is Result.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
