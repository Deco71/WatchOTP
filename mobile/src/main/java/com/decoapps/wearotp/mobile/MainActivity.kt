package com.decoapps.wearotp.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.decoapps.wearotp.mobile.screens.NavigationStack
import com.decoapps.wearotp.mobile.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme() {
                NavigationStack()
            }
        }
    }
}