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
    │   └── CartItemEntity.kt
    ├── dao/
    │   └── CartDao.kt
    └── DatabaseModule.kt
```

---

## 주요 컴포넌트

### 1. Entity - 데이터베이스 테이블 정의

#### CartItemEntity

```kotlin
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
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
    productId TEXT PRIMARY KEY NOT NULL,
    productName TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    imageUrl TEXT NOT NULL,
    created_at INTEGER NOT NULL
)
```

---

### 2. DAO - Data Access Object

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
    entities = [CartItemEntity::class],
    version = 1,
    exportSchema = true  // 마이그레이션 추적용
)
abstract class DaitsoDatabase : RoomDatabase() {
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
    fun provideCartDao(database: DaitsoDatabase): CartDao {
        return database.cartDao()
    }
}
```

---

## CRUD 작업

### Create (생성)

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

**최종 업데이트**: 2025-11-28
**SPEC 기반**: SPEC-ANDROID-INIT-001
