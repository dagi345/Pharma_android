package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.models.Pharmacist
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.data.network.PharmacyApiService
import com.example.pharma_connect_androids.util.Resource // Assuming Resource wrapper
import com.example.pharma_connect_androids.data.local.SessionManager // For getting token
import com.example.pharma_connect_androids.util.handleApiResponse // Assuming this utility exists
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Make repository a singleton
class PharmacyRepository @Inject constructor(
    private val pharmacyApiService: PharmacyApiService,
    private val sessionManager: SessionManager // Inject SessionManager
) {

    suspend fun getPharmacists(pharmacyId: String): Resource<List<Pharmacist>?> {
        val token = sessionManager.getToken()
            ?: return Resource.Error("User not authenticated") // Return early if no token

        return handleApiResponse {
             pharmacyApiService.getPharmacists("Bearer $token", pharmacyId)
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
} 