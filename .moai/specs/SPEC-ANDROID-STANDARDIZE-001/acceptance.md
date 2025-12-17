# SPEC-ANDROID-STANDARDIZE-001: Acceptance Criteria

## 인수 테스트 시나리오

---

## Scenario 1: MVI 용어 통일 검증

### AC-001: Intent 용어 완전 제거

**GIVEN** 모든 Feature 모듈이 존재할 때,

**WHEN** Contract 파일에서 "Intent"를 검색하면,

**THEN** 검색 결과가 0건이어야 한다.

**검증 스크립트:**
```bash
grep -r "Intent" --include="*Contract.kt" feature/
# 예상 결과: (no output - 0건)
```

### AC-002: Event 용어 일관성 확인

**GIVEN** 모든 Feature 모듈이 존재할 때,

**WHEN** Contract 파일에서 Event 정의를 검색하면,

**THEN** 다음 3개의 Event가 존재해야 한다:
- `HomeEvent`
- `ProductDetailEvent`
- `CartEvent`

**검증 스크립트:**
```bash
grep -r "sealed interface.*Event.*UiEvent" --include="*Contract.kt" feature/
# 예상 결과:
# ...HomeContract.kt:sealed interface HomeEvent : UiEvent
# ...ProductDetailContract.kt:sealed interface ProductDetailEvent : UiEvent
# ...CartContract.kt:sealed interface CartEvent : UiEvent
```

---

## Scenario 2: ViewModel 참조 검증

### AC-003: Detail ViewModel Event 참조

**GIVEN** ProductDetailViewModel이 존재할 때,

**WHEN** 제네릭 타입 파라미터를 확인하면,

**THEN** `ProductDetailEvent`를 사용해야 한다.

**검증 스크립트:**
```bash
grep -A5 "class ProductDetailViewModel" feature/detail/src/main/kotlin/**/ProductDetailViewModel.kt
# 예상 결과: BaseViewModel<..., ProductDetailEvent, ...>
```

### AC-004: Cart ViewModel Event 참조

**GIVEN** CartViewModel이 존재할 때,

**WHEN** 제네릭 타입 파라미터를 확인하면,

**THEN** `CartEvent`를 사용해야 한다.

**검증 스크립트:**
```bash
grep -A5 "class CartViewModel" feature/cart/src/main/kotlin/**/CartViewModel.kt
# 예상 결과: BaseViewModel<..., CartEvent, ...>
```

---

## Scenario 3: 빌드 및 테스트 검증

### AC-005: 전체 빌드 성공

**GIVEN** 모든 변경이 완료되었을 때,

**WHEN** 전체 빌드를 실행하면,

**THEN** 빌드가 성공해야 한다.

**검증 스크립트:**
```bash
./gradlew assembleDebug
# 예상 결과: BUILD SUCCESSFUL
```

### AC-006: 전체 테스트 통과

**GIVEN** 모든 변경이 완료되었을 때,

**WHEN** 전체 테스트를 실행하면,

**THEN** 모든 테스트가 통과해야 한다.

**검증 스크립트:**
```bash
./gradlew test
# 예상 결과: BUILD SUCCESSFUL
```

---

## Scenario 4: 기능 동작 검증

### AC-007: Detail 화면 정상 동작

**GIVEN** 앱이 실행될 때,

**WHEN** 상품 상세 화면에서 다음 작업을 수행하면:
1. 상품 정보 로드
2. 수량 선택
3. 장바구니 담기

**THEN** 모든 작업이 정상 동작해야 한다.

### AC-008: Cart 화면 정상 동작

**GIVEN** 앱이 실행될 때,

**WHEN** 장바구니 화면에서 다음 작업을 수행하면:
1. 장바구니 아이템 로드
2. 수량 변경
3. 아이템 삭제

**THEN** 모든 작업이 정상 동작해야 한다.

---

## 검증 완료 체크리스트

- [ ] AC-001: Intent 용어 완전 제거
- [ ] AC-002: Event 용어 일관성 확인
- [ ] AC-003: Detail ViewModel Event 참조
- [ ] AC-004: Cart ViewModel Event 참조
- [ ] AC-005: 전체 빌드 성공
- [ ] AC-006: 전체 테스트 통과
- [ ] AC-007: Detail 화면 정상 동작
- [ ] AC-008: Cart 화면 정상 동작

---

**END OF ACCEPTANCE CRITERIA**
