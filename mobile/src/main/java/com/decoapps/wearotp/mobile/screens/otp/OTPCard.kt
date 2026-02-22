package com.decoapps.wearotp.mobile.screens.otp

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.decoapps.wearotp.shared.data.OTPService

@Composable
fun OTPCard(
    service: OTPService,
    modifier: Modifier = Modifier
) {
    Text(
        text = service.name ?: "Unknown Service",
        modifier = modifier
    )
}