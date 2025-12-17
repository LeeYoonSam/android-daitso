package com.bup.ys.daitso.core.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bup.ys.daitso.core.database.DaitsoDatabase
import com.bup.ys.daitso.core.database.entity.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductDaoTest {
    private lateinit var database: DaitsoDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            DaitsoDatabase::class.java
        ).allowMainThreadQueries().build()
        productDao = database.productDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertProduct() = runTest {
        // Arrange
        val product = ProductEntity(
            id = "product-1",
            name = "Test Product",
            description = "A test product",
            price = 10000.0,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics",
            stock = 5
        )

        // Act
        productDao.insertProduct(product)

        // Assert
        val retrievedProduct = productDao.getProductById("product-1")
        assertNotNull(retrievedProduct)
        assertEquals("product-1", retrievedProduct.id)
        assertEquals("Test Product", retrievedProduct.name)
    }

    @Test
    fun testInsertMultipleProducts() = runTest {
        // Arrange
        val products = listOf(
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

        // Act
        productDao.insertProducts(products)

        // Assert
        val allProducts = productDao.getProducts().first()
        assertEquals(2, allProducts.size)
    }

    @Test
    fun testGetProductById() = runTest {
        // Arrange
        val product = ProductEntity(
            id = "product-test",
            name = "Test",
            description = "Test",
            price = 5000.0,
            imageUrl = "url",
            category = "Category",
            stock = 1
        )
        productDao.insertProduct(product)

        // Act
        val retrieved = productDao.getProductById("product-test")

        // Assert
        assertNotNull(retrieved)
        assertEquals("Test", retrieved.name)
    }

    @Test
    fun testGetProductByIdNotFound() = runTest {
        // Act
        val retrieved = productDao.getProductById("non-existent")

        // Assert
        assertNull(retrieved)
    }

    @Test
    fun testGetAllProducts() = runTest {
        // Arrange
        val products = listOf(
            ProductEntity("p1", "Product 1", "Desc 1", 1000.0, "url1", "cat1", 1),
            ProductEntity("p2", "Product 2", "Desc 2", 2000.0, "url2", "cat2", 2),
            ProductEntity("p3", "Product 3", "Desc 3", 3000.0, "url3", "cat3", 3)
        )
        productDao.insertProducts(products)

        // Act
        val allProducts = productDao.getProducts().first()

        // Assert
        assertEquals(3, allProducts.size)
    }

    @Test
    fun testDeleteAllProducts() = runTest {
        // Arrange
        val products = listOf(
            ProductEntity("p1", "Product 1", "Desc 1", 1000.0, "url1", "cat1", 1),
            ProductEntity("p2", "Product 2", "Desc 2", 2000.0, "url2", "cat2", 2)
        )
        productDao.insertProducts(products)

        // Act
        productDao.deleteAllProducts()

        // Assert
        val allProducts = productDao.getProducts().first()
        assertEquals(0, allProducts.size)
    }

    @Test
    fun testInsertProductWithConflictStrategy() = runTest {
        // Arrange
        val product = ProductEntity("p1", "Original", "Desc", 1000.0, "url", "cat", 1)
        productDao.insertProduct(product)

        val updatedProduct = ProductEntity("p1", "Updated", "Desc", 2000.0, "url", "cat", 2)

        // Act
        productDao.insertProduct(updatedProduct)

        // Assert
        val retrieved = productDao.getProductById("p1")
        assertNotNull(retrieved)
        assertEquals("Updated", retrieved.name)
        assertEquals(2000.0, retrieved.price)
    }
}
