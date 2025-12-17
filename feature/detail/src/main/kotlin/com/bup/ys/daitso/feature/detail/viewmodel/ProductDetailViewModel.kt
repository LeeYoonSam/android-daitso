package com.bup.ys.daitso.feature.detail.viewmodel

import androidx.lifecycle.viewModelScope
import com.bup.ys.daitso.core.ui.base.BaseViewModel
import com.bup.ys.daitso.feature.detail.contract.ProductDetailIntent
import com.bup.ys.daitso.feature.detail.contract.ProductDetailSideEffect
import com.bup.ys.daitso.feature.detail.contract.ProductDetailUiState
import com.bup.ys.daitso.core.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the product detail screen.
 * Manages product loading, quantity selection, and cart operations.
 *
 * Implements MVI pattern with:
 * - State: ProductDetailUiState
 * - Intent: ProductDetailIntent
 * - SideEffect: ProductDetailSideEffect
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : BaseViewModel<ProductDetailUiState, ProductDetailIntent, ProductDetailSideEffect>(
    initialState = ProductDetailUiState()
) {

    override suspend fun handleEvent(event: ProductDetailIntent) {
        when (event) {
            is ProductDetailIntent.LoadProduct -> loadProduct(event.productId)
            is ProductDetailIntent.SetQuantity -> setQuantity(event.quantity)
            is ProductDetailIntent.AddToCart -> addToCart()
            is ProductDetailIntent.DismissError -> dismissError()
            is ProductDetailIntent.DismissSuccess -> dismissSuccess()
        }
    }

    /**
     * Loads product details by ID.
     * Updates state with loading status, success, or error.
     */
    private suspend fun loadProduct(productId: String) {
        try {
            updateState(currentState.copy(isLoading = true, error = null))

            val product = cartRepository.getProductDetails(productId)

            updateState(
                currentState.copy(
                    product = product,
                    isLoading = false,
                    selectedQuantity = 1, // Reset quantity when loading new product
                    error = null
                )
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error occurred"
            updateState(
                currentState.copy(
                    isLoading = false,
                    error = errorMessage
                )
            )
        }
    }

    /**
     * Sets the selected quantity for the product.
     * Clamps the quantity between 1 and 999.
     */
    private suspend fun setQuantity(quantity: Int) {
        val clampedQuantity = quantity.coerceIn(1, 999)
        updateState(currentState.copy(selectedQuantity = clampedQuantity))
    }

    /**
     * Adds the selected product with the chosen quantity to the cart.
     * Shows appropriate snackbar feedback.
     */
    private suspend fun addToCart() {
        val product = currentState.product ?: return

        try {
            updateState(currentState.copy(isAddingToCart = true))

            val success = cartRepository.addToCart(
                product = product,
                quantity = currentState.selectedQuantity
            )

            if (success) {
                updateState(
                    currentState.copy(
                        isAddingToCart = false,
                        addToCartSuccess = true
                    )
                )

                launchSideEffect(
                    ProductDetailSideEffect.ShowSnackbar(
                        message = "${product.name}을(를) 장바구니에 추가했습니다",
                        actionLabel = "장바구니 보기"
                    )
                )
            } else {
                updateState(
                    currentState.copy(
                        isAddingToCart = false,
                        error = "장바구니 추가 실패"
                    )
                )
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "장바구니 추가 중 오류가 발생했습니다"
            updateState(
                currentState.copy(
                    isAddingToCart = false,
                    error = errorMessage
                )
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
     * Dismisses the success message.
     */
    private suspend fun dismissSuccess() {
        updateState(currentState.copy(addToCartSuccess = false))
    }
}
