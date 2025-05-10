package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.models.Pharmacist
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.data.models.UpdatePharmacyRequest
import com.example.pharma_connect_androids.data.models.AddMedicineInventoryRequest
import com.example.pharma_connect_androids.data.models.InventoryItem
import com.example.pharma_connect_androids.data.models.UpdateInventoryItemRequest
import com.example.pharma_connect_androids.data.network.PharmacyApiService
import com.example.pharma_connect_androids.util.Resource // Assuming Resource wrapper
import com.example.pharma_connect_androids.data.local.SessionManager // For getting token
import com.example.pharma_connect_androids.util.handleApiResponse // Assuming this utility exists
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.example.pharma_connect_androids.data.models.PharmacyListResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import retrofit2.HttpException // Corrected
import kotlinx.serialization.json.Json // Corrected
import kotlinx.serialization.json.jsonObject // Already correct
import kotlinx.serialization.json.jsonPrimitive // Already correct
import kotlinx.serialization.SerializationException // Corrected

@Singleton // Make repository a singleton
class PharmacyRepository @Inject constructor(
    private val pharmacyApiService: PharmacyApiService,
    private val sessionManager: SessionManager // Inject SessionManager
) {

    private val TAG = "PharmacyRepository"

    suspend fun getPharmacists(pharmacyId: String): Resource<List<Pharmacist>?> {
        // val token = sessionManager.getToken()
        //    ?: return Resource.Error("User not authenticated") // Token is likely handled by AuthInterceptor now

        return handleApiResponse {
             // Removed the first argument ("Bearer $token") to match service definition
             pharmacyApiService.getPharmacists(pharmacyId)
        }
    }

    suspend fun getAllPharmacies(): Resource<List<Pharmacy>> {
        // Assuming this is an admin-only endpoint, auth might be handled by interceptor
        // If token is explicitly needed and not via interceptor, it should be added like in getPharmacists
        return try {
            val response = pharmacyApiService.getAllPharmacies()
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty pharmacy list response or data missing")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error fetching pharmacies: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error fetching pharmacies: ${e.localizedMessage}")
        }
    }

    suspend fun getPharmacyById(pharmacyId: String): Resource<Pharmacy> {
        return try {
            val response = pharmacyApiService.getPharmacyById(pharmacyId)
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty pharmacy detail response or data missing")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error fetching pharmacy details: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error fetching pharmacy details: ${e.localizedMessage}")
        }
    }

    suspend fun deletePharmacy(pharmacyId: String): Resource<Unit> {
        return try {
            val response = pharmacyApiService.deletePharmacy(pharmacyId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error deleting pharmacy: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error deleting pharmacy: ${e.localizedMessage}")
        }
    }

    suspend fun updatePharmacy(pharmacyId: String, request: UpdatePharmacyRequest): Resource<Unit> {
        return try {
            val response = pharmacyApiService.updatePharmacy(pharmacyId, request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PharmacyRepository", "Update Pharmacy Error: Code=${response.code()}, Body=$errorBody")
                Resource.Error(errorBody ?: "Error updating pharmacy: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PharmacyRepository", "Update Pharmacy Exception: ${e.localizedMessage}", e)
            Resource.Error("Network error updating pharmacy: ${e.localizedMessage}")
        }
    }

    suspend fun addMedicineToInventory(pharmacyId: String, request: AddMedicineInventoryRequest): Resource<Unit> {
        return try {
            val response = pharmacyApiService.addMedicineToInventory(pharmacyId, request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PharmacyRepository", "Add Inventory Error: Code=${response.code()}, Body=$errorBody")
                Resource.Error(errorBody ?: "Error adding medicine to inventory: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PharmacyRepository", "Add Inventory Exception: ${e.localizedMessage}", e)
            Resource.Error("Network error adding medicine to inventory: ${e.localizedMessage}")
        }
    }

    // --- Inventory Methods ---

    suspend fun getInventory(pharmacyId: String): Resource<List<InventoryItem>> {
        return try {
            val response = pharmacyApiService.getInventory(pharmacyId)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: emptyList())
            } else {
                 Resource.Error(response.errorBody()?.string() ?: "Error fetching inventory: ${response.code()}")
            }
        } catch (e: Exception) {
             Resource.Error("Network error fetching inventory: ${e.localizedMessage}")
        }
    }

    suspend fun deleteInventoryItem(pharmacyId: String, inventoryItemId: String): Resource<Unit> {
         return try {
            val response = pharmacyApiService.deleteInventoryItem(pharmacyId, inventoryItemId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error deleting inventory item: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error deleting inventory item: ${e.localizedMessage}")
        }
    }
    
    suspend fun getInventoryItemById(pharmacyId: String, inventoryItemId: String): Resource<InventoryItem> {
         return try {
            val response = pharmacyApiService.getInventoryItemById(pharmacyId, inventoryItemId)
            if (response.isSuccessful) {
                 response.body()?.data?.let {
                     Resource.Success(it)
                 } ?: Resource.Error("Empty inventory item detail response or data missing")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error fetching inventory item: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error fetching inventory item: ${e.localizedMessage}")
        }
    }

    suspend fun updateInventoryItem(pharmacyId: String, inventoryItemId: String, request: UpdateInventoryItemRequest): Resource<Unit> {
         return try {
            val response = pharmacyApiService.updateInventoryItem(pharmacyId, inventoryItemId, request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error updating inventory item: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error updating inventory item: ${e.localizedMessage}")
        }
    }

    // Modified to fetch all pharmacies, client will sort by distance
    suspend fun getNearbyPharmacies(userLat: Double, userLon: Double): Flow<Resource<List<Pharmacy>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Fetching all pharmacies to determine nearby ones client-side.")
            // Call the existing endpoint that fetches all (or a general list of) pharmacies
            val response = pharmacyApiService.getAllPharmacies() // This is a Response<PharmacyListResponse>

            if (response.isSuccessful && response.body() != null) {
                val pharmacies = response.body()!!.data // Assuming PharmacyListResponse has a 'data' field
                Log.d(TAG, "Successfully fetched ${pharmacies.size} total pharmacies.")
                emit(Resource.Success(pharmacies)) // Emit the full list
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to fetch pharmacies: Code=${response.code()}, Message=${response.message()}, ErrorBody=$errorBody")
                val parsedErrorMsg = try {
                    errorBody?.let {
                        kotlinx.serialization.json.Json.parseToJsonElement(it).jsonObject["message"]?.jsonPrimitive?.content
                    }
                } catch (e: Exception) { null }
                val errorMsg = parsedErrorMsg ?: errorBody ?: "Failed to fetch pharmacies (Code: ${response.code()})"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Pharmacies HTTP error: ${e.code()} - ${e.message()}", e)
            emit(Resource.Error(e.message() ?: "An unexpected HTTP error occurred while fetching pharmacies."))
        } catch (e: IOException) {
            Log.e(TAG, "Pharmacies network error", e)
            emit(Resource.Error("Couldn't reach server for pharmacies. Check internet connection."))
        } catch (e: Exception) {
            Log.e(TAG, "Pharmacies general error", e)
            if (e is SerializationException) {
                emit(Resource.Error("Failed to understand server response for pharmacies."))
            } else {
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching pharmacies."))
            }
        }
    }
} 