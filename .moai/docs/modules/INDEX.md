# 모듈 네비게이션 가이드 (Module Navigation Guide)

## 개요

Daitso 프로젝트는 Clean Architecture 기반의 멀티모듈 구조를 가집니다. 본 문서는 각 모듈의 목적, 구조, 그리고 상호 작용을 설명합니다.

## Core 모듈 네비게이션

### 1. :core:model - 도메인 모델

**경로**: `/core/model`

**목적**: 앱 전역에서 사용되는 도메인 모델 정의

**특징**:
- 순수 Kotlin 모듈 (Android 의존성 없음)
- Kotlin Serialization으로 JSON 직렬화 지원
- 다른 모듈의 기초

**주요 클래스**:

| 클래스 | 설명 | 의존성 |
|--------|------|--------|
| `Product.kt` | 상품 정보 모델 | Kotlin Serialization |
| `CartItem.kt` | 장바구니 아이템 모델 | Kotlin Serialization |
| `User.kt` | 사용자 정보 모델 | Kotlin Serialization |

**사용 예**:
```kotlin
@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double
)
```

**테스트 범위**:
- 직렬화/역직렬화 테스트
- 데이터 검증 테스트
- 동등성 비교 테스트

**빌드 명령**:
```bash
./gradlew :core:model:build
./gradlew :core:model:test
```

---

### 2. :core:common - 공통 유틸리티

**경로**: `/core/common`

**목적**: 프로젝트 전역에서 사용되는 공통 기능 제공

**특징**:
- Result Wrapper로 비동기 작업 결과 표현
- Dispatcher Annotation으로 Coroutine 스레드 관리
- Logger 유틸리티로 중앙 집중식 로깅

**패키지 구조**:

```
:core:common/
├── result/
│   └── Result.kt       # Success/Error/Loading
├── di/
│   └── Dispatcher.kt   # @Dispatcher Qualifier
└── logger/
    └── Logger.kt       # 로깅 유틸리티
```

**주요 컴포넌트**:

#### Result<T> - 비동기 작업 결과
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

**사용 패턴**:
```kotlin
when (result) {
    is Result.Success -> handleSuccess(result.data)
    is Result.Error -> handleError(result.exception)
    Result.Loading -> showLoading()
}
```

#### @Dispatcher - Coroutine 주입
```kotlin
@Qualifier
annotation class Dispatcher(val dispatcher: DaitsoDispatchers)

enum class DaitsoDispatchers {
    IO,        // 네트워크, 파일 I/O
    Default,   // 무거운 계산
    Main       // UI 업데이트
}
```

**사용 패턴**:
```kotlin
class ProductRepositoryImpl @Inject constructor(
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository
```

**의존성**:
- :core:model

**테스트 범위**:
- Result 상태 전환 테스트
- Dispatcher 주입 테스트
- Logger 동작 테스트

**빌드 명령**:
```bash
./gradlew :core:common:build
./gradlew :core:common:test
```

---

### 3. :core:designsystem - Design System

**경로**: `/core/designsystem`

**목적**: 일관된 UI 컴포넌트 및 디자인 시스템 제공

**특징**:
- Material3 기반 테마 (Light/Dark 모드)
- 재사용 가능한 공통 컴포넌트
- Compose Preview 지원

**패키지 구조**:

```
:core:designsystem/
├── theme/
│   ├── Color.kt          # 컬러 팔레트
│   ├── Typography.kt     # 타이포그래피
│   ├── Shape.kt          # 형태(radius)
│   └── DaitsoTheme.kt    # 메인 테마
└── components/
    ├── DaitsoButton.kt
    ├── DaitsoTextField.kt
    ├── DaitsoLoadingIndicator.kt
    └── DaitsoErrorView.kt
```

**DaitsoTheme 사용**:
```kotlin
@Composable
fun MyScreen() {
    DaitsoTheme(darkTheme = false) {
        // Light 모드
        Scaffold {
            MyContent()
        }
    }
}
```

**공통 컴포넌트**:

| 컴포넌트 | 설명 | 파라미터 |
|----------|------|---------|
| `DaitsoButton` | 기본 버튼 | text, onClick, modifier |
| `DaitsoTextField` | 입력 필드 | value, onValueChange, label |
| `DaitsoLoadingIndicator` | 로딩 표시 | modifier, size |
| `DaitsoErrorView` | 에러 표시 | message, onRetry |

**의존성**:
- :core:common
- Jetpack Compose BOM 2024.12.01

**테스트 범위**:
- Compose Preview 렌더링
- 컴포넌트 UI 테스트
- 테마 적용 테스트

