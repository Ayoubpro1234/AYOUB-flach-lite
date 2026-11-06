package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.data.model.FlashlightLog
import com.example.data.model.UserSettings
import com.example.data.repository.FlashlightRepository
import com.example.hardware.BatteryMonitor
import com.example.hardware.BatteryState
import com.example.hardware.FlashlightController
import com.example.hardware.FlashlightMode
import com.example.hardware.HapticController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FlashlightUiState(
    val isTorchOn: Boolean = false,
    val mode: FlashlightMode = FlashlightMode.NORMAL,
    val strobeFrequency: Int = 5,
    val activeSeconds: Long = 0L,
    val hasFlashlight: Boolean = true,
    val hasCameraPermission: Boolean = false,
    val batteryState: BatteryState = BatteryState(),
    val hapticEnabled: Boolean = true,
    val screenLightColorHex: String = "#FFFFFF",
    val screenBrightness: Float = 1.0f,
    val isSosSectionVisible: Boolean = false,
    val userSettings: UserSettings? = null,
    val recentLogs: List<FlashlightLog> = emptyList(),
    val isSyncing: Boolean = false,
    val hardwareError: String? = null,
    val infoMessage: String? = null
)

class FlashlightViewModel(application: Application) : AndroidViewModel(application) {

    val flashlightController = FlashlightController(application)
    private val batteryMonitor = BatteryMonitor(application)
    private val hapticController = HapticController(application)
    val authManager = AuthManager(application)
    private val repository = FlashlightRepository(application)

    private val _uiState = MutableStateFlow(FlashlightUiState())
    val uiState: StateFlow<FlashlightUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var torchSessionStartSeconds: Long = 0L

