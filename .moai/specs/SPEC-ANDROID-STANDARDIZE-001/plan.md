# SPEC-ANDROID-STANDARDIZE-001: Implementation Plan

## 구현 계획서

### 개요

이 문서는 MVI 용어 및 Contract 패턴 표준화의 구현 계획을 상세히 기술합니다.

---

## Phase 1: Detail Feature 표준화 (예상 시간: 1시간)

### Task 1.1: ProductDetailIntent → ProductDetailEvent 변경

**파일:** `feature/detail/src/main/kotlin/.../contract/ProductDetailContract.kt`

**변경 내용:**
```kotlin
// BEFORE
sealed interface ProductDetailIntent : UiEvent {
    data class LoadProduct(val productId: String) : ProductDetailIntent
    data class AddToCart(val quantity: Int) : ProductDetailIntent
    object NavigateBack : ProductDetailIntent
    object NavigateToCart : ProductDetailIntent
}

// AFTER
sealed interface ProductDetailEvent : UiEvent {
    data class LoadProduct(val productId: String) : ProductDetailEvent
    data class AddToCart(val quantity: Int) : ProductDetailEvent
    object NavigateBack : ProductDetailEvent
    object NavigateToCart : ProductDetailEvent
}
```

### Task 1.2: ViewModel 참조 업데이트

**파일:** `feature/detail/src/main/kotlin/.../viewmodel/ProductDetailViewModel.kt`

**변경 내용:**
- 제네릭 타입 파라미터 변경: `ProductDetailIntent` → `ProductDetailEvent`
- 이벤트 처리 함수 참조 업데이트

### Task 1.3: UI 참조 업데이트

**파일:** `feature/detail/src/main/kotlin/.../ui/ProductDetailScreen.kt`

**변경 내용:**
- 이벤트 전달 코드 업데이트

### Task 1.4: 테스트 파일 업데이트

**파일:** `feature/detail/src/test/.../contract/ProductDetailContractTest.kt`

**변경 내용:**
- 테스트 코드에서 `ProductDetailIntent` → `ProductDetailEvent` 변경

---

## Phase 2: Cart Feature 표준화 (예상 시간: 1시간)

### Task 2.1: CartIntent → CartEvent 변경

**파일:** `feature/cart/src/main/kotlin/.../contract/CartContract.kt`

**변경 내용:**
```kotlin
// BEFORE
sealed interface CartIntent : UiEvent {
    object LoadCartItems : CartIntent
    data class UpdateQuantity(val productId: String, val quantity: Int) : CartIntent
    data class RemoveItem(val productId: String) : CartIntent
    object ClearCart : CartIntent
    object NavigateToCheckout : CartIntent
}

// AFTER
sealed interface CartEvent : UiEvent {
    object LoadCartItems : CartEvent
    data class UpdateQuantity(val productId: String, val quantity: Int) : CartEvent
    data class RemoveItem(val productId: String) : CartEvent
    object ClearCart : CartEvent
    object NavigateToCheckout : CartEvent
}
```

### Task 2.2: ViewModel 참조 업데이트

**파일:** `feature/cart/src/main/kotlin/.../presentation/CartViewModel.kt`

**변경 내용:**
- 제네릭 타입 파라미터 변경: `CartIntent` → `CartEvent`
- 이벤트 처리 함수 참조 업데이트

### Task 2.3: UI 참조 업데이트

**파일:** `feature/cart/src/main/kotlin/.../presentation/CartScreen.kt`

**변경 내용:**
- 이벤트 전달 코드 업데이트

### Task 2.4: 테스트 파일 업데이트

**파일:** `feature/cart/src/test/.../contract/CartContractTest.kt`

**변경 내용:**
- 테스트 코드에서 `CartIntent` → `CartEvent` 변경

---

## Phase 3: 검증 및 테스트 (예상 시간: 30분)

### Task 3.1: 빌드 검증

```bash
./gradlew assembleDebug
```

### Task 3.2: 테스트 실행

```bash
./gradlew test
```

### Task 3.3: Intent 용어 제거 확인

```bash
grep -r "Intent" --include="*Contract.kt" feature/
# 예상 결과: (no output)
```

---

## IDE 리팩토링 가이드

### Android Studio Rename 기능 사용

1. `ProductDetailIntent` 클래스에서 `Shift+F6` (Rename)
2. `ProductDetailEvent`로 변경
3. "Search in comments and strings" 체크
4. "Refactor" 클릭

### 자동 변경 범위
- 클래스명
- 파일명 (필요시)
- 모든 참조 코드
- import 문

---

## 체크리스트

### Phase 1 완료 조건
- [ ] ProductDetailIntent → ProductDetailEvent 변경
- [ ] ProductDetailViewModel 참조 업데이트
- [ ] ProductDetailScreen 참조 업데이트
- [ ] 테스트 파일 업데이트
- [ ] 빌드 성공

### Phase 2 완료 조건
- [ ] CartIntent → CartEvent 변경
- [ ] CartViewModel 참조 업데이트
- [ ] CartScreen 참조 업데이트
- [ ] 테스트 파일 업데이트
- [ ] 빌드 성공

### Phase 3 완료 조건
- [ ] 전체 빌드 성공
- [ ] 모든 테스트 통과
- [ ] Intent 용어 완전 제거 확인

---

## 예상 총 작업 시간: 2.5시간

| Phase | 예상 시간 |
|-------|----------|
| Phase 1: Detail Feature | 1시간 |
| Phase 2: Cart Feature | 1시간 |
| Phase 3: 검증 및 테스트 | 30분 |

---

**END OF PLAN**
