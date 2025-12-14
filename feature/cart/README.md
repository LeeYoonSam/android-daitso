# Cart Feature Module

**SPEC**: SPEC-ANDROID-FEATURE-CART-001
**모듈**: `:feature:cart`
**상태**: Completed (2025-12-14)
**테스트 커버리지**: 95%+

---

## 개요

Cart 모듈은 Daitso 애플리케이션에서 사용자의 장바구니를 관리하는 기능을 담당합니다.
Room Database를 로컬 저장소로 사용하여 데이터 지속성을 보장하고, MVI 아키텍처를 적용하여
아이템 조회, 수량 변경, 삭제, 그리고 총 가격 계산 기능을 구현합니다.

### 주요 특징

- **MVI 아키텍처**: 단방향 데이터 흐름 (Intent → ViewModel → State → UI)
- **Room Database 통합**: 로컬 데이터 지속성 보장
- **Flow 기반 반응형 UI**: 데이터 변경 시 자동 UI 업데이트
- **Type-safe Navigation**: Kotlin Serialization 기반 네비게이션
- **Hilt 의존성 주입**: 느슨한 결합 및 테스트 용이성

---

## 주요 기능

1. **아이템 조회**
   - Room DB에서 장바구니 아이템 비동기 로드
   - Flow 기반으로 실시간 데이터 업데이트

2. **수량 관리**
   - 각 아이템의 수량 증가/감소
   - 수량 범위 제한 (1~999)
   - 실시간 총 가격 재계산

3. **아이템 삭제**
   - 개별 아이템 삭제
   - 전체 장바구니 비우기

4. **가격 계산**
   - 자동 총 가격 계산
   - 원화(₩) 포맷 표시

5. **상태 처리**
   - 로딩 상태
   - 에러 상태
   - 빈 장바구니 상태

---

## 모듈 구조

```
:feature:cart/
├── src/main/kotlin/com/bup/ys/daitso/feature/cart/
│   ├── contract/
│   │   └── CartContract.kt              # MVI 계약 (State, Intent, SideEffect)
│   ├── presentation/
│   │   ├── CartScreen.kt                # 장바구니 UI 컴포넌트
│   │   └── CartViewModel.kt             # 장바구니 상태 관리 ViewModel
│   ├── repository/
│   │   └── CartRepositoryImpl.kt         # Repository 구현
│   ├── domain/
│   │   └── CartRepository.kt            # Repository 인터페이스
│   ├── navigation/
│   │   └── CartNavigation.kt            # Navigation 설정
│   ├── di/
│   │   └── CartModule.kt                # Hilt 의존성 주입 모듈
│   └── util/
│       └── PriceFormatter.kt            # 가격 포맷팅 유틸리티
├── src/test/kotlin/com/bup/ys/daitso/feature/cart/
│   ├── presentation/
│   │   └── CartViewModelTest.kt         # ViewModel 단위 테스트
│   ├── repository/
│   │   └── CartRepositoryTest.kt        # Repository 테스트
│   └── ...
├── build.gradle.kts
└── README.md
```

---

## CartRepository API 문서

### 인터페이스

```kotlin
interface CartRepository {
    /**
     * 장바구니의 모든 아이템을 조회합니다.
     * 데이터 변경 시 자동으로 emit됩니다.
     *
     * @return Flow<List<CartItem>> - 장바구니 아이템 목록 스트림
     */
    fun getCartItems(): Flow<List<CartItem>>

    /**
     * 특정 아이템의 수량을 업데이트합니다.
     * 수량은 자동으로 1-999 범위로 제한됩니다.
     *
     * @param productId 상품 ID
     * @param quantity 새로운 수량
     */
    suspend fun updateQuantity(productId: String, quantity: Int)

    /**
     * 장바구니에서 특정 아이템을 삭제합니다.
     *
     * @param productId 삭제할 상품 ID
     */
    suspend fun removeItem(productId: String)

    /**
     * 장바구니의 모든 아이템을 삭제합니다.
     */
    suspend fun clearCart()
}
```

### 구현 상세

#### getCartItems()

**반환 타입**: `Flow<List<CartItem>>`

**동작**:
1. Room Database에서 모든 CartItemEntity 조회
2. CartItem으로 매핑
3. Flow로 스트림 반환

