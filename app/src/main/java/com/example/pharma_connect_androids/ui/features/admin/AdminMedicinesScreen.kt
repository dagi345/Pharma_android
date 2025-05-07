package com.example.pharma_connect_androids.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMedicinesScreen(
    viewModel: AdminMedicinesViewModel = hiltViewModel(),
    // Add navigation callback for update screen
    onNavigateToUpdateMedicine: (medicineId: String) -> Unit 
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemActionState by viewModel.itemActionState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // For showing confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var medicineToDelete by remember { mutableStateOf<Medicine?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchMedicines()
    }

    // Observe item action state for showing toast/snackbar
    LaunchedEffect(itemActionState) {
        when (val state = itemActionState) {
            is ItemActionState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetItemActionState() // Reset after showing
            }
            is ItemActionState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetItemActionState() // Reset after showing
            }
            else -> {}
        }
    }

    if (showDeleteDialog && medicineToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${medicineToDelete?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        medicineToDelete?.id?.let { viewModel.deleteMedicine(it) }
                        showDeleteDialog = false
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Medicines Management") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) { // Main Column
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search Medicines (Name or Category)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )

            Box(
                modifier = Modifier.weight(1f) // Box takes remaining space
            ) {
                when (val state = uiState) {
                    is AdminMedicinesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is AdminMedicinesUiState.Success -> {
                        if (state.medicines.isEmpty()) {
                            val message = if (searchQuery.isNotBlank()) "No medicines match your search." else "No medicines found."
                            Text(
                                text = message,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.medicines, key = { it.id }) { medicine ->
                                    MedicineListItem(
                                        medicine = medicine,
                                        onUpdateClick = { 
                                            // Navigate to update screen
                                            onNavigateToUpdateMedicine(medicine.id)
                                        },
                                        onDeleteClick = {
                                            medicineToDelete = medicine
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is AdminMedicinesUiState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is AdminMedicinesUiState.Idle -> {
                         Text(
                            text = "Initializing...", // Changed from "Fetching medicines..."
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListItem(
    medicine: Medicine,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp) // Adjusted padding
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = medicine.image,
                contentDescription = "Medicine Image",
                modifier = Modifier
                    .size(72.dp) // Slightly larger image
                    .padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = medicine.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Category: ${medicine.category}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = medicine.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Update") },
                        onClick = {
                            onUpdateClick()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Update")}
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete")}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AdminMedicinesScreenPreview_Success() {
    PharmaConnectAndroidSTheme {
        val dummyMedicines = listOf(
            Medicine("1", "Paracetamol 500mg", "Painkiller", "Relieves mild to moderate pain and fever. Effective for headaches, muscle aches, arthritis, backache, toothaches, colds, and fevers.", "https://example.com/paracetamol.jpg"),
            Medicine("2", "Amoxicillin 250mg", "Antibiotic", "Used to treat a wide variety of bacterial infections. Works by stopping the growth of bacteria.", "https://example.com/amoxicillin.jpg")
        )
        Scaffold(topBar = { TopAppBar(title = { Text("Medicines Management") }) }) { pv ->
            LazyColumn(
                modifier = Modifier.padding(pv),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dummyMedicines) { medicine ->
                    MedicineListItem(medicine = medicine, onUpdateClick = {}, onDeleteClick = {})
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AdminMedicinesScreenPreview_Empty() {
    PharmaConnectAndroidSTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Medicines Management") }) }) { pv ->
            Box(modifier = Modifier.padding(pv).fillMaxSize(), contentAlignment = Alignment.Center){
                 Text("No medicines found.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AdminMedicinesScreenPreview_Error() {
    PharmaConnectAndroidSTheme {
         Scaffold(topBar = { TopAppBar(title = { Text("Medicines Management") }) }) { pv ->
            Box(modifier = Modifier.padding(pv).fillMaxSize(), contentAlignment = Alignment.Center){
                Text("Error: Failed to load medicines.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
} 