package com.example.pharma_connect_androids.ui.features.search

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.SearchRequest
import com.example.pharma_connect_androids.data.models.SearchResultItem
import com.example.pharma_connect_androids.data.repository.SearchRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define Price Range Pair typealias for clarity
typealias PriceRange = Pair<Double, Double?> // Nullable upper bound for ranges like "200+"

// State for the Search Screen
data class SearchScreenState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val searchError: String? = null,
    var searchResults: List<SearchResultItem> = emptyList(), // Made var to allow direct update after distance calc
    // Filter States
    val selectedPriceRange: PriceRange? = null,
    val selectedLocation: String? = null,
    val currentUserLocation: Location? = null, // User's current location
    val locationPermissionRequested: Boolean = false, // To track if we've asked for permission at least once
    val showLocationPermissionRationale: Boolean = false // To show rationale dialog if needed
)

// Predefined filter options
val priceRanges: List<Pair<String, PriceRange>> = listOf(
    "Any Price" to (0.0 to null), // Represents no price filter
    "Br 0 - 50" to (0.0 to 50.0),
    "Br 50 - 100" to (50.0 to 100.0),
    "Br 100 - 200" to (100.0 to 200.0),
    "Br 200+" to (200.0 to null)
)

val locations: List<String> = listOf(
    "Any Location", // Represents no location filter
    "Bole",
    "CMC",
    "Piazza",
    "Gerji",
    "Ayat"
)

