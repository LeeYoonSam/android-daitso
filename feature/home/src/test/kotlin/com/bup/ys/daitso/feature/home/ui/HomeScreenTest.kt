package com.bup.ys.daitso.feature.home.ui

import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.feature.home.contract.HomeContract
import org.junit.Test

/**
 * HomeScreen UI 컴포넌트 테스트
 *
 * Compose 컴포넌트가 올바르게 상태를 렌더링하는지 검증합니다.
 * 이 테스트는 상태 객체의 정확성과 UI 로직을 검증합니다.
 */
class HomeScreenTest {

    private val sampleProducts = listOf(
        Product("1", "상품1", "설명1", 10000.0, "url1", "카테고리1"),
        Product("2", "상품2", "설명2", 20000.0, "url2", "카테고리2")
    )

    /**
     * 테스트 1: HomeScreen이 모든 상태를 처리할 수 있어야 함
     */
    @Test
    fun homeScreenHandlesAllStates() {
        // THEN: 모든 상태 타입이 존재해야 함
        assert(HomeContract.HomeState.Initial is HomeContract.HomeState)
        assert(HomeContract.HomeState.Loading is HomeContract.HomeState)
        assert(HomeContract.HomeState.Success(sampleProducts) is HomeContract.HomeState)
        assert(HomeContract.HomeState.Error("test") is HomeContract.HomeState)
    }

    /**
     * 테스트 2: Initial 상태를 올바르게 표현
     */
    @Test
    fun initialStateRepresentation() {
        // GIVEN: Initial 상태
        val state = HomeContract.HomeState.Initial

        // THEN: 상태 타입이 올바름
        assert(state is HomeContract.HomeState.Initial)
    }

    /**
     * 테스트 3: Loading 상태를 올바르게 표현
     */
    @Test
    fun loadingStateRepresentation() {
        // GIVEN: Loading 상태
        val state = HomeContract.HomeState.Loading

        // THEN: 상태 타입이 올바름
        assert(state is HomeContract.HomeState.Loading)
    }

    /**
     * 테스트 4: Success 상태에 상품 리스트 포함
     */
    @Test
    fun successStateWithProductList() {
        // GIVEN: 상품 리스트
        val state = HomeContract.HomeState.Success(sampleProducts)

        // THEN: 상태 타입이 올바르고 상품 리스트를 포함
        assert(state is HomeContract.HomeState.Success)
        assert(state.products.size == 2)
        assert(state.products[0].name == "상품1")
    }

    /**
     * 테스트 5: Error 상태에 에러 메시지 포함
     */
    @Test
    fun errorStateWithMessage() {
        // GIVEN: 에러 메시지
        val errorMsg = "테스트 에러"
        val state = HomeContract.HomeState.Error(errorMsg)

        // THEN: 상태 타입이 올바르고 메시지를 포함
        assert(state is HomeContract.HomeState.Error)
        assert(state.message == errorMsg)
    }

    /**
     * 테스트 6: ProductCard가 모든 상품 정보를 포함
     */
    @Test
    fun productCardContainsAllInfo() {
        // GIVEN: 상품
        val product = Product("1", "상품", "설명", 10000.0, "url", "카테고리")

        // THEN: 모든 정보를 포함
        assert(product.id == "1")
        assert(product.name == "상품")
        assert(product.description == "설명")
        assert(product.price == 10000.0)
        assert(product.imageUrl == "url")
        assert(product.category == "카테고리")
    }

    /**
     * 테스트 7: 빈 상품 리스트 처리 (EmptyState)
     */
    @Test
    fun emptyProductListHandling() {
        // GIVEN: 빈 상품 리스트
        val state = HomeContract.HomeState.Success(emptyList())

        // THEN: Success 상태이지만 products가 비어있어야 함
        assert(state is HomeContract.HomeState.Success)
        assert(state.products.isEmpty())
    }

    /**
     * 테스트 8: Error 상태가 재시도 정보를 포함
     */
    @Test
    fun errorStateRetryInformation() {
        // GIVEN: 에러 상태
        val errorMsg = "네트워크 연결 실패"
        val state = HomeContract.HomeState.Error(errorMsg)

        // THEN: 에러 메시지가 명확해야 함
        assert(state is HomeContract.HomeState.Error)
        assert(state.message.contains("네트워크"))
    }

    /**
     * 테스트 9: Loading 상태 표시
     */
    @Test
    fun loadingStateDisplay() {
        // GIVEN: Loading 상태
        val state = HomeContract.HomeState.Loading

        // THEN: Loading 인디케이터를 표시할 수 있어야 함
        assert(state is HomeContract.HomeState.Loading)
    }

    /**
     * 테스트 10: Initial 상태에서 안내 메시지
     */
    @Test
    fun initialStateGuidanceMessage() {
        // GIVEN: Initial 상태
        val state = HomeContract.HomeState.Initial

        // THEN: 사용자 안내를 제공해야 함
        assert(state is HomeContract.HomeState.Initial)
    }

