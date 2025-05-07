package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.models.AddMedicineRequest
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.util.Resource // Use Resource instead of Result

interface MedicineRepository {
    suspend fun addMedicine(addMedicineRequest: AddMedicineRequest): Resource<Unit>
    suspend fun getAllMedicines(): Resource<List<Medicine>>
    suspend fun deleteMedicine(id: String): Resource<Unit>
    suspend fun getMedicineById(id: String): Resource<Medicine>
    suspend fun updateMedicine(id: String, request: AddMedicineRequest): Resource<Unit>
} 