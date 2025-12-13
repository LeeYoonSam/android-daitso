package com.bup.ys.daitso.feature.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.bup.ys.daitso.core.common.Dispatcher
import com.bup.ys.daitso.core.common.DaitsoDispatchers
import com.bup.ys.daitso.core.common.Result
import com.bup.ys.daitso.core.data.repository.ProductRepository
import com.bup.ys.daitso.core.ui.base.BaseViewModel
import com.bup.ys.daitso.feature.home.contract.HomeContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HOME 화면의 ViewModel
 *
 * MVI 아키텍처를 따르며, BaseViewModel을 확장합니다.
 * - State: HomeState (Initial, Loading, Success, Error, Refreshing)
 * - Event: HomeEvent (LoadProducts, RefreshProducts, OnProductClick, OnErrorDismiss, RetryLoad)
 * - SideEffect: HomeSideEffect (ShowError, NavigateToProductDetail, ShowToast)
 *
 * 책임:
 * - 상품 목록 로드 이벤트 처리
 * - Pull-to-Refresh 이벤트 처리
 * - 상품 클릭 이벤트 처리
 * - UI 상태 관리 (로딩, 성공, 에러)
 * - 사이드 이펙트 방출 (토스트, 네비게이션)
 * - ProductRepository를 통한 데이터 접근
 *
 * @param productRepository 상품 데이터 저장소
 * @param mainDispatcher 메인 스레드 Dispatcher
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    @Dispatcher(DaitsoDispatchers.Main) private val mainDispatcher: CoroutineDispatcher
) : BaseViewModel<HomeContract.HomeState, HomeContract.HomeEvent, HomeContract.HomeSideEffect>(
    initialState = HomeContract.HomeState.Initial
) {

    init {
        // 초기 로드
        viewModelScope.launch {
            submitEvent(HomeContract.HomeEvent.LoadProducts)
        }
    }

    /**
     * 이벤트를 처리하고 상태를 업데이트합니다.
     *
     * 이 메서드는 BaseViewModel의 이벤트 처리 루프에서 호출됩니다.
     * 각 이벤트 타입에 따라 적절한 상태 변경이 발생합니다.
     *
     * @param event 처리할 홈 화면 이벤트
     */
    override suspend fun handleEvent(event: HomeContract.HomeEvent) {
        when (event) {
            is HomeContract.HomeEvent.LoadProducts -> handleLoadProducts()
            is HomeContract.HomeEvent.RefreshProducts -> handleRefreshProducts()
            is HomeContract.HomeEvent.OnProductClick -> handleProductClick(event.productId)
            is HomeContract.HomeEvent.OnErrorDismiss -> handleErrorDismiss()
            is HomeContract.HomeEvent.RetryLoad -> handleLoadProducts()
        }
    }

    /**
     * 상품 목록을 로드합니다.
     *
     * 프로세스:
     * 1. 로딩 상태로 변경
     * 2. ProductRepository에서 상품 목록 로드
     * 3. 성공 또는 실패 상태로 변경
     */
    private suspend fun handleLoadProducts() {
        updateState(HomeContract.HomeState.Loading)

        try {
            productRepository.getProducts().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        updateState(HomeContract.HomeState.Loading)
                    }
                    is Result.Success -> {
                        updateState(
                            HomeContract.HomeState.Success(
                                products = result.data,
                                isRefreshing = false
                            )
                        )
                    }
                    is Result.Error -> {
                        val errorMessage = result.exception?.message
                            ?: "알 수 없는 오류가 발생했습니다"
                        updateState(HomeContract.HomeState.Error(errorMessage))
                        launchSideEffect(HomeContract.HomeSideEffect.ShowError(errorMessage))
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "상품 목록을 로드할 수 없습니다"
            updateState(HomeContract.HomeState.Error(errorMessage))
            launchSideEffect(HomeContract.HomeSideEffect.ShowError(errorMessage))
        }
    }

    /**
     * 상품 목록을 새로고침합니다 (Pull-to-Refresh).
     *
     * 프로세스:
     * 1. 현재 상태를 Success로 유지하면서 isRefreshing을 true로 설정
     * 2. ProductRepository에서 상품 목록 다시 로드
     * 3. 새로고침 완료 후 isRefreshing을 false로 설정
     */
    private suspend fun handleRefreshProducts() {
        val currentState = currentState
        if (currentState is HomeContract.HomeState.Success) {
            updateState(currentState.copy(isRefreshing = true))
        }

        try {
            productRepository.getProducts().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // 새로고침 중 상태 유지
                    }
                    is Result.Success -> {
                        updateState(
                            HomeContract.HomeState.Success(
                                products = result.data,
                                isRefreshing = false
                            )
                        )
                        launchSideEffect(
                            HomeContract.HomeSideEffect.ShowToast("새로고침 완료")
                        )
                    }
                    is Result.Error -> {
                        val errorMessage = result.exception?.message
                            ?: "새로고침 중 오류가 발생했습니다"
                        if (currentState is HomeContract.HomeState.Success) {
                            updateState(currentState.copy(isRefreshing = false))
                        }
                        launchSideEffect(
                            HomeContract.HomeSideEffect.ShowToast(errorMessage)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "새로고침 중 오류가 발생했습니다"
            if (currentState is HomeContract.HomeState.Success) {
                updateState(currentState.copy(isRefreshing = false))
            }
            launchSideEffect(HomeContract.HomeSideEffect.ShowToast(errorMessage))
        }
    }

    /**
     * 상품 클릭 이벤트를 처리합니다.
     *
     * 상품 상세 화면으로 네비게이션하는 SideEffect를 방출합니다.
     *
     * @param productId 클릭된 상품의 ID
     */
    private suspend fun handleProductClick(productId: String) {
        launchSideEffect(
            HomeContract.HomeSideEffect.NavigateToProductDetail(productId)
        )
    }

    /**
     * 에러 상태를 해제합니다.
     *
     * 에러 상태를 Initial로 변경하고 데이터를 다시 로드합니다.
     */
    private suspend fun handleErrorDismiss() {
        updateState(HomeContract.HomeState.Initial)
        handleLoadProducts()
    }
}
