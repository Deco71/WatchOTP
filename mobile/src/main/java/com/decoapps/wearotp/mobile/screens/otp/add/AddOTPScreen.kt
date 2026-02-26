package com.decoapps.wearotp.mobile.screens.otp.add

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.decoapps.wearotp.mobile.utils.camera.QrScanningScreen

@OptIn(ExperimentalGetImage::class)
@Composable
fun AddOTPScreen(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        QrScanningScreen(navController)
    }
}