package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable
import com.example.pharma_connect_androids.util.LocalDateSerializer // Assuming a serializer for LocalDate exists
import java.time.LocalDate

@Serializable
data class AddMedicineInventoryRequest(
    val medicineId: String,
    val quantity: Int,
    val price: Double, // Use Double for price
    @Serializable(with = LocalDateSerializer::class) // Serialize LocalDate to String (e.g., YYYY-MM-DD)
    val expiryDate: LocalDate, 
    val updatedBy: String // User ID of the owner
) 