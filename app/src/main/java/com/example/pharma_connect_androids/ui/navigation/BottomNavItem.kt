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
// Add necessary icon imports here:
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.filled.Medication // For Medicines
import androidx.compose.material.icons.outlined.Medication // Use standard outlined icon
import androidx.compose.material.icons.filled.AddCircle // For Add Medicine
import androidx.compose.material.icons.outlined.AddCircleOutline

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

    // --- Admin Items (Matching the desired layout) ---
    val AdminPharmacies = BottomNavItem(
        label = "Pharmacies",
        route = Screen.AdminPharmacies.route, // Needs Screen def & NavGraph entry
        selectedIcon = Icons.Filled.LocalPharmacy,
        unselectedIcon = Icons.Outlined.LocalPharmacy
    )
    val AdminMedicines = BottomNavItem(
        label = "Medicines",
        route = Screen.AdminMedicines.route, // Needs Screen def & NavGraph entry
        selectedIcon = Icons.Filled.Medication,
        unselectedIcon = Icons.Outlined.Medication // Use standard outlined icon
    )
    val AdminAddMedicine = BottomNavItem(
        label = "Add Medicine",
        route = Screen.AdminAddMedicine.route, // Needs Screen def & NavGraph entry
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline
    )
    val AdminApplications = BottomNavItem(
        label = "Applications",
        route = Screen.AdminApplications.route, // This one is functional
        selectedIcon = Icons.Filled.PendingActions,
        unselectedIcon = Icons.Outlined.PendingActions
    )
    // Update AdminItems list to match the photo/request
    val AdminItems = listOf(AdminPharmacies, AdminMedicines, AdminAddMedicine, AdminApplications)
}

// Remove imports from the bottom
// Remove unused placeholder icon imports if necessary
// import androidx.compose.material.icons.filled.LocalPharmacy
// import androidx.compose.material.icons.outlined.LocalPharmacy
// import androidx.compose.material.icons.filled.Medication
// import androidx.compose.material.icons.outlined.Medication
