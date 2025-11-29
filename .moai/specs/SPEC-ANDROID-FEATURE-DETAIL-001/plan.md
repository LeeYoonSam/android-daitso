# SPEC-ANDROID-FEATURE-DETAIL-001: 상품 상세 화면 - 구현 계획

## 📊 구현 단계

### Phase 1: :feature:detail 모듈 생성

**목표:** Feature 모듈 설정 및 기본 구조 생성

**작업:**
1. `:feature:detail` 디렉토리 생성
2. `build.gradle.kts` 작성 (Convention Plugin 적용)
3. 패키지 구조 생성:
   ```
   feature/detail/
   ├── src/main/kotlin/com/bup/ys/daitso/feature/detail/
   │   ├── contract/ProductDetailContract.kt
   │   ├── presentation/
   │   │   ├── ProductDetailScreen.kt
   │   │   ├── ProductDetailViewModel.kt
   │   │   └── components/
   │   │       ├── ProductImageSection.kt
   │   │       ├── ProductInfoSection.kt
   │   │       ├── QuantitySelector.kt
   │   │       └── AddToCartButton.kt
   │   └── navigation/ProductDetailNavigation.kt
   ├── src/test/kotlin/...
   └── src/androidTest/kotlin/...
   ```

**예상 소요 시간:** 30분

---

### Phase 2: ProductDetailContract 정의

**목표:** MVI 계약 정의

**작업:**
1. ProductDetailUiState 정의
2. ProductDetailIntent 정의
3. ProductDetailSideEffect 정의
4. 네비게이션 Route 정의

**예상 소요 시간:** 1시간

---

### Phase 3: ProductDetailViewModel 구현

**목표:** 비즈니스 로직 구현

**작업:**
1. ProductRepository 의존성 주입
2. CartRepository 의존성 주입
3. LoadProduct Intent 처리 (상품 조회)
4. SetQuantity Intent 처리
5. AddToCart Intent 처리 (장바구니 추가)
6. 에러 처리 및 로깅

**예상 소요 시간:** 2-3시간

---

### Phase 4: ProductDetailScreen UI 구현

**목표:** UI 렌더링

**작업:**
1. ProductDetailScreen Composable 작성
2. ProductImageSection 컴포넌트
3. ProductInfoSection 컴포넌트
4. QuantitySelector 컴포넌트
5. AddToCartButton 컴포넌트
6. Loading/Error 상태 UI
7. SideEffect 처리 (네비게이션, 토스트)

**예상 소요 시간:** 2-3시간

---

### Phase 5: 테스트 작성

**목표:** 14+ 테스트 작성

**작업:**
1. ProductDetailViewModelTest (8+ 테스트)
   - 상품 로드 성공/실패
   - 수량 설정
   - 장바구니 추가
   - 네비게이션

2. ProductDetailScreenTest (6+ 테스트)
   - 상품 정보 표시
   - 수량 선택 UI
   - 버튼 동작

**예상 소요 시간:** 2시간

---

## ⏱️ 타임라인

| Phase | 작업 | 소요 시간 |
|-------|------|---------|
| 1 | 모듈 설정 | 30분 |
| 2 | Contract 정의 | 1시간 |
| 3 | ViewModel 구현 | 2-3시간 |
| 4 | UI 구현 | 2-3시간 |
| 5 | 테스트 | 2시간 |
| **총계** | | **8-10시간** |

---

## 🛠️ 기술 접근 방식

**MVI 패턴:**
- LoadProduct Intent로 상품 로드
- SetQuantity Intent로 수량 관리
- AddToCart Intent로 장바구니 추가

**상태 관리:**
- StateFlow로 UiState 관리
- SharedFlow로 SideEffect 발행

**네비게이션:**
- 타입 안전한 Route 사용
- productId 파라미터 전달

---

## 📋 의존성

**선행 작업:**
- SPEC-ANDROID-INIT-001: Core 모듈 완료
- SPEC-ANDROID-MVI-002: MVI 패턴 정의 완료
- SPEC-ANDROID-FEATURE-HOME-001: Home 모듈 완료

**병렬 작업:**
- SPEC-ANDROID-FEATURE-CART-001

---

## ✅ 정의된 완료 조건

- ✅ :feature:detail 모듈 생성
- ✅ ProductDetailContract 정의
- ✅ ProductDetailViewModel 구현
- ✅ ProductDetailScreen UI 렌더링
- ✅ 14+ 테스트 작성 및 통과
- ✅ 코드 커버리지 95%+
- ✅ 빌드 성공

---

**END OF PLAN**
