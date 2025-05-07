package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.pharma_connect_androids.util.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class InventoryItem(
    @SerialName("_id")
    val id: String,
    @SerialName("pharmacy") // Assuming backend field name is pharmacy
    val pharmacyId: String,
    @SerialName("medicine") // Assuming backend field name is medicine (ID)
    val medicineId: String,
    val medicineName: String, // Likely populated by backend
    val category: String? = null, // Make nullable and provide default
    var quantity: Int, // Mutable var if we update locally before backend confirmation
    val price: Double,
    @Serializable(with = LocalDateSerializer::class)
    val expiryDate: LocalDate
    // Add other fields if the backend provides more (e.g., batch number)
)

@Serializable
data class InventoryListResponse(
    @SerialName("data") // Assuming list is wrapped in "data"
    val data: List<InventoryItem>? // Make list nullable to handle empty case gracefully
)

@Serializable
data class SingleInventoryItemResponse(
    @SerialName("data")
    val data: InventoryItem
)

@Serializable
data class UpdateInventoryItemRequest(
    // Fields that can be updated via the full update form
    val quantity: Int,
    val price: Double,
    @Serializable(with = LocalDateSerializer::class)
    val expiryDate: LocalDate,
    val medicineId: String? = null // Add medicineId (nullable if backend handles missing? check if required)
    // updatedBy might also be needed here, TBC based on backend API spec
) 