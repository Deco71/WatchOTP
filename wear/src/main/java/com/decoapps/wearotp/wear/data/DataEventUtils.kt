package com.decoapps.wearotp.wear.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.decoapps.wearotp.shared.crypto.TokenFileManager
import com.decoapps.wearotp.shared.data.OTPService
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import java.io.File

fun elaborateDataItem(dataItem: DataItem, tokensDir: File) : Long? {

    val tokenFileManager = TokenFileManager()

    var last_sync : Long? = null

    when {
        dataItem.uri.path?.startsWith("/create-token") == true -> {

            val id = dataItem.uri.pathSegments.lastOrNull() ?: return last_sync

            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            val newService = OTPService(
                id = id,
                issuer = dataMap.getString("issuer"),
                accountName = dataMap.getString("accountName"),
                secret = dataMap.getString("secret")!!,
                algorithm = dataMap.getString("algorithm")!!,
                digits = dataMap.getInt("digits"),
                interval = dataMap.getInt("interval"),
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

    File(tokensDir, "last_sync").writeText(last_sync?.toString() ?: "")

    return last_sync
}

fun removeDataItemFromWearable(context: Context, uri: Uri) {
    Wearable.getDataClient(context).deleteDataItems(uri)
        .addOnSuccessListener {
            Log.d("OTPViewModel", "Successfully removed data item with URI: $uri")
        }
        .addOnFailureListener {
            Log.e("OTPViewModel", "Failed to remove data item with URI: $uri, error: ${it.message}")
        }
}
