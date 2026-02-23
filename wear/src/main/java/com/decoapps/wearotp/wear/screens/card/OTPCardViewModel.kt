package com.decoapps.wearotp.wear.screens.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant


enum class ProgressColorLevel { CRITICAL, WARNING, NORMAL }

data class OTPCardUiState(
    val timeProgress: Float = 1f,
    val showDeleteDialog: Boolean = false,
    val deleteDialogServiceName: String = "",
    val progressColorLevel: ProgressColorLevel = ProgressColorLevel.NORMAL
)


class OTPCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OTPCardUiState())
    val uiState: StateFlow<OTPCardUiState> = _uiState.asStateFlow()

    private val period = 30L

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val epoch = Instant.now().epochSecond
                val secondsInPeriod = epoch % period
                val progress = 1f - (secondsInPeriod / period.toFloat())
                val level = when {
                    progress < 0.2f -> ProgressColorLevel.CRITICAL
                    progress < 0.4f -> ProgressColorLevel.WARNING
                    else -> ProgressColorLevel.NORMAL
                }
                _uiState.value = _uiState.value.copy(
                    timeProgress = progress,
                    progressColorLevel = level
                )
                delay(1000L)
            }
        }
    }

    fun formatToken(token: String): String {
        return if (token.length == 6) "${token.substring(0, 3)} ${token.substring(3)}"
        else token
    }
}