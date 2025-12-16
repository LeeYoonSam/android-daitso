# Core Database 모듈 (:core:database)

**Room 기반 로컬 데이터베이스 모듈**

---

## 개요

`:core:database` 모듈은 Room Database를 사용하여 로컬 데이터 저장소를 제공합니다. Offline-first 전략을 구현하기 위해 Single Source of Truth로 작동합니다.

### 모듈 특징

- ✅ Room Database 기반
- ✅ Type-safe DAO
- ✅ Flow를 통한 반응형 데이터 접근
- ✅ 마이그레이션 지원

---

## 모듈 구조

```
core/database/
├── build.gradle.kts
└── src/main/kotlin/com/bup/ys/daitso/core/database/
    ├── DaitsoDatabase.kt
    ├── entity/
    │   ├── CartItemEntity.kt
    │   └── ProductEntity.kt
    ├── dao/
    │   ├── CartDao.kt
    │   └── ProductDao.kt
    └── DatabaseModule.kt
```

---

## 주요 컴포넌트

### 1. Entity - 데이터베이스 테이블 정의

#### ProductEntity

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val stock: Int
)
```

**필드 설명:**

| 필드 | 타입 | 설명 | 제약사항 |
|------|------|------|---------|
| **id** | String | 상품 고유 식별자 | Primary Key |
| **name** | String | 상품명 | NOT NULL |
| **description** | String | 상품 설명 | NOT NULL |
| **price** | Double | 상품 가격 | NOT NULL |
| **imageUrl** | String | 상품 이미지 URL | NOT NULL |
| **category** | String | 상품 카테고리 | NOT NULL |
| **stock** | Int | 재고 수량 | NOT NULL |

**생성 테이블:**

```sql
CREATE TABLE products (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    price REAL NOT NULL,
    imageUrl TEXT NOT NULL,
    category TEXT NOT NULL,
    stock INTEGER NOT NULL
)
```

**매퍼 함수:**

```kotlin
/**
 * ProductEntity를 도메인 모델로 변환
 */
fun ProductEntity.toDomainModel(): Product {
    return Product(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        category = category,
        stock = stock
    )
}

/**
 * 도메인 Product를 Entity로 변환
 */
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        category = category,
        stock = stock
    )
}
```

---

#### CartItemEntity

```kotlin
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

**주요 Annotation:**

| Annotation | 용도 |
|-----------|------|
| **@Entity** | 테이블 정의 |
| **@PrimaryKey** | 기본 키 지정 |
| **@ColumnInfo** | 열 이름 지정 |
| **@Ignore** | 데이터베이스에서 무시 |
| **@Embedded** | 중첩 객체 포함 |
| **@Relation** | 관계 정의 |

**생성 테이블:**

```sql
CREATE TABLE cart_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    productId TEXT NOT NULL,
    productName TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    imageUrl TEXT NOT NULL,
    created_at INTEGER NOT NULL
)
```

---

### 2. DAO - Data Access Object

#### ProductDao

```kotlin
@Dao
interface ProductDao {

    /**
     * 모든 상품을 Flow로 조회
     *
     * @return 상품 목록의 Flow
     */
    @Query("SELECT * FROM products")
    fun getProducts(): Flow<List<ProductEntity>>

    /**
     * ID로 단일 상품 조회
     *
     * @param id 상품 ID
     * @return 상품 엔티티 또는 null
     */
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    /**
     * 단일 상품 삽입
     * 같은 ID의 상품이 있으면 대체
     *
     * @param product 삽입할 상품
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    /**
     * 여러 상품 삽입
     * 같은 ID의 상품이 있으면 대체
     *
     * @param products 삽입할 상품 목록
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    /**
     * 모든 상품 삭제
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
```

**메서드 설명:**

