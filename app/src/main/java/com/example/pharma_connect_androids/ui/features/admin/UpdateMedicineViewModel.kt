package com.example.pharma_connect_androids.ui.features.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.AddMedicineRequest // Reusing for update
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.data.repository.MedicineRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Using AddMedicineFormState and AddMedicineUiState from AdminAddMedicineViewModel
// but might need to rename/refactor if they diverge significantly.
// For now, let's alias them or copy for clarity if needed.
// Using AddMedicineFormState directly from AdminAddMedicineViewModel for form structure
// Using AddMedicineUiState for overall screen state (fetch, update result)

@HiltViewModel
class UpdateMedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val medicineId: String = savedStateHandle.get<String>("medicineId") ?: ""

    var formState by mutableStateOf(AddMedicineFormState()) // Reusing form state structure
        private set

    private val _uiState = MutableStateFlow<AddMedicineUiState>(AddMedicineUiState.Idle) // Reusing UI state
    val uiState: StateFlow<AddMedicineUiState> = _uiState.asStateFlow()

    // State for fetching the initial medicine details
    private val _loadState = MutableStateFlow<Resource<Medicine>>(Resource.Loading())
    val loadState: StateFlow<Resource<Medicine>> = _loadState.asStateFlow()

    init {
        if (medicineId.isNotBlank()) {
            fetchMedicineDetails(medicineId)
        }
    }

    private fun fetchMedicineDetails(id: String) {
        viewModelScope.launch {
            _loadState.value = Resource.Loading()
            val result = medicineRepository.getMedicineById(id)
            _loadState.value = result
            if (result is Resource.Success) {
                result.data?.let {
                    formState = formState.copy(
                        name = it.name,
                        description = it.description,
                        image = it.image ?: "",
                        category = it.category
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        formState = formState.copy(name = name, nameError = null)
    }

    fun onDescriptionChange(description: String) {
        formState = formState.copy(description = description, descriptionError = null)
    }

    fun onImageChange(image: String) {
        formState = formState.copy(image = image, imageError = null)
    }

    fun onCategoryChange(category: String) {
        formState = formState.copy(category = category, categoryError = null)
    }

    private fun validateForm(): Boolean {
        // Same validation as AddMedicineViewModel
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

    fun updateMedicine() {
        if (!validateForm() || medicineId.isBlank()) {
            if(medicineId.isBlank()) _uiState.value = AddMedicineUiState.Error("Medicine ID is missing.")
            return
        }

        _uiState.value = AddMedicineUiState.Loading
        viewModelScope.launch {
            val result = medicineRepository.updateMedicine(
                medicineId,
                AddMedicineRequest(
                    name = formState.name.trim(),
                    description = formState.description.trim(),
                    image = formState.image.trim(),
                    category = formState.category.trim()
                )
            )
            when (result) {
                is Resource.Success -> _uiState.value = AddMedicineUiState.Success // Success state will trigger navigation back via LaunchedEffect in Screen
                is Resource.Error -> _uiState.value = AddMedicineUiState.Error(result.message ?: "Unknown error occurred")
                else -> {}
            }
        }
    }
    fun resetUiStateToIdle(){
        _uiState.value = AddMedicineUiState.Idle
    }
} 