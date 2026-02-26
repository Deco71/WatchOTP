package com.decoapps.wearotp.mobile.data

import android.content.Context
import android.util.Log
import com.decoapps.wearotp.shared.data.OTPService
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

/*fun elaborateDataItem(dataItem: DataItem) {

    Log.d("WATCH_CONNECTION", "Elaborating data changes from Service")

    when {
        dataItem.uri.path?.startsWith("/create-token") == true -> {
        }
    }
}*/

fun sendToWearable(context: Context, service: OTPService) {
    try {
        val putDataMapReq = PutDataMapRequest.create("/create-token/${service.id}").apply {
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

fun removeToWearable(context: Context, tokenId: String) {
    try {
        val putDataMapReq = PutDataMapRequest.create("/delete-token/${tokenId}").apply {
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