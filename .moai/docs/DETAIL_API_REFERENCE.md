# Detail Module - API Reference

**Product Detail Feature의 공개 인터페이스 및 API 문서**

---

## 목차

1. [ProductDetailContract](#productdetailcontract)
2. [ProductDetailViewModel](#productdetailviewmodel)
3. [CartRepository 통합](#cartrepository-통합)
4. [UI Components](#ui-components)
5. [Navigation](#navigation)
6. [Event 용어 변경 (Intent → Event)](#event-용어-변경-intent--event)

---

## ProductDetailContract

상태, Intent, SideEffect를 정의하는 Contract 인터페이스입니다.

### ProductDetailUiState

**현재 화면의 UI 상태를 나타냅니다.**

```kotlin
data class ProductDetailUiState(
    val product: Product? = null,
    val selectedQuantity: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingToCart: Boolean = false,
    val addToCartSuccess: Boolean = false
)
```

**필드 설명:**

| 필드 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `product` | `Product?` | `null` | 표시할 상품 정보 |
| `selectedQuantity` | `Int` | `1` | 사용자가 선택한 수량 (1~99) |
| `isLoading` | `Boolean` | `false` | 상품 로드 중 여부 |
| `error` | `String?` | `null` | 에러 메시지 (발생 시) |
| `isAddingToCart` | `Boolean` | `false` | 장바구니 추가 중 여부 |
| `addToCartSuccess` | `Boolean` | `false` | 장바구니 추가 성공 여부 |

**사용 예시:**

```kotlin
val state by viewModel.state.collectAsState()

when {
    state.isLoading -> LoadingUI()
    state.product != null -> {
        ProductUI(
            product = state.product,
            quantity = state.selectedQuantity
        )
    }
    state.error != null -> ErrorUI(message = state.error)
}
```

### ProductDetailEvent (sealed interface)

**사용자의 인터랙션을 나타냅니다.**

#### LoadProduct

```kotlin
data class LoadProduct(val productId: String) : ProductDetailEvent
```

상품 정보 로드 요청입니다.

**파라미터:**
- `productId` (String): 로드할 상품의 고유 식별자

**사용 예시:**

```kotlin
viewModel.onEvent(
    ProductDetailEvent.LoadProduct(productId = "PRODUCT-123")
)
```

**결과:**
- `state.isLoading` → `true`
- 로드 성공: `state.product` 업데이트
- 로드 실패: `state.error` 업데이트

#### SetQuantity

```kotlin
data class SetQuantity(val quantity: Int) : ProductDetailEvent
```

상품 수량을 설정합니다. 범위는 1~999입니다.

**파라미터:**
- `quantity` (Int): 설정할 수량 (유효 범위: 1~999)

**유효성 검사:**
- 범위를 벗어나는 값은 자동으로 clamped됩니다.

**사용 예시:**

```kotlin
// 수량 5개로 설정
viewModel.onEvent(ProductDetailEvent.SetQuantity(5))

// 범위 밖: clamped됨
viewModel.onEvent(ProductDetailEvent.SetQuantity(1000))  // 999로 자동 조정
viewModel.onEvent(ProductDetailEvent.SetQuantity(0))     // 1로 자동 조정
```

**결과:**
- `state.selectedQuantity` 업데이트

#### AddToCart

```kotlin
object AddToCart : ProductDetailEvent
```

현재 선택된 수량으로 장바구니에 상품을 추가합니다.

**사용 예시:**

```kotlin
viewModel.onEvent(ProductDetailEvent.AddToCart)
```

**전제 조건:**
- `state.product`가 null이 아니어야 함
- `state.selectedQuantity` >= 1

**결과:**
- `state.isAddingToCart` → `true` (진행 중)
- 성공: `state.addToCartSuccess` → `true`, SideEffect 발생
- 실패: `state.error` 업데이트

**Side Effect:**

```kotlin
ProductDetailSideEffect.ShowSnackbar("장바구니에 추가되었습니다")
```

#### DismissError

```kotlin
object DismissError : ProductDetailEvent
```

표시된 에러 메시지를 닫습니다.

**사용 예시:**

```kotlin
viewModel.onEvent(ProductDetailEvent.DismissError)
```

**결과:**
- `state.error` → `null`

#### DismissSuccess

```kotlin
object DismissSuccess : ProductDetailEvent
```

성공 메시지를 닫습니다.

**사용 예시:**

```kotlin
viewModel.onEvent(ProductDetailEvent.DismissSuccess)
```

**결과:**
- `state.addToCartSuccess` → `false`

---

## ProductDetailEvent - 전체 목록

```kotlin
sealed interface ProductDetailEvent {
    data class LoadProduct(val productId: String) : ProductDetailEvent
    data class SetQuantity(val quantity: Int) : ProductDetailEvent
    object AddToCart : ProductDetailEvent
    object DismissError : ProductDetailEvent
    object DismissSuccess : ProductDetailEvent
}
```

---

## ProductDetailSideEffect (sealed interface)

**일회성 이벤트를 나타냅니다.**

#### NavigateBack

```kotlin
object NavigateBack : ProductDetailSideEffect
```

이전 화면(Home)으로 돌아갑니다.

**발생 시기:**
- 뒤로가기 버튼 클릭
- 에러 상황에서 자동 복귀

#### ShowSnackbar

```kotlin
data class ShowSnackbar(val message: String) : ProductDetailSideEffect
```

스낵바를 표시합니다.

**파라미터:**
- `message` (String): 표시할 메시지

**발생 예시:**
- "장바구니에 추가되었습니다"
- "상품 로드 실패"
- "장바구니 추가 실패"

**사용 예시:**

```kotlin
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            is ProductDetailSideEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            else -> {}
        }
    }
}
```

#### NavigateToCart

```kotlin
object NavigateToCart : ProductDetailSideEffect
```

장바구니 화면으로 이동합니다.

**발생 시기:**
- 사용자가 "장바구니로 이동" 버튼 클릭

---

## ProductDetailSideEffect - 전체 목록

```kotlin
sealed interface ProductDetailSideEffect {
    object NavigateBack : ProductDetailSideEffect
    data class ShowSnackbar(val message: String) : ProductDetailSideEffect
    object NavigateToCart : ProductDetailSideEffect
}
```

---

## ProductDetailViewModel

**비즈니스 로직 및 상태 관리를 담당합니다.**

### 클래스 정의

```kotlin
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<ProductDetailSideEffect>()
    val sideEffect: SharedFlow<ProductDetailSideEffect> = _sideEffect.asSharedFlow()
}
```

### 공개 프로퍼티

#### state: StateFlow<ProductDetailUiState>

현재 UI 상태입니다. 관찰 가능하며 상태 변경 시 자동으로 업데이트됩니다.

**타입:** `StateFlow<ProductDetailUiState>`

**사용 예시:**

```kotlin
val state by viewModel.state.collectAsState()

Text(text = "${state.selectedQuantity}개")
```

#### sideEffect: SharedFlow<ProductDetailSideEffect>

일회성 이벤트입니다.

**타입:** `SharedFlow<ProductDetailSideEffect>`

**사용 예시:**

```kotlin
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            ProductDetailSideEffect.NavigateBack -> onNavigateBack()
            is ProductDetailSideEffect.ShowSnackbar -> showSnackbar(effect.message)
            ProductDetailSideEffect.NavigateToCart -> onNavigateToCart()
        }
    }
}
```

### 공개 메서드

#### onEvent(event: ProductDetailEvent)

```kotlin
fun onEvent(event: ProductDetailEvent)
```

사용자 인터랙션을 처리합니다.

**파라미터:**
- `event` (ProductDetailEvent): 처리할 Event

**사용 예시:**

```kotlin
// 상품 로드
viewModel.onEvent(ProductDetailEvent.LoadProduct("PRODUCT-123"))

// 수량 설정
viewModel.onEvent(ProductDetailEvent.SetQuantity(5))

// 장바구니 추가
viewModel.onEvent(ProductDetailEvent.AddToCart)
```

---

## CartRepository 통합

ProductDetailViewModel은 장바구니 기능을 위해 `:core:data` 모듈의 통합 CartRepository를 사용합니다.

자세한 내용은 [CORE_DATA_README.md - CartRepository 통합](./modules/CORE_DATA_README.md#cartrepository-통합)을 참조하세요.

### 주요 메서드

```kotlin
interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun updateQuantity(productId: String, quantity: Int)
    suspend fun removeItem(productId: String)
    suspend fun clearCart()
    suspend fun addToCart(product: Product, quantity: Int): Boolean
    suspend fun getProductDetails(productId: String): Product
}
```

### ProductDetailViewModel에서의 사용

```kotlin
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    // 장바구니에 상품 추가
    private fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            try {
                val success = cartRepository.addToCart(product, quantity)
                if (success) {
                    _sideEffect.emit(ProductDetailSideEffect.ShowSnackbar("장바구니에 추가되었습니다"))
                } else {
                    _state.value = _state.value.copy(error = "Invalid quantity")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
```

---

## ProductRepository

```kotlin
interface ProductRepository {
    fun getProduct(id: String): Flow<Result<Product>>
    fun getProducts(): Flow<Result<List<Product>>>
}
```

#### getProduct(id: String): Flow<Result<Product>>

```kotlin
suspend fun getProduct(id: String): Flow<Result<Product>>
```

특정 상품의 상세 정보를 조회합니다.

**파라미터:**
- `id` (String): 상품 고유 식별자

**반환값:**
- `Flow<Result<Product>>`: 상품 정보 Flow

**동작 흐름:**
1. `Result.Loading` 방출
2. 로컬 캐시에서 조회 시도
3. 네트워크에서 동기화
4. `Result.Success<Product>` 또는 `Result.Error` 방출

**사용 예시:**

```kotlin
productRepository.getProduct("PRODUCT-123")
    .collect { result ->
        when (result) {
            is Result.Loading -> showLoadingUI()
            is Result.Success -> displayProduct(result.data)
            is Result.Error -> showErrorUI(result.exception)
        }
    }
```

### CartRepository

```kotlin
interface CartRepository {
    fun addToCart(product: Product, quantity: Int): Flow<Result<Unit>>
    fun getCartItems(): Flow<Result<List<CartItem>>>
    fun removeFromCart(productId: String): Flow<Result<Unit>>
    fun clearCart(): Flow<Result<Unit>>
}
```

#### addToCart(product: Product, quantity: Int): Flow<Result<Unit>>

```kotlin
fun addToCart(product: Product, quantity: Int): Flow<Result<Unit>>
```

상품을 장바구니에 추가합니다.

**파라미터:**
- `product` (Product): 추가할 상품
- `quantity` (Int): 추가할 수량 (1 이상)

**반환값:**
- `Flow<Result<Unit>>`: 작업 결과 Flow

**동작:**
1. 입력값 검증
2. CartItem 생성
3. 로컬 데이터베이스에 저장
4. `Result.Success` 또는 `Result.Error` 방출

**사용 예시:**

```kotlin
val product = Product("1", "상품명", "설명", 29999.0, "url", stock = 50)

cartRepository.addToCart(product, quantity = 3)
    .collect { result ->
        when (result) {
            is Result.Success -> {
                showSnackbar("장바구니에 추가되었습니다")
            }
            is Result.Error -> {
                showErrorSnackbar("장바구니 추가 실패")
            }
            else -> {}
        }
    }
```

**예외 처리:**
- `IllegalArgumentException`: 수량이 0 이하인 경우
- `SQLiteException`: 데이터베이스 저장 실패
- `Exception`: 예상치 못한 에러

---

## UI Components

### ProductDetailScreen

```kotlin
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
)
```

상품 상세 화면을 표시합니다.

**파라미터:**
- `viewModel` (ProductDetailViewModel): 상태 관리 ViewModel
- `onNavigateBack` (() -> Unit): 뒤로가기 콜백
- `onNavigateToCart` (() -> Unit): 장바구니로 이동 콜백
- `modifier` (Modifier): 컴포저블 Modifier

**구성 요소:**
- 뒤로가기 버튼
- 상품 이미지 및 정보
- 수량 선택기
- 장바구니 추가 버튼
- 에러 및 로딩 상태 UI

**사용 예시:**

```kotlin
ProductDetailScreen(
    viewModel = viewModel,
    onNavigateBack = { navController.popBackStack() },
    onNavigateToCart = { navController.navigate("cart") }
)
```

### QuantitySelector

```kotlin
@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxQuantity: Int = 999
)
```

수량을 선택할 수 있는 컴포넌트입니다.

**파라미터:**
- `quantity` (Int): 현재 선택된 수량
- `onQuantityChange` ((Int) -> Unit): 수량 변경 콜백
- `modifier` (Modifier): 컴포저블 Modifier
- `maxQuantity` (Int): 최대 수량 (기본값: 99)

**UI:**
- [−] 버튼: 수량 감소 (최소값 1)
- 수량 표시: 현재 선택된 수량
- [+] 버튼: 수량 증가 (최대값까지)

**사용 예시:**

```kotlin
QuantitySelector(
    quantity = 5,
    onQuantityChange = { newQuantity ->
        viewModel.processIntent(
            ProductDetailIntent.SetQuantity(newQuantity)
        )
    },
    maxQuantity = 99
)
```

### AddToCartButton

```kotlin
@Composable
fun AddToCartButton(
    isLoading: Boolean,
    isOutOfStock: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "장바구니 추가"
)
```

장바구니 추가 버튼입니다.

**파라미터:**
- `isLoading` (Boolean): 로딩 상태 여부
- `isOutOfStock` (Boolean): 품절 여부
- `onClick` (() -> Unit): 클릭 콜백
- `modifier` (Modifier): 컴포저블 Modifier
- `text` (String): 버튼 텍스트 (기본값: "장바구니 추가")

**상태:**
- **정상**: 파란색 배경, 클릭 가능
- **로딩**: 진행 표시기 표시, 클릭 불가
- **품절**: 회색 배경, "품절" 텍스트, 클릭 불가

**사용 예시:**

```kotlin
AddToCartButton(
    isLoading = state.isAddingToCart,
    isOutOfStock = state.product?.stock ?: 0 <= 0,
    onClick = { viewModel.processIntent(ProductDetailIntent.AddToCart) },
    modifier = Modifier.fillMaxWidth()
)
```

### ProductInfoSection

```kotlin
@Composable
fun ProductInfoSection(
    product: Product,
    modifier: Modifier = Modifier
)
```

상품 정보를 표시합니다.

**파라미터:**
- `product` (Product): 표시할 상품
- `modifier` (Modifier): 컴포저블 Modifier

**표시 내용:**
- 상품 이미지
- 상품명
- 상품 설명
- 가격
- 재고 상태

**사용 예시:**

```kotlin
ProductInfoSection(
    product = Product(
        id = "1",
        name = "멋진 상품",
        description = "상품 설명",
        price = 29999.0,
        imageUrl = "https://example.com/image.jpg",
        stock = 50
    )
)
```

---

## Navigation

### 네비게이션 경로

```kotlin
const val PRODUCT_DETAIL_ROUTE = "productDetail/{productId}"
```

### NavHost 설정

```kotlin
navHost(
    navController = navController,
    startDestination = "home"
) {
    composable("home") {
        HomeScreen(
            onProductClick = { productId ->
                navController.navigate("productDetail/$productId")
            }
        )
    }

    composable(
        route = PRODUCT_DETAIL_ROUTE,
        arguments = listOf(
            navArgument("productId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val productId = backStackEntry.arguments?.getString("productId") ?: return@composable

        ProductDetailScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCart = { navController.navigate("cart") }
        )
    }

    composable("cart") {
        CartScreen(...)
    }
}
```

### 딥링크

```kotlin
composable(
    route = PRODUCT_DETAIL_ROUTE,
    deepLinks = listOf(
        navDeepLink { uriPattern = "daitso://productDetail/{productId}" }
    )
)
```

---

## 데이터 모델

### Product

```kotlin
@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val stock: Int = 0
)
```

### CartItem

```kotlin
@Serializable
data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String
)
```

---

## 에러 처리

### Result 타입

```kotlin
sealed class Result<out T> {
    class Loading<T> : Result<T>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Throwable) : Result<T>()
}
```

### 에러 시나리오

| 시나리오 | 상태 업데이트 | SideEffect |
|---------|-----------|-----------|
| 상품 로드 실패 | `error` 설정 | `ShowSnackbar("상품 로드 실패")` |
| 장바구니 추가 실패 | `error` 설정 | `ShowSnackbar("장바구니 추가 실패")` |
| 네트워크 오류 | `error` 설정 | `ShowSnackbar("네트워크 오류")` |

---

## Event 용어 변경 (Intent → Event)

### 변경 배경

SPEC-ANDROID-REFACTOR-001에 따라 MVI 용어를 표준화했습니다. 모든 Feature 모듈에서 "Intent" 대신 "Event"를 사용합니다.

### 용어 변경 사항

| Before | After | 설명 |
|--------|-------|------|
| `ProductDetailIntent` | `ProductDetailEvent` | 사용자 인터랙션 표현 |
| `sealed interface ProductDetailIntent : UiEvent` | `sealed interface ProductDetailEvent : UiEvent` | 인터페이스 정의 |
| `processIntent(intent)` | `onEvent(event)` | ViewModel 메서드명 |

### 코드 마이그레이션

#### Before (Intent 사용)

```kotlin
sealed interface ProductDetailIntent : UiEvent {
    data class LoadProduct(val productId: String) : ProductDetailIntent
    data class SetQuantity(val quantity: Int) : ProductDetailIntent
    object AddToCart : ProductDetailIntent
    object DismissError : ProductDetailIntent
    object DismissSuccess : ProductDetailIntent
}

class ProductDetailViewModel @Inject constructor(...) : ViewModel() {
    fun processIntent(intent: ProductDetailIntent) {
        when (intent) {
            is ProductDetailIntent.LoadProduct -> loadProduct(intent.productId)
            is ProductDetailIntent.SetQuantity -> setQuantity(intent.quantity)
            is ProductDetailIntent.AddToCart -> addToCart()
            // ...
        }
    }
}
```

#### After (Event 사용)

```kotlin
sealed interface ProductDetailEvent : UiEvent {
    data class LoadProduct(val productId: String) : ProductDetailEvent
    data class SetQuantity(val quantity: Int) : ProductDetailEvent
    object AddToCart : ProductDetailEvent
    object DismissError : ProductDetailEvent
    object DismissSuccess : ProductDetailEvent
}

class ProductDetailViewModel @Inject constructor(...) : ViewModel() {
    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            is ProductDetailEvent.LoadProduct -> loadProduct(event.productId)
            is ProductDetailEvent.SetQuantity -> setQuantity(event.quantity)
            is ProductDetailEvent.AddToCart -> addToCart()
            // ...
        }
    }
}
```

### UI에서의 변경

#### Before

```kotlin
@Composable
fun ProductDetailScreen(viewModel: ProductDetailViewModel) {
    // ...
    Button(onClick = {
        viewModel.processIntent(ProductDetailIntent.AddToCart)
    }) {
        Text("장바구니 추가")
    }
}
```

#### After

```kotlin
@Composable
fun ProductDetailScreen(viewModel: ProductDetailViewModel) {
    // ...
    Button(onClick = {
        viewModel.onEvent(ProductDetailEvent.AddToCart)
    }) {
        Text("장바구니 추가")
    }
}
```

### 표준화된 MVI 용어 정의

| 용어 | 설명 | 구현 위치 |
|------|------|----------|
| **Event** | 사용자의 인터랙션 및 시스템 이벤트 | `{Feature}Contract.kt` |
| **State** | UI가 렌더링하기 위한 상태 데이터 | `{Feature}Contract.kt` |
| **SideEffect** | UI 업데이트 외의 부가 효과 (네비게이션, 토스트 등) | `{Feature}Contract.kt` |
| **ViewModel** | Event를 처리하고 State를 관리 | `{Feature}ViewModel.kt` |

### 전체 프로젝트 Event 용어 통일

- `feature:detail`: ProductDetailEvent ✅ (완료)
- `feature:cart`: CartEvent ✅ (완료)
- `feature:home`: HomeEvent (향후 지정)

모든 Feature 모듈에서 일관되게 "Event" 용어를 사용하여 코드 가독성과 유지보수성을 향상시킵니다.

---

## 참고

- [CartRepository 통합 - CORE_DATA_README.md](./modules/CORE_DATA_README.md#cartrepository-통합)
- [CART_API_REFERENCE.md](./CART_API_REFERENCE.md)
- [SPEC-ANDROID-REFACTOR-001](../specs/SPEC-ANDROID-REFACTOR-001/spec.md)
- [SPEC-ANDROID-MVI-002](../specs/SPEC-ANDROID-MVI-002/spec.md) - MVI 기본 구조
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Navigation](https://developer.android.com/guide/navigation)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)

---

**문서 작성**: 2025-12-13
**최종 업데이트**: 2025-12-17
**SPEC 기반**: SPEC-ANDROID-REFACTOR-001
**API 버전**: 1.1.0
