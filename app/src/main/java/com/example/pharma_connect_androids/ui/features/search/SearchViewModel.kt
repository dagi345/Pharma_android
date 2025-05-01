package com.example.pharma_connect_androids.ui.features.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.SearchRequest
import com.example.pharma_connect_androids.data.models.SearchResultItem
import com.example.pharma_connect_androids.data.repository.SearchRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
    val searchResults: List<SearchResultItem> = emptyList(),
    // Filter States
    val selectedPriceRange: PriceRange? = null,
    val selectedLocation: String? = null,
    val isNearMeChecked: Boolean = false
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

    init {
        _searchQueryFlow
            .debounce(500)
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    _state.value = _state.value.copy(searchResults = emptyList(), searchError = null)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        _searchQueryFlow.value = query
    }

    fun triggerSearchNow() {
        if (_state.value.searchQuery.isNotBlank()) {
            performSearch(_state.value.searchQuery)
        }
    }

    // --- Filter Handlers --- 
    fun onPriceRangeSelected(range: PriceRange?) {
        val actualRange = if (range == (0.0 to null)) null else range // Treat "Any Price" as null filter
        Log.d("SearchViewModel", "Price Range Selected: $actualRange")
        _state.value = _state.value.copy(selectedPriceRange = actualRange)
        // TODO: Trigger re-filtering of results based on current filters
        // For now, just update state. We can optionally refilter placeholder data:
         _state.value = _state.value.copy(searchResults = filterPlaceholderResults(_state.value))
    }

    fun onLocationSelected(location: String?) {
        val actualLocation = if (location == "Any Location") null else location // Treat "Any Location" as null filter
        Log.d("SearchViewModel", "Location Selected: $actualLocation")
        _state.value = _state.value.copy(selectedLocation = actualLocation)
        // TODO: Trigger re-filtering of results based on current filters
        _state.value = _state.value.copy(searchResults = filterPlaceholderResults(_state.value))
    }

    fun onNearMeToggled(isChecked: Boolean) {
        Log.d("SearchViewModel", "Near Me Toggled: $isChecked")
        _state.value = _state.value.copy(isNearMeChecked = isChecked)
        // TODO: Trigger re-filtering of results based on current filters
         _state.value = _state.value.copy(searchResults = filterPlaceholderResults(_state.value))
    }
    // --- End Filter Handlers --- 

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, searchError = null)
            delay(1000) // Simulate network call

            // Simulate results and apply initial filters
            val initialResults = createPlaceholderResults(query)
            _state.value = _state.value.copy(
                isLoading = false,
                searchResults = filterPlaceholderResults(_state.value.copy(searchResults = initialResults)), // Apply current filters to new results
                searchError = null
            )
            // Real repository call would go here later
        }
    }

    // Helper function to create placeholder search results
    private fun createPlaceholderResults(query: String): List<SearchResultItem> {
         if (query.contains("not found", ignoreCase = true)) return emptyList()
         
         return List(10) { index -> // Generate more items for filtering demo
             val locationIndex = index % locations.filter { it != "Any Location" }.size // Cycle through locations
             val location = locations.filter { it != "Any Location" }[locationIndex]
             SearchResultItem(
                 pharmacyName = "$location Pharmacy ${index % 3 + 1}",
                 address = "${100 + index * 10} $location St, Addis Ababa",
                 price = (10.0 + index * 23.5) % 250, // Prices up to 250
                 distance = (index + 1) * 1.5, // Distance up to 15km
                 time = (index + 1) * 3.0,
                 photo = null,
                 pharmacyId = "pharmacy_${location}_${index + 1}",
                 inventoryId = "inventory_${query}_${index + 1}"
             )
         }
     }

    // Helper function to simulate filtering on the placeholder data
    private fun filterPlaceholderResults(currentState: SearchScreenState): List<SearchResultItem> {
        val originalResults = createPlaceholderResults(currentState.searchQuery) // Re-generate base list
        
        return originalResults.filter { item ->
            val priceMatch = currentState.selectedPriceRange?.let { range ->
                val lowerBound = range.first
                val upperBound = range.second
                item.price >= lowerBound && (upperBound == null || item.price <= upperBound)
            } ?: true // If null range, always true

            val locationMatch = currentState.selectedLocation?.let {
                 item.address.contains(it, ignoreCase = true)
             } ?: true // If null location, always true

            val nearMeMatch = if (currentState.isNearMeChecked) {
                 item.distance != null && item.distance <= 5.0 // Example: Near Me means <= 5km
             } else true // If not checked, always true

            priceMatch && locationMatch && nearMeMatch
        }
    }
} 