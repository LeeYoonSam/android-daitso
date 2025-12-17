package com.bup.ys.daitso.core.common

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Dispatcher annotation and DaitsoDispatchers enum.
 *
 * Tests cover:
 * - DaitsoDispatchers enum values
 * - Dispatcher annotation type safety
 */
class DispatcherTest {

    @Test
    fun testDispatcherValues() {
        // Assert all dispatcher values are defined
        assertTrue(DaitsoDispatchers.entries.isNotEmpty())
    }

    @Test
    fun testMainDispatcher() {
        // Assert
        assertEquals("Main", DaitsoDispatchers.Main.name)
    }

    @Test
    fun testIODispatcher() {
        // Assert
        assertEquals("IO", DaitsoDispatchers.IO.name)
    }

    @Test
    fun testDefaultDispatcher() {
        // Assert
        assertEquals("Default", DaitsoDispatchers.Default.name)
    }

    @Test
    fun testDispatcherAnnotationPresence() {
        // Arrange
        @Dispatcher(DaitsoDispatchers.Main)
        fun testFunction() {}

        // This test verifies the annotation is syntactically valid
        assertTrue(true)
    }
}
