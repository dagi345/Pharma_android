package com.example.pharma_connect_androids.ui.features.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.AddMedicineRequest
import com.example.pharma_connect_androids.data.repository.MedicineRepository
import com.example.pharma_connect_androids.util.Resource // Use Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMedicineFormState(
    val name: String = "",
    val description: String = "",
    val image: String = "", // URL
    val category: String = "",
    val nameError: String? = null,
    val descriptionError: String? = null,
    val imageError: String? = null,
    val categoryError: String? = null
)

sealed class AddMedicineUiState {
    object Idle : AddMedicineUiState()
    object Loading : AddMedicineUiState()
    object Success : AddMedicineUiState()
    data class Error(val message: String) : AddMedicineUiState() // Message is String
}

@HiltViewModel
class AdminAddMedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository // Inject the repository
) : ViewModel() {

    var formState by mutableStateOf(AddMedicineFormState())
        private set

    private val _uiState = MutableStateFlow<AddMedicineUiState>(AddMedicineUiState.Idle)
    val uiState: StateFlow<AddMedicineUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        formState = formState.copy(name = name, nameError = null)
    }

    fun onDescriptionChange(description: String) {
        formState = formState.copy(description = description, descriptionError = null)
    }

    fun onImageChange(image: String) { // Expects URL
        formState = formState.copy(image = image, imageError = null)
    }

    fun onCategoryChange(category: String) {
        formState = formState.copy(category = category, categoryError = null)
    }

    private fun validateForm(): Boolean {
        var isValid = true
        formState = formState.copy(
            nameError = if (formState.name.isBlank()) "Medicine name is required" else null,
            descriptionError = if (formState.description.isBlank()) "Description is required" else null,
            imageError = if (formState.image.isBlank()) "Image URL is required" else null,
            categoryError = if (formState.category.isBlank()) "Category is required" else null
        )
        isValid = formState.nameError == null &&
                  formState.descriptionError == null &&
                  formState.imageError == null &&
                  formState.categoryError == null
        return isValid
    }

    fun addMedicine() {
        if (!validateForm()) {
            return
        }

        _uiState.value = AddMedicineUiState.Loading
        viewModelScope.launch {
            val result = medicineRepository.addMedicine(
                AddMedicineRequest(
                    name = formState.name.trim(),
                    description = formState.description.trim(),
                    image = formState.image.trim(), // URL
                    category = formState.category.trim()
                )
            )
            when (result) {
                is Resource.Success -> _uiState.value = AddMedicineUiState.Success
                is Resource.Error -> _uiState.value = AddMedicineUiState.Error(result.message ?: "Unknown error occurred")
                is Resource.Loading -> {} // Should not happen here as repository call is not a flow for this
            }
        }
    }

    fun resetState() {
        _uiState.value = AddMedicineUiState.Idle
        formState = AddMedicineFormState()
    }
} 