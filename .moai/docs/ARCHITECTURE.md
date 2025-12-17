# Daitso 아키텍처 가이드

**SPEC**: SPEC-ANDROID-INIT-001
**최종 업데이트**: 2025-11-28
**작성자**: GOOS

---

## 목차

- [개요](#개요)
- [아키텍처 패턴](#아키텍처-패턴)
- [레이어별 구조](#레이어별-구조)
- [모듈 의존성 그래프](#모듈-의존성-그래프)
- [데이터 흐름](#데이터-흐름)
- [디렉토리 구조](#디렉토리-구조)
- [핵심 컴포넌트](#핵심-컴포넌트)
- [확장 포인트](#확장-포인트)
- [성능 고려사항](#성능-고려사항)

---

## 개요

Daitso는 다음과 같은 아키텍처 원칙을 따르는 안드로이드 애플리케이션입니다:

1. **Clean Architecture**: 의존성 역전, 계층 분리
2. **MVI Pattern**: Model-View-Intent 단방향 데이터 흐름
3. **Modular Design**: 기능별 독립적인 모듈
4. **Offline-first**: Room을 Single Source of Truth로 사용

### 핵심 특징

| 특징 | 설명 |
| --- | --- |
| **단방향 데이터 흐름** | UI → Intent → ViewModel → State → UI |
| **모듈 독립성** | 모듈 간 느슨한 결합(Loose Coupling) |
| **타입 안전성** | Kotlin, 의존성 주입, 제네릭 활용 |
| **오프라인 지원** | 로컬 데이터베이스가 기본 데이터 소스 |
| **테스트 용이성** | 의존성 주입으로 Mock 객체 쉽게 교체 |

---

## 아키텍처 패턴

### 1. Clean Architecture 계층

```
┌──────────────────────────────────────────┐
│         Presentation Layer               │  UI, State Management
│    (Compose, ViewModel, State)           │
└──────────────────────────────────────────┘
           ▲
           │ (Data Class)
           ▼
┌──────────────────────────────────────────┐
│          Domain Layer                    │  Business Logic
│      (Use Cases, Repository IF)          │
└──────────────────────────────────────────┘
           ▲
           │ (Repository)
           ▼
┌──────────────────────────────────────────┐
│           Data Layer                     │  Repository Impl
│    (LocalDataSource, NetworkDataSource)  │
└──────────────────────────────────────────┘
           ▲
           ├─── Remote (Network, API)
           ├─── Local (Database, Cache)
           └─── File System

```

### 2. MVI (Model-View-Intent) 패턴

```
사용자 상호작용 (Click, Input, ...)
           │
           ▼
      ┌─────────┐
      │ Intent  │  사용자 의도 표현
      └────┬────┘
           │
           ▼
      ┌──────────────┐
      │  ViewModel   │  Intent 처리 & 상태 변경
      │ (Process)    │
      └────┬─────────┘
           │
           ▼
      ┌───────────┐
      │ State     │  UI 렌더링용 상태 데이터
      │ (Model)   │
      └────┬──────┘
           │
           ▼
      ┌──────────┐
      │   View   │  상태를 UI로 표현
      │ (Compose)│
      └──────────┘
           │
           └──► 사용자에게 표시

```

### 3. 데이터 흐름 (Offline-first)

```
┌─────────────────────────────────────┐
│         View/ViewModel              │
│  (UI 요청: getProducts())           │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│     ProductRepository               │
│  (로컬 캐시 우선 전략)               │
└────────────┬────────────────────────┘
             │
      ┌──────┴──────────────────┐
      │ 1. 로컬 캐시 로드        │
      ▼                         ▼
┌──────────────┐         ┌────────────────┐
│ LocalDataSrc │         │ NetworkDataSrc │
│ (Room DB)    │         │ (Retrofit API) │
│              │         │                │
│ ProductDao   │         │ 병렬 요청      │
│ [Products]   │         │ [Latest Data]  │
└──────┬───────┘         └────────┬───────┘
       │                          │
       ├─ Success 발행 (즉시)    │
       │                          │
       └──────┐ Success 발행     │
              │ (2번째)           │
              │                   │
       ┌──────▼───────────────────▼─┐
       │  캐시 업데이트 (insertProducts) │
       └──────────────────────────────┘
               │
               ▼
         [Flow<Result>]
          ├─ Loading
          ├─ Success (로컬 캐시)
          ├─ Success (네트워크)
          └─ Error (네트워크 실패)
               │
               ▼
            View

**주요 특징:**
- Loading → 로컬 데이터 → 네트워크 데이터 순차 발행
- 네트워크 실패해도 로컬 데이터 유지
- 사용자는 즉시 캐시된 데이터 확인 가능
```

---

## 레이어별 구조

### Presentation Layer (UI)

**책임**: 사용자 인터페이스, 상태 관리, 사용자 입력 처리

**구성 요소**:
- **Compose Screen**: UI 레이아웃 및 렌더링
- **ViewModel**: 상태 관리 및 Intent 처리
- **State**: 화면별 상태 데이터 클래스
- **Event**: 사용자 이벤트 (클릭, 입력 등)

**예시**:
```kotlin
// ProductsScreen.kt
@Composable
fun ProductsScreen(viewModel: ProductsViewModel) {
    val state by viewModel.state.collectAsState()

    when (state) {
        is ProductsState.Loading -> LoadingIndicator()
        is ProductsState.Success -> ProductList(state.products)
        is ProductsState.Error -> ErrorView(state.exception)
    }
}

// ProductsViewModel.kt
@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ProductsState>(ProductsState.Loading)
    val state = _state.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            repository.getProducts().collect { result ->
                _state.value = when (result) {
                    is Result.Success -> ProductsState.Success(result.data)
                    is Result.Error -> ProductsState.Error(result.exception)
                    Result.Loading -> ProductsState.Loading
                }
            }
        }
    }
}
```

### Domain Layer

**책임**: 비즈니스 로직, 계약(인터페이스) 정의

**구성 요소**:
- **Repository Interface**: 데이터 접근 계약
- **Model**: 도메인 엔티티
- **UseCase** (선택): 비즈니스 흐름 캡슐화

**예시**:
```kotlin
// ProductRepository.kt (인터페이스)
interface ProductRepository {
    fun getProducts(): Flow<Result<List<Product>>>
    fun getProduct(id: String): Flow<Result<Product>>
}

// Product.kt (도메인 모델)
@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String
)
```

### Data Layer

**책임**: 데이터 소스 관리, Repository 구현

**구성 요소**:
- **Repository Implementation**: 도메인 Repository 구현
- **DataSource**: 원격(Network) 및 로컬(Database) 데이터 소스
- **Entity**: 데이터베이스 엔티티
- **DAO**: 데이터베이스 접근 객체
- **API Service**: REST API 정의

**예시**:
```kotlin
// ProductRepositoryImpl.kt
class ProductRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {
    override fun getProducts(): Flow<Result<List<Product>>> = flow {
        emit(Result.Loading)

        // 1. 로컬 데이터 먼저 반환 (Offline-first)
        val cachedProducts = localDataSource.getProducts()
        emit(Result.Success(cachedProducts))

        // 2. 네트워크에서 최신 데이터 가져오기
        try {
            val remoteProducts = networkDataSource.getProducts()
            localDataSource.saveProducts(remoteProducts)
            emit(Result.Success(remoteProducts))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)
}
```

---

## 모듈 의존성 그래프

### 모듈 구조

```
app (Application Module)
 │
 ├─► :core:model
 ├─► :core:common
 ├─► :core:designsystem
 ├─► :core:data
 │    │
 │    ├─► :core:model
 │    ├─► :core:common
 │    ├─► :core:network
 │    │    ├─► :core:model
 │    │    └─► :core:common
 │    └─► :core:database
 │         ├─► :core:model
 │         └─► :core:common
 └─► :feature:products (향후)
      ├─► :core:designsystem
      ├─► :core:data
      └─► :core:model

```

### 의존성 규칙

1. **상위 → 하위 의존만 허용**
   ```
   app → feature → core → (없음)
   ```

2. **같은 레벨 간 의존 금지**
   ```
   :core:network ≠ :core:database  // 직접 의존 불가
   (Repository를 통한 간접 의존만 가능)
   ```

3. **원형 의존성 금지**
   ```
   A → B → C → A  // 불가능
   ```

---

## 데이터 흐름

### 타이밍 다이어그램: 상품 목록 조회

```
시간 흐름 →

ViewModel (ProductsViewModel)
    │
    └─► repository.getProducts() 호출
        │
        └─► ProductRepositoryImpl
            │
            ├─► [Flow 시작]
            │
            ├─► emit(Result.Loading)
            │   └─► ViewModel ◄─ emit 수신
            │       └─► UI State = Loading
            │
            ├─► localDataSource.getProducts()
            │   └─► CartDao.getCartItems() [Room Query]
            │       └─► DB 쿼리 실행
            │           └─► [로컬 데이터 반환]
            │
            ├─► emit(Result.Success(localProducts))
            │   └─► ViewModel ◄─ emit 수신
            │       └─► UI State = Success(localProducts)
            │
            ├─► try {
            │   ├─► networkDataSource.getProducts()
            │   │   └─► DaitsoApiService.getProducts()
            │   │       └─► [네트워크 요청]
            │   │           └─► [API 응답 수신]
            │   │
            │   ├─► localDataSource.saveProducts(remoteProducts)
            │   │   └─► CartDao.insertProduct()
            │   │       └─► [DB 저장]
            │   │
            │   └─► emit(Result.Success(remoteProducts))
            │       └─► ViewModel ◄─ emit 수신
            │           └─► UI State = Success(remoteProducts) [업데이트]
            │
            └─► } catch (e: Exception) {
                ├─► emit(Result.Error(e))
                │   └─► ViewModel ◄─ emit 수신
                │       └─► UI State = Error(e)
                └─► }

```

### Repository 흐름 상세

```
Repository.getProducts()
    ▼
[Flow Builder 시작]
    ▼
Step 1: Loading 상태 방출
    emit(Result.Loading)

Step 2: 로컬 데이터 먼저 방출 (Offline-first)
    val cached = localDataSource.getProducts()
    emit(Result.Success(cached))

Step 3: 네트워크 데이터 가져오기
    try {
        val remote = networkDataSource.getProducts()
        localDataSource.saveProducts(remote)  // 캐시 업데이트
        emit(Result.Success(remote))         // 최신 데이터 방출
    } catch (e: Exception) {
        emit(Result.Error(e))                 // 에러 처리
    }

Step 4: Flow 종료
    .flowOn(ioDispatcher)  // IO 스레드에서 실행

```

---

## 디렉토리 구조

### Core 모듈 구조

```
core/
├── model/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/bup/ys/daitso/core/model/
│       ├── Product.kt           # 상품 도메인 모델
│       ├── CartItem.kt          # 카트 항목 모델
│       └── User.kt              # 사용자 모델
│
├── common/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/bup/ys/daitso/core/common/
│       ├── result/
│       │   └── Result.kt        # Result<T> 래퍼
│       ├── di/
│       │   └── Dispatcher.kt    # @Dispatcher Annotation
│       └── logger/
│           └── Logger.kt        # Logger 유틸리티
│
├── designsystem/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/bup/ys/daitso/core/designsystem/
│       ├── theme/
│       │   ├── Color.kt         # Material3 색상
│       │   ├── Typography.kt    # 타이포그래피
│       │   ├── Shape.kt         # 형태(radius)
│       │   └── DaitsoTheme.kt   # 메인 테마
│       └── components/
│           ├── DaitsoButton.kt
│           ├── DaitsoTextField.kt
│           ├── DaitsoLoadingIndicator.kt
│           └── DaitsoErrorView.kt
│
├── network/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/bup/ys/daitso/core/network/
│       ├── api/
│       │   └── DaitsoApiService.kt   # Retrofit 인터페이스
│       ├── source/
│       │   └── NetworkDataSource.kt
│       └── di/
│           └── NetworkModule.kt      # Hilt 모듈
│
├── database/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/bup/ys/daitso/core/database/
│       ├── entity/
│       │   └── CartItemEntity.kt     # Room 엔티티
│       ├── dao/
│       │   └── CartDao.kt            # DAO 인터페이스
│       ├── DaitsoDatabase.kt         # Database 클래스
│       └── di/
│           └── DatabaseModule.kt     # Hilt 모듈
│
└── data/
    ├── build.gradle.kts
    └── src/main/kotlin/com/bup/ys/daitso/core/data/
        ├── repository/
        │   ├── ProductRepository.kt       # 인터페이스
        │   └── ProductRepositoryImpl.kt   # 구현체
        └── di/
            └── DataModule.kt             # Hilt 모듈

```

---

## 핵심 컴포넌트

### 1. Result<T> - 비동기 작업 결과 래퍼

**목적**: Success, Error, Loading 상태를 타입 안전하게 표현

**위치**: `core/common/src/main/kotlin/com/bup/ys/daitso/core/common/result/Result.kt`

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// 사용 예
when (result) {
    is Result.Success -> UI.show(result.data)
    is Result.Error -> UI.showError(result.exception)
    Result.Loading -> UI.showLoading()
}
```

### 2. @Dispatcher - Coroutine Dispatcher 주입

**목적**: 테스트 시 Dispatcher 교체 용이하게 함

**위치**: `core/common/src/main/kotlin/com/bup/ys/daitso/core/common/di/Dispatcher.kt`

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(val dispatcher: DaitsoDispatchers)

enum class DaitsoDispatchers {
    IO,
    Default,
    Main
}

// 사용 예
class ProductRepositoryImpl @Inject constructor(
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {
    // ...
}
```

### 3. DaitsoTheme - Material3 테마

**목적**: 앱 전체 UI 스타일 일관성 유지

**위치**: `core/designsystem/src/main/kotlin/com/bup/ys/daitso/core/designsystem/theme/DaitsoTheme.kt`

```kotlin
@Composable
fun DaitsoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DaitsoTypography,
        shapes = DaitsoShapes,
        content = content
    )
}
```

### 4. DaitsoApiService - REST API 정의

**목적**: Retrofit을 통한 REST API 엔드포인트 정의

**위치**: `core/network/src/main/kotlin/com/bup/ys/daitso/core/network/api/DaitsoApiService.kt`

```kotlin
interface DaitsoApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Product
}
```

### 5. DaitsoDatabase - Room 데이터베이스

**목적**: 로컬 데이터 저장소 구성

**위치**: `core/database/src/main/kotlin/com/bup/ys/daitso/core/database/DaitsoDatabase.kt`

```kotlin
@Database(
    entities = [CartItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DaitsoDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}
```

---

## App Integration Layer (SPEC-ANDROID-INTEGRATION-003)

### 개요

**App Integration Layer**는 모든 Feature 모듈들을 하나의 응집력 있는 애플리케이션으로 통합하는 계층입니다.

```
┌──────────────────────────────────────────────────────────┐
│            App Module (:app)                             │
│  ┌─────────────────────────────────────────────────────┐│
│  │  App Integration Layer (Navigation + DI)            ││
│  │  ├─ DaitsoApplication (@HiltAndroidApp)             ││
│  │  ├─ MainActivity (Entry Point)                      ││
│  │  ├─ DaitsoNavHost (Navigation Graph)                ││
│  │  └─ NavRoutes (Route Constants)                     ││
│  └─────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────┘
         ▲                                      ▲
         │ Feature 모듈들                       │ Core 모듈들
         │                                      │
    ┌────┴────┬────────┬──────────┐       ┌────┴─────────┐
    │          │        │          │       │              │
    ▼          ▼        ▼          ▼       ▼              ▼
 Home      Detail     Cart     (Future)  Common    Data/Network
 Module    Module     Module    Modules  Module      Modules
```

### 핵심 컴포넌트

#### 1. MainActivity - 애플리케이션 진입점

**파일**: `app/src/main/kotlin/.../MainActivity.kt`

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaitsoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DaitsoNavHost()
                }
            }
        }
    }
}
```

**역할**:
- ✅ Compose 런타임 초기화
- ✅ 테마 적용 (DaitsoTheme)
- ✅ 네비게이션 그래프 호스팅 (DaitsoNavHost)
- ✅ Hilt 의존성 주입 (@AndroidEntryPoint)

#### 2. DaitsoApplication - Hilt 구성

**파일**: `app/src/main/kotlin/.../DaitsoApplication.kt`

```kotlin
@HiltAndroidApp
class DaitsoApplication : Application() {
    // Hilt 자동 초기화
}
```

**역할**:
- ✅ Hilt 의존성 주입 컨테이너 초기화
- ✅ @Module 클래스 등록
- ✅ Singleton 인스턴스 생성 및 관리

#### 3. DaitsoNavHost - 네비게이션 그래프

**파일**: `app/src/main/kotlin/.../navigation/NavigationHost.kt`

```kotlin
@Composable
fun DaitsoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        // Home 라우트
        composable(route = NavRoutes.HOME) { ... }

        // ProductDetail 라우트
        composable(
            route = NavRoutes.PRODUCT_DETAIL,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { ... }

        // Cart 라우트
        composable(route = NavRoutes.CART) { ... }
    }
}
```

**역할**:
- ✅ 세 가지 주요 라우트 정의 (HOME, PRODUCT_DETAIL, CART)
- ✅ 라우트 매개변수 관리 (productId)
- ✅ ViewModel 주입 (@hiltViewModel())
- ✅ 화면 간 네비게이션 제어

#### 4. NavRoutes - 라우트 상수

**파일**: `app/src/main/kotlin/.../navigation/NavigationHost.kt`

```kotlin
object NavRoutes {
    const val HOME = "home"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val CART = "cart"

    fun productDetail(productId: String) = "product_detail/$productId"
}
```

**역할**:
- ✅ 타입 안전한 라우트 상수 제공
- ✅ 라우트 빌더 함수 (productDetail)
- ✅ 하드코딩 문자열 제거

### 네비게이션 흐름

```
HOME Screen
   │
   ├─ 사용자가 상품 클릭
   │
   ▼
navController.navigate(NavRoutes.productDetail(productId))
   │
   ├─ 라우트: "product_detail/P123"
   ├─ 백스택: [HOME, PRODUCT_DETAIL/P123]
   │
   ▼
PRODUCT_DETAIL Screen
   │
   ├─ LaunchedEffect(productId)
   ├─ viewModel.submitEvent(LoadProduct(productId))
   │
   ▼
상품 데이터 표시
   │
   ├─ 사용자가 "장바구니 보기" 클릭
   │
   ▼
navController.navigate(NavRoutes.CART)
   │
   ├─ 백스택: [HOME, PRODUCT_DETAIL/P123, CART]
   │
   ▼
CART Screen
   │
   ├─ 사용자가 뒤로 가기
   │
   ▼
navController.popBackStack()
   │
   ├─ 백스택: [HOME, PRODUCT_DETAIL/P123]
   │
   ▼
PRODUCT_DETAIL Screen (상태 복원)
```

### 이벤트 처리 패턴

각 화면에서의 이벤트 처리는 MVI 패턴을 따릅니다:

```
사용자 상호작용 (Click)
    │
    ▼
Composable 콜백
    │
    ├─ coroutineScope.launch {
    │    viewModel.submitEvent(intent)
    │  }
    │
    ▼
ViewModel.submitEvent()
    │
    ├─ 이벤트 처리
    ├─ 상태 업데이트
    ├─ Side Effect 방출
    │
    ▼
StateFlow 방출
    │
    ├─ collectAsState()로 구독
    │
    ▼
UI 재구성 및 업데이트
```

### 의존성 주입 구성

**파일**: `app/build.gradle.kts`

```kotlin
dependencies {
    // Compose
    implementation(libs.androidx.compose.bom)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.compose.navigation)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Feature 모듈들
    implementation(project(":feature:home"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:cart"))

    // Core 모듈들
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}
```

### AndroidManifest.xml 구성

```xml
<application
    android:name=".DaitsoApplication"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name">

    <activity
        android:name=".MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

</application>
```

### 핵심 특징

1. **선언적 네비게이션** - 라우트를 상수로 정의
2. **타입 안전성** - NavRoutes 상수로 오류 방지
3. **의존성 주입** - Hilt로 자동 주입 관리
4. **MVI 패턴** - 통합된 이벤트 처리
5. **컴포지션** - Jetpack Compose로 UI 구성

### 확장성

새로운 화면을 추가하려면:

```kotlin
// 1. NavRoutes에 상수 추가
object NavRoutes {
    const val NEW_SCREEN = "new_screen/{param}"
    fun newScreen(param: String) = "new_screen/$param"
}

// 2. DaitsoNavHost에 라우트 추가
composable(
    route = NavRoutes.NEW_SCREEN,
    arguments = listOf(navArgument("param") { type = NavType.StringType })
) { ... }

// 3. 네비게이션 호출
navController.navigate(NavRoutes.newScreen(paramValue))
```

---

## 확장 포인트

### 1. Feature 모듈 추가

새로운 기능(예: Products Feature)을 추가하려면:

```
:feature:products (새 모듈)
    ├─► :core:model
    ├─► :core:common
    ├─► :core:designsystem
    ├─► :core:data
    └─► build.gradle.kts
```

**build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.daitso.android.library.compose)
    alias(libs.plugins.daitso.android.hilt)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))

    // Compose & ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // ...
}
```

### 2. API Endpoint 추가

1. `DaitsoApiService.kt`에 새 메서드 추가:
```kotlin
@GET("new-endpoint/{id}")
suspend fun getNewData(@Path("id") id: String): NewDataModel
```

2. `NetworkDataSource.kt`에 메서드 추가:
```kotlin
interface NetworkDataSource {
    suspend fun getNewData(id: String): NewDataModel
}
```

3. Repository에서 사용:
```kotlin
override fun getNewData(id: String): Flow<Result<NewDataModel>> = flow {
    emit(Result.Loading)
    try {
        val remote = networkDataSource.getNewData(id)
        emit(Result.Success(remote))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}
```

### 3. Database 엔티티 추가

1. Entity 정의:
```kotlin
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val total: Double,
    val createdAt: Long
)
```

2. DAO 정의:
```kotlin
@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId")
    fun getUserOrders(userId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
}
```

3. Database에 추가:
```kotlin
@Database(
    entities = [CartItemEntity::class, OrderEntity::class],
    version = 2,
    exportSchema = true
)
abstract class DaitsoDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
}
```

---

## 성능 고려사항

### 1. 메모리 최적화

**Database 쿼리 최적화**:
```kotlin
// 나쁜 예: 모든 데이터 로드
@Query("SELECT * FROM products")
fun getAllProducts(): Flow<List<Product>>

// 좋은 예: 필요한 컬럼만 선택
@Query("SELECT id, name, price FROM products")
fun getProductSummaries(): Flow<List<ProductSummary>>
```

**Image Caching (Coil)**:
```kotlin
Image(
    painter = rememberAsyncImagePainter(
        model = product.imageUrl,
        contentScale = ContentScale.Crop
    ),
    contentDescription = null,
    modifier = Modifier.size(200.dp)
)
```

### 2. 네트워크 최적화

**Request 캐싱**:
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .cache(Cache(cacheDir, 10 * 1024 * 1024)) // 10MB 캐시
    .addNetworkInterceptor(HttpLoggingInterceptor())
    .build()
```

**Pagination (향후)**:
```kotlin
@GET("products")
suspend fun getProducts(
    @Query("page") page: Int,
    @Query("limit") limit: Int = 20
): PagedResponse<Product>
```

### 3. UI 렌더링 최적화

**LazyColumn 사용**:
```kotlin
@Composable
fun ProductList(products: List<Product>) {
    LazyColumn {
        items(products) { product ->
            ProductItem(product)
        }
    }
}
```

**State Hoisting (상태 끌어올리기)**:
```kotlin
@Composable
fun ProductsScreen(viewModel: ProductsViewModel) {
    val state by viewModel.state.collectAsState()

    // View가 아닌 상위에서 상태 관리
    ProductContent(state = state, onEvent = viewModel::handleEvent)
}

@Composable
fun ProductContent(state: ProductsState, onEvent: (ProductsEvent) -> Unit) {
    // UI 렌더링
}
```

---

## Phase 4: MVI UI 아키텍처

### 개요

Phase 4는 **MVI (Model-View-Intent) 패턴**의 핵심 UI 아키텍처를 구현합니다.
이는 모든 Feature 모듈이 상속할 기본 ViewModel, State 관리 메커니즘, 그리고 Type-safe Navigation을 제공합니다.

**관련 SPEC**: SPEC-ANDROID-MVI-002

### UI 상태 관리 흐름

```
┌──────────────┐
│ User Action  │
│ (클릭, 입력) │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│ UiEvent          │  submitEvent(event)
│ (사용자 의도)    │
└──────┬───────────┘
       │
       ▼
┌──────────────────────────┐
│ BaseViewModel            │
│ handleEvent(event)       │
│ - 비즈니스 로직 처리      │
└──────┬───────────────────┘
       │
       ▼
┌──────────────────┐
│ UiState          │  StateFlow<S>
│ (UI 상태)        │
└──────┬───────────┘
       │
       ▼
┌──────────────────────────┐
│ Compose Recomposition    │
│ (collectAsState())       │
└──────┬───────────────────┘
       │
       ▼
┌──────────────────────────┐
│ UiSideEffect             │  Channel<SE>
│ (한 번만 발생하는 효과)  │
│ - Toast                  │
│ - Navigation             │
│ - Dialog                 │
└──────────────────────────┘
```

### 핵심 컴포넌트

#### 1. BaseViewModel<S, E, SE>

**위치**: `/core/ui/src/main/kotlin/com/bup/ys/daitso/core/ui/base/BaseViewModel.kt`

**역할**: MVI 패턴의 기본 ViewModel 클래스

**구조**:
```kotlin
abstract class BaseViewModel<
    S : UiState,           // State type
    E : UiEvent,           // Event type
    SE : UiSideEffect      // Side Effect type
>(initialState: S) : ViewModel()
```

**주요 기능**:
- StateFlow 기반 상태 관리
- Channel 기반 이벤트 처리
- Coroutine scope 자동 관리
- Resource cleanup 자동화

#### 2. MVI 인터페이스들

**UiState** (마커 인터페이스)
```kotlin
interface UiState
```
- 목적: UI 상태를 나타내는 모든 타입의 기본 인터페이스
- 특징: sealed class로 정의되는 모든 상태는 이를 상속

**UiEvent** (마커 인터페이스)
```kotlin
interface UiEvent
```
- 목적: 사용자 상호작용과 시스템 이벤트 정의
- 특징: sealed class로 정의되는 모든 이벤트는 이를 상속

**UiSideEffect** (마커 인터페이스)
```kotlin
interface UiSideEffect
```
- 목적: 한 번만 발생해야 하는 부수 효과 정의
- 특징: Toast, Dialog, Navigation 등으로 사용

#### 3. Type-safe Navigation

**Routes.kt**:
```kotlin
@Serializable
sealed class AppRoute {
    @Serializable
    object Home : AppRoute()

    @Serializable
    data class ProductDetail(val productId: String) : AppRoute()

    @Serializable
    object Cart : AppRoute()
}
```

**특징**:
- Kotlin Serialization 지원
- Deep Link 호환성
- Jetpack Navigation과 완벽 통합
- Type-safe 파라미터 전달

### 구현 패턴

#### Feature ViewModel 예시

```kotlin
// State 정의
sealed class HomeUiState : UiState {
    data object Initial : HomeUiState()
    data object Loading : HomeUiState()
    data class Success(val products: List<Product>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// Event 정의
sealed class HomeUiEvent : UiEvent {
    data object OnLoad : HomeUiEvent()
    data object OnRefresh : HomeUiEvent()
    data class OnProductClicked(val productId: String) : HomeUiEvent()
}

// SideEffect 정의
sealed class HomeSideEffect : UiSideEffect {
    data class NavigateToDetail(val productId: String) : HomeSideEffect()
    data class ShowError(val message: String) : HomeSideEffect()
}

// ViewModel 구현
class HomeViewModel(
    private val repository: ProductRepository
) : BaseViewModel<HomeUiState, HomeUiEvent, HomeSideEffect>(
    initialState = HomeUiState.Initial
) {
    override suspend fun handleEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.OnLoad -> loadProducts()
            HomeUiEvent.OnRefresh -> refreshProducts()
            is HomeUiEvent.OnProductClicked -> {
                launchSideEffect(
                    HomeSideEffect.NavigateToDetail(event.productId)
                )
            }
        }
    }

    private suspend fun loadProducts() {
        updateState(HomeUiState.Loading)
        try {
            val products = repository.getProducts()
            updateState(HomeUiState.Success(products))
        } catch (e: Exception) {
            updateState(HomeUiState.Error(e.message ?: "오류 발생"))
        }
    }

    private suspend fun refreshProducts() {
        loadProducts()
    }
}
```

#### UI 통합 (Compose)

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val state by viewModel.uiState.collectAsState()

    // SideEffect 처리
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.NavigateToDetail -> {
                    navController.navigate(
                        AppRoute.ProductDetail(effect.productId)
                    )
                }
                is HomeSideEffect.ShowError -> {
                    // 토스트 또는 스낵바 표시
                }
            }
        }
    }

    // State 렌더링
    when (state) {
        HomeUiState.Initial -> {
            LaunchedEffect(Unit) {
                viewModel.submitEvent(HomeUiEvent.OnLoad)
            }
        }
        HomeUiState.Loading -> LoadingScreen()
        is HomeUiState.Success -> {
            val products = (state as HomeUiState.Success).products
            ProductList(
                products = products,
                onProductClick = { productId ->
                    viewModel.submitEvent(
                        HomeUiEvent.OnProductClicked(productId)
                    )
                }
            )
        }
        is HomeUiState.Error -> {
            val message = (state as HomeUiState.Error).message
            ErrorScreen(message)
        }
    }
}
```

### 의존성 흐름

```
Feature Modules
├── :feature:home
├── :feature:cart
└── :feature:detail
        │
        ▼
   :core:ui (MVI 기본 구조)
   ├── BaseViewModel
   ├── UiState/Event/SideEffect
   └── AppRoute (Navigation)
        │
        ├──► :core:model (도메인 모델)
        ├──► :core:data (Repository)
        ├──► :core:common (유틸리티)
        └──► :core:designsystem (UI 컴포넌트)
```

### 모듈 구조

```
:core:ui/src/main/kotlin/com/bup/ys/daitso/core/ui/
├── base/
│   └── BaseViewModel.kt              # MVI 기본 클래스 (151줄)
├── contract/
│   ├── UiState.kt                    # 마커 인터페이스
│   ├── UiEvent.kt                    # 마커 인터페이스
│   └── UiSideEffect.kt               # 마커 인터페이스
└── navigation/
    └── Routes.kt                     # Type-safe Navigation (45줄)

:core:ui/src/test/kotlin/com/bup/ys/daitso/core/ui/
├── base/
│   └── BaseViewModelTest.kt          # 12개 테스트 (212줄)
└── navigation/
    └── NavigationRoutesTest.kt       # 직렬화 테스트
```

### 테스트 전략

**BaseViewModel 테스트** (12개 케이스):
- 초기화 및 상태 검증
- 이벤트 제출 및 처리
- 상태 업데이트 검증
- 사이드 이펙트 발생
- 이벤트 순서 보장
- 동시 처리 안정성
- Resource cleanup

**Navigation 테스트**:
- Route 정의 검증
- 직렬화/역직렬화
- 파라미터 처리

### 코드 품질 메트릭

| 메트릭 | 값 | 평가 |
|--------|-----|------|
| 생산 코드 | 196줄 | 적절함 |
| 테스트 코드 | 104줄+ | 양호 |
| 테스트 케이스 | 12개+ | 충분함 |
| KDoc 커버리지 | 100% | 완벽 |
| 코드 커버리지 | 85%+ | 목표 달성 |

---

## Feature 모듈 구현 현황

### 구현 완료

#### :feature:cart - 장바구니 관리
- **상태**: Completed (2025-12-14)
- **SPEC**: SPEC-ANDROID-FEATURE-CART-001
- **테스트 커버리지**: 95%+
- **주요 기능**:
  - Room DB 통합 아이템 관리
  - MVI 아키텍처 적용
  - Flow 기반 반응형 UI
  - 수량 조절 및 가격 계산
- **문서**:
  - 전체 API 문서: `docs/MODULES.md`
  - Cart 상세 가이드: `feature/cart/README.md`

#### :feature:detail - 상품 상세 조회
- **상태**: Completed (2025-12-13)
- **SPEC**: SPEC-ANDROID-FEATURE-DETAIL-001
- **주요 기능**:
  - 상품 정보 상세 조회
  - 이미지 갤러리
  - 리뷰 및 평점
  - 장바구니 추가

---

## 마이그레이션 가이드

### 향후 확장 계획

#### 1단계: Feature 모듈 추가 (진행 중)
- `:feature:products` - 상품 목록, 상세 조회
- `:feature:checkout` - 결제 프로세스
- ✅ `:feature:cart` - 장바구니 관리 (완료)
- ✅ `:feature:detail` - 상품 상세 (완료)

#### 2단계: 고급 기능 (중기)
- `:feature:auth` - 인증 및 사용자 관리
- `:feature:orders` - 주문 조회 및 추적
- `:feature:search` - 검색 및 필터링

#### 3단계: 인프라 개선 (장기)
- `:core:analytics` - 분석 및 추적
- `:core:notification` - 푸시 알림
- `:core:security` - 보안 및 암호화

---

## 참고 자료

- [Android Jetpack 공식 가이드](https://developer.android.com/jetpack)
- [Compose Documentation](https://developer.android.com/jetpack/compose)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [MVI Pattern](https://medium.com/@shelajev/clean-architecture-with-kotlin-coroutines-and-mvvm-part-1-7d0ce2013d8f)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)

---

**아키텍처 문서 버전**: 1.0.1
**최종 검토**: 2025-12-14
**상태**: Active
**최신 업데이트**: Cart 모듈 (SPEC-ANDROID-FEATURE-CART-001) 완료 기록
