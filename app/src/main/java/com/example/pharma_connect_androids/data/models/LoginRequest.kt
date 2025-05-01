package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

// Data class for the Login API request body
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
) 