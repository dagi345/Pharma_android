package com.example.pharma_connect_androids.ui.features.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // ktlint-disable no-wildcard-imports
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pharma_connect_androids.R // Assuming placeholder drawable exists
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.util.Resource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPharmacyDetailScreen(
    viewModel: AdminPharmacyDetailViewModel = hiltViewModel(),
    pharmacyId: String?,
    onNavigateBack: () -> Unit
) {
    val pharmacyState by viewModel.pharmacyState.collectAsState()

    LaunchedEffect(pharmacyId) {
        // Fetch details only if pharmacyId is not null and state is not already loaded/loading
        if (!pharmacyId.isNullOrBlank() && pharmacyState == null) {
             viewModel.fetchPharmacyDetails(pharmacyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pharmacy Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = pharmacyState) {
                is Resource.Loading, null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    state.data?.let {
                        // Use a Column that is scrollable
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                             PharmacyDetailContent(pharmacy = it)
                        }
                    } ?: Text("Pharmacy details not found.", modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(
                        text = "Error loading details: ${state.message}",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PharmacyDetailContent(pharmacy: Pharmacy) {
    val context = LocalContext.current

    // Image at the top
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(pharmacy.pharmacyImage)
            .crossfade(true)
            .placeholder(R.drawable.placeholder_image) // TODO: Add a real placeholder drawable
            .error(R.drawable.placeholder_image) // TODO: Add a real error drawable
            .build(),
        contentDescription = "Pharmacy Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Adjust height as needed
        contentScale = ContentScale.Crop
    )

    // Details Section
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(pharmacy.name, style = MaterialTheme.typography.headlineMedium)
        Divider()

        PharmacyInfoRow(label = "Owner Name", value = pharmacy.ownerName)
        PharmacyInfoRow(label = "License Number", value = pharmacy.licenseNumber)
        PharmacyInfoRow(label = "Email", value = pharmacy.email)
        PharmacyInfoRow(label = "Contact Number", value = pharmacy.contactNumber)
        PharmacyInfoRow(label = "Address", value = pharmacy.address)
        PharmacyInfoRow(label = "City", value = pharmacy.city)
        PharmacyInfoRow(label = "State", value = pharmacy.state)
        PharmacyInfoRow(label = "Zipcode", value = pharmacy.zipcode)

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Map Section
        Text("Location on Map", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        val pharmacyLocation = remember(pharmacy.latitude, pharmacy.longitude) {
            if (pharmacy.latitude != null && pharmacy.longitude != null) {
                LatLng(pharmacy.latitude, pharmacy.longitude)
            } else {
                null // Handle case where coordinates are missing
            }
        }

        if (pharmacyLocation != null) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(pharmacyLocation, 15f) // Zoom level 15
            }
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp), // Adjust map height
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = pharmacyLocation),
                    title = pharmacy.name,
                    snippet = pharmacy.address
                )
            }
        } else {
            Text("Map location is unavailable.")
        }
    }
}

@Composable
fun PharmacyInfoRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("$label: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp)) // Fixed width for label
            Text(value)
        }
    }
}

// TODO: Add Preview for AdminPharmacyDetailScreen and PharmacyDetailContent 