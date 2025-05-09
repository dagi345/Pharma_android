package com.example.pharma_connect_androids.ui.features.main

import androidx.lifecycle.ViewModel
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.domain.model.UserData
import com.example.pharma_connect_androids.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Enum for User Roles (optional but recommended)
// You might already have this or want to define it elsewhere
// package com.example.pharma_connect_androids.domain.model
// enum class UserRole { USER, OWNER, ADMIN, UNKNOWN }

data class MainScreenState(
    val userRole: UserRole = UserRole.UNKNOWN,
    val currentUser: UserData? = null // Added current user
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        loadUserDetails()
    }

    private fun loadUserDetails() {
        val userData = sessionManager.getUserData()
        val role = when (userData?.role?.lowercase()) {
            "owner" -> UserRole.OWNER
            "admin" -> UserRole.ADMIN
            "user" -> UserRole.USER
            "pharmacist" -> UserRole.PHARMACIST
            else -> UserRole.UNKNOWN
        }
        _state.value = MainScreenState(userRole = role, currentUser = userData)
    }

    fun signOut() {
        sessionManager.clearSession()
        // Update state to reflect logged out status
        _state.value = MainScreenState(userRole = UserRole.UNKNOWN, currentUser = null)
        // Optionally, could also emit an event to trigger navigation if preferred
    }

    // Call this if there's a chance session data changes elsewhere and UI needs to refresh
    fun refreshUserDetails() {
        loadUserDetails()
    }
} 