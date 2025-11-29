---
id: SPEC-ANDROID-MVI-002
version: 1.0.0
status: draft
created: 2025-11-29
updated: 2025-11-29
author: Albert (@user)
priority: high
related_specs: [SPEC-ANDROID-INIT-001, SPEC-ANDROID-FEATURE-HOME-001]
---

# SPEC-ANDROID-MVI-002: MVI 아키텍처 기본 구조 및 네비게이션

## 히스토리 (HISTORY)

| 일자 | 버전 | 작성자 | 내용 |
|------|------|--------|------|
| 2025-11-29 | 1.0.0 | Albert (@user) | 최초 작성 - MVI 아키텍처 기본 구조 정의 |

## 1. 개요 (Overview)

### 1.1 MVI 패턴 개념

MVI (Model-View-Intent) 패턴은 단방향 데이터 흐름(Unidirectional Data Flow)을 기반으로 하는 아키텍처 패턴입니다.
사용자 인터렉션(Intent)에 따라 모델(Model)이 변경되고, 뷰(View)가 갱신되는 순환 구조를 형성합니다.

### 1.2 MVI 패턴의 장점

- **단방향 데이터 흐름**: 데이터가 한 방향으로만 흐르므로 상태 변화를 예측하기 쉬움
- **테스트 가능성**: 상태 변화와 이벤트 처리가 순수 함수로 분리되어 테스트 용이
- **상태 추적 가능성**: 모든 상태 변화가 기록되므로 디버깅이 용이
- **확장성**: 새로운 화면이나 기능 추가 시 기존 코드 수정 최소화
- **코루틴 기반**: Kotlin Coroutines과 Flow를 활용한 효율적인 비동기 처리

### 1.3 프로젝트 적용 이유

현재 android-daitso 프로젝트는 다중 모듈 구조(Multi-module architecture)로 구성되어 있습니다.
각 Feature 모듈이 독립적으로 개발되면서도 일관된 상태 관리와 네비게이션 구조가 필요합니다.
MVI 패턴을 적용하여:

1. Feature 모듈 간 상태 관리의 일관성 보장
2. ViewModel 기반 상태 관리와 이벤트 처리의 표준화
3. Jetpack Compose와의 자연스러운 통합
4. 테스트 가능하고 유지보수하기 좋은 구조 구축

### 1.4 SPEC의 목표

이 SPEC은 다음을 목표로 합니다:

1. MVI 패턴의 핵심 요소(UiState, UiEvent, UiSideEffect) 정의
2. 재사용 가능한 BaseViewModel 추상 클래스 구현
3. Type-safe Navigation 구조 구축
4. 모든 Feature 모듈에서 사용 가능한 표준화된 구조 제공

---

## 2. 기능 요구사항 (Functional Requirements)

### 2.1 MVI 인터페이스 정의

#### FR-MVI-001: UiState 인터페이스
```
Given: :core:ui 모듈에서 UiState 인터페이스를 정의할 때
When: 각 ViewModel이 UI 상태를 표현하고자 할 때
Then: UiState 인터페이스를 상속받아 제네릭 타입으로 상태 객체 정의 가능해야 함

상태 유형:
- Initial: 초기 상태 (앱 시작 또는 화면 진입 시)
- Loading: 데이터 로딩 중
- Success: 데이터 로딩 성공
- Error: 에러 발생
```

#### FR-MVI-002: UiEvent 인터페이스
```
Given: ViewModel이 사용자 인터렉션을 처리할 때
When: UI 이벤트(클릭, 입력 등)를 ViewModel으로 전달할 때
Then: 각 이벤트는 UiEvent를 상속받은 sealed class/data class로 정의되어야 함

이벤트 처리:
- 사용자 인터렉션 (Click, Input, Selection)
- 시스템 이벤트 (Lifecycle, Permission)
- 네비게이션 이벤트 (Navigate, Back)
```

#### FR-MVI-003: UiSideEffect 인터페이스
```
Given: ViewModel에서 UI 상태 변경 외 부수 효과가 필요할 때
When: Toast, Dialog, Navigation 등의 일회성 이벤트를 발생시킬 때
Then: UiSideEffect를 상속받은 sealed class로 정의하여 Channel을 통해 전달

사이드 이펙트 유형:
- Navigation: 화면 네비게이션
- Toast: 토스트 메시지
- Dialog: 다이얼로그 표시
- Analytics: 분석 이벤트 전송
```

