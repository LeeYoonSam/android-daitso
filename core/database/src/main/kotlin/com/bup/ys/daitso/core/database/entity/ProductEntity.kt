package com.bup.ys.daitso.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bup.ys.daitso.core.model.Product

/**
 * Room Entity representing a Product in the database.
 *
 * Maps to the "products" table.
 *
 * @param id Unique identifier (primary key)
 * @param name Product name
 * @param description Product description
 * @param price Product price
 * @param imageUrl URL of the product image
 * @param category Product category
 * @param stock Available stock quantity
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val stock: Int
)

/**
 * Convert ProductEntity to Product domain model.
 */
fun ProductEntity.toDomainModel(): Product {
    return Product(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        category = category,
        stock = stock
    )
}

/**
 * Convert Product domain model to ProductEntity.
 */
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        category = category,
        stock = stock
    )
}
