package com.bup.ys.daitso.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for User data class.
 *
 * Tests cover:
 * - User serialization and deserialization
 * - User equality
 * - User data validation
 */
class UserTest {

    @Test
    fun testUserSerialization() {
        // Arrange
        val user = User(
            id = "user-1",
            name = "John Doe",
            email = "john@example.com"
        )

        // Act
        val json = Json.encodeToString(user)

        // Assert
        assertNotNull(json)
        assert(json.contains("\"id\":\"user-1\""))
        assert(json.contains("\"name\":\"John Doe\""))
        assert(json.contains("\"email\":\"john@example.com\""))
    }

    @Test
    fun testUserDeserialization() {
        // Arrange
        val jsonString = """
            {
                "id": "user-1",
                "name": "John Doe",
                "email": "john@example.com"
            }
        """.trimIndent()

        // Act
        val user = Json.decodeFromString<User>(jsonString)

        // Assert
        assertEquals("user-1", user.id)
        assertEquals("John Doe", user.name)
        assertEquals("john@example.com", user.email)
    }

    @Test
    fun testUserEquality() {
        // Arrange
        val user1 = User(
            id = "user-1",
            name = "John Doe",
            email = "john@example.com"
        )
        val user2 = User(
            id = "user-1",
            name = "John Doe",
            email = "john@example.com"
        )

        // Assert
        assertEquals(user1, user2)
    }

    @Test
    fun testUserDataClass() {
        // Arrange & Act
        val user = User(
            id = "user-123",
            name = "Jane Smith",
            email = "jane@example.com"
        )

        // Assert
        assertEquals("user-123", user.id)
        assertEquals("Jane Smith", user.name)
        assertEquals("jane@example.com", user.email)
    }
}
