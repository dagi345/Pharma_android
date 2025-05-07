package com.example.pharma_connect_androids.ui.features.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class AdminPharmacyDetailViewModel @Inject constructor(
    private val pharmacyRepository: PharmacyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pharmacyId: String? = savedStateHandle.get<String>("pharmacyId")

    private val _pharmacyState = MutableStateFlow<Resource<Pharmacy>?>(null)
    val pharmacyState: StateFlow<Resource<Pharmacy>?> = _pharmacyState.asStateFlow()

    // Removed fetch from init, will be called from Screen's LaunchedEffect
    // init {
    //     pharmacyId?.let {
    //         fetchPharmacyDetails(it)
    //     }
    // }

    fun fetchPharmacyDetails(id: String) {
        viewModelScope.launch {
            _pharmacyState.value = Resource.Loading()
            _pharmacyState.value = pharmacyRepository.getPharmacyById(id)
        }
    }
} 