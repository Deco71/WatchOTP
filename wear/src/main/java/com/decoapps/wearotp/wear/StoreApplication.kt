package com.decoapps.wearotp.wear

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.decoapps.wearotp.wear.data.PreferencesStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)

class StoreApplication: Application() {

    lateinit var preferencesStore: PreferencesStore
    override fun onCreate() {
        super.onCreate()
        preferencesStore = PreferencesStore(dataStore)
    }
}