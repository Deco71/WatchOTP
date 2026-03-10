package com.decoapps.wearotp.mobile.data

import android.content.Context
import android.util.Log
import com.decoapps.wearotp.shared.crypto.rsaEncrypt
import com.decoapps.wearotp.shared.data.OTPService
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

fun fetchWearPublicKey(context: Context, onResult: (PublicKey?) -> Unit) {
    Wearable.getDataClient(context)
        .dataItems
        .addOnSuccessListener { dataItemBuffer ->
            var publicKey: PublicKey? = null
            dataItemBuffer.forEach { dataItem ->
                if (dataItem.uri.path?.startsWith("/public-key") == true) {
                    try {
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        val publicKeyBase64 = dataMap.getString("publicKey")
                        if (publicKeyBase64 != null) {
                            val keyBytes = Base64.getDecoder().decode(publicKeyBase64)
                            val keyFactory = KeyFactory.getInstance("RSA")
                            publicKey = keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
                        }
                    } catch (e: Exception) {
                        Log.e("DataEventUtils", "Failed to parse wear public key: ${e.message}")
                    }
                }
            }
            dataItemBuffer.release()
            onResult(publicKey)
        }
        .addOnFailureListener {
            Log.e("DataEventUtils", "Failed to fetch wear public key: ${it.message}")
            onResult(null)
        }
}

fun sendToWearable(context: Context, service: OTPService) {
    fetchWearPublicKey(context) { publicKey ->
        if (publicKey == null) {
            Log.e("DataEventUtils", "Cannot send token: wearable public key not available. The wear app must be running and connected.")
            return@fetchWearPublicKey
        }

        try {
            val encoder = Base64.getEncoder()
            val putDataMapReq = PutDataMapRequest.create("/create-token/${service.id}").apply {
                // non-sensitive fields
                dataMap.putString("issuer", service.issuer ?: "")
                dataMap.putString("accountName", service.accountName ?: "")
                dataMap.putLong("timestamp", System.currentTimeMillis())

                // sensitive fields
                dataMap.putString("secret", encoder.encodeToString(rsaEncrypt(service.secret, publicKey)))
                dataMap.putString("algorithm", encoder.encodeToString(rsaEncrypt(service.algorithm, publicKey)))
                dataMap.putString("digits", encoder.encodeToString(rsaEncrypt(service.digits.toString(), publicKey)))
                dataMap.putString("interval", encoder.encodeToString(rsaEncrypt(service.interval.toString(), publicKey)))
            }
            val request = putDataMapReq.asPutDataRequest().setUrgent()

            Wearable.getDataClient(context).putDataItem(request)
                .addOnSuccessListener {
                    Log.d("DataEventUtils", "Successfully sent encrypted token to wearable: ${service.id}")
                }
                .addOnFailureListener {
                    Log.e("DataEventUtils", "Failed to send token to wearable: ${it.message}")
                }
        } catch (e: Exception) {
            Log.e("DataEventUtils", "Error encrypting or sending token: ${e.message}")
        }
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
                Log.d("DataEventUtils", "Successfully sent delete request to wearable for token: $tokenId")
            }
            .addOnFailureListener {
                Log.e("DataEventUtils", "Failed to send delete request to wearable for token $tokenId: ${it.message}")
            }
    } catch (e: Exception) {
        Log.e("DataEventUtils", "Error sending delete request to wearable: ${e.message}")
    }
}

/*fun syncData() {
    try {
        // Implement any additional synchronization logic if needed
        Log.d("DataEventUtils", "Data synchronization completed successfully.")
    } catch (e: Exception) {
        Log.e("DataEventUtils", "Error during data synchronization: ${e.message}")
    }
}*/