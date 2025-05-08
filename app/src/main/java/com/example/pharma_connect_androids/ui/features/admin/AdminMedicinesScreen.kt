package com.example.pharma_connect_androids.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp

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
                label = { Text("Search Medicines") },
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
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant 
    val accentColor = MaterialTheme.colorScheme.secondaryContainer // Light blue accent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, 
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), 
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), 
            verticalAlignment = Alignment.CenterVertically // Make items stretch to row height
        ) {
            // Accent Color Bar
            Box(
                modifier = Modifier
                    .fillMaxHeight() // Fill the height of the Row
                    .width(8.dp) // Width of the accent bar
                    .background(accentColor)
            )

            // Content Row (with padding now)
            Row(
                 modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp) // Padding for content
                    .weight(1f), // Takes remaining space
                 verticalAlignment = Alignment.Top 
            ) {
                 AsyncImage(
                    model = medicine.image,
                    contentDescription = "Medicine Image",
                    modifier = Modifier
                        .size(72.dp) 
                        .clip(MaterialTheme.shapes.small) 
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentScale = ContentScale.Crop
                 )
                 Spacer(modifier = Modifier.width(16.dp))
                 Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                     Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.SemiBold,
                         color = MaterialTheme.colorScheme.onSurfaceVariant // Ensure good contrast
                     )
                     Text(
                        text = "Category: ${medicine.category}",
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Ensure good contrast
                     )
                     Text(
                        text = medicine.description,
                        style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), // Slightly faded
                        maxLines = 3, 
                        overflow = TextOverflow.Ellipsis
                     )
                 }
            }
            // Overflow Menu Box (outside the inner content Row)
             Box(modifier = Modifier.padding(vertical = 8.dp)) { // Add some vertical padding to align better
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More actions", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                 DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                     DropdownMenuItem(
                        text = { Text("Update") },
                        onClick = { onUpdateClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Update")}
                     )
                     DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { onDeleteClick(); showMenu = false },
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