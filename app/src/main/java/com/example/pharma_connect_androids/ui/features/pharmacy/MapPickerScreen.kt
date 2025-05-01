package com.example.pharma_connect_androids.ui.features.pharmacy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLatLng: LatLng = LatLng(9.03, 38.74), // Default to Addis Ababa center
    onLocationSelected: (LatLng) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf(initialLatLng) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 12f) // Start zoomed in
    }

    // State to track if a location has been explicitly selected by tap
    var hasSelected by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Pharmacy Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (hasSelected) {
                                onLocationSelected(selectedLocation)
                            } else {
                                // Optionally prompt user to tap a location first
                                Toast.makeText(context, "Please tap a location on the map", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = hasSelected // Enable only after a tap
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Confirm Location")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                     containerColor = Color(0xFFE9EFFF) // Match other top bar
                 )
            )
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = true), // Enable zoom controls
            onMapClick = { latLng ->
                selectedLocation = latLng
                hasSelected = true // Mark that a selection has been made
                 // Optional: Move camera to tapped location smoothly
                // cameraPositionState.move(CameraUpdateFactory.newLatLng(latLng))
            }
        ) {
            // Add a marker at the selected location
            Marker(
                state = MarkerState(position = selectedLocation),
                title = "Selected Location",
                snippet = "Lat: ${selectedLocation.latitude}, Lng: ${selectedLocation.longitude}"
            )
        }
    }
} 