package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultItem(
    val pharmacyName: String,
    val address: String,
    val photo: String?, // Can be a URL, so nullable
    val price: Double,
    val quantity: Int, // Assuming quantity is an Int, adjust if Double
    val latitude: Double?,
    val longitude: Double?,
    val pharmacyId: String,
    val inventoryId: String,
    // These fields are used in the SearchResultItemCard but might be calculated client-side
    // or added if the backend can provide them directly or if there's a plan to calculate them.
    // For now, making them nullable and optional.
    val distance: Double? = null, 
    val time: Double? = null 
) 