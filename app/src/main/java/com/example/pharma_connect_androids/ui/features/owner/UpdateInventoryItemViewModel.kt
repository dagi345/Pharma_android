package com.example.pharma_connect_androids.ui.features.owner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.InventoryItem
import com.example.pharma_connect_androids.data.models.UpdateInventoryItemRequest
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// State for the update form
data class UpdateInventoryItemState(
    val isLoadingDetails: Boolean = false,
    val itemDetails: InventoryItem? = null,
    val loadError: String? = null,
    
    val quantity: String = "",
    val price: String = "",
    val expiryDate: LocalDate? = null,

    val quantityError: String? = null,
    val priceError: String? = null,
    val expiryDateError: String? = null,
    
    val isSubmitting: Boolean = false,
    val submissionError: String? = null,
    val submissionSuccess: Boolean = false
)

@HiltViewModel
class UpdateInventoryItemViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pharmacyId: String? = savedStateHandle.get<String>("pharmacyId")
    private val inventoryItemId: String? = savedStateHandle.get<String>("inventoryItemId")

    private val _state = MutableStateFlow(UpdateInventoryItemState())
    val state: StateFlow<UpdateInventoryItemState> = _state.asStateFlow()

    init {
        fetchItemDetails()
    }

    fun fetchItemDetails() {
        if (pharmacyId == null || inventoryItemId == null) {
            _state.value = _state.value.copy(loadError = "Missing pharmacy or item ID")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingDetails = true)
            when(val result = pharmacyRepository.getInventoryItemById(pharmacyId, inventoryItemId)) {
                is Resource.Success -> {
                    result.data?.let { item ->
                        _state.value = _state.value.copy(
                            itemDetails = item,
                            quantity = item.quantity.toString(),
                            price = item.price.toString(),
                            expiryDate = item.expiryDate,
                            isLoadingDetails = false
                        )
                    } ?: run {
                        _state.value = _state.value.copy(isLoadingDetails = false, loadError = "Failed to load item details.")
                    }
                }
                is Resource.Error -> {
                     _state.value = _state.value.copy(isLoadingDetails = false, loadError = result.message ?: "Error loading item details.")
                }
                else -> {}
            }
        }
    }
    
    // Input Change Handlers
    fun onQuantityChanged(value: String) {
        _state.value = _state.value.copy(quantity = value.filter { it.isDigit() }, quantityError = null)
    }

    fun onPriceChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val decimalCount = filtered.count { it == '.' }
        if (decimalCount <= 1) {
             _state.value = _state.value.copy(price = filtered, priceError = null)
        }
    }

    fun onExpiryDateSelected(date: LocalDate?) {
         _state.value = _state.value.copy(expiryDate = date, expiryDateError = null)
    }
    
    private fun validate(): Boolean {
        val currentState = _state.value
        val today = LocalDate.now()
        val quantityVal = currentState.quantity.toIntOrNull()
        val priceVal = currentState.price.toDoubleOrNull()

        val quantityError = when {
            quantityVal == null -> "Quantity is required"
            quantityVal <= 0 -> "Quantity must be positive"
            else -> null
        }
        val priceError = when {
            priceVal == null -> "Price is required"
            priceVal <= 0 -> "Price must be positive"
            else -> null
        }
         val expiryDateError = when {
             currentState.expiryDate == null -> "Expiry date is required"
             currentState.expiryDate.isBefore(today.plusDays(1)) -> "Expiry date must be in the future"
             else -> null
         }

        _state.value = currentState.copy(
            quantityError = quantityError,
            priceError = priceError,
            expiryDateError = expiryDateError
        )
        return quantityError == null && priceError == null && expiryDateError == null
    }

    fun submitUpdate() {
        if (!validate() || pharmacyId == null || inventoryItemId == null) {
             _state.value = _state.value.copy(submissionError = "Validation failed or IDs missing")
             return
         }

        val currentState = _state.value
        val quantityVal = currentState.quantity.toInt() // Safe due to validation
        val priceVal = currentState.price.toDouble() // Safe due to validation
        val expiryDateVal = currentState.expiryDate!! // Safe due to validation

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, submissionError = null, submissionSuccess = false)
            
            // Ensure we have the medicineId from the loaded item details
            val currentMedicineId = currentState.itemDetails?.medicineId ?: run {
                 _state.value = _state.value.copy(isSubmitting = false, submissionError = "Cannot update: Medicine ID missing from loaded item.")
                 return@launch
            }

            val request = UpdateInventoryItemRequest(
                quantity = quantityVal,
                price = priceVal,
                expiryDate = expiryDateVal,
                medicineId = currentMedicineId // Pass the medicineId
            )

            when (val result = pharmacyRepository.updateInventoryItem(pharmacyId, inventoryItemId, request)) {
                 is Resource.Success -> {
                     _state.value = _state.value.copy(isSubmitting = false, submissionSuccess = true, submissionError = null)
                 }
                 is Resource.Error -> {
                     _state.value = _state.value.copy(isSubmitting = false, submissionError = result.message ?: "Update failed", submissionSuccess = false)
                 }
                 else -> {}
            }
        }
    }
    
     fun resetSubmissionStatus() {
         _state.value = _state.value.copy(submissionSuccess = false, submissionError = null)
    }
} 