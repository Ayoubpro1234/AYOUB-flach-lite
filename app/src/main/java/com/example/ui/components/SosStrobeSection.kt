package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.FlashlightMode
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ButtonRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FlashYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SosStrobeSection(
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    currentMode: FlashlightMode,
    onSelectMode: (FlashlightMode) -> Unit,
    strobeFrequency: Int,
    onFrequencyChange: (Int) -> Unit,
    isTorchOn: Boolean,
    modifier: Modifier = Modifier
) {
    val activeBorderColor = when (currentMode) {
        FlashlightMode.SOS -> ButtonRed.copy(alpha = 0.6f)
        FlashlightMode.STROBE -> AccentCyan.copy(alpha = 0.6f)
        else -> CardBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, activeBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleExpanded)
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (currentMode) {
                                    FlashlightMode.SOS -> ButtonRed.copy(alpha = 0.2f)
                                    FlashlightMode.STROBE -> AccentCyan.copy(alpha = 0.2f)
                                    else -> DarkSurfaceVariant
                                }
                            )
                            .border(
                                1.dp,
                                when (currentMode) {
                                    FlashlightMode.SOS -> ButtonRed.copy(alpha = 0.4f)
                                    FlashlightMode.STROBE -> AccentCyan.copy(alpha = 0.4f)
                                    else -> CardBorder
                                },
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (currentMode) {
                                FlashlightMode.SOS -> Icons.Filled.Emergency
                                FlashlightMode.STROBE -> Icons.Filled.Speed
                                else -> Icons.Filled.Warning
                            },
                            contentDescription = "Emergency SOS & Strobe Options",
                            tint = when (currentMode) {
                                FlashlightMode.SOS -> ButtonRed
                                FlashlightMode.STROBE -> AccentCyan
                                else -> FlashYellow
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Light Modes & Strobe",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isExpanded) "Tap to collapse options" else "Standard • SOS Morse • Strobe FX",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse SOS Section" else "Expand SOS Section",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Expanded Mode Options
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "ACTIVE LIGHT PATTERN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentMode == FlashlightMode.NORMAL,
                            onClick = { onSelectMode(FlashlightMode.NORMAL) },
                            label = { Text("Standard", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlashYellow,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = currentMode == FlashlightMode.SOS,
                            onClick = { onSelectMode(FlashlightMode.SOS) },
                            label = { Text("SOS Morse", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ButtonRed,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = currentMode == FlashlightMode.STROBE,
                            onClick = { onSelectMode(FlashlightMode.STROBE) },
                            label = { Text("Strobe", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Detailed Controls
                    when (currentMode) {
                        FlashlightMode.SOS -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ButtonRed.copy(alpha = 0.12f))
                                    .border(1.dp, ButtonRed.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Emergency,
                                            contentDescription = null,
                                            tint = ButtonRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Morse Code SOS Beacon",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ButtonRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Flashes distress signal: • • •  — — —  • • • (3 short, 3 long, 3 short flashes). Automatically loops while active.",
                                        fontSize = 11.5.sp,
                                        color = TextPrimary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        FlashlightMode.STROBE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Speed,
                                            contentDescription = null,
                                            tint = AccentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Strobe Frequency",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "$strobeFrequency Hz (${strobeFrequency}x / sec)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccentCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Slider(
                                    value = strobeFrequency.toFloat(),
                                    onValueChange = { onFrequencyChange(it.toInt()) },
                                    valueRange = 1f..20f,
                                    steps = 18,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentCyan,
                                        activeTrackColor = AccentCyan,
                                        inactiveTrackColor = DarkSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("strobe_frequency_slider")
                                )
                            }
                        }

                        FlashlightMode.NORMAL -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.FlashOn,
                                        contentDescription = null,
                                        tint = FlashYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Continuous max beam for everyday illumination.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        FlashlightMode.SCREEN -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Screen Lantern mode is selected. Tap 'Open Full Screen Light' below to illuminate.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
