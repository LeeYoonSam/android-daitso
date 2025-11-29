package com.bup.ys.daitso.core.ui.contract

import org.junit.Test
import kotlin.test.assertIs

/**
 * Tests for UiState, UiEvent, and UiSideEffect interfaces.
 *
 * These tests verify that the MVI contract types can be properly
 * defined, implemented, and used with type parameters.
 */
class UiContractTest {

    @Test
    fun testUiStateIsInstantiable() {
        val state: UiState = object : UiState {}
        assertIs<UiState>(state)
    }

    @Test
    fun testUiEventIsInstantiable() {
        val event: UiEvent = object : UiEvent {}
        assertIs<UiEvent>(event)
    }

    @Test
    fun testUiSideEffectIsInstantiable() {
        val sideEffect: UiSideEffect = object : UiSideEffect {}
        assertIs<UiSideEffect>(sideEffect)
    }

    @Test
    fun testMultipleStateTypes() {
        val state1: UiState = object : UiState {}
        val state2: UiState = object : UiState {}
        val state3: UiState = object : UiState {}

        val states: List<UiState> = listOf(state1, state2, state3)
        assert(states.size == 3)
        states.forEach { assertIs<UiState>(it) }
    }

    @Test
    fun testMultipleEventTypes() {
        val event1: UiEvent = object : UiEvent {}
        val event2: UiEvent = object : UiEvent {}
        val event3: UiEvent = object : UiEvent {}

        val events: List<UiEvent> = listOf(event1, event2, event3)
        assert(events.size == 3)
        events.forEach { assertIs<UiEvent>(it) }
    }

    @Test
    fun testMultipleSideEffectTypes() {
        val sideEffect1: UiSideEffect = object : UiSideEffect {}
        val sideEffect2: UiSideEffect = object : UiSideEffect {}

        val sideEffects: List<UiSideEffect> = listOf(sideEffect1, sideEffect2)
        assert(sideEffects.size == 2)
        sideEffects.forEach { assertIs<UiSideEffect>(it) }
    }

    @Test
    fun testStateTypeDistinction() {
        val state: UiState = object : UiState {}
        val event: UiEvent = object : UiEvent {}
        val sideEffect: UiSideEffect = object : UiSideEffect {}

        assertIs<UiState>(state)
        assertIs<UiEvent>(event)
        assertIs<UiSideEffect>(sideEffect)
    }

    @Test
    fun testCollectingMixedTypes() {
        val states: List<UiState> = listOf(
            object : UiState {},
            object : UiState {}
        )
        val events: List<UiEvent> = listOf(
            object : UiEvent {},
            object : UiEvent {}
        )
        val sideEffects: List<UiSideEffect> = listOf(
            object : UiSideEffect {}
        )

        assert(states.size == 2)
        assert(events.size == 2)
        assert(sideEffects.size == 1)
    }
}
