package com.bup.ys.daitso.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bup.ys.daitso.core.model.CartItem

/**
 * Room Entity representing a CartItem in the database.
 *
 * Maps to the "cart_items" table.
 *
 * @param id Unique identifier (primary key)
 * @param productId ID of the product
 * @param productName Name of the product
 * @param quantity Quantity in cart
 * @param price Unit price
 * @param imageUrl URL of the product image
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String = ""
)

/**
 * Convert CartItemEntity to CartItem domain model.
 */
fun CartItemEntity.toDomainModel(): CartItem {
    return CartItem(
        productId = productId,
        productName = productName,
        quantity = quantity,
        price = price,
        imageUrl = imageUrl
    )
}

/**
 * Convert CartItem domain model to CartItemEntity.
 */
fun CartItem.toEntity(id: String? = null): CartItemEntity {
    return CartItemEntity(
        id = id ?: "${productId}_${System.currentTimeMillis()}",
        productId = productId,
        productName = productName,
        quantity = quantity,
        price = price
    )
}
