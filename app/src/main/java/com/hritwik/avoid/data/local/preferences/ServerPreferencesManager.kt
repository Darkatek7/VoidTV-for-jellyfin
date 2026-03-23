package com.hritwik.avoid.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.hritwik.avoid.domain.model.auth.ServerConnectionMethod
import com.hritwik.avoid.utils.constants.PreferenceConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.serverDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PreferenceConstants.DATASTORE_NAME
)

@Singleton
class ServerPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.serverDataStore
    private val aead: Aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(context, TINK_KEYSET_NAME, TINK_KEYSET_PREF)
            .withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
            .withMasterKeyUri(TINK_MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    private val mtlsCertificateFile: File
        get() = File(context.filesDir, "mtls_certificate.bin")

    companion object {
        private val SERVER_URL = stringPreferencesKey(PreferenceConstants.KEY_SERVER_URL)
        private val SERVER_NAME = stringPreferencesKey(PreferenceConstants.KEY_SERVER_NAME)
        private val SERVER_VERSION = stringPreferencesKey(PreferenceConstants.KEY_SERVER_VERSION)
        private val SERVER_LEGACY_PLAYBACK = booleanPreferencesKey(PreferenceConstants.KEY_SERVER_LEGACY_PLAYBACK)
        private val SERVER_CONNECTED = stringPreferencesKey(PreferenceConstants.KEY_SERVER_CONNECTED)
        private val SERVER_CONNECTIONS = stringPreferencesKey(PreferenceConstants.KEY_SERVER_CONNECTIONS)
        private val MTLS_ENABLED = booleanPreferencesKey(PreferenceConstants.KEY_MTLS_ENABLED)
        private val MTLS_CERTIFICATE_NAME = stringPreferencesKey(PreferenceConstants.KEY_MTLS_CERTIFICATE_NAME)
        private val MTLS_CERTIFICATE_PASSWORD = stringPreferencesKey(PreferenceConstants.KEY_MTLS_CERTIFICATE_PASSWORD)
        private val OFFLINE_MODE = booleanPreferencesKey(PreferenceConstants.KEY_OFFLINE_MODE)

        private const val TINK_KEYSET_PREF = "void_tink_keyset"
        private const val TINK_KEYSET_NAME = "tink_keyset"
        private const val TINK_MASTER_KEY_URI = "android-keystore://void_tink_master_key"
        private const val AEAD_ASSOCIATED_DATA_MTLS = "mtls_certificate"
    }

    suspend fun saveServerConfig(url: String, name: String) {
        dataStore.edit { preferences ->
            preferences[SERVER_URL] = url
            preferences[SERVER_NAME] = name
        }
    }

    suspend fun saveServerUrlOnly(url: String) {
        dataStore.edit { preferences ->
            preferences[SERVER_URL] = url
        }
    }

    suspend fun clearServerUrl() {
        dataStore.edit { preferences ->
            preferences.remove(SERVER_URL)
        }
    }

    suspend fun saveServerDetails(
        serverVersion: String,
        serverConnected: Boolean,
        isLegacyPlaybackApi: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[SERVER_VERSION] = serverVersion
            preferences[SERVER_CONNECTED] = serverConnected.toString()
            preferences[SERVER_LEGACY_PLAYBACK] = isLegacyPlaybackApi
        }
    }

    fun getServerUrl(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[SERVER_URL]
    }

    fun getServerName(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[SERVER_NAME]
    }

    fun getServerVersion(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[SERVER_VERSION]
    }

    fun getServerConnected(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SERVER_CONNECTED]?.toBoolean() ?: false
    }

    fun getServerLegacyPlayback(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SERVER_LEGACY_PLAYBACK] ?: PreferenceConstants.DEFAULT_SERVER_LEGACY_PLAYBACK
    }

    fun getServerConnections(): Flow<List<ServerConnectionMethod>> = dataStore.data.map { preferences ->
        preferences[SERVER_CONNECTIONS]?.let { json ->
            runCatching { Json.decodeFromString<List<ServerConnectionMethod>>(json) }
                .getOrDefault(emptyList())
                .distinctBy { it.url.lowercase() }
        } ?: emptyList()
    }

    suspend fun saveServerConnections(methods: List<ServerConnectionMethod>) {
        dataStore.edit { preferences ->
            preferences[SERVER_CONNECTIONS] = Json.encodeToString(methods)
        }
    }

    fun isMtlsEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[MTLS_ENABLED] ?: PreferenceConstants.DEFAULT_MTLS_ENABLED
    }

    fun getMtlsCertificateName(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[MTLS_CERTIFICATE_NAME]
    }

    fun getMtlsCertificatePassword(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[MTLS_CERTIFICATE_PASSWORD]
    }

    suspend fun setMtlsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[MTLS_ENABLED] = enabled
        }
    }

    suspend fun saveMtlsCertificate(bytes: ByteArray, displayName: String) {
        withContext(Dispatchers.IO) {
            if (mtlsCertificateFile.exists()) {
                mtlsCertificateFile.delete()
            }
            val cipher = aead.encrypt(bytes, AEAD_ASSOCIATED_DATA_MTLS.toByteArray())
            mtlsCertificateFile.outputStream().use { output ->
                output.write(cipher)
            }
        }
        dataStore.edit { preferences ->
            preferences[MTLS_CERTIFICATE_NAME] = displayName
            preferences.remove(MTLS_CERTIFICATE_PASSWORD)
        }
    }

    suspend fun clearMtlsCertificate() {
        withContext(Dispatchers.IO) {
            if (mtlsCertificateFile.exists()) {
                mtlsCertificateFile.delete()
            }
        }
        dataStore.edit { preferences ->
            preferences.remove(MTLS_CERTIFICATE_NAME)
            preferences.remove(MTLS_CERTIFICATE_PASSWORD)
        }
    }

    suspend fun getMtlsCertificateBytes(): ByteArray? = withContext(Dispatchers.IO) {
        if (!mtlsCertificateFile.exists()) {
            return@withContext null
        }
        runCatching {
            val cipherBytes = mtlsCertificateFile.readBytes()
            aead.decrypt(cipherBytes, AEAD_ASSOCIATED_DATA_MTLS.toByteArray())
        }.getOrNull()
    }

    suspend fun setMtlsCertificatePassword(password: String) {
        dataStore.edit { preferences ->
            if (password.isBlank()) {
                preferences.remove(MTLS_CERTIFICATE_PASSWORD)
            } else {
                preferences[MTLS_CERTIFICATE_PASSWORD] = password
            }
        }
    }

    suspend fun clearServerConfiguration() {
        dataStore.edit { preferences ->
            preferences.remove(SERVER_URL)
            preferences.remove(SERVER_NAME)
            preferences.remove(SERVER_VERSION)
            preferences.remove(SERVER_CONNECTED)
            preferences.remove(SERVER_LEGACY_PLAYBACK)
            preferences[SERVER_CONNECTIONS] = Json.encodeToString<List<ServerConnectionMethod>>(emptyList())
        }
    }

    fun getOfflineMode(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[OFFLINE_MODE] ?: PreferenceConstants.DEFAULT_OFFLINE_MODE
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OFFLINE_MODE] = enabled
        }
    }
}