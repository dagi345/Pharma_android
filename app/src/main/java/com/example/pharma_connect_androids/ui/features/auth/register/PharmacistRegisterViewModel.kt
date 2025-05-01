package com.example.pharma_connect_androids.ui.features.auth.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.PharmacistRegisterRequest
import com.example.pharma_connect_androids.data.repository.AuthRepository
import com.example.pharma_connect_androids.ui.navigation.Screen
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// State for the Pharmacist Registration Screen
data class PharmacistRegisterScreenState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val pharmacyId: String = "", // Added pharmacyId
    val isLoading: Boolean = false,
    val registrationError: String? = null,
    val registrationSuccess: Boolean = false
)

@HiltViewModel
class PharmacistRegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PharmacistRegisterScreenState())
    val state: StateFlow<PharmacistRegisterScreenState> = _state.asStateFlow()

    init {
        // Pre-fill pharmacyId if passed as argument
        savedStateHandle.get<String>(Screen.PharmacistRegister.ARG_PHARMACY_ID)?.let {
            if (it.isNotBlank()) {
                _state.value = _state.value.copy(pharmacyId = it)
            }
        }
    }

    // Update functions for each field
    fun onFirstNameChange(firstName: String) {
        _state.value = _state.value.copy(firstName = firstName, registrationError = null)
    }
    fun onLastNameChange(lastName: String) {
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
    fun onPharmacyIdChange(pharmacyId: String) {
        // Allow user to override pre-filled value if needed
        _state.value = _state.value.copy(pharmacyId = pharmacyId, registrationError = null)
    }

    // Function to initiate pharmacist registration
    fun registerPharmacist() {
        if (_state.value.isLoading) return

        val currentState = _state.value

        // Basic Validations
        if (listOf(currentState.firstName, currentState.lastName, currentState.email,
                    currentState.password, currentState.confirmPassword, currentState.pharmacyId)
                .any { it.isBlank() }) {
            _state.value = currentState.copy(registrationError = "All fields must be filled")
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            _state.value = currentState.copy(registrationError = "Passwords do not match")
            return
        }
        // Add more validation if needed (email format, password strength, pharmacyId format?)

        viewModelScope.launch {
            val registerRequest = PharmacistRegisterRequest(
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                email = currentState.email,
                password = currentState.password,
                // role is set by default in the data class
                pharmacyId = currentState.pharmacyId
            )

            authRepository.registerPharmacist(registerRequest)
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
                                registrationError = result.message ?: "An unknown error occurred during registration",
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