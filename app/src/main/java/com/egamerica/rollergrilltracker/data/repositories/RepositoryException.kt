package com.egamerica.rollergrilltracker.data.repositories

/**
 * Exception thrown by repository classes when data operations fail.
 * This provides a consistent way to handle repository-level errors
 * while preserving the underlying cause for debugging.
 */
class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)