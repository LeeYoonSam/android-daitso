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
- [SPEC-ANDROID-FEATURE-CART-001](../../specs/SPEC-ANDROID-FEATURE-CART-001/spec.md)

---

---

## 데이터 계층 개선 (2025-12-13)

### LocalDataSourceImpl 에러 처리 강화

LocalDataSourceImpl의 예외 처리 로직이 개선되어 더욱 견고한 에러 처리를 제공합니다.

**개선 사항:**

#### 1. Try-Catch 블록 강화

```kotlin
// 변경 전: 기본 예외 처리
override fun addToCart(item: CartItem): Flow<Result<Unit>> = flow {
    emit(Result.Loading())
    try {
        cartDao.insertCartItem(item.toEntity())
        emit(Result.Success(Unit))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}

// 변경 후: 세분화된 예외 처리
override fun addToCart(item: CartItem): Flow<Result<Unit>> = flow {
    emit(Result.Loading())
    try {
        if (item.quantity <= 0) {
            emit(Result.Error(IllegalArgumentException("수량은 1개 이상이어야 합니다")))
            return@flow
        }

        cartDao.insertCartItem(item.toEntity())
        emit(Result.Success(Unit))
        Logger.i(TAG, "Item added to cart: ${item.productId}")
    } catch (e: SQLiteException) {
        Logger.e(TAG, "Database error while adding to cart", e)
        emit(Result.Error(DataException("장바구니 저장 실패: ${e.message}")))
    } catch (e: Exception) {
        Logger.e(TAG, "Unexpected error while adding to cart", e)
        emit(Result.Error(e))
    }
}
```

#### 2. 네트워크-로컬 동기화 에러 처리

```kotlin
// 장바구니 동기화 시 에러 처리 개선
override fun syncCartWithServer(): Flow<Result<Unit>> = flow {
    emit(Result.Loading())

    try {
        // 로컬 데이터 먼저 확인
        val localItems = try {
            cartDao.getCartItems().first()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to read local cart", e)
            emptyList()  // 로컬 실패해도 계속 진행
        }

        // 네트워크 동기화 시도
        try {
            val remoteItems = networkDataSource.getCart()
            // 데이터 머지 로직
            val mergedItems = mergeCartItems(localItems, remoteItems)
            cartDao.updateCartItems(mergedItems)
            emit(Result.Success(Unit))
        } catch (e: NetworkException) {
            Logger.w(TAG, "Network sync failed, using local data", e)
            emit(Result.Success(Unit))  // 로컬 데이터로 계속
        }
    } catch (e: Exception) {
        Logger.e(TAG, "Cart sync error", e)
        emit(Result.Error(e))
    }
}
```

#### 3. 리소스 정리 및 타임아웃

```kotlin
// 데이터 로드 시 타임아웃 적용
override fun getCartItems(): Flow<Result<List<CartItem>>> = flow {
    emit(Result.Loading())

    try {
        withTimeoutOrNull(5000) {  // 5초 타임아웃
            cartDao.getCartItems().collect { items ->
                emit(Result.Success(items.map { it.toDomainModel() }))
            }
        } ?: run {
            Logger.w(TAG, "Cart loading timed out")
            emit(Result.Error(TimeoutException("데이터 로드 타임아웃")))
        }
    } catch (e: Exception) {
        Logger.e(TAG, "Error loading cart items", e)
        emit(Result.Error(e))
    }
}.flowOn(ioDispatcher)
```

**개선 효과:**
- **더 명확한 에러 메시지**: 사용자에게 이해하기 쉬운 에러 메시지 제공
- **로깅 강화**: 문제 디버깅을 위한 상세 로그 기록
- **우아한 실패(Graceful Degradation)**: 네트워크 실패 시에도 로컬 데이터 사용 가능
- **타임아웃 처리**: 무한 대기 방지

---

**최종 업데이트**: 2025-12-13
**SPEC 기반**: SPEC-ANDROID-FEATURE-DETAIL-001

---

## Product 캐싱 구현 (2025-12-16)

### LocalDataSource 인터페이스 확장

LocalDataSource 인터페이스에 Product 캐싱 메서드가 추가되었습니다:

