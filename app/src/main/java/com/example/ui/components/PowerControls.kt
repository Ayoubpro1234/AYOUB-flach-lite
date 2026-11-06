package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.ButtonRed

@Composable
fun PowerControls(
    isTorchOn: Boolean,
    onTurnOn: () -> Unit,
    onTurnOff: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==================== LARGE GREEN ON BUTTON ====================
        val onBgBrush = if (isTorchOn) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF00FF88),
                    Color(0xFF00C853),
                    Color(0xFF009624)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF14291B),
                    Color(0xFF0A180F)
                )
            )
        }

        val onBorderColor by animateColorAsState(
            targetValue = if (isTorchOn) Color(0xFF80FFAE) else ButtonGreen.copy(alpha = 0.65f),
            animationSpec = tween(200),
            label = "onBorder"
        )

        Button(
            onClick = onTurnOn,
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .testTag("flashlight_on_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.8.dp, onBorderColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(onBgBrush, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Power,
                        contentDescription = "Turn Flashlight ON",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ON",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = if (isTorchOn) "ACTIVE" else "POWER ON",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTorchOn) Color.White.copy(alpha = 0.9f) else Color(0xFF85FFB8),
                            letterSpacing = 1.sp,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        // ==================== LARGE RED OFF BUTTON ====================
        val offBgBrush = if (!isTorchOn) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFF3B5C),
                    Color(0xFFE51A40),
                    Color(0xFFB30C2A)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF2C1318),
                    Color(0xFF19090D)
                )
            )
        }

        val offBorderColor by animateColorAsState(
            targetValue = if (!isTorchOn) Color(0xFFFF94A6) else ButtonRed.copy(alpha = 0.65f),
            animationSpec = tween(200),
            label = "offBorder"
        )

        Button(
            onClick = onTurnOff,
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .testTag("flashlight_off_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.8.dp, offBorderColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(offBgBrush, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerOff,
                        contentDescription = "Turn Flashlight OFF",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OFF",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = if (!isTorchOn) "STANDBY" else "SHUT DOWN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isTorchOn) Color.White.copy(alpha = 0.9f) else Color(0xFFFF9EAE),
                            letterSpacing = 1.sp,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }
    }
}
