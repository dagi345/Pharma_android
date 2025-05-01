package com.example.pharma_connect_androids.ui.features.pharmacy

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.ui.theme.Pharma_connect_androidsTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinPharmacyScreen(
    viewModel: JoinPharmacyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    onNavigateToMapPicker: (Double, Double) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val TAG = "JoinPharmacyScreen" // Add TAG for logging

    // --- Image Pickers --- 
    val licenseImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            Log.d(TAG, "License image picker result: $uri") // Log URI
            uri?.let {
                val uriString = it.toString()
                Log.d(TAG, "Calling VM.onLicenseImageSelected with: $uriString") // Log call
                viewModel.onLicenseImageSelected(uriString) 
            }
        }
    )

    val pharmacyImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            Log.d(TAG, "Pharmacy image picker result: $uri") // Log URI
            uri?.let { 
                val uriString = it.toString()
                Log.d(TAG, "Calling VM.onPharmacyImageSelected with: $uriString") // Log call
                viewModel.onPharmacyImageSelected(uriString) 
            }
        }
    )
    // --- End Image Pickers ---

    // Show toast for errors
    LaunchedEffect(key1 = state.submissionError) {
        state.submissionError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Navigate back on successful submission
    LaunchedEffect(key1 = state.submissionSuccess) {
        if (state.submissionSuccess) {
            Toast.makeText(context, "Application Submitted Successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetSubmissionSuccess() // Reset state
            onSubmitSuccess() // Navigate back
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Us As A Pharmacy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                 colors = TopAppBarDefaults.topAppBarColors(
                     containerColor = Color(0xFFE9EFFF) // Match other top bar
                 )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp) // Content padding
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "To join our Pharmacy Partner Program, simply fill out the form below...", // Shortened desc
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Form Fields (Two columns not easily replicated without complex layout, use single column)
            OutlinedTextField(
                value = state.pharmacyName,
                onValueChange = viewModel::onPharmacyNameChange,
                label = { Text("Pharmacy Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.ownerName,
                onValueChange = viewModel::onOwnerNameChange,
                label = { Text("Owner Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
             OutlinedTextField(
                value = state.contactNumber,
                onValueChange = viewModel::onContactNumberChange,
                label = { Text("Contact Number *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
             OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
             OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address (Street/Subcity) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
             OutlinedTextField(
                value = state.city,
                onValueChange = viewModel::onCityChange,
                label = { Text("City *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
             OutlinedTextField(
                value = state.state,
                onValueChange = viewModel::onStateChange,
                label = { Text("State/Region *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.zipCode,
                onValueChange = viewModel::onZipCodeChange,
                label = { Text("Zip Code *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
             OutlinedTextField(
                value = state.licenseNumber,
                onValueChange = viewModel::onLicenseNumberChange,
                label = { Text("License Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Image Uploads --- 
            Text("Image Uploads", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                 Button(onClick = { 
                     // Launch the image picker for license image
                     licenseImagePicker.launch("image/*")
                 }) {
                      // Show selected URI or default text
                     Text(if(state.licenseImage.isNotBlank()) "License Added ✓" else "Add License Image")
                 }
                 Button(onClick = { 
                      // Launch the image picker for pharmacy image
                     pharmacyImagePicker.launch("image/*")
                 }) {
                      // Show selected URI or default text
                     Text(if(state.pharmacyImage.isNotBlank()) "Pharmacy Img Added ✓" else "Add Pharmacy Image")
                 }
             }
             // Display selected URIs (optional, for debugging/confirmation)
             if (state.licenseImage.isNotBlank()) {
                 Text("License: ${state.licenseImage}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
             }
              if (state.pharmacyImage.isNotBlank()) {
                 Text("Pharmacy: ${state.pharmacyImage}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
             }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Map Location Picker --- 
            Text("Pharmacy Location", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                 Column(Modifier.padding(16.dp)) {
                    Text("Selected Location:", style = MaterialTheme.typography.bodyMedium)
                     Text("Lat: ${state.latitude}, Lng: ${state.longitude}", style = MaterialTheme.typography.bodyMedium)
                     Spacer(Modifier.height(8.dp))
                     Button(onClick = { 
                         // Navigate to Map Picker Screen
                         onNavigateToMapPicker(state.latitude, state.longitude)
                     }) {
                         Text("Select Location on Map")
                     }
                 }
            }
            // --- End Map Picker ---

             Spacer(modifier = Modifier.height(24.dp))

             // Submit Button
            Button(
                onClick = { viewModel.submitApplication() },
                enabled = !state.isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Application")
                }
            }

             Spacer(modifier = Modifier.height(16.dp)) // Extra space at bottom
        }
    }
}


@Preview(showBackground = true)
@Composable
fun JoinPharmacyScreenPreview() {
    Pharma_connect_androidsTheme {
        JoinPharmacyScreen(onNavigateBack = {}, onSubmitSuccess = {}, onNavigateToMapPicker = { _, _ -> })
    }
} 