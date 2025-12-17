package com.bup.ys.daitso.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bup.ys.daitso.feature.cart.contract.CartIntent
import com.bup.ys.daitso.feature.cart.presentation.CartScreen
import com.bup.ys.daitso.feature.cart.presentation.CartViewModel
import com.bup.ys.daitso.feature.detail.contract.ProductDetailIntent
import com.bup.ys.daitso.feature.detail.ui.ProductDetailScreen
import com.bup.ys.daitso.feature.detail.viewmodel.ProductDetailViewModel
import com.bup.ys.daitso.feature.home.ui.HomeScreen
import com.bup.ys.daitso.feature.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * Navigation route constants
 */
object NavRoutes {
    const val HOME = "home"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val CART = "cart"

    fun productDetail(productId: String) = "product_detail/$productId"
}

/**
 * Main navigation host for the Daitso application.
 *
 * Manages the navigation between three main screens:
 * - Home: Product listing screen
 * - ProductDetail: Product detail page with add to cart functionality
 * - Cart: Shopping cart screen
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
        startDestination = NavRoutes.HOME
    ) {
        // Home Screen Route
        composable(route = NavRoutes.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onProductClick = { productId ->
                    navController.navigate(NavRoutes.productDetail(productId))
                },
                onNavigateToDetail = { productId ->
                    navController.navigate(NavRoutes.productDetail(productId))
                }
            )
        }

        // Product Detail Screen Route
        composable(
            route = NavRoutes.PRODUCT_DETAIL,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val viewModel: ProductDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            // Load product when screen is first shown
            LaunchedEffect(productId) {
                viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
            }

            ProductDetailScreen(
                state = uiState,
                onIntentSubmitted = { intent ->
                    coroutineScope.launch {
                        viewModel.submitEvent(intent)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(NavRoutes.CART)
                }
            )
        }

        // Cart Screen Route
        composable(route = NavRoutes.CART) {
            val viewModel: CartViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                // Load cart items when screen is first shown
                viewModel.submitEvent(CartIntent.LoadCartItems)
            }

            CartScreen(
                state = uiState,
                onLoadCart = {
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.LoadCartItems)
                    }
                },
                onUpdateQuantity = { productId, newQuantity ->
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.UpdateQuantity(productId, newQuantity))
                    }
                },
                onRemoveItem = { productId ->
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.RemoveItem(productId))
                    }
                },
                onClearCart = {
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.ClearCart)
                    }
                },
                onDismissError = {
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.DismissError)
                    }
                }
            )
        }
    }
}
