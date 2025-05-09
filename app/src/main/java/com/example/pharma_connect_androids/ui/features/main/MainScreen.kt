package com.example.pharma_connect_androids.ui.features.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.pharma_connect_androids.domain.model.UserRole
import com.example.pharma_connect_androids.ui.navigation.BottomNavItems
import com.example.pharma_connect_androids.ui.navigation.BottomNavigationBar
import com.example.pharma_connect_androids.ui.navigation.Screen
import com.example.pharma_connect_androids.ui.features.search.SearchScreen
import com.example.pharma_connect_androids.ui.features.profile.ProfileScreen
import com.example.pharma_connect_androids.ui.features.admin.AdminApplicationScreen
import com.example.pharma_connect_androids.ui.features.admin.AdminAddMedicineScreen
import com.example.pharma_connect_androids.ui.features.admin.AdminMedicinesScreen
import com.example.pharma_connect_androids.ui.features.admin.UpdateMedicineScreen
import com.example.pharma_connect_androids.ui.features.admin.AdminPharmaciesScreen
import com.example.pharma_connect_androids.ui.features.admin.AdminPharmacyDetailScreen
import com.example.pharma_connect_androids.ui.features.owner.MyPharmacyScreen
import com.example.pharma_connect_androids.ui.features.pharmacy.JoinPharmacyScreen
import com.example.pharma_connect_androids.ui.features.owner.OwnerAddMedicineScreen
import com.example.pharma_connect_androids.ui.features.owner.OwnerInventoryScreen
import com.example.pharma_connect_androids.ui.features.owner.UpdateInventoryItemScreen
import com.example.pharma_connect_androids.ui.features.pharmacy.JoinPharmacyViewModel
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.livedata.observeAsState
import com.example.pharma_connect_androids.ui.components.PharmaConnectTopAppBar

/**
 * Main layout composable that includes the Scaffold and Bottom Navigation.
 * It hosts the NavHost for the main application screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToAdminApplications: (applicationId: String) -> Unit,
    onNavigateToAdminApplicationList: () -> Unit,
    onNavigateToPharmacyDetail: (pharmacyId: String) -> Unit
) {
    val bottomNavController = rememberNavController()
    val mainState by viewModel.state.collectAsState()

    val bottomNavItems = when (mainState.userRole) {
        UserRole.OWNER -> BottomNavItems.OwnerItems
        UserRole.ADMIN -> BottomNavItems.AdminItems
        UserRole.USER -> BottomNavItems.UserItems
        UserRole.PHARMACIST -> BottomNavItems.PharmacistItems
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
            onNavigateToAdminApplications = onNavigateToAdminApplications,
            onNavigateToAdminApplicationList = onNavigateToAdminApplicationList,
            onNavigateToPharmacyDetail = onNavigateToPharmacyDetail
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
    onNavigateToAdminApplications: (applicationId: String) -> Unit,
    onNavigateToAdminApplicationList: () -> Unit,
    onNavigateToPharmacyDetail: (pharmacyId: String) -> Unit
) {
    val startDestination = Screen.Home.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRegister = onNavigateToRegister,
                navController = navController
            )
        }
        composable(
            route = Screen.Search.route,
            arguments = listOf(navArgument(Screen.Search.ARG_QUERY) { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString(Screen.Search.ARG_QUERY)
            SearchScreen(
                initialQuery = query,
                onNavigateToPharmacyDetail = onNavigateToPharmacyDetail
            )
        }
        composable(Screen.JoinPharmacy.route) { backStackEntry ->
            JoinPharmacyScreen(
                onNavigateBack = { 
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.JoinPharmacy.route) { inclusive = true } }
                },
                onSubmitSuccess = { 
                    val joinViewModel: JoinPharmacyViewModel = hiltViewModel(backStackEntry)
                    joinViewModel.resetSubmissionSuccess() 
                    navController.navigate(Screen.Home.route) { 
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MyPharmacy.route) {
            MyPharmacyScreen(onNavigateToUpdatePharmacy = { pharmacyId ->
                navController.navigate(Screen.UpdatePharmacy.createRoute(pharmacyId))
            })
        }
        composable(Screen.OwnerAddMedicine.route) { 
            OwnerAddMedicineScreen()
        }
        composable(Screen.OwnerInventory.route) { 
            OwnerInventoryScreen(onNavigateToUpdateItem = { pharmacyId, inventoryItemId ->
                 navController.navigate(Screen.UpdateInventoryItem.createRoute(pharmacyId, inventoryItemId))
            })
         }
        composable(
            route = Screen.UpdatePharmacy.route,
            arguments = listOf(navArgument("pharmacyId") { type = NavType.StringType })
        ) { backStackEntry ->
            JoinPharmacyScreen(
                onNavigateBack = { navController.popBackStack() },
                onSubmitSuccess = { 
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.UpdateInventoryItem.route,
            arguments = listOf(
                 navArgument("pharmacyId") { type = NavType.StringType },
                 navArgument("inventoryItemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
             UpdateInventoryItemScreen(onNavigateBack = { navController.popBackStack() })
         }
        composable(Screen.AdminApplications.route) {
            AdminApplicationScreen(
                onNavigateToDetail = onNavigateToAdminApplications
            )
        }
        composable(Screen.AdminPharmacies.route) { 
            AdminPharmaciesScreen(onNavigateToPharmacyDetail = { pharmacyId ->
                navController.navigate(Screen.AdminPharmacyDetail.createRoute(pharmacyId))
            })
        }
        composable(Screen.AdminMedicines.route) { 
            AdminMedicinesScreen(onNavigateToUpdateMedicine = { medicineId ->
                navController.navigate(Screen.AdminUpdateMedicine.createRoute(medicineId))
            })
        }
        composable(Screen.AdminAddMedicine.route) { 
            AdminAddMedicineScreen()
        }
        composable(
            route = Screen.AdminUpdateMedicine.route,
            arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
        ) {
            backStackEntry ->
            val medicineId = backStackEntry.arguments?.getString("medicineId")
            UpdateMedicineScreen(
                medicineId = medicineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AdminPharmacyDetail.route,
            arguments = listOf(navArgument("pharmacyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pharmacyId = backStackEntry.arguments?.getString("pharmacyId")
            AdminPharmacyDetailScreen(
                pharmacyId = pharmacyId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Simple placeholder for screens not yet built
@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name Screen (Placeholder)")
    }
} 