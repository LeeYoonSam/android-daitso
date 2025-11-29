package com.bup.ys.daitso.core.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed class representing all navigation routes in the app.
 *
 * Each route is serializable for safe navigation state preservation and restoration.
 * Routes can be passed through navigation arguments while maintaining type safety.
 *
 * Example usage:
 * ```kotlin
 * // Navigate to home
 * navController.navigate(AppRoute.Home)
 *
 * // Navigate to product detail with parameter
 * navController.navigate(AppRoute.ProductDetail("product-123"))
 *
 * // Navigate to cart
 * navController.navigate(AppRoute.Cart)
 * ```
 */
@Serializable
sealed class AppRoute {

    /**
     * Home screen - Shows list of products or main content.
     */
    @Serializable
    object Home : AppRoute()

    /**
     * Product detail screen - Shows details for a specific product.
     *
     * @param productId The unique identifier of the product
     */
    @Serializable
    data class ProductDetail(val productId: String) : AppRoute()

    /**
     * Shopping cart screen - Shows user's shopping cart items.
     */
    @Serializable
    object Cart : AppRoute()
}
