package com.example.pharma_connect_androids.ui.features.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.R // For placeholder image
import com.example.pharma_connect_androids.data.models.SearchResultItem
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                selectedOption = priceRanges.find { it.second == state.selectedPriceRange }?.first ?: "Any Price",
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
                selectedOption = state.selectedLocation ?: "Any Location",
                onOptionSelected = { selectedLocation ->
                    viewModel.onLocationSelected(selectedLocation)
                },
                 modifier = Modifier.weight(1f)
            )

            // Near Me Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp) // Add some padding
            ) {
                Checkbox(
                    checked = state.isNearMeChecked,
                    onCheckedChange = { viewModel.onNearMeToggled(it) }
                )
                Text("Near Me")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.searchError ?: "An error occurred", textAlign = TextAlign.Center)
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
                             "Results for: ${state.searchQuery}",
                             style = MaterialTheme.typography.titleMedium,
                             modifier = Modifier.padding(bottom = 8.dp)
                         )
                     }
                     items(state.searchResults) { resultItem ->
                         SearchResultItemCard(item = resultItem)
                     }
                 }
             }
             state.searchQuery.isNotBlank() && !state.isLoading -> {
                 // Show "No results found" specifically after a search completes
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Text("No results found for \"${state.searchQuery}\"")
                 }
             }
            // Implicitly, if query is blank and not loading/error, show nothing or maybe suggestions
        }
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
fun SearchResultItemCard(item: SearchResultItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) { // Make row height based on content
            // Image Placeholder
            Image(
                painter = painterResource(id = R.drawable.logo), // Use logo as placeholder
                contentDescription = "Pharmacy Image",
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
                TextButton(onClick = { /* TODO: Navigate to Pharmacy Detail */ }) {
                    Text("See pharmacy detail")
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
                 Button(onClick = {}) { Text("Near Me") }
             }
             Spacer(modifier = Modifier.height(16.dp))
             LazyColumn(
                 modifier = Modifier.fillMaxSize(),
                 verticalArrangement = Arrangement.spacedBy(12.dp),
                 contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                 item { Text("Results for: ${previewState.searchQuery}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                 items(previewState.searchResults) { resultItem ->
                    SearchResultItemCard(item = resultItem)
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
             Text("No results found for \"NonExistentMedicine\"")
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