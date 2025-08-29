package com.egamerica.rollergrilltracker.data

/**
 * Exception class for repository-layer errors.
 * Used to wrap and provide meaningful error messages for database and repository operations.
 */
class RepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)