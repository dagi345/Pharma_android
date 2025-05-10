package com.example.pharma_connect_androids.ui.features.search

import android.Manifest // Required for ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.AddShoppingCart // Added for Add to Cart button
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Required for FusedLocationProviderClient
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat // Required for checkSelfPermission
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pharma_connect_androids.R // For placeholder image
import com.example.pharma_connect_androids.data.models.SearchResultItem
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import com.google.android.gms.location.LocationServices // Required for FusedLocationProviderClient
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    initialQuery: String? = null,
    onNavigateToPharmacyDetail: (pharmacyId: String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle Add to Cart Messages
    LaunchedEffect(state.addToCartMessage) {
        state.addToCartMessage?.let {
            message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearAddToCartMessage() // Clear message after showing
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                viewModel.onLocationPermissionGranted()
                fetchLastLocation(context, fusedLocationClient, viewModel)
            } else {
                // viewModel.onLocationPermissionDenied(shouldShowRationale) // We need to figure out shouldShowRationale - for now, just basic denial
                // For simplicity, just log for now. A real app would use ActivityCompat.shouldShowRequestPermissionRationale
                Log.d("SearchScreen", "Location permission denied by user.")
                viewModel.onLocationPermissionDenied(false) // Assuming no rationale to show yet by default
            }
        }
    )

    LaunchedEffect(initialQuery) {
        Log.d("SearchScreen", "LaunchedEffect triggered. initialQuery: $initialQuery")
        if (!initialQuery.isNullOrBlank()) {
            viewModel.setInitialSearchQuery(initialQuery)
        } else {
            // Optional: Clear search if navigating to search tab with no query
            // This depends on whether SearchViewModel's init already clears or if _searchQueryFlow handles it.
            // For now, if initialQuery is blank, we do nothing here, assuming default state is fine.
            // viewModel.onSearchQueryChange("") // Could be one way to clear
        }
    }
    
    // Attempt to get location if permission is already granted when screen loads or when results are available
    LaunchedEffect(state.searchResults) { // Re-check if permission granted when results change
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.onLocationPermissionGranted() // Ensure state is updated
            fetchLastLocation(context, fusedLocationClient, viewModel)
        }
    }

    if (state.showLocationPermissionRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.userNotifiedAboutRationale() },
            title = { Text("Location Permission Needed") },
            text = { Text("This app needs location permission to show distances to pharmacies. Please grant the permission.") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.userNotifiedAboutRationale()
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.userNotifiedAboutRationale() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // Padding for content
    ) {
        // Top Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            placeholder = { Text("Search for Medicine") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
            trailingIcon = {
                 if (state.isLoading) { // Show loading indicator in search bar
                     CircularProgressIndicator(modifier = Modifier.size(24.dp))
                 }
            },
            singleLine = true
        )

        // Filter Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Price Range Dropdown
            FilterDropdown(
                label = "Price",
                options = priceRanges.map { it.first }, // Display names
                selectedOption = priceRanges.find { it.second == state.selectedPriceRange }?.first ?: "All",
                onOptionSelected = { selectedLabel ->
                    val selectedRangePair = priceRanges.find { it.first == selectedLabel }?.second
                    viewModel.onPriceRangeSelected(selectedRangePair)
                },
                modifier = Modifier.weight(1f) // Distribute space
            )

            // Location Dropdown
            FilterDropdown(
                label = "Location",
                options = locations,
                selectedOption = state.selectedLocation ?: "All",
                onOptionSelected = { selectedLocation ->
                    viewModel.onLocationSelected(selectedLocation)
                },
                 modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Permission Prompt if not yet requested and not granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            !state.locationPermissionRequested) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Show distance to pharmacies?", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                        Text("Enable Location")
                    }
                }
            }
        }

        // Results Area
        when {
            state.isLoading -> {
                 // Show a loading indicator centered below filters
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                 }
             }
             state.searchError != null -> {
                 // Show error message
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.searchError?.startsWith(SearchViewModel.NO_MEDICINE_FOUND_MSG_PREFIX) == true) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.searchError!!, textAlign = TextAlign.Center)
                        } else {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.searchError ?: "An error occurred", textAlign = TextAlign.Center)
                        }
                     }
                 }
             }
             state.searchResults.isNotEmpty() -> {
                 // Show results list
                 LazyColumn(
                     modifier = Modifier.fillMaxSize(),
                     verticalArrangement = Arrangement.spacedBy(12.dp),
                     contentPadding = PaddingValues(bottom = 16.dp) // Padding at the bottom
                 ) {
                     item { // Header for results (optional)
                         Text(
                             text = "Results for: ${state.searchQuery}",
                             style = MaterialTheme.typography.titleMedium,
                             modifier = Modifier.padding(bottom = 8.dp)
                         )
                     }
                     items(state.searchResults, key = { it.inventoryId }) { resultItem ->
                         SearchResultItemCard(
                             item = resultItem,
                             isAddingToCart = state.isAddingToCart, // Pass loading state
                             onViewDetailClick = {
                                 onNavigateToPharmacyDetail(resultItem.pharmacyId)
                             },
                             onAddToCartClick = { inventoryId -> // Pass callback
                                 viewModel.addToCart(inventoryId)
                             }
                         )
                     }
                 }
             }
        }
    }
}

