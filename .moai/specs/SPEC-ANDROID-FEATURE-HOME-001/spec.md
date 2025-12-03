# SPEC-ANDROID-FEATURE-HOME-001: Home 기능 모듈 구현 (상품 목록)

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-FEATURE-HOME-001
version: 1.1.0
status: completed
priority: high
domain: ANDROID-FEATURE
created_at: 2025-11-29
updated_at: 2025-12-03
owner: Team
completed_at: 2025-12-03
implementation_branch: feature/SPEC-ANDROID-FEATURE-HOME-001
dependencies: [SPEC-ANDROID-INIT-001, SPEC-ANDROID-MVI-002]
related_specs: [SPEC-ANDROID-FEATURE-DETAIL-001, SPEC-ANDROID-FEATURE-CART-001, SPEC-ANDROID-INTEGRATION-003]
tags: [android, mvi, feature, home, compose, network, state-management]
```

---

## 📋 개요 (Overview)

Home 화면은 전체 앱의 진입점이자 상품 목록을 표시하는 핵심 Feature 모듈입니다. 본 SPEC은 MVI 아키텍처를 적용하여 상품 목록 로드, 상태 관리, UI 렌더링, 그리고 상호작용(Pull-to-Refresh, 상품 클릭)을 구현하는 것을 목표로 합니다.

**주요 기능:**
- 상품 목록 로드 및 그리드 표시
- Loading/Success/Error 상태 UI 자동 전환
- Pull-to-Refresh 새로고침
- 상품 클릭 시 상세화면 네비게이션
- 오프라인 상태 처리 (캐시된 데이터 표시)

**모듈 위치:** `:feature:home`

---

## 🌍 Environment (환경)

**개발 환경:**
- Android Studio: 2024.1.2 이상 (Hedgehog)
- Kotlin: 2.1.0
- Android Gradle Plugin: 8.7.3
- Gradle: 8.11.1

**타겟 환경:**
- minSdk: 26 (Android 8.0 Oreo)
- targetSdk: 35 (Android 15)
- compileSdk: 35

**기술 스택:**
- Jetpack Compose 1.7.5 (Compose BOM 2024.12.01)
- Material3 디자인 시스템
- MVI 아키텍처 패턴
- Coroutines 1.9.0
- Hilt 2.54

---

## 🔧 Assumptions (가정)

**기술 가정:**
1. MVI 패턴이 프로젝트 전역에서 표준 아키텍처로 사용됨
2. `:core:data` 모듈의 ProductRepository가 구현되어 있음
3. `:core:model` 모듈에 Product 데이터 클래스가 정의됨
4. Hilt DI가 프로젝트 전역에 적용되어 있음
5. Navigation Compose를 통한 타입 안전 네비게이션 사용
6. Coroutines Flow를 기반으로 한 상태 관리

**설계 가정:**
1. 상품 목록은 그리드 형태로 2열 또는 3열로 표시 (스크린 사이즈 기반)
2. 각 상품 카드에는 이미지, 이름, 가격이 표시됨
3. 로딩 중에는 Shimmer 로딩 효과 또는 CircularProgressIndicator 표시
4. 에러 상태에서는 Retry 버튼과 함께 에러 메시지 표시

**제약 조건:**
1. XML 레이아웃 사용 금지 (Jetpack Compose만 사용)
2. `:feature:home` 모듈은 다른 Feature 모듈에 직접 의존하지 않음
3. 라우팅 로직은 `:app` 모듈 또는 공통 Navigation 모듈에서 관리

---

## 📐 Requirements (요구사항)

### 기능 요구사항 (FR)

#### FR-HOME-001: 상품 목록 로드 및 그리드 표시

**WHEN** 사용자가 Home 화면을 열었을 때,
**THEN** 시스템은 ProductRepository에서 상품 목록을 로드하고 그리드 형태로 UI에 표시해야 한다.

**세부 사항:**
- 초기 로드 시 ProductRepository.getProducts() 호출
- Flow를 통해 상태 변경 감지
- 그리드는 LazyVerticalGrid 사용
- 스크린 가로 너비에 따라 컬럼 수 결정 (Phone: 2열, Tablet: 3열 이상)
- 각 상품 카드에는 다음 정보 표시:
  - 상품 이미지 (Coil을 사용한 네트워크 이미지 로드)
  - 상품명 (2줄 이상은 말줄임)
  - 가격 (원화 포맷 "₩" 기호 포함)
  - 평점 (옵션)

#### FR-HOME-002: 로딩/성공/에러 상태 UI 전환

**WHEN** 상품 목록 로드 상태가 변경될 때,
**THEN** 시스템은 현재 상태에 맞는 UI를 렌더링해야 한다.

**상태별 UI:**
- **Loading**: CircularProgressIndicator 또는 Shimmer 애니메이션
- **Success**: 상품 목록 그리드 표시
- **Error**: 에러 메시지 + Retry 버튼
- **Empty**: 상품이 없을 경우 "상품이 없습니다" 메시지

#### FR-HOME-003: Pull-to-Refresh 기능

**WHEN** 사용자가 화면을 위에서 아래로 스와이프했을 때,
**THEN** 시스템은 상품 목록을 다시 로드하고 UI를 업데이트해야 한다.

**세부 사항:**
- PullRefreshIndicator (Compose Material) 또는 SwipeRefreshLayout 패턴 사용
- 새로고침 중 Loading 상태 표시
- 새로고침 완료 후 자동으로 인디케이터 숨김

#### FR-HOME-004: 상품 클릭 시 상세화면으로 네비게이션

**WHEN** 사용자가 상품 카드를 클릭했을 때,
**THEN** 시스템은 상품 상세화면(ProductDetail)으로 네비게이션하고 상품 ID를 전달해야 한다.

**세부 사항:**
- 클릭 시 onProductClick Intent 발행
- ViewModel에서 네비게이션 SideEffect 처리
- 타입 안전한 네비게이션 Route 사용

#### FR-HOME-005: 오프라인 상태 처리 (캐시된 데이터 표시)

**WHEN** 네트워크 연결이 없는 상태에서 사용자가 Home 화면을 열었을 때,
**THEN** 시스템은 캐시된 데이터(Room DB)를 표시하거나 오프라인 안내 메시지를 표시해야 한다.

**세부 사항:**
- Offline-first 패턴 활용 (Repository에서 로컬 데이터 먼저 방출)
- 오프라인 상태 배너 표시 (선택)

### 비기능 요구사항 (NFR)

#### NFR-HOME-001: 상품 목록 로드 성능

초기 로드 시간이 2초 이내여야 한다.
- 1차: 로컬 캐시 데이터 로드 (< 500ms)
- 2차: 네트워크 데이터 동기화 (< 2초)

#### NFR-HOME-002: UI 반응성 및 프레임률

- 리스트 스크롤 시 60fps 유지
- 상태 전환 애니메이션 부드러운 실행
- 상품 카드 렌더링 최적화 (remember, derivedStateOf 사용)

#### NFR-HOME-003: 테스트 커버리지

- 단위 테스트 (Unit Test): 95%+ 커버리지
- UI 테스트 (Compose Preview + Screenshot Test): 모든 상태 검증

### 인터페이스 요구사항 (Interface Requirements)

#### HomeContract: UiState, Intent, SideEffect

```kotlin
// UI 상태
data class HomeUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

