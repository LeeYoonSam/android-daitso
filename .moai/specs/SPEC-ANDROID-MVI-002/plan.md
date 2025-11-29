---
id: SPEC-ANDROID-MVI-002-PLAN
version: 1.0.0
status: draft
created: 2025-11-29
updated: 2025-11-29
author: Albert (@user)
---

# SPEC-ANDROID-MVI-002: 구현 계획 (Implementation Plan)

## 개요

MVI 아키텍처의 기본 구조를 android-daitso 프로젝트에 구현하기 위한 상세 구현 계획입니다.
이 계획은 5개 단계의 작업으로 구성되며, 각 단계별로 산출물과 검증 기준을 정의합니다.

---

## 1. 구현 전략 (Implementation Strategy)

### 1.1 전체 접근 방식

**TDD (Test-Driven Development) 기반 구현**:
1. 각 기능별로 단위 테스트 먼저 작성
2. 테스트 실패 확인
3. 최소 코드로 테스트 통과
4. 리팩토링 및 개선

**순차적 구현**:
1. 기본 인터페이스 정의 (UiState, UiEvent, UiSideEffect)
2. BaseViewModel 추상 클래스 구현
3. Navigation 구조 정의
4. 테스트 케이스 작성 및 검증

### 1.2 작업 환경

**개발 환경**:
- IDE: Android Studio Ladybug (2024.2.1)
- Kotlin: 2.1.0+
- Gradle: 8.8+
- Target SDK: 35 (Android 15)

**의존성**:
- Jetpack Compose: 1.6.0+
- Hilt: 2.48+
- Kotlin Coroutines: 1.8.1+
- Jetpack Navigation: 2.8.0+

---

## 2. 구현 단계 (Implementation Phases)

### Phase 1: :core:ui 모듈 설정 및 준비

**목표**: :core:ui 모듈의 기본 구조 정비 및 의존성 설정

**작업**:
1. :core:ui 모듈 확인 (기존 모듈 재사용 또는 신규 생성)
2. build.gradle.kts 의존성 확인 및 추가
   - Jetpack Compose
   - Hilt
   - Kotlin Serialization
   - Testing dependencies (JUnit, MockK, Coroutine Test)
3. 패키지 구조 생성
   ```
   :core:ui/src/main/kotlin/com/example/daitso/ui/
   ├── mvi/
   │   ├── UiState.kt
   │   ├── UiEvent.kt
   │   ├── UiSideEffect.kt
   │   └── BaseViewModel.kt
   └── navigation/
       └── Route.kt
   ```
4. KDoc 주석 템플릿 준비

**산출물**:
- 설정된 :core:ui/build.gradle.kts
- 생성된 패키지 및 파일 구조
- README.md (모듈 설명서)

**소요 시간**: 약 30분
**우선순위**: 높음
**담당자**: 개발자

---

### Phase 2: MVI 인터페이스 정의 및 구현

**목표**: UiState, UiEvent, UiSideEffect 인터페이스 정의 및 문서화

**작업 항목**:

#### MVI-002-001: UiState 인터페이스 정의
```kotlin
// :core:ui/src/main/kotlin/com/example/daitso/ui/mvi/UiState.kt

/**
 * UI 상태를 나타내는 마커 인터페이스
 *
 * 모든 Feature의 UI 상태는 이 인터페이스를 상속받아야 합니다.
 *
 * 사용 예시:
 * ```
 * sealed class HomeUiState : UiState {
 *     data object Initial : HomeUiState()
 *     data class Success(val items: List<Item>) : HomeUiState()
 * }
 * ```
 */
interface UiState
```

**테스트 케이스**:
- `test_UiState_canBeImplemented`
- `test_UiState_withDataClass`

#### MVI-002-002: UiEvent 인터페이스 정의
```kotlin
// :core:ui/src/main/kotlin/com/example/daitso/ui/mvi/UiEvent.kt

/**
 * UI 이벤트를 나타내는 마커 인터페이스
 *
 * 모든 Feature의 UI 이벤트는 이 인터페이스를 상속받아야 합니다.
 * 사용자 인터렉션이나 시스템 이벤트를 표현합니다.
 */
interface UiEvent
```

**테스트 케이스**:
- `test_UiEvent_canBeImplemented`
- `test_UiEvent_withSealedClass`

