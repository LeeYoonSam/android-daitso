---
id: SPEC-ANDROID-MVI-002-ACCEPTANCE
version: 1.0.0
status: draft
created: 2025-11-29
updated: 2025-11-29
author: Albert (@user)
---

# SPEC-ANDROID-MVI-002: 수용 기준 및 테스트 계획 (Acceptance Criteria & Test Plan)

## 개요

이 문서는 SPEC-ANDROID-MVI-002의 모든 수용 기준(Acceptance Criteria)을 정의하고,
각 AC에 대한 상세한 테스트 시나리오(Given-When-Then 형식)를 제시합니다.

---

## AC-MVI-001: UiState, UiEvent, UiSideEffect 인터페이스 정의

### 요구사항

MVI 패턴의 기본이 되는 3개 마커 인터페이스를 정의하여 타입 안정성을 보장해야 합니다.

### 수용 기준

1. **인터페이스 정의**
   - [ ] UiState 인터페이스 정의
   - [ ] UiEvent 인터페이스 정의
   - [ ] UiSideEffect 인터페이스 정의

2. **마커 인터페이스 특성**
   - [ ] 메서드나 프로퍼티 없는 마커 인터페이스
   - [ ] 제네릭 타입 파라미터 없음
   - [ ] sealed class/data class 상속 가능

3. **문서화**
   - [ ] KDoc 주석으로 각 인터페이스의 목적 설명
   - [ ] 사용 예시 코드 포함
   - [ ] 필수 상속 규칙 명시

4. **테스트 커버리지**
   - [ ] 각 인터페이스별 2개 이상의 구현체 테스트
   - [ ] sealed class 상속 테스트
   - [ ] data class 상속 테스트

---

### 테스트 시나리오 (Test Scenarios)

#### TS-AC-MVI-001-001: UiState 인터페이스 구현

**Given** (전제조건):
```
- :core:ui 모듈이 준비된 상태
- UiState.kt 파일이 생성된 상태
```

**When** (동작):
```
UiState 인터페이스를 상속한 sealed class를 정의할 때
```

**Then** (검증):
```kotlin
sealed class TestUiState : UiState {
    data object Initial : TestUiState()
    data object Loading : TestUiState()
    data class Success(val data: String) : TestUiState()
    data class Error(val message: String) : TestUiState()
}

// 테스트 코드
@Test
fun testUiStateImplementation_sealedClass() {
    val state1: UiState = TestUiState.Initial
    val state2: UiState = TestUiState.Loading
    val state3: UiState = TestUiState.Success("data")
    val state4: UiState = TestUiState.Error("error")

    // 모든 구현체가 UiState 타입
    assertIsInstance<UiState>(state1)
    assertIsInstance<UiState>(state2)
    assertIsInstance<UiState>(state3)
    assertIsInstance<UiState>(state4)
}
```

---

#### TS-AC-MVI-001-002: UiState 인터페이스 with data class

**Given** (전제조건):
```
- UiState.kt 파일이 생성된 상태
```

**When** (동작):
```
UiState를 상속한 일반 data class를 정의할 때
```

**Then** (검증):
```kotlin
data class SimpleUiState(val isLoading: Boolean) : UiState

@Test
fun testUiStateImplementation_dataClass() {
    val state1 = SimpleUiState(isLoading = true)
    val state2 = SimpleUiState(isLoading = false)

    assertIsInstance<UiState>(state1)
    assertEquals(true, state1.isLoading)
    assertEquals(false, state2.isLoading)

    // copy() 메서드 동작 확인
    val state3 = state1.copy(isLoading = false)
    assertEquals(false, state3.isLoading)
}
```

---

#### TS-AC-MVI-001-003: UiEvent 인터페이스 구현

**Given** (전제조건):
```
- :core:ui 모듈이 준비된 상태
- UiEvent.kt 파일이 생성된 상태
```