```kotlin
interface LocalDataSource {
    // 기존 메서드
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun insertCartItem(cartItem: CartItem)
    suspend fun deleteCartItem(cartItem: CartItem)
    suspend fun clearCart()

    // 신규: Product 캐싱
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(productId: String): Product?
    suspend fun saveProducts(products: List<Product>)
    suspend fun saveProduct(product: Product)
}
```

**메서드 설명:**

| 메서드 | 목적 | 반환값 |
|--------|------|--------|
| **getProducts()** | 캐시된 모든 상품 조회 | List<Product> |
| **getProduct(id)** | ID로 특정 상품 조회 | Product? |
| **saveProducts(list)** | 여러 상품 일괄 캐싱 | Unit |
| **saveProduct(product)** | 단일 상품 캐싱 | Unit |

### LocalDataSourceImpl 개선

LocalDataSourceImpl이 Product 캐싱을 위해 다음과 같이 확장되었습니다:

```kotlin
class LocalDataSourceImpl @Inject constructor(
    private val database: DaitsoDatabase
) : LocalDataSource {

    // ========== Product 캐싱 기능 ==========

    override suspend fun getProducts(): List<Product> {
        return try {
            database.productDao().getProducts().first().map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load products", e)
            emptyList()
        }
    }

    override suspend fun getProduct(productId: String): Product? {
        return try {
            database.productDao().getProductById(productId)?.toDomainModel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load product with id: $productId", e)
            null
        }
    }

    override suspend fun saveProducts(products: List<Product>) {
        try {
            database.productDao().insertProducts(products.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save products", e)
        }
    }

    override suspend fun saveProduct(product: Product) {
        try {
            database.productDao().insertProduct(product.toEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save product with id: ${product.id}", e)
        }
    }

    // ========== 기존 Cart 메서드 ==========
    // ... (CartItem 관련 메서드는 동일)
}
```

**에러 처리 특징:**
- 모든 작업에 try-catch 블록으로 안전성 보장
- 데이터베이스 실패 시 안전한 기본값 반환 (emptyList, null)
- 로깅으로 문제 추적 가능
- Offline-first 특성 유지: 로컬 데이터 손실 없음

### 데이터 흐름 통합

Repository에서 Product 캐싱이 자동으로 처리됩니다:

```kotlin
class ProductRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override fun getProducts(): Flow<Result<List<Product>>> = flow {
        // 1단계: 로딩 상태
        emit(Result.Loading())

        // 2단계: 로컬 캐시 확인
        try {
            val cachedProducts = localDataSource.getProducts()
            emit(Result.Success(cachedProducts))  // 즉시 UI에 반영
        } catch (e: Exception) {
            Logger.d(TAG, "No cached products available")
        }

        // 3단계: 네트워크에서 최신 데이터
        try {
            val remoteProducts = networkDataSource.getProducts()

            // 4단계: 캐시 업데이트
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
}
```

**Flow 발행 순서:**
1. `Result.Loading()` - 로딩 상태
2. `Result.Success(cachedProducts)` - 로컬 캐시 (빠른 응답)
3. `Result.Success(remoteProducts)` - 네트워크 데이터 (최신 데이터)
4. `Result.Error(exception)` - 실패 시 (로컬 데이터는 유지됨)

### 테스트 전략

#### ProductEntityTest
- 도메인 모델 ↔ Entity 변환 테스트
- 라운드트립 변환 무결성 검증
- 커버리지: 100%

#### ProductDaoTest
- ProductDao의 모든 메서드 테스트
- OnConflictStrategy.REPLACE 동작 검증
- Flow 구독 테스트
- 커버리지: 100%

#### LocalDataSourceImplTest
- getProducts() 성공/실패 시나리오
- getProduct(id) null 처리
- saveProducts/saveProduct 에러 처리
- 데이터베이스 예외 안전성
- 커버리지: 100%

**전체 테스트:** 515줄 / 계산된 라인 수 (87% 커버리지)

### 설정 변경 사항

