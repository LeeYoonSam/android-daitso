# SPEC-ANDROID-FEATURE-HOME-001: Home 기능 모듈 - 수용 기준 및 테스트

## 📋 개요

Home Feature 모듈의 수용 기준, 테스트 시나리오, 검증 방법을 정의합니다. 14개 이상의 Given-When-Then 시나리오로 모든 기능을 검증합니다.

---

## 🧪 테스트 시나리오 (Test Scenarios)

### 시나리오 1: 초기 화면 로드 및 로딩 상태 표시

```gherkin
GIVEN Home 화면이 처음 열렸을 때
WHEN HomeViewModel이 초기화되고 loadProducts() Intent가 발행될 때
THEN 화면에 로딩 인디케이터(CircularProgressIndicator)가 표시됨
AND HomeUiState.isLoading이 true로 설정됨
```

**검증 방법:**
- Compose Testing: `composeTestRule.onNodeWithTag("loading_indicator").assertExists()`
- ViewModel Test: `assertTrue(viewModel.uiState.value.isLoading)`

**테스트 코드:**
```kotlin
@Test
fun testLoadProductsInitiallyShowsLoading() {
    val viewModel = HomeViewModel(mockRepository, mockDispatcher)

    assertEquals(true, viewModel.uiState.value.isLoading)
    assertEquals(emptyList<Product>(), viewModel.uiState.value.products)
}
```

---

### 시나리오 2: 상품 목록 로드 성공

```gherkin
GIVEN ProductRepository가 성공적으로 상품 목록을 반환했을 때
WHEN Flow가 Result.Success를 방출했을 때
THEN HomeUiState.products에 상품 목록이 저장됨
AND HomeUiState.isLoading이 false로 설정됨
AND HomeUiState.error가 null로 초기화됨
AND ProductCard 컴포넌트들이 그리드 형태로 렌더링됨
```

**검증 방법:**
- ViewModel Test: UiState가 올바르게 업데이트됨
- Compose Test: LazyVerticalGrid 내 ProductCard의 개수 확인

**테스트 코드:**
```kotlin
@Test
fun testLoadProductsSuccess() = runTest {
    val products = listOf(
        Product(id = "1", name = "Product 1", price = 10000.0, ...),
        Product(id = "2", name = "Product 2", price = 20000.0, ...)
    )

    every { mockRepository.getProducts() } returns flowOf(Result.Success(products))

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)

    // 비동기 처리 대기
    advanceUntilIdle()

    assertEquals(products, viewModel.uiState.value.products)
    assertEquals(false, viewModel.uiState.value.isLoading)
    assertEquals(null, viewModel.uiState.value.error)
}
```

---

### 시나리오 3: 상품 목록 로드 실패 (네트워크 오류)

```gherkin
GIVEN ProductRepository가 네트워크 오류를 반환했을 때
WHEN Flow가 Result.Error를 방출했을 때
THEN HomeUiState.error가 에러 메시지로 설정됨
AND HomeUiState.isLoading이 false로 설정됨
AND 화면에 에러 메시지와 Retry 버튼이 표시됨
AND 기존 products 리스트는 유지됨 (Offline-first)
```

**검증 방법:**
- ViewModel Test: error 필드가 설정됨
- Compose Test: ErrorView가 렌더링되고 Retry 버튼이 표시됨

**테스트 코드:**
```kotlin
@Test
fun testLoadProductsFailure() = runTest {
    val errorMessage = "Network error"

    every { mockRepository.getProducts() } returns flowOf(
        Result.Error(Exception(errorMessage))
    )

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)

    advanceUntilIdle()

    assertEquals(true, viewModel.uiState.value.error?.contains("error") ?: false)
    assertEquals(false, viewModel.uiState.value.isLoading)
}
```

---

### 시나리오 4: 새로고침 (Pull-to-Refresh) 기능

```gherkin
GIVEN 상품 목록이 이미 화면에 표시되어 있을 때
WHEN 사용자가 화면을 위에서 아래로 스와이프했을 때
THEN HomeUiState.isRefreshing이 true로 설정됨
AND 새로운 상품 목록을 로드하기 위해 ProductRepository.getProducts() 재호출됨
AND 새로운 데이터가 HomeUiState.products에 업데이트됨
AND 새로고침 인디케이터가 사라짐
```

**검증 방법:**
- ViewModel Test: RefreshProducts Intent 처리 후 상태 변경 확인
- Compose Test: PullRefreshIndicator 표시 및 해제 확인