**예제**:
```kotlin
// ViewModel에서 사용
viewModel.loadCartItems() // Intent 전송
// → handleEvent(CartIntent.LoadCartItems) 호출
// → loadCartItems() 메서드 실행
// → cartRepository.getCartItems().collect { items ->
//     updateState(state.copy(items = items, totalPrice = calculateTotalPrice(items)))
//   }
```

**데이터 흐름**:
```
Room Database
     ↓
CartDao.getAllCartItems() [Flow<List<CartItemEntity>>]
     ↓
Map to CartItem
     ↓
ViewModel.loadCartItems()
     ↓
Update CartUiState
     ↓
CartScreen 리컴포지션
```

#### updateQuantity(productId, quantity)

**파라미터**:
- `productId: String` - 상품 ID
- `quantity: Int` - 새로운 수량

**제약 사항**:
- 수량은 자동으로 1 이상 999 이하로 제한됨
- 동시에 여러 업데이트는 마지막 값으로 덮어씀

**예제**:
```kotlin
// UI에서 +/- 버튼 클릭
viewModel.submitEvent(
    CartIntent.UpdateQuantity(productId = "prod123", quantity = 3)
)
// → ViewModel.handleEvent 호출
// → updateQuantity(productId, quantity) 실행
// → cartRepository.updateQuantity(productId, 3)
// → CartDao.updateCartItem(updatedItem) 실행
// → Toast 메시지 표시: "상품 수량이 업데이트되었습니다"
```

**에러 처리**:
```kotlin
try {
    cartRepository.updateQuantity(productId, quantity)
    launchSideEffect(
        CartSideEffect.ShowToast("상품 수량이 업데이트되었습니다")
    )
} catch (e: Exception) {
    updateState(currentState.copy(error = e.message))
}
```

#### removeItem(productId)

**파라미터**:
- `productId: String` - 삭제할 상품 ID

**동작**:
1. 데이터베이스에서 해당 상품 ID인 아이템 삭제
2. 자동으로 UI 업데이트 (Flow 스트림)
3. Toast 메시지 표시

**예제**:
```kotlin
// UI에서 삭제 버튼 클릭
viewModel.submitEvent(CartIntent.RemoveItem(productId = "prod123"))
// → ViewModel.handleEvent 호출
// → removeItem(productId) 실행
// → cartRepository.removeItem(productId)
// → CartDao.deleteByProductId(productId) 실행
// → Toast 메시지 표시: "상품이 장바구니에서 제거되었습니다"
```

#### clearCart()

**동작**:
1. 데이터베이스의 모든 아이템 삭제
2. 자동으로 UI 업데이트
3. Toast 메시지 표시

**예제**:
```kotlin
// UI에서 "장바구니 비우기" 버튼 클릭
viewModel.submitEvent(CartIntent.ClearCart)
// → ViewModel.handleEvent 호출
// → clearCart() 실행
// → cartRepository.clearCart()
// → CartDao.clearCart() 실행
// → Toast 메시지 표시: "장바구니가 비워졌습니다"
```

---

## CartViewModel 상태 관리 상세

### 상태 정의

```kotlin
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState
```

### Intent (이벤트) 정의

```kotlin
sealed interface CartIntent : UiEvent {
    object LoadCartItems : CartIntent              // 장바구니 아이템 로드
    data class UpdateQuantity(                     // 수량 업데이트
        val productId: String,
        val quantity: Int
    ) : CartIntent
    data class RemoveItem(val productId: String) : CartIntent  // 아이템 삭제
    object ClearCart : CartIntent                 // 장바구니 전체 삭제
    object DismissError : CartIntent              // 에러 메시지 해제
}
```

### SideEffect (부수 효과) 정의

```kotlin
sealed interface CartSideEffect : UiSideEffect {
    object NavigateToCheckout : CartSideEffect    // 결제 화면 이동
    data class ShowToast(val message: String) : CartSideEffect  // 토스트 표시
    object NavigateToHome : CartSideEffect        // 홈 화면 이동
}
```

### 상태 전환 다이어그램

