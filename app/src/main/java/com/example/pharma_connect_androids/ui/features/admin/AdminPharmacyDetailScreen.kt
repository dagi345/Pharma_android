package com.example.pharma_connect_androids.ui.features.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // ktlint-disable no-wildcard-imports
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPharmacyDetailScreen(
    viewModel: AdminPharmacyDetailViewModel = hiltViewModel(),
    pharmacyId: String?,
    onNavigateBack: () -> Unit
) {
    val pharmacyState by viewModel.pharmacyState.collectAsState()

    LaunchedEffect(pharmacyId) {
        pharmacyId?.let {
            viewModel.fetchPharmacyDetails(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pharmacy Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = pharmacyState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    state.data?.let {
                        PharmacyDetailContent(pharmacy = it)
                    } ?: Text("Pharmacy details not found.", modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center))
                }
                null -> { // Initial state from ViewModel can be null before fetch
                     CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun PharmacyDetailContent(pharmacy: Pharmacy) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Name: ${pharmacy.name}", style = MaterialTheme.typography.headlineSmall)
        Text("Address: ${pharmacy.address}")
        pharmacy.city?.let { Text("City: $it") }
        Text("Contact: ${pharmacy.contactNumber}")
        Text("Email: ${pharmacy.email}")
         // Display other fields as needed, EXCLUDING medicines list and search
    }
}

// TODO: Add Preview for AdminPharmacyDetailScreen 