package com.example.pharma_connect_androids.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddMedicineScreen(
    viewModel: AdminAddMedicineViewModel = hiltViewModel()
) {
    val formState = viewModel.formState
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is AddMedicineUiState.Success -> {
                Toast.makeText(context, "Medicine added successfully!", Toast.LENGTH_LONG).show()
                viewModel.resetState() // Reset form and UI state
            }
            is AddMedicineUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add New Medicine") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Adjusted spacing
        ) {
            Text(
                text = "Add Medicine to the Platform",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp) // Added padding
            )

            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Medicine Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = formState.nameError != null,
                supportingText = { formState.nameError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = formState.category,
                onValueChange = { viewModel.onCategoryChange(it) },
                label = { Text("Category *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = formState.categoryError != null,
                supportingText = { formState.categoryError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = formState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = formState.descriptionError != null,
                supportingText = { formState.descriptionError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = formState.image, // This is the image URL
                onValueChange = { viewModel.onImageChange(it) },
                label = { Text("Image URL *") },
                placeholder = { Text("https://example.com/image.png")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = formState.imageError != null,
                supportingText = { 
                    formState.imageError?.let { Text(it) } 
                    if (formState.imageError == null) { Text("Please provide a publicly accessible URL for the image.") }
                }
            )
            // TODO: Implement Image Picker to get image, upload it, and then populate this field with the URL.

            Spacer(modifier = Modifier.height(8.dp)) // Added spacer

            Button(
                onClick = { viewModel.addMedicine() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState != AddMedicineUiState.Loading
            ) {
                if (uiState == AddMedicineUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Add Medicine")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminAddMedicineScreenPreview() {
    PharmaConnectAndroidSTheme {
        AdminAddMedicineScreen()
    }
} 