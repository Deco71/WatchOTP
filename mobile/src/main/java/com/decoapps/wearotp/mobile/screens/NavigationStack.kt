package com.decoapps.wearotp.mobile.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.decoapps.wearotp.mobile.screens.otp.screen.OTPScreen
import com.decoapps.wearotp.mobile.screens.otp.add.AddOTPScreen
import com.decoapps.wearotp.mobile.screens.settings.SettingsScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.OTP.route) {
        composable(route = Screen.OTP.route) {
            OTPScreen(
                navController
            )
        }
        composable(route = Screen.AddOTP.route) {
            AddOTPScreen(
                modifier,
                navController
            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navController
            )
        }
    }
}