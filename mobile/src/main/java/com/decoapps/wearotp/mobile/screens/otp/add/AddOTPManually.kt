package com.decoapps.wearotp.mobile.screens.otp.add

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val SUPPORTED_ALGORITHMS = listOf("SHA1", "SHA256", "SHA512")
private val SUPPORTED_DIGITS = listOf(6, 8)
private val SUPPORTED_INTERVALS = listOf(30, 60)

// Base32 alphabet (RFC 4648)
private val BASE32_REGEX = Regex("^[A-Z2-7]+=*$", RegexOption.IGNORE_CASE)

private fun isValidBase32(secret: String): Boolean {
    val trimmed = secret.trim()
    if (trimmed.isEmpty()) return false
    return BASE32_REGEX.matches(trimmed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOTPManually(modifier: Modifier = Modifier, navController: NavController) {
    var issuer by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var selectedAlgorithm by remember { mutableStateOf(SUPPORTED_ALGORITHMS[0]) }
    var selectedDigits by remember { mutableStateOf(SUPPORTED_DIGITS[0]) }
    var selectedInterval by remember { mutableStateOf(SUPPORTED_INTERVALS[0]) }

    var issuerError by remember { mutableStateOf<String?>(null) }
    var secretError by remember { mutableStateOf<String?>(null) }

    var algorithmExpanded by remember { mutableStateOf(false) }
    var digitsExpanded by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    val otpViewModel: OTPViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    val context = LocalContext.current

    fun validate(): Boolean {
        var valid = true
        if (issuer.isBlank()) {
            issuerError = "Issuer is required"
            valid = false
        } else {
            issuerError = null
        }
        if (secret.isBlank()) {
            secretError = "Secret is required"
            valid = false
        } else if (!isValidBase32(secret)) {
            secretError = "Secret must be a valid Base32 string"
            valid = false
        } else {
            secretError = null
        }
        return valid
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            // Issuer (required)
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = issuer,
                    onValueChange = { issuer = it; issuerError = null },
                    label = { Text("Issuer *") },
                    isError = issuerError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (issuerError != null) {
                    Text(
                        text = issuerError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            // Account (optional)
            TextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("Account (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Secret (required)
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = secret,
                    onValueChange = { secret = it; secretError = null },
                    label = { Text("Secret *") },
                    isError = secretError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (secretError != null) {
                    Text(
                        text = secretError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            // Algorithm dropdown
            ExposedDropdownMenuBox(
                expanded = algorithmExpanded,
                onExpandedChange = { algorithmExpanded = !algorithmExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedAlgorithm,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Algorithm") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = algorithmExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = algorithmExpanded,
                    onDismissRequest = { algorithmExpanded = false }
                ) {
                    SUPPORTED_ALGORITHMS.forEach { algo ->
                        DropdownMenuItem(
                            text = { Text(algo) },
                            onClick = { selectedAlgorithm = algo; algorithmExpanded = false }
                        )
                    }
                }
            }

            // Digits dropdown
            ExposedDropdownMenuBox(
                expanded = digitsExpanded,
                onExpandedChange = { digitsExpanded = !digitsExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedDigits.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Digits") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = digitsExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = digitsExpanded,
                    onDismissRequest = { digitsExpanded = false }
                ) {
                    SUPPORTED_DIGITS.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d.toString()) },
                            onClick = { selectedDigits = d; digitsExpanded = false }
                        )
                    }
                }
            }

            // Interval dropdown
            ExposedDropdownMenuBox(
                expanded = intervalExpanded,
                onExpandedChange = { intervalExpanded = !intervalExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = "${selectedInterval}s",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Interval") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = intervalExpanded,
                    onDismissRequest = { intervalExpanded = false }
                ) {
                    SUPPORTED_INTERVALS.forEach { i ->
                        DropdownMenuItem(
                            text = { Text("${i}s") },
                            onClick = { selectedInterval = i; intervalExpanded = false }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (validate()) {
                            otpViewModel.saveToken(
                                OTPService(
                                    id = UUID.randomUUID().toString().replace("-", ""),
                                    issuer = issuer.trim(),
                                    accountName = account.trim().ifBlank { null },
                                    secret = secret.trim().uppercase(),
                                    algorithm = selectedAlgorithm,
                                    digits = selectedDigits,
                                    interval = selectedInterval
                                ),
                                context
                            )
                            navController.navigate(Screen.OTP.route)
                        }
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }
}