#### MVI-002-003: UiSideEffect 인터페이스 정의
```kotlin
// :core:ui/src/main/kotlin/com/example/daitso/ui/mvi/UiSideEffect.kt

/**
 * UI 사이드 이펙트를 나타내는 마커 인터페이스
 *
 * UI 상태 변경 외의 부수 효과(Toast, Dialog, Navigation 등)를 표현합니다.
 * 일회성 이벤트로 처리되어야 합니다.
 */
interface UiSideEffect
```

**테스트 케이스**:
- `test_UiSideEffect_canBeImplemented`
- `test_UiSideEffect_withDataClass`

**산출물**:
- 3개의 마커 인터페이스 (UiState, UiEvent, UiSideEffect)
- KDoc 문서화
- 3개 이상의 단위 테스트

**소요 시간**: 약 1시간
**우선순위**: 높음
**담당자**: 개발자

---

### Phase 3: BaseViewModel 추상 클래스 구현

**목표**: MVI 패턴의 핵심 BaseViewModel 구현 및 검증

**작업 항목**:

#### MVI-002-003: BaseViewModel 핵심 구현
```kotlin
// :core:ui/src/main/kotlin/com/example/daitso/ui/mvi/BaseViewModel.kt

abstract class BaseViewModel<S : UiState, E : UiEvent, SE : UiSideEffect>(
    initialState: S
) : ViewModel() {

    // 상태 관리
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // 이벤트 처리
    private val _uiEvent = Channel<E>(capacity = Channel.BUFFERED)
    val uiEvent: ReceiveChannel<E> = _uiEvent

    // 사이드 이펙트 처리
    private val _uiSideEffect = Channel<SE>(capacity = Channel.BUFFERED)
    val uiSideEffect: ReceiveChannel<SE> = _uiSideEffect

    // 추상 메서드
    abstract suspend fun handleEvent(event: E)

    // 공개 메서드
    fun submitEvent(event: E) { /* ... */ }

    // 보호된 메서드
    protected suspend fun updateState(update: suspend (S) -> S) { /* ... */ }
    protected fun emitSideEffect(sideEffect: SE) { /* ... */ }

    init { /* 이벤트 처리 루프 시작 */ }
    override fun onCleared() { /* Resource cleanup */ }
}
```

**구현 상세**:
1. StateFlow를 이용한 상태 관리
2. Channel을 이용한 이벤트/사이드 이펙트 처리
3. ViewModel lifecycle에 맞춘 Resource 관리
4. 이벤트 처리 루프 자동 시작

**테스트 케이스** (8개 이상):
- `test_BaseViewModel_initialization`
- `test_BaseViewModel_submitEvent_addsEventToChannel`
- `test_BaseViewModel_handleEvent_processesEventCorrectly`
- `test_BaseViewModel_updateState_updatesStateFlow`
- `test_BaseViewModel_emitSideEffect_sendsToChannel`
- `test_BaseViewModel_eventProcessing_order`
- `test_BaseViewModel_resourceCleanup_onCleared`
- `test_BaseViewModel_multipleEvents_sequentialProcessing`

**산출물**:
- BaseViewModel 추상 클래스 구현
- 8개 이상의 단위 테스트
- KDoc 문서화
- 코드 커버리지 85% 이상

**소요 시간**: 약 2-3시간
**우선순위**: 높음 (핵심 기능)
**담당자**: 개발자

---

### Phase 4: Navigation Route 정의 및 구현

**목표**: Type-safe Navigation 구조 정의 및 구현

**작업 항목**:

#### MVI-002-004: Serializable Route 정의
```kotlin
// :core:ui/src/main/kotlin/com/example/daitso/ui/navigation/Route.kt

@Serializable
sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data class Detail(val itemId: String) : Route()

    @Serializable
    data object Settings : Route()
}
```

**구현 상세**:
1. Kotlin Serialization 활용
2. 각 Feature별 Route 확장 가능한 구조
3. Deep Link 지원 가능하도록 설계

**테스트 케이스** (3개 이상):
- `test_Route_serialization`
- `test_Route_deserialization`
- `test_Route_withParameters`

**산출물**:
- Route sealed class 정의
- 3개 이상의 단위 테스트
- Navigation NavGraph 통합 가이드 문서

**소요 시간**: 약 1시간
**우선순위**: 중간
**담당자**: 개발자

