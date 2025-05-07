package com.example.pharma_connect_androids.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateMedicineScreen(
    viewModel: UpdateMedicineViewModel = hiltViewModel(),
    medicineId: String?, // medicineId is passed via navigation
    onNavigateBack: () -> Unit
) {
    val formState = viewModel.formState
    val uiState by viewModel.uiState.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddMedicineUiState.Success -> {
                Toast.makeText(context, "Medicine updated successfully!", Toast.LENGTH_LONG).show()
                viewModel.resetUiStateToIdle() // Reset state
                onNavigateBack() // Navigate back after successful update
            }
            is AddMedicineUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetUiStateToIdle() // Reset state
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Update Medicine") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentLoadState = loadState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error loading medicine details: ${currentLoadState.message}")
                }
            }
            is Resource.Success -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                        value = formState.image,
                        onValueChange = { viewModel.onImageChange(it) },
                        label = { Text("Image URL *") },
                        placeholder = { Text("https://example.com/image.png")},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = formState.imageError != null,
                        supportingText = { 
                            formState.imageError?.let { Text(it) } 
                            if (formState.imageError == null) { Text("Provide publicly accessible URL.") }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.updateMedicine() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState != AddMedicineUiState.Loading
                    ) {
                        if (uiState == AddMedicineUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Update Medicine")
                        }
                    }
                }
            }
        }
    }
} 