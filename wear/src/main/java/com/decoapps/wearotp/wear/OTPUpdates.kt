package com.decoapps.wearotp.wear

import android.util.Log
import com.decoapps.wearotp.shared.crypto.TokenFileManager
import com.decoapps.wearotp.shared.data.OTPService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import java.io.File

class OTPUpdates : WearableListenerService() {

    companion object {
        fun elaborateDataEvent(dataEvents: DataEventBuffer, tokensDir: File) : Long? {
            val tokenFileManager = TokenFileManager()

            var last_sync : Long? = null

            for (event in dataEvents) {
                when (event.type) {
                    DataEvent.TYPE_CHANGED if event.dataItem.uri.path == "/create-token"
                        -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        //check if file with same id already exists
                        //val existingFile = otpViewModel.otpServices.value.find { it.id == dataMap.getString("id") }
                        val newService = OTPService(
                            id = dataMap.getString("id")!!,
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

                    DataEvent.TYPE_CHANGED if event.dataItem.uri.path == "/delete-token"
                        -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        last_sync = dataMap.getLong("timestamp")
                        val id = dataMap.getString("id") ?: return last_sync

                        tokenFileManager.deleteToken(tokensDir, id)
                    }

                    /*DataEvent.TYPE_CHANGED if event.dataItem.uri.path == "/sync" -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        last_sync = dataMap.getLong("timestamp")
                        val idList = dataMap.getStringArrayList("idList") ?: return


                        val existingIds = otpViewModel.otpServices.value.map { it.id }
                        val toDelete = existingIds.filterNot { idList.contains(it) }
                        toDelete.forEach { otpViewModel.deleteToken(it, this) }
                    }*/
                }
            }

            File(tokensDir, "last_sync").writeText(last_sync?.toString() ?: "")

            return last_sync
        }
    }
    private lateinit var tokensDir: File

    override fun onCreate() {
        super.onCreate()
        tokensDir = TokenFileManager.getTokensDirectory(filesDir)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WATCH_CONNECTION", "Querying data changes from Service")

        if (MainActivity.isInForeground) {
            return
        }

        elaborateDataEvent(dataEvents, tokensDir)
    }
}
