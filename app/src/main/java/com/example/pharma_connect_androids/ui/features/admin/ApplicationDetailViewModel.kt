package com.example.pharma_connect_androids.ui.features.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.Application
import com.example.pharma_connect_androids.data.repository.ApplicationRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class ApplicationDetailState(
    val application: Application? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ApplicationDetailState())
    val state: StateFlow<ApplicationDetailState> = _state.asStateFlow()
    private val TAG = "AppDetailVM"

    private val applicationId: String? = savedStateHandle.get<String>("applicationId")

    init {
        if (applicationId != null) {
            fetchApplicationDetails(applicationId)
        } else {
            _state.value = ApplicationDetailState(error = "Application ID not found")
            Log.e(TAG, "Application ID is null in SavedStateHandle")
        }
    }

    fun fetchApplicationDetails(id: String) {
        Log.d(TAG, "Fetching details for application: $id")
        applicationRepository.getApplicationById(id)
            .onEach { result ->
                _state.update {
                    when (result) {
                        is Resource.Loading -> {
                            Log.d(TAG, "Loading details...")
                            it.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "Details loaded successfully")
                            it.copy(isLoading = false, application = result.data)
                        }
                        is Resource.Error -> {
                            val errorMsg = result.message ?: "Unknown error loading details"
                            Log.e(TAG, "Error loading details: $errorMsg")
                            it.copy(isLoading = false, error = errorMsg)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    // Note: Approve/Reject logic remains in AdminApplicationViewModel for simplicity
    // If needed here, inject AdminApplicationViewModel or ApplicationRepository again.
} 