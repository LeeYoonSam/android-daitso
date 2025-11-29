# SPEC-ANDROID-FEATURE-CART-001: 장바구니 화면 기능 모듈 구현

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-FEATURE-CART-001
version: 1.0.0
status: pending
priority: high
domain: ANDROID-FEATURE
created_at: 2025-11-29
updated_at: 2025-11-29
owner: Team
completed_at: null
dependencies: [SPEC-ANDROID-INIT-001, SPEC-ANDROID-MVI-002]
related_specs: [SPEC-ANDROID-FEATURE-HOME-001, SPEC-ANDROID-FEATURE-DETAIL-001, SPEC-ANDROID-INTEGRATION-003]
tags: [android, mvi, feature, cart, compose, room-database, state-management]
```

---

## 📋 개요 (Overview)

장바구니 화면은 사용자가 추가한 상품들을 관리하는 Feature 모듈입니다. Room Database를 로컬 저장소로 사용하여 데이터 지속성을 보장하고, MVI 아키텍처를 적용하여 아이템 조회, 수량 변경, 삭제, 그리고 총 가격 계산 기능을 구현합니다.

**주요 기능:**
- Room DB에서 장바구니 아이템 조회
- 아이템 수량 증가/감소
- 아이템 삭제
- 총 가격 계산 및 표시
- 빈 장바구니 상태 처리
- 결제하기 버튼 (추후 확장)

**모듈 위치:** `:feature:cart`

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
- Material3 디자인 시스템
- MVI 아키텍처 패턴
- Room Database 2.6.1
- Coroutines 1.9.0 (Flow 기반)
- Hilt 2.54

---

## 🔧 Assumptions (가정)

**기술 가정:**
1. Room Database (:core:database 모듈)에 CartItemEntity, CartDao가 구현됨
2. CartRepository가 CRUD 작업을 지원함
3. 데이터 지속성: Room DB가 Single Source of Truth
4. Flow 기반 비동기 데이터 관리
5. Hilt DI 전역 적용

**설계 가정:**
1. 장바구니 아이템은 리스트 형태로 표시
2. 각 아이템에는 이미지, 이름, 가격, 수량, 삭제 버튼 표시
3. 총 가격은 실시간으로 계산 및 업데이트
4. 스와이프로 아이템 삭제 또는 삭제 버튼 사용 (선택)
5. 빈 장바구니 상태에서 "장바구니가 비어있습니다" 메시지 표시

**제약 조건:**
1. XML 레이아웃 사용 금지
2. Room DB 스키마 변경 시 마이그레이션 필수
3. 트랜잭션 처리로 데이터 일관성 보장

---

## 📐 Requirements (요구사항)

### 기능 요구사항 (FR)

#### FR-CART-001: Room DB에서 장바구니 아이템 조회

**WHEN** 장바구니 화면을 열었을 때,
**THEN** 시스템은 Room DB에서 모든 CartItemEntity를 조회하고 UI에 표시해야 한다.

**세부 사항:**
- CartDao.getCartItems(): Flow<List<CartItemEntity>>
- 비동기 쿼리로 데이터 스트리밍
- 데이터 변경 시 자동으로 UI 업데이트

#### FR-CART-002: 아이템 수량 증가/감소

**WHEN** 사용자가 +/- 버튼을 클릭했을 때,
**THEN** 시스템은 CartItemEntity의 quantity를 수정하고 DB에 저장해야 한다.

**세부 사항:**
- CartDao.updateCartItem(item)
- 최소값: 1, 최대값: 999
- 수량 변경 시 총 가격 자동 재계산

#### FR-CART-003: 아이템 삭제 기능

**WHEN** 사용자가 삭제 버튼을 클릭했을 때,
**THEN** 시스템은 CartItemEntity를 DB에서 삭제해야 한다.

**세부 사항:**
- CartDao.deleteCartItem(item)
- 스와이프 제스처 또는 버튼으로 삭제 (선택)
- 삭제 후 Undo 기능 (선택)

#### FR-CART-004: 총 가격 계산 및 표시

**WHEN** 장바구니 아이템이 변경될 때,
**THEN** 시스템은 총 가격을 다음과 같이 계산해야 한다:
```
총 가격 = Sum(각 아이템의 가격 * 수량)
```

**세부 사항:**
- 실시간으로 계산
- 원화 포맷(₩) 표시
- 소수점 처리 (반올림)

#### FR-CART-005: 빈 장바구니 상태 처리

**WHEN** 장바구니에 아이템이 없을 때,
**THEN** 시스템은 빈 상태 UI를 표시해야 한다.

**세부 사항:**
- "장바구니가 비어있습니다" 메시지
- Shopping 아이콘 표시
- Home 화면으로 돌아가기 버튼

#### FR-CART-006: 결제하기 버튼

**WHEN** 사용자가 결제하기 버튼을 클릭했을 때,
**THEN** 시스템은 결제 프로세스로 진행해야 한다 (추후 구현).

### 비기능 요구사항 (NFR)

#### NFR-CART-001: UI 업데이트 반응성

아이템 수량 변경 시 UI 업데이트 < 100ms

#### NFR-CART-002: 테스트 커버리지

코드 커버리지 95%+

### 인터페이스 요구사항

#### CartContract

```kotlin
// UI 상태
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Intent
sealed interface CartIntent {
    object LoadCartItems : CartIntent
    data class UpdateQuantity(val productId: String, val quantity: Int) : CartIntent
    data class RemoveItem(val productId: String) : CartIntent
    object ClearCart : CartIntent
    object DismissError : CartIntent
}

// SideEffect
sealed interface CartSideEffect {
    object NavigateToCheckout : CartSideEffect
    data class ShowToast(val message: String) : CartSideEffect
    data class NavigateToHome : CartSideEffect
}
```

---

## 🎯 Acceptance Criteria

### AC-CART-001: CartContract 정의

조건:
- CartUiState, Intent, SideEffect 명확히 정의됨
- 모든 UI 상태 표현 가능

### AC-CART-002: CartViewModel 구현 (Room 통합)

조건:
- CartRepository 주입
- Room DB와 통합 (CartDao 사용)
- LoadCartItems, UpdateQuantity, RemoveItem Intent 처리
- 총 가격 자동 계산
- Flow 기반 상태 관리

### AC-CART-003: CartScreen Composable 구현

조건:
- CartScreen이 UiState를 읽고 UI 렌더링
- LazyColumn으로 아이템 리스트 표시
- CartItem 컴포넌트 (이미지, 이름, 가격, 수량, 삭제)
- 총 가격 표시
- 결제하기 버튼
- 빈 상태 UI
- 로딩/에러 상태 처리

### AC-CART-004: 데이터 지속성 검증

조건:
- 앱 재시작 후에도 장바구니 데이터 유지
- Room DB 트랜잭션으로 데이터 일관성 보장

---

## 🔗 Traceability

**의존 SPEC:**
- SPEC-ANDROID-INIT-001
- SPEC-ANDROID-MVI-002

**관련 SPEC:**
- SPEC-ANDROID-FEATURE-DETAIL-001
- SPEC-ANDROID-INTEGRATION-003

---

**END OF SPEC**
