package com.bup.ys.daitso.feature.home.contract

import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.core.ui.contract.UiEvent
import com.bup.ys.daitso.core.ui.contract.UiSideEffect
import com.bup.ys.daitso.core.ui.contract.UiState

/**
 * HomeContract는 HOME 화면의 MVI (Model-View-Intent) 아키텍처 컨트랙트를 정의합니다.
 *
 * 이 객체는 다음 세 가지 핵심 요소를 포함합니다:
 * - HomeState: UI 상태를 나타내는 불변 데이터 클래스
 * - HomeEvent: 사용자 액션이나 시스템 이벤트를 나타내는 sealed interface
 * - HomeSideEffect: 일회성 이벤트 (예: 네비게이션, 토스트)를 나타내는 sealed interface
 */
object HomeContract {

    /**
     * HomeState는 HOME 화면의 가능한 모든 상태를 정의합니다.
     *
     * 상태 종류:
     * - Initial: 화면 초기 로드 전 상태
     * - Loading: 데이터 로드 중
     * - Success: 데이터 로드 성공 (상품 리스트 포함)
     * - Error: 데이터 로드 실패 (에러 메시지 포함)
     */
    sealed interface HomeState : UiState {
        /**
         * 초기 상태: 아직 데이터를 요청하지 않은 상태
         */
        data object Initial : HomeState

        /**
         * 로딩 상태: 서버에서 상품 데이터를 로드 중
         */
        data object Loading : HomeState

        /**
         * 성공 상태: 상품 데이터를 성공적으로 로드함
         *
         * @property products 로드된 상품 리스트
         */
        data class Success(val products: List<Product>) : HomeState

        /**
         * 에러 상태: 상품 데이터 로드 실패
         *
         * @property message 에러 메시지
         */
        data class Error(val message: String) : HomeState
    }

    /**
     * HomeEvent는 사용자의 액션이나 시스템 이벤트를 정의합니다.
     *
     * 이벤트 종류:
     * - LoadProducts: 상품 목록 로드 요청
     * - RetryLoad: 로드 실패 후 재시도
     */
    sealed interface HomeEvent : UiEvent {
        /**
         * 상품 목록을 로드하도록 요청하는 이벤트
         */
        data object LoadProducts : HomeEvent

        /**
         * 이전 로드 시도 실패 후 다시 로드를 시도하는 이벤트
         */
        data object RetryLoad : HomeEvent
    }

    /**
     * HomeSideEffect는 일회성 이벤트를 정의합니다.
     *
     * 이러한 이벤트는 상태의 일부가 아니며, 한 번만 소비되어야 합니다.
     *
     * Side Effect 종류:
     * - ShowError: 에러 메시지를 사용자에게 표시
     * - NavigateToProductDetail: 상품 상세 화면으로 네비게이션
     */
    sealed interface HomeSideEffect : UiSideEffect {
        /**
         * 에러 메시지를 사용자에게 표시하는 side effect
         *
         * @property message 표시할 에러 메시지
         */
        data class ShowError(val message: String) : HomeSideEffect

        /**
         * 상품 상세 화면으로 네비게이션하는 side effect
         *
         * @property productId 네비게이션할 상품의 ID
         */
        data class NavigateToProductDetail(val productId: String) : HomeSideEffect
    }
}
