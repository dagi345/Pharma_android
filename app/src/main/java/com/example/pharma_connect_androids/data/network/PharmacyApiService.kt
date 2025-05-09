package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.Pharmacist
import com.example.pharma_connect_androids.data.models.ApiResponse
import com.example.pharma_connect_androids.data.models.PharmacyListResponse
import com.example.pharma_connect_androids.data.models.SinglePharmacyResponse
import com.example.pharma_connect_androids.data.models.UpdatePharmacyRequest
import com.example.pharma_connect_androids.data.models.AddMedicineInventoryRequest
import com.example.pharma_connect_androids.data.models.InventoryListResponse
import com.example.pharma_connect_androids.data.models.SingleInventoryItemResponse
import com.example.pharma_connect_androids.data.models.UpdateInventoryItemRequest
import com.example.pharma_connect_androids.data.models.Pharmacy
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface PharmacyApiService {

    // Get pharmacists for a specific pharmacy
    @GET("pharmacies/{id}/pharmacists")
    suspend fun getPharmacists(
        @Path("id") pharmacyId: String
    ): Response<ApiResponse<List<Pharmacist>>>

    @GET("api/v1/pharmacies") // Fetch all pharmacies (admin)
    suspend fun getAllPharmacies(): Response<PharmacyListResponse>

    @GET("api/v1/pharmacies/{id}") // Fetch a single pharmacy by ID (admin)
    suspend fun getPharmacyById(@Path("id") pharmacyId: String): Response<SinglePharmacyResponse>

    @DELETE("api/v1/pharmacies/{id}") // Delete a pharmacy by ID (admin)
    suspend fun deletePharmacy(@Path("id") pharmacyId: String): Response<Unit> // Assuming no body in response

    @PATCH("api/v1/pharmacies/{id}") // Use PATCH for updates
    suspend fun updatePharmacy(
        @Path("id") pharmacyId: String,
        @Body request: UpdatePharmacyRequest
    ): Response<Unit> // Assuming simple success/error response

    @GET("api/v1/pharmacies/{id}/inventory") // Fetch inventory for a pharmacy
    suspend fun getInventory(@Path("id") pharmacyId: String): Response<InventoryListResponse>

    @DELETE("api/v1/pharmacies/{id}/inventory/{inventoryItemId}") // Delete item from inventory
    suspend fun deleteInventoryItem(
        @Path("id") pharmacyId: String,
        @Path("inventoryItemId") inventoryItemId: String
    ): Response<Unit>
    
    @GET("api/v1/pharmacies/{id}/inventory/{inventoryItemId}") // Fetch single inventory item (for update prefill)
    suspend fun getInventoryItemById(
         @Path("id") pharmacyId: String,
         @Path("inventoryItemId") inventoryItemId: String
    ): Response<SingleInventoryItemResponse> // Assuming response wraps single item

    @PATCH("api/v1/pharmacies/{id}/inventory/{inventoryItemId}") // Full update of an inventory item
    suspend fun updateInventoryItem(
        @Path("id") pharmacyId: String,
        @Path("inventoryItemId") inventoryItemId: String,
        @Body request: UpdateInventoryItemRequest
    ): Response<Unit>

    @POST("api/v1/pharmacies/{id}/inventory") // Add medicine to inventory
    suspend fun addMedicineToInventory(
        @Path("id") pharmacyId: String,
        @Body request: AddMedicineInventoryRequest
    ): Response<Unit> // Assuming simple response

    @GET("pharmacies/nearby") // Or your actual endpoint path
    suspend fun getNearbyPharmacies(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radiusInKm: Int = 10 // Optional: search radius in KM, default to 10km
    ): Response<PharmacyListResponse> // Assuming your backend returns a list of pharmacies
} 