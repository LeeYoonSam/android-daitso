package com.bup.ys.daitso.core.common

/**
 * A sealed class representing the result of an asynchronous operation.
 *
 * The Result type can be in one of three states:
 * - Success: Contains the successful result data
 * - Error: Contains the exception that occurred
 * - Loading: Indicates the operation is in progress
 *
 * @param T The type of data in the Success state
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with result data.
     *
     * @param data The result data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with an exception.
     *
     * @param exception The exception that caused the failure
     */
    data class Error(val exception: Throwable) : Result<Nothing>()

    /**
     * Represents an operation in progress.
     */
    data class Loading<T>(val data: T? = null) : Result<T>()

    /**
     * Returns the data if this is a Success, null otherwise.
     *
     * @return The data if Success, null for Error or Loading
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> data
    }

    /**
     * Returns true if this is a Success result.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if this is an Error result.
     */
    fun isError(): Boolean = this is Error

    /**
     * Returns true if this is a Loading result.
     */
    fun isLoading(): Boolean = this is Loading
}
