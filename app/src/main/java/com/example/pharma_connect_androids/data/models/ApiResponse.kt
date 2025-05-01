package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

/**
 * Generic wrapper for API responses from the backend.
 * Adjust fields based on the actual structure used across your API.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null, // Optional message from backend
    val data: T? = null,        // Optional data payload (type T)
    val count: Int? = null      // Optional count field
) 