### 2.2 BaseViewModel 추상 클래스

#### FR-MVI-004: BaseViewModel 구현
```
Given: MVI 인터페이스가 정의된 상태
When: 모든 Feature ViewModel이 BaseViewModel을 상속받을 때
Then: 다음 기능을 제공해야 함:

1. StateFlow 기반 상태 관리
   - _uiState: MutableStateFlow<S>
   - uiState: StateFlow<S> (읽기 전용)

2. Channel 기반 이벤트 처리
   - _uiEvent: Channel<E>
   - uiEvent: ReceiveChannel<E>

3. Channel 기반 사이드 이펙트 처리
   - _uiSideEffect: Channel<SE>
   - uiSideEffect: ReceiveChannel<SE>

4. 이벤트 처리 메서드
   - abstract fun handleEvent(event: E)
   - fun submitEvent(event: E)

5. 상태 업데이트 헬퍼
   - protected fun updateState(update: suspend (S) -> S)
   - protected fun emitSideEffect(sideEffect: SE)
```

#### FR-MVI-005: 단방향 데이터 흐름
```
Given: ViewModel이 State, Event, SideEffect를 가질 때
When: 사용자 인터렉션이 발생할 때
Then: 다음 흐름이 보장되어야 함:

User Interaction
    ↓
UI Event (Event Channel 전송)
    ↓
ViewModel.handleEvent() 처리
    ↓
State Update (StateFlow 업데이트)
    ↓
UI Recompose (StateFlow 구독)
    ↓
Side Effects 발생 (필요시 SideEffect Channel 전송)
```

#### FR-MVI-006: StateFlow와 Channel 기반 구조
```
Given: 상태 관리와 이벤트 처리가 필요할 때
When: StateFlow와 Channel을 조합할 때
Then: 다음 특성을 가져야 함:

StateFlow 사용:
- UI 상태 저장
- 최신 값 항상 사용 가능
- Compose에서 collectAsState() 사용 가능

Channel 사용:
- 일회성 이벤트 전송
- 구독자가 없을 때 버퍼링 (capacity 설정)
- UI 이벤트와 사이드 이펙트 처리
```

---

## 3. 비기능 요구사항 (Non-Functional Requirements)

### 3.1 성능 (Performance)

#### NFR-MVI-001: 이벤트 처리 속도
```
Requirement: StateFlow 이벤트 처리 지연시간
Metric: 이벤트 발생 → 상태 업데이트 완료 시간 < 100ms
Measurement: Unit test에서 측정
Context: 네트워크 요청이 없는 로컬 상태 변경 기준
```

#### NFR-MVI-002: 메모리 효율성
```
Requirement: StateFlow와 Channel의 메모리 사용 최적화
Metric: ViewModel당 메모리 사용량 < 5MB
Context: 평균적인 Feature ViewModel 기준
```

### 3.2 확장성 (Scalability)

#### NFR-MVI-003: 모듈 추가 용이성
```
Requirement: 새로운 Feature 모듈 추가 시 BaseViewModel 상속 가능
Metric: 새 Feature 모듈 추가 시 boilerplate 코드 < 50줄
Context: State, Event, SideEffect 정의 및 handleEvent() 구현
```

#### NFR-MVI-004: 상태 관리 확장성
```
Requirement: 복잡한 상태 관리를 위한 확장 지원
Metric: 여러 State 객체 조합 가능
Context: 다중 데이터 소스 병합 등 복잡한 상태 구조 지원
```

### 3.3 테스트 가능성 (Testability)

#### NFR-MVI-005: 단위 테스트 커버리지
```
Requirement: BaseViewModel과 MVI 인터페이스 테스트 커버리지
Metric: 85%+ 코드 커버리지
Tool: Jacoco 코드 커버리지 측정
```

