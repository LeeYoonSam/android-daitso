# HOME 기능 API 레퍼런스

**SPEC**: SPEC-ANDROID-FEATURE-HOME-001
**버전**: 1.1.0
**마지막 업데이트**: 2025-12-03
**모듈**: `:feature:home`

---

## 목차

- [HomeContract API](#homecontract-api)
- [HomeViewModel API](#homeviewmodel-api)
- [Composable API](#composable-api)
- [데이터 클래스](#데이터-클래스)
- [사용 예시](#사용-예시)
- [에러 처리](#에러-처리)

---

## HomeContract API

HomeContract는 MVI 패턴의 계약을 정의하는 object입니다.

### HomeState

MVI 패턴의 UI 상태를 표현하는 sealed interface입니다.

#### HomeState.Initial

```kotlin
data object Initial : HomeState
```

**설명**: 화면 초기 로드 전 상태

**사용 시기**:
- 화면 진입 시
- 아직 사용자가 액션을 취하지 않음

**예시**:
```kotlin
val state = HomeContract.HomeState.Initial
if (state is HomeContract.HomeState.Initial) {
    println("초기 상태 - 사용자 가이드 메시지 표시")
}
```

---

#### HomeState.Loading

```kotlin
data object Loading : HomeState
```

**설명**: 서버에서 상품 데이터를 로드 중인 상태

**사용 시기**:
- LoadProducts 이벤트 후
- RetryLoad 이벤트 후
- RefreshProducts 이벤트 후 (향후)

**예시**:
```kotlin
val state = HomeContract.HomeState.Loading
if (state is HomeContract.HomeState.Loading) {
    // UI: CircularProgressIndicator 표시
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
```

---

#### HomeState.Success

```kotlin
data class Success(val products: List<Product>) : HomeState
```

**설명**: 상품 데이터를 성공적으로 로드한 상태

**프로퍼티**:

| 프로퍼티 | 타입 | 설명 |
|---------|------|------|
| `products` | `List<Product>` | 로드된 상품 리스트 (빈 리스트 가능) |

**사용 시기**:
- ProductRepository.getProducts() 완료
- 로드된 상품을 화면에 표시

**예시**:
```kotlin
val state = HomeContract.HomeState.Success(
    products = listOf(
        Product("1", "상품1", "설명1", 10000.0, "url1", "카테고리1"),
        Product("2", "상품2", "설명2", 20000.0, "url2", "카테고리2")
    )
)

if (state is HomeContract.HomeState.Success) {
    val products = state.products
    // products를 LazyVerticalGrid로 렌더링
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(products) { product ->
            ProductCard(product = product, onClick = { /* ... */ })
        }
    }
}
```

**빈 상태 처리**:
```kotlin
if (state is HomeContract.HomeState.Success && state.products.isEmpty()) {
    // EmptyView 표시: "사용 가능한 상품이 없습니다"
}
```

---

#### HomeState.Error

```kotlin
data class Error(val message: String) : HomeState
```

**설명**: 상품 데이터 로드 실패 상태

**프로퍼티**:

| 프로퍼티 | 타입 | 설명 |
|---------|------|------|
| `message` | `String` | 에러 메시지 (사용자에게 표시) |

**사용 시기**:
- ProductRepository에서 예외 발생
- 네트워크 오류
- 서버 오류

**예시**:
```kotlin
val state = HomeContract.HomeState.Error("네트워크 연결 실패")

if (state is HomeContract.HomeState.Error) {
    // UI: 에러 메시지 + 재시도 버튼 표시
    ErrorView(message = state.message)
    // 사용자가 재시도 버튼을 클릭하면 RetryLoad 이벤트 발행
}
```

---

### HomeEvent

사용자 액션과 시스템 이벤트를 표현하는 sealed interface입니다.

#### HomeEvent.LoadProducts

```kotlin
data object LoadProducts : HomeEvent
```

**설명**: 상품 목록을 로드하도록 요청하는 이벤트

**발생 상황**:
- 화면 진입 시 LaunchedEffect에서
- 사용자가 새로고침 버튼 클릭
- 초기 상태에서 화면이 표시될 때

**사용 예시**:
```kotlin
// ViewModel에 이벤트 제출
LaunchedEffect(Unit) {
    viewModel.submitEvent(HomeContract.HomeEvent.LoadProducts)
}

// 또는 사용자 액션 기반
Button(onClick = {
    coroutineScope.launch {
        viewModel.submitEvent(HomeContract.HomeEvent.LoadProducts)
    }
}) {
    Text("새로고침")
}
```

**처리 로직**:
```kotlin
override suspend fun handleEvent(event: HomeContract.HomeEvent) {
    when (event) {
        is HomeContract.HomeEvent.LoadProducts -> handleLoadProducts()
        // ...
    }
}

private suspend fun handleLoadProducts() {
    setState(HomeContract.HomeState.Loading)
    try {
        val products = productRepository.getProducts()
        setState(HomeContract.HomeState.Success(products))
    } catch (e: Exception) {
        setState(HomeContract.HomeState.Error(e.message ?: "Unknown error"))
        emitSideEffect(HomeSideEffect.ShowError(e.message ?: "Unknown error"))
    }
}
```

---

#### HomeEvent.RetryLoad

```kotlin
data object RetryLoad : HomeEvent
```

**설명**: 이전 로드 시도 실패 후 다시 로드를 시도하는 이벤트

**발생 상황**:
- Error 상태에서 사용자가 재시도 버튼 클릭
- 오류 복구 후 자동 재시도

**사용 예시**:
```kotlin
// Error 상태에서 재시도 버튼
if (uiState is HomeContract.HomeState.Error) {
    Button(onClick = {
        coroutineScope.launch {
            viewModel.submitEvent(HomeContract.HomeEvent.RetryLoad)
        }
    }) {
        Text("재시도")
    }
}
```

**처리 로직**:
```kotlin
private suspend fun handleRetryLoad() {
    // 에러 상태에서 다시 로드 시도
    handleLoadProducts()
}
```

---

### HomeSideEffect

일회성 이벤트를 표현하는 sealed interface입니다.

#### HomeSideEffect.ShowError

```kotlin
data class ShowError(val message: String) : HomeSideEffect
```

**설명**: 에러 메시지를 사용자에게 표시하는 일회성 효과

**프로퍼티**:

| 프로퍼티 | 타입 | 설명 |
|---------|------|------|
| `message` | `String` | 표시할 에러 메시지 |

**발생 상황**:
- ProductRepository에서 예외 발생
- 네트워크 오류
- 데이터 처리 중 오류

**처리 예시**:
```kotlin
// LaunchedEffect에서 SideEffect 구독
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            is HomeSideEffect.ShowError -> {
                // Snackbar 표시
                scaffoldState.snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
            is HomeSideEffect.NavigateToProductDetail -> { /* ... */ }
        }
    }
}
```

**UI 구현**:
```kotlin
Scaffold(
    snackbarHost = {
        SnackbarHost(
            hostState = scaffoldState.snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
) { /* ... */ }
```

---

#### HomeSideEffect.NavigateToProductDetail

```kotlin
data class NavigateToProductDetail(val productId: String) : HomeSideEffect
```

**설명**: 상품 상세 화면으로 네비게이션하는 일회성 효과

**프로퍼티**:

| 프로퍼티 | 타입 | 설명 |
|---------|------|------|
| `productId` | `String` | 네비게이션할 상품의 ID |

**발생 상황**:
- 사용자가 ProductCard 클릭
- 상품 상세 정보 조회 요청

**처리 예시**:
```kotlin
// LaunchedEffect에서 SideEffect 구독
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            is HomeSideEffect.NavigateToProductDetail -> {
                // Navigation 라우트 전환
                navController.navigate("productDetail/${effect.productId}")
            }
            is HomeSideEffect.ShowError -> { /* ... */ }
        }
    }
}
```

**이벤트 발행 (향후 구현)**:
```kotlin
// ProductCard 클릭 핸들러
ProductCard(
    product = product,
    onClick = {
        // Intent 발행 (향후: ProductCard 클릭 이벤트)
        coroutineScope.launch {
            viewModel.submitEvent(HomeContract.HomeEvent.OnProductClick(product.id))
        }
    }
)

// ViewModel에서 처리
private suspend fun handleProductClick(productId: String) {
    emitSideEffect(HomeSideEffect.NavigateToProductDetail(productId))
}
```

---

## HomeViewModel API

HomeViewModel은 MVI 패턴의 비즈니스 로직을 담당합니다.

### 클래스 선언

```kotlin
class HomeViewModel : BaseViewModel<
    HomeContract.HomeState,
    HomeContract.HomeEvent,
    HomeSideEffect
>(
    initialState = HomeContract.HomeState.Initial
)
```

### 공개 인터페이스 (BaseViewModel에서 상속)

#### uiState

```kotlin
val uiState: StateFlow<HomeContract.HomeState>
```

**설명**: UI 상태를 발행하는 읽기 전용 StateFlow

**타입**: `StateFlow<HomeContract.HomeState>`

**사용 예시**:
```kotlin
val state by viewModel.uiState.collectAsState()

when (state) {
    is HomeContract.HomeState.Initial -> InitialView()
    is HomeContract.HomeState.Loading -> LoadingView()
    is HomeContract.HomeState.Success -> {
        val products = (state as HomeContract.HomeState.Success).products
        SuccessView(products)
    }
    is HomeContract.HomeState.Error -> {
        val message = (state as HomeContract.HomeState.Error).message
        ErrorView(message)
    }
}
```

---

#### sideEffect

```kotlin
val sideEffect: SharedFlow<HomeSideEffect>
```

**설명**: 일회성 이벤트를 발행하는 SharedFlow

**타입**: `SharedFlow<HomeSideEffect>`

**사용 예시**:
```kotlin
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            is HomeSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            is HomeSideEffect.NavigateToProductDetail -> {
                navController.navigate("productDetail/${effect.productId}")
            }
        }
    }
}
```

---

#### currentState

```kotlin
val currentState: HomeContract.HomeState
```

**설명**: 현재 UI 상태를 동기적으로 조회

**타입**: `HomeContract.HomeState`

**사용 예시**:
```kotlin
// ViewModel 내부에서 현재 상태 확인
private suspend fun handleEvent(event: HomeContract.HomeEvent) {
    val current = currentState
    when (current) {
        is HomeContract.HomeState.Loading -> return // 이미 로딩 중이면 무시
        is HomeContract.HomeState.Success -> { /* ... */ }
        else -> { /* ... */ }
    }
}
```

---

#### submitEvent

```kotlin
suspend fun submitEvent(event: HomeContract.HomeEvent)
```

**설명**: 사용자 이벤트를 ViewModel에 제출

**파라미터**:

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `event` | `HomeContract.HomeEvent` | 제출할 이벤트 |

**반환값**: `Unit` (suspend 함수)

**사용 예시**:
```kotlin
// LaunchedEffect에서 초기 로드
LaunchedEffect(Unit) {
    viewModel.submitEvent(HomeContract.HomeEvent.LoadProducts)
}

// 사용자 액션에서 재시도
Button(onClick = {
    coroutineScope.launch {
        viewModel.submitEvent(HomeContract.HomeEvent.RetryLoad)
    }
}) {
    Text("재시도")
}
```

---

### 내부 메서드 (보호됨)

#### handleEvent (Protected)

```kotlin
protected suspend fun handleEvent(event: HomeContract.HomeEvent)
```

**설명**: 이벤트를 처리하고 상태를 업데이트하는 메서드

**구현**:
```kotlin
override suspend fun handleEvent(event: HomeContract.HomeEvent) {
    when (event) {
        is HomeContract.HomeEvent.LoadProducts -> handleLoadProducts()
        is HomeContract.HomeEvent.RetryLoad -> handleRetryLoad()
    }
}
```

---

#### setState (Protected)

```kotlin
protected suspend fun setState(newState: HomeContract.HomeState)
```

**설명**: 새로운 상태로 변경하고 uiState에 발행

**파라미터**:

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `newState` | `HomeContract.HomeState` | 새로운 UI 상태 |

**사용 예시**:
```kotlin
private suspend fun handleLoadProducts() {
    setState(HomeContract.HomeState.Loading)
    try {
        val products = productRepository.getProducts()
        setState(HomeContract.HomeState.Success(products))
    } catch (e: Exception) {
        setState(HomeContract.HomeState.Error(e.message ?: "Unknown"))
    }
}
```

---

#### emitSideEffect (Protected)

```kotlin
protected suspend fun emitSideEffect(effect: HomeSideEffect)
```

**설명**: SideEffect를 발행

**파라미터**:

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `effect` | `HomeSideEffect` | 발행할 일회성 효과 |

**사용 예시**:
```kotlin
private suspend fun handleLoadProducts() {
    try {
        // ...
    } catch (e: Exception) {
        setState(HomeContract.HomeState.Error(e.message ?: "Unknown"))
        emitSideEffect(HomeSideEffect.ShowError(e.message ?: "Unknown"))
    }
}
```

---

## Composable API

HomeScreen과 관련된 Composable 함수들의 API입니다.

### HomeScreen

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onProductClick: (String) -> Unit = {}
)
```

**설명**: HOME 화면의 메인 Composable

**파라미터**:

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|-------|------|
| `viewModel` | `HomeViewModel` | 필수 | HomeViewModel 인스턴스 |
| `onProductClick` | `(String) -> Unit` | `{}` | 상품 클릭 콜백 (상품 ID 전달) |

**반환값**: `Unit` (Composable)

**사용 예시**:
```kotlin
// 네비게이션 구성
navController.navigate("home")

// HomeScreen 렌더링
HomeScreen(
    viewModel = homeViewModel,
    onProductClick = { productId ->
        navController.navigate("productDetail/$productId")
    }
)
```

**내부 구조**:
```
Scaffold
  ├── TopAppBar
  └── when (uiState) {
      Initial → InitialView()
      Loading → LoadingView()
      Success → SuccessView()
      Error → ErrorView()
  }
```

---

### ProductCard

```kotlin
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**설명**: 상품 정보를 표시하는 카드 컴포넌트

**파라미터**:

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|-------|------|
| `product` | `Product` | 필수 | 표시할 상품 데이터 |
| `onClick` | `() -> Unit` | 필수 | 클릭 콜백 |
| `modifier` | `Modifier` | `Modifier` | 스타일 수정자 |

**반환값**: `Unit` (Composable)

**사용 예시**:
```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(2)) {
    items(products) { product ->
        ProductCard(
            product = product,
            onClick = { onProductClick(product.id) },
            modifier = Modifier.padding(4.dp)
        )
    }
}
```

**카드 구조**:
```
Card (클릭 가능)
  ├── Image (1:1 비율, Coil AsyncImage)
  └── Column (상품 정보)
      ├── Category (레이블, 회색)
      ├── Name (굵음, 최대 2줄)
      └── Price (기본색, 원화 포맷)
```

---

### SuccessView

```kotlin
@Composable
fun SuccessView(
    products: List<Product>,
    onProductClick: (String) -> Unit
)
```

**설명**: 성공 상태에서 상품 그리드를 표시

**파라미터**:

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `products` | `List<Product>` | 표시할 상품 리스트 |
| `onProductClick` | `(String) -> Unit` | 상품 클릭 콜백 |

---

### ErrorView

```kotlin
@Composable
fun ErrorView(message: String)
```

**설명**: 에러 상태에서 에러 메시지를 표시

**파라미터**:

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `message` | `String` | 표시할 에러 메시지 |

---

### LoadingView

```kotlin
@Composable
fun LoadingView()
```

**설명**: 로딩 상태에서 진행 표시기를 표시

---

### InitialView

```kotlin
@Composable
fun InitialView()
```

**설명**: 초기 상태에서 가이드 메시지를 표시

---

## 데이터 클래스

### Product

```kotlin
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String
)
```

**설명**: 상품 정보를 표현하는 데이터 클래스

**프로퍼티**:

| 프로퍼티 | 타입 | 설명 | 예시 |
|---------|------|------|------|
| `id` | `String` | 상품 고유 ID | `"prod_001"` |
| `name` | `String` | 상품명 | `"무선 이어폰"` |
| `description` | `String` | 상품 설명 | `"고음질 무선 이어폰"` |
| `price` | `Double` | 가격 (원화) | `59990.0` |
| `imageUrl` | `String` | 이미지 URL | `"https://..."` |
| `category` | `String` | 카테고리 | `"전자제품"` |

**사용 예시**:
```kotlin
val product = Product(
    id = "prod_001",
    name = "무선 이어폰",
    description = "고음질 무선 이어폰",
    price = 59990.0,
    imageUrl = "https://example.com/image.jpg",
    category = "전자제품"
)

// ProductCard에 전달
ProductCard(product = product, onClick = { /* ... */ })
```

---

## 사용 예시

### 화면 초기 로드

```kotlin
@Composable
fun HomeScreenWithViewModel() {
    val viewModel = hiltViewModel<HomeViewModel>()

    // 초기 로드
    LaunchedEffect(Unit) {
        viewModel.submitEvent(HomeContract.HomeEvent.LoadProducts)
    }

    // SideEffect 처리
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.ShowError -> {
                    scaffoldState.snackbarHostState.showSnackbar(effect.message)
                }
                is HomeSideEffect.NavigateToProductDetail -> {
                    // 네비게이션 처리
                }
            }
        }
    }

    // UI 렌더링
    HomeScreen(
        viewModel = viewModel,
        onProductClick = { productId ->
            // 상품 클릭 처리
        }
    )
}
```

### 재시도 로직

```kotlin
@Composable
fun ErrorViewWithRetry(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "오류 발생",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Text("재시도")
            }
        }
    }
}
```

### 상태별 UI 분기

```kotlin
val state by viewModel.uiState.collectAsState()

Box(modifier = Modifier.fillMaxSize()) {
    when (state) {
        is HomeContract.HomeState.Initial -> {
            InitialView()
        }
        is HomeContract.HomeState.Loading -> {
            LoadingView()
        }
        is HomeContract.HomeState.Success -> {
            val products = (state as HomeContract.HomeState.Success).products
            if (products.isEmpty()) {
                EmptyView()
            } else {
                SuccessView(products) { productId ->
                    navController.navigate("productDetail/$productId")
                }
            }
        }
        is HomeContract.HomeState.Error -> {
            val message = (state as HomeContract.HomeState.Error).message
            ErrorView(message)
        }
    }
}
```

---

## 에러 처리

### 일반적인 에러 시나리오

#### 네트워크 오류

```kotlin
try {
    val products = productRepository.getProducts()
} catch (e: IOException) {
    setState(HomeContract.HomeState.Error("네트워크 연결을 확인해주세요"))
    emitSideEffect(HomeSideEffect.ShowError("네트워크 연결 실패"))
}
```

#### 서버 오류

```kotlin
catch (e: HttpException) {
    setState(HomeContract.HomeState.Error("서버 오류: ${e.code()}"))
    emitSideEffect(HomeSideEffect.ShowError("서버에서 오류가 발생했습니다"))
}
```

#### 데이터 파싱 오류

```kotlin
catch (e: JsonException) {
    setState(HomeContract.HomeState.Error("데이터 처리 오류"))
    emitSideEffect(HomeSideEffect.ShowError("데이터를 처리할 수 없습니다"))
}
```

### 에러 재시도

```kotlin
// Error 상태에서 RetryLoad 이벤트 발행
viewModel.submitEvent(HomeContract.HomeEvent.RetryLoad)

// ViewModel에서 처리
private suspend fun handleRetryLoad() {
    handleLoadProducts() // 동일한 로드 로직 재실행
}
```

---

**END OF API REFERENCE**
