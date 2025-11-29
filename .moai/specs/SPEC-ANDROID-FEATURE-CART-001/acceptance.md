# SPEC-ANDROID-FEATURE-CART-001: 장바구니 화면 - 수용 기준

## 📋 테스트 시나리오

### 시나리오 1: 장바구니 로드 및 아이템 표시

```gherkin
GIVEN 장바구니 화면이 열렸을 때
WHEN CartViewModel이 LoadCartItems Intent를 처리했을 때
THEN Room DB에서 모든 CartItemEntity를 조회함
AND 각 아이템이 리스트로 표시됨
AND 이미지, 이름, 가격, 수량이 올바르게 표시됨
```

---

### 시나리오 2: 총 가격 자동 계산

```gherkin
GIVEN 장바구니에 여러 아이템이 있을 때
WHEN UI가 렌더링될 때
THEN 총 가격 = Sum(가격 * 수량)으로 계산됨
AND 원화 포맷(₩)으로 표시됨
AND 아이템 수량 변경 시 총 가격 자동 업데이트됨
```

---

### 시나리오 3: 수량 변경

```gherkin
GIVEN 장바구니 아이템이 표시되어 있을 때
WHEN 사용자가 수량 +/- 버튼을 클릭했을 때
THEN UpdateQuantity Intent가 발행됨
AND CartDao.updateCartItem()이 호출됨
AND Room DB에서 업데이트됨
AND UI에 새 수량이 표시됨
AND 총 가격이 재계산됨
```

---

### 시나리오 4: 아이템 삭제

```gherkin
GIVEN 장바구니 아이템이 표시되어 있을 때
WHEN 삭제 버튼을 클릭했을 때
THEN RemoveItem Intent가 발행됨
AND CartDao.deleteCartItem()이 호출됨
AND Room DB에서 삭제됨
AND UI에서 아이템이 사라짐
AND 총 가격이 재계산됨
```

---

### 시나리오 5: 빈 장바구니 상태

```gherkin
GIVEN 장바구니가 비어있을 때
WHEN CartScreen이 렌더링될 때
THEN EmptyCartView가 표시됨
AND "장바구니가 비어있습니다" 메시지가 표시됨
AND Shopping 아이콘이 표시됨
AND Home으로 돌아가기 버튼이 표시됨
```

---

### 시나리오 6: 데이터 지속성 검증

```gherkin
GIVEN 장바구니에 아이템이 저장되어 있을 때
WHEN 앱을 종료하고 재시작했을 때
THEN Room DB에서 저장된 아이템을 다시 로드함
AND 이전 상태가 완벽히 유지됨
```

---

### 시나리오 7: 결제하기 버튼

```gherkin
GIVEN 장바구니에 아이템이 있을 때
WHEN 결제하기 버튼을 클릭했을 때
THEN NavigateToCheckout SideEffect가 발행됨
AND 결제 화면으로 네비게이션됨
```

---

### 시나리오 8: CartViewModel Hilt 주입

```gherkin
GIVEN :app 모듈에서 CartScreen을 사용할 때
WHEN CartViewModel을 @HiltViewModel으로 주입받을 때
THEN CartRepository가 자동으로 주입됨
AND Hilt 의존성 그래프 컴파일 성공
```

---

## 🎯 수용 기준

- ✅ 장바구니 아이템 완전히 로드 및 표시
- ✅ 총 가격 자동 계산 및 업데이트
- ✅ 수량 변경 기능 동작
- ✅ 아이템 삭제 기능 동작
- ✅ 빈 상태 UI 표시
- ✅ Room DB 데이터 지속성
- ✅ 결제하기 버튼 동작
- ✅ 14+ 테스트 통과
- ✅ 커버리지 95%+

---

**END OF ACCEPTANCE**
