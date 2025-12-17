# SPEC-ANDROID-REFACTOR-001: 중복 Repository 통합 및 Clean Architecture 적용

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-REFACTOR-001
version: 1.0.0
status: draft
priority: critical
domain: ANDROID-REFACTOR
created_at: 2025-12-17
updated_at: 2025-12-17
owner: Team
dependencies: [SPEC-ANDROID-MVI-002, SPEC-ANDROID-INTEGRATION-003]
related_specs: [SPEC-ANDROID-STANDARDIZE-001, SPEC-ANDROID-ARCH-001]
tags: [android, refactoring, clean-architecture, repository, mvi, modularization]
```

---

## HISTORY

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 1.0.0 | 2025-12-17 | 초기 SPEC 작성 | R2-D2 |

---

## 📋 개요 (Overview)

본 SPEC은 Android MVI 모듈화 프로젝트에서 발견된 아키텍처 불일치 및 중복 코드를 해결하기 위한 종합적인 리팩토링을 정의합니다.

**주요 문제점:**
1. MVI 용어 불일치 (Event vs Intent)
2. 중복된 CartRepository 인터페이스
3. 불일치한 디렉토리 구조
4. Home 모듈 DI 누락
5. Navigation 패턴 불완전

**목표:**
- 모듈화 건강도: 75점 → 90점 이상
- 코드 일관성 100% 달성
- Clean Architecture 원칙 완전 적용

---

## 🌍 Environment (환경)

**개발 환경:**
- Android Studio: 2024.1.2 이상
- Kotlin: 2.1.0
- Android Gradle Plugin: 8.7.3
- Gradle: 8.11.1

**타겟 환경:**
- minSdk: 26
- targetSdk: 35
- compileSdk: 35

**기술 스택:**
- Jetpack Compose 1.7.5
- Hilt 2.54
- Coroutines 1.9.0
- Room Database
- Navigation Compose

---

## 🔧 Assumptions (가정)

**기술 가정:**
1. 모든 Feature 모듈이 SPEC-ANDROID-MVI-002의 UiState, UiEvent, UiSideEffect 인터페이스를 상속함
2. Hilt DI를 사용한 의존성 주입 구조 유지
3. Clean Architecture 원칙 적용 (domain/data/presentation 레이어 분리)
4. 기존 테스트 커버리지 90% 이상 유지

**설계 가정:**
1. Cart 관련 데이터 접근은 단일 Repository를 통해 관리
2. MVI 용어는 Event로 통일 (SPEC-ANDROID-MVI-002 준수)
3. 각 Feature 모듈은 표준화된 디렉토리 구조를 따름

**제약 조건:**
1. 하위 호환성 유지 (기존 API 서명 유지)
2. 점진적 마이그레이션 지원
3. 빌드 실패 없이 각 단계 완료

---

## 📐 Requirements (요구사항)

### 기능 요구사항 (FR)

#### FR-REFACTOR-001: MVI 용어 표준화

**WHEN** Feature 모듈에서 사용자 인터렉션을 정의할 때,
**THEN** 시스템은 모든 Feature에서 일관되게 "Event" 용어를 사용해야 한다:

```kotlin
// BEFORE
sealed interface ProductDetailIntent : UiEvent { ... }
sealed interface CartIntent : UiEvent { ... }

// AFTER
sealed interface ProductDetailEvent : UiEvent { ... }
sealed interface CartEvent : UiEvent { ... }
```

**영향 파일:**
- `feature/detail/contract/ProductDetailContract.kt`
- `feature/cart/contract/CartContract.kt`
- 관련 ViewModel 및 테스트 파일

#### FR-REFACTOR-002: CartRepository 통합

**WHEN** Cart 관련 데이터 접근이 필요할 때,
**THEN** 시스템은 `:core:data` 모듈의 단일 CartRepository를 통해 다음 기능을 제공해야 한다:

```kotlin
interface CartRepository {
    // 장바구니 조회
    fun getCartItems(): Flow<List<CartItem>>

    // 장바구니 추가
    suspend fun addToCart(productId: String, quantity: Int): Boolean

    // 수량 업데이트
    suspend fun updateQuantity(productId: String, quantity: Int)

    // 아이템 삭제
    suspend fun removeItem(productId: String)

    // 장바구니 비우기
    suspend fun clearCart()
}
```

**변경 사항:**
- `feature/cart/domain/CartRepository.kt` → 삭제 (통합)
- `feature/detail/repository/CartRepository.kt` → 삭제 (통합)
- `core/data/repository/CartRepository.kt` → 신규 생성 (통합)

#### FR-REFACTOR-003: 디렉토리 구조 표준화

**WHEN** Feature 모듈을 구성할 때,
**THEN** 시스템은 다음 표준 구조를 따라야 한다:

```
feature/{feature-name}/
├── contract/
│   ├── {Feature}State.kt
│   ├── {Feature}Event.kt
│   └── {Feature}SideEffect.kt
├── di/
│   └── {Feature}Module.kt
├── navigation/
│   └── {Feature}Navigation.kt
├── ui/
│   └── {Feature}Screen.kt
└── viewmodel/
    └── {Feature}ViewModel.kt
