package com.example.pharma_connect_androids.ui.features.pharmacy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.models.PharmacyApplicationRequest
import com.example.pharma_connect_androids.data.repository.ApplicationRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import java.io.File
import android.webkit.MimeTypeMap

data class JoinPharmacyState(
    val ownerName: String = "",
    val pharmacyName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val address: String = "", // Street/Subcity
    val city: String = "",
    val state: String = "", // e.g., Region or State name
    val zipCode: String = "",
    val latitude: Double = 9.03, // Placeholder coordinate
    val longitude: Double = 38.74, // Placeholder coordinate
    val licenseNumber: String = "",
    val licenseImage: String = "", // Placeholder / TODO: Handle image URI
    val pharmacyImage: String = "", // Placeholder / TODO: Handle image URI

    val ownerId: String? = null, // Will be fetched from SessionManager

    val isLoading: Boolean = false,
    val submissionError: String? = null,
    val submissionSuccess: Boolean = false
)

@HiltViewModel
class JoinPharmacyViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(JoinPharmacyState())
    val state: StateFlow<JoinPharmacyState> = _state.asStateFlow()
    private val TAG = "JoinPharmacyVM"

    init {
        // Load owner ID asynchronously
        viewModelScope.launch { 
            loadOwnerId()
        }
    }

    private fun loadOwnerId() {
        // In a real app, check if user is logged in
        val userData = sessionManager.getUserData()
        // Use placeholder if not logged in (shouldn't happen in real flow)
        _state.value = _state.value.copy(ownerId = userData?.userId ?: "placeholder_owner_id")
    }

    // --- Input Change Handlers --- 
    fun onOwnerNameChange(value: String) { _state.value = _state.value.copy(ownerName = value, submissionError = null) }
    fun onPharmacyNameChange(value: String) { _state.value = _state.value.copy(pharmacyName = value, submissionError = null) }
    fun onContactNumberChange(value: String) { _state.value = _state.value.copy(contactNumber = value, submissionError = null) }
    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, submissionError = null) }
    fun onAddressChange(value: String) { _state.value = _state.value.copy(address = value, submissionError = null) }
    fun onCityChange(value: String) { _state.value = _state.value.copy(city = value, submissionError = null) }
    fun onStateChange(value: String) { _state.value = _state.value.copy(state = value, submissionError = null) }
    fun onZipCodeChange(value: String) { _state.value = _state.value.copy(zipCode = value, submissionError = null) }
    fun onLicenseNumberChange(value: String) { _state.value = _state.value.copy(licenseNumber = value, submissionError = null) }
    // TODO: Handlers for LatLng selection and Image selection/upload
    fun onLocationSelected(lat: Double, lng: Double) { 
         Log.d("JoinPharmacyVM", "Location Selected (Placeholder): Lat=$lat, Lng=$lng")
        _state.value = _state.value.copy(latitude = lat, longitude = lng)
     }
     fun onLicenseImageSelected(uriOrUrl: String) { // Placeholder for URI or uploaded URL
         Log.d(TAG, "onLicenseImageSelected called with: $uriOrUrl")
         val previousState = _state.value
         _state.value = _state.value.copy(licenseImage = uriOrUrl)
         Log.d(TAG, "State after license image update: ${_state.value}")
     }
    fun onPharmacyImageSelected(uriOrUrl: String) { // Placeholder for URI or uploaded URL
         Log.d(TAG, "onPharmacyImageSelected called with: $uriOrUrl")
         val previousState = _state.value
         _state.value = _state.value.copy(pharmacyImage = uriOrUrl)
          Log.d(TAG, "State after pharmacy image update: ${_state.value}")
     }
    // --- End Input Handlers --- 

    fun submitApplication() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = currentState.copy(isLoading = true, submissionError = null, submissionSuccess = false)

            // Validation (basic examples)
            if (currentState.pharmacyName.isBlank() || currentState.email.isBlank() || /* ... other required fields */ currentState.licenseImage.isBlank() || currentState.pharmacyImage.isBlank()) {
                _state.value = currentState.copy(isLoading = false, submissionError = "Please fill all required fields and select images.")
                return@launch
            }
            
            // --- Temporary Workaround: Use placeholder URLs --- 
            val licenseImageUrl = "https://via.placeholder.com/150/0000FF/808080?text=LicensePlaceholder"
            val pharmacyImageUrl = "https://via.placeholder.com/150/FF0000/FFFFFF?text=PharmacyPlaceholder"
            // --- End Temporary Workaround ---
            
            // TODO (Real Implementation): Implement actual image upload here
            // 1. Get URLs from uploaded images (licenseImageUrl, pharmacyImageUrl)
            //    - This would likely involve calling another repository function
            //    - Handle upload errors

            // Create request body AFTER getting image URLs
            val request = PharmacyApplicationRequest(
                ownerName = currentState.ownerName,
                pharmacyName = currentState.pharmacyName,
                contactNumber = currentState.contactNumber,
                email = currentState.email,
                address = currentState.address,
                city = currentState.city,
                state = currentState.state,
                zipCode = currentState.zipCode,
                latitude = currentState.latitude,
                longitude = currentState.longitude,
                licenseNumber = currentState.licenseNumber,
                licenseImage = licenseImageUrl,
                pharmacyImage = pharmacyImageUrl,
                ownerId = currentState.ownerId ?: ""
            )

            // Check ownerId again before submitting
             if (request.ownerId.isBlank()) {
                 _state.value = currentState.copy(isLoading = false, submissionError = "Cannot submit application: User ID not found. Please log in again.")
                 return@launch
             }

            // Make the API call
            when (val result = applicationRepository.submitApplication(request)) {
                is Resource.Success -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        submissionSuccess = true,
                        submissionError = null
                    )
                }
                is Resource.Error -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        submissionError = result.message ?: "Application submission failed",
                        submissionSuccess = false
                    )
                }
                 is Resource.Loading -> { /* Optional */ }
            }
        }
    }

    // Reset success state after navigation/message handled
    fun resetSubmissionSuccess() {
        _state.value = _state.value.copy(submissionSuccess = false)
    }

    // Reset error state if needed, e.g., when user starts typing again
    fun clearSubmissionError() {
        if (_state.value.submissionError != null) {
            _state.value = _state.value.copy(submissionError = null)
        }
    }

} 