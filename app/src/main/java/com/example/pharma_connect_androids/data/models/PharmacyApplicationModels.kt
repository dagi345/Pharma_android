package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

// Request body for submitting a pharmacy application
@Serializable
data class PharmacyApplicationRequest(
    val ownerName: String,
    val pharmacyName: String,
    val contactNumber: String,
    val email: String,
    val address: String, // Street/Subcity
    val city: String,
    val state: String,
    val zipCode: String,
    val latitude: Double, // Placeholder value will be used for now
    val longitude: Double, // Placeholder value will be used for now
    val licenseNumber: String,
    val licenseImage: String, // Placeholder URL will be used for now
    val pharmacyImage: String, // Placeholder URL will be used for now
    val ownerId: String // ID of the user submitting the application
)

// Response for application submission - matches the actual backend response
@Serializable
data class PharmacyApplicationResponse(
    val success: Boolean,
    // The backend sends the created application details nested under this key
    val createdApplication: Application? = null // Make nullable if it might be absent on error?
    // The 'message' field is not present in the successful response according to logs
    // Add a general message field if the backend sends one for errors
    // val message: String? = null
)

// Data class representing a single Application (for Admin view AND create response)
// Ensure this matches the fields within "createdApplication" object in the logs
@Serializable
data class Application(
    val _id: String, // Use _id to match MongoDB
    val ownerName: String,
    val pharmacyName: String,
    val contactNumber: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    val licenseNumber: String,
    val licenseImage: String,
    val pharmacyImage: String,
    val ownerId: String,
    val status: String, // e.g., "Pending", "Approved", "Rejected"
    val createdAt: String, // Assuming backend sends date as String (e.g., ISO format)
    val pharmacyId: String? = null, // Add pharmacyId (nullable based on logs)
    val updatedAt: String? = null, // Add updatedAt (nullable)
    val __v: Int? = null          // Add __v (nullable)
    // Add any other relevant fields returned by the backend /applications GET endpoint
)

// Wrapper class for the GET /applications response
@Serializable
data class GetApplicationsResponse(
    val success: Boolean,
    val applications: List<Application>
)

// Data class for updating application status
@Serializable
data class UpdateApplicationStatusRequest(
    val status: String // "Approved" or "Rejected"
) 