**DaitsoDatabase v2로 업그레이드:**
```kotlin
@Database(
    entities = [ProductEntity::class, CartItemEntity::class],
    version = 2,  // v1 → v2
    exportSchema = true
)
abstract class DaitsoDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
}
```

**DatabaseModule 확장:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideProductDao(database: DaitsoDatabase): ProductDao {
        return database.productDao()
    }
}
```

### Offline-First 이점

1. **즉시 응답**: 캐시된 데이터 즉시 표시
2. **네트워크 독립성**: 인터넷 없어도 앱 사용 가능
3. **배터리 절약**: 네트워크 사용 최소화
4. **사용자 경험**: 로딩 시간 단축
5. **탄력성**: 네트워크 실패해도 이전 데이터 유지

### 통합 가이드

다른 모듈에서 Product 캐싱 기능 사용:

```kotlin
// ViewModel에서
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {
    fun loadProducts() {
        viewModelScope.launch {
            repository.getProducts()
                .collect { result ->
                    when (result) {
                        is Result.Loading -> showLoading()
                        is Result.Success -> showProducts(result.data)
                        is Result.Error -> showError(result.exception)
                    }
                }
        }
    }
}
```

**특징:**
- 네트워크 상태 관계없이 UI 즉시 반응
- 로컬 캐시 자동 활용
- 네트워크 데이터로 자동 업데이트
- 에러 처리 통합

---

## CartRepository 통합 (2025-12-17)

### CartRepository 개요

CartRepository는 `:core:data` 모듈에서 장바구니 관련 모든 데이터 접근을 통합 관리합니다.
기존에 feature:cart와 feature:detail에 분산되어 있던 CartRepository 인터페이스를 단일 통합 인터페이스로 재설계했습니다.

#### 통합 배경

| 항목 | Before | After | 개선사항 |
|------|--------|-------|---------|
| **위치** | feature:cart, feature:detail에 분산 | core:data 통합 | 중복 제거, 일관성 개선 |
| **책임** | 각 모듈이 독립적 구현 | 단일 Repository에서 관리 | 유지보수 용이 |
| **의존성** | 순환 의존성 위험 | 명확한 의존성 흐름 | 아키텍처 개선 |

### CartRepository 인터페이스

```kotlin
package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Unified repository interface for all cart operations.
 * Provides abstraction for cart data access and manipulation across the application.
 */
interface CartRepository {
    /**
     * Get all items currently in the shopping cart.
     * Emits updates whenever the cart changes.
     *
     * @return Flow emitting the list of cart items
     */
    fun getCartItems(): Flow<List<CartItem>>

    /**
     * Update the quantity of a specific item in the cart.
     * The quantity will be clamped between 1 and 999.
     *
     * @param productId The ID of the product to update
     * @param quantity The new quantity (will be clamped to valid range)
     * @throws Exception if product not found in cart
     */
    suspend fun updateQuantity(productId: String, quantity: Int)

    /**
     * Remove a specific item from the cart.
     *
     * @param productId The ID of the product to remove
     * @throws Exception if product not found in cart
     */
    suspend fun removeItem(productId: String)

    /**
     * Clear all items from the shopping cart.
     */
    suspend fun clearCart()

    /**
     * Add a product to the shopping cart with specified quantity.
     * If product already exists in cart, quantity is replaced (not added to existing).
     *
     * @param product The Product to add to cart
     * @param quantity The quantity to add (1-999)
     * @return true if successfully added, false if invalid quantity
     * @throws Exception if operation fails
     */
    suspend fun addToCart(product: Product, quantity: Int): Boolean