**테스트 코드:**
```kotlin
@Test
fun testRefreshProductsUpdatesState() = runTest {
    val initialProducts = listOf(Product(id = "1", ...))
    val updatedProducts = listOf(Product(id = "1", ...), Product(id = "2", ...))

    var callCount = 0
    every { mockRepository.getProducts() } answers {
        callCount++
        if (callCount == 1) flowOf(Result.Success(initialProducts))
        else flowOf(Result.Success(updatedProducts))
    }

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)
    advanceUntilIdle()

    // RefreshProducts Intent 발행
    viewModel.handleIntent(HomeIntent.RefreshProducts)
    advanceUntilIdle()

    assertEquals(2, callCount)  // 두 번 호출됨
    assertEquals(updatedProducts, viewModel.uiState.value.products)
}
```

---

### 시나리오 5: 상품 클릭 및 네비게이션 Intent 발행

```gherkin
GIVEN 상품 목록이 화면에 표시되어 있을 때
WHEN 사용자가 특정 상품 카드를 클릭했을 때
THEN HomeIntent.OnProductClick(productId)가 발행됨
AND HomeSideEffect.NavigateToProductDetail(productId)이 발행됨
AND 상품 상세화면으로 네비게이션됨
```

**검증 방법:**
- ViewModel Test: SideEffect가 올바른 productId와 함께 발행됨
- Compose Test: 클릭 시 네비게이션 콜백이 호출됨

**테스트 코드:**
```kotlin
@Test
fun testProductClickNavigatesSideEffect() = runTest {
    val products = listOf(Product(id = "123", ...))
    every { mockRepository.getProducts() } returns flowOf(Result.Success(products))

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)
    advanceUntilIdle()

    val sideEffects = mutableListOf<HomeSideEffect>()
    backgroundScope.launch {
        viewModel.sideEffect.collect { sideEffects.add(it) }
    }

    viewModel.handleIntent(HomeIntent.OnProductClick("123"))
    advanceUntilIdle()

    assertTrue(sideEffects.any { it is HomeSideEffect.NavigateToProductDetail })
    val navigationEffect = sideEffects.first() as HomeSideEffect.NavigateToProductDetail
    assertEquals("123", navigationEffect.productId)
}
```

---

### 시나리오 6: 상품 목록이 비어있을 때 Empty 상태 표시

```gherkin
GIVEN ProductRepository가 빈 상품 목록을 반환했을 때
WHEN Flow가 Result.Success(emptyList())를 방출했을 때
THEN HomeUiState.products가 빈 리스트로 설정됨
AND 화면에 "상품이 없습니다" 메시지가 표시됨
AND LazyVerticalGrid에 아무것도 렌더링되지 않음
```

**검증 방법:**
- ViewModel Test: products가 emptyList()로 설정됨
- Compose Test: EmptyView가 렌더링되고 상품 카드가 없음

**테스트 코드:**
```kotlin
@Test
fun testLoadProductsEmptyList() = runTest {
    every { mockRepository.getProducts() } returns flowOf(Result.Success(emptyList()))

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)
    advanceUntilIdle()

    assertEquals(emptyList<Product>(), viewModel.uiState.value.products)
    assertEquals(null, viewModel.uiState.value.error)
}
```

---

### 시나리오 7: 오프라인 상태에서 캐시된 데이터 표시

```gherkin
GIVEN 네트워크 연결이 없을 때
WHEN Home 화면을 열었을 때
THEN Repository의 Offline-first 패턴에 따라 로컬 캐시된 데이터가 먼저 표시됨
AND 네트워크 재연결 후 업데이트된 데이터를 표시함
```

**검증 방법:**
- Integration Test: Room DB에서 캐시된 데이터 확인
- ViewModel Test: offline 상황에서도 products가 보존됨

**테스트 코드:**
```kotlin
@Test
fun testOfflineFirstDisplaysCachedData() = runTest {
    val cachedProducts = listOf(Product(id = "cached", ...))

    every { mockRepository.getProducts() } returns flow {
        // Offline-first: 로컬 캐시 먼저 방출
        emit(Result.Success(cachedProducts))
        // 네트워크 실패
        emit(Result.Error(Exception("No network")))
    }

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)
    advanceUntilIdle()

    // 캐시된 데이터가 유지됨
    assertEquals(cachedProducts, viewModel.uiState.value.products)
}
```

