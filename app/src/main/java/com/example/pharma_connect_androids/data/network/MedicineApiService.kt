package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.AddMedicineRequest
import com.example.pharma_connect_androids.data.models.MedicineListResponse
import com.example.pharma_connect_androids.data.models.SingleMedicineResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface MedicineApiService {
    @POST("api/v1/medicines") // Changed to api/v1/medicines for consistency
    suspend fun addMedicine(
        @Body addMedicineRequest: AddMedicineRequest
    ): Response<Unit> // Assuming backend returns 201 Created with no specific body on success, or an error response

    @GET("api/v1/medicines") // This was already changed by you
    suspend fun getAllMedicines(): Response<MedicineListResponse>

    @DELETE("api/v1/medicines/{id}") // DELETE endpoint
    suspend fun deleteMedicine(@Path("id") id: String): Response<Unit>

    @GET("api/v1/medicines/{id}") // GET single medicine by ID
    suspend fun getMedicineById(@Path("id") id: String): Response<SingleMedicineResponse>

    @PATCH("api/v1/medicines/{id}") // PATCH to update medicine
    suspend fun updateMedicine(
        @Path("id") id: String,
        @Body request: AddMedicineRequest // Reusing AddMedicineRequest for update body
    ): Response<Unit> // Assuming similar response to add/delete
} 