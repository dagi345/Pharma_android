package com.example.pharma_connect_androids.ui.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminPharmaciesUiState {
    object Loading : AdminPharmaciesUiState()
    data class Success(val pharmacies: List<Pharmacy>) : AdminPharmaciesUiState()
    data class Error(val message: String) : AdminPharmaciesUiState()
    object Idle : AdminPharmaciesUiState()
}

// Reusing ItemActionState from AdminMedicinesViewModel context if suitable,
// or define a specific one if actions differ significantly.
// For now, assuming ItemActionState (Idle, Loading, Success(msg), Error(msg)) is generic enough.

@HiltViewModel
class AdminPharmaciesViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository
) : ViewModel() {

    private var originalPharmacies: List<Pharmacy> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<AdminPharmaciesUiState>(AdminPharmaciesUiState.Idle)
    val uiState: StateFlow<AdminPharmaciesUiState> = _uiState
        .combine(_searchQuery) { state, query ->
            if (originalPharmacies.isNotEmpty()) {
                val listToFilter = originalPharmacies
                val filteredList = if (query.isBlank()) {
                    listToFilter
                } else {
                    listToFilter.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true) ||
                        it.city?.contains(query, ignoreCase = true) == true
                        // Add other searchable fields like email, status if needed
                    }
                }
                if (state is AdminPharmaciesUiState.Error && originalPharmacies.isEmpty()) state else AdminPharmaciesUiState.Success(filteredList)

            } else {
                state // Pass through Loading, Error (if no original data), Idle
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminPharmaciesUiState.Idle)

    private val _itemActionState = MutableStateFlow<ItemActionState>(ItemActionState.Idle)
    val itemActionState: StateFlow<ItemActionState> = _itemActionState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchPharmacies() {
        viewModelScope.launch {
            _uiState.value = AdminPharmaciesUiState.Loading
            _searchQuery.value = "" // Reset search on fresh fetch
            when (val result = pharmacyRepository.getAllPharmacies()) {
                is Resource.Success -> {
                    originalPharmacies = result.data ?: emptyList()
                    _uiState.value = AdminPharmaciesUiState.Success(originalPharmacies)
                }
                is Resource.Error -> {
                    originalPharmacies = emptyList()
                    _uiState.value = AdminPharmaciesUiState.Error(result.message ?: "Error fetching pharmacies")
                }
                else -> {}
            }
        }
    }

    fun deletePharmacy(id: String) {
        viewModelScope.launch {
            _itemActionState.value = ItemActionState.Loading
            when (val result = pharmacyRepository.deletePharmacy(id)) {
                is Resource.Success -> {
                    _itemActionState.value = ItemActionState.Success("Pharmacy deleted successfully.")
                    fetchPharmacies() // Refresh list
                }
                is Resource.Error -> {
                    _itemActionState.value = ItemActionState.Error(result.message ?: "Error deleting pharmacy.")
                }
                else -> {}
            }
        }
    }

    fun resetItemActionState() {
        _itemActionState.value = ItemActionState.Idle
    }
} 