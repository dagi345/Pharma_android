package com.example.pharma_connect_androids.data.repository

import com.example.pharma_connect_androids.data.models.SearchRequest
import com.example.pharma_connect_androids.data.models.SearchResponse
import com.example.pharma_connect_androids.data.network.SearchApiService
import com.example.pharma_connect_androids.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val searchApiService: SearchApiService
) {

    // In a real scenario, this would make the actual API call.
    // For now, it just defines the structure. Simulation happens in ViewModel.
    suspend fun searchMedicine(searchRequest: SearchRequest): Flow<Resource<SearchResponse>> = flow {
        // Simulate network call structure
        emit(Resource.Loading()) 
        try {
            // --- Actual API call would go here ---
             val response = searchApiService.searchMedicine(searchRequest)
             if (response.isSuccessful && response.body() != null) {
                 emit(Resource.Success(response.body()!!)) 
             } else {
                 emit(Resource.Error(response.errorBody()?.string() ?: "Search failed"))
             }
             // --- End of actual API call section ---

            // For now, we just emit success immediately for placeholder simulation
            // In VM we will add placeholder data
            // emit(Resource.Success(SearchResponse(data = emptyList()))) // Simulate success

        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
} 