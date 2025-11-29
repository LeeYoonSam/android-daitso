# SPEC-ANDROID-FEATURE-CART-001: 장바구니 화면 - 구현 계획

## 📊 구현 단계

### Phase 1: :feature:cart 모듈 생성

**목표:** Feature 모듈 설정

**작업:**
1. `:feature:cart` 디렉토리 생성
2. `build.gradle.kts` 작성 (Convention Plugin 적용)
3. 패키지 구조:
   ```
   feature/cart/
   ├── src/main/kotlin/com/bup/ys/daitso/feature/cart/
   │   ├── contract/CartContract.kt
   │   ├── presentation/
   │   │   ├── CartScreen.kt
   │   │   ├── CartViewModel.kt
   │   │   └── components/
   │   │       ├── CartItemRow.kt
   │   │       ├── CartSummary.kt
   │   │       └── QuantityControl.kt
   │   └── navigation/CartNavigation.kt
   ├── src/test/kotlin/...
   └── src/androidTest/kotlin/...
   ```

**예상 소요 시간:** 30분

---

### Phase 2: CartContract 정의

**목표:** MVI 계약 정의

**작업:**
1. CartUiState 정의
2. CartIntent 정의
3. CartSideEffect 정의

**예상 소요 시간:** 1시간

---

### Phase 3: CartViewModel 구현 (Repository, Room 통합)

**목표:** 비즈니스 로직 구현

**작업:**
1. CartRepository 의존성 주입
2. LoadCartItems Intent 처리 (Room에서 조회)
3. UpdateQuantity Intent 처리
4. RemoveItem Intent 처리
5. 총 가격 자동 계산
6. Flow 기반 상태 관리

**예상 소요 시간:** 2-3시간

---

### Phase 4: CartScreen UI 구현

**목표:** UI 렌더링

**작업:**
1. CartScreen Composable
2. CartItemRow 컴포넌트
3. QuantityControl 컴포넌트
4. CartSummary 컴포넌트
5. 빈 상태 UI
6. 로딩/에러 상태 UI
7. 결제하기 버튼

**예상 소요 시간:** 2-3시간

---

### Phase 5: 통합 테스트 및 데이터 지속성 검증

**목표:** 14+ 테스트 작성

**작업:**
1. CartViewModelTest (8+ 테스트)
2. CartScreenTest (6+ 테스트)
3. Room DB 지속성 테스트

**예상 소요 시간:** 2시간

---

## ⏱️ 타임라인

| Phase | 작업 | 소요 시간 |
|-------|------|---------|
| 1 | 모듈 설정 | 30분 |
| 2 | Contract 정의 | 1시간 |
| 3 | ViewModel 구현 | 2-3시간 |
| 4 | UI 구현 | 2-3시간 |
| 5 | 통합 테스트 | 2시간 |
| **총계** | | **10-12시간** |

---

## 🛠️ 기술 접근 방식

**Room Database 통합:**
- CartRepository가 CartDao를 통해 데이터 접근
- Flow<List<CartItem>>로 비동기 데이터 스트리밍
- 트랜잭션으로 데이터 일관성 보장

**MVI 패턴:**
- LoadCartItems Intent로 아이템 로드
- UpdateQuantity Intent로 수량 변경
- RemoveItem Intent로 삭제

**상태 관리:**
- StateFlow로 UiState 관리
- 파생 상태 (derived state)로 totalPrice 계산

---

## 📋 의존성

**선행 작업:**
- SPEC-ANDROID-INIT-001: Core 모듈 완료 (특히 :core:database)
- SPEC-ANDROID-MVI-002: MVI 패턴 정의 완료

**병렬 작업:**
- SPEC-ANDROID-FEATURE-HOME-001
- SPEC-ANDROID-FEATURE-DETAIL-001

---

## ✅ 정의된 완료 조건

- ✅ :feature:cart 모듈 생성
- ✅ CartContract 정의
- ✅ CartViewModel 구현 (Room 통합)
- ✅ CartScreen UI 렌더링
- ✅ 14+ 테스트 작성 및 통과
- ✅ 코드 커버리지 95%+
- ✅ 데이터 지속성 검증 완료

---

**END OF PLAN**
