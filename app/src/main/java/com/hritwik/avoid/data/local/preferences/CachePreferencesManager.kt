package com.hritwik.avoid.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hritwik.avoid.utils.constants.PreferenceConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cache_preferences"
)

@Singleton
class CachePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.cacheDataStore

    companion object {
        private val IMAGE_CACHE_SIZE = longPreferencesKey(PreferenceConstants.KEY_IMAGE_CACHE_SIZE)
        private val VIDEO_CACHE_SIZE = longPreferencesKey(PreferenceConstants.KEY_VIDEO_CACHE_SIZE)
        private val CACHE_WIFI_ONLY = booleanPreferencesKey(PreferenceConstants.KEY_CACHE_WIFI_ONLY)
        private val MAX_STALE_DAYS = intPreferencesKey(PreferenceConstants.KEY_MAX_STALE_DAYS)
        private val PREFETCH_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_PREFETCH_ENABLED)

        private val DATA_USAGE_RX = longPreferencesKey(PreferenceConstants.KEY_DATA_USAGE_RX)
        private val DATA_USAGE_TX = longPreferencesKey(PreferenceConstants.KEY_DATA_USAGE_TX)
        private val DAILY_DATA_CAP = longPreferencesKey(PreferenceConstants.KEY_DAILY_DATA_CAP)
        private val MONTHLY_DATA_CAP = longPreferencesKey(PreferenceConstants.KEY_MONTHLY_DATA_CAP)
        private val DAILY_DATA_USAGE = longPreferencesKey(PreferenceConstants.KEY_DAILY_DATA_USAGE)
        private val MONTHLY_DATA_USAGE = longPreferencesKey(PreferenceConstants.KEY_MONTHLY_DATA_USAGE)
        private val LAST_DAILY_RESET = stringPreferencesKey(PreferenceConstants.KEY_LAST_DAILY_RESET)
        private val LAST_MONTHLY_RESET = stringPreferencesKey(PreferenceConstants.KEY_LAST_MONTHLY_RESET)

        private val SYNC_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_SYNC_ENABLED)
        private val HEARTBEAT_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_HEARTBEAT_ENABLED)
        private val CLEANUP_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_CLEANUP_ENABLED)
    }

    fun getImageCacheSize(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[IMAGE_CACHE_SIZE] ?: PreferenceConstants.DEFAULT_IMAGE_CACHE_SIZE.toLong()
    }

    fun getVideoCacheSize(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[VIDEO_CACHE_SIZE] ?: PreferenceConstants.DEFAULT_VIDEO_CACHE_SIZE.toLong()
    }

    fun getCacheWifiOnly(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CACHE_WIFI_ONLY] ?: PreferenceConstants.DEFAULT_CACHE_WIFI_ONLY
    }

    fun getMaxStaleDays(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[MAX_STALE_DAYS] ?: PreferenceConstants.DEFAULT_MAX_STALE_DAYS
    }

    fun getPrefetchEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PREFETCH_ENABLED] ?: PreferenceConstants.DEFAULT_PREFETCH_ENABLED
    }

    fun getSyncEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SYNC_ENABLED] ?: PreferenceConstants.DEFAULT_SYNC_ENABLED
    }

    fun getHeartbeatEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HEARTBEAT_ENABLED] ?: PreferenceConstants.DEFAULT_HEARTBEAT_ENABLED
    }

    fun getCleanupEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CLEANUP_ENABLED] ?: PreferenceConstants.DEFAULT_CLEANUP_ENABLED
    }

    fun getTotalRxBytes(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[DATA_USAGE_RX] ?: 0L
    }

    fun getTotalTxBytes(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[DATA_USAGE_TX] ?: 0L
    }

    fun getDailyDataUsage(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[DAILY_DATA_USAGE] ?: 0L
    }

    fun getMonthlyDataUsage(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[MONTHLY_DATA_USAGE] ?: 0L
    }

    fun getDailyDataCap(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[DAILY_DATA_CAP] ?: 0L
    }

    fun getMonthlyDataCap(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[MONTHLY_DATA_CAP] ?: 0L
    }

    suspend fun savePrefetchEnabled(enabled: Boolean) {
        dataStore.edit { it[PREFETCH_ENABLED] = enabled }
    }

    suspend fun saveSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[SYNC_ENABLED] = enabled }
    }

    suspend fun saveHeartbeatEnabled(enabled: Boolean) {
        dataStore.edit { it[HEARTBEAT_ENABLED] = enabled }
    }

    suspend fun saveCleanupEnabled(enabled: Boolean) {
        dataStore.edit { it[CLEANUP_ENABLED] = enabled }
    }

    suspend fun saveImageCacheSize(sizeMb: Long) {
        dataStore.edit { preferences ->
            preferences[IMAGE_CACHE_SIZE] = sizeMb
        }
    }

    suspend fun saveVideoCacheSize(sizeMb: Long) {
        dataStore.edit { preferences ->
            preferences[VIDEO_CACHE_SIZE] = sizeMb
        }
    }

    suspend fun saveCacheWifiOnly(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CACHE_WIFI_ONLY] = enabled
        }
    }

    suspend fun saveMaxStaleDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[MAX_STALE_DAYS] = days
        }
    }

    suspend fun saveDailyDataCap(capBytes: Long) {
        dataStore.edit { preferences ->
            preferences[DAILY_DATA_CAP] = capBytes
        }
    }

    suspend fun saveMonthlyDataCap(capBytes: Long) {
        dataStore.edit { preferences ->
            preferences[MONTHLY_DATA_CAP] = capBytes
        }
    }

    suspend fun updateDataUsage(rxBytes: Long, txBytes: Long) {
        dataStore.edit { preferences ->
            val today = LocalDate.now().toString()
            val currentMonth = YearMonth.now().toString()

            val lastDay = preferences[LAST_DAILY_RESET]
            val lastMonth = preferences[LAST_MONTHLY_RESET]

            val dailyUsage = if (lastDay == today) {
                (preferences[DAILY_DATA_USAGE] ?: 0L) + rxBytes + txBytes
            } else {
                preferences[LAST_DAILY_RESET] = today
                rxBytes + txBytes
            }
            val monthlyUsage = if (lastMonth == currentMonth) {
                (preferences[MONTHLY_DATA_USAGE] ?: 0L) + rxBytes + txBytes
            } else {
                preferences[LAST_MONTHLY_RESET] = currentMonth
                rxBytes + txBytes
            }

            preferences[DAILY_DATA_USAGE] = dailyUsage
            preferences[MONTHLY_DATA_USAGE] = monthlyUsage

            val currentRx = preferences[DATA_USAGE_RX] ?: 0L
            val currentTx = preferences[DATA_USAGE_TX] ?: 0L
            preferences[DATA_USAGE_RX] = currentRx + rxBytes
            preferences[DATA_USAGE_TX] = currentTx + txBytes
        }
    }
}