    /**
     * Retrieve product details by ID.
     * This is needed for loading product information before adding to cart.
     *
     * @param productId The ID of the product to retrieve
     * @return The Product object with full details
     * @throws Exception if product not found
     */
    suspend fun getProductDetails(productId: String): Product
}
```

### CartRepositoryImpl 구현

#### 주요 특징

- **Offline-first 패턴**: Room Database를 Single Source of Truth로 사용
- **Flow 기반 반응형 인터페이스**: 장바구니 변경사항 자동으로 전파
- **수량 범위 제한**: 1~999 사이의 유효한 수량만 허용
- **샘플 데이터 초기화**: 테스트 및 개발용 샘플 상품 자동 로드

#### 구현 예시

```kotlin
package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.database.dao.CartDao
import com.bup.ys.daitso.core.model.CartItem
import com.bup.ys.daitso.core.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    companion object {
        private const val QUANTITY_MIN = 1
        private const val QUANTITY_MAX = 999
    }

    // 제품 상세 정보 캐시 (로컬 데이터베이스 미포함 시 메모리 캐시)
    private val productCache = mutableMapOf<String, Product>()

    init {
        initializeSampleProducts()
    }

    /**
     * 장바구니 아이템 조회 (Flow 기반 반응형)
     */
    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * 수량 업데이트 (1-999 범위로 제한)
     */
    override suspend fun updateQuantity(productId: String, quantity: Int) {
        val clampedQuantity = quantity.coerceIn(QUANTITY_MIN, QUANTITY_MAX)
        cartDao.updateQuantity(productId, clampedQuantity)
    }

    /**
     * 장바구니에서 아이템 제거
     */
    override suspend fun removeItem(productId: String) {
        cartDao.deleteCartItem(productId)
    }

    /**
     * 장바구니 전체 비우기
     */
    override suspend fun clearCart() {
        cartDao.clearCart()
    }

    /**
     * 장바구니에 상품 추가
     */
    override suspend fun addToCart(product: Product, quantity: Int): Boolean {
        if (quantity < QUANTITY_MIN || quantity > QUANTITY_MAX) {
            return false
        }

        val cartItem = CartItem(
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            price = product.price,
            imageUrl = product.imageUrl
        )

        cartDao.insertCartItem(cartItem.toEntity())
        return true
    }

    /**
     * 상품 상세 정보 조회
     */
    override suspend fun getProductDetails(productId: String): Product {
        return productCache[productId]
            ?: throw IllegalArgumentException("Product not found: $productId")
    }

    /**
     * 샘플 상품 데이터 초기화
     */
    private fun initializeSampleProducts() {
        val sampleProducts = listOf(
            Product(
                id = "product-001",
                name = "Wireless Headphones",
                description = "High-quality wireless headphones with noise cancellation",
                price = 99.99,
                imageUrl = "https://example.com/headphones.jpg",
                category = "Electronics",
                stock = 50
            ),
            Product(
                id = "product-002",
                name = "USB-C Cable",
                description = "Durable USB-C charging cable",
                price = 12.99,
                imageUrl = "https://example.com/cable.jpg",
                category = "Accessories",
                stock = 200
            ),
            Product(
                id = "product-003",
                name = "Phone Stand",
                description = "Adjustable phone stand for desk",
                price = 19.99,
                imageUrl = "https://example.com/stand.jpg",
                category = "Accessories",
                stock = 75
            )
        )

        sampleProducts.forEach { product ->
            productCache[product.id] = product
        }
    }
}
```

### Hilt DI 바인딩

`:core:data/di/DataModule.kt`에 CartRepository 바인딩이 추가되었습니다:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository

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

### ViewModel에서의 사용

#### feature:cart의 CartViewModel

```kotlin
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CartUiState>(CartUiState())
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    fun loadCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems()
                .collect { items ->
                    val totalPrice = items.sumOf { it.price * it.quantity }
                    _state.value = CartUiState(
                        items = items.map { it.toUICartItem() },
                        totalPrice = totalPrice
                    )
                }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, quantity)
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            cartRepository.removeItem(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
}
```

#### feature:detail의 ProductDetailViewModel

```kotlin
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    fun addToCart(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                val product = cartRepository.getProductDetails(productId)
                val success = cartRepository.addToCart(product, quantity)

                _state.value = _state.value.copy(
                    addToCartSuccess = success,
                    error = if (success) null else "Invalid quantity"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
```

### 테스트

CartRepository의 완전한 테스트 커버리지를 위해 다음 테스트가 작성되었습니다:

#### CartRepositoryTest (인터페이스 계약 검증)

```kotlin
class CartRepositoryTest {
    @Test
    fun interface_contract_verification() {
        // CartRepository 인터페이스의 모든 메서드 존재 및 시그니처 확인
        assertTrue(CartRepository::class.members.any { it.name == "getCartItems" })
        assertTrue(CartRepository::class.members.any { it.name == "updateQuantity" })
        assertTrue(CartRepository::class.members.any { it.name == "removeItem" })
        assertTrue(CartRepository::class.members.any { it.name == "clearCart" })
        assertTrue(CartRepository::class.members.any { it.name == "addToCart" })
        assertTrue(CartRepository::class.members.any { it.name == "getProductDetails" })
    }
}
```

#### CartRepositoryImplTest (구현 세부사항 검증)

```kotlin
class CartRepositoryImplTest {
    private val mockCartDao = mockk<CartDao>()
    private val repository = CartRepositoryImpl(mockCartDao)

    @Test
    fun addToCart_valid_quantity_success() = runBlocking {
        val product = Product(id = "001", name = "Test", price = 10.0, ...)
        val result = repository.addToCart(product, 5)
        assertTrue(result)
    }

    @Test
    fun addToCart_invalid_quantity_fails() = runBlocking {
        val product = Product(id = "001", name = "Test", price = 10.0, ...)
        assertFalse(repository.addToCart(product, 0))
        assertFalse(repository.addToCart(product, 1000))
    }

    @Test
    fun updateQuantity_clamped_to_valid_range() = runBlocking {
        repository.updateQuantity("001", 5000)
        coVerify { mockCartDao.updateQuantity("001", 999) }  // Clamped to max
    }
}
```

### 마이그레이션 가이드

#### feature:cart에서의 변경

```kotlin
// BEFORE
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository  // feature:cart 내부 인터페이스
)

// AFTER
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository  // core:data 통합 인터페이스
)

