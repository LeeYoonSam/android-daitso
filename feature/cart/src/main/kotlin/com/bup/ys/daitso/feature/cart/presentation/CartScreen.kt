package com.bup.ys.daitso.feature.cart.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bup.ys.daitso.feature.cart.contract.CartItem
import com.bup.ys.daitso.feature.cart.contract.CartUiState

/**
 * Main composable for the shopping cart screen.
 * Displays cart items, allows quantity adjustments, and shows order summary.
 *
 * @param state The current cart UI state
 * @param onLoadCart Callback to load cart items
 * @param onUpdateQuantity Callback when quantity is updated (productId, newQuantity)
 * @param onRemoveItem Callback when an item is removed (productId)
 * @param onClearCart Callback to clear all items from cart
 * @param onDismissError Callback to dismiss error messages
 */
@Composable
fun CartScreen(
    state: CartUiState,
    onLoadCart: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onDismissError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        onLoadCart()
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error)
            onDismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("loading_indicator")
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.items.isEmpty() -> {
                // Empty cart state
                EmptyCartView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("empty_cart_message")
                        .padding(innerPadding)
                )
            }

            else -> {
                // Cart with items
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Cart items list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.items,
                            key = { it.productId }
                        ) { cartItem ->
                            CartItemRow(
                                item = cartItem,
                                onUpdateQuantity = { newQuantity ->
                                    onUpdateQuantity(cartItem.productId, newQuantity)
                                },
                                onRemoveItem = {
                                    onRemoveItem(cartItem.productId)
                                }
                            )
                        }
                    }

                    // Cart summary
                    CartSummary(
                        totalPrice = state.totalPrice,
                        itemCount = state.items.size,
                        onCheckoutClick = {
                            // TODO: Navigate to checkout
                        },
                        onClearCartClick = onClearCart
                    )
                }
            }
        }
    }
}

/**
 * Displays a single cart item with quantity controls and delete button.
 *
 * @param item The cart item to display
 * @param onUpdateQuantity Callback when quantity changes
 * @param onRemoveItem Callback when delete button is clicked
 */
@Composable
fun CartItemRow(
    item: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemoveItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .width(80.dp)
                .height(80.dp),
            contentScale = ContentScale.Crop
        )

        // Product info
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$${String.format("%.2f", item.price)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Quantity control
        QuantityControl(
            quantity = item.quantity,
            onQuantityChange = onUpdateQuantity
        )

        // Delete button
        IconButton(
            onClick = onRemoveItem,
            modifier = Modifier.testTag("delete_button_${item.productId}")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Quantity control component with increment/decrement buttons.
 *
 * @param quantity Current quantity value
 * @param onQuantityChange Callback when quantity changes
 */
@Composable
fun QuantityControl(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraSmall)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onQuantityChange(quantity - 1) },
            modifier = Modifier
                .testTag("quantity_decrease_button_${quantity}")
                .height(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "Decrease",
                modifier = Modifier.padding(4.dp)
            )
        }

        Text(
            text = quantity.toString(),
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier
                .testTag("quantity_increase_button_${quantity}")
                .height(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

/**
 * Cart summary showing total price and checkout button.
 *
 * @param totalPrice Total price of all items
 * @param itemCount Number of items in cart
 * @param onCheckoutClick Callback when checkout button is clicked
 * @param onClearCartClick Callback when clear cart button is clicked
 */
@Composable
fun CartSummary(
    totalPrice: Double,
    itemCount: Int,
    onCheckoutClick: () -> Unit,
    onClearCartClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "총 가격",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$${String.format("%.2f", totalPrice)}",
                modifier = Modifier.testTag("total_price"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "항목 수: $itemCount",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onClearCartClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("장바구니 비우기")
            }

            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("결제하기")
            }
        }
    }
}

/**
 * Empty cart state view.
 *
 * @param modifier Compose modifier
 */
@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "장바구니가 비어있습니다",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "쇼핑을 계속 진행해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
