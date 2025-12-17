# SPEC-ANDROID-ARCH-001: Implementation Plan

## 구현 계획서

### 개요

이 문서는 Feature 모듈 디렉토리 구조 및 DI 표준화의 구현 계획을 상세히 기술합니다.

---

## Phase 1: Home Feature 표준화 (예상 시간: 30분)

### Task 1.1: Home DI Module 생성

**파일:** `feature/home/src/main/kotlin/com/bup/ys/daitso/feature/home/di/HomeModule.kt`

**생성 내용:**
```kotlin
package com.bup.ys.daitso.feature.home.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    // Home Feature 특화 의존성
    // 현재는 빈 모듈이나 일관성을 위해 생성
}
```

### Task 1.2: Home Navigation 생성

**파일:** `feature/home/src/main/kotlin/com/bup/ys/daitso/feature/home/navigation/HomeNavigation.kt`

**생성 내용:**
```kotlin
package com.bup.ys.daitso.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bup.ys.daitso.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavGraphBuilder.homeScreen(
    onNavigateToDetail: (productId: String) -> Unit
) {
    composable<HomeRoute> {
        HomeScreen(onNavigateToDetail = onNavigateToDetail)
    }
}
```

### Task 1.3: 디렉토리 생성

```bash
mkdir -p feature/home/src/main/kotlin/com/bup/ys/daitso/feature/home/di
mkdir -p feature/home/src/main/kotlin/com/bup/ys/daitso/feature/home/navigation
```

---

## Phase 2: Detail Feature 표준화 (예상 시간: 30분)

### Task 2.1: Detail Navigation 생성

**파일:** `feature/detail/src/main/kotlin/com/bup/ys/daitso/feature/detail/navigation/DetailNavigation.kt`

**생성 내용:**
```kotlin
package com.bup.ys.daitso.feature.detail.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bup.ys.daitso.feature.detail.ui.ProductDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailRoute(val productId: String)

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

### Task 2.2: 디렉토리 생성

```bash
mkdir -p feature/detail/src/main/kotlin/com/bup/ys/daitso/feature/detail/navigation
```

---

## Phase 3: Cart Feature 확인 (예상 시간: 10분)

### Task 3.1: 기존 구조 확인

Cart Feature는 이미 표준 구조를 갖추고 있으므로 참조 표준으로 사용:
- `di/CartModule.kt` ✅
- `navigation/CartNavigation.kt` ✅

### Task 3.2: presentation → ui 명칭 변경 (선택적)

**현재:** `feature/cart/.../presentation/`
**목표:** `feature/cart/.../ui/` (선택적 - 기능 영향 없이 일관성만 향상)

**참고:** 이 변경은 선택적이며, 기존 동작에 영향을 주지 않습니다.

---

## Phase 4: 검증 및 테스트 (예상 시간: 20분)

### Task 4.1: 빌드 검증

```bash
./gradlew assembleDebug
```

### Task 4.2: 디렉토리 구조 확인

```bash
# 모든 Feature에서 di/ 디렉토리 확인
find feature -name "*Module.kt" -path "*/di/*" | wc -l
# 예상 결과: 3

# 모든 Feature에서 navigation/ 파일 확인
find feature -name "*Navigation.kt" | wc -l
# 예상 결과: 3
```

### Task 4.3: 테스트 실행

```bash
./gradlew test
```

---

## 구현 체크리스트

### Phase 1: Home Feature
- [ ] di/ 디렉토리 생성
- [ ] HomeModule.kt 생성
- [ ] navigation/ 디렉토리 생성
- [ ] HomeNavigation.kt 생성
- [ ] 빌드 성공 확인

### Phase 2: Detail Feature
- [ ] navigation/ 디렉토리 생성
- [ ] DetailNavigation.kt 생성
- [ ] 빌드 성공 확인

### Phase 3: Cart Feature
- [ ] 기존 구조 확인 완료
- [ ] (선택) presentation → ui 명칭 변경

### Phase 4: 검증
- [ ] 전체 빌드 성공
- [ ] 모든 테스트 통과
- [ ] 디렉토리 구조 일관성 확인

---

## 예상 총 작업 시간: 1.5시간

| Phase | 예상 시간 |
|-------|----------|
| Phase 1: Home Feature | 30분 |
| Phase 2: Detail Feature | 30분 |
| Phase 3: Cart Feature | 10분 |
| Phase 4: 검증 및 테스트 | 20분 |

---

## 의존성 및 영향 분석

### 영향 받는 모듈
- `:feature:home` - 새 파일 추가 (di/, navigation/)
- `:feature:detail` - 새 파일 추가 (navigation/)
- `:feature:cart` - 변경 없음 (참조 표준)

### 기존 코드 영향
- 기존 기능 동작: 영향 없음
- 빌드 설정: 변경 없음
- 테스트: 영향 없음

### 추가 고려사항
- App 모듈의 Navigation 설정이 새 Navigation 파일을 사용하도록 업데이트 필요
- 현재 App 모듈에서 직접 정의된 Navigation이 있다면 Feature 모듈로 이동 권장

---

**END OF PLAN**
