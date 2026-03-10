package com.decoapps.wearotp.wear.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.decoapps.wearotp.shared.crypto.TokenFileManager
import com.decoapps.wearotp.shared.crypto.rsaDecrypt
import com.decoapps.wearotp.shared.data.OTPService
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.io.File
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import com.decoapps.wearotp.shared.cryptoPreferences.CryptoPreferences

fun elaborateDataItem(dataItem: DataItem, tokensDir: File, cryptoPrefs: CryptoPreferences) : Long? {

    val tokenFileManager = TokenFileManager()

    var last_sync : Long? = null

    when {
        dataItem.uri.path?.startsWith("/create-token") == true -> {

            val id = dataItem.uri.pathSegments.lastOrNull() ?: return last_sync

            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

            val privateKeyBase64 = cryptoPrefs.privateKeyBase64
            val secret: String
            val algorithm: String
            val digits: Int
            val interval: Int

            if (privateKeyBase64 != null && dataMap.containsKey("secret_encrypted")) {
                try {
                    val keyFactory = KeyFactory.getInstance("RSA")
                    val privateKey = keyFactory.generatePrivate(
                        PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64))
                    )
                    secret = rsaDecrypt(Base64.getDecoder().decode(dataMap.getString("secret")!!), privateKey)
                    algorithm = rsaDecrypt(Base64.getDecoder().decode(dataMap.getString("algorithm")!!), privateKey)
                    digits = rsaDecrypt(Base64.getDecoder().decode(dataMap.getString("digits")!!), privateKey).toInt()
                    interval = rsaDecrypt(Base64.getDecoder().decode(dataMap.getString("interval")!!), privateKey).toInt()
                } catch (e: Exception) {
                    Log.e("DataEventUtils", "RSA decryption failed: ${e.message}")
                    return last_sync
                }
            } else {
                // Fallback per messaggi non cifrati (retrocompatibilità)
                Log.w("DataEventUtils", "Received unencrypted token data, skipping for security.")
                return last_sync
            }

            val newService = OTPService(
                id = id,
                issuer = dataMap.getString("issuer"),
                accountName = dataMap.getString("accountName"),
                secret = secret,
                algorithm = algorithm,
                digits = digits,
                interval = interval,
            )
            last_sync = dataMap.getLong("timestamp")
            tokenFileManager.saveEncryptedToken(tokensDir, newService)
        }

        dataItem.uri.path?.startsWith("/delete-token") == true -> {

            val id = dataItem.uri.pathSegments.lastOrNull() ?: return last_sync

            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            last_sync = dataMap.getLong("timestamp")

            tokenFileManager.deleteToken(tokensDir, id)
        }
    }

    val lastSyncDir = tokensDir.parent

    File(lastSyncDir, "last_sync").writeText(last_sync?.toString() ?: "")

    return last_sync
}

fun publishPublicKey(context: Context, publicKeyBase64: String) {
    try {
        val putDataMapReq = PutDataMapRequest.create("/public-key").apply {
            dataMap.putString("publicKey", publicKeyBase64)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        val request = putDataMapReq.asPutDataRequest().setUrgent()

        Wearable.getDataClient(context).putDataItem(request)
            .addOnSuccessListener {
                Log.d("DataEventUtils", "Public key published to Wearable Data Layer")
            }
            .addOnFailureListener {
                Log.e("DataEventUtils", "Failed to publish public key: ${it.message}")
            }
    } catch (e: Exception) {
        Log.e("DataEventUtils", "Error publishing public key: ${e.message}")
    }
}

fun removePendingDataItem(context: Context, uri: Uri) {
    Wearable.getDataClient(context).deleteDataItems(uri)
        .addOnSuccessListener {
            Log.d("OTPViewModel", "Successfully removed data item with URI: $uri")
        }
        .addOnFailureListener {
            Log.e("OTPViewModel", "Failed to remove data item with URI: $uri, error: ${it.message}")
        }
}
