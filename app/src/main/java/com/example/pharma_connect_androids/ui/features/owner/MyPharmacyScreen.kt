package com.example.pharma_connect_androids.ui.features.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.pharma_connect_androids.ui.features.admin.PharmacyDetailContent
import com.example.pharma_connect_androids.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPharmacyScreen(
    viewModel: MyPharmacyViewModel = hiltViewModel(),
    onNavigateToUpdatePharmacy: (pharmacyId: String) -> Unit
) {
    val pharmacyState by viewModel.pharmacyState.collectAsState()
    val pharmacyId = viewModel.pharmacyId
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh data when the screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchMyPharmacyDetails() // Re-fetch data
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Pharmacy") },
                actions = {
                    if (pharmacyState is Resource.Success && pharmacyId != null) {
                        IconButton(onClick = { onNavigateToUpdatePharmacy(pharmacyId) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Update Pharmacy")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = pharmacyState) {
                is Resource.Loading, null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    state.data?.let {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            PharmacyDetailContent(pharmacy = it)
                        }
                    } ?: Text("Pharmacy details not found.", modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    val message = if(viewModel.pharmacyId == null){
                        "You have not registered a pharmacy yet."
                    } else {
                         "Error loading pharmacy details: ${state.message}"
                    }
                    Text(
                        text = message,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Preview might be complex due to SessionManager dependency
// @Preview(showBackground = true)
// @Composable
// fun MyPharmacyScreenPreview() {
//     PharmaConnectAndroidSTheme {
//         MyPharmacyScreen(onNavigateToUpdatePharmacy = {})
//     }
// } 