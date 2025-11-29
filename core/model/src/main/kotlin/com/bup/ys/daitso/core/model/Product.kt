package com.bup.ys.daitso.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model for a Product.
 *
 * @param id Unique identifier for the product
 * @param name Product name
 * @param description Product description
 * @param price Product price
 * @param imageUrl URL of the product image
 * @param category Product category
 */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String
)
