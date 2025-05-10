package com.example.pharma_connect_androids.ui.features.main

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
import com.example.pharma_connect_androids.util.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.*


data class HomeScreenState(
    val nearbyPharmacies: List<Pharmacy> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userLocation: Location? = null,
    val locationPermissionGranted: Boolean = false,
    val locationPermissionRequested: Boolean = false, // To track if we've asked once
    val showLocationPermissionRationale: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        checkAndRequestLocationPermission()
    }

    fun checkAndRequestLocationPermission() {
        val context = getApplication<Application>().applicationContext
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                _state.value = _state.value.copy(locationPermissionGranted = true, locationPermissionRequested = true)
                fetchUserLocationAndPharmacies()
            }
            else -> {
                _state.value = _state.value.copy(locationPermissionGranted = false)
            }
        }
    }

    fun onLocationPermissionGranted() {
        _state.value = _state.value.copy(locationPermissionGranted = true, locationPermissionRequested = true, showLocationPermissionRationale = false)
        fetchUserLocationAndPharmacies()
    }

    fun onLocationPermissionDenied(shouldShowRationale: Boolean) {
        if (shouldShowRationale) {
            _state.value = _state.value.copy(locationPermissionGranted = false, showLocationPermissionRationale = true)
        }
        else {
             _state.value = _state.value.copy(locationPermissionGranted = false, error = "Location permission is required to find nearby pharmacies.")
        }
        _state.value = _state.value.copy(locationPermissionRequested = true)
    }
    
    fun userNotifiedAboutRationale(){
        _state.value = _state.value.copy(showLocationPermissionRationale = false)
    }

    fun permissionRequestAttempted(){
        _state.value = _state.value.copy(locationPermissionRequested = true)
    }

    private fun fetchUserLocationAndPharmacies() {
        if (!_state.value.locationPermissionGranted) {
            _state.value = _state.value.copy(error = "Location permission not granted.", isLoading = false)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val location: Location? = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    _state.value = _state.value.copy(userLocation = location)
                    fetchNearbyPharmacies(location.latitude, location.longitude)
                } else {
                    _state.value = _state.value.copy(error = "Could not retrieve current location. Please ensure GPS is enabled.", isLoading = false)
                }
            } catch (e: SecurityException) {
                _state.value = _state.value.copy(error = "Location permission error: ${e.message}", isLoading = false, locationPermissionGranted = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error fetching location: ${e.message}", isLoading = false)
            }
        }
    }

    private fun fetchNearbyPharmacies(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            pharmacyRepository.getNearbyPharmacies(latitude, longitude).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val pharmaciesWithDistance = result.data?.mapNotNull { pharmacy ->
                            pharmacy.latitude?.let { lat ->
                                pharmacy.longitude?.let { lon ->
                                    val distance = calculateDistance(
                                        latitude, longitude,
                                        lat, lon
                                    )
                                    pharmacy.copy(distance = distance)
                                }
                            }
                        }?.sortedBy { it.distance }

                        _state.value = _state.value.copy(
                            nearbyPharmacies = pharmaciesWithDistance ?: emptyList(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Error fetching pharmacies",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
} 