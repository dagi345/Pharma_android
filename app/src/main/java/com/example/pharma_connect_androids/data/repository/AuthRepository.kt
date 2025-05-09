package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.models.LoginRequest
import com.example.pharma_connect_androids.data.models.LoginResponse
import com.example.pharma_connect_androids.data.models.RegisterRequest
import com.example.pharma_connect_androids.data.models.RegisterResponse
import com.example.pharma_connect_androids.data.models.PharmacistRegisterRequest
import com.example.pharma_connect_androids.data.network.AuthApiService
import com.example.pharma_connect_androids.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.domain.model.UserData
// Kotlinx Serialization imports for error parsing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.SerializationException

// Consider making this an interface and creating an Impl class for better testability
// ... existing code ... 

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) {
    private val TAG = "AuthRepository"

    suspend fun loginUser(loginRequest: LoginRequest): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Attempting login for: ${loginRequest.email}")
            val response = authApiService.loginUser(loginRequest)
            Log.d(TAG, "Login response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                val tokenHeader = response.headers().get("Authorization")
                val token = tokenHeader?.removePrefix("Bearer ")?.trim()

                if (token != null && responseBody.success) {
                    Log.d(TAG, "Login successful for: ${responseBody.data.userId}, Role: ${responseBody.data.role}")
                    sessionManager.saveAuthToken(token)
                    val userData = UserData(
                        userId = responseBody.data.userId,
                        role = responseBody.data.role,
                        pharmacyId = responseBody.data.pharmacyId,
                        firstName = null,
                        lastName = null
                    )
                    sessionManager.saveUserData(userData)

                    emit(Resource.Success(responseBody))
                } else {
                    val errorMsg = responseBody.message ?: "Login failed: Invalid response data (token or success flag missing)"
                    Log.e(TAG, "Login failed: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }

            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Login failed: Code=${response.code()}, Message=${response.message()}, ErrorBody=$errorBody")
                val parsedErrorMsg = try {
                    errorBody?.let {
                        val jsonError = kotlinx.serialization.json.Json.parseToJsonElement(it).jsonObject
                        jsonError["message"]?.jsonPrimitive?.content
                    }
                } catch (e: Exception) { null }
                val errorMsg = parsedErrorMsg ?: errorBody ?: "Login failed (Code: ${response.code()})"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Login HTTP error: ${e.code()} - ${e.message()}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "Login network error", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            if (e is kotlinx.serialization.SerializationException) {
                Log.e(TAG, "Login serialization error", e)
                emit(Resource.Error("Failed to understand server response. Please check for app updates."))
            } else {
                Log.e(TAG, "Login general error", e)
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
        }
    }

    suspend fun registerUser(registerRequest: RegisterRequest): Flow<Resource<RegisterResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = authApiService.registerUser(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                val parsedErrorMsg = try {
                    errorBody?.let {
                        val jsonError = kotlinx.serialization.json.Json.parseToJsonElement(it).jsonObject
                        jsonError["message"]?.jsonPrimitive?.content
                    }
                } catch (e: Exception) { null }
                val errorMsg = parsedErrorMsg ?: errorBody ?: "Registration failed (Code: ${response.code()})"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            if (e is kotlinx.serialization.SerializationException) {
                Log.e(TAG, "Registration serialization error", e)
                emit(Resource.Error("Failed to understand server response during registration."))
            } else {
                Log.e(TAG, "Registration general error", e)
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
        }
    }

    suspend fun registerPharmacist(registerRequest: PharmacistRegisterRequest): Flow<Resource<RegisterResponse>> = flow {
        emit(Resource.Loading())
        Log.d(TAG, "Attempting pharmacist registration for: ${registerRequest.email}")
        try {
            val response = authApiService.registerPharmacist(registerRequest)
            Log.d(TAG, "Pharmacist registration response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Pharmacist registration successful for: ${registerRequest.email}")
                emit(Resource.Success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Pharmacist registration failed: Code=${response.code()}, Message=${response.message()}, ErrorBody=$errorBody")
                val parsedErrorMsg = try {
                    errorBody?.let {
                        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        val jsonElement = json.parseToJsonElement(it)
                        if (jsonElement is kotlinx.serialization.json.JsonObject && jsonElement.containsKey("message")) {
                            jsonElement["message"]?.jsonPrimitive?.content
                        } else {
                            null
                        }
                    }
                } catch (e: Exception) { 
                    Log.w(TAG, "Failed to parse error body as JSON: $errorBody", e)
                    null
                }
                val errorMsg = parsedErrorMsg ?: errorBody ?: "Pharmacist registration failed (Code: ${response.code()})"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Pharmacist registration HTTP error: ${e.code()} - ${e.message()}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "Pharmacist registration network error", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            if (e is kotlinx.serialization.SerializationException) {
                Log.e(TAG, "Pharmacist registration serialization error", e)
                emit(Resource.Error("Failed to understand server response during pharmacist registration."))
            } else {
                Log.e(TAG, "Pharmacist registration general error", e)
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred during pharmacist registration"))
            }
        }
    }
} 