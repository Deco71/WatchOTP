package com.decoapps.wearotp.mobile.screens.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.decoapps.wearotp.mobile.screens.Screen
import com.decoapps.wearotp.shared.data.TOTP.generateTOTP
import java.lang.Long
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.String
import kotlin.text.uppercase
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPScreen(navController: NavController, otpViewModel: OTPViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WearOTP") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddOTP.route) }
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add new OTP")
            }
        },
        //snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        )
        {
            OTPList(Modifier, otpViewModel)
        }
    }
}



@Composable
fun OTPList(modifier: Modifier, otpViewModel: OTPViewModel) {
    val otpServices by otpViewModel.otpServices.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        otpViewModel.loadTokensFromDirectory("tokens", context)
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(otpServices) { service ->
                    OTPCard(
                        service = service,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        onDelete = {
                            otpViewModel.deleteToken(service.id, context)
                        }
                    )
                }
            }

            /*Button(
                onClick = {
                    test("I65VU7K5ZQL7WB4E")
                },
                modifier = modifier.padding(bottom = 16.dp)
            ) {
                Text("Test")
            }*/
        }
    }
}

private fun test(seed : String) {

    // Seed for HMAC-SHA1 - 20 bytes
    val T0: kotlin.Long = 0
    val X: kotlin.Long = 30

    var steps = "0"
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    df.timeZone = TimeZone.getTimeZone("UTC")
    val epoch = Instant.now().epochSecond
    val T: kotlin.Long = (epoch - T0) / X
    steps = Long.toHexString(T).uppercase(Locale.getDefault())
    val fmtTime = java.lang.String.format("%1$-11s", epoch)
    val utcTime: String? = df.format(Date(epoch * 1000))
    print(
        "|  " + fmtTime + "  |  " + utcTime +
                "  | " + steps + " |"
    )
    println(
            generateTOTP(
                seed, steps, "6",
                "HmacSHA1"
            ) + "| SHA1   |"
        )
    println(
        "+---------------+-----------------------+" +
                "------------------+--------+--------+"
    )

}
