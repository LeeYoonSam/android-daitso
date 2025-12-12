package com.bup.ys.daitso.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bup.ys.daitso.feature.detail.contract.ProductDetailIntent
import com.bup.ys.daitso.feature.detail.contract.ProductDetailSideEffect
import com.bup.ys.daitso.feature.detail.contract.ProductDetailUiState

/**
 * Public screen composable for product detail.
 * Handles side effects and navigation logic.
 *
 * @param state Current UI state
 * @param onIntentSubmitted Callback to submit intents to ViewModel
 * @param onNavigateBack Callback to navigate back
 * @param onNavigateToCart Callback to navigate to cart
 * @param onSideEffect Callback for side effects (snackbars, etc.)
 */
@Composable
fun ProductDetailScreen(
    state: ProductDetailUiState,
    onIntentSubmitted: (ProductDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onSideEffect: (ProductDetailSideEffect) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.product?.name ?: "Product Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        ProductDetailScreenContent(
            state = state,
            onIntentSubmitted = onIntentSubmitted,
            onNavigateBack = onNavigateBack,
            onNavigateToCart = onNavigateToCart,
            modifier = Modifier.padding(paddingValues)
        )
    }

    // Handle side effects (snackbar display)
    LaunchedEffect(state.addToCartSuccess) {
        if (state.addToCartSuccess) {
            snackbarHostState.showSnackbar(
                message = "${state.product?.name}을(를) 장바구니에 추가했습니다",
                actionLabel = "장바구니 보기",
                duration = SnackbarDuration.Long
            ).let { result ->
                if (result.toString() == "ActionPerformed") {
                    onNavigateToCart()
                }
            }
        }
    }
}

/**
 * Internal content composable for ProductDetailScreen.
 * Separated for easier testing and composition preview.
 */
@Composable
internal fun ProductDetailScreenContent(
    state: ProductDetailUiState,
    onIntentSubmitted: (ProductDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> {
                // Loading state
                LoadingContent(modifier = Modifier.align(Alignment.Center))
            }

            state.error != null -> {
                // Error state
                ErrorContent(
                    error = state.error,
                    onRetry = { onIntentSubmitted(ProductDetailIntent.LoadProduct("")) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.product != null -> {
                // Success state
                SuccessContent(
                    state = state,
                    onIntentSubmitted = onIntentSubmitted,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                // Empty state
                EmptyContent(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

/**
 * Loading state content showing a progress indicator.
 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .testTag("loading_progress"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "로딩 중...",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Error state content showing error message and retry button.
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty state content when no product is loaded.
 */
@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "상품 정보를 불러와주세요",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Success state content showing product details and purchase options.
 */
@Composable
private fun SuccessContent(
    state: ProductDetailUiState,
    onIntentSubmitted: (ProductDetailIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val product = state.product ?: return

        // Product image
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )

        // Product name
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Price
        Text(
            text = "$${"%.2f".format(product.price)}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Description
        Text(
            text = product.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Stock availability
        val stockText = if (product.stock > 0) {
            "In Stock (${product.stock} available)"
        } else {
            "Out of Stock"
        }
        Text(
            text = stockText,
            style = MaterialTheme.typography.bodySmall,
            color = if (product.stock > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        // Quantity selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quantity:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            QuantitySelector(
                quantity = state.selectedQuantity,
                onQuantityChange = { newQuantity ->
                    onIntentSubmitted(ProductDetailIntent.SetQuantity(newQuantity))
                }
            )
        }

        // Add to cart button
        Button(
            onClick = {
                onIntentSubmitted(ProductDetailIntent.AddToCart)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_to_cart_button"),
            enabled = !state.isAddingToCart && product.stock > 0
        ) {
            Text(
                text = if (state.isAddingToCart) "Adding..." else "Add to Cart",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