| 메서드 | 반환 타입 | 설명 | 비동기 |
|--------|----------|------|--------|
| **getProducts()** | Flow | 모든 상품 조회 | Flow |
| **getProductById(id)** | ProductEntity? | ID로 특정 상품 조회 | suspend |
| **insertProduct(product)** | Unit | 단일 상품 삽입 | suspend |
| **insertProducts(products)** | Unit | 여러 상품 삽입 | suspend |
| **deleteAllProducts()** | Unit | 전체 상품 삭제 | suspend |

**OnConflictStrategy.REPLACE 동작:**
- 이미 같은 ID의 상품이 있으면 기존 데이터 대체
- 네트워크에서 최신 데이터를 받아 캐시 업데이트할 때 유용

---

#### CartDao

```kotlin
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY created_at DESC")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getCartItem(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItems(items: List<CartItemEntity>)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartItemCount(): Flow<Int>
}
```

**DAO 주요 Annotation:**

| Annotation | 용도 | 비동기 |
|-----------|------|---------|
| **@Query** | SQL 쿼리 실행 | suspend/Flow |
| **@Insert** | 데이터 삽입 | suspend |
| **@Update** | 데이터 업데이트 | suspend |
| **@Delete** | 데이터 삭제 | suspend |

**OnConflictStrategy:**

| 전략 | 설명 |
|------|------|
| **REPLACE** | 기존 데이터 대체 |
| **IGNORE** | 기존 데이터 무시 |
| **ABORT** | 트랜잭션 중단 |

---

### 3. Database - 데이터베이스 정의

```kotlin
@Database(
    entities = [ProductEntity::class, CartItemEntity::class],
    version = 2,
    exportSchema = true  // 마이그레이션 추적용
)
abstract class DaitsoDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
}
```

**파라미터:**

- **entities**: 포함할 Entity 클래스
- **version**: 데이터베이스 버전 (마이그레이션에 사용)
- **exportSchema**: 스키마 내보내기 활성화

---

### 4. DatabaseModule - Hilt 의존성 제공

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
            "daitso_database"
        )
            .fallbackToDestructiveMigration()  // 개발 중 스키마 변경 시 재생성
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: DaitsoDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideCartDao(database: DaitsoDatabase): CartDao {
        return database.cartDao()
    }
}
```

---

## CRUD 작업

### Create (생성)

#### 상품 캐싱

```kotlin
// 단일 상품 삽입
suspend fun saveProduct(product: Product) {
    productDao.insertProduct(product.toEntity())
}

// 여러 상품 삽입 (권장: 네트워크에서 받은 상품 목록 캐싱)
suspend fun saveProductsBatch(products: List<Product>) {
    productDao.insertProducts(products.map { it.toEntity() })
}
```

#### 장바구니 항목 추가

```kotlin
// 단일 항목 삽입
suspend fun addToCart(cartItem: CartItem) {
    cartDao.insertCartItem(cartItem.toEntity())
}

// 여러 항목 삽입
suspend fun addToCartBatch(cartItems: List<CartItem>) {
    cartDao.insertCartItems(cartItems.map { it.toEntity() })
}
```

### Read (조회)

#### 상품 조회

```kotlin
// 모든 상품 조회 (Flow - 실시간 반영)
fun getProducts(): Flow<List<Product>> {
    return productDao.getProducts().map { entities ->
        entities.map { it.toDomainModel() }
    }
}

// ID로 특정 상품 조회
suspend fun getProduct(productId: String): Product? {
    return productDao.getProductById(productId)?.toDomainModel()
}
```

#### 장바구니 조회

```kotlin
// 모든 항목 조회 (Flow)
fun getCartItems(): Flow<List<CartItem>> {
    return cartDao.getCartItems().map { entities ->
        entities.map { it.toDomainModel() }
    }
}

// 단일 항목 조회
suspend fun getCartItem(productId: String): CartItem? {
    return cartDao.getCartItem(productId)?.toDomainModel()
}

