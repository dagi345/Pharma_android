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
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinPharmacyScreen(
    viewModel: JoinPharmacyViewModel = hiltViewModel(),
    // Parameter to indicate if this is for update - passed from NavHost
    // Note: ViewModel now determines mode internally via SavedStateHandle
    // pharmacyIdToUpdate: String?, 
    onNavigateBack: () -> Unit,
    onSubmitSuccess: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val TAG = "JoinPharmacyScreen"

    val isUpdateMode = state.isUpdateMode // Get mode from VM state
    val screenTitle = if (isUpdateMode) "Update Pharmacy Profile" else "Join Us As A Pharmacy"
    val submitButtonText = if (isUpdateMode) "Update Profile" else "Submit Application"

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

    // Show toast for errors or success
    LaunchedEffect(key1 = state.submissionError) {
        state.submissionError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(key1 = state.submissionSuccess) {
        if (state.submissionSuccess) {
            val successMsg = if (isUpdateMode) "Profile Updated Successfully!" else "Application Submitted Successfully!"
            Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
            viewModel.resetSubmissionSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                 colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE9EFFF))
            )
        }
    ) { paddingValues ->
         if (state.isLoadingDetails && state.isUpdateMode) { // Show loading only in update mode
             Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
         } else {
             Column(
                 modifier = Modifier
                     .fillMaxSize()
                     .padding(paddingValues)
                     .padding(16.dp)
                     .verticalScroll(scrollState),
                 verticalArrangement = Arrangement.spacedBy(12.dp)
             ) {
                 if (!isUpdateMode) {
                     Text(
                         text = "To join our Pharmacy Partner Program, simply fill out the form below...",
                         style = MaterialTheme.typography.bodyMedium,
                         color = Color.Gray,
                         modifier = Modifier.padding(bottom = 16.dp)
                     )
                 }
                
                 // Form Fields - values are pre-filled from state
                 OutlinedTextField(value = state.pharmacyName, onValueChange = viewModel::onPharmacyNameChange, label = { Text("Pharmacy Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                 OutlinedTextField(value = state.ownerName, onValueChange = viewModel::onOwnerNameChange, label = { Text("Owner Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                 OutlinedTextField(value = state.contactNumber, onValueChange = viewModel::onContactNumberChange, label = { Text("Contact Number *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                 OutlinedTextField(value = state.email, onValueChange = viewModel::onEmailChange, label = { Text("Email *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
                 OutlinedTextField(value = state.address, onValueChange = viewModel::onAddressChange, label = { Text("Address (Street/Subcity) *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                 OutlinedTextField(value = state.city, onValueChange = viewModel::onCityChange, label = { Text("City *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                 OutlinedTextField(value = state.state, onValueChange = viewModel::onStateChange, label = { Text("State/Region *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                 OutlinedTextField(value = state.zipCode, onValueChange = viewModel::onZipCodeChange, label = { Text("Zip Code *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                 OutlinedTextField(value = state.licenseNumber, onValueChange = viewModel::onLicenseNumberChange, label = { Text("License Number *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                 Spacer(modifier = Modifier.height(16.dp))
                 Text("Image Uploads", style = MaterialTheme.typography.titleMedium)
                 Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                     Button(onClick = { licenseImagePicker.launch("image/*") }) {
                         Text(if(state.licenseImage.isNotBlank()) "License Added ✓" else "Add License Image")
                     }
                      Button(onClick = { pharmacyImagePicker.launch("image/*") }) {
                         Text(if(state.pharmacyImage.isNotBlank()) "Pharmacy Img Added ✓" else "Add Pharmacy Image")
                     }
                 }
                 if (state.licenseImage.isNotBlank()) {
                     Text("License: ${state.licenseImage}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                 }
                 if (state.pharmacyImage.isNotBlank()) {
                     Text("Pharmacy: ${state.pharmacyImage}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                 }

                 Spacer(modifier = Modifier.height(16.dp))

                 // --- Google Maps Link Input --- 
                 Text("Pharmacy Location Link", style = MaterialTheme.typography.titleMedium)
                 OutlinedTextField(
                     value = state.googleMapsLink,
                     onValueChange = viewModel::onGoogleMapsLinkChange,
                     label = { Text("Google Maps Link *") },
                     placeholder = { Text("Paste Google Maps URL here...") },
                     modifier = Modifier.fillMaxWidth(),
                     isError = state.linkParseError != null,
                     supportingText = { 
                         val errorText = state.linkParseError
                         if(errorText != null) { 
                             Text(errorText, color = MaterialTheme.colorScheme.error)
                         } else {
                             Text("Find your pharmacy on Google Maps, click Share -> Copy link, and paste here.")
                         }
                     },
                     singleLine = true
                 )
                 // Display Parsed Coords (Optional for debugging)
                 // Text("Parsed Coords: Lat=${state.latitude}, Lng=${state.longitude}", style = MaterialTheme.typography.bodySmall)

                 Spacer(modifier = Modifier.height(24.dp))
                 Button(
                     onClick = { viewModel.submitForm() }, // Call unified submit function
                     enabled = !state.isLoading,
                     modifier = Modifier.align(Alignment.End)
                 ) {
                     if (state.isLoading) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                     } else {
                         Text(submitButtonText) // Use dynamic button text
                     }
                 }
                 Spacer(modifier = Modifier.height(16.dp))
             }
         }
    }
}

// Preview needs update if needed
// @Preview(showBackground = true)
// @Composable
// fun JoinPharmacyScreenPreview() { ... } 