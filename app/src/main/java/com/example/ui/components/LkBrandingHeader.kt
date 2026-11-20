package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

@Composable
fun LkCrownLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Box(
        modifier = modifier.size(width = size, height = size + 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Crown perched at the top of the circle
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = size * 0.56f, height = 18.dp)
                .offset(y = 2.dp)
        ) {
            val w = this.size.width
            val h = this.size.height
            val crownPath = Path().apply {
                moveTo(w * 0.08f, h * 0.92f)
                lineTo(w * 0.0f, h * 0.28f)
                lineTo(w * 0.28f, h * 0.56f)
                lineTo(w * 0.50f, h * 0.06f)
                lineTo(w * 0.72f, h * 0.56f)
                lineTo(w * 1.0f, h * 0.28f)
                lineTo(w * 0.92f, h * 0.92f)
                close()
            }
            drawPath(
                path = crownPath,
                brush = Brush.verticalGradient(
                    colors = listOf(PureWhite, NeonRed)
                )
            )
            drawPath(
                path = crownPath,
                color = NeonRed,
                style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Perfect Circle: Red Border, Black Inside, Big Bold "LK" Red + White Gradient
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(PureBlack)
                .border(width = 2.5.dp, color = NeonRed, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Subtle inner red aura ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonRed.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(this.size.width / 2f, this.size.height / 2f),
                        radius = this.size.minDimension * 0.5f
                    )
                )
            }
            Text(
                text = "LK",
                style = TextStyle(
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.40f).sp,
                    letterSpacing = 1.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(NeonRed, PureWhite)
                    )
                )
            )
        }
    }
}

@Composable
fun LkBrandingHeader(
    isDarkTheme: Boolean,
    isSoundEnabled: Boolean,
    onToggleTheme: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Centered LK Crown Logo
            LkCrownLogo(
                size = 54.dp,
                modifier = Modifier.align(Alignment.Center)
            )

            // Top Right Quick Toggles: Dark/Light, Sound, Settings
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .testTag("toggle_sound_button")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = if (isSoundEnabled) "Mute click sound" else "Enable click sound",
                        tint = if (isSoundEnabled) NeonRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .testTag("toggle_theme_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Open Settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "LK CALCULATOR",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .background(NeonRed, RoundedCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "// SMART CALC",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .background(NeonRed, RoundedCornerShape(1.dp))
            )
        }
    }
}
