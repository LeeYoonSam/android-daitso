package com.bup.ys.daitso.core.data.datasource

import com.bup.ys.daitso.core.database.DaitsoDatabase
import com.bup.ys.daitso.core.database.dao.ProductDao
import com.bup.ys.daitso.core.database.entity.ProductEntity
import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocalDataSourceImplTest {
    private lateinit var database: DaitsoDatabase
    private lateinit var productDao: ProductDao
    private lateinit var localDataSource: LocalDataSourceImpl

    @Before
    fun setup() {
        database = mockk()
        productDao = mockk()
        every { database.productDao() } returns productDao
        localDataSource = LocalDataSourceImpl(database)
    }

    @Test
    fun testGetProductsReturnsProductsFromDatabase() = runTest {
        // Arrange
        val productEntities = listOf(
            ProductEntity(
                id = "product-1",
                name = "Product 1",
                description = "Description 1",
                price = 10000.0,
                imageUrl = "url1",
                category = "Category 1",
                stock = 5
            ),
            ProductEntity(
                id = "product-2",
                name = "Product 2",
                description = "Description 2",
                price = 20000.0,
                imageUrl = "url2",
                category = "Category 2",
                stock = 10
            )
        )
        every { productDao.getProducts() } returns flowOf(productEntities)

        // Act
        val result = localDataSource.getProducts()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Product 1", result[0].name)
        assertEquals("Product 2", result[1].name)
    }

    @Test
    fun testGetProductsReturnsEmptyListWhenDatabaseEmpty() = runTest {
        // Arrange
        every { productDao.getProducts() } returns flowOf(emptyList())

        // Act
        val result = localDataSource.getProducts()

        // Assert
        assertEquals(0, result.size)
    }

    @Test
    fun testGetProductByIdReturnsProductWhenFound() = runTest {
        // Arrange
        val productEntity = ProductEntity(
            id = "product-1",
            name = "Test Product",
            description = "Test",
            price = 10000.0,
            imageUrl = "url",
            category = "Category",
            stock = 5
        )
        coEvery { productDao.getProductById("product-1") } returns productEntity

        // Act
        val result = localDataSource.getProduct("product-1")

        // Assert
        assertNotNull(result)
        assertEquals("Test Product", result.name)
        coVerify { productDao.getProductById("product-1") }
    }

    @Test
    fun testGetProductByIdReturnsNullWhenNotFound() = runTest {
        // Arrange
        coEvery { productDao.getProductById("non-existent") } returns null

        // Act
        val result = localDataSource.getProduct("non-existent")

        // Assert
        assertNull(result)
        coVerify { productDao.getProductById("non-existent") }
    }

    @Test
    fun testSaveProductsCallsInsertProducts() = runTest {
        // Arrange
        val products = listOf(
            Product(
                id = "p1",
                name = "Product 1",
                description = "Desc 1",
                price = 1000.0,
                imageUrl = "url1",
                category = "cat1",
                stock = 1
            ),
            Product(
                id = "p2",
                name = "Product 2",
                description = "Desc 2",
                price = 2000.0,
                imageUrl = "url2",
                category = "cat2",
                stock = 2
            )
        )
        coEvery { productDao.insertProducts(any()) } returns Unit

        // Act
        localDataSource.saveProducts(products)

        // Assert
        coVerify { productDao.insertProducts(any()) }
    }

    @Test
    fun testSaveProductCallsInsertProduct() = runTest {
        // Arrange
        val product = Product(
            id = "p1",
            name = "Product 1",
            description = "Desc",
            price = 1000.0,
            imageUrl = "url",
            category = "cat",
            stock = 1
        )
        coEvery { productDao.insertProduct(any()) } returns Unit

        // Act
        localDataSource.saveProduct(product)

        // Assert
        coVerify { productDao.insertProduct(any()) }
    }

    // Note: Error handling tests are skipped because they require Android context (android.util.Log)
    // These tests should be moved to androidTest or use Robolectric for proper testing
}
