package com.decoapps.wearotp.mobile.screens.otp.add

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.decoapps.wearotp.mobile.screens.Screen
import com.decoapps.wearotp.mobile.screens.otp.OTPViewModel
import com.decoapps.wearotp.shared.data.OTPService
import java.util.UUID


@Composable
fun AddOTPManually(modifier: Modifier = Modifier, navController: NavController) {
    val serviceName = remember { mutableStateOf("") }
    val secretToken = remember { mutableStateOf("") }
    val otpViewModel: OTPViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = serviceName.value,
                onValueChange = { serviceName.value = it },
                label = { Text("Enter service name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            TextField(
                value = secretToken.value,
                onValueChange = { secretToken.value = it },
                label = { Text("Enter Secret Token") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    otpViewModel.saveToken(OTPService( UUID.randomUUID().toString().replace("-", ""), serviceName.value, secretToken.value), context)
                    navController.navigate(Screen.OTP.route)
                },
                modifier = modifier.padding(bottom = 16.dp)
            ) {
                Text("Add")
            }
        }
    }
}