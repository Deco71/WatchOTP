package com.decoapps.wearotp.mobile.utils.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class QrScanViewModel @Inject constructor(
) : ViewModel() {
    private val _uiState: MutableStateFlow<QrScanUIState> = MutableStateFlow(QrScanUIState())
    val uiState: StateFlow<QrScanUIState> = _uiState

    fun onQrCodeDetected(result: String) {
        Log.d("Scanner", result)
        _uiState.update { it.copy(detectedQR = result) }
    }
}

data class QrScanUIState(
    val loading: Boolean = false,
    val detectedQR: String = "",
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
)