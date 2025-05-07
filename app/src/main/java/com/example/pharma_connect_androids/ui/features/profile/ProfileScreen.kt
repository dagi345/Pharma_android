package com.example.pharma_connect_androids.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit, // Callback to navigate back to Auth graph after sign out
    onNavigateToAdminApplicationList: () -> Unit, // For Admin
    onNavigateToPharmacistList: (pharmacyId: String) -> Unit // For Owner
) {
    val state by viewModel.state.collectAsState()

    // Navigate back to Auth flow when isSignedOut becomes true
    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) {
            viewModel.resetSignOutState() // Reset state before navigation
            onNavigateToAuth()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                state.userEmail?.let {
                    Text(
                        text = "Logged in as: $it (${state.userRole ?: "Unknown Role"})", // Show role
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Admin Button
                if (state.userRole?.equals("admin", ignoreCase = true) == true) {
                    Button(onClick = onNavigateToAdminApplicationList) {
                        Text("Review Applications")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Pharmacy Owner Button
                 if (state.userRole?.equals("owner", ignoreCase = true) == true && state.pharmacyId != null) {
                     Button(onClick = { 
                         // Ensure pharmacyId is not null before navigating
                         state.pharmacyId?.let { onNavigateToPharmacistList(it) }
                      }) {
                        Text("Manage Pharmacists")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                 }

                Button(onClick = { viewModel.signOut() }) {
                    Text("Sign Out")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    PharmaConnectAndroidSTheme {
         // Simulate state for preview
         val previewState = ProfileScreenState(
             userEmail = "owner@example.com",
             userRole = "owner",
             pharmacyId = "previewPharmacy123", // Added for preview
             isLoading = false
         )
         Box(
             modifier = Modifier
                 .fillMaxSize()
                 .padding(16.dp),
             contentAlignment = Alignment.Center
         ) {
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Text("Profile", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 24.dp))
                 Text(
                    text = "Logged in as: ${previewState.userEmail} (${previewState.userRole})",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                 )
                  // Admin Button (Preview)
                 Button(onClick = { /* Preview: No Nav */ }) {
                     Text("Review Applications")
                 }
                 Spacer(modifier = Modifier.height(16.dp))
                 // Owner Button (Preview)
                 Button(onClick = { /* Preview: No Nav */ }) {
                     Text("Manage Pharmacists")
                 }
                 Spacer(modifier = Modifier.height(16.dp))
                 // Sign Out Button (Preview)
                 Button(onClick = { }) {
                     Text("Sign Out")
                 }
             }
         }
    }
} 