# SPEC-ANDROID-FEATURE-HOME-001: Home 기능 모듈 구현 - 구현 계획

## 🎯 프로젝트 개요

Home Feature 모듈 구현의 전체 로드맵 및 마일스톤을 문서화합니다.

---

## 📊 구현 단계 (Implementation Phases)

### Phase 1: :feature:home 모듈 설정 및 기본 구조

**목표:** Feature 모듈 생성, Gradle 설정, Convention Plugin 적용

**작업:**
1. `:feature:home` 모듈 디렉토리 생성
2. `build.gradle.kts` 작성 (Convention Plugin 적용)
   - `alias(libs.plugins.daitso.android.library)`
   - `alias(libs.plugins.daitso.android.hilt)`
   - `alias(libs.plugins.daitso.android.library.compose)`
3. 기본 패키지 구조 생성
   ```
   feature/home/
   ├── src/main/kotlin/com/bup/ys/daitso/feature/home/
   │   ├── contract/
   │   │   └── HomeContract.kt
   │   ├── presentation/
   │   │   ├── HomeScreen.kt
   │   │   ├── HomeViewModel.kt
   │   │   └── components/
   │   │       └── ProductCard.kt
   │   └── navigation/
   │       └── HomeNavigation.kt
   ├── src/test/kotlin/com/bup/ys/daitso/feature/home/
   │   └── HomeViewModelTest.kt
   └── src/androidTest/kotlin/com/bup/ys/daitso/feature/home/
       └── HomeScreenTest.kt
   ```
4. `AndroidManifest.xml` 생성 (최소 설정)

**예상 소요 시간:** 30분

**성공 기준:**
- ✅ `:feature:home` 모듈 Gradle Sync 성공
- ✅ Convention Plugin이 올바르게 적용됨

---

### Phase 2: HomeContract 정의

**목표:** MVI 패턴의 UiState, Intent, SideEffect 정의

**작업:**
1. `HomeContract.kt` 작성
   ```kotlin
   // UiState
   data class HomeUiState(
       val products: List<Product> = emptyList(),
       val isLoading: Boolean = false,
       val error: String? = null,
       val isRefreshing: Boolean = false
   )

   // Intent
   sealed interface HomeIntent {
       object LoadProducts : HomeIntent
       object RefreshProducts : HomeIntent
       data class OnProductClick(val productId: String) : HomeIntent
       object OnErrorDismiss : HomeIntent
   }

   // SideEffect
   sealed interface HomeSideEffect {
       data class NavigateToProductDetail(val productId: String) : HomeSideEffect
       data class ShowToast(val message: String) : HomeSideEffect
   }
   ```
2. ProductCard 컴포넌트 스켈레톤 작성
3. 네비게이션 Route 정의 검토

**예상 소요 시간:** 1시간

**성공 기준:**
- ✅ HomeContract 파일 작성 완료
- ✅ 모든 Intent, UiState, SideEffect 명확히 정의됨

---

### Phase 3: HomeViewModel 구현

**목표:** MVI ViewModel 구현 (ProductRepository 통합)

**작업:**
1. `HomeViewModel.kt` 작성
   ```kotlin
   @HiltViewModel
   class HomeViewModel @Inject constructor(
       private val productRepository: ProductRepository,
       @Dispatcher(DaitsoDispatchers.Main) private val mainDispatcher: CoroutineDispatcher
   ) : ViewModel() {
       // UiState StateFlow
       private val _uiState = MutableStateFlow(HomeUiState())
       val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

       // SideEffect SharedFlow
       private val _sideEffect = MutableSharedFlow<HomeSideEffect>()
       val sideEffect: SharedFlow<HomeSideEffect> = _sideEffect.asSharedFlow()

       init {
           loadProducts()
       }

       fun handleIntent(intent: HomeIntent) {
           when (intent) {
               is HomeIntent.LoadProducts -> loadProducts()
               is HomeIntent.RefreshProducts -> refreshProducts()
               is HomeIntent.OnProductClick -> handleProductClick(intent.productId)
               is HomeIntent.OnErrorDismiss -> dismissError()
           }
       }

       private fun loadProducts() {
           // 상품 로드 로직
       }

       private fun refreshProducts() {
           // 새로고침 로직
       }

       private fun handleProductClick(productId: String) {
           // 네비게이션 SideEffect 발행
       }

       private fun dismissError() {
           // 에러 상태 초기화
       }
   }
   ```
2. ProductRepository.getProducts() Flow 처리
3. 상태 전환 로직 구현
4. 에러 처리 로직 구현
5. 로깅 추가

**예상 소요 시간:** 2-3시간

**성공 기준:**
- ✅ HomeViewModel이 Hilt로 주입 가능
- ✅ ProductRepository 의존성 주입 성공
- ✅ UiState가 Intent에 따라 올바르게 변경됨

