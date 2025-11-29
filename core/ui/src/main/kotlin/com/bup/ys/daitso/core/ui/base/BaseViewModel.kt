package com.bup.ys.daitso.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bup.ys.daitso.core.ui.contract.UiEvent
import com.bup.ys.daitso.core.ui.contract.UiSideEffect
import com.bup.ys.daitso.core.ui.contract.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Abstract base class for MVI (Model-View-Intent) architecture ViewModels.
 *
 * This class manages the three main components of MVI:
 * - **State (S extends UiState)**: The complete UI state at any moment
 * - **Event (E extends UiEvent)**: User actions or system notifications
 * - **Side Effect (SE extends UiSideEffect)**: One-time events like navigation or toasts
 *
 * @param S The state type that implements UiState
 * @param E The event type that implements UiEvent
 * @param SE The side effect type that implements UiSideEffect
 * @param initialState The initial state when the ViewModel is created
 *
 * Example usage:
 * ```kotlin
 * class HomeViewModel : BaseViewModel<HomeState, HomeEvent, HomeSideEffect>(
 *     initialState = HomeState.Initial
 * ) {
 *     override suspend fun handleEvent(event: HomeEvent) {
 *         when (event) {
 *             HomeEvent.Load -> {
 *                 updateState(HomeState.Loading)
 *                 try {
 *                     val products = repository.getProducts()
 *                     updateState(HomeState.Success(products))
 *                 } catch (e: Exception) {
 *                     updateState(HomeState.Error(e.message ?: "Unknown error"))
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */
abstract class BaseViewModel<S : UiState, E : UiEvent, SE : UiSideEffect>(
    initialState: S
) : ViewModel() {

    // State management
    private val _uiState = MutableStateFlow(initialState)

    /**
     * Public state flow that emits state changes.
     * Collectors will receive the latest state immediately upon collection.
     */
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // Event processing
    private val _uiEvent = Channel<E>(BUFFERED)

    /**
     * Public event flow for collecting events.
     * This should typically only be used internally for event processing.
     */
    val uiEvent: Flow<E> = _uiEvent.receiveAsFlow()

    // Side effect handling
    private val _sideEffect = Channel<SE>(BUFFERED)

    /**
     * Public side effect flow for collecting one-time events.
     * Each collector will receive side effects independently.
     */
    val sideEffect: Flow<SE> = _sideEffect.receiveAsFlow()

    /**
     * Gets the current state value synchronously.
     * Useful when you need the state immediately without collecting flows.
     */
    val currentState: S
        get() = _uiState.value

    init {
        // Start processing events
        viewModelScope.launch {
            uiEvent.collect { event ->
                try {
                    handleEvent(event)
                } catch (e: Exception) {
                    // Log error and continue processing
                    // Could emit an error state here if needed
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Updates the current state asynchronously.
     * This method should be called from within [handleEvent] to change the UI state.
     *
     * @param newState The new state to emit
     */
    protected suspend fun updateState(newState: S) {
        _uiState.emit(newState)
    }

    /**
     * Submits an event for processing.
     * The event will be processed by [handleEvent] in the order it was submitted.
     *
     * @param event The event to process
     */
    suspend fun submitEvent(event: E) {
        _uiEvent.send(event)
    }

    /**
     * Launches a side effect to be observed by the UI.
     * Side effects are one-time events that don't belong in state.
     *
     * @param effect The side effect to emit
     */
    protected suspend fun launchSideEffect(effect: SE) {
        _sideEffect.send(effect)
    }

    /**
     * Handles an event and updates the state accordingly.
     * This method should be implemented by subclasses to define event handling logic.
     *
     * @param event The event to handle
     */
    abstract suspend fun handleEvent(event: E)

    /**
     * Called when the ViewModel is being destroyed.
     * Ensures proper cleanup of resources.
     */
    override fun onCleared() {
        super.onCleared()
        _uiEvent.close()
        _sideEffect.close()
    }
}
