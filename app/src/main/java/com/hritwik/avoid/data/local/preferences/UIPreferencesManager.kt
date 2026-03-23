package com.hritwik.avoid.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hritwik.avoid.utils.constants.PreferenceConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.uiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PreferenceConstants.DATASTORE_NAME
)

@Singleton
class UIPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.uiDataStore

    companion object {
        private val THEME_MODE = stringPreferencesKey(PreferenceConstants.KEY_THEME_MODE)
        private val DYNAMIC_COLORS = booleanPreferencesKey(PreferenceConstants.KEY_DYNAMIC_COLORS)
        private val SHOW_FEATURED_HEADER = booleanPreferencesKey(PreferenceConstants.KEY_SHOW_FEATURED_HEADER)
        private val AMBIENT_BACKGROUND = booleanPreferencesKey(PreferenceConstants.KEY_AMBIENT_BACKGROUND)
        private val FONT_SCALE = floatPreferencesKey(PreferenceConstants.KEY_FONT_SCALE)
        private val PREFERRED_LANGUAGE = stringPreferencesKey(PreferenceConstants.KEY_PREFERRED_LANGUAGE)
        private val GESTURE_CONTROLS = booleanPreferencesKey(PreferenceConstants.KEY_GESTURE_CONTROLS)
        private val HIGH_CONTRAST = booleanPreferencesKey(PreferenceConstants.KEY_HIGH_CONTRAST)
        private val FIRST_RUN_COMPLETED = booleanPreferencesKey(PreferenceConstants.KEY_FIRST_RUN_COMPLETED)
        private val RECENT_SEARCHES = stringPreferencesKey(PreferenceConstants.KEY_RECENT_SEARCHES)
        private val TMDB_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_TMDB_ENABLED)
        private val TMDB_API_KEY = stringPreferencesKey(PreferenceConstants.KEY_TMDB_API_KEY)
    }

    fun getThemeMode(): Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: PreferenceConstants.DEFAULT_THEME_MODE
    }

    fun getDynamicColors(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLORS] ?: PreferenceConstants.DEFAULT_DYNAMIC_COLORS
    }

    fun getShowFeaturedHeader(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_FEATURED_HEADER] ?: PreferenceConstants.DEFAULT_SHOW_FEATURED_HEADER
    }

    fun getAmbientBackgroundEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AMBIENT_BACKGROUND] ?: PreferenceConstants.DEFAULT_AMBIENT_BACKGROUND
    }

    fun getFontScale(): Flow<Float> = dataStore.data.map { prefs ->
        prefs[FONT_SCALE] ?: PreferenceConstants.DEFAULT_FONT_SCALE
    }

    fun getPreferredLanguage(): Flow<String> = dataStore.data.map { prefs ->
        prefs[PREFERRED_LANGUAGE] ?: PreferenceConstants.DEFAULT_PREFERRED_LANGUAGE
    }

    fun getTmdbEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[TMDB_ENABLED] ?: PreferenceConstants.DEFAULT_TMDB_ENABLED
    }

    fun getTmdbApiKey(): Flow<String> = dataStore.data.map { prefs ->
        prefs[TMDB_API_KEY] ?: PreferenceConstants.DEFAULT_TMDB_API_KEY
    }

    fun getGestureControlsEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[GESTURE_CONTROLS] ?: PreferenceConstants.DEFAULT_GESTURE_CONTROLS
    }

    fun getHighContrastEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HIGH_CONTRAST] ?: PreferenceConstants.DEFAULT_HIGH_CONTRAST
    }

    fun isFirstRunCompleted(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[FIRST_RUN_COMPLETED] ?: PreferenceConstants.DEFAULT_FIRST_RUN_COMPLETED
    }

    fun getRecentSearches(): Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[RECENT_SEARCHES]?.let { json ->
            runCatching { kotlinx.serialization.json.Json.decodeFromString<List<String>>(json) }.getOrDefault(emptyList())
        } ?: emptyList()
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun saveFontScale(scale: Float) {
        dataStore.edit { it[FONT_SCALE] = scale }
    }

    suspend fun savePreferredLanguage(language: String) {
        dataStore.edit { it[PREFERRED_LANGUAGE] = language }
    }

    suspend fun saveTmdbEnabled(enabled: Boolean) {
        dataStore.edit { it[TMDB_ENABLED] = enabled }
    }

    suspend fun saveTmdbApiKey(apiKey: String) {
        dataStore.edit { it[TMDB_API_KEY] = apiKey }
    }

    suspend fun saveGestureControlsEnabled(enabled: Boolean) {
        dataStore.edit { it[GESTURE_CONTROLS] = enabled }
    }

    suspend fun saveHighContrastEnabled(enabled: Boolean) {
        dataStore.edit { it[HIGH_CONTRAST] = enabled }
    }

    suspend fun setFirstRunCompleted(completed: Boolean) {
        dataStore.edit { it[FIRST_RUN_COMPLETED] = completed }
    }

    suspend fun saveRecentSearches(searches: List<String>) {
        dataStore.edit { preferences ->
            preferences[RECENT_SEARCHES] = kotlinx.serialization.json.Json.encodeToString(searches)
        }
    }

    suspend fun clearRecentSearches() {
        dataStore.edit { preferences ->
            preferences.remove(RECENT_SEARCHES)
        }
    }

    suspend fun saveShowFeaturedHeader(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_FEATURED_HEADER] = enabled
        }
    }

    suspend fun saveAmbientBackgroundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AMBIENT_BACKGROUND] = enabled
        }
    }
}