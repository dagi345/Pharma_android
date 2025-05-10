package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.api.UserApiService
import com.example.pharma_connect_androids.data.models.AddToCartRequest
import com.example.pharma_connect_androids.data.models.CartItem
import com.example.pharma_connect_androids.data.models.LoginSuccessData
import com.example.pharma_connect_androids.data.models.RemoveFromCartRequest
import com.example.pharma_connect_androids.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val userApiService: UserApiService
) {

    fun addToCart(inventoryId: String): Flow<Resource<LoginSuccessData>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApiService.addToCart(AddToCartRequest(inventoryId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.message ?: "Add to cart failed"))
                }
            } else {
                emit(Resource.Error("Add to cart failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    fun getMyMedicines(): Flow<Resource<List<CartItem>>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApiService.getMyMedicines()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Failed to fetch cart items"))
                }
            } else {
                emit(Resource.Error("Failed to fetch cart items: ${response.code()} - ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    fun removeMedicineFromCart(pharmacyId: String, medicineId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApiService.removeMedicineFromCart(RemoveFromCartRequest(pharmacyId, medicineId))
            if (response.isSuccessful) { // For 204 No Content, isSuccessful should be true
                emit(Resource.Success(Unit))
            } else {
                // Even for 204, if there was an issue constructing the request or server error before 204, this might be hit.
                emit(Resource.Error("Remove from cart failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    fun removeAllMedicinesFromCart(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApiService.removeAllMedicinesFromCart()
            if (response.isSuccessful) { // For 204 No Content
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Remove all from cart failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
} 