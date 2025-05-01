package com.example.pharma_connect_androids.util

import com.example.pharma_connect_androids.data.models.ApiResponse
import kotlinx.serialization.json.Json // Import Kotlinx Json
import retrofit2.Response
import java.io.IOException // Import IOException for network errors

// Configure Json parser (lenient to handle potential inconsistencies)
private val json = Json { ignoreUnknownKeys = true }

/**
 * Helper function to wrap Retrofit API calls with Resource states.
 * Handles success, API error (based on 'success' flag), network error, and exceptions.
 * Attempts to parse error body for non-2xx responses to get specific messages.
 */
suspend fun <T> handleApiResponse(apiCall: suspend () -> Response<ApiResponse<T>>): Resource<T?> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.success) {
                Resource.Success(body.data)
            } else {
                // API returned 2xx but success flag is false or body is null
                Resource.Error(body?.message ?: "API error with no message", null)
            }
        } else {
            // Non-2xx HTTP status code - Try to parse error body
            var errorMsg = response.message() ?: "Network error (non-2xx)" // Default message
            val errorBody = response.errorBody()?.string()
            
            if (errorBody != null) {
                try {
                    // Try parsing the error body into our standard ApiResponse structure
                    val parsedError = json.decodeFromString<ApiResponse<Any?>>(errorBody)
                    if (!parsedError.message.isNullOrBlank()) {
                        errorMsg = parsedError.message // Use message from error body if available
                    }
                } catch (e: Exception) {
                    // Parsing failed, keep the default error message
                    println("Failed to parse error body: ${e.message}") 
                }
            }
            Resource.Error(errorMsg, null)
        }
    } catch (e: IOException) {
        // Network exceptions
        Resource.Error("Network error: ${e.message ?: "Unknown IO Exception"}", null)
    } catch (e: Exception) {
        // Other exceptions
        Resource.Error(e.message ?: "An unknown error occurred", null)
    }
} 