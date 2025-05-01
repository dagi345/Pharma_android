package com.example.pharma_connect_androids.ui.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.RegisterRequest
import com.example.pharma_connect_androids.data.repository.AuthRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define the state for the Registration Screen (Normal User)
data class RegisterScreenState(
    val firstName: String = "",   // Changed
    val lastName: String = "",    // Changed
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    // Removed role
    // Removed pharmacyId
    val isLoading: Boolean = false,
    val registrationError: String? = null,
    val registrationSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterScreenState())
    val state: StateFlow<RegisterScreenState> = _state.asStateFlow()

    // Update functions for each field
    fun onFirstNameChange(firstName: String) { // Changed
        _state.value = _state.value.copy(firstName = firstName, registrationError = null)
    }
    fun onLastNameChange(lastName: String) { // Changed
        _state.value = _state.value.copy(lastName = lastName, registrationError = null)
    }
    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, registrationError = null)
    }
    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, registrationError = null)
    }
    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword, registrationError = null)
    }
    // Removed onFullNameChange
    // Removed onRoleChange
    // Removed onPharmacyIdChange

    // Function to initiate registration
    fun registerUser() {
        if (_state.value.isLoading) return

        val currentState = _state.value

        // Basic Validations (enhance as needed)
        // Updated validation list
        if (listOf(currentState.firstName, currentState.lastName, currentState.email, currentState.password, currentState.confirmPassword).any { it.isBlank() }) {
            _state.value = currentState.copy(registrationError = "All fields must be filled") // Updated error message
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            _state.value = currentState.copy(registrationError = "Passwords do not match")
            return
        }
        // Add more validation for email format, password strength etc.

        viewModelScope.launch {
            // Updated request creation
            val registerRequest = RegisterRequest(
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
                // role is not sent
                // pharmacyId is not sent
            )

            authRepository.registerUser(registerRequest)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true, registrationError = null)
                        }
                        is Resource.Success -> {
                            _state.value = _state.value.copy(isLoading = false, registrationSuccess = true, registrationError = null)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                registrationError = result.message ?: "An unknown error occurred",
                                registrationSuccess = false
                            )
                        }
                    }
                }.launchIn(viewModelScope)
        }
    }

    fun resetRegistrationSuccess() {
        _state.value = _state.value.copy(registrationSuccess = false)
    }
} 