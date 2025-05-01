package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.PharmacyApplicationRequest
import com.example.pharma_connect_androids.data.models.PharmacyApplicationResponse
import com.example.pharma_connect_androids.data.models.Application
import com.example.pharma_connect_androids.data.models.UpdateApplicationStatusRequest
import com.example.pharma_connect_androids.data.models.GetApplicationsResponse
import com.example.pharma_connect_androids.data.models.ApplicationDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/**
 * Retrofit API Service interface for pharmacy application related network calls.
 */
interface ApplicationApiService {

    // Endpoint based on frontend fetch call
    @POST("api/v1/applications/createApplication") 
    suspend fun submitApplication(
        @Body applicationRequest: PharmacyApplicationRequest
    ): Response<PharmacyApplicationResponse>

    // Endpoint to get all applications (for Admin)
    @GET("api/v1/applications")
    suspend fun getApplications(): Response<GetApplicationsResponse>

    // Endpoint to update application status (for Admin)
    @PATCH("api/v1/applications/{id}/status")
    suspend fun updateApplicationStatus(
        @Path("id") applicationId: String,
        @Body statusRequest: UpdateApplicationStatusRequest
    ): Response<Unit>

    // Endpoint to get single application by ID (for Admin detail view)
    @GET("api/v1/applications/{id}")
    suspend fun getApplicationById(
        @Path("id") applicationId: String
    ): Response<ApplicationDetailResponse>
    // Note: Adjust if backend wraps the object (e.g., {"success": true, "application": {...}})

    // Add other application-related endpoints if needed (e.g., get applications)
} 