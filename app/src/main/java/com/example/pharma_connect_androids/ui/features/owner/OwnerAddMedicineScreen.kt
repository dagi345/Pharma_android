package com.example.pharma_connect_androids.ui.features.owner

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties // Import for controlling focus
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.pharma_connect_androids.data.models.Medicine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerAddMedicineScreen(
    viewModel: OwnerAddMedicineViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // Show success/error messages
    LaunchedEffect(state.submissionSuccess, state.submissionError) {
        if (state.submissionSuccess) {
            Toast.makeText(context, "Medicine added to inventory!", Toast.LENGTH_SHORT).show()
            viewModel.resetSubmissionStatus()
        }
        state.submissionError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.resetSubmissionStatus() 
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Add Medicine to Inventory") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Medicine Dropdown/Autocomplete
            MedicineAutoComplete(
                medicines = state.allMedicines,
                selectedMedicine = state.selectedMedicine,
                onMedicineSelected = viewModel::onMedicineSelected,
                isLoading = state.isLoadingMedicines,
                error = state.medicineIdError
            )

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
                onValueChange = {}, // Not directly editable
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
                onClick = viewModel::submitAddMedicineToInventory,
                enabled = !state.isSubmitting,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Add to Inventory")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineAutoComplete(
    medicines: List<Medicine>,
    selectedMedicine: Medicine?,
    onMedicineSelected: (Medicine) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember(selectedMedicine?.name) { mutableStateOf(selectedMedicine?.name ?: "") }
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    val filteredMedicines = remember(searchText, medicines) {
        if (searchText.isBlank()) {
            // Show all when focused and blank, otherwise empty
            if (hasFocus) medicines else emptyList() 
        } else {
            medicines.filter { it.name.contains(searchText, ignoreCase = true) }
        }
    }

    // Dropdown should be visible if focused and (loading or results exist)
    // AND if the text isn't exactly matching the selected item (to hide after selection)
    val showDropdown = hasFocus && (isLoading || filteredMedicines.isNotEmpty())
    
    // Update expanded state based on whether dropdown should be shown
    LaunchedEffect(showDropdown) {
        expanded = showDropdown
    }

    Column(modifier = Modifier.fillMaxWidth()) { // Use Column to stack TextField and List
        OutlinedTextField(
            value = searchText,
            onValueChange = { 
                searchText = it
                // Clear selection if text is manually changed away from a selected item
                if (it != selectedMedicine?.name) {
                    // TODO: Clear selection if needed
                }
            },
            label = { Text("Medicine Name *") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                    // Keep expanded true while focused and results available
                    if (!focusState.isFocused) {
                        expanded = false // Collapse when focus is lost
                    }
                },
            trailingIcon = { 
                // Icon to indicate dropdown, could also toggle 'expanded' on click
                 Icon(Icons.Default.ArrowDropDown, "Dropdown", Modifier.clickable { expanded = !expanded }) 
            },
            isError = error != null,
            supportingText = { error?.let { Text(it) } }
        )

        // Conditionally display the list *below* the TextField
        if (expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // Limit height and make scrollable
                    .padding(top = 4.dp), // Small gap
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) 
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (isLoading) {
                        item { 
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                         }
                    } else {
                        items(filteredMedicines, key = { it.id }) { medicine ->
                            ListItem(
                                headlineContent = { Text(medicine.name) },
                                modifier = Modifier.clickable {
                                    onMedicineSelected(medicine)
                                    searchText = medicine.name // Update text field
                                    expanded = false // Collapse after selection
                                    // Keep focus? Keyboard might hide, depends on system.
                                }
                            )
                            Divider(thickness = 0.5.dp)
                        }
                        if (filteredMedicines.isEmpty() && searchText.isNotBlank()) {
                            item { 
                                Text("No matching medicines found", modifier = Modifier.padding(16.dp))
                             }
                        }
                    }
                }
            }
        }
    }
}

// TODO: Add Previews 