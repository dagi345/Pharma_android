package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.models.AddMedicineRequest
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.data.network.MedicineApiService
import com.example.pharma_connect_androids.util.Resource
import java.io.IOException
import javax.inject.Inject

class MedicineRepositoryImpl @Inject constructor(
    private val medicineApiService: MedicineApiService
) : MedicineRepository {
    override suspend fun addMedicine(addMedicineRequest: AddMedicineRequest): Resource<Unit> {
        return try {
            val response = medicineApiService.addMedicine(addMedicineRequest)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error from API (${response.code()})"
                Resource.Error(errorMsg)
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Couldn't reach server"}")
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getAllMedicines(): Resource<List<Medicine>> {
        return try {
            val response = medicineApiService.getAllMedicines()
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response body or data field missing")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error from API (${response.code()})"
                Resource.Error(errorMsg)
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Couldn't reach server"}")
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun deleteMedicine(id: String): Resource<Unit> {
        return try {
            val response = medicineApiService.deleteMedicine(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error from API (${response.code()}) for delete"
                Resource.Error(errorMsg)
            }
        } catch (e: IOException) {
            Resource.Error("Network error during delete: ${e.message ?: "Couldn\'t reach server"}")
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred during delete: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getMedicineById(id: String): Resource<Medicine> {
        return try {
            val response = medicineApiService.getMedicineById(id)
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response body or data missing for getMedicineById")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown API error for getMedicineById (${response.code()})"
                Resource.Error(errorMsg)
            }
        } catch (e: IOException) {
            Resource.Error("Network error for getMedicineById: ${e.message ?: "Couldn\'t reach server"}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error for getMedicineById: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun updateMedicine(id: String, request: AddMedicineRequest): Resource<Unit> {
        return try {
            val response = medicineApiService.updateMedicine(id, request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown API error for updateMedicine (${response.code()})"
                Resource.Error(errorMsg)
            }
        } catch (e: IOException) {
            Resource.Error("Network error for updateMedicine: ${e.message ?: "Couldn\'t reach server"}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error for updateMedicine: ${e.message ?: "Unknown error"}")
        }
    }
} 