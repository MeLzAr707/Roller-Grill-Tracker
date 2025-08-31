package com.egamerica.rollergrilltracker.data.repositories

/**
 * Exception thrown by repository layer when operations fail
 */
class RepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)