#### NFR-MVI-006: 테스트 작성 용이성
```
Requirement: 상태 변화 테스트가 간단하고 명확해야 함
Metric: 각 Event에 대한 State 변화를 예측 가능하게 테스트 가능
Context: Given-When-Then 형식의 BDD 테스트 지원
```

---

## 4. 인터페이스 요구사항 (Interface Requirements)

### 4.1 UiState 인터페이스

```kotlin
/**
 * UI 상태를 나타내는 마커 인터페이스
 * 모든 Feature의 State는 이 인터페이스를 상속받아야 함
 */
interface UiState
```

**사용 예시:**
```kotlin
sealed class HomeUiState : UiState {
    data object Initial : HomeUiState()
    data object Loading : HomeUiState()
    data class Success(val items: List<HomeItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
```

### 4.2 UiEvent 인터페이스

```kotlin
/**
 * UI 이벤트를 나타내는 마커 인터페이스
 * 모든 Feature의 Event는 이 인터페이스를 상속받아야 함
 */
interface UiEvent
```

**사용 예시:**
```kotlin
sealed class HomeUiEvent : UiEvent {
    data object OnLoadItems : HomeUiEvent()
    data class OnItemClicked(val itemId: String) : HomeUiEvent()
    data object OnRefresh : HomeUiEvent()
}
```

### 4.3 UiSideEffect 인터페이스

```kotlin
/**
 * UI 사이드 이펙트를 나타내는 마커 인터페이스
 * 일회성 이벤트(Toast, Dialog, Navigation 등)를 표현
 */
interface UiSideEffect
```

**사용 예시:**
```kotlin
sealed class HomeSideEffect : UiSideEffect {
    data class ShowToast(val message: String) : HomeSideEffect()
    data class NavigateToDetail(val itemId: String) : HomeSideEffect()
}
```

### 4.4 BaseViewModel 추상 클래스

```kotlin
/**
 * MVI 패턴의 기본 ViewModel 구현
 *
 * @param S UiState 타입
 * @param E UiEvent 타입
 * @param SE UiSideEffect 타입
 */
abstract class BaseViewModel<S : UiState, E : UiEvent, SE : UiSideEffect>(
    initialState: S
) : ViewModel() {

    // 상태 관리
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // 이벤트 처리 (Channel 용량: 100)
    private val _uiEvent = Channel<E>(capacity = Channel.BUFFERED)
    val uiEvent: ReceiveChannel<E> = _uiEvent

    // 사이드 이펙트 처리 (Channel 용량: 100)
    private val _uiSideEffect = Channel<SE>(capacity = Channel.BUFFERED)
    val uiSideEffect: ReceiveChannel<SE> = _uiSideEffect

    /**
     * 이벤트 처리 메서드 (구현 필수)
     * 각 Feature ViewModel에서 override하여 이벤트 처리 로직 구현
     */
    abstract suspend fun handleEvent(event: E)

    /**
     * 외부에서 이벤트 제출
     * UI에서 호출 (예: 버튼 클릭)
     */
    fun submitEvent(event: E) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    /**
     * 상태 업데이트
     * protected 메서드로 ViewModel 내에서만 호출 가능
     */
    protected suspend fun updateState(update: suspend (S) -> S) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * 사이드 이펙트 발생
     * protected 메서드로 ViewModel 내에서만 호출 가능
     */
    protected fun emitSideEffect(sideEffect: SE) {
        viewModelScope.launch {
            _uiSideEffect.send(sideEffect)
        }
    }

    /**
     * 초기화: 이벤트 처리 루프 시작
     */
    init {
        viewModelScope.launch {
            for (event in _uiEvent) {
                handleEvent(event)
            }
        }
    }

    override fun onCleared() {
        _uiEvent.cancel()
        _uiSideEffect.cancel()
        super.onCleared()
    }
}
```

### 4.5 Navigation Route 정의

