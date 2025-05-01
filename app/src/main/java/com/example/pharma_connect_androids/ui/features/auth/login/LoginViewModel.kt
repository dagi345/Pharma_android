package com.example.pharma_connect_androids.ui.features.auth.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.models.LoginRequest
import com.example.pharma_connect_androids.data.models.LoginResponse
import com.example.pharma_connect_androids.data.repository.AuthRepository
import com.example.pharma_connect_androids.domain.model.UserData
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define the state for the Login Screen
data class LoginScreenState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Mutable state flow for internal updates
    private val _state = MutableStateFlow(LoginScreenState())
    // Publicly exposed immutable state flow for the UI to observe
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()

    // Get initial email from SavedStateHandle if available
    init {
        val initialEmail: String? = savedStateHandle.get<String?>("email")
        if (!initialEmail.isNullOrBlank()) {
            _state.value = _state.value.copy(email = initialEmail)
        }
    }

    // Expose arguments for LaunchedEffect (optional, init block handles pre-fill)
    fun getArguments(): SavedStateHandle {
        return savedStateHandle
    }

    // Function to update email in the state
    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, loginError = null)
    }

    // Function to update password in the state
    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, loginError = null)
    }

    // Function to initiate the login process
    fun loginUser() {
        // Prevent multiple login attempts while one is in progress
        if (_state.value.isLoading) return

        val email = _state.value.email
        val password = _state.value.password

        // Basic validation (can be enhanced)
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(loginError = "Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            val loginRequest = LoginRequest(email = email, password = password)
            authRepository.loginUser(loginRequest)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true, loginError = null)
                        }
                        is Resource.Success -> {
                            // Data (token, user details) is now saved directly by AuthRepository
                            // The ViewModel just needs to know the login was successful to trigger navigation.
                            _state.value = _state.value.copy(isLoading = false, loginSuccess = true, loginError = null)
                            // We don't need to check result.data here anymore unless we need the success message
                            // val response = result.data
                            // Log.d("LoginViewModel", "Login Success Message: ${response?.message}") // Optional logging
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                loginError = result.message ?: "An unknown error occurred",
                                loginSuccess = false
                            )
                        }
                    }
                }.launchIn(viewModelScope) // Collect the flow within the viewModelScope
        }
    }

    // Optional: Function to reset success state after navigation
    fun resetLoginSuccess() {
        _state.value = _state.value.copy(loginSuccess = false)
    }
} 