**When** (동작):
```
UiEvent 인터페이스를 상속한 sealed class를 정의할 때
```

**Then** (검증):
```kotlin
sealed class TestUiEvent : UiEvent {
    data object OnLoad : TestUiEvent()
    data object OnRefresh : TestUiEvent()
    data class OnItemClicked(val itemId: String) : TestUiEvent()
}

@Test
fun testUiEventImplementation_sealedClass() {
    val event1: UiEvent = TestUiEvent.OnLoad
    val event2: UiEvent = TestUiEvent.OnRefresh
    val event3: UiEvent = TestUiEvent.OnItemClicked("123")

    assertIsInstance<UiEvent>(event1)
    assertIsInstance<UiEvent>(event2)
    assertIsInstance<UiEvent>(event3)

    // 파라미터 검증
    val clickEvent = event3 as TestUiEvent.OnItemClicked
    assertEquals("123", clickEvent.itemId)
}
```

---

#### TS-AC-MVI-001-004: UiSideEffect 인터페이스 구현

**Given** (전제조건):
```
- :core:ui 모듈이 준비된 상태
- UiSideEffect.kt 파일이 생성된 상태
```

**When** (동작):
```
UiSideEffect 인터페이스를 상속한 sealed class를 정의할 때
```

**Then** (검증):
```kotlin
sealed class TestUiSideEffect : UiSideEffect {
    data class ShowToast(val message: String) : TestUiSideEffect()
    data class NavigateTo(val route: String) : TestUiSideEffect()
}

@Test
fun testUiSideEffectImplementation_sealedClass() {
    val effect1: UiSideEffect = TestUiSideEffect.ShowToast("Success")
    val effect2: UiSideEffect = TestUiSideEffect.NavigateTo("home")

    assertIsInstance<UiSideEffect>(effect1)
    assertIsInstance<UiSideEffect>(effect2)

    // 파라미터 검증
    val toastEffect = effect1 as TestUiSideEffect.ShowToast
    assertEquals("Success", toastEffect.message)
}
```

---

#### TS-AC-MVI-001-005: 인터페이스 다형성

**Given** (전제조건):
```
- 3개 인터페이스가 모두 정의된 상태
```

**When** (동작):
```
다양한 구현체들을 인터페이스 타입으로 다룰 때
```

**Then** (검증):
```kotlin
@Test
fun testPolymorphism_interfaces() {
    val states: List<UiState> = listOf(
        TestUiState.Initial,
        TestUiState.Success("data")
    )

    val events: List<UiEvent> = listOf(
        TestUiEvent.OnLoad,
        TestUiEvent.OnItemClicked("123")
    )

    val effects: List<UiSideEffect> = listOf(
        TestUiSideEffect.ShowToast("msg"),
        TestUiSideEffect.NavigateTo("route")
    )

    assertEquals(2, states.size)
    assertEquals(2, events.size)
    assertEquals(2, effects.size)
}
```

---

## AC-MVI-002: BaseViewModel 구현 및 테스트

### 요구사항

BaseViewModel 추상 클래스를 구현하여 모든 Feature ViewModel의 기반이 되는
상태 관리, 이벤트 처리, 사이드 이펙트 발생 기능을 제공해야 합니다.

### 수용 기준

1. **StateFlow 기반 상태 관리**
   - [ ] MutableStateFlow를 private으로 선언
   - [ ] StateFlow를 public으로 노출
   - [ ] 초기 상태(initialState) 설정
   - [ ] asStateFlow()로 불변성 보장

2. **Channel 기반 이벤트 처리**
   - [ ] Event Channel 생성 (capacity = BUFFERED)
   - [ ] submitEvent() 메서드로 외부 이벤트 제출
   - [ ] handleEvent() 추상 메서드로 이벤트 처리 로직 위임
   - [ ] 이벤트 처리 루프 자동 시작

