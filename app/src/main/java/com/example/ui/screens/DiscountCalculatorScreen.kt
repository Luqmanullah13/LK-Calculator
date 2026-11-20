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
import androidx.compose.material3.HorizontalDivider
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
import com.example.domain.MathEngine
import com.example.ui.CalculatorUiState
import com.example.ui.components.SubScreenContainer
import com.example.ui.theme.LocalLkColors
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusGreen

@Composable
fun DiscountCalculatorScreen(
    state: CalculatorUiState,
    decimalPlaces: Int,
    onPriceChange: (String) -> Unit,
    onDiscountChange: (String) -> Unit,
    onTaxChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current
    val quickDiscounts = listOf("10", "20", "25", "50", "75")

    SubScreenContainer(
        title = "Discount",
        subtitle = "PRICE • SAVINGS • FINAL PRICE",
        onBack = onBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                    value = state.discountPriceInput,
                    onValueChange = onPriceChange,
                    label = { Text("Original Price") },
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
                        .testTag("discount_price_input")
                )

                OutlinedTextField(
                    value = state.discountPercentInput,
                    onValueChange = onDiscountChange,
                    label = { Text("Discount % (0 - 100)") },
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
                        .testTag("discount_percent_input")
                )

                // Quick discount preset chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickDiscounts.forEach { pct ->
                        val active = state.discountPercentInput == pct
                        Surface(
                            onClick = { onDiscountChange(pct) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) NeonRed else lkColors.keySciBackground,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .border(
                                    1.dp,
                                    if (active) NeonRed else lkColors.keyBorder,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) PureWhite else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = state.discountTaxInput,
                    onValueChange = onTaxChange,
                    label = { Text("Sales Tax % (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = OrbitronFontFamily),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonRed,
                        unfocusedBorderColor = lkColors.keyBorder,
                        focusedLabelColor = NeonRed,
                        cursorColor = NeonRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("discount_tax_input")
                )
            }

            val result = state.discountResult
            if (result != null) {
                val precision = decimalPlaces.coerceAtLeast(2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(lkColors.displayCard)
                        .border(1.5.dp, NeonRed, RoundedCornerShape(18.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "FINAL PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRed
                    )
                    Text(
                        text = MathEngine.formatNumber(result.finalPrice, precision),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 40.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("discount_final_price_text")
                    )

                    HorizontalDivider(color = lkColors.keyBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "You Save",
                            style = MaterialTheme.typography.bodyLarge,
                            color = lkColors.historyText
                        )
                        Text(
                            text = MathEngine.formatNumber(result.discountAmount, precision),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = StatusGreen,
                            modifier = Modifier.testTag("discount_saved_text")
                        )
                    }

                    if (result.taxAmount > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tax Added",
                                style = MaterialTheme.typography.bodyLarge,
                                color = lkColors.historyText
                            )
                            Text(
                                text = MathEngine.formatNumber(result.taxAmount, precision),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(lkColors.keyOperatorBackground)
                        .border(1.dp, NeonRed, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Enter valid price and discount (0% - 100%).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
