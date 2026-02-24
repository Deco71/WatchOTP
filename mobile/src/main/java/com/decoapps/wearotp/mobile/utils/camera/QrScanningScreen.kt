package com.decoapps.wearotp.mobile.utils.camera

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.decoapps.wearotp.mobile.screens.Screen
import com.decoapps.wearotp.mobile.screens.otp.OTPViewModel
import com.decoapps.wearotp.mobile.utils.camera.permission.RequireCameraPermission
import com.decoapps.wearotp.mobile.utils.camera.permission.NeedCameraPermissionScreen
import com.decoapps.wearotp.shared.data.OtpauthParseResult
import com.decoapps.wearotp.shared.data.OTPService
import com.decoapps.wearotp.shared.data.parseOtpauth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
@ExperimentalGetImage
fun QrScanningScreen(navController: NavController) {
    val viewModel: QrScanViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val otpViewModel: OTPViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val preview = Preview.Builder().build()
    val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    LaunchedEffect(Unit) {
        imageAnalysis.setAnalyzer(
            Dispatchers.Default.asExecutor(),
            QrCodeAnalyzer { result ->
                if (result.startsWith("otpauth://")) {
                    when (val parsed = parseOtpauth(result)) {
                        is OtpauthParseResult.Success -> {
                            val fields = parsed.fields
                            otpViewModel.saveToken(
                                OTPService(
                                    id = UUID.randomUUID().toString().replace("-", ""),
                                    issuer = fields.issuer,
                                    accountName = fields.accountName,
                                    secret = fields.secret,
                                    algorithm = fields.algorithm,
                                    digits = fields.digits,
                                    interval = fields.interval
                                ),
                                context
                            )
                            viewModel.onQrCodeDetected(result, navController)
                        }
                        is OtpauthParseResult.Error -> {
                            viewModel.onQrCodeNotValid(parsed.message)
                        }
                    }
                } else {
                    viewModel.onQrCodeNotValid("Invalid QR Code")
                }
            }
        )
    }

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(uiState.lensFacing)
        .build()

    LaunchedEffect(uiState.lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context)
        withContext(Dispatchers.IO) {
            cameraProvider.get()
        }.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        preview.surfaceProvider = previewView.surfaceProvider
    }

    RequireCameraPermission(
        deniedContent = { _, requestPermission ->
            NeedCameraPermissionScreen(
                requestPermission = requestPermission
            )
        },
        grantedContent = {
            Scaffold { paddingValues ->
                QRCodeReaderBorder(
                    modifier = Modifier.padding(paddingValues),
                    previewView = previewView,
                    navController = navController,
                    error = uiState.error
                )
            }
        }
    )
}


@Composable
private fun QRCodeReaderBorder(
    modifier: Modifier,
    previewView: PreviewView,
    navController: NavController,
    error: String? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                previewView
            }
        )
        val widthInPx: Float
        val heightInPx: Float
        val radiusInPx: Float
        with(LocalDensity.current) {
            widthInPx = 250.dp.toPx()
            heightInPx = 250.dp.toPx()
            radiusInPx = 16.dp.toPx()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .5f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = .6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Scan the QR code provided by your service",
                color = MaterialTheme.colorScheme.onSurface
            )
            Canvas(
                modifier = Modifier
                    .size(250.dp)
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
            ) {
                val offset = Offset(
                    x = (size.width - widthInPx) / 2,
                    y = (size.height - heightInPx) / 2,
                )
                val cutoutRect = Rect(offset, Size(widthInPx, heightInPx))
                // Source
                drawRoundRect(
                    topLeft = cutoutRect.topLeft,
                    size = cutoutRect.size,
                    cornerRadius = CornerRadius(radiusInPx, radiusInPx),
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear
                )
            }

            if (error != null) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = .6f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Button(
            onClick = {
                navController.popBackStack()
                navController.navigate(Screen.AddOTPManually.route)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text("Add Manually")
        }
    }
}