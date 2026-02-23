package com.example.medixmvp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "medix_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ipKey = stringPreferencesKey("server_ip")
    private val portKey = stringPreferencesKey("server_port")
    private val sessionIdKey = stringPreferencesKey("session_id")
    private val continuousKey = booleanPreferencesKey("continuous")

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { pref ->
        AppSettings(
            serverIp = pref[ipKey] ?: "192.168.1.50",
            serverPort = pref[portKey] ?: "8000",
            sessionId = pref[sessionIdKey] ?: UUID.randomUUID().toString(),
            continuousMode = pref[continuousKey] ?: true
        )
    }

    suspend fun saveServer(ip: String, port: String) {
        context.dataStore.edit { it[ipKey] = ip; it[portKey] = port }
    }

    suspend fun saveContinuousMode(enabled: Boolean) {
        context.dataStore.edit { it[continuousKey] = enabled }
    }

    suspend fun ensureSessionId(): String {
        var id = ""
        context.dataStore.edit {
            id = it[sessionIdKey] ?: UUID.randomUUID().toString().also { uuid -> it[sessionIdKey] = uuid }
        }
        return id
    }

    suspend fun resetSessionId() {
        context.dataStore.edit { it[sessionIdKey] = UUID.randomUUID().toString() }
    }
}

data class AppSettings(
    val serverIp: String,
    val serverPort: String,
    val sessionId: String,
    val continuousMode: Boolean
)