// 사용자 인터랙션
sealed interface HomeIntent {
    object LoadProducts : HomeIntent
    object RefreshProducts : HomeIntent
    data class OnProductClick(val productId: String) : HomeIntent
    object OnErrorDismiss : HomeIntent
}

// 부수 효과 (네비게이션, 토스트 등)
sealed interface HomeSideEffect {
    data class NavigateToProductDetail(val productId: String) : HomeSideEffect
    data class ShowToast(val message: String) : HomeSideEffect
}
```

### 설계 제약사항 (Design Constraints)

1. **Material3 디자인 시스템 준수**: Daitso Theme 사용
2. **Jetpack Compose 2.x**: XML 레이아웃 금지
3. **MVI 아키텍처**: 상태는 ViewModel에서 관리, 뷰는 상태를 읽기만 함
4. **타입 안전성**: Navigation Route에 강타입 파라미터 사용

---

## 🎯 Acceptance Criteria (수용 기준)

### AC-HOME-001: HomeContract 정의

**조건:**
- HomeUiState, HomeIntent, HomeSideEffect가 명확히 정의됨
- 모든 UI 상태(Loading, Success, Error, Empty)가 UiState로 표현 가능
- Intent가 사용자 액션을 완벽히 대표

**검증:**
```
GIVEN HomeContract이 정의되었을 때
WHEN UiState, Intent, SideEffect를 살펴볼 때
THEN 모든 화면 상태와 사용자 액션이 명확히 표현됨
```

### AC-HOME-002: HomeViewModel 구현 (Repository 통합)

**조건:**
- HomeViewModel이 HomeContract를 구현
- ProductRepository 주입 (Hilt)
- LoadProducts, RefreshProducts Intent 처리
- Flow 기반 상태 관리
- SideEffect 발행

**검증:**
```
GIVEN ProductRepository가 mock되었을 때
WHEN ViewModel.handleIntent(LoadProducts) 호출 시
THEN UiState가 Loading → Success로 전환됨
```

### AC-HOME-003: HomeScreen Composable 구현

**조건:**
- HomeScreen Composable이 UiState를 읽고 UI 렌더링
- LazyVerticalGrid로 상품 목록 표시
- 각 상품 카드 구현 (ProductCard Composable)
- Loading/Error/Empty 상태 별 UI 표시
- Pull-to-Refresh 구현
- 네비게이션 처리 (SideEffect 수신)

**검증:**
```
GIVEN HomeScreen Composable이 렌더링되었을 때
WHEN 상태가 Success일 때
THEN 상품이 2~3열 그리드로 표시됨
```

### AC-HOME-004: 14+ 단위 테스트 및 UI 테스트

**조건:**
- HomeViewModelTest: 8+ 테스트
- HomeScreenTest (Compose): 6+ 테스트

**검증:**
```
GIVEN 테스트가 작성되었을 때
WHEN ./gradlew :feature:home:test 실행 시
THEN 모든 테스트 통과 (커버리지 95%+)
```

---

## 🔗 Traceability (추적성)

**의존 SPEC:**
- SPEC-ANDROID-INIT-001: Core 모듈 및 프로젝트 초기 설정
- SPEC-ANDROID-MVI-002: MVI 패턴 및 Feature 모듈 구조

**관련 SPEC:**
- SPEC-ANDROID-FEATURE-DETAIL-001: 상품 상세 화면 (네비게이션 대상)
- SPEC-ANDROID-INTEGRATION-003: 전체 통합 및 네비게이션 구성

**영향 받는 컴포넌트:**
- `:feature:home` 모듈
- `:core:data` 모듈 (ProductRepository 사용)
- `:core:designsystem` 모듈 (테마 및 컴포넌트 사용)
- `:app` 모듈 (네비게이션 통합)

---

**END OF SPEC**