---

### Phase 5: 통합 테스트 및 문서화

**목표**: 전체 MVI 구조의 통합 검증 및 최종 문서화

**작업 항목**:

#### MVI-002-005: 통합 테스트
```
전체 MVI 흐름 검증:
1. Event 제출 → Event Channel 수신
2. Event 처리 → State 업데이트
3. State 변경 → StateFlow 갱신
4. SideEffect 발생 → SideEffect Channel 수신
```

**테스트 케이스** (3개 이상):
- `test_MVI_integration_eventToStateFlow`
- `test_MVI_integration_multipleConcurrentEvents`
- `test_MVI_integration_sideEffectAndStateHandling`

#### MVI-002-006: 코드 커버리지 검증
```bash
# Jacoco 코드 커버리지 측정
./gradlew :core:ui:jacocoTestDebugUnitTestReport

# 목표: 85% 이상
```

#### MVI-002-007: 문서화 및 가이드
```
산출물:
1. API 문서 (KDoc)
2. 사용 가이드 (README.md)
3. 예제 코드 (Example ViewModel)
4. 아키텍처 다이어그램
```

**산출물**:
- 통합 테스트 케이스 (3개 이상)
- 코드 커버리지 보고서 (85%+ 달성)
- 완성된 API 문서
- 개발자 가이드

**소요 시간**: 약 1.5-2시간
**우선순위**: 높음
**담당자**: 개발자

---

## 3. 기술 접근 방식 (Technical Approach)

### 3.1 아키텍처 설계 원칙

**SOLID 원칙 준수**:
- **Single Responsibility**: BaseViewModel은 상태 관리만 담당
- **Open/Closed**: 새로운 UiState/Event/SideEffect 추가에 열려있음
- **Liskov Substitution**: 모든 ViewModel은 BaseViewModel을 대체 가능
- **Interface Segregation**: UiState, UiEvent, UiSideEffect는 각각 독립적
- **Dependency Inversion**: 추상화(인터페이스)에 의존

**디자인 패턴**:
- **MVI Pattern**: 단방향 데이터 흐름
- **Template Method**: BaseViewModel의 handleEvent()
- **Observer Pattern**: StateFlow와 Channel

### 3.2 핵심 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| Kotlin | 2.1.0+ | 언어 |
| Jetpack Compose | 1.6.0+ | UI 프레임워크 |
| Hilt | 2.48+ | 의존성 주입 |
| Coroutines | 1.8.1+ | 비동기 처리 |
| StateFlow | 1.8.1+ | 상태 관리 |
| Channel | 1.8.1+ | 이벤트 처리 |
| Kotlin Serialization | 1.6.0+ | 객체 직렬화 |
| JUnit | 4.13.2 | 단위 테스트 |
| MockK | 1.13.5+ | Mock 라이브러리 |
| Turbine | 1.0.0+ | Flow 테스트 |

### 3.3 개발 패턴

**이벤트 처리 패턴**:
```
when (event) {
    is HomeUiEvent.OnLoad -> handleLoadEvent()
    is HomeUiEvent.OnRefresh -> handleRefreshEvent()
    is HomeUiEvent.OnError -> handleErrorEvent()
}
```

**상태 업데이트 패턴**:
```
updateState { currentState ->
    currentState.copy(isLoading = true)
}
```

**사이드 이펙트 패턴**:
```
emitSideEffect(HomeSideEffect.ShowToast("Success"))
```

---

## 4. 마일스톤 및 우선순위 (Milestones)

### Primary Goal (1순위)
**Core MVI 구조 완성**
- Task MVI-002-001: :core:ui 모듈 설정
- Task MVI-002-002: MVI 인터페이스 정의
- Task MVI-002-003: BaseViewModel 구현
- Task MVI-002-004: 테스트 (14개 이상)

**완료 기준**:
- BaseViewModel 구현 완료
- 단위 테스트 14개 이상 통과
- 코드 커버리지 85% 이상

### Secondary Goal (2순위)
**Navigation 통합 및 문서화**
- Task MVI-002-005: Navigation Route 정의
- Task MVI-002-006: API 문서 작성
- Task MVI-002-007: 사용 가이드 작성

**완료 기준**:
- Type-safe Navigation 구현
- 완성된 KDoc 문서
- 개발자 가이드 완성