// 개수 조회
fun getCartItemCount(): Flow<Int> {
    return cartDao.getCartItemCount()
}
```

### Update (업데이트)

```kotlin
suspend fun updateCartItem(cartItem: CartItem) {
    cartDao.updateCartItem(cartItem.toEntity())
}
```

### Delete (삭제)

#### 상품 캐시 삭제

```kotlin
// 전체 상품 캐시 삭제 (스키마 버전 변경 시)
suspend fun clearProductCache() {
    productDao.deleteAllProducts()
}
```

#### 장바구니 항목 삭제

```kotlin
// 단일 항목 삭제
suspend fun deleteCartItem(productId: String) {
    cartDao.deleteCartItem(productId)
}

// 전체 장바구니 비우기
suspend fun clearCart() {
    cartDao.clearCart()
}
```

---

## 트랜잭션

여러 작업을 원자적으로 수행:

```kotlin
@Dao
interface CartDao {
    @Transaction
    suspend fun updateCartWithItems(items: List<CartItemEntity>) {
        clearCart()
        insertCartItems(items)
    }
}

// 또는
suspend fun atomicCartUpdate(items: List<CartItem>) {
    withContext(ioDispatcher) {
        cartDao.updateCartWithItems(items.map { it.toEntity() })
    }
}
```

---

## 마이그레이션

데이터베이스 스키마 변경 시:

### 1. Entity 수정

```kotlin
// v1: 원본
@Entity(tableName = "cart_items", version = 1)
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

// v2: 새 필드 추가
@Entity(tableName = "cart_items", version = 2)
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val notes: String = ""  // 새 필드
)
```

### 2. 마이그레이션 정의

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE cart_items ADD COLUMN notes TEXT NOT NULL DEFAULT ''"
        )
    }
}

// DatabaseModule에서 적용
Room.databaseBuilder(context, DaitsoDatabase::class.java, "daitso_database")
    .addMigrations(MIGRATION_1_2)
    .build()
```

### 3. Database 버전 업데이트

```kotlin
@Database(
    entities = [CartItemEntity::class],
    version = 2  // 버전 증가
)
abstract class DaitsoDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}
```

---

## 테스트

```kotlin
@RunWith(AndroidTestRunner::class)
class CartDaoTest {
    private lateinit var database: DaitsoDatabase
    private lateinit var cartDao: CartDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DaitsoDatabase::class.java
        ).allowMainThreadQueries().build()
        cartDao = database.cartDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveCartItem() = runBlocking {
        val item = CartItemEntity(
            productId = "1",
            productName = "Product",
            quantity = 2,
            price = 100.0,
            imageUrl = "url"
        )

        cartDao.insertCartItem(item)
        val retrieved = cartDao.getCartItem("1")

        assertEquals(item, retrieved)
    }

    @Test
    fun getCartItemsReturnsCorrectData() = runBlocking {
        val items = listOf(
            CartItemEntity("1", "Product 1", 1, 100.0, "url1"),
            CartItemEntity("2", "Product 2", 2, 200.0, "url2")
        )

        cartDao.insertCartItems(items)
        val retrieved = cartDao.getCartItems().first()

        assertEquals(2, retrieved.size)
    }

    @Test
    fun deleteCartItemRemovesData() = runBlocking {
        val item = CartItemEntity("1", "Product", 1, 100.0, "url")
        cartDao.insertCartItem(item)
        cartDao.deleteCartItem("1")

        val retrieved = cartDao.getCartItem("1")
        assertNull(retrieved)
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

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.room.testing)
}
```

---

## 모범 사례

### 1. Flow 활용

```kotlin
// Good: Flow로 실시간 데이터 감시
fun getCartItems(): Flow<List<CartItem>> {
    return cartDao.getCartItems().map { /* ... */ }
}

// 구독
getCartItems().collect { items ->
    // 데이터 변경시 자동 반영
}
```

### 2. 스코프 관리

