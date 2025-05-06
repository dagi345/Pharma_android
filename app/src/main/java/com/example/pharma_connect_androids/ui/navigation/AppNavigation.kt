package com.example.pharma_connect_androids.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.pharma_connect_androids.ui.features.auth.login.LoginScreen
import com.example.pharma_connect_androids.ui.features.auth.register.RegisterScreen
import com.example.pharma_connect_androids.ui.features.main.MainScreen
import com.example.pharma_connect_androids.ui.features.pharmacy.JoinPharmacyScreen
import com.example.pharma_connect_androids.ui.features.pharmacy.MapPickerScreen
import com.google.android.gms.maps.model.LatLng
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.ui.features.pharmacy.JoinPharmacyViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pharma_connect_androids.ui.features.admin.AdminApplicationScreen
import com.example.pharma_connect_androids.ui.features.admin.ApplicationDetailScreen
import com.example.pharma_connect_androids.ui.features.admin.AdminApplicationViewModel
import com.example.pharma_connect_androids.ui.features.pharmacy.PharmacistListScreen
import com.example.pharma_connect_androids.ui.features.auth.register.PharmacistRegisterScreen

/**
 * Defines the overall navigation structure, including Auth and Main flows.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.AuthNavGraph.route // Start with Auth
    ) {
        // Authentication Navigation Graph
        navigation(
            startDestination = Screen.Login.route,
            route = Screen.AuthNavGraph.route
        ) {
            composable(
                route = Screen.Login.route,
                arguments = listOf(navArgument("email") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                LoginScreen(
                    onLoginSuccess = {
                        // Navigate to MainScreen, clearing Auth graph
                        navController.navigate(Screen.MainNavGraph.route) { // Keep MainNavGraph route name for navigation target
                            popUpTo(Screen.AuthNavGraph.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onSkipLogin = {
                         // Navigate to MainScreen, clearing Auth graph
                        navController.navigate(Screen.MainNavGraph.route) { // Keep MainNavGraph route name for navigation target
                            popUpTo(Screen.AuthNavGraph.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegistrationSuccess = { email ->
                        navController.navigate(Screen.Login.route + "?email=$email") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() },
                    onNavigateToPharmacistRegister = {
                        navController.navigate(Screen.PharmacistRegister.createRoute(null))
                    }
                )
            }
            composable(
                route = Screen.PharmacistRegister.route,
                arguments = listOf(
                    navArgument(Screen.PharmacistRegister.ARG_PHARMACY_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                PharmacistRegisterScreen(
                    onRegistrationSuccess = { email ->
                        navController.navigate(Screen.Login.route + "?email=$email") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
        }

        // Route that leads to the MainScreen (Scaffold with Bottom Nav)
        composable(Screen.MainNavGraph.route) {
            // Pass the navigation actions using the top-level navController
            MainScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToJoinPharmacy = { navController.navigate(Screen.JoinPharmacy.route) },
                onNavigateToAdminApplications = { appId ->
                    navController.navigate(Screen.ApplicationDetail.createRoute(appId))
                },
                onNavigateToAdminApplicationList = { 
                    navController.navigate(Screen.AdminApplications.route)
                }
            )
        }
        
        // Join Pharmacy Screen Route (at the top level for now)
        composable(Screen.JoinPharmacy.route) { backStackEntry ->
             JoinPharmacyScreen(
                 onNavigateBack = { navController.popBackStack() },
                 onNavigateToMapPicker = { currentLat, currentLng ->
                    navController.navigate(Screen.MapPicker.route)
                 },
                 onSubmitSuccess = { 
                     navController.popBackStack() 
                 }
             )
             navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<LatLng>("selected_location")?.observe(backStackEntry) { result ->
                    navController.currentBackStackEntry?.savedStateHandle?.remove<LatLng>("selected_location")
                    
                    val viewModelProvider = ViewModelProvider(backStackEntry)
                    val viewModel = viewModelProvider.get(JoinPharmacyViewModel::class.java)
                    
                    viewModel.onLocationSelected(result.latitude, result.longitude)
                 }
        }

        // Map Picker Screen Route
        composable(Screen.MapPicker.route) {
            val viewModel: JoinPharmacyViewModel = hiltViewModel(navController.previousBackStackEntry!!)
            val initialLatLng = LatLng(viewModel.state.collectAsState().value.latitude, viewModel.state.collectAsState().value.longitude)
            MapPickerScreen(
                initialLatLng = initialLatLng,
                onLocationSelected = { latLng ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_location", latLng)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin Application List Screen Route - MOVED inside MainContentNavHost
//        composable(Screen.AdminApplications.route) { navBackStackEntry ->
//            AdminApplicationScreen(
//                onNavigateToDetail = { appId ->
//                    navController.navigate(Screen.ApplicationDetail.createRoute(appId))
//                }
//            )
//        }

        // Application Detail Screen Route
        composable(
            route = Screen.ApplicationDetail.route,
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val adminViewModel: AdminApplicationViewModel = hiltViewModel(navController.getBackStackEntry(Screen.AdminApplications.route))
            ApplicationDetailScreen(
                adminViewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pharmacist List Screen (Accessed by Pharmacy Owner)
        composable(
            route = Screen.PharmacistList.route, // "pharmacist_list/{pharmacyId}"
            arguments = listOf(navArgument("pharmacyId") { type = NavType.StringType })
        ) { backStackEntry ->
            // ViewModel will get pharmacyId from SavedStateHandle
            PharmacistListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 