---

### Phase 4: HomeScreen Composable 구현

**목표:** Home 화면 UI 렌더링

**작업:**
1. `HomeScreen.kt` 작성 - 레이아웃 구조
   ```kotlin
   @Composable
   fun HomeScreen(
       viewModel: HomeViewModel = hiltViewModel(),
       onNavigateToDetail: (String) -> Unit
   ) {
       val uiState by viewModel.uiState.collectAsState()
       val sideEffect by viewModel.sideEffect.collectAsState(null)

       // SideEffect 처리
       LaunchedEffect(sideEffect) {
           when (sideEffect) {
               is HomeSideEffect.NavigateToProductDetail -> {
                   onNavigateToDetail(sideEffect.productId)
               }
               // ...
           }
       }

       // UI 렌더링
       when {
           uiState.isLoading -> LoadingView()
           uiState.error != null -> ErrorView(uiState.error)
           uiState.products.isEmpty() -> EmptyView()
           else -> ProductGridView(uiState.products) { productId ->
               viewModel.handleIntent(HomeIntent.OnProductClick(productId))
           }
       }
   }
   ```

2. `ProductCard.kt` - 상품 카드 컴포넌트
   - 이미지 (Coil을 사용한 네트워크 이미지 로드)
   - 상품명, 가격, 평점 표시
   - 클릭 리스너

3. 상태별 UI 컴포넌트
   - `LoadingView()` - CircularProgressIndicator
   - `ErrorView()` - 에러 메시지 + Retry 버튼
   - `EmptyView()` - 상품 없음 메시지
   - `ProductGridView()` - LazyVerticalGrid

4. Pull-to-Refresh 구현
   - PullRefreshIndicator 또는 SwipeRefreshState 사용

5. Material3 테마 적용
   - DaitsoTheme 래핑

**예상 소요 시간:** 2-3시간

**성공 기준:**
- ✅ HomeScreen 렌더링 성공
- ✅ ProductCard가 올바르게 표시됨
- ✅ 모든 상태(Loading, Success, Error)에 대한 UI 표시

---

### Phase 5: 테스트 작성 및 검증

**목표:** 14+ 테스트 작성, 커버리지 95%+ 달성

**작업:**

#### 5.1 HomeViewModelTest (8+ 테스트)
```kotlin
class HomeViewModelTest {
    @Test
    fun testLoadProductsInitially() { /* ... */ }

    @Test
    fun testLoadProductsSuccess() { /* ... */ }

    @Test
    fun testLoadProductsError() { /* ... */ }

    @Test
    fun testRefreshProducts() { /* ... */ }

    @Test
    fun testOnProductClick() { /* ... */ }

    @Test
    fun testOnErrorDismiss() { /* ... */ }

    @Test
    fun testHandleIntentLoadProducts() { /* ... */ }

    @Test
    fun testHandleIntentRefreshProducts() { /* ... */ }
}
```

#### 5.2 HomeScreenTest (6+ 테스트)
```kotlin
class HomeScreenTest {
    @Test
    fun testDisplayLoadingState() { /* ... */ }

    @Test
    fun testDisplayProductList() { /* ... */ }

    @Test
    fun testDisplayErrorState() { /* ... */ }

    @Test
    fun testDisplayEmptyState() { /* ... */ }

    @Test
    fun testProductClickNavigation() { /* ... */ }

    @Test
    fun testRefreshProductsUI() { /* ... */ }
}
```

#### 5.3 통합 테스트
- 네트워크 Mocking (Mock Server 또는 Fake Repository)
- 상태 전환 검증
- 네비게이션 검증

**예상 소요 시간:** 2시간

**성공 기준:**
- ✅ 모든 테스트 통과
- ✅ 코드 커버리지 95%+
- ✅ `./gradlew :feature:home:test` 성공

---

## ⏱️ 타임라인 및 마일스톤

| Phase | 작업 | 소요 시간 | 마일스톤 |
|-------|------|---------|---------|
| 1 | :feature:home 모듈 설정 | 30분 | 모듈 생성 완료 |
| 2 | HomeContract 정의 | 1시간 | 계약 정의 완료 |
| 3 | HomeViewModel 구현 | 2-3시간 | 비즈니스 로직 완료 |
| 4 | HomeScreen UI 구현 | 2-3시간 | UI 렌더링 완료 |
| 5 | 테스트 작성 | 2시간 | 테스트 완료 |
| **총 계** | | **10-12 hours** | **Ready for Integration** |

---

## 🛠️ 기술 접근 방식 (Technical Approach)

### MVI 아키텍처 패턴