3. **Channel 기반 사이드 이펙트**
   - [ ] SideEffect Channel 생성 (capacity = BUFFERED)
   - [ ] emitSideEffect() 메서드로 부수 효과 발생
   - [ ] Channel을 통해 UI에 전달

4. **ViewModel Lifecycle 관리**
   - [ ] onCleared()에서 Channel 정리
   - [ ] viewModelScope 활용으로 Coroutine 관리
   - [ ] 메모리 누수 방지

5. **테스트 커버리지**
   - [ ] 8개 이상의 단위 테스트
   - [ ] 85% 이상 코드 커버리지
   - [ ] 모든 메서드 테스트

---

### 테스트 시나리오 (Test Scenarios)

#### TS-AC-MVI-002-001: BaseViewModel 초기화

**Given** (전제조건):
```
- BaseViewModel 추상 클래스가 구현된 상태
- TestViewModel이 BaseViewModel을 상속한 상태
```

**When** (동작):
```
BaseViewModel 인스턴스를 생성할 때
```

**Then** (검증):
```kotlin
@Test
fun testBaseViewModel_initialization() {
    val viewModel = TestViewModel()

    // 초기 상태 검증
    assertEquals(TestUiState.Initial, viewModel.uiState.value)

    // StateFlow 검증
    assertNotNull(viewModel.uiState)

    // Channel 검증
    assertNotNull(viewModel.uiEvent)
    assertNotNull(viewModel.uiSideEffect)
}

// 구현 예시
class TestViewModel : BaseViewModel<TestUiState, TestUiEvent, TestUiSideEffect>(
    initialState = TestUiState.Initial
) {
    override suspend fun handleEvent(event: TestUiEvent) {
        when (event) {
            is TestUiEvent.OnLoad -> updateState { TestUiState.Loaded }
        }
    }
}
```

---

#### TS-AC-MVI-002-002: Event 제출 및 수신

**Given** (전제조건):
```
- TestViewModel이 준비된 상태
```

**When** (동작):
```
submitEvent()를 호출하여 Event를 제출할 때
```

**Then** (검증):
```kotlin
@Test
fun testSubmitEvent_addsEventToChannel() = runTest {
    val viewModel = TestViewModel()
    val events = mutableListOf<TestUiEvent>()

    // Event 수신 시작
    launch {
        for (event in viewModel.uiEvent) {
            events.add(event)
            if (events.size == 2) break
        }
    }

    // Event 제출
    viewModel.submitEvent(TestUiEvent.OnLoad)
    viewModel.submitEvent(TestUiEvent.OnRefresh)

    advanceUntilIdle()

    // 검증
    assertEquals(2, events.size)
    assertEquals(TestUiEvent.OnLoad, events[0])
    assertEquals(TestUiEvent.OnRefresh, events[1])
}
```

---

#### TS-AC-MVI-002-003: Event 처리 및 State 업데이트

**Given** (전제조건):
```
- TestViewModel이 준비된 상태
- handleEvent()가 구현된 상태
```

**When** (동작):
```
Event를 제출하면 handleEvent()가 호출되고 State가 업데이트될 때
```

**Then** (검증):
```kotlin
@Test
fun testHandleEvent_updatesState() = runTest {
    val viewModel = TestViewModel()
    val states = mutableListOf<TestUiState>()

    // State 수집
    launch {
        viewModel.uiState.collect { states.add(it) }
    }

    // Event 제출
    viewModel.submitEvent(TestUiEvent.OnLoad)

    advanceUntilIdle()

    // 검증: Initial → Loaded
    assertEquals(2, states.size)
    assertEquals(TestUiState.Initial, states[0])
    assertEquals(TestUiState.Loaded, states[1])
}
```

---

#### TS-AC-MVI-002-004: updateState() 메서드

**Given** (전제조건):
```
- BaseViewModel이 구현된 상태
- protected updateState()가 제공되는 상태
```

**When** (동작):
```
updateState()를 호출하여 상태를 업데이트할 때
```

