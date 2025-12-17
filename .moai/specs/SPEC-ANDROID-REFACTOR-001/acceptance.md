# SPEC-ANDROID-REFACTOR-001: Acceptance Criteria

## 인수 테스트 시나리오

이 문서는 SPEC-ANDROID-REFACTOR-001의 인수 조건 및 테스트 시나리오를 정의합니다.

---

## Scenario 1: MVI 용어 통일 검증

### AC-001: 모든 Feature에서 Event 용어 사용

**GIVEN** 모든 Feature 모듈(home, detail, cart)이 존재할 때,

**WHEN** MVI Contract 파일에서 사용자 인터렉션 정의를 검색하면,

**THEN** 다음 조건을 만족해야 한다:
- 모든 인터렉션 sealed interface가 `{Feature}Event` 형식으로 명명됨
- `Intent` 용어를 사용하는 Contract가 존재하지 않음
- 모든 Event가 `UiEvent` 인터페이스를 상속함

**검증 스크립트:**
```bash
# Intent 용어 사용 여부 확인 (결과: 0건이어야 함)
grep -r "sealed interface.*Intent" --include="*Contract.kt" feature/
# 예상 결과: (no output)

# Event 용어 사용 확인 (결과: 3건 - Home, Detail, Cart)
grep -r "sealed interface.*Event.*UiEvent" --include="*Contract.kt" feature/
# 예상 결과:
# feature/home/...HomeContract.kt:sealed interface HomeEvent : UiEvent
# feature/detail/...ProductDetailContract.kt:sealed interface ProductDetailEvent : UiEvent
# feature/cart/...CartContract.kt:sealed interface CartEvent : UiEvent
```

---

## Scenario 2: CartRepository 단일화 검증

### AC-002: 중복 Repository 제거 확인

**GIVEN** CartRepository 통합이 완료되었을 때,

**WHEN** 프로젝트에서 CartRepository 파일을 검색하면,

**THEN** `core/data` 모듈에만 단일 CartRepository가 존재해야 한다.

**검증 스크립트:**
```bash
# CartRepository 파일 개수 확인 (결과: 1개여야 함)
find . -name "CartRepository.kt" -type f | wc -l
# 예상 결과: 1

# CartRepository 위치 확인
find . -name "CartRepository.kt" -type f
# 예상 결과: ./core/data/src/main/kotlin/.../repository/CartRepository.kt
```

### AC-003: 통합 Repository 기능 완전성

**GIVEN** 통합된 CartRepository가 존재할 때,

**WHEN** CartRepository 인터페이스를 확인하면,

**THEN** 다음 메서드가 모두 존재해야 한다:
- `getCartItems(): Flow<List<CartItem>>`
- `addToCart(productId: String, quantity: Int): Boolean`
- `updateQuantity(productId: String, quantity: Int)`
- `removeItem(productId: String)`
- `clearCart()`

**검증 스크립트:**
```bash
# 메서드 존재 확인
grep -E "(getCartItems|addToCart|updateQuantity|removeItem|clearCart)" \
  core/data/src/main/kotlin/*/repository/CartRepository.kt
# 예상 결과: 5개 메서드 모두 출력
```

---

## Scenario 3: 디렉토리 구조 표준화 검증

### AC-004: Home 모듈 DI 존재 확인

**GIVEN** Home Feature 모듈이 존재할 때,

**WHEN** DI Module 파일을 확인하면,

**THEN** `HomeModule.kt` 파일이 `feature/home/src/main/kotlin/.../di/` 경로에 존재해야 한다.

**검증 스크립트:**
```bash
# HomeModule.kt 존재 확인
find feature/home -name "HomeModule.kt" -type f
# 예상 결과: feature/home/src/main/kotlin/.../di/HomeModule.kt
```

### AC-005: Navigation 파일 일관성 확인

**GIVEN** 모든 Feature 모듈이 존재할 때,

**WHEN** Navigation 파일을 검색하면,

**THEN** 각 Feature에 `{Feature}Navigation.kt` 파일이 존재해야 한다.

