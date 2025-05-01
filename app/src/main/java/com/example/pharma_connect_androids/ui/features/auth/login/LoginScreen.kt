package com.example.pharma_connect_androids.ui.features.auth.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.lifecycle.SavedStateHandle

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit, // Callback for successful login navigation
    onNavigateToRegister: () -> Unit, // Callback to navigate to registration
    onSkipLogin: () -> Unit // Added callback for skipping login
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Get email argument from navigation (only if needed directly in Composable)
    // It's better practice to let ViewModel handle this via SavedStateHandle
    // For simplicity here, we'll use LaunchedEffect to pass it to VM once.
    val arguments = remember { viewModel.getArguments() } // Assuming SavedStateHandle provides this
    val emailFromRegistration = arguments?.get<String?>("email")

    LaunchedEffect(emailFromRegistration) {
        if (!emailFromRegistration.isNullOrBlank() && state.email.isBlank()) {
            viewModel.onEmailChange(emailFromRegistration) // Pre-fill email if passed and not already set
        }
    }

    // Show toast for errors
    LaunchedEffect(key1 = state.loginError) {
        state.loginError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Navigate on successful login
    LaunchedEffect(key1 = state.loginSuccess) {
        if (state.loginSuccess) {
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            viewModel.resetLoginSuccess() // Reset state after handling
            onLoginSuccess() // Trigger navigation
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)

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
                isError = state.loginError != null
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.loginUser()
                    }
                ),
                singleLine = true,
                isError = state.loginError != null
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.loginUser()
                 },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading // Disable button while loading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Login")
                }
            }

            // Row for Register and Skip buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // Pushes buttons apart
            ) {
                TextButton(onClick = onNavigateToRegister) {
                    Text("Register")
                }
                TextButton(onClick = onSkipLogin) { // Added Skip button
                    Text("Skip Login")
                }
            }
        }

        // Optional: Full screen loading indicator
        // if (state.isLoading) {
        //     CircularProgressIndicator()
        // }
    }
} 