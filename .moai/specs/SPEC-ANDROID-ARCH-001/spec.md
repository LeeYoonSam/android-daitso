# SPEC-ANDROID-ARCH-001: Feature 모듈 디렉토리 구조 및 DI 표준화

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-ARCH-001
version: 1.0.0
status: draft
priority: medium
domain: ANDROID-ARCH
created_at: 2025-12-17
updated_at: 2025-12-17
owner: Team
dependencies: [SPEC-ANDROID-MVI-002, SPEC-ANDROID-INTEGRATION-003]
related_specs: [SPEC-ANDROID-REFACTOR-001, SPEC-ANDROID-STANDARDIZE-001]
tags: [android, architecture, directory-structure, di, hilt, navigation]
```

---

## HISTORY

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 1.0.0 | 2025-12-17 | 초기 SPEC 작성 | R2-D2 |

---

## 📋 개요 (Overview)

본 SPEC은 Android MVI 모듈화 프로젝트에서 Feature 모듈의 디렉토리 구조, DI 패턴, Navigation 패턴의 일관성을 확보하기 위한 표준화 작업을 정의합니다.

**문제점:**
1. 디렉토리 구조 불일치: Home(3개), Detail(5개), Cart(7개) 패키지
2. Home 모듈 DI 누락: Cart/Detail은 Module 있으나 Home은 없음
3. Navigation 패턴 불완전: Cart만 Navigation.kt 보유
4. UI 폴더 명칭 불일치: `ui/` vs `presentation/`

**목표:**
- 모든 Feature 모듈에 표준화된 디렉토리 구조 적용
- 일관된 DI Module 패턴 적용
- 통일된 Navigation 파일 패턴 적용

**범위:**
- 디렉토리 구조 표준화
- DI Module 추가 (Home)
- Navigation 파일 추가 (Home, Detail)

---

## 🌍 Environment (환경)

**개발 환경:**
- Android Studio: 2024.1.2 이상
- Kotlin: 2.1.0
- Hilt: 2.54
- Navigation Compose

**대상 모듈:**
- `:feature:home`
- `:feature:detail`
- `:feature:cart`

---

## 🔧 Assumptions (가정)

**기술 가정:**
1. 모든 Feature 모듈이 Hilt를 사용하여 의존성 주입
2. Navigation Compose를 사용한 화면 전환
3. 각 Feature가 독립적으로 테스트 가능

**설계 가정:**
1. 표준 디렉토리 구조는 모든 Feature에 동일하게 적용
2. 빈 DI Module도 일관성을 위해 생성
3. Navigation 파일은 NavGraphBuilder 확장 함수 제공

**제약 조건:**
1. 기존 코드 동작 유지
2. 기존 파일 경로 변경 최소화

---

## 📐 Requirements (요구사항)

### 기능 요구사항 (FR)

#### FR-ARCH-001: 표준 디렉토리 구조 정의

**WHEN** 새로운 Feature 모듈을 생성하거나 기존 모듈을 리팩토링할 때,
**THEN** 시스템은 다음 표준 구조를 따라야 한다:

```
feature/{feature-name}/
└── src/main/kotlin/com/bup/ys/daitso/feature/{feature-name}/
    ├── contract/              # MVI Contract (State, Event, SideEffect)
    │   ├── {Feature}State.kt
    │   ├── {Feature}Event.kt
    │   └── {Feature}SideEffect.kt
    ├── di/                    # Hilt DI Module
    │   └── {Feature}Module.kt
    ├── navigation/            # Navigation 설정
    │   └── {Feature}Navigation.kt
    ├── ui/                    # UI 컴포넌트
    │   ├── {Feature}Screen.kt
    │   └── components/        # (선택적) 하위 컴포넌트
    └── viewmodel/             # ViewModel
        └── {Feature}ViewModel.kt
```

#### FR-ARCH-002: Home 모듈 DI 추가

**WHEN** Home Feature 모듈이 Hilt를 통해 의존성 주입을 받을 때,
**THEN** 시스템은 `HomeModule.kt`를 제공해야 한다:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    // Home 특화 의존성 (필요시 추가)
}
```

#### FR-ARCH-003: Home Navigation 추가

**WHEN** Home Feature 모듈에서 화면 네비게이션을 정의할 때,
**THEN** 시스템은 `HomeNavigation.kt`를 통해 NavGraphBuilder 확장 함수를 제공해야 한다:

```kotlin
fun NavGraphBuilder.homeScreen(
    onNavigateToDetail: (productId: String) -> Unit
) {
    composable<HomeRoute> {
        HomeScreen(onNavigateToDetail = onNavigateToDetail)
    }
}
```

