package com.example.pharma_connect_androids.domain.model

import kotlinx.serialization.Serializable

// Represents user data stored locally (e.g., in SessionManager)
// May overlap with LoginResponse but represents the core user identity
@Serializable // Add annotation for Kotlinx Serialization
data class UserData(
    val userId: String,
    val role: String,
    val pharmacyId: String?, // Nullable
    val firstName: String?,  // Added
    val lastName: String?   // Added
) 