```
[Initial State]
    items = []
    totalPrice = 0.0
    isLoading = false
    error = null
         │
         │ LoadCartItems Intent
         ▼
[Loading State]
    items = []
    totalPrice = 0.0
    isLoading = true
    error = null
         │
         ├─→ Repository에서 데이터 로드
         │
         ▼
[Success State]
    items = [CartItem(...), ...]
    totalPrice = 계산됨
    isLoading = false
    error = null
         │
         ├─→ UpdateQuantity Intent
         │    └─→ 수량 업데이트 후 자동 상태 업데이트
         │
         ├─→ RemoveItem Intent
         │    └─→ 아이템 삭제 후 자동 상태 업데이트
         │
         ├─→ ClearCart Intent
         │    └─→ 전체 삭제 후 자동 상태 업데이트
         │
         └─→ DismissError Intent
              └─→ error = null

[Error State]
    items = [이전 상태 유지]
    totalPrice = [이전 상태 유지]
    isLoading = false
    error = "에러 메시지"
         │
         └─→ DismissError Intent
              └─→ error = null
```

---

## CartScreen UI 구성

### 화면 레이아웃

```
┌─────────────────────────────┐
│     Cart Screen             │
├─────────────────────────────┤
│                             │
│  [Loading Indicator]        │  ← isLoading == true
│                             │
├─────────────────────────────┤
│  장바구니 아이템 목록        │  ← items 표시
│  ┌───────────────────────┐  │
│  │ [이미지] 상품명    ₩10,000 │
│  │          수량: 2 [+] [-]   │
│  │          [삭제]        │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │ [이미지] 상품명    ₩20,000 │
│  │          수량: 1 [+] [-]   │
│  │          [삭제]        │  │
│  └───────────────────────┘  │
│                             │
├─────────────────────────────┤
│  총 가격: ₩30,000           │  ← 자동 계산
├─────────────────────────────┤
│  [결제하기]  [계속 쇼핑하기] │
├─────────────────────────────┤
│  ⚠️ 에러 메시지             │  ← error != null
│  [닫기]                      │
└─────────────────────────────┘
```

### 빈 장바구니 상태

```
┌─────────────────────────────┐
│     Cart Screen             │
├─────────────────────────────┤
│                             │
│            🛒               │  ← Empty Icon
│                             │
│  장바구니가 비어있습니다     │
│                             │
│     [계속 쇼핑하기]          │
│                             │
└─────────────────────────────┘
```

---

## 통합 예제

### 1. Cart 화면을 App에 추가

```kotlin
// AppNavHost.kt
NavHost(navController = navController, startDestination = AppRoute.Home) {
    // ... 다른 화면들 ...

    cartNavigation(navController)  // Cart 네비게이션 추가

    // ... 다른 화면들 ...
}
```

### 2. Home에서 Cart로 네비게이션

```kotlin
// HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController
) {
    // ...

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.NavigateToCart -> {
                    navController.navigate(AppRoute.Cart)
                }
                // ...
            }
        }
    }
}
```

### 3. Home에서 Cart로 상품 추가

```kotlin
// HomeViewModel.kt (다른 모듈에서 CartRepository 사용)
// 참고: Feature 모듈 간 직접 의존성은 피해야 합니다.
// 대신 Repository 인터페이스를 통해 간접적으로 통신합니다.

// 상품을 클릭했을 때 장바구니에 추가
private suspend fun addToCart(product: Product) {
    try {
        cartRepository.addItem(CartItem(
            productId = product.id,
            name = product.name,
            price = product.price,
            quantity = 1,
            imageUrl = product.imageUrl
        ))
        launchSideEffect(
            HomeSideEffect.ShowToast("장바구니에 추가되었습니다")
        )
    } catch (e: Exception) {
        updateState(currentState.copy(error = e.message))
    }
}
```

### 4. ViewModel에서 Cart 상태 관찰

