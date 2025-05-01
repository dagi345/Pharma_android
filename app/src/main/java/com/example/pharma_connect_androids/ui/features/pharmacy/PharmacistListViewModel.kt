package com.example.pharma_connect_androids.ui.features.pharmacy // Or a more suitable features package

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.models.Pharmacist
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PharmacistListState(
    val pharmacists: List<Pharmacist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pharmacyId: String? = null, // Store pharmacyId from navigation
    val referralLink: String = ""
)

@HiltViewModel
class PharmacistListViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository,
    private val sessionManager: SessionManager, // To check owner role
    private val savedStateHandle: SavedStateHandle // To get pharmacyId from nav args
) : ViewModel() {

    private val _state = MutableStateFlow(PharmacistListState())
    val state: StateFlow<PharmacistListState> = _state.asStateFlow()

    // TODO: Define FRONTEND_BASE_URL - This should come from a config/constants file ideally
    private val FRONTEND_BASE_URL = "http://localhost:3000" // Replace with your actual frontend URL

    init {
        // Get pharmacyId from navigation arguments
        val pharmacyIdArg: String? = savedStateHandle.get<String>("pharmacyId")
        _state.value = _state.value.copy(
            pharmacyId = pharmacyIdArg,
            referralLink = if (pharmacyIdArg != null) "$FRONTEND_BASE_URL/sign-up-pharmacist/$pharmacyIdArg" else ""
        )

        // Fetch pharmacists if pharmacyId is available
        pharmacyIdArg?.let {
            // Perform initial authorization check (can add more robust checks)
            val currentUser = sessionManager.getUserData()
            if (currentUser?.role == "owner" && currentUser.pharmacyId == it) {
                 fetchPharmacists(it)
            } else {
                _state.value = _state.value.copy(error = "Unauthorized access.")
            }
        }
    }

    private fun fetchPharmacists(pharmacyId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = pharmacyRepository.getPharmacists(pharmacyId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        pharmacists = result.data ?: emptyList(),
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Failed to load pharmacists",
                        isLoading = false
                    )
                }
                 is Resource.Loading -> { /* Optionally handle loading state if Resource includes it */ }
            }
        }
    }
    
     // Function to manually trigger refresh if needed
    fun refreshPharmacists() {
        state.value.pharmacyId?.let {
             // Re-check auth maybe?
             fetchPharmacists(it)
        }
    }
} 