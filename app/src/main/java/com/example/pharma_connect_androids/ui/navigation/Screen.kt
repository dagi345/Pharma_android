package com.example.pharma_connect_androids.ui.navigation

/**
 * Sealed class representing the different screens/destinations in the application.
 * Routes are defined as strings.
 */
sealed class Screen(val route: String) {
    // Authentication Flow
    object Login : Screen("login_screen?email={email}") {
        // Helper function to create the route with the argument
        fun createRoute(email: String?) = "login_screen?email=${email ?: ""}"
    }
    object Register : Screen("register_screen")

    // Main Application Flow (Add more as needed)
    object Home : Screen("home_screen") // Example main screen after login
    object Search : Screen("search_screen") // Added for Bottom Nav
    object Profile : Screen("profile_screen") // Added for Bottom Nav
    object JoinPharmacy : Screen("join_pharmacy_screen") // Added Join Pharmacy route
    object MapPicker : Screen("map_picker_screen") // Added Map Picker route
    object AdminApplications : Screen("admin_applications_screen") // Added Admin route
    // Route for viewing single application detail
    object ApplicationDetail : Screen("application_detail/{applicationId}") {
        fun createRoute(applicationId: String) = "application_detail/$applicationId"
    }
    // Route for pharmacy owner to manage pharmacists
    object PharmacistList : Screen("pharmacist_list/{pharmacyId}") {
        fun createRoute(pharmacyId: String) = "pharmacist_list/$pharmacyId"
    }
    // Add other screens like Details, etc.
    object Dashboard : Screen("dashboard_screen") // Added for Owner Bottom Nav
    object Inventory : Screen("inventory_screen") // Added for Owner Bottom Nav

    // Define nested graph routes if needed
    object AuthNavGraph : Screen("auth_graph")
    object MainNavGraph : Screen("main_graph")

    // New Screen for Pharmacist Registration
    // Uses an optional argument for pharmacyId (e.g., from a deep link)
    object PharmacistRegister : Screen("pharmacist_register?pharmacyId={pharmacyId}") {
        // Function to create the route path, optionally replacing {pharmacyId}
        // If pharmacyId is null or empty, the parameter is omitted from the route
        fun createRoute(pharmacyId: String?): String {
            return if (!pharmacyId.isNullOrBlank()) {
                route.replace("{pharmacyId}", pharmacyId)
            } else {
                "pharmacist_register" // Route without the optional parameter
            }
        }
        // Argument name constant
        const val ARG_PHARMACY_ID = "pharmacyId"
    }

    // Helper function to append arguments (example, might need adjustment)
    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}