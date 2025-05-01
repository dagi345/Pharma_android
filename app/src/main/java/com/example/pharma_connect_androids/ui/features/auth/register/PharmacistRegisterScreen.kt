package com.example.pharma_connect_androids.ui.features.auth.register

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PharmacistRegisterScreen(
    viewModel: PharmacistRegisterViewModel = hiltViewModel(),
    onRegistrationSuccess: (email: String) -> Unit, // Pass email back for login pre-fill
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Show toast for errors
    LaunchedEffect(key1 = state.registrationError) {
        state.registrationError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            // Consider if error state needs resetting in VM after shown
        }
    }

    // Navigate on successful registration
    LaunchedEffect(key1 = state.registrationSuccess) {
        if (state.registrationSuccess) {
            Toast.makeText(context, "Pharmacist Registration Successful! Please Login.", Toast.LENGTH_SHORT).show()
            val registeredEmail = state.email // Capture email before resetting state
            viewModel.resetRegistrationSuccess() // Reset state after handling
            onRegistrationSuccess(registeredEmail) // Trigger navigation and pass email
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState), // Make column scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pharmacist Registration", style = MaterialTheme.typography.headlineMedium)

            // First Name
            OutlinedTextField(
                value = state.firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                isError = state.registrationError != null
            )

            // Last Name
            OutlinedTextField(
                value = state.lastName,
                onValueChange = viewModel::onLastNameChange,
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                isError = state.registrationError != null
            )

            // Email
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = state.registrationError != null
            )

            // Password
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = state.registrationError != null
            )

            // Confirm Password
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = state.registrationError != null
            )

            // Pharmacy ID
            OutlinedTextField(
                value = state.pharmacyId,
                onValueChange = viewModel::onPharmacyIdChange,
                label = { Text("Pharmacy ID") }, // This ID likely comes from the owner/invitation
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.registerPharmacist()
                    }
                ),
                singleLine = true,
                isError = state.registrationError != null
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.registerPharmacist()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Register Pharmacist")
                }
            }

            // Link to Login
            TextButton(onClick = onNavigateToLogin) {
                Text("Already registered? Login")
            }
        }
    }
} 