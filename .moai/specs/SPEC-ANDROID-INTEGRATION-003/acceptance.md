# SPEC-ANDROID-INTEGRATION-003: 앱 통합 및 네비게이션 - 수용 기준

## 📋 통합 테스트 시나리오

### 시나리오 1: 앱 빌드 및 실행

```gherkin
GIVEN :app 모듈이 모든 Feature 의존성을 포함했을 때
WHEN 앱을 빌드하고 에뮬레이터에서 실행했을 때
THEN 앱 시작 성공
AND Home 화면이 표시됨
AND Hilt 의존성 주입 성공
```

**검증:**
```bash
./gradlew :app:build                    # 빌드 성공
./gradlew :app:installDebug             # 설치 성공
adb shell am start -n com.bup.ys.daitso/.MainActivity  # 실행
```

---

### 시나리오 2: Home 화면 → ProductDetail 네비게이션

```gherkin
GIVEN Home 화면이 열려있고 상품 목록이 표시되었을 때
WHEN 특정 상품 카드를 클릭했을 때
THEN ProductDetail 화면으로 네비게이션됨
AND 정확한 productId가 전달됨
AND 상품 상세 정보가 표시됨
```

**검증:**
```
1. 에뮬레이터에서 앱 실행
2. Home 화면 확인 (상품 목록 표시)
3. 상품 선택
4. ProductDetail 화면으로 이동 확인
5. 상품 정보 정확성 확인
```

---

### 시나리오 3: ProductDetail 화면 → Cart 네비게이션

```gherkin
GIVEN ProductDetail 화면이 열려있을 때
WHEN "장바구니 담기" 버튼을 클릭했을 때
THEN Cart 화면으로 네비게이션됨
AND 추가된 상품이 장바구니에 표시됨
```

**검증:**
```
1. ProductDetail 화면에서 수량 선택
2. "장바구니 담기" 클릭
3. Cart 화면으로 이동 확인
4. 추가된 상품 확인
```

---

### 시나리오 4: 뒤로가기 네비게이션

```gherkin
GIVEN 어떤 화면이든 열려있을 때
WHEN 뒤로가기 버튼을 클릭했을 때
THEN 이전 화면으로 돌아감
AND 이전 화면의 상태가 유지됨
```

**검증:**
```
1. Home → Detail → 뒤로가기 → Home 확인
2. Home → Detail → Cart → 뒤로가기 (Cart) → 뒤로가기 (Detail) → 뒤로가기 (Home) 확인
```

---

### 시나리오 5: Hilt 의존성 그래프 컴파일

```gherkin
GIVEN 모든 Feature 모듈에 @HiltViewModel, @Inject가 적용되었을 때
WHEN 앱을 컴파일했을 때
THEN Hilt 의존성 그래프 생성 성공
AND 의존성 충돌 없음
AND 모든 @HiltViewModel이 주입 가능
```

**검증:**
```bash
./gradlew :app:compileDebugKotlin  # 성공 확인
./gradlew :app:kaptGenerateStubsDebugKotlin  # 없으면 KSP 사용
```

---

### 시나리오 6: 완전 플로우 테스트 (Home → Detail → Add to Cart → Cart)

```gherkin
GIVEN 앱이 실행되고 Home 화면이 표시되었을 때
WHEN 사용자가 다음과 같이 동작했을 때:
  1. Home에서 상품 선택
  2. ProductDetail에서 "장바구니 담기" 클릭
  3. Cart 화면에서 추가된 상품 확인
THEN 모든 네비게이션이 올바르게 동작함
AND 데이터가 정확히 전달됨
AND 각 화면에서 상태가 올바르게 유지됨
```

**상세 검증 단계:**
```
1. Home 화면
   - 상품 목록이 그리드로 표시됨
   - 로딩 상태 표시 확인
   - 상품 이미지, 이름, 가격 확인

2. ProductDetail 화면
   - 선택한 상품의 상세 정보 표시
   - 수량 선택 기능 동작
   - "장바구니 담기" 버튼 활성화

3. Cart 화면
   - 추가된 상품이 리스트에 표시됨
   - 수량, 가격, 총 가격 올바름
   - 삭제 기능 동작
```

---

### 시나리오 7: 에러 상황 처리

```gherkin
GIVEN 네트워크 오류가 발생했을 때
WHEN 각 화면에서 에러 상태를 표시했을 때
THEN 에러 메시지가 표시됨
AND Retry 버튼이 활성화됨
AND 네트워크 복구 후 재시도 성공
```

---

### 시나리오 8: 성능 검증

```gherkin
GIVEN 앱이 실행되었을 때
WHEN 성능 지표를 측정했을 때
THEN 앱 시작 시간 < 3초
AND 메모리 누수 없음
AND 프레임 드롭 없음 (60fps 유지)
```

