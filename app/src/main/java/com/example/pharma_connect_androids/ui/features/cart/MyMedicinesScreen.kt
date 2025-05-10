package com.example.pharma_connect_androids.ui.features.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.pharma_connect_androids.R
import com.example.pharma_connect_androids.data.models.CartItem
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMedicinesScreen(
    viewModel: MyMedicinesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadMyMedicines()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.generalMessage) {
        state.generalMessage?.let {
            message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearGeneralMessage() // Clear message after showing
            }
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Long, // Show errors longer
                    actionLabel = "Dismiss"
                )
                // Optionally clear error from ViewModel state if it's meant to be a one-time display
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Saved Medicines") },
                actions = {
                    if (state.cartItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.removeAllMedicines() }) {
                            Text("Remove All", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.isLoading && state.cartItems.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else if (state.cartItems.isEmpty() && !state.isLoading) {
                Text(
                    text = "You haven't saved any medicines yet.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.cartItems, key = { it.inventoryId }) { cartItem ->
                        MyMedicineItemCard(
                            item = cartItem,
                            onRemoveClick = {
                                viewModel.removeMedicine(cartItem.pharmacyId, cartItem.medicineId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyMedicineItemCard(
    item: CartItem,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.photo ?: R.drawable.logo, // Fallback to a default logo
                contentDescription = item.medicineName,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo),
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 8.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.medicineName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(item.pharmacyName, style = MaterialTheme.typography.bodyMedium)
                Text("Price: Br ${String.format(Locale.US, "%.2f", item.price)}", style = MaterialTheme.typography.bodySmall)
                Text("Quantity: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove from cart", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
} 