package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.hardware.BatteryState
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.ButtonRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FlashYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseUser

@Composable
fun TopBarAndBattery(
    batteryState: BatteryState,
    isTorchOn: Boolean,
    activeSeconds: Long,
    currentUser: FirebaseUser?,
    onOpenSyncSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding & Icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, FlashYellow.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_ayoub_flash),
                        contentDescription = "AYOUB Flash-Lite Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AYOUB Flash-Lite",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.2.sp
                    )
                    Text(
                        text = "ULTIMATE LED TORCH",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlashYellow.copy(alpha = 0.9f),
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Battery Indicator & Settings / Cloud Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Compact Battery Indicator Chip
                BatteryChip(batteryState = batteryState)

                // Settings & Cloud Sync Button
                IconButton(
                    onClick = onOpenSyncSheet,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("account_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings & Cloud Sync",
                        tint = if (currentUser != null) FlashYellow else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Live Status Pill Banner
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val statusColor by animateColorAsState(
                targetValue = if (isTorchOn) FlashYellow else TextMuted,
                animationSpec = tween(300),
                label = "statusColor"
            )
            val minutes = activeSeconds / 60
            val seconds = activeSeconds % 60
            val timeFormatted = String.format("%02d:%02d", minutes, seconds)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant)
                    .border(
                        1.dp,
                        if (isTorchOn) FlashYellow.copy(alpha = 0.5f) else CardBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTorchOn) "TORCH ACTIVE  •  $timeFormatted" else "POWER OFF  •  READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTorchOn) TextPrimary else TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryChip(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    val level = batteryState.percentage
    val isCharging = batteryState.isCharging

    val batteryColor = when {
        isCharging -> ButtonGreen
        level <= 15 -> ButtonRed
        level <= 30 -> FlashYellow
        else -> TextPrimary
    }

    val batteryIcon = when {
        isCharging -> Icons.Filled.BatteryChargingFull
        level <= 15 -> Icons.Filled.BatteryAlert
        else -> Icons.Filled.BatteryFull
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = batteryIcon,
            contentDescription = "Battery Level $level%",
            tint = batteryColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$level%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = batteryColor
        )
    }
}
