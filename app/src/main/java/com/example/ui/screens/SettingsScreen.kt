package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserSettings
import com.example.ui.components.SubScreenContainer
import com.example.ui.theme.LocalLkColors
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureWhite

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onSelectDecimalPlaces: (Int) -> Unit,
    onToggleVibrate: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onToggleDegreeMode: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current

    SubScreenContainer(
        title = "Settings",
        subtitle = "PRECISION • HAPTICS • THEME",
        onBack = onBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Decimal Places Selector: 0, 2, 4
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(lkColors.displayCard)
                    .border(1.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DECIMAL PLACES",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRed
                )
                Text(
                    text = "Select precision for calculated results",
                    style = MaterialTheme.typography.bodyMedium,
                    color = lkColors.historyText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(0, 2, 4).forEach { places ->
                        val selected = settings.decimalPlaces == places
                        Surface(
                            onClick = { onSelectDecimalPlaces(places) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) NeonRed else lkColors.keySciBackground,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    1.5.dp,
                                    if (selected) NeonRed else lkColors.keyBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .testTag("decimal_places_$places")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$places",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = OrbitronFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Toggle Switches
            SettingsSwitchCard(
                title = "Vibrate on Press",
                subtitle = "Tactile haptic feedback when tapping buttons",
                checked = settings.vibrateEnabled,
                onCheckedChange = { onToggleVibrate() },
                testTag = "settings_vibrate_switch"
            )

            SettingsSwitchCard(
                title = "Button Click Sound",
                subtitle = "Gamer mechanical click sound on key press",
                checked = settings.soundEnabled,
                onCheckedChange = { onToggleSound() },
                testTag = "settings_sound_switch"
            )

            SettingsSwitchCard(
                title = "Dark Mode (Black & Red)",
                subtitle = "Switch between pure black theme and light mode",
                checked = settings.isDarkTheme,
                onCheckedChange = { onToggleDarkTheme() },
                testTag = "settings_theme_switch"
            )

            SettingsSwitchCard(
                title = "Trigonometry in Degrees",
                subtitle = if (settings.isDegreeMode) "Currently DEG (sin 90 = 1)" else "Currently RAD (sin π/2 = 1)",
                checked = settings.isDegreeMode,
                onCheckedChange = { onToggleDegreeMode() },
                testTag = "settings_degree_switch"
            )

            // App Identity Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(lkColors.displayCard)
                    .border(1.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "LK CALCULATOR // SMART CALC",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = OrbitronFontFamily),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Package: com.lk.calculator • v1.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = lkColors.historyText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val lkColors = LocalLkColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(lkColors.displayCard)
            .border(1.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = lkColors.historyText
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = NeonRed,
                uncheckedThumbColor = lkColors.historyText,
                uncheckedTrackColor = lkColors.keySciBackground
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
