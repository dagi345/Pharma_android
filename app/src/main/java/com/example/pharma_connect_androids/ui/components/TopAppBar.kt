package com.example.pharma_connect_androids.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pharma_connect_androids.R // Ensure you have R.drawable.logo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaConnectTopAppBar() {
    
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo), // Replace with your actual logo resource
                    contentDescription = "Pharma Connect Logo",
                    modifier = Modifier.height(32.dp) // Adjust size as needed
                )
                // Removed the redundant Text composable
                // Spacer(modifier = Modifier.width(8.dp))
                // Text(
                //     text = "PharmaConnect",
                //     style = MaterialTheme.typography.titleLarge,
                //     fontWeight = FontWeight.Bold
                // )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFE9EFFF) // Use lightbg color from frontend
        )
        // Add navigationIcon or actions if needed later
    )
} 