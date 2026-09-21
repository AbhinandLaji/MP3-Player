package com.example.smartshuffle.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val SLEEP_TIMER_TARGET_KEY = longPreferencesKey("sleep_timer_target_millis")
        val FAVORITE_FOLDERS_KEY = stringSetPreferencesKey("favorite_folder_paths")
        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val LAST_PLAYED_POSITION_MS = longPreferencesKey("last_played_position_ms")
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

    suspend fun savePlaybackState(songId: Long, positionMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_PLAYED_SONG_ID] = songId
            preferences[LAST_PLAYED_POSITION_MS] = positionMs
        }
    }

    suspend fun getPlaybackState(): Pair<Long?, Long?> {
        val preferences = context.dataStore.data.first()
        return Pair(preferences[LAST_PLAYED_SONG_ID], preferences[LAST_PLAYED_POSITION_MS])
    }
}
