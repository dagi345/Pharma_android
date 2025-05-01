package com.example.pharma_connect_androids.ui.features.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // Use Coil for image loading
import coil.request.ImageRequest
import com.example.pharma_connect_androids.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailScreen(
    detailViewModel: ApplicationDetailViewModel = hiltViewModel(),
    // Get Admin VM associated with the list screen for update actions
    adminViewModel: AdminApplicationViewModel, 
    onNavigateBack: () -> Unit
) {
    val detailState by detailViewModel.state.collectAsState()
    val adminState by adminViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detailState.application?.pharmacyName ?: "Application Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                     containerColor = MaterialTheme.colorScheme.surfaceVariant
                 )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Use a when expression for clearer conditional rendering
            when {
                detailState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                detailState.error != null -> {
                    Text(
                        text = "Error: ${detailState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                detailState.application != null -> {
                    // Display content when application data is available
                    val app = detailState.application!! // Safe non-null assertion here
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Pharmacy Details", style = MaterialTheme.typography.headlineSmall)
                        DetailRow("Pharmacy Name:", app.pharmacyName)
                        DetailRow("Owner Name:", app.ownerName)
                        DetailRow("License Number:", app.licenseNumber)
                        DetailRow("Email:", app.email)
                        DetailRow("Contact:", app.contactNumber)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Location & Address", style = MaterialTheme.typography.headlineSmall)
                        DetailRow("Address:", app.address)
                        DetailRow("City:", app.city)
                        DetailRow("State/Region:", app.state)
                        DetailRow("Zip Code:", app.zipCode)
                        DetailRow("Coordinates:", "Lat: ${app.latitude}, Lng: ${app.longitude}")
                        // TODO: Maybe show a mini-map here?

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Documents", style = MaterialTheme.typography.headlineSmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            DocumentImage(label = "License Image", imageUrl = app.licenseImage)
                            DocumentImage(label = "Pharmacy Image", imageUrl = app.pharmacyImage)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                         Text("Status", style = MaterialTheme.typography.headlineSmall)
                        DetailRow("Current Status:", app.status)

                        // Action Buttons
                        if (app.status.equals("Pending", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(24.dp))
                             val isUpdating = adminState.updateLoadingId == app._id
                             val updateError = adminState.updateErrorId?.takeIf { it.first == app._id }?.second

                            if (updateError != null) {
                                Text("Update Error: $updateError", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { adminViewModel.updateApplicationStatus(app._id, "Rejected") }, 
                                    enabled = !isUpdating,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MaterialTheme.colorScheme.error))
                                ) {
                                    if (isUpdating && adminState.updateLoadingId == app._id) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    } else {
                                         Icon(Icons.Filled.Close, contentDescription = "Reject", modifier = Modifier.size(ButtonDefaults.IconSize))
                                         Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text("Reject")
                                    }
                                }
                                Button(
                                    onClick = { adminViewModel.updateApplicationStatus(app._id, "Approved") }, 
                                    enabled = !isUpdating,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
                                ) {
                                     if (isUpdating && adminState.updateLoadingId == app._id) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Filled.Check, contentDescription = "Approve", modifier = Modifier.size(ButtonDefaults.IconSize))
                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text("Approve")
                                    }
                                }
                            }
                        }
                    }
                }
                // Optionally, handle the case where application is null but not loading and no error
                // else -> { /* Placeholder or empty state */ }
            }
        }
    }
}

@Composable
fun DocumentImage(label: String, imageUrl: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl) // Use the URL from the application data
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_background) // Add a placeholder drawable
                .error(R.drawable.ic_launcher_background) // Add an error drawable
                .build(),
            contentDescription = label,
            modifier = Modifier
                .height(150.dp)
                .width(200.dp)
                .padding(4.dp)
                .border(1.dp, Color.Gray),
            contentScale = ContentScale.Crop
        )
        // TODO: Add onClick to show image larger in a dialog/modal?
    }
} 