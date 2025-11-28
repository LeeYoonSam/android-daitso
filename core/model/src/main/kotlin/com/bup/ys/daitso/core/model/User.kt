package com.bup.ys.daitso.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model for a User.
 *
 * @param id Unique identifier for the user
 * @param name User's full name
 * @param email User's email address
 */
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String
)
