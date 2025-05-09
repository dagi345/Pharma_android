package com.example.pharma_connect_androids.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val success: Boolean,
    val count: Int,
    val data: List<SearchResultItem> // Using the SearchResultItem we defined
) 