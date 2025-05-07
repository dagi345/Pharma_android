package com.example.pharma_connect_androids.ui.features.owner

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateInventoryItemScreen(
    viewModel: UpdateInventoryItemViewModel = hiltViewModel(),
    // IDs are read by ViewModel from SavedStateHandle
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // Handle submission result
    LaunchedEffect(state.submissionSuccess, state.submissionError) {
        if (state.submissionSuccess) {
            Toast.makeText(context, "Inventory item updated!", Toast.LENGTH_SHORT).show()
            viewModel.resetSubmissionStatus()
            onNavigateBack()
        }
        state.submissionError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.resetSubmissionStatus() 
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { 
            TopAppBar(
                title = { Text(state.itemDetails?.medicineName ?: "Update Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
         }
    ) { paddingValues ->
        when {
            state.isLoadingDetails -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.loadError != null -> {
                 Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.loadError}", color = MaterialTheme.colorScheme.error)
                }
            }
            state.itemDetails == null -> { 
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Item details not found.")
                }
            }
            else -> {
                // Capture the non-null value in a local variable
                val currentItemDetails = state.itemDetails 
                // Add a null check on the local variable (although theoretically guaranteed by `when`)
                if (currentItemDetails != null) {
                    // Form content
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(16.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Display Medicine Name (Readonly)
                        Text("Medicine: ${currentItemDetails.medicineName}", style = MaterialTheme.typography.titleMedium)
                        Divider()
                        
                        // Quantity
                        OutlinedTextField(
                            value = state.quantity,
                            onValueChange = viewModel::onQuantityChanged,
                            label = { Text("Quantity *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = state.quantityError != null,
                            supportingText = { state.quantityError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Price
                        OutlinedTextField(
                            value = state.price,
                            onValueChange = viewModel::onPriceChanged,
                            label = { Text("Price *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = state.priceError != null,
                            supportingText = { state.priceError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Expiry Date Picker
                        OutlinedTextField(
                            value = state.expiryDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expiry Date *") },
                            isError = state.expiryDateError != null,
                            supportingText = { state.expiryDateError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            }
                        )

                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = state.expiryDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                            )
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    Button(onClick = { 
                                        datePickerState.selectedDateMillis?.let {
                                            val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                            viewModel.onExpiryDateSelected(selectedDate)
                                        }
                                        showDatePicker = false 
                                    }) { Text("OK") }
                                },
                                dismissButton = { Button(onClick = { showDatePicker = false }) { Text("Cancel") } }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = viewModel::submitUpdate,
                            enabled = !state.isSubmitting,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Update Inventory Item")
                            }
                        }
                    }
                } else {
                    // Fallback if local variable is somehow null (shouldn't happen here)
                     Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Item details became unavailable.")
                    }
                }
            }
        }
    }
} 