**빌드 명령**:
```bash
./gradlew :core:designsystem:build
./gradlew :core:designsystem:test
```

---

### 4. :core:network - 네트워크 통신

**경로**: `/core/network`

**목적**: 원격 데이터 소스를 추상화하고 API 통신 관리

**특징**:
- Retrofit 2.11.0 + OkHttp 4.12.0 설정
- Kotlin Serialization Converter 통합
- NetworkDataSource 인터페이스로 추상화

**패키지 구조**:

```
:core:network/
├── api/
│   └── DaitsoApiService.kt    # Retrofit 인터페이스
├── source/
│   └── NetworkDataSource.kt   # 추상화 인터페이스
└── di/
    └── NetworkModule.kt       # Hilt 의존성 제공
```

**API Service 정의**:
```kotlin
interface DaitsoApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Product
}
```

**NetworkDataSource**:
```kotlin
interface NetworkDataSource {
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: String): Product
}
```

**NetworkModule**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient { ... }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit { ... }
}
```

**의존성**:
- :core:model
- :core:common
- Retrofit 2.11.0
- OkHttp 4.12.0

**테스트 범위**:
- Mock 서버를 사용한 API 호출
- Interceptor 동작 테스트
- 에러 처리 테스트

**빌드 명령**:
```bash
./gradlew :core:network:build
./gradlew :core:network:test
```

---

### 5. :core:database - 로컬 데이터베이스

**경로**: `/core/database`

**목적**: 로컬 데이터 저장소 및 캐시 레이어 관리

**특징**:
- Room 2.6.1 + KSP 설정
- Flow 기반 반응형 쿼리
- 엔티티 및 DAO 패턴

**패키지 구조**:

```
:core:database/
├── entity/
│   └── CartItemEntity.kt      # Room 엔티티
├── dao/
│   └── CartDao.kt             # Data Access Object
├── DaitsoDatabase.kt          # Room Database
└── di/
    └── DatabaseModule.kt      # Hilt 의존성 제공
```

**Entity 정의**:
```kotlin
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String
)
```

**DAO 인터페이스**:
```kotlin
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)
}
```

**DatabaseModule**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DaitsoDatabase {
        return Room.databaseBuilder(
            context,
            DaitsoDatabase::class.java,
            "daitso_db"
        ).build()
    }
}
```

**의존성**:
- :core:model
- :core:common
- Room 2.6.1
- KSP 2.1.0-1.0.29

**테스트 범위**:
- Room DAO CRUD 테스트
- InMemory Database 사용
- Flow 쿼리 테스트

**빌드 명령**:
```bash
./gradlew :core:database:build
./gradlew :core:database:test
```

---

### 6. :core:data - 데이터 레이어 & Repository

**경로**: `/core/data`

**목적**: Repository 패턴으로 로컬/원격 데이터 소스 통합

**특징**:
- Offline-first 데이터 계층
- Flow 기반 반응형 데이터 스트림
- 타입 안전한 의존성 주입

**패키지 구조**:

```
:core:data/
├── repository/
│   ├── ProductRepository.kt        # 인터페이스
│   └── ProductRepositoryImpl.kt     # 구현체
├── datasource/
│   ├── LocalDataSource.kt          # 로컬 데이터
│   └── RemoteDataSource.kt         # 원격 데이터
└── di/
    └── DataModule.kt               # Hilt 모듈
```

**Repository 인터페이스**:
```kotlin
interface ProductRepository {
    fun getProducts(): Flow<Result<List<Product>>>
    fun getProduct(id: String): Flow<Result<Product>>
}
```

**Offline-first 구현**:
```kotlin
class ProductRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    @Dispatcher(DaitsoDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {
    override fun getProducts(): Flow<Result<List<Product>>> = flow {
        emit(Result.Loading)

        // 로컬 캐시 먼저 방출
        val cached = localDataSource.getProducts()
        emit(Result.Success(cached))

        // 네트워크 동기화
        try {
            val remote = networkDataSource.getProducts()
            localDataSource.saveProducts(remote)
            emit(Result.Success(remote))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)
}
```

**DataModule**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Provides
    @Singleton
    @Dispatcher(DaitsoDispatchers.IO)
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

**의존성**:
- :core:model
- :core:common
- :core:network
- :core:database

**테스트 범위**:
- Repository Offline-first 동작
- Dispatcher 주입 테스트
- 데이터 소스 통합 테스트

**빌드 명령**:
```bash
./gradlew :core:data:build
./gradlew :core:data:test
```

---

## 모듈 의존성 매트릭스 (Dependency Matrix)

