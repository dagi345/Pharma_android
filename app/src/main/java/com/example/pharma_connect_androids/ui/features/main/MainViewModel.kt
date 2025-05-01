package com.example.pharma_connect_androids.ui.features.main

import androidx.lifecycle.ViewModel
import com.example.pharma_connect_androids.data.local.SessionManager
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
    val userRole: UserRole = UserRole.UNKNOWN // Default to unknown
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        val userData = sessionManager.getUserData()
        val role = when (userData?.role?.lowercase()) {
            "owner" -> UserRole.OWNER
            "admin" -> UserRole.ADMIN
            "user" -> UserRole.USER
            "pharmacist" -> UserRole.PHARMACIST // Added pharmacist case
            else -> UserRole.UNKNOWN
        }
        _state.value = _state.value.copy(userRole = role)
    }
} 