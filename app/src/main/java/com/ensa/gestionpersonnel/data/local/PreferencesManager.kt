package com.ensa.gestionpersonnel.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ensa.gestionpersonnel.domain.model.ResponsableRH
import com.ensa.gestionpersonnel.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

class PreferencesManager(private val context: Context) {
    private val gson = Gson()
    private val tokenKey = stringPreferencesKey(Constants.KEY_TOKEN)
    private val profileKey = stringPreferencesKey("rh_profile")

    // Token methods
    fun tokenFlow(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[tokenKey]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenFlow().first() != null
    }

    // Profile methods
    suspend fun saveRhProfile(profile: ResponsableRH) {
        context.dataStore.edit { preferences ->
            preferences[profileKey] = gson.toJson(profile)
        }
    }

    suspend fun getRhProfile(): ResponsableRH? {
        val json = context.dataStore.data
            .map { preferences ->
                preferences[profileKey]
            }.first()

        return try {
            json?.let { gson.fromJson(it, ResponsableRH::class.java) }
        } catch (e: Exception) {
            null
        }
    }
}