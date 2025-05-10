package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val pharmacyName: String,
    val inventoryId: String,
    val address: String,
    val photo: String?, // Assuming it's a URL for an image, can be null
    val price: Double,
    val quantity: Int, // As per backend UserServices.js
    val latitude: Double?,
    val longitude: Double?,
    val pharmacyId: String,
    val medicineId: String,
    val medicineName: String
) 