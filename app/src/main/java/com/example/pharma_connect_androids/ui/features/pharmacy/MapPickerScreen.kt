package com.example.pharma_connect_androids.ui.features.pharmacy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLatLng: LatLng = LatLng(9.03, 38.74), // Default to Addis Ababa center
    onLocationSelected: (LatLng) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val markerState = rememberMarkerState(position = initialLatLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f) // Zoom in closer initially
    }

    LaunchedEffect(markerState.position) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(markerState.position, cameraPositionState.position.zoom)
    }

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
                    IconButton(onClick = { onLocationSelected(markerState.position) }) {
                        Icon(Icons.Filled.Check, contentDescription = "Confirm Location")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE9EFFF))
            )
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                onMapClick = { latLng -> markerState.position = latLng }
            ) {
                Marker(
                    state = markerState,
                    title = "Pharmacy Location",
                    snippet = "Drag to adjust position",
                    draggable = true
                )
            }
        }
    }
} 