**Then** (검증):
```kotlin
@Test
fun testUpdateState_modifiesStateFlow() = runTest {
    val viewModel = TestViewModelWithUpdate()
    val states = mutableListOf<TestUiState>()

    launch {
        viewModel.uiState.collect { states.add(it) }
    }

    // updateState 호출
    viewModel.testUpdate()

    advanceUntilIdle()

    // 검증
    assertTrue(states.size >= 2)
    assertTrue(states.last() is TestUiState.Success)
}

class TestViewModelWithUpdate : BaseViewModel<TestUiState, TestUiEvent, TestUiSideEffect>(
    initialState = TestUiState.Initial
) {
    fun testUpdate() {
        viewModelScope.launch {
            updateState { TestUiState.Success("updated") }
        }
    }

    override suspend fun handleEvent(event: TestUiEvent) {}
}
```

---

#### TS-AC-MVI-002-005: SideEffect 발생 및 수신

**Given** (전제조건):
```
- TestViewModel이 준비된 상태
- emitSideEffect()가 구현된 상태
```

**When** (동작):
```
Event 처리 중 emitSideEffect()를 호출할 때
```

**Then** (검증):
```kotlin
@Test
fun testEmitSideEffect_sendsToChannel() = runTest {
    val viewModel = TestViewModel()
    val effects = mutableListOf<TestUiSideEffect>()

    // SideEffect 수신
    launch {
        for (effect in viewModel.uiSideEffect) {
            effects.add(effect)
            if (effects.size == 1) break
        }
    }

    // Event 제출 (내부에서 SideEffect 발생)
    viewModel.submitEvent(TestUiEvent.OnSuccess)

    advanceUntilIdle()

    // 검증
    assertEquals(1, effects.size)
    assertTrue(effects[0] is TestUiSideEffect.ShowToast)
}

class TestViewModel : BaseViewModel<TestUiState, TestUiEvent, TestUiSideEffect>(
    initialState = TestUiState.Initial
) {
    override suspend fun handleEvent(event: TestUiEvent) {
        when (event) {
            TestUiEvent.OnSuccess -> {
                updateState { TestUiState.Success }
                emitSideEffect(TestUiSideEffect.ShowToast("Success!"))
            }
            // ...
        }
    }
}
```

---

#### TS-AC-MVI-002-006: Event 처리 순서

**Given** (전제조건):
```
- TestViewModel이 준비된 상태
- 여러 Event를 빠르게 제출할 수 있는 상태
```

**When** (동작):
```
여러 Event를 동시에 제출할 때
```

**Then** (검증):
```kotlin
@Test
fun testEventProcessing_FIFO_order() = runTest {
    val viewModel = TestViewModel()
    val processedEvents = mutableListOf<TestUiEvent>()

    // Event 처리 추적
    val testViewModel = object : TestViewModel() {
        override suspend fun handleEvent(event: TestUiEvent) {
            processedEvents.add(event)
            super.handleEvent(event)
        }
    }

    // 여러 Event 제출
    testViewModel.submitEvent(TestUiEvent.OnLoad)
    testViewModel.submitEvent(TestUiEvent.OnRefresh)
    testViewModel.submitEvent(TestUiEvent.OnError)

    advanceUntilIdle()

    // FIFO 순서 검증
    assertEquals(3, processedEvents.size)
    assertEquals(TestUiEvent.OnLoad, processedEvents[0])
    assertEquals(TestUiEvent.OnRefresh, processedEvents[1])
    assertEquals(TestUiEvent.OnError, processedEvents[2])
}
```

---

#### TS-AC-MVI-002-007: 동시 Event 처리

**Given** (전제조건):
```
- TestViewModel이 준비된 상태
```

**When** (동작):
```
여러 Thread에서 동시에 submitEvent()를 호출할 때
```

