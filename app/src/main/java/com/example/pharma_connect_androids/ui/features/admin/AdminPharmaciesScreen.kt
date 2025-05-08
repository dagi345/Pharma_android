package com.example.pharma_connect_androids.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // ktlint-disable no-wildcard-imports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPharmaciesScreen(
    viewModel: AdminPharmaciesViewModel = hiltViewModel(),
    onNavigateToPharmacyDetail: (pharmacyId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemActionState by viewModel.itemActionState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pharmacyToDelete by remember { mutableStateOf<Pharmacy?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchPharmacies()
    }

    LaunchedEffect(itemActionState) {
        when (val state = itemActionState) {
            is ItemActionState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetItemActionState()
            }
            is ItemActionState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = state.message, duration = SnackbarDuration.Short)
                }
                viewModel.resetItemActionState()
            }
            else -> {}
        }
    }

    if (showDeleteDialog && pharmacyToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete pharmacy '${pharmacyToDelete?.name}'?") },
            confirmButton = {
                Button(onClick = {
                    pharmacyToDelete?.id?.let { viewModel.deletePharmacy(it) }
                    showDeleteDialog = false
                }) { Text("Delete") }
            },
            dismissButton = { Button(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Pharmacies Management") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search Pharmacies") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is AdminPharmaciesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is AdminPharmaciesUiState.Success -> {
                        if (state.pharmacies.isEmpty()) {
                            val message = if (searchQuery.isNotBlank()) "No pharmacies match your search." else "No pharmacies found."
                            Text(text = message, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.pharmacies, key = { it.id }) { pharmacy ->
                                    PharmacyListItem(
                                        pharmacy = pharmacy,
                                        onViewDetailsClick = { onNavigateToPharmacyDetail(pharmacy.id) },
                                        onDeleteClick = { pharmacyToDelete = pharmacy; showDeleteDialog = true }
                                    )
                                }
                            }
                        }
                    }
                    is AdminPharmaciesUiState.Error -> {
                        Text(text = "Error: ${state.message}", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                    }
                    is AdminPharmaciesUiState.Idle -> {
                        Text(text = "Initializing...", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyListItem(
    pharmacy: Pharmacy,
    onViewDetailsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Could add a placeholder pharmacy icon here if desired
            // Icon(Icons.Filled.LocalPharmacy, contentDescription = null, modifier = Modifier.size(40.dp).padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = pharmacy.name, style = MaterialTheme.typography.titleMedium)
                Text(text = pharmacy.address, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                pharmacy.city?.let { Text(text = "City: $it", style = MaterialTheme.typography.bodySmall) }
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More actions") }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("View Details") },
                        onClick = { onViewDetailsClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = "View Details") }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Pharmacy") },
                        onClick = { onDeleteClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete Pharmacy") }
                    )
                }
            }
        }
    }
}

// TODO: Add Previews for AdminPharmaciesScreen (Success, Empty, Error) 