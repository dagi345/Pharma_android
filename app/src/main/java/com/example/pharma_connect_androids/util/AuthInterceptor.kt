package com.example.pharma_connect_androids.util

import com.example.pharma_connect_androids.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// Provided by NetworkModule, no need for @Singleton here
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getToken()
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Add Authorization header if token exists
        token?.let {
            // Ensure the header is not added for specific paths like login/register if necessary
            // (Example: Check originalRequest.url.encodedPath)
            // For simplicity now, adding it to all requests
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
} 