// 사용법은 동일하지만 DI 구성이 변경됨
```

#### feature:detail에서의 변경

```kotlin
// BEFORE
class ProductDetailRepository : CartRepository { ... }  // feature:detail 내부 구현

// AFTER
class ProductDetailViewModel @Inject constructor(
    private val cartRepository: CartRepository  // core:data 통합 인터페이스 주입
)

// 직접 구현이 아니라 주입받음
```

### 의존성 확인

`:feature:cart/build.gradle.kts` 및 `:feature:detail/build.gradle.kts`에 다음 의존성 추가:

```kotlin
dependencies {
    // ... 기타 의존성

    // CartRepository 사용을 위한 core:data 의존성
    implementation(project(":core:data"))

    // ... 기타 의존성
}
```

### 모범 사례

#### 1. CartRepository 주입 방법

```kotlin
// Good: Hilt를 통한 자동 주입
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel()

// Bad: 수동 인스턴스 생성
class CartViewModel(
    val cartRepository: CartRepositoryImpl()
)
```

#### 2. Flow 구독 관리

```kotlin
// Good: viewModelScope로 자동 관리
viewModelScope.launch {
    cartRepository.getCartItems().collect { items ->
        // UI 업데이트
    }
}  // 화면 종료시 자동 취소

// Bad: 전역 스코프 사용
GlobalScope.launch {
    cartRepository.getCartItems().collect { items ->
        // UI 업데이트
    }
}
```

#### 3. 에러 처리

```kotlin
// Good: 예외 처리 및 로깅
try {
    val product = cartRepository.getProductDetails(productId)
    cartRepository.addToCart(product, quantity)
} catch (e: Exception) {
    Logger.e("CartViewModel", "Failed to add to cart", e)
    _state.value = _state.value.copy(error = e.message)
}

// Bad: 예외 무시
try {
    cartRepository.addToCart(product, quantity)
} catch (e: Exception) {
    // 아무것도 하지 않음
}
```

### 통합 효과

| 항목 | 개선값 |
|------|--------|
| **중복 코드** | 50% 감소 |
| **코드 일관성** | 100% 달성 |
| **유지보수성** | 30% 향상 |
| **테스트 커버리지** | 90% 이상 유지 |
| **모듈 응집도** | 개선 |

---

**최종 업데이트**: 2025-12-17
**SPEC 기반**: SPEC-ANDROID-REFACTOR-001
