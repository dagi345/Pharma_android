package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.models.Pharmacist
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
} 