```kotlin
/**
 * Type-safe Navigation을 위한 Serializable Route
 * Jetpack Navigation과 호환
 */
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

---

## 5. 설계 제약사항 (Design Constraints)

### 5.1 기술 스택 제약

#### DC-MVI-001: Kotlin 버전
```
Constraint: Kotlin 2.1.0 이상 호환성
Reason: Serialization 라이브러리와 Coroutines 기능 필요
Impact: 프로젝트의 kotlin 버전 확인 필수
```

#### DC-MVI-002: 의존성 주입 (DI)
```
Constraint: Hilt 의존성 주입 패턴 사용
Reason: 멀티 모듈 구조에서 의존성 관리 필요
Implementation: @HiltViewModel 어노테이션 사용
```

#### DC-MVI-003: Jetpack Compose 호환
```
Constraint: Jetpack Compose와의 자연스러운 통합
Reason: 현대적인 UI 개발 패러다임
Features:
  - StateFlow의 collectAsState() 사용
  - Compose-friendly한 이벤트 처리
```

### 5.2 구조 제약

#### DC-MVI-004: 모듈 구조
```
Constraint: :core:ui 모듈에 MVI 기본 구조 정의
Reason: 모든 Feature 모듈에서 재사용 가능해야 함
Location: :core:ui/src/main/kotlin/com/example/daitso/ui/mvi/
```

#### DC-MVI-005: 패키지 구조
```
Constraint: 일관된 패키지 구조 유지
Structure:
  :core:ui/
  ├── mvi/
  │   ├── UiState.kt
  │   ├── UiEvent.kt
  │   ├── UiSideEffect.kt
  │   └── BaseViewModel.kt
  └── navigation/
      └── Route.kt
```

### 5.3 코딩 제약

#### DC-MVI-006: 상태 불변성
```
Constraint: UiState는 data class로 정의하며 immutable 구조
Reason: 상태 변화 추적 및 테스트 용이
Implementation: copy() 메서드 활용
```

#### DC-MVI-007: 이벤트 처리 순차성
```
Constraint: 이벤트는 submitEvent() 호출 순서대로 처리
Reason: 상태 일관성 보장
Implementation: Channel의 FIFO 특성 활용
```

---

## 6. 수용 기준 (Acceptance Criteria)

### AC-MVI-001: UiState, UiEvent, UiSideEffect 인터페이스 정의

**Given**: :core:ui 모듈이 생성되어 있고, 파일 구조가 정비된 상태

**When**: UiState, UiEvent, UiSideEffect 인터페이스를 정의하고 구현할 때

**Then**:
1. 세 개의 인터페이스 모두 marker interface로 정의됨
2. 각 인터페이스는 제네릭 타입 파라미터 없이 단순 마커 역할 수행
3. sealed class/data class 상속 가능하도록 설계됨
4. 문서화: KDoc 주석으로 각 인터페이스의 목적 명시
5. 단위 테스트: 각 인터페이스를 상속한 구현체 테스트 2개 이상

**Verification**:
```kotlin
// UiState 구현 예시
sealed class TestUiState : UiState {
    data object Initial : TestUiState()
    data class Success(val data: String) : TestUiState()
}

// 테스트
@Test
fun testUiStateImplementation() {
    val state: UiState = TestUiState.Initial
    assertNotNull(state)
}
```

---

### AC-MVI-002: BaseViewModel 구현 및 테스트

**Given**: MVI 인터페이스(UiState, UiEvent, UiSideEffect)가 정의된 상태

**When**: BaseViewModel<S, E, SE> 추상 클래스를 구현하고 테스트할 때

**Then**:
1. BaseViewModel 구현:
   - StateFlow 기반 상태 관리 (_uiState, uiState)
   - Channel 기반 이벤트 처리 (_uiEvent, uiEvent)
   - Channel 기반 사이드 이펙트 처리 (_uiSideEffect, uiSideEffect)
   - submitEvent() 메서드로 외부 이벤트 제출
   - updateState() 메서드로 내부 상태 업데이트
   - emitSideEffect() 메서드로 사이드 이펙트 발생

2. 단위 테스트:
   - 이벤트 제출 테스트 (submitEvent)
   - 상태 업데이트 테스트 (updateState)
   - 사이드 이펙트 발생 테스트 (emitSideEffect)
   - 이벤트 처리 순서 테스트
   - ViewModel 소멸 시 Resource cleanup 테스트

3. 코드 커버리지: 85% 이상

**Verification**:
```kotlin
@Test
fun testEventSubmission() {
    val viewModel = TestViewModel()
    viewModel.submitEvent(TestEvent.OnClick)

    runBlocking {
        val receivedEvent = viewModel.uiEvent.receive()
        assertEquals(TestEvent.OnClick, receivedEvent)
    }
}

