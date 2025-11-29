# Core UI 모듈 (:core:ui)

**SPEC**: SPEC-ANDROID-MVI-002
**최종 업데이트**: 2025-11-29
**상태**: Phase 4 완료
**작성자**: Albert (@user)

---

## 목차

- [개요](#개요)
- [MVI 패턴 구현](#mvi-패턴-구현)
- [핵심 컴포넌트](#핵심-컴포넌트)
- [네비게이션 아키텍처](#네비게이션-아키텍처)
- [의존성](#의존성)
- [사용 예시](#사용-예시)
- [테스트](#테스트)
- [문제 해결](#문제-해결)

---

## 개요

Core UI 모듈은 Android Daitso 프로젝트의 **UI 계층 아키텍처**를 담당하는 핵심 모듈입니다.

### 모듈의 역할

| 영역 | 책임 |
|------|------|
| **MVI 아키텍처** | Model-View-Intent 패턴 기본 구조 제공 |
| **상태 관리** | StateFlow 기반 UI 상태 관리 |
| **이벤트 처리** | Channel 기반 사용자 이벤트 처리 |
| **네비게이션** | Type-safe, Serializable 기반 화면 네비게이션 |
| **생명주기 관리** | ViewModel lifecycle과 Coroutine scope 통합 |

### 핵심 특징

```
특징                    설명
─────────────────────  ──────────────────────────────
단방향 데이터 흐름      Event → Handler → State → UI
제네릭 기반            <S, E, SE> 타입 파라미터
타입 안전성             Kotlin의 제네릭 시스템 활용
비동기 작업 지원       Coroutine + Flow 기반
메모리 관리             자동 resource cleanup
```

---

## MVI 패턴 구현

### MVI (Model-View-Intent) 개념

```
┌─────────────────────────────────────────────┐
│           사용자 상호작용                      │
│    (클릭, 입력, 스와이프 등)                  │
└──────────────┬────────────────────────────┘
               │
               ▼
        ┌────────────┐
        │  Intent    │  사용자 의도 표현
        │  (Event)   │
        └──────┬─────┘
               │
               ▼
        ┌──────────────────┐
        │  ViewModel       │  Intent 처리
        │  (handleEvent)   │
        └──────┬───────────┘
               │
               ▼
        ┌──────────────┐
        │  Model       │  UI 상태
        │  (State)     │
        └──────┬───────┘
               │
               ▼
        ┌──────────────────┐
        │  View            │  상태를 UI로 표현
        │  (Compose)       │
        └──────────────────┘
```

### UiState 인터페이스

**목적**: UI가 필요로 하는 모든 상태를 정의하는 마커 인터페이스

**특징**:
- 마커 인터페이스 (메서드/프로퍼티 없음)
- sealed class로 모든 상태 정의
- 불변성 (immutable) 유지

**사용 예시**:

```kotlin
// Home 화면의 상태 정의
sealed class HomeUiState : UiState {
    data object Initial : HomeUiState()
    data object Loading : HomeUiState()
    data class Success(val products: List<Product>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// ViewModel에서
class HomeViewModel : BaseViewModel<HomeUiState, HomeUiEvent, HomeSideEffect>(
    initialState = HomeUiState.Initial
) {
    // ...
}
```

### UiEvent 인터페이스

**목적**: 사용자 상호작용 및 시스템 이벤트를 정의하는 마커 인터페이스

**특징**:
- 사용자 행동 표현 (Click, Input 등)
- 시스템 이벤트 표현 (타이머, 네트워크 응답 등)
- sealed class로 모든 이벤트 정의

**사용 예시**:

```kotlin
// Home 화면의 이벤트
sealed class HomeUiEvent : UiEvent {
    data object OnLoad : HomeUiEvent()
    data object OnRefresh : HomeUiEvent()
    data class OnProductClicked(val productId: String) : HomeUiEvent()
    data class OnFilterChanged(val category: String) : HomeUiEvent()
}

// UI에서 이벤트 발생
button.setOnClickListener {
    homeViewModel.submitEvent(HomeUiEvent.OnLoad)
}
```

### UiSideEffect 인터페이스

**목적**: 한 번만 발생해야 하는 부수 효과를 정의하는 마커 인터페이스

**특징**:
- 한 번만 발생 (one-time events)
- 상태 변경이 아닌 동작 (액션)
- UI 지시사항 (네비게이션, 토스트, 다이얼로그 등)

**사용 예시**:

```kotlin
// Home 화면의 사이드 이펙트
sealed class HomeSideEffect : UiSideEffect {
    data class ShowToast(val message: String) : HomeSideEffect()
    data class NavigateToDetail(val productId: String) : HomeSideEffect()
    data class ShowDialog(val title: String, val message: String) : HomeSideEffect()
}

// ViewModel에서 사이드 이펙트 발생
override suspend fun handleEvent(event: HomeUiEvent) {
    when (event) {
        HomeUiEvent.OnLoad -> {
            try {
                updateState { HomeUiState.Loading }
                val products = repository.getProducts()
                updateState { HomeUiState.Success(products) }
                launchSideEffect(HomeSideEffect.ShowToast("로드 완료"))
            } catch (e: Exception) {
                updateState { HomeUiState.Error(e.message ?: "알 수 없는 오류") }
                launchSideEffect(HomeSideEffect.ShowToast("오류 발생"))
            }
        }
        // ...
    }
}
```

---

## 핵심 컴포넌트

### BaseViewModel<S, E, SE>

**위치**: `/core/ui/src/main/kotlin/com/bup/ys/daitso/core/ui/base/BaseViewModel.kt`

**역할**: MVI 패턴의 핵심 ViewModel 기본 클래스

**구조**:

```kotlin
abstract class BaseViewModel<S : UiState, E : UiEvent, SE : UiSideEffect>(
    initialState: S
) : ViewModel() {

    // 공개 프로퍼티
    val uiState: StateFlow<S>              // UI 상태 구독
    val uiEvent: Flow<E>                   // 이벤트 채널
    val sideEffect: Flow<SE>               // 사이드 이펙트 채널
    val currentState: S                    // 동기적 상태 접근

    // 공개 메서드
    suspend fun submitEvent(event: E)      // 이벤트 제출

    // 보호된 메서드 (하위클래스에서 사용)
    protected suspend fun updateState(newState: S)
    protected suspend fun launchSideEffect(effect: SE)

    // 추상 메서드 (반드시 구현)
    abstract suspend fun handleEvent(event: E)

    // ViewModel lifecycle
    override fun onCleared()                // 정리 작업
}
```

**주요 특징**:

1. **제네릭 타입 파라미터**
   - `S`: UiState를 상속한 상태 타입
   - `E`: UiEvent를 상속한 이벤트 타입
   - `SE`: UiSideEffect를 상속한 사이드 이펙트 타입

2. **상태 관리 (StateFlow)**
   ```kotlin
   private val _uiState = MutableStateFlow(initialState)
   val uiState: StateFlow<S> = _uiState.asStateFlow()
   ```
   - 불변 StateFlow로 노출
   - 최초 상태는 initialState
   - 상태 변경은 updateState()로만 가능

3. **이벤트 처리 (Channel)**
   ```kotlin
   private val _uiEvent = Channel<E>(BUFFERED)
   val uiEvent: Flow<E> = _uiEvent.receiveAsFlow()
   ```
   - BUFFERED capacity (64)
   - FIFO 순서 보장
   - init 블록에서 자동 처리 루프 시작

4. **사이드 이펙트 처리 (Channel)**
   ```kotlin
   private val _sideEffect = Channel<SE>(BUFFERED)
   val sideEffect: Flow<SE> = _sideEffect.receiveAsFlow()
   ```
   - 토스트, 다이얼로그, 네비게이션 등
   - 일회성 이벤트 (state가 아님)

5. **생명주기 관리**
   ```kotlin
   init {
       viewModelScope.launch {
           uiEvent.collect { event ->
               handleEvent(event)
           }
       }
   }

   override fun onCleared() {
       _uiEvent.close()
       _sideEffect.close()
   }
   ```

**사용 패턴**:

```kotlin
// 1. ViewModel 정의
class ProductDetailViewModel(
    private val productId: String,
    private val repository: ProductRepository
) : BaseViewModel<ProductDetailUiState, ProductDetailUiEvent, ProductDetailSideEffect>(
    initialState = ProductDetailUiState.Loading
) {
    init {
        // 초기 로드
        submitEvent(ProductDetailUiEvent.OnLoad)
    }

    override suspend fun handleEvent(event: ProductDetailUiEvent) {
        when (event) {
            ProductDetailUiEvent.OnLoad -> loadProduct()
            is ProductDetailUiEvent.OnAddToCart -> addToCart(event.quantity)
        }
    }

    private suspend fun loadProduct() {
        try {
            updateState(ProductDetailUiState.Loading)
            val product = repository.getProduct(productId)
            updateState(ProductDetailUiState.Success(product))
        } catch (e: Exception) {
            updateState(ProductDetailUiState.Error(e.message ?: "오류"))
        }
    }

    private suspend fun addToCart(quantity: Int) {
        try {
            repository.addToCart(productId, quantity)
            launchSideEffect(ProductDetailSideEffect.ShowToast("장바구니에 추가됨"))
            launchSideEffect(ProductDetailSideEffect.NavigateBack)
        } catch (e: Exception) {
            launchSideEffect(ProductDetailSideEffect.ShowToast("추가 실패"))
        }
    }
}

// 2. UI에서 사용 (Jetpack Compose)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ProductDetailSideEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                ProductDetailSideEffect.NavigateBack ->
                    navController.popBackStack()
            }
        }
    }

    when (state) {
        ProductDetailUiState.Loading -> LoadingScreen()
        is ProductDetailUiState.Success -> {
            val product = (state as ProductDetailUiState.Success).product
            ProductContent(
                product = product,
                onAddToCart = { quantity ->
                    viewModel.submitEvent(
                        ProductDetailUiEvent.OnAddToCart(quantity)
                    )
                }
            )
        }
        is ProductDetailUiState.Error -> {
            val message = (state as ProductDetailUiState.Error).message
            ErrorScreen(message)
        }
    }
}
```

---

## 네비게이션 아키텍처

### Routes 정의

**위치**: `/core/ui/src/main/kotlin/com/bup/ys/daitso/core/ui/navigation/Routes.kt`

**목적**: Type-safe, Serializable 기반 네비게이션 라우팅

**구조**:

```kotlin
@Serializable
sealed class AppRoute {
    /**
     * Home 화면 - 상품 목록 표시
     */
    @Serializable
    object Home : AppRoute()

    /**
     * 상품 상세 화면
     * @param productId 상품 고유 ID
     */
    @Serializable
    data class ProductDetail(val productId: String) : AppRoute()

    /**
     * 장바구니 화면 - 장바구니 항목 표시
     */
    @Serializable
    object Cart : AppRoute()
}
```

**특징**:

1. **Serializable 기반**
   - Kotlin Serialization 지원
   - State 보존 가능
   - Deep Link 지원 가능

2. **Type-safe**
   - 컴파일 시간에 타입 검사
   - 런타임 에러 방지

3. **파라미터 전달**
   ```kotlin
   // ProductDetail 경로로 파라미터 전달
   navController.navigate(AppRoute.ProductDetail("product-123"))
   ```

### Jetpack Navigation과 통합

**예시 (NavGraph)**:

```kotlin
// Navigation Composable 정의
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home,
        modifier = modifier
    ) {
        composable<AppRoute.Home> {
            HomeScreen(
                onProductClicked = { productId ->
                    navController.navigate(
                        AppRoute.ProductDetail(productId)
                    )
                }
            )
        }

        composable<AppRoute.ProductDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.ProductDetail>()
            ProductDetailScreen(
                productId = route.productId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Cart> {
            CartScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

## 의존성

### Gradle 의존성

```kotlin
// build.gradle.kts (:core:ui)

dependencies {
    // Jetpack Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Jetpack Compose (UI)
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Hilt (선택사항)
    implementation("com.google.dagger:hilt-android:2.54")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
}
```

### 모듈 의존성

```
:core:ui
├── :core:model          (도메인 모델)
├── :core:common         (공통 유틸리티)
└── :core:designsystem   (UI 컴포넌트, 테마)
```

---

## 사용 예시

### 예 1: 간단한 로딩 상태

```kotlin
// State
sealed class LoadingUiState : UiState {
    data object Initial : LoadingUiState()
    data object Loading : LoadingUiState()
    data class Success(val data: String) : LoadingUiState()
    data class Error(val message: String) : LoadingUiState()
}

// Event
sealed class LoadingUiEvent : UiEvent {
    data object OnLoad : LoadingUiEvent()
}

// SideEffect
sealed class LoadingSideEffect : UiSideEffect {
    data class ShowMessage(val text: String) : LoadingSideEffect()
}

// ViewModel
class LoadingViewModel : BaseViewModel<LoadingUiState, LoadingUiEvent, LoadingSideEffect>(
    initialState = LoadingUiState.Initial
) {
    override suspend fun handleEvent(event: LoadingUiEvent) {
        when (event) {
            LoadingUiEvent.OnLoad -> {
                updateState(LoadingUiState.Loading)
                delay(2000) // 시뮬레이션
                updateState(LoadingUiState.Success("로드 완료"))
                launchSideEffect(LoadingSideEffect.ShowMessage("성공!"))
            }
        }
    }
}
```

### 예 2: 장바구니 관리

```kotlin
sealed class CartUiState : UiState {
    data object Empty : CartUiState()
    data class Content(val items: List<CartItem>, val total: Double) : CartUiState()
}

sealed class CartUiEvent : UiEvent {
    data object OnLoad : CartUiEvent()
    data class OnRemoveItem(val itemId: String) : CartUiEvent()
    data object OnCheckout : CartUiEvent()
}

sealed class CartSideEffect : UiSideEffect {
    data object NavigateToCheckout : CartSideEffect()
    data class ShowError(val message: String) : CartSideEffect()
}

class CartViewModel(
    private val repository: CartRepository
) : BaseViewModel<CartUiState, CartUiEvent, CartSideEffect>(
    initialState = CartUiState.Empty
) {
    override suspend fun handleEvent(event: CartUiEvent) {
        when (event) {
            CartUiEvent.OnLoad -> loadCart()
            is CartUiEvent.OnRemoveItem -> removeItem(event.itemId)
            CartUiEvent.OnCheckout -> checkout()
        }
    }

    private suspend fun loadCart() {
        val items = repository.getCartItems()
        if (items.isEmpty()) {
            updateState(CartUiState.Empty)
        } else {
            val total = items.sumOf { it.price * it.quantity }
            updateState(CartUiState.Content(items, total))
        }
    }

    private suspend fun removeItem(itemId: String) {
        repository.removeItem(itemId)
        loadCart()
        launchSideEffect(CartSideEffect.ShowError("항목이 제거되었습니다"))
    }

    private suspend fun checkout() {
        try {
            repository.checkout()
            launchSideEffect(CartSideEffect.NavigateToCheckout)
        } catch (e: Exception) {
            launchSideEffect(CartSideEffect.ShowError(e.message ?: "결제 실패"))
        }
    }
}
```

---

## 테스트

### 단위 테스트 작성

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {

    private lateinit var viewModel: MyViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MyViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStateUpdate() = runTest(testDispatcher) {
        viewModel.uiState.test {
            // 초기 상태 확인
            assertEquals(MyUiState.Initial, awaitItem())

            // 이벤트 제출
            viewModel.submitEvent(MyUiEvent.OnLoad)

            // 상태 변경 확인
            assertEquals(MyUiState.Loading, awaitItem())
            assertEquals(MyUiState.Success("data"), awaitItem())

            cancel()
        }
    }

    @Test
    fun testSideEffect() = runTest(testDispatcher) {
        viewModel.sideEffect.test {
            viewModel.submitEvent(MyUiEvent.OnAction)

            val effect = awaitItem()
            assertTrue(effect is MySideEffect.ShowToast)

            cancel()
        }
    }
}
```

**테스트 라이브러리**:
- **JUnit 4**: 기본 단위 테스트
- **Turbine**: Flow/StateFlow 테스트
- **Coroutines Test**: Coroutine 테스트

---

## 문제 해결

### Q1: State가 업데이트되지 않음

**증상**: `updateState()`를 호출했지만 UI가 반응하지 않음

**원인**:
- `updateState()`를 suspend 함수가 아닌 곳에서 호출
- `handleEvent()` 내에서 호출하지 않음

**해결**:
```kotlin
override suspend fun handleEvent(event: MyUiEvent) {
    // ✅ 올바름: handleEvent는 suspend 함수
    updateState(MyUiState.Loading)
}

// ❌ 틀림: init 블록에서 직접 호출
init {
    updateState(MyUiState.Initial) // 컴파일 에러
}
```

### Q2: SideEffect가 여러 번 발생

**증상**: `launchSideEffect()`로 발생시킨 이펙트가 여러 번 전달됨

**원인**:
- StateFlow 대신 Channel을 사용하므로 정상 동작
- 하지만 새 Collector가 추가되면 이전 이펙트를 못 받음

**해결**:
```kotlin
// UI에서 sideEffect 수집 시
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            is MySideEffect.ShowToast -> showToast(effect.message)
            // repeatOnLifecycle 또는 lifecycleScope 사용 권장
        }
    }
}
```

### Q3: ViewModel 초기화 시 이벤트 발생

**증상**: ViewModel 생성 시 자동으로 초기 이벤트가 처리됨

**원인**: init 블록에서 이벤트 처리 루프가 시작됨

**해결**:
```kotlin
class MyViewModel : BaseViewModel<...>(...) {
    init {
        // 초기 이벤트 자동 발생 가능
        // 원하지 않으면 하위클래스에서 제어
    }

    // 필요시 명시적으로
    fun loadData() {
        viewModelScope.launch {
            submitEvent(MyUiEvent.OnLoad)
        }
    }
}

// UI에서
LaunchedEffect(Unit) {
    viewModel.loadData()
}
```

### Q4: Memory Leak 의심

**증상**: ViewModel이 소멸하지 않거나 메모리 누수 발생

**원인**: StateFlow 구독이 해제되지 않음

**해결**:
```kotlin
// ✅ 올바른 구독 방법
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val state by viewModel.uiState.collectAsState()

    // 자동으로 생명주기 관리됨
}

// ❌ 틀린 방법: Fragment에서 수동 구독
viewModel.uiState.collect { state ->
    // 메모리 누수 위험
}

// ✅ Fragment에서 올바른 방법
repeatOnLifecycle(Lifecycle.State.STARTED) {
    viewModel.uiState.collect { state ->
        // 자동 정리됨
    }
}
```

---

## 참고 자료

### 공식 문서
- [Android ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Jetpack Compose State Management](https://developer.android.com/jetpack/compose/state)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### 관련 SPEC
- **SPEC-ANDROID-MVI-002**: MVI 아키텍처 (이 모듈의 기준 SPEC)
- **SPEC-ANDROID-INIT-001**: 프로젝트 초기 설정
- **SPEC-ANDROID-FEATURE-HOME-001**: Home Feature (이 모듈 활용 예시)

### 샘플 코드
- Home Feature ViewModel (SPEC-ANDROID-FEATURE-HOME-001)
- Cart Feature ViewModel (SPEC-ANDROID-FEATURE-CART-001)
- Detail Feature ViewModel (SPEC-ANDROID-FEATURE-DETAIL-001)

---

## 최종 업데이트

**문서 버전**: 1.0.0
**최종 수정**: 2025-11-29T17:12:12Z
**상태**: PUBLISHED
**유지관리자**: Albert (@user)
**관련 이슈**: SPEC-ANDROID-MVI-002
