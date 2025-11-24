package com.example.kmaerm.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenDataStore(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val FULL_NAME_KEY = stringPreferencesKey("user_full_name")
        private val DOANH_NGHIEP_ID_KEY = stringPreferencesKey("doanh_nghiep_id")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[ROLE_KEY] = role
        }
    }

    suspend fun saveAuth(token: String, role: String, userId: String = "", email: String = "", fullName: String = "", doanhNghiepId: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
            preferences[USER_ID_KEY] = userId
            preferences[EMAIL_KEY] = email
            preferences[FULL_NAME_KEY] = fullName
            doanhNghiepId?.let { preferences[DOANH_NGHIEP_ID_KEY] = it }
        }
    }

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val role: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val email: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    val fullName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FULL_NAME_KEY]
    }

    val doanhNghiepId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DOANH_NGHIEP_ID_KEY]
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(FULL_NAME_KEY)
            preferences.remove(DOANH_NGHIEP_ID_KEY)
        }
    }
}
