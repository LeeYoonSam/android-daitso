package com.bup.ys.daitso.core.common

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for Result sealed class.
 *
 * Tests cover:
 * - Result.Success state
 * - Result.Error state
 * - Result.Loading state
 * - Type safety and data preservation
 */
class ResultTest {

    @Test
    fun testResultSuccess() {
        // Arrange & Act
        val result: Result<String> = Result.Success("Test data")

        // Assert
        assertTrue(result is Result.Success)
        assertEquals("Test data", (result as Result.Success).data)
    }

    @Test
    fun testResultError() {
        // Arrange
        val exception = Exception("Test error")

        // Act
        val result: Result<String> = Result.Error(exception)

        // Assert
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }

    @Test
    fun testResultLoading() {
        // Arrange & Act
        val result: Result<String> = Result.Loading()

        // Assert
        assertTrue(result is Result.Loading)
    }

    @Test
    fun testResultSuccessEquality() {
        // Arrange
        val result1: Result<Int> = Result.Success(42)
        val result2: Result<Int> = Result.Success(42)

        // Assert
        assertEquals(result1, result2)
    }

    @Test
    fun testResultErrorEquality() {
        // Arrange
        val exception = Exception("Error")
        val result1: Result<String> = Result.Error(exception)
        val result2: Result<String> = Result.Error(exception)

        // Assert
        assertEquals(result1, result2)
    }

    @Test
    fun testResultLoadingEquality() {
        // Arrange
        val result1: Result<String> = Result.Loading()
        val result2: Result<String> = Result.Loading()

        // Assert
        assertEquals(result1, result2)
    }

    @Test
    fun testResultGetOrNull_Success() {
        // Arrange
        val result: Result<String> = Result.Success("Data")

        // Act
        val data = result.getOrNull()

        // Assert
        assertEquals("Data", data)
    }

    @Test
    fun testResultGetOrNull_Error() {
        // Arrange
        val result: Result<String> = Result.Error(Exception("Error"))

        // Act
        val data = result.getOrNull()

        // Assert
        assertEquals(null, data)
    }

    @Test
    fun testResultGetOrNull_Loading() {
        // Arrange
        val result: Result<String> = Result.Loading()

        // Act
        val data = result.getOrNull()

        // Assert
        assertEquals(null, data)
    }

    @Test
    fun testResultIsSuccess() {
        // Arrange
        val successResult: Result<String> = Result.Success("Data")
        val errorResult: Result<String> = Result.Error(Exception())
        val loadingResult: Result<String> = Result.Loading()

        // Assert
        assertTrue(successResult.isSuccess())
        assertFalse(errorResult.isSuccess())
        assertFalse(loadingResult.isSuccess())
    }

    @Test
    fun testResultIsError() {
        // Arrange
        val successResult: Result<String> = Result.Success("Data")
        val errorResult: Result<String> = Result.Error(Exception())
        val loadingResult: Result<String> = Result.Loading()

        // Assert
        assertFalse(successResult.isError())
        assertTrue(errorResult.isError())
        assertFalse(loadingResult.isError())
    }

    @Test
    fun testResultIsLoading() {
        // Arrange
        val successResult: Result<String> = Result.Success("Data")
        val errorResult: Result<String> = Result.Error(Exception())
        val loadingResult: Result<String> = Result.Loading()

        // Assert
        assertFalse(successResult.isLoading())
        assertFalse(errorResult.isLoading())
        assertTrue(loadingResult.isLoading())
    }
}
