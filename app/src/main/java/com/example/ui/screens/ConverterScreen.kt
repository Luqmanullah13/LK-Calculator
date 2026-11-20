package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.domain.ConverterCategory
import com.example.domain.UnitCatalog
import com.example.domain.UnitItem
import com.example.ui.CalculatorUiState
import com.example.ui.components.SubScreenContainer
import com.example.ui.theme.LocalLkColors
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureWhite

@Composable
fun ConverterScreen(
    state: CalculatorUiState,
    settings: UserSettings,
    onSelectCategory: (ConverterCategory) -> Unit,
    onInputChange: (String) -> Unit,
    onSelectFromUnit: (String) -> Unit,
    onSelectToUnit: (String) -> Unit,
    onSwapUnits: () -> Unit,
    onRefreshRates: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current
    val units = UnitCatalog.unitsFor(state.converterCategory)

    SubScreenContainer(
        title = "Converter",
        subtitle = "LENGTH • WEIGHT • TEMP • CURRENCY",
        onBack = onBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConverterCategory.entries.forEach { category ->
                    val selected = category == state.converterCategory
                    Surface(
                        onClick = { onSelectCategory(category) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) NeonRed else lkColors.displayCard,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(
                                1.dp,
                                if (selected) NeonRed else lkColors.keyBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .testTag("converter_tab_${category.name}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Currency Live Refresh Row
            if (state.converterCategory == ConverterCategory.CURRENCY) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(lkColors.displayCard)
                        .border(1.dp, lkColors.keyBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EXCHANGE RATES",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonRed
                        )
                        Text(
                            text = settings.ratesUpdatedLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = lkColors.historyText
                        )
                    }

                    IconButton(
                        onClick = onRefreshRates,
                        enabled = !state.isRefreshingRates,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("refresh_currency_button")
                    ) {
                        if (state.isRefreshingRates) {
                            CircularProgressIndicator(
                                color = NeonRed,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh live currency rates",
                                tint = NeonRed
                            )
                        }
                    }
                }
            }

            // Input Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(lkColors.displayCard)
                    .border(1.5.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "FROM (${state.converterFromUnit})",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRed
                )

                UnitChipSelector(
                    units = units,
                    selectedCode = state.converterFromUnit,
                    onSelect = onSelectFromUnit,
                    tagPrefix = "from_unit"
                )

                OutlinedTextField(
                    value = state.converterInput,
                    onValueChange = onInputChange,
                    label = { Text("Enter value") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonRed,
                        unfocusedBorderColor = lkColors.keyBorder,
                        focusedLabelColor = NeonRed,
                        cursorColor = NeonRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("converter_input_field")
                )
            }

            // Swap Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSwapUnits,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NeonRed)
                        .testTag("swap_units_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapVert,
                        contentDescription = "Swap units",
                        tint = PureWhite
                    )
                }
            }

            // Output Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(lkColors.displayCard)
                    .border(1.5.dp, NeonRed.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TO (${state.converterToUnit})",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRed
                )

                UnitChipSelector(
                    units = units,
                    selectedCode = state.converterToUnit,
                    onSelect = onSelectToUnit,
                    tagPrefix = "to_unit"
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${state.converterOutput} ${state.converterToUnit}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("converter_output_text")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UnitChipSelector(
    units: List<UnitItem>,
    selectedCode: String,
    onSelect: (String) -> Unit,
    tagPrefix: String
) {
    val lkColors = LocalLkColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        units.forEach { unit ->
            val selected = unit.code == selectedCode
            Surface(
                onClick = { onSelect(unit.code) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) NeonRed else lkColors.keySciBackground,
                modifier = Modifier
                    .height(38.dp)
                    .border(
                        1.dp,
                        if (selected) NeonRed else lkColors.keyBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("${tagPrefix}_${unit.code}")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unit.code,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
