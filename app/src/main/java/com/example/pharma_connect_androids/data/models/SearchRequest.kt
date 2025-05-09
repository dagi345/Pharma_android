package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val medicineName: String
) 