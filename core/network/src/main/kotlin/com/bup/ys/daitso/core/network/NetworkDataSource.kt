package com.bup.ys.daitso.core.network

import com.bup.ys.daitso.core.model.Product

/**
 * Interface for remote data source operations.
 *
 * Defines contract for network API calls in the Daitso application.
 */
interface NetworkDataSource {

    /**
     * Fetches a list of all products from the remote server.
     *
     * @return List of products
     * @throws Exception if the network call fails
     */
    suspend fun getProducts(): List<Product>

    /**
     * Fetches details of a specific product from the remote server.
     *
     * @param productId The ID of the product to fetch
     * @return Product details
     * @throws Exception if the network call fails
     */
    suspend fun getProduct(productId: String): Product
}

/**
 * Implementation of NetworkDataSource using Retrofit API client.
 *
 * @param apiService The Retrofit API service instance
 */
class NetworkDataSourceImpl(
    private val apiService: DaitsoApiService
) : NetworkDataSource {

    override suspend fun getProducts(): List<Product> {
        val response = apiService.getProducts().execute()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch products: ${response.code()}")
        }
    }

    override suspend fun getProduct(productId: String): Product {
        val response = apiService.getProduct(productId).execute()
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Product not found")
        } else {
            throw Exception("Failed to fetch product: ${response.code()}")
        }
    }
}
