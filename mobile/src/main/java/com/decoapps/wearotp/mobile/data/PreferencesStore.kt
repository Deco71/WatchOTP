package com.decoapps.wearotp.mobile.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.decoapps.wearotp.mobile.theme.ColorMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesStore(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val COLOR_MODE = stringPreferencesKey("color_mode")
    }


    val currentColorMode: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[COLOR_MODE] ?: ColorMode.DYNAMIC.toString()
        }

    suspend fun saveColorMode(colorMode: ColorMode) {
        dataStore.edit { preferences ->
            preferences[COLOR_MODE] = colorMode.toString()
        }
    }
}