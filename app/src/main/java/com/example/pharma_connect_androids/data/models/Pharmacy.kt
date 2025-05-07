package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pharmacy(
    @SerialName("_id")
    val id: String,
    val name: String,
    val address: String, // Assuming a simple string for now, can be a nested object if backend provides more structure
    val city: String? = null, // Optional
    val contactNumber: String,
    val email: String,
    // val status: String, // Removed status field
    val latitude: Double? = null,
    val longitude: Double? = null,
    // Add other fields like operatingHours, pharmacistInChargeId based on actual backend response if available
    val pharmacistInChargeId: String? = null, // From JoinPharmacyScreen logic
    val ownerId: String? = null // From JoinPharmacyScreen logic
)

@Serializable
data class PharmacyListResponse(
    @SerialName("data") // Assuming the list is wrapped in a "data" field
    val data: List<Pharmacy>
    // Potentially other fields like count, success, message
)

@Serializable
data class SinglePharmacyResponse(
    @SerialName("data") // Assuming single pharmacy is also wrapped in "data"
    val data: Pharmacy
    // Potentially other fields
) 