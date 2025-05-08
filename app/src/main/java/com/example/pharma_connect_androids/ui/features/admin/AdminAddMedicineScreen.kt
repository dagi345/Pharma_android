package com.example.pharma_connect_androids.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Use filled icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    MaterialTheme.colorScheme.background
                )
            )
        ),
        containerColor = Color.Transparent, // Make scaffold background transparent for gradient
        snackbarHost = { SnackbarHost(snackbarHostState) },
         
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp) // Adjust padding
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Make column scrollable
            // horizontalAlignment = Alignment.CenterHorizontally, // Not needed for fillMaxWidth fields
            verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing
        ) {
            Text(
                text = "Add Medicine to Platform",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp, top=8.dp).align(Alignment.CenterHorizontally) // Center title
            )

            // Medicine Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Medicine Name *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Medication, contentDescription = null) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                singleLine = true,
                isError = formState.nameError != null,
                supportingText = { formState.nameError?.let { Text(it) } }
            )

            // Category
            OutlinedTextField(
                value = formState.category,
                onValueChange = { viewModel.onCategoryChange(it) },
                label = { Text("Category *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                singleLine = true,
                isError = formState.categoryError != null,
                supportingText = { formState.categoryError?.let { Text(it) } }
            )

            // Description
            OutlinedTextField(
                value = formState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                minLines = 3,
                isError = formState.descriptionError != null,
                supportingText = { formState.descriptionError?.let { Text(it) } }
            )

            // Image URL
            OutlinedTextField(
                value = formState.image,
                onValueChange = { viewModel.onImageChange(it) },
                label = { Text("Image URL *") },
                placeholder = { Text("https://...")},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                singleLine = true,
                isError = formState.imageError != null,
                supportingText = { 
                    formState.imageError?.let { Text(it) } 
                    if (formState.imageError == null) { Text("Provide a publicly accessible URL.") }
                }
            )

            Spacer(modifier = Modifier.height(16.dp)) // Increased spacer

            // Submit Button
            Button(
                onClick = { viewModel.addMedicine() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = uiState != AddMedicineUiState.Loading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState == AddMedicineUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("ADD MEDICINE", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Space at bottom
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