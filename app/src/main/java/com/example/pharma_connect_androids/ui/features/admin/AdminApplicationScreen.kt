package com.example.pharma_connect_androids.ui.features.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.data.models.Application
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationScreen(
    viewModel: AdminApplicationViewModel = hiltViewModel(),
    onNavigateToDetail: (applicationId: String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Review Applications") },
                 colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                     containerColor = MaterialTheme.colorScheme.surfaceVariant
                 )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp)) {
            if (state.isLoading && state.applications.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.error?.let {
                Text(
                    text = "Error: $it", 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }

            if (state.applications.isEmpty() && !state.isLoading && state.error == null) {
                 Text(
                    text = "No applications found.", 
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.applications, key = { it._id }) { application ->
                        ApplicationItem(
                            application = application,
                            isUpdating = state.updateLoadingId == application._id,
                            updateError = state.updateErrorId?.takeIf { it.first == application._id }?.second,
                            onApprove = { viewModel.updateApplicationStatus(application._id, "Approved") },
                            onReject = { viewModel.updateApplicationStatus(application._id, "Closed") },
                            onClick = { onNavigateToDetail(application._id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationItem(
    application: Application,
    isUpdating: Boolean,
    updateError: String?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                application.pharmacyName, 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
             Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Owner:", application.ownerName)
            DetailRow("Contact:", application.contactNumber)
            DetailRow("Email:", application.email)
             DetailRow("Status:", application.status)
            // TODO: Add navigation to a detail screen to show more info like address, images etc.
            
            Spacer(modifier = Modifier.height(12.dp))

            if (updateError != null) {
                Text("Error: $updateError", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Show buttons only if status is Pending
            if (application.status.equals("Pending", ignoreCase = true)) {
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                 ) {
                     if (isUpdating) {
                         CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 2.dp)
                     } else {
                         OutlinedButton(
                             onClick = onReject, 
                             colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                             border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MaterialTheme.colorScheme.error))
                         ) {
                             Icon(Icons.Filled.Close, contentDescription = "Reject", modifier = Modifier.size(ButtonDefaults.IconSize))
                             Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                             Text("Reject")
                         }
                         Spacer(modifier = Modifier.width(12.dp))
                         Button(
                             onClick = onApprove, 
                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
                         ) {
                              Icon(Icons.Filled.Check, contentDescription = "Approve", modifier = Modifier.size(ButtonDefaults.IconSize))
                              Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                             Text("Approve")
                         }
                     }
                 }
            }
        }
    }
}

// Helper composable for consistent detail row styling
@Composable
fun DetailRow(label: String, value: String) {
    Row {
        Text("$label ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
} 