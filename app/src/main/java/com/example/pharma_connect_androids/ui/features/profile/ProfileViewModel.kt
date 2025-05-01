package com.example.pharma_connect_androids.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileScreenState(
    val userEmail: String? = null, // Store user email
    val userRole: String? = null, // Added user role
    val pharmacyId: String? = null, // Added pharmacyId
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false // To trigger navigation after sign out
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileScreenState())
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // Simulate fetching user data from SessionManager
            // In reality, SessionManager might return null if skipped login
            val userData = sessionManager.getUserData()
            _state.value = _state.value.copy(
                // Provide placeholder if no real data
                userEmail = userData?.userId ?: "user@example.com (Placeholder)", 
                userRole = userData?.role, // Get role from SessionManager
                pharmacyId = userData?.pharmacyId, // Load pharmacyId
                isLoading = false
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            sessionManager.clearSession() // Clear stored token/user data
            _state.value = _state.value.copy(isSignedOut = true)
        }
    }

    // Reset sign out state after navigation handled
    fun resetSignOutState() {
         _state.value = _state.value.copy(isSignedOut = false)
    }
} 