#### FR-ARCH-004: Detail Navigation 추가

**WHEN** Detail Feature 모듈에서 화면 네비게이션을 정의할 때,
**THEN** 시스템은 `DetailNavigation.kt`를 통해 NavGraphBuilder 확장 함수를 제공해야 한다:

```kotlin
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

### 비기능 요구사항 (NFR)

#### NFR-ARCH-001: 디렉토리 일관성

모든 Feature 모듈이 동일한 디렉토리 구조를 가져야 함

#### NFR-ARCH-002: DI 일관성

모든 Feature 모듈이 DI Module을 보유해야 함

#### NFR-ARCH-003: Navigation 일관성

모든 Feature 모듈이 Navigation 파일을 보유해야 함

---

## 🎯 Acceptance Criteria

### AC-ARCH-001: 모든 Feature에 DI Module 존재

**GIVEN** 모든 Feature 모듈이 존재할 때,
**WHEN** DI Module 파일을 검색하면,
**THEN** 각 Feature에 `{Feature}Module.kt`가 존재해야 한다.

**검증:**
```bash
find feature -name "*Module.kt" -path "*/di/*" | wc -l
# 결과: 3 (Home, Detail, Cart)
```

### AC-ARCH-002: 모든 Feature에 Navigation 파일 존재

**GIVEN** 모든 Feature 모듈이 존재할 때,
**WHEN** Navigation 파일을 검색하면,
**THEN** 각 Feature에 `{Feature}Navigation.kt`가 존재해야 한다.

**검증:**
```bash
find feature -name "*Navigation.kt" | wc -l
# 결과: 3 (Home, Detail, Cart)
```

### AC-ARCH-003: 표준 디렉토리 구조 준수

**GIVEN** 각 Feature 모듈이 존재할 때,
**WHEN** 디렉토리 구조를 확인하면,
**THEN** 모든 Feature가 `contract/`, `di/`, `navigation/`, `ui/`, `viewmodel/` 디렉토리를 포함해야 한다.

**검증:**
```bash
for feature in home detail cart; do
  echo "=== $feature ==="
  ls -d feature/$feature/src/main/kotlin/com/bup/ys/daitso/feature/$feature/*/
done
```

### AC-ARCH-004: 빌드 성공

**GIVEN** 모든 변경이 완료되었을 때,
**WHEN** 빌드를 실행하면,
**THEN** 빌드가 성공해야 한다.

**검증:**
```bash
./gradlew assembleDebug
# 결과: BUILD SUCCESSFUL
```

---

## 🔗 Traceability

**의존 SPEC:**
- SPEC-ANDROID-MVI-002: MVI 아키텍처 기본 구조
- SPEC-ANDROID-INTEGRATION-003: 앱 통합 및 전체 네비게이션

**관련 SPEC:**
- SPEC-ANDROID-REFACTOR-001: 전체 리팩토링 (상위 집합)
- SPEC-ANDROID-STANDARDIZE-001: MVI 용어 표준화

**영향 받는 모듈:**
- `:feature:home` - DI, Navigation 추가
- `:feature:detail` - Navigation 추가
- `:feature:cart` - 기존 유지 (참조 표준)

---

## 📋 현재 상태 vs 목표 상태

### Home Feature

| 항목 | 현재 상태 | 목표 상태 |
|------|----------|----------|
| contract/ | ✅ 존재 | ✅ 유지 |
| di/ | ❌ 없음 | ✅ 추가 |
| navigation/ | ❌ 없음 | ✅ 추가 |
| ui/ | ✅ 존재 | ✅ 유지 |
| viewmodel/ | ✅ 존재 | ✅ 유지 |

### Detail Feature

| 항목 | 현재 상태 | 목표 상태 |
|------|----------|----------|
| contract/ | ✅ 존재 | ✅ 유지 |
| di/ | ✅ 존재 | ✅ 유지 |
| navigation/ | ❌ 없음 | ✅ 추가 |
| ui/ | ✅ 존재 | ✅ 유지 |
| viewmodel/ | ✅ 존재 | ✅ 유지 |

### Cart Feature (표준 참조)

| 항목 | 현재 상태 | 목표 상태 |
|------|----------|----------|
| contract/ | ✅ 존재 | ✅ 유지 |
| di/ | ✅ 존재 | ✅ 유지 |
| navigation/ | ✅ 존재 | ✅ 유지 |
| presentation/ | ✅ 존재 | ✅ 유지 (ui/로 통일 권장) |

---

**END OF SPEC**
