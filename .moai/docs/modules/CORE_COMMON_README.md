# Core Common 모듈 (:core:common)

**공통 유틸리티와 확장 함수를 제공하는 모듈**

---

## 개요

`:core:common` 모듈은 애플리케이션 전반에서 필요한 공통 유틸리티, 확장 함수, 그리고 의존성 주입 관련 Annotation을 정의합니다.

### 모듈 특징

- ✅ Result<T> 비동기 작업 래퍼
- ✅ @Dispatcher 코루틴 디스패처 주입
- ✅ Logger 로깅 유틸리티
- ✅ 모든 다른 모듈에서 재사용

---

## 모듈 구조

```
core/common/
├── build.gradle.kts
└── src/main/kotlin/com/bup/ys/daitso/core/common/
    ├── Result.kt
    ├── Dispatcher.kt
    └── Logger.kt
```

---

## 주요 컴포넌트

### 1. Result<T> - 비동기 작업 래퍼

비동기 작업의 결과를 3가지 상태로 나타내는 sealed class입니다.

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data class Loading<T>(val data: T? = null) : Result<T>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> data
    }

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
}
```

#### 상태별 설명

**Success** - 작업 성공
```kotlin
// 데이터와 함께 성공 상태 반환
Result.Success(listOf(product1, product2))
```

**Error** - 작업 실패
```kotlin
// 예외와 함께 에러 상태 반환
Result.Error(Exception("Network failed"))
```

**Loading** - 작업 진행 중
```kotlin
// 선택적으로 이전 데이터 포함 가능
Result.Loading(previousData)  // 데이터 있음
Result.Loading()               // 데이터 없음
```

#### 사용 예시

```kotlin
// Repository에서 사용
fun getProducts(): Flow<Result<List<Product>>> = flow {
    emit(Result.Loading())
    try {
        val products = api.fetchProducts()
        emit(Result.Success(products))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}

// ViewModel에서 사용
productRepository.getProducts().collect { result ->
    when (result) {
        is Result.Loading -> _state.value = UiState.Loading
        is Result.Success -> _state.value = UiState.Content(result.data)
        is Result.Error -> _state.value = UiState.Error(result.exception.message)
    }
}

// UI에서 사용
when (state) {
    is UiState.Loading -> DaitsoLoadingIndicator()
    is UiState.Content -> ProductList(state.products)
    is UiState.Error -> ErrorView(state.message)
}
```

---

### 2. @Dispatcher - 코루틴 디스패처 주입

Coroutine Dispatcher를 타입 안전하게 주입하기 위한 Annotation입니다.

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(val dispatcher: DaitsoDispatchers)

enum class DaitsoDispatchers {
    IO,      // 네트워크, 디스크 I/O
    Default, // CPU 집약적 작업
    Main     // UI 업데이트
}
```

#### 사용 예시

**1. Hilt Module에서 제공**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
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
```

**2. Repository에서 주입**

```kotlin
class ProductRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override fun getProducts(): Flow<Result<List<Product>>> = flow {
        // 네트워크 요청 (IO 디스패처)
        val remoteProducts = networkDataSource.getProducts()
        // ...
    }.flowOn(ioDispatcher)
}
```

**3. ViewModel에서 주입**

```kotlin
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    @Dispatcher(DaitsoDispatchers.Main) private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    fun loadProducts() {
        viewModelScope.launch(mainDispatcher) {
            productRepository.getProducts().collect { result ->
                // UI 업데이트
            }
        }
    }
}
```

#### 디스패처 선택 가이드

| 디스패처 | 용도 | 예시 |
|---------|------|------|
| **IO** | 네트워크, 파일 I/O | API 호출, 데이터베이스 조회 |
| **Default** | CPU 작업 | 데이터 파싱, 정렬 |
| **Main** | UI 업데이트 | StateFlow 업데이트 |

---

### 3. Logger - 로깅 유틸리티

애플리케이션 전반의 로깅을 관리하는 유틸리티입니다.

```kotlin
object Logger {
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }
}
```

#### 사용 예시

```kotlin
class ProductRepositoryImpl @Inject constructor(
    // ...
) : ProductRepository {

    companion object {
        private const val TAG = "ProductRepository"
    }

    override fun getProducts(): Flow<Result<List<Product>>> = flow {
        Logger.d(TAG, "Fetching products...")

        try {
            val products = networkDataSource.getProducts()
            Logger.i(TAG, "Fetched ${products.size} products")
            emit(Result.Success(products))
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to fetch products", e)
            emit(Result.Error(e))
        }
    }
}
```

---

## 의존성

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
```

---

## 확장 함수

공통으로 사용할 확장 함수를 이 모듈에 정의할 수 있습니다:

### 예시: Result 확장 함수

```kotlin
// Result.kt
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> = apply {
    if (this is Result.Success) action(data)
}

inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> = apply {
    if (this is Result.Error) action(exception)
}

inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> = apply {
    if (this is Result.Loading) action()
}

// 사용
result
    .onSuccess { data -> println("Success: $data") }
    .onError { error -> println("Error: ${error.message}") }
    .onLoading { println("Loading...") }
```

---

## 테스트

```kotlin
// src/test/kotlin/com/bup/ys/daitso/core/common/ResultTest.kt

class ResultTest {
    @Test
    fun `Success contains data`() {
        val result = Result.Success(listOf("item1", "item2"))

        assertTrue(result.isSuccess())
        assertFalse(result.isError())
        assertEquals(listOf("item1", "item2"), result.getOrNull())
    }

    @Test
    fun `Error contains exception`() {
        val exception = Exception("Test error")
        val result = Result.Error(exception)

        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertNull(result.getOrNull())
    }

    @Test
    fun `Loading indicates progress`() {
        val result = Result.Loading<String>()

        assertTrue(result.isLoading())
        assertNull(result.getOrNull())
    }
}

// src/test/kotlin/com/bup/ys/daitso/core/common/DispatcherTest.kt

@RunWith(RobolectricTestRunner::class)
class DispatcherTest {
    @Test
    fun `IO dispatcher works correctly`() {
        val ioDispatcher = Dispatchers.IO
        val job = Job()

        val scope = CoroutineScope(ioDispatcher + job)

        scope.launch {
            // IO 작업 테스트
        }

        runBlocking {
            job.join()
        }
    }
}
```

---

## 모범 사례

### 1. Result 사용

```kotlin
// Good: 상태 관리
fun getUsers(): Flow<Result<List<User>>> = flow {
    emit(Result.Loading())
    try {
        emit(Result.Success(api.getUsers()))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}

// Bad: 예외만 던짐
fun getUsers(): List<User> {
    return api.getUsers()  // 예외 처리 어려움
}
```

### 2. Dispatcher 주입

```kotlin
// Good: Dispatcher 주입
class MyRepo @Inject constructor(
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : Repository {
    // ...
}

// Bad: 하드코딩
class MyRepo : Repository {
    override fun getData() = flow {
        emit(api.get())  // 테스트 어려움
    }.flowOn(Dispatchers.IO)
}
```

### 3. 로깅

```kotlin
// Good: 태그 상수화
class MyClass {
    companion object {
        private const val TAG = "MyClass"
    }

    fun doSomething() {
        Logger.d(TAG, "Doing something")
    }
}

// Bad: 하드코딩
fun doSomething() {
    Logger.d("Random", "Action")
}
```

---

## 참고

- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Result Pattern](https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-result/)
- [SPEC-ANDROID-INIT-001](../../specs/SPEC-ANDROID-INIT-001/spec.md)

---

**최종 업데이트**: 2025-11-28
**SPEC 기반**: SPEC-ANDROID-INIT-001
