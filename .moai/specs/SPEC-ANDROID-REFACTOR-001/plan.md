# SPEC-ANDROID-REFACTOR-001: Implementation Plan

## 구현 계획서

### 개요

이 문서는 SPEC-ANDROID-REFACTOR-001의 구현 계획을 상세히 기술합니다.

---

## Phase 1: MVI 용어 표준화 (예상 시간: 2시간)

### Task 1.1: Detail Feature Intent → Event 변경

**파일:** `feature/detail/src/main/kotlin/.../contract/ProductDetailContract.kt`

**변경 내용:**
```kotlin
// BEFORE
sealed interface ProductDetailIntent : UiEvent { ... }

// AFTER
sealed interface ProductDetailEvent : UiEvent { ... }
```

**관련 파일 업데이트:**
- `ProductDetailViewModel.kt` - 참조 업데이트
- `ProductDetailScreen.kt` - 참조 업데이트
- `ProductDetailContractTest.kt` - 테스트 업데이트

### Task 1.2: Cart Feature Intent → Event 변경

**파일:** `feature/cart/src/main/kotlin/.../contract/CartContract.kt`

**변경 내용:**
```kotlin
// BEFORE
sealed interface CartIntent : UiEvent { ... }

// AFTER
sealed interface CartEvent : UiEvent { ... }
```

**관련 파일 업데이트:**
- `CartViewModel.kt` - 참조 업데이트
- `CartScreen.kt` - 참조 업데이트
- `CartContractTest.kt` - 테스트 업데이트

---

## Phase 2: CartRepository 통합 (예상 시간: 4시간)

### Task 2.1: 통합 CartRepository 인터페이스 생성

**파일:** `core/data/src/main/kotlin/.../repository/CartRepository.kt`

```kotlin
package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.model.CartItem
import kotlinx.coroutines.flow.Flow

/**
 * 통합 CartRepository 인터페이스
 *
 * Cart 관련 모든 데이터 접근을 단일 인터페이스로 제공합니다.
 */
interface CartRepository {
    /**
     * 장바구니 아이템 목록을 Flow로 조회합니다.
     */
    fun getCartItems(): Flow<List<CartItem>>

    /**
     * 상품을 장바구니에 추가합니다.
     *
     * @param productId 상품 ID
     * @param quantity 수량
     * @return 성공 여부
     */
    suspend fun addToCart(productId: String, quantity: Int): Boolean

    /**
     * 장바구니 아이템 수량을 업데이트합니다.
     *
     * @param productId 상품 ID
     * @param quantity 새 수량
     */
    suspend fun updateQuantity(productId: String, quantity: Int)

    /**
     * 장바구니에서 아이템을 삭제합니다.
     *
     * @param productId 상품 ID
     */
    suspend fun removeItem(productId: String)

    /**
     * 장바구니를 비웁니다.
     */
    suspend fun clearCart()
}
```

### Task 2.2: 통합 CartRepositoryImpl 생성

**파일:** `core/data/src/main/kotlin/.../repository/CartRepositoryImpl.kt`

```kotlin
package com.bup.ys.daitso.core.data.repository

import com.bup.ys.daitso.core.data.datasource.LocalDataSource
import com.bup.ys.daitso.core.model.CartItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * CartRepository 구현체
 *
 * LocalDataSource를 사용하여 Cart 데이터를 관리합니다.
 */
class CartRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return localDataSource.getCartItems()
    }

    override suspend fun addToCart(productId: String, quantity: Int): Boolean {
        return try {
            val product = localDataSource.getProduct(productId)
            if (product != null) {
                val cartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    price = product.price,
                    imageUrl = product.imageUrl
                )
                localDataSource.insertCartItem(cartItem)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        // Get existing cart items, find matching, and update
        // Implementation will merge existing logic from feature modules
    }

    override suspend fun removeItem(productId: String) {
        val cartItem = CartItem(productId = productId, productName = "", quantity = 0, price = 0.0, imageUrl = "")
        localDataSource.deleteCartItem(cartItem)
    }

    override suspend fun clearCart() {
        localDataSource.clearCart()
    }
}
```

### Task 2.3: DI Module 업데이트

**파일:** `core/data/src/main/kotlin/.../di/DataModule.kt`

```kotlin
@Binds
abstract fun bindCartRepository(
    impl: CartRepositoryImpl
): CartRepository
```

