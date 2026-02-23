package com.decoapps.wearotp.mobile.screens

sealed class Screen(val route: String) {
    object OTP : Screen("OTPScreen")
    object AddOTP : Screen("AddOTPScreen")
    object Settings : Screen("SettingsScreen")
    object AddOTPManually : Screen("AddOTPManuallyScreen")
}