package com.decoapps.wearotp.shared.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.decoapps.wearotp.shared.data.OTPService
import com.decoapps.wearotp.shared.data.TOTP
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.util.Locale

enum class ProgressColorLevel { CRITICAL, WARNING, NORMAL }

class OTPCardViewModel(val service: OTPService) : ViewModel() {

    companion object {
        fun factory(service: OTPService): ViewModelProvider.Factory =
            @Suppress("UNCHECKED_CAST")
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OTPCardViewModel(service) as T
            }
    }

    private val period = service.interval

    val timeProgress: StateFlow<Float> = timerFlow(period)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)

    val progressColorLevel: StateFlow<ProgressColorLevel> = timerFlow(period)
        .map { progress ->
            when {
                progress < 0.2f -> ProgressColorLevel.CRITICAL
                progress < 0.4f -> ProgressColorLevel.WARNING
                else -> ProgressColorLevel.NORMAL
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressColorLevel.NORMAL)

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    private val _deleteDialogServiceName = MutableStateFlow("")
    val deleteDialogServiceName: StateFlow<String> = _deleteDialogServiceName

    val token: StateFlow<String> = timerFlow(period)
        .map { progress ->
            if (progress > 0.98f) getToken() else token.value
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getToken())

    private fun getToken(): String {
        val interval: Long = service.interval.toLong()
        val epoch = Instant.now().epochSecond
        val T: Long = epoch / interval
        val steps = T.toHexString().uppercase(Locale.getDefault())
        return TOTP.generateTOTP(service.secret, steps, service.digits.toString(), "Hmac" + service.algorithm)
    }

    private fun timerFlow(period: Int) = flow {
        while (true) {
            val epoch = Instant.now().epochSecond
            val secondsInPeriod = epoch % period
            val progress = 1f - (secondsInPeriod / period.toFloat())
            emit(progress)

            val millis = 1000L - (System.currentTimeMillis() % 1000L)
            delay(millis)
        }
    }

    fun onCardClick(context: Context) {
        val token = token.value
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("TOTP", token)
        clipboard.setPrimaryClip(clip)
    }

    fun onCardLongClick() {
        _showDeleteDialog.value = true
        _deleteDialogServiceName.value = service.issuer ?: "service?"
    }

    fun onDeleteConfirmed(service: OTPService, onDelete: ((OTPService) -> Unit)?) {
        _showDeleteDialog.value = false
        onDelete?.invoke(service)
    }

    fun onDeleteDismissed() {
        _showDeleteDialog.value = false
    }

    fun formatToken(token: String): String {
        return if (token.length == 6) "${token.substring(0, 3)} ${token.substring(3)}"
        else token
    }
}