    init {
        batteryMonitor.startListening()

        // Sync torch state from hardware controller
        flashlightController.isTorchOn.onEach { isOn ->
            _uiState.value = _uiState.value.copy(isTorchOn = isOn)
            if (isOn) {
                startActiveTimer()
            } else {
                stopActiveTimer()
            }
        }.launchIn(viewModelScope)

        flashlightController.hasFlashlight.onEach { hasFlash ->
            _uiState.value = _uiState.value.copy(hasFlashlight = hasFlash)
        }.launchIn(viewModelScope)

        flashlightController.hardwareError.onEach { error ->
            _uiState.value = _uiState.value.copy(hardwareError = error)
        }.launchIn(viewModelScope)

        batteryMonitor.batteryState.onEach { bState ->
            _uiState.value = _uiState.value.copy(batteryState = bState)
        }.launchIn(viewModelScope)

        // Silent auto sign-in attempt
        authManager.attemptAutoSignIn(viewModelScope)

        // Observe auth changes to bind cloud sync
        authManager.currentUser.onEach { user ->
            if (user != null) {
                observeCloudData()
            } else {
                _uiState.value = _uiState.value.copy(
                    userSettings = null,
                    recentLogs = emptyList()
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeCloudData() {
        repository.observeUserSettings()
            .onEach { settings ->
                if (settings != null) {
                    val mode = when (settings.defaultMode) {
                        "SOS" -> FlashlightMode.SOS
                        "STROBE" -> FlashlightMode.STROBE
                        else -> FlashlightMode.NORMAL
                    }
                    _uiState.value = _uiState.value.copy(
                        userSettings = settings,
                        mode = mode,
                        strobeFrequency = settings.strobeFrequency,
                        hapticEnabled = settings.hapticFeedback,
                        screenLightColorHex = settings.screenLightColor,
                        screenBrightness = settings.screenBrightness.toFloat()
                    )
                }
            }
            .catch { /* error handled by repository */ }
            .launchIn(viewModelScope)

        repository.observeRecentLogs(10)
            .onEach { logs ->
                _uiState.value = _uiState.value.copy(recentLogs = logs)
            }
            .catch { /* error handled by repository */ }
            .launchIn(viewModelScope)
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasCameraPermission = granted)
    }

    fun turnOn() {
        if (_uiState.value.hapticEnabled) {
            hapticController.vibrateClick()
        }

        when (_uiState.value.mode) {
            FlashlightMode.NORMAL -> {
                flashlightController.setTorch(true)
            }
            FlashlightMode.SOS -> {
                flashlightController.startSos()
            }
            FlashlightMode.STROBE -> {
                flashlightController.startStrobe(_uiState.value.strobeFrequency)
            }
            FlashlightMode.SCREEN -> {
                _uiState.value = _uiState.value.copy(isTorchOn = true)
                startActiveTimer()
            }
        }
    }

    fun turnOff() {
        if (_uiState.value.hapticEnabled) {
            hapticController.vibrateClick()
        }

        val duration = _uiState.value.activeSeconds.toInt()
        val currentMode = _uiState.value.mode

        flashlightController.setTorch(false)
        _uiState.value = _uiState.value.copy(isTorchOn = false)
        stopActiveTimer()

        // Log session to cloud if user is signed in
        if (duration > 1 && authManager.currentUser.value != null) {
            viewModelScope.launch {
                try {
                    repository.addLog(currentMode.name, duration)
                } catch (e: Exception) {
                    // Handled in repo
                }
            }
        }
    }

    fun toggleTorch() {
        if (_uiState.value.isTorchOn) {
            turnOff()
        } else {
            turnOn()
        }
    }

    fun setMode(newMode: FlashlightMode) {
        if (_uiState.value.hapticEnabled) {
            hapticController.vibrateClick()
        }

        val wasOn = _uiState.value.isTorchOn
        _uiState.value = _uiState.value.copy(mode = newMode)

        if (wasOn) {
            when (newMode) {
                FlashlightMode.NORMAL -> flashlightController.setTorch(true)
                FlashlightMode.SOS -> flashlightController.startSos()
                FlashlightMode.STROBE -> flashlightController.startStrobe(_uiState.value.strobeFrequency)
                FlashlightMode.SCREEN -> {
                    flashlightController.setTorch(false)
                }
            }
        }
    }

    fun setStrobeFrequency(frequencyHz: Int) {
        val freq = frequencyHz.coerceIn(1, 20)
        _uiState.value = _uiState.value.copy(strobeFrequency = freq)
        if (_uiState.value.isTorchOn && _uiState.value.mode == FlashlightMode.STROBE) {
            flashlightController.startStrobe(freq)
        }
    }

    fun toggleHapticFeedback() {
        val next = !_uiState.value.hapticEnabled
        _uiState.value = _uiState.value.copy(hapticEnabled = next)
        if (next) {
            hapticController.vibrateClick()
        }
    }

    fun setScreenLightColor(hex: String) {
        _uiState.value = _uiState.value.copy(screenLightColorHex = hex)
    }

    fun setScreenBrightness(brightness: Float) {
        _uiState.value = _uiState.value.copy(screenBrightness = brightness.coerceIn(0.1f, 1.0f))
    }

    fun toggleSosSection() {
        if (_uiState.value.hapticEnabled) {
            hapticController.vibrateClick()
        }
        _uiState.value = _uiState.value.copy(
            isSosSectionVisible = !_uiState.value.isSosSectionVisible
        )
    }

    fun saveSettings() {
        val currentUser = authManager.currentUser.value ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                repository.saveUserSettings(
                    defaultMode = _uiState.value.mode.name,
                    strobeFrequency = _uiState.value.strobeFrequency,
                    hapticFeedback = _uiState.value.hapticEnabled,
                    screenLightColor = _uiState.value.screenLightColorHex,
                    screenBrightness = _uiState.value.screenBrightness.toDouble()
                )
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    infoMessage = "Settings synced to cloud!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    infoMessage = "Failed to sync settings"
                )
            }
        }
    }

    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    private fun startActiveTimer() {
        timerJob?.cancel()
        torchSessionStartSeconds = 0L
        _uiState.value = _uiState.value.copy(activeSeconds = 0L)
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                torchSessionStartSeconds++
                _uiState.value = _uiState.value.copy(activeSeconds = torchSessionStartSeconds)
            }
        }
    }

    private fun stopActiveTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        batteryMonitor.stopListening()
        flashlightController.release()
    }
}