private fun fetchLastLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    viewModel: SearchViewModel
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                location?.let {
                    viewModel.setUserLocation(it)
                    Log.d("SearchScreen", "Location fetched: Lat=${it.latitude}, Lon=${it.longitude}")
                } ?: run {
                    Log.d("SearchScreen", "Last location is null. Consider requesting location updates.")
                    // TODO: Implement requestLocationUpdates if lastLocation is often null
                }
            }
            .addOnFailureListener {
                Log.e("SearchScreen", "Failed to get location.", it)
                // Potentially update state to show location fetch error
            }
    } else {
        Log.d("SearchScreen", "Location permission not granted at time of fetch call.")
    }
}

// Generic Exposed Dropdown Menu for Filters
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {}, // Text field is read-only
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor() // Important for anchoring the dropdown
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Composable for displaying a single search result item
@Composable
fun SearchResultItemCard(
    item: SearchResultItem,
    isAddingToCart: Boolean, // Added for loading state
    onViewDetailClick: () -> Unit,
    onAddToCartClick: (inventoryId: String) -> Unit // Added callback
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) { // Make row height based on content
            AsyncImage(
                model = item.photo,
                contentDescription = "Pharmacy Image",
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo),
                modifier = Modifier
                    .weight(0.4f) // Image takes ~40% width
                    .aspectRatio(1f) // Make image square
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            // Content Details
            Column(
                modifier = Modifier
                    .weight(0.6f) // Text content takes ~60% width
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.pharmacyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                   Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(16.dp), tint = Color.Gray)
                   Spacer(modifier = Modifier.width(4.dp))
                   Text(item.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Distance and Time (conditionally displayed)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                     item.distance?.let {
                        Text( String.format(Locale.US, "%.1f km", it), style = MaterialTheme.typography.bodySmall)
                     }
                    item.time?.let {
                         Text(String.format(Locale.US, "~%.0f min", it), style = MaterialTheme.typography.bodySmall)
                     }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Br ${String.format(Locale.US, "%.2f", item.price)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)

                // Button / Link Placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onViewDetailClick() }) {
                        Text("See pharmacy detail")
                    }
                    IconButton(onClick = { onAddToCartClick(item.inventoryId) }, enabled = !isAddingToCart) {
                        if (isAddingToCart) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.AddShoppingCart,
                                contentDescription = "Add to cart"
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Preview --- 
@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_Results() {
    PharmaConnectAndroidSTheme {
        // Simulate state with results for preview
        val previewState = SearchScreenState(
            searchQuery = "Aspirin",
            searchResults = List(3) { index ->
                 SearchResultItem(
                     pharmacyName = "Preview Pharmacy ${index + 1}",
                     address = "12${index} Preview St",
                     price = 10.50 + index,
                     quantity = (index + 1) * 5,
                     latitude = null,
                     longitude = null,
                     distance = (index+1)*0.5,
                     time = (index+1)*2.0,
                     photo = null,
                     pharmacyId = "p${index}",
                     inventoryId = "i${index}"
                 )
            }
        )
        // Directly compose the screen content for preview
         Column(
             modifier = Modifier
                 .fillMaxSize()
                 .padding(horizontal = 16.dp)
         ) {
             OutlinedTextField(value = previewState.searchQuery, onValueChange = {}, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), placeholder = { Text("Search for Medicine") }, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") })
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 Button(onClick = {}) { Text("Price Range") }
                 Button(onClick = {}) { Text("Location") }
             }
             Spacer(modifier = Modifier.height(16.dp))
             LazyColumn(
                 modifier = Modifier.fillMaxSize(),
                 verticalArrangement = Arrangement.spacedBy(12.dp),
                 contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                 item { Text("Results for: ${previewState.searchQuery}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                 items(previewState.searchResults) { resultItem ->
                    SearchResultItemCard(
                        item = resultItem, 
                        isAddingToCart = false, // Added for preview
                        onViewDetailClick = {},
                        onAddToCartClick = {} // Added for preview
                        )
                }
             }
         }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_NoResults() {
    PharmaConnectAndroidSTheme {
        // Simulate state with no results
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Column(horizontalAlignment = Alignment.CenterHorizontally){
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("${SearchViewModel.NO_MEDICINE_FOUND_MSG_PREFIX}'NonExistentMedicine'")
             }         
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_Error() {
    PharmaConnectAndroidSTheme {
         // Simulate error state
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                 Spacer(modifier = Modifier.height(8.dp))
                 Text("Couldn't reach server", textAlign = TextAlign.Center)
             }
         }
    }
} 