**검증 스크립트:**
```bash
# Navigation 파일 개수 확인 (결과: 3개 - Home, Detail, Cart)
find feature -name "*Navigation.kt" -type f | wc -l
# 예상 결과: 3

# Navigation 파일 목록
find feature -name "*Navigation.kt" -type f
# 예상 결과:
# feature/home/.../navigation/HomeNavigation.kt
# feature/detail/.../navigation/DetailNavigation.kt
# feature/cart/.../navigation/CartNavigation.kt
```

### AC-006: 표준 디렉토리 구조 확인

**GIVEN** 각 Feature 모듈이 존재할 때,

**WHEN** 디렉토리 구조를 확인하면,

**THEN** 모든 Feature가 최소한 다음 디렉토리를 포함해야 한다:
- `contract/`
- `di/`
- `navigation/`
- `ui/` 또는 `presentation/`
- `viewmodel/`

**검증 스크립트:**
```bash
# 각 Feature 디렉토리 구조 확인
for feature in home detail cart; do
  echo "=== feature/$feature ==="
  ls -d feature/$feature/src/main/kotlin/com/bup/ys/daitso/feature/$feature/*/
done
```

---

## Scenario 4: 테스트 및 빌드 검증

### AC-007: 전체 테스트 통과

**GIVEN** 모든 리팩토링이 완료되었을 때,

**WHEN** 전체 테스트를 실행하면,

**THEN** 모든 테스트가 통과해야 한다.

**검증 스크립트:**
```bash
./gradlew test
# 예상 결과: BUILD SUCCESSFUL
```

### AC-008: 빌드 성공

**GIVEN** 모든 리팩토링이 완료되었을 때,

**WHEN** 전체 빌드를 실행하면,

**THEN** 빌드가 성공해야 한다.

**검증 스크립트:**
```bash
./gradlew assembleDebug
# 예상 결과: BUILD SUCCESSFUL
```

### AC-009: 테스트 커버리지 90% 이상

**GIVEN** 모든 테스트가 통과했을 때,

**WHEN** 테스트 커버리지를 측정하면,

**THEN** 커버리지가 90% 이상이어야 한다.

---

## Scenario 5: 기능 정상 동작 검증

### AC-010: Cart 기능 정상 동작

**GIVEN** 통합된 CartRepository가 적용된 앱이 실행될 때,

**WHEN** 사용자가 다음 작업을 수행하면:
1. 상품 상세 화면에서 "장바구니 담기" 클릭
2. 장바구니 화면 이동
3. 수량 변경
4. 아이템 삭제
5. 장바구니 비우기

**THEN** 모든 작업이 정상 동작해야 한다.

### AC-011: Navigation 정상 동작

**GIVEN** Navigation 파일이 표준화된 앱이 실행될 때,

**WHEN** 사용자가 Home → Detail → Cart 플로우를 따라가면,

**THEN** 모든 화면 전환이 정상 동작해야 한다.

---

## Edge Cases

### EC-001: 빈 장바구니 처리

**GIVEN** 장바구니가 비어있을 때,

**WHEN** 장바구니 화면을 열면,

**THEN** 빈 상태 UI가 표시되어야 한다.

### EC-002: 네트워크 오류 처리

**GIVEN** 네트워크 연결이 없을 때,

**WHEN** 장바구니에 상품 추가를 시도하면,

**THEN** 적절한 에러 메시지가 표시되어야 한다.

### EC-003: 중복 상품 추가

**GIVEN** 이미 장바구니에 있는 상품일 때,

**WHEN** 같은 상품을 다시 추가하면,

**THEN** 수량이 증가하거나 적절한 처리가 되어야 한다.

---

## 검증 완료 체크리스트

- [ ] AC-001: MVI 용어 통일 완료
- [ ] AC-002: 중복 Repository 제거 완료
- [ ] AC-003: 통합 Repository 기능 완전성 확인
- [ ] AC-004: Home 모듈 DI 존재 확인
- [ ] AC-005: Navigation 파일 일관성 확인
- [ ] AC-006: 표준 디렉토리 구조 확인
- [ ] AC-007: 전체 테스트 통과
- [ ] AC-008: 빌드 성공
- [ ] AC-009: 테스트 커버리지 90% 이상
- [ ] AC-010: Cart 기능 정상 동작
- [ ] AC-011: Navigation 정상 동작

---

**END OF ACCEPTANCE CRITERIA**
