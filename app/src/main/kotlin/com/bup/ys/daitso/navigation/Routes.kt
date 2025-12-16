package com.bup.ys.daitso.navigation

import kotlinx.serialization.Serializable

/**
 * Home screen route for the main product listing screen.
 * This is the entry point of the application.
 */
@Serializable
object HomeRoute

/**
 * Product detail screen route with product ID parameter.
 * Used to navigate to the product detail page with a specific product.
 *
 * @param productId The ID of the product to display
 */
@Serializable
data class ProductDetailRoute(val productId: String)

/**
 * Shopping cart screen route.
 * Displays the current items in the shopping cart.
 */
@Serializable
object CartRoute
