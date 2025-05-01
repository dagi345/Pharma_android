package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

// Data class for the Pharmacist Register API request body
@Serializable
data class PharmacistRegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    // confirmPassword is often handled in ViewModel/UI, but send if backend requires it
    // Let's assume backend doesn't need confirmPassword based on UserService.signUp
    // val confirmPassword: String,
    val role: String = "pharmacist", // Explicitly set role
    val pharmacyId: String          // Required for pharmacists
) 