```
           model  common  design  network  database  data  app
model        -      -       -       -        -       -    ✓
common       ✓      -       -       -        -       -    ✓
design       ✓      ✓       -       -        -       -    ✓
network      ✓      ✓       -       -        -       -    ✓
database     ✓      ✓       -       -        -       -    ✓
data         ✓      ✓       -       ✓        ✓       -    ✓
app          ✓      ✓       ✓       ✓        ✓       ✓    -
```

**의존성 규칙**:
- ✓ 위쪽 모듈 → 아래쪽 모듈 의존 (가능)
- ✗ 아래쪽 모듈 → 위쪽 모듈 의존 (불가)
- ✗ 같은 레벨 모듈 간 직접 의존 (불가)

---

## Inter-Module 통신 (Communication Patterns)

### 1. 인터페이스 기반 통신

**패턴**:
```
Feature → Repository (Interface) ← RepositoryImpl (구현)
                                      ↓
                        LocalDataSource, RemoteDataSource
```

**이점**:
- Loose Coupling (느슨한 결합)
- 테스트 시 Mock 객체 주입 용이
- 구현 변경 시 인터페이스만 유지

### 2. Hilt 기반 의존성 주입

**패턴**:
```
@HiltViewModel or @Inject
    ↓
Repository Interface (계약)
    ↓
DataModule (@Binds 바인딩)
    ↓
RepositoryImpl (구현)
```

**예**:
```kotlin
class ProductsViewModel @Inject constructor(
    private val repository: ProductRepository  // 인터페이스 주입
) : ViewModel()
```

### 3. Flow 기반 데이터 스트림

**패턴**:
```
ViewModel
  ↓
repository.getProducts()  // Flow<Result<List<Product>>>
  ↓
Flow Collector
  ↓
UI State Update
```

**사용**:
```kotlin
viewModelScope.launch {
    repository.getProducts().collect { result ->
        _state.value = when (result) {
            is Result.Loading -> State.Loading
            is Result.Success -> State.Success(result.data)
            is Result.Error -> State.Error(result.exception)
        }
    }
}
```

---

## 개발 워크플로우 (Development Workflow)

### 모듈별 빌드 명령

```bash
# 특정 모듈만 빌드
./gradlew :core:model:build

# 모든 Core 모듈 빌드
./gradlew :core:model:build :core:common:build :core:data:build

# 전체 프로젝트 빌드
./gradlew build

# 테스트 실행
./gradlew :core:data:test

# Gradle 의존성 트리 확인
./gradlew :core:data:dependencies
```

### 모듈 추가 체크리스트

새로운 모듈 추가 시:

1. [ ] `build.gradle.kts` 생성
   ```kotlin
   plugins {
       alias(libs.plugins.daitso.android.library)
       // 필요한 플러그인 추가
   }

   dependencies {
       // 의존성 추가
   }
   ```

2. [ ] `settings.gradle.kts`에 모듈 등록
   ```kotlin
   include(":feature:newmodule")
   ```

3. [ ] 패키지 구조 생성
   ```
   src/main/kotlin/com/bup/ys/daitso/feature/newmodule/
   ```

4. [ ] 테스트 패키지 생성
   ```
   src/test/kotlin/com/bup/ys/daitso/feature/newmodule/
   ```

5. [ ] 의존성 그래프 검증
   ```bash
   ./gradlew :feature:newmodule:dependencies
   ```

---

## 문제 해결 (Troubleshooting)

### 빌드 에러

#### "Unable to find symbol" in KSP
```
원인: KSP가 annotation processing을 못함
해결:
1. ./gradlew clean
2. Invalidate Caches in Android Studio
3. Sync Now
```

#### Hilt "Component not found"
```
원인: Hilt Module이 올바르게 설치되지 않음
해결:
1. @Module, @InstallIn 확인
2. DI 의존성 그래프 확인
3. 순환 참조 검증
```

#### Room "Class cannot be recognized"
```
원인: Entity 클래스가 KSP에 노출되지 않음
해결:
1. @Entity 어노테이션 확인
2. 기본 생성자 확인
3. build.gradle.kts에 ksp 설정 확인
```

---

## 참고 자료

- [ARCHITECTURE.md](../ARCHITECTURE.md) - 전체 아키텍처 설계
- [SETUP.md](../SETUP.md) - 개발 환경 설정
- [SPEC-ANDROID-INIT-001](../../specs/SPEC-ANDROID-INIT-001/spec.md) - 프로젝트 스펙

---

**최종 업데이트**: 2025-11-29
**문서 버전**: 1.0.0
**상태**: Active
