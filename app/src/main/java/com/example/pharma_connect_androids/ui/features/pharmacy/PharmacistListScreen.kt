package com.example.pharma_connect_androids.ui.features.pharmacy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.data.models.Pharmacist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistListScreen(
    viewModel: PharmacistListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Pharmacists") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                 colors = TopAppBarDefaults.topAppBarColors(
                     containerColor = MaterialTheme.colorScheme.surfaceVariant
                 )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Referral Link Section
            ReferralLinkCard(state.referralLink)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Pharmacist List
            Text("Current Pharmacists", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                state.pharmacists.isEmpty() -> {
                     Text(
                        text = "No pharmacists found for this pharmacy yet.",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                else -> {
                    PharmacistList(pharmacists = state.pharmacists)
                }
            }
        }
    }
}

@Composable
fun ReferralLinkCard(referralLink: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pharmacist Referral Link",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Share this link to invite pharmacists:")
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = referralLink,
                    onValueChange = {}, // Read-only
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    label = { Text("Link") },
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    copyToClipboard(context, referralLink)
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Link")
                }
            }
        }
    }
}

@Composable
fun PharmacistList(pharmacists: List<Pharmacist>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(pharmacists, key = { it._id }) { pharmacist ->
            PharmacistItem(pharmacist)
        }
    }
}

@Composable
fun PharmacistItem(pharmacist: Pharmacist) {
    Card(
        modifier = Modifier.fillMaxWidth(),
         elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
         Row(
             modifier = Modifier.padding(16.dp),
             verticalAlignment = Alignment.CenterVertically
         ) {
            Column {
                Text(
                    text = "${pharmacist.firstName} ${pharmacist.lastName}", 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.SemiBold
                )
                 Text(
                    text = pharmacist.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
            }
        }
    }
}

// Helper function for clipboard
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Referral Link", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
} 