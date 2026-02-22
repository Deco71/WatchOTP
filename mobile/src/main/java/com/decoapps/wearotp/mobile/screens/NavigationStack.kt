package com.decoapps.wearotp.mobile.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.decoapps.wearotp.mobile.screens.otp.OTPScreen
import com.decoapps.wearotp.mobile.screens.addOtp.AddOTPScreen
import com.decoapps.wearotp.mobile.screens.otp.OTPViewModel
import com.decoapps.wearotp.mobile.screens.settings.SettingsScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val otpViewModel: OTPViewModel = viewModel<OTPViewModel>()

    NavHost(navController = navController, startDestination = Screen.OTP.route) {
        composable(route = Screen.OTP.route) {
            OTPScreen(
                navController,
                otpViewModel
            )
        }
        composable(route = Screen.AddOTP.route) {
            AddOTPScreen(
                modifier,
                navController,
                otpViewModel
            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navController
            )
        }
    }
}