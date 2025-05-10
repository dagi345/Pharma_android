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
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.filled.AddBusiness // Import for Join Us
import androidx.compose.material.icons.outlined.AddBusiness // Import for Join Us
import androidx.compose.material.icons.filled.ShoppingCart // Import for Cart/MyMedicines
import androidx.compose.material.icons.outlined.ShoppingCart // Import for Cart/MyMedicines

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
    // --- Common/User Items (Revised) ---
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
    // Removed Profile Item from User Nav
    // val Profile = BottomNavItem(...) 
    
    // New Join Us Item
    val JoinUs = BottomNavItem(
        label = "Join Us",
        route = Screen.JoinPharmacy.route, // Reuse existing route
        selectedIcon = Icons.Filled.AddBusiness, 
        unselectedIcon = Icons.Outlined.AddBusiness
    )

    val MyMedicines = BottomNavItem(
        label = "Cart",
        route = Screen.MyMedicines.route,
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart
    )

    // Update UserItems list
    val UserItems = listOf(Home, Search, MyMedicines, JoinUs)

    // --- Owner Items (Revised) ---
    val MyPharmacy = BottomNavItem(
        label = "My Pharmacy",
        route = Screen.MyPharmacy.route, // New Route - Define in Screen.kt
        selectedIcon = Icons.Filled.Storefront, 
        unselectedIcon = Icons.Outlined.Storefront
    )
    val OwnerAddMedicine = BottomNavItem(
        label = "Add Medicine",
        route = Screen.OwnerAddMedicine.route, // New Route - Define in Screen.kt (placeholder)
        selectedIcon = Icons.Filled.AddCircle, // Reusing AddCircle icon
        unselectedIcon = Icons.Outlined.AddCircleOutline
    )
    val OwnerInventory = BottomNavItem(
        label = "Inventory",
        route = Screen.OwnerInventory.route, // New Route - Define in Screen.kt (placeholder)
        selectedIcon = Icons.Filled.Inventory, 
        unselectedIcon = Icons.Outlined.Inventory2
    )
    // Update OwnerItems list to ONLY include the required three
    val OwnerItems = listOf(MyPharmacy, OwnerAddMedicine, OwnerInventory)

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
    val AdminItems = listOf(AdminPharmacies, AdminMedicines, AdminAddMedicine, AdminApplications)

    // --- Pharmacist Items (Assuming same as user for now) ---
    val PharmacistItems = UserItems // Assuming Pharmacists also see Home, Search, Join Us
}

// Remove imports from the bottom
// Remove unused placeholder icon imports if necessary
// import androidx.compose.material.icons.filled.LocalPharmacy
// import androidx.compose.material.icons.outlined.LocalPharmacy
// import androidx.compose.material.icons.filled.Medication
// import androidx.compose.material.icons.outlined.Medication
