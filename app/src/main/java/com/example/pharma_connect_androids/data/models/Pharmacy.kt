package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pharmacy(
    @SerialName("_id")
    val id: String,
    val name: String,
    val address: String, // Full address string
    val city: String? = null,
    val state: String? = null, // Added state
    val zipcode: String? = null, // Added zipcode
    val contactNumber: String,
    val email: String,
    // val status: String, // Status was removed previously
    val latitude: Double? = null,
    val longitude: Double? = null,
    // Add other fields like operatingHours, pharmacistInChargeId based on actual backend response if available
    val pharmacistInChargeId: String? = null, // From JoinPharmacyScreen logic
    val ownerId: String? = null, // From JoinPharmacyScreen logic
    // New fields assuming backend provides them:
    @SerialName("ownerName") // Assuming backend provides ownerName based on ownerId
    val ownerName: String? = null,
    @SerialName("licenseNumber") // Assuming backend provides licenseNumber
    val licenseNumber: String? = null,
    @SerialName("image") // Assuming backend provides an image URL
    val image: String? = null
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