```

#### FR-REFACTOR-004: Home 모듈 DI 추가

**WHEN** Home Feature 모듈이 Hilt를 통해 의존성 주입을 받을 때,
**THEN** 시스템은 `HomeModule.kt`를 통해 필요한 의존성을 제공해야 한다:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    // Home 특화 의존성
}
```

#### FR-REFACTOR-005: Navigation 패턴 일관화

**WHEN** 각 Feature 모듈에서 화면 네비게이션을 정의할 때,
**THEN** 시스템은 `{Feature}Navigation.kt` 파일을 통해 NavGraphBuilder 확장 함수를 제공해야 한다:

```kotlin
// HomeNavigation.kt
fun NavGraphBuilder.homeScreen(
    onNavigateToDetail: (productId: String) -> Unit
) {
    composable<HomeRoute> {
        HomeScreen(onNavigateToDetail = onNavigateToDetail)
    }
}
```

### 비기능 요구사항 (NFR)

#### NFR-REFACTOR-001: 테스트 커버리지

테스트 커버리지 90% 이상 유지

#### NFR-REFACTOR-002: 빌드 성능

리팩토링 후 빌드 시간 증가 10% 이내

#### NFR-REFACTOR-003: 하위 호환성

기존 공개 API 서명 변경 최소화

---

## 🎯 Acceptance Criteria

### AC-REFACTOR-001: MVI 용어 통일 완료

**GIVEN** 모든 Feature 모듈이 존재할 때,
**WHEN** MVI Contract를 확인하면,
**THEN** 모든 사용자 인터렉션 정의가 `{Feature}Event`로 명명되어야 한다.

**검증:**
```bash
grep -r "Intent" --include="*Contract.kt" feature/
# 결과: 0건 (Intent 용어 없음)
```

### AC-REFACTOR-002: CartRepository 단일화 완료

**GIVEN** Cart 관련 기능이 필요할 때,
**WHEN** Repository를 검색하면,
**THEN** `core/data` 모듈에만 CartRepository가 존재해야 한다.

**검증:**
```bash
find . -name "CartRepository.kt" | wc -l
# 결과: 1 (core/data에만 존재)
```

### AC-REFACTOR-003: 디렉토리 구조 표준화 완료

**GIVEN** 모든 Feature 모듈이 존재할 때,
**WHEN** 디렉토리 구조를 확인하면,
**THEN** 모든 Feature가 `contract/`, `di/`, `navigation/`, `ui/`, `viewmodel/` 폴더를 포함해야 한다.

### AC-REFACTOR-004: 테스트 통과

**GIVEN** 리팩토링이 완료되었을 때,
**WHEN** 전체 테스트를 실행하면,
**THEN** 모든 테스트가 통과하고 커버리지 90% 이상이어야 한다.

**검증:**
```bash
./gradlew test
# 결과: BUILD SUCCESSFUL
```

---

## 🔗 Traceability

**의존 SPEC:**
- SPEC-ANDROID-MVI-002: MVI 아키텍처 기본 구조
- SPEC-ANDROID-INTEGRATION-003: 앱 통합 및 전체 네비게이션

**관련 SPEC:**
- SPEC-ANDROID-STANDARDIZE-001: MVI 용어 표준화 (부분 집합)
- SPEC-ANDROID-ARCH-001: 디렉토리 구조 표준화 (부분 집합)

**영향 받는 모듈:**
- `:feature:home`
- `:feature:detail`
- `:feature:cart`
- `:core:data`

---

## 📋 영향 분석

### 수정 대상 파일

| 모듈 | 파일 | 변경 유형 |
|------|------|----------|
| :feature:detail | `ProductDetailContract.kt` | Rename |
| :feature:detail | `ProductDetailViewModel.kt` | Update |
| :feature:detail | `repository/CartRepository.kt` | Delete |
| :feature:detail | `repository/CartRepositoryImpl.kt` | Delete |
| :feature:cart | `CartContract.kt` | Rename |
| :feature:cart | `CartViewModel.kt` | Update |
| :feature:cart | `domain/CartRepository.kt` | Move |
| :feature:cart | `repository/CartRepositoryImpl.kt` | Move |
| :feature:home | `di/HomeModule.kt` | Create |
| :feature:home | `navigation/HomeNavigation.kt` | Create |
| :feature:detail | `navigation/DetailNavigation.kt` | Create |
| :core:data | `repository/CartRepository.kt` | Create |
| :core:data | `repository/CartRepositoryImpl.kt` | Create |

---

**END OF SPEC**
