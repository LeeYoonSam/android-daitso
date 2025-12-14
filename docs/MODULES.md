# Daitso 모듈 구조 가이드

**SPEC**: SPEC-ANDROID-INIT-001, SPEC-ANDROID-MVI-002, SPEC-ANDROID-FEATURE-CART-001
**최종 업데이트**: 2025-12-14
**작성자**: Doc Syncer

---

## 목차

- [개요](#개요)
- [Core 모듈](#core-모듈)
- [Feature 모듈](#feature-모듈)
  - [Cart 모듈](#cart-모듈-상세)
- [모듈 의존성 그래프](#모듈-의존성-그래프)
- [모듈별 책임](#모듈별-책임)

---

## 개요

Daitso는 Clean Architecture와 MVI 패턴을 기반으로 한 안드로이드 이커머스 애플리케이션입니다.
프로젝트는 다음과 같은 계층으로 구성됩니다:

- **Core 모듈**: 앱 전체에서 공유하는 공통 기능 제공
- **Feature 모듈**: 사용자에게 보이는 화면 및 기능 구현
- **App**: Feature와 Core를 통합하는 메인 애플리케이션

---

## Core 모듈

Core 모듈은 다른 모든 모듈에서 의존하는 공통 기능을 제공합니다.

### :core:model

**책임**: 도메인 모델 정의

**주요 클래스**:
- `Product` - 상품 도메인 모델
- `CartItem` - 장바구니 아이템 모델
- `User` - 사용자 모델

**의존성**:
- Kotlin Serialization

**사용 예**:
```kotlin
import com.bup.ys.daitso.core.model.Product

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String
)
```

---

### :core:common

**책임**: 공통 유틸리티, 결과 래퍼, 의존성 주입 기초

**주요 구성 요소**:

#### Result<T> 래퍼
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

#### @Dispatcher Annotation
```kotlin
@Qualifier
annotation class Dispatcher(val dispatcher: DaitsoDispatchers)

enum class DaitsoDispatchers {
    IO,
    Default,
    Main
}
```

**의존성**:
- Kotlin Coroutines

**사용 예**:
```kotlin
class ProductRepositoryImpl @Inject constructor(
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {
    // ...
}
```

---

### :core:designsystem

**책임**: UI 컴포넌트, 테마, 디자인 시스템

**주요 구성 요소**:
- `DaitsoTheme` - Material3 기반 메인 테마
- `DaitsoButton` - 커스텀 버튼
- `DaitsoTextField` - 입력 필드
- `Color`, `Typography`, `Shape` - 디자인 토큰

**의존성**:
- Jetpack Compose
- Material3

**사용 예**:
```kotlin
@Composable
fun HomeScreen() {
    DaitsoTheme {
        Surface {
            DaitsoButton(text = "클릭", onClick = { })
        }
    }
}
```

---

### :core:network

**책임**: REST API 통신, Retrofit 설정

**주요 구성 요소**:
- `DaitsoApiService` - Retrofit 인터페이스
- `NetworkDataSource` - 네트워크 데이터 소스
- `NetworkModule` - Hilt 모듈

**의존성**:
- Retrofit2
- OkHttp
- Hilt

**주요 API**:
```kotlin
interface DaitsoApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Product
}
```

---

### :core:database

**책임**: Room Database 설정, DAO, Entity 정의

**주요 구성 요소**:
- `DaitsoDatabase` - Room Database 클래스
- `CartDao` - Cart 아이템 접근 객체
- `CartItemEntity` - Room 엔티티

**의존성**:
- Room
- Kotlin Coroutines (Flow)

**주요 Entity**:
```kotlin
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)
```

**주요 DAO 메서드**:
```kotlin
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    fun getCartItemsByProductId(productId: String): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
```

---

### :core:data

**책임**: Repository 패턴 구현, 데이터 소스 통합

**주요 구성 요소**:
- `ProductRepository` 인터페이스
- `ProductRepositoryImpl` 구현
- `DataModule` - Hilt 모듈

**의존성**:
- :core:model
- :core:common
- :core:network
- :core:database

**Repository 인터페이스 예**:
```kotlin
interface ProductRepository {
    fun getProducts(): Flow<Result<List<Product>>>
    fun getProduct(id: String): Flow<Result<Product>>
}
```

---

### :core:ui

**책임**: MVI 패턴의 기본 구조, Navigation, BaseViewModel

**주요 구성 요소**:
- `BaseViewModel<S, E, SE>` - MVI 기본 ViewModel
- `UiState` - 상태 마커 인터페이스
- `UiEvent` - 이벤트 마커 인터페이스
- `UiSideEffect` - 부수 효과 마커 인터페이스
- `AppRoute` - Type-safe Navigation

**의존성**:
- Jetpack Lifecycle
- Kotlin Coroutines
- Kotlin Serialization

**BaseViewModel 구조**:
```kotlin
abstract class BaseViewModel<
    S : UiState,
    E : UiEvent,
    SE : UiSideEffect
>(initialState: S) : ViewModel() {
    val uiState: StateFlow<S>
    val sideEffect: Flow<SE>

    abstract suspend fun handleEvent(event: E)

    protected fun updateState(state: S)
    protected fun launchSideEffect(effect: SE)
    protected fun submitEvent(event: E)
}
```

---

## Feature 모듈

Feature 모듈은 사용자에게 보이는 화면과 기능을 구현합니다.

### Feature 모듈 구조

```
:feature:MODULE_NAME/
├── src/main/kotlin/com/bup/ys/daitso/feature/MODULE_NAME/
│   ├── contract/
│   │   └── MODULE_NAMEContract.kt       # MVI 계약 (State, Event, SideEffect)
│   ├── presentation/
│   │   ├── MODULE_NAMEViewModel.kt      # ViewModel
│   │   └── MODULE_NAMEScreen.kt         # Composable UI
│   ├── repository/
│   │   └── MODULE_NAMERepositoryImpl.kt  # Repository 구현
│   ├── domain/
│   │   └── MODULE_NAMERepository.kt     # Repository 인터페이스
│   ├── navigation/
│   │   └── MODULE_NAMENavigation.kt     # Navigation 설정
│   ├── di/
│   │   └── MODULE_NAMEModule.kt         # Hilt DI 모듈
│   └── util/
│       └── 유틸리티 클래스들
├── src/test/kotlin/
│   └── 단위 테스트
└── build.gradle.kts
```

### Feature 모듈 의존성 규칙

1. **상위 → 하위 의존만 허용**:
   ```
   app → :feature:MODULE → :core:MODULE
   ```

2. **같은 계층 간 의존 금지**:
   ```
   :feature:home ≠→ :feature:cart  // 직접 의존 불가능
   (Network layer를 통해 간접 통신)
   ```

3. **원형 의존 금지**:
   ```
   A → B → C → A  // 불가능
   ```

---

## Cart 모듈 상세

### 모듈 개요

**위치**: `:feature:cart`

**목적**: 사용자가 추가한 상품들을 관리하는 장바구니 기능 제공

**관련 SPEC**: SPEC-ANDROID-FEATURE-CART-001

**구현 상태**: 완료 (2025-12-14)

---

### CartContract 정의

#### CartUiState
장바구니 화면의 완전한 UI 상태를 나타냅니다.

```kotlin
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState
```

**속성**:
- `items`: 현재 장바구니에 있는 아이템 목록
- `totalPrice`: 모든 아이템의 총 가격 (자동 계산)
- `isLoading`: 데이터 로드 중 여부
- `error`: 발생한 에러 메시지 (null이면 에러 없음)

#### CartItem
개별 장바구니 아이템을 나타냅니다.

```kotlin
data class CartItem(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)
```

**속성**:
- `productId`: 상품 고유 ID
- `name`: 상품명
- `price`: 단위 가격
- `quantity`: 현재 수량 (1-999)
- `imageUrl`: 상품 이미지 URL

#### CartIntent
사용자 상호작용과 시스템 이벤트를 나타냅니다.

```kotlin
sealed interface CartIntent : UiEvent {
    object LoadCartItems : CartIntent
    data class UpdateQuantity(val productId: String, val quantity: Int) : CartIntent
    data class RemoveItem(val productId: String) : CartIntent
    object ClearCart : CartIntent
    object DismissError : CartIntent
}
```

**Intent 종류**:
- `LoadCartItems`: 데이터베이스에서 장바구니 아이템 로드
- `UpdateQuantity`: 특정 아이템의 수량 변경
- `RemoveItem`: 특정 아이템 삭제
- `ClearCart`: 전체 장바구니 비우기
- `DismissError`: 에러 메시지 해제

#### CartSideEffect
일회성 이벤트 (Toast, Dialog, Navigation)를 나타냅니다.

```kotlin
sealed interface CartSideEffect : UiSideEffect {
    object NavigateToCheckout : CartSideEffect
    data class ShowToast(val message: String) : CartSideEffect
    object NavigateToHome : CartSideEffect
}
```

**SideEffect 종류**:
- `NavigateToCheckout`: 결제 화면으로 이동
- `ShowToast`: 토스트 메시지 표시
- `NavigateToHome`: 홈 화면으로 이동

---

### CartViewModel 구현

**위치**: `presentation/CartViewModel.kt`

```kotlin
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : BaseViewModel<CartUiState, CartIntent, CartSideEffect>(
    initialState = CartUiState()
)
```

**책임**:
1. Cart Intent 처리
2. Repository와의 상호작용
3. 상태 업데이트
4. Side Effect 발생

**주요 메서드**:

#### loadCartItems()
데이터베이스에서 장바구니 아이템을 로드합니다.
```kotlin
private suspend fun loadCartItems() {
    try {
        updateState(currentState.copy(isLoading = true, error = null))

        cartRepository.getCartItems().collect { items ->
            val totalPrice = calculateTotalPrice(items)
            updateState(
                currentState.copy(
                    items = items,
                    totalPrice = totalPrice,
                    isLoading = false,
                    error = null
                )
            )
        }
    } catch (e: Exception) {
        updateState(
            currentState.copy(
                isLoading = false,
                error = e.message ?: "장바구니 로드 중 오류가 발생했습니다"
            )
        )
    }
}
```

#### updateQuantity(productId, quantity)
특정 아이템의 수량을 업데이트합니다.
- 수량 범위: 1 ~ 999 (자동 제한)
- 업데이트 후 Toast 메시지 표시

#### removeItem(productId)
특정 아이템을 삭제합니다.
- 데이터베이스에서 제거
- 삭제 후 Toast 메시지 표시

#### clearCart()
전체 장바구니를 비웁니다.
- 모든 아이템 삭제
- Toast 메시지로 확인

#### calculateTotalPrice(items)
아이템 목록의 총 가격을 계산합니다.
```kotlin
private fun calculateTotalPrice(items: List<CartItem>): Double {
    return items.fold(0.0) { total, item ->
        total + (item.price * item.quantity)
    }
}
```

---

### CartScreen Composable

**위치**: `presentation/CartScreen.kt`

**책임**:
1. ViewModel에서 상태 관찰
2. 상태에 따른 UI 렌더링
3. 사용자 입력 처리
4. Navigation 처리

**주요 구성**:

#### 상태별 UI
```kotlin
when (state) {
    is CartUiState.Loading -> LoadingScreen()
    is CartUiState.Success -> CartContent(items, totalPrice)
    is CartUiState.Error -> ErrorScreen(error)
    is CartUiState.Empty -> EmptyCartScreen()
}
```

#### CartItem 컴포넌트
각 아이템에 표시되는 정보:
- 상품 이미지
- 상품명
- 단위 가격
- 수량 조절 버튼 (+/-)
- 삭제 버튼

#### 총 가격 표시
원화(₩) 형식으로 표시합니다.

#### 결제하기 버튼
- 활성 조건: 장바구니에 아이템이 있을 때
- 클릭: `NavigateToCheckout` SideEffect 발생

---

### CartRepository

**인터페이스 위치**: `domain/CartRepository.kt`

**구현 위치**: `repository/CartRepositoryImpl.kt`

```kotlin
interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun updateQuantity(productId: String, quantity: Int)
    suspend fun removeItem(productId: String)
    suspend fun clearCart()
}
```

**구현 상세**:

#### getCartItems()
Room Database에서 아이템을 조회하여 Flow로 반환합니다.
```kotlin
override fun getCartItems(): Flow<List<CartItem>> {
    return cartDao.getAllCartItems().map { entities ->
        entities.map { entity ->
            CartItem(
                productId = entity.productId,
                name = entity.productName,
                price = entity.price,
                quantity = entity.quantity,
                imageUrl = entity.imageUrl
            )
        }
    }
}
```

#### updateQuantity(productId, quantity)
수량을 1~999 범위로 제한하고 업데이트합니다.

#### removeItem(productId)
productId로 아이템을 찾아 삭제합니다.

#### clearCart()
모든 아이템을 삭제합니다.

---

### CartModule (Hilt DI)

**위치**: `di/CartModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CartModule {

    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao): CartRepository {
        return CartRepositoryImpl(cartDao)
    }
}
```

**책임**:
- CartRepository 주입 설정
- CartDao 의존성 연결

---

### CartNavigation

**위치**: `navigation/CartNavigation.kt`

**구성**:
- Route 정의
- NavGraph에 Cart 화면 추가
- 다른 화면과의 네비게이션 연결

---

### PriceFormatter 유틸리티

**위치**: `util/PriceFormatter.kt`

**기능**:
- 숫자를 원화(₩) 형식으로 포맷팅
- 소수점 처리
- 천 단위 구분

```kotlin
fun Double.formatPrice(): String = "₩${String.format("%,.0f", this)}"
```

---

## 모듈 의존성 그래프

```
┌─────────────────────────────┐
│      App (Main)             │
├─────────────────────────────┤
│ - MainActivity              │
│ - AppNavHost                │
└────────────┬────────────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼──────────┐  ┌──▼────────────┐
│ :feature:cart│  │ :feature:home  │
│ :feature:*   │  │ :feature:*     │
└───┬──────────┘  └──┬────────────┘
    │                │
    └────────┬───────┘
             │
    ┌────────▼───────────────────────────────┐
    │         Core Modules                    │
    ├────────────────────────────────────────┤
    │ ┌─────────────┐ ┌─────────────────┐  │
    │ │ :core:data  │ │ :core:ui        │  │
    │ │ - Repository│ │ - BaseViewModel │  │
    │ │ - DataSource│ │ - Navigation    │  │
    │ └─────────────┘ │ - Routes        │  │
    │    │   │        └─────────────────┘  │
    │    │   │                              │
    │ ┌──▼───▼──┐ ┌──────────────────┐    │
    │ │ :core:  │ │ :core:designsystem  │  │
    │ │database  │ │ - DaitsoTheme    │  │
    │ │network   │ │ - Components     │  │
    │ │- CartDao │ │ - Color/Type     │  │
    │ │- API     │ └──────────────────┘  │
    │ └─────────┘                         │
    │                                      │
    │ ┌──────────────┐ ┌──────────────┐  │
    │ │ :core:model  │ │ :core:common │  │
    │ │ - Domain     │ │ - Result<T>  │  │
    │ │   Models     │ │ - @Dispatcher   │  │
    │ │ - Serializable  │ - Logger     │  │
    │ └──────────────┘ └──────────────┘  │
    └────────────────────────────────────┘
```

---

## 모듈별 책임

| 모듈 | 책임 | 주요 클래스 |
|------|------|-----------|
| :core:model | 도메인 모델 정의 | Product, CartItem, User |
| :core:common | 공통 유틸리티 | Result<T>, @Dispatcher |
| :core:designsystem | UI 테마 및 컴포넌트 | DaitsoTheme, DaitsoButton |
| :core:network | REST API 통신 | DaitsoApiService, NetworkDataSource |
| :core:database | 로컬 데이터 저장소 | DaitsoDatabase, CartDao, CartItemEntity |
| :core:data | Repository 구현 | ProductRepositoryImpl |
| :core:ui | MVI 기본 구조 | BaseViewModel, UiState, AppRoute |
| :feature:cart | 장바구니 화면 | CartViewModel, CartScreen, CartRepository |
| app | 메인 앱 | MainActivity, AppNavHost |

---

## 새로운 Feature 모듈 추가 가이드

새로운 기능을 추가하려면 다음 단계를 따르세요:

### 1. 모듈 구조 생성
```
:feature:new-feature/
├── src/main/kotlin/com/bup/ys/daitso/feature/newfeature/
│   ├── contract/
│   ├── presentation/
│   ├── repository/
│   ├── domain/
│   ├── navigation/
│   ├── di/
│   └── util/
└── build.gradle.kts
```

### 2. build.gradle.kts 설정
```kotlin
plugins {
    alias(libs.plugins.daitso.android.library.compose)
    alias(libs.plugins.daitso.android.hilt)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    // ...
}
```

### 3. MVI 계약 정의 (CartContract 참고)
- State 정의
- Intent 정의
- SideEffect 정의

### 4. ViewModel 구현
BaseViewModel을 상속받아 구현합니다.

### 5. UI 구현
Compose를 사용하여 화면을 구현합니다.

### 6. Navigation 설정
앱의 AppNavHost에 새 화면을 등록합니다.

---

## 문서 링크

- [전체 아키텍처 가이드](./ARCHITECTURE.md)
- [Cart API 참고](./sections/cart-api-reference.md)
- [Home 기능 문서](./FEATURE_HOME.md)
- [Detail 기능 문서](./FEATURE_DETAIL.md)

---

**모듈 문서 버전**: 1.0.0
**최종 검토**: 2025-12-14
**상태**: Active
