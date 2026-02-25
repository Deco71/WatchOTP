package com.decoapps.wearotp.mobile.screens.otp

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decoapps.wearotp.shared.crypto.TokenFileManager
import com.decoapps.wearotp.shared.data.OTPService
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
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

    fun saveToken(service: OTPService, context: Context) {
        viewModelScope.launch {
            val tokensDir = File(context.filesDir, "tokens")
            sendToWearable(context, service)
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
    }

    fun sendToWearable(context: Context, service: OTPService) {
        viewModelScope.launch {
            try {
                val putDataMapReq = PutDataMapRequest.create("/create-token").apply {
                    dataMap.putString("id", service.id)
                    dataMap.putString("issuer", service.issuer ?: "")
                    dataMap.putString("accountName", service.accountName ?: "")
                    dataMap.putString("secret", service.secret)
                    dataMap.putString("algorithm", service.algorithm)
                    dataMap.putInt("digits", service.digits)
                    dataMap.putInt("interval", service.interval)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                val request = putDataMapReq.asPutDataRequest().setUrgent()

                Wearable.getDataClient(context).putDataItem(request)
                    .addOnSuccessListener {
                        Log.d("OTPViewModel", "Successfully sent token to wearable: ${service.id}")
                    }
                    .addOnFailureListener {
                        Log.e("OTPViewModel", "Failed to connect to wearable: ${it.message}")
                    }
            } catch (e: Exception) {
                Log.e("OTPViewModel", "Error connecting to wearable: ${e.message}")
            }
        }
    }
}