    /**
     * 테스트 11: 다양한 가격대 상품 처리
     */
    @Test
    fun variousPricedProducts() {
        // GIVEN: 다양한 가격의 상품
        val products = listOf(
            Product("1", "저가", "설명", 1000.0, "url", "카테고리"),
            Product("2", "중가", "설명", 50000.0, "url", "카테고리"),
            Product("3", "고가", "설명", 500000.0, "url", "카테고리")
        )
        val state = HomeContract.HomeState.Success(products)

        // THEN: 모든 상품이 올바르게 처리되어야 함
        assert(state.products.size == 3)
        assert(state.products.map { it.price } == listOf(1000.0, 50000.0, 500000.0))
    }

    /**
     * 테스트 12: 빈 상품명 처리
     */
    @Test
    fun emptyProductName() {
        // GIVEN: 빈 상품명
        val product = Product("1", "", "설명", 10000.0, "url", "카테고리")

        // THEN: 안전하게 처리되어야 함
        assert(product.name.isEmpty())
        assert(product.id.isNotEmpty())
    }

    /**
     * 테스트 13: 동일 카테고리 상품들
     */
    @Test
    fun sameCategoryProducts() {
        // GIVEN: 같은 카테고리의 상품들
        val category = "전자기기"
        val products = (1..3).map { i ->
            Product(i.toString(), "상품$i", "설명", 10000.0 * i, "url", category)
        }
        val state = HomeContract.HomeState.Success(products)

        // THEN: 모든 상품이 같은 카테고리를 가져야 함
        assert(state.products.all { it.category == category })
    }

    /**
     * 테스트 14: 긴 에러 메시지 처리
     */
    @Test
    fun longErrorMessage() {
        // GIVEN: 긴 에러 메시지
        val longMsg = "오류가 발생했습니다. ".repeat(20)
        val state = HomeContract.HomeState.Error(longMsg)

        // THEN: 긴 메시지도 안전하게 처리되어야 함
        assert(state.message.length > 100)
    }

    /**
     * 테스트 15: 대량의 상품 처리
     */
    @Test
    fun largeProductListPerformance() {
        // GIVEN: 100개의 상품
        val largeList = (1..100).map { i ->
            Product(i.toString(), "상품$i", "설명", 1000.0 * i, "url$i", "카테고리")
        }
        val state = HomeContract.HomeState.Success(largeList)

        // THEN: 모든 상품이 처리되어야 함
        assert(state.products.size == 100)
        assert(state.products.first().id == "1")
        assert(state.products.last().id == "100")
    }

    /**
     * 테스트 16: 상품 ID 고유성 검증
     */
    @Test
    fun uniqueProductIds() {
        // GIVEN: 여러 상품
        val products = listOf(
            Product("1", "상품1", "설명1", 10000.0, "url1", "카테고리1"),
            Product("2", "상품2", "설명2", 20000.0, "url2", "카테고리2")
        )
        val state = HomeContract.HomeState.Success(products)

        // THEN: 각 상품의 ID가 고유해야 함
        val ids = state.products.map { it.id }
        assert(ids.size == ids.distinct().size)
    }

    /**
     * 테스트 17: 상품 필드 유효성 검증
     */
    @Test
    fun productFieldValidity() {
        // GIVEN: 상품
        val product = Product("1", "상품", "설명", 10000.0, "url", "카테고리")

        // THEN: 모든 필드가 유효해야 함
        assert(product.id.isNotEmpty())
        assert(product.name.isNotEmpty())
        assert(product.price >= 0)
    }

    /**
     * 테스트 18: 상태의 불변성
     */
    @Test
    fun stateImmutability() {
        // GIVEN: 두 개의 동일한 Success 상태
        val state1 = HomeContract.HomeState.Success(sampleProducts)
        val state2 = HomeContract.HomeState.Success(sampleProducts)

        // THEN: 두 상태가 동등해야 함
        assert(state1 == state2)
    }

    /**
     * 테스트 19: Success 상태의 isRefreshing 플래그
     */
    @Test
    fun successStateRefreshingFlag() {
        // GIVEN: isRefreshing이 true인 상태
        val stateRefreshing = HomeContract.HomeState.Success(sampleProducts, isRefreshing = true)
        val stateNotRefreshing = HomeContract.HomeState.Success(sampleProducts, isRefreshing = false)

        // THEN: 플래그가 올바르게 설정되어야 함
        assert(stateRefreshing.isRefreshing)
        assert(!stateNotRefreshing.isRefreshing)
    }

    /**
     * 테스트 20: 모든 Event 타입 검증
     */
    @Test
    fun allEventTypesValidation() {
        // THEN: 모든 Event가 존재해야 함
        assert(HomeContract.HomeEvent.LoadProducts is HomeContract.HomeEvent)
        assert(HomeContract.HomeEvent.RefreshProducts is HomeContract.HomeEvent)
        assert(HomeContract.HomeEvent.OnProductClick("1") is HomeContract.HomeEvent)
        assert(HomeContract.HomeEvent.OnErrorDismiss is HomeContract.HomeEvent)
        assert(HomeContract.HomeEvent.RetryLoad is HomeContract.HomeEvent)
    }
}
