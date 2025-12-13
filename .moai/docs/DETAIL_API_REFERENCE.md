# Detail Module - API Reference

**Product Detail Feature의 공개 인터페이스 및 API 문서**

---

## 목차

1. [ProductDetailContract](#productdetailcontract)
2. [ProductDetailViewModel](#productdetailviewmodel)
3. [Repository Interface](#repository-interface)
4. [UI Components](#ui-components)
5. [Navigation](#navigation)

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

### ProductDetailIntent (sealed interface)

**사용자의 인터랙션을 나타냅니다.**

#### LoadProduct

```kotlin
data class LoadProduct(val productId: String) : ProductDetailIntent
```

상품 정보 로드 요청입니다.

**파라미터:**
- `productId` (String): 로드할 상품의 고유 식별자

**사용 예시:**

```kotlin
viewModel.processIntent(
    ProductDetailIntent.LoadProduct(productId = "PRODUCT-123")
)
```

**결과:**
- `state.isLoading` → `true`
- 로드 성공: `state.product` 업데이트
- 로드 실패: `state.error` 업데이트

#### SetQuantity

```kotlin
data class SetQuantity(val quantity: Int) : ProductDetailIntent
```

상품 수량을 설정합니다. 범위는 1~99입니다.

**파라미터:**
- `quantity` (Int): 설정할 수량 (유효 범위: 1~99)

**유효성 검사:**
- 범위를 벗어나는 값은 무시됩니다.

**사용 예시:**

```kotlin
// 수량 5개로 설정
viewModel.processIntent(ProductDetailIntent.SetQuantity(5))

// 범위 밖: 무시됨
viewModel.processIntent(ProductDetailIntent.SetQuantity(100))  // 무시
viewModel.processIntent(ProductDetailIntent.SetQuantity(0))    // 무시
```

**결과:**
- `state.selectedQuantity` 업데이트

#### AddToCart

```kotlin
object AddToCart : ProductDetailIntent
```

현재 선택된 수량으로 장바구니에 상품을 추가합니다.

**사용 예시:**

```kotlin
viewModel.processIntent(ProductDetailIntent.AddToCart)
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
object DismissError : ProductDetailIntent
```

표시된 에러 메시지를 닫습니다.

**사용 예시:**

```kotlin
viewModel.processIntent(ProductDetailIntent.DismissError)
```

**결과:**
- `state.error` → `null`

#### DismissSuccess

```kotlin
object DismissSuccess : ProductDetailIntent
```

성공 메시지를 닫습니다.

**사용 예시:**

```kotlin
viewModel.processIntent(ProductDetailIntent.DismissSuccess)
```

**결과:**
- `state.addToCartSuccess` → `false`

---

## ProductDetailIntent - 전체 목록

```kotlin
sealed interface ProductDetailIntent {
    data class LoadProduct(val productId: String) : ProductDetailIntent
    data class SetQuantity(val quantity: Int) : ProductDetailIntent
    object AddToCart : ProductDetailIntent
    object DismissError : ProductDetailIntent
    object DismissSuccess : ProductDetailIntent
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
) : BaseViewModel<
    ProductDetailUiState,
    ProductDetailIntent,
    ProductDetailSideEffect
>(
    initialState = ProductDetailUiState()
)
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

#### processIntent(intent: ProductDetailIntent)

```kotlin
fun processIntent(intent: ProductDetailIntent)
```

사용자 인터랙션을 처리합니다.

**파라미터:**
- `intent` (ProductDetailIntent): 처리할 Intent

**사용 예시:**

```kotlin
// 상품 로드
viewModel.processIntent(ProductDetailIntent.LoadProduct("PRODUCT-123"))

// 수량 설정
viewModel.processIntent(ProductDetailIntent.SetQuantity(5))

// 장바구니 추가
viewModel.processIntent(ProductDetailIntent.AddToCart)
```

---

## Repository Interface

### ProductRepository

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

## 참고

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Navigation](https://developer.android.com/guide/navigation)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)

---

**문서 작성**: 2025-12-13
**SPEC 기반**: SPEC-ANDROID-FEATURE-DETAIL-001
**API 버전**: 1.0.0
