package com.decoapps.wearotp.mobile.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.decoapps.wearotp.mobile.StoreApplication
import com.decoapps.wearotp.mobile.theme.ColorMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val preferencesStore: PreferencesStore
): ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as StoreApplication)
                PreferencesViewModel(application.preferencesStore)
            }
        }
    }

    val currentColorMode: StateFlow<String> =
        preferencesStore.currentColorMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ColorMode.DYNAMIC.toString()
        )

    fun saveColorMode(colorMode: ColorMode) {
        viewModelScope.launch {
            preferencesStore.saveColorMode(colorMode)
        }
    }
}