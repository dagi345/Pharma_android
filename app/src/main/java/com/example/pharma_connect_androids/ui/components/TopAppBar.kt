package com.example.pharma_connect_androids.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pharma_connect_androids.R // Ensure you have R.drawable.logo
import com.example.pharma_connect_androids.domain.model.UserData // Import UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaConnectTopAppBar(
    currentUser: UserData?,
    onSignInClicked: () -> Unit,
    onSignOutClicked: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Pharma Connect Logo",
                    modifier = Modifier.height(32.dp)
                )
            }
        },
        actions = {
            if (currentUser != null) {
                // User is logged in - show profile icon and dropdown
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "User Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Display User's First Name if available, otherwise UserID
                        val displayName = currentUser.firstName?.takeIf { it.isNotBlank() } ?: currentUser.userId
                        DropdownMenuItem(
                            text = { Text(displayName) },
                            onClick = { showMenu = false } // Or navigate to a full profile screen
                        )
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            onClick = {
                                showMenu = false
                                onSignOutClicked()
                            }
                        )
                    }
                }
            } else {
                // User is not logged in - show Sign In button
                Button(onClick = onSignInClicked) {
                    Text("Sign In")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFE9EFFF)
        )
    )
} 