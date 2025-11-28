# Core Network 모듈 (:core:network)

**Retrofit 기반 네트워크 통신 모듈**

---

## 개요

`:core:network` 모듈은 REST API 통신을 담당합니다. Retrofit2와 OkHttp를 사용하여 타입 안전한 API 클라이언트를 제공합니다.

### 모듈 특징

- ✅ Retrofit2 기반 REST API 클라이언트
- ✅ OkHttp 로깅 및 인터셉터
- ✅ Kotlin Serialization 지원
- ✅ Coroutine 기반 비동기 API

---

## 모듈 구조

```
core/network/
├── build.gradle.kts
└── src/main/kotlin/com/bup/ys/daitso/core/network/
    ├── DaitsoApiService.kt
    ├── NetworkDataSource.kt
    └── NetworkModule.kt
```

---

## 주요 컴포넌트

### 1. DaitsoApiService - API 엔드포인트 정의

```kotlin
interface DaitsoApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): ProductDto

    @POST("cart")
    suspend fun addToCart(@Body cartItem: CartItemDto): CartItemDto

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto
}
```

#### 주요 Annotation

| Annotation | 용도 | 예시 |
|-----------|------|------|
| **@GET** | GET 요청 | `@GET("products")` |
| **@POST** | POST 요청 | `@POST("users")` |
| **@PUT** | PUT 요청 | `@PUT("products/{id}")` |
| **@DELETE** | DELETE 요청 | `@DELETE("products/{id}")` |
| **@Path** | URL 경로 파라미터 | `@Path("id")` |
| **@Query** | 쿼리 파라미터 | `@Query("page")` |
| **@Body** | 요청 본문 | `@Body request` |

---

### 2. NetworkDataSource - 데이터 소스 인터페이스

```kotlin
interface NetworkDataSource {
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: String): Product
    suspend fun addToCart(cartItem: CartItem): CartItem
    suspend fun getUser(id: String): User
}
```

#### 구현체

```kotlin
class NetworkDataSourceImpl(
    private val apiService: DaitsoApiService
) : NetworkDataSource {

    override suspend fun getProducts(): List<Product> {
        return apiService.getProducts().map { it.toDomainModel() }
    }

    override suspend fun getProduct(id: String): Product {
        return apiService.getProduct(id).toDomainModel()
    }

    override suspend fun addToCart(cartItem: CartItem): CartItem {
        return apiService.addToCart(cartItem.toDto()).toDomainModel()
    }

    override suspend fun getUser(id: String): User {
        return apiService.getUser(id).toDomainModel()
    }
}
```

---

### 3. NetworkModule - Hilt 의존성 제공

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.daitso.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideDaitsoApiService(retrofit: Retrofit): DaitsoApiService {
        return retrofit.create(DaitsoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkDataSource(
        apiService: DaitsoApiService
    ): NetworkDataSource {
        return NetworkDataSourceImpl(apiService)
    }
}
```

---

## OkHttp 인터셉터

### HttpLoggingInterceptor

HTTP 요청/응답을 로깅합니다:

```kotlin
HttpLoggingInterceptor().apply {
    level = when {
        BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
        else -> HttpLoggingInterceptor.Level.NONE
    }
}
```

**Level 설정:**

| Level | 내용 |
|-------|------|
| **NONE** | 로깅 없음 |
| **BASIC** | 요청/응답 라인 |
| **HEADERS** | 요청/응답 헤더 포함 |
| **BODY** | 본문까지 전부 (개발 환경 권장) |

### 커스텀 인터셉터

```kotlin
class ErrorHandlingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request()
            val response = chain.proceed(request)

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw HttpException(response.code, errorBody)
            }

            return response
        } catch (e: Exception) {
            // 에러 처리
            throw e
        }
    }
}

// NetworkModule에서 추가
.addInterceptor(ErrorHandlingInterceptor())
```

---

## API 호출 패턴

### 기본 사용

```kotlin
// Repository에서 API 호출
override fun getProducts(): Flow<Result<List<Product>>> = flow {
    emit(Result.Loading())
    try {
        val products = networkDataSource.getProducts()
        emit(Result.Success(products))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}
```

### 쿼리 파라미터

```kotlin
interface DaitsoApiService {
    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("category") category: String? = null
    ): List<ProductDto>
}

