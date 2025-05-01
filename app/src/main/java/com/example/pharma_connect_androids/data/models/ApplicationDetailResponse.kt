package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable
 
@Serializable
data class ApplicationDetailResponse(
    val success: Boolean,
    val application: Application? // The actual application data, nullable in case success is false or data is missing
) 