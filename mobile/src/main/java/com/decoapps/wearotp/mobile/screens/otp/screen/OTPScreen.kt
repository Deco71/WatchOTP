package com.decoapps.wearotp.mobile.screens.otp.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.decoapps.wearotp.mobile.screens.Screen
import com.decoapps.wearotp.mobile.screens.otp.OTPViewModel
import com.decoapps.wearotp.mobile.screens.otp.card.OTPCard
/*import com.decoapps.wearotp.shared.data.TOTP.generateTOTP
import java.lang.Long
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.String
import kotlin.text.uppercase
import java.time.Instant*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPScreen(navController: NavController) {
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
            OTPList(Modifier)
        }
    }
}



@Composable
fun OTPList(modifier: Modifier) {
    val otpViewModel: OTPViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    val otpServices by otpViewModel.otpServices.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        otpViewModel.loadTokensFromDirectory(context)
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
                if (otpServices.isEmpty()) {
                    item {
                        Text(
                            text = "No TOTP found. Please add one by using the add buton.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
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
            }

        }
    }
}
