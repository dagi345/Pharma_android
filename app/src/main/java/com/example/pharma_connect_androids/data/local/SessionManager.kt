package com.example.pharma_connect_androids.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.pharma_connect_androids.domain.model.UserData // Correct path
import com.example.pharma_connect_androids.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

// Already provided as Singleton by NetworkModule, no need for @Singleton here
class SessionManager @Inject constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(Constants.KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(Constants.KEY_AUTH_TOKEN, null)
    }

    fun saveUserData(userData: UserData) {
        sharedPreferences.edit().apply {
            putString(Constants.KEY_USER_ID, userData.userId)
            putString(Constants.KEY_USER_ROLE, userData.role)
            putString(Constants.KEY_PHARMACY_ID, userData.pharmacyId) // Store even if null
            putString(Constants.KEY_USER_FIRST_NAME, userData.firstName)
            putString(Constants.KEY_USER_LAST_NAME, userData.lastName)
            apply()
        }
    }

    fun getUserData(): UserData? {
        val userId = sharedPreferences.getString(Constants.KEY_USER_ID, null)
        val role = sharedPreferences.getString(Constants.KEY_USER_ROLE, null)
        val pharmacyId = sharedPreferences.getString(Constants.KEY_PHARMACY_ID, null)
        val firstName = sharedPreferences.getString(Constants.KEY_USER_FIRST_NAME, null)
        val lastName = sharedPreferences.getString(Constants.KEY_USER_LAST_NAME, null)

        return if (userId != null && role != null) {
            UserData(
                userId = userId,
                role = role,
                pharmacyId = pharmacyId,
                firstName = firstName,
                lastName = lastName
            )
        } else {
            null
        }
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
} 