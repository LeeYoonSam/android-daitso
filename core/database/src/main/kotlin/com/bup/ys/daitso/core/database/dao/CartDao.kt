package com.bup.ys.daitso.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bup.ys.daitso.core.database.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CartItem entities.
 *
 * Provides database operations for cart items including insert, update, delete, and query.
 */
@Dao
interface CartDao {

    /**
     * Inserts a cart item into the database.
     *
     * @param cartItem The cart item to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    /**
     * Updates an existing cart item in the database.
     *
     * @param cartItem The cart item to update
     */
    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    /**
     * Deletes a cart item from the database.
     *
     * @param cartItem The cart item to delete
     */
    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)

    /**
     * Retrieves a single cart item by ID.
     *
     * @param id The ID of the cart item
     * @return The cart item or null if not found
     */
    @Query("SELECT * FROM cart_items WHERE id = :id")
    suspend fun getCartItemById(id: String): CartItemEntity?

    /**
     * Retrieves all cart items from the database as a Flow.
     *
     * @return Flow of all cart items
     */
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    /**
     * Deletes all cart items from the database.
     */
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    /**
     * Retrieves cart items by product ID.
     *
     * @param productId The product ID to search for
     * @return Flow of cart items with the specified product ID
     */
    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    fun getCartItemsByProductId(productId: String): Flow<List<CartItemEntity>>
}