**장점:**
- ✅ 단방향 데이터 흐름 (Intent → ViewModel → UiState → UI)
- ✅ 테스트 용이성
- ✅ 예측 가능한 상태 관리

**구현:**
```
User Action → Intent → ViewModel.handleIntent()
  → UiState 업데이트 → UI 리컴포지션
  → SideEffect 발행 → 네비게이션/토스트 등
```

### 상태 관리

- **UiState**: `StateFlow<HomeUiState>`로 UI 상태 관리
- **Intent**: 사용자 액션을 Intent로 표현, `handleIntent()`로 처리
- **SideEffect**: `SharedFlow<HomeSideEffect>`로 일회성 이벤트 처리

### 비동기 처리

- **ProductRepository.getProducts()**: `Flow<Result<List<Product>>>` 반환
- **Repository**: Offline-first 패턴 적용 (로컬 캐시 먼저, 네트워크 동기화)
- **ViewModel**: `viewModelScope.launch { }` 내에서 Flow 수집

### Compose UI 최적화

- **remember**: 상태 유지
- **LazyVerticalGrid**: 효율적인 리스트 렌더링
- **Coil**: 이미지 로딩 및 캐싱
- **derivedStateOf**: 복잡한 상태 계산 최적화

---

## 📋 기술 스택 및 의존성

### 프로젝트 레벨 의존성

```kotlin
dependencies {
    // Core modules
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.navigation)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Image Loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## ⚠️ 위험 요소 및 대응 방안 (Risks & Mitigation)

### 1. 네트워크 Timeout 및 연결 실패

**위험:** 느린 네트워크에서 상품 로드가 길어지거나 실패할 수 있음

**대응:**
- ✅ Retrofit Timeout 설정 (30초)
- ✅ Repository에서 Offline-first 패턴 적용 (로컬 캐시 먼저 표시)
- ✅ 에러 상태에서 Retry 버튼 제공
- ✅ 네트워크 연결 감지 및 안내 배너 표시

### 2. 상태 관리 복잡성

**위험:** 다양한 상태(Loading, Success, Error, Refreshing)의 조합이 복잡함

**대응:**
- ✅ HomeUiState를 명확히 정의하여 상태 조합 제한
- ✅ Intent 기반 핸들링으로 예측 가능한 상태 전환
- ✅ UI 테스트로 모든 상태 조합 검증

### 3. UI 성능 저하

**위험:** 많은 수의 상품 카드 렌더링 시 프레임 드롭 발생 가능

**대응:**
- ✅ LazyVerticalGrid 사용 (가시 범위만 렌더링)
- ✅ ProductCard Composable 최적화 (remember, memoization)
- ✅ Coil 이미지 캐싱 및 크기 조정
- ✅ 성능 프로파일링 (Compose Layout Inspector)

### 4. 네비게이션 파라미터 전달

**위험:** 상품 ID를 안전하게 전달하지 못할 수 있음

**대응:**
- ✅ Navigation Compose 타입 안전 Route 사용
- ✅ SideEffect로 네비게이션 처리
- ✅ 네비게이션 테스트 추가

---

## 📞 의존성 및 선행 작업

### 선행 필수 작업
1. ✅ SPEC-ANDROID-INIT-001: Core 모듈 구성 완료
2. ✅ SPEC-ANDROID-MVI-002: MVI 패턴 정의 및 기본 구조 완료
3. ✅ `:core:data` 모듈의 ProductRepository 구현 완료

### 병렬 작업
- SPEC-ANDROID-FEATURE-DETAIL-001: 상품 상세 화면 (독립적으로 진행 가능)
- SPEC-ANDROID-FEATURE-CART-001: 장바구니 화면 (독립적으로 진행 가능)

### 후행 작업
- SPEC-ANDROID-INTEGRATION-003: 전체 통합 (모든 Feature 완료 후)

---

## ✅ 정의된 완료 조건 (Definition of Done)

1. ✅ `:feature:home` 모듈이 성공적으로 생성됨
2. ✅ HomeContract (UiState, Intent, SideEffect)가 명확히 정의됨
3. ✅ HomeViewModel이 MVI 패턴에 따라 구현됨
4. ✅ HomeScreen Composable이 모든 상태를 올바르게 렌더링함
5. ✅ ProductCard 컴포넌트가 이미지, 가격, 이름 표시
6. ✅ Pull-to-Refresh 기능이 동작함
7. ✅ 상품 클릭 시 상세화면으로 네비게이션됨
8. ✅ 14+ 단위 테스트 작성 및 모두 통과
9. ✅ 코드 커버리지 95%+ 달성
10. ✅ `./gradlew :feature:home:build` 성공
11. ✅ Gradle Sync 오류 없음
12. ✅ Code Review 및 Approval 완료

---

**END OF PLAN**
