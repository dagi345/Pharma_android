package com.example.pharma_connect_androids.ui.features.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.models.Pharmacy
import com.example.pharma_connect_androids.data.repository.PharmacyRepository
import com.example.pharma_connect_androids.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPharmacyViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _pharmacyState = MutableStateFlow<Resource<Pharmacy>?>(null)
    val pharmacyState: StateFlow<Resource<Pharmacy>?> = _pharmacyState.asStateFlow()

    val pharmacyId: String? = sessionManager.getUserData()?.pharmacyId

    init {
        fetchMyPharmacyDetails()
    }

    fun fetchMyPharmacyDetails() {
        pharmacyId?.let {
            viewModelScope.launch {
                _pharmacyState.value = Resource.Loading()
                _pharmacyState.value = pharmacyRepository.getPharmacyById(it)
            }
        } ?: run {
             _pharmacyState.value = Resource.Error("Pharmacy ID not found for owner.")
        }
    }
} 