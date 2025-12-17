# SPEC-ANDROID-STANDARDIZE-001: Feature 모듈 MVI 용어 및 Contract 패턴 표준화

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-STANDARDIZE-001
version: 1.0.0
status: draft
priority: high
domain: ANDROID-STANDARDIZE
created_at: 2025-12-17
updated_at: 2025-12-17
owner: Team
dependencies: [SPEC-ANDROID-MVI-002]
related_specs: [SPEC-ANDROID-REFACTOR-001, SPEC-ANDROID-ARCH-001]
tags: [android, mvi, standardization, contract, naming-convention]
```

---

## HISTORY

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 1.0.0 | 2025-12-17 | 초기 SPEC 작성 | R2-D2 |

---

## 📋 개요 (Overview)

본 SPEC은 Android MVI 모듈화 프로젝트에서 MVI 용어 및 Contract 패턴의 일관성을 확보하기 위한 표준화 작업을 정의합니다.

**문제점:**
- Home Feature: `HomeEvent` 사용
- Detail Feature: `ProductDetailIntent` 사용
- Cart Feature: `CartIntent` 사용
- Contract 정의 패턴 불일치 (Object vs 분산 정의)

**목표:**
- 모든 Feature에서 일관된 MVI 용어 사용
- 표준화된 Contract 정의 패턴 적용
- 코드 가독성 및 유지보수성 향상

**범위:**
- MVI 용어 통일 (Event 또는 Intent)
- Contract 정의 패턴 통일

---

## 🌍 Environment (환경)

**개발 환경:**
- Android Studio: 2024.1.2 이상
- Kotlin: 2.1.0

**대상 모듈:**
- `:feature:home`
- `:feature:detail`
- `:feature:cart`

---

## 🔧 Assumptions (가정)

**기술 가정:**
1. 모든 Feature 모듈이 SPEC-ANDROID-MVI-002의 `UiState`, `UiEvent`, `UiSideEffect` 인터페이스를 상속함
2. 기존 코드의 동작은 변경하지 않고 네이밍만 변경

**설계 가정:**
1. MVI 용어는 "Event"로 통일 (SPEC-ANDROID-MVI-002의 `UiEvent` 인터페이스 명칭 준수)
2. Contract 정의는 분리 파일 패턴 적용 (각 State, Event, SideEffect 별도 정의)

**제약 조건:**
1. 기존 기능 동작 유지
2. IDE 리팩토링 기능 활용

---

## 📐 Requirements (요구사항)

### 기능 요구사항 (FR)

#### FR-STD-001: MVI 용어 표준화 - Event 통일

**WHEN** Feature 모듈에서 사용자 인터렉션을 정의할 때,
**THEN** 시스템은 모든 Feature에서 `{Feature}Event`로 명명해야 한다:

```kotlin
// 표준 명명 규칙
sealed interface HomeEvent : UiEvent { ... }
sealed interface ProductDetailEvent : UiEvent { ... }  // Intent → Event
sealed interface CartEvent : UiEvent { ... }           // Intent → Event
```

**변경 대상:**
- `ProductDetailIntent` → `ProductDetailEvent`
- `CartIntent` → `CartEvent`

#### FR-STD-002: ViewModel 참조 업데이트

**WHEN** ViewModel에서 사용자 인터렉션을 처리할 때,
**THEN** 시스템은 변경된 Event 타입을 참조해야 한다:

```kotlin
// BEFORE
class ProductDetailViewModel : BaseViewModel<..., ProductDetailIntent, ...>

// AFTER
class ProductDetailViewModel : BaseViewModel<..., ProductDetailEvent, ...>
```

#### FR-STD-003: UI 참조 업데이트

**WHEN** Composable에서 이벤트를 전달할 때,
**THEN** 시스템은 변경된 Event 타입을 사용해야 한다:

```kotlin
// BEFORE
viewModel.onEvent(ProductDetailIntent.AddToCart(quantity))

// AFTER
viewModel.onEvent(ProductDetailEvent.AddToCart(quantity))
```

### 비기능 요구사항 (NFR)

#### NFR-STD-001: 기존 기능 유지

모든 리팩토링 후 기존 기능이 동일하게 동작해야 함

#### NFR-STD-002: 테스트 통과

모든 기존 테스트가 통과해야 함

---

## 🎯 Acceptance Criteria

### AC-STD-001: Intent 용어 제거 확인

**GIVEN** 모든 Feature 모듈이 존재할 때,
**WHEN** Contract 파일에서 `Intent`를 검색하면,
**THEN** 검색 결과가 0건이어야 한다.

**검증:**
```bash
grep -r "Intent" --include="*Contract.kt" feature/
# 결과: 0건
```

### AC-STD-002: Event 용어 사용 확인

**GIVEN** 모든 Feature 모듈이 존재할 때,
**WHEN** Contract 파일에서 `Event`를 검색하면,
**THEN** 모든 Feature에서 `{Feature}Event` 형식을 사용해야 한다.

**검증:**
```bash
grep -r "sealed interface.*Event" --include="*Contract.kt" feature/
# 결과: HomeEvent, ProductDetailEvent, CartEvent
```

### AC-STD-003: 빌드 및 테스트 성공

**GIVEN** 모든 변경이 완료되었을 때,
**WHEN** 빌드 및 테스트를 실행하면,
**THEN** 모든 빌드와 테스트가 성공해야 한다.

**검증:**
```bash
./gradlew test assembleDebug
# 결과: BUILD SUCCESSFUL
```

---

## 🔗 Traceability

**의존 SPEC:**
- SPEC-ANDROID-MVI-002: MVI 아키텍처 기본 구조

**관련 SPEC:**
- SPEC-ANDROID-REFACTOR-001: 전체 리팩토링 (상위 집합)
- SPEC-ANDROID-ARCH-001: 디렉토리 구조 표준화

**영향 받는 파일:**
- `feature/detail/contract/ProductDetailContract.kt`
- `feature/detail/viewmodel/ProductDetailViewModel.kt`
- `feature/detail/ui/ProductDetailScreen.kt`
- `feature/cart/contract/CartContract.kt`
- `feature/cart/presentation/CartViewModel.kt`
- `feature/cart/presentation/CartScreen.kt`
- 관련 테스트 파일들

---

**END OF SPEC**