```kotlin
@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val state by viewModel.uiState.collectAsState()

    // SideEffect 처리
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CartSideEffect.NavigateToCheckout -> {
                    navController.navigate(AppRoute.Checkout)
                }
                is CartSideEffect.ShowToast -> {
                    // Toast 표시
                }
                is CartSideEffect.NavigateToHome -> {
                    navController.navigate(AppRoute.Home)
                }
            }
        }
    }

    // 초기 로드
    LaunchedEffect(Unit) {
        viewModel.submitEvent(CartIntent.LoadCartItems)
    }

    // State 렌더링
    when {
        state.isLoading -> LoadingScreen()
        state.items.isEmpty() -> EmptyCartScreen(navController)
        else -> CartContent(
            items = state.items,
            totalPrice = state.totalPrice,
            onQuantityChanged = { productId, quantity ->
                viewModel.submitEvent(
                    CartIntent.UpdateQuantity(productId, quantity)
                )
            },
            onRemoveItem = { productId ->
                viewModel.submitEvent(CartIntent.RemoveItem(productId))
            }
        )
    }

    // Error 처리
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.submitEvent(CartIntent.DismissError)
            },
            title = { Text("오류") },
            text = { Text(state.error!!) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitEvent(CartIntent.DismissError)
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }
}
```

---

## 테스트 전략

### 단위 테스트 (Unit Tests)

#### CartViewModelTest

테스트 케이스:
1. **초기 상태 검증**: ViewModel 생성 시 초기 상태 확인
2. **아이템 로드**: LoadCartItems Intent 처리 및 상태 업데이트 확인
3. **수량 업데이트**: UpdateQuantity Intent 처리 확인
4. **아이템 삭제**: RemoveItem Intent 처리 확인
5. **전체 삭제**: ClearCart Intent 처리 확인
6. **에러 처리**: 예외 발생 시 error 상태 업데이트 확인
7. **SideEffect**: Toast 및 Navigation SideEffect 발생 확인

```kotlin
@Test
fun testLoadCartItems() = runTest {
    // Arrange
    val mockItems = listOf(
        CartItem("prod1", "상품1", 10000.0, 1, "url1"),
        CartItem("prod2", "상품2", 20000.0, 2, "url2")
    )
    whenever(cartRepository.getCartItems()).thenReturn(
        flowOf(mockItems)
    )

    // Act
    viewModel.submitEvent(CartIntent.LoadCartItems)
    advanceUntilIdle()

    // Assert
    assertEquals(mockItems, viewModel.uiState.value.items)
    assertEquals(50000.0, viewModel.uiState.value.totalPrice)
    assertEquals(false, viewModel.uiState.value.isLoading)
}
```

#### CartRepositoryTest

테스트 케이스:
1. **getCartItems()**: 데이터베이스에서 조회한 데이터가 올바르게 매핑되는지 확인
2. **updateQuantity()**: 수량이 올바르게 업데이트되는지 확인
3. **removeItem()**: 아이템이 올바르게 삭제되는지 확인
4. **clearCart()**: 모든 아이템이 삭제되는지 확인

```kotlin
@Test
fun testGetCartItems() = runTest {
    // Arrange
    val mockEntity = CartItemEntity(
        id = "1",
        productId = "prod1",
        productName = "상품1",
        price = 10000.0,
        quantity = 1,
        imageUrl = "url1"
    )
    whenever(cartDao.getAllCartItems()).thenReturn(
        flowOf(listOf(mockEntity))
    )

    // Act
    val result = repository.getCartItems().first()

    // Assert
    assertEquals(1, result.size)
    assertEquals("상품1", result[0].name)
    assertEquals(10000.0, result[0].price)
}
```

### 통합 테스트 (Integration Tests)

- **CartScreen UI 테스트**: Compose 테스트로 UI 요소 렌더링 확인
- **Navigation 테스트**: 화면 전환이 올바르게 이루어지는지 확인
- **Room Database 테스트**: 실제 데이터베이스에서 CRUD 작업 검증

### 커버리지 요구사항

- **라인 커버리지**: 95%+
- **분기 커버리지**: 90%+
- **메서드 커버리지**: 100%

---

## 성능 최적화

### 1. Database 쿼리 최적화

```kotlin
// 좋은 예: 필요한 컬럼만 선택
@Query("SELECT id, productId, productName, price, quantity, imageUrl FROM cart_items")
fun getAllCartItems(): Flow<List<CartItemEntity>>

// 나쁜 예: 불필요한 데이터 로드
@Query("SELECT * FROM cart_items")
fun getAllCartItems(): Flow<List<CartItemEntity>>
```

