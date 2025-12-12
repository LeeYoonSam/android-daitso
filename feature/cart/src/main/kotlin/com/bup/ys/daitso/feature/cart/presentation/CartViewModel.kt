package com.bup.ys.daitso.feature.cart.presentation

import androidx.lifecycle.viewModelScope
import com.bup.ys.daitso.core.ui.base.BaseViewModel
import com.bup.ys.daitso.feature.cart.contract.CartIntent
import com.bup.ys.daitso.feature.cart.contract.CartSideEffect
import com.bup.ys.daitso.feature.cart.contract.CartUiState
import com.bup.ys.daitso.feature.cart.domain.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the shopping cart screen.
 * Manages cart item display, quantity updates, and cart operations.
 *
 * Implements MVI pattern with:
 * - State: CartUiState
 * - Intent: CartIntent
 * - SideEffect: CartSideEffect
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : BaseViewModel<CartUiState, CartIntent, CartSideEffect>(
    initialState = CartUiState()
) {

    override suspend fun handleEvent(event: CartIntent) {
        when (event) {
            is CartIntent.LoadCartItems -> loadCartItems()
            is CartIntent.UpdateQuantity -> updateQuantity(event.productId, event.quantity)
            is CartIntent.RemoveItem -> removeItem(event.productId)
            is CartIntent.ClearCart -> clearCart()
            is CartIntent.DismissError -> dismissError()
        }
    }

    /**
     * Loads cart items from the repository and updates state.
     * Calculates total price automatically from items.
     */
    private suspend fun loadCartItems() {
        try {
            updateState(currentState.copy(isLoading = true, error = null))

            // Collect items from repository flow
            cartRepository.getCartItems().collect { items ->
                val totalPrice = calculateTotalPrice(items)
                updateState(
                    currentState.copy(
                        items = items,
                        totalPrice = totalPrice,
                        isLoading = false,
                        error = null
                    )
                )
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "장바구니 로드 중 오류가 발생했습니다"
            updateState(
                currentState.copy(
                    isLoading = false,
                    error = errorMessage
                )
            )
        }
    }

    /**
     * Updates the quantity of a specific cart item.
     * Clamps the quantity between 1 and 999.
     */
    private suspend fun updateQuantity(productId: String, quantity: Int) {
        try {
            val clampedQuantity = quantity.coerceIn(1, 999)
            cartRepository.updateQuantity(productId, clampedQuantity)

            launchSideEffect(
                CartSideEffect.ShowToast("상품 수량이 업데이트되었습니다")
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "수량 업데이트 중 오류가 발생했습니다"
            updateState(
                currentState.copy(error = errorMessage)
            )
        }
    }

    /**
     * Removes a specific item from the cart.
     */
    private suspend fun removeItem(productId: String) {
        try {
            cartRepository.removeItem(productId)

            launchSideEffect(
                CartSideEffect.ShowToast("상품이 장바구니에서 제거되었습니다")
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "상품 제거 중 오류가 발생했습니다"
            updateState(
                currentState.copy(error = errorMessage)
            )
        }
    }

    /**
     * Clears all items from the shopping cart.
     */
    private suspend fun clearCart() {
        try {
            cartRepository.clearCart()

            launchSideEffect(
                CartSideEffect.ShowToast("장바구니가 비워졌습니다")
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "장바구니 비우기 중 오류가 발생했습니다"
            updateState(
                currentState.copy(error = errorMessage)
            )
        }
    }

    /**
     * Dismisses the current error message.
     */
    private suspend fun dismissError() {
        updateState(currentState.copy(error = null))
    }

    /**
     * Calculates the total price of all items in the cart.
     * Total = Sum of (price * quantity) for each item.
     */
    private fun calculateTotalPrice(items: List<com.bup.ys.daitso.feature.cart.contract.CartItem>): Double {
        return items.fold(0.0) { total, item ->
            total + (item.price * item.quantity)
        }
    }
}
