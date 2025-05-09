package com.example.pharma_connect_androids.ui.features.pharmacy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.data.repository.PharmacyRepository // Assuming this repository exists
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserPharmacyDetailState(
    val pharmacy: Pharmacy? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserPharmacyDetailViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository, // Assuming this repository is injected
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(UserPharmacyDetailState())
    val state: StateFlow<UserPharmacyDetailState> = _state.asStateFlow()

    val pharmacyId: String? = savedStateHandle.get<String>("pharmacyId")

    init {
        pharmacyId?.let {
            fetchPharmacyDetails(it)
        }
    }

    fun fetchPharmacyDetails(id: String) {
        viewModelScope.launch {
            _state.value = UserPharmacyDetailState(isLoading = true)
            try {
                // Assuming pharmacyRepository.getPharmacyById returns Resource<Pharmacy>
                // and that it fetches the full Pharmacy object including contact details.
                val result = pharmacyRepository.getPharmacyById(id)
                if (result is Resource.Success) {
                    _state.value = UserPharmacyDetailState(pharmacy = result.data, isLoading = false)
                } else if (result is Resource.Error) {
                    _state.value = UserPharmacyDetailState(error = result.message ?: "Error fetching pharmacy details", isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = UserPharmacyDetailState(error = e.message ?: "An unexpected error occurred", isLoading = false)
            }
        }
    }
} 