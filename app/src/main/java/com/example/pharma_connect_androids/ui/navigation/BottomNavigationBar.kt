package com.example.pharma_connect_androids.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem> // Pass the list of items (e.g., BottomNavItems.UserItems)
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        // Optional styling (e.g., containerColor)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up using the route string of the graph's start destination
                            navController.graph.findStartDestination().route?.let { startDestinationRoute ->
                                popUpTo(startDestinationRoute) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = true, // Or false depending on your design preference
                // Optional: Customize colors
                 colors = NavigationBarItemDefaults.colors(
                     // indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                     // selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                     // unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                     // selectedTextColor = MaterialTheme.colorScheme.onSurface,
                     // unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                 )
            )
        }
    }
}

// Helper extension function (optional but recommended by documentation)
private val NavController.currentDestinationRoute: String?
    get() = currentBackStackEntry?.destination?.route 