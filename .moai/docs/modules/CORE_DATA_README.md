# Core Data 모듈 (:core:data)

**Repository 패턴 구현 및 데이터 계층 모듈**

---

## 개요

`:core:data` 모듈은 **Repository 패턴**을 구현하여 네트워크와 로컬 데이터 소스를 조정합니다. **Offline-first 전략**을 통해 로컬 데이터베이스를 Single Source of Truth로 사용합니다.

### 모듈 특징

- ✅ Repository 패턴 구현
- ✅ Offline-first 데이터 동기화
- ✅ 네트워크와 로컬 데이터 조정
- ✅ Flow 기반 반응형 데이터 접근

---

## 모듈 구조

```
core/data/
├── build.gradle.kts
└── src/main/kotlin/com/bup/ys/daitso/core/data/
    ├── repository/
    │   ├── ProductRepository.kt
    │   └── ProductRepositoryImpl.kt
    └── di/
        └── DataModule.kt
```

---

## 주요 컴포넌트

### 1. Repository 인터페이스

```kotlin
interface ProductRepository {
    fun getProducts(): Flow<Result<List<Product>>>
    fun getProduct(id: String): Flow<Result<Product>>
    fun addToCart(product: Product): Flow<Result<Unit>>
}
```

**특징:**
- 비즈니스 로직 정의
- UI와 데이터 소스 간 추상화
- Flow 기반 반응형 인터페이스

---

### 2. Repository 구현

#### Offline-first 패턴

```kotlin
class ProductRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override fun getProducts(): Flow<Result<List<Product>>> = flow {
        // 1단계: 로딩 상태
        emit(Result.Loading())

        // 2단계: 로컬 캐시에서 데이터 로드
        try {
            val cachedProducts = localDataSource.getProducts()
            emit(Result.Success(cachedProducts))  // 즉시 UI에 반영
        } catch (e: Exception) {
            Logger.d(TAG, "No cached products available")
        }

        // 3단계: 네트워크에서 최신 데이터 동기화
        try {
            val remoteProducts = networkDataSource.getProducts()

            // 4단계: 로컬 캐시 업데이트
            localDataSource.saveProducts(remoteProducts)

            // 5단계: 최신 데이터 방출
            emit(Result.Success(remoteProducts))

            Logger.i(TAG, "Fetched ${remoteProducts.size} products from network")
        } catch (e: Exception) {
            // 6단계: 네트워크 실패해도 로컬 데이터 유지
            Logger.e(TAG, "Failed to fetch products from network", e)
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)

    override fun getProduct(id: String): Flow<Result<Product>> = flow {
        emit(Result.Loading())

        try {
            // 로컬에서 먼저 시도
            val cachedProduct = localDataSource.getProduct(id)
            emit(Result.Success(cachedProduct))
        } catch (e: Exception) {
            // 네트워크에서 조회
            try {
                val remoteProduct = networkDataSource.getProduct(id)
                localDataSource.saveProduct(remoteProduct)
                emit(Result.Success(remoteProduct))
            } catch (networkError: Exception) {
                emit(Result.Error(networkError))
            }
        }
    }.flowOn(ioDispatcher)

    override fun addToCart(product: Product): Flow<Result<Unit>> = flow {
        emit(Result.Loading())

        try {
            // 로컬에 먼저 저장
            localDataSource.addToCart(product)

            // 네트워크로 동기화
            networkDataSource.addToCart(product)

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)

    companion object {
        private const val TAG = "ProductRepository"
    }
}
```

---

### 3. 데이터 소스 인터페이스

#### NetworkDataSource

```kotlin
interface NetworkDataSource {
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: String): Product
    suspend fun addToCart(product: Product): CartItem
}
```

#### LocalDataSource

```kotlin
interface LocalDataSource {
    fun getProducts(): List<Product>
    suspend fun getProduct(id: String): Product
    suspend fun saveProducts(products: List<Product>)
    suspend fun saveProduct(product: Product)
    suspend fun addToCart(product: Product)
    fun getCartItems(): Flow<List<CartItem>>
}
```

