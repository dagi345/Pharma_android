package com.example.pharma_connect_androids.ui.features.pharmacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.ui.features.admin.PharmacyDetailContent // Reusing this

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPharmacyDetailScreen(
    viewModel: UserPharmacyDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.pharmacy?.name ?: "Pharmacy Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.pharmacy?.contact?.let { phoneNumber ->
                        if (phoneNumber.isNotBlank()) {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Filled.Call, contentDescription = "Call Pharmacy")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // TEMPORARY DEBUG TEXT

            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                state.error != null -> {
                    Text(
                        text = state.error!!, // Smart-cast to non-null
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.pharmacy != null -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        PharmacyDetailContent(pharmacy = state.pharmacy!!) // Smart-cast to non-null
                    }
                }
                else -> {
                    // This case covers: !isLoading && error == null && pharmacy == null
                    Text("Pharmacy details not found.", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
} 