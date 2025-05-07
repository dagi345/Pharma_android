package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.Pharmacist
import com.example.pharma_connect_androids.data.models.ApiResponse
import com.example.pharma_connect_androids.data.models.PharmacyListResponse
import com.example.pharma_connect_androids.data.models.SinglePharmacyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.DELETE

interface PharmacyApiService {

    // Get pharmacists for a specific pharmacy
    @GET("pharmacies/{id}/pharmacists")
    suspend fun getPharmacists(
        @Header("Authorization") token: String, // Pass JWT token
        @Path("id") pharmacyId: String
    ): Response<ApiResponse<List<Pharmacist>>>

    @GET("api/v1/pharmacies") // Fetch all pharmacies (admin)
    suspend fun getAllPharmacies(): Response<PharmacyListResponse>

    @GET("api/v1/pharmacies/{id}") // Fetch a single pharmacy by ID (admin)
    suspend fun getPharmacyById(@Path("id") pharmacyId: String): Response<SinglePharmacyResponse>

    @DELETE("api/v1/pharmacies/{id}") // Delete a pharmacy by ID (admin)
    suspend fun deletePharmacy(@Path("id") pharmacyId: String): Response<Unit> // Assuming no body in response
} 