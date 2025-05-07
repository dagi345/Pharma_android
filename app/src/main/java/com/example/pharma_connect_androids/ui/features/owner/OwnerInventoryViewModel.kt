package com.example.pharma_connect_androids.ui.features.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.models.InventoryItem
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.launch
import javax.inject.Inject

// State for the inventory list screen
data class OwnerInventoryUiState(
    val isLoading: Boolean = false,
    val inventoryItems: List<InventoryItem> = emptyList(),
    val error: String? = null
)

// Define ItemActionState here if not defined globally
sealed class ItemActionState {
    object Idle : ItemActionState()
    object Loading : ItemActionState()
    data class Success(val message: String) : ItemActionState()
    data class Error(val message: String) : ItemActionState()
}

@HiltViewModel
class OwnerInventoryViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerInventoryUiState())
    val uiState: StateFlow<OwnerInventoryUiState> = _uiState.asStateFlow()

    // Separate state for item-specific actions (like delete or quantity update status)
    private val _itemActionState = MutableStateFlow<ItemActionState>(ItemActionState.Idle)
    val itemActionState: StateFlow<ItemActionState> = _itemActionState.asStateFlow()

    // Corrected: Get pharmacyId from UserData
    val pharmacyId: String? = sessionManager.getUserData()?.pharmacyId

    init {
        fetchInventory()
    }

    fun fetchInventory() {
        pharmacyId?.let {
            id ->
            viewModelScope.launch {
                _uiState.value = OwnerInventoryUiState(isLoading = true)
                when (val result = pharmacyRepository.getInventory(id)) {
                    is Resource.Success -> {
                        _uiState.value = OwnerInventoryUiState(inventoryItems = result.data ?: emptyList())
                    }
                    is Resource.Error -> {
                         _uiState.value = OwnerInventoryUiState(error = result.message ?: "Failed to load inventory")
                    }
                    else -> { /* Handle Loading if necessary */ }
                }
            }
        } ?: run {
             _uiState.value = OwnerInventoryUiState(error = "Pharmacy ID not found")
        }
    }

    fun deleteInventoryItem(inventoryItemId: String) {
        pharmacyId?.let { pid ->
            viewModelScope.launch {
                _itemActionState.value = ItemActionState.Loading
                when (val result = pharmacyRepository.deleteInventoryItem(pid, inventoryItemId)) {
                    is Resource.Success -> {
                         _itemActionState.value = ItemActionState.Success("Item deleted successfully.")
                         fetchInventory() // Refresh list
                    }
                    is Resource.Error -> {
                        _itemActionState.value = ItemActionState.Error(result.message ?: "Failed to delete item.")
                    }
                    else -> {}
                }
            }
        } ?: run {
            _itemActionState.value = ItemActionState.Error("Pharmacy ID not found")
        }
    }

    fun resetItemActionState() {
        _itemActionState.value = ItemActionState.Idle
    }
} 