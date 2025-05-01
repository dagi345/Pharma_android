package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Pharmacist(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String // Should always be 'pharmacist' here
) 