**Then** (검증):
```kotlin
@Test
fun testConcurrentEventSubmission_isThreadSafe() = runTest {
    val viewModel = TestViewModel()
    val submitCount = 100

    repeat(submitCount) {
        viewModel.submitEvent(TestUiEvent.OnLoad)
    }

    // Channel이 모든 Event를 수신했는지 검증
    val receivedCount = mutableListOf<Int>()
    launch {
        receivedCount.add(0)
        for (event in viewModel.uiEvent) {
            receivedCount[0]++
            if (receivedCount[0] >= submitCount) break
        }
    }

    advanceUntilIdle()
    assertEquals(submitCount, receivedCount[0])
}
```

---

#### TS-AC-MVI-002-008: ViewModel 소멸 시 Resource Cleanup

**Given** (전제조건):
```
- TestViewModel이 생성된 상태
```

**When** (동작):
```
ViewModel이 onCleared()를 통해 소멸할 때
```

**Then** (검증):
```kotlin
@Test
fun testOnCleared_cancelsChannels() {
    val viewModel = TestViewModel()

    // Channel 상태 확인
    assertFalse(viewModel.uiEvent.isClosedForReceive)
    assertFalse(viewModel.uiSideEffect.isClosedForReceive)

    // ViewModel 소멸
    viewModel.onCleared()

    // Channel이 닫혔는지 확인
    assertTrue(viewModel.uiEvent.isClosedForReceive)
    assertTrue(viewModel.uiSideEffect.isClosedForReceive)
}
```

---

## AC-MVI-003: Navigation Route Serializable 구현

### 요구사항

Type-safe Navigation을 위해 Serializable Route를 정의하여 Jetpack Navigation과
자연스럽게 통합되고, 파라미터를 안전하게 전달할 수 있어야 합니다.

### 수용 기준

1. **Route 정의**
   - [ ] @Serializable sealed class Route 정의
   - [ ] 각 화면별 Route 정의 (Home, Detail, Settings 등)
   - [ ] 파라미터 포함 Route 정의 (data class)
   - [ ] 문서화 및 예시 포함

2. **직렬화 호환성**
   - [ ] Kotlin Serialization으로 직렬화/역직렬화 가능
   - [ ] JSON 포맷으로 변환 가능
   - [ ] Deep Link 지원 가능한 구조

3. **Jetpack Navigation 통합**
   - [ ] NavGraph에서 Route 사용 가능
   - [ ] 파라미터 전달 가능
   - [ ] 타입 안정성 보장

4. **테스트 커버리지**
   - [ ] 직렬화 테스트
   - [ ] 역직렬화 테스트
   - [ ] 파라미터 포함 Route 테스트

---

### 테스트 시나리오 (Test Scenarios)

#### TS-AC-MVI-003-001: Route 정의 및 기본 구조

**Given** (전제조건):
```
- Route.kt 파일이 생성된 상태
- Kotlin Serialization 의존성이 포함된 상태
```

**When** (동작):
```
@Serializable sealed class Route를 정의할 때
```

**Then** (검증):
```kotlin
@Serializable
sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data class Detail(val itemId: String) : Route()

    @Serializable
    data object Settings : Route()
}

@Test
fun testRouteDefinition_createsAllRoutes() {
    val homeRoute: Route = Route.Home
    val detailRoute: Route = Route.Detail(itemId = "123")
    val settingsRoute: Route = Route.Settings

    assertIsInstance<Route>(homeRoute)
    assertIsInstance<Route>(detailRoute)
    assertIsInstance<Route>(settingsRoute)
}
```

---

#### TS-AC-MVI-003-002: Route 직렬화

**Given** (전제조건):
```
- Route sealed class가 정의된 상태
- Kotlin Serialization이 설정된 상태
```

**When** (동작):
```
Route 객체를 JSON으로 직렬화할 때
```