---

### 시나리오 8: Retry 버튼으로 재시도

```gherkin
GIVEN 상품 로드 실패로 에러 상태일 때
WHEN 사용자가 Retry 버튼을 클릭했을 때
THEN LoadProducts Intent가 다시 발행됨
AND 상품 로드가 재시도됨
AND 성공 시 상품 목록이 표시됨
```

**검증 방법:**
- ViewModel Test: Retry 버튼 클릭 후 loadProducts() 재호출
- Compose Test: Retry 버튼이 존재하고 클릭 가능

**테스트 코드:**
```kotlin
@Test
fun testRetryButtonReloadsProducts() = runTest {
    var callCount = 0
    every { mockRepository.getProducts() } answers {
        callCount++
        if (callCount == 1) flowOf(Result.Error(Exception("First fail")))
        else flowOf(Result.Success(listOf(Product(id = "1", ...))))
    }

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.error != null)

    viewModel.handleIntent(HomeIntent.LoadProducts)
    advanceUntilIdle()

    assertEquals(2, callCount)
    assertNull(viewModel.uiState.value.error)
}
```

---

### 시나리오 9: 에러 메시지 닫기 (Dismiss)

```gherkin
GIVEN 에러 상태가 표시되어 있을 때
WHEN 사용자가 에러 메시지의 닫기(X) 버튼을 클릭했을 때
THEN HomeIntent.OnErrorDismiss가 발행됨
AND HomeUiState.error가 null로 초기화됨
AND 기존 상품 목록이 다시 표시됨
```

**검증 방법:**
- ViewModel Test: OnErrorDismiss Intent 후 error가 null로 설정됨

**테스트 코드:**
```kotlin
@Test
fun testDismissErrorClearsState() = runTest {
    every { mockRepository.getProducts() } returns flowOf(
        Result.Error(Exception("Test error"))
    )

    val viewModel = HomeViewModel(mockRepository, mockDispatcher)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.error != null)

    viewModel.handleIntent(HomeIntent.OnErrorDismiss)

    assertNull(viewModel.uiState.value.error)
}
```

---

### 시나리오 10: ProductCard 컴포넌트 렌더링

```gherkin
GIVEN 상품이 로드되었을 때
WHEN ProductCard Composable이 렌더링될 때
THEN 상품 이미지가 표시됨
AND 상품명(2줄 이상은 말줄임)이 표시됨
AND 가격이 원화 포맷(₩)으로 표시됨
AND 카드가 클릭 가능한 상태임
```

**검증 방법:**
- Compose Preview: 모든 상태에서 올바르게 렌더링됨
- Compose Test: 이미지, 텍스트, 가격이 올바르게 표시됨

**테스트 코드:**
```kotlin
@Test
fun testProductCardDisplaysAllFields() {
    val product = Product(
        id = "1",
        name = "Test Product",
        price = 15000.0,
        imageUrl = "https://example.com/image.jpg",
        description = "Test Description"
    )

    composeTestRule.setContent {
        DaitsoTheme {
            ProductCard(product, onProductClick = {})
        }
    }

    composeTestRule.onNodeWithText("Test Product").assertExists()
    composeTestRule.onNodeWithText("₩15,000").assertExists()
    composeTestRule.onNodeWithTag("product_image").assertExists()
}
```

---

### 시나리오 11: 그리드 레이아웃 반응형 표시

```gherkin
GIVEN 다양한 화면 크기의 기기에서 Home 화면이 열렸을 때
WHEN 화면이 렌더링될 때
THEN Phone(가로 너비 < 600dp): 2열 그리드로 표시됨
AND Tablet(가로 너비 >= 600dp): 3열 이상 그리드로 표시됨
AND 모든 기기에서 아이템이 균등하게 배분됨
```

**검증 방법:**
- Compose Test: 다양한 크기의 기기에서 테스트
- Manual Test: 가로 회전 시 레이아웃이 올바르게 조정됨

---

### 시나리오 12: 스크롤 성능 (60fps 유지)

```gherkin
GIVEN 많은 수의 상품(50+)이 로드되었을 때
WHEN 사용자가 목록을 스크롤할 때
THEN 스크롤이 매끄러움 (60fps 유지)
AND 메모리 누수가 없음
AND Coil 이미지 캐싱이 정상 작동함
```

**검증 방법:**
- Android Profiler: CPU, Memory, Frame Rate 모니터링
- Compose Layout Inspector: 리컴포지션 횟수 확인

