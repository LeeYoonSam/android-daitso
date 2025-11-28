package com.bup.ys.daitso.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model for a CartItem.
 *
 * @param id Unique identifier for the cart item
 * @param productId ID of the product in this cart item
 * @param productName Name of the product
 * @param quantity Quantity of the product in the cart
 * @param price Unit price of the product
 */
@Serializable
data class CartItem(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)
