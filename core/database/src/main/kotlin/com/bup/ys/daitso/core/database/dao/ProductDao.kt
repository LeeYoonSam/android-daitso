package com.bup.ys.daitso.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bup.ys.daitso.core.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Product entities.
 *
 * Provides database operations for products including insert, query, and delete.
 */
@Dao
interface ProductDao {

    /**
     * Retrieves all products from the database as a Flow.
     *
     * @return Flow of all products
     */
    @Query("SELECT * FROM products")
    fun getProducts(): Flow<List<ProductEntity>>

    /**
     * Retrieves a single product by ID.
     *
     * @param id The ID of the product
     * @return The product or null if not found
     */
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    /**
     * Inserts a single product into the database.
     *
     * If a product with the same ID already exists, it will be replaced.
     *
     * @param product The product to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    /**
     * Inserts multiple products into the database.
     *
     * If products with the same IDs already exist, they will be replaced.
     *
     * @param products The products to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    /**
     * Deletes all products from the database.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
