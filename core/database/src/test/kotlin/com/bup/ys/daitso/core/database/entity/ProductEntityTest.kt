package com.bup.ys.daitso.core.database.entity

import com.bup.ys.daitso.core.model.Product
import org.junit.Test
import kotlin.test.assertEquals

class ProductEntityTest {

    @Test
    fun testProductEntityToProduct() {
        // Arrange
        val productEntity = ProductEntity(
            id = "product-1",
            name = "Test Product",
            description = "A test product",
            price = 10000.0,
            imageUrl = "https://example.com/image.jpg",
            category = "Electronics",
            stock = 5
        )

        // Act
        val product = productEntity.toDomainModel()

        // Assert
        assertEquals("product-1", product.id)
        assertEquals("Test Product", product.name)
        assertEquals("A test product", product.description)
        assertEquals(10000.0, product.price)
        assertEquals("https://example.com/image.jpg", product.imageUrl)
        assertEquals("Electronics", product.category)
        assertEquals(5, product.stock)
    }

    @Test
    fun testProductToEntity() {
        // Arrange
        val product = Product(
            id = "product-2",
            name = "Another Product",
            description = "Another test product",
            price = 20000.0,
            imageUrl = "https://example.com/image2.jpg",
            category = "Books",
            stock = 10
        )

        // Act
        val productEntity = product.toEntity()

        // Assert
        assertEquals("product-2", productEntity.id)
        assertEquals("Another Product", productEntity.name)
        assertEquals("Another test product", productEntity.description)
        assertEquals(20000.0, productEntity.price)
        assertEquals("https://example.com/image2.jpg", productEntity.imageUrl)
        assertEquals("Books", productEntity.category)
        assertEquals(10, productEntity.stock)
    }

    @Test
    fun testProductEntityToProductAndBack() {
        // Arrange
        val originalProduct = Product(
            id = "product-3",
            name = "Round Trip Product",
            description = "Test round trip",
            price = 15000.0,
            imageUrl = "https://example.com/image3.jpg",
            category = "Clothing",
            stock = 20
        )

        // Act
        val entity = originalProduct.toEntity()
        val resultProduct = entity.toDomainModel()

        // Assert
        assertEquals(originalProduct.id, resultProduct.id)
        assertEquals(originalProduct.name, resultProduct.name)
        assertEquals(originalProduct.description, resultProduct.description)
        assertEquals(originalProduct.price, resultProduct.price)
        assertEquals(originalProduct.imageUrl, resultProduct.imageUrl)
        assertEquals(originalProduct.category, resultProduct.category)
        assertEquals(originalProduct.stock, resultProduct.stock)
    }

    @Test
    fun testProductEntityWithMinimalData() {
        // Arrange
        val productEntity = ProductEntity(
            id = "min-1",
            name = "Minimal",
            description = "",
            price = 0.0,
            imageUrl = "",
            category = "",
            stock = 0
        )

        // Act
        val product = productEntity.toDomainModel()

        // Assert
        assertEquals("min-1", product.id)
        assertEquals("Minimal", product.name)
        assertEquals("", product.description)
        assertEquals(0.0, product.price)
        assertEquals("", product.imageUrl)
        assertEquals("", product.category)
        assertEquals(0, product.stock)
    }
}
