package com.bup.ys.daitso.feature.home.viewmodel

import com.bup.ys.daitso.core.ui.base.BaseViewModel
import com.bup.ys.daitso.feature.home.contract.HomeContract

/**
 * HOME 화면의 ViewModel
 *
 * MVI 아키텍처를 따르며, BaseViewModel을 확장합니다.
 * - State: HomeState (Initial, Loading, Success, Error)
 * - Event: HomeEvent (LoadProducts, RetryLoad)
 * - SideEffect: HomeSideEffect (ShowError, NavigateToProductDetail)
 *
 * 책임:
 * - 상품 목록 로드 이벤트 처리
 * - UI 상태 관리 (로딩, 성공, 에러)
 * - 사이드 이펙트 방출 (토스트, 네비게이션)
 */
class HomeViewModel : BaseViewModel<HomeContract.HomeState, HomeContract.HomeEvent, HomeContract.HomeSideEffect>(
    initialState = HomeContract.HomeState.Initial
) {

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
            is HomeContract.HomeEvent.LoadProducts -> {
                // 상품 목록 로드를 시작
                // 추후 실제 데이터 소스와 연결될 예정
                handleLoadProducts()
            }
            is HomeContract.HomeEvent.RetryLoad -> {
                // 이전 로드 실패 후 다시 시도
                handleRetryLoad()
            }
        }
    }

    /**
     * 상품 목록을 로드합니다.
     *
     * 프로세스:
     * 1. 로딩 상태로 변경
     * 2. 데이터 소스에서 상품 목록 로드
     * 3. 성공 또는 실패 상태로 변경
     */
    private suspend fun handleLoadProducts() {
        // 추후 실제 저장소와 연결될 때 구현
        // 현재는 초기 상태를 유지
    }

    /**
     * 로드 실패 후 재시도합니다.
     *
     * 프로세스:
     * 1. 로딩 상태로 변경
     * 2. 이전 실패 원인 분석 (선택 사항)
     * 3. 다시 데이터 로드 시도
     */
    private suspend fun handleRetryLoad() {
        // 추후 실제 저장소와 연결될 때 구현
        // 현재는 초기 상태를 유지
    }
}
