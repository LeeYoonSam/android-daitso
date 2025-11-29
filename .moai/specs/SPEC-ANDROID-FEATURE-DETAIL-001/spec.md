# SPEC-ANDROID-FEATURE-DETAIL-001: 상품 상세 화면 기능 모듈

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-FEATURE-DETAIL-001
version: 1.0.0
status: pending
priority: high
domain: ANDROID-FEATURE
created_at: 2025-11-29
updated_at: 2025-11-29
owner: Team
completed_at: null
dependencies: [SPEC-ANDROID-INIT-001, SPEC-ANDROID-MVI-002, SPEC-ANDROID-FEATURE-HOME-001]
related_specs: [SPEC-ANDROID-FEATURE-HOME-001, SPEC-ANDROID-FEATURE-CART-001, SPEC-ANDROID-INTEGRATION-003]
tags: [android, mvi, feature, detail, compose, state-management, navigation]
```

---

## 📋 개요 (Overview)

상품 상세 화면은 Home 화면에서 선택된 상품의 상세 정보를 표시하고, 사용자가 상품을 장바구니에 추가할 수 있는 Feature 모듈입니다. MVI 아키텍처를 적용하여 상품 정보 로드, 수량 선택, 장바구니 추가 기능을 구현합니다.

**주요 기능:**
- 상품 상세 정보 표시 (이미지, 설명, 가격, 리뷰)
- 수량 선택 (기본값: 1)
- 장바구니 담기 버튼 기능
- 장바구니 추가 완료 피드백
- 뒤로가기 네비게이션

**모듈 위치:** `:feature:detail`

---

## 🌍 Environment (환경)

**개발 환경:**
- Android Studio: 2024.1.2 이상
- Kotlin: 2.1.0
- Android Gradle Plugin: 8.7.3
- Gradle: 8.11.1

**타겟 환경:**
- minSdk: 26 (Android 8.0 Oreo)
- targetSdk: 35 (Android 15)
- compileSdk: 35

**기술 스택:**
- Jetpack Compose 1.7.5
- Material3 디자인 시스템
- MVI 아키텍처 패턴
- Coroutines 1.9.0
- Hilt 2.54

---

## 🔧 Assumptions (가정)

**기술 가정:**
1. Home 화면에서 상품 ID를 타입 안전한 네비게이션 파라미터로 전달
2. ProductRepository가 개별 상품 조회 기능 제공 (getProduct(id))
3. CartRepository가 장바구니 추가 기능 제공
4. `:core:model`에 Product 데이터 클래스 정의됨
5. Hilt DI가 전역에 적용됨

**설계 가정:**
1. 상품 이미지는 전체 너비로 표시
2. 수량은 숫자 입력 또는 +/- 버튼으로 선택
3. 장바구니 추가 후 토스트 메시지 또는 Snackbar 표시
4. 뒤로가기는 시스템 백 버튼 또는 앱 바 뒤로가기 버튼

**제약 조건:**
1. XML 레이아웃 사용 금지
2. `:feature:detail` 모듈은 `:feature:home`, `:feature:cart` 모듈에 직접 의존하지 않음

---

## 📐 Requirements (요구사항)

### 기능 요구사항 (FR)

#### FR-DETAIL-001: 상품 상세 정보 표시

**WHEN** 상품이 선택되어 상세 화면을 열었을 때,
**THEN** 시스템은 ProductRepository에서 상품 정보를 로드하고 다음을 표시해야 한다:
- 상품 이미지 (고해상도)
- 상품명
- 가격 (원화 포맷)
- 상세 설명
- 평점 (선택)
- 리뷰 수 (선택)

#### FR-DETAIL-002: 장바구니 담기 버튼 기능

**WHEN** 사용자가 "장바구니 담기" 버튼을 클릭했을 때,
**THEN** 시스템은 현재 선택된 수량과 함께 상품을 CartRepository에 추가해야 한다.

#### FR-DETAIL-003: 수량 선택 기능

**WHEN** 사용자가 수량을 조정할 때,
**THEN** 시스템은 다음을 처리해야 한다:
- 기본값: 1
- 최소값: 1, 최대값: 999
- +/- 버튼으로 증감
- 또는 텍스트 필드 직접 입력
- UI에 선택된 수량이 실시간으로 표시됨

#### FR-DETAIL-004: 장바구니 추가 완료 피드백

**WHEN** 장바구니에 상품이 추가되었을 때,
**THEN** 시스템은 사용자에게 다음과 같이 피드백해야 한다:
- 토스트 메시지: "장바구니에 추가되었습니다"
- 또는 Snackbar: "장바구니 보기" 버튼 포함
- 또는 컨펌 다이얼로그 (선택)

#### FR-DETAIL-005: 뒤로가기 네비게이션

**WHEN** 사용자가 뒤로가기를 요청했을 때,
**THEN** 시스템은 Home 화면으로 돌아가야 한다.

### 비기능 요구사항 (NFR)

#### NFR-DETAIL-001: 상품 정보 로드 성능

상품 상세 정보 로드 시간 < 1.5초

#### NFR-DETAIL-002: 테스트 커버리지

코드 커버리지 95%+ (단위 및 UI 테스트)

### 인터페이스 요구사항

#### ProductDetailContract

```kotlin
// UI 상태
data class ProductDetailUiState(
    val product: Product? = null,
    val selectedQuantity: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingToCart: Boolean = false,
    val addToCartSuccess: Boolean = false
)

// Intent
sealed interface ProductDetailIntent {
    data class LoadProduct(val productId: String) : ProductDetailIntent
    data class SetQuantity(val quantity: Int) : ProductDetailIntent
    object AddToCart : ProductDetailIntent
    object DismissError : ProductDetailIntent
    object DismissSuccess : ProductDetailIntent
}

// SideEffect
sealed interface ProductDetailSideEffect {
    object NavigateBack : ProductDetailSideEffect
    data class ShowToast(val message: String) : ProductDetailSideEffect
}
```

---

## 🎯 Acceptance Criteria

### AC-DETAIL-001: ProductDetailContract 정의

조건:
- ProductDetailUiState, Intent, SideEffect 명확히 정의됨
- 모든 UI 상태(Loading, Success, Error)가 표현 가능

### AC-DETAIL-002: ProductDetailViewModel 구현

조건:
- ProductRepository 주입 (상품 조회)
- CartRepository 주입 (장바구니 추가)
- Hilt @HiltViewModel 적용
- LoadProduct, SetQuantity, AddToCart Intent 처리
- Flow 기반 상태 관리

### AC-DETAIL-003: ProductDetailScreen Composable 구현

조건:
- ProductDetailScreen Composable이 UiState를 읽고 UI 렌더링
- 상품 정보, 가격, 설명 표시
- 수량 선택 컴포넌트
- 장바구니 담기 버튼
- 로딩/에러 상태 처리
- SideEffect 처리 (네비게이션, 토스트)

### AC-DETAIL-004: 네비게이션 파라미터 전달

조건:
- Home 화면에서 productId를 타입 안전하게 전달
- ProductDetailScreen이 파라미터를 올바르게 수신

---

## 🔗 Traceability

**의존 SPEC:**
- SPEC-ANDROID-INIT-001
- SPEC-ANDROID-MVI-002
- SPEC-ANDROID-FEATURE-HOME-001

**관련 SPEC:**
- SPEC-ANDROID-FEATURE-CART-001
- SPEC-ANDROID-INTEGRATION-003

---

**END OF SPEC**
