package com.example.pharma_connect_androids.data.api

import com.example.pharma_connect_androids.data.models.AddToCartRequest
import com.example.pharma_connect_androids.data.models.ApiResponse
import com.example.pharma_connect_androids.data.models.CartItem
import com.example.pharma_connect_androids.data.models.LoginSuccessData // Assuming this holds user data
import com.example.pharma_connect_androids.data.models.RemoveFromCartRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface UserApiService {

    @POST("api/v1/users/addtocart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<ApiResponse<LoginSuccessData>> // Backend sends user object

    @GET("api/v1/users/my-medicines")
    suspend fun getMyMedicines(): Response<ApiResponse<List<CartItem>>>

    // For DELETE with a body, we need to use @HTTP with hasBody = true
    @HTTP(method = "DELETE", path = "api/v1/users/my-medicines", hasBody = true)
    suspend fun removeMedicineFromCart(@Body request: RemoveFromCartRequest): Response<Unit> // Backend sends 204

    @DELETE("api/v1/users/my-medicines/deleteAll")
    suspend fun removeAllMedicinesFromCart(): Response<Unit> // Backend sends 204
} 