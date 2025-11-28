package com.bup.ys.daitso.core.network

import com.bup.ys.daitso.core.model.Product
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for NetworkDataSource and Retrofit setup.
 *
 * Tests cover:
 * - API client initialization
 * - Mock server communication
 * - JSON serialization/deserialization
 */
class NetworkDataSourceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var retrofit: Retrofit
    private lateinit var apiService: DaitsoApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()

        apiService = retrofit.create(DaitsoApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testFetchProducts_Success() {
        // Arrange
        val products = listOf(
            Product("1", "Product 1", "Desc 1", 10.0, "url1"),
            Product("2", "Product 2", "Desc 2", 20.0, "url2")
        )
        val responseBody = Json.encodeToString(products)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
        )

        // Act
        val response = apiService.getProducts().execute()

        // Assert
        assertTrue(response.isSuccessful)
        assertEquals(2, response.body()?.size)
        assertEquals("Product 1", response.body()?.get(0)?.name)
    }

    @Test
    fun testFetchProducts_ServerError() {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        // Act
        val response = apiService.getProducts().execute()

        // Assert
        assertEquals(500, response.code())
    }

    @Test
    fun testFetchProduct_NotFound() {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // Act
        val response = apiService.getProduct("nonexistent").execute()

        // Assert
        assertEquals(404, response.code())
    }

    private fun assertTrue(condition: Boolean) {
        assert(condition)
    }
}