**Then** (검증):
```kotlin
@Test
fun testRoute_serialization() {
    val route: Route = Route.Home
    val json = Json.encodeToString<Route>(route)

    assertNotNull(json)
    assertTrue(json.contains("Home"))
}

@Test
fun testRoute_serializationWithParameter() {
    val route: Route = Route.Detail(itemId = "abc123")
    val json = Json.encodeToString<Route>(route)

    assertNotNull(json)
    assertTrue(json.contains("abc123"))
}
```

---

#### TS-AC-MVI-003-003: Route 역직렬화

**Given** (전제조건):
```
- Route sealed class가 정의된 상태
```

**When** (동작):
```
JSON 문자열을 Route 객체로 역직렬화할 때
```

**Then** (검증):
```kotlin
@Test
fun testRoute_deserialization() {
    val json = """{"type":"Home"}"""
    val route = Json.decodeFromString<Route>(json)

    assertIsInstance<Route.Home>(route)
}

@Test
fun testRoute_deserializationWithParameter() {
    val originalRoute = Route.Detail(itemId = "xyz789")
    val json = Json.encodeToString<Route>(originalRoute)
    val decodedRoute = Json.decodeFromString<Route>(json)

    assertIsInstance<Route.Detail>(decodedRoute)
    val detail = decodedRoute as Route.Detail
    assertEquals("xyz789", detail.itemId)
}
```

---

#### TS-AC-MVI-003-004: Route Serialization Round Trip

**Given** (전제조건):
```
- Route sealed class가 정의된 상태
```

**When** (동작):
```
Route 객체를 직렬화했다가 역직렬화할 때
```

**Then** (검증):
```kotlin
@Test
fun testRoute_serializationRoundTrip() {
    val originalRoute = Route.Detail(itemId = "test-id-123")

    // 직렬화
    val json = Json.encodeToString<Route>(originalRoute)

    // 역직렬화
    val decodedRoute = Json.decodeFromString<Route>(json)

    // 원본과 동일한지 확인
    assertEquals(originalRoute, decodedRoute)
}
```

---

## AC-MVI-004: 테스트 커버리지 85% 이상 달성

### 요구사항

BaseViewModel과 MVI 인터페이스의 모든 기능에 대해 85% 이상의 코드 커버리지를
달성하고, 최소 14개 이상의 단위 테스트를 성공시켜야 합니다.

### 수용 기준

1. **코드 커버리지**
   - [ ] 전체 커버리지: 85% 이상
   - [ ] Jacoco 리포트 생성
   - [ ] build/reports/jacoco/test/html/index.html에서 확인 가능

2. **테스트 케이스 (최소 14개)**
   - [ ] AC-MVI-001 테스트 (5개)
     - UiState sealed class
     - UiState data class
     - UiEvent sealed class
     - UiSideEffect sealed class
     - 인터페이스 다형성

   - [ ] AC-MVI-002 테스트 (8개)
     - BaseViewModel 초기화
     - Event 제출 및 수신
     - Event 처리 및 State 업데이트
     - updateState() 메서드
     - SideEffect 발생 및 수신
     - Event 처리 순서
     - 동시 Event 처리
     - Resource Cleanup

   - [ ] AC-MVI-003 테스트 (3개 이상)
     - Route 정의
     - Route 직렬화
     - Route 역직렬화/Round Trip

3. **테스트 품질**
   - [ ] Given-When-Then 형식 준수
   - [ ] 명확한 테스트 이름 (테스트 의도 명확)
   - [ ] 독립적인 테스트 (test isolation)
   - [ ] 재현 가능성 (deterministic)

---

### 테스트 계획 (Test Plan)

#### Phase 1: 단위 테스트 작성

**시간**: ~3-4시간

**작성 순서**:
1. UiState 인터페이스 테스트 (UiStateTest.kt)
   ```
   - testUiStateImplementation_sealedClass
   - testUiStateImplementation_dataClass
   ```

2. UiEvent 인터페이스 테스트 (UiEventTest.kt)
   ```
   - testUiEventImplementation_sealedClass
   - testUiEventImplementation_polymorphism
   ```

