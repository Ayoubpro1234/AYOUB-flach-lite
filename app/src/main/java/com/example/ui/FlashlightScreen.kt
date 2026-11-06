package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hardware.FlashlightMode
import com.example.ui.components.CloudSyncSheet
import com.example.ui.components.FlashlightGraphic
import com.example.ui.components.HardwareStatusPanel
import com.example.ui.components.PowerControls
import com.example.ui.components.ScreenLightModal
import com.example.ui.components.SosStrobeSection
import com.example.ui.components.TopBarAndBattery
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ButtonRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FlashYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FlashlightScreen(
    viewModel: FlashlightViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.authManager.currentUser.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var isSyncSheetOpen by remember { mutableStateOf(false) }
    var isScreenLightOpen by remember { mutableStateOf(false) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    // Camera permission check & request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setCameraPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setCameraPermissionGranted(hasPermission)
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearInfoMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = statusBarPadding.calculateTopPadding())
                .padding(bottom = navBarPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Header with Battery & Settings
            TopBarAndBattery(
                batteryState = uiState.batteryState,
                isTorchOn = uiState.isTorchOn,
                activeSeconds = uiState.activeSeconds,
                currentUser = currentUser,
                onOpenSyncSheet = { isSyncSheetOpen = true }
            )

            // Camera Permission Warning Banner if missing
            if (!uiState.hasCameraPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = ButtonRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ButtonRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = ButtonRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Camera Permission Required",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ButtonRed
                            )
                            Text(
                                text = "Required to control the physical rear LED flash.",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Hardware notice if device lacks LED flash
            if (!uiState.hasFlashlight) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = FlashYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Camera LED not detected on this hardware. Screen Lantern mode is active as primary light.",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Interactive Flashlight Control Centerpiece (Master Toggle)
            FlashlightGraphic(
                isTorchOn = uiState.isTorchOn,
                onToggle = { viewModel.toggleTorch() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tap hint and Current Mode Subtitle Tag
            Text(
                text = if (uiState.isTorchOn) "FLASHLIGHT ACTIVE  •  TAP TO POWER OFF" else "READY  •  TAP CENTER OR USE ON / OFF",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (uiState.isTorchOn) FlashYellow else TextSecondary,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Hardware ON / OFF Buttons
            PowerControls(
                isTorchOn = uiState.isTorchOn,
                onTurnOn = { viewModel.turnOn() },
                onTurnOff = { viewModel.turnOff() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Mode Shortcut Bar (Screen Light)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { isScreenLightOpen = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("screen_lantern_shortcut_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhoneAndroid,
                        contentDescription = null,
                        tint = FlashYellow,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Screen Lantern Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Emergency SOS and Strobe Feature Card
            SosStrobeSection(
                isExpanded = uiState.isSosSectionVisible,
                onToggleExpanded = { viewModel.toggleSosSection() },
                currentMode = uiState.mode,
                onSelectMode = { viewModel.setMode(it) },
                strobeFrequency = uiState.strobeFrequency,
                onFrequencyChange = { viewModel.setStrobeFrequency(it) },
                isTorchOn = uiState.isTorchOn
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Authentic Android Hardware API Status Diagnostics Card
            HardwareStatusPanel(
                isTorchOn = uiState.isTorchOn,
                hasFlashlight = uiState.hasFlashlight,
                hasCameraPermission = uiState.hasCameraPermission,
                batteryState = uiState.batteryState
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Fullscreen Screen Light Modal
        ScreenLightModal(
            isOpen = isScreenLightOpen,
            onDismiss = { isScreenLightOpen = false },
            currentColorHex = uiState.screenLightColorHex,
            onColorChange = { viewModel.setScreenLightColor(it) },
            brightness = uiState.screenBrightness,
            onBrightnessChange = { viewModel.setScreenBrightness(it) }
        )

        // Cloud Synchronization & Profile Sheet
        CloudSyncSheet(
            isOpen = isSyncSheetOpen,
            onDismiss = { isSyncSheetOpen = false },
            currentUser = currentUser,
            authManager = viewModel.authManager,
            isSyncing = uiState.isSyncing,
            onSaveSettings = { viewModel.saveSettings() },
            hapticEnabled = uiState.hapticEnabled,
            onToggleHaptic = { viewModel.toggleHapticFeedback() },
            recentLogs = uiState.recentLogs
        )
    }
}
