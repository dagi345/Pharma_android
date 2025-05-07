package com.example.pharma_connect_androids.ui.features.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pharma_connect_androids.data.models.Medicine
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMedicinesScreen(
    viewModel: AdminMedicinesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMedicines()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Medicines Management") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is AdminMedicinesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AdminMedicinesUiState.Success -> {
                    if (state.medicines.isEmpty()) {
                        Text(
                            text = "No medicines found.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.medicines, key = { it.id }) { medicine ->
                                MedicineListItem(medicine = medicine)
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
                        text = "Fetching medicines...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListItem(medicine: Medicine) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = medicine.image, // TODO: Add placeholder/error images for Coil
                contentDescription = "Medicine Image",
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp)
                // .clip(CircleShape) // Optional: if you want circular images
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medicine.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Category: ${medicine.category}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = medicine.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Placeholder for actions
            IconButton(onClick = { /* TODO: Implement actions (View, Edit, Delete) */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More actions")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AdminMedicinesScreenPreview_Success() {
    PharmaConnectAndroidSTheme {
        // Manually create a ViewModel with dummy data for preview
        val dummyMedicines = listOf(
            Medicine("1", "Paracetamol 500mg", "Painkiller", "Relieves mild to moderate pain and fever. Effective for headaches, muscle aches, arthritis, backache, toothaches, colds, and fevers.", "https://example.com/paracetamol.jpg"),
            Medicine("2", "Amoxicillin 250mg", "Antibiotic", "Used to treat a wide variety of bacterial infections. Works by stopping the growth of bacteria.", "https://example.com/amoxicillin.jpg")
        )
        // This preview won't use Hilt, so we can't hiltViewModel()
        // Instead, we mock the state or pass data directly to a previewable composable
        // For simplicity, directly using a success state for the Box part of the screen.
        Scaffold(topBar = { TopAppBar(title = { Text("Medicines Management") }) }) {
            LazyColumn(
                modifier = Modifier.padding(it),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dummyMedicines) { medicine ->
                    MedicineListItem(medicine = medicine)
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
        Scaffold(topBar = { TopAppBar(title = { Text("Medicines Management") }) }) {
            Box(modifier = Modifier.padding(it).fillMaxSize(), contentAlignment = Alignment.Center){
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
         Scaffold(topBar = { TopAppBar(title = { Text("Medicines Management") }) }) {
            Box(modifier = Modifier.padding(it).fillMaxSize(), contentAlignment = Alignment.Center){
                Text("Error: Failed to load medicines.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
} 