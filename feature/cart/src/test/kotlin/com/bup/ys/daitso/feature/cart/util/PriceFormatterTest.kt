package com.bup.ys.daitso.feature.cart.util

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for PriceFormatter utility.
 * Tests Korean Won currency formatting.
 */
class PriceFormatterTest {

    @Test
    fun testFormatSmallPrice() {
        // Arrange
        val price = 100.0

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩100", formatted)
    }

    @Test
    fun testFormatPriceWithThousands() {
        // Arrange
        val price = 1000.0

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩1,000", formatted)
    }

    @Test
    fun testFormatPriceWithMultipleThousands() {
        // Arrange
        val price = 15000.0

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩15,000", formatted)
    }

    @Test
    fun testFormatLargePrice() {
        // Arrange
        val price = 999999.0

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩999,999", formatted)
    }

    @Test
    fun testFormatPriceWithDecimals() {
        // Arrange
        val price = 99.99

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩99", formatted)
    }

    @Test
    fun testFormatZeroPrice() {
        // Arrange
        val price = 0.0

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩0", formatted)
    }

    @Test
    fun testFormatNegativePrice() {
        // Arrange
        val price = -100.0

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩-100", formatted)
    }

    @Test
    fun testFormatVeryLargePrice() {
        // Arrange
        val price = 123456789.0

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩123,456,789", formatted)
    }

    @Test
    fun testFormatIntegerToPrice() {
        // Arrange
        val price = 12345

        // Act
        val formatted = PriceFormatter.formatPrice(price.toDouble())

        // Assert
        assertEquals("₩12,345", formatted)
    }

    @Test
    fun testFormatPriceWithSmallDecimal() {
        // Arrange
        val price = 1234.5

        // Act
        val formatted = PriceFormatter.formatPrice(price)

        // Assert
        assertEquals("₩1,234", formatted)
    }
}
