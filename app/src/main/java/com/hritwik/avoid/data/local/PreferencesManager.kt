package com.hritwik.avoid.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hritwik.avoid.data.local.model.PlaybackPreferences
import com.hritwik.avoid.data.local.preferences.AuthPreferencesManager
import com.hritwik.avoid.data.local.preferences.CachePreferencesManager
import com.hritwik.avoid.data.local.preferences.PlaybackPreferencesManager
import com.hritwik.avoid.data.local.preferences.ServerPreferencesManager
import com.hritwik.avoid.data.local.preferences.UIPreferencesManager
import com.hritwik.avoid.domain.model.auth.ServerConnectionMethod
import com.hritwik.avoid.domain.model.playback.DecoderMode
import com.hritwik.avoid.domain.model.playback.DisplayMode
import com.hritwik.avoid.domain.model.playback.HdrFormatPreference
import com.hritwik.avoid.domain.model.playback.PlayerType
import com.hritwik.avoid.utils.constants.PreferenceConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PreferenceConstants.DATASTORE_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authPreferencesManager: AuthPreferencesManager,
    private val serverPreferencesManager: ServerPreferencesManager,
    private val playbackPreferencesManager: PlaybackPreferencesManager,
    private val uiPreferencesManager: UIPreferencesManager,
    private val cachePreferencesManager: CachePreferencesManager
) {
    private val dataStore = context.dataStore

    companion object {
        private val DEVICE_ID = stringPreferencesKey(PreferenceConstants.KEY_DEVICE_ID)
    }

    fun isLoggedIn(): Flow<Boolean> = combine(
        authPreferencesManager.getAccessToken(),
        authPreferencesManager.getUserId(),
        serverPreferencesManager.getServerUrl()
    ) { token, userId, serverUrl ->
        val hasToken = token != null
        val hasUserId = userId != null
        val hasServerUrl = serverUrl != null
        val isSessionValid = authPreferencesManager.isSessionValid().first()
        hasToken && hasUserId && hasServerUrl && isSessionValid
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { it.clear() }
    }

    fun getDeviceId(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[DEVICE_ID]
    }

    suspend fun saveDeviceId(deviceId: String) {
        dataStore.edit { preferences ->
            preferences[DEVICE_ID] = deviceId
        }
    }

    fun initializeThemePreferences() {
    }

    private fun resolvePreferHdrOverDolbyVisionDefault(): Boolean {
        return PreferenceConstants.DEFAULT_PREFER_HDR_OVER_DV
    }

    fun getPreferHdrOverDolbyVisionDefault(): Boolean = resolvePreferHdrOverDolbyVisionDefault()

    fun getAutoPlay(): Flow<Boolean> = playbackPreferencesManager.getAutoPlay()

    fun getContinueWatching(): Flow<Boolean> = playbackPreferencesManager.getContinueWatching()

    fun getStreamingQuality(): Flow<String> = playbackPreferencesManager.getStreamingQuality()

    fun getDownloadQuality(): Flow<String> = playbackPreferencesManager.getDownloadQuality()

    fun getDownloadWifiOnly(): Flow<Boolean> = playbackPreferencesManager.getDownloadWifiOnly()

    fun getAutoDeleteDownloads(): Flow<Boolean> = playbackPreferencesManager.getAutoDeleteDownloads()

    fun getDownloadLocation(): Flow<String> = playbackPreferencesManager.getDownloadLocation()

    fun getPlaybackSpeed(): Flow<Float> = playbackPreferencesManager.getPlaybackSpeed()

    fun getSubtitleEnabled(): Flow<Boolean> = playbackPreferencesManager.getSubtitleEnabled()

    fun getSubtitleSize(): Flow<String> = playbackPreferencesManager.getSubtitleSize()

    fun getAudioTrackLanguage(): Flow<String?> = playbackPreferencesManager.getAudioTrackLanguage()

    fun getSubtitleLanguage(): Flow<String?> = playbackPreferencesManager.getSubtitleLanguage()

    fun getPlayerProgressColor(): Flow<String> = playbackPreferencesManager.getPlayerProgressColor()

    fun getPlayerProgressSeekColor(): Flow<String> = playbackPreferencesManager.getPlayerProgressSeekColor()

    fun getPlayThemeSongs(): Flow<Boolean> = playbackPreferencesManager.getPlayThemeSongs()

    fun getThemeSongVolume(): Flow<Int> = playbackPreferencesManager.getThemeSongVolume()

    fun getThemeSongFallbackUrl(): Flow<String> = playbackPreferencesManager.getThemeSongFallbackUrl()

    fun getAutoSkipSegments(): Flow<Boolean> = playbackPreferencesManager.getAutoSkipSegments()

    fun getDisplayMode(): Flow<DisplayMode> = playbackPreferencesManager.getDisplayMode()

    fun getDecoderMode(): Flow<DecoderMode> = playbackPreferencesManager.getDecoderMode()

    fun getPlayerType(): Flow<PlayerType> = playbackPreferencesManager.getPlayerType()

    fun getExternalPlayerEnabled(): Flow<Boolean> = playbackPreferencesManager.getExternalPlayerEnabled()

    fun getAudioPassthroughEnabled(): Flow<Boolean> = playbackPreferencesManager.getAudioPassthroughEnabled()

    fun getDirectPlayEnabled(): Flow<Boolean> = playbackPreferencesManager.getDirectPlayEnabled()

    fun getFrameRateSwitchEnabled(): Flow<Boolean> = playbackPreferencesManager.getFrameRateSwitchEnabled()

    fun getPreferHdrOverDolbyVision(): Flow<Boolean> = playbackPreferencesManager.getPreferHdrOverDolbyVision()

    fun getHdrFormatPreference(): Flow<HdrFormatPreference> = playbackPreferencesManager.getHdrFormatPreference()

    fun getControlsTimeoutSeconds(): Flow<Int> = playbackPreferencesManager.getControlsTimeoutSeconds()

    fun getLibraryFilterWatched(): Flow<Boolean> = playbackPreferencesManager.getLibraryFilterWatched()

    fun getImageCacheSize(): Flow<Long> = cachePreferencesManager.getImageCacheSize()

    fun getVideoCacheSize(): Flow<Long> = cachePreferencesManager.getVideoCacheSize()

    fun getCacheWifiOnly(): Flow<Boolean> = cachePreferencesManager.getCacheWifiOnly()

    fun getMaxStaleDays(): Flow<Int> = cachePreferencesManager.getMaxStaleDays()

    fun getPrefetchEnabled(): Flow<Boolean> = cachePreferencesManager.getPrefetchEnabled()

    fun getSyncEnabled(): Flow<Boolean> = cachePreferencesManager.getSyncEnabled()

    fun getHeartbeatEnabled(): Flow<Boolean> = cachePreferencesManager.getHeartbeatEnabled()

    fun getCleanupEnabled(): Flow<Boolean> = cachePreferencesManager.getCleanupEnabled()

    fun getTotalRxBytes(): Flow<Long> = cachePreferencesManager.getTotalRxBytes()

    fun getTotalTxBytes(): Flow<Long> = cachePreferencesManager.getTotalTxBytes()

    fun getDailyDataUsage(): Flow<Long> = cachePreferencesManager.getDailyDataUsage()

    fun getMonthlyDataUsage(): Flow<Long> = cachePreferencesManager.getMonthlyDataUsage()

    fun getDailyDataCap(): Flow<Long> = cachePreferencesManager.getDailyDataCap()

    fun getMonthlyDataCap(): Flow<Long> = cachePreferencesManager.getMonthlyDataCap()

    fun getThemeMode(): Flow<String> = uiPreferencesManager.getThemeMode()

    fun getDynamicColors(): Flow<Boolean> = uiPreferencesManager.getDynamicColors()

    fun getShowFeaturedHeader(): Flow<Boolean> = uiPreferencesManager.getShowFeaturedHeader()

    fun getAmbientBackgroundEnabled(): Flow<Boolean> = uiPreferencesManager.getAmbientBackgroundEnabled()

    fun getFontScale(): Flow<Float> = uiPreferencesManager.getFontScale()

    fun getPreferredLanguage(): Flow<String> = uiPreferencesManager.getPreferredLanguage()

    fun getTmdbEnabled(): Flow<Boolean> = uiPreferencesManager.getTmdbEnabled()

    fun getTmdbApiKey(): Flow<String> = uiPreferencesManager.getTmdbApiKey()

    fun getGestureControlsEnabled(): Flow<Boolean> = uiPreferencesManager.getGestureControlsEnabled()

    fun getHighContrastEnabled(): Flow<Boolean> = uiPreferencesManager.getHighContrastEnabled()

    fun isFirstRunCompleted(): Flow<Boolean> = uiPreferencesManager.isFirstRunCompleted()

    fun getRecentSearches(): Flow<List<String>> = uiPreferencesManager.getRecentSearches()

    fun getRememberAccount(): Flow<Boolean> = authPreferencesManager.getRememberAccount()

    fun getUsername(): Flow<String?> = authPreferencesManager.getUsername()

    fun getAccessToken(): Flow<String?> = authPreferencesManager.getAccessToken()

    fun getUserId(): Flow<String?> = authPreferencesManager.getUserId()

    fun getServerId(): Flow<String?> = authPreferencesManager.getServerId()

    fun getLastLoginTime(): Flow<Long?> = authPreferencesManager.getLastLoginTime()

    fun isSessionValid(): Flow<Boolean> = authPreferencesManager.isSessionValid()

    fun getServerUrl(): Flow<String?> = serverPreferencesManager.getServerUrl()

    fun getServerName(): Flow<String?> = serverPreferencesManager.getServerName()

    fun getServerVersion(): Flow<String?> = serverPreferencesManager.getServerVersion()

    fun getServerConnected(): Flow<Boolean> = serverPreferencesManager.getServerConnected()

    fun getServerLegacyPlayback(): Flow<Boolean> = serverPreferencesManager.getServerLegacyPlayback()

    fun getServerConnections(): Flow<List<ServerConnectionMethod>> = serverPreferencesManager.getServerConnections()

    fun isMtlsEnabled(): Flow<Boolean> = serverPreferencesManager.isMtlsEnabled()

    fun getMtlsCertificateName(): Flow<String?> = serverPreferencesManager.getMtlsCertificateName()

    fun getMtlsCertificatePassword(): Flow<String?> = serverPreferencesManager.getMtlsCertificatePassword()

    fun getOfflineMode(): Flow<Boolean> = serverPreferencesManager.getOfflineMode()

    suspend fun saveThemeMode(mode: String) = uiPreferencesManager.saveThemeMode(mode)

    suspend fun saveFontScale(scale: Float) = uiPreferencesManager.saveFontScale(scale)

    suspend fun savePreferredLanguage(language: String) = uiPreferencesManager.savePreferredLanguage(language)

    suspend fun saveTmdbEnabled(enabled: Boolean) = uiPreferencesManager.saveTmdbEnabled(enabled)

    suspend fun saveTmdbApiKey(apiKey: String) = uiPreferencesManager.saveTmdbApiKey(apiKey)

    suspend fun saveGestureControlsEnabled(enabled: Boolean) = uiPreferencesManager.saveGestureControlsEnabled(enabled)

    suspend fun saveHighContrastEnabled(enabled: Boolean) = uiPreferencesManager.saveHighContrastEnabled(enabled)

    suspend fun setFirstRunCompleted(completed: Boolean) = uiPreferencesManager.setFirstRunCompleted(completed)

    suspend fun saveRecentSearches(searches: List<String>) = uiPreferencesManager.saveRecentSearches(searches)

    suspend fun clearRecentSearches() = uiPreferencesManager.clearRecentSearches()

    suspend fun saveShowFeaturedHeader(enabled: Boolean) = uiPreferencesManager.saveShowFeaturedHeader(enabled)

    suspend fun saveAmbientBackgroundEnabled(enabled: Boolean) = uiPreferencesManager.saveAmbientBackgroundEnabled(enabled)

    suspend fun saveServerConfig(url: String, name: String) = serverPreferencesManager.saveServerConfig(url, name)

    suspend fun saveServerUrlOnly(url: String) = serverPreferencesManager.saveServerUrlOnly(url)

    suspend fun clearServerUrl() = serverPreferencesManager.clearServerUrl()

    suspend fun saveServerDetails(serverVersion: String, serverConnected: Boolean, isLegacyPlaybackApi: Boolean) =
        serverPreferencesManager.saveServerDetails(serverVersion, serverConnected, isLegacyPlaybackApi)

    suspend fun saveServerConnections(methods: List<ServerConnectionMethod>) =
        serverPreferencesManager.saveServerConnections(methods)

    suspend fun setMtlsEnabled(enabled: Boolean) = serverPreferencesManager.setMtlsEnabled(enabled)

    suspend fun saveMtlsCertificate(bytes: ByteArray, displayName: String) =
        serverPreferencesManager.saveMtlsCertificate(bytes, displayName)

    suspend fun clearMtlsCertificate() = serverPreferencesManager.clearMtlsCertificate()

    suspend fun getMtlsCertificateBytes(): ByteArray? = serverPreferencesManager.getMtlsCertificateBytes()

    suspend fun setMtlsCertificatePassword(password: String) = serverPreferencesManager.setMtlsCertificatePassword(password)

    suspend fun clearServerConfiguration() = serverPreferencesManager.clearServerConfiguration()

    suspend fun setOfflineMode(enabled: Boolean) = serverPreferencesManager.setOfflineMode(enabled)

    suspend fun saveAuthData(username: String, accessToken: String, userId: String, serverId: String) =
        authPreferencesManager.saveAuthData(username, accessToken, userId, serverId)

    suspend fun setRememberAccount(remember: Boolean) = authPreferencesManager.setRememberAccount(remember)

    suspend fun invalidateSession() = authPreferencesManager.invalidateSession()

    suspend fun clearAuthData() = authPreferencesManager.clearAuthData()

    suspend fun saveAutoPlay(enabled: Boolean) = playbackPreferencesManager.saveAutoPlay(enabled)

    suspend fun saveContinueWatching(enabled: Boolean) = playbackPreferencesManager.saveContinueWatching(enabled)

    suspend fun saveStreamingQuality(quality: String) = playbackPreferencesManager.saveStreamingQuality(quality)

    suspend fun saveDownloadQuality(quality: String) = playbackPreferencesManager.saveDownloadQuality(quality)

    suspend fun saveDownloadWifiOnly(enabled: Boolean) = playbackPreferencesManager.saveDownloadWifiOnly(enabled)

    suspend fun saveAutoDeleteDownloads(enabled: Boolean) = playbackPreferencesManager.saveAutoDeleteDownloads(enabled)

    suspend fun saveDownloadLimit(limit: Long) = playbackPreferencesManager.saveDownloadLimit(limit)

    suspend fun saveDownloadLocation(location: String) = playbackPreferencesManager.saveDownloadLocation(location)

    suspend fun savePlaybackSpeed(speed: Float) = playbackPreferencesManager.savePlaybackSpeed(speed)

    suspend fun saveSubtitleEnabled(enabled: Boolean) = playbackPreferencesManager.saveSubtitleEnabled(enabled)

    suspend fun saveSubtitleSize(size: String) = playbackPreferencesManager.saveSubtitleSize(size)

    suspend fun savePlayerProgressColor(colorKey: String) = playbackPreferencesManager.savePlayerProgressColor(colorKey)

    suspend fun savePlayerProgressSeekColor(colorKey: String) = playbackPreferencesManager.savePlayerProgressSeekColor(colorKey)

    suspend fun saveAudioTrackLanguage(language: String?) = playbackPreferencesManager.saveAudioTrackLanguage(language)

    suspend fun saveSubtitleLanguage(language: String?) = playbackPreferencesManager.saveSubtitleLanguage(language)

    suspend fun savePlayThemeSongs(enabled: Boolean) = playbackPreferencesManager.savePlayThemeSongs(enabled)

    suspend fun saveThemeSongVolume(volume: Int) = playbackPreferencesManager.saveThemeSongVolume(volume)

    suspend fun saveThemeSongFallbackUrl(url: String) = playbackPreferencesManager.saveThemeSongFallbackUrl(url)

    suspend fun saveAutoSkipSegments(enabled: Boolean) = playbackPreferencesManager.saveAutoSkipSegments(enabled)

    suspend fun saveDisplayMode(mode: DisplayMode) = playbackPreferencesManager.saveDisplayMode(mode)

    suspend fun saveDecoderMode(mode: DecoderMode) = playbackPreferencesManager.saveDecoderMode(mode)

    suspend fun savePlayerType(playerType: PlayerType) = playbackPreferencesManager.savePlayerType(playerType)

    suspend fun saveExternalPlayerEnabled(enabled: Boolean) = playbackPreferencesManager.saveExternalPlayerEnabled(enabled)

    suspend fun saveAudioPassthroughEnabled(enabled: Boolean) = playbackPreferencesManager.saveAudioPassthroughEnabled(enabled)

    suspend fun saveDirectPlayEnabled(enabled: Boolean) = playbackPreferencesManager.saveDirectPlayEnabled(enabled)

    suspend fun saveFrameRateSwitchEnabled(enabled: Boolean) = playbackPreferencesManager.saveFrameRateSwitchEnabled(enabled)

    suspend fun savePreferHdrOverDolbyVision(enabled: Boolean) = playbackPreferencesManager.savePreferHdrOverDolbyVision(enabled)

    suspend fun saveHdrFormatPreference(preference: HdrFormatPreference) = playbackPreferencesManager.saveHdrFormatPreference(preference)

    suspend fun saveControlsTimeoutSeconds(seconds: Int) = playbackPreferencesManager.saveControlsTimeoutSeconds(seconds)

    suspend fun saveLibraryFilterWatched(hideWatched: Boolean) = playbackPreferencesManager.saveLibraryFilterWatched(hideWatched)

    suspend fun saveImageCacheSize(sizeMb: Long) = cachePreferencesManager.saveImageCacheSize(sizeMb)

    suspend fun saveVideoCacheSize(sizeMb: Long) = cachePreferencesManager.saveVideoCacheSize(sizeMb)

    suspend fun saveCacheWifiOnly(enabled: Boolean) = cachePreferencesManager.saveCacheWifiOnly(enabled)

    suspend fun saveMaxStaleDays(days: Int) = cachePreferencesManager.saveMaxStaleDays(days)

    suspend fun savePrefetchEnabled(enabled: Boolean) = cachePreferencesManager.savePrefetchEnabled(enabled)

    suspend fun saveSyncEnabled(enabled: Boolean) = cachePreferencesManager.saveSyncEnabled(enabled)

    suspend fun saveHeartbeatEnabled(enabled: Boolean) = cachePreferencesManager.saveHeartbeatEnabled(enabled)

    suspend fun saveCleanupEnabled(enabled: Boolean) = cachePreferencesManager.saveCleanupEnabled(enabled)

    suspend fun saveDailyDataCap(capBytes: Long) = cachePreferencesManager.saveDailyDataCap(capBytes)

    suspend fun saveMonthlyDataCap(capBytes: Long) = cachePreferencesManager.saveMonthlyDataCap(capBytes)

    suspend fun updateDataUsage(rxBytes: Long, txBytes: Long) = cachePreferencesManager.updateDataUsage(rxBytes, txBytes)

    suspend fun savePlaybackPosition(itemId: String, positionTicks: Long) =
        playbackPreferencesManager.savePlaybackPosition(itemId, positionTicks)

    fun getPlaybackPosition(itemId: String): Flow<Long?> = playbackPreferencesManager.getPlaybackPosition(itemId)

    suspend fun clearPlaybackPosition(itemId: String) = playbackPreferencesManager.clearPlaybackPosition(itemId)

    suspend fun clearAllPlaybackPositions() = playbackPreferencesManager.clearAllPlaybackPositions()

    suspend fun savePlaybackPreferences(mediaId: String, prefs: PlaybackPreferences) =
        playbackPreferencesManager.savePlaybackPreferences(mediaId, prefs)

    fun getPlaybackPreferences(mediaId: String): Flow<PlaybackPreferences?> =
        playbackPreferencesManager.getPlaybackPreferences(mediaId)
}