package com.example.pharma_connect_androids.data.repository

import android.util.Log
import com.example.pharma_connect_androids.data.models.*
import com.example.pharma_connect_androids.data.network.ApplicationApiService
import com.example.pharma_connect_androids.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationRepository @Inject constructor(
    private val applicationApiService: ApplicationApiService
) {

    private val TAG = "ApplicationRepository"

    suspend fun submitApplication(request: PharmacyApplicationRequest): Resource<PharmacyApplicationResponse?> {
        Log.d(TAG, "Submitting application for pharmacy: ${request.pharmacyName}")
        return try {
            val response = applicationApiService.submitApplication(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Application submission failed"
                Log.e(TAG, "submitApplication error: Code=${response.code()}, Body=$errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: IOException) {
            Log.e(TAG, "submitApplication IO error", e)
            Resource.Error("Network error: ${e.message ?: "Unknown IO Exception"}")
        } catch (e: Exception) {
            Log.e(TAG, "submitApplication general error", e)
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    fun getApplications(): Flow<Resource<List<Application>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Fetching applications...")
            val response = applicationApiService.getApplications()
            val responseBody = response.body()

            if (response.isSuccessful && responseBody != null && responseBody.applications != null) {
                Log.d(TAG, "Applications fetched successfully: ${responseBody.applications.size} items")
                emit(Resource.Success(responseBody.applications))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch applications"
                Log.e(TAG, "getApplications error: Code=${response.code()}, Body=$errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "getApplications HTTP error", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "getApplications network error", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e(TAG, "getApplications general error", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    fun getApplicationById(applicationId: String): Flow<Resource<Application?>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Fetching application details for ID: $applicationId")
            val response = applicationApiService.getApplicationById(applicationId)
            val responseBody = response.body()

            if (response.isSuccessful && responseBody != null && responseBody.application != null) {
                Log.d(TAG, "Application details fetched successfully for ID: $applicationId")
                emit(Resource.Success(responseBody.application))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to fetch application details"
                Log.e(TAG, "getApplicationById error: Code=${response.code()}, Body=$errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "getApplicationById HTTP error", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "getApplicationById network error", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e(TAG, "getApplicationById general error", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateApplicationStatus(applicationId: String, status: String): Resource<Unit?> {
        Log.d(TAG, "Updating status for application $applicationId to $status")
        return try {
            val request = UpdateApplicationStatusRequest(status = status)
            val response = applicationApiService.updateApplicationStatus(applicationId, request)
            if (response.isSuccessful) {
                Log.d(TAG, "Application $applicationId status updated successfully to $status")
                Resource.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Failed to update status"
                Log.e(TAG, "updateApplicationStatus error: Code=${response.code()}, Body=$errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: IOException) {
            Log.e(TAG, "updateApplicationStatus IO error", e)
            Resource.Error("Network error: ${e.message ?: "Unknown IO Exception"}")
        } catch (e: Exception) {
            Log.e(TAG, "updateApplicationStatus general error", e)
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

} 