---

### 시나리오 13: 네비게이션 파라미터 전달

```gherkin
GIVEN 상품이 클릭되었을 때
WHEN 상품 상세화면으로 네비게이션될 때
THEN 정확한 상품 ID가 라우팅 파라미터로 전달됨
AND 상품 상세화면에서 올바른 상품 정보가 표시됨
AND 뒤로가기 시 Home 화면으로 돌아옴
```

**검증 방법:**
- Integration Test: Navigation 완전 흐름 테스트
- Manual Test: 앱에서 클릭 후 상세화면 확인

---

### 시나리오 14: HomeViewModel Hilt 주입

```gherkin
GIVEN :app 모듈에서 HomeScreen을 사용할 때
WHEN HomeViewModel을 @HiltViewModel으로 주입받을 때
THEN ProductRepository가 자동으로 주입됨
AND Dispatcher 어노테이션을 통한 CoroutineDispatcher 주입됨
AND Hilt 의존성 그래프에 충돌이 없음
```

**검증 방법:**
- Compile Test: `./gradlew :feature:home:compileDebugKotlin` 성공
- ViewModel Test: @HiltViewModel이 정상 작동

---

## 📊 품질 게이트 (Quality Gates)

### Code Coverage (코드 커버리지)

| 모듈 | 목표 | 검증 방법 |
|------|------|---------|
| HomeViewModel | 95%+ | JaCoCo 커버리지 리포트 |
| HomeScreen | 85%+ | Compose UI Test |
| ProductCard | 90%+ | Unit Test |
| 전체 | 90%+ | `./gradlew :feature:home:jacocoTestReport` |

### Build & Compile (빌드 및 컴파일)

```bash
# 성공 기준
./gradlew :feature:home:build           # 성공
./gradlew :feature:home:test            # 모든 테스트 통과
./gradlew :feature:home:androidTest     # UI 테스트 통과
```

### Lint & Code Style (린트 및 코드 스타일)

```bash
# 코딩 스타일 검증
./gradlew :feature:home:lint            # 경고 없음
```

---

## 🎯 정의된 완료 기준 (Definition of Done)

### 개발 완료 조건

- ✅ 14개 이상의 Given-When-Then 테스트 시나리오 구현
- ✅ 모든 테스트 통과 (`./gradlew :feature:home:test`)
- ✅ 코드 커버리지 90% 이상
- ✅ UI 테스트 모든 상태 검증
- ✅ 네비게이션 파라미터 전달 검증
- ✅ 성능 프로파일링 (60fps 유지 확인)
- ✅ Lint 오류 없음

### 검증 완료 조건

- ✅ Code Review 승인
- ✅ 에뮬레이터에서 전체 기능 동작 확인
- ✅ 실제 기기에서 테스트 (가능시)
- ✅ Hilt 의존성 그래프 컴파일 성공
- ✅ Git 커밋 메시지 Conventional Commits 준수

---

## 📝 테스트 실행 가이드

### Unit Test 실행

```bash
# 특정 테스트 클래스 실행
./gradlew :feature:home:testDebugUnitTest --tests "*.HomeViewModelTest"

# 모든 유닛 테스트 실행
./gradlew :feature:home:testDebugUnitTest

# 커버리지 리포트 생성
./gradlew :feature:home:jacocoTestReport
```

### UI Test 실행

```bash
# 에뮬레이터 또는 실제 기기 필요
./gradlew :feature:home:connectedAndroidTest
```

### 수동 테스트 (Manual Testing)

1. **앱 실행**
   ```bash
   ./gradlew :app:installDebug
   ```

2. **각 시나리오 검증**
   - [ ] 초기 로드 시 로딩 상태 표시
   - [ ] 상품 목록 그리드 표시
   - [ ] 상품 이미지, 이름, 가격 정확함
   - [ ] Pull-to-Refresh 동작
   - [ ] 상품 클릭 시 상세화면 이동
   - [ ] 네트워크 오류 시 에러 메시지 표시
   - [ ] Retry 버튼 동작
   - [ ] 오프라인 상태에서 캐시된 데이터 표시

---

## 🔗 관련 테스트 리소스

- Mockito: Repository 모킹
- Turbine: Flow 테스트
- Compose Testing: UI 테스트
- Robolectric: 로컬 Android 테스트
- JaCoCo: 커버리지 리포팅

---

**END OF ACCEPTANCE**
