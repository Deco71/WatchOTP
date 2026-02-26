package com.decoapps.wearotp.wear

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.decoapps.wearotp.shared.crypto.TokenFileManager
import com.decoapps.wearotp.wear.data.PreferencesViewModel
import com.decoapps.wearotp.wear.data.elaborateDataItem
import com.decoapps.wearotp.wear.data.removeDataItemFromWearable
import com.decoapps.wearotp.wear.screens.home.OTPList
import com.decoapps.wearotp.wear.screens.home.OTPViewModel
import com.decoapps.wearotp.wear.theme.AppTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import java.io.File

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val otpViewModel: OTPViewModel by viewModels()

    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val preferencesViewModel: PreferencesViewModel by viewModels { PreferencesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        setContent {
            AppTheme() {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    OTPList(
                        Modifier
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)

        Wearable.getDataClient(this)
            .dataItems
            .addOnSuccessListener { dataItemBuffer ->
                var uriToRemove: List<Uri> = emptyList()

                dataItemBuffer.forEach { dataItem ->
                    elaborateDataItem(dataItem, TokenFileManager.getTokensDirectory(filesDir))
                    dataItem.uri.let { uriToRemove = uriToRemove + it }
                }
                dataItemBuffer.release()

                otpViewModel.loadTokensFromDirectory(this)

                val lastSyncFile = File(filesDir, "last_sync")
                if (lastSyncFile.exists()) {
                    lastSyncFile.readText().toLongOrNull()?.let { preferencesViewModel.saveLastSync(it) }
                }

                Log.d("WATCH_CONNECTION", "Elaborated Uris to remove: ${uriToRemove.joinToString(", ")}")
                if (uriToRemove.isNotEmpty()) {
                    for (uri in uriToRemove) {
                        removeDataItemFromWearable(this, uri)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("WATCH_CONNECTION", "Failed to query data items: ${it.message}")
            }
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WATCH_CONNECTION", "Querying data changes")
        var lastSync: Long? = null

        var uriToRemove: List<Uri> = emptyList()

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                lastSync = elaborateDataItem(event.dataItem, TokenFileManager.getTokensDirectory(filesDir))
                event.dataItem.uri.let { uriToRemove = uriToRemove + it }
            }
        }

        if (uriToRemove.isNotEmpty()) {
            for (uri in uriToRemove) {
                removeDataItemFromWearable(this, uri)
            }
        }

        otpViewModel.loadTokensFromDirectory(this)

        if (lastSync != null) preferencesViewModel.saveLastSync(lastSync)
    }
}