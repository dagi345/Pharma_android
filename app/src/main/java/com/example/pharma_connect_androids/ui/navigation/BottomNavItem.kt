package com.example.pharma_connect_androids.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.outlined.Inventory2 // Using Inventory2 for outlined
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.PeopleOutline

/**
 * Represents items in the bottom navigation bar.
 */
data class BottomNavItem(
    val label: String,
    val route: String, // Corresponds to a route in MainNavGraph or top-level graph
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Define the actual items - Add more as needed for different roles
object BottomNavItems {
    // --- Common/User Items ---
    val Home = BottomNavItem(
        label = "Home",
        route = Screen.Home.route, // Matches route in AppNavigation
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    val Search = BottomNavItem(
        label = "Search",
        route = Screen.Search.route, // Use route from Screen object
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    val Profile = BottomNavItem(
        label = "Profile",
        route = Screen.Profile.route, // Use route from Screen object
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )
    val UserItems = listOf(Home, Search, Profile)

    // --- Owner Items ---
    val Dashboard = BottomNavItem(
        label = "Dashboard",
        route = Screen.Dashboard.route, // Use Screen object
        selectedIcon = Icons.Filled.Dashboard, 
        unselectedIcon = Icons.Outlined.Dashboard
    )
    val Inventory = BottomNavItem(
        label = "Inventory",
        route = Screen.Inventory.route, // Use Screen object
        selectedIcon = Icons.Filled.Inventory, 
        unselectedIcon = Icons.Outlined.Inventory2 // Using Inventory2 for outlined
    )
     val ManagePharmacists = BottomNavItem(
        label = "Pharmacists",
        // Linking to profile for now, owner accesses list via button on profile screen
         route = Screen.Profile.route, 
        selectedIcon = Icons.Filled.People, 
        unselectedIcon = Icons.Outlined.PeopleOutline
    )
    // NOTE: Dashboard/Inventory routes need implementation in MainContentNavHost
    val OwnerItems = listOf(Dashboard, Inventory, ManagePharmacists, Profile)

    // --- Admin Items ---
     val Applications = BottomNavItem(
        label = "Applications",
        route = Screen.AdminApplications.route, // Use Screen object
        selectedIcon = Icons.Filled.PendingActions, // Add import if needed
        unselectedIcon = Icons.Outlined.PendingActions // Add import if needed
    )
     val Pharmacies = BottomNavItem(
        label = "Pharmacies",
        route = "admin_pharmacies_screen", // Placeholder - Needs Screen def & NavGraph entry
        selectedIcon = Icons.Filled.LocalPharmacy, // Add import if needed
        unselectedIcon = Icons.Outlined.LocalPharmacy // Add import if needed
    )
     val Medicines = BottomNavItem(
        label = "Medicines",
        route = "admin_medicines_screen", // Placeholder - Needs Screen def & NavGraph entry
        selectedIcon = Icons.Filled.Medication, // Add import if needed
        unselectedIcon = Icons.Outlined.Medication // Add import if needed
    )
    // NOTE: Pharmacies/Medicines routes need implementation
    val AdminItems = listOf(Applications, Pharmacies, Medicines) 
}

// Add necessary icon imports:
