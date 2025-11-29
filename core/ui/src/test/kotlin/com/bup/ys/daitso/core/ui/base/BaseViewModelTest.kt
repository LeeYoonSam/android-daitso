package com.bup.ys.daitso.core.ui.base

import app.cash.turbine.test
import com.bup.ys.daitso.core.ui.contract.UiEvent
import com.bup.ys.daitso.core.ui.contract.UiSideEffect
import com.bup.ys.daitso.core.ui.contract.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for BaseViewModel MVI implementation.
 *
 * These tests verify that the BaseViewModel properly manages state,
 * events, and side effects according to MVI principles.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    // Test state implementations
    private data class TestStateInitial(val dummy: Int = 0) : UiState
    private data class TestStateLoading(val dummy: Int = 0) : UiState
    private data class TestStateSuccess(val data: String) : UiState
    private data class TestStateError(val message: String) : UiState

    // Test event implementations
    private data class TestEventLoad(val dummy: Int = 0) : UiEvent
    private data class TestEventUpdateData(val data: String) : UiEvent
    private data class TestEventRetry(val dummy: Int = 0) : UiEvent

    // Test side effect implementations
    private data class TestSideEffectShowMessage(val text: String) : UiSideEffect
    private data class TestSideEffectNavigateHome(val dummy: Int = 0) : UiSideEffect

    private inner class TestViewModel : BaseViewModel<UiState, UiEvent, UiSideEffect>(
        initialState = TestStateInitial()
    ) {
        override suspend fun handleEvent(event: UiEvent) {
            when (event) {
                is TestEventLoad -> {
                    updateState(TestStateLoading())
                    updateState(TestStateSuccess("Loaded"))
                    launchSideEffect(TestSideEffectShowMessage("Loaded successfully"))
                }

                is TestEventUpdateData -> {
                    updateState(TestStateSuccess(event.data))
                }

                is TestEventRetry -> {
                    updateState(TestStateLoading())
                    updateState(TestStateError("Retry failed"))
                    launchSideEffect(TestSideEffectShowMessage("Retry failed"))
                }
            }
        }
    }

    private lateinit var viewModel: TestViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TestViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertIs<TestStateInitial>(viewModel.currentState)
    }

    @Test
    fun testStateFlowEmitsInitialState() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertIs<TestStateInitial>(initial)
            cancel()
        }
    }

    @Test
    fun testStateUpdateEmitsNewState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.submitEvent(TestEventLoad())
            val loading = awaitItem()
            assertIs<TestStateLoading>(loading)
            val success = awaitItem()
            assertIs<TestStateSuccess>(success)
            cancel()
        }
    }

    @Test
    fun testEventProcessing() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.submitEvent(TestEventUpdateData("new data"))
            val state = awaitItem()
            assertIs<TestStateSuccess>(state)
            assertEquals("new data", (state as TestStateSuccess).data)
            cancel()
        }
    }

    @Test
    fun testMultipleEventProcessing() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.submitEvent(TestEventLoad())
            awaitItem() // Loading
            awaitItem() // Success

            viewModel.submitEvent(TestEventUpdateData("updated"))
            val updated = awaitItem()
            assertIs<TestStateSuccess>(updated)
            assertEquals("updated", (updated as TestStateSuccess).data)
            cancel()
        }
    }

    @Test
    fun testSideEffectEmission() = runTest(testDispatcher) {
        viewModel.sideEffect.test {
            viewModel.submitEvent(TestEventLoad())
            val sideEffect = awaitItem()
            assertIs<TestSideEffectShowMessage>(sideEffect)
            cancel()
        }
    }

    @Test
    fun testErrorStateAndSideEffect() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.submitEvent(TestEventRetry())
            awaitItem() // Loading
            val error = awaitItem()
            assertIs<TestStateError>(error)
            cancel()
        }
    }

    @Test
    fun testSideEffectOnError() = runTest(testDispatcher) {
        viewModel.sideEffect.test {
            viewModel.submitEvent(TestEventRetry())
            val sideEffect = awaitItem()
            assertIs<TestSideEffectShowMessage>(sideEffect)
            cancel()
        }
    }

    @Test
    fun testEventOrderPreserved() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial

            // Submit multiple events
            viewModel.submitEvent(TestEventLoad())
            viewModel.submitEvent(TestEventUpdateData("first"))

            // Wait for events to be processed
            awaitItem() // Loading
            awaitItem() // Success from Load
            awaitItem() // Success with "first"

            cancel()
        }
    }

    @Test
    fun testCurrentStateReturnsLatestState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.submitEvent(TestEventLoad())
            awaitItem() // Loading
            awaitItem() // Success
            assertIs<TestStateSuccess>(viewModel.currentState)
            cancel()
        }
    }

    @Test
    fun testStateFlowRetainsLatestValue() = runTest(testDispatcher) {
        // Emit state changes
        viewModel.submitEvent(TestEventLoad())
        // Allow the dispatcher to process
        testDispatcher.scheduler.advanceUntilIdle()

        // New collector should get the latest state
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<TestStateSuccess>(state)
            cancel()
        }
    }
}
