package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.common.Dispatcher
import com.bup.ys.daitso.core.common.DaitsoDispatchers
import com.bup.ys.daitso.core.common.Result
import com.bup.ys.daitso.core.data.datasource.LocalDataSource
import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.core.network.NetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Repository interface for Product data operations.
 *
 * Defines the contract for accessing product data from local and remote sources.
 */
interface ProductRepository {

    /**
     * Gets a list of products from local cache first, then syncs from network.
     *
     * Implements offline-first pattern:
     * - Emits Loading state first
     * - Emits Success with local data (if available)
     * - Fetches from network and emits Success with latest data
     * - Emits Error if network fails (but local data still available)
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
 * @param localDataSource Local data source for caching
 * @param ioDispatcher IO dispatcher for network and database operations
 */
class ProductRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    @Dispatcher(DaitsoDispatchers.IO)
    private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override fun getProducts(): Flow<Result<List<Product>>> = flow {
        try {
            // Emit loading state
            emit(Result.Loading())

            // Try to get from local cache first
            val localProducts = localDataSource.getProducts()
            if (localProducts.isNotEmpty()) {
                emit(Result.Success(localProducts))
            }

            // Fetch from network
            try {
                val networkProducts = networkDataSource.getProducts()
                // Update local cache
                localDataSource.saveProducts(networkProducts)
                // Emit latest data from network
                emit(Result.Success(networkProducts))
            } catch (networkError: Exception) {
                // If local data exists, don't emit error
                if (localProducts.isEmpty()) {
                    emit(Result.Error(networkError))
                }
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)

    override fun getProduct(productId: String): Flow<Result<Product>> = flow {
        try {
            // Emit loading state
            emit(Result.Loading())

            // Try to get from local cache first
            val localProduct = localDataSource.getProduct(productId)
            if (localProduct != null) {
                emit(Result.Success(localProduct))
            }

            // Fetch from network
            try {
                val networkProduct = networkDataSource.getProduct(productId)
                // Update local cache
                localDataSource.saveProduct(networkProduct)
                // Emit latest data from network
                emit(Result.Success(networkProduct))
            } catch (networkError: Exception) {
                // If local data exists, don't emit error
                if (localProduct == null) {
                    emit(Result.Error(networkError))
                }
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)
}
