package com.example.pharma_connect_androids.ui.features.admin

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

data class AdminApplicationState(
    val applications: List<Application> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdatedId: String? = null, // To trigger recomposition after update
    val updateLoadingId: String? = null, // Track which item is being updated
    val updateErrorId: Pair<String, String>? = null // Track error for specific item
)

@HiltViewModel
class AdminApplicationViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminApplicationState())
    val state: StateFlow<AdminApplicationState> = _state.asStateFlow()
    private val TAG = "AdminAppVM"

    init {
        fetchApplications()
    }

    fun fetchApplications() {
        Log.d(TAG, "Fetching applications...")
        applicationRepository.getApplications()
            .onEach { result: Resource<List<Application>> ->
                _state.update {
                    when (result) {
                        is Resource.Loading -> {
                            Log.d(TAG, "Loading applications...")
                            it.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "Applications loaded: ${result.data?.size}")
                            it.copy(isLoading = false, applications = result.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Error fetching applications: ${result.message}")
                            it.copy(isLoading = false, error = result.message ?: "Unknown error")
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun updateApplicationStatus(applicationId: String, status: String) {
        viewModelScope.launch {
             Log.d(TAG, "Attempting to update app $applicationId to $status")
            _state.update { it.copy(updateLoadingId = applicationId, updateErrorId = null) }

            val result: Resource<Unit?> = applicationRepository.updateApplicationStatus(applicationId, status)
                
            when(result) {
                is Resource.Success -> {
                     Log.d(TAG, "Successfully updated app $applicationId to $status")
                    _state.update { it.copy(updateLoadingId = null, lastUpdatedId = applicationId) }
                    // Refresh the list after successful update
                    fetchApplications()
                }
                is Resource.Error -> {
                    val errorMsg = result.message ?: "Update failed"
                     Log.e(TAG, "Error updating app $applicationId: $errorMsg")
                    _state.update { it.copy(updateLoadingId = null, updateErrorId = applicationId to errorMsg) }
                }
                 is Resource.Loading -> {
                     Log.d(TAG, "Update status is loading...")
                 }
            }
        }
    }
} 