### Final Goal (3순위)
**통합 테스트 및 최적화**
- Task MVI-002-008: 통합 테스트 작성
- Task MVI-002-009: 성능 최적화
- Task MVI-002-010: 최종 검증

**완료 기준**:
- 통합 테스트 통과
- 성능 메트릭 달성 (이벤트 처리 < 100ms)
- 모든 정합성 검사 통과

---

## 5. 작업 분해 (Task Breakdown)

### MVI-002-001: :core:ui 모듈 설정
```
1. 모듈 확인 또는 생성
   - :core:ui 모듈 존재 확인
   - 없으면 신규 생성 (./gradlew :core:ui:properties)

2. build.gradle.kts 업데이트
   - Compose 의존성 확인
   - Hilt 추가 (미포함 시)
   - Kotlin Serialization 추가
   - Testing 의존성 추가 (JUnit, MockK, Turbine)

3. 패키지 구조 생성
   - src/main/kotlin/com/example/daitso/ui/mvi/
   - src/main/kotlin/com/example/daitso/ui/navigation/
   - src/test/kotlin/com/example/daitso/ui/mvi/

4. README.md 작성
   - 모듈 목적 설명
   - 의존성 목록
   - 사용 방법

예상 시간: 30분
```

### MVI-002-002: UiState, UiEvent, UiSideEffect 정의
```
1. UiState.kt 작성
   - interface UiState 정의
   - KDoc 주석 추가
   - 사용 예시 포함

2. UiEvent.kt 작성
   - interface UiEvent 정의
   - KDoc 주석 추가
   - 사용 예시 포함

3. UiSideEffect.kt 작성
   - interface UiSideEffect 정의
   - KDoc 주석 추가
   - 사용 예시 포함

4. 단위 테스트 작성 (6개 이상)
   - UiStateTest.kt
   - UiEventTest.kt
   - UiSideEffectTest.kt

예상 시간: 1시간
```

### MVI-002-003: BaseViewModel 구현
```
1. BaseViewModel.kt 작성
   - 추상 클래스 정의
   - StateFlow 구현
   - Channel 구현
   - submitEvent() 메서드
   - updateState() 메서드
   - emitSideEffect() 메서드
   - init 블록 (이벤트 처리 루프)
   - onCleared() 메서드

2. 상세 구현
   - 제네릭 타입 파라미터: S, E, SE
   - ViewModel 상속
   - viewModelScope 활용
   - Flow 연산자 최적화

3. 단위 테스트 작성 (8개 이상)
   - BaseViewModelTest.kt
   - 초기화, 이벤트, 상태, 사이드 이펙트 등

4. KDoc 문서화
   - 클래스 문서
   - 메서드 문서
   - 파라미터 설명
   - 사용 예시

예상 시간: 2-3시간
```

### MVI-002-004: Navigation Route 정의
```
1. Route.kt 작성
   - @Serializable sealed class Route 정의
   - Home Route
   - Detail Route (파라미터 포함)
   - Settings Route

2. 확장 가능한 구조 설계
   - Feature별 Route 추가 가능
   - Deep Link 지원 설계

3. 단위 테스트 작성 (3개 이상)
   - RouteTest.kt
   - Serialization 테스트
   - 파라미터 테스트

4. Navigation NavGraph 통합 가이드
   - 문서 작성
   - 코드 샘플 제공

예상 시간: 1시간
```

### MVI-002-005: 통합 테스트 및 최종 검증
```
1. 통합 테스트 작성
   - IntegrationTest.kt
   - Event → State 흐름
   - State → UI 반영
   - SideEffect 처리
   - 동시 처리 시나리오

2. 코드 커버리지 검증
   - Jacoco 리포트 생성
   - 85% 이상 달성 확인

3. 성능 테스트
   - 이벤트 처리 시간 측정
   - 100ms 이하 확인
   - 메모리 프로파일링

4. 문서화 최종 정리
   - README.md 완성
   - API 문서 검수
   - 예제 코드 검증

예상 시간: 1.5-2시간
```

---

## 6. 위험 및 대응 방안 (Risks & Mitigation)

### Risk 1: Channel capacity 오버플로우
**위험도**: 중간
**원인**: 이벤트가 빠르게 발생하면 Channel 버퍼 초과 가능
**대응**:
- Channel capacity를 BUFFERED(64)로 설정
- 필요시 capacity 증가 (128, 256)
- 이벤트 처리 성능 모니터링

