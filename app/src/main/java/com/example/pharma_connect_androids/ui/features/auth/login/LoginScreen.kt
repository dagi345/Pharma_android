package com.example.pharma_connect_androids.ui.features.auth.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pharma_connect_androids.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onSkipLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    // Get email argument from navigation (only if needed directly in Composable)
    // It's better practice to let ViewModel handle this via SavedStateHandle
    // For simplicity here, we'll use LaunchedEffect to pass it to VM once.
    val arguments = remember { viewModel.getArguments() }
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

    // Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Limit width slightly
                .verticalScroll(rememberScrollState()), // Allow scrolling if content overflows
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo Placeholder
            // Replace with your actual logo resource
            // Image(painter = painterResource(id = R.drawable.app_logo_placeholder), contentDescription = "App Logo", modifier = Modifier.size(100.dp)) 
            Spacer(modifier = Modifier.height(32.dp))

            Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Login to your account", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = state.loginError != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password Icon") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                trailingIcon = {
                     val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                     IconButton(onClick = { passwordVisible = !passwordVisible }) {
                         Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                     }
                 },
                singleLine = true,
                isError = state.loginError != null
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.loginUser()
                 },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !state.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("LOGIN", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Register / Skip links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateToRegister) {
                    Text("Create Account")
                }
                 TextButton(onClick = onSkipLogin) {
                    Text("Skip for now")
                }
            }
        }
    }
} 