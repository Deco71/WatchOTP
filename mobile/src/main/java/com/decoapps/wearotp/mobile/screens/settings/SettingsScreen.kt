package com.decoapps.wearotp.mobile.screens.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.decoapps.wearotp.mobile.data.PreferencesViewModel
import com.decoapps.wearotp.mobile.theme.ColorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val preferencesViewModel: PreferencesViewModel =
        viewModel(factory = PreferencesViewModel.Factory)
    val currentColorMode = preferencesViewModel.currentColorMode.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ThemeOption(
                    label = "System default",
                    description = "Follows your device's light/dark setting",
                    selected = currentColorMode == ColorMode.SYSTEM.toString(),
                    onClick = { preferencesViewModel.saveColorMode(ColorMode.SYSTEM) }
                )
                ThemeOption(
                    label = "Light",
                    description = "Always use light theme",
                    selected = currentColorMode == ColorMode.LIGHT.toString(),
                    onClick = { preferencesViewModel.saveColorMode(ColorMode.LIGHT) }
                )
                ThemeOption(
                    label = "Dark",
                    description = "Always use dark theme",
                    selected = currentColorMode == ColorMode.DARK.toString(),
                    onClick = { preferencesViewModel.saveColorMode(ColorMode.DARK) }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ThemeOption(
                        label = "Dynamic colors",
                        description = "Use colors from your wallpaper (Android 12+)",
                        selected = currentColorMode == ColorMode.DYNAMIC.toString(),
                        onClick = { preferencesViewModel.saveColorMode(ColorMode.DYNAMIC) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
}