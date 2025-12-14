package com.bup.ys.daitso.feature.cart.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bup.ys.daitso.feature.cart.contract.CartIntent
import com.bup.ys.daitso.feature.cart.contract.CartSideEffect
import com.bup.ys.daitso.feature.cart.presentation.CartScreen
import com.bup.ys.daitso.feature.cart.presentation.CartViewModel
import kotlinx.coroutines.launch

/**
 * Navigation route for the shopping cart screen.
 */
object CartRoute {
    const val ROUTE = "cart"
}

/**
 * Add cart screen to navigation graph.
 * To use this, call: navGraphBuilder.cartScreen(navController)
 *
 * Sets up the cart screen route with view model and navigation callbacks.
 *
 * @param navController Navigation controller for navigation operations
 */
fun NavGraphBuilder.cartScreen(
    navController: NavController
) {
    composable(route = CartRoute.ROUTE) {
        val viewModel: CartViewModel = hiltViewModel()
        val state = viewModel.uiState.collectAsState()

        // Handle side effects
        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect { sideEffect ->
                when (sideEffect) {
                    is CartSideEffect.NavigateToCheckout -> {
                        // Navigate to checkout screen
                        // navController.navigate("checkout")
                    }
                    is CartSideEffect.ShowToast -> {
                        // Show toast message
                    }
                    is CartSideEffect.NavigateToHome -> {
                        navController.navigate("home") {
                            popUpTo(CartRoute.ROUTE) { inclusive = true }
                        }
                    }
                }
            }
        }

        CartScreen(
            state = state.value,
            onLoadCart = {
                viewModel.viewModelScope.launch {
                    viewModel.handleEvent(CartIntent.LoadCartItems)
                }
            },
            onUpdateQuantity = { productId, quantity ->
                viewModel.viewModelScope.launch {
                    viewModel.handleEvent(CartIntent.UpdateQuantity(productId, quantity))
                }
            },
            onRemoveItem = { productId ->
                viewModel.viewModelScope.launch {
                    viewModel.handleEvent(CartIntent.RemoveItem(productId))
                }
            },
            onClearCart = {
                viewModel.viewModelScope.launch {
                    viewModel.handleEvent(CartIntent.ClearCart)
                }
            },
            onDismissError = {
                viewModel.viewModelScope.launch {
                    viewModel.handleEvent(CartIntent.DismissError)
                }
            }
        )
    }
}
