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

    fun saveToken(service: OTPService, context: Context) {
        viewModelScope.launch {
            val tokensDir = TokenFileManager.getTokensDirectory(context.filesDir)
            if (tokenFileManager.saveEncryptedToken(tokensDir, service)) {
                sendToWearable(context, service)
                loadTokensFromDirectory(context)
            }
        }
    }

    fun deleteToken(tokenId: String, context: Context) {
        viewModelScope.launch {
            val tokensDir = TokenFileManager.getTokensDirectory(context.filesDir)
            if (tokenFileManager.deleteToken(tokensDir, tokenId)) {
                removeToWearable(context, tokenId)
                loadTokensFromDirectory(context)
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

    fun removeToWearable(context: Context, tokenId: String) {
        viewModelScope.launch {
            try {
                val putDataMapReq = PutDataMapRequest.create("/delete-token").apply {
                    dataMap.putString("id", tokenId)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                val request = putDataMapReq.asPutDataRequest().setUrgent()

                Wearable.getDataClient(context).putDataItem(request)
                    .addOnSuccessListener {
                        Log.d("OTPViewModel", "Successfully sent delete request to wearable for token: $tokenId")
                    }
                    .addOnFailureListener {
                        Log.e("OTPViewModel", "Failed to connect to wearable for delete request: ${it.message}")
                    }
            } catch (e: Exception) {
                Log.e("OTPViewModel", "Error connecting to wearable for delete request: ${e.message}")
            }
        }
    }
}