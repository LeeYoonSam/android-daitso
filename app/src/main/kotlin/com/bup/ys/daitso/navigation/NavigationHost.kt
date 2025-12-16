package com.bup.ys.daitso.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bup.ys.daitso.feature.cart.presentation.CartScreen
import com.bup.ys.daitso.feature.cart.presentation.CartViewModel
import com.bup.ys.daitso.feature.detail.contract.ProductDetailIntent
import com.bup.ys.daitso.feature.detail.ui.ProductDetailScreen
import com.bup.ys.daitso.feature.detail.viewmodel.ProductDetailViewModel
import com.bup.ys.daitso.feature.home.ui.HomeScreen
import com.bup.ys.daitso.feature.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * Main navigation host for the Daitso application.
 *
 * Manages the navigation between three main screens:
 * - HomeRoute: Product listing screen
 * - ProductDetailRoute: Product detail page with add to cart functionality
 * - CartRoute: Shopping cart screen
 *
 * Uses Navigation Compose for type-safe navigation with Kotlin Serialization.
 *
 * @param navController Navigation controller managing the navigation stack.
 *                      If not provided, a new NavHostController is created internally.
 */
@Composable
fun DaitsoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        // Home Screen Route
        composable<HomeRoute> {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onProductClick = { productId ->
                    navController.navigate(ProductDetailRoute(productId))
                }
            )
        }

        // Product Detail Screen Route
        composable<ProductDetailRoute> {
            val viewModel: ProductDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            ProductDetailScreen(
                state = uiState,
                onIntentSubmitted = { intent ->
                    // Submit intent to view model for processing
                    viewModel.viewModelScope.launch {
                        viewModel.submitEvent(intent)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(CartRoute) {
                        popUpTo(HomeRoute) {
                            saveState = true
                        }
                        restoreState = true
                    }
                }
            )
        }

        // Cart Screen Route
        composable<CartRoute> {
            val viewModel: CartViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            CartScreen(
                state = uiState,
                onLoadCart = {
                    // Load cart when screen is shown
                },
                onUpdateQuantity = { productId, newQuantity ->
                    // Update quantity logic
                },
                onRemoveItem = { productId ->
                    // Remove item logic
                },
                onClearCart = {
                    // Clear cart logic
                },
                onDismissError = {
                    // Dismiss error logic
                }
            )
        }
    }
}
