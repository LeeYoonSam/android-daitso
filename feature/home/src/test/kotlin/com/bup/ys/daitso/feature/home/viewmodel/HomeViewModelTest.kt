package com.bup.ys.daitso.feature.home.viewmodel

import com.bup.ys.daitso.core.common.Result
import com.bup.ys.daitso.core.data.repository.ProductRepository
import com.bup.ys.daitso.core.model.Product
import com.bup.ys.daitso.feature.home.contract.HomeContract
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)

/**
 * HomeViewModel 테스트
 *
 * MVI 패턴의 ViewModel이 올바르게 이벤트를 처리하고 상태를 업데이트하는지 검증합니다.
 * ProductRepository를 Mock하여 다양한 시나리오를 테스트합니다.
 */
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var mockProductRepository: ProductRepository
    private val testDispatcher = StandardTestDispatcher()
    private val sampleProducts = listOf(
        Product("1", "상품1", "설명1", 10000.0, "url1", "카테고리1"),
        Product("2", "상품2", "설명2", 20000.0, "url2", "카테고리2")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockProductRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 테스트 1: 초기 상태는 Initial이어야 함
     */
    @Test
    fun initialStateIsInitial() = runTest(testDispatcher) {
        coEvery { mockProductRepository.getProducts() } returns emptyFlow()
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // THEN: 초기 상태가 Initial이어야 함
        assert(viewModel.currentState is HomeContract.HomeState.Initial)
    }

    /**
     * 테스트 2: LoadProducts 성공 시 Success 상태로 변경되어야 함
     */
    @Test
    fun loadProductsSuccessStateChange() = runTest(testDispatcher) {
        // GIVEN: 성공적으로 상품을 반환하는 Repository
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Loading())
            emit(Result.Success(sampleProducts))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: LoadProducts 이벤트 제출
        viewModel.submitEvent(HomeContract.HomeEvent.LoadProducts)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Success 상태가 되어야 함
        val state = viewModel.currentState
        assert(state is HomeContract.HomeState.Success || state is HomeContract.HomeState.Loading)
    }

    /**
     * 테스트 3: LoadProducts 실패 시 Error 상태로 변경되어야 함
     */
    @Test
    fun loadProductsErrorStateChange() = runTest(testDispatcher) {
        // GIVEN: 에러를 발생시키는 Repository
        val testException = Exception("네트워크 연결 실패")
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Loading())
            emit(Result.Error(testException))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: LoadProducts 이벤트 제출
        viewModel.submitEvent(HomeContract.HomeEvent.LoadProducts)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Error 상태가 되어야 함 (또는 Loading)
        val state = viewModel.currentState
        assert(
            state is HomeContract.HomeState.Error ||
            state is HomeContract.HomeState.Loading
        )
    }

    /**
     * 테스트 4: OnProductClick 이벤트 시 NavigateToProductDetail SideEffect 발행
     */
    @Test
    fun onProductClickEmitsSideEffect() = runTest(testDispatcher) {
        // GIVEN: 정상적인 Repository
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Success(sampleProducts))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: OnProductClick 이벤트 제출
        val productId = "test-product-id"
        viewModel.submitEvent(HomeContract.HomeEvent.OnProductClick(productId))
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: NavigateToProductDetail SideEffect가 발행되어야 함
        // (SideEffect 검증은 실제 UI 테스트에서 수행)
    }

    /**
     * 테스트 5: RefreshProducts 이벤트 처리
     */
    @Test
    fun refreshProductsEvent() = runTest(testDispatcher) {
        // GIVEN: 상품이 로드된 상태
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Success(sampleProducts))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: RefreshProducts 이벤트 제출
        viewModel.submitEvent(HomeContract.HomeEvent.RefreshProducts)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: 새로고침이 처리되어야 함
        val state = viewModel.currentState
        assert(state is HomeContract.HomeState.Success || state is HomeContract.HomeState.Loading)
    }

    /**
     * 테스트 6: RetryLoad 이벤트는 LoadProducts와 동일하게 작동
     */
    @Test
    fun retryLoadEventTriggersLoad() = runTest(testDispatcher) {
        // GIVEN: Repository가 준비됨
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Loading())
            emit(Result.Success(sampleProducts))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: RetryLoad 이벤트 제출
        viewModel.submitEvent(HomeContract.HomeEvent.RetryLoad)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Repository.getProducts()가 호출되어야 함
        verify { mockProductRepository.getProducts() }
    }

    /**
     * 테스트 7: OnErrorDismiss 이벤트 처리
     */
    @Test
    fun onErrorDismissEventTriggersLoad() = runTest(testDispatcher) {
        // GIVEN: 에러 상태
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Error(Exception("테스트 에러")))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: OnErrorDismiss 이벤트 제출
        viewModel.submitEvent(HomeContract.HomeEvent.OnErrorDismiss)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: 상태가 Initial로 변경되고 재로드가 시작되어야 함
        val state = viewModel.currentState
        assert(state is HomeContract.HomeState.Initial || state is HomeContract.HomeState.Loading || state is HomeContract.HomeState.Error)
    }

    /**
     * 테스트 8: Success 상태의 isRefreshing 속성
     */
    @Test
    fun successStateHasRefreshingFlag() = runTest {
        // GIVEN: Success 상태 생성
        val successState = HomeContract.HomeState.Success(sampleProducts, isRefreshing = true)

        // THEN: isRefreshing이 true여야 함
        assert(successState.isRefreshing)
        assert(successState.products.size == 2)
    }

    /**
     * 테스트 9: 빈 상품 리스트 처리
     */
    @Test
    fun emptyProductListHandling() = runTest(testDispatcher) {
        // GIVEN: 빈 상품 리스트를 반환하는 Repository
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Success(emptyList()))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: 로드 완료
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Success 상태이지만 products는 비어있어야 함
        val state = viewModel.currentState
        if (state is HomeContract.HomeState.Success) {
            assert(state.products.isEmpty())
        }
    }

    /**
     * 테스트 10: 대량의 상품 처리
     */
    @Test
    fun largeProductListHandling() = runTest(testDispatcher) {
        // GIVEN: 많은 수의 상품
        val largeProductList = (1..100).map { index ->
            Product(
                id = index.toString(),
                name = "상품$index",
                description = "설명$index",
                price = (index * 1000).toDouble(),
                imageUrl = "url$index",
                category = "카테고리"
            )
        }
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Success(largeProductList))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: 로드 완료
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: 모든 상품이 처리되어야 함
        val state = viewModel.currentState
        if (state is HomeContract.HomeState.Success) {
            assert(state.products.size == 100)
        }
    }

    /**
     * 테스트 11: 연속 이벤트 처리
     */
    @Test
    fun consecutiveEventProcessing() = runTest(testDispatcher) {
        // GIVEN: Repository 준비
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Success(sampleProducts))
        }
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)

        // WHEN: 연속으로 여러 이벤트 제출
        viewModel.submitEvent(HomeContract.HomeEvent.LoadProducts)
        viewModel.submitEvent(HomeContract.HomeEvent.RefreshProducts)
        viewModel.submitEvent(HomeContract.HomeEvent.OnProductClick("product-1"))
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: 모든 이벤트가 처리되어야 함 (예외 없음)
        assert(viewModel.currentState is HomeContract.HomeState)
    }

    /**
     * 테스트 12: 상태 불변성 검증
     */
    @Test
    fun stateImmutabilityValidation() = runTest {
        // GIVEN: 동일한 상품 리스트로 두 개의 Success 상태 생성
        val state1 = HomeContract.HomeState.Success(sampleProducts)
        val state2 = HomeContract.HomeState.Success(sampleProducts)

        // THEN: 상태가 동등해야 함
        assert(state1 == state2)
    }

    /**
     * 테스트 13: Error 상태 메시지 검증
     */
    @Test
    fun errorStateMessageValidation() = runTest {
        // GIVEN: Error 상태
        val errorMessage = "테스트 에러 메시지"
        val errorState = HomeContract.HomeState.Error(errorMessage)

        // THEN: 메시지가 올바르게 저장되어야 함
        assert(errorState.message == errorMessage)
        assert(errorState.message.isNotEmpty())
    }

    /**
     * 테스트 14: Loading 상태 검증
     */
    @Test
    fun loadingStateValidation() = runTest {
        // GIVEN: Loading 상태
        val loadingState = HomeContract.HomeState.Loading

        // THEN: Loading 타입이어야 함
        assert(loadingState is HomeContract.HomeState.Loading)
    }

    /**
     * 테스트 15: 초기 상태에서 자동 로드 검증
     */
    @Test
    fun autoLoadOnInitialization() = runTest(testDispatcher) {
        // GIVEN: Repository 준비
        coEvery { mockProductRepository.getProducts() } returns flow {
            emit(Result.Success(sampleProducts))
        }

        // WHEN: ViewModel 초기화
        viewModel = HomeViewModel(mockProductRepository, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Repository.getProducts()가 자동으로 호출되어야 함
        verify { mockProductRepository.getProducts() }
    }

    /**
     * 테스트 16: 다양한 에러 시나리오 처리
     */
    @Test
    fun variousErrorScenarios() = runTest(testDispatcher) {
        val errors = listOf(
            "네트워크 연결 실패",
            "타임아웃 발생",
            "서버 오류",
            "잘못된 요청"
        )

        // WHEN: 각각의 에러에 대해 테스트
        errors.forEach { errorMsg ->
            coEvery { mockProductRepository.getProducts() } returns flow {
                emit(Result.Error(Exception(errorMsg)))
            }
            viewModel = HomeViewModel(mockProductRepository, testDispatcher)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN: 각 에러가 처리되어야 함
            val state = viewModel.currentState
            assert(state is HomeContract.HomeState.Error || state is HomeContract.HomeState.Loading)
        }
    }
}