**성능 측정 도구:**
- Android Profiler (CPU, Memory, Network)
- Frame Rate 모니터링
- App Startup Time 측정

---

### 시나리오 9: Type-safe Navigation 검증

```gherkin
GIVEN 모든 Route가 Serializable로 정의되었을 때
WHEN 화면 간 네비게이션할 때
THEN 파라미터가 타입 안전하게 전달됨
AND 파라미터 타입 불일치 시 컴파일 오류 발생
```

---

### 시나리오 10: 다양한 기기에서 테스트

```gherkin
GIVEN 다양한 화면 크기의 기기에서 앱을 실행했을 때
WHEN 모든 화면을 네비게이션했을 때
THEN Phone에서 정상 동작
AND Tablet에서 레이아웃 조정되어 동작
AND 태블릿 가로 방향 회전 시 UI 조정
```

---

## 🎯 수용 기준 (Acceptance Criteria)

### 구조적 요구사항

- ✅ :feature:home, :feature:detail, :feature:cart이 :app/build.gradle.kts에 의존성으로 추가됨
- ✅ Gradle Sync 성공
- ✅ 순환 의존성 없음

### 네비게이션 요구사항

- ✅ Type-safe Navigation Routes 정의 (HomeRoute, ProductDetailRoute, CartRoute)
- ✅ NavigationHost 구현 완료
- ✅ Home → Detail → Cart 플로우 동작
- ✅ 모든 화면에서 뒤로가기 동작

### Hilt 요구사항

- ✅ @HiltAndroidApp 적용 확인
- ✅ @HiltViewModel 모든 ViewModel에 적용
- ✅ Hilt 의존성 그래프 컴파일 성공
- ✅ 런타임 DI 오류 없음

### 성능 요구사항

- ✅ 앱 시작 시간 < 3초
- ✅ 메모리 누수 없음
- ✅ UI 프레임 드롭 없음 (60fps)

### 기능 요구사항

- ✅ 앱 빌드 성공 (`./gradlew :app:build`)
- ✅ 에뮬레이터에서 실행 성공
- ✅ 모든 네비게이션 플로우 동작
- ✅ 각 화면에서 올바른 데이터 표시
- ✅ 상태 유지 (Back/Forward 네비게이션)

---

## 📊 품질 게이트

| 항목 | 상태 | 검증 방법 |
|------|------|---------|
| 빌드 성공 | ✅ | `./gradlew :app:build` |
| Gradle Sync | ✅ | Android Studio Sync |
| Hilt DI 그래프 | ✅ | `./gradlew :app:compileDebugKotlin` |
| 에뮬레이터 실행 | ✅ | 에뮬레이터 설치 및 실행 |
| 네비게이션 플로우 | ✅ | 수동 테스트 |
| 성능 | ✅ | Android Profiler |
| 커버리지 | ✅ | `./gradlew :app:connectedAndroidTest` |

---

## 📝 테스트 실행 가이드

### 1. 빌드 및 설치

```bash
# Debug 빌드
./gradlew :app:build

# 에뮬레이터 설치
./gradlew :app:installDebug

# Release 빌드
./gradlew :app:assembleRelease
```

### 2. 에뮬레이터에서 실행

```bash
# 에뮬레이터 실행 (이미 실행 중이면 생략)
emulator -avd [AVD_NAME]

# 앱 실행
adb shell am start -n com.bup.ys.daitso/.MainActivity

# 로그 보기
adb logcat | grep "daitso\|Hilt"
```

### 3. 수동 테스트 체크리스트

- [ ] Home 화면 열기 (상품 목록 표시)
- [ ] 상품 클릭 (ProductDetail로 이동)
- [ ] ProductDetail에서 수량 선택
- [ ] "장바구니 담기" 클릭 (Cart로 이동)
- [ ] Cart 화면에서 아이템 확인
- [ ] 뒤로가기 버튼 테스트 (여러 번)
- [ ] 상품 수량 변경 (Cart에서)
- [ ] 상품 삭제 (Cart에서)
- [ ] 네트워크 오류 시뮬레이션 (에뮬레이터 설정)
- [ ] 에러 재시도

### 4. 성능 측정

```bash
# Android Profiler 실행 (Android Studio)
# 1. Run → Debug app
# 2. View → Tool Windows → Profiler
# 3. CPU, Memory, Network 탭에서 모니터링

# 또는 명령줄에서
adb shell am start -W com.bup.ys.daitso/.MainActivity  # 시작 시간 측정
```

---

## 🔗 관련 리소스

- Navigation Compose: https://developer.android.com/jetpack/compose/navigation
- Type-safe Navigation: https://developer.android.com/jetpack/compose/navigation/type-safe
- Hilt Documentation: https://dagger.dev/hilt/
- Android Performance: https://developer.android.com/topic/performance

---

**END OF ACCEPTANCE**
