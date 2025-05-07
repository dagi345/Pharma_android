package com.example.pharma_connect_androids.ui.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.data.repository.MedicineRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminMedicinesUiState {
    object Loading : AdminMedicinesUiState()
    data class Success(val medicines: List<Medicine>) : AdminMedicinesUiState()
    data class Error(val message: String) : AdminMedicinesUiState()
    object Idle : AdminMedicinesUiState()
}

@HiltViewModel
class AdminMedicinesViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminMedicinesUiState>(AdminMedicinesUiState.Idle)
    val uiState: StateFlow<AdminMedicinesUiState> = _uiState.asStateFlow()

    fun fetchMedicines() {
        viewModelScope.launch {
            _uiState.value = AdminMedicinesUiState.Loading
            when (val result = medicineRepository.getAllMedicines()) {
                is Resource.Success -> {
                    _uiState.value = AdminMedicinesUiState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _uiState.value = AdminMedicinesUiState.Error(result.message ?: "Unknown error fetching medicines")
                }
                is Resource.Loading -> {
                    // Handled by initial _uiState.value setting, or if Resource.Loading is emitted by repo
                     _uiState.value = AdminMedicinesUiState.Loading
                }
            }
        }
    }
    
    // init {
    //    fetchMedicines() // Option to fetch immediately on ViewModel creation
    // }
} 