@OptIn(FlowPreview::class) // Needed for debounce
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private val _searchQueryFlow = MutableStateFlow("")

    // To store raw results from API before filtering
    private var rawSearchResults: List<SearchResultItem> = emptyList()

    companion object {
        const val NO_MEDICINE_FOUND_MSG_PREFIX = "No medicine found for: "
    }

    init {
        _searchQueryFlow
            .debounce(500)
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    rawSearchResults = emptyList()
                    _state.value = _state.value.copy(searchResults = emptyList(), searchError = null, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    fun setInitialSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query, searchResults = emptyList(), searchError = null, isLoading = true)
        _searchQueryFlow.value = query
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.isBlank()) {
            rawSearchResults = emptyList()
            _state.value = _state.value.copy(searchResults = emptyList(), searchError = null, isLoading = false)
            _searchQueryFlow.value = "" 
        } else {
            _searchQueryFlow.value = query
        }
    }

    fun triggerSearchNow() {
        if (_state.value.searchQuery.isNotBlank()) {
            performSearch(_state.value.searchQuery)
        }
    }

    // --- Location Permission and Data Callbacks ---
    fun onLocationPermissionGranted() {
        // UI will call this, then UI will fetch location and call setUserLocation
        _state.value = _state.value.copy(locationPermissionRequested = true, showLocationPermissionRationale = false)
        // Actual fetching will be triggered from UI, which then calls setUserLocation
    }

    fun onLocationPermissionDenied(shouldShowRationale: Boolean) {
        _state.value = _state.value.copy(
            locationPermissionRequested = true, // User has responded to request
            showLocationPermissionRationale = shouldShowRationale
        )
    }

    fun setUserLocation(location: Location) {
        _state.value = _state.value.copy(currentUserLocation = location)
        updateSearchResultsWithDistances() // Recalculate distances with new location
    }
    
    fun userNotifiedAboutRationale(){
        _state.value = _state.value.copy(showLocationPermissionRationale = false)
    }

    // --- Filter Handlers --- 
    fun onPriceRangeSelected(range: PriceRange?) {
        val actualRange = if (range == (0.0 to null)) null else range // Treat "Any Price" as null filter
        Log.d("SearchViewModel", "Price Range Selected: $actualRange")
        _state.value = _state.value.copy(selectedPriceRange = actualRange)
        updateSearchResultsWithDistances() // Apply filters and potentially update distances
    }

    fun onLocationSelected(location: String?) {
        val actualLocation = if (location == "Any Location") null else location // Treat "Any Location" as null filter
        Log.d("SearchViewModel", "Location Selected: $actualLocation")
        _state.value = _state.value.copy(selectedLocation = actualLocation)
        updateSearchResultsWithDistances() // Apply filters and potentially update distances
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, searchError = null)
            
            searchRepository.searchMedicine(SearchRequest(medicineName = query))
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                        is Resource.Success -> {
                            rawSearchResults = resource.data?.data ?: emptyList()
                            if (rawSearchResults.isEmpty() && query.isNotBlank()) {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    searchResults = emptyList(),
                                    searchError = "$NO_MEDICINE_FOUND_MSG_PREFIX'$query'"
                                )
                            } else {
                                // Update state with possibly distance-updated results
                                updateSearchResultsWithDistances() 
                            }
                        }
                        is Resource.Error -> {
                            rawSearchResults = emptyList()
                            _state.value = _state.value.copy(
                                isLoading = false,
                                searchError = resource.message ?: "An unknown error occurred",
                                searchResults = emptyList()
                            )
                        }
                    }
                }
        }
    }

    private fun updateSearchResultsWithDistances() {
        val currentRawResults = rawSearchResults
        val userLocation = _state.value.currentUserLocation

        Log.d("SearchViewModel", "Updating distances. User location: ${userLocation?.latitude}, ${userLocation?.longitude}")
        Log.d("SearchViewModel", "Raw results count: ${currentRawResults.size}")

        val processedResults = currentRawResults.map { item ->
            val pharmacyLat = item.latitude
            val pharmacyLon = item.longitude
            var calculatedDistance: Double? = null

            if (userLocation != null && pharmacyLat != null && pharmacyLon != null) {
                val pharmacyLocation = Location("").apply {
                    latitude = pharmacyLat
                    longitude = pharmacyLon
                }
                calculatedDistance = userLocation.distanceTo(pharmacyLocation) / 1000.0 // Convert meters to KM
                Log.d("SearchViewModel", "Item: ${item.pharmacyName}, PharmLat: $pharmacyLat, PharmLon: $pharmacyLon, Dist: $calculatedDistance km")
            } else {
                Log.d("SearchViewModel", "Item: ${item.pharmacyName}, Missing location data. UserLoc: $userLocation, PharmLat: $pharmacyLat, PharmLon: $pharmacyLon")
            }
            
            item.copy(
                distance = calculatedDistance
                // Time calculation could be added here
            )
        }
        // Apply other client-side filters (price, location name) after distances are potentially added
        val filteredResults = applyClientSideFilters(processedResults)       
        _state.value = _state.value.copy(
            searchResults = filteredResults,
            isLoading = false, // Ensure loading is false after processing
            searchError = if (filteredResults.isEmpty() && rawSearchResults.isNotEmpty() && _state.value.searchQuery.isNotBlank()) {
                                // This case means filters made it empty, not that the medicine wasn't found
                                _state.value.searchError // Keep existing error or null
                          } else if (rawSearchResults.isEmpty() && _state.value.searchQuery.isNotBlank()){
                                "$NO_MEDICINE_FOUND_MSG_PREFIX'${_state.value.searchQuery}'"
                          } else {
                                null
                          }
            )
    }

    private fun applyClientSideFilters(resultsToFilter: List<SearchResultItem>): List<SearchResultItem> {
        val currentState = _state.value
        return resultsToFilter.filter { item ->
            val priceMatch = currentState.selectedPriceRange?.let { range ->
                val lowerBound = range.first
                val upperBound = range.second
                item.price >= lowerBound && (upperBound == null || item.price <= upperBound)
            } ?: true // If null range, always true

            val locationMatch = currentState.selectedLocation?.let {
                 item.address.contains(it, ignoreCase = true)
             } ?: true // If null location, always true

            priceMatch && locationMatch
        }
    }

    // Placeholder functions are now removed.
}