### 2. LazyColumn을 이용한 UI 최적화

```kotlin
LazyColumn {
    items(
        items = cartItems,
        key = { it.productId },  // 고유 키 지정
        contentType = { "cartItem" }
    ) { item ->
        CartItemRow(item = item)
    }
}
```

### 3. Image Caching

Coil 라이브러리를 사용하여 이미지 캐싱:

```kotlin
Image(
    painter = rememberAsyncImagePainter(
        model = item.imageUrl,
        contentScale = ContentScale.Crop
    ),
    contentDescription = null,
    modifier = Modifier.size(100.dp)
)
```

### 4. State Hoisting

상태를 상위 계층에서 관리하여 불필요한 리컴포지션 방지:

```kotlin
@Composable
fun CartScreen(viewModel: CartViewModel) {
    val state by viewModel.uiState.collectAsState()

    CartContent(
        state = state,
        onEvent = viewModel::submitEvent
    )
}

@Composable
fun CartContent(
    state: CartUiState,
    onEvent: (CartIntent) -> Unit
) {
    // UI 렌더링
}
```

---

## 빌드 및 테스트

### 빌드

```bash
# 전체 프로젝트 빌드
./gradlew build

# Cart 모듈만 빌드
./gradlew :feature:cart:build

# Release 빌드
./gradlew :feature:cart:assembleRelease
```

### 테스트

```bash
# 전체 테스트 실행
./gradlew test

# Cart 모듈 테스트만 실행
./gradlew :feature:cart:test

# 특정 테스트 클래스만 실행
./gradlew :feature:cart:test --tests "*CartViewModelTest"

# 커버리지 리포트 생성
./gradlew :feature:cart:jacocoTestReport
```

### 코드 스타일

```bash
# Kotlin Linter 검사
./gradlew :feature:cart:lint

# 코드 포맷팅
./gradlew :feature:cart:ktlintFormat
```

---

## 의존성

### 직접 의존성

```gradle
dependencies {
    // 선언적 의존성
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))

    // Jetpack
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.compose.ui.test)
}
```

### 전이 의존성

- Jetpack Compose
- Kotlin Coroutines
- Room Database
- Retrofit
- Dagger Hilt

---

## 트러블슈팅

### 문제: "CartRepository가 주입되지 않음"

**원인**: CartModule이 올바르게 설정되지 않았거나, 의존성이 누락됨

**해결책**:
```kotlin
// CartModule.kt에서 다음 확인:
@Module
@InstallIn(SingletonComponent::class)  // 올바른 스코프
object CartModule {
    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao): CartRepository {
        return CartRepositoryImpl(cartDao)
    }
}
```

### 문제: "Room Database가 찾을 수 없음"

**원인**: core:database 모듈이 제대로 빌드되지 않음

**해결책**:
```bash
./gradlew :core:database:clean :core:database:build
./gradlew :feature:cart:clean :feature:cart:build
```

### 문제: "UI가 업데이트되지 않음"

**원인**: StateFlow를 제대로 구독하지 않았거나, collectAsState() 호출 누락

**해결책**:
```kotlin
// ✓ 올바른 방법
val state by viewModel.uiState.collectAsState()

// ✗ 잘못된 방법
val state = viewModel.uiState.value  // Compose에서 이 방법 사용 금지
```

---

## 향후 확장 계획

1. **주문 기능**
   - 장바구니를 주문으로 변환
   - 주문 히스토리 관리

2. **위시리스트**
   - 찜하기 기능
   - 나중에 사기 리스트

3. **쿠폰/할인**
   - 쿠폰 적용
   - 할인가 계산

4. **배송 옵션**
   - 배송 방법 선택
   - 배송료 계산

---

## 관련 문서

- [Cart 구현 계획](../../.moai/specs/SPEC-ANDROID-FEATURE-CART-001/plan.md)
- [Cart SPEC](../../.moai/specs/SPEC-ANDROID-FEATURE-CART-001/spec.md)
- [전체 아키텍처](../../docs/ARCHITECTURE.md)
- [모듈 구조](../../docs/MODULES.md)

---

**Cart Feature 문서 버전**: 1.0.0
**최종 검토**: 2025-12-14
**작성자**: Doc Syncer
**상태**: Complete
