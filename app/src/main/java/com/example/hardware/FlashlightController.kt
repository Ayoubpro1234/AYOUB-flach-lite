package com.example.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class FlashlightMode {
    NORMAL,
    SOS,
    STROBE,
    SCREEN
}

class FlashlightController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _hasFlashlight = MutableStateFlow(false)
    val hasFlashlight: StateFlow<Boolean> = _hasFlashlight.asStateFlow()

    private val _hardwareError = MutableStateFlow<String?>(null)
    val hardwareError: StateFlow<String?> = _hardwareError.asStateFlow()

    private var selectedCameraId: String? = null
    private var patternJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == selectedCameraId) {
                _isTorchOn.value = enabled
                _hardwareError.value = null
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == selectedCameraId) {
                _isTorchOn.value = false
                _hardwareError.value = "Flashlight is currently unavailable (used by camera or system)"
            }
        }
    }

    init {
        detectFlashlightHardware()
    }

    private fun detectFlashlightHardware() {
        val pm = context.packageManager
        val hasFeature = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (cameraManager == null) {
            _hasFlashlight.value = false
            _hardwareError.value = "Camera service unavailable"
            return
        }

        try {
            var rearCameraWithFlash: String? = null
            var anyCameraWithFlash: String? = null

            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(CameraCharacteristics.LENS_FACING)

                if (flashAvailable) {
                    if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                        rearCameraWithFlash = id
                        break
                    } else if (anyCameraWithFlash == null) {
                        anyCameraWithFlash = id
                    }
                }
            }

            selectedCameraId = rearCameraWithFlash ?: anyCameraWithFlash
            _hasFlashlight.value = hasFeature || (selectedCameraId != null)

            selectedCameraId?.let {
                cameraManager.registerTorchCallback(torchCallback, mainHandler)
            }
        } catch (e: Exception) {
            Log.e("FlashlightController", "Error detecting flashlight", e)
            _hasFlashlight.value = hasFeature
            _hardwareError.value = e.localizedMessage ?: "Failed to inspect camera hardware"
        }
    }

    fun setTorch(enable: Boolean): Boolean {
        stopPatternRoutine()
        return applyTorchMode(enable)
    }

    private fun applyTorchMode(enable: Boolean): Boolean {
        val cameraId = selectedCameraId ?: run {
            _hardwareError.value = "No flashlight hardware detected on this device"
            return false
        }
        val cm = cameraManager ?: run {
            _hardwareError.value = "Camera manager is unavailable"
            return false
        }

        return try {
            cm.setTorchMode(cameraId, enable)
            _isTorchOn.value = enable
            _hardwareError.value = null
            true
        } catch (e: CameraAccessException) {
            Log.e("FlashlightController", "CameraAccessException toggling torch", e)
            _hardwareError.value = "Camera access error: ${e.localizedMessage}"
            false
        } catch (e: Exception) {
            Log.e("FlashlightController", "Failed to toggle torch", e)
            _hardwareError.value = e.localizedMessage ?: "Unknown hardware error"
            false
        }
    }

    fun startSos() {
        stopPatternRoutine()
        patternJob = scope.launch {
            // Morse Code SOS: ... --- ...
            // Dot: 200ms ON, 200ms OFF
            // Dash: 600ms ON, 200ms OFF
            // Gap between letters: 400ms OFF
            // Gap between words (repeats): 1500ms OFF
            try {
                while (isActive) {
                    // S: 3 dots
                    repeat(3) {
                        if (!isActive) return@launch
                        applyTorchMode(true)
                        delay(200)
                        applyTorchMode(false)
                        delay(200)
                    }
                    delay(300)

                    // O: 3 dashes
                    repeat(3) {
                        if (!isActive) return@launch
                        applyTorchMode(true)
                        delay(600)
                        applyTorchMode(false)
                        delay(200)
                    }
                    delay(300)

                    // S: 3 dots
                    repeat(3) {
                        if (!isActive) return@launch
                        applyTorchMode(true)
                        delay(200)
                        applyTorchMode(false)
                        delay(200)
                    }
                    delay(1500)
                }
            } finally {
                applyTorchMode(false)
            }
        }
    }

    fun startStrobe(frequencyHz: Int) {
        stopPatternRoutine()
        val freq = frequencyHz.coerceIn(1, 20)
        val cycleMs = 1000L / freq
        val halfCycle = (cycleMs / 2).coerceAtLeast(25L)

        patternJob = scope.launch {
            try {
                while (isActive) {
                    applyTorchMode(true)
                    delay(halfCycle)
                    applyTorchMode(false)
                    delay(halfCycle)
                }
            } finally {
                applyTorchMode(false)
            }
        }
    }

    fun stopPatternRoutine() {
        patternJob?.cancel()
        patternJob = null
    }

    fun release() {
        stopPatternRoutine()
        applyTorchMode(false)
        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            Log.w("FlashlightController", "Error unregistering torch callback", e)
        }
    }
}
