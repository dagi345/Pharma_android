package com.example.pharma_connect_androids.di

import com.example.pharma_connect_androids.data.network.ApplicationApiService
import com.example.pharma_connect_androids.data.network.AuthApiService
import com.example.pharma_connect_androids.data.network.PharmacyApiService
import com.example.pharma_connect_androids.data.network.SearchApiService
import com.example.pharma_connect_androids.data.repository.ApplicationRepository
import com.example.pharma_connect_androids.data.repository.AuthRepository
import com.example.pharma_connect_androids.data.repository.SearchRepository
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.repository.MedicineRepository
import com.example.pharma_connect_androids.data.repository.MedicineRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Binds MedicineRepository interface to its implementation
    @Binds
    @Singleton
    abstract fun bindMedicineRepository(impl: MedicineRepositoryImpl): MedicineRepository

    // Companion object for static @Provides methods
    companion object {
        @Provides
        @Singleton
        fun provideAuthRepository(
            authApiService: AuthApiService,
            sessionManager: SessionManager
        ): AuthRepository {
            return AuthRepository(authApiService, sessionManager)
        }

        @Provides
        @Singleton
        fun provideSearchRepository(searchApiService: SearchApiService): SearchRepository {
            return SearchRepository(searchApiService)
        }

        @Provides
        @Singleton
        fun provideApplicationRepository(applicationApiService: ApplicationApiService): ApplicationRepository {
            return ApplicationRepository(applicationApiService)
        }

        // PharmacyRepository uses @Inject constructor and does not need an explicit provider here.
        // Add providers for other repositories only if they DON'T use @Inject constructor
        // and are not suitable for @Binds
    }
}