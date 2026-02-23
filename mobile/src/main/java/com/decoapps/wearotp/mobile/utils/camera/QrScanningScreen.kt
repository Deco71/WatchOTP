package com.decoapps.wearotp.mobile.utils.camera

import android.util.Log
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
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.decoapps.wearotp.mobile.utils.camera.permission.FeatureThatRequiresCameraPermission
import com.decoapps.wearotp.mobile.utils.camera.permission.NeedCameraPermissionScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext

typealias AndroidSize = android.util.Size

@Composable
@ExperimentalGetImage
fun QrScanningScreen() {
    val viewModel: QrScanViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val preview = Preview.Builder().build()
    val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(
            AndroidSize(previewView.width, previewView.height)
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val targetRect by remember { derivedStateOf { uiState.targetRect } }

    LaunchedEffect(targetRect) {
        imageAnalysis.setAnalyzer(
            Dispatchers.Default.asExecutor(),
            QrCodeAnalyzer(
                targetRect = android.graphics.Rect(
                    targetRect.left.toInt(),
                    targetRect.top.toInt(),
                    targetRect.right.toInt(),
                    targetRect.bottom.toInt()
                ),
                previewView = previewView,
            ) { result ->
                Log.d("QRCODETROVATO", result)
                viewModel.onQrCodeDetected(result)
            }
        )
    }

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(uiState.lensFacing)
        .build()
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(uiState.lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context)
        camera = withContext(Dispatchers.IO) {
            cameraProvider.get()
        }.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    FeatureThatRequiresCameraPermission(
        deniedContent = { isGranted, requestPermission ->
            NeedCameraPermissionScreen(
                requestPermission = requestPermission,
                shouldShowRationale = false // Puoi gestire la rationale custom se vuoi
            )
        },
        grantedContent = {
            Scaffold { paddingValues ->
                Content(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    previewView = previewView,
                    onTargetPositioned = viewModel::onTargetPositioned
                )
            }
        }
    )
}


@Composable
private fun Content(
    modifier: Modifier,
    previewView: PreviewView,
    uiState: QrScanUIState,
    onTargetPositioned: (Rect) -> Unit,
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
            Canvas(
                modifier = Modifier
                    .size(250.dp)
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .onGloballyPositioned {
                        onTargetPositioned(it.boundsInRoot())
                    }
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
        }
        if (uiState.detectedQR.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .background(Color.White.copy(alpha = .6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = uiState.detectedQR,
            )
        }
    }
}