package com.example.pharma_connect_androids.data.models

// Remove Gson import
// import com.google.gson.annotations.SerializedName

import kotlinx.serialization.SerialName // Import for kotlinx.serialization
import kotlinx.serialization.Serializable // Import for kotlinx.serialization

@Serializable // Add @Serializable for kotlinx.serialization
data class AddMedicineRequest(
    @SerialName("name") // Use kotlinx.serialization.SerialName
    val name: String,
    @SerialName("description") // Use kotlinx.serialization.SerialName
    val description: String,
    @SerialName("image") // Use kotlinx.serialization.SerialName
    val image: String, // URL of the image
    @SerialName("category") // Use kotlinx.serialization.SerialName
    val category: String
) 