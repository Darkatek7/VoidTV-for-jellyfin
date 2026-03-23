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
import com.hritwik.avoid.data.local.model.PlaybackPreferences
import com.hritwik.avoid.domain.model.playback.DecoderMode
import com.hritwik.avoid.domain.model.playback.DisplayMode
import com.hritwik.avoid.domain.model.playback.HdrFormatPreference
import com.hritwik.avoid.domain.model.playback.PlayerType
import com.hritwik.avoid.utils.constants.PreferenceConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playbackDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PreferenceConstants.DATASTORE_NAME
)

@Singleton
class PlaybackPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.playbackDataStore

    companion object {
        private val AUTO_PLAY = booleanPreferencesKey(PreferenceConstants.KEY_AUTO_PLAY)
        private val CONTINUE_WATCHING = booleanPreferencesKey(PreferenceConstants.KEY_CONTINUE_WATCHING)
        private val STREAMING_QUALITY = stringPreferencesKey(PreferenceConstants.KEY_STREAMING_QUALITY)
        private val DOWNLOAD_QUALITY = stringPreferencesKey(PreferenceConstants.KEY_DOWNLOAD_QUALITY)
        private val DOWNLOAD_WIFI_ONLY = booleanPreferencesKey(PreferenceConstants.KEY_DOWNLOAD_WIFI_ONLY)
        private val AUTO_DELETE_DOWNLOADS = booleanPreferencesKey(PreferenceConstants.KEY_AUTO_DELETE_DOWNLOADS)
        private val DOWNLOAD_LIMIT = longPreferencesKey(PreferenceConstants.KEY_DOWNLOAD_LIMIT)
        private val DOWNLOAD_LOCATION = stringPreferencesKey(PreferenceConstants.KEY_DOWNLOAD_LOCATION)
        private val PLAYBACK_SPEED = stringPreferencesKey(PreferenceConstants.KEY_PLAYBACK_SPEED)
        private val SUBTITLE_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_SUBTITLE_ENABLED)
        private val SUBTITLE_SIZE = stringPreferencesKey(PreferenceConstants.KEY_SUBTITLE_SIZE)
        private val AUDIO_TRACK_LANGUAGE = stringPreferencesKey(PreferenceConstants.KEY_AUDIO_TRACK_LANGUAGE)
        private val SUBTITLE_LANGUAGE = stringPreferencesKey(PreferenceConstants.KEY_SUBTITLE_LANGUAGE)
        private val PLAYER_PROGRESS_COLOR = stringPreferencesKey(PreferenceConstants.KEY_PLAYER_PROGRESS_COLOR)
        private val PLAYER_PROGRESS_SEEK_COLOR = stringPreferencesKey(PreferenceConstants.KEY_PLAYER_PROGRESS_SEEK_COLOR)
        private val PLAY_THEME_SONGS = booleanPreferencesKey(PreferenceConstants.KEY_PLAY_THEME_SONGS)
        private val THEME_SONG_VOLUME = intPreferencesKey(PreferenceConstants.KEY_THEME_SONG_VOLUME)
        private val THEME_SONG_FALLBACK_URL = stringPreferencesKey(PreferenceConstants.KEY_THEME_SONG_FALLBACK_URL)
        private val DISPLAY_MODE = stringPreferencesKey(PreferenceConstants.KEY_DISPLAY_MODE)
        private val DECODER_MODE = stringPreferencesKey(PreferenceConstants.KEY_DECODER_MODE)
        private val AUTO_SKIP_SEGMENTS = booleanPreferencesKey(PreferenceConstants.KEY_AUTO_SKIP_SEGMENTS)
        private val PLAYER_TYPE = stringPreferencesKey(PreferenceConstants.KEY_PLAYER_TYPE)
        private val EXTERNAL_PLAYER_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_EXTERNAL_PLAYER_ENABLED)
        private val AUDIO_PASSTHROUGH_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_AUDIO_PASSTHROUGH_ENABLED)
        private val DIRECT_PLAY_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_DIRECT_PLAY_ENABLED)
        private val FRAME_RATE_SWITCH_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_FRAME_RATE_SWITCH_ENABLED)
        private val PREFER_HDR_OVER_DV = booleanPreferencesKey(PreferenceConstants.KEY_PREFER_HDR_OVER_DV)
        private val HDR_FORMAT_PREFERENCE = stringPreferencesKey(PreferenceConstants.KEY_HDR_FORMAT_PREFERENCE)
        private val CONTROLS_TIMEOUT_SECONDS = intPreferencesKey(PreferenceConstants.KEY_CONTROLS_TIMEOUT_SECONDS)
        private val LIBRARY_FILTER_WATCHED = booleanPreferencesKey(PreferenceConstants.KEY_LIBRARY_FILTER_WATCHED)

        private const val PLAYBACK_POSITION_PREFIX = "playback_position_"
        private const val PLAYBACK_PREFERENCES_PREFIX = "playback_preferences"
    }

    fun getAutoPlay(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_PLAY] ?: PreferenceConstants.DEFAULT_AUTO_PLAY
    }

    fun getContinueWatching(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CONTINUE_WATCHING] ?: PreferenceConstants.DEFAULT_CONTINUE_WATCHING
    }

    fun getStreamingQuality(): Flow<String> = dataStore.data.map { preferences ->
        preferences[STREAMING_QUALITY] ?: PreferenceConstants.DEFAULT_STREAMING_QUALITY
    }

    fun getDownloadQuality(): Flow<String> = dataStore.data.map { preferences ->
        preferences[DOWNLOAD_QUALITY] ?: PreferenceConstants.DEFAULT_DOWNLOAD_QUALITY
    }

    fun getDownloadWifiOnly(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DOWNLOAD_WIFI_ONLY] ?: PreferenceConstants.DEFAULT_DOWNLOAD_WIFI_ONLY
    }

    fun getAutoDeleteDownloads(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_DELETE_DOWNLOADS] ?: PreferenceConstants.DEFAULT_AUTO_DELETE_DOWNLOADS
    }

    fun getDownloadLocation(): Flow<String> = dataStore.data.map { preferences ->
        preferences[DOWNLOAD_LOCATION] ?: PreferenceConstants.DEFAULT_DOWNLOAD_LOCATION
    }

    fun getPlaybackSpeed(): Flow<Float> = dataStore.data.map { preferences ->
        preferences[PLAYBACK_SPEED]?.toFloatOrNull() ?: 1.0f
    }

    fun getSubtitleEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SUBTITLE_ENABLED] ?: false
    }

    fun getSubtitleSize(): Flow<String> = dataStore.data.map { preferences ->
        preferences[SUBTITLE_SIZE] ?: PreferenceConstants.DEFAULT_SUBTITLE_SIZE
    }

    fun getAudioTrackLanguage(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUDIO_TRACK_LANGUAGE]
    }

    fun getSubtitleLanguage(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[SUBTITLE_LANGUAGE]
    }

    fun getPlayerProgressColor(): Flow<String> = dataStore.data.map { preferences ->
        preferences[PLAYER_PROGRESS_COLOR] ?: PreferenceConstants.DEFAULT_PLAYER_PROGRESS_COLOR
    }

    fun getPlayerProgressSeekColor(): Flow<String> = dataStore.data.map { preferences ->
        preferences[PLAYER_PROGRESS_SEEK_COLOR] ?: PreferenceConstants.DEFAULT_PLAYER_PROGRESS_SEEK_COLOR
    }

    fun getPlayThemeSongs(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PLAY_THEME_SONGS] ?: PreferenceConstants.DEFAULT_PLAY_THEME_SONGS
    }

    fun getThemeSongVolume(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[THEME_SONG_VOLUME] ?: PreferenceConstants.DEFAULT_THEME_SONG_VOLUME
    }

    fun getThemeSongFallbackUrl(): Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_SONG_FALLBACK_URL]?.trim() ?: PreferenceConstants.DEFAULT_THEME_SONG_FALLBACK_URL
    }

    fun getAutoSkipSegments(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_SKIP_SEGMENTS] ?: PreferenceConstants.DEFAULT_AUTO_SKIP_SEGMENTS
    }

    fun getDisplayMode(): Flow<DisplayMode> = dataStore.data.map { preferences ->
        DisplayMode.fromValue(preferences[DISPLAY_MODE] ?: PreferenceConstants.DEFAULT_DISPLAY_MODE)
    }

    fun getDecoderMode(): Flow<DecoderMode> = dataStore.data.map { preferences ->
        DecoderMode.fromValue(preferences[DECODER_MODE] ?: PreferenceConstants.DEFAULT_DECODER_MODE)
    }

    fun getPlayerType(): Flow<PlayerType> = dataStore.data.map { preferences ->
        PlayerType.fromValue(preferences[PLAYER_TYPE] ?: PreferenceConstants.DEFAULT_PLAYER_TYPE)
    }

    fun getExternalPlayerEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[EXTERNAL_PLAYER_ENABLED] ?: PreferenceConstants.DEFAULT_EXTERNAL_PLAYER_ENABLED
    }

    fun getAudioPassthroughEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUDIO_PASSTHROUGH_ENABLED] ?: PreferenceConstants.DEFAULT_AUDIO_PASSTHROUGH_ENABLED
    }

    fun getDirectPlayEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DIRECT_PLAY_ENABLED] ?: PreferenceConstants.DEFAULT_DIRECT_PLAY_ENABLED
    }

    fun getFrameRateSwitchEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FRAME_RATE_SWITCH_ENABLED] ?: PreferenceConstants.DEFAULT_FRAME_RATE_SWITCH_ENABLED
    }

    fun getPreferHdrOverDolbyVision(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PREFER_HDR_OVER_DV] ?: PreferenceConstants.DEFAULT_PREFER_HDR_OVER_DV
    }

    fun getHdrFormatPreference(): Flow<HdrFormatPreference> = dataStore.data.map { preferences ->
        val raw = preferences[HDR_FORMAT_PREFERENCE] ?: PreferenceConstants.DEFAULT_HDR_FORMAT_PREFERENCE
        HdrFormatPreference.fromValue(raw)
    }

    fun getControlsTimeoutSeconds(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[CONTROLS_TIMEOUT_SECONDS] ?: PreferenceConstants.DEFAULT_CONTROLS_TIMEOUT_SECONDS
    }

    fun getLibraryFilterWatched(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LIBRARY_FILTER_WATCHED] ?: PreferenceConstants.DEFAULT_LIBRARY_FILTER_WATCHED
    }

    suspend fun saveAutoPlay(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PLAY] = enabled
        }
    }

    suspend fun saveContinueWatching(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CONTINUE_WATCHING] = enabled
        }
    }

    suspend fun saveStreamingQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[STREAMING_QUALITY] = quality
        }
    }

    suspend fun saveDownloadQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[DOWNLOAD_QUALITY] = quality
        }
    }

    suspend fun saveDownloadWifiOnly(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DOWNLOAD_WIFI_ONLY] = enabled
        }
    }

    suspend fun saveAutoDeleteDownloads(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_DELETE_DOWNLOADS] = enabled
        }
    }

    suspend fun saveDownloadLimit(limit: Long) {
        dataStore.edit { preferences ->
            preferences[DOWNLOAD_LIMIT] = limit
        }
    }

    suspend fun saveDownloadLocation(location: String) {
        dataStore.edit { preferences ->
            preferences[DOWNLOAD_LOCATION] = location
        }
    }

    suspend fun savePlaybackSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[PLAYBACK_SPEED] = speed.toString()
        }
    }

    suspend fun saveSubtitleEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SUBTITLE_ENABLED] = enabled
        }
    }

    suspend fun saveSubtitleSize(size: String) {
        dataStore.edit { preferences ->
            preferences[SUBTITLE_SIZE] = size
        }
    }

    suspend fun savePlayerProgressColor(colorKey: String) {
        dataStore.edit { preferences ->
            preferences[PLAYER_PROGRESS_COLOR] = colorKey
        }
    }

    suspend fun savePlayerProgressSeekColor(colorKey: String) {
        dataStore.edit { preferences ->
            preferences[PLAYER_PROGRESS_SEEK_COLOR] = colorKey
        }
    }

    suspend fun saveAudioTrackLanguage(language: String?) {
        dataStore.edit { preferences ->
            if (language != null) {
                preferences[AUDIO_TRACK_LANGUAGE] = language
            } else {
                preferences.remove(AUDIO_TRACK_LANGUAGE)
            }
        }
    }

    suspend fun saveSubtitleLanguage(language: String?) {
        dataStore.edit { preferences ->
            if (language != null) {
                preferences[SUBTITLE_LANGUAGE] = language
            } else {
                preferences.remove(SUBTITLE_LANGUAGE)
            }
        }
    }

    suspend fun savePlayThemeSongs(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PLAY_THEME_SONGS] = enabled
        }
    }

    suspend fun saveThemeSongVolume(volume: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_SONG_VOLUME] = volume
        }
    }

    suspend fun saveThemeSongFallbackUrl(url: String) {
        val normalized = url.trim().trimEnd('/')
        dataStore.edit { preferences ->
            if (normalized.isEmpty()) {
                preferences.remove(THEME_SONG_FALLBACK_URL)
            } else {
                preferences[THEME_SONG_FALLBACK_URL] = normalized
            }
        }
    }

    suspend fun saveAutoSkipSegments(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SKIP_SEGMENTS] = enabled
        }
    }

    suspend fun saveDisplayMode(mode: DisplayMode) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_MODE] = mode.value
        }
    }

    suspend fun saveDecoderMode(mode: DecoderMode) {
        dataStore.edit { preferences ->
            preferences[DECODER_MODE] = mode.value
        }
    }

    suspend fun savePlayerType(playerType: PlayerType) {
        dataStore.edit { preferences ->
            preferences[PLAYER_TYPE] = playerType.value
        }
    }

    suspend fun saveExternalPlayerEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[EXTERNAL_PLAYER_ENABLED] = enabled
        }
    }

    suspend fun saveAudioPassthroughEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUDIO_PASSTHROUGH_ENABLED] = enabled
        }
    }

    suspend fun saveDirectPlayEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DIRECT_PLAY_ENABLED] = enabled
        }
    }

    suspend fun saveFrameRateSwitchEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FRAME_RATE_SWITCH_ENABLED] = enabled
        }
    }

    suspend fun savePreferHdrOverDolbyVision(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PREFER_HDR_OVER_DV] = enabled
        }
    }

    suspend fun saveHdrFormatPreference(preference: HdrFormatPreference) {
        dataStore.edit { preferences ->
            preferences[HDR_FORMAT_PREFERENCE] = preference.value
        }
    }

    suspend fun saveControlsTimeoutSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[CONTROLS_TIMEOUT_SECONDS] = seconds
        }
    }

    suspend fun saveLibraryFilterWatched(hideWatched: Boolean) {
        dataStore.edit { preferences ->
            preferences[LIBRARY_FILTER_WATCHED] = hideWatched
        }
    }

    suspend fun savePlaybackPosition(itemId: String, positionTicks: Long) {
        val key = longPreferencesKey("${PLAYBACK_POSITION_PREFIX}$itemId")
        dataStore.edit { preferences ->
            preferences[key] = positionTicks
        }
    }

    fun getPlaybackPosition(itemId: String): Flow<Long?> {
        val key = longPreferencesKey("${PLAYBACK_POSITION_PREFIX}$itemId")
        return dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    suspend fun clearPlaybackPosition(itemId: String) {
        val key = longPreferencesKey("${PLAYBACK_POSITION_PREFIX}$itemId")
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    suspend fun clearAllPlaybackPositions() {
        dataStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys.filter {
                it.name.startsWith(PLAYBACK_POSITION_PREFIX)
            }
            keysToRemove.forEach { preferences.remove(it) }
        }
    }

    suspend fun savePlaybackPreferences(mediaId: String, prefs: PlaybackPreferences) {
        val key = stringPreferencesKey("${PLAYBACK_PREFERENCES_PREFIX}$mediaId")
        dataStore.edit { preferences ->
            preferences[key] = Json.encodeToString(prefs)
        }
    }

    fun getPlaybackPreferences(mediaId: String): Flow<PlaybackPreferences?> {
        val key = stringPreferencesKey("${PLAYBACK_PREFERENCES_PREFIX}$mediaId")
        return dataStore.data.map { preferences ->
            preferences[key]?.let { json ->
                runCatching { Json.decodeFromString<PlaybackPreferences>(json) }.getOrNull()
            }
        }
    }
}