---

### 4. DataModule - Hilt 바인딩

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    companion object {
        @Provides
        @Singleton
        @Dispatcher(DaitsoDispatchers.IO)
        fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @Singleton
        @Dispatcher(DaitsoDispatchers.Default)
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @Provides
        @Singleton
        @Dispatcher(DaitsoDispatchers.Main)
        fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    }
}
```

---

## Offline-first 동작 원리

### 시퀀스 다이어그램

```
사용자 요청
    │
    ├─→ [Repository] 로딩 상태 발행
    │
    ├─→ [로컬 DB] 캐시 데이터 확인
    │   ├─ 데이터 있음 → Success 발행 (즉시 UI 렌더링)
    │   └─ 데이터 없음 → 계속 진행
    │
    ├─→ [네트워크] API 호출 (백그라운드)
    │   ├─ 성공 → 로컬 DB 업데이트 → Success 발행
    │   └─ 실패 → Error 발행
    │
    └─→ [UI] 데이터 렌더링
```

### Result 발행 순서

```kotlin
// 사용자가 상품 목록 요청
getProducts()
    .collect { result ->
        when (result) {
            // 1번째: 로딩 상태
            is Result.Loading -> println("Loading...")

            // 2번째: 로컬 캐시 데이터 (있으면)
            is Result.Success -> println("Data: ${result.data}")  // 즉시 표시

            // 3번째: 네트워크 데이터 (다시 Success)
            is Result.Success -> println("Updated: ${result.data}")

            // 또는 네트워크 실패
            is Result.Error -> println("Error: ${result.exception}")
        }
    }
```

---

## 사용 예시

### ViewModel에서 사용

```kotlin
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts()
                .collect { result ->
                    _state.value = when (result) {
                        is Result.Loading -> UiState.Loading
                        is Result.Success -> UiState.Content(result.data)
                        is Result.Error -> UiState.Error(result.exception.message ?: "Unknown error")
                    }
                }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            productRepository.addToCart(product)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // 장바구니 추가 성공
                        }
                        is Result.Error -> {
                            // 에러 처리
                        }
                        else -> {}
                    }
                }
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    data class Content(val products: List<Product>) : UiState()
    data class Error(val message: String) : UiState()
}
```

### Composable에서 사용

```kotlin
@Composable
fun ProductScreen(
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    when (state) {
        is UiState.Loading -> {
            DaitsoLoadingIndicator()
        }
        is UiState.Content -> {
            val products = (state as UiState.Content).products
            ProductList(
                products = products,
                onProductClick = { product ->
                    viewModel.addToCart(product)
                }
            )
        }
        is UiState.Error -> {
            DaitsoErrorView(
                message = (state as UiState.Error).message,
                onRetry = { viewModel.loadProducts() }
            )
        }
    }
}
```

---

## 고급 패턴

### 1. Retry 로직

```kotlin
fun <T> getProductsWithRetry(): Flow<Result<List<Product>>> = flow {
    var attempt = 0
    var lastError: Exception? = null

    while (attempt < MAX_RETRIES) {
        try {
            emit(Result.Loading())
            val products = networkDataSource.getProducts()
            emit(Result.Success(products))
            return@flow
        } catch (e: Exception) {
            lastError = e
            attempt++
            if (attempt < MAX_RETRIES) {
                delay(RETRY_DELAY * attempt)  // Exponential backoff
            }
        }
    }

    emit(Result.Error(lastError ?: Exception("Unknown error")))
}.flowOn(ioDispatcher)

private companion object {
    const val MAX_RETRIES = 3
    const val RETRY_DELAY = 1000L
}
```

### 2. 캐시 무효화

```kotlin
private val cacheInvalidationTime = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