3. UiSideEffect 인터페이스 테스트 (UiSideEffectTest.kt)
   ```
   - testUiSideEffectImplementation_sealedClass
   - testUiSideEffectImplementation_dataClass
   ```

4. BaseViewModel 테스트 (BaseViewModelTest.kt)
   ```
   - testBaseViewModel_initialization
   - testSubmitEvent_addsEventToChannel
   - testHandleEvent_updatesState
   - testUpdateState_modifiesStateFlow
   - testEmitSideEffect_sendsToChannel
   - testEventProcessing_FIFO_order
   - testConcurrentEventSubmission_isThreadSafe
   - testOnCleared_cancelsChannels
   ```

5. Route 테스트 (RouteTest.kt)
   ```
   - testRouteDefinition_createsAllRoutes
   - testRoute_serialization
   - testRoute_deserialization
   - testRoute_serializationRoundTrip
   ```

#### Phase 2: 커버리지 측정

**명령어**:
```bash
# 테스트 실행
./gradlew :core:ui:testDebugUnitTest --build-cache

# 커버리지 리포트 생성
./gradlew :core:ui:jacocoTestDebugUnitTestReport

# 결과 확인
open :core:ui/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html
```

**목표 커버리지**:
```
- BaseViewModel: 90%+
- UiState/Event/SideEffect: 95%+
- 전체: 85%+
```

#### Phase 3: 커버리지 개선 (필요시)

**커버리지 부족 시 대응**:
1. 누락된 코드 경로 확인
2. 추가 테스트 케이스 작성
3. 엣지 케이스 테스트 추가

---

### 테스트 실행 체크리스트

**Pre-Test**:
- [ ] 모든 테스트 파일이 src/test/kotlin에 위치
- [ ] 테스트 이름이 명확하고 descriptive
- [ ] import 문 정리 (unused imports 제거)

**Test Execution**:
- [ ] 모든 테스트 실행: `./gradlew :core:ui:testDebugUnitTest`
- [ ] 테스트 결과: 전부 PASS
- [ ] 테스트 실행 시간: < 30초 (권장)

**Post-Test**:
- [ ] 커버리지 리포트 생성: `./gradlew :core:ui:jacocoTestDebugUnitTestReport`
- [ ] 커버리지 85% 이상 확인
- [ ] 리포트 저장: 버전 관리

**Verification**:
- [ ] 테스트 통과율: 100%
- [ ] 커버리지: 85%+
- [ ] 성능: 이벤트 처리 < 100ms

---

### 테스트 데이터 (Test Fixtures)

모든 테스트에서 사용할 공통 Test Double을 정의합니다.

```kotlin
// TestData.kt
sealed class TestUiState : UiState {
    data object Initial : TestUiState()
    data object Loading : TestUiState()
    data class Success(val data: String) : TestUiState()
    data class Error(val message: String) : TestUiState()
}

sealed class TestUiEvent : UiEvent {
    data object OnLoad : TestUiEvent()
    data object OnRefresh : TestUiEvent()
    data class OnItemClicked(val itemId: String) : TestUiEvent()
    data object OnSuccess : TestUiEvent()
    data object OnError : TestUiEvent()
}

sealed class TestUiSideEffect : UiSideEffect {
    data class ShowToast(val message: String) : TestUiSideEffect()
    data class NavigateTo(val route: String) : TestUiSideEffect()
}

class TestViewModel : BaseViewModel<TestUiState, TestUiEvent, TestUiSideEffect>(
    initialState = TestUiState.Initial
) {
    override suspend fun handleEvent(event: TestUiEvent) {
        when (event) {
            TestUiEvent.OnLoad -> {
                updateState { TestUiState.Loading }
                // 시뮬레이션 (실제로는 비동기 작업)
                updateState { TestUiState.Success("loaded data") }
            }
            TestUiEvent.OnRefresh -> {
                updateState { TestUiState.Loading }
                updateState { TestUiState.Success("refreshed") }
            }
            is TestUiEvent.OnItemClicked -> {
                emitSideEffect(TestUiSideEffect.NavigateTo("detail/${event.itemId}"))
            }
            TestUiEvent.OnSuccess -> {
                updateState { TestUiState.Success("success") }
                emitSideEffect(TestUiSideEffect.ShowToast("Success!"))
            }
            TestUiEvent.OnError -> {
                updateState { TestUiState.Error("error occurred") }
                emitSideEffect(TestUiSideEffect.ShowToast("Error!"))
            }
        }
    }
}
```

