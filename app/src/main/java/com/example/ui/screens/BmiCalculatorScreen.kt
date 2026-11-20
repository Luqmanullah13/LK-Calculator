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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CalculatorsEngine
import com.example.ui.CalculatorUiState
import com.example.ui.components.SubScreenContainer
import com.example.ui.theme.LocalLkColors
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import java.util.Locale

@Composable
fun BmiCalculatorScreen(
    state: CalculatorUiState,
    onToggleMetric: (Boolean) -> Unit,
    onWeightChange: (String) -> Unit,
    onHeightCmChange: (String) -> Unit,
    onHeightImperialChange: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current

    SubScreenContainer(
        title = "BMI Calc",
        subtitle = "BODY MASS INDEX & HEALTH STATUS",
        onBack = onBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Metric / Imperial Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { onToggleMetric(true) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (state.bmiIsMetric) NeonRed else lkColors.displayCard,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .border(1.dp, if (state.bmiIsMetric) NeonRed else lkColors.keyBorder, RoundedCornerShape(14.dp))
                        .testTag("bmi_metric_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "METRIC (KG / CM)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.bmiIsMetric) PureWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Surface(
                    onClick = { onToggleMetric(false) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (!state.bmiIsMetric) NeonRed else lkColors.displayCard,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .border(1.dp, if (!state.bmiIsMetric) NeonRed else lkColors.keyBorder, RoundedCornerShape(14.dp))
                        .testTag("bmi_imperial_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "IMPERIAL (LBS / FT)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!state.bmiIsMetric) PureWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Inputs Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(lkColors.displayCard)
                    .border(1.5.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.bmiWeightInput,
                    onValueChange = onWeightChange,
                    label = { Text(if (state.bmiIsMetric) "Weight (kg)" else "Weight (lbs)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = OrbitronFontFamily),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonRed,
                        unfocusedBorderColor = lkColors.keyBorder,
                        focusedLabelColor = NeonRed,
                        cursorColor = NeonRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bmi_weight_input")
                )

                if (state.bmiIsMetric) {
                    OutlinedTextField(
                        value = state.bmiHeightCmInput,
                        onValueChange = onHeightCmChange,
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = OrbitronFontFamily),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = lkColors.keyBorder,
                            focusedLabelColor = NeonRed,
                            cursorColor = NeonRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bmi_height_cm_input")
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.bmiHeightFtInput,
                            onValueChange = { onHeightImperialChange(it, state.bmiHeightInInput) },
                            label = { Text("Feet") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = OrbitronFontFamily),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonRed,
                                unfocusedBorderColor = lkColors.keyBorder,
                                focusedLabelColor = NeonRed,
                                cursorColor = NeonRed
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bmi_height_ft_input")
                        )
                        OutlinedTextField(
                            value = state.bmiHeightInInput,
                            onValueChange = { onHeightImperialChange(state.bmiHeightFtInput, it) },
                            label = { Text("Inches") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = OrbitronFontFamily),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonRed,
                                unfocusedBorderColor = lkColors.keyBorder,
                                focusedLabelColor = NeonRed,
                                cursorColor = NeonRed
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bmi_height_in_input")
                        )
                    }
                }
            }

            // Result Panel
            val bmiResult = state.bmiResult
            if (bmiResult != null) {
                val statusColor = when (bmiResult.status) {
                    CalculatorsEngine.BmiStatus.UNDERWEIGHT -> StatusAmber
                    CalculatorsEngine.BmiStatus.NORMAL -> StatusGreen
                    CalculatorsEngine.BmiStatus.OVERWEIGHT -> StatusOrange
                    CalculatorsEngine.BmiStatus.OBESE -> NeonRed
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(lkColors.displayCard)
                        .border(1.5.dp, statusColor, RoundedCornerShape(18.dp))
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "YOUR BMI SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = lkColors.historyText
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", bmiResult.bmi),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 48.sp
                        ),
                        color = statusColor,
                        modifier = Modifier.testTag("bmi_score_text")
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.16f))
                            .border(1.dp, statusColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = bmiResult.status.label.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor,
                            modifier = Modifier.testTag("bmi_status_text")
                        )
                    }

                    // Color-coded visual scale bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(StatusAmber))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(StatusGreen))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(StatusOrange))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(NeonRed))
                    }

                    Text(
                        text = bmiResult.status.tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = lkColors.historyText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format(
                            Locale.US,
                            "Healthy Weight Range: %.1f kg – %.1f kg",
                            bmiResult.healthyMinKg,
                            bmiResult.healthyMaxKg
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
