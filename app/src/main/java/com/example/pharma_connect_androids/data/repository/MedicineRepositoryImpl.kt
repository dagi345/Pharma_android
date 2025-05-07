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
} 