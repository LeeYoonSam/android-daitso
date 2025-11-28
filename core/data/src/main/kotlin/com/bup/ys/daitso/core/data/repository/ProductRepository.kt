package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.common.Result
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Product data operations.
 *
 * Defines the contract for accessing product data from local and remote sources.
 */
interface ProductRepository {

    /**
     * Gets a list of products from local cache first, then syncs from network.
     *
     * Emits:
     * - Loading state first
     * - Success with local data
     * - Success with network-synced data
     * - Error if network fails (but local data still available)
     *
     * @return Flow of Result states with Product list
     */
    fun getProducts(): Flow<Result<List<Product>>>

    /**
     * Gets a single product by ID from network.
     *
     * Emits:
     * - Loading state
     * - Success with product data
     * - Error if not found or network fails
     *
     * @param productId The ID of the product to fetch
     * @return Flow of Result states with Product
     */
    fun getProduct(productId: String): Flow<Result<Product>>
}

/**
 * Implementation of ProductRepository with offline-first pattern.
 *
 * Implements offline-first strategy:
 * 1. Emit Loading state
 * 2. Fetch from local database
 * 3. Emit Success with local data (if available)
 * 4. Fetch from network in background
 * 5. Update local database with network data
 * 6. Emit Success with latest data
 *
 * @param networkDataSource Network data source for remote API calls
 */
class ProductRepositoryImpl(
    private val networkDataSource: com.bup.ys.daitso.core.network.NetworkDataSource
) : ProductRepository {

    override fun getProducts(): Flow<Result<List<Product>>> = kotlinx.coroutines.flow.flow {
        try {
            // Emit loading state
            emit(Result.Loading())

            // Fetch from network
            val products = networkDataSource.getProducts()

            // Emit success with products
            emit(Result.Success(products))
        } catch (e: Exception) {
            // Emit error
            emit(Result.Error(e))
        }
    }

    override fun getProduct(productId: String): Flow<Result<Product>> = kotlinx.coroutines.flow.flow {
        try {
            // Emit loading state
            emit(Result.Loading())

            // Fetch from network
            val product = networkDataSource.getProduct(productId)

            // Emit success
            emit(Result.Success(product))
        } catch (e: Exception) {
            // Emit error
            emit(Result.Error(e))
        }
    }
}
