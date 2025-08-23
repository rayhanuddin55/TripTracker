package com.rayhan.triptracker.data.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val INTERVAL_SEC = intPreferencesKey("interval_sec")
        val BG_ENABLED = booleanPreferencesKey("bg_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val intervalSeconds: Flow<Int> = context.dataStore.data.map { it[Keys.INTERVAL_SEC] ?: 5 }
    val backgroundEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.BG_ENABLED] ?: true }
    val darkMode: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.DARK_MODE] ?: false }

    suspend fun setInterval(seconds: Int) {
        context.dataStore.edit { it[Keys.INTERVAL_SEC] = seconds }
    }

    suspend fun setBackground(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BG_ENABLED] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }


}
