package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePharmacyRequest(
    val ownerName: String,
    val name: String,
    val contactNumber: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    val licenseNumber: String,
    // Assuming URLs are sent after upload for images, similar to registration
    val licenseImage: String, 
    val pharmacyImage: String, 
    val ownerId: String? = null // May not be strictly needed for update, but included for consistency
) 