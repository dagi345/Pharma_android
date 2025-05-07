package com.example.pharma_connect_androids.ui.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.data.repository.MedicineRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Import flow operators
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminMedicinesUiState {
    object Loading : AdminMedicinesUiState()
    data class Success(val medicines: List<Medicine>) : AdminMedicinesUiState()
    data class Error(val message: String) : AdminMedicinesUiState()
    object Idle : AdminMedicinesUiState() // Represents initial state or after an action that clears data
}

// New state for individual item actions like delete/update
sealed class ItemActionState {
    object Idle : ItemActionState()
    object Loading : ItemActionState() // Optional: if delete is quick, might not need specific loading for it
    data class Success(val message: String) : ItemActionState()
    data class Error(val message: String) : ItemActionState()
}

@HiltViewModel
class AdminMedicinesViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    // Holds the original unfiltered list
    private var originalMedicines: List<Medicine> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<AdminMedicinesUiState>(AdminMedicinesUiState.Idle)
    val uiState: StateFlow<AdminMedicinesUiState> = _uiState
        // Combine with searchQuery to produce the final list
        .combine(_searchQuery) { state, query ->
            // If the underlying state is Success, apply search query to originalMedicines
            if (state is AdminMedicinesUiState.Success || originalMedicines.isNotEmpty()) {
                val listToFilter = originalMedicines // Always filter from the original full list
                val filteredList = if (query.isBlank()) {
                    listToFilter
                } else {
                    listToFilter.filter { 
                        it.name.contains(query, ignoreCase = true) || 
                        it.category.contains(query, ignoreCase = true)
                    }
                }
                // If the original state was already an error or loading, preserve that, otherwise return success with filtered list.
                // This ensures that if fetch fails, search doesn't wrongly show a success with empty list.
                if (state is AdminMedicinesUiState.Error || state is AdminMedicinesUiState.Loading || state is AdminMedicinesUiState.Idle && originalMedicines.isEmpty()){
                    state
                } else {
                    AdminMedicinesUiState.Success(filteredList)
                }
            } else {
                state // Pass through other states like Loading, Error, Idle if original list is empty
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminMedicinesUiState.Idle)

    private val _itemActionState = MutableStateFlow<ItemActionState>(ItemActionState.Idle)
    val itemActionState: StateFlow<ItemActionState> = _itemActionState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // The combine operator on uiState will automatically re-evaluate and filter.
    }

    fun fetchMedicines() {
        viewModelScope.launch {
            _uiState.value = AdminMedicinesUiState.Loading // Set to loading
            _searchQuery.value = "" // Reset search query on fresh fetch
            when (val result = medicineRepository.getAllMedicines()) {
                is Resource.Success -> {
                    originalMedicines = result.data ?: emptyList()
                    // The combine operator will now use this new originalMedicines with the current searchQuery
                    // and update the _uiState automatically if the query is blank.
                    // If query is not blank, combine will also filter.
                    _uiState.value = AdminMedicinesUiState.Success(originalMedicines)
                }
                is Resource.Error -> {
                    originalMedicines = emptyList()
                    _uiState.value = AdminMedicinesUiState.Error(result.message ?: "Unknown error fetching medicines")
                }
                else -> { /* Not Resource.Loading in this path */ }
            }
        }
    }

    fun deleteMedicine(id: String) {
        viewModelScope.launch {
            _itemActionState.value = ItemActionState.Loading 
            when (val result = medicineRepository.deleteMedicine(id)) {
                is Resource.Success -> {
                    _itemActionState.value = ItemActionState.Success("Medicine deleted successfully.")
                    fetchMedicines() 
                }
                is Resource.Error -> {
                    _itemActionState.value = ItemActionState.Error(result.message ?: "Error deleting medicine.")
                }
                else -> { /* Not Resource.Loading */ }
            }
        }
    }

    fun resetItemActionState() {
        _itemActionState.value = ItemActionState.Idle
    }
    
    // init {
    //    fetchMedicines()
    // }
} 