### Risk 2: StateFlow 메모리 누수
**위험도**: 중간
**원인**: StateFlow 구독이 해제되지 않으면 메모리 누수
**대응**:
- View lifecycle과 StateFlow 구독 연동
- LaunchedEffect 또는 repeatOnLifecycle 사용
- onCleared()에서 명시적 정리

### Risk 3: 이벤트 순서 보장 실패
**위험도**: 낮음
**원인**: 동시 다발적 이벤트 처리 시 순서 문제
**대응**:
- Channel의 FIFO 특성으로 순서 보장
- 복잡한 상태는 lock 메커니즘 추가
- 통합 테스트에서 동시 처리 검증

### Risk 4: 테스트 작성 시간 초과
**위험도**: 낮음
**원인**: 복잡한 StateFlow/Channel 테스트
**대응**:
- Turbine 라이브러리로 Flow 테스트 간편화
- 테스트 템플릿 사전 준비
- 핵심 테스트부터 우선 작성

### Risk 5: 기존 코드와 호환성
**위험도**: 낮음
**원인**: 기존 ViewModel과의 충돌
**대응**:
- BaseViewModel을 선택적으로 적용
- 점진적 마이그레이션 전략
- 기존 코드와 병행 운영

---

## 7. 검증 기준 (Definition of Done)

### Phase별 검증 기준

**Phase 1 완료 기준**:
- [ ] :core:ui/build.gradle.kts 모든 의존성 추가
- [ ] 패키지 구조 생성 완료
- [ ] README.md 작성 완료

**Phase 2 완료 기준**:
- [ ] 3개 인터페이스 정의 및 KDoc 작성
- [ ] 6개 이상 단위 테스트 작성
- [ ] 모든 테스트 통과

**Phase 3 완료 기준**:
- [ ] BaseViewModel 구현 완료
- [ ] 8개 이상 단위 테스트 작성 및 통과
- [ ] 코드 커버리지 85% 이상
- [ ] KDoc 문서화 완료

**Phase 4 완료 기준**:
- [ ] Route sealed class 정의
- [ ] 3개 이상 단위 테스트 작성 및 통과
- [ ] Navigation 통합 가이드 작성

**Phase 5 완료 기준**:
- [ ] 통합 테스트 작성 및 통과
- [ ] 코드 커버리지 85% 이상 유지
- [ ] 성능 메트릭 달성 (이벤트 처리 < 100ms)
- [ ] 완전한 API 문서화
- [ ] 개발자 가이드 완성

### 전체 SPEC 완료 기준
- [ ] 모든 기능 요구사항 구현
- [ ] 모든 비기능 요구사항 검증
- [ ] 14개 이상 단위 테스트 통과
- [ ] 코드 커버리지 85% 이상
- [ ] 모든 문서화 완료
- [ ] 성능 메트릭 달성

---

## 8. 다음 단계 (Next Steps)

### 구현 후 진행 사항

1. **Feature 모듈 적용**
   - Home, Detail 등 각 Feature 모듈에서 BaseViewModel 상속
   - SPEC-ANDROID-FEATURE-HOME-001 등에서 활용

2. **Navigation 통합**
   - NavGraph와 Route 연동
   - Deep Link 지원 추가

3. **테스트 강화**
   - 통합 테스트 추가 (E2E)
   - 성능 테스트 자동화

4. **문서화 확대**
   - 아키텍처 가이드 작성
   - 마이그레이션 가이드 작성 (기존 ViewModel → BaseViewModel)

---

## 참고 (References)

### 관련 문서
- SPEC-ANDROID-INIT-001: 프로젝트 초기 설정
- SPEC-ANDROID-FEATURE-HOME-001: Home Feature 구현
- android-daitso project structure

### 외부 자료
- [MVI Architecture Pattern](https://github.com/MindorksOpenSource/MVI-Architecture-Android-Beginners)
- [Jetpack Compose State Management](https://developer.android.com/jetpack/compose/state)
- [Kotlin Coroutines Flow](https://kotlinlang.org/docs/flow.html)
- [Android ViewModel Lifecycle](https://developer.android.com/topic/libraries/architecture/viewmodel)

