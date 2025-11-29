package com.bup.ys.daitso.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model for a CartItem.
 *
 * @param productId ID of the product in this cart item
 * @param productName Name of the product
 * @param quantity Quantity of the product in the cart
 * @param price Unit price of the product
 * @param imageUrl URL of the product image
 */
@Serializable
data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String
)
