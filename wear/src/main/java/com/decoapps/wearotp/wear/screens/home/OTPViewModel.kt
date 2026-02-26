package com.decoapps.wearotp.wear.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decoapps.wearotp.shared.crypto.TokenFileManager
import com.decoapps.wearotp.shared.data.OTPService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OTPViewModel() : ViewModel() {
    private val tokenFileManager = TokenFileManager()

    private val _otpServices = MutableStateFlow<List<OTPService>>(emptyList())
    val otpServices: StateFlow<List<OTPService>> = _otpServices

    fun loadTokensFromDirectory(context: Context) {
        viewModelScope.launch {
            val tokensDir = TokenFileManager.getTokensDirectory(context.filesDir)
            val services = tokenFileManager.loadEncryptedTokens(tokensDir)
            _otpServices.value = services
        }
    }
}