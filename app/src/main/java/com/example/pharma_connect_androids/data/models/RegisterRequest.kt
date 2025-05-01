package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

// Data class for the Register API request body (Normal User)
@Serializable
data class RegisterRequest(
    val firstName: String,     // Changed from fullName
    val lastName: String,      // Added
    val email: String,
    val password: String,
    val confirmPassword: String,
    // confirmPassword is usually handled in ViewModel/UI, not sent in request body
    // role is omitted (defaults to 'user' on backend)
    // pharmacyId is omitted
) 