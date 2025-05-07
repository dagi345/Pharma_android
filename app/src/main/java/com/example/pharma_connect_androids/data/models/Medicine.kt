package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Medicine(
    @SerialName("_id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("category")
    val category: String,
    @SerialName("description")
    val description: String,
    @SerialName("image") // Assuming image URL is also part of the medicine data
    val image: String? = null // Make it nullable if it might be missing
)

@Serializable
data class MedicineListResponse(
    @SerialName("data")
    val data: List<Medicine>,
    // Add other fields from the response if necessary, e.g., success, message, count
    // For now, focusing on the data array as per web frontend observation
) 