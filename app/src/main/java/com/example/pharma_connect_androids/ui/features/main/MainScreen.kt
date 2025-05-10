package com.example.pharma_connect_androids.ui.features.main

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.pharma_connect_androids.R
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.domain.model.UserRole
import com.example.pharma_connect_androids.ui.components.PharmaConnectTopAppBar
import com.example.pharma_connect_androids.ui.navigation.BottomNavItems
import com.example.pharma_connect_androids.ui.navigation.BottomNavigationBar
import com.example.pharma_connect_androids.ui.navigation.Screen
import com.example.pharma_connect_androids.ui.theme.PharmaConnectAndroidSTheme
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
import com.example.pharma_connect_androids.ui.features.cart.MyMedicinesScreen

/**
 * Main layout composable that includes the Scaffold and Bottom Navigation.
 * It hosts the NavHost for the main application screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    appNavController: NavHostController,
) {
    val bottomNavController = rememberNavController()
    val mainState by mainViewModel.state.collectAsState()

    LaunchedEffect(mainState.currentUser) {
        if (mainState.currentUser == null) {
            if (appNavController.currentDestination?.route?.startsWith("login") == false &&
                appNavController.currentDestination?.route != Screen.AuthNavGraph.route) {
                appNavController.navigate(Screen.Login.route) {
                    popUpTo(Screen.MainNavGraph.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val bottomNavItems = when (mainState.userRole) {
        UserRole.OWNER -> BottomNavItems.OwnerItems
        UserRole.ADMIN -> BottomNavItems.AdminItems
        UserRole.USER -> BottomNavItems.UserItems
        UserRole.PHARMACIST -> BottomNavItems.PharmacistItems
        UserRole.UNKNOWN -> BottomNavItems.UserItems
    }

    Scaffold(
        topBar = {
            PharmaConnectTopAppBar(
                currentUser = mainState.currentUser,
                onSignInClicked = {
                    appNavController.navigate(Screen.Login.createRoute(null)) {
                        popUpTo(Screen.MainNavGraph.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignOutClicked = {
                    mainViewModel.signOut()
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        MainContentNavHost(
            mainNavController = appNavController,
            bottomNavController = bottomNavController,
            innerPadding = innerPadding,
            onNavigateToSearchTabWithQuery = { query ->
                bottomNavController.navigate(Screen.Search.createRoute(query)) {
                    popUpTo(bottomNavController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

/**
 * NavHost specifically for the main content area within the Scaffold.
 */
