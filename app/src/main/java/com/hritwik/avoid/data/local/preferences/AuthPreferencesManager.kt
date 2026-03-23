package com.hritwik.avoid.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hritwik.avoid.utils.constants.PreferenceConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences"
)

@Singleton
class AuthPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val authDataStore = context.authDataStore

    companion object {
        private val USERNAME = stringPreferencesKey(PreferenceConstants.KEY_USERNAME)
        private val ACCESS_TOKEN = stringPreferencesKey(PreferenceConstants.KEY_ACCESS_TOKEN)
        private val USER_ID = stringPreferencesKey(PreferenceConstants.KEY_USER_ID)
        private val SERVER_ID = stringPreferencesKey(PreferenceConstants.KEY_SERVER_ID)
        private val LAST_LOGIN_TIME = stringPreferencesKey(PreferenceConstants.KEY_LAST_LOGIN_TIME)
        private val SESSION_VALID = stringPreferencesKey(PreferenceConstants.KEY_SESSION_VALID)
        private val REMEMBER_ACCOUNT = booleanPreferencesKey(PreferenceConstants.KEY_REMEMBER_ACCOUNT)
    }

    fun getRememberAccount(): Flow<Boolean> = authDataStore.data.map { preferences ->
        preferences[REMEMBER_ACCOUNT] ?: PreferenceConstants.DEFAULT_REMEMBER_ACCOUNT
    }

    suspend fun setRememberAccount(remember: Boolean) {
        authDataStore.edit { preferences ->
            preferences[REMEMBER_ACCOUNT] = remember
        }
    }

    fun getUsername(): Flow<String?> = authDataStore.data.map { preferences ->
        preferences[USERNAME]
    }

    fun getAccessToken(): Flow<String?> = authDataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    fun getUserId(): Flow<String?> = authDataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    fun getServerId(): Flow<String?> = authDataStore.data.map { preferences ->
        preferences[SERVER_ID]
    }

    fun getLastLoginTime(): Flow<Long?> = authDataStore.data.map { preferences ->
        preferences[LAST_LOGIN_TIME]?.toLongOrNull()
    }

    fun isSessionValid(): Flow<Boolean> = authDataStore.data.map { preferences ->
        preferences[SESSION_VALID] == "true"
    }

    suspend fun invalidateSession() {
        authDataStore.edit { preferences ->
            preferences[SESSION_VALID] = "false"
        }
    }

    suspend fun saveAuthData(
        username: String,
        accessToken: String,
        userId: String,
        serverId: String
    ) {
        authDataStore.edit { preferences ->
            preferences[USERNAME] = username
            preferences[ACCESS_TOKEN] = accessToken
            preferences[USER_ID] = userId
            preferences[SERVER_ID] = serverId
            preferences[LAST_LOGIN_TIME] = System.currentTimeMillis().toString()
            preferences[SESSION_VALID] = "true"
        }
    }

    suspend fun clearAuthData() {
        authDataStore.edit { preferences ->
            preferences.remove(USERNAME)
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(SERVER_ID)
            preferences.remove(LAST_LOGIN_TIME)
            preferences.remove(SESSION_VALID)
        }
    }

    suspend fun clearAll() {
        authDataStore.edit { it.clear() }
    }
}