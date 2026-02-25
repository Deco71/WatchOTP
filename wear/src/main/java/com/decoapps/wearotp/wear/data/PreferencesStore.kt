package com.decoapps.wearotp.wear.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesStore(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val LAST_SYNC = longPreferencesKey("last_sync")
    }


    val currentLastSync: Flow<Long?> =
        dataStore.data.map { preferences ->
            preferences[LAST_SYNC]
        }

    suspend fun saveLastSync(lastSync: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC] = lastSync
        }
    }
}