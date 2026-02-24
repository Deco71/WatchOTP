package com.decoapps.wearotp.mobile.utils.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.decoapps.wearotp.mobile.screens.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class QrScanViewModel @Inject constructor(
) : ViewModel() {
    private val _uiState: MutableStateFlow<QrScanUIState> = MutableStateFlow(QrScanUIState())
    val uiState: StateFlow<QrScanUIState> = _uiState

    private var clearErrorJob: Job? = null

    fun onQrCodeDetected(result: String, navController: NavController) {
        Log.d("Scanner", result)
        _uiState.update { it.copy(detectedQR = result, error = null) }
        navController.navigate(Screen.OTP.route)

    }

    fun onQrCodeNotValid(error: String) {
        _uiState.update {
            it.copy(error = error)
        }
        clearErrorJob?.cancel()
        clearErrorJob = viewModelScope.launch {
            delay(3_000L)
            _uiState.update { it.copy(error = null) }
        }
    }
}

data class QrScanUIState(
    val loading: Boolean = false,
    val detectedQR: String = "",
    val error: String? = null,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
)