---

## 통합 검증 (Integration Verification)

### MVI 패턴의 전체 흐름

```
┌─────────────────────────────────────────┐
│  Verify MVI Pattern Flow                  │
└─────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────┐
│ 1. Event 제출 (submitEvent)               │
│    - Event Channel에 추가됨                │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│ 2. Event 처리 (handleEvent)               │
│    - ViewModel에서 비즈니스 로직 처리     │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│ 3. State 업데이트 (updateState)           │
│    - StateFlow 갱신                       │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│ 4. UI 반영 (collectAsState)                │
│    - Compose UI 리컴포지션                │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│ 5. SideEffect 발생 (필요시)                │
│    - Toast, Dialog, Navigation 등        │
└─────────────────────────────────────────┘
```

### 전체 흐름 검증 테스트

```kotlin
@Test
fun testMVIPattern_completeFlow() = runTest {
    val viewModel = TestViewModel()
    val states = mutableListOf<TestUiState>()
    val effects = mutableListOf<TestUiSideEffect>()

    // State 수집 시작
    launch {
        viewModel.uiState.collect { states.add(it) }
    }

    // SideEffect 수집 시작
    launch {
        for (effect in viewModel.uiSideEffect) {
            effects.add(effect)
        }
    }

    // Event 제출
    viewModel.submitEvent(TestUiEvent.OnLoad)
    advanceUntilIdle()

    // 검증
    assertEquals(3, states.size) // Initial, Loading, Success
    assertTrue(states[0] is TestUiState.Initial)
    assertTrue(states[1] is TestUiState.Loading)
    assertTrue(states[2] is TestUiState.Success)
}
```

---

## 최종 검증 체크리스트 (Final Verification Checklist)

**AC-MVI-001**:
- [ ] UiState 인터페이스 정의 및 KDoc 작성
- [ ] UiEvent 인터페이스 정의 및 KDoc 작성
- [ ] UiSideEffect 인터페이스 정의 및 KDoc 작성
- [ ] 5개 이상의 단위 테스트 작성 및 통과

**AC-MVI-002**:
- [ ] BaseViewModel 추상 클래스 완전 구현
- [ ] StateFlow 기반 상태 관리
- [ ] Channel 기반 이벤트/사이드 이펙트 처리
- [ ] 8개 이상의 단위 테스트 작성 및 통과
- [ ] 코드 커버리지 85%+ 달성

**AC-MVI-003**:
- [ ] Route sealed class 정의
- [ ] @Serializable 어노테이션 적용
- [ ] 직렬화/역직렬화 테스트 통과
- [ ] Navigation 통합 가능성 확인

**AC-MVI-004**:
- [ ] 14개 이상의 단위 테스트 작성
- [ ] 모든 테스트 통과 (100% pass rate)
- [ ] 코드 커버리지 85%+ 달성
- [ ] Jacoco 리포트 생성 및 저장

**최종 검증**:
- [ ] 모든 AC 통과
- [ ] 모든 테스트 통과 (14개+)
- [ ] 코드 커버리지 85%+
- [ ] 성능 메트릭 달성 (이벤트 처리 < 100ms)
- [ ] 문서화 완성
- [ ] 코드 검토 완료 (Code Review)
- [ ] Git commit 및 PR 제출

