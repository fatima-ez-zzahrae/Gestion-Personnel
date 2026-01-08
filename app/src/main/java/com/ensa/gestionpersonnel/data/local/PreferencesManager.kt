package com.ensa.gestionpersonnel.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ensa.gestionpersonnel.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

class PreferencesManager(private val context: Context) {

    private val tokenKey = stringPreferencesKey(Constants.KEY_TOKEN)

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
        clearToken()
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenFlow().first() != null
    }
}
