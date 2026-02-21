package com.decoapps.wearotp.mobile.screens;

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.decoapps.wearotp.mobile.screens.otp.OTPScreen
import com.decoapps.wearotp.mobile.screens.addOtp.AddOTPScreen
import com.decoapps.wearotp.mobile.screens.settings.SettingsScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.OTP.route) {
        composable(route = Screen.OTP.route) {
            OTPScreen(

            )
        }
        composable(route = Screen.AddOTP.route) {
            AddOTPScreen(

            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(

            )
        }
    }
}