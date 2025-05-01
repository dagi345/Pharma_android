package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.SearchRequest
import com.example.pharma_connect_androids.data.models.SearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API Service interface for search-related network calls.
 */
interface SearchApiService {

    @POST("api/v1/search") // Match the endpoint used in SearchResult.jsx
    suspend fun searchMedicine(
        @Body searchRequest: SearchRequest
    ): Response<SearchResponse>

    // Add other search-related endpoints if needed
} 