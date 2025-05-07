package com.example.pharma_connect_androids.ui.features.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.models.AddMedicineInventoryRequest
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.data.repository.MedicineRepository
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

data class OwnerAddMedicineState(
    val allMedicines: List<Medicine> = emptyList(),
    val isLoadingMedicines: Boolean = false,
    val medicineLoadError: String? = null,

    val selectedMedicine: Medicine? = null,
    val quantity: String = "",
    val price: String = "",
    val expiryDate: LocalDate? = null,

    val quantityError: String? = null,
    val priceError: String? = null,
    val expiryDateError: String? = null,
    val medicineIdError: String? = null,

    val isSubmitting: Boolean = false,
    val submissionError: String? = null,
    val submissionSuccess: Boolean = false
)

@HiltViewModel
class OwnerAddMedicineViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository,
    private val medicineRepository: MedicineRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerAddMedicineState())
    val state: StateFlow<OwnerAddMedicineState> = _state.asStateFlow()

    init {
        fetchAllMedicines()
    }

    private fun fetchAllMedicines() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMedicines = true)
            when (val result = medicineRepository.getAllMedicines()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        allMedicines = result.data ?: emptyList(),
                        isLoadingMedicines = false,
                        medicineLoadError = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoadingMedicines = false,
                        medicineLoadError = result.message ?: "Failed to load medicines"
                    )
                }
                else -> {}
            }
        }
    }

    fun onMedicineSelected(medicine: Medicine) {
        _state.value = _state.value.copy(selectedMedicine = medicine, medicineIdError = null)
    }

    fun onQuantityChanged(value: String) {
        _state.value = _state.value.copy(quantity = value.filter { it.isDigit() }, quantityError = null)
    }

    fun onPriceChanged(value: String) {
        // Allow digits and at most one decimal point
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
        var isValid = true
        val today = LocalDate.now()

        val quantityVal = currentState.quantity.toIntOrNull()
        val priceVal = currentState.price.toDoubleOrNull()

        val medicineIdError = if (currentState.selectedMedicine == null) "Medicine must be selected" else null
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
            medicineIdError = medicineIdError,
            quantityError = quantityError,
            priceError = priceError,
            expiryDateError = expiryDateError
        )
        
        return medicineIdError == null && quantityError == null && priceError == null && expiryDateError == null
    }

    fun submitAddMedicineToInventory() {
        if (!validate()) return

        val currentState = _state.value
        val pharmacyId = sessionManager.getUserData()?.pharmacyId
        val userId = sessionManager.getUserData()?.userId
        val selectedMedId = currentState.selectedMedicine?.id
        val quantityVal = currentState.quantity.toIntOrNull() // Already validated
        val priceVal = currentState.price.toDoubleOrNull() // Already validated
        val expiryDateVal = currentState.expiryDate // Already validated

        if (pharmacyId == null || userId == null || selectedMedId == null || quantityVal == null || priceVal == null || expiryDateVal == null) {
             _state.value = _state.value.copy(
                submissionError = "Cannot submit: Missing required data (Pharmacy, User, Medicine, or Form Fields).", 
                isSubmitting = false
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, submissionError = null, submissionSuccess = false)

            // Re-check potentially nullable values inside launch scope
            val currentUserId = userId ?: run {
                 _state.value = _state.value.copy(
                    submissionError = "Cannot submit: User ID not found.", 
                    isSubmitting = false
                )
                return@launch
            }
            
            val currentPharmacyId = pharmacyId ?: run {
                _state.value = _state.value.copy(
                    submissionError = "Cannot submit: Pharmacy ID not found.", 
                    isSubmitting = false
                )
                return@launch
            }
            
            // ID and expiryDate should be guaranteed non-null by validate(), but check anyway or use !!
            val currentSelectedMedId = selectedMedId ?: return@launch 
            val currentExpiryDate = expiryDateVal ?: return@launch

            val request = AddMedicineInventoryRequest(
                medicineId = currentSelectedMedId,
                quantity = quantityVal, // Non-null by validate()
                price = priceVal, // Non-null by validate()
                expiryDate = currentExpiryDate,
                updatedBy = currentUserId // Now guaranteed non-null
            )

            when (val result = pharmacyRepository.addMedicineToInventory(currentPharmacyId, request)) {
                is Resource.Success -> {
                    _state.value = OwnerAddMedicineState() // Reset state on success
                    fetchAllMedicines() // Refresh medicine list (maybe not needed? depends on UI)
                    // Keep success flag separate or use a dedicated event flow
                    _state.value = _state.value.copy(submissionSuccess = true, isSubmitting = false) 
                }
                is Resource.Error -> {
                     _state.value = _state.value.copy(
                        submissionError = result.message ?: "Failed to add medicine to inventory.", 
                        isSubmitting = false,
                        submissionSuccess = false
                    )
                }
                else -> { _state.value = _state.value.copy(isSubmitting = false) }
            }
        }
    }
    
    fun resetSubmissionStatus() {
         _state.value = _state.value.copy(submissionSuccess = false, submissionError = null)
    }
} 