@Test
fun testStateUpdate() {
    val viewModel = TestViewModel()
    val states = mutableListOf<TestUiState>()

    runBlocking {
        launch {
            viewModel.uiState.collect { states.add(it) }
        }
        viewModel.submitEvent(TestEvent.OnLoad)
        delay(100)
    }

    assertEquals(2, states.size)
    assertTrue(states[1] is TestUiState.Loaded)
}
```

---

### AC-MVI-003: Navigation Route Serializable 구현

**Given**: MVI 기본 구조가 완성되고 Jetpack Navigation이 설정된 상태

**When**: Type-safe Navigation을 위해 Serializable Route를 정의할 때

**Then**:
1. Route 정의:
   - @Serializable 어노테이션 적용
   - sealed class로 모든 네비게이션 경로 정의
   - 각 Route는 data object 또는 data class로 구현
   - 네비게이션 파라미터 (itemId 등)는 생성자에 정의

2. 네비게이션 호환성:
   - Jetpack Navigation의 TypeSafeNavArgsBuilder와 호환
   - Deep link 지원 가능
   - Parcelable 변환 불필요 (Serialization으로 처리)

3. 단위 테스트:
   - Route 직렬화/역직렬화 테스트
   - 파라미터 포함 Route 생성 테스트

**Verification**:
```kotlin
@Test
fun testRouteSerializability() {
    val route: Route = Route.Detail(itemId = "123")
    val json = Json.encodeToString(route)
    val decoded = Json.decodeFromString<Route>(json)

    assertTrue(decoded is Route.Detail)
    assertEquals("123", (decoded as Route.Detail).itemId)
}
```

---

### AC-MVI-004: 테스트 커버리지 85% 이상 달성

**Given**: BaseViewModel과 MVI 인터페이스 구현이 완성된 상태

**When**: 단위 테스트를 모두 작성하고 커버리지를 측정할 때

**Then**:
1. 테스트 커버리지: 85% 이상
   - 대상: BaseViewModel, UiState/Event/SideEffect 구현체
   - 도구: Jacoco 코드 커버리지 측정
   - 보고: build/reports/jacoco/test/html/index.html에서 확인 가능

2. 테스트 시나리오 (최소 14개):
   - BaseViewModel 초기화 (1개)
   - 이벤트 제출 (2개)
   - 상태 업데이트 (2개)
   - 사이드 이펙트 발생 (2개)
   - 이벤트 처리 순서 (1개)
   - Resource cleanup (1개)
   - Navigation Route 테스트 (3개)

3. 테스트 품질:
   - Given-When-Then 형식 준수
   - 명확한 테스트 이름 (테스트 의도 명확함)
   - 독립적인 테스트 (test isolation)

**Verification**:
```bash
# 테스트 실행 및 커버리지 측정
./gradlew :core:ui:testDebugUnitTest --build-cache
./gradlew :core:ui:jacocoTestDebugUnitTestReport

