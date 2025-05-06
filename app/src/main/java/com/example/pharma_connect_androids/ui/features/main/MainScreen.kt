package com.example.pharma_connect_androids.ui.features.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pharma_connect_androids.domain.model.UserRole
import com.example.pharma_connect_androids.ui.components.PharmaConnectTopAppBar
import com.example.pharma_connect_androids.ui.navigation.BottomNavItem
import com.example.pharma_connect_androids.ui.navigation.BottomNavItems
import com.example.pharma_connect_androids.ui.navigation.BottomNavigationBar
import com.example.pharma_connect_androids.ui.navigation.Screen
import com.example.pharma_connect_androids.ui.features.search.SearchScreen
import com.example.pharma_connect_androids.ui.features.profile.ProfileScreen
import com.example.pharma_connect_androids.ui.features.admin.AdminApplicationScreen

/**
 * Main layout composable that includes the Scaffold and Bottom Navigation.
 * It hosts the NavHost for the main application screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToJoinPharmacy: () -> Unit,
    onNavigateToAdminApplications: (applicationId: String) -> Unit,
    onNavigateToAdminApplicationList: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val mainState by viewModel.state.collectAsState()

    val bottomNavItems = when (mainState.userRole) {
        UserRole.OWNER -> BottomNavItems.OwnerItems
        UserRole.ADMIN -> BottomNavItems.AdminItems
        UserRole.USER -> BottomNavItems.UserItems
        UserRole.PHARMACIST -> BottomNavItems.UserItems
        UserRole.UNKNOWN -> BottomNavItems.UserItems
    }

    Scaffold(
        topBar = {
            PharmaConnectTopAppBar()
        },
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        MainContentNavHost(
            navController = bottomNavController,
            innerPadding = innerPadding,
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToJoinPharmacy = onNavigateToJoinPharmacy,
            onNavigateToAdminApplications = onNavigateToAdminApplications,
            onNavigateToAdminApplicationList = onNavigateToAdminApplicationList
        )
    }
}

/**
 * NavHost specifically for the main content area within the Scaffold.
 */
@Composable
fun MainContentNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onNavigateToRegister: () -> Unit,
    onNavigateToJoinPharmacy: () -> Unit,
    onNavigateToAdminApplications: (applicationId: String) -> Unit,
    onNavigateToAdminApplicationList: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRegister = onNavigateToRegister,
                onNavigateToJoinPharmacy = onNavigateToJoinPharmacy
            )
        }
        composable(Screen.Search.route) {
            SearchScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToAdminApplicationList = onNavigateToAdminApplicationList,
                onNavigateToPharmacistList = { pharmacyId ->
                    navController.navigate(Screen.PharmacistList.createRoute(pharmacyId))
                }
            )
        }
        composable(Screen.AdminApplications.route) {
            AdminApplicationScreen(
                onNavigateToDetail = onNavigateToAdminApplications
            )
        }
        // Placeholder screens for Admin
        composable(Screen.AdminPharmacies.route) { PlaceholderScreen("Pharmacies") }
        composable(Screen.AdminMedicines.route) { PlaceholderScreen("Medicines") }
        composable(Screen.AdminAddMedicine.route) { PlaceholderScreen("Add Medicine") }

        // Placeholders for Owner
        composable(Screen.Dashboard.route) { PlaceholderScreen("Dashboard") }
        composable(Screen.Inventory.route) { PlaceholderScreen("Inventory") }
    }
}

// Simple placeholder for screens not yet built
@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name Screen (Placeholder)")
    }
} 