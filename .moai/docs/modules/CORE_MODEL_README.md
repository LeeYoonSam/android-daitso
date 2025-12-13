# Core Model 모듈 (:core:model)

**도메인 모델을 정의하는 순수 Kotlin 모듈**

---

## 개요

`:core:model` 모듈은 애플리케이션 전반에서 사용되는 도메인 모델을 정의합니다. 이 모듈은 **Android 의존성이 없는 순수 Kotlin 모듈**이므로, 다른 모든 모듈에서 안전하게 의존할 수 있습니다.

### 모듈 특징

- ✅ Android 의존성 없음 (순수 Kotlin)
- ✅ Kotlin Serialization 지원
- ✅ 타입 안전성 보장
- ✅ 모든 모듈에서 의존 가능

---

## 모듈 구조

```
core/model/
├── build.gradle.kts
└── src/main/kotlin/com/bup/ys/daitso/core/model/
    ├── Product.kt
    ├── CartItem.kt
    └── User.kt
```

---

## 주요 모델

### Product (상품)

```kotlin
@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val stock: Int = 0
)
```

**설명:**
- 상품 정보를 나타내는 데이터 클래스
- API 응답과 로컬 데이터베이스에서 사용
- Kotlin Serialization으로 JSON 직렬화/역직렬화 지원

**필드 설명:**
- `id`: 상품 고유 식별자
- `name`: 상품명
- `description`: 상품 설명
- `price`: 상품 가격
- `imageUrl`: 상품 이미지 URL
- `stock`: 상품의 현재 재고 수량 (기본값: 0, 범위: 0 이상)

**사용 예시:**

```kotlin
val product = Product(
    id = "1",
    name = "상품명",
    description = "상품 설명",
    price = 29999.0,
    imageUrl = "https://example.com/image.jpg",
    stock = 50
)
```

### CartItem (장바구니 항목)

```kotlin
@Serializable
data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String
)
```

**설명:**
- 장바구니에 담긴 상품 정보
- 수량과 개별 가격 포함
- Product와 함께 사용

**사용 예시:**

```kotlin
val cartItem = CartItem(
    productId = "1",
    productName = "상품명",
    quantity = 2,
    price = 29999.0,
    imageUrl = "https://example.com/image.jpg"
)
```

### User (사용자)

```kotlin
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String
)
```

**설명:**
- 사용자 정보를 나타내는 데이터 클래스
- 인증 및 프로필 정보에 사용
- 확장 가능한 구조

**사용 예시:**

```kotlin
val user = User(
    id = "user-123",
    name = "홍길동",
    email = "user@example.com"
)
```

---

## Kotlin Serialization

모든 모델은 `@Serializable` 어노테이션을 사용하여 Kotlin Serialization을 지원합니다.

### 직렬화 예시

```kotlin
import kotlinx.serialization.json.Json
import com.bup.ys.daitso.core.model.Product

// 객체 → JSON
val product = Product(
    id = "1",
    name = "상품",
    description = "설명",
    price = 29999.0,
    imageUrl = "url"
)
val json = Json.encodeToString(Product.serializer(), product)

// JSON → 객체
val decoded = Json.decodeFromString(Product.serializer(), json)
```

---

## 의존성

이 모듈의 의존성은 최소한으로 유지됩니다:

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
```

---

## 모듈 간 사용

### NetworkDataSource에서 사용

```kotlin
// :core:network
interface NetworkDataSource {
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: String): Product
}
```

### Repository에서 사용

```kotlin
// :core:data
interface ProductRepository {
    fun getProducts(): Flow<Result<List<Product>>>
    fun getProduct(id: String): Flow<Result<Product>>
}
```

### ViewModel에서 사용

```kotlin
// :app (ViewModel)
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    // Product 사용
}
```

---

## 확장 가이드

새로운 모델을 추가할 때는 다음 규칙을 따릅니다:

### 1. 데이터 클래스 정의

```kotlin
@Serializable
data class NewModel(
    val id: String,
    val name: String,
    val createdAt: String
)
```

### 2. 필수 어노테이션

```kotlin
// 모든 모델에 필수
@Serializable
data class Model(...)

// JSON 필드명 변경 필요시
@SerialName("field_name")
val fieldName: String
```

### 3. 복잡한 타입 처리

```kotlin
@Serializable
data class Order(
    val id: String,
    val items: List<CartItem>,  // 리스트 지원
    val user: User,             // 중첩 객체 지원
    val status: OrderStatus     // Enum 지원
)

@Serializable
enum class OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED
}
```

---

## 테스트

모듈 간 의존성이 없으므로, 이 모듈의 테스트는 간단합니다:

```kotlin
// src/test/kotlin/com/bup/ys/daitso/core/model/ProductTest.kt

import org.junit.Test
import kotlin.test.assertEquals

class ProductTest {
    @Test
    fun `Product data class works correctly`() {
        val product = Product(
            id = "1",
            name = "Test Product",
            description = "Test",
            price = 100.0,
            imageUrl = "url"
        )

        assertEquals("1", product.id)
        assertEquals("Test Product", product.name)
    }

    @Test
    fun `Product serialization works`() {
        val product = Product(...)
        val json = Json.encodeToString(Product.serializer(), product)
        val decoded = Json.decodeFromString(Product.serializer(), json)

        assertEquals(product, decoded)
    }
}
```

---

## 주의사항

1. **Android 의존성 금지**
   - `android.*` 패키지 import 금지
   - Context, Activity 등 Android 클래스 금지

2. **외부 라이브러리 최소화**
   - 필수 라이브러리만 추가
   - 무거운 라이브러리 피하기

3. **모델 설계**
   - 불변(immutable) 데이터 클래스 권장
   - getter/setter 대신 val/var 사용

---

## 참고

- [Kotlin Serialization 공식 문서](https://kotlinlang.org/docs/serialization.html)
- [SPEC-ANDROID-INIT-001](../../specs/SPEC-ANDROID-INIT-001/spec.md)

---

---

## Product 모델 확장 (2025-12-13)

### 신규 필드: stock

`stock` 필드가 Product 모델에 추가되었습니다. 이는 상품 상세 화면에서 재고 수량을 표시하기 위해 필요합니다.

**필드 상세:**
- **필드명**: `stock`
- **타입**: `Int`
- **기본값**: `0`
- **범위**: 0 이상 (음수 불가)
- **용도**: 상품 상세 화면에서 재고 표시 및 장바구니 추가 가능 여부 판단
- **업데이트 일시**: 2025-12-13

**사용 시나리오:**

```kotlin
// 상품 상세 화면에서 재고 표시
if (product.stock > 0) {
    Text("재고: ${product.stock}개")
    Button(onClick = { /* 장바구니 추가 */ }) {
        Text("장바구니 추가")
    }
} else {
    Text("품절")
    Button(onClick = { /* 재입고 알림 */ }, enabled = false) {
        Text("장바구니 추가")
    }
}
```

---

**최종 업데이트**: 2025-12-13
**SPEC 기반**: SPEC-ANDROID-FEATURE-DETAIL-001