# 결과 확인
open :core:ui/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html
```

---

## 7. 관련 태그 (Traceability Tags)

**FR (Functional Requirements)**:
- FR-MVI-001: UiState 인터페이스
- FR-MVI-002: UiEvent 인터페이스
- FR-MVI-003: UiSideEffect 인터페이스
- FR-MVI-004: BaseViewModel 구현
- FR-MVI-005: 단방향 데이터 흐름
- FR-MVI-006: StateFlow와 Channel 기반 구조

**NFR (Non-Functional Requirements)**:
- NFR-MVI-001: 이벤트 처리 속도
- NFR-MVI-002: 메모리 효율성
- NFR-MVI-003: 모듈 추가 용이성
- NFR-MVI-004: 상태 관리 확장성
- NFR-MVI-005: 단위 테스트 커버리지
- NFR-MVI-006: 테스트 작성 용이성

**DC (Design Constraints)**:
- DC-MVI-001: Kotlin 버전
- DC-MVI-002: 의존성 주입
- DC-MVI-003: Jetpack Compose 호환
- DC-MVI-004: 모듈 구조
- DC-MVI-005: 패키지 구조
- DC-MVI-006: 상태 불변성
- DC-MVI-007: 이벤트 처리 순차성

**AC (Acceptance Criteria)**:
- AC-MVI-001: UiState, UiEvent, UiSideEffect 인터페이스 정의
- AC-MVI-002: BaseViewModel 구현 및 테스트
- AC-MVI-003: Navigation Route Serializable 구현
- AC-MVI-004: 테스트 커버리지 85% 이상 달성

---

## 부록 (Appendix)

### A. MVI 패턴 다이어그램

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)              │
│  - 버튼 클릭, 입력 등 사용자 인터렉션       │
└──────────────────┬──────────────────────────┘
                   │
                   ↓ submitEvent(UiEvent)
┌──────────────────────────────────────────────┐
│          ViewModel Layer (MVI)                │
│  1. handleEvent() - 이벤트 처리               │
│  2. updateState() - 상태 변경                 │
│  3. emitSideEffect() - 부수 효과 발생         │
└──────┬──────────────────────┬────────────────┘
       │                      │
       ↓ StateFlow            ↓ Channel
┌──────────────────┐  ┌───────────────────┐
│   UiState        │  │   UiSideEffect    │
│  (최신 상태)     │  │  (일회성 이벤트)  │
└────────┬─────────┘  └─────────┬─────────┘
         │                      │
         ↓ collectAsState()      ↓ collect()
┌──────────────────────────────────────────────┐
│         Compose Recomposition                 │
│  UI가 새로운 상태로 다시 그려짐               │
└──────────────────────────────────────────────┘
```

### B. BaseViewModel 사용 예시

```kotlin
// 1. State 정의
sealed class HomeUiState : UiState {
    data object Initial : HomeUiState()
    data object Loading : HomeUiState()
    data class Success(val items: List<Item>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// 2. Event 정의
sealed class HomeUiEvent : UiEvent {
    data object OnLoadItems : HomeUiEvent()
    data class OnItemClicked(val itemId: String) : HomeUiEvent()
}

// 3. SideEffect 정의
sealed class HomeSideEffect : UiSideEffect {
    data class NavigateToDetail(val itemId: String) : HomeSideEffect()
    data class ShowToast(val message: String) : HomeSideEffect()
}

// 4. ViewModel 구현
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : BaseViewModel<HomeUiState, HomeUiEvent, HomeSideEffect>(
    initialState = HomeUiState.Initial
) {
    override suspend fun handleEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnLoadItems -> loadItems()
            is HomeUiEvent.OnItemClicked -> navigateToDetail(event.itemId)
        }
    }

    private suspend fun loadItems() {
        updateState { HomeUiState.Loading }
        try {
            val items = itemRepository.getItems()
            updateState { HomeUiState.Success(items) }
        } catch (e: Exception) {
            updateState { HomeUiState.Error(e.message ?: "Unknown error") }
        }
    }

    private fun navigateToDetail(itemId: String) {
        emitSideEffect(HomeSideEffect.NavigateToDetail(itemId))
    }
}

// 5. UI (Compose)에서 사용
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.submitEvent(HomeUiEvent.OnLoadItems)
    }

    when (uiState) {
        is HomeUiState.Initial -> {}
        is HomeUiState.Loading -> LoadingIndicator()
        is HomeUiState.Success -> {
            ItemList(
                items = (uiState as HomeUiState.Success).items,
                onItemClick = { itemId ->
                    viewModel.submitEvent(HomeUiEvent.OnItemClicked(itemId))
                }
            )
        }
        is HomeUiState.Error -> {
            ErrorMessage(message = (uiState as HomeUiState.Error).message)
        }
    }

    // SideEffect 처리
    LaunchedEffect(Unit) {
        for (sideEffect in viewModel.uiSideEffect) {
            when (sideEffect) {
                is HomeSideEffect.NavigateToDetail -> {
                    // Navigate to detail screen
                }
                is HomeSideEffect.ShowToast -> {
                    // Show toast message
                }
            }
        }
    }
}
```

