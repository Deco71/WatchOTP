package com.decoapps.wearotp.mobile.utils.camera.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@Composable
fun RequireCameraPermission(
    deniedContent: @Composable (Boolean, () -> Unit) -> Unit,
    grantedContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    ) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
    }

    if (isGranted) {
        grantedContent()
    } else {
        deniedContent(isGranted) { launcher.launch(Manifest.permission.CAMERA) }
    }
}