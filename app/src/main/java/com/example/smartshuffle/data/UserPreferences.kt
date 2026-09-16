package com.example.smartshuffle.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val SLEEP_TIMER_TARGET_KEY = longPreferencesKey("sleep_timer_target_millis")
        val FAVORITE_FOLDERS_KEY = stringSetPreferencesKey("favorite_folder_paths")
    }

    val sleepTimerTargetFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[SLEEP_TIMER_TARGET_KEY]
    }

    suspend fun setSleepTimerTarget(targetMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[SLEEP_TIMER_TARGET_KEY] = targetMillis
        }
    }

    suspend fun clearSleepTimerTarget() {
        context.dataStore.edit { preferences ->
            preferences.remove(SLEEP_TIMER_TARGET_KEY)
        }
    }
}