@Composable
fun MainContentNavHost(
    mainNavController: NavHostController,
    bottomNavController: NavHostController,
    innerPadding: PaddingValues,
    onNavigateToSearchTabWithQuery: (query: String) -> Unit
) {
    val startDestination = Screen.Home.route
    
    NavHost(
        navController = bottomNavController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = mainNavController,
                onNavigateToRegister = {
                    mainNavController.navigate(Screen.Register.route)
                },
                onNavigateToSearchTabWithQuery = onNavigateToSearchTabWithQuery
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
                onNavigateToPharmacyDetail = { pharmacyId ->
                    mainNavController.navigate(Screen.UserPharmacyDetail.createRoute(pharmacyId))
                }
            )
        }
        composable(Screen.JoinPharmacy.route) { backStackEntry ->
            JoinPharmacyScreen(
                onNavigateBack = { bottomNavController.popBackStack() },
                onSubmitSuccess = { 
                    bottomNavController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MyPharmacy.route) {
            MyPharmacyScreen(onNavigateToUpdatePharmacy = { pharmacyId ->
                mainNavController.navigate(Screen.UpdatePharmacy.createRoute(pharmacyId))
            })
        }
        composable(Screen.OwnerAddMedicine.route) { 
            OwnerAddMedicineScreen()
        }
        composable(Screen.OwnerInventory.route) { 
            OwnerInventoryScreen(onNavigateToUpdateItem = { pharmacyId, inventoryItemId ->
                 mainNavController.navigate(Screen.UpdateInventoryItem.createRoute(pharmacyId, inventoryItemId))
            })
         }
        composable(
            route = Screen.UpdatePharmacy.route,
            arguments = listOf(navArgument("pharmacyId") { type = NavType.StringType })
        ) { backStackEntry ->
            JoinPharmacyScreen(
                onNavigateBack = { bottomNavController.popBackStack() },
                onSubmitSuccess = { 
                    bottomNavController.popBackStack()
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
             UpdateInventoryItemScreen(onNavigateBack = { bottomNavController.popBackStack() })
         }
        composable(Screen.AdminApplications.route) {
            AdminApplicationScreen(
                onNavigateToDetail = { appId ->
                    mainNavController.navigate(Screen.ApplicationDetail.createRoute(appId))
                }
            )
        }
        composable(Screen.AdminPharmacies.route) { 
            AdminPharmaciesScreen(onNavigateToPharmacyDetail = { pharmacyId ->
                mainNavController.navigate(Screen.AdminPharmacyDetail.createRoute(pharmacyId))
            })
        }
        composable(Screen.AdminMedicines.route) { 
            AdminMedicinesScreen(onNavigateToUpdateMedicine = { medicineId ->
                mainNavController.navigate(Screen.AdminUpdateMedicine.createRoute(medicineId))
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
                onNavigateBack = { bottomNavController.popBackStack() }
            )
        }
        composable(
            route = Screen.AdminPharmacyDetail.route,
            arguments = listOf(navArgument("pharmacyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pharmacyId = backStackEntry.arguments?.getString("pharmacyId")
            AdminPharmacyDetailScreen(
                pharmacyId = pharmacyId,
                onNavigateBack = { bottomNavController.popBackStack() }
            )
        }
        composable(Screen.MyMedicines.route) {
            MyMedicinesScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToRegister: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSearchTabWithQuery: (query: String) -> Unit
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val homeState by homeViewModel.state.collectAsState()
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                homeViewModel.onLocationPermissionGranted()
            } else {
                homeViewModel.onLocationPermissionDenied(
                    ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION)
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        homeViewModel.checkAndRequestLocationPermission()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Find medicines",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for Medicine") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) {
                        onNavigateToSearchTabWithQuery(searchQuery.trim())
                        keyboardController?.hide()
                    }
                })
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (homeState.showLocationPermissionRationale) {
            AlertDialog(
                onDismissRequest = { homeViewModel.userNotifiedAboutRationale() },
                title = { Text("Location Permission Required") },
                text = { Text("This app uses your location to show nearby pharmacies. Please grant the permission for the best experience.") },
                confirmButton = {
                    Button(onClick = {
                        homeViewModel.userNotifiedAboutRationale()
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }) { Text("Grant") }
                },
                dismissButton = {
                    Button(onClick = { homeViewModel.userNotifiedAboutRationale() }) { Text("Later") }
                }
            )
        }
        if (!homeState.locationPermissionGranted && !homeState.locationPermissionRequested && !homeState.showLocationPermissionRationale) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Enable location to see pharmacies near you.", textAlign = TextAlign.Center, modifier = Modifier.padding(bottom=8.dp))
                Button(onClick = {
                    homeViewModel.permissionRequestAttempted()
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Enable Location")
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Nearby pharmacies",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when {
                homeState.isLoading && homeState.nearbyPharmacies.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                }
                homeState.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                        Text(homeState.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(top=8.dp, bottom = 8.dp))
                        Button(onClick = { homeViewModel.checkAndRequestLocationPermission() }) {
                            Text("Retry")
                        }
                    }
                }
                !homeState.locationPermissionGranted && homeState.locationPermissionRequested -> {
                     Text(
                         "Location permission is denied. Please enable it in app settings to see nearby pharmacies.", 
                         modifier = Modifier.padding(16.dp), 
                         textAlign = TextAlign.Center
                    )
                }
                homeState.nearbyPharmacies.isNotEmpty() -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(homeState.nearbyPharmacies) { pharmacy ->
                            NearbyPharmacyCard(pharmacy = pharmacy, onPharmacyClick = { pharmacyId ->
                                navController.navigate(Screen.UserPharmacyDetail.createRoute(pharmacyId))
                            })
                        }
                    }
                }
                homeState.locationPermissionGranted && homeState.userLocation != null && homeState.nearbyPharmacies.isEmpty() && !homeState.isLoading -> {
                    Text("No nearby pharmacies found.", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
                homeState.locationPermissionGranted && homeState.userLocation == null && !homeState.isLoading -> {
                     Text("Trying to get your location... Ensure GPS is enabled.", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
                 else -> {
                     if (!homeState.locationPermissionGranted && !homeState.locationPermissionRequested) {
                         // This case is handled by the "Enable Location" button above
                     } else if (!homeState.locationPermissionGranted) {
                         // Already requested, but denied, message above covers it.
                     } else {
                        Text("Finding pharmacies near you...", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                     }
                 }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Most Searched Medicines Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Most Searched Medicines",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(commonMedicines) { medicineInfo ->
                    MedicineCard(
                        medicine = medicineInfo,
                        onMedicineClick = {
                            val query = medicineInfo.name.trim()
                            onNavigateToSearchTabWithQuery(query)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyPharmacyCard(
    pharmacy: Pharmacy,
    onPharmacyClick: (pharmacyId: String) -> Unit
) {
    Card(
        onClick = { onPharmacyClick(pharmacy.id) },
        modifier = Modifier.size(width = 180.dp, height = 160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pharmacy.image ?: R.drawable.logo,
                contentDescription = pharmacy.name,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo),
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pharmacy.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            pharmacy.distance?.let {
                Text(
                    text = "%.1f km".format(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PharmaConnectAndroidSTheme {
        HomeScreen(
            onNavigateToRegister = {},
            navController = rememberNavController(),
            onNavigateToSearchTabWithQuery = { _ -> }
        )
    }
}

// Simple placeholder for screens not yet built
@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name Screen (Placeholder)")
    }
}

// Data for the new carousel
data class MedicineInfo(val name: String, val imageResId: Int? = null) // imageResId is optional for future use

val commonMedicines = listOf(
    MedicineInfo("Paracetamol"),
    MedicineInfo("Panadol"),
    MedicineInfo("Amoxicillin"),
    MedicineInfo("Ibuprofen"),
    MedicineInfo("Aspirin"),
    MedicineInfo("Metformin"),
    MedicineInfo("Omeprazole"),
    MedicineInfo("Salbutamol"),
    MedicineInfo("Cetirizine")
)

@Composable
fun MedicineCard(
    medicine: MedicineInfo,
    onMedicineClick: (medicineName: String) -> Unit
) {
    Card(
        onClick = { onMedicineClick(medicine.name.trim()) }, // Trim to be safe, though names are single words here
        modifier = Modifier
            .width(120.dp) // Slightly smaller than pharmacy cards
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Optionally, add an icon or generic image for medicines later using medicine.imageResId
            // For now, just text.
            // Icon(painterResource(id = R.drawable.ic_medicine_placeholder), contentDescription = null, modifier = Modifier.size(40.dp).padding(bottom=4.dp))
            Text(
                text = medicine.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
} 