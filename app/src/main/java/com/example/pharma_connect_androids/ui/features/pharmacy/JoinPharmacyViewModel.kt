package com.example.pharma_connect_androids.ui.features.pharmacy

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.models.PharmacyApplicationRequest
import com.example.pharma_connect_androids.data.models.UpdatePharmacyRequest
import com.example.pharma_connect_androids.data.repository.ApplicationRepository
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
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
    val licenseImage: String = "", // URI or URL
    val pharmacyImage: String = "", // URI or URL
    val googleMapsLink: String = "", // New field for Maps Link

    val ownerId: String? = null, // Will be fetched from SessionManager

    val isLoading: Boolean = false,
    val submissionError: String? = null,
    val submissionSuccess: Boolean = false,
    val isUpdateMode: Boolean = false, // Flag for update mode
    val isLoadingDetails: Boolean = false, // Flag for loading initial details
    val linkParseError: String? = null // New field for link parsing errors
)

@HiltViewModel
class JoinPharmacyViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(JoinPharmacyState())
    val state: StateFlow<JoinPharmacyState> = _state.asStateFlow()
    private val TAG = "JoinPharmacyVM"

    // Check if we are in update mode by getting pharmacyId from navigation args
    private val pharmacyIdToUpdate: String? = savedStateHandle.get<String>("pharmacyId")

    init {
        loadOwnerId() // Load owner ID regardless of mode
        // If pharmacyId exists, set update mode and fetch details
        pharmacyIdToUpdate?.let {
            if (it.isNotBlank()) {
                _state.value = _state.value.copy(isUpdateMode = true)
                fetchPharmacyDetailsForUpdate(it)
            }
        }
    }

    private fun loadOwnerId() {
        val userData = sessionManager.getUserData()
        // Only set ownerId if it's not already set (e.g., by fetchPharmacyDetailsForUpdate)
        if (_state.value.ownerId == null) {
             _state.value = _state.value.copy(ownerId = userData?.userId)
        }
    }

    private fun fetchPharmacyDetailsForUpdate(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingDetails = true)
            when(val result = pharmacyRepository.getPharmacyById(id)) {
                is Resource.Success -> {
                    result.data?.let { pharmacy ->
                        _state.value = _state.value.copy(
                            ownerName = pharmacy.ownerName ?: "", // Use fetched owner name
                            pharmacyName = pharmacy.name,
                            contactNumber = pharmacy.contactNumber,
                            email = pharmacy.email,
                            address = pharmacy.address,
                            city = pharmacy.city ?: "",
                            state = pharmacy.state ?: "",
                            zipCode = pharmacy.zipcode ?: "",
                            latitude = pharmacy.latitude ?: 9.03,
                            longitude = pharmacy.longitude ?: 38.74,
                            licenseNumber = pharmacy.licenseNumber ?: "",
                            // Assume 'image' from Pharmacy model maps to pharmacyImage field
                            // License image might need separate handling/field if distinct
                            pharmacyImage = pharmacy.image ?: "", 
                            licenseImage = pharmacy.image ?: "", // Placeholder: Using same image, adjust if needed
                            ownerId = pharmacy.ownerId, // Use fetched owner ID
                            isLoadingDetails = false,
                            submissionError = null, // Clear any previous errors
                            googleMapsLink = pharmacy.googleMapsLink ?: "", // Use fetched googleMapsLink
                            linkParseError = null // Clear any previous link parse errors
                        )
                    } ?: run {
                         _state.value = _state.value.copy(isLoadingDetails = false, submissionError = "Failed to load pharmacy details.")
                    }
                }
                is Resource.Error -> {
                     _state.value = _state.value.copy(isLoadingDetails = false, submissionError = result.message ?: "Error loading pharmacy details.")
                }
                is Resource.Loading -> { /* Handled by isLoadingDetails flag */ }
            }
        }
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
     fun onLicenseImageSelected(uriOrUrl: String) { _state.value = _state.value.copy(licenseImage = uriOrUrl) }
    fun onPharmacyImageSelected(uriOrUrl: String) { _state.value = _state.value.copy(pharmacyImage = uriOrUrl) }
    
    fun onGoogleMapsLinkChange(link: String) {
        _state.value = _state.value.copy(googleMapsLink = link, linkParseError = null)
        // Attempt to parse immediately or on focus loss/button click?
        // Let's parse immediately for simplicity, but debounce might be better in real app.
        parseCoordinatesFromLink(link)
    }
    // --- End Input Handlers ---

    private fun parseCoordinatesFromLink(link: String) {
        // Regex to find patterns like @<lat>,<lng>,... in Google Maps URLs
        // Example: https://www.google.com/maps/place/SomePlace/@9.12345,-38.54321,15z
        val regex = "/@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+),?".toRegex()
        val match = regex.find(link)

        if (match != null && match.groupValues.size == 3) {
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) {
                Log.d(TAG, "Parsed coordinates: Lat=$lat, Lng=$lng")
                _state.value = _state.value.copy(latitude = lat, longitude = lng, linkParseError = null)
            } else {
                _state.value = _state.value.copy(linkParseError = "Could not parse numbers from link")
            }
        } else {
            // Clear coordinates if link is invalid or doesn't contain coords
             _state.value = _state.value.copy(linkParseError = if(link.isNotBlank()) "Invalid or unsupported Google Maps link format" else null)
             // Optionally reset lat/lng to defaults if link becomes invalid?
             // _state.value = _state.value.copy(latitude = 9.03, longitude = 38.74) 
        }
    }

    // Renamed for clarity
    fun submitForm() {
        if (_state.value.isUpdateMode) {
            submitUpdate()
        } else {
            submitRegistration()
        }
    }

    private fun submitRegistration() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = currentState.copy(isLoading = true, submissionError = null, submissionSuccess = false)
            
            if (currentState.pharmacyName.isBlank() || currentState.ownerName.isBlank() /* ... other validation */) {
                _state.value = currentState.copy(isLoading = false, submissionError = "Please fill all required fields.")
                return@launch
            }
            // TODO: Implement real image upload and URL retrieval before creating request
            val licenseImageUrl = "PLACEHOLDER_LICENSE_URL"
            val pharmacyImageUrl = "PLACEHOLDER_PHARMACY_URL"
             if (currentState.ownerId.isNullOrBlank()) {
                  _state.value = currentState.copy(isLoading = false, submissionError = "User ID not found. Please log in again.")
                  return@launch
              }

            val request = PharmacyApplicationRequest(
                 ownerName = currentState.ownerName, pharmacyName = currentState.pharmacyName, contactNumber = currentState.contactNumber, email = currentState.email, address = currentState.address, city = currentState.city, state = currentState.state, zipCode = currentState.zipCode, latitude = currentState.latitude, longitude = currentState.longitude, licenseNumber = currentState.licenseNumber, licenseImage = licenseImageUrl, pharmacyImage = pharmacyImageUrl, ownerId = currentState.ownerId
            )

            when (val result = applicationRepository.submitApplication(request)) {
                is Resource.Success -> { _state.value = _state.value.copy(isLoading = false, submissionSuccess = true, submissionError = null) }
                is Resource.Error -> { _state.value = _state.value.copy(isLoading = false, submissionError = result.message ?: "Application submission failed", submissionSuccess = false) }
                is Resource.Loading -> { /* Handled by isLoading flag */ }
            }
        }
    }

    private fun submitUpdate() {
        val idToUpdate = pharmacyIdToUpdate ?: return // Exit if ID is missing for update
        val currentState = _state.value // Capture state before launch

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, submissionError = null, submissionSuccess = false)

             if (currentState.pharmacyName.isBlank() || currentState.ownerName.isBlank() /* ... other validation */) {
                 _state.value = _state.value.copy(isLoading = false, submissionError = "Please fill all required fields.")
                 return@launch
             }
            
            // TODO: Image upload logic still needed here.
            // Using current image values (which could be old URLs or new URIs)
            val finalLicenseUrl = if(currentState.licenseImage.startsWith("http")) currentState.licenseImage else "PLACEHOLDER_UPDATED_LICENSE_URL"
            val finalPharmacyUrl = if(currentState.pharmacyImage.startsWith("http")) currentState.pharmacyImage else "PLACEHOLDER_UPDATED_PHARMACY_URL"

            val request = UpdatePharmacyRequest(
                 ownerName = currentState.ownerName, 
                 name = currentState.pharmacyName,
                 contactNumber = currentState.contactNumber, 
                 email = currentState.email, 
                 address = currentState.address, 
                 city = currentState.city, 
                 state = currentState.state, 
                 zipCode = currentState.zipCode, 
                 latitude = currentState.latitude, 
                 longitude = currentState.longitude, 
                 licenseNumber = currentState.licenseNumber, 
                 licenseImage = finalLicenseUrl, 
                 pharmacyImage = finalPharmacyUrl, 
                 ownerId = currentState.ownerId
            )

            // Explicitly check result before setting success state
             when (val result = pharmacyRepository.updatePharmacy(idToUpdate, request)) {
                 is Resource.Success -> {
                     _state.value = _state.value.copy(
                         isLoading = false, 
                         submissionSuccess = true, // Only set success on actual success
                         submissionError = null
                     )
                 }
                 is Resource.Error -> {
                     _state.value = _state.value.copy(
                         isLoading = false, 
                         submissionError = result.message ?: "Pharmacy update failed",
                         submissionSuccess = false // Ensure success is false on error
                     )
                 }
                 is Resource.Loading -> { /* Handled by isLoading flag, no state change needed here */ }
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