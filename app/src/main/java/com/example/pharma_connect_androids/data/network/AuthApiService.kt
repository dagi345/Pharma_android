package com.example.pharma_connect_androids.data.network

import com.example.pharma_connect_androids.data.models.LoginRequest // Correct path
import com.example.pharma_connect_androids.data.models.LoginResponse // Correct path
import com.example.pharma_connect_androids.data.models.RegisterRequest // Correct path
import com.example.pharma_connect_androids.data.models.RegisterResponse // Correct path
import com.example.pharma_connect_androids.data.models.PharmacistRegisterRequest // Add import
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/users/signIn") // Corrected endpoint
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("api/v1/users/signUp") // Corrected endpoint
    suspend fun registerUser(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    // Endpoint for Pharmacist Registration (uses the same backend route but different request body)
    @POST("api/v1/users/signUp")
    suspend fun registerPharmacist(
        @Body registerRequest: PharmacistRegisterRequest // Use PharmacistRegisterRequest model
    ): Response<RegisterResponse>

    // Add other auth-related endpoints here (e.g., forgot password, refresh token)
} 