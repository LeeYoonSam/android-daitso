package com.bup.ys.daitso.core.common

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Dispatcher annotation.
 *
 * Tests cover:
 * - Dispatcher enum values
 * - Dispatcher type safety
 */
class DispatcherTest {

    @Test
    fun testDispatcherValues() {
        // Assert all dispatcher values are defined
        assertTrue(Dispatcher.values().isNotEmpty())
    }

    @Test
    fun testMainDispatcher() {
        // Assert
        assertEquals("Main", Dispatcher.Main.name)
    }

    @Test
    fun testIODispatcher() {
        // Assert
        assertEquals("IO", Dispatcher.IO.name)
    }

    @Test
    fun testDefaultDispatcher() {
        // Assert
        assertEquals("Default", Dispatcher.Default.name)
    }

    @Test
    fun testDispatcherAnnotationPresence() {
        // Arrange
        @DispatcherType(Dispatcher.Main)
        fun testFunction() {}

        // This test verifies the annotation is syntactically valid
        assertTrue(true)
    }
}
