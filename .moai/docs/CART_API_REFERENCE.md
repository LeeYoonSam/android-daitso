# Cart Feature - API Reference

**Shopping Cart 기능의 공개 인터페이스 및 API 문서**

**SPEC**: SPEC-ANDROID-REFACTOR-001
**최종 업데이트**: 2025-12-17

---

## 목차

1. [CartContract](#cartcontract)
2. [CartViewModel](#cartviewmodel)
3. [CartRepository](#cartrepository)
4. [UI Components](#ui-components)
5. [Navigation](#navigation)
6. [Event 용어 변경 (Intent → Event)](#event-용어-변경-intent--event)

---

## CartContract

상태, Event, SideEffect를 정의하는 Contract 인터페이스입니다.

### CartUiState

**현재 화면의 UI 상태를 나타냅니다.**

```kotlin
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState
```

**필드 설명:**

| 필드 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `items` | `List<CartItem>` | `emptyList()` | 장바구니에 있는 상품 목록 |
| `totalPrice` | `Double` | `0.0` | 모든 상품의 총 가격 |
| `isLoading` | `Boolean` | `false` | 데이터 로드 중 여부 |
| `error` | `String?` | `null` | 에러 메시지 (발생 시) |

**사용 예시:**

```kotlin
val state by viewModel.state.collectAsState()

when {
    state.isLoading -> {
        LoadingUI()
    }
    state.items.isNotEmpty() -> {
        CartList(
            items = state.items,
            totalPrice = state.totalPrice,
            onItemQuantityChange = { productId, quantity ->
                viewModel.updateQuantity(productId, quantity)
            },
            onRemoveItem = { productId ->
                viewModel.removeItem(productId)
            }
        )
    }
    state.error != null -> {
        ErrorUI(message = state.error)
    }
    else -> {
        EmptyCartUI()
    }
}
```

### CartItem (UI 데이터 모델)

**장바구니의 상품 항목을 나타냅니다.**

```kotlin
data class CartItem(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)
```

**필드 설명:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `productId` | `String` | 상품의 고유 식별자 |
| `name` | `String` | 상품 이름 |
| `price` | `Double` | 상품의 단가 |
| `quantity` | `Int` | 장바구니에 있는 수량 |
| `imageUrl` | `String` | 상품 이미지 URL |

### CartEvent (sealed interface)

**사용자의 인터랙션을 나타냅니다.**

#### LoadCartItems

```kotlin
object LoadCartItems : CartEvent
```

장바구니 아이템 로드 요청입니다.

**사용 예시:**

```kotlin
LaunchedEffect(Unit) {
    viewModel.onEvent(CartEvent.LoadCartItems)
}
```

---

#### UpdateQuantity

```kotlin
data class UpdateQuantity(
    val productId: String,
    val quantity: Int
) : CartEvent
```

특정 상품의 수량을 업데이트합니다.

**파라미터:**
- `productId` (String): 업데이트할 상품의 ID
- `quantity` (Int): 변경할 수량 (1-999)

**사용 예시:**

```kotlin
// 사용자가 상품 "product-001"의 수량을 5로 변경
viewModel.onEvent(
    CartEvent.UpdateQuantity(
        productId = "product-001",
        quantity = 5
    )
)
```

**검증:**
- 수량은 1 이상 999 이하여야 함
- 유효하지 않은 수량은 자동으로 clamped됨

---

#### RemoveItem

```kotlin
data class RemoveItem(val productId: String) : CartEvent
```

장바구니에서 특정 상품을 제거합니다.

**파라미터:**
- `productId` (String): 제거할 상품의 ID

**사용 예시:**

```kotlin
// "product-001" 상품 제거
viewModel.onEvent(CartEvent.RemoveItem(productId = "product-001"))
```

**예외:**
- 상품이 장바구니에 없으면 Exception 발생 가능

---

#### ClearCart

```kotlin
object ClearCart : CartEvent
```

장바구니의 모든 아이템을 제거합니다.

**사용 예시:**

```kotlin
// 확인 후 장바구니 비우기
showConfirmationDialog(
    message = "정말 장바구니를 비우시겠습니까?",
    onConfirm = {
        viewModel.onEvent(CartEvent.ClearCart)
    }
)
```

---

#### DismissError

```kotlin
object DismissError : CartEvent
```

현재 표시되고 있는 에러 메시지를 닫습니다.

**사용 예시:**

```kotlin
Button(onClick = {
    viewModel.onEvent(CartEvent.DismissError)
}) {
    Text("닫기")
}
```

---

### CartSideEffect (sealed interface)

**사용자에게 보여줄 부가 효과입니다.**

```kotlin
sealed interface CartSideEffect : UiSideEffect {
    /**
     * 장바구니가 비워졌을 때 홈 화면으로 이동
     */
    object NavigateToHome : CartSideEffect

    /**
     * 결제 화면으로 이동
     */
    object NavigateToCheckout : CartSideEffect

    /**
     * 토스트 메시지 표시
     */
    data class ShowToast(val message: String) : CartSideEffect
}
```

---

## CartViewModel

`CartEvent`를 처리하고 `CartUiState`를 관리하는 ViewModel입니다.

### 선언

```kotlin
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CartUiState>(CartUiState())
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CartSideEffect>()
    val sideEffect: SharedFlow<CartSideEffect> = _sideEffect.asSharedFlow()
}
```

### 주요 메서드

#### onEvent(event: CartEvent)

```kotlin
fun onEvent(event: CartEvent) {
    when (event) {
        is CartEvent.LoadCartItems -> loadCartItems()
        is CartEvent.UpdateQuantity -> updateQuantity(event.productId, event.quantity)
        is CartEvent.RemoveItem -> removeItem(event.productId)
        is CartEvent.ClearCart -> clearCart()
        is CartEvent.DismissError -> dismissError()
    }
}
```

Event를 처리하고 상태를 업데이트합니다.

**사용 예시:**

```kotlin
@Composable
fun CartScreen(viewModel: CartViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(CartEvent.LoadCartItems)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CartSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    CartContent(
        state = state,
        onUpdateQuantity = { productId, quantity ->
            viewModel.onEvent(CartEvent.UpdateQuantity(productId, quantity))
        },
        onRemoveItem = { productId ->
            viewModel.onEvent(CartEvent.RemoveItem(productId))
        }
    )
}
```

#### loadCartItems()

```kotlin
private fun loadCartItems() {
    viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)

        cartRepository.getCartItems()
            .collect { items ->
                val totalPrice = items.sumOf { it.price * it.quantity }
                _state.value = CartUiState(
                    items = items.map { it.toUICartItem() },
                    totalPrice = totalPrice,
                    isLoading = false
                )
            }
    }
}
```

장바구니 아이템을 로드합니다.

---

#### updateQuantity(productId: String, quantity: Int)

```kotlin
private fun updateQuantity(productId: String, quantity: Int) {
    viewModelScope.launch {
        try {
            cartRepository.updateQuantity(productId, quantity)
            _sideEffect.emit(CartSideEffect.ShowToast("수량이 업데이트되었습니다"))
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message)
        }
    }
}
```

상품 수량을 업데이트합니다.

---

#### removeItem(productId: String)

```kotlin
private fun removeItem(productId: String) {
    viewModelScope.launch {
        try {
            cartRepository.removeItem(productId)
            _sideEffect.emit(CartSideEffect.ShowToast("상품이 제거되었습니다"))
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message)
        }
    }
}
```

장바구니에서 상품을 제거합니다.

---

#### clearCart()

```kotlin
private fun clearCart() {
    viewModelScope.launch {
        try {
            cartRepository.clearCart()
            _state.value = CartUiState()
            _sideEffect.emit(CartSideEffect.NavigateToHome)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message)
        }
    }
}
```

장바구니를 비웁니다.

---

## CartRepository

자세한 CartRepository 인터페이스 및 구현은 [CORE_DATA_README.md - CartRepository 통합](./modules/CORE_DATA_README.md#cartrepository-통합)을 참조하세요.

### 핵심 메서드

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

---

## UI Components

### CartScreen

메인 장바구니 화면 Composable입니다.

```kotlin
@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onNavigateToDetail: (productId: String) -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping Cart",
            style = MaterialTheme.typography.headlineMedium
        )

        when {
            state.isLoading -> {
                LoadingIndicator()
            }
            state.items.isEmpty() -> {
                EmptyCartContent(
                    onContinueShopping = {
                        // Navigate to home or product list
                    }
                )
            }
            else -> {
                CartItems(
                    items = state.items,
                    onQuantityChange = { productId, quantity ->
                        viewModel.onEvent(CartEvent.UpdateQuantity(productId, quantity))
                    },
                    onRemoveItem = { productId ->
                        viewModel.onEvent(CartEvent.RemoveItem(productId))
                    }
                )

                CartSummary(
                    totalPrice = state.totalPrice,
                    itemCount = state.items.size,
                    onCheckout = onNavigateToCheckout
                )
            }
        }

        if (state.error != null) {
            ErrorBanner(
                message = state.error!!,
                onDismiss = {
                    viewModel.onEvent(CartEvent.DismissError)
                }
            )
        }
    }
}
```

### CartItems

장바구니 아이템 목록 표시 Composable입니다.

```kotlin
@Composable
fun CartItems(
    items: List<CartItem>,
    onQuantityChange: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = items,
            key = { it.productId }
        ) { item ->
            CartItemRow(
                item = item,
                onQuantityChange = { newQuantity ->
                    onQuantityChange(item.productId, newQuantity)
                },
                onRemove = {
                    onRemoveItem(item.productId)
                }
            )
        }
    }
}
```

### CartItemRow

개별 장바구니 아이템 표시 Composable입니다.

```kotlin
@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "₩${item.price}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Quantity controls
        QuantitySelector(
            quantity = item.quantity,
            onQuantityChange = onQuantityChange,
            minQuantity = 1,
            maxQuantity = 999
        )

        // Remove button
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove item"
            )
        }
    }
}
```

### CartSummary

주문 요약 및 결제 버튼 Composable입니다.

```kotlin
@Composable
fun CartSummary(
    totalPrice: Double,
    itemCount: Int,
    onCheckout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .border(1.dp, Color.Gray)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("총 상품수:")
            Text("$itemCount 개")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("총 금액:", style = MaterialTheme.typography.headlineSmall)
            Text("₩$totalPrice", style = MaterialTheme.typography.headlineSmall)
        }

        Button(
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("결제하기")
        }
    }
}
```

---

## Navigation

### CartNavigation

장바구니 기능의 네비게이션을 정의하는 확장 함수입니다.

```kotlin
fun NavGraphBuilder.cartScreen(
    onNavigateToDetail: (productId: String) -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    composable<CartRoute> {
        CartScreen(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToCheckout = onNavigateToCheckout
        )
    }
}
```

### CartRoute

네비게이션 라우트 정의입니다.

```kotlin
@Serializable
object CartRoute
```

**사용 예시:**

```kotlin
// 앱 네비게이션 그래프
NavHost(
    navController = navController,
    startDestination = HomeRoute
) {
    homeScreen(...)
    cartScreen(...)
    checkoutScreen(...)
}

// 화면 전환
navController.navigate(CartRoute)
```

---

## Event 용어 변경 (Intent → Event)

### 변경 배경

SPEC-ANDROID-REFACTOR-001에 따라 MVI 용어를 표준화했습니다. 모든 Feature 모듈에서 "Intent" 대신 "Event"를 사용합니다.

### 용어 변경 사항

| Before | After | 설명 |
|--------|-------|------|
| `CartIntent` | `CartEvent` | 사용자 인터랙션 표현 |
| `sealed interface CartIntent : UiEvent` | `sealed interface CartEvent : UiEvent` | 인터페이스 정의 |
| `data class LoadCart : CartIntent` | `object LoadCartItems : CartEvent` | Event 클래스 |

### 코드 마이그레이션

#### Before (Intent 사용)

```kotlin
sealed interface CartIntent : UiEvent {
    object LoadCart : CartIntent
    data class UpdateQuantity(val productId: String, val quantity: Int) : CartIntent
    data class RemoveItem(val productId: String) : CartIntent
    object ClearCart : CartIntent
    object DismissError : CartIntent
}

class CartViewModel @Inject constructor(...) : ViewModel() {
    fun handleIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.LoadCart -> loadCartItems()
            is CartIntent.UpdateQuantity -> updateQuantity(intent.productId, intent.quantity)
            // ...
        }
    }
}
```

#### After (Event 사용)

```kotlin
sealed interface CartEvent : UiEvent {
    object LoadCartItems : CartEvent
    data class UpdateQuantity(val productId: String, val quantity: Int) : CartEvent
    data class RemoveItem(val productId: String) : CartEvent
    object ClearCart : CartEvent
    object DismissError : CartEvent
}

class CartViewModel @Inject constructor(...) : ViewModel() {
    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.LoadCartItems -> loadCartItems()
            is CartEvent.UpdateQuantity -> updateQuantity(event.productId, event.quantity)
            // ...
        }
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

- `feature:cart`: CartEvent
- `feature:detail`: ProductDetailEvent
- `feature:home`: HomeEvent (향후 지정)

모든 Feature 모듈에서 일관되게 "Event" 용어를 사용하여 코드 가독성과 유지보수성을 향상시킵니다.

---

## 참고

- [CartRepository 통합 - CORE_DATA_README.md](./modules/CORE_DATA_README.md#cartrepository-통합)
- [SPEC-ANDROID-REFACTOR-001](../specs/SPEC-ANDROID-REFACTOR-001/spec.md)
- [SPEC-ANDROID-MVI-002](../specs/SPEC-ANDROID-MVI-002/spec.md) - MVI 기본 구조
- [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Android ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)

---

**작성자**: doc-syncer agent
**최종 업데이트**: 2025-12-17
**상태**: Complete

