package com.example.pharma_connect_androids.ui.features.owner

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
import androidx.compose.ui.platform.LocalLifecycleOwner // Import LifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle // Import Lifecycle
import androidx.lifecycle.LifecycleEventObserver // Import Observer
import com.example.pharma_connect_androids.data.models.InventoryItem
import kotlinx.coroutines.launch
import java.time.LocalDate // Needed for expiry check
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerInventoryScreen(
    viewModel: OwnerInventoryViewModel = hiltViewModel(),
    onNavigateToUpdateItem: (pharmacyId: String, inventoryItemId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemActionState by viewModel.itemActionState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }

    // Refresh data when the screen resumes (e.g., after update)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchInventory() // Re-fetch data
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle item action feedback
    LaunchedEffect(itemActionState) {
        when (val state = itemActionState) {
            is ItemActionState.Success -> {
                // Only show Toast for delete actions
                if (state.message.contains("deleted")) { 
                     Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                 }
                viewModel.resetItemActionState()
            }
            is ItemActionState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
                viewModel.resetItemActionState()
            }
            else -> {}
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Delete '${itemToDelete?.medicineName}' from inventory?") },
            confirmButton = {
                Button(onClick = {
                    itemToDelete?.id?.let { viewModel.deleteInventoryItem(it) }
                    showDeleteDialog = false
                }) { Text("Delete") }
            },
            dismissButton = { Button(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("My Inventory") }) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(uiState.error!!, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                uiState.inventoryItems.isEmpty() -> {
                    Text("Inventory is empty.", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.inventoryItems, key = { it.id }) { item ->
                            InventoryItemCard(
                                item = item,
                                onUpdateClick = { 
                                     onNavigateToUpdateItem(item.pharmacyId, item.id)
                                },
                                onDeleteClick = { itemToDelete = item; showDeleteDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isExpired = item.expiryDate.isBefore(LocalDate.now())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if(isExpired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.medicineName, style = MaterialTheme.typography.titleMedium)
                Text("Category: ${item.category ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Price: ${String.format("%.2f", item.price)}", style = MaterialTheme.typography.bodySmall)
                 Text(
                     text = "Expires: ${item.expiryDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}", 
                     style = MaterialTheme.typography.bodySmall,
                     fontWeight = if(isExpired) FontWeight.Bold else FontWeight.Normal
                 )
                 Text("Stock: ${item.quantity}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Actions") }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Update Details") }, onClick = { onUpdateClick(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem(text = { Text("Delete Item") }, onClick = { onDeleteClick(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null) })
                }
            }
        }
    }
}

// TODO: Add Previews 