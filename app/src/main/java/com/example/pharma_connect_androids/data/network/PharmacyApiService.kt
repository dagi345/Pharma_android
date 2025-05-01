package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.Pharmacist
import com.example.pharma_connect_androids.data.models.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface PharmacyApiService {

    // Get pharmacists for a specific pharmacy
    @GET("pharmacies/{id}/pharmacists")
    suspend fun getPharmacists(
        @Header("Authorization") token: String, // Pass JWT token
        @Path("id") pharmacyId: String
    ): Response<ApiResponse<List<Pharmacist>>>
} 