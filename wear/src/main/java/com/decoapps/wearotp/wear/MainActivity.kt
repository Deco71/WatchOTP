package com.decoapps.wearotp.wear

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
import com.decoapps.wearotp.shared.data.OTPService
import com.decoapps.wearotp.wear.data.PreferencesViewModel
import com.decoapps.wearotp.wear.screens.home.OTPList
import com.decoapps.wearotp.wear.screens.home.OTPViewModel
import com.decoapps.wearotp.wear.theme.AppTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

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
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WATCH_CONNECTION", "Querying data changes")

        for (event in dataEvents) {
            when (event.type) {
                DataEvent.TYPE_CHANGED if event.dataItem.uri.path == "/create-token"
                    -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    //check if file with same id already exists
                    val existingFile = otpViewModel.otpServices.value.find { it.id == dataMap.getString("id") }
                    if (existingFile != null) {
                        return
                    }
                    val newService = OTPService(
                        id = dataMap.getString("id")!!,
                        issuer = dataMap.getString("issuer"),
                        accountName = dataMap.getString("accountName"),
                        secret = dataMap.getString("secret")!!,
                        algorithm = dataMap.getString("algorithm")!!,
                        digits = dataMap.getInt("digits"),
                        interval = dataMap.getInt("interval"),
                    )
                    preferencesViewModel.saveLastSync(dataMap.getLong("timestamp"))
                    otpViewModel.saveToken(newService, this)
                }

                DataEvent.TYPE_CHANGED if event.dataItem.uri.path == "/delete-token"
                    -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val id = dataMap.getString("id") ?: return
                    otpViewModel.deleteToken(id, this)
                }

                DataEvent.TYPE_CHANGED if event.dataItem.uri.path == "/sync" -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val idList = dataMap.getStringArrayList("idList") ?: return
                    val existingIds = otpViewModel.otpServices.value.map { it.id }
                    val toDelete = existingIds.filterNot { idList.contains(it) }
                    toDelete.forEach { otpViewModel.deleteToken(it, this) }
                }
            }
        }
    }
}