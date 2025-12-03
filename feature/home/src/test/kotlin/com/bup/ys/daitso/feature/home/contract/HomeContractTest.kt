package com.bup.ys.daitso.feature.home.contract

import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.core.ui.contract.UiEvent
import com.bup.ys.daitso.core.ui.contract.UiSideEffect
import com.bup.ys.daitso.core.ui.contract.UiState
import org.junit.Test

/**
 * 테스트: HomeContract는 HomeState, HomeEvent, HomeSideEffect를 정의해야 함
 *
 * 이 테스트는 HomeContract 클래스가 MVI 아키텍처의 세 가지 주요 컴포넌트를
 * 올바르게 구현했는지 검증합니다.
 */
class HomeContractTest {

    /**
     * 테스트 1: HomeState는 UiState를 구현해야 함
     * - Initial: 초기 상태
     * - Loading: 데이터 로딩 중
     * - Success: 데이터 로드 성공
     * - Error: 데이터 로드 실패
     */
    @Test
    fun homeStateImplementsUiState() {
        // GIVEN: HomeState의 각 구현이 생성될 때
        val initialState: UiState = HomeContract.HomeState.Initial
        val loadingState: UiState = HomeContract.HomeState.Loading
        val successState: UiState = HomeContract.HomeState.Success(emptyList())
        val errorState: UiState = HomeContract.HomeState.Error("테스트 에러")

        // THEN: 모두 UiState 인터페이스를 구현해야 함
        assert(initialState is HomeContract.HomeState)
        assert(loadingState is HomeContract.HomeState)
        assert(successState is HomeContract.HomeState)
        assert(errorState is HomeContract.HomeState)
    }

    /**
     * 테스트 2: HomeEvent는 UiEvent를 구현해야 함
     * - LoadProducts: 상품 목록 로드 요청
     * - RetryLoad: 로드 재시도
     */
    @Test
    fun homeEventImplementsUiEvent() {
        // GIVEN: HomeEvent의 각 구현이 생성될 때
        val loadEvent: UiEvent = HomeContract.HomeEvent.LoadProducts
        val retryEvent: UiEvent = HomeContract.HomeEvent.RetryLoad

        // THEN: 모두 UiEvent 인터페이스를 구현해야 함
        assert(loadEvent is HomeContract.HomeEvent)
        assert(retryEvent is HomeContract.HomeEvent)
    }

    /**
     * 테스트 3: HomeSideEffect는 UiSideEffect를 구현해야 함
     * - ShowError: 에러 메시지 표시
     * - NavigateToProductDetail: 상품 상세 화면으로 네비게이션
     */
    @Test
    fun homeSideEffectImplementsUiSideEffect() {
        // GIVEN: HomeSideEffect의 각 구현이 생성될 때
        val showErrorEffect: UiSideEffect = HomeContract.HomeSideEffect.ShowError("에러 메시지")
        val navigateEffect: UiSideEffect = HomeContract.HomeSideEffect.NavigateToProductDetail("product-1")

        // THEN: 모두 UiSideEffect 인터페이스를 구현해야 함
        assert(showErrorEffect is HomeContract.HomeSideEffect)
        assert(navigateEffect is HomeContract.HomeSideEffect)
    }

    /**
     * 테스트 4: HomeState.Success는 Product 리스트를 보유해야 함
     */
    @Test
    fun homeStateSuccessContainsProductList() {
        // GIVEN: 상품 리스트가 있을 때
        val products = listOf(
            Product("1", "상품1", "설명1", 10000.0, "url1", "카테고리1"),
            Product("2", "상품2", "설명2", 20000.0, "url2", "카테고리2")
        )

        // WHEN: Success 상태가 생성될 때
        val successState = HomeContract.HomeState.Success(products)

        // THEN: 상품 리스트를 올바르게 보유해야 함
        assert(successState.products == products)
        assert(successState.products.size == 2)
    }

    /**
     * 테스트 5: HomeState.Error는 에러 메시지를 보유해야 함
     */
    @Test
    fun homeStateErrorContainsMessage() {
        // GIVEN: 에러 메시지가 있을 때
        val errorMessage = "네트워크 연결 실패"

        // WHEN: Error 상태가 생성될 때
        val errorState = HomeContract.HomeState.Error(errorMessage)

        // THEN: 에러 메시지를 올바르게 보유해야 함
        assert(errorState.message == errorMessage)
    }
}