// 호출
apiService.getProducts(page = 1, limit = 20, category = "electronics")
```

### 요청 헤더

```kotlin
interface DaitsoApiService {
    @GET("products")
    suspend fun getProducts(
        @Header("Authorization") token: String
    ): List<ProductDto>
}

// 또는 전역 헤더 설정
okHttpClient.newBuilder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        chain.proceed(request)
    }
```

### 파일 업로드

```kotlin
interface DaitsoApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): UploadResponse
}

// 호출
val file = File(context.cacheDir, "image.jpg")
val requestBody = file.asRequestBody("image/jpeg".toMediaType())
val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
apiService.uploadImage(
    description = RequestBody.create("text/plain".toMediaType(), "My image"),
    file = part
)
```

---

## 에러 처리

### HTTP 에러

```kotlin
sealed class NetworkError {
    data class HttpError(val code: Int, val message: String) : NetworkError()
    data class NetworkError(val exception: IOException) : NetworkError()
    data class UnknownError(val exception: Exception) : NetworkError()
}

// 처리
try {
    networkDataSource.getProducts()
} catch (e: HttpException) {
    // HTTP 에러 (4xx, 5xx)
} catch (e: IOException) {
    // 네트워크 에러
} catch (e: Exception) {
    // 기타 에러
}
```

### Retry 로직

```kotlin
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 100,
    block: suspend () -> T
): T {
    var delay = initialDelay
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: IOException) {
            if (attempt == maxRetries - 1) throw e
            delay(delay)
            delay *= 2  // Exponential backoff
        }
    }
    throw IllegalStateException("Unreachable")
}

// 사용
retryWithBackoff {
    networkDataSource.getProducts()
}
```

---

## 모킹 및 테스트

### MockWebServer를 사용한 테스트

```kotlin
class NetworkDataSourceTest {
    private val mockWebServer = MockWebServer()
    private lateinit var apiService: DaitsoApiService

    @Before
    fun setup() {
        mockWebServer.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .build()
        apiService = retrofit.create(DaitsoApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getProducts returns product list`() = runBlocking {
        // Mock 응답 설정
        mockWebServer.enqueue(
            MockResponse().setBody("""
                [
                    {"id":"1","name":"Product 1","price":100},
                    {"id":"2","name":"Product 2","price":200}
                ]
            """)
        )

        // API 호출
        val products = apiService.getProducts()

        // 검증
        assertEquals(2, products.size)
        assertEquals("Product 1", products[0].name)
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

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.okhttp.mockwebserver)
}
```

---

## 모범 사례

### 1. API 버전 관리

```kotlin
const val API_BASE_URL = "https://api.daitso.com/v1/"

// 또는 BuildConfig에서
buildConfigField("String", "API_BASE_URL", "\"https://api.daitso.com/v1/\"")
```

### 2. 타임아웃 설정

```kotlin
.connectTimeout(30, TimeUnit.SECONDS)
.readTimeout(30, TimeUnit.SECONDS)
.writeTimeout(30, TimeUnit.SECONDS)
```

### 3. 요청 로깅 제어

```kotlin
val loggingLevel = if (BuildConfig.DEBUG) {
    HttpLoggingInterceptor.Level.BODY
} else {
    HttpLoggingInterceptor.Level.NONE
}
```

### 4. 예외 처리

```kotlin
override suspend fun getProducts(): List<Product> {
    return try {
        apiService.getProducts().map { it.toDomainModel() }
    } catch (e: Exception) {
        Logger.e(TAG, "Failed to fetch products", e)
        throw e
    }
}
```

---

## 참고

- [Retrofit 공식 문서](https://square.github.io/retrofit/)
- [OkHttp 공식 문서](https://square.github.io/okhttp/)
- [Kotlin Serialization](https://kotlinlang.org/docs/serialization.html)
- [SPEC-ANDROID-INIT-001](../../specs/SPEC-ANDROID-INIT-001/spec.md)

---

**최종 업데이트**: 2025-11-28
**SPEC 기반**: SPEC-ANDROID-INIT-001
