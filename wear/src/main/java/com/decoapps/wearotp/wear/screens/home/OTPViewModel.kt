package com.decoapps.wearotp.wear.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decoapps.wearotp.shared.crypto.TokenFileManager
import com.decoapps.wearotp.shared.data.OTPService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class OTPViewModel() : ViewModel() {
    private val tokenFileManager = TokenFileManager()

    private val _otpServices = MutableStateFlow<List<OTPService>>(emptyList())
    val otpServices: StateFlow<List<OTPService>> = _otpServices

    fun loadTokensFromDirectory(directoryName: String, context: Context) {
        viewModelScope.launch {
            val tokensDir = File(context.filesDir, directoryName)
            val services = tokenFileManager.loadEncryptedTokens(tokensDir)
            _otpServices.value = services
        }
    }

    /*fun saveToken(service: OTPService, context: Context) {
        viewModelScope.launch {
            val tokensDir = File(context.filesDir, "tokens")
            if (tokenFileManager.saveEncryptedToken(tokensDir, service)) {
                loadTokensFromDirectory("tokens", context)
            }
        }
    }

    fun deleteToken(tokenId: String, context: Context) {
        viewModelScope.launch {
            val tokensDir = File(context.filesDir, "tokens")
            val tokenFile = File(tokensDir, tokenId)
            if (tokenFileManager.deleteToken(tokenFile)) {
                // Ricarica i token dopo la cancellazione
                loadTokensFromDirectory("tokens", context)
            }
        }
    }*/
}