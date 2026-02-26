package com.decoapps.wearotp.mobile

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.decoapps.wearotp.mobile.screens.NavigationStack
import com.decoapps.wearotp.mobile.screens.otp.OTPViewModel
import com.decoapps.wearotp.mobile.theme.AppTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import kotlin.getValue
class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val otpViewModel: OTPViewModel by viewModels()
    private val dataClient by lazy { Wearable.getDataClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        setContent {
            AppTheme() {
                NavigationStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
        otpViewModel.loadTokensFromDirectory(this)
        Wearable.getDataClient(this)
            .dataItems
            .addOnSuccessListener { dataItemBuffer ->
                dataItemBuffer.forEach { dataItem ->
                    Log.d("WATCH_CONNECTION", "Elaborating DataItem: ${dataItem.uri}")
                    //elaborateDataItem(dataItem, TokenFileManager.getTokensDirectory(filesDir))
                }
                dataItemBuffer.release()
            }
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WATCH_CONNECTION", "Querying data changes (mobile)")

        for (event in dataEvents) {
            Log.d("WATCH_CONNECTION", "Received data change event: ${event.type} for URI: ${event.dataItem.uri}")
            /*if (event.type == DataEvent.TYPE_CHANGED)
                elaborateDataItem(event.dataItem)*/
        }

    }
}