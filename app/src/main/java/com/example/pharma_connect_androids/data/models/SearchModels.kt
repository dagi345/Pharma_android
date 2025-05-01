package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val medicineName: String
    // Add location fields (lat, lng) if backend search uses them
)

@Serializable
data class SearchResponse(
    val data: List<SearchResultItem> // Assuming backend wraps results in a 'data' field
    // Add other fields from backend response if needed (e.g., pagination)
)

@Serializable
data class SearchResultItem(
    val pharmacyName: String,
    val address: String,
    val price: Double, // Assuming price is a number
    val distance: Double? = null, // Made nullable as it depends on location
    val time: Double? = null,     // Made nullable as it depends on location
    val photo: String? = null, // URL or identifier for the image - Nullable placeholder
    val pharmacyId: String,
    val inventoryId: String
    // val medicineName: String? = null // Usually the searched medicine is known contextually
) 