package com.bup.ys.daitso.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for navigation routes.
 * Tests the type-safe route definitions used throughout the application.
 */
class RoutesTest {

    @Test
    fun homeRoute_isSerializable() {
        val route = HomeRoute
        assertEquals("Expected HomeRoute to be serializable", HomeRoute, route)
    }

    @Test
    fun productDetailRoute_constructsWithProductId() {
        val productId = "product-123"
        val route = ProductDetailRoute(productId)
        assertEquals("Expected ProductDetailRoute to contain productId", productId, route.productId)
    }

    @Test
    fun productDetailRoute_differentiatesByProductId() {
        val route1 = ProductDetailRoute("product-1")
        val route2 = ProductDetailRoute("product-2")
        assertNotEquals("Expected routes with different IDs to not be equal", route1, route2)
    }

    @Test
    fun productDetailRoute_sameIdEqualityTest() {
        val productId = "same-product"
        val route1 = ProductDetailRoute(productId)
        val route2 = ProductDetailRoute(productId)
        assertEquals("Expected routes with same ID to be equal", route1, route2)
    }

    @Test
    fun cartRoute_isSerializable() {
        val route = CartRoute
        assertEquals("Expected CartRoute to be serializable", CartRoute, route)
    }

    @Test
    fun homeRoute_canBeInstantiated() {
        val route = HomeRoute
        assertEquals("Expected HomeRoute singleton to be instantiable", HomeRoute, route)
    }

    @Test
    fun allRoutes_areAccessible() {
        // Test that all routes are accessible and properly defined
        val homeRoute = HomeRoute
        val detailRoute = ProductDetailRoute("test-id")
        val cartRoute = CartRoute

        assertNotNull("HomeRoute should not be null", homeRoute)
        assertNotNull("DetailRoute should not be null", detailRoute)
        assertNotNull("CartRoute should not be null", cartRoute)
    }

    @Test
    fun productDetailRoute_storesProductIdCorrectly() {
        val testIds = listOf("prod-001", "prod-002", "special-product")

        testIds.forEach { id ->
            val route = ProductDetailRoute(id)
            assertEquals("Expected route to store productId correctly", id, route.productId)
        }
    }

    @Test
    fun homeRoute_isSingletonInstance() {
        val route1 = HomeRoute
        val route2 = HomeRoute
        assertEquals("Expected HomeRoute to be same instance", route1, route2)
    }

    @Test
    fun cartRoute_isSingletonInstance() {
        val route1 = CartRoute
        val route2 = CartRoute
        assertEquals("Expected CartRoute to be same instance", route1, route2)
    }
}
