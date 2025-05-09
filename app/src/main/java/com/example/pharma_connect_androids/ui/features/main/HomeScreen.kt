package com.example.pharma_connect_androids.ui.features.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pharma_connect_androids.R // Assuming you have placeholder images in drawable
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
import androidx.navigation.NavController
import com.example.pharma_connect_androids.ui.navigation.Screen // Ensure Screen is imported
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    onNavigateToRegister: () -> Unit,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background) // Use theme background
    ) {
        // 1. Hero Section (Mimicking structure)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Find medicines",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Search Bar Placeholder
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for Medicine") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            navController.navigate(Screen.Search.createRoute(searchQuery)) {
                                // Optional: Configure navigation options, e.g., launchSingleTop = true
                            }
                            keyboardController?.hide() // Hide keyboard after search
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                 Button(onClick = { 
                     onNavigateToRegister()
                 }) {
                     Text("Sign Up")
                 }
             }
        }

        // Placeholder for Hero Illustration (Optional, depending on if you add one)
        // Image(painter = painterResource(id = R.drawable.hero_illustration_placeholder), ...)

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Nearby Pharmacies Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Nearby pharmacies",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Placeholder for Carousel
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(5) { // Display 5 placeholder cards
                    PharmacyPlaceholderCard()
                }
            }
        }




        // TODO: Add Footer Section
        Spacer(modifier = Modifier.height(32.dp))

    }
}

// Placeholder Composable for Pharmacy Card in Carousel
@Composable
fun PharmacyPlaceholderCard() {
    Card(
        modifier = Modifier.size(width = 180.dp, height = 120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pharmacy Info", textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PharmaConnectAndroidSTheme {
        HomeScreen(
            onNavigateToRegister = {},
            navController = rememberNavController() // For preview purposes
        )
    }
} 