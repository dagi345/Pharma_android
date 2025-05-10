package com.example.pharma_connect_androids.di

import android.content.Context
import com.example.pharma_connect_androids.data.local.SessionManager
import com.example.pharma_connect_androids.data.network.AuthApiService
import com.example.pharma_connect_androids.data.network.SearchApiService
import com.example.pharma_connect_androids.data.network.PharmacyApiService
import com.example.pharma_connect_androids.data.network.ApplicationApiService
import com.example.pharma_connect_androids.data.network.MedicineApiService
import com.example.pharma_connect_androids.data.api.UserApiService
import com.example.pharma_connect_androids.util.AuthInterceptor
import com.example.pharma_connect_androids.util.Constants
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Provides the Application Context safely
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    // Provides SessionManager (depends on Context)
    @Provides
    @Singleton
    fun provideSessionManager(context: Context): SessionManager {
        return SessionManager(context)
    }

    // Provides AuthInterceptor (depends on SessionManager)
    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    // Provides OkHttpClient (depends on AuthInterceptor)
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log network traffic
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Provides Kotlinx JSON parser instance
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json { ignoreUnknownKeys = true }
    }

    // Provides Retrofit instance (depends on OkHttpClient and Json)
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // Provides AuthApiService (depends on Retrofit)
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    // Provides SearchApiService (depends on Retrofit)
    @Provides
    @Singleton
    fun provideSearchApiService(retrofit: Retrofit): SearchApiService {
        return retrofit.create(SearchApiService::class.java)
    }

    // Provides ApplicationApiService (depends on Retrofit)
    @Provides
    @Singleton
    fun provideApplicationApiService(retrofit: Retrofit): ApplicationApiService {
        return retrofit.create(ApplicationApiService::class.java)
    }

    // Provides PharmacyApiService (depends on Retrofit)
    @Provides
    @Singleton
    fun providePharmacyApiService(retrofit: Retrofit): PharmacyApiService {
        return retrofit.create(PharmacyApiService::class.java)
    }

    // Provides MedicineApiService (depends on Retrofit)
    @Provides
    @Singleton
    fun provideMedicineApiService(retrofit: Retrofit): MedicineApiService {
        return retrofit.create(MedicineApiService::class.java)
    }

    // Provides UserApiService (depends on Retrofit) - Added for Cart functionality
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    // Add provider for other API services later when needed
}