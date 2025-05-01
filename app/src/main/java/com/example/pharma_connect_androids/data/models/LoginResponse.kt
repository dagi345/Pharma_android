package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

// Nested data class for the actual user info within the response
@Serializable
data class LoginSuccessData(
    val userId: String,
    val role: String,
    val pharmacyId: String? = null // Make nullable as it's not always present
)

// Updated LoginResponse to match the actual API JSON structure
@Serializable
data class LoginResponse(
    val success: Boolean, // Added success flag
    val message: String,  // Added message field
    val data: LoginSuccessData // Nested data object
    // token is removed as it comes from the header
) 