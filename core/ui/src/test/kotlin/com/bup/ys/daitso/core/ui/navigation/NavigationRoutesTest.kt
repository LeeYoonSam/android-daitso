package com.bup.ys.daitso.core.ui.navigation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for Navigation Routes serialization.
 *
 * These tests verify that navigation routes can be properly serialized
 * and deserialized for navigation purposes.
 */
class NavigationRoutesTest {

    private val json = Json

    @Test
    fun testHomeRouteCanBeSerialized() {
        val route = AppRoute.Home
        val serialized = json.encodeToString(route)
        assert(serialized.isNotEmpty())
    }

    @Test
    fun testHomeRouteCanBeDeserialized() {
        val serialized = json.encodeToString<AppRoute>(AppRoute.Home)
        val deserialized = json.decodeFromString<AppRoute>(serialized)
        assertIs<AppRoute.Home>(deserialized)
    }

    @Test
    fun testProductDetailRouteWithParameter() {
        val route = AppRoute.ProductDetail("product-123")
        val serialized = json.encodeToString(route)
        assert(serialized.isNotEmpty())
        assert(serialized.contains("product-123"))
    }

    @Test
    fun testProductDetailRoutePreservesParameter() {
        val productId = "product-456"
        val route = AppRoute.ProductDetail(productId)
        val serialized = json.encodeToString<AppRoute>(route)
        val deserialized = json.decodeFromString<AppRoute>(serialized)
        assertIs<AppRoute.ProductDetail>(deserialized)
        assertEquals(productId, (deserialized as AppRoute.ProductDetail).productId)
    }

    @Test
    fun testCartRouteCanBeSerialized() {
        val route = AppRoute.Cart
        val serialized = json.encodeToString(route)
        assert(serialized.isNotEmpty())
    }

    @Test
    fun testCartRouteCanBeDeserialized() {
        val serialized = json.encodeToString<AppRoute>(AppRoute.Cart)
        val deserialized = json.decodeFromString<AppRoute>(serialized)
        assertIs<AppRoute.Cart>(deserialized)
    }

    @Test
    fun testMultipleRouteTypesSerialization() {
        val routes: List<AppRoute> = listOf(
            AppRoute.Home,
            AppRoute.ProductDetail("id-1"),
            AppRoute.Cart
        )

        routes.forEach { route ->
            val serialized = json.encodeToString<AppRoute>(route)
            val deserialized = json.decodeFromString<AppRoute>(serialized)
            assertIs<AppRoute>(deserialized)
        }
    }

    @Test
    fun testNavigationRouteRetainsType() {
        val homeRoute = AppRoute.Home
        val cartRoute = AppRoute.Cart
        val detailRoute = AppRoute.ProductDetail("test")

        assertEquals(AppRoute.Home::class, homeRoute::class)
        assertEquals(AppRoute.Cart::class, cartRoute::class)
        assertEquals(AppRoute.ProductDetail::class, detailRoute::class)
    }

    @Test
    fun testDifferentProductIdsCreateDifferentRoutes() {
        val route1 = AppRoute.ProductDetail("product-1")
        val route2 = AppRoute.ProductDetail("product-2")

        val serialized1 = json.encodeToString<AppRoute>(route1)
        val serialized2 = json.encodeToString<AppRoute>(route2)

        assert(serialized1 != serialized2)
        assert(serialized1.contains("product-1"))
        assert(serialized2.contains("product-2"))
    }
}
