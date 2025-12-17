# SPEC-ANDROID-ARCH-001: Acceptance Criteria

## 인수 테스트 시나리오

---

## Scenario 1: DI Module 일관성 검증

### AC-001: 모든 Feature에 DI Module 존재

**GIVEN** 모든 Feature 모듈(home, detail, cart)이 존재할 때,

**WHEN** DI Module 파일을 검색하면,

**THEN** 각 Feature에 `{Feature}Module.kt`가 `di/` 디렉토리에 존재해야 한다.

**검증 스크립트:**
```bash
# DI Module 파일 개수 확인 (결과: 3개여야 함)
find feature -name "*Module.kt" -path "*/di/*" | wc -l
# 예상 결과: 3

# DI Module 파일 목록
find feature -name "*Module.kt" -path "*/di/*"
# 예상 결과:
# feature/home/.../di/HomeModule.kt
# feature/detail/.../di/DetailModule.kt
# feature/cart/.../di/CartModule.kt
```

### AC-002: DI Module Hilt 어노테이션 확인

**GIVEN** 각 DI Module이 존재할 때,

**WHEN** Module 파일의 어노테이션을 확인하면,

**THEN** `@Module`과 `@InstallIn` 어노테이션이 존재해야 한다.

**검증 스크립트:**
```bash
# @Module 어노테이션 확인
grep -l "@Module" feature/*/src/main/kotlin/**/di/*Module.kt | wc -l
# 예상 결과: 3

# @InstallIn 어노테이션 확인
grep -l "@InstallIn" feature/*/src/main/kotlin/**/di/*Module.kt | wc -l
# 예상 결과: 3
```

---

## Scenario 2: Navigation 파일 일관성 검증

### AC-003: 모든 Feature에 Navigation 파일 존재

**GIVEN** 모든 Feature 모듈이 존재할 때,

**WHEN** Navigation 파일을 검색하면,

**THEN** 각 Feature에 `{Feature}Navigation.kt`가 존재해야 한다.

**검증 스크립트:**
```bash
# Navigation 파일 개수 확인 (결과: 3개여야 함)
find feature -name "*Navigation.kt" | wc -l
# 예상 결과: 3

# Navigation 파일 목록
find feature -name "*Navigation.kt"
# 예상 결과:
# feature/home/.../navigation/HomeNavigation.kt
# feature/detail/.../navigation/DetailNavigation.kt
# feature/cart/.../navigation/CartNavigation.kt
```

### AC-004: Navigation 파일에 NavGraphBuilder 확장 함수 존재

**GIVEN** 각 Navigation 파일이 존재할 때,

**WHEN** NavGraphBuilder 확장 함수를 검색하면,

**THEN** 각 파일에 `fun NavGraphBuilder.{feature}Screen` 형식의 함수가 존재해야 한다.

**검증 스크립트:**
```bash
# NavGraphBuilder 확장 함수 확인
grep -r "fun NavGraphBuilder\." feature/*/src/main/kotlin/**/navigation/*Navigation.kt
# 예상 결과: 3개의 확장 함수
```

---

## Scenario 3: 디렉토리 구조 표준화 검증

### AC-005: 표준 디렉토리 구조 확인

**GIVEN** 각 Feature 모듈이 존재할 때,

**WHEN** 디렉토리 구조를 확인하면,

**THEN** 모든 Feature가 다음 디렉토리를 포함해야 한다:
- `contract/`
- `di/`
- `navigation/`
- `ui/` 또는 `presentation/`
- `viewmodel/`

**검증 스크립트:**
```bash
# 각 Feature 디렉토리 구조 확인
for feature in home detail cart; do
  echo "=== $feature ==="
  ls -d feature/$feature/src/main/kotlin/com/bup/ys/daitso/feature/$feature/*/
done
# 예상 결과: 각 Feature에서 5개 디렉토리 출력
```

### AC-006: Home Feature 필수 디렉토리 확인

**GIVEN** Home Feature 모듈이 존재할 때,

**WHEN** Home의 디렉토리 구조를 확인하면,

**THEN** `contract/`, `di/`, `navigation/`, `ui/`, `viewmodel/` 디렉토리가 존재해야 한다.

**검증 스크립트:**
```bash
# Home Feature 디렉토리 확인
ls -d feature/home/src/main/kotlin/com/bup/ys/daitso/feature/home/{contract,di,navigation,ui,viewmodel}/ 2>/dev/null | wc -l
# 예상 결과: 5
```

### AC-007: Detail Feature 필수 디렉토리 확인

**GIVEN** Detail Feature 모듈이 존재할 때,

**WHEN** Detail의 디렉토리 구조를 확인하면,

**THEN** `contract/`, `di/`, `navigation/`, `ui/`, `viewmodel/` 디렉토리가 존재해야 한다.

**검증 스크립트:**
```bash
# Detail Feature 디렉토리 확인
ls -d feature/detail/src/main/kotlin/com/bup/ys/daitso/feature/detail/{contract,di,navigation,ui,viewmodel}/ 2>/dev/null | wc -l
# 예상 결과: 5
```

---

## Scenario 4: 빌드 및 테스트 검증

### AC-008: 전체 빌드 성공

**GIVEN** 모든 변경이 완료되었을 때,

**WHEN** 전체 빌드를 실행하면,

**THEN** 빌드가 성공해야 한다.

**검증 스크립트:**
```bash
./gradlew assembleDebug
# 예상 결과: BUILD SUCCESSFUL
```

### AC-009: 전체 테스트 통과

**GIVEN** 모든 변경이 완료되었을 때,

**WHEN** 전체 테스트를 실행하면,

**THEN** 모든 테스트가 통과해야 한다.

**검증 스크립트:**
```bash
./gradlew test
# 예상 결과: BUILD SUCCESSFUL
```

---

## Scenario 5: 기능 동작 검증

### AC-010: Home 화면 Navigation 동작

**GIVEN** HomeNavigation이 설정되었을 때,

**WHEN** Home 화면에서 상품을 선택하면,

**THEN** Detail 화면으로 정상 이동해야 한다.

### AC-011: Detail 화면 Navigation 동작

**GIVEN** DetailNavigation이 설정되었을 때,

**WHEN** Detail 화면에서 장바구니 버튼을 클릭하면,

**THEN** Cart 화면으로 정상 이동해야 한다.

---

## 검증 완료 체크리스트

### DI Module 검증
- [ ] AC-001: 모든 Feature에 DI Module 존재
- [ ] AC-002: DI Module Hilt 어노테이션 확인

### Navigation 파일 검증
- [ ] AC-003: 모든 Feature에 Navigation 파일 존재
- [ ] AC-004: NavGraphBuilder 확장 함수 존재

### 디렉토리 구조 검증
- [ ] AC-005: 표준 디렉토리 구조 확인
- [ ] AC-006: Home Feature 필수 디렉토리 확인
- [ ] AC-007: Detail Feature 필수 디렉토리 확인

### 빌드 및 테스트 검증
- [ ] AC-008: 전체 빌드 성공
- [ ] AC-009: 전체 테스트 통과

### 기능 동작 검증
- [ ] AC-010: Home 화면 Navigation 동작
- [ ] AC-011: Detail 화면 Navigation 동작

---

**END OF ACCEPTANCE CRITERIA**