### Task 2.4: Feature 모듈 중복 파일 삭제

- 삭제: `feature/cart/domain/CartRepository.kt`
- 삭제: `feature/cart/repository/CartRepositoryImpl.kt`
- 삭제: `feature/detail/repository/CartRepository.kt`
- 삭제: `feature/detail/repository/CartRepositoryImpl.kt`

### Task 2.5: Feature 모듈 의존성 업데이트

- `feature/detail/build.gradle.kts` - `core:data` 의존성 추가
- `feature/cart/build.gradle.kts` - 로컬 Repository 제거, `core:data` 사용

---

## Phase 3: 디렉토리 구조 표준화 (예상 시간: 2시간)

### Task 3.1: Home 모듈 DI 추가

**파일:** `feature/home/src/main/kotlin/.../di/HomeModule.kt`

```kotlin
package com.bup.ys.daitso.feature.home.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Home Feature DI Module
 *
 * Home 특화 의존성을 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    // Home 특화 의존성 바인딩
    // 현재는 core:data의 ProductRepository를 직접 사용하므로 추가 바인딩 불필요
}
```

### Task 3.2: Home Navigation 추가

**파일:** `feature/home/src/main/kotlin/.../navigation/HomeNavigation.kt`

```kotlin
package com.bup.ys.daitso.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bup.ys.daitso.core.ui.navigation.HomeRoute
import com.bup.ys.daitso.feature.home.ui.HomeScreen

/**
 * Home Feature Navigation
 */
fun NavGraphBuilder.homeScreen(
    onNavigateToDetail: (productId: String) -> Unit
) {
    composable<HomeRoute> {
        HomeScreen(onNavigateToDetail = onNavigateToDetail)
    }
}
```

### Task 3.3: Detail Navigation 추가

**파일:** `feature/detail/src/main/kotlin/.../navigation/DetailNavigation.kt`

```kotlin
package com.bup.ys.daitso.feature.detail.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bup.ys.daitso.core.ui.navigation.ProductDetailRoute
import com.bup.ys.daitso.feature.detail.ui.ProductDetailScreen

/**
 * Detail Feature Navigation
 */
fun NavGraphBuilder.productDetailScreen(
    onNavigateToCart: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable<ProductDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ProductDetailRoute>()
        ProductDetailScreen(
            productId = route.productId,
            onNavigateToCart = onNavigateToCart,
            onNavigateBack = onNavigateBack
        )
    }
}
```

---

## Phase 4: 테스트 업데이트 (예상 시간: 2시간)

### Task 4.1: Contract 테스트 업데이트

- `ProductDetailContractTest.kt` - Intent → Event 참조 변경
- `CartContractTest.kt` - Intent → Event 참조 변경

### Task 4.2: Repository 테스트 이동

- `CartRepositoryTest.kt` → `core/data/src/test/`로 이동
- `CartRepositoryImplTest.kt` → `core/data/src/test/`로 이동

### Task 4.3: 통합 테스트 실행

```bash
./gradlew test
./gradlew assembleDebug
```

---

## 체크리스트

### Phase 1 완료 조건
- [ ] Detail Feature Intent → Event 변경
- [ ] Cart Feature Intent → Event 변경
- [ ] 관련 테스트 업데이트
- [ ] 빌드 성공

### Phase 2 완료 조건
- [ ] 통합 CartRepository 인터페이스 생성
- [ ] CartRepositoryImpl 구현
- [ ] DI Module 업데이트
- [ ] 중복 파일 삭제
- [ ] Feature 모듈 의존성 업데이트
- [ ] 빌드 성공

### Phase 3 완료 조건
- [ ] HomeModule.kt 생성
- [ ] HomeNavigation.kt 생성
- [ ] DetailNavigation.kt 생성
- [ ] 빌드 성공

### Phase 4 완료 조건
- [ ] 모든 테스트 통과
- [ ] 테스트 커버리지 90% 이상
- [ ] 빌드 성공

---

## 예상 총 작업 시간: 10시간

| Phase | 예상 시간 |
|-------|----------|
| Phase 1: MVI 용어 표준화 | 2시간 |
| Phase 2: CartRepository 통합 | 4시간 |
| Phase 3: 디렉토리 구조 표준화 | 2시간 |
| Phase 4: 테스트 업데이트 | 2시간 |

---

**END OF PLAN**
