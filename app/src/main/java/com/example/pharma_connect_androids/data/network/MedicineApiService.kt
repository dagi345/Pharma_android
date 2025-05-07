package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.AddMedicineRequest
import com.example.pharma_connect_androids.data.models.MedicineListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MedicineApiService {
    @POST("api/v1/medicines") // Changed to api/v1/medicines for consistency
    suspend fun addMedicine(
        @Body addMedicineRequest: AddMedicineRequest
    ): Response<Unit> // Assuming backend returns 201 Created with no specific body on success, or an error response

    @GET("api/v1/medicines") // This was already changed by you
    suspend fun getAllMedicines(): Response<MedicineListResponse>
} 