fun getProductsWithCacheControl(): Flow<Result<List<Product>>> = flow {
    val cachedProducts = localDataSource.getProducts()
    emit(Result.Success(cachedProducts))

    // 캐시 무효화 신호 대기
    cacheInvalidationTime.collect {
        val freshProducts = networkDataSource.getProducts()
        localDataSource.saveProducts(freshProducts)
        emit(Result.Success(freshProducts))
    }
}.flowOn(ioDispatcher)

fun invalidateCache() {
    cacheInvalidationTime.tryEmit(Unit)
}
```

### 3. 동시 요청 처리

```kotlin
fun getProductsAndUser(): Flow<Result<Pair<List<Product>, User>>> = flow {
    emit(Result.Loading())

    try {
        val products = async { networkDataSource.getProducts() }
        val user = async { networkDataSource.getUser() }

        emit(Result.Success(Pair(products.await(), user.await())))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}.flowOn(ioDispatcher)
```

---

## 테스트

```kotlin
class ProductRepositoryTest {
    private val networkDataSource = mockk<NetworkDataSource>()
    private val localDataSource = mockk<LocalDataSource>()
    private val repository = ProductRepositoryImpl(
        networkDataSource = networkDataSource,
        localDataSource = localDataSource,
        ioDispatcher = Dispatchers.Unconfined
    )

    @Test
    fun `getProducts returns local data first`() = runBlocking {
        val cachedProducts = listOf(Product(...))
        coEvery { localDataSource.getProducts() } returns cachedProducts

        repository.getProducts()
            .collect { result ->
                if (result is Result.Success) {
                    assertEquals(cachedProducts, result.data)
                }
            }
    }

    @Test
    fun `getProducts syncs with network`() = runBlocking {
        val remoteProducts = listOf(Product(...))
        coEvery { localDataSource.getProducts() } returns emptyList()
        coEvery { networkDataSource.getProducts() } returns remoteProducts
        coEvery { localDataSource.saveProducts(remoteProducts) } returns Unit

        var finalResult: List<Product>? = null
        repository.getProducts()
            .collect { result ->
                if (result is Result.Success) {
                    finalResult = result.data
                }
            }

        assertEquals(remoteProducts, finalResult)
        coVerify { localDataSource.saveProducts(remoteProducts) }
    }

    @Test
    fun `getProducts handles network error gracefully`() = runBlocking {
        val exception = Exception("Network error")
        coEvery { localDataSource.getProducts() } throws exception
        coEvery { networkDataSource.getProducts() } throws exception

        repository.getProducts()
            .collect { result ->
                assertTrue(result is Result.Error)
            }
    }
}
```

---

## 의존성

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.daitso.android.library)
    alias(libs.plugins.daitso.android.hilt)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
}
```

---

## 모범 사례

### 1. 단일 책임 원칙

```kotlin
// Good: Repository는 데이터 조정만
class ProductRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource
) : ProductRepository

// Bad: Repository가 UI 상태 관리까지
class BadProductRepository {
    val uiState = MutableStateFlow<UiState>(UiState.Loading)
}
```

### 2. Flow 구독 취소 관리

```kotlin
// Good: viewModelScope 사용
viewModelScope.launch {
    repository.getProducts().collect { /* ... */ }
}  // 화면 종료시 자동 취소

// Bad: 수동 관리
GlobalScope.launch {
    repository.getProducts().collect { /* ... */ }
}
```

### 3. 에러 처리

```kotlin
// Good: Result 타입으로 상태 관리
fun getProducts(): Flow<Result<List<Product>>>

// Bad: 예외 던지기
fun getProducts(): List<Product>  // 예외 처리 어려움
```

---

## 참고

- [Repository 패턴](https://developer.android.com/codelabs/android-architecture-basics)
- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [Flow API](https://kotlinlang.org/docs/flow.html)
- [SPEC-ANDROID-INIT-001](../../specs/SPEC-ANDROID-INIT-001/spec.md)

---

**최종 업데이트**: 2025-11-28
**SPEC 기반**: SPEC-ANDROID-INIT-001
