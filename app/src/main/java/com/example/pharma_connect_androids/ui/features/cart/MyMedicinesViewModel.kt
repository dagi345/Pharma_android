package com.example.pharma_connect_androids.ui.features.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.models.CartItem
import com.example.pharma_connect_androids.data.repository.CartRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyMedicinesScreenState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val error: String? = null,
    val generalMessage: String? = null // For success/info messages like "Item removed"
)

@HiltViewModel
class MyMedicinesViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyMedicinesScreenState())
    val state: StateFlow<MyMedicinesScreenState> = _state.asStateFlow()

    init {
        loadMyMedicines()
    }

    fun loadMyMedicines() {
        cartRepository.getMyMedicines().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null, generalMessage = null)
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        cartItems = result.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load cart items",
                        cartItems = emptyList() // Clear cart items on error
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun removeMedicine(pharmacyId: String, medicineId: String) {
        viewModelScope.launch {
            cartRepository.removeMedicineFromCart(pharmacyId, medicineId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, generalMessage = null, error = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false, generalMessage = "Medicine removed successfully")
                        loadMyMedicines() // Refresh the cart list
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to remove medicine"
                        )
                    }
                }
            }
        }
    }

    fun removeAllMedicines() {
        viewModelScope.launch {
            cartRepository.removeAllMedicinesFromCart().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, generalMessage = null, error = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false, generalMessage = "All medicines removed")
                        loadMyMedicines() // Refresh the cart list (it should be empty)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to remove all medicines"
                        )
                    }
                }
            }
        }
    }
    fun clearGeneralMessage() {
        _state.value = _state.value.copy(generalMessage = null)
    }
} 