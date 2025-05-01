package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

// Data class for the Register API response body
@Serializable // Add annotation for Kotlinx Serialization
data class RegisterResponse(
    val message: String, // e.g., "Registration successful"
    val userId: String   // ID of the newly created user
) 