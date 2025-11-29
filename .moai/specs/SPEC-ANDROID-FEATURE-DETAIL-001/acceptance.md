# SPEC-ANDROID-FEATURE-DETAIL-001: 상품 상세 화면 - 수용 기준

## 📋 테스트 시나리오

### 시나리오 1: 상품 상세 정보 로드 및 표시

```gherkin
GIVEN 상품 ID가 전달되었을 때
WHEN ProductDetailScreen이 로드될 때
THEN 상품 이미지, 이름, 가격, 설명이 표시됨
AND 수량 선택기가 기본값 1로 표시됨
AND 장바구니 담기 버튼이 활성화됨
```

**테스트 코드:**
```kotlin
@Test
fun testLoadProductDisplaysAllInfo() = runTest {
    val product = Product(id = "1", name = "Product", price = 10000.0, ...)
    every { mockRepository.getProduct("1") } returns flowOf(Result.Success(product))

    val viewModel = ProductDetailViewModel(mockRepository, mockCartRepository, mockDispatcher)
    viewModel.handleIntent(ProductDetailIntent.LoadProduct("1"))

    advanceUntilIdle()

    assertEquals(product, viewModel.uiState.value.product)
    assertEquals(1, viewModel.uiState.value.selectedQuantity)
}
```

---

### 시나리오 2: 수량 선택 기능

```gherkin
GIVEN 상품이 로드되었을 때
WHEN 사용자가 수량을 3으로 변경했을 때
THEN UiState.selectedQuantity가 3으로 업데이트됨
AND UI에 새 수량이 표시됨
```

---

### 시나리오 3: 장바구니 담기

```gherkin
GIVEN 상품이 로드되고 수량이 2로 선택되었을 때
WHEN 장바구니 담기 버튼을 클릭했을 때
THEN CartRepository.addToCart()가 호출됨
AND 성공 메시지가 표시됨
AND isAddingToCart 상태가 false로 복귀함
```

---

### 시나리오 4: 에러 처리

```gherkin
GIVEN 상품 로드 실패 시
WHEN Flow가 Result.Error를 방출했을 때
THEN 에러 메시지가 표시됨
AND Retry 버튼이 활성화됨
```

---

### 시나리오 5: 뒤로가기

```gherkin
GIVEN 상세 화면이 열려있을 때
WHEN 뒤로가기 버튼을 클릭했을 때
THEN NavigateBack SideEffect가 발행됨
AND Home 화면으로 돌아감
```

---

## 🎯 수용 기준

- ✅ 상품 정보 완전히 로드 및 표시
- ✅ 수량 선택 기능 동작
- ✅ 장바구니 추가 성공
- ✅ 에러 상황 처리
- ✅ 네비게이션 동작
- ✅ 14+ 테스트 통과
- ✅ 커버리지 95%+

---

**END OF ACCEPTANCE**
