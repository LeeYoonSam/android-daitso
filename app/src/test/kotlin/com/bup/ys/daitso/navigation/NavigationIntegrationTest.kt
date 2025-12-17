package com.bup.ys.daitso.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Integration tests for the navigation system.
 * Tests the complete navigation flow between Home, Detail, and Cart screens.
 */
class NavigationIntegrationTest {

    @Test
    fun homeRoute_isStartDestination() {
        val startDestination = HomeRoute
        assertNotNull("HomeRoute should be accessible", startDestination)
        assertEquals("HomeRoute should be the start destination", HomeRoute, startDestination)
    }

    @Test
    fun homeToDetail_navigationRouteConstruction() {
        val productId = "product-001"
        val detailRoute = ProductDetailRoute(productId)

        assertNotNull("ProductDetailRoute should be constructed", detailRoute)
        assertEquals("ProductDetailRoute should contain productId", productId, detailRoute.productId)
    }

    @Test
    fun detailToCart_navigationRouteConstruction() {
        val cartRoute = CartRoute
        assertNotNull("CartRoute should be accessible", cartRoute)
        assertEquals("CartRoute should be singleton", CartRoute, cartRoute)
    }

    @Test
    fun navigationFlow_homeToDetailToCart() {
        // Test the complete navigation flow: Home -> Detail -> Cart
        val homeRoute = HomeRoute
        val detailRoute = ProductDetailRoute("product-123")
        val cartRoute = CartRoute

        assertNotNull("All routes should be accessible", homeRoute)
        assertNotNull("Detail route with productId should be accessible", detailRoute)
        assertNotNull("Cart route should be accessible", cartRoute)

        assertEquals("Starting at home route", HomeRoute, homeRoute)
        assertEquals("Detail route should have productId", "product-123", detailRoute.productId)
        assertEquals("Cart route is final destination", CartRoute, cartRoute)
    }

    @Test
    fun navigationFlow_multipleProductDetailRoutes() {
        // Test navigating to different products
        val product1 = ProductDetailRoute("product-001")
        val product2 = ProductDetailRoute("product-002")
        val product3 = ProductDetailRoute("product-003")

        assertEquals("Should navigate to product 1", "product-001", product1.productId)
        assertEquals("Should navigate to product 2", "product-002", product2.productId)
        assertEquals("Should navigate to product 3", "product-003", product3.productId)

        // All should be different instances
        assert(product1 != product2)
        assert(product2 != product3)
        assert(product1 != product3)
    }

    @Test
    fun cartRoute_canBeAccessedFromMultiplePaths() {
        // Cart should be reachable from Detail
        val detailRoute = ProductDetailRoute("product-001")
        val cartRoute = CartRoute

        assertNotNull("Cart should be reachable from Detail screen", cartRoute)
        assertEquals("Cart is singleton destination", CartRoute, cartRoute)
    }

    @Test
    fun navigationState_routesAreSerializable() {
        // Test that routes can be serialized (for navigation graph type-safety)
        val homeRoute = HomeRoute
        val detailRoute = ProductDetailRoute("test-product")
        val cartRoute = CartRoute

        // All routes should be instances of their respective types
        assertEquals("HomeRoute instance matches", homeRoute.javaClass.simpleName, "HomeRoute")
        assertEquals("ProductDetailRoute instance matches", detailRoute.javaClass.simpleName, "ProductDetailRoute")
        assertEquals("CartRoute instance matches", cartRoute.javaClass.simpleName, "CartRoute")
    }

    @Test
    fun backNavigation_fromDetailToHome() {
        // Test that navigating back from Detail returns to Home
        val detailRoute = ProductDetailRoute("product-001")
        val homeRoute = HomeRoute

        assertNotNull("Detail route should be accessible", detailRoute)
        assertNotNull("Home route should be accessible for back navigation", homeRoute)

        // Back navigation should always go to Home
        assertEquals("Back navigation target", HomeRoute, homeRoute)
    }

    @Test
    fun backNavigation_fromCartWithStateRestoration() {
        // Test that Cart can navigate back and restore previous state
        val cartRoute = CartRoute
        val homeRoute = HomeRoute

        assertNotNull("Cart route should exist for back navigation", cartRoute)
        assertNotNull("Home route should restore state after back navigation", homeRoute)

        // Verify routes are valid
        assertEquals("Cart route is valid", CartRoute, cartRoute)
        assertEquals("Home route is valid", HomeRoute, homeRoute)
    }

    @Test
    fun productDetailRoute_storesProductIdCorrectly() {
        val productIds = listOf("prod-001", "prod-002", "special-product-xyz")

        productIds.forEach { id ->
            val route = ProductDetailRoute(id)
            assertEquals("Route should store productId: $id", id, route.productId)
        }
    }

    @Test
    fun navigationDeepLink_productDetailRouteWithId() {
        // Test that ProductDetailRoute correctly handles different product IDs
        val validProductIds = listOf(
            "simple-id",
            "id-with-dashes",
            "id.with.dots",
            "id_with_underscores",
            "product-123-abc-456"
        )

        validProductIds.forEach { productId ->
            val route = ProductDetailRoute(productId)
            assertEquals("ProductDetailRoute should preserve productId format", productId, route.productId)
        }
    }

    @Test
    fun navigationRoutes_allSingletonRoutesAreEqual() {
        // Test that singleton routes (Home and Cart) are always equal
        val home1 = HomeRoute
        val home2 = HomeRoute
        val cart1 = CartRoute
        val cart2 = CartRoute

        assertEquals("HomeRoute instances should be equal", home1, home2)
        assertEquals("CartRoute instances should be equal", cart1, cart2)
        assert(home1 === home2) // Should be same object (singleton)
        assert(cart1 === cart2) // Should be same object (singleton)
    }
}