```kotlin
// Good: viewModelScope 사용
viewModelScope.launch {
    cartDao.insertCartItem(item)
}

// Bad: 스코프 없이
GlobalScope.launch {
    cartDao.insertCartItem(item)
}
```

### 3. 에러 처리

```kotlin
override fun addToCart(item: CartItem): Flow<Result<Unit>> = flow {
    emit(Result.Loading())
    try {
        cartDao.insertCartItem(item.toEntity())
        emit(Result.Success(Unit))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}
```

---

## 참고

- [Room 공식 문서](https://developer.android.com/training/data-storage/room)
- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [SPEC-ANDROID-INIT-001](../../specs/SPEC-ANDROID-INIT-001/spec.md)

---

---

## 데이터베이스 스키마 업데이트 (2025-12-13)

### CartItemEntity 필드 추가

CartItemEntity에 ID 필드가 추가되어 엔티티의 고유 식별성이 강화되었습니다.

**신규 필드:**
- **필드명**: `id`
- **타입**: `Int`
- **특성**: Primary Key, Auto-increment
- **기본값**: 0
- **용도**: 각 장바구니 항목의 고유 식별자

**변경 사항:**

```kotlin
// 변경 전
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,  // productId를 Primary Key로 사용
    // ...
)

// 변경 후
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // 새 Primary Key
    val productId: String,  // 이제 일반 필드
    // ...
)
```

### CartDao 신규 메서드

다음 메서드가 CartDao 인터페이스에 추가될 수 있습니다:

```kotlin
@Dao
interface CartDao {
    // ... 기존 메서드 ...

    // 신규: ID로 항목 조회
    @Query("SELECT * FROM cart_items WHERE id = :id")
    suspend fun getCartItemById(id: Int): CartItemEntity?

    // 신규: ID로 항목 삭제
    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: Int)

    // 신규: 마지막 삽입된 항목의 ID 반환
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItemAndReturnId(item: CartItemEntity): Long
}
```

**사용 예시:**

```kotlin
// ID로 항목 조회
val item = cartDao.getCartItemById(1)

// ID로 항목 삭제
cartDao.deleteCartItemById(1)

// 항목 삽입 후 생성된 ID 반환
val newId = cartDao.insertCartItemAndReturnId(cartItemEntity)
```

---

## Product 데이터베이스 캐싱 (2025-12-16)

### ProductEntity 추가

ProductEntity가 새로운 엔티티로 추가되어 상품 정보의 오프라인 캐싱을 지원합니다.

**주요 특징:**
- **스키마 버전**: v2로 업그레이드
- **테이블명**: `products`
- **기본 키**: `id` (String)
- **필드**: id, name, description, price, imageUrl, category, stock

**마이그레이션:**

```sql
-- v1 → v2 마이그레이션
CREATE TABLE products (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    price REAL NOT NULL,
    imageUrl TEXT NOT NULL,
    category TEXT NOT NULL,
    stock INTEGER NOT NULL
)
```

### ProductDao 추가

ProductDao 인터페이스가 새로 추가되어 다음 메서드를 제공합니다:

```kotlin
@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
```

**메서드별 용도:**

| 메서드 | 용도 | 예시 |
|--------|------|------|
| **getProducts()** | 모든 상품 오프라인 조회 | 홈화면 상품 목록 표시 |
| **getProductById(id)** | 상세 정보 오프라인 캐시 조회 | 상품 상세 페이지 초기 로딩 |
| **insertProduct(product)** | 단일 상품 캐싱 | 상품 상세 조회 후 캐싱 |
| **insertProducts(products)** | 일괄 캐싱 | API로부터 상품 목록 받아 캐싱 |
| **deleteAllProducts()** | 캐시 초기화 | 앱 재시작 또는 강제 새로고침 |

### LocalDataSourceImpl 개선

LocalDataSourceImpl이 Product 캐싱 기능을 지원하도록 개선되었습니다:

```kotlin
/**
 * 캐시된 모든 상품 조회
 * @return 캐시된 상품 목록, 실패 시 빈 목록
 */
override suspend fun getProducts(): List<Product> {
    return try {
        database.productDao().getProducts().first().map { it.toDomainModel() }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load products", e)
        emptyList()
    }
}

/**
 * ID로 특정 상품 조회
 * @param productId 상품 ID
 * @return 캐시된 상품 또는 null
 */
override suspend fun getProduct(productId: String): Product? {
    return try {
        database.productDao().getProductById(productId)?.toDomainModel()
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load product with id: $productId", e)
        null
    }
}

/**
 * 여러 상품을 캐시에 저장
 * @param products 저장할 상품 목록
 */
override suspend fun saveProducts(products: List<Product>) {
    try {
        database.productDao().insertProducts(products.map { it.toEntity() })
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save products", e)
    }
}

/**
 * 단일 상품을 캐시에 저장
 * @param product 저장할 상품
 */
override suspend fun saveProduct(product: Product) {
    try {
        database.productDao().insertProduct(product.toEntity())
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save product with id: ${product.id}", e)
    }
}
```

**에러 처리 전략:**
- 모든 작업에 try-catch 블록 적용
- 예외 발생 시 로깅하고 안전한 값 반환
- 네트워크 오류로 로컬 캐시 사용 가능 (Offline-first)

### 테스트 커버리지

#### ProductEntityTest (113줄)
- toDomainModel() 변환 테스트
- toEntity() 변환 테스트
- 라운드트립 변환 테스트
- 최소 데이터 테스트

#### ProductDaoTest (173줄)
- 단일 상품 삽입 테스트
- 여러 상품 삽입 테스트
- ID로 상품 조회 테스트
- 모든 상품 조회 테스트
- 충돌 전략(REPLACE) 테스트
- 상품 삭제 테스트

#### LocalDataSourceImplTest (229줄)
- 상품 목록 로드 테스트
- 빈 목록 처리 테스트
- ID로 상품 조회 테스트
- 상품 저장 테스트
- 데이터베이스 에러 처리 테스트

**총 커버리지: 87%** (테스트 515줄)

### Offline-First 데이터 흐름

```
사용자 요청 (상품 목록 조회)
    │
    ▼
Repository.getProducts()
    │
    ├─→ LocalDataSource.getProducts()  [로컬 캐시]
    │   └─→ ProductDao.getProducts()
    │       └─→ Flow<List<ProductEntity>>
    │
    ├─→ NetworkDataSource.getProducts()  [네트워크]
    │   └─→ API 호출
    │
    └─→ LocalDataSource.saveProducts()  [캐시 업데이트]
        └─→ ProductDao.insertProducts()
            └─→ Room Database에 저장

┌─────────────────────────┐
│  Flow<Result<>>         │
│ - Loading               │
│ - Success (로컬 캐시)  │
│ - Success (네트워크)   │
│ - Error                 │
└─────────────────────────┘
```

**동작 순서:**
1. Loading 상태 발행
2. 로컬 캐시에서 상품 로드 → Success 발행 (즉시 UI 표시)
3. 네트워크에서 최신 상품 로드 → 캐시 업데이트 → Success 다시 발행
4. 네트워크 실패 시 → Error 발행 (로컬 데이터 유지)

### 의존성 업데이트

DaitsoDatabase가 v2로 업그레이드되었습니다:

```kotlin
@Database(
    entities = [ProductEntity::class, CartItemEntity::class],
    version = 2,
    exportSchema = true
)
abstract class DaitsoDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
}
```

DatabaseModule에 ProductDao 제공자 추가:

```kotlin
@Provides
@Singleton
fun provideProductDao(database: DaitsoDatabase): ProductDao {
    return database.productDao()
}
```

---

**최종 업데이트**: 2025-12-16
**SPEC 기반**: SPEC-ANDROID-FEATURE-CART-001
