package com.example.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryState(
    val percentage: Int = 100,
    val isCharging: Boolean = false
)

class BatteryMonitor(private val context: Context) {

    private val _batteryState = MutableStateFlow(readCurrentBattery())
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { parseBatteryIntent(it) }
        }
    }

    private var isRegistered = false

    fun startListening() {
        if (!isRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = context.registerReceiver(receiver, filter)
            stickyIntent?.let { parseBatteryIntent(it) }
            isRegistered = true
        }
    }

    fun stopListening() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            isRegistered = false
        }
    }

    private fun readCurrentBattery(): BatteryState {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        return intent?.let { extractBattery(it) } ?: BatteryState()
    }

    private fun parseBatteryIntent(intent: Intent) {
        _batteryState.value = extractBattery(intent)
    }

    private fun extractBattery(intent: Intent): BatteryState {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            100
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        return BatteryState(
            percentage = percent.coerceIn(0, 100),
            isCharging = isCharging
        )
    }
}
