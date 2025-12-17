# SPEC-ANDROID-INTEGRATION-003: 앱 통합 및 전체 네비게이션 구성

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-INTEGRATION-003
version: 1.0.0
status: completed
priority: critical
domain: ANDROID-INTEGRATION
created_at: 2025-11-29
updated_at: 2025-12-17
owner: Team
completed_at: 2025-12-17
dependencies: [SPEC-ANDROID-INIT-001, SPEC-ANDROID-MVI-002, SPEC-ANDROID-FEATURE-HOME-001, SPEC-ANDROID-FEATURE-DETAIL-001, SPEC-ANDROID-FEATURE-CART-001]
related_specs: []
tags: [android, integration, navigation, hilt, compose, app-module]
```

---

## 📋 개요 (Overview)

본 SPEC은 모든 Feature 모듈을 `:app` 모듈에 통합하고, Type-safe Navigation Graph를 구성하며, Hilt 의존성 주입을 완성하는 것을 목표로 합니다. 사용자가 Home → Detail → Cart로 이동하는 전체 플로우를 검증합니다.

**주요 작업:**
- 모든 Feature 모듈 (:feature:home, :feature:detail, :feature:cart)을 :app에 포함
- Type-safe Navigation Route 정의 및 구현
- MainActivity 작성 및 NavigationHost 설정
- 전체 Hilt 의존성 그래프 생성 및 검증
- 에뮬레이터에서 전체 플로우 테스트

**범위:**
- :app 모듈 설정
- Navigation Graph 구성
- MainActivity 작성
- Hilt 의존성 그래프 검증
- 통합 테스트 및 수동 테스트

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
- Navigation Compose (Type-safe)
- Material3 디자인 시스템
- Hilt 2.54
- Coroutines 1.9.0

---

## 🔧 Assumptions (가정)

**기술 가정:**
1. 모든 Feature 모듈 (:feature:home, :feature:detail, :feature:cart)이 완성됨
2. 각 Feature 모듈이 자체 Navigation Route를 정의
3. Type-safe Navigation을 사용 (kotlinx.serialization 기반)
4. Hilt @HiltAndroidApp이 DaitsoApplication에 적용됨
5. 모든 Hilt 모듈이 올바르게 구성됨

**설계 가정:**
1. 네비게이션 시작점은 Home 화면
2. Home → Detail (상품 클릭) → Cart (선택 후)
3. 모든 화면에서 Bottom Navigation 또는 Top App Bar로 네비게이션 가능
4. 뒤로가기는 시스템 백 버튼 또는 앱 바 백 버튼

**제약 조건:**
1. XML Activity 사용 금지 (Compose만 사용)
2. Fragment 사용 금지
3. 순환 의존성 없음
4. 모든 DI는 Hilt를 통해 관리

---

## 📐 Requirements (요구사항)

### 기능 요구사항 (FR)

#### FR-INT-001: 모든 Feature 모듈을 :app에 포함

**WHEN** :app/build.gradle.kts를 설정할 때,
**THEN** 시스템은 다음과 같이 모든 Feature 모듈을 의존성으로 추가해야 한다:

```kotlin
dependencies {
    implementation(project(":feature:home"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:cart"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    // ...
}
```

#### FR-INT-002: Type-safe Navigation Graph 설정

**WHEN** Navigation Graph를 구성할 때,
**THEN** 시스템은 다음과 같이 타입 안전한 Route를 정의해야 한다:

```kotlin
// Navigation Routes
sealed class Route(val path: String) {
    object Home : Route("home")
    data class ProductDetail(val productId: String) : Route("detail/{productId}")
    object Cart : Route("cart")
    object Checkout : Route("checkout")
}
```

또는 Kotlin Serialization 기반:

```kotlin
@Serializable
object HomeRoute

@Serializable
data class ProductDetailRoute(val productId: String)

@Serializable
object CartRoute
```

#### FR-INT-003: 시작점을 Home 화면으로 설정

**WHEN** 앱이 시작될 때,
**THEN** Navigation Graph의 시작점이 Home 화면이어야 한다.

#### FR-INT-004: 화면 간 네비게이션 (Home → Detail → Cart)

**WHEN** 사용자가 다양한 화면을 이동할 때,
**THEN** 시스템은 다음 플로우를 지원해야 한다:

```
Home (상품 목록)
  → 상품 클릭
    → ProductDetail (상품 상세)
      → "장바구니 담기" 클릭
        → Cart (장바구니)
```

#### FR-INT-005: Deep linking 지원 (선택)

**WHEN** 외부 URI로 특정 화면을 열려고 할 때,
**THEN** 시스템은 Deep Link를 통해 접근 가능해야 한다.

예:
```
daitso://product/{productId}
daitso://cart
```

#### FR-INT-006: 전체 Hilt 의존성 그래프 생성 검증

**WHEN** `./gradlew :app:compileDebugKotlin`을 실행할 때,
**THEN** Hilt 의존성 그래프가 성공적으로 생성되어야 한다.

**검증:**
- 모든 @HiltViewModel이 의존성 주입 가능
- 모든 @Inject 필드가 제공됨
- 의존성 순환 참조 없음

### 비기능 요구사항 (NFR)

#### NFR-INT-001: 앱 시작 성능

앱 시작 시간 (cold start) < 3초

#### NFR-INT-002: Hilt 의존성 그래프 검증

모든 Hilt 모듈 충돌 해결, 컴파일 오류 없음

#### NFR-INT-003: 에뮬레이터 통합 테스트 통과

모든 통합 테스트 통과, 네비게이션 플로우 검증

### 인터페이스 요구사항

#### MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaitsoTheme {
                NavigationHost()
            }
        }
    }
}
```

#### NavigationHost

```kotlin
@Composable
fun NavigationHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToDetail = { productId ->
                    navController.navigate(ProductDetailRoute(productId))
                }
            )
        }
        composable<ProductDetailRoute> {
            ProductDetailScreen(
                onNavigateToCart = {
                    navController.navigate(CartRoute)
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<CartRoute> {
            CartScreen(
                onNavigateToCheckout = {
                    navController.navigate(CheckoutRoute)
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
```

---

## 🎯 Acceptance Criteria

### AC-INT-001: 모든 Feature 모듈 :app에 의존성 추가

**조건:**
- :feature:home, :feature:detail, :feature:cart가 :app/build.gradle.kts에 추가됨
- Gradle Sync 성공

**검증:**
```bash
./gradlew :app:dependencies | grep feature
```

### AC-INT-002: Type-safe Navigation Route 구현

**조건:**
- HomeRoute, ProductDetailRoute, CartRoute가 정의됨
- 모든 화면이 Route를 통해 네비게이션 가능
- 파라미터 전달이 타입 안전함

**검증:**
```bash
./gradlew :app:compileDebugKotlin
```

### AC-INT-003: 전체 플로우 테스트 (Home → Detail → Add to Cart → Cart)

**조건:**
- 에뮬레이터에서 앱 실행 성공
- 모든 네비게이션 플로우 동작 확인
- 각 화면이 올바르게 렌더링됨

**테스트 시나리오:**
```
1. Home 화면 열기
2. 상품 선택 → ProductDetail 화면 이동
3. "장바구니 담기" 클릭
4. Cart 화면 이동
5. 장바구니 아이템 확인
```

### AC-INT-004: Hilt 의존성 그래프 컴파일 성공

**조건:**
- Hilt 의존성 그래프 생성 성공
- @HiltAndroidApp 정상 작동
- 모든 ViewModel, Repository 주입 성공

**검증:**
```bash
./gradlew :app:kaptGenerateStubsDebugKotlin
```

---

## 🔗 Traceability

**의존 SPEC:**
- SPEC-ANDROID-INIT-001: Core 모듈 완료
- SPEC-ANDROID-MVI-002: MVI 패턴 정의 완료
- SPEC-ANDROID-FEATURE-HOME-001: Home 모듈 완료
- SPEC-ANDROID-FEATURE-DETAIL-001: Detail 모듈 완료
- SPEC-ANDROID-FEATURE-CART-001: Cart 모듈 완료

**영향 받는 컴포넌트:**
- `:app` 모듈
- `:feature:home`, `:feature:detail`, `:feature:cart` 모듈
- `:core:data`, `:core:designsystem` 모듈
- `AndroidManifest.xml`

---

## 📋 모듈 의존성 그래프

```
:app
├── :feature:home
│   ├── :core:model
│   ├── :core:common
│   ├── :core:designsystem
│   └── :core:data
├── :feature:detail
│   ├── :core:model
│   ├── :core:common
│   ├── :core:designsystem
│   └── :core:data
├── :feature:cart
│   ├── :core:model
│   ├── :core:common
│   ├── :core:designsystem
│   └── :core:data
└── :core:designsystem
```

---

**END OF SPEC**
