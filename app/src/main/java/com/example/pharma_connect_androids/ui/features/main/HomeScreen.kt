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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToJoinPharmacy: () -> Unit
) {
    val scrollState = rememberScrollState()

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
                text = "Find medicines, compare prices, and check availability instantly.",
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
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                 Button(onClick = { 
                     onNavigateToRegister()
                 }) {
                     Text("Sign Up")
                 }
                 Spacer(modifier = Modifier.width(8.dp))
                 OutlinedButton(onClick = { 
                     onNavigateToJoinPharmacy()
                 }) {
                     Text("Join As Pharmacy")
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

        Spacer(modifier = Modifier.height(32.dp))

        // 3. About Us Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE9EFFF)) // Light blue background like lightbg
                .padding(vertical = 32.dp, horizontal = 16.dp)
        ) {
            // Simple Column layout for now, add Image later if needed
            Text(
                text = "About US",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF286AA7), // Primary color
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "At Pharma Connect, we're dedicated to making it easier for you to find the medicines you need. Our platform connects you with trusted pharmacies across the city, giving you access to medicine availability, prices, and locations all in one place. With a quick search, you can compare prices and check real-time availability to save time and avoid unnecessary trips.\n\nPharma Connect ensures that finding the right medicine is simple, efficient, and hassle-free. We believe in leveraging technology to improve access to healthcare. By connecting people to pharmacies seamlessly, Pharma Connect is transforming the way you find and access the medicines you need.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF808080) // Body text color
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Product Demo Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE9EFFF)) // Light blue background like lightbg
                .padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center align content
        ) {
            Text(
                text = "Product Demo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF286AA7), // Primary color
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Our platform is easy to use. Here is a short demo of our product.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF808080), // Body text color
                 modifier = Modifier.padding(bottom = 16.dp),
                 textAlign = TextAlign.Center
            )
            // Placeholder Box for Demo Video/Image
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Take up 80% of width
                    .height(200.dp)
                    .background(Color.Gray)
            ) {
                Text("Demo Placeholder", Modifier.align(Alignment.Center), color = Color.White)
            }
             // Add placeholder for illustration if needed
             // Image(painter = painterResource(id = R.drawable.product_demo_placeholder), ...)
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
            onNavigateToJoinPharmacy = {}
        )
    }
} 