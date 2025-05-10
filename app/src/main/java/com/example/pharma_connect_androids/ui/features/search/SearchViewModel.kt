package com.example.pharma_connect_androids.ui.features.search

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.SearchRequest
import com.example.pharma_connect_androids.data.models.SearchResultItem
import com.example.pharma_connect_androids.data.repository.SearchRepository
import com.example.pharma_connect_androids.data.repository.CartRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
    val showLocationPermissionRationale: Boolean = false, // To show rationale dialog if needed
    // Cart related state
    val isAddingToCart: Boolean = false, // To show loading for add to cart action
    val addToCartMessage: String? = null // For displaying messages like "Added to cart" or errors
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
    private val searchRepository: SearchRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private val _typedQueryFlow = MutableStateFlow("") // Renamed from _searchQueryFlow

    // To store raw results from API before filtering
    private var rawSearchResults: List<SearchResultItem> = emptyList()
    private var currentSearchJob: Job? = null // To keep track of ongoing search

    companion object {
        const val NO_MEDICINE_FOUND_MSG_PREFIX = "No medicine found for: "
    }

    init {
        _typedQueryFlow // Changed from _searchQueryFlow
            .debounce(500)
            .onEach { typedQuery -> // query from the text field input after debounce
                Log.d("SearchViewModel", "Debounced typed query: $typedQuery. Current state query: ${_state.value.searchQuery}")
                // Only proceed if this debounced query matches the *current* query in the state.
                if (typedQuery.isNotBlank() && typedQuery == _state.value.searchQuery) {
                    performSearch(typedQuery, isDebouncedSearch = true)
                } else if (typedQuery.isBlank() && typedQuery == _state.value.searchQuery) {
                    clearSearchResultsAndError()
                } else {
                    Log.d("SearchViewModel", "Debounced search for '$typedQuery' skipped, current active query is '${_state.value.searchQuery}'")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun clearSearchResultsAndError() {
        rawSearchResults = emptyList()
        _state.value = _state.value.copy(searchResults = emptyList(), searchError = null, isLoading = false)
    }

    fun setInitialSearchQuery(query: String) {
        Log.d("SearchViewModel", "setInitialSearchQuery called with query: $query. Current state query: ${_state.value.searchQuery}")
        
        currentSearchJob?.cancel() // Cancel any ongoing search (debounced or direct)
        rawSearchResults = emptyList() // Clear raw search results immediately
        
        // DO NOT update _typedQueryFlow here. It's for user typing.
        // The TextField will get its value from _state.searchQuery.

        // Set the state directly for the new query
        _state.value = _state.value.copy(
            searchQuery = query, 
            searchResults = emptyList(), 
            searchError = null, 
            isLoading = false 
        )

        if (query.isNotBlank()) {
            performSearch(query, isDebouncedSearch = false) // Directly perform search
        } else {
            clearSearchResultsAndError() // Clear if the initial query is blank
        }
    }

    fun onSearchQueryChange(query: String) {
        Log.d("SearchViewModel", "onSearchQueryChange: $query")
        // Update state for TextField responsiveness
        _state.value = _state.value.copy(searchQuery = query) 
        // Feed the typed query to the debouncing flow
        _typedQueryFlow.value = query 
        
        if (query.isBlank()) {
             // Immediate visual clear might be desired, or let debounce handle it.
             // Current clearSearchResultsAndError in debounce for blank will set isLoading = false.
             // If we clear here, it's more immediate for UI, but debounce will still run.
             // The debounce guard (typedQuery == _state.value.searchQuery) will ensure it doesn't misbehave.
             clearSearchResultsAndError() 
        }
    }

    // Called by a direct action like a search button
    fun triggerSearchNow() {
        val currentQuery = _state.value.searchQuery
        Log.d("SearchViewModel", "triggerSearchNow called. Current query: $currentQuery")
        if (currentQuery.isNotBlank()) {
            currentSearchJob?.cancel() // Cancel previous if any
            performSearch(currentQuery, isDebouncedSearch = false)
        }
    }
    
    // Main search execution logic
    private fun performSearch(query: String, isDebouncedSearch: Boolean) {
        Log.d("SearchViewModel", "performSearch called for query: '$query', isDebounced: $isDebouncedSearch. Current state query: '${_state.value.searchQuery}'")

        currentSearchJob?.cancel() // Cancel any previous search job immediately.

        // Primary Guard: If the query this performSearch was invoked with is no longer the active query in the state, abort.
        if (query != _state.value.searchQuery) {
            Log.w("SearchViewModel", "performSearch for '$query' is stale (current state query is '${_state.value.searchQuery}'). Aborting this search.")
            // If this job was cancelled, the new job should handle isLoading.
            // If this job wasn't cancelled but is stale, and it was the one that set isLoading = true,
            // then isLoading might be stuck. However, any action that changes _state.value.searchQuery
            // should also manage isLoading (e.g. setInitialSearchQuery sets it to false, then true via new performSearch).
            return
        }

        currentSearchJob = viewModelScope.launch {
            // Set loading true for THIS query's search attempt and clear previous error.
            // Results are cleared by callers like setInitialSearchQuery or handled by success/error branches.
            _state.value = _state.value.copy(isLoading = true, searchError = null)
            
            searchRepository.searchMedicine(SearchRequest(medicineName = query))
                .collect { resource ->
                    // Secondary Guard: Only update state if the result is for the currently active query.
                    if (query == _state.value.searchQuery) {
                        when (resource) {
                            is Resource.Loading -> {
                                // isLoading is already true.
                            }
                            is Resource.Success -> {
                                rawSearchResults = resource.data?.data ?: emptyList()
                                // Set isLoading false *before* any further processing like distance calculation.
                                _state.value = _state.value.copy(isLoading = false) 
                                if (rawSearchResults.isEmpty() && query.isNotBlank()) {
                                    _state.value = _state.value.copy(
                                        searchResults = emptyList(),
                                        searchError = "$NO_MEDICINE_FOUND_MSG_PREFIX'$query'"
                                        // isLoading is already false from above
                                    )
                                } else {
                                    updateSearchResultsWithDistances() // This will update searchResults in state.
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
                    } else {
                        Log.w("SearchViewModel", "Search result for '$query' received, but current query is now '${_state.value.searchQuery}'. Discarding result.")
                        // If this job is still running but is for an old query, its isLoading contribution should be ignored
                        // as the new query's job will manage isLoading.
                    }
                }
            
            // Safeguard: After collection, if this job was for the active query and it's still loading, set loading to false.
            // This handles cases where the flow might complete without hitting Success/Error that explicitly sets isLoading = false.
            if (query == _state.value.searchQuery && _state.value.isLoading) {
                Log.d("SearchViewModel", "Search for '$query' coroutine ended, ensuring isLoading is false.")
                _state.value = _state.value.copy(isLoading = false)
            }
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

    // --- Cart Functionality ---
    fun addToCart(inventoryId: String) {
        viewModelScope.launch {
            cartRepository.addToCart(inventoryId).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isAddingToCart = true, addToCartMessage = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isAddingToCart = false,
                            addToCartMessage = result.data?.userId?.let { "Added to cart successfully!" } ?: "Added to cart!" // Example message
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isAddingToCart = false,
                            addToCartMessage = result.message ?: "Failed to add to cart"
                        )
                    }
                }
            }.launchIn(viewModelScope) // Use launchIn for a flow that should be collected for its side effects
        }
    }

    fun clearAddToCartMessage() {
        _state.value = _state.value.copy(addToCartMessage = null)
    }
}