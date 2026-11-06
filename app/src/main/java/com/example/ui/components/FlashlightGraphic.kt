package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FlashYellow
import com.example.ui.theme.FlashYellowBright
import com.example.ui.theme.FlashYellowGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FlashlightGraphic(
    isTorchOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beam_glow_pulse")

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .height(300.dp)
            .testTag("flashlight_center_graphic")
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (isTorchOn) Color(0xFF1F1B05) else Color(0xFF111118),
                        if (isTorchOn) Color(0xFF141203) else Color(0xFF0C0C12),
                        DarkSurface
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = if (isTorchOn) {
                    Brush.verticalGradient(
                        listOf(
                            FlashYellow.copy(alpha = 0.85f),
                            FlashYellow.copy(alpha = 0.3f),
                            Color(0xFF2C2A10)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            CardBorder,
                            Color(0xFF161622)
                        )
                    )
                },
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Upward Volumetric Light Beam Projection Canvas
        if (isTorchOn) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Beam origin at center of the flashlight head (around Y = 44% height)
                val beamOriginX = width / 2f
                val beamOriginY = height * 0.44f

                // Wide trapezoid expanding towards top
                val beamPath = Path().apply {
                    moveTo(beamOriginX - 36.dp.toPx(), beamOriginY)
                    lineTo(beamOriginX + 36.dp.toPx(), beamOriginY)
                    lineTo(width + 40.dp.toPx(), 0f)
                    lineTo(-40.dp.toPx(), 0f)
                    close()
                }

                drawPath(
                    path = beamPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FlashYellow.copy(alpha = 0.45f * pulseGlow),
                            FlashYellowBright.copy(alpha = 0.35f * pulseGlow),
                            FlashYellowGlow.copy(alpha = 0.15f * pulseGlow),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = beamOriginY + 20.dp.toPx()
                    )
                )

                // Radial spotlight bloom at flashlight lens
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f * pulseGlow),
                            FlashYellowBright.copy(alpha = 0.8f * pulseGlow),
                            FlashYellow.copy(alpha = 0.4f * pulseGlow),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(beamOriginX, beamOriginY - 10.dp.toPx()),
                        radius = 85.dp.toPx()
                    ),
                    radius = 85.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(beamOriginX, beamOriginY - 10.dp.toPx())
                )
            }
        }

        // Realistic 3D Center Flashlight Illustration
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.img_flashlight_center),
                contentDescription = if (isTorchOn) "Flashlight Illumination Active" else "Flashlight Standby",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(if (isTorchOn) 1.02f else 0.98f),
                contentScale = ContentScale.Fit,
                colorFilter = if (!isTorchOn) {
                    ColorFilter.tint(Color(0xFF888899), blendMode = BlendMode.Modulate)
                } else null
            )
        }

        // Top Status Pill inside graphic (Authentic Callback State)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isTorchOn) Color(0xDD282200) else Color(0xCC181824))
                .border(
                    1.dp,
                    if (isTorchOn) FlashYellow.copy(alpha = 0.6f) else Color(0xFF282838),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = if (isTorchOn) FlashYellow else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTorchOn) "CAMERA LED TORCH • ACTIVE" else "CAMERA LED TORCH • STANDBY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isTorchOn) FlashYellow else TextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom Interactive Guidance Strip
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xEE09090E),
                            Color(0xFF09090E)
                        )
                    )
                )
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isTorchOn) FlashYellow else Color(0xFF454558))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTorchOn) "CAMERA HARDWARE: setTorchMode(true)" else "TAP CENTER OR USE HARDWARE ON/OFF CONTROLS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTorchOn) TextPrimary else TextMuted,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
